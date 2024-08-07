package com.fari.jkslinks;

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

        assertEquals("com.fari.jkslinks", appContext.getPackageName());
    }
}


package com.fari.jkslinks;

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

package com.fari.jkslinks;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToWikipedia(View view)
    {
        Intent browser = new Intent(Intent.ACTION_VIEW,Uri.parse("https://en.wikipedia.org/wiki/Jang_Keun-suk"));
        startActivity(browser);
    }

    public void goToOfficial(View view)
    {
        Intent browser = new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.princejks.com/"));
        startActivity(browser);
    }

    public void goToFacebook(View view)
    {
        Intent browser = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.facebook.com/AsiaPrinceJKS0804"));
        startActivity(browser);
    }

    public void goToInstagram(View view)
    {
        Intent browser = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/_asia_prince_jks/"));
        startActivity(browser);
    }

    public void goToYoutube(View view)
    {
        Intent browser = new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.youtube.com/user/PrinceJKS"));
        startActivity(browser);
    }

    public void goToTwitter(View view)
    {
        Intent browser = new Intent(Intent.ACTION_VIEW,Uri.parse("https://twitter.com/AsiaPrince_JKS"));
        startActivity(browser);
    }
}

