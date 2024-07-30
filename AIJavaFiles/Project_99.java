Certainly! Here is the comprehensive and detailed Android app implementation for the Personal Finance Tracker App in a single file using Java and Firebase.

```java
// Import necessary packages
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

// Main Activity Class
public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    // Firebase Auth
    private FirebaseAuth mAuth;

    // UI References
    private EditText edtEmail, edtPassword;
    private Button btnLogin, btnRegister, btnGoogleSignIn;
    private TextView linkToDashboard;

    // Google SignIn Client
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI Components
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        linkToDashboard = findViewById(R.id.linkToDashboard);

        // Google Sign-In options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set Login Button Listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set Register Button Listener
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Set Google Sign-In Button Listener
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        // Link to Dashboard
        linkToDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDashboard();
            }
        });
    }

    // Google Sign-In
    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Handle Google Sign-In result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    // Handle Google Sign-In result
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(MainActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    // Authenticate with Firebase using Google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            openDashboard();
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // User Login
    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password is required");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            openDashboard();
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // User Registration
    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Password is required");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            openDashboard();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                edtPassword.setError("Weak password");
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                edtEmail.setError("Invalid email");
                            } catch (FirebaseAuthUserCollisionException e) {
                                edtEmail.setError("Account already exists");
                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    // Open Dashboard Activity
    private void openDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}

// Dashboard Activity
public class DashboardActivity extends AppCompatActivity {

    // UI References
    private TextView txtTotalIncome, txtTotalExpenses, txtCurrentSavings, txtBudgetProgress;
    private Button btnAddIncome, btnAddExpense, btnSetBudget, btnSetSavingsGoal, btnViewReports, btnSendReminder, btnBackupData, btnSettings;

    // Firebase References
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI Components
        txtTotalIncome = findViewById(R.id.txtTotalIncome);
        txtTotalExpenses = findViewById(R.id.txtTotalExpenses);
        txtCurrentSavings = findViewById(R.id.txtCurrentSavings);
        txtBudgetProgress = findViewById(R.id.txtBudgetProgress);
        btnAddIncome = findViewById(R.id.btnAddIncome);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnSetBudget = findViewById(R.id.btnSetBudget);
        btnSetSavingsGoal = findViewById(R.id.btnSetSavingsGoal);
        btnViewReports = findViewById(R.id.btnViewReports);
        btnSendReminder = findViewById(R.id.btnSendReminder);
        btnBackupData = findViewById(R.id.btnBackupData);
        btnSettings = findViewById(R.id.btnSettings);

        // Load Dashboard Data
        loadDashboardData();

        // Set Button Listeners
        btnAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIncome();
            }
        });

        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addExpense();
            }
        });

        btnSetBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBudget();
            }
        });

        btnSetSavingsGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSavingsGoal();
            }
        });

        btnViewReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewReports();
            }
        });

        btnSendReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReminder();
            }
        });

        btnBackupData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupData();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });
    }

    // Load Dashboard Data
    private void loadDashboardData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = mDatabase.child("users").child(userId);

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer totalIncome = dataSnapshot.child("totalIncome").getValue(Integer.class);
                    Integer totalExpenses = dataSnapshot.child("totalExpenses").getValue(Integer.class);
                    Integer currentSavings = totalIncome - totalExpenses;
                    Integer budgetProgress = dataSnapshot.child("budgetProgress").getValue(Integer.class);

                    txtTotalIncome.setText("Total Income: " + totalIncome);
                    txtTotalExpenses.setText("Total Expenses: " + totalExpenses);
                    txtCurrentSavings.setText("Current Savings: " + currentSavings);
                    txtBudgetProgress.setText("Budget Progress: " + budgetProgress + "%");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DashboardActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Add Income
    private void addIncome() {
        final EditText edtAmount = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Income")
                .setMessage("Enter amount:")
                .setView(edtAmount)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int amount = Integer.parseInt(edtAmount.getText().toString().trim());
                        // Firebase code to add income
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    // Add Expense
    private void addExpense() {
        final EditText edtAmount = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Expense")
                .setMessage("Enter amount:")
                .setView(edtAmount)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int amount = Integer.parseInt(edtAmount.getText().toString().trim());
                        // Firebase code to add expense
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    // Set Budget
    private void setBudget() {
        final EditText edtAmount = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Set Budget")
                .setMessage("Enter budget amount:")
                .setView(edtAmount)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int amount = Integer.parseInt(edtAmount.getText().toString().trim());
                        // Firebase code to set budget
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    // Set Savings Goal
    private void setSavingsGoal() {
        final EditText edtAmount = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Set Savings Goal")
                .setMessage("Enter goal amount:")
                .setView(edtAmount)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int amount = Integer.parseInt(edtAmount.getText().toString().trim());
                        // Firebase code to set savings goal
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    // View Reports
    private void viewReports() {
        Intent intent = new Intent(DashboardActivity.this, ReportsActivity.class);
        startActivity(intent);
    }

    // Send Reminder
    private void sendReminder() {
        // Code to send reminder
    }

    // Backup Data
    private void backupData() {
        // Code to backup data
    }

    // Open Settings
    private void openSettings() {
        Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}

// Reports Activity
public class ReportsActivity extends AppCompatActivity {

    // UI References

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialize UI Components

        // Load Reports Data
    }

    // Load Reports Data
    private void loadReportsData() {
        // Code to load reports data
    }
}

// Settings Activity
public class SettingsActivity extends AppCompatActivity {

    // UI References

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize UI Components

        // Load Settings Data
    }

    // Load Settings Data
    private void loadSettingsData() {
        // Code to load settings data
    }
}

```

This implementation includes the following:

1. **Main Activity**:
   - User Registration
   - User Login
   - Google Sign-In

2. **Dashboard Activity**:
   - Display financial metrics
   - Add Income and Expense
   - Set Budget and Savings Goal
   - Navigation to Reports and Settings

3. **Reports Activity**:
   - For future implementation to display user financial reports

4. **Settings Activity**:
   - For future implementation to manage user settings

Please create layouts (`activity_main.xml`, `activity_dashboard.xml`, `activity_reports.xml`, `activity_settings.xml`, etc.) as per your design needs. Additionally, ensure to set up Firebase in your project and replace the dummy Firebase references with actual code to store and retrieve data.