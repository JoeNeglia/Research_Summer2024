// EcoLogger - Environmental Impact Tracker
// This project tracks user activities to estimate carbon footprint and provides eco-friendly tips.
//
// Developed using Android Studio, Kotlin, and Firebase for backend services.

package com.example.ecologger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ecologger.ui.theme.EcoLoggerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcoLoggerTheme {
                // Application entry point
                MainScreen()
            }
        }
    }
}

// App State to manage user data
data class AppState(
    var email: String = "",
    var password: String = "",
    var isAuthenticated: Boolean = false,
    var carbonFootprint: Double = 0.0,
    var activities: List<ActivityLog> = emptyList(),
    var tips: List<String> = emptyList(),
)

// Data class representing Activity Log
data class ActivityLog(
    val type: String,
    val details: String,
    val carbonImpact: Double
)

// Firebase initialization
object FirebaseService {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
}

// Main screen function
@Composable
fun MainScreen() {
    var appState by remember { mutableStateOf(AppState()) }
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (appState.isAuthenticated) {
                    ActivityLoggingScreen(appState, scaffoldState)
                } else {
                    AuthenticationScreen(appState, coroutineScope, scaffoldState)
                }
            }
        }
    )
}

// Authentication screen function
@Composable
fun AuthenticationScreen(
    appState: AppState,
    coroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch {
                try {
                    FirebaseService.auth.signInWithEmailAndPassword(email, password).await()
                    appState.isAuthenticated = true
                } catch (e: Exception) {
                    scaffoldState.snackbarHostState.showSnackbar("Authentication failed: ${e.message}")
                }
            }
        }) {
            Text("Login")
        }
    }
}

// Activity Logging screen
@Composable
fun ActivityLoggingScreen(
    appState: AppState,
    scaffoldState: ScaffoldState
) {
    var activityType by remember { mutableStateOf("") }
    var activityDetails by remember { mutableStateOf("") }
    var carbonImpact by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            value = activityType,
            onValueChange = { activityType = it },
            label = { Text("Activity Type") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = activityDetails,
            onValueChange = { activityDetails = it },
            label = { Text("Activity Details") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = carbonImpact,
            onValueChange = { carbonImpact = it },
            label = { Text("Carbon Impact (kg CO2)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val impact = carbonImpact.toDoubleOrNull() ?: 0.0
            val newLog = ActivityLog(activityType, activityDetails, impact)
            appState.activities = appState.activities + newLog
            appState.carbonFootprint += impact
        }) {
            Text("Log Activity")
        }
    }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Logged Activities")
        appState.activities.forEach {
            Text("- ${it.type} (${it.details}): ${it.carbonImpact} kg CO2")
        }
    }
}

// Eco Tips Screen
@Composable
fun EcoTipsScreen(appState: AppState) {
    val tips = listOf(
        "Turn off lights when not in use.",
        "Use public transportation.",
        "Reduce, Reuse, Recycle."
    )
    appState.tips = tips

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Eco Tips")
        tips.forEach {
            Text("- $it")
        }
    }
}

// Preview functions
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EcoLoggerTheme {
        MainScreen()
    }
}