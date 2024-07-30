Here's the comprehensive implementation for the Android Eco-Friendly Habits Tracker App with Java. Due to the complexity and length of the project, this will be an extensive single file. Note that this code includes only the structural framework with in-code comments to guide you through the different features. You would need to incrementally build each feature into fully working functionality which can span several weeks as per your project timeline:

```java
// MainActivity.java

package com.eco.habittracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.eco.habittracker.auth.LoginActivity;
import com.eco.habittracker.dashboard.DashboardFragment;
import com.eco.habittracker.habits.HabitsFragment;
import com.eco.habittracker.reminder.ReminderService;
import com.eco.habittracker.community.CommunityFragment;
import com.eco.habittracker.gamification.GamificationFragment;
import com.eco.habittracker.tips.TipsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If the user is not logged in, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Setting up BottomNavigationView for navigation among different features
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Load the default fragment
        loadFragment(new DashboardFragment());

        // Set up notification channel for reminders
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("habit_tracker_channel", name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.navigation_dashboard:
                fragment = new DashboardFragment();
                loadFragment(fragment);
                return true;
            case R.id.navigation_habits:
                fragment = new HabitsFragment();
                loadFragment(fragment);
                return true;
            case R.id.navigation_tips:
                fragment = new TipsFragment();
                loadFragment(fragment);
                return true;
            case R.id.navigation_community:
                fragment = new CommunityFragment();
                loadFragment(fragment);
                return true;
            case R.id.navigation_gamification:
                fragment = new GamificationFragment();
                loadFragment(fragment);
                return true;
        }
        return false;
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Setting Reminders using Service on Stop
        Intent intent = new Intent(this, ReminderService.class);
        startService(intent);
    }
}

// Auth/LoginActivity.java

package com.eco.habittracker.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.eco.habittracker.MainActivity;
import com.eco.habittracker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField, nameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        nameField = findViewById(R.id.name);
        Button loginButton = findViewById(R.id.button_login);
        Button registerButton = findViewById(R.id.button_register);

        // Login button functionality
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            loginWithEmail(email, password);
        });

        // Register button functionality
        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();
            String name = nameField.getText().toString();
            registerWithEmail(email, password, name);
        });
    }

    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void registerWithEmail(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateProfile(user, name);
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateProfile(FirebaseUser user, String name) {
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "User profile updated.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

// dashboard/DashboardFragment.java

package com.eco.habittracker.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eco.habittracker.R;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the dashboard with user statistics, progress, and impact data
        // e.g. total water saved, CO2 emissions reduced etc.
    }
}

// habits/HabitsFragment.java

package com.eco.habittracker.habits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eco.habittracker.R;

public class HabitsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_habits, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add custom eco-friendly habits creation and tracking logic here
        // Including options for daily, weekly, or monthly goals
        // Interface to view user progress with visual aids like graphs and progress bars
    }
}

// reminder/ReminderService.java

package com.eco.habittracker.reminder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.eco.habittracker.R;

public class ReminderService extends Service {

    public ReminderService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Generate notifications to remind users of their eco-friendly habits
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "habit_tracker_channel")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Eco-friendly Habit Reminder")
                .setContentText("Don't forget to complete your eco-friendly habits for today!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }
}

// tips/TipsFragment.java

package com.eco.habittracker.tips;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eco.habittracker.R;

public class TipsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tips, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Fetch and display tips and articles related to sustainability and eco-friendly living
        // Allow users to bookmark their favorite tips and articles
    }
}

// community/CommunityFragment.java

package com.eco.habittracker.community;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eco.habittracker.R;

public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Create a community feature allowing users to share progress and success stories
        // Integrate social media sharing options
    }
}

// gamification/GamificationFragment.java

package com.eco.habittracker.gamification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.eco.habittracker.R;

public class GamificationFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gamification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Implement gamification logic with points and rewards system
        // Allow users to earn badges for reaching specific milestones
    }
}

// res/layout/activity_main.xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <fragment
        android:id="@+id/nav_host_fragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:name="androidx.navigation.fragment.NavHostFragment"
        tools:layout_editor_absoluteX="0dp"
        tools:layout_editor_absoluteY="0dp"
        tools:ignore="MissingConstraints" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/nav_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:background="?android:attr/windowBackground"
        android:menu="@menu/bottom_nav_menu" />

</RelativeLayout>

// res/layout/activity_login.xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".auth.LoginActivity">

    <EditText
        android:id="@+id/email"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:hint="Email" />

    <EditText
        android:id="@+id/password"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/email"
        android:layout_margin="16dp"
        android:hint="Password"
        android:inputType="textPassword" />

    <EditText
        android:id="@+id/name"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/password"
        android:layout_margin="16dp"
        android:hint="Name"
        android:visibility="gone" />

    <Button
        android:id="@+id/button_login"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/name"
        android:layout_margin="16dp"
        android:text="Login" />

    <Button
        android:id="@+id/button_register"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/name"
        android:layout_margin="16dp"
        android:layout_toEndOf="@id/button_login"
        android:text="Register" />

</RelativeLayout>

// res/layout/fragment_dashboard.xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <!-- Layout for Dashboard: Statistics, progress, and impact overview -->
    <TextView
        android:id="@+id/dashboard_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Dashboard"
        android:textSize="24sp"
        android:textStyle="bold" />

    <!-- Add additional views for displaying dashboard data -->

</RelativeLayout>

// res/layout/fragment_habits.xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <!-- Layout for Habits: Create custom habits, set goals, track progress -->
    <TextView
        android:id="@+id/habits_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Habits"
        android:textSize="24sp"
        android:textStyle="bold" />

    <!-- Add additional views for managing habits -->

</RelativeLayout>

// res/layout/fragment_tips.xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <!-- Layout for Tips: Display tips and articles related to eco-friendly habits -->
    <TextView
        android:id="@+id/tips_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Tips"
        android:textSize="24sp"
        android:textStyle="bold" />

    <!-- Add additional views for displaying tips and articles -->

</RelativeLayout>

// res/layout/fragment_community.xml

<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <!-- Layout for Community: Share progress, success stories and social media integration -->
    <TextView
        android:id="@+id/community_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Community"
        android:textSize="24sp"
        android:textStyle="bold" />

    <!-- Add additional views for community features -->

</RelativeLayout>

// res/layout/fragment_gamification.xml

<?xml version="1.0" encoding="utf