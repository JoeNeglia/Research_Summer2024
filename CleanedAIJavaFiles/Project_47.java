// File: EcoTrackApp.kt
package com.example.ecotrack

import androidx.room.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlinx.coroutines.*

@Entity
data class UserActivity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val type: String,
    val value: Double,
    val date: Long
)

@Dao
interface UserActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: UserActivity)
    
    @Query("SELECT * FROM UserActivity WHERE date BETWEEN :start AND :end")
    fun getActivitiesBetween(start: Long, end: Long): LiveData<List<UserActivity>>
}

@Database(entities = [UserActivity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userActivityDao(): UserActivityDao
}

class MainViewModel(private val dao: UserActivityDao) : ViewModel() {
    val activities: LiveData<List<UserActivity>> = dao.getAll()
    
    fun addActivity(activity: UserActivity) {
        viewModelScope.launch {
            dao.insert(activity)
        }
    }
}

class MainViewModelFactory(private val dao: UserActivityDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

interface SustainabilityTipsApi {
    @GET("tips")
    suspend fun getTips(): List<String>
}

class DashboardFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        // Set up your UI components here
        return rootView
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val dao = AppDatabase.getDatabase(requireContext()).userActivityDao()
        viewModel = ViewModelProvider(this, MainViewModelFactory(dao)).get(MainViewModel::class.java)
        
        viewModel.activities.observe(viewLifecycleOwner, {
            // Update UI accordingly
        })
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> { loadFragment(DashboardFragment()) }
                R.id.nav_footprint -> { /* Load Footprint Fragment */ }
                R.id.nav_waste -> { /* Load Waste Fragment */ }
                R.id.nav_resources -> { /* Load Resources Fragment */ }
                R.id.nav_community -> { /* Load Community Fragment */ }
            }
            true
        }
        
        loadFragment(DashboardFragment())
    }
    
    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

object RetrofitClient {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val api: SustainabilityTipsApi = retrofit.create(SustainabilityTipsApi::class.java)
}

suspend fun getTips(): List<String> {
    return RetrofitClient.api.getTips()
}

fun getGoogleSignInClient(context: Context) =
    GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN)

class EcoTrackApp : Application() {
    companion object {
        lateinit var instance: EcoTrackApp
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

@HiltAndroidApp
class MyApplication : Application()