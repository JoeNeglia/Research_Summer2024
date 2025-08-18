package com.doxart.myvpn;

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
        assertEquals("com.doxart.ivpn", appContext.getPackageName());
    }
}

package com.doxart.myvpn;

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

package com.doxart.myvpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.Util.SharePrefs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

import de.blinkt.openvpn.OpenVpnApi;

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ServerModel server = SharePrefs.getInstance(context).getServer();

        File file = new File(context.getFilesDir().toString() + "/" + server.getOvpn());

        try {
            InputStream conf;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                conf = Files.newInputStream(file.toPath());
            } else conf = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(conf);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder config = new StringBuilder();
            String line;

            while (true) {
                line = br.readLine();
                if (line == null) break;
                config.append(line).append("\n");
            }

            br.readLine();

            if (server.getOvpn() != null & server.getCountry() != null & server.getOvpnUserName() != null & server.getOvpnUserPassword() != null) {
                OpenVpnApi.startVpn(context, config.toString(), server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());
            }

        } catch (IOException | RemoteException e) {
            Log.d("AutoConnectException", "startVpn: " + e);
        }
    }
}


package com.doxart.myvpn;

import android.app.Application;

import com.adapty.Adapty;
import com.google.android.gms.ads.MobileAds;

public class MyVPN extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        Adapty.activate(this, "CHANGE WITH YOUR ADAPTY APP ID");
    }
}


package com.doxart.myvpn.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.doxart.myvpn.Activities.ConnectionReportActivity;
import com.doxart.myvpn.Activities.MainActivity;
import com.doxart.myvpn.DB.ServerDB;
import com.doxart.myvpn.Interfaces.ChangeServer;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.RetroFit.GetIPDataService;
import com.doxart.myvpn.RetroFit.MyIP;
import com.doxart.myvpn.RetroFit.RetrofitClient;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.Util.VPNCountdownTimer;
import com.doxart.myvpn.databinding.FragmentVPNBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.blinkt.openvpn.OpenVpnApi;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VPNFragment extends Fragment implements ChangeServer {

    private static VPNFragment instance;

    FragmentVPNBinding b;
    Context context;

    private ChangeServer changeServer;
    private ServerModel server;
    private List<ServerModel> serverList;
    boolean vpnRunning = false;

    private final String TAG = "VPNApp";

    private InterstitialAd mInterstitialAd;

    private boolean premium = false;

    private SharePrefs sharePrefs;
    private MyIP myIP;
    int adDelay = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (instance == null) instance = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentVPNBinding.inflate(inflater, container, false);
        context = getContext();

        sharePrefs = new SharePrefs(context);

        premium = sharePrefs.getBoolean("premium");
        adDelay = sharePrefs.getInt("delayTimeBetweenAds");

        if (!premium) buildInterstitial();

        getIPLocation();
        init();

        return b.getRoot();
    }

    private void getIPLocation() {
        GetIPDataService service = RetrofitClient.getRetrofitInstance().create(GetIPDataService.class);

        Call<MyIP> call = service.getMyIP();

        call.enqueue(new Callback<MyIP>() {
            @Override
            public void onResponse(@NonNull Call<MyIP> call, @NonNull Response<MyIP> response) {
                myIP = response.body();

                if (myIP != null) b.myIpTxt.setText(myIP.getQuery());
            }
            @Override
            public void onFailure(@NonNull Call<MyIP> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t);
                Toast.makeText(context, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buildInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context, "CHANGE WITH YOUR ADMOB INTERSTITIAL ID", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    private void showInterstitial(boolean forConnect) {
        long lastAd = sharePrefs.getLastAd();

        Log.d(TAG, "showInterstitial: now: " + new Date().getTime() + " then: " + lastAd);

        if (new Date().getTime() < lastAd) {
            goConnectionReport(forConnect);
            Log.d(TAG, "showInterstitial: getTime");
            return;
        }

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d(TAG, "showInterstitial: fail");
                    goConnectionReport(forConnect);
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    sharePrefs.putLong("lastAd", new Date().getTime() + (long) adDelay * 60 * 1000);
                    goConnectionReport(forConnect);
                    buildInterstitial();
                    Log.d(TAG, "showInterstitial: dismiss");

                }
            });

            mInterstitialAd.show(requireActivity());
        } else {
            goConnectionReport(forConnect);
            Log.d(TAG, "showInterstitial: null");
            buildInterstitial();
        }

    }

    private void goConnectionReport(boolean isConnection) {
        if (server != null) {
            Intent i = new Intent(context, ConnectionReportActivity.class);
            i.putExtra("isConnection", isConnection);
            if (isConnection) {
                i.putExtra("sessionM", m);
                i.putExtra("sessionS", s);
            }
            startActivity(i);
            ConnectionReportActivity.server = server;
        }
    }

    int initTry = 0;
    private void init() {
        changeServer = this;

        serverList = new ArrayList<>();
        List<ServerModel> nativeList = ServerDB.getInstance().getServerList();

        if (nativeList != null) {
            for (ServerModel sv : nativeList) {
                if (sv.getLatency() > 0) serverList.add(sv);
            }

            makeServerList();
        } else {
            initTry++;

            if (initTry < 3) init();
            else {
                ServerDB.getInstance().getServers(context, () -> {
                    initTry = 0;
                    init();
                });
            }
        }

        b.vpnBtn.getRoot().setOnClickListener(v -> {
            if (vpnRunning) {
                stopVpn();
            } else {
                prepareVpn();
            }
        });

        b.countryLay.getRoot().setOnClickListener(v -> {
            if (vpnRunning) Toast.makeText(context, getString(R.string.vpn_is_running), Toast.LENGTH_SHORT).show();
            else MainActivity.getInstance().openServerList();

        });

        isServiceRunning();
        VpnStatus.initLogCache(context.getCacheDir());
    }

    private void makeServerList() {
        serverList.sort(Comparator.comparing(ServerModel::getLatency));

        if (serverList != null) {
            if (serverList.size() > 0) {
                if (!sharePrefs.getBoolean("premium")) {
                    for (ServerModel sv : serverList) {
                        if (!sv.isPremium()) {
                            server = sv;
                            break;
                        }
                    }
                } else server = serverList.get(0);
            }
        } else {
            return;
        }

        if (server != null) {
            sharePrefs.putServer(server);
            updateCurrentServerLay(server);
        }
    }

    public void changeServer(ServerModel index) {
        changeServer.newServer(index);
    }

    private void prepareVpn() {
        if (!vpnRunning) {
            if (getInternetStatus()) {
                Intent intent = VpnService.prepare(context);

                if (intent != null) resultLauncher.launch(intent);
                else startVpn();

                status("connecting");
            } else showToast("You have no internet connection!!");
        } else if (stopVpn()) showToast("Successfully disconnected.");
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) startVpn();
        else showToast("Permission Deny!!");
    });

    public boolean stopVpn() {
        try {
            b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.orange), PorterDuff.Mode.SRC_ATOP);
            b.vpnStatus.setText(getString(R.string.disconnecting));
            b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.orange));
            OpenVPNThread.stop();

            if (!premium) {
                showInterstitial(false);
            } else goConnectionReport(false);

            status("connect");

            vpnRunning = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean getInternetStatus() {
        return Utils.checkConnection(context);
    }

    public void isServiceRunning() {
        setStatus(OpenVPNService.getStatus());
    }

    private void startVpn() {
        if (server != null) {
            File file = new File(context.getFilesDir().toString() + "/" + server.getOvpn());

            Log.d(TAG, "startVpn: " + server.getCountry() + " ovpn: " + server.getOvpn() + " passid: " + server.getOvpnUserPassword() + " " + server.getOvpnUserName());

            try {
                InputStream conf;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    conf = Files.newInputStream(file.toPath());
                } else conf = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(conf);
                BufferedReader br = new BufferedReader(isr);
                StringBuilder config = new StringBuilder();
                String line;

                while (true) {
                    line = br.readLine();
                    if (line == null) break;
                    config.append(line).append("\n");
                }

                br.readLine();

                OpenVpnApi.startVpn(context, config.toString(), server.getCountry(), server.getOvpnUserName(), server.getOvpnUserPassword());

                if (!premium) {
                    showInterstitial(true);
                } else goConnectionReport(true);

                b.vpnStatus.setText(getString(R.string.starting));
                vpnRunning = true;
            } catch (IOException | RemoteException e) {
                Log.d(TAG, "startVpn: " + e);
                status("connect");
            }
        }
    }

    public void setStatus(String connectionState) {
        if (connectionState!= null)
            switch (connectionState) {
                case "DISCONNECTED":
                    status("disconnected");
                    vpnRunning = false;
                    OpenVPNService.setDefaultStatus();

                    context.stopService(new Intent(context, VPNCountdownTimer.class));

                    break;
                case "CONNECTED":
                    vpnRunning = true;
                    status("connected");
                    context.startService(new Intent(context, VPNCountdownTimer.class));

                    break;
                case "WAIT":
                    status("connecting");
                    break;
                case "AUTH":
                    status("auth");
                    break;
                case "RECONNECTING":
                    status("RECONNECTING");

                    break;
                case "NONETWORK":
                    status("nonetwork");
                    vpnRunning = false;
                    break;
            }
    }

    public void status(String status) {
        switch (status) {
            case "disconnected":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.disconnected));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
                if (myIP != null) b.myIpTxt.setText(myIP.getQuery());
                b.myIpTxt.setTextColor(ContextCompat.getColor(context, R.color.blat));
                if (LocationFragment.getInstance() != null) LocationFragment.getInstance().getIPLocation();
                break;
            case "connecting":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.orange), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.connecting));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.orange));
                break;
            case "connected":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.green), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.connected));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
                if (myIP != null) b.myIpTxt.setText(server.getIpv4());
                b.myIpTxt.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                if (LocationFragment.getInstance() != null) LocationFragment.getInstance().getIPLocation();
                break;
            case "tryDifferentServer":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.try_different_server));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
                break;
            case "loading":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.loading));
                break;
            case "invalidDevice":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.invalid_device));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
                break;
            case "authenticationCheck":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.authenticating));
                b.durationTxt.setText("");
                break;
            case "nonetwork":
                b.vpnBtn.switchBtnBg.getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                b.vpnStatus.setText(getString(R.string.disconnected));
                b.vpnStatus.setText(getString(R.string.tap_to_connect));
                b.vpnStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
                b.durationTxt.setText("");
                break;
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                setStatus(intent.getStringExtra("state"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Objects.requireNonNull(intent.getAction()).equals("usage_data_updated")) {
                int m = intent.getIntExtra("usageMinutes", 0);
                int s = intent.getIntExtra("usageSeconds", 0);
                updateConnectionStatus(m, s);
            }
        }
    };

    int m = 0;
    int s = 0;
    public void updateConnectionStatus(int mm, int ss) {
        int totalSeconds = mm * 60 + ss;
        m = totalSeconds / 60;
        s = totalSeconds % 60;
        String formattedTime = String.format(Locale.getDefault(), "%02d.%02d", m, s);
        if (vpnRunning) {
            b.durationTxt.setText(formattedTime);
        }
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void updateCurrentServerLay(ServerModel m) {
        if (m != null) {
            if (m.getFlagUrl() != null) Glide.with(context).load("https://flagcdn.com/h80/" + m.getFlagUrl() + ".png").centerCrop().into(b.countryLay.cFlagImg);
            b.countryLay.cCountryTxt.setText(m.getCountry());

            Utils.setSignalView(context, b.countryLay.s1, b.countryLay.s2, b.countryLay.s3, m.getLatency());
        }
    }

    @Override
    public void newServer(ServerModel server) {
        this.server = server;
        updateCurrentServerLay(server);

        if (vpnRunning) {
            stopVpn();
        }

        prepareVpn();
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, new IntentFilter("usage_data_updated"));
        if (sharePrefs != null) premium = sharePrefs.getBoolean("premium");
        super.onResume();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public static VPNFragment getInstance() {
        return instance;
    }
}

package com.doxart.myvpn.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.doxart.myvpn.R;
import com.doxart.myvpn.RetroFit.GetIPDataService;
import com.doxart.myvpn.RetroFit.MyIP;
import com.doxart.myvpn.RetroFit.RetrofitClient;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.databinding.FragmentLocationBinding;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;

import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationFragment extends Fragment {
    private static LocationFragment instance;
    private MyIP myIP;

    public static LocationFragment getInstance() {
        return instance;
    }

    FragmentLocationBinding b;
    Context context;

    IMapController mapController;
    private final String TAG = "LOCATION_PROCESS";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentLocationBinding.inflate(inflater, container, false);
        context = getContext();

        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        init();

        return b.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        b.mapView.setTileSource(TileSourceFactory.MAPNIK);

        mapController = b.mapView.getController();

        b.mapView.setOnTouchListener((v, event) -> {
            b.getRoot().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        if (!SharePrefs.getInstance(context).getBoolean("premium")) {
            if (SharePrefs.getInstance(context).getBoolean("showBannerAds")) loadAds();
            else b.myTemplate.setVisibility(View.GONE);
        } else b.myTemplate.setVisibility(View.GONE);

        b.refreshBT.setOnClickListener(v -> {
            first = false;
            getIPLocation();
        });

        getIPLocation();
    }

    private void loadAds() {
        AdLoader adLoader = new AdLoader.Builder(context, "CHANGE WITH YOUR ADMOB NATIVE ID")
                .forNativeAd(nativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();
                    TemplateView template = b.myTemplate;
                    template.setStyles(styles);
                    template.setNativeAd(nativeAd);
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    boolean first = false;

    public void getIPLocation() {
        GetIPDataService service = RetrofitClient.getRetrofitInstance().create(GetIPDataService.class);

        Call<MyIP> call = service.getMyIP();

        call.enqueue(new Callback<MyIP>() {
            @Override
            public void onResponse(@NonNull Call<MyIP> call, @NonNull Response<MyIP> response) {
                myIP = response.body();
                if (!first) {
                    setIPLocation();
                    first = true;
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyIP> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }

    public void setIPLocation() {
        if (myIP != null) {
            mapController.animateTo(new GeoPoint(myIP.getLat(), myIP.getLon()));
            mapController.setZoom(15f);

            b.myIpTxt.setText(myIP.getQuery());

            DecimalFormat decimalFormat = new DecimalFormat("##.#######");

            b.latTxt.setText(String.format(getString(R.string.lat_d), decimalFormat.format(myIP.getLat())));
            b.lngTxt.setText(String.format(getString(R.string.lng_d), decimalFormat.format(myIP.getLon())));

            b.regionTxt.setText(myIP.getRegion());
            b.cityTxt.setText(myIP.getCity());
            b.countryTxt.setText(myIP.getCountry());
            b.ispTxt.setText(myIP.getIsp());
        } else getIPLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        b.mapView.onResume();
        setIPLocation();
    }

    @Override
    public void onPause() {
        b.mapView.onPause();
        super.onPause();
    }
}

package com.doxart.myvpn.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import com.doxart.myvpn.Activities.PaywallActivity;
import com.doxart.myvpn.Adapter.UsageAdapter;
import com.doxart.myvpn.BootCompleteReceiver;
import com.doxart.myvpn.DB.Usage;
import com.doxart.myvpn.DB.UsageViewModel;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.databinding.FragmentSettingsBinding;

import java.util.List;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SETTINGS";
    FragmentSettingsBinding b;
    Context context;

    SharePrefs sharePrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSettingsBinding.inflate(inflater, container, false);
        context = getContext();

        sharePrefs = new SharePrefs(context);

        getUsage();
        init();

        return b.getRoot();
    }

    private void getUsage() {
        UsageAdapter adapter = new UsageAdapter(context, () -> b.usageRecycler.smoothScrollToPosition(Gravity.END));

        b.usageRecycler.setHasFixedSize(true);
        b.usageRecycler.setAdapter(adapter);

        b.usageRecycler.smoothScrollToPosition(Gravity.END);

        UsageViewModel usageViewModel = new ViewModelProvider(requireActivity()).get(UsageViewModel.class);
        LiveData<List<Usage>> allUsages = usageViewModel.getAllUsages();

        allUsages.observe(requireActivity(), usages -> {
            adapter.setUsageList(usages);

            if (usages.isEmpty()) {
                b.noStatistics.setVisibility(View.VISIBLE);
                b.usageRecycler.setVisibility(View.GONE);
            }
            else {
                b.noStatistics.setVisibility(View.GONE);
                b.usageRecycler.setVisibility(View.VISIBLE);
            }

            b.usageRecycler.smoothScrollToPosition(Gravity.END);
        });
    }

    private void init() {
        b.dynamicBgSwitch.setChecked(sharePrefs.isDynamicBackground());
        b.dynamicBgSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> sharePrefs.setDynamicBackground(isChecked));

        b.autoConnectSwitch.setChecked(sharePrefs.isAutoConnect());
        b.autoConnectSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (sharePrefs.getBoolean("premium")) {
                sharePrefs.setAutoConnect(isChecked);
                setAutoConnect(isChecked);
            } else {
                buttonView.setChecked(sharePrefs.isAutoConnect());
                startActivity(new Intent(context, PaywallActivity.class).putExtra("timer", 0));
            }
        });
    }

    private void setAutoConnect(boolean on) {
        int flag = (on ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED);

        ComponentName comp = new ComponentName(context, BootCompleteReceiver.class);

        context.getPackageManager().setComponentEnabledSetting(comp, flag, PackageManager.DONT_KILL_APP);
    }
}

package com.doxart.myvpn.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.R;
import com.doxart.myvpn.databinding.FragmentSpeedTestBinding;
import com.ekn.gruzer.gaugelibrary.Range;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import java.text.DecimalFormat;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SpeedTestFragment extends Fragment implements ISpeedTestListener {

    FragmentSpeedTestBinding b;
    Context context;

    private long startTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentSpeedTestBinding.inflate(inflater, container, false);
        context = getContext();

        init();

        return b.getRoot();
    }

    private void loadAds() {
        AdLoader adLoader = new AdLoader.Builder(context, "CHANGE WITH YOUR ADMOB NATIVE ID")
                .forNativeAd(nativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();
                    TemplateView template = b.myTemplate;
                    template.setStyles(styles);
                    template.setNativeAd(nativeAd);
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void init() {
        setupGauge();

        if (!SharePrefs.getInstance(context).getBoolean("premium")) {
            if (SharePrefs.getInstance(context).getBoolean("showBannerAds")) loadAds();
            else b.myTemplate.setVisibility(View.GONE);
        } else b.myTemplate.setVisibility(View.GONE);

        b.startTestBT.setOnClickListener(v -> {
            b.startTestBT.setVisibility(View.GONE);
            new Thread(this::getNetSpeed).start();
        });
    }

    private void setupGauge() {
        Range range = new Range();
        range.setColor(ContextCompat.getColor(context, R.color.red));
        range.setFrom(0d);
        range.setTo(50d);

        Range range1 = new Range();
        range1.setColor(ContextCompat.getColor(context, R.color.orange));
        range1.setFrom(50d);
        range1.setTo(100d);

        Range range2 = new Range();
        range2.setColor(ContextCompat.getColor(context, R.color.green));
        range2.setFrom(100d);
        range2.setTo(150d);

        b.speedGauge.addRange(range);
        b.speedGauge.addRange(range1);
        b.speedGauge.addRange(range2);

        b.speedGauge.setMinValue(0d);
        b.speedGauge.setMaxValue(150d);
        b.speedGauge.setValue(0d);

        b.speedGauge.setValueColor(ContextCompat.getColor(context, android.R.color.transparent));
    }

    int test = 0;
    private void getNetSpeed() {
        test = 99;
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();

        b.speedGauge.setValueColor(ContextCompat.getColor(context, R.color.colorWhite));

        startTime = System.currentTimeMillis();
        speedTestSocket.addSpeedTestListener(this);
        speedTestSocket.startDownload("http://ipv4.appliwave.testdebit.info/50M.iso", 100);
    }

    @Override
    public void onCompletion(SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;
        getActivity().runOnUiThread(() -> {
            b.speedGauge.setValue(Math.floor(r));

            b.startTestBT.setVisibility(View.VISIBLE);
            b.speedGauge.setValueColor(ContextCompat.getColor(context, android.R.color.transparent));

            b.speedTxt.setText(String.format("%s MB/s", new DecimalFormat("##").format(r)));
            b.latencyTxt.setText(String.format("%s ms", (System.currentTimeMillis() - startTime) / 600));
            b.startTestBT.setText(getString(R.string.start));
        });
        Log.d("AGDDGSGSGSDFSDG", "onCompletion: " + r);
    }

    @Override
    public void onProgress(float percent, SpeedTestReport report) {
        float r = report.getTransferRateBit().floatValue() / 1000000;
        getActivity().runOnUiThread(() -> b.speedGauge.setValue(Math.floor(r)));
        Log.d("AGDDGSGSGSDFSDG", "onProgress: " + r / 1000000);
    }

    @Override
    public void onError(SpeedTestError speedTestError, String errorMessage) {
        Log.d("2AGDDGSGSGSDFSDG", "onError: " + errorMessage);
    }
}

package com.doxart.myvpn.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doxart.myvpn.Activities.MainActivity;
import com.doxart.myvpn.Adapter.ServerListRVAdapter;
import com.doxart.myvpn.DB.ServerDB;
import com.doxart.myvpn.Interfaces.NavItemClickListener;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.FragmentServersBinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServersFragment extends Fragment {

    FragmentServersBinding b;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        b = FragmentServersBinding.inflate(inflater, container, false);
        context = getContext();

        adjustMargin();
        init();

        return b.getRoot();
    }

    private void adjustMargin() {
        final int statusBarHeight = Utils.getStatusBarHeight(context);
        final int navigationBarHeight = Utils.getNavigationBarHeight(context);

        int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        b.appbar.setPaddingRelative(0, statusBarHeight, 0, 0);
    }

    private void init() {
        NavItemClickListener navItemClickListener = (NavItemClickListener) context;

        if (navItemClickListener != null) {
            List<ServerModel> nativeList = ServerDB.getInstance().getServerList();
            List<ServerModel> adjustedList = new ArrayList<>();
            List<ServerModel> adjustedVipList = new ArrayList<>();

            for (ServerModel sv : nativeList) {
                if (sv.getLatency() > 0 & !sv.isPremium()) adjustedList.add(sv);
                if (sv.getLatency() > 0 & sv.isPremium()) adjustedVipList.add(sv);
            }

            adjustedList.sort(Comparator.comparing(ServerModel::getLatency));

            ServerListRVAdapter adapter = new ServerListRVAdapter(context, adjustedList, navItemClickListener, 0);
            ServerListRVAdapter adapter1 = new ServerListRVAdapter(context, adjustedVipList, navItemClickListener, 1);

            b.serverRecycler.setHasFixedSize(true);
            b.serverRecycler.setAdapter(adapter);

            b.vipServerRecycler.setHasFixedSize(true);
            b.vipServerRecycler.setAdapter(adapter1);

            b.closeBT.setOnClickListener(v -> MainActivity.getInstance().closeServerList());
        }
    }
}

package com.doxart.myvpn.Util;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.adapty.Adapty;
import com.adapty.errors.AdaptyError;
import com.adapty.models.AdaptyPaywall;
import com.adapty.models.AdaptyPaywallProduct;
import com.adapty.models.AdaptyViewConfiguration;
import com.adapty.ui.AdaptyPaywallInsets;
import com.adapty.ui.AdaptyPaywallView;
import com.adapty.ui.AdaptyUI;
import com.adapty.ui.listeners.AdaptyUiEventListener;
import com.adapty.utils.AdaptyResult;
import com.doxart.myvpn.Model.PaywallModel;

import java.util.List;

public class PaywallViewUtils {

    private static final PaywallViewUtils instance = new PaywallViewUtils();

    public String TAG = "PAYWALL_GETTER";

    public PaywallModel paywallHolder = new PaywallModel();

    public interface OnPaywallLoadFinishListener {
        void onFinish();
        void onError();
    }

    public OnPaywallLoadFinishListener paywallLoadFinishListener;

    public void setPaywallLoadFinishListener(OnPaywallLoadFinishListener listener) {
        paywallLoadFinishListener = listener;
    }

    public void getPaywallView(String pid) {
        Adapty.getPaywall(pid, "en", result -> {
            if (result instanceof AdaptyResult.Success) {
                AdaptyPaywall paywall = ((AdaptyResult.Success<AdaptyPaywall>) result).getValue();


                new Handler().postDelayed(() -> {
                    paywallHolder.setAdaptyPaywall(paywall);

                    getProducts(paywall);
                }, 0);
            } else if (result instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result).getError();

                Log.d(TAG, "getPaywallView: " + error);
                if (paywallLoadFinishListener != null) paywallLoadFinishListener.onError();
            }
        });
    }

    private void getProducts(AdaptyPaywall paywall) {
        Adapty.getPaywallProducts(paywall, result -> {
            if (result instanceof AdaptyResult.Success) {
                List<AdaptyPaywallProduct> products = ((AdaptyResult.Success<List<AdaptyPaywallProduct>>) result).getValue();

                new Handler().postDelayed(() -> {
                    paywallHolder.setProducts(products);

                    getViewConf(paywall);
                }, 0);

            } else if (result instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result).getError();
                // handle the error
                Log.d(TAG, "getProducts: " + error);
                if (paywallLoadFinishListener != null) paywallLoadFinishListener.onError();
            }
        });
    }

    public void getViewConf(AdaptyPaywall paywall) {
        Adapty.getViewConfiguration(paywall, "en", result1 -> {
            if (result1 instanceof AdaptyResult.Success) {
                AdaptyViewConfiguration viewConfiguration = ((AdaptyResult.Success<AdaptyViewConfiguration>) result1).getValue();
                Log.d(TAG, "createView: " + viewConfiguration.getTemplateId());

                new Handler().postDelayed(() -> {
                    paywallHolder.setViewConfiguration(viewConfiguration);

                    if (paywallLoadFinishListener != null) paywallLoadFinishListener.onFinish();
                }, 0);
            } else if (result1 instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result1).getError();
                Log.d(TAG, "makePRC: " + error);
                if (paywallLoadFinishListener != null) paywallLoadFinishListener.onError();
            }
        });
    }

    public AdaptyPaywallView createView(Activity activity, AdaptyUiEventListener adaptyUiEventListener) {
        Log.d(TAG, "createView: products " + paywallHolder.getProducts());

        Log.d(TAG, "createView: paywall " + paywallHolder.getAdaptyPaywall());
        Log.d(TAG, "createView: viewConfig " + paywallHolder.getViewConfiguration());

        if (paywallHolder != null & paywallHolder.getAdaptyPaywall() != null & paywallHolder.getViewConfiguration() != null & paywallHolder.getProducts() != null) {
            AdaptyPaywallView adaptyPaywallView = AdaptyUI.getPaywallView(
                    activity,
                    paywallHolder.getAdaptyPaywall(),
                    paywallHolder.getProducts(),
                    paywallHolder.getViewConfiguration(),
                    AdaptyPaywallInsets.of(10, 10),
                    adaptyUiEventListener,
                    adaptyPaywallProduct -> false
            );

            paywallHolder.setAdaptyPaywallView(adaptyPaywallView);

            Log.d(TAG, "createView: paywallView " + paywallHolder.getAdaptyPaywallView());

            return adaptyPaywallView;
        }

        return null;
    }

    public PaywallModel getPaywallHolder() {
        return paywallHolder;
    }

    public static PaywallViewUtils getInstance() {
        return instance;
    }
}


package com.doxart.myvpn.Util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.doxart.myvpn.Interfaces.OnAnswerListener;
import com.doxart.myvpn.R;
import com.doxart.myvpn.databinding.AskViewBinding;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static Dialog setProgress(Context context){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.progress_view);
        if (dialog.getWindow() != null) dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(false);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (dialog.getWindow() != null) {
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);

        return dialog;
    }

    public static void shareApp(Context context) {
        final String appPackageName = context.getPackageName();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=" + appPackageName);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void openAppInPlayStore(Context context) {
        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static double getNetSpeed(String urlStr) {
        double downloadSpeed = 0.0;
        try {
            URL url = new URL("https://www.google.com");
            URLConnection connection = url.openConnection();

            long startTime = System.currentTimeMillis();
            connection.connect();
            long endTime = System.currentTimeMillis();

            long fileSize = connection.getContentLength();

            if (fileSize > 0) {
                double downloadTime = (endTime - startTime) / 1000.0;
                downloadSpeed = (fileSize / downloadTime) * 8 / 1000000;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloadSpeed;
    }

    public static String getImgURL(int resourceId) {
        return Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + resourceId).toString();
    }

    public static void setSignalView(Context context, CardView s1, CardView s2, CardView s3, int latency) {
        int resGreen = ContextCompat.getColor(context, R.color.green);
        int resDark = ContextCompat.getColor(context, R.color.blat1);
        int resOrange = ContextCompat.getColor(context, R.color.orange);
        int resRed = ContextCompat.getColor(context, R.color.red);

        if (latency < 100) {
            s1.setCardBackgroundColor(resGreen);
            s2.setCardBackgroundColor(resGreen);
            s3.setCardBackgroundColor(resGreen);
        } else if (latency > 100 & latency < 300) {
            s1.setCardBackgroundColor(resOrange);
            s2.setCardBackgroundColor(resOrange);
            s3.setCardBackgroundColor(resDark);
        } else if (latency > 300) {
            s1.setCardBackgroundColor(resRed);
            s2.setCardBackgroundColor(resDark);
            s3.setCardBackgroundColor(resDark);
        } else {
            s1.setCardBackgroundColor(resDark);
            s2.setCardBackgroundColor(resDark);
            s3.setCardBackgroundColor(resDark);
        }
    }

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yy ", Locale.US);

    public static String getToday() {
        Date today = new Date();
        today.setTime(today.getTime()+100000);

        return dateFormat.format(today);
    }

    public static Dialog askQuestion(Context context, String title, String contain,
                                     String positive, String negative, String other,
                                     SpannableStringBuilder isSpanned, boolean showCheckImg,
                                     OnAnswerListener answerListener){
        View view = LayoutInflater.from(context).inflate(R.layout.ask_view, null);
        AskViewBinding b = AskViewBinding.bind(view);

        Dialog ask = new Dialog(context);
        ask.setContentView(b.getRoot());
        if (ask.getWindow() != null) ask.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ask.setCancelable(false);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (ask.getWindow() != null) {
            layoutParams.copyFrom(ask.getWindow().getAttributes());
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            ask.getWindow().setAttributes(layoutParams);
        }

        ask.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);

        b.askTitle.setText(HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT));

        if (showCheckImg) {
            b.checkImg.setVisibility(View.VISIBLE);
            b.askNegative.setVisibility(View.GONE);
        }

        if(!contain.isEmpty()) b.askContain.setText(Html.fromHtml(contain));
        else b.askContain.setText(isSpanned);

        if (!positive.isEmpty()) b.askPositive.setText(positive);
        if (!negative.isEmpty()) b.askNegative.setText(negative);
        if (!other.isEmpty()) {
            b.askOther.setVisibility(View.VISIBLE);
            b.askOther.setText(other);
        }
        else b.askOther.setVisibility(View.GONE);

        b.askPositive.setOnClickListener(v -> {
            ask.dismiss();
            if (answerListener != null) {
                answerListener.onPositive();
            }
        });

        b.askNegative.setOnClickListener(v -> {
            ask.dismiss();
            if (answerListener != null) {
                answerListener.onNegative();
            }
        });

        b.askOther.setOnClickListener(v -> {
            ask.dismiss();
            if (answerListener != null) {
                answerListener.onOther();
            }
        });
        return ask;
    }

    public static boolean checkConnection(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo != null && nInfo.isConnectedOrConnecting();
    }
}


package com.doxart.myvpn.Util;


import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        RemoteMessage.Notification notification = message.getNotification();

        if (notification == null) return;

        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();

        NotificationUtils notify = new NotificationUtils(this);

        if (text != null & title != null) {
            if (text.startsWith("@free") & !SharePrefs.getInstance(this).getBoolean("premium")) notify.sendNotification(title, text.replace("@free", ""), false);
            else if (text.startsWith("@all")) notify.sendNotification(title, text.replace("@all", ""), false);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }
}


package com.doxart.myvpn.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.doxart.myvpn.Model.ServerModel;

import java.util.UUID;

public class SharePrefs {
    public static String PREFERENCE = "DoxyVPNUser";


    private final Context ctx;
    public SharedPreferences sharedPreferences;
    private static SharePrefs instance;


    public SharePrefs(Context context) {
        this.ctx = context;
        sharedPreferences = context.getSharedPreferences(PREFERENCE, 0);
    }

    public static SharePrefs getInstance(Context ctx) {
        if (instance == null) {
            instance = new SharePrefs(ctx);
        }
        return instance;
    }

    public void putString(String key, String val) {
        sharedPreferences.edit().putString(key, val).apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public String getCollReference(String key) {
        return sharedPreferences.getString(key, "all-coll");
    }

    public void putInt(String key, Integer val) {
        sharedPreferences.edit().putInt(key, val).apply();
    }

    public void increaseInt(String key, Integer val){
        sharedPreferences.edit().putInt(key, sharedPreferences.getInt(key, 0) + val).apply();
    }

    public long getLong(String key){
        return sharedPreferences.getLong(key, 0);
    }

    public String getUid() {
        if (sharedPreferences.getString("uuid", "").isEmpty()) {
            sharedPreferences.edit().putString("uuid", UUID.randomUUID().toString()).apply();
        }

        return sharedPreferences.getString("uuid", "");
    }

    public long getLastAd() {
        return sharedPreferences.getLong("lastAd", 0);
    }

    public void putLong(String key, long val){
        sharedPreferences.edit().putLong(key, val).apply();
    }

    public void putBoolean(String key, Boolean val) {
        sharedPreferences.edit().putBoolean(key, val).apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public Boolean isDynamicBackground() {
        return sharedPreferences.getBoolean("dynamicBackground", true);
    }

    public void setDynamicBackground(Boolean val) {
        sharedPreferences.edit().putBoolean("dynamicBackground", val).apply();
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void clearSharePrefs() {
        sharedPreferences.edit().clear().apply();
    }

    public Boolean isAutoConnect() {
        return sharedPreferences.getBoolean("autoConnect", false);
    }

    public void setAutoConnect(Boolean val) {
        sharedPreferences.edit().putBoolean("autoConnect", val).apply();
    }

    public void putServer(ServerModel server) {
        if (server.getOvpn() == null & server.getCountry() != null) server.setOvpn(server.getCountry() + ".ovpn");
        if (server.getOvpn() != null & server.getCountry() != null & server.getOvpnUserName() != null & server.getOvpnUserPassword() != null) {
            sharedPreferences.edit().putString("serverOVPN", server.getOvpn()).apply();
            sharedPreferences.edit().putString("serverCountry", server.getCountry()).apply();
            sharedPreferences.edit().putString("serverUsername", server.getOvpnUserName()).apply();
            sharedPreferences.edit().putString("serverPassword", server.getOvpnUserPassword()).apply();
            sharedPreferences.edit().putBoolean("serverIsPremium", server.isPremium()).apply();
        }
    }

    public ServerModel getServer() {
        ServerModel s = new ServerModel();
        s.setOvpn(sharedPreferences.getString("serverOVPN", ""));
        s.setCountry(sharedPreferences.getString("serverCountry", ""));
        s.setOvpnUserName(sharedPreferences.getString("serverUsername", ""));
        s.setOvpnUserPassword(sharedPreferences.getString("serverPassword", ""));
        s.setPremium(sharedPreferences.getBoolean("serverIsPremium", false));

        return s;
    }
}


package com.doxart.myvpn.Util;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.doxart.myvpn.DB.Usage;

public class VPNCountdownTimer extends Service {
    private Handler handler;
    private Runnable runnable;
    private boolean isServiceRunning = false;
    private int usageMinutes, usageSeconds, inUsageSeconds = 0;
    private int oneMinute;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        oneMinute = 60;

        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                if (isServiceRunning) {
                    usageSeconds++;
                    inUsageSeconds++;

                    if (inUsageSeconds >= oneMinute) {
                        usageMinutes++;
                        inUsageSeconds = 0;

                        String today = Utils.getToday();

                        new Thread(() -> insertToDB(today, usageMinutes)).start();
                    }

                    Intent intent = new Intent("usage_data_updated");
                    intent.putExtra("usageMinutes", usageMinutes);
                    intent.putExtra("usageSeconds", usageSeconds);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void insertToDB(String today, int usageMinutes) {
        Usage usage = ViewModelHolder.getInstance().getUsageViewModel().getUsageRepository().getUsage(today);

        if (usage == null) {
            usage = new Usage(today, System.currentTimeMillis(), usageMinutes);
        } else usage.setUsageInMinutes(usage.getUsageInMinutes() + usageMinutes);

        ViewModelHolder.getInstance().getUsageViewModel().insertUsage(usage);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isServiceRunning = true;
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        handler.removeCallbacks(runnable);
    }
}


package com.doxart.myvpn.Util;

import android.app.Activity;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.doxart.myvpn.DB.UsageDatabase;
import com.doxart.myvpn.DB.UsageViewModel;

public class ViewModelHolder {
    private static final ViewModelHolder instance = new ViewModelHolder();

    public static ViewModelHolder getInstance() {
        return instance;
    }

    private UsageDatabase usageDatabase;
    private UsageViewModel usageViewModel;

    public void createViewModels(ViewModelStoreOwner viewModelStoreOwner, Activity activity) {
        usageDatabase = UsageDatabase.getInstance(activity);
        usageViewModel = new ViewModelProvider(viewModelStoreOwner).get(UsageViewModel.class);

        setUsageDatabase(usageDatabase);
        setUsageViewModel(usageViewModel);
    }

    public UsageDatabase getUsageDatabase() {
        return usageDatabase;
    }

    public void setUsageDatabase(UsageDatabase usageDatabase) {
        this.usageDatabase = usageDatabase;
    }

    public UsageViewModel getUsageViewModel() {
        return usageViewModel;
    }

    public void setUsageViewModel(UsageViewModel usageViewModel) {
        this.usageViewModel = usageViewModel;
    }

    public void onDestroy() {
        if (getUsageDatabase() != null) getUsageDatabase().close();
    }
}


package com.doxart.myvpn.Util;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.doxart.myvpn.R;


public class NotificationUtils {
    private static final String CHANNEL_ID = "DoxyVPN";
    Context context;

    public NotificationUtils(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    public void sendNotification(String title, String content, boolean ongoing) {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("DoxyVPN",
                    "Communication",
                    NotificationManager.IMPORTANCE_DEFAULT);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        Notification notification = builder.setOngoing(false)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(title)
                .setOngoing(ongoing)
                .setContentText(content)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(PRIORITY_MIN)
                .build();

        manager.notify(1500, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Communication",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager nManager = context.getSystemService(NotificationManager.class);
            if (nManager != null) {
                nManager.createNotificationChannel(channel);
            }
        }
    }

}


package com.doxart.myvpn.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.doxart.myvpn.DB.Usage;
import com.doxart.myvpn.R;
import com.skydoves.progressview.ProgressView;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UsageAdapter extends RecyclerView.Adapter<UsageAdapter.UHolder> {

    Context context;
    List<Usage> usageList = new ArrayList<>();

    Date myDate = new Date();
    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

    public UsageAdapter(Context context, OnViewCreated onViewCreated) {
        this.context = context;
        this.onViewCreated = onViewCreated;
    }

    public UsageAdapter(Context context) {
        this.context = context;
    }

    public interface OnViewCreated {
        void onCreated();
    }

    OnViewCreated onViewCreated;

    public void setUsageList(List<Usage> usageList) {
        this.usageList = usageList;
        for (Usage u : usageList) {
            Log.d("aAFASFASDASD", "setUsageList: " + u.getDateTime());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.usage_item, parent, false);
        if (onViewCreated != null) onViewCreated.onCreated();
        return new UHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UHolder h, int p) {
        Usage m = usageList.get(p);

        Date date = new Date(m.getDateTime());

        Log.d("aAFASFASDASD", "onBindViewHolder: " + System.currentTimeMillis());

        float usage = m.getUsageInMinutes()/60f;

        h.usage.setProgress(usage);

        h.date.setText(DateFormat.getDateInstance().format(date));

        h.hour.setText(String.format("%sh", new DecimalFormat("#.#").format(usage)));
    }

    @Override
    public int getItemCount() {
        return usageList.size();
    }

    public static class UHolder extends RecyclerView.ViewHolder {
        ProgressView usage;
        TextView date, hour;
        public UHolder(@NonNull View v) {
            super(v);
            usage = v.findViewById(R.id.usageProgress);
            date = v.findViewById(R.id.usageDate);
            hour = v.findViewById(R.id.usageHour);
        }
    }
}


package com.doxart.myvpn.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.doxart.myvpn.Activities.PaywallActivity;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Interfaces.NavItemClickListener;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;

import java.util.List;


public class ServerListRVAdapter extends RecyclerView.Adapter<ServerListRVAdapter.MyViewHolder> {

    private final List<ServerModel> serverLists;
    private final Context context;
    private final NavItemClickListener listener;
    int type = 0;

    public ServerListRVAdapter(Context context, List<ServerModel> serverLists, NavItemClickListener listener, int type) {
        this.serverLists = serverLists;
        this.context = context;
        this.listener = listener;
        this.type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (type == 0) return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.country_item, parent, false));
        else return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.vip_country_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder h, int p) {
        ServerModel m = serverLists.get(p);

        h.serverCountry.setText(m.getCountry());
        h.serverRegion.setText(m.getRegion());
        h.serverMs.setText(String.format("%sms", m.getLatency()));

        Glide.with(context).load("https://flagcdn.com/h80/" + m.getFlagUrl() + ".png").centerCrop().into(h.serverFlag);

        if (m.isPremium()) {
            h.status.setText(context.getString(R.string.premium));
            h.status.setBackgroundResource(R.drawable.card_basic_bg);
        } else {
            h.status.setText(context.getString(R.string.basic));
            h.status.setBackgroundResource(R.drawable.card_basic_bg);
        }

        Utils.setSignalView(context, h.s1, h.s2, h.s3, m.getLatency());

        h.itemView.setOnClickListener(v -> {
            if (m.isPremium()) {
                if (SharePrefs.getInstance(context).getBoolean("premium")) {
                    listener.clickedItem(m);
                } else context.startActivity(new Intent(context, PaywallActivity.class).putExtra("timer", 0));
            } else listener.clickedItem(m);
        });
    }

    @Override
    public int getItemCount() {
        return serverLists.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView serverFlag;
        TextView serverCountry, serverRegion, serverMs, status;
        CardView s1, s2, s3;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            serverFlag = itemView.findViewById(R.id.cFlagImg);
            serverCountry = itemView.findViewById(R.id.cCountryTxt);
            serverRegion = itemView.findViewById(R.id.cRegionTxt);
            serverMs = itemView.findViewById(R.id.cMsTxt);
            status = itemView.findViewById(R.id.cStatusTxt);
            s1 = itemView.findViewById(R.id.s1);
            s2 = itemView.findViewById(R.id.s2);
            s3 = itemView.findViewById(R.id.s3);
        }
    }
}


package com.doxart.myvpn.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class VPAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> fragmentList = new ArrayList<>();

    public VPAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    public void addFragment(Fragment fragment) {
        fragmentList.add(fragment);
    }

    public void removeFragment(Fragment fragment){
        fragmentList.remove(fragment);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

}


package com.doxart.myvpn.RetroFit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://ip-api.com";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}


package com.doxart.myvpn.RetroFit;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetIPDataService {
    @GET("/json")
    Call<MyIP> getMyIP();
}


package com.doxart.myvpn.RetroFit;

import com.google.gson.annotations.SerializedName;

public class MyIP {
    @SerializedName("query")
    private String query;

    @SerializedName("status")
    private String status;

    @SerializedName("country")
    private String country;

    @SerializedName("countryCode")
    private String countryCode;

    @SerializedName("region")
    private String region;

    @SerializedName("regionName")
    private String regionName;

    @SerializedName("city")
    private String city;

    @SerializedName("zip")
    private String zip;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lon")
    private double lon;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("isp")
    private String isp;

    @SerializedName("org")
    private String org;

    @SerializedName("as")
    private String as;

    public MyIP(String query, String status, String country, String countryCode, String region, String regionName, String city, String zip, double lat, double lon, String timezone, String isp, String org, String as) {
        this.query = query;
        this.status = status;
        this.country = country;
        this.countryCode = countryCode;
        this.region = region;
        this.regionName = regionName;
        this.city = city;
        this.zip = zip;
        this.lat = lat;
        this.lon = lon;
        this.timezone = timezone;
        this.isp = isp;
        this.org = org;
        this.as = as;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getAs() {
        return as;
    }

    public void setAs(String as) {
        this.as = as;
    }
}


package com.doxart.myvpn.Model;

public class ServerModel {
    private String country, region;
    private String flagUrl;
    private String ovpn;
    private String ovpnUserName;
    private String ovpnUserPassword;
    private boolean premium;
    private String urlToOVPN;
    private String ipv4;

    private int latency;
    private int port;


    public ServerModel() {
    }

    public ServerModel(String country, String region, String flagUrl, String ovpn, String ovpnUserName, String ovpnUserPassword, boolean premium, String urlToOVPN, String ipv4, int latency, int port) {
        this.country = country;
        this.region = region;
        this.flagUrl = flagUrl;
        this.ovpn = ovpn;
        this.ovpnUserName = ovpnUserName;
        this.ovpnUserPassword = ovpnUserPassword;
        this.premium = premium;
        this.urlToOVPN = urlToOVPN;
        this.ipv4 = ipv4;
        this.latency = latency;
        this.port = port;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }

    public void setUrlToOVPN(String urlToOVPN) {
        this.urlToOVPN = urlToOVPN;
    }

    public String getUrlToOVPN() {
        return urlToOVPN;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public boolean isPremium() {
        return premium;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public String getOvpn() {
        return ovpn;
    }

    public void setOvpn(String ovpn) {
        this.ovpn = ovpn;
    }

    public String getOvpnUserName() {
        return ovpnUserName;
    }

    public void setOvpnUserName(String ovpnUserName) {
        this.ovpnUserName = ovpnUserName;
    }

    public String getOvpnUserPassword() {
        return ovpnUserPassword;
    }

    public void setOvpnUserPassword(String ovpnUserPassword) {
        this.ovpnUserPassword = ovpnUserPassword;
    }
}


package com.doxart.myvpn.Model;

public class UsageModel {
    int id;
    String date;
    int usage;
    long dateTime;

    public UsageModel() {}

    public UsageModel(int id, String date, long dateTime, int usage) {
        this.id = id;
        this.date = date;
        this.dateTime = dateTime;
        this.usage = usage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }
}


package com.doxart.myvpn.Model;

import com.adapty.models.AdaptyPaywall;
import com.adapty.models.AdaptyPaywallProduct;
import com.adapty.models.AdaptyViewConfiguration;
import com.adapty.ui.AdaptyPaywallView;

import java.util.List;

public class PaywallModel {
    AdaptyPaywall adaptyPaywall;
    AdaptyViewConfiguration viewConfiguration;
    AdaptyPaywallView adaptyPaywallView;
    List<AdaptyPaywallProduct> products;

    public PaywallModel() {}

    public PaywallModel(AdaptyPaywall adaptyPaywall, AdaptyViewConfiguration viewConfiguration, AdaptyPaywallView adaptyPaywallView, List<AdaptyPaywallProduct> products) {
        this.adaptyPaywall = adaptyPaywall;
        this.viewConfiguration = viewConfiguration;
        this.adaptyPaywallView = adaptyPaywallView;
        this.products = products;
    }

    public AdaptyPaywallView getAdaptyPaywallView() {
        return adaptyPaywallView;
    }

    public void setAdaptyPaywallView(AdaptyPaywallView adaptyPaywallView) {
        this.adaptyPaywallView = adaptyPaywallView;
    }

    public AdaptyPaywall getAdaptyPaywall() {
        return adaptyPaywall;
    }

    public void setAdaptyPaywall(AdaptyPaywall adaptyPaywall) {
        this.adaptyPaywall = adaptyPaywall;
    }

    public AdaptyViewConfiguration getViewConfiguration() {
        return viewConfiguration;
    }

    public void setViewConfiguration(AdaptyViewConfiguration viewConfiguration) {
        this.viewConfiguration = viewConfiguration;
    }

    public List<AdaptyPaywallProduct> getProducts() {
        return products;
    }

    public void setProducts(List<AdaptyPaywallProduct> products) {
        this.products = products;
    }
}


package com.doxart.myvpn.DB;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import com.doxart.myvpn.Adapter.ServerListRVAdapter;
import com.doxart.myvpn.Interfaces.NavItemClickListener;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.databinding.VpnListSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ServerDB {

    private static final ServerDB instance = new ServerDB();

    public interface OnServerReadyListener {
        void onReady();
    }

    private List<ServerModel> serverList;

    public void getServers(Context context, OnServerReadyListener onServerReadyListener) {
        List<ServerModel> list = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("servers").whereEqualTo("active", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot snapshot : task.getResult()) {
                    ServerModel serverModel = new ServerModel();

                    serverModel.setOvpn(snapshot.getString("ovpn"));
                    serverModel.setRegion(snapshot.getString("region"));
                    serverModel.setFlagUrl(snapshot.getString("flagUrl"));
                    serverModel.setOvpnUserName(snapshot.getString("ovpnUserName"));
                    serverModel.setOvpnUserPassword(snapshot.getString("ovpnUserPassword"));
                    serverModel.setCountry(snapshot.getString("country"));
                    serverModel.setPremium(Boolean.TRUE.equals(snapshot.getBoolean("premium")));
                    serverModel.setUrlToOVPN(snapshot.getString("urlToOVPN"));
                    serverModel.setIpv4(snapshot.getString("ipv4"));
                    serverModel.setPort(Objects.requireNonNull(snapshot.getLong("port")).intValue());

                    Thread thread = new Thread(() -> serverModel.setLatency(getPing(serverModel.getIpv4(),  serverModel.getPort())));
                    thread.start();

                    File file = new File(context.getFilesDir().toString() + "/" + serverModel.getOvpn());
                    if (!file.exists()) new Thread(() -> downloadOVPN(context, serverModel.getUrlToOVPN(), serverModel.getOvpn())).start();

                    list.add(serverModel);
                }

                list.sort(Comparator.comparing(ServerModel::getLatency));

                setServerList(list);
                if (onServerReadyListener != null) onServerReadyListener.onReady();
            }
        });
    }

    private void downloadOVPN(Context context, String url, String name) {
        Log.d("SYNC getUpdate", "downloadOVPN: getting: " + url);
        try {
            URL u = new URL(url);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            FileOutputStream fos = new FileOutputStream(context.getFilesDir().toString() + "/" + name);
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

        } catch (MalformedURLException mue) {
            Log.e("SYNC getUpdate", "malformed url error", mue);
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }
    }

    public int getPing(String URL, int port) {
        long ms = 0;
        try {
            long startTime = System.currentTimeMillis();
            Socket socket = new Socket(URL, port);
            long endTime = System.currentTimeMillis();

            ms = endTime - startTime;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (int) ms;
    }

    public void setServerList(List<ServerModel> serverList) {
        this.serverList = serverList;
    }

    public List<ServerModel> getServerList() {
        return serverList;
    }

    public static BottomSheetDialog getListView(Context context, NavItemClickListener navItemClickListener) {
        VpnListSheetBinding b = VpnListSheetBinding.bind(LayoutInflater.from(context).inflate(R.layout.vpn_list_sheet, null));

        BottomSheetDialog d = new BottomSheetDialog(context);
        d.setContentView(b.getRoot());
        d.show();

        ServerListRVAdapter adapter = new ServerListRVAdapter(context, ServerDB.getInstance().getServerList(), navItemClickListener, 0);

        b.serverRecycler.setHasFixedSize(true);
        b.serverRecycler.setAdapter(adapter);

        return d;
    }

    public static ServerDB getInstance() {
        return instance;
    }
}


package com.doxart.myvpn.DB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Usage.class}, version = 1)
public abstract class UsageDatabase extends RoomDatabase {
    public abstract UsageDao usageDao();
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile UsageDatabase INSTANCE;

    public static UsageDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (UsageDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UsageDatabase.class, "usage_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}


package com.doxart.myvpn.DB;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class UsageRepository {
    private final UsageDao usageDao;
    private final LiveData<List<Usage>>allUsages;

    public UsageRepository(Application application) {
        UsageDatabase database = UsageDatabase.getInstance(application);
        usageDao = database.usageDao();
        allUsages = usageDao.getAllUsage();
    }

    public LiveData<List<Usage>> getAllUsages() {
        return allUsages;
    }

    public Usage getUsage(String date) {
        return usageDao.getUsage(date);
    }

    public void insertUsage(Usage usage) {
        UsageDatabase.databaseWriteExecutor.execute(() -> usageDao.insertUsage(usage));
    }
}


package com.doxart.myvpn.DB;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsageDao {
    @Query("SELECT * FROM usage WHERE date = :date")
    Usage getUsage(String date);

    @Query("SELECT * FROM usage")
    LiveData<List<Usage>> getAllUsage();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsage(Usage budget);
}


package com.doxart.myvpn.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usage")
public class Usage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "date")
    private String date;

    @ColumnInfo(name = "date_time")
    private long dateTime;

    @ColumnInfo(name = "usage_in_minutes")
    private int usageInMinutes;

    public Usage(String date, long dateTime, int usageInMinutes) {
        this.date = date;
        this.dateTime = dateTime;
        this.usageInMinutes = usageInMinutes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getUsageInMinutes() {
        return usageInMinutes;
    }

    public void setUsageInMinutes(int usageInMinutes) {
        this.usageInMinutes = usageInMinutes;
    }
}


package com.doxart.myvpn.DB;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UsageViewModel extends AndroidViewModel {
    private final UsageRepository usageRepository;
    private final LiveData<List<Usage>> allUsages;

    public UsageViewModel(Application application) {
        super(application);
        usageRepository = new UsageRepository(application);
        allUsages = usageRepository.getAllUsages();
    }

    public UsageRepository getUsageRepository() {
        return usageRepository;
    }

    public LiveData<List<Usage>> getAllUsages() {
        return allUsages;
    }

    public void insertUsage(Usage usage) {
        usageRepository.insertUsage(usage);
    }
}


package com.doxart.myvpn.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.adapty.Adapty;
import com.adapty.errors.AdaptyError;
import com.adapty.models.AdaptyProfile;
import com.adapty.utils.AdaptyResult;
import com.doxart.myvpn.DB.ServerDB;
import com.doxart.myvpn.Interfaces.OnAnswerListener;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.PaywallViewUtils;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivityLauncherBinding;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LauncherActivity extends AppCompatActivity {

    ActivityLauncherBinding b;

    SharePrefs sharePrefs;

    private final String TAG = "LAUNCHER_PROCESS";
    private ExecutorService executorService;
    private Dialog connectionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (Utils.checkConnection(LauncherActivity.this)) {
                if (connectionDialog != null) {
                    if (connectionDialog.isShowing()) connectionDialog.cancel();
                }
                inflate();
            } else showConnectionDialog();
        }
    });

    private void showConnectionDialog() {
        connectionDialog = Utils.askQuestion(this, getString(R.string.no_connection), getString(R.string.no_connection_detail), getString(R.string.go_network_settings),
                getString(R.string.try_again), getString(R.string.exit), null, false, new OnAnswerListener() {
                    @Override
                    public void onPositive() {
                        resultLauncher.launch(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }

                    @Override
                    public void onNegative() {
                        inflate();
                    }

                    @Override
                    public void onOther() {
                        System.exit(1);
                    }
                });

        connectionDialog.show();
    }

    private void inflate() {
        b = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        if (!Utils.checkConnection(this)) {
            showConnectionDialog();
            return;
        }

        sharePrefs = new SharePrefs(this);

        b.adjustingTxt.setText(getString(R.string.launch_phase_0));

        executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        b.adjustingTxt.setText(getString(R.string.launch_phase_1));

        executorService.execute(() -> {
            PaywallViewUtils.getInstance().getPaywallView("CHANGE WITH YOUR ADAPTY PLACEMENT ID");

            handler.post(() -> PaywallViewUtils.getInstance().setPaywallLoadFinishListener(new PaywallViewUtils.OnPaywallLoadFinishListener() {
                @Override
                public void onFinish() {
                    getConfig();
                }

                @Override
                public void onError() {
                    getConfig();
                }
            }));
        });
    }

    private void getConfig() {
        b.adjustingTxt.setText(getString(R.string.launch_phase_2));
        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sharePrefs.putBoolean("showPaywallCloseAd", remoteConfig.getBoolean("showPaywallCloseAd"));
                sharePrefs.putBoolean("showBannerAds", remoteConfig.getBoolean("showBannerAds"));
                sharePrefs.putInt("vpnButtonAdMode", ((Long)remoteConfig.getLong("vpnButtonAdMode")).intValue());
                sharePrefs.putInt("delayTimeBetweenAds", ((Long)remoteConfig.getLong("delayTimeBetweenAds")).intValue());
            }

            b.adjustingTxt.setText(getString(R.string.launch_phase_3));
            ServerDB.getInstance().getServers(this, this::getStatus);
        });
    }

    private void getStatus() {
        b.adjustingTxt.setText(getString(R.string.launch_phase_4));
        Adapty.getProfile(result -> {
            if (result instanceof AdaptyResult.Success) {
                AdaptyProfile profile = ((AdaptyResult.Success<AdaptyProfile>) result).getValue();

                AdaptyProfile.AccessLevel premium = profile.getAccessLevels().get("premium");

                sharePrefs.putBoolean("premium", premium != null && premium.isActive());
            } else if (result instanceof AdaptyResult.Error) {
                AdaptyError error = ((AdaptyResult.Error) result).getError();
                Log.d(TAG, "getStatus: " + error);
            }

            b.adjustingTxt.setText(getString(R.string.launch_phase_5));
            init();
        });
    }

    private void init() {
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        executorService.shutdown();

        finish();
    }
}

package com.doxart.myvpn.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.doxart.myvpn.Adapter.VPAdapter;
import com.doxart.myvpn.Fragments.LocationFragment;
import com.doxart.myvpn.Fragments.ServersFragment;
import com.doxart.myvpn.Fragments.SettingsFragment;
import com.doxart.myvpn.Fragments.SpeedTestFragment;
import com.doxart.myvpn.Fragments.VPNFragment;
import com.doxart.myvpn.Interfaces.NavItemClickListener;
import com.doxart.myvpn.Interfaces.OnAnswerListener;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.Util.ViewModelHolder;
import com.doxart.myvpn.databinding.ActivityMainBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavItemClickListener {
    private final String TAG = "MAIN_ACTIVITY";
    private static MainActivity instance;
    ActivityMainBinding b;

    Dialog connectionDialog;

    InterstitialAd mInterstitialAd;
    SharePrefs sharePrefs;
    int adDelay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (instance == null) instance = this;
        ViewModelHolder.getInstance().createViewModels(getInstance(), this);
        setupNetworkListener();
        inflate();
    }

    private void setupNetworkListener() {
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                if (connectionDialog != null) {
                    if (connectionDialog.isShowing()) connectionDialog.cancel();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                new Handler().postDelayed(() -> {
                    if (!Utils.checkConnection(MainActivity.this)) {
                        showConnectionDialog();
                    }
                }, 1000);
            }
        };

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (Utils.checkConnection(MainActivity.this)) {
                if (connectionDialog != null) {
                    if (connectionDialog.isShowing()) connectionDialog.cancel();
                }
            } else showConnectionDialog();
        }
    });

    private final ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getData() != null) {
                if (!SharePrefs.getInstance(MainActivity.this).getBoolean("premium") &
                        o.getData().getBooleanExtra("showAD", false)) showInterstitial();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    });

    private void showConnectionDialog() {
        connectionDialog = Utils.askQuestion(this, getString(R.string.no_connection), getString(R.string.no_connection_detail), getString(R.string.go_network_settings),
                getString(R.string.try_again), getString(R.string.exit), null, false, new OnAnswerListener() {
                    @Override
                    public void onPositive() {
                        settingsLauncher.launch(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }

                    @Override
                    public void onNegative() {
                        if (!Utils.checkConnection(MainActivity.this)) showConnectionDialog();
                    }

                    @Override
                    public void onOther() {
                        System.exit(1);
                    }
                });

        connectionDialog.show();
    }

    private void inflate() {
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        sharePrefs = new SharePrefs(this);
        adDelay = sharePrefs.getInt("delayTimeBetweenAds");

        if (!sharePrefs.getBoolean("premium")) buildInterstitial();

        if (!SharePrefs.getInstance(this).getBoolean("premium"))
            activityLauncher.launch(new Intent(this, PaywallActivity.class).putExtra("timer", 3000));
        else b.appbar.premiumBT.setVisibility(View.GONE);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));

        //if (SharePrefs.getInstance(this).isDynamicBackground()) setBackground();

        init();
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), o -> {});

    private void init() {
        createFragments();

        b.appbar.premiumBT.setOnClickListener(v -> activityLauncher.launch(new Intent(this, PaywallActivity.class).putExtra("timer", 0)));
        b.appbar.shareBT.setOnClickListener(v -> Utils.shareApp(this));
    }

    private void createFragments() {
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), getLifecycle());

        vpAdapter.addFragment(new ServersFragment());
        vpAdapter.addFragment(new VPNFragment());
        vpAdapter.addFragment(new LocationFragment());
        vpAdapter.addFragment(new SpeedTestFragment());
        vpAdapter.addFragment(new SettingsFragment());

        b.mainPager.setUserInputEnabled(false);
        b.mainPager.setAdapter(vpAdapter);

        b.mainPager.setCurrentItem(1, false);

        b.mainNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navVpn) {
                b.mainNav.getMenu().findItem(R.id.navVpn).setChecked(true);
                b.mainPager.setCurrentItem(1);
            } else if (item.getItemId() == R.id.navLocation) {
                b.mainNav.getMenu().findItem(R.id.navLocation).setChecked(true);
                b.mainPager.setCurrentItem(2);
                if (LocationFragment.getInstance() != null) LocationFragment.getInstance().setIPLocation();

            } else if (item.getItemId() == R.id.navSpeedTest){
                b.mainNav.getMenu().findItem(R.id.navSpeedTest).setChecked(true);
                b.mainPager.setCurrentItem(3);
            } else {
                b.mainNav.getMenu().findItem(R.id.navSettings).setChecked(true);
                b.mainPager.setCurrentItem(4);
            }
            return false;
        });
    }

    public void openServerList() {
        b.mainPager.setCurrentItem(0);
        //b.mainNav.setVisibility(View.GONE);
        b.appbar.getRoot().setVisibility(View.GONE);
    }

    public void closeServerList() {
        b.mainPager.setCurrentItem(1);
        //b.mainNav.setVisibility(View.VISIBLE);
        b.appbar.getRoot().setVisibility(View.VISIBLE);
    }

    @Override
    public void clickedItem(ServerModel index) {
        closeServerList();
        VPNFragment.getInstance().changeServer(index);
    }

    private void buildInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ADD YOUR ADMOB INTERSTITIAL ID", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    private void showInterstitial() {
        long lastAd = sharePrefs.getLastAd();

        if (new Date().getTime() < lastAd) return;

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                    Log.d(TAG, "showInterstitial: fail");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    sharePrefs.putLong("lastAd", new Date().getTime() + (long) adDelay * 60 * 1000);
                    buildInterstitial();
                    Log.d(TAG, "showInterstitial: dismiss");

                }
            });

            mInterstitialAd.show(this);
        } else {
            Log.d(TAG, "showInterstitial: null");
            buildInterstitial();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    public static MainActivity getInstance() {
        return instance;
    }
}

package com.doxart.myvpn.Activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.doxart.myvpn.Model.ServerModel;
import com.doxart.myvpn.R;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivityConnectionReportBinding;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;

import java.util.Locale;

public class ConnectionReportActivity extends AppCompatActivity {

    ActivityConnectionReportBinding b;
    public static ServerModel server;
    private Dialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflate();
    }

    private void inflate() {
        b = ActivityConnectionReportBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        loadAds();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        setupView();
        init();
    }

    private void setupView() {
        if (server != null) {
            boolean isConnection = getIntent().getBooleanExtra("isConnection", true);

            if (isConnection) b.connectionTypeTxt.setText(getString(R.string.connection_successful));
            else {
                b.connectionTypeTxt.setText(getString(R.string.connection_report));
                updateConnectionStatus(getIntent().getIntExtra("sessionM", 0), getIntent().getIntExtra("sessionS", 0));
            }

            Glide.with(this).load("https://flagcdn.com/h80/" + server.getFlagUrl() + ".png").centerCrop().into(b.serverFlag);
            b.serverIp.setText(server.getIpv4());
            b.serverName.setText(server.getCountry());
        } else finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        SharePrefs sharePrefs = new SharePrefs(this);

        boolean premium = sharePrefs.getBoolean("premium");

        if (!premium) {
            if (sharePrefs.getBoolean("showBannerAds")) loadAds();
            else b.myTemplate.setVisibility(View.GONE);
        } else b.myTemplate.setVisibility(View.GONE);

        b.closeBT.setOnClickListener(v -> finish());

        b.shareBT.setOnClickListener(v -> Utils.shareApp(this));
        b.ratingView.setOnTouchListener((v, event) -> {
            Utils.openAppInPlayStore(ConnectionReportActivity.this);
            return false;
        });
    }


    private void loadAds() {
        b.myTemplate.setVisibility(View.GONE);
        AdLoader adLoader = new AdLoader.Builder(this, "CHANGE WITH YOUR ADMOB NATIVE ID")
                .forNativeAd(nativeAd -> {
                    NativeTemplateStyle styles = new
                            NativeTemplateStyle.Builder().build();
                    TemplateView template = findViewById(R.id.my_template);
                    template.setStyles(styles);
                    template.setNativeAd(nativeAd);
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
        b.myTemplate.setVisibility(View.VISIBLE);
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("usage_data_updated")) {
                int m = intent.getIntExtra("usageMinutes", 0);
                int s = intent.getIntExtra("usageSeconds", 0);
                updateConnectionStatus(m, s);
            }
        }
    };

    public void updateConnectionStatus(int m, int s) {
        int totalSeconds = m * 60 + s;
        String formattedTime = String.format(Locale.getDefault(), "%02d s", totalSeconds % 60);
        b.serverTime.setText(formattedTime);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("connectionState"));
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("usage_data_updated"));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
}

package com.doxart.myvpn.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adapty.Adapty;
import com.adapty.errors.AdaptyError;
import com.adapty.models.AdaptyPaywall;
import com.adapty.models.AdaptyPaywallProduct;
import com.adapty.models.AdaptyProfile;
import com.adapty.models.AdaptyViewConfiguration;
import com.adapty.ui.AdaptyPaywallInsets;
import com.adapty.ui.AdaptyPaywallView;
import com.adapty.ui.AdaptyUI;
import com.adapty.ui.listeners.AdaptyUiEventListener;
import com.doxart.myvpn.Util.PaywallViewUtils;
import com.doxart.myvpn.Util.SharePrefs;
import com.doxart.myvpn.Util.Utils;
import com.doxart.myvpn.databinding.ActivityPaywallBinding;

import java.util.List;

public class PaywallActivity extends AppCompatActivity {

    private final String TAG = "APPTAG";
    ActivityPaywallBinding b;

    boolean isPremium = false;

    SharePrefs sharePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        inflate();

    }

    private void inflate() {
        b = ActivityPaywallBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        sharePrefs = new SharePrefs(this);

        init();
    }

    int iTimer = 3000;

    private void init() {
        b.closeBT.setOnClickListener(v -> onBackPressed());

        b.payFrame.removeAllViews();

        AdaptyPaywall paywall = PaywallViewUtils.getInstance().getPaywallHolder().getAdaptyPaywall();
        List<AdaptyPaywallProduct> products = PaywallViewUtils.getInstance().getPaywallHolder().getProducts();
        AdaptyViewConfiguration viewConfiguration = PaywallViewUtils.getInstance().getPaywallHolder().getViewConfiguration();

        iTimer = getIntent().getIntExtra("timer", 3000);

        if (paywall != null & products != null & viewConfiguration != null) {
            getInsets(paywall, products, viewConfiguration);
            new CountDownTimer(iTimer, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    b.closeBTCounter.setText(String.valueOf(millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    b.closeBT.setVisibility(View.VISIBLE);
                    b.closeBTCounter.setVisibility(View.GONE);
                }
            }.start();
        } else onBackPressed();
    }

    private void getInsets(AdaptyPaywall paywall, List<AdaptyPaywallProduct> products, AdaptyViewConfiguration viewConfiguration) {
        final int statusBarHeight = Utils.getStatusBarHeight(this);
        final int navigationBarHeight = Utils.getNavigationBarHeight(this);

        int pxToDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

        try {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) b.closeBT.getLayoutParams();
            ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) b.closeBTCounter.getLayoutParams();
            params.setMargins(statusBarHeight, pxToDp + statusBarHeight, 0, 0);
            params1.setMargins(statusBarHeight, pxToDp + statusBarHeight, 0, 0);

            b.closeBT.setLayoutParams(params);
            b.closeBTCounter.setLayoutParams(params);
            b.closeBT.requestLayout();
            b.closeBTCounter.requestLayout();
        } catch (Exception e) {
            Log.d(TAG, "getInsets: " + e);
        }

        b.payFrame.setEventListener(eventListener);
        b.payFrame.showPaywall(paywall, products,
                viewConfiguration,
                AdaptyPaywallInsets.of(statusBarHeight, navigationBarHeight + pxToDp), adaptyPaywallProduct -> false);

        Adapty.logShowPaywall(paywall);
    }

    private final AdaptyUiEventListener eventListener = new AdaptyUiEventListener() {
        @Override
        public void onActionPerformed(@NonNull AdaptyUI.Action action, @NonNull AdaptyPaywallView adaptyPaywallView) {
            if (action instanceof AdaptyUI.Action.Close) {
                onBackPressed();
            }
            Log.d(TAG, "onActionPerformed: " + action);
        }

        @Override
        public boolean onLoadingProductsFailure(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallView adaptyPaywallView) {
            onBackPressed();
            return false;
        }

        @Override
        public void onProductSelected(@NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {

        }

        @Override
        public void onPurchaseCanceled(@NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseCanceled: canceled");
            isPremium = false;
        }

        @Override
        public void onPurchaseFailure(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseFailure: " + adaptyError);
            isPremium = false;
        }

        @Override
        public void onPurchaseStarted(@NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseStarted: started");
        }

        @Override
        public void onPurchaseSuccess(@Nullable AdaptyProfile adaptyProfile, @NonNull AdaptyPaywallProduct adaptyPaywallProduct, @NonNull AdaptyPaywallView adaptyPaywallView) {
            Log.d(TAG, "onPurchaseSuccess: success " + true);
            isPremium = true;
            onBackPressed();
        }

        @Override
        public void onRenderingError(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallView adaptyPaywallView) {
            onBackPressed();
        }

        @Override
        public void onRestoreFailure(@NonNull AdaptyError adaptyError, @NonNull AdaptyPaywallView adaptyPaywallView) {

        }

        @Override
        public void onRestoreSuccess(@NonNull AdaptyProfile adaptyProfile, @NonNull AdaptyPaywallView adaptyPaywallView) {

        }
    };

    @Override
    public void onBackPressed() {
        sharePrefs.putBoolean("premium", isPremium);

        Intent data = new Intent();
        data.putExtra("showAD", sharePrefs.getBoolean("showPaywallCloseAd"));
        setResult(Activity.RESULT_OK, data);

        super.onBackPressed();
    }
}

package com.doxart.myvpn.Interfaces;

public interface OnAnswerListener {
    void onPositive();
    void onNegative();
    void onOther();
}


package com.doxart.myvpn.Interfaces;

import com.doxart.myvpn.Model.ServerModel;

public interface ChangeServer {
    void newServer(ServerModel server);
}


package com.doxart.myvpn.Interfaces;

import com.doxart.myvpn.Model.ServerModel;

public interface NavItemClickListener {
    void clickedItem(ServerModel index);
}


/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package de.blinkt.openvpn.core;
/** Created by arne on 15.11.16. */
public interface IOpenVPNServiceInternal extends android.os.IInterface
{
  /** Default implementation for IOpenVPNServiceInternal. */
  public static class Default implements de.blinkt.openvpn.core.IOpenVPNServiceInternal
  {
    @Override public boolean protect(int fd) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void userPause(boolean b) throws android.os.RemoteException
    {
    }
    /**
     * @param replaceConnection True if the VPN is connected by a new connection.
     * @return true if there was a process that has been send a stop signal
     */
    @Override public boolean stopVPN(boolean replaceConnection) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void addAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
    {
    }
    @Override public boolean isAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
    {
      return false;
    }
    @Override public void challengeResponse(java.lang.String repsonse) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.core.IOpenVPNServiceInternal
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.core.IOpenVPNServiceInternal interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.core.IOpenVPNServiceInternal asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.core.IOpenVPNServiceInternal))) {
        return ((de.blinkt.openvpn.core.IOpenVPNServiceInternal)iin);
      }
      return new de.blinkt.openvpn.core.IOpenVPNServiceInternal.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_protect:
        {
          int _arg0;
          _arg0 = data.readInt();
          boolean _result = this.protect(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_userPause:
        {
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          this.userPause(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_stopVPN:
        {
          boolean _arg0;
          _arg0 = (0!=data.readInt());
          boolean _result = this.stopVPN(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_addAllowedExternalApp:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.addAllowedExternalApp(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_isAllowedExternalApp:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          boolean _result = this.isAllowedExternalApp(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_challengeResponse:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.challengeResponse(_arg0);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.core.IOpenVPNServiceInternal
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public boolean protect(int fd) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(fd);
          boolean _status = mRemote.transact(Stub.TRANSACTION_protect, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void userPause(boolean b) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((b)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_userPause, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * @param replaceConnection True if the VPN is connected by a new connection.
       * @return true if there was a process that has been send a stop signal
       */
      @Override public boolean stopVPN(boolean replaceConnection) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(((replaceConnection)?(1):(0)));
          boolean _status = mRemote.transact(Stub.TRANSACTION_stopVPN, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void addAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_addAllowedExternalApp, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      @Override public boolean isAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isAllowedExternalApp, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void challengeResponse(java.lang.String repsonse) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(repsonse);
          boolean _status = mRemote.transact(Stub.TRANSACTION_challengeResponse, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_protect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_userPause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_stopVPN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_addAllowedExternalApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_isAllowedExternalApp = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_challengeResponse = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
  }
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.core.IOpenVPNServiceInternal";
  public boolean protect(int fd) throws android.os.RemoteException;
  public void userPause(boolean b) throws android.os.RemoteException;
  /**
   * @param replaceConnection True if the VPN is connected by a new connection.
   * @return true if there was a process that has been send a stop signal
   */
  public boolean stopVPN(boolean replaceConnection) throws android.os.RemoteException;
  public void addAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException;
  public boolean isAllowedExternalApp(java.lang.String packagename) throws android.os.RemoteException;
  public void challengeResponse(java.lang.String repsonse) throws android.os.RemoteException;
}


/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package de.blinkt.openvpn.core;
public interface IServiceStatus extends android.os.IInterface
{
  /** Default implementation for IServiceStatus. */
  public static class Default implements de.blinkt.openvpn.core.IServiceStatus
  {
    /**
     * Registers to receive OpenVPN Status Updates and gets a
     * ParcelFileDescript back that contains the log up to that point
     */
    @Override public android.os.ParcelFileDescriptor registerStatusCallback(de.blinkt.openvpn.core.IStatusCallbacks cb) throws android.os.RemoteException
    {
      return null;
    }
    /** Remove a previously registered callback interface. */
    @Override public void unregisterStatusCallback(de.blinkt.openvpn.core.IStatusCallbacks cb) throws android.os.RemoteException
    {
    }
    /** Returns the last connedcted VPN */
    @Override public java.lang.String getLastConnectedVPN() throws android.os.RemoteException
    {
      return null;
    }
    /** Sets a cached password */
    @Override public void setCachedPassword(java.lang.String uuid, int type, java.lang.String password) throws android.os.RemoteException
    {
    }
    /** Gets the traffic history */
    @Override public de.blinkt.openvpn.core.TrafficHistory getTrafficHistory() throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.core.IServiceStatus
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.core.IServiceStatus interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.core.IServiceStatus asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.core.IServiceStatus))) {
        return ((de.blinkt.openvpn.core.IServiceStatus)iin);
      }
      return new de.blinkt.openvpn.core.IServiceStatus.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_registerStatusCallback:
        {
          de.blinkt.openvpn.core.IStatusCallbacks _arg0;
          _arg0 = de.blinkt.openvpn.core.IStatusCallbacks.Stub.asInterface(data.readStrongBinder());
          android.os.ParcelFileDescriptor _result = this.registerStatusCallback(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_unregisterStatusCallback:
        {
          de.blinkt.openvpn.core.IStatusCallbacks _arg0;
          _arg0 = de.blinkt.openvpn.core.IStatusCallbacks.Stub.asInterface(data.readStrongBinder());
          this.unregisterStatusCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getLastConnectedVPN:
        {
          java.lang.String _result = this.getLastConnectedVPN();
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_setCachedPassword:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          java.lang.String _arg2;
          _arg2 = data.readString();
          this.setCachedPassword(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getTrafficHistory:
        {
          de.blinkt.openvpn.core.TrafficHistory _result = this.getTrafficHistory();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.core.IServiceStatus
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
       * Registers to receive OpenVPN Status Updates and gets a
       * ParcelFileDescript back that contains the log up to that point
       */
      @Override public android.os.ParcelFileDescriptor registerStatusCallback(de.blinkt.openvpn.core.IStatusCallbacks cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.ParcelFileDescriptor _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(cb);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerStatusCallback, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.ParcelFileDescriptor.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Remove a previously registered callback interface. */
      @Override public void unregisterStatusCallback(de.blinkt.openvpn.core.IStatusCallbacks cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(cb);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterStatusCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Returns the last connedcted VPN */
      @Override public java.lang.String getLastConnectedVPN() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getLastConnectedVPN, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Sets a cached password */
      @Override public void setCachedPassword(java.lang.String uuid, int type, java.lang.String password) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(uuid);
          _data.writeInt(type);
          _data.writeString(password);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setCachedPassword, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Gets the traffic history */
      @Override public de.blinkt.openvpn.core.TrafficHistory getTrafficHistory() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        de.blinkt.openvpn.core.TrafficHistory _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getTrafficHistory, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, de.blinkt.openvpn.core.TrafficHistory.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_registerStatusCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_unregisterStatusCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getLastConnectedVPN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_setCachedPassword = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_getTrafficHistory = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
  }
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.core.IServiceStatus";
  /**
   * Registers to receive OpenVPN Status Updates and gets a
   * ParcelFileDescript back that contains the log up to that point
   */
  public android.os.ParcelFileDescriptor registerStatusCallback(de.blinkt.openvpn.core.IStatusCallbacks cb) throws android.os.RemoteException;
  /** Remove a previously registered callback interface. */
  public void unregisterStatusCallback(de.blinkt.openvpn.core.IStatusCallbacks cb) throws android.os.RemoteException;
  /** Returns the last connedcted VPN */
  public java.lang.String getLastConnectedVPN() throws android.os.RemoteException;
  /** Sets a cached password */
  public void setCachedPassword(java.lang.String uuid, int type, java.lang.String password) throws android.os.RemoteException;
  /** Gets the traffic history */
  public de.blinkt.openvpn.core.TrafficHistory getTrafficHistory() throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}


/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package de.blinkt.openvpn.core;
public interface IStatusCallbacks extends android.os.IInterface
{
  /** Default implementation for IStatusCallbacks. */
  public static class Default implements de.blinkt.openvpn.core.IStatusCallbacks
  {
    /** Called when the service has a new status for you. */
    @Override public void newLogItem(de.blinkt.openvpn.core.LogItem item) throws android.os.RemoteException
    {
    }
    @Override public void updateStateString(java.lang.String state, java.lang.String msg, int resid, de.blinkt.openvpn.core.ConnectionStatus level, android.content.Intent intent) throws android.os.RemoteException
    {
    }
    @Override public void updateByteCount(long inBytes, long outBytes) throws android.os.RemoteException
    {
    }
    @Override public void connectedVPN(java.lang.String uuid) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.core.IStatusCallbacks
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.core.IStatusCallbacks interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.core.IStatusCallbacks asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.core.IStatusCallbacks))) {
        return ((de.blinkt.openvpn.core.IStatusCallbacks)iin);
      }
      return new de.blinkt.openvpn.core.IStatusCallbacks.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_newLogItem:
        {
          de.blinkt.openvpn.core.LogItem _arg0;
          _arg0 = _Parcel.readTypedObject(data, de.blinkt.openvpn.core.LogItem.CREATOR);
          this.newLogItem(_arg0);
          break;
        }
        case TRANSACTION_updateStateString:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          int _arg2;
          _arg2 = data.readInt();
          de.blinkt.openvpn.core.ConnectionStatus _arg3;
          _arg3 = _Parcel.readTypedObject(data, de.blinkt.openvpn.core.ConnectionStatus.CREATOR);
          android.content.Intent _arg4;
          _arg4 = _Parcel.readTypedObject(data, android.content.Intent.CREATOR);
          this.updateStateString(_arg0, _arg1, _arg2, _arg3, _arg4);
          break;
        }
        case TRANSACTION_updateByteCount:
        {
          long _arg0;
          _arg0 = data.readLong();
          long _arg1;
          _arg1 = data.readLong();
          this.updateByteCount(_arg0, _arg1);
          break;
        }
        case TRANSACTION_connectedVPN:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.connectedVPN(_arg0);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.core.IStatusCallbacks
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /** Called when the service has a new status for you. */
      @Override public void newLogItem(de.blinkt.openvpn.core.LogItem item) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, item, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_newLogItem, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void updateStateString(java.lang.String state, java.lang.String msg, int resid, de.blinkt.openvpn.core.ConnectionStatus level, android.content.Intent intent) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(state);
          _data.writeString(msg);
          _data.writeInt(resid);
          _Parcel.writeTypedObject(_data, level, 0);
          _Parcel.writeTypedObject(_data, intent, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_updateStateString, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void updateByteCount(long inBytes, long outBytes) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeLong(inBytes);
          _data.writeLong(outBytes);
          boolean _status = mRemote.transact(Stub.TRANSACTION_updateByteCount, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      @Override public void connectedVPN(java.lang.String uuid) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(uuid);
          boolean _status = mRemote.transact(Stub.TRANSACTION_connectedVPN, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_newLogItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_updateStateString = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_updateByteCount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_connectedVPN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
  }
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.core.IStatusCallbacks";
  /** Called when the service has a new status for you. */
  public void newLogItem(de.blinkt.openvpn.core.LogItem item) throws android.os.RemoteException;
  public void updateStateString(java.lang.String state, java.lang.String msg, int resid, de.blinkt.openvpn.core.ConnectionStatus level, android.content.Intent intent) throws android.os.RemoteException;
  public void updateByteCount(long inBytes, long outBytes) throws android.os.RemoteException;
  public void connectedVPN(java.lang.String uuid) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}


/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package de.blinkt.openvpn.api;
public interface IOpenVPNAPIService extends android.os.IInterface
{
  /** Default implementation for IOpenVPNAPIService. */
  public static class Default implements de.blinkt.openvpn.api.IOpenVPNAPIService
  {
    @Override public java.util.List<de.blinkt.openvpn.api.APIVpnProfile> getProfiles() throws android.os.RemoteException
    {
      return null;
    }
    @Override public void startProfile(java.lang.String profileUUID) throws android.os.RemoteException
    {
    }
    /**
     * Use a profile with all certificates etc. embedded,
     * old version which does not return the UUID of the addded profile, see
     * below for a version that return the UUID on add
     */
    @Override public boolean addVPNProfile(java.lang.String name, java.lang.String config) throws android.os.RemoteException
    {
      return false;
    }
    /**
     * start a profile using a config as inline string. Make sure that all needed data is inlined,
     * e.g., using <ca>...</ca> or <auth-user-pass>...</auth-user-pass>
     * See the OpenVPN manual page for more on inlining files
     */
    @Override public void startVPN(java.lang.String inlineconfig) throws android.os.RemoteException
    {
    }
    /**
     * This permission framework is used  to avoid confused deputy style attack to the VPN
     * calling this will give null if the app is allowed to use the external API and an Intent
     * that can be launched to request permissions otherwise
     */
    @Override public android.content.Intent prepare(java.lang.String packagename) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
     * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
     * to let OpenVPN for Android request the permission
     */
    @Override public android.content.Intent prepareVPNService() throws android.os.RemoteException
    {
      return null;
    }
    /** Disconnect the VPN */
    @Override public void disconnect() throws android.os.RemoteException
    {
    }
    /** Pause the VPN (same as using the pause feature in the notifcation bar) */
    @Override public void pause() throws android.os.RemoteException
    {
    }
    /** Resume the VPN (same as using the pause feature in the notifcation bar) */
    @Override public void resume() throws android.os.RemoteException
    {
    }
    /** Registers to receive OpenVPN Status Updates */
    @Override public void registerStatusCallback(de.blinkt.openvpn.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
    {
    }
    /** Remove a previously registered callback interface. */
    @Override public void unregisterStatusCallback(de.blinkt.openvpn.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
    {
    }
    /** Remove a profile by UUID */
    @Override public void removeProfile(java.lang.String profileUUID) throws android.os.RemoteException
    {
    }
    /**
     * Request a socket to be protected as a VPN socket would be. Useful for creating
     * a helper socket for an app controlling OpenVPN
     * Before calling this function you should make sure OpenVPN for Android may actually
     * this function by checking if prepareVPNService returns null;
     */
    @Override public boolean protectSocket(android.os.ParcelFileDescriptor fd) throws android.os.RemoteException
    {
      return false;
    }
    /** Use a profile with all certificates etc. embedded */
    @Override public de.blinkt.openvpn.api.APIVpnProfile addNewVPNProfile(java.lang.String name, boolean userEditable, java.lang.String config) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.api.IOpenVPNAPIService
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.api.IOpenVPNAPIService interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.api.IOpenVPNAPIService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.api.IOpenVPNAPIService))) {
        return ((de.blinkt.openvpn.api.IOpenVPNAPIService)iin);
      }
      return new de.blinkt.openvpn.api.IOpenVPNAPIService.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_getProfiles:
        {
          java.util.List<de.blinkt.openvpn.api.APIVpnProfile> _result = this.getProfiles();
          reply.writeNoException();
          _Parcel.writeTypedList(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_startProfile:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.startProfile(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_addVPNProfile:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          boolean _result = this.addVPNProfile(_arg0, _arg1);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_startVPN:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.startVPN(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_prepare:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.content.Intent _result = this.prepare(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_prepareVPNService:
        {
          android.content.Intent _result = this.prepareVPNService();
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_disconnect:
        {
          this.disconnect();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_pause:
        {
          this.pause();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_resume:
        {
          this.resume();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_registerStatusCallback:
        {
          de.blinkt.openvpn.api.IOpenVPNStatusCallback _arg0;
          _arg0 = de.blinkt.openvpn.api.IOpenVPNStatusCallback.Stub.asInterface(data.readStrongBinder());
          this.registerStatusCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_unregisterStatusCallback:
        {
          de.blinkt.openvpn.api.IOpenVPNStatusCallback _arg0;
          _arg0 = de.blinkt.openvpn.api.IOpenVPNStatusCallback.Stub.asInterface(data.readStrongBinder());
          this.unregisterStatusCallback(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_removeProfile:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          this.removeProfile(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_protectSocket:
        {
          android.os.ParcelFileDescriptor _arg0;
          _arg0 = _Parcel.readTypedObject(data, android.os.ParcelFileDescriptor.CREATOR);
          boolean _result = this.protectSocket(_arg0);
          reply.writeNoException();
          reply.writeInt(((_result)?(1):(0)));
          break;
        }
        case TRANSACTION_addNewVPNProfile:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          boolean _arg1;
          _arg1 = (0!=data.readInt());
          java.lang.String _arg2;
          _arg2 = data.readString();
          de.blinkt.openvpn.api.APIVpnProfile _result = this.addNewVPNProfile(_arg0, _arg1, _arg2);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.api.IOpenVPNAPIService
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      @Override public java.util.List<de.blinkt.openvpn.api.APIVpnProfile> getProfiles() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.List<de.blinkt.openvpn.api.APIVpnProfile> _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getProfiles, _data, _reply, 0);
          _reply.readException();
          _result = _reply.createTypedArrayList(de.blinkt.openvpn.api.APIVpnProfile.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override public void startProfile(java.lang.String profileUUID) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(profileUUID);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startProfile, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * Use a profile with all certificates etc. embedded,
       * old version which does not return the UUID of the addded profile, see
       * below for a version that return the UUID on add
       */
      @Override public boolean addVPNProfile(java.lang.String name, java.lang.String config) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(name);
          _data.writeString(config);
          boolean _status = mRemote.transact(Stub.TRANSACTION_addVPNProfile, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * start a profile using a config as inline string. Make sure that all needed data is inlined,
       * e.g., using <ca>...</ca> or <auth-user-pass>...</auth-user-pass>
       * See the OpenVPN manual page for more on inlining files
       */
      @Override public void startVPN(java.lang.String inlineconfig) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(inlineconfig);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startVPN, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * This permission framework is used  to avoid confused deputy style attack to the VPN
       * calling this will give null if the app is allowed to use the external API and an Intent
       * that can be launched to request permissions otherwise
       */
      @Override public android.content.Intent prepare(java.lang.String packagename) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.content.Intent _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(packagename);
          boolean _status = mRemote.transact(Stub.TRANSACTION_prepare, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.content.Intent.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
       * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
       * to let OpenVPN for Android request the permission
       */
      @Override public android.content.Intent prepareVPNService() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.content.Intent _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_prepareVPNService, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.content.Intent.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Disconnect the VPN */
      @Override public void disconnect() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_disconnect, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Pause the VPN (same as using the pause feature in the notifcation bar) */
      @Override public void pause() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Resume the VPN (same as using the pause feature in the notifcation bar) */
      @Override public void resume() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_resume, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Registers to receive OpenVPN Status Updates */
      @Override public void registerStatusCallback(de.blinkt.openvpn.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(cb);
          boolean _status = mRemote.transact(Stub.TRANSACTION_registerStatusCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Remove a previously registered callback interface. */
      @Override public void unregisterStatusCallback(de.blinkt.openvpn.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(cb);
          boolean _status = mRemote.transact(Stub.TRANSACTION_unregisterStatusCallback, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Remove a profile by UUID */
      @Override public void removeProfile(java.lang.String profileUUID) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(profileUUID);
          boolean _status = mRemote.transact(Stub.TRANSACTION_removeProfile, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * Request a socket to be protected as a VPN socket would be. Useful for creating
       * a helper socket for an app controlling OpenVPN
       * Before calling this function you should make sure OpenVPN for Android may actually
       * this function by checking if prepareVPNService returns null;
       */
      @Override public boolean protectSocket(android.os.ParcelFileDescriptor fd) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _Parcel.writeTypedObject(_data, fd, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_protectSocket, _data, _reply, 0);
          _reply.readException();
          _result = (0!=_reply.readInt());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Use a profile with all certificates etc. embedded */
      @Override public de.blinkt.openvpn.api.APIVpnProfile addNewVPNProfile(java.lang.String name, boolean userEditable, java.lang.String config) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        de.blinkt.openvpn.api.APIVpnProfile _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(name);
          _data.writeInt(((userEditable)?(1):(0)));
          _data.writeString(config);
          boolean _status = mRemote.transact(Stub.TRANSACTION_addNewVPNProfile, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, de.blinkt.openvpn.api.APIVpnProfile.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_getProfiles = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_startProfile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_addVPNProfile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_startVPN = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_prepare = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_prepareVPNService = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_disconnect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_pause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_resume = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_registerStatusCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_unregisterStatusCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_removeProfile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_protectSocket = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_addNewVPNProfile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
  }
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.api.IOpenVPNAPIService";
  public java.util.List<de.blinkt.openvpn.api.APIVpnProfile> getProfiles() throws android.os.RemoteException;
  public void startProfile(java.lang.String profileUUID) throws android.os.RemoteException;
  /**
   * Use a profile with all certificates etc. embedded,
   * old version which does not return the UUID of the addded profile, see
   * below for a version that return the UUID on add
   */
  public boolean addVPNProfile(java.lang.String name, java.lang.String config) throws android.os.RemoteException;
  /**
   * start a profile using a config as inline string. Make sure that all needed data is inlined,
   * e.g., using <ca>...</ca> or <auth-user-pass>...</auth-user-pass>
   * See the OpenVPN manual page for more on inlining files
   */
  public void startVPN(java.lang.String inlineconfig) throws android.os.RemoteException;
  /**
   * This permission framework is used  to avoid confused deputy style attack to the VPN
   * calling this will give null if the app is allowed to use the external API and an Intent
   * that can be launched to request permissions otherwise
   */
  public android.content.Intent prepare(java.lang.String packagename) throws android.os.RemoteException;
  /**
   * Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
   * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
   * to let OpenVPN for Android request the permission
   */
  public android.content.Intent prepareVPNService() throws android.os.RemoteException;
  /** Disconnect the VPN */
  public void disconnect() throws android.os.RemoteException;
  /** Pause the VPN (same as using the pause feature in the notifcation bar) */
  public void pause() throws android.os.RemoteException;
  /** Resume the VPN (same as using the pause feature in the notifcation bar) */
  public void resume() throws android.os.RemoteException;
  /** Registers to receive OpenVPN Status Updates */
  public void registerStatusCallback(de.blinkt.openvpn.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException;
  /** Remove a previously registered callback interface. */
  public void unregisterStatusCallback(de.blinkt.openvpn.api.IOpenVPNStatusCallback cb) throws android.os.RemoteException;
  /** Remove a profile by UUID */
  public void removeProfile(java.lang.String profileUUID) throws android.os.RemoteException;
  /**
   * Request a socket to be protected as a VPN socket would be. Useful for creating
   * a helper socket for an app controlling OpenVPN
   * Before calling this function you should make sure OpenVPN for Android may actually
   * this function by checking if prepareVPNService returns null;
   */
  public boolean protectSocket(android.os.ParcelFileDescriptor fd) throws android.os.RemoteException;
  /** Use a profile with all certificates etc. embedded */
  public de.blinkt.openvpn.api.APIVpnProfile addNewVPNProfile(java.lang.String name, boolean userEditable, java.lang.String config) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedList(
        android.os.Parcel parcel, java.util.List<T> value, int parcelableFlags) {
      if (value == null) {
        parcel.writeInt(-1);
      } else {
        int N = value.size();
        int i = 0;
        parcel.writeInt(N);
        while (i < N) {
    writeTypedObject(parcel, value.get(i), parcelableFlags);
          i++;
        }
      }
    }
  }
}


/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package de.blinkt.openvpn.api;
/**
 * This is very simple interface that is specialised to have only the minimal set of crypto
 * operation that are needed for OpenVPN to authenticate with an external certificate
 */
public interface ExternalCertificateProvider extends android.os.IInterface
{
  /** Default implementation for ExternalCertificateProvider. */
  public static class Default implements de.blinkt.openvpn.api.ExternalCertificateProvider
  {
    /**
     * Requests signing the data with RSA/ECB/PKCS1PADDING
     * for RSA certficate and with NONEwithECDSA for EC certificates
     * @parm alias the parameter that
     */
    @Override public byte[] getSignedData(java.lang.String alias, byte[] data) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Requests the certificate chain for the selected alias
     * The first certifcate returned is assumed to be
     * the user certificate
     */
    @Override public byte[] getCertificateChain(java.lang.String alias) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * This function is called for the app to get additional meta information from the
     * external provider and will be called with the stored alias in the app
     * 
     * For external app provider that do not provide an activity to configure them, this
     * is used to get the alias that should be used.
     * The format is the same as the activity should return, i.e.
     * 
     * EXTRA_ALIAS = "de.blinkt.openvpn.api.KEY_ALIAS"
     * EXTRA_DESCRIPTION = "de.blinkt.openvpn.api.KEY_DESCRIPTION"
     * 
     * as the keys for the bundle.
     */
    @Override public android.os.Bundle getCertificateMetaData(java.lang.String alias) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.api.ExternalCertificateProvider
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.api.ExternalCertificateProvider interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.api.ExternalCertificateProvider asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.api.ExternalCertificateProvider))) {
        return ((de.blinkt.openvpn.api.ExternalCertificateProvider)iin);
      }
      return new de.blinkt.openvpn.api.ExternalCertificateProvider.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_getSignedData:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          byte[] _arg1;
          _arg1 = data.createByteArray();
          byte[] _result = this.getSignedData(_arg0, _arg1);
          reply.writeNoException();
          reply.writeByteArray(_result);
          break;
        }
        case TRANSACTION_getCertificateChain:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          byte[] _result = this.getCertificateChain(_arg0);
          reply.writeNoException();
          reply.writeByteArray(_result);
          break;
        }
        case TRANSACTION_getCertificateMetaData:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.os.Bundle _result = this.getCertificateMetaData(_arg0);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.api.ExternalCertificateProvider
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
       * Requests signing the data with RSA/ECB/PKCS1PADDING
       * for RSA certficate and with NONEwithECDSA for EC certificates
       * @parm alias the parameter that
       */
      @Override public byte[] getSignedData(java.lang.String alias, byte[] data) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          _data.writeByteArray(data);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSignedData, _data, _reply, 0);
          _reply.readException();
          _result = _reply.createByteArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Requests the certificate chain for the selected alias
       * The first certifcate returned is assumed to be
       * the user certificate
       */
      @Override public byte[] getCertificateChain(java.lang.String alias) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        byte[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCertificateChain, _data, _reply, 0);
          _reply.readException();
          _result = _reply.createByteArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * This function is called for the app to get additional meta information from the
       * external provider and will be called with the stored alias in the app
       * 
       * For external app provider that do not provide an activity to configure them, this
       * is used to get the alias that should be used.
       * The format is the same as the activity should return, i.e.
       * 
       * EXTRA_ALIAS = "de.blinkt.openvpn.api.KEY_ALIAS"
       * EXTRA_DESCRIPTION = "de.blinkt.openvpn.api.KEY_DESCRIPTION"
       * 
       * as the keys for the bundle.
       */
      @Override public android.os.Bundle getCertificateMetaData(java.lang.String alias) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(alias);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCertificateMetaData, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_getSignedData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getCertificateChain = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getCertificateMetaData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
  }
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.api.ExternalCertificateProvider";
  /**
   * Requests signing the data with RSA/ECB/PKCS1PADDING
   * for RSA certficate and with NONEwithECDSA for EC certificates
   * @parm alias the parameter that
   */
  public byte[] getSignedData(java.lang.String alias, byte[] data) throws android.os.RemoteException;
  /**
   * Requests the certificate chain for the selected alias
   * The first certifcate returned is assumed to be
   * the user certificate
   */
  public byte[] getCertificateChain(java.lang.String alias) throws android.os.RemoteException;
  /**
   * This function is called for the app to get additional meta information from the
   * external provider and will be called with the stored alias in the app
   * 
   * For external app provider that do not provide an activity to configure them, this
   * is used to get the alias that should be used.
   * The format is the same as the activity should return, i.e.
   * 
   * EXTRA_ALIAS = "de.blinkt.openvpn.api.KEY_ALIAS"
   * EXTRA_DESCRIPTION = "de.blinkt.openvpn.api.KEY_DESCRIPTION"
   * 
   * as the keys for the bundle.
   */
  public android.os.Bundle getCertificateMetaData(java.lang.String alias) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}


/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package de.blinkt.openvpn.api;
/**
 * Example of a callback interface used by IRemoteService to send
 * synchronous notifications back to its clients.  Note that this is a
 * one-way interface so the server does not block waiting for the client.
 */
public interface IOpenVPNStatusCallback extends android.os.IInterface
{
  /** Default implementation for IOpenVPNStatusCallback. */
  public static class Default implements de.blinkt.openvpn.api.IOpenVPNStatusCallback
  {
    /** Called when the service has a new status for you. */
    @Override public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements de.blinkt.openvpn.api.IOpenVPNStatusCallback
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an de.blinkt.openvpn.api.IOpenVPNStatusCallback interface,
     * generating a proxy if needed.
     */
    public static de.blinkt.openvpn.api.IOpenVPNStatusCallback asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof de.blinkt.openvpn.api.IOpenVPNStatusCallback))) {
        return ((de.blinkt.openvpn.api.IOpenVPNStatusCallback)iin);
      }
      return new de.blinkt.openvpn.api.IOpenVPNStatusCallback.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_newStatus:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          java.lang.String _arg3;
          _arg3 = data.readString();
          this.newStatus(_arg0, _arg1, _arg2, _arg3);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements de.blinkt.openvpn.api.IOpenVPNStatusCallback
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /** Called when the service has a new status for you. */
      @Override public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(uuid);
          _data.writeString(state);
          _data.writeString(message);
          _data.writeString(level);
          boolean _status = mRemote.transact(Stub.TRANSACTION_newStatus, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_newStatus = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
  }
  public static final java.lang.String DESCRIPTOR = "de.blinkt.openvpn.api.IOpenVPNStatusCallback";
  /** Called when the service has a new status for you. */
  public void newStatus(java.lang.String uuid, java.lang.String state, java.lang.String message, java.lang.String level) throws android.os.RemoteException;
}


/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.android.vending.billing;
/**
 * InAppBillingService is the service that provides in-app billing version 3 and beyond.
 * This service provides the following features:
 * 1. Provides a new API to get details of in-app items published for the app including
 *    price, type, title and description.
 * 2. The purchase flow is synchronous and purchase information is available immediately
 *    after it completes.
 * 3. Purchase information of in-app purchases is maintained within the Google Play system
 *    till the purchase is consumed.
 * 4. An API to consume a purchase of an inapp item. All purchases of one-time
 *    in-app items are consumable and thereafter can be purchased again.
 * 5. An API to get current purchases of the user immediately. This will not contain any
 *    consumed purchases.
 * 
 * All calls will give a response code with the following possible values
 * RESULT_OK = 0 - success
 * RESULT_USER_CANCELED = 1 - user pressed back or canceled a dialog
 * RESULT_BILLING_UNAVAILABLE = 3 - this billing API version is not supported for the type requested
 * RESULT_ITEM_UNAVAILABLE = 4 - requested SKU is not available for purchase
 * RESULT_DEVELOPER_ERROR = 5 - invalid arguments provided to the API
 * RESULT_ERROR = 6 - Fatal error during the API action
 * RESULT_ITEM_ALREADY_OWNED = 7 - Failure to purchase since item is already owned
 * RESULT_ITEM_NOT_OWNED = 8 - Failure to consume since item is not owned
 */
public interface IInAppBillingService extends android.os.IInterface
{
  /** Default implementation for IInAppBillingService. */
  public static class Default implements com.android.vending.billing.IInAppBillingService
  {
    /**
     * Checks support for the requested billing API version, package and in-app type.
     * Minimum API version supported by this interface is 3.
     * @param apiVersion the billing version which the app is using
     * @param packageName the package name of the calling app
     * @param type type of the in-app item being purchased "inapp" for one-time purchases
     *        and "subs" for subscription.
     * @return RESULT_OK(0) on success, corresponding result code on failures
     */
    @Override public int isBillingSupported(int apiVersion, java.lang.String packageName, java.lang.String type) throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Provides details of a list of SKUs
     * Given a list of SKUs of a valid type in the skusBundle, this returns a bundle
     * with a list JSON strings containing the productId, price, title and description.
     * This API can be called with a maximum of 20 SKUs.
     * @param apiVersion billing API version that the Third-party is using
     * @param packageName the package name of the calling app
     * @param skusBundle bundle containing a StringArrayList of SKUs with key "ITEM_ID_LIST"
     * @return Bundle containing the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.
     *         "DETAILS_LIST" with a StringArrayList containing purchase information
     *              in JSON format similar to:
     *              '{ "productId" : "exampleSku", "type" : "inapp", "price" : "$5.00",
     *                 "title : "Example Title", "description" : "This is an example description" }'
     */
    @Override public android.os.Bundle getSkuDetails(int apiVersion, java.lang.String packageName, java.lang.String type, android.os.Bundle skusBundle) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Returns a pending intent to launch the purchase flow for an in-app item by providing a SKU,
     * the type, a unique purchase token and an optional developer payload.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param sku the SKU of the in-app item as published in the developer console
     * @param type the type of the in-app item ("inapp" for one-time purchases
     *        and "subs" for subscription).
     * @param developerPayload optional argument to be sent back with the purchase information
     * @return Bundle containing the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.
     *         "BUY_INTENT" - PendingIntent to start the purchase flow
     * 
     * The Pending intent should be launched with startIntentSenderForResult. When purchase flow
     * has completed, the onActivityResult() will give a resultCode of OK or CANCELED.
     * If the purchase is successful, the result data will contain the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.
     *         "INAPP_PURCHASE_DATA" - String in JSON format similar to
     *              '{"orderId":"12999763169054705758.1371079406387615",
     *                "packageName":"com.example.app",
     *                "productId":"exampleSku",
     *                "purchaseTime":1345678900000,
     *                "purchaseToken" : "122333444455555",
     *                "developerPayload":"example developer payload" }'
     *         "INAPP_DATA_SIGNATURE" - String containing the signature of the purchase data that
     *                                  was signed with the private key of the developer
     *                                  TODO: change this to app-specific keys.
     */
    @Override public android.os.Bundle getBuyIntent(int apiVersion, java.lang.String packageName, java.lang.String sku, java.lang.String type, java.lang.String developerPayload) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Returns the current SKUs owned by the user of the type and package name specified along with
     * purchase information and a signature of the data to be validated.
     * This will return all SKUs that have been purchased in V3 and managed items purchased using
     * V1 and V2 that have not been consumed.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param type the type of the in-app items being requested
     *        ("inapp" for one-time purchases and "subs" for subscription).
     * @param continuationToken to be set as null for the first call, if the number of owned
     *        skus are too many, a continuationToken is returned in the response bundle.
     *        This method can be called again with the continuation token to get the next set of
     *        owned skus.
     * @return Bundle containing the following key-value pairs
     *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
     *              failure as listed above.
     *         "INAPP_PURCHASE_ITEM_LIST" - StringArrayList containing the list of SKUs
     *         "INAPP_PURCHASE_DATA_LIST" - StringArrayList containing the purchase information
     *         "INAPP_DATA_SIGNATURE_LIST"- StringArrayList containing the signatures
     *                                      of the purchase information
     *         "INAPP_CONTINUATION_TOKEN" - String containing a continuation token for the
     *                                      next set of in-app purchases. Only set if the
     *                                      user has more owned skus than the current list.
     */
    @Override public android.os.Bundle getPurchases(int apiVersion, java.lang.String packageName, java.lang.String type, java.lang.String continuationToken) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Consume the last purchase of the given SKU. This will result in this item being removed
     * from all subsequent responses to getPurchases() and allow re-purchase of this item.
     * @param apiVersion billing API version that the app is using
     * @param packageName package name of the calling app
     * @param purchaseToken token in the purchase information JSON that identifies the purchase
     *        to be consumed
     * @return 0 if consumption succeeded. Appropriate error values for failures.
     */
    @Override public int consumePurchase(int apiVersion, java.lang.String packageName, java.lang.String purchaseToken) throws android.os.RemoteException
    {
      return 0;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements com.android.vending.billing.IInAppBillingService
  {
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.android.vending.billing.IInAppBillingService interface,
     * generating a proxy if needed.
     */
    public static com.android.vending.billing.IInAppBillingService asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof com.android.vending.billing.IInAppBillingService))) {
        return ((com.android.vending.billing.IInAppBillingService)iin);
      }
      return new com.android.vending.billing.IInAppBillingService.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      java.lang.String descriptor = DESCRIPTOR;
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(descriptor);
      }
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
      }
      switch (code)
      {
        case TRANSACTION_isBillingSupported:
        {
          int _arg0;
          _arg0 = data.readInt();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          int _result = this.isBillingSupported(_arg0, _arg1, _arg2);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getSkuDetails:
        {
          int _arg0;
          _arg0 = data.readInt();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          android.os.Bundle _arg3;
          _arg3 = _Parcel.readTypedObject(data, android.os.Bundle.CREATOR);
          android.os.Bundle _result = this.getSkuDetails(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getBuyIntent:
        {
          int _arg0;
          _arg0 = data.readInt();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          java.lang.String _arg3;
          _arg3 = data.readString();
          java.lang.String _arg4;
          _arg4 = data.readString();
          android.os.Bundle _result = this.getBuyIntent(_arg0, _arg1, _arg2, _arg3, _arg4);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getPurchases:
        {
          int _arg0;
          _arg0 = data.readInt();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          java.lang.String _arg3;
          _arg3 = data.readString();
          android.os.Bundle _result = this.getPurchases(_arg0, _arg1, _arg2, _arg3);
          reply.writeNoException();
          _Parcel.writeTypedObject(reply, _result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_consumePurchase:
        {
          int _arg0;
          _arg0 = data.readInt();
          java.lang.String _arg1;
          _arg1 = data.readString();
          java.lang.String _arg2;
          _arg2 = data.readString();
          int _result = this.consumePurchase(_arg0, _arg1, _arg2);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static class Proxy implements com.android.vending.billing.IInAppBillingService
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
       * Checks support for the requested billing API version, package and in-app type.
       * Minimum API version supported by this interface is 3.
       * @param apiVersion the billing version which the app is using
       * @param packageName the package name of the calling app
       * @param type type of the in-app item being purchased "inapp" for one-time purchases
       *        and "subs" for subscription.
       * @return RESULT_OK(0) on success, corresponding result code on failures
       */
      @Override public int isBillingSupported(int apiVersion, java.lang.String packageName, java.lang.String type) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(apiVersion);
          _data.writeString(packageName);
          _data.writeString(type);
          boolean _status = mRemote.transact(Stub.TRANSACTION_isBillingSupported, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Provides details of a list of SKUs
       * Given a list of SKUs of a valid type in the skusBundle, this returns a bundle
       * with a list JSON strings containing the productId, price, title and description.
       * This API can be called with a maximum of 20 SKUs.
       * @param apiVersion billing API version that the Third-party is using
       * @param packageName the package name of the calling app
       * @param skusBundle bundle containing a StringArrayList of SKUs with key "ITEM_ID_LIST"
       * @return Bundle containing the following key-value pairs
       *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
       *              failure as listed above.
       *         "DETAILS_LIST" with a StringArrayList containing purchase information
       *              in JSON format similar to:
       *              '{ "productId" : "exampleSku", "type" : "inapp", "price" : "$5.00",
       *                 "title : "Example Title", "description" : "This is an example description" }'
       */
      @Override public android.os.Bundle getSkuDetails(int apiVersion, java.lang.String packageName, java.lang.String type, android.os.Bundle skusBundle) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(apiVersion);
          _data.writeString(packageName);
          _data.writeString(type);
          _Parcel.writeTypedObject(_data, skusBundle, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSkuDetails, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Returns a pending intent to launch the purchase flow for an in-app item by providing a SKU,
       * the type, a unique purchase token and an optional developer payload.
       * @param apiVersion billing API version that the app is using
       * @param packageName package name of the calling app
       * @param sku the SKU of the in-app item as published in the developer console
       * @param type the type of the in-app item ("inapp" for one-time purchases
       *        and "subs" for subscription).
       * @param developerPayload optional argument to be sent back with the purchase information
       * @return Bundle containing the following key-value pairs
       *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
       *              failure as listed above.
       *         "BUY_INTENT" - PendingIntent to start the purchase flow
       * 
       * The Pending intent should be launched with startIntentSenderForResult. When purchase flow
       * has completed, the onActivityResult() will give a resultCode of OK or CANCELED.
       * If the purchase is successful, the result data will contain the following key-value pairs
       *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
       *              failure as listed above.
       *         "INAPP_PURCHASE_DATA" - String in JSON format similar to
       *              '{"orderId":"12999763169054705758.1371079406387615",
       *                "packageName":"com.example.app",
       *                "productId":"exampleSku",
       *                "purchaseTime":1345678900000,
       *                "purchaseToken" : "122333444455555",
       *                "developerPayload":"example developer payload" }'
       *         "INAPP_DATA_SIGNATURE" - String containing the signature of the purchase data that
       *                                  was signed with the private key of the developer
       *                                  TODO: change this to app-specific keys.
       */
      @Override public android.os.Bundle getBuyIntent(int apiVersion, java.lang.String packageName, java.lang.String sku, java.lang.String type, java.lang.String developerPayload) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(apiVersion);
          _data.writeString(packageName);
          _data.writeString(sku);
          _data.writeString(type);
          _data.writeString(developerPayload);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getBuyIntent, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Returns the current SKUs owned by the user of the type and package name specified along with
       * purchase information and a signature of the data to be validated.
       * This will return all SKUs that have been purchased in V3 and managed items purchased using
       * V1 and V2 that have not been consumed.
       * @param apiVersion billing API version that the app is using
       * @param packageName package name of the calling app
       * @param type the type of the in-app items being requested
       *        ("inapp" for one-time purchases and "subs" for subscription).
       * @param continuationToken to be set as null for the first call, if the number of owned
       *        skus are too many, a continuationToken is returned in the response bundle.
       *        This method can be called again with the continuation token to get the next set of
       *        owned skus.
       * @return Bundle containing the following key-value pairs
       *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
       *              failure as listed above.
       *         "INAPP_PURCHASE_ITEM_LIST" - StringArrayList containing the list of SKUs
       *         "INAPP_PURCHASE_DATA_LIST" - StringArrayList containing the purchase information
       *         "INAPP_DATA_SIGNATURE_LIST"- StringArrayList containing the signatures
       *                                      of the purchase information
       *         "INAPP_CONTINUATION_TOKEN" - String containing a continuation token for the
       *                                      next set of in-app purchases. Only set if the
       *                                      user has more owned skus than the current list.
       */
      @Override public android.os.Bundle getPurchases(int apiVersion, java.lang.String packageName, java.lang.String type, java.lang.String continuationToken) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.Bundle _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(apiVersion);
          _data.writeString(packageName);
          _data.writeString(type);
          _data.writeString(continuationToken);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPurchases, _data, _reply, 0);
          _reply.readException();
          _result = _Parcel.readTypedObject(_reply, android.os.Bundle.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Consume the last purchase of the given SKU. This will result in this item being removed
       * from all subsequent responses to getPurchases() and allow re-purchase of this item.
       * @param apiVersion billing API version that the app is using
       * @param packageName package name of the calling app
       * @param purchaseToken token in the purchase information JSON that identifies the purchase
       *        to be consumed
       * @return 0 if consumption succeeded. Appropriate error values for failures.
       */
      @Override public int consumePurchase(int apiVersion, java.lang.String packageName, java.lang.String purchaseToken) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(apiVersion);
          _data.writeString(packageName);
          _data.writeString(purchaseToken);
          boolean _status = mRemote.transact(Stub.TRANSACTION_consumePurchase, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_isBillingSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getSkuDetails = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getBuyIntent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_getPurchases = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_consumePurchase = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
  }
  public static final java.lang.String DESCRIPTOR = "com.android.vending.billing.IInAppBillingService";
  /**
   * Checks support for the requested billing API version, package and in-app type.
   * Minimum API version supported by this interface is 3.
   * @param apiVersion the billing version which the app is using
   * @param packageName the package name of the calling app
   * @param type type of the in-app item being purchased "inapp" for one-time purchases
   *        and "subs" for subscription.
   * @return RESULT_OK(0) on success, corresponding result code on failures
   */
  public int isBillingSupported(int apiVersion, java.lang.String packageName, java.lang.String type) throws android.os.RemoteException;
  /**
   * Provides details of a list of SKUs
   * Given a list of SKUs of a valid type in the skusBundle, this returns a bundle
   * with a list JSON strings containing the productId, price, title and description.
   * This API can be called with a maximum of 20 SKUs.
   * @param apiVersion billing API version that the Third-party is using
   * @param packageName the package name of the calling app
   * @param skusBundle bundle containing a StringArrayList of SKUs with key "ITEM_ID_LIST"
   * @return Bundle containing the following key-value pairs
   *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
   *              failure as listed above.
   *         "DETAILS_LIST" with a StringArrayList containing purchase information
   *              in JSON format similar to:
   *              '{ "productId" : "exampleSku", "type" : "inapp", "price" : "$5.00",
   *                 "title : "Example Title", "description" : "This is an example description" }'
   */
  public android.os.Bundle getSkuDetails(int apiVersion, java.lang.String packageName, java.lang.String type, android.os.Bundle skusBundle) throws android.os.RemoteException;
  /**
   * Returns a pending intent to launch the purchase flow for an in-app item by providing a SKU,
   * the type, a unique purchase token and an optional developer payload.
   * @param apiVersion billing API version that the app is using
   * @param packageName package name of the calling app
   * @param sku the SKU of the in-app item as published in the developer console
   * @param type the type of the in-app item ("inapp" for one-time purchases
   *        and "subs" for subscription).
   * @param developerPayload optional argument to be sent back with the purchase information
   * @return Bundle containing the following key-value pairs
   *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
   *              failure as listed above.
   *         "BUY_INTENT" - PendingIntent to start the purchase flow
   * 
   * The Pending intent should be launched with startIntentSenderForResult. When purchase flow
   * has completed, the onActivityResult() will give a resultCode of OK or CANCELED.
   * If the purchase is successful, the result data will contain the following key-value pairs
   *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
   *              failure as listed above.
   *         "INAPP_PURCHASE_DATA" - String in JSON format similar to
   *              '{"orderId":"12999763169054705758.1371079406387615",
   *                "packageName":"com.example.app",
   *                "productId":"exampleSku",
   *                "purchaseTime":1345678900000,
   *                "purchaseToken" : "122333444455555",
   *                "developerPayload":"example developer payload" }'
   *         "INAPP_DATA_SIGNATURE" - String containing the signature of the purchase data that
   *                                  was signed with the private key of the developer
   *                                  TODO: change this to app-specific keys.
   */
  public android.os.Bundle getBuyIntent(int apiVersion, java.lang.String packageName, java.lang.String sku, java.lang.String type, java.lang.String developerPayload) throws android.os.RemoteException;
  /**
   * Returns the current SKUs owned by the user of the type and package name specified along with
   * purchase information and a signature of the data to be validated.
   * This will return all SKUs that have been purchased in V3 and managed items purchased using
   * V1 and V2 that have not been consumed.
   * @param apiVersion billing API version that the app is using
   * @param packageName package name of the calling app
   * @param type the type of the in-app items being requested
   *        ("inapp" for one-time purchases and "subs" for subscription).
   * @param continuationToken to be set as null for the first call, if the number of owned
   *        skus are too many, a continuationToken is returned in the response bundle.
   *        This method can be called again with the continuation token to get the next set of
   *        owned skus.
   * @return Bundle containing the following key-value pairs
   *         "RESPONSE_CODE" with int value, RESULT_OK(0) if success, other response codes on
   *              failure as listed above.
   *         "INAPP_PURCHASE_ITEM_LIST" - StringArrayList containing the list of SKUs
   *         "INAPP_PURCHASE_DATA_LIST" - StringArrayList containing the purchase information
   *         "INAPP_DATA_SIGNATURE_LIST"- StringArrayList containing the signatures
   *                                      of the purchase information
   *         "INAPP_CONTINUATION_TOKEN" - String containing a continuation token for the
   *                                      next set of in-app purchases. Only set if the
   *                                      user has more owned skus than the current list.
   */
  public android.os.Bundle getPurchases(int apiVersion, java.lang.String packageName, java.lang.String type, java.lang.String continuationToken) throws android.os.RemoteException;
  /**
   * Consume the last purchase of the given SKU. This will result in this item being removed
   * from all subsequent responses to getPurchases() and allow re-purchase of this item.
   * @param apiVersion billing API version that the app is using
   * @param packageName package name of the calling app
   * @param purchaseToken token in the purchase information JSON that identifies the purchase
   *        to be consumed
   * @return 0 if consumption succeeded. Appropriate error values for failures.
   */
  public int consumePurchase(int apiVersion, java.lang.String packageName, java.lang.String purchaseToken) throws android.os.RemoteException;
  /** @hide */
  static class _Parcel {
    static private <T> T readTypedObject(
        android.os.Parcel parcel,
        android.os.Parcelable.Creator<T> c) {
      if (parcel.readInt() != 0) {
          return c.createFromParcel(parcel);
      } else {
          return null;
      }
    }
    static private <T extends android.os.Parcelable> void writeTypedObject(
        android.os.Parcel parcel, T value, int parcelableFlags) {
      if (value != null) {
        parcel.writeInt(1);
        value.writeToParcel(parcel, parcelableFlags);
      } else {
        parcel.writeInt(0);
      }
    }
  }
}


/**
 * Automatically generated file. DO NOT MODIFY
 */
package de.blinkt.openvpn;

public final class BuildConfig {
  public static final boolean DEBUG = Boolean.parseBoolean("true");
  public static final String LIBRARY_PACKAGE_NAME = "de.blinkt.openvpn";
  public static final String BUILD_TYPE = "debug";
}


package de.blinkt.openvpn;

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

        assertEquals("de.blinkt.openvpn.test", appContext.getPackageName());
    }
}


package de.blinkt.openvpn;

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

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.io.pem;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.spongycastle.util.encoders.Base64;

/**
 * A generic PEM writer, based on RFC 1421
 */
@SuppressWarnings("all")
public class PemWriter
    extends BufferedWriter
{
    private static final int LINE_LENGTH = 64;

    private final int nlLength;
    private char[]  buf = new char[LINE_LENGTH];

    /**
     * Base constructor.
     *
     * @param out output stream to use.
     */
    public PemWriter(Writer out)
    {
        super(out);

        String nl = System.getProperty("line.separator");
        if (nl != null)
        {
            nlLength = nl.length();
        }
        else
        {
            nlLength = 2;
        }
    }

    /**
     * Return the number of bytes or characters required to contain the
     * passed in object if it is PEM encoded.
     *
     * @param obj pem object to be output
     * @return an estimate of the number of bytes
     */
    public int getOutputSize(PemObject obj)
    {
        // BEGIN and END boundaries.
        int size = (2 * (obj.getType().length() + 10 + nlLength)) + 6 + 4;

        if (!obj.getHeaders().isEmpty())
        {
            for (Iterator it = obj.getHeaders().iterator(); it.hasNext();)
            {
                PemHeader hdr = (PemHeader)it.next();

                size += hdr.getName().length() + ": ".length() + hdr.getValue().length() + nlLength;
            }

            size += nlLength;
        }

        // base64 encoding
        int dataLen = ((obj.getContent().length + 2) / 3) * 4;
        
        size += dataLen + (((dataLen + LINE_LENGTH - 1) / LINE_LENGTH) * nlLength);

        return size;
    }
    
    public void writeObject(PemObjectGenerator objGen)
        throws IOException
    {
        PemObject obj = objGen.generate();

        writePreEncapsulationBoundary(obj.getType());

        if (!obj.getHeaders().isEmpty())
        {
            for (Iterator it = obj.getHeaders().iterator(); it.hasNext();)
            {
                PemHeader hdr = (PemHeader)it.next();

                this.write(hdr.getName());
                this.write(": ");
                this.write(hdr.getValue());
                this.newLine();
            }

            this.newLine();
        }
        
        writeEncoded(obj.getContent());
        writePostEncapsulationBoundary(obj.getType());
    }

    private void writeEncoded(byte[] bytes)
        throws IOException
    {
        bytes = Base64.encode(bytes);

        for (int i = 0; i < bytes.length; i += buf.length)
        {
            int index = 0;

            while (index != buf.length)
            {
                if ((i + index) >= bytes.length)
                {
                    break;
                }
                buf[index] = (char)bytes[i + index];
                index++;
            }
            this.write(buf, 0, index);
            this.newLine();
        }
    }

    private void writePreEncapsulationBoundary(
        String type)
        throws IOException
    {
        this.write("-----BEGIN " + type + "-----");
        this.newLine();
    }

    private void writePostEncapsulationBoundary(
        String type)
        throws IOException
    {
        this.write("-----END " + type + "-----");
        this.newLine();
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.io.pem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.spongycastle.util.encoders.Base64;

public class PemReader extends BufferedReader {
    private static final String BEGIN = "-----BEGIN ";
    private static final String END = "-----END ";

    public PemReader(Reader reader) {
        super(reader);
    }

    public PemObject readPemObject() throws IOException {
        String line = readLine();

        while (line != null && !line.startsWith(BEGIN)) {
            line = readLine();
        }

        if (line != null) {
            line = line.substring(BEGIN.length());
            int index = line.indexOf('-');
            String type = line.substring(0, index);

            if (index > 0) {
                return loadObject(type);
            }
        }

        return null;
    }

    private PemObject loadObject(String type) throws IOException {
        String line;
        String endMarker = END + type;
        StringBuilder buf = new StringBuilder();
        List headers = new ArrayList();

        while ((line = readLine()) != null) {
            if (line.indexOf(":") >= 0) {
                int index = line.indexOf(':');
                String hdr = line.substring(0, index);
                String value = line.substring(index + 1).trim();

                headers.add(new PemHeader(hdr, value));

                continue;
            }

            if (line.indexOf(endMarker) != -1) {
                break;
            }

            buf.append(line.trim());
        }

        if (line == null) {
            throw new IOException(endMarker + " not found");
        }

        return new PemObject(type, headers, Base64.decode(buf.toString()));
    }

}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.io.pem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("all")
public class PemObject
    implements PemObjectGenerator
{
	private static final List EMPTY_LIST = Collections.unmodifiableList(new ArrayList());

    private String type;
    private List   headers;
    private byte[] content;

    /**
     * Generic constructor for object without headers.
     *
     * @param type pem object type.
     * @param content the binary content of the object.
     */
    public PemObject(String type, byte[] content)
    {
        this(type, EMPTY_LIST, content);
    }

    /**
     * Generic constructor for object with headers.
     *
     * @param type pem object type.
     * @param headers a list of PemHeader objects.
     * @param content the binary content of the object.
     */
    public PemObject(String type, List headers, byte[] content)
    {
        this.type = type;
        this.headers = Collections.unmodifiableList(headers);
        this.content = content;
    }

    public String getType()
    {
        return type;
    }

    public List getHeaders()
    {
        return headers;
    }

    public byte[] getContent()
    {
        return content;
    }

    public PemObject generate()
        throws PemGenerationException
    {
        return this;
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.io.pem;

public interface PemObjectGenerator
{
    PemObject generate()
        throws PemGenerationException;
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.io.pem;

import java.io.IOException;

@SuppressWarnings("serial")
public class PemGenerationException
    extends IOException
{
    private Throwable cause;

    public PemGenerationException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public PemGenerationException(String message)
    {
        super(message);
    }

    public Throwable getCause()
    {
        return cause;
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.io.pem;

public class PemHeader
{
    private String name;
    private String value;

    public PemHeader(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public int hashCode()
    {
        return getHashCode(this.name) + 31 * getHashCode(this.value);    
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof PemHeader))
        {
            return false;
        }

        PemHeader other = (PemHeader)o;

        return other == this || (isEqual(this.name, other.name) && isEqual(this.value, other.value));
    }

    private int getHashCode(String s)
    {
        if (s == null)
        {
            return 1;
        }

        return s.hashCode();
    }

    private boolean isEqual(String s1, String s2)
    {
        if (s1 == s2)
        {
            return true;
        }

        if (s1 == null || s2 == null)
        {
            return false;
        }

        return s1.equals(s2);
    }

}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;

public class Base64Encoder implements Encoder {
    protected final byte[] encodingTable = {(byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z', (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/'};

    protected byte padding = (byte) '=';

    /*
     * set up the decoding table.
     */
    protected final byte[] decodingTable = new byte[128];

    protected void initialiseDecodingTable() {
        for (int i = 0; i < encodingTable.length; i++) {
            decodingTable[encodingTable[i]] = (byte) i;
        }
    }

    public Base64Encoder() {
        initialiseDecodingTable();
    }

    /**
     * encode the input data producing a base 64 output stream.
     *
     * @return the number of bytes produced.
     */
    public int encode(byte[] data, int off, int length, OutputStream out) throws IOException {
        int modulus = length % 3;
        int dataLength = (length - modulus);
        int a1, a2, a3;

        for (int i = off; i < off + dataLength; i += 3) {
            a1 = data[i] & 0xff;
            a2 = data[i + 1] & 0xff;
            a3 = data[i + 2] & 0xff;

            out.write(encodingTable[(a1 >>> 2) & 0x3f]);
            out.write(encodingTable[((a1 << 4) | (a2 >>> 4)) & 0x3f]);
            out.write(encodingTable[((a2 << 2) | (a3 >>> 6)) & 0x3f]);
            out.write(encodingTable[a3 & 0x3f]);
        }

        /*
         * process the tail end.
         */
        int b1, b2, b3;
        int d1, d2;

        switch (modulus) {
            case 0:        /* nothing left to do */
                break;
            case 1:
                d1 = data[off + dataLength] & 0xff;
                b1 = (d1 >>> 2) & 0x3f;
                b2 = (d1 << 4) & 0x3f;

                out.write(encodingTable[b1]);
                out.write(encodingTable[b2]);
                out.write(padding);
                out.write(padding);
                break;
            case 2:
                d1 = data[off + dataLength] & 0xff;
                d2 = data[off + dataLength + 1] & 0xff;

                b1 = (d1 >>> 2) & 0x3f;
                b2 = ((d1 << 4) | (d2 >>> 4)) & 0x3f;
                b3 = (d2 << 2) & 0x3f;

                out.write(encodingTable[b1]);
                out.write(encodingTable[b2]);
                out.write(encodingTable[b3]);
                out.write(padding);
                break;
        }

        return (dataLength / 3) * 4 + ((modulus == 0) ? 0 : 4);
    }

    private boolean ignore(char c) {
        return (c == '\n' || c == '\r' || c == '\t' || c == ' ');
    }

    /**
     * decode the base 64 encoded byte data writing it to the given output stream,
     * whitespace characters will be ignored.
     *
     * @return the number of bytes produced.
     */
    public int decode(byte[] data, int off, int length, OutputStream out) throws IOException {
        byte b1, b2, b3, b4;
        int outLen = 0;

        int end = off + length;

        while (end > off) {
            if (!ignore((char) data[end - 1])) {
                break;
            }

            end--;
        }

        int i = off;
        int finish = end - 4;

        i = nextI(data, i, finish);

        while (i < finish) {
            b1 = decodingTable[data[i++]];

            i = nextI(data, i, finish);

            b2 = decodingTable[data[i++]];

            i = nextI(data, i, finish);

            b3 = decodingTable[data[i++]];

            i = nextI(data, i, finish);

            b4 = decodingTable[data[i++]];

            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));
            out.write((b3 << 6) | b4);

            outLen += 3;

            i = nextI(data, i, finish);
        }

        outLen += decodeLastBlock(out, (char) data[end - 4], (char) data[end - 3], (char) data[end - 2], (char) data[end - 1]);

        return outLen;
    }

    private int nextI(byte[] data, int i, int finish) {
        while ((i < finish) && ignore((char) data[i])) {
            i++;
        }
        return i;
    }

    /**
     * decode the base 64 encoded String data writing it to the given output stream,
     * whitespace characters will be ignored.
     *
     * @return the number of bytes produced.
     */
    public int decode(String data, OutputStream out) throws IOException {
        byte b1, b2, b3, b4;
        int length = 0;

        int end = data.length();

        while (end > 0) {
            if (!ignore(data.charAt(end - 1))) {
                break;
            }

            end--;
        }

        int i = 0;
        int finish = end - 4;

        i = nextI(data, i, finish);

        while (i < finish) {
            b1 = decodingTable[data.charAt(i++)];

            i = nextI(data, i, finish);

            b2 = decodingTable[data.charAt(i++)];

            i = nextI(data, i, finish);

            b3 = decodingTable[data.charAt(i++)];

            i = nextI(data, i, finish);

            b4 = decodingTable[data.charAt(i++)];

            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));
            out.write((b3 << 6) | b4);

            length += 3;

            i = nextI(data, i, finish);
        }

        length += decodeLastBlock(out, data.charAt(end - 4), data.charAt(end - 3), data.charAt(end - 2), data.charAt(end - 1));

        return length;
    }

    private int decodeLastBlock(OutputStream out, char c1, char c2, char c3, char c4) throws IOException {
        byte b1, b2, b3, b4;

        if (c3 == padding) {
            b1 = decodingTable[c1];
            b2 = decodingTable[c2];

            out.write((b1 << 2) | (b2 >> 4));

            return 1;
        } else if (c4 == padding) {
            b1 = decodingTable[c1];
            b2 = decodingTable[c2];
            b3 = decodingTable[c3];

            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));

            return 2;
        } else {
            b1 = decodingTable[c1];
            b2 = decodingTable[c2];
            b3 = decodingTable[c3];
            b4 = decodingTable[c4];

            out.write((b1 << 2) | (b2 >> 4));
            out.write((b2 << 4) | (b3 >> 2));
            out.write((b3 << 6) | b4);

            return 3;
        }
    }

    private int nextI(String data, int i, int finish) {
        while ((i < finish) && ignore(data.charAt(i))) {
            i++;
        }
        return i;
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.encoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Base64 {
    private static final Encoder encoder = new Base64Encoder();

    /**
     * encode the input data producing a base 64 encoded byte array.
     *
     * @return a byte array containing the base 64 encoded data.
     */
    public static byte[] encode(byte[] data) {
        int len = (data.length + 2) / 3 * 4;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

        try {
            encoder.encode(data, 0, data.length, bOut);
        } catch (IOException e) {
            throw new RuntimeException("exception encoding base64 string: " + e);
        }

        return bOut.toByteArray();
    }

    /**
     * Encode the byte data to base 64 writing it to the given output stream.
     *
     * @return the number of bytes produced.
     */
    public static int encode(byte[] data, OutputStream out) throws IOException {
        return encoder.encode(data, 0, data.length, out);
    }

    /**
     * Encode the byte data to base 64 writing it to the given output stream.
     *
     * @return the number of bytes produced.
     */
    public static int encode(byte[] data, int off, int length, OutputStream out) throws IOException {
        return encoder.encode(data, off, length, out);
    }

    /**
     * decode the base 64 encoded input data. It is assumed the input data is valid.
     *
     * @return a byte array representing the decoded data.
     */
    public static byte[] decode(byte[] data) {
        int len = data.length / 4 * 3;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

        try {
            encoder.decode(data, 0, data.length, bOut);
        } catch (IOException e) {
            throw new RuntimeException("exception decoding base64 string: " + e);
        }

        return bOut.toByteArray();
    }

    /**
     * decode the base 64 encoded String data - whitespace will be ignored.
     *
     * @return a byte array representing the decoded data.
     */
    public static byte[] decode(String data) {
        int len = data.length() / 4 * 3;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream(len);

        try {
            encoder.decode(data, bOut);
        } catch (IOException e) {
            throw new RuntimeException("exception decoding base64 string: " + e);
        }

        return bOut.toByteArray();
    }

    /**
     * decode the base 64 encoded String data writing it to the given output stream,
     * whitespace characters will be ignored.
     *
     * @return the number of bytes produced.
     */
    public static int decode(String data, OutputStream out) throws IOException {
        return encoder.decode(data, out);
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package org.spongycastle.util.encoders;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Encode and decode byte arrays (typically from binary to 7-bit ASCII 
 * encodings).
 */
public interface Encoder
{
    int encode(byte[] data, int off, int length, OutputStream out) throws IOException;
    
    int decode(byte[] data, int off, int length, OutputStream out) throws IOException;

    int decode(String data, OutputStream out) throws IOException;
}


package de.blinkt.openvpn;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.StringReader;

import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;

public class OpenVpnApi {

    private static final String  TAG = "OpenVpnApi";
    public static void startVpn(Context context, String inlineConfig, String sCountry, String userName, String pw) throws RemoteException {
        if (inlineConfig.isEmpty()) throw new RemoteException("config is empty");
        else startVpnInternal(context, inlineConfig, sCountry, userName, pw);
    }

    static void startVpnInternal(Context context, String inlineConfig, String sCountry, String userName, String pw) throws RemoteException {
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(inlineConfig));
            VpnProfile vp = cp.convertProfile();// Analysis.ovpn
            Log.d(TAG, "startVpnInternal: =============="+cp+"\n" + vp);

            vp.mName = sCountry;
            if (vp.checkProfile(context) != de.blinkt.openvpn.R.string.no_error_found){
                throw new RemoteException(context.getString(vp.checkProfile(context)));
            }
            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = userName;
            vp.mPassword = pw;
            ProfileManager.setTemporaryProfile(context, vp);
            VPNLaunchHelper.startOpenVpn(vp, context);
        } catch (IOException | ConfigParser.ConfigParseError e) {
            throw new RemoteException(e.getMessage());
        }
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */
package de.blinkt.openvpn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;


import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;

public class DisconnectVPNActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    protected static OpenVPNService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        showDisconnectDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private void showDisconnectDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_cancel);
        builder.setMessage(R.string.cancel_connection_query);
        builder.setNegativeButton(android.R.string.no, this);
        builder.setPositiveButton(android.R.string.yes, this);
        builder.setOnCancelListener(this);
        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            stopVpn();
        }
        finish();
    }
    public void stopVpn(){
        ProfileManager.setConntectedVpnProfileDisconnected(this);
        if (mService != null && mService.getManagement() != null) {
            mService.getManagement().stopVPN(false);
        }
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.io.IOException;

import de.blinkt.openvpn.api.ExternalAppDatabase;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IServiceStatus;
import de.blinkt.openvpn.core.OpenVPNStatusService;
import de.blinkt.openvpn.core.PasswordCache;
import de.blinkt.openvpn.core.Preferences;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * This Activity actually handles two stages of a launcher shortcut's life cycle.
 * <p/>
 * 1. Your application offers to provide shortcuts to the launcher.  When
 * the user installs a shortcut, an activity within your application
 * generates the actual shortcut and returns it to the launcher, where it
 * is shown to the user as an icon.
 * <p/>
 * 2. Any time the user clicks on an installed shortcut, an intent is sent.
 * Typically this would then be handled as necessary by an activity within
 * your application.
 * <p/>
 * We handle stage 1 (creating a shortcut) by simply sending back the information (in the form
 * of an {@link android.content.Intent} that the launcher will use to create the shortcut.
 * <p/>
 * You can also implement this in an interactive way, by having your activity actually present
 * UI for the user to select the specific nature of the shortcut, such as a contact, picture, URL,
 * media item, or action.
 * <p/>
 * We handle stage 2 (responding to a shortcut) in this sample by simply displaying the contents
 * of the incoming {@link android.content.Intent}.
 * <p/>
 * In a real application, you would probably use the shortcut intent to display specific content
 * or start a particular operation.
 */
public class LaunchVPN extends Activity {

    public static final String EXTRA_KEY = "de.blinkt.openvpn.shortcutProfileUUID";
    public static final String EXTRA_NAME = "de.blinkt.openvpn.shortcutProfileName";
    public static final String EXTRA_HIDELOG = "de.blinkt.openvpn.showNoLogWindow";
    public static final String CLEARLOG = "clearlogconnect";


    private static final int START_VPN_PROFILE = 70;


    private VpnProfile mSelectedProfile;
    private boolean mhideLog = false;

    private boolean mCmfixed = false;
    private String mTransientAuthPW;
    private String mTransientCertOrPCKS12PW;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.launchvpn);
        startVpnFromIntent();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            IServiceStatus service = IServiceStatus.Stub.asInterface(binder);
            try {
                if (mTransientAuthPW != null)

                    service.setCachedPassword(mSelectedProfile.getUUIDString(), PasswordCache.AUTHPASSWORD, mTransientAuthPW);
                if (mTransientCertOrPCKS12PW != null)
                    service.setCachedPassword(mSelectedProfile.getUUIDString(), PasswordCache.PCKS12ORCERTPASSWORD, mTransientCertOrPCKS12PW);

                onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            unbindService(this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    protected void startVpnFromIntent() {
        // Resolve the intent

        final Intent intent = getIntent();
        final String action = intent.getAction();

        // If the intent is a request to create a shortcut, we'll do that and exit


        if (Intent.ACTION_MAIN.equals(action)) {
            // Check if we need to clear the log
            if (Preferences.getDefaultSharedPreferences(this).getBoolean(CLEARLOG, true))
                VpnStatus.clearLog();

            // we got called to be the starting point, most likely a shortcut
            String shortcutUUID = intent.getStringExtra(EXTRA_KEY);
            String shortcutName = intent.getStringExtra(EXTRA_NAME);
            mhideLog = intent.getBooleanExtra(EXTRA_HIDELOG, false);

            VpnProfile profileToConnect = ProfileManager.get(this, shortcutUUID);
            if (shortcutName != null && profileToConnect == null) {
                profileToConnect = ProfileManager.getInstance(this).getProfileByName(shortcutName);
                if (!(new ExternalAppDatabase(this).checkRemoteActionPermission(this, getCallingPackage()))) {
                    finish();
                    return;
                }
            }


            if (profileToConnect == null) {
                VpnStatus.logError(R.string.shortcut_profile_notfound);
                // show Log window to display error
                showLogWindow();
                finish();
            } else {
                mSelectedProfile = profileToConnect;
                launchVPN();
            }
        }
    }

    private void askForPW(final int type) {

        final EditText entry = new EditText(this);

        entry.setSingleLine();
        entry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        entry.setTransformationMethod(new PasswordTransformationMethod());

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.pw_request_dialog_title, getString(type)));
        dialog.setMessage(getString(R.string.pw_request_dialog_prompt, mSelectedProfile.mName));


        @SuppressLint("InflateParams") final View userpwlayout = getLayoutInflater().inflate(R.layout.userpass, null, false);

        if (type == R.string.password) {
            ((EditText) userpwlayout.findViewById(R.id.username)).setText(mSelectedProfile.mUsername);
            ((EditText) userpwlayout.findViewById(R.id.password)).setText(mSelectedProfile.mPassword);
            ((CheckBox) userpwlayout.findViewById(R.id.save_password)).setChecked(!TextUtils.isEmpty(mSelectedProfile.mPassword));
            ((CheckBox) userpwlayout.findViewById(R.id.show_password)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        ((EditText) userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    else
                        ((EditText) userpwlayout.findViewById(R.id.password)).setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            });

            dialog.setView(userpwlayout);
        } else {
            dialog.setView(entry);
        }

        AlertDialog.Builder builder = dialog.setPositiveButton(android.R.string.ok,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (type == R.string.password) {
                            mSelectedProfile.mUsername = ((EditText) userpwlayout.findViewById(R.id.username)).getText().toString();

                            String pw = ((EditText) userpwlayout.findViewById(R.id.password)).getText().toString();
                            if (((CheckBox) userpwlayout.findViewById(R.id.save_password)).isChecked()) {
                                mSelectedProfile.mPassword = pw;
                            } else {
                                mSelectedProfile.mPassword = null;
                                mTransientAuthPW = pw;
                            }
                        } else {
                            mTransientCertOrPCKS12PW = entry.getText().toString();
                        }
                        Intent intent = new Intent(LaunchVPN.this, OpenVPNStatusService.class);
                        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    }

                });
        dialog.setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VpnStatus.updateStateString("USER_VPN_PASSWORD_CANCELLED", "", R.string.state_user_vpn_password_cancelled,
                                ConnectionStatus.LEVEL_NOTCONNECTED);
                        finish();
                    }
                });

        dialog.create().show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_VPN_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                int needpw = mSelectedProfile.needUserPWInput(mTransientCertOrPCKS12PW, mTransientAuthPW);
                if (needpw != 0) {
                    VpnStatus.updateStateString("USER_VPN_PASSWORD", "", R.string.state_user_vpn_password,
                            ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
                    askForPW(needpw);
                } else {
                    SharedPreferences prefs = Preferences.getDefaultSharedPreferences(this);
                    boolean showLogWindow = prefs.getBoolean("showlogwindow", true);

                    if (!mhideLog && showLogWindow)
                        showLogWindow();
                    ProfileManager.updateLRU(this, mSelectedProfile);
                    VPNLaunchHelper.startOpenVpn(mSelectedProfile, getBaseContext());
                    finish();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User does not want us to start, so we just vanish
                VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
                        ConnectionStatus.LEVEL_NOTCONNECTED);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    VpnStatus.logError(R.string.nought_alwayson_warning);

                finish();
            }
        }
    }

    void showLogWindow() {

        Intent startLW = new Intent();
        startLW.setComponent(new ComponentName(this, getPackageName() + ".activities.LogWindow"));
        startLW.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(startLW);

    }

    void showConfigErrorDialog(int vpnok) {
        AlertDialog.Builder d = new AlertDialog.Builder(this);
        d.setTitle(R.string.config_error_found);
        d.setMessage(vpnok);
        d.setPositiveButton(android.R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();

            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            setOnDismissListener(d);
        d.show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void setOnDismissListener(AlertDialog.Builder d) {
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }

    void launchVPN() {
        int vpnok = mSelectedProfile.checkProfile(this);
        if (vpnok != R.string.no_error_found) {
            showConfigErrorDialog(vpnok);
            return;
        }

        Intent intent = VpnService.prepare(this);
        // Check if we want to fix /dev/tun
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(this);
        boolean usecm9fix = prefs.getBoolean("useCM9Fix", false);
        boolean loadTunModule = prefs.getBoolean("loadTunModule", false);

        if (loadTunModule)
            execeuteSUcmd("insmod /system/lib/modules/tun.ko");

        if (usecm9fix && !mCmfixed) {
            execeuteSUcmd("chown system /dev/tun");
        }

        if (intent != null) {
            VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                    ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
            // Start the query
            try {
                startActivityForResult(intent, START_VPN_PROFILE);
            } catch (ActivityNotFoundException ane) {
                // Shame on you Sony! At least one user reported that
                // an official Sony Xperia Arc S image triggers this exception
                VpnStatus.logError(R.string.no_vpn_support_image);
                showLogWindow();
            }
        } else {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        }

    }

    private void execeuteSUcmd(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("su", "-c", command);
            Process p = pb.start();
            int ret = p.waitFor();
            if (ret == 0)
                mCmfixed = true;
        } catch (InterruptedException | IOException e) {
            VpnStatus.logException("SU command", e);
        }
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.ContentProvider;
import android.content.ContentProvider.PipeDataWriter;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * A very simple content provider that can serve arbitrary asset files from
 * our .apk.
 */
public class FileProvider extends ContentProvider
implements PipeDataWriter<InputStream> {
	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		try {
			File dumpfile = getFileFromURI(uri);


			MatrixCursor c = new MatrixCursor(projection);

			Object[] row = new Object[projection.length];
			int i=0;
			for (String r:projection) {
				if(r.equals(OpenableColumns.SIZE))
					row[i] = dumpfile.length();
				if(r.equals(OpenableColumns.DISPLAY_NAME))
					row[i] = dumpfile.getName();
				i++;
			}
			c.addRow(row);
			return c;
		} catch (FileNotFoundException e) {
            VpnStatus.logException(e);
            return null;
		}


	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Don't support inserts.
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// Don't support deletes.
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// Don't support updates.
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// For this sample, assume all files are .apks.
		return "application/octet-stream";
	}

	@Override
	public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
		File dumpfile = getFileFromURI(uri);

		try {

			InputStream is = new FileInputStream(dumpfile);
			// Start a new thread that pipes the stream data back to the caller.
			return new AssetFileDescriptor(
					openPipeHelper(uri, null, null, is, this), 0,
					dumpfile.length());
		} catch (IOException e) {
            throw new FileNotFoundException("Unable to open minidump " + uri);
		}
	}

	private File getFileFromURI(Uri uri) throws FileNotFoundException {
		// Try to open an asset with the given name.
		String path = uri.getPath();
		if(path.startsWith("/"))
			path = path.replaceFirst("/", "");       

		// I think this already random enough, no need for magic secure cookies
		// 1f9563a4-a1f5-2165-255f2219-111823ef.dmp
		if (!path.matches("^[0-9a-z-.]*(dmp|dmp.log)$"))
			throw new FileNotFoundException("url not in expect format " + uri);
		File cachedir = getContext().getCacheDir();
        return new File(cachedir,path);
	}

	@Override
	public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType,
			Bundle opts, InputStream args) {
		// Transfer data from the asset to the pipe the client is reading.
		byte[] buffer = new byte[8192];
		int n;
		FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
		try {
			while ((n=args.read(buffer)) >= 0) {
				fout.write(buffer, 0, n);
			}
		} catch (IOException e) {
			Log.i("OpenVPNFileProvider", "Failed transferring", e);
		} finally {
			try {
				args.close();
			} catch (IOException e) {
			}
			try {
				fout.close();
			} catch (IOException e) {
			}
		}
	}
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import de.blinkt.openvpn.core.Preferences;
import de.blinkt.openvpn.core.ProfileManager;


public class OnBootReceiver extends BroadcastReceiver {



	// Debug: am broadcast -a android.intent.action.BOOT_COMPLETED
	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();
		SharedPreferences prefs = Preferences.getDefaultSharedPreferences(context);

		boolean useStartOnBoot = prefs.getBoolean("restartvpnonboot", false);
		if (!useStartOnBoot)
			return;

		if(Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
			VpnProfile bootProfile = ProfileManager.getAlwaysOnVPN(context);
			if(bootProfile != null) {
				launchVPN(bootProfile, context);
			}		
		}
	}

	void launchVPN(VpnProfile profile, Context context) {
		Intent startVpnIntent = new Intent(Intent.ACTION_MAIN);
		startVpnIntent.setClass(context, LaunchVPN.class);
		startVpnIntent.putExtra(LaunchVPN.EXTRA_KEY,profile.getUUIDString());
		startVpnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startVpnIntent.putExtra(LaunchVPN.EXTRA_HIDELOG, true);

		context.startActivity(startVpnIntent);
	}
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.KeyChain;
import android.security.KeyChainException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import de.blinkt.openvpn.core.*;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class VpnProfile implements Serializable, Cloneable {
    // Note that this class cannot be moved to core where it belongs since
    // the profile loading depends on it being here
    // The Serializable documentation mentions that class name change are possible
    // but the how is unclear
    //
    transient public static final long MAX_EMBED_FILE_SIZE = 2048 * 1024; // 2048kB
    // Don't change this, not all parts of the program use this constant
    public static final String EXTRA_PROFILEUUID = "de.blinkt.openvpn.profileUUID";
    public static final String INLINE_TAG = "[[INLINE]]";
    public static final String DISPLAYNAME_TAG = "[[NAME]]";
    public static final int MAXLOGLEVEL = 4;
    public static final int CURRENT_PROFILE_VERSION = 8;
    public static final int DEFAULT_MSSFIX_SIZE = 1280;
    public static final int TYPE_CERTIFICATES = 0;
    public static final int TYPE_PKCS12 = 1;
    public static final int TYPE_KEYSTORE = 2;
    public static final int TYPE_USERPASS = 3;
    public static final int TYPE_STATICKEYS = 4;
    public static final int TYPE_USERPASS_CERTIFICATES = 5;
    public static final int TYPE_USERPASS_PKCS12 = 6;
    public static final int TYPE_USERPASS_KEYSTORE = 7;
    public static final int TYPE_EXTERNAL_APP = 8;
    public static final int X509_VERIFY_TLSREMOTE = 0;
    public static final int X509_VERIFY_TLSREMOTE_COMPAT_NOREMAPPING = 1;
    public static final int X509_VERIFY_TLSREMOTE_DN = 2;
    public static final int X509_VERIFY_TLSREMOTE_RDN = 3;
    public static final int X509_VERIFY_TLSREMOTE_RDN_PREFIX = 4;
    public static final int AUTH_RETRY_NONE_FORGET = 0;
    public static final int AUTH_RETRY_NOINTERACT = 2;
    public static final boolean mIsOpenVPN22 = false;
    private static final long serialVersionUID = 7085688938959334563L;
    private static final int AUTH_RETRY_NONE_KEEP = 1;
    private static final int AUTH_RETRY_INTERACT = 3;
    public static String DEFAULT_DNS1 = "8.8.8.8";
    public static String DEFAULT_DNS2 = "8.8.4.4";
    // variable named wrong and should haven beeen transient
    // but needs to keep wrong name to guarante loading of old
    // profiles
    public transient boolean profileDeleted = false;
    public int mAuthenticationType = TYPE_KEYSTORE;
    public String mName;
    public String mAlias;
    public String mClientCertFilename;
    public String mTLSAuthDirection = "";
    public String mTLSAuthFilename;
    public String mClientKeyFilename;
    public String mCaFilename;
    public boolean mUseLzo = true;
    public String mPKCS12Filename;
    public String mPKCS12Password;
    public boolean mUseTLSAuth = false;
    public String mDNS1 = DEFAULT_DNS1;
    public String mDNS2 = DEFAULT_DNS2;
    public String mIPv4Address;
    public String mIPv6Address;
    public boolean mOverrideDNS = false;
    public String mSearchDomain = "blinkt.de";
    public boolean mUseDefaultRoute = true;
    public boolean mUsePull = true;
    public String mCustomRoutes;
    public boolean mCheckRemoteCN = true;
    public boolean mExpectTLSCert = false;
    public String mRemoteCN = "";
    public String mPassword = "";
    public String mUsername = "";
    public boolean mRoutenopull = false;
    public boolean mUseRandomHostname = false;
    public boolean mUseFloat = false;
    public boolean mUseCustomConfig = false;
    public String mCustomConfigOptions = "";
    public String mVerb = "1";  //ignored
    public String mCipher = "";
    public boolean mNobind = true;
    public boolean mUseDefaultRoutev6 = true;
    public String mCustomRoutesv6 = "";
    public String mKeyPassword = "";
    public boolean mPersistTun = false;
    public String mConnectRetryMax = "-1";
    public String mConnectRetry = "2";
    public String mConnectRetryMaxTime = "300";
    public boolean mUserEditable = true;
    public String mAuth = "";
    public int mX509AuthType = X509_VERIFY_TLSREMOTE_RDN;
    public String mx509UsernameField = null;
    public boolean mAllowLocalLAN;
    public String mExcludedRoutes;
    public String mExcludedRoutesv6;
    public int mMssFix = 0; // -1 is default,
    public Connection[] mConnections = new Connection[0];
    public boolean mRemoteRandom = false;
    public HashSet<String> mAllowedAppsVpn = new HashSet<>();
    public boolean mAllowedAppsVpnAreDisallowed = true;
    public boolean mAllowAppVpnBypass = false;
    public String mCrlFilename;
    public String mProfileCreator;
    public String mExternalAuthenticator;
    public int mAuthRetry = AUTH_RETRY_NONE_FORGET;
    public int mTunMtu;
    public boolean mPushPeerInfo = false;
    public int mVersion = 0;
    // timestamp when the profile was last used
    public long mLastUsed;
    public String importedProfileHash;
    /* Options no longer used in new profiles */
    public String mServerName = "openvpn.example.com";
    public String mServerPort = "1194";
    public boolean mUseUdp = true;
    public boolean mTemporaryProfile = false;
    private transient PrivateKey mPrivateKey;
    // Public attributes, since I got mad with getter/setter
    // set members to default values
    private UUID mUuid;
    private int mProfileVersion;

    public boolean mBlockUnusedAddressFamilies =true;

    public VpnProfile(String name) {
        mUuid = UUID.randomUUID();
        mName = name;
        mProfileVersion = CURRENT_PROFILE_VERSION;

        mConnections = new Connection[1];
        mConnections[0] = new Connection();
        mLastUsed = System.currentTimeMillis();
    }

    public static String openVpnEscape(String unescaped) {
        if (unescaped == null)
            return null;
        String escapedString = unescaped.replace("\\", "\\\\");
        escapedString = escapedString.replace("\"", "\\\"");
        escapedString = escapedString.replace("\n", "\\n");

        if (escapedString.equals(unescaped) && !escapedString.contains(" ") &&
                !escapedString.contains("#") && !escapedString.contains(";")
                && !escapedString.equals(""))
            return unescaped;
        else
            return '"' + escapedString + '"';
    }

    public static boolean doUseOpenVPN3(Context c) {
        // Nerver use OpenVPN3
        return false;
    }

    //! Put inline data inline and other data as normal escaped filename
    public static String insertFileData(String cfgentry, String filedata) {
        if (filedata == null) {
            return String.format("%s %s\n", cfgentry, "file missing in config profile");
        } else if (isEmbedded(filedata)) {
            String dataWithOutHeader = getEmbeddedContent(filedata);
            return String.format(Locale.ENGLISH, "<%s>\n%s\n</%s>\n", cfgentry, dataWithOutHeader, cfgentry);
        } else {
            return String.format(Locale.ENGLISH, "%s %s\n", cfgentry, openVpnEscape(filedata));
        }
    }

    public static String getDisplayName(String embeddedFile) {
        int start = DISPLAYNAME_TAG.length();
        int end = embeddedFile.indexOf(INLINE_TAG);
        return embeddedFile.substring(start, end);
    }

    public static String getEmbeddedContent(String data) {
        if (!data.contains(INLINE_TAG))
            return data;

        int start = data.indexOf(INLINE_TAG) + INLINE_TAG.length();
        return data.substring(start);
    }

    public static boolean isEmbedded(String data) {
        if (data == null)
            return false;
        if (data.startsWith(INLINE_TAG) || data.startsWith(DISPLAYNAME_TAG))
            return true;
        else
            return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VpnProfile) {
            VpnProfile vpnProfile = (VpnProfile) obj;
            return mUuid.equals(vpnProfile.mUuid);
        } else {
            return false;
        }
    }

    public void clearDefaults() {
        mServerName = "unknown";
        mUsePull = false;
        mUseLzo = false;
        mUseDefaultRoute = false;
        mUseDefaultRoutev6 = false;
        mExpectTLSCert = false;
        mCheckRemoteCN = false;
        mPersistTun = false;
        mAllowLocalLAN = true;
        mPushPeerInfo = false;
        mMssFix = 0;
        mNobind = false;
    }

    public UUID getUUID() {
        return mUuid;

    }

    // Only used for the special case of managed profiles
    public void setUUID(UUID uuid) {
        mUuid = uuid;
    }

    public String getName() {
        if (TextUtils.isEmpty(mName))
            return "No profile name";
        return mName;
    }

    public void upgradeProfile() {

        /* Fallthrough is intended here */
        switch(mProfileVersion) {
            case 0:
            case 1:
                /* default to the behaviour the OS used */
                mAllowLocalLAN = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
            case 2:
            case 3:
                moveOptionsToConnection();
                mAllowedAppsVpnAreDisallowed = true;

                if (mAllowedAppsVpn == null)
                    mAllowedAppsVpn = new HashSet<>();

                if (mConnections == null)
                    mConnections = new Connection[0];
            case 4:
            case 5:

                if (TextUtils.isEmpty(mProfileCreator))
                    mUserEditable = true;
            case 6:
                for (Connection c : mConnections)
                    if (c.mProxyType == null)
                        c.mProxyType = Connection.ProxyType.NONE;
            case 7:
                if (mAllowAppVpnBypass)
                    mBlockUnusedAddressFamilies = !mAllowAppVpnBypass;
            default:
        }

        mProfileVersion = CURRENT_PROFILE_VERSION;

    }

    private void moveOptionsToConnection() {
        mConnections = new Connection[1];
        Connection conn = new Connection();

        conn.mServerName = mServerName;
        conn.mServerPort = mServerPort;
        conn.mUseUdp = mUseUdp;
        conn.mCustomConfiguration = "";

        mConnections[0] = conn;

    }

    public String getConfigFile(Context context, boolean configForOvpn3) {

        File cacheDir = context.getCacheDir();
        StringBuilder cfg = new StringBuilder();

        if (!configForOvpn3) {
            // Enable management interface
            cfg.append("# Config for OpenVPN 2.x\n");
            cfg.append("# Enables connection to GUI\n");
            cfg.append("management ");

            cfg.append(cacheDir.getAbsolutePath()).append("/").append("mgmtsocket");
            cfg.append(" unix\n");
            cfg.append("management-client\n");
            // Not needed, see updated man page in 2.3
            //cfg += "management-signal\n";
            cfg.append("management-query-passwords\n");
            cfg.append("management-hold\n\n");

            cfg.append(String.format("setenv IV_GUI_VER %s \n", openVpnEscape(getVersionEnvString(context))));
            cfg.append("setenv IV_SSO openurl,crtext\n");
            String versionString = getPlatformVersionEnvString();
            cfg.append(String.format("setenv IV_PLAT_VER %s\n", openVpnEscape(versionString)));
        } else {
            cfg.append("# Config for OpenVPN 3 C++\n");
        }


        if (!configForOvpn3) {
            cfg.append("machine-readable-output\n");
            if (!mIsOpenVPN22)
                cfg.append("allow-recursive-routing\n");

            // Users are confused by warnings that are misleading...
            cfg.append("ifconfig-nowarn\n");
        }

        boolean useTLSClient = (mAuthenticationType != TYPE_STATICKEYS);

        if (useTLSClient && mUsePull)
            cfg.append("client\n");
        else if (mUsePull)
            cfg.append("pull\n");
        else if (useTLSClient)
            cfg.append("tls-client\n");


        //cfg += "verb " + mVerb + "\n";
        cfg.append("verb " + MAXLOGLEVEL + "\n");

        if (mConnectRetryMax == null) {
            mConnectRetryMax = "-1";
        }

        if (!mConnectRetryMax.equals("-1"))
            cfg.append("connect-retry-max ").append(mConnectRetryMax).append("\n");

        if (TextUtils.isEmpty(mConnectRetry))
            mConnectRetry = "2";

        if (TextUtils.isEmpty(mConnectRetryMaxTime))
            mConnectRetryMaxTime = "300";


        if (!mIsOpenVPN22)
            cfg.append("connect-retry ").append(mConnectRetry).append(" ").append(mConnectRetryMaxTime).append("\n");
        else if (mIsOpenVPN22 && !mUseUdp)
            cfg.append("connect-retry ").append(mConnectRetry).append("\n");


        cfg.append("resolv-retry 60\n");


        // We cannot use anything else than tun
        cfg.append("dev tun\n");


        boolean canUsePlainRemotes = true;

        if (mConnections.length == 1) {
            cfg.append(mConnections[0].getConnectionBlock(configForOvpn3));
        } else {
            for (Connection conn : mConnections) {
                canUsePlainRemotes = canUsePlainRemotes && conn.isOnlyRemote();
            }

            if (mRemoteRandom)
                cfg.append("remote-random\n");

            if (canUsePlainRemotes) {
                for (Connection conn : mConnections) {
                    if (conn.mEnabled) {
                        cfg.append(conn.getConnectionBlock(configForOvpn3));
                    }
                }
            }
        }


        switch (mAuthenticationType) {
            case VpnProfile.TYPE_USERPASS_CERTIFICATES:
                cfg.append("auth-user-pass\n");
            case VpnProfile.TYPE_CERTIFICATES:
                // Ca
                cfg.append(insertFileData("ca", mCaFilename));

                // Client Cert + Key
                cfg.append(insertFileData("key", mClientKeyFilename));
                cfg.append(insertFileData("cert", mClientCertFilename));

                break;
            case VpnProfile.TYPE_USERPASS_PKCS12:
                cfg.append("auth-user-pass\n");
            case VpnProfile.TYPE_PKCS12:
                cfg.append(insertFileData("pkcs12", mPKCS12Filename));

                if (!TextUtils.isEmpty(mCaFilename))
                {
                    cfg.append(insertFileData("ca", mCaFilename));
                }
                break;

            case VpnProfile.TYPE_USERPASS_KEYSTORE:
                cfg.append("auth-user-pass\n");
            case VpnProfile.TYPE_KEYSTORE:
            case VpnProfile.TYPE_EXTERNAL_APP:
                if (!configForOvpn3) {
                    String[] ks = getExternalCertificates(context);
                    cfg.append("### From Keystore/ext auth app ####\n");
                    if (ks != null) {
                        cfg.append("<ca>\n").append(ks[0]).append("\n</ca>\n");
                        if (!TextUtils.isEmpty(ks[1]))
                            cfg.append("<extra-certs>\n").append(ks[1]).append("\n</extra-certs>\n");
                        cfg.append("<cert>\n").append(ks[2]).append("\n</cert>\n");
                        cfg.append("management-external-key nopadding\n");
                    } else {
                        cfg.append(context.getString(R.string.keychain_access)).append("\n");
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
                            if (!mAlias.matches("^[a-zA-Z0-9]$"))
                                cfg.append(context.getString(R.string.jelly_keystore_alphanumeric_bug)).append("\n");
                    }
                }
                break;
            case VpnProfile.TYPE_USERPASS:
                cfg.append("auth-user-pass\n");
                cfg.append(insertFileData("ca", mCaFilename));
                if (configForOvpn3) {
                    // OpenVPN 3 needs to be told that a client certificate is not required
                    cfg.append("client-cert-not-required\n");
                }
        }

        if (isUserPWAuth()) {
            if (mAuthRetry == AUTH_RETRY_NOINTERACT)
                cfg.append("auth-retry nointeract\n");
        }

        if (!TextUtils.isEmpty(mCrlFilename))
            cfg.append(insertFileData("crl-verify", mCrlFilename));

        if (mUseLzo) {
            cfg.append("comp-lzo\n");
        }

        if (mUseTLSAuth) {
            boolean useTlsCrypt = mTLSAuthDirection.equals("tls-crypt");
            boolean useTlsCrypt2 = mTLSAuthDirection.equals("tls-crypt-v2");

            if (mAuthenticationType == TYPE_STATICKEYS)
                cfg.append(insertFileData("secret", mTLSAuthFilename));
            else if (useTlsCrypt)
                cfg.append(insertFileData("tls-crypt", mTLSAuthFilename));
            else if (useTlsCrypt2)
                cfg.append(insertFileData("tls-crypt-v2", mTLSAuthFilename));
            else
                cfg.append(insertFileData("tls-auth", mTLSAuthFilename));

            if (!TextUtils.isEmpty(mTLSAuthDirection) && !useTlsCrypt && !useTlsCrypt2) {
                cfg.append("key-direction ");
                cfg.append(mTLSAuthDirection);
                cfg.append("\n");
            }

        }

        if (!mUsePull) {
            if (!TextUtils.isEmpty(mIPv4Address))
                cfg.append("ifconfig ").append(cidrToIPAndNetmask(mIPv4Address)).append("\n");

            if (!TextUtils.isEmpty(mIPv6Address)) {
                // Use our own ip as gateway since we ignore it anyway
                String fakegw = mIPv6Address.split("/", 2)[0];
                cfg.append("ifconfig-ipv6 ").append(mIPv6Address).append(" ").append(fakegw).append("\n");
            }

        }

        if (mUsePull && mRoutenopull)
            cfg.append("route-nopull\n");

        String routes = "";

        if (mUseDefaultRoute)
            routes += "route 0.0.0.0 0.0.0.0 vpn_gateway\n";
        else {
            for (String route : getCustomRoutes(mCustomRoutes)) {
                routes += "route " + route + " vpn_gateway\n";
            }

            for (String route : getCustomRoutes(mExcludedRoutes)) {
                routes += "route " + route + " net_gateway\n";
            }
        }


        if (mUseDefaultRoutev6)
            cfg.append("route-ipv6 ::/0\n");
        else
            for (String route : getCustomRoutesv6(mCustomRoutesv6)) {
                routes += "route-ipv6 " + route + "\n";
            }

        cfg.append(routes);

        if (mOverrideDNS || !mUsePull) {
            if (!TextUtils.isEmpty(mDNS1)) {
                cfg.append("dhcp-option DNS ").append(mDNS1).append("\n");
            }
            if (!TextUtils.isEmpty(mDNS2)) {
                cfg.append("dhcp-option DNS ").append(mDNS2).append("\n");
            }
            if (!TextUtils.isEmpty(mSearchDomain))
                cfg.append("dhcp-option DOMAIN ").append(mSearchDomain).append("\n");

        }

        if (mMssFix != 0) {
            if (mMssFix != 1450) {
                if (configForOvpn3)
                    cfg.append(String.format(Locale.US, "mssfix %d mtu\n", mMssFix));
                else
                    cfg.append(String.format(Locale.US, "mssfix %d\n", mMssFix));
            } else
                cfg.append("mssfix\n");
        }

        if (mTunMtu >= 48 && mTunMtu != 1500) {
            cfg.append(String.format(Locale.US, "tun-mtu %d\n", mTunMtu));
        }

        if (mNobind)
            cfg.append("nobind\n");


        // Authentication
        if (mAuthenticationType != TYPE_STATICKEYS) {
            if (mCheckRemoteCN) {
                if (mRemoteCN == null || mRemoteCN.equals(""))
                    cfg.append("verify-x509-name ").append(openVpnEscape(mConnections[0].mServerName)).append(" name\n");
                else
                    switch (mX509AuthType) {

                        // 2.2 style x509 checks
                        case X509_VERIFY_TLSREMOTE_COMPAT_NOREMAPPING:
                            cfg.append("compat-names no-remapping\n");
                        case X509_VERIFY_TLSREMOTE:
                            cfg.append("tls-remote ").append(openVpnEscape(mRemoteCN)).append("\n");
                            break;

                        case X509_VERIFY_TLSREMOTE_RDN:
                            cfg.append("verify-x509-name ").append(openVpnEscape(mRemoteCN)).append(" name\n");
                            break;

                        case X509_VERIFY_TLSREMOTE_RDN_PREFIX:
                            cfg.append("verify-x509-name ").append(openVpnEscape(mRemoteCN)).append(" name-prefix\n");
                            break;

                        case X509_VERIFY_TLSREMOTE_DN:
                            cfg.append("verify-x509-name ").append(openVpnEscape(mRemoteCN)).append("\n");
                            break;
                    }
                if (!TextUtils.isEmpty(mx509UsernameField))
                    cfg.append("x509-username-field ").append(openVpnEscape(mx509UsernameField)).append("\n");
            }
            if (mExpectTLSCert)
                cfg.append("remote-cert-tls server\n");
        }

        if (!TextUtils.isEmpty(mCipher)) {
            cfg.append("cipher ").append(mCipher).append("\n");
        }

        if (!TextUtils.isEmpty(mAuth)) {
            cfg.append("auth ").append(mAuth).append("\n");
        }

        // Obscure Settings dialog
        if (mUseRandomHostname)
            cfg.append("#my favorite options :)\nremote-random-hostname\n");

        if (mUseFloat)
            cfg.append("float\n");

        if (mPersistTun) {
            cfg.append("persist-tun\n");
            cfg.append("# persist-tun also enables pre resolving to avoid DNS resolve problem\n");
            if (!mIsOpenVPN22)
                cfg.append("preresolve\n");
        }

        if (mPushPeerInfo)
            cfg.append("push-peer-info\n");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean usesystemproxy = prefs.getBoolean("usesystemproxy", true);
        if (usesystemproxy && !mIsOpenVPN22 && !configForOvpn3 && !usesExtraProxyOptions()) {
            cfg.append("# Use system proxy setting\n");
            cfg.append("management-query-proxy\n");
        }


        if (mUseCustomConfig) {
            cfg.append("# Custom configuration options\n");
            cfg.append("# You are on your on own here :)\n");
            cfg.append(mCustomConfigOptions);
            cfg.append("\n");

        }

        if (!canUsePlainRemotes) {
            cfg.append("# Connection Options are at the end to allow global options (and global custom options) to influence connection blocks\n");
            for (Connection conn : mConnections) {
                if (conn.mEnabled) {
                    cfg.append("<connection>\n");
                    cfg.append(conn.getConnectionBlock(configForOvpn3));
                    cfg.append("</connection>\n");
                }
            }
        }


        return cfg.toString();
    }

    public String getPlatformVersionEnvString() {
        return String.format(Locale.US, "%d %s %s %s %s %s", Build.VERSION.SDK_INT, Build.VERSION.RELEASE,
                NativeUtils.getNativeAPI(), Build.BRAND, Build.BOARD, Build.MODEL);
    }

    static public String getVersionEnvString(Context c) {
        String version = "unknown";
        try {
            PackageInfo packageinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = packageinfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            VpnStatus.logException(e);
        }
        return String.format(Locale.US, "%s %s", c.getPackageName(), version);

    }

    @NonNull
    private Collection<String> getCustomRoutes(String routes) {
        Vector<String> cidrRoutes = new Vector<>();
        if (routes == null) {
            // No routes set, return empty vector
            return cidrRoutes;
        }
        for (String route : routes.split("[\n \t]")) {
            if (!route.equals("")) {
                String cidrroute = cidrToIPAndNetmask(route);
                if (cidrroute == null)
                    return cidrRoutes;

                cidrRoutes.add(cidrroute);
            }
        }

        return cidrRoutes;
    }

    private Collection<String> getCustomRoutesv6(String routes) {
        Vector<String> cidrRoutes = new Vector<>();
        if (routes == null) {
            // No routes set, return empty vector
            return cidrRoutes;
        }
        for (String route : routes.split("[\n \t]")) {
            if (!route.equals("")) {
                cidrRoutes.add(route);
            }
        }

        return cidrRoutes;
    }

    private String cidrToIPAndNetmask(String route) {
        String[] parts = route.split("/");

        // No /xx, assume /32 as netmask
        if (parts.length == 1)
            parts = (route + "/32").split("/");

        if (parts.length != 2)
            return null;
        int len;
        try {
            len = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ne) {
            return null;
        }
        if (len < 0 || len > 32)
            return null;


        long nm = 0xffffffffL;
        nm = (nm << (32 - len)) & 0xffffffffL;

        String netmask = String.format(Locale.ENGLISH, "%d.%d.%d.%d", (nm & 0xff000000) >> 24, (nm & 0xff0000) >> 16, (nm & 0xff00) >> 8, nm & 0xff);
        return parts[0] + "  " + netmask;
    }

    public Intent prepareStartService(Context context) {
        Intent intent = getStartServiceIntent(context);

        // TODO: Handle this?!
//        if (mAuthenticationType == VpnProfile.TYPE_KEYSTORE || mAuthenticationType == VpnProfile.TYPE_USERPASS_KEYSTORE) {
//            if (getKeyStoreCertificates(context) == null)
//                return null;
//        }

        return intent;
    }

    public void writeConfigFile(Context context) throws IOException {
        FileWriter cfg = new FileWriter(VPNLaunchHelper.getConfigFilePath(context));
        cfg.write(getConfigFile(context, false));
        cfg.flush();
        cfg.close();

    }

    public Intent getStartServiceIntent(Context context) {
        String prefix = context.getPackageName();

        Intent intent = new Intent(context, OpenVPNService.class);
        intent.putExtra(prefix + ".profileUUID", mUuid.toString());
        intent.putExtra(prefix + ".profileVersion", mVersion);
        return intent;
    }

    public void checkForRestart(final Context context) {
        /* This method is called when OpenVPNService is restarted */

        if ((mAuthenticationType == VpnProfile.TYPE_KEYSTORE || mAuthenticationType == VpnProfile.TYPE_USERPASS_KEYSTORE)
                && mPrivateKey == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getExternalCertificates(context);

                }
            }).start();
        }
    }

    @Override
    protected VpnProfile clone() throws CloneNotSupportedException {
        VpnProfile copy = (VpnProfile) super.clone();
        copy.mUuid = UUID.randomUUID();
        copy.mConnections = new Connection[mConnections.length];
        int i = 0;
        for (Connection conn : mConnections) {
            copy.mConnections[i++] = conn.clone();
        }
        copy.mAllowedAppsVpn = (HashSet<String>) mAllowedAppsVpn.clone();
        return copy;
    }

    public VpnProfile copy(String name) {
        try {
            VpnProfile copy = clone();
            copy.mName = name;
            return copy;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void pwDidFail(Context c) {

    }

    private X509Certificate[] getKeyStoreCertificates(Context context) throws KeyChainException, InterruptedException {
        PrivateKey privateKey = KeyChain.getPrivateKey(context, mAlias);
        mPrivateKey = privateKey;


        X509Certificate[] caChain = KeyChain.getCertificateChain(context, mAlias);
        return caChain;
    }

    private X509Certificate[] getExtAppCertificates(Context context) throws KeyChainException {
        if (mExternalAuthenticator == null || mAlias == null)
            throw new KeyChainException("Alias or external auth provider name not set");
        return ExtAuthHelper.getCertificateChain(context, mExternalAuthenticator, mAlias);
    }

    public String[] getExternalCertificates(Context context) {
        return getExternalCertificates(context, 5);
    }


    synchronized String[] getExternalCertificates(Context context, int tries) {
        // Force application context- KeyChain methods will block long enough that by the time they
        // are finished and try to unbind, the original activity context might have been destroyed.
        context = context.getApplicationContext();

        try {
            String keystoreChain = null;

            X509Certificate caChain[];
            if (mAuthenticationType == TYPE_EXTERNAL_APP) {
                caChain = getExtAppCertificates(context);
            } else {
                caChain = getKeyStoreCertificates(context);
            }
            if (caChain == null)
                throw new NoCertReturnedException("No certificate returned from Keystore");

            if (caChain.length <= 1 && TextUtils.isEmpty(mCaFilename)) {
                VpnStatus.logMessage(VpnStatus.LogLevel.ERROR, "", context.getString(R.string.keychain_nocacert));
            } else {
                StringWriter ksStringWriter = new StringWriter();

                PemWriter pw = new PemWriter(ksStringWriter);
                for (int i = 1; i < caChain.length; i++) {
                    X509Certificate cert = caChain[i];
                    pw.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
                }
                pw.close();
                keystoreChain = ksStringWriter.toString();
            }


            String caout = null;
            if (!TextUtils.isEmpty(mCaFilename)) {
                try {
                    Certificate[] cacerts = X509Utils.getCertificatesFromFile(mCaFilename);
                    StringWriter caoutWriter = new StringWriter();
                    PemWriter pw = new PemWriter(caoutWriter);

                    for (Certificate cert : cacerts)
                        pw.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
                    pw.close();
                    caout = caoutWriter.toString();

                } catch (Exception e) {
                    VpnStatus.logError("Could not read CA certificate" + e.getLocalizedMessage());
                }
            }


            StringWriter certout = new StringWriter();


            if (caChain.length >= 1) {
                X509Certificate usercert = caChain[0];

                PemWriter upw = new PemWriter(certout);
                upw.writeObject(new PemObject("CERTIFICATE", usercert.getEncoded()));
                upw.close();

            }
            String user = certout.toString();


            String ca, extra;
            if (caout == null) {
                ca = keystoreChain;
                extra = null;
            } else {
                ca = caout;
                extra = keystoreChain;
            }

            return new String[]{ca, extra, user};
        } catch (InterruptedException | IOException | KeyChainException | NoCertReturnedException | IllegalArgumentException
                | CertificateException e) {
            e.printStackTrace();
            VpnStatus.logError(R.string.keyChainAccessError, e.getLocalizedMessage());

            VpnStatus.logError(R.string.keychain_access);
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
                if (!mAlias.matches("^[a-zA-Z0-9]$")) {
                    VpnStatus.logError(R.string.jelly_keystore_alphanumeric_bug);
                }
            }
            return null;

        } catch (AssertionError e) {
            if (tries == 0)
                return null;
            VpnStatus.logError(String.format("Failure getting Keystore Keys (%s), retrying", e.getLocalizedMessage()));
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e1) {
                VpnStatus.logException(e1);
            }
            return getExternalCertificates(context, tries - 1);
        }

    }

    public int checkProfile(Context c) {
        return checkProfile(c, doUseOpenVPN3(c));
    }

    //! Return an error if something is wrong
    public int checkProfile(Context context, boolean useOpenVPN3) {
        if (mAuthenticationType == TYPE_KEYSTORE || mAuthenticationType == TYPE_USERPASS_KEYSTORE || mAuthenticationType == TYPE_EXTERNAL_APP) {
            if (mAlias == null)
                return R.string.no_keystore_cert_selected;
        } else if (mAuthenticationType == TYPE_CERTIFICATES || mAuthenticationType == TYPE_USERPASS_CERTIFICATES) {
            if (TextUtils.isEmpty(mCaFilename))
                return R.string.no_ca_cert_selected;
        }

        if (mCheckRemoteCN && mX509AuthType == X509_VERIFY_TLSREMOTE)
            return R.string.deprecated_tls_remote;

        if (!mUsePull || mAuthenticationType == TYPE_STATICKEYS) {
            if (mIPv4Address == null || cidrToIPAndNetmask(mIPv4Address) == null)
                return R.string.ipv4_format_error;
        }
        if (!mUseDefaultRoute) {
            if (!TextUtils.isEmpty(mCustomRoutes) && getCustomRoutes(mCustomRoutes).size() == 0)
                return R.string.custom_route_format_error;

            if (!TextUtils.isEmpty(mExcludedRoutes) && getCustomRoutes(mExcludedRoutes).size() == 0)
                return R.string.custom_route_format_error;

        }

        if (mUseTLSAuth && TextUtils.isEmpty(mTLSAuthFilename))
            return R.string.missing_tlsauth;

        if ((mAuthenticationType == TYPE_USERPASS_CERTIFICATES || mAuthenticationType == TYPE_CERTIFICATES)
                && (TextUtils.isEmpty(mClientCertFilename) || TextUtils.isEmpty(mClientKeyFilename)))
            return R.string.missing_certificates;

        if ((mAuthenticationType == TYPE_CERTIFICATES || mAuthenticationType == TYPE_USERPASS_CERTIFICATES)
                && TextUtils.isEmpty(mCaFilename))
            return R.string.missing_ca_certificate;


        boolean noRemoteEnabled = true;
        for (Connection c : mConnections) {
            if (c.mEnabled)
                noRemoteEnabled = false;

        }
        if (noRemoteEnabled)
            return R.string.remote_no_server_selected;

        if (useOpenVPN3) {
            if (mAuthenticationType == TYPE_STATICKEYS) {
                return R.string.openvpn3_nostatickeys;
            }
            if (mAuthenticationType == TYPE_PKCS12 || mAuthenticationType == TYPE_USERPASS_PKCS12) {
                return R.string.openvpn3_pkcs12;
            }
            for (Connection conn : mConnections) {
                if (conn.mProxyType == Connection.ProxyType.ORBOT || conn.mProxyType == Connection.ProxyType.SOCKS5)
                    return R.string.openvpn3_socksproxy;
            }
        }
        for (Connection c : mConnections) {
            if (c.mProxyType == Connection.ProxyType.ORBOT) {
                if (usesExtraProxyOptions())
                    return R.string.error_orbot_and_proxy_options;
                if (!OrbotHelper.checkTorReceier(context))
                    return R.string.no_orbotfound;
            }
        }


        // Everything okay
        return R.string.no_error_found;

    }

    //! Openvpn asks for a "Private Key", this should be pkcs12 key
    //
    public String getPasswordPrivateKey() {
        String cachedPw = PasswordCache.getPKCS12orCertificatePassword(mUuid, true);
        if (cachedPw != null) {
            return cachedPw;
        }
        switch (mAuthenticationType) {
            case TYPE_PKCS12:
            case TYPE_USERPASS_PKCS12:
                return mPKCS12Password;

            case TYPE_CERTIFICATES:
            case TYPE_USERPASS_CERTIFICATES:
                return mKeyPassword;

            case TYPE_USERPASS:
            case TYPE_STATICKEYS:
            default:
                return null;
        }
    }

    public boolean isUserPWAuth() {
        switch (mAuthenticationType) {
            case TYPE_USERPASS:
            case TYPE_USERPASS_CERTIFICATES:
            case TYPE_USERPASS_KEYSTORE:
            case TYPE_USERPASS_PKCS12:
                return true;
            default:
                return false;

        }
    }

    public boolean requireTLSKeyPassword() {
        if (TextUtils.isEmpty(mClientKeyFilename))
            return false;

        String data = "";
        if (isEmbedded(mClientKeyFilename))
            data = mClientKeyFilename;
        else {
            char[] buf = new char[2048];
            FileReader fr;
            try {
                fr = new FileReader(mClientKeyFilename);
                int len = fr.read(buf);
                while (len > 0) {
                    data += new String(buf, 0, len);
                    len = fr.read(buf);
                }
                fr.close();
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }

        }

        if (data.contains("Proc-Type: 4,ENCRYPTED"))
            return true;
        else if (data.contains("-----BEGIN ENCRYPTED PRIVATE KEY-----"))
            return true;
        else
            return false;
    }

    public int needUserPWInput(String transientCertOrPkcs12PW, String mTransientAuthPW) {
        if ((mAuthenticationType == TYPE_PKCS12 || mAuthenticationType == TYPE_USERPASS_PKCS12) &&
                (mPKCS12Password == null || mPKCS12Password.equals(""))) {
            if (transientCertOrPkcs12PW == null)
                return R.string.pkcs12_file_encryption_key;
        }

        if (mAuthenticationType == TYPE_CERTIFICATES || mAuthenticationType == TYPE_USERPASS_CERTIFICATES) {
            if (requireTLSKeyPassword() && TextUtils.isEmpty(mKeyPassword))
                if (transientCertOrPkcs12PW == null) {
                    return R.string.private_key_password;
                }
        }

        if (isUserPWAuth() &&
                (TextUtils.isEmpty(mUsername) ||
                        (TextUtils.isEmpty(mPassword) && mTransientAuthPW == null))) {
            return R.string.password;
        }
        return 0;
    }

    public String getPasswordAuth() {
        String cachedPw = PasswordCache.getAuthPassword(mUuid, true);
        if (cachedPw != null) {
            return cachedPw;
        } else {
            return mPassword;
        }
    }

    // Used by the Array Adapter
    @Override
    public String toString() {
        return mName;
    }

    public String getUUIDString() {
        return mUuid.toString().toLowerCase(Locale.ENGLISH);
    }

    public PrivateKey getKeystoreKey() {
        return mPrivateKey;
    }

    @Nullable
    public String getSignedData(Context c, String b64data, boolean pkcs1padding) {
        byte[] data = Base64.decode(b64data, Base64.DEFAULT);
        byte[] signed_bytes;
        if (mAuthenticationType == TYPE_EXTERNAL_APP)
            signed_bytes = getExtAppSignedData(c, data);
        else
            signed_bytes = getKeyChainSignedData(data, pkcs1padding);

        if (signed_bytes != null)
            return Base64.encodeToString(signed_bytes, Base64.NO_WRAP);
        else
            return null;
    }

    private byte[] getExtAppSignedData(Context c, byte[] data) {
        if (TextUtils.isEmpty(mExternalAuthenticator))
            return null;
        try {
            return ExtAuthHelper.signData(c, mExternalAuthenticator, mAlias, data);
        } catch (KeyChainException | InterruptedException e) {
            VpnStatus.logError(R.string.error_extapp_sign, mExternalAuthenticator, e.getClass().toString(), e.getLocalizedMessage());
            return null;
        }
    }

    private byte[] getKeyChainSignedData(byte[] data, boolean pkcs1padding) {

        PrivateKey privkey = getKeystoreKey();
        // The Jelly Bean *evil* Hack
        // 4.2 implements the RSA/ECB/PKCS1PADDING in the OpenSSLprovider
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            return processSignJellyBeans(privkey, data, pkcs1padding);
        }


        try {
            @SuppressLint("GetInstance")
            String keyalgorithm = privkey.getAlgorithm();

            byte[] signed_bytes;
            if (keyalgorithm.equals("EC")) {
                Signature signer = Signature.getInstance("NONEwithECDSA");

                signer.initSign(privkey);
                signer.update(data);
                signed_bytes = signer.sign();

            } else {
            /* ECB is perfectly fine in this special case, since we are using it for
               the public/private part in the TLS exchange
             */
                Cipher signer;
                if (pkcs1padding)
                    signer = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
                else
                    signer = Cipher.getInstance("RSA/ECB/NoPadding");


                signer.init(Cipher.ENCRYPT_MODE, privkey);

                signed_bytes = signer.doFinal(data);
            }
            return signed_bytes;
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException | NoSuchPaddingException | SignatureException e) {
            VpnStatus.logError(R.string.error_rsa_sign, e.getClass().toString(), e.getLocalizedMessage());
            return null;
        }
    }

    private byte[] processSignJellyBeans(PrivateKey privkey, byte[] data, boolean pkcs1padding) {
        try {
            Method getKey = privkey.getClass().getSuperclass().getDeclaredMethod("getOpenSSLKey");
            getKey.setAccessible(true);

            // Real object type is OpenSSLKey
            Object opensslkey = getKey.invoke(privkey);

            getKey.setAccessible(false);

            Method getPkeyContext = opensslkey.getClass().getDeclaredMethod("getPkeyContext");

            // integer pointer to EVP_pkey
            getPkeyContext.setAccessible(true);
            int pkey = (Integer) getPkeyContext.invoke(opensslkey);
            getPkeyContext.setAccessible(false);

            // 112 with TLS 1.2 (172 back with 4.3), 36 with TLS 1.0
            return NativeUtils.rsasign(data, pkey, pkcs1padding);

        } catch (NoSuchMethodException | InvalidKeyException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            VpnStatus.logError(R.string.error_rsa_sign, e.getClass().toString(), e.getLocalizedMessage());
            return null;
        }
    }

    private boolean usesExtraProxyOptions() {
        if (mUseCustomConfig && mCustomConfigOptions != null && mCustomConfigOptions.contains("http-proxy-option "))
            return true;
        for (Connection c : mConnections)
            if (c.usesExtraProxyOptions())
                return true;

        return false;
    }

    class NoCertReturnedException extends Exception {
        public NoCertReturnedException(String msg) {
            super(msg);
        }
    }


}






/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import android.util.Pair;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by arne on 08.11.16.
 */

public class OpenVPNStatusService extends Service implements VpnStatus.LogListener, VpnStatus.ByteCountListener, VpnStatus.StateListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    static final RemoteCallbackList<IStatusCallbacks> mCallbacks =
            new RemoteCallbackList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        VpnStatus.addLogListener(this);
        VpnStatus.addByteCountListener(this);
        VpnStatus.addStateListener(this);
        mHandler.setService(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        VpnStatus.removeLogListener(this);
        VpnStatus.removeByteCountListener(this);
        VpnStatus.removeStateListener(this);
        mCallbacks.kill();

    }

    private static final IServiceStatus.Stub mBinder = new IServiceStatus.Stub() {

        @Override
        public ParcelFileDescriptor registerStatusCallback(IStatusCallbacks cb) throws RemoteException {
            final LogItem[] logbuffer = VpnStatus.getlogbuffer();
            if (mLastUpdateMessage != null)
                sendUpdate(cb, mLastUpdateMessage);

            mCallbacks.register(cb);
            try {
                final ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
                new Thread("pushLogs") {
                    @Override
                    public void run() {
                        DataOutputStream fd = new DataOutputStream(new ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]));
                        try {
                            synchronized (VpnStatus.readFileLock) {
                                if (!VpnStatus.readFileLog) {
                                    VpnStatus.readFileLock.wait();
                                }
                            }
                        } catch (InterruptedException e) {
                            VpnStatus.logException(e);
                        }
                        try {

                            for (LogItem logItem : logbuffer) {
                                byte[] bytes = logItem.getMarschaledBytes();
                                fd.writeShort(bytes.length);
                                fd.write(bytes);
                            }
                            // Mark end
                            fd.writeShort(0x7fff);
                            fd.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }.start();
                return pipe[0];
            } catch (IOException e) {
                e.printStackTrace();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    throw new RemoteException(e.getMessage());
                }
                return null;
            }
        }

        @Override
        public void unregisterStatusCallback(IStatusCallbacks cb) throws RemoteException {
            mCallbacks.unregister(cb);
        }

        @Override
        public String getLastConnectedVPN() throws RemoteException {
            return VpnStatus.getLastConnectedVPNProfile();
        }

        @Override
        public void setCachedPassword(String uuid, int type, String password) {
            PasswordCache.setCachedPassword(uuid, type, password);
        }

        @Override
        public TrafficHistory getTrafficHistory() throws RemoteException {
            return VpnStatus.trafficHistory;
        }

    };

    @Override
    public void newLog(LogItem logItem) {
        Message msg = mHandler.obtainMessage(SEND_NEW_LOGITEM, logItem);
        msg.sendToTarget();
    }

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {
        Message msg = mHandler.obtainMessage(SEND_NEW_BYTECOUNT, Pair.create(in, out));
        msg.sendToTarget();
    }

    static UpdateMessage mLastUpdateMessage;

    static class UpdateMessage {
        public String state;
        public String logmessage;
        public ConnectionStatus level;
        public Intent intent;
        int resId;

        UpdateMessage(String state, String logmessage, int resId, ConnectionStatus level, Intent intent) {
            this.state = state;
            this.resId = resId;
            this.logmessage = logmessage;
            this.level = level;
            this.intent = intent;
        }
    }


    @Override
    public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent intent) {

        mLastUpdateMessage = new UpdateMessage(state, logmessage, localizedResId, level, intent);
        Message msg = mHandler.obtainMessage(SEND_NEW_STATE, mLastUpdateMessage);
        msg.sendToTarget();
    }

    @Override
    public void setConnectedVPN(String uuid) {
        Message msg = mHandler.obtainMessage(SEND_NEW_CONNECTED_VPN, uuid);
        msg.sendToTarget();
    }

    private static final OpenVPNStatusHandler mHandler = new OpenVPNStatusHandler();

    private static final int SEND_NEW_LOGITEM = 100;
    private static final int SEND_NEW_STATE = 101;
    private static final int SEND_NEW_BYTECOUNT = 102;
    private static final int SEND_NEW_CONNECTED_VPN = 103;

    private static class OpenVPNStatusHandler extends Handler {
        WeakReference<OpenVPNStatusService> service = null;

        private void setService(OpenVPNStatusService statusService) {
            service = new WeakReference<>(statusService);
        }

        @Override
        public void handleMessage(Message msg) {

            RemoteCallbackList<IStatusCallbacks> callbacks;
            if (service == null || service.get() == null)
                return;
            callbacks = service.get().mCallbacks;
            // Broadcast to all clients the new value.
            final int N = callbacks.beginBroadcast();
            for (int i = 0; i < N; i++) {

                try {
                    IStatusCallbacks broadcastItem = callbacks.getBroadcastItem(i);

                    switch (msg.what) {
                        case SEND_NEW_LOGITEM:
                            broadcastItem.newLogItem((LogItem) msg.obj);
                            break;
                        case SEND_NEW_BYTECOUNT:
                            Pair<Long, Long> inout = (Pair<Long, Long>) msg.obj;
                            broadcastItem.updateByteCount(inout.first, inout.second);
                            break;
                        case SEND_NEW_STATE:
                            sendUpdate(broadcastItem, (UpdateMessage) msg.obj);
                            break;

                        case SEND_NEW_CONNECTED_VPN:
                            broadcastItem.connectedVPN((String) msg.obj);
                            break;
                    }
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
            }
            callbacks.finishBroadcast();
        }
    }

    private static void sendUpdate(IStatusCallbacks broadcastItem,
                                   UpdateMessage um) throws RemoteException {
        broadcastItem.updateStateString(um.state, um.logmessage, um.resId, um.level, um.intent);
    }
}

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.preference.PreferenceManager;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.core.VpnStatus.ByteCountListener;

import java.util.LinkedList;
import java.util.Objects;
import java.util.StringTokenizer;

import static de.blinkt.openvpn.core.OpenVPNManagement.pauseReason;

public class DeviceStateReceiver extends BroadcastReceiver implements ByteCountListener, OpenVPNManagement.PausedStateCallback {
    private final Handler mDisconnectHandler;
    private int lastNetwork = -1;
    private OpenVPNManagement mManagement;

    // Window time in s
    private final int TRAFFIC_WINDOW = 60;
    // Data traffic limit in bytes
    private final long TRAFFIC_LIMIT = 64 * 1024;

    // Time to wait after network disconnect to pause the VPN
    private final int DISCONNECT_WAIT = 20;


    connectState network = connectState.DISCONNECTED;
    connectState screen = connectState.SHOULDBECONNECTED;
    connectState userpause = connectState.SHOULDBECONNECTED;

    private String lastStateMsg = null;
    private java.lang.Runnable mDelayDisconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (!(network == connectState.PENDINGDISCONNECT))
                return;

            network = connectState.DISCONNECTED;

            // Set screen state to be disconnected if disconnect pending
            if (screen == connectState.PENDINGDISCONNECT)
                screen = connectState.DISCONNECTED;

            mManagement.pause(getPauseReason());
        }
    };
    private NetworkInfo lastConnectedNetwork;

    @Override
    public boolean shouldBeRunning() {
        return shouldBeConnected();
    }

    private enum connectState {
        SHOULDBECONNECTED,
        PENDINGDISCONNECT,
        DISCONNECTED
    }

    private static class Datapoint {
        private Datapoint(long t, long d) {
            timestamp = t;
            data = d;
        }

        long timestamp;
        long data;
    }

    private LinkedList<Datapoint> trafficdata = new LinkedList<>();


    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {
        if (screen != connectState.PENDINGDISCONNECT)
            return;

        long total = diffIn + diffOut;
        trafficdata.add(new Datapoint(System.currentTimeMillis(), total));

        while (trafficdata.getFirst().timestamp <= (System.currentTimeMillis() - TRAFFIC_WINDOW * 1000)) {
            trafficdata.removeFirst();
        }

        long windowtraffic = 0;
        for (Datapoint dp : trafficdata)
            windowtraffic += dp.data;

        if (windowtraffic < TRAFFIC_LIMIT) {
            screen = connectState.DISCONNECTED;
            VpnStatus.logInfo(R.string.screenoff_pause,
                    "64 kB", TRAFFIC_WINDOW);

            mManagement.pause(getPauseReason());
        }
    }


    public void userPause(boolean pause) {
        if (pause) {
            userpause = connectState.DISCONNECTED;
            // Check if we should disconnect
            mManagement.pause(getPauseReason());
        } else {
            boolean wereConnected = shouldBeConnected();
            userpause = connectState.SHOULDBECONNECTED;
            if (shouldBeConnected() && !wereConnected)
                mManagement.resume();
            else
                // Update the reason why we currently paused
                mManagement.pause(getPauseReason());
        }
    }

    public DeviceStateReceiver(OpenVPNManagement magnagement) {
        super();
        mManagement = magnagement;
        mManagement.setPauseCallback(this);
        mDisconnectHandler = new Handler();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(context);


        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            networkStateChange(context);
        } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            boolean screenOffPause = prefs.getBoolean("screenoff", false);

            if (screenOffPause) {
                if (ProfileManager.getLastConnectedVpn() != null && !ProfileManager.getLastConnectedVpn().mPersistTun)
                    VpnStatus.logError(R.string.screen_nopersistenttun);

                screen = connectState.PENDINGDISCONNECT;
                fillTrafficData();
                if (network == connectState.DISCONNECTED || userpause == connectState.DISCONNECTED)
                    screen = connectState.DISCONNECTED;
            }
        } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            // Network was disabled because screen off
            boolean connected = shouldBeConnected();
            screen = connectState.SHOULDBECONNECTED;

            /* We should connect now, cancel any outstanding disconnect timer */
            mDisconnectHandler.removeCallbacks(mDelayDisconnectRunnable);
            /* should be connected has changed because the screen is on now, connect the VPN */
            if (shouldBeConnected() != connected)
                mManagement.resume();
            else if (!shouldBeConnected())
                /*Update the reason why we are still paused */
                mManagement.pause(getPauseReason());

        }
    }


    private void fillTrafficData() {
        trafficdata.add(new Datapoint(System.currentTimeMillis(), TRAFFIC_LIMIT));
    }

    public static boolean equalsObj(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }


    public void networkStateChange(Context context) {
        NetworkInfo networkInfo = getCurrentNetworkInfo(context);
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(context);
        boolean sendusr1 = prefs.getBoolean("netchangereconnect", true);


        String netstatestring;
        if (networkInfo == null) {
            netstatestring = "not connected";
        } else {
            String subtype = networkInfo.getSubtypeName();
            if (subtype == null)
                subtype = "";
            String extrainfo = networkInfo.getExtraInfo();
            if (extrainfo == null)
                extrainfo = "";

			/*
            if(networkInfo.getType()==android.net.ConnectivityManager.TYPE_WIFI) {
				WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiinfo = wifiMgr.getConnectionInfo();
				extrainfo+=wifiinfo.getBSSID();

				subtype += wifiinfo.getNetworkId();
			}*/


            netstatestring = String.format("%2$s %4$s to %1$s %3$s", networkInfo.getTypeName(),
                    networkInfo.getDetailedState(), extrainfo, subtype);
        }

        if (networkInfo != null && networkInfo.getState() == State.CONNECTED) {
            int newnet = networkInfo.getType();

            boolean pendingDisconnect = (network == connectState.PENDINGDISCONNECT);
            network = connectState.SHOULDBECONNECTED;

            boolean sameNetwork;
            if (lastConnectedNetwork == null
                    || lastConnectedNetwork.getType() != networkInfo.getType()
                    || !equalsObj(lastConnectedNetwork.getExtraInfo(), networkInfo.getExtraInfo())
                    )
                sameNetwork = false;
            else
                sameNetwork = true;

            /* Same network, connection still 'established' */
            if (pendingDisconnect && sameNetwork) {
                mDisconnectHandler.removeCallbacks(mDelayDisconnectRunnable);
                // Reprotect the sockets just be sure
                mManagement.networkChange(true);
            } else {
                /* Different network or connection not established anymore */

                if (screen == connectState.PENDINGDISCONNECT)
                    screen = connectState.DISCONNECTED;

                if (shouldBeConnected()) {
                    mDisconnectHandler.removeCallbacks(mDelayDisconnectRunnable);

                    if (pendingDisconnect || !sameNetwork)
                        mManagement.networkChange(sameNetwork);
                    else
                        mManagement.resume();
                }

                lastNetwork = newnet;
                lastConnectedNetwork = networkInfo;
            }
        } else if (networkInfo == null) {
            // Not connected, stop openvpn, set last connected network to no network
            lastNetwork = -1;
            if (sendusr1) {
                network = connectState.PENDINGDISCONNECT;
                mDisconnectHandler.postDelayed(mDelayDisconnectRunnable, DISCONNECT_WAIT * 1000);

            }
        }


        if (!netstatestring.equals(lastStateMsg))
            VpnStatus.logInfo(R.string.netstatus, netstatestring);
        VpnStatus.logDebug(String.format("Debug state info: %s, pause: %s, shouldbeconnected: %s, network: %s ",
                netstatestring, getPauseReason(), shouldBeConnected(), network));
        lastStateMsg = netstatestring;

    }


    public boolean isUserPaused() {
        return userpause == connectState.DISCONNECTED;
    }

    private boolean shouldBeConnected() {
        return (screen == connectState.SHOULDBECONNECTED && userpause == connectState.SHOULDBECONNECTED &&
                network == connectState.SHOULDBECONNECTED);
    }

    private pauseReason getPauseReason() {
        if (userpause == connectState.DISCONNECTED)
            return pauseReason.userPause;

        if (screen == connectState.DISCONNECTED)
            return pauseReason.screenOff;

        if (network == connectState.DISCONNECTED)
            return pauseReason.noNetwork;

        return pauseReason.userPause;
    }

    private NetworkInfo getCurrentNetworkInfo(Context context) {
        ConnectivityManager conn = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return conn.getActiveNetworkInfo();
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by arne on 08.01.17.
 */

// Until I find a good solution

public class Preferences {

    static SharedPreferences getSharedPreferencesMulti(String name, Context c) {
        return c.getSharedPreferences(name, Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);

    }


    public static SharedPreferences getDefaultSharedPreferences(Context c) {
        return c.getSharedPreferences(c.getPackageName() + "_preferences", Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);

    }


}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

public interface OpenVPNManagement {
    interface PausedStateCallback {
        boolean shouldBeRunning();
    }

    enum pauseReason {
        noNetwork,
        userPause,
        screenOff,
    }

    int mBytecountInterval = 2;

    void reconnect();

    void pause(pauseReason reason);

    void resume();

    /**
     * @param replaceConnection True if the VPN is connected by a new connection.
     * @return true if there was a process that has been send a stop signal
     */
    boolean stopVPN(boolean replaceConnection);

    /*
     * Rebind the interface
     */
    void networkChange(boolean sameNetwork);

    void setPauseCallback(PausedStateCallback callback);

    /**
     * Send the response to a challenge response
     * @param response  Base64 encoded response
     */
    void sendCRResponse(String response);
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import static de.blinkt.openvpn.core.ConnectionStatus.LEVEL_CONNECTED;
import static de.blinkt.openvpn.core.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT;
import static de.blinkt.openvpn.core.NetworkSpace.IpAddress;

import android.Manifest.permission;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.system.OsConstants;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Vector;

import de.blinkt.openvpn.DisconnectVPNActivity;
import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.api.ExternalAppDatabase;
import de.blinkt.openvpn.core.VpnStatus.ByteCountListener;
import de.blinkt.openvpn.core.VpnStatus.StateListener;
import de.blinkt.openvpn.utils.TotalTraffic;

public class OpenVPNService extends VpnService implements StateListener, Callback, ByteCountListener, IOpenVPNServiceInternal {

    private String byteIn, byteOut;
    private String duration;

    public static final String START_SERVICE = "de.blinkt.openvpn.START_SERVICE";
    public static final String START_SERVICE_STICKY = "de.blinkt.openvpn.START_SERVICE_STICKY";
    public static final String ALWAYS_SHOW_NOTIFICATION = "de.blinkt.openvpn.NOTIFICATION_ALWAYS_VISIBLE";
    public static final String DISCONNECT_VPN = "de.blinkt.openvpn.DISCONNECT_VPN";
    public static final String NOTIFICATION_CHANNEL_BG_ID = "openvpn_bg";
    public static final String NOTIFICATION_CHANNEL_NEWSTATUS_ID = "openvpn_newstat";
    public static final String NOTIFICATION_CHANNEL_USERREQ_ID = "openvpn_userreq";

    public static final String VPNSERVICE_TUN = "vpnservice-tun";
    public final static String ORBOT_PACKAGE_NAME = "org.torproject.android";
    private static final String PAUSE_VPN = "de.blinkt.openvpn.PAUSE_VPN";
    private static final String RESUME_VPN = "de.blinkt.openvpn.RESUME_VPN";

    public static final String EXTRA_CHALLENGE_TXT = "de.blinkt.openvpn.core.CR_TEXT_CHALLENGE";
    public static final String EXTRA_CHALLENGE_OPENURL = "de.blinkt.openvpn.core.OPENURL_CHALLENGE";

    private static final int PRIORITY_MIN = -2;
    private static final int PRIORITY_DEFAULT = 0;
    private static final int PRIORITY_MAX = 2;
    private static boolean mNotificationAlwaysVisible = false;
    private static Class<? extends Activity> mNotificationActivityClass;
    private final Vector<String> mDnslist = new Vector<>();
    private final NetworkSpace mRoutes = new NetworkSpace();
    private final NetworkSpace mRoutesv6 = new NetworkSpace();
    private final Object mProcessLock = new Object();
    private String lastChannel;
    private Thread mProcessThread = null;
    private VpnProfile mProfile;
    private String mDomain = null;
    private CIDRIP mLocalIP = null;
    private int mMtu;
    private String mLocalIPv6 = null;
    private DeviceStateReceiver mDeviceStateReceiver;
    private boolean mDisplayBytecount = false;
    private boolean mStarting = false;
    private long mConnecttime;
    private OpenVPNManagement mManagement;
    /*private final IBinder mBinder = new IOpenVPNServiceInternal.Stub() {

        @Override
        public boolean protect(int fd) throws RemoteException {
            return OpenVPNService.this.protect(fd);
        }

        @Override
        public void userPause(boolean shouldbePaused) throws RemoteException {
            OpenVPNService.this.userPause(shouldbePaused);
        }

        @Override
        public boolean stopVPN(boolean replaceConnection) throws RemoteException {
            return OpenVPNService.this.stopVPN(replaceConnection);
        }

        @Override
        public void addAllowedExternalApp(String packagename) throws RemoteException {
            OpenVPNService.this.addAllowedExternalApp(packagename);
        }

        @Override
        public boolean isAllowedExternalApp(String packagename) throws RemoteException {
            return OpenVPNService.this.isAllowedExternalApp(packagename);

        }

        @Override
        public void challengeResponse(String repsonse) throws RemoteException {
            OpenVPNService.this.challengeResponse(repsonse);
        }


    };*/

    private final IBinder mBinder = new LocalBinder();
    private static String state = "";
    boolean flag = false;
    private String mLastTunCfg;
    private String mRemoteGW;
    private Handler guiHandler;
    private Toast mlastToast;
    private Runnable mOpenVPNThread;

    // From: http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public static String humanReadableByteCount(long bytes, boolean speed, Resources res) {
        if (speed)
            bytes = bytes * 8;
        int unit = speed ? 1000 : 1024;


        int exp = Math.max(0, Math.min((int) (Math.log(bytes) / Math.log(unit)), 3));

        float bytesUnit = (float) (bytes / Math.pow(unit, exp));

        if (speed)
            switch (exp) {
                case 0:
                    return res.getString(R.string.bits_per_second, bytesUnit);
                case 1:
                    return res.getString(R.string.kbits_per_second, bytesUnit);
                case 2:
                    return res.getString(R.string.mbits_per_second, bytesUnit);
                default:
                    return res.getString(R.string.gbits_per_second, bytesUnit);
            }
        else
            switch (exp) {
                case 0:
                    return res.getString(R.string.volume_byte, bytesUnit);
                case 1:
                    return res.getString(R.string.volume_kbyte, bytesUnit);
                case 2:
                    return res.getString(R.string.volume_mbyte, bytesUnit);
                default:
                    return res.getString(R.string.volume_gbyte, bytesUnit);

            }
    }

    public static double humanReadableDoubleByteCount(long bytes, boolean speed) {
        if (speed)
            bytes = bytes * 8;
        int unit = speed ? 1000 : 1024;


        int exp = Math.max(0, Math.min((int) (Math.log(bytes) / Math.log(unit)), 3));

        float bytesUnit = (float) (bytes / Math.pow(unit, exp));

        return bytesUnit;
    }

    /**
     * Sets the activity which should be opened when tapped on the permanent notification tile.
     *
     * @param activityClass The activity class to open
     */
    public static void setNotificationActivityClass(Class<? extends Activity> activityClass) {
        mNotificationActivityClass = activityClass;
    }

    PendingIntent getContentIntent() {
        try {
            if (mNotificationActivityClass != null) {
                // Let the configure Button show the Log
                Intent intent = new Intent(getBaseContext(), mNotificationActivityClass);
                String typeStart = Objects.requireNonNull(
                        mNotificationActivityClass.getField("TYPE_START").get(null)).toString();
                Integer typeFromNotify = Integer.parseInt(Objects.requireNonNull(mNotificationActivityClass.getField("TYPE_FROM_NOTIFY").get(null)).toString());
                intent.putExtra(typeStart, typeFromNotify);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        } catch (Exception e) {
            Log.e(this.getClass().getCanonicalName(), "Build detail intent error", e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void addAllowedExternalApp(String packagename) throws RemoteException {
        ExternalAppDatabase extapps = new ExternalAppDatabase(OpenVPNService.this);
        extapps.addApp(packagename);
    }

    @Override
    public boolean isAllowedExternalApp(String packagename) throws RemoteException {
        ExternalAppDatabase extapps = new ExternalAppDatabase(OpenVPNService.this);
        return extapps.checkRemoteActionPermission(this, packagename);
    }

    @Override
    public void challengeResponse(String response) throws RemoteException {
        if (mManagement != null) {
            String b64response = Base64.encodeToString(response.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT);
            mManagement.sendCRResponse(b64response);
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(START_SERVICE))
            return mBinder;
        else
            return super.onBind(intent);
    }

    @Override
    public void onRevoke() {
        VpnStatus.logError(R.string.permission_revoked);
        mManagement.stopVPN(false);
        endVpnService();
    }

    // Similar to revoke but do not try to stop process
    public void openvpnStopped() {
        endVpnService();
    }

    public void endVpnService() {
        synchronized (mProcessLock) {
            mProcessThread = null;
        }
        VpnStatus.removeByteCountListener(this);
        unregisterDeviceStateReceiver();
        ProfileManager.setConntectedVpnProfileDisconnected(this);
        mOpenVPNThread = null;
        if (!mStarting) {
            stopForeground(!mNotificationAlwaysVisible);

            if (!mNotificationAlwaysVisible) {
                stopSelf();
                VpnStatus.removeStateListener(this);
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId) {
        if (lastChannel == null || lastChannel.isEmpty()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Communication",
                        NotificationManager.IMPORTANCE_LOW
                );

                NotificationManager nManager = getSystemService(NotificationManager.class);
                if (nManager != null) {
                    nManager.createNotificationChannel(channel);
                }
            }

            lastChannel = channelId;
        }

        return lastChannel;

    }

    private void showNotification(final String msg, String tickerText, @NonNull String channel,
                                  long when, ConnectionStatus status, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = createNotificationChannel(channel);
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //The Error: it seems a bug in android greater than version 8, where it needs to Identify the channelId before a starting a foreground : https://stackoverflow.com/questions/47531742/startforeground-fail-after-upgrade-to-android-8-1/51281297#51281297
        // It was Already Fixed in Android 12 :https://issuetracker.google.com/issues/192032398#comment6
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this, channel);

        int priority;
        if (channel.equals(NOTIFICATION_CHANNEL_BG_ID))
            priority = NotificationCompat.PRIORITY_LOW;
        else if (channel.equals(NOTIFICATION_CHANNEL_USERREQ_ID))
            priority = NotificationCompat.PRIORITY_LOW;
        else
            priority = NotificationCompat.PRIORITY_LOW;

        if (mProfile != null)
            nBuilder.setContentTitle(getString(R.string.notifcation_title, mProfile.mName));
        else
            nBuilder.setContentTitle(getString(R.string.notifcation_title_notconnect));

        nBuilder.setContentText(msg);
        nBuilder.setOnlyAlertOnce(true);
        nBuilder.setOngoing(true);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nBuilder.setSmallIcon(R.drawable.app_icon_png);
            nBuilder.setColor(Color.BLACK);
        } else {
            nBuilder.setSmallIcon(R.drawable.app_icon);
        }

        if (when != 0)
            nBuilder.setWhen(when);


        // Try to set the priority available since API 16 (Jellybean)
        jbNotificationExtras(priority, nBuilder);
        //addVpnActionsToNotification(nBuilder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            lpNotificationExtras(nBuilder, Notification.CATEGORY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //noinspection NewApi
            nBuilder.setChannelId(channel);
            if (mProfile != null)
                //noinspection NewApi
                nBuilder.setShortcutId(mProfile.getUUIDString());

        }

        if (tickerText != null && !tickerText.equals(""))
            nBuilder.setTicker(tickerText);
        try {
            Notification notification = nBuilder.build();
            nBuilder.setPriority(Notification.PRIORITY_LOW);
            nBuilder.setSilent(true);

            int notificationId = channel.hashCode();

            mNotificationManager.notify(notificationId, notification);

            startForeground(notificationId, notification);

            if (lastChannel != null && !channel.equals(lastChannel)) {
                // Cancel old notification
                mNotificationManager.cancel(lastChannel.hashCode());
            }
        } catch (Throwable th) {
            Log.e(getClass().getCanonicalName(), "Error when show notification", th);
        }

        // Check if running on a TV
        if (runningOnAndroidTV() && !(priority < 0))
            guiHandler.post(() -> {

                if (mlastToast != null)
                    mlastToast.cancel();
                String toastText = String.format(Locale.getDefault(), "%s - %s", mProfile.mName, msg);
                mlastToast = Toast.makeText(getBaseContext(), toastText, Toast.LENGTH_SHORT);
                mlastToast.show();
            });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void lpNotificationExtras(NotificationCompat.Builder nbuilder, String category) {
        nbuilder.setCategory(category);
        nbuilder.setLocalOnly(true);

    }
    private int getIconByConnectionStatus(ConnectionStatus level) {
        switch (level) {
            case LEVEL_AUTH_FAILED:
            case LEVEL_NONETWORK:
            case LEVEL_NOTCONNECTED:
                return R.drawable.ic_stat_vpn_offline;
            case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
            case LEVEL_WAITING_FOR_USER_INPUT:
                return R.drawable.ic_stat_vpn_outline;
            case LEVEL_CONNECTING_SERVER_REPLIED:
                return R.drawable.ic_stat_vpn_empty_halo;
            case LEVEL_VPNPAUSED:
                return android.R.drawable.ic_media_pause;
            case UNKNOWN_LEVEL:
            case LEVEL_CONNECTED:
            default:
                return R.drawable.ic_stat_vpn;

        }
    }
    private boolean runningOnAndroidTV() {
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void jbNotificationExtras(int priority,
                                      NotificationCompat.Builder nbuilder) {
        try {
            if (priority != 0) {
                Method setpriority = nbuilder.getClass().getMethod("setPriority", int.class);
                setpriority.invoke(nbuilder, Notification.PRIORITY_LOW);

                Method setUsesChronometer = nbuilder.getClass().getMethod("setUsesChronometer", boolean.class);
                setUsesChronometer.invoke(nbuilder, true);

            }

            //ignore exception
        } catch (NoSuchMethodException | IllegalArgumentException |
                InvocationTargetException | IllegalAccessException e) {
            VpnStatus.logException(e);
        }

    }

    private void addVpnActionsToNotification(Notification.Builder nbuilder) {
        Intent disconnectVPN = new Intent(this, DisconnectVPNActivity.class);
        disconnectVPN.setAction(DISCONNECT_VPN);
        PendingIntent disconnectPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            disconnectPendingIntent = PendingIntent.getActivity(this, 0, disconnectVPN, PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            disconnectPendingIntent = PendingIntent.getActivity(this, 0, disconnectVPN, 0);

        }

        nbuilder.addAction(R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.cancel_connection), disconnectPendingIntent);

        Intent pauseVPN = new Intent(this, OpenVPNService.class);
        if (mDeviceStateReceiver == null || !mDeviceStateReceiver.isUserPaused()) {
            pauseVPN.setAction(PAUSE_VPN);
            PendingIntent pauseVPNPending = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                pauseVPNPending = PendingIntent.getService(this, 0, pauseVPN, PendingIntent.FLAG_IMMUTABLE);
            }
            else {
                pauseVPNPending = PendingIntent.getService(this, 0, pauseVPN, 0);

            }
            nbuilder.addAction(R.drawable.ic_menu_pause,
                    getString(R.string.pauseVPN), pauseVPNPending);

        } else {
            pauseVPN.setAction(RESUME_VPN);
            PendingIntent resumeVPNPending = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                resumeVPNPending = PendingIntent.getService(this, 0, pauseVPN, PendingIntent.FLAG_IMMUTABLE);
            }
            else {
                resumeVPNPending = PendingIntent.getService(this, 0, pauseVPN, 0);

            }
            nbuilder.addAction(R.drawable.ic_menu_play,
                    getString(R.string.resumevpn), resumeVPNPending);
        }
    }

    PendingIntent getUserInputIntent(String needed) {
        Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("need", needed);
        Bundle b = new Bundle();
        b.putString("need", needed);
        PendingIntent pIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pIntent = PendingIntent.getActivity(this, 12, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pIntent = PendingIntent.getActivity(this, 12, intent, 0);

        }
        return pIntent;
    }

    PendingIntent getGraphPendingIntent() {
        // Let the configure Button show the Log


        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, getPackageName() + ".view.MainActivity"));

        intent.putExtra("PAGE", "graph");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent startLW = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            startLW = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            startLW = PendingIntent.getActivity(this, 0, intent, 0);
        }
        return startLW;
    }

    synchronized void registerDeviceStateReceiver(OpenVPNManagement magnagement) {
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mDeviceStateReceiver = new DeviceStateReceiver(magnagement);

        // Fetch initial network state
        mDeviceStateReceiver.networkStateChange(this);

        registerReceiver(mDeviceStateReceiver, filter);
        VpnStatus.addByteCountListener(mDeviceStateReceiver);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            addLollipopCMListener(); */
    }

    synchronized void unregisterDeviceStateReceiver() {
        if (mDeviceStateReceiver != null)
            try {
                VpnStatus.removeByteCountListener(mDeviceStateReceiver);
                this.unregisterReceiver(mDeviceStateReceiver);
            } catch (IllegalArgumentException iae) {
                // I don't know why  this happens:
                // java.lang.IllegalArgumentException: Receiver not registered: de.blinkt.openvpn.NetworkSateReceiver@41a61a10
                // Ignore for now ...
                iae.printStackTrace();
            }
        mDeviceStateReceiver = null;

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            removeLollipopCMListener();*/

    }

    public void userPause(boolean shouldBePaused) {
        if (mDeviceStateReceiver != null)
            mDeviceStateReceiver.userPause(shouldBePaused);
    }

    @Override
    public boolean stopVPN(boolean replaceConnection) throws RemoteException {
        if (getManagement() != null)
            return getManagement().stopVPN(replaceConnection);
        else
            return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getBooleanExtra(ALWAYS_SHOW_NOTIFICATION, false))
            mNotificationAlwaysVisible = true;

        VpnStatus.addStateListener(this);
        VpnStatus.addByteCountListener(this);

        guiHandler = new Handler(getMainLooper());

        if (intent != null && DISCONNECT_VPN.equals(intent.getAction())) {
            try {
                stopVPN(false);
            } catch (RemoteException e) {
                VpnStatus.logException(e);
            }
            return START_NOT_STICKY;
        }

        if (intent != null && PAUSE_VPN.equals(intent.getAction())) {
            if (mDeviceStateReceiver != null)
                mDeviceStateReceiver.userPause(true);
            return START_NOT_STICKY;
        }

        if (intent != null && RESUME_VPN.equals(intent.getAction())) {
            if (mDeviceStateReceiver != null)
                mDeviceStateReceiver.userPause(false);
            return START_NOT_STICKY;
        }


        if (intent != null && START_SERVICE.equals(intent.getAction()))
            return START_NOT_STICKY;
        if (intent != null && START_SERVICE_STICKY.equals(intent.getAction())) {
            return START_REDELIVER_INTENT;
        }

        // Always show notification here to avoid problem with startForeground timeout
        VpnStatus.logInfo(R.string.building_configration);
        VpnStatus.updateStateString("VPN_GENERATE_CONFIG", "", R.string.building_configration, ConnectionStatus.LEVEL_START);
        showNotification(VpnStatus.getLastCleanLogMessage(this),
                VpnStatus.getLastCleanLogMessage(this), NOTIFICATION_CHANNEL_NEWSTATUS_ID, 0, ConnectionStatus.LEVEL_START, null);

        if (intent != null && intent.hasExtra(getPackageName() + ".profileUUID")) {
            String profileUUID = intent.getStringExtra(getPackageName() + ".profileUUID");
            int profileVersion = intent.getIntExtra(getPackageName() + ".profileVersion", 0);
            // Try for 10s to get current version of the profile
            mProfile = ProfileManager.get(this, profileUUID, profileVersion, 100);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                updateShortCutUsage(mProfile);
            }

        } else {
            /* The intent is null when we are set as always-on or the service has been restarted. */
            mProfile = ProfileManager.getLastConnectedProfile(this);
            VpnStatus.logInfo(R.string.service_restarted);

            /* Got no profile, just stop */
            if (mProfile == null) {
                Log.d("OpenVPN", "Got no last connected profile on null intent. Assuming always on.");
                mProfile = ProfileManager.getAlwaysOnVPN(this);

                if (mProfile == null) {
                    stopSelf(startId);
                    return START_NOT_STICKY;
                }
            }
            /* Do the asynchronous keychain certificate stuff */
            mProfile.checkForRestart(this);
        }

        if (mProfile == null) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }


        /* start the OpenVPN process itself in a background thread */
        new Thread(new Runnable() {
            @Override
            public void run() {
                startOpenVPN();
            }
        }).start();


        ProfileManager.setConnectedVpnProfile(this, mProfile);
        VpnStatus.setConnectedVPNProfile(mProfile.getUUIDString());

        return START_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private void updateShortCutUsage(VpnProfile profile) {
        if (profile == null)
            return;
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
        shortcutManager.reportShortcutUsed(profile.getUUIDString());
    }

    private void startOpenVPN() {
        try {
            mProfile.writeConfigFile(this);
        } catch (IOException e) {
            VpnStatus.logException("Error writing config file", e);
            endVpnService();
            return;
        }
        String nativeLibraryDirectory = getApplicationInfo().nativeLibraryDir;
        String tmpDir;
        try {
            tmpDir = getApplication().getCacheDir().getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            tmpDir = "/tmp";
        }

        // Write OpenVPN binary
        String[] argv = VPNLaunchHelper.buildOpenvpnArgv(this);


        // Set a flag that we are starting a new VPN
        mStarting = true;
        // Stop the previous session by interrupting the thread.

        stopOldOpenVPNProcess();
        // An old running VPN should now be exited
        mStarting = false;

        // Start a new session by creating a new thread.
        boolean useOpenVPN3 = VpnProfile.doUseOpenVPN3(this);

        // Open the Management Interface
        if (!useOpenVPN3) {
            // start a Thread that handles incoming messages of the managment socket
            OpenVpnManagementThread ovpnManagementThread = new OpenVpnManagementThread(mProfile, this);
            if (ovpnManagementThread.openManagementInterface(this)) {
                Thread mSocketManagerThread = new Thread(ovpnManagementThread, "OpenVPNManagementThread");
                mSocketManagerThread.start();
                mManagement = ovpnManagementThread;
                VpnStatus.logInfo("started Socket Thread");
            } else {
                endVpnService();
                return;
            }
        }

        Runnable processThread;
        if (useOpenVPN3) {
            OpenVPNManagement mOpenVPN3 = instantiateOpenVPN3Core();
            processThread = (Runnable) mOpenVPN3;
            mManagement = mOpenVPN3;
        } else {
            processThread = new OpenVPNThread(this, argv, nativeLibraryDirectory, tmpDir);
            mOpenVPNThread = processThread;
        }

        synchronized (mProcessLock) {
            mProcessThread = new Thread(processThread, "OpenVPNProcessThread");
            mProcessThread.start();
        }

        new Handler(getMainLooper()).post(new Runnable() {
                                              @Override
                                              public void run() {
                                                  if (mDeviceStateReceiver != null)
                                                      unregisterDeviceStateReceiver();

                                                  registerDeviceStateReceiver(mManagement);
                                              }
                                          }

        );
    }


    private void stopOldOpenVPNProcess() {
        if (mManagement != null) {
            if (mOpenVPNThread != null)
                ((OpenVPNThread) mOpenVPNThread).setReplaceConnection();
            if (mManagement.stopVPN(true)) {
                // an old was asked to exit, wait 1s
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }

        forceStopOpenVpnProcess();
    }

    public void forceStopOpenVpnProcess() {
        synchronized (mProcessLock) {
            if (mProcessThread != null) {
                mProcessThread.interrupt();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }

    private OpenVPNManagement instantiateOpenVPN3Core() {
        try {
            Class cl = Class.forName("de.blinkt.openvpn.core.OpenVPNThreadv3");
            return (OpenVPNManagement) cl.getConstructor(OpenVPNService.class, VpnProfile.class).newInstance(this, mProfile);
        } catch (IllegalArgumentException | InstantiationException | InvocationTargetException |
                NoSuchMethodException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public IBinder asBinder() {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        sendMessage("DISCONNECTED");
        synchronized (mProcessLock) {
            if (mProcessThread != null) {
                mManagement.stopVPN(true);
            }
        }

        if (mDeviceStateReceiver != null) {
            this.unregisterReceiver(mDeviceStateReceiver);
        }
        // Just in case unregister for state
        VpnStatus.removeStateListener(this);
        VpnStatus.flushLog();
    }

    private String getTunConfigString() {
        // The format of the string is not important, only that
        // two identical configurations produce the same result
        String cfg = "TUNCFG UNQIUE STRING ips:";

        if (mLocalIP != null)
            cfg += mLocalIP.toString();
        if (mLocalIPv6 != null)
            cfg += mLocalIPv6;


        cfg += "routes: " + TextUtils.join("|", mRoutes.getNetworks(true)) + TextUtils.join("|", mRoutesv6.getNetworks(true));
        cfg += "excl. routes:" + TextUtils.join("|", mRoutes.getNetworks(false)) + TextUtils.join("|", mRoutesv6.getNetworks(false));
        cfg += "dns: " + TextUtils.join("|", mDnslist);
        cfg += "domain: " + mDomain;
        cfg += "mtu: " + mMtu;
        return cfg;
    }

    public ParcelFileDescriptor openTun() {

        //Debug.startMethodTracing(getExternalFilesDir(null).toString() + "/opentun.trace", 40* 1024 * 1024);

        Builder builder = new Builder();

        VpnStatus.logInfo(R.string.last_openvpn_tun_config);

        boolean allowUnsetAF = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !mProfile.mBlockUnusedAddressFamilies;
        if (allowUnsetAF) {
            allowAllAFFamilies(builder);
        }

        if (mLocalIP == null && mLocalIPv6 == null) {
            VpnStatus.logError(getString(R.string.opentun_no_ipaddr));
            return null;
        }

        if (mLocalIP != null) {
            // OpenVPN3 manages excluded local networks by callback
            if (!VpnProfile.doUseOpenVPN3(this))
                addLocalNetworksToRoutes();
            try {
                builder.addAddress(mLocalIP.mIp, mLocalIP.len);
            } catch (IllegalArgumentException iae) {
                VpnStatus.logError(R.string.dns_add_error, mLocalIP, iae.getLocalizedMessage());
                return null;
            }
        }

        if (mLocalIPv6 != null) {
            String[] ipv6parts = mLocalIPv6.split("/");
            try {
                builder.addAddress(ipv6parts[0], Integer.parseInt(ipv6parts[1]));
            } catch (IllegalArgumentException iae) {
                VpnStatus.logError(R.string.ip_add_error, mLocalIPv6, iae.getLocalizedMessage());
                return null;
            }

        }


        for (String dns : mDnslist) {
            try {
                builder.addDnsServer(dns);
            } catch (IllegalArgumentException iae) {
                VpnStatus.logError(R.string.dns_add_error, dns, iae.getLocalizedMessage());
            }
        }

        String release = Build.VERSION.RELEASE;
        if ((Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !release.startsWith("4.4.3")
                && !release.startsWith("4.4.4") && !release.startsWith("4.4.5") && !release.startsWith("4.4.6"))
                && mMtu < 1280) {
            VpnStatus.logInfo(String.format(Locale.US, "Forcing MTU to 1280 instead of %d to workaround Android Bug #70916", mMtu));
            builder.setMtu(1280);
        } else {
            builder.setMtu(mMtu);
        }

        Collection<IpAddress> positiveIPv4Routes = mRoutes.getPositiveIPList();
        Collection<IpAddress> positiveIPv6Routes = mRoutesv6.getPositiveIPList();

        if ("samsung".equals(Build.BRAND) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mDnslist.size() >= 1) {
            // Check if the first DNS Server is in the VPN range
            try {
                IpAddress dnsServer = new IpAddress(new CIDRIP(mDnslist.get(0), 32), true);
                boolean dnsIncluded = false;
                for (IpAddress net : positiveIPv4Routes) {
                    if (net.containsNet(dnsServer)) {
                        dnsIncluded = true;
                    }
                }
                if (!dnsIncluded) {
                    String samsungwarning = String.format("Warning Samsung Android 5.0+ devices ignore DNS servers outside the VPN range. To enable DNS resolution a route to your DNS Server (%s) has been added.", mDnslist.get(0));
                    VpnStatus.logWarning(samsungwarning);
                    positiveIPv4Routes.add(dnsServer);
                }
            } catch (Exception e) {
                // If it looks like IPv6 ignore error
                if (!mDnslist.get(0).contains(":"))
                    VpnStatus.logError("Error parsing DNS Server IP: " + mDnslist.get(0));
            }
        }

        IpAddress multicastRange = new IpAddress(new CIDRIP("224.0.0.0", 3), true);

        for (IpAddress route : positiveIPv4Routes) {
            try {

                if (multicastRange.containsNet(route))
                    VpnStatus.logDebug(R.string.ignore_multicast_route, route.toString());
                else
                    builder.addRoute(route.getIPv4Address(), route.networkMask);
            } catch (IllegalArgumentException ia) {
                VpnStatus.logError(getString(R.string.route_rejected) + route + " " + ia.getLocalizedMessage());
            }
        }

        for (IpAddress route6 : positiveIPv6Routes) {
            try {
                builder.addRoute(route6.getIPv6Address(), route6.networkMask);
            } catch (IllegalArgumentException ia) {
                VpnStatus.logError(getString(R.string.route_rejected) + route6 + " " + ia.getLocalizedMessage());
            }
        }


        if (mDomain != null)
            builder.addSearchDomain(mDomain);

        String ipv4info;
        String ipv6info;
        if (allowUnsetAF) {
            ipv4info = "(not set, allowed)";
            ipv6info = "(not set, allowed)";
        } else {
            ipv4info = "(not set)";
            ipv6info = "(not set)";
        }

        int ipv4len;
        if (mLocalIP != null) {
            ipv4len = mLocalIP.len;
            ipv4info = mLocalIP.mIp;
        } else {
            ipv4len = -1;
        }

        if (mLocalIPv6 != null) {
            ipv6info = mLocalIPv6;
        }

        if ((!mRoutes.getNetworks(false).isEmpty() || !mRoutesv6.getNetworks(false).isEmpty()) && isLockdownEnabledCompat()) {
            VpnStatus.logInfo("VPN lockdown enabled (do not allow apps to bypass VPN) enabled. Route exclusion will not allow apps to bypass VPN (e.g. bypass VPN for local networks)");
        }
        if (mDomain != null) builder.addSearchDomain(mDomain);
        VpnStatus.logInfo(R.string.local_ip_info, ipv4info, ipv4len, ipv6info, mMtu);
        VpnStatus.logInfo(R.string.dns_server_info, TextUtils.join(", ", mDnslist), mDomain);
        VpnStatus.logInfo(R.string.routes_info_incl, TextUtils.join(", ", mRoutes.getNetworks(true)), TextUtils.join(", ", mRoutesv6.getNetworks(true)));
        VpnStatus.logInfo(R.string.routes_info_excl, TextUtils.join(", ", mRoutes.getNetworks(false)), TextUtils.join(", ", mRoutesv6.getNetworks(false)));
        VpnStatus.logDebug(R.string.routes_debug, TextUtils.join(", ", positiveIPv4Routes), TextUtils.join(", ", positiveIPv6Routes));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAllowedVpnPackages(builder);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // VPN always uses the default network
            builder.setUnderlyingNetworks(null);
        }


        String session = mProfile.mName;
        if (mLocalIP != null && mLocalIPv6 != null)
            session = getString(R.string.session_ipv6string, session, mLocalIP, mLocalIPv6);
        else if (mLocalIP != null)
            session = getString(R.string.session_ipv4string, session, mLocalIP);
        else
            session = getString(R.string.session_ipv4string, session, mLocalIPv6);

        builder.setSession(session);

        // No DNS Server, log a warning
        if (mDnslist.size() == 0)
            VpnStatus.logInfo(R.string.warn_no_dns);

        mLastTunCfg = getTunConfigString();

        // Reset information
        mDnslist.clear();
        mRoutes.clear();
        mRoutesv6.clear();
        mLocalIP = null;
        mLocalIPv6 = null;
        mDomain = null;

        builder.setConfigureIntent(getGraphPendingIntent());

        try {
            //Debug.stopMethodTracing();
            ParcelFileDescriptor tun = builder.establish();
            if (tun == null)
                throw new NullPointerException("Android establish() method returned null (Really broken network configuration?)");
            return tun;
        } catch (Exception e) {
            VpnStatus.logError(R.string.tun_open_error);
            VpnStatus.logError(getString(R.string.error) + e.getLocalizedMessage());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                VpnStatus.logError(R.string.tun_error_helpful);
            }
            return null;
        }

    }

    private boolean isLockdownEnabledCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return isLockdownEnabled();
        } else {
            /* We cannot determine this, return false */
            return false;
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void allowAllAFFamilies(Builder builder) {
        builder.allowFamily(OsConstants.AF_INET);
        builder.allowFamily(OsConstants.AF_INET6);
    }

    private void addLocalNetworksToRoutes() {
        for (String net : NetworkUtils.getLocalNetworks(this, false)) {
            String[] netparts = net.split("/");
            String ipAddr = netparts[0];
            int netMask = Integer.parseInt(netparts[1]);
            if (ipAddr.equals(mLocalIP.mIp))
                continue;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && !mProfile.mAllowLocalLAN) {
                mRoutes.addIPSplit(new CIDRIP(ipAddr, netMask), true);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && mProfile.mAllowLocalLAN)
                mRoutes.addIP(new CIDRIP(ipAddr, netMask), false);
        }

        // IPv6 is Lollipop+ only so we can skip the lower than KITKAT case
        if (mProfile.mAllowLocalLAN) {
            for (String net : NetworkUtils.getLocalNetworks(this, true)) {
                addRoutev6(net, false);
            }
        }


    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setAllowedVpnPackages(Builder builder) {
        boolean profileUsesOrBot = false;

        for (Connection c : mProfile.mConnections) {
            if (c.mProxyType == Connection.ProxyType.ORBOT)
                profileUsesOrBot = true;
        }

        if (profileUsesOrBot)
            VpnStatus.logDebug("VPN Profile uses at least one server entry with Orbot. Setting up VPN so that OrBot is not redirected over VPN.");


        boolean atLeastOneAllowedApp = false;

        if (mProfile.mAllowedAppsVpnAreDisallowed && profileUsesOrBot) {
            try {
                builder.addDisallowedApplication(ORBOT_PACKAGE_NAME);
            } catch (PackageManager.NameNotFoundException e) {
                VpnStatus.logDebug("Orbot not installed?");
            }
        }

        for (String pkg : mProfile.mAllowedAppsVpn) {
            try {
                if (mProfile.mAllowedAppsVpnAreDisallowed) {
                    builder.addDisallowedApplication(pkg);
                } else {
                    if (!(profileUsesOrBot && pkg.equals(ORBOT_PACKAGE_NAME))) {
                        builder.addAllowedApplication(pkg);
                        atLeastOneAllowedApp = true;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                mProfile.mAllowedAppsVpn.remove(pkg);
                VpnStatus.logInfo(R.string.app_no_longer_exists, pkg);
            }
        }

        if (!mProfile.mAllowedAppsVpnAreDisallowed && !atLeastOneAllowedApp) {
            VpnStatus.logDebug(R.string.no_allowed_app, getPackageName());
            try {
                builder.addAllowedApplication(getPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                VpnStatus.logError("This should not happen: " + e.getLocalizedMessage());
            }
        }

        if (mProfile.mAllowedAppsVpnAreDisallowed) {
            VpnStatus.logDebug(R.string.disallowed_vpn_apps_info, TextUtils.join(", ", mProfile.mAllowedAppsVpn));
        } else {
            VpnStatus.logDebug(R.string.allowed_vpn_apps_info, TextUtils.join(", ", mProfile.mAllowedAppsVpn));
        }

        if (mProfile.mAllowAppVpnBypass) {
            builder.allowBypass();
            VpnStatus.logDebug("Apps may bypass VPN");
        }
    }

    public void addDNS(String dns) {
        mDnslist.add(dns);
    }

    public void setDomain(String domain) {
        if (mDomain == null) {
            mDomain = domain;
        }
    }

    /**
     * Route that is always included, used by the v3 core
     */
    public void addRoute(CIDRIP route, boolean include) {
        mRoutes.addIP(route, include);
    }

    public void addRoute(String dest, String mask, String gateway, String device) {
        CIDRIP route = new CIDRIP(dest, mask);
        boolean include = isAndroidTunDevice(device);

        IpAddress gatewayIP = new IpAddress(new CIDRIP(gateway, 32), false);

        if (mLocalIP == null) {
            VpnStatus.logError("Local IP address unset and received. Neither pushed server config nor local config specifies an IP addresses. Opening tun device is most likely going to fail.");
            return;
        }
        IpAddress localNet = new IpAddress(mLocalIP, true);
        if (localNet.containsNet(gatewayIP))
            include = true;

        if (gateway != null &&
                (gateway.equals("255.255.255.255") || gateway.equals(mRemoteGW)))
            include = true;


        if (route.len == 32 && !mask.equals("255.255.255.255")) {
            VpnStatus.logWarning(R.string.route_not_cidr, dest, mask);
        }

        if (route.normalise())
            VpnStatus.logWarning(R.string.route_not_netip, dest, route.len, route.mIp);

        mRoutes.addIP(route, include);
    }

    public void addRoutev6(String network, String device) {
        // Tun is opened after ROUTE6, no device name may be present
        boolean included = isAndroidTunDevice(device);
        addRoutev6(network, included);
    }

    public void addRoutev6(String network, boolean included) {
        String[] v6parts = network.split("/");

        try {
            Inet6Address ip = (Inet6Address) InetAddress.getAllByName(v6parts[0])[0];
            int mask = Integer.parseInt(v6parts[1]);
            mRoutesv6.addIPv6(ip, mask, included);

        } catch (UnknownHostException e) {
            VpnStatus.logException(e);
        }


    }

    private boolean isAndroidTunDevice(String device) {
        return device != null &&
                (device.startsWith("tun") || "(null)".equals(device) || VPNSERVICE_TUN.equals(device));
    }

    public void setMtu(int mtu) {
        mMtu = mtu;
    }

    public void setLocalIP(CIDRIP cdrip) {
        mLocalIP = cdrip;
    }

    public void setLocalIP(String local, String netmask, int mtu, String mode) {
        mLocalIP = new CIDRIP(local, netmask);
        mMtu = mtu;
        mRemoteGW = null;

        long netMaskAsInt = CIDRIP.getInt(netmask);

        if (mLocalIP.len == 32 && !netmask.equals("255.255.255.255")) {
            // get the netmask as IP

            int masklen;
            long mask;
            if ("net30".equals(mode)) {
                masklen = 30;
                mask = 0xfffffffc;
            } else {
                masklen = 31;
                mask = 0xfffffffe;
            }

            // Netmask is Ip address +/-1, assume net30/p2p with small net
            if ((netMaskAsInt & mask) == (mLocalIP.getInt() & mask)) {
                mLocalIP.len = masklen;
            } else {
                mLocalIP.len = 32;
                if (!"p2p".equals(mode))
                    VpnStatus.logWarning(R.string.ip_not_cidr, local, netmask, mode);
            }
        }
        if (("p2p".equals(mode) && mLocalIP.len < 32) || ("net30".equals(mode) && mLocalIP.len < 30)) {
            VpnStatus.logWarning(R.string.ip_looks_like_subnet, local, netmask, mode);
        }


        /* Workaround for Lollipop, it  does not route traffic to the VPNs own network mask */
        if (mLocalIP.len <= 31 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CIDRIP interfaceRoute = new CIDRIP(mLocalIP.mIp, mLocalIP.len);
            interfaceRoute.normalise();
            addRoute(interfaceRoute, true);
        }


        // Configurations are sometimes really broken...
        mRemoteGW = netmask;
    }

    public void setLocalIPv6(String ipv6addr) {
        mLocalIPv6 = ipv6addr;
    }

    @Override
    public void updateState(String state, String logmessage, int resid, ConnectionStatus level, Intent intent) {
        // If the process is not running, ignore any state,
        // Notification should be invisible in this state

        doSendBroadcast(state, level);
        if (mProcessThread == null && !mNotificationAlwaysVisible)
            return;

        String channel = NOTIFICATION_CHANNEL_NEWSTATUS_ID;
        // Display byte count only after being connected

        {
            if (level == LEVEL_CONNECTED) {
                mDisplayBytecount = true;
                mConnecttime = System.currentTimeMillis();
                if (!runningOnAndroidTV())
                    channel = NOTIFICATION_CHANNEL_BG_ID;
            } else {
                mDisplayBytecount = false;
            }

            // Other notifications are shown,
            // This also mean we are no longer connected, ignore bytecount messages until next
            // CONNECTED
            // Does not work :(
            String msg = getString(resid);
            showNotification(VpnStatus.getLastCleanLogMessage(this),
                    VpnStatus.getLastCleanLogMessage(this), channel, 0, level, intent);

        }
    }

    @Override
    public void setConnectedVPN(String uuid) {
    }

    private void doSendBroadcast(String state, ConnectionStatus level) {
        Intent vpnstatus = new Intent();
        vpnstatus.setAction("de.blinkt.openvpn.VPN_STATUS");
        vpnstatus.putExtra("status", level.toString());
        vpnstatus.putExtra("detailstatus", state);
        sendBroadcast(vpnstatus, permission.ACCESS_NETWORK_STATE);
        sendMessage(state);
    }

    long c = Calendar.getInstance().getTimeInMillis();
    long time;
    int lastPacketReceive = 0;
    String seconds = "0", minutes, hours;

    @Override
    public void updateByteCount(long in, long out, long diffIn, long diffOut) {
        TotalTraffic.calcTraffic(this, in, out, diffIn, diffOut);
        if (mDisplayBytecount) {
            String netstat = String.format(getString(R.string.statusline_bytecount),
                    humanReadableByteCount(in, false, getResources()),
                    humanReadableByteCount(diffIn / OpenVPNManagement.mBytecountInterval, true, getResources()),
                    humanReadableByteCount(out, false, getResources()),
                    humanReadableByteCount(diffOut / OpenVPNManagement.mBytecountInterval, true, getResources()));


            showNotification(netstat, null, NOTIFICATION_CHANNEL_BG_ID, mConnecttime, LEVEL_CONNECTED, null);
            byteIn = String.format("↓%2$s", getString(R.string.statusline_bytecount),
                    humanReadableByteCount(in,false, getResources())) + " - " + humanReadableByteCount(diffIn / OpenVPNManagement.mBytecountInterval, false, getResources()) + "/s";
            byteOut = String.format("↑%2$s", getString(R.string.statusline_bytecount),
                    humanReadableByteCount(out, false,getResources())) + " - " + humanReadableByteCount(diffOut / OpenVPNManagement.mBytecountInterval, false, getResources()) + "/s";
            time = Calendar.getInstance().getTimeInMillis() - c;
            lastPacketReceive = Integer.parseInt(convertTwoDigit((int) (time / 1000) % 60)) - Integer.parseInt(seconds);
            seconds = convertTwoDigit((int) (time / 1000) % 60);
            minutes = convertTwoDigit((int) ((time / (1000 * 60)) % 60));
            hours = convertTwoDigit((int) ((time / (1000 * 60 * 60)) % 24));
            duration = hours + ":" + minutes + ":" + seconds;
            lastPacketReceive = checkPacketReceive(lastPacketReceive);
            sendMessageToDouble(duration, String.valueOf(lastPacketReceive), humanReadableDoubleByteCount(diffIn / OpenVPNManagement.mBytecountInterval,false), humanReadableDoubleByteCount(diffOut / OpenVPNManagement.mBytecountInterval,false));
        }

    }

    private void sendMessageToDouble(String duration, String lastPacketReceive, double byteIn, double byteOut) {
        Intent intent = new Intent("connectionState");
        intent.putExtra("duration", duration);
        intent.putExtra("lastPacketReceive", lastPacketReceive);
        intent.putExtra("byteIn", byteIn);
        intent.putExtra("byteOut", byteOut);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    public int checkPacketReceive(int value) {
        value -= 2;
        if (value < 0) return 0;
        else return value;
    }
    public String convertTwoDigit(int value) {
        if (value < 10) return "0" + value;
        else return value + "";
    }

    @Override
    public boolean handleMessage(Message msg) {
        Runnable r = msg.getCallback();
        if (r != null) {
            r.run();
            return true;
        } else {
            return false;
        }
    }

    public OpenVPNManagement getManagement() {
        return mManagement;
    }

    public String getTunReopenStatus() {
        String currentConfiguration = getTunConfigString();
        if (currentConfiguration.equals(mLastTunCfg)) {
            return "NOACTION";
        } else {
            String release = Build.VERSION.RELEASE;
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !release.startsWith("4.4.3")
                    && !release.startsWith("4.4.4") && !release.startsWith("4.4.5") && !release.startsWith("4.4.6"))
                // There will be probably no 4.4.4 or 4.4.5 version, so don't waste effort to do parsing here
                return "OPEN_AFTER_CLOSE";
            else
                return "OPEN_BEFORE_CLOSE";
        }
    }

    public void requestInputFromUser(int resid, String needed) {
        VpnStatus.updateStateString("NEED", "need " + needed, resid, LEVEL_WAITING_FOR_USER_INPUT);
        showNotification(getString(resid), getString(resid), NOTIFICATION_CHANNEL_NEWSTATUS_ID, 0, LEVEL_WAITING_FOR_USER_INPUT, null);
    }


    public void trigger_sso(String info) {
        String channel = NOTIFICATION_CHANNEL_USERREQ_ID;
        String method = info.split(":", 2)[0];

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder nbuilder = new NotificationCompat.Builder(this, channel);
        nbuilder.setAutoCancel(true);
        int icon = android.R.drawable.ic_dialog_info;
        nbuilder.setSmallIcon(icon);
        nbuilder.setPriority(NotificationCompat.PRIORITY_LOW);
        nbuilder.setSilent(true);

        Intent intent;

        int reason;
        if (method.equals("CR_TEXT")) {
            String challenge = info.split(":", 2)[1];
            reason = R.string.crtext_requested;
            nbuilder.setContentTitle(getString(reason));
            nbuilder.setContentText(challenge);

            intent = new Intent();
            intent.setComponent(new ComponentName(this, getPackageName() + ".activities.CredentialsPopup"));

            intent.putExtra(EXTRA_CHALLENGE_TXT, challenge);

        } else {
            VpnStatus.logError("Unknown SSO method found: " + method);
            return;
        }

        // updateStateString trigger the notification of the VPN to be refreshed, save this intent
        // to have that notification also this intent to be set
        PendingIntent pIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        }
        VpnStatus.updateStateString("USER_INPUT", "waiting for user input", reason, LEVEL_WAITING_FOR_USER_INPUT, intent);
        nbuilder.setContentIntent(pIntent);


        // Try to set the priority available since API 16 (Jellybean)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            jbNotificationExtras(PRIORITY_MAX, nbuilder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            lpNotificationExtras(nbuilder, Notification.CATEGORY_STATUS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //noinspection NewApi
            nbuilder.setChannelId(channel);
        }

        @SuppressWarnings("deprecation")
        Notification notification = nbuilder.getNotification();


        int notificationId = channel.hashCode();

        mNotificationManager.notify(notificationId, notification);
    }

    //sending message to main activity
    private void sendMessage(String state) {
        Intent intent = new Intent("connectionState");
        intent.putExtra("state", state);
        this.state = state;
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    //sending message to main activity
    private void sendMessage(String duration, String lastPacketReceive, String byteIn, String byteOut) {
        Intent intent = new Intent("connectionState");
        intent.putExtra("duration", duration);
        intent.putExtra("lastPacketReceive", lastPacketReceive);
        intent.putExtra("byteIn", byteIn);
        intent.putExtra("byteOut", byteOut);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    public class LocalBinder extends Binder {
        public OpenVPNService getService() {
            // Return this instance of LocalService so clients can call public methods
            return OpenVPNService.this;
        }
    }
    public static String getStatus() {//it will be call from mainactivity for get current status
        return state;
    }
    public static void setDefaultStatus() {
        state = "";
    }
    public boolean isConnected() {
        return flag;
    }
}


/*
 * Copyright (c) 2012-2018 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.Context;
import android.net.*;
import android.os.Build;
import android.text.TextUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Vector;

public class NetworkUtils {

    public static Vector<String> getLocalNetworks(Context c, boolean ipv6) {
        Vector<String> nets = new Vector<>();
        ConnectivityManager conn = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = conn.getAllNetworks();
            for (Network network : networks) {
                NetworkInfo ni = conn.getNetworkInfo(network);
                LinkProperties li = conn.getLinkProperties(network);

                NetworkCapabilities nc = conn.getNetworkCapabilities(network);

                // Skip VPN networks like ourselves
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                    continue;

                // Also skip mobile networks
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    continue;


                for (LinkAddress la : li.getLinkAddresses()) {
                    if ((la.getAddress() instanceof Inet4Address && !ipv6) ||
                            (la.getAddress() instanceof Inet6Address && ipv6))
                        nets.add(la.toString());
                }
            }
        } else {
            // Old Android Version, use native utils via ifconfig instead
            // Add local network interfaces
            if (ipv6)
                return nets;

            String[] localRoutes = NativeUtils.getIfconfig();

            // The format of mLocalRoutes is kind of broken because I don't really like JNI
            for (int i = 0; i < localRoutes.length; i += 3) {
                String intf = localRoutes[i];
                String ipAddr = localRoutes[i + 1];
                String netMask = localRoutes[i + 2];

                if (intf == null || intf.equals("lo") ||
                        intf.startsWith("tun") || intf.startsWith("rmnet"))
                    continue;

                if (ipAddr == null || netMask == null) {
                    VpnStatus.logError("Local routes are broken?! (Report to author) " + TextUtils.join("|", localRoutes));
                    continue;
                }
                nets.add(ipAddr + "/" + CIDRIP.calculateLenFromMask(netMask));

            }

        }
        return nets;
    }

}

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.StrictMode;

import de.blinkt.openvpn.BuildConfig;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.api.AppRestrictions;

public class ICSOpenVPNApplication extends Application {
    private StatusListener mStatus;

    @Override
    public void onCreate() {
        if("robolectric".equals(Build.FINGERPRINT))
            return;

        super.onCreate();
        PRNGFixes.apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannels();
        mStatus = new StatusListener();
        mStatus.init(getApplicationContext());

        if (BuildConfig.BUILD_TYPE.equals("debug"))
            enableStrictModes();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppRestrictions.getInstance(this).checkRestrictions(this);
        }
    }

    private void enableStrictModes() {
        StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build();
        StrictMode.setVmPolicy(policy);

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannels() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Background message
        CharSequence name = getString(R.string.channel_name_background);
        NotificationChannel mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_BG_ID,
                name, NotificationManager.IMPORTANCE_MIN);

        mChannel.setDescription(getString(R.string.channel_description_background));
        mChannel.enableLights(false);

        mChannel.setLightColor(Color.DKGRAY);
        mNotificationManager.createNotificationChannel(mChannel);

        // Connection status change messages

        name = getString(R.string.channel_name_status);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_NEWSTATUS_ID,
                name, NotificationManager.IMPORTANCE_LOW);

        mChannel.setDescription(getString(R.string.channel_description_status));
        mChannel.enableLights(true);

        mChannel.setLightColor(Color.BLUE);
        mNotificationManager.createNotificationChannel(mChannel);


        // Urgent requests, e.g. two factor auth
        name = getString(R.string.channel_name_userreq);
        mChannel = new NotificationChannel(OpenVPNService.NOTIFICATION_CHANNEL_USERREQ_ID,
                name, NotificationManager.IMPORTANCE_HIGH);
        mChannel.setDescription(getString(R.string.channel_description_userreq));
        mChannel.enableVibration(true);
        mChannel.setLightColor(Color.CYAN);
        mNotificationManager.createNotificationChannel(mChannel);
    }
}


/*
 * Copyright (c) 2012-2017 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Vector;

import static java.lang.Math.max;

/**
 * Created by arne on 23.05.17.
 */

public class TrafficHistory implements Parcelable {

    public static final long PERIODS_TO_KEEP = 5;
    public static final int TIME_PERIOD_MINTUES = 60 * 1000;
    public static final int TIME_PERIOD_HOURS = 3600 * 1000;
    private LinkedList<TrafficDatapoint> trafficHistorySeconds = new LinkedList<>();
    private LinkedList<TrafficDatapoint> trafficHistoryMinutes = new LinkedList<>();
    private LinkedList<TrafficDatapoint> trafficHistoryHours = new LinkedList<>();

    private TrafficDatapoint lastSecondUsedForMinute;
    private TrafficDatapoint lastMinuteUsedForHours;

    public TrafficHistory() {

    }

    protected TrafficHistory(Parcel in) {
        in.readList(trafficHistorySeconds, getClass().getClassLoader());
        in.readList(trafficHistoryMinutes, getClass().getClassLoader());
        in.readList(trafficHistoryHours, getClass().getClassLoader());
        lastSecondUsedForMinute = in.readParcelable(getClass().getClassLoader());
        lastMinuteUsedForHours = in.readParcelable(getClass().getClassLoader());
    }

    public static final Creator<TrafficHistory> CREATOR = new Creator<TrafficHistory>() {
        @Override
        public TrafficHistory createFromParcel(Parcel in) {
            return new TrafficHistory(in);
        }

        @Override
        public TrafficHistory[] newArray(int size) {
            return new TrafficHistory[size];
        }
    };

    public LastDiff getLastDiff(TrafficDatapoint tdp) {

        TrafficDatapoint lasttdp;


        if (trafficHistorySeconds.size() == 0)
            lasttdp = new TrafficDatapoint(0, 0, System.currentTimeMillis());

        else
            lasttdp = trafficHistorySeconds.getLast();

        if (tdp == null) {
            tdp = lasttdp;
            if (trafficHistorySeconds.size() < 2)
                lasttdp = tdp;
            else {
                trafficHistorySeconds.descendingIterator().next();
                tdp = trafficHistorySeconds.descendingIterator().next();
            }
        }

        return new LastDiff(lasttdp, tdp);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(trafficHistorySeconds);
        dest.writeList(trafficHistoryMinutes);
        dest.writeList(trafficHistoryHours);
        dest.writeParcelable(lastSecondUsedForMinute, 0);
        dest.writeParcelable(lastMinuteUsedForHours, 0);

    }

    public LinkedList<TrafficDatapoint> getHours() {
        return trafficHistoryHours;
    }

    public LinkedList<TrafficDatapoint> getMinutes() {
        return trafficHistoryMinutes;
    }

    public LinkedList<TrafficDatapoint> getSeconds() {
        return trafficHistorySeconds;
    }

    public static LinkedList<TrafficDatapoint> getDummyList() {
        LinkedList<TrafficDatapoint> list = new LinkedList<>();
        list.add(new TrafficDatapoint(0, 0, System.currentTimeMillis()));
        return list;
    }


    public static class TrafficDatapoint implements Parcelable {
        private TrafficDatapoint(long inBytes, long outBytes, long timestamp) {
            this.in = inBytes;
            this.out = outBytes;
            this.timestamp = timestamp;
        }

        public final long timestamp;
        public final long in;
        public final long out;

        private TrafficDatapoint(Parcel in) {
            timestamp = in.readLong();
            this.in = in.readLong();
            out = in.readLong();
        }

        public static final Creator<TrafficDatapoint> CREATOR = new Creator<TrafficDatapoint>() {
            @Override
            public TrafficDatapoint createFromParcel(Parcel in) {
                return new TrafficDatapoint(in);
            }

            @Override
            public TrafficDatapoint[] newArray(int size) {
                return new TrafficDatapoint[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(timestamp);
            dest.writeLong(in);
            dest.writeLong(out);
        }
    }

    LastDiff add(long in, long out) {
        TrafficDatapoint tdp = new TrafficDatapoint(in, out, System.currentTimeMillis());

        LastDiff diff = getLastDiff(tdp);
        addDataPoint(tdp);
        return diff;
    }

    private void addDataPoint(TrafficDatapoint tdp) {
        trafficHistorySeconds.add(tdp);

        if (lastSecondUsedForMinute == null) {
            lastSecondUsedForMinute = new TrafficDatapoint(0, 0, 0);
            lastMinuteUsedForHours = new TrafficDatapoint(0, 0, 0);
        }

        removeAndAverage(tdp, true);
    }

    private void removeAndAverage(TrafficDatapoint newTdp, boolean seconds) {
        HashSet<TrafficDatapoint> toRemove = new HashSet<>();
        Vector<TrafficDatapoint> toAverage = new Vector<>();

        long timePeriod;
        LinkedList<TrafficDatapoint> tpList, nextList;
        TrafficDatapoint lastTsPeriod;

        if (seconds) {
            timePeriod = TIME_PERIOD_MINTUES;
            tpList = trafficHistorySeconds;
            nextList = trafficHistoryMinutes;
            lastTsPeriod = lastSecondUsedForMinute;
        } else {
            timePeriod = TIME_PERIOD_HOURS;
            tpList = trafficHistoryMinutes;
            nextList = trafficHistoryHours;
            lastTsPeriod = lastMinuteUsedForHours;
        }

        if (newTdp.timestamp / timePeriod > (lastTsPeriod.timestamp / timePeriod)) {
            nextList.add(newTdp);

            if (seconds) {
                lastSecondUsedForMinute = newTdp;
                removeAndAverage(newTdp, false);
            } else
                lastMinuteUsedForHours = newTdp;

            for (TrafficDatapoint tph : tpList) {
                // List is iteratered from oldest to newest, remembert first one that we did not
                if ((newTdp.timestamp - tph.timestamp) / timePeriod >= PERIODS_TO_KEEP)
                    toRemove.add(tph);
            }
            tpList.removeAll(toRemove);
        }
    }

    static class LastDiff {

        final private TrafficDatapoint tdp;
        final private TrafficDatapoint lasttdp;

        private LastDiff(TrafficDatapoint lasttdp, TrafficDatapoint tdp) {
            this.lasttdp = lasttdp;
            this.tdp = tdp;
        }

        public long getDiffOut() {
            return max(0, tdp.out - lasttdp.out);
        }

        public long getDiffIn() {
            return max(0, tdp.in - lasttdp.in);
        }

        public long getIn() {
            return tdp.in;
        }

        public long getOut() {
            return tdp.out;
        }

    }


}

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by arne on 08.11.16.
 */
public enum ConnectionStatus implements Parcelable {
    LEVEL_CONNECTED,
    LEVEL_VPNPAUSED,
    LEVEL_CONNECTING_SERVER_REPLIED,
    LEVEL_CONNECTING_NO_SERVER_REPLY_YET,
    LEVEL_NONETWORK,
    LEVEL_NOTCONNECTED,
    LEVEL_START,
    LEVEL_AUTH_FAILED,
    LEVEL_WAITING_FOR_USER_INPUT,
    UNKNOWN_LEVEL;

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ConnectionStatus> CREATOR = new Creator<ConnectionStatus>() {
        @Override
        public ConnectionStatus createFromParcel(Parcel in) {
            return ConnectionStatus.values()[in.readInt()];
        }

        @Override
        public ConnectionStatus[] newArray(int size) {
            return new ConnectionStatus[size];
        }
    };
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Locale;
import java.util.UnknownFormatConversionException;

import de.blinkt.openvpn.R;

/**
 * Created by arne on 24.04.16.
 */
public class LogItem implements Parcelable {
    private Object[] mArgs = null;
    private String mMessage = null;
    private int mRessourceId;
    // Default log priority
    VpnStatus.LogLevel mLevel = VpnStatus.LogLevel.INFO;
    private long logtime = System.currentTimeMillis();
    private int mVerbosityLevel = -1;

    private LogItem(int ressourceId, Object[] args) {
        mRessourceId = ressourceId;
        mArgs = args;
    }

    public LogItem(VpnStatus.LogLevel level, int verblevel, String message) {
        mMessage = message;
        mLevel = level;
        mVerbosityLevel = verblevel;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(mArgs);
        dest.writeString(mMessage);
        dest.writeInt(mRessourceId);
        dest.writeInt(mLevel.getInt());
        dest.writeInt(mVerbosityLevel);

        dest.writeLong(logtime);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LogItem))
            return obj.equals(this);
        LogItem other = (LogItem) obj;

        return Arrays.equals(mArgs, other.mArgs) &&
                ((other.mMessage == null && mMessage == other.mMessage) ||
                        mMessage.equals(other.mMessage)) &&
                mRessourceId == other.mRessourceId &&
                ((mLevel == null && other.mLevel == mLevel) ||
                        other.mLevel.equals(mLevel)) &&
                mVerbosityLevel == other.mVerbosityLevel &&
                logtime == other.logtime;


    }

    public byte[] getMarschaledBytes() throws UnsupportedEncodingException, BufferOverflowException {
        ByteBuffer bb = ByteBuffer.allocate(16384);


        bb.put((byte) 0x0);               //version
        bb.putLong(logtime);              //8
        bb.putInt(mVerbosityLevel);      //4
        bb.putInt(mLevel.getInt());
        bb.putInt(mRessourceId);
        if (mMessage == null || mMessage.length() == 0) {
            bb.putInt(0);
        } else {
            marschalString(mMessage, bb);
        }
        if (mArgs == null || mArgs.length == 0) {
            bb.putInt(0);
        } else {
            bb.putInt(mArgs.length);
            for (Object o : mArgs) {
                if (o instanceof String) {
                    bb.putChar('s');
                    marschalString((String) o, bb);
                } else if (o instanceof Integer) {
                    bb.putChar('i');
                    bb.putInt((Integer) o);
                } else if (o instanceof Float) {
                    bb.putChar('f');
                    bb.putFloat((Float) o);
                } else if (o instanceof Double) {
                    bb.putChar('d');
                    bb.putDouble((Double) o);
                } else if (o instanceof Long) {
                    bb.putChar('l');
                    bb.putLong((Long) o);
                } else if (o == null) {
                    bb.putChar('0');
                } else {
                    VpnStatus.logDebug("Unknown object for LogItem marschaling " + o);
                    bb.putChar('s');
                    marschalString(o.toString(), bb);
                }

            }
        }

        int pos = bb.position();
        bb.rewind();
        return Arrays.copyOf(bb.array(), pos);

    }

    public LogItem(byte[] in, int length) throws UnsupportedEncodingException {
        ByteBuffer bb = ByteBuffer.wrap(in, 0, length);
        bb.get(); // ignore version
        logtime = bb.getLong();
        mVerbosityLevel = bb.getInt();
        mLevel = VpnStatus.LogLevel.getEnumByValue(bb.getInt());
        mRessourceId = bb.getInt();
        int len = bb.getInt();
        if (len == 0) {
            mMessage = null;
        } else {
            if (len > bb.remaining())
                throw new IndexOutOfBoundsException("String length " + len + " is bigger than remaining bytes " + bb.remaining());
            byte[] utf8bytes = new byte[len];
            bb.get(utf8bytes);
            mMessage = new String(utf8bytes, "UTF-8");
        }
        int numArgs = bb.getInt();
        if (numArgs > 30) {
            throw new IndexOutOfBoundsException("Too many arguments for Logitem to unmarschal");
        }
        if (numArgs == 0) {
            mArgs = null;
        } else {
            mArgs = new Object[numArgs];
            for (int i = 0; i < numArgs; i++) {
                char type = bb.getChar();
                switch (type) {
                    case 's':
                        mArgs[i] = unmarschalString(bb);
                        break;
                    case 'i':
                        mArgs[i] = bb.getInt();
                        break;
                    case 'd':
                        mArgs[i] = bb.getDouble();
                        break;
                    case 'f':
                        mArgs[i] = bb.getFloat();
                        break;
                    case 'l':
                        mArgs[i] = bb.getLong();
                        break;
                    case '0':
                        mArgs[i] = null;
                        break;
                    default:
                        throw new UnsupportedEncodingException("Unknown format type: " + type);
                }
            }
        }
        if (bb.hasRemaining())
            throw new UnsupportedEncodingException(bb.remaining() + " bytes left after unmarshaling everything");
    }

    private void marschalString(String str, ByteBuffer bb) throws UnsupportedEncodingException {
        byte[] utf8bytes = str.getBytes("UTF-8");
        bb.putInt(utf8bytes.length);
        bb.put(utf8bytes);
    }

    private String unmarschalString(ByteBuffer bb) throws UnsupportedEncodingException {
        int len = bb.getInt();
        byte[] utf8bytes = new byte[len];
        bb.get(utf8bytes);
        return new String(utf8bytes, "UTF-8");
    }


    public LogItem(Parcel in) {
        mArgs = in.readArray(Object.class.getClassLoader());
        mMessage = in.readString();
        mRessourceId = in.readInt();
        mLevel = VpnStatus.LogLevel.getEnumByValue(in.readInt());
        mVerbosityLevel = in.readInt();
        logtime = in.readLong();
    }

    public static final Creator<LogItem> CREATOR
            = new Creator<LogItem>() {
        public LogItem createFromParcel(Parcel in) {
            return new LogItem(in);
        }

        public LogItem[] newArray(int size) {
            return new LogItem[size];
        }
    };

    public LogItem(VpnStatus.LogLevel loglevel, int ressourceId, Object... args) {
        mRessourceId = ressourceId;
        mArgs = args;
        mLevel = loglevel;
    }


    public LogItem(VpnStatus.LogLevel loglevel, String msg) {
        mLevel = loglevel;
        mMessage = msg;
    }


    public LogItem(VpnStatus.LogLevel loglevel, int ressourceId) {
        mRessourceId = ressourceId;
        mLevel = loglevel;
    }

    public String getString(Context c) {
        try {
            if (mMessage != null) {
                return mMessage;
            } else {
                if (c != null) {
                    if (mRessourceId == R.string.mobile_info)
                        return getMobileInfoString(c);
                    if (mArgs == null)
                        return c.getString(mRessourceId);
                    else
                        return c.getString(mRessourceId, mArgs);
                } else {
                    String str = String.format(Locale.ENGLISH, "Log (no context) resid %d", mRessourceId);
                    if (mArgs != null)
                        str += join("|", mArgs);

                    return str;
                }
            }
        } catch (UnknownFormatConversionException e) {
            if (c != null)
                throw new UnknownFormatConversionException(e.getLocalizedMessage() + getString(null));
            else
                throw e;
        } catch (java.util.FormatFlagsConversionMismatchException e) {
            if (c != null)
                throw new FormatFlagsConversionMismatchException(e.getLocalizedMessage() + getString(null), e.getConversion());
            else
                throw e;
        }

    }


    // TextUtils.join will cause not macked exeception in tests ....
    public static String join(CharSequence delimiter, Object[] tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }


    public VpnStatus.LogLevel getLogLevel() {
        return mLevel;
    }


    @Override
    public String toString() {
        return getString(null);
    }

    // The lint is wrong here
    @SuppressLint("StringFormatMatches")
    private String getMobileInfoString(Context c) {
        c.getPackageManager();
        String apksign = "error getting package signature";

        String version = "error getting version";
        try {
            @SuppressLint("PackageManagerGetSignatures")
            Signature raw = c.getPackageManager().getPackageInfo(c.getPackageName(), PackageManager.GET_SIGNATURES).signatures[0];
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(raw.toByteArray()));
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] der = cert.getEncoded();
            md.update(der);
            byte[] digest = md.digest();

            if (Arrays.equals(digest, VpnStatus.officalkey))
                apksign = c.getString(R.string.official_build);
            else if (Arrays.equals(digest, VpnStatus.officaldebugkey))
                apksign = c.getString(R.string.debug_build);
            else if (Arrays.equals(digest, VpnStatus.amazonkey))
                apksign = "amazon version";
            else if (Arrays.equals(digest, VpnStatus.fdroidkey))
                apksign = "F-Droid built and signed version";
            else
                apksign = c.getString(R.string.built_by, cert.getSubjectX500Principal().getName());

            PackageInfo packageinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = packageinfo.versionName;

        } catch (PackageManager.NameNotFoundException | CertificateException |
                NoSuchAlgorithmException ignored) {
        }

        Object[] argsext = Arrays.copyOf(mArgs, mArgs.length);
        argsext[argsext.length - 1] = apksign;
        argsext[argsext.length - 2] = version;

        return c.getString(R.string.mobile_info, argsext);

    }

    public long getLogtime() {
        return logtime;
    }


    public int getVerbosityLevel() {
        if (mVerbosityLevel == -1) {
            // Hack:
            // For message not from OpenVPN, report the status level as log level
            return mLevel.getInt();
        }
        return mVerbosityLevel;
    }

    public boolean verify() {
        if (mLevel == null)
            return false;

        if (mMessage == null && mRessourceId == 0)
            return false;

        return true;
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import java.util.Locale;

class CIDRIP {
    String mIp;
    int len;


    public CIDRIP(String ip, String mask) {
        mIp = ip;
        len = calculateLenFromMask(mask);

    }

    public static int calculateLenFromMask(String mask) {
        long netmask = getInt(mask);

        // Add 33. bit to ensure the loop terminates
        netmask += 1l << 32;

        int lenZeros = 0;
        while ((netmask & 0x1) == 0) {
            lenZeros++;
            netmask = netmask >> 1;
        }
        int len;
        // Check if rest of netmask is only 1s
        if (netmask != (0x1ffffffffl >> lenZeros)) {
            // Asume no CIDR, set /32
            len = 32;
        } else {
            len = 32 - lenZeros;
        }
        return len;
    }

    public CIDRIP(String address, int prefix_length) {
        len = prefix_length;
        mIp = address;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "%s/%d", mIp, len);
    }

    public boolean normalise() {
        long ip = getInt(mIp);

        long newip = ip & (0xffffffffL << (32 - len));
        if (newip != ip) {
            mIp = String.format(Locale.US,"%d.%d.%d.%d", (newip & 0xff000000) >> 24, (newip & 0xff0000) >> 16, (newip & 0xff00) >> 8, newip & 0xff);
            return true;
        } else {
            return false;
        }

    }

    static long getInt(String ipaddr) {
        String[] ipt = ipaddr.split("\\.");
        long ip = 0;

        ip += Long.parseLong(ipt[0]) << 24;
        ip += Integer.parseInt(ipt[1]) << 16;
        ip += Integer.parseInt(ipt[2]) << 8;
        ip += Integer.parseInt(ipt[3]);

        return ip;
    }

    public long getInt() {
        return getInt(mIp);
    }

}

/*
 * Copyright (c) 2012-2015 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Locale;

import de.blinkt.openvpn.R;

/**
 * Created by arne on 23.01.16.
 */
class LogFileHandler extends Handler {
    static final int TRIM_LOG_FILE = 100;
    static final int FLUSH_TO_DISK = 101;
    static final int LOG_INIT = 102;
    public static final int LOG_MESSAGE = 103;
    public static final int MAGIC_BYTE = 0x55;
    protected OutputStream mLogFile;

    public static final String LOGFILE_NAME = "logcache.dat";


    public LogFileHandler(Looper looper) {
        super(looper);
    }


    @Override
    public void handleMessage(Message msg) {
        try {
            if (msg.what == LOG_INIT) {
                if (mLogFile != null)
                    throw new RuntimeException("mLogFile not null");
                readLogCache((File) msg.obj);
                openLogFile((File) msg.obj);
            } else if (msg.what == LOG_MESSAGE && msg.obj instanceof LogItem) {
                // Ignore log messages if not yet initialized
                if (mLogFile == null)
                    return;
                writeLogItemToDisk((LogItem) msg.obj);
            } else if (msg.what == TRIM_LOG_FILE) {
                trimLogFile();
                for (LogItem li : VpnStatus.getlogbuffer())
                    writeLogItemToDisk(li);
            } else if (msg.what == FLUSH_TO_DISK) {
                flushToDisk();
            }

        } catch (IOException | BufferOverflowException e) {
            e.printStackTrace();
            VpnStatus.logError("Error during log cache: " + msg.what);
            VpnStatus.logException(e);
        }

    }

    private void flushToDisk() throws IOException {
        mLogFile.flush();
    }

    private void trimLogFile() {
        try {
            mLogFile.flush();
            ((FileOutputStream) mLogFile).getChannel().truncate(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLogItemToDisk(LogItem li) throws IOException {

        // We do not really care if the log cache breaks between Android upgrades,
        // write binary format to disc

        byte[] liBytes = li.getMarschaledBytes();

        writeEscapedBytes(liBytes);
    }

    public void writeEscapedBytes(byte[] bytes) throws IOException {
        int magic = 0;
        for (byte b : bytes)
            if (b == MAGIC_BYTE || b == MAGIC_BYTE + 1)
                magic++;

        byte eBytes[] = new byte[bytes.length + magic];

        int i = 0;
        for (byte b : bytes) {
            if (b == MAGIC_BYTE || b == MAGIC_BYTE + 1) {
                eBytes[i++] = MAGIC_BYTE + 1;
                eBytes[i++] = (byte) (b - MAGIC_BYTE);
            } else {
                eBytes[i++] = b;
            }
        }

        byte[] lenBytes = ByteBuffer.allocate(4).putInt(bytes.length).array();
        synchronized (mLogFile) {
            mLogFile.write(MAGIC_BYTE);
            mLogFile.write(lenBytes);
            mLogFile.write(eBytes);
        }
    }

    private void openLogFile(File cacheDir) throws FileNotFoundException {
        File logfile = new File(cacheDir, LOGFILE_NAME);
        mLogFile = new FileOutputStream(logfile);
    }

    private void readLogCache(File cacheDir) {
        try {
            File logfile = new File(cacheDir, LOGFILE_NAME);


            if (!logfile.exists() || !logfile.canRead())
                return;

            FileInputStream log = new FileInputStream(logfile);
            readCacheContents(log);
            log.close();

        } catch (java.io.IOException | java.lang.RuntimeException e) {
            VpnStatus.logError("Reading cached logfile failed");
            VpnStatus.logException(e);
            e.printStackTrace();
            // ignore reading file error
        } finally {
            synchronized (VpnStatus.readFileLock) {
                VpnStatus.readFileLog = true;
                VpnStatus.readFileLock.notifyAll();
            }
        }
    }


    protected void readCacheContents(InputStream in) throws IOException {
        BufferedInputStream logFile = new BufferedInputStream(in);

        byte[] buf = new byte[16384];
        int read = logFile.read(buf, 0, 5);
        int itemsRead = 0;


        readloop:
        while (read >= 5) {
            int skipped = 0;
            while (buf[skipped] != MAGIC_BYTE) {
                skipped++;
                if (!(logFile.read(buf, skipped + 4, 1) == 1) || skipped + 10 > buf.length) {
                    VpnStatus.logDebug(String.format(Locale.US, "Skipped %d bytes and no a magic byte found", skipped));
                    break readloop;
                }
            }
            if (skipped > 0)
                VpnStatus.logDebug(String.format(Locale.US, "Skipped %d bytes before finding a magic byte", skipped));

            int len = ByteBuffer.wrap(buf, skipped + 1, 4).asIntBuffer().get();

            // Marshalled LogItem
            int pos = 0;
            byte buf2[] = new byte[buf.length];

            while (pos < len) {
                byte b = (byte) logFile.read();
                if (b == MAGIC_BYTE) {
                    VpnStatus.logDebug(String.format(Locale.US, "Unexpected magic byte found at pos %d, abort current log item", pos));
                    read = logFile.read(buf, 1, 4) + 1;
                    continue readloop;
                } else if (b == MAGIC_BYTE + 1) {
                    b = (byte) logFile.read();
                    if (b == 0)
                        b = MAGIC_BYTE;
                    else if (b == 1)
                        b = MAGIC_BYTE + 1;
                    else {
                        VpnStatus.logDebug(String.format(Locale.US, "Escaped byte not 0 or 1: %d", b));
                        read = logFile.read(buf, 1, 4) + 1;
                        continue readloop;
                    }
                }
                buf2[pos++] = b;
            }

            restoreLogItem(buf2, len);

            //Next item
            read = logFile.read(buf, 0, 5);
            itemsRead++;
            if (itemsRead > 2 * VpnStatus.MAXLOGENTRIES) {
                VpnStatus.logError("Too many logentries read from cache, aborting.");
                read = 0;
            }

        }
        VpnStatus.logDebug(R.string.reread_log, itemsRead);
    }

    protected void restoreLogItem(byte[] buf, int len) throws UnsupportedEncodingException {

        LogItem li = new LogItem(buf, len);
        if (li.verify()) {
            VpnStatus.newLogItem(li, true);
        } else {
            VpnStatus.logError(String.format(Locale.getDefault(),
                    "Could not read log item from file: %d: %s",
                    len, bytesToHex(buf, Math.max(len, 80))));
        }
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes, int len) {
        len = Math.min(bytes.length, len);
        char[] hexChars = new char[len * 2];
        for (int j = 0; j < len; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;

public class ProxyDetection {
	static SocketAddress detectProxy(VpnProfile vp) {
		// Construct a new url with https as protocol
		try {
			URL url = new URL(String.format("https://%s:%s",vp.mServerName,vp.mServerPort));
			Proxy proxy = getFirstProxy(url);

			if(proxy==null)
				return null;
			SocketAddress addr = proxy.address();
			if (addr instanceof InetSocketAddress) {
				return addr; 
			}
			
		} catch (MalformedURLException e) {
			VpnStatus.logError(R.string.getproxy_error, e.getLocalizedMessage());
		} catch (URISyntaxException e) {
			VpnStatus.logError(R.string.getproxy_error, e.getLocalizedMessage());
		}
		return null;
	}

	static Proxy getFirstProxy(URL url) throws URISyntaxException {
		System.setProperty("java.net.useSystemProxies", "true");

		List<Proxy> proxylist = ProxySelector.getDefault().select(url.toURI());


		if (proxylist != null) {
			for (Proxy proxy: proxylist) {
				SocketAddress addr = proxy.address();

				if (addr != null) {
					return proxy;
				}
			}

		}
		return null;
	}
}

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;

import de.blinkt.openvpn.BuildConfig;

/**
 * Created by arne on 09.11.16.
 */

public class StatusListener implements VpnStatus.LogListener {
    private File mCacheDir;
    private Context mContext;
    private IStatusCallbacks mCallback = new IStatusCallbacks.Stub() {
        @Override
        public void newLogItem(LogItem item) throws RemoteException {
            VpnStatus.newLogItem(item);
        }

        @Override
        public void updateStateString(String state, String msg, int resid, ConnectionStatus
                level, Intent intent) throws RemoteException {
            VpnStatus.updateStateString(state, msg, resid, level, intent);
        }

        @Override
        public void updateByteCount(long inBytes, long outBytes) throws RemoteException {
            VpnStatus.updateByteCount(inBytes, outBytes);
        }

        @Override
        public void connectedVPN(String uuid) throws RemoteException {
            VpnStatus.setConnectedVPNProfile(uuid);
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            IServiceStatus serviceStatus = IServiceStatus.Stub.asInterface(service);
            try {
                /* Check if this a local service ... */
                if (service.queryLocalInterface("de.blinkt.openvpn.core.IServiceStatus") == null) {
                    // Not a local service
                    VpnStatus.setConnectedVPNProfile(serviceStatus.getLastConnectedVPN());
                    VpnStatus.setTrafficHistory(serviceStatus.getTrafficHistory());
                    ParcelFileDescriptor pfd = serviceStatus.registerStatusCallback(mCallback);
                    DataInputStream fd = new DataInputStream(new ParcelFileDescriptor.AutoCloseInputStream(pfd));

                    short len = fd.readShort();
                    byte[] buf = new byte[65336];
                    while (len != 0x7fff) {
                        fd.readFully(buf, 0, len);
                        LogItem logitem = new LogItem(buf, len);
                        VpnStatus.newLogItem(logitem, false);
                        len = fd.readShort();
                    }
                    fd.close();


                } else {
                    VpnStatus.initLogCache(mCacheDir);
                    /* Set up logging to Logcat with a context) */

                    if (BuildConfig.DEBUG) {
                        VpnStatus.addLogListener(StatusListener.this);
                    }


                }

            } catch (RemoteException | IOException e) {
                e.printStackTrace();
                VpnStatus.logException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            VpnStatus.removeLogListener(StatusListener.this);
        }

    };

    void init(Context c) {

        Intent intent = new Intent(c, OpenVPNStatusService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        mCacheDir = c.getCacheDir();

        c.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        this.mContext = c;

    }

    @Override
    public void newLog(LogItem logItem) {
        switch (logItem.getLogLevel()) {
            case INFO:
                Log.i("OpenVPN", logItem.getString(mContext));
                break;
            case DEBUG:
                Log.d("OpenVPN", logItem.getString(mContext));
                break;
            case ERROR:
                Log.e("OpenVPN", logItem.getString(mContext));
                break;
            case VERBOSE:
                Log.v("OpenVPN", logItem.getString(mContext));
                break;
            case WARNING:
            default:
                Log.w("OpenVPN", logItem.getString(mContext));
                break;
        }

    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;

public class VPNLaunchHelper {
    private static final String MININONPIEVPN = "nopie_openvpn";
    private static final String MINIPIEVPN = "pie_openvpn";
    private static final String OVPNCONFIGFILE = "android.conf";


    private static String writeMiniVPN(Context context) {
        String nativeAPI = NativeUtils.getNativeAPI();
        /* Q does not allow executing binaries written in temp directory anymore */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return new File(context.getApplicationInfo().nativeLibraryDir, "libovpnexec.so").getPath();
        String[] abis;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            abis = getSupportedABIsLollipop();
        else
            //noinspection deprecation
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};

        if (!nativeAPI.equals(abis[0])) {
            VpnStatus.logWarning(R.string.abi_mismatch, Arrays.toString(abis), nativeAPI);
            abis = new String[]{nativeAPI};
        }

        for (String abi : abis) {

            File vpnExecutable = new File(context.getCacheDir(), "c_" + getMiniVPNExecutableName() + "." + abi);
            if ((vpnExecutable.exists() && vpnExecutable.canExecute()) || writeMiniVPNBinary(context, abi, vpnExecutable)) {
                return vpnExecutable.getPath();
            }
        }

        throw new RuntimeException("Cannot find any execulte for this device's ABIs " + abis.toString());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String[] getSupportedABIsLollipop() {
        return Build.SUPPORTED_ABIS;
    }

    private static String getMiniVPNExecutableName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return MINIPIEVPN;
        else
            return MININONPIEVPN;
    }


    public static String[] replacePieWithNoPie(String[] mArgv) {
        mArgv[0] = mArgv[0].replace(MINIPIEVPN, MININONPIEVPN);
        return mArgv;
    }


    static String[] buildOpenvpnArgv(Context c) {
        Vector<String> args = new Vector<>();

        String binaryName = writeMiniVPN(c);
        // Add fixed paramenters
        //args.add("/data/data/de.blinkt.openvpn/lib/openvpn");
        if (binaryName == null) {
            VpnStatus.logError("Error writing minivpn binary");
            return null;
        }

        args.add(binaryName);

        args.add("--config");
        args.add(getConfigFilePath(c));

        return args.toArray(new String[args.size()]);
    }

    private static boolean writeMiniVPNBinary(Context context, String abi, File mvpnout) {
        try {
            InputStream mvpn;

            try {
                mvpn = context.getAssets().open(getMiniVPNExecutableName() + "." + abi);
            } catch (IOException errabi) {
                VpnStatus.logInfo("Failed getting assets for archicture " + abi);
                return false;
            }


            FileOutputStream fout = new FileOutputStream(mvpnout);

            byte buf[] = new byte[4096];

            int lenread = mvpn.read(buf);
            while (lenread > 0) {
                fout.write(buf, 0, lenread);
                lenread = mvpn.read(buf);
            }
            fout.close();

            if (!mvpnout.setExecutable(true)) {
                VpnStatus.logError("Failed to make OpenVPN executable");
                return false;
            }


            return true;
        } catch (IOException e) {
            VpnStatus.logException(e);
            return false;
        }

    }


    public static void startOpenVpn(VpnProfile startprofile, Context context) {
        Intent startVPN = startprofile.prepareStartService(context);
        if (startVPN != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                //noinspection NewApi
                context.startForegroundService(startVPN);
            else
                context.startService(startVPN);

        }
    }


    public static String getConfigFilePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + "/" + OVPNCONFIGFILE;
    }

}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import de.blinkt.openvpn.VpnProfile;

public class ProfileManager {
    private static final String PREFS_NAME = "VPNList";

    private static final String LAST_CONNECTED_PROFILE = "lastConnectedProfile";
    private static final String TEMPORARY_PROFILE_FILENAME = "temporary-vpn-profile";
    private static ProfileManager instance;

    private static VpnProfile mLastConnectedVpn = null;
    private HashMap<String, VpnProfile> profiles = new HashMap<>();
    private static VpnProfile tmpprofile = null;


    private static VpnProfile get(String key) {
        if (tmpprofile != null && tmpprofile.getUUIDString().equals(key))
            return tmpprofile;

        if (instance == null)
            return null;
        return instance.profiles.get(key);

    }


    private ProfileManager() {
    }

    private static void checkInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager();
            instance.loadVPNList(context);
        }
    }

    synchronized public static ProfileManager getInstance(Context context) {
        checkInstance(context);
        return instance;
    }

    public static void setConntectedVpnProfileDisconnected(Context c) {
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(c);
        Editor prefsedit = prefs.edit();
        prefsedit.putString(LAST_CONNECTED_PROFILE, null);
        prefsedit.apply();

    }

    /**
     * Sets the profile that is connected (to connect if the service restarts)
     */
    public static void setConnectedVpnProfile(Context c, VpnProfile connectedProfile) {
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(c);
        Editor prefsedit = prefs.edit();

        prefsedit.putString(LAST_CONNECTED_PROFILE, connectedProfile.getUUIDString());
        prefsedit.apply();
        mLastConnectedVpn = connectedProfile;

    }

    /**
     * Returns the profile that was last connected (to connect if the service restarts)
     */
    public static VpnProfile getLastConnectedProfile(Context c) {
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(c);

        String lastConnectedProfile = prefs.getString(LAST_CONNECTED_PROFILE, null);
        if (lastConnectedProfile != null)
            return get(c, lastConnectedProfile);
        else
            return null;
    }


    public Collection<VpnProfile> getProfiles() {
        return profiles.values();
    }

    public VpnProfile getProfileByName(String name) {
        for (VpnProfile vpnp : profiles.values()) {
            if (vpnp.getName().equals(name)) {
                return vpnp;
            }
        }
        return null;
    }

    public void saveProfileList(Context context) {
        SharedPreferences sharedprefs = Preferences.getSharedPreferencesMulti(PREFS_NAME, context);
        Editor editor = sharedprefs.edit();
        editor.putStringSet("vpnlist", profiles.keySet());

        // For reasing I do not understand at all
        // Android saves my prefs file only one time
        // if I remove the debug code below :(
        int counter = sharedprefs.getInt("counter", 0);
        editor.putInt("counter", counter + 1);
        editor.apply();

    }

    public void addProfile(VpnProfile profile) {
        profiles.put(profile.getUUID().toString(), profile);

    }

    public static void setTemporaryProfile(Context c, VpnProfile tmp) {
        tmp.mTemporaryProfile = true;
        ProfileManager.tmpprofile = tmp;
        saveProfile(c, tmp, true, true);
    }

    public static boolean isTempProfile() {
        return mLastConnectedVpn != null && mLastConnectedVpn  == tmpprofile;
    }

    public void saveProfile(Context context, VpnProfile profile) {
        saveProfile(context, profile, true, false);
    }

    private static void saveProfile(Context context, VpnProfile profile, boolean updateVersion, boolean isTemporary) {

        if (updateVersion)
            profile.mVersion += 1;
        ObjectOutputStream vpnFile;

        String filename = profile.getUUID().toString() + ".vp";
        if (isTemporary)
            filename = TEMPORARY_PROFILE_FILENAME + ".vp";

        try {
            vpnFile = new ObjectOutputStream(context.openFileOutput(filename, Activity.MODE_PRIVATE));

            vpnFile.writeObject(profile);
            vpnFile.flush();
            vpnFile.close();
        } catch (IOException e) {
            VpnStatus.logException("saving VPN profile", e);
            throw new RuntimeException(e);
        }
    }


    private void loadVPNList(Context context) {
        profiles = new HashMap<>();
        SharedPreferences listpref = Preferences.getSharedPreferencesMulti(PREFS_NAME, context);
        Set<String> vlist = listpref.getStringSet("vpnlist", null);
        if (vlist == null) {
            vlist = new HashSet<>();
        }
        // Always try to load the temporary profile
        vlist.add(TEMPORARY_PROFILE_FILENAME);

        for (String vpnentry : vlist) {
            ObjectInputStream vpnfile=null;
            try {
                 vpnfile = new ObjectInputStream(context.openFileInput(vpnentry + ".vp"));
                VpnProfile vp = ((VpnProfile) vpnfile.readObject());

                // Sanity check
                if (vp == null || vp.mName == null || vp.getUUID() == null)
                    continue;

                vp.upgradeProfile();
                if (vpnentry.equals(TEMPORARY_PROFILE_FILENAME)) {
                    tmpprofile = vp;
                } else {
                    profiles.put(vp.getUUID().toString(), vp);
                }


            } catch (IOException | ClassNotFoundException e) {
                if (!vpnentry.equals(TEMPORARY_PROFILE_FILENAME))
                    VpnStatus.logException("Loading VPN List", e);
            } finally {
                if (vpnfile!=null) {
                    try {
                        vpnfile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void removeProfile(Context context, VpnProfile profile) {
        String vpnentry = profile.getUUID().toString();
        profiles.remove(vpnentry);
        saveProfileList(context);
        context.deleteFile(vpnentry + ".vp");
        if (mLastConnectedVpn == profile)
            mLastConnectedVpn = null;

    }

    public static VpnProfile get(Context context, String profileUUID) {
        return get(context, profileUUID, 0, 10);
    }

    public static VpnProfile get(Context context, String profileUUID, int version, int tries) {
        checkInstance(context);
        VpnProfile profile = get(profileUUID);
        int tried = 0;
        while ((profile == null || profile.mVersion < version) && (tried++ < tries)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            instance.loadVPNList(context);
            profile = get(profileUUID);
            int ver = profile == null ? -1 : profile.mVersion;
        }

        if (tried > 5)

        {
            int ver = profile == null ? -1 : profile.mVersion;
            VpnStatus.logError(String.format(Locale.US, "Used x %d tries to get current version (%d/%d) of the profile", tried, ver, version));
        }
        return profile;
    }

    public static VpnProfile getLastConnectedVpn() {
        return mLastConnectedVpn;
    }

    public static VpnProfile getAlwaysOnVPN(Context context) {
        checkInstance(context);
        SharedPreferences prefs = Preferences.getDefaultSharedPreferences(context);

        String uuid = prefs.getString("alwaysOnVpn", null);
        return get(uuid);

    }

    public static void updateLRU(Context c, VpnProfile profile) {
        profile.mLastUsed = System.currentTimeMillis();
        // LRU does not change the profile, no need for the service to refresh
        if (profile!=tmpprofile)
            saveProfile(c, profile, false, false);
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.Context;
import android.content.Intent;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.system.Os;
import android.util.Log;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class OpenVpnManagementThread implements Runnable, OpenVPNManagement {

    public static final int ORBOT_TIMEOUT_MS = 20 * 1000;
    private static final String TAG = "openvpn";
    private static final Vector<OpenVpnManagementThread> active = new Vector<>();
    private final Handler mResumeHandler;
    private LocalSocket mSocket;
    private VpnProfile mProfile;
    private OpenVPNService mOpenVPNService;
    private LinkedList<FileDescriptor> mFDList = new LinkedList<>();
    private LocalServerSocket mServerSocket;
    private boolean mWaitingForRelease = false;
    private long mLastHoldRelease = 0;
    private LocalSocket mServerSocketLocal;

    private pauseReason lastPauseReason = pauseReason.noNetwork;
    private PausedStateCallback mPauseCallback;
    private boolean mShuttingDown;
    private Runnable mResumeHoldRunnable = () -> {
        if (shouldBeRunning()) {
            releaseHoldCmd();
        }
    };
    private Runnable orbotStatusTimeOutRunnable = new Runnable() {
        @Override
        public void run() {
            sendProxyCMD(Connection.ProxyType.SOCKS5, "127.0.0.1", Integer.toString(OrbotHelper.SOCKS_PROXY_PORT_DEFAULT), false);
            OrbotHelper.get(mOpenVPNService).removeStatusCallback(statusCallback);

        }
    };
    private OrbotHelper.StatusCallback statusCallback = new OrbotHelper.StatusCallback() {

        @Override
        public void onStatus(Intent statusIntent) {
            StringBuilder extras = new StringBuilder();
            for (String key : statusIntent.getExtras().keySet()) {
                Object val = statusIntent.getExtras().get(key);

                extras.append(String.format(Locale.ENGLISH, "%s - '%s'", key, val == null ? "null" : val.toString()));
            }
            VpnStatus.logDebug("Got Orbot status: " + extras);
        }

        @Override
        public void onNotYetInstalled() {
            VpnStatus.logDebug("Orbot not yet installed");
        }

        @Override
        public void onOrbotReady(Intent intent, String socksHost, int socksPort) {
            mResumeHandler.removeCallbacks(orbotStatusTimeOutRunnable);
            sendProxyCMD(Connection.ProxyType.SOCKS5, socksHost, Integer.toString(socksPort), false);
            OrbotHelper.get(mOpenVPNService).removeStatusCallback(this);
        }

        @Override
        public void onDisabled(Intent intent) {
            VpnStatus.logWarning("Orbot integration for external applications is disabled. Waiting %ds before connecting to the default port. Enable external app integration in Orbot or use Socks v5 config instead of Orbot to avoid this delay.");
        }
    };
    private transient Connection mCurrentProxyConnection;

    public OpenVpnManagementThread(VpnProfile profile, OpenVPNService openVpnService) {
        mProfile = profile;
        mOpenVPNService = openVpnService;
        mResumeHandler = new Handler(openVpnService.getMainLooper());

    }

    private static boolean stopOpenVPN() {
        synchronized (active) {
            boolean sendCMD = false;
            for (OpenVpnManagementThread mt : active) {
                sendCMD = mt.managmentCommand("signal SIGINT\n");
                try {
                    if (mt.mSocket != null)
                        mt.mSocket.close();
                } catch (IOException e) {
                    // Ignore close error on already closed socket
                }
            }
            return sendCMD;
        }
    }

    public boolean openManagementInterface(@NonNull Context c) {
        // Could take a while to open connection
        int tries = 8;

        String socketName = (c.getCacheDir().getAbsolutePath() + "/" + "mgmtsocket");
        // The mServerSocketLocal is transferred to the LocalServerSocket, ignore warning

        mServerSocketLocal = new LocalSocket();

        while (tries > 0 && !mServerSocketLocal.isBound()) {
            try {
                mServerSocketLocal.bind(new LocalSocketAddress(socketName,
                        LocalSocketAddress.Namespace.FILESYSTEM));
            } catch (IOException e) {
                // wait 300 ms before retrying
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {
                }

            }
            tries--;
        }

        try {

            mServerSocket = new LocalServerSocket(mServerSocketLocal.getFileDescriptor());
            return true;
        } catch (IOException e) {
            VpnStatus.logException(e);
        }
        return false;


    }

    /**
     * @param cmd command to write to management socket
     * @return true if command have been sent
     */
    public boolean managmentCommand(String cmd) {
        try {
            if (mSocket != null && mSocket.getOutputStream() != null) {
                mSocket.getOutputStream().write(cmd.getBytes());
                mSocket.getOutputStream().flush();
                return true;
            }
        } catch (IOException e) {
            // Ignore socket stack traces
        }
        return false;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        //	mSocket.setSoTimeout(5); // Setting a timeout cannot be that bad

        String pendingInput = "";
        synchronized (active) {
            active.add(this);
        }

        try {
            // Wait for a client to connect
            mSocket = mServerSocket.accept();
            InputStream instream = mSocket.getInputStream();


            // Close the management socket after client connected
            try {
                mServerSocket.close();
            } catch (IOException e) {
                VpnStatus.logException(e);
            }

            // Closing one of the two sockets also closes the other
            //mServerSocketLocal.close();
            managmentCommand("version 3\n");

            while (true) {

                int numbytesread = instream.read(buffer);
                if (numbytesread == -1)
                    return;

                FileDescriptor[] fds = null;
                try {
                    fds = mSocket.getAncillaryFileDescriptors();
                } catch (IOException e) {
                    VpnStatus.logException("Error reading fds from socket", e);
                }
                if (fds != null) {
                    Collections.addAll(mFDList, fds);
                }

                String input = new String(buffer, 0, numbytesread, "UTF-8");

                pendingInput += input;

                pendingInput = processInput(pendingInput);


            }
        } catch (IOException e) {
            if (!e.getMessage().equals("socket closed") && !e.getMessage().equals("Connection reset by peer"))
                VpnStatus.logException(e);
        }
        synchronized (active) {
            active.remove(this);
        }
    }

    //! Hack O Rama 2000!
    private void protectFileDescriptor(FileDescriptor fd) {
        try {
            Method getInt = FileDescriptor.class.getDeclaredMethod("getInt$");
            int fdint = (Integer) getInt.invoke(fd);

            // You can even get more evil by parsing toString() and extract the int from that :)

            boolean result = mOpenVPNService.protect(fdint);
            if (!result)
                VpnStatus.logWarning("Could not protect VPN socket");


            //ParcelFileDescriptor pfd = ParcelFileDescriptor.fromFd(fdint);
            //pfd.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fdCloseLollipop(fd);
            } else {
                NativeUtils.jniclose(fdint);
            }
            return;
        } catch ( NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException | NullPointerException e) {
            VpnStatus.logException("Failed to retrieve fd from socket (" + fd + ")", e);
        }

        Log.d("Openvpn", "Failed to retrieve fd from socket: " + fd);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fdCloseLollipop(FileDescriptor fd) {
        try {
            Os.close(fd);
        } catch (Exception e) {
            VpnStatus.logException("Failed to close fd (" + fd + ")", e);
        }
    }

    private String processInput(String pendingInput) {


        while (pendingInput.contains("\n")) {
            String[] tokens = pendingInput.split("\\r?\\n", 2);
            processCommand(tokens[0]);
            if (tokens.length == 1)
                // No second part, newline was at the end
                pendingInput = "";
            else
                pendingInput = tokens[1];
        }
        return pendingInput;
    }

    private void processCommand(String command) {
        //Log.i(TAG, "Line from managment" + command);

        if (command.startsWith(">") && command.contains(":")) {
            String[] parts = command.split(":", 2);
            String cmd = parts[0].substring(1);
            String argument = parts[1];

            switch (cmd) {
                case "INFO":
                /* Ignore greeting from management */
                    return;
                case "PASSWORD":
                    processPWCommand(argument);
                    break;
                case "HOLD":
                    handleHold(argument);
                    break;
                case "NEED-OK":
                    processNeedCommand(argument);
                    break;
                case "BYTECOUNT":
                    processByteCount(argument);
                    break;
                case "STATE":
                    if (!mShuttingDown)
                        processState(argument);
                    break;
                case "PROXY":
                    processProxyCMD(argument);
                    break;
                case "LOG":
                    processLogMessage(argument);
                    break;
                case "PK_SIGN":
                    processSignCommand(argument);
                    break;
                case "INFOMSG":
                    processInfoMessage(argument);
                    break;
                default:
                    VpnStatus.logWarning("MGMT: Got unrecognized command" + command);
                    Log.i(TAG, "Got unrecognized command" + command);
                    break;
            }
        } else if (command.startsWith("SUCCESS:")) {
            /* Ignore this kind of message too */
            return;
        } else if (command.startsWith("PROTECTFD: ")) {
            FileDescriptor fdtoprotect = mFDList.pollFirst();
            if (fdtoprotect != null)
                protectFileDescriptor(fdtoprotect);
        } else {
            Log.i(TAG, "Got unrecognized line from managment" + command);
            VpnStatus.logWarning("MGMT: Got unrecognized line from management:" + command);
        }
    }

    private void processInfoMessage(String info)
    {
        if (info.startsWith("OPEN_URL:") || info.startsWith("CR_TEXT:"))
        {
            mOpenVPNService.trigger_sso(info);
        }
        else
        {
            VpnStatus.logDebug("Info message from server:" + info);
        }
    }

    private void processLogMessage(String argument) {
        String[] args = argument.split(",", 4);
        // 0 unix time stamp
        // 1 log level N,I,E etc.
                /*
                  (b) zero or more message flags in a single string:
          I -- informational
          F -- fatal error
          N -- non-fatal error
          W -- warning
          D -- debug, and
                 */
        // 2 log message

        Log.d("OpenVPN", argument);

        VpnStatus.LogLevel level;
        switch (args[1]) {
            case "I":
                level = VpnStatus.LogLevel.INFO;
                break;
            case "W":
                level = VpnStatus.LogLevel.WARNING;
                break;
            case "D":
                level = VpnStatus.LogLevel.VERBOSE;
                break;
            case "F":
                level = VpnStatus.LogLevel.ERROR;
                break;
            default:
                level = VpnStatus.LogLevel.INFO;
                break;
        }

        int ovpnlevel = Integer.parseInt(args[2]) & 0x0F;
        String msg = args[3];

        if (msg.startsWith("MANAGEMENT: CMD"))
            ovpnlevel = Math.max(4, ovpnlevel);

        VpnStatus.logMessageOpenVPN(level, ovpnlevel, msg);
    }

    boolean shouldBeRunning() {
        if (mPauseCallback == null)
            return false;
        else
            return mPauseCallback.shouldBeRunning();
    }

    private void handleHold(String argument) {
        mWaitingForRelease = true;
        int waittime = Integer.parseInt(argument.split(":")[1]);
        if (shouldBeRunning()) {
            if (waittime > 1)
                VpnStatus.updateStateString("CONNECTRETRY", String.valueOf(waittime),
                        R.string.state_waitconnectretry, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET);
            mResumeHandler.postDelayed(mResumeHoldRunnable, waittime * 1000);
            if (waittime > 5)
                VpnStatus.logInfo(R.string.state_waitconnectretry, String.valueOf(waittime));
            else
                VpnStatus.logDebug(R.string.state_waitconnectretry, String.valueOf(waittime));

        } else {
            VpnStatus.updateStatePause(lastPauseReason);
        }
    }

    private void releaseHoldCmd() {
        mResumeHandler.removeCallbacks(mResumeHoldRunnable);
        if ((System.currentTimeMillis() - mLastHoldRelease) < 5000) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
            }

        }
        mWaitingForRelease = false;
        mLastHoldRelease = System.currentTimeMillis();
        managmentCommand("hold release\n");
        managmentCommand("bytecount " + mBytecountInterval + "\n");
        managmentCommand("state on\n");
        //managmentCommand("log on all\n");
    }

    public void releaseHold() {
        if (mWaitingForRelease)
            releaseHoldCmd();
    }

    private void processProxyCMD(String argument) {
        String[] args = argument.split(",", 3);

        Connection.ProxyType proxyType = Connection.ProxyType.NONE;

        int connectionEntryNumber = Integer.parseInt(args[0]) - 1;
        String proxyport = null;
        String proxyname = null;
        boolean proxyUseAuth = false;

        if (mProfile.mConnections.length > connectionEntryNumber) {
            Connection connection = mProfile.mConnections[connectionEntryNumber];
            proxyType = connection.mProxyType;
            proxyname = connection.mProxyName;
            proxyport = connection.mProxyPort;
            proxyUseAuth = connection.mUseProxyAuth;

            // Use transient variable to remember http user/password
            mCurrentProxyConnection = connection;

        } else {
            VpnStatus.logError(String.format(Locale.ENGLISH, "OpenVPN is asking for a proxy of an unknown connection entry (%d)", connectionEntryNumber));
        }

        // atuo detection of proxy
        if (proxyType == Connection.ProxyType.NONE) {
            SocketAddress proxyaddr = ProxyDetection.detectProxy(mProfile);
            if (proxyaddr instanceof InetSocketAddress) {
                InetSocketAddress isa = (InetSocketAddress) proxyaddr;
                proxyType = Connection.ProxyType.HTTP;
                proxyname = isa.getHostName();
                proxyport = String.valueOf(isa.getPort());
                proxyUseAuth = false;

            }
        }


        if (args.length >= 2 && proxyType == Connection.ProxyType.HTTP) {
            String proto = args[1];
            if (proto.equals("UDP")) {
                proxyname = null;
                VpnStatus.logInfo("Not using an HTTP proxy since the connection uses UDP");
            }
        }


        if (proxyType == Connection.ProxyType.ORBOT) {
            VpnStatus.updateStateString("WAIT_ORBOT", "Waiting for Orbot to start", R.string.state_waitorbot, ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET);
            OrbotHelper orbotHelper = OrbotHelper.get(mOpenVPNService);
            if (!orbotHelper.checkTorReceier(mOpenVPNService))
                VpnStatus.logError("Orbot does not seem to be installed!");

            mResumeHandler.postDelayed(orbotStatusTimeOutRunnable, ORBOT_TIMEOUT_MS);
            orbotHelper.addStatusCallback(mOpenVPNService, statusCallback);

            orbotHelper.sendOrbotStartAndStatusBroadcast();

        } else {
            sendProxyCMD(proxyType, proxyname, proxyport, proxyUseAuth);
        }
    }

    private void sendProxyCMD(Connection.ProxyType proxyType, String proxyname, String proxyport, boolean usePwAuth) {
        if (proxyType != Connection.ProxyType.NONE && proxyname != null) {

            VpnStatus.logInfo(R.string.using_proxy, proxyname, proxyname);

            String pwstr =  usePwAuth ? " auto" : "";

            String proxycmd = String.format(Locale.ENGLISH, "proxy %s %s %s%s\n",
                    proxyType == Connection.ProxyType.HTTP ? "HTTP" : "SOCKS",
                    proxyname, proxyport, pwstr);
            managmentCommand(proxycmd);
        } else {
            managmentCommand("proxy NONE\n");
        }
    }

    private void processState(String argument) {
        String[] args = argument.split(",", 3);
        String currentstate = args[1];

        if (args[2].equals(",,"))
            VpnStatus.updateStateString(currentstate, "");
        else
            VpnStatus.updateStateString(currentstate, args[2]);
    }

    private void processByteCount(String argument) {
        //   >BYTECOUNT:{BYTES_IN},{BYTES_OUT}
        int comma = argument.indexOf(',');
        long in = Long.parseLong(argument.substring(0, comma));
        long out = Long.parseLong(argument.substring(comma + 1));

        VpnStatus.updateByteCount(in, out);

    }

    private void processNeedCommand(String argument) {
        int p1 = argument.indexOf('\'');
        int p2 = argument.indexOf('\'', p1 + 1);

        String needed = argument.substring(p1 + 1, p2);
        String extra = argument.split(":", 2)[1];

        String status = "ok";


        switch (needed) {
            case "PROTECTFD":
                FileDescriptor fdtoprotect = mFDList.pollFirst();
                protectFileDescriptor(fdtoprotect);
                break;
            case "DNSSERVER":
            case "DNS6SERVER":
                mOpenVPNService.addDNS(extra);
                break;
            case "DNSDOMAIN":
                mOpenVPNService.setDomain(extra);
                break;
            case "ROUTE": {
                String[] routeparts = extra.split(" ");

            /*
            buf_printf (&out, "%s %s %s dev %s", network, netmask, gateway, rgi->iface);
            else
            buf_printf (&out, "%s %s %s", network, netmask, gateway);
            */

                if (routeparts.length == 5) {
                    //if (BuildConfig.DEBUG)
                    //                assertEquals("dev", routeparts[3]);
                    mOpenVPNService.addRoute(routeparts[0], routeparts[1], routeparts[2], routeparts[4]);
                } else if (routeparts.length >= 3) {
                    mOpenVPNService.addRoute(routeparts[0], routeparts[1], routeparts[2], null);
                } else {
                    VpnStatus.logError("Unrecognized ROUTE cmd:" + Arrays.toString(routeparts) + " | " + argument);
                }

                break;
            }
            case "ROUTE6": {
                String[] routeparts = extra.split(" ");
                mOpenVPNService.addRoutev6(routeparts[0], routeparts[1]);
                break;
            }
            case "IFCONFIG":
                String[] ifconfigparts = extra.split(" ");
                int mtu = Integer.parseInt(ifconfigparts[2]);
                mOpenVPNService.setLocalIP(ifconfigparts[0], ifconfigparts[1], mtu, ifconfigparts[3]);
                break;
            case "IFCONFIG6":
                String[] ifconfig6parts = extra.split(" ");
                mtu = Integer.parseInt(ifconfig6parts[1]);
                mOpenVPNService.setMtu(mtu);
                mOpenVPNService.setLocalIPv6(ifconfig6parts[0]);
                break;
            case "PERSIST_TUN_ACTION":
                // check if tun cfg stayed the same
                status = mOpenVPNService.getTunReopenStatus();
                break;
            case "OPENTUN":
                if (sendTunFD(needed, extra))
                    return;
                else
                    status = "cancel";
                // This not nice or anything but setFileDescriptors accepts only FilDescriptor class :(

                break;
            default:
                Log.e(TAG, "Unknown needok command " + argument);
                return;
        }

        String cmd = String.format("needok '%s' %s\n", needed, status);
        managmentCommand(cmd);
    }

    private boolean sendTunFD(String needed, String extra) {
        if (!extra.equals("tun")) {
            // We only support tun
            VpnStatus.logError(String.format("Device type %s requested, but only tun is possible with the Android API, sorry!", extra));

            return false;
        }
        ParcelFileDescriptor pfd = mOpenVPNService.openTun();
        if (pfd == null)
            return false;

        Method setInt;
        int fdint = pfd.getFd();
        try {
            setInt = FileDescriptor.class.getDeclaredMethod("setInt$", int.class);
            FileDescriptor fdtosend = new FileDescriptor();

            setInt.invoke(fdtosend, fdint);

            FileDescriptor[] fds = {fdtosend};
            mSocket.setFileDescriptorsForSend(fds);

            // Trigger a send so we can close the fd on our side of the channel
            // The API documentation fails to mention that it will not reset the file descriptor to
            // be send and will happily send the file descriptor on every write ...
            String cmd = String.format("needok '%s' %s\n", needed, "ok");
            managmentCommand(cmd);

            // Set the FileDescriptor to null to stop this mad behavior
            mSocket.setFileDescriptorsForSend(null);

            pfd.close();

            return true;
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException |
                IOException | IllegalAccessException exp) {
            VpnStatus.logException("Could not send fd over socket", exp);
        }

        return false;
    }

    private void processPWCommand(String argument) {
        //argument has the form 	Need 'Private Key' password
        // or  ">PASSWORD:Verification Failed: '%s' ['%s']"
        String needed;


        try {
            // Ignore Auth token message, already managed by openvpn itself
            if (argument.startsWith("Auth-Token:")) {
                return;
            }

            int p1 = argument.indexOf('\'');
            int p2 = argument.indexOf('\'', p1 + 1);
            needed = argument.substring(p1 + 1, p2);
            if (argument.startsWith("Verification Failed")) {
                proccessPWFailed(needed, argument.substring(p2 + 1));
                return;
            }
        } catch (StringIndexOutOfBoundsException sioob) {
            VpnStatus.logError("Could not parse management Password command: " + argument);
            return;
        }

        String pw = null;
        String username = null;

        if (needed.equals("Private Key")) {
            pw = mProfile.getPasswordPrivateKey();
        } else if (needed.equals("Auth")) {
            pw = mProfile.getPasswordAuth();
            username = mProfile.mUsername;

        } else if (needed.equals("HTTP Proxy")) {
            if( mCurrentProxyConnection != null) {
                pw = mCurrentProxyConnection.mProxyAuthPassword;
                username = mCurrentProxyConnection.mProxyAuthUser;
            }
        }
        if (pw != null) {
            if (username !=null) {
                String usercmd = String.format("username '%s' %s\n",
                        needed, VpnProfile.openVpnEscape(username));
                managmentCommand(usercmd);
            }
            String cmd = String.format("password '%s' %s\n", needed, VpnProfile.openVpnEscape(pw));
            managmentCommand(cmd);
        } else {
            mOpenVPNService.requestInputFromUser(R.string.password, needed);
            VpnStatus.logError(String.format("Openvpn requires Authentication type '%s' but no password/key information available", needed));
        }

    }

    private void proccessPWFailed(String needed, String args) {
        VpnStatus.updateStateString("AUTH_FAILED", needed + args, R.string.state_auth_failed, ConnectionStatus.LEVEL_AUTH_FAILED);
    }

    @Override
    public void networkChange(boolean samenetwork) {
        if (mWaitingForRelease)
            releaseHold();
        else if (samenetwork)
            managmentCommand("network-change samenetwork\n");
        else
            managmentCommand("network-change\n");
    }

    @Override
    public void setPauseCallback(PausedStateCallback callback) {
        mPauseCallback = callback;
    }

    @Override
    public void sendCRResponse(String response) {
        managmentCommand("cr-response "  + response + "\n");
    }

    public void signalusr1() {
        mResumeHandler.removeCallbacks(mResumeHoldRunnable);
        if (!mWaitingForRelease)
            managmentCommand("signal SIGUSR1\n");
        else
            // If signalusr1 is called update the state string
            // if there is another for stopping
            VpnStatus.updateStatePause(lastPauseReason);
    }

    public void reconnect() {
        signalusr1();
        releaseHold();
    }

    private void processSignCommand(String argument) {

        String[] arguments = argument.split(",");

        boolean pkcs1padding = arguments[1].equals("RSA_PKCS1_PADDING");
        String signed_string = mProfile.getSignedData(mOpenVPNService, arguments[0], pkcs1padding);

        if (signed_string == null) {
            managmentCommand("pk-sig\n");
            managmentCommand("\nEND\n");
            stopOpenVPN();
            return;
        }
        managmentCommand("pk-sig\n");
        managmentCommand(signed_string);
        managmentCommand("\nEND\n");
    }

    @Override
    public void pause(pauseReason reason) {
        lastPauseReason = reason;
        signalusr1();
    }

    @Override
    public void resume() {
        releaseHold();
        /* Reset the reason why we are disconnected */
        lastPauseReason = pauseReason.noNetwork;
    }

    @Override
    public boolean stopVPN(boolean replaceConnection) {
        boolean stopSucceed = stopOpenVPN();
        if (stopSucceed) {
            mShuttingDown = true;

        }
        return stopSucceed;
    }

}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemReader;


import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class X509Utils {
	public static Certificate[] getCertificatesFromFile(String certfilename) throws FileNotFoundException, CertificateException {
		CertificateFactory certFact = CertificateFactory.getInstance("X.509");

        Vector<Certificate> certificates = new Vector<>();
		if(VpnProfile.isEmbedded(certfilename)) {
            int subIndex = certfilename.indexOf("-----BEGIN CERTIFICATE-----");
            do {
                // The java certifcate reader is ... kind of stupid
                // It does NOT ignore chars before the --BEGIN ...

                subIndex = Math.max(0, subIndex);
                InputStream inStream = new ByteArrayInputStream(certfilename.substring(subIndex).getBytes());
                certificates.add(certFact.generateCertificate(inStream));

                subIndex = certfilename.indexOf("-----BEGIN CERTIFICATE-----", subIndex+1);
            } while (subIndex > 0);
            return certificates.toArray(new Certificate[certificates.size()]);
        } else {
			InputStream inStream = new FileInputStream(certfilename);
            return new Certificate[] {certFact.generateCertificate(inStream)};
        }


	}

	public static PemObject readPemObjectFromFile (String keyfilename) throws IOException {

		Reader inStream;

		if(VpnProfile.isEmbedded(keyfilename))
			inStream = new StringReader(VpnProfile.getEmbeddedContent(keyfilename));
		else 
			inStream = new FileReader(new File(keyfilename));

		PemReader pr = new PemReader(inStream);
		PemObject r = pr.readPemObject();
		pr.close();
		return r;
	}




	public static String getCertificateFriendlyName (Context c, String filename) {
		if(!TextUtils.isEmpty(filename)) {
			try {
				X509Certificate cert = (X509Certificate) getCertificatesFromFile(filename)[0];
                String friendlycn = getCertificateFriendlyName(cert);
                friendlycn = getCertificateValidityString(cert, c.getResources()) + friendlycn;
                return friendlycn;

			} catch (Exception e) {
				VpnStatus.logError("Could not read certificate" + e.getLocalizedMessage());
			}
		}
		return c.getString(R.string.cannotparsecert);
	}

    public static String getCertificateValidityString(X509Certificate cert, Resources res) {
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException ce) {
            return "EXPIRED: ";
        } catch (CertificateNotYetValidException cny) {
            return "NOT YET VALID: ";
        }

        Date certNotAfter = cert.getNotAfter();
        Date now = new Date();
        long timeLeft = certNotAfter.getTime() - now.getTime(); // Time left in ms

        // More than 72h left, display days
        // More than 3 months display months
        if (timeLeft > 90l* 24 * 3600 * 1000) {
            long months = getMonthsDifference(now, certNotAfter);
            return res.getQuantityString(R.plurals.months_left, (int) months, months);
        } else if (timeLeft > 72 * 3600 * 1000) {
            long days = timeLeft / (24 * 3600 * 1000);
            return res.getQuantityString(R.plurals.days_left, (int) days, days);
        } else {
            long hours = timeLeft / (3600 * 1000);

            return res.getQuantityString(R.plurals.hours_left, (int)hours, hours);
        }
    }

    public static int getMonthsDifference(Date date1, Date date2) {
        int m1 = date1.getYear() * 12 + date1.getMonth();
        int m2 = date2.getYear() * 12 + date2.getMonth();
        return m2 - m1 + 1;
    }

    public static String getCertificateFriendlyName(X509Certificate cert) {
        X500Principal principal = cert.getSubjectX500Principal();
        byte[] encodedSubject = principal.getEncoded();
        String friendlyName=null;

        /* Hack so we do not have to ship a whole Spongy/bouncycastle */
        Exception exp=null;
        try {
            @SuppressLint("PrivateApi") Class X509NameClass = Class.forName("com.android.org.bouncycastle.asn1.x509.X509Name");
            Method getInstance = X509NameClass.getMethod("getInstance",Object.class);

            Hashtable defaultSymbols = (Hashtable) X509NameClass.getField("DefaultSymbols").get(X509NameClass);

            if (!defaultSymbols.containsKey("1.2.840.113549.1.9.1"))
                defaultSymbols.put("1.2.840.113549.1.9.1","eMail");

            Object subjectName = getInstance.invoke(X509NameClass, encodedSubject);

            Method toString = X509NameClass.getMethod("toString",boolean.class,Hashtable.class);

            friendlyName= (String) toString.invoke(subjectName,true,defaultSymbols);
                    
        } catch (ClassNotFoundException e) {
            exp =e ;
        } catch (NoSuchMethodException e) {
            exp =e;
        } catch (InvocationTargetException e) {
            exp =e;
        } catch (IllegalAccessException e) {
            exp =e;
        } catch (NoSuchFieldException e) {
            exp =e;
        }
        if (exp!=null)
            VpnStatus.logException("Getting X509 Name from certificate", exp);

        /* Fallback if the reflection method did not work */
        if(friendlyName==null)
            friendlyName = principal.getName();


        // Really evil hack to decode email address
        // See: http://code.google.com/p/android/issues/detail?id=21531

        String[] parts = friendlyName.split(",");
        for (int i=0;i<parts.length;i++){
            String part = parts[i];
            if (part.startsWith("1.2.840.113549.1.9.1=#16")) {
                parts[i] = "email=" + ia5decode(part.replace("1.2.840.113549.1.9.1=#16", ""));
            }
        }
        friendlyName = TextUtils.join(",", parts);
        return friendlyName;
    }

    public static boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
        return (!Character.isISOControl(c)) &&
                block != null &&
                block != Character.UnicodeBlock.SPECIALS;
    }

    private static String ia5decode(String ia5string) {
        String d = "";
        for (int i=1;i<ia5string.length();i=i+2) {
            String hexstr = ia5string.substring(i-1,i+1);
            char c = (char) Integer.parseInt(hexstr,16);
            if (isPrintableChar(c)) {
                d+=c;
            } else if (i==1 && (c==0x12 || c==0x1b)) {
                ;   // ignore
            } else {
                d += "\\x" + hexstr;
            }
        }
        return d;
    }


}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.blinkt.openvpn.R;

public class OpenVPNThread implements Runnable {
    private static final String DUMP_PATH_STRING = "Dump path: ";
    @SuppressLint("SdCardPath")
    private static final String BROKEN_PIE_SUPPORT = "/data/data/de.blinkt.openvpn/cache/pievpn";
    private final static String BROKEN_PIE_SUPPORT2 = "syntax error";
    private static final String TAG = "OpenVPN";
    // 1380308330.240114 18000002 Send to HTTP proxy: 'X-Online-Host: bla.blabla.com'
    private static final Pattern LOG_PATTERN = Pattern.compile("(\\d+).(\\d+) ([0-9a-f])+ (.*)");
    public static final int M_FATAL = (1 << 4);
    public static final int M_NONFATAL = (1 << 5);
    public static final int M_WARN = (1 << 6);
    public static final int M_DEBUG = (1 << 7);
    private String[] mArgv;
    private static Process mProcess;
    private String mNativeDir;
    private String mTmpDir;
    private static OpenVPNService mService;
    private String mDumpPath;
    private boolean mBrokenPie = false;
    private boolean mNoProcessExitStatus = false;

    public OpenVPNThread(OpenVPNService service, String[] argv, String nativelibdir, String tmpdir) {
        mArgv = argv;
        mNativeDir = nativelibdir;
        mTmpDir = tmpdir;
        mService = service;
    }

    public OpenVPNThread() {
    }

    public void stopProcess() {
        mProcess.destroy();
    }

    void setReplaceConnection()
    {
        mNoProcessExitStatus=true;
    }

    @Override
    public void run() {
        try {
            Log.i(TAG, "Starting openvpn");
            startOpenVPNThreadArgs(mArgv);
            Log.i(TAG, "OpenVPN process exited");
        } catch (Exception e) {
            VpnStatus.logException("Starting OpenVPN Thread", e);
            Log.e(TAG, "OpenVPNThread Got " + e.toString());
        } finally {
            int exitvalue = 0;
            try {
                if (mProcess != null)
                    exitvalue = mProcess.waitFor();
            } catch (IllegalThreadStateException ite) {
                VpnStatus.logError("Illegal Thread state: " + ite.getLocalizedMessage());
            } catch (InterruptedException ie) {
                VpnStatus.logError("InterruptedException: " + ie.getLocalizedMessage());
            }
            if (exitvalue != 0) {
                VpnStatus.logError("Process exited with exit value " + exitvalue);
                if (mBrokenPie) {
                    /* This will probably fail since the NoPIE binary is probably not written */
                    String[] noPieArgv = VPNLaunchHelper.replacePieWithNoPie(mArgv);

                    // We are already noPIE, nothing to gain
                    if (!noPieArgv.equals(mArgv)) {
                        mArgv = noPieArgv;
                        VpnStatus.logInfo("PIE Version could not be executed. Trying no PIE version");
                        run();
                    }

                }

            }

            if (!mNoProcessExitStatus)
                VpnStatus.updateStateString("NOPROCESS", "No process running.", R.string.state_noprocess, ConnectionStatus.LEVEL_NOTCONNECTED);

            if (mDumpPath != null) {
                try {
                    BufferedWriter logout = new BufferedWriter(new FileWriter(mDumpPath + ".log"));
                    SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
                    for (LogItem li : VpnStatus.getlogbuffer()) {
                        String time = timeformat.format(new Date(li.getLogtime()));
                        logout.write(time + " " + li.getString(mService) + "\n");
                    }
                    logout.close();
                    VpnStatus.logError(R.string.minidump_generated);
                } catch (IOException e) {
                    VpnStatus.logError("Writing minidump log: " + e.getLocalizedMessage());
                }
            }

            if (!mNoProcessExitStatus)
                mService.openvpnStopped();
            Log.i(TAG, "Exiting");
        }
    }

    public static boolean stop(){
        mService.openvpnStopped();
        mProcess.destroy();
        return true;
    }

    private void startOpenVPNThreadArgs(String[] argv) {
        LinkedList<String> argvlist = new LinkedList<String>();

        Collections.addAll(argvlist, argv);

        ProcessBuilder pb = new ProcessBuilder(argvlist);
        // Hack O rama

        String lbpath = genLibraryPath(argv, pb);

        pb.environment().put("LD_LIBRARY_PATH", lbpath);
        pb.environment().put("TMPDIR", mTmpDir);

        pb.redirectErrorStream(true);
        try {
            mProcess = pb.start();
            // Close the output, since we don't need it
            mProcess.getOutputStream().close();
            InputStream in = mProcess.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            while (true) {
                String logline = br.readLine();
                if (logline == null)
                    return;

                if (logline.startsWith(DUMP_PATH_STRING))
                    mDumpPath = logline.substring(DUMP_PATH_STRING.length());

                if (logline.startsWith(BROKEN_PIE_SUPPORT) || logline.contains(BROKEN_PIE_SUPPORT2))
                    mBrokenPie = true;

                Matcher m = LOG_PATTERN.matcher(logline);
                int logerror = 0;
                if (m.matches()) {
                    int flags = Integer.parseInt(m.group(3), 16);
                    String msg = m.group(4);
                    int logLevel = flags & 0x0F;

                    VpnStatus.LogLevel logStatus = VpnStatus.LogLevel.INFO;

                    if ((flags & M_FATAL) != 0)
                        logStatus = VpnStatus.LogLevel.ERROR;
                    else if ((flags & M_NONFATAL) != 0)
                        logStatus = VpnStatus.LogLevel.WARNING;
                    else if ((flags & M_WARN) != 0)
                        logStatus = VpnStatus.LogLevel.WARNING;
                    else if ((flags & M_DEBUG) != 0)
                        logStatus = VpnStatus.LogLevel.VERBOSE;

                    if (msg.startsWith("MANAGEMENT: CMD"))
                        logLevel = Math.max(4, logLevel);

                    if ((msg.endsWith("md too weak") && msg.startsWith("OpenSSL: error")) || msg.contains("error:140AB18E"))
                        logerror = 1;

                    VpnStatus.logMessageOpenVPN(logStatus, logLevel, msg);
                    if (logerror==1)
                        VpnStatus.logError("OpenSSL reported a certificate with a weak hash, please the in app FAQ about weak hashes");

                } else {
                    VpnStatus.logInfo("P:" + logline);
                }

                if (Thread.interrupted()) {
                    throw new InterruptedException("OpenVpn process was killed form java code");
                }
            }
        } catch (InterruptedException | IOException e) {
            VpnStatus.logException("Error reading from output of OpenVPN process", e);
            stopProcess();
        }


    }

    private String genLibraryPath(String[] argv, ProcessBuilder pb) {
        // Hack until I find a good way to get the real library path
        String applibpath = argv[0].replaceFirst("/cache/.*$", "/lib");

        String lbpath = pb.environment().get("LD_LIBRARY_PATH");
        if (lbpath == null)
            lbpath = applibpath;
        else
            lbpath = applibpath + ":" + lbpath;

        if (!applibpath.equals(mNativeDir)) {
            lbpath = mNativeDir + ":" + lbpath;
        }
        return lbpath;
    }
}


/*
 * Copyright (c) 2012-2018 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.*;
import android.security.KeyChainException;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import de.blinkt.openvpn.api.ExternalCertificateProvider;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ExtAuthHelper {

    public static final String ACTION_CERT_CONFIGURATION = "de.blinkt.openvpn.api.ExternalCertificateConfiguration";
    public static final String ACTION_CERT_PROVIDER = "de.blinkt.openvpn.api.ExternalCertificateProvider";

    public static final String EXTRA_ALIAS = "de.blinkt.openvpn.api.KEY_ALIAS";
    public static final String EXTRA_DESCRIPTION = "de.blinkt.openvpn.api.KEY_DESCRIPTION";


    public static void setExternalAuthProviderSpinnerList(Spinner spinner, String selectedApp) {
        Context c = spinner.getContext();
        final PackageManager pm = c.getPackageManager();
        ArrayList<ExternalAuthProvider> extProviders = getExternalAuthProviderList(c);

        int selectedPos = -1;

        if (extProviders.size() ==0)
        {
            selectedApp = "";
            ExternalAuthProvider noauthprovider = new ExternalAuthProvider();
            noauthprovider.label = "No external auth provider found";
            noauthprovider.packageName = selectedApp;
            noauthprovider.configurable = false;
            extProviders.add(noauthprovider);
        }


        for (int i = 0; i < extProviders.size(); i++) {
            if (extProviders.get(i).packageName.equals(selectedApp))
                selectedPos = i;
        }
        SpinnerAdapter extAppAdapter = new ArrayAdapter<ExternalAuthProvider>(c, android.R.layout.simple_spinner_item, android.R.id.text1, extProviders);
        spinner.setAdapter(extAppAdapter);
        if (selectedPos != -1)
            spinner.setSelection(selectedPos);
    }

    static ArrayList<ExternalAuthProvider> getExternalAuthProviderList(Context c) {
        Intent configureExtAuth = new Intent(ACTION_CERT_CONFIGURATION);

        final PackageManager packageManager = c.getPackageManager();
        List<ResolveInfo> configureList =
                packageManager.queryIntentActivities(configureExtAuth, 0);

        Intent serviceExtAuth = new Intent(ACTION_CERT_PROVIDER);

        List<ResolveInfo> serviceList =
                packageManager.queryIntentServices(serviceExtAuth, 0);


        // For now only list those who appear in both lists

        ArrayList<ExternalAuthProvider> providers = new ArrayList<ExternalAuthProvider>();

        for (ResolveInfo service : serviceList) {
            ExternalAuthProvider ext = new ExternalAuthProvider();
            ext.packageName = service.serviceInfo.packageName;

            ext.label = (String) service.serviceInfo.applicationInfo.loadLabel(packageManager);

            for (ResolveInfo activity : configureList) {
                if (service.serviceInfo.packageName.equals(activity.activityInfo.packageName)) {
                    ext.configurable = true;
                }
            }
            providers.add(ext);

        }
        return providers;

    }

    @Nullable
    @WorkerThread
    public static byte[] signData(@NonNull Context context,
                                  @NonNull String extAuthPackageName,
                                  @NonNull String alias,
                                  @NonNull byte[] data
    ) throws KeyChainException, InterruptedException

    {


        try (ExternalAuthProviderConnection authProviderConnection = bindToExtAuthProvider(context.getApplicationContext(), extAuthPackageName)) {
            ExternalCertificateProvider externalAuthProvider = authProviderConnection.getService();
            return externalAuthProvider.getSignedData(alias, data);

        } catch (RemoteException e) {
            throw new KeyChainException(e);
        }
    }

    @Nullable
    @WorkerThread
    public static X509Certificate[] getCertificateChain(@NonNull Context context,
                                                        @NonNull String extAuthPackageName,
                                                        @NonNull String alias) throws KeyChainException {

        final byte[] certificateBytes;
        try (ExternalAuthProviderConnection authProviderConnection = bindToExtAuthProvider(context.getApplicationContext(), extAuthPackageName)) {
            ExternalCertificateProvider externalAuthProvider = authProviderConnection.getService();
            certificateBytes = externalAuthProvider.getCertificateChain(alias);
            if (certificateBytes == null) {
                return null;
            }
            Collection<X509Certificate> chain = toCertificates(certificateBytes);
            return chain.toArray(new X509Certificate[chain.size()]);

        } catch (RemoteException | RuntimeException | InterruptedException e) {
            throw new KeyChainException(e);
        }
    }

    public static Bundle getCertificateMetaData(@NonNull Context context,
                                                @NonNull String extAuthPackageName,
                                                String alias) throws KeyChainException
    {
        try (ExternalAuthProviderConnection authProviderConnection = bindToExtAuthProvider(context.getApplicationContext(), extAuthPackageName)) {
            ExternalCertificateProvider externalAuthProvider = authProviderConnection.getService();
            return externalAuthProvider.getCertificateMetaData(alias);

        } catch (RemoteException | RuntimeException | InterruptedException e) {
            throw new KeyChainException(e);
        }
    }

    public static Collection<X509Certificate> toCertificates(@NonNull byte[] bytes) {
        final String BEGINCERT = "-----BEGIN CERTIFICATE-----";
        try {
            Vector<X509Certificate> retCerts = new Vector<>();
            // Java library is broken, although the javadoc says it will extract all certificates from a byte array
            // it only extracts the first one
            String allcerts = new String(bytes, "iso8859-1");
            String[] certstrings = allcerts.split(BEGINCERT);
            for (String certstring: certstrings) {
                certstring = BEGINCERT + certstring;
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                retCerts.addAll((Collection<? extends X509Certificate>) certFactory.generateCertificates(
                        new ByteArrayInputStream((certstring.getBytes("iso8859-1")))));

            }
            return retCerts;

        } catch (CertificateException e) {
            throw new AssertionError(e);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    // adapted form Keychain
    @WorkerThread
    public static ExternalAuthProviderConnection bindToExtAuthProvider(@NonNull Context context, String packagename) throws KeyChainException, InterruptedException {
        ensureNotOnMainThread(context);
        final BlockingQueue<ExternalCertificateProvider> q = new LinkedBlockingQueue<>(1);
        ServiceConnection extAuthServiceConnection = new ServiceConnection() {
            volatile boolean mConnectedAtLeastOnce = false;

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (!mConnectedAtLeastOnce) {
                    mConnectedAtLeastOnce = true;
                    try {
                        q.put(ExternalCertificateProvider.Stub.asInterface(service));
                    } catch (InterruptedException e) {
                        // will never happen, since the queue starts with one available slot
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        Intent intent = new Intent(ACTION_CERT_PROVIDER);
        intent.setPackage(packagename);

        if (!context.bindService(intent, extAuthServiceConnection, Context.BIND_AUTO_CREATE)) {
            throw new KeyChainException("could not bind to external authticator app: " + packagename);
        }
        return new ExternalAuthProviderConnection(context, extAuthServiceConnection, q.take());
    }

    private static void ensureNotOnMainThread(@NonNull Context context) {
        Looper looper = Looper.myLooper();
        if (looper != null && looper == context.getMainLooper()) {
            throw new IllegalStateException(
                    "calling this from your main thread can lead to deadlock");
        }
    }

    public static class ExternalAuthProvider {

        public String packageName;
        public boolean configurable = false;
        private String label;

        @Override
        public String toString() {
            return label;
        }
    }

    public static class ExternalAuthProviderConnection implements Closeable {
        private final Context context;
        private final ServiceConnection serviceConnection;
        private final ExternalCertificateProvider service;

        protected ExternalAuthProviderConnection(Context context,
                                                 ServiceConnection serviceConnection,
                                                 ExternalCertificateProvider service) {
            this.context = context;
            this.serviceConnection = serviceConnection;
            this.service = service;
        }

        @Override
        public void close() {
            context.unbindService(serviceConnection);
        }

        public ExternalCertificateProvider getService() {
            return service;
        }
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Locale;

public class Connection implements Serializable, Cloneable {
    public String mServerName = "openvpn.example.com";
    public String mServerPort = "1194";
    public boolean mUseUdp = true;
    public String mCustomConfiguration = "";
    public boolean mUseCustomConfig = false;
    public boolean mEnabled = true;
    public int mConnectTimeout = 0;
    public static final int CONNECTION_DEFAULT_TIMEOUT = 120;
    public ProxyType mProxyType = ProxyType.NONE;
    public String mProxyName = "proxy.example.com";
    public String mProxyPort = "8080";

    public boolean mUseProxyAuth;
    public String mProxyAuthUser = null;
    public String mProxyAuthPassword = null;

    public enum ProxyType {
        NONE,
        HTTP,
        SOCKS5,
        ORBOT
    }

    private static final long serialVersionUID = 92031902903829089L;


    public String getConnectionBlock(boolean isOpenVPN3) {
        String cfg = "";

        // Server Address
        cfg += "remote ";
        cfg += mServerName;
        cfg += " ";
        cfg += mServerPort;
        if (mUseUdp)
            cfg += " udp\n";
        else
            cfg += " tcp-client\n";

        if (mConnectTimeout != 0)
            cfg += String.format(Locale.US, " connect-timeout  %d\n", mConnectTimeout);

        // OpenVPN 2.x manages proxy connection via management interface
        if ((isOpenVPN3 || usesExtraProxyOptions()) && mProxyType == ProxyType.HTTP)
        {
            cfg+=String.format(Locale.US,"http-proxy %s %s\n", mProxyName, mProxyPort);
            if (mUseProxyAuth)
                cfg+=String.format(Locale.US, "<http-proxy-user-pass>\n%s\n%s\n</http-proxy-user-pass>\n", mProxyAuthUser, mProxyAuthPassword);
        }
        if (usesExtraProxyOptions() && mProxyType == ProxyType.SOCKS5) {
            cfg+=String.format(Locale.US,"socks-proxy %s %s\n", mProxyName, mProxyPort);
        }

        if (!TextUtils.isEmpty(mCustomConfiguration) && mUseCustomConfig) {
            cfg += mCustomConfiguration;
            cfg += "\n";
        }


        return cfg;
    }

    public boolean usesExtraProxyOptions() {
        return (mUseCustomConfig && mCustomConfiguration.contains("http-proxy-option "));
    }


    @Override
    public Connection clone() throws CloneNotSupportedException {
        return (Connection) super.clone();
    }

    public boolean isOnlyRemote() {
        return TextUtils.isEmpty(mCustomConfiguration) || !mUseCustomConfig;
    }

    public int getTimeout() {
        if (mConnectTimeout <= 0)
            return CONNECTION_DEFAULT_TIMEOUT;
        else
            return mConnectTimeout;
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;/*
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will Google be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, as long as the origin is not misrepresented.
 */

import android.os.Build;
import android.os.Process;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.security.Security;

/**
 * Fixes for the output of the default PRNG having low entropy.
 *
 * The fixes need to be applied via {@link #apply()} before any use of Java
 * Cryptography Architecture primitives. A good place to invoke them is in the
 * application's {@code onCreate}.
 */
public final class PRNGFixes {

    private static final int VERSION_CODE_JELLY_BEAN = 16;
    private static final int VERSION_CODE_JELLY_BEAN_MR2 = 18;
    private static final byte[] BUILD_FINGERPRINT_AND_DEVICE_SERIAL =
            getBuildFingerprintAndDeviceSerial();

    /** Hidden constructor to prevent instantiation. */
    private PRNGFixes() {}

    /**
     * Applies all fixes.
     *
     * @throws SecurityException if a fix is needed but could not be applied.
     */
    public static void apply() {
        applyOpenSSLFix();
        installLinuxPRNGSecureRandom();
    }

    /**
     * Applies the fix for OpenSSL PRNG having low entropy. Does nothing if the
     * fix is not needed.
     *
     * @throws SecurityException if the fix is needed but could not be applied.
     */
    private static void applyOpenSSLFix() throws SecurityException {
        if ((Build.VERSION.SDK_INT < VERSION_CODE_JELLY_BEAN)
                || (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2)) {
            // No need to apply the fix
            return;
        }

        try {
            // Mix in the device- and invocation-specific seed.
            Class.forName("org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                    .getMethod("RAND_seed", byte[].class)
                    .invoke(null, generateSeed());

            // Mix output of Linux PRNG into OpenSSL's PRNG
            int bytesRead = (Integer) Class.forName(
                    "org.apache.harmony.xnet.provider.jsse.NativeCrypto")
                    .getMethod("RAND_load_file", String.class, long.class)
                    .invoke(null, "/dev/urandom", 1024);
            if (bytesRead != 1024) {
                throw new IOException(
                        "Unexpected number of bytes read from Linux PRNG: "
                                + bytesRead);
            }
        } catch (Exception e) {
            throw new SecurityException("Failed to seed OpenSSL PRNG", e);
        }
    }

    /**
     * Installs a Linux PRNG-backed {@code SecureRandom} implementation as the
     * default. Does nothing if the implementation is already the default or if
     * there is not need to install the implementation.
     *
     * @throws SecurityException if the fix is needed but could not be applied.
     */
    private static void installLinuxPRNGSecureRandom()
            throws SecurityException {
        if (Build.VERSION.SDK_INT > VERSION_CODE_JELLY_BEAN_MR2) {
            // No need to apply the fix
            return;
        }

        // Install a Linux PRNG-based SecureRandom implementation as the
        // default, if not yet installed.
        Provider[] secureRandomProviders =
                Security.getProviders("SecureRandom.SHA1PRNG");
        if ((secureRandomProviders == null)
                || (secureRandomProviders.length < 1)
                || (!LinuxPRNGSecureRandomProvider.class.equals(
                secureRandomProviders[0].getClass()))) {
            Security.insertProviderAt(new LinuxPRNGSecureRandomProvider(), 1);
        }

        // Assert that new SecureRandom() and
        // SecureRandom.getInstance("SHA1PRNG") return a SecureRandom backed
        // by the Linux PRNG-based SecureRandom implementation.
        SecureRandom rng1 = new SecureRandom();
        if (!LinuxPRNGSecureRandomProvider.class.equals(
                rng1.getProvider().getClass())) {
            throw new SecurityException(
                    "new SecureRandom() backed by wrong Provider: "
                            + rng1.getProvider().getClass());
        }

        SecureRandom rng2;
        try {
            rng2 = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("SHA1PRNG not available", e);
        }
        if (!LinuxPRNGSecureRandomProvider.class.equals(
                rng2.getProvider().getClass())) {
            throw new SecurityException(
                    "SecureRandom.getInstance(\"SHA1PRNG\") backed by wrong"
                            + " Provider: " + rng2.getProvider().getClass());
        }
    }

    /**
     * {@code Provider} of {@code SecureRandom} engines which pass through
     * all requests to the Linux PRNG.
     */
    private static class LinuxPRNGSecureRandomProvider extends Provider {

        public LinuxPRNGSecureRandomProvider() {
            super("LinuxPRNG",
                    1.0,
                    "A Linux-specific random number provider that uses"
                            + " /dev/urandom");
            // Although /dev/urandom is not a SHA-1 PRNG, some apps
            // explicitly request a SHA1PRNG SecureRandom and we thus need to
            // prevent them from getting the default implementation whose output
            // may have low entropy.
            put("SecureRandom.SHA1PRNG", LinuxPRNGSecureRandom.class.getName());
            put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
        }
    }

    /**
     * {@link SecureRandomSpi} which passes all requests to the Linux PRNG
     * ({@code /dev/urandom}).
     */
    public static class LinuxPRNGSecureRandom extends SecureRandomSpi {

        /*
         * IMPLEMENTATION NOTE: Requests to generate bytes and to mix in a seed
         * are passed through to the Linux PRNG (/dev/urandom). Instances of
         * this class seed themselves by mixing in the current time, PID, UID,
         * build fingerprint, and hardware serial number (where available) into
         * Linux PRNG.
         *
         * Concurrency: Read requests to the underlying Linux PRNG are
         * serialized (on sLock) to ensure that multiple threads do not get
         * duplicated PRNG output.
         */

        private static final File URANDOM_FILE = new File("/dev/urandom");

        private static final Object sLock = new Object();

        /**
         * Input stream for reading from Linux PRNG or {@code null} if not yet
         * opened.
         *
         * @GuardedBy("sLock")
         */
        private static DataInputStream sUrandomIn;

        /**
         * Output stream for writing to Linux PRNG or {@code null} if not yet
         * opened.
         *
         * @GuardedBy("sLock")
         */
        private static OutputStream sUrandomOut;

        /**
         * Whether this engine instance has been seeded. This is needed because
         * each instance needs to seed itself if the client does not explicitly
         * seed it.
         */
        private boolean mSeeded;

        @Override
        protected void engineSetSeed(byte[] bytes) {
            try {
                OutputStream out;
                synchronized (sLock) {
                    out = getUrandomOutputStream();
                }
                out.write(bytes);
                out.flush();
            } catch (IOException e) {
                // On a small fraction of devices /dev/urandom is not writable.
                // Log and ignore.
                Log.w(PRNGFixes.class.getSimpleName(),
                        "Failed to mix seed into " + URANDOM_FILE);
            } finally {
                mSeeded = true;
            }
        }

        @Override
        protected void engineNextBytes(byte[] bytes) {
            if (!mSeeded) {
                // Mix in the device- and invocation-specific seed.
                engineSetSeed(generateSeed());
            }

            try {
                DataInputStream in;
                synchronized (sLock) {
                    in = getUrandomInputStream();
                }
                synchronized (in) {
                    in.readFully(bytes);
                }
            } catch (IOException e) {
                throw new SecurityException(
                        "Failed to read from " + URANDOM_FILE, e);
            }
        }

        @Override
        protected byte[] engineGenerateSeed(int size) {
            byte[] seed = new byte[size];
            engineNextBytes(seed);
            return seed;
        }

        private DataInputStream getUrandomInputStream() {
            synchronized (sLock) {
                if (sUrandomIn == null) {
                    // NOTE: Consider inserting a BufferedInputStream between
                    // DataInputStream and FileInputStream if you need higher
                    // PRNG output performance and can live with future PRNG
                    // output being pulled into this process prematurely.
                    try {
                        sUrandomIn = new DataInputStream(
                                new FileInputStream(URANDOM_FILE));
                    } catch (IOException e) {
                        throw new SecurityException("Failed to open "
                                + URANDOM_FILE + " for reading", e);
                    }
                }
                return sUrandomIn;
            }
        }

        private OutputStream getUrandomOutputStream() throws IOException {
            synchronized (sLock) {
                if (sUrandomOut == null) {
                    sUrandomOut = new FileOutputStream(URANDOM_FILE);
                }
                return sUrandomOut;
            }
        }
    }

    /**
     * Generates a device- and invocation-specific seed to be mixed into the
     * Linux PRNG.
     */
    private static byte[] generateSeed() {
        try {
            ByteArrayOutputStream seedBuffer = new ByteArrayOutputStream();
            DataOutputStream seedBufferOut =
                    new DataOutputStream(seedBuffer);
            seedBufferOut.writeLong(System.currentTimeMillis());
            seedBufferOut.writeLong(System.nanoTime());
            seedBufferOut.writeInt(Process.myPid());
            seedBufferOut.writeInt(Process.myUid());
            seedBufferOut.write(BUILD_FINGERPRINT_AND_DEVICE_SERIAL);
            seedBufferOut.close();
            return seedBuffer.toByteArray();
        } catch (IOException e) {
            throw new SecurityException("Failed to generate seed", e);
        }
    }

    /**
     * Gets the hardware serial number of this device.
     *
     * @return serial number or {@code null} if not available.
     */
    private static String getDeviceSerialNumber() {
        // We're using the Reflection API because Build.SERIAL is only available
        // since API Level 9 (Gingerbread, Android 2.3).
        try {
            return (String) Build.class.getField("SERIAL").get(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static byte[] getBuildFingerprintAndDeviceSerial() {
        StringBuilder result = new StringBuilder();
        String fingerprint = Build.FINGERPRINT;
        if (fingerprint != null) {
            result.append(fingerprint);
        }
        String serial = getDeviceSerialNumber();
        if (serial != null) {
            result.append(serial);
        }
        try {
            return result.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported");
        }
    }
}

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Build;
import de.blinkt.openvpn.BuildConfig;

import java.security.InvalidKeyException;

public class NativeUtils {
    public static native byte[] rsasign(byte[] input, int pkey, boolean pkcs1padding) throws InvalidKeyException;

    public static native String[] getIfconfig() throws IllegalArgumentException;

    static native void jniclose(int fdint);

    public static String getNativeAPI()
    {
        if (isRoboUnitTest())
            return "ROBO";
        else
            return getJNIAPI();
    }

    private static native String getJNIAPI();

    public static native String getOpenVPN2GitVersion();

    public static native String getOpenVPN3GitVersion();

    public final static int[] openSSLlengths = {
        16, 64, 256, 1024, 8 * 1024, 16 * 1024
    };

    public static native double[] getOpenSSLSpeed(String algorithm, int testnum);

    static {
        if (!isRoboUnitTest()) {
            System.loadLibrary("opvpnutil");
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN)
                System.loadLibrary("jbcrypto");

        }
    }

    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT); }

}

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Build;
import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.util.Collection;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.Vector;

import de.blinkt.openvpn.BuildConfig;



public class NetworkSpace {

    static void assertTrue(boolean f)
    {
        if (!f)
            throw new IllegalStateException();
    }

    static class IpAddress implements Comparable<IpAddress> {
        private BigInteger netAddress;
        public int networkMask;
        private boolean included;
        private boolean isV4;
        private BigInteger firstAddress;
        private BigInteger lastAddress;


        /**
         * sorts the networks with following criteria:
         * 1. compares first 1 of the network
         * 2. smaller networks are returned as smaller
         */
        @Override
        public int compareTo(@NonNull IpAddress another) {
            int comp = getFirstAddress().compareTo(another.getFirstAddress());
            if (comp != 0)
                return comp;


            if (networkMask > another.networkMask)
                return -1;
            else if (another.networkMask == networkMask)
                return 0;
            else
                return 1;
        }

        /**
         * Warning ignores the included integer
         *
         * @param o the object to compare this instance with.
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IpAddress))
                return super.equals(o);


            IpAddress on = (IpAddress) o;
            return (networkMask == on.networkMask) && on.getFirstAddress().equals(getFirstAddress());
        }

        public IpAddress(CIDRIP ip, boolean include) {
            included = include;
            netAddress = BigInteger.valueOf(ip.getInt());
            networkMask = ip.len;
            isV4 = true;
        }

        public IpAddress(Inet6Address address, int mask, boolean include) {
            networkMask = mask;
            included = include;

            int s = 128;

            netAddress = BigInteger.ZERO;
            for (byte b : address.getAddress()) {
                s -= 8;
                netAddress = netAddress.add(BigInteger.valueOf((b & 0xFF)).shiftLeft(s));
            }
        }

        public BigInteger getLastAddress() {
            if (lastAddress == null)
                lastAddress = getMaskedAddress(true);
            return lastAddress;
        }


        public BigInteger getFirstAddress() {
            if (firstAddress == null)
                firstAddress = getMaskedAddress(false);
            return firstAddress;
        }


        private BigInteger getMaskedAddress(boolean one) {
            BigInteger numAddress = netAddress;

            int numBits;
            if (isV4) {
                numBits = 32 - networkMask;
            } else {
                numBits = 128 - networkMask;
            }

            for (int i = 0; i < numBits; i++) {
                if (one)
                    numAddress = numAddress.setBit(i);
                else
                    numAddress = numAddress.clearBit(i);
            }
            return numAddress;
        }


        @Override
        public String toString() {
            //String in = included ? "+" : "-";
            if (isV4)
                return String.format(Locale.US, "%s/%d", getIPv4Address(), networkMask);
            else
                return String.format(Locale.US, "%s/%d", getIPv6Address(), networkMask);
        }

        IpAddress(BigInteger baseAddress, int mask, boolean included, boolean isV4) {
            this.netAddress = baseAddress;
            this.networkMask = mask;
            this.included = included;
            this.isV4 = isV4;
        }


        public IpAddress[] split() {
            IpAddress firstHalf = new IpAddress(getFirstAddress(), networkMask + 1, included, isV4);
            IpAddress secondHalf = new IpAddress(firstHalf.getLastAddress().add(BigInteger.ONE), networkMask + 1, included, isV4);
            if (BuildConfig.DEBUG)
                assertTrue(secondHalf.getLastAddress().equals(getLastAddress()));
            return new IpAddress[]{firstHalf, secondHalf};
        }

        String getIPv4Address() {
            if (BuildConfig.DEBUG) {
                assertTrue(isV4);
                assertTrue(netAddress.longValue() <= 0xffffffffl);
                assertTrue(netAddress.longValue() >= 0);
            }
            long ip = netAddress.longValue();
            return String.format(Locale.US, "%d.%d.%d.%d", (ip >> 24) % 256, (ip >> 16) % 256, (ip >> 8) % 256, ip % 256);
        }

        String getIPv6Address() {
            if (BuildConfig.DEBUG) assertTrue(!isV4);
            BigInteger r = netAddress;

            String ipv6str = null;
            boolean lastPart = true;

            while (r.compareTo(BigInteger.ZERO) == 1) {

                long part = r.mod(BigInteger.valueOf(0x10000)).longValue();
                if (ipv6str != null || part != 0) {
                    if (ipv6str == null && !lastPart)
                            ipv6str = ":";

                    if (lastPart)
                        ipv6str = String.format(Locale.US, "%x", part, ipv6str);
                    else
                        ipv6str = String.format(Locale.US, "%x:%s", part, ipv6str);
                }

                r = r.shiftRight(16);
                lastPart = false;
            }
            if (ipv6str == null)
                return "::";


            return ipv6str;
        }

        public boolean containsNet(IpAddress network) {
            // this.first >= net.first &&  this.last <= net.last
            BigInteger ourFirst = getFirstAddress();
            BigInteger ourLast = getLastAddress();
            BigInteger netFirst = network.getFirstAddress();
            BigInteger netLast = network.getLastAddress();

            boolean a = ourFirst.compareTo(netFirst) != 1;
            boolean b = ourLast.compareTo(netLast) != -1;
            return a && b;

        }
    }


    TreeSet<IpAddress> mIpAddresses = new TreeSet<IpAddress>();


    public Collection<IpAddress> getNetworks(boolean included) {
        Vector<IpAddress> ips = new Vector<IpAddress>();
        for (IpAddress ip : mIpAddresses) {
            if (ip.included == included)
                ips.add(ip);
        }
        return ips;
    }

    public void clear() {
        mIpAddresses.clear();
    }


    void addIP(CIDRIP cidrIp, boolean include) {

        mIpAddresses.add(new IpAddress(cidrIp, include));
    }

    public void addIPSplit(CIDRIP cidrIp, boolean include) {
        IpAddress newIP = new IpAddress(cidrIp, include);
        IpAddress[] splitIps = newIP.split();
        for (IpAddress split : splitIps)
            mIpAddresses.add(split);
    }

    void addIPv6(Inet6Address address, int mask, boolean included) {
        mIpAddresses.add(new IpAddress(address, mask, included));
    }

    TreeSet<IpAddress> generateIPList() {

        PriorityQueue<IpAddress> networks = new PriorityQueue<IpAddress>(mIpAddresses);

        TreeSet<IpAddress> ipsDone = new TreeSet<IpAddress>();

        IpAddress currentNet = networks.poll();
        if (currentNet == null)
            return ipsDone;

        while (currentNet != null) {
            // Check if it and the next of it are compatible
            IpAddress nextNet = networks.poll();

            if (BuildConfig.DEBUG) assertTrue(currentNet!=null);
            if (nextNet == null || currentNet.getLastAddress().compareTo(nextNet.getFirstAddress()) == -1) {
                // Everything good, no overlapping nothing to do
                ipsDone.add(currentNet);

                currentNet = nextNet;
            } else {
                // This network is smaller or equal to the next but has the same base address
                if (currentNet.getFirstAddress().equals(nextNet.getFirstAddress()) && currentNet.networkMask >= nextNet.networkMask) {
                    if (currentNet.included == nextNet.included) {
                        // Included in the next next and same type
                        // Simply forget our current network
                        currentNet = nextNet;
                    } else {
                        // our currentNet is included in next and types differ. Need to split the next network
                        IpAddress[] newNets = nextNet.split();


                        // TODO: The contains method of the Priority is stupid linear search

                        // First add the second half to keep the order in networks
                        if (!networks.contains(newNets[1]))
                            networks.add(newNets[1]);

                        if (newNets[0].getLastAddress().equals(currentNet.getLastAddress())) {
                            if (BuildConfig.DEBUG)
                                assertTrue(newNets[0].networkMask == currentNet.networkMask);
                            // Don't add the lower half that would conflict with currentNet
                        } else {
                            if (!networks.contains(newNets[0]))
                                networks.add(newNets[0]);
                        }
                        // Keep currentNet as is
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        assertTrue(currentNet.networkMask < nextNet.networkMask);
                        assertTrue(nextNet.getFirstAddress().compareTo(currentNet.getFirstAddress()) == 1);
                        assertTrue(currentNet.getLastAddress().compareTo(nextNet.getLastAddress()) != -1);
                    }
                    // This network is bigger than the next and last ip of current >= next

                    //noinspection StatementWithEmptyBody
                    if (currentNet.included == nextNet.included) {
                        // Next network is in included in our network with the same type,
                        // simply ignore the next and move on
                    } else {
                        // We need to split our network
                        IpAddress[] newNets = currentNet.split();


                        if (newNets[1].networkMask == nextNet.networkMask) {
                            if (BuildConfig.DEBUG) {
                                assertTrue(newNets[1].getFirstAddress().equals(nextNet.getFirstAddress()));
                                assertTrue(newNets[1].getLastAddress().equals(currentNet.getLastAddress()));
                                // split second equal the next network, do not add it
                            }
                            networks.add(nextNet);
                        } else {
                            // Add the smaller network first
                            networks.add(newNets[1]);
                            networks.add(nextNet);
                        }
                        currentNet = newNets[0];

                    }
                }
            }

        }

        return ipsDone;
    }

    Collection<IpAddress> getPositiveIPList() {
        TreeSet<IpAddress> ipsSorted = generateIPList();

        Vector<IpAddress> ips = new Vector<IpAddress>();
        for (IpAddress ia : ipsSorted) {
            if (ia.included)
                ips.add(ia);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // Include postive routes from the original set under < 4.4 since these might overrule the local
            // network but only if no smaller negative route exists
            for (IpAddress origIp : mIpAddresses) {
                if (!origIp.included)
                    continue;

                // The netspace exists
                if (ipsSorted.contains(origIp))
                    continue;

                boolean skipIp = false;
                // If there is any smaller net that is excluded we may not add the positive route back

                for (IpAddress calculatedIp : ipsSorted) {
                    if (!calculatedIp.included && origIp.containsNet(calculatedIp)) {
                        skipIp = true;
                        break;
                    }
                }
                if (skipIp)
                    continue;

                // It is safe to include the IP
                ips.add(origIp);
            }

        }

        return ips;
    }

}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.TargetApi;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

/**
 * Created by arne on 26.11.14.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopDeviceStateListener extends ConnectivityManager.NetworkCallback {

    private String mLastConnectedStatus;
    private String mLastLinkProperties;
    private String mLastNetworkCapabilities;

    @Override
    public void onAvailable(Network network) {
        super.onAvailable(network);

        if (!network.toString().equals(mLastConnectedStatus)) {
            mLastConnectedStatus = network.toString();
            VpnStatus.logDebug("Connected to " + mLastConnectedStatus);
        }
    }

    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties);

        if (!linkProperties.toString().equals(mLastLinkProperties)) {
            mLastLinkProperties = linkProperties.toString();
            VpnStatus.logDebug(String.format("Linkproperties of %s: %s", network, linkProperties));
        }
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
        if (!networkCapabilities.toString().equals(mLastNetworkCapabilities)) {
            mLastNetworkCapabilities = networkCapabilities.toString();
            VpnStatus.logDebug(String.format("Network capabilities of %s: %s", network, networkCapabilities));
        }
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import java.util.UUID;

/**
 * Created by arne on 15.12.16.
 */

public class PasswordCache {
    public static final int PCKS12ORCERTPASSWORD = 2;
    public static final int AUTHPASSWORD = 3;
    private static PasswordCache mInstance;
    final private UUID mUuid;
    private String mKeyOrPkcs12Password;
    private String mAuthPassword;

    private PasswordCache(UUID uuid) {
        mUuid = uuid;
    }

    public static PasswordCache getInstance(UUID uuid) {
        if (mInstance == null || !mInstance.mUuid.equals(uuid)) {
            mInstance = new PasswordCache(uuid);
        }
        return mInstance;
    }

    public static String getPKCS12orCertificatePassword(UUID uuid, boolean resetPw) {
        String pwcopy = getInstance(uuid).mKeyOrPkcs12Password;
        if (resetPw)
            getInstance(uuid).mKeyOrPkcs12Password = null;
        return pwcopy;
    }


    public static String getAuthPassword(UUID uuid, boolean resetPW) {
        String pwcopy = getInstance(uuid).mAuthPassword;
        if (resetPW)
            getInstance(uuid).mAuthPassword = null;
        return pwcopy;
    }

    public static void setCachedPassword(String uuid, int type, String password) {
        PasswordCache instance = getInstance(UUID.fromString(uuid));
        switch (type) {
            case PCKS12ORCERTPASSWORD:
                instance.mKeyOrPkcs12Password = password;
                break;
            case AUTHPASSWORD:
                instance.mAuthPassword = password;
                break;
        }
    }


}


/*
 * Copyright (c) 2012-2018 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

/*
 * Portions Copyright 2014-2016 Hans-Christoph Steiner
 * Portions Copyright 2012-2016 Nathan Freitas
 * Portions Copyright (c) 2016 CommonsWare, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package de.blinkt.openvpn.core;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.blinkt.openvpn.core.OpenVPNService.ORBOT_PACKAGE_NAME;

public class OrbotHelper {
    //! Based on the class from NetCipher but stripped down and modified for icsopenvpn

    /**
     * {@link Intent} send by Orbot with {@code ON/OFF/STARTING/STOPPING} status
     * included as an {@link #EXTRA_STATUS} {@code String}.  Your app should
     * always receive {@code ACTION_STATUS Intent}s since any other app could
     * start Orbot.  Also, user-triggered starts and stops will also cause
     * {@code ACTION_STATUS Intent}s to be broadcast.
     */
    public final static String ACTION_STATUS = "org.torproject.android.intent.action.STATUS";
    public final static String STATUS_ON = "ON";
    public final static String STATUS_STARTS_DISABLED = "STARTS_DISABLED";

    public final static String STATUS_STARTING = "STARTING";
    public final static String STATUS_STOPPING = "STOPPING";
    public final static String EXTRA_STATUS = "org.torproject.android.intent.extra.STATUS";
    /**
     * A request to Orbot to transparently start Tor services
     */
    public final static String ACTION_START = "org.torproject.android.intent.action.START";
    public final static String EXTRA_PACKAGE_NAME = "org.torproject.android.intent.extra.PACKAGE_NAME";
    public static final int SOCKS_PROXY_PORT_DEFAULT = 9050;
    private static OrbotHelper mInstance;

    String EXTRA_SOCKS_PROXY_HOST = "org.torproject.android.intent.extra.SOCKS_PROXY_HOST";
    String EXTRA_SOCKS_PROXY_PORT = "org.torproject.android.intent.extra.SOCKS_PROXY_PORT";
    private Context mContext;
    private Set<StatusCallback> statusCallbacks = new HashSet<>();
    private BroadcastReceiver orbotStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (TextUtils.equals(intent.getAction(),
                    OrbotHelper.ACTION_STATUS)) {
                for (StatusCallback cb : statusCallbacks) {
                    cb.onStatus(intent);
                }

                String status = intent.getStringExtra(EXTRA_STATUS);
                if (TextUtils.equals(status, STATUS_ON)) {
                    int socksPort = intent.getIntExtra(EXTRA_SOCKS_PROXY_PORT, SOCKS_PROXY_PORT_DEFAULT);
                    String socksHost = intent.getStringExtra(EXTRA_SOCKS_PROXY_HOST);
                    if (TextUtils.isEmpty(socksHost))
                        socksHost = "127.0.0.1";
                    for (StatusCallback cb : statusCallbacks) {
                        cb.onOrbotReady(intent, socksHost, socksPort);
                    }
                } else if (TextUtils.equals(status, STATUS_STARTS_DISABLED)) {
                    for (StatusCallback cb : statusCallbacks)
                        cb.onDisabled(intent);
                }

            }
        }
    };

    private OrbotHelper() {

    }

    public static OrbotHelper get(OpenVPNService mOpenVPNService) {
        if (mInstance == null)
            mInstance = new OrbotHelper();
        return mInstance;
    }

    /**
     * Gets an {@link Intent} for starting Orbot.  Orbot will reply with the
     * current status to the {@code packageName} of the app in the provided
     * {@link Context} (i.e.  {@link Context#getPackageName()}.
     */
    public static Intent getOrbotStartIntent(Context context) {
        Intent intent = new Intent(ACTION_START);
        intent.setPackage(ORBOT_PACKAGE_NAME);
        intent.putExtra(EXTRA_PACKAGE_NAME, context.getPackageName());
        return intent;
    }

    public static boolean checkTorReceier(Context c) {
        Intent startOrbot = getOrbotStartIntent(c);
        PackageManager pm = c.getPackageManager();
        Intent result = null;
        List<ResolveInfo> receivers =
                pm.queryBroadcastReceivers(startOrbot, 0);

        return receivers != null && receivers.size() > 0;
    }

    /**
     * Adds a StatusCallback to be called when we find out that
     * Orbot is ready. If Orbot is ready for use, your callback
     * will be called with onEnabled() immediately, before this
     * method returns.
     *
     * @param cb a callback
     * @return the singleton, for chaining
     */
    public synchronized OrbotHelper addStatusCallback(Context c, StatusCallback cb) {
        if (statusCallbacks.size() == 0) {
            c.getApplicationContext().registerReceiver(orbotStatusReceiver,
                    new IntentFilter(OrbotHelper.ACTION_STATUS));
            mContext = c.getApplicationContext();
        }
        if (!checkTorReceier(c))
            cb.onNotYetInstalled();
        statusCallbacks.add(cb);
        return (this);
    }

    /**
     * Removes an existing registered StatusCallback.
     *
     * @param cb the callback to remove
     * @return the singleton, for chaining
     */
    public synchronized void removeStatusCallback(StatusCallback cb) {
        statusCallbacks.remove(cb);
        if (statusCallbacks.size() == 0)
            mContext.unregisterReceiver(orbotStatusReceiver);
    }

    public void sendOrbotStartAndStatusBroadcast() {
        mContext.sendBroadcast(getOrbotStartIntent(mContext));
    }

    private void startOrbotService(String action) {
        Intent clearVPNMode = new Intent();
        clearVPNMode.setComponent(new ComponentName(ORBOT_PACKAGE_NAME, ".service.TorService"));
        clearVPNMode.setAction(action);
        mContext.startService(clearVPNMode);
    }

    public interface StatusCallback {
        /**
         * Called when Orbot is operational
         *
         * @param statusIntent an Intent containing information about
         *                     Orbot, including proxy ports
         */
        void onStatus(Intent statusIntent);


        /**
         * Called if Orbot is not yet installed. Usually, you handle
         * this by checking the return value from init() on OrbotInitializer
         * or calling isInstalled() on OrbotInitializer. However, if
         * you have need for it, if a callback is registered before
         * an init() call determines that Orbot is not installed, your
         * callback will be called with onNotYetInstalled().
         */
        void onNotYetInstalled();

        void onOrbotReady(Intent intent, String socksHost, int socksPort);

        /**
         * Called if Orbot background control is disabled.
         * @param intent the intent delivered
         */
        void onDisabled(Intent intent);
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.os.Message;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Vector;

import de.blinkt.openvpn.R;

public class VpnStatus {


    private static final LinkedList<LogItem> logbuffer;

    private static Vector<LogListener> logListener;
    private static Vector<StateListener> stateListener;
    private static Vector<ByteCountListener> byteCountListener;

    private static String mLaststatemsg = "";

    private static String mLaststate = "NOPROCESS";

    private static int mLastStateresid = R.string.state_noprocess;

    private static Intent mLastIntent = null;

    private static HandlerThread mHandlerThread;

    private static String mLastConnectedVPNUUID;
    static boolean readFileLog =false;
    final static java.lang.Object readFileLock = new Object();


    public static TrafficHistory trafficHistory;


    public static void logException(LogLevel ll, String context, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        LogItem li;
        if (context != null) {
            li = new LogItem(ll, R.string.unhandled_exception_context, e.getMessage(), sw.toString(), context);
        } else {
            li = new LogItem(ll, R.string.unhandled_exception, e.getMessage(), sw.toString());
        }
        newLogItem(li);
    }

    public static void logException(Exception e) {
        logException(LogLevel.ERROR, null, e);
    }

    public static void logException(String context, Exception e) {
        logException(LogLevel.ERROR, context, e);
    }

    static final int MAXLOGENTRIES = 1000;

    public static boolean isVPNActive() {
        return mLastLevel != ConnectionStatus.LEVEL_AUTH_FAILED && !(mLastLevel == ConnectionStatus.LEVEL_NOTCONNECTED);
    }

    public static String getLastCleanLogMessage(Context c) {
        String message = mLaststatemsg;
        switch (mLastLevel) {
            case LEVEL_CONNECTED:
                String[] parts = mLaststatemsg.split(",");
                /*
                   (a) the integer unix date/time,
                   (b) the state name,
                   0 (c) optional descriptive string (used mostly on RECONNECTING
                    and EXITING to show the reason for the disconnect),

                    1 (d) optional TUN/TAP local IPv4 address
                   2 (e) optional address of remote server,
                   3 (f) optional port of remote server,
                   4 (g) optional local address,
                   5 (h) optional local port, and
                   6 (i) optional TUN/TAP local IPv6 address.
*/
                // Return only the assigned IP addresses in the UI
                if (parts.length >= 7)
                    message = String.format(Locale.US, "%s %s", parts[1], parts[6]);
                break;
        }

        while (message.endsWith(","))
            message = message.substring(0, message.length() - 1);

        String status = mLaststate;
        if (status.equals("NOPROCESS"))
            return message;

        if (mLastStateresid == R.string.state_waitconnectretry) {
            return c.getString(R.string.state_waitconnectretry, mLaststatemsg);
        }

        String prefix = c.getString(mLastStateresid);
        if (mLastStateresid == R.string.unknown_state)
            message = status + message;
        if (message.length() > 0)
            prefix += ": ";

        return prefix + message;

    }

    public static void initLogCache(File cacheDir) {
        mHandlerThread = new HandlerThread("LogFileWriter", Thread.MIN_PRIORITY);
        mHandlerThread.start();
        mLogFileHandler = new LogFileHandler(mHandlerThread.getLooper());


        Message m = mLogFileHandler.obtainMessage(LogFileHandler.LOG_INIT, cacheDir);
        mLogFileHandler.sendMessage(m);

    }

    public static void flushLog() {
        if (mLogFileHandler!=null)
            mLogFileHandler.sendEmptyMessage(LogFileHandler.FLUSH_TO_DISK);
    }

    public static void setConnectedVPNProfile(String uuid) {
        mLastConnectedVPNUUID = uuid;
        for (StateListener sl: stateListener)
            sl.setConnectedVPN(uuid);
    }


    public static String getLastConnectedVPNProfile()
    {
        return mLastConnectedVPNUUID;
    }

    public static void setTrafficHistory(TrafficHistory trafficHistory) {
        VpnStatus.trafficHistory = trafficHistory;
    }


    public enum LogLevel {
        INFO(2),
        ERROR(-2),
        WARNING(1),
        VERBOSE(3),
        DEBUG(4);

        protected int mValue;

        LogLevel(int value) {
            mValue = value;
        }

        public int getInt() {
            return mValue;
        }

        public static LogLevel getEnumByValue(int value) {
            switch (value) {
                case 2:
                    return INFO;
                case -2:
                    return ERROR;
                case 1:
                    return WARNING;
                case 3:
                    return VERBOSE;
                case 4:
                    return DEBUG;

                default:
                    return null;
            }
        }
    }

    // keytool -printcert -jarfile de.blinkt.openvpn_85.apk
    static final byte[] officalkey = {-58, -42, -44, -106, 90, -88, -87, -88, -52, -124, 84, 117, 66, 79, -112, -111, -46, 86, -37, 109};
    static final byte[] officaldebugkey = {-99, -69, 45, 71, 114, -116, 82, 66, -99, -122, 50, -70, -56, -111, 98, -35, -65, 105, 82, 43};
    static final byte[] amazonkey = {-116, -115, -118, -89, -116, -112, 120, 55, 79, -8, -119, -23, 106, -114, -85, -56, -4, 105, 26, -57};
    static final byte[] fdroidkey = {-92, 111, -42, -46, 123, -96, -60, 79, -27, -31, 49, 103, 11, -54, -68, -27, 17, 2, 121, 104};


    private static ConnectionStatus mLastLevel = ConnectionStatus.LEVEL_NOTCONNECTED;

    private static LogFileHandler mLogFileHandler;

    static {
        logbuffer = new LinkedList<>();
        logListener = new Vector<>();
        stateListener = new Vector<>();
        byteCountListener = new Vector<>();
        trafficHistory = new TrafficHistory();

        logInformation();

    }


    public interface LogListener {
        void newLog(LogItem logItem);
    }

    public interface StateListener {
        void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent Intent);

        void setConnectedVPN(String uuid);
    }

    public interface ByteCountListener {
        void updateByteCount(long in, long out, long diffIn, long diffOut);
    }

    public synchronized static void logMessage(LogLevel level, String prefix, String message) {
        newLogItem(new LogItem(level, prefix + message));

    }

    public synchronized static void clearLog() {
        logbuffer.clear();
        logInformation();
        if (mLogFileHandler != null)
            mLogFileHandler.sendEmptyMessage(LogFileHandler.TRIM_LOG_FILE);
    }

    private static void logInformation() {
        String nativeAPI;
        try {
            nativeAPI = NativeUtils.getNativeAPI();
        } catch (UnsatisfiedLinkError ignore) {
            nativeAPI = "error";
        }

        logInfo(R.string.mobile_info, Build.MODEL, Build.BOARD, Build.BRAND, Build.VERSION.SDK_INT,
                nativeAPI, Build.VERSION.RELEASE, Build.ID, Build.FINGERPRINT, "", "");
    }

    public synchronized static void addLogListener(LogListener ll) {
        logListener.add(ll);
    }

    public synchronized static void removeLogListener(LogListener ll) {
        logListener.remove(ll);
    }

    public synchronized static void addByteCountListener(ByteCountListener bcl) {
        TrafficHistory.LastDiff diff = trafficHistory.getLastDiff(null);
        bcl.updateByteCount(diff.getIn(), diff.getOut(), diff.getDiffIn(),diff.getDiffOut());
        byteCountListener.add(bcl);
    }

    public synchronized static void removeByteCountListener(ByteCountListener bcl) {
        byteCountListener.remove(bcl);
    }


    public synchronized static void addStateListener(StateListener sl) {
        if (!stateListener.contains(sl)) {
            stateListener.add(sl);
            if (mLaststate != null)
                sl.updateState(mLaststate, mLaststatemsg, mLastStateresid, mLastLevel, mLastIntent);
        }
    }

    private static int getLocalizedState(String state) {
        switch (state) {
            case "CONNECTING":
                return R.string.state_connecting;
            case "WAIT":
                return R.string.state_wait;
            case "AUTH":
                return R.string.state_auth;
            case "GET_CONFIG":
                return R.string.state_get_config;
            case "ASSIGN_IP":
                return R.string.state_assign_ip;
            case "ADD_ROUTES":
                return R.string.state_add_routes;
            case "CONNECTED":
                return R.string.state_connected;
            case "DISCONNECTED":
                return R.string.state_disconnected;
            case "RECONNECTING":
                return R.string.state_reconnecting;
            case "EXITING":
                return R.string.state_exiting;
            case "RESOLVE":
                return R.string.state_resolve;
            case "TCP_CONNECT":
                return R.string.state_tcp_connect;
            case "AUTH_PENDING":
                return R.string.state_auth_pending;
            default:
                return R.string.unknown_state;
        }

    }

    public static void updateStatePause(OpenVPNManagement.pauseReason pauseReason) {
        switch (pauseReason) {
            case noNetwork:
                VpnStatus.updateStateString("NONETWORK", "", R.string.state_nonetwork, ConnectionStatus.LEVEL_NONETWORK);
                break;
            case screenOff:
                VpnStatus.updateStateString("SCREENOFF", "", R.string.state_screenoff, ConnectionStatus.LEVEL_VPNPAUSED);
                break;
            case userPause:
                VpnStatus.updateStateString("USERPAUSE", "", R.string.state_userpause, ConnectionStatus.LEVEL_VPNPAUSED);
                break;
        }

    }

    private static ConnectionStatus getLevel(String state) {
        String[] noreplyet = {"CONNECTING", "WAIT", "RECONNECTING", "RESOLVE", "TCP_CONNECT"};
        String[] reply = {"AUTH", "GET_CONFIG", "ASSIGN_IP", "ADD_ROUTES", "AUTH_PENDING"};
        String[] connected = {"CONNECTED"};
        String[] notconnected = {"DISCONNECTED", "EXITING"};

        for (String x : noreplyet)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET;

        for (String x : reply)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_CONNECTING_SERVER_REPLIED;

        for (String x : connected)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_CONNECTED;

        for (String x : notconnected)
            if (state.equals(x))
                return ConnectionStatus.LEVEL_NOTCONNECTED;

        return ConnectionStatus.UNKNOWN_LEVEL;

    }


    public synchronized static void removeStateListener(StateListener sl) {
        stateListener.remove(sl);
    }


    synchronized public static LogItem[] getlogbuffer() {

        // The stoned way of java to return an array from a vector
        // brought to you by eclipse auto complete
        return logbuffer.toArray(new LogItem[logbuffer.size()]);

    }

    static void updateStateString(String state, String msg) {
        // We want to skip announcing that we are trying to get the configuration since
        // this is just polling until the user input has finished.be
        if (mLastLevel == ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT && state.equals("GET_CONFIG"))
            return;
        int rid = getLocalizedState(state);
        ConnectionStatus level = getLevel(state);
        updateStateString(state, msg, rid, level);
    }

    public synchronized static void updateStateString(String state, String msg, int resid, ConnectionStatus level)
    {
        updateStateString(state, msg, resid, level, null);
    }

    public synchronized static void updateStateString(String state, String msg, int resid, ConnectionStatus level, Intent intent) {
        // Workound for OpenVPN doing AUTH and wait and being connected
        // Simply ignore these state
        if (mLastLevel == ConnectionStatus.LEVEL_CONNECTED &&
                (state.equals("WAIT") || state.equals("AUTH"))) {
            newLogItem(new LogItem((LogLevel.DEBUG), String.format("Ignoring OpenVPN Status in CONNECTED state (%s->%s): %s", state, level.toString(), msg)));
            return;
        }

        mLaststate = state;
        mLaststatemsg = msg;
        mLastStateresid = resid;
        mLastLevel = level;
        mLastIntent = intent;


        for (StateListener sl : stateListener) {
            sl.updateState(state, msg, resid, level, intent);
        }
        //newLogItem(new LogItem((LogLevel.DEBUG), String.format("New OpenVPN Status (%s->%s): %s",state,level.toString(),msg)));
    }

    public static void logInfo(String message) {
        newLogItem(new LogItem(LogLevel.INFO, message));
    }

    public static void logDebug(String message) {
        newLogItem(new LogItem(LogLevel.DEBUG, message));
    }

    public static void logInfo(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.INFO, resourceId, args));
    }

    public static void logDebug(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.DEBUG, resourceId, args));
    }

    static void newLogItem(LogItem logItem) {
        newLogItem(logItem, false);
    }


    synchronized static void newLogItem(LogItem logItem, boolean cachedLine) {
        if (cachedLine) {
            logbuffer.addFirst(logItem);
        } else {
            logbuffer.addLast(logItem);
            if (mLogFileHandler != null) {
                Message m = mLogFileHandler.obtainMessage(LogFileHandler.LOG_MESSAGE, logItem);
                mLogFileHandler.sendMessage(m);
            }
        }

        if (logbuffer.size() > MAXLOGENTRIES + MAXLOGENTRIES / 2) {
            while (logbuffer.size() > MAXLOGENTRIES)
                logbuffer.removeFirst();
            if (mLogFileHandler != null)
                mLogFileHandler.sendMessage(mLogFileHandler.obtainMessage(LogFileHandler.TRIM_LOG_FILE));
        }

        for (LogListener ll : logListener) {
            ll.newLog(logItem);
        }
    }


    public static void logError(String msg) {
        newLogItem(new LogItem(LogLevel.ERROR, msg));

    }

    public static void logWarning(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.WARNING, resourceId, args));
    }

    public static void logWarning(String msg) {
        newLogItem(new LogItem(LogLevel.WARNING, msg));
    }


    public static void logError(int resourceId) {
        newLogItem(new LogItem(LogLevel.ERROR, resourceId));
    }

    public static void logError(int resourceId, Object... args) {
        newLogItem(new LogItem(LogLevel.ERROR, resourceId, args));
    }

    public static void logMessageOpenVPN(LogLevel level, int ovpnlevel, String message) {
        newLogItem(new LogItem(level, ovpnlevel, message));

    }


    public static synchronized void updateByteCount(long in, long out) {
        TrafficHistory.LastDiff diff = trafficHistory.add(in, out);

        for (ByteCountListener bcl : byteCountListener) {
            bcl.updateByteCount(in, out, diff.getDiffIn(), diff.getDiffOut());
        }
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.os.Build;
import androidx.core.util.Pair;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import de.blinkt.openvpn.VpnProfile;

//! Openvpn Config FIle Parser, probably not 100% accurate but close enough

// And remember, this is valid :)
// --<foo>
// bar
// </foo>
public class ConfigParser {


    public static final String CONVERTED_PROFILE = "converted Profile";
    final String[] unsupportedOptions = {"config",
            "tls-server"

    };
    // Ignore all scripts
    // in most cases these won't work and user who wish to execute scripts will
    // figure out themselves
    private final String[] ignoreOptions = {"tls-client",
            "allow-recursive-routing",
            "askpass",
            "auth-nocache",
            "up",
            "down",
            "route-up",
            "ipchange",
            "route-pre-down",
            "auth-user-pass-verify",
            "block-outside-dns",
            "client-cert-not-required",
            "dhcp-release",
            "dhcp-renew",
            "dh",
            "group",
            "ip-win32",
            "ifconfig-nowarn",
            "management-hold",
            "management",
            "management-client",
            "management-query-remote",
            "management-query-passwords",
            "management-query-proxy",
            "management-external-key",
            "management-forget-disconnect",
            "management-signal",
            "management-log-cache",
            "management-up-down",
            "management-client-user",
            "management-client-group",
            "pause-exit",
            "preresolve",
            "plugin",
            "machine-readable-output",
            "persist-key",
            "push",
            "register-dns",
            "route-delay",
            "route-gateway",
            "route-metric",
            "route-method",
            "status",
            "script-security",
            "show-net-up",
            "suppress-timestamps",
            "tap-sleep",
            "tmp-dir",
            "tun-ipv6",
            "topology",
            "user",
            "win-sys",
    };
    private final String[][] ignoreOptionsWithArg =
            {
                    {"setenv", "IV_GUI_VER"},
                    {"setenv", "IV_SSO"},
                    {"setenv", "IV_PLAT_VER"},
                    {"setenv", "IV_OPENVPN_GUI_VERSION"},
                    {"engine", "dynamic"},
                    {"setenv", "CLIENT_CERT"},
                    {"resolv-retry", "60"}
            };
    private final String[] connectionOptions = {
            "local",
            "remote",
            "float",
            "port",
            "connect-retry",
            "connect-timeout",
            "connect-retry-max",
            "link-mtu",
            "tun-mtu",
            "tun-mtu-extra",
            "fragment",
            "mtu-disc",
            "local-port",
            "remote-port",
            "bind",
            "nobind",
            "proto",
            "http-proxy",
            "http-proxy-retry",
            "http-proxy-timeout",
            "http-proxy-option",
            "socks-proxy",
            "socks-proxy-retry",
            "http-proxy-user-pass",
            "explicit-exit-notify",
    };
    private HashSet<String>  connectionOptionsSet = new HashSet<>(Arrays.asList(connectionOptions));

    private HashMap<String, Vector<Vector<String>>> options = new HashMap<>();
    private HashMap<String, Vector<String>> meta = new HashMap<String, Vector<String>>();
    private String auth_user_pass_file;

    static public void useEmbbedUserAuth(VpnProfile np, String inlinedata) {
        String data = VpnProfile.getEmbeddedContent(inlinedata);
        String[] parts = data.split("\n");
        if (parts.length >= 2) {
            np.mUsername = parts[0];
            np.mPassword = parts[1];
        }
    }

    static public void useEmbbedHttpAuth(Connection c, String inlinedata) {
        String data = VpnProfile.getEmbeddedContent(inlinedata);
        String[] parts = data.split("\n");
        if (parts.length >= 2) {
            c.mProxyAuthUser = parts[0];
            c.mProxyAuthPassword = parts[1];
            c.mUseProxyAuth = true;
        }
    }

    public void parseConfig(Reader reader) throws IOException, ConfigParseError {

        HashMap<String, String> optionAliases = new HashMap<>();
        optionAliases.put("server-poll-timeout", "timeout-connect");

        BufferedReader br = new BufferedReader(reader);

        int lineno = 0;
        try {
            while (true) {
                String line = br.readLine();
                lineno++;
                if (line == null)
                    break;

                if (lineno == 1) {
                    if ((line.startsWith("PK\003\004")
                            || (line.startsWith("PK\007\008")))) {
                        throw new ConfigParseError("Input looks like a ZIP Archive. Import is only possible for OpenVPN config files (.ovpn/.conf)");
                    }
                    if (line.startsWith("\uFEFF")) {
                        line = line.substring(1);
                    }
                }

                // Check for OpenVPN Access Server Meta information
                if (line.startsWith("# OVPN_ACCESS_SERVER_")) {
                    Vector<String> metaarg = parsemeta(line);
                    meta.put(metaarg.get(0), metaarg);
                    continue;
                }
                Vector<String> args = parseline(line);

                if (args.size() == 0)
                    continue;


                if (args.get(0).startsWith("--"))
                    args.set(0, args.get(0).substring(2));

                checkinlinefile(args, br);

                String optionname = args.get(0);
                if (optionAliases.get(optionname) != null)
                    optionname = optionAliases.get(optionname);

                if (!options.containsKey(optionname)) {
                    options.put(optionname, new Vector<Vector<String>>());
                }
                options.get(optionname).add(args);
            }
        } catch (java.lang.OutOfMemoryError memoryError) {
            throw new ConfigParseError("File too large to parse: " + memoryError.getLocalizedMessage());
        }
    }

    private Vector<String> parsemeta(String line) {
        String meta = line.split("#\\sOVPN_ACCESS_SERVER_", 2)[1];
        String[] parts = meta.split("=", 2);
        Vector<String> rval = new Vector<String>();
        Collections.addAll(rval, parts);
        return rval;

    }

    private void checkinlinefile(Vector<String> args, BufferedReader br) throws IOException, ConfigParseError {
        String arg0 = args.get(0).trim();
        // CHeck for <foo>
        if (arg0.startsWith("<") && arg0.endsWith(">")) {
            String argname = arg0.substring(1, arg0.length() - 1);
            String inlinefile = VpnProfile.INLINE_TAG;

            String endtag = String.format("</%s>", argname);
            do {
                String line = br.readLine();
                if (line == null) {
                    throw new ConfigParseError(String.format("No endtag </%s> for starttag <%s> found", argname, argname));
                }
                if (line.trim().equals(endtag))
                    break;
                else {
                    inlinefile += line;
                    inlinefile += "\n";
                }
            } while (true);

            if (inlinefile.endsWith("\n"))
                inlinefile = inlinefile.substring(0, inlinefile.length() - 1);

            args.clear();
            args.add(argname);
            args.add(inlinefile);
        }

    }

    public String getAuthUserPassFile() {
        return auth_user_pass_file;
    }

    private boolean space(char c) {
        // I really hope nobody is using zero bytes inside his/her config file
        // to sperate parameter but here we go:
        return Character.isWhitespace(c) || c == '\0';

    }

    // adapted openvpn's parse function to java
    private Vector<String> parseline(String line) throws ConfigParseError {
        Vector<String> parameters = new Vector<String>();

        if (line.length() == 0)
            return parameters;


        linestate state = linestate.initial;
        boolean backslash = false;
        char out = 0;

        int pos = 0;
        String currentarg = "";

        do {
            // Emulate the c parsing ...
            char in;
            if (pos < line.length())
                in = line.charAt(pos);
            else
                in = '\0';

            if (!backslash && in == '\\' && state != linestate.readin_single_quote) {
                backslash = true;
            } else {
                if (state == linestate.initial) {
                    if (!space(in)) {
                        if (in == ';' || in == '#') /* comment */
                            break;
                        if (!backslash && in == '\"')
                            state = linestate.reading_quoted;
                        else if (!backslash && in == '\'')
                            state = linestate.readin_single_quote;
                        else {
                            out = in;
                            state = linestate.reading_unquoted;
                        }
                    }
                } else if (state == linestate.reading_unquoted) {
                    if (!backslash && space(in))
                        state = linestate.done;
                    else
                        out = in;
                } else if (state == linestate.reading_quoted) {
                    if (!backslash && in == '\"')
                        state = linestate.done;
                    else
                        out = in;
                } else if (state == linestate.readin_single_quote) {
                    if (in == '\'')
                        state = linestate.done;
                    else
                        out = in;
                }

                if (state == linestate.done) {
                    /* ASSERT (parm_len > 0); */
                    state = linestate.initial;
                    parameters.add(currentarg);
                    currentarg = "";
                    out = 0;
                }

                if (backslash && out != 0) {
                    if (!(out == '\\' || out == '\"' || space(out))) {
                        throw new ConfigParseError("Options warning: Bad backslash ('\\') usage");
                    }
                }
                backslash = false;
            }

            /* store parameter character */
            if (out != 0) {
                currentarg += out;
            }
        } while (pos++ < line.length());

        return parameters;
    }

    // This method is far too long
    @SuppressWarnings("ConstantConditions")
    public VpnProfile convertProfile() throws ConfigParseError, IOException {
        boolean noauthtypeset = true;
        VpnProfile np = new VpnProfile(CONVERTED_PROFILE);
        // Pull, client, tls-client
        np.clearDefaults();

        if (options.containsKey("client") || options.containsKey("pull")) {
            np.mUsePull = true;
            options.remove("pull");
            options.remove("client");
        }

        Vector<String> secret = getOption("secret", 1, 2);
        if (secret != null) {
            np.mAuthenticationType = VpnProfile.TYPE_STATICKEYS;
            noauthtypeset = false;
            np.mUseTLSAuth = true;
            np.mTLSAuthFilename = secret.get(1);
            if (secret.size() == 3)
                np.mTLSAuthDirection = secret.get(2);

        }

        Vector<Vector<String>> routes = getAllOption("route", 1, 4);
        if (routes != null) {
            String routeopt = "";
            String routeExcluded = "";
            for (Vector<String> route : routes) {
                String netmask = "255.255.255.255";
                String gateway = "vpn_gateway";

                if (route.size() >= 3)
                    netmask = route.get(2);
                if (route.size() >= 4)
                    gateway = route.get(3);

                String net = route.get(1);
                try {
                    CIDRIP cidr = new CIDRIP(net, netmask);
                    if (gateway.equals("net_gateway"))
                        routeExcluded += cidr.toString() + " ";
                    else
                        routeopt += cidr.toString() + " ";
                } catch (ArrayIndexOutOfBoundsException aioob) {
                    throw new ConfigParseError("Could not parse netmask of route " + netmask);
                } catch (NumberFormatException ne) {


                    throw new ConfigParseError("Could not parse netmask of route " + netmask);
                }

            }
            np.mCustomRoutes = routeopt;
            np.mExcludedRoutes = routeExcluded;
        }

        Vector<Vector<String>> routesV6 = getAllOption("route-ipv6", 1, 4);
        if (routesV6 != null) {
            String customIPv6Routes = "";
            for (Vector<String> route : routesV6) {
                customIPv6Routes += route.get(1) + " ";
            }

            np.mCustomRoutesv6 = customIPv6Routes;
        }

        Vector<String> routeNoPull = getOption("route-nopull", 0, 0);
        if (routeNoPull != null)
            np.mRoutenopull = true;

        // Also recognize tls-auth [inline] direction ...
        Vector<Vector<String>> tlsauthoptions = getAllOption("tls-auth", 1, 2);
        if (tlsauthoptions != null) {
            for (Vector<String> tlsauth : tlsauthoptions) {
                if (tlsauth != null) {
                    if (!tlsauth.get(1).equals("[inline]")) {
                        np.mTLSAuthFilename = tlsauth.get(1);
                        np.mUseTLSAuth = true;
                    }
                    if (tlsauth.size() == 3)
                        np.mTLSAuthDirection = tlsauth.get(2);
                }
            }
        }

        Vector<String> direction = getOption("key-direction", 1, 1);
        if (direction != null)
            np.mTLSAuthDirection = direction.get(1);

        for (String crypt: new String[]{"tls-crypt", "tls-crypt-v2"}) {
            Vector<String> tlscrypt = getOption(crypt, 1, 1);
            if (tlscrypt != null) {
                np.mUseTLSAuth = true;
                np.mTLSAuthFilename = tlscrypt.get(1);
                np.mTLSAuthDirection = crypt;
            }
        }

        Vector<Vector<String>> defgw = getAllOption("redirect-gateway", 0, 7);
        if (defgw != null) {
            checkRedirectParameters(np, defgw, true);
        }

        Vector<Vector<String>> redirectPrivate = getAllOption("redirect-private", 0, 5);
        if (redirectPrivate != null) {
            checkRedirectParameters(np, redirectPrivate, false);
        }
        Vector<String> dev = getOption("dev", 1, 1);
        Vector<String> devtype = getOption("dev-type", 1, 1);

        if ((devtype != null && devtype.get(1).equals("tun")) ||
                (dev != null && dev.get(1).startsWith("tun")) ||
                (devtype == null && dev == null)) {
            //everything okay
        } else {
            throw new ConfigParseError("Sorry. Only tun mode is supported. See the FAQ for more detail");
        }

        Vector<String> mssfix = getOption("mssfix", 0, 2);

        if (mssfix != null) {
            if (mssfix.size() >= 2) {
                try {
                    np.mMssFix = Integer.parseInt(mssfix.get(1));
                } catch (NumberFormatException e) {
                    throw new ConfigParseError("Argument to --mssfix has to be an integer");
                }
            } else {
                np.mMssFix = 1450; // OpenVPN default size
            }
            // Ignore mtu argument of OpenVPN3 and report error otherwise
            if (mssfix.size() >= 3 && !(mssfix.get(2).equals("mtu"))) {
                throw new ConfigParseError("Second argument to --mssfix unkonwn");
            }
        }


        Vector<String> tunmtu = getOption("tun-mtu", 1, 1);

        if (tunmtu != null) {
            try {
                np.mTunMtu = Integer.parseInt(tunmtu.get(1));
            } catch (NumberFormatException e) {
                throw new ConfigParseError("Argument to --tun-mtu has to be an integer");
            }
        }


        Vector<String> mode = getOption("mode", 1, 1);
        if (mode != null) {
            if (!mode.get(1).equals("p2p"))
                throw new ConfigParseError("Invalid mode for --mode specified, need p2p");
        }


        Vector<Vector<String>> dhcpoptions = getAllOption("dhcp-option", 2, 2);
        if (dhcpoptions != null) {
            for (Vector<String> dhcpoption : dhcpoptions) {
                String type = dhcpoption.get(1);
                String arg = dhcpoption.get(2);
                if (type.equals("DOMAIN")) {
                    np.mSearchDomain = dhcpoption.get(2);
                } else if (type.equals("DNS")) {
                    np.mOverrideDNS = true;
                    if (np.mDNS1.equals(VpnProfile.DEFAULT_DNS1))
                        np.mDNS1 = arg;
                    else
                        np.mDNS2 = arg;
                }
            }
        }

        Vector<String> ifconfig = getOption("ifconfig", 2, 2);
        if (ifconfig != null) {
            try {
                CIDRIP cidr = new CIDRIP(ifconfig.get(1), ifconfig.get(2));
                np.mIPv4Address = cidr.toString();
            } catch (NumberFormatException nfe) {
                throw new ConfigParseError("Could not pase ifconfig IP address: " + nfe.getLocalizedMessage());
            }

        }

        if (getOption("remote-random-hostname", 0, 0) != null)
            np.mUseRandomHostname = true;

        if (getOption("float", 0, 0) != null)
            np.mUseFloat = true;

        if (getOption("comp-lzo", 0, 1) != null)
            np.mUseLzo = true;

        Vector<String> cipher = getOption("cipher", 1, 1);
        if (cipher != null)
            np.mCipher = cipher.get(1);

        Vector<String> auth = getOption("auth", 1, 1);
        if (auth != null)
            np.mAuth = auth.get(1);


        Vector<String> ca = getOption("ca", 1, 1);
        if (ca != null) {
            np.mCaFilename = ca.get(1);
        }

        Vector<String> cert = getOption("cert", 1, 1);
        if (cert != null) {
            np.mClientCertFilename = cert.get(1);
            np.mAuthenticationType = VpnProfile.TYPE_CERTIFICATES;
            noauthtypeset = false;
        }
        Vector<String> key = getOption("key", 1, 1);
        if (key != null)
            np.mClientKeyFilename = key.get(1);

        Vector<String> pkcs12 = getOption("pkcs12", 1, 1);
        if (pkcs12 != null) {
            np.mPKCS12Filename = pkcs12.get(1);
            np.mAuthenticationType = VpnProfile.TYPE_KEYSTORE;
            noauthtypeset = false;
        }

        Vector<String> cryptoapicert = getOption("cryptoapicert", 1, 1);
        if (cryptoapicert != null) {
            np.mAuthenticationType = VpnProfile.TYPE_KEYSTORE;
            noauthtypeset = false;
        }

        Vector<String> compatnames = getOption("compat-names", 1, 2);
        Vector<String> nonameremapping = getOption("no-name-remapping", 1, 1);
        Vector<String> tlsremote = getOption("tls-remote", 1, 1);
        if (tlsremote != null) {
            np.mRemoteCN = tlsremote.get(1);
            np.mCheckRemoteCN = true;
            np.mX509AuthType = VpnProfile.X509_VERIFY_TLSREMOTE;

            if ((compatnames != null && compatnames.size() > 2) ||
                    (nonameremapping != null))
                np.mX509AuthType = VpnProfile.X509_VERIFY_TLSREMOTE_COMPAT_NOREMAPPING;
        }

        Vector<String> verifyx509name = getOption("verify-x509-name", 1, 2);
        if (verifyx509name != null) {
            np.mRemoteCN = verifyx509name.get(1);
            np.mCheckRemoteCN = true;
            if (verifyx509name.size() > 2) {
                if (verifyx509name.get(2).equals("name"))
                    np.mX509AuthType = VpnProfile.X509_VERIFY_TLSREMOTE_RDN;
                else if (verifyx509name.get(2).equals("subject"))
                    np.mX509AuthType = VpnProfile.X509_VERIFY_TLSREMOTE_DN;
                else if (verifyx509name.get(2).equals("name-prefix"))
                    np.mX509AuthType = VpnProfile.X509_VERIFY_TLSREMOTE_RDN_PREFIX;
                else
                    throw new ConfigParseError("Unknown parameter to verify-x509-name: " + verifyx509name.get(2));
            } else {
                np.mX509AuthType = VpnProfile.X509_VERIFY_TLSREMOTE_DN;
            }

        }

        Vector<String> x509usernamefield = getOption("x509-username-field", 1, 1);
        if (x509usernamefield != null) {
            np.mx509UsernameField = x509usernamefield.get(1);
        }


        Vector<String> verb = getOption("verb", 1, 1);
        if (verb != null) {
            np.mVerb = verb.get(1);
        }


        if (getOption("nobind", 0, 0) != null)
            np.mNobind = true;

        if (getOption("persist-tun", 0, 0) != null)
            np.mPersistTun = true;

        if (getOption("push-peer-info", 0, 0) != null)
            np.mPushPeerInfo = true;

        Vector<String> connectretry = getOption("connect-retry", 1, 2);
        if (connectretry != null) {
            np.mConnectRetry = connectretry.get(1);
            if (connectretry.size() > 2)
                np.mConnectRetryMaxTime = connectretry.get(2);
        }

        Vector<String> connectretrymax = getOption("connect-retry-max", 1, 1);
        if (connectretrymax != null)
            np.mConnectRetryMax = connectretrymax.get(1);

        Vector<Vector<String>> remotetls = getAllOption("remote-cert-tls", 1, 1);
        if (remotetls != null)
            if (remotetls.get(0).get(1).equals("server"))
                np.mExpectTLSCert = true;
            else
                options.put("remotetls", remotetls);

        Vector<String> authuser = getOption("auth-user-pass", 0, 1);

        if (authuser != null) {
            if (noauthtypeset) {
                np.mAuthenticationType = VpnProfile.TYPE_USERPASS;
            } else if (np.mAuthenticationType == VpnProfile.TYPE_CERTIFICATES) {
                np.mAuthenticationType = VpnProfile.TYPE_USERPASS_CERTIFICATES;
            } else if (np.mAuthenticationType == VpnProfile.TYPE_KEYSTORE) {
                np.mAuthenticationType = VpnProfile.TYPE_USERPASS_KEYSTORE;
            }
            if (authuser.size() > 1) {
                if (!authuser.get(1).startsWith(VpnProfile.INLINE_TAG))
                    auth_user_pass_file = authuser.get(1);
                np.mUsername = null;
                useEmbbedUserAuth(np, authuser.get(1));
            }
        }

        Vector<String> authretry = getOption("auth-retry", 1, 1);
        if (authretry != null) {
            if (authretry.get(1).equals("none"))
                np.mAuthRetry = VpnProfile.AUTH_RETRY_NONE_FORGET;
            else if (authretry.get(1).equals("nointeract"))
                np.mAuthRetry = VpnProfile.AUTH_RETRY_NOINTERACT;
            else if (authretry.get(1).equals("interact"))
                np.mAuthRetry = VpnProfile.AUTH_RETRY_NOINTERACT;
            else
                throw new ConfigParseError("Unknown parameter to auth-retry: " + authretry.get(2));
        }


        Vector<String> crlfile = getOption("crl-verify", 1, 2);
        if (crlfile != null) {
            // If the 'dir' parameter is present just add it as custom option ..
            if (crlfile.size() == 3 && crlfile.get(2).equals("dir"))
                np.mCustomConfigOptions += join(" ", crlfile) + "\n";
            else
                // Save the filename for the config converter to add later
                np.mCrlFilename = crlfile.get(1);

        }


        Pair<Connection, Connection[]> conns = parseConnectionOptions(null);
        np.mConnections = conns.second;

        Vector<Vector<String>> connectionBlocks = getAllOption("connection", 1, 1);

        if (np.mConnections.length > 0 && connectionBlocks != null) {
            throw new ConfigParseError("Using a <connection> block and --remote is not allowed.");
        }

        if (connectionBlocks != null) {
            np.mConnections = new Connection[connectionBlocks.size()];

            int connIndex = 0;
            for (Vector<String> conn : connectionBlocks) {
                Pair<Connection, Connection[]> connectionBlockConnection =
                        parseConnection(conn.get(1), conns.first);

                if (connectionBlockConnection.second.length != 1)
                    throw new ConfigParseError("A <connection> block must have exactly one remote");
                np.mConnections[connIndex] = connectionBlockConnection.second[0];
                connIndex++;
            }
        }
        if (getOption("remote-random", 0, 0) != null)
            np.mRemoteRandom = true;

        Vector<String> protoforce = getOption("proto-force", 1, 1);
        if (protoforce != null) {
            boolean disableUDP;
            String protoToDisable = protoforce.get(1);
            if (protoToDisable.equals("udp"))
                disableUDP = true;
            else if (protoToDisable.equals("tcp"))
                disableUDP = false;
            else
                throw new ConfigParseError(String.format("Unknown protocol %s in proto-force", protoToDisable));

            for (Connection conn : np.mConnections)
                if (conn.mUseUdp == disableUDP)
                    conn.mEnabled = false;
        }

        // Parse OpenVPN Access Server extra
        for (String as_name_directive: new String[]{"PROFILE", "FRIENDLY_NAME"}) {
            Vector<String> friendlyname = meta.get(as_name_directive);
            if (friendlyname != null && friendlyname.size() > 1)
                np.mName = friendlyname.get(1);
        }


        Vector<String> ocusername = meta.get("USERNAME");
        if (ocusername != null && ocusername.size() > 1)
            np.mUsername = ocusername.get(1);

        checkIgnoreAndInvalidOptions(np);
        fixup(np);

        return np;
    }

    private String join(String s, Vector<String> str) {
        if (Build.VERSION.SDK_INT > 26)
            return String.join(s, str);
        else
            return TextUtils.join(s, str);
    }

    private Pair<Connection, Connection[]> parseConnection(String connection, Connection defaultValues) throws IOException, ConfigParseError {
        // Parse a connection Block as a new configuration file


        ConfigParser connectionParser = new ConfigParser();
        StringReader reader = new StringReader(connection.substring(VpnProfile.INLINE_TAG.length()));
        connectionParser.parseConfig(reader);

        Pair<Connection, Connection[]> conn = connectionParser.parseConnectionOptions(defaultValues);

        return conn;
    }

    private Pair<Connection, Connection[]> parseConnectionOptions(Connection connDefault) throws ConfigParseError {
        Connection conn;
        if (connDefault != null)
            try {
                conn = connDefault.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return null;
            }
        else
            conn = new Connection();

        Vector<String> port = getOption("port", 1, 1);
        if (port != null) {
            conn.mServerPort = port.get(1);
        }

        Vector<String> rport = getOption("rport", 1, 1);
        if (rport != null) {
            conn.mServerPort = rport.get(1);
        }

        Vector<String> proto = getOption("proto", 1, 1);
        if (proto != null) {
            conn.mUseUdp = isUdpProto(proto.get(1));
        }

        Vector<String> connectTimeout = getOption("connect-timeout", 1, 1);
        if (connectTimeout != null) {
            try {
                conn.mConnectTimeout = Integer.parseInt(connectTimeout.get(1));
            } catch (NumberFormatException nfe) {
                throw new ConfigParseError(String.format("Argument to connect-timeout (%s) must to be an integer: %s",
                        connectTimeout.get(1), nfe.getLocalizedMessage()));

            }
        }

        Vector<String> proxy = getOption("socks-proxy", 1, 2);
        if (proxy == null)
            proxy = getOption("http-proxy", 2, 2);

        if (proxy != null) {
            if (proxy.get(0).equals("socks-proxy")) {
                conn.mProxyType = Connection.ProxyType.SOCKS5;
                // socks defaults to 1080, http always sets port
                conn.mProxyPort = "1080";
            } else {
                conn.mProxyType = Connection.ProxyType.HTTP;
            }

            conn.mProxyName = proxy.get(1);
            if (proxy.size() >= 3)
                conn.mProxyPort = proxy.get(2);
        }

        Vector<String> httpproxyauthhttp = getOption("http-proxy-user-pass", 1, 1);
        if (httpproxyauthhttp != null)
            useEmbbedHttpAuth(conn, httpproxyauthhttp.get(1));


        // Parse remote config
        Vector<Vector<String>> remotes = getAllOption("remote", 1, 3);



        Vector <String> optionsToRemove = new Vector<>();
        // Assume that we need custom options if connectionDefault are set or in the connection specific set
        for (Map.Entry<String, Vector<Vector<String>>> option : options.entrySet()) {
            if (connDefault != null || connectionOptionsSet.contains(option.getKey())) {
                conn.mCustomConfiguration += getOptionStrings(option.getValue());
                optionsToRemove.add(option.getKey());
            }
        }
        for (String o: optionsToRemove)
            options.remove(o);

        if (!(conn.mCustomConfiguration == null || "".equals(conn.mCustomConfiguration.trim())))
            conn.mUseCustomConfig = true;

        // Make remotes empty to simplify code
        if (remotes == null)
            remotes = new Vector<Vector<String>>();

        Connection[] connections = new Connection[remotes.size()];


        int i = 0;
        for (Vector<String> remote : remotes) {
            try {
                connections[i] = conn.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            switch (remote.size()) {
                case 4:
                    connections[i].mUseUdp = isUdpProto(remote.get(3));
                case 3:
                    connections[i].mServerPort = remote.get(2);
                case 2:
                    connections[i].mServerName = remote.get(1);
            }
            i++;
        }

        return Pair.create(conn, connections);

    }

    private void checkRedirectParameters(VpnProfile np, Vector<Vector<String>> defgw, boolean defaultRoute) {

        boolean noIpv4 = false;
        if (defaultRoute)

            for (Vector<String> redirect : defgw)
                for (int i = 1; i < redirect.size(); i++) {
                    if (redirect.get(i).equals("block-local"))
                        np.mAllowLocalLAN = false;
                    else if (redirect.get(i).equals("unblock-local"))
                        np.mAllowLocalLAN = true;
                    else if (redirect.get(i).equals("!ipv4"))
                        noIpv4 = true;
                    else if (redirect.get(i).equals("ipv6"))
                        np.mUseDefaultRoutev6 = true;
                }
        if (defaultRoute && !noIpv4)
            np.mUseDefaultRoute = true;
    }

    private boolean isUdpProto(String proto) throws ConfigParseError {
        boolean isudp;
        if (proto.equals("udp") || proto.equals("udp4") || proto.equals("udp6"))
            isudp = true;
        else if (proto.equals("tcp-client") ||
                proto.equals("tcp") ||
                proto.equals("tcp4") ||
                proto.endsWith("tcp4-client") ||
                proto.equals("tcp6") ||
                proto.endsWith("tcp6-client"))
            isudp = false;
        else
            throw new ConfigParseError("Unsupported option to --proto " + proto);
        return isudp;
    }

    private void checkIgnoreAndInvalidOptions(VpnProfile np) throws ConfigParseError {
        for (String option : unsupportedOptions)
            if (options.containsKey(option))
                throw new ConfigParseError(String.format("Unsupported Option %s encountered in config file. Aborting", option));

        for (String option : ignoreOptions)
            // removing an item which is not in the map is no error
            options.remove(option);


        boolean customOptions=false;
        for (Vector<Vector<String>>  option: options.values())
        {
            for (Vector<String> optionsline : option) {
                if (!ignoreThisOption(optionsline)) {
                    customOptions = true;
                }
            }
        }
        if (customOptions) {
            np.mCustomConfigOptions = "# These options found in the config file do not map to config settings:\n"
                    + np.mCustomConfigOptions;

            for (Vector<Vector<String>> option : options.values()) {

                np.mCustomConfigOptions += getOptionStrings(option);

            }
            np.mUseCustomConfig = true;

        }
    }

    boolean ignoreThisOption(Vector<String> option) {
        for (String[] ignoreOption : ignoreOptionsWithArg) {

            if (option.size() < ignoreOption.length)
                continue;

            boolean ignore = true;
            for (int i = 0; i < ignoreOption.length; i++) {
                if (!ignoreOption[i].equals(option.get(i)))
                    ignore = false;
            }
            if (ignore)
                return true;

        }
        return false;
    }

    //! Generate options for custom options
    private String getOptionStrings(Vector<Vector<String>> option) {
        String custom = "";
        for (Vector<String> optionsline : option) {
            if (!ignoreThisOption(optionsline)) {
                // Check if option had been inlined and inline again
                if (optionsline.size() == 2 &&
                        "extra-certs".equals(optionsline.get(0))) {
                    custom += VpnProfile.insertFileData(optionsline.get(0), optionsline.get(1));
                } else {
                    for (String arg : optionsline)
                        custom += VpnProfile.openVpnEscape(arg) + " ";
                    custom += "\n";
                }
            }
        }
        return custom;
    }

    private void fixup(VpnProfile np) {
        if (np.mRemoteCN.equals(np.mServerName)) {
            np.mRemoteCN = "";
        }
    }

    private Vector<String> getOption(String option, int minarg, int maxarg) throws ConfigParseError {
        Vector<Vector<String>> alloptions = getAllOption(option, minarg, maxarg);
        if (alloptions == null)
            return null;
        else
            return alloptions.lastElement();
    }

    private Vector<Vector<String>> getAllOption(String option, int minarg, int maxarg) throws ConfigParseError {
        Vector<Vector<String>> args = options.get(option);
        if (args == null)
            return null;

        for (Vector<String> optionline : args)

            if (optionline.size() < (minarg + 1) || optionline.size() > maxarg + 1) {
                String err = String.format(Locale.getDefault(), "Option %s has %d parameters, expected between %d and %d",
                        option, optionline.size() - 1, minarg, maxarg);
                throw new ConfigParseError(err);
            }
        options.remove(option);
        return args;
    }

    enum linestate {
        initial,
        readin_single_quote, reading_quoted, reading_unquoted, done
    }

    public static class ConfigParseError extends Exception {
        private static final long serialVersionUID = -60L;

        public ConfigParseError(String msg) {
            super(msg);
        }
    }

}






package de.blinkt.openvpn.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PropertiesService {

    private static final String DOWNLOADED_DATA_KEY = "downloaded_data";
    private static final String UPLOADED_DATA_KEY = "uploaded_data";
    private static SharedPreferences prefs;

    private synchronized static SharedPreferences getPrefs(Context context) {
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return prefs;
    }

    public static long getDownloaded(Context context) {
        return getPrefs(context).getLong(DOWNLOADED_DATA_KEY, 0);
    }

    public static void setDownloaded(Context context, long count) {
        getPrefs(context).edit().putLong(DOWNLOADED_DATA_KEY, count).apply();
    }

    public static long getUploaded(Context context) {
        return getPrefs(context).getLong(UPLOADED_DATA_KEY, 0);
    }

    public static void setUploaded(Context context, long count) {
        getPrefs(context).edit().putLong(UPLOADED_DATA_KEY, count).apply();
    }
}


package de.blinkt.openvpn.utils;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import de.blinkt.openvpn.core.OpenVPNService;

public class TotalTraffic {

    public static final String TRAFFIC_ACTION = "traffic_action";

    public static final String DOWNLOAD_ALL = "download_all";
    public static final String DOWNLOAD_SESSION = "download_session";
    public static final String UPLOAD_ALL = "upload_all";
    public static final String UPLOAD_SESSION = "upload_session";

    public static long inTotal;
    public static long outTotal;


    public static void calcTraffic(Context context, long in, long out, long diffIn, long diffOut) {
        List<String> totalTraffic = getTotalTraffic(context, diffIn, diffOut);

        Intent traffic = new Intent();
        traffic.setAction(TRAFFIC_ACTION);
        traffic.putExtra(DOWNLOAD_ALL, totalTraffic.get(0));
        traffic.putExtra(DOWNLOAD_SESSION, OpenVPNService.humanReadableByteCount(in, false, context.getResources()));
        traffic.putExtra(UPLOAD_ALL, totalTraffic.get(1));
        traffic.putExtra(UPLOAD_SESSION, OpenVPNService.humanReadableByteCount(out, false, context.getResources()));

        context.sendBroadcast(traffic);
    }

    public static List<String> getTotalTraffic(Context context) {
        return getTotalTraffic(context, 0, 0);
    }

    public static List<String> getTotalTraffic(Context context, long in, long out) {
        List<String> totalTraffic = new ArrayList<String>();

        if (inTotal == 0)
            inTotal = PropertiesService.getDownloaded(context);

        if (outTotal == 0)
            outTotal = PropertiesService.getUploaded(context);

        inTotal = inTotal + in;
        outTotal = outTotal + out;

        totalTraffic.add(OpenVPNService.humanReadableByteCount(inTotal, false, context.getResources()));
        totalTraffic.add(OpenVPNService.humanReadableByteCount(outTotal, false, context.getResources()));

        return totalTraffic;
    }

    public static void saveTotal(Context context) {
        if (inTotal != 0)
            PropertiesService.setDownloaded(context, inTotal);

        if (outTotal != 0)
            PropertiesService.setUploaded(context, outTotal);
    }

    public static void clearTotal(Context context) {
        inTotal = 0;
        PropertiesService.setDownloaded(context, inTotal);
        outTotal = 0;
        PropertiesService.setUploaded(context, outTotal);
    }

}


/*
 * Copyright (c) 2012-2018 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.api;

import android.annotation.TargetApi;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.Connection;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AppRestrictions {
    public static final String PROFILE_CREATOR = "de.blinkt.openvpn.api.AppRestrictions";
    final static int CONFIG_VERSION = 1;
    static boolean alreadyChecked = false;
    private static AppRestrictions mInstance;
    private RestrictionsManager mRestrictionsMgr;
    private BroadcastReceiver mRestrictionsReceiver;

    private AppRestrictions(Context c) {

    }

    public static AppRestrictions getInstance(Context c) {
        if (mInstance == null)
            mInstance = new AppRestrictions(c);
        return mInstance;
    }

    private void addChangesListener(Context c) {
        IntentFilter restrictionsFilter =
                new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED);
        mRestrictionsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                applyRestrictions(context);
            }
        };
        c.registerReceiver(mRestrictionsReceiver, restrictionsFilter);
    }

    private void removeChangesListener(Context c) {
        c.unregisterReceiver(mRestrictionsReceiver);
    }

    private String hashConfig(String config) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA1");
            byte utf8_bytes[] = config.getBytes();
            digest.update(utf8_bytes, 0, utf8_bytes.length);
            return new BigInteger(1, digest.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void applyRestrictions(Context c) {
        mRestrictionsMgr = (RestrictionsManager) c.getSystemService(Context.RESTRICTIONS_SERVICE);
        if (mRestrictionsMgr == null)
            return;
        Bundle restrictions = mRestrictionsMgr.getApplicationRestrictions();
        if (restrictions == null)
            return;

        String configVersion = restrictions.getString("version", "(not set)");
        try {
            if (Integer.parseInt(configVersion) != CONFIG_VERSION)
                throw new NumberFormatException("Wrong version");
        } catch (NumberFormatException nex) {
            if ("(not set)".equals(configVersion))
                // Ignore error if no version present
                return;
            VpnStatus.logError(String.format(Locale.US, "App restriction version %s does not match expected version %d", configVersion, CONFIG_VERSION));
            return;
        }
        Parcelable[] profileList = restrictions.getParcelableArray(("vpn_configuration_list"));
        if (profileList == null) {
            VpnStatus.logError("App restriction does not contain a profile list (vpn_configuration_list)");
            return;
        }

        Set<String> provisionedUuids = new HashSet<>();

        ProfileManager pm = ProfileManager.getInstance(c);
        for (Parcelable profile : profileList) {
            if (!(profile instanceof Bundle)) {
                VpnStatus.logError("App restriction profile has wrong type");
                continue;
            }
            Bundle p = (Bundle) profile;

            String uuid = p.getString("uuid");
            String ovpn = p.getString("ovpn");
            String name = p.getString("name");

            if (uuid == null || ovpn == null || name == null) {
                VpnStatus.logError("App restriction profile misses uuid, ovpn or name key");
                continue;
            }

            String ovpnHash = hashConfig(ovpn);

            provisionedUuids.add(uuid.toLowerCase(Locale.ENGLISH));
            // Check if the profile already exists
            VpnProfile vpnProfile = ProfileManager.get(c, uuid);


            if (vpnProfile != null) {
                // Profile exists, check if need to update it
                if (ovpnHash.equals(vpnProfile.importedProfileHash))
                    // not modified skip to next profile
                    continue;

            }
            addProfile(c, ovpn, uuid, name, vpnProfile);
        }

        Vector<VpnProfile> profilesToRemove = new Vector<>();
        // get List of all managed profiles
        for (VpnProfile vp: pm.getProfiles())
        {
            if (PROFILE_CREATOR.equals(vp.mProfileCreator)) {
                if (!provisionedUuids.contains(vp.getUUIDString()))
                    profilesToRemove.add(vp);
            }
        }
        for (VpnProfile vp: profilesToRemove) {
            VpnStatus.logInfo("Remove with uuid: %s and name: %s since it is no longer in the list of managed profiles");
            pm.removeProfile(c, vp);
        }

    }

    private String prepare(String config) {
        String newLine = System.getProperty("line.separator");
        if (!config.contains(newLine)&& !config.contains(" ")) {
            try {
                byte[] decoded = android.util.Base64.decode(config.getBytes(), android.util.Base64.DEFAULT);
                config  = new String(decoded);
                return config; 
            } catch(IllegalArgumentException e) {
               
            }
        }
        return config;
    };
    
    private void addProfile(Context c, String config, String uuid, String name, VpnProfile vpnProfile) {
        config  = prepare(config);
        ConfigParser cp = new ConfigParser();
        try {
            cp.parseConfig(new StringReader(config));
            VpnProfile vp = cp.convertProfile();
            vp.mProfileCreator = PROFILE_CREATOR;

            // We don't want provisioned profiles to be editable
            vp.mUserEditable = false;

            vp.mName = name;
            vp.setUUID(UUID.fromString(uuid));
            vp.importedProfileHash = hashConfig(config);

            ProfileManager pm = ProfileManager.getInstance(c);

            if (vpnProfile != null) {
                vp.mVersion = vpnProfile.mVersion + 1;
                vp.mAlias = vpnProfile.mAlias;
            }

            // The add method will replace any older profiles with the same UUID
            pm.addProfile(vp);
            pm.saveProfile(c, vp);
            pm.saveProfileList(c);

        } catch (ConfigParser.ConfigParseError | IOException | IllegalArgumentException e) {
            VpnStatus.logException("Error during import of managed profile", e);
        }
    }

    public void checkRestrictions(Context c) {
        if (alreadyChecked) {
            return;
        }
        alreadyChecked = true;
        addChangesListener(c);
        applyRestrictions(c);
    }

    public void pauseCheckRestrictions(Context c)
    {
        removeChangesListener(c);
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;

import java.util.HashSet;
import java.util.Set;

import de.blinkt.openvpn.core.Preferences;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class ExternalAppDatabase {

	Context mContext;
	
	public ExternalAppDatabase(Context c) {
		mContext =c;
	}

	private final String PREFERENCES_KEY = "allowed_apps";

	boolean isAllowed(String packagename) {
		Set<String> allowedapps = getExtAppList();

		return allowedapps.contains(packagename); 

	}

	public Set<String> getExtAppList() {
		SharedPreferences prefs = Preferences.getDefaultSharedPreferences(mContext);
        return prefs.getStringSet(PREFERENCES_KEY, new HashSet<String>());
	}
	
	public void addApp(String packagename)
	{
		Set<String> allowedapps = getExtAppList();
		allowedapps.add(packagename);
		saveExtAppList(allowedapps);
	}

	private void saveExtAppList( Set<String> allowedapps) {
		SharedPreferences prefs = Preferences.getDefaultSharedPreferences(mContext);
		Editor prefedit = prefs.edit();

		// Workaround for bug
		prefedit.putStringSet(PREFERENCES_KEY, allowedapps);
		int counter = prefs.getInt("counter", 0);
		prefedit.putInt("counter", counter + 1);
		prefedit.apply();
	}
	
	public void clearAllApiApps() {
		saveExtAppList(new HashSet<String>());
	}

	public void removeApp(String packagename) {
		Set<String> allowedapps = getExtAppList();
		allowedapps.remove(packagename);
		saveExtAppList(allowedapps);		
	}


	public String checkOpenVPNPermission(PackageManager pm) throws SecurityRemoteException {

		for (String appPackage : getExtAppList()) {
			ApplicationInfo app;
			try {
				app = pm.getApplicationInfo(appPackage, 0);
				if (Binder.getCallingUid() == app.uid) {
					return appPackage;
				}
			} catch (PackageManager.NameNotFoundException e) {
				// App not found. Remove it from the list
				removeApp(appPackage);
			}

		}
		throw new SecurityException("Unauthorized OpenVPN API Caller");
	}


	public boolean checkRemoteActionPermission(Context c, String callingPackage) {
		if (callingPackage == null)
			callingPackage = ConfirmDialog.ANONYMOUS_PACKAGE;

		if (isAllowed(callingPackage)) {
			return true;
		} else {
			Intent confirmDialog = new Intent(c, ConfirmDialog.class);
			confirmDialog.addFlags(FLAG_ACTIVITY_NEW_TASK);
			confirmDialog.putExtra(ConfirmDialog.EXTRA_PACKAGE_NAME, callingPackage);
			c.startActivity(confirmDialog);
			return false;
		}
	}
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.api;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ConfigParser.ConfigParseError;
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.core.VpnStatus.StateListener;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class ExternalOpenVPNService extends Service implements StateListener {

    private static final int SEND_TOALL = 0;

    final RemoteCallbackList<IOpenVPNStatusCallback> mCallbacks =
            new RemoteCallbackList<>();

    private IOpenVPNServiceInternal mService;
    private ExternalAppDatabase mExtAppDb;


    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mService = (IOpenVPNServiceInternal) (service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }

    };

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Intent.ACTION_UNINSTALL_PACKAGE.equals(intent.getAction())){
                // Check if the running config is temporary and installed by the app being uninstalled
                VpnProfile vp = ProfileManager.getLastConnectedVpn();
                if (ProfileManager.isTempProfile()) {
                    if(intent.getPackage().equals(vp.mProfileCreator)) {
                        if (mService != null)
                            try {
                                mService.stopVPN(false);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        VpnStatus.addStateListener(this);
        mExtAppDb = new ExternalAppDatabase(this);

        Intent intent = new Intent(getBaseContext(), OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mHandler.setService(this);
        IntentFilter uninstallBroadcast = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED );
        registerReceiver(mBroadcastReceiver, uninstallBroadcast);

    }

    private final IOpenVPNAPIService.Stub mBinder = new IOpenVPNAPIService.Stub() {

        @Override
        public List<APIVpnProfile> getProfiles() throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());

            ProfileManager pm = ProfileManager.getInstance(getBaseContext());

            List<APIVpnProfile> profiles = new LinkedList<>();

            for (VpnProfile vp : pm.getProfiles()) {
                if (!vp.profileDeleted)
                    profiles.add(new APIVpnProfile(vp.getUUIDString(), vp.mName, vp.mUserEditable, vp.mProfileCreator));
            }

            return profiles;
        }


        private void startProfile(VpnProfile vp)
        {
            Intent vpnPermissionIntent = VpnService.prepare(ExternalOpenVPNService.this);
            /* Check if we need to show the confirmation dialog,
             * Check if we need to ask for username/password */

            int neddPassword = vp.needUserPWInput(null, null);

            if(vpnPermissionIntent != null || neddPassword != 0){
                Intent shortVPNIntent = new Intent(Intent.ACTION_MAIN);
                shortVPNIntent.setClass(getBaseContext(), de.blinkt.openvpn.LaunchVPN.class);
                shortVPNIntent.putExtra(de.blinkt.openvpn.LaunchVPN.EXTRA_KEY, vp.getUUIDString());
                shortVPNIntent.putExtra(de.blinkt.openvpn.LaunchVPN.EXTRA_HIDELOG, true);
                shortVPNIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(shortVPNIntent);
            } else {
                VPNLaunchHelper.startOpenVpn(vp, getBaseContext());
            }

        }

        @Override
        public void startProfile(String profileUUID) throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());

            VpnProfile vp = ProfileManager.get(getBaseContext(), profileUUID);
            if (vp.checkProfile(getApplicationContext()) != R.string.no_error_found)
                throw new RemoteException(getString(vp.checkProfile(getApplicationContext())));

            startProfile(vp);
        }

        public void startVPN(String inlineConfig) throws RemoteException {
            String callingApp = mExtAppDb.checkOpenVPNPermission(getPackageManager());

            ConfigParser cp = new ConfigParser();
            try {
                cp.parseConfig(new StringReader(inlineConfig));
                VpnProfile vp = cp.convertProfile();
                vp.mName = "Remote APP VPN";
                if (vp.checkProfile(getApplicationContext()) != R.string.no_error_found)
                    throw new RemoteException(getString(vp.checkProfile(getApplicationContext())));

                vp.mProfileCreator = callingApp;

                /*int needpw = vp.needUserPWInput(false);
                if(needpw !=0)
                    throw new RemoteException("The inline file would require user input: " + getString(needpw));
                    */

                ProfileManager.setTemporaryProfile(ExternalOpenVPNService.this, vp);

                startProfile(vp);

            } catch (IOException | ConfigParseError e) {
                throw new RemoteException(e.getMessage());
            }
        }


        @Override
        public boolean addVPNProfile(String name, String config) throws RemoteException {
            return addNewVPNProfile(name, true, config) != null;
        }


        @Override
        public APIVpnProfile addNewVPNProfile(String name, boolean userEditable, String config) throws RemoteException {
            String callingPackage = mExtAppDb.checkOpenVPNPermission(getPackageManager());

            ConfigParser cp = new ConfigParser();
            try {
                cp.parseConfig(new StringReader(config));
                VpnProfile vp = cp.convertProfile();
                vp.mName = name;
                vp.mProfileCreator = callingPackage;
                vp.mUserEditable = userEditable;
                ProfileManager pm = ProfileManager.getInstance(getBaseContext());
                pm.addProfile(vp);
                pm.saveProfile(ExternalOpenVPNService.this, vp);
                pm.saveProfileList(ExternalOpenVPNService.this);
                return new APIVpnProfile(vp.getUUIDString(), vp.mName, vp.mUserEditable, vp.mProfileCreator);
            } catch (IOException e) {
                VpnStatus.logException(e);
                return null;
            } catch (ConfigParseError e) {
                VpnStatus.logException(e);
                return null;
            }
        }

        @Override
        public void removeProfile(String profileUUID) throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());
            ProfileManager pm = ProfileManager.getInstance(getBaseContext());
            VpnProfile vp = ProfileManager.get(getBaseContext(), profileUUID);
            pm.removeProfile(ExternalOpenVPNService.this, vp);
        }

        @Override
        public boolean protectSocket(ParcelFileDescriptor pfd) throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());
            try {
                boolean success= mService.protect(pfd.getFd());
                pfd.close();
                return success;
            } catch (IOException e) {
                throw new RemoteException(e.getMessage());
            }
        }


        @Override
        public Intent prepare(String packageName) {
            if (new ExternalAppDatabase(ExternalOpenVPNService.this).isAllowed(packageName))
                return null;

            Intent intent = new Intent();
            intent.setClass(ExternalOpenVPNService.this, ConfirmDialog.class);
            return intent;
        }

        @Override
        public Intent prepareVPNService() throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());

            if (VpnService.prepare(ExternalOpenVPNService.this) == null)
                return null;
            else
                return new Intent(getBaseContext(), GrantPermissionsActivity.class);
        }


        @Override
        public void registerStatusCallback(IOpenVPNStatusCallback cb)
                throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());

            if (cb != null) {
                cb.newStatus(mMostRecentState.vpnUUID, mMostRecentState.state,
                        mMostRecentState.logmessage, mMostRecentState.level.name());
                mCallbacks.register(cb);
            }


        }

        @Override
        public void unregisterStatusCallback(IOpenVPNStatusCallback cb)
                throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());

            if (cb != null)
                mCallbacks.unregister(cb);
        }

        @Override
        public void disconnect() throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());
            if (mService != null)
                mService.stopVPN(false);
        }

        @Override
        public void pause() throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());
            if (mService != null)
                mService.userPause(true);
        }

        @Override
        public void resume() throws RemoteException {
            mExtAppDb.checkOpenVPNPermission(getPackageManager());
            if (mService != null)
                mService.userPause(false);

        }
    };


    private UpdateMessage mMostRecentState;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallbacks.kill();
        unbindService(mConnection);
        VpnStatus.removeStateListener(this);
        unregisterReceiver(mBroadcastReceiver);
    }



    class UpdateMessage {
        public String state;
        public String logmessage;
        public ConnectionStatus level;
        String vpnUUID;

        UpdateMessage(String state, String logmessage, ConnectionStatus level) {
            this.state = state;
            this.logmessage = logmessage;
            this.level = level;
        }
    }

    @Override
    public void updateState(String state, String logmessage, int resid, ConnectionStatus level, Intent intent) {
        mMostRecentState = new UpdateMessage(state, logmessage, level);
        if (ProfileManager.getLastConnectedVpn() != null)
            mMostRecentState.vpnUUID = ProfileManager.getLastConnectedVpn().getUUIDString();

        Message msg = mHandler.obtainMessage(SEND_TOALL, mMostRecentState);
        msg.sendToTarget();

    }

    @Override
    public void setConnectedVPN(String uuid) {

    }

    private static final OpenVPNServiceHandler mHandler = new OpenVPNServiceHandler();


    static class OpenVPNServiceHandler extends Handler {
        WeakReference<ExternalOpenVPNService> service = null;

        private void setService(ExternalOpenVPNService eos) {
            service = new WeakReference<>(eos);
        }

        @Override
        public void handleMessage(Message msg) {

            RemoteCallbackList<IOpenVPNStatusCallback> callbacks;
            switch (msg.what) {
                case SEND_TOALL:
                    if (service == null || service.get() == null)
                        return;

                    callbacks = service.get().mCallbacks;


                    // Broadcast to all clients the new value.
                    final int N = callbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            sendUpdate(callbacks.getBroadcastItem(i), (UpdateMessage) msg.obj);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    callbacks.finishBroadcast();
                    break;
            }
        }

        private void sendUpdate(IOpenVPNStatusCallback broadcastItem,
                                UpdateMessage um) throws RemoteException {
            broadcastItem.newStatus(um.vpnUUID, um.state, um.logmessage, um.level.name());
        }
    }



}

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.api;

import android.os.Parcel;
import android.os.Parcelable;

public class APIVpnProfile implements Parcelable {

    public final String mUUID;
    public final String mName;
    public final boolean mUserEditable;
    //public final String mProfileCreator;

    public APIVpnProfile(Parcel in) {
        mUUID = in.readString();
        mName = in.readString();
        mUserEditable = in.readInt() != 0;
        //mProfileCreator = in.readString();
    }

    public APIVpnProfile(String uuidString, String name, boolean userEditable, String profileCreator) {
        mUUID = uuidString;
        mName = name;
        mUserEditable = userEditable;
        //mProfileCreator = profileCreator;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUUID);
        dest.writeString(mName);
        if (mUserEditable)
            dest.writeInt(0);
        else
            dest.writeInt(1);
        //dest.writeString(mProfileCreator);
    }

    public static final Parcelable.Creator<APIVpnProfile> CREATOR
            = new Parcelable.Creator<APIVpnProfile>() {
        public APIVpnProfile createFromParcel(Parcel in) {
            return new APIVpnProfile(in);
        }

        public APIVpnProfile[] newArray(int size) {
            return new APIVpnProfile[size];
        }
    };


}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.api;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;

public class GrantPermissionsActivity extends Activity {
	private static final int VPN_PREPARE = 0;

	@Override
	protected void onStart() {
		super.onStart();
		Intent i= VpnService.prepare(this);
		if(i==null)
			onActivityResult(VPN_PREPARE, RESULT_OK, null);
		else
			startActivityForResult(i, VPN_PREPARE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setResult(resultCode);
		finish();
	}
}


/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.blinkt.openvpn.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import de.blinkt.openvpn.R;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;


public class ConfirmDialog extends Activity implements
        CompoundButton.OnCheckedChangeListener, DialogInterface.OnClickListener {
    private static final String TAG = "OpenVPNVpnConfirm";

    public static final String EXTRA_PACKAGE_NAME = "android.intent.extra.PACKAGE_NAME";

    public static final String ANONYMOUS_PACKAGE = "de.blinkt.openvpn.ANYPACKAGE";

    private String mPackage;

    private Button mButton;

    private AlertDialog mAlert;

    private IOpenVPNServiceInternal mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }

    };

    @Override
    protected void onResume() {
        super.onResume();

        Intent serviceintent = new Intent(this, OpenVPNService.class);
        serviceintent.setAction(OpenVPNService.START_SERVICE);
        bindService(serviceintent, mConnection, Context.BIND_AUTO_CREATE);

        Intent intent = getIntent();
        if (intent.getStringExtra(EXTRA_PACKAGE_NAME) != null) {
            mPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME);
        } else {
            mPackage = getCallingPackage();
            if (mPackage == null) {
                finish();
                return;
            }
        }

        try {
            View view = View.inflate(this, R.layout.api_confirm, null);
            CharSequence appString;
            if (mPackage.equals(ANONYMOUS_PACKAGE)) {
                appString = getString(R.string.all_app_prompt, getString(R.string.app));
            } else {
                PackageManager pm = getPackageManager();
                ApplicationInfo app = pm.getApplicationInfo(mPackage, 0);
                appString = getString(R.string.prompt, app.loadLabel(pm), getString(R.string.app));
                ((ImageView) view.findViewById(R.id.icon)).setImageDrawable(app.loadIcon(pm));
            }


            ((TextView) view.findViewById(R.id.prompt)).setText(appString);
            ((CompoundButton) view.findViewById(R.id.check)).setOnCheckedChangeListener(this);


            Builder builder = new AlertDialog.Builder(this);

            builder.setView(view);

            builder.setIconAttribute(android.R.attr.alertDialogIcon);
            builder.setTitle(android.R.string.dialog_alert_title);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);

            mAlert = builder.create();
            mAlert.setCanceledOnTouchOutside(false);

            mAlert.setOnShowListener(new OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    mButton = mAlert.getButton(DialogInterface.BUTTON_POSITIVE);
                    mButton.setEnabled(false);

                }
            });

            //setCloseOnTouchOutside(false);

            mAlert.show();

        } catch (Exception e) {
            Log.e(TAG, "onResume", e);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean checked) {
        mButton.setEnabled(checked);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (which == DialogInterface.BUTTON_POSITIVE) {
            try {
                mService.addAllowedExternalApp(mPackage);
            } catch (RemoteException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            setResult(RESULT_OK);
            finish();
        }

        if (which == DialogInterface.BUTTON_NEGATIVE) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

}



/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.api;

import android.os.RemoteException;

public class SecurityRemoteException extends RemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}


/*
 * Copyright (c) 2012-2017 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.api;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;

public class RemoteAction extends Activity {

    public static final String EXTRA_NAME = "de.blinkt.openvpn.api.profileName";
    private ExternalAppDatabase mExtAppDb;
    private boolean mDoDisconnect;
    private IOpenVPNServiceInternal mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
            try {
                performAction();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            //mService = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExtAppDb = new ExternalAppDatabase(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    private void performAction() throws RemoteException {

        if (!mService.isAllowedExternalApp(getCallingPackage())) {
            finish();
            return;
        }

        Intent intent = getIntent();
        setIntent(null);
        ComponentName component = intent.getComponent();
        if (component.getShortClassName().equals(".api.DisconnectVPN")) {
            mService.stopVPN(false);
        } else if (component.getShortClassName().equals(".api.ConnectVPN")) {
            String vpnName = intent.getStringExtra(EXTRA_NAME);
            VpnProfile profile = ProfileManager.getInstance(this).getProfileByName(vpnName);
            if (profile == null) {
                Toast.makeText(this, String.format("Vpn profile %s from API call not found", vpnName), Toast.LENGTH_LONG).show();
            } else {
                Intent startVPN = new Intent(this, LaunchVPN.class);
                startVPN.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
                startVPN.setAction(Intent.ACTION_MAIN);
                startActivity(startVPN);
            }
        }
        finish();



    }

    @Override
    public void finish() {
        if(mService!=null) {
            mService = null;
            getApplicationContext().unbindService(mConnection);
        }
        super.finish();
    }
}


/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.R;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * Created by arne on 13.10.13.
 */
public class DisconnectVPN extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {
    private IOpenVPNServiceInternal mService;
    private ServiceConnection mConnection = new ServiceConnection() {



        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        showDisconnectDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private void showDisconnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_cancel);
        builder.setMessage(R.string.cancel_connection_query);
        builder.setNegativeButton(android.R.string.cancel, this);
        builder.setPositiveButton(R.string.cancel_connection, this);
        builder.setNeutralButton(R.string.reconnect, this);
        builder.setOnCancelListener(this);

        builder.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            ProfileManager.setConntectedVpnProfileDisconnected(this);
            if (mService != null) {
                try {
                    mService.stopVPN(false);
                } catch (RemoteException e) {
                    VpnStatus.logException(e);
                }
            }
        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            Intent intent = new Intent(this, LaunchVPN.class);
            intent.putExtra(LaunchVPN.EXTRA_KEY, VpnStatus.getLastConnectedVPNProfile());
            intent.setAction(Intent.ACTION_MAIN);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}


// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.ads.nativetemplates;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.nativetemplates.R;

/**
 * Base class for a template view. *
 */
public class TemplateView extends FrameLayout {

  private int templateType;
  private NativeTemplateStyle styles;
  private NativeAd nativeAd;
  private NativeAdView nativeAdView;

  private TextView primaryView;
  private TextView secondaryView;
  private RatingBar ratingBar;
  private TextView tertiaryView;
  private ImageView iconView;
  private MediaView mediaView;
  private Button callToActionView;
  private ConstraintLayout background;

  private static final String MEDIUM_TEMPLATE = "medium_template";
  private static final String SMALL_TEMPLATE = "small_template";

  public TemplateView(Context context) {
    super(context);
  }

  public TemplateView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  public TemplateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context, attrs);
  }

  public TemplateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initView(context, attrs);
  }

  public void setStyles(NativeTemplateStyle styles) {
    this.styles = styles;
    this.applyStyles();
  }

  public NativeAdView getNativeAdView() {
    return nativeAdView;
  }

  private void applyStyles() {

    Drawable mainBackground = styles.getMainBackgroundColor();
    if (mainBackground != null) {
      background.setBackground(mainBackground);
      if (primaryView != null) {
        primaryView.setBackground(mainBackground);
      }
      if (secondaryView != null) {
        secondaryView.setBackground(mainBackground);
      }
      if (tertiaryView != null) {
        tertiaryView.setBackground(mainBackground);
      }
    }

    Typeface primary = styles.getPrimaryTextTypeface();
    if (primary != null && primaryView != null) {
      primaryView.setTypeface(primary);
    }

    Typeface secondary = styles.getSecondaryTextTypeface();
    if (secondary != null && secondaryView != null) {
      secondaryView.setTypeface(secondary);
    }

    Typeface tertiary = styles.getTertiaryTextTypeface();
    if (tertiary != null && tertiaryView != null) {
      tertiaryView.setTypeface(tertiary);
    }

    Typeface ctaTypeface = styles.getCallToActionTextTypeface();
    if (ctaTypeface != null && callToActionView != null) {
      callToActionView.setTypeface(ctaTypeface);
    }

    if (styles.getPrimaryTextTypefaceColor() != null && primaryView != null) {
      primaryView.setTextColor(styles.getPrimaryTextTypefaceColor());
    }

    if (styles.getSecondaryTextTypefaceColor() != null && secondaryView != null) {
      secondaryView.setTextColor(styles.getSecondaryTextTypefaceColor());
    }

    if (styles.getTertiaryTextTypefaceColor() != null && tertiaryView != null) {
      tertiaryView.setTextColor(styles.getTertiaryTextTypefaceColor());
    }

    if (styles.getCallToActionTypefaceColor() != null && callToActionView != null) {
      callToActionView.setTextColor(styles.getCallToActionTypefaceColor());
    }

    float ctaTextSize = styles.getCallToActionTextSize();
    if (ctaTextSize > 0 && callToActionView != null) {
      callToActionView.setTextSize(ctaTextSize);
    }

    float primaryTextSize = styles.getPrimaryTextSize();
    if (primaryTextSize > 0 && primaryView != null) {
      primaryView.setTextSize(primaryTextSize);
    }

    float secondaryTextSize = styles.getSecondaryTextSize();
    if (secondaryTextSize > 0 && secondaryView != null) {
      secondaryView.setTextSize(secondaryTextSize);
    }

    float tertiaryTextSize = styles.getTertiaryTextSize();
    if (tertiaryTextSize > 0 && tertiaryView != null) {
      tertiaryView.setTextSize(tertiaryTextSize);
    }

    Drawable ctaBackground = styles.getCallToActionBackgroundColor();
    if (ctaBackground != null && callToActionView != null) {
      callToActionView.setBackground(ctaBackground);
    }

    Drawable primaryBackground = styles.getPrimaryTextBackgroundColor();
    if (primaryBackground != null && primaryView != null) {
      primaryView.setBackground(primaryBackground);
    }

    Drawable secondaryBackground = styles.getSecondaryTextBackgroundColor();
    if (secondaryBackground != null && secondaryView != null) {
      secondaryView.setBackground(secondaryBackground);
    }

    Drawable tertiaryBackground = styles.getTertiaryTextBackgroundColor();
    if (tertiaryBackground != null && tertiaryView != null) {
      tertiaryView.setBackground(tertiaryBackground);
    }

    invalidate();
    requestLayout();
  }

  private boolean adHasOnlyStore(NativeAd nativeAd) {
    String store = nativeAd.getStore();
    String advertiser = nativeAd.getAdvertiser();
    return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser);
  }

  public void setNativeAd(NativeAd nativeAd) {
    this.nativeAd = nativeAd;

    String store = nativeAd.getStore();
    String advertiser = nativeAd.getAdvertiser();
    String headline = nativeAd.getHeadline();
    String body = nativeAd.getBody();
    String cta = nativeAd.getCallToAction();
    Double starRating = nativeAd.getStarRating();
    NativeAd.Image icon = nativeAd.getIcon();

    String secondaryText;

    nativeAdView.setCallToActionView(callToActionView);
    nativeAdView.setHeadlineView(primaryView);
    nativeAdView.setMediaView(mediaView);
    secondaryView.setVisibility(VISIBLE);
    if (adHasOnlyStore(nativeAd)) {
      nativeAdView.setStoreView(secondaryView);
      secondaryText = store;
    } else if (!TextUtils.isEmpty(advertiser)) {
      nativeAdView.setAdvertiserView(secondaryView);
      secondaryText = advertiser;
    } else {
      secondaryText = "";
    }

    primaryView.setText(headline);
    callToActionView.setText(cta);

    //  Set the secondary view to be the star rating if available.
    if (starRating != null && starRating > 0) {
      secondaryView.setVisibility(GONE);
      ratingBar.setVisibility(VISIBLE);
      ratingBar.setRating(starRating.floatValue());

      nativeAdView.setStarRatingView(ratingBar);
    } else {
      secondaryView.setText(secondaryText);
      secondaryView.setVisibility(VISIBLE);
      ratingBar.setVisibility(GONE);
    }

    if (icon != null) {
      iconView.setVisibility(VISIBLE);
      iconView.setImageDrawable(icon.getDrawable());
    } else {
      iconView.setVisibility(GONE);
    }

    if (tertiaryView != null) {
      tertiaryView.setText(body);
      nativeAdView.setBodyView(tertiaryView);
    }

    nativeAdView.setNativeAd(nativeAd);
  }

  /**
   * To prevent memory leaks, make sure to destroy your ad when you don't need it anymore. This
   * method does not destroy the template view.
   * https://developers.google.com/admob/android/native-unified#destroy_ad
   */
  public void destroyNativeAd() {
    nativeAd.destroy();
  }

  public String getTemplateTypeName() {
    if (templateType == R.layout.gnt_medium_template_view) {
      return MEDIUM_TEMPLATE;
    } else if (templateType == R.layout.gnt_small_template_view) {
      return SMALL_TEMPLATE;
    }
    return "";
  }

  private void initView(Context context, AttributeSet attributeSet) {

    TypedArray attributes =
        context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TemplateView, 0, 0);

    try {
      templateType =
          attributes.getResourceId(
              R.styleable.TemplateView_gnt_template_type, R.layout.gnt_medium_template_view);
    } finally {
      attributes.recycle();
    }
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(templateType, this);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    nativeAdView = (NativeAdView) findViewById(R.id.native_ad_view);
    primaryView = (TextView) findViewById(R.id.primary);
    secondaryView = (TextView) findViewById(R.id.secondary);
    tertiaryView = (TextView) findViewById(R.id.body);

    ratingBar = (RatingBar) findViewById(R.id.rating_bar);
    ratingBar.setEnabled(false);

    callToActionView = (Button) findViewById(R.id.cta);
    iconView = (ImageView) findViewById(R.id.icon);
    mediaView = (MediaView) findViewById(R.id.media_view);
    background = (ConstraintLayout) findViewById(R.id.background);
  }
}


// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.android.ads.nativetemplates;

import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.Nullable;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/** A class containing the optional styling options for the Native Template. */
public class NativeTemplateStyle {

  // Call to action typeface.
  private Typeface callToActionTextTypeface;

  // Size of call to action text.
  private float callToActionTextSize;

  // Call to action typeface color in the form 0xAARRGGBB.
  @Nullable private Integer callToActionTypefaceColor;

  // Call to action background color.
  private ColorDrawable callToActionBackgroundColor;

  // All templates have a primary text area which is populated by the native ad's headline.

  // Primary text typeface.
  private Typeface primaryTextTypeface;

  // Size of primary text.
  private float primaryTextSize;

  // Primary text typeface color in the form 0xAARRGGBB.
  @Nullable private Integer primaryTextTypefaceColor;

  // Primary text background color.
  private ColorDrawable primaryTextBackgroundColor;

  // The typeface, typeface color, and background color for the second row of text in the template.
  // All templates have a secondary text area which is populated either by the body of the ad or
  // by the rating of the app.

  // Secondary text typeface.
  private Typeface secondaryTextTypeface;

  // Size of secondary text.
  private float secondaryTextSize;

  // Secondary text typeface color in the form 0xAARRGGBB.
  @Nullable private Integer secondaryTextTypefaceColor;

  // Secondary text background color.
  private ColorDrawable secondaryTextBackgroundColor;

  // The typeface, typeface color, and background color for the third row of text in the template.
  // The third row is used to display store name or the default tertiary text.

  // Tertiary text typeface.
  private Typeface tertiaryTextTypeface;

  // Size of tertiary text.
  private float tertiaryTextSize;

  // Tertiary text typeface color in the form 0xAARRGGBB.
  @Nullable private Integer tertiaryTextTypefaceColor;

  // Tertiary text background color.
  private ColorDrawable tertiaryTextBackgroundColor;

  // The background color for the bulk of the ad.
  private ColorDrawable mainBackgroundColor;

  public Typeface getCallToActionTextTypeface() {
    return callToActionTextTypeface;
  }

  public float getCallToActionTextSize() {
    return callToActionTextSize;
  }

  @Nullable
  public Integer getCallToActionTypefaceColor() {
    return callToActionTypefaceColor;
  }

  public ColorDrawable getCallToActionBackgroundColor() {
    return callToActionBackgroundColor;
  }

  public Typeface getPrimaryTextTypeface() {
    return primaryTextTypeface;
  }

  public float getPrimaryTextSize() {
    return primaryTextSize;
  }

  @Nullable
  public Integer getPrimaryTextTypefaceColor() {
    return primaryTextTypefaceColor;
  }

  public ColorDrawable getPrimaryTextBackgroundColor() {
    return primaryTextBackgroundColor;
  }

  public Typeface getSecondaryTextTypeface() {
    return secondaryTextTypeface;
  }

  public float getSecondaryTextSize() {
    return secondaryTextSize;
  }

  @Nullable
  public Integer getSecondaryTextTypefaceColor() {
    return secondaryTextTypefaceColor;
  }

  public ColorDrawable getSecondaryTextBackgroundColor() {
    return secondaryTextBackgroundColor;
  }

  public Typeface getTertiaryTextTypeface() {
    return tertiaryTextTypeface;
  }

  public float getTertiaryTextSize() {
    return tertiaryTextSize;
  }

  @Nullable
  public Integer getTertiaryTextTypefaceColor() {
    return tertiaryTextTypefaceColor;
  }

  public ColorDrawable getTertiaryTextBackgroundColor() {
    return tertiaryTextBackgroundColor;
  }

  public ColorDrawable getMainBackgroundColor() {
    return mainBackgroundColor;
  }

  /** A class that provides helper methods to build a style object. */
  public static class Builder {

    private NativeTemplateStyle styles;

    public Builder() {
      this.styles = new NativeTemplateStyle();
    }

    @CanIgnoreReturnValue
    public Builder withCallToActionTextTypeface(Typeface callToActionTextTypeface) {
      this.styles.callToActionTextTypeface = callToActionTextTypeface;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withCallToActionTextSize(float callToActionTextSize) {
      this.styles.callToActionTextSize = callToActionTextSize;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withCallToActionTypefaceColor(int callToActionTypefaceColor) {
      this.styles.callToActionTypefaceColor = callToActionTypefaceColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withCallToActionBackgroundColor(ColorDrawable callToActionBackgroundColor) {
      this.styles.callToActionBackgroundColor = callToActionBackgroundColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withPrimaryTextTypeface(Typeface primaryTextTypeface) {
      this.styles.primaryTextTypeface = primaryTextTypeface;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withPrimaryTextSize(float primaryTextSize) {
      this.styles.primaryTextSize = primaryTextSize;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withPrimaryTextTypefaceColor(int primaryTextTypefaceColor) {
      this.styles.primaryTextTypefaceColor = primaryTextTypefaceColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withPrimaryTextBackgroundColor(ColorDrawable primaryTextBackgroundColor) {
      this.styles.primaryTextBackgroundColor = primaryTextBackgroundColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withSecondaryTextTypeface(Typeface secondaryTextTypeface) {
      this.styles.secondaryTextTypeface = secondaryTextTypeface;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withSecondaryTextSize(float secondaryTextSize) {
      this.styles.secondaryTextSize = secondaryTextSize;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withSecondaryTextTypefaceColor(int secondaryTextTypefaceColor) {
      this.styles.secondaryTextTypefaceColor = secondaryTextTypefaceColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withSecondaryTextBackgroundColor(ColorDrawable secondaryTextBackgroundColor) {
      this.styles.secondaryTextBackgroundColor = secondaryTextBackgroundColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withTertiaryTextTypeface(Typeface tertiaryTextTypeface) {
      this.styles.tertiaryTextTypeface = tertiaryTextTypeface;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withTertiaryTextSize(float tertiaryTextSize) {
      this.styles.tertiaryTextSize = tertiaryTextSize;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withTertiaryTextTypefaceColor(int tertiaryTextTypefaceColor) {
      this.styles.tertiaryTextTypefaceColor = tertiaryTextTypefaceColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withTertiaryTextBackgroundColor(ColorDrawable tertiaryTextBackgroundColor) {
      this.styles.tertiaryTextBackgroundColor = tertiaryTextBackgroundColor;
      return this;
    }

    @CanIgnoreReturnValue
    public Builder withMainBackgroundColor(ColorDrawable mainBackgroundColor) {
      this.styles.mainBackgroundColor = mainBackgroundColor;
      return this;
    }

    public NativeTemplateStyle build() {
      return styles;
    }
  }
}


