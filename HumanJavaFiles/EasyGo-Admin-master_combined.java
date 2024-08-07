package com.ajinkya.easygo_admin;

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
        assertEquals("com.ajinkya.easygo_admin", appContext.getPackageName());
    }
}

package com.ajinkya.easygo_admin;

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

package com.ajinkya.easygo_admin;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Adapters.PassengerAdapter;
import com.ajinkya.easygo_admin.Model.UserModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class PassengerScreen2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private String BusKey,date;

    ArrayList<UserModel> userModelArrayList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_screen2);
        Initialize();
        Display();
    }

    private void Display() {
        PassengerAdapter adapter = new PassengerAdapter(userModelArrayList);
        recyclerView.setAdapter(adapter);

        progressDialog.show();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Tickets").child("AdminSideCheck").child(date).child(BusKey);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                UserModel userModel = new UserModel();
                Log.e(TAG, "onChildAdded: "+ snapshot.child("PassengerName").getValue());
                userModel.setName(snapshot.child("PassengerName").getValue().toString());
                userModel.setPhoneNo(snapshot.child("PassengerPhone").getValue().toString());
                userModel.setSeat(snapshot.child("PassengerSeatNo").getValue().toString());
                userModelArrayList.add(userModel);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        progressDialog.dismiss();
    }


    private void Initialize() {
        recyclerView = findViewById(R.id.RecyclerViewPS2);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        Toolbar toolbar = findViewById(R.id.ToolbarRPS2);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        BusKey = getIntent().getStringExtra("BusKey");
        date = getIntent().getStringExtra("Date");


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading Information...");
        progressDialog.setMessage("Just a Movement..");
        progressDialog.setCancelable(false);

    }
}

package com.ajinkya.easygo_admin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Adapters.UserTableAdapter;
import com.ajinkya.easygo_admin.Model.UserModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class ViewUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private ArrayList<UserModel> userModelArrayList;
    private UserTableAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

        Initialize();
        DisplayDetails();
    }

    private void DisplayDetails() {
        progressDialog.show();
        userModelArrayList = new ArrayList<>();
        adapter = new UserTableAdapter(ViewUsersActivity.this, userModelArrayList);
        recyclerView.setAdapter(adapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Users");

        databaseReference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChild("Name") && snapshot.hasChild("Email") && snapshot.hasChild("Phone")) {
                    {
                        UserModel userModel = new UserModel();
                        userModel.setName(Objects.requireNonNull(snapshot.child("Name").getValue()).toString());
                        userModel.setPhoneNo(Objects.requireNonNull(snapshot.child("Phone").getValue()).toString());
                        userModel.setEmail(Objects.requireNonNull(snapshot.child("Email").getValue()).toString());
                        userModelArrayList.add(userModel);
                        adapter.notifyDataSetChanged();
                    }
                }else
                    Toast.makeText(ViewUsersActivity.this, "Data not Available", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void Initialize() {
        recyclerView = findViewById(R.id.RecyclerViewVU);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        Toolbar toolbar = findViewById(R.id.ToolbarVU);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        toolbar.setTitle("User Details");


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Searching for Users...");
        progressDialog.setMessage("Just a Movement..");
        progressDialog.setCancelable(false);
    }
}

package com.ajinkya.easygo_admin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Adapters.BusViewTableAdapter;
import com.ajinkya.easygo_admin.Model.BusModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ViewBusActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText Date;
    private Button Search;
    private ProgressDialog progressDialog;
    private ArrayList<BusModel> busModelArrayList;
    private BusViewTableAdapter adapter;

    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bus);

        Initialize();
        Buttons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Date.setText("");
        date="";
    }

    private void Buttons() {
        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Date.getText().toString().isEmpty()){
                    progressDialog.show();
                    busModelArrayList = new ArrayList<>();
                    adapter = new BusViewTableAdapter(ViewBusActivity.this,busModelArrayList);
                    recyclerView.setAdapter(adapter);
                    FirebaseDataRetrieve();
                    progressDialog.dismiss();
                }
                else {
                    Toast.makeText(ViewBusActivity.this, "Please Select Date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void FirebaseDataRetrieve() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Buses").child(date);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                DatabaseReference databaseReference1 = snapshot.getRef();
                Log.e(TAG, "onChildAdded: "+databaseReference1 );
                databaseReference1.addChildEventListener(new ChildEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        BusModel busModel = new BusModel();
                        busModel.setBusNo(Objects.requireNonNull(snapshot.child("BusNo").getValue()).toString());
                        busModel.setDate(Objects.requireNonNull(snapshot.child("Date").getValue()).toString());
                        busModel.setFromLocation(Objects.requireNonNull(snapshot.child("FromLocation").getValue()).toString());
                        busModel.setToLocation(Objects.requireNonNull(snapshot.child("ToLocation").getValue()).toString());
                        busModel.setArrivalTime(Objects.requireNonNull(snapshot.child("StartTime").getValue()).toString());
                        busModel.setDepartureTime(Objects.requireNonNull(snapshot.child("EndTime").getValue()).toString());
                        busModel.setTicketPrice(Objects.requireNonNull(snapshot.child("TicketPrice").getValue()).toString());
                        busModel.setTypeSit(Objects.requireNonNull(snapshot.child("BusType").getValue()).toString());
                        busModel.setAvailableSeat(Objects.requireNonNull(snapshot.child("NumberOfSeat").getValue()).toString());
                        Log.e(TAG, "onChildAdded: "+ Objects.requireNonNull(snapshot.child("BusNo").getValue()) );
                        busModelArrayList.add(busModel);
                        adapter.notifyDataSetChanged();
                        Log.e(TAG, "onChildAdded: "+busModelArrayList.size());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void Initialize() {
        Date = findViewById(R.id.DateVB);
        Search = findViewById(R.id.SearchVB);
        recyclerView = findViewById(R.id.RecyclerViewVB);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));




        Toolbar toolbar = findViewById(R.id.ToolbarVB);
        setSupportActionBar(toolbar);
        toolbar.setTitle("View Buses");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        //Seating datePicker to EditText
        Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                String myFormat = "dd MMM yyyy";
                SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
                date = dateFormat.format(myCalendar.getTime());
                Date.setText(date);

            }
        };
        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ViewBusActivity.this, date1, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Searching for Buses...");
        progressDialog.setMessage("Just a Movement..");
        progressDialog.setCancelable(false);

    }
}

package com.ajinkya.easygo_admin;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ajinkya.easygo_admin.Interface.IFirebaseLoadDone;
import com.ajinkya.easygo_admin.Model.IDs;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddBuses extends AppCompatActivity implements IFirebaseLoadDone {
    private Spinner BusType,ToLocation, FromLocation;
    private EditText BusNumber,StartTime,EndTime,SeatAvailable,Price,Date;
    private Button AddBus;

    private String fromLocation,toLocation,startTime,endTime,date,busType;

    private boolean isStartTime = true;
    private ProgressDialog sendingData, progressDialog;
    private List<IDs> iDs;
    private final Calendar myCalendar = Calendar.getInstance();
    private IFirebaseLoadDone iFirebaseLoadDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_buses);
        Initialize();
        DateTimePicker();
        setDateToEditText();
        FirebaseDataRetrieve();
        spinner();
        Confirming();
    }

    private void Initialize() {
        Toolbar toolbar = findViewById(R.id.toolbarAddBus);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        BusType = findViewById(R.id.BusTypeSpinnerAB);
        BusNumber = findViewById(R.id.BusNumberAB);
        ToLocation = findViewById(R.id.ToLocationAB);
        StartTime = findViewById(R.id.StartTimeAB);
        EndTime = findViewById(R.id.EndTimeAB);
        SeatAvailable = findViewById(R.id.SeatsAB);
        Price = findViewById(R.id.TicketPriceAB);
        Date = findViewById(R.id.DateAB);
        AddBus = findViewById(R.id.AddBusAB);
        FromLocation = findViewById(R.id.StartLocationAB);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading data...");
        progressDialog.setMessage("Just a Second...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        sendingData = new ProgressDialog(this);
        sendingData.setTitle("Saving data to database");
        sendingData.setCancelable(false);



    }


    private void DateTimePicker(){
        StartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartTime =true;
                SetTime();
            }
        });

        EndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartTime = false;
                SetTime();

            }
        });


    }


    private void setDateToEditText() {
        DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                String myFormat = "dd MMM yyyy";
                SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
                date = dateFormat.format(myCalendar.getTime());
                Date.setText(date);

            }
        };
        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(AddBuses.this, date1, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void FirebaseDataRetrieve(){
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference("Locations");
        iFirebaseLoadDone = this;
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<IDs> iDs = new ArrayList<>();

                for (DataSnapshot idSnapShot:dataSnapshot.getChildren()){
                    iDs.add(idSnapShot.getValue(IDs.class));
                }
                iFirebaseLoadDone.onFirebaseLoadSuccess(iDs);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                iFirebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());
            }
        });
    }

    private void Confirming(){
        AddBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner();
                String busNumber = BusNumber.getText().toString();
                String dbLocationDeparture = fromLocation;
                String dbLocationDestination = toLocation;
                String AB_NoOfSeatSt = SeatAvailable.getText().toString();

                if(!dbLocationDeparture.equals(dbLocationDestination)){

                    if(!busType.isEmpty() && !busNumber.isEmpty() && !dbLocationDeparture.isEmpty()
                            &&!dbLocationDestination.isEmpty() &&!startTime.isEmpty()&&!AB_NoOfSeatSt.isEmpty()&&!date.isEmpty()&&!Price.getText().toString().isEmpty()){

                        sendingData.show();
                        SendBusData(busType,busNumber,fromLocation,toLocation,startTime,endTime,date,AB_NoOfSeatSt);

                    }else {
                        Toast.makeText(AddBuses.this,"Please fill each box",Toast.LENGTH_SHORT).show();

                    }

                }else {
                    Toast.makeText(AddBuses.this,"Location is repeated",Toast.LENGTH_SHORT).show();


                }






            }
        });

    }

    private void SendBusData(String busType,String dbBusNumber,
                             String fromLocation,String toLocation,
                             String StartTime,String EndTime,String date,String busNo){
        String  route = (fromLocation + toLocation);
        DatabaseReference storingData = FirebaseDatabase.getInstance().getReference().
                child("Buses").child(date).child(route).child(dbBusNumber);
        String Ticket_price = Price.getText().toString();
        HashMap<String, String> loci = new HashMap<>();
        loci.put("FromLocation",fromLocation);
        loci.put("ToLocation",toLocation);
        loci.put("StartTime",StartTime);
        loci.put("EndTime",EndTime);
        loci.put("Date",date);
        loci.put("BusType",busType.toLowerCase());
        loci.put("BusNo",dbBusNumber);
        loci.put("NumberOfSeat",busNo);
        loci.put("TicketPrice",Ticket_price);
        storingData.setValue(loci).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    sendingData.dismiss();
                    Toast.makeText(AddBuses.this,"Bus Added Successfully..." ,Toast.LENGTH_SHORT).show();
                }else {
                    sendingData.dismiss();
                    Toast.makeText(AddBuses.this,"Failed to Add bus, Please try Again Later" ,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void spinner(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.BusType,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BusType.setAdapter(adapter);
        BusType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                busType =adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        FromLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                IDs iD = iDs.get(i);
                fromLocation = iD.getPlace();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




        ToLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IDs iD = iDs.get(position);
                toLocation = iD.getPlace();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {



            }
        });



    }



    private void SetTime(){
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(AddBuses.this, new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                String am_pm;
                if (i>12){
                    i = i-12;
                    am_pm = "PM";
                }else am_pm = "AM";

                if (isStartTime){
                    StartTime.setText(i+":"+i1+" "+am_pm);
                    startTime = (i+":"+i1+" "+am_pm);
                }else {
                    EndTime.setText(i+":"+i1+" "+am_pm);
                    endTime = (i+":"+i1+" "+am_pm);
                }
            }
        }, hour, minute,false);
        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();

    }


    @Override
    public void onFirebaseLoadSuccess(List<IDs> LocationList) {

        iDs = LocationList;
        List<String> id_list= new ArrayList<>();
        for(IDs id: LocationList){
            id_list.add(id.getPlace());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,id_list);

            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,id_list);
            FromLocation.setAdapter(adapter);
            ToLocation.setAdapter(adapter2);
            progressDialog.dismiss();
        }
    }

    @Override
    public void onFirebaseLoadFailed(String Message) {
        progressDialog.dismiss();
        Toast.makeText(AddBuses.this,Message,Toast.LENGTH_LONG).show();

    }
}

package com.ajinkya.easygo_admin;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class AddLocations extends AppCompatActivity {
    private EditText locality, locationPin;
    private Button addLoc;
    private String Locality, LocationPin, state;

    private ProgressDialog progressDialog;

    private DatabaseReference databaseAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_locations);

        Initialize();
        Buttons();
    }

    private void Buttons() {
        addLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean filled = checkInput();
                if (filled) {
                    progressDialog = new ProgressDialog(AddLocations.this);
                    progressDialog.setTitle("Registering");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    SetLocation(Locality, LocationPin, state);
                }else {
                    Toast.makeText(AddLocations.this, "Please Fill All Details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SetLocation(String Location, String PinCode, String State) {

        HashMap<String, String> loci = new HashMap<>();
        loci.put("Location_pin", PinCode);
        loci.put("Place", Location.toUpperCase());
        loci.put("State", State.toUpperCase());
        Log.e(TAG, "SetLocation: " + loci);

        databaseAddress.child(PinCode).setValue(loci).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddLocations.this, "Location Added Successfully", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    clearEditText();
                } else {
                    Toast.makeText(AddLocations.this, "Failed TO Add Location", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }


    private Boolean checkInput() {
        Locality = locality.getText().toString();
        LocationPin = locationPin.getText().toString();

        if (Locality.isEmpty()) {
            locality.setError("Enter locality");
            return false;
        } else if (LocationPin.isEmpty()) {
            locationPin.setError("Enter Location Pin");
            return false;
        } else if (state.isEmpty()) {
            Toast.makeText(this, "Please Select State", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }

    private void Initialize() {
        Toolbar toolbar = findViewById(R.id.toolbarLoc);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        locality = findViewById(R.id.LocalityLoc);
        locationPin = findViewById(R.id.LocationPinLoc);
        Spinner stateSpinner = findViewById(R.id.StateSpinnerLoc);
        addLoc = findViewById(R.id.AddLoc);


        String[] State = new String[]{"Maharashtra", "Bihar", "Jharkhand", "Delhi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(AddLocations.this, android.R.layout.simple_spinner_item, State);
        stateSpinner.setAdapter(adapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                state = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        databaseAddress = FirebaseDatabase.getInstance().getReference().child("Locations");

    }

    private void clearEditText() {
        locality.setText("");
        locationPin.setText("");
    }


}

package com.ajinkya.easygo_admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {

    private CardView addLocation, addBus, viewBuses, viewPassengers, viewUsers, support;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Initialize();
        Buttons();
    }

    private void Buttons() {
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddLocations.class);
                startActivity(intent);
            }
        });
        addBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AddBuses.class);
                startActivity(intent);
            }
        });
        viewBuses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ViewBusActivity.class);
                startActivity(intent);
            }
        });
        viewPassengers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, PassengerScreen1.class);
                startActivity(intent);
            }
        });

        viewUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ViewUsersActivity.class);
                startActivity(intent);
            }
        });


    }

    private void Initialize() {
        Toolbar toolbar = findViewById(R.id.HomeToolbar);
        toolbar.setTitle("EasyGo");
        setSupportActionBar(toolbar);


        addLocation = findViewById(R.id.AddLocationCard);
        addBus = findViewById(R.id.AddBusCard);
        viewBuses = findViewById(R.id.ViewBusesCard);
        viewPassengers = findViewById(R.id.ViewPassengersCard);
        viewUsers = findViewById(R.id.ViewUsersCard);
        support = findViewById(R.id.SupportCard);

    }
}

package com.ajinkya.easygo_admin;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Adapters.PassengerScreenBusAdapter;
import com.ajinkya.easygo_admin.Model.BusModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class PassengerScreen1 extends AppCompatActivity {
    private EditText Date;
    private Button FindBtn;
    private RecyclerView recyclerView;

    private ArrayList<BusModel> busModelArrayList;
    private ProgressDialog progressDialog;

    private String date;
    PassengerScreenBusAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_screen1);

        Initialize();
        Buttons();
    }

    protected void onStart() {
        super.onStart();
        Date.setText("");
        date=null;
    }

    private void Buttons() {
        FindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Date.getText().toString().isEmpty()){
                    progressDialog.show();
                    busModelArrayList = new ArrayList<>();
                    adapter = new PassengerScreenBusAdapter(PassengerScreen1.this,busModelArrayList);
                    recyclerView.setAdapter(adapter);
                    FirebaseDataRetrieve();
                }
                else {
                    Toast.makeText(PassengerScreen1.this, "Please Select Date", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void FirebaseDataRetrieve() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Tickets").child("AdminSideCheck").child(date);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                assert key != null;
                String[] strings = key.split(",");
                BusModel busModel = new BusModel();
                busModel.setBusNo(strings[0]);
                busModel.setFromLocation(strings[1]);
                busModel.setToLocation(strings[2]);
                busModel.setDepartureTime(strings[3]);
                busModel.setArrivalTime(strings[4]);
                busModel.setDate(date);
                busModelArrayList.add(busModel);
                adapter.notifyDataSetChanged();
                Log.e(TAG,"Strings are "+strings[0]+strings[1]+strings[2]);

                Log.e(TAG, "onChildAdded: "+key);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        progressDialog.dismiss();
    }


    private void Initialize() {
        Date = findViewById(R.id.DatePS1);
        FindBtn = findViewById(R.id.FindPS1);
        recyclerView = findViewById(R.id.RecyclerViewPS1);



        Toolbar toolbar = findViewById(R.id.ToolbarRPS1);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //Seating datePicker to EditText
        Calendar myCalendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date1 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, day);
                String myFormat = "dd MMM yyyy";
                SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat, Locale.US);
                date = dateFormat.format(myCalendar.getTime());
                Date.setText(date);

            }
        };
        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(PassengerScreen1.this, date1, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Searching for Buses...");
        progressDialog.setMessage("Just a Movement..");
        progressDialog.setCancelable(false);

    }
}

package com.ajinkya.easygo_admin.Interface;
import com.ajinkya.easygo_admin.Model.IDs;

import java.util.List;

public interface IFirebaseLoadDone {
    void onFirebaseLoadSuccess(List<IDs> LocationList);
    void onFirebaseLoadFailed(String Message);
}




package com.ajinkya.easygo_admin.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Model.UserModel;
import com.ajinkya.easygo_admin.R;

import java.util.ArrayList;

public class PassengerAdapter extends RecyclerView.Adapter<PassengerAdapter.ViewHolder> {
    ArrayList<UserModel> userModelArrayList;

    public PassengerAdapter(ArrayList<UserModel> userModelArrayList) {
        this.userModelArrayList = userModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.passenger_info, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel userModel = userModelArrayList.get(position);
        holder.name.setText(userModel.getName());
        holder.phoneNo.setText(userModel.getPhoneNo());
        holder.seatNo.setText(userModel.getSeat());


    }

    @Override
    public int getItemCount() {
        return userModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name,phoneNo, seatNo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.NameRPS2);
            this.phoneNo = itemView.findViewById(R.id.PhoneNoRPS2);
            this.seatNo = itemView.findViewById(R.id.SeatNoRPS2);
        }
    }
}


package com.ajinkya.easygo_admin.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Model.BusModel;
import com.ajinkya.easygo_admin.PassengerScreen2;
import com.ajinkya.easygo_admin.R;

import java.util.ArrayList;

public class PassengerScreenBusAdapter extends RecyclerView.Adapter<PassengerScreenBusAdapter.ViewHolder>{
    ArrayList<BusModel> busModelArrayList;
    Context context;

    public PassengerScreenBusAdapter(Context context ,ArrayList<BusModel> List){
        this.context= context;
        this.busModelArrayList = List;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.bus_info_ps1, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BusModel busModel = busModelArrayList.get(position);
        holder.BusNo.setText(busModel.getBusNo());
        holder.Date.setText(busModel.getDate());
        holder.FromLocation.setText(busModel.getFromLocation());
        holder.ToLocation.setText(busModel.getToLocation());
        holder.StartTime.setText(busModel.getDepartureTime());
        holder.EndTime.setText(busModel.getArrivalTime());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String BusKey = busModel.getBusNo()+","+busModel.getFromLocation()+","+busModel.getToLocation()+","+busModel.getDepartureTime()+","+busModel.getArrivalTime();
                Intent intent = new Intent(context, PassengerScreen2.class);
                intent.putExtra("BusKey",BusKey);
                intent.putExtra("Date",busModel.getDate());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return busModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView BusNo,Date,FromLocation,ToLocation, StartTime,EndTime;
        public LinearLayout cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.cardView = itemView.findViewById(R.id.CardViewRPS1);
            this.BusNo = itemView.findViewById(R.id.BusNumberRPS1);
            this.Date = itemView.findViewById(R.id.DateRPS1);
            this.FromLocation = itemView.findViewById(R.id.FromLocationRPS1);
            this.ToLocation = itemView.findViewById(R.id.ToLocationRPS1);
            this.StartTime = itemView.findViewById(R.id.StartTimeRPS1);
            this.EndTime = itemView.findViewById(R.id.EndTimeRPS1);
        }
    }
}


package com.ajinkya.easygo_admin.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Model.UserModel;
import com.ajinkya.easygo_admin.R;

import java.util.ArrayList;

public class UserTableAdapter extends RecyclerView.Adapter<UserTableAdapter.ViewHolder> {
    Context context;

    public UserTableAdapter(Context context, ArrayList<UserModel> userModelArrayList) {
        this.context = context;
        this.userModelArrayList = userModelArrayList;
    }

    ArrayList<UserModel> userModelArrayList;


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vu_table_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!userModelArrayList.isEmpty()){
            UserModel userModel = userModelArrayList.get(position);
            holder.SrNo.setText(String.valueOf(position+1));
            holder.UserName.setText(userModel.getName());
            holder.MobileNo.setText(userModel.getPhoneNo());
            holder.EmailID.setText(userModel.getEmail());
        }
    }

    @Override
    public int getItemCount() {
        return userModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView SrNo, UserName, MobileNo, EmailID;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            SrNo = itemView.findViewById(R.id.SrNoTBU);
            UserName = itemView.findViewById(R.id.UserNameTBU);
            MobileNo = itemView.findViewById(R.id.MobileNoTBU);
            EmailID = itemView.findViewById(R.id.EmailIdTBU);
        }
    }
}


package com.ajinkya.easygo_admin.Adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajinkya.easygo_admin.Model.BusModel;
import com.ajinkya.easygo_admin.R;


import java.util.ArrayList;

public class BusViewTableAdapter extends RecyclerView.Adapter<BusViewTableAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<BusModel> busModelArrayList;

    public BusViewTableAdapter(Context context, ArrayList<BusModel> busModelArrayList) {
        this.context = context;
        this.busModelArrayList = busModelArrayList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.vb_table_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!busModelArrayList.isEmpty()){
            BusModel busModel = busModelArrayList.get(position);
            holder.SerialNumber.setText(String.valueOf(position+1));
            holder.Date1.setText(busModel.getDate());
            holder.BusNo.setText(busModel.getBusNo());
            holder.FromLocation.setText(busModel.getFromLocation());
            holder.ToLocation.setText(busModel.getToLocation());
            holder.StartTime.setText(busModel.getDepartureTime());
            holder.EndTime.setText(busModel.getArrivalTime());
            holder.SeatType.setText(busModel.getTypeSit());
            holder.AvailableSeats.setText(busModel.getAvailableSeat());
            holder.TicketPrice.setText(busModel.getTicketPrice());
            Log.e(TAG, "onBindViewHolder: "+busModel.getBusNo() );
        }

    }

    @Override
    public int getItemCount() {
        return busModelArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView SerialNumber,Date1,BusNo,FromLocation,ToLocation,StartTime,EndTime,SeatType,AvailableSeats,TicketPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            SerialNumber = itemView.findViewById(R.id.SrNoTBV);
            Date1 = itemView.findViewById(R.id.DateTBV);
            BusNo = itemView.findViewById(R.id.BusNoTBV);
            FromLocation = itemView.findViewById(R.id.FromLocationTBV);
            ToLocation = itemView.findViewById(R.id.ToLocationTBV);
            StartTime = itemView.findViewById(R.id.StartTimeTBV);
            EndTime = itemView.findViewById(R.id.EndTimeTBV);
            SeatType = itemView.findViewById(R.id.SeatTypeTBV);
            AvailableSeats = itemView.findViewById(R.id.AvailableSeatsTBV);
            TicketPrice = itemView.findViewById(R.id.TicketPriceTBV);
        }
    }
}


package com.ajinkya.easygo_admin.Model;

public class BusModel {
    public String ToLocation;
    public String arrivalTime;
    public String busNo;
    public String date;
    public String FromLocation;
    public String departureTime;
    public String typeSit;
    public String ticketPrice;
    public String AvailableSeat;




    public BusModel(){

    }

    public BusModel(String arrivalPin, String arrivalTime, String busNo,
                    String date, String departurePin, String departureTime,
                    String typeSit, String ticketPrice) {
        this.ToLocation = arrivalPin;
        this.arrivalTime = arrivalTime;
        this.busNo = busNo;
        this.date = date;
        this.FromLocation = departurePin;
        this.departureTime = departureTime;
        this.typeSit = typeSit;
        this.ticketPrice = ticketPrice;
    }


    public String getAvailableSeat() {
        return AvailableSeat;
    }

    public void setAvailableSeat(String availableSeat) {
        AvailableSeat = availableSeat;
    }

    public String getToLocation() {
        return ToLocation;
    }

    public void setToLocation(String toLocation) {
        this.ToLocation = toLocation;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }


    public String getBusNo() {
        return busNo;
    }

    public void setBusNo(String busNo) {
        this.busNo = busNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFromLocation() {
        return FromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.FromLocation = fromLocation;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getTypeSit() {
        return typeSit;
    }

    public void setTypeSit(String typeSit) {
        this.typeSit = typeSit;
    }

    public String getTicketPrice(){
        return ticketPrice;
    }

    public void setTicketPrice(String  ticketPrice){
        this.ticketPrice = ticketPrice;

    }

}


package com.ajinkya.easygo_admin.Model;

public class UserModel {
    public String Name;
    public String PhoneNo;
    public String Email;
    public String Seat;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoneNo() {
        return PhoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        PhoneNo = phoneNo;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getSeat() {
        return Seat;
    }

    public void setSeat(String seat) {
        Seat = seat;
    }

}


package com.ajinkya.easygo_admin.Model;

public class IDs {
    private String Location_pin,Place;
    public IDs(){
    }
    public IDs(String Location_pin,String Place ){
        this.Location_pin = Location_pin;
        this.Place = Place;
    }
    public String getLocation_pin() {
        return Location_pin;
    }

    public void setLocation_pin(String Location_pin) {
        this.Location_pin = Location_pin;
    }
    public String getPlace() {
        return Place;
    }
    public void setPlace(String Place) {
        this.Place = Place;
    }

}


