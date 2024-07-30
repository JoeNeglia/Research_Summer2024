Sure, below is a comprehensive implementation of a "Smart Expense Tracker and Budget Planner" application all written within a single file. Note that this is a comprehensive but illustrative example that covers core functionalities. In reality, an Android app of this complexity would typically be split across multiple files for better structure and maintainability.

```java
package com.example.smartexpensetracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SmartExpenseTracker extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView totalIncome, totalExpenses, remainingBudget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        totalIncome = findViewById(R.id.totalIncome);
        totalExpenses = findViewById(R.id.totalExpenses);
        remainingBudget = findViewById(R.id.remainingBudget);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                return true;
            case R.id.nav_expenses:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExpenseFragment()).commit();
                return true;
            case R.id.nav_budget:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BudgetFragment()).commit();
                return true;
        }
        return false;
    };

    public static class LoginActivity extends AppCompatActivity {
        private EditText emailEditText, passwordEditText;
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
            mAuth = FirebaseAuth.getInstance();

            emailEditText = findViewById(R.id.email);
            passwordEditText = findViewById(R.id.password);

            findViewById(R.id.loginButton).setOnClickListener(v -> loginUser());
            findViewById(R.id.registerButton).setOnClickListener(v -> startActivity(new Intent(this, RegistrationActivity.class)));
        }

        private void loginUser() {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Invalid Email");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password Required");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, SmartExpenseTracker.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    public static class RegistrationActivity extends AppCompatActivity {
        private EditText emailEditText, passwordEditText, confirmPasswordEditText;
        private FirebaseAuth mAuth;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register);
            mAuth = FirebaseAuth.getInstance();

            emailEditText = findViewById(R.id.email);
            passwordEditText = findViewById(R.id.password);
            confirmPasswordEditText = findViewById(R.id.confirmPassword);

            findViewById(R.id.registerButton).setOnClickListener(v -> createAccount());
        }

        private void createAccount() {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Enter a valid email address");
                return;
            }

            if (TextUtils.isEmpty(password) || password.length() < 6) {
                passwordEditText.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordEditText.setError("Passwords do not match");
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(RegistrationActivity.this, SmartExpenseTracker.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    public static class DashboardFragment extends Fragment {

        private FirebaseFirestore db;
        private FirebaseAuth mAuth;
        private TextView totalIncome, totalExpenses, remainingBudget, recentTransactions;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            totalIncome = view.findViewById(R.id.totalIncome);
            totalExpenses = view.findViewById(R.id.totalExpenses);
            remainingBudget = view.findViewById(R.id.remainingBudget);
            recentTransactions = view.findViewById(R.id.recentTransactions);

            loadDashboardData();

            return view;
        }

        private void loadDashboardData() {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    double income = documentSnapshot.getDouble("totalIncome");
                    double expenses = documentSnapshot.getDouble("totalExpenses");
                    double budget = documentSnapshot.getDouble("totalBudget");
                    totalIncome.setText(String.valueOf(income));
                    totalExpenses.setText(String.valueOf(expenses));
                    remainingBudget.setText(String.valueOf(budget - expenses));
                    recentTransactions.setText(documentSnapshot.getString("recentTransactions"));
                }
            });
        }
    }

    public static class ExpenseFragment extends Fragment {

        private FirebaseFirestore db;
        private FirebaseAuth mAuth;
        private EditText amount, category, date, notes;
        private ImageView receiptImage;
        private Button saveButton;
        private RecyclerView expenseList;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_expense, container, false);

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            amount = view.findViewById(R.id.amount);
            category = view.findViewById(R.id.category);
            date = view.findViewById(R.id.date);
            notes = view.findViewById(R.id.notes);
            receiptImage = view.findViewById(R.id.receiptImage);
            saveButton = view.findViewById(R.id.saveButton);
            expenseList = view.findViewById(R.id.expenseList);
            expenseList.setLayoutManager(new LinearLayoutManager(getContext()));

            saveButton.setOnClickListener(v -> saveExpense());

            return view;
        }

        private void saveExpense() {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            Map<String, Object> expense = new HashMap<>();
            expense.put("amount", Double.parseDouble(amount.getText().toString()));
            expense.put("category", category.getText().toString());
            expense.put("date", date.getText().toString());
            expense.put("notes", notes.getText().toString());
            // Add logic to save receipt image, example as Bitmap or URI.

            db.collection("users").document(user.getUid()).collection("expenses")
                .add(expense)
                .addOnSuccessListener(documentReference ->Toast.makeText(getContext(), "Expense saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error adding expense", Toast.LENGTH_SHORT).show());
        }
    }

    public static class BudgetFragment extends Fragment {

        private FirebaseFirestore db;
        private FirebaseAuth mAuth;
        private EditText budgetAmount, budgetCategory, budgetPeriod;
        private Button saveBudgetButton;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_budget, container, false);

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            budgetAmount = view.findViewById(R.id.budgetAmount);
            budgetCategory = view.findViewById(R.id.budgetCategory);
            budgetPeriod = view.findViewById(R.id.budgetPeriod);
            saveBudgetButton = view.findViewById(R.id.saveBudgetButton);

            saveBudgetButton.setOnClickListener(v -> saveBudget());

            return view;
        }

        private void saveBudget() {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            Map<String, Object> budget = new HashMap<>();
            budget.put("amount", Double.parseDouble(budgetAmount.getText().toString()));
            budget.put("category", budgetCategory.getText().toString());
            budget.put("period", budgetPeriod.getText().toString());

            db.collection("users").document(user.getUid()).collection("budgets")
                .add(budget)
                .addOnSuccessListener(documentReference -> Toast.makeText(getContext(), "Budget saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error adding budget", Toast.LENGTH_SHORT).show());
        }
    }

}
```

### Additional Notes:
- This code is structured to include basic user registration, authentication, and fragments for Dashboard, Expense, and Budget management.
- Error handling and null checks are simplified for illustrative purposes.
- Firebase Firestore is used for storing user data. In a production application, better error handling and input validation should be implemented.
- UI elements referenced by IDs (e.g., `R.layout.activity_main`) should be defined in appropriate XML layout files (not included here).
- Real receipt handling, encryption, and backup/sync functionality would require additional implementation.
- This structure uses single Activity with Fragments to swap different views, representing a typical Android development practice. Different fragments like DashboardFragment, ExpenseFragment, and BudgetFragment are used to show data and interact with it.

This code can be further expanded with additional functionalities such as visualization with graphs, secure data storage, PIN/Biometric auth, and more.