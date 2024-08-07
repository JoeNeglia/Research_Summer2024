package com.farizma.imagebox;

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
        assertEquals("com.farizma.imagebox", appContext.getPackageName());
    }
}

package com.farizma.imagebox;

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

package com.farizma.imagebox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FavoriteActivity extends AppCompatActivity {

    protected static final String SHARED_PREF = "mysharedpref";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<Item> itemList = new ArrayList<>();

    private Toolbar toolbar;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme2);
        setContentView(R.layout.activity_favorite);

        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_fav);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        // Enable the Up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = findViewById(R.id.imageView);
        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fav, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear: if(imageView.getVisibility() == View.INVISIBLE) showDialog(); break;
            case R.id.home: onBackPressed(); break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    private void start() {
        itemList.clear();
        showFavorites();
    }

    private void showDialog() {
        // show dialog, if yes -> clearFavList then showFav ; else do nothings
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dailog);

        dialog.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                clearFavList();
                Toast.makeText(getApplicationContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                start();
            }
        });

        dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void clearFavList() {
        // remove data from sharedPreference
        if(sharedPreferences.contains("FavouriteList")) {
            editor.putString("FavouriteList", (new JSONArray()).toString());
            editor.apply();
        }
    }

    private void showFavorites() {
        if(sharedPreferences.contains("FavouriteList")) {
            try {
                JSONArray favouriteList = new JSONArray(sharedPreferences.getString("FavouriteList", null));
                if(!favouriteList.isNull(0)) imageView.setVisibility(View.INVISIBLE);
                else imageView.setVisibility(View.VISIBLE);
                getList();
                recyclerViewConfig();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getList() {
        try {
            JSONArray favouriteList = new JSONArray(sharedPreferences.getString("FavouriteList", null));
            for(int i=0; i<favouriteList.length(); i++) {
                JSONObject item = new JSONObject(favouriteList.getJSONObject(i).toString());
                insertData(item.getString("id"), item.getString("username"), item.getString("name"),
                        item.getString("url"), item.getString("download_location"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertData(String id, String username, String name, String raw, String download_location) {
        itemList.add(new Item(id, username, name, raw, download_location));
    }

    private void recyclerViewConfig() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.columnSpan)));
        adapter = new Adapter(itemList);
        recyclerView.setAdapter(adapter);
    }
}

package com.farizma.imagebox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "https://api.unsplash.com/";
    public static final String ACCESS_KEY = "Paste Unsplash APi's ACCESS KEY here";

    private ProgressBar progressBar;
    private Button loadButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private ArrayList<Item> itemList = new ArrayList<>();
    private int count = 1;

    private String mQuery;
    private boolean isSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme1);
        setContentView(R.layout.activity_main);

        loadButton = findViewById(R.id.loadMore);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        recyclerViewConfig();
        fetchData();

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ++count;
                fetchData();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("showButton"));
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get data included in the Intent
            boolean isLast = intent.getBooleanExtra("value", false);
            if (isLast) loadButton.setVisibility(View.VISIBLE);
            else loadButton.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();

        // Associate searchable configuration with the SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                reset(true, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mQuery = newText;
                return false;
            }
        });

        MenuItemCompat.setOnActionExpandListener(search, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                reset(false, null);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fav:
                startActivity(new Intent(this, FavoriteActivity.class));
                break;
        }
        return true;
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);
        String url = isSearch ? getSearchUrl(mQuery) : getRandomUrl();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.INVISIBLE);
                        try {
                            JSONArray jsonArray;
                            if(isSearch) {
                                // response is in JSONObject format
                            JSONObject jsonObject1 = new JSONObject(response);
                            jsonArray = jsonObject1.getJSONArray("results");
                            }
                            // response is in JSONArray format
                            else jsonArray = new JSONArray(response);

                            for(int i=0; i<jsonArray.length(); i++) {
                                // fetch one item at a time
                                JSONObject jsonObject = new JSONObject(jsonArray.getJSONObject(i).toString());
                                // get ID
                                String id = jsonObject.getString("id");
                                // get image link
                                JSONObject urls = new JSONObject(jsonObject.getJSONObject("urls").toString());
                                String raw = urls.getString("raw");
                                // get user's username & name
                                JSONObject user = new JSONObject(jsonObject.getJSONObject("user").toString());
                                // get username
                                String username = user.getString("username");
                                // get name
                                String name = user.getString("name");
                                // get download link
                                JSONObject links = new JSONObject(jsonObject.getJSONObject("links").toString());
                                // set download location
                                String download_loc = links.getString("download_location")+"&client_id="+ACCESS_KEY;
                                // insert all information into ArrayList
                                insertData(id, username, name, raw, download_loc);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("Volley", "JSONException: " + e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d("Volley", "ERROR: " + error);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String getRandomUrl() {
        // url for getting the images as jSONArray
        String url = URL + "photos?page=" + count + "&per_page=30&client_id=" + ACCESS_KEY;
        return url;
    }

    private String getSearchUrl(String query) {
        // url for getting the images for query as jSONObject
        String url = URL + "search/photos?page=" + count + "&per_page=30&query=" + query + "&client_id=" + ACCESS_KEY;
        return url;
    }

    private void insertData(String id, String username, String name, String raw, String download_location) {
        itemList.add(new Item(id, username, name, raw, download_location));
        adapter.notifyDataSetChanged();
    }

    private void recyclerViewConfig() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.columnSpan)));
        adapter = new Adapter(itemList);
        recyclerView.setAdapter(adapter);
    }

    private void reset(boolean isSearch, String query) {
        count = 1;
        this.isSearch = isSearch;
        mQuery = query;
        itemList.clear();
        adapter.notifyDataSetChanged();
        fetchData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // release itemList
        itemList.clear();
        itemList = null;
        recyclerView.getRecycledViewPool().clear();
    }
}

package com.farizma.imagebox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.farizma.imagebox.FavoriteActivity.SHARED_PREF;

public class SingleImageActivity extends AppCompatActivity {

    private static final String PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private ImageView imageView;
    private TextView textView;
    private ImageButton downloadButton, heartButton, shareButton;

    private String id, username, name, url, downloadLocation;
    private Bitmap bitmap;
    private boolean isHearted, isLoaded;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private JSONArray favouriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme2);
        setContentView(R.layout.activity_single_image);

        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        statusBarConfig(findViewById(R.id.rootView));

        ActivityCompat.requestPermissions(SingleImageActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        downloadButton = findViewById(R.id.downloadButton);
        heartButton = findViewById(R.id.heartButton);
        shareButton = findViewById(R.id.shareButton);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!permissionGranted()) showToast(getString(R.string.permission_download));
                else getDownloadLink();
            }
        });

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFav()) removeFromFav();
                else addToFav();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!permissionGranted()) showToast(getString(R.string.permission_share));
                else {
                    if(isLoaded) shareImage();
                    else showToast(getString(R.string.loading_image));
                }
            }
        });

        Intent intent = getIntent();
        id = intent.getStringExtra("ID");
        username = intent.getStringExtra("USERNAME");
        name = intent.getStringExtra("NAME");
        url = intent.getStringExtra("URL");
        downloadLocation = intent.getStringExtra("DOWNLOAD_LOCATION");

        setText();

        isHearted = isFav();
        if(isHearted) heartButton.setImageResource(R.drawable.ic_heart_fill);
        else heartButton.setImageResource(R.drawable.ic_heart_empty);
        displayImage();
    }

    private static class ClickableString extends ClickableSpan {
        private View.OnClickListener mListener;

        public ClickableString(View.OnClickListener onClickListener) {
            mListener = onClickListener;
        }

        @Override
        public void onClick(@NonNull View view) {
            mListener.onClick(view);
        }
    }

    private void makeLinksFocusable(TextView tv) {
        MovementMethod m = tv.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (tv.getLinksClickable()) {
                tv.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private SpannableString makeLineSpan(String text, View.OnClickListener onClickListener) {
        SpannableString link = new SpannableString(text);
        link.setSpan(new ClickableString(onClickListener), 0, text.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
        return link;
    }

    private void setText() {
        textView.setText(getString(R.string.photo_by));
        textView.setTypeface(Typeface.SERIF, Typeface.ITALIC);
        textView.setTextSize(10);

        SpannableString link_username = makeLineSpan(name, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getString(R.string.link_user_start) + username +getString(R.string.link_user_end);
                openUrl(url);
            }
        });
        SpannableString link_unsplash = makeLineSpan(getString(R.string.unsplash), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getString(R.string.link_unsplash);
                openUrl(url);
            }
        });

        textView.append(link_username);
        textView.append(" on ");
        textView.append(link_unsplash);
        makeLinksFocusable(textView);
    }

    private void openUrl(String url) {
        Intent linkIntent = new Intent(Intent.ACTION_VIEW);
        linkIntent.setData(Uri.parse(url));
        startActivity(linkIntent);
    }

    private void displayImage() {
        Glide.with(this)
                .asBitmap()
                .load(url)
                .override(1000, 1000)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView.setImageBitmap(resource);
                        bitmap = resource;
                        isLoaded = true;
                    }
                });
    }

    private void shareImage() {
        // share image without saving it to gallery using cache directory
        Uri contentUri = getImageUri(bitmap);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(contentUri, getApplicationContext().getContentResolver().getType(contentUri));
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
    }

    private Uri getImageUri(Bitmap bmp) {
        File cachePath = new File(getApplicationContext().getCacheDir(), "images");
        if(!cachePath.exists())
            cachePath.mkdirs();
        try {
            FileOutputStream out = new FileOutputStream(cachePath + "/image.jpg");
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  catch (IOException e) {
            e.printStackTrace();
        }
        File imagePath = new File(getApplicationContext().getCacheDir(), "images");
        File newFile = new File(imagePath, "image.jpg");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", newFile);
        return uri;
    }

    private void downloadImage(String link) {
        String title = getTitleName();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
        request.allowScanningByMediaScanner();
        request.setTitle(title);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir("/ImageBox", title);

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
        showToast(getString(R.string.downloading));

    }

    private void getDownloadLink() {
        StringRequest downloadRequest = new StringRequest(Request.Method.GET, downloadLocation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject downloadObject = new JSONObject(response);
                            downloadImage(downloadObject.getString("url"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d("Volley", "ERROR: " + error);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(downloadRequest);
    }

    private String getTitleName() {
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss").format(new Date());
        return "ImageBox_" + timeStamp + ".jpeg";
    }

    private void showToast(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    private void addToFav() {
        // add image to favourites
        heartButton.setImageResource(R.drawable.ic_heart_fill);
        isHearted = true;
        JSONObject item = new JSONObject();
        try {
            item.put("id", id);
            item.put("username", username);
            item.put("name", name);
            item.put("url", url);
            item.put("download_location", downloadLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(!sharedPreferences.contains("FavouriteList")) {
            favouriteList = new JSONArray();
        } else {
            try {
                favouriteList = new JSONArray(sharedPreferences.getString("FavouriteList", null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        favouriteList.put(item);
        editor.putString("FavouriteList", favouriteList.toString());
        editor.apply();
        showToast(getString(R.string.added));
    }

    private void removeFromFav() {
        // ic_clear image from favourites
        heartButton.setImageResource(R.drawable.ic_heart_empty);
        isHearted = false;
        for (int i=0; i<favouriteList.length(); i++) {
            try {
                JSONObject currentItem = new JSONObject(favouriteList.getJSONObject(i).toString());
                if(currentItem.getString("id").compareTo(id) == 0) {
                    favouriteList.remove(i);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.putString("FavouriteList", favouriteList.toString());
        editor.apply();
        showToast(getString(R.string.removed));
    }

    private boolean isFav() {
        if(sharedPreferences.contains("FavouriteList")) {
            try {
                favouriteList = new JSONArray(sharedPreferences.getString("FavouriteList", null));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for (int i=0; i<favouriteList.length(); i++) {
                try {
                    JSONObject currentItem = new JSONObject(favouriteList.getJSONObject(i).toString());
                    if(currentItem.getString("id").compareTo(id) == 0)
                        return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void statusBarConfig(View view) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            this.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    private boolean permissionGranted() {
        return (ContextCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED);
    }
}

package com.farizma.imagebox;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private ArrayList<Item> arrayList;
    private Context context;

    public Adapter(ArrayList<Item> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Item currentItem = arrayList.get(position);
        Glide.with(context)
                .load(currentItem.getUrl())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .thumbnail(0.05f)
                .dontAnimate()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SingleImageActivity.class);
                intent.putExtra("ID", currentItem.getId());
                intent.putExtra("USERNAME", currentItem.getUsername());
                intent.putExtra("NAME", currentItem.getName());
                intent.putExtra("URL", currentItem.getUrl());
                intent.putExtra("DOWNLOAD_LOCATION", currentItem.getDownloadLocation());
                context.startActivity(intent);
            }
        });

        Intent intent = new Intent("showButton");
        if(position == (arrayList.size()-1)) intent.putExtra("value", true);
        else intent.putExtra("value", false);
         LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}


package com.farizma.imagebox;

public class Item {
    private String mId;
    private String mUsername;
    private String mName;
    private String mUrl;
    private String mDownloadLocation;

    public Item(String id, String username, String name, String url, String downloadLoc) {
        mId = id;
        mUsername = username;
        mName = name;
        mUrl = url;
        mDownloadLocation = downloadLoc;
    }

    public String getId() {
        return mId;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getName() {
        return mName;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDownloadLocation() { return mDownloadLocation; }
}


