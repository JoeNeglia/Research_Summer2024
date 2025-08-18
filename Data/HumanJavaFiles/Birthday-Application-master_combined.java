package bday.gits;

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

        assertEquals("bday.gits", appContext.getPackageName());
    }
}


package bday.gits;

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

package bday.gits;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Videos extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);
    }
}


package bday.gits;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class FirstActivity extends AppCompatActivity {

    LinearLayout wish;
    LinearLayout website;
    LinearLayout reading;
    LinearLayout random;
    LinearLayout call;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        wish=(LinearLayout)findViewById(R.id.wishes);
        website=(LinearLayout)findViewById(R.id.websites);
        reading=(LinearLayout)findViewById(R.id.reading);
        random=(LinearLayout)findViewById(R.id.random);
        call=(LinearLayout)findViewById(R.id.call);
        wish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,Wishes.class));
            }
        });
        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,Websites.class));
            }
        });
        reading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,Reading.class));
            }
        });
        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,Random_Things.class));
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FirstActivity.this,Call_me.class));
            }
        });

    }
}


package bday.gits;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class MinionWishTrans extends AppCompatActivity {

    private int sTime =0, eTime =0, fTime = 5000, bTime = 5000;
    ImageButton play_button,forward,backward;
    MediaPlayer md;
    SeekBar seekBar;
    Button translate;
    Handler handler=new Handler();
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minion_wish_trans);
        getInIt();
        md.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                count=0;
                play_button.setBackgroundResource(android.R.drawable.ic_media_play);
            }
        });
        eTime=md.getDuration();
        seekUpdation();
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                md.stop();
                startActivity(new Intent(MinionWishTrans.this,MinionWish.class));
                finish();
            }
        });
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic();
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime+fTime)<eTime)
                {
                    sTime=sTime+fTime;
                    md.seekTo(sTime);
                }
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime-bTime)>0)
                {
                    sTime=sTime-bTime;
                    md.seekTo(sTime);
                }
            }
        });

    }
    public void getInIt()
    {
        translate=(Button)findViewById(R.id.topbtn);
        play_button=(ImageButton)findViewById(R.id.play);
        forward=(ImageButton)findViewById(R.id.forward);
        backward=(ImageButton)findViewById(R.id.backward);
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        md=MediaPlayer.create(this,R.raw.minionwishaudioeng);
        seekBar.setMax(md.getDuration());
        seekBar.setClickable(false);

    }
    public void logic()
    {
        if(count==1)
        {
            md.pause();
            count=0;
            play_button.setBackgroundResource(android.R.drawable.ic_media_play);
        }
        else {
            md.start();
            count = 1;
            play_button.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }

    public void seekUpdation() {

        sTime=md.getCurrentPosition();
        seekBar.setProgress(sTime);
        handler.postDelayed(run, 100);
    }
    Runnable run=new Runnable(){
        @Override
        public void run()
        {
            seekUpdation();
        }
    };

    @Override
    public void onBackPressed() {
        md.stop();
        finish();
        super.onBackPressed();
    }
    protected void onUserLeaveHint() {
        count=1;
        logic();
        super.onUserLeaveHint();
    }
}

package bday.gits;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class Wishes extends AppCompatActivity {

    LinearLayout ll1;
    LinearLayout ll2;
    LinearLayout ll3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishes);
        ll1=(LinearLayout)findViewById(R.id.bitwa);
        ll2=(LinearLayout)findViewById(R.id.minion);
        ll3=(LinearLayout)findViewById(R.id.dog);
        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wishes.this,BitwaWish.class));
            }
        });
        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wishes.this,MinionWish.class));
            }
        });
        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Wishes.this,DogWish.class));
            }
        });
    }
}


package bday.gits;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class DogWishTrans extends AppCompatActivity {

    private int sTime =0, eTime =0, fTime = 5000, bTime = 5000;
    ImageButton play_button,forward,backward;
    MediaPlayer md;
    SeekBar seekBar;
    Button translate;
    Handler handler=new Handler();
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_wish_trans);
        getInIt();
        md.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                count=0;
                play_button.setBackgroundResource(android.R.drawable.ic_media_play);
            }
        });
        eTime=md.getDuration();
        seekUpdation();
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(md.isPlaying())
                {
                    md.stop();
                }
                startActivity(new Intent(DogWishTrans.this,DogWish.class));
                finish();
            }
        });
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic();
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime+fTime)<eTime)
                {
                    sTime=sTime+fTime;
                    md.seekTo(sTime);
                }
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime-bTime)>0)
                {
                    sTime=sTime-bTime;
                    md.seekTo(sTime);
                }
            }
        });

    }
    public void logic()
    {
        if(count==1)
        {
            md.pause();
            count=0;
            play_button.setBackgroundResource(android.R.drawable.ic_media_play);
        }
        else {
            md.start();
            count = 1;
            play_button.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }
    public void getInIt()
    {
        translate=(Button)findViewById(R.id.topbtn);
        play_button=(ImageButton)findViewById(R.id.play);
        forward=(ImageButton)findViewById(R.id.forward);
        backward=(ImageButton)findViewById(R.id.backward);
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        md=MediaPlayer.create(this,R.raw.dogenglish);
        seekBar.setMax(md.getDuration());
        seekBar.setClickable(false);

    }
    public void seekUpdation() {

        sTime=md.getCurrentPosition();
        seekBar.setProgress(sTime);
        handler.postDelayed(run, 100);
    }
    Runnable run=new Runnable(){
        @Override
        public void run()
        {
            seekUpdation();
        }
    };

    @Override
    public void onBackPressed() {
        md.stop();
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        count=1;
        logic();
        super.onUserLeaveHint();
    }
}

package bday.gits;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class Reading extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);
    }
}


package bday.gits;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class BitwaWish extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitwa_wish);
    }
}


package bday.gits;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Call_me extends AppCompatActivity {

    LinearLayout call,insta,msg,mail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_me);
        call=(LinearLayout)findViewById(R.id.call);
        insta=(LinearLayout)findViewById(R.id.insta);
        msg=(LinearLayout)findViewById(R.id.msg);
        mail=(LinearLayout)findViewById(R.id.mail);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jadu();
            }
        });
        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.instagram.com/"));
                startActivity(intent);
            }
        });
        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.putExtra("sms_body", "");
                sendIntent.setType("vnd.android-dir/mms-sms");
                startActivity(sendIntent);
            }
        });
        mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] TO = {"a.081005@gmail.com"};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });
    }
    public void jadu()
    {
        final CharSequence options[] = new CharSequence[] {"1234567890", "0123456789"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Select your option:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if("7987785055".equals(options[which]))
                {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:7987785055"));
                    startActivity(intent);
                }
                else if("9589497123".equals(options[which]))
                {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:9589497123"));
                    startActivity(intent);
                }
            }

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //the user clicked on Cancel
            }
        });
        builder.show();
    }

}


package bday.gits;

import android.content.Intent;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class Random_Things extends AppCompatActivity {

    LinearLayout selfie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random__things);
        selfie=(LinearLayout)findViewById(R.id.selfie);
        selfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra("android.intent.extras.CAMERA_FACING",1);
                startActivity(intent);
            }
        });
    }
}


package bday.gits;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class Websites extends AppCompatActivity {

    LinearLayout gits;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websites);
        gits=(LinearLayout)findViewById(R.id.gitswebsite);
        gits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("https://prettydarncute.com/"));
                    startActivity(intent);
            }
        });
    }
}


package bday.gits;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

public class FlashActivity extends AppCompatActivity {

    MediaPlayer md;
    LinearLayout ll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);
        ll=(LinearLayout)findViewById(R.id.ll);
        ll.setVisibility(View.INVISIBLE);
        md=MediaPlayer.create(this,R.raw.bdayscream);
        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                md.start();
                Animation startAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                ll.startAnimation(startAnimation);
                ll.setVisibility(View.VISIBLE);
            }
        },800);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ;
            }
        },1000);
        Button btn=(Button)findViewById(R.id.pButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FlashActivity.this,FirstActivity.class));
                finish();
            }
        });
    }
}


package bday.gits;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class MinionWish extends AppCompatActivity {

    private int sTime =0, eTime =0, fTime = 5000, bTime = 5000;
    ImageButton play_button,forward,backward;
    MediaPlayer md;
    SeekBar seekBar;
    Button translate;
    Handler handler=new Handler();
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minion_wish);
        getInIt();
        md.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                count=0;
                play_button.setBackgroundResource(android.R.drawable.ic_media_play);
            }
        });
        eTime=md.getDuration();
        seekUpdation();
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                md.stop();
                startActivity(new Intent(MinionWish.this,MinionWishTrans.class));
                finish();
            }
        });
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logic();
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime+fTime)<eTime)
                {
                    sTime=sTime+fTime;
                    md.seekTo(sTime);
                }
            }
        });
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((sTime-bTime)>0)
                {
                    sTime=sTime-bTime;
                    md.seekTo(sTime);
                }
            }
        });

    }
    public void getInIt()
    {
        translate=(Button)findViewById(R.id.topbtn);
        play_button=(ImageButton)findViewById(R.id.play);
        forward=(ImageButton)findViewById(R.id.forward);
        backward=(ImageButton)findViewById(R.id.backward);
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        md=MediaPlayer.create(this,R.raw.minionwishaudiotranslated);
        seekBar.setMax(md.getDuration());
        seekBar.setClickable(false);
    }
    public void logic()
    {
        if(count==1)
        {
            md.pause();
            count=0;
            play_button.setBackgroundResource(android.R.drawable.ic_media_play);
        }
        else {
            md.start();
            count = 1;
            play_button.setBackgroundResource(android.R.drawable.ic_media_pause);
        }
    }

    public void seekUpdation() {

        sTime=md.getCurrentPosition();
        seekBar.setProgress(sTime);
        handler.postDelayed(run, 100);
    }
    Runnable run=new Runnable(){
        @Override
        public void run()
        {
            seekUpdation();
        }
    };

    @Override
    public void onBackPressed() {
        finish();
        md.stop();
        super.onBackPressed();
    }

    @Override
    protected void onUserLeaveHint() {
        count=1;
        logic();
        super.onUserLeaveHint();
    }
}

package bday.gits;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class DogWish extends AppCompatActivity {

    Button translate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_wish);
        translate = (Button) findViewById(R.id.topbtn);
        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DogWish.this, DogWishTrans.class));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}

