package com.nautanki.loginregapp;

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

        assertEquals("com.nautanki.loginregapp", appContext.getPackageName());
    }
}


package com.nautanki.loginregapp;

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

package com.nautanki.loginregapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Santosh on 8/20/2016. First.
 */
public class MySingleton {
    private static MySingleton mInstance;
    private RequestQueue requestQueue;
    private static Context mCtx;

    //Constructor
    private MySingleton(Context context){
        mCtx = context; //Initialize context.
        requestQueue = getRequestQueue(); //Call the method.
    }

    //Request queue method.
    public RequestQueue getRequestQueue(){
        if (requestQueue == null){
            //Initialize request queue.
            requestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return requestQueue;
    }

    //This method returns instance of this class.
    public static synchronized MySingleton getInstance(Context context){
        if (mInstance == null){
            //Initialize instance
            mInstance = new MySingleton(context);
        }
        return mInstance;
    }

    //This method is to add request queue.
    public <T> void addToRequestque(Request<T> request){
        //Add each of the request to request queue.
        requestQueue.add(request);
    }
}

package com.nautanki.loginregapp;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
//kkkkk
public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button login_button;
    Button b10;
    Button b11;
    EditText UserName, Password;
    String username, password;
    String login_url = "https://untruthful-oscillat.000webhostapp.com/  ";
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        User user=new User(MainActivity.this);
        if(user.getName()!="")
        {
            Intent intent=new Intent(this,Choose.class);

            startActivity(intent);
            finish();
        }


        /*textView = (TextView)findViewById(R.id.reg_txt); //Reg new user.
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start the newly created activity.
                startActivity(new Intent(MainActivity.this,Register.class));
            }
        });*/

        builder = new AlertDialog.Builder(MainActivity.this); //Init builder.
        login_button = (Button) findViewById(R.id.bn_login);
        b10=(Button)findViewById(R.id.button10);
        b11=(Button)findViewById(R.id.button11);
        UserName = (EditText) findViewById(R.id.login_name);
        Password = (EditText) findViewById(R.id.login_password);
        //Click listner for login button
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check if they entered un and pwd
                username = UserName.getText().toString();
                password = Password.getText().toString();
                Toast.makeText(MainActivity.this, username+password, Toast.LENGTH_SHORT).show();
                if (username.equals("")||password.equals("")){
                    builder.setTitle("Error");
                    //Call display Alert. Code it now.
                    displayAlert("Enter a valid username or password.");
                } else{
                    //Auth user using script.
                    StringRequest stringRequest = new StringRequest(Request.Method.POST,
                            login_url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //Handle response, from server its a JSON array
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                String code = jsonObject.getString("code");

                                if (code.equals("login_failed")){
                                    builder.setTitle("Login failed");
                                    displayAlert(jsonObject.getString("message"));
                                } else{
                                    String name=jsonObject.getString("name");
                                    User user=new User(MainActivity.this);
                                    user.setName(name);
                                    builder.setTitle("Login Success!");
                                    displayAlert1(jsonObject.getString("message")+""+jsonObject.getString("name"),jsonObject.getString("name"));


                                }
                            } catch (JSONException e) {

                                e.printStackTrace();
                            }


                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this,"Error from ErroResponse",Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                        }
                    }){
                        //We need to pass username and password, thus override getparams

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            //As return type is a map create a map
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("email", username);
                            params.put("pass", password);
                            return params;
                        }
                    };
                    //Add string request to request queue
                    MySingleton.getInstance(MainActivity.this).addToRequestque(stringRequest);

                }
            }
        });
    }

    public void displayAlert(String message){
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UserName.setText(""); Password.setText("");
            }
        });
        AlertDialog alertDialog = builder.create(); //create
        alertDialog.show(); //Show it.
    }

    public void displayAlert1(String message, final String name){
        builder.setMessage(message);
        builder.setPositiveButton("Go Ahead", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UserName.setText(""); Password.setText("");

                //Start another activity. Create an intent.
                Intent intent = new Intent(MainActivity.this, Choose.class);


                startActivity(intent);
                finish();
            }
        });
        AlertDialog alertDialog = builder.create(); //create
        alertDialog.show(); //Show it.
    }

    public void logOut(View view) {
        new User(this).removeUser();
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    public void userlogin(View view)
    {
        Toast.makeText(this, "Loading...", Toast.LENGTH_LONG).show();
        Intent intent=new Intent(this,UserLoginPage.class);
        startActivity(intent);

    }

    public void register(View view) {
        Intent intent=new Intent(this,Register.class);
        startActivity(intent);
    }


}

package com.nautanki.loginregapp;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
//2
public class Register extends AppCompatActivity {
    Button reg_bn;
    TextView tw;
    EditText Name, UserName, Password, ConPassword;
    String name, username, password, conpass;
    AlertDialog.Builder builder;
    String reg_url = "https://untruthful-oscillat.000webhostapp.com/register.php";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_register);
        tw=(TextView)findViewById(R.id.textView);
        reg_bn = (Button)findViewById(R.id.bn_reg);
        Name = (EditText)findViewById(R.id.reg_name);
        UserName = (EditText)findViewById(R.id.reg_user_name);
        Password = (EditText)findViewById(R.id.reg_password);
        ConPassword = (EditText)findViewById(R.id.reg_con_password);
        builder = new AlertDialog.Builder(Register.this);
        reg_bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Fetch the values.
                name = Name.getText().toString();
                username = UserName.getText().toString();
                password = Password.getText().toString();
                conpass = ConPassword.getText().toString();
                if (name.equals("")||username.equals("")||password.equals("")||conpass.equals("")){
                    builder.setTitle("Error");
                    builder.setMessage("Please fill up all the fields.");
                    displayAlert("input_error");
                } else{
                    //Check if passwords match.
                    if (!(password.equals(conpass))){
                        builder.setTitle("Error");
                        builder.setMessage("Passwords do not match.");
                        displayAlert("input_error");
                    }else {
                        //Register user
                        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                                reg_url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                //Handle response.
                                try {
                                    Toast.makeText(Register.this, "onresponse", Toast.LENGTH_SHORT).show();
                                    JSONArray jsonArray = new JSONArray(response);
                                    JSONObject jsonObject = jsonArray.getJSONObject(0); //0=Index
                                    //Fetch data from server
                                    String code = jsonObject.getString("code");
                                    String message = jsonObject.getString("message");
                                    builder.setTitle("Server response");
                                    builder.setMessage(message);
                                    displayAlert(code); //Method we defined.
                                } catch (JSONException e) {
                                    tw.setText("From Exception Error!");
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(Register.this, "onerrorresponse", Toast.LENGTH_SHORT).show();
                            }
                        }){
                            //Override a method called get params to pass data.

                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<String, String>();
                                //The keys must match the keys on $_POST on SSS.
                                params.put("name",name);
                                params.put("email",username);
                                params.put("pass",password);
                                return params; //Return the MAP.
                            }
                        };
                        //Add this string request to request queue.
                        MySingleton.getInstance(Register.this).addToRequestque(stringRequest);
                    }
                }
            }
        });
    } // End on onCreate.

    public void displayAlert(final String code){
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (code == "input_error"){
                    Password.setText("");
                    ConPassword.setText("");
                }
                else if (code.equals("reg_success")){
                    finish(); //Finish activity
                }
                else if (code.equals("reg_failed")){
                    //Reset all input.
                    Name.setText("");
                    UserName.setText("");
                    Password.setText("");
                    ConPassword.setText("");
                }
            }
        });
        //Display the alert dialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

package com.nautanki.loginregapp;

import android.content.Context;
import android.content.SharedPreferences;

public class User {
    Context context;
    String bkid,stdid;

    public void removeUser()
    {
        sharedPreferences.edit().clear().commit();
    }


    public String getName() {
        name=sharedPreferences.getString("userdata","");
        return name;
    }

    public void setName(String name) {
        this.name = name;
        sharedPreferences.edit().putString("userdata",name).commit();

    }



    public String getBookId1() {
        bkid=sharedPreferences.getString("bookid1","");
        return bkid;
    }
    public void setBookId1(String bkid) {
        this.bkid=bkid;
        sharedPreferences.edit().putString("bookid1",bkid).commit();

    }



    public String getBookId2() {
        bkid=sharedPreferences.getString("bookid2","");
        return bkid;
    }
    public void setBookId2(String bkid) {
        this.bkid=bkid;
        sharedPreferences.edit().putString("bookid2",bkid).commit();

    }


    public String getBookId3() {
        bkid=sharedPreferences.getString("bookid3","");
        return bkid;
    }
    public void setBookId3(String bkid) {
        this.bkid=bkid;
        sharedPreferences.edit().putString("bookid3",bkid).commit();

    }
    public String getBookId4() {
        bkid=sharedPreferences.getString("bookid4","");
        return bkid;
    }
    public void setBookId4(String bkid) {
        this.bkid=bkid;
        sharedPreferences.edit().putString("bookid4",bkid).commit();

    }




   /* public String getBookId() {
        bkid=sharedPreferences.getString("bookid","");
        return bkid;
    }
    public void setBookId(String bkid) {
        this.bkid=bkid;
        sharedPreferences.edit().putString("bookid",bkid).commit();

    }*/

    public String getStudentId() {
        stdid=sharedPreferences.getString("studentid","");
        return stdid;
    }
    public void setStudentId(String stdid) {
        this.stdid=stdid;
        sharedPreferences.edit().putString("studentid",stdid).commit();

    }



    String name;
    SharedPreferences sharedPreferences;


    public User(Context context)
    {
        this.context=context;

        sharedPreferences= context.getSharedPreferences("userinfo",context.MODE_PRIVATE);

    }
}


package com.nautanki.loginregapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SearchDetail extends AppCompatActivity {

    private static WebView browser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search_detail);
        getSupportActionBar().hide();
        browser=findViewById(R.id.web1);
        String url="https://untruthful-oscillat.000webhostapp.com/home.php";

        //Intent intent = getIntent();
       // String what = intent.getStringExtra("search_type");

        browser.setWebViewClient(new WebViewClient());
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.loadUrl(url);




    }

    @Override
    public void onBackPressed() {
        if(browser.canGoBack()){
            browser.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    public void manage(View view) {
        Toast.makeText(this, "Loding...", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this,ManageTemp.class));
    }
}


package com.nautanki.loginregapp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();

        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },5000);
    }
}


package com.nautanki.loginregapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Choose extends AppCompatActivity {
    TextView ms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        getSupportActionBar().hide();
        ms=findViewById(R.id.wlcmsg);
        User user=new User(Choose.this);
        ms.setText("Welcome  :"+user.getName());

    }

    public void bookReturn(View view) {
        startActivity(new Intent(this,LoginSuccess.class));
    }

    public void bookIssue(View view) {startActivity(new Intent(this,IssuePage.class));    }


    public void logOut(View view) {
        new User(this).removeUser();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void findStudHistory(View view) {
        Toast.makeText(this, "Loading...", Toast.LENGTH_LONG).show();
        Intent intent=new Intent(this,SearchDetail.class);
        //intent.putExtra("search_type", "student_history");
        startActivity(intent);
    }
}



package com.nautanki.loginregapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class IssuePage extends AppCompatActivity implements View.OnClickListener {
    EditText stdid,bkid1;
    Button btn_stdid,btn_bkid1,btn_bkid2;
    AlertDialog.Builder builder;
    String reg_url = "https://untruthful-oscillat.000webhostapp.com/issue.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_issue_page);
        stdid= findViewById(R.id.txt_issue_std_id);
        bkid1=findViewById(R.id.txt_issue_bkid1);

        initViews();

        User user=new User(IssuePage.this);
        stdid.setText(user.getStudentId().toString());
        bkid1.setText(user.getBookId1().toString());


    }

    private void initViews() {
        btn_stdid=findViewById(R.id.btn_issue_std_id);
        btn_bkid1=findViewById(R.id.btn_issue_bkid1);
        btn_bkid2=findViewById(R.id.btn_issue_bkid2);

        btn_stdid.setOnClickListener(this);
        btn_bkid1.setOnClickListener( this);
        btn_bkid2.setOnClickListener( this);

    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_issue_bkid1:
                Intent intent1=new Intent(this,ScannedBarcodeActivty.class);
                intent1.putExtra("btnvalue","issue_book1");
                startActivity(intent1);
                finish();
                break;
            case R.id.btn_issue_bkid2:
                Intent intent2=new Intent(this,ScannedBarcodeActivty.class);
                intent2.putExtra("btnvalue","issue_book2");
                startActivity(intent2);
                finish();
                break;

            case R.id.btn_issue_std_id:
                Intent i=new Intent(this,ScannedBarcodeActivty.class);
                i.putExtra("btnvalue","issue_student");
                startActivity(i);
                finish();
                break;

        }

    }

    public void reset(View view) {
        User user=new User(IssuePage.this);
        user.setStudentId("");
        user.setBookId1("");
        user.setBookId2("");
        user.setBookId3("");
        user.setBookId4("");
        startActivity(new Intent(this,IssuePage.class));
        finish();
    }

    public void issue_btn(View view) {

        final String bookid = bkid1.getText().toString();
        final String studid = stdid.getText().toString();
        builder = new AlertDialog.Builder(IssuePage.this);

        if (bookid.equals("")||studid.equals("") ){
            builder.setTitle("Error");
            builder.setMessage("Please fill up all the fields.");
            displayAlert("input_error");
        } else{

                StringRequest stringRequest = new StringRequest(Request.Method.POST,
                        reg_url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Handle response.
                        try {
                            Toast.makeText(IssuePage.this, "onresponse", Toast.LENGTH_SHORT).show();
                            JSONArray jsonArray = new JSONArray(response);
                            JSONObject jsonObject = jsonArray.getJSONObject(0); //0=Index
                            //Fetch data from server
                            String code = jsonObject.getString("code");
                            String message = jsonObject.getString("message");
                            builder.setTitle("Server response");
                            builder.setMessage(message);
                            displayAlert(code); //Method we defined.
                        } catch (JSONException e) {
                           // tw.setText("From Exception Error!");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(IssuePage.this, "on_error_response", Toast.LENGTH_SHORT).show();
                    }
                }){
                    //Override a method called get params to pass data.

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<String, String>();
                        //The keys must match the keys on $_POST on SSS.

                        params.put("sid",studid);
                        params.put("bid",bookid);

                        return params; //Return the MAP.
                    }
                };
                //Add this string request to request queue.
                MySingleton.getInstance(IssuePage.this).addToRequestque(stringRequest);
            //}
        }

    }




    public void displayAlert(final String code){
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (code.equals("succ_issue")){
                    finish(); //Finish activity
                }
                else if(code.equals("stdid_wrong"))
                {
                    stdid.setText("");

                }
                else if(code.equals("bkid_wrong") || code.equals("returned_succ"))
                {
                    bkid1.setText("");
                }



            }
        });
        //Display the alert dialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}


package com.nautanki.loginregapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginSuccess extends AppCompatActivity implements View.OnClickListener {
    /*TextView username,password;*/
    TextView t1;
    Button btnScanStdid,btnScanBkid,return_butn;
    EditText txtstdid,txtbkid;
    AlertDialog.Builder builder;
    String ret_url = "https://untruthful-oscillat.000webhostapp.com/return.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login_success);

        t1=findViewById(R.id.textView6);
        t1.setText("Books Return Area");

        Intent intent=getIntent();

        User user=new User(LoginSuccess.this);



        txtstdid=findViewById(R.id.txt_return_stdid);
        txtbkid=findViewById(R.id.txt_return_bkid);


        initViews();

        // Toast.makeText(this, sharedPrefForIDs.getBookId()+" stdid "+sharedPrefForIDs.getStudentId(), Toast.LENGTH_LONG).show();
        if( user.getStudentId()!="")
        {

            //Toast.makeText(this, sharedPrefForIDs.getBookId().toString(), Toast.LENGTH_SHORT).show();

            txtstdid.setText(user.getStudentId().toString());
            txtbkid.setText(user.getBookId2().toString());
        }


    }



    private void initViews() {
        btnScanStdid = findViewById(R.id.btn_return_stdid);
        btnScanBkid = findViewById(R.id.btn_return_bkid);
        return_butn = findViewById(R.id.return_butn);



        // btnScanBarcode = findViewById(R.id.btnScanBarcode);

        btnScanStdid.setOnClickListener( this);
        btnScanBkid.setOnClickListener( this);
        return_butn.setOnClickListener( this);
        //  btnScanBarcode.setOnClickListener(this);
    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_return_stdid:
                Intent i=new Intent(this,ScannedBarcodeActivty.class);
                i.putExtra("btnvalue","return_student");
                startActivity(i);
                finish();
                break;
            case R.id.btn_return_bkid:
                Intent i1=new Intent(this,ScannedBarcodeActivty.class);
                i1.putExtra("btnvalue","return_student");
                startActivity(i1);
                finish();
                break;
            case R.id.return_butn:
                return_book();
                break;

        }

    }

    private void return_book() {
       // Toast.makeText(this, "from Return area", Toast.LENGTH_SHORT).show();
        final String bookid = txtbkid.getText().toString();
        final String studid = txtstdid.getText().toString();
        builder = new AlertDialog.Builder(LoginSuccess.this);

        if (bookid.equals("")||studid.equals("") ){
            builder.setTitle("Error");
            builder.setMessage("Please fill up all the fields.");
            displayAlert("input_error");
        } else{

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    ret_url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Handle response.
                    try {
                        Toast.makeText(LoginSuccess.this, "onresponse bookid"+bookid+"stdid  "+studid, Toast.LENGTH_SHORT).show();
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0); //0=Index
                        //Fetch data from server
                        String code = jsonObject.getString("code");
                        String message = jsonObject.getString("message");
                        builder.setTitle("Server response");
                        builder.setMessage(message);
                        displayAlert(code); //Method we defined.
                    } catch (JSONException e) {
                        // tw.setText("From Exception Error!");
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(LoginSuccess.this, "on_error_response", Toast.LENGTH_SHORT).show();
                }
            }){
                //Override a method called get params to pass data.

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    //The keys must match the keys on $_POST on SSS.

                    params.put("sid",studid);
                    params.put("bid",bookid);

                    return params; //Return the MAP.
                }
            };
            //Add this string request to request queue.
            MySingleton.getInstance(LoginSuccess.this).addToRequestque(stringRequest);
            //}
        }

    }




    public void displayAlert(final String code){
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (code.equals("return_succ")){
                    txtbkid.setText("");
                    finish(); //Finish activity
                }
                else if(code.equals("stdid_wrong"))
                {
                    txtstdid.setText("");

                }
                else if(code.equals("bkid_wrong"))
                {
                    txtbkid.setText("");
                }

            }
        });
        //Display the alert dialog.
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }




}

package com.nautanki.loginregapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ManageTemp extends AppCompatActivity {

    private static WebView browser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_manage_temp);
        getSupportActionBar().hide();
        browser=findViewById(R.id.webview1);
        String url="https://untruthful-oscillat.000webhostapp.com/home.php";
        browser.setWebViewClient(new WebViewClient());
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.loadUrl(url);



    }

    @Override
    public void onBackPressed() {
        if(browser.canGoBack()){
            browser.goBack();
        }
        else {
            super.onBackPressed();
        }
    }
}


package com.nautanki.loginregapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class UserLoginPage extends AppCompatActivity {
    private static WebView browser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_user_login_page);
        getSupportActionBar().hide();
        browser=findViewById(R.id.webview);
        String url="http://kniceassist.com/";
        browser.setWebViewClient(new WebViewClient());
        browser.getSettings().setLoadsImagesAutomatically(true);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        browser.loadUrl(url);



    }

    @Override
    public void onBackPressed() {
        if(browser.canGoBack()){
            browser.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    public void manage(View view) {
        Toast.makeText(this, "Loding...", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this,ManageTemp.class));
    }
}

package com.nautanki.loginregapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScannedBarcodeActivty extends AppCompatActivity {


    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnAction;
    String intentData = "";
    String btntype;
    boolean isEmail = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_scanned_barcode_activty);
        User user=new User(this);
        Intent intent=getIntent();
        btntype=intent.getStringExtra("btnvalue");

        initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
       // btnAction = findViewById(R.id.btnAction);


        /*btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (intentData.length() > 0) {
                    if (isEmail)
                        startActivity(new Intent(ScannedBarcodeActivity.this, EmailActivity.class).putExtra("email_address", intentData));
                    else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                    }
                }


            }
        });*/
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started for "+btntype, Toast.LENGTH_LONG).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivty.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivty.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {


            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {


                    txtBarcodeValue.post(new Runnable() {

                        @Override
                        public void run() {
/*
                            if (barcodes.valueAt(0).email != null) {
                                txtBarcodeValue.removeCallbacks(null);
                                intentData = barcodes.valueAt(0).email.address;
                                txtBarcodeValue.setText(intentData);
                                isEmail = true;
                                btnAction.setText("ADD CONTENT TO THE MAIL");
                            } else {*/
                            //isEmail = false;
                            // btnAction.setText("LAUNCH URL");
                            intentData = barcodes.valueAt(0).displayValue;


                            //}
                            if(btntype.equals("issue_book1")){

                                txtBarcodeValue.setText(intentData);
                                User user=new User(ScannedBarcodeActivty.this);
                                user.setBookId1(intentData);}

                            if(btntype.equals("return_book2")){

                                txtBarcodeValue.setText(intentData);
                                User user=new User(ScannedBarcodeActivty.this);
                                user.setBookId2(intentData);}

                            if(btntype.equals("issue_book3")){

                                txtBarcodeValue.setText(intentData);
                                User user=new User(ScannedBarcodeActivty.this);
                                user.setBookId3(intentData);}

                            if(btntype.equals("issue_book4")){

                                txtBarcodeValue.setText(intentData);
                                User user=new User(ScannedBarcodeActivty.this);
                                user.setBookId4(intentData);}



                            if(btntype.equals("issue_student")) {

                                txtBarcodeValue.setText(intentData);
                                User sharedPrefForIDs=new User(ScannedBarcodeActivty.this);
                                sharedPrefForIDs.setStudentId(intentData);
                            }


                            if(btntype.equals("return_student")) {

                                txtBarcodeValue.setText(intentData);
                                User sharedPrefForIDs=new User(ScannedBarcodeActivty.this);
                                sharedPrefForIDs.setStudentId(intentData);
                            }
                            //Toast.makeText(ScannedBarcodeActivity.this, intentData, Toast.LENGTH_SHORT).show();


                            finish();
                        }
                    });

                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
        if(btntype.equals("issue_book1")){

            startActivity(new Intent(this,IssuePage.class));
            finish();
        }

        if(btntype.equals("issue_book2")){

            startActivity(new Intent(this,IssuePage.class));
            finish();
        }

        if(btntype.equals("issue_book3")){

            startActivity(new Intent(this,IssuePage.class));
            finish();
        }

        if(btntype.equals("issue_book4")){

            startActivity(new Intent(this,IssuePage.class));
            finish();
        }

        if(btntype.equals("issue_student")){

            startActivity(new Intent(this,IssuePage.class));
            finish();
        }

        if(btntype.equals("issue_book1")){

            startActivity(new Intent(this,IssuePage.class));
            finish();
        }

        if(btntype.equals("return_student")){

            startActivity(new Intent(this,LoginSuccess.class));
            finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        initialiseDetectorsAndSources();


    }

}


