package com.praise.recipe;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

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
        assertEquals("com.praise.recipe", appContext.getPackageName());
    }
}

package com.praise.recipe;

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

package com.praise.recipe;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private  static  final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private  static  ApiClient apiClient;
    private static Retrofit retrofit;

    private  ApiClient(){
        retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }
    public static  synchronized  ApiClient getInstance(){
        if (apiClient== null){
            apiClient = new ApiClient();
        }
        return apiClient;
    }
    public  ApiInterface getApi(){
        return  retrofit.create(ApiInterface.class);
    }
}


package com.praise.recipe;

import com.praise.recipe.Model.All;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("complexSearch")
    Call<All> getAll(
            @Query("apiKey") String apiKey,
            @Query("query") String query

    );
    @GET("information")
    Call<All> getInfo(
            @Query("apiKey") String apiKey,
            @Query("id") String id
    );
}


package com.praise.recipe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Details extends AppCompatActivity {
    TextView tvTitle, tvSource, tvDate, tvDesc;
    WebView webView;
    ImageView i;
    ProgressBar progressBar;
    final String apiKey = "YOUR API KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        tvTitle = findViewById(R.id.tvTitle);
        tvDesc = findViewById(R.id.tvDesc);
        tvSource = findViewById(R.id.tvSource);
        tvDate = findViewById(R.id.tvDate);
        webView = findViewById(R.id.webview);
        i = findViewById(R.id.imageV);
        progressBar = findViewById(R.id.webloader);
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        tvTitle.setText(title);
        String imageUrl = intent.getStringExtra("imageUrl");
        Picasso.get().load(imageUrl).into(i);
        String id = intent.getStringExtra("id");
        tvDesc.setText(id);
        fetchData(id,apiKey);
    }
    private void fetchData(String id , String apikey) {
        String url = " https://api.spoonacular.com/recipes/"+id+"/information?apiKey="+apikey;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(Details.this, "error", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resp = response.body().string();
                    Details.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject(resp);
                                String sourceName = jsonObject.getString("sourceName");
                                    String sourceUrl = jsonObject.getString("sourceUrl");
                                    String likes = jsonObject.getString("aggregateLikes");
                                    String ready = jsonObject.getString("readyInMinutes");
                                    tvDesc.setText("ready in minutes : "+ready);
                                    tvSource.setText("Source name : "+sourceName);
                                    tvDate.setText("likes : " +likes);
                                webView.getSettings().setDomStorageEnabled(true);
                                webView.getSettings().setJavaScriptEnabled(true);
                                webView.getSettings().setLoadsImagesAutomatically(true);
                                webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                                webView.setWebViewClient(new WebViewClient());
                                webView.loadUrl(sourceUrl);
                                if (webView.isShown()){
                                    progressBar.setVisibility(View.INVISIBLE);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(Details.this, "error "+ e, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });
    }
}

package com.praise.recipe;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.praise.recipe.Model.All;
import com.praise.recipe.Model.Results;

import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    final String apiKey = "Your API Key";
    RecyclerView recyclerView;
    com.praise.recipe.adapter adapter;
    List<Results> foods = new ArrayList<>();
    EditText editText;
    Button button;
    SwipeRefreshLayout swipeRefreshLayout;
    LottieAnimationView lottieAnimationView;

    //example link on fecthing requests...
    String searchurl = "https://api.spoonacular.com/recipes/complexSearch?apiKey=44479338b9954d4ca340b984102073ae&query=pasta";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.edtQuery);
        button = findViewById(R.id.search);
        swipeRefreshLayout = findViewById(R.id.swipe);
        recyclerView = findViewById(R.id.recyclerView);
        lottieAnimationView = findViewById(R.id.lottie);
        lottieAnimationView.setVisibility(View.INVISIBLE);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        String query = "";
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                retrieveJson(apiKey, "");
            }
        });
        retrieveJson(apiKey, query);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            retrieveJson(apiKey, editText.getText().toString());
                        }
                    });
                    retrieveJson(apiKey, editText.getText().toString());
                } else {
                    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            retrieveJson(apiKey, "");
                        }
                    });
                    retrieveJson(apiKey, "");
                }
            }
        });

    }

    public void retrieveJson(String apikey, String query) {
        swipeRefreshLayout.setRefreshing(true);
        Call<All> call;
        call = ApiClient.getInstance().getApi().getAll(apiKey, query);
        call.enqueue(new Callback<All>() {
            @Override
            public void onResponse(Call<All> call, Response<All> response) {
                if (response.isSuccessful() && response.body().getResultsList() != null) {
                    lottieAnimationView.setVisibility(View.INVISIBLE);
                    foods.clear();
                    swipeRefreshLayout.setRefreshing(false);
                    foods = response.body().getResultsList();
                    adapter = new adapter(MainActivity.this, foods);
                    recyclerView.setAdapter(adapter);

                }
                if (response.isSuccessful() && response.body().getResultsList().isEmpty()) {
                    Toast.makeText(MainActivity.this, "no resource in our database", Toast.LENGTH_SHORT).show();
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                if(!response.isSuccessful()){
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                    Toast.makeText(MainActivity.this, "check your connection!!!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<All> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

}

package com.praise.recipe;

public class Food {
    String title;
    String imageUrl;
    int id;
    int likes;
    String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Food(String title, String imageUrl, int id) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.id = id;
    }

    public Food(String title, String imageUrl, int id, int likes, String source) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.id = id;
        this.likes = likes;
        this.source = source;
    }

    public Food(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}


package com.praise.recipe;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.praise.recipe.Model.Results;
import com.squareup.picasso.Picasso;

import java.util.List;

public class adapter extends RecyclerView.Adapter<adapter.ViewHolder> {
    final String apiKey = "44479338b9954d4ca340b984102073ae";
    Context context;
    List<Results> results;

    public adapter(Context context, List<Results> results) {
        this.context = context;
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull adapter.ViewHolder holder, int position) {
        final Results a = results.get(position);
        holder.tvTitle.setText(a.getTitle());
        String id = a.getId();
        String imageUrl = a.getUrl();
        Picasso.get().load(imageUrl).into(holder.imageView);

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Details.class);
                intent.putExtra("title", a.getTitle());
                intent.putExtra("imageUrl", a.getUrl());
                intent.putExtra("id",a.getId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSource, tvDate;
        ImageView imageView;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            imageView = itemView.findViewById(R.id.image);
            cardView = itemView.findViewById(R.id.cardView);

        }
    }


}



package com.praise.recipe.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class All {
    @SerializedName("results")
    @Expose
    private List<Results> resultsList;

    public List<Results> getResultsList() {
        return resultsList;
    }

    public void setResultsList(List<Results> resultsList) {
        this.resultsList = resultsList;
    }
}


package com.praise.recipe.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Info {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("sourceName")
    @Expose
    private String sourceName;
    @SerializedName("sourceUrl")
    @Expose
    private String sourceUrl;
    @SerializedName("aggregateLikes")
    @Expose
    private String likes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }
}


package com.praise.recipe.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Results {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("image")
    @Expose
    private String url;
    @SerializedName("title")
    @Expose
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}


