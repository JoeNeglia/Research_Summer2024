package com.scoreboard;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

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

        assertEquals("com.scoreboard", appContext.getPackageName());
    }
}


package com.scoreboard;

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

package com.scoreboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    boolean A=false,B=false;
    TextView tvScore;
    Button  butReset,butOneRunA,butFourRunA,butSixRunA,butOneRunB,butFourRunB,butSixRunB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        butReset = findViewById(R.id.butReset);
        butOneRunB = findViewById(R.id.butOneRunB);
        butFourRunB = findViewById(R.id.butFourRunB);
        butSixRunB = findViewById(R.id.butSixRunB);
        butOneRunA = findViewById(R.id.butOneRunA);
        butFourRunA = findViewById(R.id.butFourRunA);
        butSixRunA = findViewById(R.id.butSixRunA);

        butOneRunA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                A=true;
                onClickOneRun();
                A=false;
            }
        });

        butFourRunA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                A = true;
                onClickFourRun();
                A = false;
        }
        });

        butSixRunA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                A = true;
                onClickSixRun();
                A = false;
            }
        });
        butOneRunB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                B = true;
                onClickOneRun();
                B = false;
            }
        });

        butFourRunB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                B = true;
                onClickFourRun();
                B = false;
            }
        });

        butSixRunB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                B = true;
                onClickSixRun();
                B =false;
            }
        });

        butReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickReset();
            }
        });
    }
    private void onClickSixRun(){
        if(A){
            tvScore = findViewById(R.id.tvScoreA);
        }
        if(B){
            tvScore = findViewById(R.id.tvScoreB);
        }
        int prev_score = Integer.parseInt(tvScore.getText() + "");
        tvScore.setText(prev_score + 6 + "");
    }
    private void onClickFourRun(){
        if(A){
            tvScore = findViewById(R.id.tvScoreA);
        }
        if(B){
            tvScore = findViewById(R.id.tvScoreB);
        }
        int prev_score = Integer.parseInt(tvScore.getText() + "");
        tvScore.setText(prev_score + 4 + "");
    }
    private void onClickOneRun(){
        if(A){
            tvScore = findViewById(R.id.tvScoreA);
        }
        if(B){
            tvScore = findViewById(R.id.tvScoreB);
        }
        int prev_score = Integer.parseInt(tvScore.getText() + "");
        tvScore.setText(prev_score + 1 + "");
    }


    public void onClickReset() {
        tvScore = findViewById(R.id.tvScoreA);
        tvScore.setText("0");
        tvScore = findViewById(R.id.tvScoreB);
        tvScore.setText("0"); }
}


