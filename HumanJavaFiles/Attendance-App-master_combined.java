package com.sabikrahat.attendanceapp;

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
        assertEquals("com.sabikrahat.attendanceapp", appContext.getPackageName());
    }
}

package com.sabikrahat.attendanceapp;

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

package com.sabikrahat.attendanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class ShowAttendanceSheet extends AppCompatActivity {

    private WebView webView;
    private TextView tv;
    private String URL = "http://www.muthosoft.com/univ/attendance/report.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attendance_sheet);

        webView = findViewById(R.id.webViewId);
        tv = findViewById(R.id.textView_title);
        Intent intent = getIntent();
        String crsCode = intent.getStringExtra("courseCode");

        tv.setText(crsCode);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        String[] keys = {"CSE489-Lab", "year", "semester", "course", "section", "sid"};
        String[] values = {"true", "2022", "1", crsCode, "2", "2019160256"};
        httpRequest(keys, values);
    }

    private void httpRequest(final String keys[], final String values[]) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    for (int i = 0; i < keys.length; i++) {
                        System.out.println(keys[i] + " " + values[i]);
                        params.add(new BasicNameValuePair(keys[i], values[i]));
                    }
                    String data = JSONParser.getInstance().makeHttpRequest(URL, "POST", params);
                    System.out.println(data);
                    return data;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String data) {
                if (data != null) {
                    try {
                        System.out.println("Final data Html Body: " + data);
                        webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}

package com.sabikrahat.attendanceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class ShowCourseList extends AppCompatActivity {

    private GridView gridView;

    private String URL = "https://muthosoft.com/univ/attendance/report.php";
    private ArrayList<Course> courseList;
    private CustomCourseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_course_list);

        gridView = findViewById(R.id.gridViewId);
        String[] keys = {"my_courses", "sid"};
        String[] values = {"true", "2019160256"};
        httpRequest(keys, values);

        gridView.setOnItemClickListener((adapterView, view, i, l) -> {
            Course course = courseList.get(i);
            Intent intent = new Intent(ShowCourseList.this, ShowAttendanceSheet.class);
            intent.putExtra("courseCode", course.courseCode);
            startActivity(intent);
        });

        findViewById(R.id.exitBtn).setOnClickListener(v -> finish());
    }


    private void httpRequest(final String keys[], final String values[]) {
        courseList = new ArrayList<>();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    for (int i = 0; i < keys.length; i++) {
                        System.out.println("Rahat In For Loop" + keys[i] + " " + values[i]);
                        params.add(new BasicNameValuePair(keys[i], values[i]));
                    }
                    String data = JSONParser.getInstance().makeHttpRequest(URL, "POST", params);
                    System.out.println("Rahat OutSide For Loop: " + data);
                    System.out.println("Rahat OutSide For Loop: " + data.split(",")[0]);
                    System.out.println("Rahat OutSide For Loop: " + data.split(",")[1]);
                    System.out.println("Rahat OutSide For Loop: " + data.split(",")[2]);
                    courseList.add(new Course(data.split(",")[0], data.split(",")[1], data.split(",")[2]));
                    return data;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String data) {
                if (data != null) {
                    try {
                        System.out.println("Final data Html Body: " + data);
                        adapter = new CustomCourseAdapter(ShowCourseList.this, courseList);
                        gridView.setAdapter(adapter);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}

package com.sabikrahat.attendanceapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class CustomCourseAdapter extends ArrayAdapter<Course> {

    private final Context context;
    private final ArrayList<Course> values;


    public CustomCourseAdapter(@NonNull Context context, @NonNull ArrayList<Course> objects) {
        super(context, -1, objects);
        this.context = context;
        this.values = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.layout_grid_item, parent, false);

        TextView crsCode = rowView.findViewById(R.id.courseCode);
        TextView crsTitle = rowView.findViewById(R.id.courseTitle);
        TextView crsInstructor = rowView.findViewById(R.id.courseInstructor);

        crsCode.setText(values.get(position).getCourseCode());
        crsTitle.setText(values.get(position).getCourseTitle());
        crsInstructor.setText(values.get(position).getInstructor());

        return rowView;
    }
}

package com.sabikrahat.attendanceapp;

public class Course {
    String courseCode = "";
    String courseTitle = "";
    String instructor = "";

    public Course() {
    }

    public Course(String courseCode, String courseTitle, String instructor) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.instructor = instructor;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }
}


package com.sabikrahat.attendanceapp;


import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("ALL")
public class JSONParser {

    private String TAG = "JSONParser";
    private static JSONParser instance = new JSONParser();
    private JSONParser() {}
    public static JSONParser getInstance() {
        return instance;
    }

    public String makeHttpRequest(String url, String method, List<NameValuePair> params) {

        HttpURLConnection http = null;
        InputStream is = null;
        String data = "";
        // Making HTTP request
        try {
            // check for request method
            if (method == "POST") {
                //httpClient = new DefaultHttpClient();
                if(params != null) {
                    String paramString = URLEncodedUtils.format(params, "utf-8");
                    url += "?" + paramString;
                }
            }
            System.out.println("@JSONParser-"+": "+ url);
            URL urlc = new URL(url);
            http = (HttpURLConnection) urlc.openConnection();
            //System.out.println("Here 2");
            http = (HttpURLConnection) urlc.openConnection();
            //System.out.println("Here 3");
            http.connect();
            is = http.getInputStream();
            //System.out.println("Here 4");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            //System.out.println("@JSONParser_later-"+": "+ sb.toString());
            is.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error Found: " + e.getMessage());
        }
        try {
            http.disconnect();
        } catch (Exception e) {
        }
        return null;
    }
}


