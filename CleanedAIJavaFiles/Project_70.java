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