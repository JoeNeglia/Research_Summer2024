```java
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Elements
    private EditText emailEditText, passwordEditText;
    private ProgressBar progressBar;
    private Button loginButton, registerButton, logActivityButton, showStatsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        logActivityButton = findViewById(R.id.logActivityButton);
        showStatsButton = findViewById(R.id.showStatsButton);

        // Set button click listeners
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        logActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LogActivity.class));
            }
        });

        showStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Authenticate user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Create user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        // Navigate to Home Activity if the user is authenticated
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

class LogActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private EditText transportationEditText, energyConsumptionEditText, wasteProductionEditText;
    private Button saveButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        transportationEditText = findViewById(R.id.transportation);
        energyConsumptionEditText = findViewById(R.id.energyConsumption);
        wasteProductionEditText = findViewById(R.id.wasteProduction);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logActivity();
            }
        });
    }

    private void logActivity() {
        String userId = mAuth.getCurrentUser().getUid();
        String transportation = transportationEditText.getText().toString().trim();
        String energyConsumption = energyConsumptionEditText.getText().toString().trim();
        String wasteProduction = wasteProductionEditText.getText().toString().trim();

        if (TextUtils.isEmpty(transportation) || TextUtils.isEmpty(energyConsumption) || TextUtils.isEmpty(wasteProduction)) {
            Toast.makeText(getApplicationContext(), "Fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Save Activity data
        Map<String, String> activity = new HashMap<>();
        activity.put("transportation", transportation);
        activity.put("energyConsumption", energyConsumption);
        activity.put("wasteProduction", wasteProduction);

        mDatabase.child("users").child(userId).child("activities").push().setValue(activity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(LogActivity.this, "Activity logged!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(LogActivity.this, "Failed to log activity.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

class StatisticsActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private LineChart lineChart;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        lineChart = findViewById(R.id.lineChart);
        progressBar = findViewById(R.id.progressBar);

        fetchStatistics();
    }

    private void fetchStatistics() {
        String userId = mAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("users").child(userId).child("activities").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> entries = new ArrayList<>();
                int index = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Sample data parsing and calculation
                    String transportation = snapshot.child("transportation").getValue(String.class);
                    String energyConsumption = snapshot.child("energyConsumption").getValue(String.class);
                    String wasteProduction = snapshot.child("wasteProduction").getValue(String.class);

                    // Just an example metric
                    float score = calculateMetric(transportation, energyConsumption, wasteProduction);
                    entries.add(new Entry(index++, score));
                }

                LineDataSet dataSet = new LineDataSet(entries, "Carbon Footprint");
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                Description description = new Description();
                description.setText("Your Environmental Impact Over Time");
                lineChart.setDescription(description);
                lineChart.invalidate();

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(StatisticsActivity.this, "Failed to fetch data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private float calculateMetric(String transportation, String energyConsumption, String wasteProduction) {
        // Basic example calculation
        return transportation.length() + energyConsumption.length() + wasteProduction.length();
    }
}

class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Further implementation goes here for Home Activity
    }
}
```

**Notes:**
1. Ensure you have the necessary dependencies and a valid Firebase project link in your `build.gradle` files.
2. UI layouts (such as `activity_main.xml`, `activity_log.xml`, `activity_statistics.xml`, and `activity_home.xml`) should also be defined to match the `findViewById` calls.
3. Make sure to add appropriate permissions and configurations in `AndroidManifest.xml` and other required setup files (e.g., for Firebase).
4. The `HomeActivity` and other navigation elements should be tailored to complete functionalities as specified in the project prompt.

This is a comprehensive template to get started, but you'd need to add a few more key details to fulfill the prompt entirely.