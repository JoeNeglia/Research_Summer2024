// Add necessary imports
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// MainActivity Class Handles User Authentication and Shopping List Management
public class MainActivity extends AppCompatActivity {

    // Declare FirebaseAuth and DatabaseReference to use Firebase functionalities
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    // Declare Views
    private EditText emailField, passwordField;
    private Button loginButton, signupButton;

    // Initialise Views and Firebase instances on Create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        emailField = findViewById(R.id.email_field);
        passwordField = findViewById(R.id.password_field);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.signup_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    // Registers a new user
    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser user = mAuth.getCurrentUser();
                        navigateToShoppingLists();
                    } else {
                        // Registration fails
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    // Login existing user
    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Login success
                        FirebaseUser user = mAuth.getCurrentUser();
                        navigateToShoppingLists();
                    } else {
                        // Login fails
                        Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    // Navigates to Shopping List Activity after successful login/registration
    private void navigateToShoppingLists() {
        Intent intent = new Intent(MainActivity.this, ShoppingListActivity.class);
        startActivity(intent);
        finish();
    }
}

// ShoppingListActivity Class handles creating, viewing, and managing shopping lists
class ShoppingListActivity extends AppCompatActivity {
    // Declare Firebase reference and other needed variables
    private DatabaseReference mDatabase;

    private RecyclerView recyclerView;
    private ShoppingListAdapter mAdapter;
    private List<ShoppingList> shoppingLists;

    // Initialise Firebase reference and views
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        mDatabase = FirebaseDatabase.getInstance().getReference("shopping_lists");
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        shoppingLists = new ArrayList<>();
        mAdapter = new ShoppingListAdapter(shoppingLists);
        recyclerView.setAdapter(mAdapter);

        loadShoppingLists();
    }

    // Load shopping lists from Firebase
    private void loadShoppingLists() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shoppingLists.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    ShoppingList shoppingList = postSnapshot.getValue(ShoppingList.class);
                    shoppingLists.add(shoppingList);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ShoppingListActivity.this, "Failed to load shopping lists.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to add a new shopping list 
    private void addShoppingList(String name) {
        String id = mDatabase.push().getKey();
        ShoppingList shoppingList = new ShoppingList(id, name);
        if (id != null) {
            mDatabase.child(id).setValue(shoppingList);
        }
    }
}

// Model class for ShoppingList
class ShoppingList {
    public String id;
    public String name;

    public ShoppingList() {
        // Default constructor required for calls to DataSnapshot.getValue(ShoppingList.class)
    }

    public ShoppingList(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters (used by Firebase when serializing data)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

// Adapter class to manage shopping list items in RecyclerView
class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder> {
    private List<ShoppingList> shoppingLists;

    public ShoppingListAdapter(List<ShoppingList> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

    @NonNull
    @Override
    public ShoppingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_list_item, parent, false);
        return new ShoppingListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListViewHolder holder, int position) {
        ShoppingList shoppingList = shoppingLists.get(position);
        holder.nameTextView.setText(shoppingList.getName());
    }

    @Override
    public int getItemCount() {
        return shoppingLists.size();
    }

    class ShoppingListViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.list_name_text_view);
        }
    }
}