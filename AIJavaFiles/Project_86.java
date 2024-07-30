```java
// MainActivity.java
package com.example.campusapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.ui.IconGenerator;

import android.content.SharedPreferences;
import android.os.Parcelable;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import java.util.ArrayList;
// Import necessary libraries


public class MainActivity extends AppCompatActivity {

    // Firebase authentication instance
    private FirebaseAuth firebaseAuth;

    // Initialize the main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Add fragments to the main activity
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().replace(R.id.map_container, new CampusMapFragment()).commit();
            fragmentManager.beginTransaction().replace(R.id.directory_container, new DirectoryFragment()).commit();
            fragmentManager.beginTransaction().replace(R.id.event_container, new EventCalendarFragment()).commit();
            fragmentManager.beginTransaction().replace(R.id.communication_container, new CommunicationFragment()).commit();
            fragmentManager.beginTransaction().replace(R.id.emergency_container, new EmergencyAlertsFragment()).commit();
        }
    }

    // Check if the user is authenticated
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // Redirect to login if not authenticated
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }
}

// CampusMapFragment.java
package com.example.campusapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

public class CampusMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SearchView searchView;

    public CampusMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_campus_map, container, false);

        // Initialize search view
        searchView = rootView.findViewById(R.id.search_view);

        // Set up the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add markers for important locations
        LatLng library = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(library).title("Library"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(library));

        // Set up search function
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle search logic here
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
}

// DirectoryFragment.java
package com.example.campusapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;

import java.util.ArrayList;

public class DirectoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private DirectoryAdapter directoryAdapter;
    private DatabaseReference databaseReference;

    public DirectoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_directory, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        directoryAdapter = new DirectoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(directoryAdapter);

        // Fetch data from Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("directory");
        // Add Firebase value event listener to fetch data

        return rootView;
    }
}

// EventCalendarFragment.java
package com.example.campusapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class EventCalendarFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private DatabaseReference databaseReference;

    public EventCalendarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_event_calendar, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(new ArrayList<>());
        recyclerView.setAdapter(eventAdapter);

        // Fetch data from Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("events");
        // Add Firebase value event listener to fetch data

        return rootView;
    }
}

// CommunicationFragment.java
package com.example.campusapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;

public class CommunicationFragment extends Fragment {

    private DatabaseReference databaseReference;

    public CommunicationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_communication, container, false);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("messages");

        // Set up messaging UI components

        return rootView;
    }
}

// EmergencyAlertsFragment.java
package com.example.campusapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class EmergencyAlertsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EmergencyAdapter emergencyAdapter;
    private DatabaseReference databaseReference;

    public EmergencyAlertsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_emergency_alerts, container, false);

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.recycler_view_emergency);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emergencyAdapter = new EmergencyAdapter(new ArrayList<>());
        recyclerView.setAdapter(emergencyAdapter);

        // Fetch data from Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("emergencies");
        // Add Firebase value event listener to fetch data

        return rootView;
    }
}

// DirectoryAdapter.java
package com.example.campusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private List<Faculty> facultyList;

    public DirectoryAdapter(List<Faculty> facultyList) {
        this.facultyList = facultyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faculty, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set data to ViewHolder
        Faculty faculty = facultyList.get(position);
        holder.nameTextView.setText(faculty.getName());
        holder.departmentTextView.setText(faculty.getDepartment());
    }

    @Override
    public int getItemCount() {
        return facultyList.size();
    }

    // ViewHolder class to hold the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView departmentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_view_name);
            departmentTextView = itemView.findViewById(R.id.text_view_department);
        }
    }
}

// EventAdapter.java
package com.example.campusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set data to ViewHolder
        Event event = eventList.get(position);
        holder.titleTextView.setText(event.getTitle());
        holder.dateTextView.setText(event.getDate());
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class to hold the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_title);
            dateTextView = itemView.findViewById(R.id.text_view_date);
        }
    }
}

// EmergencyAdapter.java
package com.example.campusapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmergencyAdapter extends RecyclerView.Adapter<EmergencyAdapter.ViewHolder> {

    private List<Emergency> emergencyList;

    public EmergencyAdapter(List<Emergency> emergencyList) {
        this.emergencyList = emergencyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set data to ViewHolder
        Emergency emergency = emergencyList.get(position);
        holder.alertTextView.setText(emergency.getAlert());
        holder.dateTextView.setText(emergency.getDate());
    }

    @Override
    public int getItemCount() {
        return emergencyList.size();
    }

    // ViewHolder class to hold the views
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView alertTextView;
        public TextView dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            alertTextView = itemView.findViewById(R.id.text_view_alert);
            dateTextView = itemView.findViewById(R.id.text_view_date);
        }
    }
}

// LoginActivity.java
package com.example.campusapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.ui.IconGenerator;

import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    // Firebase authentication instance
    private FirebaseAuth firebaseAuth;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.edit_text_email);
        passwordEditText = findViewById(R.id.edit_text_password);
        Button loginButton = findViewById(R.id.button_login);

        // Set login button click listener
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login successful, redirect to main activity
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Login failed, display a message to the user
                        Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
```