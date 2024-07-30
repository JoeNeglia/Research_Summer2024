Creating a comprehensive Android application in a single file is not the recommended practice as it adheres to bad coding practices and goes against modularity, readability, and maintainability. However, for demonstration purposes, I will provide a consolidated code block that combines the major functionalities. Please note that in a real-world scenario, you should separate concerns into different files and follow best practices.

Here's a consolidated example of a simplified version of the "Local Events Explorer" app, which will not cover all requirements due to the single-file constraint but will provide a good overview:

```java
// MainActivity.java

package com.example.localeventsexplorer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private EventViewModel eventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                Snackbar.make(findViewById(android.R.id.content), "Welcome, " + user.getDisplayName(), Snackbar.LENGTH_SHORT).show();
            } else {
                // User is signed out
                startSignIn();
            }
        };

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this);
        recyclerView.setAdapter(eventAdapter);

        // ViewModel to observe LiveData
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        eventViewModel.getEvents().observe(this, events -> eventAdapter.setEvents(events));
    
        Button btnSignOut = findViewById(R.id.btn_sign_out);
        btnSignOut.setOnClickListener(v -> signOut());
    
        createNotificationChannel();
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void startSignIn() {
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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // Successfully signed in
                Snackbar.make(findViewById(android.R.id.content), "Signed in as: " + user.getEmail(), Snackbar.LENGTH_SHORT).show();
            } else {
                // Sign in failed
                Snackbar.make(findViewById(android.R.id.content), "Sign in failed", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> Snackbar.make(findViewById(android.R.id.content), "Signed out", Snackbar.LENGTH_SHORT).show());
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("LocalEvents", "Local Events Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for local events");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

// EventViewModel.java
package com.example.localeventsexplorer;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class EventViewModel extends AndroidViewModel {
    private final EventRepository eventRepository;
    private final MutableLiveData<List<Event>> events;

    public EventViewModel(@NonNull Application application) {
        super(application);
        eventRepository = new EventRepository(application);
        events = eventRepository.getEvents();
    }

    public LiveData<List<Event>> getEvents() {
        return events;
    }
}

// EventRepository.java
package com.example.localeventsexplorer;

import android.app.Application;
import androidx.lifecycle.MutableLiveData;
import java.util.List;

public class EventRepository {
    private final MutableLiveData<List<Event>> events;

    public EventRepository(Application application) {
        events = new MutableLiveData<>();
        // Mock data - ideally fetched from third-party API
        events.setValue(mockData());
    }

    public MutableLiveData<List<Event>> getEvents() {
        return events;
    }

    private List<Event> mockData() {
        // This is where you'd fetch data from API
        return Arrays.asList(
                new Event("Music Concert", "2023-10-15", "Downtown", "Desc1"),
                new Event("Tech Conference", "2023-11-20", "Convention Center", "Desc2")
        );
    }
}

// EventAdapter.java
package com.example.localeventsexplorer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final LayoutInflater inflater;
    private List<Event> events; // Cached copy of events

    EventAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    @NonNull
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.recyclerview_item, parent, false);
        return new EventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        if (events != null) {
            Event current = events.get(position);
            holder.eventItemView.setText(current.getEventName());
        } else {
            holder.eventItemView.setText("No Event");
        }
    }

    void setEvents(List<Event> events){
        this.events = events;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (events != null)
            return events.size();
        else return 0;
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView eventItemView;

        private EventViewHolder(View itemView) {
            super(itemView);
            eventItemView = itemView.findViewById(R.id.textView);
            itemView.setOnClickListener(v -> {
                // onClick, show event details
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Event event = events.get(position);
                    Intent intent = new Intent(eventItemView.getContext(), EventDetailsActivity.class);
                    intent.putExtra("event", event);
                    eventItemView.getContext().startActivity(intent);
                }
            });
        }
    }
}

// EventDetailsActivity.java
package com.example.localeventsexplorer;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EventDetailsActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        Event event = getIntent().getParcelableExtra("event");

        TextView name = findViewById(R.id.name);
        TextView date = findViewById(R.id.date);
        TextView location = findViewById(R.id.location);
        WebView description = findViewById(R.id.description);

        name.setText(event.getEventName());
        date.setText(event.getEventDate());
        location.setText(event.getEventLocation());
        description.loadData(event.getEventDescription(), "text/html", "UTF-8");
    }
}

// Event.java
package com.example.localeventsexplorer;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
    private final String eventName;
    private final String eventDate;
    private final String eventLocation;
    private final String eventDescription;

    public Event(String eventName, String eventDate, String eventLocation, String eventDescription) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.eventDescription = eventDescription;
    }

    protected Event(Parcel in) {
        eventName = in.readString();
        eventDate = in.readString();
        eventLocation = in.readString();
        eventDescription = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public String getEventName() {
        return eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(eventName);
        dest.writeString(eventDate);
        dest.writeString(eventLocation);
        dest.writeString(eventDescription);
    }
}
```

Please note that external resources, like `activity_main.xml` and layout files, haven't been included due to the single-file constraint and complexity of UI components which require multiple files. The integration example provided here is greatly simplified. You should separate concerns into appropriate files and follow best coding practices for a real-world application development.