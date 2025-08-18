package com.yangdai.calc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 30415
 */
public class MyCrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "MyCrashHandler";
    private static Thread.UncaughtExceptionHandler mDefaultHandler;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private final Map<String, String> stringHashMap = new HashMap<>();

    private MyCrashHandler() {
    }

    /**
     * 初始化
     */
    public static void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new MyCrashHandler());
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        handleException(ex);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "error : ", e);
        }

        if (mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        }

    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     */
    private void handleException(Throwable ex) {
        if (ex == null) {
            return;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Looper.prepare();//准备发消息的MessageQueue
            Toast.makeText(mContext, mContext.getString(R.string.errorMsg), Toast.LENGTH_LONG).show();
            Looper.loop();
        });
        executorService.shutdown();
        //收集设备参数信息
        collectDeviceInfo(mContext);
        //保存日志文件
        saveCrashInfo2File(ex);
    }


    /**
     * 保存错误信息到文件中
     */
    private void saveCrashInfo2File(Throwable ex) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : stringHashMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            Date date = new Date(System.currentTimeMillis());
            @SuppressLint("SimpleDateFormat")
            String str = new SimpleDateFormat("yyyyMMddHHmmss").format(date);

            String fileName = "Calc-Crash-Log-" + str + ".txt";
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "an error occurred while writing file...", e);
        }
    }

    /**
     * 收集设备参数信息
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = String.valueOf(pi.getLongVersionCode());
                stringHashMap.put("versionName", versionName);
                stringHashMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occurred when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                stringHashMap.put(field.getName(), Objects.requireNonNull(field.get(null)).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occurred when collect crash info", e);
            }
        }
    }
}

package com.yangdai.calc;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.window.embedding.RuleController;

import com.google.android.material.color.DynamicColors;

/**
 * @author 30415
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        SharedPreferences defaultSp = getDefaultSharedPreferences(getApplicationContext());
        int theme = defaultSp.getInt("themeSetting", 2);
        switch (theme) {
            case 0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            case 1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            case 2 ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            default -> {
            }
        }
        boolean isActivityEmbeddingEnabled = defaultSp.getBoolean("split", false);
        if (isActivityEmbeddingEnabled) {
            RuleController.getInstance(this).setRules(RuleController.parseRules(this, R.xml.main_split_config));
        }
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            MyCrashHandler.init(getApplicationContext());
            Toast.makeText(this, "Debug", Toast.LENGTH_SHORT).show();
        }
    }
}


package com.yangdai.calc.features;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.algebra.StatisticsActivity;
import com.yangdai.calc.main.toolbox.functions.compass.Compass;
import com.yangdai.calc.main.toolbox.functions.converter.UnitActivity;
import com.yangdai.calc.main.toolbox.functions.currency.CurrencyActivity;
import com.yangdai.calc.main.toolbox.functions.finance.FinanceActivity;
import com.yangdai.calc.main.MainActivity;
import com.yangdai.calc.main.toolbox.functions.shopping.ShoppingActivity;
import com.yangdai.calc.main.toolbox.functions.time.DateRangeActivity;

/**
 * @author 30415
 */
public class MyWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_BUTTON_0 = "com.yangdai.calc.main.MainActivity";
    private static final String ACTION_BUTTON_2 = "com.yangdai.calc.main.toolbox.functions.converter.UnitActivity";
    private static final String ACTION_BUTTON_3 = "com.yangdai.calc.exchange.CurrencyActivity";
    private static final String ACTION_BUTTON_4 = "com.yangdai.calc.main.toolbox.functions.time.DateRangeActivity";
    private static final String ACTION_BUTTON_5 = "com.yangdai.calc.main.toolbox.functions.shopping.ShoppingActivity";
    private static final String ACTION_BUTTON_6 = "com.yangdai.calc.main.toolbox.functions.compass.Compass";
    private static final String ACTION_BUTTON_7 = "com.yangdai.calc.main.toolbox.functions.finance.FinanceActivity";
    private static final String ACTION_BUTTON_1 = "com.yangdai.calc.main.toolbox.functions.algebra.StatisticsActivity";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            setOnClickPendingIntent(context, views, R.id.calculator, MainActivity.class, ACTION_BUTTON_0);
            setOnClickPendingIntent(context, views, R.id.button1, StatisticsActivity.class, ACTION_BUTTON_1);
            setOnClickPendingIntent(context, views, R.id.button2, UnitActivity.class, ACTION_BUTTON_2);
            setOnClickPendingIntent(context, views, R.id.button3, CurrencyActivity.class, ACTION_BUTTON_3);
            setOnClickPendingIntent(context, views, R.id.button4, DateRangeActivity.class, ACTION_BUTTON_4);
            setOnClickPendingIntent(context, views, R.id.button5, ShoppingActivity.class, ACTION_BUTTON_5);
            setOnClickPendingIntent(context, views, R.id.button6, Compass.class, ACTION_BUTTON_6);
            setOnClickPendingIntent(context, views, R.id.button7, FinanceActivity.class, ACTION_BUTTON_7);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_BUTTON_0.equals(intent.getAction())) {
            startNewTaskActivity(context, MainActivity.class);
        } else if (ACTION_BUTTON_1.equals(intent.getAction())) {
            startNewTaskActivity(context, StatisticsActivity.class);
        } else if (ACTION_BUTTON_2.equals(intent.getAction())) {
            startNewTaskActivity(context, UnitActivity.class);
        } else if (ACTION_BUTTON_3.equals(intent.getAction())) {
            startNewTaskActivity(context, CurrencyActivity.class);
        } else if (ACTION_BUTTON_4.equals(intent.getAction())) {
            startNewTaskActivity(context, DateRangeActivity.class);
        } else if (ACTION_BUTTON_5.equals(intent.getAction())) {
            startNewTaskActivity(context, ShoppingActivity.class);
        } else if (ACTION_BUTTON_6.equals(intent.getAction())) {
            startNewTaskActivity(context, Compass.class);
        } else if (ACTION_BUTTON_7.equals(intent.getAction())) {
            startNewTaskActivity(context, FinanceActivity.class);
        }
    }

    private void setOnClickPendingIntent(Context context, RemoteViews views, int viewId, Class<?> activityClass, String action) {
        Intent intent = new Intent(context, activityClass);
        intent.setAction(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(viewId, pendingIntent);
    }

    private void startNewTaskActivity(Context context, Class<?> activityClass) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

package com.yangdai.calc.features;

import static com.yangdai.calc.main.calculator.CalculatorUtils.highlightSpecialSymbols;
import static com.yangdai.calc.utils.Utils.formatNumber;
import static com.yangdai.calc.utils.Utils.isNumber;
import static com.yangdai.calc.utils.Utils.isNumeric;
import static com.yangdai.calc.utils.Utils.isSymbol;
import static com.yangdai.calc.utils.Utils.removeZeros;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.icu.math.BigDecimal;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.yangdai.calc.R;
import com.yangdai.calc.main.MainActivity;
import com.yangdai.calc.main.calculator.Calculator;
import com.yangdai.calc.utils.TouchAnimation;

/**
 * @author 30415
 */
public class FloatingWindow extends Service implements View.OnClickListener {

    private ViewGroup floatView;
    private WindowManager.LayoutParams floatWindowLayoutParam;
    private WindowManager windowManager;
    private TextView inputView, outputView;
    private int left = 0, right = 0;
    private ColorStateList color;
    private static final int[] BUTTON_IDS = {R.id.div, R.id.mul, R.id.sub, R.id.add, R.id.seven,
            R.id.eight, R.id.nine, R.id.brackets, R.id.four, R.id.five, R.id.six, R.id.inverse, R.id.delete,
            R.id.three, R.id.two, R.id.one, R.id.dot, R.id.zero, R.id.equal, R.id.Clean};
    private boolean isSmallSize = true;
    private float currentAlpha = 1.0f;

    /*
     API >= 26, TYPE_APPLICATION_OVERLAY, 否则 TYPE.SYSTEM.ALERT
     */
    private static final int LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate() {
        super.onCreate();
        // 计算屏幕宽高
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        int trueWidth, trueHeight;
        boolean isHeightSmaller = width >= height;
        if (isHeightSmaller) {
            trueHeight = (int) (height * (0.45f));
            trueWidth = trueHeight * 10 / 16;
        } else {
            trueWidth = (int) (width * (0.38f));
            trueHeight = (int) (height * (0.32f));
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);

        ImageView btMaximize = floatView.findViewById(R.id.open_in_full);
        ImageView btExit = floatView.findViewById(R.id.close_float);
        ImageView btResize = floatView.findViewById(R.id.resizeWindow);
        ImageView btTransparency = floatView.findViewById(R.id.transparency);
        inputView = floatView.findViewById(R.id.edit);
        outputView = floatView.findViewById(R.id.view);
        color = outputView.getTextColors();
        outputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals(getString(R.string.formatError)) || editable.toString().equals(getString(R.string.bigNum))) {
                    outputView.setTextColor(getColor(R.color.wrong));
                } else {
                    outputView.setTextColor(color);
                }
            }
        });

        for (int buttonId : BUTTON_IDS) {
            View view1 = floatView.findViewById(buttonId);
            if (null != view1) {
                view1.setOnClickListener(this);
                TouchAnimation touchAnimation = new TouchAnimation(view1);
                view1.setOnTouchListener(touchAnimation);
            }
        }

        floatWindowLayoutParam = new WindowManager.LayoutParams(
                trueWidth,
                trueHeight,
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        // 显示在屏幕中央
        floatWindowLayoutParam.gravity = Gravity.CENTER;
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        // 添加到屏幕
        windowManager.addView(floatView, floatWindowLayoutParam);

        btMaximize.setOnClickListener(v -> {
            // 返回应用 MainActivity
            Intent backToHome = new Intent(FloatingWindow.this, MainActivity.class);

            // 1) FLAG_ACTIVITY_NEW_TASK flag helps activity to start a new task on the history stack.
            // If a task is already running like the floating window service, a new activity will not be started.
            // Instead the task will be brought back to the front just like the MainActivity here
            // 2) FLAG_ACTIVITY_CLEAR_TASK can be used in the conjunction with FLAG_ACTIVITY_NEW_TASK. This flag will
            // kill the existing task first and then new activity is started.
            backToHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(backToHome);
            exit();
        });

        btExit.setOnClickListener(v -> exit());
        btResize.setOnClickListener(v -> {
            if (isSmallSize) {
                // 将悬浮窗大小设置为大尺寸
                floatWindowLayoutParam.width = (int) (trueWidth * 1.3f);
                floatWindowLayoutParam.height = (int) (trueHeight * 1.3f);
            } else {
                // 将悬浮窗大小设置为小尺寸
                floatWindowLayoutParam.width = trueWidth;
                floatWindowLayoutParam.height = trueHeight;
            }
            // 更新悬浮窗的布局参数
            windowManager.updateViewLayout(floatView, floatWindowLayoutParam);
            // 切换大小标志
            isSmallSize = !isSmallSize;
        });
        btTransparency.setOnClickListener(v -> {
            if (currentAlpha == 1.0f) {
                // 将悬浮窗透明度设置为 0.5
                floatWindowLayoutParam.alpha = 0.6f;
                currentAlpha = 0.6f;
            } else {
                // 将悬浮窗透明度设置为 1.0
                floatWindowLayoutParam.alpha = 1.0f;
                currentAlpha = 1.0f;
            }
            // 更新悬浮窗的布局参数
            windowManager.updateViewLayout(floatView, floatWindowLayoutParam);
        });

        // 实现拖动悬浮窗
        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    // 记录第一次触碰位置
                    case MotionEvent.ACTION_DOWN -> {
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;


                        px = event.getRawX();
                        py = event.getRawY();
                    }
                    // 更新位置
                    case MotionEvent.ACTION_MOVE -> {
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);
                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                    }
                }
                return false;
            }
        });


    }

    // It is called when stopService()
    // method is called in MainActivity
    @Override
    public void onDestroy() {
        super.onDestroy();
        exit();
    }

    private void exit() {
        stopSelf();
        windowManager.removeView(floatView);
    }

    @Override
    public void onClick(View v) {
        //获取输入
        String inputStr = inputView.getText().toString();
        Calculator formulaUtil = new Calculator(false);

        try {
            if (v.getId() == R.id.equal && !inputStr.isEmpty()) {
                handleEqualButton(inputStr, formulaUtil, true);
            } else if (v.getId() == R.id.Clean) {
                handleCleanButton();
            } else if (v.getId() == R.id.delete) {
                handleDeleteButton(inputStr);
            } else if (v.getId() == R.id.brackets) {
                handleBracketsButton(inputStr);
            } else if (v.getId() == R.id.inverse) {
                handleInverseButton(inputStr);
            } else {
                handleOtherButtons(v, inputStr);
            }
            String inputStr1 = inputView.getText().toString();
            highlightSpecialSymbols(inputView);
            //自动运算
            if (!inputStr1.isEmpty()) {
                Calculator formulaUtil1 = new Calculator(false);
                handleEqualButton(inputStr1, formulaUtil1, false);
            }
        } catch (Exception e) {
            outputView.setText("");
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleEqualButton(String inputStr, Calculator formulaUtil, boolean clicked) {
        // 忽略特殊情况
        if (inputStr.isEmpty() || isNumeric(inputStr)) {
            outputView.setText("");
            return;
        }

        // 补全
        if (isSymbol(String.valueOf(inputStr.charAt(inputStr.length() - 1)))) {
            inputStr += "0";
        } else if (isSymbol(String.valueOf(inputStr.charAt(0))) || inputStr.charAt(0) == '%') {
            inputStr = "0" + inputStr;
        }

        // 处理左右括号数量不一致的情况
        if (left != right) {
            int addCount = Math.abs(left - right);
            StringBuilder inputStrBuilder = new StringBuilder(inputStr);
            for (int j = 0; j < addCount; j++) {
                inputStrBuilder.append(")");
            }
            inputStr = inputStrBuilder.toString();
        }

        try {
            BigDecimal bigDecimal = formulaUtil.calc(inputStr);
            if (null == bigDecimal) {
                outputView.setText(getString(R.string.bigNum));
                return;
            }
            bigDecimal = bigDecimal.setScale(10, BigDecimal.ROUND_HALF_UP);
            String res = bigDecimal.toBigDecimal().toPlainString();
            res = removeZeros(res);

            if (clicked) {
                inputView.setText(res);
                outputView.setText("");
            } else {
                outputView.setText(formatNumber(res));
            }
        } catch (Exception e) {
            if (clicked) {
                outputView.setText(getString(R.string.formatError));
            }
        }
    }

    private void handleCleanButton() {
        inputView.setText("");
        outputView.setText("");
        left = 0;
        right = 0;
    }

    private void handleDeleteButton(String inputStr) {
        if (!inputStr.isEmpty()) {
            if (inputStr.endsWith("asin(") || inputStr.endsWith("acos(")
                    || inputStr.endsWith("atan(") || inputStr.endsWith("acot(")) {
                inputStr = inputStr.substring(0, inputStr.length() - 5);
                left--;
            } else if (inputStr.endsWith("sin(") || inputStr.endsWith("cos(") || inputStr.endsWith("exp(")
                    || inputStr.endsWith("tan(") || inputStr.endsWith("cot(") || inputStr.endsWith("log(")) {
                inputStr = inputStr.substring(0, inputStr.length() - 4);
                left--;
            } else if (inputStr.endsWith("ln(")) {
                inputStr = inputStr.substring(0, inputStr.length() - 3);
                left--;
            } else {
                char lastChar = inputStr.charAt(inputStr.length() - 1);
                if (lastChar == ')') {
                    right--;
                }
                if (lastChar == '(') {
                    left--;
                }
                inputStr = inputStr.substring(0, inputStr.length() - 1);
            }
            inputView.setText(inputStr);
        }
        if (inputStr.isEmpty()) {
            outputView.setText("");
        }
    }

    @SuppressLint("SetTextI18n")
    private void handleBracketsButton(String inputStr) {
        if (!inputStr.isEmpty()) {
            char lastChar = inputStr.charAt(inputStr.length() - 1);
            if (left > right && isNumber(String.valueOf(lastChar))
                    || left > right && lastChar == '%' || left > right && lastChar == ')') {
                inputView.setText(inputStr + ")");
                right++;
                return;
            } else if (lastChar == ')' || isNumber(String.valueOf(lastChar))) {
                inputView.setText(inputStr + "×(");
            } else {
                inputView.setText(inputStr + "(");
            }
        } else {
            inputView.setText(inputStr + "(");
        }
        left++;
    }

    @SuppressLint("SetTextI18n")
    private void handleInverseButton(String inputStr) {
        // 取反
        if (!inputStr.isEmpty()) {
            char lastChar = inputStr.charAt(inputStr.length() - 1);
            //最后一位是数字
            if (isNumber(String.valueOf(lastChar))) {
                StringBuilder n = new StringBuilder();
                n.insert(0, lastChar);
                // 如果长度大于一， 从后向前遍历直到数字前一位
                if (inputStr.length() > 1) {
                    for (int i = inputStr.length() - 2; i >= 0; i--) {
                        char curr = inputStr.charAt(i);
                        if (isNumber(String.valueOf(curr)) || curr == '.') {
                            n.insert(0, curr);
                        } else {
                            // 遇到负号，如果负号前是 (-，则去掉 (-
                            if (curr == '-') {
                                if (i >= 1 && "(-".equals(inputStr.substring(i - 1, i + 1))) {
                                    inputStr = inputStr.substring(0, i - 1);
                                    inputView.setText(inputStr + n);
                                    left--;
                                    return;
                                }
                            }  // + × ÷ (  ^ 特殊情况 )
                            inputStr = inputStr.substring(0, i + 1);
                            String prefix = (curr == ')') ? "×(-" : "(-";
                            inputView.setText(inputStr + prefix + n);
                            left++;
                            return;
                        }
                    }
                }
                //只有数字
                inputView.setText("(-" + n);
                left++;
                return;
            } else if (lastChar == '-') {
                // 最后是 (-， 直接去掉
                if (inputStr.length() > 1 && (inputStr.charAt(inputStr.length() - 2) == '(')) {
                    inputView.setText(inputStr.substring(0, inputStr.length() - 2));
                    left--;
                    return;
                }
            }
            String prefix = (lastChar == ')' || lastChar == '!') ? "×(-" : "(-";
            inputView.setText(inputStr + prefix);
        } else {
            inputView.setText("(-");
        }
        left++;
    }

    @SuppressLint("SetTextI18n")
    private void handleOtherButtons(View v, String inputStr) {
        String append = ((MaterialButton) v).getText().toString();

        //长度大于0时
        if (!inputStr.isEmpty()) {
            char lastInput = inputStr.charAt(inputStr.length() - 1);
            // )、e、π 后输入数字默认加上 ×
            if (isNumber(append)) {
                if (")".equals(String.valueOf(lastInput))
                        || "e".equals(String.valueOf(lastInput)) || "π".equals(String.valueOf(lastInput))) {
                    inputView.setText(inputStr + "×" + append);
                    return;
                }
            }
            // 最后一位是两数运算符号时，再次输入符号则替换最后一位
            if (isSymbol(String.valueOf(lastInput)) && isSymbol(append)) {
                inputView.setText(inputStr.substring(0, inputStr.length() - 1) + append);
                return;
            }
            // 最后一位是数字时，输入e、π默认加上 ×
            if (isNumber(String.valueOf(lastInput))) {
                if ("e".equals(append) || "π".equals(append)) {
                    inputView.setText(inputStr + "×" + append);
                    return;
                }
            }
        }

        inputView.setText(inputStr + append);
    }
}


package com.yangdai.calc.features;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.yangdai.calc.R;
import com.yangdai.calc.main.calculator.Calculator;
import com.yangdai.calc.main.calculator.CalculatorUtils;
import com.yangdai.calc.utils.Utils;

/**
 * @author 30415
 */
public class MyWidgetProviderCalc1 extends AppWidgetProvider {

    private static final String ACTION_BUTTON_CLICK = "com.yangdai.calc.BUTTON_CLICK1";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_calc1);
            // Set up click listeners for the calculator buttons
            setButtonClickListeners(context, views);
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() != null && intent.getAction().equals(ACTION_BUTTON_CLICK)) {
            // Handle button click action
            String buttonValue = intent.getStringExtra("buttonValue");
            if (buttonValue != null) {
                Log.e("input", buttonValue);
            }
            updateTextView(context, buttonValue);
        }
    }

    private void setButtonClickListeners(Context context, RemoteViews views) {
        // Set up click listeners for the calculator buttons
        int[] buttonIds = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5,
                R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.buttonDelete, R.id.buttonAdd,
                R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide, R.id.buttonDecimal, R.id.buttonEquals,
                R.id.buttonClean, R.id.buttonPercentage, R.id.buttonPower};

        String[] buttonSymbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫",
                "+", "-", "×", "÷", ".", "=", "C", "%", "^"};

        for (int i = 0; i < buttonIds.length; i++) {
            views.setOnClickPendingIntent(buttonIds[i], getButtonClickPendingIntent(context, buttonSymbols[i], i));
        }
    }

    private PendingIntent getButtonClickPendingIntent(Context context, String buttonSymbol, int requestCode) {
        Intent intent = new Intent(context, this.getClass());
        intent.setAction(ACTION_BUTTON_CLICK);
        intent.putExtra("buttonValue", buttonSymbol);
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void updateTextView(Context context, String buttonValue) {
        RemoteViews views1 = new RemoteViews(context.getPackageName(), R.layout.widget_calc1);
        SharedPreferences sharedPreferences = context.getSharedPreferences("widget_content1", Context.MODE_PRIVATE);
        String input = sharedPreferences.getString("text", "");
        if ("=".equals(buttonValue)) {
            try {
                input = CalculatorUtils.optimizePercentage(input);
                input = input.replace("%", "÷100");
                BigDecimal res = evaluateExpression(input);
                input = res == null ? "" : Utils.removeZeros(res.setScale(10, BigDecimal.ROUND_HALF_UP).toString());
            } catch (Exception e) {
                input = context.getString(R.string.formatError);
            }
        } else if ("⌫".equals(buttonValue)) {
            if (!input.isEmpty()) {
                input = input.substring(0, input.length() - 1);
            }
        } else if ("C".equals(buttonValue)) {
            input = "";
        } else {
            input = input + buttonValue;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("text", input);
        editor.apply();
        views1.setTextViewText(R.id.textViewResult, input);
        // Update the widget
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, this.getClass()), views1);
    }

    private BigDecimal evaluateExpression(String expression) {
        Calculator calculator = new Calculator(false);
        return calculator.calc(expression);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // 清空SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("widget_content1", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_calc1);
        // Set up click listeners for the calculator buttons
        setButtonClickListeners(context, views);
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}


package com.yangdai.calc.features;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.yangdai.calc.R;
import com.yangdai.calc.main.calculator.Calculator;
import com.yangdai.calc.main.calculator.CalculatorUtils;
import com.yangdai.calc.utils.Utils;

/**
 * @author 30415
 */
public class MyWidgetProviderCalc2 extends AppWidgetProvider {

    private static final String ACTION_BUTTON_CLICK = "com.yangdai.calc.BUTTON_CLICK2";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_calc2);

            // Set up click listeners for the calculator buttons
            setButtonClickListeners(context, views);
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() != null && intent.getAction().equals(ACTION_BUTTON_CLICK)) {
            // Handle button click action
            String buttonValue = intent.getStringExtra("buttonValue");
            if (buttonValue != null) {
                Log.e("input", buttonValue);
            }
            updateTextView(context, buttonValue);
        }
    }

    private void setButtonClickListeners(Context context, RemoteViews views) {
        // Set up click listeners for the calculator buttons
        int[] buttonIds = {R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5,
                R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.buttonDelete, R.id.buttonAdd,
                R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide, R.id.buttonDecimal, R.id.buttonEquals,
                R.id.buttonClean, R.id.buttonPercentage, R.id.buttonPower};

        String[] buttonSymbols = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫",
                "+", "-", "×", "÷", ".", "=", "C", "%", "^"};

        for (int i = 0; i < buttonIds.length; i++) {
            views.setOnClickPendingIntent(buttonIds[i], getButtonClickPendingIntent(context, buttonSymbols[i], i));
        }
    }

    private PendingIntent getButtonClickPendingIntent(Context context, String buttonSymbol, int requestCode) {
        Intent intent = new Intent(context, this.getClass());
        intent.setAction(ACTION_BUTTON_CLICK);
        intent.putExtra("buttonValue", buttonSymbol);
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void updateTextView(Context context, String buttonValue) {
        RemoteViews views1 = new RemoteViews(context.getPackageName(), R.layout.widget_calc2);
        SharedPreferences sharedPreferences = context.getSharedPreferences("widget_content2", Context.MODE_PRIVATE);
        String input = sharedPreferences.getString("text", "");
        if ("=".equals(buttonValue)) {
            try {
                input = CalculatorUtils.optimizePercentage(input);
                input = input.replace("%", "÷100");
                BigDecimal res = evaluateExpression(input);
                input = res == null ? "" : Utils.removeZeros(res.setScale(10, BigDecimal.ROUND_HALF_UP).toString());
            } catch (Exception e) {
                input = context.getString(R.string.formatError);
            }
        } else if ("⌫".equals(buttonValue)) {
            if (!input.isEmpty()) {
                input = input.substring(0, input.length() - 1);
            }
        } else if ("C".equals(buttonValue)) {
            input = "";
        } else {
            input = input + buttonValue;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("text", input);
        editor.apply();
        views1.setTextViewText(R.id.textViewResult, input);
        // Update the widget
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, this.getClass()), views1);
    }

    private BigDecimal evaluateExpression(String expression) {
        Calculator calculator = new Calculator(false);
        return calculator.calc(expression);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // 清空SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("widget_content2", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_calc2);
        // Set up click listeners for the calculator buttons
        setButtonClickListeners(context, views);
        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


package com.yangdai.calc.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.net.URISyntaxException;

/**
 * @author 30415
 */
public class PaymentUtil {
    /**
     * 旧版支付宝二维码通用 Intent Scheme Url 格式
     */
    private static final String INTENT_URL_FORMAT = "intent://platformapi/startapp?saId=10000007&" +
            "clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s" +
            "%3Dweb-other&_t=1472443966571#Intent;" +
            "scheme=alipayqr;package=com.eg.android.AlipayGphone;end";

    /**
     * 打开转账窗口
     * 旧版支付宝二维码方法，需要使用 <a href="https://fama.alipay.com/qrcode/index.htm">...</a> 网站生成的二维码
     * 这个方法最好，但在 2016 年 8 月发现新用户可能无法使用
     *
     * @param activity Parent Activity
     * @param urlCode  手动解析二维码获得地址中的参数，例如 <a href="https://qr.alipay.com/aehvyvf4taxxxxxxx">...</a> 最后那段
     */
    public static void startAlipayClient(Activity activity, String urlCode) {
        startIntentUrl(activity, INTENT_URL_FORMAT.replace("{urlCode}", urlCode));
    }

    /**
     * 打开 Intent Scheme Url
     *
     * @param activity      Parent Activity
     * @param intentFullUrl Intent 跳转地址
     */
    public static void startIntentUrl(Activity activity, String intentFullUrl) {
        try {
            Intent intent = Intent.parseUri(
                    intentFullUrl,
                    Intent.URI_INTENT_SCHEME
            );
            activity.startActivity(intent);
        } catch (URISyntaxException | ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断支付宝客户端是否已安装，建议调用转账前检查
     *
     * @param context Context
     * @return 支付宝客户端是否已安装
     */
    public static boolean isInstalledPackage(Context context) {
        Uri uri = Uri.parse("alipays://platformapi/startApp");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        ComponentName componentName = intent.resolveActivity(context.getPackageManager());
        return componentName != null;
    }
}

package com.yangdai.calc.utils;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * @author 30415
 */
public class TouchAnimation implements View.OnTouchListener {
    private static final float SCALE_DOWN_FACTOR = 0.75f;
    private static final long ANIMATION_DURATION = 100;

    private final View view;
    private ObjectAnimator xAnimator;
    private ObjectAnimator yAnimator;

    public TouchAnimation(View view) {
        this.view = view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN -> {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                    startScaleDownAnimation();
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE);
                    startScaleUpAnimation();
                }
                default -> {
                }
            }
        } catch (Exception ignored) {

        }
        return false;
    }

    private void startScaleDownAnimation() {
        cancelAnimations(); // 取消之前的动画

        xAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, SCALE_DOWN_FACTOR);
        yAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, SCALE_DOWN_FACTOR);

        xAnimator.setDuration(ANIMATION_DURATION);
        yAnimator.setDuration(ANIMATION_DURATION);

        xAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        yAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        xAnimator.start();
        yAnimator.start();
    }

    private void startScaleUpAnimation() {
        cancelAnimations(); // 取消之前的动画

        xAnimator = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.0f);
        yAnimator = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.0f);

        xAnimator.setDuration(ANIMATION_DURATION);
        yAnimator.setDuration(ANIMATION_DURATION);

        xAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        yAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        xAnimator.start();
        yAnimator.start();
    }

    private void cancelAnimations() {
        if (xAnimator != null && xAnimator.isRunning()) {
            xAnimator.cancel();
        }
        if (yAnimator != null && yAnimator.isRunning()) {
            yAnimator.cancel();
        }
    }
}


package com.yangdai.calc.utils;

/**
 * @author 30415
 */
public interface TTSInitializationListener {
    void onTTSInitialized(boolean isSuccess);
}


package com.yangdai.calc.utils;

import static android.content.Context.VIBRATOR_MANAGER_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.icu.math.BigDecimal;
import android.icu.number.NumberFormatter;
import android.icu.number.Precision;
import android.icu.text.NumberFormat;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author 30415
 */
public class Utils {
    private static VibratorManager vibratorManager = null;
    private static Vibrator vibrator = null;

    /**
     * 震动方法
     */
    public static void vibrate(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (null == vibratorManager || null == vibrator) {
                    vibratorManager = (VibratorManager) context.getSystemService(VIBRATOR_MANAGER_SERVICE);
                    vibrator = vibratorManager.getDefaultVibrator();
                }
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                if (null == vibrator) {
                    vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                }
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * 判断是否是数字
     */
    public static boolean isNumber(String num) {
        return num.equals("0") || num.equals("1") || num.equals("2") || num.equals("3") || num.equals("4")
                || num.equals("5") || num.equals("6") || num.equals("7") || num.equals("8") || num.equals("9")
                || num.equals("e") || num.equals("π");
    }

    /**
     * 判断是否是运算符
     */
    public static boolean isSymbol(String s) {
        return s.equals("+") || s.equals("-") || s.equals("×") || s.equals(".") || s.equals("^")
                || s.equals("÷");
    }

    /**
     * 格式化数字
     */
    public static String formatNumber(String number) {
        try {
            BigDecimal bigDecimal = new BigDecimal(number);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return NumberFormatter
                        .with()
                        .locale(Locale.getDefault())
                        .precision(Precision.maxFraction(10))
                        .format(bigDecimal)
                        .toString();
            } else {
                NumberFormat numberFormat = NumberFormat.getNumberInstance();
                numberFormat.setMaximumFractionDigits(10);
                return numberFormat.format(bigDecimal);
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 格式化金融数字
     */
    public static String formatNumberFinance(String number) {
        try {
            BigDecimal bigDecimal = new BigDecimal(number);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return NumberFormatter.withLocale(Locale.getDefault())
                        .precision(Precision.maxFraction(2))
                        .format(bigDecimal)
                        .toString();
            } else {
                NumberFormat numberFormat = NumberFormat.getNumberInstance();
                numberFormat.setMaximumFractionDigits(2);
                return numberFormat.format(bigDecimal);
            }
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 移除多余的 0
     */
    public static String removeZeros(String num) {
        if (num == null || num.isEmpty()) {
            return "";
        }

        if (!num.contains(".")) {
            return num;
        }

        // 分割整数部分和小数部分
        String[] parts = num.split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts[1];

        // 移除小数部分的尾随0
        decimalPart = decimalPart.replaceAll("0+?$", "");

        // 如果小数部分为空，则返回整数部分
        if (decimalPart.isEmpty()) {
            return integerPart;
        }

        // 合并整数部分和小数部分
        return integerPart + "." + decimalPart;
    }

    public static void closeKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    /*
     * 判断是否是单一数字
     */
    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * 计算最大公约数
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        return a.gcd(b);
    }

    /**
     * 计算最小公倍数
     */
    public static BigInteger lcm(BigInteger a, BigInteger b) {
        try {
            return (a.multiply(b)).divide(gcd(a, b));
        } catch (Exception e) {
            return BigInteger.ZERO;
        }
    }

    /**
     * 计算多个数最大公约数
     */
    public static BigInteger gcdMultiple(List<BigInteger> numbers) {
        BigInteger result = numbers.get(0);
        for (int i = 1; i < numbers.size(); i++) {
            result = gcd(result, numbers.get(i));
        }
        return result;
    }

    /**
     * 计算多个数的最小公倍数
     */
    public static BigInteger lcmMultiple(List<BigInteger> numbers) {
        BigInteger result = numbers.get(0);
        for (int i = 1; i < numbers.size(); i++) {
            result = lcm(result, numbers.get(i));
        }
        return result;
    }

    /**
     * 分数转小数方法，适用于有限小数和循环小数
     */
    public static String fractionToDecimal(int numerator, int denominator) {
        long numeratorLong = numerator;
        long denominatorLong = denominator;
        // 能整除直接返回
        if (numeratorLong % denominatorLong == 0) {
            return String.valueOf(numeratorLong / denominatorLong);
        }

        StringBuilder sb = new StringBuilder();
        // 用异或符号判断负号
        if (numeratorLong < 0 ^ denominatorLong < 0) {
            sb.append('-');
        }

        numeratorLong = Math.abs(numeratorLong);
        denominatorLong = Math.abs(denominatorLong);

        // 整数部分
        long integerPart = numeratorLong / denominatorLong;
        sb.append(integerPart);
        sb.append('.');

        // 小数部分
        StringBuilder fractionPart = getFractionPart(numeratorLong, denominatorLong);

        sb.append(fractionPart);
        return sb.toString();
    }

    @NonNull
    private static StringBuilder getFractionPart(long numeratorLong, long denominatorLong) {
        StringBuilder fractionPart = new StringBuilder();
        Map<Long, Integer> remainderIndexMap = new HashMap<>();
        long remainder = numeratorLong % denominatorLong;

        int index = 0;
        while (remainder != 0 && !remainderIndexMap.containsKey(remainder)) {
            remainderIndexMap.put(remainder, index);
            remainder *= 10;
            fractionPart.append(remainder / denominatorLong);
            remainder %= denominatorLong;
            index++;
        }
        if (remainder != 0) {
            // 有循环节
            Integer insertIndex = remainderIndexMap.get(remainder);
            if (insertIndex != null) {
                fractionPart.insert(insertIndex.intValue(), '(');
                fractionPart.append(')');
            }
        }
        return fractionPart;
    }

    /**
     * 小数转分数方法，适用于有限小数
     */
    public static String decimalToFractionForNonRepeating(double decimal) {
        String stringNumber = String.valueOf(decimal);
        int numberDigitsDecimals = stringNumber.length() - 1 - stringNumber.indexOf('.');
        int denominator = 1;
        for (int i = 0; i < numberDigitsDecimals; i++) {
            decimal *= 10;
            denominator *= 10;
        }
        int numerator = (int) Math.round(decimal);
        int greatestCommonFactor = gcd(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator)).intValue();
        int numerator1 = numerator / greatestCommonFactor;
        int denominator1 = denominator / greatestCommonFactor;
        return numerator1 + " / " + denominator1;
    }

    /**
     * 小数转分数方法，适用于循环小数
     */
    public static String decimalToFractionForRepeating(String decimal) {
        int wholePart = Integer.parseInt(decimal.substring(0, decimal.indexOf(".")));
        int nonRepeatingPart = Integer.parseInt(decimal.substring(decimal.indexOf(".") + 1, decimal.indexOf("(")));
        int repeatingPart = Integer.parseInt(decimal.substring(decimal.indexOf("(") + 1, decimal.indexOf(")")));

        int nonRepeatingLength = decimal.indexOf("(") - decimal.indexOf(".") - 1;
        int repeatingLength = decimal.indexOf(")") - decimal.indexOf("(") - 1;

        int denominator = (int) Math.pow(10, nonRepeatingLength + repeatingLength) - (int) Math.pow(10, nonRepeatingLength);

        int numerator = (nonRepeatingPart * (int) Math.pow(10, repeatingLength) + repeatingPart) - nonRepeatingPart;

        int wholeNumerator = wholePart * denominator;

        numerator += wholeNumerator;

        int gcd = gcd(BigInteger.valueOf(Math.abs(numerator)), BigInteger.valueOf(denominator)).intValue();
        numerator /= gcd;
        denominator /= gcd;

        return numerator + " / " + denominator;
    }

    /**
     * 通用小数转分数方法
     */
    public static String decimalToFraction(String decimal) {
        try {
            if (decimal.contains("(")) {
                if (!decimal.contains(")")) {
                    decimal += ")";
                }
                return decimalToFractionForRepeating(decimal);
            } else {
                return decimalToFractionForNonRepeating(Double.parseDouble(decimal));
            }
        } catch (Exception e) {
            return "A / B";
        }
    }

}


package com.yangdai.calc.utils;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.yangdai.calc.R;

import java.util.Locale;

/**
 * @author 30415
 */
public class TTS implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private Context mContext;
    private TTSInitializationListener initializationListener;

    public boolean ttsCreate(Activity activity, TTSInitializationListener initializationListener) {
        //语音初始化
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }

        mContext = activity.getApplicationContext();
        this.initializationListener = initializationListener;
        // 创建新的TextToSpeech对象并设置语言
        try {
            textToSpeech = new TextToSpeech(mContext, this);
            return true;
        } catch (Exception exception) {
            textToSpeech = null;
            return false;
        }
    }

    public void ttsDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
            // 关闭，释放资源
        }
    }

    public void ttsSpeak(String content) {
        if (textToSpeech != null && !content.isEmpty()) {
            content = content.replace("=", mContext.getString(R.string.equal))
                    .replace("(", mContext.getString(R.string.bracket))
                    .replace(")", mContext.getString(R.string.bracket))
                    .replace("!!", mContext.getString(R.string.double_factorial))
                    .replace("!", mContext.getString(R.string.factorial))
                    .replace("%", mContext.getString(R.string.percentage))
                    .replace("^", mContext.getString(R.string.power))
                    .replace(".", mContext.getString(R.string.point))
                    .replace("+", mContext.getString(R.string.addNum))
                    .replace("-", mContext.getString(R.string.minusNum))
                    .replace("×", mContext.getString(R.string.multiplyNum))
                    .replace("÷", mContext.getString(R.string.divideNum))
                    .replace("asin", mContext.getString(R.string.asin))
                    .replace("acos", mContext.getString(R.string.acos))
                    .replace("atan", mContext.getString(R.string.atan))
                    .replace("acot", mContext.getString(R.string.acot))
                    .replace("sin", mContext.getString(R.string.sin))
                    .replace("cos", mContext.getString(R.string.cos))
                    .replace("tan", mContext.getString(R.string.tan))
                    .replace("cot", mContext.getString(R.string.cot))
                    .replace("log", mContext.getString(R.string.log))
                    .replace("ln", mContext.getString(R.string.ln));
            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // 获取应用语言设置
            Locale appLocale = mContext.getResources().getConfiguration().getLocales().get(0);

            // 设置TextToSpeech的语言为应用语言
            if (textToSpeech != null) {
                int result = textToSpeech.setLanguage(appLocale);
                textToSpeech.setSpeechRate(1.2f);
                textToSpeech.setPitch(0.8f);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech = null;
                    // 语言数据缺失或不支持，无法进行语音播报
                    Log.e("TTS", "Language pack is missing");
                    initializationListener.onTTSInitialized(false);
                }
            } else {
                Log.e("TTS", "TextToSpeech object is null");
                initializationListener.onTTSInitialized(false);
            }
        } else {
            textToSpeech = null;
            // 可能遇到手机内置讯飞tts
            textToSpeech = new TextToSpeech(mContext, i -> {
                if (i == TextToSpeech.SUCCESS) {
                    // 获取应用语言设置
                    Locale appLocale = mContext.getResources().getConfiguration().getLocales().get(0);

                    // 设置TextToSpeech的语言为应用语言
                    if (textToSpeech != null) {
                        int result = textToSpeech.setLanguage(appLocale);
                        textToSpeech.setSpeechRate(1.2f);
                        textToSpeech.setPitch(0.8f);

                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            textToSpeech = null;
                            // 语言数据缺失或不支持，无法进行语音播报
                            Log.e("TTS", "Language pack is missing");
                            initializationListener.onTTSInitialized(false);
                        }
                    } else {
                        Log.e("TTS", "TextToSpeech object is null");
                        initializationListener.onTTSInitialized(false);
                    }
                } else {
                    textToSpeech = null;
                    // 初始化TextToSpeech失败
                    Log.e("TTS", "Failed to initialize TTS");
                    initializationListener.onTTSInitialized(false);
                }
            }, "com.iflytek.speechsuite");
        }
    }
}



package com.yangdai.calc.main;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

/**
 * 自定义适用于viewpager2的页面切换动画，用于历史记录和计算器界面的切换
 *
 * @author 30415
 */
public class DepthPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.9f;

    @Override
    public void transformPage(View view, float position) {
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0f);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1f);
            view.setTranslationY(0f);
            view.setTranslationZ(0f);
            view.setScaleX(1f);
            view.setScaleY(1f);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationY(pageHeight * -position);
            // Move it behind the left page
            view.setTranslationZ(-1f);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0f);
        }
    }
}



package com.yangdai.calc.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.elevation.SurfaceColors;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;
import com.yangdai.calc.R;
import com.yangdai.calc.features.FloatingWindow;
import com.yangdai.calc.main.sheets.BottomSheetFragment;
import com.yangdai.calc.main.toolbox.ToolBoxFragment;
import com.yangdai.calc.utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 30415
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Menu menu;
    private ViewPager2 viewPager;
    private int currentPosition = 0;
    private ImageView pageIcon;
    private SharedPreferences defaultSharedPrefs;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;
        if (currentPosition == 1) {
            menu.findItem(R.id.historys).setVisible(false);
            menu.findItem(R.id.view_layout).setVisible(true);
        } else {
            menu.findItem(R.id.historys).setVisible(true);
            menu.findItem(R.id.view_layout).setVisible(false);
        }
        boolean isGrid = defaultSharedPrefs.getBoolean("GridLayout", true);
        if (isGrid) {
            menu.findItem(R.id.view_layout).setIcon(getDrawable(R.drawable.grid_on));
        } else {
            menu.findItem(R.id.view_layout).setIcon(getDrawable(R.drawable.table_rows));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.historys) {
            ViewPager2 viewPager2;
            try {
                viewPager2 = findViewById(R.id.view_pager);
                if (viewPager2.getCurrentItem() == 0) {
                    viewPager2.setCurrentItem(1);
                } else {
                    viewPager2.setCurrentItem(0);
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        } else if (item.getItemId() == R.id.resize) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayDisplayPermission();
            } else {
                startService(new Intent(MainActivity.this, FloatingWindow.class));
                finish();
            }
        } else if (item.getItemId() == R.id.setting) {
            BottomSheetFragment.newInstance().show(getSupportFragmentManager(), "dialog");
        } else if (item.getItemId() == R.id.view_layout) {
            boolean isGrid = defaultSharedPrefs.getBoolean("GridLayout", true);
            SharedPreferences.Editor editor = defaultSharedPrefs.edit();
            editor.putBoolean("GridLayout", !isGrid);
            editor.apply();
            if (!isGrid) {
                item.setIcon(getDrawable(R.drawable.grid_on));
            } else {
                item.setIcon(getDrawable(R.drawable.table_rows));
            }
            Bundle result = new Bundle();
            result.putBoolean("GridLayout", !isGrid);
            getSupportFragmentManager().setFragmentResult("ChangeLayout", result);
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestOverlayDisplayPermission() {
        new MaterialAlertDialogBuilder(this)
                .setCancelable(true)
                .setTitle(getString(R.string.Screen_Overlay_Permission_Needed))
                .setMessage(getString(R.string.Permission_Dialog_Messege))
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> {
                    // 处理权限未授予的情况
                    Toast.makeText(MainActivity.this, getString(R.string.permission), Toast.LENGTH_SHORT).show();
                })
                .setPositiveButton(getString(R.string.Open_Settings), (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    overlayPermissionLauncher.launch(intent);
                }).show();
    }

    private final ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // 处理权限已授予的情况
                    startService(new Intent(MainActivity.this, FloatingWindow.class));
                    finish();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_0.getColor(this));
        setContentView(R.layout.activity_main);

        defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPrefs.registerOnSharedPreferenceChangeListener(this);

        if (defaultSharedPrefs.getBoolean("screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setupToolbar();
        setupViewPager();
        pageIcon.setOnClickListener(v -> {
            if (currentPosition == 0) {
                currentPosition = 1;
                viewPager.setCurrentItem(1, true);
            } else {
                currentPosition = 0;
                viewPager.setCurrentItem(0, true);
            }
        });
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(SurfaceColors.SURFACE_0.getColor(this)));
        getSupportActionBar().setElevation(0f);
        pageIcon = findViewById(R.id.view_pager_icon);
    }

    private void setupViewPager() {
        WormDotsIndicator dotsIndicator = findViewById(R.id.dotsIndicator);
        viewPager = findViewById(R.id.view_pager_main);
        if (viewPager != null) {
            reduceDragSensitivity();
            List<Fragment> fragments = new ArrayList<>();
            fragments.add(MainFragment.newInstance());
            fragments.add(ToolBoxFragment.newInstance());
            MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), getLifecycle(), fragments);
            viewPager.setAdapter(pagerAdapter);
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onPageSelected(int position) {
                    currentPosition = position;
                    if (currentPosition == 0) {
                        if (menu != null) {
                            menu.findItem(R.id.historys).setVisible(true);
                            menu.findItem(R.id.view_layout).setVisible(false);
                        }
                        pageIcon.setImageDrawable(getDrawable(R.drawable.calculate_icon));
                    } else {
                        if (menu != null) {
                            menu.findItem(R.id.historys).setVisible(false);
                            menu.findItem(R.id.view_layout).setVisible(true);
                        }
                        pageIcon.setImageDrawable(getDrawable(R.drawable.grid_view_more));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        if (currentPosition == 0) {
                            if (menu != null) {
                                menu.findItem(R.id.historys).setVisible(true);
                                menu.findItem(R.id.view_layout).setVisible(false);
                            }
                        } else {
                            if (menu != null) {
                                menu.findItem(R.id.historys).setVisible(false);
                                menu.findItem(R.id.view_layout).setVisible(true);
                            }
                        }
                    }
                }
            });
            dotsIndicator.attachTo(viewPager);
        }
    }

    private void reduceDragSensitivity() {
        try {
            Field ff = ViewPager2.class.getDeclaredField("mRecyclerView");
            ff.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) ff.get(viewPager);
            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            Integer touchSlop = (Integer) touchSlopField.get(recyclerView);
            if (touchSlop != null) {
                touchSlopField.set(recyclerView, touchSlop * 5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if (defaultSharedPrefs.getBoolean("vib", false)) {
            Utils.vibrate(this);
        }
        if ("split".equals(key)) {
            Toast.makeText(this, getString(R.string.restart), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        defaultSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}

package com.yangdai.calc.main;

import com.yangdai.calc.main.toolbox.ToolBoxItem;

/**
 * @author 30415
 */
public interface ItemClick {
    void onClick(ToolBoxItem item);
}


package com.yangdai.calc.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

/**
 * @author 30415
 */
public class MyPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments;

    public MyPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<Fragment> fragments) {
        super(fragmentManager, lifecycle);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

}


package com.yangdai.calc.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yangdai.calc.R;
import com.yangdai.calc.main.calculator.CalculatorFragment;
import com.yangdai.calc.main.calculator.HistoryListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 30415
 */
public class MainFragment extends Fragment {
    private MyPagerAdapter myPagerAdapter;

    public MainFragment() {
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(HistoryListFragment.newInstance());
        fragments.add(CalculatorFragment.newInstance());
        myPagerAdapter = new MyPagerAdapter(getChildFragmentManager(), getLifecycle(), fragments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewPager2 viewPager2 = view.findViewById(R.id.view_pager);
        viewPager2.setAdapter(myPagerAdapter);
        viewPager2.setPageTransformer(new DepthPageTransformer());
        viewPager2.setCurrentItem(1, false);
    }

}

package com.yangdai.calc.main.sheets;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.yangdai.calc.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements Preference.OnPreferenceClickListener {

    Preference themePref, languagePref, cleanPref;
    SharedPreferences defaultSharedPrefs;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        themePref = findPreference("theme");
        languagePref = findPreference("language");
        cleanPref = findPreference("clean");

        languagePref.setVisible(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU);
        languagePref.setOnPreferenceClickListener(this);
        themePref.setOnPreferenceClickListener(this);
        cleanPref.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        if ("theme".equals(preference.getKey())) {
            SharedPreferences.Editor editor = defaultSharedPrefs.edit();
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.theme)
                    .setCancelable(false)
                    .setSingleChoiceItems(
                            getResources().getStringArray(R.array.theme_options),
                            defaultSharedPrefs.getInt("themeSetting", 2),
                            (dialog, which) -> {
                                editor.putInt("themeSetting", which);
                                editor.apply();
                            })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        int theme = defaultSharedPrefs.getInt("themeSetting", 2);
                        switch (theme) {
                            case 0 ->
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            case 1 ->
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            case 2 ->
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            default -> {
                            }
                        }
                    })
                    .show();
        } else if ("language".equals(preference.getKey())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
                    intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                    startActivity(intent);
                } catch (Exception e) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.fromParts("package", requireContext().getPackageName(), null));
                        startActivity(intent);
                    } catch (Exception ignored) {
                    }
                }
            }
        } else if ("clean".equals(preference.getKey())) {
            SharedPreferences history = requireActivity().getSharedPreferences("history", MODE_PRIVATE);
            SharedPreferences.Editor editor = history.edit();
            editor.putString("newHistory", "");
            if (editor.commit()) {
                Toast.makeText(getContext(), getString(R.string.hasCleaned), Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }
}


package com.yangdai.calc.main.sheets;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.yangdai.calc.R;
import com.yangdai.calc.databinding.FragmentBottomSheetDialogBinding;

import java.util.Objects;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     BottomSheetFragment.newInstance().show(getSupportFragmentManager(), "dialog");
 * </pre>
 */
public class BottomSheetFragment extends BottomSheetDialogFragment {

    private FragmentBottomSheetDialogBinding binding;
    private final AboutFragment aboutFragment;

    public BottomSheetFragment() {
        aboutFragment = AboutFragment.newInstance();
    }

    public static BottomSheetFragment newInstance() {
        return new BottomSheetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentBottomSheetDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Replace the container view with the PreferenceFragmentCompat
        getChildFragmentManager().beginTransaction()
                .replace(binding.settings.getId(), SettingsFragment.newInstance())
                .commit();

        binding.closeButton.setOnClickListener(v -> BottomSheetFragment.this.dismiss());
        binding.chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.get(0) == R.id.chip1) {
                getChildFragmentManager().beginTransaction()
                        .replace(binding.settings.getId(), SettingsFragment.newInstance())
                        .commit();
            } else {
                getChildFragmentManager().beginTransaction()
                        .replace(binding.settings.getId(), aboutFragment)
                        .commit();
            }
        });

        Objects.requireNonNull(getDialog()).setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet == null) return;
            // 获取屏幕高度
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            // 计算设置的 peekHeight
            int peekHeight = screenHeight * 10 / 21;
            BottomSheetBehavior.from(bottomSheet).setPeekHeight(peekHeight);
            BottomSheetBehavior.from(bottomSheet).addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        binding.closeButton.setVisibility(View.VISIBLE);
                    } else {
                        binding.closeButton.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}

package com.yangdai.calc.main.sheets;

import static android.app.Activity.RESULT_OK;
import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.yangdai.calc.R;
import com.yangdai.calc.utils.PaymentUtil;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

/**
 * @author 30415
 */
public class AboutFragment extends Fragment {
    private AppUpdateManager appUpdateManager = null;
    private Task<AppUpdateInfo> appUpdateInfoTask = null;
    private final ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private final CustomTabsIntent webIntent = new CustomTabsIntent.Builder().setShowTitle(true).build();

    public AboutFragment() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    // handle callback
                    if (result.getResultCode() != RESULT_OK) {
                        Toast.makeText(requireContext(), getString(R.string.checkNet), Toast.LENGTH_SHORT).show();
                        Log.e("update", "Update flow failed! Result code: " + result.getResultCode());
                    }
                });
    }

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != appUpdateManager) {
            appUpdateManager
                    .getAppUpdateInfo()
                    .addOnSuccessListener(
                            appUpdateInfo -> {
                                if (appUpdateInfo.updateAvailability()
                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                    // If an in-app update is already running, resume the update.
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            activityResultLauncher,
                                            AppUpdateOptions.newBuilder(IMMEDIATE)
                                                    .setAllowAssetPackDeletion(true)
                                                    .build());
                                }
                            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        appUpdateManager = null;
        appUpdateInfoTask = null;
    }

    @SuppressLint("SetTextI18n")
    private void init(View view) {
        view.findViewById(R.id.about_rate).setOnClickListener(v -> webIntent.launchUrl(requireContext(), Uri.parse("https://play.google.com/store/apps/details?id=com.yangdai.calc")));
        view.findViewById(R.id.about_share).setOnClickListener(v -> {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.shareContent));
            startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)));
        });
        view.findViewById(R.id.about_donate).setOnClickListener(v -> {
            try {
                if (PaymentUtil.isInstalledPackage(requireContext())) {
                    PaymentUtil.startAlipayClient(requireActivity(), "fkx12941hqcc7gpulzphmee"); // 第二步获取到的字符串
                } else {
                    Intent donateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/YangDaiDevelpoer?country.x=DE&locale.x=de_DE"));
                    startActivity(donateIntent);
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Please install Paypal or Alipay.", Toast.LENGTH_SHORT).show();
            }

        });
        view.findViewById(R.id.about_github).setOnClickListener(v -> webIntent.launchUrl(requireContext(), Uri.parse("https://github.com/YangDai-Github/Multi-Calculator-Android")));
        view.findViewById(R.id.about_email).setOnClickListener(v -> {
            Uri uri = Uri.parse("mailto:dy15800837435@gmail.com");
            Intent email = new Intent(Intent.ACTION_SENDTO, uri);
            email.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            startActivity(Intent.createChooser(email, "Feedback (E-mail)"));
        });
        view.findViewById(R.id.about_privacy_policy).setOnClickListener(v -> webIntent.launchUrl(requireContext(), Uri.parse("https://github.com/YangDai2003/Multi-Calculator-Android/blob/master/PRIVACY_POLICY.md")));
        TextView textView = view.findViewById(R.id.about_app_version);
        textView.setOnLongClickListener(v -> {
            Toast.makeText(requireContext(), getString(R.string.thank), Toast.LENGTH_LONG).show();
            return true;
        });
        view.findViewById(R.id.about_app_osl).setOnClickListener(v -> {
            OssLicensesMenuActivity.setActivityTitle(getString(R.string.app_osl));
            startActivity(new Intent(requireContext(), OssLicensesMenuActivity.class));
        });
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                textView.setText(getString(R.string.app_version) + " "
                        + requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), PackageManager.PackageInfoFlags.of(0)).versionName);
            } else {
                textView.setText(getString(R.string.app_version) + " "
                        + requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            textView.setText(getString(R.string.app_version) + " ");
        }
        view.findViewById(R.id.about_app_more).setOnClickListener(v -> webIntent.launchUrl(requireContext(), Uri.parse("https://play.google.com/store/apps/dev?id=7281798021912275557")));
        view.findViewById(R.id.about_app_update).setOnClickListener(view1 -> {
            appUpdateManager = AppUpdateManagerFactory.create(requireContext());
            appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // an activity result launcher registered via registerForActivityResult
                            activityResultLauncher,
                            // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                            // flexible updates.
                            AppUpdateOptions.newBuilder(IMMEDIATE)
                                    .setAllowAssetPackDeletion(true)
                                    .build());
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                    Toast.makeText(requireContext(), getString(R.string.newest), Toast.LENGTH_SHORT).show();
                } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UNKNOWN) {
                    Toast.makeText(requireContext(), getString(R.string.checkNet), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}

package com.yangdai.calc.main.calculator;

import static android.content.Context.MODE_PRIVATE;

import static com.yangdai.calc.main.calculator.CalculatorUtils.highlightSpecialSymbols;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yangdai.calc.R;
import com.yangdai.calc.utils.TTS;
import com.yangdai.calc.utils.TTSInitializationListener;
import com.yangdai.calc.utils.TouchAnimation;

/**
 * @author 30415
 */
public class CalculatorFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener
        , View.OnClickListener, TTSInitializationListener {
    private TextView inputView;
    private TextView outputView;
    SharedPreferences defaultSp, historySp;
    private boolean switched = false;
    private TTS tts;
    private boolean ttsAvailable;
    private ColorStateList color;
    private boolean fromUser;
    private static final int[] BUTTON_IDS = {R.id.div, R.id.mul, R.id.sub, R.id.add, R.id.seven,
            R.id.eight, R.id.nine, R.id.brackets, R.id.four, R.id.five, R.id.six, R.id.inverse, R.id.delete,
            R.id.e, R.id.pi, R.id.factorial, R.id.time, R.id.SHOW_ALL, R.id.percentage, R.id.g,
            R.id.switchViews, R.id.sin, R.id.cos, R.id.tan, R.id.cot,
            R.id.three, R.id.two, R.id.one, R.id.dot, R.id.zero, R.id.equal, R.id.Clean};
    private CalculatorViewModel viewModel;

    public CalculatorFragment() {
    }

    public static CalculatorFragment newInstance() {
        return new CalculatorFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CalculatorViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        defaultSp = PreferenceManager.getDefaultSharedPreferences(requireContext());
        defaultSp.registerOnSharedPreferenceChangeListener(this);
        historySp = requireActivity().getSharedPreferences("history", MODE_PRIVATE);

        // 初始化TextToSpeech对象
        tts = new TTS();
        ttsAvailable = tts.ttsCreate(requireActivity(), this);

        inputView = view.findViewById(R.id.edit);
        outputView = view.findViewById(R.id.view);
        color = outputView.getTextColors();
        outputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals(getString(R.string.formatError)) || editable.toString().equals(getString(R.string.bigNum))) {
                    outputView.setTextColor(requireActivity().getColor(R.color.wrong));
                } else {
                    outputView.setTextColor(color);
                }
            }
        });

        for (int buttonId : BUTTON_IDS) {
            View view1 = view.findViewById(buttonId);
            if (null != view1) {
                view1.setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
                view1.setOnClickListener(this);
                TouchAnimation touchAnimation = new TouchAnimation(view1);
                view1.setOnTouchListener(touchAnimation);
            }
        }

        updateSpeaker();

        // 处理历史记录点击结果
        getParentFragmentManager().setFragmentResultListener("requestKey", getViewLifecycleOwner(), (requestKey, bundle) -> {
            if (null != bundle.getString("select")) {
                String selected = bundle.getString("select");
                // 负数加括号
                if (selected != null && selected.contains("-")) {
                    selected = "(" + selected + ")";
                }
                String inputtedEquation = inputView.getText().toString();
                if (inputtedEquation.isEmpty()) {
                    // 输入框为空，直接显示点击的历史记录
                    inputView.setText(selected);
                } else {
                    char last = inputtedEquation.charAt(inputtedEquation.length() - 1);
                    if (last == '+' || last == '-' || last == '(' || last == '×' || last == '÷' || last == '^') {
                        inputView.setText(inputtedEquation + selected);
                    } else {
                        inputView.setText(inputtedEquation + "+" + selected);
                    }
                    boolean useDeg = defaultSp.getBoolean("mode", false);
                    Calculator formulaUtil1 = new Calculator(useDeg);
                    fromUser = false;
                    viewModel.handleEqualButton(inputView.getText().toString(), formulaUtil1, defaultSp, historySp, fromUser, getString(R.string.bigNum), getString(R.string.formatError));
                }
                highlightSpecialSymbols(inputView);
            }
        });

        viewModel.getInputTextState().observe(getViewLifecycleOwner(), input -> inputView.setText(input));
        viewModel.getOutputTextState().observe(getViewLifecycleOwner(), output -> outputView.setText(output));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        if ("vib".equals(s)) {
            for (int buttonId : BUTTON_IDS) {
                View view = requireView().findViewById(buttonId);
                if (view != null) {
                    view.setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
                }
            }
        } else if ("scale".equals(s) || "mode".equals(s)) {
            String inputStr1 = inputView.getText().toString();
            //自动运算
            boolean useDeg = defaultSp.getBoolean("mode", false);
            if (!inputStr1.isEmpty()) {
                Calculator formulaUtil1 = new Calculator(useDeg);
                fromUser = false;
                viewModel.handleEqualButton(inputStr1, formulaUtil1, defaultSp, historySp, false, getString(R.string.bigNum), getString(R.string.formatError));
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        tts.ttsDestroy();
        // 在语言变化时重新初始化TextToSpeech对象
        ttsAvailable = tts.ttsCreate(requireActivity(), this);
        updateSpeaker();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 释放TextToSpeech资源
        tts.ttsDestroy();
        defaultSp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onClick(View v) {

        boolean useDeg = defaultSp.getBoolean("mode", false);
        boolean canSpeak = defaultSp.getBoolean("voice", false);

        //获取输入
        String inputStr = inputView.getText().toString();
        Calculator formulaUtil = new Calculator(useDeg);

        try {
            if (v.getId() == R.id.equal && !inputStr.isEmpty()) {
                if (canSpeak) {
                    tts.ttsSpeak(getString(R.string.equal));
                }
                fromUser = true;
                viewModel.handleEqualButton(inputStr, formulaUtil, defaultSp, historySp, true, getString(R.string.bigNum), getString(R.string.formatError));
            } else if (v.getId() == R.id.Clean) {
                if (canSpeak) {
                    tts.ttsSpeak(getString(R.string.resetInput));
                }
                viewModel.handleCleanButton();
            } else if (v.getId() == R.id.factorial) {
                String fac = getString(R.string.factorial);
                String doubleFac = getString(R.string.double_factorial);
                viewModel.handleFactorial(inputStr, canSpeak, tts, fac, doubleFac);
            } else if (v.getId() == R.id.delete) {
                viewModel.handleDeleteButton(inputStr);
            } else if (v.getId() == R.id.brackets) {
                if (canSpeak) {
                    tts.ttsSpeak(getString(R.string.bracket));
                }
                viewModel.handleBracketsButton(inputStr);
            } else if (v.getId() == R.id.inverse) {
                if (canSpeak) {
                    tts.ttsSpeak(getString(R.string.inverse));
                }
                viewModel.handleInverseButton(inputStr);
            } else if (v.getId() == R.id.switchViews) {
                View view = requireView();
                if (!switched) {
                    ((Button) view.findViewById(R.id.sin)).setText("sin⁻¹");
                    ((Button) view.findViewById(R.id.cos)).setText("cos⁻¹");
                    ((Button) view.findViewById(R.id.tan)).setText("tan⁻¹");
                    ((Button) view.findViewById(R.id.cot)).setText("cot⁻¹");
                    ((Button) view.findViewById(R.id.g)).setText("ln");
                    ((Button) view.findViewById(R.id.e)).setText("exp");
                } else {
                    ((Button) view.findViewById(R.id.sin)).setText("sin");
                    ((Button) view.findViewById(R.id.cos)).setText("cos");
                    ((Button) view.findViewById(R.id.tan)).setText("tan");
                    ((Button) view.findViewById(R.id.cot)).setText("cot");
                    ((Button) view.findViewById(R.id.g)).setText("log");
                    ((Button) view.findViewById(R.id.e)).setText("e");
                }
                switched = !switched;
            } else {
                viewModel.handleOtherButtons(v, inputStr, canSpeak, tts, fromUser);
            }
            String inputStr1 = inputView.getText().toString();
            highlightSpecialSymbols(inputView);
            if (v.getId() != R.id.equal) {
                //自动运算
                if (!inputStr1.isEmpty()) {
                    Calculator formulaUtil1 = new Calculator(useDeg);
                    fromUser = false;
                    viewModel.handleEqualButton(inputStr1, formulaUtil1, defaultSp, historySp, false, getString(R.string.bigNum), getString(R.string.formatError));
                }
            }
        } catch (Exception e) {
            outputView.setText("");
        }
    }

    @Override
    public void onTTSInitialized(boolean isSuccess) {
        ttsAvailable = isSuccess;
        updateSpeaker();
    }

    private void updateSpeaker() {
        try {
            ImageButton readoutButton = requireView().findViewById(R.id.speak);
            if (!ttsAvailable) {
                readoutButton.setVisibility(View.INVISIBLE);
            } else {
                readoutButton.setVisibility(View.VISIBLE);
                readoutButton.bringToFront();
                readoutButton.setOnClickListener(v -> {
                    if (!inputView.getText().toString().isEmpty() && !outputView.getText().toString().isEmpty()) {
                        String text = inputView.getText().toString() + "= " + outputView.getText().toString();
                        tts.ttsSpeak(text);
                    }
                });
            }
        } catch (Exception e) {
            Log.e("updateSpeaker", e.toString());
        }
    }
}


package com.yangdai.calc.main.calculator;

/**
 * @author 30415
 */
public interface HistoryItemClick {
    void onClick(String str);
}


package com.yangdai.calc.main.calculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yangdai.calc.R;
import com.yangdai.calc.utils.Utils;

import java.util.List;

public class HistoryAdapter extends ArrayAdapter<String> {
    private final HistoryItemClick itemClick;

    public HistoryAdapter(@NonNull Context context, @NonNull List<String> objects, HistoryItemClick itemClick) {
        super(context, 0, objects);
        this.itemClick = itemClick;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        String item = getItem(position);
        if (item != null) {
            TextView input = convertView.findViewById(R.id.input_tv);
            TextView output = convertView.findViewById(R.id.output_tv);

            String[] parts = item.split("=");
            if (parts.length == 2) {
                String leftSide = parts[0].trim();
                String rightSide = parts[1].trim();

                input.setText(leftSide);
                CalculatorUtils.highlightSpecialSymbols(input);
                output.setText(Utils.formatNumber(rightSide));

                View.OnClickListener onClickListener = v -> itemClick.onClick(rightSide);

                convertView.setOnClickListener(onClickListener);
                input.setOnClickListener(v -> itemClick.onClick(leftSide));
                output.setOnClickListener(onClickListener);
            }

        }

        return convertView;
    }
}


package com.yangdai.calc.main.calculator;

import static com.yangdai.calc.main.calculator.CalculatorUtils.calculateAllFactorial;
import static com.yangdai.calc.main.calculator.CalculatorUtils.optimizePercentage;
import static com.yangdai.calc.utils.Utils.formatNumber;
import static com.yangdai.calc.utils.Utils.isNumber;
import static com.yangdai.calc.utils.Utils.isNumeric;
import static com.yangdai.calc.utils.Utils.isSymbol;
import static com.yangdai.calc.utils.Utils.removeZeros;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.icu.math.BigDecimal;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.material.button.MaterialButton;
import com.yangdai.calc.utils.TTS;
import com.yangdai.calc.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 30415
 */
public class CalculatorViewModel extends ViewModel {

    private final MutableLiveData<String> expression = new MutableLiveData<>();
    private final MutableLiveData<String> result = new MutableLiveData<>();
    private int left = 0, right = 0;

    public LiveData<String> getInputTextState() {
        return expression;
    }

    public LiveData<String> getOutputTextState() {
        return result;
    }

    @SuppressLint("SetTextI18n")
    public void handleFactorial(String inputStr, boolean canSpeak, TTS tts, String fac, String doubleFac) {
        if (!inputStr.isEmpty()) {
            char lastChar = inputStr.charAt(inputStr.length() - 1);
            if (isNumber(String.valueOf(lastChar)) && lastChar != 'e' && lastChar != 'g' && lastChar != 'π') {
                for (int i = inputStr.length() - 1; i >= 0; i--) {
                    if (inputStr.charAt(i) == '.') {
                        return;
                    }
                    if (isSymbol(String.valueOf(inputStr.charAt(i)))) {
                        expression.setValue(inputStr + "!");
                        if (canSpeak) {
                            tts.ttsSpeak(fac);
                        }
                        return;
                    }
                }
                if (canSpeak) {
                    tts.ttsSpeak(fac);
                }
                expression.setValue(inputStr + "!");
            } else if (lastChar == '!') {
                char secondLastChar = inputStr.charAt(inputStr.length() - 2);
                if (secondLastChar != '!') {
                    expression.setValue(inputStr + "!");
                    if (canSpeak) {
                        tts.ttsSpeak(doubleFac);
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void handleEqualButton(String inputStr, Calculator formulaUtil, SharedPreferences defaultSp, SharedPreferences history, boolean fromUser, String bigNum, String error) {
        // 处理常量情况
        if ("e".equals(inputStr) || "π".equals(inputStr)) {
            inputStr = inputStr.replace("e", String.valueOf(Math.E))
                    .replace("π", String.valueOf(Math.PI));
            result.setValue(inputStr);
            return;
        }

        // 忽略特殊情况
        if (inputStr.isEmpty() || isNumeric(inputStr)) {
            result.setValue("");
            return;
        }

        // 补全
        if (isSymbol(String.valueOf(inputStr.charAt(inputStr.length() - 1)))) {
            inputStr += "0";
        } else if (isSymbol(String.valueOf(inputStr.charAt(0))) || inputStr.charAt(0) == '%') {
            inputStr = "0" + inputStr;
        }

        // 处理左右括号数量不一致的情况
        if (left != right) {
            int addCount = Math.abs(left - right);
            StringBuilder inputStrBuilder = new StringBuilder(inputStr);
            for (int j = 0; j < addCount; j++) {
                inputStrBuilder.append(")");
            }
            inputStr = inputStrBuilder.toString();
        }

        inputStr = optimizePercentage(inputStr);

        try {
            if (inputStr.contains("!")) {
                // 优化！阶乘和！！双阶乘
                inputStr = calculateAllFactorial(inputStr);
                if ("数值过大".equals(inputStr)) {
                    result.setValue(bigNum);
                    return;
                }
            }

            // 使用正则表达式进行匹配
            String patternStr = "\\be\\b";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(inputStr);
            // 替换匹配到的单独 "e"
            inputStr = matcher.replaceAll(String.valueOf(Math.E));
            //替换常数
            inputStr = inputStr.replace("π", String.valueOf(Math.PI))
                    .replace("%", "÷100");
            BigDecimal bigDecimal = formulaUtil.calc(inputStr);
            if (null == bigDecimal) {
                result.setValue(bigNum);
                return;
            }
            bigDecimal = bigDecimal.setScale(defaultSp.getInt("scale", 10), BigDecimal.ROUND_HALF_UP);
            String res = bigDecimal.toBigDecimal().toPlainString();
            res = removeZeros(res);

            if (fromUser) {
                String historys = history.getString("newHistory", "");
                List<String> savedStringList = new ArrayList<>(Arrays.asList(historys.split("//")));

                if (savedStringList.size() >= defaultSp.getInt("historyNum", 100)) {
                    int removeCount = savedStringList.size() - defaultSp.getInt("historyNum", 100) + 1;
                    savedStringList.removeAll(savedStringList.subList(0, removeCount));
                }
                savedStringList.add(inputStr + "\n" + "=" + res);
                String listString = TextUtils.join("//", savedStringList);
                SharedPreferences.Editor editor = history.edit();
                editor.putString("newHistory", listString);
                editor.apply();
                expression.setValue(res);
                result.setValue("");
                left = 0;
                right = 0;
            } else {
                result.setValue(formatNumber(res));
            }
        } catch (Exception e) {
            if (fromUser) {
                result.setValue(error);
            }
        }
    }

    public void handleCleanButton() {
        expression.setValue("");
        result.setValue("");
        left = 0;
        right = 0;
    }

    public void handleDeleteButton(String inputStr) {
        if (!inputStr.isEmpty()) {
            if (inputStr.endsWith("sin⁻¹(") || inputStr.endsWith("cos⁻¹(")
                    || inputStr.endsWith("tan⁻¹(") || inputStr.endsWith("cot⁻¹(")) {
                inputStr = inputStr.substring(0, inputStr.length() - 6);
                left--;
            } else if (inputStr.endsWith("sin(") || inputStr.endsWith("cos(") || inputStr.endsWith("exp(")
                    || inputStr.endsWith("tan(") || inputStr.endsWith("cot(") || inputStr.endsWith("log(")) {
                inputStr = inputStr.substring(0, inputStr.length() - 4);
                left--;
            } else if (inputStr.endsWith("ln(")) {
                inputStr = inputStr.substring(0, inputStr.length() - 3);
                left--;
            } else {
                char lastChar = inputStr.charAt(inputStr.length() - 1);
                if (lastChar == ')') {
                    right--;
                }
                if (lastChar == '(') {
                    left--;
                }
                inputStr = inputStr.substring(0, inputStr.length() - 1);
            }
            expression.setValue(inputStr);
        }
        if (inputStr.isEmpty()) {
            result.setValue("");
        }
    }

    @SuppressLint("SetTextI18n")
    public void handleBracketsButton(String inputStr) {
        if (!inputStr.isEmpty()) {
            char lastChar = inputStr.charAt(inputStr.length() - 1);
            if (left > right && (isNumber(String.valueOf(lastChar))
                    || lastChar == '!' || lastChar == '%' || lastChar == ')')) {
                expression.setValue(inputStr + ")");
                right++;
                return;
            } else if (lastChar == ')' || isNumber(String.valueOf(lastChar))) {
                expression.setValue(inputStr + "×(");
            } else {
                expression.setValue(inputStr + "(");
            }
        } else {
            expression.setValue(inputStr + "(");
        }
        left++;
    }

    @SuppressLint("SetTextI18n")
    public void handleInverseButton(String inputStr) {
        // 取反
        if (!inputStr.isEmpty()) {
            char lastChar = inputStr.charAt(inputStr.length() - 1);
            //最后一位是数字
            if (isNumber(String.valueOf(lastChar))) {
                StringBuilder n = new StringBuilder();
                n.insert(0, lastChar);
                // 如果长度大于一， 从后向前遍历直到数字前一位
                if (inputStr.length() > 1) {
                    for (int i = inputStr.length() - 2; i >= 0; i--) {
                        char curr = inputStr.charAt(i);
                        if (isNumber(String.valueOf(curr)) || curr == '.') {
                            n.insert(0, curr);
                        } else {
                            // 遇到负号，如果负号前是 (-，则去掉 (-
                            if (curr == '-') {
                                if (i >= 1 && "(-".equals(inputStr.substring(i - 1, i + 1))) {
                                    inputStr = inputStr.substring(0, i - 1);
                                    expression.setValue(inputStr + n);
                                    left--;
                                    return;
                                }
                            }  // + × ÷ (  ^ 特殊情况 )
                            inputStr = inputStr.substring(0, i + 1);
                            String prefix = (curr == ')') ? "×(-" : "(-";
                            expression.setValue(inputStr + prefix + n);
                            left++;
                            return;
                        }
                    }
                }
                //只有数字
                expression.setValue("(-" + n);
                left++;
                return;
            } else if (lastChar == '-') {
                // 最后是 (-， 直接去掉
                if (inputStr.length() > 1 && (inputStr.charAt(inputStr.length() - 2) == '(')) {
                    expression.setValue(inputStr.substring(0, inputStr.length() - 2));
                    left--;
                    return;
                }
            }
            String prefix = (lastChar == ')' || lastChar == '!') ? "×(-" : "(-";
            expression.setValue(inputStr + prefix);
        } else {
            expression.setValue("(-");
        }
        left++;
    }

    @SuppressLint("SetTextI18n")
    public void handleOtherButtons(View v, String inputStr, boolean canSpeak, TTS tts, boolean fromUser) {
        String append = ((MaterialButton) v).getText().toString();

        if (canSpeak) {
            tts.ttsSpeak(append);
        }

        if (fromUser && Utils.isNumber(append)) {
            // 点击等号后，再次输入数字时清空结果
            expression.setValue(append);
        } else {
            //长度大于0时
            if (!inputStr.isEmpty()) {
                char lastInput = inputStr.charAt(inputStr.length() - 1);
                // )、e、π、！、% 后输入数字默认加上 ×
                if (isNumber(append)) {
                    if (")".equals(String.valueOf(lastInput)) || "!".equals(String.valueOf(lastInput)) || "%".equals(String.valueOf(lastInput))
                            || "e".equals(String.valueOf(lastInput)) || "π".equals(String.valueOf(lastInput))) {
                        expression.setValue(inputStr + "×" + append);
                        return;
                    }
                }
                // 最后一位是两数运算符号时，再次输入符号则替换最后一位
                if (isSymbol(String.valueOf(lastInput)) && isSymbol(append)) {
                    expression.setValue(inputStr.substring(0, inputStr.length() - 1) + append);
                    return;
                }
                // 最后一位是数字时，输入e、π默认加上 ×
                if (isNumber(String.valueOf(lastInput)) && ("e".equals(append) || "π".equals(append))) {
                    expression.setValue(inputStr + "×" + append);
                    return;
                }
            }

            //三角函数运算符和对数运算符后自动加上括号
            if ("sin".equals(append) || "cos".equals(append) || "tan".equals(append) || "cot".equals(append)
                    || "sin⁻¹".equals(append) || "cos⁻¹".equals(append) || "tan⁻¹".equals(append) || "cot⁻¹".equals(append)
                    || "log".equals(append) || "ln".equals(append) || "exp".equals(append)) {
                if (!inputStr.isEmpty()) {
                    char lastInput = inputStr.charAt(inputStr.length() - 1);
                    if (isNumber(String.valueOf(lastInput)) || ")".equals(String.valueOf(lastInput))
                            || "!".equals(String.valueOf(lastInput)) || "%".equals(String.valueOf(lastInput))) {
                        expression.setValue(inputStr + "×" + append + "(");
                        left++;
                        return;
                    }
                }
                expression.setValue(inputStr + append + "(");
                left++;
                return;
            }
            expression.setValue(inputStr + append);
        }
    }
}


package com.yangdai.calc.main.calculator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;


/**
 * @author 30415
 */
public class MyScrollListView extends ListView {
    private float preY = 0, preX = 0;

    public MyScrollListView(Context context) {
        super(context);
    }

    public MyScrollListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN -> {
                preY = ev.getY();
                preX = ev.getX();
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            case MotionEvent.ACTION_MOVE ->
            {
                float currentY = ev.getY();
                float deltaY = currentY - preY;
                float deltaX = ev.getX() - preX;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                } else {
                    getParent().requestDisallowInterceptTouchEvent(!slideToTheBottom(ev));
                }
            }
            case MotionEvent.ACTION_UP -> getParent().requestDisallowInterceptTouchEvent(false);
            default -> {}
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 最后一个可见item为全部item的最后一个，且手势向上滑动，且全部露出
     */
    private boolean slideToTheBottom(MotionEvent ev) {
        if (getCount() == 0) {
            return true;
        }
        return getLastVisiblePosition() == getCount() - 1
//                && getChildAt(getChildCount() - 1).getBottom() == getHeight()
                && ev.getY() - preY < 0;
    }
}

package com.yangdai.calc.main.calculator;

import android.icu.math.BigDecimal;

import com.yangdai.calc.utils.Utils;

import java.util.*;

/**
 * @author 30415
 */
public class Calculator {
    /**
     * 四个列表
     * 储存运算符（代号）, 包括三角函数指数函数等
     * 储存运算符的优先等级
     * 专门储存特殊运算符（代号）
     * 共有 + - × ÷ ln log sin cos tan cot sin⁻¹ cos⁻¹ tan⁻¹ cot⁻¹ ^ () exp
     */
    private static final List<String> CALC_LIST = Arrays.asList("l", "g", "i", "a", "n", "v", "s", "c", "t", "e", "o", "^", "×", "÷", "+", "-", "(", ")");
    private static final List<Integer> ORDER_LIST = Arrays.asList(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 3, 3, 2, 2, 1, 1);
    private static final List<String> SPECIAL_LIST = Arrays.asList("l", "g", "i", "a", "n", "v", "s", "c", "t", "e", "o");
    private static final List<String> OB_SPECIAL = Arrays.asList("ln", "log", "sin⁻¹", "cos⁻¹", "tan⁻¹", "cot⁻¹", "sin", "cos", "tan", "exp", "cot");

    private final boolean useRad;

    public Calculator(boolean rad) {
        useRad = rad;
    }

    public BigDecimal calc(String str) {
        String res = calculate(change(str), useRad);
        if (null == res) {
            return null;
        }
        return new BigDecimal(res);
    }

    /**
     * 储存特殊运算符的全名
     * 转化为后缀表达式
     * 创建change函数把中缀表达式转化为后缀表达式
     */
    public List<String> change(String func) {
        for (int i = 0; i < OB_SPECIAL.size(); i++) {
            //将特殊函数用单个字符替换
            func = func.replace(OB_SPECIAL.get(i), SPECIAL_LIST.get(i));
        }
        //栈S2
        List<String> numList = new ArrayList<>();
        //栈S1
        List<String> obList = new ArrayList<>();
        func = func.replace(" ", "");
        //去掉空格
        String temp;
        //遍历到的符号
        int i = 0;
        //遍历的位置
        while (i < func.length()) {
            temp = func.substring(i, i + 1);
            //遍历的符号
            //由于计算机是一位一位遍历, 下面的操作是为了提取出两位及以上的数
            StringBuilder num = new StringBuilder();
            //先表示为空字符串
            while (!CALC_LIST.contains(temp)) {
                //判断是否为数字
                num.append(temp);
                i = i + 1;
                try {
                    temp = func.substring(i, i + 1);
                } catch (Exception ex) {
                    break;
                }
            }
            if (!num.toString().isEmpty()) {
                numList.add(num.toString());
                //添加数字入栈S2
            }
            //若是运算符
            //先要处理负数, 在-前加上0, 否则有些情况会报错
            if (i == 0 && "-".equals(temp)) {
                numList.add("0");
            } else if (i != 0 && "-".equals(temp)) {
                if (func.charAt(i - 1) == '(') {
                    numList.add("0");
                }
            }
            if (CALC_LIST.contains(temp)) {
                if (obList.isEmpty()) {
                    //如果是S1空着, 直接入栈
                    obList.add(temp);
                } else {
                    if ("(".equals(temp)) {
                        //左括号直接入栈
                        obList.add(temp);
                    } else if (")".equals(temp)) {
                        //右括号, 则从栈顶开始依次弹出, 直到遇到左括号
                        String last = obList.get(obList.size() - 1);
                        while (!"(".equals(last)) {
                            numList.add(obList.get(obList.size() - 1));
                            obList.remove(obList.size() - 1);
                            last = obList.get(obList.size() - 1);
                        }
                        obList.remove(obList.size() - 1);
                        //如果左括号前是特殊函数, 直接压入S2
                        try {
                            if (SPECIAL_LIST.contains(obList.get(obList.size() - 1))) {
                                numList.add(obList.get(obList.size() - 1));
                                obList.remove(obList.size() - 1);
                            }
                        } catch (Exception ignored) {
                        }
                    } else {
                        //如果是运算符
                        //比较两个运算符的优先级
                        int order1 = ORDER_LIST.get(CALC_LIST.indexOf(obList.get(obList.size() - 1)));
                        int order2 = ORDER_LIST.get(CALC_LIST.indexOf(temp));
                        if (order2 > order1) {
                            obList.add(temp);
                            //优先级高, 直接压入S1
                        } else {
                            //否则将栈顶元素弹出至S2中
                            while (order2 <= order1) {
                                numList.add(obList.get(obList.size() - 1));
                                obList.remove(obList.size() - 1);
                                if (obList.isEmpty()) {
                                    break;
                                }
                                order1 = ORDER_LIST.get(CALC_LIST.indexOf(obList.get(obList.size() - 1)));
                            }
                            obList.add(temp);
                        }
                    }
                }
                i += 1;
            }
        }//遍历结束后, 把S1中剩余的一次弹入S2
        while (!obList.isEmpty()) {
            numList.add(obList.get(obList.size() - 1));
            obList.remove(obList.size() - 1);
        }
        return numList;
    }

    /**
     * 运算符的定义
     */
    public BigDecimal division(BigDecimal i1, BigDecimal i2) {
        return i1.divide(i2, 15, BigDecimal.ROUND_HALF_UP);
    }

    public double cot(double i2) {
        return (1 / Math.tan(i2));
    }

    public double acot(double i2) {
        return (Math.PI / 2 - Math.atan(i2));
    }

    /**
     * 计算后缀表达式
     */
    public String calculate(List<String> list, boolean useDeg) {
        Deque<String> numStack = new LinkedList<>();

        for (String s : list) {
            if (CALC_LIST.contains(s)) {
                BigDecimal result;
                if (SPECIAL_LIST.contains(s)) {
                    result = BigDecimal.valueOf(calculateSpecialFunction(numStack.pop(), s, useDeg));
                } else {
                    result = calculateOperator(numStack.pop(), numStack.pop(), s);
                    if (null == result) {
                        return null;
                    }
                }
                numStack.push(result.toBigDecimal().toPlainString());
            } else {
                numStack.push(s);
            }
        }

        return numStack.pop();
    }

    private double calculateSpecialFunction(String number, String function, boolean useRad) {
        double num = Double.parseDouble(number);
        double numRad = num;

        if (useRad) {
            numRad = Math.toRadians(num);
        }

        return switch (function) {
            case "s" -> Math.sin(numRad);
            case "c" -> Math.cos(numRad);
            case "t" -> Math.tan(numRad);
            case "o" -> cot(numRad);
            case "i" -> Math.asin(numRad);
            case "a" -> Math.acos(numRad);
            case "n" -> Math.atan(numRad);
            case "v" -> acot(numRad);
            case "e" -> Math.exp(num);
            case "l" -> Math.log(num);
            case "g" -> Math.log10(num);
            default -> 0;
        };
    }

    private BigDecimal calculateOperator(String number1, String number2, String operator) {
        BigDecimal num2 = new BigDecimal(number1);
        BigDecimal num1 = new BigDecimal(number2);

        switch (operator) {
            case "+" -> {
                return num1.add(num2);
            }
            case "-" -> {
                return num1.subtract(num2);
            }
            case "×" -> {
                return num1.multiply(num2);
            }
            case "÷" -> {
                return division(num1, num2);
            }
            case "^" -> {
                if (num2.compareTo(BigDecimal.ZERO) == 0) {
                    return BigDecimal.ONE;
                } else if (num2.compareTo(BigDecimal.ONE) == 0) {
                    return num1;
                } else if (num2.compareTo(BigDecimal.ONE) < 0) {
                    return BigDecimal.valueOf(Math.pow(num1.doubleValue(), num2.doubleValue()));
                } else {
                    if (num2.compareTo(BigDecimal.valueOf(1000)) < 0) {
                        if (new BigDecimal(Utils.removeZeros(num2.toBigDecimal().toPlainString())).scale() <= 0) {
                            return num1.pow(num2);
                        } else {
                            return BigDecimal.valueOf(Math.pow(num1.doubleValue(), num2.doubleValue()));
                        }
                    } else {
                        return null;
                    }
                }
            }
            default -> {
                return BigDecimal.ZERO;
            }
        }
    }
}


package com.yangdai.calc.main.calculator;

import static com.yangdai.calc.utils.Utils.isNumber;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.math.BigInteger;

public class CalculatorUtils {
    /**
     * 普通阶乘
     */
    public static BigInteger calculateFactorial(BigInteger num) {
        if (num.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ONE;
        }

        BigInteger factorial = BigInteger.ONE;
        for (BigInteger i = BigInteger.ONE; i.compareTo(num) <= 0; i = i.add(BigInteger.ONE)) {
            factorial = factorial.multiply(i);
        }
        return factorial;
    }

    /**
     * 双阶乘
     */
    public static BigInteger calculateDoubleFactorial(BigInteger num) {
        if (num.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ONE;
        }

        BigInteger result = BigInteger.ONE;
        int n = num.intValue();
        while (n > 0) {
            result = result.multiply(BigInteger.valueOf(n));
            n -= 2;
        }
        return result;
    }

    /**
     * 通用阶乘计算方法
     */
    public static String calculateAllFactorial(String str) {
        StringBuilder sb = new StringBuilder(str);
        int index = sb.indexOf("!");
        while (index >= 0) {
            int start = index - 1;
            while (start >= 0 && Character.isDigit(sb.charAt(start))) {
                start--;
            }
            start++;
            String num = sb.substring(start, index);
            BigInteger result;

            if (num.length() >= 4) {
                return "数值过大";
            }

            if (index + 1 < sb.length() && sb.charAt(index + 1) == '!') {
                // 连续出现两次的阶乘
                result = calculateDoubleFactorial(new BigInteger(num));
                sb.replace(start, index + 2, result.toString());
            } else {
                // 连续出现一次的阶乘
                result = calculateFactorial(new BigInteger(num));
                sb.replace(start, index + 1, result.toString());
            }
            index = sb.indexOf("!", start + result.toString().length());
        }
        return sb.toString();
    }

    public static String optimizePercentage(String inputStr) {
        if (inputStr.contains("%")) {
            // 优化百分号(%)
            StringBuilder optimizedInputStrBuilder = new StringBuilder(inputStr);
            for (int k = optimizedInputStrBuilder.length() - 1; k >= 0; k--) {
                char currentChar = optimizedInputStrBuilder.charAt(k);
                if (currentChar == '%') {
                    int startIndex = k - 1;
                    if (optimizedInputStrBuilder.charAt(startIndex) == ')') {
                        while (startIndex >= 0) {
                            char prevChar = optimizedInputStrBuilder.charAt(startIndex);
                            if (prevChar == '(') {
                                optimizedInputStrBuilder.insert(startIndex + 1, '(');
                                break;
                            }
                            startIndex--;
                        }
                    } else {
                        boolean atBeginning = true;
                        while (startIndex >= 0) {
                            char prevChar = optimizedInputStrBuilder.charAt(startIndex);
                            if (!(isNumber(String.valueOf(prevChar)) || prevChar == '.')) {
                                optimizedInputStrBuilder.insert(startIndex + 1, '(');
                                atBeginning = false;
                                break;
                            }
                            startIndex--;
                        }
                        if (atBeginning) {
                            optimizedInputStrBuilder.insert(0, '(');
                        }
                    }
                    optimizedInputStrBuilder.insert(k + 2, ')');
                }
            }
            inputStr = optimizedInputStrBuilder.toString();
        }
        return inputStr;
    }

    public static void highlightSpecialSymbols(TextView textView) {
        String text = textView.getText().toString();
        SpannableString spannableString = new SpannableString(text);

        // 遍历文本，找到特定符号并设置颜色
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // 设置特殊符号的颜色
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.GRAY);
            if (c == '+' || c == '-' || c == '×' || c == '÷' || c == '^') {
                spannableString.setSpan(colorSpan, i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        // 将带有特殊颜色的 SpannableString 设置给 TextView
        textView.setText(spannableString);
    }
}


package com.yangdai.calc.main.calculator;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yangdai.calc.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 30415
 */
public class HistoryListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences historySp;
    private MyScrollListView listView;
    private TextView textView;


    public HistoryListFragment() {
    }

    public static HistoryListFragment newInstance() {
        return new HistoryListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historySp = requireActivity().getSharedPreferences("history", MODE_PRIVATE);
        if (historySp != null) {
            historySp.registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.historyList);
        textView = view.findViewById(R.id.historyHint);
        setupListView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (historySp != null) {
            historySp.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        if ("newHistory".equals(s)) {
            setupListView();
        }
    }

    private void setupListView() {
        if (listView == null) {
            return;
        }
        String historys = historySp.getString("newHistory", "");
        List<String> savedStringList = new ArrayList<>(Arrays.asList(historys.split("//")));
        savedStringList.removeIf(String::isEmpty);
        if (savedStringList.isEmpty()) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
        HistoryAdapter adapter = new HistoryAdapter(requireContext(), savedStringList, str -> {
            if (!str.isEmpty()) {
                Bundle result = new Bundle();
                result.putString("select", str.trim());
                getParentFragmentManager().setFragmentResult("requestKey", result);
                ViewPager2 viewPager = requireParentFragment().requireView().findViewById(R.id.view_pager);
                viewPager.setCurrentItem(1);
            }
        });
        listView.setAdapter(adapter);
    }
}

package com.yangdai.calc.main.toolbox;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.yangdai.calc.R;
import com.yangdai.calc.main.ItemClick;

import java.util.List;

/**
 * @author 30415
 */
public class ToolBoxAdapter extends RecyclerView.Adapter<ToolBoxAdapter.ViewHolder> {
    private final List<ToolBoxItem> list;
    final ItemClick itemClick;
    private final boolean isGrid;

    public ToolBoxAdapter(List<ToolBoxItem> list, boolean isGrid, ItemClick itemClick) {
        this.list = list;
        this.itemClick = itemClick;
        this.isGrid = isGrid;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isGrid){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item_toolbox, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_toolbox, parent, false);
        }
        ViewHolder viewHolder = new ViewHolder(view);

        view.setOnClickListener(v -> {
            int position1 = viewHolder.getBindingAdapterPosition();
            itemClick.onClick(list.get(position1));
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.textView.setText(list.get(position).title());
        holder.imageView.setImageDrawable(list.get(position).drawable());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final ShapeableImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}


package com.yangdai.calc.main.toolbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yangdai.calc.main.toolbox.functions.bmi.BMIActivity;
import com.yangdai.calc.main.toolbox.functions.chinese.ChineseNumberConversionActivity;
import com.yangdai.calc.main.toolbox.functions.programmer.ProgrammerActivity;
import com.yangdai.calc.main.toolbox.functions.shopping.ShoppingActivity;
import com.yangdai.calc.main.toolbox.functions.algebra.StatisticsActivity;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.fraction.FractionActivity;
import com.yangdai.calc.main.toolbox.functions.equation.EquationActivity;
import com.yangdai.calc.main.toolbox.functions.random.RandomNumberActivity;
import com.yangdai.calc.main.toolbox.functions.compass.Compass;
import com.yangdai.calc.main.toolbox.functions.converter.UnitActivity;
import com.yangdai.calc.main.toolbox.functions.currency.CurrencyActivity;
import com.yangdai.calc.main.toolbox.functions.finance.FinanceActivity;
import com.yangdai.calc.main.toolbox.functions.relationship.RelationshipActivity;
import com.yangdai.calc.main.toolbox.functions.time.DateRangeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 30415
 */
public class ToolBoxFragment extends Fragment {
    SharedPreferences sharedPreferences;
    RecyclerView recyclerView;
    ToolBoxAdapter adapter;
    private List<ToolBoxItem> newData;
    private List<ToolBoxItem> data;
    private static final String ORDER = "0/1/2/3/4/5/6/7/8/9/10/11/12/13";
    boolean isGrid;
    private static final int UNIT_ACTIVITY_ID = 0;
    private static final int DATE_RANGE_ACTIVITY_ID = 1;
    private static final int FINANCE_ACTIVITY_ID = 2;
    private static final int COMPASS_ACTIVITY_ID = 3;
    private static final int BMI_ACTIVITY_ID = 4;
    private static final int SHOPPING_ACTIVITY_ID = 5;
    private static final int CURRENCY_ACTIVITY_ID = 6;
    private static final int CHINESE_NUMBER_CONVERSION_ACTIVITY_ID = 7;
    private static final int RELATIONSHIP_ACTIVITY_ID = 8;
    private static final int RANDOM_ACTIVITY_ID = 9;
    private static final int FUNCTION_ACTIVITY_ID = 10;
    private static final int STATISTICS_ACTIVITY_ID = 11;
    private static final int FRACTION_ACTIVITY_ID = 12;
    private static final int PROGRAMMER_ACTIVITY_ID = 13;

    public ToolBoxFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = createToolBoxItems();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private List<ToolBoxItem> createToolBoxItems() {
        List<ToolBoxItem> items = new ArrayList<>();
        items.add(new ToolBoxItem(UNIT_ACTIVITY_ID, getString(R.string.UnitsActivity), getResources().getDrawable(R.drawable.unit_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(DATE_RANGE_ACTIVITY_ID, getString(R.string.dateActivity), getResources().getDrawable(R.drawable.date_range_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(FINANCE_ACTIVITY_ID, getString(R.string.financeActivity), getResources().getDrawable(R.drawable.finance_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(COMPASS_ACTIVITY_ID, getString(R.string.compassActivity), getResources().getDrawable(R.drawable.compass_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(BMI_ACTIVITY_ID, getString(R.string.bmiActivity), getResources().getDrawable(R.drawable.bmi_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(SHOPPING_ACTIVITY_ID, getString(R.string.shoppingActivity), getResources().getDrawable(R.drawable.shopping_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(CURRENCY_ACTIVITY_ID, getString(R.string.exchangeActivity), getResources().getDrawable(R.drawable.currency_exchange_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(CHINESE_NUMBER_CONVERSION_ACTIVITY_ID, getString(R.string.chineseNumberConverter), getResources().getDrawable(R.drawable.chinese_number_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(RELATIONSHIP_ACTIVITY_ID, getString(R.string.relationshipActivity), getResources().getDrawable(R.drawable.relation_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(RANDOM_ACTIVITY_ID, getString(R.string.randomActivity), getResources().getDrawable(R.drawable.random_number_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(FUNCTION_ACTIVITY_ID, getString(R.string.EquationActivity), getResources().getDrawable(R.drawable.functions_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(STATISTICS_ACTIVITY_ID, getString(R.string.statisticActivity), getResources().getDrawable(R.drawable.statistics_icon, requireContext().getTheme())));
        items.add(new ToolBoxItem(FRACTION_ACTIVITY_ID, getString(R.string.numberConvert), getResources().getDrawable(R.drawable.fraction, requireContext().getTheme())));
        items.add(new ToolBoxItem(PROGRAMMER_ACTIVITY_ID, getString(R.string.programmer), getResources().getDrawable(R.drawable.binary_icon, requireContext().getTheme())));
        return items;
    }

    public static ToolBoxFragment newInstance() {
        return new ToolBoxFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_toolbox, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View notes, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(notes, savedInstanceState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        isGrid = sharedPreferences.getBoolean("GridLayout", true);
        getParentFragmentManager().setFragmentResultListener("ChangeLayout", getViewLifecycleOwner(), (requestKey, bundle) -> {
            isGrid = bundle.getBoolean("GridLayout", true);
            updateRecycleView(isGrid);
        });
        String order = sharedPreferences.getString("order", ORDER);
        List<String> orderList = new ArrayList<>(Arrays.asList(order.split("/")));
        if (orderList.size() < data.size()) {
            int oLength = orderList.size();
            for (int i = oLength; i < data.size(); i++) {
                orderList.add(String.valueOf(i));
            }
            String orderString = TextUtils.join("/", orderList);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("order", orderString);
            editor.apply();
        } else if (orderList.size() > data.size()) {
            orderList = new ArrayList<>(Arrays.asList(ORDER.split("/")));
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("order", ORDER);
            editor.apply();
        }

        newData = new ArrayList<>();
        for (String c : orderList) {
            int index = Integer.parseInt(c);
            newData.add(data.get(index));
        }

        recyclerView = requireView().findViewById(R.id.recyclerView);
        updateRecycleView(isGrid);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createItemTouchHelperCallback());
        // 将 ItemTouchHelper 与 RecyclerView 关联
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private ItemTouchHelper.Callback createItemTouchHelperCallback() {
        return new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // 设置拖动和滑动的方向
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                int swipeFlags = 0;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // 处理拖动操作，更新数据集和适配器
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();
                ToolBoxItem movedToolBoxItem = newData.remove(fromPosition);
                newData.add(toPosition, movedToolBoxItem);
                adapter.notifyItemMoved(fromPosition, toPosition);
                StringBuilder newOrder = new StringBuilder();
                for (int i = 0; i < newData.size(); i++) {
                    newOrder.append(newData.get(i).id()).append("/");
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("order", newOrder.toString());
                editor.apply();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 处理滑动操作
            }
        };
    }


    private void updateRecycleView(boolean isGrid) {
        adapter = new ToolBoxAdapter(newData, isGrid, (item) -> {
            int itemId = item.id();
            switch (itemId) {
                case UNIT_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), UnitActivity.class));
                case DATE_RANGE_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), DateRangeActivity.class));
                case FINANCE_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), FinanceActivity.class));
                case COMPASS_ACTIVITY_ID -> startActivity(new Intent(getContext(), Compass.class));
                case BMI_ACTIVITY_ID -> startActivity(new Intent(getContext(), BMIActivity.class));
                case SHOPPING_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), ShoppingActivity.class));
                case CURRENCY_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), CurrencyActivity.class));
                case CHINESE_NUMBER_CONVERSION_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), ChineseNumberConversionActivity.class));
                case RELATIONSHIP_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), RelationshipActivity.class));
                case RANDOM_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), RandomNumberActivity.class));
                case FUNCTION_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), EquationActivity.class));
                case STATISTICS_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), StatisticsActivity.class));
                case FRACTION_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), FractionActivity.class));
                case PROGRAMMER_ACTIVITY_ID ->
                        startActivity(new Intent(getContext(), ProgrammerActivity.class));
                default -> {
                }
            }
        });
        recyclerView.setLayoutManager(isGrid ? new GridLayoutManager(getContext(), 3) : new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }
}


package com.yangdai.calc.main.toolbox;

import android.graphics.drawable.Drawable;

/**
 * @author 30415
 */
public record ToolBoxItem(int id, String title, Drawable drawable) {
}


package com.yangdai.calc.main.toolbox.functions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import com.google.android.material.elevation.SurfaceColors;

public abstract class BaseFunctionActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected SharedPreferences defaultSp;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(SurfaceColors.SURFACE_0.getColor(this));
        setRootView();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(SurfaceColors.SURFACE_0.getColor(this)));
            getSupportActionBar().setElevation(0f);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        defaultSp = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSp.registerOnSharedPreferenceChangeListener(this);

        if (defaultSp.getBoolean("screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

    }

    protected abstract void setRootView();

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        defaultSp.unregisterOnSharedPreferenceChangeListener(this);
    }
}

package com.yangdai.calc.main.toolbox.functions.shopping;

import static com.yangdai.calc.utils.Utils.closeKeyboard;
import static com.yangdai.calc.utils.Utils.formatNumberFinance;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;


/**
 * @author 30415
 */
public class UnitPriceFragment extends Fragment implements TextWatcher {
    private TextInputEditText etQuantity, etPrice;
    private TextView tvUnitPrice;

    public UnitPriceFragment() {
    }

    public static UnitPriceFragment newInstance() {
        return new UnitPriceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_unit_price, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        etQuantity.removeTextChangedListener(this);
        etPrice.removeTextChangedListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etQuantity = view.findViewById(R.id.etQuantity);
        etPrice = view.findViewById(R.id.etPrice);
        tvUnitPrice = view.findViewById(R.id.tvUnitPrice);
        etPrice.addTextChangedListener(this);
        etQuantity.addTextChangedListener(this);
        etQuantity.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                etQuantity.clearFocus();
                return true;
            }
            return false;
        });
    }

    private void calculateUnitPrice() {
        if (!TextUtils.isEmpty(etQuantity.getText()) && !TextUtils.isEmpty(etPrice.getText())) {
            String quantityStr = etQuantity.getText().toString();
            String priceStr = etPrice.getText().toString();

            try {
                double quantity = Double.parseDouble(quantityStr);
                double price = Double.parseDouble(priceStr);
                if (quantity == 0) {
                    return;
                }

                double res = price / quantity;

                tvUnitPrice.setText(formatNumberFinance(String.valueOf(res)));
            } catch (Exception e) {
                tvUnitPrice.setText("");
            }

        } else {
            tvUnitPrice.setText("");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        calculateUnitPrice();
    }
}

package com.yangdai.calc.main.toolbox.functions.shopping;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;


/**
 * @author 30415
 */
public class ShoppingActivity extends BaseFunctionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_discount);
    }

    private void initView() {
        ViewPager2 mViewPager = findViewById(R.id.view_pager_main);
        TabLayout mTabLayout = findViewById(R.id.tab_view);

        final String[] tabs = new String[]{getString(R.string.discount_fragment), getString(R.string.unit_price_fragment)};
        final int[] icons = new int[]{R.drawable.discount_icon, R.drawable.unit_price_icon};

        mViewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return DiscountFragment.newInstance();
                } else {
                    return UnitPriceFragment.newInstance();
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    tab.setText(tabs[position]);
                    tab.setIcon(icons[position]);
                }).attach();
    }
}


package com.yangdai.calc.main.toolbox.functions.shopping;

import static com.yangdai.calc.utils.Utils.closeKeyboard;
import static com.yangdai.calc.utils.Utils.formatNumberFinance;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;


/**
 * @author 30415
 */
public class DiscountFragment extends Fragment implements TextWatcher {
    private TextInputEditText etOriginalPrice, etDiscountPercentage;
    private TextView tvDiscountedPrice, tvSavedAmount;

    public DiscountFragment() {
    }

    public static DiscountFragment newInstance() {
        return new DiscountFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_discount, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etOriginalPrice = view.findViewById(R.id.etOriginalPrice);
        etDiscountPercentage = view.findViewById(R.id.etDiscountPercentage);
        tvDiscountedPrice = view.findViewById(R.id.tvDiscountedPrice);
        tvSavedAmount = view.findViewById(R.id.tvSavedAmount);

        etOriginalPrice.addTextChangedListener(this);
        etDiscountPercentage.addTextChangedListener(this);
        etDiscountPercentage.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                etDiscountPercentage.clearFocus();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        etOriginalPrice.removeTextChangedListener(this);
        etDiscountPercentage.removeTextChangedListener(this);
    }

    @SuppressLint("DefaultLocale")
    private void calculateDiscount() {
        if (!TextUtils.isEmpty(etOriginalPrice.getText()) && !TextUtils.isEmpty(etDiscountPercentage.getText())) {
            String originalPriceStr = etOriginalPrice.getText().toString();
            String discountPercentageStr = etDiscountPercentage.getText().toString();

            try {
                double originalPrice = Double.parseDouble(originalPriceStr);
                double discountPercentage = Double.parseDouble(discountPercentageStr);

                double discountAmount = originalPrice * (discountPercentage / 100);
                double discountedPrice = originalPrice - discountAmount;

                tvDiscountedPrice.setText(formatNumberFinance(String.valueOf(discountedPrice)));
                tvSavedAmount.setText(formatNumberFinance(String.valueOf(discountAmount)));
            } catch (Exception e) {
                tvDiscountedPrice.setText("");
                tvSavedAmount.setText("");
            }

        } else {
            tvDiscountedPrice.setText("");
            tvSavedAmount.setText("");
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        calculateDiscount();
    }
}

package com.yangdai.calc.main.toolbox.functions.equation;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.annotation.SuppressLint;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.yangdai.calc.R;
import com.yangdai.calc.utils.Utils;

/**
 * @author 30415
 */
public class LinearFragment extends Fragment {

    private EditText aEditText;
    private EditText bEditText;
    private TextView xTextView;
    private TextView equationView;

    public LinearFragment() {
    }

    public static LinearFragment newInstance() {
        return new LinearFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_linear, container, false);

        // 获取布局中的视图组件
        aEditText = view.findViewById(R.id.aEditText);
        bEditText = view.findViewById(R.id.bEditText);
        xTextView = view.findViewById(R.id.xTextView);
        equationView = view.findViewById(R.id.equation);

        // 添加输入监听器，以便在参数a或b发生更改时重新计算x
        aEditText.addTextChangedListener(textWatcher);
        bEditText.addTextChangedListener(textWatcher);
        bEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                bEditText.clearFocus();
                return true;
            }
            return false;
        });

        return view;
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // 不需要实现
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // 不需要实现
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void afterTextChanged(Editable editable) {
            // 获取输入的参数a和b的值
            String aValue = aEditText.getText().toString();
            String bValue = bEditText.getText().toString();

            // 检查参数a和b是否为空
            if (aValue.isEmpty() && bValue.isEmpty()) {
                equationView.setText("A 𝑥 + B = 0");
                xTextView.setText("");
                return;
            }

            String equation = buildEquation(aValue, bValue);
            equationView.setText(equation);

            try {
                BigDecimal a = parseBigDecimal(aValue);
                BigDecimal b = parseBigDecimal(bValue);
                String x = calculateX(a, b);
                if (getString(R.string.formatError).equals(x)) {
                    xTextView.setText(x);
                    return;
                }

                // 在TextView中显示x的值
                xTextView.setText(Utils.formatNumber(x));
            } catch (Exception e) {
                xTextView.setText("");
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        aEditText.removeTextChangedListener(textWatcher);
        bEditText.removeTextChangedListener(textWatcher);
    }

    private String buildEquation(String aValue, String bValue) {
        StringBuilder equationBuilder = new StringBuilder();

        if (!aValue.isEmpty()) {
            equationBuilder.append(aValue).append(" 𝑥 + ");
        } else {
            equationBuilder.append("A 𝑥 + ");
        }

        if (!bValue.isEmpty()) {
            equationBuilder.append(bValue);
        } else {
            equationBuilder.append("B");
        }

        equationBuilder.append(" = 0");

        return equationBuilder.toString();
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String calculateX(BigDecimal a, BigDecimal b) {
        if (a.compareTo(BigDecimal.ZERO) == 0) {
            return getString(R.string.formatError);
        }

        return (b.negate()).divide(a, 10, BigDecimal.ROUND_HALF_UP).toBigDecimal().toPlainString();
    }
}


package com.yangdai.calc.main.toolbox.functions.equation;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yangdai.calc.R;
import com.yangdai.calc.utils.Utils;

/**
 * @author 30415
 */
public class QuadraticFragment extends Fragment {

    private EditText aEditText, bEditText, cEditText;
    private TextView x1TextView, x2TextView, equationView;

    public QuadraticFragment() {
    }

    public static QuadraticFragment newInstance() {
        return new QuadraticFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quadratic, container, false);

        // 获取布局中的视图组件
        aEditText = view.findViewById(R.id.aEditText);
        bEditText = view.findViewById(R.id.bEditText);
        cEditText = view.findViewById(R.id.cEditText);
        x1TextView = view.findViewById(R.id.x1TextView);
        x2TextView = view.findViewById(R.id.x2TextView);
        equationView = view.findViewById(R.id.equation);

        // 添加输入监听器，以便在参数a、b或c发生更改时重新计算x1和x2
        aEditText.addTextChangedListener(textWatcher);
        bEditText.addTextChangedListener(textWatcher);
        cEditText.addTextChangedListener(textWatcher);
        cEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                cEditText.clearFocus();
                return true;
            }
            return false;
        });

        return view;
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // 不需要实现
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // 不需要实现
        }

        @Override
        public void afterTextChanged(Editable editable) {
            // 获取输入的参数a、b和c的值
            String aValue = aEditText.getText().toString().trim();
            String bValue = bEditText.getText().toString().trim();
            String cValue = cEditText.getText().toString().trim();

            // 检查参数a、b和c是否为空
            if (aValue.isEmpty() && bValue.isEmpty() && cValue.isEmpty()) {
                x1TextView.setText("");
                x2TextView.setText("");
                equationView.setText("A 𝑥² + B 𝑥 + C = 0");
                return;
            }

            equationView.setText(buildEquation(aValue, bValue, cValue));

            try {
                // 解析输入的参数为BigDecimal类型
                BigDecimal a = parseBigDecimal(aValue);
                BigDecimal b = parseBigDecimal(bValue);
                BigDecimal c = parseBigDecimal(cValue);

                // 计算一元二次方程的解
                String result = calculateQuadraticEquation(a, b, c);
                String[] res = new String[2];
                if ("error".equals(result)) {
                    res[0] = getString(R.string.formatError);
                    res[1] = getString(R.string.formatError);
                } else if ("No real roots".equals(result)) {
                    res[0] = getString(R.string.noRoot);
                    res[1] = getString(R.string.noRoot);
                } else {
                    res = result.split(",");
                    res[0] = Utils.formatNumber(res[0]);
                    res[1] = Utils.formatNumber(res[1]);
                }

                // 在TextView中显示解的值
                x1TextView.setText(res[0]);
                x2TextView.setText(res[1]);
            } catch (Exception ignored) {

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        aEditText.removeTextChangedListener(textWatcher);
        bEditText.removeTextChangedListener(textWatcher);
        cEditText.removeTextChangedListener(textWatcher);
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String calculateQuadraticEquation(BigDecimal a, BigDecimal b, BigDecimal c) {
        BigDecimal discriminant = b.pow(BigDecimal.valueOf(2)).subtract(a.multiply(c).multiply(new BigDecimal(4)));

        if (a.compareTo(BigDecimal.ZERO) == 0) {
            return "error";
        } else if (discriminant.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sqrtDiscriminant = sqrt(discriminant);
            BigDecimal x1 = b.negate().add(sqrtDiscriminant).divide(a.multiply(new BigDecimal(2)), 10, BigDecimal.ROUND_HALF_UP);
            BigDecimal x2 = b.negate().subtract(sqrtDiscriminant).divide(a.multiply(new BigDecimal(2)), 10, BigDecimal.ROUND_HALF_UP);
            return x1.toBigDecimal().toPlainString() + "," + x2.toBigDecimal().toPlainString();
        } else if (discriminant.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal x = b.negate().divide(a.multiply(new BigDecimal(2)));
            return x.toBigDecimal().toPlainString() + "," + x.toBigDecimal().toPlainString();
        } else {
            return "No real roots";
        }
    }

    private BigDecimal sqrt(BigDecimal value) {
        BigDecimal sqrt = BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
        return sqrt.setScale(10, BigDecimal.ROUND_HALF_UP);
    }

    private String buildEquation(String aValue, String bValue, String cValue) {
        StringBuilder equationBuilder = new StringBuilder();

        if (!aValue.isEmpty()) {
            equationBuilder.append(aValue).append(" 𝑥² + ");
        } else {
            equationBuilder.append("A 𝑥² + ");
        }

        if (!bValue.isEmpty()) {
            equationBuilder.append(bValue).append(" 𝑥 + ");
        } else {
            equationBuilder.append("B 𝑥 + ");
        }

        if (!cValue.isEmpty()) {
            equationBuilder.append(cValue);
        } else {
            equationBuilder.append("C");
        }
        equationBuilder.append(" = 0");

        return equationBuilder.toString();
    }
}


package com.yangdai.calc.main.toolbox.functions.equation;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;


/**
 * @author 30415
 */
public class EquationActivity extends BaseFunctionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_equation);
    }

    private void initView() {
        ViewPager2 mViewPager = findViewById(R.id.view_pager_main);
        TabLayout mTabLayout = findViewById(R.id.tab_view);

        final String[] tabs = new String[]{getString(R.string.linear), getString(R.string.quadratic)};
        final int[] icons = new int[]{R.drawable.linear_icon, R.drawable.wave_icon};

        mViewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return LinearFragment.newInstance();
                } else {
                    return QuadraticFragment.newInstance();
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    tab.setText(tabs[position]);
                    tab.setIcon(icons[position]);
                }).attach();
    }
}


package com.yangdai.calc.main.toolbox.functions.converter;

import android.icu.math.BigDecimal;

import java.util.Map;

/**
 * @author 30415
 */
public class UnitConverter {
    /**
     * 单位转换
     */
    public static UnitValue convert(String physicalName, String from, String to, BigDecimal value, int scale) {
        Map<String, Double> unitTable = UnitTable.getUnitTable(physicalName);
        BigDecimal fromValue = new BigDecimal(String.valueOf(unitTable.get(from)));
        BigDecimal toValue = new BigDecimal(String.valueOf(unitTable.get(to)));

        UnitValue unitValue = new UnitValue();

        value = value.multiply(fromValue);
        value = value.divide(toValue, scale, BigDecimal.ROUND_HALF_UP);
        unitValue.setValue(value);
        unitValue.setUnit(to);
        return unitValue;
    }
}


package com.yangdai.calc.main.toolbox.functions.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 30415
 */
public class UnitTable {
    private UnitTable() {
    }

    private static final Map<String, Map<String, Double>> UNIT_TABLE = new HashMap<>();
    /**
     * 长度单位，以m为基准单位，指定单位换算表
     */
    private static final Map<String, Double> LENGTH_UNIT_MAP = new HashMap<>();
    /**
     * 面积单位，以m2为基准单位，指定单位换算表
     */
    private static final Map<String, Double> AREA_UNIT_MAP = new HashMap<>();
    /**
     * 体积单位，容积单位，以 m3（立方米）为基准单位，指定单位换算表
     */
    private static final Map<String, Double> VOLUME_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> MASS_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> STORAGE_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> HEAT_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> PRESSURE_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> VELOCITY_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> TIME_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> ANGLE_UNIT_MAP = new HashMap<>();
    private static final Map<String, Double> POWER_UNIT_MAP = new HashMap<>();

    static {
        LENGTH_UNIT_MAP.put("mm", 0.001);
        LENGTH_UNIT_MAP.put("cm", 0.01);
        LENGTH_UNIT_MAP.put("dm", 0.1);
        LENGTH_UNIT_MAP.put("m", 1.0);
        LENGTH_UNIT_MAP.put("km", 1000.0);
        LENGTH_UNIT_MAP.put("ft", 0.3048); // Feet
        LENGTH_UNIT_MAP.put("in", 0.0254); // Inches
        LENGTH_UNIT_MAP.put("yd", 0.9144); // Yards
        LENGTH_UNIT_MAP.put("mi", 1609.34); // Miles
        LENGTH_UNIT_MAP.put("NM", 1852.0); // Nautical miles

        AREA_UNIT_MAP.put("km²", 1000000.0);
        AREA_UNIT_MAP.put("m²", 1.0);
        AREA_UNIT_MAP.put("dm²", 0.01);
        AREA_UNIT_MAP.put("cm²", 0.0001);
        AREA_UNIT_MAP.put("a", 100.0); // Square decameter
        AREA_UNIT_MAP.put("ha", 10000.0); // Hectare
        AREA_UNIT_MAP.put("顷", 66666.6667); // Acre
        AREA_UNIT_MAP.put("亩", 666.6667); // Mu
        AREA_UNIT_MAP.put("坪", 3.30578512397); // Ping
        AREA_UNIT_MAP.put("ft²", 0.092903); // Square foot
        AREA_UNIT_MAP.put("in²", 0.00064516); // Square inch

        VOLUME_UNIT_MAP.put("cm³", 0.000001);
        VOLUME_UNIT_MAP.put("dm³", 0.001);
        VOLUME_UNIT_MAP.put("m³", 1.0);
        VOLUME_UNIT_MAP.put("L", 0.001);
        VOLUME_UNIT_MAP.put("mL", 0.000001);

        MASS_UNIT_MAP.put("mg", 0.000001);
        MASS_UNIT_MAP.put("g", 0.001);
        MASS_UNIT_MAP.put("kg", 1.0);
        MASS_UNIT_MAP.put("t", 1000.0);
        MASS_UNIT_MAP.put("lb", 0.453592); // Pound
        MASS_UNIT_MAP.put("oz", 0.0283495); // Ounce

        STORAGE_UNIT_MAP.put("bit", 1.0);
        STORAGE_UNIT_MAP.put("B", 8.0); // Byte
        STORAGE_UNIT_MAP.put("KB", 8000.0); // Kilobyte
        STORAGE_UNIT_MAP.put("KiB", 8192.0); // Kibibyte
        STORAGE_UNIT_MAP.put("MB", 8000000.0); // Megabyte
        STORAGE_UNIT_MAP.put("MiB", 8388608.0); // Mebibyte
        STORAGE_UNIT_MAP.put("GB", 8000000000.0); // Gigabyte
        STORAGE_UNIT_MAP.put("GiB", 8589934592.0); // Gibibyte
        STORAGE_UNIT_MAP.put("TB", 8000000000000.0); // Terabyte
        STORAGE_UNIT_MAP.put("TiB", 8796093022208.0); // Tebibyte

        HEAT_UNIT_MAP.put("J", 1.0);
        HEAT_UNIT_MAP.put("cal", 4.184);
        HEAT_UNIT_MAP.put("kcal", 4184.0);

        PRESSURE_UNIT_MAP.put("Pa", 1.0);
        PRESSURE_UNIT_MAP.put("bar", 100000.0);
        PRESSURE_UNIT_MAP.put("psi", 6894.76);
        PRESSURE_UNIT_MAP.put("atm", 101325.0);
        PRESSURE_UNIT_MAP.put("mmHg", 133.3223);

        VELOCITY_UNIT_MAP.put("m/s", 1.0);
        VELOCITY_UNIT_MAP.put("km/h", 0.277778);
        VELOCITY_UNIT_MAP.put("mile/h", 0.44704);
        VELOCITY_UNIT_MAP.put("knots", 0.514444);
        VELOCITY_UNIT_MAP.put("Mach", 340.29);
        VELOCITY_UNIT_MAP.put("c", 299792458.0);
        VELOCITY_UNIT_MAP.put("km/s", 1000.0);

        TIME_UNIT_MAP.put("ms", 0.001); // Millisecond
        TIME_UNIT_MAP.put("s", 1.0);    // Second
        TIME_UNIT_MAP.put("min", 60.0); // Minute
        TIME_UNIT_MAP.put("h", 3600.0); // Hour
        TIME_UNIT_MAP.put("d", 86400.0); // Day
        TIME_UNIT_MAP.put("wk", 604800.0); // Week

        ANGLE_UNIT_MAP.put("°", 1.0); // 度
        ANGLE_UNIT_MAP.put("′", 1.0 / 60); // 分
        ANGLE_UNIT_MAP.put("″", 1.0 / 3600); // 秒
        ANGLE_UNIT_MAP.put("rad", 180 / Math.PI); // 弧度

        POWER_UNIT_MAP.put("kW", 1000.0);
        POWER_UNIT_MAP.put("W", 1.0);
        POWER_UNIT_MAP.put("J/s", 1.0);
        POWER_UNIT_MAP.put("hp", 745.69987); // Horsepower
        POWER_UNIT_MAP.put("ps", 735.49875); // Metric horsepower
        POWER_UNIT_MAP.put("kcal/s", 4184.0); // Kilocalorie per second
        POWER_UNIT_MAP.put("N•m/s", 1.0); // Newton-meter per second
        POWER_UNIT_MAP.put("kg•m/s", 9.80665); // Kilogram-meter per second
        POWER_UNIT_MAP.put("Btu/s", 1055.056); // British thermal unit per second
        POWER_UNIT_MAP.put("ft•lb/s", 1.355817948); // Foot-pound force per second

        UNIT_TABLE.put("a", LENGTH_UNIT_MAP);
        UNIT_TABLE.put("b", AREA_UNIT_MAP);
        UNIT_TABLE.put("c", VOLUME_UNIT_MAP);
        UNIT_TABLE.put("e", MASS_UNIT_MAP);
        UNIT_TABLE.put("f", STORAGE_UNIT_MAP);
        UNIT_TABLE.put("g", PRESSURE_UNIT_MAP);
        UNIT_TABLE.put("h", HEAT_UNIT_MAP);
        UNIT_TABLE.put("i", VELOCITY_UNIT_MAP);
        UNIT_TABLE.put("j", TIME_UNIT_MAP);
        UNIT_TABLE.put("k", ANGLE_UNIT_MAP);
        UNIT_TABLE.put("p", POWER_UNIT_MAP);
    }

    public static Map<String, Double> getUnitTable(String unitName) {
        if (UNIT_TABLE.containsKey(unitName)) {
            return UNIT_TABLE.get(unitName);
        } else {
            return new HashMap<>(0);
        }
    }
}


package com.yangdai.calc.main.toolbox.functions.converter;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.icu.math.BigDecimal;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.elevation.SurfaceColors;
import com.google.android.material.tabs.TabLayout;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;
import com.yangdai.calc.utils.TouchAnimation;
import com.yangdai.calc.utils.Utils;

import java.util.Objects;

/**
 * @author 30415
 */
public class UnitActivity extends BaseFunctionActivity implements View.OnClickListener, TextWatcher {
    /**
     * 1：长度换算；2：面积换算；
     * 3：体积转换 4: 质量 5: 温度 6: 容量 7: 压力 8: 热量 9: 速度 10: 时间 11: 角度 12: 功率
     */
    private int flag = 1;
    private TextView tvInput, tvOutput;
    private String inputStr = "", outputStr = "";
    private String input = "";
    private Button btInput, btOutput;
    private static final String[] ITEMS_DISTANCE = {"km", "m", "dm", "cm", "mm", "ft", "in", "yd", "mi", "NM"};
    private static final String[] ITEMS_AREA = {"km²", "m²", "dm²", "cm²", "a", "ha", "顷", "亩", "坪", "ft²", "in²"};
    private static final String[] ITEMS_VOLUME = {"L", "m³", "dm³", "cm³", "mL"};
    private static final String[] ITEMS_MASS = {"mg", "g", "kg", "oz", "lb"};
    private static final String[] ITEMS_TEMPE = {"℃", "℉"};
    private static final String[] ITEMS_STORAGE = {"bit", "B", "KB", "KiB", "MB", "MiB", "GB", "GiB", "TB", "TiB"};
    private static final String[] ITEMS_PRESSURE = {"Pa", "bar", "psi", "atm", "mmHg"};
    private static final String[] ITEMS_HEAT = {"J", "cal", "kcal"};
    private static final String[] ITEMS_SPEED = {"m/s", "km/h", "km/s", "mile/h", "knots", "Mach", "c"};
    private static final String[] ITEMS_TIME = {"ms", "s", "min", "h", "d", "wk"};
    private static final String[] ITEMS_ANGLE = {"°", "′", "″", "rad"};
    private static final String[] ITEMS_POWER = {"kW", "W", "J/s", "hp", "ps", "kcal/s", "N•m/s", "kg•m/s", "Btu/s", "ft•lb/s"};
    TabLayout mTabLayout;
    private static final int[] BUTTON_IDS = {R.id.seven, R.id.eight,
            R.id.nine, R.id.four, R.id.five, R.id.six, R.id.three, R.id.clean,
            R.id.two, R.id.one, R.id.zero, R.id.switchUnit, R.id.delete, R.id.dot};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        tvInput.addTextChangedListener(this);
        btInput.addTextChangedListener(this);
        btOutput.addTextChangedListener(this);
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_unit);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tvInput.removeTextChangedListener(this);
        btInput.removeTextChangedListener(this);
        btOutput.removeTextChangedListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        btInput = findViewById(R.id.input);
        btOutput = findViewById(R.id.output);
        tvInput = findViewById(R.id.change_1);
        tvOutput = findViewById(R.id.change_result);
        findViewById(R.id.parent).setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this));
        mTabLayout = findViewById(R.id.tab_view);

        final String[] tabs = {getString(R.string.length), getString(R.string.area), getString(R.string.volume), getString(R.string.mass), getString(R.string.tempe),
                getString(R.string.storage), getString(R.string.pressure), getString(R.string.heat), getString(R.string.speed), getString(R.string.time), getString(R.string.angle), getString(R.string.powerUnit)};

        final int[] icons = {R.drawable.length_icon, R.drawable.area_icon, R.drawable.volume_icon, R.drawable.mass_icon, R.drawable.tempe_icon,
                R.drawable.data_icon, R.drawable.pressure_icon, R.drawable.heat_icon, R.drawable.speed_icon, R.drawable.time_icon, R.drawable.angle_icon, R.drawable.power_icon};

        for (int i = 0; i < tabs.length; i++) {
            TabLayout.Tab tab = mTabLayout.newTab();
            tab.setText(tabs[i]);
            tab.setIcon(icons[i]);
            mTabLayout.addTab(tab);
        }

        mTabLayout.setSmoothScrollingEnabled(true);
        mTabLayout.selectTab(null);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                flag = tab.getPosition() + 1;
                SharedPreferences.Editor editor = defaultSp.edit();
                editor.putInt("selectedTab", tab.getPosition());
                editor.apply();

                btInput.setText("");
                btOutput.setText("");
                tvInput.setText("");
                input = "";
                tvOutput.setText("");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    int tabNum = defaultSp.getInt("selectedTab", 0);
                    if (tabNum < tabs.length) {
                        Objects.requireNonNull(mTabLayout.getTabAt(tabNum)).select();
                    } else {
                        Objects.requireNonNull(mTabLayout.getTabAt(0)).select();
                    }
                },
                100);

        for (int buttonId : BUTTON_IDS) {
            findViewById(buttonId).setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
            findViewById(buttonId).setOnClickListener(this);
            TouchAnimation touchAnimation = new TouchAnimation(findViewById(buttonId));
            findViewById(buttonId).setOnTouchListener(touchAnimation);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.delete) {
            if (!input.isEmpty()) {
                input = input.substring(0, input.length() - 1);
                tvInput.setText(Utils.formatNumber(input));
            }
        } else if (v.getId() == R.id.clean) {
            tvInput.setText("");
            tvOutput.setText("");
            input = "";
        } else if (v.getId() == R.id.switchUnit) {
            // 交换字符串的值
            String temp = inputStr;
            inputStr = outputStr;
            outputStr = temp;
            btInput.setText(inputStr);
            btOutput.setText(outputStr);
        } else {
            // 限制输入长度
            if (input.length() < 9) {
                String append = ((MaterialButton) v).getText().toString();
                if (".".equals(append)) {
                    if (input.isEmpty() || input.contains(".")) {
                        return;
                    }
                }
                input = input + append;
                tvInput.setText(Utils.formatNumber(input));
            }
        }
    }

    private void showOptions(View view, String[] items, boolean isInput) {
        ListPopupWindow listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAnchorView(view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                com.google.android.material.R.layout.support_simple_spinner_dropdown_item, items);
        listPopupWindow.setAdapter(adapter);
        listPopupWindow.setOnItemClickListener((parent, view1, position, id) -> {
            if (isInput) {
                inputStr = items[position];
                btInput.setText(inputStr);
            } else {
                outputStr = items[position];
                btOutput.setText(outputStr);
            }
            listPopupWindow.dismiss();
        });
        listPopupWindow.show();
    }

    public void showInputOptions(View view) {
        switchOption(view, true);
    }

    public void showOutputOptions(View view) {
        switchOption(view, false);
    }

    private void switchOption(View view, boolean isInput) {
        switch (flag) {
            case 1 -> showOptions(view, ITEMS_DISTANCE, isInput);
            case 2 -> showOptions(view, ITEMS_AREA, isInput);
            case 3 -> showOptions(view, ITEMS_VOLUME, isInput);
            case 4 -> showOptions(view, ITEMS_MASS, isInput);
            case 5 -> showOptions(view, ITEMS_TEMPE, isInput);
            case 6 -> showOptions(view, ITEMS_STORAGE, isInput);
            case 7 -> showOptions(view, ITEMS_PRESSURE, isInput);
            case 8 -> showOptions(view, ITEMS_HEAT, isInput);
            case 9 -> showOptions(view, ITEMS_SPEED, isInput);
            case 10 -> showOptions(view, ITEMS_TIME, isInput);
            case 11 -> showOptions(view, ITEMS_ANGLE, isInput);
            case 12 -> showOptions(view, ITEMS_POWER, isInput);
            default -> {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void change() {
        String x = input;
        int scale = defaultSp.getInt("scale", 10);
        BigDecimal num;
        try {
            num = new BigDecimal(x);
        } catch (Exception e) {
            tvOutput.setText("");
            return;
        }

        UnitValue unitValue;
        switch (flag) {
            case 1 -> unitValue = convertUnit("a", inputStr, outputStr, num, scale);
            case 2 -> unitValue = convertUnit("b", inputStr, outputStr, num, scale);
            case 3 -> unitValue = convertUnit("c", inputStr, outputStr, num, scale);
            case 4 -> unitValue = convertUnit("e", inputStr, outputStr, num, scale);
            case 5 -> unitValue = convertTemperature(inputStr, outputStr, num, scale);
            case 6 -> unitValue = convertUnit("f", inputStr, outputStr, num, scale);
            case 7 -> unitValue = convertUnit("g", inputStr, outputStr, num, scale);
            case 8 -> unitValue = convertUnit("h", inputStr, outputStr, num, scale);
            case 9 -> unitValue = convertUnit("i", inputStr, outputStr, num, scale);
            case 10 -> unitValue = convertUnit("j", inputStr, outputStr, num, scale);
            case 11 -> unitValue = convertUnit("k", inputStr, outputStr, num, scale);
            case 12 -> unitValue = convertUnit("p", inputStr, outputStr, num, scale);
            default -> unitValue = null;
        }

        if (unitValue != null) {
            String res = unitValue.getValue().toBigDecimal().toPlainString();
            tvOutput.setText(Utils.formatNumber(res));
        } else {
            tvOutput.setText("");
        }
    }

    private UnitValue convertUnit(String unitType, String inputStr, String outputStr, BigDecimal num, int scale) {
        try {
            return UnitConverter.convert(unitType, inputStr, outputStr, num, scale);
        } catch (Exception e) {
            return null;
        }
    }

    private UnitValue convertTemperature(String inputStr, String outputStr, BigDecimal num, int scale) {
        BigDecimal temp;
        switch (inputStr) {
            case "℃" -> {
                temp = num.multiply(BigDecimal.valueOf(1.8));
                temp = temp.add(BigDecimal.valueOf(32));
                temp.setScale(scale, BigDecimal.ROUND_HALF_UP);
            }
            case "℉" -> {
                temp = num.subtract(BigDecimal.valueOf(32));
                temp = temp.divide(BigDecimal.valueOf(1.8), scale, BigDecimal.ROUND_HALF_UP);
            }
            default -> temp = null;
        }
        return new UnitValue(temp, outputStr);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (!input.isEmpty()
                && !btInput.getText().toString().isEmpty() && !btOutput.getText().toString().isEmpty()) {
            change();
        }
        if (input.isEmpty()) {
            tvOutput.setText("");
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        if ("vib".equals(s)) {
            for (int buttonId : BUTTON_IDS) {
                findViewById(buttonId).setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
            }
        } else if ("scale".equals(s)) {
            if (!input.isEmpty()
                    && !btInput.getText().toString().isEmpty() && !btOutput.getText().toString().isEmpty()) {
                change();
            }
            if (input.isEmpty()) {
                tvOutput.setText("");
            }
        }
    }
}


package com.yangdai.calc.main.toolbox.functions.converter;

import android.icu.math.BigDecimal;

import androidx.annotation.NonNull;

/**
 * 带单位的值。
 *
 * @author 30415
 */
public class UnitValue {
    private BigDecimal value;
    private String unit;

    public UnitValue() {
        super();
    }

    /**
     * @param value 数值
     * @param unit  单位
     */
    public UnitValue(BigDecimal value, String unit) {
        super();
        this.value = value;
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    /** @noinspection unused*/
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        long temp;
        temp = value.longValue();
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnitValue other = (UnitValue) obj;
        if (unit == null) {
            if (other.unit != null) {
                return false;
            }
        } else if (!unit.equals(other.unit)) {
            return false;
        }
        return value.longValue() == (other.value).longValue();
    }

    @NonNull
    @Override
    public String toString() {
        return value + unit;
    }
}


package com.yangdai.calc.main.toolbox.functions.bmi;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;

import com.google.android.material.elevation.SurfaceColors;
import com.yangdai.calc.R;
import com.yangdai.calc.databinding.ActivityBmiBinding;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;

/**
 * @author 30415
 */
public class BMIActivity extends BaseFunctionActivity implements TextWatcher {
    double heightM = 0, weightKg = 0, bmi = 0, recommendedBmi = 0;
    String weightToLose = "";
    ActivityBmiBinding binding;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            resetColor();

            binding.heightCm.setText("");
            binding.weightKg.setText("");
            binding.bmi.setText(R.string._00_00);
            binding.comment.setText("");
            bmi = 0;
            heightM = 0;
            weightKg = 0;
            recommendedBmi = 0;
            weightToLose = "";
            binding.commentLayout.setVisibility(View.GONE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding.cardView.setCardBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.CardViewRes.setCardBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));

        binding.heightCm.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(this);
                binding.heightCm.clearFocus();
                return true;
            }
            return false;
        });
        binding.weightKg.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(this);
                binding.weightKg.clearFocus();
                return true;
            }
            return false;
        });
        binding.heightCm.addTextChangedListener(this);
        binding.weightKg.addTextChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.heightCm.removeTextChangedListener(this);
        binding.weightKg.removeTextChangedListener(this);
    }

    @Override
    protected void setRootView() {
        binding = ActivityBmiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @SuppressLint("DefaultLocale")
    private void calculateBmi(double weight, double height) {
        resetColor();
        binding.bmi.setText(R.string._00_00);
        binding.comment.setText("");
        bmi = 0;
        recommendedBmi = 0;
        weightToLose = "";
        binding.commentLayout.setVisibility(View.GONE);

        try {
            weightKg = weight;
            heightM = height / 100;
            bmi = weight / (heightM * heightM);
            binding.bmi.setText(String.format("%.2f", bmi));
        } catch (Exception ignored) {

        }

        if (bmi > 0) {
            setMessageBackground();
            findRecommendedBmi();
        }
    }

    private void findRecommendedBmi() {
        if (bmi < 18.50) {
            for (int i = 1; i < 100; i++) {
                double newWeight = weightKg + i;
                recommendedBmi = newWeight / (heightM * heightM);
                if (recommendedBmi >= 18.5) {
                    weightToLose = String.valueOf(i);
                    binding.commentLayout.setVisibility(View.VISIBLE);
                    binding.weightNeed.setText(R.string.need_healthy);
                    binding.showKg.setText(R.string.kg);
                    binding.reCommand.setText(weightToLose);
                    break;
                }
            }

        } else if (bmi > 24.90) {
            for (int i = 1; i < 150; i++) {
                double newWeight = weightKg - i;
                recommendedBmi = newWeight / (heightM * heightM);
                if (recommendedBmi <= 24.9) {
                    weightToLose = String.valueOf(i);
                    binding.commentLayout.setVisibility(View.VISIBLE);
                    binding.weightNeed.setText(R.string.lose_healthy);
                    binding.showKg.setText(R.string.kg);
                    binding.reCommand.setText(weightToLose);
                    break;
                }
            }
        } else {
            weightToLose = String.valueOf(0);
        }
    }

    private void setMessageBackground() {
        if (bmi < 16.0) {
            binding.comment.setText(R.string.very_severely_underweight);
            binding.verySeverelyUnderweight.setBackgroundColor(getColor(R.color.Very_Severely_underweight));
        } else if (bmi >= 16.0 && bmi <= 16.99) {
            binding.comment.setText(R.string.severely_underweight);
            binding.severelyUnderweight.setBackgroundColor(getColor(R.color.Severely_underweight));
        } else if (bmi >= 17.0 && bmi <= 18.49) {
            binding.comment.setText(R.string.underweight);
            binding.underweight.setBackgroundColor(getColor(R.color.Underweight));

        } else if (bmi >= 18.5 && bmi <= 24.99) {
            binding.comment.setText(R.string.healthy);
            binding.healthy.setBackgroundColor(getColor(R.color.Healthy));

        } else if (bmi >= 25.0 && bmi <= 29.99) {
            binding.comment.setText(R.string.overweight);
            binding.overweight.setBackgroundColor(getColor(R.color.Overweight));

        } else if (bmi >= 30.0 && bmi <= 34.99) {
            binding.comment.setText(R.string.obese_class_i);
            binding.obeseClassI.setBackgroundColor(getColor(R.color.Obese_Class_I));

        } else if (bmi >= 35.0 && bmi <= 39.99) {
            binding.comment.setText(R.string.obese_class_ii);
            binding.obeseClassIi.setBackgroundColor(getColor(R.color.Obese_Class_ii));

        } else if (bmi >= 40.0) {
            binding.comment.setText(R.string.obese_class_ii);
            binding.obeseClassIii.setBackgroundColor(getColor(R.color.Obese_Class_iii));
        }
    }

    private void resetColor() {
        binding.verySeverelyUnderweight.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.severelyUnderweight.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.underweight.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.healthy.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.overweight.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.obeseClassI.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.obeseClassIi.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
        binding.obeseClassIii.setBackgroundColor(SurfaceColors.SURFACE_5.getColor(this));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!TextUtils.isEmpty(binding.heightCm.getText()) && !TextUtils.isEmpty(binding.weightKg.getText())) {
            String cm = binding.heightCm.getText().toString();
            String kg = binding.weightKg.getText().toString();
            try {
                double height = Double.parseDouble(cm);
                double weight = Double.parseDouble(kg);
                if (weight != 0 && height != 0) {
                    calculateBmi(weight, height);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

package com.yangdai.calc.main.toolbox.functions.time;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


/**
 * @author 30415
 */
public class DateRangeActivity extends BaseFunctionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_date_range);
    }

    private void initView() {
        ViewPager2 mViewPager = findViewById(R.id.view_pager_main);
        TabLayout mTabLayout = findViewById(R.id.tab_view);

        final String[] tabs = new String[]{getString(R.string.text_date_diff), getString(R.string.text_addsub_date)};
        final int[] icons = new int[]{R.drawable.calendar_diff, R.drawable.calendar_add};

        mViewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return DateDifferCalcFragment.newInstance();
                } else {
                    return DateAddSubCalcFragment.newInstance();
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    tab.setText(tabs[position]);
                    tab.setIcon(icons[position]);
                }).attach();
    }
}


package com.yangdai.calc.main.toolbox.functions.time;

import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.yangdai.calc.R;

import java.util.Date;
import java.util.Locale;

/**
 * @author 30415
 */
public class DateAddSubCalcFragment extends Fragment {

    private final Date chosenTimeStart = new Date(MaterialDatePicker.todayInUtcMilliseconds());
    private int inputtedDay = 0;
    private int inputtedMonth = 0;
    private int inputtedYear = 0;
    private boolean inputIsAdd = true;

    private Button btnStartDate;
    private TextView textResultDate;
    private RadioButton radioButtonAdd;
    private RadioButton radioButtonSub;

    public static DateAddSubCalcFragment newInstance() {
        return new DateAddSubCalcFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_date_addsub_calc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initControls(view);
    }

    private void initControls(View view) {

        MaterialDatePicker<Long> materialDatePickerStart = MaterialDatePicker.Builder.datePicker()
                .setTitleText("")
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        materialDatePickerStart.addOnPositiveButtonClickListener(selection -> {
            chosenTimeStart.setTime(selection);
            updateTimeStart();
            calculate();
        });

        btnStartDate = view.findViewById(R.id.btn_start_date);
        // Show the dialog when the start date button is clicked
        btnStartDate.setOnClickListener(v -> materialDatePickerStart.show(getParentFragmentManager(), "DATE_PICKER_START"));

        textResultDate = view.findViewById(R.id.text_result_date);
        radioButtonSub = view.findViewById(R.id.radio_sub);
        radioButtonAdd = view.findViewById(R.id.radio_add);

        EditText editDay = view.findViewById(R.id.edit_day);
        EditText editMonth = view.findViewById(R.id.edit_month);
        EditText editYear = view.findViewById(R.id.edit_year);

        editDay.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    inputtedDay = Integer.parseInt(s.toString());
                } else {
                    inputtedDay = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        });

        editMonth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    inputtedMonth = Integer.parseInt(s.toString());
                } else {
                    inputtedMonth = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        });

        editYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    inputtedYear = Integer.parseInt(s.toString());
                } else {
                    inputtedYear = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        });

        radioButtonAdd.setOnClickListener((View v) -> {
            inputIsAdd = true;
            radioButtonSub.setChecked(false);
            calculate();
        });

        radioButtonSub.setOnClickListener((View v) -> {
            inputIsAdd = false;
            radioButtonAdd.setChecked(false);
            calculate();
        });

        view.findViewById(R.id.btn_clear).setOnClickListener((v) -> {
            editYear.setText("");
            editMonth.setText("");
            editDay.setText("");
            textResultDate.setText("");
        });

        updateTimeStart();
    }

    private void updateTimeStart() {
        btnStartDate.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(chosenTimeStart));
    }

    private void calculate() {
        try {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(chosenTimeStart);
            calendar1.add(Calendar.DAY_OF_YEAR, inputIsAdd ? inputtedDay : -inputtedDay);
            calendar1.add(Calendar.MONTH, inputIsAdd ? inputtedMonth : -inputtedMonth);
            calendar1.add(Calendar.YEAR, inputIsAdd ? inputtedYear : -inputtedYear);
            textResultDate.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(calendar1.getTime()));
        } catch (Exception e) {
            textResultDate.setText("");
        }
    }
}

package com.yangdai.calc.main.toolbox.functions.time;

import android.icu.text.DateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.yangdai.calc.R;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


/**
 * @author 30415
 */
public class DateDifferCalcFragment extends Fragment {
    private Button btnStartDate;
    private Button btnEndDate;
    private TextView textResultDate;

    private final Date chosenTimeStart = new Date(MaterialDatePicker.todayInUtcMilliseconds());
    private final Date chosenTimeEnd = new Date(MaterialDatePicker.todayInUtcMilliseconds());

    public static DateDifferCalcFragment newInstance() {
        return new DateDifferCalcFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmen_date_diff_calc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initControls(view);
    }

    private void initControls(View view) {

        MaterialDatePicker<Long> materialDatePickerStart = MaterialDatePicker.Builder.datePicker()
                .setTitleText("")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setNegativeButtonText(android.R.string.cancel)
                .build();

        materialDatePickerStart.addOnPositiveButtonClickListener(selection -> {
            chosenTimeStart.setTime(selection);
            updateTimeStart();
            calculate();
        });

        MaterialDatePicker<Long> materialDatePickerEnd = MaterialDatePicker.Builder.datePicker()
                .setTitleText("")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setNegativeButtonText(android.R.string.cancel)
                .build();

        materialDatePickerEnd.addOnPositiveButtonClickListener(selection -> {
            chosenTimeEnd.setTime(selection);
            updateTimeEnd();
            calculate();
        });

        btnStartDate = view.findViewById(R.id.btn_start_date);
        btnEndDate = view.findViewById(R.id.btn_end_date);
        textResultDate = view.findViewById(R.id.text_result_date);

        // Show the dialog when the start date button is clicked
        btnStartDate.setOnClickListener(v -> materialDatePickerStart.show(getParentFragmentManager(), "DATE_PICKER_START"));
        // Show the dialog when the end date button is clicked
        btnEndDate.setOnClickListener(v -> materialDatePickerEnd.show(getParentFragmentManager(), "DATE_PICKER_END"));

        updateTimeStart();
        updateTimeEnd();
    }

    private void updateTimeStart() {
        btnStartDate.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(chosenTimeStart));
    }

    private void updateTimeEnd() {
        btnEndDate.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(chosenTimeEnd));
    }

    private void calculate() {
        Date chosenTimeStartS;
        Date chosenTimeEndS;

        if (chosenTimeStart.compareTo(chosenTimeEnd) == 0) {
            textResultDate.setText(getString(R.string.text_same_day));
            return;
        }
        if (chosenTimeStart.after(chosenTimeEnd)) {
            chosenTimeStartS = chosenTimeEnd;
            chosenTimeEndS = chosenTimeStart;
        } else {
            chosenTimeStartS = chosenTimeStart;
            chosenTimeEndS = chosenTimeEnd;
        }

        try {
            long durationInMillis = chosenTimeEndS.getTime() - chosenTimeStartS.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(durationInMillis);
            textResultDate.setText(String.valueOf(days));
        } catch (Exception e) {
            textResultDate.setText("");
        }
    }
}

package com.yangdai.calc.main.toolbox.functions.relationship;

import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;
import com.yangdai.calc.utils.TouchAnimation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 30415
 */
public class RelationshipActivity extends BaseFunctionActivity implements View.OnClickListener {
    MaterialRadioButton male, female;
    RadioGroup radioGroup;
    TextView tvInput;
    TextView tvOutput;

    // 初始值的性别
    private boolean initSex;
    // 当前结果的性别
    private boolean isFemale;
    // 操作数数组
    private final List<String> callList = new ArrayList<>();
    // 显示的文本内容
    private String showText = "";
    // 当前运算结果
    private String resultsText = "";
    private Button[] buttonArr;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button[] buttons = {
                findViewById(R.id.btn_h),
                findViewById(R.id.btn_w),
                findViewById(R.id.btn_f),
                findViewById(R.id.btn_m),
                findViewById(R.id.btn_ob),
                findViewById(R.id.btn_lb),
                findViewById(R.id.btn_os),
                findViewById(R.id.btn_ls),
                findViewById(R.id.btn_s),
                findViewById(R.id.btn_d),
                findViewById(R.id.btn_each),
                findViewById(R.id.btn_eq),
                findViewById(R.id.btn_clr),
                findViewById(R.id.iv_del)
        };
        buttonArr = buttons;

        radioGroup = findViewById(R.id.ratioGroup);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        tvInput = findViewById(R.id.input_textview);
        tvOutput = findViewById(R.id.output_textview);

        callList.add("我");
        initSex = isFemale = female.isChecked();
        radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.male) {
                initSex = isFemale = false;
            } else {
                initSex = isFemale = true;
            }
            clear();
            forbiddenButton();
            emphasisShowInput();
        });
        // 给按钮设置的点击事件
        for (Button button : buttons) {
            button.setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
            button.setOnClickListener(this);
            TouchAnimation touchAnimation = new TouchAnimation(button);
            button.setOnTouchListener(touchAnimation);
        }
        forbiddenButton();
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_relationship);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.btn_clr
                && v.getId() != R.id.iv_del && v.getId() != R.id.btn_eq
                && "关系有点远，年长的就叫长辈~\n同龄人就叫帅哥美女吧".equals(resultsText)) {
            return;
        }

        // 点击了清除按钮
        if (v.getId() == R.id.btn_clr) {
            emphasisShowInput();
            initSex = isFemale = female.isChecked();
            forbiddenButton();
            clear();
        }

        // 点击了删除按钮
        else if (v.getId() == R.id.iv_del) {
            emphasisShowInput();
            delete();
            if (callList.size() > 1) {
                isFemale = !isMan(callList.get(callList.size() - 1));
            } else {
                isFemale = female.isChecked();
            }
            forbiddenButton();
        }

        // 点击了等于按钮
        else if (v.getId() == R.id.btn_eq) {
            if (callList.size() <= 1) {
                return;
            }
            if (!"TA称呼我".equals(showText) && !"关系有点远，年长的就叫长辈~\n同龄人就叫帅哥美女吧".equals(resultsText)) {
                buttonArr[10].setEnabled(true);
                refreshText();
            }
        }

        // 点击了互查按钮
        else if (v.getId() == R.id.btn_each) {
            if (!"TA称呼我".equals(showText)) {
                peerReview();
            } else {
                emphasisShowInput();
                refreshText();
                buttonArr[10].setEnabled(false);
            }
        }

        // 点击了亲戚关系按钮
        else if (v.getId() == R.id.btn_h) {
            emphasisShowInput();
            callList.add("丈夫");
            isFemale = false;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_w) {
            emphasisShowInput();
            callList.add("妻子");
            isFemale = true;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_f) {
            emphasisShowInput();
            callList.add("爸爸");
            isFemale = false;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_m) {
            emphasisShowInput();
            callList.add("妈妈");
            isFemale = true;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_ob) {
            emphasisShowInput();
            callList.add("哥哥");
            isFemale = false;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_lb) {
            emphasisShowInput();
            callList.add("弟弟");
            isFemale = false;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_os) {
            emphasisShowInput();
            callList.add("姐姐");
            isFemale = true;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_ls) {
            emphasisShowInput();
            callList.add("妹妹");
            isFemale = true;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_s) {
            emphasisShowInput();
            callList.add("儿子");
            isFemale = false;
            forbiddenButton();
            refreshText();
        } else if (v.getId() == R.id.btn_d) {
            emphasisShowInput();
            callList.add("女儿");
            isFemale = true;
            forbiddenButton();
            refreshText();
        }
    }

    // 清空并初始化
    private void clear() {
        callList.clear();
        callList.add("我");
        resultsText = "";
        refreshText();
    }

    // 刷新文本显示
    private void refreshText() {
        showText = "";
        resultsText = "";
        StringBuilder sbShowText = new StringBuilder();

        for (int i = 0; i < callList.size(); i++) {
            sbShowText.append(callList.get(i));
            if (i == callList.size() - 1) {
                break;
            }
            sbShowText.append("的");
        }
        showText = sbShowText.toString();

        if (callList.size() > 8) {
            resultsText = "关系有点远，年长的就叫长辈~\n同龄人就叫帅哥美女吧";
        } else if (callList.size() > 1) {
            operation(callList, initSex);
        }

        tvInput.setText(showText);
        tvOutput.setText(resultsText);
    }

    // 回退
    private void delete() {
        if (callList.size() > 1) {
            callList.remove(callList.size() - 1);
            operation(callList, initSex);
            refreshText();
        }
    }

    // 运算
    private void operation(List<String> list, boolean b) {
        String[][] relationshipData;
        if (b) {
            relationshipData = new RelationShipData().getRelationShipDataByWoman();
        } else {
            relationshipData = new RelationShipData().getRelationShipDataByMan();
        }
        int column = 0, row = 0;
        String resultValue = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            for (int m = 0; m < relationshipData.length; ++m) {
                if (relationshipData[m][0].equals(resultValue)) {
                    row = m;
                    break;
                }
            }
            for (int n = 0; n < relationshipData[0].length; n++) {
                if (relationshipData[0][n].equals(list.get(i))) {
                    column = n;
                    break;
                }
            }
            resultValue = relationshipData[row][column];
            if (!isExist(resultValue, relationshipData)) {
                resultValue = "未知亲戚";
                break;
            }
        }
        if ("未知亲戚".equals(resultValue) || "".equals(resultValue)) {
            resultsText = "关系有点远，年长的就叫长辈~\n同龄人就叫帅哥美女吧";
        } else {
            resultsText = resultValue;
        }
    }

    // 判断某个值在二维数组中的行首值中是否存在
    public boolean isExist(String value, String[][] array) {
        for (String[] strings : array) {
            if (value.equals(strings[0])) {
                return true;
            }
        }
        return false;
    }

    // 互查
    private void peerReview() {
        showText = "TA称呼我";
        List<String> tempList = new ArrayList<>();
        boolean tempSex;
        tempList.add("我");
        for (int i = callList.size() - 1; i > 0; i--) {
            if (("我".equals(callList.get(i - 1)) && !initSex) || isMan(callList.get(i - 1))) {
                switch (callList.get(i)) {
                    case "儿子", "女儿" -> tempList.add("爸爸");
                    case "弟弟", "妹妹" -> tempList.add("哥哥");
                    case "哥哥", "姐姐" -> tempList.add("弟弟");
                    case "爸爸", "妈妈" -> tempList.add("儿子");
                    case "妻子" -> tempList.add("丈夫");
                    default -> {
                    }
                }
            } else {
                switch (callList.get(i)) {
                    case "儿子", "女儿" -> tempList.add("妈妈");
                    case "弟弟", "妹妹" -> tempList.add("姐姐");
                    case "哥哥", "姐姐" -> tempList.add("妹妹");
                    case "爸爸", "妈妈" -> tempList.add("女儿");
                    case "丈夫" -> tempList.add("妻子");
                    default -> {
                    }
                }
            }
        }
        // 判断“我”的性别
        tempSex = !isMan(callList.get(callList.size() - 1));
        operation(tempList, tempSex);
        tvInput.setText(showText);
        tvOutput.setText(resultsText);
    }

    // 判断该亲戚是否为男性
    private boolean isMan(String s) {
        return "丈夫".equals(s) || "爸爸".equals(s) || "哥哥".equals(s)
                || "弟弟".equals(s) || "儿子".equals(s);
    }

    // 重点显示输入
    private void emphasisShowInput() {
        buttonArr[10].setEnabled(false);
    }

    // 禁用夫 \ 妻按钮
    private void forbiddenButton() {
        if (isFemale) {
            buttonArr[1].setEnabled(false);
            buttonArr[0].setEnabled(true);
        } else {
            buttonArr[0].setEnabled(false);
            buttonArr[1].setEnabled(true);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        if ("vib".equals(s)) {
            for (Button button : buttonArr) {
                button.setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
            }
        }
    }

}


package com.yangdai.calc.main.toolbox.functions.relationship;

/**
 * @author 30415
 */
public class RelationShipData {
    public String[][] getRelationShipDataByMan() {
        return new String[][] {
                {"我", "爸爸", "妈妈", "哥哥", "弟弟", "姐姐", "妹妹", "儿子", "女儿", "妻子", "丈夫", "未知亲戚"},
                {"爸爸", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "我", "妹妹", "妈妈", "", "未知亲戚"},
                {"妈妈", "外公", "外婆", "大舅", "小舅", "大姨", "小姨", "我", "妹妹", "", "爸爸", "未知亲戚"},
                {"哥哥", "爸爸", "妈妈", "哥哥", "我", "姐姐", "妹妹", "侄子", "侄女", "嫂子", "", "未知亲戚"},
                {"弟弟", "爸爸", "妈妈", "我", "弟弟", "姐姐", "妹妹", "侄子", "侄女", "弟妹", "", "未知亲戚"},
                {"姐姐", "爸爸", "妈妈", "哥哥", "我", "姐姐", "妹妹", "外甥", "外甥女", "", "姐夫", "未知亲戚"},
                {"妹妹", "爸爸", "妈妈", "我", "弟弟", "姐姐", "妹妹", "外甥", "外甥女", "", "妹夫", "未知亲戚"},
                {"儿子", "我", "妻子", "儿子", "儿子", "女儿", "女儿", "孙子", "孙女", "儿媳", "", "未知亲戚"},
                {"女儿", "我", "妻子", "儿子", "儿子", "女儿", "女儿", "外孙", "外孙女", "", "女婿", "未知亲戚"},
                {"妻子", "岳父", "岳母", "大舅子", "小舅子", "大姨子", "小姨子", "儿子", "女儿", "", "我", "未知亲戚"},
                {"丈夫", "", "", "", "", "", "", "", "", "", "", "未知亲戚"},
                {"爷爷", "曾祖父", "曾祖母", "伯祖父", "叔祖父", "祖姑母", "祖姑母", "爸爸", "姑妈", "奶奶", "", "未知亲戚"},
                {"奶奶", "曾外祖父", "曾外祖母", "舅公", "舅公", "祖姨母", "祖姨母", "爸爸", "姑妈", "", "爷爷", "未知亲戚"},
                {"伯父", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "堂哥", "堂姐", "伯母", "", "未知亲戚"},
                {"叔叔", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "堂弟", "堂妹", "婶婶", "", "未知亲戚"},
                {"姑妈", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "姑表哥", "姑表姐", "", "姑丈", "未知亲戚"},
                {"外公", "外曾祖父", "外曾祖母", "伯外祖父", "叔外祖父", "姑外祖母", "姑外祖母", "舅舅", "妈妈", "外婆", "", "未知亲戚"},
                {"外婆", "外曾外祖父", "外曾外祖母", "外舅公", "外舅公", "姨外祖母", "姨外祖母", "舅舅", "妈妈", "", "外公", "未知亲戚"},
                {"大舅", "外公", "外婆", "大舅", "舅舅", "大姨", "妈妈", "舅表哥", "舅表姐", "大舅妈", "", "未知亲戚"},
                {"小舅", "外公", "外婆", "舅舅", "小舅", "妈妈", "小姨", "舅表弟", "舅表妹", "小舅妈", "", "未知亲戚"},
                {"舅舅", "外公", "外婆", "大舅", "小舅", "大姨", "小姨", "舅表哥", "舅表姐", "舅妈", "", "未知亲戚"},
                {"大姨", "外公", "外婆", "大舅", "舅舅", "大姨", "妈妈", "姨表哥", "姨表姐", "", "大姨父", "未知亲戚"},
                {"小姨", "外公", "外婆", "舅舅", "小舅", "妈妈", "小姨", "姨表弟", "姨表妹", "", "小姨父", "未知亲戚"},
                {"侄子", "哥哥", "嫂子", "侄子", "侄子", "侄女", "侄女", "侄孙子", "侄孙女", "侄媳", "", "未知亲戚"},
                {"侄女", "哥哥", "嫂子", "侄子", "侄子", "侄女", "侄女", "外侄孙", "外侄孙女", "", "侄女婿", "未知亲戚"},
                {"嫂子", "姻伯父", "姻伯母", "姻兄", "姻弟", "姻姐", "姻妹", "侄子", "侄女", "", "哥哥", "未知亲戚"},
                {"弟妹", "姻叔父", "姻叔母", "姻兄", "姻弟", "姻姐", "姻妹", "侄子", "侄女", "", "弟弟", "未知亲戚"},
                {"外甥", "姐夫", "姐姐", "外甥", "外甥", "外甥女", "外甥女", "外甥孙", "外甥孙女", "外甥媳妇", "", "未知亲戚"},
                {"外甥女", "姐夫", "姐姐", "外甥", "外甥", "外甥女", "外甥女", "外甥孙", "外甥孙女", "", "外甥女婿", "未知亲戚"},
                {"姐夫", "姻世伯", "姻伯母", "姻兄", "姻弟", "姻姐", "姻妹", "外甥", "外甥女", "姐姐", "", "未知亲戚"},
                {"妹夫", "姻世伯", "姻伯母", "姻兄", "姻弟", "姻姐", "姻妹", "外甥", "外甥女", "妹妹", "", "未知亲戚"},
                {"孙子", "儿子", "儿媳", "孙子", "孙子", "孙女", "孙女", "曾孙", "曾孙女", "孙媳", "", "未知亲戚"},
                {"孙女", "儿子", "儿媳", "孙子", "孙子", "孙女", "孙女", "曾外孙", "曾外孙女", "", "孙女婿", "未知亲戚"},
                {"儿媳", "亲家公", "亲家母", "姻侄", "姻侄", "姻侄女", "姻侄女", "孙子", "孙女", "", "儿子", "未知亲戚"},
                {"外孙女", "女婿", "女儿", "外孙", "外孙", "外孙女", "外孙女", "外曾外孙", "外曾外孙女", "", "外孙女婿", "未知亲戚"},
                {"外孙", "女婿", "女儿", "外孙", "外孙", "外孙女", "外孙女", "外曾孙", "外曾孙女", "外孙媳", "", "未知亲戚"},
                {"女婿", "亲家公", "亲家母", "姻侄", "姻侄", "姻侄女", "姻侄女", "外孙", "外孙女", "女儿", "", "未知亲戚"},
                {"岳父", "太岳父", "太岳母", "伯岳", "叔岳", "姑岳母", "姑岳母", "大舅子", "大姨子", "岳母", "", "未知亲戚"},
                {"岳母", "外太岳父", "外太岳母", "舅岳父", "舅岳父", "姨岳母", "姨岳母", "大舅子", "大姨子", "", "岳父", "未知亲戚"},
                {"大舅子", "岳父", "岳母", "大舅子", "小舅子", "大姨子", "妻子", "内侄", "内侄女", "舅嫂", "", "未知亲戚"},
                {"小舅子", "岳父", "岳母", "大舅子", "小舅子", "妻子", "小姨子", "内侄", "内侄女", "舅弟媳", "", "未知亲戚"},
                {"大姨子", "岳父", "岳母", "大舅子", "小舅子", "大姨子", "妻子", "内甥", "姨甥女", "", "大姨夫", "未知亲戚"},
                {"小姨子", "岳父", "岳母", "大舅子", "小舅子", "妻子", "小姨子", "内甥", "姨甥女", "", "小姨夫", "未知亲戚"},
                {"曾祖父", "高祖父", "高祖母", "曾伯祖父", "曾叔祖父", "增祖姑母", "增祖姑母", "爷爷", "祖姑母", "曾祖母", "", "未知亲戚"},
                {"曾祖母", "高外祖父", "高外祖母", "舅曾祖父", "舅曾祖父", "姨曾祖母", "姨曾祖母", "爷爷", "祖姑母", "", "曾祖父", "未知亲戚"},
                {"伯祖父", "曾祖父", "曾祖母", "伯祖父", "爷爷", "祖姑母", "祖姑母", "堂伯", "堂姑", "伯祖母", "", "未知亲戚"},
                {"叔祖父", "曾祖父", "曾祖母", "爷爷", "叔祖父", "祖姑母", "祖姑母", "堂伯", "堂姑", "叔祖母", "", "未知亲戚"},
                {"祖姑母", "曾祖父", "曾祖母", "伯祖父", "爷爷", "祖姑母", "祖姑母", "姑表伯父", "姑表姑母", "", "祖姑父", "未知亲戚"},
                {"曾外祖父", "祖太爷", "祖太太", "伯曾外祖父", "叔曾外祖父", "姑曾外祖母", "姑曾外祖母", "舅公", "奶奶", "曾外祖母", "", "未知亲戚"},
                {"曾外祖母", "祖太姥爷", "祖太姥姥", "舅曾外祖父", "舅曾外祖父", "姨曾外祖母", "姨曾外祖母", "舅公", "奶奶", "", "曾外祖父", "未知亲戚"},
                {"舅公", "曾外祖父", "曾外祖母", "舅公", "舅公", "祖姨母", "奶奶", "舅表伯父", "舅表姑母", "舅婆", "", "未知亲戚"},
                {"祖姨母", "曾外祖父", "曾外祖母", "舅公", "舅公", "祖姨母", "奶奶", "姨表伯父", "姨表姑母", "", "祖姨夫", "未知亲戚"},
                {"堂哥", "伯父", "伯母", "堂哥", "堂弟", "堂姐", "堂妹", "堂侄", "堂侄女", "堂嫂", "", "未知亲戚"},
                {"堂弟", "叔叔", "婶婶", "堂哥", "堂弟", "堂姐", "堂妹", "堂侄", "堂侄女", "堂弟媳", "", "未知亲戚"},
                {"堂姐", "伯父", "伯母", "堂哥", "堂弟", "堂姐", "堂妹", "堂外甥", "堂外甥女", "", "堂姐夫", "未知亲戚"},
                {"堂妹", "叔叔", "婶婶", "堂哥", "堂弟", "堂姐", "堂妹", "堂外甥", "堂外甥女", "", "堂妹夫", "未知亲戚"},
                {"伯母", "姻伯公", "姻伯婆", "姻世伯", "姻世伯", "姻伯母", "姻伯母", "堂哥", "堂姐", "", "伯父", "未知亲戚"},
                {"婶婶", "姻伯公", "姻伯婆", "姻世伯", "姻世伯", "姻伯母", "姻伯母", "堂弟", "堂妹", "", "叔叔", "未知亲戚"},

                {"姑表哥", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑表姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑丈", "", "", "", "", "", "", "", "", "", "", ""},

                {"外曾祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑外祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"外曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾外祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"外舅公", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨外祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"舅表哥", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"大舅妈", "", "", "", "", "", "", "", "", "", "", ""},
                {"小舅妈", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅妈", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表弟", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表妹", "", "", "", "", "", "", "", "", "", "", ""},

                {"姨表哥", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表弟", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表妹", "", "", "", "", "", "", "", "", "", "", ""},
                {"大姨父", "", "", "", "", "", "", "", "", "", "", ""},
                {"小姨父", "", "", "", "", "", "", "", "", "", "", ""},

                {"侄孙子", "", "", "", "", "", "", "", "", "", "", ""},
                {"外侄孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"侄孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外侄孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"侄媳", "", "", "", "", "", "", "", "", "", "", ""},
                {"侄女婿", "", "", "", "", "", "", "", "", "", "", ""},

                {"姻伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻叔父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻伯母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻叔母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻兄", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻弟", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻妹", "", "", "", "", "", "", "", "", "", "", ""},

                {"外甥孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外甥孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"外甥媳妇", "", "", "", "", "", "", "", "", "", "", ""},
                {"外甥女婿", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻世伯", "", "", "", "", "", "", "", "", "", "", ""},

                {"曾孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾外孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾外孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"孙媳", "", "", "", "", "", "", "", "", "", "", ""},
                {"孙女婿", "", "", "", "", "", "", "", "", "", "", ""},

                {"亲家公", "", "", "", "", "", "", "", "", "", "", ""},
                {"亲家母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻侄", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻侄女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾外孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾外孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外孙女婿", "", "", "", "", "", "", "", "", "", "", ""},
                {"外孙媳", "", "", "", "", "", "", "", "", "", "", ""},

                {"太岳父", "", "", "", "", "", "", "", "", "", "", ""},
                {"外太岳父", "", "", "", "", "", "", "", "", "", "", ""},
                {"太岳母", "", "", "", "", "", "", "", "", "", "", ""},
                {"外太岳母", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯岳", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔岳", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅岳父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑岳母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨岳母", "", "", "", "", "", "", "", "", "", "", ""},

                {"内侄", "", "", "", "", "", "", "", "", "", "", ""},
                {"内侄女", "", "", "", "", "", "", "", "", "", "", ""},
                {"内甥", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨甥女", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅嫂", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅弟媳", "", "", "", "", "", "", "", "", "", "", ""},
                {"大姨夫", "", "", "", "", "", "", "", "", "", "", ""},
                {"小姨夫", "", "", "", "", "", "", "", "", "", "", ""},

                {"高祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"高祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"高外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"高外祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾伯祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾叔祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅曾祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"增祖姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨曾祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"堂伯", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂姑", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑表伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑表姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖姑父", "", "", "", "", "", "", "", "", "", "", ""},

                {"祖太爷", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖太太", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖太姥爷", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖太姥姥", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑曾外祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨曾外祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"舅表伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅婆", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖姨夫", "", "", "", "", "", "", "", "", "", "", ""},

                {"堂侄", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂侄女", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂嫂", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂弟媳", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂外甥", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂外甥女", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂姐夫", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂妹夫", "", "", "", "", "", "", "", "", "", "", ""},

                {"姻伯公", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻伯婆", "", "", "", "", "", "", "", "", "", "", ""},

                {"未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚"}
        };
    }

    public String[][] getRelationShipDataByWoman() {
        return new String[][] {
                {"我", "爸爸", "妈妈", "哥哥", "弟弟", "姐姐", "妹妹", "儿子", "女儿", "妻子", "丈夫", "未知亲戚"},
                {"爸爸", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "弟弟", "我", "妈妈", "", "未知亲戚"},
                {"妈妈", "外公", "外婆", "大舅", "小舅", "大姨", "小姨", "弟弟", "我", "", "爸爸", "未知亲戚"},
                {"哥哥", "爸爸", "妈妈", "哥哥", "弟弟", "姐姐", "我", "侄子", "侄女", "嫂子", "", "未知亲戚"},
                {"弟弟", "爸爸", "妈妈", "哥哥", "弟弟", "我", "妹妹", "侄子", "侄女", "弟妹", "", "未知亲戚"},
                {"姐姐", "爸爸", "妈妈", "哥哥", "弟弟", "姐姐", "我", "外甥", "外甥女", "", "姐夫", "未知亲戚"},
                {"妹妹", "爸爸", "妈妈", "哥哥", "弟弟", "我", "妹妹", "外甥", "外甥女", "", "妹夫", "未知亲戚"},
                {"儿子", "丈夫", "我", "儿子", "儿子", "女儿", "女儿", "孙子", "孙女", "儿媳", "", "未知亲戚"},
                {"女儿", "丈夫", "我", "儿子", "儿子", "女儿", "女儿", "外孙", "外孙女", "", "女婿", "未知亲戚"},
                {"妻子", "", "", "", "", "", "", "", "", "", "", "未知亲戚"},
                {"丈夫", "公公", "婆婆", "大伯子", "小叔子", "大姑子", "小姑子", "儿子", "女儿", "我", "", "未知亲戚"},
                {"爷爷", "曾祖父", "曾祖母", "伯祖父", "叔祖父", "祖姑母", "祖姑母", "爸爸", "姑妈", "奶奶", "", "未知亲戚"},
                {"奶奶", "曾外祖父", "曾外祖母", "舅公", "舅公", "祖姨母", "祖姨母", "爸爸", "姑妈", "", "爷爷", "未知亲戚"},
                {"伯父", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "堂哥", "堂姐", "伯母", "", "未知亲戚"},
                {"叔叔", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "堂弟", "堂妹", "婶婶", "", "未知亲戚"},
                {"姑妈", "爷爷", "奶奶", "伯父", "叔叔", "姑妈", "姑妈", "姑表哥", "姑表姐", "", "姑丈", "未知亲戚"},
                {"外公", "外曾祖父", "外曾祖母", "伯外祖父", "叔外祖父", "姑外祖母", "姑外祖母", "舅舅", "妈妈", "外婆", "", "未知亲戚"},
                {"外婆", "外曾外祖父", "外曾外祖母", "外舅公", "外舅公", "姨外祖母", "姨外祖母", "舅舅", "妈妈", "", "外公", "未知亲戚"},
                {"大舅", "外公", "外婆", "大舅", "舅舅", "大姨", "妈妈", "舅表哥", "舅表姐", "大舅妈", "", "未知亲戚"},
                {"小舅", "外公", "外婆", "舅舅", "小舅", "妈妈", "小姨", "舅表弟", "舅表妹", "小舅妈", "", "未知亲戚"},
                {"舅舅", "外公", "外婆", "大舅", "小舅", "大姨", "小姨", "舅表哥", "舅表姐", "舅妈", "", "未知亲戚"},
                {"大姨", "外公", "外婆", "大舅", "舅舅", "大姨", "妈妈", "姨表哥", "姨表姐", "", "大姨父", "未知亲戚"},
                {"小姨", "外公", "外婆", "舅舅", "小舅", "妈妈", "小姨", "姨表弟", "姨表妹", "", "小姨父", "未知亲戚"},
                {"侄子", "哥哥", "嫂子", "侄子", "侄子", "侄女", "侄女", "侄孙子", "侄孙女", "侄媳", "", "未知亲戚"},
                {"侄女", "哥哥", "嫂子", "侄子", "侄子", "侄女", "侄女", "外侄孙", "外侄孙女", "", "侄女婿", "未知亲戚"},
                {"嫂子", "姻伯父", "姻伯母", "姻兄", "姻弟", "姻姐", "姻妹", "侄子", "侄女", "", "哥哥", "未知亲戚"},
                {"弟妹", "姻叔父", "姻叔母", "姻兄", "姻弟", "姻姐", "姻妹", "侄子", "侄女", "", "弟弟", "未知亲戚"},
                {"外甥", "姐夫", "姐姐", "外甥", "外甥", "外甥女", "外甥女", "外甥孙", "外甥孙女", "外甥媳妇", "", "未知亲戚"},
                {"外甥女", "姐夫", "姐姐", "外甥", "外甥", "外甥女", "外甥女", "外甥孙", "外甥孙女", "", "外甥女婿", "未知亲戚"},
                {"姐夫", "姻世伯", "姻伯母", "姻兄", "姻弟", "姻姐", "姻妹", "外甥", "外甥女", "姐姐", "", "未知亲戚"},
                {"妹夫", "姻世伯", "姻伯母", "姻兄", "姻弟", "姻姐", "姻妹", "外甥", "外甥女", "妹妹", "", "未知亲戚"},
                {"孙子", "儿子", "儿媳", "孙子", "孙子", "孙女", "孙女", "曾孙", "曾孙女", "孙媳", "", "未知亲戚"},
                {"孙女", "儿子", "儿媳", "孙子", "孙子", "孙女", "孙女", "曾外孙", "曾外孙女", "", "孙女婿", "未知亲戚"},
                {"儿媳", "亲家公", "亲家母", "姻侄", "姻侄", "姻侄女", "姻侄女", "孙子", "孙女", "", "儿子", "未知亲戚"},
                {"外孙女", "女婿", "女儿", "外孙", "外孙", "外孙女", "外孙女", "外曾外孙", "外曾外孙女", "", "外孙女婿", "未知亲戚"},
                {"外孙", "女婿", "女儿", "外孙", "外孙", "外孙女", "外孙女", "外曾孙", "外曾孙女", "外孙媳", "", "未知亲戚"},
                {"女婿", "亲家公", "亲家母", "姻侄", "姻侄", "姻侄女", "姻侄女", "外孙", "外孙女", "女儿", "", "未知亲戚"},
                {"公公", "祖翁", "祖婆", "伯翁", "叔公", "姑婆", "姑婆", "大伯子", "大姑子", "婆婆", "", "未知亲戚"},
                {"婆婆", "外公", "外婆", "舅公", "舅公", "姨婆", "姨婆", "大伯子", "大姑子", "", "公公", "未知亲戚"},
                {"大伯子", "公公", "婆婆", "大伯子", "丈夫", "大姑子", "小姑子", "婆家侄", "侄女", "大婶子", "", "未知亲戚"},
                {"小叔子", "公公", "婆婆", "丈夫", "小叔子", "大姑子", "小姑子", "婆家侄", "侄女", "小婶子", "", "未知亲戚"},
                {"大姑子", "公公", "婆婆", "大伯子", "丈夫", "大姑子", "小姑子", "婆家甥", "外甥女", "", "大姑夫", "未知亲戚"},
                {"小姑子", "公公", "婆婆", "丈夫", "小叔子", "大姑子", "小姑子", "婆家甥", "外甥女", "", "小姑夫", "未知亲戚"},
                {"曾祖父", "高祖父", "高祖母", "曾伯祖父", "曾叔祖父", "增祖姑母", "增祖姑母", "爷爷", "祖姑母", "曾祖母", "", "未知亲戚"},
                {"曾祖母", "高外祖父", "高外祖母", "舅曾祖父", "舅曾祖父", "姨曾祖母", "姨曾祖母", "爷爷", "祖姑母", "", "曾祖父", "未知亲戚"},
                {"伯祖父", "曾祖父", "曾祖母", "伯祖父", "爷爷", "祖姑母", "祖姑母", "堂伯", "堂姑", "伯祖母", "", "未知亲戚"},
                {"叔祖父", "曾祖父", "曾祖母", "爷爷", "叔祖父", "祖姑母", "祖姑母", "堂伯", "堂姑", "叔祖母", "", "未知亲戚"},
                {"祖姑母", "曾祖父", "曾祖母", "伯祖父", "爷爷", "祖姑母", "祖姑母", "姑表伯父", "姑表姑母", "", "祖姑父", "未知亲戚"},
                {"曾外祖父", "祖太爷", "祖太太", "伯曾外祖父", "叔曾外祖父", "姑曾外祖母", "姑曾外祖母", "舅公", "奶奶", "曾外祖母", "", "未知亲戚"},
                {"曾外祖母", "祖太姥爷", "祖太姥姥", "舅曾外祖父", "舅曾外祖父", "姨曾外祖母", "姨曾外祖母", "舅公", "奶奶", "", "曾外祖父", "未知亲戚"},
                {"舅公", "曾外祖父", "曾外祖母", "舅公", "舅公", "祖姨母", "奶奶", "舅表伯父", "舅表姑母", "舅婆", "", "未知亲戚"},
                {"祖姨母", "曾外祖父", "曾外祖母", "舅公", "舅公", "祖姨母", "奶奶", "姨表伯父", "姨表姑母", "", "祖姨夫", "未知亲戚"},
                {"堂哥", "伯父", "伯母", "堂哥", "堂弟", "堂姐", "堂妹", "堂侄", "堂侄女", "堂嫂", "", "未知亲戚"},
                {"堂弟", "叔叔", "婶婶", "堂哥", "堂弟", "堂姐", "堂妹", "堂侄", "堂侄女", "堂弟媳", "", "未知亲戚"},
                {"堂姐", "伯父", "伯母", "堂哥", "堂弟", "堂姐", "堂妹", "堂外甥", "堂外甥女", "", "堂姐夫", "未知亲戚"},
                {"堂妹", "叔叔", "婶婶", "堂哥", "堂弟", "堂姐", "堂妹", "堂外甥", "堂外甥女", "", "堂妹夫", "未知亲戚"},
                {"伯母", "姻伯公", "姻伯婆", "姻世伯", "姻世伯", "姻伯母", "姻伯母", "堂哥", "堂姐", "", "伯父", "未知亲戚"},
                {"婶婶", "姻伯公", "姻伯婆", "姻世伯", "姻世伯", "姻伯母", "姻伯母", "堂弟", "堂妹", "", "叔叔", "未知亲戚"},

                {"姑表哥", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑表姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑丈", "", "", "", "", "", "", "", "", "", "", ""},

                {"外曾祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑外祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"外曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾外祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"外舅公", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨外祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"舅表哥", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"大舅妈", "", "", "", "", "", "", "", "", "", "", ""},
                {"小舅妈", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅妈", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表弟", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表妹", "", "", "", "", "", "", "", "", "", "", ""},

                {"姨表哥", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表弟", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表妹", "", "", "", "", "", "", "", "", "", "", ""},
                {"大姨父", "", "", "", "", "", "", "", "", "", "", ""},
                {"小姨父", "", "", "", "", "", "", "", "", "", "", ""},

                {"侄孙子", "", "", "", "", "", "", "", "", "", "", ""},
                {"外侄孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"侄孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外侄孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"侄媳", "", "", "", "", "", "", "", "", "", "", ""},
                {"侄女婿", "", "", "", "", "", "", "", "", "", "", ""},

                {"姻伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻叔父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻伯母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻叔母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻兄", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻弟", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻姐", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻妹", "", "", "", "", "", "", "", "", "", "", ""},

                {"外甥孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外甥孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"外甥媳妇", "", "", "", "", "", "", "", "", "", "", ""},
                {"外甥女婿", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻世伯", "", "", "", "", "", "", "", "", "", "", ""},

                {"曾孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾外孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾外孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"孙媳", "", "", "", "", "", "", "", "", "", "", ""},
                {"孙女婿", "", "", "", "", "", "", "", "", "", "", ""},

                {"亲家公", "", "", "", "", "", "", "", "", "", "", ""},
                {"亲家母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻侄", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻侄女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾外孙", "", "", "", "", "", "", "", "", "", "", ""},
                {"外曾外孙女", "", "", "", "", "", "", "", "", "", "", ""},
                {"外孙女婿", "", "", "", "", "", "", "", "", "", "", ""},
                {"外孙媳", "", "", "", "", "", "", "", "", "", "", ""},

                {"祖翁", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖婆", "", "", "", "", "", "", "", "", "", "", ""},
                {"外婆", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯翁", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔公", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅公", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑婆", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨婆", "", "", "", "", "", "", "", "", "", "", ""},

                {"婆家侄", "", "", "", "", "", "", "", "", "", "", ""},
                {"侄女", "", "", "", "", "", "", "", "", "", "", ""},
                {"婆家甥", "", "", "", "", "", "", "", "", "", "", ""},
                {"外甥女", "", "", "", "", "", "", "", "", "", "", ""},
                {"大婶子", "", "", "", "", "", "", "", "", "", "", ""},
                {"小婶子", "", "", "", "", "", "", "", "", "", "", ""},
                {"大姑夫", "", "", "", "", "", "", "", "", "", "", ""},
                {"小姑夫", "", "", "", "", "", "", "", "", "", "", ""},

                {"高祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"高祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"高外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"高外祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾伯祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"曾叔祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅曾祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"增祖姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨曾祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"堂伯", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂姑", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑表伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑表姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖姑父", "", "", "", "", "", "", "", "", "", "", ""},

                {"祖太爷", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖太太", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖太姥爷", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖太姥姥", "", "", "", "", "", "", "", "", "", "", ""},
                {"伯曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"叔曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅曾外祖父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姑曾外祖母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨曾外祖母", "", "", "", "", "", "", "", "", "", "", ""},

                {"舅表伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表伯父", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅表姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"姨表姑母", "", "", "", "", "", "", "", "", "", "", ""},
                {"舅婆", "", "", "", "", "", "", "", "", "", "", ""},
                {"祖姨夫", "", "", "", "", "", "", "", "", "", "", ""},

                {"堂侄", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂侄女", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂嫂", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂弟媳", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂外甥", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂外甥女", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂姐夫", "", "", "", "", "", "", "", "", "", "", ""},
                {"堂妹夫", "", "", "", "", "", "", "", "", "", "", ""},

                {"姻伯公", "", "", "", "", "", "", "", "", "", "", ""},
                {"姻伯婆", "", "", "", "", "", "", "", "", "", "", ""},

                {"未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚", "未知亲戚"}
        };
    }
}



package com.yangdai.calc.main.toolbox.functions.programmer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;
import com.yangdai.calc.utils.TouchAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ProgrammerActivity extends BaseFunctionActivity implements View.OnClickListener {

    //声明所有组件
    private TextView etText;
    private TextView et2, et8, et10, et16;
    private Button bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9, bt0, bta, btb, btc, btd, bte, btf;
    private int select = 16;
    private int digit = 5;
    private static final int[] BUTTONS = new int[]{R.id.bt_0, R.id.bt_1, R.id.bt_2, R.id.bt_3, R.id.bt_4, R.id.bt_5, R.id.bt_6, R.id.bt_7, R.id.bt_8, R.id.bt_9, R.id.bt_del, R.id.clean,
            R.id.bt_point, R.id.bt_a, R.id.bt_b, R.id.bt_c, R.id.bt_d, R.id.bt_e, R.id.bt_f};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Spinner spScale = findViewById(R.id.spinner_scale);
        String[] scaleList = {getString(R.string.hex), getString(R.string.decimal), getString(R.string.octal), getString(R.string.binary)};
        List<String> allItems = new ArrayList<>(Arrays.asList(scaleList));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, allItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spScale.setAdapter(adapter);
        spScale.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                etText.setText("");
                switch (i) {
                    case 0 -> {
                        select = 16;
                        bt0.setEnabled(true);
                        bt1.setEnabled(true);
                        bt2.setEnabled(true);
                        bt3.setEnabled(true);
                        bt4.setEnabled(true);
                        bt5.setEnabled(true);
                        bt6.setEnabled(true);
                        bt7.setEnabled(true);
                        bt8.setEnabled(true);
                        bt9.setEnabled(true);
                        bta.setEnabled(true);
                        btb.setEnabled(true);
                        btc.setEnabled(true);
                        btd.setEnabled(true);
                        bte.setEnabled(true);
                        btf.setEnabled(true);
                    }
                    case 1 -> {
                        select = 10;
                        bt0.setEnabled(true);
                        bt1.setEnabled(true);
                        bt2.setEnabled(true);
                        bt3.setEnabled(true);
                        bt4.setEnabled(true);
                        bt5.setEnabled(true);
                        bt6.setEnabled(true);
                        bt7.setEnabled(true);
                        bt8.setEnabled(true);
                        bt9.setEnabled(true);
                        bta.setEnabled(false);
                        btb.setEnabled(false);
                        btc.setEnabled(false);
                        btd.setEnabled(false);
                        bte.setEnabled(false);
                        btf.setEnabled(false);
                    }
                    case 2 -> {
                        select = 8;
                        bt0.setEnabled(true);
                        bt1.setEnabled(true);
                        bt2.setEnabled(true);
                        bt3.setEnabled(true);
                        bt4.setEnabled(true);
                        bt5.setEnabled(true);
                        bt6.setEnabled(true);
                        bt7.setEnabled(true);
                        bt8.setEnabled(false);
                        bt9.setEnabled(false);
                        bta.setEnabled(false);
                        btb.setEnabled(false);
                        btc.setEnabled(false);
                        btd.setEnabled(false);
                        bte.setEnabled(false);
                        btf.setEnabled(false);
                    }
                    case 3 -> {
                        select = 2;
                        bt0.setEnabled(true);
                        bt1.setEnabled(true);
                        bt2.setEnabled(false);
                        bt3.setEnabled(false);
                        bt4.setEnabled(false);
                        bt5.setEnabled(false);
                        bt6.setEnabled(false);
                        bt7.setEnabled(false);
                        bt8.setEnabled(false);
                        bt9.setEnabled(false);
                        bta.setEnabled(false);
                        btb.setEnabled(false);
                        btc.setEnabled(false);
                        btd.setEnabled(false);
                        bte.setEnabled(false);
                        btf.setEnabled(false);
                    }
                    default -> {
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        etText = findViewById(R.id.et_text);
        et2 = findViewById(R.id.et_2);
        et8 = findViewById(R.id.et_8);
        et10 = findViewById(R.id.et_10);
        et16 = findViewById(R.id.et_16);
        bt1 = findViewById(R.id.bt_1);
        bt2 = findViewById(R.id.bt_2);
        bt3 = findViewById(R.id.bt_3);
        bt4 = findViewById(R.id.bt_4);
        bt5 = findViewById(R.id.bt_5);
        bt6 = findViewById(R.id.bt_6);
        bt7 = findViewById(R.id.bt_7);
        bt8 = findViewById(R.id.bt_8);
        bt9 = findViewById(R.id.bt_9);
        bt0 = findViewById(R.id.bt_0);
        bta = findViewById(R.id.bt_a);
        btb = findViewById(R.id.bt_b);
        btc = findViewById(R.id.bt_c);
        btd = findViewById(R.id.bt_d);
        bte = findViewById(R.id.bt_e);
        btf = findViewById(R.id.bt_f);

        et2.setOnClickListener(this);
        et8.setOnClickListener(this);
        et10.setOnClickListener(this);
        et16.setOnClickListener(this);

        for (int buttonId : BUTTONS) {
            findViewById(buttonId).setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
            findViewById(buttonId).setOnClickListener(this);
            TouchAnimation touchAnimation = new TouchAnimation(findViewById(buttonId));
            findViewById(buttonId).setOnTouchListener(touchAnimation);
        }

        //添加文本框变化监听
        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                initRadix(input);
            }
        });

        //OnCreate 方法执行时，将shared preference里面的设置赋值给digit
        digit = defaultSp.getInt("scale", 5);
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_programmer);
    }

    //判断选择的进制
    private void initRadix(String input) {

        //若空则返回
        if (TextUtils.isEmpty(input)) {
            et2.setText("");
            et8.setText("");
            et10.setText("");
            et16.setText("");
            return;
        }

        //若最后一位为"."，则返回
        if (etText.getText().toString().substring(etText.getText().toString().length() - 1).equals("."))
            return;

        //判断选择的RadioButton
        try {
            if (select == 2) {
                et2.setText(sendString(input, 2));
                et8.setText(sendString(input, 8));
                et10.setText(sendString(input, 10));
                et16.setText(sendString(input, 16));
            } else if (select == 8) {
                et2.setText(sendString(input, 2));
                et8.setText(sendString(input, 8));
                et10.setText(sendString(input, 10));
                et16.setText(sendString(input, 16));
            } else if (select == 10) {
                et2.setText(sendString(input, 2));
                et8.setText(sendString(input, 8));
                et10.setText(sendString(input, 10));
                et16.setText(sendString(input, 16));
            } else if (select == 16) {
                et2.setText(sendString(input, 2));
                et8.setText(sendString(input, 8));
                et10.setText(sendString(input, 10));
                et16.setText(sendString(input, 16));
            }
        } catch (Exception e) {
            Snackbar.make(etText, R.string.formatError, Snackbar.LENGTH_SHORT).show();
        }
    }

    //向进制转换工具类传值，分别是：需要转换的字符串，需要转换到的进制，当前进制，该方法会返回一个结果值
    private String sendString(String input, int toRadix) {
        String result;
        String[] array;
        array = input.split("\\.");
        int fromRadix = select;
        //if input data only contains integer
        if (!input.contains(".") || array.length == 1) {
            result = RadixUtil.integerConverter(array[0], toRadix, fromRadix);
        } else {
            result = RadixUtil.integerConverter(array[0], toRadix, fromRadix)
                    + "." + RadixUtil.decimalsConverter(array[1], toRadix, fromRadix, digit - 1);
        }
        return result;
    }

    //文本框和按钮设置监听
    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_point) {
            etText.setText(etText.getText() + ".");
        } else if (view.getId() == R.id.bt_del) {
            if (!TextUtils.isEmpty(etText.getText().toString())) {
                String str = etText.getText().toString();
                etText.setText(str.substring(0, str.length() - 1));
            }
        } else if (view.getId() == R.id.bt_0) {
            etText.setText(etText.getText() + "0");
        } else if (view.getId() == R.id.bt_1) {
            etText.setText(etText.getText() + "1");
        } else if (view.getId() == R.id.bt_2) {
            etText.setText(etText.getText() + "2");
        } else if (view.getId() == R.id.bt_3) {
            etText.setText(etText.getText() + "3");
        } else if (view.getId() == R.id.bt_4) {
            etText.setText(etText.getText() + "4");
        } else if (view.getId() == R.id.bt_5) {
            etText.setText(etText.getText() + "5");
        } else if (view.getId() == R.id.bt_6) {
            etText.setText(etText.getText() + "6");
        } else if (view.getId() == R.id.bt_7) {
            etText.setText(etText.getText() + "7");
        } else if (view.getId() == R.id.bt_8) {
            etText.setText(etText.getText() + "8");
        } else if (view.getId() == R.id.bt_9) {
            etText.setText(etText.getText() + "9");
        } else if (view.getId() == R.id.bt_a) {
            etText.setText(etText.getText() + "A");
        } else if (view.getId() == R.id.bt_b) {
            etText.setText(etText.getText() + "B");
        } else if (view.getId() == R.id.bt_c) {
            etText.setText(etText.getText() + "C");
        } else if (view.getId() == R.id.bt_d) {
            etText.setText(etText.getText() + "D");
        } else if (view.getId() == R.id.bt_e) {
            etText.setText(etText.getText() + "E");
        } else if (view.getId() == R.id.bt_f) {
            etText.setText(etText.getText() + "F");
        } else if (view.getId() == R.id.clean) {
            etText.setText("");
        }
    }

}


package com.yangdai.calc.main.toolbox.functions.programmer;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.BigInteger;


public class RadixUtil {
    private static BigInteger mIntegerOfRadix;
    private static final String DIGITS = "0123456789ABCDEF";

    public static String integerConvertTo10(BigInteger data, int fromRadix) {
        StringBuilder result = new StringBuilder();
        mIntegerOfRadix = new BigInteger(String.valueOf(fromRadix));
        while (!data.toString().equals("0")) {
            result.insert(0, DIGITS.charAt(data.remainder(mIntegerOfRadix).intValue()));
            data = data.divide(mIntegerOfRadix);
        }
        if (TextUtils.isEmpty(result.toString())) {
            result = new StringBuilder("0");
        }
        return result.toString();
    }


    public static String integerConverter(String data, int toRadix, int fromRadix) {
        if (fromRadix == toRadix) {
            return data;
        }
        char[] chars = data.toCharArray();
        int len = chars.length;
        if (toRadix != 10) {
            data = integerConverter(data, 10, fromRadix);
            return integerConvertTo10(new BigInteger(data), toRadix);
        } else {
            BigInteger mBigData = new BigInteger("0");
            for (int i = len - 1; i >= 0; i--) {
                mIntegerOfRadix = BigDecimal.valueOf(DIGITS.indexOf(chars[i]) * Math.pow(fromRadix, len - i - 1)).toBigInteger();
                mBigData = mBigData.add(mIntegerOfRadix);
            }
            return mBigData.toString();
        }
    }

    public static String decimalsConverter(String data, int toRadix, int formRadix, int digit) {

        if (toRadix == formRadix) {
            return data;
        }
        //if specified radix(toRadix) is not base 10, covert decimals in base 10
        if (formRadix != 10) {
            data = decimalsConvertTo10(data, formRadix);
            if (toRadix == 10) {
                return data;
            }
        }
        char[] chars = DIGITS.toCharArray();
        int integer;
        BigDecimal bigDecimal = new BigDecimal("0." + data);
        StringBuilder result = new StringBuilder();
        //covert decimals(base 10) in specified radix(toRadix)
        while (bigDecimal.compareTo(new BigDecimal("0")) != 0) {
            bigDecimal = bigDecimal.multiply(new BigDecimal(toRadix));
            integer = bigDecimal.intValue();
            result.append(chars[integer]);
            bigDecimal = new BigDecimal("0." + bigDecimal.toPlainString().split("\\.")[1]);
            //if length greater then 9(equal to 10) break
            if (result.length() > digit) {
                break;
            }
        }

        if (result.length() == 0) {
            result = new StringBuilder("0");
        }
        return result.toString();

    }

    public static String decimalsConvertTo10(String data, int fromRadix) {

        char[] chars = data.toCharArray();
        BigDecimal sum = new BigDecimal("0");
        for (int i = 0; i < data.length(); i++) {
            int index = DIGITS.indexOf(chars[i]);
            int power = -i - 1;
            sum = sum.add(new BigDecimal(index * Math.pow(fromRadix, power)));
        }
        String[] ss = sum.toString().split("\\.");
        try {
            return ss[1];
        } catch (Exception e) {
            return "0";
        }
    }

    public static boolean checkData(String data, int radix) {
        data = data.replaceAll(" ", "");
        String digits = ".0123456789ABCDEF";
        //can only contains one point
        if (data.split("\\.").length > 2) {
            return false;
        }
        for (int i = 0; i < data.length(); i++) {
            char digit = data.charAt(i);
            int index = digits.indexOf(digit);
            if (index == -1 || index > radix) {
                return false;
            }
        }
        return true;
    }

    /*
     * 	第二种方法模拟人工算术
     *  用来将正数或者负数十进制转换为其他进制数
     * */
    public static String secDecToBin(int num) {
        //定义字符串，用来存放计算出来的二进制数据
        StringBuilder sb = new StringBuilder();
        while (num != 0) {
            //向字符串中添加计算出来的二进制数
            sb.append(num & 1);
            //对num进行无符号位运算，类似于除2运算，具体的区别还需要读者自己查找
            num = num >>> 1;
        }
        //将字符串反序返回
        return sb.reverse().toString();
    }

    public static String secDecToOctal(int num) {
        StringBuilder sb = new StringBuilder();
        while (num != 0) {
            sb.append(num & 7);
            num = num >>> 3;
        }
        return sb.reverse().toString();
    }

    public static String secDecToHex(int num) {
        StringBuilder sb = new StringBuilder();
        while (num != 0) {
            sb.append(num & 15);
            num = num >>> 4;
        }
        return sb.reverse().toString();
    }
}


package com.yangdai.calc.main.toolbox.functions.finance;

import static com.yangdai.calc.utils.Utils.closeKeyboard;
import static com.yangdai.calc.utils.Utils.formatNumberFinance;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;
import com.yangdai.calc.databinding.FragmentLoanBinding;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author 30415
 */
public class LoanFragment extends Fragment implements TextWatcher {
    private TextInputEditText etLoanAmount, etInterestRate, etLoanPeriod;
    FragmentLoanBinding binding;

    public LoanFragment() {
    }

    public static LoanFragment newInstance() {
        return new LoanFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        etLoanPeriod.removeTextChangedListener(this);
        etLoanAmount.removeTextChangedListener(this);
        etInterestRate.removeTextChangedListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etLoanAmount = view.findViewById(R.id.etLoanAmount);
        etInterestRate = view.findViewById(R.id.etInterestRate);
        etLoanPeriod = view.findViewById(R.id.etLoanPeriod);
        etLoanPeriod.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                etLoanPeriod.clearFocus();
                return true;
            }
            return false;
        });
        etLoanAmount.addTextChangedListener(this);
        etInterestRate.addTextChangedListener(this);
        etLoanPeriod.addTextChangedListener(this);
    }

    private void calculateLoan() {
        double loanAmount = Double.parseDouble(Objects.requireNonNull(etLoanAmount.getText()).toString());
        double interestRate = Double.parseDouble(Objects.requireNonNull(etInterestRate.getText()).toString()) / 100;
        int loanPeriod = Integer.parseInt(Objects.requireNonNull(etLoanPeriod.getText()).toString());

        double monthlyInterestRate = interestRate / 12;

        double monthlyPayment = (loanAmount * monthlyInterestRate) /
                (1 - Math.pow(1 + monthlyInterestRate, -loanPeriod));
        double totalInterest = (monthlyPayment * loanPeriod) - loanAmount;
        double totalAmount = monthlyPayment * loanPeriod;

        // 将数据转换为float类型
        float interestValue = Float.parseFloat(String.valueOf(totalInterest));
        float totalAmountValue = Float.parseFloat(String.valueOf(totalAmount));
        // 创建饼状图数据项
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(interestValue, getString(R.string.total_interest)));
        entries.add(new PieEntry(totalAmountValue, getString(R.string.total_amount)));
        // 创建饼状图数据集
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        // 创建饼状图数据对象
        PieData data = new PieData(dataSet);
        data.setValueTextSize(26);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(formatNumberFinance(String.valueOf(value)));
            }
        });
        // 获取饼状图视图
        PieChart pieChart = binding.pieChart;
        pieChart.setVisibility(View.VISIBLE);
        // 设置数据
        pieChart.setData(data);
        pieChart.setCenterText(getString(R.string.monthly_payment) + " " + formatNumberFinance(String.valueOf(monthlyPayment)));
        pieChart.getDescription().setEnabled(false);
        // 获取图例，但是在数据设置给chart之前是不可获取的
        Legend legend = pieChart.getLegend();
        // 是否绘制图例
        legend.setEnabled(false);

        // 刷新图表
        pieChart.invalidate();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        try {
            calculateLoan();
        } catch (Exception ignored) {

        }
    }
}


package com.yangdai.calc.main.toolbox.functions.finance;

import static com.yangdai.calc.utils.Utils.closeKeyboard;
import static com.yangdai.calc.utils.Utils.formatNumberFinance;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;
import com.yangdai.calc.databinding.FragmentBankBinding;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author 30415
 */
public class BankFragment extends Fragment implements TextWatcher {
    FragmentBankBinding binding;
    private TextInputEditText editTextPrincipal, editTextInterestRate, editTextTime;
    int flag;

    public BankFragment() {
    }

    public static BankFragment newInstance() {
        return new BankFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        editTextPrincipal.removeTextChangedListener(this);
        editTextInterestRate.removeTextChangedListener(this);
        editTextTime.removeTextChangedListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBankBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editTextPrincipal = binding.editTextPrincipal;
        editTextInterestRate = binding.editTextInterestRate;
        editTextTime = binding.editTextTime;
        editTextTime.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                editTextTime.clearFocus();
                return true;
            }
            return false;
        });
        editTextTime.addTextChangedListener(this);
        editTextPrincipal.addTextChangedListener(this);
        editTextInterestRate.addTextChangedListener(this);

        Spinner spinner = binding.spinnerPeriod;

        String[] arr = new String[]{getString(R.string.monthly), getString(R.string.quarterly), getString(R.string.half)
                , getString(R.string.yearly), getString(R.string.end)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(com.google.android.material.R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                flag = i;
                try {
                    calculateInterest();
                } catch (Exception ignored) {

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void calculateInterest() {
        double principal = Double.parseDouble(Objects.requireNonNull(editTextPrincipal.getText()).toString());
        double interestRate = Double.parseDouble(Objects.requireNonNull(editTextInterestRate.getText()).toString());
        int time = Integer.parseInt(Objects.requireNonNull(editTextTime.getText()).toString());
        int compoundPeriods = 0;
        double interest = 0;
        switch (flag) {
            case 0 -> compoundPeriods = 12;
            case 1 -> compoundPeriods = 4;
            case 2 -> compoundPeriods = 2;
            case 3 -> compoundPeriods = 1;
            case 4 -> interest = principal * (interestRate / 100);
            default -> {
            }
        }
        if (flag != 4) {
            double ratePerPeriod = (interestRate / 100) / compoundPeriods;
            interest = principal * Math.pow(1 + ratePerPeriod, compoundPeriods * (time / 12.0)) - principal;
        }

        double total = interest + principal;

        // 将数据转换为float类型
        float interestValue = Float.parseFloat(String.valueOf(interest));
        float principalValue = Float.parseFloat(String.valueOf(principal));
        // 创建饼状图数据项
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(interestValue, getString(R.string.total_interest)));
        entries.add(new PieEntry(principalValue, getString(R.string.principal)));
        // 创建饼状图数据集
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        // 创建饼状图数据对象
        PieData data = new PieData(dataSet);
        data.setValueTextSize(26);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(formatNumberFinance(String.valueOf(value)));
            }
        });
        // 获取饼状图视图
        PieChart pieChart = binding.pieChart;
        pieChart.setVisibility(View.VISIBLE);
        // 设置数据
        pieChart.setData(data);
        pieChart.setCenterText(getString(R.string.settlement_amount) + " " + formatNumberFinance(String.valueOf(total)));
        pieChart.setCenterTextSize(22);
        pieChart.getDescription().setEnabled(false);
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);

        // 刷新图表
        pieChart.invalidate();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        try {
            calculateInterest();
        } catch (Exception ignored) {

        }
    }
}


package com.yangdai.calc.main.toolbox.functions.finance;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.annotation.SuppressLint;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;
import com.yangdai.calc.utils.Utils;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author 30415
 */
public class InvestmentFragment extends Fragment implements TextWatcher {
    private TextInputEditText investmentAmountEditText, settlementAmountEditText;
    private TextView profitTextView;
    private TextView profitMarginTextView;
    private TextView roiTextView;
    private Button btnStartDate;
    private Button btnEndDate;

    private Date chosenTimeStart = new Date();
    private Date chosenTimeEnd = new Date();

    public InvestmentFragment() {
    }

    public static InvestmentFragment newInstance() {
        return new InvestmentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_investment, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        investmentAmountEditText.removeTextChangedListener(this);
        settlementAmountEditText.removeTextChangedListener(this);
        btnStartDate.removeTextChangedListener(this);
        btnEndDate.removeTextChangedListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        investmentAmountEditText = view.findViewById(R.id.investmentAmountEditText);
        settlementAmountEditText = view.findViewById(R.id.settlementAmountEditText);
        settlementAmountEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                settlementAmountEditText.clearFocus();
                return true;
            }
            return false;
        });
        investmentAmountEditText.addTextChangedListener(this);
        settlementAmountEditText.addTextChangedListener(this);

        Calendar calendar = Calendar.getInstance();

        // Initialize the MaterialDatePicker for the start date
        MaterialDatePicker<Long> materialDatePickerStart = MaterialDatePicker.Builder.datePicker()
                .setTitleText("")
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(calendar.getTimeInMillis())
                .build();

        // Set the callback for when a start date is selected
        materialDatePickerStart.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            chosenTimeStart = selectedDate.getTime();
            updateTimeStart();
        });

        // Initialize the MaterialDatePicker for the end date
        MaterialDatePicker<Long> materialDatePickerEnd = MaterialDatePicker.Builder.datePicker()
                .setTitleText("")
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(calendar.getTimeInMillis())
                .build();

        // Set the callback for when an end date is selected
        materialDatePickerEnd.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            chosenTimeEnd = selectedDate.getTime();
            updateTimeEnd();
        });

        btnStartDate = view.findViewById(R.id.btn_start_date);
        btnEndDate = view.findViewById(R.id.btn_end_date);
        btnStartDate.addTextChangedListener(this);
        btnEndDate.addTextChangedListener(this);

        // Show the dialog when the start date button is clicked
        btnStartDate.setOnClickListener(v -> materialDatePickerStart.show(getParentFragmentManager(), "DATE_PICKER_START"));
        // Show the dialog when the end date button is clicked
        btnEndDate.setOnClickListener(v -> materialDatePickerEnd.show(getParentFragmentManager(), "DATE_PICKER_END"));

        updateTimeStart();
        updateTimeEnd();
        profitTextView = view.findViewById(R.id.profitTextView);
        profitMarginTextView = view.findViewById(R.id.profitMarginTextView);
        roiTextView = view.findViewById(R.id.roiTextView);

        view.findViewById(R.id.info).setOnClickListener(v -> Snackbar
                .make(view, getString(R.string.tip), Snackbar.LENGTH_SHORT)
                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE).show());
    }

    @SuppressLint("SetTextI18n")
    private void calculateRoi() {
        double investmentAmount = Double.parseDouble(Objects.requireNonNull(investmentAmountEditText.getText()).toString());
        double settlementAmount = Double.parseDouble(Objects.requireNonNull(settlementAmountEditText.getText()).toString());

        Date chosenTimeStartS;
        Date chosenTimeEndS;

        if (chosenTimeStart.compareTo(chosenTimeEnd) == 0) {
            return;
        }
        if (chosenTimeStart.after(chosenTimeEnd)) {
            chosenTimeStartS = chosenTimeEnd;
            chosenTimeEndS = chosenTimeStart;
        } else {
            chosenTimeStartS = chosenTimeStart;
            chosenTimeEndS = chosenTimeEnd;
        }
        try {
            long durationInMillis = chosenTimeEndS.getTime() - chosenTimeStartS.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(durationInMillis);
            double profit = settlementAmount - investmentAmount;
            double profitMargin = (profit / investmentAmount) * 100;
            double roi = (Math.pow(settlementAmount / investmentAmount, 365.0 / days) - 1) * 100;
            //double roi = profit / investmentAmount / days * 365;

            profitTextView.setText(getString(R.string.profit) + " " + Utils.formatNumberFinance(String.valueOf(profit)));
            profitMarginTextView.setText(getString(R.string.profitMargin) + " " + String.format(Locale.getDefault(), "%.2f%%", profitMargin));
            roiTextView.setText(getString(R.string.annualized_rate_of_return) + " " + String.format(Locale.getDefault(), "%.2f%%", roi));
        } catch (Exception ignored) {
        }
    }

    private void updateTimeStart() {
        btnStartDate.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(chosenTimeStart));
    }

    private void updateTimeEnd() {
        btnEndDate.setText(DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()).format(chosenTimeEnd));
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        try {
            calculateRoi();
        } catch (Exception ignored) {

        }
    }
}


package com.yangdai.calc.main.toolbox.functions.finance;

import static com.yangdai.calc.utils.Utils.closeKeyboard;
import static com.yangdai.calc.utils.Utils.formatNumberFinance;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;
import com.yangdai.calc.databinding.FragmentVatBinding;

import java.util.ArrayList;
import java.util.Objects;

/**
 * @author 30415
 */
public class VatFragment extends Fragment implements TextWatcher {
    private TextInputEditText editTextAmount, editTextVatRate;
    private CheckBox checkBoxVatIncluded;
    FragmentVatBinding binding;

    public VatFragment() {
    }

    public static VatFragment newInstance() {
        return new VatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        editTextAmount.removeTextChangedListener(this);
        editTextVatRate.removeTextChangedListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextVatRate = view.findViewById(R.id.editTextVatRate);
        editTextAmount.addTextChangedListener(this);
        editTextVatRate.addTextChangedListener(this);
        editTextVatRate.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                editTextVatRate.clearFocus();
                return true;
            }
            return false;
        });
        checkBoxVatIncluded = view.findViewById(R.id.checkBoxVatIncluded);
        checkBoxVatIncluded.setOnCheckedChangeListener((compoundButton, b) -> calculateVat());
    }

    @SuppressLint("SetTextI18n")
    private void calculateVat() {
        String amountStr = Objects.requireNonNull(editTextAmount.getText()).toString();
        String vatRateStr = Objects.requireNonNull(editTextVatRate.getText()).toString();

        if (amountStr.isEmpty() || vatRateStr.isEmpty()) {
            return;
        }
        try {
            double amount = Double.parseDouble(editTextAmount.getText().toString());
            double vatRate = Double.parseDouble(editTextVatRate.getText().toString());
            boolean vatIncluded = checkBoxVatIncluded.isChecked();

            double vatAmount;
            double totalAmount;
            double vatDeductedAmount;
            if (vatIncluded) {
                vatDeductedAmount = amount / (1 + vatRate) * vatRate;
                totalAmount = amount;
                vatAmount = amount - vatDeductedAmount;
            } else {
                vatAmount = amount * vatRate / 100;
                totalAmount = amount + vatAmount;
                vatDeductedAmount = amount;
            }

            // 将数据转换为float类型
            float vatValue = (float) vatAmount;
            float vatDeductedValue = (float) vatDeductedAmount;
            // 创建饼状图数据项
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(vatValue, getString(R.string.vat)));
            entries.add(new PieEntry(vatDeductedValue, getString(R.string.taxExcludedAmount)));
            // 创建饼状图数据集
            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            // 创建饼状图数据对象
            PieData data = new PieData(dataSet);
            data.setValueTextSize(26);
            data.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.valueOf(formatNumberFinance(String.valueOf(value)));
                }
            });
            // 获取饼状图视图
            PieChart pieChart = binding.pieChart;
            pieChart.setVisibility(View.VISIBLE);
            // 设置数据
            pieChart.setData(data);
            pieChart.setCenterText(getString(R.string.taxIncludedAmount) + " " + formatNumberFinance(String.valueOf(totalAmount)));
            pieChart.getDescription().setEnabled(false);
            Legend legend = pieChart.getLegend();
            legend.setEnabled(false);
            // 刷新图表
            pieChart.invalidate();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), getString(R.string.formatError), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        calculateVat();
    }
}


package com.yangdai.calc.main.toolbox.functions.finance;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;


/**
 * @author 30415
 */
public class FinanceActivity extends BaseFunctionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_finance);
    }

    private void initView() {
        ViewPager2 mViewPager = findViewById(R.id.view_pager_main);
        TabLayout mTabLayout = findViewById(R.id.tab_view);

        final String[] tabs = new String[]{getString(R.string.bankFragment), getString(R.string.roiFragment), getString(R.string.loanFragment), getString(R.string.vat)};
        final int[] icons = new int[]{R.drawable.bank_icon, R.drawable.invest_icon, R.drawable.loan_icon, R.drawable.tax_icon};

        mViewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return BankFragment.newInstance();
                } else if (position == 1) {
                    return InvestmentFragment.newInstance();
                } else if (position == 2) {
                    return LoanFragment.newInstance();
                } else {
                    return VatFragment.newInstance();
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    tab.setText(tabs[position]);
                    tab.setIcon(icons[position]);
                }).attach();
    }
}

package com.yangdai.calc.main.toolbox.functions.chinese;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;
import com.yangdai.calc.utils.TouchAnimation;
import com.yangdai.calc.utils.Utils;

/**
 * @author 30415
 */
public class ChineseNumberConversionActivity extends BaseFunctionActivity implements View.OnClickListener {

    private static final String[] CN_UPPER_NUMBER = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] CN_UPPER_MONETARY_UNIT = {"分", "角", "元", "拾", "佰", "仟",
            "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟"};
    private static final String CN_FULL = "整";
    private static final String CN_NEGATIVE = "负";
    private static final String CN_ZERO_FULL = "零元";
    private static final int MONEY_PRECISION = 2;
    private TextView tvInput;
    private TextView tvResults;

    private String showText = "0";
    private String resultsText = "零元整";
    private static final int[] BUTTON_IDS = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4, R.id.btn_5,
            R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_pt, R.id.btn_clr, R.id.btn_negate, R.id.iv_del};


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tvInput = findViewById(R.id.input_textview);
        tvResults = findViewById(R.id.output_textview);

        for (int buttonId : BUTTON_IDS) {
            findViewById(buttonId).setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
            findViewById(buttonId).setOnClickListener(this);
            TouchAnimation touchAnimation = new TouchAnimation(findViewById(buttonId));
            findViewById(buttonId).setOnTouchListener(touchAnimation);
        }
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_chinese_number_conversion);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() != R.id.iv_del && v.getId() != R.id.btn_clr
                && v.getId() != R.id.btn_negate && v.getId() != R.id.btn_pt) {
            if (!showText.contains(".") && showText.length() >= 16) {
                return;
            }
        }

        String inputText = "";
        if (v.getId() != R.id.iv_del) {
            inputText = ((TextView) v).getText().toString();
        }

        if (v.getId() == R.id.btn_clr) {
            clear();
        } else if (v.getId() == R.id.iv_del) {
            if (Double.parseDouble(showText) <= 0 && showText.length() == 2) {
                showText = "0";
                operation();
                refreshText();
            } else {
                delete();
            }
        } else if (v.getId() == R.id.btn_negate) {
            if (Double.parseDouble(showText) > 0) {
                showText = "-" + showText;
            } else if (Double.parseDouble(showText) < 0) {
                showText = showText.substring(1);
            }
            operation();
            refreshText();
        } else if (v.getId() == R.id.btn_pt) {
            if (!showText.contains(".")) {
                showText = showText + ".";
            }
            refreshText();
        } else {
            if (showText.contains(".")) {
                if (showText.substring(showText.indexOf(".")).length() >= 3) {
                    return;
                }
            }
            if ("0".equals(showText)) {
                showText = inputText;
            } else {
                showText = showText + inputText;
            }
            operation();
            refreshText();
        }
    }

    private void clear() {
        showText = "0";
        resultsText = "零元整";
        refreshText();
    }

    private void refreshText() {
        tvInput.setText(Utils.formatNumber(showText));
        tvResults.setText(resultsText);
    }

    private void delete() {
        if (!showText.isEmpty()) {
            showText = showText.substring(0, showText.length() - 1);
            if (showText.isEmpty()) {
                showText = "0";
            }
            operation();
            refreshText();
        }
    }

    private void operation() {
        BigDecimal numberOfMoney = new BigDecimal(showText);
        StringBuilder sb = new StringBuilder();
        int signum = numberOfMoney.signum();
        if (signum == 0) {
            resultsText = CN_ZERO_FULL + CN_FULL;
            return;
        }

        BigDecimal scaledMoney = numberOfMoney.movePointRight(MONEY_PRECISION).setScale(0, 4).abs();
        long number = scaledMoney.longValue();
        int numUnit;
        int numIndex = 0;
        boolean getZero = false;
        int zeroSize = 0;

        while (number > 0) {
            numUnit = (int) (number % 10);
            if (numUnit > 0) {
                if (numIndex == 9 || numIndex == 13 || numIndex == 17) {
                    if (zeroSize >= 3) {
                        sb.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                    }
                }
                sb.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                getZero = false;
                zeroSize = 0;
            } else {
                ++zeroSize;
                if (numIndex != 0 && numIndex != 1 && numIndex != 2 &&
                        numIndex != 6 && numIndex != 10 && numIndex != 14) {
                    if (!getZero) {
                        sb.insert(0, CN_UPPER_NUMBER[numUnit]);
                    }
                }

                if (numIndex == 2) {
                    sb.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                } else if (((numIndex - 2) % 4 == 0) && (number % 1000 > 0)) {
                    sb.insert(0, CN_UPPER_MONETARY_UNIT[numIndex]);
                }
                getZero = true;
            }

            number /= 10;
            ++numIndex;
        }

        if (signum == -1) {
            sb.insert(0, CN_NEGATIVE);
        }
        if (!sb.toString().contains("分") && !sb.toString().contains("角")) {
            sb.append(CN_FULL);
        }
        resultsText = sb.toString();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        if ("vib".equals(s)) {
            for (int buttonId : BUTTON_IDS) {
                findViewById(buttonId).setHapticFeedbackEnabled(defaultSp.getBoolean("vib", false));
            }
        }
    }
}


package com.yangdai.calc.main.toolbox.functions.compass;

import static com.yangdai.calc.main.toolbox.functions.compass.CompassHelper.convertToDeg;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.math.BigDecimal;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 30415
 */
public class Compass extends BaseFunctionActivity implements SensorEventListener {
    private SensorManager sensorManager;
    Sensor accelerationSensor, magneticFieldSensor, pressureSensor;
    private final float[] accelerometerValues = new float[3];
    private final float[] magneticValues = new float[3];

    private float heading, longitude, latitude, altitude, magneticDeclination;
    private boolean isLocationRetrieved = false;
    private TextView tvTrueHeading, tvMagneticDeclination, tvDegree, tvLocation;
    private ImageView imageViewCompass;

    private String magneticFieldStrengthStr;
    private String addressStr;
    private String pressureStr;
    private Location location;
    int isGoogleAvailable;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    boolean permissionChecked = false;
    boolean updated = false;
    private Geocoder geocoder;
    private final HandlerThread handlerThread = new HandlerThread("LocationThread");
    private Handler handler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        tvDegree = findViewById(R.id.degree);
        tvLocation = findViewById(R.id.location);
        tvTrueHeading = findViewById(R.id.text_view_true_heading);
        tvMagneticDeclination = findViewById(R.id.text_view_magnetic_declination);
        imageViewCompass = findViewById(R.id.image_compass);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        isGoogleAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        @SuppressLint("MissingPermission") ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean fineLocationGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                    boolean coarseLocationGranted = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                    if (fineLocationGranted || coarseLocationGranted) {
                        permissionChecked = true;
                        getLocation();
                    } else {
                        // 用户拒绝了权限，可以根据需求进行相应的处理，例如显示一个提示信息或者禁用相关功能
                        Toast.makeText(getApplicationContext(), getString(R.string.permission), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            //请求权限
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        } else {
            permissionChecked = true;
            getLocation();
        }

        // 默认值为N/A。如果已检索到位置，则文本将更新为相应的值
        tvTrueHeading.setText(R.string.not_available);
        tvMagneticDeclination.setText(R.string.not_available);
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.compass);
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        if (isGoogleAvailable == ConnectionResult.SUCCESS) {
            handler.post(() -> {
                locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000).build();
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        update(locationResult.getLastLocation());
                    }
                };
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest);
                SettingsClient client = LocationServices.getSettingsClient(this);
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
                task.addOnFailureListener(this, e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(Compass.this, 2048);
                        } catch (IntentSender.SendIntentException ignored) {

                        }
                    }
                });

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                startLocationUpdates();
                updated = true;
            });

        } else {
            Toast.makeText(getApplicationContext(), "Not available on this device.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, handlerThread.getLooper());
    }

    private void stopLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @SuppressLint("SetTextI18n")
    private void update(Location location) {
        if (location != null) {
            this.location = location;
            getAddress(location);
            String coordinateStr = getString(R.string.latitude) + ": " + convertToDeg(location.getLatitude())
                    + " " + getString(R.string.longitude) + ": " + convertToDeg(location.getLongitude());
            String altitudeStr = getString(R.string.altitude) + ": "
                    + BigDecimal.valueOf(location.getAltitude())
                    .setScale(2, BigDecimal.ROUND_HALF_UP) + " m";
            String speedStr = getString(R.string.speed) + ": "
                    + BigDecimal.valueOf(location.getSpeed())
                    .setScale(2, BigDecimal.ROUND_HALF_UP) + " m/s";
            int mv = (int) Math.sqrt(Math.pow(magneticValues[0], 2) +
                    Math.pow(magneticValues[1], 2) + Math.pow(magneticValues[2], 2));
            isLocationRetrieved = true;
            latitude = (float) location.getLatitude();
            longitude = (float) location.getLongitude();
            altitude = (float) location.getAltitude();
            magneticDeclination = CompassHelper.calculateMagneticDeclination(latitude, longitude, altitude);

            runOnUiThread(() -> {
                tvLocation.setText(coordinateStr + "\n"
                        + (addressStr == null ? "" : addressStr) + "\n"
                        + altitudeStr + "\n"
                        + speedStr + "\n"
                        + (magneticFieldStrengthStr == null ? getString(R.string.magnetic) + ": " : magneticFieldStrengthStr) + "\n"
                        + (pressureStr == null ? getString(R.string.air_pressure) + ": " : pressureStr));

                if (mv > 20 && mv < 60) {
                    findViewById(R.id.abnormal).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.abnormal).setVisibility(View.VISIBLE);
                }
                tvMagneticDeclination.setText(getString(R.string.magnetic_declination, magneticDeclination));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (permissionChecked && updated) {
            startLocationUpdates();
        }

        if (accelerationSensor != null) {
            sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        if (magneticFieldSensor != null) {
            sensorManager.registerListener(this, magneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
        }

        if (pressureSensor != null) {
            sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (permissionChecked && updated) {
            stopLocationUpdates();
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handlerThread.quitSafely();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // 使用低通滤波器使传感器读数更平滑
            CompassHelper.lowPassFilter(event.values.clone(), accelerometerValues);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // 使用低通滤波器使传感器读数更平滑
            magneticFieldStrengthStr = getString(R.string.magnetic) + ": " + (int) Math.sqrt(
                    Math.pow(magneticValues[0], 2) +
                            Math.pow(magneticValues[1], 2) +
                            Math.pow(magneticValues[2], 2)) + " μT";
            CompassHelper.lowPassFilter(event.values.clone(), magneticValues);
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressureStr = getString(R.string.air_pressure) + ": "
                    + BigDecimal.valueOf(event.values[0]).setScale(2, BigDecimal.ROUND_HALF_UP) + " hPa";
        }
        update(location);
        updateHeading();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void updateHeading() {
        // 旧的heading值用于图像旋转动画
        float oldHeading = heading;

        heading = CompassHelper.calculateHeading(accelerometerValues, magneticValues);
        heading = CompassHelper.convertRadioDeg(heading);
        heading = CompassHelper.map180to360(heading);

        int angle = (int) heading;
        String direction;
        if (angle > 358 || angle < 2) {
            direction = getString(R.string.north);
        } else if (angle <= 88) {
            direction = getString(R.string.northeast) + " " + angle + "°";
        } else if (angle < 92) {
            direction = getString(R.string.east);
        } else if (angle <= 178) {
            direction = getString(R.string.southeast) + " " + (180 - angle) + "°";
        } else if (angle < 182) {
            direction = getString(R.string.south);
        } else if (angle <= 268) {
            direction = getString(R.string.southwest) + " " + (angle - 180) + "°";
        } else if (angle < 272) {
            direction = getString(R.string.west);
        } else {
            direction = getString(R.string.northwest) + " " + (360 - angle) + "°";
        }

        runOnUiThread(() -> {
            tvDegree.setText(direction);

            if (isLocationRetrieved) {
                float trueHeading = heading + magneticDeclination;
                if (trueHeading > 360) {
                    // 如果trueHeading为362度，例如，它应该调整为2度
                    trueHeading = trueHeading - 360;
                }
                tvTrueHeading.setText(getString(R.string.true_heading, (int) trueHeading));
                magneticDeclination = CompassHelper.calculateMagneticDeclination(latitude, longitude, altitude);
                tvMagneticDeclination.setText(getString(R.string.magnetic_declination, magneticDeclination));
            }

            RotateAnimation rotateAnimation
                    = new RotateAnimation(-oldHeading, -heading,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(500);
            rotateAnimation.setFillAfter(true);
            imageViewCompass.startAnimation(rotateAnimation);
        });
    }

    /**
     * 获取地址信息: 城市、街道等信息
     */
    private void getAddress(Location location) {
        if (location == null) {
            return;
        }

        EXECUTOR.execute(() -> {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(),
                            1,
                            list -> {
                                if (!list.isEmpty()) {
                                    Compass.this.addressStr = list.get(0).getAddressLine(0);
                                }
                            });
                } else {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(),
                            1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Compass.this.addressStr = addresses.get(0).getAddressLine(0);
                    }
                }
            } catch (Exception e) {
                Log.e("Address", e.toString());
            }
        });
    }
}


package com.yangdai.calc.main.toolbox.functions.compass;

import android.hardware.GeomagneticField;

/**
 * @author 30415
 */
public class CompassHelper {
    /**
     * 0 ≤ ALPHA ≤ 1
     * 较小的ALPHA值会导致更平滑的传感器数据，但更新速度较慢
     */
    public static final float ALPHA = 0.12f;

    public static float calculateHeading(float[] accelerometerReading, float[] magnetometerReading) {
        float ax = accelerometerReading[0];
        float ay = accelerometerReading[1];
        float az = accelerometerReading[2];

        float ex = magnetometerReading[0];
        float ey = magnetometerReading[1];
        float ez = magnetometerReading[2];

        // 磁场向量和重力向量的叉积
        float hx = ey * az - ez * ay;
        float hy = ez * ax - ex * az;
        float hz = ex * ay - ey * ax;

        // 规范化结果向量的值
        final float invH = 1.0f / (float) Math.sqrt(hx * hx + hy * hy + hz * hz);
        hx *= invH;
        hy *= invH;
        hz *= invH;

        // 规范化重力向量的值
        final float invA = 1.0f / (float) Math.sqrt(ax * ax + ay * ay + az * az);
        ax *= invA;
        //ay *= invA;
        az *= invA;

        // 重力向量和新向量H的叉积
        //final float mx = ay * hz - az * hy;
        final float my = az * hx - ax * hz;
        //final float mz = ax * hy - ay * hx;

        // 使用反正切函数获取弧度表示的方向
        return (float) Math.atan2(hy, my);
    }

    /**
     * 获取真北与磁北的磁偏角
     */
    public static float calculateMagneticDeclination(double latitude, double longitude, double altitude) {
        GeomagneticField geomagneticField =
                new GeomagneticField((float) latitude,
                        (float) longitude,
                        (float) altitude,
                        System.currentTimeMillis());
        return geomagneticField.getDeclination();
    }

    public static float convertRadioDeg(float rad) {
        return (float) (rad / Math.PI) * 180;
    }

    /**
     * 将角度从[-180,180]范围映射到[0,360]范围
     */
    public static float map180to360(float angle) {
        return (angle + 360) % 360;
    }

    public static void lowPassFilter(float[] input, float[] output) {
        if (output == null) {
            return;
        }
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
    }

    public static String convertToDeg(double coordinate) {
        // 判断是否为负数
        boolean isNegative = coordinate < 0;
        // 取绝对值进行计算
        double absoluteCoordinate = Math.abs(coordinate);
        // 度部分
        int degrees = (int) absoluteCoordinate;
        // 分部分
        double decimalMinutes = (absoluteCoordinate - degrees) * 60;
        int minutes = (int) decimalMinutes;
        // 秒部分
        double decimalSeconds = (decimalMinutes - minutes) * 60;
        int seconds = (int) decimalSeconds;

        // 根据是否为负数拼接结果
        String result;
        if (isNegative) {
            result = "-";
        } else {
            result = "";
        }

        result += degrees + "° " + minutes + "' " + seconds + "\" ";
        return result;
    }

}


package com.yangdai.calc.main.toolbox.functions.random;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yangdai.calc.R;

import java.util.Random;

/**
 * @author 30415
 */
public class PickerFragment extends Fragment {
    private TextView textView;
    private EditText minValueEditText;
    private EditText maxValueEditText;
    private Button button;
    private Handler handler;
    private boolean isRolling;
    private final Random random = new Random();

    public PickerFragment() {
    }

    public static PickerFragment newInstance() {
        return new PickerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = view.findViewById(R.id.textView);
        button = view.findViewById(R.id.startButton);
        minValueEditText = view.findViewById(R.id.minValueEditText);
        minValueEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                minValueEditText.clearFocus();
                return true;
            }
            return false;
        });
        maxValueEditText = view.findViewById(R.id.maxValueEditText);
        maxValueEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                maxValueEditText.clearFocus();
                return true;
            }
            return false;
        });
        handler = new Handler(Looper.getMainLooper());

        String startOrStop = getString(R.string.start_pause);
        button.setText(startOrStop.split("/")[0].trim());

        button.setOnClickListener(v -> {
            closeKeyboard(requireActivity());
            if (isRolling) {
                stopRolling();
                button.setText(startOrStop.split("/")[0].trim());
            } else {
                if (validateInput()) {
                    startRolling();
                    button.setText(startOrStop.split("/")[1].trim());
                } else {
                    Toast.makeText(getContext(), getString(R.string.formatError), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInput() {
        String minText = minValueEditText.getText().toString();
        String maxText = maxValueEditText.getText().toString();

        if (TextUtils.isEmpty(minText) || TextUtils.isEmpty(maxText)) {
            return false;
        }

        int min = Integer.parseInt(minText);
        int max = Integer.parseInt(maxText);

        return min < max;
    }

    private synchronized void startRolling() {
        isRolling = true;
        handler.postDelayed(rollingRunnable, 80);
    }

    private synchronized void stopRolling() {
        isRolling = false;
        handler.removeCallbacks(rollingRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRolling();
    }

    private final Runnable rollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!validateInput()) {
                return;
            }
            try {
                int minValue = Integer.parseInt(minValueEditText.getText().toString());
                int maxValue = Integer.parseInt(maxValueEditText.getText().toString());
                int randomNumber = random.nextInt(maxValue - minValue + 1) + minValue;
                textView.setText(String.valueOf(randomNumber));
            } catch (NumberFormatException e) {
                // Handle invalid input here, display an error message to the user
                Toast.makeText(getContext(), getString(R.string.formatError), Toast.LENGTH_SHORT).show();
                stopRolling();
                return;
            }

            if (isRolling) {
                handler.postDelayed(this, 80);
            }
        }
    };
}

package com.yangdai.calc.main.toolbox.functions.random;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.yangdai.calc.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author 30415
 */
public class GeneratorFragment extends Fragment {

    private TextView randomNumberTextView, randomNumber;
    private EditText countEditText;
    private EditText minValueEditText;
    private EditText maxValueEditText;
    private List<Integer> randomNumbersList;
    private int index = 0;
    private boolean repeat = false;
    private Button generateButton;
    private AnimatorSet animatorSet;

    public GeneratorFragment() {
    }

    public static GeneratorFragment newInstance() {
        return new GeneratorFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        randomNumberTextView = view.findViewById(R.id.randomNumberTextView);
        countEditText = view.findViewById(R.id.countEditText);
        minValueEditText = view.findViewById(R.id.minValueEditText);
        maxValueEditText = view.findViewById(R.id.maxValueEditText);
        maxValueEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                maxValueEditText.clearFocus();
                return true;
            }
            return false;
        });
        generateButton = view.findViewById(R.id.generateButton);
        randomNumber = view.findViewById(R.id.randomNumber);
        MaterialCheckBox checkBox = view.findViewById(R.id.checkbox);

        checkBox.setOnCheckedChangeListener((compoundButton, b) -> repeat = b);

        generateButton.setOnClickListener(v -> {
            closeKeyboard(requireActivity());
            if (generateButton.getText().toString().equals(getString(R.string.jump))) {
                animatorSet.cancel();
                randomNumber.setVisibility(View.GONE);
                index = 0;
                randomNumberTextView.setText(TextUtils.join(";  ", randomNumbersList));
                generateButton.setText(getString(R.string.generate));
                return;
            }
            if (validateInput()) {
                randomNumber.setVisibility(View.VISIBLE);
                index = 0;
                randomNumberTextView.setText("");
                generateRandomNumbers();
            } else {
                Toast.makeText(getContext(), getString(R.string.formatError), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput() {
        String countText = countEditText.getText().toString();
        String minText = minValueEditText.getText().toString();
        String maxText = maxValueEditText.getText().toString();

        if (countText.isEmpty() || minText.isEmpty() || maxText.isEmpty()) {
            return false;
        }

        int count = Integer.parseInt(countText);
        int min = Integer.parseInt(minText);
        int max = Integer.parseInt(maxText);

        if (count == 0) {
            return false;
        }

        if (!repeat) {
            if (count > Math.abs(max - min) + 1) {
                return false;
            }
        }

        return min <= max;
    }

    @SuppressLint("SetTextI18n")
    private void generateRandomNumbers() {
        int count = Integer.parseInt(countEditText.getText().toString());
        int min = Integer.parseInt(minValueEditText.getText().toString());
        int max = Integer.parseInt(maxValueEditText.getText().toString());
        generateButton.setText(getString(R.string.jump));

        if (!repeat) {
            randomNumbersList = ThreadLocalRandom.current()
                    .ints(min, max + 1)
                    .distinct()
                    .limit(count)
                    .boxed()
                    .collect(Collectors.toList());
        } else {
            randomNumbersList = ThreadLocalRandom.current()
                    .ints(min, max + 1)
                    .limit(count)
                    .boxed()
                    .collect(Collectors.toList());
        }
        animateRandomNumbers(randomNumbersList);
    }

    private void animateRandomNumbers(List<Integer> randomNumbersList) {
        animatorSet = new AnimatorSet();
        List<Animator> animatorList = new ArrayList<>();

        for (int randomNumber : randomNumbersList) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(0, randomNumber);
            valueAnimator.setDuration(400);
            valueAnimator.setStartDelay(250);

            valueAnimator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                this.randomNumber.setText(String.valueOf(animatedValue));
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    updateTextView();
                }
            });

            animatorList.add(valueAnimator);
        }

        animatorSet.playSequentially(animatorList);
        animatorSet.start();
    }

    @SuppressLint("SetTextI18n")
    private void updateTextView() {
        if (index < randomNumbersList.size()) {
            if (index == randomNumbersList.size() - 1) {
                randomNumber.setVisibility(View.GONE);
                generateButton.setText(getString(R.string.generate));
                randomNumberTextView.setText(randomNumberTextView.getText().toString() + randomNumbersList.get(index));
            } else {
                randomNumberTextView.setText(randomNumberTextView.getText().toString() + randomNumbersList.get(index) + ";  ");
            }
            index++;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (animatorSet != null) {
            animatorSet.cancel();
        }
    }
}

package com.yangdai.calc.main.toolbox.functions.random;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;


/**
 * @author 30415
 */
public class RandomNumberActivity extends BaseFunctionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_random_number);
    }

    private void initView() {
        ViewPager2 mViewPager = findViewById(R.id.view_pager_main);
        TabLayout mTabLayout = findViewById(R.id.tab_view);

        final String[] tabs = new String[]{getString(R.string.generator), getString(R.string.draw)};
        final int[] icons = new int[]{R.drawable.random_dice, R.drawable.random_card};

        mViewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return GeneratorFragment.newInstance();
                } else {
                    return PickerFragment.newInstance();
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    tab.setText(tabs[position]);
                    tab.setIcon(icons[position]);
                }).attach();
    }
}


package com.yangdai.calc.main.toolbox.functions.fraction;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;

/**
 * @author 30415
 */
public class FractorizationFragment extends Fragment implements TextWatcher {

    TextInputEditText edEnter;
    TextView tvIsPrim, tvFactors;

    public FractorizationFragment() {
    }

    public static FractorizationFragment newInstance() {
        return new FractorizationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fractorization, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edEnter = view.findViewById(R.id.enter);
        tvIsPrim = view.findViewById(R.id.isPrim);
        tvFactors = view.findViewById(R.id.factor);
        edEnter.addTextChangedListener(this);
        edEnter.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                edEnter.clearFocus();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        edEnter.removeTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable == null || editable.toString().isEmpty()) {
            tvIsPrim.setText("");
            tvFactors.setText("");
            return;
        }
        try {
            calculate(editable.toString());
        } catch (Exception e) {
            tvIsPrim.setText("");
            tvFactors.setText("");
        }
    }

    private boolean prime(int x) {
        //判断是否为质数（因子和原数，为质数时返回值为true）
        boolean s = true;
        for (int i = 2; i <= Math.sqrt(x); i++) {
            if (x % i == 0) {
                s = false;
                break;
            }
        }
        return s;
    }

    private String fac(int x) {
        StringBuilder sb = new StringBuilder();
        //计算因式分解式
        for (int i = 2; i <= Math.sqrt(x); i++) {
            if (prime(i)) {
                //判断i是否为质数
                while (x % i == 0) {
                    //判断质数i是否为y的质因数
                    sb.append(i).append("⨯");
                    x = x / i;
                    //除以第一个质因数后计算后面的质因数
                    if (prime(x)) {
                        //是否分解完全
                        sb.append(x);
                        break;
                    }
                }
            }
        }
        return sb.toString();
    }

    private void calculate(String str) {
        int x = Integer.parseInt(str);
        if (x == 0 || x == 1) {
            tvIsPrim.setText(getString(R.string.isPrime_false));
            tvFactors.setText("");
            return;
        }
        Handler handler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            boolean isPrime = prime(x);
            String factors = fac(x);
            handler.post(() -> {
                tvIsPrim.setText(isPrime ? getString(R.string.isPrime_true) : getString(R.string.isPrime_false));
                tvFactors.setText(isPrime ? str : factors);
            });
        }).start();
    }
}

package com.yangdai.calc.main.toolbox.functions.fraction;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.icu.math.BigDecimal;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;
import com.yangdai.calc.utils.Utils;

import java.util.Objects;

/**
 * @author 30415
 */
public class ToDecimalFragment extends Fragment implements TextWatcher {
    TextInputEditText aInput, bInput;
    TextView textView;

    public ToDecimalFragment() {
    }

    public static ToDecimalFragment newInstance() {
        return new ToDecimalFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_decimal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        aInput = view.findViewById(R.id.aEditText);
        bInput = view.findViewById(R.id.bEditText);
        bInput.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                bInput.clearFocus();
                return true;
            }
            return false;
        });
        textView = view.findViewById(R.id.equation);

        aInput.addTextChangedListener(this);
        bInput.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String a = Objects.requireNonNull(aInput.getText()).toString();
        String b = Objects.requireNonNull(bInput.getText()).toString();

        StringBuilder equationStr = new StringBuilder();
        if (a.isEmpty()) {
            equationStr.append("A");
        } else {
            equationStr.append(a);
        }
        equationStr.append(" / ");
        if (b.isEmpty()) {
            equationStr.append("B");
        } else {
            equationStr.append(b);
        }
        equationStr.append(" = ");

        if (!a.isEmpty() && !b.isEmpty()) {
            try {
                if (new BigDecimal(b).compareTo(BigDecimal.ZERO) != 0) {
                    String resStr;
                    try {
                        resStr = Utils.fractionToDecimal(Integer.parseInt(a), Integer.parseInt(b));
                    } catch (Exception e) {
                        resStr = "_.__";
                    }
                    equationStr.append(resStr);
                    textView.setText(equationStr);
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), getString(R.string.formatError), Toast.LENGTH_SHORT).show();
            }
        }
        equationStr.append("_.__");
        textView.setText(equationStr);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        aInput.removeTextChangedListener(this);
        bInput.removeTextChangedListener(this);
    }
}

package com.yangdai.calc.main.toolbox.functions.fraction;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.yangdai.calc.R;
import com.yangdai.calc.utils.Utils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author 30415
 */
public class ToFractionFragment extends Fragment implements TextWatcher {

    TextView textView;
    TextInputEditText editText;

    public ToFractionFragment() {
    }

    public static ToFractionFragment newInstance() {
        return new ToFractionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_to_fraction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = view.findViewById(R.id.equation);
        editText = view.findViewById(R.id.xEditText);
        editText.addTextChangedListener(this);
        editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(requireActivity());
                editText.clearFocus();
                return true;
            }
            return false;
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void afterTextChanged(Editable editable) {
        String input = Objects.requireNonNull(editText.getText()).toString();
        if (input.isEmpty()) {
            textView.setText("_.__ = A / B");
            return;
        }
        // 使用正则表达式进行匹配
        String pattern = "^[0-9.()]+$";
        boolean isMatch = Pattern.matches(pattern, input);
        if (isMatch) {
            StringBuilder sb = new StringBuilder();
            sb.append(input).append(" = ");
            String res = Utils.decimalToFraction(input);
            sb.append(res);
            textView.setText(sb);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        editText.removeTextChangedListener(this);
    }
}

package com.yangdai.calc.main.toolbox.functions.fraction;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;


/**
 * @author 30415
 */
public class FractionActivity extends BaseFunctionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_fraction);
    }

    private void initView() {
        ViewPager2 mViewPager = findViewById(R.id.view_pager_main);
        TabLayout mTabLayout = findViewById(R.id.tab_view);

        final String[] tabs = new String[]{getString(R.string.toDecimal), getString(R.string.toFraction), getString(R.string.factorization)};
        final int[] icons = new int[]{R.drawable.fraction_icon, R.drawable.decimal_icon, R.drawable.factor_icon};

        mViewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return ToDecimalFragment.newInstance();
                } else if (position == 1) {
                    return ToFractionFragment.newInstance();
                } else {
                    return FractorizationFragment.newInstance();
                }
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        new TabLayoutMediator(mTabLayout, mViewPager,
                (tab, position) -> {
                    tab.setText(tabs[position]);
                    tab.setIcon(icons[position]);
                }).attach();
    }
}

package com.yangdai.calc.main.toolbox.functions.currency;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;
import com.yangdai.calc.R;

import java.util.List;

/**
 * @author 30415
 */
public class CurrencyAdapter extends ArrayAdapter<Currency> {
    private final boolean showEnglishName;

    public CurrencyAdapter(Context context, List<Currency> currencies, boolean showEnglishName) {
        super(context, 0, currencies);
        this.showEnglishName = showEnglishName;
    }

    @NonNull
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.currency_layout, parent, false);
        }

        Currency currency = getItem(position);
        if (currency != null) {
            ShapeableImageView shapeableImageView = convertView.findViewById(R.id.flag);
            shapeableImageView.setImageResource(currency.id());
            TextView textView = convertView.findViewById(R.id.symbol);
            textView.setText(currency.symbol());
            TextView textView1 = convertView.findViewById(R.id.name);
            if (!showEnglishName) {
                textView1.setText(currency.chineseName());
            } else {
                textView1.setText(currency.englishName());
            }
        }

        return convertView;
    }
}


package com.yangdai.calc.main.toolbox.functions.currency;

import static com.yangdai.calc.utils.Utils.closeKeyboard;
import static com.yangdai.calc.utils.Utils.formatNumberFinance;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.imageview.ShapeableImageView;
import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;
import com.yangdai.calc.utils.TouchAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author 30415
 */
public class CurrencyActivity extends BaseFunctionActivity {
    private Spinner fromCurrencySpinner;
    private Spinner toCurrencySpinner;
    private EditText amountEditText;
    private TextView resultTextView, textView, nameFrom, nameTo;
    private CurrencyViewModel currencyViewModel;
    private ShapeableImageView imageViewFrom, imageViewTo;
    private boolean first = true, showEnglishName;
    private static final List<Currency> CURRENCIES = new ArrayList<>(Arrays.asList(
            new Currency(R.drawable.australia, "AUD", "澳大利亚元", "Australian Dollar"),
            new Currency(R.drawable.bulgaria, "BGN", "保加利亚列弗", "Bulgarian Lev"),
            new Currency(R.drawable.brazil, "BRL", "巴西雷亚尔", "Brazilian Real"),
            new Currency(R.drawable.canada, "CAD", "加拿大元", "Canadian Dollar"),
            new Currency(R.drawable.switzerland, "CHF", "瑞士法郎", "Swiss Franc"),
            new Currency(R.drawable.china, "CNY", "人民币", "Chinese Yuan"),
            new Currency(R.drawable.czechia, "CZK", "捷克克朗", "Czech Koruna"),
            new Currency(R.drawable.denmark, "DKK", "丹麦克朗", "Danish Krone"),
            new Currency(R.drawable.eu, "EUR", "欧元", "Euro"),
            new Currency(R.drawable.uk, "GBP", "英镑", "British Pound"),
            new Currency(R.drawable.hongkongchina, "HKD", "港元", "Hong Kong Dollar"),
            new Currency(R.drawable.hungary, "HUF", "匈牙利福林", "Hungarian Forint"),
            new Currency(R.drawable.indonesia, "IDR", "印尼卢比", "Indonesian Rupiah"),
            new Currency(R.drawable.israel, "ILS", "以色列新谢克尔", "Israeli Shekel"),
            new Currency(R.drawable.india, "INR", "印度卢比", "Indian Rupee"),
            new Currency(R.drawable.iceland, "ISK", "冰岛克朗", "Icelandic Króna"),
            new Currency(R.drawable.japan, "JPY", "日元", "Japanese Yen"),
            new Currency(R.drawable.south_korea, "KRW", "韩元", "South Korean Won"),
            new Currency(R.drawable.mexico, "MXN", "墨西哥比索", "Mexican Peso"),
            new Currency(R.drawable.malaysia, "MYR", "马来西亚林吉特", "Malaysian Ringgit"),
            new Currency(R.drawable.norway, "NOK", "挪威克朗", "Norwegian Krone"),
            new Currency(R.drawable.new_zealand, "NZD", "新西兰元", "New Zealand Dollar"),
            new Currency(R.drawable.philippines, "PHP", "菲律宾比索", "Philippine Peso"),
            new Currency(R.drawable.poland, "PLN", "波兰兹罗提", "Polish Zloty"),
            new Currency(R.drawable.romania, "RON", "罗马尼亚列伊", "Romanian Leu"),
            new Currency(R.drawable.sweden, "SEK", "瑞典克朗", "Swedish Krona"),
            new Currency(R.drawable.singapore, "SGD", "新加坡元", "Singapore Dollar"),
            new Currency(R.drawable.thailand, "THB", "泰铢", "Thai Baht"),
            new Currency(R.drawable.turkey, "TRY", "土耳其里拉", "Turkish Lira"),
            new Currency(R.drawable.us, "USD", "美元", "United States Dollar"),
            new Currency(R.drawable.south_africa, "ZAR", "南非兰特", "South African Rand")
    ));

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.currency_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.tip) {
            boolean showed = findViewById(R.id.list_view_currency).getVisibility() == View.VISIBLE;
            findViewById(R.id.list_view_currency).setVisibility(showed ? View.GONE : View.VISIBLE);
            if (showed) {
                item.setIcon(R.drawable.tips_off);
            } else {
                item.setIcon(R.drawable.tips_on);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();

        // 获取当前应用语言
        Configuration config = getResources().getConfiguration();
        Locale currentLocale = config.getLocales().get(0);
        showEnglishName = !currentLocale.getLanguage().equals("zh");

        List<String> currenciesList = new ArrayList<>();
        currenciesList.add("N/A");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                currenciesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromCurrencySpinner.setAdapter(adapter);
        fromCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = parent.getItemAtPosition(position).toString();
                CURRENCIES.forEach(currency -> {
                    if (selectedCurrency.equals(currency.symbol())) {
                        imageViewFrom.setImageResource(currency.id());
                        nameFrom.setText(showEnglishName ? currency.englishName() : currency.chineseName());
                    }
                });
                calculateCurrency();
                if (!first) {
                    SharedPreferences.Editor editor = defaultSp.edit();
                    editor.putInt("from", position);
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        toCurrencySpinner.setAdapter(adapter);
        toCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = parent.getItemAtPosition(position).toString();
                CURRENCIES.forEach(currency -> {
                    if (selectedCurrency.equals(currency.symbol())) {
                        imageViewTo.setImageResource(currency.id());
                        nameTo.setText(showEnglishName ? currency.englishName() : currency.chineseName());
                    }
                });
                calculateCurrency();
                if (!first) {
                    SharedPreferences.Editor editor = defaultSp.edit();
                    editor.putInt("to", position);
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Create the CurrencyViewModel
        currencyViewModel = new ViewModelProvider(this).get(CurrencyViewModel.class);
        currencyViewModel.getExchangeRates().observe(this, exchangeRates -> {
            if (exchangeRates != null && !exchangeRates.isEmpty()) {
                String[] currencies = exchangeRates.keySet().toArray(new String[0]);
                Arrays.sort(currencies);
                adapter.clear();
                adapter.addAll(currencies);
                adapter.notifyDataSetChanged();

                if (first) {
                    first = false;
                    int from = defaultSp.getInt("from", 0);
                    int to = defaultSp.getInt("to", 0);
                    fromCurrencySpinner.setSelection(from);
                    toCurrencySpinner.setSelection(to);
                    imageViewFrom.setImageResource(CURRENCIES.get(from).id());
                    imageViewTo.setImageResource(CURRENCIES.get(to).id());
                    nameFrom.setText(showEnglishName ? CURRENCIES.get(from).englishName() : CURRENCIES.get(from).chineseName());
                    nameTo.setText(showEnglishName ? CURRENCIES.get(to).englishName() : CURRENCIES.get(to).chineseName());
                }

                String date;
                // 创建一个Calendar对象，并设置时区为中欧时区
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Central"));
                // 获取当前时间的小时数
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                if (currentHour >= 15) {
                    // 当前时间大于等于15:00，输出当天日期
                    int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int currentMonth = calendar.get(Calendar.MONTH) + 1; // 月份从0开始，需要加1
                    int currentYear = calendar.get(Calendar.YEAR);

                    date = currentYear + "-" + currentMonth + "-" + currentDay;
                } else {
                    // 当前时间小于15:00，输出前一天日期
                    calendar.add(Calendar.DAY_OF_MONTH, -1); // 将日期减一天

                    int previousDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int previousMonth = calendar.get(Calendar.MONTH) + 1; // 月份从0开始，需要加1
                    int previousYear = calendar.get(Calendar.YEAR);

                    date = previousYear + "-" + previousMonth + "-" + previousDay;
                }
                textView.setText(getString(R.string.ecb) + "\n" + getString(R.string.updateDate) + " " + date);
            }
        });

        currencyViewModel.loadExchangeRates();

        ListView listView = findViewById(R.id.list_view_currency);
        CurrencyAdapter adapter1 = new CurrencyAdapter(this, CURRENCIES, showEnglishName);
        listView.setAdapter(adapter1);
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_exchange);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUi() {
        resultTextView = findViewById(R.id.result_text_view);
        fromCurrencySpinner = findViewById(R.id.from_currency_spinner);
        toCurrencySpinner = findViewById(R.id.to_currency_spinner);
        amountEditText = findViewById(R.id.amount_edit_text);
        textView = findViewById(R.id.textView2);
        imageViewFrom = findViewById(R.id.flag_from);
        imageViewTo = findViewById(R.id.flag_to);
        nameFrom = findViewById(R.id.name_from);
        nameTo = findViewById(R.id.name_to);
        ImageView imageView = findViewById(R.id.switchCurrency);
        TouchAnimation touchAnimation = new TouchAnimation(imageView);
        imageView.setOnTouchListener(touchAnimation);
        imageView.setOnClickListener(v -> {
            // 获取当前fromSpinner和toSpinner的选中位置
            int fromPosition = fromCurrencySpinner.getSelectedItemPosition();
            int toPosition = toCurrencySpinner.getSelectedItemPosition();

            // 交换fromSpinner和toSpinner的选中位置
            fromCurrencySpinner.setSelection(toPosition);
            toCurrencySpinner.setSelection(fromPosition);
        });
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                calculateCurrency();
            }
        });
        amountEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(this);
                amountEditText.clearFocus();
                return true;
            }
            return false;
        });
    }

    public void calculateCurrency() {
        HashMap<String, Double> exchangeRates = currencyViewModel.getExchangeRates().getValue();
        if (exchangeRates == null || exchangeRates.isEmpty()) {
            // 检查问题
            return;
        }

        String amountStr = amountEditText.getText().toString();
        if (amountStr.isEmpty() || "N/A".equals(amountStr)) {
            resultTextView.setText("");
            return;
        }

        try {
            String fromCurrency = fromCurrencySpinner.getSelectedItem().toString();
            String toCurrency = toCurrencySpinner.getSelectedItem().toString();
            double amount = Double.parseDouble(amountStr);
            Double fromRate = exchangeRates.get(fromCurrency);
            Double toRate = exchangeRates.get(toCurrency);
            if (fromRate != null && toRate != null) {
                double result = (amount / fromRate) * toRate;
                resultTextView.setText(formatNumberFinance(String.valueOf(result)));
            }
        } catch (Exception e) {
            resultTextView.setText("");
        }
    }
}


package com.yangdai.calc.main.toolbox.functions.currency;

/**
 * @author 30415
 */
public record Currency(int id, String symbol, String chineseName, String englishName) {
}


package com.yangdai.calc.main.toolbox.functions.currency;

import android.util.Xml;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 30415
 */
public class CurrencyViewModel extends ViewModel {

    private MutableLiveData<HashMap<String, Double>> exchangeRates;
    private static final String ECB_API_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private ExecutorService executorService;

    public LiveData<HashMap<String, Double>> getExchangeRates() {
        if (exchangeRates == null) {
            exchangeRates = new MutableLiveData<>();
            loadExchangeRates();
        }
        return exchangeRates;
    }

    public void loadExchangeRates() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        executorService.execute(() -> {
            try {
                URL url = new URL(ECB_API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                InputStream inputStream = connection.getInputStream();

                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(inputStream, null);

                HashMap<String, Double> exchangeRatesMap = parseXml(parser);

                inputStream.close();
                connection.disconnect();

                exchangeRates.postValue(exchangeRatesMap);
            } catch (Exception e) {
                e.printStackTrace();
                exchangeRates.postValue(null);
            }
        });
    }

    private HashMap<String, Double> parseXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        HashMap<String, Double> exchangeRates = new HashMap<>();
        int eventType = parser.getEventType();
        String currency = "";
        double rate = 0.0;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG -> {
                    if ("Cube".equals(tagName) && parser.getAttributeCount() == 2) {
                        currency = parser.getAttributeValue(null, "currency");
                        String rateString = parser.getAttributeValue(null, "rate");
                        rate = Double.parseDouble(rateString);
                    }
                }
                case XmlPullParser.END_TAG -> {
                    if ("Cube".equals(tagName) && !currency.isEmpty()) {
                        exchangeRates.put(currency, rate);
                        currency = "";
                    }
                }
                default -> {
                }
            }
            eventType = parser.next();
        }
        exchangeRates.put("EUR", 1.0);
        return exchangeRates;
    }
}


package com.yangdai.calc.main.toolbox.functions.algebra;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.icu.math.BigDecimal;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;


import com.yangdai.calc.R;
import com.yangdai.calc.main.toolbox.functions.BaseFunctionActivity;
import com.yangdai.calc.utils.TouchAnimation;
import com.yangdai.calc.utils.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 30415
 */
public class StatisticsActivity extends BaseFunctionActivity {

    private Adapter adapter;
    private TextView tvGcd, tvLcm, tvAvg0, tvAvg1, tvAvg2, tvAvg3, tvSum, tvDiff0, tvDiff1;
    private List<String> inputStringList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        tvGcd = findViewById(R.id.gcd);
        tvLcm = findViewById(R.id.lcm);
        tvAvg0 = findViewById(R.id.avg0);
        tvAvg1 = findViewById(R.id.avg1);
        tvAvg2 = findViewById(R.id.avg2);
        tvAvg3 = findViewById(R.id.avg3);
        tvDiff0 = findViewById(R.id.diff0);
        tvDiff1 = findViewById(R.id.diff1);
        tvSum = findViewById(R.id.sum);
        findViewById(R.id.bt_add).setOnClickListener(v -> adapter.add());
        TouchAnimation touchAnimation = new TouchAnimation(findViewById(R.id.bt_add));
        findViewById(R.id.bt_add).setOnTouchListener(touchAnimation);

        List<Item> dataList = new ArrayList<>();
        dataList.add(new Item(""));
        dataList.add(new Item(""));

        adapter = new Adapter(this, dataList, textWatcher);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void setRootView() {
        setContentView(R.layout.activity_statistic);
    }

    private final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getBindingAdapterPosition() == 0 || viewHolder.getBindingAdapterPosition() == 1) {
                return 0;
            }
            int swiped = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            //第一个参数拖动，第二个删除侧滑
            return makeMovementFlags(0, swiped);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getBindingAdapterPosition();
            adapter.remove(position);
            inputStringList = adapter.getAllInput();
            calculate(inputStringList);
        }
    });

    final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            inputStringList = adapter.getAllInput();
            calculate(inputStringList);
        }
    };

    private void calculate(List<String> inputStringList) {
        List<BigInteger> integerList = inputStringList.stream()
                .filter(str -> !str.isEmpty())
                .map(BigInteger::new)
                .collect(Collectors.toList());
        calculateGcd(integerList);
        calculateLcm(integerList);
        calculateAvg0(integerList);
        calculateAvg1(integerList);
        calculateAvg2(integerList);
        calculateAvg3(integerList);
        calculateVariance(integerList);
        calculateStandardDeviation(integerList);
        calculateSum(integerList);
    }

    @SuppressLint("SetTextI18n")
    private void calculateGcd(List<BigInteger> integerList) {
        try {
            tvGcd.setText(Utils.formatNumber(Utils.gcdMultiple(integerList).toString()));
        } catch (Exception e) {
            tvGcd.setText("");
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateLcm(List<BigInteger> integerList) {
        try {
            tvLcm.setText(Utils.formatNumber(Utils.lcmMultiple(integerList).toString()));
        } catch (Exception e) {
            tvLcm.setText("");
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateAvg0(List<BigInteger> integerList) {
        BigInteger sum = integerList.stream()
                .reduce(BigInteger.ZERO, BigInteger::add);
        try {
            BigDecimal avg = new BigDecimal(sum).divide(BigDecimal.valueOf(integerList.size()), 10, BigDecimal.ROUND_HALF_UP);
            tvAvg0.setText(Utils.formatNumber(avg.toBigDecimal().toPlainString()));
        } catch (Exception e) {
            tvAvg0.setText("");
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateAvg1(List<BigInteger> integerList) {
        if (integerList.isEmpty()) {
            tvAvg1.setText("");
            return;
        }
        double product = integerList.stream()
                .mapToDouble(BigInteger::doubleValue)
                .reduce(1, (a, b) -> a * b);
        try {
            double avg = Math.pow(product, 1.0 / integerList.size());
            tvAvg1.setText(Utils.formatNumber(Double.toString(avg)));
        } catch (Exception e) {
            tvAvg1.setText("");
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateAvg2(List<BigInteger> integerList) {
        double sumReciprocals = integerList.stream()
                .mapToDouble(value -> 1.0 / value.doubleValue())
                .sum();
        try {
            double avg = integerList.size() / sumReciprocals;
            tvAvg2.setText(Utils.formatNumber(Double.toString(avg)));
        } catch (Exception e) {
            tvAvg2.setText("");
        }
    }

    @SuppressLint("SetTextI18n")
    private void calculateAvg3(List<BigInteger> integerList) {
        double sumSquares = integerList.stream()
                .mapToDouble(value -> Math.pow(value.doubleValue(), 2))
                .sum();
        try {
            double avg = Math.sqrt(sumSquares / integerList.size());
            tvAvg3.setText(Utils.formatNumber(Double.toString(avg)));
        } catch (Exception e) {
            tvAvg3.setText("");
        }
    }

    private double calculateVariance(List<BigInteger> integerList) {
        double sum = 0;
        double sumSquares = 0;
        int size = integerList.size();

        for (BigInteger value : integerList) {
            double doubleValue = value.doubleValue();
            sum += doubleValue;
            sumSquares += Math.pow(doubleValue, 2);
        }

        try {
            double mean = sum / size;
            double variance = (sumSquares / size) - Math.pow(mean, 2);
            tvDiff0.setText(Utils.formatNumber(Double.toString(variance)));
            return variance;
        } catch (Exception e) {
            tvDiff0.setText("");
            return 0;
        }
    }

    private void calculateStandardDeviation(List<BigInteger> integerList) {
        double variance = calculateVariance(integerList);
        double standardDeviation = Math.sqrt(variance);
        tvDiff1.setText(Utils.formatNumber(Double.toString(standardDeviation)));
    }


    @SuppressLint("SetTextI18n")
    private void calculateSum(List<BigInteger> integerList) {
        if (integerList.isEmpty()) {
            tvSum.setText("");
            return;
        }
        BigInteger sum = integerList.stream()
                .reduce(BigInteger.ZERO, BigInteger::add);
        try {
            tvSum.setText(Utils.formatNumber(sum.toString()));
        } catch (Exception e) {
            tvSum.setText("");
        }
    }
}

package com.yangdai.calc.main.toolbox.functions.algebra;

import static com.yangdai.calc.utils.Utils.closeKeyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yangdai.calc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 30415
 */
public class Adapter extends RecyclerView.Adapter<Adapter.viewHolder> {
    private final List<Item> list;
    private final TextWatcher textWatcher;
    private final List<viewHolder> viewHolderList;
    private final Context context;

    public Adapter(Context context, List<Item> list, TextWatcher textWatcher) {
        this.list = list;
        this.textWatcher = textWatcher;
        viewHolderList = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new viewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.algebra_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.setId("(" + (position + 1) + ")");
        holder.setText(list.get(position).content());
        holder.editText.addTextChangedListener(textWatcher);
        holder.editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard((Activity) context);
                holder.editText.clearFocus();
                return true;
            }
            return false;
        });
        viewHolderList.add(holder);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    public void remove(int position) {
        list.remove(position);
        viewHolderList.remove(position);
        for (int i = 0; i < viewHolderList.size(); i++) {
            viewHolderList.get(i).setId("(" + (i + 1) + ")");
        }
        notifyItemRemoved(position);
    }

    public void add() {
        list.add(new Item(""));
        notifyItemInserted(list.size());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public List<String> getAllInput() {
        List<String> editTextTextList = new ArrayList<>();
        for (viewHolder holder : viewHolderList) {
            editTextTextList.add(holder.getText());
        }
        return editTextTextList;
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final EditText editText;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_id);
            editText = itemView.findViewById(R.id.content);
        }

        public String getText() {
            return editText.getText().toString();
        }

        public void setText(String content) {
            editText.setText(content);
        }

        public void setId(String id) {
            textView.setText(id);
        }
    }
}


package com.yangdai.calc.main.toolbox.functions.algebra;

/**
 * @author 30415
 */
public record Item(String content) {
}


