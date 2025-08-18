package com.example.shoppinglistapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.common.BarcodeScannerOptions;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_REQUEST_CODE = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    
    private EditText emailEditText, passwordEditText, itemNameEditText;
    private Button loginButton, registerButton, scanButton, addItemButton;
    private RecyclerView itemListRecycler;
    
    private List<String> itemList;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        itemNameEditText = findViewById(R.id.itemNameEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        scanButton = findViewById(R.id.scanButton);
        addItemButton = findViewById(R.id.addItemButton);
        itemListRecycler = findViewById(R.id.itemListRecycler);

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(itemList);
        itemListRecycler.setAdapter(itemAdapter);
        itemListRecycler.setLayoutManager(new LinearLayoutManager(this));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(emailEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() { 
            @Override
            public void onClick(View v) {
                registerUser(emailEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem(itemNameEditText.getText().toString());
            }
        });

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBarcodeScan();
            }
        });
        
        // Listen for real-time updates to the user's shopping list
        listenForShoppingListUpdates();
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login successful.", Toast.LENGTH_SHORT).show();
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
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void addItem(String itemName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", itemName);
            
            db.collection("items").document(user.getUid()).set(item, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Item added.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to add item.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void listenForShoppingListUpdates() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("items").document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(MainActivity.this, "Listen failed.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (snapshot != null && snapshot.exists()) {
                        itemList.clear();
                        itemList.addAll((List<String>) snapshot.get("items"));
                        itemAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private void startBarcodeScan() {
        Intent intent = new Intent(this, BarcodeScannerActivity.class);
        startActivityForResult(intent, BARCODE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String scannedCode = data.getStringExtra("scanned_code");
                fetchProductDetails(scannedCode);
            }
        }
    }

    private void fetchProductDetails(String barcode) {
        // Implement API call to fetch product details by barcode
        // For demonstration, let's assume we get a product name "Sample Product"
        String productName = "Sample Product"; 
        addItem(productName);
    }
    
    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(MainActivity.this, "Logged out!", Toast.LENGTH_SHORT).show();
    }
}