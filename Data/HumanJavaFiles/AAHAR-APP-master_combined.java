package com.example.aahaarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;


public class About extends AppCompatActivity {

    CardView instagram,facebook,twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        instagram = findViewById(R.id.instagram);
        facebook = findViewById(R.id.facebook);
        twitter = findViewById(R.id.twitter);

        instagram.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://www.instagram.com"));
                startActivity(myWebLink);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://www.facebook.com"));
                startActivity(myWebLink);
            }
        });
        twitter.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("http://www.twitter.com"));
                startActivity(myWebLink);
            }
        });
    }

}

package com.example.aahaarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Contact extends AppCompatActivity {
    EditText name, email, message;
    Button submit;
    boolean isNameValid, isEmailValid, isMessageValid;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    public static final String TAG = "TAG";
    TextInputLayout nameError, emailError, messageError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        message = (EditText) findViewById(R.id.message);
        submit = (Button) findViewById(R.id.submit);
        nameError = (TextInputLayout) findViewById(R.id.nameError);
        emailError = (TextInputLayout) findViewById(R.id.emailError);
        messageError = (TextInputLayout) findViewById(R.id.messageError);

        fAuth=FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetValidation();
            }
        });
    }
        public void SetValidation() {

            // Check for a valid name.
            if (name.getText().toString().isEmpty()) {
                nameError.setError(getResources().getString(R.string.name_error));
                isNameValid = false;
            } else  {
                isNameValid = true;
                nameError.setErrorEnabled(false);
            }

            // Check for a valid email address.
            if (email.getText().toString().isEmpty()) {
                emailError.setError(getResources().getString(R.string.email_error));
                isEmailValid = false;
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                emailError.setError(getResources().getString(R.string.error_invalid_email));
                isEmailValid = false;
            } else  {
                isEmailValid = true;
                emailError.setErrorEnabled(false);
            }

            // Check for a valid phone number.
            if (message.getText().toString().isEmpty()) {
                messageError.setError(getResources().getString(R.string.phone_error));
                isMessageValid = false;
            } else  {
                isMessageValid = true;
                messageError.setErrorEnabled(false);
            }

            if (isNameValid && isEmailValid && isMessageValid ) {

                String Name = name.getText().toString().trim();
                String Email= email.getText().toString().trim();
                String Message= message.getText().toString().trim();
                userID = fAuth.getCurrentUser().getUid();
                //DocumentReference documentReference = fStore.collection("donate").document(userID);
                CollectionReference collectionReference = fStore.collection("contact data");

                Map<String,Object> user = new HashMap<>();
                user.put("timestamp", FieldValue.serverTimestamp());
                user.put("name",Name);
                user.put("email",Email);
                user.put("message",Message);
                user.put("userid",userID);

                collectionReference.add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"Successfully! We will shortly revert you back.");
                                //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                Intent intent = new Intent(Contact.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error!", e);
                            }
                        });
            }

        }
    }


package com.example.aahaarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    CardView donate, receive, logout, foodmap, about, contact, mypin, history;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        donate = findViewById(R.id.cardDonate);
        receive = findViewById(R.id.cardReceive);
        logout = findViewById(R.id.cardLogout);
        foodmap = findViewById(R.id.cardFoodmap);
        mypin = findViewById(R.id.cardMyPin);
        history = findViewById(R.id.cardHistory);
        about = findViewById(R.id.cardAboutus);
        contact = findViewById(R.id.cardContact);

        fAuth= FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() ==null){
            Intent intent = new Intent(MainActivity.this, landingpage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        donate.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Donate.class);
                startActivity(intent);
            }
        });
        receive.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Receive.class);
                startActivity(intent);
            }
        });
        foodmap.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FoodMap.class);
                startActivity(intent);
            }
        });
        about.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), About.class);
                startActivity(intent);
            }
        });
        mypin.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyPin.class);
                startActivity(intent);
            }
        });
        history.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserdataActivity.class);
                startActivity(intent);
            }
        });
        contact.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Contact.class);
                startActivity(intent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, landingpage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}

package com.example.aahaarapp;


import android.content.Intent;
import android.text.TextUtils;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Logup extends AppCompatActivity {

    EditText mEmail,mPassword;
    Button mLoginBtn;
    TextView mRegisterBtn;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logup);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mRegisterBtn = findViewById(R.id.register);
        mLoginBtn = findViewById(R.id.login);

        fAuth=FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() !=null){
            Intent intent = new Intent(Logup.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password= mPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6)
                {
                    mPassword.setError("Password Must be >=6 Characters");
                    return;
                }

                //authenticate the user
                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Logup.this, "Logged in Successfully.", Toast.LENGTH_SHORT) .show();
                            Intent intent = new Intent(Logup.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            Toast.makeText(Logup.this, "Error! " + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // redirect to RegisterActivity
                Intent intent = new Intent(getApplicationContext(), Signup.class);
                startActivity(intent);
            }
        });
    }

}



package com.example.aahaarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserdataActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<model> datalist;
    FirebaseFirestore db;
    myadapter adapter;
    FirebaseAuth fAuth= FirebaseAuth.getInstance();
    public String userID = fAuth.getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userdata);

        recyclerView=(RecyclerView)findViewById(R.id.rec_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        datalist=new ArrayList<>();
        adapter=new myadapter(datalist);
        recyclerView.setAdapter(adapter);

        db=FirebaseFirestore.getInstance();
        db.collection("user data").orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list=queryDocumentSnapshots.getDocuments();
                        for(DocumentSnapshot d:list)
                        {
                            model obj=d.toObject(model.class);
                            //datalist.add(obj);
                            String Userid = (String) d.get("userid");
                            if(Userid.equals(userID)) {
                                datalist.add(obj);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

package com.example.aahaarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static android.util.Base64.CRLF;

public class FoodMap extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFragment;
    private int REQUEST_CODE = 11;
    FirebaseFirestore fStore;
    public static final String TAG = "TAG";
    private FirebaseFirestore cloudstorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        showLocation();
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        MarkerOptions markerOptions1 = new MarkerOptions().position(latLng).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mMap.addMarker(markerOptions1).showInfoWindow();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(markerOptions1).showInfoWindow();
    }

    public void showLocation() {
        this.cloudstorage = FirebaseFirestore.getInstance();
        cloudstorage.collection("user data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());//
                                if (document.contains("location") && document.contains("name") && document.contains("description")) {
                                    GeoPoint location = (GeoPoint) document.get("location");
                                    String title = (String) document.get("name");
                                    String type = (String) document.get("type");
                                    String description = (String) document.get("description");

                                    if(type.equals("Donor")) {
                                        Log.d(TAG, String.valueOf(location) + " Success " + title);
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        mMap.addMarker(new MarkerOptions().position(latLng).title(title+"("+type+")").snippet(description).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    }
                                    else if(type.equals("Receiver")){
                                        Log.d(TAG, String.valueOf(location) + " Success " + title);
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        mMap.addMarker(new MarkerOptions().position(latLng).title(title+"("+type+")").snippet(description).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error fetching data: ", task.getException());
                        }
                    }
                });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                mapFragment.getMapAsync(this);
            }else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.example.aahaarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class History extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookref = db.collection("user data");
    public static final String TAG = "TAG";
    private TextView textViewData;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        fAuth= FirebaseAuth.getInstance();
        textViewData=findViewById(R.id.data);

        loadNotes();
    }

    public void loadNotes() {
        notebookref.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String data="";
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());//
                                if (document.contains("name") && document.contains("description") && document.contains("user type") && document.contains("userid")) {

                                    String name = (String) document.get("name");
                                    String type = (String) document.get("user type");
                                    String description = (String) document.get("description");
                                    String Userid = (String) document.get("userid");
                                    String userID = fAuth.getCurrentUser().getUid();
                                    Timestamp ts = (Timestamp) document.get("timestamp");
                                    //String dateandtime=String.valueOf(ts);
                                    String dateandtime=String.valueOf(ts.toDate());
                                    //String dateandtime = ts.toString();

                                    if(Userid.equals(userID)) {
                                        data += "Name: " + name + "\nUser Type: " + type + "\nDescription: " + description + "\nDate & Time: " + dateandtime + "\n\n";
                                        //data += "Name: " + name + "\nUser Type: " + type + "\nDescription: " + description + "\n";
                                    }
                                    textViewData.setText(data);
                                }
                            }
                            //textViewData.setText(data);
                        } else {
                            Log.d(TAG, "Error fetching data: ", task.getException());
                        }
                    }
                });
    }
}

package com.example.aahaarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MyPin extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,  com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFragment;
    private int REQUEST_CODE = 11;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    public static final String TAG = "TAG";
    private FirebaseFirestore cloudstorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pin);
        fAuth= FirebaseAuth.getInstance();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        showLocation();
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        MarkerOptions markerOptions1 = new MarkerOptions().position(latLng).title("You are here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mMap.addMarker(markerOptions1).showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(markerOptions1).showInfoWindow();
    }

    public void showLocation() {
        this.cloudstorage = FirebaseFirestore.getInstance();
        cloudstorage.collection("user data")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());//
                                if (document.contains("location") && document.contains("name") && document.contains("description") && document.contains("userid")) {
                                    GeoPoint location = (GeoPoint) document.get("location");
                                    String title = (String) document.get("name");
                                    String type = (String) document.get("type");
                                    String description = (String) document.get("description");
                                    String Userid = (String) document.get("userid");
                                    String userID = fAuth.getCurrentUser().getUid();

                                    if(type.equals("Donor") & Userid.equals(userID)) {
                                        Log.d(TAG, userID + " Success " + title);
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        mMap.addMarker(new MarkerOptions().position(latLng).title(title+"("+type+")").snippet(description).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    }
                                    else if(type.equals("Receiver") & Userid.equals(userID)){
                                        Log.d(TAG, String.valueOf(location) + " Success " + title);
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        mMap.addMarker(new MarkerOptions().position(latLng).title(title+"("+type+")").snippet(description).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error fetching data: ", task.getException());
                        }
                    }
                });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                mapFragment.getMapAsync(this);
            }else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

package com.example.aahaarapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class landingpage extends AppCompatActivity {

    CardView login,register,about;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landingpage);

        login = findViewById(R.id.cardLogin);
        register = findViewById(R.id.cardRegister);
        about = findViewById(R.id.cardAboutus);

        fAuth= FirebaseAuth.getInstance();
        if(fAuth.getCurrentUser() !=null){
            Intent intent = new Intent(landingpage.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        login.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Logup.class));
            }
        });
        register.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Signup.class));
            }
        });
        about.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), About.class));
            }
        });
    }

}

package com.example.aahaarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.HashMap;
import java.util.Map;

public class Receive extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private int REQUEST_CODE = 11;
    SupportMapFragment mapFragment;
    EditText mFullName,mDescription;
    Button mSubmitBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        mFullName = findViewById(R.id.receivername);
        mDescription = findViewById(R.id.description);
        mSubmitBtn=findViewById(R.id.submit);

        fAuth=FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        //MarkerOptions markerOptions1 = new MarkerOptions().position(latLng).title("You are here");
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mMap.addMarker(markerOptions1).showInfoWindow();

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(markerOptions).showInfoWindow();

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = mFullName.getText().toString().trim();
                String description= mDescription.getText().toString().trim();
                String type= "Receiver";

                if(TextUtils.isEmpty(fullname))
                {
                    mFullName.setError("Name is Required.");
                    return;
                }
                if(TextUtils.isEmpty(description))
                {
                    mFullName.setError("Description is Required.");
                    return;
                }


                userID = fAuth.getCurrentUser().getUid();
                //DocumentReference documentReference = fStore.collection("receiver").document(userID);
                CollectionReference collectionReference = fStore.collection("user data");

                GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
                Map<String,Object> user = new HashMap<>();
                user.put("timestamp", FieldValue.serverTimestamp());
                user.put("name",fullname);
                user.put("description",description);
                user.put("location",geoPoint);
                user.put("userid",userID);
                user.put("type",type);

                collectionReference.add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"Success!");
                                //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                Intent intent = new Intent(Receive.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error!", e);
                            }
                        });

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(1000);
        //mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                mapFragment.getMapAsync(this);
            }else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


package com.example.aahaarapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class myadapter extends RecyclerView.Adapter<myadapter.myviewholder>
{
    ArrayList<model> datalist;
    FirebaseAuth fAuth= FirebaseAuth.getInstance();
    public String userID = fAuth.getCurrentUser().getUid();
    public String uid;


    public myadapter(ArrayList<model> datalist) {
        this.datalist = datalist;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder (@NonNull ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singlerow, parent, false);
        return new myviewholder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull myviewholder holder, int position) {
        holder.tname.setText(datalist.get(position).getName());
        holder.ttype.setText(datalist.get(position).getType());
        holder.tdescription.setText(datalist.get(position).getDescription());
    }

    public void deleteItem(int position){
        //getSnapshots().getSnapshot(position).getReference().delete();
        //notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    class myviewholder extends RecyclerView.ViewHolder
    {
        TextView tname,ttype,tdescription;
        public myviewholder(@NonNull View itemView) {
            super(itemView);
            tname = itemView.findViewById(R.id.name);
            ttype = itemView.findViewById(R.id.type);
            tdescription = itemView.findViewById(R.id.description);
        }
    }
}


package com.example.aahaarapp;

public class model {
    String name,type,description,userid;

    public model() {

    }

    public model(String name, String type, String description, String userid) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


package com.example.aahaarapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.HashMap;
import java.util.Map;

public class Donate extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private int REQUEST_CODE = 11;
    SupportMapFragment mapFragment;
    EditText mFullName,mFoodItem,mDescription,mPhone;
    Button mSubmitBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;
    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        mFullName = findViewById(R.id.donorname);
        mFoodItem = findViewById(R.id.fooditem);
        mPhone = findViewById(R.id.phone);
        mDescription = findViewById(R.id.description);
        mSubmitBtn=findViewById(R.id.submit);

        fAuth=FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        //MarkerOptions markerOptions1 = new MarkerOptions().position(latLng).title("You are here");
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        //mMap.addMarker(markerOptions1).showInfoWindow();

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are here");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        mMap.addMarker(markerOptions).showInfoWindow();


        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullname = mFullName.getText().toString().trim();
                String fooditem= mFoodItem.getText().toString().trim();
                String description= mDescription.getText().toString().trim();
                String phone= mPhone.getText().toString().trim();
                String type= "Donor";

                if(TextUtils.isEmpty(fullname))
                {
                    mFullName.setError("Name is Required.");
                    return;
                }

                if(TextUtils.isEmpty(fooditem))
                {
                    mFoodItem.setError("Required.");
                    return;
                }

                if(phone.length() < 10)
                {
                    mPhone.setError("Phone Number Must be >=10 Characters");
                    return;
                }

                userID = fAuth.getCurrentUser().getUid();
                //DocumentReference documentReference = fStore.collection("donate").document(userID);
                CollectionReference collectionReference = fStore.collection("user data");

                GeoPoint geoPoint = new GeoPoint(location.getLatitude(),location.getLongitude());
                Map<String,Object> user = new HashMap<>();
                user.put("timestamp", FieldValue.serverTimestamp());
                user.put("name",fullname);
                user.put("food item",fooditem);
                user.put("phone",phone);
                user.put("description",description);
                user.put("location",geoPoint);
                user.put("userid",userID);
                user.put("type",type);

                collectionReference.add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"Success!");
                                //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                Intent intent = new Intent(Donate.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(),"Error!",Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error!", e);
                            }
                        });
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0]  == PackageManager.PERMISSION_GRANTED){
                mapFragment.getMapAsync(this);
            }else{
                Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


package com.example.aahaarapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText mFullName,mEmail,mPassword,mPhone;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mFullName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPhone = findViewById(R.id.phone);
        mRegisterBtn=findViewById(R.id.register);
        mLoginBtn = findViewById(R.id.login);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser() !=null){
            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
            //finish();
            Intent intent = new Intent(Signup.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        mRegisterBtn.setOnClickListener(new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                String email = mEmail.getText().toString().trim();
                String password= mPassword.getText().toString().trim();
                String name= mFullName.getText().toString().trim();
                String phone= mPhone.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    mEmail.setError("Email is Required.");
                    return;
                }

                if(TextUtils.isEmpty(password))
                {
                    mPassword.setError("Password is Required.");
                    return;
                }

                if(password.length() < 6)
                {
                    mPassword.setError("Password Must be >=6 Characters");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Signup.this, "User Created.", Toast.LENGTH_SHORT) .show();
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("name",name);
                            user.put("email",email);
                            user.put("phone",phone);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"onSuccess: user Profile is created for "+ userID);
                                    Toast.makeText(Signup.this, "Registered Successfully.", Toast.LENGTH_SHORT) .show();
                                }
                            });
                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            Intent intent = new Intent(Signup.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        else{
                            Toast.makeText(Signup.this, "Error!" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Logup.class));
            }
        });
    }
}

package com.example.aahaarapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this,landingpage.class));
        finish();
    }
}

