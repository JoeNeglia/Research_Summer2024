// The main file: EcoTracker.java
package com.example.ecotracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.ecotracker.auth.LoginActivity;
import com.example.ecotracker.dashboard.DashboardFragment;
import com.example.ecotracker.habits.HabitsFragment;
import com.example.ecotracker.logger.ActivityLoggerFragment;
import com.example.ecotracker.rewards.RewardsFragment;
import com.example.ecotracker.settings.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EcoTracker extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userDbRef;
    
    private static final String CHANNEL_ID = "EcoFriendlyHabitsChannel";
    private static final String CHANNEL_NAME = "Habit Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for eco-friendly habit reminders";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            userDbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            setupBottomNavigation();
            createNotificationChannel();
        } else {
            // Redirect to Login Activity
            startActivity(new Intent(EcoTracker.this, LoginActivity.class));
            finish();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_dashboard:
                    selectedFragment = new DashboardFragment();
                    break;
                case R.id.nav_habits:
                    selectedFragment = new HabitsFragment();
                    break;
                case R.id.nav_logger:
                    selectedFragment = new ActivityLoggerFragment();
                    break;
                case R.id.nav_rewards:
                    selectedFragment = new RewardsFragment();
                    break;
                case R.id.nav_settings:
                    selectedFragment = new SettingsFragment();
                    break;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}