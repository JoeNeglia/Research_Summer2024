package com.example.saferoute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private TextView txtLocation;
    private Button btnSOS, btnIncidentReport, btnPanicMode;
    private ImageView imgMicrophone;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_VOICE_COMMAND = 2;
    private static final String TWILIO_ACCOUNT_SID = "your_twilio_account_sid";
    private static final String TWILIO_AUTH_TOKEN = "your_twilio_auth_token";
    private static final String TWILIO_PHONE_NUMBER = "your_twilio_phone_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Location Client
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Reference UI components
        txtLocation = findViewById(R.id.txtLocation);
        btnSOS = findViewById(R.id.btnSOS);
        btnIncidentReport = findViewById(R.id.btnIncidentReport);
        btnPanicMode = findViewById(R.id.btnPanicMode);
        imgMicrophone = findViewById(R.id.imgMicrophone);

        // Check permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // SOS Button OnClick Listener
        btnSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSOSAlert();
            }
        });

        // Incident Report Button OnClick Listener
        btnIncidentReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportIncident();
            }
        });

        // Panic Mode Button OnClick Listener
        btnPanicMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activatePanicMode();
            }
        });

        // Voice Command Activation
        imgMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentLocation).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                            txtLocation.setText("Lat: " + location.getLatitude() + " , Lng: " + location.getLongitude());
                        }
                    }
                });
    }

    private void sendSOSAlert() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("emergency_contacts").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ArrayList<String> contacts = (ArrayList<String>) documentSnapshot.get("contacts");

                            // Get current location
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mFusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(this, location -> {
                                        if (location != null && contacts != null) {
                                            String message = "SOS Alert! Location: http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                                            Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
                                            for (String contact : contacts) {
                                                Message.creator(new PhoneNumber(contact),
                                                        new PhoneNumber(TWILIO_PHONE_NUMBER),
                                                        message).create();
                                            }
                                            Toast.makeText(this, "SOS alerts sent!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Failed to get location or contacts.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "No emergency contacts found.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void reportIncident() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Map<String, Object> incident = new HashMap<>();
            incident.put("userId", currentUser.getUid());
            incident.put("timestamp", System.currentTimeMillis());
            // You can add more details like description, photos, etc.

            db.collection("incidents").add(incident)
                    .addOnSuccessListener(documentReference -> Toast.makeText(this, "Incident reported!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to report incident.", Toast.LENGTH_SHORT).show());
        }
    }

    // Panic Mode sends location updates discreetly
    private void activatePanicMode() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("emergency_contacts").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            ArrayList<String> contacts = (ArrayList<String>) documentSnapshot.get("contacts");

                            // Get current location
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mFusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(this, location -> {
                                        if (location != null && contacts != null) {
                                            String message = "Panic Mode Activated. Location: http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
                                            Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
                                            for (String contact : contacts) {
                                                Message.creator(new PhoneNumber(contact),
                                                        new PhoneNumber(TWILIO_PHONE_NUMBER),
                                                        message).create();
                                            }
                                            Toast.makeText(this, "Panic mode alerts sent!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Failed to get location or contacts.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "No emergency contacts found.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Voice command initiation
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_VOICE_COMMAND);
        } else {
            Toast.makeText(this, "Your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VOICE_COMMAND && resultCode == RESULT_OK) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            handleVoiceCommand(result.get(0));
        }
    }

    // Process voice commands
    private void handleVoiceCommand(String command) {
        switch (command.toLowerCase()) {
            case "send sos":
                sendSOSAlert();
                break;
            case "activate panic mode":
                activatePanicMode();
                break;
            // Add more voice commands as needed
            default:
                Toast.makeText(this, "Unknown command: " + command, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}