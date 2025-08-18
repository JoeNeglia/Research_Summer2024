package com.farizma.truthdare;

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

        assertEquals("com.farizma.truthdare", appContext.getPackageName());
    }
}


package com.farizma.truthdare;

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

package com.farizma.truthdare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

import java.util.ArrayList;

public class TruthActivity extends AppCompatActivity {

    private ArrayList<TruthItem> truthList;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_values);

        sharedPreferences = getSharedPreferences("mySharedPreference", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        truthList = new ArrayList<>();

        recyclerViewConfig();
        populateDefaultData();
        if(sharedPreferences.contains("UserTruths"))
            populateUserData(sharedPreferences.getString("UserTruths", null));
    }

    public void populateDefaultData() {
        Values values = new Values();
        for(int i=0; i<values.truths.length; i++)
            truthList.add(new TruthItem(values.truths[i]));
    }

    public void populateUserData(String jsonTruths) {
        String[] truths = gson.fromJson(jsonTruths, String[].class);
        for(int i=0; i<truths.length; i++)
                truthList.add(new TruthItem(truths[i]));
    }

    public void recyclerViewConfig() {
        // config for RV
        recyclerView = findViewById(R.id.recyclerView);

        //performance
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        adapter = new TruthAdapter(truthList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // setup buttons
        final EditText input = dialog.findViewById(R.id.editText);
        Button dismiss = dialog.findViewById(R.id.dismiss);
        Button add = dialog.findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mText = input.getText().toString();
                if(mText.isEmpty())
                    Toast.makeText(getApplicationContext(), "Empty Text", Toast.LENGTH_LONG).show();
                else{
                    updateUserData(mText);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public void updateUserData(String string) {
        ArrayList<String> textList = new ArrayList<>();

        if(sharedPreferences.contains("UserTruths")) {
            String jsonTruths = sharedPreferences.getString("UserTruths", null);
            String[] truths = gson.fromJson(jsonTruths, String[].class);
            for(int i=0; i<truths.length; i++)
                textList.add(truths[i]);
        }

        textList.add(string);
        editor.putString("UserTruths", gson.toJson(textList));
        editor.apply();
        truthList.add(new TruthItem(string));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {

            case R.id.action_add:
                showDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


package com.farizma.truthdare;

public class Values {

    String[] truths = new String[]{
            "What does your dream boy or girl look like?",
            "Would you rather live with no internet or no A/C or heating?",
            "If you could go back in time in erase one thing you said or did, what would it be?",
            "Have you ever waved at someone thinking they saw you when really they didn't? What did you do when you realized it?",
            "Describe the strangest dream you've ever had. Did you like it?",
            "The world ends next week, and you can do anything you want (even if it's illegal). What would you do?",
            "How far would you go to land the guy or girl of your dreams?",
            "What is the most childish thing that you still do?"
    };

    String[] dares = new String[]{
            "Rate everyone in the room from 1 to 10, with 10 being the best personality.",
            "Go next door with a measuring cup and ask for a cup of sugar.",
            "Open Facebook, go to the account of the first person you see, and like every post on their wall going back to a year.",
            "Call your crush.", "Get into a debate with a wall.", "Eat a spoonful of mustard.",
            "Write a letter to your doctor describing an embarrassing rash you have, and post it on Facebook.",
            "Let the group choose three random things from the refrigerator and mix them together. Then you have to eat it.",
            "Dig through the trash and name everything you find.",
            "Call a NY-style pizza place and ask them what the difference is between NY pizza and “real” pizza.",
            "Take a selfie with the toilet and post it online."
    };
}


package com.farizma.truthdare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

import java.util.ArrayList;

public class DareActivity extends AppCompatActivity {

    private ArrayList<TruthItem> truthList;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_values);

        sharedPreferences = getSharedPreferences("mySharedPreference", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        truthList = new ArrayList<>();

        recyclerViewConfig();
        populateDefaultData();
        if(sharedPreferences.contains("UserDares"))
            populateUserData(sharedPreferences.getString("UserDares", null));
    }

    public void populateDefaultData() {
        Values values = new Values();
        for(int i=0; i<values.dares.length; i++)
            truthList.add(new TruthItem(values.dares[i]));
    }

    public void populateUserData(String jsonDares) {
        String[] dares = gson.fromJson(jsonDares, String[].class);
        for(int i=0; i<dares.length; i++)
            truthList.add(new TruthItem(dares[i]));
    }

    public void recyclerViewConfig() {
        // config for RV
        recyclerView = findViewById(R.id.recyclerView);

        //performance
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        adapter = new TruthAdapter(truthList);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    public void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // setup buttons
        final EditText input = dialog.findViewById(R.id.editText);
        Button dismiss = dialog.findViewById(R.id.dismiss);
        Button add = dialog.findViewById(R.id.add);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mText = input.getText().toString();
                if(mText.isEmpty())
                    Toast.makeText(getApplicationContext(), "Empty Text", Toast.LENGTH_LONG).show();
                else{
                    updateUserData(mText);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    public void updateUserData(String string) {
        ArrayList<String> textList = new ArrayList<>();

        if(sharedPreferences.contains("UserDares")) {
            String jsonDares = sharedPreferences.getString("UserDares", null);
            String[] dares = gson.fromJson(jsonDares, String[].class);
            for(int i=0; i<dares.length; i++)
                textList.add(dares[i]);
        }

        textList.add(string);
        editor.putString("UserDares", gson.toJson(textList));
        editor.apply();
        truthList.add(new TruthItem(string));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()) {

            case R.id.action_add:
                //TODO: add
                showDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

package com.farizma.truthdare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button start, truth, dare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_start);

        start = findViewById(R.id.start);
        truth = findViewById(R.id.truth);
        dare = findViewById(R.id.dare);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        truth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), TruthActivity.class));
            }
        });

        dare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), DareActivity.class));
            }
        });

    }
}


package com.farizma.truthdare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Button btn, truthBtn, dareBtn;
    private ImageView imgView;
    private Random random = new Random();
    private int lastDirection;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.button);
        truthBtn = findViewById(R.id.btn1);
        dareBtn = findViewById(R.id.btn2);
        imgView = findViewById(R.id.imageView);

        truthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), TruthActivity.class));
            }
        });

        dareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), DareActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        truthBtn.setEnabled(false);
        dareBtn.setEnabled(false);
        btn.setEnabled(true);
    }

    public void spin(View view) {

        int newDirection = random.nextInt(5400);
        float pivotX = imgView.getWidth()/2;
        float pivotY = imgView.getHeight()/2;

        Animation rotate = new RotateAnimation(lastDirection, newDirection, pivotX, pivotY);
        rotate.setDuration(2000);
        rotate.setFillAfter(true);
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mp = MediaPlayer.create(MainActivity.this, R.raw.audio);
                mp.start();
                btn.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mp.stop();
                mp.release();
                mp = null;
                truthBtn.setEnabled(true);
                dareBtn.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        lastDirection = newDirection;
        imgView.startAnimation(rotate);
    }

}


package com.farizma.truthdare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TruthAdapter extends RecyclerView.Adapter<TruthAdapter.TruthViewHolder>  {

    private ArrayList<TruthItem> mTruthList;

    public static class TruthViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public TruthViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    public TruthAdapter(ArrayList<TruthItem> truthList) {
        mTruthList = truthList;
    }

    @NonNull
    @Override
    public TruthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.example_card, parent, false);
        TruthViewHolder truthViewHolder = new TruthViewHolder(view);
        return truthViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TruthViewHolder holder, int position) {
        TruthItem currentItem = mTruthList.get(position);
        holder.textView.setText(currentItem.getmText());
    }

    @Override
    public int getItemCount() {
        return mTruthList.size();
    }
}


package com.farizma.truthdare;

public class TruthItem{

    private String mText;

    public TruthItem(String text) {
        mText = text;
    }

    public String getmText() {
        return mText;
    }

}

