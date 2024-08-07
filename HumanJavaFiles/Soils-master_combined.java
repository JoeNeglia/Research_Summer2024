package com.application.soil.soils;

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

        assertEquals("com.application.soil.soils", appContext.getPackageName());
    }
}


package com.application.soil.soils;

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

package com.application.soil.soils;

/**
 * Created by Akarsh on 10-01-2018.
 */

public class Values {

    public static final String STATES_ENG = "https://api.myjson.com/bins/6kjkl";
    public static final String SOIL_ENG = "https://api.myjson.com/bins/11ppdl";

    public static final String STATES_HIN = "https://api.myjson.com/bins/10a9ix";
    public static final String SOIL_HIN = "https://api.myjson.com/bins/16fvx5";

    public static final String STATES_PUN = "https://api.myjson.com/bins/qb1fd";
    public static final String SOIL_PUN = "https://api.myjson.com/bins/jtexl";

    public static final String STATES_MAL = "https://api.myjson.com/bins/l0a55";
    public static final String SOIL_MAL = "https://api.myjson.com/bins/8jog9";

    public static final String STATES_BEN = "https://api.myjson.com/bins/1de0jt";
    public static final String SOIL_BEN = "https://api.myjson.com/bins/l0a55";

}


package com.application.soil.soils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class SoilEdit extends AppCompatActivity {

    String soilName, cityName;
    ArrayList<String> soilComposition;
    EditText etPotash, etPhos, etAlkali, etNitro, etIron, etLime;
    String potash, phos, alkali, nitro, iron, lime;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_edit);

        soilName = getIntent().getExtras().getString("soilName");
        cityName = getIntent().getExtras().getString("cityName");
        soilComposition = getIntent().getStringArrayListExtra("soilComposition");

        etPotash = findViewById(R.id.etPotash);
        etPhos = findViewById(R.id.etPhos);
        etAlkali = findViewById(R.id.etAlkali);
        etNitro = findViewById(R.id.etNitro);
        etIron = findViewById(R.id.etIron);
        etLime = findViewById(R.id.etLime);
        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseHelper databaseHelper = new DatabaseHelper(SoilEdit.this, cityName);
                        databaseHelper.getWritableDatabase();
                        databaseHelper.createTable(cityName);

                        if(TextUtils.isEmpty(etPotash.getText().toString()))
                            potash = soilComposition.get(0);
                        else
                            potash = etPotash.getText().toString()+"%";

                        if(TextUtils.isEmpty(etPhos.getText().toString()))
                            phos = soilComposition.get(1);
                        else
                            phos = etPhos.getText().toString()+"%";

                        if(TextUtils.isEmpty(etAlkali.getText().toString()))
                            alkali = soilComposition.get(2);
                        else
                            alkali = etAlkali.getText().toString()+"%";

                        if(TextUtils.isEmpty(etNitro.getText().toString()))
                            nitro = soilComposition.get(3);
                        else
                            nitro = etNitro.getText().toString()+"%";

                        if(TextUtils.isEmpty(etIron.getText().toString()))
                            iron = soilComposition.get(4);
                        else
                            iron = etIron.getText().toString()+"%";

                        if(TextUtils.isEmpty(etLime.getText().toString()))
                            lime = soilComposition.get(5);
                        else
                            lime = etLime.getText().toString()+"%";

                        if(databaseHelper.insertData(soilName, potash, phos, alkali, nitro, iron, lime)) {
                            Toast.makeText(SoilEdit.this, "Values edited successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else
                            Toast.makeText(SoilEdit.this, "Try Again", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}


package com.application.soil.soils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class CityActivity extends AppCompatActivity {

    ArrayList<String> cityList, soilTypes;
    ListView cityListView;
    ArrayAdapter cityAdapter;

    RequestQueue requestQueue;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        final String JSONData = getResources().getString(R.string.states_JSON);

        final String stateName = getIntent().getExtras().getString("stateName");
        final String stateNameEnglish = getIntent().getExtras().getString("stateNameEnglish");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");
        progressDialog.show();

        requestQueue = Volley.newRequestQueue(this);

        cityListView = findViewById(R.id.cityListView);
        cityList = new ArrayList<>();
        soilTypes = new ArrayList<>();
        cityAdapter = new ArrayAdapter<>(this, R.layout.listview_item, cityList);
        cityListView.setAdapter(cityAdapter);

        ActionBar myToolbar = getSupportActionBar();
        myToolbar.setTitle(getTitle()+" ("+stateName+")");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, JSONData, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray statesArray = response.getJSONArray("states");
                            for (int i = 0; i < statesArray.length(); ++i) {
                                JSONObject stateObject = statesArray.getJSONObject(i);
                                if (TextUtils.equals(stateObject.getString("sname"), stateName)) {
                                    JSONArray cityArray = stateObject.getJSONArray("cities");
                                    for (int j = 0; j < cityArray.length(); ++j) {
                                        cityList.add(cityArray.get(j).toString());
                                    }
                                    /*JSONArray soilArray = stateObject.getJSONArray("soiltype");
                                    for(int j=0; j<soilArray.length(); ++j)
                                        soilTypes.add(soilArray.get(j).toString());*/
                                    progressDialog.dismiss();
                                }
                            }
                        //    Collections.sort(cityList);
                            cityAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CityActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

        requestQueue.add(jsonObjectRequest);

        cityListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                        progressDialog.show();
                        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, Values.STATES_ENG, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            String cityNameEnglish;
                                            JSONArray statesArray = response.getJSONArray("states");
                                            for (int j = 0; j < statesArray.length(); ++j) {
                                                JSONObject stateObject = statesArray.getJSONObject(j);
                                                if (TextUtils.equals(stateObject.getString("sname"), stateNameEnglish)) {
                                                    JSONArray cityArray = stateObject.getJSONArray("cities");
                                                    cityNameEnglish = cityArray.getString(i);
                                                    Intent intent = new Intent(CityActivity.this, MapsActivity.class);
                                                    intent.putExtra("stateName", stateName);
                                                    intent.putExtra("cityName", adapterView.getItemAtPosition(i).toString());
                                                    intent.putExtra("stateNameEnglish", stateNameEnglish);
                                                    intent.putExtra("cityNameEnglish", cityNameEnglish);
                                                    startActivity(intent);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            progressDialog.dismiss();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(CityActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                        requestQueue.add(jsonObjectRequest1);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(progressDialog!=null)
            if(progressDialog.isShowing())
                progressDialog.dismiss();
    }
}

package com.application.soil.soils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ImageView weatherIcon;
    TextView weatherStatus, tempTv, pressureTv, humidityTv, tmpMinTv, tmpMaxTv, windSpeedTv, windDegTv, soilInfo;
    WeatherModel weatherModel;
    RequestQueue requestQueue;
    double lat = 20.5;
    double longi = 78.6;
    String cityName = "";
    String stateName = "";
    ProgressDialog progressDialog;
    int statePosition;
    int cityPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");
        progressDialog.show();

        stateName = getIntent().getExtras().get("stateName").toString();
        cityName = getIntent().getExtras().get("cityName").toString();
        final String stateNameEnglish = getIntent().getExtras().getString("stateNameEnglish");
        final String cityNameEnglish = getIntent().getExtras().getString("cityNameEnglish");
        final String jsonData = getResources().getString(R.string.states_JSON);

        weatherIcon = findViewById(R.id.weatherIcon);
        weatherStatus = findViewById(R.id.weatherStatus);
        tempTv = findViewById(R.id.tempTv);
        pressureTv = findViewById(R.id.pressureTv);
        humidityTv = findViewById(R.id.humidityTv);
        tmpMinTv = findViewById(R.id.tmpMinTv);
        tmpMaxTv = findViewById(R.id.tmpMaxTv);
        windSpeedTv  = findViewById(R.id.windSpeedTv);
        windDegTv = findViewById(R.id.windDegTv);
        soilInfo = findViewById(R.id.soilInfo);

        requestQueue = Volley.newRequestQueue(this);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, Values.STATES_ENG, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray statesArray = response.getJSONArray("states");
                            for (int i = 0; i < statesArray.length(); ++i) {
                                JSONObject stateObject = statesArray.getJSONObject(i);
                                if (TextUtils.equals(stateObject.getString("sname"), stateNameEnglish)) {
                                    JSONArray cityArray = stateObject.getJSONArray("cities");
                                    for (int j = 0; j < cityArray.length(); ++j) {
                                        if(TextUtils.equals(cityArray.getString(j), cityNameEnglish)) {
                                            statePosition = i;
                                            cityPosition = j;
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(jsonObjectRequest1);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, jsonData, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray statesArray = response.getJSONArray("states");
                            JSONObject stateObject = statesArray.getJSONObject(statePosition);
                            stateName = stateObject.getString("sname");
                            cityName = stateObject.getJSONArray("cities").getString(cityPosition);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MapsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonObjectRequest);


        try {
            getWeatherInfo(stateNameEnglish, cityNameEnglish);
            List<Address> addressList = geocoder.getFromLocationName(cityNameEnglish+stateNameEnglish+",India", 1);
            if(addressList.size()>0) {
                lat = addressList.get(0).getLatitude();
                longi = addressList.get(0).getLongitude();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error in weather", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        soilInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.show();
                        Intent intent = new Intent(MapsActivity.this, SoilInfoActivity.class);
                        //intent.putStringArrayListExtra("soilTypes", soilTypes);
                        intent.putExtra("stateName", stateName);
                        intent.putExtra("cityName", cityName);
                        intent.putExtra("stateNameEnglish", stateNameEnglish);
                        intent.putExtra("cityNameEnglish", cityNameEnglish);
                        startActivity(intent);
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(progressDialog!=null)
            if(progressDialog.isShowing())
                progressDialog.dismiss();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng location = new LatLng(lat, longi);
        mMap.addMarker(new MarkerOptions().position(location).title(cityName+","+stateName+",India"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 800));
        progressDialog.dismiss();
    }

    private void getWeatherInfo(String stateName, String cityName) throws IOException {

        //String weather_url = Constants.CURRENT_WEATHER_URL+cityCode+"&appid="+Constants.API_KEY+"&units=metric";
        cityName = cityName.replace("*", " ").trim();
        stateName = stateName.replace("*", "").trim();
        stateName = stateName.replace(" ","+");
        String weather_url = Constants.CURRENT_WEATHER_URL+cityName+","+stateName+",India&appid="+Constants.API_KEY+"&units=metric";
        Log.d("muy", weather_url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, weather_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        weatherModel = gson.fromJson(response, WeatherModel.class);

                        Glide.with(MapsActivity.this).load(Constants.WEATHER_ICON+weatherModel.getWeather().get(0).getIcon()+".png").into(weatherIcon);
                        weatherStatus.append(weatherModel.getWeather().get(0).getDescription());
                        tempTv.append(String.valueOf(weatherModel.getMain().getTemp()));
                        pressureTv.append(String.valueOf(weatherModel.getMain().getPressure()));
                        humidityTv.append(String.valueOf(weatherModel.getMain().getHumidity()));
                        tmpMinTv.append(String.valueOf(weatherModel.getMain().getTemp_min()));
                        tmpMaxTv.append(String.valueOf(weatherModel.getMain().getTemp_max()));
                        windSpeedTv.append(String.valueOf(weatherModel.getWind().getSpeed()));
                        windDegTv.append(String.valueOf(weatherModel.getWind().getDeg()));
                        //Log.d("weather", String.valueOf(weatherModel.getMain().getTemp()));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(MapsActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(stringRequest);
    }
}


package com.application.soil.soils;

import android.Manifest;
import android.app.ProgressDialog;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.Preference;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    LocationListener locationListener;
    LocationManager locationManager;
    Button btnFeed, btnAccess;
    //RadioGroup radioGroup;
    AlertDialog alertDialog;
    AlertDialog.Builder builder;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        sharedPref.registerOnSharedPreferenceChangeListener(this);

        String langPref = sharedPref.getString("langPref", null);

        if(langPref == null) {
            editor.putString("langPref", "hi_IN");
            editor.apply();
        }
        /*else {
            Locale locale;
            switch(langPref) {
                case "hi_IN":
                    Toast.makeText(this, "Hindi", Toast.LENGTH_SHORT).show();
                    locale = new Locale("hi", "IN");
                    Toast.makeText(this, "Lang: "+locale.getLanguage()+" "+locale.getDisplayLanguage(), Toast.LENGTH_LONG).show();
                    break;
                case "en_IN":
                    locale = new Locale("en", "IN");
                    Toast.makeText(this, "English", Toast.LENGTH_SHORT).show();
                    break;
                case "pa_IN":
                    locale = new Locale("pa", "IN");
                    Toast.makeText(this, "Lang: "+locale.getLanguage()+" "+locale.getDisplayLanguage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Punjabi", Toast.LENGTH_SHORT).show();
                    break;
                case "bn_IN":
                    locale = new Locale("bn", "IN");
                    Toast.makeText(this, "Bangali", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Lang: "+locale.getLanguage()+" "+locale.getDisplayLanguage(), Toast.LENGTH_LONG).show();
                    break;
                default:
                    locale = new Locale("hi", "IN");
            }
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }*/
        CharSequence[] languages = {"हिंदी", "English", "ਪੰਜਾਬੀ", "বাংলা"};

        builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.select_language));
        builder.setSingleChoiceItems(languages, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String pref;
                switch(i)
                {
                    case 0:
                        pref="hi_IN";
                        break;
                    case 1:
                        pref="en_IN";
                        break;
                    case 2:
                        pref="pa_IN";
                        break;
                    case 3:
                        pref="bn_IN";
                        break;
                    default:
                        pref="hi_IN";
                        break;
                }
                editor.putString("langPref", pref);
                editor.commit();
                alertDialog.dismiss();
            }
        });

        btnFeed = findViewById(R.id.btnFeed);
        btnAccess = findViewById(R.id.btnAccess);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        btnFeed.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog.setMessage("Loading");
                        progressDialog.show();

                        locationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                Geocoder geocoder = new Geocoder(MainActivity.this);
                                try {
                                    List<Address> add = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                    intent.putExtra("cityNameEnglish", add.get(0).getSubAdminArea());
                                    intent.putExtra("stateNameEnglish", add.get(0).getAdminArea());
                                    intent.putExtra("cityName", add.get(0).getSubAdminArea());
                                    intent.putExtra("stateName", add.get(0).getAdminArea());
                                    startActivity(intent);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onStatusChanged(String s, int i, Bundle bundle) {

                            }

                            @Override
                            public void onProviderEnabled(String s) {

                            }

                            @Override
                            public void onProviderDisabled(String s) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        };
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Toast.makeText(MainActivity.this, "Not granted", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1000, locationListener);
                    }
                });

        btnAccess.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, StatesActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_lang, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_language) {
            alertDialog = builder.create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(progressDialog!=null)
            if(progressDialog.isShowing())
                progressDialog.dismiss();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("langPref")) {
            String[] langPref = sharedPreferences.getString(s, null).split("_");
            Locale locale = new Locale(langPref[0], langPref[1]);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            recreate();
        }
    }
}



package com.application.soil.soils;

/**
 * Created by Akarsh on 09-01-2018.
 */

class SoilModel {

    String name;

    String potash;

    String phos;
    String alkali;
    String nitro;
    String iron;
    String lime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPotash() {
        return potash;
    }

    public void setPotash(String potash) {
        this.potash = potash;
    }

    public String getPhos() {
        return phos;
    }

    public void setPhos(String phos) {
        this.phos = phos;
    }

    public String getAlkali() {
        return alkali;
    }

    public void setAlkali(String alkali) {
        this.alkali = alkali;
    }

    public String getNitro() {
        return nitro;
    }

    public void setNitro(String nitro) {
        this.nitro = nitro;
    }

    public String getIron() {
        return iron;
    }

    public void setIron(String iron) {
        this.iron = iron;
    }

    public String getLime() {
        return lime;
    }

    public void setLime(String lime) {
        this.lime = lime;
    }
}


package com.application.soil.soils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoilDetails extends AppCompatActivity {

    RequestQueue requestQueue;

    String cityName, soilName;
    ArrayList<String> soilComposition = new ArrayList<>();
    TextView sName, sDescription, sComposition, edit;

    ImageView sImage;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_details);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.show();

        final String soilJSON = getResources().getString(R.string.soil_JSON);
        requestQueue = Volley.newRequestQueue(this);

        cityName = getIntent().getExtras().getString("cityName");
        soilName = getIntent().getExtras().getString("soilName");
        final String stateNameEnglish = getIntent().getExtras().getString("stateNameEnglish");
        final String cityNameEnglish = getIntent().getExtras().getString("cityNameEnglish");
        final String soilNameEnglish = getIntent().getExtras().getString("soilNameEnglish");

        sName = findViewById(R.id.sName);
        sDescription = findViewById(R.id.sDescription);
        sComposition = findViewById(R.id.sComposition);
        sImage = findViewById(R.id.sImage);
        edit = findViewById(R.id.edit);

        final DatabaseHelper databaseHelper = new DatabaseHelper(SoilDetails.this, cityName.trim());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, soilJSON, null,
            new Response.Listener<JSONObject>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(JSONObject response) {
                    List<SoilModel> soilModels;
                    try {
                        JSONArray soilsArray = response.getJSONArray("Soils");
                        for(int i=0; i<soilsArray.length(); ++i) {
                            JSONObject soilsObject = soilsArray.getJSONObject(i);
                            if(TextUtils.equals(soilsObject.get("soilName").toString(), soilName)) {
                                sName.setText(soilName);
                                Glide.with(SoilDetails.this).load(soilsObject.get("url")).into(sImage);
                                JSONArray charArray = soilsObject.getJSONArray("Characteristics");
                                for(int j=0; j<charArray.length(); ++j) {
                                    sDescription.append("-> "+charArray.getString(j)+"\n");
                                }
                                if(databaseHelper.isTableExist(cityNameEnglish)) {
                                    soilModels = databaseHelper.getSoilInformation(cityNameEnglish);
                                    for(int j=0; j<soilModels.size(); ++j)
                                        if(soilModels.get(j).getName().equals(soilNameEnglish)) {
                                            sComposition.setText(
                                                getResources().getString(R.string.alkali)+": "+soilModels.get(j).getAlkali()+"\n"+
                                                getResources().getString(R.string.potassium)+": "+soilModels.get(j).getPotash() +"\n"+
                                                getResources().getString(R.string.phosphorous)+": "+soilModels.get(j).getPhos()+"\n"+
                                                getResources().getString(R.string.nitrogen)+": "+soilModels.get(j).getNitro()+"\n"+
                                                getResources().getString(R.string.iron_oxide)+": "+soilModels.get(j).getIron()+"\n"+
                                                getResources().getString(R.string.lime)+": "+soilModels.get(j).getLime()+"\n");
                                            return;
                                        }
                                }
                                else {
                                    JSONObject contentsObject = soilsObject.getJSONObject("Contents");
                                    Iterator<String> keys = contentsObject.keys();
                                    while (keys.hasNext()) {
                                        String key = keys.next(); // First key in your json object
                                        String value = contentsObject.getString(key);
                                        sComposition.append(key + ": " + value + "\n");
                                        soilComposition.add(value);
                                    }
                                }
                            }
                        }
                        progressDialog.dismiss();
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(SoilDetails.this, "Error!!", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                    progressDialog.dismiss();
                }
            });

        requestQueue.add(jsonObjectRequest);

        edit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(SoilDetails.this, SoilEdit.class);
                        intent.putExtra("cityName", cityNameEnglish);
                        intent.putExtra("soilName", soilNameEnglish);
                        if(databaseHelper.isTableExist(cityNameEnglish)) {
                            Toast.makeText(SoilDetails.this, "Adding from db", Toast.LENGTH_SHORT).show();
                            List<SoilModel> soilModels = databaseHelper.getSoilInformation(cityNameEnglish);
                            for(int i=0; i<soilModels.size(); ++i)
                                if(TextUtils.equals(soilModels.get(i).getName(), soilName)) {
                                    soilComposition.add(soilModels.get(i).getPotash());
                                    soilComposition.add(soilModels.get(i).getPhos());
                                    soilComposition.add(soilModels.get(i).getAlkali());
                                    soilComposition.add(soilModels.get(i).getNitro());
                                    soilComposition.add(soilModels.get(i).getIron());
                                    soilComposition.add(soilModels.get(i).getLime());
                                }
                        }
                        intent.putStringArrayListExtra("soilComposition", soilComposition);
                        startActivity(intent);
                    }
                }
        );
    }
}


package com.application.soil.soils;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class Splash extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 5000;
    ImageView splashImageView;
    //String uriPath = "android.resource://com.ecample.lenovo.soils" + R.mipmap.farm2;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashImageView= findViewById(R.id.imageView2);
        progressBar=findViewById(R.id.progressBar2);


        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(Splash.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

    }}


package com.application.soil.soils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akarsh on 05-01-2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "data.db";
    public String TABLE_NAME = "city";
    public String COLUMN_SOIL = "soil";
    public static final String COLUMN_POTASH = "potash";
    public static final String COLUMN_PHOSPHORIC= "phosphoric";
    public static final String COLUMN_ALKALI = "alkali";
    public static final String COLUMN_NITROGEN = "nitrogen";
    public static final String COLUMN_IRON = "iron";
    public static final String COLUMN_LIME = "lime";
    Context context;
    //private DatabaseHelper databaseHelper;

    public DatabaseHelper(Context context, String cityName) {
        super(context, DB_NAME, null, 1);
        cityName = cityName.trim();
        TABLE_NAME = cityName;
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+COLUMN_SOIL+" TEXT, "+COLUMN_POTASH+" TEXT, "+COLUMN_PHOSPHORIC+" TEXT, "+COLUMN_ALKALI+" TEXT, "+COLUMN_NITROGEN+" TEXT, "+COLUMN_IRON+" TEXT,"+COLUMN_LIME+" TEXT);";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void createTable(String tableName) {
        tableName = tableName.trim();
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        String query = "CREATE TABLE IF NOT EXISTS "+tableName+" ("+COLUMN_SOIL+" TEXT, "+COLUMN_POTASH+" TEXT, "+COLUMN_PHOSPHORIC+" TEXT, "+COLUMN_ALKALI+" TEXT, "+COLUMN_NITROGEN+" TEXT, "+COLUMN_IRON+" TEXT,"+COLUMN_LIME+" TEXT);";
        sqLiteDatabase.execSQL(query);
    }

    public boolean insertData(String soil, String potash, String phos, String alkali, String nitro, String iron, String lime) {

        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_SOIL, soil);
        contentValues.put(COLUMN_POTASH, potash);
        contentValues.put(COLUMN_PHOSPHORIC, phos);
        contentValues.put(COLUMN_ALKALI, alkali);
        contentValues.put(COLUMN_NITROGEN, nitro);
        contentValues.put(COLUMN_IRON, iron);
        contentValues.put(COLUMN_LIME, lime);

        TABLE_NAME = TABLE_NAME.trim();

        String queryDelete = "DELETE FROM "+TABLE_NAME+" WHERE "+COLUMN_SOIL+"='"+soil+"';";
        String query = "INSERT INTO "+TABLE_NAME+" VALUES ('"+soil+"', '"+potash+"', '"+phos+"', '"+alkali+"', '"+nitro+"', '"+iron+"', '"+lime+"');";
        sqLiteDatabase.execSQL(queryDelete);
        sqLiteDatabase.execSQL(query);

        //long l1 = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        return true;
/*

        if(l1 > 0)
            return true;
        else
            return false;
*/
    }

    public List<SoilModel> getSoilInformation(String tableName) {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        List<SoilModel> soilModels = new ArrayList<>();
        String query = "SELECT * FROM "+tableName+";";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        do {
            String soil = cursor.getString(cursor.getColumnIndex(COLUMN_SOIL));
            String potash = cursor.getString(cursor.getColumnIndex(COLUMN_POTASH));
            String phos = cursor.getString(cursor.getColumnIndex(COLUMN_PHOSPHORIC));
            String alkali = cursor.getString(cursor.getColumnIndex(COLUMN_ALKALI));
            String nitro = cursor.getString(cursor.getColumnIndex(COLUMN_NITROGEN));
            String iron = cursor.getString(cursor.getColumnIndex(COLUMN_IRON));
            String lime = cursor.getString(cursor.getColumnIndex(COLUMN_LIME));
            SoilModel soilModel = new SoilModel();
            soilModel.setName(soil);
            soilModel.setPotash(potash);
            soilModel.setPhos(phos);
            soilModel.setAlkali(alkali);
            soilModel.setNitro(nitro);
            soilModel.setIron(iron);
            soilModel.setLime(lime);
            soilModels.add(soilModel);
        } while(cursor.moveToNext());
        cursor.close();
        Log.d("muy", soilModels.toString());
        return soilModels;
    }

    public boolean isTableExist(String tableName) {

        SQLiteDatabase sqLiteDatabase = getReadableDatabase();

        //String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
        try {
            String query = "SELECT * FROM " + tableName + ";";
            /*Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    Toast.makeText(context, "Returning true", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(context, "Returning false from else", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            Toast.makeText(context, "Returning false", Toast.LENGTH_SHORT).show();
            return false;*/
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            if(cursor.getCount()==0) {
                return false;
            }
            else {
                return true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}

package com.application.soil.soils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatesActivity extends AppCompatActivity {

    ListView stateListView;
    RequestQueue requestQueue;
    List<String> stateList;
    ArrayAdapter<String> stateAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_states);

        final String JSONData = StatesActivity.this.getResources().getString(R.string.states_JSON);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading");
        progressDialog.show();

        requestQueue = Volley.newRequestQueue(this);
        stateListView = findViewById(R.id.stateListView);
        stateList = new ArrayList<>();

        stateAdapter = new ArrayAdapter<>(this, R.layout.listview_item, stateList);

        stateListView.setAdapter(stateAdapter);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, JSONData, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray statesArray = response.getJSONArray("states");
                            for (int i = 0; i < statesArray.length(); ++i) {
                                JSONObject statesObject = statesArray.getJSONObject(i);
                                stateList.add(statesObject.get("sname").toString());
                                //Collections.sort(stateList);
                                //Log.d("state",statesObject.get("sname").toString());
                                stateAdapter.notifyDataSetChanged();
                                progressDialog.dismiss();
                            }
                            stateAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(StatesActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

        requestQueue.add(jsonObjectRequest);

        stateListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {
                        progressDialog.show();
                        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, Values.STATES_ENG, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            String state;
                                            JSONArray statesArray = response.getJSONArray("states");
                                            state = statesArray.getJSONObject(i).getString("sname");
                                            Intent intent = new Intent(StatesActivity.this, CityActivity.class);
                                            intent.putExtra("stateName", adapterView.getItemAtPosition(i).toString());
                                            intent.putExtra("stateNameEnglish", state);
                                            startActivity(intent);
                                            progressDialog.dismiss();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(StatesActivity.this, "Error!!", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                });
                        requestQueue.add(jsonObjectRequest1);
                    }
                }
        );

    }
}


package com.application.soil.soils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

public class SoilInfoActivity extends AppCompatActivity {

    ListView soilListView;
    ArrayAdapter<String> soilArrayAdapter;
    ArrayList<String> soilTypes;
    String stateName, cityName;
    RequestQueue requestQueue;
    int statePosition, cityPosition;
    JSONArray soilJSONArray;
    String stateNameEnglish, cityNameEnglish;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_info);

        final String JSONData = getResources().getString(R.string.states_JSON);

        //stateName = getIntent().getExtras().getString("stateName");
        //cityName = getIntent().getExtras().getString("cityName");
        stateNameEnglish = getIntent().getExtras().getString("stateNameEnglish");
        cityNameEnglish = getIntent().getExtras().getString("cityNameEnglish");

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        soilListView = findViewById(R.id.soilListView);
        soilTypes = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        soilArrayAdapter = new ArrayAdapter<>(this, R.layout.listview_item, soilTypes);
        soilListView.setAdapter(soilArrayAdapter);

        stateNameEnglish = stateNameEnglish.trim();
        cityNameEnglish = cityNameEnglish.trim();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, JSONData, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            JSONArray statesArray = response.getJSONArray("states");
                            JSONObject stateObject = statesArray.getJSONObject(statePosition);
                            stateName = stateObject.getString("sname");
                            cityName = stateObject.getJSONArray("cities").getString(cityPosition);
                            JSONArray soilArray = stateObject.getJSONArray("soiltype");
                            for(int j=0; j<soilArray.length(); ++j) {
                                soilTypes.add(soilArray.get(j).toString());
                                soilArrayAdapter.notifyDataSetChanged();
                            }
                            soilArrayAdapter.notifyDataSetChanged();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(SoilInfoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }){
            @Override
            public Priority getPriority() {
                return Priority.LOW;
            }
        };

        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, Values.STATES_ENG, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray statesArray = response.getJSONArray("states");
                            for (int i = 0; i < statesArray.length(); ++i) {
                                JSONObject stateObject = statesArray.getJSONObject(i);
                                if (TextUtils.equals(stateObject.getString("sname"), stateNameEnglish)) {
                                    JSONArray cityArray = stateObject.getJSONArray("cities");
                                    for (int j = 0; j < cityArray.length(); ++j) {
                                        if(TextUtils.equals(cityArray.getString(j).trim(), cityNameEnglish)) {
                                            statePosition = i;
                                            cityPosition = j;
                                            soilJSONArray = stateObject.getJSONArray("soiltype");
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(SoilInfoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }){
            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }
        };

        requestQueue.add(jsonObjectRequest1);
        requestQueue.add(jsonObjectRequest);

        soilListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {
          /*              JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.GET, Values.STATES_ENG, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        JSONArray statesArray;
                                        try {
                                            statesArray = response.getJSONArray("states");
                                            for (int j = 0; j < statesArray.length(); ++j) {
                                                JSONObject stateObject = statesArray.getJSONObject(j);
                                                if (TextUtils.equals(stateObject.getString("sname"), stateNameEnglish)) {
                                                    JSONArray soilArray = stateObject.getJSONArray("soiltype");
                                                    String soilNameEnglish = soilArray.getString(i);
                                                    Intent intent = new Intent(SoilInfoActivity.this, SoilDetails.class);
                                                    intent.putExtra("soilName", adapterView.getItemAtPosition(i).toString());
                                                    intent.putExtra("soilNameEnglish", soilNameEnglish);
                                                    intent.putExtra("cityName", cityName);
                                                    intent.putExtra("stateNameEnglish", stateNameEnglish);
                                                    intent.putExtra("cityNameEnglish", cityNameEnglish);
                                                    startActivity(intent);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                });
                        requestQueue.add(jsonObjectRequest1);*/

                        try {
                            Intent intent = new Intent(SoilInfoActivity.this, SoilDetails.class);
                            intent.putExtra("soilName", adapterView.getItemAtPosition(i).toString());
                            intent.putExtra("soilNameEnglish", soilJSONArray.getString(i));
                            intent.putExtra("cityName", cityName);
                            intent.putExtra("stateNameEnglish", stateNameEnglish);
                            intent.putExtra("cityNameEnglish", cityNameEnglish);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }
}


package com.application.soil.soils;

import java.util.List;

/**
 * Created by Akarsh on 10-08-2017.
 */

public class WeatherModel {


    /**
     * coord : {"lon":78.77,"lat":28.83}
     * weather : [{"id":800,"main":"Clear","description":"clear sky","icon":"01n"}]
     * base : stations
     * main : {"temp":8.7,"pressure":1005.08,"humidity":90,"temp_min":8.7,"temp_max":8.7,"sea_level":1029.03,"grnd_level":1005.08}
     * wind : {"speed":4.31,"deg":284.001}
     * clouds : {"all":0}
     * dt : 1515349879
     * sys : {"message":0.007,"country":"IN","sunrise":1515289157,"sunset":1515326622}
     * id : 1262801
     * name : Moradabad
     * cod : 200
     */

    private CoordBean coord;
    private String base;
    private MainBean main;
    private WindBean wind;
    private CloudsBean clouds;
    private int dt;
    private SysBean sys;
    private int id;
    private String name;
    private int cod;
    private List<WeatherBean> weather;

    public CoordBean getCoord() {
        return coord;
    }

    public void setCoord(CoordBean coord) {
        this.coord = coord;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public MainBean getMain() {
        return main;
    }

    public void setMain(MainBean main) {
        this.main = main;
    }

    public WindBean getWind() {
        return wind;
    }

    public void setWind(WindBean wind) {
        this.wind = wind;
    }

    public CloudsBean getClouds() {
        return clouds;
    }

    public void setClouds(CloudsBean clouds) {
        this.clouds = clouds;
    }

    public int getDt() {
        return dt;
    }

    public void setDt(int dt) {
        this.dt = dt;
    }

    public SysBean getSys() {
        return sys;
    }

    public void setSys(SysBean sys) {
        this.sys = sys;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCod() {
        return cod;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public List<WeatherBean> getWeather() {
        return weather;
    }

    public void setWeather(List<WeatherBean> weather) {
        this.weather = weather;
    }

    public static class CoordBean {
        /**
         * lon : 78.77
         * lat : 28.83
         */

        private double lon;
        private double lat;

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }
    }

    public static class MainBean {
        /**
         * temp : 8.7
         * pressure : 1005.08
         * humidity : 90
         * temp_min : 8.7
         * temp_max : 8.7
         * sea_level : 1029.03
         * grnd_level : 1005.08
         */

        private double temp;
        private double pressure;
        private int humidity;
        private double temp_min;
        private double temp_max;
        private double sea_level;
        private double grnd_level;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public double getTemp_min() {
            return temp_min;
        }

        public void setTemp_min(double temp_min) {
            this.temp_min = temp_min;
        }

        public double getTemp_max() {
            return temp_max;
        }

        public void setTemp_max(double temp_max) {
            this.temp_max = temp_max;
        }

        public double getSea_level() {
            return sea_level;
        }

        public void setSea_level(double sea_level) {
            this.sea_level = sea_level;
        }

        public double getGrnd_level() {
            return grnd_level;
        }

        public void setGrnd_level(double grnd_level) {
            this.grnd_level = grnd_level;
        }
    }

    public static class WindBean {
        /**
         * speed : 4.31
         * deg : 284.001
         */

        private double speed;
        private double deg;

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getDeg() {
            return deg;
        }

        public void setDeg(double deg) {
            this.deg = deg;
        }
    }

    public static class CloudsBean {
        /**
         * all : 0
         */

        private int all;

        public int getAll() {
            return all;
        }

        public void setAll(int all) {
            this.all = all;
        }
    }

    public static class SysBean {
        /**
         * message : 0.007
         * country : IN
         * sunrise : 1515289157
         * sunset : 1515326622
         */

        private double message;
        private String country;
        private int sunrise;
        private int sunset;

        public double getMessage() {
            return message;
        }

        public void setMessage(double message) {
            this.message = message;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public int getSunrise() {
            return sunrise;
        }

        public void setSunrise(int sunrise) {
            this.sunrise = sunrise;
        }

        public int getSunset() {
            return sunset;
        }

        public void setSunset(int sunset) {
            this.sunset = sunset;
        }
    }

    public static class WeatherBean {
        /**
         * id : 800
         * main : Clear
         * description : clear sky
         * icon : 01n
         */

        private int id;
        private String main;
        private String description;
        private String icon;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}


package com.application.soil.soils;

/**
 * Created by Akarsh on 10-08-2017.
 */

public class Constants {

    public static final String API_KEY =  "db1bb48267e5522f512dc3b3e1b26f95";
    public static final String CURRENT_WEATHER_URL =  "http://api.openweathermap.org/data/2.5/weather?q=";
    public static final String WEATHER_ICON =  "http://openweathermap.org/img/w/";

}


