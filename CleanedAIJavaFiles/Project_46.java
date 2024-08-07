package com.example.healthandfitnesstracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Widgets
    private EditText emailInput, passwordInput, nameInput, ageInput, heightInput, weightInput, goalInput;
    private Button registerButton, loginButton, googleLoginButton;
    private TextView statusTextView, bmiTextView;
    private LoginButton facebookLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        emailInput = findViewById(R.id.editEmail);
        passwordInput = findViewById(R.id.editPassword);
        nameInput = findViewById(R.id.editName);
        ageInput = findViewById(R.id.editAge);
        heightInput = findViewById(R.id.editHeight);
        weightInput = findViewById(R.id.editWeight);
        goalInput = findViewById(R.id.editGoal);

        registerButton = findViewById(R.id.buttonRegister);
        loginButton = findViewById(R.id.buttonLogin);
        googleLoginButton = findViewById(R.id.buttonGoogleLogin);
        facebookLoginButton = findViewById(R.id.buttonFacebookLogin);

        statusTextView = findViewById(R.id.textStatus);
        bmiTextView = findViewById(R.id.textBMI);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleLogin();
            }
        });

        facebookLoginButton.setPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(/* Provide Facebook CallbackManager */);
    }

    private void registerUser() {
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString().trim();
        final String name = nameInput.getText().toString().trim();
        final String age = ageInput.getText().toString().trim();
        final String height = heightInput.getText().toString().trim();
        final String weight = weightInput.getText().toString().trim();
        final String goal = goalInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileUpdates);

                                // Saving additional user data to database
                                UserProfile userProfile = new UserProfile(name, Integer.parseInt(age), Float.parseFloat(height), Float.parseFloat(weight), goal);
                                mDatabase.child("users").child(user.getUid()).setValue(userProfile);

                                updateUI(user);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter your password!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void googleLogin() {
        // Implement Google Sign-In functionality
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("google.com");
        mAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Google Login failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void updateBMI(float height, float weight) {
        float heightInMeters = height / 100;
        float bmi = weight / (heightInMeters * heightInMeters);
        bmiTextView.setText(String.format("Your BMI: %.2f", bmi));
    }
    
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            statusTextView.setText("Welcome, " + user.getDisplayName());
            mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserProfile profile = snapshot.getValue(UserProfile.class);
                    if (profile != null) {
                        updateBMI(profile.getHeight(), profile.getWeight());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            statusTextView.setText("Please log in or register.");
        }
    }
    
    public static class UserProfile {
        private String name;
        private int age;
        private float height;
        private float weight;
        private String goal;
        
        public UserProfile() {}
        
        public UserProfile(String name, int age, float height, float weight, String goal) {
            this.name = name;
            this.age = age;
            this.height = height;
            this.weight = weight;
            this.goal = goal;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public float getHeight() {
            return height;
        }

        public float getWeight() {
            return weight;
        }

        public String getGoal() {
            return goal;
        }
    }
}