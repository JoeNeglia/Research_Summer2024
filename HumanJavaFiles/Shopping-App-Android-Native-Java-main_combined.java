package com.example.shoppingapp;

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
        assertEquals("com.example.shoppingapp", appContext.getPackageName());
    }
}

package com.example.shoppingapp;

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

package com.example.shoppingapp;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeAdabter extends RecyclerView.Adapter<HomeAdabter.ProductInHomeViewHolder> {

    ArrayList<Products> products;
    OnRecyclerViewClickListener listener;

    public HomeAdabter(ArrayList<Products> products, OnRecyclerViewClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductInHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custome_card_home,null,false);
        ProductInHomeViewHolder pvh = new ProductInHomeViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductInHomeViewHolder holder, int position) {
        Products p = products.get(position);

        if(p.getImage() != 0){
            holder.img.setImageResource(p.getImage());
        }else{
            holder.img.setImageResource(R.drawable.products);
        }
        holder.name.setText(p.getName());
        holder.price.setText(p.getPrice()+"$");
        holder.rating.setRating(p.getRating());
        if(p.getDiscount()>0){
            holder.priceAfter.setText(p.getPrice()-(p.getPrice()*(p.getDiscount()/100))+"$");
            holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//وضع خط علي السعر القديم
            holder.price.setTextColor(Color.parseColor("#BFBFBF"));
        }else{
            holder.priceAfter.setText("");
            holder.price.setTextColor(Color.parseColor("#000000"));
        }

        holder.img.setTag(position+1); //اوبجكت مخفي لكي اخزن product_id
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductInHomeViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView name,price,priceAfter;
        RatingBar rating;

        public ProductInHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_car_home);
            name = itemView.findViewById(R.id.tv_name_card_home);
            price = itemView.findViewById(R.id.tv_price_card_home);
            rating = itemView.findViewById(R.id.rating_card_home);
            priceAfter = itemView.findViewById(R.id.tv_priceafter_card_home);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = (int)img.getTag();
                    listener.OnItemClick(id);
                }
            });
        }
    }
}


package com.example.shoppingapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ShoppingDatabase extends SQLiteOpenHelper {

    public static final String DB_NAME = "shopping_db";
    public static final int DB_VERSION = 1;

    public static final String TB_FASHION = "fashion";
    public static final String TB_BOOK = "book";
    public static final String TB_BEAUTY = "beauty";
    public static final String TB_ELECTRICS = "electrics";
    public static final String TB_GAME = "game";
    public static final String TB_HOME_COOKER = "homeCooker";
    public static final String TB_LAPTOP = "laptop";
    public static final String TB_MOBILE = "mobile";
    public static final String TB_SPORTS = "sports";
    public static final String TB_CAR_TOOL = "carTools";
    public static final String TB_USERS = "users";
    public static final String TB_PURCHASES = "purchases";
    public static final String TB_PRODUCT_DISCOUNT = "product_discount";

    public static final String TB_CLM_ID = "id";
    public static final String TB_CLM_IMAGE = "image";
    public static final String TB_CLM_NAME = "name";
    public static final String TB_CLM_PRICE = "price";
    public static final String TB_CLM_BRAND = "brand";
    public static final String TB_CLM_PIECES = "pieces";
    public static final String TB_CLM_DESCRIPTION = "description";
    public static final String TB_CLM_DISCOUNT = "discount";
    public static final String TB_CLM_RATING = "rating";
    public static final String TB_CLM_QUANTITY = "quantity";

    public static final String TB_CLM_USER_ID = "user_id";
    public static final String TB_CLM_USER_NAME = "user_name";
    public static final String TB_CLM_USER_FULL_NAME = "full_name";
    public static final String TB_CLM_USER_PASSWORD = "user_password";
    public static final String TB_CLM_USER_EMAIL = "user_email";
    public static final String TB_CLM_USER_PHONE = "user_phone";
    public static final String TB_CLM_USER_IMAGE = "user_image";

    public ShoppingDatabase(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(createTables(TB_FASHION));
        sqLiteDatabase.execSQL(createTables(TB_BOOK));
        sqLiteDatabase.execSQL(createTables(TB_BEAUTY));
        sqLiteDatabase.execSQL(createTables(TB_ELECTRICS));
        sqLiteDatabase.execSQL(createTables(TB_GAME));
        sqLiteDatabase.execSQL(createTables(TB_HOME_COOKER));
        sqLiteDatabase.execSQL(createTables(TB_LAPTOP));
        sqLiteDatabase.execSQL(createTables(TB_MOBILE));
        sqLiteDatabase.execSQL(createTables(TB_SPORTS));
        sqLiteDatabase.execSQL(createTables(TB_CAR_TOOL));
        sqLiteDatabase.execSQL(createTables(TB_PRODUCT_DISCOUNT));

        sqLiteDatabase.execSQL("CREATE TABLE "+TB_USERS+" ("+TB_CLM_USER_ID+" INTEGER PRIMARY KEY AUTOINCREMENT , "+TB_CLM_USER_NAME+" TEXT UNIQUE , "+
                TB_CLM_USER_FULL_NAME+" TEXT , "+TB_CLM_USER_PASSWORD+" TEXT , "+TB_CLM_USER_EMAIL+" TEXT UNIQUE , "+TB_CLM_USER_PHONE+" TEXT , "+TB_CLM_USER_IMAGE+" TEXT );");


        sqLiteDatabase.execSQL("CREATE TABLE "+TB_PURCHASES+" ("+TB_CLM_ID+" INTEGER PRIMARY KEY AUTOINCREMENT , "+TB_CLM_IMAGE+" INTEGER , "+
                TB_CLM_NAME+" TEXT , "+TB_CLM_PRICE+" REAL , "+TB_CLM_BRAND+" TEXT , "+TB_CLM_RATING+" REAL , "+TB_CLM_QUANTITY+" INTEGER );");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public String createTables(String tableName){
        return "CREATE TABLE "+tableName+" ("+TB_CLM_ID+" INTEGER PRIMARY KEY AUTOINCREMENT , "+TB_CLM_IMAGE+" INTEGER , "+
                TB_CLM_NAME+" TEXT , "+TB_CLM_PRICE+" REAL , "+TB_CLM_BRAND+" TEXT , "+TB_CLM_PIECES+" INTEGER , "+
                TB_CLM_DESCRIPTION+" TEXT , "+TB_CLM_DISCOUNT+" REAL) ; ";
    }


    public boolean insertProduct(Products p,String tableName){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TB_CLM_IMAGE,p.getImage());
        values.put(TB_CLM_NAME,p.getName());
        values.put(TB_CLM_PRICE,p.getPrice());
        values.put(TB_CLM_BRAND,p.getBrand());
        values.put(TB_CLM_PIECES,p.getPieces());
        values.put(TB_CLM_DESCRIPTION,p.getDescription());
        values.put(TB_CLM_DISCOUNT,p.getDiscount());

        long res = db.insert(tableName,null,values);
        db.close();
        if(p.getDiscount()>0){
            insertProductDiscount(p);
        }
        return res != -1;
    }

    public boolean insertProductDiscount(Products p){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TB_CLM_IMAGE,p.getImage());
        values.put(TB_CLM_NAME,p.getName());
        values.put(TB_CLM_PRICE,p.getPrice());
        values.put(TB_CLM_BRAND,p.getBrand());
        values.put(TB_CLM_PIECES,p.getPieces());
        values.put(TB_CLM_DESCRIPTION,p.getDescription());
        values.put(TB_CLM_DISCOUNT,p.getDiscount());

        long ress = db.insert(TB_PRODUCT_DISCOUNT,null,values);
        db.close();

        return ress != -1;
    }

    public ArrayList<Products> getAllProducts(String tableName){
        ArrayList<Products> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+tableName+"",null);

        if(cursor.moveToFirst()){
            do{
                @SuppressLint("Range") int image = cursor.getInt(cursor.getColumnIndex(TB_CLM_IMAGE));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(TB_CLM_NAME));
                @SuppressLint("Range") Double price = cursor.getDouble(cursor.getColumnIndex(TB_CLM_PRICE));
                @SuppressLint("Range") String brand = cursor.getString(cursor.getColumnIndex(TB_CLM_BRAND));
                @SuppressLint("Range") int pieces = cursor.getInt(cursor.getColumnIndex(TB_CLM_PIECES));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(TB_CLM_DESCRIPTION));
                @SuppressLint("Range") double discount = cursor.getDouble(cursor.getColumnIndex(TB_CLM_DISCOUNT));

                Products p = new Products(image,name,price,brand,pieces,description,discount);
                products.add(p);
            }while (cursor.moveToNext());
                cursor.close();
        }
        db.close();
        return products;
    }

    public Products getProduct(int product_id,String tableName){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+tableName+" WHERE "+TB_CLM_ID+" =?",new String[]{String.valueOf(product_id)});

        if(cursor.moveToFirst() && cursor != null){
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(TB_CLM_ID));
            @SuppressLint("Range") int image = cursor.getInt(cursor.getColumnIndex(TB_CLM_IMAGE));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(TB_CLM_NAME));
            @SuppressLint("Range") Double price = cursor.getDouble(cursor.getColumnIndex(TB_CLM_PRICE));
            @SuppressLint("Range") String brand = cursor.getString(cursor.getColumnIndex(TB_CLM_BRAND));
            @SuppressLint("Range") int pieces = cursor.getInt(cursor.getColumnIndex(TB_CLM_PIECES));
            @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(TB_CLM_DESCRIPTION));
            @SuppressLint("Range") double discount = cursor.getDouble(cursor.getColumnIndex(TB_CLM_DISCOUNT));

            Products p = new Products(id,image,name,price,brand,pieces,description,discount);
            cursor.close();
            db.close();
            return p;
        }
        return null;
    }

    public ArrayList<Products> getProductForSearch(String nameProduct, String tableName){
        ArrayList<Products> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+tableName+" WHERE "+TB_CLM_NAME+" =?",new String[]{String.valueOf(nameProduct)});

        if(cursor.moveToFirst() && cursor != null){
            do{
                @SuppressLint("Range") int image = cursor.getInt(cursor.getColumnIndex(TB_CLM_IMAGE));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(TB_CLM_NAME));
                @SuppressLint("Range") Double price = cursor.getDouble(cursor.getColumnIndex(TB_CLM_PRICE));
                @SuppressLint("Range") String brand = cursor.getString(cursor.getColumnIndex(TB_CLM_BRAND));
                @SuppressLint("Range") int pieces = cursor.getInt(cursor.getColumnIndex(TB_CLM_PIECES));
                @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(TB_CLM_DESCRIPTION));
                @SuppressLint("Range") double discount = cursor.getDouble(cursor.getColumnIndex(TB_CLM_DISCOUNT));

                Products p = new Products(image,name,price,brand,pieces,description,discount);
                products.add(p);
            }while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return products;
    }

    public boolean deleteProduct(Products products,String tableName){
        SQLiteDatabase database = getWritableDatabase();
        String args [] = new String[]{products.getId()+""};
        long result = database.delete(tableName,TB_CLM_ID+"=?",args);
        return result > 0;
    }

    public boolean insertProductInPurchases(Products p){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TB_CLM_IMAGE,p.getImage());
        values.put(TB_CLM_NAME,p.getName());
        values.put(TB_CLM_PRICE,p.getPrice());
        values.put(TB_CLM_BRAND,p.getBrand());
        values.put(TB_CLM_RATING,p.getRating());
        values.put(TB_CLM_QUANTITY,p.getQuantity());

        long res = db.insert(TB_PURCHASES,null,values);
        db.close();
        return res != -1;
    }

    public ArrayList<Products> getAllProductsInPurchases(){
        ArrayList<Products> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TB_PURCHASES+"",null);

        if(cursor.moveToFirst() && cursor != null){
            do{
                @SuppressLint("Range") int image = cursor.getInt(cursor.getColumnIndex(TB_CLM_IMAGE));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(TB_CLM_NAME));
                @SuppressLint("Range") Double price = cursor.getDouble(cursor.getColumnIndex(TB_CLM_PRICE));
                @SuppressLint("Range") String brand = cursor.getString(cursor.getColumnIndex(TB_CLM_BRAND));
                @SuppressLint("Range") float rating = cursor.getFloat(cursor.getColumnIndex(TB_CLM_RATING));
                @SuppressLint("Range") int quantity = cursor.getInt(cursor.getColumnIndex(TB_CLM_QUANTITY));

                Products p = new Products(image,name,price,brand,rating,quantity);
                products.add(p);
            }while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return products;
    }

    public boolean insertUser(Users user){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(TB_CLM_USER_NAME,user.getUserName());
        values.put(TB_CLM_USER_FULL_NAME,user.getFullName());
        values.put(TB_CLM_USER_PASSWORD,user.getUserPassword());
        values.put(TB_CLM_USER_EMAIL,user.getEmail());
        values.put(TB_CLM_USER_PHONE,user.getPhone());
        values.put(TB_CLM_USER_IMAGE,user.getUserImage());

        long res = db.insert(TB_USERS,null,values);
        db.close();
        return res != -1;
    }

    public Users getUser(int user_id){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+TB_USERS+" WHERE "+TB_CLM_USER_ID+" =?",new String[]{String.valueOf(user_id)});

        if(cursor.moveToFirst() && cursor != null){
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(TB_CLM_USER_ID));
            @SuppressLint("Range") String userName = cursor.getString(cursor.getColumnIndex(TB_CLM_USER_NAME));
            @SuppressLint("Range") String fullName = cursor.getString(cursor.getColumnIndex(TB_CLM_USER_FULL_NAME));
            @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex(TB_CLM_USER_PASSWORD));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(TB_CLM_USER_EMAIL));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(TB_CLM_USER_PHONE));
            @SuppressLint("Range") String image = cursor.getString(cursor.getColumnIndex(TB_CLM_USER_IMAGE));

            Users user = new Users(id,userName,fullName,image,password,email,phone);
            cursor.close();
            db.close();
            return user;
        }
        return null;
    }

    public boolean upDataUser(Users user){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(TB_CLM_USER_FULL_NAME,user.getFullName());
        values.put(TB_CLM_USER_EMAIL,user.getEmail());
        values.put(TB_CLM_USER_PHONE,user.getPhone());
        values.put(TB_CLM_USER_IMAGE,user.getUserImage());

        String args [] = new String[]{user.getId()+""};
        long result = db.update(TB_USERS, values,TB_CLM_USER_ID+"=?",args);
        db.close();
        return result > 0;

    }

    @SuppressLint("Range")
    public int checkUser(String user_name, String password){
        SQLiteDatabase db = getReadableDatabase();
        String[] selectionArgs = {user_name, password};
        String[] columns = {TB_CLM_USER_ID};

        Cursor cursor = db.query(TB_USERS,columns,TB_CLM_USER_NAME+" =? AND "+TB_CLM_USER_PASSWORD+" =?",selectionArgs,null,null,null);
        int id=0;
        int cursorCount = cursor.getCount();

        if (cursorCount > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex(TB_CLM_USER_ID));
            cursor.close();
            db.close();
            return id;
        }

        return id;
    }

}


package com.example.shoppingapp;

public interface OnRecyclerViewClickListener {
    void OnItemClick(int productId);
}


package com.example.shoppingapp;

public class Products {

    private int id;
    private int image;
    private String name;
    private double price;
    private String brand;
    private int pieces;
    private String description;
    private double discount;
    private float rating;
    private int quantity;

    public Products(int image, String name, double price, String brand, int pieces, String description, double discount, float rating) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.pieces = pieces;
        this.description = description;
        this.discount = discount;
        this.rating = rating;
    }

    public Products(int id) {
        this.id = id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Products(int id, int image, String name, double price, float rating) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    public Products(int image, String name, double price, double discount, float rating) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.rating = rating;
    }

    public Products(int id, int image, String name, double price, String brand, int pieces, String description, double discount) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.pieces = pieces;
        this.description = description;
        this.discount = discount;
    }

    public Products(int id, int image, String name, double price, String brand, float rating, int quantity) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.rating = rating;
        this.quantity = quantity;
    }

    public Products(int image, String name, double price, String brand, float rating, int quantity) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.rating = rating;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Products(int image, String name, double price, String brand, int pieces, String description, double discount) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.pieces = pieces;
        this.description = description;
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }
}


package com.example.shoppingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentTransaction;

import android.content.SharedPreferences;
import android.os.Bundle;

public class SettingActivity extends AppCompatActivity {

    SharedPreferences sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Setting s = new Setting();
        fragmentTransaction.replace(R.id.settings,s);
        fragmentTransaction.commit();

//        sh = getSharedPreferences("myPreferences", MODE_PRIVATE);
//        boolean stats = sh.getBoolean("dark_mood_screen",false);
//
//        if(stats == true){
//            setTheme(androidx.preference.R.style.ThemeOverlay_AppCompat_Dark);
//        }else{
//            setTheme(com.airbnb.lottie.R.style.Theme_AppCompat_DayNight);
//        }

    }
}

package com.example.shoppingapp;

import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    ArrayList<Products> products;
    OnRecyclerViewClickListener listener;

    public ProductAdapter(ArrayList<Products> products, OnRecyclerViewClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public ArrayList<Products> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Products> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custome_card_products,null,false);
        ProductViewHolder pvh = new ProductViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Products p = products.get(position);
        if(p.getImage() != 0){
            holder.img.setImageResource(p.getImage());
        }else{
            holder.img.setImageResource(R.drawable.products);
        }
        holder.name.setText(p.getName());
        holder.price.setText(p.getPrice()+"$");
        holder.brand.setText("Brand: "+p.getBrand());
        holder.number_pieces.setText(p.getPieces()+"");
        if(p.getDiscount()>0){
            holder.priceAfter.setText(p.getPrice()-(p.getPrice()*(p.getDiscount()/100))+"$");
            holder.price.setPaintFlags(holder.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//وضع خط علي السعر القديم
            holder.price.setTextColor(Color.parseColor("#BFBFBF"));
        }else{
            holder.priceAfter.setText("");
            holder.price.setTextColor(Color.parseColor("#000000"));
        }

        holder.name.setTag(position+1); //اوبجكت مخفي لكي اخزن product_id
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView name,price,brand,number_pieces,priceAfter;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_card_products);
            name = itemView.findViewById(R.id.tv_name_card_products);
            price = itemView.findViewById(R.id.tv_price_card_products);
            brand = itemView.findViewById(R.id.tv_brand_card_products);
            number_pieces = itemView.findViewById(R.id.tv_mun_pieces_card_products);
            priceAfter = itemView.findViewById(R.id.tv_priceafter_card_products);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int id = (int)name.getTag();
                    listener.OnItemClick(id);
                }
            });

        }
    }

}


package com.example.shoppingapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    CardView fashion_card,book_card,beauty_card,electrics_card,game_card,homeCooker_card,laptop_card,mobile_card,sports_card,carTools_card;
    TextView tv_fashion,tv_book,tv_beauty,tv_electrics,tv_game,tv_home,tv_laptop,tv_mobile,tv_sports,tv_car;
    public static final String FASHION_KEY = "fashion_key";
    public static final String BOOK_KEY = "book_key";
    public static final String BEAUTY_KEY = "beauty_key";
    public static final String ELECTRICS_KEY = "electrics_key";
    public static final String GAME_KEY = "game_key";
    public static final String HOME_KEY = "home_key";
    public static final String LAPTOP_KEY = "laptop_key";
    public static final String MOBILE_KEY = "mobile_key";
    public static final String SPORTS_KEY = "sports_key";
    public static final String CAR_KEY = "car_key";
    public static String name_data ="";

    ShoppingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fashion_card = findViewById(R.id.fashion_card);
        book_card = findViewById(R.id.book_card);
        beauty_card = findViewById(R.id.beauty_card);
        electrics_card = findViewById(R.id.electronics_card);
        game_card = findViewById(R.id.games_card);
        homeCooker_card = findViewById(R.id.home_cooker_card);
        laptop_card = findViewById(R.id.laptop_card);
        mobile_card = findViewById(R.id.mobiles_card);
        sports_card = findViewById(R.id.sports_card);
        carTools_card = findViewById(R.id.car_card);
        tv_fashion = findViewById(R.id.tv_fashion_card);
        tv_book = findViewById(R.id.tv_books_card);
        tv_beauty = findViewById(R.id.tv_beauty_card);
        tv_electrics = findViewById(R.id.tv_electronics_card);
        tv_game = findViewById(R.id.tv_game_card);
        tv_home = findViewById(R.id.tv_home_card);
        tv_laptop = findViewById(R.id.tv_laptop_card);
        tv_mobile = findViewById(R.id.tv_mobile_card);
        tv_sports = findViewById(R.id.tv_sports_card);
        tv_car = findViewById(R.id.tv_car_card);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Products");


        db = new ShoppingDatabase(this);
//        Products p = new Products(R.drawable.python,"python",32.0,"Electronic Book",23,"Python is a computer programming language often used to build websites and software, automate tasks, and conduct data analysis.",0);
//
//        db.insertProduct(p,ShoppingDatabase.TB_BOOK);

        fashion_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_fashion.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(FASHION_KEY,tv_fashion.getText().toString());
                startActivity(intent);
            }
        });

        book_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_book.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(BOOK_KEY,tv_book.getText().toString());
                startActivity(intent);
            }
        });

        beauty_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_beauty.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(BEAUTY_KEY,tv_beauty.getText().toString());
                startActivity(intent);
            }
        });

        electrics_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_electrics.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(ELECTRICS_KEY,tv_electrics.getText().toString());
                startActivity(intent);
            }
        });

        game_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_game.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(GAME_KEY,tv_game.getText().toString());
                startActivity(intent);
            }
        });

        homeCooker_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_home.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(HOME_KEY,tv_home.getText().toString());
                startActivity(intent);
            }
        });

        laptop_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_laptop.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(LAPTOP_KEY,tv_laptop.getText().toString());
                startActivity(intent);
            }
        });

        mobile_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_mobile.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(MOBILE_KEY,tv_mobile.getText().toString());
                startActivity(intent);
            }
        });

        sports_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_sports.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(SPORTS_KEY,tv_sports.getText().toString());
                startActivity(intent);
            }
        });

        carTools_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name_data = tv_car.getText().toString();
                Intent intent = new Intent(getBaseContext(),ProductsCardActivity.class);
                intent.putExtra(CAR_KEY,tv_car.getText().toString());
                startActivity(intent);
            }
        });

    }
}

package com.example.shoppingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    TextView tv_login;
    EditText et_user_name,et_full_name,et_user_email,et_user_password,et_user_ConformPassword,et_user_phone;
    Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tv_login  = findViewById(R.id.et_login_from_register);
        et_user_name = findViewById(R.id.et_user_name_register);
        et_full_name = findViewById(R.id.et_full_name_register);
        et_user_email = findViewById(R.id.et_user_email_register);
        et_user_password = findViewById(R.id.et_user_password_register);
        et_user_ConformPassword = findViewById(R.id.et_user_ConformPassword_register);
        et_user_phone = findViewById(R.id.et_user_phone_register);
        btn_register = findViewById(R.id.btn_register_from_register);

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });

        ShoppingDatabase db = new ShoppingDatabase(this);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int c = 0;

                if(et_user_name.getText().toString().isEmpty()){
                    et_user_name.setError("Please Enter Your User Name");
                }else{
                    c++;
                }
                if(et_full_name.getText().toString().isEmpty()){
                    et_full_name.setError("Please Enter Your Full Name");
                }else{
                    c++;
                }
                if(et_user_email.getText().toString().isEmpty()){
                    et_user_email.setError("Please Enter Your Email");
                }else{
                    c++;
                }
                if(et_user_password.getText().toString().isEmpty()){
                    et_user_password.setError("Please Enter Your password");
                }else{
                    c++;
                }
                if(et_user_ConformPassword.getText().toString().isEmpty()){
                    et_user_ConformPassword.setError("Please Enter Your password");
                }else{
                    if(et_user_password.getText().toString().equals(et_user_ConformPassword.getText().toString())){
                        c++;
                    }else {
                        Toast.makeText(RegisterActivity.this, "ConformPassword not equals Password", Toast.LENGTH_SHORT).show();
                    }
                }
                if(et_user_phone.getText().toString().isEmpty()){
                    et_user_phone.setError("Please Enter Your Phone");
                }else{
                    c++;
                }

                if(c == 6){
                    db.insertUser(new Users(et_user_name.getText().toString(),
                                            et_full_name.getText().toString(),
                                            R.drawable.user+"",
                                            et_user_password.getText().toString(),
                                            et_user_email.getText().toString(),
                                            et_user_phone.getText().toString()));
                    Toast.makeText(RegisterActivity.this, "successfully registered", Toast.LENGTH_SHORT).show();
                    finish();
                }

            }
        });



    }
}

package com.example.shoppingapp;

public class Users {

    private int id;
    private String userName;
    private String fullName;
    private String userImage;
    private String userPassword;
    private String email;
    private String phone;

    public Users(int id, String userName, String fullName, String userImage, String userPassword, String email, String phone) {
        this.id = id;
        this.userName = userName;
        this.fullName = fullName;
        this.userImage = userImage;
        this.userPassword = userPassword;
        this.email = email;
        this.phone = phone;
    }

    public Users(String userName, String fullName, String userImage, String userPassword, String email, String phone) {
        this.userName = userName;
        this.fullName = fullName;
        this.userImage = userImage;
        this.userPassword = userPassword;
        this.email = email;
        this.phone = phone;
    }

    public Users(int id, String fullName, String userImage, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.userImage = userImage;
        this.email = email;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}


package com.example.shoppingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PurchasesAdapter extends BaseAdapter {

    ArrayList<Products> purchases;
    Context context;

    public PurchasesAdapter(ArrayList<Products> purchases, Context context) {
        this.purchases = purchases;
        this.context = context;
    }

    @Override
    public int getCount() {
        return purchases.size();
    }

    @Override
    public Products getItem(int i) {
        return purchases.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("ResourceType")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View v = view;
        if(v==null){
            v = LayoutInflater.from(context).inflate(R.layout.custome_purchases_products,null,false);
        }

        ImageView img = (ImageView) v.findViewById(R.id.img_products_purchases);
        TextView tv_name = v.findViewById(R.id.tv_name_purchases);
        TextView tv_price = v.findViewById(R.id.tv_price_purchases);
        TextView tv_brand = v.findViewById(R.id.tv_brand_purchases);
        RatingBar rating = v.findViewById(R.id.rating_purchases);
        TextView tv_quantity = v.findViewById(R.id.tv_quantity);

        Products p = getItem(i);
        if(p.getImage() != 0){
            img.setImageResource(p.getImage());
        }else{
            img.setImageResource(R.drawable.products);
        }
        tv_name.setText(p.getName());
        tv_price.setText(p.getPrice()+"$");
        tv_brand.setText(p.getBrand());
        rating.setRating(p.getRating());
        tv_quantity.setText(p.getQuantity()+"");

        return v;
    }
}


package com.example.shoppingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ProductsCardActivity extends AppCompatActivity {

    RecyclerView rv;
    private ProductAdapter adapter;
    public static final String PRODUCT_ID_KEY = "product_key";
    public static final String TABLE_NAME_KEY = "table_name_key";
    TextView tv_product_name;
    ShoppingDatabase db;
    private String table_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_card);

        rv = findViewById(R.id.rv_products);
        tv_product_name = findViewById(R.id.tv_product_name);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.zoom_element);
        tv_product_name.setAnimation(animation);

        db = new ShoppingDatabase(this);

        Intent intent = getIntent();
        String name;
        switch (MainActivity.name_data){
            case "Fashion":
                name = intent.getStringExtra(MainActivity.FASHION_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_FASHION;
                break;
            case "Books":
                name = intent.getStringExtra(MainActivity.BOOK_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_BOOK;
                break;
            case "Beauty Tools":
                name = intent.getStringExtra(MainActivity.BEAUTY_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_BEAUTY;
                break;
            case "Electronics":
                name = intent.getStringExtra(MainActivity.ELECTRICS_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_ELECTRICS;
                break;
            case "Video Game":
                name = intent.getStringExtra(MainActivity.GAME_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_GAME;
                break;
            case "Home and Cooker":
                name = intent.getStringExtra(MainActivity.HOME_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_HOME_COOKER;
                break;
            case "Laptop":
                name = intent.getStringExtra(MainActivity.LAPTOP_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_LAPTOP;
                break;
            case "Mobile":
                name = intent.getStringExtra(MainActivity.MOBILE_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_MOBILE;
                break;
            case "Sports":
                name = intent.getStringExtra(MainActivity.SPORTS_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_SPORTS;
                break;
            case "Car Tools":
                name = intent.getStringExtra(MainActivity.CAR_KEY);
                tv_product_name.setText(name);
                table_name = ShoppingDatabase.TB_CAR_TOOL;
                break;
        }
        MainActivity.name_data = "";

        ArrayList<Products> products = new ArrayList<>();
        products = db.getAllProducts(table_name);
        adapter = new ProductAdapter(products, new OnRecyclerViewClickListener() {
            @Override
            public void OnItemClick(int productId) {
                Intent i = new Intent(getBaseContext(),DisplayProductsActivity.class);
                i.putExtra(PRODUCT_ID_KEY,productId);
                i.putExtra(TABLE_NAME_KEY,table_name);
                HomeActivity.flag = false;
                startActivity(i);
            }
        });
        RecyclerView.LayoutManager lm = new GridLayoutManager(this,2);
        rv.setLayoutManager(lm);
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);

    }

    @SuppressLint("ResourceAsColor")
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.main_search).getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<Products> product = db.getProductForSearch(query,table_name);
                adapter.setProducts(product);
                adapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<Products> product = db.getProductForSearch(newText,table_name);
                adapter.setProducts(product);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ArrayList<Products> product = db.getAllProducts(table_name);
                adapter.setProducts(product);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        return true;
    }

}

package com.example.shoppingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText user_name,user_password;
    CheckBox remember_me;
    TextView error;
    Button btn_login;
    TextView btn_register;
    ShoppingDatabase db;
    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;
    SharedPreferences shp_id;
    SharedPreferences.Editor shpEditor_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        btn_register =findViewById(R.id.tv_register_from_login);
        btn_login = findViewById(R.id.btnlogin);
        user_name = findViewById(R.id.et_user_name_login);
        user_password = findViewById(R.id.et_password_login);
        remember_me = findViewById(R.id.chb_remember_login);
        error = findViewById(R.id.tv_login_error);
        shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        shp_id = getSharedPreferences("Preferences_id", MODE_PRIVATE);
        checkLogin();

        db = new ShoppingDatabase(this);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int c = 0;
                if(user_name.getText().toString().isEmpty()){
                    user_name.setError("Enter User Name");
                }else{
                    c++;
                }
                if(user_password.getText().toString().isEmpty()){
                    user_password.setError("Enter Your Password");
                }else{
                    c++;
                }

               if(c == 2){
                   int id = db.checkUser(user_name.getText().toString(),user_password.getText().toString());
                   Toast.makeText(LoginActivity.this, ""+id, Toast.LENGTH_SHORT).show();
                   try{
                       if(id>0){
                           if (shp == null){
                               shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
                           }
                           if (shp_id == null){
                               shp_id = getSharedPreferences("Preferences_id", MODE_PRIVATE);
                           }
                           shpEditor_id = shp_id.edit();
                           shpEditor_id.putInt("user_id",id);
                           shpEditor_id.commit();

                           if(remember_me.isChecked()){
                               shpEditor = shp.edit();
                               shpEditor.putInt("user",id);
                               shpEditor.commit();
                           }

                           startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                           finish();
                       }else{
                           error.setVisibility(View.VISIBLE);
                       }
                   }catch (Exception ex){
                       error.setText(ex.getMessage().toString());
                   }

               }

            }
        });

    }

    public void checkLogin(){
        if (shp == null){
            shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        }
        int user_id = shp.getInt("user",0);

        if(user_id != 0 ){
            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
            finish();
        }
    }

}

package com.example.shoppingapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayProductsActivity extends AppCompatActivity {

    RatingBar rb;
    ImageView product_img;
    TextView tv_rating,product_name,Product_price,Product_discount,Product_brand,Product_pieces,Product_description;
    Spinner product_quantity;
    Button add_to_cart;
    double priceAfter;

    ShoppingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_products);

        rb = findViewById(R.id.ratingBar);
        product_img = findViewById(R.id.display_iv_product);
        product_name = findViewById(R.id.display_tv_name);
        Product_price = findViewById(R.id.display_tv_price);
        Product_discount = findViewById(R.id.display_tv_discount);
        Product_brand = findViewById(R.id.display_tv_brand);
        Product_pieces = findViewById(R.id.display_tv_pieces);
        Product_description = findViewById(R.id.display_tv_description);
        product_quantity = findViewById(R.id.display_get_quantity);
        add_to_cart = findViewById(R.id.display_btn_cart);
        tv_rating = findViewById(R.id.display_rating_number);

        rb.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                tv_rating.setText(v+"");
            }
        });
        int product_id;
        String table_name;
        if(HomeActivity.flag == true){
            Intent intent = getIntent();
            product_id = intent.getIntExtra(HomeActivity.PRODUCT_KEY,-1);
            table_name = intent.getStringExtra(HomeActivity.TABLE_NAME_KEY);
        }else{
            Intent intent = getIntent();
            product_id = intent.getIntExtra(ProductsCardActivity.PRODUCT_ID_KEY,-1);
            table_name = intent.getStringExtra(ProductsCardActivity.TABLE_NAME_KEY);
        }

        db = new ShoppingDatabase(this);
        Products p = db.getProduct(product_id,table_name);

        if(p.getImage() != 0)
            product_img.setImageResource(p.getImage());
        product_name.setText(p.getName());
        if (p.getDiscount() > 0) {
            priceAfter = p.getPrice() - (p.getPrice() * (p.getDiscount() / 100));
            Product_discount.setText(priceAfter + "$");
            Product_price.setText(p.getPrice()+"$");
            Product_price.setPaintFlags(Product_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);//وضع خط علي السعر القديم
            Product_price.setTextColor(Color.parseColor("#BFBFBF"));
        } else {
            priceAfter = p.getPrice();
            Product_discount.setText("");
            Product_price.setText(priceAfter + "$");
            Product_price.setTextColor(Color.parseColor("#000000"));
        }
        Product_brand.setText(p.getBrand());
        Product_pieces.setText(p.getPieces()+"");
        Product_description.setText(p.getDescription());

        add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int image = p.getImage();
                String name = product_name.getText().toString();
                String brand = Product_brand.getText().toString();
                int quantity = Integer.parseInt(product_quantity.getSelectedItem().toString());
                float rating = Float.parseFloat(tv_rating.getText().toString());
                
                Products ppp = new Products(image,name,priceAfter,brand,rating,quantity);

                AlertDialog alertDialog = new AlertDialog.Builder(DisplayProductsActivity.this).create();
                alertDialog.setTitle(name);
                alertDialog.setMessage("Click (Ok) TO Add This Product To Cart");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        db.insertProductInPurchases(ppp);
                        Toast.makeText(DisplayProductsActivity.this, "Purchased Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                alertDialog.show();
            }
        });

    }
}

package com.example.shoppingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    EditText et_name,et_gmail,et_phone;
    ImageView iv_edit_data,iv_edit_photo,iv_photo;
    Button btn_save;
    TextView tv_full_name,tv_user_name;
    LinearLayout layout_name;
    Uri imageUri;
    SharedPreferences shp_id;
    private static final int PICK_IMAGE_REQ_COD = 1;
    SharedPreferences.Editor shpEditor_id;
    ShoppingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My Profile");

        et_name = findViewById(R.id.et_full_name_profile);
        et_gmail = findViewById(R.id.et_gmail_user_profile);
        et_phone = findViewById(R.id.et_phone_user_profile);
        iv_edit_data = findViewById(R.id.iv_edit_data);
        iv_edit_photo = findViewById(R.id.iv_edit_photo);
        iv_photo = findViewById(R.id.ri_profile_image);
        btn_save = findViewById(R.id.btn_save_data);
        tv_full_name = findViewById(R.id.tv_full_name_profile);
        tv_user_name = findViewById(R.id.tv_user_name_profile);
        layout_name = findViewById(R.id.enter_name);

        shp_id = getSharedPreferences("Preferences_id", MODE_PRIVATE);

        int user_id = shp_id.getInt("user_id",0);

        db = new ShoppingDatabase(this);

        Users users = db.getUser(user_id);
        et_name.setText(users.getFullName());
        et_gmail.setText(users.getEmail());
        et_phone.setText(users.getPhone());
        if(users.getUserImage()!=null && !users.getUserImage().equals("")){
            imageUri = Uri.parse(users.getUserImage());
            iv_photo.setImageURI(imageUri);
        }else{
            iv_photo.setImageResource(R.drawable.man);
        }
        tv_full_name.setText(users.getFullName());
        tv_user_name.setText(users.getUserName());


        iv_edit_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_edit_data.setVisibility(View.INVISIBLE);
                btn_save.setVisibility(View.VISIBLE);
                et_name.setEnabled(true);
                et_gmail.setEnabled(true);
                et_phone.setEnabled(true);
                iv_edit_photo.setVisibility(View.VISIBLE);
                layout_name.setVisibility(View.VISIBLE);
            }
        });


        iv_edit_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in,PICK_IMAGE_REQ_COD);
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get data

                String name = et_name.getText().toString();
                String gmail = et_gmail.getText().toString();
                String phone = et_phone.getText().toString();
                String image = "";
                if(imageUri != null){
                    image = imageUri.toString();
                }

                db.upDataUser(new Users(user_id,name,image,gmail,phone));

                Users user = db.getUser(user_id);
                et_name.setText(user.getFullName());
                et_gmail.setText(user.getEmail());
                et_phone.setText(user.getPhone());
                if(user.getUserImage()!=null && !user.getUserImage().equals("")){
                    iv_photo.setImageURI(Uri.parse(user.getUserImage()));
                }else{
                    iv_photo.setImageResource(R.drawable.man);
                }
                tv_full_name.setText(user.getFullName());

                iv_edit_data.setVisibility(View.VISIBLE);
                btn_save.setVisibility(View.GONE);
                et_name.setEnabled(false);
                et_gmail.setEnabled(false);
                et_phone.setEnabled(false);
                iv_edit_photo.setVisibility(View.GONE);
                layout_name.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQ_COD && resultCode == RESULT_OK){
            if(data != null){
                imageUri = data.getData();
                iv_photo.setImageURI(imageUri);
            }
        }
    }

}

package com.example.shoppingapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;

public class Setting extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}


package com.example.shoppingapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class PurchasesActivity extends AppCompatActivity {

    ListView lv;
    PurchasesAdapter pa;
    ShoppingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchases);

        lv = findViewById(R.id.lv_purchases);

        db = new ShoppingDatabase(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("purchases");

        ArrayList<Products> p = new ArrayList<>();
        p = db.getAllProductsInPurchases();
        pa = new PurchasesAdapter(p,this);
        pa.notifyDataSetChanged();
        lv.setAdapter(pa);

    }
}

package com.example.shoppingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    BottomNavigationView bnv;
    RecyclerView rv;
    public static final String PRODUCT_KEY = "product_key";
    private static final int PERMISSION_REQ_COD = 1;
    public static final String TABLE_NAME_KEY = "table_key";
    SharedPreferences shp;
    SharedPreferences.Editor shpEditor;
    SharedPreferences shp_id;
    SharedPreferences.Editor shpEditor_id;
    public static boolean flag;
    ShoppingDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        flag = false;
        db = new ShoppingDatabase(this);
        // int image, String name, double price, String brand, int pieces, String description, double discount, float rating
//        Products p = new Products(R.drawable.playstation4,"playstation4",2500,"sony",25,"computer noc Barnes & Noble Retail and Samsung Electronics launched the new Samsung Galaxy Tab 4 Noc tablet in New York.",10,(float) 3.5);
//        db.insertProduct(p,ShoppingDatabase.TB_GAME);
//
//        Products p1 = new Products(R.drawable.playstation5,"playstation5&4",7600,"sony",81,"Sony PlayStation 5 Console + 2 DualSense Wireless Controller+ fifa 21 standard edition All prices include VAT..",25,(float) 3.5);
//        db.insertProduct(p1,ShoppingDatabase.TB_GAME);
//
//        Products p2 = new Products(R.drawable.pes2012,"pes2012",4900,"Konami",45,"AUTHENTIC LEAGUES - Fully licensed leagues are coming to PES 2012. Details to be revealed soon!",0,(float) 3.5);
//        db.insertProduct(p2,ShoppingDatabase.TB_GAME);
//
//        Products p3 = new Products(R.drawable.menacing,"menacing",5850,"acer",32,"Acer Predator Thronos Air, Gaming Throne with Massage Pad and Gaming Chair, Up to 3 Displays, 130 Degrees Recline, LED Lighting, PC Landing Pad, Stabilizing Arm (PC and Monitors Sold Separately)",0,(float) 3.5);
//        db.insertProduct(p3,ShoppingDatabase.TB_GAME);
//
//        Products p4 = new Products(R.drawable.steelchair3,"char gaming",6000,"acer",51,"different colors gaming Thermaltake U-Fit Black-Red Gaming Chair GGC-UFT-BRMWDS-01 GGC-UFT-BRMWDS-01",0,(float) 3.5);
//        db.insertProduct(p4,ShoppingDatabase.TB_GAME);
//
//        Products p5 = new Products(R.drawable.gamecontroller5,"gamecontroller5",800," Powerextra",26,"Powerextra Xbox 360 Controllers,USB Gamepad Wired Controller Improved Ergonomic Joystick Dual Vibration,Compatible with Xbox 360 Slim/Xbox 360/PC(Windows / 7/8.1/10),Black",0,(float) 3.5);
//        db.insertProduct(p5,ShoppingDatabase.TB_GAME);
//        Products p6 = new Products(R.drawable.microwave,"Microwave",270,"TOSHIBA",92,"Microwave is an electric device used to heat various types of foods, and this type of oven is different from traditional ovens",0,(float) 3.5);
//        db.insertProduct(p6,ShoppingDatabase.TB_LAPTOP);
//        Products p7 = new Products(R.drawable.vacuumcleaner,"vacuum cleaner",210,"TOSHIBA",32,"Hoover Vacuum Cleaner - 2000W, Black - Red TCP2010020 Buy with installments and pay EGP 51.06 for 48 months with select banks.",10,(float) 3.5);
//        db.insertProduct(p7,ShoppingDatabase.TB_LAPTOP);
//        Products p8 = new Products(R.drawable.washer,"Washer",690,"TOSHIBA",81,"TOSHIBA Washing Machine Half Automatic Capacity : 12 Kg Max Spin Speed : 1400 RPM With 2 Motors Washing Machine Giant Super Size Works With All Kinds of Regular Powders Vortexes System",0,(float) 3.5);
//        db.insertProduct(p8,ShoppingDatabase.TB_LAPTOP);
//        Products p9 = new Products(R.drawable.teshert,"t_shirt",45,"Zara",52,"Black t_shirt size XL.",0,(float) 3.5);
//        db.insertProduct(p9,ShoppingDatabase.TB_HOME_COOKER);

//        Products p = new Products(15);
//        db.deleteProduct(p,ShoppingDatabase.TB_FASHION);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQ_COD);
        }



        rv = findViewById(R.id.rv_home);
        shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
        shp_id = getSharedPreferences("Preferences_id", MODE_PRIVATE);

        ArrayList<Products> products1 = new ArrayList<>();
        products1 = db.getAllProducts(ShoppingDatabase.TB_PRODUCT_DISCOUNT);
        HomeAdabter adapter = new HomeAdabter(products1, new OnRecyclerViewClickListener() {
            @Override
            public void OnItemClick(int productId) {
                Intent i = new Intent(getBaseContext(),DisplayProductsActivity.class);
                i.putExtra(PRODUCT_KEY,productId);
                i.putExtra(TABLE_NAME_KEY,ShoppingDatabase.TB_PRODUCT_DISCOUNT);
                flag = true;
                startActivity(i);
            }
        });
        RecyclerView.LayoutManager lm = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        rv.setLayoutManager(lm);
        rv.setHasFixedSize(true);
        rv.setAdapter(adapter);

        bnv = findViewById(R.id.BottomNavigationView);
        bnv.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent1 = new Intent(getBaseContext(),HomeActivity.class);
                        startActivity(intent1);
                        break;
                    case R.id.products:
                        Intent intent2 = new Intent(getBaseContext(),MainActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.profile:
                        Intent intent3 = new Intent(getBaseContext(),ProfileActivity.class);
                        startActivity(intent3);
                        break;
                    case R.id.basket:
                        Intent intent4 = new Intent(getBaseContext(),PurchasesActivity.class);
                        startActivity(intent4);
                        break;
                }

                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_up_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                logout();
                break;
            case R.id.settings:
                Intent intent = new Intent(this,SettingActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        try {
            if (shp == null){
                shp = getSharedPreferences("myPreferences", MODE_PRIVATE);
            }
            shpEditor = shp.edit();
            shpEditor.putInt("user", 0);
            shpEditor.commit();

            if (shp_id == null){
                shp_id = getSharedPreferences("Preferences_id", MODE_PRIVATE);
            }
            shpEditor_id = shp_id.edit();
            shpEditor_id.putInt("user_id",0);
            shpEditor_id.commit();

            startActivity(new Intent(HomeActivity.this,LoginActivity.class));
            finish();

        }catch (Exception ex){
            Toast.makeText(this, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_REQ_COD:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //تم الحصول عليه
                }else{

                }
        }
    }

}

package com.example.shoppingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable,2000);

    }
}

