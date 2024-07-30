Given the complexity of the project, I'll break down the implementation into sections. Here, I'm providing a comprehensive single-file implementation focusing more on structure, comments, and essential parts of the code. Full implementation with all features would require separating concerns into multiple files, but due to constraints, I'll keep it concise.

This is a basic outline to get you started. Features such as analytics, social platform, and optional integrations are complex and would need more detailed implementations.

```java
package com.example.fitnesstrackerapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
// Pedometer-related imports
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Firebase Authentication
    private FirebaseAuth mAuth;

    // Firebase Database
    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    // UI Elements
    private EditText emailText, passwordText;
    private Button loginButton, logoutButton;
    private TextView stepsTextView, caloriesTextView, activeMinutesTextView, goalProgressTextView;
    private ProgressBar goalProgressBar;

    // Step Counting
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private boolean isCounting = false;
    private int stepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Initialize UI elements
        emailText = findViewById(R.id.emailText);
        passwordText = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        logoutButton = findViewById(R.id.logoutButton);
        stepsTextView = findViewById(R.id.stepsTextView);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        activeMinutesTextView = findViewById(R.id.activeMinutesTextView);
        goalProgressTextView = findViewById(R.id.goalProgressTextView);
        goalProgressBar = findViewById(R.id.goalProgressBar);

        // Initialize step counter
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Update UI based on user authentication state
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void loginUser() {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        updateUI(null);
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            loginButton.setVisibility(View.GONE);
            emailText.setVisibility(View.GONE);
            passwordText.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
            isCounting = true;
        } else {
            loginButton.setVisibility(View.VISIBLE);
            emailText.setVisibility(View.VISIBLE);
            passwordText.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
            sensorManager.unregisterListener(this);
            isCounting = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isCounting) {
            stepCount = (int) event.values[0];
            stepsTextView.setText("Steps: " + stepCount);
            // Update other UI elements and Firebase database as needed
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this implementation
    }
}

// Layout file: res/layout/activity_main.xml
/*
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <EditText
        android:id="@+id/emailText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:hint="Email" />

    <EditText
        android:id="@+id/passwordText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:layout_below="@+id/emailText"
        android:layout_marginTop="8dp" />

    <Button
        android:id="@+id/loginButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Login"
        android:layout_below="@+id/passwordText"
        android:layout_marginTop="8dp" />

    <Button
        android:id="@+id/logoutButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Logout"
        android:layout_below="@+id/passwordText"
        android:layout_marginTop="8dp"
        android:visibility="gone" />

    <TextView
        android:id="@+id/stepsTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@+id/logoutButton"
        android:layout_marginTop="16dp"
        android:text="Steps: 0"
        android:textSize="18sp" />

    <!-- Additional UI elements for dashboard -->
    <TextView
        android:id="@+id/caloriesTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@+id/stepsTextView"
        android:layout_marginTop="8dp"
        android:text="Calories: 0"
        android:textSize="18sp" />

    <TextView
        android:id="@+id/activeMinutesTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@+id/caloriesTextView"
        android:layout_marginTop="8dp"
        android:text="Active Minutes: 0"
        android:textSize="18sp" />

    <TextView
        android:id="@+id/goalProgressTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@+id/activeMinutesTextView"
        android:layout_marginTop="8dp"
        android:text="Goal Progress"
        android:textSize="18sp" />

    <ProgressBar
        android:id="@+id/goalProgressBar"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@+id/goalProgressTextView"
        android:layout_marginTop="8dp"
        android:max="100"
        android:progress="0" />
</RelativeLayout>
*/
```

### Explanation:
1. **Firebase Authentication**:
   - `FirebaseAuth` is used for authentication.
   - `mAuth.signInWithEmailAndPassword` to log in the user.
   - `mAuth.signOut` to log out the user.
   - `updateUI` to toggle visibility of login/logout buttons and register/unregister sensor listener based on user authentication status.

2. **Step Counting**:
   - Implemented using `SensorManager` and `SensorEventListener`.
   - Registered for step counter sensor to count steps and update the UI.

3. **Dashboard Layout**:
   - Basic layout with EditTexts for email and password.
   - Buttons for login and logout.
   - TextViews for displaying step count, calories, active minutes, and goal progress.
   - ProgressBar for goal progress.

### Comments:
- This implementation provides a basic structure and essential functionalities.
- Expansion for the other features (workout logging, goal setting, nutrition tracking, performance analytics, and social platform) would follow a similar pattern, involving Firebase for backend, Android components for UI, and necessary integrations.
- For a complete implementation, you should separate classes (e.g., separate files for authentication, database management, UI components, etc.), error handling, and more sophisticated UI/UX interactions.