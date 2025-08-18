package com.polar.mirror;

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
        assertEquals("com.polar.mirror", appContext.getPackageName());
    }
}

package com.polar.mirror;

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

package com.polar.mirror;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

class ActionPanelControllerData implements Parcelable{
    public final Boolean isPanelVisible;
    public final Boolean isFirstTimeHide;

    public static final String PARCELABLE_NAME = "ActionPanelControllerData";

    public ActionPanelControllerData(boolean _isPanelVisible, boolean _isFirstTimeHide){
        isPanelVisible = _isPanelVisible;
        isFirstTimeHide = _isFirstTimeHide;
    }

    protected ActionPanelControllerData(Parcel in) {
        isPanelVisible = in.readInt() != 0;
        isFirstTimeHide = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        // We use int here to keep our code compatible with API level < 29
        if(isPanelVisible){
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        if(isFirstTimeHide){
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ActionPanelControllerData> CREATOR =
            new Creator<ActionPanelControllerData>() {
        @Override
        public ActionPanelControllerData createFromParcel(Parcel in) {
            return new ActionPanelControllerData(in);
        }

        @Override
        public ActionPanelControllerData[] newArray(int size) {
            return new ActionPanelControllerData[size];
        }
    };
}

/**
 * Controls panel with floating action buttons
 */
public class ActionPanelController implements View.OnClickListener {
    private final static String TAG = "ActionPanelController";
    private final View mPanelView;
    private final View mOverlayView;
    private final Animation mSlideDownAnimation;
    private final Animation mSlideUpAnimation;
    private boolean mPanelVisible = true;
    private Handler mHideHandler;
    private Runnable mHideRunnable;
    private final int hideMs;
    private boolean isFirstTimeHide = true;
    private final Context mContext;
    public ActionPanelController(Context context, View panelView, View overlayView){
        mPanelView = panelView;
        mOverlayView = overlayView;
        mSlideDownAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        mSlideUpAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        hideMs = context.getResources().getInteger(R.integer.autohide_action_panel_ms);
        if(hideMs < 0){
            throw new RuntimeException("Bad configuration: negative hideMs");
        }
        mContext = context;
        setupAnimations();
        setupAutoHide();
    }

    private void setupAutoHide(){
        mHideHandler = new Handler();
        mHideRunnable = this::hidePanel;
        scheduleHide();
    }

    private void scheduleHide(){
        mHideHandler.postDelayed(mHideRunnable, hideMs);
    }

    private void cancelHide(){
        mHideHandler.removeCallbacksAndMessages(null);
    }

    private void setupAnimations(){
        mSlideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                /*stub*/
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPanelView.setVisibility(View.GONE);
                mOverlayView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                /*stub*/
            }
        });
        mSlideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mPanelView.setVisibility(View.VISIBLE);
                mOverlayView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                /*stub*/
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                /*stub*/
            }
        });
    }

    private void hidePanel(){
        if(isFirstTimeHide){
            final String toastText = mContext.getString(R.string.tap_to_show_actions);
            Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();
            isFirstTimeHide = false;
        }
        mPanelView.startAnimation(mSlideDownAnimation);
        mOverlayView.startAnimation(mSlideDownAnimation);
        mPanelVisible = false;
        mHideHandler.removeCallbacks(mHideRunnable);
    }

    private void showPanel(){
        mPanelView.startAnimation(mSlideUpAnimation);
        mOverlayView.startAnimation(mSlideUpAnimation);
        mPanelVisible = true;
        scheduleHide();
    }

    private void togglePanelVisibility(){
        if(mPanelVisible){
            hidePanel();
        } else {
            showPanel();
        }
    }
    @Override
    public void onClick(@NonNull View v) {
        final int viewId = v.getId();
        if(viewId == R.id.preview_view || viewId == R.id.stop_view){
            togglePanelVisibility();
        }
    }

    /**
     * Hides or shows panel immediately
     * @param isVisible whether panel should be visible
     */
    private void setPanelVisible(boolean isVisible){
        Log.d(TAG, "setting visibility to " + isVisible);
        mPanelVisible = isVisible;
        if(!isVisible){
            hidePanel();
            cancelHide(); //Cancel timer, so we would not show useless toasts
        } else {
            mPanelView.setVisibility(View.VISIBLE);
            mOverlayView.setVisibility(View.VISIBLE);
        }
    }


    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(ActionPanelControllerData.PARCELABLE_NAME,
                new ActionPanelControllerData(mPanelVisible, isFirstTimeHide));
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        ActionPanelControllerData controllerData =
                savedInstanceState.getParcelable(ActionPanelControllerData.PARCELABLE_NAME);
        if(controllerData == null){
            Log.w(TAG, "activityData is null, ignoring restoring instance state");
            return;
        }
        isFirstTimeHide = controllerData.isFirstTimeHide;
        setPanelVisible(controllerData.isPanelVisible);
    }
}


package com.polar.mirror;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.ExecutionException;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private PreviewView mCameraView;
    private FreezeController mFreezeController;
    private LowLightController mLowLightController;
    private ActionPanelController mActionPanelController;
    private final static String TAG = "MainActivity";
    private Preview mPreview = null;
    private static final int CAMERA_PERMISSION_CODE = 858;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestCameraPermissionIfNeeded();

        View panelView = findViewById(R.id.action_panel_layout);
        View overlayView = findViewById(R.id.overlay_view);
        mActionPanelController = new ActionPanelController(this, panelView, overlayView);
        mCameraView = findViewById(R.id.preview_view);

        setupView();

        //Initialize freeze controller
        FloatingActionButton freezeButton = findViewById(R.id.freeze_button);
        ImageView freezeView = findViewById(R.id.stop_view);
        mFreezeController = new FreezeController(this, freezeButton, mCameraView,
                freezeView);

        //Get low-light FAB
        FloatingActionButton lowLightModeButton = findViewById(R.id.low_light_button);

        //Get low-light overlay
        ImageView lowLightOverlay = findViewById(R.id.low_light_overlay_view);

        //Start camera
        try {
            startCamera();
            mLowLightController = new LowLightController(this, lowLightModeButton,
                    lowLightOverlay);
        } catch (ExecutionException | InterruptedException e) {
            final String toastText = getString(R.string.can_not_start_camera);
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        setupView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mActionPanelController.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mActionPanelController.onRestoreInstanceState(savedInstanceState);
    }


    /**
     * Makes app fullscreen and applies other cosmetic options
     */
    private void setupView(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        } else {
            Log.e(TAG, "Can not hide action bar: got null from getSupportActionBar()");
        }
        setupFloatingButtons();
        hideSystemUi();
        setupPanel();
    }

    /**
     * Setups logic of action panel in a general
     */
    private void setupPanel(){
        //Setup action panel
        View stopView = findViewById(R.id.stop_view);
        mCameraView.setClickable(true);
        mCameraView.setOnClickListener(mActionPanelController);
        stopView.setClickable(true);
        stopView.setOnClickListener(mActionPanelController);
    }

    /**
     * Setups actions for floating buttons
     */
    private void setupFloatingButtons(){
        FloatingActionButton exitButton = findViewById(R.id.exit_button);
        FloatingActionButton freezeButton = findViewById(R.id.freeze_button);
        FloatingActionButton lowLightButton = findViewById(R.id.low_light_button);
        exitButton.setClickable(true);
        exitButton.setOnClickListener(this);
        freezeButton.setClickable(true);
        freezeButton.setOnClickListener(this);
        lowLightButton.setClickable(true);
        lowLightButton.setOnClickListener(this);
    }

    /**
     * Hides SystemUI elements such as navigation and status bars
     */
    private void hideSystemUi(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /**
     * Initializes camera stream
     * @throws ExecutionException in case of task errors
     * @throws InterruptedException in case of thread interruption
     */
    private void startCamera() throws ExecutionException, InterruptedException {
        mPreview = new Preview.Builder().build();
        mPreview.setSurfaceProvider(mCameraView.getSurfaceProvider());
        ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
        try {
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, mPreview);
            mFreezeController.onCameraInitialized(cameraProvider, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggles camera freeze mode
     */
    private void toggleCameraFreeze(){
        mFreezeController.toggleFreeze();
    }

    private void toggleLowLightMode(){
        if(mLowLightController == null){
            Log.wtf(TAG, "Low-light mode controller is null");
            return;
        }
        mLowLightController.toggleLowLightMode();
    }

    @Override
    public void onClick(@NonNull View v) {
        int viewId = v.getId();
        if(viewId == R.id.exit_button){
            Log.d(TAG, "Exit button pressed");
            super.finish();
        } else if(viewId == R.id.freeze_button) {
            toggleCameraFreeze();
        } else if(viewId == R.id.low_light_button){
            toggleLowLightMode();
        } else {
            Log.w(TAG, "Unknown id of view: " + viewId);
        }
    }

    public void requestCameraPermissionIfNeeded() {
        if(Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission is granted.");
                return;
            }
            Log.d(TAG, "Camera permission is not granted yet, so will request it now");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (!(grantResults.length > 0) || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                final String toastText = getString(R.string.no_camera_permissions);
                Log.d(TAG, "User denied camera permission");
                Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
            }
        }
    }
}



package com.polar.mirror;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class Utils {
    private static final String TAG = "Utils";

    /**
     * @param context context for getting WindowManager
     * @return orientation state as in Surface class
     */
    public static int getOrientation(Context context){
        int rotation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
        Log.d(TAG, "Rotation: " + rotation);
        return rotation;
    }
}


package com.polar.mirror;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.camera.core.Preview;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Implements low-light mode
 */
public class LowLightController {
    private final Activity mActivity;
    private float lastBrightness = 1;
    public boolean isLowLightModeEnabled = false;
    private final FloatingActionButton mLowLightModeButton;
    private final ImageView mLowLightOverlay;
    private static final String TAG = "LowLightController";
    private static final int WHITENING_VALUE = 128;


    LowLightController(Activity activity, FloatingActionButton lowLightModeButton,
                       ImageView lowLightOverlay){
        mActivity = activity;
        mLowLightModeButton = lowLightModeButton;
        mLowLightOverlay = lowLightOverlay;
    }

    private void enableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        lastBrightness = layout.screenBrightness;
        layout.screenBrightness = 1F;
        mActivity.getWindow().setAttributes(layout);
        //Set image on FAB
        mLowLightModeButton.setImageResource(R.drawable.flashlight_off);
        //Enable whitening overlay
        mLowLightOverlay.setVisibility(View.VISIBLE);
    }

    private void disableLowLightMode(){
        WindowManager.LayoutParams layout = mActivity.getWindow().getAttributes();
        Log.d(TAG, "Setting brightness " + lastBrightness);
        layout.screenBrightness = lastBrightness;
        mActivity.getWindow().setAttributes(layout);
        //Set image on FAB
        mLowLightModeButton.setImageResource(R.drawable.flashlight_on);
        //Disable whitening overlay
        mLowLightOverlay.setVisibility(View.GONE);
    }

    /**
     * Toggles low-light mode
     * @return whether low-light mode is enabled
     */
    public boolean toggleLowLightMode(){
        if(isLowLightModeEnabled){
            disableLowLightMode();
        }else{
            enableLowLightMode();
        }
        isLowLightModeEnabled = !isLowLightModeEnabled;
        return isLowLightModeEnabled;
    }
}


package com.polar.mirror;

import static androidx.core.content.ContextCompat.getMainExecutor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.nio.ByteBuffer;

/**
 * Controls freezing camera view
 */
public class FreezeController {
    private static final String TAG = "FreezeController";
    private final PreviewView mCameraView;
    private final ImageView mFreezeView;
    private final ImageCapture mImageCapture;
    private final Context mContext;
    private boolean mCameraFrozen = false;
    private final FloatingActionButton mFreezeButton;

    FreezeController(Context context, FloatingActionButton freezeButton, PreviewView cameraView,
                     ImageView freezeView){
        mCameraView = cameraView;
        mFreezeView = freezeView;
        mContext = context;
        mImageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        mFreezeButton = freezeButton;
    }

    /**
     * Should be called when camera is ready
     * @param provider camera provider
     * @param lcOwner lifecycle owner used for binding camera use-cases
     */
    public void onCameraInitialized(@NonNull ProcessCameraProvider provider, LifecycleOwner lcOwner){
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();
        provider.bindToLifecycle(lcOwner, cameraSelector, mImageCapture);
        Log.d(TAG, "completed onCameraInitialized");
    }

    private int getRotationAngleFromOrientation(int orientation){
        int angle = 270;
        switch (orientation) {
            case Surface.ROTATION_90:
                angle = 0;
                break;
            case Surface.ROTATION_180:
                angle = 90;
                break;
            case Surface.ROTATION_270:
                angle = 180;
                break;
            default:
                break;
        }
        return angle;
    }

    private Bitmap processFreezeImage(byte[] bytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        // Rotate image
        int rotation = getRotationAngleFromOrientation(Utils.getOrientation(mContext));
        matrix.postRotate(rotation);
        bitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
        );
        //Mirror image
        matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f);
        bitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
        );
        return bitmap;
    }


    private void setFrozenImage(){
        mImageCapture.takePicture(getMainExecutor(mContext),
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    @SuppressLint("UnsafeOptInUsageError")
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy){
                        Log.i(TAG, "Capture success");
                        Image image = imageProxy.getImage();
                        if(image == null){
                            Log.e(TAG, "Image is null");
                            return;
                        }
                        int format = image.getFormat();
                        if(format != ImageFormat.JPEG){
                            Log.e(TAG, "Expected JPEG format, got format " + format);
                            return;
                        }
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        Bitmap bitmap = processFreezeImage(bytes);
                        mFreezeView.setImageBitmap(bitmap);
                        imageProxy.close();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Can not capture image");
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Toggles camera freeze
     */
    public void toggleFreeze(){
        if(mCameraFrozen){
            mFreezeView.setVisibility(View.GONE);
            mCameraView.setVisibility(View.VISIBLE);
            mFreezeButton.setImageResource(android.R.drawable.ic_media_pause);
            mCameraFrozen = false;
        } else {
            setFrozenImage();
            mCameraView.setVisibility(View.GONE);
            mFreezeView.setVisibility(View.VISIBLE);
            mFreezeButton.setImageResource(android.R.drawable.ic_media_play);
            mCameraFrozen = true;
        }
    }
}


