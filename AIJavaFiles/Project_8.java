Below is a comprehensive all-in-one Android app implementation code snippet for the "Interactive Fitness Buddy" project. Due to the limitations of a single response, I will provide an illustrative schema and partial implementations that highlight the main features. You must expand on this to include all features fully.

```kotlin
// Import necessary libraries
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.viewModelScope

// ============ User Authentication (FirebaseAuth) ============
// Build.gradle (app module)
// implementation 'com.google.firebase:firebase-auth:21.0.1'
// implementation 'com.google.android.gms:play-services-auth:19.2.0'

// Init Firebase in MainApplication.kt
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

// MainActivity.kt
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        val auth = FirebaseAuth.getInstance()

        // Example: Login using Email and Password
        val email = "user@example.com"
        val password = "password123"
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    // Update UI
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication Failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }

        // Example: Register User
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success
                    val user = auth.currentUser
                    // Update UI
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Registration Failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}

// ============ User Data (Room DB) ============
// User.kt: Data class for User
@Entity
data class User(
    @PrimaryKey val uid: String,
    val email: String,
    val name: String?
)

// AppDatabase.kt: Database setup
@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

// UserDao.kt: Data Access Object
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)

    @Query("SELECT * FROM User WHERE uid = :uid")
    fun getUser(uid: String): LiveData<User>
}

// MainRepository.kt: Repository to handle data operations
class MainRepository(private val userDao: UserDao) {
    fun insertUser(user: User) {
        runBlocking {
            launch(Dispatchers.IO) {
                userDao.insert(user)
            }
        }
    }

    fun getUser(uid: String): LiveData<User> {
        return userDao.getUser(uid)
    }
}

// MainViewModel.kt: ViewModel for business logic
class MainViewModel(private val repository: MainRepository) : ViewModel() {
    fun insertUser(user: User) {
        viewModelScope.launch {
            repository.insertUser(user)
        }
    }

    fun getUser(uid: String): LiveData<User> {
        return repository.getUser(uid)
    }
}

class MainViewModelFactory(private val repository: MainRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ============ Activity Tracking (Google Fit API) ============
// Build.gradle (app module)
// implementation 'com.google.android.gms:play-services-fitness:20.0.0'

// GoogleFitManager.kt
class GoogleFitManager(context: Context) {
    private val fitnessOptions: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    init {
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        Fitness.getHistoryClient(context, account).readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener { dataSet ->
                val total = if (dataSet.isEmpty) {
                    0
                } else {
                    dataSet.dataPoints[0].getValue(Field.FIELD_STEPS).asInt()
                }
                // Use this total step count
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }
}

// ============ Workout Plan Generator ============
// SampleWorkouts.kt
object SampleWorkouts {
    val workouts = listOf(
        "Push-ups",
        "Squats",
        "Burpees",
        "Plank",
        "Lunges"
    )

    fun generatePlan(fitnessLevel: String): List<String> {
        return when (fitnessLevel) {
            "Beginner" -> listOf("Push-ups", "Squats")
            "Intermediate" -> listOf("Push-ups", "Squats", "Burpees")
            "Advanced" -> listOf("Push-ups", "Squats", "Burpees", "Plank")
            else -> listOf()
        }
    }
}

// MainViewModel.kt: Extended for Workout Plan
class MainViewModel(private val repository: MainRepository) : ViewModel() {
    private val _workoutPlan = MutableLiveData<List<String>>()
    val workoutPlan: LiveData<List<String>> get() = _workoutPlan

    fun generateWorkoutPlan(fitnessLevel: String) {
        _workoutPlan.value = SampleWorkouts.generatePlan(fitnessLevel)
    }
}

// ============ UI Components ============
// activity_main.xml
// Add UI elements (e.g., RecyclerView for workout display, TextViews for user info)

// MainActivity.kt: Extended for displaying and generating workout plans
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userDao = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        ).build().userDao()

        val repository = MainRepository(userDao)
        val factory = MainViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        
        // Firebase User
        val user = FirebaseAuth.getInstance().currentUser

        user?.let {
            val userId = it.uid
            // Observe user data from ViewModel
            viewModel.getUser(userId).observe(this, { user ->
                // Update UI with user data
            })
        }

        // Example: Generate workout plan
        viewModel.generateWorkoutPlan("Intermediate")
        viewModel.workoutPlan.observe(this, { plan ->
            // Update UI with workout plan
        })
    }
}
```

This illustrative example includes the setup and partial implementation of the core features, including user authentication (via Firebase), local database setup using Room, basic Google Fit API connectivity, workout plan generation, and ViewModel-Repository pattern for architecture.

Expand and refine this code by adding:
- Detailed UI components and layout files.
- Complete logic for all other features.
- Full integration with Google Fit, notifications, and other APIs.
- Proper error handling, extensive comments, and comprehensive testing.
- Complete utilization of modern Android development practices like Jetpack components and MVVM architecture.

You can also set up a Git repository to manage and share your full project code efficiently. Don't forget to adhere to best security practices to handle user data securely and comply with relevant data protection regulations.