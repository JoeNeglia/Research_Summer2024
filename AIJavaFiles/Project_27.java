```java
// Import necessary packages
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// MainActivity class - Entry point of the application
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView userStatus;
    private EditText emailField, passwordField;
    private Button signInButton, signUpButton, signOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userStatus = findViewById(R.id.userStatus);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
        signOutButton = findViewById(R.id.signOutButton);

        signInButton.setOnClickListener(view -> signIn());
        signUpButton.setOnClickListener(view -> signUp());
        signOutButton.setOnClickListener(view -> signOut());
    }

    // Update UI based on user's authentication state
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            userStatus.setText("Signed in as: " + user.getEmail());
            signInButton.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
        } else {
            userStatus.setText("Not signed in");
            signInButton.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
        }
    }

    // Handle user sign-in
    private void signIn() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        userStatus.setText("Authentication failed.");
                        updateUI(null);
                    }
                });
    }

    // Handle user sign-up
    private void signUp() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        userStatus.setText("Authentication failed.");
                        updateUI(null);
                    }
                });
    }

    // Handle user sign-out
    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
}

// Add New Transaction Activity
class AddTransactionActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText transactionName, transactionAmount, transactionCategory;
    private Button addTransactionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        transactionName = findViewById(R.id.transactionName);
        transactionAmount = findViewById(R.id.transactionAmount);
        transactionCategory = findViewById(R.id.transactionCategory);
        addTransactionButton = findViewById(R.id.addTransactionButton);

        addTransactionButton.setOnClickListener(view -> addTransaction());
    }

    // Add a new transaction to Firestore
    private void addTransaction() {
        String name = transactionName.getText().toString();
        String amount = transactionAmount.getText().toString();
        String category = transactionCategory.getText().toString();
        
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("name", name);
        transaction.put("amount", Double.parseDouble(amount));
        transaction.put("category", category);

        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(documentReference -> Log.d("AddTransaction", "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("AddTransaction", "Error adding document", e));
    }
}

// View Transactions Activity
class ViewTransactionsActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView transactionsRecyclerView;
    private TransactionsAdapter transactionsAdapter;
    private ArrayList<Transaction> transactionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transactions);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsAdapter = new TransactionsAdapter(transactionList);
        transactionsRecyclerView.setAdapter(transactionsAdapter);

        fetchTransactions();
    }

    // Fetch transactions from Firestore
    private void fetchTransactions() {
        db.collection("transactions").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        Transaction transaction = document.toObject(Transaction.class);
                        transactionList.add(transaction);
                    }
                    transactionsAdapter.notifyDataSetChanged();
                } else {
                    Log.w("ViewTransactions", "Error getting documents.", task.getException());
                }
            });
    }
}

// Transaction Adapter for RecyclerView
class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {
    private ArrayList<Transaction> transactionList;

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public TextView transactionName, transactionAmount, transactionCategory;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            transactionName = itemView.findViewById(R.id.transactionName);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionCategory = itemView.findViewById(R.id.transactionCategory);
        }
    }

    public TransactionsAdapter(ArrayList<Transaction> transactions) {
        transactionList = transactions;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        return new TransactionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction currentTransaction = transactionList.get(position);
        holder.transactionName.setText(currentTransaction.getName());
        holder.transactionAmount.setText(String.valueOf(currentTransaction.getAmount()));
        holder.transactionCategory.setText(currentTransaction.getCategory());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
}

// Transaction Class
class Transaction {
    private String name;
    private double amount;
    private String category;

    public Transaction() {
        // Default constructor required for calls to DataSnapshot.getValue(Transaction.class)
    }

    public Transaction(String name, double amount, String category) {
        this.name = name;
        this.amount = amount;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }
}

/* 
XML Layout files (res/layout/activity_main.xml, res/layout/activity_add_transaction.xml, res/layout/activity_view_transactions.xml, and res/layout/transaction_item.xml) would need to be created accordingly to define the UI for MainActivity, AddTransactionActivity, ViewTransactionsActivity, and a single transaction item respectively.
*/

// Add necessary dependencies in build.gradle
/*
dependencies {
    // Firebase
    implementation 'com.google.firebase:firebase-auth:21.0.1'
    implementation 'com.google.firebase:firebase-firestore:24.0.0'
    // for Recyclerview
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
}
*/
```

This Android application covers basic features such as user authentication via Firebase, adding, and viewing transactions using Firestore database. There is room for further development such as detailed budgeting and visualizations, which would be implemented in subsequent milestones.