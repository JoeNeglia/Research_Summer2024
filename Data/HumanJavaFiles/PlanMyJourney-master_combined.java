package com.application.recommend.recommendplaces;

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

        assertEquals("com.application.recommend.recommendplaces", appContext.getPackageName());
    }
}


package com.application.recommend.recommendplaces;

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

package com.application.recommend.recommendplaces;

/**
 * Created by Akarsh on 28-01-2018.
 */

public class Values {

    String CLIENT_ID = "0Y13VO4S0HUYMZKGFNB2AHEYEFLL31VZXGJ2UX02G31WZIKN";
    String CLIENT_SECRET = "CDI1NS2CWHLUCDYBMVIDT51QEUNGSZRJ3QPLTMLLALJXJQF2";

    public String getCLIENT_ID() {
        return CLIENT_ID;
    }

    public String getCLIENT_SECRET() {
        return CLIENT_SECRET;
    }
}


package com.application.recommend.recommendplaces;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = findViewById(R.id.imageView);

        Glide.with(this).load(getIntent().getStringExtra("imageURL")).into(imageView);

    }
}


package com.application.recommend.recommendplaces;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SourceDestActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvLocation;
    EditText etSearch;
    ImageButton ibSearch;
    ImageView ivFood, ivCafe, ivFun, ivShopping;
    LocationListener locationListener;
    LocationManager locationManager;
    double lat = 0;
    double longi = 0;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source_dest);

        etSearch = findViewById(R.id.etSearch);
        tvLocation = findViewById(R.id.tvLocation);
        ibSearch = findViewById(R.id.ibSearch);
        ivFood = findViewById(R.id.ivFood);
        ivCafe = findViewById(R.id.ivCafe);
        ivFun = findViewById(R.id.ivFun);
        ivShopping = findViewById(R.id.ivShopping);

        ibSearch.setOnClickListener(this);
        ivFood.setOnClickListener(this);
        ivCafe.setOnClickListener(this);
        ivFun.setOnClickListener(this);
        ivShopping.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.show();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Toast.makeText(SourceDestActivity.this, "You are in: " + addressList.get(0).getSubAdminArea() + ", " + addressList.get(0).getAdminArea(), Toast.LENGTH_LONG).show();
                    lat = location.getLatitude();
                    longi = location.getLongitude();
                    tvLocation.setText(addressList.get(0).getLocality());
                    progressDialog.dismiss();
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1000, locationListener);
    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(SourceDestActivity.this, PlacesActivity.class);
        intent.putExtra("lat", String.valueOf(lat));
        intent.putExtra("longi", String.valueOf(longi));

        switch (view.getId()) {

            case R.id.ivFood:
                intent.putExtra("placeType", "food");
                startActivity(intent);
                break;

            case R.id.ivCafe:
                intent.putExtra("placeType", "coffee");
                startActivity(intent);
                break;

            case R.id.ivFun:
                intent.putExtra("placeType", "fun");
                startActivity(intent);
                break;

            case R.id.ivShopping:
                intent.putExtra("placeType", "shops");
                startActivity(intent);
                break;

        }
    }
}


package com.application.recommend.recommendplaces;

/**
 * Created by lenovo on 1/17/2018.
 */

public class MyInfo {
    public String name,email,dob,pass,phone,otp;
    MyInfo(){

    }
    MyInfo(String name,String email,String dob,String pass,String phone,String otp)
    {
        //this.key=key;
        this.name=name;
        this.email=email;
        this.dob=dob;
        this.pass=pass;
        this.phone=phone;
        this.otp=otp;
    }
}


package com.application.recommend.recommendplaces;

import java.io.Serializable;

/**
 * Created by Akarsh on 11-01-2018.
 */

public class PlacesModel implements Serializable{

    private String id;
    private String name;
    private String lat;
    private String longi;
    private String add1 = "";
    private String price;
    private String rating;
    private String ratingColor;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAdd1() {
        return add1;
    }

    public String getPrice() {
        return price;
    }

    public String getRating() {
        return rating;
    }

    public String getRatingColor() {
        return ratingColor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongi() {
        return longi;
    }

    public void setLongi(String longi) {
        this.longi = longi;
    }

    public void setAdd1(String add1) {
        this.add1 = add1;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setRatingColor(String ratingColor) {
        this.ratingColor = ratingColor;
    }
}


package com.application.recommend.recommendplaces;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Info extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    EditText t1,t2;
    ImageButton img1,img2;
    TextToSpeech tts;
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        t1=findViewById(R.id.etsource);
        t2=findViewById(R.id.etdest);
        img1=findViewById(R.id.imageButton);
        img2=findViewById(R.id.imageButton2);
        tv=findViewById(R.id.tvTime);
        tv.setOnClickListener(this);
        img1.setOnClickListener(this);
        img2.setOnClickListener(this);
        tts=new TextToSpeech(this,this);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);


        finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        tts.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
    }

    @Override
    public void onClick(View view) {
        if(view==img1)
        {
            Intent in= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            in.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say");
            startActivityForResult(in,1234);

        }

        if(view==img2)
        {
            Intent in= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            in.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say");
            startActivityForResult(in,123);

        }

        if(view==tv)
        {
            startActivity(new Intent(this,Time.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1234 && resultCode==RESULT_OK)
        {
            ArrayList<String> array =data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            t1.setText(array.get(0));
            t2.requestFocus();
            tts.speak("Fill the destination",TextToSpeech.QUEUE_FLUSH,null);

        }
        if(requestCode == 123 && resultCode==RESULT_OK)
        {
            ArrayList<String> array =data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            t2.setText(array.get(0));

        }

    }

    @Override
    public void onInit(int i) {

        /*if(i==TextToSpeech.SUCCESS)
            Toast.makeText(this, "Initialised....", Toast.LENGTH_SHORT).show();*/
        //tts.speak("Intialised",TextToSpeech.QUEUE_FLUSH,null);
        String a=t1.getText().toString();
        String b=t2.getText().toString();
        if(a.length()==0 || b.length()==0)
            tts.speak("Fill the source and destination",TextToSpeech.QUEUE_FLUSH,null);
        if(i==TextToSpeech.ERROR)
            Toast.makeText(this, "Error Intialising..", Toast.LENGTH_SHORT).show();
        // tts.speak("Error in Initialising",TextToSpeech.QUEUE_FLUSH,null);
        if(i==TextToSpeech.STOPPED)
            Toast.makeText(this, "Stopped...", Toast.LENGTH_SHORT).show();
    }
}


package com.application.recommend.recommendplaces;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Akarsh on 16-01-2018.
 */

class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.Items> {

    List<ImagesModel> imagesModels;
    Context context;

    public ImagesAdapter(List<ImagesModel> imagesModels, Context context) {
        this.imagesModels = imagesModels;
        this.context = context;
    }

    @Override
    public ImagesAdapter.Items onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.images_item, parent, false);
        return new Items(view);
    }

    @Override
    public void onBindViewHolder(final ImagesAdapter.Items holder, int position) {
        final String prefix = imagesModels.get(position).getPrefix();
        final String suffix = imagesModels.get(position).getSuffix();

        String url = prefix+"500x500"+imagesModels.get(position).getSuffix();
        Picasso.with(context).load(url).into(holder.imageView);
        holder.imageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String link = prefix+imagesModels.get(holder.getAdapterPosition()).getWidth()+"x"+imagesModels.get(holder.getAdapterPosition()).getHeight()+suffix;
                        Intent intent = new Intent(context, ImageActivity.class);
                        intent.putExtra("imageURL", link);
                        context.startActivity(intent);
                    }
                }
        );

    }

    @Override
    public int getItemCount() {
        return imagesModels.size();
    }

    public class Items extends RecyclerView.ViewHolder {

        ImageView imageView;

        public Items(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}


package com.application.recommend.recommendplaces;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PlaceDetails extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double lat=0;
    double longi=0;
    public static final String BASE_URL = "https://api.foursquare.com/v2/";
    String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
    RequestQueue requestQueue;

    TextView tvAddress, tvRating, tvOpen, tvContact;
    RatingBar ratingBar;
    LinearLayout llRating;
    RecyclerView imagesRecyclerView;
    GridLayoutManager gridLayoutManager;
    ImagesAdapter imagesAdapter;
    ArrayList<ImagesModel> imagesModels = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();

        ArrayList<PlacesModel> placesModels = (ArrayList<PlacesModel>) getIntent().getSerializableExtra("BUNDLE");
        int position = getIntent().getExtras().getInt("position");

        lat = Double.parseDouble(placesModels.get(position).getLat());
        longi = Double.parseDouble(placesModels.get(position).getLongi());
        requestQueue = Volley.newRequestQueue(this);
        tvAddress = findViewById(R.id.tvAddress);
        tvRating = findViewById(R.id.tvRating);
        tvOpen = findViewById(R.id.tvOpen);
        ratingBar = findViewById(R.id.ratingBar);
        llRating = findViewById(R.id.llRating);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
        tvContact = findViewById(R.id.tvContact);
        gridLayoutManager = new GridLayoutManager(this, 1, LinearLayoutManager.HORIZONTAL, false);
        imagesAdapter = new ImagesAdapter(imagesModels, this);
        imagesRecyclerView.setLayoutManager(gridLayoutManager);
        imagesRecyclerView.setAdapter(imagesAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(placesModels.get(position).getName());
        setActionBar(toolbar);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Values values = new Values();
        String url = BASE_URL+"venues/"+placesModels.get(position).getId()+"?&client_id="+values.getCLIENT_ID()+"&client_secret="+values.getCLIENT_SECRET()+"&v="+date;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject responseObject = response.getJSONObject("response");
                            JSONObject venueObject = responseObject.getJSONObject("venue");
                            if(venueObject.has("hours")) {
                                JSONObject hoursObject = venueObject.getJSONObject("hours");
                                if (hoursObject.getString("isOpen").equals("true")) {
                                    Toast.makeText(PlaceDetails.this, "Open", Toast.LENGTH_SHORT).show();
                                    tvOpen.setText(hoursObject.getString("status"));
                                    tvOpen.setBackgroundColor(Color.GREEN);
                                } else {
                                    JSONArray timeframesArray = hoursObject.getJSONArray("timeframes");
                                    for (int i = 0; i < timeframesArray.length(); ++i) {
                                        JSONObject timeObject = timeframesArray.getJSONObject(i);
                                        if (timeObject.get("includesToday").equals("true")) {
                                            String time = timeObject.getJSONArray("open").getJSONObject(0).getString("renderedTime");
                                            tvOpen.setText(time);
                                            tvOpen.setBackgroundColor(Color.GREEN);
                                        } else {
                                            tvOpen.setText("We're closed this time");
                                            tvOpen.setTextColor(Color.WHITE);
                                            tvOpen.setBackgroundColor(Color.RED);
                                        }
                                    }
                                }
                            }
                            else {
                                tvOpen.setVisibility(View.GONE);
                            }
                            JSONObject photosObject = venueObject.getJSONObject("photos");
                            if(photosObject.getInt("count")>0) {
                                JSONArray groupsArray = photosObject.getJSONArray("groups");
                                for (int i=0; i<groupsArray.length(); ++i) {
                                    JSONObject groupsObject = groupsArray.getJSONObject(i);
                                    JSONArray itemsArray = groupsObject.getJSONArray("items");
                                    for(int j=0; j<itemsArray.length(); ++j) {
                                        JSONObject itemsObject = itemsArray.getJSONObject(j);
                                        ImagesModel imagesModel = new ImagesModel();
                                        imagesModel.setPrefix(itemsObject.getString("prefix"));
                                        imagesModel.setSuffix(itemsObject.getString("suffix"));
                                        imagesModel.setWidth(itemsObject.getString("width"));
                                        imagesModel.setHeight(itemsObject.getString("height"));
                                        imagesModels.add(imagesModel);
                                        imagesAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                            JSONObject contactObject = venueObject.getJSONObject("contact");
                            if(!TextUtils.isEmpty(contactObject.getString("formattedPhone")))
                                tvContact.setText(contactObject.getString("formattedPhone"));
                            else
                                if(!TextUtils.isEmpty(contactObject.getString("phone")))
                                    tvContact.setText(contactObject.getString("phone"));
                                else
                                    tvContact.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                });

        requestQueue.add(jsonObjectRequest);

        if(placesModels.get(position).getRating().equals("0"))
            llRating.setVisibility(View.GONE);
        else {
            float rating = Float.parseFloat(placesModels.get(position).getRating())/2;
            ratingBar.setRating(rating);
            tvRating.setText(String.valueOf(rating));
            tvRating.setBackgroundColor(Color.parseColor("#"+placesModels.get(position).getRatingColor()));
        }

        tvAddress.setText(placesModels.get(position).getAdd1());
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(lat, longi);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMapLoadedCallback(
                new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longi), 15));
                    }
                }
        );

    }
}


package com.application.recommend.recommendplaces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import com.basgeekball.awesomevalidation.AwesomeValidation;
//import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {
    EditText etuser,etpass;
    Button bsignup,bsignin;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressDialog dialog;
    TextToSpeech tts;
    DatabaseReference reference;
    String email,password;
    TextView tvlogin;
    ImageView img;
    boolean hidden =true;
    boolean Success;
    ConnectivityManager connectivityManager;
    NetworkInfo networkInfo;
    int i=0;
 //   AwesomeValidation validation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_ACTIVITY_CLEAR_TOP);
        setContentView(R.layout.activity_main);
        etuser=findViewById(R.id.etuser);
        etpass=findViewById(R.id.etpass);
        img=findViewById(R.id.imageButton3);
        img.setOnClickListener(this);
       // bsignin=findViewById(R.id.bsignin);
        bsignup=findViewById(R.id.bsignup);
        bsignup.setOnClickListener(this);
       // bsignin.setOnClickListener(this);
        tvlogin=findViewById(R.id.tvlogin);
        tvlogin.setOnClickListener(this);
        dialog=new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();
        user=FirebaseAuth.getInstance().getCurrentUser();
        tts=new TextToSpeech(this,this);
        reference= FirebaseDatabase.getInstance().getReference();
        reference.child("Records").child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Toast.makeText(MainActivity.this, "Username already exists!!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            // reference.addValueEventListener(this);

    });
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
    }
    @Override
    public void onBackPressed() {
        long t=System.currentTimeMillis();
        if(i==0 ){
            i=1;
            Toast.makeText(this, "Press again to exit!!", Toast.LENGTH_SHORT).show();
        }
        else if(System.currentTimeMillis()-t<1000)
        {
            super.onBackPressed();
            //System.exit(0);
           /* Intent intent = new Intent(this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);*/
           finish();
          // return;

        }
       // i=0;
    }


    @Override
    public void onClick(View view) {
        final String str = etuser.getText().toString();

        if (view == bsignup) {
            //final String use=etuser.getText().toString();
            //String pass=etpass.getText().toString();
            if (TextUtils.isEmpty(etuser.getText().toString()) || TextUtils.isEmpty(etpass.getText().toString())) {
                Toast.makeText(this, "Fill the fields..then only you can proceed further!!!!", Toast.LENGTH_SHORT).show();
                tts.speak("Fill the fields..then only you can proceed further", TextToSpeech.QUEUE_FLUSH, null);
            } else if (!str.contains("@") || !str.contains(".com"))
                Toast.makeText(this, "Invalid Email!!! ", Toast.LENGTH_SHORT).show();
            else if (etpass.length() < 6)
                Toast.makeText(this, R.string.pass, Toast.LENGTH_SHORT).show();
            /* else  if(user.getEmail().toString().equals(etuser.getText().toString()))
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();*/


            else {
                registerUser();

            }


        }
        if (view == tvlogin) {


            Intent in = new Intent(this, SignIn.class);
            in.putExtra("username", etuser.getText().toString());
            in.putExtra("password", etpass.getText().toString());
            startActivity(in);
        }

        if (view == img) {
            if (hidden) {
                etpass.setTransformationMethod(null);
                img.setImageResource(R.mipmap.green);
                hidden=false;
            }
            else {
                etpass.setTransformationMethod(new PasswordTransformationMethod());
                img.setImageResource(R.mipmap.red);
                hidden=true;
            }
        }
    }


    private void registerUser() {

        //final boolean Success;
        String user=etuser.getText().toString();
        String pass=etpass.getText().toString();

        if(TextUtils.isEmpty(user)|| TextUtils.isEmpty(pass))
            Toast.makeText(this, "All fields are Mandatory!!!", Toast.LENGTH_SHORT).show();
        dialog.setMessage("Registering in firebase...PLease Wait");
        dialog.show();
        auth.createUserWithEmailAndPassword(user,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                     Toast.makeText(MainActivity.this, "Successfully Registered!!!", Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                    //startActivity(new Intent(MainActivity.this,Login.class));
                    Intent in = new Intent(MainActivity.this, Signup.class);
                    in.putExtra("username", etuser.getText().toString());
                    in.putExtra("password", etpass.getText().toString());
                    startActivity(in);
                    Log.d("Suces","in sucees"+String.valueOf(Success));


                }

            }

        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

               Toast.makeText(MainActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                etuser.setText("");
                etpass.setText("");
                etuser.requestFocus();

            }
        });
            //return Success;
    }

    @Override
    public void onInit(int i) {

    }



}




package com.application.recommend.recommendplaces;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class Time extends AppCompatActivity implements TextToSpeech.OnInitListener, View.OnClickListener {

    TextToSpeech tts;
    FirebaseAuth auth;
   // TimePickerDialog dialog;
    TextView tv1,tv2;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tts=new TextToSpeech(this,this);
        auth=FirebaseAuth.getInstance();
        tv1=findViewById(R.id.tvFrom);
        tv2=findViewById(R.id.tvTo);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public void onInit(int i) {
        if(i==TextToSpeech.SUCCESS)
        {
            tts.speak("Enter the time",TextToSpeech.QUEUE_FLUSH,null);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
           // startActivity(new Intent(this,Main3Activity.class));
            return true;
        }

        if (id == R.id.action_cab) {
           // startActivity(new Intent(this,Main3Activity.class));
            return true;
        }

        if (id == R.id.action_sign_out) {
            // startActivity(new Intent(this,Main3Activity.class));
            auth.signOut();
            Intent intent = new Intent(this,
                    SignIn.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        tts.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.stop();
        tts.shutdown();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,Info.class));
        finish();
    }

    @Override
    public void onClick(View view) {
          TimePickerDialog dialog;
        if(view==tv1)
        {
            Calendar c=Calendar.getInstance();
            int hour=c.get(Calendar.HOUR);
            int min=c.get(Calendar.MINUTE);
            dialog=new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    tv1.setText(i +": "+i1);
                }
            },hour,min,true);

            dialog.show();
        }
        else
        if(view==tv2)
        {
            Calendar c=Calendar.getInstance();
            int hour=c.get(Calendar.HOUR);
            int min=c.get(Calendar.MINUTE);
            dialog=new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {

                    String str=tv1.getText().toString();
                    String [] timepart=str.split(":");
                    String hour=timepart[0];
                    String min=timepart[1];
                    min=min.replace(" ","");
                     if(Integer.parseInt(hour)>i)
                     {
                         Toast.makeText(Time.this, "The destination time should be larger than the the source time", Toast.LENGTH_SHORT).show();
                     }
                     else if(Integer.parseInt(hour)==i)
                     {
                         if(Integer.parseInt(min)>i1)
                             Toast.makeText(Time.this, "The destination time should be larger than the the source time", Toast.LENGTH_SHORT).show();
                     }

                        else
                         tv2.setText(i+":"+i1);
                }
            },hour,min,true);

            dialog.show();
        }
        else
            if(view==fab) {
                Intent intent = new Intent(Time.this, SourceDestActivity.class);
                startActivity(intent);
        }

    }
}


package com.application.recommend.recommendplaces;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PlacesActivity extends AppCompatActivity {

    Values values = new Values();
    public static final String BASE_URL = "https://api.foursquare.com/v2/";
    public final String EXPLORE_REQUEST = "venues/explore?client_id="+values.getCLIENT_ID()+"&client_secret="+values.getCLIENT_SECRET();
    public final String LAT_LANG_REQUEST = BASE_URL+EXPLORE_REQUEST+"&ll=";
    String date = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

    RequestQueue requestQueue;
    RecyclerView placesRecyclerView;
    LinearLayoutManager linearLayoutManager;
    PlacesAdapter placesAdapter;
    ArrayList<PlacesModel>  placesModels = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();

        String lat = getIntent().getExtras().getString("lat");
        String longi = getIntent().getExtras().getString("longi");
        /*String lat = "19.0817112";
        String longi = "72.83105";*/

        String categoryId="";

        switch (getIntent().getExtras().getString("placeType")) {

            case "food":
                categoryId="4bf58dd8d48988d1f9941735";
                break;

            case "coffee":
                categoryId="4bf58dd8d48988d1e0931735";
                break;

            case "shops":
                categoryId = "4d4b7105d754a06378d81259";
                break;

            case "fun":
                categoryId = "4d4b7104d754a06370d81259";
        }

        requestQueue = Volley.newRequestQueue(this);
        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        placesAdapter = new PlacesAdapter(this, placesModels);
        linearLayoutManager = new LinearLayoutManager(this);

        placesRecyclerView.setLayoutManager(linearLayoutManager);
        placesRecyclerView.setAdapter(placesAdapter);

        String url = LAT_LANG_REQUEST+lat+","+longi+"&categoryId="+categoryId+"&v="+date+"&sortByDistance=1";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getJSONObject("meta").getInt("code") != 400) {
                                JSONObject responseObject = response.getJSONObject("response");
                                    JSONArray itemsArray = responseObject.getJSONArray("groups").getJSONObject(0).getJSONArray("items");
                                    for(int i=0; i<itemsArray.length(); ++i) {
                                        PlacesModel placesModel = new PlacesModel();
                                        JSONObject itemsObject = itemsArray.getJSONObject(i);
                                        placesModel.setId(itemsObject.getJSONObject("venue").getString("id"));
                                        placesModel.setName(itemsObject.getJSONObject("venue").getString("name"));
                                        placesModel.setLat(itemsObject.getJSONObject("venue").getJSONObject("location").getString("lat"));
                                        placesModel.setLongi(itemsObject.getJSONObject("venue").getJSONObject("location").getString("lng"));
                                        JSONArray locationArray = itemsObject.getJSONObject("venue").getJSONObject("location").getJSONArray("formattedAddress");
                                        for(int j=0; j<locationArray.length(); ++j)
                                            placesModel.setAdd1(placesModel.getAdd1()+locationArray.getString(j)+"\n");
                                        if(!itemsObject.getJSONObject("venue").isNull("price")) {
                                            String price = itemsObject.getJSONObject("venue").getJSONObject("price").getString("message");
                                            placesModel.setPrice(price);
                                        }
                                        else
                                            placesModel.setPrice("null");

                                        if(!itemsObject.getJSONObject("venue").isNull("rating")) {
                                            placesModel.setRating(itemsObject.getJSONObject("venue").getString("rating"));
                                            placesModel.setRatingColor(itemsObject.getJSONObject("venue").getString("ratingColor"));
                                        }
                                        else
                                            placesModel.setRating("0");
                                        placesModels.add(placesModel);
                                        placesAdapter.notifyDataSetChanged();
                                    }
                                    Toast.makeText(PlacesActivity.this, "Total Items: "+placesModels.size(), Toast.LENGTH_LONG).show();
                                    progressDialog.dismiss();
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

                    }
                });

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_plan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(PlacesActivity.this, JourneyPlan.class);
        intent.putExtra("BUNDLE", placesModels);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }
}


package com.application.recommend.recommendplaces;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JourneyPlan extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Location> locations = new ArrayList<>();
    ArrayList<PlacesModel> placesModels;
    ProgressDialog progressDialog;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_plan);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestQueue = Volley.newRequestQueue(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please Wait..");
        progressDialog.show();

        placesModels = (ArrayList<PlacesModel>) getIntent().getSerializableExtra("BUNDLE");
        for(int i=0; i<placesModels.size(); ++i) {
            Location location = new Location(LocationManager.NETWORK_PROVIDER);
            location.setLatitude(Double.parseDouble(placesModels.get(i).getLat()));
            location.setLongitude(Double.parseDouble(placesModels.get(i).getLongi()));
            locations.add(location);
        }
        drawPrimaryLinePath(locations);
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

        mMap.setOnMapLoadedCallback(
                new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        drawPrimaryLinePath(locations);
                        progressDialog.dismiss();
                    }
                }
        );
        mMap.setOnPolylineClickListener(
                new GoogleMap.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(Polyline polyline) {
                        List<LatLng> latLngs = polyline.getPoints();
                        Log.d("muy", latLngs.size()+"");
                        for(LatLng latLng : latLngs)
                            Log.d("muy", latLng.toString());
                        getDistanceDetails(polyline.getPoints());
                    }
                }
        );
    }
    //final List<String> distanceDetails = new ArrayList<>();
    private void getDistanceDetails(final List<LatLng> latLngs) {
        String origin = latLngs.get(0).latitude+","+latLngs.get(0).longitude;
        String dest = latLngs.get(1).latitude+","+latLngs.get(1).longitude;
        String urlDriving = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + origin + "&destinations=" + dest + "&mode=driving&key=AIzaSyDKPWqssYRY-vBHAvH3WIvDJSlUDIwScRc";
        Log.d("muy", urlDriving);
        final AlertDialog.Builder alert = new AlertDialog.Builder(JourneyPlan.this);
        JsonObjectRequest objectRequestDriving =
                new JsonObjectRequest(Request.Method.GET, urlDriving, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("muy", "onResponse() called");
                            JSONArray jsonArray = response.getJSONArray("rows");
                            String status = jsonArray.getJSONObject(0).getJSONArray("elements").getJSONObject(0).get("status").toString();
                            if (TextUtils.equals(status, "ZERO_RESULTS")) {
                                Log.d("muy", "no data");
                                alert.setTitle("Error");
                                alert.setMessage("No Data!!");
                                alert.show();
      //                          distanceDetails.add("No data");
                                return;
                            }
                            JSONObject object = jsonArray.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance");
                            String distance = object.getString("text");
                            JSONObject object1 = jsonArray.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration");
                            String duration = object1.getString("text");
                            /*distanceDetails.add(distance);
                            distanceDetails.add(duration);*/
                            Log.d("muy", distance);
                            Log.d("muy", duration);
                            /*double lat = (latLngs.get(0).latitude+latLngs.get(1).latitude)/2;
                            double lng = (latLngs.get(0).longitude+latLngs.get(1).longitude)/2;
                            mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(distance).snippet(duration));*/
                            alert.setTitle("Distance: "+distance);
                            alert.setMessage("Duration: "+duration);
                            alert.show();
                        } catch (JSONException e) {
                            Log.d("muy", "catch");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("muy", "error");
                        error.printStackTrace();
                        Toast.makeText(JourneyPlan.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(objectRequestDriving);
        //Log.d("muy",distanceDetails.size()+" size");
        /*Log.d("muy",distanceDetails.get(0));
        Log.d("muy",distanceDetails.get(1));*/
    }


    private void drawPrimaryLinePath( ArrayList<Location> listLocsToDraw )
    {
        if ( mMap == null )
            return;

        if ( listLocsToDraw.size() < 2 )
            return;

        listLocsToDraw.add(listLocsToDraw.get(0));
        //options.clickable(true);
        LatLng latLng;
        PolylineOptions options = new PolylineOptions();
        List<LatLng> latLngs = new ArrayList<>();

        for (int i=0; i<listLocsToDraw.size(); ++i) {

            if(i%2 == 0 || i>1) {
                options = new PolylineOptions();
                //Log.d("muy", options.getPoints().size()+"");
                options.color(Color.RED);
                options.width( 20 );
                options.visible( true );
            }

            if(i>1)
                options.add(latLngs.get(latLngs.size()-1));

            //options.color( Color.parseColor( "#CC0000FF" ) );
            Location locRecorded = listLocsToDraw.get(i);
            latLng = new LatLng(locRecorded.getLatitude(),locRecorded.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(placesModels.get(i%placesModels.size()).getName()).snippet((i+1)+""));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            options.add(new LatLng(locRecorded.getLatitude(), locRecorded.getLongitude()));
            latLngs.add(new LatLng(locRecorded.getLatitude(), locRecorded.getLongitude()));
            Log.d("muy", options.getPoints().size()+""+", i: "+i);
            if(i%2 != 0 || i>1) {
                Polyline polyline = mMap.addPolyline(options);
                polyline.setClickable(true);
                Log.d("muy", "i: "+i);
                /*Log.d("muy", "Options Size: "+options.getPoints().size());
                Log.d("muy", "options points: "+options.getPoints().toString());*/
                Log.d("muy", "polyline Size: "+polyline.getPoints().size());
                Log.d("muy", "drawing line"+polyline.getPoints().toString());
            }
        }
    }
}


package com.application.recommend.recommendplaces;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Akarsh on 11-01-2018.
 */

public class PlacesAdapter extends RecyclerView.Adapter<PlacesAdapter.Items> implements Serializable {

    private Context context;
    private ArrayList<PlacesModel> placesModels = new ArrayList<>();

    public PlacesAdapter(Context context, ArrayList<PlacesModel> placesModels) {
        this.context = context;
        this.placesModels = placesModels;
    }

    @Override
    public PlacesAdapter.Items onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.places_item, parent, false);
        return new Items(view);
    }


    @Override
    public void onBindViewHolder(final PlacesAdapter.Items holder, int position) {
        holder.tvName.setText(placesModels.get(position).getName());
        holder.tvAdd1.setText(placesModels.get(position).getAdd1());
        if(placesModels.get(position).getRating().equals("0"))
            holder.ratingBar.setVisibility(View.GONE);
        else
            holder.ratingBar.setRating(Float.parseFloat(placesModels.get(position).getRating())/2);
        holder.myView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, PlaceDetails.class);
                        intent.putExtra("BUNDLE", placesModels);
                        intent.putExtra("position", holder.getAdapterPosition());
                        context.startActivity(intent);
                    }
                }
        );
    }

    public ArrayList<PlacesModel> getPlacesModels() {
        return placesModels;
    }

    @Override
    public int getItemCount() {
        return placesModels.size();
    }

    public class Items extends RecyclerView.ViewHolder implements Serializable {

        TextView tvName, tvAdd1;
        RatingBar ratingBar;
        View myView;

        public Items(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvAdd1 = itemView.findViewById(R.id.tvAdd1);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            myView = itemView.findViewById(R.id.myView);

        }
    }
}


package com.application.recommend.recommendplaces;

/**
 * Created by Akarsh on 16-01-2018.
 */

public class ImagesModel {

    String prefix;
    String suffix;
    String width;
    String height;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }
}


package com.application.recommend.recommendplaces;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OTP extends AppCompatActivity implements View.OnClickListener {

    EditText t1;
    Button b1;
    String otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        otp=getIntent().getExtras().getString("otp");
        t1=findViewById(R.id.eotp);
        t1.setText(otp);
        b1=findViewById(R.id.bok);
        b1.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(otp.contentEquals(t1.getText().toString())){
            {
                Toast.makeText(this, "You are a valid user!!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,MainActivity.class));
            }
        }
        else
        {  Toast.makeText(this, "Wrong OTP!!!", Toast.LENGTH_SHORT).show();
           t1.setText("");

    }}
}


package com.application.recommend.recommendplaces;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

//import com.basgeekball.awesomevalidation.AwesomeValidation;
//import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class Signup extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    EditText t1,t2,t3,t4,t5,totp;
    Button b1;
    int i;
    String email,pass;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    FirebaseUser user;
    SmsManager smsManager;
     PendingIntent sendpi,delpi;
    public static final String SEND_SMS="send_sms";
   public static final String DEL_SMS="del_sms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email=getIntent().getExtras().getString("username");
        pass=getIntent().getExtras().getString("password");
        t1=findViewById(R.id.etName);
        t2=findViewById(R.id.etEmail);
        t3=findViewById(R.id.etDob);
        t4=findViewById(R.id.etPassword);
        t5=findViewById(R.id.etPhone);
        t2.setText(email);
        t4.setText(pass);
        b1=findViewById(R.id.bRegister);
        b1.setOnClickListener(this);
        auth=FirebaseAuth.getInstance();
        smsManager=SmsManager.getDefault();
        t3.setOnClickListener(this);
        t3.setOnFocusChangeListener(this);

        databaseReference= FirebaseDatabase.getInstance().getReference().child("Records");

        sendpi=PendingIntent.getBroadcast(this,0,new Intent(SEND_SMS),0);
       delpi=PendingIntent.getBroadcast(this,0,new Intent(DEL_SMS),0);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        DatePickerDialog dialog;
        if(b == true){
            Calendar c = Calendar.getInstance();

            int y = c.get(Calendar.YEAR);
            int mo = c.get(Calendar.MONTH);
            int d = c.get(Calendar.DAY_OF_MONTH);
            dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    t3.setText( dayOfMonth + " / " + (monthOfYear + 1) + " / " + year);

                }
            }, y, mo, d);
            dialog.show();
        }
    }

    class MySend extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (this.getResultCode())
            {
                case RESULT_OK:
                    Toast.makeText(context, "Otp Sent to ur Mobile!!!", Toast.LENGTH_SHORT).show();
                   /* intent=new Intent(Signup.this,OTP.class);
                    intent.putExtra("otp",String.valueOf(i));
                    startActivity(intent);*/


                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No Service available!!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class MyDel extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (this.getResultCode())
            {
                case RESULT_OK:
                    Toast.makeText(context, "Otp Delivered to ur Mobile!!!", Toast.LENGTH_SHORT).show();
                    //totp.setText(i);
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No Service available!!!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.registerReceiver(new MySend(),new IntentFilter(SEND_SMS));
        this.registerReceiver(new MyDel(),new IntentFilter(DEL_SMS));
    }

   @Override
   protected void onDestroy() {


       super.onDestroy();
       finish();

    }

    @Override
    public void onClick(View view) {
        String dob=t3.getText().toString();
        String [] dateParts = dob.split("/");
        String year = dateParts[2];
        year=year.replace(" ","");
        String name=t1.getText().toString();

        Calendar c=Calendar.getInstance();
        int y = c.get(Calendar.YEAR);
        String phone=t5.getText().toString();
        if(view==b1) {
            if(TextUtils.isEmpty(t1.getText().toString()) ||TextUtils.isEmpty(t2.getText().toString())||TextUtils.isEmpty(t3.getText().toString())||TextUtils.isEmpty(t4.getText().toString())||TextUtils.isEmpty(t5.getText().toString()))
            {
                Toast.makeText(this, "Fields cannot be left empty!!!!", Toast.LENGTH_SHORT).show();
            }
           else  if(Integer.parseInt(year)>y){
                Toast.makeText(this, "Enter a valid date!!!", Toast.LENGTH_SHORT).show();
                t3.requestFocus();
            }
           else  if(phone.length()<10)
                Toast.makeText(this, "Enter a phone number of 10 digits!!!", Toast.LENGTH_SHORT).show();
            else
            {
            Random r = new Random();
            i = r.nextInt(99999) + 100000;
            String str = Integer.toString(i);
            register();
            showOtpDialog();

            smsManager.sendTextMessage(t5.getText().toString(), null, str, sendpi, delpi);
        }}

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,
                MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void register()

    {

        //dialog.setMessage("Registering Please");
        final String key=databaseReference.push().getKey();
        String name=t1.getText().toString();
        String dob=t3.getText().toString();
        String phone=t5.getText().toString();
        final MyInfo myInfo=new MyInfo(name,email,dob,pass,phone,String.valueOf(i));
        databaseReference.child(key).setValue(myInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(Signup.this, "Data Saved Succesfully!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signup.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOtpDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater alertLayout = this.getLayoutInflater();
        final View dialogView = alertLayout.inflate(R.layout.activity_otp, null);
        builder.setView(dialogView);
        totp = dialogView.findViewById(R.id.eotp);
        final Button b1 =  dialogView.findViewById(R.id.bok);
        builder.setTitle("OTP!!!");
        //totp.setText(""+i);
        final AlertDialog dialog = builder.create();
        dialog.show();

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if(String.valueOf(i).contentEquals(totp.getText().toString())){

                Toast.makeText(Signup.this, "You are a valid user!!!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Signup.this,MainActivity.class));
            }

        else
        {  Toast.makeText(Signup.this, "Wrong OTP!!!", Toast.LENGTH_SHORT).show();
           t1.setText("");

    }
        }});

    }

}



package com.application.recommend.recommendplaces;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity implements View.OnClickListener {
    EditText etuser, etpass;
    Button bsignin;
    String user, pass;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    ProgressDialog dialog;
    ImageView imageButton;
    boolean hidden=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        etuser = findViewById(R.id.etuser);
        etpass = findViewById(R.id.etpass);
        bsignin = findViewById(R.id.blogin);
        imageButton=findViewById(R.id.imageButton4);
       // user = getIntent().getExtras().getString("username");
        //pass = getIntent().getExtras().getString("password");
       // etuser.setText(user);
        //etpass.setText(pass);
        dialog=new ProgressDialog(SignIn.this);
        auth=FirebaseAuth.getInstance();
        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        bsignin.setOnClickListener(this);
        imageButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view ==bsignin) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to become an authenticated user?");
            builder.setTitle("Message!!!");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    checkUser();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    startActivity(new Intent(SignIn.this, MainActivity.class));
                }
            });
            builder.show();

        }

        if (view == imageButton) {
            if (hidden) {
                etpass.setTransformationMethod(null);
                imageButton.setImageResource(R.mipmap.green);
                hidden=false;
            }
            else {
                etpass.setTransformationMethod(new PasswordTransformationMethod());
                imageButton.setImageResource(R.mipmap.red);
                hidden=true;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);

        //startActivity(new Intent(this,MainActivity.class));
        finish();
    }


    private void checkUser() {

            dialog.setMessage("Logging...PLease Wait");
            dialog.show();
            String email=etuser.getText().toString();
            String pas=etpass.getText().toString();
            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pas) )
                Toast.makeText(this, "All fields are Mandatory!!!", Toast.LENGTH_SHORT).show();
            else {
                auth.signInWithEmailAndPassword(email, pas).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            Toast.makeText(SignIn.this, "Successfully Login!!!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignIn.this, Info.class));
                            etuser.setText("");
                            etpass.setText("");
                        }

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(SignIn.this, "OOps...FAiled!!!", Toast.LENGTH_SHORT).show();
                        etuser.setText("");
                        etpass.setText("");
                    }
                });
            }
    }
}


package com.application.recommend.recommendplaces;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}


