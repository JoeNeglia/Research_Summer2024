Below is an example of the implementation of the "Nearby Events and Activities Aggregator" Android application in a single Java file with inline comments explaining the different parts of the code. While it's comprehensive, due to the nature of space and practical reasons, some parts such as API key details and exact API responses are simplified. This should give you a strong foundation to build upon and extend further.

```java
package com.example.nearbyevents;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;
// Import libraries for Retrofit, Volley, Picasso, Glide based on requirement

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth firebaseAuth;
    private GoogleMap map;

    // Replace with your actual keys
    private final String GOOGLE_API_KEY = "<GOOGLE_API_KEY>";
    private final String EVENTBRITE_API_KEY = "<EVENTBRITE_API_KEY>";
    private final String MEETUP_API_KEY = "<MEETUP_API_KEY>";

    private List<Event> eventsList = new ArrayList<>();
    private RecyclerView eventsRecyclerView;
    private EventsAdapter eventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up RecyclerView
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsAdapter = new EventsAdapter(eventsList);
        eventsRecyclerView.setAdapter(eventsAdapter);

        // Set up Google Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Check location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLastLocation();
        }

        // Retrieve and display events
        fetchEvents();
    }

    // Handle map ready callback
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    // Get the user's last known location
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                            loadNearbyEvents(latLng);
                        } else {
                            Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Fetch events from multiple sources
    private void fetchEvents() {
        // Example with Retrofit for one API - repeat for other APIs
        // Using Eventbrite as an example
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        // Call Eventbrite API (you need to define the endpoint in EventbriteService interface)
        eventbriteService.getEvents("Bearer " + EVENTBRITE_API_KEY, "location.latitude=37.7749&location.longitude=-122.4194&location.within=10mi")
                .enqueue(new Callback<EventResponse>() {
                    @Override
                    public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Event> events = response.body().getEvents();
                            eventsList.addAll(events);
                            eventsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<EventResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Error fetching events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Load nearby events on the map
    private void loadNearbyEvents(LatLng latLng) {
        for (Event event : eventsList) {
            LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());
            map.addMarker(new MarkerOptions().position(eventLocation).title(event.getName()));
        }
    }

    // Placeholder class for Event - you'd need to flesh out fields and getter/setter methods as per actual API data
    public class Event {
        private String name;
        private double latitude;
        private double longitude;

        public String getName() {
            return name;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }

    // Placeholder interface for EventbriteService - define endpoints as per actual API
    public interface EventbriteService {
        @GET("events/search/")
        Call<EventResponse> getEvents(@Header("Authorization") String authHeader, @Query("location") String location);
    }

    // Placeholder class for EventResponse - define fields as per actual API response
    public class EventResponse {
        private List<Event> events;

        public List<Event> getEvents() {
            return events;
        }
    }

    // Example Adapter for RecyclerView
    public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {
        private List<Event> eventList;

        EventsAdapter(List<Event> eventList) {
            this.eventList = eventList;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            Event event = eventList.get(position);
            holder.eventName.setText(event.getName());
            // Load event image using Glide or Picasso
            // Example: Picasso.get().load(event.getImageUrl()).into(holder.eventImage);
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        class EventViewHolder extends RecyclerView.ViewHolder {
            TextView eventName;
            ImageView eventImage;

            EventViewHolder(@NonNull View itemView) {
                super(itemView);
                eventName = itemView.findViewById(R.id.eventName);
                eventImage = itemView.findViewById(R.id.eventImage);
            }
        }
    }
    
    // Method to authenticate user with Google Sign-In
    private void googleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Start the sign-in intent
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Handle sign-in result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Process Google Sign-In result
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Handle sign-in failure
        }
    }

    // Authenticate with Firebase using Google account
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign-in fails
                            updateUI(null);
                        }
                    }
                });
    }

    // Update UI based on the authenticated user
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Navigate to the main content activity
            startActivity(new Intent(MainActivity.this, MainContentActivity.class));
            finish();
        } else {
            // Show error message
            Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Send Push Notification example
    private void sendPushNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "EVENT_CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_event)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Placeholder methods to add event to user's calendar and set reminders
    private void addToCalendar(Event event) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, event.getName())
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartTimeMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEndTimeMillis());
        startActivity(intent);
    }
}

// Layout XML (activity_main.xml) sample:
/*
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/eventsRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_above="@id/map"/>

    <fragment
        android:id="@+id/map"
        android:name="com.google.android.gms.maps.SupportMapFragment"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"/>
</RelativeLayout>
*/
```

### Notes:

- Add corresponding layouts, string resources, and drawables for UI elements.
- Define additional classes and methods to handle interactions, RSVP, purchase tickets, notifications, and other required features.
- Implement proper error handling and edge case scenarios.
- Ensure to include necessary permissions in the AndroidManifest.xml file.
- Make sure to manage API keys securely and follow best practices for API usage.