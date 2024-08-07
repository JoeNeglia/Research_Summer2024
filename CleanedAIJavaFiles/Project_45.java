package com.example.smartreminderapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.smartreminderapp.data.ReminderDatabase;
import com.example.smartreminderapp.data.ReminderRepository;
import com.example.smartreminderapp.data.model.Reminder;
import com.example.smartreminderapp.ui.ReminderViewModel;
import com.example.smartreminderapp.ui.ReminderViewModelFactory;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.GoogleApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int RC_SIGN_IN = 123;
    private EditText reminderTitle, reminderDescription;
    private Button saveReminderButton, dateButton, timeButton, voiceInputButton;
    private ReminderViewModel reminderViewModel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextToSpeech tts;
    private Location currentLocation;
    private Calendar reminderTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        reminderTime = Calendar.getInstance();

        reminderTitle = findViewById(R.id.reminderTitle);
        reminderDescription = findViewById(R.id.reminderDescription);
        saveReminderButton = findViewById(R.id.saveReminderButton);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        voiceInputButton = findViewById(R.id.voiceInputButton);

        reminderViewModel = new ViewModelProvider(this, new ReminderViewModelFactory(getApplication()))
                .get(ReminderViewModel.class);

        tts = new TextToSpeech(this, this);

        saveReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReminder();
            }
        });

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        voiceInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        checkAuthentication();
    }

    // Check if user is authenticated
    private void checkAuthentication() {
        if (auth.getCurrentUser() == null) {
            createSignInIntent();
        } else {
            loadReminders();
        }
    }

    // FirebaseUI Authentication
    public void createSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(this, "Welcome " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                loadReminders();
            } else {
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            }
        }

        // For voice input
        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            reminderTitle.setText(result.get(0));
        }
    }

    // Load reminders from Firestore
    private void loadReminders() {
        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("reminders")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Reminder reminder = document.toObject(Reminder.class);
                            reminderViewModel.insert(reminder);
                        }
                    }
                });
    }

    // Save reminder to ViewModel and Firestore
    private void saveReminder() {
        String title = reminderTitle.getText().toString();
        String description = reminderDescription.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Reminder reminder = new Reminder(title, description, reminderTime.getTimeInMillis());
        reminderViewModel.insert(reminder);

        // Save to Firestore
        db.collection("users").document(auth.getCurrentUser().getUid())
                .collection("reminders")
                .add(reminder)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Reminder Saved", Toast.LENGTH_SHORT).show();
                    scheduleNotification(reminder);
                    finish();
                });
    }

    // Schedule a notification
    private void scheduleNotification(Reminder reminder) {
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        intent.putExtra("reminderTitle", reminder.getTitle());
        intent.putExtra("reminderDescription", reminder.getDescription());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(reminder.getTitle())
                .setContentText(reminder.getDescription())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    // Show DatePicker Dialog
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        reminderTime.set(Calendar.YEAR, year);
                        reminderTime.set(Calendar.MONTH, month);
                        reminderTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    }
                },
                reminderTime.get(Calendar.YEAR),
                reminderTime.get(Calendar.MONTH),
                reminderTime.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    // Show TimePicker Dialog
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        reminderTime.set(Calendar.MINUTE, minute);
                    }
                },
                reminderTime.get(Calendar.HOUR_OF_DAY),
                reminderTime.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    // Prompt speech input
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi, say something!");

        try {
            startActivityForResult(intent, 10);
        } catch (Exception e) {
            Toast.makeText(this, "Your device does not support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onInit(int status) {
        // Initialize Text-to-Speech
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported");
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

    @Override
    protected void onDestroy() {
        // Shutdown TTS
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}