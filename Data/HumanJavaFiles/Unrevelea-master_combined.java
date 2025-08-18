package com.example.unrevelea;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.unrevelea", appContext.getPackageName());
    }
}

package com.example.unrevelea;

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

package com.example.unrevelea;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHandler {

    String[] notificationData;
    Resources resources;
    Context context;

    public NotificationHandler(Resources appResources, Context appContext) {

        resources = appResources;
        context = appContext;

        notificationData = resources.getStringArray(R.array.notification_data);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    notificationData[0],
                    notificationData[1],
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            channel.setDescription(notificationData[2]);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void createPostNotification(String secretTitle) {
        String toNotify = String.format(notificationData[3], secretTitle);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationData[0])
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationData[1])
                .setContentText(toNotify)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(toNotify));
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());
    }

    public void createNewUserNotification(long userId) {
        String toNotify = String.format(notificationData[4], userId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationData[0])
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationData[1])
                .setContentText(toNotify);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());
    }

    public void createNUErrorNotification() {
        String toNotify = notificationData[5];

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationData[0])
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationData[1])
                .setContentText(toNotify);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());
    }
}


package com.example.unrevelea;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DBInstance {

    private final FirebaseFirestore db;
    private final ArrayList<QueryDocumentSnapshot> arraySecrets;
    private final ArrayList<String> idSecrets;
    private final SecretAdapter secretAdapter;
    private final NotificationHandler notifications;
    private final String userRawId;
    private final long USER_ID;

    DBInstance(SecretAdapter adapter, ArrayList<QueryDocumentSnapshot> secrets, NotificationHandler notificationHandler) {
        db = FirebaseFirestore.getInstance();
        userRawId = FirebaseAuth.getInstance().getUid();
        USER_ID = handleUserId();
        idSecrets = new ArrayList<>();
        arraySecrets = secrets;
        secretAdapter = adapter;
        notifications = notificationHandler;

        updateSecrets();
    }

    private long handleUserId() {
        boolean userExist = checkUserInDB(userRawId);
        long newId;

        Log.i("handleUserId", String.valueOf(userExist));

        if (userExist) {
            newId = getUserId(userRawId);
        }
        else {
            long lastId = getLastId(); newId = ++lastId;

            setLastId(newId);
            createNewUser(userRawId, newId);
        }

        return newId;
    }

    public void updateSecrets() {
        db.waitForPendingWrites();
        db.collection("secrets")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (idSecrets.contains(document.getId())) {continue;}
                            arraySecrets.add(0, document);
                            idSecrets.add(document.getId());
                        }
                        secretAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(task -> Log.e("getSecrets Failed", task.getMessage()));
    }

    public void postSecret(View view) {
        RelativeLayout parent = (RelativeLayout) view.getParent(),
                grandParent = (RelativeLayout) parent.getParent();
        EditText title = parent.findViewById(R.id.create_title),
                content = parent.findViewById(R.id.create_content);
        Map<String, Object> newData = new HashMap<>();

        parent.setEnabled(false);

        newData.put("user", USER_ID);
        newData.put("date", LocalDateTime.now());
        newData.put("title", title.getText().toString());
        newData.put("content", content.getText().toString());

        db.waitForPendingWrites();
        db.collection("secrets")
                .add(newData)
                .addOnSuccessListener(documentReference -> notifications.createPostNotification(Objects.requireNonNull(newData.get("title")).toString()))
                .addOnFailureListener(e -> Log.w("createSecret Failed", "Error adding Document", e));

        grandParent.removeView(parent);

        updateSecrets();
    }

    private boolean checkUserInDB(String userId) {
        final boolean[] result = new boolean[1];
        db.collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    result[0] = task.getResult().exists();
                    Log.i("checkUserInDB 1", String.valueOf(result[0]));
                })
                .addOnFailureListener(e -> {
                    Log.e("checkUserInDB Failed", "Error checking user", e);
                    result[0] = false;
                });
        Log.i("checkUserInDB 2", String.valueOf(result[0]));
        return result[0];
    }

    private long getUserId(String user) {
        final long[] userId = new long[1];

        db.collection("users")
                .document(user)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> data = task.getResult().getData();
                        userId[0] = (int) Objects.requireNonNull(data).get("id");
                    }
                })
                .addOnFailureListener(e -> Log.e("getUserId Failed", "Error getting the user Id existed in DB", e));

        return userId[0];
    }

    @SuppressLint("DefaultLocale")
    private void createNewUser(String userRawId, long newId) {
        Map<String, Object> newUserData = new HashMap<>();
        newUserData.put("id", newId);

        db.waitForPendingWrites();
        db.collection("users")
                .document(userRawId)
                .set(newUserData)
                .addOnSuccessListener(unused -> notifications.createNewUserNotification(newId))
                .addOnFailureListener(e -> {
                    notifications.createNUErrorNotification();
                    Log.e("createNewUser Failed", "Error while creating new user", e);
                });
    }

    private long getLastId() {
        final long[] test = {0};

        db.waitForPendingWrites();
        db.collection("users")
                .document("last_id")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> data = task.getResult().getData();
                        test[0] = (long) Objects.requireNonNull(data).get("id");
                    }
                })
                .addOnFailureListener(e -> Log.e("getLastId Failed", "Error getting last_id document", e));

        return test[0];
    }

    private void setLastId(long newLastId) {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("id", newLastId);

        db.collection("users")
                .document("last_id")
                .update(updatedData)
                .addOnSuccessListener(unused -> Log.i("setLastId", "New last_id was set"))
                .addOnFailureListener(e -> Log.e("setLastId Failed", "Couldn't connect or update de DB"));
    }
}


package com.example.unrevelea;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DBInstance db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
                new FirebaseAuthUIActivityResultContract(),
                this::onSignInResult
        );

        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();

        signInLauncher.launch(signInIntent);

        Resources resources = getResources();
        Context context = this.getApplicationContext();
        NotificationHandler notificationHandler = new NotificationHandler(resources, context);

        ArrayList<QueryDocumentSnapshot> arraySecrets = new ArrayList<>();
        RecyclerView secrets = findViewById(R.id.secrets);
        SecretAdapter secretAdapter = new SecretAdapter(arraySecrets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.secrets_swipe);
        db = new DBInstance(secretAdapter, arraySecrets, notificationHandler);

        secrets.setLayoutManager(layoutManager);
        secrets.setHasFixedSize(true);
        secrets.setAdapter(secretAdapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            db.updateSecrets();
            swipeRefreshLayout.setRefreshing(false);
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Log.i("onSignInResult Successful", Objects.requireNonNull(result.getIdpResponse()).toString());
        } else {Log.i("onSignInResult Failed", result.getResultCode().toString());}
    }

    public void displayCreate(View view) {
        RelativeLayout parent = (RelativeLayout) view.getParent();
        Context parentContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(parentContext);

        View createSecretPanel = inflater.inflate(R.layout.create_secret, null, true);
        parent.addView(createSecretPanel);
    }

    public void postSecret(View view) {db.postSecret(view);}
}

package com.example.unrevelea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class SecretAdapter extends RecyclerView.Adapter<SecretAdapter.SecretViewHolder> {

    private final ArrayList<QueryDocumentSnapshot> secrets;

    public SecretAdapter(ArrayList<QueryDocumentSnapshot> arraySecrets) {
        secrets = arraySecrets;
    }

    @NonNull
    @Override
    public SecretViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int secretModel = R.layout.secret;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachedParent = false;

        View view = inflater.inflate(secretModel, parent, attachedParent);
        return new SecretViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SecretViewHolder holder, int position) {
        holder.bind(secrets.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return secrets.size();
    }

    static class SecretViewHolder extends RecyclerView.ViewHolder {
        TextView user, date, title, content;
        String dUser, dDate, dTitle, dContent;
        Map<String, Object> rawDate;

        public SecretViewHolder(View view) {
            super(view);
            user = view.findViewById(R.id.secret_user);
            date = view.findViewById(R.id.secret_date);
            title = view.findViewById(R.id.secret_title);
            content = view.findViewById(R.id.secret_content);
        }

        @SuppressLint("DefaultLocale")
        void bind(Map<String, Object> secret) {
            rawDate = (Map<String, Object>) secret.get("date");
            dUser = String.format("%07d", Integer.valueOf(Objects.requireNonNull(secret.get("user")).toString()));
            dDate = String.format("%1$02d/%2$02d/%3$d", (long) rawDate.get("dayOfMonth"), (long) rawDate.get("monthValue"), (long) rawDate.get("year"));
            dTitle = Objects.requireNonNull(secret.get("title")).toString();
            dContent = Objects.requireNonNull(secret.get("content")).toString();

            user.setText(dUser);
            date.setText(dDate);
            title.setText(dTitle);
            content.setText(dContent);
        }
    }
}

