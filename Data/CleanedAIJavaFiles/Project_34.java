package com.example.smartshoppinglist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

// Main Activity handling authentication and basic navigation
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if the user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is signed in
            startActivity(new Intent(this, ShoppingListActivity.class));
            finish();
        } else {
            // No user is signed in
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}

// Login Activity handling user authentication
class LoginActivity extends AppCompatActivity {
    
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        
        findViewById(R.id.signInButton).setOnClickListener(v -> signIn());
        findViewById(R.id.signUpButton).setOnClickListener(v -> signUp());
    }

    // Sign in existing user
    private void signIn() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, task -> {
                 if (task.isSuccessful()) {
                     // Sign in success
                     FirebaseUser user = mAuth.getCurrentUser();
                     startActivity(new Intent(this, ShoppingListActivity.class));
                     finish();
                 } else {
                     // If sign in fails, display a message to the user.
                     Toast.makeText(LoginActivity.this, "Authentication failed.", 
                                    Toast.LENGTH_SHORT).show();
                 }
             });
    }

    // Register new user
    private void signUp() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, task -> {
                 if (task.isSuccessful()) {
                     // Sign up success
                     FirebaseUser user = mAuth.getCurrentUser();
                     // Move to next activity
                     startActivity(new Intent(this, ShoppingListActivity.class));
                     finish();
                 } else {
                     // If sign up fails, display a message to the user.
                     Toast.makeText(LoginActivity.this, "Registration failed.",
                                    Toast.LENGTH_SHORT).show();
                 }
             });
    }
}

// Shopping List Activity handling CRUD operations on the shopping list
class ShoppingListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private ShoppingListAdapter adapter;
    private RecyclerView recyclerView;
    private List<ShoppingItem> shoppingItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        db = FirebaseFirestore.getInstance();
        shoppingItemList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ShoppingListAdapter(shoppingItemList);
        recyclerView.setAdapter(adapter);

        fetchShoppingList();

        findViewById(R.id.addItemButton).setOnClickListener(v -> addNewItem());
    }

    // Fetch items from Firestore
    private void fetchShoppingList() {
        db.collection("shoppingItems").get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  shoppingItemList.clear();
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      ShoppingItem item = document.toObject(ShoppingItem.class);
                      shoppingItemList.add(item);
                  }
                  adapter.notifyDataSetChanged();
              } else {
                  Log.w("ShoppingListActivity", "Error getting documents.", task.getException());
              }
          });
    }

    // Add new item to the list
    private void addNewItem() {
        EditText itemNameEditText = findViewById(R.id.itemNameEditText);
        String itemName = itemNameEditText.getText().toString();
        if (!itemName.isEmpty()) {
            ShoppingItem newItem = new ShoppingItem(itemName);
            db.collection("shoppingItems").add(newItem)
              .addOnSuccessListener(documentReference -> fetchShoppingList())
              .addOnFailureListener(e -> Toast.makeText(this, "Error adding item",
                                                         Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    // Adapter for RecyclerView to handle the display of shopping items
    private class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

        private List<ShoppingItem> shoppingList;

        ShoppingListAdapter(List<ShoppingItem> shoppingList) {
            this.shoppingList = shoppingList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.shopping_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ShoppingItem item = shoppingList.get(position);
            holder.itemNameTextView.setText(item.getName());
            holder.itemView.setOnLongClickListener(v -> {
                // Removing item from the list
                db.collection("shoppingItems").document(item.getId()).delete()
                  .addOnSuccessListener(aVoid -> {
                      shoppingList.remove(position);
                      notifyItemRemoved(position);
                      Toast.makeText(ShoppingListActivity.this, "Item removed", 
                                     Toast.LENGTH_SHORT).show();
                  })
                  .addOnFailureListener(e -> Toast.makeText(ShoppingListActivity.this, 
                                                           "Error removing item", 
                                                           Toast.LENGTH_SHORT).show());
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return shoppingList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView itemNameTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            }
        }
    }
}

// Shopping Item Model to hold item data
class ShoppingItem {

    private String id;
    private String name;

    public ShoppingItem() { }

    ShoppingItem(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

// Maps Activity to show nearby stores (Location-based recommendations)
class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this,
                                               Manifest.permission.ACCESS_COARSE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));  // Uncomment for actual use
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onMapReady(mMap);
        }
    }
}

// Add necessary XML layouts accordingly: activity_main.xml, activity_login.xml,
// activity_shopping_list.xml, activity_maps.xml, shopping_item.xml