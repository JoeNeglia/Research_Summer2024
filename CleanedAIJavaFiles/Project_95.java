// Import dependencies
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

// Main Application class
public class FitnessBuddyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseAuth.getInstance();
        FirebaseFirestore.getInstance();
    }
}

// Room Database setup for offline capabilities
@Database(entities = {User.class, Workout.class, Nutrition.class}, version = 1)
abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract WorkoutDao workoutDao();
    public abstract NutritionDao nutritionDao();
}

// Entity classes representing different data structures

// User profile entity
@Entity
class User {
    @PrimaryKey
    @NonNull
    public String uid;
    public String name;
    public String email;
    public int age;
    public String gender;
    public float height;
    public float weight;
    public String fitnessGoal;
    public String dietaryPreferences;
}

// Workout entity
@Entity
class Workout {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String workoutName;
    public String videoUrl;
    public int duration; // in minutes
    public int caloriesBurned;
    public String userUid;
}

// Nutrition entity
@Entity
class Nutrition {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String foodName;
    public int calories;
    public float protein;
    public float carbs;
    public float fats;
    public String userUid;
}

// DAO interfaces
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Query("SELECT * FROM User WHERE uid = :uid LIMIT 1")
    User findByUid(String uid);
}

@Dao
interface WorkoutDao {
    @Insert
    void insert(Workout workout);

    @Query("SELECT * FROM Workout WHERE userUid = :userUid")
    List<Workout> findByUserUid(String userUid);
}

@Dao
interface NutritionDao {
    @Insert
    void insert(Nutrition nutrition);

    @Query("SELECT * FROM Nutrition WHERE userUid = :userUid")
    List<Nutrition> findByUserUid(String userUid);
}

// MainActivity for user authentication
public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        
        findViewById(R.id.emailSignInButton).setOnClickListener(view -> {
            String email = "user@example.com"; // Replace with actual email input
            String password = "password";     // Replace with actual password input
            signIn(email, password);
        });

        // Add Google and Facebook login functionality
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success
                FirebaseUser user = mAuth.getCurrentUser();
                startActivity(new Intent(MainActivity.this, ProfileSetupActivity.class));
                finish();
            } else {
                // If sign in fails, display a message to the user
                Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}

// Activity for user profile setup
public class ProfileSetupActivity extends AppCompatActivity {
    private UserProfileViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

        findViewById(R.id.saveProfileButton).setOnClickListener(view -> {
            User user = new User();
            user.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            user.name = "John Doe"; // Retrieve from input
            user.email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            user.age = 25; // Retrieve from input
            user.gender = "Male"; // Retrieve from input
            user.height = 180; // Retrieve from input
            user.weight = 75; // Retrieve from input
            user.fitnessGoal = "Weight Loss"; // Retrieve from input
            user.dietaryPreferences = "Vegan"; // Retrieve from input

            viewModel.saveUserProfile(user);
            startActivity(new Intent(ProfileSetupActivity.this, WorkoutActivity.class));
            finish();
        });
    }

    public static class UserProfileViewModel extends ViewModel {
        private final MutableLiveData<User> userProfile = new MutableLiveData<>();
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();

        public void saveUserProfile(User user) {
            db.collection("users").document(user.uid).set(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Save to local database
                    AppDatabase database = Room.databaseBuilder(AppInstance.getAppContext(), AppDatabase.class, "fitness-buddy").build();
                    database.userDao().insert(user);
                    userProfile.setValue(user);
                } else {
                    // Handle failure
                }
            });
        }

        public LiveData<User> getUserProfile() {
            return userProfile;
        }
    }
}

// Activity for personalized workout plans
public class WorkoutActivity extends AppCompatActivity {
    private WorkoutViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        viewModel = new ViewModelProvider(this).get(WorkoutViewModel.class);

        viewModel.getWorkouts().observe(this, workouts -> {
            // Update UI with workout list
        });

        findViewById(R.id.startWorkoutButton).setOnClickListener(view -> {
            // Start workout session
        });
    }

    public static class WorkoutViewModel extends ViewModel {
        private final MutableLiveData<List<Workout>> workouts = new MutableLiveData<>();
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();

        public WorkoutViewModel() {
            loadWorkouts();
        }

        private void loadWorkouts() {
            db.collection("workouts").whereEqualTo("userUid", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Workout> workoutList = task.getResult().toObjects(Workout.class);
                    workouts.setValue(workoutList);
                } else {
                    // Handle failure
                }
            });
        }

        public LiveData<List<Workout>> getWorkouts() {
            return workouts;
        }
    }
}

// Activity for nutrition and diet tracking
public class NutritionActivity extends AppCompatActivity {
    private NutritionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        viewModel = new ViewModelProvider(this).get(NutritionViewModel.class);

        viewModel.getNutritions().observe(this, nutritions -> {
            // Update UI with nutrition list
        });

        findViewById(R.id.logFoodButton).setOnClickListener(view -> {
            // Log food intake
        });
    }

    public static class NutritionViewModel extends ViewModel {
        private final MutableLiveData<List<Nutrition>> nutritions = new MutableLiveData<>();
        private final FirebaseFirestore db = FirebaseFirestore.getInstance();

        public NutritionViewModel() {
            loadNutritions();
        }

        private void loadNutritions() {
            db.collection("nutrition").whereEqualTo("userUid", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Nutrition> nutritionList = task.getResult().toObjects(Nutrition.class);
                    nutritions.setValue(nutritionList);
                } else {
                    // Handle failure
                }
            });
        }

        public LiveData<List<Nutrition>> getNutritions() {
            return nutritions;
        }
    }
}