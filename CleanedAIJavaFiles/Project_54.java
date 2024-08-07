package com.example.ecofriend

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.ecofriend.adapters.ActivityLogAdapter
import com.example.ecofriend.data.ActivityLog
import com.example.ecofriend.data.User
import com.example.ecofriend.databinding.ActivityMainBinding
import com.example.ecofriend.utils.GamificationUtils
import com.example.ecofriend.utils.GoogleFitUtils
import com.example.ecofriend.viewmodel.MainViewModel
import com.example.ecofriend.viewmodel.MainViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        viewModel = ViewModelProvider(this, MainViewModelFactory(auth.currentUser!!)).get(MainViewModel::class.java)
        
        initViews()
        observeViewModel()

        // Fetch initial data
        viewModel.fetchActivities()
        viewModel.fetchUserDetails()
    }

    private fun initViews() {
        binding.apply {
            logoutButton.setOnClickListener { 
                auth.signOut()
                navigateToLogin()
            }

            addActivityButton.setOnClickListener {
                addActivity()
            }
        }
    }

    private fun addActivity() {
        // Logic to log activity; for simplicity, we'll log a dummy activity here
        val activity = ActivityLog(
            description = "Used a reusable water bottle",
            points = 10,
            timestamp = System.currentTimeMillis()
        )

        viewModel.logActivity(activity)
    }
    
    private fun navigateToLogin() {
        // Start the login activity
    }

    private fun observeViewModel() {
        viewModel.user.observe(this, { user ->
            if (user != null) {
                updateUserDetails(user)
            }
        })

        viewModel.activities.observe(this, { activities ->
            if (activities != null) {
                updateActivityLog(activities)
            }
        })

        viewModel.errorMessage.observe(this, { message ->
            if (message != null) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUserDetails(user: User) {
        binding.apply {
            userName.text = user.name
            userPoints.text = "Points: ${user.points}"

            Glide.with(this@MainActivity)
                .load(user.photoUrl)
                .into(profileImage)
        }
    }

    private fun updateActivityLog(activities: List<ActivityLog>) {
        val adapter = ActivityLogAdapter(activities)
        binding.recyclerView.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        // Save any necessary state
    }
}

// ViewModel and ViewModelFactory
package com.example.ecofriend.viewmodel

import androidx.lifecycle.*
import com.example.ecofriend.data.ActivityLog
import com.example.ecofriend.data.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainViewModel(private val user: FirebaseUser) : ViewModel() {
    private val db = Firebase.firestore

    private val _userDetails = MutableLiveData<User>()
    val userDetails: LiveData<User> get() = _userDetails

    private val _activities = MutableLiveData<List<ActivityLog>>()
    val activities: LiveData<List<ActivityLog>> get() = _activities

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun fetchUserDetails() {
        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    _userDetails.value = document.toObject(User::class.java)
                }
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.localizedMessage
            }
    }

    fun logActivity(activity: ActivityLog) {
        viewModelScope.launch {
            // Assume we have a method to save activity log to Firestore
            db.collection("users")
                .document(user.uid)
                .collection("activities")
                .add(activity)
                .addOnSuccessListener {
                    fetchActivities()
                }
                .addOnFailureListener { exception ->
                    _errorMessage.value = exception.localizedMessage
                }

            // Update user points
            db.collection("users")
                .document(user.uid)
                .update("points", User.FieldValue.increment(activity.points.toLong()))
                .addOnSuccessListener {
                    fetchUserDetails()
                }
                .addOnFailureListener { exception ->
                    _errorMessage.value = exception.localizedMessage
                }
        }
    }

    fun fetchActivities() {
        db.collection("users")
            .document(user.uid)
            .collection("activities")
            .get()
            .addOnSuccessListener { result ->
                val activities = result.toObjects(ActivityLog::class.java)
                _activities.value = activities
            }
            .addOnFailureListener { exception ->
                _errorMessage.value = exception.localizedMessage
            }
    }
}

class MainViewModelFactory(private val user: FirebaseUser) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(user) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Data classes
package com.example.ecofriend.data

data class User(
    var name: String = "",
    var points: Int = 0,
    var photoUrl: String = ""
)

data class ActivityLog(
    var description: String = "",
    var points: Int = 0,
    var timestamp: Long = 0L
)

// Adapter
package com.example.ecofriend.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ecofriend.data.ActivityLog
import com.example.ecofriend.databinding.ItemActivityLogBinding

class ActivityLogAdapter(private val activities: List<ActivityLog>) : 
        RecyclerView.Adapter<ActivityLogAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemActivityLogBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(activity: ActivityLog) {
            binding.apply {
                description.text = activity.description
                points.text = "Points: ${activity.points}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemActivityLogBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = activities.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(activities[position])
    }
}

// Utility for Google Fit
package com.example.ecofriend.utils

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.result.DataReadResponse

object GoogleFitUtils {

    fun requestGoogleFitPermissions(activity: Activity) {
        val account = GoogleSignIn.getLastSignedInAccount(activity) ?: return
        Fitness.getConfigClient(activity, account).readData(
            DataReadResponse.builder().read(DataType.TYPE_STEP_COUNT_DELTA).build()
        )
    }

    fun readGoogleFitHistory(activity: Activity) {
        val account = GoogleSignIn.getLastSignedInAccount(activity) ?: return
        Fitness.getHistoryClient(activity, account)
            .readData(DataReadResponse.builder().read(DataType.TYPE_STEP_COUNT_DELTA).build())
            .addOnSuccessListener { response ->
                // Process data
            }
    }
}

// Utility for Gamification system
package com.example.ecofriend.utils

object GamificationUtils {

    fun calculatePoints(activity: String): Int {
        return when (activity) {
            "recycling" -> 5
            "biking" -> 10
            else -> 1
        }
    }
}

// Moshi Retrofit setup for API calls
object RetrofitInstance {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    fun <T> create(service: Class<T>): T {
        return retrofit.create(service)
    }
}