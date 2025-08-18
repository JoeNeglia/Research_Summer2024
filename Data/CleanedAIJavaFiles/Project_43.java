package com.example.advanceddhabittracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.advanceddhabittracker.databinding.ActivityMainBinding;
import com.example.advanceddhabittracker.models.Habit;
import com.example.advanceddhabittracker.models.User;
import com.example.advanceddhabittracker.utils.NotificationUtils;
import com.example.advanceddhabittracker.viewmodel.HabitViewModel;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

// Main Application Class to initialize resources
public class HabitTrackerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "habit_reminder_channel", 
                    "Habit Reminder", 
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

// User Authentication Activity
public class AuthActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private final int RC_SIGN_IN = 9001; // Google Sign In request code

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        setupGoogleSignIn();
    }
    
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        findViewById(R.id.sign_in_button).setOnClickListener(v -> signInWithGoogle(gso));
    }
    
    private void signInWithGoogle(GoogleSignInOptions gso) {
        // Implementation of Google Sign-In
    }
    
    private void handleFirebaseAuth(FirebaseUser user) {
        if (user != null) {
            // Save user data and navigate to MainActivity
        }
    }
}

// Main Activity or Home Activity
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private HabitViewModel habitViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        setupNavigation();
        initializeViewModel();
        initializeUI();
        
        // Setting up RecyclerView
        RecyclerView recyclerView = findViewById(R.id.habit_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Observer ViewModel Habit Data
    }
    
    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Setup navigation setup code
    }
    
    private void initializeViewModel() {
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        // Observe LiveData
    }
    
    private void initializeUI() {
        FloatingActionButton fab = findViewById(R.id.fab_add_habit);
        fab.setOnClickListener(view -> showAddHabitDialog());
    }
    
    private void showAddHabitDialog() {
        // Dialog for adding a habit
    }
}

// ViewModel for managing Habit Data
public class HabitViewModel extends AndroidViewModel {
    private final LiveData<List<Habit>> allHabits;
    private final HabitRepository repository;
    
    public HabitViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitRepository(application);
        allHabits = repository.getAllHabits();
    }
    
    public void insert(Habit habit) {
        repository.insert(habit);
    }
    
    public void delete(Habit habit) {
        repository.delete(habit);
    }
    
    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }
}

// Habit Repository
public class HabitRepository {
    private final HabitDao habitDao;
    private final LiveData<List<Habit>> allHabits;
    
    public HabitRepository(Application application) {
        HabitDatabase database = HabitDatabase.getInstance(application);
        habitDao = database.habitDao();
        allHabits = habitDao.getAllHabits();
    }
    
    public void insert(Habit habit) {
        // Perform insert operation
    }
    
    public void delete(Habit habit) {
        // Perform delete operation
    }
    
    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }
}

// Habit Entity
@Entity(tableName = "habit_table")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private String frequency;
    private long createdAt;
    private long updatedAt;
    
    // Constructors, getters, and setters
}

// Firebase Notification Utility
public class NotificationUtils {
    public static void scheduleNotification(Context context, String title, String message, long timeInMillis) {
        // Implementation for scheduling local notification
    }
}

// Habit Adapter for displaying habits in RecyclerView
public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitHolder> {
    private List<Habit> habits = new ArrayList<>();
    
    @NonNull
    @Override
    public HabitHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate habit item layout
    }
    
    @Override
    public void onBindViewHolder(@NonNull HabitHolder holder, int position) {
        // Bind habit data to views
    }
    
    @Override
    public int getItemCount() {
        return habits.size();
    }
    
    public void setHabits(List<Habit> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }
    
    class HabitHolder extends RecyclerView.ViewHolder {
        public HabitHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
        }
    }
}

// HabitDao Interface for Room Database operations
@Dao
public interface HabitDao {
    @Insert
    void insert(Habit habit);
    
    @Delete
    void delete(Habit habit);
    
    @Query("SELECT * FROM habit_table ORDER BY createdAt DESC")
    LiveData<List<Habit>> getAllHabits();
}

// Habit Database for Room Database
@Database(entities = {Habit.class}, version = 1, exportSchema = false)
public abstract class HabitDatabase extends RoomDatabase {
    private static volatile HabitDatabase instance;
    
    public static synchronized HabitDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    HabitDatabase.class, "habit_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
    
    public abstract HabitDao habitDao();
}

// Main Layout file (activity_main.xml)

<!--
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:tools="http://schemas.android.com/tools"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                tools:context=".MainActivity">

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/nav_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"/>
    
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/habit_recycler_view"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_above="@id/nav_view"
        android:layout_alignParentTop="true"
        android:layout_marginTop="16dp"/>
    
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_add_habit"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentEnd="true"
        android:layout_alignParentBottom="true"
        android:layout_margin="16dp"
        android:src="@android:drawable/ic_input_add"/>
</RelativeLayout>
-->