// Import statements omitted for brevity

public class SmartGroceryListApp extends Application {
    // Entry point for the application. Might initialize global resources here.

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}

// MainActivity - Main entry point of the app, handles UI navigation.
public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if the user is logged in, if not redirect to LoginActivity
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Load the primary fragment
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
        }
    }
}

// LoginActivity - Handles user authentication via Email, Google, or Facebook.
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    // UI elements
    private EditText emailField, passwordField;
    private Button emailLoginButton, googleLoginButton, facebookLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailField = findViewById(R.id.email_field);
        passwordField = findViewById(R.id.password_field);
        emailLoginButton = findViewById(R.id.email_sign_in_button);
        googleLoginButton = findViewById(R.id.google_sign_in_button);
        facebookLoginButton = findViewById(R.id.facebook_sign_in_button);

        emailLoginButton.setOnClickListener(v -> signInWithEmail());
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());
        facebookLoginButton.setOnClickListener(v -> signInWithFacebook());
    }

    // Email sign in
    private void signInWithEmail() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    // Google sign in
    private void signInWithGoogle() {
        // Implementation for Google Sign-In
    }

    // Facebook sign in
    private void signInWithFacebook() {
        // Implementation for Facebook Sign-In
    }
}

// HomeFragment - Main interface for managing grocery lists.
public class HomeFragment extends Fragment {

    private RecyclerView groceryListRecyclerView;
    private FloatingActionButton fabAddList;
    private GroceryListAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        groceryListRecyclerView = root.findViewById(R.id.grocery_list_recycler_view);
        fabAddList = root.findViewById(R.id.fab_add_list);
        adapter = new GroceryListAdapter();

        groceryListRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groceryListRecyclerView.setAdapter(adapter);

        fabAddList.setOnClickListener(v -> {
            // Open activity or dialog to add a new grocery list
        });

        loadGroceryLists();

        return root;
    }

    private void loadGroceryLists() {
        // Load grocery lists from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("grocery_lists")
          .get()
          .addOnCompleteListener(task -> {
              if (task.isSuccessful()) {
                  List<GroceryList> lists = new ArrayList<>();
                  for (DocumentSnapshot document : task.getResult()) {
                      GroceryList list = document.toObject(GroceryList.class);
                      lists.add(list);
                  }
                  adapter.setLists(lists);
              } else {
                  Toast.makeText(getContext(), "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
              }
          });
    }
}

// GroceryListAdapter - RecyclerView.Adapter for displaying grocery lists.
class GroceryListAdapter extends RecyclerView.Adapter<GroceryListAdapter.GroceryListViewHolder> {

    private List<GroceryList> lists = new ArrayList<>();

    @NonNull
    @Override
    public GroceryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_grocery_list, parent, false);
        return new GroceryListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryListViewHolder holder, int position) {
        GroceryList list = lists.get(position);
        holder.bind(list);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public void setLists(List<GroceryList> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    static class GroceryListViewHolder extends RecyclerView.ViewHolder {

        private TextView listName;
        private RecyclerView itemsRecyclerView;

        GroceryListViewHolder(@NonNull View itemView) {
            super(itemView);
            listName = itemView.findViewById(R.id.list_name);
            itemsRecyclerView = itemView.findViewById(R.id.items_recycler_view);
        }

        void bind(GroceryList list) {
            listName.setText(list.getName());

            ItemAdapter itemAdapter = new ItemAdapter();
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            itemsRecyclerView.setAdapter(itemAdapter);
            itemAdapter.setItems(list.getItems());
        }
    }
}

// ItemAdapter - RecyclerView.Adapter for displaying grocery list items.
class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> items = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_grocery_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView itemName, itemCategory;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemCategory = itemView.findViewById(R.id.item_category);
        }

        void bind(Item item) {
            itemName.setText(item.getName());
            itemCategory.setText(item.getCategory());
        }
    }
}

// Models
class GroceryList {
    private String name;
    private List<Item> items;

    // getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}

class Item {
    private String name;
    private String category;

    // getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

// Summary: This setup includes User authentication with FirebaseAuth, RecyclerView for displaying grocery lists and items, Firestore for database operations, and UI flow from MainActivity to HomeFragment and LoginActivity for auth management. Further implementation should include additional activities for barcode scanning, voice integration, cloud synchronization, etc.