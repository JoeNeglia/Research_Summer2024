package com.example.smartmoney;

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
        assertEquals("com.example.smartmoney", appContext.getPackageName());
    }
}

package com.example.smartmoney;

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

package com.example.smartmoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

public class Profile extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    Button logoutBtn;
    TextView userName,userEmail,userId;
    ImageView profileImage;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logoutBtn = findViewById(R.id.logoutBtn);
        userName = findViewById(R.id.name);
        userEmail = findViewById(R.id.email);
        userId = findViewById(R.id.userId);
        profileImage = findViewById(R.id.profileImage);

        gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()){
                                    gotoMainActivity();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Session not close",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr= Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if(opr.isDone()){
            GoogleSignInResult result=opr.get();
            handleSignInResult(result);
        }else{
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }
    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account=result.getSignInAccount();
            userName.setText(account.getDisplayName());
            userEmail.setText(account.getEmail());
            userId.setText(account.getId());
            try{
                Glide.with(this).load(account.getPhotoUrl()).into(profileImage);
            }catch (NullPointerException e){
                Toast.makeText(getApplicationContext(),"image not found",Toast.LENGTH_LONG).show();
            }

        }else{
            gotoMainActivity();
        }
    }
    private void gotoMainActivity(){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}


package com.example.smartmoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class Dashboard_Activity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NavigationView.OnNavigationItemSelectedListener{

    BottomNavigationView bottomNavigationView;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        drawerLayout= findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Smart Money");
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Dashboard_fragment dashboard_fragment = new Dashboard_fragment();
        Income_fragment income_fragment = new Income_fragment();
        Expense_fragment expense_fragment = new Expense_fragment();
        Analytics_Fragment analytics_fragment =new Analytics_Fragment();
        Profile_Fragment profile_fragment = new Profile_Fragment();
        About_Fragment about_fragment=new About_Fragment();
        FeedbackFragment feedbackFragment = new FeedbackFragment();



        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.dashBoard);
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, dashboard_fragment).commit();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.dashBoard:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, dashboard_fragment).commit();
                        return true;

                    case R.id.income:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, income_fragment).commit();
                        return true;

                    case R.id.expense:
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, expense_fragment).commit();
                        return true;
                }
                return false;
            }
        });



        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);

        if(drawerLayout.isDrawerOpen(GravityCompat.END)){
            drawerLayout.closeDrawer(GravityCompat.END);
        }else{
            super.onBackPressed();
        }
    }

    public void displaySelectedListener(int itemId){
        Fragment fragment = null;
        switch (itemId){
            case R.id.dashBoard_menu:
                fragment = new Dashboard_fragment();
                bottomNavigationView.setVisibility(View.VISIBLE);
                break;

            case R.id.analytics_menu:
                fragment = new Analytics_Fragment();
                bottomNavigationView.setVisibility(View.INVISIBLE);
                break;

            case R.id.Profile_menu:
                fragment=new Profile_Fragment();
                bottomNavigationView.setVisibility(View.INVISIBLE);
                break;

            case R.id.logout_menu:
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                break;

            case R.id.about_menu:
                fragment=new About_Fragment();
                bottomNavigationView.setVisibility(View.INVISIBLE);
                break;

            case R.id.feedback_menu:
                fragment =new FeedbackFragment();
                bottomNavigationView.setVisibility(View.INVISIBLE);
                break;
        }
        if (fragment!=null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flFragment,fragment);
            ft.commit();
        }
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        displaySelectedListener(item.getItemId());
        return true;
    }
}

package com.example.smartmoney;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Income_barchart_Activity extends AppCompatActivity {

    Toolbar toolbar;
    BarChart barChart;
    ArrayList<BarEntry> barEntryArrayList;
    ArrayList<String> strings;
    ArrayList<Data> typeandamount_arraylist ;
    FirebaseAuth mAuth;
    DatabaseReference mincomeDatabase;
    String TAG = "Income_barchart_Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_barchart);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Smart Money");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        barChart = findViewById(R.id.income_barChart);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser =mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mincomeDatabase = FirebaseDatabase.getInstance().getReference().child("Income_Data").child(uid);

        barEntryArrayList = new ArrayList<>();
        strings = new ArrayList<>();


       // typeandamount_arraylist.clear();

        mincomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                typeandamount_arraylist =  new ArrayList<>();
                for (DataSnapshot mysnapshot: snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);
                    typeandamount_arraylist.add(new Data(data.getType(),data.getAmount()));
                }
                for (int i=0;i<typeandamount_arraylist.size();i++){
                    String type=typeandamount_arraylist.get(i).getType();
                    int amount = typeandamount_arraylist.get(i).getAmount();
                    barEntryArrayList.add(new BarEntry(i,amount));
                    // Log.i(TAG, "value of "+i+amount);
                    strings.add(type);
                }

                showchart(barEntryArrayList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    private void showchart(ArrayList<BarEntry> barEntryArrayList) {
        BarDataSet barDataSet = new BarDataSet(barEntryArrayList,"Income Data");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLUE);
        barDataSet.setValueTextSize(12f);
        Description description = new Description();
        description.setText("Income");
        barChart.setDescription(description);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2500);
        barChart.invalidate();
        XAxis xAxis=barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(strings));
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(strings.size());
        xAxis.setLabelRotationAngle(270);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

package com.example.smartmoney;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class Expense_fragment extends Fragment {

    EditText edt_expense_type, edt_expense_amount, edt_expense_note;
    Button add_expense;

    FirebaseAuth mAuth;
    DatabaseReference mexpenseDatabase;

    RecyclerView recyclerView;

    TextView expanse_total_txt;


    private EditText edt_Amount;
    private EditText edt_Type;
    private EditText edt_Note;

    private Button btn_Update;
    private Button btn_Cancel;

    private String type;
    private String note;
    private int amount;

    private String post_key;
    private String post_key_delete;


    public Expense_fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expense_fragment, container, false);

        edt_expense_type = view.findViewById(R.id.edt_expence_type);
        edt_expense_amount = view.findViewById(R.id.edt_expence_amount);
        edt_expense_note = view.findViewById(R.id.edt_expence_note);
        add_expense = view.findViewById(R.id.btn_add_expence);
        expanse_total_txt = view.findViewById(R.id.expense_txt_result);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mexpenseDatabase = FirebaseDatabase.getInstance().getReference().child("Expense_Data").child(uid);

        recyclerView = view.findViewById(R.id.recycleview_expanse);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        add_expense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String expense_type = edt_expense_type.getText().toString().trim();
                String expense_amount = edt_expense_amount.getText().toString().trim();
                String expense_note = edt_expense_note.getText().toString().trim();
                int amount_expense = Integer.parseInt(expense_amount);

                if (expense_type.isEmpty()) {
                    edt_expense_type.setError("income type is empty");
                    edt_expense_type.requestFocus();
                    return;
                }
                if (expense_amount.isEmpty()) {
                    edt_expense_amount.setError("amount is empty");
                    edt_expense_amount.requestFocus();
                    return;
                }
                if (expense_note.isEmpty()) {
                    edt_expense_note.setError("note is empty");
                    edt_expense_note.requestFocus();
                    return;
                }


                String id = mexpenseDatabase.push().getKey();

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(amount_expense, expense_type, expense_note, id, mDate);

                mexpenseDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data added", Toast.LENGTH_LONG).show();

            }




        });


        mexpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalValue_expanse = 0;


                for (DataSnapshot mysnapshot : snapshot.getChildren()) {

                    Data data = mysnapshot.getValue(Data.class);

                    totalValue_expanse += data.getAmount();
                    String stTotalvalue_expanse = String.valueOf(totalValue_expanse);

                    expanse_total_txt.setText(stTotalvalue_expanse);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }




    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mexpenseDatabase, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Data model) {

                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());
                holder.setAmount(model.getAmount());

                ImageButton imageButton_expense = holder.mView.findViewById(R.id.delete_img_expense);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        post_key = getRef(position).getKey();
                        type = model.getType();
                        note = model.getNote();
                        amount = model.getAmount();

                        updateDataitem();
                    }
                });


                imageButton_expense.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder d_builder = new AlertDialog.Builder(holder.mView.getContext());
                        d_builder.setTitle("Are You Sure ?");
                        d_builder.setMessage("Deleted Data can't be Undo");

                        d_builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                post_key_delete=getRef(position).getKey();
                                mexpenseDatabase.child(post_key_delete).removeValue();
                            }
                        });

                        d_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        d_builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_recycler_data, parent, false);
                return new MyViewHolder(view);
            }
        };


        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }


        private void setType(String type) {
            TextView mType = mView.findViewById(R.id.type_txt_expense);
            mType.setText(type);
        }

        private void setNote(String note) {
            TextView mNote = mView.findViewById(R.id.note_txt_expense);
            mNote.setText(note);
        }

        private void setDate(String date) {
            TextView mDate = mView.findViewById(R.id.date_txt_expense);
            mDate.setText(date);
        }

        private void setAmount(int amount) {
            TextView mAmount = mView.findViewById(R.id.amount_txt_expense);
            String stamount = String.valueOf(amount);
            mAmount.setText(stamount);
        }
    }


    private void updateDataitem() {
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.custom_layout_for_dialogbox, null);
        mydialog.setView(myview);

        edt_Amount = myview.findViewById(R.id.amount_edt);
        edt_Type = myview.findViewById(R.id.type_edt);
        edt_Note = myview.findViewById(R.id.note_edt);

        edt_Type.setText(type);
        edt_Type.setSelection(type.length());

        edt_Note.setText(note);
        edt_Note.setSelection(note.length());

        edt_Amount.setText(String.valueOf(amount));
        edt_Amount.setSelection(String.valueOf(amount).length());


        btn_Update = myview.findViewById(R.id.btn_update);
         btn_Cancel= myview.findViewById(R.id.btn_cancel);

        AlertDialog dialog = mydialog.create();

        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = edt_Type.getText().toString().trim();
                note = edt_Note.getText().toString().trim();

                String mdamount = String.valueOf(amount);

                mdamount = edt_Amount.getText().toString().trim();

                if (type.isEmpty()) {
                    edt_Type.setError("Expanse type is empty");
                    edt_Type.requestFocus();
                    return;
                }
                if (mdamount.isEmpty()) {
                    edt_Amount.setError("amount is empty");
                    edt_Amount.requestFocus();
                    return;
                }
                if (note.isEmpty()) {
                    edt_Note.setError("note is empty");
                    edt_Note.requestFocus();
                    return;
                }

                int myAmount = Integer.parseInt(mdamount);

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(myAmount, type, note, post_key, mDate);

                mexpenseDatabase.child(post_key).setValue(data);

                dialog.dismiss();
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

}

package com.example.smartmoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    //google authentication
    private static final String TAG = "MainActivity";
    private SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    private static final int RC_SIGN_IN = 1;
    String name, email;
    String idToken;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


    //phone no authentication
    private FirebaseAuth mAuth;
    private EditText edtPhone, edtOTP;
    private Button verifyOTPBtn, generateOTPBtn;
    private String verificationId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //google authentication

        firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        //this is where we start the Auth state Listener to listen for whether the user is signed in or not
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get signedIn user
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //if user is signed in, we call a helper method to save the user details to Firebase
                if (user != null) {
                    // User is signed in
                    // you could place other firebase code
                    //logic to save the user details to Firebase
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        GoogleSignInOptions gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });





        //phone no authentication
        mAuth = FirebaseAuth.getInstance();

        edtPhone = findViewById(R.id.phoneno);
        edtOTP = findViewById(R.id.otp_edittext);
        generateOTPBtn = findViewById(R.id.send_otp);
        verifyOTPBtn = findViewById(R.id.verify_otp);


        generateOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtPhone.getText().toString())) {

                    Toast.makeText(MainActivity.this, "Please enter a valid phone number.", Toast.LENGTH_SHORT).show();
                } else {
                    String phone = "+91" + edtPhone.getText().toString();
                    sendVerificationCode(phone);
                }
            }
        });



        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    verifyCode(edtOTP.getText().toString());
                }
            }
        });


    }

    //phone no authentication


    private void signInWithCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent i = new Intent(MainActivity.this, Fill_Details.class);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void sendVerificationCode(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallBack)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
                edtOTP.setText(code);

                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }


    //google authentication

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            idToken = account.getIdToken();
            name = account.getDisplayName();
            email = account.getEmail();
            // you can store user data to SharedPreference
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuthWithGoogle(credential);
        }else{
            // Google Sign In failed, update UI appropriately
            Log.e(TAG, "Login Unsuccessful. "+result);
            Toast.makeText(this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
        }
    }
    private void firebaseAuthWithGoogle(AuthCredential credential){

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(),Fill_Details.class);
                            startActivity(i);
                            // gotoProfile();
                        }else{
                            Log.w(TAG, "signInWithCredential" + task.getException().getMessage());
                            task.getException().printStackTrace();
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }




    private void gotoProfile(){
        Intent intent = new Intent(MainActivity.this, Profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (authStateListener != null){
            FirebaseAuth.getInstance().signOut();
        }
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}

package com.example.smartmoney;

public class User {

    private String FName;
    private String LName;
    private String Email;
    private String Address;
    private String Gender;
    private String Prof_list;

    public User(){
    }



    public String getFName() {
        return FName;
    }

    public void setFName(String FName) {
        this.FName = FName;
    }

    public String getLName() {
        return LName;
    }

    public void setLName(String LName) {
        this.LName = LName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getProf_list() {
        return Prof_list;
    }

    public void setProf_list(String prof_list) {
        Prof_list = prof_list;
    }
}


package com.example.smartmoney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ConstraintLayout constraintLayout = findViewById(R.id.main_layout);

        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                finish();
            }
        },9000);
    }
}

package com.example.smartmoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Expanse_piechart_Activity extends AppCompatActivity {

    Toolbar toolbar;
    private PieChart pieChart;
    private FirebaseAuth mAuth;
    private DatabaseReference mexpenseDatabase;
    private ArrayList<PieEntry> pieEntryArrayList;
    private ArrayList<Data> dataArrayList ;
    private ArrayList<String> strings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanse_piechart);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Smart Money");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        pieChart=findViewById(R.id.expense_pie_chart);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mexpenseDatabase = FirebaseDatabase.getInstance().getReference().child("Expense_Data").child(uid);

        pieEntryArrayList = new ArrayList<>();
        strings= new ArrayList<>();


        mexpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataArrayList =  new ArrayList<>();
                for (DataSnapshot mysnapshot: snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);
                    dataArrayList.add(new Data(data.getType(),data.getAmount()));
                }
                for (int i=0;i<dataArrayList.size();i++){
                    String type=dataArrayList.get(i).getType();
                    int amount = dataArrayList.get(i).getAmount();

                    // Log.i(TAG, "value of "+i+amount);
                    //strings.add(type);
                    pieEntryArrayList.add(new PieEntry(amount,type));
                }


                showchart(pieEntryArrayList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void showchart(ArrayList<PieEntry> pieEntryArrayList) {

        PieDataSet dataSet = new PieDataSet(pieEntryArrayList,"Expense");

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS){
            colors.add(color);
        }

        for (int color: ColorTemplate.VORDIPLOM_COLORS){
            colors.add(color);
        }

        dataSet.setColors(colors);

        PieData dataa = new PieData(dataSet);
        dataa.setDrawValues(true);
        dataa.setValueFormatter(new PercentFormatter(pieChart));
        dataa.setValueTextSize(12f);
        dataa.setValueTextColors(Collections.singletonList(Color.BLUE));

        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("EXPENSE");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("Expense Data");
        pieChart.setDescription(description);
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
        pieChart.setData(dataa);
        pieChart.animateX(2500);
        pieChart.invalidate();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

package com.example.smartmoney;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


public class FeedbackFragment extends Fragment {

    EditText to, feedback_msg;
    Button send;
    RatingBar ratingBar;
    TextView rate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        to=view.findViewById(R.id.email_feedback);
        feedback_msg=view.findViewById(R.id.feedback_msg);
        send=view.findViewById(R.id.btn_feedback_send);
        ratingBar= view.findViewById(R.id.rating_bar);
        rate= view.findViewById(R.id.txt_rate);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW
                , Uri.parse("mailto:"+to.getText().toString()));
                intent.putExtra(Intent.EXTRA_SUBJECT,"Feedback to Smart Money");
                intent.putExtra(Intent.EXTRA_TEXT,feedback_msg.getText().toString());
                startActivity(intent);
            }
        });


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                rate.setText("Rate :"+v);
                Toast.makeText(getActivity(), "Thanks For Rating", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}

package com.example.smartmoney;

public class Data {
    private int amount;
    private String type;
    private String note;
    private String id;
    private String date;

    public Data(){

    }

    public Data(int amount, String income_type, String income_note, String id, String mDate) {
        this.amount = amount;
        this.type = income_type;
        this.note= income_note;
        this.date= mDate;
        this.id = id;
    }

    public Data(String type, int amount) {
        this.type=type;
        this.amount=amount;
    }




    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}


package com.example.smartmoney;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Profile_Fragment extends Fragment {

    TextView t_fname,t_lname,t_email,t_profession,t_address,t_gender;
    FirebaseDatabase db;
    DatabaseReference root;
    FirebaseUser firebaseUser;
    String uid;
    User user;
    Button update, logout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_, container, false);
        db = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = firebaseUser.getUid();
        root = db.getReference().child("Users").child(uid);
        user = new User();

        t_fname=view.findViewById(R.id.profile_fname);
        t_lname=view.findViewById(R.id.profile_lname);
        t_email=view.findViewById(R.id.profile_email);
        t_profession=view.findViewById(R.id.profile_profession);
        t_address=view.findViewById(R.id.profile_address);
        t_gender=view.findViewById(R.id.profile_gender);
        update=view.findViewById(R.id.update_profile);
        logout=view.findViewById(R.id.logout_profile_btn);


        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                t_fname.setText("First Name :"+" "+user.getFName());
                t_lname.setText("Last name :"+" "+user.getLName());
                t_email.setText("Email :"+" "+user.getEmail());
                t_profession.setText("Profession :"+" "+user.getProf_list());
                t_address.setText("Address :"+" "+user.getAddress());
                t_gender.setText("Gender :"+" "+user.getGender());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(getActivity(),Fill_Details.class);
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =  new Intent(getActivity(),MainActivity.class);
                startActivity(i);
            }
        });


        return view;
    }
}

package com.example.smartmoney;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class Analytics_Fragment extends Fragment {

    Button incomebar,incomepie,expansebar,expansepie;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_analytics_, container, false);


        incomebar=view.findViewById(R.id.btn_income_bar);
        incomepie=view.findViewById(R.id.btn_income_pie);
        expansebar=view.findViewById(R.id.btn_expanse_bar);
        expansepie=view.findViewById(R.id.btn_expanse_pie);

        incomebar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),Income_barchart_Activity.class));
            }
        });

        incomepie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),Income_piechart_Activity.class));
            }
        });

        expansebar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),Expanse_barchart_Activity.class));
            }
        });

        expansepie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),Expanse_piechart_Activity.class));
            }
        });
        return view;
    }
}

package com.example.smartmoney;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Dashboard_fragment extends Fragment {

    private TextView totalIncome_txt;
    private TextView totalExpense_txt;

    FirebaseAuth mAuth;
    DatabaseReference mincomeDatabase;
    DatabaseReference mexpenseDatabase;


    FirebaseDatabase db;
    DatabaseReference root;
    FirebaseUser firebaseUser;
    String uid;
    User user;
    TextView username_display;


    private RecyclerView recyclerView_income;
    private RecyclerView recyclerView_expanse;

    public Dashboard_fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_dashboard_fragment, container, false);



        db = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = firebaseUser.getUid();
        root = db.getReference().child("Users").child(uid);
        user = new User();
        username_display = myview.findViewById(R.id.username_txt);


        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username_display.setText(user.getFName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        totalIncome_txt = myview.findViewById(R.id.dashboard_income_sum_txt);
        totalExpense_txt = myview.findViewById(R.id.dashboard_expense_sum_txt);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uid = mUser.getUid();

        recyclerView_income = myview.findViewById(R.id.dashboard_recycleview_income);
        recyclerView_expanse = myview.findViewById(R.id.dashboard_recycleview_expanse);

        mincomeDatabase = FirebaseDatabase.getInstance().getReference().child("Income_Data").child(uid);
        mexpenseDatabase = FirebaseDatabase.getInstance().getReference().child("Expense_Data").child(uid);

        mincomeDatabase.keepSynced(true);
        mexpenseDatabase.keepSynced(true);

        mincomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalsum = 0;

                for (DataSnapshot mysnapshot : snapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);

                    totalsum += data.getAmount();

                    String s_total_income = String.valueOf(totalsum);

                    totalIncome_txt.setText(s_total_income);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mexpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalsum_expanse = 0;

                for (DataSnapshot mysnapshot : snapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);

                    totalsum_expanse += data.getAmount();

                    String s_expanse_total = String.valueOf(totalsum_expanse);

                    totalExpense_txt.setText(s_expanse_total);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        LinearLayoutManager layoutManager_income = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,false);
        layoutManager_income.setStackFromEnd(true);
        layoutManager_income.setReverseLayout(true);
        recyclerView_income.setHasFixedSize(true);
        recyclerView_income.setLayoutManager(layoutManager_income);


        LinearLayoutManager layoutManager_expanse = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        layoutManager_expanse.setStackFromEnd(true);
        layoutManager_expanse.setReverseLayout(true);
        recyclerView_expanse.setHasFixedSize(true);
        recyclerView_expanse.setLayoutManager(layoutManager_expanse);



        return myview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mincomeDatabase, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data,IncomeViewHolder> income_adapter = new FirebaseRecyclerAdapter<Data, IncomeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {
                holder.setIncomeType(model.getType());
                holder.setIncomeAmount(model.getAmount());
                holder.setIncomeDate(model.getDate());

            }
            @NonNull
            @Override
            public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view_income = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_income_recycler_data, parent, false);
                return new IncomeViewHolder(view_income);
            }
        };

        FirebaseRecyclerOptions<Data> options_expanse = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mexpenseDatabase, Data.class)
                .build();


        FirebaseRecyclerAdapter<Data,ExpanseViewHolder> expanse_adapter = new FirebaseRecyclerAdapter<Data, ExpanseViewHolder>(options_expanse) {
            @Override
            protected void onBindViewHolder(@NonNull ExpanseViewHolder holder, int position, @NonNull Data model) {
                holder.setExpanseType(model.getType());
                holder.setExpanseAmount(model.getAmount());
                holder.setExpanseDate(model.getDate());
            }

            @NonNull
            @Override
            public ExpanseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view_expanse = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_expanse_recycler_data, parent, false);
                return new ExpanseViewHolder(view_expanse);
            }
        };
        recyclerView_income.setAdapter(income_adapter);
       income_adapter.startListening();


        recyclerView_expanse.setAdapter(expanse_adapter);
        expanse_adapter.startListening();
    }


    public static class ExpanseViewHolder extends RecyclerView.ViewHolder{

        View mExpanseView;
        public ExpanseViewHolder(@NonNull View itemView) {
            super(itemView);
            mExpanseView=itemView;
        }

        public void setExpanseType(String type) {
            TextView mType_expanse = mExpanseView.findViewById(R.id.db_type_txt_expanse);
            mType_expanse.setText(type);
        }
        public void setExpanseAmount(int amount) {
            TextView mAmount_expanse = mExpanseView.findViewById(R.id.db_amount_txt_expanse);
            String stamount = String.valueOf(amount);
            mAmount_expanse.setText(stamount);
        }

        public void setExpanseDate(String date) {
            TextView mDate_expanse = mExpanseView.findViewById(R.id.db_date_txt_expanse);
            mDate_expanse.setText(date);
        }

    }



    public static class IncomeViewHolder extends RecyclerView.ViewHolder {

        View mIncomeView;

        public IncomeViewHolder(@NonNull View itemViewe) {
            super(itemViewe);
            mIncomeView = itemViewe;
        }

        public void setIncomeType(String type) {
            TextView mType_income = mIncomeView.findViewById(R.id.db_type_txt_income);
            mType_income.setText(type);
        }
        public void setIncomeAmount(int amount) {
            TextView mAmount_income = mIncomeView.findViewById(R.id.db_amount_txt_income);
            String stamount = String.valueOf(amount);
            mAmount_income.setText(stamount);
        }

        public void setIncomeDate(String date) {
            TextView mDate_income = mIncomeView.findViewById(R.id.db_date_txt_income);
            mDate_income.setText(date);
        }


    }
}

package com.example.smartmoney;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;



public class Income_fragment extends Fragment {

    EditText edt_income_type,edt_income_amount,edt_income_note;
    Button add_income;

    FirebaseAuth mAuth;
    DatabaseReference mincomeDatabase;

   // FirebaseAuth firebaseAuth;
   // DatabaseReference databaseReference;

    RecyclerView recyclerView;

    TextView income_total_txt;

    private EditText edt_Amount;
    private EditText edt_Type;
    private EditText edt_Note;

    private Button btn_Update;
    private Button btn_Cancel;

    private String type;
    private String note;
    private int amount;

    private String post_key;
    private String post_key_delete;





    public Income_fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income_fragment, container, false);

        edt_income_type = view.findViewById(R.id.edt_income_type);
        edt_income_amount = view.findViewById(R.id.edt_income_amount);
        edt_income_note = view.findViewById(R.id.edt_income_note);
        add_income = view.findViewById(R.id.btn_add_income);
        income_total_txt = view.findViewById(R.id.income_txt_result);



        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser =mAuth.getCurrentUser();
        String uid = mUser.getUid();

        mincomeDatabase = FirebaseDatabase.getInstance().getReference().child("Income_Data").child(uid);



        recyclerView = view.findViewById(R.id.recycleview_income);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);



        add_income.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String income_type = edt_income_type.getText().toString().trim();
                String income_amount = edt_income_amount.getText().toString().trim();
                String income_note = edt_income_note.getText().toString().trim();
                int amount = Integer.parseInt(income_amount);


                if(income_type.isEmpty())
                {
                    edt_income_type.setError("income type is empty");
                    edt_income_type.requestFocus();
                    return;
                }
                if(income_amount.isEmpty())
                {
                    edt_income_amount.setError("amount is empty");
                    edt_income_amount.requestFocus();
                    return;
                }
                if(income_note.isEmpty())
                {
                    edt_income_note.setError("note is empty");
                    edt_income_note.requestFocus();
                    return;
                }


                String id = mincomeDatabase.push().getKey();

                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(amount,income_type,income_note,id,mDate);

                mincomeDatabase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data added", Toast.LENGTH_LONG).show();
            }
        });


        mincomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int totalValue = 0;


                for (DataSnapshot mysnapshot: snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);

                    totalValue+= data.getAmount();
                    String stTotalvalue = String.valueOf(totalValue);

                    income_total_txt.setText(stTotalvalue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(mincomeDatabase, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data,MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull Data model) {

                holder.setType(model.getType());
                holder.setNote(model.getNote());
                holder.setDate(model.getDate());
                holder.setAmount(model.getAmount());

               ImageButton imageButton = holder.mView.findViewById(R.id.delete_img);



                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        post_key=getRef(position).getKey();
                        type=model.getType();
                        note=model.getNote();
                        amount=model.getAmount();

                        updateDataitem();
                    }
                });

               imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder d_builder = new AlertDialog.Builder(holder.mView.getContext());
                        d_builder.setTitle("Are You Sure ?");
                        d_builder.setMessage("Deleted Data can't be Undo");

                        d_builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                post_key_delete=getRef(position).getKey();
                                mincomeDatabase.child(post_key_delete).removeValue();
                            }
                        });

                        d_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        d_builder.show();
                    }
                });



            }
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.income_recycler_data, parent, false);
                return new MyViewHolder(view);
            }
        };


        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setType(String type){
            TextView mType = mView.findViewById(R.id.type_txt_income);
            mType.setText(type);
        }
       public void setNote(String note){
            TextView mNote = mView.findViewById(R.id.note_txt_income);
            mNote.setText(note);
        }
        public void setDate(String date){
            TextView mDate = mView.findViewById(R.id.date_txt_income);
            mDate.setText(date);
        }
        public void setAmount(int amount){
            TextView mAmount = mView.findViewById(R.id.amount_txt_income);
            String stamount = String.valueOf(amount);
            mAmount.setText(stamount);
        }
    }

    private void updateDataitem(){
        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myview = inflater.inflate(R.layout.custom_layout_for_dialogbox,null);
        mydialog.setView(myview);

        edt_Amount = myview.findViewById(R.id.amount_edt);
        edt_Type = myview.findViewById(R.id.type_edt);
        edt_Note = myview.findViewById(R.id.note_edt);

        edt_Type.setText(type);
        edt_Type.setSelection(type.length());

        edt_Note.setText(note);
        edt_Note.setSelection(note.length());

        edt_Amount.setText(String.valueOf(amount));
        edt_Amount.setSelection(String.valueOf(amount).length());


        btn_Update= myview.findViewById(R.id.btn_update);
        btn_Cancel = myview.findViewById(R.id.btn_cancel);

        AlertDialog dialog = mydialog.create();

        btn_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type= edt_Type.getText().toString().trim();
                note=edt_Note.getText().toString().trim();

                String mdamount= String.valueOf(amount);

                mdamount=edt_Amount.getText().toString().trim();


                if (type.isEmpty()) {
                    edt_Type.setError("Income type is empty");
                    edt_Type.requestFocus();
                    return;
                }
                if (mdamount.isEmpty()) {
                    edt_Amount.setError("amount is empty");
                    edt_Amount.requestFocus();
                    return;
                }
                if (note.isEmpty()) {
                    edt_Note.setError("note is empty");
                    edt_Note.requestFocus();
                    return;
                }

                int myAmount = Integer.parseInt(mdamount);

                String mDate=DateFormat.getDateInstance().format(new Date());

                Data data= new Data(myAmount,type,note,post_key,mDate);

                mincomeDatabase.child(post_key).setValue(data);

                dialog.dismiss();
            }
        });

        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }



}

package com.example.smartmoney;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Fill_Details extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText edt_fname, edt_lname, edt_address, edt_email;
    Spinner profession;
    RadioGroup radio_gender;
    Button submit;
    FirebaseDatabase db;
    DatabaseReference root;
    FirebaseUser firebaseUser;
    User user;
    String[] prof = {"Student", "Goverment Employee", "Private Employee", "Freelancer", "Entrepreneur"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_details);

        edt_fname = findViewById(R.id.edt_fname);
        edt_lname = findViewById(R.id.edt_lname);
        edt_email = findViewById(R.id.edt_email);
        edt_address = findViewById(R.id.edt_address);
        profession = findViewById(R.id.profession);
        radio_gender = findViewById(R.id.gender);
        submit = findViewById(R.id.btn_submit_details);
        profession.setOnItemSelectedListener(this);


        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, prof);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        profession.setAdapter(aa);


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fname = edt_fname.getText().toString();
                String lname = edt_lname.getText().toString();
                String email = edt_email.getText().toString();
                String address = edt_address.getText().toString();
                int id = radio_gender.getCheckedRadioButtonId();
                String prof_list = profession.getSelectedItem().toString();
                String gender = "";
                user = new User();


                if (id == R.id.male) {
                    gender = "Male";
                }
                if (id == R.id.female) {
                    gender = "Female";
                }
                if (id == R.id.other) {
                    gender = "Other";
                }

                if (fname.isEmpty()) {
                    edt_fname.setError("First Name is empty");
                    edt_fname.requestFocus();
                    return;
                }
                if (lname.isEmpty()) {
                    edt_lname.setError("Last Name is empty");
                    edt_lname.requestFocus();
                    return;
                }
                if (email.isEmpty()) {
                    edt_email.setError("Email is empty");
                    edt_email.requestFocus();
                    return;
                }
                if (address.isEmpty()) {
                    edt_address.setError("Address is empty");
                    edt_address.requestFocus();
                    return;
                }
                db = FirebaseDatabase.getInstance();
                root = db.getReference().child("Users");
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                addDatatoFirebase(fname, lname, email, address, gender, prof_list);

                Intent i = new Intent(getApplicationContext(), Dashboard_Activity.class);
                startActivity(i);
            }
        });


    }

    private void addDatatoFirebase(String fname, String lname, String email, String address, String gender, String prof_list) {

        user.setFName(fname);
        user.setLName(lname);
        user.setEmail(email);
        user.setAddress(address);
        user.setGender(gender);
        user.setProf_list(prof_list);


        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                root.child(firebaseUser.getUid()).setValue(user);
                Toast.makeText(getApplicationContext(), "Data Inserted Sucessfully", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Error..", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}

package com.example.smartmoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Income_piechart_Activity extends AppCompatActivity {

    Toolbar toolbar;
    private PieChart pieChart;
    private FirebaseAuth mAuth;
    private DatabaseReference mincomeDatabase;
    private ArrayList<PieEntry> pieEntryArrayList;
    private ArrayList<Data> dataArrayList ;
    private ArrayList<String> strings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_piechart);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Smart Money");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        pieChart=findViewById(R.id.income_pie_chart);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser =mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mincomeDatabase = FirebaseDatabase.getInstance().getReference().child("Income_Data").child(uid);

        pieEntryArrayList = new ArrayList<>();
        strings= new ArrayList<>();


        mincomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataArrayList =  new ArrayList<>();
                for (DataSnapshot mysnapshot: snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);
                    dataArrayList.add(new Data(data.getType(),data.getAmount()));
                }
                for (int i=0;i<dataArrayList.size();i++){
                    String type=dataArrayList.get(i).getType();
                    int amount = dataArrayList.get(i).getAmount();

                    // Log.i(TAG, "value of "+i+amount);
                    //strings.add(type);
                    pieEntryArrayList.add(new PieEntry(amount,type));
                }


                showchart(pieEntryArrayList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    private void showchart(ArrayList<PieEntry> pieEntryArrayList) {

        PieDataSet dataSet = new PieDataSet(pieEntryArrayList,"Income");

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS){
            colors.add(color);
        }

        for (int color: ColorTemplate.VORDIPLOM_COLORS){
            colors.add(color);
        }

        dataSet.setColors(colors);

        PieData dataa = new PieData(dataSet);
        dataa.setDrawValues(true);
        dataa.setValueFormatter(new PercentFormatter(pieChart));
        dataa.setValueTextSize(12f);
        dataa.setValueTextColors(Collections.singletonList(Color.BLUE));

        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("INCOME");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(true);
        Description description = new Description();
        description.setText("Income Data");
        pieChart.setDescription(description);
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
        pieChart.setData(dataa);
        pieChart.animateX(2500);
        pieChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

package com.example.smartmoney;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class About_Fragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_about_, container, false);


    }
}

package com.example.smartmoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Expanse_barchart_Activity extends AppCompatActivity {

    Toolbar toolbar;
   private BarChart barChart;
   private ArrayList<BarEntry> barEntryArrayList;
    private ArrayList<String> strings;
    private ArrayList<Data> typeandamount_arraylist ;
    private FirebaseAuth mAuth;
    private DatabaseReference mexpenseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expanse_barchart);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Smart Money");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        barChart = findViewById(R.id.expanse_barChart);

        mAuth=FirebaseAuth.getInstance();
        FirebaseUser mUser =mAuth.getCurrentUser();
        String uid = mUser.getUid();
        mexpenseDatabase = FirebaseDatabase.getInstance().getReference().child("Expense_Data").child(uid);
        barEntryArrayList = new ArrayList<>();
        strings = new ArrayList<>();


        // typeandamount_arraylist.clear();

        mexpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                typeandamount_arraylist =  new ArrayList<>();
                for (DataSnapshot mysnapshot: snapshot.getChildren()){

                    Data data = mysnapshot.getValue(Data.class);
                    typeandamount_arraylist.add(new Data(data.getType(),data.getAmount()));
                }
                for (int i=0;i<typeandamount_arraylist.size();i++){
                    String type=typeandamount_arraylist.get(i).getType();
                    int amount = typeandamount_arraylist.get(i).getAmount();
                    barEntryArrayList.add(new BarEntry(i,amount));
                    // Log.i(TAG, "value of "+i+amount);
                    strings.add(type);
                }

                showchart(barEntryArrayList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    private void showchart(ArrayList<BarEntry> barEntryArrayList) {
        BarDataSet barDataSet = new BarDataSet(barEntryArrayList,"Expense Data");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLUE);
        barDataSet.setValueTextSize(12f);
        Description description = new Description();
        description.setText("Expenses");
        barChart.setDescription(description);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.animateY(2500);
        barChart.invalidate();
        XAxis xAxis=barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(strings));
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(strings.size());
        xAxis.setLabelRotationAngle(270);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

