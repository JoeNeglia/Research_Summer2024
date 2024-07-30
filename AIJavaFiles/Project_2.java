Creating a fully functional and feature-complete Android application within a single file is not practical or possible due to the complexity and modularity of Android development. However, I can provide a simplified version that outlines the main structure and key components you'd need for the EventBuzz app.

### Complete Code Outline for EventBuzz

```kotlin
// MainActivity.kt
package com.example.eventbuzz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        database = FirebaseDatabase.getInstance().reference

        setUpRecyclerView()
        checkForUserAuthentication()
        subscribeToNotifications()
    }

    private fun setUpRecyclerView() {
        // Set up RecyclerView with adapter and layout manager
        // Load events from Firebase
    }

    private fun checkForUserAuthentication() {
        // Check if user is authenticated
        // If not, redirect to login/signup activity
    }

    private fun subscribeToNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("events")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Handle error
                }
            }
    }
}

// SignUpLoginActivity.kt
package com.example.eventbuzz

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class SignUpLoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_login)

        auth = FirebaseAuth.getInstance()

        // Set up Google and Facebook sign-in
        // Handle sign-in/intents
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI
                } else {
                    // If sign in fails, display a message to the user.
                }
            }
    }
}

// EventDetailActivity.kt
package com.example.eventbuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EventDetailActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        database = FirebaseDatabase.getInstance().reference

        // Load event details from Firebase
        // Display event details
    }

    private fun registerForEvent(eventId: String) {
        // Implement ticket purchasing using Stripe or PayPal SDK
    }
}

// EventCreateActivity.kt
package com.example.eventbuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EventCreateActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_create)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Set up form to create event
        // Upload event data to Firebase
    }

    private fun createEvent() {
        // Collect event details from form
        // Save event to Firebase
    }
}

// EventBookingActivity.kt
package com.example.eventbuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EventBookingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_booking)

        // Implement ticket purchasing system using Stripe or PayPal
    }

    private fun completePurchase() {
        // Handle purchase completion
    }
}

// UserProfileActivity.kt
package com.example.eventbuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()

        // Load user profile details from Firebase
    }
}

// QRCodeCheckInActivity.kt
package com.example.eventbuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class QRCodeCheckInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code_checkin)

        // Implement QR code scanning
    }
}

// ChatActivity.kt
package com.example.eventbuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ChatActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        database = FirebaseDatabase.getInstance().reference

        // Set up chat functionality using Firebase Realtime Database
    }
}

// AnalyticsDashboardActivity.kt
package com.example.eventbuzz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AnalyticsDashboardActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics_dashboard)

        database = FirebaseDatabase.getInstance().reference

        // Load and display analytics data using charts and graphs
    }
}

// Layout files:
// activity_main.xml
// activity_sign_up_login.xml
// activity_event_detail.xml
// activity_event_create.xml
// activity_event_booking.xml
// activity_user_profile.xml
// activity_qr_code_checkin.xml
// activity_chat.xml
// activity_analytics_dashboard.xml

// Note: Ensure all necessary permissions and dependencies are added in the AndroidManifest.xml and build.gradle files.
```

### Additional Points to Consider:
1. **Permissions:** Add necessary permissions for internet access, location, and storage in `AndroidManifest.xml`.
2. **Dependencies:** Include necessary dependencies for Firebase, Google Maps, payment gateways, and other required libraries in `build.gradle`.

This outline provides a basic structure for the EventBuzz app. Each part of the application should be elaborated and expanded into multiple files and components in a real-world scenario for better manageability, scalability, and modularity. Remember to follow good coding practices and separate concerns into different packages and modules.