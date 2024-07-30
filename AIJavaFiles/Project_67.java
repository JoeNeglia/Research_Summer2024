Here is the complete implementation of the Virtual Pet Care App in a single Java file. This implementation covers the core features mentioned in the prompt.

Note: Actual Android apps should have separate files for various components like activities, views, services, etc. This single file approach is simplified for the sake of completeness in this format.

```java
// Import statements
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import com.google.android.gms.tasks.*;
import java.util.*;

// Main Activity
public class MainActivity extends AppCompatActivity {

    // Firebase Authentication UI
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // UI Elements
    private EditText emailInput, passwordInput;
    private Button loginButton, registerButton, adoptButton, feedButton, playButton, groomButton, healthCheckButton;

    // Virtual Pet attributes
    private TextView petName, petBreed, petAge, petPersonality;
    private TextView hungerLevel, happinessLevel, cleanlinessLevel, healthLevel;

    // RecyclerView for pet selection
    private RecyclerView petRecyclerView;
    private PetAdapter petAdapter;
    private List<Pet> petList = new ArrayList<>();

    // Virtual pet selected by the user
    private Pet currentPet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize UI Elements
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        petName = findViewById(R.id.petName);
        petBreed = findViewById(R.id.petBreed);
        petAge = findViewById(R.id.petAge);
        petPersonality = findViewById(R.id.petPersonality);
        hungerLevel = findViewById(R.id.hungerLevel);
        happinessLevel = findViewById(R.id.happinessLevel);
        cleanlinessLevel = findViewById(R.id.cleanlinessLevel);
        healthLevel = findViewById(R.id.healthLevel);
        adoptButton = findViewById(R.id.adoptButton);
        feedButton = findViewById(R.id.feedButton);
        playButton = findViewById(R.id.playButton);
        groomButton = findViewById(R.id.groomButton);
        healthCheckButton = findViewById(R.id.healthCheckButton);
        petRecyclerView = findViewById(R.id.petRecyclerView);

        // Set up RecyclerView
        petRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        petAdapter = new PetAdapter(petList, this::onPetSelected);
        petRecyclerView.setAdapter(petAdapter);

        // Load pet list
        loadPetList();

        // Register user
        registerButton.setOnClickListener(v -> registerUser());

        // Login user
        loginButton.setOnClickListener(v -> loginUser());

        // Adopt pet
        adoptButton.setOnClickListener(v -> adoptPet());

        // Feed the pet
        feedButton.setOnClickListener(v -> feedPet());

        // Play with the pet
        playButton.setOnClickListener(v -> playWithPet());

        // Groom the pet
        groomButton.setOnClickListener(v -> groomPet());

        // Perform health check on pet
        healthCheckButton.setOnClickListener(v -> checkPetHealth());
    }

    // Load list of pets from the database or a pre-defined list
    private void loadPetList() {
        // Load from Firebase or local static list
        petList.add(new Pet("Buddy", "Dog", 2, "Friendly"));
        petList.add(new Pet("Whiskers", "Cat", 3, "Curious"));
        petList.add(new Pet("Flopsy", "Rabbit", 1, "Energetic"));
        petAdapter.notifyDataSetChanged();
    }

    // Register a new user
    private void registerUser() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Login an existing user
    private void loginUser() {
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                // Load user data and pets
            } else {
                Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adopt a pet
    private void adoptPet() {
        if (currentPet != null) {
            mDatabase.child("users").child(mAuth.getUid()).child("pets").setValue(currentPet).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Pet Adopted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Pet Adoption Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Feed the pet
    private void feedPet() {
        if (currentPet != null) {
            currentPet.feed();
            updatePetUI();
            Toast.makeText(MainActivity.this, "Pet Fed", Toast.LENGTH_SHORT).show();
        }
    }

    // Play with the pet
    private void playWithPet() {
        if (currentPet != null) {
            currentPet.play();
            updatePetUI();
            Toast.makeText(MainActivity.this, "Played with Pet", Toast.LENGTH_SHORT).show();
        }
    }

    // Groom the pet
    private void groomPet() {
        if (currentPet != null) {
            currentPet.groom();
            updatePetUI();
            Toast.makeText(MainActivity.this, "Pet Groomed", Toast.LENGTH_SHORT).show();
        }
    }

    // Perform health check on the pet
    private void checkPetHealth() {
        if (currentPet != null) {
            currentPet.checkHealth();
            updatePetUI();
            Toast.makeText(MainActivity.this, "Health Check Done", Toast.LENGTH_SHORT).show();
        }
    }

    // Update pet UI elements
    private void updatePetUI() {
        petName.setText(currentPet.getName());
        petBreed.setText(currentPet.getBreed());
        petAge.setText(String.valueOf(currentPet.getAge()));
        petPersonality.setText(currentPet.getPersonality());
        hungerLevel.setText(String.valueOf(currentPet.getHunger()));
        happinessLevel.setText(String.valueOf(currentPet.getHappiness()));
        cleanlinessLevel.setText(String.valueOf(currentPet.getCleanliness()));
        healthLevel.setText(String.valueOf(currentPet.getHealth()));
    }

    // Handle pet selection
    private void onPetSelected(Pet pet) {
        currentPet = pet;
        updatePetUI();
    }

    // Virtual Pet class with attributes and methods for interaction
    static class Pet {
        private String name;
        private String breed;
        private int age;
        private String personality;
        private int hunger;
        private int happiness;
        private int cleanliness;
        private int health;

        public Pet(String name, String breed, int age, String personality) {
            this.name = name;
            this.breed = breed;
            this.age = age;
            this.personality = personality;
            this.hunger = 100;
            this.happiness = 100;
            this.cleanliness = 100;
            this.health = 100;
        }

        public String getName() {
            return name;
        }

        public String getBreed() {
            return breed;
        }

        public int getAge() {
            return age;
        }

        public String getPersonality() {
            return personality;
        }

        public int getHunger() {
            return hunger;
        }

        public int getHappiness() {
            return happiness;
        }

        public int getCleanliness() {
            return cleanliness;
        }

        public int getHealth() {
            return health;
        }

        public void feed() {
            hunger = Math.min(hunger + 10, 100);
        }

        public void play() {
            happiness = Math.min(happiness + 10, 100);
        }

        public void groom() {
            cleanliness = Math.min(cleanliness + 10, 100);
        }

        public void checkHealth() {
            health = Math.min(health + 10, 100);
        }
    }

    // Adapter for RecyclerView to display the list of pets
    static class PetAdapter extends RecyclerView.Adapter<PetViewHolder> {
        private final List<Pet> petList;
        private final OnPetSelectedListener listener;

        public PetAdapter(List<Pet> petList, OnPetSelectedListener listener) {
            this.petList = petList;
            this.listener = listener;
        }

        @Override
        public PetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pet_item, parent, false);
            return new PetViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PetViewHolder holder, int position) {
            Pet pet = petList.get(position);
            holder.bind(pet, listener);
        }

        @Override
        public int getItemCount() {
            return petList.size();
        }
    }

    // ViewHolder for RecyclerView
    static class PetViewHolder extends RecyclerView.ViewHolder {
        private final TextView petName;
        private final TextView petBreed;
        private final TextView petAge;
        private final TextView petPersonality;

        public PetViewHolder(View itemView) {
            super(itemView);
            petName = itemView.findViewById(R.id.petName);
            petBreed = itemView.findViewById(R.id.petBreed);
            petAge = itemView.findViewById(R.id.petAge);
            petPersonality = itemView.findViewById(R.id.petPersonality);
        }

        public void bind(Pet pet, OnPetSelectedListener listener) {
            petName.setText(pet.getName());
            petBreed.setText(pet.getBreed());
            petAge.setText(String.valueOf(pet.getAge()));
            petPersonality.setText(pet.getPersonality());
            itemView.setOnClickListener(v -> listener.onPetSelected(pet));
        }
    }

    // Interface for pet selection callbacks
    interface OnPetSelectedListener {
        void onPetSelected(Pet pet);
    }
}
```

This code provides a basic implementation of the Virtual Pet Care App, including user authentication, pet selection, and various pet care activities. The app also includes UI updates and interaction with Firebase. Please note that actual implementation details such as layout XML files, Firebase database rules, and additional UI/UX enhancements would need to be created to complete the project fully. 

Make sure to use appropriate XML layout files (not included here) to match the IDs used in the Java code above.