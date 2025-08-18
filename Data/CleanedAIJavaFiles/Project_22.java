// File: MainActivity.java

package com.example.androidquizapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Login failed! Please check your credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Registering...");
        progressDialog.show();
        
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Registration failed! Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

// File: DashboardActivity.java

package com.example.androidquizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private Button scienceCategoryButton, historyCategoryButton, popCultureCategoryButton, technologyCategoryButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        scienceCategoryButton = findViewById(R.id.science_category_button);
        historyCategoryButton = findViewById(R.id.history_category_button);
        popCultureCategoryButton = findViewById(R.id.pop_culture_category_button);
        technologyCategoryButton = findViewById(R.id.technology_category_button);
        logoutButton = findViewById(R.id.logout_button);

        scienceCategoryButton.setOnClickListener(v -> startQuizActivity("Science"));
        historyCategoryButton.setOnClickListener(v -> startQuizActivity("History"));
        popCultureCategoryButton.setOnClickListener(v -> startQuizActivity("Pop Culture"));
        technologyCategoryButton.setOnClickListener(v -> startQuizActivity("Technology"));
        logoutButton.setOnClickListener(v -> logoutUser());
    }

    private void startQuizActivity(String category) {
        Intent intent = new Intent(DashboardActivity.this, QuizActivity.class);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(DashboardActivity.this, MainActivity.class));
        finish();
    }
}

// File: QuizActivity.java

package com.example.androidquizapp;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private TextView questionTextView, timerTextView;
    private RadioGroup answersRadioGroup;
    private Button nextButton;
    private String category, currentQuestion;
    private String[] currentAnswers;
    private int score = 0, questionIndex = 0;
    private CountDownTimer countDownTimer;
    private static final long TIMER_MILLISECONDS = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTextView = findViewById(R.id.question_textview);
        timerTextView = findViewById(R.id.timer_textview);
        answersRadioGroup = findViewById(R.id.answers_radiogroup);
        nextButton = findViewById(R.id.next_button);

        category = getIntent().getStringExtra("CATEGORY");

        loadNextQuestion();

        nextButton.setOnClickListener(v -> handleNextButton());

        startTimer();
    }

    private void loadNextQuestion() {
        // For the purpose of this example, we use dummy data. Real data should be loaded dynamically from an API.
        if (category.equals("Science")) {
            currentQuestion = "What is the chemical symbol for water?";
            currentAnswers = new String[]{"HO", "H2O", "H2O2", "O2"};
        } 
        // There should be more conditions for other categories
        updateQuestionUI();
    }

    private void updateQuestionUI() {
        questionTextView.setText(currentQuestion);
        answersRadioGroup.removeAllViews();
        for (String answer : currentAnswers) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(answer);
            answersRadioGroup.addView(radioButton);
        }
    }

    private void handleNextButton() {
        int selectedAnswerId = answersRadioGroup.getCheckedRadioButtonId();
        if (selectedAnswerId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedAnswer = findViewById(selectedAnswerId);
        if (selectedAnswer.getText().equals("H2O")) {
            score++;
        }

        questionIndex++;
        if (questionIndex >= 5) {
            // Completed 5 questions (Example), show score
            showScore();
        } else {
            // Load next question
            loadNextQuestion();
        }
    }

    private void showScore() {
        // Show a dialog with the score. Simplified for this example
        Toast.makeText(this, "Your score: " + score, Toast.LENGTH_LONG).show();
        finish(); // Close quiz activity
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(TIMER_MILLISECONDS * 1, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d",
                        millisUntilFinished / 1000 / 60,
                        millisUntilFinished / 1000 % 60);
                timerTextView.setText(timeLeftFormatted);
            }

            @Override
            public void onFinish() {
                Toast.makeText(QuizActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                showScore();
            }
        }.start();
    }
}