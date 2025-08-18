// SmartHabitTracker.kt

package com.example.smarthabittracker

import android.app.*
import android.content.*
import android.os.*
import androidx.appcompat.app.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.room.*
import com.google.firebase.auth.*
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.gson.Gson
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

// Models
@Entity
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,
    val intervalType: IntervalType, // DAILY, WEEKLY, CUSTOM
    val notes: String?,
    val tags: List<String>?,
    val createdBy: String
)

enum class IntervalType {
    DAILY, WEEKLY, CUSTOM
}

@Entity
data class HabitProgress(
    @PrimaryKey val date: String,
    val habitId: Int,
    val completed: Boolean
)

// Room Database and DAO
@Database(entities = [Habit::class, HabitProgress::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Insert
    suspend fun insertProgress(progress: HabitProgress)

    @Query("SELECT * FROM Habit WHERE createdBy = :userId")
    fun getHabitsByUser(userId: String): LiveData<List<Habit>>

    @Query("SELECT * FROM HabitProgress WHERE habitId = :habitId")
    fun getProgressByHabit(habitId: Int): LiveData<List<HabitProgress>>
}

// Firebase Authentication and Firestore integration
class AuthRepository(private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore) {
    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(true, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    fun addUserToFirestore(user: FirebaseUser) {
        val userProfile = hashMapOf(
            "uid" to user.uid,
            "email" to user.email
        )
        firestore.collection("users").document(user.uid).set(userProfile)
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}

// Activity for Managing Habits
class HabitManagementActivity : AppCompatActivity(), LifecycleOwner {
    // ViewModel for handling data and business logic
    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_management)

        // UI setup and interactions...

        // Example: Save a new habit
        val newHabit = Habit(
            id = 0,
            title = "Exercise",
            intervalType = IntervalType.DAILY,
            notes = "Do at least 30 mins",
            tags = listOf("health", "fitness"),
            createdBy = viewModel.currentUser?.uid ?: ""
        )

        // Insert habit into DB
        viewModel.insertHabit(newHabit)
    }
}

// ViewModel class for the Habit Management Activity
class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val habitDao: HabitDao = AppDatabase.getAppDatabase(application).habitDao()
    private val userRepo: AuthRepository = AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    val currentUser: FirebaseUser? = userRepo.getCurrentUser()

    fun getHabits(): LiveData<List<Habit>> {
        return habitDao.getHabitsByUser(currentUser?.uid ?: "")
    }

    fun insertHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.insertHabit(habit)
        }
    }
}

// Notification Service
class HabitNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle the received message
    }
}

// Retrofit Service for ML-based habit recommendations
interface RecommendationService {
    @GET("recommend_habits")
    suspend fun getRecommendedHabits(@Query("userId") userId: String): List<Habit>
}

class RecommendationRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(RecommendationService::class.java)

    suspend fun getRecommendations(userId: String): List<Habit> {
        return service.getRecommendedHabits(userId)
    }
}

// Firebase Messaging Service to handle notifications
class HabitNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Create the notification
        val notification = NotificationCompat.Builder(this, "habit_channel")
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setSmallIcon(R.drawable.ic_habit)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }
}

// MainActivity: Entry point for the app, handles authentication
class MainActivity : AppCompatActivity() {
    private lateinit var authRepository: AuthRepository
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        authRepository = AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "habit-db").build()

        // Example: Login user
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()

            authRepository.login(email, password) { success, message ->
                if (success) {
                    // Navigate to HabitManagementActivity
                    startActivity(Intent(this, HabitManagementActivity::class.java))
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Example: Register a new user
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmail).text.toString()
            val password = findViewById<EditText>(R.id.editPassword).text.toString()

            authRepository.register(email, password) { success, message ->
                if (success) {
                    // Add user to Firestore
                    authRepository.getCurrentUser()?.let { user ->
                        authRepository.addUserToFirestore(user)
                        // Navigate to HabitManagementActivity
                        startActivity(Intent(this, HabitManagementActivity::class.java))
                    }
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}