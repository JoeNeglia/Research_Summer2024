// File: MainActivity.java
package com.example.smartrecipes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.journeyapps.barcodescanner.CaptureActivity;
import androidx.room.*;
import android.content.Context;
import android.widget.Toast;
import com.google.mlkit.vision.barcode.*;
import java.util.List;
import java.util.ArrayList;

// Recipe Entity for Room Database
@Entity(tableName = "recipes")
class Recipe {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String ingredients;
    public String steps;
    public String image;
    public String dietaryPreferences;
}

// Recipe DAO Interface
@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes")
    List<Recipe> getAll();

    @Insert
    void insertAll(Recipe... recipes);

    @Query("SELECT * FROM recipes WHERE ingredients LIKE :ingredient")
    List<Recipe> findByIngredient(String ingredient);
}

// Room Database
@Database(entities = {Recipe.class}, version = 1)
abstract class AppDatabase extends RoomDatabase {
    public abstract RecipeDao recipeDao();
}

// Recipe ViewModel for LiveData
class RecipeViewModel extends AndroidViewModel {
    private final RecipeRepository repository;
    private final LiveData<List<Recipe>> allRecipes;

    public RecipeViewModel(Application application) {
        super(application);
        repository = new RecipeRepository(application);
        allRecipes = repository.getAllRecipes();
    }

    LiveData<List<Recipe>> getAllRecipes() {
        return allRecipes;
    }

    void insert(Recipe recipe) {
        repository.insert(recipe);
    }
}

// Recipe Repository for Database Operations
class RecipeRepository {
    private final RecipeDao recipeDao;
    private final LiveData<List<Recipe>> allRecipes;

    RecipeRepository(Application application) {
        AppDatabase db = Room.databaseBuilder(application, AppDatabase.class, "database-name").build();
        recipeDao = db.recipeDao();
        allRecipes = recipeDao.getAll();
    }

    LiveData<List<Recipe>> getAllRecipes() {
        return allRecipes;
    }

    void insert(Recipe recipe) {
        new Thread(() -> {
            recipeDao.insertAll(recipe);
        }).start();
    }
}

// Main Activity with UI Implementation
public class MainActivity extends AppCompatActivity {
    private RecipeViewModel recipeViewModel;
    private AutoCompleteTextView ingredientInput;
    private ListView recipesListView;
    private ArrayAdapter<String> adapter;
    private List<String> availableIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Components
        ingredientInput = findViewById(R.id.ingredient_input);
        recipesListView = findViewById(R.id.recipes_list_view);
        FloatingActionButton addIngredientButton = findViewById(R.id.add_ingredient_button);
        FloatingActionButton scanBarcodeButton = findViewById(R.id.scan_barcode_button);
        
        // Initialize ViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        recipeViewModel.getAllRecipes().observe(this, recipes -> {
            // Update the cached copy of the words in the adapter.
            List<String> recipeTitles = new ArrayList<>();
            for (Recipe recipe : recipes) {
                recipeTitles.add(recipe.title);
            }
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recipeTitles);
            recipesListView.setAdapter(adapter);
        });

        // Setting up the auto-complete suggestions
        availableIngredients = new ArrayList<>();
        ArrayAdapter<String> ingredientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, availableIngredients);
        ingredientInput.setAdapter(ingredientAdapter);

        // Add Ingredient Button Listener
        addIngredientButton.setOnClickListener(view -> {
            String ingredient = ingredientInput.getText().toString();
            if (!ingredient.isEmpty()) {
                availableIngredients.add(ingredient);
                ingredientAdapter.notifyDataSetChanged();
                ingredientInput.setText("");
            }
        });

        // Scan Barcode Button Listener
        scanBarcodeButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            startActivityForResult(intent, 0);
        });

        // Notification Setup
        createNotificationChannel();
    }

    // Barcode Scan Handling
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            // Handle Barcode scan result here
            String scannedBarcode = data.getStringExtra("SCAN_RESULT");
            // Add code to convert barcode to ingredient and add to list
        }
    }

    // Notification Channel Setup
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SmartRecipeChannel";
            String description = "Channel for Smart Recipe notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("smartrecipechannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Method to fetch recipes based on ingredients
    private void fetchRecipesBasedOnIngredients() {
        new Thread(() -> {
            List<Recipe> matchedRecipes = recipeViewModel.getRecipeDao().findByIngredient("%" + ingredient + "%");
            runOnUiThread(() -> {
                List<String> recipeTitles = new ArrayList<>();
                for (Recipe recipe : matchedRecipes) {
                    recipeTitles.add(recipe.title);
                }
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, recipeTitles);
                recipesListView.setAdapter(adapter);
            });
        }).start();
    }
}

/*
 * Note: This file is provided in a single file for simplicity.
 * In a real Android project, you should separate classes and layouts into 
 * their respective files and directories (e.g., layout XML files in res/layout,
 * entities in separate Entity files, etc.).
 */