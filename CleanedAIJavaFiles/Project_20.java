package com.example.fitcompanion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String CHANNEL_ID = "fitness_channel";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userGreeting;
    private Button signOutButton;
    private Button workoutPlanButton;
    private Button nutritionButton;
    private Button progressButton;
    private Button tipsButton;
    private Button shareButton;

    //For Nutritionix API
    private OkHttpClient client;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        userGreeting = findViewById(R.id.user_greeting);
        signOutButton = findViewById(R.id.sign_out_button);
        workoutPlanButton = findViewById(R.id.workout_plan_button);
        nutritionButton = findViewById(R.id.nutrition_button);
        progressButton = findViewById(R.id.progress_button);
        tipsButton = findViewById(R.id.tips_button);
        shareButton = findViewById(R.id.share_button);

        client = new OkHttpClient();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Create Notification Channel for Push Notifications
        createNotificationChannel();

        // Initialize Firebase Auth
        if (mAuth.getCurrentUser() == null) {
            signIn();
        } else {
            updateUI(mAuth.getCurrentUser());
        }

        signOutButton.setOnClickListener(v -> signOut());
        
        //Handle other buttons separately
        workoutPlanButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WorkoutPlanActivity.class)));
        nutritionButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NutritionActivity.class)));
        progressButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProgressActivity.class)));
        tipsButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TipsActivity.class)));
        shareButton.setOnClickListener(v -> shareAchievement());
        
        // Firebase Cloud Messaging
        FirebaseMessaging.getInstance().subscribeToTopic("fitness_tips")
            .addOnCompleteListener(task -> {
                String msg = task.isSuccessful() ? "Subscribed" : "Subscription failed";
                showNotification(msg);
            });
    }

    // FirebaseUI for Sign-In
    private void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN
        );
    }

    // Handling FirebaseUI Sign-In Result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateUI(user);
            } else {
                // Sign in failed
            }
        }
    }

    // Update UI with user info
    private void updateUI(FirebaseUser user) {
        String greetingText = "Hello, " + Objects.requireNonNull(user).getDisplayName();
        userGreeting.setText(greetingText);
    }

    // Sign Out
    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> signIn());
    }

    // Create Notification Channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Fitness Channel";
            String description = "Channel for fitness notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
        }
    }

    // Show Notification
    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("FitCompanion")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(100, builder.build());
    }

    // Share Achievement
    private void shareAchievement() {
        String shareText = "Just completed my workout on FitCompanion!";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    // Get Wellness Tips using OkHttp
    private void getWellnessTips() {
        Request request = new Request.Builder()
                .url("https://api.example.com/getTips")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = Objects.requireNonNull(response.body()).string();

                    MainActivity.this.runOnUiThread(() -> userGreeting.setText(myResponse));
                }
            }
        });
    }
    
    // Other Activities would be coded similarly: Custom workout plans, nutrition tracking, progress tracking, etc.
    // Here's a skeleton example of one such activity.

    public static class WorkoutPlanActivity extends AppCompatActivity {

        private FirebaseFirestore db;
        private TextView workoutPlanText;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_workout_plan);

            workoutPlanText = findViewById(R.id.workout_plan_text);
            db = FirebaseFirestore.getInstance();

            loadWorkoutPlan();
        }

        private void loadWorkoutPlan() {
            CollectionReference plansRef = db.collection("workout_plans");
            plansRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    StringBuilder plans = new StringBuilder();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        plans.append(document.getString("plan")).append("\n");
                    }
                    workoutPlanText.setText(plans.toString());
                } else {
                    workoutPlanText.setText("Failed to load plans.");
                }
            });
        }
    }

    // Similar setup can be done for NutritionActivity, ProgressActivity, and TipsActivity.
    // Each would connect to Firebase Firestore or an external API (like Nutritionix) as per requirement.
}