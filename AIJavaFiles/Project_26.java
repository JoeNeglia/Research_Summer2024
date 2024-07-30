Sure! Below, you'll find a comprehensive implementation of the "Weather Exploration App" in a single file format. This is a sizable project, so I'll ensure all core components are included. Some implementation details like API keys and certain UI elements (due to limitations of a text interface) are generalized but should give you a clear structure to follow:

```java
// Import necessary libraries

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


// MainActivity.java

public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView temperatureView, humidityView, windSpeedView, weatherConditionView;
    private ImageView weatherIcon;
    private LineChart weatherForecastChart;
    private Button refreshButton;

    private LocationManager locationManager;
    private String weatherApiKey = "YOUR_API_KEY";
    private String baseUrl = "http://api.openweathermap.org/data/2.5/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        temperatureView = findViewById(R.id.temperatureView);
        humidityView = findViewById(R.id.humidityView);
        windSpeedView = findViewById(R.id.windSpeedView);
        weatherConditionView = findViewById(R.id.weatherConditionView);
        weatherIcon = findViewById(R.id.weatherIcon);
        weatherForecastChart = findViewById(R.id.weatherForecastChart);
        refreshButton = findViewById(R.id.refreshButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWeatherUpdates();
            }
        });

        requestWeatherUpdates();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        requestWeatherData(latitude, longitude);
    }

    private void requestWeatherUpdates() {
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        requestWeatherData(latitude, longitude);
    }

    private void requestWeatherData(double latitude, double longitude) {

        OkHttpClient client = new OkHttpClient();
        String url = baseUrl + "weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + weatherApiKey;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

            @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            String jsonData = response.body().string();
            runOnUiThread(() -> {
                try {
                    parseWeatherData(jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
        });
    }

    private void parseWeatherData(String jsonData) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonData);

        JSONObject main = jsonObject.getJSONObject("main");
        String temperature = main.getString("temp");
        String humidity = main.getString("humidity");

        JSONObject wind = jsonObject.getJSONObject("wind");
        String windSpeed = wind.getString("speed");

        String weatherCondition = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");
        String icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");

        updateWeatherUI(temperature, humidity, windSpeed, weatherCondition, icon);
    }

    private void updateWeatherUI(String temperature, String humidity, String windSpeed, String weatherCondition, String icon) {
        temperatureView.setText(temperature + "°C");
        humidityView.setText("Humidity: " + humidity + "%");
        windSpeedView.setText("Wind Speed: " + windSpeed + " m/s");
        weatherConditionView.setText(weatherCondition);
        Glide.with(this).load("http://openweathermap.org/img/w/" + icon + ".png").into(weatherIcon);

        updateWeatherForecastChart();
    }

    private void updateWeatherForecastChart() {
        OkHttpClient client = new OkHttpClient();
        String forecastUrl = baseUrl + "forecast?q=YOUR_CITY&appid=" + weatherApiKey;

        Request request = new Request.Builder()
                .url(forecastUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

            @Override
    public void onResponse(Call call, Response response) throws IOException {
        if (response.isSuccessful()) {
            String jsonData = response.body().string();
            runOnUiThread(() -> {
                try {
                    parseWeatherForecastData(jsonData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }
        });
    }

    private void parseWeatherForecastData(String jsonData) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonData);
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < jsonObject.getJSONArray("list").length(); i++) {
            JSONObject forecast = jsonObject.getJSONArray("list").getJSONObject(i);
            String date = forecast.getString("dt_txt");
            double temperature = forecast.getJSONObject("main").getDouble("temp");

            entries.add(new Entry(i, (float) temperature));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temperature");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextColor(ColorTemplate.getHoloBlue());
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);

        weatherForecastChart.setData(lineData);
        weatherForecastChart.invalidate(); // refresh

        Legend legend = weatherForecastChart.getLegend();
        legend.setTextColor(ColorTemplate.getHoloBlue());

        weatherForecastChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
```

This code includes:

1. **Location Services**: Detect user's location and request weather data based on it.
2. **Weather Data Fetching**: Fetch real-time weather data using the OpenWeatherMap API.
3. **UI Update**: Display current weather, temperature, humidity, wind speed, and weather conditions with meaningful icons.
4. **Weather Forecast**: Display a basic weather forecast using MPAndroidChart for data visualization.

Please ensure to replace `"YOUR_API_KEY"` with your actual API Key from OpenWeatherMap, and `"YOUR_CITY"` with your targeted city for weather forecasts.

Note: This sample does not include the complete implementation of all project features, UI elements in XML layout files, and full scope functionalities like environmental alerts, personalized notifications, additional functionalities, etc., due to space and complexity constraints. However, it forms a solid base for further extensions.