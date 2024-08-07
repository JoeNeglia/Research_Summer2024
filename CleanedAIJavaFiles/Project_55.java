package com.example.personalfinancemanager;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Firebase Authentication and Database reference
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    // UI elements
    private TextView welcomeTextView, totalIncomeTextView, totalExpensesTextView;
    private RecyclerView transactionRecyclerView;
    private Button addTransactionButton, logoutButton;
    
    private static final int RC_SIGN_IN = 123;

    // Shared Preferences for local storage
    private SharedPreferences sharedPreferences;
    private final String PREF_NAME = "personal_finance_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI elements
        welcomeTextView = findViewById(R.id.welcomeTextView);
        totalIncomeTextView = findViewById(R.id.totalIncomeTextView);
        totalExpensesTextView = findViewById(R.id.totalExpensesTextView);
        transactionRecyclerView = findViewById(R.id.transactionRecyclerView);
        addTransactionButton = findViewById(R.id.addTransactionButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Set up RecyclerView for transactions
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Shared Preferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Set up authentication listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    welcomeTextView.setText("Welcome, " + user.getDisplayName());
                    setupUserData(user.getUid());
                } else {
                    // User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        // Listen for add transaction button click
        addTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTransactionDialog();
            }
        });

        // Listen for logout button click
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void showAddTransactionDialog() {
        // Implementation of this method to show a dialog for adding transactions
        // For simplicity, let's assume we just add a hardcoded transaction
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = mDatabase.child("users").child(userId).child("transactions");

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Transaction transaction = new Transaction("Salary", 1000, "Income", date);
        userRef.push().setValue(transaction);

        Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                welcomeTextView.setText("Welcome, " + user.getDisplayName());
                setupUserData(user.getUid());
            } else {
                // Sign-in failed, handle the error
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupUserData(String userId) {
        // Get user data from Firebase Database and update UI accordingly
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        userRef.child("totalIncome").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long totalIncome = dataSnapshot.getValue(Long.class);
                if (totalIncome != null) {
                    totalIncomeTextView.setText("Total Income: $" + totalIncome);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        userRef.child("totalExpenses").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long totalExpenses = dataSnapshot.getValue(Long.class);
                if (totalExpenses != null) {
                    totalExpensesTextView.setText("Total Expenses: $" + totalExpenses);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });

        // Retrieve and display the transactions
        userRef.child("transactions").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Code to populate RecyclerView with the list of transactions
                // For simplicity, let's just log the transactions
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    // TODO: Add transaction to RecyclerView adapter
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
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

    // Defined Transaction class to hold transaction data
    public static class Transaction {
        public String description;
        public long amount;
        public String category;
        public String date;

        // Empty constructor required for Firebase
        public Transaction() {
        }

        public Transaction(String description, long amount, String category, String date) {
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }
    }
}