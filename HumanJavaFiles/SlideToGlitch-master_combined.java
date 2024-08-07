package com.smv.slidetoglitch;

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

        assertEquals("com.smv.slidetoglitch", appContext.getPackageName());
    }
}


package com.smv.slidetoglitch;

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

package com.smv.slidetoglitch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smv.slidetoglitch.Filter.Glitcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    public static final String IMAGE_URI = "IMAGE_URI_KEY";
    private ImageView imageView;
    private Bitmap originalBitmap;
    private Bitmap glitchBitmap;
    Uri imageUri;

    private int amount=24;
    private int seed=53;
    private int iterations=21;
    private int quality = 69;

    private SeekBar amountSeek;
    private SeekBar seedSeek;
    private SeekBar iterationsSeek;
    private SeekBar qualitySeek;
    private TextView amountText;
    private TextView seedText;
    private TextView iterationsText;
    private TextView qualityText;
    private Button originalPreview;

    private LinearLayout glitchLayout;
    private LinearLayout aboutLayout;
    private Button glitchButton;
    private Button aboutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imageUri = Uri.parse(getIntent().getExtras().getString(IMAGE_URI));
            try {
                InputStream in =  getContentResolver().openInputStream(imageUri);
                originalBitmap = decodeFile(in);
                imageView.setImageBitmap(originalBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        amountText = findViewById(R.id.amountValue);
        seedText = findViewById(R.id.seedValue);
        iterationsText = findViewById(R.id.iterationsValue);
        qualityText = findViewById(R.id.qualityValue);

        amountSeek = findViewById(R.id.amountSeek);
        seedSeek = findViewById(R.id.seedSeek);
        iterationsSeek = findViewById(R.id.iterationsSeek);
        qualitySeek = findViewById(R.id.qualitySeek);

        amountSeek.setOnSeekBarChangeListener(this);
        seedSeek.setOnSeekBarChangeListener(this);
        iterationsSeek.setOnSeekBarChangeListener(this);
        qualitySeek.setOnSeekBarChangeListener(this);

        glitchLayout = findViewById(R.id.glitchLayout);
        aboutLayout = findViewById(R.id.aboutLayout);
        glitchButton = findViewById(R.id.Glitch);
        aboutButton = findViewById(R.id.About);
        glitchLayout.setVisibility(View.VISIBLE);
        aboutLayout.setVisibility(View.INVISIBLE);

        originalPreview = findViewById(R.id.originalPreview);
        originalPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    imageView.setImageBitmap(originalBitmap);
                }else{
                    imageView.setImageBitmap(glitchBitmap);
                }
                return true;
            }
        });

        initGlitch();
    }

    private Bitmap decodeFile(InputStream in) {
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int IMAGE_MAX_SIZE = 1000;
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = getContentResolver().openInputStream(imageUri);
            b = BitmapFactory.decodeStream(in, null, o2);
            in.close();


        }catch (Exception e){
            e.printStackTrace();
        }
        return b;
    }

    public void initGlitch(){
        glitchBitmap = Glitcher.glitch(originalBitmap,amount,seed,iterations,quality);
        imageView.setImageBitmap(glitchBitmap);
        amountText.setText(""+amount);
        amountSeek.setProgress(amount);
        seedText.setText(""+seed);
        seedSeek.setProgress(seed);
        iterationsText.setText(""+iterations);
        iterationsSeek.setProgress(iterations);
        qualityText.setText(""+quality);
        qualitySeek.setProgress(quality);
    }


    public void aboutVisible(View view) {
        glitchLayout.setVisibility(View.INVISIBLE);
        aboutLayout.setVisibility(View.VISIBLE);
    }

    public void glitchVisible(View view) {
        glitchLayout.setVisibility(View.VISIBLE);
        aboutLayout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.amountSeek:
                amount = progress;
                glitchBitmap = Glitcher.glitch(originalBitmap, amount, seed, iterations, quality);
                imageView.setImageBitmap(glitchBitmap);
                amountText.setText("" + progress);
                break;
            case R.id.seedSeek:
                seed = progress;
                glitchBitmap = Glitcher.glitch(originalBitmap, amount, seed, iterations, quality);
                imageView.setImageBitmap(glitchBitmap);
                seedText.setText("" + progress);
                break;
            case R.id.iterationsSeek:
                iterations = progress;
                glitchBitmap = Glitcher.glitch(originalBitmap, amount, seed, iterations, quality);
                imageView.setImageBitmap(glitchBitmap);
                iterationsText.setText("" + progress);
                break;
            case R.id.qualitySeek:
                if (progress == 0) {
                    quality = 1;
                    glitchBitmap = Glitcher.glitch(originalBitmap, amount, seed, iterations, quality);
                    imageView.setImageBitmap(glitchBitmap);
                    qualityText.setText("" + 1);
                } else {
                    quality = progress;
                    glitchBitmap = Glitcher.glitch(originalBitmap, amount, seed, iterations, quality);
                    imageView.setImageBitmap(glitchBitmap);
                    qualityText.setText("" + progress);
                }
                break;
        }
        }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public static Intent getIntent(Context context, Bundle bundle) {
        Intent intent = new Intent(context, MainActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        return intent;
    }

    public void saveImage(View view) {
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        String imageFileName = "SToG_" + timeStamp + ".jpeg";
        File direct = new File(Environment.getExternalStorageDirectory()+"/SToG");
        if(!direct.exists()){
            File imageDirect = new File("/storage/emulated/0/SToG/");
            imageDirect.mkdir();
        }
        File file = new File("/storage/emulated/0/SToG/",imageFileName);
        if(file.exists()){
            file.delete();
        }
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(file);
            glitchBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Image Saved at SToG/", Toast.LENGTH_SHORT).show();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void backToHome(View view) {
        finish();
    }
}


package com.smv.slidetoglitch;

import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int delayDuration = 1500;
                new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(HomeActivity.getIntent(SplashActivity.this));
                finish();
            }
        }, delayDuration);
    }

}


package com.smv.slidetoglitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class HomeActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQ_CODE = 1001;
    private static final int STORAGE_PERMISSION_REQ_CODE = 1002;
    private String mCapturedImagePath;
    private static final String FILE_PROVIDER_AUTHORITY = "com.smv.photoedit";
    private static final int GALLERY_RESULT = 1;
    private static final int CAMERA_RESULT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    public void openCamera(View view) {
        if(ContextCompat.checkSelfPermission(this,CAMERA)!= PERMISSION_GRANTED){
            String[] cameraPermission = { CAMERA };
            ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_PERMISSION_REQ_CODE);
        }else {
            dispatchImageCaptureIntent();
        }
    }

    private void dispatchImageCaptureIntent(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraIntent.resolveActivity(getPackageManager())!=null){
            File photo = null;
            try {
                photo = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photo != null) {
                Uri photoFileUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photo);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
                startActivityForResult(cameraIntent, CAMERA_RESULT);
            }
        }
    }
    private File createImageFile()throws IOException {
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCapturedImagePath = image.getAbsolutePath();
        return image;
    }

    public void openGallery(View view){
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            String[] storagePermissions = { READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE };
            ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_PERMISSION_REQ_CODE);
        } else {
            dispatchGalleryIntent();
        }
    }
    private void dispatchGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_RESULT);
    }


    private Bundle uriToBundle(Uri imageUri) {
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.IMAGE_URI, imageUri.toString());
        return bundle;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_RESULT) {
                Uri imageUri = data.getData();
                startActivity(MainActivity.getIntent(this, uriToBundle(Objects.requireNonNull(imageUri))));
            } else if (requestCode == CAMERA_RESULT) {
                    File imageFile = new File(mCapturedImagePath);
                    Uri imageUri = Uri.fromFile(imageFile);
                    startActivity(MainActivity.getIntent(this, uriToBundle(imageUri)));
            }
        } else {
            Toast.makeText(this, "Image not loaded.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_REQ_CODE:
                if (grantResults[0] == PERMISSION_GRANTED) {
                    dispatchImageCaptureIntent();
                } else {
                    Toast.makeText(this, "Required camera permission not granted", Toast.LENGTH_SHORT).show();
                }
                break;

            case STORAGE_PERMISSION_REQ_CODE:
                if (grantResults[0] == PERMISSION_GRANTED) {
                    dispatchGalleryIntent();
                } else {
                    Toast.makeText(this, "Required storage permission not granted", Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            default:
                throw new IllegalArgumentException("Unexpected request code");
        }
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }


}


package com.smv.slidetoglitch.Filter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class Glitcher {

    static double amount,seed,iterations;
    static byte[] imageByte;
    static int jpgHeaderLength;
    public static Bitmap glitch(Bitmap bitmap, int amount1, int seed1, int iterations1, int quality1){
        amount = amount1;
        seed = seed1;
        iterations = iterations1;
        normalized();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality1, bos);
        imageByte = bos.toByteArray();
        jpgHeaderLength = getJpegHeaderSize();
        for (int i = 0, len = (int)iterations; i < len; i++ ) {
            glitchJpegBytes(i);
        }
        Bitmap glitchedBitmap = BitmapFactory.decodeByteArray(imageByte,0,imageByte.length);
        return glitchedBitmap;

    }

    private static void glitchJpegBytes (int i) {
        int maxIndex = imageByte.length - jpgHeaderLength - 4;
        double pxMin = ( maxIndex / iterations * i);
        double pxMax = ( maxIndex / iterations * ( i + 1 ));
        double delta = pxMax - pxMin;
        double pxIndex = ( pxMin + delta * seed);
        if ( pxIndex > maxIndex ) {
            pxIndex = maxIndex;
        }
        int index = (int)Math.floor( jpgHeaderLength + pxIndex );
        imageByte[index] = (byte)Math.floor( amount * 256 );
    }
    private static int getJpegHeaderSize() {
        int result = 417;
        for(int i = 0,len = imageByte.length; i < len; i++ ) {
            if ( imageByte[i] == 255 && imageByte[i + 1] == 218 ) {
                result = i + 2;
                break;
            }
        }
        return result;
    }

    public static void normalized(){
        seed = seed/100;
        amount = amount/100;
    }

}


