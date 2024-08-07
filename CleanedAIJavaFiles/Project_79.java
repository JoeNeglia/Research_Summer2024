// SmartRecipeAssistant.kt
package com.example.smartrecipeassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer

class SmartRecipeAssistant : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ingredientScannerView: LinearLayout
    private lateinit var mealPlannerView: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var cameraButton: Button
    private lateinit var signInButton: Button
    private lateinit var signOutButton: Button
    private lateinit var voiceSearchButton: Button
    private lateinit var currentUser: FirebaseUser?
    
    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val SIGN_IN_REQUEST_CODE = 101
        private const val PERMISSIONS_REQUEST_CODE = 102
        private const val TAG = "SmartRecipeAssistant"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize UI elements
        ingredientScannerView = findViewById(R.id.ingredientScannerView)
        mealPlannerView = findViewById(R.id.mealPlannerView)
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        cameraButton = findViewById(R.id.cameraButton)
        signInButton = findViewById(R.id.signInButton)
        signOutButton = findViewById(R.id.signOutButton)
        voiceSearchButton = findViewById(R.id.voiceSearchButton)
        
        // Set up button listeners
        searchButton.setOnClickListener { performSearch() }
        cameraButton.setOnClickListener { startCamera() }
        signInButton.setOnClickListener { signIn() }
        signOutButton.setOnClickListener { signOut() }
        voiceSearchButton.setOnClickListener { startVoiceSearch() }
        
        // Check if the app has necessary permissions
        checkAndRequestPermissions()
    }

    private fun performSearch() {
        // Trigger search with the input text
        val query = searchInput.text.toString().trim()
        if (query.isNotEmpty()) {
            searchRecipes(query)
        } else {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchRecipes(query: String) {
        // Placeholder function for searching recipes in the Firestore database
        db.collection("recipes").whereEqualTo("ingredient", query)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun startCamera() {
        // Check for camera permissions and start camera activity for ingredient recognition
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(this, CameraActivity::class.java)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    // Handling permission results
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startCamera()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    // Firebase Auth methods
    private fun signIn() {
        // Placeholder sign-in method using Firebase Authentication
        // Intent to start SignInActivity
        val signInIntent = Intent(this, SignInActivity::class.java)
        startActivityForResult(signInIntent, SIGN_IN_REQUEST_CODE)
    }

    private fun signOut() {
        auth.signOut()
        currentUser = null
        updateUI()
    }

    private fun updateUI() {
        // Update UI based on user authentication status
        currentUser = auth.currentUser
        if (currentUser != null) {
            signInButton.visibility = View.GONE
            signOutButton.visibility = View.VISIBLE
        } else {
            signInButton.visibility = View.VISIBLE
            signOutButton.visibility = View.GONE
        }
    }

    private fun startVoiceSearch() {
        // Placeholder for starting voice search action
        val intent = Intent(Intent.ACTION_VOICE_COMMAND)
        startActivityForResult(intent, VoiceRecognitionHelper.VOICE_SEARCH_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SIGN_IN_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    updateUI()
                }
            }
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK && data != null) {
                    val imagePath = data.getStringExtra("imagePath")
                    processImageForIngredients(imagePath!!)
                }
            }
        }
    }

    private fun processImageForIngredients(imagePath: String) {
        // Using FirebaseVision to process image for OCR
        val image = FirebaseVisionImage.fromFilePath(this, Uri.parse(imagePath))
        val recognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        recognizer.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                // Handle successful OCR recognition
                handleTextRecognitionResult(firebaseVisionText)
            }
            .addOnFailureListener { e ->
                // Handle failure in OCR recognition
                Log.e(TAG, "Error processing image for OCR", e)
            }
    }

    private fun handleTextRecognitionResult(firebaseVisionText: com.google.firebase.ml.vision.text.FirebaseVisionText) {
        // Placeholder for handling OCR text recognition results
        for (block in firebaseVisionText.textBlocks) {
            for (line in block.lines) {
                Log.d(TAG, line.text)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }
}