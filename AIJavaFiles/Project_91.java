```java
// filename: EventHubApp.java

// Import necessary libraries

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Main activity, handles login and main app logic
public class EventHubApp extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            signIn();
        }

        setupNotificationChannel();
        setupRecyclerView();
        setupFirebaseDatabase();
        setupMapView();

        Button createEventButton = findViewById(R.id.createEventButton);
        createEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreateEventActivity.class);
            startActivity(intent);
        });
    }

    // Method to handle Firebase UI Sign-In
    private void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Setup RecyclerView for displaying events
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.event_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>());
        recyclerView.setAdapter(eventAdapter);
    }

    // Setup Firebase database to read events
    private void setupFirebaseDatabase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("events");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> events = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Event event = dataSnapshot.getValue(Event.class);
                    events.add(event);
                }
                eventAdapter.updateEvents(events);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EventHubApp", "Database error: " + error.getMessage());
            }
        });
    }

    // Setup notification channel for Android O and above
    private void setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "EVENTHUB_CHANNEL",
                    "EventHub Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("events")
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("EventHubApp", "Subscription to events topic failed");
                    }
                });
    }

    // Setup Google Map view
    private void setupMapView() {
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.getMapAsync(googleMap -> {
            LatLng currentLoc = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(currentLoc).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
        });
    }
}

// Adapter for RecyclerView to display events in the main activity
class EventAdapter extends RecyclerView.Adapter<EventViewHolder> {

    private List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item_view, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }
}

// ViewHolder for holding event views in the RecyclerView
class EventViewHolder extends RecyclerView.ViewHolder {

    private TextView titleTextView;
    private TextView descTextView;

    public EventViewHolder(@NonNull View itemView) {
        super(itemView);
        titleTextView = itemView.findViewById(R.id.event_title);
        descTextView = itemView.findViewById(R.id.event_description);
    }

    public void bind(Event event) {
        titleTextView.setText(event.getTitle());
        descTextView.setText(event.getDescription());
    }
}

// Data class for Event objects
class Event {
    private String id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String location;

    public Event() {
    }

    public Event(String id, String title, String description, String date, String time, String location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

// Activity for creating new events
class CreateEventActivity extends Activity {

    private EditText titleEditText;
    private EditText descEditText;
    private EditText dateEditText;
    private EditText timeEditText;
    private EditText locationEditText;
    private Button saveButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        titleEditText = findViewById(R.id.titleEditText);
        descEditText = findViewById(R.id.descEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        locationEditText = findViewById(R.id.locationEditText);
        saveButton = findViewById(R.id.saveButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("events");

        saveButton.setOnClickListener(view -> {
            saveEvent();
        });
    }

    // Method to save the event in Firebase Database
    private void saveEvent() {
        String id = databaseReference.push().getKey();
        String title = titleEditText.getText().toString();
        String description = descEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();
        String location = locationEditText.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(date) ||
                TextUtils.isEmpty(time) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
        } else {
            Event event = new Event(id, title, description, date, time, location);
            if (id != null) {
                databaseReference.child(id).setValue(event);
                Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error creating event", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

// Note: This is a basic comprehensive implementation without actual layout XML files and additional features such as image uploading, event notifications, and comments. Further elaboration and integration would be required for a complete app.
```