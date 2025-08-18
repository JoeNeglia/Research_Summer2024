// MainActivity.java
package com.example.workoutcompanion;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private TextView userDetailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        userDetailsTextView = findViewById(R.id.userDetailsTextView);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> createUser());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email or Password should not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Authentication Successful",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication Failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email or Password should not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserProfile(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication Failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserProfile(FirebaseUser user) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", user.getUid());
        userProfile.put("email", user.getEmail());
        userProfile.put("age", "");
        userProfile.put("weight", "");
        userProfile.put("height", "");
        userProfile.put("fitnessLevel", "");
        userProfile.put("fitnessGoals", "");

        db.collection("users").document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Profile Created Successfully",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Failed to Create Profile",
                                Toast.LENGTH_SHORT).show());
    }
}

// UserProfileActivity.java
package com.example.workoutcompanion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText ageEditText, weightEditText, heightEditText, fitnessLevelEditText, fitnessGoalsEditText;
    private Button updateProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ageEditText = findViewById(R.id.ageEditText);
        weightEditText = findViewById(R.id.weightEditText);
        heightEditText = findViewById(R.id.heightEditText);
        fitnessLevelEditText = findViewById(R.id.fitnessLevelEditText);
        fitnessGoalsEditText = findViewById(R.id.fitnessGoalsEditText);
        updateProfileButton = findViewById(R.id.updateProfileButton);

        updateProfileButton.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String age = ageEditText.getText().toString();
            String weight = weightEditText.getText().toString();
            String height = heightEditText.getText().toString();
            String fitnessLevel = fitnessLevelEditText.getText().toString();
            String fitnessGoals = fitnessGoalsEditText.getText().toString();

            if (TextUtils.isEmpty(age) || TextUtils.isEmpty(weight) ||
                    TextUtils.isEmpty(height) || TextUtils.isEmpty(fitnessLevel) ||
                    TextUtils.isEmpty(fitnessGoals)) {
                Toast.makeText(this, "All fields must be filled.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("age", age);
            userProfile.put("weight", weight);
            userProfile.put("height", height);
            userProfile.put("fitnessLevel", fitnessLevel);
            userProfile.put("fitnessGoals", fitnessGoals);

            db.collection("users").document(user.getUid())
                    .update(userProfile)
                    .addOnSuccessListener(aVoid -> Toast.makeText(UserProfileActivity.this,
                            "Profile Updated Successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(UserProfileActivity.this,
                            "Failed to Update Profile", Toast.LENGTH_SHORT).show());
        }
    }
}

// WorkoutActivity.java
package com.example.workoutcompanion;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class WorkoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        // Example components to simulate adding a workout
        EditText workoutNameEditText = findViewById(R.id.workoutNameEditText);
        EditText repsEditText = findViewById(R.id.repsEditText);
        EditText setsEditText = findViewById(R.id.setsEditText);
        Button saveWorkoutButton = findViewById(R.id.saveWorkoutButton);

        // SQLite example for local storage
        saveWorkoutButton.setOnClickListener(v -> {
            String workoutName = workoutNameEditText.getText().toString();
            int reps = Integer.parseInt(repsEditText.getText().toString());
            int sets = Integer.parseInt(setsEditText.getText().toString());

            WorkoutDbHelper dbHelper = new WorkoutDbHelper(WorkoutActivity.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(WorkoutContract.WorkoutEntry.COLUMN_NAME_TITLE, workoutName);
            values.put(WorkoutContract.WorkoutEntry.COLUMN_NAME_REPS, reps);
            values.put(WorkoutContract.WorkoutEntry.COLUMN_NAME_SETS, sets);

            long newRowId = db.insert(WorkoutContract.WorkoutEntry.TABLE_NAME, null, values);
            if (newRowId != -1) {
                Toast.makeText(WorkoutActivity.this, "Workout Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(WorkoutActivity.this, "Error Saving Workout", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

// DBHelper.java
package com.example.workoutcompanion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WorkoutDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "workout.db";
    private static final int DATABASE_VERSION = 1;

    public WorkoutDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + WorkoutContract.WorkoutEntry.TABLE_NAME + " (" +
                    WorkoutContract.WorkoutEntry._ID + " INTEGER PRIMARY KEY," +
                    WorkoutContract.WorkoutEntry.COLUMN_NAME_TITLE + " TEXT," +
                    WorkoutContract.WorkoutEntry.COLUMN_NAME_REPS + " INTEGER," +
                    WorkoutContract.WorkoutEntry.COLUMN_NAME_SETS + " INTEGER)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simplified: discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + WorkoutContract.WorkoutEntry.TABLE_NAME);
        onCreate(db);
    }
}

// WorkoutContract.java
package com.example.workoutcompanion;

import android.provider.BaseColumns;

public final class WorkoutContract {
    private WorkoutContract() {
    }

    public static class WorkoutEntry implements BaseColumns {
        public static final String TABLE_NAME = "workout";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_REPS = "reps";
        public static final String COLUMN_NAME_SETS = "sets";
    }
}

// activity_main.xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <EditText
        android:id="@+id/emailEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Email"
        android:inputType="textEmailAddress" />
    <EditText
        android:id="@+id/passwordEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/emailEditText"
        android:hint="Password"
        android:inputType="textPassword" />
    <Button
        android:id="@+id/loginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/passwordEditText"
        android:text="Login" />
    <Button
        android:id="@+id/registerButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/loginButton"
        android:text="Register" />
    <TextView
        android:id="@+id/userDetailsTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/registerButton"
        android:text="User details will appear here" />
</RelativeLayout>

// activity_user_profile.xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <EditText
        android:id="@+id/ageEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Age" />
    <EditText
        android:id="@+id/weightEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/ageEditText"
        android:hint="Weight" />
    <EditText
        android:id="@+id/heightEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/weightEditText"
        android:hint="Height" />
    <EditText
        android:id="@+id/fitnessLevelEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/heightEditText"
        android:hint="Fitness Level" />
    <EditText
        android:id="@+id/fitnessGoalsEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/fitnessLevelEditText"
        android:hint="Fitness Goals" />
    <Button
        android:id="@+id/updateProfileButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/fitnessGoalsEditText"
        android:text="Update Profile" />
</RelativeLayout>

// activity_workout.xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <EditText
        android:id="@+id/workoutNameEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Workout Name" />
    <EditText
        android:id="@+id/repsEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/workoutNameEditText"
        android:hint="Reps" />
    <EditText
        android:id="@+id/setsEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/repsEditText"
        android:hint="Sets" />
    <Button
        android:id="@+id/saveWorkoutButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/setsEditText"
        android:text="Save Workout" />
</RelativeLayout>