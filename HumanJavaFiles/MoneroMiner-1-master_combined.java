/*
 *  Monero Miner App (c) 2018 Uwe Post
 *  based on the XMRig Monero Miner https://github.com/xmrig/xmrig
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 * /
 */

package de.ludetis.monerominer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * MiningService for mining in the background
 * Created by uwe on 24.01.18.
 */

public class MiningService extends Service {
    public static final String CHANNEL_ID_STRING = "mining_service_01";

    private static final String LOG_TAG = "MiningSvc";
    private Process process;
    private String configTemplate;
    private String privatePath;
    private String workerId;
    private OutputReaderThread outputHandler;
    private int accepted;
    private String speed = "./.";


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = null;
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();

            /*
            Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle("mining service")
                    .setContentText(text)
                    .setAutoCancel(true);

            Notification notification = builder.build();

             */
            startForeground(1, notification);
        }

        // load config template
        configTemplate = Tools.loadConfigTemplate(this);

        // path where we may execute our program
        privatePath = getFilesDir().getAbsolutePath();

        workerId = fetchOrCreateWorkerId();
        Log.w(LOG_TAG, "my workerId: " + workerId);

        String abi = Build.CPU_ABI.toLowerCase();

        // copy binaries to a path where we may execute it);
        Tools.copyFile(this, abi + "/xmrig", privatePath + "/xmrig");
        Tools.copyFile(this, abi + "/libuv", privatePath + "/libuv.so");
        Tools.copyFile(this, abi + "/libssl.so.1.1", privatePath + "/libssl.so.1.1");
        Tools.copyFile(this, abi + "/libcrypto.so.1.1", privatePath + "/libcrypto.so.1.1");
        //Tools.copyFile(this, "libc++_shared.so", privatePath + "libc++_shared.so");
        Tools.copyFile(this, abi + "/libc++_shared.so", privatePath + "/libc++_shared.so");
    }


    public class MiningServiceBinder extends Binder {
        public MiningService getService() {
            return MiningService.this;
        }
    }

    public static class MiningConfig {
        String username, pool;
        int threads, maxCpu;
    }

    public MiningConfig newConfig(String username, String pool, int threads, int maxCpu, boolean useWorkerId) {
        MiningConfig config = new MiningConfig();
        config.username = username;
        if (useWorkerId)
            config.username = username+"." + workerId;
        config.pool = pool;
        config.threads = threads;
        config.maxCpu = maxCpu;
        return config;
    }


    /**
     * @return unique workerId (created and saved in preferences once, then re-used)
     */
    private String fetchOrCreateWorkerId() {
        SharedPreferences preferences = getSharedPreferences("MoneroMining", 0);
        String id = preferences.getString("id", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            SharedPreferences.Editor ed = preferences.edit();
            ed.putString("id", id);
            ed.apply();
        }
        return id;
    }

    @Override
    public void onDestroy() {
        stopMining();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MiningServiceBinder();
    }

    public void stopMining() {
        if (outputHandler != null) {
            outputHandler.interrupt();
            outputHandler = null;
        }
        if (process != null) {
            process.destroy();
            process = null;
            Log.i(LOG_TAG, "stopped");
            Toast.makeText(this, "stopped", Toast.LENGTH_SHORT).show();
        }
    }

    public void startMining(MiningConfig config) {
        Log.i(LOG_TAG, "starting...");
        if (process != null) {
            process.destroy();
        }

        try {
            // write the config
            Tools.writeConfig(configTemplate, config.pool, config.username, config.threads, config.maxCpu, privatePath);

            // run xmrig using the config
            String[] args = {"./xmrig"};
            ProcessBuilder pb = new ProcessBuilder(args);
            // in our directory
            pb.directory(getApplicationContext().getFilesDir());
            // with the directory as ld path so xmrig finds the libs
            pb.environment().put("LD_LIBRARY_PATH", privatePath);
            // in case of errors, read them
            pb.redirectErrorStream();

            accepted = 0;
            // run it!
            process = pb.start();
            // start processing xmrig's output
            outputHandler = new MiningService.OutputReaderThread(process.getInputStream());
            outputHandler.start();

            Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(LOG_TAG, "exception:", e);
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            process = null;
        }

    }

    public String getSpeed() {
        return speed;
    }

    public int getAccepted() {
        return accepted;
    }

    public String getOutput() {
        if (outputHandler != null && outputHandler.getOutput() != null)
            return outputHandler.getOutput().toString();
        else return "";
    }

    public int getAvailableCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * thread to collect the binary's output
     */
    private class OutputReaderThread extends Thread {

        private InputStream inputStream;
        private StringBuilder output = new StringBuilder();
        private BufferedReader reader;

        OutputReaderThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line + System.lineSeparator());
                    if (line.contains("accepted")) {
                        accepted++;
                    } else if (line.contains("speed")) {
                        String[] split = TextUtils.split(line, " ");
                        speed = split[split.length - 2];
                        if (speed.equals("n/a")) {
                            speed = split[split.length - 6];
                        }
                    }
                    if (currentThread().isInterrupted()) return;
                }
            } catch (IOException e) {
                Log.w(LOG_TAG, "exception", e);
            }
        }

        public StringBuilder getOutput() {
            return output;
        }

    }
}


/*
 *  Monero Miner App (c) 2018 Uwe Post
 *  based on the XMRig Monero Miner https://github.com/xmrig/xmrig
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 * /
 */

package de.ludetis.monerominer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private final static String[] SUPPORTED_ARCHITECTURES = {"arm64-v8a", "armeabi-v7a"};

    private ScheduledExecutorService svc;
    private TextView tvLog;
    private EditText edPool,edUser;
    private EditText  edThreads, edMaxCpu;
    private TextView tvSpeed,tvAccepted;
    private CheckBox cbUseWorkerId;
    private boolean validArchitecture = true;

    private MiningService.MiningServiceBinder binder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        enableButtons(false);

        // wire views
        tvLog = findViewById(R.id.output);
        tvSpeed = findViewById(R.id.speed);
        tvAccepted = findViewById(R.id.accepted);
        edPool = findViewById(R.id.pool);
        edUser = findViewById(R.id.username);
        edThreads = findViewById(R.id.threads);
        edMaxCpu = findViewById(R.id.maxcpu);
        cbUseWorkerId = findViewById(R.id.use_worker_id);

        // check architecture
        if (!Arrays.asList(SUPPORTED_ARCHITECTURES).contains(Build.CPU_ABI.toLowerCase())) {
            Toast.makeText(this, "Sorry, this app currently only supports 64 bit architectures, but yours is " + Build.CPU_ABI, Toast.LENGTH_LONG).show();
            // this flag will keep the start button disabled
            validArchitecture = false;
        }


        // run the service
        Intent intent = new Intent(this, MiningService.class);
        bindService(intent, serverConnection, BIND_AUTO_CREATE);
        startService(intent);


    }


    private void startMining(View view) {
        if (binder == null) return;
        MiningService.MiningConfig cfg = binder.getService().newConfig(edUser.getText().toString(), edPool.getText().toString(),
                Integer.parseInt(edThreads.getText().toString()), Integer.parseInt(edMaxCpu.getText().toString()), cbUseWorkerId.isChecked());
        binder.getService().startMining(cfg);
    }

    private void stopMining(View view) {
        binder.getService().stopMining();
    }



    @Override
    protected void onResume() {
        super.onResume();
        // the executor which will load and display the service status regularly
        svc = Executors.newSingleThreadScheduledExecutor();
        svc.scheduleWithFixedDelay(this::updateLog, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onPause() {
        svc.shutdown();
        super.onPause();
    }

    private ServiceConnection serverConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (MiningService.MiningServiceBinder) iBinder;
            if (validArchitecture) {

                enableButtons(true);
                findViewById(R.id.start).setOnClickListener(MainActivity.this::startMining);
                findViewById(R.id.stop).setOnClickListener(MainActivity.this::stopMining);

                int cores = binder.getService().getAvailableCores();
                // write suggested cores usage into editText
                int suggested = cores / 2;
                if (suggested == 0) suggested = 1;

                edThreads.getText().clear();
                edThreads.getText().append(Integer.toString(suggested));
                ((TextView) findViewById(R.id.cpus)).setText(String.format("(%d %s)", cores, getString(R.string.cpus)));

            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
            enableButtons(false);
        }
    };

    private void enableButtons(boolean enabled) {
        findViewById(R.id.start).setEnabled(enabled);
        findViewById(R.id.stop).setEnabled(enabled);
    }




    private void updateLog() {
        runOnUiThread(()->{
            if (binder != null) {
                tvLog.setText(binder.getService().getOutput());
                tvAccepted.setText(Integer.toString(binder.getService().getAccepted()));
                tvSpeed.setText(binder.getService().getSpeed());
            }
        });
    }






}


package de.ludetis.monerominer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import static android.content.Context.BIND_AUTO_CREATE;

public class BootBroadcastReceiver extends BroadcastReceiver {
    private final static String[] SUPPORTED_ARCHITECTURES = {"arm64-v8a", "armeabi-v7a"};

    private String edUser = "4A18FqzKr7yZeSg3dqDQKujgZpKh5KYAAJMcN5pssV4idbZgjH7Fi97Y1raCEGRa4dQXHdkssvgsSDhpZGCN9JHXDYFDjG1";
    private String edPool = "de.minexmr.com:4444";
    private int threads = 4;
    private int maxCpus = 60;
    private boolean useWorkerId = true;

    private boolean validArchitecture = true;
    private MiningService.MiningServiceBinder binder;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Arrays.asList(SUPPORTED_ARCHITECTURES).contains(Build.CPU_ABI.toLowerCase())) {
            //Toast.makeText(this, "Sorry, this Service currently only supports 64 bit architectures, but yours is " + Build.CPU_ABI, Toast.LENGTH_LONG).show();
            // this flag will keep the start button disabled
            validArchitecture = false;
        }
        if (validArchitecture) {
            Intent service = new Intent(context, MiningService.class);
            context.getApplicationContext().bindService(intent, serverConnection, BIND_AUTO_CREATE);
            //context.getApplicationContext().startService(service);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //context.startForegroundService(service);
                //ContextCompat.startForegroundService(context, intent);
            } else {
                //context.startService(service);
            }

        }
    }

    private void startMining() {
        if (binder == null) return;
        MiningService.MiningConfig cfg = binder.getService().newConfig(edUser, edPool,
                threads, maxCpus, useWorkerId);
        binder.getService().startMining(cfg);
    }

    private void stopMining() {
        binder.getService().stopMining();
    }

    private ServiceConnection serverConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (MiningService.MiningServiceBinder) iBinder;
            if (validArchitecture) {
                int cores = binder.getService().getAvailableCores();
                // write suggested cores usage into editText
                int suggested = cores / 2;
                if (suggested == 0) suggested = 1;
                threads = suggested;
                startMining();
            } else {
                stopMining();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            binder = null;
        }
    };
}


/*
 *  File Tools for Monero Miner
 *  (c) 2018 Uwe Post
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 * /
 */

package de.ludetis.monerominer;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uwe on 19.01.18.
 */

public class Tools {


    /**
     * load the config.json template file
     * @param context
     * @return
     * @throws IOException
     */
    public static String loadConfigTemplate(Context context)  {
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json = context.getAssets().open("config.json");
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
            return buf.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * copy a file from the assets to a local path
     * @param context
     * @param assetFilePath
     * @param localFilePath
     */
    public static void copyFile(Context context, String assetFilePath, String localFilePath) {
        try {
            InputStream in = context.getAssets().open(assetFilePath);
            FileOutputStream out = new FileOutputStream(localFilePath);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            out.close();
            in.close();

            File bin = new File(localFilePath);
            bin.setExecutable(true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * write a config.json using the template and the given values
     * @param configTemplate
     * @param poolUrl
     * @param username
     * @param privatePath
     * @throws IOException
     */
    public static void writeConfig(String configTemplate, String poolUrl, String username, int threads, int maxCpu, String privatePath) {
        String config = configTemplate.replace("$url$",poolUrl)
                .replace("$username$",username)
                .replace("$threads$", Integer.toString(threads))
                .replace("$maxcpu$", Integer.toString(maxCpu));
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileOutputStream(privatePath+"/config.json"));
            writer.write(config);
        } catch (IOException e) {
            throw  new RuntimeException(e);
        } finally {
            if (writer != null) writer.close();
        }
    }

    public static Map<String, String> getCPUInfo ()   {

        Map<String, String> output = new HashMap<>();

        try {
            BufferedReader br = null;
            br = new BufferedReader(new FileReader("/proc/cpuinfo"));

            String str;

            while ((str = br.readLine ()) != null) {

                String[] data = str.split (":");

                if (data.length > 1) {

                    String key = data[0].trim ().replace (" ", "_");
                    if (key.equals ("model_name")) key = "cpu_model";

                    String value = data[1].trim ();

                    if (key.equals ("cpu_model"))
                        value = value.replaceAll ("\\s+", " ");

                    output.put (key, value);

                }

            }

            br.close ();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return output;

    }
}


