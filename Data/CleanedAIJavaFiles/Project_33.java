import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


// Main Activity class for Android Smart Home Control Application
public class MainActivity extends AppCompatActivity {

    // UI Components
    private EditText emailInput, passwordInput;
    private Button loginButton, registerButton;
    private TextView statusTextView, deviceStatusTextView;

    // Firebase Authentication and Database
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // OkHttpClient for REST API calls
    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        statusTextView = findViewById(R.id.status_text_view);
        deviceStatusTextView = findViewById(R.id.device_status_text_view);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set click listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(emailInput.getText().toString(), passwordInput.getText().toString());
            }
        });

        // Set click listener for register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(emailInput.getText().toString(), passwordInput.getText().toString());
            }
        });

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI(currentUser);
        }
    }

    // Method to register a new user
    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w("Register", "createUserWithEmail:failure", task.getException());
                        statusTextView.setText("Authentication failed.");
                    }
                });
    }

    // Method to log in an existing user
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w("Login", "signInWithEmail:failure", task.getException());
                        statusTextView.setText("Authentication failed.");
                    }
                });
    }

    // Method to update the UI after user is logged in
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            statusTextView.setText("User: " + user.getEmail());
            loadDevices();
        }
    }

    // Method to load devices from the database
    private void loadDevices() {
        mDatabase.child("devices").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                StringBuilder deviceStatuses = new StringBuilder();
                for (DataSnapshot deviceSnapshot : dataSnapshot.getChildren()) {
                    String deviceName = deviceSnapshot.child("name").getValue(String.class);
                    String deviceStatus = deviceSnapshot.child("status").getValue(String.class);
                    deviceStatuses.append(deviceName).append(": ").append(deviceStatus).append("\n");
                }
                deviceStatusTextView.setText(deviceStatuses.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Database", "loadDevices:onCancelled", databaseError.toException());
            }
        });
    }

    // Method to control a specific device
    private void controlDevice(String deviceId, String action) {
        String url = "https://smart-home-api.com/device/" + deviceId + "/control";

        Map<String, String> jsonParams = new HashMap<>();
        jsonParams.put("action", action);

        RequestBody body = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                new JSONObject(jsonParams).toString()
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ControlDevice", "Failed to control device", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }
                try {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    Log.i("ControlDevice", "Response: " + jsonResponse.toString());
                } catch (JSONException e) {
                    Log.e("ControlDevice", "Failed to parse response", e);
                }
            }
        });
    }
}