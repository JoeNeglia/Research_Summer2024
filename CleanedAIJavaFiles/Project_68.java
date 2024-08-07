package com.example.heritagehunt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HeritageHunt extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleMap mMap;
    private Button startHuntButton;
    private TextView clueTextView;
    private ImageView arImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        startHuntButton = findViewById(R.id.start_hunt_button);
        clueTextView = findViewById(R.id.clue_text_view);
        arImageView = findViewById(R.id.ar_image_view);

        // Check if the user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Not signed in, launch the sign-in UI
            startLoginActivity();
        } else {
            // User is already signed in, set up the map
            setUpMap();
        }

        startHuntButton.setOnClickListener(v -> startScavengerHunt());

        arImageView.setOnClickListener(v -> launchAR());
    }

    private void startLoginActivity() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
                // Add more providers if needed
        );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // Set up the map
                setUpMap();
            } else {
                // Sign in failed
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setUpMap() {
        // Request location permission, so that we can get the location of the device.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Location permission granted
            initMap();
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    initMap();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Permission denied to access location.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                // Add markers for each landmark from Firestore
                CollectionReference landmarks = db.collection("landmarks");
                landmarks.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> landmark = document.getData();
                            LatLng location = new LatLng((double) landmark.get("latitude"), (double) landmark.get("longitude"));
                            String title = (String) landmark.get("name");
                            mMap.addMarker(new MarkerOptions().position(location).title(title));
                        }
                    } else {
                        Toast.makeText(HeritageHunt.this, "Error getting landmarks.", Toast.LENGTH_SHORT).show();
                    }
                });

                // Center map on a specific location (e.g., city center)
                LatLng cityCenter = new LatLng(37.7749, -122.4194); // Example coordinates (San Francisco)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cityCenter, 12));
            }
        });
    }

    private void startScavengerHunt() {
        // Example starting point
        // In a real app, this could be based on user selections or randomized
        LatLng startPoint = new LatLng(37.7749, -122.4194); // Example coordinates (San Francisco)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 14));
        clueTextView.setText("Find the landmark at these coordinates: 37.7749,-122.4194");
    }

    private void launchAR() {
        // Example AR activity launch
        Intent intent = new Intent(this, ARActivity.class);
        startActivity(intent);
    }
}