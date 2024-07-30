Creating the "Smart Shopping List App" as detailed in the project overview is an extensive task that goes beyond what can be showcased in a single file and a brief code segment. However, I will provide a detailed structure and some core components to get you started. This will include user authentication, creating and managing shopping lists, smart suggestions using Firebase, and including essential dependencies.

### Full Source Code for Smart Shopping List App

```java
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
```

### Explanation:
1. **User Authentication**: 
    - The `MainActivity` handles email/password login, registration, and navigation to the shopping list screen upon successful authentication.
    - Uses Firebase Authentication for handling user signups and logins.

2. **Shopping List Management**: 
    - `ShoppingListActivity` handles creating, viewing, and managing shopping lists.
    - Firebase Realtime Database is used to store and retrieve shopping lists.
    - `ShoppingList` is a data model for shopping lists.
    - `ShoppingListAdapter` is a RecyclerView adapter that displays the list of shopping lists.

### Next Steps:
1. **Smart Suggestions**:
    - Integrate APIs or ML models for providing item suggestions.
2. **Price Tracking**:
    - Integrate APIs to fetch and display item prices.
3. **Barcode Scanning**:
    - Utilize ZXing library for barcode scanning.
4. **Voice Input**:
    - Implement Android’s Speech-to-Text API for voice commands.
5. **Sharing and Collaboration**:
    - Implement real-time collaboration using Firebase Firestore.
6. **Cloud Sync**:
    - Ensure lists are synced across devices using Firebase Firestore.
7. **Notifications**:
    - Implement reminder notifications for shopping.

### Additional Libraries:
- ZXing for barcode scanning.
- ML Kit for machine learning features.
- Retrofit/Volley for network operations.

### User Interface:
Ensure you design the UI following Material Design guidelines. Create layouts and activities for each feature.

### Note:
This is a basic starting point focusing on user authentication and shopping list management in a single file. For a comprehensive app, structure your project across multiple files and activities, following Android best practices for maintainability and scalability. Check Firebase documentation and Android developer resources for detailed implementation guides.