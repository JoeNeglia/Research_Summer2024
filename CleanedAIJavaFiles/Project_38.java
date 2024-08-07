package com.example.weatherfitnessapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

// MainActivity class
class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val TAG = "WeatherFitnessApp"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Check Google Fit permissions
        requestGoogleFitPermission()

        // Check Location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 
                LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            getCurrentLocation()
        }

        // Fetch weather data
        fetchWeatherData()

        // Initialize Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        sign_in_button.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Sign out
        sign_out_button.setOnClickListener {
            auth.signOut()
            updateUI(null)
        }

        // On click listener for button to set fitness goals (not shown in XML, hypothetical UI component)
        set_goal_button.setOnClickListener {
            // Set fitness goals
        }
    }

    private fun fetchWeatherData() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.openweathermap.org/data/2.5/weather?q=London&appid=YOUR_API_KEY")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Failed to fetch weather data")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.body?.let { body ->
                    val json = JSONObject(body.string())
                    val main = json.getJSONObject("main")
                    val weather = json.getJSONArray("weather").getJSONObject(0)
                    val temp = main.getDouble("temp")
                    val description = weather.getString("description")

                    runOnUiThread {
                        weather_temp.text = "${temp - 273.15} °C"
                        weather_description.text = description
                    }
                }
            }
        })
    }

    private fun getCurrentLocation() {
        // Use FusedLocationProviderClient to get the user's location
        // ...
    }

    private fun requestGoogleFitPermission() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                account,
                fitnessOptions
            )
        } else {
            subscribeToStepCount()
        }
    }

    private fun subscribeToStepCount() {
        val fitnessClient = Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this)!!)
        fitnessClient.subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully subscribed!")
            }
            .addOnFailureListener {
                Log.w(TAG, "There was a problem subscribing.", it)
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            user_email.text = user.email
            user_display_name.text = user.displayName
            
            // Load user profile image using Glide
            Glide.with(this).load(user.photoUrl).into(profile_image_view)

            sign_in_button.visibility = GONE
            sign_out_button.visibility = VISIBLE
        } else {
            user_email.text = ""
            user_display_name.text = ""
            profile_image_view.setImageResource(R.drawable.ic_account_circle)

            sign_in_button.visibility = VISIBLE
            sign_out_button.visibility = GONE
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    }
}