package com.android.zakaria.weatherappdemo;

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

        assertEquals("com.android.zakaria.weatherappdemo", appContext.getPackageName());
    }
}


package com.android.zakaria.weatherappdemo;

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

package com.android.zakaria.weatherappdemo.models;

import com.android.zakaria.weatherappdemo.pojos.Weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface WeatherApi {
    @GET
    Call<Weather> getWeather(@Url String url);
}


package com.android.zakaria.weatherappdemo.models;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String BASE_URL = "https://api.darksky.net/";
    public static Retrofit retrofit = null;

    public static Retrofit getRetrofitClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

package com.android.zakaria.weatherappdemo.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.zakaria.weatherappdemo.R;
import com.android.zakaria.weatherappdemo.activities.WeeklyDetailsActivity;

import java.util.ArrayList;

public class WeeklyHorizontalAdapter  extends RecyclerView.Adapter<WeeklyHorizontalAdapter.WeeklyHorizontalViewHolder> {

    private Context context;
    private ArrayList<String> weeklyDayList;
    private ArrayList<String> weeklyPercentList;
    private ArrayList<String> weeklyHighTempList;
    private ArrayList<String> weeklyLowTempList;
    private ArrayList<String> weeklyIconList;
    private int pos;


    public WeeklyHorizontalAdapter(Context context, ArrayList<String> weeklyTimeList, ArrayList<String> weeklyPercentList, ArrayList<String> weeklyTempList, ArrayList<String> weeklyLowTempList, ArrayList<String> weeklyImgList) {
        this.context = context;
        this.weeklyDayList = weeklyTimeList;
        this.weeklyPercentList = weeklyPercentList;
        this.weeklyHighTempList = weeklyTempList;
        this.weeklyLowTempList = weeklyLowTempList;
        this.weeklyIconList = weeklyImgList;
    }


    @NonNull
    @Override
    public WeeklyHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_custom_weekly_layout, parent, false);
        return new WeeklyHorizontalViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull WeeklyHorizontalViewHolder holder, final int position) {
        holder.weeklyDayTV.setText(weeklyDayList.get(position));
        holder.weeklyPercentTV.setText(weeklyPercentList.get(position));
        holder.weeklyHighTempTV.setText(weeklyHighTempList.get(position));
        holder.weeklyLowTempTV.setText(weeklyLowTempList.get(position));
        holder.weekIconView.setImageResource(R.drawable.weekly);


        holder.weeklyDayTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos = position;
                Intent intent = new Intent(context,WeeklyDetailsActivity.class);
                intent.putExtra("weekly_id", pos);
                intent.putExtra("weekly_name", weeklyDayList.get(position));
                intent.putExtra("weekly_percent", weeklyPercentList.get(position));
                intent.putExtra("weekly_high_temp", weeklyHighTempList.get(position));
                intent.putExtra("weekly_low_temp", weeklyLowTempList.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return weeklyDayList.size();
    }

    public class WeeklyHorizontalViewHolder extends RecyclerView.ViewHolder {
        TextView weeklyDayTV, weeklyPercentTV, weeklyHighTempTV, weeklyLowTempTV;
        ImageView weekIconView;

        public WeeklyHorizontalViewHolder(View itemView) {
            super(itemView);
            weeklyDayTV = itemView.findViewById(R.id.weekDayTV);
            weeklyPercentTV = itemView.findViewById(R.id.weekPercent);
            weeklyHighTempTV = itemView.findViewById(R.id.weekTempHigh);
            weeklyLowTempTV = itemView.findViewById(R.id.weekTempLow);
            weekIconView = itemView.findViewById(R.id.weekImg);

            weeklyDayTV.setPaintFlags(weeklyDayTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }
}


package com.android.zakaria.weatherappdemo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.zakaria.weatherappdemo.R;

import java.util.ArrayList;

public class HourlyHorizontalAdapter extends RecyclerView.Adapter<HourlyHorizontalAdapter.HourlyHorizontalViewHolder> {

    private Context context;
    private ArrayList<String> hourlyTimeList;
    private ArrayList<String> hourlyPercentList;
    private ArrayList<String> hourlyTempList;
    private ArrayList<String> hourlyIconList;

    public HourlyHorizontalAdapter(Context context, ArrayList<String> hourlyTimeList, ArrayList<String> hourlyPercentList, ArrayList<String> hourlyTempList, ArrayList<String> hourlyImgList) {
        this.context = context;
        this.hourlyTimeList = hourlyTimeList;
        this.hourlyPercentList = hourlyPercentList;
        this.hourlyTempList = hourlyTempList;
        this.hourlyIconList = hourlyImgList;
    }

    @NonNull
    @Override
    public HourlyHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_custom_hourly_layout, parent, false);
        return new HourlyHorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyHorizontalViewHolder holder, int position) {
        holder.hourlyTimeTV.setText(hourlyTimeList.get(position));
        holder.hourlyPercentTV.setText(hourlyPercentList.get(position));
        holder.hourlyTempTV.setText(hourlyTempList.get(position));
        holder.hourlyIconView.setImageResource(R.drawable.hourly);
    }

    @Override
    public int getItemCount() {
        return hourlyTimeList.size();
    }

    public class HourlyHorizontalViewHolder extends RecyclerView.ViewHolder {
        TextView hourlyTimeTV, hourlyPercentTV, hourlyTempTV;
        ImageView hourlyIconView;

        public HourlyHorizontalViewHolder(View itemView) {
            super(itemView);
            hourlyTimeTV = itemView.findViewById(R.id.hourlyTimeTV);
            hourlyPercentTV = itemView.findViewById(R.id.hourlyPercentTV);
            hourlyTempTV = itemView.findViewById(R.id.hourlyTempTV);
            hourlyIconView = itemView.findViewById(R.id.hourlyIconTV);
        }
    }
}

package com.android.zakaria.weatherappdemo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.zakaria.weatherappdemo.R;

import java.util.ArrayList;

public class MyListViewAdapter extends BaseAdapter {

    private Context context;
    private int[] currentMoreIconArray;
    private ArrayList<String> currentMoreArrayList;
    private String[] currentlyMoreTitleArray;


    public MyListViewAdapter(@NonNull Context context, int[] currentMoreIconArray, String[] currentlyMoreTitleArray, ArrayList<String> currentMoreArrayList) {
        //super(context, R.layout.activity_custom_current_layout);
        this.context = context;
        this.currentMoreArrayList = currentMoreArrayList;
        this.currentMoreIconArray = currentMoreIconArray;
        this.currentlyMoreTitleArray = currentlyMoreTitleArray;
    }

    @Override
    public int getCount() {
        return currentMoreArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.activity_custom_current_layout, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder();
        if (convertView == null) {
            myViewHolder.titleTextView = convertView.findViewById(R.id.currentMoreTextId);
            myViewHolder.imageView = convertView.findViewById(R.id.currentImgIcon);
            myViewHolder.dataTextView = convertView.findViewById(R.id.currentMoreDataId);

            convertView.setTag(myViewHolder);
        }
        else {
            myViewHolder = (MyViewHolder) convertView.getTag();
        }

        myViewHolder.dataTextView.setText(currentMoreArrayList.get(position));
       /* try {


        }
        catch (Exception e) {
            Log.d("my tag ", e.getMessage());
        }*/

        myViewHolder.imageView.setImageResource(currentMoreIconArray[position]);
        myViewHolder.titleTextView.setText(currentlyMoreTitleArray[position]);
        return convertView;
    }

    static class MyViewHolder {
        TextView titleTextView, dataTextView;
        ImageView imageView;
    }
}

package com.android.zakaria.weatherappdemo.connections;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.net.ConnectivityManagerCompat;

public class NetConnectionDetector {

    private Context context;

    public NetConnectionDetector(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        @SuppressLint("ServiceCast") ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }
        }
        return false;
    }
}


package com.android.zakaria.weatherappdemo.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.android.zakaria.weatherappdemo.R;

public class SplashScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);
        progressBar = findViewById(R.id.progressBarId);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startSplashActivity();
                startMainActivity();
            }
        });
        thread.start();
    }

    public void startMainActivity() {
        ActivityOptions options =
                ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out);
        startActivity(new Intent(this, MainActivity.class), options.toBundle());
        finish();
    }

    public void startSplashActivity() {
        for (int i = 20; i < 60; i=i+20) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


package com.android.zakaria.weatherappdemo.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.zakaria.weatherappdemo.adapters.HourlyHorizontalAdapter;
import com.android.zakaria.weatherappdemo.adapters.WeeklyHorizontalAdapter;
import com.android.zakaria.weatherappdemo.R;
import com.android.zakaria.weatherappdemo.connections.NetConnectionDetector;
import com.android.zakaria.weatherappdemo.models.RetrofitClient;
import com.android.zakaria.weatherappdemo.models.WeatherApi;
import com.android.zakaria.weatherappdemo.pojos.Weather;
import com.android.zakaria.weatherappdemo.shared_preferences.MySharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.android.zakaria.weatherappdemo.shared_preferences.MySharedPreferences.getMyPreferences;

public class MainActivity extends AppCompatActivity {

    private TextView currentTemp, currentHumidity, currentDate, currentSummary, more;
    private ImageView currentImgIcon;
    private ListView listView;
    private RadioGroup radioGroup;
    private boolean isFahrenheit = true;
    private LinearLayout linearBack;
    ;

    private static final String URL = "forecast/44af292051fdb50297c330b7e270abc9/";
    private WeatherApi weatherApi;
    private String customUrl;
    private String iconImgString;
    private static String area;
    private int celsius;

    private static final int TIME_DELAY = 2000;
    private static long backPressed;
    private NetConnectionDetector netConnectionDetector;

    private ArrayList<String> hourlyTimeList;
    private ArrayList<String> hourlyPercentList;
    private ArrayList<String> hourlyTempList;
    private ArrayList<String> hourlyIconList;

    private ArrayList<String> weeklyDayList;
    private ArrayList<String> weeklyPercentList;
    private ArrayList<String> weeklyHighTempList;
    private ArrayList<String> weeklyLowTempList;
    private ArrayList<String> weeklyIconList;
    private List<String> currentlyMoreArrayList;

    private String currentTempStr;
    private String currentHumidityStr;
    private String currentDateStr;
    private String currentSummaryStr;
    private MySharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentDate = findViewById(R.id.currentDate);
        currentHumidity = findViewById(R.id.currentHumidity);
        currentTemp = findViewById(R.id.currentTemp);
        currentSummary = findViewById(R.id.currentSummary);
        currentImgIcon = findViewById(R.id.currentImgIcon);
        linearBack = findViewById(R.id.linearBack);

        more = findViewById(R.id.moreTV);
        more.setPaintFlags(more.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        mySharedPreferences = getMyPreferences(this);

        isFahrenheit = mySharedPreferences.getIsTempType();
        defaultUrlDataLoad();

        netConnectionDetector = new NetConnectionDetector(this);
        if (!netConnectionDetector.isConnected() && !isSharedPreferencesHaveData()) {
            Toast.makeText(MainActivity.this, "Network not found", Toast.LENGTH_LONG).show();
        } else if (!netConnectionDetector.isConnected() && isSharedPreferencesHaveData()) {
            getSavedDataFromSharedPreferences();

            currentTemp.setText(currentTempStr + "\u2109");
            currentHumidity.setText("Humidity: " + currentHumidityStr + "%");
            currentDate.setText(currentDateStr);
            currentSummary.setText(currentSummaryStr);
            currentImgIcon.setImageResource(R.drawable.cloudy);
        }
    }

    public void setSelectedArea(String latitudeLongitude) {
        area = latitudeLongitude;
    }

    public static String getSelectedArea() {
        return area;
    }

    public void getHourlyRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView hourLyRecyclerView = findViewById(R.id.hourLyRecyclerViewId);
        hourLyRecyclerView.setLayoutManager(linearLayoutManager);

        HourlyHorizontalAdapter hourlyHorizontalAdapter = new HourlyHorizontalAdapter(this, hourlyTimeList, hourlyPercentList, hourlyTempList, hourlyIconList);
        hourLyRecyclerView.setAdapter(hourlyHorizontalAdapter);
    }

    public void getWeeklyRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView weeklyRecyclerView = findViewById(R.id.weeklyRecyclerViewId);
        weeklyRecyclerView.setLayoutManager(linearLayoutManager);
        WeeklyHorizontalAdapter weeklyHorizontalAdapter = new WeeklyHorizontalAdapter(this, weeklyDayList, weeklyPercentList, weeklyHighTempList, weeklyLowTempList, weeklyIconList);
        weeklyRecyclerView.setAdapter(weeklyHorizontalAdapter);
    }

    public void getWeatherData(String selectedLatitudeLongitude) {
        hourlyTimeList = new ArrayList<>();
        hourlyPercentList = new ArrayList<>();
        hourlyTempList = new ArrayList<>();
        hourlyIconList = new ArrayList<>();

        weeklyDayList = new ArrayList<>();
        weeklyPercentList = new ArrayList<>();
        weeklyHighTempList = new ArrayList<>();
        weeklyLowTempList = new ArrayList<>();
        weeklyIconList = new ArrayList<>();
        currentlyMoreArrayList = new ArrayList<>();

        weatherApi = RetrofitClient.getRetrofitClient().create(WeatherApi.class);
        customUrl = URL + selectedLatitudeLongitude;

        Call<Weather> callWeatherCondition = weatherApi.getWeather(customUrl);
        callWeatherCondition.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Weather weather = response.body();

                if (isFahrenheit) {
                    currentTempStr = doubleToIntToString(weather.getCurrently().getTemperature());
                    currentTemp.setText(currentTempStr + "\u2109");
                } else {
                    currentTempStr = doubleToIntToString(((weather.getCurrently().getTemperature() - 32) / 1.8));
                    currentTemp.setText(currentTempStr + "\u2103");
                }

                currentHumidityStr = doubleToIntToString((weather.getCurrently().getHumidity() * 100));
                currentHumidity.setText("Humidity: " + currentHumidityStr + "%");
                currentDateStr = getCurrentDayAndDate(weather.getCurrently().getTime());
                currentDate.setText(currentDateStr);
                currentSummaryStr = weather.getCurrently().getSummary();
                currentSummary.setText(currentSummaryStr);


                currentlyMoreArrayList.add(/*"Dew Point: " + */weather.getCurrently().getDewPoint().toString());
                currentlyMoreArrayList.add(/*"Wind Speed: " + */weather.getCurrently().getWindSpeed().toString());
                currentlyMoreArrayList.add(/*"Wind Bearing: " + */weather.getCurrently().getWindBearing().toString());
                currentlyMoreArrayList.add(/*"Cloud Cover" + */weather.getCurrently().getCloudCover().toString());
                currentlyMoreArrayList.add(/*"Pressure: " + */weather.getCurrently().getPressure().toString());
                currentlyMoreArrayList.add(/*"Ozone: " + */weather.getCurrently().getOzone().toString());

                for (int i = 0; i < 24; i++) {
                    hourlyTimeList.add(getCurrentTime(weather.getHourly().getData().get(i).getTime()));
                    hourlyPercentList.add(doubleToIntToString(weather.getHourly().getData().get(i).getHumidity() * 100) + "%");

                    if (isFahrenheit) {
                        hourlyTempList.add(doubleToIntToString(weather.getHourly().getData().get(i).getTemperature()) + "\u2109");
                    } else {
                        hourlyTempList.add(doubleToIntToString((weather.getHourly().getData().get(i).getTemperature() - 32) / 1.8) + "\u2103");
                    }
                    hourlyIconList.add(String.valueOf(R.drawable.weekly));
                }


                for (int i = 0; i < 7; i++) {
                    weeklyDayList.add(getCurrentDayName(weather.getDaily().getData().get(i).getTime()));
                    weeklyPercentList.add(doubleToIntToString(weather.getDaily().getData().get(i).getHumidity() * 100) + "%");
                    if (isFahrenheit) {
                        weeklyHighTempList.add(doubleToIntToString(weather.getDaily().getData().get(i).getTemperatureHigh()) + "\u2109");
                        weeklyLowTempList.add(doubleToIntToString(weather.getDaily().getData().get(i).getTemperatureLow()) + "\u2109");
                    } else {
                        weeklyHighTempList.add(doubleToIntToString((weather.getDaily().getData().get(i).getTemperatureHigh() - 32) / 1.8) + "\u2103");
                        weeklyLowTempList.add(doubleToIntToString((weather.getDaily().getData().get(i).getTemperatureLow() - 32) / 1.8) + "\u2103");
                    }
                    weeklyIconList.add(String.valueOf(R.drawable.weekly));
                }

                iconImgString = weather.getCurrently().getIcon();
                getCurrentIcon();

                saveDataToSharedPreferences();
                getHourlyRecyclerView();
                getWeeklyRecyclerView();
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Internet connection needed", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getCurrentIcon() {
        if (iconImgString.equals("clear-day")) {
            currentImgIcon.setImageResource(R.drawable.clear_day);
            linearBack.setBackgroundResource(R.drawable.bg_img1);
        } else if (iconImgString.equals("clear-night")) {
            currentImgIcon.setImageResource(R.drawable.clear_night);
            linearBack.setBackgroundResource(R.drawable.bd_im);
        } else if (iconImgString.equals("rain")) {
            currentImgIcon.setImageResource(R.drawable.rain_strom);
            linearBack.setBackgroundResource(R.drawable.bg_image);
        } else if (iconImgString.equals("fog")) {
            currentImgIcon.setImageResource(R.drawable.fog);
            linearBack.setBackgroundResource(R.drawable.bg_image);
        } else if (iconImgString.equals("cloudy")) {
            currentImgIcon.setImageResource(R.drawable.cloudy);
            linearBack.setBackgroundResource(R.drawable.bg_image);
        } else if (iconImgString.equals("partly-cloudy-night")) {
            currentImgIcon.setImageResource(R.drawable.partly_cloudy_night);
            linearBack.setBackgroundResource(R.drawable.bd_im);
        } else if (iconImgString.equals("partly-cloudy-day")) {
            currentImgIcon.setImageResource(R.drawable.partly_cloudy_day);
            linearBack.setBackgroundResource(R.drawable.bg_image);
        } else if (iconImgString.equals("snow")) {
            currentImgIcon.setImageResource(R.drawable.snow);
            linearBack.setBackgroundResource(R.drawable.bg_image);
        } else if (iconImgString.equals("sleet")) {
            currentImgIcon.setImageResource(R.drawable.sleet);
            linearBack.setBackgroundResource(R.drawable.bg_image);
        } else if (iconImgString.equals("wind")) {
            currentImgIcon.setImageResource(R.drawable.wind);
        } else {
            currentImgIcon.setImageResource(R.drawable.cloudy);
            linearBack.setBackgroundResource(R.drawable.bg_image);
        }
    }

    public String convertToCelsius(double fahrenheit) {
        fahrenheit = (fahrenheit - 32) / 1.8;
        return String.valueOf(fahrenheit);
    }

    public void saveDataToSharedPreferences() {
        mySharedPreferences.setCurrentTemp(currentTempStr);
        mySharedPreferences.setCurrentHumidity(currentHumidityStr);
        mySharedPreferences.setCurrentDate(currentDateStr);
        mySharedPreferences.setCurrentSummary(currentSummaryStr);
        mySharedPreferences.setIsTempType(isFahrenheit);
    }

    public void getSavedDataFromSharedPreferences() {
        currentTempStr = mySharedPreferences.getCurrentTemp();
        currentHumidityStr = mySharedPreferences.getCurrentHumidity();
        currentDateStr = mySharedPreferences.getCurrentDate();
        currentSummaryStr = mySharedPreferences.getCurrentSummary();
        isFahrenheit = mySharedPreferences.getIsTempType();
    }

    public String getCurrentDayAndDate(long unixDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMMM dd");
        String currentDate = simpleDateFormat.format(new Date(unixDate * 1000L));
        return currentDate;
    }

    public String getCurrentDayName(long unixDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE");
        String currentDate = simpleDateFormat.format(new Date(unixDate * 1000L));
        return currentDate.toUpperCase();
    }

    public String getCurrentTime(long unixDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        String currentDate = simpleDateFormat.format(new Date(unixDate * 1000L));
        return currentDate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.country_menu, menu);
        return true;
    }

    public void setUnit(MenuItem item) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.activity_unit_layout, null);
        builder.setView(view);
        builder.setTitle("Choose your type");
        builder.setIcon(R.drawable.ic_radio_button_checked_black_24dp);

        radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.celsius:
                        isFahrenheit = false;
                        break;

                    case R.id.fahrenheit:
                        isFahrenheit = true;
                        break;

                    default:
                        break;
                }
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection needed", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                } else {
                    getWeatherData(getSelectedArea());
                    dialog.cancel();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String latitudeLongitude;

        switch (item.getItemId()) {
            case R.id.dhaka:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    setSelectedArea("23.8103,90.4125");
                    setTitle("Dhaka - 24/7 Weather");
                    defaultUrlDataLoad();
                }

            case R.id.rajshahi:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "24.3636,88.6241";
                    setTitle("Rajshahi - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.chattogram:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "22.3569,91.7832";
                    setTitle("Chattogram - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.rangpur:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "25.7468,89.2508";
                    setTitle("Rangpur - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.sylhet:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "24.8949,91.8687";
                    setTitle("Sylhet - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.barishal:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "22.7010,90.3535";
                    setTitle("Barishal - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.mymensingh:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "24.7471,90.4203";
                    setTitle("Mymensingh - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.khulna:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "22.8456,89.5403";
                    setTitle("Khulna - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.sydney:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "-33.8688,151.2093";
                    setTitle("Sydney - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.victoria:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "-36.686043,143.580322";
                    setTitle("Victoria - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.melbourne:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    latitudeLongitude = "-37.8136,144.9631";
                    setTitle("Melbourne - 24/7 Weather");
                    setSelectedArea(latitudeLongitude);
                    getWeatherData(latitudeLongitude);
                    return true;
                }

            case R.id.refresh:
                if (!netConnectionDetector.isConnected()) {
                    Toast.makeText(MainActivity.this, "Internet connection not found", Toast.LENGTH_LONG).show();
                    break;
                } else {
                    getWeatherData(getSelectedArea());
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    public void defaultUrlDataLoad() {
        String savedLatitudeLongitude = "23.8103,90.4125";
        setTitle("Dhaka - 24/7 Weather");
        setSelectedArea(savedLatitudeLongitude);
        getWeatherData(getSelectedArea());
    }

    public boolean isSharedPreferencesHaveData() {
        SharedPreferences myPreference = getSharedPreferences("myPreference", Context.MODE_PRIVATE);
        String curTemp = mySharedPreferences.getCurrentTemp();
        String curHumidity = mySharedPreferences.getCurrentHumidity();
        String curDate = mySharedPreferences.getCurrentDate();
        String curSummary = mySharedPreferences.getCurrentSummary();

        if (curTemp != "" && curHumidity != "" && curDate != "" && curSummary != "") {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressed + TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        }
        backPressed = System.currentTimeMillis();
    }

    public void currentMore(View view) {
        if (netConnectionDetector.isConnected()) {
            Intent intent = new Intent(this, CurrentMoreActivity.class);
            intent.putStringArrayListExtra("current_more", (ArrayList<String>) currentlyMoreArrayList);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Internet connection needed", Toast.LENGTH_LONG).show();
        }
    }

    public String doubleToIntToString(double value) {
        return String.valueOf((int) Math.round(value));
    }
}



package com.android.zakaria.weatherappdemo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.android.zakaria.weatherappdemo.R;
import com.android.zakaria.weatherappdemo.adapters.MyListViewAdapter;

import java.util.ArrayList;

public class CurrentMoreActivity extends AppCompatActivity {

    private int[] currentlyMoreIconArray = {R.drawable.dewpoint,
            R.drawable.pressure, R.drawable.wind_speed,
            R.drawable.windgust, R.drawable.windbearing,
            R.drawable.cloudcover, R.drawable.ozone};

    private String[] currentlyMoreTitleArray;
    private ArrayList<String> currentlyMoreArrayList;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_more);

        listView = findViewById(R.id.listViewId);

        currentlyMoreArrayList =getIntent().getStringArrayListExtra("current_more");
        currentlyMoreTitleArray = getResources().getStringArray(R.array.current_more_info_title);

        MyListViewAdapter myListViewAdapter = new MyListViewAdapter(this, currentlyMoreIconArray, currentlyMoreTitleArray, currentlyMoreArrayList);
        listView.setAdapter(myListViewAdapter);
    }
}


package com.android.zakaria.weatherappdemo.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.zakaria.weatherappdemo.R;
import com.android.zakaria.weatherappdemo.models.RetrofitClient;
import com.android.zakaria.weatherappdemo.models.WeatherApi;
import com.android.zakaria.weatherappdemo.pojos.Weather;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeeklyDetailsActivity extends AppCompatActivity {

    private int id;
    private String customUrl;
    private String summaryStr;
    private WeatherApi weatherApi;
    private static final String URL = "forecast/44af292051fdb50297c330b7e270abc9/";

    private TextView summary, dewPoint, pressure, windSpeed, windGust, windBearing, cloudCover, ozone, weekName, percent, highTemp, lowTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_details);

        weekName = findViewById(R.id.weekName);
        percent = findViewById(R.id.humidityPercent);
        highTemp = findViewById(R.id.highTempTV);
        lowTemp = findViewById(R.id.lowTempTV);
        summary = findViewById(R.id.summaryTV);
        dewPoint = findViewById(R.id.dewPointTV);
        pressure = findViewById(R.id.pressureTV);
        windSpeed = findViewById(R.id.windSpeedTV);
        windGust = findViewById(R.id.windGustTV);
        windBearing = findViewById(R.id.windBearingTV);
        cloudCover = findViewById(R.id.cloudCoverTV);
        ozone = findViewById(R.id.ozoneTV);

        Intent intent = getIntent();
        id = intent.getIntExtra("weekly_id", -1);
        String nameStr = intent.getStringExtra("weekly_name");
        String percentStr = intent.getStringExtra("weekly_percent");
        String highTempStr = intent.getStringExtra("weekly_high_temp");
        String lowTempStr = intent.getStringExtra("weekly_low_temp");

        weatherApi = RetrofitClient.getRetrofitClient().create(WeatherApi.class);
        customUrl = URL + MainActivity.getSelectedArea();

        Call<Weather> callWeatherCondition = weatherApi.getWeather(customUrl);
        callWeatherCondition.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(Call<Weather> call, Response<Weather> response) {
                Weather weather = response.body();

                dewPoint.setText("dewPoint : " + weather.getDaily().getData().get(id).getDewPoint());
                pressure.setText("pressure : " + weather.getDaily().getData().get(id).getPressure());
                windSpeed.setText("windSpeed : " + weather.getDaily().getData().get(id).getWindSpeed());
                windGust.setText("windGust : " + weather.getDaily().getData().get(id).getWindGust());
                windBearing.setText("windBearing : " + weather.getDaily().getData().get(id).getWindBearing());
                cloudCover.setText("cloudCover : " + weather.getDaily().getData().get(id).getCloudCover());
                ozone.setText("ozone : " + weather.getDaily().getData().get(id).getOzone());
                summary.setText("Summary : " + weather.getDaily().getData().get(id).getSummary());
            }

            @Override
            public void onFailure(Call<Weather> call, Throwable t) {
                Toast.makeText(WeeklyDetailsActivity.this, "Internet connection needed", Toast.LENGTH_SHORT).show();
            }
        });

        weekName.setText("Day name : " + nameStr);
        percent.setText("Humidity : " + percentStr);
        highTemp.setText("High Temperature : " + highTempStr);
        lowTemp.setText("Low Temperature : " + lowTempStr);
    }
}


package com.android.zakaria.weatherappdemo.shared_preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreferences {

    private static String currentTemp;
    private static String currentHumidity;
    private static String currentDate;
    private static String currentSummary;
    private static boolean isTempType;

    private static MySharedPreferences mySharedPreferences;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private MySharedPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(Config.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public static MySharedPreferences getMyPreferences(Context context) {
        if (mySharedPreferences == null) {
            mySharedPreferences = new MySharedPreferences(context);
        }
        return mySharedPreferences;
    }

    public static void setIsTempType(boolean isTempType) {
        MySharedPreferences.isTempType = isTempType;
    }

    public static void setCurrentTemp(String currentTemp) {
        editor.putString(Config.CURRENT_TEMP, currentTemp);
        editor.apply();
    }

    public static void setCurrentHumidity(String currentHumidity) {
        editor.putString(Config.CURRENT_HUMIDITY, currentHumidity);
        editor.apply();
    }

    public static void setCurrentDate(String currentDate) {
        editor.putString(Config.CURRENT_DATE, currentDate);
        editor.apply();
    }

    public static void setCurrentSummary(String currentSummary) {
        editor.putString(Config.CURRENT_SUMMARY, currentSummary);
        editor.apply();
    }

    public static boolean getIsTempType() {
        return isTempType;
    }

    public static String getCurrentTemp() {
        return sharedPreferences.getString(Config.CURRENT_TEMP, "");
    }

    public static String getCurrentHumidity() {
        return sharedPreferences.getString(Config.CURRENT_HUMIDITY, "");
    }

    public static String getCurrentDate() {
        return sharedPreferences.getString(Config.CURRENT_DATE, "");
    }

    public static String getCurrentSummary() {
        return sharedPreferences.getString(Config.CURRENT_SUMMARY, "");
    }
}


package com.android.zakaria.weatherappdemo.shared_preferences;

public class Config {
    public static final String SHARED_PREFERENCES_NAME = "mySavedPreferences";
    public static final String CURRENT_TEMP = "currentTemp";
    public static final String CURRENT_HUMIDITY = "currentHumidity";
    public static final String CURRENT_DATE = "currentDate";
    public static final String CURRENT_SUMMARY = "currentSummary";
    public static final String IS_TEMP_TYPE = "temperatureType";
}



package com.android.zakaria.weatherappdemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum {

    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("precipIntensity")
    @Expose
    private Double precipIntensity;
    @SerializedName("precipProbability")
    @Expose
    private Double precipProbability;
    @SerializedName("precipType")
    @Expose
    private String precipType;
    @SerializedName("temperature")
    @Expose
    private Double temperature;
    @SerializedName("apparentTemperature")
    @Expose
    private Double apparentTemperature;
    @SerializedName("dewPoint")
    @Expose
    private Double dewPoint;
    @SerializedName("humidity")
    @Expose
    private Double humidity;
    @SerializedName("pressure")
    @Expose
    private Double pressure;
    @SerializedName("windSpeed")
    @Expose
    private Double windSpeed;
    @SerializedName("windGust")
    @Expose
    private Double windGust;
    @SerializedName("windBearing")
    @Expose
    private Integer windBearing;
    @SerializedName("cloudCover")
    @Expose
    private Double cloudCover;
    @SerializedName("uvIndex")
    @Expose
    private Integer uvIndex;
    @SerializedName("visibility")
    @Expose
    private Double visibility;
    @SerializedName("ozone")
    @Expose
    private Double ozone;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Double getPrecipIntensity() {
        return precipIntensity;
    }

    public void setPrecipIntensity(Double precipIntensity) {
        this.precipIntensity = precipIntensity;
    }

    public Double getPrecipProbability() {
        return precipProbability;
    }

    public void setPrecipProbability(Double precipProbability) {
        this.precipProbability = precipProbability;
    }

    public String getPrecipType() {
        return precipType;
    }

    public void setPrecipType(String precipType) {
        this.precipType = precipType;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getApparentTemperature() {
        return apparentTemperature;
    }

    public void setApparentTemperature(Double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public Double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(Double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getWindGust() {
        return windGust;
    }

    public void setWindGust(Double windGust) {
        this.windGust = windGust;
    }

    public Integer getWindBearing() {
        return windBearing;
    }

    public void setWindBearing(Integer windBearing) {
        this.windBearing = windBearing;
    }

    public Double getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(Double cloudCover) {
        this.cloudCover = cloudCover;
    }

    public Integer getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
    }

    public Double getVisibility() {
        return visibility;
    }

    public void setVisibility(Double visibility) {
        this.visibility = visibility;
    }

    public Double getOzone() {
        return ozone;
    }

    public void setOzone(Double ozone) {
        this.ozone = ozone;
    }

}



package com.android.zakaria.weatherappdemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Hourly {

    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

}



package com.android.zakaria.weatherappdemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Flags {

    @SerializedName("sources")
    @Expose
    private List<String> sources = null;
    @SerializedName("isd-stations")
    @Expose
    private List<String> isdStations = null;
    @SerializedName("units")
    @Expose
    private String units;

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<String> getIsdStations() {
        return isdStations;
    }

    public void setIsdStations(List<String> isdStations) {
        this.isdStations = isdStations;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

}



package com.android.zakaria.weatherappdemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("currently")
    @Expose
    private Currently currently;
    @SerializedName("hourly")
    @Expose
    private Hourly hourly;
    @SerializedName("daily")
    @Expose
    private Daily daily;
    @SerializedName("flags")
    @Expose
    private Flags flags;
    @SerializedName("offset")
    @Expose
    private Integer offset;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Currently getCurrently() {
        return currently;
    }

    public void setCurrently(Currently currently) {
        this.currently = currently;
    }

    public Hourly getHourly() {
        return hourly;
    }

    public void setHourly(Hourly hourly) {
        this.hourly = hourly;
    }

    public Daily getDaily() {
        return daily;
    }

    public void setDaily(Daily daily) {
        this.daily = daily;
    }

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

}



package com.android.zakaria.weatherappdemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum_ {

    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("sunriseTime")
    @Expose
    private Integer sunriseTime;
    @SerializedName("sunsetTime")
    @Expose
    private Integer sunsetTime;
    @SerializedName("moonPhase")
    @Expose
    private Double moonPhase;
    @SerializedName("precipIntensity")
    @Expose
    private Double precipIntensity;
    @SerializedName("precipIntensityMax")
    @Expose
    private Double precipIntensityMax;
    @SerializedName("precipIntensityMaxTime")
    @Expose
    private Integer precipIntensityMaxTime;
    @SerializedName("precipProbability")
    @Expose
    private Double precipProbability;
    @SerializedName("precipType")
    @Expose
    private String precipType;
    @SerializedName("temperatureHigh")
    @Expose
    private Double temperatureHigh;
    @SerializedName("temperatureHighTime")
    @Expose
    private Integer temperatureHighTime;
    @SerializedName("temperatureLow")
    @Expose
    private Double temperatureLow;
    @SerializedName("temperatureLowTime")
    @Expose
    private Integer temperatureLowTime;
    @SerializedName("apparentTemperatureHigh")
    @Expose
    private Double apparentTemperatureHigh;
    @SerializedName("apparentTemperatureHighTime")
    @Expose
    private Integer apparentTemperatureHighTime;
    @SerializedName("apparentTemperatureLow")
    @Expose
    private Double apparentTemperatureLow;
    @SerializedName("apparentTemperatureLowTime")
    @Expose
    private Integer apparentTemperatureLowTime;
    @SerializedName("dewPoint")
    @Expose
    private Double dewPoint;
    @SerializedName("humidity")
    @Expose
    private Double humidity;
    @SerializedName("pressure")
    @Expose
    private Double pressure;
    @SerializedName("windSpeed")
    @Expose
    private Double windSpeed;
    @SerializedName("windGust")
    @Expose
    private Double windGust;
    @SerializedName("windGustTime")
    @Expose
    private Integer windGustTime;
    @SerializedName("windBearing")
    @Expose
    private Integer windBearing;
    @SerializedName("cloudCover")
    @Expose
    private Double cloudCover;
    @SerializedName("uvIndex")
    @Expose
    private Integer uvIndex;
    @SerializedName("uvIndexTime")
    @Expose
    private Integer uvIndexTime;
    @SerializedName("visibility")
    @Expose
    private Double visibility;
    @SerializedName("ozone")
    @Expose
    private Double ozone;
    @SerializedName("temperatureMin")
    @Expose
    private Double temperatureMin;
    @SerializedName("temperatureMinTime")
    @Expose
    private Integer temperatureMinTime;
    @SerializedName("temperatureMax")
    @Expose
    private Double temperatureMax;
    @SerializedName("temperatureMaxTime")
    @Expose
    private Integer temperatureMaxTime;
    @SerializedName("apparentTemperatureMin")
    @Expose
    private Double apparentTemperatureMin;
    @SerializedName("apparentTemperatureMinTime")
    @Expose
    private Integer apparentTemperatureMinTime;
    @SerializedName("apparentTemperatureMax")
    @Expose
    private Double apparentTemperatureMax;
    @SerializedName("apparentTemperatureMaxTime")
    @Expose
    private Integer apparentTemperatureMaxTime;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSunriseTime() {
        return sunriseTime;
    }

    public void setSunriseTime(Integer sunriseTime) {
        this.sunriseTime = sunriseTime;
    }

    public Integer getSunsetTime() {
        return sunsetTime;
    }

    public void setSunsetTime(Integer sunsetTime) {
        this.sunsetTime = sunsetTime;
    }

    public Double getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(Double moonPhase) {
        this.moonPhase = moonPhase;
    }

    public Double getPrecipIntensity() {
        return precipIntensity;
    }

    public void setPrecipIntensity(Double precipIntensity) {
        this.precipIntensity = precipIntensity;
    }

    public Double getPrecipIntensityMax() {
        return precipIntensityMax;
    }

    public void setPrecipIntensityMax(Double precipIntensityMax) {
        this.precipIntensityMax = precipIntensityMax;
    }

    public Integer getPrecipIntensityMaxTime() {
        return precipIntensityMaxTime;
    }

    public void setPrecipIntensityMaxTime(Integer precipIntensityMaxTime) {
        this.precipIntensityMaxTime = precipIntensityMaxTime;
    }

    public Double getPrecipProbability() {
        return precipProbability;
    }

    public void setPrecipProbability(Double precipProbability) {
        this.precipProbability = precipProbability;
    }

    public String getPrecipType() {
        return precipType;
    }

    public void setPrecipType(String precipType) {
        this.precipType = precipType;
    }

    public Double getTemperatureHigh() {
        return temperatureHigh;
    }

    public void setTemperatureHigh(Double temperatureHigh) {
        this.temperatureHigh = temperatureHigh;
    }

    public Integer getTemperatureHighTime() {
        return temperatureHighTime;
    }

    public void setTemperatureHighTime(Integer temperatureHighTime) {
        this.temperatureHighTime = temperatureHighTime;
    }

    public Double getTemperatureLow() {
        return temperatureLow;
    }

    public void setTemperatureLow(Double temperatureLow) {
        this.temperatureLow = temperatureLow;
    }

    public Integer getTemperatureLowTime() {
        return temperatureLowTime;
    }

    public void setTemperatureLowTime(Integer temperatureLowTime) {
        this.temperatureLowTime = temperatureLowTime;
    }

    public Double getApparentTemperatureHigh() {
        return apparentTemperatureHigh;
    }

    public void setApparentTemperatureHigh(Double apparentTemperatureHigh) {
        this.apparentTemperatureHigh = apparentTemperatureHigh;
    }

    public Integer getApparentTemperatureHighTime() {
        return apparentTemperatureHighTime;
    }

    public void setApparentTemperatureHighTime(Integer apparentTemperatureHighTime) {
        this.apparentTemperatureHighTime = apparentTemperatureHighTime;
    }

    public Double getApparentTemperatureLow() {
        return apparentTemperatureLow;
    }

    public void setApparentTemperatureLow(Double apparentTemperatureLow) {
        this.apparentTemperatureLow = apparentTemperatureLow;
    }

    public Integer getApparentTemperatureLowTime() {
        return apparentTemperatureLowTime;
    }

    public void setApparentTemperatureLowTime(Integer apparentTemperatureLowTime) {
        this.apparentTemperatureLowTime = apparentTemperatureLowTime;
    }

    public Double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(Double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getWindGust() {
        return windGust;
    }

    public void setWindGust(Double windGust) {
        this.windGust = windGust;
    }

    public Integer getWindGustTime() {
        return windGustTime;
    }

    public void setWindGustTime(Integer windGustTime) {
        this.windGustTime = windGustTime;
    }

    public Integer getWindBearing() {
        return windBearing;
    }

    public void setWindBearing(Integer windBearing) {
        this.windBearing = windBearing;
    }

    public Double getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(Double cloudCover) {
        this.cloudCover = cloudCover;
    }

    public Integer getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
    }

    public Integer getUvIndexTime() {
        return uvIndexTime;
    }

    public void setUvIndexTime(Integer uvIndexTime) {
        this.uvIndexTime = uvIndexTime;
    }

    public Double getVisibility() {
        return visibility;
    }

    public void setVisibility(Double visibility) {
        this.visibility = visibility;
    }

    public Double getOzone() {
        return ozone;
    }

    public void setOzone(Double ozone) {
        this.ozone = ozone;
    }

    public Double getTemperatureMin() {
        return temperatureMin;
    }

    public void setTemperatureMin(Double temperatureMin) {
        this.temperatureMin = temperatureMin;
    }

    public Integer getTemperatureMinTime() {
        return temperatureMinTime;
    }

    public void setTemperatureMinTime(Integer temperatureMinTime) {
        this.temperatureMinTime = temperatureMinTime;
    }

    public Double getTemperatureMax() {
        return temperatureMax;
    }

    public void setTemperatureMax(Double temperatureMax) {
        this.temperatureMax = temperatureMax;
    }

    public Integer getTemperatureMaxTime() {
        return temperatureMaxTime;
    }

    public void setTemperatureMaxTime(Integer temperatureMaxTime) {
        this.temperatureMaxTime = temperatureMaxTime;
    }

    public Double getApparentTemperatureMin() {
        return apparentTemperatureMin;
    }

    public void setApparentTemperatureMin(Double apparentTemperatureMin) {
        this.apparentTemperatureMin = apparentTemperatureMin;
    }

    public Integer getApparentTemperatureMinTime() {
        return apparentTemperatureMinTime;
    }

    public void setApparentTemperatureMinTime(Integer apparentTemperatureMinTime) {
        this.apparentTemperatureMinTime = apparentTemperatureMinTime;
    }

    public Double getApparentTemperatureMax() {
        return apparentTemperatureMax;
    }

    public void setApparentTemperatureMax(Double apparentTemperatureMax) {
        this.apparentTemperatureMax = apparentTemperatureMax;
    }

    public Integer getApparentTemperatureMaxTime() {
        return apparentTemperatureMaxTime;
    }

    public void setApparentTemperatureMaxTime(Integer apparentTemperatureMaxTime) {
        this.apparentTemperatureMaxTime = apparentTemperatureMaxTime;
    }

}



package com.android.zakaria.weatherappdemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Currently {

    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("precipIntensity")
    @Expose
    private Double precipIntensity;
    @SerializedName("precipProbability")
    @Expose
    private Double precipProbability;
    @SerializedName("precipType")
    @Expose
    private String precipType;
    @SerializedName("temperature")
    @Expose
    private Double temperature;
    @SerializedName("apparentTemperature")
    @Expose
    private Double apparentTemperature;
    @SerializedName("dewPoint")
    @Expose
    private Double dewPoint;
    @SerializedName("humidity")
    @Expose
    private Double humidity;
    @SerializedName("pressure")
    @Expose
    private Double pressure;
    @SerializedName("windSpeed")
    @Expose
    private Double windSpeed;
    @SerializedName("windGust")
    @Expose
    private Double windGust;
    @SerializedName("windBearing")
    @Expose
    private Integer windBearing;
    @SerializedName("cloudCover")
    @Expose
    private Double cloudCover;
    @SerializedName("uvIndex")
    @Expose
    private Integer uvIndex;
    @SerializedName("visibility")
    @Expose
    private Double visibility;
    @SerializedName("ozone")
    @Expose
    private Double ozone;

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Double getPrecipIntensity() {
        return precipIntensity;
    }

    public void setPrecipIntensity(Double precipIntensity) {
        this.precipIntensity = precipIntensity;
    }

    public Double getPrecipProbability() {
        return precipProbability;
    }

    public void setPrecipProbability(Double precipProbability) {
        this.precipProbability = precipProbability;
    }

    public String getPrecipType() {
        return precipType;
    }

    public void setPrecipType(String precipType) {
        this.precipType = precipType;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getApparentTemperature() {
        return apparentTemperature;
    }

    public void setApparentTemperature(Double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public Double getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(Double dewPoint) {
        this.dewPoint = dewPoint;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getWindGust() {
        return windGust;
    }

    public void setWindGust(Double windGust) {
        this.windGust = windGust;
    }

    public Integer getWindBearing() {
        return windBearing;
    }

    public void setWindBearing(Integer windBearing) {
        this.windBearing = windBearing;
    }

    public Double getCloudCover() {
        return cloudCover;
    }

    public void setCloudCover(Double cloudCover) {
        this.cloudCover = cloudCover;
    }

    public Integer getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(Integer uvIndex) {
        this.uvIndex = uvIndex;
    }

    public Double getVisibility() {
        return visibility;
    }

    public void setVisibility(Double visibility) {
        this.visibility = visibility;
    }

    public Double getOzone() {
        return ozone;
    }

    public void setOzone(Double ozone) {
        this.ozone = ozone;
    }

}



package com.android.zakaria.weatherappdemo.pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Daily {

    @SerializedName("summary")
    @Expose
    private String summary;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("data")
    @Expose
    private List<Datum_> data = null;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<Datum_> getData() {
        return data;
    }

    public void setData(List<Datum_> data) {
        this.data = data;
    }

}


