// EcoTracker.java

package com.example.ecotracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EcoTracker extends AppCompatActivity implements OnMapReadyCallback {

    private SharedPreferences sharedPreferences;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecotracker);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users");
        
        sharedPreferences = getSharedPreferences("EcoTrackerPrefs", Context.MODE_PRIVATE);

        Button habitTrackingButton = findViewById(R.id.habitTrackingButton);
        habitTrackingButton.setOnClickListener(view -> openHabitTracker());

        Button ecoTipsButton = findViewById(R.id.ecoTipsButton);
        ecoTipsButton.setOnClickListener(view -> openEcoTips());

        Button communityChallengesButton = findViewById(R.id.communityChallengesButton);
        communityChallengesButton.setOnClickListener(view -> openCommunityChallenges());

        Button carbonFootprintButton = findViewById(R.id.carbonFootprintButton);
        carbonFootprintButton.setOnClickListener(view -> openCarbonFootprintCalculator());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng recyclerCenter = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions()
                .position(recyclerCenter)
                .title("Recycling Center"));
    }

    private void openHabitTracker() {
        setContentView(R.layout.activity_habit_tracker);
        // Add functionality for tracking habits here
    }

    private void openEcoTips() {
        setContentView(R.layout.activity_eco_tips);
        // Add functionality for eco tips here
    }

    private void openCommunityChallenges() {
        setContentView(R.layout.activity_community_challenges);
        // Add functionality for community challenges here
    }

    private void openCarbonFootprintCalculator() {
        setContentView(R.layout.activity_carbon_footprint_calculator);
        // Add functionality for carbon footprint calculation here
    }

    // Function for showing user notifications
    private void showNotification(String title, String content) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("EcoTrackerChannel", "EcoTracker Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "EcoTrackerChannel")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(0, builder.build());
    }
}