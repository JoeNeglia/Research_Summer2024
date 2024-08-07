package com.skocur.watchnotificationgenerator;

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
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.skocur.watchnotificationgenerator", appContext.getPackageName());
    }
}


package com.skocur.watchnotificationgenerator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
}

package com.skocur.watchnotificationgenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.skocur.watchnotificationgenerator.models.Notification;
import com.skocur.watchnotificationgenerator.utils.CustomAddAlertDialog;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class NotificationsActivity extends AppCompatActivity {

    private static final String NOTIFICATIONS_CHANNEL = "s0x";
    private static int NOTIFICATIONS_COUNTER = 0;

    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        createNotificationChannel();

        Intent intent = getIntent();
        categoryName = intent.getStringExtra("category_name");

        initiateViews();

        try {
            NotificationsListAdapter notificationsListAdapter = new NotificationsListAdapter(
                    HomeActivity.databaseService.getAllNotificationsFromCategory(categoryName)
            );

            RecyclerView recyclerView = findViewById(R.id.notifications_recycler_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(notificationsListAdapter);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    class NotificationsListAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

        private List<Notification> mNotifications;

        NotificationsListAdapter(List<Notification> notifications) {
            this.mNotifications = notifications;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layoutInflater = LayoutInflater.from(NotificationsActivity.this.getApplicationContext())
                    .inflate(R.layout.item_general, parent, false);
            return new NotificationViewHolder((ViewGroup) layoutInflater.findViewById(R.id.item_general_container));
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Notification notification = mNotifications.get(position);
            holder.setItemTitle(notification.getNotificationTitle());

            holder.setIconListener(notification);
        }

        @Override
        public int getItemCount() {
            return mNotifications.size();
        }
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mViewGroup;
        private TextView mItemTitle;
        private ImageView mNewNotification;

        public NotificationViewHolder(@NonNull ViewGroup container) {
            super(container);

            mViewGroup = container;
            mItemTitle = mViewGroup.findViewById(R.id.item_general_name);
            mNewNotification = mViewGroup.findViewById(R.id.item_general_create_notification);
            mNewNotification.setVisibility(View.VISIBLE);
        }

        void setIconListener(final Notification notification) {
            mNewNotification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    displayNotification(notification);
                }
            });
        }

        void setItemTitle(String title) {
            mItemTitle.setText(title);
        }

        /**
         * IMPORTANT NOTE
         * User has to add this app to "allowed applications' on watch settings.
         *
         * @param notification
         */
        private void displayNotification(Notification notification) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotificationsActivity.this.getApplicationContext(), NOTIFICATIONS_CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(notification.getNotificationTitle())
                    .setContentText(notification.notificationContent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true));

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(NotificationsActivity.this.getApplicationContext());
            notificationManager.notify(NOTIFICATIONS_COUNTER++, mBuilder.build());
        }
    }

    private void initiateViews() {
        findViewById(R.id.activity_notifications_fab_add_notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAddAlertDialog customAddAlertDialog = new CustomAddAlertDialog();
                customAddAlertDialog.alertFor(NotificationsActivity.this)
                        .setTitle("Add Notification")
                        .setPositiveButton(new CustomAddAlertDialog.InputReadyListener() {
                            @Override
                            public void onClick(EditText input) {
                                Intent intent = new Intent(NotificationsActivity.this, NewNotificationActivity.class);
                                intent.putExtra("notification_name", input.getText().toString());
                                intent.putExtra("category_name", categoryName);

                                startActivity(intent);
                            }
                        }).build();
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = NOTIFICATIONS_CHANNEL;
            String description = "Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATIONS_CHANNEL, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}


package com.skocur.watchnotificationgenerator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.skocur.watchnotificationgenerator.models.Category;
import com.skocur.watchnotificationgenerator.models.Notification;

import java.util.concurrent.ExecutionException;

public class NewNotificationActivity extends AppCompatActivity {

    private String notificationName;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_notification);

        notificationName = getIntent().getStringExtra("notification_name");
        categoryName = getIntent().getStringExtra("category_name");

        initiateViews();
    }

    private void initiateViews() {
        EditText etNotificationTitle = findViewById(R.id.activity_new_notification_edit_title);
        etNotificationTitle.setText(notificationName);

        final EditText etNotificationContent = findViewById(R.id.activity_new_notification_edit_content);

        findViewById(R.id.activity_new_notification_button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Notification notification = new Notification();
                notification.setNotificationTitle(notificationName);
                notification.setNotificationContent(etNotificationContent.getText().toString());

                try {
                    Category category = HomeActivity.databaseService.getCategoryForName(categoryName);
                    notification.setCategoryUid(category.getCategoryUid());

                    HomeActivity.databaseService.addNotification(notification);

                    finish();
                } catch (InterruptedException e) {
                    Log.e("!", e.toString());
                } catch (ExecutionException e) {
                    Log.e("!", e.toString());
                }
            }
        });
    }
}


package com.skocur.watchnotificationgenerator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.skocur.watchnotificationgenerator.models.Category;
import com.skocur.watchnotificationgenerator.sqlutils.DatabaseService;
import com.skocur.watchnotificationgenerator.utils.CustomAddAlertDialog;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeActivity extends AppCompatActivity {

    public static DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        databaseService = new DatabaseService(getApplicationContext());

        initiateViews();

        try {
            List<Category> categories = databaseService.getAllCategories();
            for (Category category : categories) {
                Log.i(">>>>>", category.getCategoryName());
            }
            CategoriesListAdapter categoriesListAdapter = new CategoriesListAdapter(
                    categories
            );

            RecyclerView recyclerView = findViewById(R.id.home_recycler_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setAdapter(categoriesListAdapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    class CategoriesListAdapter extends RecyclerView.Adapter<CategoryViewHolder> {

        private List<Category> mCategories;

        CategoriesListAdapter(List<Category> categories) {
            mCategories = categories;
        }

        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(HomeActivity.this.getApplicationContext())
                    .inflate(R.layout.item_general, parent, false);
            return new CategoryViewHolder((ViewGroup) view.findViewById(R.id.item_general_container));
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = mCategories.get(position);
            holder.setCategoryTitle(category.categoryName);
            holder.setCategoryListener(category);
        }

        @Override
        public int getItemCount() {
            return mCategories.size();
        }
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mContainer;
        private TextView mCategoryTitle;

        CategoryViewHolder(@NonNull ViewGroup itemView) {
            super(itemView);

            mContainer = itemView;
            mCategoryTitle = mContainer.findViewById(R.id.item_general_name);
        }

        void setCategoryListener(final Category category) {
            mContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = HomeActivity.this.getApplicationContext();
                    Intent intent = new Intent(context, NotificationsActivity.class);
                    intent.putExtra("category_name", category.getCategoryName());

                    context.startActivity(intent);
                }
            });
        }

        void setCategoryTitle(String title) {
            mCategoryTitle.setText(title);
        }
    }

    private void initiateViews() {
        findViewById(R.id.activity_home_fab_add_category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAddAlertDialog customAddAlertDialog = new CustomAddAlertDialog();
                customAddAlertDialog.alertFor(HomeActivity.this)
                        .setTitle("Add category")
                        .setPositiveButton(new CustomAddAlertDialog.InputReadyListener() {
                    @Override
                    public void onClick(EditText input) {
                        Category category = new Category();
                        category.setCategoryName(input.getText().toString());

                        databaseService.addCategory(category);
                    }
                }).build();
            }
        });
    }
}


package com.skocur.watchnotificationgenerator.sqlutils;

import com.skocur.watchnotificationgenerator.models.Category;
import com.skocur.watchnotificationgenerator.models.Notification;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Notification.class, Category.class}, version = 1)
public abstract class MainDatabase extends RoomDatabase {

    public abstract NotificationDao notificationDao();

    public abstract CategoryDao categoryDao();
}


package com.skocur.watchnotificationgenerator.sqlutils;

import android.content.Context;
import android.os.AsyncTask;

import com.skocur.watchnotificationgenerator.models.Category;
import com.skocur.watchnotificationgenerator.models.Notification;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.room.Room;

public class DatabaseService {

    private MainDatabase db;

    public DatabaseService(Context context){
        db = Room.databaseBuilder(context,
                MainDatabase.class, "watch-notifications-db").build();
    }

    public void addNotification(Notification notification) {
        new NotificationInserterAsyncTask().execute(notification);
    }

    public List<Notification> getAllNotifications() throws InterruptedException, ExecutionException {
        return new NotificationDownloaderAsyncTask().execute().get();
    }

    public List<Notification> getAllNotificationsFromCategory(String category) throws InterruptedException, ExecutionException {
        return new NotificationCategoryDownloaderAsyncTask().execute(category).get();
    }

    public void addCategory(Category category) {
        new CategoryInserterAsyncTask().execute(category);
    }

    public List<Category> getAllCategories() throws InterruptedException, ExecutionException {
        return new CategoryDownloaderAsyncTask().execute().get();
    }

    public Category getCategoryForName(String categoryName) throws InterruptedException, ExecutionException {
        return new CategoryForNameDownloaderAsyncTask().execute(categoryName).get();
    }

    private class NotificationInserterAsyncTask extends AsyncTask<Notification, Void, Void> {

        @Override
        protected Void doInBackground(Notification... notifications) {
            db.notificationDao().insertAll(notifications);
            return null;
        }
    }

    private class NotificationCategoryDownloaderAsyncTask extends AsyncTask<String, Void, List<Notification>> {

        @Override
        protected List<Notification> doInBackground(String... data) {
            return db.notificationDao().getAllFromCategory(data[0]);
        }
    }

    private class NotificationDownloaderAsyncTask extends AsyncTask<Void, Void, List<Notification>> {

        @Override
        protected List<Notification> doInBackground(Void... url) {
            return db.notificationDao().getAll();
        }
    }

    private class CategoryInserterAsyncTask extends AsyncTask<Category, Void, Void> {

        @Override
        protected Void doInBackground(Category... categories) {
            db.categoryDao().insertAll(categories);
            return null;
        }
    }

    private class CategoryDownloaderAsyncTask extends AsyncTask<Void, Void, List<Category>> {

        @Override
        protected List<Category> doInBackground(Void... url) {
            return db.categoryDao().getAllCategories();
        }
    }


    private class CategoryForNameDownloaderAsyncTask extends AsyncTask<String, Void, Category> {

        @Override
        protected Category doInBackground(String... data) {
            return db.categoryDao().getCategoryForName(data[0]);
        }
    }
}


package com.skocur.watchnotificationgenerator.sqlutils;

import com.skocur.watchnotificationgenerator.models.Notification;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface NotificationDao {

    @Query("SELECT * FROM notification")
    List<Notification> getAll();

    @Query("SELECT * FROM notification " +
            "INNER JOIN category ON notification.category_uid = category.categoryUid " +
            "WHERE category.category_name LIKE :category")
    List<Notification> getAllFromCategory(String category);

    @Insert
    void insertAll(Notification... notifications);

    @Delete
    void delete(Notification notification);
}


package com.skocur.watchnotificationgenerator.sqlutils;

import com.skocur.watchnotificationgenerator.models.Category;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM category")
    List<Category> getAllCategories();

    @Query("SELECT * FROM category " +
            "WHERE category_name LIKE :categoryName")
    Category getCategoryForName(String categoryName);

    @Insert
    void insertAll(Category... categories);

    @Delete
    void delete(Category category);
}


package com.skocur.watchnotificationgenerator.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

public class CustomAddAlertDialog {

    private String alertTitle;
    private Context context;
    private DialogInterface.OnClickListener onClickListener;
    private InputReadyListener inputReadyListener;

    public CustomAddAlertDialog alertFor(Context context) {
        this.context = context;

        return this;
    }

    public CustomAddAlertDialog setTitle(String alertTitle) {
        this.alertTitle = alertTitle;

        return this;
    }

    public CustomAddAlertDialog setPositiveButton(InputReadyListener inputReadyListener) {
        this.inputReadyListener = inputReadyListener;

        return this;
    }

    public void build() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(alertTitle);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30, 5, 30, 5);

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        container.addView(input, params);
        builder.setView(container);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                inputReadyListener.onClick(input);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    public interface InputReadyListener {

        void onClick(EditText input);
    }
}


package com.skocur.watchnotificationgenerator.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Notification {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name="notification_title")
    public String notificationTitle;

    @ColumnInfo(name = "notification_content")
    public String notificationContent;

    @ColumnInfo(name = "category_uid")
    public int categoryUid;

    public int getCategoryUid() {
        return categoryUid;
    }

    public void setCategoryUid(int categoryUid) {
        this.categoryUid = categoryUid;
    }

    //@Embedded
    //public Category category;

    /*public Category getCategory() {
        return category;
    }*/

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }

    /*public void setCategory(Category category) {
        this.category = category;
    }*/

    @NonNull
    @Override
    public String toString() {
        return getCategoryUid() + " "
                + getNotificationTitle() + " "
                + getNotificationContent();
    }
}


package com.skocur.watchnotificationgenerator.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Category {

    @PrimaryKey(autoGenerate = true)
    public int categoryUid;

    @ColumnInfo(name = "category_name")
    public String categoryName;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryUid() {
        return categoryUid;
    }

    @Override
    public String toString() {
        return categoryName;
    }
}


