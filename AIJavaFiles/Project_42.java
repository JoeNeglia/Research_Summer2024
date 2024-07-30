```java
// File: MyFitnessHubApp.java
// Ensure you have the necessary dependencies in your build.gradle file:
// dependencies {
//    implementation 'com.google.firebase:firebase-auth:20.0.4'
//    implementation 'com.google.android.gms:play-services-auth:19.0.0'
//    implementation 'androidx.appcompat:appcompat:1.3.0'
//    implementation 'com.google.android.material:material:1.3.0'
//    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
//    implementation 'com.google.android.gms:play-services-fitness:20.0.0'
//    implementation 'androidx.recyclerview:recyclerview:1.2.0'
//    // Other necessary dependencies...
// }

// Import required packages
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;

import java.util.ArrayList;
import java.util.List;

// MainActivity class for user authentication
public class MainActivity extends AppCompatActivity {
    // Firebase authentication instance
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
            
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up sign-in button
        Button signInButton = findViewById(R.id.btn_google_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }
    
    // Sign-in method
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Google Sign In failed
        }
    }
    
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<androidx.work.Operation.State.SUCCESS>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Go to HomeActivity
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        // Handle error
                    }
                }
            });
    }
}

// HomeActivity class shows home screen after login
class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Setup buttons for different features
        Button btnWorkoutPlan = findViewById(R.id.btn_workout_plan);
        Button btnDietPlan = findViewById(R.id.btn_diet_plan);
        Button btnActivityTracking = findViewById(R.id.btn_activity_tracking);
        
        btnWorkoutPlan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, WorkoutPlanActivity.class));
            }
        });
        
        btnDietPlan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, DietPlanActivity.class));
            }
        });
        
        btnActivityTracking.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ActivityTrackingActivity.class));
            }
        });

        // Initialize Google Fit
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
            .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE);
    }
}

// WorkoutPlanActivity class handles workout plans
class WorkoutPlanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plan);
        
        // Setup questionnaire and custom workout generation logic
        
        // RecyclerView for showing generated workout plans
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Example workout list
        List<WorkoutPlan> workoutPlans = new ArrayList<>();
        // Add workout plans based on questionnaire results
        
        // Set adapter with workout plans
        WorkoutPlanAdapter adapter = new WorkoutPlanAdapter(workoutPlans);
        recyclerView.setAdapter(adapter);
    }
}

// DietPlanActivity class for personalized meal plans
class DietPlanActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_plan);
        
        // Setup diet plan logic
        // RecyclerView for showing meal plans
    }
}

// ActivityTrackingActivity class for tracking activities
class ActivityTrackingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_tracking);
        
        // Set up Google Fit activity tracking and logging functionality        
    }
}

// WorkoutPlan class representing a workout plan
class WorkoutPlan {
    private String name;
    private String description;
    private String videoUrl;
    
    // Constructor, getters, setters
}

// WorkoutPlanAdapter class for RecyclerView to show workout plans
class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.ViewHolder> {
    private List<WorkoutPlan> workoutPlans;
    
    // Constructor
    public WorkoutPlanAdapter(List<WorkoutPlan> workoutPlans) {
        this.workoutPlans = workoutPlans;
    }
    
    @Override
    public WorkoutPlanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_plan, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(WorkoutPlanAdapter.ViewHolder holder, int position) {
        // Bind workout plan data to views
    }
    
    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView description;
        
        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
        }
    }
}

// Other necessary class implementations similar to the above structure
// Diet adaptation, manual activity logging, progress monitoring, and other features follow similarly structured class and activity implementations.
```

Note: Only snippet layout files (e.g., `activity_main.xml`, `activity_home.xml`, etc.) and certain classes (like `WorkoutPlan`, and `WorkoutPlanAdapter`) are included. For brevity, the implementation overview provides guidance for additional components such as diet plans, progress monitoring, and social features. Ensure to structure each activity, class, and feature following the provided format.