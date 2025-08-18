package com.example.ecotrack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.HashMap;

// MainActivity Class - Entry point
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText email, password;
    private Button signInButton, signUpButton;

    // onCreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);

        signInButton.setOnClickListener(view -> signInUser(email.getText().toString(), password.getText().toString()));
        signUpButton.setOnClickListener(view -> signUpUser(email.getText().toString(), password.getText().toString()));
        
        createNotificationChannel();
    }

    // Essential methods for sign-in and up
    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                // Sign-in failed, display error message
                updateUI(null);
            }
        });
    }

    private void signUpUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                saveNewUser(user);
                updateUI(user);
            } else {
                // Sign-up failed, display error message
                updateUI(null);
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Show error message
        }
    }

    private void saveNewUser(FirebaseUser user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String userId = user.getUid();
        UserProfile userProfile = new UserProfile(user.getEmail(), "Unknown", new HashMap<>());
        mDatabase.child("users").child(userId).setValue(userProfile);
    }

    // Create a notification channel for notifications
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EcoTrackChannel";
            String description = "Channel for EcoTrack notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("EcoTrack", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

// User Profile class for database mapping
class UserProfile {
    public String email;
    public String location;
    public HashMap<String, Boolean> environmentalGoals;

    public UserProfile(String email, String location, HashMap<String, Boolean> environmentalGoals) {
        this.email = email;
        this.location = location;
        this.environmentalGoals = environmentalGoals;
    }
}

// HomeActivity class
class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView userProfileText;
    private Button logActivityButton, viewLogsButton, communityButton, resourcesButton, settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        userProfileText = findViewById(R.id.userProfileText);
        logActivityButton = findViewById(R.id.logActivityButton);
        viewLogsButton = findViewById(R.id.viewLogsButton);
        communityButton = findViewById(R.id.communityButton);
        resourcesButton = findViewById(R.id.resourcesButton);
        settingsButton = findViewById(R.id.settingsButton);

        if (currentUser != null) {
            userProfileText.setText("Welcome, " + currentUser.getEmail());
        }

        logActivityButton.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, LogActivityActivity.class)));
        viewLogsButton.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ViewLogsActivity.class)));
        communityButton.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, CommunityActivity.class)));
        resourcesButton.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ResourcesActivity.class)));
        settingsButton.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, SettingsActivity.class)));
    }
}

// LogActivityActivity class
class LogActivityActivity extends AppCompatActivity {

    private EditText logDescription, carbonFootprint;
    private Button saveLogButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logDescription = findViewById(R.id.logDescription);
        carbonFootprint = findViewById(R.id.carbonFootprint);
        saveLogButton = findViewById(R.id.saveLogButton);

        saveLogButton.setOnClickListener(view -> saveLog());
    }

    private void saveLog() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String logId = mDatabase.child("logs").push().getKey();
        LogActivity log = new LogActivity(logDescription.getText().toString(), Float.parseFloat(carbonFootprint.getText().toString()));
        assert logId != null;
        mDatabase.child("logs").child(logId).setValue(log);
    }
}

// LogActivity class for database mapping
class LogActivity {
    public String description;
    public float carbonFootprint;

    public LogActivity(String description, float carbonFootprint) {
        this.description = description;
        this.carbonFootprint = carbonFootprint;
    }
}

// Note: Class definitions for ViewLogsActivity, CommunityActivity, ResourcesActivity, and SettingsActivity follow a similar pattern.

// SettingsActivity class
class SettingsActivity extends AppCompatActivity {

    private EditText location;
    private Button saveSettingsButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        location = findViewById(R.id.location);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);

        saveSettingsButton.setOnClickListener(view -> saveSettings(user));
    }

    private void saveSettings(FirebaseUser user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child("users").child(userId).child("location").setValue(location.getText().toString());
        }
    }
}

// Final Sample Layout Files and XML Setup:
// res/layout/activity_main.xml, res/layout/activity_home.xml, res/layout/activity_log.xml, res/layout/activity_settings.xml