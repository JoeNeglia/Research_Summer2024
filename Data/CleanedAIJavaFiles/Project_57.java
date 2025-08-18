// File: PersonalFitnessTracker.java

// Necessary imports
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// Main Activity class that handles user authentication and dashboard
public class PersonalFitnessTracker extends AppCompatActivity {
    private static final String TAG = "PersonalFitnessTracker";
    private static final String CHANNEL_ID = "fitness_tracker_channel";

    // Firebase components
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private EditText emailField, passwordField, usernameField;
    private Button loginButton, registerButton, logoutButton, trackButton, goalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI Components
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        usernameField = findViewById(R.id.username);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.register);
        logoutButton = findViewById(R.id.logout);
        trackButton = findViewById(R.id.trackWorkout);
        goalButton = findViewById(R.id.setGoal);

        // Notification Setup
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FitnessTrackerChannel";
            String description = "Channel for fitness tracker notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Authentication Listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(emailField.getText().toString(), passwordField.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(emailField.getText().toString(), passwordField.getText().toString(), usernameField.getText().toString());
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                updateUI(null);
            }
        });

        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalFitnessTracker.this, TrackWorkoutActivity.class));
            }
        });

        goalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PersonalFitnessTracker.this, SetGoalActivity.class));
            }
        });

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    // Method to register a new user
    private void registerUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates);
                            updateUI(user);
                        } else {
                            Toast.makeText(PersonalFitnessTracker.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    // Method to login an existing user
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(PersonalFitnessTracker.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    // Update UI according to the authenticated user
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in
            findViewById(R.id.authenticated_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.unauthenticated_layout).setVisibility(View.GONE);
        } else {
            // User is signed out
            findViewById(R.id.authenticated_layout).setVisibility(View.GONE);
            findViewById(R.id.unauthenticated_layout).setVisibility(View.VISIBLE);
        }
    }
}


// TrackWorkoutActivity.java

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// Activity to log a new workout
public class TrackWorkoutActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText typeField, durationField, caloriesField;
    private Button logWorkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_workout);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI Components
        typeField = findViewById(R.id.type);
        durationField = findViewById(R.id.duration);
        caloriesField = findViewById(R.id.calories);
        logWorkoutButton = findViewById(R.id.logWorkoutButton);

        logWorkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logWorkout();
            }
        });
    }

    // Method to log workout data in Firestore
    private void logWorkout() {
        String type = typeField.getText().toString();
        String duration = durationField.getText().toString();
        String calories = caloriesField.getText().toString();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> workout = new HashMap<>();
        workout.put("type", type);
        workout.put("duration", duration);
        workout.put("calories", calories);

        db.collection("users").document(currentUser.getUid())
                .collection("workouts").add(workout)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(TrackWorkoutActivity.this, "Workout Logged", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(TrackWorkoutActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


// SetGoalActivity.java

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// Activity to set or update fitness goals
public class SetGoalActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText workoutsField, caloriesField;
    private Button setGoalButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_goal);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI Components
        workoutsField = findViewById(R.id.workouts);
        caloriesField = findViewById(R.id.calories);
        setGoalButton = findViewById(R.id.setGoalButton);

        setGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setGoal();
            }
        });
    }

    // Method to set fitness goals in Firestore
    private void setGoal() {
        String workouts = workoutsField.getText().toString();
        String calories = caloriesField.getText().toString();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> goal = new HashMap<>();
        goal.put("workouts", workouts);
        goal.put("calories", calories);

        db.collection("users").document(currentUser.getUid())
                .collection("goals").document("currentGoal").set(goal)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SetGoalActivity.this, "Goal Set", Toast.LENGTH_SHORT).show();
                        finish();

                        // Set a notification for the goal in 24 hours (repeat it daily)
                        // Code for setting notification based on goal time (omitted for brevity)
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetGoalActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

// The XML layout files (activity_main.xml, activity_track_workout.xml and activity_set_goal.xml) 
// would be needed for this application to define the user interface for each activity. 
// Ensure that your AndroidManifest.xml includes the activities and Firebase permissions as required.