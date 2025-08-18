// MainActivity.java

package com.ecotrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity class is the entry point of the EcoTrack app.
 * It checks for user authentication status and redirects accordingly.
 */
public class MainActivity extends AppCompatActivity {

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already logged in
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        }

        // Link to login and register buttons
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        // Set onClick listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }
}

// LoginActivity.java

package com.ecotrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * LoginActivity class handles user login functionality.
 */
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    /**
     * loginUser method handles the login process.
     */
    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}

// RegisterActivity.java

package com.ecotrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * RegisterActivity class handles user registration functionality.
 */
public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        Button registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    /**
     * registerUser method handles the registration process.
     */
    private void registerUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}

// ProfileActivity.java

package com.ecotrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * ProfileActivity class shows the user's profile and eco-footprint score.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView userEmail, ecoScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        userEmail = findViewById(R.id.userEmail);
        ecoScore = findViewById(R.id.ecoScore);
        Button logoutButton = findViewById(R.id.logoutButton);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userEmail.setText(currentUser.getEmail());
        }

        // Placeholder value for eco-score
        ecoScore.setText("Eco Score: 75");

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}

// ActivityTracking.java

package com.ecotrack;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * ActivityTracking class for tracking various eco-friendly activities.
 */
public class ActivityTracking extends AppCompatActivity {

    private EditText transportation, energyUsage, waterUsage, wasteManagement;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        transportation = findViewById(R.id.transportation);
        energyUsage = findViewById(R.id.energyUsage);
        waterUsage = findViewById(R.id.waterUsage);
        wasteManagement = findViewById(R.id.wasteManagement);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> trackActivity());
    }

    /**
     * trackActivity method handles the submission of tracked user data.
     */
    private void trackActivity() {
        String transportationMethod = transportation.getText().toString();
        String energy = energyUsage.getText().toString();
        String water = waterUsage.getText().toString();
        String waste = wasteManagement.getText().toString();

        // Placeholder for database submission
        Toast.makeText(this, "Activity tracked successfully!", Toast.LENGTH_SHORT).show();

        // Clear fields after submission
        transportation.setText("");
        energyUsage.setText("");
        waterUsage.setText("");
        wasteManagement.setText("");
    }
}

// EcoTips.java

package com.ecotrack;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * EcoTips class displays personalized tips based on user data.
 */
public class EcoTips extends AppCompatActivity {

    private TextView tipsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        tipsView = findViewById(R.id.tipsView);
        // Placeholder tips
        tipsView.setText("Try using public transport to reduce your carbon footprint.");
    }
}

// Achievements.java

package com.ecotrack;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Achievements class displays user achievements and badges.
 */
public class Achievements extends AppCompatActivity {

    private TextView achievementsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsView = findViewById(R.id.achievementsView);
        // Placeholder achievements
        achievementsView.setText("Achievement Unlocked: Biked 100km!");
    }
}

// ReminderService.java

package com.ecotrack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

/**
 * ReminderService class handles push notifications for reminders and tips.
 */
public class ReminderService extends Service {

    public static final String CHANNEL_ID = "EcoTrackReminderChannel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("EcoTrack Reminder")
                .setContentText("Don't forget to log your eco-friendly activities today!")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent);

        startForeground(1, notification.build());

        // Service stop self
        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Creates notification channel for reminders.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "EcoTrack Reminder Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}