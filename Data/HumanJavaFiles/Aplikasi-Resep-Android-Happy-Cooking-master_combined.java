package com.example.bayup.happycooking;

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
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.example.bayup.happycooking", appContext.getPackageName());
    }
}


package com.example.bayup.happycooking;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}

package com.example.bayup.happycooking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by bayup on 10-May-19.
 */


public class RequestHandler {
    //Metode Untuk mengirim httpPostRequest
    //Metode ini mengambil 2 Argumen
    //Metode Pertama adalah URL dari Skrip yang digunakan untuk mengirimkan permintaan
    //Yang lainnya adalah HashMap dengan nilai pasangan nama yang berisi data yang akan dikirim dengan permintaan
    public String sendPostRequest(String requestURL,
                                  HashMap<String, String> postDataParams) {
        //Membuat URL
        URL url;

        //Objek StringBuilder untuk menyimpan pesan diambil dari server
        StringBuilder sb = new StringBuilder();
        try {
            //Inisialisasi URL
            url = new URL(requestURL);

            //Membuat Koneksi HttpURLConnection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //Konfigurasi koneksi
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //Membuat Keluaran Stream
            OutputStream os = conn.getOutputStream();

            //Menulis Parameter Untuk Permintaan
            //Kita menggunakan metode getPostDataString yang didefinisikan di bawah ini
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                sb = new StringBuilder();
                String response;
                //Reading server response
                while ((response = br.readLine()) != null){
                    sb.append(response);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public String sendGetRequest(String requestURL){
        StringBuilder sb =new StringBuilder();
        try {
            URL url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            while((s=bufferedReader.readLine())!=null){
                sb.append(s+"\n");
            }
        }catch(Exception e){
        }
        return sb.toString();
    }

    public String sendGetRequestParam(String requestURL, String id){
        StringBuilder sb =new StringBuilder();
        try {
            URL url = new URL(requestURL+id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            while((s=bufferedReader.readLine())!=null){
                sb.append(s+"\n");
            }
        }catch(Exception e){
        }
        return sb.toString();
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}


package com.example.bayup.happycooking;

/**`
 * Created by bayup on 10-May-19.
 */

public class konfigurasi {

    public static final String URL_GET_ALL = "https://puskesmasbogor.000webhostapp.com/api_happycooking/tampilsemuaresep.php";
    public static final String URL_GET_DTL = "https://puskesmasbogor.000webhostapp.com/api_happycooking/detailresep.php?id_resep=";
    public static final String URL_GET_KAT = "https://puskesmasbogor.000webhostapp.com/api_happycooking/tampilkategori.php?kategori=";

    public static final String TAG_JSON_ARRAY="result";
    public static final String TAG_id_resep = "id_resep";
    public static final String TAG_JUDUL = "judul";
    public static final String TAG_KATEGORI = "kategori";
    public static final String TAG_GAMBAR = "gambar";
    public static final String TAG_ISI = "isi";

    public static final String id_resep = "id_resep";
}


package com.example.bayup.happycooking;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.bayup.happycooking.konfigurasi.id_resep;

public class kategori extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public String kategori;
    private ListView listView;
    private String JSON_STRING;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kategori);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user user = new user();
        kategori = user.getkategori();

        setTitle(user.getkategori());

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        getResep();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent pulang = new Intent(kategori.this,MainActivity.class);
                startActivity(pulang);
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showResep(){
        JSONObject jsonObject = null;
        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
        try {
            jsonObject = new JSONObject(JSON_STRING);
            JSONArray result = jsonObject.getJSONArray(konfigurasi.TAG_JSON_ARRAY);

            for(int i = 0; i<result.length(); i++){

                JSONObject jo = result.getJSONObject(i);
                String id_resep = jo.getString(konfigurasi.TAG_id_resep);
                String gambar = jo.getString(konfigurasi.TAG_GAMBAR);
                String judul = jo.getString(konfigurasi.TAG_JUDUL);
                String kategori = jo.getString(konfigurasi.TAG_KATEGORI);

                HashMap<String,String> resep = new HashMap<>();
                resep.put(konfigurasi.TAG_id_resep,id_resep);
                resep.put(konfigurasi.TAG_GAMBAR,gambar);
                resep.put(konfigurasi.TAG_JUDUL,judul);
                resep.put(konfigurasi.TAG_KATEGORI,kategori);
                list.add(resep);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ListAdapter adapter = new MyAdapter(
                kategori.this, list, R.layout.list_item,
                new String[]{konfigurasi.TAG_id_resep, konfigurasi.TAG_GAMBAR, konfigurasi.TAG_JUDUL},
                new int[]{R.id.id_resep, R.id.gambar, R.id.judul});

        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, detailresep.class);
        HashMap<String,String> map =(HashMap)parent.getItemAtPosition(position);
        String Id = map.get(konfigurasi.TAG_id_resep).toString();
        user asal = new user();
        asal.setasal("kategori");
        intent.putExtra(konfigurasi.id_resep, Id);
        startActivity(intent);
    }


    public class MyAdapter extends SimpleAdapter {

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to){
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            // here you let SimpleAdapter built the view normally.
            View v = super.getView(position, convertView, parent);

            // Then we get reference for Picasso
            ImageView img = (ImageView) v.getTag();
            if(img == null){
                img = (ImageView) v.findViewById(R.id.gambar);
                v.setTag(img); // <<< THIS LINE !!!!
            }
            // get the url from the data you passed to the `Map`
            String url = (String) ((Map)getItem(position)).get(konfigurasi.TAG_GAMBAR);
            // do Picasso
            Picasso.with(v.getContext()).load(url).into(img);

            // return the view
            return v;
        }
    }

    private void getResep(){
        class GetResep extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                JSON_STRING = s;
                showResep();

            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String kat = rh.sendGetRequestParam(konfigurasi.URL_GET_KAT,kategori);
                return kat;

            }
        }
        GetResep gbt = new GetResep();
        gbt.execute();
    }
}


package com.example.bayup.happycooking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    private ListView listView;
    private String JSON_STRING;

    ImageView gambar;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        getResep();

        user asal = new user();
        asal.setasal("main");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        user usr = new user();

        if (id == R.id.nav_ayam) {
            Intent intent = new Intent(MainActivity.this, kategori.class);
            usr.setkategori("AYAM");
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.nav_daging) {
            Intent intent = new Intent(MainActivity.this, kategori.class);
            usr.setkategori("DAGING");
            startActivity(intent);

        } else if (id == R.id.nav_ikan) {
            Intent intent = new Intent(MainActivity.this, kategori.class);
            usr.setkategori("IKAN");
            startActivity(intent);

        } else if (id == R.id.nav_tahu) {
            Intent intent = new Intent(MainActivity.this, kategori.class);
            usr.setkategori("TAHU");
            startActivity(intent);

        } else if (id == R.id.nav_sayuran) {
            Intent intent = new Intent(MainActivity.this, kategori.class);
            usr.setkategori("SAYURAN");
            startActivity(intent);

        } else if (id == R.id.nav_sambal) {
            Intent intent = new Intent(MainActivity.this, kategori.class);
            usr.setkategori("SAMBAL");
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //onclick listview
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, detailresep.class);
        HashMap<String,String> map =(HashMap)parent.getItemAtPosition(position);
        String Id = map.get(konfigurasi.TAG_id_resep).toString();
        intent.putExtra(konfigurasi.id_resep, Id);
        startActivity(intent);
    }

    private void showResep(){
        JSONObject jsonObject = null;
        ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String, String>>();
        try {
            jsonObject = new JSONObject(JSON_STRING);
            JSONArray result = jsonObject.getJSONArray(konfigurasi.TAG_JSON_ARRAY);

            for(int i = 0; i<result.length(); i++){
                JSONObject jo = result.getJSONObject(i);
                String id_berita = jo.getString(konfigurasi.TAG_id_resep);
                String gambar = jo.getString(konfigurasi.TAG_GAMBAR);
                String judul = jo.getString(konfigurasi.TAG_JUDUL);
                String kategori = jo.getString(konfigurasi.TAG_KATEGORI);

                HashMap<String,String> news = new HashMap<>();
                news.put(konfigurasi.TAG_id_resep,id_berita);
                news.put(konfigurasi.TAG_GAMBAR,gambar);
                news.put(konfigurasi.TAG_JUDUL,judul);
                news.put(konfigurasi.TAG_KATEGORI,kategori);
                list.add(news);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ListAdapter adapter = new MyAdapter(
                MainActivity.this, list, R.layout.list_item,
                new String[]{konfigurasi.TAG_id_resep, konfigurasi.TAG_GAMBAR, konfigurasi.TAG_JUDUL},
                new int[]{R.id.id_resep, R.id.gambar, R.id.judul});

        listView.setAdapter(adapter);
    }
    public class MyAdapter extends SimpleAdapter {

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to){
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            // here you let SimpleAdapter built the view normally.
            View v = super.getView(position, convertView, parent);

            // Then we get reference for Picasso
            ImageView img = (ImageView) v.getTag();
            if(img == null){
                img = (ImageView) v.findViewById(R.id.gambar);
                v.setTag(img); // <<< THIS LINE !!!!
            }
            // get the url from the data you passed to the `Map`
            String url = (String) ((Map)getItem(position)).get(konfigurasi.TAG_GAMBAR);
            // do Picasso
            Picasso.with(v.getContext()).load(url).into(img);

            // return the view
            return v;
        }
    }

    private void getResep(){
        class GetJSON extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this,"Mengambil Data","Mohon Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                JSON_STRING = s;
                showResep();
            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String s = rh.sendGetRequest(konfigurasi.URL_GET_ALL);
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }


}


package com.example.bayup.happycooking;

/**
 * Created by bayup on 10-May-19.
 */

public class user {
    private static String kategori;
    private static String asal;

    // ini method setter
    public void setkategori(String kategori){

        user.kategori = kategori;
    }
    public void setasal(String asal){

        user.asal = asal;
    }

    // ini method getter
    public String getkategori(){

        return kategori;
    }

    public String getasal(){

        return asal;
    }
}


package com.example.bayup.happycooking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class detailresep extends AppCompatActivity {

    TextView txtjudul, txtisi, txtid_berita;

    ImageView imggambar;
    ScrollView detail;
    private String JSON_STRING;

    private String id;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailresep);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtid_berita = (TextView) findViewById(R.id.id_resep);
        txtjudul = (TextView) findViewById(R.id.judul);
        txtisi = (TextView) findViewById(R.id.isi);
        imggambar = (ImageView) findViewById(R.id.gambar);

        detail = (ScrollView) findViewById(R.id.detail);


        Intent intent = getIntent();

        id = intent.getStringExtra(konfigurasi.id_resep);
        getResep();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                user asal = new user();
                if (asal.getasal().equals("kategori")){
                    Intent pulang = new Intent(detailresep.this,kategori.class);
                    startActivity(pulang);
                }else {
                    Intent pulang = new Intent(detailresep.this, MainActivity.class);
                    startActivity(pulang);
                }

                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showResep(String json){
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray result = jsonObject.getJSONArray(konfigurasi.TAG_JSON_ARRAY);
            JSONObject c = result.getJSONObject(0);
            String id_resep = c.getString(konfigurasi.TAG_id_resep);
            String gambar = c.getString(konfigurasi.TAG_GAMBAR);
            String judul = c.getString(konfigurasi.TAG_JUDUL);
            String isi = c.getString(konfigurasi.TAG_ISI);

            String plain = Html.fromHtml(isi).toString();

            setTitle(judul);
            txtjudul.setText(judul);
            txtisi.setText(plain);
            Picasso.with(detailresep.this).load(gambar).into(imggambar);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getResep(){
        class GetResep extends AsyncTask<Void,Void,String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                detail.setVisibility(View.INVISIBLE);
                loading = ProgressDialog.show(detailresep.this,"Melihat Resep...","Mohon Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                detail.setVisibility(View.VISIBLE);
                showResep(s);

            }

            @Override
            protected String doInBackground(Void... params) {
                RequestHandler rh = new RequestHandler();
                String dtl = rh.sendGetRequestParam(konfigurasi.URL_GET_DTL,id);
                return dtl;

            }
        }
        GetResep ge = new GetResep();
        ge.execute();
    }
}


package com.example.bayup.happycooking;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {
    private int waktu_loading=2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent home=new Intent(SplashScreen.this,MainActivity.class);
                startActivity(home);
                finish();
            }
        },waktu_loading);
    }
}


