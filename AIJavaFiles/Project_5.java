Below is a comprehensive single-file Java implementation for the Smart Recipe Manager App as described in the project prompt. The code contains comments to help you understand each section.

```java
// MainActivity.java

package com.example.smartrecipemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

// Entry point of the application that handles user authentication
public class MainActivity extends AppCompatActivity {

    // Firebase Authentication instance
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnSignUp = findViewById(R.id.btnSignUp);

        // Handle user login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogin();
            }
        });

        // Handle user sign up
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSignUp();
            }
        });
    }

    // Function for user login
    private void handleLogin() {
        String email = "test@example.com"; // Replace with actual input
        String password = "password"; // Replace with actual input

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            navigateToRecipeManager();
                        } else {
                            // If sign in fails, display a message to the user.
                            // Show error
                        }
                    }
                });
    }

    // Function for user sign up
    private void handleSignUp() {
        String email = "newuser@example.com"; // Replace with actual input
        String password = "newpassword"; // Replace with actual input

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            navigateToRecipeManager();
                        } else {
                            // If sign up fails, display a message to the user.
                            // Show error
                        }
                    }
                });
    }

    // Navigate to RecipeManagerActivity after successful login/sign-up
    private void navigateToRecipeManager() {
        Intent intent = new Intent(this, RecipeManagerActivity.java);
        startActivity(intent);
        finish();
    }
}

// RecipeManagerActivity.java

package com.example.smartrecipemanager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

// This activity handles Recipe Management operations
public class RecipeManagerActivity extends AppCompatActivity {

    // Firestore database instance
    private FirebaseFirestore db;
    private List<Recipe> recipeList;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_manager);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();
        recipeList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(adapter);

        // Fetch recipes from Firestore
        fetchRecipes();
    }

    // Function to fetch recipes from Firestore
    private void fetchRecipes() {
        db.collection("recipes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        recipeList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            recipeList.add(recipe);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle the error
                    }
                });
    }
}

// Recipe.java
package com.example.smartrecipemanager;

public class Recipe {
    private String title;
    private String ingredients;
    private String steps;
    private String cookingTime;
    private String difficultyLevel;
    private String photoUrl;
    private String category;

    public Recipe() {
    }

    public Recipe(String title, String ingredients, String steps, String cookingTime, String difficultyLevel, String photoUrl, String category) {
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.cookingTime = cookingTime;
        this.difficultyLevel = difficultyLevel;
        this.photoUrl = photoUrl;
        this.category = category;
    }

    // Getters and setters
}

// RecipeAdapter.java

package com.example.smartrecipemanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adapter class for displaying recipes in RecyclerView
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private List<Recipe> recipeList;

    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.titleText.setText(recipe.getTitle());
        holder.ingredientsText.setText(recipe.getIngredients());
        // Load image using Glide or Picasso (not implemented)
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleText;
        public TextView ingredientsText;
        public TextView stepsText;
        public ImageView photoImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            ingredientsText = itemView.findViewById(R.id.ingredientsText);
            stepsText = itemView.findViewById(R.id.stepsText);
            photoImageView = itemView.findViewById(R.id.photoImageView);
        }
    }
}

// activity_main.xml
<!-- Layout file for MainActivity -->
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/btnLogin"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Log In"
        android:layout_centerInParent="true"/>

    <Button
        android:id="@+id/btnSignUp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Sign Up"
        android:layout_below="@+id/btnLogin"
        android:layout_marginTop="16dp"
        android:layout_centerHorizontal="true"/>

</RelativeLayout>

// activity_recipe_manager.xml
<!-- Layout file for RecipeManagerActivity -->
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".RecipeManagerActivity">

    <RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

</RelativeLayout>

// item_recipe.xml
<!-- Layout file for each item in RecyclerView -->
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="8dp">
    
    <TextView
        android:id="@+id/titleText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Recipe Title"
        android:textSize="16sp"
        android:textStyle="bold"/>

    <TextView
        android:id="@+id/ingredientsText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Ingredients"
        android:layout_below="@+id/titleText"/>

    <TextView
        android:id="@+id/stepsText"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Preparation Steps"
        android:layout_below="@+id/ingredientsText"/>
        
    <ImageView
        android:id="@+id/photoImageView"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_below="@+id/stepsText"
        android:layout_marginTop="8dp"/>

</RelativeLayout>

// Add other necessary classes and functionalities like GroceryListActivity, MealPlanActivity, etc.

```

This example covers:
- User Authentication using Firebase
- Recipe Management using Firestore
- UI components and layouts

The `MainActivity` handles user login and sign-up. After successful login/sign-up, it navigates to `RecipeManagerActivity`, where users can view a list of their recipes. 

For a full implementation, you would need to extend `RecipeManagerActivity` to add features like creating, updating, and deleting recipes. Similarly, add activities for meal planning (`MealPlanActivity`), grocery list management (`GroceryListActivity`), etc.

This structure is a good starting point, and you can build upon it by adding more activities and features as required by the project prompt.