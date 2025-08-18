// Import Statements
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HabiTrackActivity extends AppCompatActivity {

    // Firebase authentication and database instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI elements
    private EditText emailEditText, passwordEditText, habitNameEditText;
    private Button registerButton, loginButton, addHabitButton;
    private RecyclerView dashboardRecyclerView;
    private ProgressBar habitProgressBar;
    private BottomNavigationView navigationView;

    // Shared preferences
    private SharedPreferences sharedPreferences;

    // Habit list
    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habitrack);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        habitNameEditText = findViewById(R.id.habitNameEditText);
        addHabitButton = findViewById(R.id.addHabitButton);
        dashboardRecyclerView = findViewById(R.id.dashboardRecyclerView);
        habitProgressBar = findViewById(R.id.habitProgressBar);
        navigationView = findViewById(R.id.navigationView);

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences("HabiTrackPrefs", Context.MODE_PRIVATE);

        // Initialize habit list and adapter
        habitList = new ArrayList<>();
        habitAdapter = new HabitAdapter(habitList);

        // Setup RecyclerView
        dashboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dashboardRecyclerView.setAdapter(habitAdapter);

        // Setup event listeners
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addHabit();
            }
        });

        navigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    // Handle dashboard
                    return true;
                case R.id.navigation_reports:
                    // Handle reports
                    return true;
                case R.id.navigation_settings:
                    // Handle settings
                    return true;
            }
            return false;
        });

        // Load habits from Firestore
        loadHabits();
    }

    private void registerUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        // Register user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(HabiTrackActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        loadHabits();
                    } else {
                        // Registration failed
                        Toast.makeText(HabiTrackActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        // Login user with Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(HabiTrackActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        loadHabits();
                    } else {
                        // Login failed
                        Toast.makeText(HabiTrackActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addHabit() {
        String habitName = habitNameEditText.getText().toString();
        // Add habit to Firestore
        Map<String, Object> habit = new HashMap<>();
        habit.put("name", habitName);
        habit.put("progress", 0);
        db.collection("habits").add(habit)
                .addOnSuccessListener(documentReference -> {
                    habitList.add(new Habit(habitName, 0));
                    habitAdapter.notifyDataSetChanged();
                    Toast.makeText(HabiTrackActivity.this, "Habit added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(HabiTrackActivity.this, "Failed to add habit!", Toast.LENGTH_SHORT).show());
    }

    private void loadHabits() {
        db.collection("habits")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            int progress = document.getLong("progress").intValue();
                            habitList.add(new Habit(name, progress));
                        }
                        habitAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HabiTrackActivity.this, "Failed to load habits!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class Habit {
        String name;
        int progress;

        Habit(String name, int progress) {
            this.name = name;
            this.progress = progress;
        }
    }

    private class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

        private ArrayList<Habit> habitList;

        HabitAdapter(ArrayList<Habit> habitList) {
            this.habitList = habitList;
        }

        @NonNull
        @Override
        public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_item, parent, false);
            return new HabitViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
            Habit habit = habitList.get(position);
            holder.habitNameTextView.setText(habit.name);
            holder.habitProgressBar.setProgress(habit.progress);
        }

        @Override
        public int getItemCount() {
            return habitList.size();
        }

        class HabitViewHolder extends RecyclerView.ViewHolder {
            TextView habitNameTextView;
            ProgressBar habitProgressBar;

            HabitViewHolder(View itemView) {
                super(itemView);
                habitNameTextView = itemView.findViewById(R.id.habitNameTextView);
                habitProgressBar = itemView.findViewById(R.id.habitProgressBar);
            }
        }
    }
}