package com.example.flashlight_imagebutton_app_java;

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
        assertEquals("com.example.flashlight_imagebutton_app_java", appContext.getPackageName());
    }
}

package com.example.flashlight_imagebutton_app_java;

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

package com.example.flashlight_imagebutton_app_java;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    ImageButton imageButton;
    CameraManager cameraManager = (CameraManager)getSystemService(CAMERA_SERVICE);
    private boolean flashlightStateChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageButton = findViewById(R.id.imageButton3);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (!flashlightStateChanged) {
                    try {
                        String cameraIdForFlashlight = cameraManager.getCameraIdList()[0];
                        cameraManager.setTorchMode(cameraIdForFlashlight, true);
                        imageButton.setImageResource(R.drawable.on);
                        flashlightStateChanged = true;
                    } catch (CameraAccessException exception) {
//                        System.out.println(exception);
                    }
                } else {
                    try {
                        String cameraIdForFlashlight = cameraManager.getCameraIdList()[0];
                        cameraManager.setTorchMode(cameraIdForFlashlight, false);
                        imageButton.setImageResource(R.drawable.off);
                        flashlightStateChanged = false;
                    } catch (CameraAccessException exception) {
//                        System.out.println(exception);
                    }
                }
            }
        });
    }
}

