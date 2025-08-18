// Import necessary packages
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import java.util.List;
import java.util.ArrayList;

// Main activity of the application
public class MyLocalRecipesActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference recipeRef;
    private FirebaseStorage storage;
    private RecipesViewModel recipesViewModel;
    private ProgressBar loadingSpinner;
    private RecyclerView recipesList;
    private RecipesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        recipeRef = database.getReference("recipes");
        storage = FirebaseStorage.getInstance();
        
        // Set up ViewModel
        recipesViewModel = new ViewModelProvider(this).get(RecipesViewModel.class);
        
        // Set up RecyclerView for displaying recipes
        recipesList = findViewById(R.id.recipes_list);
        recipesList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipesAdapter(new ArrayList<>());
        recipesList.setAdapter(adapter);

        // Observe the LiveData from the ViewModel
        recipesViewModel.getRecipes().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                adapter.updateRecipesList(recipes);
            }
        });

        // Set up loading spinner
        loadingSpinner = findViewById(R.id.loading_spinner);

        // Handle authentication and recipe management functionalities
        setupAuthentication();
        setupRecipeManagement();
    }

    // Authentication functionalities
    private void setupAuthentication() {
        Button loginButton = findViewById(R.id.login_button);
        EditText emailField = findViewById(R.id.email_field);
        EditText passwordField = findViewById(R.id.password_field);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                if (!email.isEmpty() && !password.isEmpty()) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            Toast.makeText(MyLocalRecipesActivity.this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MyLocalRecipesActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MyLocalRecipesActivity.this, "Email and password cannot be empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Recipe Management functionalities
    private void setupRecipeManagement() {
        ImageView addRecipeButton = findViewById(R.id.add_recipe_button);
        addRecipeButton.setOnClickListener(v -> {
            // Navigate to Add Recipe Activity
            startActivity(new Intent(MyLocalRecipesActivity.this, AddRecipeActivity.class));
        });

        DatabaseReference recipesRef = database.getReference("recipes");
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }
                recipesViewModel.setRecipes(recipes);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}

// Define Recipe model class
class Recipe {
    public String id;
    public String title;
    public String ingredients;
    public String instructions;
    public String photoUrl;
    public String cuisineType;
    public String region;

    public Recipe() {
        // Default constructor required for calls to DataSnapshot.getValue(Recipe.class)
    }
   
    // Getters and setters for recipe properties
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCuisineType() { return cuisineType; }
    public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}

// Adapter class for recipes RecyclerView
class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder> {
    private List<Recipe> recipesList;

    public RecipesAdapter(List<Recipe> recipesList) {
        this.recipesList = recipesList;
    }

    public void updateRecipesList(List<Recipe> recipes) {
        this.recipesList = recipes;
        notifyDataSetChanged();
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        Recipe recipe = recipesList.get(position);
        holder.title.setText(recipe.getTitle());
        holder.ingredients.setText(recipe.getIngredients());
        holder.instructions.setText(recipe.getInstructions());
        // Assume Glide library is used for image loading
        Glide.with(holder.photo.getContext()).load(recipe.getPhotoUrl()).into(holder.photo);
    }

    @Override
    public int getItemCount() {
        return recipesList.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView ingredients;
        TextView instructions;
        ImageView photo;

        public RecipeViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.recipe_title);
            ingredients = view.findViewById(R.id.recipe_ingredients);
            instructions = view.findViewById(R.id.recipe_instructions);
            photo = view.findViewById(R.id.recipe_photo);
        }
    }
}

// ViewModel class for managing recipes list
class RecipesViewModel extends ViewModel {
    private MutableLiveData<List<Recipe>> recipes;

    public RecipesViewModel() {
        recipes = new MutableLiveData<>();
        // Initialize with an empty list or fetch from backend
        recipes.setValue(new ArrayList<>());
    }

    public LiveData<List<Recipe>> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes.setValue(recipes);
    }
}

// Layout Activity Code (activity_main.xml)
/*
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    tools:context=".MyLocalRecipesActivity">

    <EditText
        android:id="@+id/email_field"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Email" />

    <EditText
        android:id="@+id/password_field"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Password" />

    <Button
        android:id="@+id/login_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login" />

    <ImageView
        android:id="@+id/add_recipe_button"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_add_recipe" />

    <ProgressBar
        android:id="@+id/loading_spinner"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone" />

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recipes_list"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>

</LinearLayout>
*/