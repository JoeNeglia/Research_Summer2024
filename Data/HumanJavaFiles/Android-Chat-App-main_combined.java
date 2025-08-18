package com.example.chatapp;

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
        assertEquals("com.example.chatapp", appContext.getPackageName());
    }
}

package com.example.chatapp;

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

package models;

public class MessageModel {

    String uId, msgText;
    long msgTime;

    public MessageModel() {
    }

    public MessageModel(long msgTime, String msgText) {
        this.msgTime = msgTime;
        this.msgText = msgText;
    }

    public MessageModel(String uId, String msgText, long msgTime) {
        this.uId = uId;
        this.msgText = msgText;
        this.msgTime = msgTime;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public long getMsgTime() {
        return msgTime;
    }

    public void setMsgTime(long msgTime) {
        this.msgTime = msgTime;
    }
}


package models;

import com.example.chatapp.R;

public class UserModel {

    String profilePic = "R.drawable.user", userName, userMail, userId, userPassword = "null", recentMessage, about, token;
    long  recentMsgTime;

    public UserModel(String profilePic, String userName, String userMail, String userId, String userPassword, String about) {
        this.profilePic = profilePic;
        this.userName = userName;
        this.userMail = userMail;
        this.userId = userId;
        this.userPassword = userPassword;
        this.about = about;
    }


    // For storing in DB
    public UserModel(String userName, String userMail, String userPassword, String profilePic, String about){

        this.profilePic = profilePic;
        this.userName = userName;
        this.userMail = userMail;
        this.userPassword = userPassword;
        this.about = about;


    }


    public UserModel() {
    }

    // for displaying in chats list and search list
    public UserModel(String userName, String userMail, String profilePic) {
        this.userName = userName;
        this.userMail = userMail;
        this.profilePic = profilePic;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAbout() {
        return about;
    }

    public long getRecentMsgTime() {
        return recentMsgTime;
    }

    public void setRecentMsgTime(long recentMsgTime) {
        this.recentMsgTime = recentMsgTime;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }


    public String getRecentMessage() {
        return recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage;
    }

}


package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import models.UserModel;


public class chatPageAdapter extends RecyclerView.Adapter<chatPageAdapter.ViewHolder> {

    private final ArrayList<UserModel> userData;
    Context context;
    private static OnClickListener listener;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;

    public chatPageAdapter(ArrayList<UserModel> userData, Context context) {
        this.userData = userData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chats_list_items, parent, false);

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        userData.sort(Comparator.comparing(UserModel::getRecentMsgTime).reversed());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();


        // Show date/time on contact list
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(userData.get(position).getRecentMsgTime());
        final String timeString1 =
                new SimpleDateFormat("dd-M-yy HH:mm").format(cal.getTime());
        final String timeString2 =
                new SimpleDateFormat("dd-M-yy HH:mm").format(new Date().getTime());
        int diff = Integer.parseInt(timeString2.substring(0, 2)) - Integer.parseInt(timeString1.substring(0, 2));

        if (diff < 1) {
            holder.recent_time.setText(timeString1.trim().substring(8));
        } else if (diff >= 1) {
            holder.recent_time.setText(timeString1.trim().substring(0, 8));
        }


        holder.chat_name.setText(userData.get(position).getUserName());
        holder.recent_message.setText(userData.get(position).getRecentMessage());

        String picUrl = userData.get(position).getProfilePic();

        Picasso.get().load(picUrl)
                .fit().centerCrop()
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(holder.profile_pic_imageview);

    }


    @Override
    public int getItemCount() {
        return userData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView chat_name;
        private final ImageView profile_pic_imageview;
        private final TextView recent_message;
        private final TextView recent_time;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chat_name = itemView.findViewById(R.id.chat_name);
            profile_pic_imageview = itemView.findViewById(R.id.profile_pic_imageview);
            recent_message = itemView.findViewById(R.id.recent_message);
            recent_time = itemView.findViewById(R.id.recent_time);


            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int pos = getAdapterPosition();

                    if (listener != null && pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(userData.get(pos));
                    }
                }
            });
        }
    }

    public interface OnClickListener {
        void onItemClick(UserModel userdata);
    }

    public void setOnItemClickListener(OnClickListener listener) {
        chatPageAdapter.listener = listener;
    }

}


package adapters;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import models.MessageModel;

public class messageAdapter extends RecyclerView.Adapter {

    ArrayList<MessageModel> msgData;
    Context context;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    final int SENDER_VIEWHOLDER = 0;
    final int RECEIVER_VIEWHOLDER = 1;


    public messageAdapter(ArrayList<MessageModel> msgData, Context context) {

        this.msgData = msgData;
        this.context = context;

    }

    @Override
    public int getItemViewType(int position) {

        if (msgData.get(position).getuId().equals(firebaseAuth.getUid()))
            return SENDER_VIEWHOLDER;
        else
            return RECEIVER_VIEWHOLDER;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == SENDER_VIEWHOLDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_listitem, parent, false);
            return new OutgoingViewholder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_listitem, parent, false);
            return new IncomingViewholder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder.getClass() == OutgoingViewholder.class) {
            ((OutgoingViewholder) holder).outgoingMsg.setText(msgData.get(position).getMsgText());

            long time = msgData.get(position).getMsgTime();
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            final String timeString =
                    new SimpleDateFormat("HH:mm").format(cal.getTime());

            ((OutgoingViewholder) holder).outgoingMsgTime.setText(timeString);
        } else {

            ((IncomingViewholder) holder).incomingMsg.setText(msgData.get(position).getMsgText());
            long time = msgData.get(position).getMsgTime();
            final Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            final String timeString =
                    new SimpleDateFormat("HH:mm").format(cal.getTime());

            ((IncomingViewholder) holder).incomingMsgTime.setText(timeString);
        }


    }

    @Override
    public int getItemCount() {
        return msgData.size();
    }

    public class OutgoingViewholder extends RecyclerView.ViewHolder {

        TextView outgoingMsg, outgoingMsgTime;


        public OutgoingViewholder(@NonNull View itemView) {
            super(itemView);

            outgoingMsg = itemView.findViewById(R.id.outgoing_msg);
            outgoingMsgTime = itemView.findViewById(R.id.outgoing_msg_time);
        }
    }

    public class IncomingViewholder extends RecyclerView.ViewHolder {

        TextView incomingMsg, incomingMsgTime;

        public IncomingViewholder(@NonNull View itemView) {
            super(itemView);

            incomingMsg = itemView.findViewById(R.id.incoming_msg);
            incomingMsgTime = itemView.findViewById(R.id.incoming_msg_time);
        }
    }

}


package com.example.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    NotificationManager mNotificationManager;
    SharedPreferences sharedPreferences;
    public String nToken;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        sharedPreferences = getSharedPreferences("SavedToken",MODE_PRIVATE);

        nToken = s;
        SharedPreferences.Editor tokenEdit = sharedPreferences.edit();

        tokenEdit.putString("ntoken",s);
        tokenEdit.commit();

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.setLooping(false);
        }

        // vibration
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 300};
        v.vibrate(pattern, -1);


        int resourceImage = getResources().getIdentifier(remoteMessage.getNotification().getIcon(), "drawable", getPackageName());



        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CHANNEL_ID");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(resourceImage);
        } else {
            builder.setSmallIcon(resourceImage);
        }



        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentTitle(remoteMessage.getNotification().getTitle());
        builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setContentIntent(pendingIntent);
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()));
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);

        mNotificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);




        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }



// notificationId is a unique int for each notification that you must define
        mNotificationManager.notify(100, builder.build());


    }

}


package com.example.chatapp;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.chatapp.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


public class ProfileFragment extends Fragment {

   FirebaseAuth firebaseAuth;
   FirebaseDatabase firebaseDatabase;
   FragmentProfileBinding binding;
   FragmentManager fragmentManager;
   FirebaseStorage firebaseStorage;

    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        fragmentManager = getChildFragmentManager();

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();



        String uid = firebaseAuth.getUid();

        binding.uid.setText(uid);

        binding.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.editFragContainer.setVisibility(View.VISIBLE);
                binding.editFragContainer.bringToFront();
                binding.uid.setVisibility(View.GONE);

                binding.text.setText("Enter new name");

                binding.edittext.requestFocus();
                binding.saveEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ss = binding.edittext.getText().toString().trim();




                        firebaseDatabase.getReference("Users").child(firebaseAuth.getUid()).child("userName").setValue(ss).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Toast.makeText(getContext(), "Username editted ", Toast.LENGTH_SHORT).show();
                                binding.edittext.setText("");
                                binding.editFragContainer.setVisibility(View.GONE);
                            }
                        });



                    }
                });

            }
        });

        binding.about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.editFragContainer.setVisibility(View.VISIBLE);
                binding.editFragContainer.bringToFront();
                binding.uid.setVisibility(View.GONE);


                binding.text.setText("Enter about");
                binding.edittext.setHint("about");

                binding.edittext.requestFocus();
                binding.saveEditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String ss = binding.edittext.getText().toString().trim();

                        firebaseDatabase.getReference("Users").child(firebaseAuth.getUid()).child("about").setValue(ss);
                        binding.edittext.setText("");
                        binding.editFragContainer.setVisibility(View.GONE);

                    }
                });

            }
        });
        



        binding.newPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                 CropImage.activity()
                         .setAspectRatio(3,3)
                         .setGuidelines(CropImageView.Guidelines.ON)
                         .setFixAspectRatio(true).setOutputCompressQuality(60)
                        .start(getContext(),ProfileFragment.this);


            }
        });




                firebaseDatabase.getReference("Users").child(uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String uName = snapshot.child("userName").getValue().toString();
                        String uMail = snapshot.child("userMail").getValue().toString();
                        String uPic = snapshot.child("profilePic").getValue().toString();
                        String uAbout = snapshot.child("about").getValue().toString();

                        Picasso.get().load(uPic).error(R.drawable.user)
                                .placeholder(R.drawable.user).centerCrop().fit()
                                .into(binding.profilePicImageview);

                        binding.username.setText(uName);
                        binding.usermail.setText(uMail);
                        binding.about.setText(uAbout);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                return binding.getRoot();
            }



        @Override
         public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();


                    Picasso.get().load(resultUri).fit().centerCrop().into(binding.profilePicImageview);

                    final StorageReference storageRef = firebaseStorage.getReference().child("Profile pictures").child(firebaseAuth.getUid());
                    storageRef.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    firebaseDatabase.getReference("Users").child(firebaseAuth.getUid()).child("profilePic").setValue(uri.toString());

                                }
                            });

                        }
                    });


                }
            }

        }
}

package com.example.chatapp;


import android.app.Activity;
import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class FcmNotificationsSender  {

    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAA5nu0z6g:APA91bG2BDQQZrvxpEDSNrK-QSkpENW6GoGXbsfuwj2SVazM3DLIa4xZP0eRjYYTvjfwZYn1zm8wBoHDiuEg3F59qpRThnOwJku1hA7Kfsw4qynzNJ6e5uO4yFpWYrhHbzDK80Sk2iGc";
    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;
    private RequestQueue requestQueue;

    public FcmNotificationsSender(String userFcmToken, String title, String body, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;


    }

    public void SendNotifications() {

        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("icon", "icon_for_splash");
            mainObj.put("notification", notiObject);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    // code run is got response

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {


                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;


                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }




    }
}


package com.example.chatapp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils
{

    private static final byte[] keyValue =
            new byte[]{'c', 'o', 'd', 'i', 'n', 'g', 'a', 'f', 'f', 'a', 'i', 'r', 's', 'c', 'o', 'm'};


    public static String encrypt(String cleartext)
            throws Exception {
        byte[] rawKey = getRawKey();
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    public static String decrypt(String encrypted)
            throws Exception {

        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(enc);
        return new String(result);
    }

    private static byte[] getRawKey() throws Exception {
        SecretKey key = new SecretKeySpec(keyValue, "AES");
        byte[] raw = key.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKey skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] encrypted)
            throws Exception {
        SecretKey skeySpec = new SecretKeySpec(keyValue, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}

package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.chatapp.databinding.ActivitySettingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding activitySettingBinding;
    FragmentManager fragmentManager;
    FirebaseAuth myAuth;
    FragmentTransaction fragmentTransaction;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        activitySettingBinding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(activitySettingBinding.getRoot());

        fragmentManager = getSupportFragmentManager();

        myAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();


        activitySettingBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = getSupportFragmentManager();
                if(fm.getBackStackEntryCount()>0) {
                    fm.popBackStack();
                    activitySettingBinding.profile.setVisibility(View.VISIBLE);
                    activitySettingBinding.logout.setVisibility(View.VISIBLE);
                }else {
                    Intent i = new Intent(SettingActivity.this, MainActivity.class);
                    startActivity(i);
                }
            }
        });


        activitySettingBinding.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activitySettingBinding.profile.setVisibility(View.GONE);
                activitySettingBinding.logout.setVisibility(View.GONE);

                fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.setting_container,ProfileFragment.class,null)
                                .addToBackStack(null)
                                .commit();


            }
        });

        activitySettingBinding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseDatabase.getReference("Users").child(myAuth.getUid())
                        .child("token").setValue("");
                myAuth.signOut();
                Intent intent = new Intent(SettingActivity.this, SignupActivity.class);
                startActivity(intent);

            }
        });



    }

    @Override
    public void onBackPressed() {

        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount()>0) {
            fm.popBackStack();
            activitySettingBinding.profile.setVisibility(View.VISIBLE);
            activitySettingBinding.logout.setVisibility(View.VISIBLE);
        }
        else
            super.onBackPressed();

//
    }
}

package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import adapters.chatPageAdapter;
import models.UserModel;


public class MainActivity extends AppCompatActivity {


    FirebaseAuth myAuth;
    ActivityMainBinding activityMainBinding;
    FirebaseDatabase firebaseDatabase;
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    chatPageAdapter chatPageAdapter;
    ArrayList<UserModel> userData = new ArrayList<>();
    Toolbar myToolbar;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        firebaseDatabase = FirebaseDatabase.getInstance();
        myAuth = FirebaseAuth.getInstance();

        userId = myAuth.getCurrentUser().getUid();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        myToolbar = activityMainBinding.myToolbar;
        myToolbar.inflateMenu(R.menu.main_menu);



        setContentView(activityMainBinding.getRoot());

        activityMainBinding.tutorial.setVisibility(View.GONE);

        if(!isOnline()){
            Toast.makeText(MainActivity.this, "Check Internet Connection", Toast.LENGTH_LONG).show();
        }

        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

               Intent intent  = new Intent(MainActivity.this,SettingActivity.class);
               startActivity(intent);

                return true;
            }
        });


        activityMainBinding.moveToContactlistFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ContactListsActivity.class);
                startActivity(intent);

            }
        });


        chatPageAdapter = new chatPageAdapter(userData, MainActivity.this);
        executorService.execute(new Runnable() {
            @Override
            public void run() {



                firebaseDatabase.getReference("Users").addValueEventListener(new ValueEventListener() {



                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {



                        userData.clear();
                        ArrayList<String> contactIds = new ArrayList<>();
                        ArrayList<Long> recentMsgTimes = new ArrayList<>();
                        ArrayList<String> recentMsg = new ArrayList<>();


                        if(snapshot.child(userId).hasChild("Contacts"))
                            for (DataSnapshot e : snapshot.child(myAuth.getUid()).child("Contacts").getChildren()){
                                contactIds.add(e.getKey());


                                if(e.hasChild("interactionTime")) {
                                    recentMsgTimes.add((long)e.child("interactionTime").getValue());
                                }

                                if(e.hasChild("recentMessage")){
                                    recentMsg.add(e.child("recentMessage").getValue().toString());
                                }

                            }

                        if(contactIds.isEmpty()){
                            activityMainBinding.tutorial.setVisibility(View.VISIBLE);
                        }else{
                            activityMainBinding.tutorial.setVisibility(View.GONE);

                        }


                        for(int i=0;i<contactIds.size();i++) {

                            String e = contactIds.get(i);
                            long time = 0;
                            String recentmsg = "";

                            try{
                                if(!recentMsgTimes.isEmpty()){time = recentMsgTimes.get(i);}
                                if(!recentMsg.isEmpty()){recentmsg = recentMsg.get(i);}
                            }catch (IndexOutOfBoundsException err){

                            }




                            String uName = snapshot.child(e).child("userName").getValue().toString();
                            String uMail = snapshot.child(e).child("userMail").getValue().toString();
                            String uPic = snapshot.child(e).child("profilePic").getValue().toString();
                            String token = snapshot.child(e).child("token").getValue().toString();

                            UserModel model = new UserModel(uName, uMail, uPic);
                            model.setUserId(e);
                            model.setRecentMsgTime(time);
                            model.setToken(token);
                            model.setRecentMessage(recentmsg);
                            userData.add(model);
                            chatPageAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

//        Drawable drawable =  ContextCompat.getDrawable(MainActivity.this,R.drawable.divider);
        DividerItemDecoration decoration = new DividerItemDecoration(activityMainBinding.chatsRecyclerview.getContext(), DividerItemDecoration.VERTICAL);
        activityMainBinding.chatsRecyclerview.addItemDecoration(decoration);
        activityMainBinding.chatsRecyclerview.setLayoutManager(new LinearLayoutManager(MainActivity.this));


        activityMainBinding.chatsRecyclerview.setAdapter(chatPageAdapter);


        chatPageAdapter.setOnItemClickListener(new chatPageAdapter.OnClickListener() {
            @Override
            public void onItemClick(UserModel userdata) {


                Intent intent = new Intent(MainActivity.this, MessagingActivity.class);
                intent.putExtra("USERNAME", userdata.getUserName());
                intent.putExtra("PROFILEIMAGE", userdata.getProfilePic());
                intent.putExtra("USERID", userdata.getUserId());
                intent.putExtra("TOKEN", userdata.getToken());
                startActivity(intent);


            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

}


package com.example.chatapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.chatapp.databinding.ActivityContactListsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import models.UserModel;

public class ContactListsActivity extends AppCompatActivity {

    ActivityContactListsBinding activityContactListsBinding;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    ArrayList<UserModel> searchedUser = new ArrayList<>(1);


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar()!=null){
            getSupportActionBar().hide();
        }

        activityContactListsBinding = ActivityContactListsBinding.inflate(getLayoutInflater());
        setContentView(activityContactListsBinding.getRoot());
        activityContactListsBinding.newUserDisplay.setVisibility(View.GONE);


        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        activityContactListsBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                activityContactListsBinding.searchView.clearFocus();
                searchedUser.clear();

              firebaseDatabase.getReference("Users").addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot snapshot) {

                      boolean flag = false;

                      for(DataSnapshot e : snapshot.getChildren()){

                                flag = e.getKey().equals(firebaseAuth.getCurrentUser().getUid());


                          if(!flag && e.child("userMail").getValue().equals(query.trim()) ) {


                              Log.d("testcontact"," "+query+"  name= "+e.child("userName").getValue().toString());
                              UserModel userModel = new UserModel();
                              userModel.setUserName(e.child("userName").getValue().toString());

                              userModel.setUserId(e.getKey());
                              searchedUser.add(userModel);

                              activityContactListsBinding.userName.setText(e.child("userName").getValue().toString());
                              activityContactListsBinding.usermail.setText(e.child("userMail").getValue().toString());
                              String pic = e.child("profilePic").getValue().toString();
                              Picasso.get().load(pic)
                                      .fit()
                                      .centerCrop()
                                      .error(R.drawable.user)
                                      .placeholder(R.drawable.user)
                                      .into(activityContactListsBinding.profilePicImageview);

                              activityContactListsBinding.newUserDisplay.setVisibility(View.VISIBLE);

                              activityContactListsBinding.addContactBtn.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {

                                      Toast.makeText(ContactListsActivity.this, "Contact added", Toast.LENGTH_SHORT).show();

                                      String userId = searchedUser.get(0).getUserId();

                                      firebaseDatabase.getReference("Users").child(firebaseAuth.getCurrentUser().getUid())
                                              .child("Contacts").child(userId).setValue("Chats");
                                      searchedUser.clear();

                                      firebaseDatabase.getReference("Users").child(firebaseAuth.getUid()).child("Contacts").child(userId)
                                              .child("interactionTime").setValue(new Date().getTime());
                                      firebaseDatabase.getReference("Users").child(firebaseAuth.getUid()).child("Contacts").child(userId)
                                              .child("recentMessage").setValue("");



                                  }
                              });

                          }
                      }

                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError error) {
                      searchedUser.clear();
                  }
              });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });





    }


}

package com.example.chatapp;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.chatapp.databinding.ActivityMessagingBinding;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import adapters.messageAdapter;
import models.MessageModel;

public class MessagingActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    ActivityMessagingBinding activityMessagingBinding;
    public String receiverId;
    String receiverToken, senderName;
    String senderId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        activityMessagingBinding = ActivityMessagingBinding.inflate(getLayoutInflater());
        setContentView(activityMessagingBinding.getRoot());

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                activityMessagingBinding.parentViewgroup.setBackground(AppCompatResources.getDrawable(MessagingActivity.this,R.drawable.wpdark));
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                activityMessagingBinding.parentViewgroup.setBackground(AppCompatResources.getDrawable(MessagingActivity.this,R.drawable.wplight));
                break;
        }

        senderId = firebaseAuth.getUid();

        Intent intent = getIntent();
        String uname = intent.getStringExtra("USERNAME");
        String profileImg = intent.getStringExtra("PROFILEIMAGE");
        receiverId = intent.getStringExtra("USERID");
        receiverToken = intent.getStringExtra("TOKEN");


        activityMessagingBinding.receiverName.setText(uname);
        Picasso.get().load(profileImg).fit().centerCrop()
                .error(R.drawable.user)
                .placeholder(R.drawable.user)
                .into(activityMessagingBinding.profilePicImageview);


        activityMessagingBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MessagingActivity.this,MainActivity.class);
               startActivity(intent);
            }
        });


        final ArrayList<MessageModel> msgData = new ArrayList<>();
        final messageAdapter msgAdapter = new messageAdapter(msgData,MessagingActivity.this);
        activityMessagingBinding.msgRecyclerview.setAdapter(msgAdapter);
        activityMessagingBinding.msgRecyclerview.setLayoutManager(new LinearLayoutManager(this));


        firebaseDatabase.getReference("Users")
                .child(senderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){

                senderName = dataSnapshot.child("userName").getValue().toString();
                msgData.clear();

                for (DataSnapshot e : dataSnapshot.child("Contacts").child(receiverId).child("Chats").getChildren()){


                    String msg = e.child("msgText").getValue().toString();
                    
                    try {
                        decrypted = AESUtils.decrypt(msg);
                    } catch (Exception er) {
                        er.printStackTrace();
                    }

                    msgData.add(new MessageModel(e.child("uId").getValue().toString()
                            ,decrypted
                            ,(Long) Long.valueOf(e.child("msgTime").getValue().toString())));

                }

                msgAdapter.notifyDataSetChanged();
                activityMessagingBinding.msgRecyclerview.scrollToPosition(msgAdapter.getItemCount()-1);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });



//        FirebaseMessaging.getInstance().subscribeToTopic("all");

        //Messaging Mechanism
        activityMessagingBinding.sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = activityMessagingBinding.typingSpace.getText().toString().trim();
              
                String encryptedMsg = msg;
                try {
                    encryptedMsg = AESUtils.encrypt(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                long date = new Date().getTime();

                activityMessagingBinding.typingSpace.setText("");
                final MessageModel messageModel = new MessageModel(senderId, encryptedMsg, date);

                if(!msg.isEmpty()) {
                    firebaseDatabase.getReference("Users").child(senderId).child("Contacts")
                            .child(receiverId).child("Chats").push()
                            .setValue(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            activityMessagingBinding.msgRecyclerview.scrollToPosition(msgAdapter.getItemCount()-1);

                            FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(receiverToken,senderName
                                    ,msg,getApplicationContext(),MessagingActivity.this);
                            fcmNotificationsSender.SendNotifications();

                            firebaseDatabase.getReference("Users").child(receiverId).child("Contacts").child(senderId)
                                    .child("interactionTime").setValue(date);

                            firebaseDatabase.getReference("Users").child(senderId).child("Contacts").child(receiverId)
                                    .child("interactionTime").setValue(date);


                            firebaseDatabase.getReference("Users").child(receiverId).child("Contacts")
                                    .child(senderId).child("Chats").push()
                                    .setValue(messageModel);

                            firebaseDatabase.getReference("Users").child(senderId).child("Contacts").child(receiverId)
                                    .child("recentMessage").setValue(msg);

                            firebaseDatabase.getReference("Users").child(receiverId).child("Contacts").child(senderId)
                                    .child("recentMessage").setValue(msg);


                        }
                    });
                }


            }
        });



        activityMessagingBinding.msgRecyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override

            public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop,int oldRight, int oldBottom)
            {

                if ( bottom < oldBottom) {
                    activityMessagingBinding.msgRecyclerview.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if((msgAdapter.getItemCount()-1)>1)
                            activityMessagingBinding.msgRecyclerview.smoothScrollToPosition(msgAdapter.getItemCount()-1);
                        }
                    }, 10);
                }

            }
        });

    }




}


package com.example.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.databinding.ActivitySignupBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import models.UserModel;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding activitySignupBinding;
    private FirebaseAuth myAuth;
    FirebaseDatabase firebaseDatabase;
    ActivityResultLauncher<Intent> activityResultLauncher;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if(myAuth.getCurrentUser()!=null){
            Intent intent = new Intent(SignupActivity.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }

        activitySignupBinding = ActivitySignupBinding.inflate(getLayoutInflater());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(activitySignupBinding.getRoot());

        activitySignupBinding.progressBar.setVisibility(View.GONE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        activitySignupBinding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email =  activitySignupBinding.mail.getText().toString().trim();
                String password = activitySignupBinding.password.getText().toString().trim();
                String userName = activitySignupBinding.username.getText().toString().trim();
                String about = "online";

                if(!email.isEmpty() && !password.isEmpty() && !userName.isEmpty())
                    signupUser(email,password, userName,about);
                else
                    Toast.makeText(SignupActivity.this, "Enter details", Toast.LENGTH_SHORT).show();
            }
        });

        activitySignupBinding.hidePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(activitySignupBinding.password.getTransformationMethod()!=null)
                    activitySignupBinding.password.setTransformationMethod(null);
                else activitySignupBinding.password.setTransformationMethod(new PasswordTransformationMethod());

            }
        });

        activitySignupBinding.moveToSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this,SigninActivity.class);
                startActivity(intent);
            }
        });


        //      Signin with google

        activitySignupBinding.googleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                signInWithGoogle();
                activitySignupBinding.progressBar.setVisibility(View.VISIBLE);

                        Intent signInIntent  = mGoogleSignInClient.getSignInIntent();
                        activityResultLauncher.launch(signInIntent);



            }
        });


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {



                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());

                    } catch (ApiException e) {
                        // Google Sign In failed, update UI appropriately
                        Toast.makeText(SignupActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                activitySignupBinding.progressBar.setVisibility(View.GONE);
            }
        });

    }



    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        myAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String id =  task.getResult().getUser().getUid();


                            // To not override default user values in DB when signing again with google
                            firebaseDatabase.getReference().child("Users").child(id).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                @Override
                                public void onSuccess(DataSnapshot dataSnapshot) {

                                    if(!dataSnapshot.hasChild("userName")){

                                        String defaultUserName = task.getResult().getUser().getEmail();
                                        String about = "Online";

                                        sharedPreferences = getSharedPreferences("SavedToken",MODE_PRIVATE);
                                        String tokenInMain =  sharedPreferences.getString("ntoken","mynull");

                                        UserModel userModel = new UserModel(defaultUserName.substring(0, defaultUserName.indexOf('@'))
                                                , task.getResult().getUser().getEmail()
                                                , "null"
                                                , task.getResult().getUser().getPhotoUrl().toString()
                                                , about);

                                        userModel.setToken(tokenInMain);

                                        firebaseDatabase.getReference().child("Users").child(id).setValue(userModel);


                                    }else{

                                        sharedPreferences = getSharedPreferences("SavedToken",MODE_PRIVATE);
                                        String tokenInMain =  sharedPreferences.getString("ntoken","mynull");
                                        firebaseDatabase.getReference("Users").child(id).child("token").setValue(tokenInMain);

                                    }
                                }
                            });

                            activitySignupBinding.progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(SignupActivity.this,MainActivity.class);
                            startActivity(intent);

//                            }

                        } else {
                            Toast.makeText(SignupActivity.this, task.getException().getLocalizedMessage()+"", Toast.LENGTH_SHORT).show();
                            Log.d("TAG2", "signInWithCredential:failure", task.getException());
                            activitySignupBinding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }



    private void signupUser(String email, String password, String userName, String about){

        activitySignupBinding.progressBar.setVisibility(View.VISIBLE);


        myAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        activitySignupBinding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {

                           String id2 =  task.getResult().getUser().getUid();



                                        UserModel userModel = new UserModel(userName,email,password,"R.drawable.user",about);

                                        sharedPreferences = getSharedPreferences("SavedToken",MODE_PRIVATE);
                                        String tokenInMain =  sharedPreferences.getString("ntoken","mynull");
                                        userModel.setToken(tokenInMain);

                                        firebaseDatabase.getReference().child("Users")
                                                .child(id2)
                                                .setValue(userModel);


                            Intent intent = new Intent(SignupActivity.this,MainActivity.class);
                            startActivity(intent);







                        } else {
                            Toast.makeText(SignupActivity.this, "SignUp failed "+task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




}

package com.example.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import com.example.chatapp.databinding.ActivitySigninBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SigninActivity extends AppCompatActivity {


    FirebaseAuth myAuth;
    ActivitySigninBinding activitySigninBinding;
    SharedPreferences sharedPreferences;
    FirebaseDatabase firebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activitySigninBinding = ActivitySigninBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(activitySigninBinding.getRoot());

//        sharedPreferences = getSharedPreferences("SavedToken",MODE_PRIVATE);


        myAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        activitySigninBinding.progressBar.setVisibility(View.GONE);


        activitySigninBinding.hidePassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(activitySigninBinding.signinPassword.getTransformationMethod()!=null)
                     activitySigninBinding.signinPassword.setTransformationMethod(null);
                else activitySigninBinding.signinPassword.setTransformationMethod(new PasswordTransformationMethod());

            }
        });


        activitySigninBinding.signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = activitySigninBinding.signinMail.getText().toString().trim();
                String password = activitySigninBinding.signinPassword.getText().toString().trim();

                activitySigninBinding.progressBar.setVisibility(View.VISIBLE);

                if(!email.isEmpty() && !password.isEmpty()) {

                    myAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SigninActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    activitySigninBinding.progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {

                                        String id =  task.getResult().getUser().getUid();


                                            sharedPreferences = getSharedPreferences("SavedToken",MODE_PRIVATE);
                                            String tokenInMain =  sharedPreferences.getString("ntoken","mynull");
                                            firebaseDatabase.getReference("Users").child(id).child("token").setValue(tokenInMain);




                                        Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                                        startActivity(intent);

                                    } else {
                                        activitySigninBinding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(SigninActivity.this, "Try again - " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else {
                    activitySigninBinding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(SigninActivity.this, "Enter details", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}

