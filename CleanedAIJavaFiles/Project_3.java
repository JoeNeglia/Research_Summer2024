import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarEntries;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

// EcoSaver Main Activity
public class EcoSaverMainActivity extends AppCompatActivity {

    private SQLiteDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize the database helper
        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        // Initialize user interface components
        EditText userName = findViewById(R.id.userName);
        EditText userAge = findViewById(R.id.userAge);
        EditText userLocation = findViewById(R.id.userLocation);
        EditText userLifestyle = findViewById(R.id.userLifestyle);
        Button registerButton = findViewById(R.id.registerButton);
        Button logActivityButton = findViewById(R.id.logActivityButton);
        PieChart carbonFootprintChart = findViewById(R.id.carbonFootprintChart);
        BarChart activityChart = findViewById(R.id.activityChart);
        
        // User Registration
        registerButton.setOnClickListener(view -> {
            String name = userName.getText().toString();
            int age = Integer.parseInt(userAge.getText().toString());
            String location = userLocation.getText().toString();
            String lifestyle = userLifestyle.getText().toString();
            registerUser(name, age, location, lifestyle);
        });

        // Log Daily Activity
        logActivityButton.setOnClickListener(view -> {
            // Placeholder values for daily logging to be replaced with actual inputs/activity tracking
            String activity = "Commuting";
            double carbonFootprint = 5.0;
            logDailyActivity(activity, carbonFootprint);
        });

        // Example charts setup
        setupPieChart(carbonFootprintChart);
        setupBarChart(activityChart);

        // Create notifications channel
        createNotificationChannel();
    }

    // User registration method
    private void registerUser(String name, int age, String location, String lifestyle) {
        database.execSQL("INSERT INTO users (name, age, location, lifestyle) VALUES (?, ?, ?, ?)", 
                new Object[]{name, age, location, lifestyle});
        Toast.makeText(this, "User Registered", Toast.LENGTH_SHORT).show();
    }

    // Log daily activity method
    private void logDailyActivity(String activity, double carbonFootprint) {
        database.execSQL("INSERT INTO activities (activity, carbon_footprint) VALUES (?, ?)", 
                new Object[]{activity, carbonFootprint});
        Toast.makeText(this, "Activity Logged", Toast.LENGTH_SHORT).show();
    }

    // Setup for PieChart visualizing Carbon Footprint
    private void setupPieChart(PieChart pieChart) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(30.0f, "Transport"));
        entries.add(new PieEntry(40.0f, "Energy"));
        
        // Note: setup dataset, formatter, and descriptions here
        Description desc = new Description();
        desc.setText("Carbon Footprint Analysis");
        pieChart.setDescription(desc);
    }

    // Setup for BarChart visualizing Activities
    private void setupBarChart(BarChart barChart) {
        ArrayList<BarEntries> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 5.0f));
        entries.add(new BarEntry(2, 3.0f));
        
        // Note: setup dataset, formatter, and descriptions here
        Description desc = new Description();
        desc.setText("Activity Tracking");
        barChart.setDescription(desc);
    }

    // Method to display and setup notifications
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "EcoSaverChannel";
            String description = "Channel for EcoSaver notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("ecosaver_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // DBHelper class for database management
    static class DBHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "EcoSaver.db";
        private static final int DATABASE_VERSION = 1;

        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        // Create required tables
        @Override
        public void onCreate(SQLiteDatabase db) {
            String CREATE_USERS_TABLE = "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, age INTEGER, location TEXT, lifestyle TEXT)";
            String CREATE_ACTIVITIES_TABLE = "CREATE TABLE activities (id INTEGER PRIMARY KEY AUTOINCREMENT, activity TEXT, carbon_footprint DOUBLE)";
            
            db.execSQL(CREATE_USERS_TABLE);
            db.execSQL(CREATE_ACTIVITIES_TABLE);
        }

        // Handle database upgrades
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS activities");
            onCreate(db);
        }
    }
}