package com.example.personalfinancetracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalfinancetracker.adapter.TransactionAdapter;
import com.example.personalfinancetracker.database.TransactionEntity;
import com.example.personalfinancetracker.viewmodel.TransactionViewModel;
import com.example.personalfinancetracker.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ActivityMainBinding binding;
    private TransactionViewModel transactionViewModel;
    private TransactionAdapter adapter;
    private PieChart pieChart;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set up RecyclerView
        RecyclerView recyclerView = binding.recyclerViewTransactions;
        adapter = new TransactionAdapter(new TransactionAdapter.TransactionDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set up ViewModel
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        transactionViewModel.getAllTransactions().observe(this, new Observer<List<TransactionEntity>>() {
            @Override
            public void onChanged(List<TransactionEntity> transactions) {
                adapter.submitList(transactions);
                updateCharts(transactions);
            }
        });

        // Set up Pie Chart and Bar Chart
        pieChart = binding.pieChart;
        barChart = binding.barChart;

        // Handle adding new transactions
        binding.buttonAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = binding.editTextDescription.getText().toString();
                String amountString = binding.editTextAmount.getText().toString();
                if (!description.isEmpty() && !amountString.isEmpty()) {
                    double amount = Double.parseDouble(amountString);
                    TransactionEntity transaction = new TransactionEntity(description, amount, System.currentTimeMillis());
                    transactionViewModel.insert(transaction);
                    Toast.makeText(MainActivity.this, "Transaction added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle user login
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                loginUser(email, password);
            }
        });

        // Handle user registration
        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                registerUser(email, password);
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();
                        // Update UI or go to main application page
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void updateCharts(List<TransactionEntity> transactions) {
        // Update Pie Chart
        List<PieEntry> pieEntries = new ArrayList<>();
        // Assuming some predefined categories
        double totalIncome = 0, totalExpenses = 0;
        for (TransactionEntity transaction : transactions) {
            if (transaction.getAmount() > 0) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpenses += transaction.getAmount();
            }
        }
        pieEntries.add(new PieEntry((float) totalIncome, "Income"));
        pieEntries.add(new PieEntry((float) -totalExpenses, "Expenses"));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Finance Summary");
        PieData pieData = new PieData(pieDataSet);
        pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        pieChart.setData(pieData);
        pieChart.invalidate(); // refresh chart

        // Update Bar Chart
        List<BarEntry> barEntries = new ArrayList<>();
        // Assuming transactions are monthly categorized
        double[] monthlyExpenses = new double[12];
        for (TransactionEntity transaction : transactions) {
            int month = transaction.getMonth();
            monthlyExpenses[month] += transaction.getAmount();
        }
        for (int i = 0; i < 12; i++) {
            barEntries.add(new BarEntry(i, (float) monthlyExpenses[i]));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Monthly Expenses");
        BarData barData = new BarData(barDataSet);
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barChart.setData(barData);
        barChart.invalidate(); // refresh chart
    }
}