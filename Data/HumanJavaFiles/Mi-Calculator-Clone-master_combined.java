package com.gyan.calculator;

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
        assertEquals("com.gyan.calculator", appContext.getPackageName());
    }
}

package com.gyan.calculator;

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

package com.gyan.calculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gyan.calculator.dotmenu.AboutActivity;
import com.gyan.calculator.dotmenu.HistoryActivity;
import com.gyan.calculator.dotmenu.PrivacyPolicyActivity;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;
    ViewPager viewPager;
    AppCompatButton btndotmenu,btnlessmore;
    PopupMenu dropDownMenu;
    Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //toolbar set
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // making notification bar transparent
        changeStatusBarColor();

        btnlessmore=findViewById(R.id.less_more);
        viewPager=findViewById(R.id.viewpager);
        bottomNavigationView=findViewById(R.id.top_nav);

        //BottomNavigationBar, I am arrange in Top in between menu & Less_more and there include three fragment
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.equalto:
                        viewPager.setCurrentItem(0);
                        break;
                    case R.id.more:
                        viewPager.setCurrentItem(1);
                        break;
                    case R.id.home:
                        viewPager.setCurrentItem(2);
                        break;
                }
                return true;
            }
        });
        setUpViewPager();

        btndotmenu=findViewById(R.id.dotMenu);
        //DropDownMenu, This menu I am set First Fragment
        dropDownMenu=new PopupMenu(getApplicationContext(),btndotmenu);
        menu=dropDownMenu.getMenu();
        dropDownMenu.getMenuInflater().inflate(R.menu.equalto_frag_menu,menu);
        dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.history:
                        startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                        Toast.makeText(MainActivity.this, "History Clicked", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.about:
                        startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                        Toast.makeText(MainActivity.this, "About Clicked", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        //Click menu
        btndotmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem()==0){
                    dropDownMenu.show();
                }else if (viewPager.getCurrentItem()==1){
                    startActivity(new Intent(getApplicationContext(), PrivacyPolicyActivity.class));
                }else if (viewPager.getCurrentItem()==2){
                    startActivity(new Intent(getApplicationContext(),PrivacyPolicyActivity.class));
                }
            }
        });

    }

    // Making notification bar transparent
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    //Scrolling View between Fragment, there are three fragment when include BottomNavigationBar
    private void setUpViewPager(){
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager(), FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.equalto).setChecked(true);
                        btnlessmore.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.more).setChecked(true);
                        btnlessmore.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.home).setChecked(true);
                        btnlessmore.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}

package com.gyan.calculator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.gyan.calculator.fragment.EqualtoFragment;
import com.gyan.calculator.fragment.HomeFragment;
import com.gyan.calculator.fragment.MoreFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new EqualtoFragment();
            case 1:
                return new MoreFragment();
            case 2:
                return new HomeFragment();
            default:
                return new EqualtoFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}


package com.gyan.calculator.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gyan.calculator.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);


        return view;
    }
}

package com.gyan.calculator.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gyan.calculator.R;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class EqualtoFragment extends Fragment {

    TextView workingsTV;
    TextView resultsTV;
    Button btnAc,btnPercent,btn7,btn8,btn9,btn4,btn5,btn6,btn1,btn2,btn3,btn0,btnDot;
    ImageButton btnBackspace,btnDivide,btnMultiply,btnMinus,btnPlus,btnMoreOpt,btnEqual;
    String workings = "";
    int lastindex;
    char last;

    public EqualtoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_equalto, container, false);

        workingsTV=view.findViewById(R.id.workingsTextView);
        resultsTV=view.findViewById(R.id.resultTextView);
        btnAc=view.findViewById(R.id.btnAc);
        btnPercent=view.findViewById(R.id.btnPercent);
        btn7=view.findViewById(R.id.btn7);
        btn8=view.findViewById(R.id.btn8);
        btn9=view.findViewById(R.id.btn9);
        btn4=view.findViewById(R.id.btn4);
        btn5=view.findViewById(R.id.btn5);
        btn6=view.findViewById(R.id.btn6);
        btn1=view.findViewById(R.id.btn1);
        btn2=view.findViewById(R.id.btn2);
        btn3=view.findViewById(R.id.btn3);
        btn0=view.findViewById(R.id.btn0);
        btnDot=view.findViewById(R.id.btnDot);
        btnBackspace=view.findViewById(R.id.btnBackspace);
        btnDivide=view.findViewById(R.id.btnDivide);
        btnMultiply=view.findViewById(R.id.btnMultiply);
        btnMinus=view.findViewById(R.id.btnMinus);
        btnPlus=view.findViewById(R.id.btnPlus);
        btnMoreOpt=view.findViewById(R.id.btnMoreOpt);
        btnEqual=view.findViewById(R.id.btnEqual);

        btnMoreOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //Clear Button
        btnAc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workingsTV.setText("0");
                workings="";
                resultsTV.setText("");
            }
        });

        //Back Button
        btnBackspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()>=1){
                    workings=workings.substring(0,workings.length()-1);
                    workingsTV.setText(workings);
                }
                if (workings.length()<1){
                    workingsTV.setText("0");
                    resultsTV.setText("");
                }
            }
        });

        //Equalto Button
        btnEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();

                //If last charecter is (+, -, * and /) the remove this charecter
                char lc=workings.charAt(workings.length()-1);
                if (lc=='+'||lc=='−'||lc=='×'||lc=='÷'){
                    workings = workings.replaceAll(".$", "");
                }

                workings=workings.replaceAll("−","-");
                workings=workings.replaceAll("×","*");
                workings=workings.replaceAll("÷","/");

                Context rhino=Context.enter();
                rhino.setOptimizationLevel(-1);

                String finalResult="";
                Scriptable scriptable=rhino.initStandardObjects();
                finalResult=rhino.evaluateString(scriptable,workings,"Javascript",1,null).toString();

                resultsTV.setText(finalResult);
                workingsTV.setText(finalResult);
            }
        });

        btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    char fc=workings.charAt(0);
                    if (fc=='1'||fc=='2'||fc=='3'||fc=='4'||fc=='5'||fc=='6'||fc=='7'||fc=='8'||fc=='9'){
                        setworking("0");
                    }
                }else {
                    setworking("0");
                }
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("1");
                    }else {
                        setworking("1");
                    }
                }else {
                    setworking("1");
                }
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("2");
                    }else {
                        setworking("2");
                    }
                }else {
                    setworking("2");
                }
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("3");
                    }else {
                        setworking("3");
                    }
                }else {
                    setworking("3");
                }
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("4");
                    }else {
                        setworking("4");
                    }
                }else {
                    setworking("4");
                }
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("5");
                    }else {
                        setworking("5");
                    }
                }else {
                    setworking("5");
                }
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("6");
                    }else {
                        setworking("6");
                    }
                }else {
                    setworking("6");
                }
            }
        });

        btn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("7");
                    }else {
                        setworking("7");
                    }
                }else {
                    setworking("7");
                }
            }
        });

        btn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("8");
                    }else {
                        setworking("8");
                    }
                }else {
                    setworking("8");
                }
            }
        });

        btn9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()<=1) {
                    if (workings.charAt(0)=='0'){
                        workingsTV.setText("9");
                    }else {
                        setworking("9");
                    }
                }else {
                    setworking("9");
                }
            }
        });

        btnDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                lastindex=workings.indexOf(last);
                int dotcompair = workings.indexOf('.',lastindex);
                if (dotcompair==-1){
                    setworking(".");
                }
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()!=0){
                    last = workings.charAt(workings.length() -1);
                    if(last=='+' || last=='−' || last=='×' || last=='÷'){
                        workings=workings.substring(0, workings.length()-1) + "+";
                        workingsTV.setText(workings);
                    }else {
                        setworking("+");
                    }
                }else {
                    setworking("+");
                }
                workings=workingsTV.getText().toString();
                last = workings.charAt(workings.length() -1);
            }
        });

        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()!=0){
                    last = workings.charAt(workings.length() -1);
                    if(last=='+' || last=='−' || last=='×' || last=='÷'){
                        workings=workings.substring(0, workings.length()-1) + "−";
                        workingsTV.setText(workings);
                    }else {
                        setworking("−");
                    }
                }else {
                    setworking("−");
                }
                workings=workingsTV.getText().toString();
                last = workings.charAt(workings.length() -1);
            }
        });

        btnMultiply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()!=0){
                    last = workings.charAt(workings.length() -1);
                    if(last=='+' || last=='−' || last=='×' || last=='÷'){
                        workings=workings.substring(0, workings.length()-1) + "×";
                        workingsTV.setText(workings);
                    }else {
                        setworking("×");
                    }
                }else {
                    setworking("×");
                }
                workings=workingsTV.getText().toString();
                last = workings.charAt(workings.length() -1);
            }
        });

        btnDivide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();
                if (workings.length()!=0){
                    last = workings.charAt(workings.length() -1);
                    if(last=='+' || last=='−' || last=='×' || last=='÷'){
                        workings=workings.substring(0, workings.length()-1) + "÷";
                        workingsTV.setText(workings);
                    }else {
                        setworking("÷");
                    }
                }else {
                    setworking("÷");
                }
                workings=workingsTV.getText().toString();
                last = workings.charAt(workings.length() -1);
            }
        });

        btnPercent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workings=workingsTV.getText().toString();

                char lc=workings.charAt(workings.length()-1);
                if (lc=='+'||lc=='−'||lc=='×'||lc=='÷'){
                    workings = workings.replaceAll(".$", "");
                }

                String newString=workings.substring(workings.indexOf(last)+1);
                if (workings.length()<=1){
                    char fc=workings.charAt(0);
                    if (fc=='1'||fc=='2'||fc=='3'||fc=='4'||fc=='5'||fc=='6'||fc=='7'||fc=='8'||fc=='9'){
                        double no = Double.parseDouble(newString) / 100;
                        workingsTV.setText(no+"");
                    }
                }else {
                    double no = Double.parseDouble(newString) / 100;
                    workingsTV.setText(no+"");
                }
            }
        });

        return view;
    }

    private void setworking(String i) {
        workings=workingsTV.getText().toString();
        workingsTV.setText(workings+i);
    }

}

package com.gyan.calculator.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gyan.calculator.R;

public class MoreFragment extends Fragment {

    public MoreFragment() {
        // Required empty public constructor
    }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_more, container, false);


        return view;
    }
}

package com.gyan.calculator.dotmenu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.gyan.calculator.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}

package com.gyan.calculator.dotmenu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.gyan.calculator.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}

package com.gyan.calculator.dotmenu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.gyan.calculator.R;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }
}

