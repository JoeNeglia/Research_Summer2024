package com.bcstudio.androidsqlitetoolboxexemple;

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

        assertEquals("com.bcstudio.androidsqlitetoolboxexemple", appContext.getPackageName());
    }
}


package com.bcstudio.androidsqlitetoolboxexemple;

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

package com.bcstudio.androidsqlitetoolboxexemple;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bcstudio.androidsqlitetoolbox.Constants;
import com.bcstudio.androidsqlitetoolbox.Database.Column;
import com.bcstudio.androidsqlitetoolbox.Database.DBHandler;
import com.bcstudio.androidsqlitetoolbox.Export.DBExporterJson;
import com.bcstudio.androidsqlitetoolbox.Export.ExportConfig;

public class SecondFragment extends Fragment {
    private DBHandler db;
    private String DEV_TEST_DB = "DEV_TEST_DB";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DBHandler(getContext(), DEV_TEST_DB, null, 1);

        view.findViewById(R.id.buttonExportDb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.exportDbToCSV();
                db.exportDbToJSON();

                /*try {
                    ExportConfig config = new ExportConfig(db.openDataBase(), DEV_TEST_DB, ". json", getContext());
                    DBExporterJson exporter = new DBExporterJson(config);
                    exporter.export();

                    config = new ExportConfig(db.openDataBase(), DEV_TEST_DB, ".j son", getContext());
                    exporter = new DBExporterJson(config);
                    exporter.export();

                    config = new ExportConfig(db.openDataBase(), DEV_TEST_DB, ".js7on", getContext());
                    exporter = new DBExporterJson(config);
                    exporter.export();

                    config = new ExportConfig(db.openDataBase(), DEV_TEST_DB, ".7", getContext());
                    exporter = new DBExporterJson(config);
                    exporter.export();

                    config = new ExportConfig(db.openDataBase(), DEV_TEST_DB, ".Json", getContext());
                    exporter = new DBExporterJson(config);
                    exporter.export();

                    config = new ExportConfig(db.openDataBase(), DEV_TEST_DB, ".jsoN", getContext());
                    exporter = new DBExporterJson(config);
                    exporter.export();

                    config = new ExportConfig(db.openDataBase(), DEV_TEST_DB, " .json ", getContext());
                    exporter = new DBExporterJson(config);
                    exporter.export();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
        });
        view.findViewById(R.id.buttonImportDb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.PACKAGE_NAME, "importing db...");
                if(db.importDataFromJSON())
                    Log.d(Constants.PACKAGE_NAME, "import finish");
                else
                    Log.w(Constants.PACKAGE_NAME, "import failed");
            }
        });
    }
}


package com.bcstudio.androidsqlitetoolboxexemple;

import android.database.Cursor;
import android.os.Bundle;

import com.bcstudio.androidsqlitetoolbox.Constants;
import com.bcstudio.androidsqlitetoolbox.Database.Column;
import com.bcstudio.androidsqlitetoolbox.Database.DBHandler;
import com.bcstudio.androidsqlitetoolbox.Database.Data;
import com.bcstudio.androidsqlitetoolbox.Database.Table;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class DBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_b);
        Button createDb = findViewById(R.id.buttonCreateDb);
        Button addTableDb = findViewById(R.id.buttonAddTableDb);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

}


package com.bcstudio.androidsqlitetoolboxexemple;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bcstudio.androidsqlitetoolbox.Constants;
import com.bcstudio.androidsqlitetoolbox.Database.Column;
import com.bcstudio.androidsqlitetoolbox.Database.DBHandler;
import com.bcstudio.androidsqlitetoolbox.Database.Data;
import com.bcstudio.androidsqlitetoolbox.Http.FileUploadService;

import okhttp3.ResponseBody;
import retrofit2.Call;

import java.io.FileNotFoundException;

public class FirstFragment extends Fragment {
    private DBHandler db;
    private String DEV_TEST_DB = "DEV_TEST_DB";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DBHandler(getContext(), DEV_TEST_DB, null, 1);
        db.addTable("Exemple1", new Column("Col1", "text"), new Column("Col2", "text"), new Column("Col3", "text"));
        db.addTable("Exemple2", new Column("Col1", "text"), new Column("Col2", "text"), new Column("Col3", "text"));
        db.addTable("Exemple3", new Column("Col1", "text"), new Column("Col2", "text"), new Column("Col3", "text"));

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        view.findViewById(R.id.buttonCreateDb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db.isTableEmpty("Exemple1") || db.isTableEmpty("Exemple2") || db.isTableEmpty("Exemple3")) {
                    db.deleteAllDataFrom("Exemple1");
                    db.deleteAllDataFrom("Exemple2");
                    db.deleteAllDataFrom("Exemple3");

                    db.addDataInTable("Exemple1", new Data("Col1", "Col1Data1"),
                            new Data("Col2", "Col2Data1"),
                            new Data("Col3", "Col3Data1"));
                    db.addDataInTable("Exemple1", new Data("Col1", "Col1Data1"),
                            new Data("Col2", "Col2Data1"),
                            new Data("Col3", "Col3Data1"));
                    db.addDataInTable("Exemple1", new Data("Col1", "Col1Data2"),
                            new Data("Col2", "Col2Data2"),
                            new Data("Col3", "Col3Data2"));

                    db.addDataInTable("Exemple2", new Data("Col1", "Col1Data1"),
                            new Data("Col2", "Col2Data1"),
                            new Data("Col3", "Col3Data1"));
                    db.addDataInTable("Exemple2", new Data("Col1", "Col1Data1"),
                            new Data("Col2", "Col2Data1"),
                            new Data("Col3", "Col3Data1"));
                    db.addDataInTable("Exemple2", new Data("Col1", "Col1Data2"),
                            new Data("Col2", "Col2Data2"),
                            new Data("Col3", "Col3Data2"));

                    db.addDataInTable("Exemple3", new Data("Col1", "Col1Data1"),
                            new Data("Col2", "Col2Data1"),
                            new Data("Col3", "Col3Data1"));
                    db.addDataInTable("Exemple3", new Data("Col1", "Col1Data1"),
                            new Data("Col2", "Col2Data1"),
                            new Data("Col3", "Col3Data1"));
                    db.addDataInTable("Exemple3", new Data("Col1", "Col1Data2"),
                            new Data("Col2", "Col2Data2"),
                            new Data("Col3", "Col3Data2"));
                }

                /*db.deleteAllDataFrom("Exemple1");
                db.deleteRow("Exemple2", 6);
                db.deleteRowWhere("Exemple2", new Data("Col1", "Col1Data1"));
                db.updateData("Exemple3", 6, new Data("Col2", "testUpdate"), new Data("Col3", "testUpdate2"));*/

                /*Cursor cr = db.getMultipleDataFromTable("Exemple1", "Col1", "Col3");
                Log.d(Constants.PACKAGE_NAME, "-> db.getMultipleDataFromTable(\"Exemple1\", \"Col1\", \"Col3\"");
                while(cr.moveToNext()){
                    for (int i=0; i<cr.getColumnCount(); i++)
                        Log.d(Constants.PACKAGE_NAME, cr.getString(i));
                }

                cr = db.getMultipleDataFromTableWhere("Exemple1", "Col1=\"Col1Data1\"", "Col1", "Col3");
                Log.d(Constants.PACKAGE_NAME, "-> db.getMultipleDataFromTableWhere(\"Exemple1\", \"Col1=\"Col1Data1\", \"Col1\", \"Col3\"");
                while(cr.moveToNext()){
                    for (int i=0; i<cr.getColumnCount(); i++)
                        Log.d(Constants.PACKAGE_NAME, cr.getString(i));
                }*/
            }
        });
        view.findViewById(R.id.buttonAddTableDb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db == null)
                    db = new DBHandler(getContext(), DEV_TEST_DB, null, 1);
                db.addTable("Test", new Column("Col1", "text"), new Column("Col2", "text"));
            }
        });
        view.findViewById(R.id.buttonSyncDb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db == null)
                    db = new DBHandler(getContext(), DEV_TEST_DB, null, 1){
                        // Enable to custom sync request
                        @Override
                        public Call<ResponseBody> requestBuilder(String jsonData) {
                            return super.requestBuilder(jsonData);
                        }
                    };
                try {
                    db.syncDb(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        view.findViewById(R.id.buttonDeleteData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db == null)
                    db = new DBHandler(getContext(), DEV_TEST_DB, null, 1);
                db.deleteAllDataFrom("Exemple1");
                db.deleteAllDataFrom("Exemple2");
                db.deleteAllDataFrom("Exemple3");
            }
        });
    }
}


package com.bcstudio.androidsqlitetoolbox;

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

        assertEquals("com.bcstudio.androidsqlitetoolbox.test", appContext.getPackageName());
    }
}


package com.bcstudio.androidsqlitetoolbox;

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

package com.bcstudio.androidsqlitetoolbox;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class FileUtils {

    public static String getAppDir(Context c){
        return c.getApplicationInfo().dataDir;
    }

    public static File createDirIfNotExist(String path){
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdir();
        }
        return dir;
    }

    /**
     *  Checks if external storage is available for read and write
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     *  Checks if external storage is available to at least read
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}


package com.bcstudio.androidsqlitetoolbox;

public class Constants {
    public static String PACKAGE_NAME = "androidsqlitetoolbox";
    public static String SYNC_URL = "";
}


package com.bcstudio.androidsqlitetoolbox.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Debug;
import android.util.Log;

import com.bcstudio.androidsqlitetoolbox.Constants;
import com.bcstudio.androidsqlitetoolbox.Exceptions.MissingSyncUrlException;
import com.bcstudio.androidsqlitetoolbox.Export.DBExporterCsv;
import com.bcstudio.androidsqlitetoolbox.Export.DBExporterJson;
import com.bcstudio.androidsqlitetoolbox.Export.ExportConfig;
import com.bcstudio.androidsqlitetoolbox.FileUtils;
import com.bcstudio.androidsqlitetoolbox.Http.FileUploadService;
import com.bcstudio.androidsqlitetoolbox.Http.ServiceGenerator;
import com.bcstudio.androidsqlitetoolbox.Import.DBImporterJson;
import com.bcstudio.androidsqlitetoolbox.Import.ImportConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

/**
 * Database helper class used for interact with db and table for operation like:
 * - insert data
 * - delete data
 * - select and get single or multiple data from tables with or without where arg
 * - update data
 * - create database and add tables
 * - export db
 * - sync db to remote api
 */

public class DBHandler extends SQLiteOpenHelper {
    private final String DB_NAME;

    private DatabaseErrorHandler dbErrHandler;
    private SQLiteDatabase.CursorFactory curFactory;
    private Context appContext;
    private int version;

    private ArrayList<Table> tables = new ArrayList<>();
    public ArrayList<Table> getTables() {
        return tables;
    }
    public void setTables(ArrayList<Table> tables) {
        this.tables = tables;
    }

    public DBHandler(Context context, String dbName, SQLiteDatabase.CursorFactory curFactory, int version) {
        super(context, dbName, curFactory, version);
        this.appContext = context;
        this.DB_NAME = dbName;
        this.curFactory = curFactory;
        this.version = version;

        refreshTablesSet(true);
    }

    public DBHandler(Context context, String dbName, SQLiteDatabase.CursorFactory curFactory, int version, DatabaseErrorHandler dbErrHandler) {
        super(context, dbName, curFactory, version);
        this.appContext = context;
        this.DB_NAME = dbName;
        this.curFactory = curFactory;
        this.version = version;
        this.dbErrHandler = dbErrHandler;

        refreshTablesSet(true);
    }

    /**
     * Update tables array attribute with sqlite_master data
     * Convert all existing tables to Table and Column objects and add them to tables property
     * Ignore sql tables (sqlite_sequence, android_metadata)
     *
     * @param override Allow function to override Table instance in tables if it already exist
     */
    public void refreshTablesSet(boolean override){
        Cursor c = openDataBase().rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name!='sqlite_sequence' AND name!='android_metadata'", null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String currentTableName = c.getString(c.getColumnIndex("name"));
                String sqlQuery = c.getString(c.getColumnIndex("sql"));

                String[] sqlQuerySplitted = sqlQuery.split("\\s+");

                ArrayList<Column> columnList = new ArrayList<>();
                String columnName = null;
                ArrayList<String> columnArgs = new ArrayList<>();

                for (int i = 4; i<sqlQuerySplitted.length-1; i++) {
                    if(columnName == null)
                        columnName = sqlQuerySplitted[i];
                    else if(!sqlQuerySplitted[i].equals(",")) {
                        columnArgs.add(sqlQuerySplitted[i]);
                        if(i == sqlQuerySplitted.length-2)
                            columnList.add(new Column(columnName, columnArgs.toArray(new String[0])));
                    }
                    else if(sqlQuerySplitted[i].equals(",")){
                        if(!columnName.equals("ID"))
                            columnList.add(new Column(columnName, columnArgs.toArray(new String[0])));
                        columnArgs.clear();
                        columnName = null;
                    }
                }

                if(getTableIndexFromName(currentTableName) != -1){
                    if(override)
                        tables.set(getTableIndexFromName(currentTableName), new Table(currentTableName, columnList.toArray(new Column[0])));
                }
                else
                    tables.add(new Table(currentTableName, columnList.toArray(new Column[0])));

                c.moveToNext();
            }
        }
        c.close();
    }

    /**
     * Add table in existing database and upgrade it
     * @param tableName Name of the new table
     * @param columns List of column
     */
    public void addTable(String tableName, Column... columns) {
        Table table = new Table(
                tableName,
                columns
        );

        if(getTableIndexFromName(tableName) != -1)
            tables.set(getTableIndexFromName(tableName), table);
        else
            tables.add(table);

        openDataBase().execSQL(table.getSql());
    }

    /**
     * Open writable db instance
     * @return Writable db instance
     */
    public synchronized SQLiteDatabase openDataBase() {
        return getWritableDatabase();
    }

    /**
     * Delete db
     * @return Success
     */
    public boolean deleteDatabase() {
        return appContext.deleteDatabase(DB_NAME);
    }

    /**
     * Delete all data from table
     * @param tableName Table name
     */
    public void deleteAllDataFrom(String tableName) {
        SQLiteDatabase db = openDataBase();
        db.execSQL("DELETE FROM " + tableName);
    }

    /**
     * Delete row from table
     * @param tableName Table name
     * @param rowIndex Row id
     * @return Success bool
     */
    public boolean deleteRow(String tableName, int rowIndex) {
        SQLiteDatabase db = openDataBase();
        return db.delete(tableName, "id = ?", new String[]{String.valueOf(rowIndex)}) == 1;
    }

    /**
     * Delete row from table with where clause
     * @param tableName Table name
     * @param data Data instance
     * @return Success bool
     */
    public boolean deleteRowWhere(String tableName, Data data) {
        SQLiteDatabase db = openDataBase();
        return db.delete(tableName, data.getColumnName() + " = ?", new String[]{String.valueOf(data.getValue())}) == 1;
    }

    /**
     * Insert Data instance into db table
     * @param tableName Table name
     * @param data Array of Data
     * @return Success bool
     */
    public boolean addDataInTable(String tableName, Data... data) {
        if(getTableIndexFromName(tableName) == -1)
            return false;

        ContentValues cv = new ContentValues();
        for (Data datum : data) {
            if (datum.getColumnName().isEmpty()) {
                return false;
            } else {
                cv.put(datum.getColumnName(), datum.getValue());
            }
        }

        long result = openDataBase().insert(tableName, null, cv);
        return result != -1;
    }

    /**
     * Insert Data instance into db table
     * @param tableName Table name
     * @param data Array of Data
     * @return Success bool
     */
    public boolean addDataInTable(String tableName, Set<Data> data) {
        if(getTableIndexFromName(tableName) == -1)
            return false;

        ContentValues cv = new ContentValues();
        for (Data datum : data) {
            if (datum.getColumnName().isEmpty()) {
                return false;
            } else {
                cv.put(datum.getColumnName(), datum.getValue());
            }
        }

        long result = openDataBase().insert(tableName, null, cv);
        return result != -1;
    }

    /**
     * Update row from table with Data array
     * @param tableName Table name
     * @param rowIndex Row id
     * @param data Data array
     * @return Success bool
     */
    public boolean updateData(String tableName, int rowIndex, Data... data) {
        ContentValues cv = new ContentValues();
        for (Data datum : data) {
            if (datum.getColumnName().isEmpty()) {
                return false;
            } else {
                cv.put(datum.getColumnName(), datum.getValue());
            }
        }
        return openDataBase().update(tableName, cv, "id = ?", new String[]{String.valueOf(rowIndex)}) > 0;
    }

    /**
     * Return Cursor pointing on first element of desired table
     *
     * @param table Table name
     * @return Cursor
     */
    public Cursor getAllDataFromTable(String table)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + table, null);
    }

    /**
     * Return Cursor pointing on first element of desired table with where
     *
     * @param table Table name
     * @return Cursor
     */
    public Cursor getAllDataFromTable(String table, String where)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM " + table + " WHERE " + where, null);
    }

    /**
     * Return Cursor pointing on first element of desired column of table
     *
     * @param table Table name
     * @param column Data name
     * @return Cursor
     */
    public Cursor getOneDataFromTable(String table, String column)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT " + column + " FROM " + table, null);
    }

    /**
     * Return Cursor pointing on first element of desired data of table with where parameter
     *
     * @param table Table name
     * @param column Data name
     * @param where Where constraint
     * @return Cursor
     */
    public Cursor getOneDataFromTable(String table, String column, String where)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT " + column + " FROM " + table + " WHERE " + where, null);
    }

    /**
     * Return Cursor pointing on first element of desired columns of table
     *
     * @param table Table name
     * @param columns Data name
     * @return Cursor
     */
    public Cursor getMultipleDataFromTable(String table, String... columns)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String catColumn = columns[0];
        for (int i = 1; i < columns.length; i++) {
            catColumn = catColumn.concat(", "+columns[i]);
        }
        Log.d(Constants.PACKAGE_NAME, "SELECT " + catColumn + " FROM " + table);
        return db.rawQuery("SELECT " + catColumn + " FROM " + table, null);
    }

    /**
     * Return Cursor pointing on first element of desired columns of table with where parameter
     *
     * @param table Table name
     * @param where Where string argument
     * @param columns Data name
     * @return Cursor
     */
    public Cursor getMultipleDataFromTableWhere(String table, String where, String... columns)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String catColumn = columns[0];
        for (int i = 1; i < columns.length; i++) {
            catColumn = catColumn.concat(", "+columns[i]);
        }
        Log.d(Constants.PACKAGE_NAME, "SELECT " + catColumn + " FROM " + table + " WHERE " + where);
        return db.rawQuery("SELECT " + catColumn + " FROM " + table + " WHERE " + where, null);
    }

    /**
     * Drop all table of the db
     */
    private void dropAllTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();

        while(c.moveToNext()) {
            tables.add(c.getString(0));
        }
        c.close();

        for(String table : tables) {
            String dropQuery = "DROP TABLE IF EXISTS " + table;
            db.execSQL(dropQuery);
        }
    }

    /**
     * Debug function for reset database
     * Drop all tables and re-create them
     */
    public void resetDB()
    {
        dropAllTables();
        SQLiteDatabase db = this.getWritableDatabase();
        onCreate(db);
    }

    public String getDBName()
    {
        return DB_NAME;
    }

    /**
     * Used to get table index in tables array with his name
     * Can also be used to verify existence of the table
     * @param tableName Name of the table
     * @return Table index or -1 if not found
     */
    public int getTableIndexFromName(String tableName){
        for(int i = 0; i<tables.size(); i++){
            if(tables.get(i).getTableName().trim().equals(tableName))
                return i;
        }
        return -1;
    }

    /**
     * Return the number of elements in the table
     *
     * @param table Table name
     * @return Number of entry of table
     */
    public int getNumEntries(String table)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return (int)DatabaseUtils.queryNumEntries(db, table);
    }

    /**
     * Check if table in db is empty
     *
     * @param table Table instance
     * @return boolean
     */
    public boolean isTableEmpty(Table table)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + table.getTableName(), null);
        boolean empty;

        empty = !mCursor.moveToFirst();
        mCursor.close();

        return empty;
    }
    /**
     * Check if table in db is empty
     *
     * @param table Table name
     * @return boolean
     */
    public boolean isTableEmpty(String table)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + table, null);
        boolean empty;

        empty = !mCursor.moveToFirst();
        mCursor.close();

        return empty;
    }

    /**
     * Export db to csv
     * @return Success boolean
     */
    public boolean exportDbToCSV(){
        try {
            SQLiteDatabase db = openDataBase();
            ExportConfig config = new ExportConfig(db, DB_NAME, ExportConfig.ExportType.CSV, appContext);
            DBExporterCsv exporter = new DBExporterCsv(config);
            exporter.export();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Export db to json
     * @return Success boolean
     */
    public boolean exportDbToJSON(){
        try {
            SQLiteDatabase db = openDataBase();
            ExportConfig config = new ExportConfig(db, DB_NAME, ExportConfig.ExportType.JSON, appContext);
            DBExporterJson exporter = new DBExporterJson(config);
            exporter.export();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Restore db from json export
     * @return Success boolean
     */
    public boolean restoreDbFromJSON(){
        try {
            File dbJsonPath = new File(FileUtils.getAppDir(appContext) + "/databases/" + DB_NAME + ".json");
            if(dbJsonPath.exists() && !dbJsonPath.isDirectory()) {
                ImportConfig config = new ImportConfig(this, dbJsonPath, ImportConfig.ImportType.JSON);
                DBImporterJson importer = new DBImporterJson(config);
                importer.restore();
            }
            else{
                throw new FileNotFoundException("Db json file not found for restore");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Import data from json export to db
     * @return Success boolean
     */
    public boolean importDataFromJSON(){
        try {
            File dbJsonPath = new File(FileUtils.getAppDir(appContext) + "/databases/" + DB_NAME + ".json");
            if(dbJsonPath.exists() && !dbJsonPath.isDirectory()) {
                ImportConfig config = new ImportConfig(this, dbJsonPath, ImportConfig.ImportType.JSON);
                DBImporterJson importer = new DBImporterJson(config);
                importer.importData();
            }
            else{
                throw new FileNotFoundException("Db json file not found for import");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Import data from custom json file to db
     * @param file Json file with compatible data
     * @return Success boolean
     */
    public boolean importDataFromJSON(File file){
        try {
            if(file.exists() && !file.isDirectory()) {
                ImportConfig config = new ImportConfig(this, file, ImportConfig.ImportType.JSON);
                DBImporterJson importer = new DBImporterJson(config);
                importer.importData();
            }
            else{
                throw new FileNotFoundException("Db json file not found for import");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Change the base url for db sync
     * @param url Api url
     */
    public void setSyncBaseUrl(String url){
        Constants.SYNC_URL = url;
    }

    /**
     * Synchronization function used to send db instance in json file to remote api
     *
     * @param autoExport Enable auto export to json before sync
     */
    public void syncDb(boolean autoExport) throws Exception {
        if(Constants.SYNC_URL == null || Constants.SYNC_URL.trim().equals("")) {
            if (autoExport)
                exportDbToJSON();

            File dbJsonPath = new File(FileUtils.getAppDir(appContext) + "/databases/" + DB_NAME + ".json");
            if (dbJsonPath.exists() && !dbJsonPath.isDirectory()) {
                Log.d(Constants.PACKAGE_NAME, dbJsonPath.getAbsolutePath());

                String jsonContent;
                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(dbJsonPath));
                    JsonObject js = new Gson().fromJson(bufferedReader, JsonObject.class);
                    jsonContent = new Gson().toJson(js);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }

                Call<ResponseBody> call = requestBuilder(jsonContent);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    @EverythingIsNonNull
                    public void onResponse(Call<ResponseBody> call,
                                           Response<ResponseBody> response) {
                        Log.v("Upload", "success");
                    }

                    @Override
                    @EverythingIsNonNull
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("Upload error:", Objects.requireNonNull(t.getMessage()));
                    }
                });
            } else {
                throw new FileNotFoundException("Db json file not found for synchronizing");
            }
        }
        else{
            throw new MissingSyncUrlException("Constants.SYNC_URL empty or null");
        }
    }

    /**
     * Request builder method, used to build custom request for syncDb method
     * Can be override to build custom request, need an interface template for the request model (cf : FileUploadService)
     * By default, it will create a request composed by : description ("DB sync") and a content (jsonData)
     *
     * @param jsonData Exported json data
     * @return Call<ResponseBody> build with specific model
     */
    public Call<ResponseBody> requestBuilder(String jsonData){
        FileUploadService service =
                ServiceGenerator.createService(FileUploadService.class);

        String titleString = "DB sync";
        RequestBody title =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, titleString);

        RequestBody content =
                RequestBody.create(
                        okhttp3.MultipartBody.FORM, jsonData);

        return service.upload(title, content);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 0; i < tables.size(); i++) {
            db.execSQL(tables.get(i).getSql());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old, int n) {
        for (int i = 0; i < tables.size(); i++) {
            db.execSQL("DROP TABLE IF EXISTS " + tables.get(i).getTableName());
        }
        onCreate(db);
    }
}


package com.bcstudio.androidsqlitetoolbox.Database;

import java.util.Arrays;
import java.util.Objects;

public class Table {
    private String tableName;
    private Column[] columns;
    private String sql = "";

    public Table(String tableName, Column[] columns) {
        this.tableName = tableName.replace(" ", "_");
        this.columns = columns;

        if(columns != null)
            initSQL();
    }

    private void initSQL(){
        sql = " CREATE TABLE IF NOT EXISTS " + tableName + " ( ID INTEGER PRIMARY KEY AUTOINCREMENT , ";
        for (int i = 0; i < columns.length; i++) {
            sql = sql.concat(" " + columns[i].getColumnName() + " " + columns[i].getColumnDataType() + " ");
            if (i == columns.length - 1) {
                sql = sql.concat(" ) ");
            } else {
                sql = sql.concat(" , ");
            }
        }
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        initSQL();
    }

    public Column[] getColumns() {
        return columns;
    }

    public void setColumns(Column[] columns) {
        this.columns = columns;
        initSQL();
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Table)) return false;
        Table table = (Table) o;
        return Objects.equals(tableName, table.tableName) &&
                Arrays.equals(columns, table.columns);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tableName);
        result = 31 * result + Arrays.hashCode(columns);
        return result;
    }
}


package com.bcstudio.androidsqlitetoolbox.Database;

import java.util.Objects;

public class Data {
    private String columnName;
    private String value;

    public Data(String columnName, String value) {
        this.columnName = columnName.replace(" ", "_");
        this.value = value;
    }

    public Data(String columnName, int value) {
        this.columnName = columnName.toUpperCase();
        this.value = String.valueOf(value);
    }

    public Data(String columnName, double value) {
        this.columnName = columnName.toUpperCase();
        this.value = String.valueOf(value);
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Data)) return false;
        Data data = (Data) o;
        return Objects.equals(columnName, data.columnName) &&
                Objects.equals(value, data.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnName, value);
    }
}


package com.bcstudio.androidsqlitetoolbox.Database;

import java.util.Objects;

public class Column {

    private String columnName, columnDataType;

    public Column(String columnName, String... columnDataTypes) {
        this.columnName = columnName.replaceAll(" ", "_");
        String finalDatatype = "";
        for (int i = 0; i < columnDataTypes.length; i++) {
            if (!columnDataTypes[i].startsWith(" ")) {
                columnDataTypes[i] = " " + columnDataTypes[i];
            }
            if (!columnDataTypes[i].endsWith(" ")) {
                columnDataTypes[i] = columnDataTypes[i] + " ";
            }
            finalDatatype = finalDatatype.concat(columnDataTypes[i]);
        }
        this.columnDataType = finalDatatype.toUpperCase();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnDataType() {
        return columnDataType;
    }

    public void setColumnDataType(String columnDataType) {
        this.columnDataType = columnDataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Column)) return false;
        Column column = (Column) o;
        return Objects.equals(columnName, column.columnName) &&
                Objects.equals(columnDataType, column.columnDataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnName, columnDataType);
    }
}


package com.bcstudio.androidsqlitetoolbox.Exceptions;

/**
 * This exception is raised when sync url is empty or null
 */

public final class MissingSyncUrlException extends Exception {
    private static final long serialVersionUID = 1L;

    public MissingSyncUrlException(String msg) {
        super(msg);
    }

    public MissingSyncUrlException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Creates exception with the specified cause. Consider using
     * {@link #MissingSyncUrlException(String, Throwable)} instead if you can describe what happened.
     *
     * @param cause root exception that caused this exception to be thrown.
     */
    public MissingSyncUrlException(Throwable cause) {
        super(cause);
    }
}


package com.bcstudio.androidsqlitetoolbox.Componants;

import java.util.HashSet;
import java.util.Set;

public abstract class DbInteractionConfig {

    protected String databaseName;
    protected Set<String> excludedTables;
    protected Set<String> excludedFields;

    /**
     * Add table to exclude list
     * @param tableName Table name
     */
    public void setExcludeTable(String tableName) {
        if (excludedTables == null) {
            excludedTables = new HashSet<>();
        }
        excludedTables.add(tableName.trim().toUpperCase());
    }

    /**
     * Check if table name is exclude
     * @param tableName Table name
     * @return bool
     */
    public boolean isExcludeTable(String tableName) {
        if (excludedTables == null) {
            return false;
        }
        return excludedTables.contains(tableName.trim().toUpperCase());
    }

    /**
     * Add field to exclude list
     * @param fieldName Field name
     */
    public void setExcludeField(String fieldName) {
        if (excludedFields == null) {
            excludedFields = new HashSet<>();
        }
        excludedFields.add(fieldName.trim().toUpperCase());
    }

    /**
     * Check if field name is exclude
     * @param fieldName Field name
     * @return bool
     */
    public boolean isExcludeField(String fieldName) {
        if (excludedFields == null) {
            return false;
        }
        return excludedFields.contains(fieldName.trim().toUpperCase());
    }

    public String getDatabaseName() {
        return databaseName;
    }
}


package com.bcstudio.androidsqlitetoolbox.Http;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Template for file upload request
 */
public interface FileUploadService {
    @Multipart
    @POST("sync")
    Call<ResponseBody> upload(
            @Part("description") RequestBody description,
            @Part("content") RequestBody file
    );
}


package com.bcstudio.androidsqlitetoolbox.Http;

import com.bcstudio.androidsqlitetoolbox.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builder to create a new REST client with the given API base url using retrofit2
 */
public class ServiceGenerator {

    // base url for request
    private static final String BASE_URL = Constants.SYNC_URL;

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    private static OkHttpClient.Builder httpClient =
            new OkHttpClient.Builder();

    public static <S> S createService(
            Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}


package com.bcstudio.androidsqlitetoolbox.Export;

public class DBExporterCsv extends DBExporter {
    private boolean newTable;
    private String dataRow;
    private String row;
    private String table;
    private String dbData;

    public DBExporterCsv(ExportConfig config) {
        super(config);
    }

    @Override
    protected void prepairExport(String dbName) throws Exception {
        this.newTable = true;
        this.row = "";
        this.dataRow = "";
        this.table = "";
        this.dbData = "";
    }

    @Override
    protected String getExportAsString() throws Exception {
        return dbData;
    }

    @Override
    protected void startTable(String tableName) throws Exception {
        newTable = true;
        table = "";
        table = table.concat("\"table="+tableName+"\"\n");
    }

    @Override
    protected void endTable() throws Exception {
        dataRow = "";
        dbData = dbData.concat(table+"\n");
    }

    @Override
    protected void endRow() throws Exception {
        if(newTable) {
            table = table.concat(dataRow + "\n");
            table = table.concat(row + "\n");
            newTable = false;
        }
        else
            table = table.concat(row + "\n");
    }

    @Override
    protected void populateRowWithField(String columnName, String string) throws Exception {
        if(newTable) {
            dataRow = dataRow.concat("\"" + columnName + "\",");
            row = row.concat("\"" + string + "\",");
        }
        else
            row = row.concat("\"" + string + "\",");
    }

    @Override
    protected void startRow() throws Exception {
        row = "";
    }
}


package com.bcstudio.androidsqlitetoolbox.Export;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.bcstudio.androidsqlitetoolbox.Componants.DbInteractionConfig;
import com.bcstudio.androidsqlitetoolbox.FileUtils;

import java.io.File;
import java.io.IOException;

public class ExportConfig extends DbInteractionConfig {

    public enum ExportType {
        JSON, CSV
    }

    // Package variables
    SQLiteDatabase db;
    File directory;

    private ExportType exportType;
    private String exportCustomFileExtension = "";

    /**
     * Constructor
     * @param db SQLiteDatabase instance
     * @param databaseName Db name
     * @param exportType ExportType (define if file will be export to CSV or JSON)
     * @param appContext Application context
     * @throws IOException
     */
    public ExportConfig(SQLiteDatabase db, String databaseName, ExportType exportType, Context appContext) throws IOException {
        this.db = db;
        this.exportType = exportType;
        this.databaseName = databaseName;

        if( !FileUtils.isExternalStorageWritable() ){
            throw new IOException("Cannot write to external storage");
        }
        this.directory = FileUtils.createDirIfNotExist(FileUtils.getAppDir(appContext) + "/databases/");
    }

    /**
     * Alternative constructor for custom file extension (not handled by ExportType)
     * @param db SQLiteDatabase instance
     * @param databaseName Db name
     * @param exportCustomFileExtension File extension
     * @param appContext Application context
     * @throws IOException
     */
    public ExportConfig(SQLiteDatabase db, String databaseName, String exportCustomFileExtension, Context appContext) throws IOException {
        this.db = db;
        this.exportCustomFileExtension = exportCustomFileExtension;
        this.databaseName = databaseName;

        if(!FileUtils.isExternalStorageWritable()){
            throw new IOException("Cannot write to external storage");
        }
        this.directory = FileUtils.createDirIfNotExist(FileUtils.getAppDir(appContext) + "/databases/");
    }

    /**
     * Return corresponding file extension
     * @return File extension string
     * @throws IllegalArgumentException
     */
    public String getFileExtension() throws IllegalArgumentException {
        if(exportType != null) {
            switch (exportType) {
                case CSV:
                    return ".csv";
                case JSON:
                    return ".json";
                default:
                    throw new IllegalArgumentException("File format unhandled or invalid");
            }
        } else {
            if(!exportCustomFileExtension.trim().equals("") && exportCustomFileExtension.trim().matches("\\.[a-z]+"))
                return exportCustomFileExtension.trim();
            throw new IllegalArgumentException("File format unhandled or invalid");
        }
    }
}


package com.bcstudio.androidsqlitetoolbox.Export;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Log;

import com.bcstudio.androidsqlitetoolbox.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstract class designed to export db tables
 * Must be extends
 */
public abstract class DBExporter {
    protected final SQLiteDatabase db;
    protected final File directory;
    protected final ExportConfig config;

    /**
     * Constructor
     * @param config ExportConfig instance containing export config
     */
    DBExporter(ExportConfig config) {
        this.config = config;
        db = config.db;
        directory = config.directory;
    }

    /**
     * Main export function
     * Skip metadata and excluded tables
     * @throws Exception
     */
    public void export() throws Exception {
        String dbName = config.getDatabaseName();
        if (dbName == null) {
            throw new IllegalArgumentException("ExportConfig.databaseName must not be null");
        }
        Log.i(Constants.PACKAGE_NAME, "exporting database - " + dbName);

        prepairExport(dbName);

        // get the tables
        String sql = "select * from sqlite_master";
        Cursor c = this.db.rawQuery(sql, new String[0]);
        Log.d(Constants.PACKAGE_NAME, "select * from sqlite_master, cur size " + c.getCount());
        while (c.moveToNext()) {
            String tableName = c.getString(c.getColumnIndex("name"));
            Log.d(Constants.PACKAGE_NAME, "table name " + tableName);

            // skip metadata, sequence, and uidx (unique indexes)
            if (!tableName.equals("android_metadata") && !tableName.equals("sqlite_sequence")
                    && !tableName.startsWith("uidx") && !tableName.startsWith("idx_") && !tableName.startsWith("_idx")
                    && !config.isExcludeTable(tableName)) {
                try {
                    this.exportTable(tableName);
                } catch (SQLiteException e) {
                    Log.w(Constants.PACKAGE_NAME, "Error exporting table " + tableName, e);
                }
            }
        }

        c.close();
        try {
            this.writeToFile(getExportAsString(), dbName + config.getFileExtension());
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            return;
        }

        Log.i(Constants.PACKAGE_NAME, "exporting database complete");
    }

    /**
     * Export table content
     * @param tableName
     * @throws Exception
     */
    private void exportTable(final String tableName) throws Exception {
        Log.d(Constants.PACKAGE_NAME, "exporting table - " + tableName);

        String sql = "select * from " + tableName;
        Cursor c = this.db.rawQuery(sql, new String[0]);

        startTable(tableName);
        while (c.moveToNext()) {
            startRow();
            String id = c.getString(1);
            if (id == null || TextUtils.isEmpty(id)) {
                id = c.getString(0);
            }
            for (int i = 0; i < c.getColumnCount(); i++) {
                populateRowWithField(c.getColumnName(i), c.getString(i));
            }
            endRow();
        }
        c.close();
        endTable();
    }

    /**
     * Write exported data to dest file
     * @param payload
     * @param exportFileName
     * @throws IOException
     */
    private void writeToFile(String payload, String exportFileName) throws IOException {
        File file = new File(directory, exportFileName);
        if (file.exists()) {
            file.delete();
        }

        ByteBuffer buff = ByteBuffer.wrap(payload.getBytes());
        try (FileChannel channel = new FileOutputStream(file).getChannel()) {
            channel.write(buff);
        } finally {
            Log.i(Constants.PACKAGE_NAME, "Exported DB to " + file.toString());
        }
    }

    /**
     * Can be used to generate filename with time
     * @return
     */
    private String createBackupFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmm");
        return "_backup_" + sdf.format(new Date());
    }

    /**
     * Return exported data to string
     * @return exported data string
     * @throws Exception
     */
    abstract protected String getExportAsString() throws Exception;

    /**
     * Initialize export
     * @param dbName
     * @throws Exception
     */
    abstract protected void prepairExport(String dbName) throws Exception;

    /**
     * Handle instructions at row end
     * @throws Exception
     */
    abstract protected void endRow() throws Exception;

    /**
     * Add data to row
     * @param columnName
     * @param string
     * @throws Exception
     */
    abstract protected void populateRowWithField(String columnName, String string) throws Exception;

    abstract protected void startRow() throws Exception;

    /**
     * Handle instructions at table end
     * @throws Exception
     */
    abstract protected void endTable() throws Exception;

    /**
     * Handle instructions at table start
     * @param tableName
     * @throws Exception
     */
    abstract protected void startTable(String tableName) throws Exception;
}


package com.bcstudio.androidsqlitetoolbox.Export;

import org.json.JSONArray;
import org.json.JSONObject;

public class DBExporterJson extends DBExporter{
    private JSONObject jsonRoot;
    private JSONObject jsonDB;
    private JSONObject row;
    private JSONArray table;


    public DBExporterJson(ExportConfig config) {
        super(config);
        jsonRoot = new JSONObject();
    }

    @Override
    protected void prepairExport(String dbName) throws Exception {
        jsonDB = new JSONObject();
        jsonRoot.put(dbName, jsonDB);
    }

    @Override
    protected String getExportAsString() throws Exception {
        return jsonRoot.toString(1);
    }

    @Override
    protected void startTable(String tableName) throws Exception {
        table = new JSONArray();
        jsonDB.put(tableName, table);
    }

    @Override
    protected void endTable() throws Exception {

    }

    @Override
    protected void endRow() throws Exception {
        table.put(row);
    }

    @Override
    protected void populateRowWithField(String columnName, String string) throws Exception {
        row.put(columnName, string);
    }

    @Override
    protected void startRow() throws Exception {
        row = new JSONObject();
    }
}


package com.bcstudio.androidsqlitetoolbox.Import;

import com.bcstudio.androidsqlitetoolbox.Componants.DbInteractionConfig;
import com.bcstudio.androidsqlitetoolbox.Database.DBHandler;

import java.io.File;

public class ImportConfig extends DbInteractionConfig {
    public enum ImportType {
        JSON
    }

    // Package variables
    DBHandler db;
    File srcFile;

    private ImportType importType;

    public ImportConfig(DBHandler database, File srcFile, ImportType importType) {
        this.srcFile = srcFile;
        this.db = database;
        this.importType = importType;
        this.databaseName = db.getDBName();
    }

    public ImportConfig(String databaseName, File srcFile, ImportType importType) {
        this.srcFile = srcFile;
        this.databaseName = databaseName;
        this.importType = importType;
    }
}


package com.bcstudio.androidsqlitetoolbox.Import;

import android.util.Log;
import android.util.Pair;

import com.bcstudio.androidsqlitetoolbox.Constants;
import com.bcstudio.androidsqlitetoolbox.Database.DBHandler;
import com.bcstudio.androidsqlitetoolbox.Database.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

public abstract class DBImporter {
    protected final DBHandler db;
    protected final File srcFile;
    protected final ImportConfig importConfig;

    protected String parsedData;
    protected JSONObject jsonDb;

    DBImporter(ImportConfig importConfig) throws IOException {
        this.importConfig = importConfig;
        this.db = importConfig.db;
        this.srcFile = importConfig.srcFile;

        BufferedReader reader = new BufferedReader(new FileReader(srcFile));
        parseData(readFile(reader));
    }

    /**
     * Convert data to JSONObject
     * @param data File data string from readFile()
     */
    protected abstract void parseData(String data);

    /**
     * Read and build a string from a file
     * @param bf BufferedReader
     * @return File data string
     * @throws IOException
     */
    protected abstract String readFile(BufferedReader bf) throws IOException;

    protected ArrayList<Pair<String, JSONArray>> getTables() throws JSONException {
        Iterator<String> keys = jsonDb.keys();
        ArrayList<Pair<String, JSONArray>> tables = new ArrayList<>();
        while(keys.hasNext()) {
            String key = keys.next();
            tables.add(new Pair<>(key, jsonDb.getJSONArray(key)));
            Log.d(Constants.PACKAGE_NAME, "getTables() : Table -> " + key);
        }
        return tables;
    }

    protected JSONArray getTable(String tableName) throws JSONException {
        return jsonDb.getJSONArray(tableName);
    }

    /**
     * Import data to db from file
     * Ignore id, created_at and update_at fields and add new data after the rest
     * @throws JSONException
     */
    public void importData() throws JSONException {
        ArrayList<Pair<String, JSONArray>> tables = getTables();
        for (int i = 0; i < tables.size(); i++) {
            Log.d(Constants.PACKAGE_NAME, "importData() : table -> " + tables.get(i).first);
            importConfig.setExcludeField("id");
            importConfig.setExcludeField("created_at");
            importConfig.setExcludeField("updated_at");
            if (!importConfig.isExcludeTable(tables.get(i).first)) {
                Log.d(Constants.PACKAGE_NAME, "importData() : table -> " + tables.get(i).first + " is not excluded from import, continuing...");
                if (db.getTableIndexFromName(tables.get(i).first) != -1) {
                    Log.d(Constants.PACKAGE_NAME, "importData() : table -> " + tables.get(i).first + " exist in db, continuing...");
                    LinkedHashSet<Data> tableData = new LinkedHashSet<>();
                    for (int j = 0; j < tables.get(i).second.length(); j++) {
                        JSONObject row = tables.get(i).second.getJSONObject(j);
                        Iterator<String> keys = row.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if(!importConfig.isExcludeField(key)) {
                                tableData.add(new Data(key, row.getString(key)));
                                Log.d(Constants.PACKAGE_NAME, "importData() : Colomn -> " + key + " Value -> " + row.getString(key));
                            } else Log.d(Constants.PACKAGE_NAME, "importData() : Colomn -> " + key + " Ignore : field exclude");
                        }
                        db.addDataInTable(tables.get(i).first, tableData);
                    }
                }
            }
        }
    }

    /**
     * Restore data to db from file
     * Keep id, created_at and updated_at fields (so it will don't add data with already taken id or it need a fresh database)
     * @throws JSONException
     */
    public void restore() throws JSONException {
        ArrayList<Pair<String, JSONArray>> tables = getTables();
        for(int i = 0; i<tables.size(); i++){
            Log.d(Constants.PACKAGE_NAME, "restore() : table -> " + tables.get(i).first);
            if(!importConfig.isExcludeTable(tables.get(i).first)){
                Log.d(Constants.PACKAGE_NAME, "restore() : table -> " + tables.get(i).first + " is not excluded from import, continuing...");
                if(db.getTableIndexFromName(tables.get(i).first) != -1){
                    Log.d(Constants.PACKAGE_NAME, "restore() : table -> " + tables.get(i).first + " exist in db, continuing...");
                    LinkedHashSet<Data> tableData = new LinkedHashSet<>();
                    for(int j = 0; j < tables.get(i).second.length(); j++) {
                        JSONObject row = tables.get(i).second.getJSONObject(j);
                        Iterator<String> keys = row.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if(!importConfig.isExcludeField(key)
                                    || (key.trim().toUpperCase().equals("ID")
                                        || key.trim().toUpperCase().equals("CREATED_AT")
                                        || key.trim().toUpperCase().equals("UPDATED_AT"))) {
                                tableData.add(new Data(key, row.getString(key)));
                                Log.d(Constants.PACKAGE_NAME, "restore() : Colomn -> " + key + " Value -> " + row.getString(key));
                            }
                        }
                        db.addDataInTable(tables.get(i).first, tableData);
                    }
                }
            }
        }

    }




}


package com.bcstudio.androidsqlitetoolbox.Import;

import android.util.Log;

import com.bcstudio.androidsqlitetoolbox.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;

public class DBImporterJson extends DBImporter{

    public DBImporterJson(ImportConfig importConfig) throws IOException {
        super(importConfig);
    }

    @Override
    protected String readFile(BufferedReader bf) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bf.readLine()) != null) {
            sb.append(line);
        }
        Log.d(Constants.PACKAGE_NAME, "Json file content : " + sb.toString());
        return sb.toString();
    }

    @Override
    protected void parseData(String data) {
        try {
            jsonDb = (JSONObject) (new JSONObject(data)).get(importConfig.getDatabaseName());
            Log.d(Constants.PACKAGE_NAME, "JSONObject content : " + jsonDb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            jsonDb = new JSONObject();
        }
    }
}


