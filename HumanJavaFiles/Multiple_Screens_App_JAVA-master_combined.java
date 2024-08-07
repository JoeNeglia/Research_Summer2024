package com.example.multiple_screens_appjava;

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
        assertEquals("com.example.multiple_screens_appjava", appContext.getPackageName());
    }
}

package com.example.multiple_screens_appjava;

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

package com.example.multiple_screens_appjava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ThirdPage extends AppCompatActivity {
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_page);
        backButton = findViewById(R.id.button4);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ThirdPage.this, MainActivity.class);
            startActivity(intent);
        });
    }
}

package com.example.multiple_screens_appjava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SecondPage extends AppCompatActivity {
    Button nextPageButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_page);
        nextPageButton = findViewById(R.id.button2);
        backButton = findViewById(R.id.button3);

        nextPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(SecondPage.this, ThirdPage.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SecondPage.this, MainActivity.class);
            startActivity(intent);
        });
    }
}

package com.example.multiple_screens_appjava;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button nextPageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nextPageButton = findViewById(R.id.button);

        nextPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondPage.class);
            startActivity(intent);
        });
    }
}

