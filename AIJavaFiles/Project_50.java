```kotlin
package com.example.myfitnesspalplus

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import kotlin.properties.Delegates

// Main Application class
class MyFitnessPalPlusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

data class Metric(
    val id: String? = null,
    val type: String = "",
    val value: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class User(
    val id: String,
    val name: String,
    val email: String
)

// ViewModel for handling metrics
class MetricsViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val metricsLiveData = MutableLiveData<List<Metric>>()

    fun addMetric(metric: Metric) {
        firestore.collection("users").document(userId)
            .collection("metrics").add(metric)
    }

    fun fetchMetrics() {
        firestore.collection("users").document(userId)
            .collection("metrics")
            .get()
            .addOnSuccessListener { result ->
                val metrics = result.map { document -> document.toObject(Metric::class.java) }
                metricsLiveData.value = metrics
            }
    }
}

// ViewModel for user authentication
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    val userLiveData = MutableLiveData<FirebaseUser?>()

    init {
        userLiveData.value = auth.currentUser
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userLiveData.value = auth.currentUser
                } else {
                    userLiveData.value = null
                }
            }
    }

    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userLiveData.value = auth.currentUser
                } else {
                    userLiveData.value = null
                }
            }
    }

    fun logout() {
        auth.signOut()
        userLiveData.value = null
    }
}

// Base Activity for handling common features like Bottom Navigation
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_dashboard -> {
                    // TODO: Navigate to Dashboard
                    true
                }
                R.id.action_tracking -> {
                    // TODO: Navigate to Tracking
                    true
                }
                R.id.action_profile -> {
                    // TODO: Navigate to Profile
                    true
                }
                else -> false
            }
        }
    }
}

// Activity for User Authentication
class AuthActivity : AppCompatActivity() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login_button)
        val registerButton = findViewById<Button>(R.id.register_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            authViewModel.login(email, password)
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            authViewModel.register(email, password)
        }

        authViewModel.userLiveData.observe(this, Observer { user ->
            if (user != null) {
                // TODO: Navigate to MainActivity
            } else {
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// Activity for Main Dashboard
class MainActivity : BaseActivity() {
    private lateinit var metricsViewModel: MetricsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        metricsViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(MetricsViewModel::class.java)

        val exerciseButton = findViewById<Button>(R.id.exercise_button)
        exerciseButton.setOnClickListener {
            // TODO: Navigate to Exercise Tracking
        }

        val calorieButton = findViewById<Button>(R.id.calorie_button)
        calorieButton.setOnClickListener {
            // TODO: Navigate to Calorie Counter
        }

        val waterButton = findViewById<Button>(R.id.water_button)
        waterButton.setOnClickListener {
            // TODO: Navigate to Water Intake Tracking
        }

        val sleepButton = findViewById<Button>(R.id.sleep_button)
        sleepButton.setOnClickListener {
            // TODO: Navigate to Sleep Tracking
        }

        // Observe metrics and update UI
        metricsViewModel.metricsLiveData.observe(this, Observer { metrics ->
            // TODO: Update dashboard UI
        })
    }
}

// Fragment for Exercise Tracking
class ExerciseFragment : Fragment() {
    private lateinit var metricsViewModel: MetricsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercise, container, false)
        metricsViewModel = ViewModelProvider(this).get(MetricsViewModel::class.java)

        val exerciseTypeEditText = view.findViewById<EditText>(R.id.exercise_type)
        val exerciseDurationEditText = view.findViewById<EditText>(R.id.exercise_duration)
        val logExerciseButton = view.findViewById<Button>(R.id.log_exercise_button)

        logExerciseButton.setOnClickListener {
            val type = exerciseTypeEditText.text.toString()
            val duration = exerciseDurationEditText.text.toString().toInt()

            val metric = Metric(
                type = "exercise",
                value = duration, // Assuming duration in minutes
                timestamp = System.currentTimeMillis()
            )
            metricsViewModel.addMetric(metric)
        }

        return view
    }
}

// Fragment for Calorie Tracking
class CalorieFragment : Fragment() {
    private lateinit var metricsViewModel: MetricsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calorie, container, false)
        metricsViewModel = ViewModelProvider(this).get(MetricsViewModel::class.java)

        val foodNameEditText = view.findViewById<EditText>(R.id.food_name)
        val calorieCountEditText = view.findViewById<EditText>(R.id.calorie_count)
        val logCalorieButton = view.findViewById<Button>(R.id.log_calorie_button)

        logCalorieButton.setOnClickListener {
            val food = foodNameEditText.text.toString()
            val calories = calorieCountEditText.text.toString().toInt()

            val metric = Metric(
                type = "calorie",
                value = calories,
                timestamp = System.currentTimeMillis()
            )
            metricsViewModel.addMetric(metric)
        }

        return view
    }
}

// More Fragments for Water Tracking, Sleep Tracking, etc. would be similarly implemented.

// Extending Firebase Messaging for Notifications & Reminders
class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null) {
            sendNotification(remoteMessage.notification!!.body!!)
        }
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("MyFitnessPal+")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder)
    }
}

// Main Application class and other classes...
```

This code provides a skeleton for a comprehensive Android health and wellness tracking app. It includes user authentication, a dashboard, exercise tracking, calorie tracking, and more. It relies on Firebase Firestore for backend storage and authentication. Firebase Messaging is used for notifications, and fragments are utilized for different tracking features.

This is just the starting framework. For full implementation, you would continue developing each feature, including integration with the Google Fit API for exercise and sleep tracking, the Nutritionix API for calorie data, and additional UI components following Material Design guidelines. Furthermore, you would add comprehensive error handling, input validation, and unit tests to ensure the app's robustness and quality.