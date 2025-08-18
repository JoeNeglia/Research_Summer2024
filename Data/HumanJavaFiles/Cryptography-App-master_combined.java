package sa.alburooj.enigma;

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
        assertEquals("sa.alburooj.enigma", appContext.getPackageName());
    }
}

package sa.alburooj.enigma;

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

package sa.alburooj.enigma;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class aboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}

package sa.alburooj.enigma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View decorView = getWindow().getDecorView();
        //Hide the status bar
        int uiOptions =View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //Hide the action bar
        getSupportActionBar().hide();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this,homeActivity.class);
                MainActivity.this.startActivity(mainIntent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}

package sa.alburooj.enigma;
import java.util.*;


public class cryptography {
    //to keep track of index
    public static final String alpha = "abcdefghijklmnopqrstuvwxyz";
    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    //Encryption
    public  String encrypt(String message, int shiftKey) {
        message = message.toLowerCase();
        String cipherText = "";
        for (int ii = 0; ii < message.length(); ii++) {
            int charPosition = alpha.indexOf(message.charAt(ii));
            int keyVal = (shiftKey + charPosition) % 26;
            char replaceVal = alpha.charAt(keyVal);
            cipherText += replaceVal;
        }
        return cipherText;
    }

    //Decryption
    public  String decrypt(String cipherText, int shiftKey) {
        cipherText = cipherText.toLowerCase();
        String message = "";
        for (int ii = 0; ii < cipherText.length(); ii++) {
            int charPosition = ALPHABET.indexOf(cipherText.charAt(ii));
            int keyVal = (charPosition - shiftKey) % 26;
            if (keyVal < 0) {
                keyVal = ALPHABET.length() + keyVal;
            }
            char replaceVal = ALPHABET.charAt(keyVal);
            message += replaceVal;
        }
        return message;
    }
}


package sa.alburooj.enigma;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class decryptActivity extends AppCompatActivity {

    //Declaration
    EditText et_decrypt_input,et_key_input;
    Button btn_decrypted;
    TextView tv_decrypted;

    String message,d_message;
    int key;

    //Class Instance
    cryptography decrypt =new cryptography();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);
        getSupportActionBar().setTitle("Decrypt");

        on_menu(); //Whats on Menu
        on_click();//Their Click Func.
    }

    private void on_menu() {

        et_decrypt_input = findViewById(R.id.et_decrypt_input);
        et_key_input = findViewById(R.id.et_key_input_d);
        btn_decrypted = findViewById(R.id.btn_to_decrypt);
        tv_decrypted =findViewById(R.id.tv_decrypted);

        et_key_input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    }

    private void on_click() {

        btn_decrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                message = et_decrypt_input.getText().toString();
                key = Integer.parseInt(et_key_input.getText().toString());


                d_message = decrypt.decrypt(message,key);

                tv_decrypted.setText(d_message);

            }
        });
    }
}

package sa.alburooj.enigma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class homeActivity extends AppCompatActivity {

    private Button btn_encrypt,btn_decrypt;
    private ImageView img_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        on_menu();//What's on Menu
        on_click();//Their on Click Func.
    }

    private void on_click() {

        btn_encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(homeActivity.this, encryptActivity.class);
                startActivity(intent);
            }
        });

        btn_decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(homeActivity.this, decryptActivity.class);
                startActivity(intent);
            }
        });

        img_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(homeActivity.this, aboutActivity.class);
                startActivity(intent);
            }
        });
    }

    private void on_menu() {

        btn_encrypt = findViewById(R.id.btn_encryption);
        btn_decrypt = findViewById(R.id.btn_decryption);
        img_about = findViewById(R.id.img_about);
    }
}

package sa.alburooj.enigma;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class encryptActivity extends AppCompatActivity {

    //Declaration
    EditText et_encrypt_input,et_key_input;
    Button btn_encrypted;
    TextView tv_encrypted;

    String message,e_message;
    int key;

    //Instance of Class
    cryptography encrypt =new cryptography();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encrypt);
        getSupportActionBar().setTitle("Encrypt");


        on_menu(); //What's on Menu
        on_click();//Their on Click Func.


    }

    private void on_menu() {

        et_encrypt_input = findViewById(R.id.et_encrypt_input);
        et_key_input = findViewById(R.id.et_key_input_e);
        btn_encrypted = findViewById(R.id.btn_to_encrypt);
        tv_encrypted =findViewById(R.id.tv_encrypted);

        et_key_input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    }

    private void on_click() {

        btn_encrypted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                message = et_encrypt_input.getText().toString();
                key = Integer.parseInt(et_key_input.getText().toString());

                 e_message = encrypt.encrypt(message,key);

                 tv_encrypted.setText(e_message);
            }
        });
    }
}

