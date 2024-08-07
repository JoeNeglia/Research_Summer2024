package com.aaars.b;

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

        assertEquals("com.aaars.b", appContext.getPackageName());
    }
}


package com.aaars.b;

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

package com.aaars.b;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static com.aaars.b.Root.USER_ID;

public class TriggerReceiver extends BroadcastReceiver {
    Module md2;

    @Override
    public void onReceive(final Context context, Intent intent) {
        /*
        MediaPlayer mpintro = MediaPlayer.create(context, Uri.parse(Environment.getExternalStorageDirectory().getPath()+ "/Alarms/pirate.mp3"));
        mpintro.setLooping(true);
        mpintro.start();
        */
        md2 = new Module();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference drmd2 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("2");
        ValueEventListener wifiTimerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md2 = dataSnapshot.getValue(Module.class);
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(System.currentTimeMillis());

                    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                    int weekDay = 0;

                    if (Calendar.MONDAY == dayOfWeek) weekDay = 0;
                    else if (Calendar.TUESDAY == dayOfWeek) weekDay = 1;
                    else if (Calendar.WEDNESDAY == dayOfWeek) weekDay = 2;
                    else if (Calendar.THURSDAY == dayOfWeek) weekDay = 3;
                    else if (Calendar.FRIDAY == dayOfWeek) weekDay = 4;
                    else if (Calendar.SATURDAY == dayOfWeek) weekDay = 5;
                    else if (Calendar.SUNDAY == dayOfWeek) weekDay = 6;

                    if(md2.parameters.get(weekDay).equals("true")) {
                        try {
                            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            if(wifiManager != null) {
                                wifiManager.setWifiEnabled(false);
                                callNotification("A Module Ran - WiFi Turned OFF","WiFi was turned OFF!", context);
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(context, "Module ran, Restart app to sync", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd2.addValueEventListener(wifiTimerListener);
    }

    public void callNotification(String title, String text, Context context) {
        Intent intent = new Intent(context, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default")
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setSmallIcon(R.drawable.alpha)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, mBuilder.build());
    }

}


package com.aaars.b;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Fragments.AboutFragment;
import com.aaars.b.Fragments.HelpFragment;
import com.aaars.b.UserData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

public class Root extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button logout;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    GoogleSignInAccount user;
    private GoogleSignInClient mGoogleSignInClient;

    public static String USER_ID;
    private String TAG = "TAG";
    private boolean fromChild = false;

    FrameLayout mFrame;
    BottomNavigationView mNav;
    ActionBarDrawerToggle toggle;

    private DiscoverFragment discoverFragment;
    private UserFragment userFragment;
    private ModuleFragment moduleFragment;
    private HelpFragment helpFragment;
    private AboutFragment aboutFragment;

    public UserData userData, intentData;
    DatabaseReference dr, dr2;
    public LastRun lr;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_discover:
                setFragment(discoverFragment);
                break;
            case R.id.nav_modules:
                setFragment(moduleFragment);
                break;
            case R.id.nav_profile:
                setFragment(userFragment);
                break;
            case R.id.help:
                setFragment(helpFragment);
                break;
            case R.id.about:
                setFragment(aboutFragment);
                break;
            case R.id.restart:
                Intent i = new Intent(getApplicationContext(),Splash.class);
                startActivity(i);
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.exit:
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.cancelAll();
                finish();
                break;
            default:
                return false;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        call();
    }

    void call() {
        Intent i = new Intent(Root.this, Triggers.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        final Intent intent = getIntent();
        intentData = new UserData();
        lr = new LastRun();
        USER_ID = intent.getStringExtra("USER_ID");
        intentData.email = intent.getStringExtra("EMAIL");
        intentData.name = intent.getStringExtra("NAME");
        intentData.photo = intent.getStringExtra("PHOTO");

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            USER_ID = account.getId();
        }

        if(USER_ID == null) {
            Intent i = new Intent(this, Onboarding.class);
            startActivity(i);
        }

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        dr = database.getInstance().getReference().child("users").child(USER_ID);

        ValueEventListener postListener = new ValueEventListener() {
        @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    userData = dataSnapshot.getValue(UserData.class);
                    updateUI(userData);
                } else {
                    FirebaseDatabase.getInstance().getReference("users").child(USER_ID).setValue(intentData);
                    userData = intentData;
                    updateUI(userData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        dr.addValueEventListener(postListener);

        dr2 = database.getInstance().getReference().child("users").child(USER_ID).child("lastrun");
        ValueEventListener lastr = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    lr = dataSnapshot.getValue(LastRun.class);
                }
                else {
                    dr2.setValue(lr);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr2.addValueEventListener(lastr);

        //*************************************//

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavView = findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        TextView username = mNavView.getHeaderView(0).findViewById(R.id.username);
        TextView useremail = mNavView.getHeaderView(0).findViewById(R.id.useremail);
        username.setText(intentData.name);
        useremail.setText(intentData.email);

        mFrame = (FrameLayout) findViewById(R.id.frame);
        mNav = (BottomNavigationView) findViewById(R.id.navigation);

        discoverFragment = new DiscoverFragment();
        moduleFragment = new ModuleFragment();
        userFragment = new UserFragment();
        helpFragment = new HelpFragment();
        aboutFragment = new AboutFragment();

        setFragment(discoverFragment);
        mNav.setSelectedItemId(R.id.nav_discover);

        fromChild = intent.getBooleanExtra("fromChild", false);
        if(fromChild) {
            int pos = intent.getIntExtra("pos",1);
            switch(pos) {
                case R.id.nav_modules:
                    setFragment(moduleFragment);
                    break;
                case R.id.nav_discover:
                    setFragment(discoverFragment);
                    break;
                case R.id.nav_profile:
                    setFragment(userFragment);
                    break;
            }
        } else {
            setFragment(discoverFragment);
            mNav.setSelectedItemId(R.id.nav_discover);
        }

        mNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                switch(item.getItemId()) {
                    case R.id.nav_modules:
                        setFragment(moduleFragment);
                        return true;
                    case R.id.nav_discover:
                        setFragment(discoverFragment);
                        return true;
                    case R.id.nav_profile:
                        setFragment(userFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void updateUI(UserData userData){
        NavigationView mNavView = findViewById(R.id.nav_view);

        TextView username = mNavView.getHeaderView(0).findViewById(R.id.username);
        TextView useremail = mNavView.getHeaderView(0).findViewById(R.id.useremail);
        username.setText(userData.name);
        useremail.setText(userData.email);
    }

    public void logout() {
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(Root.this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(Root.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent homeIntent = new Intent(Root.this, Onboarding.class);
                        startActivity(homeIntent);
                        Toast.makeText(Root.this,"Logged Out!",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


package com.aaars.b;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.signin.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Demonstrates retrieving an ID token for the current Google user.
 */
public class Home extends AppCompatActivity {

    private static final String TAG = "IdTokenActivity";
    private static final int RC_GET_TOKEN = 9002;

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mIdTokenTextView;
    private Button mRefreshButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // For sample only: make sure there is a valid server client ID.
        validateServerClientID();

        // [START configure_signin]
        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("799925587615-3e2fceaf4qrb3dca85kktihaeo3pi7ls.apps.googleusercontent.com")
                .requestEmail()
                .build();
        // [END configure_signin]

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton mGoogleSignIn = (SignInButton) findViewById(R.id.gLogin);
        mGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIdToken();
            }
        });

    }

    private void getIdToken() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    private void refreshIdToken() {
        // Attempt to silently refresh the GoogleSignInAccount. If the GoogleSignInAccount
        // already has a valid token this method may complete immediately.
        //
        // If the user has not previously signed in on this device or the sign-in has expired,
        // this asynchronous branch will attempt to sign in the user silently and get a valid
        // ID token. Cross-device single sign on will occur in this branch.
        mGoogleSignInClient.silentSignIn()
                .addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        handleSignInResult(task);
                    }
                });
    }

    // [START handle_sign_in_result]
    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if(account == null) {
                Toast.makeText(this, "yes", Toast.LENGTH_LONG).show();
            }

            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(null);
        }
    }
    // [END handle_sign_in_result]

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_TOKEN) {
            // [START get_id_token]
            // This task is always completed immediately, there is no need to attach an
            // asynchronous listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            // [END get_id_token]
        }
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            Toast.makeText(this, "yes", Toast.LENGTH_LONG).show();
            String idToken = account.getIdToken();
        } else {
            //Toast.makeText(this, "no", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Validates that there is a reasonable server client ID in strings.xml, this is only needed
     * to make sure users of this sample follow the README.
     */
    private void validateServerClientID() {
        String serverClientId = "799925587615-3e2fceaf4qrb3dca85kktihaeo3pi7ls.apps.googleusercontent.com";
        String suffix = ".apps.googleusercontent.com";
        if (!serverClientId.trim().endsWith(suffix)) {
            String message = "Invalid server client ID in strings.xml, must end with " + suffix;

            Log.w(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}

package com.aaars.b;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.aaars.b.Root.USER_ID;

public class HelpSMS extends AppCompatActivity {
    SmsManager smsManager, smsManagerbeta, smsManagergamma;
    LocationManager locationManager;
    Module md6;
    DatabaseReference drmd6;
    Boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_sms);

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //5 HELP - MODULE 10
        drmd6 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("10");
        ValueEventListener helpListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md6 = dataSnapshot.getValue(Module.class);
                    if(flag)
                        call(md6.parameters.get(0),md6.parameters.get(1),md6.parameters.get(2));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd6.addValueEventListener(helpListener);

    }

    void call(String a, String b, String c) {
        try {
            flag = false;
            smsManager = SmsManager.getDefault();

            final LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(final Location location) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                    ref.child("10").child("currentLocation").setValue(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };

            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION))
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15,
                        50, locationListener);

            Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String content = "I am here: https://www.google.com/maps/search/?api=1&query=" + lc.getLatitude() + "," + lc.getLongitude() + " - Sent via Automator";
            if (a.length() == 10)
                smsManager.sendTextMessage(a, null, content, null, null);
            if (b.length() == 10)
                smsManager.sendTextMessage(b, null, content, null, null);
            if (c.length() == 10)
                smsManager.sendTextMessage(c, null, content, null, null);
            finish();
            System.exit(0);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }
}


package com.aaars.b;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.aaars.b.Root.USER_ID;

public class ModuleFragment extends Fragment {

    private List<ModuleData> data;
    private Map<String, Module> md;
    private RecyclerView rv;
    Boolean[] b;
    int i = 0;

    public ModuleFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_module, container, false);
        RecyclerView rv = view.findViewById(R.id.rv);

        b = new Boolean[100];

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();

        RVAdapter adapter = new RVAdapter(data);
        rv.setAdapter(adapter);

        return view;
    }

    private void initializeData(){
        data = new ArrayList<>();


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(listener);

        try {
            data = new ArrayList<>();
            data.add(new ModuleData("Device Security", "Set volume to min through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 9));
            data.add(new ModuleData("Safety", "Send SMS with device location when battery is critically low", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.orange), 1));
            data.add(new ModuleData("Wifi", "Turn Wi-Fi OFF at specific time to save battery life", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 2));
            data.add(new ModuleData("Device Security", "Turns OFF Wi-Fi through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 7));
            data.add(new ModuleData("Wifi", "Turn Wi-Fi ON at specific time to connect to the Internet", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 3));
            data.add(new ModuleData("Location", "Log time spent at specific location", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.yellove), 4));
            data.add(new ModuleData("Device Security", "Turns On Wi-Fi through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 6));
            data.add(new ModuleData("Email", "Sends you Quote of the Day to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 5));
            data.add(new ModuleData("Safety", "Sends location to selected contacts on notification tap", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.orange), 10));
            data.add(new ModuleData("Device Security", "Recover lost device through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 15));
            data.add(new ModuleData("Email", "Sends you a comic snippet to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 11));
            data.add(new ModuleData("Device Security", "Set volume to max through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 8));
            data.add(new ModuleData("Email", "Sends bundled RSS feed to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 13));
            data.add(new ModuleData("Device Security", "Locate your phone through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 12));
            data.add(new ModuleData("Wifi", "Turn Wi-Fi ON at specific location for seamless switch", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 14));
        }
        catch(Exception e) {

        }

        }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.DataViewHolder>{

        List<ModuleData> data;
        RVAdapter(List<ModuleData> persons){
            this.data = persons;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_discover, viewGroup, false);
            DataViewHolder pvh = new DataViewHolder(v, viewGroup.getContext());
            return pvh;
        }

        @Override
        public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
            try {
                dataViewHolder.header.setText(data.get(i).header);
                dataViewHolder.desc.setText(data.get(i).desc);
                dataViewHolder.img.setImageResource(data.get(i).img);
                dataViewHolder.cv.setCardBackgroundColor(data.get(i).clr);
            }
            catch (Exception e){}
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }



        public class DataViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView header;
            TextView desc;
            ImageView img;

            DataViewHolder(View itemView, final Context context) {
                super(itemView);
                cv = itemView.findViewById(R.id.cv);
                header = itemView.findViewById(R.id.header);
                desc = itemView.findViewById(R.id.desc);
                img = itemView.findViewById(R.id.img);

                cv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(getContext(),ModuleDesign.class);
                        i.putExtra("MODULE_ID",data.get(getAdapterPosition()).moduleid);
                        i.putExtra("USER_ID",Root.USER_ID);
                        startActivity(i);
                    }
                });
            }
        }
    }
}

class ModuleData {
    String header;
    String desc;
    int img;
    int clr;
    int moduleid;

    ModuleData(String header, String desc, int img, int clr, int moduleid) {
        this.header = header;
        this.desc = desc;
        this.img = img;
        this.clr = clr;
        this.moduleid = moduleid;


    }
}


package com.aaars.b;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.aaars.b.Root.USER_ID;

public class DiscoverFragment extends Fragment {

    private List<Data> data;
    private RecyclerView rv;

    public DiscoverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        RecyclerView rv = view.findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();

        RVAdapter adapter = new RVAdapter(data);
        rv.setAdapter(adapter);

        return view;
    }

    private void initializeData(){
        try {
            data = new ArrayList<>();
            data.add(new Data("Device Security", "Set volume to min through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 9));
            data.add(new Data("Safety", "Send SMS with device location when battery is critically low", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.orange), 1));
            data.add(new Data("Wifi", "Turn Wi-Fi OFF at specific time to save battery life", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 2));
            data.add(new Data("Device Security", "Turns OFF Wi-Fi through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 7));
            data.add(new Data("Wifi", "Turn Wi-Fi ON at specific time to connect to the Internet", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 3));
            data.add(new Data("Location", "Log time spent at specific location", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.yellove), 4));
            data.add(new Data("Device Security", "Turns On Wi-Fi through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 6));
            data.add(new Data("Email", "Sends you Quote of the Day to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 5));
            data.add(new Data("Safety", "Sends location to selected contacts on notification tap", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.orange), 10));
            data.add(new Data("Device Security", "Recover lost device through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 15));
            data.add(new Data("Email", "Sends you a comic snippet to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 11));
            data.add(new Data("Device Security", "Set volume to max through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 8));
            data.add(new Data("Email", "Sends bundled RSS feed to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 13));
            data.add(new Data("Device Security", "Locate your phone through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 12));
            data.add(new Data("Wifi", "Turn Wi-Fi ON at specific location for seamless switch", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 14));
        }
        catch(Exception e) {}
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.DataViewHolder>{

        List<Data> data;
        RVAdapter(List<Data> persons){
            this.data = persons;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_discover, viewGroup, false);
            DataViewHolder pvh = new DataViewHolder(v, viewGroup.getContext());
            return pvh;
        }

        @Override
        public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
            try {
                dataViewHolder.header.setText(data.get(i).header);
                dataViewHolder.desc.setText(data.get(i).desc);
                dataViewHolder.img.setImageResource(data.get(i).img);
                dataViewHolder.cv.setCardBackgroundColor(data.get(i).clr);
                dataViewHolder.footer.setVisibility(View.GONE);
            }
            catch(Exception e) {}
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }



        public class DataViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView header;
            TextView desc;
            ImageView img;
            TextView footer;

            DataViewHolder(View itemView, final Context context) {
                super(itemView);
                cv = itemView.findViewById(R.id.cv);
                header = itemView.findViewById(R.id.header);
                desc = itemView.findViewById(R.id.desc);
                img = itemView.findViewById(R.id.img);
                footer = itemView.findViewById(R.id.footer);

                cv.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                    Intent i = new Intent(getContext(),ModuleDesign.class);
                    i.putExtra("MODULE_ID",data.get(getAdapterPosition()).moduleid);
                    i.putExtra("USER_ID",Root.USER_ID);
                    startActivity(i);
                    }
                });
            }
        }
    }
}

class Data {
    String header;
    String desc;
    int img;
    int clr;
    int moduleid;

    Data(String header, String desc, int img, int clr, int moduleid) {
        this.header = header;
        this.desc = desc;
        this.img = img;
        this.clr = clr;
        this.moduleid = moduleid;
    }
}


package com.aaars.b;

import java.util.*;

public class Module {
    public int activityid;
    public int triggerid;
    public Boolean enabled;
    public ArrayList<String> parameters;


    public Module() {
        activityid = -1;
        triggerid = -1;
        enabled = false;
        parameters = new ArrayList<>();
    }

    public void onStart() {
        if(enabled) {
            switch(activityid) {
                case 1:
                    break;
                case -1:
                    return;
            }

            switch(triggerid) {
                case 1:
                    break;
                case -1:
                    return;
            }
        }
    }
}


package com.aaars.b;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static com.aaars.b.Root.USER_ID;

public class TriggerOnReceiver extends BroadcastReceiver {
    Module md2;

    @Override
    public void onReceive(final Context context, Intent intent) {

        try {
            md2 = new Module();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference drmd2 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("3");
            ValueEventListener wifiTimerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        md2 = dataSnapshot.getValue(Module.class);
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(System.currentTimeMillis());

                        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                        int weekDay = 0;

                        if (Calendar.MONDAY == dayOfWeek) weekDay = 0;
                        else if (Calendar.TUESDAY == dayOfWeek) weekDay = 1;
                        else if (Calendar.WEDNESDAY == dayOfWeek) weekDay = 2;
                        else if (Calendar.THURSDAY == dayOfWeek) weekDay = 3;
                        else if (Calendar.FRIDAY == dayOfWeek) weekDay = 4;
                        else if (Calendar.SATURDAY == dayOfWeek) weekDay = 5;
                        else if (Calendar.SUNDAY == dayOfWeek) weekDay = 6;


                        if (md2.parameters.get(weekDay).equals("true")) {
                            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            if (wifiManager != null) {
                                wifiManager.setWifiEnabled(true);
                                callNotification("A Module Ran - WiFi Turned ON", "WiFi was turned ON!", context);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to load post.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            drmd2.addValueEventListener(wifiTimerListener);
        }
        catch(Exception e) {
            Toast.makeText(context, "Module ran, Restart app to sync", Toast.LENGTH_LONG).show();
        }
    }

    public void callNotification(String title, String text, Context context) {
        Intent intent = new Intent(context, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default")
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setSmallIcon(R.drawable.alpha)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, mBuilder.build());
    }
}


package com.aaars.b;

public interface MessageListener {
    void messageReceived(String message);
}


package com.aaars.b;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.UserData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Target;
import java.util.List;

public class EmergencyTimer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button logout;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    GoogleSignInAccount user;
    private GoogleSignInClient mGoogleSignInClient;

    public static String USER_ID;
    private String TAG = "TAG";
    private boolean fromChild = false;

    FrameLayout mFrame;
    BottomNavigationView mNav;
    ActionBarDrawerToggle toggle;

    private DiscoverFragment discoverFragment;
    private UserFragment userFragment;
    private ModuleFragment moduleFragment;

    public UserData userData, intentData;
    DatabaseReference dr;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_discover:
                setFragment(discoverFragment);
                break;
            case R.id.nav_modules:
                setFragment(moduleFragment);
                break;
            case R.id.nav_profile:
                setFragment(userFragment);
                break;
            case R.id.help:
                break;
            case R.id.about:
                break;
            case R.id.restart:
                Intent i = new Intent(getApplicationContext(),Splash.class);
                startActivity(i);
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.exit:
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.cancelAll();
                finish();
                break;
            default:
                return false;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        call();
    }

    void call() {
        Intent i = new Intent(this, Triggers.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(i);
        } else {
            startService(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        final Intent intent = getIntent();
        intentData = new UserData();
        USER_ID = intent.getStringExtra("USER_ID");
        intentData.email = intent.getStringExtra("EMAIL");
        intentData.name = intent.getStringExtra("NAME");
        intentData.photo = intent.getStringExtra("PHOTO");


        if(USER_ID == null) {
            Intent i = new Intent(this, Onboarding.class);
            startActivity(i);
        }
        Toast.makeText(getApplicationContext(),intentData.email,Toast.LENGTH_SHORT).show();

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        //ref.child(USER_ID).setValue(intentData);
        //ref.child(USER_ID).child("email").setValue("thecygnusalpha@gmail.com");

        dr = database.getInstance().getReference().child("users").child(USER_ID);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    userData = dataSnapshot.getValue(UserData.class);
                    updateUI(userData);
                } else {
                    FirebaseDatabase.getInstance().getReference("users").child(USER_ID).setValue(intentData);
                    userData = intentData;
                    updateUI(userData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        dr.addValueEventListener(postListener);


        //*************************************//

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavView = findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        TextView username = mNavView.getHeaderView(0).findViewById(R.id.username);
        TextView useremail = mNavView.getHeaderView(0).findViewById(R.id.useremail);
        username.setText(intentData.name);
        useremail.setText(intentData.email);

        mFrame = (FrameLayout) findViewById(R.id.frame);
        mNav = (BottomNavigationView) findViewById(R.id.navigation);

        discoverFragment = new DiscoverFragment();
        moduleFragment = new ModuleFragment();
        userFragment = new UserFragment();

        setFragment(discoverFragment);
        mNav.setSelectedItemId(R.id.nav_discover);

        fromChild = intent.getBooleanExtra("fromChild", false);
        if(fromChild) {
            int pos = intent.getIntExtra("pos",1);
            switch(pos) {
                case R.id.nav_modules:
                    setFragment(moduleFragment);
                    break;
                case R.id.nav_discover:
                    setFragment(discoverFragment);
                    break;
                case R.id.nav_profile:
                    setFragment(userFragment);
                    break;
            }
        } else {
            setFragment(discoverFragment);
            mNav.setSelectedItemId(R.id.nav_discover);
        }

        mNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                switch(item.getItemId()) {
                    case R.id.nav_modules:
                        setFragment(moduleFragment);
                        return true;
                    case R.id.nav_discover:
                        setFragment(discoverFragment);
                        return true;
                    case R.id.nav_profile:
                        setFragment(userFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void updateUI(UserData userData){
        NavigationView mNavView = findViewById(R.id.nav_view);

        TextView username = mNavView.getHeaderView(0).findViewById(R.id.username);
        TextView useremail = mNavView.getHeaderView(0).findViewById(R.id.useremail);
        username.setText(userData.name);
        useremail.setText(userData.email);
    }

    public void logout() {
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent homeIntent = new Intent(getApplicationContext(), Onboarding.class);
                        startActivity(homeIntent);
                        Toast.makeText(getApplicationContext(),"Logged Out!",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


package com.aaars.b;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

public class GMailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new com.aaars.b.JSSEProvider());
    }

    public GMailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try{
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/html"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
        }catch(Exception e){

        }
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}

package com.aaars.b;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Random;

public class Splash extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        createNotificationChannel();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        progressBar = findViewById(R.id.progressBar);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progressStatus < 100) {
                    progressStatus += new Random().nextInt(15);
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        Thread.sleep(50);    //SET TO ARBITRARY NUMBER
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Intent onbIntent = new Intent(Splash.this, Onboarding.class);
                Splash.this.startActivity(onbIntent);
                finish();
            }
        }).start();
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //MIN
            int importanceLow = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channelLow = new NotificationChannel("default", "Automator", importanceLow);
            channelLow.setDescription("Automator Notification Channel");

            //HIGH PRIORITY
            int importanceHigh = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channelHigh = new NotificationChannel("high", "Automator", importanceHigh);
            channelHigh.setDescription("Automator Notification Channel");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channelLow);
            notificationManager.createNotificationChannel(channelHigh);
        }
    }
}


package com.aaars.b;

import java.security.AccessController;
import java.security.Provider;

public final class JSSEProvider extends Provider {
    public JSSEProvider() {
        super("HarmonyJSSE", 1.0, "Harmony JSSE Provider");
        AccessController
                .doPrivileged(new java.security.PrivilegedAction<Void>() {

                    public Void run() {
                        put("SSLContext.TLS",
                                "org.apache.harmony.xnet.provider.jsse.SSLContextImpl");
                        put("Alg.Alias.SSLContext.TLSv1", "TLS");
                        put("KeyManagerFactory.X509",
                                "org.apache.harmony.xnet.provider.jsse.KeyManagerFactoryImpl");
                        put("TrustManagerFactory.X509",
                                "org.apache.harmony.xnet.provider.jsse.TrustManagerFactoryImpl");
                        return null;
                    }

                });
    }
}



package com.aaars.b;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.aaars.b.Root.USER_ID;

public class UserFragment extends Fragment {
    private List<Notif> data;
    private RecyclerView rv;
    private UserData userData;
    DatabaseReference dr;
    DatabaseReference last;
    LastRun lr;

    public UserFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        lr = new LastRun();

        final RecyclerView rvuser = view.findViewById(R.id.rvuser);
        final TextView tvhead = view.findViewById(R.id.tvhead);
        final TextView tv = view.findViewById(R.id.tv);

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    userData = dataSnapshot.getValue(UserData.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.", Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);



        //LASTRUN
        last = database.getInstance().getReference().child("users").child(USER_ID).child("lastrun");
        ValueEventListener lastr = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    lr = dataSnapshot.getValue(LastRun.class);
                    LinearLayoutManager llmuser = new LinearLayoutManager(getContext());
                    rvuser.setLayoutManager(llmuser);
                    rvuser.setHasFixedSize(true);
                    initializeData();
                    RVAdapter adapter = new RVAdapter(data);
                    rvuser.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        last.addValueEventListener(lastr);



        return view;
    }

    private void initializeData(){
        try {
            data = new ArrayList<>();
            data.add(new Notif("Device Security", "Set volume to min through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 9));
            data.add(new Notif("Safety", "Send SMS with device location when battery is critically low", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.orange), 1));
            data.add(new Notif("Wifi", "Turn Wi-Fi OFF at specific time to save battery life", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 2));
            data.add(new Notif("Device Security", "Turns OFF Wi-Fi through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 7));
            data.add(new Notif("Wifi", "Turn Wi-Fi ON at specific time to connect to the Internet", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 3));
            data.add(new Notif("Location", "Log time spent at specific location", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.yellove), 4));
            data.add(new Notif("Device Security", "Turns On Wi-Fi through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 6));
            data.add(new Notif("Email", "Sends you Quote of the Day to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 5));
            data.add(new Notif("Safety", "Sends location to selected contacts on notification tap", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.orange), 10));
            data.add(new Notif("Device Security", "Recover lost device through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 15));
            data.add(new Notif("Email", "Sends you a comic snippet to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 11));
            data.add(new Notif("Device Security", "Set volume to max through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 8));
            data.add(new Notif("Email", "Sends bundled RSS feed to your email address", R.drawable.mail, ContextCompat.getColor(getContext(), R.color.cyan), 13));
            data.add(new Notif("Device Security", "Locate your phone through SMS", R.drawable.alpha, ContextCompat.getColor(getContext(), R.color.red), 12));
            data.add(new Notif("Wifi", "Turn Wi-Fi ON at specific location for seamless switch", R.drawable.network, ContextCompat.getColor(getContext(), R.color.cardGreen), 14));
        }
        catch(Exception e) {

        }
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.DataViewHolder>{

        List<Notif> notif;

        RVAdapter(List<Notif> persons){
            this.notif = persons;
        }

        @Override
        public int getItemCount() {
            return notif.size();
        }

        @Override
        public RVAdapter.DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_user, viewGroup, false);
            RVAdapter.DataViewHolder pvh = new RVAdapter.DataViewHolder(v);
            return pvh;
        }

        @Override
        public void onBindViewHolder(RVAdapter.DataViewHolder dataViewHolder, int i) {
            try {
                dataViewHolder.desc.setText(notif.get(i).desc);
                dataViewHolder.img.setImageResource(notif.get(i).img);
                dataViewHolder.cv.setCardBackgroundColor(notif.get(i).clr);
                dataViewHolder.footer.setText((!lr.lastrun.get(notif.get(i).moduleid).equals("0") ? "LAST RAN : " + lr.lastrun.get(notif.get(i).moduleid).substring(0, 20) : "NEVER RAN"));
            }
            catch (Exception e) {}
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public class DataViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView header;
            TextView desc;
            ImageView img;
            TextView footer;

            DataViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.cvuser);
                desc = itemView.findViewById(R.id.desc);
                img = itemView.findViewById(R.id.img);
                footer = itemView.findViewById(R.id.footer);
            }
        }
    }
}

class Notif {
    String header;
    String desc;
    int img;
    int clr;
    int moduleid;

    Notif(String header, String desc, int img, int clr, int moduleid) {
        this.header = header;
        this.desc = desc;
        this.img = img;
        this.clr = clr;
        this.moduleid = moduleid;
    }
}


package com.aaars.b;

public class TriggerLogs {
    public int id;

    public TriggerLogs() {}

    public TriggerLogs(int id) {
        this.id = id;
    }
}


package com.aaars.b;

import android.support.annotation.Keep;

@Keep
public class UserData {
    public String name;
    public String email;
    public String photo;

    public UserData() { }

    public UserData(String name, String email, String photo) {
        this.name = name;
        this.email = email;
        this.photo = photo;

    }

    public class activity { }
}


package com.aaars.b;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import static android.content.Context.LOCATION_SERVICE;
import static com.aaars.b.Root.USER_ID;

public class MessageReceiver extends BroadcastReceiver {

    private static MessageListener mListener;
    LocationManager locationManager;
    SmsManager smsManager;
    Module md2;
    DatabaseReference last;
    LastRun lr;
    Boolean flag;
    GoogleSignInAccount account;


    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            account = GoogleSignIn.getLastSignedInAccount(context);
            if (account != null && USER_ID == null) {
                USER_ID = account.getId();
            }

            md2 = new Module();
            smsManager = SmsManager.getDefault();
            flag = true;

            //LASTRUN
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            last = database.getInstance().getReference().child("users").child(USER_ID).child("lastrun");
            ValueEventListener lastr = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        lr = dataSnapshot.getValue(LastRun.class);
                        if(flag) {
                            //call(context, intent);
                            flag = false;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to load post.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            last.addValueEventListener(lastr);
            call(context, intent);
        }
        catch (Exception e) {
            Toast.makeText(context, "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }

    void call(final Context context, final Intent intent) {
        try {

            md2 = new Module();
            smsManager = SmsManager.getDefault();

            if (account != null && USER_ID == null) {
                USER_ID = account.getId();
            }

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference drmd2 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
            ValueEventListener wifiTimerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        md2 = dataSnapshot.getValue(Module.class);
                        Bundle data = intent.getExtras();
                        Object[] pdus = (Object[]) data.get("pdus");
                        for (int i = 0; i < pdus.length; i++) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

                            if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("wifi on " + md2.parameters.get(0)) && md2.parameters.get(1).equals("true")) {
                                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager != null) {
                                    wifiManager.setWifiEnabled(true);
                                }

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(6,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("wifi off " + md2.parameters.get(0)) && md2.parameters.get(2).equals("true")) {
                                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager != null) {
                                    wifiManager.setWifiEnabled(false);
                                }

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(7,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("volume max " + md2.parameters.get(0)) && md2.parameters.get(3).equals("true")) {
                                AudioManager mobilemode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, mobilemode.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_RING, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(8,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("volume min " + md2.parameters.get(0)) && md2.parameters.get(4).equals("true")) {
                                AudioManager mobilemode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, mobilemode.getStreamMinVolume(AudioManager.STREAM_MUSIC), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, mobilemode.getStreamMinVolume(AudioManager.STREAM_ALARM), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_RING, mobilemode.getStreamMinVolume(AudioManager.STREAM_RING), 0);

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(9,"" + cc.getTime());
                                last.setValue(lr);

                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("location " + md2.parameters.get(0)) && md2.parameters.get(6).equals("true")) {
                                final LocationListener locationListener = new LocationListener() {
                                    @Override
                                    public void onLocationChanged(final Location location) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                                        ref.child("1").child("currentLocation").setValue(location);
                                        Calendar cc = Calendar.getInstance();
                                        lr.lastrun.set(12,"" + cc.getTime());
                                        last.setValue(lr);
                                    }

                                    @Override
                                    public void onStatusChanged(String s, int i, Bundle bundle) {
                                    }

                                    @Override
                                    public void onProviderEnabled(String s) {
                                    }

                                    @Override
                                    public void onProviderDisabled(String s) {
                                    }
                                };

                                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15,
                                            50, locationListener);

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(12,"" + cc.getTime());
                                last.setValue(lr);

                                Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                String content = "UNABLE";
                                if (lc != null)
                                    content = "I am here: https://www.google.com/maps/search/?api=1&query=" + lc.getLatitude() + "," + lc.getLongitude() + " - Sent via Automator";
                                smsManager.sendTextMessage(smsMessage.getOriginatingAddress(), null, content, null, null);
                            } else if (smsMessage.getDisplayMessageBody().equalsIgnoreCase("lostphone " + md2.parameters.get(0)) && md2.parameters.get(5).equals("true")) {

                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(15,"" + cc.getTime());
                                last.setValue(lr);

                                //AUDIO MAX
                                AudioManager mobilemode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                                mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, mobilemode.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
                                mobilemode.setStreamVolume(AudioManager.STREAM_RING, mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING), 0);

                                //WIFI ON
                                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                if (wifiManager != null) {
                                    wifiManager.setWifiEnabled(true);
                                }

                                final LocationListener locationListener = new LocationListener() {
                                    @Override
                                    public void onLocationChanged(final Location location) {
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                                        ref.child("1").child("currentLocation").setValue(location);
                                    }

                                    @Override
                                    public void onStatusChanged(String s, int i, Bundle bundle) {
                                    }

                                    @Override
                                    public void onProviderEnabled(String s) {
                                    }

                                    @Override
                                    public void onProviderDisabled(String s) {
                                    }
                                };

                                locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
                                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
                                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15,
                                            50, locationListener);

                                Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                String content = "I am here: https://www.google.com/maps/search/?api=1&query=" + lc.getLatitude() + "," + lc.getLongitude() + " - Sent via Automator";
                                smsManager.sendTextMessage(smsMessage.getOriginatingAddress(), null, content, null, null);

                                MediaPlayer mpintro = MediaPlayer.create(context, R.raw.alarm);
                                mpintro.setLooping(false);
                                mpintro.start();
                            }

                            //mListener.messageReceived(message);
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, "Failed to load post.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            drmd2.addValueEventListener(wifiTimerListener);
        }
        catch (Exception e) {
            Toast.makeText(context, "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }

    public static void bindListener(MessageListener listener){
        mListener = listener;
    }
}


package com.aaars.b;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.aaars.b.Fragments.QuotesReceiver;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;


import java.util.HashMap;
import java.util.Random;

import static com.aaars.b.Root.USER_ID;


public class Triggers extends Service implements MessageListener{
    Module md1, md2, md3, md4, md5, md6, md7, md8;
    LastRun lr;
    long[] lastrun = new long[50];
    Module[] md;
    DatabaseReference[] dr;
    DatabaseReference drmd1, drmd2, drmd3, drmd4, drmd5, drmd6, last;
    HashMap<String, Boolean> booleanMap;
    SmsManager smsManager;
    LocationManager locationManager;
    Calendar start, end;
    Boolean begin = true;



    public Triggers() {

    }

    @Override
    public void messageReceived(String message) {
        Toast.makeText(this, "New Message Received: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        booleanMap = new HashMap<>();
        smsManager = SmsManager.getDefault();

        md = new Module[20];
        dr = new DatabaseReference[20];


        MessageReceiver.bindListener(this);

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //LASTRUN
        last = database.getInstance().getReference().child("users").child(USER_ID).child("lastrun");
        ValueEventListener lastr = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    lr = dataSnapshot.getValue(LastRun.class);
                }
                else {
                    last.setValue(lr);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        last.addValueEventListener(lastr);


        //LOW BATTERY SMS LOCATION - MODULE 01
        drmd1 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("1");
        ValueEventListener lowbatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md1 = dataSnapshot.getValue(Module.class);
                    booleanMap.put("lowBatteryLocation", md1.enabled);
                    booleanMap.put("onConnectDisconnectToast",true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd1.addValueEventListener(lowbatListener);

        //WIFI OFF TIMER - MODULE 02
        drmd2 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("2");
        ValueEventListener wifiTimerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md2 = dataSnapshot.getValue(Module.class);
                    booleanMap.put("wifiTimer", md2.enabled);
                    wifitimer();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd2.addValueEventListener(wifiTimerListener);

        //WIFI ON TIMER - MODULE 03
        dr[4] = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("4");
        ValueEventListener geoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md[4] = dataSnapshot.getValue(Module.class);
                    booleanMap.put("geofencing", md[4].enabled);
                    geotimer();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr[4].addValueEventListener(geoListener);

        //GEO WIFI ON TIMER - MODULE 03
        dr[5] = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("11");
        ValueEventListener geofListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md[5] = dataSnapshot.getValue(Module.class);
                    booleanMap.put("geowifi", md[5].enabled);
                    geooff();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr[5].addValueEventListener(geofListener);

        //QUOTES EMAIL - MODULE 05
        drmd5 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("5");
        ValueEventListener quotesListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md5 = dataSnapshot.getValue(Module.class);
                    booleanMap.put("quotesEmail", md5.enabled);
                    quotesEmail();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd5.addValueEventListener(quotesListener);

        //5 HELP - MODULE 10
        drmd6 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("10");
        ValueEventListener helpListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md6 = dataSnapshot.getValue(Module.class);
                    booleanMap.put("askHelp", md6.enabled);
                    askHelp();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd6.addValueEventListener(helpListener);

        //XKCD - MODULE 11
        dr[7] = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("7");
        ValueEventListener xkcdListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md[7] = dataSnapshot.getValue(Module.class);
                    booleanMap.put("xkcd", md[7].enabled);
                    quotesEmail();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr[7].addValueEventListener(xkcdListener);

        //RSS - MODULE 11
        dr[8] = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("8");
        ValueEventListener rssListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md[8] = dataSnapshot.getValue(Module.class);
                    booleanMap.put("rss", md[8].enabled);
                    quotesEmail();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr[8].addValueEventListener(xkcdListener);

        batteryTrigger();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Triggers.this);
        notificationManager.cancel(3);
    }

    public void callNotification(String title, String text) {
        Intent intent = new Intent(Triggers.this, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(Triggers.this, 0, intent, 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Triggers.this, "default")
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setSmallIcon(R.drawable.alpha)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Triggers.this);
        notificationManager.notify(1, mBuilder.build());
    }

    public void callStickyNotification() {
        Intent intent = new Intent(Triggers.this, HelpSMS.class);
        String s = md6.parameters.get(0) + md6.parameters.get(1) + md6.parameters.get(2);
        intent.putExtra("number",s);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(Triggers.this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(Triggers.this, "default")
                .setContentTitle("Emergency Location Sender")
                .setContentText("Tap on this Notification to send Location to trusted contacts")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Tap on this Notification to send Location to trusted contacts"))
                .setSmallIcon(R.drawable.alpha)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAutoCancel(false)
                .setOngoing(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Triggers.this);
        notificationManager.notify(1, mBuilder.build());
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, "default")
                    .setAutoCancel(true)
                    .setOngoing(true);

            Notification notification = builder.build();
            startForeground(3, notification);
        }
        else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(Triggers.this, "default")
                    .setAutoCancel(true)
                    .setOngoing(true);

            Notification notification = builder.build();
            startForeground(3, notification);
        }
        return START_STICKY;
    }

    public void batteryTrigger() {
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    //LOW BATTERY SMS LOCATION - MODULE 01
                    if (booleanMap.get("lowBatteryLocation")) {
                        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
                            final LocationListener locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(final Location location) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                                    ref.child("1").child("currentLocation").setValue(location);
                                    Calendar cc = Calendar.getInstance();
                                    lr.lastrun.set(1,"" + cc.getTime());
                                    last.setValue(lr);
                                }

                                @Override
                                public void onStatusChanged(String s, int i, Bundle bundle) { }

                                @Override
                                public void onProviderEnabled(String s) { }

                                @Override
                                public void onProviderDisabled(String s) { }
                            };

                            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                            if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION))
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,15,
                                        50, locationListener);

                            Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            String content = "I am here: https://www.google.com/maps/search/?api=1&query=" + lc.getLatitude() + "," + lc.getLongitude() + " - Sent via Automator" ;
                            smsManager.sendTextMessage(md1.parameters.get(0),null,content,null,null);
                            callNotification("A Module Ran - Low Battery","Your current location sent to your trusted number for security purpose.");
                            Calendar cc = Calendar.getInstance();
                            lr.lastrun.set(1,"" + cc.getTime());
                            last.setValue(lr);
                        }
                    }

                    //SEND TOAST ON CHARGING CONNECT / DISCONNECT - MODULE *
                    if (booleanMap.get("onConnectDisconnectToast")) {

                        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                            Toast.makeText(context, "The device is charging", Toast.LENGTH_SHORT).show();
                            Calendar cc = Calendar.getInstance();
                            lr.lastrun.set(1,"" + cc.getTime());
                            last.setValue(lr);
                        } else {
                            intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED);
                            Toast.makeText(context, "The device is not charging", Toast.LENGTH_SHORT).show();
                            Calendar cc = Calendar.getInstance();
                            lr.lastrun.set(1,"" + cc.getTime());
                            last.setValue(lr);
                        }
                    }
                }
                catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(receiver, filter);
    }

    //WIFI OFF TIMER - MODULE 02 - [FUNCTION CALL]
    public void wifitimer() {
        try {
            if (booleanMap.get("wifiTimer")) {
                Intent alarm = new Intent(getApplicationContext(), TriggerReceiver.class);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(md2.parameters.get(7)));
                c.set(Calendar.MINUTE, Integer.parseInt(md2.parameters.get(8)));

                if (Calendar.getInstance().after(c)) {
                    c.add(Calendar.DAY_OF_MONTH, 1);
                }

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), 999685, alarm, 0);
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmManager.INTERVAL_DAY, pendingIntent);
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }

    //WIFI ON TIMER - MODULE 03 - [FUNCTION CALL]
    public void wifiontimer(){
        try {
            if (booleanMap.get("wifiOnTimer")) {
                Intent alarmtwo = new Intent(getApplicationContext(), TriggerOnReceiver.class);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(md3.parameters.get(7)));
                c.set(Calendar.MINUTE, Integer.parseInt(md3.parameters.get(8)));

                if (Calendar.getInstance().after(c)) {
                    c.add(Calendar.DAY_OF_MONTH, 1);
                }

                PendingIntent pendingIntenttwo = PendingIntent.getBroadcast(
                        getApplicationContext(), 999687, alarmtwo, 0);
                AlarmManager alarmManagertwo = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManagertwo.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmManagertwo.INTERVAL_DAY, pendingIntenttwo);
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }

    //QUOTES EMAIL - MODULE 05 - [FUNCTION CALL]
    public void quotesEmail() {
        try {
            if (booleanMap.get("quotesEmail") || booleanMap.get("xkcd") || booleanMap.get("rss")) {
                Intent alarmthree = new Intent(getApplicationContext(), QuotesReceiver.class);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.HOUR_OF_DAY, 10);
                c.set(Calendar.MINUTE, 0);

                if (Calendar.getInstance().after(c)) {
                    c.add(Calendar.DAY_OF_MONTH, 1);
                }

                PendingIntent pendingIntentthree = PendingIntent.getBroadcast(
                        getApplicationContext(), 999686, alarmthree, 0);
                AlarmManager alarmManagerthree = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManagerthree.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmManagerthree.INTERVAL_DAY, pendingIntentthree);
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }

    public void askHelp(){
        if(booleanMap.get("askHelp")){
            callStickyNotification();
        }
        else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(Triggers.this);
            notificationManager.cancel(1);
        }
    }

    public float distFrom(double lat1, double lng1, double lat2, double lng2) {
        float dist = 0;
        try {
            double earthRadius = 6371000; //meters
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLng / 2) * Math.sin(dLng / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            dist = (float) (earthRadius * c);
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
        return dist;
    }


    void geotimer() {
        try {
            if (booleanMap.get("geofencing")) {
                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {
                        if (distFrom(Double.parseDouble(md[4].parameters.get(0)), Double.parseDouble(md[4].parameters.get(1)), location.getLatitude(), location.getLongitude()) < 50) {
                            if (begin) {
                                start = Calendar.getInstance();
                                begin = false;
                            }
                            end = Calendar.getInstance();
                            if ((end.getTimeInMillis() - start.getTimeInMillis()) > 60000) {
                                long secs = Long.parseLong(md[4].parameters.get(2)) + (end.getTimeInMillis() - start.getTimeInMillis());
                                md[4].parameters.set(2, Long.toString(secs));
                                dr[4].setValue(md[4]);
                                start = end;
                            }
                        } else {
                            if (!begin) {
                                end = Calendar.getInstance();
                                long secs = Long.parseLong(md[4].parameters.get(2)) + (end.getTimeInMillis() - start.getTimeInMillis());
                                md[4].parameters.set(2, Long.toString(secs));
                                dr[4].setValue(md[4]);
                                begin = true;
                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(4,"" + cc.getTime());
                                last.setValue(lr);
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                    }
                };

                locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION))
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15,
                            50, locationListener);
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }


    void geooff() {
        try {
            if (booleanMap.get("geowifi")) {
                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {
                        if (distFrom(Double.parseDouble(md[5].parameters.get(0)), Double.parseDouble(md[5].parameters.get(1)), location.getLatitude(), location.getLongitude()) < 250) {
                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                            if (wifiManager != null) {
                                wifiManager.setWifiEnabled(true);
                                callNotification("Wi-Fi turned on", "Wi-Fi was turned on entering location");
                                Calendar cc = Calendar.getInstance();
                                lr.lastrun.set(5,"" + cc.getTime());
                                last.setValue(lr);
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                    }
                };

                locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION))
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15,
                            50, locationListener);
                Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (lc != null) {
                    if (distFrom(Double.parseDouble(md[5].parameters.get(0)), Double.parseDouble(md[5].parameters.get(1)), lc.getLatitude(), lc.getLongitude()) < 500) {
                        if (wifiManager != null) {
                            wifiManager.setWifiEnabled(true);
                            callNotification("Wi-Fi turned on", "Wi-Fi was turned on entering location");
                            Calendar cc = Calendar.getInstance();
                            lr.lastrun.set(5,"" + cc.getTime());
                            last.setValue(lr);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Module Ran, Restart app to sync with server", Toast.LENGTH_LONG).show();
        }
    }
}

package com.aaars.b;

import java.util.*;

public class LastRun {
    public ArrayList<String> lastrun;


    public LastRun() {
        lastrun = new ArrayList<>();
        for(int i = 0; i < 29; i++) {
            lastrun.add("0");
        }
    }

}


package com.aaars.b;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Onboarding extends AppCompatActivity {

    //GOOGLE
    private static final String TAG = "Home";
    private static int RC_SIGN_IN = 9001;
    private static GoogleSignInClient mGoogleSignInClient;

    //PERMISSIONS
    static String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NFC,
            Manifest.permission.NFC_TRANSACTION_EVENT,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    // ONBOARDING DECLARATIONS
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    ImageButton mNextBtn;
    static Button mSkipBtn, mFinishBtn, mDo;
    static Intent rootIntent;
    static ImageView img;

    ImageView zero, one, two;
    ImageView[] indicators;

    int page = 0;

    //GOOGLE LOGIN FUNCTIONS
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
            finish();
        } catch (ApiException e) {
            Toast.makeText(getApplicationContext(),"Login Failed!",Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
    }

    private  void updateUI(GoogleSignInAccount a) {

    }

    private void googleSignIn() {
        rootIntent = new Intent(this, Root.class);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null) {
            rootIntent = new Intent(Onboarding.this, Root.class);
            rootIntent.putExtra("EMAIL", account.getEmail());
            rootIntent.putExtra("NAME", account.getDisplayName());
            rootIntent.putExtra("USER_ID", account.getId());
            startActivity(rootIntent);
            updateUI(account);
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_onboarding);

        googleSignIn();

        //ONBOARDING DEFINITIONS
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mNextBtn = (ImageButton) findViewById(R.id.intro_btn_next);
        mSkipBtn = (Button) findViewById(R.id.intro_btn_skip);
        mFinishBtn = (Button) findViewById(R.id.intro_btn_finish);
        mDo = (Button) findViewById(R.id.intro_btn_do);



        zero = (ImageView) findViewById(R.id.intro_indicator_0);
        one = (ImageView) findViewById(R.id.intro_indicator_1);
        two = (ImageView) findViewById(R.id.intro_indicator_2);

        indicators = new ImageView[]{zero, one, two};

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(page);
        updateIndicators(page);

        final int color1 = ContextCompat.getColor(this, R.color.cyan);
        final int color2 = ContextCompat.getColor(this, R.color.orange);
        final int color3 = ContextCompat.getColor(this, R.color.green);
        final int[] colorList = new int[]{color1, color2, color3};

        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //COLOR UPDATE ON SCROLL
                int colorUpdate = (Integer) evaluator.evaluate(positionOffset, colorList[position], colorList[position == 2 ? position : position + 1]);
                mViewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mViewPager.setBackgroundColor(color1);
                        break;
                    case 1:
                        mViewPager.setBackgroundColor(color2);
                        break;
                    case 2:
                        mViewPager.setBackgroundColor(color3);
                        break;
                }
                updateIndicators(position);

                mNextBtn.setVisibility(position == 2 ? View.GONE : View.VISIBLE);
                mFinishBtn.setVisibility(position == 2 ? View.VISIBLE : View.GONE);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //REQUIRED FOR SCROLL ACTIVITY
            }
        });

        //ONCLICK LISTENERS

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page += 1;
                mViewPager.setCurrentItem(page, true);
            }
        });

        mSkipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reIntent = new Intent(getApplicationContext(), Onboarding.class);
                startActivity(reIntent);
                finish();
            }
        });

        mFinishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reIntent = new Intent(getApplicationContext(), Onboarding.class);
                startActivity(reIntent);
                finish();
            }
        });
    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_onboarding, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //FRAGMENT PLACEHOLDER
    public static class PlaceholderFragment extends Fragment {

        String[] btnC = new String[] {"Google Sign In", "Give Permissions", "Get Started!"};
        int[] imgC = new int[] {R.drawable.login, R.drawable.laptop, R.drawable.project};
        private static final String ARG_SECTION_NUMBER = "section_number";
        public PlaceholderFragment() {

        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_onboarding, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            String[] str = new String[]{getContext().getString(R.string.section_one),getContext().getString(R.string.section_two),getContext().getString(R.string.section_three)};
            textView.setText(str[getArguments().getInt(ARG_SECTION_NUMBER)-1]);

            img = rootView.findViewById(R.id.img);
            img.setImageResource(imgC[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);

            mDo = rootView.findViewById(R.id.intro_btn_do);
            mDo.setText(btnC[getArguments().getInt(ARG_SECTION_NUMBER) - 1]);
            mDo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDoOnClick(getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                }
            });
            return rootView;
        }

        private void mDoOnClick(int pos) {
            switch(pos) {
                case 0:
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent,RC_SIGN_IN);
                    break;
                case 1:
                    requestPermissions(permissions, 9999);
                    break;
                case 2:
                    Intent reIntent = new Intent(getContext(), Onboarding.class);
                    startActivity(reIntent);
                    getActivity().finish();
                    break;
            }
        }
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Login / Signup";
                case 1:
                    return "Permissions";
                case 2:
                    return "Blast Off";
            }
            return null;
        }
    }
}


package com.aaars.b;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Fragments.GeoFencing;
import com.aaars.b.Fragments.GetLocation;
import com.aaars.b.Fragments.HelpButton;
import com.aaars.b.Fragments.LocationWiFi;
import com.aaars.b.Fragments.LostPhone;
import com.aaars.b.Fragments.LowBatterySMS;
import com.aaars.b.Fragments.ModuleDesignFragment;
import com.aaars.b.Fragments.QuotesEmail;
import com.aaars.b.Fragments.RSS;
import com.aaars.b.Fragments.SMSWiFiOff;
import com.aaars.b.Fragments.SMSWiFiOn;
import com.aaars.b.Fragments.VolumeMax;
import com.aaars.b.Fragments.VolumeMin;
import com.aaars.b.Fragments.WiFiOnTimer;
import com.aaars.b.Fragments.WiFiTimer;

import com.aaars.b.Fragments.XKCD;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ModuleDesign extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Button logout;
    FirebaseAuth mAuth;
    GoogleSignInAccount user;
    private GoogleSignInClient mGoogleSignInClient;

    public static String USER_ID;
    public static int MODULE_ID;
    private String TAG = "TAG";

    FrameLayout mFrame;
    BottomNavigationView mNav;
    ActionBarDrawerToggle toggle;

    private ModuleDesignFragment moduleDesignFragment;
    private LowBatterySMS lowBatterySMS;
    private WiFiTimer wiFiTimer;
    private WiFiOnTimer wiFiOnTimer;
    private GeoFencing geoFencing;
    private QuotesEmail quotesEmail;
    private SMSWiFiOn smsWiFiOn;
    private SMSWiFiOff smsWiFiOff;
    private VolumeMax volumeMax;
    private VolumeMin volumeMin;
    private HelpButton helpButton;
    private XKCD xkcd;
    private LostPhone lostPhone;
    private LocationWiFi locationWiFi;
    private RSS rss;
    private GetLocation getLocation;

    public UserData userData, intentData;
    DatabaseReference dr;

    private Intent i;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_discover:
                i.putExtra("fromChild", true);
                i.putExtra("USER_ID",USER_ID);
                i.putExtra("pos",R.id.nav_discover);
                startActivity(i);
                break;
            case R.id.nav_modules:
                i.putExtra("fromChild", true);
                i.putExtra("USER_ID",USER_ID);
                i.putExtra("pos",R.id.nav_modules);
                startActivity(i);
                break;
            case R.id.nav_profile:
                i.putExtra("fromChild", true);
                i.putExtra("USER_ID",USER_ID);
                i.putExtra("pos",R.id.nav_profile);
                startActivity(i);
                break;
            case R.id.help:
                break;
            case R.id.about:
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.restart:
                Intent i = new Intent(getApplicationContext(),Splash.class);
                startActivity(i);
                break;
            case R.id.exit:
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                notificationManager.cancelAll();
                finish();
                System.exit(0);
                break;
            default:
                return false;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        i = new Intent(this, Root.class);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        final Intent intent = getIntent();
        intentData = new UserData();
        USER_ID = intent.getStringExtra("USER_ID");
        MODULE_ID = intent.getIntExtra("MODULE_ID", -1);


        if(USER_ID == null) {
            Intent i = new Intent(this, Onboarding.class);
            startActivity(i);
        }

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");

        dr = database.getInstance().getReference().child("users").child(USER_ID);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    userData = dataSnapshot.getValue(UserData.class);
                    updateUI(userData);

                } else {
                    FirebaseDatabase.getInstance().getReference("users").child(USER_ID).setValue(intentData);
                    userData = intentData;
                    updateUI(userData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(getApplicationContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        dr.addValueEventListener(postListener);


        //**********************************************//

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavView = findViewById(R.id.nav_view);
        mNavView.setNavigationItemSelectedListener(this);

        mFrame = (FrameLayout) findViewById(R.id.frame);
        mNav = (BottomNavigationView) findViewById(R.id.navigation);

        switch (MODULE_ID) {
            case 1:
                lowBatterySMS = new LowBatterySMS();
                setFragment(lowBatterySMS);
                break;
            case 2:
                wiFiTimer = new WiFiTimer();
                setFragment(wiFiTimer);
                break;
            case 3:
                wiFiOnTimer = new WiFiOnTimer();
                setFragment(wiFiOnTimer);
                break;
            case 4:
                geoFencing = new GeoFencing();
                setFragment(geoFencing);
                break;
            case 5:
                quotesEmail = new QuotesEmail();
                setFragment(quotesEmail);
                break;
            case 6:
                smsWiFiOn = new SMSWiFiOn();
                setFragment(smsWiFiOn);
                break;
            case 7:
                smsWiFiOff = new SMSWiFiOff();
                setFragment(smsWiFiOff);
                break;
            case 8:
                volumeMax = new VolumeMax();
                setFragment(volumeMax);
                break;
            case 9:
                volumeMin = new VolumeMin();
                setFragment(volumeMin);
                break;
            case 10:
                helpButton = new HelpButton();
                setFragment(helpButton);
                break;
            case 11:
                xkcd = new XKCD();
                setFragment(xkcd);
                break;
            case 15:
                lostPhone = new LostPhone();
                setFragment(lostPhone);
                break;
            case 13:
                rss = new RSS();
                setFragment(rss);
                break;
            case 14:
                locationWiFi = new LocationWiFi();
                setFragment(locationWiFi);
                break;
            case 12:
                getLocation = new GetLocation();
                setFragment(getLocation);
                break;
            default:
                moduleDesignFragment = new ModuleDesignFragment();
                setFragment(moduleDesignFragment);
                break;

        }

        mNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START))
                    drawer.closeDrawer(GravityCompat.START);
                i.putExtra("fromChild", true);
                i.putExtra("USER_ID",USER_ID);
                switch(item.getItemId()) {
                    case R.id.nav_modules:
                        i.putExtra("pos",R.id.nav_modules);
                        startActivity(i);
                        return true;
                    case R.id.nav_discover:
                        i.putExtra("pos",R.id.nav_discover);
                        startActivity(i);
                        return true;
                    case R.id.nav_profile:
                        i.putExtra("pos",R.id.nav_profile);
                        startActivity(i);
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

    public void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //[UPDATE UI]
    public void updateUI(UserData userData){
        NavigationView mNavView = findViewById(R.id.nav_view);
        TextView username = mNavView.getHeaderView(0).findViewById(R.id.username);
        TextView useremail = mNavView.getHeaderView(0).findViewById(R.id.useremail);
        username.setText(userData.name);
        useremail.setText(userData.email);
    }

    //[LOGOUT]
    public void logout() {
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent homeIntent = new Intent(ModuleDesign.this, Onboarding.class);
                        startActivity(homeIntent);
                        Toast.makeText(ModuleDesign.this,"Logged Out!",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GetLocation extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public GetLocation() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume_min, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);
        TextView paraphrase = view.findViewById(R.id.paraphrase);

        tvhead.setText("Get device location through sms");
        tv.setText("On receiving a sms containing keywords and paraphrase sends the location of the device to requesting number");
        paraphrase.setText("location <paraphrase>");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);

                    if(!md.parameters.get(0).equals(""))
                        input.setEnabled(false);

                    toggle.setChecked(md.parameters.get(6).equals("true"));
                    input.setText(md.parameters.get(0));
                    email.setText("Edit Paraphrase");

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 105;
                    md.activityid = 108;
                    md.enabled = true;
                    md.parameters.add("");
                    for(int i = 0; i < 6; i++)
                        md.parameters.add("false");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String paraphrase = input.getText().toString();
                    if (paraphrase.length() > 3) {
                        md.parameters.set(0,paraphrase);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Paraphrase");
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.parameters.set(6,(b?"true":"false"));
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LostPhone extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public LostPhone() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume_min, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);
        TextView paraphrase = view.findViewById(R.id.paraphrase);

        tvhead.setText("Take extreme measures to find the lost device");
        tv.setText("On receiving a sms containing keywords and paraphrase switches on the wifi, send location to the number and sounds alarm for recovering the device");
        paraphrase.setText("lostphone <paraphrase>");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);

                    if(!md.parameters.get(0).equals(""))
                        input.setEnabled(false);

                    toggle.setChecked(md.parameters.get(5).equals("true"));
                    input.setText(md.parameters.get(0));
                    email.setText("Edit Paraphrase");

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 105;
                    md.activityid = 108;
                    md.enabled = true;
                    md.parameters.add("");
                    for(int i = 0; i < 6; i++)
                        md.parameters.add("false");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String paraphrase = input.getText().toString();
                    if (paraphrase.length() >= 3) {
                        md.parameters.set(0,paraphrase);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Paraphrase");
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.parameters.set(5,(b?"true":"false"));
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.aaars.b.Root;
import com.aaars.b.Splash;
import com.aaars.b.TriggerReceiver;
import com.aaars.b.Triggers;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.ALARM_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;


public class WiFiTimer extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    Button click;
    String USER_ID;
    CheckBox mon, tue, wed, thu, fri, sat, sun;
    TimePicker timePicker;

    public Module md;
    DatabaseReference dr;

    private int hr, min;


    public WiFiTimer() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_timer, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        click = view.findViewById(R.id.click);

        mon = view.findViewById(R.id.mon);
        tue = view.findViewById(R.id.tue);
        wed = view.findViewById(R.id.wed);
        thu = view.findViewById(R.id.thu);
        fri = view.findViewById(R.id.fri);
        sat = view.findViewById(R.id.sat);
        sun = view.findViewById(R.id.sun);

        timePicker = view.findViewById(R.id.time);

        tvhead.setText("Turn Wi-Fi off at specific time");
        tv.setText("Wi-Fi can be turned off to save battery, during sleeping hours or during study time on selected days");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("2");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    mon.setChecked(md.parameters.get(0).equals("true"));
                    tue.setChecked(md.parameters.get(1).equals("true"));
                    wed.setChecked(md.parameters.get(2).equals("true"));
                    thu.setChecked(md.parameters.get(3).equals("true"));
                    fri.setChecked(md.parameters.get(4).equals("true"));
                    sat.setChecked(md.parameters.get(5).equals("true"));
                    sun.setChecked(md.parameters.get(6).equals("true"));
                    timePicker.setCurrentHour(Integer.parseInt(md.parameters.get(7)));
                    timePicker.setCurrentMinute(Integer.parseInt(md.parameters.get(8)));
                }
                else {
                    //INSTANTIATION OF MODULE 02
                    md.triggerid = 102;
                    md.activityid = 102;
                    md.enabled = false;
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("0");
                    md.parameters.add("0");
                    dr.setValue(md);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hr = timePicker.getCurrentHour();
                min = timePicker.getCurrentMinute();

                md.parameters.set(0,mon.isChecked()?"true":"false");
                md.parameters.set(1,tue.isChecked()?"true":"false");
                md.parameters.set(2,wed.isChecked()?"true":"false");
                md.parameters.set(3,thu.isChecked()?"true":"false");
                md.parameters.set(4,fri.isChecked()?"true":"false");
                md.parameters.set(5,sat.isChecked()?"true":"false");
                md.parameters.set(6,sun.isChecked()?"true":"false");

                md.parameters.set(7,Integer.toString(hr));
                md.parameters.set(8,Integer.toString(min));
                md.enabled = true;

                dr.setValue(md);

                Intent i = new Intent(getContext(),Splash.class);
                startActivity(i);
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });



        return view;
    }

}


package com.aaars.b.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.aaars.b.Splash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class WiFiOnTimer extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    Button click;
    String USER_ID;
    CheckBox mon, tue, wed, thu, fri, sat, sun;
    TimePicker timePicker;

    public Module md;
    DatabaseReference dr;

    private int hr, min;


    public WiFiOnTimer() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_timer, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        click = view.findViewById(R.id.click);

        mon = view.findViewById(R.id.mon);
        tue = view.findViewById(R.id.tue);
        wed = view.findViewById(R.id.wed);
        thu = view.findViewById(R.id.thu);
        fri = view.findViewById(R.id.fri);
        sat = view.findViewById(R.id.sat);
        sun = view.findViewById(R.id.sun);

        timePicker = view.findViewById(R.id.time);

        tvhead.setText("Turn Wi-Fi on at specific time");
        tv.setText("Wi-Fi can be turned on at a specific time to connect to the Internet, enable updates and sync the device on selected days");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("3");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    mon.setChecked(md.parameters.get(0).equals("true"));
                    tue.setChecked(md.parameters.get(1).equals("true"));
                    wed.setChecked(md.parameters.get(2).equals("true"));
                    thu.setChecked(md.parameters.get(3).equals("true"));
                    fri.setChecked(md.parameters.get(4).equals("true"));
                    sat.setChecked(md.parameters.get(5).equals("true"));
                    sun.setChecked(md.parameters.get(6).equals("true"));
                    timePicker.setCurrentHour(Integer.parseInt(md.parameters.get(7)));
                    timePicker.setCurrentMinute(Integer.parseInt(md.parameters.get(8)));
                }
                else {
                    //INSTANTIATION OF MODULE 02
                    md.triggerid = 102;
                    md.activityid = 103;
                    md.enabled = false;
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("false");
                    md.parameters.add("0");
                    md.parameters.add("0");
                    dr.setValue(md);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hr = timePicker.getCurrentHour();
                min = timePicker.getCurrentMinute();

                md.parameters.set(0,mon.isChecked()?"true":"false");
                md.parameters.set(1,tue.isChecked()?"true":"false");
                md.parameters.set(2,wed.isChecked()?"true":"false");
                md.parameters.set(3,thu.isChecked()?"true":"false");
                md.parameters.set(4,fri.isChecked()?"true":"false");
                md.parameters.set(5,sat.isChecked()?"true":"false");
                md.parameters.set(6,sun.isChecked()?"true":"false");

                md.parameters.set(7,Integer.toString(hr));
                md.parameters.set(8,Integer.toString(min));
                md.enabled = true;

                dr.setValue(md);

                Intent i = new Intent(getContext(),Splash.class);
                startActivity(i);
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LowBatterySMS extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button savePhone;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public LowBatterySMS() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_low_battery_sm, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        savePhone = view.findViewById(R.id.savePhone);

        tvhead.setText("Send SMS to your trusted contacts when battery is low.");
        tv.setText("Sending SMS to your trusted contacts can be of great help when your battery runs out of power");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("1");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    if(md.parameters.get(0).length() == 10) {
                        input.setText(md.parameters.get(0));
                        input.setEnabled(false);
                        savePhone.setText("Edit Number");
                    }

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 101;
                    md.activityid = 101;
                    md.enabled = true;
                    md.parameters.add("");
                    dr.setValue(md);

                    input.setEnabled(true);
                    savePhone.setText("Save Number");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        savePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String phone = input.getText().toString();
                    if (phone.length() == 10) {
                        md.parameters.set(0,phone);
                        dr.setValue(md);
                        input.setEnabled(false);
                        savePhone.setText("Edit Number");
                    }
                }
                else {
                    input.setEnabled(true);
                    savePhone.setText("Save Number");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class VolumeMax extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public VolumeMax() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume_min, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);
        TextView paraphrase = view.findViewById(R.id.paraphrase);

        tvhead.setText("Set device volume to maximum through sms");
        tv.setText("On receiving a sms containing keywords and paraphrase sets the volume to maximum value");
        paraphrase.setText("volume max <paraphrase>");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);

                    if(!md.parameters.get(0).equals(""))
                        input.setEnabled(false);

                    toggle.setChecked(md.parameters.get(3).equals("true"));
                    input.setText(md.parameters.get(0));
                    email.setText("Edit Paraphrase");

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 105;
                    md.activityid = 108;
                    md.enabled = true;
                    md.parameters.add("");
                    for(int i = 0; i < 6; i++)
                        md.parameters.add("false");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String paraphrase = input.getText().toString();
                    if (paraphrase.length() > 3) {
                        md.parameters.set(0,paraphrase);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Paraphrase");
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.parameters.set(3,(b?"true":"false"));
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class VolumeMin extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public VolumeMin() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume_min, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);
        TextView paraphrase = view.findViewById(R.id.paraphrase);

        tvhead.setText("Set device volume to minimum through sms");
        tv.setText("On receiving a sms containing keywords and paraphrase sets the volume to minimum value");
        paraphrase.setText("volume min <paraphrase>");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);

                    if(!md.parameters.get(0).equals(""))
                        input.setEnabled(false);

                    toggle.setChecked(md.parameters.get(4).equals("true"));
                    input.setText(md.parameters.get(0));
                    email.setText("Edit Paraphrase");

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 105;
                    md.activityid = 108;
                    md.enabled = true;
                    md.parameters.add("");
                    for(int i = 0; i < 6; i++)
                        md.parameters.add("false");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String paraphrase = input.getText().toString();
                    if (paraphrase.length() > 3) {
                        md.parameters.set(0,paraphrase);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Paraphrase");
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.parameters.set(4,(b?"true":"false"));
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class QuotesEmail extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public QuotesEmail() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xkcd, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);

        tvhead.setText("Sends Quote of the Day to you email address");
        tv.setText("Uses RSS feed to send you a quote of the day at 10 a.m. everyday.");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("5");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    if(md.parameters.get(0).contains("@") && md.parameters.get(0).contains(".")) {
                        input.setText(md.parameters.get(0));
                        input.setEnabled(false);
                        email.setText("Edit Email");
                    }
                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 101;
                    md.activityid = 101;
                    md.enabled = true;
                    md.parameters.add("");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Email");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String emailid = input.getText().toString();
                    if (emailid.contains("@") && emailid.contains(".") && emailid.length() > 10) {
                        md.parameters.set(0,emailid);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Email");
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Email");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class XKCD extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public XKCD() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xkcd, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);

        tvhead.setText("Sends a comic snippet daily to you email address");
        tv.setText("Uses RSS feed to send you a comic snippet at 10 a.m. everyday.");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("7");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    if(md.parameters.get(0).contains("@") && md.parameters.get(0).contains(".")) {
                        input.setText(md.parameters.get(0));
                        input.setEnabled(false);
                        email.setText("Edit Email");
                    }
                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 101;
                    md.activityid = 101;
                    md.enabled = true;
                    md.parameters.add("");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Email");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String emailid = input.getText().toString();
                    if (emailid.contains("@") && emailid.contains(".") && emailid.length() > 10) {
                        md.parameters.set(0,emailid);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Email");
                    }
                    else {
                        Toast.makeText(getContext(),"Enter a valid email address",Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Email");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.LOCATION_SERVICE;
import static com.aaars.b.Root.USER_ID;


public class LocationWiFi extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText lat, lon;
    Button getloc, saveloc;
    String USER_ID;

    public Module md;
    DatabaseReference dr;
    LocationManager locationManager;


    public LocationWiFi() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geo_fencing, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        lat = view.findViewById(R.id.inputLat);
        lon = view.findViewById(R.id.inputLong);
        getloc = view.findViewById(R.id.save);
        saveloc = view.findViewById(R.id.savebeta);
        TextView paraphrase = view.findViewById(R.id.paraphrase);

        paraphrase.setVisibility(View.GONE);
        tvhead.setText("Turn on Wi-Fi after entering a location");
        tv.setText("Wi-Fi will be turned on after entering a location to connect to faster internet seamlessly");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("11");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    if(!md.parameters.get(0).equals("0.0") && !md.parameters.get(1).equals("0.0")) {
                        lat.setText(md.parameters.get(0));
                        lon.setText(md.parameters.get(1));
                        lon.setEnabled(false);
                        lat.setEnabled(false);
                        getloc.setText("Get Location");
                        saveloc.setText("Edit Location");
                    }
                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 101;
                    md.activityid = 101;
                    md.enabled = true;
                    md.parameters.add("0.0");
                    md.parameters.add("0.0");
                    md.parameters.add("0");
                    dr.setValue(md);
                    getloc.setText("Get Location");
                    saveloc.setText("Set Location");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        saveloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lat.isEnabled() && lon.isEnabled()) {
                    String latitude = lat.getText().toString();
                    String longitude = lon.getText().toString();
                    if (!latitude.isEmpty() && !longitude.isEmpty()) {
                        md.parameters.set(0,latitude);
                        md.parameters.set(1,longitude);
                        dr.setValue(md);
                        lon.setEnabled(false);
                        lat.setEnabled(false);
                        getloc.setText("Get Location");
                        saveloc.setText("Edit Location");
                    }
                }
                else {
                    lon.setEnabled(true);
                    lat.setEnabled(true);
                    getloc.setText("Get Location");
                    saveloc.setText("Set Location");
                }
            }
        });

        getloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                        ref.child("1").child("currentLocation").setValue(location);
                        lat.setText(Double.toString(location.getLatitude()));
                        lon.setText(Double.toString(location.getLongitude()));
                        getloc.setText("Get Location");
                        saveloc.setText("Edit Location");
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) { }

                    @Override
                    public void onProviderEnabled(String s) { }

                    @Override
                    public void onProviderDisabled(String s) { }
                };

                locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
                if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION))
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,15,
                            50, locationListener);

                Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lat.setText(Double.toString(lc.getLatitude()));
                lon.setText(Double.toString(lc.getLongitude()));
                getloc.setText("Get Location");
                saveloc.setText("Edit Location");
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaars.b.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HelpFragment extends Fragment {


    public HelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HelpButton extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input, inputbeta, inputgamma;
    Button savePhone, savePhonebeta, savePhonegamma;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public HelpButton() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_button, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();


        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        inputbeta = view.findViewById(R.id.inputbeta);
        inputgamma = view.findViewById(R.id.inputgamma);
        savePhone = view.findViewById(R.id.savePhone);
        savePhonebeta = view.findViewById(R.id.savePhonebeta);
        savePhonegamma = view.findViewById(R.id.savePhonegamma);

        tvhead.setText("Send SMS to your trusted contacts on a click");
        tv.setText("Send SMS with your location to your trusted contacts on clicking a sticky notification when in danger");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("10");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    if(md.parameters.get(0).length() == 10) {
                        input.setText(md.parameters.get(0));
                        input.setEnabled(false);
                        savePhone.setText("Edit Number");
                    }
                    if(md.parameters.get(1).length() == 10) {
                        inputbeta.setText(md.parameters.get(1));
                        inputbeta.setEnabled(false);
                        savePhonebeta.setText("Edit Number");
                    }
                    if(md.parameters.get(2).length() == 10) {
                        inputgamma.setText(md.parameters.get(2));
                        inputgamma.setEnabled(false);
                        savePhonegamma.setText("Edit Number");
                    }

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 101;
                    md.activityid = 101;
                    md.enabled = true;
                    md.parameters.add("");
                    md.parameters.add("");
                    md.parameters.add("");
                    dr.setValue(md);

                    savePhone.setText("Save Number");
                    savePhonebeta.setText("Save Number");
                    savePhonegamma.setText("Save Number");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        savePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String phone = input.getText().toString();
                    if (phone.length() == 10) {
                        md.parameters.set(0,phone);
                        dr.setValue(md);
                        input.setEnabled(false);
                        savePhone.setText("Edit Number");
                    }
                }
                else {
                    input.setEnabled(true);
                    savePhone.setText("Save Number");
                }
            }
        });

        savePhonebeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputbeta.isEnabled()) {
                    String phonebeta = inputbeta.getText().toString();
                    if (phonebeta.length() == 10) {
                        md.parameters.set(1,phonebeta);
                        dr.setValue(md);
                        inputbeta.setEnabled(false);
                        savePhonebeta.setText("Edit Number");
                    }
                }
                else {
                    inputbeta.setEnabled(true);
                    savePhonebeta.setText("Save Number");
                }
            }
        });

        savePhonegamma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputgamma.isEnabled()) {
                    String phonegamma = inputgamma.getText().toString();
                    if (phonegamma.length() == 10) {
                        md.parameters.set(2,phonegamma);
                        dr.setValue(md);
                        inputgamma.setEnabled(false);
                        savePhonegamma.setText("Edit Number");
                    }
                }
                else {
                    inputgamma.setEnabled(true);
                    savePhonegamma.setText("Save Number");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SMSWiFiOn extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public SMSWiFiOn() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume_min, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);
        TextView paraphrase = view.findViewById(R.id.paraphrase);

        tvhead.setText("Switch on device wifi through sms");
        tv.setText("On receiving a sms containing keywords and paraphrase switches on the wifi for remote control of your device");
        paraphrase.setText("wifi on <paraphrase>");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);

                    if(!md.parameters.get(0).equals(""))
                        input.setEnabled(false);

                    toggle.setChecked(md.parameters.get(1).equals("true"));
                    input.setText(md.parameters.get(0));
                    email.setText("Edit Paraphrase");

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 105;
                    md.activityid = 106;
                    md.enabled = true;
                    md.parameters.add("");
                    for(int i = 0; i < 6; i++)
                        md.parameters.add("false");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String paraphrase = input.getText().toString();
                    if (paraphrase.length() > 3) {
                        md.parameters.set(0,paraphrase);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Paraphrase");
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.parameters.set(1,(b?"true":"false"));
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.LOCATION_SERVICE;
import static com.aaars.b.Root.USER_ID;


public class GeoFencing extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText lat, lon;
    Button getloc, saveloc;
    String USER_ID;

    public Module md;
    DatabaseReference dr;
    LocationManager locationManager;


    public GeoFencing() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geo_fencing, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        lat = view.findViewById(R.id.inputLat);
        lon = view.findViewById(R.id.inputLong);
        getloc = view.findViewById(R.id.save);
        saveloc = view.findViewById(R.id.savebeta);
        final TextView paraphrase = view.findViewById(R.id.paraphrase);

        tvhead.setText("Log time at any particular location");
        tv.setText("Log the amount of time spent at any particular location within 250m of the given coordinates");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("4");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    if(!md.parameters.get(0).equals("0.0") && !md.parameters.get(1).equals("0.0")) {
                        lat.setText(md.parameters.get(0));
                        lon.setText(md.parameters.get(1));
                        lon.setEnabled(false);
                        lat.setEnabled(false);
                        getloc.setText("Get Location");
                        saveloc.setText("Edit Location");
                        paraphrase.setText(md.parameters.get(2) + " mili seconds");
                    }
                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 101;
                    md.activityid = 101;
                    md.enabled = true;
                    md.parameters.add("0.0");
                    md.parameters.add("0.0");
                    md.parameters.add("0");
                    dr.setValue(md);
                    paraphrase.setText("0 mili seconds");
                    getloc.setText("Get Location");
                    saveloc.setText("Set Location");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        saveloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lat.isEnabled() && lon.isEnabled()) {
                    String latitude = lat.getText().toString();
                    String longitude = lon.getText().toString();
                    if (!latitude.isEmpty() && !longitude.isEmpty()) {
                        md.parameters.set(0,latitude);
                        md.parameters.set(1,longitude);
                        md.parameters.set(2,"0");
                        paraphrase.setText("0 mili seconds");
                        dr.setValue(md);
                        lon.setEnabled(false);
                        lat.setEnabled(false);
                        getloc.setText("Get Location");
                        saveloc.setText("Edit Location");
                    }
                }
                else {
                    lon.setEnabled(true);
                    lat.setEnabled(true);
                    getloc.setText("Get Location");
                    saveloc.setText("Set Location");
                }
            }
        });

        getloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference ref = database.getReference("users").child(USER_ID).child("modules");
                        ref.child("1").child("currentLocation").setValue(location);
                        lat.setText(Double.toString(location.getLatitude()));
                        lon.setText(Double.toString(location.getLongitude()));
                        getloc.setText("Get Location");
                        saveloc.setText("Edit Location");
                        md.parameters.set(2,"0");
                        paraphrase.setText("0 mili seconds");
                        dr.setValue(md);
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) { }

                    @Override
                    public void onProviderEnabled(String s) { }

                    @Override
                    public void onProviderDisabled(String s) { }
                };

                locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
                if(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION))
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,15,
                            50, locationListener);

                Location lc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lat.setText(Double.toString(lc.getLatitude()));
                lon.setText(Double.toString(lc.getLongitude()));
                getloc.setText("Get Location");
                saveloc.setText("Edit Location");
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class RSS extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input, rss;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public RSS() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rss, container, false);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        rss = view.findViewById(R.id.rss);
        email = view.findViewById(R.id.savePhone);

        tvhead.setText("Sends RSS feed bundle to you email address");
        tv.setText("Creates an RSS bundle specially for you to include all of your daily reads");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("8");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    toggle.setChecked(md.enabled);
                    if(md.parameters.get(0).contains("@") && md.parameters.get(0).contains(".")) {
                        input.setText(md.parameters.get(0));
                        input.setEnabled(false);
                        email.setText("Edit Email");
                    }
                    if(!md.parameters.get(1).equals("")) {
                        rss.setText(md.parameters.get(1));
                        rss.setEnabled(false);
                    }

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 101;
                    md.activityid = 101;
                    md.enabled = true;
                    md.parameters.add("");
                    md.parameters.add("");
                    dr.setValue(md);

                    input.setEnabled(true);
                    rss.setEnabled(true);
                    email.setText("Save Email");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String emailid = input.getText().toString();
                    if (emailid.contains("@") && emailid.contains(".") && emailid.length() > 10) {
                        md.parameters.set(0,emailid);
                        dr.setValue(md);
                        input.setEnabled(false);
                        rss.setEnabled(false);
                        email.setText("Edit Email");
                    }
                }
                else {
                    input.setEnabled(true);
                    rss.setEnabled(true);
                    email.setText("Save Email");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.enabled = b;
                dr.setValue(md);
            }
        });

        return view;
    }

}


package com.aaars.b.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aaars.b.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

}


package com.aaars.b.Fragments;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aaars.b.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ModuleDesignFragment extends Fragment {

    private Button mDo;
    private EditText input;
    private int parameter;

    public ModuleDesignFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_module_design, container, false);

        mDo = view.findViewById(R.id.click);
        input = view.findViewById(R.id.input);

        final TextView tv = view.findViewById(R.id.tv);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getContext().registerReceiver(null, ifilter);

        final int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        tv.setText("" + level);

        mDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parameter = Integer.parseInt(input.getText().toString());
                if(level < parameter) {
                    tv.setTextColor(getResources().getColor(R.color.green));
                }
                else {
                    tv.setTextColor(getResources().getColor(R.color.orange));
                }
            }
        });



        return view;
    }

}


package com.aaars.b.Fragments;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.aaars.b.GMailSender;
import com.aaars.b.Module;
import com.aaars.b.R;
import com.aaars.b.Splash;
import com.aaars.b.Triggers;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prof.rssparser.Article;
import com.prof.rssparser.Parser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import static com.aaars.b.Root.USER_ID;

public class QuotesReceiver extends BroadcastReceiver {

    Module md, md2, md3;

    @Override
    public void onReceive(final Context context, Intent intent) {
        md = new Module();
        md2 = new Module();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference drmd2 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("5");
        ValueEventListener wifiTimerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md2 = dataSnapshot.getValue(Module.class);
                    quotesEmail(context, md2.parameters.get(0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd2.addValueEventListener(wifiTimerListener);

        md = new Module();
        DatabaseReference drmd = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("7");
        ValueEventListener xkcdListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);
                    xkcd(context, md.parameters.get(0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd.addValueEventListener(xkcdListener);

        md3 = new Module();
        DatabaseReference drmd3 = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("8");
        ValueEventListener nasaListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md3 = dataSnapshot.getValue(Module.class);
                    nasa(context, md3.parameters.get(0));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        drmd3.addValueEventListener(nasaListener);

    }

    void quotesEmail(final Context context, final String email) {
        try {
            String[] urlString = {"https://feeds.feedburner.com/quotationspage/qotd?format=xml", "https://www.brainyquote.com/link/quotebr.rss"};
            Parser parser = new Parser();
            parser.execute(urlString[new Random().nextInt(1)]);
            parser.onFinish(new Parser.OnTaskCompleted() {
                @Override
                public void onTaskCompleted(final ArrayList<Article> list) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                int random = new Random().nextInt(4);
                                GMailSender sender = new GMailSender(
                                        "automator.alpha@gmail.com",
                                        "AbCd1234");
                                sender.sendMail("Quote of the Day | Automator", "<h1>" + list.get(random).getDescription() + "<br>- <b>" + list.get(random).getTitle() + "</b></h1>",
                                        "Automator", email);
                                callNotification("A Module Ran - Sent Quote on your EMail", "Quote of the Day was sent to your EMail address", context, 8);

                            } catch (Exception e) {
                                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).start();
                }

                @Override
                public void onError() {
                    //what to do in case of error
                }
            });
        }
        catch(Exception e) {}
    }

    void xkcd(final Context context, final String email) {
        try {
            Parser parser = new Parser();
            parser.execute("https://xkcd.com/rss.xml?format=xml");
            parser.onFinish(new Parser.OnTaskCompleted() {
                @Override
                public void onTaskCompleted(final ArrayList<Article> list) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                int random = new Random().nextInt(2);
                                GMailSender sender = new GMailSender(
                                        "automator.alpha@gmail.com",
                                        "AbCd1234");
                                sender.sendMail("XKCD Snippet | Automator", "<h1>" + list.get(random).getDescription() + "</h1><br>- <b>" + list.get(random).getTitle() + "</b>",
                                        "Automator", email);
                                callNotification("A Module Ran - Sent XKCD snippet on your EMail", "XKCD snippet was sent to your email address", context, 9);

                            } catch (Exception e) {
                                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).start();
                }

                @Override
                public void onError() {
                    //what to do in case of error
                }
            });
        }
        catch(Exception e) {}
    }

    void nasa(final Context context, final String email) {
        try {
            Parser parser = new Parser();
            parser.execute(md3.parameters.get(1));
            parser.onFinish(new Parser.OnTaskCompleted() {
                @Override
                public void onTaskCompleted(final ArrayList<Article> list) {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                int random = new Random().nextInt(2);
                                GMailSender sender = new GMailSender(
                                        "automator.alpha@gmail.com",
                                        "AbCd1234");
                                sender.sendMail("RSS Bundle | Automator", "<h1>" + list.get(random).getDescription() + "</h1><br>- <b>" + list.get(random).getTitle() + "</b><br><br>",
                                        "Automator", email);
                                callNotification("A Module Ran - Sent RSS bundle on your EMail", "RSS bundle was sent to your email address", context, 5);

                            } catch (Exception e) {
                                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                            }
                        }
                    }).start();
                }

                @Override
                public void onError() {
                    //what to do in case of error
                }
            });
        }
        catch(Exception e) {}
    }

    public void callNotification(String title, String text, Context context, int id) {
        Intent intent = new Intent(context, Splash.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "default")
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setSmallIcon(R.drawable.alpha)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, mBuilder.build());
    }
}


package com.aaars.b.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.aaars.b.Module;
import com.aaars.b.ModuleDesign;
import com.aaars.b.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SMSWiFiOff extends Fragment {

    TextView tv, tvhead;
    Switch toggle;
    EditText input;
    Button email;
    String USER_ID;

    public Module md;
    DatabaseReference dr;


    public SMSWiFiOff() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_volume_min, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        USER_ID = ModuleDesign.USER_ID;
        md = new Module();

        tvhead = view.findViewById(R.id.tvhead);
        tv = view.findViewById(R.id.tv);
        toggle = view.findViewById(R.id.toggle);
        input = view.findViewById(R.id.input);
        email = view.findViewById(R.id.savePhone);
        TextView paraphrase = view.findViewById(R.id.paraphrase);

        tvhead.setText("Switch off device wifi through sms");
        tv.setText("On receiving a sms containing keywords and paraphrase switches off the wifi for remote control of your device");
        paraphrase.setText("wifi off <paraphrase>");

        //FIREBASE DATABASE
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        dr = database.getInstance().getReference().child("users").child(USER_ID).child("modules").child("6");
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    md = dataSnapshot.getValue(Module.class);

                    if(!md.parameters.get(0).equals(""))
                        input.setEnabled(false);

                    toggle.setChecked(md.parameters.get(2).equals("true"));
                    input.setText(md.parameters.get(0));
                    email.setText("Edit Paraphrase");

                }
                else {
                    //INSTANTIATION OF MODULE 01
                    md.triggerid = 105;
                    md.activityid = 107;
                    md.enabled = true;
                    md.parameters.add("");
                    for(int i = 0; i < 6; i++)
                        md.parameters.add("false");
                    dr.setValue(md);

                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        dr.addValueEventListener(postListener);

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input.isEnabled()) {
                    String paraphrase = input.getText().toString();
                    if (paraphrase.length() > 3) {
                        md.parameters.set(0,paraphrase);
                        dr.setValue(md);
                        input.setEnabled(false);
                        email.setText("Edit Paraphrase");
                    }
                }
                else {
                    input.setEnabled(true);
                    email.setText("Save Paraphrase");
                }
            }
        });

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                md.parameters.set(2,(b?"true":"false"));
                dr.setValue(md);
            }
        });

        return view;
    }

}


