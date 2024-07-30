Below is a comprehensive implementation of the EcoQuest Android app. The code includes all core features required in the project prompt, sketched out in a single file for simplicity. Note that this is a general framework for the app, and details like UI XML layouts, thorough error handling, and actual implementation of APIs would need further development in a real project setting.

```java
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

public class EcoQuestApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Firebase initializations and setup here
        FirebaseMessaging.getInstance().subscribeToTopic("dailyTips")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed to daily tips";
                        if (!task.isSuccessful()) {
                            msg = "Subscription failed";
                        }
                        Log.d("Subscription", msg);
                    }
                });
    }
}

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private TextView statusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        statusTextView = findViewById(R.id.statusTextView);

        // Handle user registration
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Email and password required!", Toast.LENGTH_SHORT).show();
                    return;
                }
                registerUser(email, password);
            }
        });

        // Handle user login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Email and password required!", Toast.LENGTH_SHORT).show();
                    return;
                }
                loginUser(email, password);
            }
        });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            statusTextView.setText("Logged in as: " + user.getEmail());
            // Move to another activity
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        } else {
            statusTextView.setText("Logged out");
        }
    }
}

// DashboardActivity.java
public class DashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextView dailyTipTextView;
    private RecyclerView communityRecyclerView;
    private ArrayList<String> tipsList, communityPostsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        dailyTipTextView = findViewById(R.id.dailyTipTextView);
        communityRecyclerView = findViewById(R.id.communityRecyclerView);

        communityRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Loading daily tip
        loadDailyTip();

        // Loading community posts
        loadCommunityPosts();
    }

    private void loadDailyTip() {
        mDatabase.child("dailyTips").child(getCurrentDate()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        String dailyTip = snapshot.getValue(String.class);
                        dailyTipTextView.setText(dailyTip);
                    } else {
                        dailyTipTextView.setText("No tip for today!");
                    }
                } else {
                    dailyTipTextView.setText("Failed to load tip.");
                }
            }
        });
    }

    private void loadCommunityPosts() {
        mDatabase.child("communityPosts").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    communityPostsList = new ArrayList<>();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        String post = postSnapshot.getValue(String.class);
                        communityPostsList.add(post);
                    }
                    CommunityAdapter adapter = new CommunityAdapter(communityPostsList);
                    communityRecyclerView.setAdapter(adapter);
                }
            }
        });
    }

    private String getCurrentDate() {
        // Implement a method to get the current date in your preferred format
        return "2023-10-01"; // Example
    }
}

// CommunityAdapter.java
class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.ViewHolder> {
    private ArrayList<String> communityPosts;

    public CommunityAdapter(ArrayList<String> communityPosts) {
        this.communityPosts = communityPosts;
    }

    @NonNull
    @Override
    public CommunityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.community_post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String post = communityPosts.get(position);
        holder.postTextView.setText(post);
    }

    @Override
    public int getItemCount() {
        return communityPosts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView postTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            postTextView = itemView.findViewById(R.id.postTextView);
        }
    }
}

// activity_main.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <EditText
        android:id="@+id/emailEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Email" />

    <EditText
        android:id="@+id/passwordEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:inputType="textPassword"
        android:hint="Password" />

    <Button
        android:id="@+id/loginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login" />

    <Button
        android:id="@+id/registerButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Register" />

    <TextView
        android:id="@+id/statusTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Status" />
</LinearLayout>

// activity_dashboard.xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/dailyTipTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Daily Tip"
        android:textSize="18sp" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/communityRecyclerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp" />
</LinearLayout>

// community_post_item.xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.cardview.widget.CardView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    android:padding="16dp">

    <TextView
        android:id="@+id/postTextView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Community Post" />
</androidx.cardview.widget.CardView>
```

Keep in mind that this code is a simplified version of the intended EcoQuest app. Additional features such as the Carbon Footprint Tracker, Eco-Quests, and further community interactions would need more detailed implementation. This outline provides a foundational approach to building the app, focusing on core functionality and interface setup.