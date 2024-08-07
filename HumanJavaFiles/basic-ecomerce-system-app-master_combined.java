package com.example.zakaria.myproducts;

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

        assertEquals("com.example.ifty.myproducts", appContext.getPackageName());
    }
}


package com.example.zakaria.myproducts;

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

package com.example.zakaria.myproducts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EmailActivity extends AppCompatActivity{

    private static EditText toEmail, subject, body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        toEmail = findViewById(R.id.toEmailEditText);
        subject = findViewById(R.id.subjectEditText);
        body = findViewById(R.id.messageEditText);
    }

    public void sendUsEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, toEmail.getText().toString());
        intent.putExtra(Intent.EXTRA_SUBJECT, subject.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, body.getText().toString());
        intent.setType("message/rfc822");

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}


package com.example.zakaria.myproducts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemsActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> itemList;
    private String type;
    private String checkItem;
    private TextView productType;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        listView=findViewById(R.id.itemListV);
        productType=findViewById(R.id.productType);
        itemList=new ArrayList<>();
        type=getIntent().getStringExtra("type");
        itemList=getIntent().getStringArrayListExtra("items");

        productType.setText(type);
        ArrayAdapter<String> listAdapter=new ArrayAdapter<String>(this,R.layout.item_list_row,R.id.item_row,itemList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                checkItem=adapterView.getItemAtPosition(position).toString();
                intent=new Intent(ItemsActivity.this,PostAdActivity.class);
                intent.putExtra("item",checkItem);
                startActivity(intent);
            }
        });
    }
}


package com.example.zakaria.myproducts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PhoneVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
    }
}


package com.example.zakaria.myproducts;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zakaria.myproducts.models.MobileProduct;
import com.example.zakaria.myproducts.models.Others;
import com.example.zakaria.myproducts.models.UpdateProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ru.dimorinny.floatingtextbutton.FloatingTextButton;

public class ProductListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabaseRef;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<MobileProduct> productList;
    private AllProductAdapter allProductAdapter;

    private Button signUpButton, phoneVerifyButton;
    private TextView login, close, noDataFoundTV;
    private boolean isAddProductButton;
    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        FloatingTextButton addProductBtn = findViewById(R.id.postAddBtn);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recylerAllV);
        noDataFoundTV = findViewById(R.id.noDataFoundTV);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(ProductListActivity.this, 2));
        productList = new ArrayList<>();

        if (firebaseAuth.getCurrentUser() != null) {
            noDataFoundTV.setVisibility(View.GONE);

            mDatabaseRef = FirebaseDatabase.getInstance().getReference("product_list").child(firebaseAuth.getCurrentUser().getUid());

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            MobileProduct mobileProduct = postSnapshot.getValue(MobileProduct.class);
                            productList.add(mobileProduct);
                        }

                        allProductAdapter = new AllProductAdapter(ProductListActivity.this, productList);
                        recyclerView.setAdapter(allProductAdapter);

                    }
                    else {
                        Toast.makeText(ProductListActivity.this, "No data found", Toast.LENGTH_LONG).show();
                        noDataFoundTV.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProductListActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            noDataFoundTV.setVisibility(View.VISIBLE);
        }

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(ProductListActivity.this, ProductAddActivity.class));
                    finish();
                } else {
                    isAddProductButton = true;
                    getSignUpFloatingAction();
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        if (firebaseAuth.getCurrentUser() == null) {
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.myAccount).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.accountIconWithEmailOrPhone).setVisible(false);

            menu.findItem(R.id.accountIcon).setIcon(R.drawable.ic_account_circle_black_24dp);
            menu.findItem(R.id.accountIcon).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    isAddProductButton = false;
                    getSignUpFloatingAction();
                    return true;
                }
            });
        } else {
            String userEmail = firebaseAuth.getCurrentUser().getEmail();
            for (int i = 0; i < userEmail.length(); i++) {
                if (userEmail.charAt(i) != '@') {
                    userName += userEmail.charAt(i);
                }
                else if (userEmail.charAt(i) == '@') {
                    break;
                }
            }

            menu.findItem(R.id.logout).setVisible(true);
            menu.findItem(R.id.myAccount).setVisible(true);
            menu.findItem(R.id.accountIconWithEmailOrPhone).setVisible(true);

            menu.findItem(R.id.accountIcon).setIcon(R.drawable.user);
            menu.findItem(R.id.accountIconWithEmailOrPhone).setIcon(R.drawable.user);
            menu.findItem(R.id.accountIconWithEmailOrPhone).setTitle(userName);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.myAccount) {
            Toast.makeText(this, "my", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.logout) {
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseAuth.signOut();
                Toast.makeText(this, "Log out successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProductListActivity.this, ProductListActivity.class));
                finish();
            }
        } else if (id == R.id.shareApp) {
            Toast.makeText(this, "share", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.faq) {
            startActivity(new Intent(ProductListActivity.this, FaqActivity.class));
        } else if (id == R.id.about) {
            Others others = new Others(this);
            others.aboutUs();
        } else if (id == R.id.send) {
            startActivity(new Intent(this, EmailActivity.class));
        } else if (id == R.id.updateProfile) {
            startActivity(new Intent(ProductListActivity.this, UpdateProfileActivity.class));
        } else if (id == R.id.changePassword) {
            startActivity(new Intent(ProductListActivity.this, PasswordChangeActivity.class).putExtra("password_key", "change_password"));
        }
        return super.onOptionsItemSelected(item);
    }

    public void getSignUpFloatingAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.sign_up_popup, null);
        builder.setView(view);

        signUpButton = view.findViewById(R.id.signUpPopUpId);
        phoneVerifyButton = view.findViewById(R.id.phoneVerifyPopUpId);
        login = view.findViewById(R.id.loginPopUpId);
        close = view.findViewById(R.id.closePopUpId);

        final AlertDialog alertDialog = builder.create();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAddProductButton) {
                    startActivity(new Intent(ProductListActivity.this, SignUpActivity.class).putExtra("sign_up_key", "login_for_add_product"));
                }
                else {
                    startActivity(new Intent(ProductListActivity.this, SignUpActivity.class).putExtra("sign_up_key", "login_for_my_account"));
                }
                alertDialog.dismiss();
            }
        });
        phoneVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProductListActivity.this, "phone", Toast.LENGTH_SHORT).show();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAddProductButton) {
                    startActivity(new Intent(ProductListActivity.this, LoginActivity.class).putExtra("login_key", "login_for_add_product"));
                }
                else {
                    startActivity(new Intent(ProductListActivity.this, LoginActivity.class).putExtra("login_key", "login_for_my_account"));
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}


package com.example.zakaria.myproducts;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;

public class PasswordChangeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextView errorMsg, passChangeHeaderTV, passChangeSubTV;
    private EditText newPassword, confirmPassword, emailAddress;
    private Button changePasswordBtn;
    private String passIntentValue;

    private String newPass, confirmPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        errorMsg = findViewById(R.id.errorMsgId);
        passChangeHeaderTV = findViewById(R.id.passChangeHeaderTV);
        passChangeSubTV = findViewById(R.id.passChangeSubTV);
        changePasswordBtn = findViewById(R.id.changePasswordBtn);
        emailAddress = findViewById(R.id.emailAddress);
        newPassword = findViewById(R.id.newPass);
        confirmPassword = findViewById(R.id.confirmPass);

        firebaseAuth = FirebaseAuth.getInstance();

        passIntentValue = getIntent().getStringExtra("password_key");
        if (passIntentValue.equals("forgot_password")) {
            forgotPasswordEmailVerification();
        } else if (passIntentValue.equals("change_password")) {
            changePassword();
        }
    }

    public void changePassword() {
        setTitle("Change Password");
        passChangeHeaderTV.setText("Change Your Password");
        passChangeSubTV.setText("To change your password, enter a new password, confirm password and press the change password button");
        newPassword.setVisibility(View.VISIBLE);
        confirmPassword.setVisibility(View.VISIBLE);
        changePasswordBtn.setText("Change Password");

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newPass = newPassword.getText().toString().trim();
                confirmPass = confirmPassword.getText().toString().trim();
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                if (newPass.length() <= 0) {
                    newPassword.setError("New password can not be empty or white space");
                }
                if (confirmPass.trim().length() <= 0) {
                    confirmPassword.setError("Confirm password can not be empty or white space");
                } else if (newPass.length() > 0 && confirmPass.length() > 0) {

                    if (newPass.equals(confirmPass)) {
                        user.updatePassword(confirmPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.e("pass", "success");
                                } else {
                                    if (task.getException() instanceof FirebaseAuthRecentLoginRequiredException) {
                                        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), confirmPass);
                                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.e("reAuth", "User re-authenticated.");
                                                } else {
                                                    Log.e("reAuth", "fail");
                                                    errorMsg.setVisibility(View.VISIBLE);
                                                    errorMsg.setText(task.getException().getMessage());
                                                    errorMsg.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            errorMsg.setVisibility(View.GONE);
                                                        }
                                                    }, 8000);
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("pass", "fail");
                                        errorMsg.setVisibility(View.VISIBLE);
                                        errorMsg.setText(task.getException().getMessage());
                                        errorMsg.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                errorMsg.setVisibility(View.GONE);
                                            }
                                        }, 8000);
                                    }
                                }
                            }
                        });
                    } else {
                        errorMsg.setVisibility(View.VISIBLE);
                        errorMsg.setText("password not matched");
                        errorMsg.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                errorMsg.setVisibility(View.GONE);
                            }
                        }, 8000);
                    }
                }
            }
        });
    }

    public void forgotPasswordEmailVerification() {
        setTitle("Reset Password");
        passChangeHeaderTV.setText("Forgot your password?");
        passChangeSubTV.setText("To reset your password, enter your email, press the button and check mail to follow instruction");
        emailAddress.setVisibility(View.VISIBLE);
        changePasswordBtn.setText("Reset Password");

        changePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailAddress.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    errorMsg.setVisibility(View.VISIBLE);
                    errorMsg.setText("Email field can not be empty");
                    errorMsg.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            errorMsg.setVisibility(View.GONE);
                        }
                    }, 8000);
                } else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                errorMsg.setVisibility(View.VISIBLE);
                                errorMsg.setTextColor(Color.BLACK);
                                errorMsg.setText("Check your email to reset your password!");
                                errorMsg.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        errorMsg.setVisibility(View.GONE);
                                        startActivity(new Intent(PasswordChangeActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                }, 8000);
                                Log.e("sent_email", "yes");
                            } else {
                                errorMsg.setVisibility(View.VISIBLE);
                                errorMsg.setText("Fail to send reset password email!");
                                errorMsg.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        errorMsg.setVisibility(View.GONE);
                                    }
                                }, 8000);
                                Log.e("sent_email", "no");
                            }
                        }
                    });
                }
            }
        });
    }
}


package com.example.zakaria.myproducts;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zakaria.myproducts.models.MobileProduct;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

public class PostAdActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private static final int PIC_IMAGE_REQUEST = 1;

    private TextView productType, conditionTV, deviceTpTV, transmissionTV, newNumberTV, addAnotherNmbTV, addNumbTV, nameTV, emailTV, landTypeTV, landUnitsTV, houseUnitsTV;

    private EditText userNameET, locationET, brandET, modelET, fuelTpET, engineET, kilometersET, modelYrET, titleET, descriptionET, priceET, anotherNumbET, bedsET, bathsET, landsizeET, sizeET, housesizeET, productNameET;

    private Button addPhotoBtn, addPostBtn;

    private TextInputLayout brandEtLayout, modelEtLayout, fuelTpEtLayout, engineEtLayout, kilometersEtLayout, modelYrEtLayout, titleEtLayout, anotherNumbEtLayout, bedsEtLayout, bathsEtLayout, landsizeEtLayout, sizeEtLayout, housesizeEtLayout, productNameEtLayout;

    private RadioGroup conditionRG, deviceTpRG, transmissionRG;
    private RadioButton reconRB;
    private RadioButton radioButton;

    private ImageView imageProductView;
    private Spinner landUitsSP, landTypeSP, houseUitsSP;
    private CheckBox negotiableCB;
    private String checkItem;
    private String selectedCategory;
    private ProgressBar progressBar;

    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);

        progressBar = findViewById(R.id.progressBar);
        addPhotoBtn = findViewById(R.id.addPhotoBtn);
        addPostBtn = findViewById(R.id.addPostBtn);
        productType = findViewById(R.id.productType);
        conditionTV = findViewById(R.id.conditionTV);
        deviceTpTV = findViewById(R.id.deviceTpTV);
        transmissionTV = findViewById(R.id.transmissionTV);
        landTypeTV = findViewById(R.id.landTypeTV);
        landUnitsTV = findViewById(R.id.landUnitsTV);
        houseUnitsTV = findViewById(R.id.houseUnitsTV);
        userNameET = findViewById(R.id.userNameET);
        productNameET = findViewById(R.id.productNameET);
        locationET = findViewById(R.id.locationET);
        brandET = findViewById(R.id.brandET);
        modelET = findViewById(R.id.modelET);
        fuelTpET = findViewById(R.id.fuelTpET);
        engineET = findViewById(R.id.engineET);
        kilometersET = findViewById(R.id.kilometersET);
        modelYrET = findViewById(R.id.modelYrET);
        titleET = findViewById(R.id.titleET);
        descriptionET = findViewById(R.id.descriptionET);
        priceET = findViewById(R.id.priceET);
        anotherNumbET = findViewById(R.id.anotherNumbET);
        bedsET = findViewById(R.id.bedsET);
        bathsET = findViewById(R.id.bathsET);
        landsizeET = findViewById(R.id.landsizeET);
        sizeET = findViewById(R.id.sizeET);
        housesizeET = findViewById(R.id.housesizeET);
        imageProductView = findViewById(R.id.imageProdctId);
        conditionRG = findViewById(R.id.conditionRG);
        deviceTpRG = findViewById(R.id.deviceTpRG);
        transmissionRG = findViewById(R.id.transmissionRG);
        reconRB = findViewById(R.id.recommRB);
        negotiableCB = findViewById(R.id.negotiableCB);
        landUitsSP = findViewById(R.id.landUitsSP);
        landTypeSP = findViewById(R.id.landTypeSP);
        houseUitsSP = findViewById(R.id.houseUitsSP);

        brandEtLayout = findViewById(R.id.brandEtLayout);
        modelEtLayout = findViewById(R.id.modelEtLayout);
        fuelTpEtLayout = findViewById(R.id.fuelTpEtLayout);
        engineEtLayout = findViewById(R.id.engineEtLayout);
        kilometersEtLayout = findViewById(R.id.kilometersEtLayout);
        modelYrEtLayout = findViewById(R.id.modelYrEtLayout);
        titleEtLayout = findViewById(R.id.titleEtLayout);
        anotherNumbEtLayout = findViewById(R.id.anotherNumbEtLayout);
        bedsEtLayout = findViewById(R.id.bedsEtLayout);
        bathsEtLayout = findViewById(R.id.bathsEtLayout);
        landsizeEtLayout = findViewById(R.id.landsizeEtLayout);
        sizeEtLayout = findViewById(R.id.sizeEtLayout);
        housesizeEtLayout = findViewById(R.id.housesizeEtLayout);
        //productNameEtLayout = findViewById(R.id.productNameEtLayout);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("product_image");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("product_list");

        checkItem = getIntent().getStringExtra("item");
        productType.setText(checkItem);

        if (checkItem.equals("Mobile Phones")) {
            conditionTV.setVisibility(View.VISIBLE);
            conditionRG.setVisibility(View.VISIBLE);
            brandEtLayout.setVisibility(View.VISIBLE);
            modelEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Mobile Phone Accessories")) {
            conditionTV.setVisibility(View.VISIBLE);
            conditionRG.setVisibility(View.VISIBLE);
            titleEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Computers & Tablets")) {
            conditionTV.setVisibility(View.VISIBLE);
            conditionRG.setVisibility(View.VISIBLE);
            deviceTpTV.setVisibility(View.VISIBLE);
            deviceTpRG.setVisibility(View.VISIBLE);
            brandEtLayout.setVisibility(View.VISIBLE);
            modelEtLayout.setVisibility(View.VISIBLE);
            titleEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("TVs")) {
            conditionTV.setVisibility(View.VISIBLE);
            conditionRG.setVisibility(View.VISIBLE);
            brandEtLayout.setVisibility(View.VISIBLE);
            modelEtLayout.setVisibility(View.VISIBLE);
            titleEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Motorbikes & Scooters")) {
            conditionTV.setVisibility(View.VISIBLE);
            conditionRG.setVisibility(View.VISIBLE);
            brandEtLayout.setVisibility(View.VISIBLE);
            modelEtLayout.setVisibility(View.VISIBLE);
            modelYrEtLayout.setVisibility(View.VISIBLE);
            engineEtLayout.setVisibility(View.VISIBLE);
            kilometersEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Cars")) {
            conditionTV.setVisibility(View.VISIBLE);
            conditionRG.setVisibility(View.VISIBLE);
            reconRB.setVisibility(View.VISIBLE);
            brandEtLayout.setVisibility(View.VISIBLE);
            modelEtLayout.setVisibility(View.VISIBLE);
            modelYrEtLayout.setVisibility(View.VISIBLE);
            transmissionTV.setVisibility(View.VISIBLE);
            transmissionRG.setVisibility(View.VISIBLE);
            fuelTpEtLayout.setVisibility(View.VISIBLE);
            engineEtLayout.setVisibility(View.VISIBLE);
            kilometersEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Trucks, Vans & Buses")) {
            conditionTV.setVisibility(View.VISIBLE);
            conditionRG.setVisibility(View.VISIBLE);
            reconRB.setVisibility(View.VISIBLE);
            brandEtLayout.setVisibility(View.VISIBLE);
            modelEtLayout.setVisibility(View.VISIBLE);
            modelYrEtLayout.setVisibility(View.VISIBLE);
            kilometersEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Apartment & Flats")) {
            bedsEtLayout.setVisibility(View.VISIBLE);
            bathsEtLayout.setVisibility(View.VISIBLE);
            sizeEtLayout.setVisibility(View.VISIBLE);
            titleEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Houses")) {
            bedsEtLayout.setVisibility(View.VISIBLE);
            bathsEtLayout.setVisibility(View.VISIBLE);
            landsizeEtLayout.setVisibility(View.VISIBLE);
            landUnitsTV.setVisibility(View.VISIBLE);
            landUitsSP.setVisibility(View.VISIBLE);
            housesizeEtLayout.setVisibility(View.VISIBLE);
            houseUnitsTV.setVisibility(View.VISIBLE);
            houseUitsSP.setVisibility(View.VISIBLE);
            titleEtLayout.setVisibility(View.VISIBLE);
        }

        if (checkItem.equals("Plots & Land")) {
            landTypeTV.setVisibility(View.VISIBLE);
            landTypeSP.setVisibility(View.VISIBLE);
            landsizeET.setVisibility(View.VISIBLE);
            landUnitsTV.setVisibility(View.VISIBLE);
            landUitsSP.setVisibility(View.VISIBLE);
            titleEtLayout.setVisibility(View.VISIBLE);
        }

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this, R.array.land_type, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        landTypeSP.setAdapter(typeAdapter);
        landTypeSP.setSelection(2);
        landTypeSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(PostAdActivity.this, "" + adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> HouseUnitsAdapter = ArrayAdapter.createFromResource(this, R.array.units, android.R.layout.simple_spinner_item);
        HouseUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        houseUitsSP.setAdapter(HouseUnitsAdapter);
        houseUitsSP.setSelection(2);
        houseUitsSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(PostAdActivity.this, "" + adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<CharSequence> LandUnitsAdapter = ArrayAdapter.createFromResource(this, R.array.units, android.R.layout.simple_spinner_item);
        LandUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        landUitsSP.setAdapter(LandUnitsAdapter);
        landUitsSP.setSelection(2);
        landUitsSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(PostAdActivity.this, "" + adapterView.getItemAtPosition(i).toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        addPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFIleChooser();
            }
        });

        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(PostAdActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    uploadProduct();
                }
            }
        });
    }

    public void addNewNumb(View view) {
        newNumberTV.setVisibility(View.VISIBLE);
        newNumberTV.setText(anotherNumbET.getText().toString());
        addAnotherNmbTV.setVisibility(View.GONE);
        anotherNumbET.setVisibility(View.GONE);
        addNumbTV.setVisibility(View.GONE);
    }

    public void postAd(View view) {
    }

    public void addAnotherNmb(View view) {
        anotherNumbEtLayout.setVisibility(View.VISIBLE);
        addNumbTV.setVisibility(View.VISIBLE);
    }

    private void openFIleChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PIC_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PIC_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null && data != null) {
            mImageUri = data.getData();

            Glide.with(this).
                    load(mImageUri)
                    .override(300, 800)
                    .into(imageProductView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mine = MimeTypeMap.getSingleton();
        return mine.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadProduct() {
            if (checkItem.equals("Mobile Phones")) {
                selectedCategory = "Mobile Phones";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("Mobile Phone Accessories")) {
                selectedCategory = "Mobile Phone Accessories";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("Computers & Tablets")) {
                selectedCategory = "Computers & Tablets";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("TVs")) {
                selectedCategory = "TVs";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("Motorbikes & Scooters")) {
                selectedCategory = "Motorbikes & Scooters";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("Cars")) {
                selectedCategory = "Cars";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("Trucks, Vans & Buses")) {
                selectedCategory = "Trucks, Vans & Buses";
            }

            if (checkItem.equals("Apartment & Flats")) {
                selectedCategory = "Apartment & Flats";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("Houses")) {
                selectedCategory = "Houses";
                uploadSelectedCategoryWise(selectedCategory);
            }

            if (checkItem.equals("Plots & Land")) {
                selectedCategory = "Plots & Land";
                uploadSelectedCategoryWise(selectedCategory);
            }
    }

    public String getCurrentDateAndTime() {
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm a");
        Date date = new Date();
        return String.valueOf(dateFormat.format(date));
    }

    private String getCondition() {
        int seletedId = conditionRG.getCheckedRadioButtonId();
        radioButton = findViewById(seletedId);
        return radioButton.getText().toString();
    }

    public void uploadSelectedCategoryWise(final String category) {
        Toast.makeText(this, "up click", Toast.LENGTH_SHORT).show();
        Log.e("up_tag", "up_click");

        if (mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+"."+getFileExtension(mImageUri));

            fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(0);
                        }
                    }, 4000);
                    Toast.makeText(PostAdActivity.this, "Upload Successful img", Toast.LENGTH_SHORT).show();

                    String price;
                    String userName = userNameET.getText().toString();
                    String productName = productNameET.getText().toString();
                    String location = locationET.getText().toString();
                    String productCondition = getCondition();
                    String brand = brandET.getText().toString();
                    String model = modelET.getText().toString();
                    String description = descriptionET.getText().toString();
                    String posted = getCurrentDateAndTime();
                    String phoneNumbe = anotherNumbET.getText().toString();
                    String imageUrl = taskSnapshot.getDownloadUrl().toString();

                    if (negotiableCB.isChecked()) {
                        price = "\u09F3 " + priceET.getText().toString() + " (" + negotiableCB.getText().toString() + ")";
                    } else {
                        price = "\u09F3 " + priceET.getText().toString();
                    }

                    String upLoadId = mDatabaseRef.push().getKey();
                    String getUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    MobileProduct mobileProduct = new MobileProduct(userName, productName, location, productCondition, brand, model, category, description, posted, phoneNumbe, price, imageUrl, upLoadId);

                    mDatabaseRef.child(getUserId).child(upLoadId).setValue(mobileProduct);
                    Toast.makeText(PostAdActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PostAdActivity.this, ProductListActivity.class));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostAdActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                    progressBar.isShown();
                }
            });
        }
        else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_LONG).show();
        }
    }
}


package com.example.zakaria.myproducts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.zakaria.myproducts.models.Others;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;

public class ProductAddActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_add);

        firebaseAuth = FirebaseAuth.getInstance();
    }


    public void electItem(View view) {
        arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList("Mobile Phones", "Mobile Phone Accessories", "Computers & Tablets", "TVs"));
        Intent intent = new Intent(this, ItemsActivity.class);
        intent.putExtra("type", "Electronics");
        intent.putStringArrayListExtra("items", arrayList);
        startActivity(intent);
    }

    public void carItem(View view) {
        arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList("Motorbikes & Scooters", "Cars", "Trucks, Vans & Buses"));
        Intent intent = new Intent(this, ItemsActivity.class);
        intent.putExtra("type", "Cars & Vehicles");
        intent.putStringArrayListExtra("items", arrayList);
        startActivity(intent);
    }

    public void propertyItem(View view) {
        arrayList = new ArrayList<>();
        arrayList.addAll(Arrays.asList("Apartment & Flats", "Houses", "Plots & Land"));
        Intent intent = new Intent(this, ItemsActivity.class);
        intent.putExtra("type", "Property");
        intent.putStringArrayListExtra("items", arrayList);
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        if (firebaseAuth.getCurrentUser() == null) {
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.myAccount).setVisible(false);
            menu.findItem(R.id.settings).setVisible(false);

            menu.findItem(R.id.accountIcon).setIcon(R.drawable.ic_account_circle_black_24dp);
            menu.findItem(R.id.accountIcon).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    return true;
                }
            });
        } else {
            menu.findItem(R.id.logout).setVisible(true);
            menu.findItem(R.id.myAccount).setVisible(true);

            menu.findItem(R.id.accountIcon).setIcon(R.drawable.user);
            menu.findItem(R.id.myAccount).setIcon(R.drawable.user);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.myAccount) {
            Toast.makeText(this, "my", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.logout) {
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseAuth.signOut();
                Toast.makeText(this, "Log out successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProductAddActivity.this, ProductListActivity.class));
                finish();
            }
        } else if (id == R.id.shareApp) {
            Toast.makeText(this, "share", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.faq) {
            startActivity(new Intent(ProductAddActivity.this, FaqActivity.class));
        } else if (id == R.id.about) {
            Others others = new Others(this);
            others.aboutUs();
        } else if (id == R.id.send) {
            startActivity(new Intent(this, EmailActivity.class));
        } else if (id == R.id.updateProfile) {
            startActivity(new Intent(ProductAddActivity.this, UpdateProfileActivity.class));
        } else if (id == R.id.changePassword) {
            startActivity(new Intent(ProductAddActivity.this, PasswordChangeActivity.class).putExtra("password_key", "change_password"));
        }
        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onBackPressed() {
        startActivity(new Intent(ProductAddActivity.this, ProductListActivity.class));
        finish();
        super.onBackPressed();
    }*/
}


package com.example.zakaria.myproducts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaqActivity extends AppCompatActivity {

    private ExpandableListView faqExpandableLitView;
    private List<String> listHeaderData;
    private Map<String, List<String>> listChildData;
    private Map<String, List<String>> listTestData;
    private CustomExpandableAdapter customExpandableAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        faqExpandableLitView = findViewById(R.id.faqExpandableLitViewId);
        prepareListData();

        customExpandableAdapter = new CustomExpandableAdapter(this, listHeaderData, listChildData);
        faqExpandableLitView.setAdapter(customExpandableAdapter);
    }

    public void prepareListData() {
        List<String> child;

        String[] faqHeaderString = getResources().getStringArray(R.array.faq_header);
        String[] faqChildString = getResources().getStringArray(R.array.faq_child);

        listHeaderData = new ArrayList<>();
        listChildData = new HashMap<>();

        for (int i=0; i<faqHeaderString.length; i++) {
            listHeaderData.add(faqHeaderString[i]);

            child = new ArrayList<>();
            child.add(faqChildString[i]);

            listChildData.put(listHeaderData.get(i), child);
        }
    }
}


package com.example.zakaria.myproducts;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zakaria.myproducts.models.UpdateProfile;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class UpdateProfileActivity extends AppCompatActivity {

    private Uri downloadUrl;
    private StorageReference storageReference;
    private static final int PIC_SELECT = 1;
    private TextInputLayout nameTextInputLayout, phoneNumberTextInputLayout, locationTextInputLayout;
    private EditText nameUpdateET, phoneUpdateET, locationUpdateET;
    private Button updateBtn, changeImgBtn;
    private TextView updateTV;
    private ImageView profileImageView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    //private MenuItem editItem;
    private static String userName;
    private static String phoneNumber;
    private static String location;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        profileImageView = findViewById(R.id.profileImageView);
        nameUpdateET = findViewById(R.id.nameUpdateET);
        phoneUpdateET = findViewById(R.id.phoneUpdateET);
        locationUpdateET = findViewById(R.id.locationUpdateET);
        updateBtn = findViewById(R.id.updateBtnId);
        changeImgBtn = findViewById(R.id.changeImgBtn);
        updateTV = findViewById(R.id.myProfileTV);
        nameTextInputLayout = findViewById(R.id.nameTextInputLayout);
        phoneNumberTextInputLayout = findViewById(R.id.phoneNumberTextInputLayout);
        locationTextInputLayout = findViewById(R.id.locationTextInputLayout);

        //tv = findViewById(R.id.tv);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        //getProfileImage();
        getCurrentUserInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_profile_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.updateProIconId) {
            nameUpdateET.setEnabled(true);
            phoneUpdateET.setEnabled(true);
            locationUpdateET.setEnabled(true);
            updateTV.setText("Update My Profile");
            changeImgBtn.setVisibility(View.VISIBLE);
            updateBtn.setVisibility(View.VISIBLE);

            nameTextInputLayout.setHintEnabled(true);
            phoneNumberTextInputLayout.setHintEnabled(true);
            locationTextInputLayout.setHintEnabled(true);
            //editItem.setVisible(false);
        }
        return super.onOptionsItemSelected(item);
    }

    public void changeImgBtn(View view) {
//        Others others = new Others(this);
//        others.changeProfileImg();

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PIC_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uriFile = data.getData();
        String filePath = uriFile.getPath();
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_image").child(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        UploadTask uploadTask = storageReference.putFile(uriFile);

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUrl = task.getResult();
                }
                else {
                    Toast.makeText(UpdateProfileActivity.this, "Profile image is not updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateProfileBtn(View view) {
        try {
            DatabaseReference databaseReference = firebaseDatabase.getReference("user_profile");
            UpdateProfile updateProfile = new UpdateProfile(nameUpdateET.getText().toString(), phoneUpdateET.getText().toString(), locationUpdateET.getText().toString());

            userName = nameUpdateET.getText().toString();
            phoneNumber = phoneUpdateET.getText().toString();
            location = locationUpdateET.getText().toString();

            databaseReference.child(firebaseAuth.getCurrentUser().getUid()).setValue(updateProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.e("data", "success");
                        getCurrentUserInfo();
                    } else {
                        Log.e("data", "failed");
                    }
                }
            });
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void getCurrentUserInfo() {
            final DatabaseReference databaseReference = firebaseDatabase.getReference("user_profile").child(firebaseAuth.getCurrentUser().getUid());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            UpdateProfile updateProfile = dataSnapshot.getValue(UpdateProfile.class);
                            ArrayList<String> updatedDataList = new ArrayList<>();

                            updatedDataList.add(updateProfile.getUserName());
                            updatedDataList.add(updateProfile.getPhoneNumber());
                            updatedDataList.add(updateProfile.getLocation());

                            nameUpdateET.setText(updatedDataList.get(0));
                            phoneUpdateET.setText(updatedDataList.get(1));
                            locationUpdateET.setText(updatedDataList.get(2));
                        }
                        else {
                            Toast.makeText(UpdateProfileActivity.this, "No data found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UpdateProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            //editItem.setVisible(true);
            nameUpdateET.setEnabled(false);
            phoneUpdateET.setEnabled(false);
            locationUpdateET.setEnabled(false);
            updateTV.setText("My Profile");
            changeImgBtn.setVisibility(View.GONE);
            updateBtn.setVisibility(View.GONE);
    }

    public static String getUserName() {
        return userName;
    }
}


package com.example.zakaria.myproducts;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zakaria.myproducts.models.MobileProduct;
import com.example.zakaria.myproducts.models.Product;

import java.util.ArrayList;
import java.util.List;

public class AllProductAdapter extends RecyclerView.Adapter<AllProductAdapter.AllProductViewHolder> {

    Context context;
    List<MobileProduct> productList;

    public AllProductAdapter(Context context, List<MobileProduct> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public AllProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_product_layout, parent,false);
        return new AllProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllProductViewHolder holder, final int position) {
        final MobileProduct mobileProduct = productList.get(position);
        holder.companyName.setText(mobileProduct.getUserName());
        holder.productName.setText(mobileProduct.getProductName());
        holder.price.setText(mobileProduct.getPrice());
        Glide.with(context).
                load(mobileProduct.getImageUrl())
                .centerCrop()
                .override(130, 120)
                .into(holder.imageProdct);

        holder.imageProdct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ProductDetailsActivity.class);
                intent.putExtra("company_name", productList.get(position).getUserName());
                intent.putExtra("product_name", productList.get(position).getProductName());
                intent.putExtra("product_model", productList.get(position).getModel());
                intent.putExtra("product_brand", productList.get(position).getBrand());
                intent.putExtra("product_category", productList.get(position).getCategory());
                intent.putExtra("product_condition", productList.get(position).getCondition());
                intent.putExtra("product_location", productList.get(position).getLocation());
                intent.putExtra("product_posted_time", productList.get(position).getPosted());
                intent.putExtra("product_price", productList.get(position).getPrice());
                intent.putExtra("product_phone_number", productList.get(position).getPrice());
                intent.putExtra("product_image_url", mobileProduct.getImageUrl());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class AllProductViewHolder extends RecyclerView.ViewHolder{
        TextView companyName;
        TextView productName;
        TextView price;
        ImageView imageProdct;

        public AllProductViewHolder(View itemView) {
            super(itemView);
            companyName=itemView.findViewById(R.id.companyName);
            productName=itemView.findViewById(R.id.productName);
            price=itemView.findViewById(R.id.price);
            imageProdct=itemView.findViewById(R.id.imageProdct);
        }
    }
}


package com.example.zakaria.myproducts;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText emailLoginET, passwordLoginET;
    private TextView errorMsg;
    private String intentValue;
    private AppCompatCheckBox passwordShowCheckBox;
    //private TextView forgotTV, signUpTV, continueWithPhoneTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        errorMsg = findViewById(R.id.errorMsgId);
        emailLoginET = findViewById(R.id.emailLoginET);
        passwordLoginET = findViewById(R.id.passwordLoginET);
        passwordShowCheckBox = findViewById(R.id.passwordShowCheckBox);
        /*forgotTV = findViewById(R.id.forgotPassId);
        signUpTV = findViewById(R.id.signUpId);
        continueWithPhoneTV = findViewById(R.id.continueWithPhoneId);*/

        getPasswordShowHide();

        firebaseAuth = FirebaseAuth.getInstance();
        intentValue = getIntent().getStringExtra("login_key");
    }

    public void continueWithPhone(View view) {
        Toast.makeText(this, "ph", Toast.LENGTH_SHORT).show();
    }

    public void signUpTv(View view) {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
    }

    public void forgotPasswordTv(View view) {
        startActivity(new Intent(LoginActivity.this, PasswordChangeActivity.class).putExtra("password_key", "forgot_password"));
    }

    public void loginBtn(View view) {
        String email = emailLoginET.getText().toString().trim();
        String password = passwordLoginET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailLoginET.setError("Empty Email Address");
            emailLoginET.setText(null);
        }
        if (TextUtils.isEmpty(password)) {
            passwordLoginET.setError("Empty Password");
            emailLoginET.setText(null);
        }
        else if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.e("tag_login", "success");
                        if (intentValue.equals("login_for_add_product")) {
                            startActivity(new Intent(LoginActivity.this, ProductAddActivity.class));
                            finish();
                        }
                        else if (intentValue.equals("login_for_my_account")) {
                            startActivity(new Intent(LoginActivity.this, ProductListActivity.class));
                            finish();
                        }
                    }
                    else {
                        /*Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();*/
                        errorMsg.setVisibility(View.VISIBLE);
                        errorMsg.setText(task.getException().getMessage());
                        errorMsg.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                errorMsg.setVisibility(View.GONE);
                            }
                        }, 8000);
                        Log.w("tag_login", "failed", task.getException());
                    }
                }
            });
        }
    }

    public void getPasswordShowHide() {
        passwordShowCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    //show password
                    passwordLoginET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else {
                    //hide password
                    passwordLoginET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }
}


package com.example.zakaria.myproducts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;

public class ProductDetailsActivity extends AppCompatActivity {

    private TextView companyNameTextView, productNameTextView, priceTextView, modelTextView, brandTextView, categoryTextView, locationTextView, conditionTextView, postedTextView;
    private ImageView productImageDetails;
    private Button callNowBtn;
    private boolean isFavourite;
    private String number;
    private static final int REQUEST_CODE_FOR_PHONE_CALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        companyNameTextView = findViewById(R.id.companyNameDetails);
        productNameTextView = findViewById(R.id.productNameDetails);
        priceTextView = findViewById(R.id.priceDetails);
        modelTextView = findViewById(R.id.modelTV);
        brandTextView = findViewById(R.id.brandTV);
        categoryTextView = findViewById(R.id.categoryTV);
        locationTextView = findViewById(R.id.locationTV);
        conditionTextView = findViewById(R.id.conditionsTV);
        postedTextView = findViewById(R.id.postedTV);
        callNowBtn = findViewById(R.id.callNowBtn);
        productImageDetails = findViewById(R.id.productImageDetails);

        String companyName = getIntent().getStringExtra("company_name");
        String productName = getIntent().getStringExtra("product_name");
        String model = getIntent().getStringExtra("product_model");
        String brand = getIntent().getStringExtra("product_brand");
        String category = getIntent().getStringExtra("product_category");
        String condition = getIntent().getStringExtra("product_condition");
        String location = getIntent().getStringExtra("product_location");
        String posted_time = getIntent().getStringExtra("product_posted_time");
        String price = getIntent().getStringExtra("product_price");
        String phoneNumber = getIntent().getStringExtra("product_phone_number");
        String imageUrl = getIntent().getStringExtra("product_image_url");

        companyNameTextView.setText(companyName);
        priceTextView.setText(price);
        productNameTextView.setText(productName);
        modelTextView.setText(model);
        brandTextView.setText(brand);
        categoryTextView.setText(category);
        conditionTextView.setText(condition);
        locationTextView.setText(location);
        postedTextView.setText(posted_time);
        Glide.with(this).
                load(imageUrl)
                .override(300, 800)
                .into(productImageDetails);

        callNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callNow();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.favourite_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.favorite_item) {
            if (isFavourite == false) {
                Toast.makeText(this, "Added to favourite", Toast.LENGTH_SHORT).show();
                item.setIcon(R.drawable.ic_favorite_black_24dp);
                isFavourite = true;
            }
            else {
                item.setIcon(R.drawable.ic_favorite_border_black_24dp);
                isFavourite = false;
            }
        }
        else if (id == R.id.shareApp) {
            shareApp();
        }
        return super.onOptionsItemSelected(item);
    }

    public void shareApp() {
        ApplicationInfo info = getApplicationContext().getApplicationInfo();
        String apkPath = info.sourceDir;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkPath)));
        startActivity(Intent.createChooser(intent, "Share Kenakata.com Using:"));
    }

    public void callNow() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE_FOR_PHONE_CALL);
        } else {
            String dial = "tel:" + number;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_FOR_PHONE_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callNow();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}



package com.example.zakaria.myproducts;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText emailSignUpET, passwordSignUpET;
    private TextView errorMsg;
    private String intentValue;
    private AppCompatCheckBox passwordShowCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        errorMsg = findViewById(R.id.errorMsgId);
        emailSignUpET = findViewById(R.id.emailSignUpET);
        passwordSignUpET = findViewById(R.id.passwordSignUpET);
        passwordShowCheckBox = findViewById(R.id.passwordShowCheckBox);

        getPasswordShowHide();

        firebaseAuth = FirebaseAuth.getInstance();
        intentValue = getIntent().getStringExtra("login_key");
    }

    public void signUpBtn(View view) {
        String email = emailSignUpET.getText().toString().trim();
        String password = passwordSignUpET.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailSignUpET.setError("Empty Email Address");
            emailSignUpET.setText(null);
        }
        if (TextUtils.isEmpty(password)) {
            passwordSignUpET.setError("Empty Password");
            emailSignUpET.setText(null);
        }
        else if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Log.e("tag_login", "success");
                        if (intentValue.equals("login_for_add_product")) {
                            startActivity(new Intent(SignUpActivity.this, ProductAddActivity.class));
                            finish();
                        }
                        else if (intentValue.equals("login_for_my_account")) {
                            startActivity(new Intent(SignUpActivity.this, ProductListActivity.class));
                            finish();
                        }
                    }
                    else {
                       /* Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();*/
                        errorMsg.setVisibility(View.VISIBLE);
                        errorMsg.setText(task.getException().getMessage());
                        errorMsg.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                errorMsg.setVisibility(View.GONE);
                            }
                        }, 8000);
                        Log.w("tag_sign_up", "failed", task.getException());
                    }
                }
            });
        }
    }

    public void loginTv(View view) {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
    }

    public void continueWithPhone(View view) {
        Toast.makeText(this, "ph", Toast.LENGTH_SHORT).show();
    }

    public void getPasswordShowHide() {
        passwordShowCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    //show password
                    passwordSignUpET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                else {
                    //hide password
                    passwordSignUpET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }
}


package com.example.zakaria.myproducts;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

class CustomExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listHeaderData;
    private Map<String, List<String>> listChildData;

    public CustomExpandableAdapter(Context context, List<String> listHeaderData, Map<String, List<String>> listChildData) {
        this.context = context;
        this.listHeaderData = listHeaderData;
        this.listChildData = listChildData;
    }

    @Override
    public int getGroupCount() {
        return listHeaderData.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listChildData.get(listHeaderData.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return listHeaderData.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listChildData.get(listHeaderData.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean isExpand, View view, ViewGroup viewGroup) {
        String headerString = (String) getGroup(i);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.header_layout, null);
        }
        TextView headerTextView = view.findViewById(R.id.groupTextViewId);
        headerTextView.setText(headerString);

        if (isExpand) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                headerTextView.setTextColor(context.getColor(R.color.colorExpand));
            }
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                headerTextView.setTextColor(context.getColor(android.R.color.primary_text_light));
            }
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String childString = (String) getChild(i, i1);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.child_layout, null);
        }
        TextView headerTextView = view.findViewById(R.id.childTextViewId);
        headerTextView.setText(childString);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}


package com.example.zakaria.myproducts.models;

public class CarProduct {

    private String name;
    private String location;
    private String condition;
    private String brand;
    private String model;
    private String transmission;
    private String bodyType;
    private String fuelType;
    private String registerYear;
    private String engineCapacity;
    private String category;
    private String kilometersRun;
    private String description;
    private String posted;
    private String phoneNumber;
    private String price;

    public CarProduct() {

    }

    public CarProduct(String name, String location, String condition, String brand, String model, String transmission, String bodyType, String fuelType, String registerYear, String engineCapacity, String category, String kilometersRun, String description, String posted, String phoneNumber, String price) {
        this.name = name;
        this.location = location;
        this.condition = condition;
        this.brand = brand;
        this.model = model;
        this.transmission = transmission;
        this.bodyType = bodyType;
        this.fuelType = fuelType;
        this.registerYear = registerYear;
        this.engineCapacity = engineCapacity;
        this.category = category;
        this.kilometersRun = kilometersRun;
        this.description = description;
        this.posted = posted;
        this.phoneNumber = phoneNumber;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getRegisterYear() {
        return registerYear;
    }

    public void setRegisterYear(String registerYear) {
        this.registerYear = registerYear;
    }

    public String getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(String engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKilometersRun() {
        return kilometersRun;
    }

    public void setKilometersRun(String kilometersRun) {
        this.kilometersRun = kilometersRun;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}


package com.example.zakaria.myproducts.models;

public class UpdateProfile {

    private String userName;
    private String phoneNumber;
    private String location;

    public UpdateProfile() {

    }

    public UpdateProfile(String userName, String phoneNumber, String location) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.location = location;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}


package com.example.zakaria.myproducts.models;

public class Product {
    String companyName;
    String productName;
    String price;

    public Product() {
    }

    public Product(String companyName, String productName, String price) {
        this.companyName = companyName;
        this.productName = productName;
        this.price = price;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getProductName() {
        return productName;
    }

    public String getPrice() {
        return price;
    }

    public void setCompanyName(String companyName) {

        this.companyName = companyName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}


package com.example.zakaria.myproducts.models;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zakaria.myproducts.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Others extends AppCompatActivity {

    private StorageReference storageReference;
    private static final int PIC_SELECT = 1;
    private Context context;
    private static LinearLayout linearLayout1;
    private static LinearLayout linearLayout2;
    private static LinearLayout linearLayout3;

    public Others(Context context) {
        this.context = context;
    }

    /*
    public static void shareMyApp(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        String apkPath = info.sourceDir;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");

        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(apkPath)));
        context.startActivity(Intent.createChooser(intent, "Share this app using:"));
    }*/

    public void aboutUs() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.about_us_pop_up, null);
        dialogBuilder.setView(view);

         linearLayout1 = view.findViewById(R.id.layout1);
         linearLayout2 = view.findViewById(R.id.layout2);
         linearLayout3 = view.findViewById(R.id.layout3);


        dialogBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void changeProfileImg() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.camera_pop_up, null);
        builder.setView(view);

        linearLayout1 = view.findViewById(R.id.takePhotoLayout);
        linearLayout2 = view.findViewById(R.id.galleryPhotoLayout);

        linearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "take", Toast.LENGTH_SHORT).show();
            }
        });

        linearLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PIC_SELECT);*/
                Toast.makeText(context, "get", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri uriFile = data.getData();
        String filePath = uriFile.getPath();
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_image").child(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        UploadTask uploadTask = storageReference.putFile(uriFile);

        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult();
                }
                else {
                    Toast.makeText(context, "Profile image is not updated", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/
}


package com.example.zakaria.myproducts.models;

public class TelevisionProduct {

    private String name;
    private String location;
    private String condition;
    private String brand;
    private String model;
    private String category;
    private String description;
    private String posted;
    private String phoneNumber;
    private String price;

    public TelevisionProduct() {

    }

    public TelevisionProduct(String name, String location, String condition, String brand, String model, String category, String description, String posted, String phoneNumber, String price) {
        this.name = name;
        this.location = location;
        this.condition = condition;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.description = description;
        this.posted = posted;
        this.phoneNumber = phoneNumber;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}


package com.example.zakaria.myproducts.models;

public class MobileAccessoriesProduct {

    private String name;
    private String location;
    private String condition;
    private String itemType;
    private String category;
    private String description;
    private String phoneNumber;
    private String price;
    private String posted;

    MobileAccessoriesProduct() {

    }

    public MobileAccessoriesProduct(String name, String location, String condition, String itemType, String category, String description, String phoneNumber, String price, String posted) {
        this.name = name;
        this.location = location;
        this.condition = condition;
        this.itemType = itemType;
        this.category = category;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.price = price;
        this.posted = posted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }
}


package com.example.zakaria.myproducts.models;

public class MobileProduct {

    private String userName;
    private String productName;
    private String location;
    private String condition;
    private String brand;
    private String model;
    private String category;
    private String description;
    private String posted;
    private String phoneNumber;
    private String price;
    private String imageUrl;
    private String uploadId;

    public MobileProduct() {

    }

    public MobileProduct(String userName, String productName, String location, String condition, String brand, String model, String category, String description, String posted, String phoneNumber, String price, String imageUrl, String uploadId) {
        this.userName = userName;
        this.productName = productName;
        this.location = location;
        this.condition = condition;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.description = description;
        this.posted = posted;
        this.phoneNumber = phoneNumber;
        this.price = price;
        this.imageUrl = imageUrl;
        this.uploadId = uploadId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
}

package com.example.zakaria.myproducts.models;

public class BikeAndScooterProduct {

    private String name;
    private String location;
    private String condition;
    private String brand;
    private String model;
    private String modelYear;
    private String engineCapacity;
    private String category;
    private String kilometersRun;
    private String description;
    private String posted;
    private String phoneNumber;
    private String price;

    public BikeAndScooterProduct() {

    }

    public BikeAndScooterProduct(String name, String location, String condition, String brand, String model, String modelYear, String engineCapacity, String category, String kilometersRun, String description, String posted, String phoneNumber, String price) {
        this.name = name;
        this.location = location;
        this.condition = condition;
        this.brand = brand;
        this.model = model;
        this.modelYear = modelYear;
        this.engineCapacity = engineCapacity;
        this.category = category;
        this.kilometersRun = kilometersRun;
        this.description = description;
        this.posted = posted;
        this.phoneNumber = phoneNumber;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getModelYear() {
        return modelYear;
    }

    public void setModelYear(String modelYear) {
        this.modelYear = modelYear;
    }

    public String getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(String engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKilometersRun() {
        return kilometersRun;
    }

    public void setKilometersRun(String kilometersRun) {
        this.kilometersRun = kilometersRun;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}


package com.example.zakaria.myproducts.models;

public class ComputerAndTabletProduct {

    private String name;
    private String location;
    private String condition;
    private String deviceType;
    private String brand;
    private String model;
    private String category;
    private String description;
    private String phoneNumber;
    private String price;
    private String posted;

    public ComputerAndTabletProduct() {

    }

    public ComputerAndTabletProduct(String name, String location, String condition, String deviceType, String brand, String model, String category, String description, String phoneNumber, String price, String posted) {
        this.name = name;
        this.location = location;
        this.condition = condition;
        this.deviceType = deviceType;
        this.brand = brand;
        this.model = model;
        this.category = category;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.price = price;
        this.posted = posted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPosted() {
        return posted;
    }

    public void setPosted(String posted) {
        this.posted = posted;
    }
}


