package com.sandeep.smsspamfilter;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.sandeep.smsspamfilter", appContext.getPackageName());
    }
}

package com.sandeep.smsspamfilter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}

package com.sandeep.smsspamfilter.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sandeep.smsspamfilter.model.Message;

import java.util.ArrayList;
import java.util.List;

public class SMSFetcher {
    public static List<Message> fetchSMS(Context context) {
        List<Message> smsList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String senderName = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                smsList.add(new Message(message,senderName,-1));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return smsList;
    }
}


package com.sandeep.smsspamfilter.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;
        import com.android.volley.Request;
        import com.android.volley.Response;
        import com.android.volley.VolleyError;
        import com.android.volley.toolbox.JsonObjectRequest;
        import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sandeep.smsspamfilter.R;
import com.sandeep.smsspamfilter.adapter.SMSAdapter;
import com.sandeep.smsspamfilter.model.Message;
import com.sandeep.smsspamfilter.util.SMSFetcher;

import org.json.JSONException;
        import org.json.JSONObject;
        import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_SMS = 1;
    private RecyclerView recyclerView;
    private SMSAdapter smsAdapter;
    private List<Message> smsList;

    private TextView hider;

    private ImageView dataset;


    private static final String FLASK_SERVER_URL = "http://192.168.0.180:5000/predict";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        hider = findViewById(R.id.hider);

//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                if (dy > 0) {
//                    // Scrolling down
//                    hider.setVisibility(View.GONE);
//                } else if (dy < 0) {
//                    // Scrolling up
//                    hider.setVisibility(View.VISIBLE);
//                }
//            }
//        });

        dataset = findViewById(R.id.dataset);

        smsAdapter = new SMSAdapter(smsList);
        recyclerView.setAdapter(smsAdapter);


        if (hasReadSmsPermission()) {
            fetchSMS();
        } else {
            requestReadSmsPermission();
        }

        smsAdapter.setOnItemClickListener(new SMSAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, ImageView imageView) {
                Message sms = smsList.get(position);
                loadGif(MainActivity.this,imageView);
                sendApiRequest(sms,position);
            }
        });

        dataset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this,DatasetActvitiy.class);
                startActivity(intent);
            }
        });
    }

    private boolean hasReadSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadSmsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS},
                PERMISSION_REQUEST_READ_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchSMS();
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchSMS() {
        smsList = SMSFetcher.fetchSMS(this);
        smsAdapter.setSmsList(smsList);
        smsAdapter.notifyDataSetChanged();
    }

    private void sendApiRequest(Message sms,int position) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("message", sms.getBody());

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FLASK_SERVER_URL, requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String decisionTreeResult = response.getString("decision_tree");
//                                String logisticRegressionResult = response.getString("logistic_regression");
                                String naiveBayesResult = response.getString("naive_bayes");
                                String svcResult = response.getString("svc");

                                String spamResult;
                                if ((decisionTreeResult.equals("spam") && (naiveBayesResult.equals("spam") || svcResult.equals("spam"))) ||
                                        (naiveBayesResult.equals("spam") && svcResult.equals("spam"))) {
                                    spamResult = "spam";
                                } else {
                                    spamResult = "ham";
                                }
                                Toast.makeText(MainActivity.this, "SMS is: " + spamResult, Toast.LENGTH_SHORT).show();
                                int result = (spamResult.equals("spam"))? 1:0;
                                smsList.set(position,new Message(sms.getBody(),sms.getAddress(),result));
                                smsAdapter.setSmsList(smsList);
                                smsAdapter.notifyDataSetChanged();
                                // You can update the SMS item view here based on the spamResult value
                            } catch (JSONException e) {
                                Log.e("net",e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, "Server error, Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    });

            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) {
            Log.e("net",e.toString());
        }
    }
    private void loadGif(Context context, ImageView imageView) {

        Glide.with(context)
                .asGif()
                .load(R.drawable.anim)
                .into(new ImageViewTarget<GifDrawable>(imageView) {
                    @Override
                    protected void setResource(GifDrawable resource) {
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void onResourceReady(GifDrawable resource, Transition<? super GifDrawable> transition) {
                        super.onResourceReady(resource, transition);
                        resource.start();
                    }
                });
    }


}


package com.sandeep.smsspamfilter.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sandeep.smsspamfilter.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DatasetActvitiy extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SERVER_URL = "http://192.168.0.180:5000/message-count";

    private FrameLayout chartContainer;
    private BarChart barChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dataset_actvitiy);

        chartContainer = findViewById(R.id.chartContainer);

        // Create the BarChart instance
        barChart = new BarChart(this);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);

        // Add the BarChart to the chart container
        chartContainer.addView(barChart);

        // Make the HTTP request to the Flask server
        makeRequest();
    }

    private void makeRequest() {
        // Instantiate the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, SERVER_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Retrieve the spam and ham counts from the response
                            int spamCount = response.getInt("spam_count");
                            int hamCount = response.getInt("ham_count");

                            // Populate the chart with the retrieved data
                            populateChart(spamCount, hamCount);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(DatasetActvitiy.this, "Error parsing JSON response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error: " + error.getMessage());
                        Toast.makeText(DatasetActvitiy.this, "Error retrieving data from server", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue
        queue.add(request);
    }

    private void populateChart(int spamCount, int hamCount) {
        // Create the chart data
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, spamCount));
        entries.add(new BarEntry(1f, hamCount));

        BarDataSet dataSet = new BarDataSet(entries, "Spam vs Ham");

        // Set colors for spam and ham bars
        int[] colors = new int[] { getResources().getColor(android.R.color.holo_red_light),
                getResources().getColor(android.R.color.holo_green_light) };
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);

        // Set the data to the BarChart
        barChart.setData(barData);
        barChart.invalidate(); // Refresh the chart to display the updated data
    }
}

package com.sandeep.smsspamfilter.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.sandeep.smsspamfilter.R;
import com.sandeep.smsspamfilter.model.Message;

import java.util.List;

public class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.SMSViewHolder> {

    private List<Message> smsList;
    private OnItemClickListener clickListener;

    public SMSAdapter(List<Message> smsList) {
        this.smsList = smsList;
    }

    public void setSmsList(List<Message> smsList) {
        this.smsList = smsList;
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SMSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms, parent, false);
        return new SMSViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SMSViewHolder holder, int position) {
        Message sms = smsList.get(holder.getAdapterPosition());
        holder.messageTextView.setText(sms.getBody());
        holder.addressTextView.setText(sms.getAddress());
        switch (sms.getResult())
        {
            case -1: holder.status.setImageResource(R.drawable.unknown);
            break;
            case 0: holder.status.setImageResource(R.drawable.accept);
            break;
            case 1: holder.status.setImageResource(R.drawable.multiply);
            break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClick(v, holder.getAdapterPosition(),holder.status);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return smsList.size();
    }

    static class SMSViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView addressTextView;

        ImageView status;

        SMSViewHolder(View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            status = itemView.findViewById(R.id.statusView);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position,ImageView imageView);
    }
}


package com.sandeep.smsspamfilter.model;

public class Message {
    private String body;
    private String address;

    private int result;

    public Message(String body, String address, int result) {
        this.body = body;
        this.address = address;
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}


