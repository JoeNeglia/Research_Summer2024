package com.project.newsapp;

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
        assertEquals("com.project.newsapp", appContext.getPackageName());
    }
}

package com.project.newsapp;

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

package com.project.newsapp.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.project.newsapp.model.News;
import com.project.newsapp.model.TotalNews;
import com.project.newsapp.restapi.ApiClient;
import com.project.newsapp.restapi.RestInterface;
import com.project.newsapp.utils.Util;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {

    private MutableLiveData<List<News>> newsLiveData;
    private List<News> newsList;
    private String countryCode;
    private String apiKey;

    public MainViewModel() {
        newsLiveData = new MutableLiveData<>();
        newsList = new ArrayList<>();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        getNews(countryCode, "");
    }

    public MutableLiveData<List<News>> getNewsLiveData() {
        return newsLiveData;
    }

    private RestInterface getRestInterface() {
        RestInterface[] restInterface = new RestInterface[1];
        restInterface[0] = ApiClient.getClient(Util.API_BASE_URL).create(RestInterface.class);
        return restInterface[0];
    }

    private void getNews(String langCode, String category) {
        RestInterface restInterface = getRestInterface();
        Call<TotalNews> call;
        newsList.clear();
        newsLiveData.setValue(null);
        if (!category.equals("")) {
            call = restInterface.getTotalNews(langCode, category, apiKey);
        } else {
            call = restInterface.getTotalNews(langCode, apiKey);
        }
        call.enqueue(new Callback<TotalNews>() {
            @Override
            public void onResponse(Call<TotalNews> call, Response<TotalNews> response) {
                if (response.body() != null) {
                    TotalNews totalNews = response.body();
                    fillNewsList(totalNews);
                }
            }

            @Override
            public void onFailure(Call<TotalNews> call, Throwable t) {
                newsLiveData.setValue(null);
            }
        });
    }

    private void getSearchedNews(String keyword) {
        RestInterface restInterface = getRestInterface();
        Call<TotalNews> call;
        newsList.clear();
        newsLiveData.setValue(null);
        call = restInterface.getSearchedTotalNews(keyword, apiKey);

        call.enqueue(new Callback<TotalNews>() {
            @Override
            public void onResponse(Call<TotalNews> call, Response<TotalNews> response) {
                if (response.body() != null) {
                    TotalNews totalNews = response.body();
                    fillNewsList(totalNews);
                }
            }

            @Override
            public void onFailure(Call<TotalNews> call, Throwable t) {
                newsLiveData.setValue(null);
            }
        });
    }

    private void fillNewsList(TotalNews totalNews) {
        newsList.addAll(totalNews.getNewsList());
        newsLiveData.setValue(newsList);
    }

    public void newsCategoryClick(Object category) {
        getNews(countryCode, String.valueOf(category));
    }

    public void searchNews(String keyword) {
        getSearchedNews(keyword);
    }
}


package com.project.newsapp.utils;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;

public class Util {

    public static final String APP_NAME = "NEWS_APP";
    public static final String API_BASE_URL = "http://newsapi.org/";

    public static final String COUNTRY_PREF = "countryPref";

    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = act.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(act.getResources().getColor(color));
        }
    }

    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

}


package com.project.newsapp.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}


package com.project.newsapp.utils.glidessl;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.InputStream;

import okhttp3.OkHttpClient;


@GlideModule
public class MyGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        super.applyOptions(context, builder);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient okHttpClient= UnsafeOkHttpClient.getUnsafeOkHttpClient();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
    }
}


package com.project.newsapp.utils.glidessl;


import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class UnsafeOkHttpClient {

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}


package com.project.newsapp.restapi;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String apiBaseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(apiBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient())
                    .build();
        }
        return retrofit;
    }

}


package com.project.newsapp.restapi;

import com.project.newsapp.model.TotalNews;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestInterface {

    @GET("v2/top-headlines")
    Call<TotalNews> getTotalNews(@Query("country") String country, @Query("apiKey") String apiKey);

    @GET("v2/top-headlines")
    Call<TotalNews> getTotalNews(@Query("country") String country, @Query("category") String category, @Query("apiKey") String apiKey);

    @GET("v2/everything")
    Call<TotalNews> getSearchedTotalNews(@Query("q") String country, @Query("apiKey") String apiKey);
}


package com.project.newsapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.project.newsapp.R;
import com.project.newsapp.clicklisteners.AdapterItemClickListener;
import com.project.newsapp.databinding.NewsBinding;
import com.project.newsapp.model.News;

import java.util.List;

public class AdapterListNews extends RecyclerView.Adapter<AdapterListNews.NewsViewHolder> {

    private List<News> items;
    private AdapterItemClickListener adapterItemClickListener;

    public AdapterListNews(List<News> items, AdapterItemClickListener adapterItemClickListener) {
        this.items = items;
        this.adapterItemClickListener = adapterItemClickListener;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        NewsBinding newsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_news_dashboard, parent, false);
        return new NewsViewHolder(newsBinding);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(NewsViewHolder holder, final int position) {
        holder.bind(getItem(position), adapterItemClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private News getItem(int position) {
        return items.get(position);
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        private NewsBinding newsBinding;

        public NewsViewHolder(NewsBinding newsBinding) {
            super(newsBinding.getRoot());
            this.newsBinding = newsBinding;
        }

        public void bind(News news, AdapterItemClickListener adapterItemClickListener) {
            this.newsBinding.setNews(news);
            this.newsBinding.setClickListener(adapterItemClickListener);
        }

    }

}

package com.project.newsapp.clicklisteners;

import com.project.newsapp.model.News;

public interface AdapterItemClickListener {

    void onNewsItemClick(News news);

}


package com.project.newsapp.clicklisteners;

public interface NewsDialogClickListeners {

    void onGotoWebSiteClick(String url);
    void onDismissClick();

}


package com.project.newsapp.model;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class News {

    @NonNull
    @SerializedName("source")
    private Source source;

    @NonNull
    @SerializedName("title")
    private String newsTitle;

    @NonNull
    @SerializedName("description")
    private String newsDescription;

    @NonNull
    @SerializedName("url")
    private String newsUrl;

    @NonNull
    @SerializedName("urlToImage")
    private String newsImage;

    @NonNull
    @SerializedName("publishedAt")
    private Date newsPublishedDate;

    @NonNull
    public Source getSource() {
        return source;
    }

    public void setSource(@NonNull Source source) {
        this.source = source;
    }

    @NonNull
    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(@NonNull String newsTitle) {
        this.newsTitle = newsTitle;
    }

    @NonNull
    public String getNewsDescription() {
        return newsDescription;
    }

    public void setNewsDescription(@NonNull String newsDescription) {
        this.newsDescription = newsDescription;
    }

    @NonNull
    public String getNewsUrl() {
        return newsUrl;
    }

    public void setNewsUrl(@NonNull String newsUrl) {
        this.newsUrl = newsUrl;
    }

    @NonNull
    public String getNewsImage() {
        return newsImage;
    }

    public void setNewsImage(@NonNull String newsImage) {
        this.newsImage = newsImage;
    }

    @NonNull
    public String getNewsPublishedDate() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(newsPublishedDate);
    }

    public void setNewsPublishedDate(@NonNull Date newsPublishedDate) {
        this.newsPublishedDate = newsPublishedDate;
    }

    //Added for Child JSON Object
    public class Source {
        @SerializedName("name")
        private String sourceName;

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }
    }

    //Image Binding - I didn't write newsviewmodel for just this method
    @BindingAdapter({"bind:imgUrl"})
    public static void setImage(ImageView imageView, String imgUrl) {
        Glide.with(imageView.getContext()).load(imgUrl).into(imageView);
    }
}


package com.project.newsapp.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TotalNews {

    @NonNull
    private String status;

    @NonNull
    @SerializedName("totalResults")
    private int totalNewsCount;

    @NonNull
    @SerializedName("articles")
    private List<News> newsList;

    public TotalNews() {
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public int getTotalNewsCount() {
        return totalNewsCount;
    }

    public void setTotalNewsCount(int totalNewsCount) {
        this.totalNewsCount = totalNewsCount;
    }

    @NonNull
    public List<News> getNewsList() {
        return newsList;
    }

    public void setNewsList(@NonNull List<News> newsList) {
        this.newsList = newsList;
    }
}


package com.project.newsapp.activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.newsapp.R;
import com.project.newsapp.adapters.AdapterListNews;
import com.project.newsapp.clicklisteners.AdapterItemClickListener;
import com.project.newsapp.clicklisteners.NewsDialogClickListeners;
import com.project.newsapp.databinding.NewsDialogBinding;
import com.project.newsapp.model.News;
import com.project.newsapp.utils.LocaleHelper;
import com.project.newsapp.utils.Util;
import com.project.newsapp.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LifecycleOwner, AdapterItemClickListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.ivToolbarCountry)
    ImageView ivToolbarCountry;


    MainActivity context;
    MainViewModel viewModel;
    AdapterListNews adapterListNews;
    List<News> newsList;

    private String firstControl = "firstControl";
    private String countryPositionPref = "countryPositionPref";
    SharedPreferences pref;
    private String[] countrys;
    private TypedArray countrysIcons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getApplicationContext().getSharedPreferences(Util.APP_NAME, MODE_PRIVATE);
        languageControl();
        setContentView(R.layout.activity_main);
        context = this;
        ButterKnife.bind(this);
        countrys = getResources().getStringArray(R.array.countrys);
        countrysIcons = getResources().obtainTypedArray(R.array.countrysIcons);

        initToolbar();

        newsList = new ArrayList<>();
        adapterListNews = new AdapterListNews(newsList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapterListNews);


        if (pref.contains(countryPositionPref))
            ivToolbarCountry.setImageResource(countrysIcons.getResourceId(pref.getInt(countryPositionPref, 0), 0));

        viewModel = ViewModelProviders.of(context).get(MainViewModel.class);
        viewModel.getNewsLiveData().observe(context, newsListUpdateObserver);
        viewModel.setApiKey(getString(R.string.news_api_key));
        viewModel.setCountryCode(pref.getString(Util.COUNTRY_PREF, "tr"));


    }

    private void languageControl() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N && !pref.getBoolean(firstControl, false)) {
            Locale primaryLocale = getResources().getConfiguration().getLocales().get(0);
            LocaleHelper.setLocale(MainActivity.this, primaryLocale.getLanguage());
            int position = getLanguagePosition(primaryLocale.getLanguage());
            pref.edit().putInt(countryPositionPref, position).apply();
            pref.edit().putBoolean(firstControl, true).apply();
            recreate();
        }
    }

    private int getLanguagePosition(String displayLanguage) {
        String[] codes = getResources().getStringArray(R.array.countrysCodes);
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(displayLanguage)) return i;
        }
        return 0;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle(null);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Util.setSystemBarColor(this, android.R.color.white);
        Util.setSystemBarLight(this);
    }

    private void showLanguageDialog() {
        new AlertDialog.Builder(this).setCancelable(false)
                .setTitle("Choose Country")
                .setSingleChoiceItems(countrys, pref.getInt(countryPositionPref, 0), null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    pref.edit().putInt(countryPositionPref, selectedPosition).apply();
                    pref.edit().putString(Util.COUNTRY_PREF, getResources().getStringArray(R.array.countrysCodes)[selectedPosition]).apply();
                    LocaleHelper.setLocale(MainActivity.this, getResources().getStringArray(R.array.countrysCodes)[selectedPosition]);
                    recreate();
                    dialog.dismiss();
                })
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        Util.changeMenuIconColor(menu, Color.BLACK);
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
        }
        searchView.setQueryHint(getString(R.string.search_in_everything));
        if (searchView != null)
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (viewModel != null) viewModel.searchNews(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        return true;
    }

    public void categoryClicked(View view) {
        viewModel.newsCategoryClick(String.valueOf(view.getTag()));
    }

    public void countryClick(View view) {
        showLanguageDialog();
    }

    Observer<List<News>> newsListUpdateObserver = new Observer<List<News>>() {
        @Override
        public void onChanged(List<News> news) {
            newsList.clear();
            if (news != null) {
                newsList.addAll(news);
            }
            adapterListNews.notifyDataSetChanged();
        }
    };


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    public void onNewsItemClick(News news) {
        showDialogPolygon(news);
    }

    private void showDialogPolygon(News news) {
        final Dialog dialog = new Dialog(this);
        NewsDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getApplicationContext()), R.layout.dialog_header_polygon, null, false);
        binding.setNews(news);
        binding.setListener(new NewsDialogClickListeners() {
            @Override
            public void onGotoWebSiteClick(String url) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }

            @Override
            public void onDismissClick() {
                dialog.dismiss();
            }
        });

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(binding.getRoot());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        dialog.show();
    }

}


