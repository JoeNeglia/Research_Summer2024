# Campus Buddy - University Event and Resource Tracker

```java
package com.campusbuddy;

// Imports
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.campusbuddy.models.Event;
import com.campusbuddy.models.Resource;
import com.campusbuddy.models.User;
import com.campusbuddy.adapters.EventAdapter;
import com.campusbuddy.adapters.ResourceAdapter;
import com.campusbuddy.adapters.SocialFeedAdapter;
import com.campusbuddy.adapters.ChatAdapter;
import com.campusbuddy.fragments.DashboardFragment;
import com.campusbuddy.fragments.EventFragment;
import com.campusbuddy.fragments.ProfileFragment;
import com.campusbuddy.utils.NotificationsUtils;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// MainActivity
public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore db;
    private RecyclerView eventRecyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private RecyclerView resourceRecyclerView;
    private ResourceAdapter resourceAdapter;
    private List<Resource> resourceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerViews
        eventRecyclerView = findViewById(R.id.event_recycler_view);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList);
        eventRecyclerView.setAdapter(eventAdapter);
        
        resourceRecyclerView = findViewById(R.id.resource_recycler_view);
        resourceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        resourceList = new ArrayList<>();
        resourceAdapter = new ResourceAdapter(this, resourceList);
        resourceRecyclerView.setAdapter(resourceAdapter);

        // Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivityForResult(
                AuthUI.getInstance()
                     .createSignInIntentBuilder()
                     .setAvailableProviders(Arrays.asList(
                         new AuthUI.IdpConfig.EmailBuilder().build(),
                         new AuthUI.IdpConfig.GoogleBuilder().build(),
                         new AuthUI.IdpConfig.FacebookBuilder().build()))
                     .build(),
                RC_SIGN_IN);
        } else {
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser user) {
        TextView userName = findViewById(R.id.user_name);
        ImageView profileImage = findViewById(R.id.profile_image);

        userName.setText(user.getDisplayName());
        Glide.with(this).load(user.getPhotoUrl()).into(profileImage);

        loadEvents();
        loadResources();
    }

    private void loadEvents() {
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Event event = document.toObject(Event.class);
                                eventList.add(event);
                            }
                            eventAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void loadResources() {
        db.collection("resources")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Resource resource = document.toObject(Resource.class);
                                resourceList.add(resource);
                            }
                            resourceAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            } else {
                // Sign-in failed
                finish();
            }
        }
    }
}

// Event Model
public class Event {
    private String id;
    private String name;
    private String date;
    private String type;
    private String description;

    // Empty constructor
    public Event() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

// Resource Model
public class Resource {
    private String id;
    private String name;
    private String location;
    private String contact;

    // Empty constructor
    public Resource() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
}

// User Model
public class User {
    private String id;
    private String name;
    private String email;
    private String bio;

    // Empty constructor
    public User() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}

// EventAdapter
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.name.setText(event.getName());
        holder.date.setText(event.getDate());
        holder.type.setText(event.getType());
        holder.description.setText(event.getDescription());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView date;
        public TextView type;
        public TextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.event_name);
            date = itemView.findViewById(R.id.event_date);
            type = itemView.findViewById(R.id.event_type);
            description = itemView.findViewById(R.id.event_description);
        }
    }
}

// ResourceAdapter
public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ViewHolder> {
    private Context context;
    private List<Resource> resourceList;

    public ResourceAdapter(Context context, List<Resource> resourceList) {
        this.context = context;
        this.resourceList = resourceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Resource resource = resourceList.get(position);
        holder.name.setText(resource.getName());
        holder.location.setText(resource.getLocation());
        holder.contact.setText(resource.getContact());
    }

    @Override
    public int getItemCount() {
        return resourceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView location;
        public TextView contact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.resource_name);
            location = itemView.findViewById(R.id.resource_location);
            contact = itemView.findViewById(R.id.resource_contact);
        }
    }
}

// NotificationsUtils
public class NotificationsUtils {
    public static void sendNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "default_channel_id")
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_notification_large))
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(0, notificationBuilder.build());
    }
}

// MyFirebaseMessagingService
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Assuming there is a title and body in the message
        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();

        // Send the notification
        NotificationsUtils.sendNotification(getApplicationContext(), title, body);
    }
}
```

Our project consolidates functionalities such as user authentication, fetching and displaying events/resources, sending notifications, and adhering to Firebase for backend services. Due to a comprehensive project nature, deployment nuances and other advanced features would require more elaborate implementation, particularly around UI/UX, error handling, and further testing.