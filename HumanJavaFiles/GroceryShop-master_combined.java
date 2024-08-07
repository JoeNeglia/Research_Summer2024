package com.carsonskjerdal.app.groceryshop;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.carsonskjerdal.app.groceryshop", appContext.getPackageName());
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by Carson on 2018-01-13.
 * <p>
 * Feel free to use code just give credit please :)
 */

@RunWith(AndroidJUnit4.class)
public class MainHomePageActivityTest {

    @Rule
    public ActivityTestRule<MainHomePageActivity> activityTestRule = new ActivityTestRule<>(MainHomePageActivity.class);

    @Test
    public void TestItems(){

        //check shopButton is visible
        Espresso.onView(withId(R.id.shop_button)).check(matches((isDisplayed())));

    }
}


package com.carsonskjerdal.app.groceryshop;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}

package com.carsonskjerdal.app.groceryshop;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Carson on 2017-12-13.
 * <p>
 * Feel free to use code just give credit please :)
 *
 * Singleton that controls access to the SQLiteDatabase instance
 * for this application.
 */

public class DatabaseManager {
    private Integer mOpenCounter = 0;

    private static DatabaseManager sInstance;
    private DatabaseHelper myDbHelper;
    private SQLiteDatabase mDatabase;

    public DatabaseManager(Context context) {
        myDbHelper = new DatabaseHelper(context);
    }

    public static synchronized DatabaseManager getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter+=1;
        if(mOpenCounter == 1) {
            // Opening new database
            mDatabase = myDbHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter-=1;
        if(mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }

    public Cursor queryAllItems(String table) {
        //Implements the query
        SQLiteDatabase db = myDbHelper.getReadableDatabase();
        return db.rawQuery("select * from " + table, null);
    }

}


package com.carsonskjerdal.app.groceryshop;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Carson on 2018-01-03.
 * <p>
 * Feel free to use code just give credit please :)
 */

public class NotificationHandler {

    // Notification handler singleton
    private static NotificationHandler nHandler;
    private static NotificationManager mNotificationManager;


    private NotificationHandler () {}


    /**
     * Singleton pattern implementation
     * @return
     */
    public static  NotificationHandler getInstance(Context context) {
        if(nHandler == null) {
            nHandler = new NotificationHandler();
            mNotificationManager =
                    (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return nHandler;
    }


    /**
     * Shows a simple notification
     * @param context aplication context
     */
    public void createSimpleNotification(Context context) {
        // Creates an explicit intent for an Activity
        Intent resultIntent = new Intent(context, FinishedActivity.class);

        // Creating a artifical activity stack for the notification activity
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(FinishedActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        // Pending intent to the notification manager
        PendingIntent resultPending = stackBuilder
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        simpleDateFormat.format(currentTime);
        Log.e("time", " current time is" + currentTime);
        String time = String.valueOf(currentTime);

        // Building the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // notification icon
                .setContentTitle("Grocery Purchase Reminder") // main title of the notification
                .setContentText("Your order should be ready around " + time) // notification text
                .setContentIntent(resultPending); // notification intent

        // mId allows you to update the notification later on.
        mNotificationManager.notify(10, mBuilder.build());
    }

}


package com.carsonskjerdal.app.groceryshop;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class MainHomePageActivity extends BaseActivity {

    //Ui References
    Button shopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home_page);

        //assign buttons
        shopButton = findViewById(R.id.shop_button);
        shopButton.setOnClickListener(onClickListener);


    }

    //listens for button clicks to launch activities
    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            switch(v.getId()){
                case R.id.shop_button:
                    //Launch Shop Activity
                    Intent i = new Intent(MainHomePageActivity.this, GroceryActivity.class);
                    startActivity(i);

                    break;

            }

        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ProductsActivity extends AppCompatActivity {

    //Ui Componenets

    SearchView searchView;

    //Adapter
    ProductsExpandableAdapter mAdapter;

    //list
    List<Products> list;

    //Database
    DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbManager = DatabaseManager.getInstance(this);

        Intent intent = getIntent();
        String name = intent.getStringExtra("resultName");

        //Search Bar
        searchView = findViewById(R.id.search_view);
        //Turn iconified to false:
        searchView.setIconified(false);
        //The above line will expand it to fit the area as well as throw up the keyboard

        //To remove the keyboard, but make sure you keep the expanded version:
        searchView.clearFocus();

        //proper list temporarily generated until a database is built
        final List<Products> products = generateProductList(name);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new ProductsExpandableAdapter(this, products);
        mAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int position) {
                Products expandedProducts = products.get(position);

               /* String toastMsg = getResources().getString("expanded", expandedProducts.getName());
                Toast.makeText(ProductsActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();*/
            }

            @Override
            public void onListItemCollapsed(int position) {
                Products expandedProducts = products.get(position);

               /* String toastMsg = getResources().getString("expanded", expandedProducts.getName());
                Toast.makeText(ProductsActivity.this,
                        toastMsg,
                        Toast.LENGTH_SHORT)
                        .show();*/
            }
        });

        recyclerView.setAdapter(mAdapter);
       // recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                llm.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        recyclerView.setLayoutManager(llm);

    }


    private List<Products> generateProductList(String name) {

        List<Products> list = new ArrayList<>();
        Products products;
        ProductsChild productsChild;

        //opens a cursor containing all the data from our database Table

        //loop through putting the cursor data into object which are then put into a list
        try (Cursor cursor = dbManager.queryAllItems("products")) {
            while (cursor.moveToNext()) {
                //if the group matches the grocery name then add this to current list
                String data3 = cursor.getString(3);
                if (data3.equals(name)) {

                    //pull name, image, and price
                    String data = cursor.getString(1);
                    String data2 = cursor.getString(2);
                    Log.e("Cursor"," " + data2);
                    String price = cursor.getString(4);

                    //add child details, then combine to make a product item, finally add to list
                    productsChild = new ProductsChild(price);
                    products = new Products(data, data2, Collections.singletonList(productsChild));

                    list.add(products);
                }
            }
            //close the cursor after use.
            cursor.close();
        }

        return list;


       /* final List<Products> productsList = Arrays.asList(appleProduct1, appleProduct1);

        return productsList;*/
    }


    void filter(String text) {
        List<Products> temp = new ArrayList<>();
        for (Products d : list) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            String searchText = text.toLowerCase();
            String recycleText = d.getName().toLowerCase();
            if (recycleText.contains(searchText)) {
                temp.add(d);

            }
        }
        //update recyclerview
        //productsExpandableAdapter.updateList(temp);


    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_complete:
                // complete workout
                Integer int1 = 0;
                //Adds new workout to Adapter
                Intent myIntent = new Intent(ProductsActivity.this,
                        CartActivity.class);
                startActivityForResult(myIntent, int1);
                return true;

            /*case R.id.action_complete:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;*/

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


/**
 * Created by Carson on 2017-11-28.
 * <p>
 * Feel free to use code just give credit please :)
 */

public class ProductsChildViewHolder extends ChildViewHolder {

    public TextView mDateText;
    public Button mAddButton;
    public TextView mQuanityText;
    public SeekBar mSeekBar;
    View view;
    // variable to hold context
    private Context context;

    public ProductsChildViewHolder(final View itemView) {
        super(itemView);

        final DatabaseManager dbManager = DatabaseManager.getInstance(itemView.getContext());
        final SQLiteDatabase mDatabase = dbManager.openDatabase();


        mDateText = itemView.findViewById(R.id.child_list_item_crime_date_text_view);
        mAddButton = itemView.findViewById(R.id.add_button);


        view = itemView.findViewById(R.id.seekBar);

        mQuanityText = view.findViewById(R.id.textViewCustom);
        mSeekBar = view.findViewById(R.id.seekBarCustom);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                mQuanityText.setText("Quantity: " + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //add the item to the shopping cart.
                    //does not work if quantity is zero
                String quanityCount = (String) mQuanityText.getText();
                String quanityNumber = quanityCount.replaceAll("[^0-9]", "");
                int count = Integer.parseInt(quanityNumber);
                if (count != 0) {
                    //give you the position of the parentLayout
                    int position = getAdapterPosition() - 1;
                    View view2 = itemView.getRootView();
                    RecyclerView rv = view2.findViewById(R.id.recycler_view);
                    ProductsExpandableAdapter mAdapter = (ProductsExpandableAdapter) rv.getAdapter();
                    List list = mAdapter.getParentItemList();
                    //the current product you are on
                    Products products = (Products) list.get(position);

                    Log.e("list output", "" + products.getName());

                    String priceTag = (String) mDateText.getText();

                    //toast
                    String toastMsg = mDateText.getText() + " Added To Cart";
                    Toast.makeText(view.getContext(),
                            toastMsg,
                            Toast.LENGTH_SHORT)
                            .show();

                    // Add items to cart table to the database
                    ContentValues values = new ContentValues();
                    values.put("cartName", products.getName());
                    values.put("cartImage", products.getImage());
                    values.put("cartPrice", priceTag);
                    values.put("cartQuantity", count);
                    mDatabase.insert("cart", null, values);
                    Log.e("values", "" + values);
                } else {
                    //toast
                    String toastMsg = "Quantity must be greater than zero.";
                    Toast.makeText(view.getContext(),
                            toastMsg,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }

}

package com.carsonskjerdal.app.groceryshop;




import java.util.List;

/**
 * Created by Carson on 2017-11-22.
 * <p>
 * Feel free to use code just give credit please :)
 */

public final class Products implements ParentListItem {

    private String name;
    private String image;
    private List<ProductsChild> mChildrenList;

    public Products(String name, String image, List<ProductsChild> productsChildren){

        this.name = name;
        this.image = image;
        mChildrenList = productsChildren;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }


    @Override
    public List<?> getChildItemList() {
        return mChildrenList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class GroceryActivity extends AppCompatActivity {

    //Ui Componenets
    RecyclerView recyclerView;
    SearchView searchView;
    ImageButton cartButton;

    //Adapter
    GroceryAdapter myAdapter;

    //list
    List<Groceries> list;

    //Database
    DatabaseManager dbManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);

        dbManager = DatabaseManager.getInstance(this);

        //Search Bar
        searchView = findViewById(R.id.search_view);

        //Turn iconified to false:
        searchView.setIconified(false);
        //The above line will expand it to fit the area as well as throw up the keyboard

        //To remove the keyboard, but make sure you keep the expanded version:
        searchView.clearFocus();

        list = buildList();

        //Setup Recycler & Adapter
        recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                llm.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        myAdapter = new GroceryAdapter(list);

        recyclerView.setAdapter(myAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do your search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //update recycler view to display the results
                filter(newText);
                return true;
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // launch InsectDetailsActivity passing information over.
                        Intent intent = new Intent(view.getContext(), ProductsActivity.class);

                        //passing data over to next activity
                        Groceries grocery = list.get(position);
                        String nameToPass = grocery.getName();

                        intent.putExtra("resultName", nameToPass);

                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // in case LongClick is to later be implemented
                    }
                })
        );



        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_complete:
                // complete workout
                Integer int1 = 0;
                //Adds new workout to Adapter
                Intent myIntent = new Intent(GroceryActivity.this,
                        CartActivity.class);
                startActivityForResult(myIntent, int1);
                return true;

            /*case R.id.action_complete:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;*/

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }



   private List<Groceries> buildList() {
       List<Groceries> list = new ArrayList<>();
       Groceries groceries;

       //opens a cursor containing all the data from our database Table

       //loop through putting the cursor data into object which are then put into a list
       try (Cursor cursor = dbManager.queryAllItems("groceries")) {
           while (cursor.moveToNext()) {
               String data = cursor.getString(1);
               String data2 = cursor.getString(2);

               groceries = new Groceries(data, data2);

               list.add(groceries);
           }
           //close the cursor after use.
           cursor.close();
       }

       return list;
    }

    void filter(String text) {
        List<Groceries> temp = new ArrayList<>();
        for (Groceries d : list) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            String searchText = text.toLowerCase();
            String recycleText = d.getName().toLowerCase();
            if (recycleText.contains(searchText)) {
                temp.add(d);

            }

            //update recyclerview
            myAdapter.updateList(temp);


        }
    }
}



package com.carsonskjerdal.app.groceryshop;

/**
 * Created by Carson on 2017-12-05.
 * <p>
 * Feel free to use code just give credit please :)
 */


import java.util.List;

/**
 * Wrapper used to link expanded state with a {@link ParentListItem}.
 *
 * @author Ryan Brooks
 * @version 1.0
 * @since 6/11/15
 */
public class ParentWrapper {

    private boolean mExpanded;
    private ParentListItem mParentListItem;

    /**
     * Default constructor.
     *
     * @param parentListItem The {@link ParentListItem} to wrap
     */
    public ParentWrapper(ParentListItem parentListItem) {
        mParentListItem = parentListItem;
        mExpanded = false;
    }

    /**
     * Gets the {@link ParentListItem} being wrapped.
     *
     * @return The {@link ParentListItem} being wrapped
     */
    public ParentListItem getParentListItem() {
        return mParentListItem;
    }

    /**
     * Sets the {@link ParentListItem} to wrap.
     *
     * @param parentListItem The {@link ParentListItem} to wrap
     */
    public void setParentListItem(ParentListItem parentListItem) {
        mParentListItem = parentListItem;
    }

    /**
     * Gets the expanded state associated with the {@link ParentListItem}.
     *
     * @return true if expanded, false if not
     */
    public boolean isExpanded() {
        return mExpanded;
    }

    /**
     * Sets the expanded state associated with the {@link ParentListItem}.
     *
     * @param expanded true if expanded, false if not
     */
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    public boolean isInitiallyExpanded() {
        return mParentListItem.isInitiallyExpanded();
    }

    public List<?> getChildItemList() {
        return mParentListItem.getChildItemList();
    }
}

package com.carsonskjerdal.app.groceryshop;


/**
 * Created by Carson on 2017-11-28.
 * <p>
 * Feel free to use code just give credit please :)
 */

public class ProductsChild {


    private String mPrice;

    public ProductsChild(String price) {
        mPrice = price;
    }


    public String getPrice() {
        return mPrice;
    }


}


package com.carsonskjerdal.app.groceryshop;


import android.util.Log;

/**
 * Created by Carson on 2017-11-22.
 * <p>
 * Feel free to use code just give credit please :)
 */

public final class CartItems {

    private String name;
    private Integer image;
    private String price;
    private String quantity;

    public CartItems(String name, Integer image, String price, String quantity){

        this.name = name;
        this.image = image;
        this.price = price;
        this.quantity = quantity;

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(Integer image) {
        this.image = image;
    }

    public Integer getImage() {
        return image;
    }


    public void setPrice(String price) {
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getQuantity() {
        return quantity;
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.braintreepayments.cardform.view.CardForm;

public class CheckoutActivity extends AppCompatActivity {

    Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        CardForm cardForm =  findViewById(R.id.card_form);
        cardForm.cardRequired(true)
                .expirationRequired(true)
                .cvvRequired(true)
                .postalCodeRequired(true)
                .mobileNumberRequired(true)
                .mobileNumberExplanation("SMS is required on this number")
                .actionLabel("Purchase")
                .setup(this);

        buttonSubmit = findViewById(R.id.buttonFinish);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //do purchase work..

                //launch finished activity
                Intent i = new Intent(CheckoutActivity.this, FinishedActivity.class);

                startActivity(i);
            }
        });
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Carson on 2017-11-22.
 *
 * RecyclerView adapter extended with project-specific required methods.
 */

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.GroceryHolder> {

    private List<Groceries> groceryList;
    List<GroceryHolder> displayedList;

    public GroceryAdapter(List<Groceries> list) {
        groceryList = list;


    }

    /* ViewHolder for each insect item */
    public class GroceryHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        ImageView image;


        GroceryHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
        }



        @Override
        public void onClick(View v) {

        }
    }

    @Override
    public GroceryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grocery_item_layout, parent, false);


        return new GroceryHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GroceryHolder holder, int position) {
        Groceries grocery = groceryList.get(position);

        //Sets Text
        holder.name.setText(grocery.getName());

        //sets image
        Log.e("adapter","image " + grocery.getImage());
        String uri = grocery.getImage();
        Resources res = holder.image.getContext().getResources();
        holder.image.setImageResource(res.getIdentifier(uri, "drawable", BuildConfig.APPLICATION_ID));
        //holder.image.setImageResource(grocery.getImage());

    }


    @Override
    public int getItemCount() {
        return groceryList.size();
    }


    public void updateList(List<Groceries> newList){
        groceryList = newList;
        notifyDataSetChanged();
    }


}


package com.carsonskjerdal.app.groceryshop;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Created by Carson on 2017-12-06.
 * <p>
 * Feel free to use code just give credit please :)
 */

public class QuanitySeekBar extends android.support.v7.widget.AppCompatSeekBar {

    public QuanitySeekBar(Context context) {
        super(context);
    }

    public QuanitySeekBar (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public QuanitySeekBar (Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}


package com.carsonskjerdal.app.groceryshop;

/**
 * Created by Carson on 2017-12-05.
 * <p>
 * Feel free to use code just give credit please :)
 */


import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * ViewHolder for a child list
 * item.
 * <p>
 * The user should extend this class and implement as they wish for their
 * child list item.
 *
 * @author Ryan Brooks
 * @version 1.0
 * @since 5/27/2015
 */
public class ChildViewHolder extends RecyclerView.ViewHolder {

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public ChildViewHolder(View itemView) {
        super(itemView);
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import java.util.List;

/**
 * Created by Carson on 2017-11-28.
 * <p>
 * Feel free to use code just give credit please :)
 */

public class ProductsExpandableAdapter extends ExpandableRecyclerAdapter<ProductsParentViewHolder, ProductsChildViewHolder> {

    LayoutInflater mInflater;

    public ProductsExpandableAdapter(Context context, List<Products> parentItemList) {
        super(parentItemList);

        mInflater = LayoutInflater.from(context);
    }

    //creates the main layout in the recycler view.
    @Override
    public ProductsParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.product_item_layout, viewGroup, false);
        return new ProductsParentViewHolder(view);
    }

    //creates the child layout
    @Override
    public ProductsChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.product_item_layout_child, viewGroup, false);
        return new ProductsChildViewHolder(view);
    }


    //binds daya with the main parent layout
    @Override
    public void onBindParentViewHolder(ProductsParentViewHolder productViewHolder, int position, ParentListItem parentListItem) {
        Products products = (Products) parentListItem;
        productViewHolder.bind(products);
    }

    //binds data with the child layout
    @Override
    public void onBindChildViewHolder(ProductsChildViewHolder productsChildViewHolder, int i, Object childObject) {
        ProductsChild productsChild = (ProductsChild) childObject;
        productsChildViewHolder.mDateText.setText(productsChild.getPrice());

    }


}


package com.carsonskjerdal.app.groceryshop;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Carson on 2017-12-13.
 * <p>
 * Feel free to use code just give credit please :)
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "groceryDatabase";
    private static final int DATABASE_VERSION = 18;

    // Table Names
    private static final String TABLE_GROCERIES = "groceries";
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_CART = "cart";

    // Grocery Table Columns
    private static final String KEY_GROCERIES_ID = "id";
    private static final String KEY_GROCERIES_NAME = "groceryName";
    private static final String KEY_GROCERIES_IMAGE = "groceryImage";

    // User Table Columns
    private static final String KEY_PRODUCTS_ID = "id";
    private static final String KEY_PRODUCTS_NAME = "productsName";
    private static final String KEY_PRODUCTS_IMAGE = "productsImage";
    private static final String KEY_PRODUCTS_GROUP = "productsGroup";
    private static final String KEY_PRODUCTS_PRICE = "productsPrice";

    // Cart Table Columns
    private static final String KEY_CART_ID = "id";
    private static final String KEY_CART_NAME = "cartName";
    private static final String KEY_CART_IMAGE = "cartImage";
    private static final String KEY_CART_PRICE = "cartPrice";
    private static final String KEY_CART_QUANTITY = "cartQuantity";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_GROCERIES +
                "(" +
                KEY_GROCERIES_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_GROCERIES_NAME + " TEXT, " +
                KEY_GROCERIES_IMAGE + " TEXT" +
                ")";

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS +
                "(" +
                KEY_PRODUCTS_ID + " INTEGER PRIMARY KEY," +
                KEY_PRODUCTS_NAME + " TEXT," +
                KEY_PRODUCTS_IMAGE + " TEXT," +
                KEY_PRODUCTS_GROUP + " TEXT," +
                KEY_PRODUCTS_PRICE + " TEXT" +
                ")";

        String CREATE_CART_TABLE = "CREATE TABLE " + TABLE_CART +
                "(" +
                KEY_CART_ID + " INTEGER PRIMARY KEY," +
                KEY_CART_NAME + " TEXT," +
                KEY_CART_IMAGE + " TEXT," +
                KEY_CART_PRICE + " TEXT," +
                KEY_CART_QUANTITY + " TEXT" +
                ")";

        db.execSQL(CREATE_POSTS_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CART_TABLE);

        addTableData(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
            onCreate(db);
        }
    }


    private void addTableData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        //add Grocery data to its table

        String[][] data = {{"Fish", "fishicon48"}, {"Bread", "breadicon"}, {"Milk", "milkicon"}, {"Apples", "appleicon"},
                {"Oranges", "orangeicon"}, {"Candy", "candyicon"}, {"Soup", "soupicon"}, {"Medicine", "medicineicon"}, {"Pasta", "pastaicon"},
                {"Condiments", "condimenticon"}, {"Soft Drinks", "softdrinkicon"}, {"Beef", "beeficon"}, {"Vegetables", "veggieicon"}, {"Cheese", "cheeseicon"}};

        for (String[] aData : data) {
            values.put(KEY_GROCERIES_NAME, aData[0]);
            values.put(KEY_GROCERIES_IMAGE, aData[1]);

            db.insert(TABLE_GROCERIES, null, values);
        }

        //add Product data to its table
        ContentValues values2 = new ContentValues();
        //ugly example data
        String[][] data2 = {{"Johnnys Fish", "fishicon48", "11.95", "Fish"},  {"Pirate Man Fish", "fishicon48", "15.95", "Fish"},  {"Prime AA Fish", "fishicon48", "11.95", "Fish"},  {"Bland Bread", "0", "3.15", "Bread"}
                ,  {"King's Bread", "0", "6.95", "Bread"},  {"Wonder Bread", "0", "3.95", "Bread"},  {"Moo Man Milk", "0", "4.25", "Milk"},  {"Prairie Cow", "0", "4.45", "Milk"}
                ,  {"American Milk", "0", "11.95", "Milk"},  {"Red Apples", "0", "11.95", "Apples"},  {"Round New Yorks", "0", "11.95", "Apples"},  {"Sour Sam Apples", "0", "11.95", "Apples"}
                ,  {"Not Red Oranges", "0", "0.95", "Oranges"},  {"Chinese", "0", "1.10", "Oranges"},  {"Box Oranges", "0", "18.95", "Oranges"},  {"Sour Keys", "0", "0.25", "Candy"}
                ,  {"Blue Whales", "0", "0.25", "Candy"},  {"Pringles", "0", "1.25", "Candy"},  {"Johnnys Fish", "0", "11.95", "Soup"},  {"Johnnys Fish", "0", "11.95", "Soup"}
                ,  {"Johnnys Fish Soup", "0", "11.95", "Soup"},  {"Johnnys Fish", "0", "11.95", "Medicine"},  {"Johnnys Fish", "0", "11.95", "Medicine"},  {"Johnnys Fish", "0", "11.95", "Medicine"}
                ,  {"Johnnys Fish Pasta", "0", "11.95", "Pasta"},  {"Johnnys Fish", "0", "11.95", "Pasta"},  {"Johnnys Fish", "0", "11.95", "Pasta"},  {"Johnnys Fish", "0", "11.95", "Condiments"}
                ,  {"Johnnys Fish", "0", "11.95", "Condiments"},  {"Johnnys Fish", "0", "11.95", "Soft Drinks"},  {"Johnnys Fish", "0", "11.95", "Soft Drinks"},  {"Johnnys Fish", "0", "11.95", "Soft Drinks"}
                ,  {"Johnnys Beef", "0", "11.95", "Beef"},  {"Johnnys Fish", "0", "11.95", "Condiments"},  {"Johnnys Fish", "0", "11.95", "Beef"},  {"Johnnys Fish", "0", "11.95", "Beef"}
                ,  {"Johnnys Fish", "0", "11.95", "Vegetables"},  {"Johnnys Fish", "0", "11.95", "Vegetables"},  {"Johnnys Fish", "0", "11.95", "Vegetables"},  {"Johnnys Fish", "0", "11.95", "Cheese"}
                ,  {"Johnnys Cheese", "0", "11.95", "Cheese"},  {"Johnnys Fish", "0", "11.95", "Cheese"}};

        for (String[] aData : data2) {
            values2.put(KEY_PRODUCTS_NAME, aData[0]);
            values2.put(KEY_PRODUCTS_IMAGE, aData[1]);
            values2.put(KEY_PRODUCTS_PRICE, aData[2]);
            values2.put(KEY_PRODUCTS_GROUP, aData[3]);

            db.insert(TABLE_PRODUCTS, null, values2);
        }
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    Button checkoutButton;
    List<CartItems> cartProducts;
    CartItems cartItems;

    DatabaseManager dbManager;
    //final SQLiteDatabase mDatabase = dbManager.openDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbManager = DatabaseManager.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(llm);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                llm.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        //method to build the CartProducts list
        cartProducts = getCartData();

        CartItemAdapter mAdapter = new CartItemAdapter(cartProducts);
        recyclerView.setAdapter(mAdapter);

        TextView textTotal = findViewById(R.id.textViewTotal);
        String total = buildPrice(cartProducts);
        textTotal.setText(total);

        //button for ending the activity and moving to transaction completion
        checkoutButton = findViewById(R.id.button_checkout);

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //pass the data

                //clear the data after payment is successful

                //opens payment window
                Intent myIntent = new Intent(CartActivity.this, CheckoutActivity.class);

                startActivity(myIntent);

                //purchase made closing screen with eta counter?
            }
        });
    }

    private String buildPrice(List<CartItems> cartProducts) {
        Double total = 0.0;
        CartItems cartItem;
        for(int i = 0; i < cartProducts.size(); i++){
            cartItem = cartProducts.get(i);
            total += Double.parseDouble(cartItem.getPrice());
        }

        return total.toString();
    }

    private List<CartItems> getCartData() {
        List<CartItems> list = new ArrayList<>();
        Integer image = R.mipmap.ic_launcher_round;
        cartItems = new CartItems("App1", 0, "19.21", "1");

        //opens a cursor containing all the data from our database Table

        //loop through putting the cursor data into object which are then put into a list
        try (Cursor cursor = dbManager.queryAllItems("cart")) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(1);
                Integer data2 = cursor.getInt(2);
                String data3 = cursor.getString(3);
                String data4 = cursor.getString(4);

                cartItems = new CartItems(data, data2, data3, data4);

                list.add(cartItems);
            }
            //close the cursor after use.
            cursor.close();
        }

        return list;
    }
}


package com.carsonskjerdal.app.groceryshop;

/**
 * Created by Carson on 2017-12-05.
 * <p>
 * Feel free to use code just give credit please :)
 */


import java.util.ArrayList;
import java.util.List;
/**
 * Helper for {@link ExpandableRecyclerAdapter}.
 *
 * Created by Ryan Brooks on 6/11/15.
 */
public class ExpandableRecyclerAdapterHelper {

    /**
     * Generates a full list of all {@link ParentListItem} objects and their
     * children, in order.
     *
     * @param parentItemList A list of the {@code ParentListItem} objects from
     *                       the {@link ExpandableRecyclerAdapter}
     * @return A list of all {@code ParentListItem} objects and their children, expanded
     */
    public static List<Object> generateParentChildItemList(List<? extends ParentListItem> parentItemList) {
        List<Object> parentWrapperList = new ArrayList<>();
        ParentListItem parentListItem;
        ParentWrapper parentWrapper;

        int parentListItemCount = parentItemList.size();
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = parentItemList.get(i);
            parentWrapper = new ParentWrapper(parentListItem);
            parentWrapperList.add(parentWrapper);

            if (parentWrapper.isInitiallyExpanded()) {
                parentWrapper.setExpanded(true);

                int childListItemCount = parentWrapper.getChildItemList().size();
                for (int j = 0; j < childListItemCount; j++) {
                    parentWrapperList.add(parentWrapper.getChildItemList().get(j));
                }
            }
        }

        return parentWrapperList;
    }
}


package com.carsonskjerdal.app.groceryshop;

/**
 * Created by Carson on 2017-11-24.
 * <p>
 * Feel free to use code just give credit please :)
 */


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onLongItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}
}


package com.carsonskjerdal.app.groceryshop;

/**
 * Created by Carson on 2017-12-05.
 * <p>
 * Feel free to use code just give credit please :)
 */

import java.util.List;

/**
 * Interface for implementing required methods in a parent list item.
 */
public interface ParentListItem {

    /**
     * Getter for the list of this parent list item's child list items.
     * <p>
     * If list is empty, the parent list item has no children.
     *
     * @return A {@link List} of the children of this {@link ParentListItem}
     */
    List<?> getChildItemList();

    /**
     * Getter used to determine if this {@link ParentListItem}'s
     * {@link android.view.View} should show up initially as expanded.
     *
     * @return true if expanded, false if not
     */
    boolean isInitiallyExpanded();
}


package com.carsonskjerdal.app.groceryshop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.e("Login","Attempted");
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("Login","Complete");
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Log.e("Login","Complete");
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);

        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //finish();
                Intent i = new Intent(LoginActivity.this, MainHomePageActivity.class);
                startActivity(i);
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



package com.carsonskjerdal.app.groceryshop;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Carson on 2017-11-22.
 * <p>
 * Feel free to use code just give credit please :)
 */

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}

//TODO Master todo List
//Pictures for images - working
//Quanity passes over to Cart
//Cart quantity data
//Pick if cart details are correct and change the options
//set up a logging in system
//rotation
//cancel item in order
//change quanity
//perhaps an information screen?
//fix up finish screen to look nicer
//      (wierd line must be the image itself)
//
//extras
//saving order history
//delivery tracking
//setting with features
//


package com.carsonskjerdal.app.groceryshop;

/**
 * Created by Carson on 2017-12-05.
 * <p>
 * Feel free to use code just give credit please :)
 */


import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RecyclerView.Adapter implementation that
 * adds the ability to expand and collapse list items.
 *
 * Changes should be notified through:
 * {@link #notifyParentItemInserted(int)}
 * {@link #notifyParentItemRemoved(int)}
 * {@link #notifyParentItemChanged(int)}
 * {@link #notifyParentItemRangeInserted(int, int)}
 * {@link #notifyChildItemInserted(int, int)}
 * {@link #notifyChildItemRemoved(int, int)}
 * {@link #notifyChildItemChanged(int, int)}
 * methods and not the notify methods of RecyclerView.Adapter.
 *
 * @author Ryan Brooks
 * @version 1.0
 * @since 5/27/2015
 */
public abstract class ExpandableRecyclerAdapter<PVH extends ParentViewHolder, CVH extends ChildViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ParentViewHolder.ParentListItemExpandCollapseListener {

    private static final String EXPANDED_STATE_MAP = "ExpandableRecyclerAdapter.ExpandedStateMap";
    private static final int TYPE_PARENT = 0;
    private static final int TYPE_CHILD = 1;

    /**
     * A {@link List} of all currently expanded {@link ParentListItem} objects
     * and their children, in order. Changes to this list should be made through the add/remove methods
     * available in {@link ExpandableRecyclerAdapter}
     */
    protected List<Object> mItemList;

    private List<? extends ParentListItem> mParentItemList;
    private ExpandCollapseListener mExpandCollapseListener;
    private List<RecyclerView> mAttachedRecyclerViewPool;

    /**
     * Allows objects to register themselves as expand/collapse listeners to be
     * notified of change events.
     * <p>
     * Implement this in your {@link android.app.Activity} or {@link android.app.Fragment}
     * to receive these callbacks.
     */
    public interface ExpandCollapseListener {

        /**
         * Called when a list item is expanded.
         *
         * @param position The index of the item in the list being expanded
         */
        void onListItemExpanded(int position);

        /**
         * Called when a list item is collapsed.
         *
         * @param position The index of the item in the list being collapsed
         */
        void onListItemCollapsed(int position);
    }

    /**
     * Primary constructor. Sets up {@link #mParentItemList} and {@link #mItemList}.
     *
     * Changes to {@link #mParentItemList} should be made through add/remove methods in
     * {@link ExpandableRecyclerAdapter}
     *
     * @param parentItemList List of all {@link ParentListItem} objects to be
     *                       displayed in the RecyclerView that this
     *                       adapter is linked to
     */
    public ExpandableRecyclerAdapter(List<Products> parentItemList) {
        super();
        mParentItemList = parentItemList;
        mItemList = ExpandableRecyclerAdapterHelper.generateParentChildItemList(parentItemList);
        mAttachedRecyclerViewPool = new ArrayList<>();
    }

    /**
     * Implementation of Adapter.onCreateViewHolder(ViewGroup, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either {@link #onCreateParentViewHolder(ViewGroup)}
     * or {@link #onCreateChildViewHolder(ViewGroup)}.
     *
     * @param viewGroup The {@link ViewGroup} into which the new {@link android.view.View}
     *                  will be added after it is bound to an adapter position.
     * @param viewType The view type of the new {@code android.view.View}.
     * @return A new RecyclerView.ViewHolder
     *         that holds a {@code android.view.View} of the given view type.
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_PARENT) {
            PVH pvh = onCreateParentViewHolder(viewGroup);
            pvh.setParentListItemExpandCollapseListener(this);
            return pvh;
        } else if (viewType == TYPE_CHILD) {
            return onCreateChildViewHolder(viewGroup);
        } else {
            throw new IllegalStateException("Incorrect ViewType found");
        }
    }

    /**
     * Implementation of Adapter.onBindViewHolder(RecyclerView.ViewHolder, int)
     * that determines if the list item is a parent or a child and calls through
     * to the appropriate implementation of either {@link #onBindParentViewHolder(ParentViewHolder, int, ParentListItem)}
     * or {@link #onBindChildViewHolder(ChildViewHolder, int, Object)}.
     *
     * @param holder The RecyclerView.ViewHolder to bind data to
     * @param position The index in the list at which to bind
     * @throws IllegalStateException if the item in the list is either null or
     *         not of type {@link ParentListItem}
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            PVH parentViewHolder = (PVH) holder;

            if (parentViewHolder.shouldItemViewClickToggleExpansion()) {
                parentViewHolder.setMainItemClickToExpand();
            }

            ParentWrapper parentWrapper = (ParentWrapper) listItem;
            parentViewHolder.setExpanded(parentWrapper.isExpanded());
            onBindParentViewHolder(parentViewHolder, position, parentWrapper.getParentListItem());
        } else if (listItem == null) {
            throw new IllegalStateException("Incorrect ViewHolder found");
        } else {
            onBindChildViewHolder((CVH) holder, position, listItem);
        }
    }

    /**
     * Callback called from {@link #onCreateViewHolder(ViewGroup, int)} when
     * the list item created is a parent.
     *
     * @param parentViewGroup The {@link ViewGroup} in the list for which a {@link PVH}
     *                        is being created
     * @return A {@code PVH} corresponding to the {@link ParentListItem} with
     *         the {@code ViewGroup} parentViewGroup
     */
    public abstract PVH onCreateParentViewHolder(ViewGroup parentViewGroup);

    /**
     * Callback called from {@link #onCreateViewHolder(ViewGroup, int)} when
     * the list item created is a child.
     *
     * @param childViewGroup The {@link ViewGroup} in the list for which a {@link CVH}
     *                       is being created
     * @return A {@code CVH} corresponding to the child list item with the
     *         {@code ViewGroup} childViewGroup
     */
    public abstract CVH onCreateChildViewHolder(ViewGroup childViewGroup);

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a parent.
     * <p>
     * Bind data to the {@link PVH} here.
     *
     * @param parentViewHolder The {@code PVH} to bind data to
     * @param position The index in the list at which to bind
     * @param parentListItem The {@link ParentListItem} which holds the data to
     *                       be bound to the {@code PVH}
     */
    public abstract void onBindParentViewHolder(PVH parentViewHolder, int position, ParentListItem parentListItem);

    /**
     * Callback called from onBindViewHolder(RecyclerView.ViewHolder, int)
     * when the list item bound to is a child.
     * <p>
     * Bind data to the {@link CVH} here.
     *
     * @param childViewHolder The {@code CVH} to bind data to
     * @param position The index in the list at which to bind
     * @param childListItem The child list item which holds that data to be
     *                      bound to the {@code CVH}
     */
    public abstract void onBindChildViewHolder(CVH childViewHolder, int position, Object childListItem);

    /**
     * Gets the number of parent and child objects currently expanded.
     *
     * @return The size of {@link #mItemList}
     */
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    /**
     * Gets the view type of the item at the given position.
     *
     * @param position The index in the list to get the view type of
     * @return {@value #TYPE_PARENT} for {@link ParentListItem} and {@value #TYPE_CHILD}
     *         for child list items
     * @throws IllegalStateException if the item at the given position in the list is null
     */
    @Override
    public int getItemViewType(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            return TYPE_PARENT;
        } else if (listItem == null) {
            throw new IllegalStateException("Null object added");
        } else {
            return TYPE_CHILD;
        }
    }

    /**
     * Gets the list of ParentItems that is backing this adapter.
     * Changes can be made to the list and the adapter notified via the
     * {@link #notifyParentItemInserted(int)}
     * {@link #notifyParentItemRemoved(int)}
     * {@link #notifyParentItemChanged(int)}
     * {@link #notifyParentItemRangeInserted(int, int)}
     * {@link #notifyChildItemInserted(int, int)}
     * {@link #notifyChildItemRemoved(int, int)}
     * {@link #notifyChildItemChanged(int, int)}
     * methods.
     *
     *
     * @return The list of ParentListItems that this adapter represents
     */
    public List<? extends ParentListItem> getParentItemList() {
        return mParentItemList;
    }

    /**
     * <p>
     * Called when a {@link ParentListItem} is triggered to expand.
     *
     * @param position The index of the item in the list being expanded
     */
    @Override
    public void onParentListItemExpanded(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            expandParentListItem((ParentWrapper) listItem, position, true);
        }
    }

    /**
     * <p>
     * Called when a {@link ParentListItem} is triggered to collapse.
     *
     * @param position The index of the item in the list being collapsed
     */
    @Override
    public void onParentListItemCollapsed(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            collapseParentListItem((ParentWrapper) listItem, position, true);
        }
    }

    /**
     * Implementation of Adapter#onAttachedToRecyclerView(RecyclerView).
     * <p>
     * Called when this {@link ExpandableRecyclerAdapter} is attached to a RecyclerView.
     *
     * @param recyclerView The {@code RecyclerView} this {@code ExpandableRecyclerAdapter}
     *                     is being attached to
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.add(recyclerView);
    }

    /**
     * Implementation of Adapter.onDetachedFromRecyclerView(RecyclerView)
     * <p>
     * Called when this ExpandableRecyclerAdapter is detached from a RecyclerView.
     *
     * @param recyclerView The {@code RecyclerView} this {@code ExpandableRecyclerAdapter}
     *                     is being detached from
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.remove(recyclerView);
    }

    public void setExpandCollapseListener(ExpandCollapseListener expandCollapseListener) {
        mExpandCollapseListener = expandCollapseListener;
    }

    // region Programmatic Expansion/Collapsing

    /**
     * Expands the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to expand
     */
    public void expandParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object listItem = getListItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (listItem instanceof ParentWrapper) {
            parentWrapper = (ParentWrapper) listItem;
        } else {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Expands the parent associated with a specified {@link ParentListItem} in
     * the list of parents.
     *
     * @param parentListItem The {@code ParentListItem} of the parent to expand
     */
    public void expandParent(ParentListItem parentListItem) {
        ParentWrapper parentWrapper = getParentWrapper(parentListItem);
        int parentWrapperIndex = mItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Expands all parents in a range of indices in the list of parents.
     *
     * @param startParentIndex The index at which to to start expanding parents
     * @param parentCount The number of parents to expand
     */
    public void expandParentRange(int startParentIndex, int parentCount) {
        int endParentIndex = startParentIndex + parentCount;
        for (int i = startParentIndex; i < endParentIndex; i++) {
            expandParent(i);
        }
    }

    /**
     * Expands all parents in the list.
     */
    public void expandAllParents() {
        for (ParentListItem parentListItem : mParentItemList) {
            expandParent(parentListItem);
        }
    }

    /**
     * Collapses the parent with the specified index in the list of parents.
     *
     * @param parentIndex The index of the parent to collapse
     */
    public void collapseParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object listItem = getListItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (listItem instanceof ParentWrapper) {
            parentWrapper = (ParentWrapper) listItem;
        } else {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Collapses the parent associated with a specified {@link ParentListItem} in
     * the list of parents.
     *
     * @param parentListItem The {@code ParentListItem} of the parent to collapse
     */
    public void collapseParent(ParentListItem parentListItem) {
        ParentWrapper parentWrapper = getParentWrapper(parentListItem);
        int parentWrapperIndex = mItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    /**
     * Collapses all parents in a range of indices in the list of parents.
     *
     * @param startParentIndex The index at which to to start collapsing parents
     * @param parentCount The number of parents to collapse
     */
    public void collapseParentRange(int startParentIndex, int parentCount) {
        int endParentIndex = startParentIndex + parentCount;
        for (int i = startParentIndex; i < endParentIndex; i++) {
            collapseParent(i);
        }
    }

    /**
     * Collapses all parents in the list.
     */
    public void collapseAllParents() {
        for (ParentListItem parentListItem : mParentItemList) {
            collapseParent(parentListItem);
        }
    }

    /**
     * Stores the expanded state map across state loss.
     * <p>
     * Should be called from {@link Activity#onSaveInstanceState(Bundle)} in
     * the {@link Activity} that hosts the RecyclerView that this
     * {@link ExpandableRecyclerAdapter} is attached to.
     * <p>
     * This will make sure to add the expanded state map as an extra to the
     * instance state bundle to be used in {@link #onRestoreInstanceState(Bundle)}.
     *
     * @param savedInstanceState The {@code Bundle} into which to store the
     *                           expanded state map
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(EXPANDED_STATE_MAP, generateExpandedStateMap());
    }

    /**
     * Fetches the expandable state map from the saved instance state {@link Bundle}
     * and restores the expanded states of all of the list items.
     * <p>
     * Should be called from {@link Activity#onRestoreInstanceState(Bundle)} in
     * the {@link Activity} that hosts the RecyclerView that this
     * {@link ExpandableRecyclerAdapter} is attached to.
     * <p>
     * Assumes that the list of parent list items is the same as when the saved
     * instance state was stored.
     *
     * @param savedInstanceState The {@code Bundle} from which the expanded
     *                           state map is loaded
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null
                || !savedInstanceState.containsKey(EXPANDED_STATE_MAP)) {
            return;
        }

        HashMap<Integer, Boolean> expandedStateMap = (HashMap<Integer, Boolean>) savedInstanceState.getSerializable(EXPANDED_STATE_MAP);
        if (expandedStateMap == null) {
            return;
        }

        List<Object> parentWrapperList = new ArrayList<>();
        ParentListItem parentListItem;
        ParentWrapper parentWrapper;

        int parentListItemCount = mParentItemList.size();
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = mParentItemList.get(i);
            parentWrapper = new ParentWrapper(parentListItem);
            parentWrapperList.add(parentWrapper);

            if (expandedStateMap.containsKey(i)) {
                boolean expanded = expandedStateMap.get(i);
                if (expanded) {
                    parentWrapper.setExpanded(true);

                    int childListItemCount = parentWrapper.getChildItemList().size();
                    for (int j = 0; j < childListItemCount; j++) {
                        parentWrapperList.add(parentWrapper.getChildItemList().get(j));
                    }
                }
            }
        }

        mItemList = parentWrapperList;

        notifyDataSetChanged();
    }

    /**
     * Gets the list item held at the specified adapter position.
     *
     * @param position The index of the list item to return
     * @return The list item at the specified position
     */
    protected Object getListItem(int position) {
        boolean indexInRange = position >= 0 && position < mItemList.size();
        if (indexInRange) {
            return mItemList.get(position);
        } else {
            return null;
        }
    }

    /**
     * Calls through to the ParentViewHolder to expand views for each
     * RecyclerView the specified parent is a child of.
     *
     * These calls to the ParentViewHolder are made so that animations can be
     * triggered at the ViewHolder level.
     *
     * @param parentIndex The index of the parent to expand
     */
    private void expandViews(ParentWrapper parentWrapper, int parentIndex) {
        PVH viewHolder;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            viewHolder = (PVH) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null && !viewHolder.isExpanded()) {
                viewHolder.setExpanded(true);
                viewHolder.onExpansionToggled(false);
            }

            expandParentListItem(parentWrapper, parentIndex, false);
        }
    }

    /**
     * Calls through to the ParentViewHolder to collapse views for each
     * RecyclerView a specified parent is a child of.
     *
     * These calls to the ParentViewHolder are made so that animations can be
     * triggered at the ViewHolder level.
     *
     * @param parentIndex The index of the parent to collapse
     */
    private void collapseViews(ParentWrapper parentWrapper, int parentIndex) {
        PVH viewHolder;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            viewHolder = (PVH) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null && viewHolder.isExpanded()) {
                viewHolder.setExpanded(false);
                viewHolder.onExpansionToggled(true);
            }

            collapseParentListItem(parentWrapper, parentIndex, false);
        }
    }

    /**
     * Expands a specified parent item. Calls through to the
     * ExpandCollapseListener and adds children of the specified parent to the
     * total list of items.
     *
     * @param parentWrapper The ParentWrapper of the parent to expand
     * @param parentIndex The index of the parent to expand
     * @param expansionTriggeredByListItemClick true if expansion was triggered
     *                                          by a click event, false otherwise.
     */
    private void expandParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean expansionTriggeredByListItemClick) {
        if (!parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(true);

            List<?> childItemList = parentWrapper.getChildItemList();
            if (childItemList != null) {
                int childListItemCount = childItemList.size();
                for (int i = 0; i < childListItemCount; i++) {
                    mItemList.add(parentIndex + i + 1, childItemList.get(i));
                }

                notifyItemRangeInserted(parentIndex + 1, childListItemCount);
            }

            if (expansionTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemExpanded(parentIndex - expandedCountBeforePosition);
            }
        }
    }

    /**
     * Collapses a specified parent item. Calls through to the
     * ExpandCollapseListener and adds children of the specified parent to the
     * total list of items.
     *
     * @param parentWrapper The ParentWrapper of the parent to collapse
     * @param parentIndex The index of the parent to collapse
     * @param collapseTriggeredByListItemClick true if expansion was triggered
     *                                         by a click event, false otherwise.
     */
    private void collapseParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean collapseTriggeredByListItemClick) {
        if (parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(false);

            List<?> childItemList = parentWrapper.getChildItemList();
            if (childItemList != null) {
                int childListItemCount = childItemList.size();
                for (int i = childListItemCount - 1; i >= 0; i--) {
                    mItemList.remove(parentIndex + i + 1);
                }

                notifyItemRangeRemoved(parentIndex + 1, childListItemCount);
            }

            if (collapseTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemCollapsed(parentIndex - expandedCountBeforePosition);
            }
        }
    }

    /**
     * Gets the number of expanded child list items before the specified position.
     *
     * @param position The index before which to return the number of expanded
     *                 child list items
     * @return The number of expanded child list items before the specified position
     */
    private int getExpandedItemCount(int position) {
        if (position == 0) {
            return 0;
        }

        int expandedCount = 0;
        for (int i = 0; i < position; i++) {
            Object listItem = getListItem(i);
            if (!(listItem instanceof ParentWrapper)) {
                expandedCount++;
            }
        }
        return expandedCount;
    }

    // endregion

    // region Data Manipulation

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has been newly inserted. The ParentListItem previously at {@code parentPosition} is now at
     * position {@code parentPosition + 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the newly inserted ParentListItem in the data set, relative
     *                       to list of ParentListItems only.
     *
     * @see #notifyParentItemRangeInserted(int, int)
     */
    public void notifyParentItemInserted(int parentPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);

        int wrapperIndex;
        if (parentPosition < mParentItemList.size() - 1) {
            wrapperIndex = getParentWrapperIndex(parentPosition);
        } else {
            wrapperIndex = mItemList.size();
        }

        int sizeChanged = addParentWrapper(wrapperIndex, parentListItem);
        notifyItemRangeInserted(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the currently reflected {@code itemCount}
     * ParentListItems starting at {@code parentPositionStart} have been newly inserted.
     * The ParentListItems previously located at {@code parentPositionStart} and beyond
     * can now be found starting at position {@code parentPositionStart + itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart Position of the first ParentListItem that was inserted, relative
     *                            to list of ParentListItems only.
     * @param itemCount Number of items inserted
     *
     * @see #notifyParentItemInserted(int)
     */
    public void notifyParentItemRangeInserted(int parentPositionStart, int itemCount) {
        int initialWrapperIndex;
        if (parentPositionStart < mParentItemList.size() - itemCount) {
            initialWrapperIndex = getParentWrapperIndex(parentPositionStart);
        } else {
            initialWrapperIndex = mItemList.size();
        }

        int sizeChanged = 0;
        int wrapperIndex = initialWrapperIndex;
        int changed;
        int parentPositionEnd = parentPositionStart + itemCount;
        for (int i = parentPositionStart; i < parentPositionEnd; i++) {
            ParentListItem parentListItem = mParentItemList.get(i);
            changed = addParentWrapper(wrapperIndex, parentListItem);
            wrapperIndex += changed;
            sizeChanged += changed;
        }

        notifyItemRangeInserted(initialWrapperIndex, sizeChanged);
    }

    private int addParentWrapper(int wrapperIndex, ParentListItem parentListItem) {
        int sizeChanged = 1;
        ParentWrapper parentWrapper = new ParentWrapper(parentListItem);
        mItemList.add(wrapperIndex, parentWrapper);
        if (parentWrapper.isInitiallyExpanded()) {
            parentWrapper.setExpanded(true);
            List<?> childItemList = parentWrapper.getChildItemList();
            mItemList.addAll(wrapperIndex + sizeChanged, childItemList);
            sizeChanged += childItemList.size();
        }
        return sizeChanged;
    }

    /**
     * Notify any registered observers that the ParentListItem previously located at {@code parentPosition}
     * has been removed from the data set. The ParentListItems previously located at and after
     * {@code parentPosition} may now be found at {@code oldPosition - 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem that has now been removed, relative
     *                       to list of ParentListItems only.
     */
    public void notifyParentItemRemoved(int parentPosition) {
        int wrapperIndex = getParentWrapperIndex(parentPosition);
        int sizeChanged = removeParentWrapper(wrapperIndex);

        notifyItemRangeRemoved(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the {@code itemCount} ParentListItems previously located
     * at {@code parentPositionStart} have been removed from the data set. The ParentListItems
     * previously located at and after {@code parentPositionStart + itemCount} may now be found at
     * {@code oldPosition - itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart The previous position of the first ParentListItem that was
     *                            removed, relative to list of ParentListItems only.
     * @param itemCount Number of ParentListItems removed from the data set
     */
    public void notifyParentItemRangeRemoved(int parentPositionStart, int itemCount) {
        int sizeChanged = 0;
        int wrapperIndex = getParentWrapperIndex(parentPositionStart);
        for (int i = 0; i < itemCount; i++) {
            sizeChanged += removeParentWrapper(wrapperIndex);
        }

        notifyItemRangeRemoved(wrapperIndex, sizeChanged);
    }

    private int removeParentWrapper(int parentWrapperIndex) {
        int sizeChanged = 1;
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.remove(parentWrapperIndex);
        if (parentWrapper.isExpanded()) {
            int childListSize = parentWrapper.getChildItemList().size();
            for (int i = 0; i < childListSize; i++) {
                mItemList.remove(parentWrapperIndex);
                sizeChanged++;
            }
        }
        return sizeChanged;
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has changed.
     * This will also trigger an item changed for children of the ParentList specified.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at {@code parentPosition} is out of date and should be updated.
     * The ParentListItem at {@code parentPosition} retains the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPosition Position of the item that has changed
     */
    public void notifyParentItemChanged(int parentPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int wrapperIndex = getParentWrapperIndex(parentPosition);
        int sizeChanged = changeParentWrapper(wrapperIndex, parentListItem);

        notifyItemRangeChanged(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the {@code itemCount} ParentListItems starting
     * at {@code parentPositionStart} have changed. This will also trigger an item changed
     * for children of the ParentList specified.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data in the given position range is out of date and should be updated.
     * The ParentListItems in the given range retain the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPositionStart Position of the item that has changed
     * @param itemCount Number of ParentListItems changed in the dataset
     */
    public void notifyParentItemRangeChanged(int parentPositionStart, int itemCount) {
        int initialWrapperIndex = getParentWrapperIndex(parentPositionStart);

        int wrapperIndex = initialWrapperIndex;
        int sizeChanged = 0;
        int changed;
        ParentListItem parentListItem;
        for (int j = 0; j < itemCount; j++) {
            parentListItem = mParentItemList.get(parentPositionStart);
            changed = changeParentWrapper(wrapperIndex, parentListItem);
            sizeChanged += changed;
            wrapperIndex += changed;
            parentPositionStart++;
        }
        notifyItemRangeChanged(initialWrapperIndex, sizeChanged);
    }

    private int changeParentWrapper(int wrapperIndex, ParentListItem parentListItem) {
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(wrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        int sizeChanged = 1;
        if (parentWrapper.isExpanded()) {
            List<?> childItems = parentWrapper.getChildItemList();
            int childListSize = childItems.size();
            Object child;
            for (int i = 0; i < childListSize; i++) {
                child = childItems.get(i);
                mItemList.set(wrapperIndex + i + 1, child);
                sizeChanged++;
            }
        }

        return sizeChanged;

    }

    /**
     * Notify any registered observers that the ParentListItem and it's child list items reflected at
     * {@code fromParentPosition} has been moved to {@code toParentPosition}.
     *
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     *
     * @param fromParentPosition Previous position of the ParentListItem, relative to list of
     *                           ParentListItems only.
     * @param toParentPosition New position of the ParentListItem, relative to list of
     *                         ParentListItems only.
     */
    public void notifyParentItemMoved(int fromParentPosition, int toParentPosition) {

        int fromWrapperIndex = getParentWrapperIndex(fromParentPosition);
        ParentWrapper fromParentWrapper = (ParentWrapper) mItemList.get(fromWrapperIndex);

        // If the parent is collapsed we can take advantage of notifyItemMoved otherwise
        // we are forced to do a "manual" move by removing and then adding the parent + children
        // (no notifyItemRangeMovedAvailable)
        boolean isCollapsed = !fromParentWrapper.isExpanded();
        boolean isExpandedNoChildren = !isCollapsed && (fromParentWrapper.getChildItemList().size() == 0);
        if (isCollapsed || isExpandedNoChildren) {
            int toWrapperIndex = getParentWrapperIndex(toParentPosition);
            ParentWrapper toParentWrapper = (ParentWrapper) mItemList.get(toWrapperIndex);
            mItemList.remove(fromWrapperIndex);
            int childOffset = 0;
            if (toParentWrapper.isExpanded()) {
                childOffset = toParentWrapper.getChildItemList().size();
            }
            mItemList.add(toWrapperIndex + childOffset, fromParentWrapper);

            notifyItemMoved(fromWrapperIndex, toWrapperIndex + childOffset);
        } else {
            // Remove the parent and children
            int sizeChanged = 0;
            int childListSize = fromParentWrapper.getChildItemList().size();
            for (int i = 0; i < childListSize + 1; i++) {
                mItemList.remove(fromWrapperIndex);
                sizeChanged++;
            }
            notifyItemRangeRemoved(fromWrapperIndex, sizeChanged);


            // Add the parent and children at new position
            int toWrapperIndex = getParentWrapperIndex(toParentPosition);
            int childOffset = 0;
            if (toWrapperIndex != -1) {
                ParentWrapper toParentWrapper = (ParentWrapper) mItemList.get(toWrapperIndex);
                if (toParentWrapper.isExpanded()) {
                    childOffset = toParentWrapper.getChildItemList().size();
                }
            } else {
                toWrapperIndex = mItemList.size();
            }
            mItemList.add(toWrapperIndex + childOffset, fromParentWrapper);
            List<?> childItemList = fromParentWrapper.getChildItemList();
            sizeChanged = childItemList.size() + 1;
            mItemList.addAll(toWrapperIndex + childOffset + 1, childItemList);
            notifyItemRangeInserted(toWrapperIndex + childOffset, sizeChanged);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has a child list item that has been newly inserted at {@code childPosition}.
     * The child list item previously at {@code childPosition} is now at
     * position {@code childPosition + 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has been added a child, relative
     *                       to list of ParentListItems only.
     * @param childPosition Position of the child object that has been inserted, relative to children
     *                      of the ParentListItem specified by {@code parentPosition} only.
     *
     */
    public void notifyChildItemInserted(int parentPosition, int childPosition) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            ParentListItem parentListItem = mParentItemList.get(parentPosition);
            Object child = parentListItem.getChildItemList().get(childPosition);
            mItemList.add(parentWrapperIndex + childPosition + 1, child);
            notifyItemInserted(parentWrapperIndex + childPosition + 1);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has {@code itemCount} child list items that have been newly inserted at {@code childPositionStart}.
     * The child list item previously at {@code childPositionStart} and beyond are now at
     * position {@code childPositionStart + itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has been added a child, relative
     *                       to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been inserted,
     *                           relative to children of the ParentListItem specified by
     *                           {@code parentPosition} only.
     * @param itemCount number of children inserted
     *
     */
    public void notifyChildItemRangeInserted(int parentPosition, int childPositionStart, int itemCount) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            ParentListItem parentListItem = mParentItemList.get(parentPosition);
            List<?> childList = parentListItem.getChildItemList();
            Object child;
            for (int i = 0; i < itemCount; i++) {
                child = childList.get(childPositionStart + i);
                mItemList.add(parentWrapperIndex + childPositionStart + i + 1, child);
            }
            notifyItemRangeInserted(parentWrapperIndex + childPositionStart + 1, itemCount);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem located at {@code parentPosition}
     * has a child list item that has been removed from the data set, previously located at {@code childPosition}.
     * The child list item previously located at and after {@code childPosition} may
     * now be found at {@code childPosition - 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has a child removed from, relative
     *                       to list of ParentListItems only.
     * @param childPosition Position of the child object that has been removed, relative to children
     *                      of the ParentListItem specified by {@code parentPosition} only.
     */
    public void notifyChildItemRemoved(int parentPosition, int childPosition) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            mItemList.remove(parentWrapperIndex + childPosition + 1);
            notifyItemRemoved(parentWrapperIndex + childPosition + 1);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem located at {@code parentPosition}
     * has {@code itemCount} child list items that have been removed from the data set, previously
     * located at {@code childPositionStart} onwards. The child list item previously located at and
     * after {@code childPositionStart} may now be found at {@code childPositionStart - itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has a child removed from, relative
     *                       to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been removed, relative
     *                           to children of the ParentListItem specified by
     *                           {@code parentPosition} only.
     * @param itemCount number of children removed
     */
    public void notifyChildItemRangeRemoved(int parentPosition, int childPositionStart, int itemCount) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            for (int i = 0; i < itemCount; i++) {
                mItemList.remove(parentWrapperIndex + childPositionStart + 1);
            }
            notifyItemRangeRemoved(parentWrapperIndex + childPositionStart + 1, itemCount);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has
     * a child located at {@code childPosition} that has changed.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at {@code childPosition} is out of date and should be updated.
     * The ParentListItem at {@code childPosition} retains the same identity.
     *
     * @param parentPosition Position of the ParentListItem who has a child that has changed
     * @param childPosition Position of the child that has changed
     */
    public void notifyChildItemChanged(int parentPosition, int childPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            int listChildPosition = parentWrapperIndex + childPosition + 1;
            Object child = parentWrapper.getChildItemList().get(childPosition);
            mItemList.set(listChildPosition, child);
            notifyItemChanged(listChildPosition);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has
     * {@code itemCount} child Objects starting at {@code childPositionStart} that have changed.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * The ParentListItem at {@code childPositionStart} retains the same identity.
     * reflection of the set of {@code itemCount} child objects starting at {@code childPositionStart}
     * are out of date and should be updated.
     *
     * @param parentPosition Position of the ParentListItem who has a child that has changed
     * @param childPositionStart Position of the first child object that has changed
     * @param itemCount number of child objects changed
     */
    public void notifyChildItemRangeChanged(int parentPosition, int childPositionStart, int itemCount) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            int listChildPosition = parentWrapperIndex + childPositionStart + 1;
            for (int i = 0; i < itemCount; i++) {
                Object child = parentWrapper.getChildItemList().get(childPositionStart + i);
                mItemList.set(listChildPosition + i, child);

            }
            notifyItemRangeChanged(listChildPosition, itemCount);
        }
    }

    /**
     * Notify any registered observers that the child list item contained within the ParentListItem
     * at {@code parentPosition} has moved from {@code fromChildPosition} to {@code toChildPosition}.
     *
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     *
     * @param parentPosition Position of the ParentListItem who has a child that has moved
     * @param fromChildPosition Previous position of the child list item
     * @param toChildPosition New position of the child list item
     */
    public void notifyChildItemMoved(int parentPosition, int fromChildPosition, int toChildPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            Object fromChild = mItemList.remove(parentWrapperIndex + 1 + fromChildPosition);
            mItemList.add(parentWrapperIndex + 1 + toChildPosition, fromChild);
            notifyItemMoved(parentWrapperIndex + 1 + fromChildPosition, parentWrapperIndex + 1 + toChildPosition);
        }
    }



    // endregion

    /**
     * Generates a HashMap used to store expanded state for items in the list
     * on configuration change or whenever onResume is called.
     *
     * @return A HashMap containing the expanded state of all parent list items
     */
    private HashMap<Integer, Boolean> generateExpandedStateMap() {
        HashMap<Integer, Boolean> parentListItemHashMap = new HashMap<>();
        int childCount = 0;

        Object listItem;
        ParentWrapper parentWrapper;
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            if (mItemList.get(i) != null) {
                listItem = getListItem(i);
                if (listItem instanceof ParentWrapper) {
                    parentWrapper = (ParentWrapper) listItem;
                    parentListItemHashMap.put(i - childCount, parentWrapper.isExpanded());
                } else {
                    childCount++;
                }
            }
        }

        return parentListItemHashMap;
    }

    /**
     * Gets the index of a ParentWrapper within the helper item list based on
     * the index of the ParentWrapper.
     *
     * @param parentIndex The index of the parent in the list of parent items
     * @return The index of the parent in the list of all views in the adapter
     */
    private int getParentWrapperIndex(int parentIndex) {
        int parentCount = 0;
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            if (mItemList.get(i) instanceof ParentWrapper) {
                parentCount++;

                if (parentCount > parentIndex) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Gets the ParentWrapper for a specified ParentListItem from the list of
     * parents.
     *
     * @param parentListItem A ParentListItem in the list of parents
     * @return If the parent exists on the list, returns its ParentWrapper.
     *         Otherwise, returns null.
     */
    private ParentWrapper getParentWrapper(ParentListItem parentListItem) {
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            Object listItem = mItemList.get(i);
            if (listItem instanceof ParentWrapper) {
                if (((ParentWrapper) listItem).getParentListItem().equals(parentListItem)) {
                    return (ParentWrapper) listItem;
                }
            }
        }

        return null;
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Carson on 2017-11-28.
 * <p>
 * Feel free to use code just give credit please :)
 */

public class ProductsParentViewHolder extends ParentViewHolder{

    public TextView mTitleTextView;
    public ImageView mIconView;
    public ImageView mParentDropDownArrow;

    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;

    public ProductsParentViewHolder(View itemView) {
        super(itemView);

        mTitleTextView = itemView.findViewById(R.id.name);
        mIconView = itemView.findViewById(R.id.image);
        mParentDropDownArrow = itemView.findViewById(R.id.parent_list_item_expand_arrow);
    }

    public void bind(@NonNull Products product) {

        //set the text of item
        mTitleTextView.setText(product.getName());

        //set item image
        Resources res = itemView.getContext().getResources();
        String uri = product.getImage();
        mIconView.setImageResource(res.getIdentifier(uri, "drawable", BuildConfig.APPLICATION_ID));
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);

        if (expanded) {
            mParentDropDownArrow.setRotation(ROTATED_POSITION);
        } else {
            mParentDropDownArrow.setRotation(INITIAL_POSITION);
        }

    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);

        RotateAnimation rotateAnimation;
        if (expanded) { // rotate clockwise
            rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                    INITIAL_POSITION,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        } else { // rotate counterclockwise
            rotateAnimation = new RotateAnimation(-1 * ROTATED_POSITION,
                    INITIAL_POSITION,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        }

        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        mParentDropDownArrow.startAnimation(rotateAnimation);

    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class FinishedActivity extends AppCompatActivity {

    NotificationHandler nHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);

        nHandler = NotificationHandler.getInstance(this);
        nHandler.createSimpleNotification(this);

        Button buttonFinish = findViewById(R.id.buttonFinish);

        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainHomePageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //click event to let the user to click anywhere to move on, there is nothing else to click.
        //might remove since we have a button click, still neat to know though.
        Intent intent = new Intent(getApplicationContext(), MainHomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        return true;
    }
}


package com.carsonskjerdal.app.groceryshop;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

/**
 * Created by Carson on 2017-11-22.
 *
 * RecyclerView adapter extended with project-specific required methods
 *
 * This may all be null and useless now after upgrading my adapter with the ExpandabaleRecyclerAdapter
 */

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartHolder> {

    private List<CartItems> cartList;
    private LayoutInflater mInflater;

    public CartItemAdapter(List<CartItems> list) {
        cartList = list;
        //mInflater = LayoutInflater.from(context);

    }

    /* ViewHolder for each cart item */
    public class CartHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        ImageView image;
        TextView price;
        Button button;


        CartHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            price = itemView.findViewById(R.id.price);
            button = itemView.findViewById(R.id.delete);
        }



        @Override
        public void onClick(View v) {
            Log.e("Adapter","On Click");
            switch(v.getId()){
                case R.id.delete:
                    Log.e("Adapter","Item Deleted");
                    break;
            }
        }
    }

    @Override
    public CartHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item_layout, parent, false);


        return new CartHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CartHolder holder, int position) {
        CartItems cartItem = cartList.get(position);

        //Sets Text
        holder.name.setText(cartItem.getName());
        holder.image.setImageResource(cartItem.getImage());
        String total = cartItem.getPrice() + " x " + cartItem.getQuantity();
        holder.price.setText(total);
    }


    @Override
    public int getItemCount() {
        return cartList.size();
    }


    public void updateList(List<CartItems> newList){
        cartList = newList;
        notifyDataSetChanged();
    }


}


package com.carsonskjerdal.app.groceryshop;


/**
 * Created by Carson on 2017-11-22.
 * <p>
 * Feel free to use code just give credit please :)
 */

public final class Groceries  {

    private String name;
    private String image;

    public Groceries(String name, String image){

        this.name = name;
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }



}


package com.carsonskjerdal.app.groceryshop;


import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Carson on 2017-12-05.
 * <p>
 * Code borrowd from another project
 */

public class ParentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ParentListItemExpandCollapseListener mParentListItemExpandCollapseListener;
    private boolean mExpanded;

    /**
     * implementations to be notified of expand/collapse state change events.
     */
    public interface ParentListItemExpandCollapseListener {

        /**
         * Called when a list item is expanded.
         *
         * @param position The index of the item in the list being expanded
         */
        void onParentListItemExpanded(int position);

        /**
         * Called when a list item is collapsed.
         *
         * @param position The index of the item in the list being collapsed
         */
        void onParentListItemCollapsed(int position);
    }

    /**
     * Default constructor.
     *
     * @param itemView The {@link View} being hosted in this ViewHolder
     */
    public ParentViewHolder(View itemView) {
        super(itemView);
        mExpanded = false;
    }

    /**
     * Sets a {@link android.view.View.OnClickListener} on the entire parent
     * view to trigger expansion.
     */
    public void setMainItemClickToExpand() {
        itemView.setOnClickListener(this);
    }

    /**
     * corresponding to this {@link ParentViewHolder}.
     *
     * @return true if expanded, false if not
     */
    public boolean isExpanded() {
        return mExpanded;
    }

    /**
     * Setter method for expanded state, used for initialization of expanded state.
     * changes to the state are given in {@link #onExpansionToggled(boolean)}
     *
     * @param expanded true if expanded, false if not
     */
    public void setExpanded(boolean expanded) {
        mExpanded = expanded;
    }

    /**
     * Callback triggered when expansion state is changed, but not during
     * initialization.
     * <p>
     * Useful for implementing animations on expansion.
     *
     * @param expanded true if view is expanded before expansion is toggled,
     *                 false if not
     */
    public void onExpansionToggled(boolean expanded) {

    }

    /**
     * Getter for the {@link ParentListItemExpandCollapseListener} implemented in
     *
     * @return The {@link ParentListItemExpandCollapseListener} set in the {@link ParentViewHolder}
     */
    public ParentListItemExpandCollapseListener getParentListItemExpandCollapseListener() {
        return mParentListItemExpandCollapseListener;
    }

    /**
     * Setter for the {@link ParentListItemExpandCollapseListener} implemented in
     *
     * @param parentListItemExpandCollapseListener The {@link ParentListItemExpandCollapseListener} to set on the {@link ParentViewHolder}
     */
    public void setParentListItemExpandCollapseListener(ParentListItemExpandCollapseListener parentListItemExpandCollapseListener) {
        mParentListItemExpandCollapseListener = parentListItemExpandCollapseListener;
    }

    /**
     * {@link android.view.View.OnClickListener} to listen for click events on
     * the entire parent {@link View}.
     * <p>
     * Only registered if {@link #shouldItemViewClickToggleExpansion()} is true.
     *
     * @param v The {@link View} that is the trigger for expansion
     */
    @Override
    public void onClick(View v) {
        if (mExpanded) {
            collapseView();
        } else {
            expandView();
        }
    }

    /**
     * Used to determine whether a click in the entire parent {@link View}
     * should trigger row expansion.
     * <p>
     * If you return false, you can call {@link #expandView()} to trigger an
     * expansion in response to a another event or {@link #collapseView()} to
     * trigger a collapse.
     *
     * @return true to set an {@link android.view.View.OnClickListener} on the item view
     */
    public boolean shouldItemViewClickToggleExpansion() {
        return true;
    }

    /**
     * Triggers expansion of the parent.
     */
    protected void expandView() {
        setExpanded(true);
        onExpansionToggled(false);

        if (mParentListItemExpandCollapseListener != null) {
            mParentListItemExpandCollapseListener.onParentListItemExpanded(getAdapterPosition());
        }
    }

    /**
     * Triggers collapse of the parent.
     */
    protected void collapseView() {
        setExpanded(false);
        onExpansionToggled(true);

        if (mParentListItemExpandCollapseListener != null) {
            mParentListItemExpandCollapseListener.onParentListItemCollapsed(getAdapterPosition());
        }
    }
}


