// EcoHeroApp.java - Main Application File

package com.example.ecohero;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class EcoHeroApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}

// LoginActivity.java - User Authentication

package com.example.ecohero;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginButton, signUpButton, loginWithGoogleButton;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        loginWithGoogleButton = findViewById(R.id.loginWithGoogleButton);

        // Set up Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        loginWithGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success
                    FirebaseUser user = mAuth.getCurrentUser();
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signUpUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign up success
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(LoginActivity.this, "Sign Up Successful.", Toast.LENGTH_SHORT).show();
                } else {
                    // If sign up fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "Sign Up Failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                // Successfully signed in
                GoogleSignInAccount account = task.getResult();
                firebaseAuthWithGoogle(account);
            } else {
                // Sign in failed
                Toast.makeText(this, "Google Sign-In Failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign in failed
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}

// DashboardActivity.java - User Dashboard

package com.example.ecohero;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();

        welcomeMessage = findViewById(R.id.welcomeMessage);

        // Load default fragment
        loadFragment(new DailyTipsFragment());

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.nav_tips:
                        selectedFragment = new DailyTipsFragment();
                        break;
                    case R.id.nav_challenges:
                        selectedFragment = new ChallengesFragment();
                        break;
                    case R.id.nav_community:
                        selectedFragment = new CommunityFragment();
                        break;
                }
                return loadFragment(selectedFragment);
            }
        });

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            welcomeMessage.setText("Welcome, " + user.getDisplayName());
        }
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                mAuth.signOut();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(DashboardActivity.this, SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

// Fragments (Daily Tips, Challenges, Community)

// DailyTipsFragment.java

package com.example.ecohero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DailyTipsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TipsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_tips, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        List<String> tips = new ArrayList<>();
        tips.add("Use reusable bags instead of plastic bags.");
        tips.add("Conserve water by taking shorter showers.");
        tips.add("Turn off lights when you leave a room.");

        adapter = new TipsAdapter(tips);
        recyclerView.setAdapter(adapter);

        return view;
    }
}

// ChallengesFragment.java

package com.example.ecohero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
    ...
    // Implementation similar to DailyTipsFragment
}

// CommunityFragment.java

package com.example.ecohero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
    ...
    // Implementation similar to DailyTipsFragment
}

// RecyclerView Adapters

// TipsAdapter.java

package com.example.ecohero;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TipsAdapter extends RecyclerView.Adapter<TipsAdapter.TipsViewHolder> {

    private List<String> tipsList;

    public TipsAdapter(List<String> tipsList) {
        this.tipsList = tipsList;
    }

    @NonNull
    @Override
    public TipsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
        return new TipsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipsViewHolder holder, int position) {
        String tip = tipsList.get(position);
        holder.tipTextView.setText(tip);
    }

    @Override
    public int getItemCount() {
        return tipsList.size();
    }

    public static class TipsViewHolder extends RecyclerView.ViewHolder {

        TextView tipTextView;

        public TipsViewHolder(@NonNull View itemView) {
            super(itemView);
            tipTextView = itemView.findViewById(R.id.tipTextView);
        }
    }
}

// Layout files would look somewhat like the following:

// res/layout/activity_login.xml

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="16dp">

    <EditText
        android:id="@+id/emailEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Email"/>

    <EditText
        android:id="@+id/passwordEditText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:inputType="textPassword"/>

    <Button
        android:id="@+id/loginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login"/>

    <Button
        android:id="@+id/signUpButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Sign Up"/>

    <Button
        android:id="@+id/loginWithGoogleButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login with Google"/>
</LinearLayout>

// res/layout/activity_dashboard.xml

<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:id="@+id/welcomeMessage"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_centerHorizontal="true"
        android:text="Welcome"
        android:textSize="18sp"/>

    <FrameLayout
        android:id="@+id/fragment_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/welcomeMessage"/>

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_navigation"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:background="?android:attr/windowBackground"
        app:menu="@menu/bottom_nav_menu"/>
</RelativeLayout>

// res/layout/fragment_daily_tips.xml

<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
</LinearLayout>

// res/menu/bottom_nav_menu.xml

<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_tips"
        android:icon="@drawable/ic_tips"
        android:title="Tips"/>
    <item
        android:id="@+id/nav_challenges"
        android:icon="@drawable/ic_challenges"
        android:title="Challenges"/>
    <item
        android:id="@+id/nav_community"
        android:icon="@drawable/ic_community"
        android:title="Community"/>
</menu>

// Add remaining Fragments and RecyclerView Adapters similarly.