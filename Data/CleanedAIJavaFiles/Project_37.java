import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthException;
import java.util.HashMap;
import java.util.Map;
import android.os.Handler;
import androidx.annotation.NonNull;

// Main Entry Point Activity
public class MainActivity extends AppCompatActivity {

    private Button loginButton, registerButton;
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);

        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                signIn(email, password);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                createAccount(email, password);
            }
        });

    }

    // Sign in existing user
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    // Create a new user account
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            writeNewUser(userId, email);
                        }
                        updateUI(user);
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    // Write new user data to Firebase
    private void writeNewUser(String userId, String email) {
        User user = new User(email);
        mDatabase.child("users").child(userId).setValue(user);
    }

    // Update UI based on user status
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

// User model
class User {
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email) {
        this.email = email;
    }
}

// Dashboard Activity
class DashboardActivity extends AppCompatActivity {

    private Button addPlantButton, viewPlantsButton, setReminderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        addPlantButton = findViewById(R.id.add_plant_button);
        viewPlantsButton = findViewById(R.id.view_plants_button);
        setReminderButton = findViewById(R.id.set_reminder_button);

        addPlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, AddPlantActivity.class);
                startActivity(intent);
            }
        });

        viewPlantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ViewPlantsActivity.class);
                startActivity(intent);
            }
        });

        setReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, SetReminderActivity.class);
                startActivity(intent);
            }
        });
    }
}

// Add Plant Activity
class AddPlantActivity extends AppCompatActivity {

    private EditText plantNameEditText, plantSpeciesEditText;
    private Button savePlantButton;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plant);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        plantNameEditText = findViewById(R.id.plant_name_edit_text);
        plantSpeciesEditText = findViewById(R.id.plant_species_edit_text);
        savePlantButton = findViewById(R.id.save_plant_button);

        savePlantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlant();
            }
        });
    }

    // Save plant data to Firebase
    private void savePlant() {
        String plantName = plantNameEditText.getText().toString();
        String plantSpecies = plantSpeciesEditText.getText().toString();

        if (!plantName.isEmpty() && !plantSpecies.isEmpty()) {
            String userId = mAuth.getCurrentUser().getUid();
            String plantId = mDatabase.child("plants").child(userId).push().getKey();
            Plant plant = new Plant(plantName, plantSpecies);
            mDatabase.child("plants").child(userId).child(plantId).setValue(plant);

            Toast.makeText(AddPlantActivity.this, "Plant saved.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(AddPlantActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
        }
    }
}

// Plant model
class Plant {
    public String name;
    public String species;

    public Plant() {
        // Default constructor required for calls to DataSnapshot.getValue(Plant.class)
    }

    public Plant(String name, String species) {
        this.name = name;
        this.species = species;
    }
}

// View Plants Activity
class ViewPlantsActivity extends AppCompatActivity {

    // Placeholder for plant listing code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_plants);
    }
}

// Set Reminder Activity
class SetReminderActivity extends AppCompatActivity {

    private EditText reminderTitleEditText, reminderTimeEditText;
    private Button saveReminderButton;
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "plant_care_reminder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_reminder);

        reminderTitleEditText = findViewById(R.id.reminder_title_edit_text);
        reminderTimeEditText = findViewById(R.id.reminder_time_edit_text);
        saveReminderButton = findViewById(R.id.save_reminder_button);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Plant Care Reminder";
            String description = "Channel for plant care reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        saveReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReminder();
            }
        });
    }

    // Set reminder notification
    private void setReminder() {
        String title = reminderTitleEditText.getText().toString();
        String time = reminderTimeEditText.getText().toString();

        if (!title.isEmpty() && !time.isEmpty()) {
            Handler handler = new Handler();
            handler.postDelayed(() -> sendNotification(title), Long.parseLong(time) * 1000);

            Toast.makeText(SetReminderActivity.this, "Reminder set.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(SetReminderActivity.this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
        }
    }

    // Send reminder notification
    private void sendNotification(String title) {
        Intent intent = new Intent(this, DashboardActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Plant Care Reminder")
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}