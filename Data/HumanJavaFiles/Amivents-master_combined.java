package com.example.layoutpractise3;

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

        assertEquals("com.example.layoutpractise3", appContext.getPackageName());
    }
}


package com.example.layoutpractise3;

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

package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AmtcEvents extends AppCompatActivity {
    private Button upcomimgEvents;
    private Button CurrentEvents;
    private Button PastEvents;
    private TextView ClubTitle;

    int Value,nvalue=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amtc_events);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
             Value = bundle.getInt("value");
        }
        ClubTitle=findViewById(R.id.ClubEventTitle);

        switch(Value)
        {
            case 11: ClubTitle.setText("AMTC: Events");
                break;
            case 12: ClubTitle.setText("ALIAS: Events");
                break;
            case 21: ClubTitle.setText("CEZZANE: Events");
                break;
            case 31: ClubTitle.setText("DASP: Events");
                break;
            case 41: ClubTitle.setText("STROKES: Events");
                break;
            case 51: ClubTitle.setText("AZMIE: Events");
                break;
            case 61: ClubTitle.setText("ALFAAZ: Events");
                break;
        }
        upcomimgEvents=findViewById(R.id.btn_upcom_events);
        CurrentEvents=findViewById(R.id.btn_curr_events);
        PastEvents=findViewById(R.id.btn_past_events);

        upcomimgEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent upet=new Intent(AmtcEvents.this, amtc_UpEvents.class);
                nvalue=100*Value+21;
                upet.putExtra("value",nvalue);
                startActivity(upet);
            }
        });

        CurrentEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent curret=new Intent(AmtcEvents.this, amtc_UpEvents.class);
                nvalue=100*Value+22;
                curret.putExtra("value",nvalue);
                startActivity(curret);
            }
        });

        PastEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pastet=new Intent(AmtcEvents.this, amtc_UpEvents.class);
                nvalue=100*Value+23;
                pastet.putExtra("value",nvalue);
                startActivity(pastet);
            }
        });
    }

}


package com.example.layoutpractise3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class aboutus extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.aboutusview, container, false);
    }
}


package com.example.layoutpractise3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Amtc_UpEvent_Adapter extends RecyclerView.Adapter<Amtc_UpEvent_Adapter.MyViewHolder> {


    String edata1[], edata2[];
    int images[];
    int Value;
    Context context;

    public Amtc_UpEvent_Adapter(Context ct, String[] s1, String[] s2, int[] img, int Value) {
        context = ct;
        edata1= s1;
        edata2=s2;
        images=img;
        this.Value=Value;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater= LayoutInflater.from(context);
        View view1= inflater.inflate(R.layout.amtc_eventrow, parent, false);
        return new MyViewHolder(view1);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.eText1.setText(edata1[position]);
        holder.eText2.setText(edata2[position]);
        holder.eImage.setImageResource(images[position]);

        holder.emainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it= new Intent(context, AmtcUpEventsDisp.class);
                it.putExtra("data1",edata1[position]);
                it.putExtra("data2",edata2[position]);
                it.putExtra("myImage",images[position]);
                it.putExtra("position",position);
                it.putExtra("value",Value);
                context.startActivity(it);
            }
        });
    }

    @Override
    public int getItemCount() {
        return edata1.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView eText1, eText2;
        ImageView eImage;
        LinearLayout emainLayout;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            eText1=itemView.findViewById(R.id.eventtitle);
            eText2=itemView.findViewById(R.id.eventdaydate);
            eImage=itemView.findViewById(R.id.eventimg);
            emainLayout=itemView.findViewById(R.id.parent_1layout);
        }
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class Aboutamtc extends AppCompatActivity {

    int Value;

    private TextView InfoTitle, AboutPara;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutamtc);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }

        InfoTitle=findViewById(R.id.Infotitle);
        AboutPara=findViewById(R.id.txtpara);

        switch(Value)
        {
            case 11: InfoTitle.setText("ABOUT AMTC");
                    break;
            case 12: InfoTitle.setText("ABOUT ALIAS");
                AboutPara.setText(R.string.AliasMainAbout);
                break;

            case 21: InfoTitle.setText("ABOUT CEZZANE");
                AboutPara.setText("The official dance club of Aset");
                break;

            case 31: InfoTitle.setText("ABOUT DASP");
                     AboutPara.setText(R.string.aboutDasppage);
                    break;
            case 41:InfoTitle.setText("ABOUT STROKES");
                    AboutPara.setText(R.string.strokesaboutpara);
                    break;
            case 51:InfoTitle.setText("ABOUT AZMIE");
                AboutPara.setText("The official dramatics club of Aset");
                break;
            case 61:   InfoTitle.setText("ABOUT ALFAAZ");
                AboutPara.setText(R.string.aboutAlfaaz);
                break;
            default: InfoTitle.setText("About The Club");
                     AboutPara.setText("Sorry! No Information Available");
        }


    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class amtc_UpEvents extends AppCompatActivity {

    RecyclerView recyclerView;
    private  TextView EventHeading, NoEventStatus;

    private Boolean data_available=true;

    String s1[],s2[];
    int Value;
    int image[]={};
    int amtcEventImage[]={
            R.drawable.event1, R.drawable.event2, R.drawable.event3,R.drawable.event4,
            R.drawable.event5, R.drawable.event6, R.drawable.event7, R.drawable.event8,
            R.drawable.event9,R.drawable.event10
    };
    int daspEventImage[]={
            R.drawable.deimg1
    };
    int alfaazPastEvent[]={
           R.drawable.alfz_eimg1, R.drawable.alfz_eimg2, R.drawable.azmie_img3
    };
    int strokesEventImage[]={
         R.drawable.sevimg1,R.drawable.sevimg2,R.drawable.sevimg3,R.drawable.sevimg4,R.drawable.sevimg5,
            R.drawable.sevimg6,R.drawable.sevimg7,R.drawable.sevimg8,R.drawable.sevimg9,R.drawable.sevimg10
    };
  int DummyEventImage[]={
    R.drawable.work,   R.drawable.seminar,R.drawable.compete
  };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amtc__up_events);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }

        recyclerView=findViewById(R.id.recycler1View);
        EventHeading=findViewById(R.id.evt_head);
        NoEventStatus=findViewById(R.id.novettxt);
        NoEventStatus.setVisibility(View.GONE);

        int Second=Value%10;
        if(Second==1)   EventHeading.setText("UpComing Events");
        else if (Second==2) EventHeading.setText("Current Events");
        else if (Second==3) EventHeading.setText("Past Events");

        switch(Value)
        {
            case 1121: s1=getResources().getStringArray(R.array.Amtc_EVENT_TITLE);
                     s2=getResources().getStringArray(R.array.AMTC_Event_Day);
                     image=amtcEventImage;
                    break;
            case 3123:
                    s1=getResources().getStringArray(R.array.DASP_EVENT_TITLE);
                     s2=getResources().getStringArray(R.array.DASP_Event_Day);
                      image=daspEventImage;
                   break;
            case 4121: s1=getResources().getStringArray(R.array.SROKES_EVENT_TITLE);
                     s2=getResources().getStringArray(R.array.AMTC_Event_Day);
                      image=strokesEventImage;
                    break;
            case 6123:  s1=getResources().getStringArray(R.array.Alfaaz_EVENT_TITLE);
                s2=getResources().getStringArray(R.array.Alfaaz_Event_Day);
                image=alfaazPastEvent;
                break;
            case 1221:
                s1=getResources().getStringArray(R.array.DummyEventName);
                s2=getResources().getStringArray(R.array.DummyEventFutureDay);
                image=DummyEventImage;
                break;
            case 1223:
            case 2123:
            case 5123:  s1=getResources().getStringArray(R.array.DummyEventName);
                s2=getResources().getStringArray(R.array.DummyEventPastDay);
                image=DummyEventImage;
                break;

            default:   NoEventStatus.setVisibility(View.VISIBLE);
                       recyclerView.setVisibility(View.GONE);
                         data_available=false;

        }

       // s1=getResources().getStringArray(R.array.Amtc_EVENT_TITLE);
        //s2=getResources().getStringArray(R.array.AMTC_Event_Day);
         if(data_available==true) {
             Amtc_UpEvent_Adapter UpEventAdapter = new Amtc_UpEvent_Adapter(this, s1, s2, image, Value);
             recyclerView.setAdapter(UpEventAdapter);
             recyclerView.setLayoutManager(new LinearLayoutManager(this));
         }
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends AppCompatActivity {


    ImageView mainImageView;
    TextView title, description;

    String data1, data2;
    int myImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mainImageView=findViewById(R.id.mainImageView);
        title=findViewById(R.id.profile_title);
        description=findViewById(R.id.profile_desc);

        getData();
        setData();
    }

    private void getData()
    {

        if(getIntent().hasExtra("myImage")&&getIntent().hasExtra("data1")&&getIntent().hasExtra("data2"))
        {
            data1=getIntent().getStringExtra("data1");
            data2=getIntent().getStringExtra("data2");
            myImage=getIntent().getIntExtra("myImage",1);
        }
        else
        {
            Toast.makeText(this, "No data",Toast.LENGTH_SHORT).show();
        }
    }

    private void setData()
    {
        title.setText(data1);
        description.setText(data2);
        mainImageView.setImageResource(myImage);
    }
}


package com.example.layoutpractise3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class society extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.societyview, container, false);
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SocialHandle extends AppCompatActivity {

    int Value;
    private Button instagram;
    private Button facebook;
    private Button twitter;
    private Button website;
    String insta="https://instagram.com/cybercup19?igshid=1ummkbwogiz0o",
            fb="https://www.facebook.com/amtcnoida",
            twit="https://twitter.com/amtcnoida",
            web="http://www.isajaljain.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_handle);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }


        instagram=(Button)findViewById(R.id.instagram);
        facebook=(Button)findViewById(R.id.facebook);
        twitter=(Button)findViewById(R.id.twitter);
        website=(Button)findViewById(R.id.web);

        switch(Value)
        {
            case 11:   //amtc
                        insta="https://instagram.com/cybercup19?igshid=1ummkbwogiz0o";
                        fb="https://www.facebook.com/amtcnoida";
                        twit="https://twitter.com/amtcnoida";
                        web="http://www.isajaljain.com/";
                break;
            case 12:  //alias
                insta="https://instagram.com/asetalias?igshid=153l3skdgw123";
                fb="https://www.facebook.com/asetalias/";
                twit="https://twitter.com/asetalias";
                web="https://asetalias.in/index.html#events";
                break;
            case 21: //cezanne
                insta="https://www.instagram.com/cezanne_dance_crew/?hl=en";
                fb="https://www.facebook.com/cezannedancecrew/?eid=ARDO4HIqbhoB8he-CI2YbYg_osADlm7kfyaXjUkho8BPCTtydXHgLZd9Lt4SgH1xDCd29izIkXYy07rG";
                twit="";
                web="https://www.youtube.com/channel/UC8DGJ0j67L5KJhYZdI6ffaw";//YOUTUBE CHANNEl
                twitter.setVisibility(View.GONE);
                break;
            case 31: //dasp
                insta="https://instagram.com/daspmusicsociety?igshid=bcjlos8lmmhn";
                fb="https://www.facebook.com/DASPAMITY/?ref=page_internal";
                twit="https://twitter.com/search?q=dasp%20music%20society%20aset&src=typed_query";
                web="";
                website.setVisibility(View.GONE);
                break;
            case 41: //strokes
                insta="https://instagram.com/strokes_aset?igshid=ifnchz25ufbr";
                fb="https://www.facebook.com/STROKES.ASET/";
                twit="";
                web="";
                twitter.setVisibility(View.GONE);
                website.setVisibility(View.GONE);
                break;
            case 51: //azmie
                insta="https://instagram.com/azmiekinautanki?igshid=1mmp309804tne";
                fb="https://www.facebook.com/amtcnoida";
                twit="";
                twitter.setVisibility(View.GONE);
                web="https://www.youtube.com/channel/UC04hSHKPuQ5ZnwyuBCog9sQ";//YOU TUBE CHANNEL
                break;
            case 61: //alfaaz
                insta="https://instagram.com/alfaaz.aset?igshid=qyqz7ue4ukmh";
                fb="https://www.facebook.com/AlfaazASET/";
                twit="";
                twitter.setVisibility(View.GONE);
                web="https://alfaazlitsocblog.wordpress.com/";//wordpress
                break;
        }

        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(insta));
                startActivity(intent);
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(fb));
                startActivity(intent);
            }
        });

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(web));
                startActivity(intent);
            }
        });

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(twit));
                startActivity(intent);
            }
        });

    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AMTC extends AppCompatActivity {

    private TextView ClubTitle;
    private TextView ClubSubTitle;
    private TextView Clubmaininfo;
    private TextView NoofMembers;
    private Button details;
    private Button join;
    private ImageView ClubBanner, ClubProfile;
    int Value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_m_t_c);

        ClubBanner=findViewById(R.id.img_header);
        ClubProfile=findViewById(R.id.img_profile);
        ClubTitle=findViewById(R.id.title_amtc);
        ClubSubTitle=findViewById(R.id.subtitle_amtc);
        Clubmaininfo=findViewById(R.id.para);
        NoofMembers=findViewById(R.id.noofmember);
        details=(Button)findViewById(R.id.details_bttn);
        join=(Button)findViewById(R.id.join_bttn);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }

        switch(Value)
        {
            case 11: ClubTitle.setText("AMTC");
                     ClubSubTitle.setText("AMITY MICROSOFT TECHNICAL CLUB");
                     ClubProfile.setImageResource(R.drawable.amtc_logo);
                     ClubBanner.setImageResource(R.drawable.amtc_banner);
                     break;
            case 12:   ClubTitle.setText("ALIAS");
                     Clubmaininfo.setText(R.string.AliasAbout);
                     ClubSubTitle.setText("Amity Linux Assistance Sapience");
                     ClubProfile.setImageResource(R.drawable.alias_logo);
                     ClubBanner.setImageResource(R.drawable.alias_banner);
                      break;
            case 21:   ClubTitle.setText("CEZZANE");
                      Clubmaininfo.setText(R.string.CezzaneAbout);
                       ClubSubTitle.setText("The Dance Society of Aset");
                      NoofMembers.setText("(27 members)");
                      ClubProfile.setImageResource(R.drawable.cezzane_logo);
                      ClubBanner.setImageResource(R.drawable.cezzane_banner);
                      break;
            case 31: ClubTitle.setText("DASP");
                    ClubSubTitle.setText("Music Society Of Aset");
                    Clubmaininfo.setText(R.string.AboutDASP);
                     ClubProfile.setImageResource(R.drawable.dasp_logo);
                     ClubBanner.setImageResource(R.drawable.dimg5);
                    NoofMembers.setText("(51 members)");
                    break;

            case 41: ClubTitle.setText("STROKES");
                     ClubSubTitle.setText("Let your creativity flow....");
                     Clubmaininfo.setText(R.string.stokesmaininfo);
                     ClubBanner.setImageResource(R.drawable.simg2);
                     ClubProfile.setImageResource(R.drawable.strokes);
                     NoofMembers.setText("(20 members)");
                      break;
            case 51:   ClubTitle.setText("AZMIE");
                ClubSubTitle.setText("The Drama Society of Aset");
                NoofMembers.setText("(30 members)");
                Clubmaininfo.setText(R.string.AzmieAbout);
                ClubProfile.setImageResource(R.drawable.azmie_logo);
                ClubBanner.setImageResource(R.drawable.azmie_banner);
                break;
            case 61:   ClubTitle.setText("ALFAAZ");
                ClubSubTitle.setText("The Literary Society of Aset");
                NoofMembers.setText("(32 members)");
                Clubmaininfo.setText(R.string.aboutAlfaaz);
                ClubBanner.setImageResource(R.drawable.alfz_eimg3);
                ClubProfile.setImageResource(R.drawable.alfz_logo);

                break;
        }



        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it=new Intent(AMTC.this, Amtc_details.class);
                it.putExtra("value",Value);
                startActivity(it);
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinintent=new Intent(AMTC.this, FormAmtc.class);
                joinintent.putExtra("value",Value);
                startActivity(joinintent);
            }
        });

    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class AmtcPhoto extends AppCompatActivity {

    GridView gridView;
    int photovalue;
    static int m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amtc_photo);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            photovalue = bundle.getInt("value");
        }

        getPhotoValue(photovalue);

        gridView=findViewById(R.id.grid_view);

        gridView.setAdapter(new ImageAdapter(this));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent=new Intent(getApplicationContext(), Picview.class);
                intent.putExtra("id", position);
                intent.putExtra("value",photovalue);
                startActivity(intent);

            }
        });
    }

    static void getPhotoValue(int x)
    {
        m=x;
    }
    public  static int ReturnPhotoValue()
    {
        return m;
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FormAmtc extends AppCompatActivity {

    int Value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_amtc);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }

        final EditText edit1=findViewById(R.id.edit1);
        final EditText edit2=findViewById(R.id.edit2);
        final EditText edit3=findViewById(R.id.edit3);
        final EditText edit4=findViewById(R.id.edit4);
        final EditText edit5=findViewById(R.id.edit5);
        final EditText edit6=findViewById(R.id.edit6);
        final EditText edit7=findViewById(R.id.edit7);
        final TextView title=findViewById(R.id.heading);
        Button btn=findViewById(R.id.button);


        switch(Value)
        {
            case 11:  title.setText("AMTC MEMBERSHIP FORM");
                    break;
            case 12: title.setText("ALIAS MEMBERSHIP FORM");
                    break;
            case 21:  title.setText("CEZZANE MEMBERSHIP FORM");
                edit6.setHint("Classical, Western, Free-Style Dance, etc");
                break;
            case 31: title.setText("DASP REGISTRATION FORM");
                edit6.setHint("Vocal,Guitar,Tabla, etc");
                   break;
            case 41:  title.setText("STROKES MEMBERSHIP FORM");
                       edit6.setHint("Doodle, portrait, watercolor, etc");
                    break;
            case 51:  title.setText("AZMIE MEMBERSHIP FORM");
                edit6.setHint("plays, etc");
                break;
            case 61:  title.setText("ALFAAZ MEMBERSHIP FORM");
                edit6.setHint("Writing, Debate, etc");
                break;
        }

        //  final String To= "swatitripathi2000@gmail.com";
        btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                Intent i= new Intent(Intent.ACTION_SENDTO);
                i.setType("message/html");

                i.setData(Uri.parse("mailto:"));
                i.putExtra(Intent.EXTRA_EMAIL, new String[] { "swatitripathi2000@gmail.com" });
                //i.putExtra(Intent.EXTRA_EMAIL, To);
                i.putExtra(Intent.EXTRA_SUBJECT, "FORM SUBMISSION TO JOIN CLUB");
                i.putExtra(Intent.EXTRA_TEXT, "FORM TO JOIN AMTC CLUB:\n\n"+"\nFullName: "+ edit1.getText()+
                        "\n Class & Section: "+edit2.getText()+
                        "\nRollNo: "+edit3.getText()+
                        "\nCourse: "+edit4.getText()+
                        "\nBatch: "+edit5.getText()+
                        "\nMy Skills: "+edit6.getText()+
                        "\nReason To Join:  "+edit7.getText());
                try{
                    startActivity(Intent.createChooser(i,"Please Select Email"));
                }
                catch (android.content.ActivityNotFoundException ex)
                {
                    Toast.makeText(FormAmtc.this, "There is no Email clients", Toast.LENGTH_SHORT).show();
                }
                finish();

            }
        });

    }
}


package com.example.layoutpractise3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Button Tech;
    private Button Dance;
    private Button Drama;
    private Button Art;
    private Button Literary;
    private Button Music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout=findViewById(R.id.drawer);
        mToggle= new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout,R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView=findViewById(R.id.navigview);
       navigationView.setNavigationItemSelectedListener(this);

        Tech=findViewById(R.id.tech);
        Dance=findViewById(R.id.dance);
        Drama=findViewById(R.id.drama);
        Art=findViewById(R.id.art);
        Literary=findViewById(R.id.literary);
        Music=findViewById(R.id.music);

        Tech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii= new Intent(MainActivity.this, tech.class);
                //NOTE just below line
                ii.putExtra("value",1);
                startActivity(ii);
            }
        });

        //NOTE BELOW FUNCTION
        Art.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ia=new Intent(MainActivity.this, tech.class);
                ia.putExtra("value",4);
                startActivity(ia);

            }
        });

        Music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ia=new Intent(MainActivity.this, tech.class);
                ia.putExtra("value",3);
                startActivity(ia);
            }
        });

        Dance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ia=new Intent(MainActivity.this, tech.class);
                ia.putExtra("value",2);
                startActivity(ia);
            }
        });
        Drama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ia=new Intent(MainActivity.this, tech.class);
                ia.putExtra("value",5);
                startActivity(ia);
            }
        });

        Literary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ia=new Intent(MainActivity.this, tech.class);
                ia.putExtra("value",6);
                startActivity(ia);
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(mToggle.onOptionsItemSelected(item))
        {
            return  true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        switch (id)
        {
            case R.id.home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new society()).commit();

                Toast.makeText(this, "Welcome To Society Page", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:  Toast.makeText(this, "Events Page", Toast.LENGTH_SHORT).show();
                   Intent i =new Intent(MainActivity.this, Events.class);
                   startActivity(i);
               // getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new events()).commit();
                break;

            case  R.id.logout: Toast.makeText(this, "About Us!!", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new aboutus()).commit();
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}


package com.example.layoutpractise3;

        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.TextView;

public class Amtc_details extends AppCompatActivity {

    int Value;

    private TextView DetailsTitle;
    private Button socialhandle;
    private Button About;
    private Button members;
    private Button photobutton;
    private Button events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amtc_details);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }

        DetailsTitle=findViewById(R.id.details_title);
        socialhandle=(Button)findViewById(R.id.social_handle_button);
        About=(Button)findViewById(R.id.aboutusamtc);
        members=(Button)findViewById(R.id.members);
        events=(Button)findViewById(R.id.events);
        photobutton=findViewById(R.id.photobutton);

        switch(Value)
        {
            case 11: DetailsTitle.setText("AMTC: DETAILS");
                break;
            case 12: DetailsTitle.setText("ALIAS: DETAILS");
                break;
            case 21: DetailsTitle.setText("CEZZANE: DETAILS");
                break;
            case 31: DetailsTitle.setText("DASP: DETAILS");
                break;
            case 41: DetailsTitle.setText("STROKES: DETAILS");
                 break;
            case 51: DetailsTitle.setText("AZMIE: DETAILS");
                break;
            case 61: DetailsTitle.setText("ALFAAZ: DETAILS");
                break;
        }

        About.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ia=new Intent(Amtc_details.this, Aboutamtc.class);
                ia.putExtra("value",Value);
                startActivity(ia);
            }
        });
        socialhandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ish =new Intent(Amtc_details.this, SocialHandle.class);
                ish.putExtra("value",Value);
                startActivity(ish);
            }
        });

        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ie=new Intent(Amtc_details.this, AmtcEvents.class);
                ie.putExtra("value",Value);
                startActivity(ie);
            }
        });

        members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent im= new Intent(Amtc_details.this, Amtc_members.class);
                im.putExtra("value",Value);
                startActivity(im);
            }
        });

        photobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ip= new Intent(Amtc_details.this, AmtcPhoto.class);
                ip.putExtra("value",Value);
                startActivity(ip);
            }
        });


    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Amtc_members extends AppCompatActivity {

    private Button corememberbutton, generalmemberbtn, facultybtn;
    int Value,nvalue=0;
    private TextView MemTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amtc_members);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }

        MemTitle=findViewById(R.id.Membertitle);
        corememberbutton=findViewById(R.id.btn_main_member);
        generalmemberbtn=findViewById(R.id.btn_gen_members);
        facultybtn=findViewById(R.id.btn_teacher_member);

        switch(Value)
        {
            case 11: MemTitle.setText("AMTC: MEMBERS");
                break;
            case 12: MemTitle.setText("ALIAS: MEMBERS");
                break;
            case 21: MemTitle.setText("CEZZANE: MEMBERS");
                break;
            case 31: MemTitle.setText("DASP: MEMBERS");
                break;
            case 41: MemTitle.setText("STROKES: MEMBERS");
                break;
            case 51: MemTitle.setText("AZMIE: MEMBERS");
                break;
            case 61: MemTitle.setText("ALFAAZ: MEMBERS");
                break;
        }

        corememberbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cmbutton= new Intent(Amtc_members.this, CoreTeamMembers.class);
                nvalue=100*Value+31;
                cmbutton.putExtra("value",nvalue);
                startActivity(cmbutton);
            }
        });

        generalmemberbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cmbutton= new Intent(Amtc_members.this, CoreTeamMembers.class);
                nvalue=100*Value+32;
                cmbutton.putExtra("value",nvalue);
                startActivity(cmbutton);
            }
        });

        facultybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cmbutton= new Intent(Amtc_members.this, CoreTeamMembers.class);
                nvalue=100*Value+33;
                cmbutton.putExtra("value",nvalue);
                startActivity(cmbutton);
            }
        });
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class tech extends AppCompatActivity {

    private TextView GenClubHeading;
  private Button amtc, alias;
  int value, nvalue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech);

        GenClubHeading=findViewById(R.id.genclubhead);
        amtc=findViewById(R.id.amtc_bttn);
        alias=findViewById(R.id.alias_btn);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            value = bundle.getInt("value");
        }

        DISPLAY();

        amtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(tech.this, AMTC.class);
                //int no=1;
               // int x=10*value+number;
                //value=AMTC(no);
                nvalue=10*value+1;
                intent.putExtra("value",nvalue);
                startActivity(intent);
            }
        });

        alias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nvalue=10*value+2;
                Intent intent= new Intent(tech.this, AMTC.class);
                intent.putExtra("value",nvalue);
                startActivity(intent);
            }
        });


    }

    void DISPLAY()
    {
        alias.setVisibility(View.GONE);
        switch(value)
        {
            case 1:  GenClubHeading.setText("TECHNICAL SOCIETIES");
                amtc.setText("AMTC");
                alias.setVisibility(View.VISIBLE);
                alias.setText("ALIAS");
                break;

            case 2:  GenClubHeading.setText("DANCE SOCIETIES");
                amtc.setText("CEZZANE");
                break;
            case 3: GenClubHeading.setText("MUSIC SOCIETIES");
                amtc.setText("DASP");
                break;

            case 4:  GenClubHeading.setText("ART SOCIETIES");
                amtc.setText("STROKES");
                 break;

            case 5: GenClubHeading.setText("DRAMA SOCIETIES");
                amtc.setText("AZMIE");
                break;
            case 6:GenClubHeading.setText("LITERARY SOCIETIES");
                amtc.setText("ALFAAZ");
                break;
        }

    }
}


//if(value==1)  GenClubHeading.setText("TECHNICAL SOCITIES");
// if(value==4) GenClubHeading.setText("ART SOCITIES");
         /*
        Intent i=getIntent();
        Bundle b=i.getExtras();
        if(b!=null)
        {
            value=(int) b.get("value");
        }
        if(value==1) GenClubHeading.setText("TECH SOCITIES");
        else if (value==4) GenClubHeading.setText("ART SOCITIES");*/

package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class Picview extends AppCompatActivity {


    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picview);

        imageView=findViewById(R.id.img_view);

        getSupportActionBar().hide();
        getSupportActionBar().setTitle("Full Screen Image");

        Intent i=getIntent();

        int position =i.getExtras().getInt("id");

        ImageAdapter imageAdapter= new ImageAdapter(this);

        imageView.setImageResource(imageAdapter.imageArray[position]);
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AmtcUpEventsDisp extends AppCompatActivity {

    ImageView mainImageView;
    TextView title, description,timing,venue,aboutevent;
    String et_time[], et_venue[], et_abt_event[];


    String data1, data2;
    int myImage;
    int pos,Value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amtc_up_events_disp);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }

        et_time=getResources().getStringArray(R.array.EventTime);
        et_venue=getResources().getStringArray(R.array.EventVenue);
        et_abt_event=getResources().getStringArray(R.array.EventPurpose);
        switch(Value)
        {
            case 1121: et_time=getResources().getStringArray(R.array.EventTime);
                et_venue=getResources().getStringArray(R.array.EventVenue);
                et_abt_event=getResources().getStringArray(R.array.EventPurpose);
                break;
            case 3121:
                et_time=getResources().getStringArray(R.array.Dasp_EventTime);
                et_venue=getResources().getStringArray(R.array.DaspEventVenue);
                et_abt_event=getResources().getStringArray(R.array.DaspEventPurpose);
                break;
            case 4121:
                et_time=getResources().getStringArray(R.array.StrokesEventTime);
                et_venue=getResources().getStringArray(R.array.StrokesEventVenue);
                et_abt_event=getResources().getStringArray(R.array.StrokesEventPurpose);
                break;
            case 6123:
                et_time=getResources().getStringArray(R.array.AlfaazEventTime);
                et_venue=getResources().getStringArray(R.array.AlfaazEventVenue);
                et_abt_event=getResources().getStringArray(R.array.AlfaazEventPurpose);
                break;
            case 1221:
            case 1223:
            case 2123:
            case 5123:
                et_time=getResources().getStringArray(R.array.DummyEventTime);
                et_venue=getResources().getStringArray(R.array.DummyEventVenue);
                et_abt_event=getResources().getStringArray(R.array.DummyEventPurpose);
                break;

        }


        mainImageView=findViewById(R.id.maineventImageView);
        title=findViewById(R.id.event_heading);
        description=findViewById(R.id.event_daydate);
        timing=findViewById(R.id.tvevent_time);
        venue=findViewById(R.id.tvevent_venue);
        aboutevent=findViewById(R.id.tvevent_purpose);

        getData();
        setData();
    }

    private void getData()
    {

        if(getIntent().hasExtra("myImage")&&getIntent().hasExtra("data1")&&getIntent().hasExtra("data2")&&getIntent().hasExtra("position"))
        {
            data1=getIntent().getStringExtra("data1");
            data2=getIntent().getStringExtra("data2");
            myImage=getIntent().getIntExtra("myImage",1);
            pos=getIntent().getIntExtra("position",1);

        }
        else
        {
            Toast.makeText(this, "No data",Toast.LENGTH_SHORT).show();
        }
    }

    private void setData()
    {
        title.setText(data1);
        description.setText(data2);
        mainImageView.setImageResource(myImage);
        timing.setText(et_time[pos]);
        venue.setText(et_venue[pos]);
        aboutevent.setText(et_abt_event[pos]);
    }
}


package com.example.layoutpractise3;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    String data1[], data2[];
    int images[];
    Context context;

    public MyAdapter(Context ct, String s1[], String s2[], int img[] ) {

        context = ct;
        data1= s1;
        data2=s2;
        images=img;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater= LayoutInflater.from(context);
        View view= inflater.inflate(R.layout.my_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.myText1.setText(data1[position]);
        holder.myText2.setText(data2[position]);
        holder.myImage.setImageResource(images[position]);

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it= new Intent(context, Profile.class);
                it.putExtra("data1",data1[position]);
                it.putExtra("data2",data2[position]);
                it.putExtra("myImage",images[position]);
                context.startActivity(it);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data1.length;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView myText1, myText2;
        ImageView myImage;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            myText1=itemView.findViewById(R.id.title);
            myText2=itemView.findViewById(R.id.description);
            myImage=itemView.findViewById(R.id.profileimg);
            mainLayout=itemView.findViewById(R.id.parent_layout);
        }
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class Events extends AppCompatActivity {

    RecyclerView recyclerView;
    String s1[],s2[];
    int Value=1121;
    int image[]={};
    int amtcEventImage[]={
            R.drawable.event1, R.drawable.event2, R.drawable.event3,R.drawable.event4,
            R.drawable.event5, R.drawable.event6, R.drawable.event7, R.drawable.event8,
            R.drawable.event9,R.drawable.event10
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        s1=getResources().getStringArray(R.array.Amtc_EVENT_TITLE);
        s2=getResources().getStringArray(R.array.AMTC_Event_Day);
        recyclerView=findViewById(R.id.evt_recycleview);
       image=amtcEventImage;
        Amtc_UpEvent_Adapter UpEventAdapter = new Amtc_UpEvent_Adapter(this, s1, s2, image, Value);
        recyclerView.setAdapter(UpEventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}


package com.example.layoutpractise3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;

    int PhotoCode= AmtcPhoto.ReturnPhotoValue();

    //ALLOCATING PHOTOS TO RESPECTIVE CLUBS

    int[] amtcimage= {
            R.drawable.aimg1,R.drawable.aimg2,R.drawable.aimg3,R.drawable.aimg4,R.drawable.aimg5,
            R.drawable.aimg6,R.drawable.aimg7,R.drawable.aimg8, R.drawable.aimg9,R.drawable.aimg10
    };

    int [] aliasimage={
          R.drawable.alias_img1, R.drawable.alias_logo
    };

    int[] cezzaneimage={
            R.drawable.cimg1,R.drawable.cimg2,R.drawable.cimg3,R.drawable.cimg4,R.drawable.cimg5,
            R.drawable.cimg6,R.drawable.cimg7,R.drawable.cimg8,R.drawable.cimg9,R.drawable.cimg10,
            R.drawable.cimg11
    };

    int[] daspimage={
            R.drawable.dimg1,R.drawable.dimg2,R.drawable.dimg3,R.drawable.dimg4,
            R.drawable.deimg1
    };

    int[] stokesimage={
            R.drawable.simg1, R.drawable.simg2
    };

    int [] alfaazimage={
         R.drawable.alfz_img1,R.drawable.alfz_img2,R.drawable.alfz_img3,R.drawable.alfz_img4,
            R.drawable.alfz_img5,R.drawable.alfz_logo
    };

    int[] azmieimage={
              R.drawable.azmie_img1, R.drawable.azmie_img2, R.drawable.azmie_img3
    };
    //SELECTING APT PHOTO ARRAY FOR RESPECTIVE CLUB
    public int[] Choose()
    {
        int[] iA={ };
        switch(PhotoCode){
            case 11: iA= amtcimage; break;
            case 12: iA= aliasimage; break;
            case 21: iA=cezzaneimage; break;
            case 31: iA= daspimage; break;
            case 41: iA= stokesimage;break;
            case 51: iA=azmieimage; break;
            case 61: iA=alfaazimage; break;

        }
        return  iA;
    }

    public  int[] imageArray= Choose();

    public ImageAdapter(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return imageArray.length;
    }

    @Override
    public Object getItem(int position) {
        return imageArray[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView= new ImageView(mContext);
        imageView.setImageResource(imageArray[position]);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(340, 340));
        return imageView;
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CoreTeamMembers extends AppCompatActivity {

    RecyclerView recyclerView;
    private TextView MemberHeading, NoMemberStatus;
    private Boolean data_available=true;
    int Value;


    String s1[],s2[];
    int image[]={};
    int amtc_memimg[]={
            R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon,
            R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon,
            R.drawable.profileicon, R.drawable.profileicon
    };
    int alfaazcoremem[]={
            R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon,
            R.drawable.profileicon
    };
    int dummyimg[]={
            R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon,
            R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon, R.drawable.profileicon,
            R.drawable.profileicon, R.drawable.profileicon
    };
    int teacherdummyimg[]={R.drawable.profileicon, R.drawable.profileicon,R.drawable.profileicon};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core_team_members);

        recyclerView=findViewById(R.id.recyclerView);
        MemberHeading=findViewById(R.id.mem_head);
        NoMemberStatus=findViewById(R.id.nomemtxt);
        NoMemberStatus.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Value = bundle.getInt("value");
        }
        int second=Value%10;
        if(second==1)  MemberHeading.setText("Core Team Members");
        else if (second==2) MemberHeading.setText("General Members");
        else if (second==3) MemberHeading.setText("Faculy Coordinators");

        switch (Value)
        {
            case 1131:s1=getResources().getStringArray(R.array.programming_languages);
                        s2=getResources().getStringArray(R.array.description);
                        image=amtc_memimg;
                       break;
            case 1231:
            case 2131:
            case 3131:
            case 4131:
            case 5131:s1=getResources().getStringArray(R.array.DummyNames);
                      s2=getResources().getStringArray(R.array.DummySeniorDesignation);
                      image=dummyimg;
                       break;
            case 6131:s1=getResources().getStringArray(R.array.Alfaaz_Core_Members);
                s2=getResources().getStringArray(R.array.Alfaaz_CoreMem_Desg);
                image=alfaazcoremem;
                break;
                //Below is for gen members button
            case 1132:
            case  1232:
            case 2132:
            case 3132:
            case 4132:
            case 5132:
            case 6132: s1=getResources().getStringArray(R.array.DummyNames);
                        s2=getResources().getStringArray(R.array.DummyJuniorDesignation);
                        image=dummyimg;
                         break;
            case 1133:
            case 1233:
            case 2133:
            case 3133:
            case 4133:
            case 5133:
            case 6133:  s1=getResources().getStringArray(R.array.TeacherDummyNames);
                        s2=getResources().getStringArray(R.array.TeacherDummyDesig);
                       image=teacherdummyimg;
                       break;
            default:
                    NoMemberStatus.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    data_available=false;
        }



        if(data_available==true) {
            MyAdapter myAdapter = new MyAdapter(this, s1, s2, image);
            recyclerView.setAdapter(myAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }
}


package com.example.layoutpractise3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class splashscreen extends AppCompatActivity {

    private int SLEEP_TIMER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splashscreen);
        getSupportActionBar().hide();

        LogoLauncher logolauncher= new LogoLauncher();
        logolauncher.start();
    }


    private class LogoLauncher extends Thread{
        public void run(){
            try{
                sleep(1000*SLEEP_TIMER);

            }
            catch(InterruptedException e){
                e.printStackTrace();
            }

            Intent intent = new Intent( splashscreen.this, MainActivity.class);
            startActivity(intent);
            splashscreen.this.finish();


        }
    }
}


