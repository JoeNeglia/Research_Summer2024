Certainly! Below is a comprehensive implementation of the Smart Grocery List App in a single Java file, utilizing modern Android components like LiveData, ViewModel, and Room. Due to the complexity and verbosity of a real-world application, the implementation is separated into sections using appropriate comments. Note: This is a basic version to cover the main requirements. Integration with third-party APIs and full authentication setups are indicated but need to be more thoroughly developed.

```java
package com.example.smartgrocerylist;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.ktx.auth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;

import java.util.ArrayList;
import java.util.List;

// ********** MODEL ********** //
@Entity(tableName = "groceries")
class GroceryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "quantity")
    public int quantity;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "priority")
    public String priority;

    @ColumnInfo(name = "isPurchased")
    public boolean isPurchased;

    // Constructor & Getters/Setters
    public GroceryItem(String name, int quantity, String category, String priority, boolean isPurchased) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.priority = priority;
        this.isPurchased = isPurchased;
    }
}

// ********** DATABASE ********** //
@androidx.room.Database(entities = {GroceryItem.class}, version = 1)
abstract class GroceryDatabase extends RoomDatabase {
    public abstract GroceryDao groceryDao();

    private static volatile GroceryDatabase INSTANCE;

    public static GroceryDatabase getDatabase(final Application application) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(application, GroceryDatabase.class, "grocery_database").build();
        }
        return INSTANCE;
    }
}

// DAO (Database Access Object)
@androidx.room.Dao
interface GroceryDao {
    @androidx.room.Insert
    void insert(GroceryItem item);

    @androidx.room.Update
    void update(GroceryItem item);

    @androidx.room.Delete
    void delete(GroceryItem item);

    @androidx.room.Query("SELECT * FROM groceries")
    LiveData<List<GroceryItem>> getGroceryItems();
}

// ********** VIEWMODEL ********** //
class GroceryViewModel extends AndroidViewModel {
    private GroceryDao groceryDao;
    private LiveData<List<GroceryItem>> groceryItems;

    public GroceryViewModel(Application application) {
        super(application);
        GroceryDatabase db = GroceryDatabase.getDatabase(application);
        groceryDao = db.groceryDao();
        groceryItems = groceryDao.getGroceryItems();
    }

    public LiveData<List<GroceryItem>> getGroceryItems() {
        return groceryItems;
    }

    public void insert(GroceryItem item) {
        GroceryDatabase.databaseWriteExecutor.execute(() -> groceryDao.insert(item));
    }

    public void update(GroceryItem item) {
        GroceryDatabase.databaseWriteExecutor.execute(() -> groceryDao.update(item));
    }

    public void delete(GroceryItem item) {
        GroceryDatabase.databaseWriteExecutor.execute(() -> groceryDao.delete(item));
    }
}

// ********** ACTIVITY ********** //

public class MainActivity extends AppCompatActivity {
    private GroceryViewModel groceryViewModel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button btnAddItem, btnLogin, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = Firebase.auth;
        db = FirebaseFirestore.getInstance();

        btnAddItem = findViewById(R.id.btnAddItem);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);

        groceryViewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(GroceryViewModel.class);

        // Dummy authentication listeners
        btnLogin.setOnClickListener(view -> {
            // Replace with real sign-in logic.
            signInWithGoogle();
        });

        btnLogout.setOnClickListener(view -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        });

        btnAddItem.setOnClickListener(view -> {
            // Implement item addition logic here
            addGroceryItem();
        });

        createNotificationChannel();

        groceryViewModel.getGroceryItems().observe(this, new Observer<List<GroceryItem>>() {
            @Override
            public void onChanged(@Nullable final List<GroceryItem> items) {
                // Update UI with grocery items
            }
        });
    }

    private void addGroceryItem() {
        EditText etName = findViewById(R.id.etName);
        EditText etQuantity = findViewById(R.id.etQuantity);
        EditText etCategory = findViewById(R.id.etCategory);
        EditText etPriority = findViewById(R.id.etPriority);

        String name = etName.getText().toString();
        int quantity = Integer.parseInt(etQuantity.getText().toString());
        String category = etCategory.getText().toString();
        String priority = etPriority.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(category) || TextUtils.isEmpty(priority)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        GroceryItem item = new GroceryItem(name, quantity, category, priority, false);
        groceryViewModel.insert(item);
    }

    private void signInWithGoogle() {
        // Placeholder for Google Sign-In
        mAuth.signInWithProvider(this, OAuthProvider.newBuilder("google.com").build())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            AuthResult result = task.getResult();
                            Toast.makeText(MainActivity.this, "Signed in as " + result.getUser().getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Sign-In Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GroceryChannel";
            String description = "Channel for grocery notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyGrocery", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
```

### Key Components:
1. **Model**
  - `GroceryItem`: Represents a grocery item entity in Room database.

2. **Database**
  - `GroceryDatabase`: Handles database creation and version management.
  - `GroceryDao`: Defines methods for accessing the database.

3. **ViewModel**
  - `GroceryViewModel`: Manages UI-related data in a lifecycle-conscious way. Acts as a bridge between the database and UI.

4. **Activity**
  - `MainActivity`: Manages UI components, handles sign-in, sign-out, and interactions such as adding items.

### Additional Notes:
- Real authentication logic is indicated but would need a thorough setup of Firebase Authentication and Firestore.
- Notification channel setup is included to enable push notifications.
- Full implementation including handling RecyclerView, List adapters, and additional fragments for detailed views would be added for a comprehensive UI.
- This is a simplified structure aimed at covering the necessary components. Complete error handling, proper user interface, and full features should be developed in a production-level app.