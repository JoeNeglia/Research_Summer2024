Sure, let's break down the entire functioning of the Eco Footprint Tracker project. The code will be comprehensive but within the context of an answer, it won't be possible to cover all possible features (like real-time interaction with Firebase, Notifications, etc.) in a single file's space. Still, it will showcase the major portions of the project to illustrate how to put it all together.

Note: Below is a simplified version of how the code might look like in a single file. This example is implemented in Kotlin for clarity. In practice, you would organize your app differently across multiple files and folders.

```kotlin
// Import necessary libraries
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // Firebase authentication and database references
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference

        // Set up RecyclerView with dummy data for dashboard visualization
        val ecoActivities = listOf(
            EcoActivity("Daily Commute", "Car", "20 km"),
            EcoActivity("Energy Consumption", "Electricity", "15 kWh"),
            EcoActivity("Dietary Impact", "Vegan", "3 meals"),
            EcoActivity("Waste Management", "Recyclable", "2 kg")
        )
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EcoAdapter(ecoActivities)
        
        // Handle user sign-in flow
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign-in succeeded, display dashboard
                updateUI()
            } else {
                // Sign-in failed, display a message to the user
                toast("Authentication Failed.")
            }
        }
    }

    private fun updateUI() {
        // Update the UI with user-specific details
    }
}

// Data class to represent each eco activity
data class EcoActivity(val name: String, val type: String, val value: String)

// Adapter for displaying eco activities in RecyclerView
class EcoAdapter(private val ecoActivities: List<EcoActivity>) : RecyclerView.Adapter<EcoAdapter.ViewHolder>(){

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.eco_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ecoActivities[position]
        holder.view.findViewById<TextView>(R.id.activity_name).text = item.name
        holder.view.findViewById<TextView>(R.id.activity_type).text = item.type
        holder.view.findViewById<TextView>(R.id.activity_value).text = item.value
    }

    override fun getItemCount() = ecoActivities.size
}
```

1. **User Registration and Authentication**: The code above includes Firebase authentication and user sign-in functionality.

2. **Dashboard**: It includes a RecyclerView that displays the user's eco activities. The data is currently hardcoded but can be expanded to fetch from Firebase.

3. **Transportation Tracker, Energy Consumption, Dietary Impact, and Waste Management**: Rather than implementing individual trackers in one file, the `EcoActivity` class allows us to model each type of tracker.

4. **Settings and Personalization**: Implementing user preferences may involve shared preferences or another database. Integrate it within the `updateUI()` function and the settings menu.

The actual implementation will span multiple files and activities/fragments for simplicity and best practices, breaking down components into more manageable units. 

You also need to configure your project with `build.gradle` files, add relevant permissions in the `AndroidManifest.xml`, and create corresponding XML layout files for views. This is a skeleton to guide you towards a more comprehensive implementation.