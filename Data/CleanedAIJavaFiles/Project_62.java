// SmartRecipeOrganizer.java
// Main activity and implementation of the Smart Recipe Organizer Android app

package com.example.smartrecipeorganizer;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailField, passwordField, ingredientField;
    private Button signUpButton, loginButton, searchButton;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private ArrayList<Recipe> recipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        ingredientField = findViewById(R.id.ingredientField);
        signUpButton = findViewById(R.id.signUpButton);
        loginButton = findViewById(R.id.loginButton);
        searchButton = findViewById(R.id.searchButton);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(adapter);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchRecipes();
            }
        });
    }

    private void signUp() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void login() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(MainActivity.this, "Fields are empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void searchRecipes() {
        final String ingredient = ingredientField.getText().toString();
        if (TextUtils.isEmpty(ingredient)) {
            Toast.makeText(MainActivity.this, "Please enter an ingredient", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference recipesRef = db.collection("recipes");
        recipesRef.whereArrayContains("ingredients", ingredient)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            recipeList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Recipe recipe = document.toObject(Recipe.class);
                                recipeList.add(recipe);
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this, "Error getting recipes.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

// Recipe.java
// Data model for a Recipe

package com.example.smartrecipeorganizer;

import java.util.List;

public class Recipe {
    private String name;
    private List<String> ingredients;
    private String steps;
    private int cookingTime;
    private String difficulty;
    private String category;

    // Constructors, getters, and setters
    public Recipe() {}

    public Recipe(String name, List<String> ingredients, String steps, int cookingTime, String difficulty, String category) {
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
        this.cookingTime = cookingTime;
        this.difficulty = difficulty;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

// RecipeAdapter.java
// Adapter for displaying recipes in a RecyclerView

package com.example.smartrecipeorganizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;

    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.nameTextView.setText(recipe.getName());
        holder.categoryTextView.setText(recipe.getCategory());
        holder.cookingTimeTextView.setText(String.valueOf(recipe.getCookingTime()));
        holder.difficultyTextView.setText(recipe.getDifficulty());
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView, categoryTextView, cookingTimeTextView, difficultyTextView;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            cookingTimeTextView = itemView.findViewById(R.id.cookingTimeTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
        }
    }
}