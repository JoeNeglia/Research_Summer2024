package com.codebinars.a2048game;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.codebinars.a2048game.database.ScoreModel;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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
        assertEquals("com.codebinars.a2048gameclone", appContext.getPackageName());
    }

    @Test //Number 0: DELETE ALL RECORDS
    public void deleteData(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DatabaseHelper databaseHelper = new DatabaseHelper(appContext);
        databaseHelper.deleteAllData();

    }

    @Test //Number 1: Adding ScoreModels to DB: WORKS !!
    public void testAddingAndDisplayingDB(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DatabaseHelper databaseHelper = new DatabaseHelper(appContext);
        String name = "Eduardo Tequiero";
        int userScore = 12;
        String datetime = "03 20 4900";
        float duration = 3.342983F;
        ScoreModel scoreModel = new ScoreModel();
        scoreModel.setUsername(name);
        scoreModel.setScore(userScore);
        scoreModel.setDatetime(datetime);
        scoreModel.setDuration(duration);
        databaseHelper.addScore(scoreModel);
        List<ScoreModel> scoreList;
    }
}

package com.codebinars.a2048game;

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

package com.codebinars.a2048game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.codebinars.a2048game.scoresView.ScoreListRecycler;


import static com.codebinars.a2048game.scoresView.ScoreConstants.USER_NAME;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClick(View view) {
        Intent myIntent = null;
        switch (view.getId()) {
            case R.id.btnPlayGame:
                EditText playerusername = findViewById(R.id.playerUsername);
                String username = playerusername.getText().toString();
                myIntent = new Intent(MainActivity.this, GameActivity.class);
                myIntent.putExtra(USER_NAME, username);
                break;
            case R.id.btnCheckScore:
                myIntent = new Intent(this, ScoreListRecycler.class);
                break;
        }
        if(myIntent != null){
            startActivity(myIntent);
        }
    }
}

package com.codebinars.a2048game;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.codebinars.a2048game.database.DBHelper;
import com.codebinars.a2048game.scoresView.ImageUtils;
import com.codebinars.a2048game.scoresView.ScoreListRecycler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.codebinars.a2048game.scoresView.ScoreConstants.*;

public class EditScoreActivity extends Activity {
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private DBHelper dbHelper;
    private EditText editUsername, editScore, editDuration, editCountry;
    private TextView editDate;
    private ImageView avatarView;
    private boolean updatedImage = false;
    private int scoreId;
    private Bitmap avatarImage = null;
    private Calendar calendar;
    private String imageroot;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_score);

        calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            // TODO Auto-generated method stub
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate();
        };
        dbHelper = DBHelper.getInstance(getApplicationContext());
        editScore = findViewById(R.id.editScoreCamp);
        editUsername = findViewById(R.id.editUsernameCamp);
        editDate = findViewById(R.id.editDateCamp);
        editDuration = findViewById(R.id.editDurationCamp);
        editCountry = findViewById(R.id.editCountryCamp);
        avatarView = findViewById(R.id.avatarDisplay);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        scoreId = extras.getInt(SCORE_ID);
        editScore.setText(extras.getString(SCORE_VALUE));
        editDuration.setText(extras.getString(SCORE_DURATION));
        editUsername.setText(extras.getString(USER_NAME));
        editCountry.setText(extras.getString(USER_COUNTRY));
        if(extras.getString(USER_AVATAR) != null && (extras.getString(USER_AVATAR).length() > 5)){
            avatarView.setImageBitmap(ImageUtils.loadImage(extras.getString(USER_AVATAR)));
        }
        editDate.setText(extras.getString(SCORE_DATETIME));
        editDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditScoreActivity.this, date, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public void onClick(View view) {
        Intent myIntent = null;
        switch (view.getId()) {
            case R.id.editAvatarButton:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED) { //Permission not granted, request it
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE);
                    } else{ //Permission already granted
                        pickImageFromGallery();
                    }
                }
                else{ //System OS is less then marshmallow
                    pickImageFromGallery();
                }
                break;
            case R.id.updateLocation:
                editCountry.setText(getApplicationContext().getResources().getConfiguration().locale.getDisplayCountry());
                break;
            case R.id.savechanges:
                    dbHelper.updateScore(
                            scoreId,
                            editUsername.getText().toString(),
                            Integer.valueOf(editScore.getText().toString()),
                            editDate.getText().toString(),
                            Float.parseFloat(editDuration.getText().toString()));
                    if (avatarImage!=null){
                        saveImage(avatarImage); //Store the IMAGE on DEVICE if it was loaded
                    }
                    dbHelper.updateUser(editUsername.getText().toString().toLowerCase(), imageroot, editCountry.getText().toString());
                    myIntent = new Intent(EditScoreActivity.this, ScoreListRecycler.class);
                    startActivity(myIntent);
                    finish();
                break;
            case R.id.undochanges:
                myIntent = new Intent(EditScoreActivity.this, ScoreListRecycler.class);
                startActivity(myIntent);
                finish();
                break;
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void updateDate(){
        String myFormat = "dd-MM-yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(myFormat);
        editDate.setText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Permission was granted
                    pickImageFromGallery();
                }
                else{
                    Toast.makeText(this, "Permission denied ... !", Toast.LENGTH_SHORT).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            Uri uri = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(uri);
                avatarImage = BitmapFactory.decodeStream(inputStream);
                updatedImage = true;
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
            avatarView.setImageBitmap(avatarImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * We could simply save images every time a user loads one
     * But we prefer so store them only if he clicks "Save"
     * @param finalBitmap
     */
    private void saveImage(Bitmap finalBitmap) {
        String root = getExternalFilesDir(null).getAbsolutePath();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        String imagename = "Image-"+ editUsername.getText().toString() +".jpg";
        File file = new File (myDir, imagename);
        imageroot = myDir+"/"+imagename;
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}



package com.codebinars.a2048game;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;

public class GameActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }
}


package com.codebinars.a2048game.database;

public class ScoreModel {
    private int id;
    private Integer usernameId;
    private Integer score;
    private String datetime;
    private Float duration;

    public ScoreModel(int id, Integer usernameId, Integer score, String datetime, Float duration) {
        this.id = id;
        this.usernameId = usernameId;
        this.score = score;
        this.datetime = datetime;
        this.duration = duration;
    }

    public ScoreModel(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUsernameId() {
        return usernameId;
    }

    public void setUsernameId(Integer usernameId) {
        this.usernameId = usernameId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "ScoreModel{" +
                "id=" + id +
                ", usernameId='" + usernameId + '\'' +
                ", score=" + score +
                ", datetime='" + datetime + '\'' +
                ", duration=" + duration +
                '}';
    }
}


package com.codebinars.a2048game.database;

public class ScoreDisplay {
    int ID;
    Integer score;
    String username;
    String datetime;
    Float duration;
    String country;
    String avatar;

    public ScoreDisplay(int ID, Integer score, String datetime, Float duration, String username, String avatar, String country) {
        this.ID = ID;
        this.score = score;
        this.datetime = datetime;
        this.duration = duration;
        this.username = username;
        this.country = country;
        this.avatar = avatar;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Float getDuration() {
        return duration;
    }

    public void setDuration(Float duration) {
        this.duration = duration;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}


package com.codebinars.a2048game.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "Scores_db";

    //USER TABLE
    protected static final String USER_TABLE = "USER_TABLE";
    protected static final String COLUMN_ID = "ID";
    protected static final String COLUMN_USERNAME = "USERNAME";
    protected static final String COLUMN_IMAGE = "IMAGE";
    protected static final String COLUMN_COUNTRY = "COUNTRY";

    //SCORE TABLE
    protected static final String SCORE_TABLE = "SCORE_TABLE";
    protected static final String COLUMN_USERNAME_ID = "USERNAME_ID";
    protected static final String COLUMN_SCORE = "SCORE";
    protected static final String COLUMN_DATETIME = "DATETIME";
    protected static final String COLUMN_DURATION = "DURATION";

    protected static final String CREATE_SCORE_TABLE_STATEMENT =
            "CREATE TABLE " + SCORE_TABLE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME_ID + " TEXT, " +
                    COLUMN_SCORE + " INTEGER, " +
                    COLUMN_DATETIME + " TEXT, " +
                    COLUMN_DURATION + " INTEGER, " +
                    "FOREIGN KEY (" + COLUMN_USERNAME_ID + ") REFERENCES " + USER_TABLE + " (" + COLUMN_ID + ") )";

    protected static final String CREATE_USER_TABLE_STATEMENT =
            "CREATE TABLE " + USER_TABLE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_IMAGE + " TEXT, " +
                    COLUMN_COUNTRY + " TEXT) ";

    private SQLiteDatabase db;

    private static DBHelper dbInstance = null;

    public static DBHelper getInstance(Context activityContext) {
        if (dbInstance == null) {
            dbInstance = new DBHelper(activityContext.getApplicationContext());
        }
        return dbInstance;
    }

    private DBHelper(Context applicationContext) {
        super(applicationContext, DB_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE_STATEMENT);
        db.execSQL(CREATE_SCORE_TABLE_STATEMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + SCORE_TABLE);
        onCreate(db);
    }

    public void checkDbStatus() {
        if (db == null) {
            db = getWritableDatabase();
        }
    }

    /**
     * Method to add a Score in DB
     *
     * @param scoreModel Score to add
     */
    public void addScore(ScoreModel scoreModel) {
        checkDbStatus();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME_ID, scoreModel.getUsernameId());
        cv.put(COLUMN_SCORE, scoreModel.getScore());
        cv.put(COLUMN_DATETIME, scoreModel.getDatetime());
        cv.put(COLUMN_DURATION, scoreModel.getDuration());
        db.insert(SCORE_TABLE, null, cv);
    }


    /**
     * Get all scores in DB
     * Including Username, Country and Avatar of the USER matching that Score
     *Select id, number, date, name, country, avatarpath
     * FROM userTable
     * INNER JOIN scoreTable on id = user_id
     * @return ArrayList with all scores
     */
    public List<ScoreDisplay> getAllScores() {
        checkDbStatus();
        List<ScoreDisplay> getAllScores = new ArrayList<>();
        String queryAllScores =
                "SELECT "+ "SCORE."+COLUMN_ID+", "+COLUMN_SCORE+", "+COLUMN_DATETIME+", "+COLUMN_DURATION+", "+
                 COLUMN_USERNAME+", "+COLUMN_IMAGE+", "+COLUMN_COUNTRY +
                 " FROM "+USER_TABLE +" USER"+
                 " INNER JOIN "+SCORE_TABLE+ " SCORE" + " ON "+ "USER."+COLUMN_ID +" = "+COLUMN_USERNAME_ID +
                 " ORDER BY " + COLUMN_SCORE + " DESC";
        Cursor cursor = db.rawQuery(queryAllScores, null);
        if (cursor.moveToNext()) {
            do{
            int scoreId = cursor.getInt(0);
            Integer score = cursor.getInt(1);
            String datetime = cursor.getString(2);
            Float duration = cursor.getFloat(3);
            String username = cursor.getString(4);
            String imagePath = cursor.getString(5);
            String country = cursor.getString(6);
            ScoreDisplay scoreDisplay = new ScoreDisplay(scoreId, score, datetime, duration, username, imagePath, country);
            getAllScores.add(scoreDisplay);
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        return getAllScores;
    }

    /**
     * Find top 10 scores
     * @return ArrayList10 with 10 top scores (Sorted by Score)
     */
    public List<ScoreDisplay> getTop10Scores() {
        checkDbStatus();
        List<ScoreDisplay> getAllScores = new ArrayList<>();
        String queryAllScores =
                 "SELECT "+ "SCORE."+COLUMN_ID+", "+COLUMN_SCORE+", "+COLUMN_DATETIME+", "+COLUMN_DURATION+", "+
                 COLUMN_USERNAME+", "+COLUMN_IMAGE+", "+COLUMN_COUNTRY +
                 " FROM "+USER_TABLE +" USER"+
                 " INNER JOIN "+SCORE_TABLE+ " SCORE" + " ON "+ "USER."+COLUMN_ID +" = "+COLUMN_USERNAME_ID +
                 " ORDER BY " + COLUMN_SCORE + " DESC LIMIT 10";
        Cursor cursor = db.rawQuery(queryAllScores, null);
        if (cursor.moveToNext()) {
            do{
                int scoreId = cursor.getInt(0);
                Integer score = cursor.getInt(1);
                String datetime = cursor.getString(2);
                Float duration = cursor.getFloat(3);
                String username = cursor.getString(4);
                String imagePath = cursor.getString(5);
                String country = cursor.getString(6);
                ScoreDisplay scoreDisplay = new ScoreDisplay(scoreId, score, datetime, duration, username, imagePath, country);
                getAllScores.add(scoreDisplay);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return  getAllScores;
    }

    /**
     * Find top 10 scores by a determined user
     * @param usertop is the User we will use in query
     * @return ArrayList10 with 10 top scores (Sorted by Score with the specified username)
     */
    public List<ScoreDisplay> getTop10ByUser(String usertop) {
        checkDbStatus();
        List<ScoreDisplay> getAllScores = new ArrayList<>();
        String queryAllScores =
                "SELECT "+ "SCORE."+COLUMN_ID+", "+COLUMN_SCORE+", "+COLUMN_DATETIME+", "+COLUMN_DURATION+", "+
                        COLUMN_USERNAME+", "+COLUMN_IMAGE+", "+COLUMN_COUNTRY +
                        " FROM "+USER_TABLE +" USER"+
                        " INNER JOIN "+SCORE_TABLE+ " SCORE" + " ON "+ "USER."+COLUMN_ID +" = "+COLUMN_USERNAME_ID +
                        " WHERE " + COLUMN_USERNAME + " LIKE " + "'%" + usertop + "%'" +
                        " ORDER BY " + COLUMN_SCORE + " DESC LIMIT 10";

        Cursor cursor = db.rawQuery(queryAllScores, null);
        if (cursor.moveToNext()) {
            do{
                int scoreId = cursor.getInt(0);
                Integer score = cursor.getInt(1);
                String datetime = cursor.getString(2);
                Float duration = cursor.getFloat(3);
                String username = cursor.getString(4);
                String imagePath = cursor.getString(5);
                String country = cursor.getString(6);
                ScoreDisplay scoreDisplay = new ScoreDisplay(scoreId, score, datetime, duration, username, imagePath, country);
                getAllScores.add(scoreDisplay);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return  getAllScores;
    }
    
    /**
     * We delete a Score by ID
     * Method used on RecyclerView
     * @param id to be deleted
     */
    public void deleteScoreByID(Integer id){
        checkDbStatus();
        String queryDeleteByID = "DELETE FROM " + SCORE_TABLE + " WHERE " + COLUMN_ID + " = " + id;
        db.execSQL(queryDeleteByID);
    }

    /**
     * We search in DB the topScore
     * If there's no topScore, return 20
     * @return topScore
     */
    public int getTopScore(){
        checkDbStatus();
        Integer topScore;
        String queryTopScore = "SELECT MAX(" + COLUMN_SCORE + ") FROM "+ SCORE_TABLE;
        Cursor cursor = db.rawQuery(queryTopScore, null);
        cursor.moveToFirst();
        topScore = cursor.getInt(0);
        if (topScore == 0 || topScore == null){
            return 20;
        }
        else{
            return topScore;
        }
    }

    /**
     * Check if user exists in DB
     * If it doesn't, create it
     */
    public int UserInDB(String username){
        checkDbStatus();
        int userId;
        username = username.toLowerCase();
        String queryUserExist = "SELECT "+COLUMN_ID+" FROM "+ USER_TABLE + " WHERE " + COLUMN_USERNAME + " = '" + username + "'";
        Cursor cursor = db.rawQuery(queryUserExist, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            userId = cursor.getInt(0);
            System.out.println("USER WAS ALREADY CREATED: "+username + " with the following id: "+userId);
        }
        else{
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USERNAME, username);
            cv.put(COLUMN_COUNTRY, "Unknown country");
            db.insert(USER_TABLE, null, cv);
            String queryLastID = "SELECT MAX(" + COLUMN_ID + ") FROM "+USER_TABLE;
            cursor = db.rawQuery(queryLastID, null);
            cursor.moveToFirst();
            userId = cursor.getInt(0);

            System.out.println("USER HAS BEEN CREATED: "+username + " with the following id: "+ userId);
        }
        cursor.close();
        return userId;
    }

    /**
     * UPDATE SCORE
     * @param id ID To update
     * @param username (We will get the new ID in case it's not registered in DB)
     * @param score Score to update
     * @param datetime Date to update
     * @param duration Duration to update
     */
    public void updateScore(int id, String username, Integer score, String datetime, Float duration){
        checkDbStatus();
        String updateScoreByID = "UPDATE " + SCORE_TABLE
                + " SET " + COLUMN_USERNAME_ID + " = '" + UserInDB(username) + "' , " +
                COLUMN_SCORE + " = " + score + ", " +
                COLUMN_DATETIME + " = '" + datetime + "', " +
                COLUMN_DURATION + " = " + duration +
                " WHERE " + COLUMN_ID + " = " + id;
        db.execSQL(updateScoreByID);
    }

    /**
     * UPDATE USER
     * @param username Parameter of WHERE
     * @param avatar Avatar to update. If not specified, another query is executed
     * @param country Country to update
     */
    public void updateUser(String username, String avatar, String country){
        checkDbStatus();
        String updateUser;
        username = username.toLowerCase();
        if (avatar != null){
            updateUser =  "UPDATE " + USER_TABLE
                    + " SET " + COLUMN_COUNTRY + " = '" + country + "' , " +
                    COLUMN_IMAGE + " = '" + avatar +
                    "' WHERE " + COLUMN_USERNAME + " = '" + username + "'";}
        else{
            updateUser =  "UPDATE " + USER_TABLE
                    + " SET " + COLUMN_COUNTRY + " = '" + country +
                    "' WHERE " + COLUMN_USERNAME + " = '" + username + "'";
        }
        db.execSQL(updateUser);
    }
}


package com.codebinars.a2048game.databaseRoom;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.UUID;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "scores",
        foreignKeys = {
                @ForeignKey(entity = UserModel.class,
                            parentColumns = "user_id",
                            childColumns = "score_username_id")})
public class ScoreModel {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "score_id")
    protected String mId;

    @ColumnInfo(name = "score_value")
    protected Integer mScore;

    @ColumnInfo(name = "score_username_id")
    protected String mUsername;

    @ColumnInfo(name = "score_datetime")
    protected String mDateTime;

    @ColumnInfo(name = "score_duration")
    protected Float mDuration;

    @Ignore
    public ScoreModel(String username, Integer score, String dateTime, Float duration) {
        mId = UUID.randomUUID().toString();
        mScore = score;
        mUsername = username;
        mDateTime = dateTime;
        mDuration = duration;
    }

    public ScoreModel(String mId, Integer score, String mUsername, String mDateTime, Float mDuration) {
        this.mId = mId;
        this.mScore = score;
        this.mUsername = mUsername;
        this.mDateTime = mDateTime;
        this.mDuration = mDuration;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public Integer getmScore() {
        return mScore;
    }

    public void setmScore(Integer mScore) {
        this.mScore = mScore;
    }

    public String getmUsername() {
        return mUsername;
    }

    public void setmUsername(String mUsername) {
        this.mUsername = mUsername;
    }

    public String getmDateTime() {
        return mDateTime;
    }

    public void setmDateTime(String mDateTime) {
        this.mDateTime = mDateTime;
    }

    public Float getmDuration() {
        return mDuration;
    }

    public void setmDuration(Float mDuration) {
        this.mDuration = mDuration;
    }

    @Override
    public String toString() {
        return "ScoreModel{" +
                "mId='" + mId + '\'' +
                ", mUsername='" + mUsername + '\'' +
                ", mDateTime='" + mDateTime + '\'' +
                ", mDuration=" + mDuration +
                '}';
    }
}

package com.codebinars.a2048game.databaseRoom;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ScoreModel.class, UserModel.class}, version = 2, exportSchema = false)
public abstract class ScoreRoomDB extends RoomDatabase {
    private static ScoreRoomDB database;
    public static String DATABASE_NAME = "scoreDB";

    public synchronized static ScoreRoomDB getInstance(Context context){
        //Check
        if(database == null){
            database = Room.databaseBuilder(context.getApplicationContext(),
                    ScoreRoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;
    }
    public abstract ScoreDao scoreDao();
}


package com.codebinars.a2048game.databaseRoom;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface ScoreDao {

    //Insert query
    @Insert(onConflict = REPLACE)
    void addScore(ScoreModel scoreModel);

    //Check if user exists. In case it doesn't, create it
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long addUser(UserModel userModel); // 1 = Created, 0 = Ignored

    //Delete query
    @Delete
    void delete(ScoreModel scoreModel);

    //Delete by id query
    @Query("DELETE FROM scores WHERE score_id = :scoreId")
    void deleteScoreByID(int scoreId);

    //Delete all query
    @Delete
    void deleteAllData(List<ScoreModel> scoreModelList, List<UserModel> userModelList);

    //Update user
    @Query("UPDATE users SET user_name = :uName, user_avatar = :uAvatar, user_country = :uCountry WHERE user_id = :uID")
    void updateUser(int uID, String uName, String uAvatar, String uCountry);

    //Update score
    @Query("UPDATE scores SET score_username_id = :sUsername, score_datetime = :sDateTime, score_duration = :sDuration WHERE score_id = :sID")
    void updateScore(int sID, String sUsername, String sDateTime, Float sDuration);

    //Select All
    @Query("SELECT * FROM scores ORDER BY score_value DESC")
    List<ScoreModel> getAllScores();

    //Top 10
    @Query("SELECT * FROM scores ORDER BY score_value DESC LIMIT 10")
    List<ScoreModel> getTop10Scores();

    //Top 10 by User
    @Query("SELECT * FROM scores WHERE score_username_id = :qUser ORDER BY score_value DESC")
    List<ScoreModel> getTop10ByUser(String qUser);

    //Top by Score
    @Query("SELECT MAX (score_value) FROM scores")
    int getTopScore();
}


package com.codebinars.a2048game.databaseRoom;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.codebinars.a2048game.databaseRoom.*;

import java.util.UUID;

@Entity(tableName = "users")
public class UserModel {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_id")
    protected String uId;

    @ColumnInfo(name = "user_name")
    protected String uName;

    @ColumnInfo(name = "user_country")
    protected String uCountry;

    @ColumnInfo(name = "user_avatar")
    protected String uAvatarpath;

    @Ignore
    public UserModel(@NonNull String uId, String uName, String uCountry, String uAvatarpath) {
        this.uId = UUID.randomUUID().toString();
        this.uName = uName;
        this.uCountry = uCountry;
        this.uAvatarpath = uAvatarpath;
    }

    public UserModel(String uName, String uCountry, String uAvatarpath) {
        this.uName = uName;
        this.uCountry = uCountry;
        this.uAvatarpath = uAvatarpath;
    }

    @NonNull
    public String getuId() {
        return uId;
    }

    public void setuId(@NonNull String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuCountry() {
        return uCountry;
    }

    public void setuCountry(String uCountry) {
        this.uCountry = uCountry;
    }

    public String getuAvatarpath() {
        return uAvatarpath;
    }

    public void setuAvatarpath(String uAvatarpath) {
        this.uAvatarpath = uAvatarpath;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "uId='" + uId + '\'' +
                ", uName='" + uName + '\'' +
                ", uCountry='" + uCountry + '\'' +
                ", uAvatarpath='" + uAvatarpath + '\'' +
                '}';
    }
}


package com.codebinars.a2048game.scoresView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.codebinars.a2048game.R;

public class ScoresViewHolder extends RecyclerView.ViewHolder{
    public TextView username, score, datetime, duration, country;
    public ImageView editImage, deleteImage, tweetImage, avatarImage;



    /**
     * View holder of my Score textView items
     * @param itemView
     */
    public ScoresViewHolder(View itemView, final OnItemClickListener listener){
        super(itemView);
        this.username = itemView.findViewById(R.id.playerRecycler);
        this.score = itemView.findViewById(R.id.scoreRecycler);
        this.datetime = itemView.findViewById(R.id.dateRecycler);
        this.duration = itemView.findViewById(R.id.durationRecycler);
        this.country = itemView.findViewById(R.id.countryRecycler);
        this.deleteImage = itemView.findViewById(R.id.imageDelete);
        this.editImage = itemView.findViewById(R.id.imageEdit);
        this.tweetImage = itemView.findViewById(R.id.tweetIt);
        this.avatarImage = itemView.findViewById(R.id.avatarItem);

        deleteImage.setOnClickListener(v -> {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(position);
                }
            }
        });

        editImage.setOnClickListener(v -> {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(position);
                }
            }

        });

        tweetImage.setOnClickListener(v -> {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTweetClick(position);
                }
            }
        });
    }
}


package com.codebinars.a2048game.scoresView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codebinars.a2048game.R;
import com.codebinars.a2048game.database.ScoreDisplay;

import java.util.ArrayList;

public class ScoreListAdapter extends RecyclerView.Adapter<ScoresViewHolder>  {

    public ArrayList<ScoreDisplay> playersList;
    public OnItemClickListener itemListener;

    public void setOnItemclickListener(OnItemClickListener listener){
        itemListener = listener;
    }

    public ScoreListAdapter(ArrayList<ScoreDisplay> playersList) {
        this.playersList = playersList;
    }
    @NonNull
    @Override
    public ScoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_score,null,false);
        ScoresViewHolder sch = new ScoresViewHolder(view, itemListener);
        return sch;
    }

    @Override
    public void onBindViewHolder(@NonNull ScoresViewHolder holder, int position) {
        holder.score.setText(playersList.get(position).getScore().toString());
        holder.username.setText(playersList.get(position).getUsername());
        holder.datetime.setText(playersList.get(position).getDatetime());
        holder.duration.setText(playersList.get(position).getDuration().toString());
        holder.country.setText(playersList.get(position).getCountry());
        if(playersList.get(position).getAvatar() !=null && playersList.get(position).getAvatar().length() > 5){
            holder.avatarImage.setImageBitmap(ImageUtils.loadImage(playersList.get(position).getAvatar()));
        }
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }


}

package com.codebinars.a2048game.scoresView;

public interface OnItemClickListener {
    void onEditClick(int position);
    void onDeleteClick(int position);
    void onTweetClick(int position);
}



package com.codebinars.a2048game.scoresView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codebinars.a2048game.EditScoreActivity;
import com.codebinars.a2048game.R;
import com.codebinars.a2048game.database.DBHelper;
import com.codebinars.a2048game.database.ScoreDisplay;
import static com.codebinars.a2048game.scoresView.ScoreConstants.*;

import java.util.ArrayList;
import java.util.Comparator;

public class ScoreListRecycler extends Activity implements AdapterView.OnItemSelectedListener{
    private final int SORT_BY_USERNAME=  0;
    private final int SORT_BY_SCORE =  1;
    private final int SORT_BY_DURATION =  2;
    private final int SORT_BY_DATETIME =  3;
    private final String TWEET_INTENT = "https://twitter.com/intent/tweet?text=In Rionacko's 2048 game, I have achieved the score of ";

    private ArrayList<ScoreDisplay> listScores;
    private RecyclerView recyclerViewScores;
    private DBHelper dbHelper;
    private ScoreListAdapter adapter;
    private EditText usertop10, filterScoreNumber;
    private Spinner spinnerScore;
    private Intent myIntent;
    private boolean sortedByUsername = false;
    private boolean sortedByScore = false;
    private boolean sortedByDuration = false;
    private boolean sortedByDate = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_score_recycler);
        dbHelper = DBHelper.getInstance(getApplicationContext());
        listScores = new ArrayList<>();
        recyclerViewScores = findViewById(R.id.recyclerScores);
        recyclerViewScores.setLayoutManager(new LinearLayoutManager(this));
        checkScoreList();
        adapter = new ScoreListAdapter(listScores);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewScores);
        recyclerViewScores.setAdapter(adapter);
        adapter.setOnItemclickListener(new OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeItem(position);
            }

            @Override
            public void onEditClick(int position) {
                myIntent = new Intent(ScoreListRecycler.this, EditScoreActivity.class);
                //Send score data
                myIntent.putExtra(SCORE_ID, listScores.get(position).getID());
                myIntent.putExtra(SCORE_VALUE, listScores.get(position).getScore().toString());
                myIntent.putExtra(SCORE_DATETIME, listScores.get(position).getDatetime());
                myIntent.putExtra(SCORE_DURATION, listScores.get(position).getDuration().toString());
                myIntent.putExtra(USER_NAME, listScores.get(position).getUsername());
                myIntent.putExtra(USER_COUNTRY, listScores.get(position).getCountry());
                myIntent.putExtra(USER_AVATAR, listScores.get(position).getAvatar());
                startActivity(myIntent);
                finish();
            }

            @Override
            public void onTweetClick(int position) {
                int tweetScore = listScores.get(position).getScore();
                String tweetUrl = TWEET_INTENT +tweetScore;
                Uri uri = Uri.parse(tweetUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });

        //SCORE Filtering: Smaller than, equals to, bigger than
        Spinner scoreSpinner = findViewById(R.id.scoreSpinner);
        ArrayAdapter<CharSequence> scoreAdapter = ArrayAdapter.createFromResource(this, R.array.sortScoreValues, R.layout.support_simple_spinner_dropdown_item);
        scoreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scoreSpinner.setAdapter(scoreAdapter);
        scoreSpinner.setOnItemSelectedListener(this);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) { //Type of movement, DIRECTION
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            dbHelper.deleteScoreByID(listScores.get(viewHolder.getAdapterPosition()).getID());
            listScores.remove(viewHolder.getAdapterPosition());
            adapter.notifyDataSetChanged();
        }
    };

    private void removeItem(int position) {
        dbHelper.deleteScoreByID(listScores.get(position).getID());
        listScores.remove(position);
        adapter.notifyItemRemoved(position);

    }

    /**
     * Method to load ScoreList
     */
    private void checkScoreList() {
        listScores = (ArrayList<ScoreDisplay>) dbHelper.getAllScores();
    }

    /**
     * Method for sorting ArrayList of ScoreModel
     * @param option Option clicked
     */
    private void scoreSort(int option){
        switch (option){
            case 0:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!sortedByUsername){
                        listScores.sort(Comparator.comparing(ScoreDisplay::getUsername));
                        sortedByUsername = true;}
                    else{
                        listScores.sort(Comparator.comparing(ScoreDisplay::getUsername).reversed());
                        sortedByUsername = false;
                    }
                }
                break;
            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!sortedByScore){
                        listScores.sort(Comparator.comparing(ScoreDisplay::getScore));
                        sortedByScore = true;}
                    else{
                        listScores.sort(Comparator.comparing(ScoreDisplay::getScore).reversed());
                        sortedByScore = false;
                    }
                }
                break;
            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!sortedByDuration){
                        listScores.sort(Comparator.comparing(ScoreDisplay::getDuration));
                        sortedByDuration = true;}
                    else{
                        listScores.sort(Comparator.comparing(ScoreDisplay::getDuration).reversed());
                        sortedByDuration = false;
                    }
                }
                break;
            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!sortedByDate){
                        listScores.sort(Comparator.comparing(ScoreDisplay::getDatetime));
                        sortedByDate = true;}
                    else{
                        listScores.sort(Comparator.comparing(ScoreDisplay::getDatetime).reversed());
                        sortedByDate = false;
                    }
                }
                break;
        }
        adapter.playersList = listScores;
        recyclerViewScores.setAdapter(adapter);
    }

    /**
     * Filter ScoreArrayList by Score nº
     */
    public void filterByScore(String option, int filterScore){
         if (option.equals("Bigger than")){
            for (int i = 0; i < listScores.size(); i++) {
                if (listScores.get(i).getScore() <= filterScore){
                    listScores.remove(i--);
                }
            }
        }
        else if (option.equals("Smaller than")){
            for (int i = 0; i < listScores.size(); i++) {
                if (listScores.get(i).getScore() >= filterScore){
                    listScores.remove(i--);
                }
            }
        }
        else{
            for (int i = 0; i < listScores.size(); i++) {
                if (listScores.get(i).getScore() != filterScore){
                    listScores.remove(i--);
                }
            }
        }
        adapter.playersList = listScores;
        recyclerViewScores.setAdapter(adapter);
    }

    /**
     * Buttons on RecyclerView layout
     * @param view
     */
    public void onClick(View view){
        switch (view.getId()){
            case R.id.top10:
                listScores = (ArrayList<ScoreDisplay>) dbHelper.getTop10Scores();
                adapter.playersList = listScores;
                recyclerViewScores.setAdapter(adapter);
                break;
            case R.id.top10byuser:
                usertop10 = findViewById(R.id.textusertop10);
                String username = usertop10.getText().toString();
                listScores = (ArrayList<ScoreDisplay>) dbHelper.getTop10ByUser(username);
                adapter.playersList = listScores;
                recyclerViewScores.setAdapter(adapter);
                break;
            case R.id.resetFilters:
                checkScoreList();
                adapter.playersList = listScores;
                recyclerViewScores.setAdapter(adapter);
                break;
            case R.id.filterButton:
                filterScoreNumber = findViewById(R.id.scoreFilter);
                int scoreForFilter = Integer.parseInt(filterScoreNumber.getText().toString());
                spinnerScore = findViewById(R.id.scoreSpinner);
                String scoreOperator = spinnerScore.getSelectedItem().toString();
                filterByScore(scoreOperator, scoreForFilter);
            case R.id.sortByUsername:
                scoreSort(SORT_BY_USERNAME);
                break;
            case R.id.sortByScore:
                scoreSort(SORT_BY_SCORE);
                break;
            case R.id.sortByDuration:
                scoreSort(SORT_BY_DURATION);
                break;
            case R.id.sortByDatetime:
                scoreSort(SORT_BY_DATETIME);
                break;
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                   }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


package com.codebinars.a2048game.scoresView;

public class ScoreConstants {
    public static final String SCORE_ID = "Score_id";
    public static final String SCORE_VALUE = "Score_value";
    public static final String SCORE_DURATION = "Score_duration";
    public static final String SCORE_DATETIME = "Score_datetime";
    public static final String USER_NAME = "User_name";
    public static final String USER_COUNTRY = "User_country";
    public static final String USER_AVATAR = "User_avatar";
}


package com.codebinars.a2048game.scoresView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class ImageUtils {
    public static Bitmap loadImage(String imageRoot){
        Bitmap bitmap;
        try {
            File file = new File (imageRoot);
            FileInputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream);
        } catch (Exception e) {
            bitmap = null;
        }
        return bitmap;
    }
}


package com.codebinars.a2048game.engine;

public interface GameTaskCallback {
    void gameOver();
    void updateScore(int delta);
    void reached2048();
}


package com.codebinars.a2048game.engine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.codebinars.a2048game.R;
import com.codebinars.a2048game.engine.sprites.Sprite;
import com.codebinars.a2048game.engine.sprites.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TileManager implements TileManagerCallback, Sprite {
    private Resources resources;
    private int standardSize, screenWidth, screenHeight;
    private ArrayList<Integer> drawables = new ArrayList<>();
    private HashMap<Integer, Bitmap> tileBitmaps = new HashMap<>();
    private Tile[][] matrix = new Tile[4][4];
    private Tile[][] backupMatrix = new Tile[4][4];
    private boolean moving = false;
    public ArrayList<Tile> movingTiles;
    private boolean toSpawn = false;
    private boolean endGame = false;
    private GameTaskCallback callback;

    public TileManager(Resources resources, int standardSize, int screenWidth, int screenHeight, GameTaskCallback callback) {
        this.resources = resources;
        this.standardSize = standardSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.callback = callback;
        initBitmaps();
        initGame();
    }

    /**
     * Initialize drawables of Tile values
     */
    private void initBitmaps(){
        drawables.add(R.drawable.one);
        drawables.add(R.drawable.two);
        drawables.add(R.drawable.three);
        drawables.add(R.drawable.four);
        drawables.add(R.drawable.five);
        drawables.add(R.drawable.six);
        drawables.add(R.drawable.seven);
        drawables.add(R.drawable.eight);
        drawables.add(R.drawable.nine);
        drawables.add(R.drawable.ten);
        drawables.add(R.drawable.eleven);
        drawables.add(R.drawable.twelve);
        drawables.add(R.drawable.thirteen);
        drawables.add(R.drawable.fourteen);
        drawables.add(R.drawable.fifteen);
        drawables.add(R.drawable.sixteen);

        for(int i=1; i<=16; i++){
            Bitmap bmp = BitmapFactory.decodeResource(resources, drawables.get(i-1));
            Bitmap tileBmp = Bitmap.createScaledBitmap(bmp, standardSize, standardSize,false);
            tileBitmaps.put(i,tileBmp);
        }
    }

    /**
     * Once we init a game, create a new matrix and backup matrix.
     * Generate 2 random tiles on random positions
     */
    public void initGame() {
        matrix = new Tile[4][4];
        backupMatrix = new Tile[4][4];
        movingTiles = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            int x = new Random().nextInt(4);
            int y = new Random().nextInt(4);
            if (matrix[x][y] == null) {
                Tile tile = new Tile(standardSize, screenWidth, screenHeight, this, x, y);
                matrix[x][y] = tile;
            } else {
                i--;
            }
        }
    }

    @Override
    public Bitmap getBitmap(int count) {
        return tileBitmaps.get(count);
    }

    @Override
    public void draw(Canvas canvas) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (matrix[i][j] != null) {
                    matrix[i][j].draw(canvas);
                }
            }
        }
        if (endGame) {
            callback.gameOver();
        }
    }

    @Override
    public void update() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (matrix[i][j] != null) {
                    matrix[i][j].update();
                }
            }
        }
    }


    /**
     * Algorithm to move and remove tiles
     * Save a backup on start
     * @param direction to determine the case
     */
    public void onSwipe(SwipeCallback.Direction direction) {
        if (!moving) {
            backupMatrix();
            Tile[][] newMatrix = new Tile[4][4];
            switch (direction) {
                case UP:
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            if (matrix[i][j] != null) {
                                newMatrix[i][j] = matrix[i][j];
                                for (int k = i - 1; k >= 0; k--) {
                                    if (newMatrix[k][j] == null) {
                                        newMatrix[k][j] = matrix[i][j];
                                        if (newMatrix[k + 1][j] == matrix[i][j]) {
                                            newMatrix[k + 1][j] = null;
                                        }
                                    } else if (newMatrix[k][j].getValue() == matrix[i][j].getValue() && !newMatrix[k][j].toIncrement()) {
                                        newMatrix[k][j] = matrix[i][j].increment();
                                        if (newMatrix[k + 1][j] == matrix[i][j]) {
                                            newMatrix[k + 1][j] = null;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            Tile t = matrix[i][j];
                            Tile newT = null;
                            int matrixX = 0;
                            int matrixY = 0;
                            for (int a = 0; a < 4; a++) {
                                for (int b = 0; b < 4; b++) {
                                    if (newMatrix[a][b] == t) {
                                        newT = newMatrix[a][b];
                                        matrixX = a;
                                        matrixY = b;
                                        break;
                                    }
                                }
                            }
                            if (newT != null) {
                                movingTiles.add(t);
                                t.move(matrixX, matrixY);
                            }
                        }
                    }
                    break;

                case DOWN:
                    for (int i = 3; i >= 0; i--) {
                        for (int j = 0; j < 4; j++) {
                            if (matrix[i][j] != null) {
                                newMatrix[i][j] = matrix[i][j];
                                for (int k = i + 1; k < 4; k++) {
                                    if (newMatrix[k][j] == null) {
                                        newMatrix[k][j] = matrix[i][j];
                                        if (newMatrix[k - 1][j] == matrix[i][j]) {
                                            newMatrix[k - 1][j] = null;
                                        }
                                    } else if (newMatrix[k][j].getValue() == matrix[i][j].getValue() && !newMatrix[k][j].toIncrement()) {
                                        newMatrix[k][j] = matrix[i][j].increment();
                                        if (newMatrix[k - 1][j] == matrix[i][j]) {
                                            newMatrix[k - 1][j] = null;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    for (int i = 3; i >= 0; i--) {
                        for (int j = 0; j < 4; j++) {
                            Tile t = matrix[i][j];
                            Tile newT = null;
                            int matrixX = 0;
                            int matrixY = 0;
                            for (int a = 0; a < 4; a++) {
                                for (int b = 0; b < 4; b++) {
                                    if (newMatrix[a][b] == t) {
                                        newT = newMatrix[a][b];
                                        matrixX = a;
                                        matrixY = b;
                                        break;
                                    }
                                }
                            }
                            if (newT != null) {
                                movingTiles.add(t);
                                t.move(matrixX, matrixY);
                            }
                        }
                    }
                    break;

                case LEFT:
                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            if (matrix[i][j] != null) {
                                newMatrix[i][j] = matrix[i][j];
                                for (int k = j - 1; k >= 0; k--) {
                                    if (newMatrix[i][k] == null) {
                                        newMatrix[i][k] = matrix[i][j];
                                        if (newMatrix[i][k + 1] == matrix[i][j]) {
                                            newMatrix[i][k + 1] = null;
                                        }
                                    } else if (newMatrix[i][k].getValue() == matrix[i][j].getValue() && !newMatrix[i][k].toIncrement()) {
                                        newMatrix[i][k] = matrix[i][j].increment();
                                        if (newMatrix[i][k + 1] == matrix[i][j]) {
                                            newMatrix[i][k + 1] = null;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    for (int i = 0; i < 4; i++) {
                        for (int j = 0; j < 4; j++) {
                            Tile t = matrix[i][j];
                            Tile newT = null;
                            int matrixX = 0;
                            int matrixY = 0;
                            for (int a = 0; a < 4; a++) {
                                for (int b = 0; b < 4; b++) {
                                    if (newMatrix[a][b] == t) {
                                        newT = newMatrix[a][b];
                                        matrixX = a;
                                        matrixY = b;
                                        break;
                                    }
                                }
                            }
                            if (newT != null) {
                                movingTiles.add(t);
                                t.move(matrixX, matrixY);
                            }
                        }
                    }
                    break;

                case RIGHT:
                    for (int i = 0; i < 4; i++) {
                        for (int j = 3; j >= 0; j--) {
                            if (matrix[i][j] != null) {
                                newMatrix[i][j] = matrix[i][j];
                                for (int k = j + 1; k < 4; k++) {
                                    if (newMatrix[i][k] == null) {
                                        newMatrix[i][k] = matrix[i][j];
                                        if (newMatrix[i][k - 1] == matrix[i][j]) {
                                            newMatrix[i][k - 1] = null;
                                        }
                                    } else if (newMatrix[i][k].getValue() == matrix[i][j].getValue() && !newMatrix[i][k].toIncrement()) {
                                        newMatrix[i][k] = matrix[i][j].increment();
                                        if (newMatrix[i][k - 1] == matrix[i][j]) {
                                            newMatrix[i][k - 1] = null;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    for (int i = 0; i < 4; i++) {
                        for (int j = 3; j >= 0; j--) {
                            Tile t = matrix[i][j];
                            Tile newT = null;
                            int matrixX = 0;
                            int matrixY = 0;
                            for (int a = 0; a < 4; a++) {
                                for (int b = 0; b < 4; b++) {
                                    if (newMatrix[a][b] == t) {
                                        newT = newMatrix[a][b];
                                        matrixX = a;
                                        matrixY = b;
                                        break;
                                    }
                                }
                            }
                            if (newT != null) {
                                movingTiles.add(t);
                                t.move(matrixX, matrixY);
                            }
                        }
                    }

                    break;
            }
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (newMatrix[i][j] != matrix[i][j]) {
                        toSpawn = true;
                        break;
                    }
                }
            }
            matrix = newMatrix;
        }
    }


    @Override
    public void finishedMoving(Tile t) {
        movingTiles.remove(t);
        if (movingTiles.isEmpty()) {
            moving = false;
            spawn();
            checkEndgame();
        }
    }

    /**
     * Method to check if game is over
     * First: Check if we can add more values to Tiles
     * Second: Check if there are value matches (Skip if First case is possible)
     */
    private synchronized void checkEndgame() {
        endGame = true;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (matrix[i][j] == null) {
                    endGame = false;
                    break;
                }
            }
        }
        if (endGame) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if ((i > 0 && matrix[i - 1][j].getValue() == matrix[i][j].getValue()) ||
                            (i < 3 && matrix[i + 1][j].getValue() == matrix[i][j].getValue()) ||
                            (j > 0 && matrix[i][j - 1].getValue() == matrix[i][j].getValue()) ||
                            (j < 3 && matrix[i][j + 1].getValue() == matrix[i][j].getValue())) {
                        endGame = false;
                    }
                }
            }
        }
    }

    /**
     * Save a copy of matrix before movement
     */
    public void backupMatrix() {
        for (int i = 0; i < matrix.length; i++){
            for (int j = 0; j < matrix[0].length; j++){
                backupMatrix[i][j] = matrix[i][j];
            }
        }
    }

    /**
     * Restore matrix to State before movement
     */
    public void restoreMatrix() {
        for (int i = 0; i < matrix.length; i++){
            for (int j = 0; j < matrix[0].length; j++){
                matrix[i][j] = backupMatrix[i][j];
                Tile t = matrix[i][j];
                if(t != null){
                    if (t.isWasIncremented()){ //¿Was it incremented after moving?
                        t.setCount(t.getCount()-1);
                    }
                    movingTiles.add(t);
                    t.move(i,j);
                }
            }
        }
    }


    /**
     * Spawn a Number in random location.
     * If location has a value assigned in matrix, try again.
     */
    private void spawn() {
        if (toSpawn) {
            toSpawn = false;
            Tile t = null;
            while (t == null) {
                int x = new Random().nextInt(4);
                int y = new Random().nextInt(4);
                if (matrix[x][y] == null) {
                    t = new Tile(standardSize, screenWidth, screenHeight, this, x, y);
                    matrix[x][y] = t;
                }
            }
        }
    }
    @Override
    public void updateScore(int delta) {
        callback.updateScore(delta);
    }

    @Override
    public void reached2048() {
        callback.reached2048();
    }
}

package com.codebinars.a2048game.engine;

public interface SwipeCallback {
    void onSwipe(Direction direction);

    enum Direction{
        LEFT,
        RIGHT,
        UP,
        DOWN
    }
}


package com.codebinars.a2048game.engine;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowInsets;
import android.view.WindowMetrics;


import androidx.annotation.NonNull;

import com.codebinars.a2048game.R;
import com.codebinars.a2048game.database.DBHelper;
import com.codebinars.a2048game.database.ScoreModel;
import com.codebinars.a2048game.engine.sprites.EndGame;
import com.codebinars.a2048game.engine.sprites.Grid;
import com.codebinars.a2048game.engine.sprites.Score;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.codebinars.a2048game.scoresView.ScoreConstants.USER_NAME;

public class GameTask extends SurfaceView implements SurfaceHolder.Callback, SwipeCallback, GameTaskCallback {

    private MainThread thread;
    private Grid grid;
    private int scWidth, scHeight, standardSize;
    private TileManager tileManager;
    private boolean endGame = false;
    private EndGame endgameSprite;
    private Score score;
    private Bitmap restartButton, undomovementButton, bmpCopyright;
    private int restartButtonX, restartButtonY, undoMovementX, undoMovementY;
    private DBHelper dbHelper;
    private Boolean scoreSaved = false;
    private String username;
    private int buttonHeight, buttonWidth;
    private SwipeListener swipe;
    private SimpleDateFormat dateFormat;
    private Calendar cal;

    public GameTask(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        setLongClickable(true);
        getHolder().addCallback(this);

        //Get username
        Activity activity = (Activity) context;
        Bundle extras = activity.getIntent().getExtras();
        username = extras.getString(USER_NAME);
        cal = new GregorianCalendar();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(cal.getTimeZone());

        swipe = new SwipeListener(getContext(), this);
        scWidth = getScreenWidth((Activity)context);
        scHeight = getScreenHeight((Activity)context);
        standardSize = (int) (scWidth*0.88)/4;
        dbHelper = DBHelper.getInstance((Activity) context);

        grid = new Grid(getResources(),scWidth,scHeight,standardSize);
        tileManager = new TileManager(getResources(), standardSize, scWidth, scHeight, this);
        endgameSprite = new EndGame(getResources(), scWidth, scHeight);
        score = new Score(getResources(), scWidth, scHeight, standardSize, dbHelper, username);

        buttonWidth = (int) getResources().getDimension(R.dimen.button_width);
        buttonHeight = (int) getResources().getDimension(R.dimen.button_height);
        Bitmap bmpRestart = BitmapFactory.decodeResource(getResources(), R.drawable.restartgame);
        restartButton = Bitmap.createScaledBitmap(bmpRestart, buttonWidth, buttonHeight, false);
        restartButtonX = scWidth / 2 + 2 * standardSize - buttonWidth;
        restartButtonY = scHeight / 2 - 2 * standardSize - 3 * buttonHeight / 2;


        Bitmap bmpUndoMovement = BitmapFactory.decodeResource(getResources(), R.drawable.undomovement);
        undomovementButton = Bitmap.createScaledBitmap(bmpUndoMovement, buttonWidth, buttonHeight, false);
        undoMovementX = scWidth / 2 + 2 * standardSize - buttonWidth;
        undoMovementY = scHeight / 2 - 2 * standardSize - 6 * buttonHeight / 2;

        int copyrightWidth = 1000;
        int copyrightHeight = 200;
        Bitmap copyright = BitmapFactory.decodeResource(getResources(), R.drawable.copyright);
        bmpCopyright = Bitmap.createScaledBitmap(copyright, copyrightWidth, copyrightHeight, false);

    }
    /**
     * These methods are to check Android Version and, depending of the version,
     * Fix size with one resolution or the other.
     * API Level 13 deprecated getDefaultDisplay and added new functions
     * But we want to keep the methods in case Android version is lower
     */
    public static int getScreenWidth(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }

    public static int getScreenHeight(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().height() - insets.top - insets.bottom;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
    }



    public void initGame() {
        endGame = false;
        tileManager.initGame();
        score = new Score(getResources(), scWidth, scHeight, standardSize, dbHelper, username);
        scoreSaved = false;
        System.out.printf("Oh, thanks %s for playing the game",username);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new MainThread(holder,this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceHolder(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while(retry){
            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void update(){
        if (!endGame) {
            tileManager.update();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(getResources().getColor(R.color.bgColor));
        grid.draw(canvas);
        tileManager.draw(canvas);
        score.draw(canvas);
        canvas.drawBitmap(restartButton, restartButtonX, restartButtonY, null);
        canvas.drawBitmap(undomovementButton, undoMovementX, undoMovementY, null);
        canvas.drawBitmap(bmpCopyright, 3 * scWidth / 4 -  15 * bmpCopyright.getWidth() / 20, 20 * scHeight / 23, null);
        if (endGame) {
            endgameSprite.draw(canvas);
            endGame = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (endGame) {
                initGame();
        } else {
            float eventX = event.getAxisValue(MotionEvent.AXIS_X);
            float eventY = event.getAxisValue(MotionEvent.AXIS_Y);
            //Check if RESTARD GAME was pressed
            if (event.getAction() == MotionEvent.ACTION_DOWN && eventX > restartButtonX && eventX < restartButtonX + buttonWidth &&
                    eventY > restartButtonY && eventY < restartButtonY + buttonHeight) {
                initGame();
            }
            //Check if UNDO MOVEMENT was pressed
            if(event.getAction() == MotionEvent.ACTION_DOWN && eventX > undoMovementX && eventX < undoMovementX + buttonWidth &&
                    eventY > undoMovementY && eventY < undoMovementY + buttonHeight && score.getScore() > 0){
                    restoreBackup();
            }
            swipe.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    private void restoreBackup() {
        score.restoreScore();
        tileManager.restoreMatrix();
    }

    @Override
    public void onSwipe(Direction direction) {
        score.setBackupScore();
        tileManager.onSwipe(direction);
    }

    @Override
    public void gameOver() {
        endGame = true;
        if (!scoreSaved){
            saveScore();
            scoreSaved = true;
        }
    }

    @Override
    public void updateScore(int delta) {
        score.updateScore(delta);
    }

    @Override
    public void reached2048() {
        score.reached2048();
    }

    public void saveScore(){
        ScoreModel scoreModel = new ScoreModel();
        scoreModel.setScore(score.getScore());
        scoreModel.setUsernameId(dbHelper.UserInDB(username));
        scoreModel.setDatetime(dateFormat.format(cal.getTime()));
        scoreModel.setDuration(score.getCurrentTimeSeconds());
        dbHelper.addScore(scoreModel);
        System.out.println("Score saved: "+scoreModel);
        }
}

package com.codebinars.a2048game.engine;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread{

    private SurfaceHolder surfaceHolder;
    private GameTask gameTask;
    private int targetFPS = 60;
    private Canvas canvas;
    private Boolean running;

    public MainThread(SurfaceHolder surfaceHolder, GameTask gameTask){
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameTask = gameTask;
    }

    public void setRunning(Boolean isRunning){
        running = isRunning;
    }
    public void setSurfaceHolder(SurfaceHolder holder){
        surfaceHolder = holder;
    }

    @Override
    public void run() {
        long startTime, timeMillis,waitTime;
        long targetTime = 1000 / targetFPS;

        while(running){
            startTime = System.nanoTime();
            canvas = null;

            try{
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    gameTask.update();
                    try {
                        gameTask.draw(canvas);
                    } catch (Exception e) {
                        setRunning(false);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if(canvas!=null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000;
            waitTime = targetTime - timeMillis;

            try{
                if(waitTime > 0){
                    sleep(waitTime);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

package com.codebinars.a2048game.engine;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class SwipeListener implements GestureDetector.OnGestureListener{
    private GestureDetector detector;
    private SwipeCallback callback;

    public SwipeListener(Context context, SwipeCallback callback){
        this.callback = callback;
        detector = new GestureDetector(context,this);
    }

    public void onTouchEvent(MotionEvent e){
        detector.onTouchEvent(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(Math.abs(velocityX) > Math.abs(velocityY)){
            //Horizontal Fling
            if(velocityX > 5){
                callback.onSwipe(SwipeCallback.Direction.RIGHT);

            } else if(velocityX < 5){
                callback.onSwipe(SwipeCallback.Direction.LEFT);
            }
            //Vertical Fling
        } else{
            if(velocityY > 5){
                callback.onSwipe(SwipeCallback.Direction.DOWN);
            } else if(velocityY < 5){
                callback.onSwipe(SwipeCallback.Direction.UP);
            }


        }
        return false;
    }
}


package com.codebinars.a2048game.engine;

import android.graphics.Bitmap;

import com.codebinars.a2048game.engine.sprites.Tile;

public interface TileManagerCallback {
    Bitmap getBitmap(int count);
    void finishedMoving(Tile t);
    void updateScore(int delta);
    void reached2048();
}


package com.codebinars.a2048game.engine.sprites;

import android.graphics.Canvas;

public interface Sprite {
    void draw(Canvas canvas);
    void update();
}


package com.codebinars.a2048game.engine.sprites;

import android.graphics.Canvas;

import com.codebinars.a2048game.engine.TileManagerCallback;

import java.util.Random;

public class Tile implements Sprite {

    private int screenWidth, screenHeight, standardSize;
    private TileManagerCallback callback;
    private int count = 1;
    private int currentX, currentY;
    private int destX, destY;
    private boolean moving = false;
    private int speed = 75;
    private boolean increment = false;
    private boolean wasIncremented;

    public Tile(int standardSize, int screenWidth, int screenHeight, TileManagerCallback callback, int matrixX, int matrixY) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.standardSize = standardSize;
        this.callback = callback;
        currentX = destX = screenWidth / 2 - 2 * standardSize + matrixY * standardSize;
        currentY = destY = screenHeight / 2 - 2 * standardSize + matrixX * standardSize;
        int chance = new Random().nextInt(100);
        if (chance >= 80) {
            if (chance >= 98) {
                count = 5;
            } else if (chance >= 95) {
                count = 4;
            } else if (chance >= 90) {
                count = 3;
            } else {
                count = 2;
            }
        }
    }

    public void move(int matrixX, int matrixY) {
        moving = true;
        destX = screenWidth / 2 - 2 * standardSize + matrixY * standardSize;
        destY = screenHeight / 2 - 2 * standardSize + matrixX * standardSize;
    }

    public int getValue() {
        return count;
    }

    public Tile increment() {
        increment = true;
        return this;
    }

    public boolean toIncrement() {
        return increment;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(callback.getBitmap(count), currentX, currentY, null);
        if (moving && currentX == destX && currentY == destY) {
            moving = false;
            if (increment) {
                count++;
                increment = false;
                wasIncremented = true;
                int amount = (int) Math.pow(2, count);
                callback.updateScore(amount);
                if (count == 11) {
                    callback.reached2048();
                }
            }
            else{
                wasIncremented = false;
            }
        }
        callback.finishedMoving(this);
    }

    @Override
    public void update() {
        if (currentX < destX) {
            if (currentX + speed > destX) {
                currentX = destX;
            } else {
                currentX += speed;
            }
        } else if (currentX > destX) {
            if (currentX - speed < destX) {
                currentX = destX;
            } else {
                currentX -= speed;
            }
        }

        if (currentY < destY) {
            if (currentY + speed > destY) {
                currentY = destY;
            } else {
                currentY += speed;
            }
        } else if (currentY > destY) {
            if (currentY - speed < destY) {
                currentY = destY;
            } else {
                currentY -= speed;
            }
        }
    }

    public boolean isWasIncremented() {
        return wasIncremented;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

package com.codebinars.a2048game.engine.sprites;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.codebinars.a2048game.R;

public class Grid implements Sprite{
    private Bitmap grid;
    private int screenWidth, screenHeight, standardSize;

    public Grid(Resources resources, int screenWidth, int screenHeight, int standardSize ){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.standardSize = standardSize;

        Bitmap bmp = BitmapFactory.decodeResource(resources, R.drawable.grid);
        grid = Bitmap.createScaledBitmap(bmp,standardSize * 4,standardSize * 4,false);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(grid,screenWidth/2-grid.getWidth()/2,screenHeight/2-grid.getHeight()/2,null);
    }

    @Override
    public void update() {

    }

}


package com.codebinars.a2048game.engine.sprites;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import com.codebinars.a2048game.R;

public class EndGame implements Sprite {

    private int screenWidth, screenHeight;
    private Bitmap bmp;

    public EndGame(Resources resources, int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        int endgameWidth = (int) resources.getDimension(R.dimen.endgame_width);
        int endgameHeight = (int) resources.getDimension(R.dimen.endgame_height);

        Bitmap b = BitmapFactory.decodeResource(resources, R.drawable.gameover);
        bmp = Bitmap.createScaledBitmap(b, endgameWidth, endgameHeight, false);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bmp, screenWidth / 2 - bmp.getWidth() / 2, screenHeight / 2 - bmp.getHeight() / 2, null);
    }

    @Override
    public void update() {

    }
}

package com.codebinars.a2048game.engine.sprites;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.codebinars.a2048game.R;
import com.codebinars.a2048game.database.DBHelper;

public class Score implements Sprite {

    private Resources resources;
    private int screenWidth, screenHeight, standardSize;
    private Bitmap bmpScore, bmpTopScore, bmpUsertime;
    private Bitmap bmpTopScoreBonus, bmp2048Bonus;
    private int score, topScore, backupScore;
    private DBHelper dbHelper;
    private Paint paint;
    private boolean topScoreBonus = false;
    private boolean a2048Bonus = false;
    private String username;
    private long startTime;
    private long currentTimeMillis;
    private float currentTimeSeconds;

    public Score(Resources resources, int screenWidth, int screenHeight, int standardSize, DBHelper dbHelper, String username) {
        this.resources = resources;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.standardSize = standardSize;
        this.dbHelper = dbHelper;
        this.username = username;
        this.startTime = System.currentTimeMillis();

        topScore = dbHelper.getTopScore();
        int width = (int) resources.getDimension(R.dimen.score_label_width);
        int height = (int) resources.getDimension(R.dimen.score_label_height);

        Bitmap sc = BitmapFactory.decodeResource(resources, R.drawable.scoresquare);
        bmpScore = Bitmap.createScaledBitmap(sc, width, height, false);

        Bitmap tsc = BitmapFactory.decodeResource(resources, R.drawable.topscoresquare);
        bmpTopScore = Bitmap.createScaledBitmap(tsc, width, height, false);

        Bitmap ut = BitmapFactory.decodeResource(resources, R.drawable.usertimetable);
        bmpUsertime = Bitmap.createScaledBitmap(ut, (int) resources.getDimension(R.dimen.user_and_time_width), (int) resources.getDimension(R.dimen.user_and_time_height), false);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(resources.getDimension(R.dimen.score_text_size));
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bmpScore, screenWidth / 4 - bmpScore.getWidth() / 2, bmpScore.getHeight()/9, null);
        canvas.drawBitmap(bmpTopScore, (float) (3.1 * screenWidth / 4 - bmpTopScore.getWidth() / 2), bmpTopScore.getHeight()/9, null);
        canvas.drawBitmap(bmpUsertime, screenWidth / 2 - bmpUsertime.getWidth()/2, (float) (screenHeight / 1.35), null);

        int width1 = (int) paint.measureText(String.valueOf(score));
        int width2 = (int) paint.measureText(String.valueOf(topScore));

        currentTimeMillis = System.currentTimeMillis() - startTime;
        currentTimeSeconds = currentTimeMillis / 1000F;

        canvas.drawText(String.valueOf(score), screenWidth / 4 - width1 / 2, bmpScore.getHeight(), paint);
        canvas.drawText(String.valueOf(topScore), 3 * screenWidth / 4 - width2 / 2, bmpTopScore.getHeight(), paint);
        canvas.drawText((username), (float) (screenWidth / 3.4), (float) (screenHeight / 1.29), paint);
        canvas.drawText(String.valueOf((currentTimeSeconds)),(float) (screenWidth / 2.5), (float) (screenHeight / 1.215), paint);
        if (topScoreBonus) {
            canvas.drawBitmap(bmpTopScoreBonus, screenWidth / 2 - 2 * standardSize, screenHeight / 2 - 2 * standardSize - 2 * bmpTopScoreBonus.getHeight(), null);
        }
        if (a2048Bonus) {
            canvas.drawBitmap(bmp2048Bonus, screenWidth / 2 - 2 * standardSize, screenHeight / 2 - 2 * standardSize - 4 * bmp2048Bonus.getHeight(), null);
        }

    }

    @Override
    public void update() {
    }

    public void updateScore(int delta) {
        score += delta;
        checkTopScore();
    }

    /**
     * Check if score can be in Top 10 Scores
     */
    private void checkTopScore() {
        topScore = dbHelper.getTopScore();
        if (topScore < score) {
            topScore = score;

            int width = (int) resources.getDimension(R.dimen.score_bonus_width);
            int height = (int) resources.getDimension(R.dimen.score_bonus_height);
            Bitmap tsb = BitmapFactory.decodeResource(resources, R.drawable.highscore);
            bmpTopScoreBonus = Bitmap.createScaledBitmap(tsb, width, height, false);
            topScoreBonus = true;
        }
    }

    /**
     * Once 2048 is Reached, show the win
     */
    public void reached2048() {
        a2048Bonus = true;
        int width = (int) resources.getDimension(R.dimen.score_bonus_width);
        int height = (int) resources.getDimension(R.dimen.score_bonus_height);
        Bitmap r2048bmp = BitmapFactory.decodeResource(resources, R.drawable.a2048);
        bmp2048Bonus = Bitmap.createScaledBitmap(r2048bmp, width, height, false);

    }

    /**
     * Save a BackUp of the score
     */
    public void setBackupScore(){
        this.backupScore = score;
    }

    /**
     * Undo score changes of last movement
     */
    public void restoreScore(){
        this.score = backupScore;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public float getCurrentTimeSeconds() {
        return currentTimeSeconds;
    }


}

