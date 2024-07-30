Creating a robust Android weather app with location-based notifications and several other key features is a comprehensive task. Below is a detailed implementation in a single file with all the necessary classes and comments included. Due to constraints on the amount of code, this implementation will cover the essential components of the application.

```java
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private static final String CHANNEL_ID = "WeatherAlerts";

    private WeatherViewModel weatherViewModel;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize View Model
        weatherViewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        final WeatherAdapter adapter = new WeatherAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Observe ViewModel data
        weatherViewModel.getWeatherLiveData().observe(this, new Observer<List<Weather>>() {
            @Override
            public void onChanged(List<Weather> weathers) {
                adapter.setWeathers(weathers);
            }
        });

        // Firebase Authentication
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        } else {
            setupLocationServices();
        }

        createNotificationChannel();
    }

    private void setupLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000); // 60 seconds interval
        locationRequest.setFastestInterval(30000); // 30 seconds fastest interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            double latitude = locationResult.getLastLocation().getLatitude();
            double longitude = locationResult.getLastLocation().getLongitude();
            weatherViewModel.fetchWeatherData(latitude, longitude);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                    setupLocationServices();
                }
            } else {
                Toast.makeText(this, "Sign-in failed, please try again.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Weather Alerts";
            String description = "Channel for weather alerts";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_weather)
                .setContentTitle("Weather Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}

// WeatherViewModel.java
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class WeatherViewModel extends AndroidViewModel {

    private WeatherRepository weatherRepository;
    private MutableLiveData<List<Weather>> weatherLiveData;

    public WeatherViewModel(@NonNull Application application) {
        super(application);
        weatherRepository = new WeatherRepository(application);
        weatherLiveData = weatherRepository.getWeatherLiveData();
    }

    public LiveData<List<Weather>> getWeatherLiveData() {
        return weatherLiveData;
    }

    public void fetchWeatherData(double latitude, double longitude) {
        weatherRepository.fetchWeatherData(latitude, longitude);
    }
}

// WeatherRepository.java
import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class WeatherRepository {

    private MutableLiveData<List<Weather>> weatherLiveData;

    public WeatherRepository(Application application) {
        weatherLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<List<Weather>> getWeatherLiveData() {
        return weatherLiveData;
    }

    public void fetchWeatherData(double latitude, double longitude) {
        // Simulating fetching data from a weather API
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Simulate weather data
                weatherLiveData.setValue(mockWeatherData());
            }
        }, 2000); // Simulating 2 seconds delay
    }

    private List<Weather> mockWeatherData() {
        // Mock data logic
        return null; // Replace with actual data
    }
}

// Weather.java
public class Weather {
    private String day;
    private String description;
    private int temperature;
    
    // Constructor, Getters, Setters
}

// WeatherAdapter.java
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    private final LayoutInflater mInflater;
    private List<Weather> mWeathers;

    public WeatherAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public WeatherV iewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.weather_item, parent, false);
        return new WeatherViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        if (mWeathers != null) {
            Weather current = mWeathers.get(position);
            holder.weatherDay.setText(current.getDay());
            holder.weatherDescription.setText(current.getDescription());
            holder.weatherTemperature.setText(String.valueOf(current.getTemperature()));
        }
    }

    @Override
    public int getItemCount() {
        if (mWeathers != null)
            return mWeathers.size();
        else return 0;
    }

    void setWeathers(List<Weather> weathers){
        mWeathers = weathers;
        notifyDataSetChanged();
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder {
        private final TextView weatherDay;
        private final TextView weatherDescription;
        private final TextView weatherTemperature;

        private WeatherViewHolder(View itemView) {
            super(itemView);
            weatherDay = itemView.findViewById(R.id.weather_day);
            weatherDescription = itemView.findViewById(R.id.weather_description);
            weatherTemperature = itemView.findViewById(R.id.weather_temperature);
        }
    }
}
```

This is a base implementation and omits certain features such as caching offline support, detailed UI design, and complete weather repository implementation. For a fully functional app, consider integrating with actual weather APIs like OpenWeatherMap and implementing specific features as described in the project prompt using appropriate libraries for image loading, local storage, and more.