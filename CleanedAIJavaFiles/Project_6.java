// MainActivity.java
package com.example.bookwormhaven;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

// Main activity to host fragments and handle user navigation
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bottomNav = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recycler_view);
        bookList = new ArrayList<>();

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(bookList);
        recyclerView.setAdapter(bookAdapter);

        // BottomNavigationView setup and item selection handler
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    loadFragment(new HomeFragment());
                    return true;
                case R.id.nav_wishlist:
                    loadFragment(new WishlistFragment());
                    return true;
                case R.id.nav_profile:
                    loadFragment(new ProfileFragment());
                    return true;
            }
            return false;
        });

        // Load default fragment
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // Fetch and display books
        fetchBooks();
    }

    // Load selected fragment
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    // Fetch books from Firestore based on user ID
    private void fetchBooks() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("books").whereEqualTo("userId", user.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            bookList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Book book = document.toObject(Book.class);
                                bookList.add(book);
                            }
                            bookAdapter.notifyDataSetChanged();
                        } else {
                            Log.w("MainActivity", "Error getting documents.", task.getException());
                        }
                    });
        }
    }
}

// Book.java - Model class for a Book entity
class Book {
    private String title;
    private String author;
    private String genre;
    private int publicationYear;
    private String coverImage;
    private String isbn;
    private String userId; // To identify which user added the book
    private String status; // "Want to Read", "Currently Reading", "Read"
    private float rating;
    private String review;
   
    // Constructors, getters, and setters
    // ...
}

// BookAdapter.java - RecyclerView Adapter for displaying books
class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;

    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.bind(book);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    class BookViewHolder extends RecyclerView.ViewHolder {

        TextView title, author, status;
        ImageView coverImage;

        public BookViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            status = itemView.findViewById(R.id.book_status);
            coverImage = itemView.findViewById(R.id.book_cover);
        }

        public void bind(Book book) {
            title.setText(book.getTitle());
            author.setText(book.getAuthor());
            status.setText(book.getStatus());
            Glide.with(itemView.getContext()).load(book.getCoverImage()).into(coverImage);
            itemView.setOnClickListener(v -> {
                // Handle book item click (e.g., open book detail)
            });
        }
    }
}

// HomeFragment.java
public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}

// WishlistFragment.java
public class WishlistFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }
}

// ProfileFragment.java
public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}

// SignUpActivity.java for user sign up
public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email_field);
        passwordField = findViewById(R.id.password_field);
        signUpButton = findViewById(R.id.sign_up_button);

        // Sign up button event handler
        signUpButton.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

// SignInActivity.java for user sign in
public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button signInButton, googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email_field);
        passwordField = findViewById(R.id.password_field);
        signInButton = findViewById(R.id.sign_in_button);
        googleSignInButton = findViewById(R.id.google_sign_in_button);

        // Sign in button event handler
        signInButton.setOnClickListener(v -> signIn());

        // Google sign-in button event handler (Google sign in process should be implemented)
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void signIn() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void signInWithGoogle() {
        // Implement Google Sign-In
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

// BookDetailActivity.java for displaying book details and adding reviews, notes, etc.
public class BookDetailActivity extends AppCompatActivity {
    private TextView title, author, genre, publicationYear, isbn;
    private ImageView coverImage;
    private EditText reviewField, noteField;
    private RatingBar ratingBar;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        title = findViewById(R.id.book_title);
        author = findViewById(R.id.book_author);
        genre = findViewById(R.id.book_genre);
        publicationYear = findViewById(R.id.book_publication_year);
        isbn = findViewById(R.id.book_isbn);
        coverImage = findViewById(R.id.book_cover);
        reviewField = findViewById(R.id.book_review);
        noteField = findViewById(R.id.book_note);
        ratingBar = findViewById(R.id.book_rating);
        saveButton = findViewById(R.id.save_button);

        // Get book data from intent
        Book book = (Book) getIntent().getSerializableExtra("book");
        bindData(book);

        // Save button event handler
        saveButton.setOnClickListener(v -> saveBookDetails(book));
    }

    private void bindData(Book book) {
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        genre.setText(book.getGenre());
        publicationYear.setText(String.valueOf(book.getPublicationYear()));
        isbn.setText(book.getIsbn());
        Glide.with(this).load(book.getCoverImage()).into(coverImage);
        reviewField.setText(book.getReview());
        noteField.setText(book.getNote());
        ratingBar.setRating(book.getRating());
    }

    private void saveBookDetails(Book book) {
        // Save book review, notes, and rating here
    }
}