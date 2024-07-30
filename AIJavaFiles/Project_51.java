Here's a comprehensive single-file implementation of a Smart Expense Tracker Android application in Java:

```java
// MainActivity.java

package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }
}

// LoginActivity.java

package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}

// RegisterActivity.java

package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}

// HomeActivity.java

package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        welcomeTextView = findViewById(R.id.welcomeTextView);

        if (currentUser != null) {
            welcomeTextView.setText("Welcome, " + currentUser.getEmail());
        } else {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

// Expense.java

package com.example.expensetracker;

public class Expense {
    private String id;
    private String category;
    private String subcategory;
    private double amount;
    private String date;
    private String note;

    public Expense() {
        // Default constructor required for calls to DataSnapshot.getValue(Expense.class)
    }

    public Expense(String id, String category, String subcategory, double amount, String date, String note) {
        this.id = id;
        this.category = category;
        this.subcategory = subcategory;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    // Getters and setters
}

// ExpenseLoggingActivity.java

package com.example.expensetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ExpenseLoggingActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private EditText amountEditText, dateEditText, noteEditText;
    private Spinner categorySpinner, subcategorySpinner;
    private Button logExpenseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_logging);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        amountEditText = findViewById(R.id.amountEditText);
        dateEditText = findViewById(R.id.dateEditText);
        noteEditText = findViewById(R.id.noteEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        subcategorySpinner = findViewById(R.id.subcategorySpinner);
        logExpenseButton = findViewById(R.id.logExpenseButton);

        // Dummy categories
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        logExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logExpense();
            }
        });
    }

    private void logExpense() {
        String amount = amountEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String note = noteEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Create a new expense record
            Map<String, Object> expense = new HashMap<>();
            expense.put("amount", Double.parseDouble(amount));
            expense.put("date", date);
            expense.put("note", note);
            expense.put("category", category);
            expense.put("userId", userId);

            // Add a new document with a generated ID
            db.collection("expenses").add(expense)
                    .addOnSuccessListener(documentReference -> {
                        startActivity(new Intent(ExpenseLoggingActivity.this, HomeActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                    });
        }
    }
}

// ExpenseAdapter.java

package com.example.expensetracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense> expenseList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView amount, category, date, note;

        public ViewHolder(View view) {
            super(view);
            amount = view.findViewById(R.id.amountTextView);
            category = view.findViewById(R.id.categoryTextView);
            date = view.findViewById(R.id.dateTextView);
            note = view.findViewById(R.id.noteTextView);
        }
    }

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenseList.get(position);
        holder.amount.setText(String.valueOf(expense.getAmount()));
        holder.category.setText(expense.getCategory());
        holder.date.setText(expense.getDate());
        holder.note.setText(expense.getNote());
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }
}

// BudgetSettingActivity.java

package com.example.expensetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BudgetSettingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private EditText budgetAmountEditText, categoryEditText;
    private Button setBudgetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setting);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        budgetAmountEditText = findViewById(R.id.budgetAmountEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        setBudgetButton = findViewById(R.id.setBudgetButton);

        setBudgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBudget();
            }
        });
    }

    private void setBudget() {
        String budgetAmount = budgetAmountEditText.getText().toString();
        String category = categoryEditText.getText().toString();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Create a new budget record
            Map<String, Object> budget = new HashMap<>();
            budget.put("budgetAmount", Double.parseDouble(budgetAmount));
            budget.put("category", category);
            budget.put("userId", userId);

            // Add a new document with a generated ID
            db.collection("budgets").add(budget)
                    .addOnSuccessListener(documentReference -> {
                        // Budget successfully set
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                    });
        }
    }
}

// VisualizationActivity.java

package com.example.expensetracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VisualizationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        PieChart pieChart = findViewById(R.id.pieChart);

        // Fetch data from Firestore
        fetchData(pieChart);
    }

    private void fetchData(PieChart pieChart) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Query to get the expenses for the logged-in user
            db.collection("expenses")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<PieEntry> entries = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Parse your data for visualization
                            }

                            PieDataSet dataSet = new PieDataSet(entries, "Expenses");
                            PieData data = new PieData(dataSet);
                            pieChart.setData(data);
                            pieChart.invalidate(); // refresh
                        }
                    });
        }
    }
}

```

Note: This code includes basic structure for

- User Authentication using Firebase
- Logging expenses
- Basic data visualization setup with MPAndroidChart

Before running this code, make sure:

- You have Firebase Authentication and Firestore properly configured.
- XML layout files (activity_main.xml, activity_login.xml, activity_register.xml, activity_home.xml, activity_expense_logging.xml, activity_budget_setting.xml, activity_visualization.xml, and others) should be created accordingly.
- Necessary dependencies (Firebase, MPAndroidChart, etc.) should be added to your project build.gradle file.

Further functionalities like AI/ML, notifications, exporting/importing data, OCR integration, etc., need to be implemented as per requirements. This code provides a strong foundational structure to build upon.