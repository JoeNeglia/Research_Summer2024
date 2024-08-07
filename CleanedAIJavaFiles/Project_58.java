// Import required packages
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

// Main Activity
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Google Sign-in options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not signed in, initiate sign-in flow
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.FacebookBuilder().build());

            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        } else {
            updateUI(currentUser);
        }

        // Set up button interactions
        Button signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            TextView welcomeText = findViewById(R.id.welcomeText);
            welcomeText.setText("Welcome " + user.getDisplayName());
            // Further UI updates and data fetching from Firebase can be done here
        } else {
            // Update the UI to show the sign-in button etc.
        }
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> {
                    // User is now signed out
                    updateUI(null);
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            } else {
                // Sign in failed, handle error
                Log.e("MainActivity", "Sign in failed.");
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add marker to a predefined location and move the camera, this is just for testing.
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    // Route planning, carpool integration, bike sharing locator, public transit information,
    // EV sharing, carbon footprint tracking, and more to be implemented here.

    // Method to fetch and display commuting options
    private void fetchCommutingOptions() {
        // Placeholder to fetch and display commuting options using Google Maps API and other service APIs.
        // Implementation would involve making API calls and updating the UI accordingly.
    }

    // Method to allow users to join carpool
    private void joinCarpool() {
        // Placeholder for user to join carpool rides, will involve Firebase database interactions.
    }

    // Method to integrate bike-sharing services
    private void integrateBikeSharing() {
        // Placeholder to show bike stations and check availability of bikes.
    }

    // Method to provide public transit information
    private void showPublicTransitInfo() {
        // Placeholder for showing public transit schedules and delays.
    }

    // Method to integrate EV sharing services
    private void integrateEVSharing() {
        // Placeholder to show available EVs and their battery status.
    }

    // Method for carbon footprint tracking
    private void trackCarbonFootprint() {
        // Placeholder to calculate and display carbon footprint.
    }

    // Method for community-driven feedback
    private void collectFeedback() {
        // Placeholder for enabling user reviews and feedback on commuting options.
    }

    // Method for sending in-app notifications
    private void sendNotifications() {
        // Placeholder to send notifications for available carpool rides, bike stations, EVs, etc.
    }

    // Method for populating user dashboard
    private void populateUserDashboard() {
        // Placeholder for displaying user's commute statistics.
    }
}