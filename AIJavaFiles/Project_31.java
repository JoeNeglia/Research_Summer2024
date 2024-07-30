Let's develop the "Lost & Found App" project as specified in the prompt. This will be a comprehensive implementation in Java for an Android application. The project will use Firebase for backend services and Google Maps API for geolocation features. The following code provides a comprehensive approach with detailed comments.

NOTE: Due to the length and complexity, this implementation will focus on key components; however, it is divided into several functions/classes for readability purposes.

```java
package com.example.lostfound;

import android.app.Application;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LostFoundApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true); // Offline capabilities
    }
}
```

```java
// models/User.java
package com.example.lostfound.models;

public class User {
    private String id;
    private String email;
    private String name;
    private String profilePictureUrl;

    public User() {}

    // Getters and Setters
}
```

```java
// models/Item.java
package com.example.lostfound.models;

import java.util.Date;

public class Item {
    private String id;
    private String description;
    private String category;
    private String photoUrl;
    private double latitude;
    private double longitude;
    private Date date;
    private String contactInfo;
    private String userId;

    public Item() {}

    // Getters and Setters
}
```

```java
// activities/LoginActivity.java
package com.example.lostfound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lostfound.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.register);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        
        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                registerUser();
            }
        });
    }

    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            } else {
                Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
        });
    }
    
    private void registerUser(){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }
}
```

```java
// activities/RegisterActivity.java
package com.example.lostfound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lostfound.R;
import com.example.lostfound.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText emailField, passwordField, nameField;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        nameField = findViewById(R.id.name);
        registerButton = findViewById(R.id.register);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        String name = nameField.getText().toString();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                createUserProfile(user, name);
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUserProfile(FirebaseUser firebaseUser, String name) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(name);

        mDatabase.child(firebaseUser.getUid()).setValue(user)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                updateUI(firebaseUser);
            } else {
                Toast.makeText(RegisterActivity.this, "Profile creation failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
```

```java
// activities/MainActivity.java
package com.example.lostfound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lostfound.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button reportLostButton, reportFoundButton, searchButton, profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        reportLostButton = findViewById(R.id.report_lost_button);
        reportFoundButton = findViewById(R.id.report_found_button);
        searchButton = findViewById(R.id.search_button);
        profileButton = findViewById(R.id.profile_button);
        
        reportLostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportLostActivity.class);
                startActivity(intent);
            }
        });
        
        reportFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportFoundActivity.class);
                startActivity(intent);
            }
        });
        
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
```

```java
// activities/ReportLostActivity.java
package com.example.lostfound.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.lostfound.R;
import com.example.lostfound.models.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;

public class ReportLostActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private EditText descriptionField, categoryField, contactInfoField;
    private Button submitButton;
    private ImageView imageView;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_lost);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("items");
        mStorage = FirebaseStorage.getInstance().getReference();

        descriptionField = findViewById(R.id.description);
        categoryField = findViewById(R.id.category);
        contactInfoField = findViewById(R.id.contact_info);
        imageView = findViewById(R.id.imageView);
        submitButton = findViewById(R.id.submit_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportLostItem();
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> task = fusedLocationClient.getLastLocation();
                task.addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("LOCATION_ERROR", "Unable to get location", e);
        }
    }

    private void reportLostItem() {
        String description = descriptionField.getText().toString();
        String category = categoryField.getText().toString();
        String contactInfo = contactInfoField.getText().toString();

        if (description.isEmpty() || category.isEmpty() || contactInfo.isEmpty() || currentLocation == null) {
            Toast.makeText(ReportLostActivity.this, "Please fill all fields and allow location access.", Toast.LENGTH_SHORT).show();
            return;
        }

        Item item = new Item();
        item.setDescription(description);
        item.setCategory(category);
        item.setContactInfo(contactInfo);
        item.setLatitude(currentLocation.getLatitude());
        item.setLongitude(currentLocation.getLongitude());
        item.setDate(new Date());
        item.setUserId(mAuth.getCurrentUser().getUid());

        // Code for uploading image to Firebase storage is omitted for brevity.

        mDatabase.push().setValue(item)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ReportLostActivity.this, "Item reported successfully.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ReportLostActivity.this, "Failed to report item.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(ReportLostActivity.this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
```

```java
// activities/SearchActivity.java
package com.example.lostfound.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lostfound.R;
import com.example.lostfound.adapters.ItemAdapter;
import com.example.lostfound.models.Item;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private EditText searchField;
    private Button searchButton;
    private ListView resultsListView;
    private List<Item> itemList;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mDatabase = FirebaseDatabase.getInstance().getReference("items");
        searchField = findViewById(R.id.search_field);
        searchButton = findViewById(R.id.search_button);
        resultsListView = findViewById(R.id.results_list_view);
        
        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(this, R.layout.item_layout, itemList);
        resultsListView.setAdapter(itemAdapter);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchItems();
            }
        });
    }

    private void searchItems() {
        String query = searchField.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(SearchActivity.this, "Enter search term.", Toast.LENGTH_SHORT).show();
            return;
        }

        mDatabase.orderByChild("description").startAt(query).endAt(query + "\uf8ff")
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Item item = snapshot.getValue(Item.class);
                    itemList.add(item);
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SearchActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
```

To use this code:

1. Configure Firebase in your project.
2. Add appropriate dependencies for Firebase, Google Maps, etc., in `build.gradle`.
3. Create XML layouts for these activities (layouts here are referenced as `activity_login.xml`, `activity_register.xml`, `activity_main.xml`, `activity_report_lost.xml`, `activity_search.xml`).
4. Ensure you have permission handling for location and necessary permissions declared in `AndroidManifest.xml`.

Remaining sections, such as image uploading, in-app messaging, notifications, and the admin dashboard can follow similar detailed implementations as provided above, making use of Firebase's various tools like Storage, Firestore, and Cloud Messaging.

Given the scope of the project, this is a coherent framework that hits the main points of the required features whilst being modular enough to iteratively expand and improve upon.