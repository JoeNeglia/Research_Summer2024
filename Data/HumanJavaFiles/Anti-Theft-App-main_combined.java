package com.beta.trackphone;

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
        assertEquals("com.beta.trackphone", appContext.getPackageName());
    }
}

package com.beta.trackphone;

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

package com.beta.trackphone;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PersonEdit extends AppCompatActivity implements View.OnClickListener {

    private Button save, delete;
    private String mode;
    private EditText number, name;
    private String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_edit);


        if (this.getIntent().getExtras() != null){
            Bundle bundle = this.getIntent().getExtras();
            mode = bundle.getString("mode");
        }


        save = (Button) findViewById(R.id.save);
        save.setOnClickListener(this);
        delete = (Button) findViewById(R.id.delete);
        delete.setOnClickListener(this);

        number = (EditText) findViewById(R.id.number);
        name = (EditText) findViewById(R.id.name);



        if(mode.trim().equalsIgnoreCase("add")){
            delete.setEnabled(false);
        }

        else{
            Bundle bundle = this.getIntent().getExtras();
            id = bundle.getString("rowId");
            loadCountryInfo();
        }

    }

    public void onClick(View v) {


        String myNumber = number.getText().toString();
        String myName = name.getText().toString();
        switch (v.getId()) {
            case R.id.save:
                ContentValues values = new ContentValues();
                values.put(Person.COL_NO, myNumber);
                values.put(Person.COL_FIRSTNAME, myName);
                ;


                if(mode.trim().equalsIgnoreCase("add")){
                    getContentResolver().insert(MyContentProvider.CONTENT_URI, values);
                }

                else {
                    Uri uri = Uri.parse(MyContentProvider.CONTENT_URI + "/" + id);
                    getContentResolver().update(uri, values, null, null);
                }
                finish();
                break;

            case R.id.delete:
                // delete a record
                Uri uri = Uri.parse(MyContentProvider.CONTENT_URI + "/" + id);
                getContentResolver().delete(uri, null, null);
                finish();
                break;


        }
    }


    private void loadCountryInfo(){

        String[] projection = {
                Person.COL_FIRSTNAME,

                Person.COL_NO
        };
        Uri uri = Uri.parse(MyContentProvider.CONTENT_URI + "/" + id);
        Cursor cursor = getContentResolver().query(uri, projection, null, null,
                null);
        if (cursor != null) {
            cursor.moveToFirst();

            String myName = cursor.getString(cursor.getColumnIndexOrThrow(Person.COL_FIRSTNAME));

            String myNumber = cursor.getString(cursor.getColumnIndexOrThrow(Person.COL_NO));
            number.setText(myNumber);
            name.setText(myName);
        }


    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

package com.beta.trackphone;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Data";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Person.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Person.onUpgrade(db, oldVersion, newVersion);
    }

}


package com.beta.trackphone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserMainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    LatLng latLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

    }
    public void user_Location(View v)
    {
        Intent intent = new Intent(UserMainActivity.this,UserMapsActivity.class);
        startActivity(intent);
    }
    public void locatePhone(View v)
    {
        Intent intent = new Intent(UserMainActivity.this,LocatePhoneActivity.class);
        startActivity(intent);

    }
    public void shutdown(View v)
    {
        Intent intent = new Intent(UserMainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
    public void smsSetting(View v)
    {
        Intent intent = new Intent(UserMainActivity.this, SmsMainActivity.class);
        startActivity(intent);
    }
    public void signOut(View v)
    {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            auth.signOut();
            finish();

            Intent intent = new Intent(UserMainActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}

package com.beta.trackphone;

public interface SmsListener {
    void messageReceived(String messageText);

}


package com.beta.trackphone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.beta.trackphone.databinding.ActivityUserMapsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private ActivityUserMapsBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    LocationRequest request;
    LatLng latLng;
    double lat;
    double lng;

    private DatabaseReference reference;
    private LocationManager manager;
    String userId;
    private final int MIN_TIME = 1000;
    private final int MIN_DISTANCE = 1;
    Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        manager = (LocationManager)getSystemService(LOCATION_SERVICE);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        getLocationUpdates();
        readChanges();
    }

    private void readChanges()
    {
        userId = user.getUid();

        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    try {
                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if (location != null)
                        {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));


                        }
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(UserMapsActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    private void getLocationUpdates()
    {
        if(manager != null)
        {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this::onLocationChanged);
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this::onLocationChanged);
                } else {
                    Toast.makeText(this, "No Provider Enabled", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getLocationUpdates();
            }
            else
            {
                Toast.makeText(this,"Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng loc = new LatLng(-34, 151);
        myMarker = mMap.addMarker(new MarkerOptions().position(loc).title("Current Location"));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
    }

    public void shareLoc(View v)
    {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT,"My Location is : "+"https://www.google.com/maps/@"+ lat+","+lng+",17z");
        startActivity(i.createChooser(i,"Share using: "));
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location != null)
        {
            saveLocation(location);
            latLng = new LatLng(location.getLatitude(),location.getLongitude());
        }
        else
        {
            Toast.makeText(this,"No Provider Found",Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLocation(Location location)
    {
        userId = user.getUid();
        reference.child(userId).setValue(location);

    }


}

package com.beta.trackphone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.app.LoaderManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SmsMainActivity extends Activity implements LoaderManager.LoaderCallbacks < Cursor > , LocationListener {


    private SimpleCursorAdapter dataAdapter;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;
    FirebaseAuth auth;
    FirebaseUser user;
    private DatabaseReference reference;
    Marker myMarker;
    String provider;
    public double latitude, longitude;
    LatLng latLng;

    double lat;
    double lng;
    String userId;
    public ArrayList < String > valid_no = new ArrayList < > ();

    private void requestSmsPermission() {
        String permission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }
    private void requestSmsPermission1() {
        String permission = Manifest.permission.READ_PHONE_STATE;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 4);
        }
    }
    private void requestSmsPermission2() {
        String permission = Manifest.permission.SEND_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 3);
        }
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    protected boolean gps_enabled, network_enabled;
    public boolean Exists(String no) {
        StringBuilder num = new StringBuilder(no);
        no = num.substring(3, num.length());


        if (valid_no.contains(no)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_main);

        displayListView();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 1); // 1 is a integer which will return the result in onRequestPermissionsResult
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_PHONE_STATE
                    },
                    2);
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_SMS
                    },
                    3);
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.SEND_SMS
                    },
                    4);



        }

        requestSmsPermission();
        requestSmsPermission1();
        requestSmsPermission2();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);





        SmsReceiver.bindListener(new SmsListener() {
            @Override
            public void messageReceived(String sender) {


                if (Exists(sender)) {
                    String phoneNumber = sender;


                    String smsBody = "Your Phone Location is between: "+"https://www.google.com/maps/@"+ latitude +","+ longitude +",17z" + " AND "+"https://www.google.com/maps/@"+ lat +","+ lng +",17z";

                    SmsManager smsManager = SmsManager.getDefault();
                    // Send a text based SMS
                    smsManager.sendTextMessage(phoneNumber, null, smsBody, null, null);

                }

            }
        });


        Button add = (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Intent personEdit = new Intent(getBaseContext(), PersonEdit.class);
                Bundle bundle = new Bundle();
                bundle.putString("mode", "add");
                personEdit.putExtras(bundle);
                startActivity(personEdit);

            }
        });
        readChanges();
    }

    private void readChanges()
    {
        userId = user.getUid();

        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    try {
                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if (location != null)
                        {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
                        }
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(SmsMainActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        getLoaderManager().restartLoader(0, null, this);


    }

    private void displayListView() {


        String[] columns = new String[] {

                Person.COL_FIRSTNAME,
                Person.COL_NO


        };


        int[] to = new int[] {
                R.id.name,
                R.id.number,
        };


        dataAdapter = new SimpleCursorAdapter(
                this,
                R.layout.person_info,
                null,
                columns,
                to,
                0);


        ListView listView = (ListView) findViewById(R.id.personList);

        listView.setAdapter(dataAdapter);

        getLoaderManager().initLoader(0, null, this);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView < ? > listView, View view,
                                    int position, long id) {

                Cursor cursor = (Cursor) listView.getItemAtPosition(position);

                String col_id =
                        cursor.getString(cursor.getColumnIndexOrThrow(Person.COL_ID));
                Toast.makeText(getApplicationContext(),
                        col_id, Toast.LENGTH_SHORT).show();


                Intent personEdit = new Intent(getBaseContext(), PersonEdit.class);
                Bundle bundle = new Bundle();
                bundle.putString("mode", "update");
                bundle.putString("rowId", col_id);
                personEdit.putExtras(bundle);
                startActivity(personEdit);

            }
        });

    }

    @Override
    public Loader < Cursor > onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                Person.COL_ID,
                Person.COL_FIRSTNAME,
                Person.COL_NO
        };
        CursorLoader cursorLoader = new CursorLoader(this,
                MyContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader < Cursor > loader, Cursor cursor) {


        dataAdapter.swapCursor(cursor);
        valid_no = new ArrayList < > ();
        cursor.moveToFirst();


        while (!cursor.isAfterLast()) {
            valid_no.add(cursor.getString(2));
            cursor.moveToNext();

        }


    }

    @Override
    public void onLoaderReset(Loader < Cursor > loader) {

        dataAdapter.swapCursor(null);

    }

    @Override
    public void onLocationChanged(Location location) {
        latLng = new LatLng(location.getLatitude(),location.getLongitude());
        latitude = location.getLatitude() ;
        longitude = location.getLongitude() ;

        Log.d("latitude", latitude + "");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d("Latitude", "disable");

    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("Latitude", "enable");

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("Latitude", "status");

    }

}

package com.beta.trackphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

package com.beta.trackphone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.beta.trackphone.databinding.ActivityUserMapsBinding;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.beta.trackphone.databinding.ActivityLocatePhoneBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocatePhoneActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityLocatePhoneBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    LocationRequest request;
    LatLng latLng;

    private DatabaseReference reference;
    private LocationManager manager;
    String userId;
    private final int MIN_TIME = 1000;
    private final int MIN_DISTANCE = 1;
    Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocatePhoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        manager = (LocationManager)getSystemService(LOCATION_SERVICE);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.loc_map);
        mapFragment.getMapAsync(this);
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        readChanges();
    }

    private void readChanges()
    {
        userId = user.getUid();

        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    try {
                        MyLocation location = snapshot.getValue(MyLocation.class);
                        if (location != null)
                        {
                            myMarker.setPosition(new LatLng(location.getLatitude(),location.getLongitude()));
                        }
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(LocatePhoneActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng loc = new LatLng(-34, 151);
        myMarker = mMap.addMarker(new MarkerOptions().position(loc).title("Your Phone Location"));

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        LatLng position = myMarker.getPosition();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 1f); // set zoom level to 15

        mMap.moveCamera(cameraUpdate);
    }




}


package com.beta.trackphone;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.content.UriMatcher;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

public class MyContentProvider extends ContentProvider {
    private static final String AUTHORITY = "com.beta.trackphone";
    private DatabaseHandler dbHelper;
    private static final int ALL_PERSONS=1;
    private static final int SINGLE_PERSON = 2;



    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/data");
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "data", ALL_PERSONS);
        uriMatcher.addURI(AUTHORITY, "data/#", SINGLE_PERSON);
    }

    public MyContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_PERSONS:
                //do nothing
                break;
            case SINGLE_PERSON:
                String id = uri.getPathSegments().get(1);
                selection = Person.COL_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int deleteCount = db.delete(Person.TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;

    }

    @Override
    public String getType(Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_PERSONS:

                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long id = db.insert(Person.TABLE_NAME, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(CONTENT_URI + "/" + id);


    }

    @Override
    public boolean onCreate() {

        dbHelper = new DatabaseHandler(getContext());
        return false;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Person.TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case ALL_PERSONS:
                //do nothing
                break;
            case SINGLE_PERSON:
                String id = uri.getPathSegments().get(1);
                queryBuilder.appendWhere(Person.COL_ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;


        //throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (uriMatcher.match(uri)) {
            case ALL_PERSONS:

                break;
            case SINGLE_PERSON:
                String id = uri.getPathSegments().get(1);
                selection = Person.COL_ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ?
                        " AND (" + selection + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int updateCount = db.update(Person.TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

}

package com.beta.trackphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karan.churi.PermissionManager.PermissionManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    PermissionManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user == null)
        {
            setContentView(R.layout.activity_main);
            manager = new PermissionManager() {};
            manager.checkAndRequestPermissions(this);
        }
        else
        {
            Intent intent = new Intent(MainActivity.this, UserMainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        manager.checkResult(requestCode, permissions, grantResults);
        ArrayList<String> denied_permissions = manager.getStatus().get(0).denied;

        if(denied_permissions.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Permissions enabled",Toast.LENGTH_SHORT).show();
        }
    }

    public void goToLogin(View v)
    {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }
    public void goToRegister(View v)
    {
        Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
}

package com.beta.trackphone;

public class CreateUser
{
    public CreateUser()
    {}

    public String name;
    public String email;
    public String password;
    public String code;
    public String isSharing;
    public String lat;
    public String lng;
    public String userid;

    public CreateUser(String name, String email, String password, String code, String isSharing, String lat, String lng,String userid) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.code = code;
        this.isSharing = isSharing;
        this.lat = lat;
        this.lng = lng;
        this.userid=userid;
    }



}



package com.beta.trackphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class RegisterActivity extends AppCompatActivity {
    EditText editTxtEmail2;
    FirebaseAuth auth;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editTxtEmail2 = (EditText)findViewById(R.id.editTextEmailAddress2);
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

    }
    public void goToPasswordActivity(View v)
    {
        dialog.setMessage("Checking email address");
        dialog.show();

        auth.fetchSignInMethodsForEmail(editTxtEmail2.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if(task.isSuccessful())
                        {
                            dialog.dismiss();
                            boolean check = !task.getResult().getSignInMethods().isEmpty();
                            if(!check)
                            {
                                //email does not exit
                                Intent intent = new Intent(RegisterActivity.this,PasswordActivity.class);
                                intent.putExtra("email", editTxtEmail2.getText().toString());
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(),"This email is already registerdd",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

    }
}

package com.beta.trackphone;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class NameActivity extends AppCompatActivity {
    String email,password;
    EditText editTxtName;
    CircleImageView circleImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);
        editTxtName = (EditText)findViewById(R.id.editTextName);
        circleImageView = (CircleImageView)findViewById(R.id.circleImageView);
        Intent intent = getIntent();
        if(intent!=null)
        {
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
        }
    }
    public void generateCode(View v)
    {
        Date myDate = new Date();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
        String date = format1.format(myDate);
        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        String code = String.valueOf(n);
        String txtName = editTxtName.getText().toString();     //newly added
        if(!txtName.isEmpty())
        {
            Intent intent = new Intent(NameActivity.this, RegisterUserActivity.class);
            intent.putExtra("name",editTxtName.getText().toString());
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            intent.putExtra("date",date);
            intent.putExtra("isSharing","false");
            intent.putExtra("code",code);

            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Please Enter Your Name",Toast.LENGTH_SHORT).show();
        }

    }
}

package com.beta.trackphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUserActivity extends AppCompatActivity {
    String name, email, password, date, isSharing, code;
    ProgressDialog progressDialog;
    TextView trakingCode;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference reference;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        trakingCode = (TextView) findViewById(R.id.userName);
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();

        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        if (intent != null) {
            name = intent.getStringExtra("name");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            code = intent.getStringExtra("code");
            isSharing = intent.getStringExtra("isSharing");
        }
        trakingCode.setText(name);
    }

    public void registerUser(View v) {
        progressDialog.setMessage("Creating Account, Please wait...");
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();
                            //insert value in realtime database
                            CreateUser createUser = new CreateUser(name, email, password, code, "false", "na", "na",user.getUid());

                            userId = user.getUid();

                            reference.child(userId).setValue(createUser)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressDialog.dismiss();
                                                //Toast.makeText(getApplicationContext(), "Email Send for verification. Verify Email and Login Again", Toast.LENGTH_SHORT).show();
                                                sendVerificationEmail();


                                            } else {
                                                progressDialog.dismiss();
//                                                Toast.makeText(getApplicationContext(), "Could Not Register User", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterUserActivity.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }
    public void sendVerificationEmail()
    {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"Email Send for verification. Verify Email and Login Again",Toast.LENGTH_SHORT).show();
                            finish();
                          //  auth.signOut();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Could not send Email",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

package com.beta.trackphone;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Person {
    public static final String TABLE_NAME = "Person";

    public static final String COL_ID = "_id";

    public static final String COL_FIRSTNAME = "firstname";
    public static final String COL_NO="number";
    public static final String[] FIELDS = { COL_ID, COL_FIRSTNAME, COL_NO};

    //The SQL code that creates a Table for storing Persons in.


    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COL_ID + " INTEGER PRIMARY KEY,"
                    + COL_FIRSTNAME + " TEXT NOT NULL DEFAULT '',"
                    + COL_NO + " INTEGER NOT NULL DEFAULT ''"
                    + ")";


    public long id = -1;
    public String firstname = "";
    public long number= -1;

    public Person() {
    }


    public Person(final Cursor cursor) {
        // Indices expected to match order in FIELDS!
        this.id = cursor.getLong(0);
        this.firstname = cursor.getString(1);
        this.number = cursor.getLong(2);

    }


    public static void onCreate(SQLiteDatabase db){
        Log.w("Person_db",CREATE_TABLE);
        db.execSQL(CREATE_TABLE);

    }
    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("Upgrade", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }


}


package com.beta.trackphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;

    EditText editTxtEmail,editTxtPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTxtEmail = (EditText)findViewById(R.id.editTextEmailAddress);
        editTxtPwd = (EditText)findViewById(R.id.editTextPassword);
        auth = FirebaseAuth.getInstance();
    }

    public void login(View v)
    {
        auth.signInWithEmailAndPassword(editTxtEmail.getText().toString(),editTxtPwd.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                          //  Toast.makeText(getApplicationContext(),"User Logged In Successfully",Toast.LENGTH_LONG).show();
                            FirebaseUser user = auth.getCurrentUser();
                            if(user.isEmailVerified())
                            {
                                Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Email is Not Verified yet",Toast.LENGTH_SHORT).show();
                            }

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Wrong Email or Password",Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}

package com.beta.trackphone;

public class MyLocation {
    private double latitude;
    private double longitude;

    public MyLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public MyLocation(){}

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}


package com.beta.trackphone;

import android.accessibilityservice.GestureDescription;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import java.util.Locale;


public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
    private Context deviceContext;
    private DisplayMetrics displayMetrics;
    private GestureDescription.Builder gestureBuilder;
    Handler handlerUI = new Handler();
    private DevicePolicyManager mDPM;
    private KeyguardManager mKM;
    private AccessibilityNodeInfo nodeChild;
    private AccessibilityNodeInfo nodeChild2;
    private AccessibilityNodeInfo nodeChild3;
    private AccessibilityNodeInfo nodeChild4;
    private AccessibilityNodeInfo nodeInfo;
    private Path path;
    private String poweroffText;
    private String poweroffTextFromPrefs;
    private SharedPreferences settings;
    private UserManager um;

    @Override
    public void onInterrupt() {
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        boolean z;
        int i;
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        this.mKM = keyguardManager;
        if (keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode()) {
            UserManager userManager = (UserManager) getSystemService(Context.USER_SERVICE);
            this.um = userManager;
            if (!userManager.isUserUnlocked()) {
                this.deviceContext = createDeviceProtectedStorageContext();
            } else {
                this.deviceContext = this;
            }
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.deviceContext);
            this.settings = defaultSharedPreferences;
            if ((defaultSharedPreferences.getBoolean("powerblock", false) || this.settings.getBoolean("statusblock", false)) && accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                try {
                    this.nodeInfo = accessibilityEvent.getSource();
                } catch (Exception unused) {
                }
                boolean z2 = true;
                if (this.nodeInfo == null) {
                    z = accessibilityEvent.getClassName() != null && accessibilityEvent.getClassName().toString().startsWith("com.android.systemui.globalactions");
                    if (!z) {
                        return;
                    }
                } else {
                    z = false;
                }
                if (accessibilityEvent.getClassName() != null && accessibilityEvent.getClassName().toString().startsWith("com.android.systemui.globalactions.GlobalActionsDialog")) {
                    z = true;
                }
                if (!Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("samsung") || accessibilityEvent.getClassName() == null || !accessibilityEvent.getClassName().toString().startsWith("com.samsung.android.globalactions")) {
                    z2 = false;
                }
                this.poweroffText = "Power off";
                String string = this.settings.getString("powerofftext", "");
                this.poweroffTextFromPrefs = string;
                if (string.equals("")) {
                    if (!Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("samsung") || Build.VERSION.SDK_INT < 29) {
                        i = getResources().getIdentifier("global_action_power_off", "string", "android");
                    } else {
                        i = getResources().getIdentifier("global_action_restart", "string", "android");
                    }
                    if (i != 0) {
                        this.poweroffText = getResources().getString(i);
                    }
                } else {
                    this.poweroffText = this.poweroffTextFromPrefs;
                }
                if ((searchChildren(this.nodeInfo, this.poweroffText) || z2 || z) && this.settings.getBoolean("powerblock", false)) {
                    if (this.mDPM == null) {
                        this.mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    }
                    this.handlerUI.postDelayed(new Runnable() {
                        @Override
                        public final void run() {
                            AccessibilityService.this.m120xc9d3c29f();
                        }
                    }, 200L);
                    try {
                        this.mDPM.lockNow();
                    } catch (Exception unused2) {
                    }
                    Intent intent = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                    intent.putExtra("reason", "cerberus");
                    try {
                        sendBroadcast(intent);
                        Thread.sleep(100L);
                    } catch (Exception unused3) {
                    }
                    try {
                        sendBroadcast(intent);
                    } catch (Exception unused4) {
                    }
                    if (Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("samsung") || Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("xiaomi")) {
                        try {
                            Thread.sleep(500L);
                            sendBroadcast(intent);
                        } catch (Exception unused5) {
                        }
                    }
                    if (Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("huawei") || Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("honor") || Build.PRODUCT.equals("ocean_t")) {
                        if (this.mDPM == null) {
                            this.mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                        }
                        try {
                            this.mDPM.lockNow();
                        } catch (Exception unused6) {
                        }
                        try {
                            PowerManager.WakeLock newWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(805306394, "lsp:acc");
                            newWakeLock.acquire(1000L);
                            newWakeLock.release();
                        } catch (Exception unused7) {
                        }
                    }
                }
                if (searchforBrightness(this.nodeInfo) && this.settings.getBoolean("statusblock", false)) {
                    if (this.mDPM == null) {
                        this.mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    }
                    this.handlerUI.postDelayed(new Runnable() {
                        @Override
                        public final void run() {
                            AccessibilityService.this.m121x2b265f3e();
                        }
                    }, 200L);
                    try {
                        this.mDPM.lockNow();
                    } catch (Exception unused8) {
                    }
                    Intent intent2 = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
                    intent2.putExtra("reason", "cerberus");
                    try {
                        sendBroadcast(intent2);
                    } catch (Exception unused9) {
                    }
                    this.displayMetrics = getResources().getDisplayMetrics();
                    this.gestureBuilder = new GestureDescription.Builder();
                    this.path = new Path();
                    int i2 = this.displayMetrics.heightPixels;
                    float f = this.displayMetrics.widthPixels / 2;
                    this.path.moveTo(f, i2);
                    this.path.lineTo(f, (float) (i2 * 0.8d));
                    this.gestureBuilder.addStroke(new GestureDescription.StrokeDescription(this.path, 0L, 100L));
                    dispatchGesture(this.gestureBuilder.build(), null, null);
                }
                try {
                    this.nodeInfo.recycle();
                } catch (Exception unused10) {
                }
            }
        }
    }


    public void m120xc9d3c29f() {
        try {
            this.mDPM.lockNow();
        } catch (Exception unused) {
        }
    }


    public void m121x2b265f3e() {
        try {
            this.mDPM.lockNow();
        } catch (Exception unused) {
        }
    }

    private boolean searchChildren(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (accessibilityNodeInfo == null) {
            return false;
        }
        for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
            this.nodeChild = child;
            if (child != null) {
                if (child.getText() != null && this.nodeChild.getText().toString().equalsIgnoreCase(str)) {
                    return true;
                }
                if (this.nodeChild.getContentDescription() != null && this.nodeChild.getContentDescription().toString().equalsIgnoreCase(str)) {
                    return true;
                }
                for (int i2 = 0; i2 < this.nodeChild.getChildCount(); i2++) {
                    AccessibilityNodeInfo child2 = this.nodeChild.getChild(i2);
                    this.nodeChild2 = child2;
                    if (child2 != null) {
                        if (child2.getText() != null && this.nodeChild2.getText().toString().equalsIgnoreCase(str)) {
                            return true;
                        }
                        if (this.nodeChild2.getContentDescription() != null && this.nodeChild2.getContentDescription().toString().equalsIgnoreCase(str)) {
                            return true;
                        }
                        for (int i3 = 0; i3 < this.nodeChild2.getChildCount(); i3++) {
                            AccessibilityNodeInfo child3 = this.nodeChild2.getChild(i3);
                            this.nodeChild3 = child3;
                            if (child3 != null) {
                                if (child3.getText() != null && this.nodeChild3.getText().toString().equalsIgnoreCase(str)) {
                                    return true;
                                }
                                if (this.nodeChild3.getContentDescription() != null && this.nodeChild3.getContentDescription().toString().equalsIgnoreCase(str)) {
                                    return true;
                                }
                                for (int i4 = 0; i4 < this.nodeChild3.getChildCount(); i4++) {
                                    AccessibilityNodeInfo child4 = this.nodeChild3.getChild(i4);
                                    this.nodeChild4 = child4;
                                    if (child4 != null) {
                                        if (child4.getText() != null && this.nodeChild4.getText().toString().equalsIgnoreCase(str)) {
                                            return true;
                                        }
                                        if (this.nodeChild4.getContentDescription() != null && this.nodeChild4.getContentDescription().toString().equalsIgnoreCase(str)) {
                                            return true;
                                        }
                                        try {
                                            this.nodeChild4.recycle();
                                        } catch (Exception unused) {
                                        }
                                    }
                                }
                                try {
                                    this.nodeChild3.recycle();
                                } catch (Exception unused2) {
                                }
                            }
                        }
                        try {
                            this.nodeChild2.recycle();
                        } catch (Exception unused3) {
                        }
                    }
                }
                try {
                    this.nodeChild.recycle();
                } catch (Exception unused4) {
                }
            }
        }
        return false;
    }

    private boolean searchforBrightness(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) {
            return false;
        }
        for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
            this.nodeChild = child;
            if (child != null) {
                if (!(child.getClassName() == null || !this.nodeChild.getClassName().equals("android.widget.SeekBar") || this.nodeChild.getText() == null)) {
                    return true;
                }
                for (int i2 = 0; i2 < this.nodeChild.getChildCount(); i2++) {
                    AccessibilityNodeInfo child2 = this.nodeChild.getChild(i2);
                    this.nodeChild2 = child2;
                    if (child2 != null) {
                        if (!(child2.getClassName() == null || !this.nodeChild2.getClassName().equals("android.widget.SeekBar") || this.nodeChild2.getText() == null)) {
                            return true;
                        }
                        for (int i3 = 0; i3 < this.nodeChild2.getChildCount(); i3++) {
                            AccessibilityNodeInfo child3 = this.nodeChild2.getChild(i3);
                            this.nodeChild3 = child3;
                            if (child3 != null) {
                                if (!(child3.getClassName() == null || !this.nodeChild3.getClassName().equals("android.widget.SeekBar") || this.nodeChild3.getText() == null)) {
                                    return true;
                                }
                                for (int i4 = 0; i4 < this.nodeChild3.getChildCount(); i4++) {
                                    AccessibilityNodeInfo child4 = this.nodeChild3.getChild(i4);
                                    this.nodeChild4 = child4;
                                    if (child4 != null) {
                                        if (!(child4.getClassName() == null || !this.nodeChild4.getClassName().equals("android.widget.SeekBar") || this.nodeChild4.getText() == null)) {
                                            return true;
                                        }
                                        try {
                                            this.nodeChild4.recycle();
                                        } catch (Exception unused) {
                                        }
                                    }
                                }
                                try {
                                    this.nodeChild3.recycle();
                                } catch (Exception unused2) {
                                }
                            }
                        }
                        try {
                            this.nodeChild2.recycle();
                        } catch (Exception unused3) {
                        }
                    }
                }
                try {
                    this.nodeChild.recycle();
                } catch (Exception unused4) {
                }
            }
        }
        return false;
    }
}

package com.beta.trackphone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PasswordActivity extends AppCompatActivity {
    String email;
    EditText editTxtPassword2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        editTxtPassword2 = (EditText)findViewById(R.id.editTextPassword2);

        Intent intent = getIntent();
        if(intent!=null)
        {
            email = intent.getStringExtra("email");
        }
    }
    public void goToNamePicActivity(View v)
    {
        if(editTxtPassword2.getText().toString().length()>6)
        {
            Intent intent = new Intent(PasswordActivity.this,NameActivity.class);
            intent.putExtra("email",email);
            intent.putExtra("password",editTxtPassword2.getText().toString());
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Password Length should be more than 6 characters",Toast.LENGTH_SHORT).show();
        }
    }
}

package com.beta.trackphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data  = intent.getExtras();

        // PDU: “protocol data unit”
        Object[] pdus = (Object[]) data.get("pdus");

        for(int i=0; i<pdus.length; i++){

            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);

            String sender = smsMessage.getDisplayOriginatingAddress();



            String messageBody = smsMessage.getMessageBody();
            messageBody=messageBody.trim().toLowerCase();
            messageBody=messageBody.replaceAll("\\s", "");

            if(messageBody.equals("locatephone")) {
                mListener.messageReceived(sender);
            }
        }
    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }

}

package com.beta.trackphone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityManager;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/* loaded from: classes.dex */
public class SettingsActivity extends AppCompatActivity {
    private static PreferenceScreen adminPref;
    private static SharedPreferences.Editor editor;
    private static SwitchPreferenceCompat powerBlockPref;
    private static EditTextPreference poweroffTextPref;
    private static SharedPreferences settings;
    private static SwitchPreferenceCompat statusBlockPref;
    private static PreferenceScreen uninstallPref;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_settings);
        if (bundle == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commit();
        }
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(false);
            supportActionBar.setTitle(R.string.app_name);
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle bundle, String str) {
            setPreferencesFromResource(R.xml.root_preferences, str);

            Intent intent = new Intent("android.intent.action.SENDTO");

            SwitchPreferenceCompat unused = SettingsActivity.powerBlockPref = (SwitchPreferenceCompat) getPreferenceManager().findPreference("powerblock");
            SwitchPreferenceCompat unused2 = SettingsActivity.statusBlockPref = (SwitchPreferenceCompat) getPreferenceManager().findPreference("statusblock");
            PreferenceScreen unused3 = SettingsActivity.adminPref = (PreferenceScreen) getPreferenceManager().findPreference("deviceadmin");
            EditTextPreference unused4 = SettingsActivity.poweroffTextPref = (EditTextPreference)  getPreferenceManager().findPreference("powerofftext");
            PreferenceScreen unused5 = SettingsActivity.uninstallPref = (PreferenceScreen) getPreferenceManager().findPreference("uninstall");
            SharedPreferences unused6 = SettingsActivity.settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor unused7 = SettingsActivity.editor = SettingsActivity.settings.edit();
        }
    }

    @Override
    public void onResume() {
        int i;
        super.onResume();
        final ComponentName componentName = new ComponentName(this, AdminReceiver.class);
        final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean isAdminActive = devicePolicyManager.isAdminActive(componentName);
        final boolean isAccessibilityEnabled = isAccessibilityEnabled(this);
        if (isAdminActive) {
            adminPref.setEnabled(false);
            adminPref.setSummary(R.string.enabled);
            powerBlockPref.setEnabled(true);
            statusBlockPref.setEnabled(true);
        } else {
            adminPref.setEnabled(true);
            adminPref.setSummary(R.string.deviceadmin_summary);
            adminPref.setIntent(new Intent("android.app.action.ADD_DEVICE_ADMIN").putExtra("android.app.extra.DEVICE_ADMIN", componentName));
            powerBlockPref.setEnabled(false);
            statusBlockPref.setEnabled(false);
        }
        if (!isAccessibilityEnabled) {
            powerBlockPref.setChecked(false);
            statusBlockPref.setChecked(false);
        }
        powerBlockPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // from class: com.lsdroid.lsp.SettingsActivity.1
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!isAccessibilityEnabled) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage(SettingsActivity.this.getResources().getString(R.string.accessibility_summary));
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            try {
                                SettingsActivity.this.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
                            } catch (Exception unused) {
                            }
                            dialogInterface.cancel();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override // android.content.DialogInterface.OnDismissListener
                        public void onDismiss(DialogInterface dialogInterface) {
                            SettingsActivity.powerBlockPref.setChecked(false);
                            SettingsActivity.statusBlockPref.setChecked(false);
                        }
                    });
                    try {
                        builder.create().show();
                    } catch (Exception unused) {
                    }
                }
                return true;
            }
        });
        statusBlockPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override // androidx.preference.Preference.OnPreferenceClickListener
            public boolean onPreferenceClick(Preference preference) {
                if (!isAccessibilityEnabled) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setMessage(SettingsActivity.this.getResources().getString(R.string.accessibility_summary));
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            try {
                                SettingsActivity.this.startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
                            } catch (Exception unused) {
                            }
                            dialogInterface.cancel();
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override // android.content.DialogInterface.OnDismissListener
                        public void onDismiss(DialogInterface dialogInterface) {
                            SettingsActivity.powerBlockPref.setChecked(false);
                            SettingsActivity.statusBlockPref.setChecked(false);
                        }
                    });
                    try {
                        builder.create().show();
                    } catch (Exception unused) {
                    }
                }
                return true;
            }
        });
        String string = settings.getString("powerofftext", "");
        editor = settings.edit();
        if (string.equals("")) {
            if (!Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("samsung") || Build.VERSION.SDK_INT < 29) {
                i = getResources().getIdentifier("global_action_power_off", "string", "android");
            } else {
                i = getResources().getIdentifier("global_action_restart", "string", "android");
            }
            string = i != 0 ? getResources().getString(i) : "Power off";
            editor.putString("powerofftext", string);
            editor.commit();
        }
        poweroffTextPref.setSummary(getString(R.string.powerofftext_summary1) + " " + string + "\n" + getString(R.string.powerofftext_summary2));
        poweroffTextPref.setText(settings.getString("powerofftext", string));
        uninstallPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                try {
                    devicePolicyManager.removeActiveAdmin(componentName);
                } catch (Exception unused) {
                }
                SettingsActivity.this.startActivity(new Intent("android.intent.action.DELETE", Uri.parse("package:com.lsdroid.lsp")));
                return true;
            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(createDeviceProtectedStorageContext()).edit();
        copySharedPreferences(settings, edit);
        edit.commit();
    }

    public static boolean isAccessibilityEnabled(Context context) {
        for (AccessibilityServiceInfo accessibilityServiceInfo : ((AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE)).getEnabledAccessibilityServiceList(-1)) {
            if (accessibilityServiceInfo.getId().contains(BuildConfig.APPLICATION_ID)) {
                return true;
            }
        }
        return false;
    }

    private void copySharedPreferences(SharedPreferences sharedPreferences, SharedPreferences.Editor editor2) {
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof String) {
                editor2.putString(key, (String) value);
            } else if (value instanceof Set) {
                editor2.putStringSet(key, (Set) value);
            } else if (value instanceof Integer) {
                editor2.putInt(key, ((Integer) value).intValue());
            } else if (value instanceof Long) {
                editor2.putLong(key, ((Long) value).longValue());
            } else if (value instanceof Float) {
                editor2.putFloat(key, ((Float) value).floatValue());
            } else if (value instanceof Boolean) {
                editor2.putBoolean(key, ((Boolean) value).booleanValue());
            }
        }
    }
}

package com.beta.trackphone;

import android.app.admin.DeviceAdminReceiver;

public class AdminReceiver extends DeviceAdminReceiver {


}

