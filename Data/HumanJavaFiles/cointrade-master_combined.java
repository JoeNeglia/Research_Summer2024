package khalil.cointrader;

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
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("khalil.cointrader", appContext.getPackageName());
    }
}


package khalil.cointrader;

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

package khalil.cointrader;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.tv.TvContract;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by khalil on 12/22/17.
 */

public class Fragment1 extends Fragment {
    private ListView listView;
    private Toast mToast;
    private Bittrex wrapper;
    public String[] mTickerPrice;
    public String[] mPercentChange;
    private String[] mMarket;
    private String[] mCoinName;
    private String[] mCoinSymbol;
    private String[] mMarketName;
    private String[] mHoldings;
    private String[] mHoldingsUSD;
    private Integer[] mImageID;
    private Integer[] mImageIDOrig;
    private String[] mLogoURL;
    private int index;
    private int NUM_COINS = 12;
    private ProgressDialog mProgressBar;
    private ProgressBar mProgressSpin;
    private Timer mTimer;
    private Controller Controller;
    public JSONObject mJSONResponse;
    private List<Ticker> mTickerList;
    private boolean isInitialized = false;

    /*
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (mTimer != null) {
                System.out.println("STOPPING FRAGMENT 1 TIMER");
                mTimer.cancel();
                mTimer = null;
            }
        } else {
            if (mTimer == null && isInitialized) {
                System.out.println("STARTING FRAGMENT 1 TIMER");

                startPriceUpdateTimer();
            }
        }
    }
    */

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mMarket = getResources().getStringArray(R.array.Markets);
        mProgressBar = new ProgressDialog(getActivity());
        mProgressBar.setMessage("Updating Data ...");

        mProgressSpin = (ProgressBar) view.findViewById(R.id.price_progressBar);
        mProgressSpin.setVisibility(View.INVISIBLE);
        wrapper = new Bittrex();
        wrapper.setAuthKeysFromTextFile("keys.txt");
        listView = (ListView) view.findViewById(R.id.list);
        Controller = new Controller(getActivity(), mMarket);

        String[] coin_name = getResources().getStringArray(R.array.CoinName);
        String[] coin_symbol = getResources().getStringArray(R.array.Symbols);

        Integer[] ImageID = {
                R.drawable.btc_1st,
                R.drawable.btc_2give,
                R.drawable.btc_aby,
                R.drawable.btc_ada,
                R.drawable.btc_adt,
                R.drawable.btc_adx,
                R.drawable.btc_aeon,
                R.drawable.btc_agrs,
                R.drawable.btc_amp,
                R.drawable.btc_ant,
                R.drawable.btc_apx,
                R.drawable.btc_ardr,
                R.drawable.btc_ark,
                R.drawable.btc_aur,
                R.drawable.btc_bat,
                R.drawable.btc_bay,
                R.drawable.btc_bcc,
                R.drawable.btc_bcy,
                R.drawable.btc_bitb,
                R.drawable.btc_blitz,
                R.drawable.btc_blk,
                R.drawable.btc_block,
                R.drawable.btc_bnt,
                R.drawable.btc_brk,
                R.drawable.btc_brx,
                R.drawable.btc_bsd,
                R.drawable.btc_btcd,
                R.drawable.btc_btg,
                R.drawable.btc_burst,
                R.drawable.btc_byc,
                R.drawable.btc_cann,
                R.drawable.btc_cfi,
                R.drawable.btc_clam,
                R.drawable.btc_cloak,
                R.drawable.btc_club,
                R.drawable.btc_coval,
                R.drawable.btc_cpc,
                R.drawable.btc_crb,
                R.drawable.btc_crw,
                R.drawable.btc_cure,
                R.drawable.btc_cvc,
                R.drawable.btc_dash,
                R.drawable.btc_dcr,
                R.drawable.btc_dct,
                R.drawable.btc_dgb,
                R.drawable.btc_dgd,
                R.drawable.btc_dmd,
                R.drawable.btc_dnt,
                R.drawable.btc_doge,
                R.drawable.btc_dope,
                R.drawable.btc_dtb,
                R.drawable.btc_dyn,
                R.drawable.btc_ebst,
                R.drawable.btc_edg,
                R.drawable.btc_efl,
                R.drawable.btc_egc,
                R.drawable.btc_emc,
                R.drawable.btc_emc2,
                R.drawable.btc_eng,
                R.drawable.btc_enrg,
                R.drawable.btc_erc,
                R.drawable.btc_etc,
                R.drawable.btc_eth,
                R.drawable.btc_excl,
                R.drawable.btc_exp,
                R.drawable.btc_fair,
                R.drawable.btc_fct,
                R.drawable.btc_fldc,
                R.drawable.btc_flo,
                R.drawable.btc_ftc,
                R.drawable.btc_fun,
                R.drawable.btc_gam,
                R.drawable.btc_game,
                R.drawable.btc_gbg,
                R.drawable.btc_gbyte,
                R.drawable.btc_gcr,
                R.drawable.btc_geo,
                R.drawable.btc_gld,
                R.drawable.btc_gno,
                R.drawable.btc_gnt,
                R.drawable.btc_golos,
                R.drawable.btc_grc,
                R.drawable.btc_grs,
                R.drawable.btc_gup,
                R.drawable.btc_hmq,
                R.drawable.btc_incnt,
                R.drawable.btc_infx,
                R.drawable.btc_ioc,
                R.drawable.btc_ion,
                R.drawable.btc_iop,
                R.drawable.btc_kmd,
                R.drawable.btc_kore,
                R.drawable.btc_lbc,
                R.drawable.btc_lgd,
                R.drawable.btc_lmc,
                R.drawable.btc_lsk,
                R.drawable.btc_ltc,
                R.drawable.btc_lun,
                R.drawable.btc_maid,
                R.drawable.btc_mana,
                R.drawable.btc_mco,
                R.drawable.btc_meme,
                R.drawable.btc_mer,
                R.drawable.btc_mln,
                R.drawable.btc_mona,
                R.drawable.btc_mtl,
                R.drawable.btc_mue,
                R.drawable.btc_music,
                R.drawable.btc_myst,
                R.drawable.btc_nav,
                R.drawable.btc_nbt,
                R.drawable.btc_neo,
                R.drawable.btc_neos,
                R.drawable.btc_nlg,
                R.drawable.btc_nmr,
                R.drawable.btc_nxc,
                R.drawable.btc_nxs,
                R.drawable.btc_nxt,
                R.drawable.btc_ok,
                R.drawable.btc_omg,
                R.drawable.btc_omni,
                R.drawable.btc_part,
                R.drawable.btc_pay,
                R.drawable.btc_pdc,
                R.drawable.btc_pink,
                R.drawable.btc_pivx,
                R.drawable.btc_pkb,
                R.drawable.btc_pot,
                R.drawable.btc_powr,
                R.drawable.btc_ppc,
                R.drawable.btc_ptc,
                R.drawable.btc_ptoy,
                R.drawable.btc_qrl,
                R.drawable.btc_qtum,
                R.drawable.btc_qwark,
                R.drawable.btc_rads,
                R.drawable.btc_rby,
                R.drawable.btc_rcn,
                R.drawable.btc_rdd,
                R.drawable.btc_rep,
                R.drawable.btc_rise,
                R.drawable.btc_rlc,
                R.drawable.btc_salt,
                R.drawable.btc_sbd,
                R.drawable.btc_sc,
                R.drawable.btc_seq,
                R.drawable.btc_shift,
                R.drawable.btc_sib,
                R.drawable.btc_slr,
                R.drawable.btc_sls,
                R.drawable.btc_snrg,
                R.drawable.btc_snt,
                R.drawable.btc_sphr,
                R.drawable.btc_spr,
                R.drawable.btc_start,
                R.drawable.btc_steem,
                R.drawable.btc_storj,
                R.drawable.btc_strat,
                R.drawable.btc_swift,
                R.drawable.btc_swt,
                R.drawable.btc_synx,
                R.drawable.btc_sys,
                R.drawable.btc_thc,
                R.drawable.btc_tix,
                R.drawable.btc_tks,
                R.drawable.btc_trig,
                R.drawable.btc_trst,
                R.drawable.btc_trust,
                R.drawable.btc_tx,
                R.drawable.btc_ubq,
                R.drawable.btc_unb,
                R.drawable.btc_via,
                R.drawable.btc_vib,
                R.drawable.btc_vox,
                R.drawable.btc_vrc,
                R.drawable.btc_vrm,
                R.drawable.btc_vtc,
                R.drawable.btc_vtr,
                R.drawable.btc_waves,
                R.drawable.btc_wings,
                R.drawable.btc_xcp,
                R.drawable.btc_xdn,
                R.drawable.btc_xel,
                R.drawable.btc_xem,
                R.drawable.btc_xlm,
                R.drawable.btc_xmg,
                R.drawable.btc_xmr,
                R.drawable.btc_xmy,
                R.drawable.btc_xrp,
                R.drawable.btc_xst,
                R.drawable.btc_xvc,
                R.drawable.btc_xvg,
                R.drawable.btc_xwc,
                R.drawable.btc_xzc,
                R.drawable.btc_zcl,
                R.drawable.btc_zec,
                R.drawable.btc_zen,
                R.drawable.eth_1st,
                R.drawable.eth_ada,
                R.drawable.eth_adt,
                R.drawable.eth_adx,
                R.drawable.eth_ant,
                R.drawable.eth_bat,
                R.drawable.eth_bcc,
                R.drawable.eth_bnt,
                R.drawable.eth_btg,
                R.drawable.eth_cfi,
                R.drawable.eth_crb,
                R.drawable.eth_cvc,
                R.drawable.eth_dash,
                R.drawable.eth_dgb,
                R.drawable.eth_dgd,
                R.drawable.eth_dnt,
                R.drawable.eth_eng,
                R.drawable.eth_etc,
                R.drawable.eth_fct,
                R.drawable.eth_fun,
                R.drawable.eth_gno,
                R.drawable.eth_gnt,
                R.drawable.eth_gup,
                R.drawable.eth_hmq,
                R.drawable.eth_lgd,
                R.drawable.eth_ltc,
                R.drawable.eth_lun,
                R.drawable.eth_mana,
                R.drawable.eth_mco,
                R.drawable.eth_mtl,
                R.drawable.eth_myst,
                R.drawable.eth_neo,
                R.drawable.eth_nmr,
                R.drawable.eth_omg,
                R.drawable.eth_pay,
                R.drawable.eth_powr,
                R.drawable.eth_ptoy,
                R.drawable.eth_qrl,
                R.drawable.eth_qtum,
                R.drawable.eth_rcn,
                R.drawable.eth_rep,
                R.drawable.eth_rlc,
                R.drawable.eth_salt,
                R.drawable.eth_sc,
                R.drawable.eth_snt,
                R.drawable.eth_storj,
                R.drawable.eth_strat,
                R.drawable.eth_tix,
                R.drawable.eth_trst,
                R.drawable.eth_vib,
                R.drawable.eth_waves,
                R.drawable.eth_wings,
                R.drawable.eth_xem,
                R.drawable.eth_xlm,
                R.drawable.eth_xmr,
                R.drawable.eth_xrp,
                R.drawable.eth_zec,
                R.drawable.usdt_bcc,
                R.drawable.usdt_btc,
                R.drawable.usdt_btg,
                R.drawable.usdt_dash,
                R.drawable.usdt_etc,
                R.drawable.usdt_eth,
                R.drawable.usdt_ltc,
                R.drawable.usdt_neo,
                R.drawable.usdt_omg,
                R.drawable.usdt_xmr,
                R.drawable.usdt_xrp,
                R.drawable.usdt_zec};

        mImageIDOrig = ImageID;


        updatePrice();
        startPriceUpdateTimer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment1_layout, container, false);
        return v;
    }

    private void storeSelectedCoin(int position) {
        SharedPreferences mSharedIndex = getActivity().getSharedPreferences("coin_index", getActivity().MODE_PRIVATE);
        SharedPreferences mSharedPrice = getActivity().getSharedPreferences("selected_price", getActivity().MODE_PRIVATE);
        SharedPreferences mSharedPercent = getActivity().getSharedPreferences("selected_percent", getActivity().MODE_PRIVATE);
        SharedPreferences mSharedMarket = getActivity().getSharedPreferences("market", getActivity().MODE_PRIVATE);
        SharedPreferences mSharedMarketLong = getActivity().getSharedPreferences("marketlong", getActivity().MODE_PRIVATE);
        SharedPreferences mSharedLogoURL = getActivity().getSharedPreferences("logourl", getActivity().MODE_PRIVATE);

        SharedPreferences.Editor mEditIndex = mSharedIndex.edit();
        SharedPreferences.Editor mEditPrice = mSharedPrice.edit();
        SharedPreferences.Editor mEditPercent = mSharedPercent.edit();
        SharedPreferences.Editor mEditMarket = mSharedMarket.edit();
        SharedPreferences.Editor mEditMarketLong = mSharedMarketLong.edit();
        SharedPreferences.Editor mEditLogoURL = mSharedLogoURL.edit();


        mEditMarket.putString("market", mCoinSymbol[position]);
        mEditMarketLong.putString("marketlong", mCoinName[position]);
        mEditIndex.putInt("coin_index", position);
        mEditPrice.putString("selected_price", mTickerPrice[position]);
        mEditPercent.putString("selected_percent", mPercentChange[position]);
        mEditLogoURL.putString("logourl", mLogoURL[position]);


        mEditIndex.apply();
        mEditPrice.apply();
        mEditPercent.apply();
        mEditMarket.apply();
        mEditMarketLong.apply();
        mEditLogoURL.apply();
    }


    public void startPriceUpdateTimer() {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        // a potentially  time consuming task
                        updatePrice();
                    }
                }).start();
            }
        }, 5000, 4000); //Length of how often to update location
    }

    public void updatePrice() {
        //https://bittrex.com/api/v1.1/public/getticker?market=USDT-BTC
        String url = "https://bittrex.com/api/v1.1/public/getmarketsummaries";
        String url2 = "https://bittrex.com/api/v1.1/public/getmarkets";

        Controller.getMarketSummaryResponse(url, url2, new Controller.VolleyCallback() {
            @Override
            public void onSuccessResponse(String[] result) {
                System.out.println("MARKET SUMMARY: " + result[0]);
                System.out.println("MARKET LOGO: " + result[1]);
                mTickerList = Controller.populateTickers(result[0], result[1], mImageIDOrig, getResources().getStringArray(R.array.CoinName));
                updateListView();
            }
        }, mImageIDOrig);

    }


    public void updateBalance() {
        final Double mCurrentPriceDouble;


        for (index = 0; index < NUM_COINS; index++) {
            String rawResponse = wrapper.getBalance(mCoinSymbol[index]);
            final String coinBalance;
            final String balance2;
            List<HashMap<String, String>> responseMapList;

            if(rawResponse.contains("\"success\":true")) {
                responseMapList = Bittrex.getMapsFromResponse(rawResponse);
                final HashMap<String, String> currency1 = responseMapList.get(0);
                System.out.println("BALANCES " + rawResponse);
                coinBalance = currency1.get("Balance");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!(coinBalance == null) && !coinBalance.equals("0.00000000")) {
                            mTickerList.get(index).setBalance(Controller.formatDecimalSetoci(coinBalance));
                        } else {
                        }
                    }
                });
            }
            else {
                System.out.println("ERROR! Response: " + rawResponse);
            }

            }

        for(index = 0; index < NUM_COINS; index++){
            System.out.println("HOLDINGS FOR " + index + ": " + mTickerList.get(index).getBalance());
        }
    }


    public void updateListView(){
        System.out.println("SIZE OF MARKETS: " + mTickerList.size());

        if(!isInitialized) {
            mCoinName = new String[mTickerList.size()];
            mCoinSymbol = new String[mTickerList.size()];
            mTickerPrice = new String[mTickerList.size()];
            mPercentChange = new String[mTickerList.size()];
            mHoldings = new String[mTickerList.size()];
            mHoldingsUSD = new String[mTickerList.size()];
            mImageID = new Integer[mTickerList.size()];
            mLogoURL = new String[mTickerList.size()];

            CustomListAdapter adapter = new CustomListAdapter(getActivity(), mImageID, mCoinSymbol, mCoinName, mTickerPrice, mPercentChange, mHoldings, mHoldingsUSD, mLogoURL);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View view, int position, long id) {
                    storeSelectedCoin(position);

                    final Intent intent = new Intent(getActivity(), SecondActivity.class);
                    startActivity(intent);
                }
            });

            isInitialized = true;
        }

        for(int i = 0; i < mTickerList.size(); i++){
            mImageID[i] = mTickerList.get(i).getLogo();
            mCoinSymbol[i] = mTickerList.get(i).getMarketName();
            mCoinName[i] = mTickerList.get(i).getNameLong();
            mPercentChange[i] = mTickerList.get(i).getPercentChange();
            mTickerPrice[i] = mTickerList.get(i).getPrice();
            mLogoURL[i] = mTickerList.get(i).getLogoUrl();
        }

        /*
        updateBalance();

        for(int i = 0; i < NUM_COINS; i++){
            mHoldings[i] = mTickerList.get(i).getBalance();
            if(mHoldings[i] != null)
                mHoldingsUSD[i] = "$" + Controller.formatDecimal(Double.toString(Double.parseDouble(mHoldings[i])*Double.parseDouble(mTickerPrice[i].substring(1).replace(",",""))));
            System.out.println("UPDATING INDEX " + i + " WITH BALANCE: " + mHoldingsUSD[i]);
        }
        */

        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }



    @Override
    public void onStop(){
        super.onStop();
        System.out.println("STOPPING FRAGMENT 1 TIMER");
        mTimer.cancel();
        mTimer = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mTimer == null){
            System.out.println("STARTING FRAGMENT 1 TIMER");
            startPriceUpdateTimer();
        }
    }

    public void stopTimer(){
        System.out.println("STOPPING FRAGMENT 1 TIMER");
        mTimer.cancel();
        mTimer = null;
    }

    @Override
    public void onDestroy(){
        listView.setAdapter(null);
        super.onDestroy();
    }


    public class spinProgressBar extends AsyncTask<Void, Void, Void> {
        /**
         * Background task to sleep a thread for 1 second while the
         * progress circle spins.
         * upon successful check-in.
         * @param args no parameters needed for this task.
         * @return null
         */
        @Override
        protected Void doInBackground(Void... args) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After waiting one second, hide the progress circle.
         */
        @Override
        protected void onPostExecute(Void result) {
            mProgressSpin.setVisibility(View.INVISIBLE);
            super.onPostExecute(result);
        }

        /**
         * Make the progress circle animation visible to
         * the user.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressSpin.setVisibility(View.VISIBLE);
        }
    }

}


package khalil.cointrader;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by khalil on 1/16/18.
 */

public class FileCache {

    private File cacheDir;

    public FileCache(Context context){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            cacheDir = new File(Environment.getExternalStorageDirectory(), "LazyList");
        } else {
            cacheDir = context.getCacheDir();
        }

        if(!cacheDir.exists()){
            cacheDir.mkdirs();
        }
    }

    public File getFile(String url){
        String filename = String.valueOf(url.hashCode());

        File f = new File(cacheDir, filename);
        return f;
    }

    public void clear(){
        File[] files = cacheDir.listFiles();
        if(files == null)
            return;

        for(File f: files){
            f.delete();
        }
    }
}


package khalil.cointrader;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtility {

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	
	public static String calculateHash(String secret, String url, String encryption) {

		Mac shaHmac = null;

		try {

			shaHmac = Mac.getInstance(encryption);

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}

		SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), encryption);

		try {

			shaHmac.init(secretKey);

		} catch (InvalidKeyException e) {

			e.printStackTrace();
		}

		byte[] hash = shaHmac.doFinal(url.getBytes());
		String check = bytesToHex(hash);

		return check;
	}

	public static String generateNonce() {

		SecureRandom random = null;
		
		try {

			random = SecureRandom.getInstance("SHA1PRNG");

		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}

		random.setSeed(System.currentTimeMillis()); 

		byte[] nonceBytes = new byte[16]; 
		random.nextBytes(nonceBytes); 

		String nonce = null;

		try {

			nonce = new String(Base64.getEncoder().encode(nonceBytes), "UTF-8");

		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}

		return nonce;
	}
	
	private static String bytesToHex(byte[] bytes) {
		
	    char[] hexChars = new char[bytes.length * 2];
	    
	    for(int j = 0; j < bytes.length; j++) {
	    	
	        int v = bytes[j] & 0xFF;
	        
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    
	    return new String(hexChars);
	}
}


package khalil.cointrader;

public class ReconnectionAttemptsExceededException extends RuntimeException {
	
	  private static final long serialVersionUID = 3756544403778584892L;
	  
	  public ReconnectionAttemptsExceededException() { super(); }
	  public ReconnectionAttemptsExceededException(String message) { super(message); }
	  public ReconnectionAttemptsExceededException(String message, Throwable cause) { super(message, cause); }
	  public ReconnectionAttemptsExceededException(Throwable cause) { super(cause); }
	}


package khalil.cointrader;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.navigation_home:
                                setViewPager(0);
                                break;
                            case R.id.navigation_dashboard:
                                setViewPager(1);
                                break;
                            case R.id.navigation_notifications:
                                setViewPager(2);
                                break;
                        }
                        return true;
                    }
                });

    }

    private void setupViewPager(ViewPager viewPager){
        SectionsStatePagerAdapter sectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        sectionsStatePagerAdapter.addFragment(new Fragment1(), "Market Overview");
        sectionsStatePagerAdapter.addFragment(new Fragment2(), "Order History");

        viewPager.setAdapter(sectionsStatePagerAdapter);
    }

    public void setViewPager(int fragmentNum){
        mViewPager.setCurrentItem(fragmentNum);
    }
}


package khalil.cointrader;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by khalil on 1/16/18.
 */

public class Utils {
    public static void copyStream(InputStream is, OutputStream os){
        final int buffer_size = 1024;

        try {
            byte[] bytes = new byte[buffer_size];
            for(;;){
                int count = is.read(bytes, 0, buffer_size);
                if(count == -1)
                    break;
                os.write(bytes, 0 , count);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


package khalil.cointrader;

import android.content.Context;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.jar.Attributes;

import javax.crypto.Mac;

/**
 * Created by khalil on 12/16/17.
 */

public class Controller extends MainActivity {

    private RequestQueue mRequestQueue;
    public JsonObjectRequest stringRequest;
    private String coinURL = "https://api.coinbase.com/v2/prices/BTC-USD/buy";
    public String BTCURL = "https://api.coinbase.com/v2/prices/BTC-USD/buy";
    public String ETHURL = "https://api.coinbase.com/v2/prices/ETH-USD/buy";
    public String LTCURL = "https://api.coinbase.com/v2/prices/LTC-USD/buy";
    private static final String TAG = MainActivity.class.getName();
    private static final String REQUESTTAG = "Price Request";
    private Context mContext;
    private String[] mMarkets;
    private ListView listView;
    private List<Ticker> mTicker;
    Mac sha512_HMAC = null;
    String result = null;
    String key = "YOUR_KEY_HERE";


    public Controller(Context context, String[] mMarket){
        this.mContext=context;
        this.mMarkets = mMarkets;
        this.listView = listView;
        this.mMarkets = mMarket;
    }

    public interface VolleyCallback {
        void onSuccessResponse(String[] result);
    }

    public void getMarketSummaryResponse(final String url, final String url2, final VolleyCallback callback, final Integer[] imageID){
        mRequestQueue = Volley.newRequestQueue(mContext);
        stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                getMarketResponse(response.toString(), url2, callback, imageID);
                //callback.onSuccessResponse(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "HTTP Error: " + error.toString());
            }
        });

        stringRequest.setTag(REQUESTTAG);
        mRequestQueue.add(stringRequest);
    }

    public void getMarketResponse(final String response0, String url, final VolleyCallback callback, final Integer[] imageID){
        mRequestQueue = Volley.newRequestQueue(mContext);
        stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String[] responses = new String[2];
                responses[0] = response0;
                responses[1] = response.toString();
                callback.onSuccessResponse(responses);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "HTTP Error: " + error.toString());
            }
        });

        stringRequest.setTag(REQUESTTAG);
        mRequestQueue.add(stringRequest);
    }

    public String formatDecimalPrice(String value) {
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        return df.format(Double.valueOf(value));
    }

    public List<Ticker> populateTickers(String marketSummaryResponse, String marketResponse, Integer[] imageID, String[] NameLong) {
        String MarketName;
        String FullMarketName;
        String High;
        String Low;
        Double Volume;
        String Last;
        Double BaseVolume;
        String TimeStamp;
        String Ask;
        String OpenBuyOrders;
        String OpenSellOrders;
        String PrevDay;
        String PercentChange;
        String Price;
        Double prevDay;
        Double currentPriceDouble;
        String sign;
        String dollarChange;
        String percentChange;
        String currency;
        Integer Logo;

        mTicker = new ArrayList<Ticker>();

        List<HashMap<String, String>> summaryMap;

        if (!marketSummaryResponse.contains("\"success\":false")) {
            summaryMap = Bittrex.getMapsFromResponse(marketSummaryResponse);

            for(int i = 0; i < summaryMap.size(); i++){
                HashMap<String, String> onlyMap = summaryMap.get(i);
                MarketName = onlyMap.get("MarketName");
                FullMarketName = "";
                High = onlyMap.get("High");
                Low = onlyMap.get("Low");
                Volume = Double.parseDouble(onlyMap.get("Volume"));
                Last = onlyMap.get("Last");
                BaseVolume = Double.parseDouble(onlyMap.get("BaseVolume"));
                TimeStamp = onlyMap.get("TimeStamp");
                Ask = onlyMap.get("Ask");
                OpenBuyOrders = onlyMap.get("OpenBuyOrders");
                OpenSellOrders = onlyMap.get("OpenSellOrders");
                PrevDay = onlyMap.get("PrevDay");
                Logo = 0;

                prevDay = Double.parseDouble(onlyMap.get("PrevDay"));
                currentPriceDouble = Double.parseDouble(Last);

                if ((currentPriceDouble - prevDay) > 0) {
                    sign = "+";
                    //mPercentGain.setTextColor(getResources().getColor(R.color.greenColor));
                } else {
                    sign = "-";
                    //mPercentGain.setTextColor(getResources().getColor(R.color.redColor));
                }

                if(MarketName.split("-")[0].equals("USDT"))
                    currency = "$";
                else
                    currency = "";

                dollarChange = "(" + currency + formatDecimalSetoci(Double.toString(currentPriceDouble - prevDay)) + ")";
                percentChange = sign + formatDecimal(Double.toString((Math.abs(prevDay - currentPriceDouble) / prevDay) * 100)) + "%";

                //PercentChange = (percentChange + " " + dollarChange);
                PercentChange = percentChange;
                Price = formatDecimalSetoci(Last);

                Ticker ticker;
                ticker = new Ticker(Logo, FullMarketName, MarketName, High, Low, Volume, Last, BaseVolume, TimeStamp, Ask,
                        OpenBuyOrders, OpenSellOrders, PrevDay, PercentChange, Price, null, null);

                mTicker.add(ticker);
            }
            populateLogoUrls(marketResponse);
        } else
            System.out.println("ERROR! Response: " + marketSummaryResponse);


        Collections.sort(mTicker, new Comparator<Ticker>() {
            @Override
            public int compare(Ticker t1, Ticker t2) {
                return Double.compare(t2.getBaseVolume(), t1.getBaseVolume());
            }
        });

        return mTicker;
    }

    private void populateLogoUrls(String marketResponse) {
        List<HashMap<String, String>> marketMap;

        if (!marketResponse.contains("\"success\":false")) {
            marketMap = Bittrex.getMapsFromResponse(marketResponse);

            Collections.sort(marketMap, new Comparator<HashMap<String, String>>(){
                public int compare(HashMap<String, String> one, HashMap<String, String> two) {
                    return one.get("MarketName").compareTo(two.get("MarketName"));
                }
            });

            for(int i = 0; i < mTicker.size(); i++) {
                HashMap<String, String> onlyMap = marketMap.get(i);
                String url = onlyMap.get("LogoUrl");
                String coin_name = onlyMap.get("MarketCurrencyLong");

                mTicker.get(i).setLogoUrl(url);
                mTicker.get(i).setNameLong(coin_name);
                if(mTicker.get(i).getLogoUrl() != null && mTicker.get(i).getLogoUrl().equals("https://bittrex.com/Content/img/symbols/BTC.png"))
                    mTicker.get(i).setLogoUrl("https://cdn3.iconfinder.com/data/icons/circle-payment-methods-4/512/Bitcoin-512.png");

            }
        }
    }

    public String formatDecimal(String value) {
        DecimalFormat df;
        if (!(value == null)) {
            df = new DecimalFormat("#,###,##0.00");
            return df.format(Double.valueOf(value));
        } else {
            return ("-");
        }
    }

    public String formatDecimalSetoci(String value) {
        DecimalFormat df = new DecimalFormat("#,###,##0.00######");
        return df.format(Double.valueOf(value));
    }


    @Override
    protected void onStop(){
        super.onStop();
        if(mRequestQueue!=null){
            mRequestQueue.cancelAll(REQUESTTAG);
        }
    }
}


package khalil.cointrader;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by khalil on 12/22/17.
 */

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {


    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title){
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }
}


package khalil.cointrader;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] coinSymbol;
    //private final Integer[] imageID;
    private final String[] mTickerPrice;
    private final String[] mCoinName;
    private final String[] mPercentChange;
    private final String[] mHoldings;
    private final String[] mHoldingsUSD;
    private final String[] mLogoURL;

    private String mPriceTrimmed;
    private Integer[] mImageID;
    private ImageLoader imageLoader;

    static class ViewHolder {
        TextView symbol;
        TextView coin_name;
        TextView current_price;
        TextView percent;
        ImageView arrow;
        ImageView logo;
    }

    public CustomListAdapter(Activity context, Integer[] image_ID, String[] coin_symbol, String[] coin_name,
                             String[] tickerPrice, String[] percentChange, String[] holdings, String[] holdingsUSD,
                             String [] logoURL) {
        super(context, R.layout.row_layout, coin_symbol);
        // TODO Auto-generated constructor stub

        this.context= context;
        this.coinSymbol= coin_symbol;
        this.mCoinName = coin_name;
        this.mTickerPrice = tickerPrice;
        this.mPercentChange = percentChange;
        this.mHoldings = holdings;
        this.mHoldingsUSD = holdingsUSD;
        this.mImageID = image_ID;
        this.mLogoURL = logoURL;

        imageLoader = new ImageLoader(context.getApplicationContext());
    }

    public View getView(int position, View rowView, ViewGroup parent) {

        ViewHolder holder;
        if(rowView == null) {
            LayoutInflater inflater=context.getLayoutInflater();
            rowView=inflater.inflate(R.layout.row_layout, null,true);
            holder = new ViewHolder();
            holder.symbol = (TextView) rowView.findViewById(R.id.symbol);
            holder.coin_name = (TextView) rowView.findViewById(R.id.coin_name);
            holder.current_price = (TextView) rowView.findViewById(R.id.current_price);
            holder.percent = (TextView) rowView.findViewById(R.id.percent_gain);
            //holder.arrow = (ImageView) rowView.findViewById(R.id.arrow);
            holder.logo = (ImageView) rowView.findViewById(R.id.logo);
            rowView.setBackgroundResource(R.drawable.selector_list_background);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        if(mCoinName[position] != null)
            holder.coin_name.setText(mCoinName[position]);

        if(mImageID[position] != null)
            holder.logo.setBackgroundResource(mImageID[position]);

        if(mPercentChange[position] != null && mPercentChange[position].charAt(0) == '+') {
            //holder.percent.setTextColor(Color.parseColor("#4CA53B"));
            holder.percent.setBackgroundResource(R.drawable.green_percentage);
        }
        else if (mPercentChange[position] != null) {
            //holder.percent.setTextColor(Color.parseColor("#F34827"));
            holder.percent.setBackgroundResource(R.drawable.red_percentage);

        }

        updatePrice(holder, position);

        holder.percent.setText(mPercentChange[position]);
        holder.symbol.setText(coinSymbol[position]);
        //logo.setBackgroundResource(imageID[position]);

        ImageView image = holder.logo;
        imageLoader.displayImage(mLogoURL[position], image);
        return rowView;

    }

    public void updatePrice(ViewHolder holder, Integer position){
        final ObjectAnimator animator;



        if(mTickerPrice[position] != null && mTickerPrice[position].length() > 10)
            mPriceTrimmed = mTickerPrice[position].substring(0,10);
        else
            mPriceTrimmed = mTickerPrice[position];


        if(holder.current_price.getText() != null && !holder.current_price.getText().toString().equals("")){
            Double currentPrice = Double.parseDouble(holder.current_price.getText().toString().replaceAll(",",""));
            Double newPrice = Double.parseDouble(mPriceTrimmed.replaceAll(",",""));

            final Property<TextView, Integer> property = new Property<TextView, Integer>(int.class, "textColor"){
                @Override
                public Integer get(TextView object){
                    return object.getCurrentTextColor();
                }
                @Override
                public void set(TextView object, Integer value){
                    object.setTextColor(value);
                }
            };

            if (currentPrice > newPrice) {
                animator = ObjectAnimator.ofInt(holder.current_price, property, Color.GREEN, Color.WHITE);
                animator.setDuration(2000);
                animator.setEvaluator(new ArgbEvaluator());
                animator.start();
            } else if (currentPrice < newPrice){
                animator = ObjectAnimator.ofInt(holder.current_price, property, Color.RED, Color.WHITE);
                animator.setDuration(2000);
                animator.setEvaluator(new ArgbEvaluator());
                animator.start();
            }

            holder.current_price.setText(mPriceTrimmed);
        } else {
            holder.current_price.setText(mPriceTrimmed);
        }
    }
}


package khalil.cointrader;

import android.graphics.Bitmap;
import android.util.Log;

import java.security.KeyStore;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by khalil on 1/15/18.
 */

public class MemoryCache {
    private static final String TAG = "MemoryCache";

    private Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10, 1.5f, true));

    private long size = 0;
    private long limit = 1000000;

    public MemoryCache(){
        setLimit(Runtime.getRuntime().maxMemory()/4);
    }

    public void setLimit(long new_limit){
        limit = new_limit;
        Log.i(TAG, "Memory cache will use up to " + limit / 1024. / 1024. + "MB");
    }

    public Bitmap get(String id){
        try{
            if(!cache.containsKey(id))
                return null;

            return cache.get(id);

        } catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    public void put(String id, Bitmap bitmap){
        try{
            if(cache.containsKey(id))
                size -= getSizeInBytes(cache.get(id));

            cache.put(id, bitmap);
            size += getSizeInBytes(bitmap);
            checkSize();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void checkSize() {
        Log.i(TAG, "Cache size = " + size + " length = " + cache.size());
        if(size > limit){
            Iterator<Map.Entry<String, Bitmap>> iter = cache.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry<String, Bitmap> entry = iter.next();
                size -= getSizeInBytes(entry.getValue());
                iter.remove();
                if(size <= limit)
                    break;
            }
            Log.i(TAG, "Clean cache. New size " + cache.size());
        }
    }

    public void clear(){
        try{
            cache.clear();
            size = 0;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public long getSizeInBytes(Bitmap bitmap){
        if(bitmap == null)
            return 0;
        return bitmap.getRowBytes() * bitmap.getHeight();
    }
}


package khalil.cointrader;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class Bittrex {

	public static final String ORDERBOOK_BUY = "buy", ORDERBOOK_SELL = "sell", ORDERBOOK_BOTH = "both";
	public static final int DEFAULT_RETRY_ATTEMPTS = 1;
	public static final int DEFAULT_RETRY_DELAY = 15;
	private static final Exception InvalidStringListException = new Exception("Must be in key-value pairs");
	private final String API_VERSION = "1.1", INITIAL_URL = "https://bittrex.com/api/";
	private final String PUBLIC = "public", MARKET = "market", ACCOUNT = "account";
	private final String encryptionAlgorithm = "HmacSHA512";
	private String apikey;
	private String secret;
	private String mResult;
	private final int retryAttempts;
	private int retryAttemptsLeft;
	private final int retryDelaySeconds;
	private String mBaseUrl;

	public Bittrex(String apikey, String secret, int retryAttempts, int retryDelaySeconds) {

		this.apikey = apikey;
		this.secret = secret;
		this.retryAttempts = retryAttempts;
		this.retryDelaySeconds = retryDelaySeconds;
		
		retryAttemptsLeft = retryAttempts;
	}

	public Bittrex(int retryAttempts, int retryDelaySeconds) {
		
		this.retryAttempts = retryAttempts;
		this.retryDelaySeconds = retryDelaySeconds;
		
		retryAttemptsLeft = retryAttempts;
	}
	
	public Bittrex() {

		this(DEFAULT_RETRY_ATTEMPTS, DEFAULT_RETRY_DELAY);
	}

	public void setAuthKeysFromTextFile(String textFile) { // Add the text file containing the key & secret in the same path as the source code

		try (Scanner scan = new Scanner(getClass().getResourceAsStream(textFile))) {

			String apikeyLine = scan.nextLine(), secretLine = scan.nextLine();

			apikey = apikeyLine.substring(apikeyLine.indexOf("\"") + 1, apikeyLine.lastIndexOf("\""));
			secret = secretLine.substring(secretLine.indexOf("\"") + 1, secretLine.lastIndexOf("\""));

		} catch (NullPointerException | IndexOutOfBoundsException e) {

			System.err.println("Text file not found or corrupted - please attach key & secret in the format provided.");
			apikey = "YOUR_API_KEY_HERE";
			secret = "YOUR_SECRET_HERE";
		}
	}

	public String getMarkets() { // Returns all markets with their metadata

		return getJson(API_VERSION, PUBLIC, "getmarkets");
	}

	public String getCurrencies() { // Returns all currencies currently on Bittrex with their metadata

		return getJson(API_VERSION, PUBLIC, "getcurrencies");
	}

	public String getTicker(String market) { // Returns current tick values for a specific market

		return getJson(API_VERSION, PUBLIC, "getticker", returnCorrectMap("market", market));
	}

	public String getMarketSummaries() { // Returns a 24-hour summary of all markets

		return getJson(API_VERSION, PUBLIC, "getmarketsummaries");
	}

	public String getMarketSummary(String market) { // Returns a 24-hour summar for a specific market

		return getJson(API_VERSION, PUBLIC, "getmarketsummary", returnCorrectMap("market", market));
	}

	public String getOrderBook(String market, String type) { // Returns the orderbook for a specific market

		return getJson(API_VERSION, PUBLIC, "getorderbook", returnCorrectMap("market", market, "type", type));
	}

	public String getMarketHistory(String market) { // Returns latest trades that occurred for a specific market

		return getJson(API_VERSION, PUBLIC, "getmarkethistory", returnCorrectMap("market", market));
	}

	public String buyLimit(String market, String quantity, String rate) { // Places a limit buy in a specific market; returns the UUID of the order

		return getJson(API_VERSION, MARKET, "buylimit", returnCorrectMap("market", market, "quantity", quantity, "rate", rate));
	}

	public String buyMarket(String market, String quantity) { // Places a market buy in a specific market; returns the UUID of the order

		return getJson(API_VERSION, MARKET, "buymarket", returnCorrectMap("market", market, "quantity", quantity));
	}

	public String sellLimit(String market, String quantity, String rate) { // Places a limit sell in a specific market; returns the UUID of the order

		return getJson(API_VERSION, MARKET, "selllimit", returnCorrectMap("market", market, "quantity", quantity, "rate", rate));
	}

	public String sellMarket(String market, String quantity) { // Places a market sell in a specific market; returns the UUID of the order

		return getJson(API_VERSION, MARKET, "sellmarket", returnCorrectMap("market", market, "quantity", quantity));
	}

	public String cancelOrder(String uuid) { // Cancels a specific order based on its UUID

		return getJson(API_VERSION, MARKET, "cancel", returnCorrectMap("uuid", uuid));
	}

	public String getOpenOrders(String market) { // Returns your currently open orders in a specific market

		String method = "getopenorders";

		if(market.equals(""))

			return getJson(API_VERSION, MARKET, method);

		return getJson(API_VERSION, MARKET, method, returnCorrectMap("market", market));
	}

	public String getOpenOrders() { // Returns all your currently open orders

		return getOpenOrders("");
	}

	public String getBalances() { // Returns all balances in your account

		return getJson(API_VERSION, ACCOUNT, "getbalances");
	}

	public String getBalance(String currency) { // Returns a specific balance in your account

		return getJson(API_VERSION, ACCOUNT, "getbalance", returnCorrectMap("currency", currency));
	}

	public String getDepositAddres(String currency) { // Returns the deposit address for a specific currency - if one is not found, it will be generated

		return getJson(API_VERSION, ACCOUNT, "getdepositaddress", returnCorrectMap("currency", currency));
	}

	public String withdraw(String currency, String quantity, String address, String paymentId) { // Withdraw a certain amount of a specific coin to an address, and add a payment id

		String method = "withdraw";

		if(paymentId.equals(""))

			return getJson(API_VERSION, ACCOUNT, method, returnCorrectMap("currency", currency, "quantity", quantity, "address", address));

		return getJson(API_VERSION, ACCOUNT, method, returnCorrectMap("currency", currency, "quantity", quantity, "address", address, "paymentid", paymentId));
	}

	public String withdraw(String currency, String quantity, String address) { // Withdraw a certain amount of a specific coin to an address

		return withdraw(currency, quantity, address, "");
	}

	public String getOrder(String uuid) { // Returns information about a specific order (by UUID)

		return getJson(API_VERSION, ACCOUNT, "getorder", returnCorrectMap("uuid", uuid));
	}

	public String getOrderHistory(String market) { // Returns your order history for a specific market

		String method = "getorderhistory";

		if(market.equals(""))

			return getJson(API_VERSION, ACCOUNT, method);

		return getJson(API_VERSION, ACCOUNT, method, returnCorrectMap("market", market));
	}

	public String getOrderHistory() { // Returns all of your order history

		return getOrderHistory("");
	}

	public String getWithdrawalHistory(String currency) { // Returns your withdrawal history for a specific currency

		String method = "getwithdrawalhistory";

		if(currency.equals(""))

			return getJson(API_VERSION, ACCOUNT, method);

		return getJson(API_VERSION, ACCOUNT, method, returnCorrectMap("currency", currency));
	}

	public String getWithdrawalHistory() { // Returns all of your withdrawal history

		return getWithdrawalHistory("");
	}

	public String getDepositHistory(String currency) { // Returns your deposit history for a specific currency

		String method = "getdeposithistory";

		if(currency.equals(""))

			return getJson(API_VERSION, ACCOUNT, method);

		return getJson(API_VERSION, ACCOUNT, method, returnCorrectMap("currency", currency));
	}

	public String getDepositHistory() { // Returns all of your deposit history

		return getDepositHistory("");
	}

	private HashMap<String, String> returnCorrectMap(String...parameters) { // Handles the exception of the generateHashMapFromStringList() method gracefully as to not have an excess of try-catch statements

		HashMap<String, String> map = null;

		try {

			map = generateHashMapFromStringList(parameters);

		} catch (Exception e) {

			e.printStackTrace();
		}

		return map;
	}

	private HashMap<String, String> generateHashMapFromStringList(String...strings) throws Exception { // Method to easily create a HashMap from a list of Strings

		if(strings.length % 2 != 0)

			throw InvalidStringListException;

		HashMap<String, String> map = new HashMap<>();

		for(int i = 0; i < strings.length; i += 2) // Each key will be i, with the following becoming its value

			map.put(strings[i], strings[i + 1]);

		return map;
	}

	private String getJson(String apiVersion, String type, String method) {

		return getResponseBody(generateUrl(apiVersion, type, method));
	}

	private String getJson(String apiVersion, String type, String method, HashMap<String, String> parameters) {

		return getResponseBody(generateUrl(apiVersion, type, method, parameters));
	}

	private String generateUrl(String apiVersion, String type, String method) {

		return generateUrl(apiVersion, type, method, new HashMap<String, String>());
	}

	private String generateUrl(String apiVersion, String type, String method, HashMap<String, String> parameters) {

		String url = INITIAL_URL;

		url += "v" + apiVersion + "/";
		url += type + "/";
		url += method;
		url += generateUrlParameters(parameters);

		return url;
	}

	private String generateUrlParameters(HashMap<String, String> parameters) { // Returns a String with the key-value pairs formatted for URL

		String urlAttachment = "?";

		Object[] keys = parameters.keySet().toArray();

		for(Object key : keys)

			urlAttachment += key.toString() + "=" + parameters.get(key) + "&";

		return urlAttachment;
	}

	public static List<HashMap<String, String>> getMapsFromResponse(String response) {

		final List<HashMap<String, String>> maps = new ArrayList<>();

		if(!response.contains("[")) {

			maps.add(jsonMapToHashMap(response.substring(response.lastIndexOf("\"result\":") + "\"result\":".length(), response.indexOf("}") + 1))); // Sorry.

		} else {

			final String resultArray = response.substring(response.indexOf("\"result\":") + "\"result\":".length() + 1, response.lastIndexOf("]"));

			final String[] jsonMaps = resultArray.split(",(?=\\{)");

			for(String map : jsonMaps)

				maps.add(jsonMapToHashMap(map));
		}

		return maps;
	}

	private static HashMap<String, String> jsonMapToHashMap(String jsonMap) {

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		return gson.fromJson(jsonMap, new TypeToken<HashMap<String, String>>(){}.getType());
	}

	private String getResponseBody(final String baseUrl) {
        try {
        	mBaseUrl = baseUrl;
            mResult = new spinProgressBar().execute(mBaseUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

		return mResult;
	}

	public class spinProgressBar extends AsyncTask<String, Void, String> {

		/**
		 * Background task to sleep a thread for 1 second while the
		 * progress circle spins.
		 * upon successful check-in.
		 * @param args no parameters needed for this task.
		 * @return null
		 */
		private String urlString;
		private String result = null;

		@Override
		protected String doInBackground(String... args) {

			try {
				URL url = new URL(urlString);
				HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
				httpsURLConnection.setRequestMethod("GET");
				httpsURLConnection.setRequestProperty("apisign", EncryptionUtility.calculateHash(secret, urlString, encryptionAlgorithm));
				BufferedReader reader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));

				StringBuffer resultBuffer = new StringBuffer();
				String line = "";

				while ((line = reader.readLine()) != null)

					resultBuffer.append(line);

				result = resultBuffer.toString();
				//System.out.println("RESULT:   " + result);
				mResult = result;

			} catch (UnknownHostException | SocketException e) {

				if(retryAttemptsLeft-- > 0) {

					System.err.printf("Could not connect to host - retrying in %d seconds... [%d/%d]%n", retryDelaySeconds, retryAttempts - retryAttemptsLeft, retryAttempts);

					try {
						Thread.sleep(retryDelaySeconds * 1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					result = getResponseBody(mBaseUrl);

				} else {
					throw new ReconnectionAttemptsExceededException("Maximum amount of attempts to connect to host exceeded.");
				}

			} catch (IOException e) {

				e.printStackTrace();

			} finally {

				retryAttemptsLeft = retryAttempts;
			}
			return result;
		}

		/**
		 * After waiting one second, hide the progress circle.
		 */
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

		/**
		 * Make the progress circle animation visible to
		 * the user.
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			urlString = mBaseUrl + "apikey=" + apikey + "&nonce=" + EncryptionUtility.generateNonce();
			System.out.println("BASE URL: " + urlString);
			String header = ("apisign: " + EncryptionUtility.calculateHash(secret, urlString, encryptionAlgorithm));
		}
	}
}


package khalil.cointrader;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

/**
 * Created by khalil on 12/22/17.
 */

public class Fragment2 extends Fragment {
    private ListView listView;
    private Toast mToast;
    private Bittrex wrapper;
    public JSONObject mJSONResponse;
    private List<Ticker> mTickerList;
    private String[] mTickers;
    private String[] mTickerBalances;
    private String[] mTickerBalancesUSD;
    private TextView mTicker;
    private TextView mTickerBalance;
    private TextView mTickerBalanceUSD;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);


    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        mTicker = (TextView) getActivity().findViewById(R.id.balance_tickers);
        mTickerBalance = (TextView) getActivity().findViewById(R.id.balance_holdings);
        mTickerBalanceUSD = (TextView) getActivity().findViewById(R.id.balance_holdingsUSD);
        wrapper = new Bittrex();
        wrapper.setAuthKeysFromTextFile("keys.txt");

        new Thread(new Runnable() {
            public void run() {
                // a potentially  time consuming task
                pullBalances();
                updateTextViews();
            }
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment2_layout, container, false);

        //Initialize the views
        //Pull Bitcoin-USD Data
        //Pull Ticker Data
        //Convert Ticker share to bitcoin shares, then bitcoin shares to USD
        return v;
    }

    private void pullBalances() {
        String rawResponse = wrapper.getBalances();
        String marketResponse = wrapper.getMarketSummaries();

        System.out.println("BALANCE INFORMATION: " + rawResponse);
        List<HashMap<String, String>> responseMapList;

        if(rawResponse.contains("\"success\":true")) {
            responseMapList = Bittrex.getMapsFromResponse(rawResponse);

            mTickers = new String[responseMapList.size()];
            mTickerBalances = new String[responseMapList.size()];
            mTickerBalancesUSD = new String[responseMapList.size()];

            for(int i = 0; i < responseMapList.size(); i++){
                final HashMap<String, String> balance = responseMapList.get(i);
                    mTickers[i] = balance.get("Currency");
                    mTickerBalances[i] = balance.get("Balance");
            }
        }
        else
            System.out.println("ERROR! Response: " + rawResponse);
    }

    private void updateTextViews() {
        String TickerText = "";
        String TickerBalances = "";

        for(int i = 0; i < mTickers.length; i++){
           if(!mTickerBalances[i].equals("0.00000000")) {
               mTicker.append(mTickers[i] + "\n");
               mTickerBalance.append(mTickerBalances[i].substring(0, 8) + "\n");
           }
        }
    }
}


package khalil.cointrader;

/**
 * Created by khalil on 12/22/17.
 */

public class Ticker {
    private String MarketName;
    private String NameLong;
    private String High;
    private String Low;
    private Double Volume;
    private String Last;
    private Double BaseVolume;
    private String TimeStamp;
    private String Ask;
    private String OpenBuyOrders;
    private String OpenSellOrders;
    private String PrevDay;
    private String PercentChange;
    private String Price;
    private String LogoUrl;
    private Integer Logo;

    private String Balance;
    private String BalanceUSD;

    public Ticker(Integer Logo, String NameLong, String MarketName, String High, String Low, Double Volume, String Last,
                  Double BaseVolume, String TimeStamp, String Ask, String OpenBuyOrders,
                  String OpenSellOrders, String PrevDay, String PercentChange, String Price,
                  String Balance, String BalanceUSD){

        this.NameLong = NameLong;
        this.MarketName = MarketName;
        this.High = High;
        this.Low = Low;
        this.Volume = Volume;
        this.Last = Last;
        this.BaseVolume = BaseVolume;
        this.TimeStamp = TimeStamp;
        this.Ask = Ask;
        this.OpenBuyOrders = OpenBuyOrders;
        this.OpenSellOrders = OpenSellOrders;
        this.PrevDay = PrevDay;
        this.PercentChange = PercentChange;
        this.Price = Price;
        this.Balance = Balance;
        this.BalanceUSD=BalanceUSD;
        this.Logo = Logo;
    }

    public String getNameLong() {
        return NameLong;
    }

    public void setNameLong(String nameLong) {
        NameLong = nameLong;
    }

    public Integer getLogo() {
        return Logo;
    }

    public void setLogo(Integer mLogo) {
        this.Logo = mLogo;
    }

    public String getMarketName() {
        return MarketName;
    }

    public void setMarketName(String marketName) {
        MarketName = marketName;
    }

    public String getHigh() {
        return High;
    }

    public void setHigh(String high) {
        High = high;
    }

    public String getLow() {
        return Low;
    }

    public void setLow(String low) {
        Low = low;
    }

    public Double getVolume() {
        return Volume;
    }

    public void setVolume(Double volume) {
        Volume = volume;
    }

    public String getLast() {
        return Last;
    }

    public void setLast(String last) {
        Last = last;
    }

    public Double getBaseVolume() {
        return BaseVolume;
    }

    public void setBaseVolume(Double baseVolume) {
        BaseVolume = baseVolume;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }

    public String getAsk() {
        return Ask;
    }

    public void setAsk(String ask) {
        Ask = ask;
    }

    public String getOpenBuyOrders() {
        return OpenBuyOrders;
    }

    public void setOpenBuyOrders(String openBuyOrders) {
        OpenBuyOrders = openBuyOrders;
    }

    public String getOpenSellOrders() {
        return OpenSellOrders;
    }

    public void setOpenSellOrders(String openSellOrders) {
        OpenSellOrders = openSellOrders;
    }

    public String getPrevDay() {
        return PrevDay;
    }

    public void setPrevDay(String prevDay) {
        PrevDay = prevDay;
    }

    public String getPercentChange() {
        return PercentChange;
    }

    public void setPercentChange(String percentChange) {
        PercentChange = percentChange;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public void viewTickers(){
        System.out.println(this.getMarketName());
    }

    public String getBalanceUSD() {
        return BalanceUSD;
    }

    public void setBalanceUSD(String balanceUSD) {
        BalanceUSD = balanceUSD;
    }

    public String getBalance() {
        return Balance;
    }

    public void setBalance(String balance) {
        Balance = balance;
    }

    public String getLogoUrl() {
        return LogoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        LogoUrl = logoUrl;
    }
}


package khalil.cointrader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import khalil.cointrader.Controller;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class SecondActivity extends AppCompatActivity {

    private TextView mBanner;
    private ImageView mBannerBackground;
    private ImageView mBalance;
    private ImageView mOrderType;
    private ImageView mLogo;
    private TextView mAmount;
    private TextView mAmountType2;
    private TextView mAmountType;
    public TextView mLimitStop;
    public TextView mPrice;
    private TextView mPercentGain;
    private TextView mCoinBalance;
    private TextView mUSDBalance;
    private TextView mSymbol;
    private TextView mCoinName;
    public EditText mEditAmount;
    public EditText mEditLimitStop;
    public JSONObject mJSONresponse;
    private Button marketButton;
    private Button limitButton;
    private Button stopButton;
    private Button mBuyButton;
    private Button mSellButton;
    private Button mBackArrow;
    public String mOrder = "MARKET";
    public String mCurrentPrice = "-";
    public String mSelectedCurrency;
    public Toast mToast;
    private ProgressBar mPriceProgress;
    Timer mtimer;
    private Controller Controller;
    private Bittrex wrapper;
    private Double mCurrentPriceDouble;
    private ImageLoader imageLoader;

    private static final String API_KEY = "5573a4c7e31431ba97fb6163e6494d4a";
    private static final String API_SECRET = "O4IXSLZBO4f/l/WVJk9K6d4LyCgQBQ2LHNDzyBTszA2d+DWjR7lkdsYSRrFNqevAkqq64eRbYNjgfSbQsgNvvQ==";
    private static final String API_PASS = "sthtqgl8ocb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity2_layout);
        wrapper = new Bittrex();
        wrapper.setAuthKeysFromTextFile("keys.txt");
        mBanner = (TextView) findViewById(R.id.banner);
        mBannerBackground = (ImageView) findViewById(R.id.banner_background);
        mBalance = (ImageView) findViewById(R.id.balance);
        mOrderType = (ImageView) findViewById(R.id.order_type);
        mLogo = (ImageView) findViewById(R.id.logo);

        mAmount = (TextView) findViewById(R.id.amount);
        mAmountType = (TextView) findViewById(R.id.amount_currency);
        mAmountType2 = (TextView) findViewById(R.id.amount_usd);
        mLimitStop = (TextView) findViewById(R.id.limit);
        mPrice = (TextView) findViewById(R.id.current_price);
        mPercentGain = (TextView) findViewById(R.id.percent_gain);
        mCoinBalance = (TextView) findViewById(R.id.balance_coin);
        mUSDBalance = (TextView) findViewById(R.id.balance_usd);

        mSymbol = (TextView) findViewById(R.id.symbol);
        mCoinName = (TextView) findViewById(R.id.coin_name);
        imageLoader = new ImageLoader(getApplicationContext());

        String[] coins = getResources().getStringArray(R.array.Markets);
        String[] coin_symbol = getResources().getStringArray(R.array.Symbols);
        String[] fullCoinName = getResources().getStringArray(R.array.CoinName);

        int coin_index = getSelectedCoin();
        //mSelectedCurrency = coins[coin_index];
        mSymbol.setText(mSelectedCurrency);
        //mCoinName.setText(fullCoinName[coin_index]);
        //mLogo.setBackgroundResource(imageId[coin_index]);
        mAmount.setText("Amount (" + mSelectedCurrency.split("-")[1] + ")");



        mEditAmount = (EditText) findViewById(R.id.edit_amount);
        mEditLimitStop = (EditText) findViewById(R.id.edit_limit);
        mEditAmount.setBackgroundResource(R.drawable.selector_textwindow);
        mEditLimitStop.setBackgroundResource(R.drawable.selector_textwindow);
        mAmountType.setText(mSelectedCurrency.split("-")[1]);
        mAmountType2.setText(mSelectedCurrency.split("-")[0]);

        mPriceProgress = (ProgressBar) findViewById(R.id.price_progressBar);
        mPriceProgress.setVisibility(View.INVISIBLE);

        mBackArrow = (Button) findViewById(R.id.back_arrow);
        marketButton = (Button) findViewById(R.id.button_market);
        limitButton = (Button) findViewById(R.id.button_limit);
        stopButton = (Button) findViewById(R.id.button_stop);
        mBuyButton = (Button) findViewById(R.id.buy_button);
        mSellButton = (Button) findViewById(R.id.sell_button);
        mBuyButton.setBackgroundResource(R.drawable.selector_buy);
        mSellButton.setBackgroundResource(R.drawable.selector_sell);

        //Initialize Views
        mOrderType.setBackgroundResource(R.drawable.market_select);
        mBalance.setBackgroundResource(R.drawable.balance);

        mLimitStop.setText("Price");
        mEditAmount.setHint("0.00");
        mEditLimitStop.setHint(R.string.hint1);
        mEditLimitStop.setFocusable(false);
        mEditLimitStop.setAlpha(0.5f);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecondActivity.this.finish();
            }
        });

        marketButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mLimitStop.setText("Price");
                mEditLimitStop.setText("");
                mEditLimitStop.setHint(R.string.hint1);
                mEditLimitStop.setError(null);
                mEditLimitStop.setFocusable(false);
                mEditLimitStop.setAlpha(0.5f);

                //Limit changes to home tab only

                mOrderType.setBackgroundResource(R.drawable.market_select);
                mOrder = "MARKET";
                //SET TO MARKET ORDER
            }
        });

        limitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mLimitStop.setText("Limit Price");
                mEditLimitStop.setHint("0.00");
                mEditLimitStop.setFocusableInTouchMode(true);
                mEditLimitStop.setAlpha(1f);
                mEditAmount.setHint("0.00");

                //Limit changes to home tab only
                mOrderType.setBackgroundResource(R.drawable.limit_select);
                mOrder = "LIMIT";

                //SET TO LIMIT ORDER
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mLimitStop.setText("Stop Price");
                mEditLimitStop.setHint("0.00");
                mEditLimitStop.setFocusableInTouchMode(true);
                mEditLimitStop.setAlpha(1f);
                mEditAmount.setHint("0.00");

                //Limit changes to home tab only

                mOrderType.setBackgroundResource(R.drawable.stop_select);
                mOrder = "STOP";

                //SET TO STOP ORDER
            }
        });

        mEditAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                } else {
                    scrollDown();
                }

                String text1 = mEditAmount.getText().toString();
                if(!text1.equals("")) {
                    mEditAmount.setText(formatDecimalSetoci(text1));
                }
            }
        });

        mEditLimitStop.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }

                String text2 = mEditLimitStop.getText().toString();

                if(!text2.equals("")) {
                    mEditLimitStop.setText(formatDecimalSetoci(text2));
                }

                String balance = mUSDBalance.getText().toString();

                if(!balance.equals("") && !text2.equals("")) {
                    Double numShares = Double.parseDouble(balance)/Double.parseDouble(text2);
                    mEditAmount.setText(Double.toString(numShares - numShares*.0025));
                }
            }

        });

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                View view = getCurrentFocus();

                if (view != null) {
                    mEditLimitStop.clearFocus();
                    mEditAmount.clearFocus();
                    mLimitStop.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                //Limit changes to home tab only
                //checkAmount();
                buy(mSymbol.getText().toString());

                //SET TO MARKET ORDER
            }
        });

        mSellButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                View view = getCurrentFocus();
                if (view != null) {
                    mEditLimitStop.clearFocus();
                    mEditAmount.clearFocus();
                    mLimitStop.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                //Limit changes to home tab only
                //checkAmount();
                sell(mSymbol.getText().toString());

                //SET TO MARKET ORDER
            }
        });

        startPriceUpdateTimer();
        //testRequest();
    }

    private void scrollDown(){
        final ScrollView scrollview = ((ScrollView) findViewById(R.id.scroll_main));
        scrollview.postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollview.smoothScrollTo(0, 500);
                System.out.println("SCROLL Y POSITION: " + scrollview.getScrollY());
            }
        }, 100);
    }

    private int getSelectedCoin() {
        SharedPreferences mSharedURL = getSharedPreferences("coin_index", MODE_PRIVATE);
        SharedPreferences mSharedPrice = getSharedPreferences("selected_price", MODE_PRIVATE);
        SharedPreferences mSharedPercent = getSharedPreferences("selected_percent", MODE_PRIVATE);
        SharedPreferences mSharedMarket = getSharedPreferences("market", MODE_PRIVATE);
        SharedPreferences mSharedMarketLong = getSharedPreferences("marketlong", MODE_PRIVATE);
        SharedPreferences mSharedLogo = getSharedPreferences("logourl", MODE_PRIVATE);


        String gain = mSharedPercent.getString("selected_percent", "-");

        mSelectedCurrency = mSharedMarket.getString("market", "-");
        mCoinName.setText(mSharedMarketLong.getString("marketlong", "-"));
        imageLoader.displayImage(mSharedLogo.getString("logourl", null), mLogo);

        System.out.println(mSelectedCurrency);
        mPercentGain.setText(gain);

        if(gain.charAt(0) == '+') {
            mPercentGain.setTextColor(getResources().getColor(R.color.greenColor));
        }
        else {
            mPercentGain.setTextColor(getResources().getColor(R.color.redColor));
        }

        mPrice.setText(mSharedPrice.getString("selected_price", "-"));

        return mSharedURL.getInt("coin_index", 0);
    }

    private void toastMessage(String message){
        if(mToast!=null)
            mToast.cancel();
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER_HORIZONTAL,0,900);
        mToast.show();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public String formatDecimal(String value) {
        DecimalFormat df;
        if(!(value == null)) {
            df = new DecimalFormat("#,###,##0.00");
            return df.format(Double.valueOf(value));
        }
        else {
            return ("-");
        }
    }

    public String formatDecimalSetoci(String value) {
        DecimalFormat df = new DecimalFormat("#,###,##0.00######");
        return df.format(Double.valueOf(value));
    }

    public boolean areFieldsValid(){
        boolean isValid = false;
        boolean isAmountEmpty = (mEditAmount.getText().toString().equals("") || mEditAmount.getText().toString().equals("0.00"));
        boolean isStopLimitEmpty = mEditLimitStop.getText().toString().equals("");
        boolean isMarketOrder = mOrder.equals("MARKET");

        if(isMarketOrder){
            if(isAmountEmpty) {
                mEditAmount.setError("Amount is required!");
            } else {
                isValid = true;
            }
        } else {
            if(isAmountEmpty || isStopLimitEmpty){
                if(isAmountEmpty)
                    mEditAmount.setError("Amount is required!");
                if(isStopLimitEmpty)
                    mEditLimitStop.setError(mLimitStop.getText() + " is required!");
            } else {
                isValid = true;
            }
        }
        return isValid;
    }

    public void startPriceUpdateTimer() {

        mtimer = new Timer();
        mtimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                //System.out.println(mSelectedCurrency);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPriceProgress.setVisibility(View.VISIBLE);
                    }
                });

                new Thread(new Runnable() {
                    public void run() {
                        // a potentially  time consuming task
                        updateTicker(wrapper);
                        updateBalance(wrapper);
                    }
                }).start();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPriceProgress.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }, 0, 10000); //Length of how often to update location
    }

    public void updateTicker(Bittrex wrapper) {
        String rawResponse = wrapper.getMarketSummary(mSelectedCurrency);
        Double prevDay;
        final String currentPrice;
        final String percentChange;
        final String dollarChange;
        String sign;
        final String currency;

        List<HashMap<String, String>> responseMapList;
        if(!rawResponse.contains("\"success\":false")) {
            System.out.println(rawResponse);
            responseMapList = Bittrex.getMapsFromResponse(rawResponse);
            HashMap<String, String> onlyMap = responseMapList.get(0);
            prevDay = Double.parseDouble(onlyMap.get("PrevDay"));
            currentPrice = onlyMap.get("Last");
            mCurrentPriceDouble = Double.parseDouble(currentPrice);

            if((mCurrentPriceDouble - prevDay)>0) {
                sign = "+";
                mPercentGain.setTextColor(getResources().getColor(R.color.greenColor));
            }
            else {
                sign = "-";
                mPercentGain.setTextColor(getResources().getColor(R.color.redColor));
            }

            if(onlyMap.get("MarketName").split("-")[0].equals("USDT"))
                currency = "$";
            else
                currency = "";


            dollarChange = "(" + currency + formatDecimalSetoci(Double.toString(mCurrentPriceDouble - prevDay)) + ")";
            percentChange = sign + formatDecimal(Double.toString((Math.abs(prevDay - mCurrentPriceDouble)/prevDay)*100)) + "%";

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPercentGain.setText(percentChange + " " + dollarChange);
                    if(formatDecimalSetoci(currentPrice).length() > 11)
                        mPrice.setText(currency + formatDecimalSetoci(currentPrice).substring(0,9));
                    else
                        mPrice.setText(currency + formatDecimalSetoci(currentPrice));
                }
            });
            // Get wanted value using a key found in the KeySet
            //onlyMap.get("Volume");
        }
        else
            System.out.println("ERROR! Response: " + rawResponse);
    }

    public void updateBalance(Bittrex wrapper){
        String rawResponse = wrapper.getBalance(mSelectedCurrency.split("-")[1]);
        String rawResponse2 = wrapper.getBalance(mSelectedCurrency.split("-")[0]);
        final String coinBalance;
        final String balance2;
        List<HashMap<String, String>> responseMapList;

        if(rawResponse.contains("\"success\":true") && rawResponse2.contains("\"success\":true")) {
            responseMapList = Bittrex.getMapsFromResponse(rawResponse);
            final HashMap<String, String> currency1 = responseMapList.get(0);

            responseMapList = Bittrex.getMapsFromResponse(rawResponse2);
            final HashMap<String, String> currency2 = responseMapList.get(0);

            coinBalance = currency1.get("Balance");
            balance2 = currency2.get("Balance");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!(coinBalance == null)) {
                        String coinBalanceUSD = Double.toString(mCurrentPriceDouble * Double.parseDouble(coinBalance));
                        mCoinBalance.setText(formatDecimalSetoci(coinBalance) + " ($" + formatDecimal(coinBalanceUSD) + ")");
                    }
                    else
                        mCoinBalance.setText("0.000000 ($0.00)");

                    if(!(balance2 == null)) {
                        if(mAmountType2.getText().toString().equals("USDT"))
                            mUSDBalance.setText("$" + formatDecimal(balance2));
                        else
                            mUSDBalance.setText("" + formatDecimalSetoci(balance2));
                    }
                    else
                        mUSDBalance.setText("$0.00");
                }
            });
        }
        else
            System.out.println("ERROR! Response: " + rawResponse);
    }

    public void testRequest(){
        String rawResponse = wrapper.getCurrencies();
        List<HashMap<String, String>> responseMapList;
        if(!rawResponse.contains("\"success\":false")) {
            responseMapList = Bittrex.getMapsFromResponse(rawResponse);
            //responseMapList.get()
            HashMap<String, String> onlyMap;

            for(int i = 0; i < responseMapList.size(); i++){
                onlyMap = responseMapList.get(i);
                System.out.println(onlyMap.get("CurrencyLong") + " ("+onlyMap.get("Currency") + ")");
            }
            // See available information using present keys
            onlyMap = responseMapList.get(0);

            for(String key : onlyMap.keySet())
                System.out.print(key + " ");
            System.out.println();

            System.out.println(responseMapList.size());
        }
        else
            System.out.println("ERROR! Response: " + rawResponse);

    }
    /**
     * Async task which spins a progress circle for 1 second when the distance
     * has been recalculated due to an updated user location.
     */
    public class spinProgressBar extends AsyncTask<Void, Void, Void> {

        /**
         * Background task to sleep a thread for 1 second while the
         * progress circle spins.
         * upon successful check-in.
         * @param args no parameters needed for this task.
         * @return null
         */
        @Override
        protected Void doInBackground(Void... args) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After waiting one second, hide the progress circle.
         */
        @Override
        protected void onPostExecute(Void result) {
            mPriceProgress.setVisibility(View.INVISIBLE);
            super.onPostExecute(result);
        }

        /**
         * Make the progress circle animation visible to
         * the user.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPriceProgress.setVisibility(View.VISIBLE);
        }
    }

    public void buy(String market){
        if(mOrder.equals("MARKET")) {
            if(areFieldsValid()) {
                toastMessage("Placing " + mSelectedCurrency + " BUY Market Order for " + mEditAmount.getText());
                String response = wrapper.buyMarket(market, mEditAmount.getText().toString());
                toastMessage(response);

            }
        }
        else if(mOrder.equals("LIMIT")) {
            if(areFieldsValid()) {
                toastMessage("Placing " + mSelectedCurrency + " BUY Limit Order for " + mEditAmount.getText() +
                        " with a LIMIT of " + mEditLimitStop.getText());
                String response = wrapper.buyLimit(market, mEditAmount.getText().toString(), mEditLimitStop.getText().toString());
                toastMessage(response);
            }
        }
        else if(mOrder.equals("STOP")) {
            if(areFieldsValid()) {
                toastMessage("Sorry, stop order functionality is not available on the Bittrex API");
            }
        }
    }

    public void sell(String market){
        if(mOrder.equals("MARKET")) {
            if(areFieldsValid()) {
                toastMessage("Placing " + mSelectedCurrency + " SELL Market Order for " + mEditAmount.getText());
                String response = wrapper.sellMarket(market, mEditAmount.getText().toString());
                toastMessage(response);

            }
        }
        else if(mOrder.equals("LIMIT")) {
            if(areFieldsValid()) {
                toastMessage("Placing " + mSelectedCurrency + " SELL Limit Order for " + mEditAmount.getText() +
                        " with a LIMIT of " + mEditLimitStop.getText());
                String response = wrapper.sellLimit(market, mEditAmount.getText().toString(), mEditLimitStop.getText().toString());
                toastMessage(response);
            }
        }
        else if(mOrder.equals("STOP")) {
            if(areFieldsValid()) {
                toastMessage("Sorry, stop order functionality is not available on the Bittrex API");
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        this.finish();
        mtimer.cancel();
        mtimer = null;
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mtimer==null)
            startPriceUpdateTimer();
    }
}


package khalil.cointrader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Output;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by khalil on 1/16/18.
 */

public class ImageLoader {
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;

    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    ExecutorService executorService;
    android.os.Handler handler = new android.os.Handler();

    public ImageLoader(Context context){
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
    }

    final int stub_int = R.drawable.loading;

    public void displayImage(String url, ImageView imageView){
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);

        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        } else {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_int);
        }
    }

    private void queuePhoto(String url, ImageView imageView){
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private class PhotoToLoad{
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        public PhotosLoader(PhotoToLoad ptl){
            this.photoToLoad = ptl;
        }


        @Override
        public void run() {
            try {
                if(imageViewReused(photoToLoad))
                    return;

                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);

                if(imageViewReused(photoToLoad))
                    return;

                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            } catch(Throwable e){
                e.printStackTrace();
            }
        }
    }

    private Bitmap getBitmap(String url){
        File f = fileCache.getFile(url);

        Bitmap b = decodeFile(f);

        if(b != null)
            return b;

        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);

            Utils.copyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f);
            return bitmap;

        } catch (Throwable e) {
            e.printStackTrace();
            if(e instanceof OutOfMemoryError)
                memoryCache.clear();

            return null;
        }
    }

    private Bitmap decodeFile(File f){
        try{
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            final int REQUIRED_SIZE = 85;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;

            while(true){
                if(width_tmp/2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE){
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();

            return bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    boolean imageViewReused(PhotoToLoad ptl){
        String tag = imageViews.get(ptl.imageView);

        if(tag == null || !tag.equals(ptl.url))
            return true;

        return false;
    }

    class BitmapDisplayer implements Runnable {

        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p){
            bitmap = b;
            photoToLoad = p;
        }

        @Override
        public void run() {
            if(imageViewReused(photoToLoad))
                return;

            if(bitmap != null){
                photoToLoad.imageView.setVisibility(View.GONE);

                Animation a = new AlphaAnimation(0.00f, 1.00f);
                a.setDuration(500);
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        photoToLoad.imageView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                photoToLoad.imageView.setImageBitmap(bitmap);
                photoToLoad.imageView.startAnimation(a);

            } else {
                photoToLoad.imageView.setImageResource(stub_int);
            }
        }
    }

    public void clearCache(){
        memoryCache.clear();
        fileCache.clear();
    }
}


