Below is a comprehensive implementation of the Smart Habit Tracker Android application written entirely in Kotlin. For simplicity and clarity, this example will focus on key features and structure while highlighting integration points for machine learning, social sharing, and other advanced functionalities.

```kotlin
// MainActivity.kt
package com.example.smarthabittracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.smarthabittracker.viewmodel.HabitViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var viewModel: HabitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        viewModel = ViewModelProvider(this).get(HabitViewModel::class.java)

        // Handle navigation actions and setup
        // ...
    }
}

```

```kotlin
// Habit.kt - Data Class
package com.example.smarthabittracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val frequency: String,
    val reminderTime: String,
    val tags: String,
    val isCompleted: Boolean = false,
    val streak: Int = 0
)
```

```kotlin
// HabitDao.kt - DAO Interface
package com.example.smarthabittracker.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.smarthabittracker.model.Habit

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)

    @Update
    suspend fun update(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Query("SELECT * FROM habits")
    fun getAllHabits(): LiveData<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabit(id: Int): LiveData<Habit>
}
```

```kotlin
// HabitDatabase.kt - Database Class
package com.example.smarthabittracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smarthabittracker.model.Habit

@Database(entities = [Habit::class], version = 1, exportSchema = false)
abstract class HabitDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        fun getDatabase(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

```kotlin
// HabitRepository.kt - Repository Class
package com.example.smarthabittracker.repository

import androidx.lifecycle.LiveData
import com.example.smarthabittracker.database.HabitDao
import com.example.smarthabittracker.model.Habit

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: LiveData<List<Habit>> = habitDao.getAllHabits()

    suspend fun insert(habit: Habit) {
        habitDao.insert(habit)
    }

    suspend fun update(habit: Habit) {
        habitDao.update(habit)
    }

    suspend fun delete(habit: Habit) {
        habitDao.delete(habit)
    }
}
```

```kotlin
// HabitViewModel.kt - ViewModel Class
package com.example.smarthabittracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.smarthabittracker.database.HabitDatabase
import com.example.smarthabittracker.model.Habit
import com.example.smarthabittracker.repository.HabitRepository
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    val allHabits: LiveData<List<Habit>>

    init {
        val habitDao = HabitDatabase.getDatabase(application).habitDao()
        repository = HabitRepository(habitDao)
        allHabits = repository.allHabits
    }

    fun insert(habit: Habit) = viewModelScope.launch {
        repository.insert(habit)
    }

    fun update(habit: Habit) = viewModelScope.launch {
        repository.update(habit)
    }

    fun delete(habit: Habit) = viewModelScope.launch {
        repository.delete(habit)
    }
}
```

```kotlin
// activity_main.xml - Main Activity Layout
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <fragment
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_graph"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

```kotlin
// nav_graph.xml - Navigation Graph
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    app:startDestination="@id/dashboardFragment">

    <fragment
        android:id="@+id/dashboardFragment"
        android:name="com.example.smarthabittracker.ui.DashboardFragment"
        android:label="Dashboard"
        tools:layout="@layout/fragment_dashboard">
        <action
            android:id="@+id/action_dashboardFragment_to_addHabitFragment"
            app:destination="@id/addHabitFragment" />
    </fragment>

    <fragment
        android:id="@+id/addHabitFragment"
        android:name="com.example.smarthabittracker.ui.AddHabitFragment"
        android:label="Add Habit"
        tools:layout="@layout/fragment_add_habit" />
</navigation>
```

```kotlin
// DashboardFragment.kt - Dashboard Fragment
package com.example.smarthabittracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.smarthabittracker.R
import com.example.smarthabittracker.databinding.FragmentDashboardBinding
import com.example.smarthabittracker.viewmodel.HabitViewModel

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val habitViewModel: HabitViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Observer for habits
        habitViewModel.allHabits.observe(viewLifecycleOwner, Observer { habits ->
            // Update UI with habits data
            // ...
        })
        
        binding.fabAddHabit.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addHabitFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

```kotlin
// fragment_dashboard.xml - Dashboard Fragment Layout
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>
        <!-- Data binding variables -->
    </data>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".ui.DashboardFragment">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerView"
            android:layout_width="0dp"
            android:layout_height="0dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"/>
        
        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/fabAddHabit"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_add"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintBottom_margin="16dp"
            app:layout_constraintEnd_margin="16dp"/>

    </androidx.constraintlayout.widget.ConstraintLayout>
</layout>
```

```kotlin
// AddHabitFragment.kt - Add Habit Fragment
package com.example.smarthabittracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.smarthabittracker.R
import com.example.smarthabittracker.databinding.FragmentAddHabitBinding
import com.example.smarthabittracker.model.Habit
import com.example.smarthabittracker.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

class AddHabitFragment : Fragment() {
    private var _binding: FragmentAddHabitBinding? = null
    private val binding get() = _binding!!
    private val habitViewModel: HabitViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveHabit.setOnClickListener {
            val name = binding.etHabitName.text.toString().trim()
            val startDate = binding.etStartDate.text.toString().trim()
            val endDate = binding.etEndDate.text.toString().trim()
            val frequency = binding.etFrequency.text.toString().trim()
            val reminder = binding.etReminderTime.text.toString().trim()
            val tags = binding.etTags.text.toString().trim()

            val newHabit = Habit(
                name = name,
                startDate = startDate,
                endDate = endDate,
                frequency = frequency,
                reminderTime = reminder,
                tags = tags
            )

            lifecycleScope.launch {
                habitViewModel.insert(newHabit)
                findNavController().navigate(R.id.action_addHabitFragment_to_dashboardFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

```kotlin
// fragment_add_habit.xml - Add Habit Fragment Layout
<?xml version="1.0" encoding="utf-8"?>
<layout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools">

    <data>
        <!-- Data binding variables -->
    </data>

    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        tools:context=".ui.AddHabitFragment">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">

            <EditText
                android:id="@+id/etHabitName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Habit Name" />

            <EditText
                android:id="@+id/etStartDate"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Start Date" />

            <EditText
                android:id="@+id/etEndDate"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="End Date (optional)" />

            <EditText
                android:id="@+id/etFrequency"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Frequency" />

            <EditText
                android:id="@+id/etReminderTime"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Reminder Time" />

            <EditText
                android:id="@+id/etTags"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="Tags (optional)" />

            <Button
                android:id="@+id/btnSaveHabit"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="Save Habit" />

        </LinearLayout>
    </ScrollView>
</layout>
```

```kotlin
// ApplicationClass.kt - Custom Application Class for Initializing Firebase and other services
package com.example.smarthabittracker

import android.app.Application
import com.google.firebase.FirebaseApp

class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        // Initialize other services like ML Kit or Crashlytics if needed
    }
}
```

```kotlin
// AndroidManifest.xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.smarthabittracker">

    <application
        android:name=".ApplicationClass"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        
        <!-- Add Internet Permission -->
        <uses-permission android:name="android.permission.INTERNET"/>
        
        <!-- Firebase and other services initialization -->
        <meta-data
            android:name="com.google.firebase.messaging.default_notification_channel_id"
            android:value="@string/default_notification_channel_id" />

        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### Summary:
This code provides the structure for a Smart Habit Tracker application. It includes essential components such as:

1. **User Authentication**: Placeholder for Firebase initialization.
2. **Dashboard**: Displays habits and navigates to the Add Habit screen.
3. **Habit Creation and Management**: Allows users to create new habits via `AddHabitFragment`.
4. **Progress Tracking**: Partially implemented; more detailed visual representation can be added.
5. **Machine Learning Integration**: Placeholder, real ML integration should use TensorFlow Lite or Firebase ML Kit.
6. **Data Storage**: Utilizes Room database for local storage.

Remember to add your Firebase configuration files (`google-services.json`), set up Firebase Auth, Database, and Cloud Storage in the Firebase Console. Expand on each feature, such as using Retrofit for network requests, Glide for image loading, and MPAndroidChart for charts and graphs, as per the full project requirements.