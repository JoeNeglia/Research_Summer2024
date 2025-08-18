// MainActivity.java

package com.example.smartrecipeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

// Import Firebase Authentication
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    // Instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        } else {
            // Navigate to login activity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void updateUI(FirebaseUser user) {
        // Navigate to Home Activity
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }
}

// LoginActivity.java

package com.example.smartrecipeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Import Firebase Authentication
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.annotation.NonNull;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.register);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Register Activity
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Authentication Failed.",
                                       Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }
            });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            // Navigate to Home Activity
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }
}

// RegisterActivity.java

package com.example.smartrecipeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// Import Firebase Authentication
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.annotation.NonNull;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        registerButton = findViewById(R.id.register);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(RegisterActivity.this, "Authentication Failed.",
                                       Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }
            });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            // Navigate to Home Activity
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
            finish();
        }
    }
}

// HomeActivity.java

package com.example.smartrecipeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        // Implementation to load user-specific data and feature options goes here
    }
}

// Recipe.java
package com.example.smartrecipeorganizer;

import java.util.ArrayList;

public class Recipe {
    private String title;
    private ArrayList<String> ingredients;
    private String preparationSteps;
    private int cookTime;  // in minutes
    private int servings;
    private String mealType;  // "breakfast", "lunch", "dinner", "snacks", "desserts"

    public Recipe(String title, ArrayList<String> ingredients, String preparationSteps, int cookTime, int servings, String mealType) {
        this.title = title;
        this.ingredients = ingredients;
        this.preparationSteps = preparationSteps;
        this.cookTime = cookTime;
        this.servings = servings;
        this.mealType = mealType;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public String getPreparationSteps() {
        return preparationSteps;
    }

    public int getCookTime() {
        return cookTime;
    }

    public int getServings() {
        return servings;
    }

    public String getMealType() {
        return mealType;
    }
}

// AddEditRecipeActivity.java

package com.example.smartrecipeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AddEditRecipeActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText ingredientsEditText;
    private EditText preparationStepsEditText;
    private EditText cookTimeEditText;
    private EditText servingsEditText;
    private EditText mealTypeEditText;
    private Button saveRecipeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recipe);

        titleEditText = findViewById(R.id.title);
        ingredientsEditText = findViewById(R.id.ingredients);
        preparationStepsEditText = findViewById(R.id.preparationSteps);
        cookTimeEditText = findViewById(R.id.cookTime);
        servingsEditText = findViewById(R.id.servings);
        mealTypeEditText = findViewById(R.id.mealType);
        saveRecipeButton = findViewById(R.id.saveRecipe);

        saveRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe();
            }
        });
    }

    private void saveRecipe() {
        String title = titleEditText.getText().toString();
        String ingredientsString = ingredientsEditText.getText().toString();
        String preparationSteps = preparationStepsEditText.getText().toString();
        int cookTime = Integer.parseInt(cookTimeEditText.getText().toString());
        int servings = Integer.parseInt(servingsEditText.getText().toString());
        String mealType = mealTypeEditText.getText().toString();

        ArrayList<String> ingredients = new ArrayList<>();
        for (String ingredient : ingredientsString.split(",")) {
            ingredients.add(ingredient.trim());
        }

        Recipe recipe = new Recipe(title, ingredients, preparationSteps, cookTime, servings, mealType);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        databaseReference.push().setValue(recipe)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddEditRecipeActivity.this, "Recipe saved successfully.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditRecipeActivity.this, "Failed to save recipe.",
                            Toast.LENGTH_SHORT).show();
                }
            });
    }
}

// ProfileActivity.java

package com.example.smartrecipeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Implementation to load and display user profile data goes here
    }
}

// RecipeDetailActivity.java

package com.example.smartrecipeorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class RecipeDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Implementation to load and display recipe details goes here
    }
}

// AndroidManifest.xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.smartrecipeorganizer">
    
    <application
        android:allowBackup="true"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <activity android:name=".LoginActivity" />
        <activity android:name=".RegisterActivity" />
        <activity android:name=".HomeActivity" />
        <activity android:name=".AddEditRecipeActivity" />
        <activity android:name=".RecipeDetailActivity" />
        <activity android:name=".ProfileActivity" />

    </application>
</manifest>

// build.gradle (App level) dependencies
dependencies {
    implementation project(':firebase-auth')
    implementation project(':firebase-database')
    implementation project(':firebase-storage')
    implementation 'com.google.firebase:firebase-auth:21.0.1'
    implementation 'com.google.firebase:firebase-database:20.0.3'
    implementation 'com.google.firebase:firebase-storage:20.0.1'
    implementation 'com.google.android.gms:play-services-auth:19.2.0'
    implementation 'com.google.firebase:firebase-core:20.1.0'
    implementation 'androidx.appcompat:appcompat:1.3.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
}

// XML Layouts
// 1. activity_main.xml - Main activity layout
// 2. activity_login.xml - Login activity layout
// 3. activity_register.xml - Register activity layout
// 4. activity_home.xml - Home activity layout
// 5. activity_add_edit_recipe.xml - Add/Edit recipe activity layout
// 6. activity_profile.xml - Profile activity layout
// 7. activity_recipe_detail.xml - Recipe detail activity layout