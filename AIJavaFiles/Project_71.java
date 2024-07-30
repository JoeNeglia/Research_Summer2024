```java
package com.example.weatheroo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatheroo.api.WeatherApi;
import com.example.weatheroo.database.WeatherDatabase;
import com.example.weatheroo.models.WeatherResponse;
import com.example.weatheroo.ui.CurrentWeatherFragment;
import com.example.weatheroo.ui.ForecastAdapter;
import com.example.weatheroo.ui.WeatherViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private WeatherViewModel weatherViewModel;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private static final String CHANNEL_ID = "weather_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize location services API
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the WeatherViewModel
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        // Set up Retrofit to consume the weather API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WeatherApi weatherApi = retrofit.create(WeatherApi.class);

        // Request location
        getCurrentLocation(weatherApi);

        // Set up RecyclerView for 5-day forecast
        RecyclerView recyclerView = findViewById(R.id.forecast_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ForecastAdapter adapter = new ForecastAdapter();
        recyclerView.setAdapter(adapter);

        // Observe the weather data
        weatherViewModel.getWeatherData().observe(this, weatherResponse -> {
            if (weatherResponse != null) {
                // Update UI
                adapter.setWeatherList(weatherResponse.getForecastList());
                // Implement UI update for current weather and map as well.
            }
        });

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Set up the weather alert notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    /**
     * Request location permissions and fetch the current location.
     */
    private void getCurrentLocation(WeatherApi weatherApi) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED 
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    fetchWeatherData(weatherApi, location.getLatitude(), location.getLongitude());
                }
            });
    }

    /**
     * Fetch weather data using the API.
     */
    private void fetchWeatherData(WeatherApi weatherApi, double lat, double lon) {
        weatherApi.getCurrentWeather(lat, lon, "your_api_key", "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            weatherViewModel.setWeatherData(response.body());
                            displayWeatherAlerts(response.body());
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to get weather data", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                        Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Display weather alerts as notifications.
     */
    private void displayWeatherAlerts(WeatherResponse weatherResponse) {
        if (weatherResponse.getWeather().get(0).getId() >= 200 
                && weatherResponse.getWeather().get(0).getId() <= 232) { // Assuming storm alerts have IDs between 200-232
            showNotification(weatherResponse.getWeather().get(0).getDescription());
        }
    }

    /**
     * Shows notifications for severe weather conditions.
     */
    private void showNotification(String alertMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather_alert)
                .setContentTitle("Weather Alert")
                .setContentText(alertMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence name = "Weather Alerts";
        String description = "Channel for weather alerts";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation((WeatherApi) new Retrofit.Builder()
                        .baseUrl("https://api.openweathermap.org/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(WeatherApi.class));
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(-34, 151); // Example coordinates
        mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
    }
}
```

**Notes on Implementation:**

1. **MainActivity:** This class encompasses the bulk of the application logic. It initializes location services, sets up the RecyclerView for weather forecasts, and handles weather API calls.
2. **WeatherViewModel:** Manages UI-related weather data, adhering to lifecycle-aware principles.
3. **CurrentWeatherFragment, ForecastAdapter:** These components display current weather details and 5-day forecasts.
4. **WeatherApi and WeatherResponse:** Handle weather API integration and JSON parsing.
5. **Google Maps:** Shows a map with weather conditions. This can be expanded to show overlays or more detailed information.
6. **Notification Channels:** Used to send weather alerts about severe conditions like storms.

Replace `"your_api_key"` with your actual API key from the weather service you decide to use.

To complete the project, you would need to implement additional UI elements, like the `CurrentWeatherFragment`, the `ForecastAdapter`, other required adapters, and the database for saving user preferences and widget customization. Adding WorkManager for background updates and AppWidgetProvider for the home screen widgets would also be essential steps. Integration of voice commands and weather trends are bonus features that could be added if time permits.