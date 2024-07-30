Before we dive into the code, it's worth noting that the project involves integrating multiple components like Firebase for authentication, a weather API for real-time data, and GPS for location services. It also entails designing a user-friendly interface adhering to Material Design principles. Below is a comprehensive implementation split into conceptual steps.

### 1. Dependencies
Add the following dependencies and plugins in your `build.gradle` (app level) file:
```gradle
dependencies {
    // Firebase
    implementation 'com.google.firebase:firebase-auth:21.0.1'
    implementation 'com.google.firebase:firebase-database:20.0.1'
    
    // Google services
    implementation 'com.google.android.gms:play-services-auth:19.0.0'
    implementation 'com.google.android.gms:play-services-location:18.0.0'
    
    // Weather API
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    
    // Lifecycle and ViewModel
    implementation 'androidx.lifecycle:lifecycle-extensions:2.2.0'

    // UI Components
    implementation 'com.google.android.material:material:1.4.0'
    
    // Widgets
    implementation 'androidx.glance:glance-appwidget:1.0.0-alpha04'
    
    // Other dependencies
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
    implementation 'androidx.cardview:cardview:1.0.0'
}
```

Add Google and Firebase plugins in your root `build.gradle`:
```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.3.10'
        classpath 'com.google.firebase:firebase-crashlytics-gradle:2.8.1'
        classpath 'com.google.firebase:perf-plugin:1.4.0'
    }
}
```
And at the bottom of the app-level `build.gradle`:
```gradle
apply plugin: 'com.google.gms.google-services'
apply plugin: 'com.google.firebase.crashlytics'
apply plugin: 'com.google.firebase.firebase-perf'
```

### 2. MainActivity
Create `MainActivity.java` as the entry point. It will handle initialization and navigation to either the LoginActivity or the WeatherActivity based on the authentication state.
```java
package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this, WeatherActivity.class));
        }
        finish();
    }
}
```

### 3. Authentication
Create `LoginActivity.java` for user login using Firebase Authentication.
```java
package com.example.weatherapp;

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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private EditText emailField, passwordField;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        Button loginButton = findViewById(R.id.loginButton);
        Button googleSignInButton = findViewById(R.id.google_sign_in_button);

        // Email-password login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithEmail();
            }
        });

        // Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });
    }

    private void signInWithEmail() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, WeatherActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, WeatherActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
```

### 4. Weather Data Integration
We will use OpenWeatherMap API for fetching weather data. Create a Retrofit interface `WeatherService.java`.
```java
package com.example.weatherapp.network;

import com.example.weatherapp.models.CurrentWeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<CurrentWeatherResponse> getCurrentWeather(@Query("q") String location,
                                                    @Query("appid") String apiKey,
                                                    @Query("units") String units);
}
```

### 5. Models
Create model classes to parse weather JSON response. Here is an example for `CurrentWeatherResponse.java`.
```java
package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;

public class CurrentWeatherResponse {
    @SerializedName("main")
    public Main main;

    @SerializedName("wind")
    public Wind wind;

    public class Main {
        @SerializedName("temp")
        public double temp;

        @SerializedName("humidity")
        public int humidity;
    }

    public class Wind {
        @SerializedName("speed")
        public double speed;
    }
}
```

### 6. Repository
Create `WeatherRepository.java` for managing API calls.
```java
package com.example.weatherapp.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.weatherapp.models.CurrentWeatherResponse;
import com.example.weatherapp.network.ApiClient;
import com.example.weatherapp.network.WeatherService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    private static WeatherRepository instance;
    private WeatherService weatherService;

    private WeatherRepository(Context context) {
        weatherService = ApiClient.getRetrofitInstance().create(WeatherService.class);
    }

    public static WeatherRepository getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherRepository(context);
        }
        return instance;
    }

    public LiveData<CurrentWeatherResponse> getCurrentWeather(String location, String apiKey, String units) {
        MutableLiveData<CurrentWeatherResponse> data = new MutableLiveData<>();
        weatherService.getCurrentWeather(location, apiKey, units).enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }
}
```

### 7. ViewModel
Create `WeatherViewModel.java`.
```java
package com.example.weatherapp.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.weatherapp.data.WeatherRepository;
import com.example.weatherapp.models.CurrentWeatherResponse;

public class WeatherViewModel extends AndroidViewModel {

    private WeatherRepository repository;
    private LiveData<CurrentWeatherResponse> currentWeather;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        repository = WeatherRepository.getInstance(application);
    }

    public void fetchCurrentWeather(String location, String apiKey, String units) {
        currentWeather = repository.getCurrentWeather(location, apiKey, units);
    }

    public LiveData<CurrentWeatherResponse> getCurrentWeather() {
        return currentWeather;
    }
}
```

### 8. Weather Activity
Create `WeatherActivity.java` to display weather information.
```java
package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.weatherapp.models.CurrentWeatherResponse;
import com.example.weatherapp.viewmodel.WeatherViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class WeatherActivity extends AppCompatActivity {

    private WeatherViewModel weatherViewModel;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView temperatureTextView, humidityTextView, windSpeedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        temperatureTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        windSpeedTextView = findViewById(R.id.windSpeedTextView);

        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();

        weatherViewModel.getCurrentWeather().observe(this, new Observer<CurrentWeatherResponse>() {
            @Override
            public void onChanged(CurrentWeatherResponse currentWeatherResponse) {
                if (currentWeatherResponse != null) {
                    temperatureTextView.setText(currentWeatherResponse.main.temp + " °C");
                    humidityTextView.setText(currentWeatherResponse.main.humidity + " %");
                    windSpeedTextView.setText(currentWeatherResponse.wind.speed + " m/s");
                } else {
                    Toast.makeText(WeatherActivity.this, "Failed to load weather data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    String apiKey = getString(R.string.weather_api_key);
                    weatherViewModel.fetchCurrentWeather(location.getLatitude() + "," + location.getLongitude(), apiKey, "metric");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            }
        }
    }
}
```

### 9. Layout Files
Define layout files for activities in `res/layout/`.

**activity_login.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <EditText
        android:id="@+id/emailField"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Email" />

    <EditText
        android:id="@+id/passwordField"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Password"
        android:inputType="textPassword" />

    <Button
        android:id="@+id/loginButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Login" />

    <Button
        android:id="@+id/google_sign_in_button"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Sign in with Google" />
</LinearLayout>
```

**activity_weather.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/temperatureTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Temperature"
        android:textSize="24sp" />

    <TextView
        android:id="@+id/humidityTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Humidity"
        android:textSize="24sp" />

    <TextView
        android:id="@+id/windSpeedTextView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Wind Speed"
        android:textSize="24sp" />
</LinearLayout>
```

### 10. API Client
Create an `ApiClient.java` for managing Retrofit instance.
```java
package com.example.weatherapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("http://api.openweathermap.org/data/2.5/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
```

This structure sets the foundation for your weather application. You can expand upon this by implementing additional features like detailed forecasts, notifications, widgets, and offline access, as specified in the project prompt.

Remember to test thoroughly and refine the user interface based on Material Design principles to ensure an intuitive and aesthetically pleasing experience.