// "Daily Health Tracker" Android Application

// Import necessary packages
import android.app.*
import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.*
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.facebook.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.google.firebase.firestore.*
import com.google.firebase.storage.*
import com.squareup.picasso.*
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.utils.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Firebase initialization
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Check if user is authenticated
        if (auth.currentUser == null) {
            // Launch authentication flow
            launchSignInFlow()
        } else {
            // Initialize activity tracking view
            initActivityTrackingView()
        }
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                initActivityTrackingView()
            } else {
                // Sign in failed, handle error
                Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initActivityTrackingView() {
        // Initialize views and set up listeners
        val btnLogExercise = findViewById<Button>(R.id.btnLogExercise)
        val btnLogSleep = findViewById<Button>(R.id.btnLogSleep)
        val btnLogWater = findViewById<Button>(R.id.btnLogWater)
        val btnLogMeal = findViewById<Button>(R.id.btnLogMeal)
        val btnSetGoals = findViewById<Button>(R.id.btnSetGoals)

        btnLogExercise.setOnClickListener { logActivity("Exercise") }
        btnLogSleep.setOnClickListener { logActivity("Sleep") }
        btnLogWater.setOnClickListener { logActivity("Water") }
        btnLogMeal.setOnClickListener { logActivity("Meal") }
        btnSetGoals.setOnClickListener { setGoals() }

        // Load user data and display statistics
        loadUserData()
    }

    private fun logActivity(activityType: String) {
        // Show dialog to log activity based on type
        val dialog = LogActivityDialog(activityType, auth.currentUser!!.uid)
        dialog.show(supportFragmentManager, "LogActivityDialog")
    }

    private fun setGoals() {
        // Show goal setting activity
        val intent = Intent(this, GoalSettingActivity::class.java)
        startActivity(intent)
    }

    private fun loadUserData() {
        // Load and display user activity statistics
        val userId = auth.currentUser!!.uid
        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (document != null) {
                val activityStats = document.data!!["activity_stats"] as Map<String, Any>
                displayStats(activityStats)
            }
        }
    }

    private fun displayStats(activityStats: Map<String, Any>) {
        val statsChart = findViewById<BarChart>(R.id.statsChart)
        val entries = ArrayList<BarEntry>()
        
        for ((index, value) in activityStats.values.withIndex()) {
            entries.add(BarEntry(index.toFloat(), value.toString().toFloat()))
        }

        val dataSet = BarDataSet(entries, "Daily Activities")
        val barData = BarData(dataSet)
        statsChart.data = barData
        statsChart.invalidate() // Refresh chart
    }

    companion object {
        private const val RC_SIGN_IN = 123
    }
}

class LogActivityDialog(private val activityType: String, private val userId: String) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_log_activity, container, false)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val etAmount = view.findViewById<EditText>(R.id.etAmount)
        
        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().toFloat()
            logActivityToFirebase(activityType, amount)
        }
        
        return view
    }

    private fun logActivityToFirebase(activityType: String, amount: Float) {
        // Log activity data to Firebase
        val activityData = hashMapOf(
            "type" to activityType,
            "amount" to amount,
            "timestamp" to System.currentTimeMillis()
        )
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .collection("activities").add(activityData).addOnSuccessListener {
                dismiss() // Close dialog
            }
    }
}

class GoalSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_setting)
        
        val btnSaveGoals = findViewById<Button>(R.id.btnSaveGoals)
        btnSaveGoals.setOnClickListener { saveGoals() }
    }

    private fun saveGoals() {
        // Save user goals to Firebase
        val dailyStepsGoal = findViewById<EditText>(R.id.etDailyStepsGoal).text.toString().toInt()
        val weeklyStepsGoal = findViewById<EditText>(R.id.etWeeklyStepsGoal).text.toString().toInt()
        val monthlyStepsGoal = findViewById<EditText>(R.id.etMonthlyStepsGoal).text.toString().toInt()
        
        val goals = hashMapOf(
            "daily_steps" to dailyStepsGoal,
            "weekly_steps" to weeklyStepsGoal,
            "monthly_steps" to monthlyStepsGoal
        )
        
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .update("goals", goals).addOnSuccessListener {
                finish() // Close activity
            }
    }
}

class ReminderService : IntentService("ReminderService") {

    override fun onHandleIntent(intent: Intent?) {
        // Handle reminder notifications
        val reminderType = intent!!.getStringExtra("reminderType")
        sendReminderNotification(reminderType)
    }

    private fun sendReminderNotification(reminderType: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "health_tracker_channel")
            .setContentTitle("Health Tracker Reminder")
            .setContentText("Time to $reminderType")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        
        notificationManager.notify(1, notification)
    }
}

class SocialSharingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social_sharing)

        findViewById<Button>(R.id.btnShareProgress).setOnClickListener { shareProgress() }
    }

    private fun shareProgress() {
        // Share user progress on social media
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out my progress!")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }
}

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<Button>(R.id.btnSwitchTheme).setOnClickListener { switchTheme() }
    }

    private fun switchTheme() {
        // Switch between light and dark mode
        val sharedPreferences = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        with(sharedPreferences.edit()) {
            putBoolean("dark_mode", !isDarkMode)
            apply()
        }
        recreate() // Recreate activity to apply theme change
    }
}

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        
        // Load FAQs and contact information
        findViewById<TextView>(R.id.tvContactSupport).setOnClickListener { contactSupport() }
    }

    private fun contactSupport() {
        // Provide contact options for user support
        val contactIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@healthtracker.com")
            putExtra(Intent.EXTRA_SUBJECT, "Health Tracker Support")
        }
        startActivity(contactIntent)
    }
}