Below is the comprehensive code for the Urban Bike Sharing App. Note that writing the entire code in a single file is not feasible for a complex project like this. Instead, I will provide a structured overview with the necessary code snippets for critical parts to guide you in creating the full project.

Let's assume each section corresponds to a piece of code from different files of the project:

### Gradle Build File (`build.gradle`)
```gradle
// Project level build.gradle
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:7.0.4'
        classpath 'com.google.gms:google-services:4.3.10'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

// Application level build.gradle
apply plugin: 'com.android.application'
apply plugin: 'com.google.gms.google-services'

android {
    compileSdkVersion 31
    defaultConfig {
        applicationId "com.example.urbanbikesharing"
        minSdkVersion 21
        targetSdkVersion 31
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
        }
    }
}

dependencies {
    implementation 'com.google.firebase:firebase-auth:21.0.3'
    implementation 'com.google.firebase:firebase-database:20.0.4'
    implementation 'com.google.android.gms:play-services-maps:18.0.2'
    implementation 'com.google.android.gms:play-services-location:19.0.1'
    implementation 'com.google.code.gson:gson:2.8.8'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.stripe:stripe-android:20.17.0'
    implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
    implementation 'com.squareup.okhttp3:okhttp:4.9.3'
    implementation 'com.android.support:design:28.0.0'
}
```

### Firebase Setup (`google-services.json`)

Download your `google-services.json` file from Firebase Console and place it in the `app` directory.

### Main Application File (MainActivity.java)
```java
package com.example.urbanbikesharing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private Button btnSignOut, btnReserveBike;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        
        btnSignOut = findViewById(R.id.btnSignOut);
        btnReserveBike = findViewById(R.id.btnReserveBike);
        
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnReserveBike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReserveBikeActivity.class);
                startActivity(intent);
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
```

### User Authentication (LoginActivity.java)
```java
package com.example.urbanbikesharing;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private EditText emailField, passwordField;
    private Button btnLogin, btnSignUp, btnGoogleSignIn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(emailField.getText().toString(), passwordField.getText().toString());
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("LoginActivity", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}
```

### Bike Reservation (ReserveBikeActivity.java)
```java
package com.example.urbanbikesharing;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ReserveBikeActivity extends AppCompatActivity {
    private DatabaseReference bikeReference;
    private String userId, bikeId;
    private long reservationTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_bike);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bikeReference = FirebaseDatabase.getInstance().getReference("bikes");
        bikeId = "BIKE123"; // This should be fetched based on nearby available bikes
        reservationTime = System.currentTimeMillis();

        reserveBike();
    }

    private void reserveBike() {
        bikeReference.child(bikeId).child("reserved").setValue(true);
        bikeReference.child(bikeId).child("reserved_by").setValue(userId);
        bikeReference.child(bikeId).child("reservation_time").setValue(reservationTime);
    }
}
```

### Map Integration (MapActivity.java)
```java
package com.example.urbanbikesharing;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng bikeStation = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(bikeStation).title("Bike Station"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bikeStation, 15));
    }
}
```

### Bike Unlocking (UnlockBikeActivity.java)
```java
package com.example.urbanbikesharing;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class UnlockBikeActivity extends AppCompatActivity {
    private String bikeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_bike);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan Bike QR Code");
        integrator.setCameraId(0);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                finish();
            } else {
                bikeId = result.getContents();
                unlockBike();
            }
        }
    }

    private void unlockBike() {
        // Implement unlocking logic here
    }
}
```

### Ride Management & Tracking (RideActivity.java)
```java
package com.example.urbanbikesharing;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RideActivity extends AppCompatActivity {
    private DatabaseReference rideReference;
    private String userId, rideId;
    private long rideStartTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rideReference = FirebaseDatabase.getInstance().getReference("rides");
        rideId = rideReference.push().getKey();
        rideStartTime = System.currentTimeMillis();

        startRide();
    }

    private void startRide() {
        rideReference.child(rideId).child("userId").setValue(userId);
        rideReference.child(rideId).child("startTime").setValue(rideStartTime);
        rideReference.child(rideId).child("bikeId").setValue("BIKE123"); // Get bikeId from reserved bike
    }
}
```

### Layout Files

#### `activity_main.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <Button
        android:id="@+id/btnSignOut"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_centerInParent="true"
        android:text="Sign Out" />

    <Button
        android:id="@+id/btnReserveBike"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/btnSignOut"
        android:layout_centerInParent="true"
        android:text="Reserve Bike" />
</RelativeLayout>
```

#### `activity_login.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".LoginActivity">

    <EditText
        android:id="@+id/emailField"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Email" />

    <EditText
        android:id="@+id/passwordField"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/emailField"
        android:hint="Password"
        android:inputType="textPassword" />

    <Button
        android:id="@+id/btnLogin"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/passwordField"
        android:layout_centerHorizontal="true"
        android:text="Login" />

    <Button
        android:id="@+id/btnSignUp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/btnLogin"
        android:layout_centerHorizontal="true"
        android:text="Sign Up" />

    <Button
        android:id="@+id/btnGoogleSignIn"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/btnSignUp"
        android:layout_centerHorizontal="true"
        android:text="Google Sign In" />
</RelativeLayout>
```

#### `activity_reserve_bike.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".ReserveBikeActivity">

    <!-- Add UI elements for displaying bike reservation info -->
</RelativeLayout>
```

#### `activity_map.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MapActivity">

    <fragment
        android:id="@+id/map"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:name="com.google.android.gms.maps.SupportMapFragment"/>
</RelativeLayout>
```

#### `activity_unlock_bike.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".UnlockBikeActivity">

    <!-- Add UI for QR code scanning if needed -->
</RelativeLayout>
```

#### `activity_ride.xml`
```xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".RideActivity">

    <!-- Add UI elements for ride status -->
</RelativeLayout>
```

### Summary

The provided code samples cover key functionalities of the Urban Bike Sharing App, including user authentication, bike reservation, map integration, bike unlocking, and ride management. Each section represents a class or activity in the Android application.

You should split the provided code into separate files corresponding to each class (`MainActivity.java`, `LoginActivity.java`, `ReserveBikeActivity.java`, etc.) and XML layout file (`activity_main.xml`, `activity_login.xml`, etc.). Ensure that you configure Firebase and the necessary APIs correctly in your project.

### Further Implementation

- **In-App Payments**: Integrate a payment gateway such as Stripe by following the Stripe documentation for Android.
- **Notifications**: Use Firebase Cloud Messaging (FCM) to send push notifications.
- **Customer Support**: Set up a help and support section, including a chat feature.

Continue to modularize and enhance the project by following best practices for a production-grade application.