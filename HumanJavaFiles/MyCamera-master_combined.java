package com.farizma.mycamera;

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
        assertEquals("com.farizma.mycamera", appContext.getPackageName());
    }
}

package com.farizma.mycamera;

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

package com.farizma.mycamera;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_CODE = 10;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera camera;
    private CameraPreview cameraPreview;
    private MediaRecorder mediaRecorder;
    private FrameLayout preview;
    private Button photo, video;
    private ImageButton capture, record, flip, pausePlayButton;
    private TextView textRecording;

    private boolean isRecording = false;
    private boolean isPause = false;
    private boolean isBack = true;

    private File videoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE);

        if (!allPermissionsGranted()) {
            finish();
        }

        capture = findViewById(R.id.capture);
        record = findViewById(R.id.record);
        flip = findViewById(R.id.flip);
        photo = findViewById(R.id.photo);
        video = findViewById(R.id.video);
        textRecording = findViewById(R.id.textRecording);
        pausePlayButton = findViewById(R.id.pausePlay);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, Picture);
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBack) startRecording(Camera.CameraInfo.CAMERA_FACING_BACK);
                else startRecording(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
        });

        flip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipCamera();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isBack) start(Camera.CameraInfo.CAMERA_FACING_BACK);
        else  start(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    private void start(int face) {
        if(camera != null)
            releaseCamera();

        camera = getCameraInstance(face);
        cameraPreview = new CameraPreview(this, camera);
        camera.setDisplayOrientation(90);

        preview = findViewById(R.id.cameraPreview);
        preview.addView(cameraPreview);

        capture.setVisibility(View.VISIBLE);
        record.setVisibility(View.INVISIBLE);

        photo.setTextColor(getColor(R.color.red));
        video.setTextColor(getColor(R.color.black));
    }

    private void flipCamera() {
        if(isBack) {
            isBack = false;
            start(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            isBack = true;
            start(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
    }

    private Camera.PictureCallback Picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if(pictureFile == null) {
                Log.d("MediaFile", "Error creating media file, check storage permissions");
                return;
            }

            try {
                Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(1, info);
                Bitmap bitmap;

                if(isBack) bitmap = rotate(realImage, 90);
                else bitmap = rotate(realImage, 270);

                FileOutputStream fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Toast.makeText(MainActivity.this, R.string.saved_image, Toast.LENGTH_SHORT).show();
                //fos.write(data);
                fos.flush();
                fos.close();

                // scan to make it visible in gallery
                scanFile(pictureFile.toString());
            }catch (FileNotFoundException e) {
                Log.d("MediaFile", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("MediaFile", "Error accessing file: " + e.getMessage());
            }
        }
    };

    private boolean prepareVideoRecorder(int face) {
        videoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);

        camera = getCameraInstance(face);
        camera.setDisplayOrientation(90);
        mediaRecorder = new MediaRecorder();
        if(isBack) mediaRecorder.setOrientationHint(90);
        else mediaRecorder.setOrientationHint(270);
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mediaRecorder.setOutputFile(videoFile.toString());
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("VIDEO_RECORDER", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("VIDEO_RECORDER", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void startRecording(int face) {
        if(isRecording) {
            mediaRecorder.stop();
            releaseMediaRecorder();
            camera.lock();
            // inform the user that recording has stopped
            Toast.makeText(this, R.string.saved_video, Toast.LENGTH_SHORT).show();
            stopTimer();
            flip.setVisibility(View.VISIBLE);
            isRecording = false;
            // scan to make it visible in gallery
            scanFile(videoFile.toString());
        } else {
            if(prepareVideoRecorder(face)) {
                Toast.makeText(this, R.string.recording_start, Toast.LENGTH_SHORT).show();
                flip.setVisibility(View.INVISIBLE);
                mediaRecorder.start();
                // inform the user that recording has started
                startTimer();
                isRecording = true;
            } else {
                releaseMediaRecorder();
            }
        }
    }

    private void startTimer() {
        pausePlayButton.setVisibility(View.VISIBLE);
        pausePlayButton.setImageDrawable(getDrawable(R.drawable.ic_pause));
        isPause = false;
        //TODO: start timer
        textRecording.setText("Recording...");
    }

    private void stopTimer() {
        pausePlayButton.setVisibility(View.INVISIBLE);
        isPause = false;
        //TODO: stop timer
        textRecording.setText("");
    }

    public void playPause(View view) {
        if(!isPause) {
            //TODO: pause timer
            Toast.makeText(this, R.string.recording_pause, Toast.LENGTH_SHORT).show();
            textRecording.setText("Pause");
            mediaRecorder.pause();
            pausePlayButton.setImageDrawable(getDrawable(R.drawable.ic_play));
            isPause = true;
        } else {
            //TODO: resume timer
            textRecording.setText("Recording...");
            mediaRecorder.resume();
            pausePlayButton.setImageDrawable(getDrawable(R.drawable.ic_pause));
            isPause = false;
        }
    }

    public void switchMode(View view) {
        if(view.getId() == R.id.photo) {
            photo.setTextColor(getColor(R.color.red));
            video.setTextColor(getColor(R.color.black));
            record.setVisibility(View.INVISIBLE);
            capture.setVisibility(View.VISIBLE);
        }
        else {
            photo.setTextColor(getColor(R.color.black));
            video.setTextColor(getColor(R.color.red));
            capture.setVisibility(View.INVISIBLE);
            record.setVisibility(View.VISIBLE);
        }
    }

    private static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    private boolean allPermissionsGranted() {
        boolean flag = true;
        for(int i=0; i< REQUIRED_PERMISSIONS.length; i++)
            if(ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED)
                flag = false;
        return flag;
    }

    public Camera getCameraInstance(int face) {
        Camera c = null;
        try {
            c = Camera.open(face);
        }
        catch (Exception e) {
            Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show();
        }
        return c;
    }

    private  File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCamera");
        if(!mediaStorageDir.exists())
            if(!mediaStorageDir.mkdirs()) {
                Log.d("MyCamera", "Failed to create directory");
                return null;
            }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE)
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpeg");
        else if(type == MEDIA_TYPE_VIDEO)
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
        else
            return null;

        return mediaFile;
    }

    private void scanFile(String file) {
        MediaScannerConnection.scanFile(this,
                new String[]{file}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String s, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + s + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    private void releaseCamera() {
        if(camera != null) {
            camera.release();
            camera = null;
        }
    }

    private void releaseMediaRecorder() {
        if(mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        releaseMediaRecorder();
    }
}

package com.farizma.mycamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder holder;
    private Camera camera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch (IOException e) {
            Log.d("ERROR", "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        if(holder.getSurface() == null)
            return;

        try {
            camera.stopPreview();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // start preview with new settings
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        }
        catch (Exception e){
            Log.d("ERROR", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}

