package com.hamzaamin.i180550_i170298;

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
        assertEquals("com.abubakar.i180449_i180564", appContext.getPackageName());
    }
}

package com.hamzaamin.i180550_i170298;

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

package com.hamzaamin.i180550_i170298;


public class Message {
    String senderId;
    String receiverId;
    String text;
    Long timestamp;
    String imgSrc;

    public Message(String senderId, String receiverId, String text,String imgSrc) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.timestamp=System.currentTimeMillis()/1000;
        this.imgSrc=imgSrc;
    }

    public Message(String senderId, String receiverId, String text, String timestamp ,String imgSrc) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        System.out.println("HEYYYYYYYYYYYYYYYYYYYYYYYYYY"+timestamp);
        this.timestamp= Long.parseLong(timestamp, 10);
        this.imgSrc=imgSrc;
    }

    public Message(){

    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }


    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

package com.hamzaamin.i180550_i170298;

import android.app.Application;

import com.onesignal.OneSignal;

public class MyApp extends Application {
    private static final String ONESIGNAL_APP_ID = "a639dd08-3cbf-4992-8ce2-1c28ccdb8695";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }
}


package com.hamzaamin.i180550_i170298;

public class Profile {
    String id;
    String email;
    String name;
    String gender;
    String bio;
    String phoneNo;
    String dp;
    //Boolean isOnline;

    public Profile(String id, String email, String name, String gender, String phoneNo, String bio, String dp) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.gender = gender;
        this.phoneNo = phoneNo;
        this.bio = bio;
        this.dp=dp;
    }
    public Profile(){

    }

    public String getDp() {
        return dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
}



package com.hamzaamin.i180550_i170298;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallActivity extends AppCompatActivity {
    CircleImageView profile;
    TextView text;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        Intent data= getIntent();

        String uriString = data.getStringExtra("img");
        String name = data.getStringExtra("name");
        Uri uri = Uri.parse(uriString);

        profile = findViewById(R.id.profileImage);
        text = findViewById(R.id.CallerName);
        fab = findViewById(R.id.endCall);

        profile.setImageURI(uri);
        text.setText(name);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}

package com.hamzaamin.i180550_i170298;

import android.app.Application;

public class Id extends Application {
    private static String id;
    private static String path;
    private static String ip;
    private static String name;
    private static String dp;

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        Id.name = name;
    }

    public static String getDp() {
        return dp;
    }

    public static void setDp(String dp) {
        Id.dp = dp;
    }

    public static String getId() {
        return id;
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        Id.ip = ip;
    }

    public static void setId(String id) {
        Id.id = id;
    }

    public static String getPath() {
        return path;
    }

    public static void setPath(String path) {
        Id.path = path;
    }
}


package com.hamzaamin.i180550_i170298;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;

public abstract class ScreenShotContentObserver extends ContentObserver {

    private Context context;
    private boolean isFromEdit = false;
    private String previousPath;

    public ScreenShotContentObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA
        }, null, null, null)) {
            if (cursor != null && cursor.moveToLast()) {
                int displayNameColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                String fileName = cursor.getString(displayNameColumnIndex);
                String path = cursor.getString(dataColumnIndex);
                if (new File(path).lastModified() >= System.currentTimeMillis() - 10000) {
                    if (isScreenshot(path) && !isFromEdit && !(previousPath != null && previousPath.equals(path))) {
                        onScreenShot(path, fileName);
                    }
                    previousPath = path;
                    isFromEdit = false;
                } else {
                    cursor.close();
                    return;
                }
            }
        } catch (Throwable t) {
            isFromEdit = true;
        }
        super.onChange(selfChange, uri);
    }

    private boolean isScreenshot(String path) {
        return path != null && path.toLowerCase().contains("screenshot");
    }

    protected abstract void onScreenShot(String path, String fileName) throws FileNotFoundException;

}

package com.hamzaamin.i180550_i170298;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.dhaval2404.imagepicker.ImagePicker;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddContact extends AppCompatActivity {

    CircleImageView dp;
    Uri imgURI;

    Button back,uploadImg, post;
    EditText name, number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        back = findViewById(R.id.backbtn);
        name = findViewById(R.id.Name);
        number = findViewById(R.id.Number);
        dp = findViewById(R.id.dp);
        post = findViewById(R.id.post);



        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(AddContact.this)
                        .galleryOnly()
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)*/
                        .start();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper helper = new DBHelper(AddContact.this);
                SQLiteDatabase database = helper.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(MyContactContract.Contact._NAME, name.getText().toString());
                cv.put(MyContactContract.Contact._PHNO, number.getText().toString());
                cv.put(MyContactContract.Contact._IMAGE, imgURI.toString());
                database.insert(MyContactContract.Contact.TABLENAME, null, cv);
                database.close();
                helper.close();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imgURI = data.getData();
        dp.setImageURI(imgURI);
    }
}

package com.hamzaamin.i180550_i170298;

import android.provider.BaseColumns;

public class MyContactContract {
    public static String DB_NAME = "myContacts.db";
    public static int DB_VERSION = 5;

    public static class Contact implements BaseColumns {
        public static String TABLENAME = "contactsTable";
        public static String _NAME = "name";
        public static String _PHNO = "phoneNo";
        public static String _IMAGE = "image";
    }

    public static class Chat implements BaseColumns {
        public static String TABLENAME = "chatTable";
        public static String _RECV_PHNO = "recv_phno";
        public static String _Receiver = "receiver";
        public static String _MESSAGE = "message";
    }
}


package com.hamzaamin.i180550_i170298;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CameraFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ImageView imageView;
    Uri imgURI;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CameraFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CameraFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CameraFragment newInstance(String param1, String param2) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        Button b = view.findViewById(R.id.takeImage);
        b.setVisibility(View.VISIBLE);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCameraActivity();
                b.setVisibility(View.INVISIBLE);
            }
        });


        return view;
    }

    protected void startCameraActivity(){
        startActivity(new Intent(getActivity(), CameraActivity.class));
    }
}

package com.hamzaamin.i180550_i170298;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

public class Register extends AppCompatActivity {
    EditText email, pass, confirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.EmailAddress);
        pass = findViewById(R.id.Password);
        confirmPass = findViewById(R.id.ConfirmPassword);
        Id.setIp("http://192.168.18.81/PHP_Files/");
        TextView login = findViewById(R.id.Login);
        login.setOnClickListener(view -> login());

        TextView register = findViewById(R.id.Signup);
        register.setOnClickListener(view -> register());
    }


    protected void register(){
        createProfile(email.getText().toString(),pass.getText().toString());
    }


    protected void login(){
        Intent signIn=new Intent(Register.this, Login.class);
        startActivity(signIn);
        finish();
    }

    protected void createProfile(String email, String password) {
        String url=Id.getIp()+"getProfileByEmail.php?email="+email;
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    Log.d("length",Integer.toString(arr.length()));
                    if(arr.length()>0){
                        Toast.makeText(Register.this,"A user with same email exists,change email", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Intent intent = new Intent(Register.this, CreateProfile.class);
                        intent.putExtra("email",email);
                        intent.putExtra("password",password);
                        startActivity(intent);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Register.this,"Error in volley",Toast.LENGTH_LONG).show();
            }
        });

        RequestQueue queue= Volley.newRequestQueue(Register.this);
        queue.add(stringRequest);
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}

package com.hamzaamin.i180550_i170298;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateProfile extends AppCompatActivity {

    CircleImageView dp;
    Uri imgURI;
    Bitmap bitmap;
    Button back, post;
    EditText firstName, lastName, gender, phoneNo, aboutMe;
    String encodedImage;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        getWindow().setStatusBarColor(ContextCompat.getColor(CreateProfile.this, R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Intent data = getIntent();
        String id = data.getStringExtra("id");
        String email = data.getStringExtra("email");

        back = findViewById(R.id.backbtn);
        firstName = findViewById(R.id.FirstName);
        lastName = findViewById(R.id.LastName);
        gender = findViewById(R.id.Gender);
        dp = findViewById(R.id.dp);
        post = findViewById(R.id.post);
        phoneNo = findViewById(R.id.phoneNo);
        aboutMe = findViewById(R.id.AboutMe);

        back.setOnClickListener(view -> login());

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(CreateProfile.this)
                        .galleryOnly()
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)*/
                        //.saveDir(new File(getFilesDir(), "ImagePicker"))
                        .start();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    String name = firstName.getText().toString() + " " + lastName.getText().toString();
                    String email = getIntent().getStringExtra("email").toString();
                    String password = getIntent().getStringExtra("password").toString();
                    String Gender = gender.getText().toString();
                    String phoneno = phoneNo.getText().toString();
                    String bio = aboutMe.getText().toString();
                    String url = Id.getIp()+"insert.php";
                    RequestQueue requestQueue = Volley.newRequestQueue(CreateProfile.this);
                    StringRequest request = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        JSONObject x=new JSONObject(response);
                                        Id.setId(x.getString("id"));
                                        Id.setPath(x.getString("dp"));
                                        Toast.makeText(CreateProfile.this,"Data Inserted", Toast.LENGTH_SHORT).show();
                                        Intent intent=new Intent(CreateProfile.this,Dashboard.class);
                                        startActivity(intent);
                                        finish();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(CreateProfile.this,"Data not Inserted", Toast.LENGTH_SHORT).show();
                        }
                    }
                    ) {
                        @Nullable
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String> params = new HashMap<String,String>();
                            params.put("name",name);
                            params.put("email",email);
                            params.put("password",password);
                            params.put("gender",Gender);
                            params.put("phoneno", phoneno);
                            params.put("bio",bio);
                            params.put("dp",encodedImage);

                            return params;
                        }
                    };
                requestQueue.add(request);
            }
        });
    }

    protected void login() {
        startActivity(new Intent(CreateProfile.this, Login.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imgURI = data.getData();
        dp.setImageURI(imgURI);

        try {
            InputStream inputStream = getContentResolver().openInputStream(imgURI);
            bitmap = BitmapFactory.decodeStream(inputStream);


            imageStore(bitmap);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void imageStore(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,stream);

        byte[] imageBytes = stream.toByteArray();

        encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);

    }

}


package com.hamzaamin.i180550_i170298;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jaeger.library.StatusBarUtil;

import org.json.JSONArray;
import org.json.JSONException;

public class Login extends AppCompatActivity {
    TextView register;
    EditText email, password;
    Button login;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        StatusBarUtil.setTranslucent(Login.this, 75);
        register = findViewById(R.id.Register);
        email = findViewById(R.id.EmailAddress);
        password = findViewById(R.id.Password);
        register.setOnClickListener(view -> register());
        login = findViewById(R.id.Login);
        Id.setIp("http://192.168.18.81/PHP_Files/");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email1=email.getText().toString().trim();
                final String password1=password.getText().toString().trim();
                if(email1.isEmpty()){
                    email.setError("Provide email id");
                    email.requestFocus();
                }

                else if(password1.isEmpty()){
                    password.setError("Provide password");
                    password.requestFocus();
                }

                else if(!email1.isEmpty() && !password1.isEmpty()){
                    String url=Id.getIp()+"login.php?email="+email.getText().toString()+"&password="+password.getText().toString();
                    StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arr = new JSONArray(response);
                                Log.d("length",Integer.toString(arr.length()));
                                if(arr.length()>0){
                                    id = arr.getJSONObject(0).getString("id");
                                    String dp = arr.getJSONObject(0).getString("dp");
                                    Id.setDp(dp);
                                    Id.setName(arr.getJSONObject(0).getString("name"));
                                    Intent toHome=new Intent(Login.this,Dashboard.class);
                                    toHome.putExtra("id",id);
                                    SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("id",id);
                                    editor.apply(); // commit changes

                                    Id.setId(arr.getJSONObject(0).getString("id"));
                                    Id.setPath(arr.getJSONObject(0).getString("dp"));

                                    startActivity(toHome);
                                    finish();
                                }
                                else{
                                    Toast.makeText(Login.this,"Error in Login",Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(Login.this,"Error in volley",Toast.LENGTH_LONG).show();
                        }
                    });
                    RequestQueue queue= Volley.newRequestQueue(Login.this);
                    queue.add(stringRequest);
                }
                else{
                    Toast.makeText(Login.this,"Error occurred",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    protected void register(){
        startActivity(new Intent(Login.this, Register.class));
    }
    @Override
    protected void onStart(){
        super.onStart();
    }

    protected String getId() {
        return id;
    }
}

package com.hamzaamin.i180550_i170298;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int secondsDelayed = 1;
        new Handler().postDelayed(() -> {
            startActivity(new Intent(Splash.this, Login.class));
            finish();
        }, secondsDelayed * 1500);
    }
}

package com.hamzaamin.i180550_i170298;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hamzaamin.i180550_i170298.databinding.ActivityDashboardBinding;
import com.hamzaamin.i180550_i170298.main.SectionsPagerAdapter;

public class Dashboard extends AppCompatActivity {

    private ActivityDashboardBinding binding;

    TabLayout tabLayout;

    private int[] tabIcons = {
            R.drawable.ic_twotone_phone_24,
            R.drawable.ic_twotone_photo_camera,
            R.drawable.ic_twotone_chat_bubble,
            R.drawable.ic_twotone_people
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());


        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);


        tabLayout = binding.tabs;
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

}

package com.hamzaamin.i180550_i170298;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ScreenShotContentObserver screenShotContentObserver;
    RecyclerView recyclerView;
    ImageButton selectImage,clearImage;
    Button send;
    View imagePreviewBackground;
    EditText messageContent;
    MessageRVAdapter adapter;
    Uri selectedImage=null;
    ImageButton backButton;
    Bitmap bitmap;
    ArrayList<Message> messagesList;
    TextView appbar_heading;
    ImageView receiverImage,imagePreview;
    String id;
    String encodedImage;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setStatusBarColor(ContextCompat.getColor(ChatActivity.this,R.color.black));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        Intent data=getIntent();
        id=data.getStringExtra("id");
        String receiverName=data.getStringExtra("profileName");
        String receiverImageUri = data.getStringExtra("image");
        // Populate dummy messages in List, you can implement your code here
        send=findViewById(R.id.send_button);
        selectImage=findViewById(R.id.select_image);
        messageContent=findViewById(R.id.message_content);
        backButton = findViewById(R.id.back_button);
        imagePreview = findViewById(R.id.image_preview);
        clearImage = findViewById(R.id.clear_image);
        imagePreviewBackground = findViewById(R.id.image_preview_bg);

        appbar_heading = findViewById(R.id.appbarHeading);
        receiverImage = findViewById(R.id.recImg);
        appbar_heading.setText(receiverName);
        Picasso.get().load(Id.getIp()+receiverImageUri).fit().centerCrop().into(receiverImage);

        messagesList = new ArrayList<>();

        System.out.println("BRUH"+Id.getId());

        HandlerThread handlerThread = new HandlerThread("content_observer");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
            }
        };


        screenShotContentObserver = new ScreenShotContentObserver(handler, this) {
            @Override
            protected void onScreenShot(String path, String fileName) throws FileNotFoundException {
                File file = new File(path); //this is the file of screenshot image
                Uri screenshot = Uri.fromFile(file);

                System.out.println("\n\n\n\nWorks works works\n\n\n");
                Toast.makeText(ChatActivity.this,"Screenshot detected. "+path,Toast.LENGTH_SHORT).show();

                //  clearImage();

                InputStream inputStream = getContentResolver().openInputStream(screenshot);
                bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,80,stream);

                byte[] imageBytes = stream.toByteArray();

                encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);

                PostMessage(new Message(Id.getId(), id, "Screenshot Taken!", encodedImage));
            }
        };


        getChat();

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,200);
            }
        });

        backButton.setOnClickListener(v -> {
            finish();
        });

        send.setOnClickListener(view -> {
            if(messageContent.getText().toString().equals("")){
                Toast.makeText(this, "Message field empty!", Toast.LENGTH_SHORT).show();
            }
            else{
                PostMessage(new Message(Id.getId(), id, messageContent.getText().toString(), encodedImage));
                //clearImage();
            }
        });

        clearImage.setOnClickListener(v -> {
            clearImage();
        });

    }



    @Override
    public void onResume() {
        super.onResume();

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                screenShotContentObserver
        );
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            getContentResolver().unregisterContentObserver(screenShotContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getContentResolver().unregisterContentObserver(screenShotContentObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==200 && resultCode==RESULT_OK){
            selectedImage=data.getData();
            imagePreview.setImageURI(selectedImage);
            imagePreview.setVisibility(View.VISIBLE);
            clearImage.setVisibility(View.VISIBLE);
            imagePreviewBackground.setVisibility(View.VISIBLE);

            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                bitmap = BitmapFactory.decodeStream(inputStream);


                imageStore(bitmap);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private void imageStore(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,stream);

        byte[] imageBytes = stream.toByteArray();

        encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    void PostMessage(Message message){

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);
        String url = Id.getIp()+"postMessage.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("reponse",response);
                getChat();
                clearImage();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("message error",error.getMessage());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData;
                MyData = new HashMap<String, String>();
                if(encodedImage==null){
                    MyData.put("senderId", message.getSenderId());
                    MyData.put("receiverId", message.getReceiverId());
                    MyData.put("text", message.getText());
                    MyData.put("timestamp", Long.toString(message.getTimestamp()));
                    //clearImage();
                }
                else{
                    MyData.put("senderId", message.getSenderId());
                    MyData.put("receiverId", message.getReceiverId());
                    MyData.put("text", message.getText());
                    MyData.put("timestamp", Long.toString(message.getTimestamp()));
                    MyData.put("image", encodedImage);
                }

                return MyData;
            }
        };

        MyRequestQueue.add(MyStringRequest);
    }
    public void clearImage(){
        selectedImage= null;
        encodedImage=null;
        messageContent.setText(null);
        imagePreview.setImageURI(null);
        imagePreview.setVisibility(View.GONE);
        clearImage.setVisibility(View.GONE);
        imagePreviewBackground.setVisibility(View.GONE);
    }

    public void getChat(){
        ArrayList<Profile> temp=new ArrayList<>();
        messagesList.clear();
        RequestQueue queue= Volley.newRequestQueue(ChatActivity.this);
        String url=Id.getIp()+"getMessages.php?senderId="+Id.getId()+"&receiverId="+id;
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.i("tagconvertstr", "["+response+"]");
                    JSONArray arr = new JSONArray(response);
                    for(int i=0;i<arr.length();i++) {
                        System.out.println("Count: "+i);
                        JSONObject object1=arr.getJSONObject(i);
                        System.out.println("\n\n\nHeyHeyHey\n\n\n");
                        messagesList.add(new Message(object1.getString("senderId"),object1.getString("receiverId"),object1.getString("text"), object1.getString("timestamp"),object1.getString("image")));
                    }
                    adapter = new MessageRVAdapter(ChatActivity.this, messagesList,id,Id.getId());
                    recyclerView = findViewById(R.id.rv);
                    LinearLayoutManager lm = new LinearLayoutManager(ChatActivity.this);
                    lm.setStackFromEnd(true);
                    recyclerView.setLayoutManager(lm);
                    recyclerView.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ChatActivity.this,"Error in c=vollleyy",Toast.LENGTH_SHORT).show();
            }

        });

        queue.add(stringRequest);

    }
}

package com.hamzaamin.i180550_i170298;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CallFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    RecyclerView rv;
    List<Profile> contactList;
    List<Profile> mutualContacts;
    ArrayList<String> arrayList;
    CallRVAdapter adapter;
    private final int ADD_CONTACT = 0;
    EditText searchView;
    CharSequence search ="";
    String senderId;
    SinchClient sinchClient;
    Call call;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CallFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CallFragment newInstance(int columnCount) {
        CallFragment fragment = new CallFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);

        rv = view.findViewById(R.id.list);
        arrayList=new ArrayList<>();
        contactList=new ArrayList<>();
        mutualContacts=new ArrayList<>();

        // requesting to the user for permission.
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        // requesting to the user for permission.
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // requesting to the user for permission.
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);

        } else {
            //if app already has permission this block will execute.
            readContacts();
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // requesting to the user for permission.
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);

        } else {
            //if app already has permission this block will execute.

        }

        getAllUsers();

        RecyclerView.LayoutManager lm= new LinearLayoutManager(getContext());
        rv.setLayoutManager(lm);
        adapter=new CallRVAdapter(mutualContacts, getContext(), CallFragment.this);
        rv.setAdapter(adapter);

        SharedPreferences prefs = getContext().getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        senderId = prefs.getString("id", "0");

        sinchClient = Sinch.getSinchClientBuilder().context(getContext())
                        .userId(senderId)
                        .applicationKey("b862a54e-e886-489d-9356-42970da89d44")
                        .applicationSecret("uXaIyKiXkUql3MBEdaTNew==")
                        .environmentHost("clientapi.sinch.com")
                        .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener() {

        });

        sinchClient.start();

        return view;
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallProgressing(com.sinch.android.rtc.calling.Call call) {

            Toast.makeText(getContext(), "Ringing", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCallEstablished(com.sinch.android.rtc.calling.Call call) {
            Toast.makeText(getContext(), "Call Established", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCallEnded(com.sinch.android.rtc.calling.Call endedCall) {
            Toast.makeText(getContext(), "Call Ended", Toast.LENGTH_LONG).show();
            call = null;
            endedCall.hangup();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }
    }

    private class SinchCallClientListener implements CallClientListener {
        @Override
        public void onIncomingCall(CallClient callClient, com.sinch.android.rtc.calling.Call incomingcall) {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setTitle("CALLING");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Reject", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    call.hangup();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Pick", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    call = incomingcall;
                    call.answer();
                    call.addCallListener(new SinchCallListener());
                    Toast.makeText(getContext(),"Call is Started", Toast.LENGTH_LONG).show();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //list.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        readContacts();
    }
    // function to read contacts using content resolver
    @SuppressLint("Range")
    private void readContacts() {
        ContentResolver contentResolver=getContext().getContentResolver();
        Cursor phones=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        String number;
        while (phones.moveToNext())
        {
            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("+92","0");
            arrayList.add(number);
//                Log.d("Hello",phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        }

        phones.close();
    }

    public void getAllUsers(){
        ArrayList<Profile> temp=new ArrayList<>();
        String url=Id.getIp()+"get.php";
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    for(int i=0;i<arr.length();i++) {
                        JSONObject object1=arr.getJSONObject(i);
                        String id=object1.get("id").toString();
                        String email=object1.get("email").toString();
                        String name=object1.get("name").toString();
                        String gender=object1.get("gender").toString();
                        String phone=object1.get("phoneno").toString();
                        String bio=object1.get("bio").toString();
                        String dp=object1.get("dp").toString();
                        if(arrayList.contains(phone)){
                            Log.d("list i:",id);
                            Log.d("list i:",email);
                            Log.d("list i:",name);
                            Log.d("list i:",gender);
                            mutualContacts.add(new Profile(id,email,name,gender,phone,bio,dp));
                        }

                    }
                    RecyclerView.LayoutManager lm= new LinearLayoutManager(getContext());
                    rv.setLayoutManager(lm);
                    adapter=new CallRVAdapter(mutualContacts, getContext(), CallFragment.this);
                    rv.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"Error in c=vollleyy",Toast.LENGTH_SHORT).show();
            }

        });

        RequestQueue queue= Volley.newRequestQueue(getContext());
        queue.add(stringRequest);

    }

    public void callUser(Profile user) {
        if(call == null) {
            call = sinchClient.getCallClient().callUser(user.getId());
            call.addCallListener(new SinchCallListener());

            openCallerDialog(call);
        }
    }

    private void openCallerDialog(final Call call) {
        AlertDialog alertDialogCall = new AlertDialog.Builder(getContext()).create();
        alertDialogCall.setTitle("ALERT");
        alertDialogCall.setMessage("CALLING");
        alertDialogCall.setButton(AlertDialog.BUTTON_NEUTRAL, "Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                call.hangup();
            }
        });
        alertDialogCall.show();
    }
}


package com.hamzaamin.i180550_i170298;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MessageRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    ArrayList<Message> list;
    String rid;
    String sid;
    boolean changeOrRemove;
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
    public static final int MESSAGE_TYPE_IN = 1;
    public static final int MESSAGE_TYPE_OUT = 2;
    public static final int MESSAGE_TYPE_OUT_IMAGE = 3;
    public static final int MESSAGE_TYPE_IN_IMAGE = 4;
    public static final int MESSAGE_TYPE_OUT_IMAGE_ONLY = 5;
    public static final int MESSAGE_TYPE_IN_IMAGE_ONLY = 6;

    public MessageRVAdapter(Context context, ArrayList<Message> list,String rid,String sid) { // you can pass other parameters in constructor
        this.changeOrRemove = false;
        this.context = context;
        this.sid=sid;
        for(int i=0;i<list.size();i++){
            Log.d("messages",list.get(i).text);
        }
        this.list=list;

        this.rid=rid;
    }


    //http://192.168.100.8/assignment4/deleteMessage.php?text=pog


    //http://192.168.100.8/assignment4/updateMessage.php?text=[value-3]&newText=unpog

    void updateMessage(String text, String newText){
        String url=Id.getIp()+"updateMessage.php?text="+text+"&newText="+newText;

        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    changeOrRemove = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changeOrRemove = false;
                Toast.makeText(context,"Error in c=vollleyy",Toast.LENGTH_SHORT).show();
            }

        });

        RequestQueue queue= Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }
    void deleteMessage(String text){
        String url=Id.getIp()+"deleteMessage.php?text="+text;
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    changeOrRemove = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                changeOrRemove = false;
                Toast.makeText(context,"Error in c=vollleyy",Toast.LENGTH_SHORT).show();
            }

        });

        RequestQueue queue= Volley.newRequestQueue(context);
        queue.add(stringRequest);


    }

    private class MessageInViewHolder extends RecyclerView.ViewHolder {
        TextView dateTime;
        TextView messageTV;
        MessageInViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.text_received);
            dateTime = itemView.findViewById(R.id.time_date_text_received);
        }
        void bind(int position) {
            Message message = list.get(position);
            messageTV.setText(message.text);


            Date resultDate = new Date(message.getTimestamp()*1000);
            dateTime.setText(sdf.format(resultDate));
        }
    }

    private class MessageOutViewHolder extends RecyclerView.ViewHolder {
        TextView dateTime;
        TextView messageTV;
        RelativeLayout rl;
        MessageOutViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.text_sent);
            dateTime = itemView.findViewById(R.id.time_date_text_sent);
            rl = itemView.findViewById(R.id.msll);
        }
        void bind(int position) {
            Message message = list.get(position);
            messageTV.setText(message.text);
            Date resultDate = new Date(message.getTimestamp()*1000);
            dateTime.setText(sdf.format(resultDate));


            messageTV.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                    //If the keyevent is a key-down event on the "enter" button
                    if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                        String text = message.getText();
                        String newText = messageTV.getText().toString();

                        message.setText(newText);

                        updateMessage(text, newText);

                        return true;
                    }
                    return false;
                }
            });




            rl.setOnLongClickListener((View.OnLongClickListener) v -> {
                String text = message.getText();
                Toast.makeText(context,"Message Deleted",Toast.LENGTH_SHORT).show();
                deleteMessage(text);
                list.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, list.size());
                changeOrRemove = false;
                return false;
            });



        }
    }


    private class MessageImageInViewHolder extends RecyclerView.ViewHolder {
        TextView dateTime;
        TextView messageTV;
        ImageView image;
        MessageImageInViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.text_received_image);
            image=itemView.findViewById(R.id.image_received);
            dateTime=itemView.findViewById(R.id.time_date_text_received_image);

        }
        void bind(int position) {
            Message message = list.get(position);
            messageTV.setText(message.text);
            if(!message.getImgSrc().isEmpty()){
                Picasso.get().load(Id.getIp()+message.getImgSrc()).fit().centerCrop().into(image);
            }

            Date resultDate = new Date(message.getTimestamp()*1000);
            dateTime.setText(sdf.format(resultDate));

        }
    }

    private class MessageImageOutViewHolder extends RecyclerView.ViewHolder {
        TextView dateTime;
        TextView messageTV;
        ImageView image;
        RelativeLayout rl;
        MessageImageOutViewHolder(final View itemView) {
            super(itemView);
            messageTV = itemView.findViewById(R.id.text_sent_image);
            image=itemView.findViewById(R.id.image_sent);
            dateTime = itemView.findViewById(R.id.time_date_text_sent_image);
            rl = itemView.findViewById(R.id.msll);
        }
        void bind(int position) {
            Message message = list.get(position);
            messageTV.setText(message.text);
            if(!message.getImgSrc().isEmpty()){
                Picasso.get().load(Id.getIp()+message.getImgSrc()).fit().centerCrop().into(image);
            }
            Date resultDate = new Date(message.getTimestamp()*1000);
            dateTime.setText(sdf.format(resultDate));

            rl.setOnLongClickListener((View.OnLongClickListener) v -> {
                String text = message.getText();
                if(!text.equals("Screenshot Taken!")) {
                    deleteMessage(text);
                    Toast.makeText(context, "Message Deleted", Toast.LENGTH_SHORT).show();
                    list.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, list.size());
                    changeOrRemove = false;
                }

                return false;
            });

            messageTV.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View view, int keyCode, KeyEvent keyevent) {
                    //If the keyevent is a key-down event on the "enter" button
                    if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {


                        String text = message.getText();
                        if(!text.equals("Screenshot Taken!")) {
                            String newText = messageTV.getText().toString();

                            message.setText(newText);

                            updateMessage(text, newText);
                        }
                        return true;
                    }
                    return false;
                }
            });

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_IN) {
            return new MessageInViewHolder(LayoutInflater.from(context).inflate(R.layout.message_rec, parent, false));
        }
        else if(viewType == MESSAGE_TYPE_OUT){
            return new MessageOutViewHolder(LayoutInflater.from(context).inflate(R.layout.message_sent, parent, false));
        }
        else if(viewType == MESSAGE_TYPE_OUT_IMAGE){
            return new MessageImageOutViewHolder(LayoutInflater.from(context).inflate(R.layout.message_sent_img, parent, false));
        }
        else{
            return new MessageImageInViewHolder(LayoutInflater.from(context).inflate(R.layout.message_rec_img, parent, false));
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (list.get(position).senderId.equals(rid)  && list.get(position).getImgSrc().equals("Not Available")) {
            ((MessageInViewHolder) holder).bind(position);
        }
        else if(list.get(position).senderId.equals(rid)  && !list.get(position).getImgSrc().equals("Not Available")){
            ((MessageImageInViewHolder) holder).bind(position);
        }
        else if(list.get(position).senderId.equals(sid) && !list.get(position).getImgSrc().equals("Not Available")){
            ((MessageImageOutViewHolder) holder).bind(position);
        }
        else{
            ((MessageOutViewHolder) holder).bind(position);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).senderId.equals(rid)  && list.get(position).getImgSrc().equals("Not Available")) {
            return 1;
        }
        else if(list.get(position).senderId.equals(rid) && !list.get(position).getImgSrc().equals("Not Available")){
            return 4;
        }
        else if(list.get(position).senderId.equals(sid)&& list.get(position).getImgSrc().equals("Not Available")){
            return 2;
        }
        else{
            return 3;
        }


    }
}


package com.hamzaamin.i180550_i170298;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactRVAdapter extends RecyclerView.Adapter<ContactRVAdapter.MyViewHolder> implements Filterable {

    List<Profile> list;
    List<Profile> filteredList;
    Context c;
    public ContactRVAdapter(List<Profile> list, Context c) {
        this.c=c;
        this.list=list;
        this.filteredList = list;
    }

    @NonNull
    @Override
    public ContactRVAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(c).inflate(R.layout.contact_item,parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactRVAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.name.setText(filteredList.get(position).getName());
        holder.contactNo.setText(filteredList.get(position).getPhoneNo());
        Picasso.get().load(Id.getIp()+filteredList.get(position).getDp()).fit().centerCrop().into(holder.profile);

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(c, ChatActivity.class);
                intent.putExtra("profileName",filteredList.get(position).getName());
                intent.putExtra("id",filteredList.get(position).getId());
                intent.putExtra("image",filteredList.get(position).getDp());

                c.startActivity(intent);
            }

        });

    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String Key = constraint.toString();
                if(Key.isEmpty()){
                    filteredList = list;
                }
                else{
                    List<Profile> listFiltered = new ArrayList<>();
                    for (Profile row: list){
                        if(row.getName().toLowerCase().contains(Key.toLowerCase())){
                            listFiltered.add(row);

                        }
                    }
                    filteredList = listFiltered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList =  (List<Profile>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, contactNo;
        CircleImageView profile;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            contactNo= itemView.findViewById(R.id.ContactNo);
            profile=itemView.findViewById(R.id.profile);
        }
    }
}



package com.hamzaamin.i180550_i170298;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A fragment representing a list of Items.
 */
public class ContactFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    RecyclerView rv;
    Button nContact;
    List<Profile> contactList;
    List<Profile> mutualContacts;
    ArrayList<String> arrayList;
    ContactRVAdapter adapter;
    private final int ADD_CONTACT = 0;
    EditText searchView;
    CharSequence search ="";
    CircleImageView profileImage;
    String senderId;
    Context context;
    TextView name;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ContactFragment newInstance(int columnCount) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        nContact = view.findViewById(R.id.newContact);
        nContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getActivity(), AddContact.class), ADD_CONTACT);
            }
        });




        rv = view.findViewById(R.id.list);
        arrayList=new ArrayList<>();
        contactList=new ArrayList<>();
        mutualContacts=new ArrayList<>();
        profileImage = view.findViewById(R.id.profilePicc);
        Picasso.get().load(Id.getIp()+Id.getDp()).fit().centerCrop().into(profileImage);
        name = view.findViewById(R.id.name);
        name.setText(Id.getName());

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // requesting to the user for permission.
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);

        } else {
            //if app already has permission this block will execute.
            readContacts();
        }

        getAllUsers();

        RecyclerView.LayoutManager lm= new LinearLayoutManager(getContext());
        rv.setLayoutManager(lm);
        adapter=new ContactRVAdapter(mutualContacts, getContext());
        rv.setAdapter(adapter);

        return view;
    }

    public void getAllUsers(){
        ArrayList<Profile> temp=new ArrayList<>();
        String url=Id.getIp()+"get.php";
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    for(int i=0;i<arr.length();i++) {
                        JSONObject object1=arr.getJSONObject(i);
                        String id=object1.get("id").toString();
                        String email=object1.get("email").toString();
                        String name=object1.get("name").toString();
                        String gender=object1.get("gender").toString();
                        String phone=object1.get("phoneno").toString();
                        String bio=object1.get("bio").toString();
                        String dp=object1.get("dp").toString();
                        if(arrayList.contains(phone)){
                            Log.d("list i:",id);
                            Log.d("list i:",email);
                            Log.d("list i:",name);
                            Log.d("list i:",gender);
                            mutualContacts.add(new Profile(id,email,name,gender,phone,bio,dp));
                        }

                    }
                    RecyclerView.LayoutManager lm= new LinearLayoutManager(getContext());
                    rv.setLayoutManager(lm);
                    adapter=new ContactRVAdapter(mutualContacts, getContext());
                    rv.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"Error in c=vollleyy",Toast.LENGTH_SHORT).show();
            }

        });

        RequestQueue queue= Volley.newRequestQueue(getContext());
        queue.add(stringRequest);

    }

    @Override
    public void onResume() {
        super.onResume();
        //list.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        readContacts();
    }
    // function to read contacts using content resolver
    @SuppressLint("Range")
    private void readContacts() {
        ContentResolver contentResolver=getContext().getContentResolver();
        Cursor phones=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        String number;
        while (phones.moveToNext())
        {
            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("+92","0");
            arrayList.add(number);
//                Log.d("Hello",phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        }

        phones.close();
    }
}

package com.hamzaamin.i180550_i170298;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    String CREATE_CONTACTS_TABLE="CREATE TABLE " + MyContactContract.Contact.TABLENAME + "("+MyContactContract.Contact._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
            MyContactContract.Contact._NAME+" TEXT NOT NULL,"+
            MyContactContract.Contact._PHNO+" TEXT,"+
            MyContactContract.Contact._IMAGE+ " TEXT);";

    String CREATE_CHAT_TABLE="CREATE TABLE " + MyContactContract.Chat.TABLENAME + "("+MyContactContract.Chat._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+MyContactContract.Chat._RECV_PHNO+" TEXT NOT NULL,"+
            MyContactContract.Chat._Receiver+" TEXT NOT NULL,"+
            MyContactContract.Chat._MESSAGE+ " TEXT);";

    String DELETE_CONTACTS_TABLE = "DROP TABLE IF EXISTS " + MyContactContract.Contact.TABLENAME;
    String DELETE_CHAT_TABLE = "DROP TABLE IF EXISTS " + MyContactContract.Chat.TABLENAME;

    public DBHelper(@Nullable Context context) {
        super(context, MyContactContract.DB_NAME, null, MyContactContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CONTACTS_TABLE);
        sqLiteDatabase.execSQL(CREATE_CHAT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_CONTACTS_TABLE);
        sqLiteDatabase.execSQL(DELETE_CHAT_TABLE);
        onCreate(sqLiteDatabase);
    }
}


package com.hamzaamin.i180550_i170298;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    RecyclerView rv;
    ContactRVAdapter rvAdapter;
    List<Profile> list = new ArrayList<>();
    ImageView imageView;
    Uri imgURI;


    private final int ADD_CONTACT = 0;
    DBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        imageView = findViewById(R.id.takenImage);

        SharedPreferences pref = getSharedPreferences("UserData", Context.MODE_PRIVATE);




        rv = findViewById(R.id.list);
        rv.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvAdapter = new ContactRVAdapter(list, this);
        rv.setAdapter(rvAdapter);

        ImagePicker.with(CameraActivity.this)
                .cameraOnly()
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)*/
                .saveDir(new File(getFilesDir(), "ImagePicker"))
                .start();
    }


    @SuppressLint("Range")
    void getData() {
        helper = new DBHelper(this);
        SQLiteDatabase database = helper.getReadableDatabase();
        String[] projection = new String[] {
                MyContactContract.Contact._NAME,
                MyContactContract.Contact._PHNO,
                MyContactContract.Contact._IMAGE
        };
        String sort = MyContactContract.Contact._NAME + " ASC";

        rvAdapter = new ContactRVAdapter(list, this);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        list.clear();
        getData();
        imgURI = data.getData();
        imageView.setImageURI(imgURI);
    }
}

package com.hamzaamin.i180550_i170298;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;




public class ChatFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    RecyclerView rv;
    Button nContact;
    List<Profile> contactList;
    List<Profile> mutualContacts;
    ArrayList<String> arrayList;
    ContactRVAdapter adapter;
    private final int ADD_CONTACT = 0;
    EditText searchView;
    CharSequence search ="";
    CircleImageView profileImage;
    String senderId;
    Context context;
    TextView name;

    public ChatFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChatFragment newInstance(int columnCount) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        rv = view.findViewById(R.id.list);
        arrayList=new ArrayList<>();
        contactList=new ArrayList<>();
        mutualContacts=new ArrayList<>();
        name = view.findViewById(R.id.name);

        // Set the adapter
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // requesting to the user for permission.
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 100);

        } else {
            //if app already has permission this block will execute.
            readContacts();
        }


        getAllUsers();

        RecyclerView.LayoutManager lm= new LinearLayoutManager(getContext());
        rv.setLayoutManager(lm);
        adapter=new ContactRVAdapter(mutualContacts, getContext());
        rv.setAdapter(adapter);
        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        readContacts();
    }

    @SuppressLint("Range")
    private void readContacts() {
        ContentResolver contentResolver=getContext().getContentResolver();
        Cursor phones=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        String number;
        while (phones.moveToNext())
        {
            number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).replace("+92","0");
            arrayList.add(number);
//                Log.d("Hello",phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
        }

        phones.close();
    }


    public void getAllUsers(){
        ArrayList<Profile> temp=new ArrayList<>();
        String url=Id.getIp()+"get.php";
        StringRequest stringRequest=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arr = new JSONArray(response);
                    for(int i=0;i<arr.length();i++) {
                        JSONObject object1=arr.getJSONObject(i);
                        String id=object1.get("id").toString();
                        String email=object1.get("email").toString();
                        String name=object1.get("name").toString();
                        String gender=object1.get("gender").toString();
                        String phone=object1.get("phoneno").toString();
                        String bio=object1.get("bio").toString();
                        String dp=object1.get("dp").toString();
                        if(arrayList.contains(phone)){
                            Log.d("list i:",id);
                            Log.d("list i:",email);
                            Log.d("list i:",name);
                            Log.d("list i:",gender);
                            mutualContacts.add(new Profile(id,email,name,gender,phone,bio,dp));
                        }

                    }
                    RecyclerView.LayoutManager lm= new LinearLayoutManager(getContext());
                    rv.setLayoutManager(lm);
                    adapter=new ContactRVAdapter(mutualContacts, getContext());
                    rv.setAdapter(adapter);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(),"Error in c=vollleyy",Toast.LENGTH_SHORT).show();
            }

        });

        RequestQueue queue= Volley.newRequestQueue(getContext());
        queue.add(stringRequest);

    }
}


package com.hamzaamin.i180550_i170298;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallRVAdapter extends RecyclerView.Adapter<CallRVAdapter.MyViewHolder> implements Filterable {

    List<Profile> list;
    List<Profile> filteredList;
    Context c;
    CallFragment fragment;
    public CallRVAdapter(List<Profile> list, Context c, CallFragment fragment) {
        this.c=c;
        this.list=list;
        this.filteredList = list;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(c).inflate(R.layout.fragment_call_items,parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.name.setText(filteredList.get(position).getName());
        holder.contactNo.setText(filteredList.get(position).getPhoneNo());
        Picasso.get().load(Id.getIp()+filteredList.get(position).getDp()).fit().centerCrop().into(holder.profile);
        holder.callbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 fragment.callUser(filteredList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String Key = constraint.toString();
                if(Key.isEmpty()){
                    filteredList = list;
                }
                else{
                    List<Profile> listFiltered = new ArrayList<>();
                    for (Profile row: list){
                        if(row.getName().toLowerCase().contains(Key.toLowerCase())){
                            listFiltered.add(row);

                        }
                    }
                    filteredList = listFiltered;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;

            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList =  (List<Profile>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name, contactNo;
        CircleImageView profile;
        FloatingActionButton callbtn;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            contactNo= itemView.findViewById(R.id.contactNo);
            profile=itemView.findViewById(R.id.profile);
            callbtn = itemView.findViewById(R.id.callbtn);
        }
    }
}


package com.hamzaamin.i180550_i170298.placeholder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    public static final List<PlaceholderItem> ITEMS = new ArrayList<PlaceholderItem>();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    public static final Map<String, PlaceholderItem> ITEM_MAP = new HashMap<String, PlaceholderItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPlaceholderItem(i));
        }
    }

    private static void addItem(PlaceholderItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static PlaceholderItem createPlaceholderItem(int position) {
        return new PlaceholderItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A placeholder item representing a piece of content.
     */
    public static class PlaceholderItem {
        public final String id;
        public final String content;
        public final String details;

        public PlaceholderItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}

package com.hamzaamin.i180550_i170298.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hamzaamin.i180550_i170298.CallFragment;
//import com.abubakar.i180449_i180564.CameraFragment;
//import com.mhassanakbar.i180564_i180449.ChatFragment;
//import com.mhassanakbar.i180564_i180449.ContactFragment;
import com.hamzaamin.i180550_i170298.CameraFragment;
import com.hamzaamin.i180550_i170298.ChatFragment;
import com.hamzaamin.i180550_i170298.ContactFragment;
import com.hamzaamin.i180550_i170298.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2, R.string.tab_text_3, R.string.tab_text_4};
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if(position == 3){
            return ContactFragment.newInstance(1);
        }
        if(position == 1){
            return CameraFragment.newInstance("1", "1");
        }
        if(position == 2) {
            return ChatFragment.newInstance(1);
        }
        if(position == 0) {
            return CallFragment.newInstance(1);
        }
        return PlaceholderFragment.newInstance(position + 1);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 4;
    }
}

package com.hamzaamin.i180550_i170298.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hamzaamin.i180550_i170298.databinding.FragmentDashboardBinding;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private FragmentDashboardBinding binding;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 3;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.sectionLabel;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

package com.hamzaamin.i180550_i170298.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            return "Hello world from section: " + input;
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }
}

