// MainActivity.java - The entry point of the app
package com.example.indoor_navigation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.List;

public class MainActivity extends FragmentActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = "MainActivity";

    private ImageView mapView;
    private TextView locationTextView;
    private Button btnNavigate;

    private LocationManager locationManager; // Handles user localization via BLE or Wi-Fi
    private MapManager mapManager; // Manages map rendering and switching floors
    private PathFinder pathFinder; // Implements the pathfinding algorithm

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        locationTextView = findViewById(R.id.locationTextView);
        btnNavigate = findViewById(R.id.btnNavigate);

        locationManager = new LocationManager(this);
        mapManager = new MapManager(this, mapView);
        pathFinder = new PathFinder(mapManager);

        btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                startActivity(intent);
            }
        });

        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            startLocalization();
        }
    }

    private void startLocalization() {
        locationManager.startLocalization(new LocationManager.LocalizationCallback() {
            @Override
            public void onLocationUpdated(Location location) {
                locationTextView.setText("Current Location: " + location.getName());
                mapManager.updateUserPosition(location);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocalization();
            } else {
                Log.e(TAG, "Location permission denied.");
            }
        }
    }
}

// NavigationActivity.java - Handles the navigation functionality
package com.example.indoor_navigation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

public class NavigationActivity extends FragmentActivity {

    private TextView currentLocationTextView;
    private TextView destinationTextView;
    private TextView navigationInstructionsTextView;

    private Button btnStartNavigation;

    private LocationManager locationManager;
    private PathFinder pathFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        currentLocationTextView = findViewById(R.id.currentLocationTextView);
        destinationTextView = findViewById(R.id.destinationTextView);
        navigationInstructionsTextView = findViewById(R.id.navigationInstructionsTextView);

        btnStartNavigation = findViewById(R.id.btnStartNavigation);

        locationManager = new LocationManager(this);
        pathFinder = new PathFinder(new MapManager(this, null));

        btnStartNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation();
            }
        });
    }

    private void startNavigation() {
        Location currentLocation = locationManager.getCurrentLocation();
        Location destinationLocation = new Location("Store #100", 5, 10); // Example destination

        currentLocationTextView.setText("Current Location: " + currentLocation.getName());
        destinationTextView.setText("Destination: " + destinationLocation.getName());

        List<Location> path = pathFinder.findPath(currentLocation, destinationLocation);
        displayNavigationInstructions(path);
    }

    private void displayNavigationInstructions(List<Location> path) {
        StringBuilder instructions = new StringBuilder();
        for (Location location : path) {
            instructions.append(" -> ").append(location.getName());
        }
        navigationInstructionsTextView.setText("Route: " + instructions.toString());
    }
}

// LocationManager.java - Manages user localization using BLE or Wi-Fi
package com.example.indoor_navigation;

import android.content.Context;

public class LocationManager {

    private Context context;
    private Location currentLocation;

    public interface LocalizationCallback {
        void onLocationUpdated(Location location);
    }

    public LocationManager(Context context) {
        this.context = context;
    }

    public void startLocalization(LocalizationCallback callback) {
        // Mock implementation for BLE/Wi-Fi based localization
        // Replace this with actual BLE/Wi-Fi processes
        currentLocation = new Location("Entrance", 0, 0);
        callback.onLocationUpdated(currentLocation);
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}

// MapManager.java - Handles map rendering and floor switching
package com.example.indoor_navigation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class MapManager {

    private Context context;
    private ImageView mapView;

    public MapManager(Context context, ImageView mapView) {
        this.context = context;
        this.mapView = mapView;
    }

    public void updateUserPosition(Location location) {
        // Mock implementation to update user position on the map
        // Replace this with actual map rendering processes
        if (mapView != null) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.mock_map); // Mock map
            mapView.setImageBitmap(bitmap);
        }
    }
}

// PathFinder.java - Implements the pathfinding algorithm
package com.example.indoor_navigation;

import java.util.ArrayList;
import java.util.List;

public class PathFinder {

    private MapManager mapManager;

    public PathFinder(MapManager mapManager) {
        this.mapManager = mapManager;
    }

    public List<Location> findPath(Location start, Location end) {
        // Mock implementation of pathfinding algorithm
        // Replace this with an actual pathfinding algorithm (e.g., A* algorithm)
        List<Location> path = new ArrayList<>();
        path.add(start);
        path.add(new Location("Midpoint", 2, 2));
        path.add(end);
        return path;
    }
}

// Location.java - Represents a location in the mall
package com.example.indoor_navigation;

public class Location {

    private String name;
    private int x;
    private int y;

    public Location(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}