package com.example.movieapp;

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
        assertEquals("com.example.myapplication", appContext.getPackageName());
    }
}

package com.example.movieapp;

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

package com.example.movieapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ViewHolder>{

    //Initialise the list item here
    private ArrayList<ShowModel> arrayListAllShow;
    //Creating context for toast
    private Context context;

    public ShowAdapter(ArrayList<ShowModel> arrayListAllShow, Context context) {
        this.arrayListAllShow = arrayListAllShow;
        this.context = context;
    }

    //View holder(it calls the created recycler View)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_show,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    //All the click listener is done here
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.textViewShowName.setText(arrayListAllShow.get(position).getShowName());
        Picasso.with(context).load(arrayListAllShow.get(position).getShowImageUrl()).into(holder.imageViewShow);

        //On click listener
        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowDetailActivity.class);
                intent.putExtra("show_id",arrayListAllShow.get(position).getShowId());
                intent.putExtra("show_language",arrayListAllShow.get(position).getShowLanguage());
                intent.putExtra("show_premiered",arrayListAllShow.get(position).getShowPremiered());
                intent.putExtra("show_summary",arrayListAllShow.get(position).getShowSummary());
                intent.putExtra("show_img_url",arrayListAllShow.get(position).getShowImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayListAllShow.size();
    }

    //Every view inside the recycler view is declared and initialised here
    public class ViewHolder extends RecyclerView.ViewHolder{
        //Declaration
        private TextView textViewShowName;
        private ImageView imageViewShow;
        private LinearLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewShowName = itemView.findViewById(R.id.tv_show_name);
            imageViewShow =  itemView.findViewById(R.id.ic_show_image);
            //The container
            parent = itemView.findViewById(R.id.single_show);
        }
    }
}


package com.example.movieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.ViewHolder>{
    //Initialise the list item here
    private ArrayList<CastModel> arrayListAllCast;
    //Creating context for toast
    private Context context;

    public CastAdapter(ArrayList<CastModel> arrayListAllCast, Context context) {
        this.arrayListAllCast = arrayListAllCast;
        this.context = context;
    }

    //View holder(it calls the created recycler View)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_cast,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    //All the click listener is done here
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.textViewCastName.setText(arrayListAllCast.get(position).getCastName());
        Picasso.with(context).load(arrayListAllCast.get(position).getCastImageUrl()).into(holder.imageViewCast);

    }

    @Override
    public int getItemCount() {
        return arrayListAllCast.size();
    }

    //Every view inside the recycler view is declared and initialised here
    public class ViewHolder extends RecyclerView.ViewHolder{
        //Declaration
        private TextView textViewCastName;
        private ImageView imageViewCast;
        private LinearLayout parent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCastName = itemView.findViewById(R.id.tv_cast_name);
            imageViewCast =  itemView.findViewById(R.id.ic_cast_image);
            //The container
            parent = itemView.findViewById(R.id.single_cast);
        }
    }
}


package com.example.movieapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //Declaration
    private RecyclerView recyclerViewShows;
    private ArrayList<ShowModel> arrayListAllShows;
    private ShowAdapter showAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewShows = findViewById(R.id.recyclerview_shows);
        arrayListAllShows = new ArrayList<>();

        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,"https://api.tvmaze.com/shows", null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++){

                                JSONObject info = response.getJSONObject(i);

                                arrayListAllShows.add(new ShowModel(
                                        info.getString("name"),
                                        info.getString("language"),
                                        info.getString("premiered"),
                                        info.getString("summary"),
                                        info.getJSONObject("image").getString("medium"),
                                        info.getString("id")
                                ));
                            }
                            if(response.length()>0){
                                showAdapter = new ShowAdapter(arrayListAllShows, MainActivity.this);
                                recyclerViewShows.setHasFixedSize(true);
                                recyclerViewShows.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
                                recyclerViewShows.setAdapter(showAdapter);
                                showAdapter.notifyDataSetChanged();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "NO DATA FOUND", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }
}

package com.example.movieapp;

public class CastModel {
    private String castName;
    private String castImageUrl;

    public CastModel(String castName, String castImageUrl) {
        this.castName = castName;
        this.castImageUrl = castImageUrl;
    }

    public String getCastName() {
        return castName;
    }

    public String getCastImageUrl() {
        return castImageUrl;
    }
}


package com.example.movieapp;

import java.io.Serializable;

public class ShowModel implements Serializable {

    private String showName;
    private String showLanguage;
    private String showPremiered;
    private String showSummary;
    private String showImageUrl;
    private String showId;


    public ShowModel(String showName, String showLanguage, String showPremiered, String showSummary, String showImageUrl, String showId) {
        this.showName = showName;
        this.showLanguage = showLanguage;
        this.showPremiered = showPremiered;
        this.showSummary = showSummary;
        this.showImageUrl = showImageUrl;
        this.showId = showId;
    }

    public String getShowName() {
        return showName;
    }

    public String getShowLanguage() {
        return showLanguage;
    }

    public String getShowPremiered() {
        return showPremiered;
    }

    public String getShowSummary() {
        return showSummary;
    }

    public String getShowImageUrl() {
        return showImageUrl;
    }

    public String getShowId() {
        return showId;
    }
}


package com.example.movieapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowDetailActivity extends AppCompatActivity {

    private TextView textViewShowName, textViewShowLanguage, textViewShowPremiered,textViewShowSummary;
    private ImageView imageViewShow;

    private RecyclerView recyclerViewCast;
    private ArrayList<CastModel> arrayListAllCast;
    private CastAdapter castAdapter;

    ShowModel showModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_detail);

        textViewShowName = findViewById(R.id.tv_show_name);
        textViewShowLanguage = findViewById(R.id.tv_language);
        textViewShowPremiered = findViewById(R.id.tv_premiered);
        textViewShowSummary = findViewById(R.id.tv_summary);
        imageViewShow = findViewById(R.id.ic_show_image);

        showModel = (ShowModel) getIntent().getSerializableExtra("showModel");

        Intent intent=getIntent();
        String showId = intent.getStringExtra("show_id");
        String showLanguage = intent.getStringExtra("show_language");
        String showPremiered = intent.getStringExtra("show_premiered");
        String showSummary = intent.getStringExtra("show_summary");
        String showImgUrl = intent.getStringExtra("show_img_url");

//        textViewShowName.setText(showName);
        textViewShowLanguage.setText(showLanguage);
        textViewShowPremiered.setText(showPremiered);
        textViewShowSummary.setText(showSummary);

        Picasso.with(this).load(showImgUrl).into(imageViewShow);

        recyclerViewCast = findViewById(R.id.recyclerview_cast);
        arrayListAllCast = new ArrayList<>();

        RequestQueue mQueue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,"https://api.tvmaze.com/shows/"+showId+"/cast", null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("--->",response+"");
                        try {
                            for (int i = 0; i < response.length(); i++){

                                JSONObject info = response.getJSONObject(i);

                                arrayListAllCast.add(new CastModel(
                                        info.getJSONObject("person").getString("name"),
                                        info.getJSONObject("character").getJSONObject("image").getString("medium")
                                ));
                            }
                            if(response.length()>0){
                                castAdapter = new CastAdapter(arrayListAllCast, ShowDetailActivity.this);
                                recyclerViewCast.setHasFixedSize(true);
                                recyclerViewCast.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                                recyclerViewCast.setAdapter(castAdapter);
                                castAdapter.notifyDataSetChanged();
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "NO DATA FOUND", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);


    }
}

