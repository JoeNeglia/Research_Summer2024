package app.msdeep14.tripcount;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}

package app.msdeep14.tripcount;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
}

package app.msdeep14.tripcount;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class AddPlaceDetails extends AppCompatActivity {

    private static Button btn;
    private static Button b1;
    private static Button btnadd;
    public DatePickerDialog datepick = null;
    SQLiteDatabase db1=null;
    EditText e1,e2;
    Editable d1,d2=null;
    TextView edtDob=null;
    TextView edtxt;
    EditText f_add;
    EditText num;
    int check=0;
    ArrayList<String> fname= new ArrayList<String>();
    int size=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn = (Button) findViewById(R.id.button_SUBMIT);
        edtxt=(TextView) findViewById(R.id.daye);
        btnadd=(Button)findViewById(R.id.add_btn);
        b1= (Button) findViewById(R.id.daypickbut);
        f_add=(EditText)findViewById(R.id.friend_name);
        num=(EditText)findViewById(R.id.editno);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        OnClickpickDate();
        OnClickButtonSubmit();
        setTitle("New Trip");
        OnClickButtonAddFriend();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(AddPlaceDetails.this, AddPlace.class));
        finish();
    }

    public void OnClickpickDate(){
        try{
            b1.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v){
                    datepick = new DatePickerDialog(v.getContext(), (DatePickerDialog.OnDateSetListener) new DatePickHandler(), Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

                    datepick.show();
                }
            });
        }
        catch(Exception e){
            Toast.makeText(getApplicationContext(), "Invalid Date", Toast.LENGTH_SHORT).show();
        }
    }

    public class DatePickHandler implements DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int month, int day) {
            int months = month+1;
            if((months<10)&&(day<10))
                edtxt.setText(year + "-0" + (months) + "-0" + day);
            else if((months<10)&&(day>10))
                edtxt.setText(year + "-0" + (months) + "-" + day);
            else if((months>10)&&(day<10))
                edtxt.setText(year + "-" + (months) + "-0" + day);
            else
                edtxt.setText(year + "-" + (months) + "-" + day);
            datepick.hide();
        }
    }

    void OnClickButtonSubmit(){
        e1=(EditText)findViewById(R.id.Textplace);
        e2=(EditText)findViewById(R.id.editno);
        edtDob=(TextView) findViewById(R.id.daye);
        btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db1 = openOrCreateDatabase("trip.db", Context.MODE_PRIVATE, null);
                        d1 = e1.getText();
                        d2 = e2.getText();
                        String dd = edtDob.getText().toString();
                        String s1 = d1.toString().toLowerCase();
                        String s2 = d2.toString();
                        s1=s1.trim();
                        s2=s2.trim();

                        try {
                            db1.execSQL("CREATE TABLE IF NOT EXISTS Trip_details (id INTEGER PRIMARY KEY AUTOINCREMENT ,place_name TEXT NOT NULL, date_go DATE NOT NULL," + " friend_no INTEGER NOT NULL DEFAULT 0);");
                            String COL_2 = "place_name";
                            String COL_3 = "date_go";
                            String COL_4="friend_no";
                            String table = "Trip_details";
                            //check if any of the fields is not empty or friend no is not equal to zero
                            if (s1.matches("") || s2.matches("") || dd.matches("") || Integer.parseInt(s2)==0)
                                throw new ArithmeticException("Inadequate details..\nEnter Again");
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(COL_2, d1.toString().toLowerCase());
                            contentValues.put(COL_2, d1.toString().toLowerCase());
                            contentValues.put(COL_3, dd);
                            contentValues.put(COL_4,Integer.parseInt(num.getText().toString()));
                            int x=Integer.parseInt(num.getText().toString());

                            try {
                                Cursor c = db1.rawQuery("SELECT * FROM Trip_details ORDER BY date_go DESC;", null);
                                if (c != null) {
                                    int i = 0;
                                    if (c.moveToFirst()) {
                                        do {
                                            String compare = c.getString(c.getColumnIndex("place_name"));
                                            if (compare.matches(e1.getText().toString().toLowerCase()))
                                                throw new ArithmeticException("HELLO");

                                        } while (c.moveToNext());
                                    }
                                }

                                //EXCEPTION
                                try {
                                    if (check == Integer.parseInt(num.getText().toString())) {
                                        long result = db1.insert(table, null, contentValues);




                                        if (result != -1) {
                                            Toast.makeText(getApplicationContext(), "Trip Added", Toast.LENGTH_SHORT).show();
                                            Intent lis = new Intent(getApplicationContext(), AddPlace.class);
                                            startActivity(lis);
                                            finish();
                                        } else
                                            throw new ArithmeticException("Inadequate details..\nEnter Again");

                                    } else {
                                        Toast.makeText(getApplicationContext(), "ADD MORE FRIENDS", Toast.LENGTH_SHORT).show();

                                    }

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "Inadequate details..\n" +
                                            "Enter Again", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Trip name already added", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Inadequate details..\n" +
                                    "Enter Again", Toast.LENGTH_LONG).show();
                        }
                    }

                }
        );
    }

    public void OnClickButtonAddFriend(){
        btnadd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            String z = num.getText().toString().trim();
                            if (z.matches(""))
                                throw new ArithmeticException("hello");

                            int x = Integer.parseInt(num.getText().toString());

                            if (check < x) {
                                db1 = openOrCreateDatabase("trip.db", Context.MODE_PRIVATE, null);
                                String TABLE_NAME;
                                e1 = (EditText) findViewById(R.id.Textplace);
                                TABLE_NAME = "f" + e1.getText().toString().toLowerCase();
                                try {
                                    Cursor c = db1.rawQuery("SELECT * FROM Trip_details ORDER BY date_go DESC;", null);
                                    if (c != null) {
                                        int i = 0;
                                        if (c.moveToFirst()) {
                                            do {
                                                String compare = c.getString(c.getColumnIndex("place_name"));
                                                if (compare.matches(e1.getText().toString().toLowerCase()))
                                                    throw new ArithmeticException("HELLO");

                                            } while (c.moveToNext());
                                        }
                                    }
                                    db1.execSQL("create table if not exists " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,friend TEXT,amount INTEGER NOT NULL DEFAULT 0)");
                                    ContentValues contentValues = new ContentValues();
                                    String COL_2;
                                    COL_2 = "friend";
                                    //exception
                                    try {
                                        String y = f_add.getText().toString();
                                        y=y.toLowerCase();
                                        int flag1=0;
                                        if (y.matches(""))
                                            throw new ArithmeticException("Inadequate details..\nEnter Again");

                                        //check if friend name is alrealy included in the list
                                        for(int i=0;i<fname.size();i++){
                                            if(y.matches(fname.get(i))){
                                                Toast.makeText(getApplicationContext(),y+" already included",Toast.LENGTH_SHORT).show();
                                                flag1=1;
                                                break;
                                            }
                                        }
                                        if(flag1==0) {
                                            contentValues.put(COL_2, y);
                                            fname.add(y);
                                            size++;
                                            long result = db1.insert(TABLE_NAME, null, contentValues);
                                            check++;
                                        }
                                        f_add.setText("");
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), "enter friend name", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "Trip already added", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "friend limit exceeded", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(), "Inadequate details..\n" +
                                    "Enter Again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


package app.msdeep14.tripcount;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hp15-p017tu on 25-07-2016.
 */

/*
* adapter class for AddPlace
* */

public class add_adapter extends ArrayAdapter<additem> {
    private SparseBooleanArray mSelectedItemsIds;
    private LayoutInflater inflater;
    private Context mContext;
    private List<additem> list;

    public add_adapter (Context context, int resourceId, List<additem> list) {
        super(context, resourceId, list);
        mSelectedItemsIds = new SparseBooleanArray();
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.list = list;
    }

    private static class ViewHolder {
        TextView date;
        TextView name;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.add_text, null);
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.date = (TextView) view.findViewById(R.id.date);
            //holder.amount = (TextView) view.findViewById(R.id.amount);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.date.setText(list.get(position).getDate());
        holder.name.setText(list.get(position).getName());
        return view;
    }

    @Override
    public void remove(additem remitm) {
        list.remove(remitm);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}


package app.msdeep14.tripcount;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class Update extends AppCompatActivity {

    Spinner spBusinessType;
    ArrayAdapter<String> adapterBusinessType;
    ArrayList<String> names = new ArrayList<String>();
    int flag=0;
    EditText title,amount;
    Button btn_add;
    String p;
    SQLiteDatabase db1=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        db1=openOrCreateDatabase("trip.db", Context.MODE_PRIVATE, null);
        Intent in=getIntent();
        Bundle bundle=in.getExtras();
        p=bundle.getString("id");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // onBackPressed();
        setTitle("New expense");
        final String sp="f"+p;
        Cursor c=db1.rawQuery("SELECT * FROM "+sp+";",null);
        if(c!=null) {
            int i=0;
            if(c.moveToFirst()){
                do{
                    String d4=c.getString(c.getColumnIndex("friend"));
                    names.add(d4);
                    i++;
                }while(c.moveToNext());
            }
        }
        spBusinessType = (Spinner) findViewById(R.id.spBussinessType);
        adapterBusinessType = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,names);
        adapterBusinessType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spBusinessType.setAdapter(adapterBusinessType);
        title=(EditText)findViewById(R.id.editTitle);
        amount=(EditText)findViewById(R.id.editAmount);
        btn_add=(Button)findViewById(R.id.button_ok);
        OnClickButtonAdd();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent=new Intent(getApplicationContext(),ViewTripDetails.class);
        intent.putExtra("id",p);
        startActivity(intent);
        finish();

    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void OnClickButtonAdd(){
        final String s="f"+p;
        btn_add.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        db1.execSQL("create table if not exists " + p+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,friend_name TEXT,note TEXT,amount TEXT)");
                        String COL_2="friend_name";
                        String COL_3="note";
                        String COL_4="amount";
                        String ti=title.getText().toString();
                        String am=amount.getText().toString();
                        String n = spBusinessType.getSelectedItem().toString();
                        String table=p;

                        //EXCEPTION
                        try {
                            if (ti.matches("") || am.matches(""))
                                throw new ArithmeticException("string");

                            //check if the note entered is previously entered in the table
                            Cursor c1 = db1.rawQuery("SELECT * FROM '" + p + "';", null);
                            if (c1 != null) {
                                Log.e("c1!=null", "ppppppppppp");
                                if (c1.moveToFirst()) {
                                    do {
                                        if (ti.matches(c1.getString(c1.getColumnIndex("note"))) && n.matches(c1.getString(c1.getColumnIndex("friend_name")))) {
                                            //update the value
                                            int a = Integer.parseInt(c1.getString(c1.getColumnIndex("amount")));
                                            a += Integer.parseInt(am);
                                            String t = String.valueOf(a);
                                            int index=c1.getInt(c1.getColumnIndex("ID"));
                                            db1.execSQL("UPDATE '" + p + "' SET amount = '" + a + "' WHERE ID ='" + index + "';");
                                            flag = 1;
                                            Log.e(ti, String.valueOf(a));
                                            break;
                                        }
                                    } while (c1.moveToNext());
                                }
                            }


                            if (flag == 0) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(COL_2, n);
                                contentValues.put(COL_3, ti);
                                contentValues.put(COL_4, am);
                                long result = db1.insert(table, null, contentValues);
                                if (result != -1) {
                                   //do
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"error data inserting",Toast.LENGTH_SHORT).show();
                                }
                            }
                            flag = 0;

                            //update

                            String tb = "f" + p;
                            Cursor c = db1.rawQuery("SELECT * FROM  " + tb + " ;", null);
                            if (c != null) {
                                if (c.moveToFirst()) {
                                    do {

                                        String ch = c.getString(c.getColumnIndex("friend"));
                                        if (ch.matches(n)) {
                                            String temp = c.getString(c.getColumnIndex("amount"));
                                            int index = c.getInt(c.getColumnIndex("ID"));
                                            int money1 = Integer.parseInt(temp);
                                            int money2 = Integer.parseInt(am);
                                            int money = money1 + money2;
                                            Log.e("money1",String.valueOf(money1));
                                            Log.e("money2",String.valueOf(money2));
                                            Log.e("money",String.valueOf(money));

                                            db1.execSQL("UPDATE '" + tb + "' SET amount = '" + money + "' WHERE ID ='" + index + "';");

                                            int x=c.getInt(c.getColumnIndex("amount"));
                                            Log.e("updated amount:::::::::",String.valueOf(x));
                                            c.moveToLast();
                                        }
                                    } while (c.moveToNext());
                                }
                            }


                            //update
                            Toast.makeText(getApplicationContext(), "trip details updated!", Toast.LENGTH_SHORT).show();
                            Intent newscreen = new Intent(getApplicationContext(), ViewTripDetails.class);
                            newscreen.putExtra("id", p);
                            startActivity(newscreen);
                            finish();
                        }catch (Exception e)
                        {
                            Toast.makeText(getApplicationContext(),"EMPTY FIELDS",Toast.LENGTH_SHORT).show();
                        }

                    }

                }

        );
        flag=0;
    }
}


package app.msdeep14.tripcount;


/**
 * Created by mandeep singh on 7/25/2016.
 */
public class DataProvider {
    private int amount;
    private  String name;
    private  int id;


    public DataProvider(String name, int id, int amount) {
        this.name = name;
        this.id = id;
        this.amount = amount;
    }

    public DataProvider(int amount, String name) {
        this.amount = amount;
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}


package app.msdeep14.tripcount;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/*
* displays list of created trips
* */

public class AddPlace extends AppCompatActivity {

    private static Button btn_go;
    TextView t;

    Long k;
    int j;
    FloatingActionButton fb;
    String j1;
    TextView noDet;
    SQLiteDatabase db1=null;
    FloatingActionButton f;
    ArrayList<String> notes = new ArrayList<String>();
    ArrayList<String> list_item = new ArrayList<>();
    Vector<Integer> vector = new Vector<>();
    Vector<Integer> vec = new Vector<>();
    int count=0;
    add_adapter adapter;
    List<additem> niitemlist;
    ArrayAdapter adapter1;
    ListView listView;
    ListView listView1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fillActivity();
        fb=(FloatingActionButton)findViewById(R.id.mynew);
        setTitle("Trips");
        opennewwindow();
        listView=(ListView)findViewById(R.id.list_add);

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        TextView temp = (TextView) view.findViewById(R.id.name);
                        String str = temp.getText().toString();
                        Intent intent = new Intent(getApplicationContext(), ViewTripDetails.class);
                        intent.putExtra("id", str);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(AddPlace.this, MainActivity.class));
        finish();

    }

    //function to fill up the activity with trip details from data base create trip.db
    void fillActivity(){
        db1=openOrCreateDatabase("trip.db", Context.MODE_PRIVATE, null);

        niitemlist=new ArrayList<>();
        listView=(ListView)findViewById(R.id.list_add);
        db1.execSQL("CREATE TABLE IF NOT EXISTS Trip_details (id INTEGER PRIMARY KEY AUTOINCREMENT ,place_name TEXT NOT NULL, date_go DATE NOT NULL, "+"friend_no INTEGER NOT NULL DEFAULT 0);");
        Cursor c=db1.rawQuery("SELECT * FROM Trip_details ORDER BY date_go DESC;",null);
        if(c!=null)
        {if(c.moveToFirst())

            do{

                String d4=c.getString(c.getColumnIndex("place_name"));
                final Long d1=c.getLong((c.getColumnIndex("date_go")));
                String d2=c.getString(c.getColumnIndex("date_go"));
                String temp=d4;
                niitemlist.add(new additem(d4,d2));
                vec.add(1);
                j++;	}while(c.moveToNext());
        }

        adapter = new add_adapter(getApplicationContext(),R.layout.add_text,niitemlist);

        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(vec.get(i)==0){
                    // if(list_items.contains(niitemlist.get(i))) {
                    list_item.remove(niitemlist.get(i));
                    count -= 1;
                    listView.getChildAt(i).setBackgroundColor(Color.WHITE);

                    vec.set(i,1);
                    //c.setSelected(true);
                    //convertView.setPressed(true);
                    String ii=Integer.toString(i);
                    Log.e("not select",ii);

                    actionMode.setTitle(count + " items selected");
                } else {
                    count += 1;
                    listView.getChildAt(i).setBackgroundColor(Color.LTGRAY);

                    vec.set(i,0);
                    String ii=Integer.toString(i);
                    Log.e("select",ii);
                    //   nlist.add(niitemlist.get(i));

                    actionMode.setTitle(count + " items selected");
                    // list_items.add(niitemlist.get(i));
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.my_context_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                Log.e("1", "entwr switch");

                switch (menuItem.getItemId()) {
                    case R.id.delete_id:
                        for (int i = vec.size() - 1; i > -1; i--) {
                            if (vec.get(i) == 0) {
                                String name = niitemlist.get(i).getName();
                                String amount = niitemlist.get(i).getDate();
                                vec.set(i, 1);
                                listView.getChildAt(i).setBackgroundColor(Color.WHITE);
                                final String TABLE_NAME = "Trip_details";
                                final String NOTE = "note";
                                Log.e("table name", TABLE_NAME);
                                db1.execSQL("DELETE FROM " + TABLE_NAME + " WHERE place_name='" + name + "';");
                                String tb1="f"+name;
                                db1.execSQL("DROP TABLE IF EXISTS'"+tb1+"';");
                                db1.execSQL("DROP TABLE IF EXISTS'"+name+"';");
                                niitemlist.remove(i);

                            }

                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), count + " items removed ", Toast.LENGTH_SHORT).show();
                        count = 0;
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });



    }

    //when clicked on floating action button
    public void opennewwindow(){
        fb.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //
                        Intent intent= new Intent(AddPlace.this,AddPlaceDetails.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


package app.msdeep14.tripcount;

/**
 * Created by mandeep singh on 7/26/2016.
 */
public class DataNoteProvider {
    private int amount;

    public DataNoteProvider(int amount, String note, int id) {
        this.amount = amount;
        this.note = note;
        this.id = id;
    }

    private  String note;
    private  int id;

    public int getAmount() {
        return amount;
    }

    public DataNoteProvider(int id,int amount,String note){
        this.setAmount(amount);
        this.setNote(note);

    }

    public DataNoteProvider(int amount,String note){
        this.setAmount(amount);
        this.setNote(note);
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

package app.msdeep14.tripcount;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
* mainproject class
* */
public class MainActivity extends AppCompatActivity {

    FloatingActionButton ab;
    Button btnt;
    Button btnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ab=(FloatingActionButton)findViewById(R.id.about);
        btnd=(Button)findViewById(R.id.btnday);
        btnt=(Button)findViewById(R.id.btntrip);
        setTitle("Tripcount");
        OnClickButtonListener();
        OnButtonTransClicked();
        OnaboutClicked();

    }
    //dialog box to display aboutus
    public void OnaboutClicked(){
        ab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder adb=new AlertDialog.Builder(MainActivity.this);
                        adb.setMessage("developed by:\n\nmandeep singh\npawan sheoran\n\ncontact: mandeepsinghshekhawat95@gmail.com\npsheoran231@gmail.com\n\nTripcount is " +
                                "a app for managing your expenses while you are on group trips or sharing money between friends in day to day life." +
                                "\n\nThe app this divided into two modules:\n\n1.Trips\nIn this option you can add no. of friends while on trips and " +
                                "you just need the update the money paid by any friend and the app will automatically calculate who owes how much money." +
                                "\n\n2.Day to Day expenses\nIn this division you can add friends and amount paid or taken by them so that you can keep t" +
                                "rack of all your friends separately.\n\nDisclaimer:\nThanks to stackoverflow.com for sorting out my various errors\n\n" +
                                "android_guides\ndistributed by CodePath" +
                                " under The MIT License (MIT)");
                        adb.setTitle("Tripcount");
                        adb.show();
                    }
                }
        );
    }

    //activity to add new trip
    public void OnClickButtonListener(){
        btnt.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent= new Intent(MainActivity.this,AddPlace.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    //function call for transactions with friends
    public void OnButtonTransClicked(){
        btnd.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(getApplicationContext(),Display_friend.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    //when back pressed to confirm whether accidently pressed or not
    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        } else {
            Toast.makeText(this, "Press Back again to Exit",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }
}


package app.msdeep14.tripcount;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Display_friend extends AppCompatActivity {

    private String m_Text = "";
       ListView listView;
    int count=0;
    SQLiteDatabase db2 = null;
    ArrayList<DataProvider> list_item = new ArrayList<>();
    Vector<Integer> vector = new Vector<>();
    Vector<Integer> vec = new Vector<>();
    friendAdapter adapter;
    private List<DataProvider> niitemlist;
    EditText e1;
    RadioButton rb;
    RadioButton rb2;
    ImageButton f1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_friend);
        listView = (ListView) findViewById(R.id.list_friend);
        db2 = openOrCreateDatabase("trans.db", Context.MODE_PRIVATE, null);
        e1 = (EditText) findViewById(R.id.addname);
        f1=(ImageButton) findViewById(R.id.myF);

        setTitle("Friend List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        db2.execSQL("CREATE TABLE IF NOT EXISTS friends (ID INTEGER PRIMARY KEY AUTOINCREMENT ,friend TEXT NOT NULL,amount INTEGER NOT NULL DEFAULT 0);");


        Cursor c = db2.rawQuery("SELECT * FROM friends;", null);
        niitemlist=new ArrayList<>();

        if (c != null) {
            int i = 0;
            if (c.moveToFirst()) {
                do {
                    String d2 = c.getString(c.getColumnIndex("friend"));
                    int d4 = Integer.parseInt(c.getString(c.getColumnIndex("amount")));
                    niitemlist.add(new DataProvider(d4,d2));
                    vec.add(1);


                } while (c.moveToNext());
            }
        }

        try {
            adapter = new friendAdapter(getApplicationContext(),R.layout.row_layout,niitemlist);

            listView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e("hvfjdkl", "fjdkmcxz");
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
            System.out.println(e);
        }

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(vec.get(i)==0){
                    list_item.remove(niitemlist.get(i));
                    count -= 1;
                    listView.getChildAt(i).setBackgroundColor(Color.WHITE);

                    vec.set(i,1);
                    String ii=Integer.toString(i);
                    Log.e("not select",ii);

                    actionMode.setTitle(count + " items selected");
                } else {
                    count += 1;
                    listView.getChildAt(i).setBackgroundColor(Color.LTGRAY);

                    vec.set(i,0);
                    String ii=Integer.toString(i);
                    Log.e("select",ii);
                    actionMode.setTitle(count + " items selected");
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.my_context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                Log.e("1","entwr switch");

                switch (menuItem.getItemId()) {
                    case R.id.delete_id:
                        for (int i=vec.size()-1;i>-1;i--) {
                            if (vec.get(i) == 0) {
                                String name=niitemlist.get(i).getName();
                                int amount=niitemlist.get(i).getAmount();
                                vec.set(i,1);
                                listView.getChildAt(i).setBackgroundColor(Color.WHITE);
                                final String TABLE_NAME ="friends";
                                final String NOTE="note";
                                final String FRIEND = "friend_name";
                                Log.e("table name",TABLE_NAME);
                                db2.execSQL("DELETE FROM "+TABLE_NAME+" WHERE friend='"+name+"';");
                                db2.execSQL("DROP TABLE IF EXISTS " +name+ ";");
                                niitemlist.remove(i);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        Toast.makeText(getApplicationContext(), count + " items removed ", Toast.LENGTH_SHORT).show();
                        count = 0;
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        TextView temp = (TextView) view.findViewById(R.id.name);
                        String str = temp.getText().toString();
                        Intent intent = new Intent(getApplicationContext(), Display_friendDetails.class);
                        intent.putExtra("id", str);
                        startActivity(intent);
                        finish();
                    }
                }
        );

        OnFabButtonClicked();
    }

    void OnFabButtonClicked() {
        f1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int flag=0;
                        String s = e1.getText().toString().toLowerCase();
                        s=s.toLowerCase();
                        if (s.matches("")) {
                            Toast.makeText(getApplicationContext(), "field empty", Toast.LENGTH_SHORT).show();
                        } else {
                            //check if the new name already exists in the table
                            Cursor c1=db2.rawQuery("SELECT * FROM friends;",null);
                            if(c1!=null){
                                if(c1.moveToFirst()){
                                    do{
                                        String  m=c1.getString(c1.getColumnIndex("friend"));
                                        if(m.matches(e1.getText().toString().toLowerCase())){
                                            Toast.makeText(getApplicationContext(),"Name already exists",Toast.LENGTH_SHORT).show();
                                            e1.setText("");
                                            flag=1;
                                            break;
                                        }
                                    }while(c1.moveToNext());
                                }
                            }


                            if(flag==0) {
                                String COL_2 = "friend";
                                String table = "friends";
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(COL_2, e1.getText().toString().toLowerCase());
                                long result = db2.insert(table, null, contentValues);
                                if (result != -1) {
                                    //  Toast.makeText(getApplicationContext(), "friend Added", Toast.LENGTH_SHORT).show();
                                    Intent lis = new Intent(getApplicationContext(), Display_friend.class);
                                    startActivity(lis);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "friend not added!!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package app.msdeep14.tripcount;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by mandeep singh on 7/23/2016.
 */
public class Fragment_show extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    String p;
    private ListView nlist;
    private adapter_Show adapter;
    private List<item> niitemlist;
    ArrayList<item> list_items= new ArrayList<>();
    Vector<Integer> vec =new Vector<>();

    int flag=0;
    SQLiteDatabase db1=null;


    Button btn;
    int count=0;
    FloatingActionButton f;

    // newInstance constructor for creating fragment with arguments
    public static Fragment_show newInstance(int page, String title) {
        Fragment_show fragmentFirst = new Fragment_show();
        Bundle args = new Bundle();
        Log.e("fragment show","1111111111111");
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.show_fragment, container, false);

        db1 = getActivity().openOrCreateDatabase("trip.db", Context.MODE_PRIVATE, null);
        Intent in = getActivity().getIntent();
        Bundle bundle = in.getExtras();
        p = bundle.getString("id");
        f = (FloatingActionButton) view.findViewById(R.id.myFAB);
        f.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), Update.class);
                        intent.putExtra("id", p);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
        );


        nlist = (ListView) view.findViewById(R.id.list_view);
        niitemlist = new ArrayList<>();
        int x=0;
        db1.execSQL("create table if not exists " + p + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,friend_name TEXT,note TEXT,amount TEXT)");
        Cursor c = db1.rawQuery("SELECT * FROM " + p + ";", null);
        if (c != null) {
            int i = 0;
            if (c.moveToFirst()) {
                do {
                    //create list view from table place_name
                    String d2 = c.getString(c.getColumnIndex("friend_name"));
                    String d1 = c.getString(c.getColumnIndex("note"));
                    String d4 = c.getString(c.getColumnIndex("amount"));
                    vec.add(1);
                    niitemlist.add(new item(d1,d4,d2));

                } while (c.moveToNext());
            }
        }
        try {
            //adapter=new adapter_Show(Fragment_show.this,niitemlist);
            adapter =new adapter_Show(getActivity(),R.layout.textfile,niitemlist);

            nlist.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
            System.out.println(e);
        }

        nlist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        nlist.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(vec.get(i)==0){
                    list_items.remove(niitemlist.get(i));
                    count -= 1;
                    nlist.getChildAt(i).setBackgroundColor(Color.WHITE);

                    vec.set(i,1);
                    String ii=Integer.toString(i);
                    Log.e("not select",ii);

                    actionMode.setTitle(count + " items selected");
                } else {
                    count += 1;
                    nlist.getChildAt(i).setBackgroundColor(Color.LTGRAY);

                    vec.set(i,0);
                    String ii=Integer.toString(i);
                    Log.e("select",ii);
                    actionMode.setTitle(count + " items selected");
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.my_context_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                Log.e("1","entwr switch");

                switch (menuItem.getItemId()) {
                    case R.id.delete_id:
                        for (int i=vec.size()-1;i>-1;i--) {
                            if (vec.get(i) == 0) {
                                TextView t = (TextView) view.findViewById(R.id.name);
                                String name=niitemlist.get(i).getName();
                                String amount=niitemlist.get(i).getAmount();
                                String note=niitemlist.get(i).getNote();

                                vec.set(i,1);
                                nlist.getChildAt(i).setBackgroundColor(Color.WHITE);
                                final String TABLE_NAME =p;
                                final String NOTE="note";
                                final String FRIEND = "friend_name";
                                Log.e("table name",TABLE_NAME);


                                db1.delete(TABLE_NAME,
                                        FRIEND + " = ? AND " + NOTE + " = ?",
                                        new String[] {name, note+""});
                                //update fpune
                                String tb="f"+p;
                                Log.e("sdhjbs",name);
                                Cursor c3 = db1.rawQuery("SELECT * FROM " + tb + ";", null);
                                if (c3 != null) {
                                    if (c3.moveToFirst()) {
                                        do {
                                            String ch = c3.getString(c3.getColumnIndex("friend"));
                                            Log.e("friend name",ch);
                                            if (ch.matches(name)) {
                                                String temp = c3.getString(c3.getColumnIndex("amount"));
                                                int index = c3.getInt(c3.getColumnIndex("ID"));
                                                int money1 = Integer.parseInt(temp);
                                                int money2 = Integer.parseInt(amount);
                                                int money= money1 - money2;
                                                ContentValues cv=new ContentValues();
                                                cv.put("amount",money);
                                                cv.put("friend",name);
                                                db1.update(tb,cv, "ID="+index, null);
                                                temp =c3.getString(c3.getColumnIndex("amount"));
                                                break;
                                            }

                                        } while (c3.moveToNext());
                                    }
                                }

                                niitemlist.remove(i);
                            }
                            adapter.notifyDataSetChanged();
                            flag=1;
                            start();
                        }
                        Toast.makeText(getActivity(), count + " items removed ", Toast.LENGTH_SHORT).show();
                        count = 0;
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });

        return view;
    }
    public void start(){
        Intent same=new Intent(getActivity(),ViewTripDetails.class);
        same.putExtra("id",p);
        startActivity(same);
        getActivity().finish();
    }
}






package app.msdeep14.tripcount;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Display_friendDetails extends AppCompatActivity {

    String p;
    SQLiteDatabase db2=null;
    FloatingActionButton f;
    ArrayList<String> notes = new ArrayList<String>();
    ArrayList<String> list_item = new ArrayList<>();
    Vector<Integer> vector = new Vector<>();
    Vector<Integer> vec = new Vector<>();
    int count=0;
    friendDetailAdapter adapter;
    List<DataNoteProvider> niitemlist;
    ArrayAdapter adapter1;
    ListView listView;
    ListView listView1;
    RadioButton rg,rt;
    int y=0;
    int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_friend_details);

        Intent in=getIntent();
        Bundle bundle=in.getExtras();
        p=bundle.getString("id");
        setTitle(p);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listView = (ListView) findViewById(R.id.list_note);
        listView1=(ListView)findViewById(R.id.list_note);
        db2 = openOrCreateDatabase("trans.db", Context.MODE_PRIVATE, null);
        rg=(RadioButton)findViewById(R.id.given);
        rt=(RadioButton)findViewById(R.id.taken);

        niitemlist=new ArrayList<>();
        fillActivity();
        f=(FloatingActionButton)findViewById(R.id.myFAB);
        f.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //alert dialog box
                        LayoutInflater factory = LayoutInflater.from(Display_friendDetails.this);
                        final View textEntryView = factory.inflate(R.layout.alert_addtrans, null);
//text_entry is an Layout XML file containing two text field to display in alert dialog
                        final EditText input1 = (EditText) textEntryView.findViewById(R.id.rdescription);
                        final EditText input2 = (EditText) textEntryView.findViewById(R.id.ramount);
                        //  input1.setText("Enter note", TextView.BufferType.EDITABLE);
                        //  input2.setText("Enter amount", TextView.BufferType.EDITABLE);
                        final AlertDialog.Builder alert = new AlertDialog.Builder(Display_friendDetails.this);

                        alert.setIcon(R.drawable.image)
                                .setTitle("Enter Details")
                                .setView(textEntryView)
                                .setPositiveButton("Save",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                Log.i("AlertDialog", "TextEntry 1 Entered " + input1.getText().toString());
                                                Log.i("AlertDialog", "TextEntry 2 Entered " + input2.getText().toString());
                    /* User clicked OK so do some stuff */
                                                //store the values inside the friend table

                                                int z=0;
                                                if (y == 0 || input1.getText().toString().matches("") || input2.getText().toString().matches("")) {
                                                    Log.e("111111111111","1111111111");
                                                    Toast.makeText(getApplicationContext(), "Inadequate details", Toast.LENGTH_SHORT).show();}
                                                else  z=Integer.parseInt(input2.getText().toString());
                                                if(y==-1) z=(-1)*z;
                                                flag=0;
                                                if (y == 0 || input1.getText().toString().matches("") || input2.getText().toString().matches("")) {
                                                    Log.e("22222222222","2222222222");
                                                    Toast.makeText(getApplicationContext(), "Inadequate details", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    //for example breakfast is already present as a note
                                                    //and if a new note note named breakfast is added again
                                                    //then update the previous entry of breakfast;;
                                                    Cursor c2 = db2.rawQuery("SELECT * FROM " + p + ";", null);
                                                    if (c2 != null) {
                                                        if (c2.moveToFirst()) {
                                                            do {
                                                                String x = c2.getString(c2.getColumnIndex("note"));
                                                                if (x.matches(input1.getText().toString())) {
                                                                    int a = c2.getInt(c2.getColumnIndex("amount"));
                                                                    //int l=Integer.parseInt(input2.getText().toString());
                                                                    a += z;
                                                                    db2.execSQL("UPDATE " + p + " set amount = '" + a + "' WHERE note = '" + x + "';");
                                                                    flag = 1;
                                                                }
                                                            } while (c2.moveToNext());
                                                        }
                                                    }

                                                    if (flag == 0) {
                                                        // db2.execSQL("CREATE TABLE IF NOT EXISTS '"+p+"'(ID INTEGER PRIMARY KEY AUTOINCREMENT ,note TEXT NOT NULL,amount INTEGER NOT NULL DEFAULT 0);");
                                                        db2.execSQL("create table if not exists " + p + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,note TEXT,amount INTEGER NOT NULL DEFAULT 0)");
                                                        String COL_2 = "note";
                                                        String COL_3 = "amount";
                                                        String table = p;
                                                        ContentValues contentValues = new ContentValues();
                                                        contentValues.put(COL_2, input1.getText().toString());
                                                        contentValues.put(COL_3, z);
                                                        long result = db2.insert(p, null, contentValues);
                                                        if (result != -1) {
                                                            // Toast.makeText(getApplicationContext(), "note added", Toast.LENGTH_SHORT).show();
                                                            //fillActivity();
                                                            DataNoteProvider dataProvider = new DataNoteProvider(z, input1.getText().toString());
                                                            adapter.add(dataProvider);

                                                        } else
                                                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                                                    }
                                                    Cursor c3 = db2.rawQuery("SELECT * FROM friends;", null);
                                                    if (c3 != null) {
                                                        if (c3.moveToFirst()) {
                                                            do {
                                                                String t = c3.getString(c3.getColumnIndex("friend"));
                                                                if (t.matches(p)) {
                                                                    int a = c3.getInt(c3.getColumnIndex("amount"));
                                                                    a += z;
                                                                    db2.execSQL("UPDATE friends set amount = '" + a + "' WHERE friend = '" + p + "';");
                                                                }
                                                            } while (c3.moveToNext());
                                                        }
                                                    }

                                                }
                                                y=0;
                                                fillActivity();
                                                // clearlistview();
                                                //end of code added new
                                            }
                                        })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                            }
                                        });
                        alert.show();

                    }
                }
        );



    }
    public void clearlistview()
    {
        notes.clear();
        vector.clear();
        listView.setAdapter(null);
//        adapter.notifyDataSetChanged();
        fillActivity();
    }

    public int OnRadioButtonClicked(View view){
        boolean checked = ((RadioButton) view).isChecked();

        //check if radio button is checked
        switch (view.getId()){
            case R.id.given:
                if (checked)
                    y=1;
                break;
            case R.id.taken:
                if (checked)
                    y=-1;
                break;
        }

        return y;
    }

    public void fillActivity(){
        niitemlist.clear();
        //listView.setAdapter(null);
        adapter = new friendDetailAdapter(getApplicationContext(),R.layout.note_row_layout,niitemlist);

        listView.setAdapter(adapter);

        db2.execSQL("CREATE TABLE IF NOT EXISTS '"+p+"' (ID INTEGER PRIMARY KEY AUTOINCREMENT ,note TEXT NOT NULL,amount INTEGER NOT NULL DEFAULT 0);");
        db2.execSQL("create table if not exists " + p+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,note TEXT,amount INTEGER NOT NULL DEFAULT 0)");
        //display values from table into list;
        //Cursor c = db2.rawQuery("SELECT * FROM '"+p+"';", null);
        Cursor c=db2.rawQuery("SELECT * FROM "+p+";",null);
        if (c != null) {
            int i = 0;
            if (c.moveToFirst()) {
                do {
                    String d2 = c.getString(c.getColumnIndex("note"));
                    int d4 = Integer.parseInt(c.getString(c.getColumnIndex("amount")));
                    niitemlist.add(new DataNoteProvider(d4,d2));
                    vec.add(1);
                } while (c.moveToNext());
            }
        }



        adapter = new friendDetailAdapter(getApplicationContext(),R.layout.note_row_layout,niitemlist);

        listView.setAdapter(adapter);



        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(vec.get(i)==0){
                    // if(list_items.contains(niitemlist.get(i))) {
                    list_item.remove(niitemlist.get(i));
                    count -= 1;
                    listView.getChildAt(i).setBackgroundColor(Color.WHITE);

                    vec.set(i,1);
                    //c.setSelected(true);
                    //convertView.setPressed(true);
                    String ii=Integer.toString(i);
                    Log.e("not select",ii);

                    actionMode.setTitle(count + " items selected");
                } else {
                    count += 1;
                    listView.getChildAt(i).setBackgroundColor(Color.LTGRAY);

                    vec.set(i,0);
                    String ii=Integer.toString(i);
                    Log.e("select",ii);
                    //   nlist.add(niitemlist.get(i));

                    actionMode.setTitle(count + " items selected");
                    // list_items.add(niitemlist.get(i));
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.my_context_menu, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                Log.e("1", "entwr switch");

                switch (menuItem.getItemId()) {
                    case R.id.delete_id:
                        for (int i = vec.size() - 1; i > -1; i--) {
                            if (vec.get(i) == 0) {
                                String notes = niitemlist.get(i).getNote();
                                int amount = niitemlist.get(i).getAmount();
                                vec.set(i, 1);
                                listView.getChildAt(i).setBackgroundColor(Color.WHITE);
                                final String TABLE_NAME = p;
                                final String NOTE = "note";
                                final String FRIEND = "friend_name";
                                Log.e("table name", TABLE_NAME);
                                db2.execSQL("DELETE FROM " + TABLE_NAME + " WHERE note='" + notes + "';");

                                Cursor c4 = db2.rawQuery("SELECT * FROM friends;", null);
                                if (c4 != null) {
                                    if (c4.moveToFirst()) {
                                        do {
                                            String ch = c4.getString(c4.getColumnIndex("friend"));
                                            if (ch.matches(p)) {
                                                int money = c4.getInt(c4.getColumnIndex("amount"));
                                                int index = c4.getInt(c4.getColumnIndex("ID"));
                                                money = money - amount;
                                                ContentValues cv = new ContentValues();
                                                cv.put("amount", money);
                                                db2.update("friends", cv, "ID=" + index, null);
                                            }

                                        } while (c4.moveToNext());
                                    }

                                }
                                niitemlist.remove(i);
                            }
                            adapter.notifyDataSetChanged();
                        }
                        Toast.makeText(getApplicationContext(), count + " items removed ", Toast.LENGTH_SHORT).show();
                        count = 0;
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });



    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent=new Intent(getApplicationContext(),Display_friend.class);
        startActivity(intent);
        finish();

    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}


package app.msdeep14.tripcount;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hp15-p017tu on 25-07-2016.
 */



public class friendDetailAdapter extends ArrayAdapter<DataNoteProvider> {
    private SparseBooleanArray mSelectedItemsIds;
    private LayoutInflater inflater;
    private Context mContext;
    private List<DataNoteProvider> list;

    public friendDetailAdapter (Context context, int resourceId, List<DataNoteProvider> list) {
        super(context, resourceId, list);
        mSelectedItemsIds = new SparseBooleanArray();
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.list = list;
    }

    private static class ViewHolder {
        TextView id;
        TextView note;
        TextView amount;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.note_row_layout, null);
            //holder.name = (TextView) view.findViewById(R.id.name);
            holder.note = (TextView) view.findViewById(R.id.note);
            holder.amount = (TextView) view.findViewById(R.id.amount);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.note.setText(list.get(position).getNote());
        int am=list.get(position).getAmount();
        String amount=Integer.toString(am);
        holder.amount.setText(amount);
        // holder.name.setText(list.get(position).getName());
        return view;
    }

    @Override
    public void remove(DataNoteProvider remitm) {
        list.remove(remitm);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}


package app.msdeep14.tripcount;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by mandeep singh on 7/23/2016.
 * show results of balanced amount to be shared between friends
 */
public class Fragment_balance extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    String p;
    int n;
    SQLiteDatabase db1=null;
    balance_adapter adapter;
    ArrayAdapter adapter2;
    ArrayList<balancedata> list_bal=new ArrayList<>();
    ArrayList<balancedata> list_items=new ArrayList<>();
    ListView list_a,list_b;
    ArrayList<String> list=new ArrayList<String>();
    ArrayList<String> list_ball=new ArrayList<String >();

    // newInstance constructor for creating fragment with arguments
    public static Fragment_balance newInstance(int page, String title) {
        Fragment_balance fragmentSecond = new Fragment_balance();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        Log.e("frament balance","222222222");
        fragmentSecond.setArguments(args);
        return fragmentSecond;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.balance_fragment, container, false);
        List<calculate> list = new ArrayList<>();

        list_a=(ListView)view.findViewById(R.id.list_above);
        list_b=(ListView)view.findViewById(R.id.list_below);
        db1=getActivity().openOrCreateDatabase("trip.db", Context.MODE_PRIVATE, null);
        Intent in=getActivity().getIntent();
        Bundle bundle=in.getExtras();
        p=bundle.getString("id");
        Cursor c=db1.rawQuery("SELECT * FROM Trip_details ORDER BY date_go DESC;",null);
        if(c!=null){
            if(c.moveToFirst()){
                do{
                    String s=c.getString(c.getColumnIndex("place_name"));
                    if(s.matches(p)){
                        n=c.getInt(c.getColumnIndex("friend_no"));
                        break;
                    }

                }while(c.moveToNext());
            }
        }
        String table="f"+p;
        int i=0;
        c=db1.rawQuery("SELECT * FROM "+table+";",null);
        if(c!=null){
            if (c.moveToFirst()) {
                do{
                    calculate assignment1 = new calculate();
                    assignment1.name = c.getString(c.getColumnIndex("friend"));
                    assignment1.money = c.getDouble(c.getColumnIndex("amount"));
                    list.add(assignment1);
                    i++;
                }while(c.moveToNext());
            }
        }



        Collections.sort(list, new Comparator<calculate>() {
            @Override
            public int compare(calculate fruit2, calculate fruit1)
            {
                return Double.compare(fruit2.money, fruit1.money);
            }
        });

        double avg=0.0;
        for(calculate p :list)
        {
            avg+=p.money;
        }
        avg=(1.0*avg)/n;
        DecimalFormat df = new DecimalFormat("####0.00");
        for(calculate p:list){
            p.money-=avg;
            String d1=p.name;
            String d2=df.format(p.money);
            list_items.add(new balancedata(d1,d2));
        }
        adapter= new balance_adapter(getActivity(),R.layout.balance_textview,list_items);
        list_a.setAdapter(adapter);

        for(calculate p : list){
            Log.e(p.name,String.valueOf(p.money));
        }

        //update the second list for sorting out who owes whom and how much money

        int j=list.size()-1;
        i=0;
        while(i<j){
            if (Math.abs(list.get(i).money)>Math.abs(list.get(j).money)){
                list_ball.add(list.get(i).name + " owes " + list.get(j).name + " :: " + df.format(Math.abs(list.get(j).money)));
                list.get(i).money += list.get(j).money;
                list.get(j).money = 0.0;
                j--;
            }
            else if(Math.abs(list.get(i).money)<Math.abs(list.get(j).money)){
                list_ball.add(list.get(i).name + " owes " + list.get(j).name + " :: " + df.format(Math.abs(list.get(i).money)));
                list.get(j).money += list.get(i).money;
                list.get(i).money = 0.0;
                i++;
            }
            else {
                list_ball.add(list.get(i).name + " owes " + list.get(j).name + " :: " + df.format(Math.abs(list.get(i).money)));
                list.get(i).money = 0.0;
                list.get(j).money = 0.0;
                i++;
                j--;
            }
        }
        adapter2=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,list_ball);
        list_b.setAdapter(adapter2);

        return view;
    }


    public class calculate
    {
        String name;
        Double money;

    }

}


package app.msdeep14.tripcount;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hp15-p017tu on 25-07-2016.
 */



public class balance_adapter extends ArrayAdapter<balancedata> {
    private SparseBooleanArray mSelectedItemsIds;
    private LayoutInflater inflater;
    private Context mContext;
    private List<balancedata> list;

    public balance_adapter (Context context, int resourceId, List<balancedata> list) {
        super(context, resourceId, list);
        mSelectedItemsIds = new SparseBooleanArray();
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.list = list;
    }

    private static class ViewHolder {
        TextView name;
        TextView amount;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.balance_textview, null);
            holder.name = (TextView) view.findViewById(R.id.name);
            // holder.note = (TextView) view.findViewById(R.id.note);
            holder.amount = (TextView) view.findViewById(R.id.amount);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // holder.note.setText(list.get(position).getNote());
        holder.amount.setText(list.get(position).getMoney());
        holder.name.setText(list.get(position).getName());
        return view;
    }

    @Override
    public void remove(balancedata remitm) {
        list.remove(remitm);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}



package app.msdeep14.tripcount;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hp15-p017tu on 25-07-2016.
 */



public class friendAdapter extends ArrayAdapter<DataProvider> {
    private SparseBooleanArray mSelectedItemsIds;
    private LayoutInflater inflater;
    private Context mContext;
    private List<DataProvider> list;

    public friendAdapter (Context context, int resourceId, List<DataProvider> list) {
        super(context, resourceId, list);
        mSelectedItemsIds = new SparseBooleanArray();
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.list = list;
    }

    private static class ViewHolder {
        TextView id;
        TextView name;
        TextView amount;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.row_layout, null);
            holder.name = (TextView) view.findViewById(R.id.name);
            //holder.note = (TextView) view.findViewById(R.id.note);
            holder.amount = (TextView) view.findViewById(R.id.amount);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.name.setText(list.get(position).getName());
        int am=list.get(position).getAmount();
        String amount=Integer.toString(am);
        holder.amount.setText(amount);
        // holder.name.setText(list.get(position).getName());
        return view;
    }

    @Override
    public void remove(DataProvider remitm) {
        list.remove(remitm);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}


package app.msdeep14.tripcount;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

/*
* main class for fragments show_fragment and balance_fragment
* */
public class ViewTripDetails extends AppCompatActivity {

    FragmentPagerAdapter adapterViewPager;
    String p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trip_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent in=getIntent();
        Bundle bundle=in.getExtras();
        p=bundle.getString("id");
        setTitle(p);
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                // Toast.makeText(MainActivity.this, "Selected page position: " + position, Toast.LENGTH_SHORT).show();
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    Fragment f=new Fragment_show();
                    return Fragment_show.newInstance(0, null);
                case 1:
                    Fragment f2=new Fragment_balance();
                    return Fragment_balance.newInstance(1,null);


                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if(position==0)
                return "EXPENSES";
            else if(position==1) return "BALANCES";
            return null;
        }

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent in=new Intent(getApplicationContext(),AddPlace.class);
        in.putExtra("id",p);
        startActivity(in);
        finish();

    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}


package app.msdeep14.tripcount;

/**
 * Created by hp15-p017tu on 01-08-2016.
 */

/*
* adapter class for Fragment_show
* */
public class additem {
    String name;
    String date;

    public additem(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


package app.msdeep14.tripcount;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hp15-p017tu on 25-07-2016.
 */



public class adapter_Show extends ArrayAdapter<item> {
    private SparseBooleanArray mSelectedItemsIds;
    private LayoutInflater inflater;
    private Context mContext;
    private List<item> list;

    public adapter_Show (Context context, int resourceId, List<item> list) {
        super(context, resourceId, list);
        mSelectedItemsIds = new SparseBooleanArray();
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.list = list;
    }

    private static class ViewHolder {
        TextView name;
        TextView note;
        TextView amount;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.textfile, null);
            holder.name = (TextView) view.findViewById(R.id.name);
            holder.note = (TextView) view.findViewById(R.id.note);
            holder.amount = (TextView) view.findViewById(R.id.amount);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.note.setText(list.get(position).getNote());
        holder.amount.setText(list.get(position).getAmount());
        holder.name.setText(list.get(position).getName());
        return view;
    }

    @Override
    public void remove(item remitm) {
        list.remove(remitm);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}


package app.msdeep14.tripcount;

/**
 * Created by hp15-p017tu on 29-07-2016.
 */
public class balancedata {
    public String name;
    public String money;

    public balancedata(String name, String money) {
        this.name = name;
        this.money = money;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}

package app.msdeep14.tripcount;

/**
 * Created by hp15-p017tu on 25-07-2016.
 */
public class item {

    private  int id;
    private  String name;
    private String amount;
    private  String note;

    public item(int id, String name, String amount, String note) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.note = note;
    }

    public item(String note, String amount, String name){
        this.name = name;
        this.amount = amount;
        this.note = note;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }
}



