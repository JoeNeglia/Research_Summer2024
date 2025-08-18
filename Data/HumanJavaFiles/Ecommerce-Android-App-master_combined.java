package co.sandyedemo.ecomdemo;

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

package co.sandyedemo.ecomdemo;

import android.content.Context;
import android.net.ConnectivityManager;


public class DetectConnection {
    public static boolean checkInternetConnection(Context context) {
        // detect internet connection
        ConnectivityManager con_manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (con_manager.getActiveNetworkInfo() != null
                && con_manager.getActiveNetworkInfo().isAvailable()
                && con_manager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}

package co.sandyedemo.ecomdemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by AbhiAndroid
 */

public class Common {
    public static final String SHARED_PREF = "userData";

    public static void saveUserData(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getSavedUserData(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREF, 0);
        return pref.getString(key, "");

    }
}


package co.sandyedemo.ecomdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import co.sandyedemo.ecomdemo.Activities.Login;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.PaymentIntegrationMethods.OrderConfirmed;
import co.sandyedemo.ecomdemo.Activities.SignUp;
import co.sandyedemo.ecomdemo.Adapters.CartListAdapter;
import co.sandyedemo.ecomdemo.Fragments.ChoosePaymentMethod;
import co.sandyedemo.ecomdemo.Fragments.MyCartList;
import co.sandyedemo.ecomdemo.MVP.CartistResponse;
import co.sandyedemo.ecomdemo.MVP.SignUpResponse;

import co.sandyedemo.ecomdemo.R;

import co.sandyedemo.ecomdemo.Retrofit.Api;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Config {
    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
   // public static final String PAYPAL_CLIENT_ID = "your_paypal_id";
    // id to handle the notification in the notification tray
    public static final String SHARED_PREF = "ah_firebase";

    public static void moveTo(Context context, Class targetClass) {
        Intent intent = new Intent(context, targetClass);
        context.startActivity(intent);
    }
    public static boolean validateEmail(EditText editText,Context context) {
        String email = editText.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            editText.setError(context.getString(R.string.err_msg_email));
            editText.requestFocus();
            return false;
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public static void showCustomAlertDialog(Context context, String title, String msg,int type) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(context, type);
        alertDialog.setTitleText(title);

        if (msg.length() > 0)
            alertDialog.setContentText(msg);
        alertDialog.show();
        Button btn = (Button) alertDialog.findViewById(R.id.confirm_button);
        btn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }

    public static void showLoginCustomAlertDialog(final Context context, String title, String msg, int type) {
        SweetAlertDialog alertDialog = new SweetAlertDialog(context, type);
        alertDialog.setTitleText(title);
        alertDialog.setCancelText("Login");
        alertDialog.setCancelClickListener( new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Config.moveTo(context, Login.class);

            }
        });
        alertDialog.setConfirmText("Signup");
        alertDialog.setConfirmClickListener( new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Config.moveTo(context, SignUp.class);

            }
        });
        if (msg.length() > 0)
            alertDialog.setContentText(msg);
        alertDialog.show();
        Button btn = (Button) alertDialog.findViewById(R.id.confirm_button);
        btn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
        Button btn1 = (Button) alertDialog.findViewById(R.id.cancel_button);
        btn1.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));

    }

    public static void getCartList(final Context context, final boolean b) {
        if (b)
            MainActivity.progressBar.setVisibility(View.VISIBLE);
        MainActivity.cartCount.setVisibility(View.GONE);
        Api.getClient().getCartList(MainActivity.userId, new Callback<CartistResponse>() {
            @Override
            public void success(CartistResponse cartistResponse, Response response) {
                MainActivity.progressBar.setVisibility(View.GONE);
                try {
                    if (cartistResponse.getProducts().size() <= 0) {
                        MainActivity.cartCount.setVisibility(View.GONE);
                    } else {
                        MainActivity.cartCount.setText(cartistResponse.getProducts().size() + "");
                        if (!b) {
                            Log.d("equals", "equals");
                            MainActivity.cartCount.setVisibility(View.GONE);

                        } else {
                            MainActivity.cartCount.setVisibility(View.VISIBLE);

                        }
                    }
                } catch (Exception e) {
                    MainActivity.cartCount.setVisibility(View.GONE);

                }

            }

            @Override
            public void failure(RetrofitError error) {
                MainActivity.progressBar.setVisibility(View.GONE);
            }
        });
    }

    public static void addOrder(final Context context, String transactionId, String paymentMode) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Api.getClient().addOrder(MainActivity.userId,
                MyCartList.cartistResponseData.getCartid(),
                ChoosePaymentMethod.address,
                ChoosePaymentMethod.mobileNo,
                transactionId,
                "succeeded",
                CartListAdapter.totalAmountPayable,
                paymentMode,
                new Callback<SignUpResponse>() {
                    @Override
                    public void success(SignUpResponse signUpResponse, Response response) {
                        progressDialog.dismiss();
                        Intent intent = new Intent(context, OrderConfirmed.class);
                        context.startActivity(intent);
                        ((Activity) context).finishAffinity();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();
                        ((Activity) context).finish();
                    }
                });
    }
}

package co.sandyedemo.ecomdemo.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import co.sandyedemo.ecomdemo.Activities.OptionalImageFullView;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailPageFragment extends Fragment {
    public static DetailPageFragment newInstance(int position, ArrayList<String> imagesList) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putStringArrayList("imageList", imagesList);
        DetailPageFragment fragment = new DetailPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final int position = getArguments().getInt("position");
        final List<String> imagesList = getArguments().getStringArrayList("imageList");
        int layout = R.layout.fragment_page_one;
        View root = inflater.inflate(layout, container, false);
        root.setTag(position);
        ImageView image_one = (ImageView) root.findViewById(R.id.image_one);
        Picasso.with(getActivity())
                .load(imagesList.get(position))
                .placeholder(R.drawable.defaultimage)
                .resize(Integer.parseInt(getResources().getString(R.string.targetProductImageWidth)),Integer.parseInt(getResources().getString(R.string.targetProductImageWidth)))
                .into(image_one);
        image_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFullSize(position, imagesList);

            }
        });
        return root;
    }

    private void showFullSize(int i, List<String> imagesList) {
        OptionalImageFullView.imagesList = imagesList;
        OptionalImageFullView.currentPos = i;
        Intent intent = new Intent(getActivity(), OptionalImageFullView.class);
        startActivity(intent);
    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppInfo extends Fragment {

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.termsLayout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.termsLayout:
                ((MainActivity) getActivity()).loadFragment(new TermsAndConditions(), true);
                break;

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.title.setText("App Info");
        Config.getCartList(getActivity(), true);
    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Activities.AccountVerification;
import co.sandyedemo.ecomdemo.Adapters.ColorListAdapter;
import co.sandyedemo.ecomdemo.Adapters.DetailPageSliderPagerAdapter;
import co.sandyedemo.ecomdemo.Adapters.DotsAdapter;
import co.sandyedemo.ecomdemo.Adapters.MyPagerAdapter;
import co.sandyedemo.ecomdemo.Adapters.SizeListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.AddToWishlistResponse;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ProductDetail extends Fragment {

    View view;
    private static ViewPager mPager;
    int position;
    @BindView(R.id.dotsRecyclerView)
    RecyclerView dotsRecyclerView;
    @BindView(R.id.sizeRecyclerView)
    RecyclerView sizeRecyclerView;
    @BindView(R.id.colorRecyclerView)
    RecyclerView colorRecyclerView;
    public static DotsAdapter dotsAdapter;
    Activity activity;
    ArrayList<String> sliderImages = new ArrayList<>();
    @BindViews({R.id.productName, R.id.price, R.id.actualPrice, R.id.discountPercentage, R.id.quantity, R.id.status})
    List<TextView> textViews;
    public static List<Product> productList = new ArrayList<>();
    ArrayList<String> sizeList = new ArrayList<>();
    ArrayList<String> colorList = new ArrayList<>();
    @BindView(R.id.productDescWebView)
    WebView productDescWebView;
    TextView addToWishList;
    @BindView(R.id.sizeCardView)
    CardView sizeCardView;
    @BindView(R.id.colorCardView)
    CardView colorCardView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.progressBar1)
    ProgressBar progressBar1;
    public static Button addToCart;
    public static String productQuantity;
    @BindView(R.id.noImageAdded)
    ImageView noImageAdded;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_product_detail, container, false);
        ButterKnife.bind(this, view);
        activity = (Activity) view.getContext();
        Bundle bundle = getArguments();
        position = bundle.getInt("position");
        addToCart = (Button) view.findViewById(R.id.addToCart);
        addToWishList = (TextView) view.findViewById(R.id.addToWishList);
        getProductDetails();
        setData();
        checkWishList();
        return view;
    }

    @OnClick({R.id.addToWishListLayout, R.id.addToWishList, R.id.addToCart})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addToWishList:
            case R.id.addToWishListLayout:
                if (!MainActivity.userId.equalsIgnoreCase("")) {
                    addToWishList();
                } else {

                    Config.showLoginCustomAlertDialog(getActivity(),
                            "Login To Continue",
                            "Please login to add product in your wishlist",
                            SweetAlertDialog.WARNING_TYPE);

                }
                break;
            case R.id.addToCart:
                if (!MainActivity.userId.equalsIgnoreCase("")) {
                    if (addToCart.getText().toString().trim().equalsIgnoreCase("Add To Cart")) {

                        if (SizeListAdapter.pos == -1 && sizeCardView.getVisibility() == View.VISIBLE) {
                            Config.showCustomAlertDialog(getActivity(), "Select Size:", "Please select your size to add this item in your cart.", SweetAlertDialog.ERROR_TYPE);
                        } else if (ColorListAdapter.pos == -1 && colorCardView.getVisibility() == View.VISIBLE) {
                            Config.showCustomAlertDialog(getActivity(), "Select Color:", "Please select your color to add this item in your cart.", SweetAlertDialog.ERROR_TYPE);
                        } else {
                            addToCart();
                        }

                    } else if (addToCart.getText().toString().trim().equalsIgnoreCase("Out of Stock")) {
                        Config.showCustomAlertDialog(getActivity(),
                                "Out Of Stock",
                                "This Product is out of stock.",
                                SweetAlertDialog.ERROR_TYPE);

                    } else {
                        ((MainActivity) getActivity()).loadFragment(new MyCartList(), true);
                    }
                } else {

                    Config.showLoginCustomAlertDialog(getActivity(),
                            "Login To Continue",
                            "Please login to add product in your cart",
                            SweetAlertDialog.WARNING_TYPE);
                }
                break;
        }

    }

    private void addToWishList() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().addToWishList(productList.get(position).getProductId(),
                MainActivity.userId,
                new Callback<AddToWishlistResponse>() {
                    @Override
                    public void success(AddToWishlistResponse addToWishlistResponse, Response response) {
                        pDialog.dismiss();


                        Log.d("addToWishListResponse", addToWishlistResponse.getSuccess() + "");
                        if (addToWishlistResponse.getSuccess().equalsIgnoreCase("true")) {
                            Config.showCustomAlertDialog(getActivity(),
                                    addToWishlistResponse.getMessage(),
                                    "",
                                    SweetAlertDialog.SUCCESS_TYPE);
                            checkWishList();
                        } else {
                            final SweetAlertDialog alertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
                            alertDialog.setTitleText(addToWishlistResponse.getMessage());
                            alertDialog.setConfirmText("Verify Now");
                            alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    alertDialog.dismissWithAnimation();
                                    Config.moveTo(getActivity(), AccountVerification.class);

                                }
                            });
                            alertDialog.show();
                            Button btn = (Button) alertDialog.findViewById(R.id.confirm_button);
                            btn.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }

    private void addToCart() {
        String size = "", color = "";
        try {
            if (SizeListAdapter.pos != -1) {
                size = sizeList.get(SizeListAdapter.pos);
            }
        } catch (Exception e) {
        }
        try {

            if (ColorListAdapter.pos != -1) {
                color = colorList.get(ColorListAdapter.pos);
            }
        } catch (Exception e) {
        }
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().addToCart(productList.get(position).getProductId(),
                MainActivity.userId,
                "1",
                size,
                color,
                new Callback<AddToWishlistResponse>() {
                    @Override
                    public void success(AddToWishlistResponse addToWishlistResponse, Response response) {
                        pDialog.dismiss();

                        Log.d("addToCartResponse", addToWishlistResponse.getSuccess() + "");
                        if (addToWishlistResponse.getSuccess().equalsIgnoreCase("true")) {
                            addToCart.setText("Go to Cart");
                            Config.getCartList(getActivity(), true);

                            Config.showCustomAlertDialog(getActivity(),
                                    addToWishlistResponse.getMessage(),
                                    "",
                                    SweetAlertDialog.SUCCESS_TYPE);
                        } else {
                        final SweetAlertDialog alertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
                        alertDialog.setTitleText(addToWishlistResponse.getMessage());
                        alertDialog.setConfirmText("Verify Now");
                        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                alertDialog.dismissWithAnimation();
                                Config.moveTo(getActivity(), AccountVerification.class);

                            }
                        });
                        alertDialog.show();
                        Button btn = (Button) alertDialog.findViewById(R.id.confirm_button);
                        btn.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                    }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }

    private void checkWishList() {
        progressBar.setVisibility(View.VISIBLE);
        addToWishList.setVisibility(View.GONE);
        Api.getClient().checkWishList(productList.get(position).getProductId(),
                MainActivity.userId,
                new Callback<AddToWishlistResponse>() {
                    @Override
                    public void success(AddToWishlistResponse addToWishlistResponse, Response response) {

                        progressBar.setVisibility(View.GONE);
                        addToWishList.setVisibility(View.VISIBLE);
                        Log.d("addToWishListResponse", addToWishlistResponse.getSuccess() + "");
                        if (addToWishlistResponse.getSuccess().equalsIgnoreCase("true")) {
                            addToWishList.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorited_icon, 0, 0, 0);
                        } else
                            addToWishList.setCompoundDrawablesWithIntrinsicBounds(R.drawable.unfavorite_icon, 0, 0, 0);


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressBar.setVisibility(View.GONE);
                        addToWishList.setVisibility(View.VISIBLE);
                        Log.e("error", error.toString());
                    }
                });
    }

    private void getProductDetails() {
        progressBar1.setVisibility(View.VISIBLE);
        addToCart.setVisibility(View.GONE);
        Api.getClient().getProductDetails(productList.get(position).getProductId(),
                new Callback<Product>() {
                    @Override
                    public void success(Product product, Response response) {

                        progressBar1.setVisibility(View.GONE);
                        addToCart.setVisibility(View.VISIBLE);
                        Log.d("productDetailsResponse", product.getProductId() + "" + product.toString());
                        if (Integer.parseInt(product.getQuantity()) < 1) {
                            textViews.get(4).setText("Out of Stock");
                            addToCart.setBackgroundColor(Color.parseColor("#80148cbf"));
                            addToCart.setText("Out of Stock");
                        } else if (Integer.parseInt(product.getQuantity()) > 0 && Integer.parseInt(product.getQuantity()) < 10) {
                            textViews.get(4).setText("Hurry, only " + product.getQuantity() + " left");
                        } else textViews.get(4).setVisibility(View.GONE);
                        textViews.get(5).setText(product.getStatus());

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressBar1.setVisibility(View.GONE);
                        addToCart.setVisibility(View.VISIBLE);
                        Log.e("error", error.toString());
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.search.setVisibility(View.VISIBLE);
        MainActivity.cart.setVisibility(View.VISIBLE);
        MainActivity.title.setText("");
        Config.getCartList(getActivity(), true);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.search.setVisibility(View.GONE);
    }

    public void setSizeListData() {
        FlowLayoutManager flowLayoutManager = new FlowLayoutManager();
        flowLayoutManager.setAutoMeasureEnabled(true);
        sizeRecyclerView.setLayoutManager(flowLayoutManager);
        SizeListAdapter topListAdapter = new SizeListAdapter(getActivity(), sizeList);
        sizeRecyclerView.setAdapter(topListAdapter);
    }

    public void setColorListData() {
        FlowLayoutManager flowLayoutManager = new FlowLayoutManager();
        flowLayoutManager.setAutoMeasureEnabled(true);
        colorRecyclerView.setLayoutManager(flowLayoutManager);
        ColorListAdapter topListAdapter = new ColorListAdapter(getActivity(), colorList);
        colorRecyclerView.setAdapter(topListAdapter);
    }

    private void setData() {
//        productList.addAll(SplashScreen.categoryListResponseData.get(parentPosition).getProducts());
        Log.d("productId", productList.get(position).getProductId());
        sliderImages = new ArrayList<>();
        try {
            sliderImages.addAll(productList.get(position).getImages());
            if (sliderImages.size() > 0) {
                init();
                noImageAdded.setVisibility(View.GONE);
            } else {
                noImageAdded.setVisibility(View.VISIBLE);
            }
        }catch (Exception e)
        {
            noImageAdded.setVisibility(View.VISIBLE);
        }
        setDots(0);
        productQuantity = productList.get(position).getQuantity();
        productDescWebView.loadDataWithBaseURL(null, productList.get(position).getDescription(), "text/html", "utf-8", null);
        textViews.get(0).setText(productList.get(position).getProductName());
        textViews.get(1).setText(MainActivity.currency + " " + productList.get(position).getSellprice());
        try {
            double discountPercentage = Integer.parseInt(productList.get(position).getMrpprice()) - Integer.parseInt(productList.get(position).getSellprice());
            Log.d("percentage", discountPercentage + "");
            discountPercentage = (discountPercentage / Integer.parseInt(productList.get(position).getMrpprice())) * 100;
            if ((int) Math.round(discountPercentage) > 0) {
                textViews.get(3).setText(((int) Math.round(discountPercentage) + "% Off"));
            }
            textViews.get(2).setText(MainActivity.currency + " " + productList.get(position).getMrpprice());
            textViews.get(2).setPaintFlags(textViews.get(2).getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } catch (Exception e) {
        }
        String[] sizeArray = productList.get(position).getSize().split(",");
        String[] colorArray = productList.get(position).getProductColor().split(",");
        sizeList = new ArrayList<>(Arrays.asList(sizeArray));
        colorList = new ArrayList<>(Arrays.asList(colorArray));
        Log.d("sizeList", productList.get(position).getSize() + "");
        if (productList.get(position).getSize().length() > 0) {
            setSizeListData();
        } else {
            sizeCardView.setVisibility(View.GONE);
        }
        if (productList.get(position).getProductColor().length() > 0) {
            setColorListData();
        } else {
            colorCardView.setVisibility(View.GONE);
        }

    }

    private void init() {
        mPager = (ViewPager) view.findViewById(R.id.pager);
        DetailPageSliderPagerAdapter mAdapter = new DetailPageSliderPagerAdapter(getChildFragmentManager(), sliderImages);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mPager.getChildCount() * MyPagerAdapter.LOOPS_COUNT / 2, false); // set current item in the adapter to middle
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                position = position % sliderImages.size();
                Log.d("onPageSelected", position + "");
                setDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setDots(int selectedPos) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        dotsRecyclerView.setLayoutManager(linearLayoutManager);
        dotsAdapter = new DotsAdapter(activity, sliderImages.size(), selectedPos);
        dotsRecyclerView.setAdapter(dotsAdapter);

    }
}


package co.sandyedemo.ecomdemo.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Activities.AccountVerification;
import co.sandyedemo.ecomdemo.Common;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Activities.Login;
import co.sandyedemo.ecomdemo.MVP.SignUpResponse;
import co.sandyedemo.ecomdemo.MVP.UserProfileResponse;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.Activities.SignUp;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyProfile extends Fragment {

    View view;
    @BindViews({R.id.fullNameEdt, R.id.mobEditText, R.id.cityEditText, R.id.areaEditText, R.id.buildingEditText, R.id.pincodeEditText, R.id.stateEditText, R.id.landmarkEditText,})
    List<EditText> editTexts;
    UserProfileResponse userProfileResponseData;
    @BindView(R.id.submitBtn)
    Button submitBtn;
    @BindViews({R.id.male, R.id.female})
    List<CircleImageView> circleImageViews;
    String gender = "";
    @BindView(R.id.profileLayout)
    LinearLayout profileLayout;
    @BindView(R.id.loginLayout)
    LinearLayout loginLayout;
    @BindView(R.id.logout)
    Button logout;

    @BindView(R.id.verifyEmailLayout)
    LinearLayout verifyEmailLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText("My Profile");
        if (!MainActivity.userId.equalsIgnoreCase("")) {
            getUserProfileData();
        } else {
            profileLayout.setVisibility(View.INVISIBLE);
            loginLayout.setVisibility(View.VISIBLE);
        }
        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);

            }
        });
        return view;
    }
    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void setUserProfileData() {
//        logout.setText("Logout ( "+userProfileResponseData.get);
        editTexts.get(0).setText(userProfileResponseData.getName());
        editTexts.get(1).setText(userProfileResponseData.getMobile());
        editTexts.get(2).setText(userProfileResponseData.getCity());
        editTexts.get(3).setText(userProfileResponseData.getLocality());
        editTexts.get(4).setText(userProfileResponseData.getFlat());
        editTexts.get(5).setText(userProfileResponseData.getPincode());
        editTexts.get(6).setText(userProfileResponseData.getState());
        editTexts.get(7).setText(userProfileResponseData.getLandmark());
        try {
            if (userProfileResponseData.getGender().equalsIgnoreCase("Female")) {
                circleImageViews.get(0).setImageResource(R.drawable.male_unselect);
                circleImageViews.get(1).setImageResource(R.drawable.female_select);
                gender = "female";
            } else if (userProfileResponseData.getGender().equalsIgnoreCase("male")) {

                circleImageViews.get(0).setImageResource(R.drawable.male_select);
                circleImageViews.get(1).setImageResource(R.drawable.female_unselect);
                gender = "male";
            }
        } catch (Exception e) {

        }
    }

    @OnClick({R.id.male, R.id.female, R.id.submitBtn, R.id.logout, R.id.loginNow, R.id.txtSignUp, R.id.verfiyNow})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.male:
                circleImageViews.get(0).setImageResource(R.drawable.male_select);
                circleImageViews.get(1).setImageResource(R.drawable.female_unselect);
                gender = "male";
                break;
            case R.id.female:
                circleImageViews.get(0).setImageResource(R.drawable.male_unselect);
                circleImageViews.get(1).setImageResource(R.drawable.female_select);
                gender = "female";
                break;
            case R.id.submitBtn:
                if (gender.equalsIgnoreCase("")) {
                    Config.showCustomAlertDialog(getActivity(), "Please choose your gender to update your profile", "",
                            SweetAlertDialog.ERROR_TYPE);
                } else if (validate(editTexts.get(0))
                        && validate(editTexts.get(1))
                        && validate(editTexts.get(2))
                        && validate(editTexts.get(3))
                        && validate(editTexts.get(4))
                        && validate(editTexts.get(5))
                        && validate(editTexts.get(6)))
                    updateProfile();
                break;
            case R.id.logout:
                logout();
                break;
            case R.id.loginNow:
                Config.moveTo(getActivity(), Login.class);
                break;
            case R.id.txtSignUp:
                Config.moveTo(getActivity(), SignUp.class);
                break;

            case R.id.verfiyNow:
                Config.moveTo(getActivity(), AccountVerification.class);
                break;
        }
    }

    private void logout() {

        final SweetAlertDialog alertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.WARNING_TYPE);
        alertDialog.setTitleText("Are you sure you want to logout?");
        alertDialog.setCancelText("Cancel");
        alertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                alertDialog.dismissWithAnimation();
            }
        });
        alertDialog.show();
        Button btn = (Button) alertDialog.findViewById(R.id.confirm_button);
        btn.setBackground(getResources().getDrawable(R.drawable.custom_dialog_button));
        btn.setText("Logout");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Common.saveUserData(getActivity(), "email", "");
                Common.saveUserData(getActivity(), "userId", "");
                Config.moveTo(getActivity(), Login.class);
                getActivity().finishAffinity();

            }
        });
    }

    private boolean validate(EditText editText) {
        if (editText.getText().toString().trim().length() > 0) {
            return true;
        }
        editText.setError("Please Fill This");
        editText.requestFocus();
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Config.getCartList(getActivity(), true);
    }


    public void getUserProfileData() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().getUserProfile(
                MainActivity.userId, new Callback<UserProfileResponse>() {
                    @Override
                    public void success(UserProfileResponse userProfileResponse, Response response) {
                        userProfileResponseData = userProfileResponse;
                        pDialog.dismiss();
                        if (userProfileResponse.getSuccess().equalsIgnoreCase("false")) {
                            profileLayout.setVisibility(View.INVISIBLE);
                            verifyEmailLayout.setVisibility(View.VISIBLE);
                        } else
                            setUserProfileData();


                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                    }
                });
    }

    public void updateProfile() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().updateProfile(
                MainActivity.userId,
                editTexts.get(0).getText().toString().trim(),
                editTexts.get(2).getText().toString().trim(),
                editTexts.get(6).getText().toString().trim(),
                editTexts.get(5).getText().toString().trim(),
                editTexts.get(3).getText().toString().trim(),
                editTexts.get(4).getText().toString().trim(),
                gender,
                editTexts.get(1).getText().toString().trim(),
                editTexts.get(7).getText().toString().trim(),
                new Callback<SignUpResponse>() {
                    @Override
                    public void success(SignUpResponse signUpResponse, Response response) {
                        pDialog.dismiss();
                        if (signUpResponse.getSuccess().equalsIgnoreCase("true")) {
                            Config.showCustomAlertDialog(getActivity(),
                                    "Profile Status",
                                    "Profile updated",
                                    SweetAlertDialog.SUCCESS_TYPE);
                        } else {
                            Toast.makeText(getActivity(), "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                    }
                });
    }

}


package co.sandyedemo.ecomdemo.Fragments;

import android.app.Activity;

public class OrderConfirmation extends Activity {
}


package co.sandyedemo.ecomdemo.Fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Adapters.DotsAdapter;
import co.sandyedemo.ecomdemo.Adapters.HomeCategoryAdapter;
import co.sandyedemo.ecomdemo.Adapters.HomeCategoryProductsAdapter;
import co.sandyedemo.ecomdemo.Adapters.MyPagerAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.DetectConnection;
import co.sandyedemo.ecomdemo.MVP.CategoryListResponse;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.MVP.SliderListResponse;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.Activities.SplashScreen;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Home extends Fragment {

    View view;

    private static ViewPager mPager;
    @BindView(R.id.categoryRecyclerView)
    RecyclerView categoryRecyclerView;
    @BindView(R.id.categoryProductRecyclerView)
    RecyclerView categoryProductRecyclerView;
    public static RecyclerView dotsRecyclerView;
    public static DotsAdapter dotsAdapter;
    public static Activity activity;
    public static NestedScrollView nestedScrollView;
    private String TAG = "testing";
    @BindString(R.string.app_name)
    String app_name;
    @BindView(R.id.sliderLayout)
    LinearLayout sliderLayout;
    public static SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText(app_name);
        activity = (Activity) view.getContext();
        dotsRecyclerView = (RecyclerView) view.findViewById(R.id.dotsRecyclerView);
        nestedScrollView = (NestedScrollView) view.findViewById(R.id.nestedScrollView);
        setCategoryData();
        setCategoryProductsData();
        try {
            if (SplashScreen.sliderListResponsesData.size() > 0) {
                setDots(0);
                init();
            }else
                sliderLayout.setVisibility(View.GONE);

        } catch (Exception e) {
            sliderLayout.setVisibility(View.GONE);
        }
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.simpleSwipeRefreshLayout);

        // implement setOnRefreshListener event on SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DetectConnection.checkInternetConnection(getActivity())) {
                    MainActivity.searchLayout.setVisibility(View.GONE);
                    Config.getCartList(getActivity(), true);
                    getSliderList();
                } else {
                    Toast.makeText(getActivity(), "Internet Not Available", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

         return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("onStart", "called");
        MainActivity.cart.setVisibility(View.VISIBLE);
        Config.getCartList(getActivity(), true);
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_UNLOCKED);
        MainActivity.drawerLayout.closeDrawers();
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (MainActivity.searchLayout.getVisibility() == View.VISIBLE) {
                    if (scrollY > oldScrollY) {
                        Log.i(TAG, "Scroll DOWN");
                        hideToolbar();
                    }
                    if (scrollY < oldScrollY) {
                        Log.i(TAG, "Scroll UP");
                        showToolbar();
                    }

                    if (scrollY == 0) {
                        Log.i(TAG, "TOP SCROLL");
                        showToolbar();

                    }
                    if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                        Log.i(TAG, "BOTTOM SCROLL");
                        hideToolbar();
                    }
                } else
                    nestedScrollView.setNestedScrollingEnabled(false);
            }
        });

    }

    public void showToolbar() {
        MainActivity.toolbarContainer.clearAnimation();
        MainActivity.toolbarContainer
                .animate()
                .translationY(0)
                .start();

    }

    private void hideToolbar() {
        MainActivity.toolbarContainer.clearAnimation();
        MainActivity.toolbarContainer
                .animate()
                .translationY(-MainActivity.toolbar.getBottom())
                .alpha(1.0f)
                .start();
    }

    private void setCategoryData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4);
        categoryRecyclerView.setLayoutManager(gridLayoutManager);
        Log.d("categorySize", SplashScreen.categoryListResponseData.size() + "");
        if (SplashScreen.categoryListResponseData.size() < 4) {
            HomeCategoryAdapter homeCategoryAdapter = new HomeCategoryAdapter(getActivity(), SplashScreen.categoryListResponseData, SplashScreen.categoryListResponseData.size());
            categoryRecyclerView.setAdapter(homeCategoryAdapter);
        } else {
            HomeCategoryAdapter homeCategoryAdapter = new HomeCategoryAdapter(getActivity(), SplashScreen.categoryListResponseData, 4);
            categoryRecyclerView.setAdapter(homeCategoryAdapter);

        }
    }

    public void setDots(int selectedPos) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        dotsRecyclerView.setLayoutManager(linearLayoutManager);
        dotsAdapter = new DotsAdapter(activity, SplashScreen.sliderListResponsesData.size(), selectedPos);
        dotsRecyclerView.setAdapter(dotsAdapter);

    }

    private void setCategoryProductsData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        categoryProductRecyclerView.setLayoutManager(gridLayoutManager);
        HomeCategoryProductsAdapter homeCategoryAdapter = new HomeCategoryProductsAdapter(getActivity(), SplashScreen.categoryListResponseData);
        categoryProductRecyclerView.setAdapter(homeCategoryAdapter);

    }

    private void init() {
        mPager = (ViewPager) view.findViewById(R.id.pager);
        MyPagerAdapter mAdapter = new MyPagerAdapter(getChildFragmentManager(), SplashScreen.sliderListResponsesData);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(mPager.getChildCount() * MyPagerAdapter.LOOPS_COUNT / 2, false); // set current item in the adapter to middle
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                position = position % SplashScreen.sliderListResponsesData.size();
                Log.d("onPageSelected", position + "");
                setDots(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    public void getCategoryList() {
        // getting category list news data
        Api.getClient().getCategoryList(new Callback<List<CategoryListResponse>>() {
            @Override
            public void success(List<CategoryListResponse> categoryListResponses, Response response) {
                SplashScreen.categoryListResponseData.clear();
                SplashScreen.categoryListResponseData.addAll(categoryListResponses);
                setCategoryData();
                setCategoryProductsData();
                swipeRefreshLayout.setRefreshing(false);
                MainActivity.searchLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
                swipeRefreshLayout.setRefreshing(false);
                MainActivity.searchLayout.setVisibility(View.VISIBLE);

            }
        });
    }

    public void getSliderList() {
        // getting slider list data
        Api.getClient().getSliderList(new Callback<List<SliderListResponse>>() {
            @Override
            public void success(List<SliderListResponse> sliderListResponses, Response response) {

                try {
                    SplashScreen.sliderListResponsesData = new ArrayList<>();
                    SplashScreen.sliderListResponsesData.addAll(sliderListResponses);
                    sliderLayout.setVisibility(View.VISIBLE);
                    setDots(0);
                    init();
                } catch (Exception e) {
                    sliderLayout.setVisibility(View.GONE);
                }
                getAllProducts();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
                swipeRefreshLayout.setRefreshing(false);
                MainActivity.searchLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void getAllProducts() {

        // getting news list data
        Api.getClient().getAllProducts(new Callback<List<Product>>() {
            @Override
            public void success(List<Product> allProducts, Response response) {
                Log.d("allProductsDataHome", allProducts.get(0).getProductName());
                SplashScreen.allProductsData.clear();
                SplashScreen.allProductsData.addAll(allProducts);
                getCategoryList();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
                swipeRefreshLayout.setRefreshing(false);
                MainActivity.searchLayout.setVisibility(View.VISIBLE);
            }
        });
    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Adapters.CategoryListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Activities.SplashScreen;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryList extends Fragment {

    View view;
    @BindView(R.id.categoryRecyclerView)
    RecyclerView categoryRecyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_category_list, container, false);
        ButterKnife.bind(this, view);
        setCategoryData();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.search.setVisibility(View.VISIBLE);
        MainActivity.title.setText("Categories");
        Config.getCartList(getActivity(),true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.search.setVisibility(View.GONE);
    }

    private void setCategoryData() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        categoryRecyclerView.setLayoutManager(gridLayoutManager);
        CategoryListAdapter categoryListAdapter = new CategoryListAdapter(getActivity(), SplashScreen.categoryListResponseData);
        categoryRecyclerView.setAdapter(categoryListAdapter);
    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Adapters.DetailOrderProductListAdapter;
import co.sandyedemo.ecomdemo.Adapters.WishListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.Ordere;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class MyOrderedProductsDetailPage extends Fragment {

    View view;
    @BindView(R.id.orderedProductsRecyclerView)
    RecyclerView orderedProductsRecyclerView;
    public static List<Ordere> orderes;
    @BindViews({R.id.orderNo, R.id.date, R.id.totalAmount, R.id.paymentMode, R.id.shippingAddress, R.id.orderStatus})
    List<TextView> textViews;
    public static int pos;
    public static String currency;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_ordered_products_detail, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText("");
        setData();
        setProductsData();

        return view;
    }

    private void setData() {
        if (orderes.get(pos).getOrdredproduct().get(0).getCurrency().equalsIgnoreCase("USD"))
            currency = "$";
        else
            currency = "₹";
        textViews.get(0).setText(orderes.get(pos).getOrderid());
        textViews.get(1).setText(orderes.get(pos).getDate());
        textViews.get(3).setText(orderes.get(pos).getPaymentmode());
        textViews.get(4).setText(orderes.get(pos).getAddress());
        textViews.get(5).setText(orderes.get(pos).getOrdredproduct().get(0).getOrderstatus());
        textViews.get(2).setText(currency + " " + orderes.get(pos).getTotal());
    }


    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Config.getCartList(getActivity(), true);
    }

    private void setProductsData() {
        WishListAdapter wishListAdapter;
        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        orderedProductsRecyclerView.setLayoutManager(gridLayoutManager);
        DetailOrderProductListAdapter myOrdersAdapter = new DetailOrderProductListAdapter(getActivity(), orderes.get(pos).getOrdredproduct());
        orderedProductsRecyclerView.setAdapter(myOrdersAdapter);

    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import co.sandyedemo.ecomdemo.Activities.AccountVerification;
import co.sandyedemo.ecomdemo.Adapters.MyOrdersAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Activities.Login;
import co.sandyedemo.ecomdemo.MVP.MyOrdersResponse;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.Activities.SignUp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyOrders extends Fragment {

    View view;
    @BindView(R.id.myOrdersRecyclerView)
    RecyclerView myOrdersRecyclerView;
    public static MyOrdersResponse myOrdersResponseData;

    @BindView(R.id.emptyOrdersLayout)
    LinearLayout emptyOrdersLayout;
    @BindView(R.id.loginLayout)
    LinearLayout loginLayout;
    @BindView(R.id.continueShopping)
    Button continueShopping;

    @BindView(R.id.verifyEmailLayout)
    LinearLayout verifyEmailLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_orders, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText("My Orders");
        if (!MainActivity.userId.equalsIgnoreCase("")) {
            getMyOrders();
        } else {
            loginLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @OnClick({R.id.continueShopping, R.id.loginNow, R.id.txtSignUp, R.id.verfiyNow})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.continueShopping:
                Config.moveTo(getActivity(), MainActivity.class);
                getActivity().finish();
                break;
            case R.id.loginNow:
                Config.moveTo(getActivity(), Login.class);
                break;
            case R.id.txtSignUp:
                Config.moveTo(getActivity(), SignUp.class);
                break;

            case R.id.verfiyNow:
                Config.moveTo(getActivity(), AccountVerification.class);
                break;
        }
    }
    public void getMyOrders() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().getMyOrders(MainActivity.userId, new Callback<MyOrdersResponse>() {
            @Override
            public void success(MyOrdersResponse myOrdersResponse, Response response) {
                pDialog.dismiss();
                if (myOrdersResponse.getSuccess().equalsIgnoreCase("true")) {
                    try {
                        Log.d("size", myOrdersResponse.getOrderes().size() + "");
                        myOrdersResponseData = myOrdersResponse;
                        setProductsData();
                    } catch (Exception e) {
                        emptyOrdersLayout.setVisibility(View.VISIBLE);
                    }
                }else {
                    verifyEmailLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                emptyOrdersLayout.setVisibility(View.VISIBLE);
                pDialog.dismiss();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Config.getCartList(getActivity(), true);
    }

    private void setProductsData() {
        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        myOrdersRecyclerView.setLayoutManager(gridLayoutManager);
        MyOrdersAdapter myOrdersAdapter = new MyOrdersAdapter(getActivity(), myOrdersResponseData.getOrderes());
        myOrdersRecyclerView.setAdapter(myOrdersAdapter);

    }
}



package co.sandyedemo.ecomdemo.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.UserProfileResponse;
//import co.sandyedemo.ecomdemo.PaymentIntegrationMethods.PayPalActivityPayment;
//import co.sandyedemo.ecomdemo.PaymentIntegrationMethods.StripePaymentIntegration;
import co.sandyedemo.ecomdemo.PaymentIntegrationMethods.RazorPayIntegration;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChoosePaymentMethod extends Fragment {

    View view;
    @BindView(R.id.addNewAddressLayout)
    LinearLayout addNewAddressLayout;
    @BindView(R.id.addressCheckBox)
    CheckBox addressCheckBox;
    @BindView(R.id.addNewAddress)
    TextView addNewAddress;
    @BindView(R.id.fillAddress)
    TextView fillAddress;
    @BindView(R.id.paymentMethodsGroup)
    RadioGroup paymentMethodsGroup;
    @BindView(R.id.makePayment)
    Button makePayment;
    String paymentMethod;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.choosePaymentLayout)
    LinearLayout choosePaymentLayout;
    @BindViews({R.id.fullNameEdt, R.id.mobEditText, R.id.cityEditText, R.id.areaEditText, R.id.buildingEditText, R.id.pincodeEditText, R.id.stateEditText, R.id.landmarkEditText,})
    List<EditText> editTexts;
    public static String address, mobileNo,userEmail;
    Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int layout = R.layout.fragment_choose_payment_method;
        view = inflater.inflate(layout, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText("Choose Payment Method");
        MainActivity.cart.setVisibility(View.GONE);
        MainActivity.cartCount.setVisibility(View.GONE);
        getUserProfileData();
        addressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    addNewAddressLayout.setVisibility(View.GONE);
                    addNewAddress.setText("Add New Address");

                }
            }
        });
        choosePaymentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);

            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.cart.setVisibility(View.VISIBLE);
        MainActivity.cartCount.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.addNewAddress, R.id.makePayment, R.id.fillAddress})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addNewAddress:
                addNewAddressLayout.setVisibility(View.VISIBLE);
                addressCheckBox.setChecked(false);
                addNewAddress.setText("Use This Address");
                break;
            case R.id.makePayment:
                if (!addressCheckBox.isChecked()) {
                    if (addNewAddressLayout.getVisibility() == View.VISIBLE) {
                        if (validate(editTexts.get(0))
                                && validate(editTexts.get(1))
                                && validate(editTexts.get(2))
                                && validate(editTexts.get(3))
                                && validate(editTexts.get(4))
                                && validate(editTexts.get(5))
                                && validate(editTexts.get(6))) {
                            String s = "";
                            if (editTexts.get(6).getText().toString().trim().length() > 0) {
                                s = ", " + editTexts.get(6).getText().toString().trim();
                            }
                            address = editTexts.get(0).getText().toString().trim()
                                    + ", "
                                    + editTexts.get(4).getText().toString().trim()
                                    + s
                                    + ", " + editTexts.get(3).getText().toString().trim()
                                    + ", " + editTexts.get(2).getText().toString().trim()
                                    + ", " + editTexts.get(6).getText().toString().trim()
                                    + ", " + editTexts.get(5).getText().toString().trim()
                                    + "\n" + editTexts.get(1).getText().toString().trim();
                            mobileNo = editTexts.get(1).getText().toString().trim();
                            moveNext();
                        }
                    } else {
                        Config.showCustomAlertDialog(getActivity(),
                                "Please choose your saved address or add new to make payment",
                                "",
                                SweetAlertDialog.ERROR_TYPE);
                    }
                } else {
                    moveNext();
                }

                break;
            case R.id.fillAddress:
                ((MainActivity) getActivity()).loadFragment(new MyProfile(), true);
                break;
        }

    }
    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private void moveNext() {
        switch (paymentMethodsGroup.getCheckedRadioButtonId()) {
            //case R.id.paypal:
              //  paymentMethod = "paypal";
                //intent = new Intent(getActivity(), PayPalActivityPayment.class);
                //startActivity(intent);
                //break;
            case R.id.cod:
                paymentMethod = "cod";
                Config.addOrder(getActivity(),
                        "COD",
                        "COD");
                break;
            //case R.id.stripe:
              //  paymentMethod = "stripe";
                //intent = new Intent(getActivity(), StripePaymentIntegration.class);
                //startActivity(intent);
                //break;

            case R.id.razorPay:
                paymentMethod = "razorPay";
                intent = new Intent(getActivity(), RazorPayIntegration.class);
                startActivity(intent);
                break;
            default:
                paymentMethod = "";
                Config.showCustomAlertDialog(getActivity(),
                        "Payment Method",
                        "Select your payment method to make payment",
                        SweetAlertDialog.NORMAL_TYPE);
                break;


        }

        Log.d("paymentMethod", paymentMethod);
    }

    private boolean validate(EditText editText) {
        if (editText.getText().toString().trim().length() > 0) {
            return true;
        }
        editText.setError("Please Fill This");
        editText.requestFocus();
        return false;
    }

    public void getUserProfileData() {
        progressBar.setVisibility(View.VISIBLE);
        Api.getClient().getUserProfile(
                MainActivity.userId, new Callback<UserProfileResponse>() {
                    @Override
                    public void success(UserProfileResponse userProfileResponse, Response response) {
                        progressBar.setVisibility(View.GONE);
                        userEmail=userProfileResponse.getEmail();
                        String s = "";
                        if (!userProfileResponse.getLandmark().equalsIgnoreCase("")) {
                            s = ", " + userProfileResponse.getLandmark();
                        }
                        if (userProfileResponse.getFlat().equalsIgnoreCase("")) {
                            addressCheckBox.setChecked(false);
                            addressCheckBox.setVisibility(View.GONE);
                            fillAddress.setVisibility(View.VISIBLE);
                        } else {
                            address = userProfileResponse.getName()
                                    + ", "
                                    + userProfileResponse.getFlat()
                                    + s
                                    + ", " + userProfileResponse.getLocality()
                                    + ", " + userProfileResponse.getCity()
                                    + ", " + userProfileResponse.getState()
                                    + ", " + userProfileResponse.getPincode()
                                    + "\n" + userProfileResponse.getMobile();
                            addressCheckBox.setText(address);
                           mobileNo = userProfileResponse.getMobile();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressBar.setVisibility(View.GONE);

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
}



package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.TermsResponse;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class TermsAndConditions extends Fragment {

    View view;
    @BindView(R.id.faq)
    WebView faq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_faq, container, false);
        ButterKnife.bind(this, view);
        getTerms();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.title.setText("Terms & Conditions");
        Config.getCartList(getActivity(), true);
    }

    public void getTerms() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().getTerms(new Callback<TermsResponse>() {
            @Override
            public void success(TermsResponse termsResponse, Response response) {
                pDialog.dismiss();
                faq.loadDataWithBaseURL(null, termsResponse.getTerms(), "text/html", "utf-8", null);

            }

            @Override
            public void failure(RetrofitError error) {
                pDialog.dismiss();

            }
        });
    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Adapters.SearchProductListAdapter;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Activities.SplashScreen;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchProducts extends Fragment {

    @BindView(R.id.searchProductsRecyclerView)
    RecyclerView searchProductsRecyclerView;
    @BindView(R.id.searchEditText)
    EditText searchEditText;
    List<Product> productList;

    @BindView(R.id.defaultMessage)
    TextView defaultMessage;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_search_products, container, false);
        ButterKnife.bind(this, view);
        defaultMessage.setText("Search Any Product");
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("text", editable.toString());
                searchProducts(editable.toString());
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.title.setText("Search");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void searchProducts(String s) {
        productList = new ArrayList<>();
        if (s.length() > 0) {
            for (int i = 0; i < SplashScreen.allProductsData.size(); i++)
                if (SplashScreen.allProductsData.get(i).getProductName().toLowerCase().contains(s.toLowerCase().trim())) {
                    productList.add(SplashScreen.allProductsData.get(i));
                }
            if (productList.size() < 1) {
                defaultMessage.setText("Record Not Found");
                defaultMessage.setVisibility(View.VISIBLE);
            } else {
                defaultMessage.setVisibility(View.GONE);
            }
            Log.d("size", productList.size() + "" + SplashScreen.allProductsData.size());
        } else {
            productList = new ArrayList<>();
            defaultMessage.setText("Search Any Product");
            defaultMessage.setVisibility(View.VISIBLE);
        }
        setProductsData();


    }

    private void setProductsData() {
        SearchProductListAdapter productListAdapter;
        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        searchProductsRecyclerView.setLayoutManager(gridLayoutManager);
        productListAdapter = new SearchProductListAdapter(getActivity(), productList);
        searchProductsRecyclerView.setAdapter(productListAdapter);

    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import co.sandyedemo.ecomdemo.Activities.AccountVerification;
import co.sandyedemo.ecomdemo.Adapters.WishListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Activities.Login;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.MVP.WishlistResponse;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.Activities.SignUp;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyWishList extends Fragment {

    View view;

    @BindView(R.id.categoryRecyclerView)
    RecyclerView productsRecyclerView;
    public static int categoryPosition = 0;
    public static List<Product> productsData = new ArrayList<>();
    @BindView(R.id.emptyWishlistLayout)
    LinearLayout emptyWishlistLayout;
    @BindView(R.id.loginLayout)
    LinearLayout loginLayout;
    @BindView(R.id.verifyEmailLayout)
    LinearLayout verifyEmailLayout;
    @BindView(R.id.continueShopping)
    Button continueShopping;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wish_list, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText("My Wish List");
        if (!MainActivity.userId.equalsIgnoreCase("")) {
            getWishList();
        } else {
            loginLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @OnClick({R.id.continueShopping, R.id.loginNow, R.id.txtSignUp, R.id.verfiyNow})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.continueShopping:
                Config.moveTo(getActivity(), MainActivity.class);
                getActivity().finish();
                break;
            case R.id.loginNow:
                Config.moveTo(getActivity(), Login.class);
                break;
            case R.id.txtSignUp:
                Config.moveTo(getActivity(), SignUp.class);
                break;
            case R.id.verfiyNow:
                Config.moveTo(getActivity(), AccountVerification.class);
                break;
        }
    }

    public void getWishList() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().getWishList(MainActivity.userId, new Callback<WishlistResponse>() {
            @Override
            public void success(WishlistResponse wishlistResponse, Response response) {
                pDialog.dismiss();
                try {
                    if (wishlistResponse.getSuccess().equalsIgnoreCase("true")) {

                        Log.d("cartId", wishlistResponse.getProducts().size() + "");
                        productsData.clear();
                        productsData = wishlistResponse.getProducts();
                        setProductsData();

                    } else {
                        verifyEmailLayout.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    Log.d("wishList", "Not available");
                    emptyWishlistLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                emptyWishlistLayout.setVisibility(View.VISIBLE);
                pDialog.dismiss();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Config.getCartList(getActivity(), true);
    }

    private void setProductsData() {
        WishListAdapter wishListAdapter;
        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        productsRecyclerView.setLayoutManager(gridLayoutManager);
        wishListAdapter = new WishListAdapter(getActivity(), productsData);
        productsRecyclerView.setAdapter(wishListAdapter);

    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import co.sandyedemo.ecomdemo.Adapters.ProductListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Activities.SplashScreen;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductsList extends Fragment {

    View view;

    @BindView(R.id.categoryRecyclerView)
    RecyclerView productsRecyclerView;
    public static int categoryPosition;
    @BindView(R.id.noProductAddedLayout)
    LinearLayout noProductAddedLayout;
    @BindView(R.id.contShopping)
    Button contShopping;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_category_list, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText(SplashScreen.categoryListResponseData.get(categoryPosition).getCategory_name());
        setProductsData();
        contShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).removeCurrentFragmentAndMoveBack();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.search.setVisibility(View.VISIBLE);
        Config.getCartList(getActivity(), true);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.search.setVisibility(View.GONE);
    }

    private void setProductsData() {
        if (SplashScreen.categoryListResponseData.get(categoryPosition).getProducts().size() > 0) {
            ProductListAdapter productListAdapter;
            GridLayoutManager gridLayoutManager;
            gridLayoutManager = new GridLayoutManager(getActivity(), 1);
            productsRecyclerView.setLayoutManager(gridLayoutManager);
            productListAdapter = new ProductListAdapter(getActivity(), SplashScreen.categoryListResponseData.get(categoryPosition).getProducts(), categoryPosition);
            productsRecyclerView.setAdapter(productListAdapter);
        } else {
            noProductAddedLayout.setVisibility(View.VISIBLE);
        }

    }
}


package co.sandyedemo.ecomdemo.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.FAQResponse;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class FAQ extends Fragment {

    View view;
    @BindView(R.id.faq)
    WebView faq;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_faq, container, false);
        ButterKnife.bind(this, view);
        getFAQ();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.title.setText("");
        Config.getCartList(getActivity(), true);
    }

    public void getFAQ() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().getFAQ(new Callback<FAQResponse>() {
            @Override
            public void success(FAQResponse faqResponse, Response response) {
                pDialog.dismiss();
                MainActivity.title.setText(faqResponse.getTitle());
                faq.loadDataWithBaseURL(null, faqResponse.getDescription(), "text/html", "utf-8", null);

            }

            @Override
            public void failure(RetrofitError error) {
                pDialog.dismiss();

            }
        });
    }
}


package co.sandyedemo.ecomdemo.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;

import butterknife.ButterKnife;

public class EditProfile extends Fragment {

    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        ButterKnife.bind(this, view);
        MainActivity.title.setText("Edit Profile");
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
}


package co.sandyedemo.ecomdemo.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Activities.SplashScreen;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PageFragment extends Fragment {
    public static PageFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt("position", position);
        PageFragment fragment = new PageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final int position = getArguments().getInt("position");
        int layout = R.layout.fragment_page_one;
        View root = inflater.inflate(layout, container, false);
        root.setTag(position);
        ImageView image_one = (ImageView) root.findViewById(R.id.image_one);
        image_one.setScaleType(ImageView.ScaleType.FIT_XY);
        try {
            Picasso.with(getActivity())
                    .load(SplashScreen.sliderListResponsesData.get(position).getImage())
                    .placeholder(R.drawable.defaultimage)
                    .into(image_one);
        }catch (Exception e)
        {

        }
        Log.d("positionOfSlider", position + "");
        image_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Home.swipeRefreshLayout.isRefreshing()) {
                    Log.d("clickedPosition", position + "");
                    List<Product> list = new ArrayList<>();
                    list.add(SplashScreen.sliderListResponsesData.get(position).getProductsdetails());
                    ProductDetail.productList.clear();
                    ProductDetail.productList.addAll(list);
                    ProductDetail productDetail = new ProductDetail();
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", 0);
                    productDetail.setArguments(bundle);
                    ((MainActivity) getActivity()).loadFragment(productDetail, true);
                }
            }
        });
        return root;
    }

}


package co.sandyedemo.ecomdemo.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import co.sandyedemo.ecomdemo.Activities.AccountVerification;
import co.sandyedemo.ecomdemo.Adapters.CartListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Activities.Login;
import co.sandyedemo.ecomdemo.MVP.CartistResponse;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.Activities.SignUp;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyCartList extends Fragment {

    View view;

    @BindView(R.id.categoryRecyclerView)
    RecyclerView productsRecyclerView;
    public static int categoryPosition = 0;
    public static List<Product> productsData = new ArrayList<>();
    public static CartistResponse cartistResponseData;
    @BindView(R.id.proceedToPayment)
    Button proceedToPayment;
    public static Context context;
    @BindView(R.id.emptyCartLayout)
    LinearLayout emptyCartLayout;
    @BindView(R.id.loginLayout)
    LinearLayout loginLayout;
    @BindView(R.id.continueShopping)
    Button continueShopping;

    @BindView(R.id.verifyEmailLayout)
    LinearLayout verifyEmailLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_cart_list, container, false);
        ButterKnife.bind(this, view);
        context = getActivity();
        MainActivity.title.setText("My Cart");
        proceedToPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).loadFragment(new ChoosePaymentMethod(), true);

            }
        });
        if (!MainActivity.userId.equalsIgnoreCase("")) {
            getCartList();
        } else {
            proceedToPayment.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @OnClick({R.id.continueShopping, R.id.loginNow, R.id.txtSignUp, R.id.verfiyNow})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.continueShopping:
                Config.moveTo(getActivity(), MainActivity.class);
                getActivity().finish();
                break;
            case R.id.loginNow:
                Config.moveTo(getActivity(), Login.class);
                break;
            case R.id.txtSignUp:
                Config.moveTo(getActivity(), SignUp.class);
                break;

            case R.id.verfiyNow:
                Config.moveTo(getActivity(), AccountVerification.class);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MainActivity.cart.setVisibility(View.VISIBLE);
    }

    public void getCartList() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().getCartList(MainActivity.userId, new Callback<CartistResponse>() {
            @Override
            public void success(CartistResponse cartistResponse, Response response) {

                cartistResponseData = cartistResponse;
                pDialog.dismiss();
                productsData = new ArrayList<>();
                productsData = cartistResponse.getProducts();
                if (cartistResponse.getSuccess().equalsIgnoreCase("false")) {
                    verifyEmailLayout.setVisibility(View.VISIBLE);
                    proceedToPayment.setVisibility(View.GONE);
                } else {
                    try {
                        Log.d("cartId", cartistResponse.getCartid());
                        cartistResponse.getProducts().size();
                        setProductsData();
                    } catch (Exception e) {
                        proceedToPayment.setVisibility(View.GONE);
                        emptyCartLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("errorInCartList", error.toString());

                pDialog.dismiss();

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).lockUnlockDrawer(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        MainActivity.cart.setVisibility(View.GONE);
        Config.getCartList(getActivity(), false);
    }

    private void setProductsData() {
        CartListAdapter wishListAdapter;
        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(getActivity(), 1);
        productsRecyclerView.setLayoutManager(gridLayoutManager);
        wishListAdapter = new CartListAdapter(getActivity(), productsData);
        productsRecyclerView.setAdapter(wishListAdapter);

    }
}


package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryListResponse {

    private List<Product> products = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private String cat_id;
    private String category_name;
    private String category_image;
    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_image() {
        return category_image;
    }

    public void setCategory_image(String category_image) {
        this.category_image = category_image;
    }

}


package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class UserProfileResponse {

    private String name;
    private String gender;
    private String mobile;
    private String city;
    private String locality;
    private String flat;
    private String pincode;
    private String state;
    private String landmark;
    private String success;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String email;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}


package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {

    private String success;
    private String productId;
    private String iteam_id;
    private String plimit;
    private String orderstatus;
    private String productName;
    private String mrp;
    private String sellprice;
    private String size;
    private String status;
    private String color;
    private String currency;
    private String quantity;
    private String stock;
    private String description;
    private List<String> images = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();



    public String getOrderstatus() {
        return orderstatus;
    }

    public void setOrderstatus(String orderstatus) {
        this.orderstatus = orderstatus;
    }

    public String getPlimit() {
        return plimit;
    }

    public void setPlimit(String plimit) {
        this.plimit = plimit;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getIteam_id() {
        return iteam_id;
    }

    public void setItemId(String iteam_id) {
        this.iteam_id = iteam_id;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductColor() {
        return color;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getMrpprice() {
        return mrp;
    }

    public void setMrpprice(String mrp) {
        this.mrp = mrp;
    }

    public String getSellprice() {
        return sellprice;
    }

    public void setSellprice(String sellprice) {
        this.sellprice = sellprice;
    }


    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status)

    {
        this.status = status;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}


package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ordere {

    private String orderid;
    private String paymentmode;
    private String paymenref;
    private String paymenstatus;
    private String date;
    private String total;
    private String address;
    private List<Product> ordredproduct = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getPaymentmode() {
        return paymentmode;
    }

    public void setPaymentmode(String paymentmode) {
        this.paymentmode = paymentmode;
    }

    public String getPaymenref() {
        return paymenref;
    }

    public void setPaymenref(String paymenref) {
        this.paymenref = paymenref;
    }

    public String getPaymenstatus() {
        return paymenstatus;
    }

    public void setPaymenstatus(String paymenstatus) {
        this.paymenstatus = paymenstatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public List<Product> getOrdredproduct() {
        return ordredproduct;
    }

    public void setOrdredproduct(List<Product> ordredproduct) {
        this.ordredproduct = ordredproduct;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class FAQResponse {

private String title;
private String description;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public String getTitle() {
return title;
}

public void setTitle(String title) {
this.title = title;
}

public String getDescription() {
return description;
}

public void setDescription(String description) {
this.description = description;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class Category {

    private String cat_id;
    private String category_name;
    private String category_image;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_image() {
        return category_image;
    }

    public void setCategory_image(String category_image) {
        this.category_image = category_image;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class TermsResponse {

private String terms;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public String getTerms() {
return terms;
}

public void setTerms(String terms) {
this.terms = terms;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartistResponse {


    private String success;
    private String cartid;
    private String userid;
    private String useremail;
    private String tax;
    private String shipping;
    private List<Product> products = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getCartid() {
        return cartid;
    }

    public void setCartid(String cartid) {
        this.cartid = cartid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
    public String getShipping() {
        return shipping;
    }

    public void setShipping(String shipping) {
        this.shipping = shipping;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.List;

public class WishlistResponse {

    private String success;
    private String message;

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Product> getProducts() {
        return product;
    }

    public void setProducts(List<Product> products) {
        this.product = products;
    }

    private List<Product> product = null;

}

package co.sandyedemo.ecomdemo.MVP;
import java.util.HashMap;
import java.util.Map;

public class StripeResponse {

private String Success;
private String Message;
private Integer amount;
private String status;
private String date;
private String paymentref;
private String currency;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public String getSuccess() {
return Success;
}

public void setSuccess(String success) {
this.Success = success;
}

public String getMessage() {
return Message;
}

public void setMessage(String message) {
this.Message = message;
}

public Integer getAmount() {
return amount;
}

public void setAmount(Integer amount) {
this.amount = amount;
}

public String getStatus() {
return status;
}

public void setStatus(String status) {
this.status = status;
}

public String getDate() {
return date;
}

public void setDate(String date) {
this.date = date;
}

public String getPaymentref() {
return paymentref;
}

public void setPaymentref(String paymentref) {
this.paymentref = paymentref;
}

public String getCurrency() {
return currency;
}

public void setCurrency(String currency) {
this.currency = currency;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class AddToWishlistResponse {

private String success;
private String message;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public String getSuccess() {
return success;
}

public void setSuccess(String success) {
this.success = success;
}

public String getMessage() {
return message;
}

public void setMessage(String message) {
this.message = message;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class RegistrationResponse {

private String Success;
private String Message;
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

public String getSuccess() {
return Success;
}

public void setSuccess(String success) {
this.Success = success;
}

public String getMessage() {
return Message;
}

public void setMessage(String message) {
this.Message = message;
}

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class SignUpResponse {

    private String success;
    private String message;
    private Integer userid;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.Map;

public class SliderListResponse {

    private Product productsdetails;
    private String id;
    private String bannerimage;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Product getProductsdetails() {
        return productsdetails;
    }

    public void setProductsdetails(Product productsdetails) {
        this.productsdetails = productsdetails;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return bannerimage;
    }

    public void setImage(String image) {
        this.bannerimage = image;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

package co.sandyedemo.ecomdemo.MVP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOrdersResponse {
    private String success;

    private String userid;
    private String useremail;
    private String tax;
    private String shipping;
    private List<Ordere> orderes = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUseremail() {
        return useremail;
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public String getTax() {
        return tax;
    }

    public void setTax(String tax) {
        this.tax = tax;
    }

    public String getShipping() {
        return shipping;
    }

    public void setShipping(String shipping) {
        this.shipping = shipping;
    }

    public List<Ordere> getOrderes() {
        return orderes;
    }

    public void setOrderes(List<Ordere> orderes) {
        this.orderes = orderes;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}


package co.sandyedemo.ecomdemo.Retrofit;


import co.sandyedemo.ecomdemo.MVP.AddToWishlistResponse;
import co.sandyedemo.ecomdemo.MVP.CartistResponse;
import co.sandyedemo.ecomdemo.MVP.CategoryListResponse;
import co.sandyedemo.ecomdemo.MVP.FAQResponse;
import co.sandyedemo.ecomdemo.MVP.MyOrdersResponse;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.MVP.RegistrationResponse;
import co.sandyedemo.ecomdemo.MVP.SignUpResponse;
import co.sandyedemo.ecomdemo.MVP.SliderListResponse;
import co.sandyedemo.ecomdemo.MVP.StripeResponse;
import co.sandyedemo.ecomdemo.MVP.TermsResponse;
import co.sandyedemo.ecomdemo.MVP.UserProfileResponse;
import co.sandyedemo.ecomdemo.MVP.WishlistResponse;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

public interface ApiInterface {

    // API's endpoints
    @GET("/app_dashboard/JSON/allproducts.php")
    public void getAllProducts(
            Callback<List<Product>> callback);

    @GET("/app_dashboard/JSON/pbyc.php")
    public void getCategoryList(Callback<List<CategoryListResponse>> callback);

    @GET("/app_dashboard/JSON/slider.php")
    public void getSliderList(Callback<List<SliderListResponse>> callback);

    @GET("/app_dashboard/JSON/faq.php")
    public void getFAQ(Callback<FAQResponse> callback);

    @GET("/app_dashboard/JSON/terms.php")
    public void getTerms(Callback<TermsResponse> callback);

    @FormUrlEncoded
    @POST("/app_dashboard/JSON/pushadd.php")
    public void sendAccessToken(@Field("accesstoken") String accesstoken, Callback<RegistrationResponse> callback);

    @FormUrlEncoded
    @POST("/app_dashboard/JSON/addwishlist.php")
    public void addToWishList(@Field("product_id") String product_id, @Field("user_id") String user_id, Callback<AddToWishlistResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/add_cart.php")
    public void addToCart(@Field("product_id") String product_id, @Field("userid") String user_id,
                          @Field("cartquantity") String cartquantity, @Field("size") String size,
                          @Field("color") String color, Callback<AddToWishlistResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/updatecart.php")
    public void updateCart(@Field("cartquantity") String cartquantity, @Field("iteamid") String iteamid, Callback<AddToWishlistResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/wishcheck.php")
    public void checkWishList(@Field("product_id") String product_id, @Field("user_id") String user_id, Callback<AddToWishlistResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/wishlist.php")
    public void getWishList(@Field("user_id") String user_id, Callback<WishlistResponse> callback);



    @FormUrlEncoded
    @POST("/app_dashboard/JSON/product.php")
    public void getProductDetails(@Field("product_id") String product_id, Callback<Product> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/vieworders.php")
    public void getMyOrders(@Field("user_id") String user_id, Callback<MyOrdersResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/viewcart.php")
    public void getCartList(@Field("user_id") String user_id, Callback<CartistResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/userprofile.php")
    public void getUserProfile(@Field("user_id") String user_id, Callback<UserProfileResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/updateprofile.php")
    public void updateProfile(@Field("user_id") String user_id,
                              @Field("name") String name,
                              @Field("city") String city,
                              @Field("state") String state,
                              @Field("pincode") String pincode,
                              @Field("local") String local,
                              @Field("flat") String flat,
                              @Field("gender") String gender,
                              @Field("phone") String phone,
                              @Field("landmark") String landmark,
                              Callback<SignUpResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/resentmail.php")
    public void resentEmail(@Field("email") String email, Callback<SignUpResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/login.php")
    public void login(@Field("email") String email, @Field("password") String password, @Field("logintype") String logintype, Callback<SignUpResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/paystripe.php")
    public void stripePayment(@Field("stripeToken") String stripeToken,
                              @Field("total") String total,
                              @Field("user_id") String user_id,
                              @Field("cart_id") String cart_id,
                              @Field("address") String address,
                              @Field("phone") String phone,
                              Callback<StripeResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/addorders.php")
    public void addOrder(@Field("user_id") String user_id,
                         @Field("cart_id") String cart_id,
                         @Field("address") String address,
                         @Field("phone") String phone,
                         @Field("paymentref") String paymentref,
                         @Field("paystatus") String paystatus,
                         @Field("total") String total,
                         @Field("paymentmode") String paymentmode,
                         Callback<SignUpResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/forgot.php")
    public void forgotPassword(@Field("email") String email, Callback<SignUpResponse> callback);


    @FormUrlEncoded
    @POST("/app_dashboard/JSON/register.php")
    public void registration(@Field("name") String name, @Field("email") String email, @Field("password") String password, @Field("logintype") String logintype, Callback<SignUpResponse> callback);


}


package co.sandyedemo.ecomdemo.Retrofit;

import retrofit.RestAdapter;

/**
 * Created by AbhiAndroid
 */
public class Api {

    public static ApiInterface getClient() {

        // change your base URL
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("http://sandyedemo.co") //Set the Root URL
                .build(); //Finally building the adapter

        //Creating object for our interface
        ApiInterface api = adapter.create(ApiInterface.class);
        return api;
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.ButterKnife;

public class DetailOrderedProductsListViewHolder extends RecyclerView.ViewHolder {

    ImageView image1;
    TextView productName1,size,color,qty,price;

    public DetailOrderedProductsListViewHolder(final Context context, View itemView, List<Product> productList) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        image1 = (ImageView) itemView.findViewById(R.id.productImage1);
        productName1 = (TextView) itemView.findViewById(R.id.productName1);
        size = (TextView) itemView.findViewById(R.id.size);
        color = (TextView) itemView.findViewById(R.id.color);
        qty = (TextView) itemView.findViewById(R.id.quantity);
        price = (TextView) itemView.findViewById(R.id.price);



    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.R;

import java.util.List;


/**
 * Created by Android
 */
public class HomeProductsViewHolder extends RecyclerView.ViewHolder {

    ImageView image, image1,delete;
    TextView productName, price, actualPrice, productName1, price1, actualPrice1, discountPercentage, discountPercentage1;
    CardView cardView, cardView1;

    public HomeProductsViewHolder(final Context context, View itemView, List<Product> productList) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.productImage);
        image1 = (ImageView) itemView.findViewById(R.id.productImage1);
        delete = (ImageView) itemView.findViewById(R.id.delete);
        productName = (TextView) itemView.findViewById(R.id.productName);
        price = (TextView) itemView.findViewById(R.id.price);
        actualPrice = (TextView) itemView.findViewById(R.id.actualPrice);
        productName1 = (TextView) itemView.findViewById(R.id.productName1);
        price1 = (TextView) itemView.findViewById(R.id.price1);
        actualPrice1 = (TextView) itemView.findViewById(R.id.actualPrice1);
        discountPercentage = (TextView) itemView.findViewById(R.id.discountPercentage);
        discountPercentage1 = (TextView) itemView.findViewById(R.id.discountPercentage1);
        cardView = (CardView) itemView.findViewById(R.id.cardView);
        cardView1 = (CardView) itemView.findViewById(R.id.cardView1);

    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Fragments.MyWishList;
import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.MVP.AddToWishlistResponse;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import com.squareup.picasso.Picasso;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by Android
 */
public class WishListAdapter extends RecyclerView.Adapter<HomeProductsViewHolder> {
    Context context;
    List<Product> productList;

    public WishListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public HomeProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.wish_list_items, null);
        HomeProductsViewHolder homeProductsViewHolder = new HomeProductsViewHolder(context, view, productList);
        return homeProductsViewHolder;
    }

    @Override
    public void onBindViewHolder(final HomeProductsViewHolder holder, final int position) {


        holder.cardView.setVisibility(View.GONE);
        holder.cardView1.setVisibility(View.VISIBLE);
        holder.productName1.setText(productList.get(position).getProductName());
        holder.price1.setText(MainActivity.currency + " " + productList.get(position).getSellprice());
        try {
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageWidth1)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .placeholder(R.drawable.defaultimage)
                    .into(holder.image1);
        } catch (Exception e) {
        }
        try {
            double discountPercentage = Integer.parseInt(productList.get(position).getMrpprice()) - Integer.parseInt(productList.get(position).getSellprice());
            Log.d("percentage", discountPercentage + "");
            discountPercentage = (discountPercentage / Integer.parseInt(productList.get(position).getMrpprice())) * 100;
            if ((int) Math.round(discountPercentage) > 0) {
                holder.discountPercentage1.setText(((int) Math.round(discountPercentage) + "% Off"));
            }
            holder.actualPrice1.setText(MainActivity.currency + " " + productList.get(position).getMrpprice());
            holder.actualPrice1.setPaintFlags(holder.actualPrice1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }catch (Exception e){}
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductDetail.productList.clear();
                ProductDetail.productList.addAll(productList);
                ProductDetail productDetail = new ProductDetail();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                productDetail.setArguments(bundle);
                ((MainActivity) context).loadFragment(productDetail, true);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToWishList(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void addToWishList(final int position) {
        final SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        Api.getClient().addToWishList(productList.get(position).getProductId(),
                MainActivity.userId,
                new Callback<AddToWishlistResponse>() {
                    @Override
                    public void success(AddToWishlistResponse addToWishlistResponse, Response response) {
                        pDialog.dismiss();

                        Log.d("addToWishListResponse", addToWishlistResponse.getSuccess() + "");
                        if (addToWishlistResponse.getSuccess().equalsIgnoreCase("true")) {
                            ((MainActivity) context).loadFragment(new MyWishList(), false);
                            Config.showCustomAlertDialog(context,
                                    "Your wishlist status",
                                    addToWishlistResponse.getMessage(),
                                    SweetAlertDialog.SUCCESS_TYPE);
                        }else {
                            Config.showCustomAlertDialog(context,
                                    "Your wishlist status",
                                    addToWishlistResponse.getMessage(),
                                    SweetAlertDialog.NORMAL_TYPE);
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Fragments.MyOrderedProductsDetailPage;
import co.sandyedemo.ecomdemo.MVP.Ordere;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;

import java.util.List;


/**
 * Created by Android
 */
public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersViewHolder> {

    Context context;
    List<Ordere> orderes;

    public MyOrdersAdapter(Context context, List<Ordere> orderes) {
        this.context = context;
        this.orderes = orderes;
    }

    @Override
    public MyOrdersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.my_orders_list_items, null);
        MyOrdersViewHolder MyOrdersViewHolder = new MyOrdersViewHolder(context, view);
        return MyOrdersViewHolder;
    }

    @Override
    public void onBindViewHolder(MyOrdersViewHolder holder, final int position) {
        setProductsData(holder, position);
        holder.date.setText("Date: " + orderes.get(position).getDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyOrderedProductsDetailPage.orderes = orderes;
                MyOrderedProductsDetailPage.pos = position;
                ((MainActivity) context).loadFragment(new MyOrderedProductsDetailPage(), true);
            }
        });

    }

    @Override
    public int getItemCount() {
        return orderes.size();
    }


    private void setProductsData(MyOrdersViewHolder holder, int position) {
        Log.d("orderProducts", orderes.get(position).getOrdredproduct().size() + "");
        GridLayoutManager gridLayoutManager;
        gridLayoutManager = new GridLayoutManager(context, 1);
        holder.orderedProductsRecyclerView.setLayoutManager(gridLayoutManager);
        OrderProductListAdapter myOrdersAdapter = new OrderProductListAdapter(context, orderes.get(position).getOrdredproduct());
        holder.orderedProductsRecyclerView.setAdapter(myOrdersAdapter);

    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Fragments.CategoryList;
import co.sandyedemo.ecomdemo.Fragments.Home;
import co.sandyedemo.ecomdemo.Fragments.ProductsList;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;


/**
 * Created by Android
 */
public class CategoriesViewHolder extends RecyclerView.ViewHolder {

    ImageView image;
    TextView catName;
    CardView cardView;

    public CategoriesViewHolder(final Context context, View itemView) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.categoryIcon);
        catName = (TextView) itemView.findViewById(R.id.categoryName);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Home.swipeRefreshLayout.isRefreshing())
                    if (getAdapterPosition() == 3) {
                        ((MainActivity) context).loadFragment(new CategoryList(), true);
                    } else {
                        ProductsList.categoryPosition = getAdapterPosition();
                        ((MainActivity) context).loadFragment(new ProductsList(), true);
                    }
            }
        });
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class CartListViewHolder extends RecyclerView.ViewHolder {

    ImageView image1;
    ImageView delete;
    TextView productName1, price1, actualPrice1, discountPercentage1, quantity, size, color, txtGurantee;
    CardView cardView1;
    @BindView(R.id.totalAmount)
    LinearLayout totalAmount;
    @BindViews({R.id.txtPrice, R.id.price, R.id.delivery,  R.id.tax,  R.id.amountPayable,  R.id.txtTax})
    List<TextView> textViews;

    public CartListViewHolder(final Context context, View itemView, List<Product> productList) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        image1 = (ImageView) itemView.findViewById(R.id.productImage1);
        delete = (ImageView) itemView.findViewById(R.id.delete);
        productName1 = (TextView) itemView.findViewById(R.id.productName1);
        size = (TextView) itemView.findViewById(R.id.size);
        color = (TextView) itemView.findViewById(R.id.color);
        price1 = (TextView) itemView.findViewById(R.id.price1);
        quantity = (TextView) itemView.findViewById(R.id.quantity);
        txtGurantee = (TextView) itemView.findViewById(R.id.txtGurantee);
        actualPrice1 = (TextView) itemView.findViewById(R.id.actualPrice1);
        discountPercentage1 = (TextView) itemView.findViewById(R.id.discountPercentage1);
        cardView1 = (CardView) itemView.findViewById(R.id.cardView1);


    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Android
 */
public class ProductListAdapter extends RecyclerView.Adapter<HomeProductsViewHolder> {
    Context context;
    List<Product> productList;
    int categoryPosition;

    public ProductListAdapter(Context context, List<Product> productList, int categoryPosition) {
        this.context = context;
        this.productList = productList;
        this.categoryPosition = categoryPosition;
    }

    @Override
    public HomeProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_products_list_items, null);
        HomeProductsViewHolder homeProductsViewHolder = new HomeProductsViewHolder(context, view, productList);
        return homeProductsViewHolder;
    }

    @Override
    public void onBindViewHolder(final HomeProductsViewHolder holder, final int position) {


        holder.cardView.setVisibility(View.GONE);
        holder.cardView1.setVisibility(View.VISIBLE);
        holder.productName1.setText(productList.get(position).getProductName());
        holder.price1.setText(MainActivity.currency + " " + productList.get(position).getSellprice());
        try {
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageWidth1)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .placeholder(R.drawable.defaultimage)
                    .into(holder.image1);
        } catch (Exception e) {
        }
        try {
            double discountPercentage = Integer.parseInt(productList.get(position).getMrpprice()) - Integer.parseInt(productList.get(position).getSellprice());
            Log.d("percentage", discountPercentage + "");
            discountPercentage = (discountPercentage / Integer.parseInt(productList.get(position).getMrpprice())) * 100;
            if ((int) Math.round(discountPercentage) > 0) {
                holder.discountPercentage1.setText(((int) Math.round(discountPercentage) + "% Off"));
            }
            holder.actualPrice1.setText(MainActivity.currency + " " + productList.get(position).getMrpprice());
            holder.actualPrice1.setPaintFlags(holder.actualPrice1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        } catch (Exception e) {
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductDetail.productList.clear();
                ProductDetail.productList.addAll(productList);
                ProductDetail productDetail = new ProductDetail();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                productDetail.setArguments(bundle);
                ((MainActivity) context).loadFragment(productDetail, true);
            }
        });


    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

}


package co.sandyedemo.ecomdemo.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import co.sandyedemo.ecomdemo.Fragments.DetailPageFragment;

import java.util.ArrayList;
import java.util.List;

public class DetailPageSliderPagerAdapter extends FragmentStatePagerAdapter {
    public static int LOOPS_COUNT = 1000;
    private List<String> imagesList;


    public DetailPageSliderPagerAdapter(FragmentManager manager, List<String> imagesList) {
        super(manager);
        this.imagesList = imagesList;
    }


    @Override
    public Fragment getItem(int position) {
        if (imagesList != null && imagesList.size() > 0) {
            position = position % imagesList.size(); // use modulo for infinite cycling
            return DetailPageFragment.newInstance(position, (ArrayList<String>) imagesList);
        } else {
            return DetailPageFragment.newInstance(0, (ArrayList<String>) imagesList);
        }
    }


    @Override
    public int getCount() {
        if (imagesList != null && imagesList.size() > 1) {
            return imagesList.size() * LOOPS_COUNT; // simulate infinite by big number of products
        } else {
            return 1;
        }
    }
} 

package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import co.sandyedemo.ecomdemo.R;


/**
 * Created by Android
 */
public class DotViewHolder extends RecyclerView.ViewHolder {

    ImageView dotImageView;

    public DotViewHolder(final Context context, View itemView) {
        super(itemView);
        dotImageView = (ImageView) itemView.findViewById(R.id.dotImageView);

    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Fragments.Home;
import co.sandyedemo.ecomdemo.Fragments.ProductsList;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;


/**
 * Created by Android
 */
public class CategoriesProductsViewHolder extends RecyclerView.ViewHolder {

    TextView catName;
    CardView cardView;
    RecyclerView productsRecyclerView;
    Button viewAll;
    LinearLayout homeCategoryProductLayout;
    RelativeLayout homeCategoryRelativeLayout;

    public CategoriesProductsViewHolder(final Context context, View itemView) {
        super(itemView);
        productsRecyclerView = (RecyclerView) itemView.findViewById(R.id.productsRecyclerView);
        catName = (TextView) itemView.findViewById(R.id.categoryName);
        viewAll = (Button) itemView.findViewById(R.id.viewAll);
        homeCategoryProductLayout = (LinearLayout) itemView.findViewById(R.id.homeCategoryProductLayout);
        homeCategoryRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.homeCategoryRelativeLayout);
        viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Home.swipeRefreshLayout.isRefreshing()) {
                    ProductsList.categoryPosition = getAdapterPosition();
                    ((MainActivity) context).loadFragment(new ProductsList(), true);
                }
            }
        });
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Fragments.MyOrderedProductsDetailPage;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Android
 */
public class DetailOrderProductListAdapter extends RecyclerView.Adapter<DetailOrderedProductsListViewHolder> {
    Context context;
    List<Product> productList;

    public DetailOrderProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public DetailOrderedProductsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.detail_ordered_products_list_items1, null);
        DetailOrderedProductsListViewHolder DetailOrderedProductsListViewHolder = new DetailOrderedProductsListViewHolder(context, view, productList);
        return DetailOrderedProductsListViewHolder;
    }

    @Override
    public void onBindViewHolder(final DetailOrderedProductsListViewHolder holder, final int position) {

        holder.productName1.setText(productList.get(position).getProductName());

        if (!productList.get(position).getSize().equalsIgnoreCase("")) {
            Log.d("size", productList.get(position).getSize());
            holder.size.setText("Size: " + productList.get(position).getSize());
            holder.size.setVisibility(View.VISIBLE);
        } else {
            holder.size.setVisibility(View.GONE);
        }
        if (!productList.get(position).getProductColor().equalsIgnoreCase("")) {
            Log.d("color", productList.get(position).getProductColor());
            holder.color.setText("Color: " + productList.get(position).getProductColor());
            holder.color.setVisibility(View.VISIBLE);
        } else {
            holder.color.setVisibility(View.INVISIBLE);
        }

        holder.qty.setText("Qty: " + productList.get(position).getQuantity());
        holder.price.setText("Price: " + MyOrderedProductsDetailPage.currency + " " + productList.get(position).getSellprice());
        try {
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageWidth1)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .placeholder(R.drawable.defaultimage)
                    .into(holder.image1);
        } catch (Exception e) {
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.MVP.CategoryListResponse;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Android
 */
public class HomeCategoryAdapter extends RecyclerView.Adapter<CategoriesViewHolder> {
    Context context;
    List<CategoryListResponse> categoryListResponses;
    int size;

    public HomeCategoryAdapter(Context context, List<CategoryListResponse> categoryListResponses,int size) {
        this.context = context;
        this.categoryListResponses = categoryListResponses;
        this.size=size;
    }

    @Override
    public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.homw_category_list_items, null);
        CategoriesViewHolder categoriesViewHolder = new CategoriesViewHolder(context, view);
        return categoriesViewHolder;
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder holder, int position) {
        if (position == 3) {
            holder.catName.setText("More");
            holder.image.setImageResource(R.drawable.new_more_icon);
        } else {
            holder.catName.setText(categoryListResponses.get(position).getCategory_name());
            String temp = categoryListResponses.get(position).getCategory_image().replaceAll(" ", "%20");
            Picasso.with(context)
                    .load(temp)
                    .placeholder(R.drawable.defaultimage)
                    .resize(Integer.parseInt(context.getResources().getString(R.string.cartImageWidth)),Integer.parseInt(context.getResources().getString(R.string.cartImageWidth)))
                    .into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        return size;
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Fragments.ProductsList;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;


/**
 * Created by Android
 */
public class CategorieListViewHolder extends RecyclerView.ViewHolder {

    ImageView image;
    TextView catName;
    CardView cardView;

    public CategorieListViewHolder(final Context context, View itemView) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.categoryIcon);
        catName = (TextView) itemView.findViewById(R.id.categoryName);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductsList.categoryPosition = getAdapterPosition();
                ((MainActivity) context).loadFragment(new ProductsList(), true);
            }
        });
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Fragments.Home;
import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Android
 */
public class HomeProductsAdapter extends RecyclerView.Adapter<HomeProductsViewHolder> {
    Context context;
    List<Product> productList;
    ;
    int i, parentPosition;

    public HomeProductsAdapter(Context context, List<Product> productList, int i, int position) {
        this.context = context;
        this.i = i;
        this.parentPosition = position;
        this.productList = productList;
    }

    @Override
    public HomeProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_products_list_items, null);
        HomeProductsViewHolder homeProductsViewHolder = new HomeProductsViewHolder(context, view, productList);
        return homeProductsViewHolder;
    }

    @Override
    public void onBindViewHolder(final HomeProductsViewHolder holder, final int position) {

        if (this.parentPosition % 2 == 0) {
            holder.cardView.setVisibility(View.VISIBLE);
            holder.cardView1.setVisibility(View.GONE);
        } else {
            holder.cardView.setVisibility(View.GONE);
            holder.cardView1.setVisibility(View.VISIBLE);
        }
        holder.productName.setText(productList.get(position).getProductName());
        holder.price.setText(MainActivity.currency + " " + productList.get(position).getSellprice());

        try {
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .placeholder(R.drawable.defaultimage)
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageWidth)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .into(holder.image);
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
        holder.productName1.setText(productList.get(position).getProductName());
        holder.price1.setText(MainActivity.currency + " " + productList.get(position).getSellprice());
        try {
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageWidth1)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .placeholder(R.drawable.defaultimage)
                    .into(holder.image1);
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
        try {
            double discountPercentage = Integer.parseInt(productList.get(position).getMrpprice()) - Integer.parseInt(productList.get(position).getSellprice());
            Log.d("percentage", discountPercentage + "");
            discountPercentage = (discountPercentage / Integer.parseInt(productList.get(position).getMrpprice())) * 100;
            if ((int) Math.round(discountPercentage) > 0) {
                holder.discountPercentage.setText(((int) Math.round(discountPercentage) + "% Off"));
                holder.discountPercentage1.setText(((int) Math.round(discountPercentage) + "% Off"));
            }
            Log.d("mrptextsize", productList.get(position).getMrpprice().length() + "");
            holder.actualPrice.setText(MainActivity.currency + " " + productList.get(position).getMrpprice());
            holder.actualPrice.setPaintFlags(holder.actualPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.actualPrice1.setPaintFlags(holder.actualPrice1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.actualPrice1.setText(MainActivity.currency + " " + productList.get(position).getMrpprice());
        } catch (Exception e) {

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Home.swipeRefreshLayout.isRefreshing()) {
                    ProductDetail.productList.clear();
                    ProductDetail.productList.addAll(productList);
                    ProductDetail productDetail = new ProductDetail();
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    productDetail.setArguments(bundle);
                    ((MainActivity) context).loadFragment(productDetail, true);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return i;
    }

}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Android
 */
public class MyOrdersViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.orderedProductsRecyclerView)
    RecyclerView orderedProductsRecyclerView;
    @BindView(R.id.viewOrderDetails)
    TextView viewOrderDetails;
    @BindView(R.id.date)
    TextView date;


    public MyOrdersViewHolder(final Context context, View itemView) {
        super(itemView);
        ButterKnife.bind(this,itemView);
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Fragments.MyCartList;
import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.MVP.AddToWishlistResponse;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import com.squareup.picasso.Picasso;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by Android
 */
public class CartListAdapter extends RecyclerView.Adapter<CartListViewHolder> {
    Context context;
    List<Product> productList;
    double totalAmount = 0f, amountPayable;
    public static String totalAmountPayable;
    double tax=0f;
    public CartListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        for(int position =0;position<productList.size();position++){
            totalAmount = totalAmount + (Double.parseDouble(productList.get(position).getSellprice()) * Double.parseDouble(productList.get(position).getQuantity()));

        }
    }

    @Override
    public CartListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_list_items, null);
        CartListViewHolder CartListViewHolder = new CartListViewHolder(context, view, productList);
        return CartListViewHolder;
    }

    @Override
    public void onBindViewHolder(final CartListViewHolder holder, final int position) {
        if (position == productList.size() - 1) {
            holder.totalAmount.setVisibility(View.VISIBLE);
            holder.txtGurantee.setText(Html.fromHtml(context.getResources().getString(R.string.secure_payment_text)));

            holder.textViews.get(0).setText("Price (" + productList.size() + " items)");
            holder.textViews.get(1).setText(MainActivity.currency + " " + totalAmount);
            if (MyCartList.cartistResponseData.getShipping().length()>0) {

                holder.textViews.get(2).setText(MainActivity.currency + " " + MyCartList.cartistResponseData.getShipping());
                 amountPayable = totalAmount +
                        Double.parseDouble(MyCartList.cartistResponseData.getShipping());
            }else {
                amountPayable=totalAmount;
                holder.textViews.get(2).setText(MainActivity.currency + " 0.0");

            }
            if (MyCartList.cartistResponseData.getTax().length()>0) {
                 tax = (totalAmount / 100) * Double.parseDouble(MyCartList.cartistResponseData.getTax());
                holder.textViews.get(5).setText("Tax (" + MyCartList.cartistResponseData.getTax() + "%)");
            }
            tax = Double.parseDouble(String.format("%.2f", tax));
            Log.d("floatTax", tax + "");
            holder.textViews.get(3).setText(MainActivity.currency + " " + tax);
            holder.textViews.get(4).setText(MainActivity.currency + " " + (String.format("%.2f", (amountPayable + tax))));
            totalAmountPayable = (String.format("%.2f", (amountPayable + tax)));
            Log.d("totalAmountPayable", totalAmountPayable);
        } else
            holder.totalAmount.setVisibility(View.GONE);

        holder.productName1.setText(productList.get(position).getProductName());
        holder.price1.setText(MainActivity.currency + " " + productList.get(position).getSellprice());
        holder.quantity.setText("Qty: " + productList.get(position).getQuantity());
        try {
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .resize(Integer.parseInt(context.getResources().getString(R.string.cartImageWidth)),Integer.parseInt(context.getResources().getString(R.string.cartImageWidth)))
                    .placeholder(R.drawable.defaultimage)
                    .into(holder.image1);
        } catch (Exception e) {
        }

        if (!productList.get(position).getSize().equalsIgnoreCase("")) {
            Log.d("size", productList.get(position).getSize());
            holder.size.setText("Size: " + productList.get(position).getSize());
            holder.size.setVisibility(View.VISIBLE);
        } else {
            holder.size.setVisibility(View.GONE);
        }
        if (!productList.get(position).getProductColor().equalsIgnoreCase("")) {
            Log.d("color", productList.get(position).getProductColor());
            holder.color.setText("Color: " + productList.get(position).getProductColor());
            holder.color.setVisibility(View.VISIBLE);
        } else {
            holder.color.setVisibility(View.GONE);
        }
        try {
            double discountPercentage = Integer.parseInt(productList.get(position).getMrpprice()) - Integer.parseInt(productList.get(position).getSellprice());
            Log.d("percentage", discountPercentage + "");
            discountPercentage = (discountPercentage / Integer.parseInt(productList.get(position).

                    getMrpprice())) * 100;
            if ((int) Math.round(discountPercentage) > 0)

            {
                holder.discountPercentage1.setText(((int) Math.round(discountPercentage) + "% Off"));
            }
            holder.actualPrice1.setText(MainActivity.currency + " " + productList.get(position).getMrpprice());
            holder.actualPrice1.setPaintFlags(holder.actualPrice1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        } catch (Exception e) {
        }
        holder.productName1.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                ProductDetail.productList.clear();
                ProductDetail.productList.addAll(productList);
                ProductDetail productDetail = new ProductDetail();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                productDetail.setArguments(bundle);
                ((MainActivity) context).loadFragment(productDetail, true);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                updateCart(position, 0 + "");
            }
        });
        final PopupMenu popupMenu = new PopupMenu(context, holder.quantity);
        popupMenu.getMenuInflater().

                inflate(R.menu.textview_popup_menu, popupMenu.getMenu());
        Log.d("productQuantity", Integer.parseInt(productList.get(position).getQuantity()) + "");
        for (int i = 1; i <= Integer.parseInt(productList.get(position).getPlimit()); i++)

        {
            popupMenu.getMenu().add(i + "");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()

        {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (!productList.get(position).getQuantity().trim().equalsIgnoreCase(menuItem.getTitle().toString().trim())) {
                    holder.quantity.setText("Qty: " + menuItem.getTitle() + "");
                    updateCart(position, menuItem.getTitle().toString());
                }
                return false;
            }
        });
        holder.quantity.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                popupMenu.show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void updateCart(final int position, final String quantity) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Api.getClient().updateCart(
                quantity,
                productList.get(position).getIteam_id(),
                new Callback<AddToWishlistResponse>() {
                    @Override
                    public void success(AddToWishlistResponse addToWishlistResponse, Response response) {
                        progressDialog.dismiss();
                        Log.d("addToCartResponse", addToWishlistResponse.getSuccess() + "");
                        if (addToWishlistResponse.getSuccess().equalsIgnoreCase("true")) {
//                            Config.getCartList(context);
                            ((MainActivity) context).loadFragment(new MyCartList(), false);

                            Config.showCustomAlertDialog(context,
                                    "Your Cart status",
                                    addToWishlistResponse.getMessage(),
                                    SweetAlertDialog.SUCCESS_TYPE);
                        } else {

                            Config.showCustomAlertDialog(context,
                                    "Your Cart status",
                                    addToWishlistResponse.getMessage(),
                                    SweetAlertDialog.NORMAL_TYPE);
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progressDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.R;

import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;


public class ColorListAdapter extends RecyclerView.Adapter<ColorListAdapter.MyViewHolder> implements View.OnClickListener {

    ArrayList<String> listData;
    Context context;
    public static int pos;


    public ColorListAdapter(Context context, ArrayList<String> listData) {
        this.context = context;
        this.listData = listData;
        pos = -1;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.size_list_items, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder

        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // set the data in items
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[]{5, 5, 5, 5, 5, 5, 5, 5});
        if (position == pos) {
            holder.size.setTextColor(Color.WHITE);
            shape.setColor(holder.colorPrimary);
        } else {
            holder.size.setTextColor(Color.BLACK);
            shape.setColor(holder.gray);
        }
        holder.size.setText(listData.get(position).trim());
        holder.size.setBackgroundDrawable(shape);
        holder.size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos = position;
                if (Integer.parseInt(ProductDetail.productQuantity) < 1) {
                    ProductDetail.addToCart.setText("Out of Stock");
                } else
                    ProductDetail.addToCart.setText("Add to Cart");
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        @BindView(R.id.size)
        TextView size;
        @BindColor(R.color.gray)
        int gray;
        @BindColor(R.color.colorPrimary)
        int colorPrimary;

        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            ButterKnife.bind(this, itemView);

        }
    }
}

package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.ButterKnife;

public class OrderedProductsListViewHolder extends RecyclerView.ViewHolder {

    ImageView image1;
    TextView productName1;

    public OrderedProductsListViewHolder(final Context context, View itemView, List<Product> productList) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        image1 = (ImageView) itemView.findViewById(R.id.productImage1);
        productName1 = (TextView) itemView.findViewById(R.id.productName1);



    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.MVP.CategoryListResponse;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Activities.SplashScreen;

import java.util.List;


/**
 * Created by Android
 */
public class HomeCategoryProductsAdapter extends RecyclerView.Adapter<CategoriesProductsViewHolder> {
    Context context;
    List<CategoryListResponse> categoryListResponses;

    public HomeCategoryProductsAdapter(Context context, List<CategoryListResponse> categoryListResponses) {
        this.context = context;
        this.categoryListResponses = categoryListResponses;
    }

    @Override
    public CategoriesProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.homw_category_products_list_items, null);
        CategoriesProductsViewHolder categoriesProductsViewHolder = new CategoriesProductsViewHolder(context, view);
        return categoriesProductsViewHolder;
    }

    @Override
    public void onBindViewHolder(CategoriesProductsViewHolder holder, int position) {
        if (SplashScreen.categoryListResponseData.get(position).getProducts().size() > 0) {
            holder.homeCategoryProductLayout.setVisibility(View.VISIBLE);
            holder.homeCategoryRelativeLayout.setVisibility(View.VISIBLE);
            holder.catName.setText(categoryListResponses.get(position).getCategory_name());
            setCategoryProductsData(holder.productsRecyclerView, position);
        }else
        {
            holder.homeCategoryProductLayout.setVisibility(View.GONE);
            holder.homeCategoryRelativeLayout.setVisibility(View.GONE);
        }


    }

    private void setCategoryProductsData(RecyclerView productsRecyclerView, int position) {
        HomeProductsAdapter homeProductsAdapter;
        GridLayoutManager gridLayoutManager;
        if (position % 2 == 0) {
            gridLayoutManager = new GridLayoutManager(context, 2);
        } else
            gridLayoutManager = new GridLayoutManager(context, 1);

        productsRecyclerView.setLayoutManager(gridLayoutManager);
        if (SplashScreen.categoryListResponseData.get(position).getProducts().size() > 4)
            homeProductsAdapter = new HomeProductsAdapter(context, SplashScreen.categoryListResponseData.get(position).getProducts(), 4, position);
        else
            homeProductsAdapter = new HomeProductsAdapter(context, SplashScreen.categoryListResponseData.get(position).getProducts(), SplashScreen.categoryListResponseData.get(position).getProducts().size(), position);


        productsRecyclerView.setAdapter(homeProductsAdapter);

    }

    @Override
    public int getItemCount() {
        return categoryListResponses.size();
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Android
 */
public class OrderProductListAdapter extends RecyclerView.Adapter<OrderedProductsListViewHolder> {
    Context context;
    List<Product> productList;

    public OrderProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public OrderedProductsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ordered_products_list_items1, null);
        OrderedProductsListViewHolder OrderedProductsListViewHolder = new OrderedProductsListViewHolder(context, view, productList);
        return OrderedProductsListViewHolder;
    }

    @Override
    public void onBindViewHolder(final OrderedProductsListViewHolder holder, final int position) {

        holder.productName1.setText(productList.get(position).getProductName());

        try {
            Log.d("image",productList.get(position).getImages().get(0));
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageWidth1)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .placeholder(R.drawable.defaultimage)
                    .into(holder.image1);
        } catch (Exception e) {
            holder.image1.setImageResource(R.drawable.defaultimage);
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.R;

import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;


public class SizeListAdapter extends RecyclerView.Adapter<SizeListAdapter.MyViewHolder> implements View.OnClickListener {

    ArrayList<String> listData;
    Context context;
    public static int pos;


    public SizeListAdapter(Context context, ArrayList<String> listData) {
        this.context = context;
        this.listData = listData;
        pos = -1;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.size_list_items, parent, false);
        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder

        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // set the data in items
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadii(new float[]{5, 5, 5, 5, 5, 5, 5, 5});
        if (position == pos) {
            holder.size.setTextColor(Color.WHITE);
            shape.setColor(holder.colorPrimary);
        } else {
            holder.size.setTextColor(Color.BLACK);
            shape.setColor(holder.gray);
        }
        holder.size.setText(listData.get(position).trim());
        holder.size.setBackgroundDrawable(shape);
        holder.size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pos = position;
                if (Integer.parseInt(ProductDetail.productQuantity) < 1) {
                    ProductDetail.addToCart.setText("Out of Stock");
                } else
                    ProductDetail.addToCart.setText("Add to Cart");
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        @BindView(R.id.size)
        TextView size;
        @BindColor(R.color.gray)
        int gray;
        @BindColor(R.color.colorPrimary)
        int colorPrimary;

        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            ButterKnife.bind(this, itemView);

        }
    }
}

package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Android
 */
public class SearchProductListAdapter extends RecyclerView.Adapter<HomeProductsViewHolder> {
    Context context;
    List<Product> productList;

    public SearchProductListAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @Override
    public HomeProductsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.home_products_list_items, null);
        HomeProductsViewHolder homeProductsViewHolder = new HomeProductsViewHolder(context, view, productList);
        return homeProductsViewHolder;
    }

    @Override
    public void onBindViewHolder(final HomeProductsViewHolder holder, final int position) {


        holder.cardView.setVisibility(View.GONE);
        holder.cardView1.setVisibility(View.VISIBLE);
        holder.productName1.setText(productList.get(position).getProductName());
        holder.price1.setText(MainActivity.currency + " " + productList.get(position).getSellprice());
        try {
            Picasso.with(context)
                    .load(productList.get(position).getImages().get(0))
                    .placeholder(R.drawable.defaultimage)
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageWidth1)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .into(holder.image1);
        } catch (Exception e) {
        }
        try {

            double discountPercentage = Integer.parseInt(productList.get(position).getMrpprice()) - Integer.parseInt(productList.get(position).getSellprice());
            Log.d("percentage", discountPercentage + "");
            discountPercentage = (discountPercentage / Integer.parseInt(productList.get(position).getMrpprice())) * 100;
            if ((int) Math.round(discountPercentage) > 0) {
                holder.discountPercentage1.setText(((int) Math.round(discountPercentage) + "% Off"));
            }
            holder.actualPrice1.setText(MainActivity.currency + " " + productList.get(position).getMrpprice());
            holder.actualPrice1.setPaintFlags(holder.actualPrice1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        }catch (Exception e){}
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductDetail.productList.clear();
                ProductDetail.productList.addAll(productList);
                ProductDetail productDetail = new ProductDetail();
                Bundle bundle = new Bundle();
                bundle.putInt("position", position);
                productDetail.setArguments(bundle);
                ((MainActivity) context).loadFragment(productDetail, true);
            }
        });


    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

}


package co.sandyedemo.ecomdemo.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import co.sandyedemo.ecomdemo.Fragments.PageFragment;
import co.sandyedemo.ecomdemo.MVP.SliderListResponse;

import java.util.List;

public class MyPagerAdapter extends FragmentStatePagerAdapter {
    public static int LOOPS_COUNT = 1000;
    private List<SliderListResponse> sliderListResponsesData;


    public MyPagerAdapter(FragmentManager manager, List<SliderListResponse> sliderListResponsesData) {
        super(manager);
        this.sliderListResponsesData = sliderListResponsesData;
    }


    @Override
    public Fragment getItem(int position) {
        if (sliderListResponsesData != null && sliderListResponsesData.size() > 0) {
            position = position % sliderListResponsesData.size(); // use modulo for infinite cycling
            return PageFragment.newInstance(position);
        } else {
            return PageFragment.newInstance(0);
        }
    }


    @Override
    public int getCount() {
        if (sliderListResponsesData != null && sliderListResponsesData.size() > 0) {
            return sliderListResponsesData.size() * LOOPS_COUNT; // simulate infinite by big number of products
        } else {
            return 1;
        }
    }
} 

package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.MVP.CategoryListResponse;
import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by Android
 */
public class CategoryListAdapter extends RecyclerView.Adapter<CategorieListViewHolder> {
    Context context;
    List<CategoryListResponse> categoryListResponses;

    public CategoryListAdapter(Context context, List<CategoryListResponse> categoryListResponses) {
        this.context = context;
        this.categoryListResponses = categoryListResponses;
    }

    @Override
    public CategorieListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_list_items, null);
        CategorieListViewHolder CategorieListViewHolder = new CategorieListViewHolder(context, view);
        return CategorieListViewHolder;
    }

    @Override
    public void onBindViewHolder(CategorieListViewHolder holder, int position) {

            holder.catName.setText(categoryListResponses.get(position).getCategory_name());
            String temp = categoryListResponses.get(position).getCategory_image().replaceAll(" ", "%20");
            Picasso.with(context)
                    .load(temp)
                    .placeholder(R.drawable.defaultimage)
                    .resize(Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)),Integer.parseInt(context.getResources().getString(R.string.targetProductImageHeight)))
                    .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return categoryListResponses.size();
    }
}


package co.sandyedemo.ecomdemo.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.sandyedemo.ecomdemo.R;


/**
 * Created by Android
 */
public class DotsAdapter extends RecyclerView.Adapter<DotViewHolder> {

    Context context;
    int size, selectedPos;

    public DotsAdapter(Context context, int size, int selectedPos) {
        this.context = context;
        this.size = size;
        this.selectedPos = selectedPos;
    }

    @Override
    public DotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dots_list_items, null);
        DotViewHolder DotViewHolder = new DotViewHolder(context, view);
        return DotViewHolder;
    }

    @Override
    public void onBindViewHolder(DotViewHolder holder, int position) {
        if (position == selectedPos) {
            holder.dotImageView.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary));
        } else {
            holder.dotImageView.setColorFilter(ContextCompat.getColor(context, R.color.gray));
        }

    }

    @Override
    public int getItemCount() {
        return size;
    }
}


/*
package co.sandyedemo.ecomdemo.PaymentIntegrationMethods;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.Adapters.CartListAdapter;
import co.sandyedemo.ecomdemo.Fragments.ChoosePaymentMethod;
import co.sandyedemo.ecomdemo.Fragments.MyCartList;
import co.sandyedemo.ecomdemo.MVP.StripeResponse;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
//import com.stripe.android.Stripe;
//import com.stripe.android.TokenCallback;
//import com.stripe.android.model.Card;
//import com.stripe.android.model.Token;
//import com.stripe.android.view.CardInputWidget;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StripePaymentIntegration extends AppCompatActivity {

    @BindView(R.id.submit)
    Button submit;
    SweetAlertDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stripe_payment_integration);
        ButterKnife.bind(this);
        final CardInputWidget mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog = new SweetAlertDialog(StripePaymentIntegration.this, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
                pDialog.setTitleText("Loading");
                pDialog.setCancelable(false);
                pDialog.show();
                try {
                    Log.d("cardDetails", mCardInputWidget.getCard().getNumber());
                    Card cardToSave = mCardInputWidget.getCard();
                    if (cardToSave == null) {
                        Toast.makeText(getApplicationContext(), "Invalid Card Data", Toast.LENGTH_SHORT).show();
                    }
                    Stripe stripe = new Stripe(StripePaymentIntegration.this, "your_key");
                    stripe.createToken(
                            cardToSave,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    // Send token to your server
                                    Log.d("stripeToken", token.getId() + "");
                                    sendStripeToken(token);
                                }

                                public void onError(Exception error) {
                                    // Show localized error message
                                    Toast.makeText(getApplicationContext(),
                                            error.toString(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                    pDialog.dismiss();
                                }
                            }
                    );
                } catch (Exception e) {
                    new SweetAlertDialog(StripePaymentIntegration.this)
                            .setTitleText("Card Details:")
                            .setContentText("Please fill your card details.")
                            .show();
                    pDialog.dismiss();

                }
            }
        });
    }

    */
/*private void sendStripeToken(Token token) {

        // sending gcm token to server
        Api.getClient().stripePayment(token.getId(),
                CartListAdapter.totalAmountPayable,
                MainActivity.userId,
                MyCartList.cartistResponseData.getCartid(),
                ChoosePaymentMethod.address,
                ChoosePaymentMethod.mobileNo,
                new Callback<StripeResponse>() {
                    @Override
                    public void success(StripeResponse stripeResponse, Response response) {
                        pDialog.dismiss();
                        try {
                            Log.d("stripeResponse", stripeResponse.getStatus());
                            if (stripeResponse.getStatus().equalsIgnoreCase("succeeded"))
                            {
                                Intent intent=new Intent(StripePaymentIntegration.this,OrderConfirmed.class);
                                startActivity(intent);
                            //    finishAffinity();
                            }else {
                                finish();
                            }

                        } catch (Exception e) {
                            new SweetAlertDialog(StripePaymentIntegration.this)
                                    .setTitleText("Payment Failed")
                                    .setContentText("Something went wrong. Please try Again.")
                                    .show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }*//*

}
*/


package co.sandyedemo.ecomdemo.PaymentIntegrationMethods;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import co.sandyedemo.ecomdemo.Activities.MainActivity;
import co.sandyedemo.ecomdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrderConfirmed extends AppCompatActivity {

    @BindView(R.id.continueShopping)
    Button continueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmed);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.continueShopping)
    public void onClick(View view) {
        Intent intent = new Intent(OrderConfirmed.this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(OrderConfirmed.this, MainActivity.class);
        startActivity(intent);
        finishAffinity();
    }
}


/*
package co.sandyedemo.ecomdemo.PaymentIntegrationMethods;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import co.sandyedemo.ecomdemo.Adapters.CartListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.R;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class PayPalActivityPayment extends AppCompatActivity {

    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_pal_payment);
        ButterKnife.bind(this);

        final Intent intent = new Intent(PayPalActivityPayment.this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);
        PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(CartListAdapter.totalAmountPayable), "USD", getResources().getString(R.string.app_name), PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent1 = new Intent(PayPalActivityPayment.this, PaymentActivity.class);
        intent1.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent1.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        startActivityForResult(intent1, 9999);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9999) {

            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetails);
                        Log.d("response", jsonObject.getJSONObject("response") + "");
                        if (jsonObject.getJSONObject("response").getString("state").equalsIgnoreCase("approved")) {
                            Config.addOrder(PayPalActivityPayment.this,
                                    jsonObject.getJSONObject("response").getString("id"),
                                    "PayPal Payment-Gateway");
                        } else {
                            new SweetAlertDialog(PayPalActivityPayment.this)
                                    .setTitleText("Payment Failed")
                                    .setContentText("Error While Processing Payment, Please Try Again.")
                                    .show();
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                new SweetAlertDialog(PayPalActivityPayment.this)
                        .setTitleText("Payment")
                        .setContentText("Payment Canceled.")
                        .show();
                finish();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {

                new SweetAlertDialog(PayPalActivityPayment.this)
                        .setTitleText("Payment")
                        .setContentText("Payment Invalid.")
                        .show();
                finish();
            }
        }
    }
}
*/


package co.sandyedemo.ecomdemo.PaymentIntegrationMethods;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;


import org.json.JSONObject;

import co.sandyedemo.ecomdemo.Adapters.CartListAdapter;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Fragments.ChoosePaymentMethod;
import co.sandyedemo.ecomdemo.Fragments.MyCartList;
import co.sandyedemo.ecomdemo.R;

public class RazorPayIntegration extends AppCompatActivity implements PaymentResultListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_razor_pay_integration);
        startPayment();
    }

    public void startPayment() {
        /**
         * You need to pass current activity in order to let Razorpay create CheckoutActivity
         */
        final Activity activity = this;

        final Checkout co = new Checkout();

        try {
            JSONObject options = new JSONObject();
            options.put("name", getResources().getString(R.string.app_name));
            options.put("description", "Payment for "+ MyCartList.cartistResponseData.getProducts().size()+" products");
            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://rzp-mobile.s3.amazonaws.com/images/rzp.png");
            options.put("currency", "INR");

            JSONObject preFill = new JSONObject();
            preFill.put("email", ChoosePaymentMethod.userEmail);
            preFill.put("contact", ChoosePaymentMethod.mobileNo);
            options.put("prefill", preFill);
            String payment = CartListAdapter.totalAmountPayable;

            double total = Double.parseDouble(payment);
            total = total * 100;
            options.put("amount", total);
            co.open(activity, options);
        } catch (Exception e) {
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Config.addOrder(RazorPayIntegration.this,
                razorpayPaymentID,
                "RazorPay Payment-Gateway");

    }

    @Override
    public void onPaymentError(int code, String response) {
        finish();
        try {
            Toast.makeText(this, "Payment error please try again", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OnPaymentError", "Exception in onPaymentError", e);
        }
    }
}


package co.sandyedemo.ecomdemo.FCM;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.RegistrationResponse;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken);

        // sending reg id to your server
        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        Log.e(TAG, "sendRegistrationToServer: " + token);
        Api.getClient().sendAccessToken(token, new Callback<RegistrationResponse>() {
            @Override
            public void success(RegistrationResponse registrationResponse, Response response) {
                Log.d("registrationResponse",registrationResponse.getSuccess());


            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
            }
        });
    }

    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("regId", token);
        editor.commit();
    }
}

package co.sandyedemo.ecomdemo.FCM;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import co.sandyedemo.ecomdemo.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public  String ANDROID_CHANNEL_ID = null;
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
        private Context mContext;
    String value, type;
    int count = 0;
    NotificationManager notificationManager;
    String placeImage, placeId, placeTitle, placeMessage;
    String random_id;
    public static int NOTIFICATION_ID = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());
        mContext=getApplicationContext();
        ANDROID_CHANNEL_ID=getApplicationContext().getPackageName();
        if (remoteMessage == null)
            return;

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Map<String, String> data=remoteMessage.getData();
        placeImage = data.get("image");
        placeTitle = data.get("title");
        placeMessage = data.get("message");
        placeId = data.get("product_id");
        random_id = data.get("random_id");
        //Log.d("placeId", placeId);
        if (NOTIFICATION_ID != Integer.parseInt(random_id)) {
            NOTIFICATION_ID= Integer.parseInt(random_id);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();


            notificationManager.notify(0 /* ID of notification */,  getAndroidChannelNotification(placeTitle,placeMessage).build());
        }else{

            createNotification(remoteMessage);
        }

    }
    private void createNotification(RemoteMessage remoteMessage) {

        Bitmap remote_picture = null;
        int icon = R.drawable.appicon;
        //if message and image url
        if (placeMessage != null && placeImage != null) {


            Log.v("TAG_MESSAGE", "" + placeMessage);
            Log.v("TAG_IMAGE", "" + placeImage);
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(NOTIFICATION_ID);

            NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
            notiStyle.setSummaryText(placeMessage);

            try {
                remote_picture = BitmapFactory.decodeStream((InputStream) new URL(placeImage).getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            notiStyle.bigPicture(remote_picture);
            notificationManager = (NotificationManager) mContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        PendingIntent contentIntent = null;

        Intent gotoIntent = new Intent();
        gotoIntent.putExtra("id", placeId);
        gotoIntent.setClassName(mContext, getApplicationContext().getPackageName()+".Activities.SplashScreen");//Start activity when user taps on notification.
        contentIntent = PendingIntent.getActivity(mContext,
                (int) (Math.random() * 100), gotoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);





        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)

        Notification.Builder mBuilder = new Notification.Builder(
                mContext);
        Notification notification = mBuilder
                .setSmallIcon(R.drawable.appicon).setTicker(placeTitle).setWhen(0)
                .setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.drawable.appicon)).getBitmap())
                .setAutoCancel(true)
                .setContentTitle(placeTitle)
                .setPriority(Notification.PRIORITY_HIGH)
                // .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setStyle(new Notification.BigPictureStyle()
                        .bigPicture(remote_picture)
                        .setBigContentTitle(placeTitle))
                .setContentIntent(contentIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentText(placeMessage).build();
        // .setStyle(notiStyle).build();

        notificationManager.notify(0   , notification);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {

        // create android channel
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true);
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(true);
        // Sets the notification light color for notifications posted to this channel
        androidChannel.setLightColor(Color.GREEN);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        notificationManager.createNotificationChannel(androidChannel);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getAndroidChannelNotification(String title, String message) {
        Bitmap remote_picture = null;
        int icon = R.drawable.appicon;
        //if message and image url
        if (placeMessage != null && placeImage != null) {


                Log.v("TAG_MESSAGE", "" + placeMessage);
                Log.v("TAG_IMAGE", "" + placeImage);
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(NOTIFICATION_ID);

                NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
                notiStyle.setSummaryText(placeMessage);

                try {
                    remote_picture = BitmapFactory.decodeStream((InputStream) new URL(placeImage).getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                notiStyle.bigPicture(remote_picture);

            }
                PendingIntent contentIntent = null;

                Intent gotoIntent = new Intent();
                gotoIntent.putExtra("id", placeId);
                gotoIntent.setClassName(mContext, getApplicationContext().getPackageName()+".Activities.SplashScreen");//Start activity when user taps on notification.
                contentIntent = PendingIntent.getActivity(mContext,
                        (int) (Math.random() * 100), gotoIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);




        return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.appicon).setTicker(placeTitle).setWhen(0)
                .setLargeIcon(((BitmapDrawable) getResources().getDrawable(R.drawable.appicon)).getBitmap())
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setStyle(new Notification.BigPictureStyle()
                        .bigPicture(remote_picture)
                        .setBigContentTitle(placeTitle))
                ;
    }

}

package co.sandyedemo.ecomdemo.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.SignUpResponse;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ForgotPassword extends AppCompatActivity {
    @BindViews({R.id.emailId})
    List<EditText> editTexts;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.back,R.id.submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.submit:
                if (Config.validateEmail(editTexts.get(0),ForgotPassword.this)) {
                    forgotPassword();
                }
                break;
        }
    }

    private void forgotPassword() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(ForgotPassword.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        // sending gcm token to server
        Api.getClient().forgotPassword(editTexts.get(0).getText().toString().trim(),
                new Callback<SignUpResponse>() {
                    @Override
                    public void success(SignUpResponse signUpResponse, Response response) {
                        pDialog.dismiss();
                        Log.d("signUpResponse", signUpResponse.getMessage());
                        Toast.makeText(ForgotPassword.this, signUpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if (signUpResponse.getSuccess().equalsIgnoreCase("true")) {
                            Config.moveTo(ForgotPassword.this, Login.class);
                            finishAffinity();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }
}


package co.sandyedemo.ecomdemo.Activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OptionalImageFullView extends AppCompatActivity {
    CustomPagerAdapter mCustomPagerAdapter;
    ViewPager mViewPager;
    public static int currentPos = 0;
    public static List<String> imagesList;
    ImageView imageView;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.imageText)
    TextView imageText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optional_image_full_view);
        ButterKnife.bind(this);
        mCustomPagerAdapter = new CustomPagerAdapter(this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);
        mViewPager.setCurrentItem(currentPos);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                imageText.setText("Image " + (position + 1) + " of " + imagesList.size());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        imageText.setText("Image " + (currentPos + 1) + " of " + imagesList.size());
    }


    class CustomPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;

        public CustomPagerAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return imagesList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            Picasso.with(mContext)
                    .load(imagesList.get(position))
                    .placeholder(R.drawable.defaultimage)
                    .resize(Integer.parseInt(getResources().getString(R.string.fullImageWidth)),Integer.parseInt(getResources().getString(R.string.fullImageWidth)))
                    .error(R.drawable.defaultimage)
                    .into(imageView);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

}


package co.sandyedemo.ecomdemo.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Common;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.Fragments.AppInfo;
import co.sandyedemo.ecomdemo.Fragments.FAQ;
import co.sandyedemo.ecomdemo.Fragments.Home;
import co.sandyedemo.ecomdemo.Fragments.MyCartList;
import co.sandyedemo.ecomdemo.Fragments.MyOrders;
import co.sandyedemo.ecomdemo.Fragments.MyProfile;
import co.sandyedemo.ecomdemo.Fragments.MyWishList;
import co.sandyedemo.ecomdemo.Fragments.ProductDetail;
import co.sandyedemo.ecomdemo.Fragments.SearchProducts;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class MainActivity extends AppCompatActivity {

    String from;
    public static LinearLayout toolbarContainer;
    public static View toolbar, searchLayout;
    @BindView(R.id.searchView)
    SearchView searchView;
    boolean doubleBackToExitPressedOnce = false;
    public static ImageView menu, back, cart, search;
    public static DrawerLayout drawerLayout;
    public static String currency, userId;
    @BindView(R.id.navigationView)
    NavigationView navigationView;
    public static TextView title, cartCount;
    int count;
    public static ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initViews();
        getCurrency();
        getUserId();
        Intent intent = getIntent();
        try {
            from = intent.getStringExtra("from");
            if (from.equalsIgnoreCase("signUp")) {
                Config.showCustomAlertDialog(MainActivity.this,
                        "Verification email sent successfully.",
                        "Please check your inbox and confirm your email address. The email may take upto 5 minutes to reach your inbox\n\nIf you didn't receive email from us, make sure to check your spam folder.",
                        SweetAlertDialog.WARNING_TYPE);
            }
        } catch (Exception e) {
            Log.e("errorOccur", "Error");
        }
        // customized searchView
        int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) searchView.findViewById(id);
        searchEditText.setTextSize(12);
        searchLayout = findViewById(R.id.searchLayout);
        toolbarContainer = (LinearLayout) findViewById(R.id.toolbar_container);
        toolbar = findViewById(R.id.toolbar);
        loadFragment(new Home(), false);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.myWishList:
                        loadFragment(new MyWishList(), true);

                        break;
                    case R.id.myCart:
                        loadFragment(new MyCartList(), true);
                        break;
                    case R.id.myOrders:
                        loadFragment(new MyOrders(), true);
                        break;
                    case R.id.myProfile:
                        loadFragment(new MyProfile(), true);
                        break;
                    case R.id.faq:
                        loadFragment(new FAQ(), true);
                        break;
                    case R.id.appInfo:
                        loadFragment(new AppInfo(), true);
                        break;
                    case R.id.share:
                        shareApp();
                        break;
                    case R.id.rateApp:
                        // perform click on Rate Item
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                        }
                        break;
                    case R.id.email:
                        openGmail();
                        break;
                    case R.id.call:
                        call();
                        break;
                    case R.id.Whatsapp:
                        whatsapp ();
                        break;
                }
                return false;
            }
        });
        displayFirebaseRegId(); // display firebase id

        if (getIntent().getBooleanExtra("isFromNotification", false)) {
            ProductDetail.productList.clear();
            ProductDetail.productList.addAll(SplashScreen.imagesList1);
            ProductDetail productDetail = new ProductDetail();
            Bundle bundle = new Bundle();
            bundle.putInt("position", 0);
            productDetail.setArguments(bundle);
            loadFragment(productDetail, true);
        }

    }

    private void call() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
       intent.setData(Uri.parse("tel:" + getResources().getString(R.string.contactNo)));
       startActivity(intent);
    }

    private void whatsapp()
    {
        /*Intent sendintent= new Intent();
        sendintent.setAction(Intent.ACTION_SEND);
        sendintent.setPackage("com.whatsapp");
        startActivity(sendintent);*/

        String url="https://wa.me/+918669067004";
        Intent i= new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);



    }

    private void openGmail()
    {
        // perform click on Email ID
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        String className = null;
        for (final ResolveInfo info : matches) {
            if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                className = info.activityInfo.name;

                if (className != null && !className.isEmpty()) {
                    break;
                }
            }
        }
        emailIntent.setData(Uri.parse("mailto:" + getResources().getString(R.string.emailId)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback Of " + getResources().getString(R.string.app_name));
        emailIntent.setClassName("com.google.android.gm", className);
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException ex) {
            // handle error
        }

    }

    public void shareApp() {
        // share app with your friends
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Try this " + getResources().getString(R.string.app_name) + " App: https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
        startActivity(Intent.createChooser(shareIntent, "Share Using"));
    }

    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        Log.e("FCM", "Firebase reg id: " + regId);
        if (!TextUtils.isEmpty(regId)) {
        } else
            Log.d("Firebase", "Firebase Reg Id is not received yet!");
    }

    private void getUserId() {
        if (Common.getSavedUserData(MainActivity.this, "userId").equalsIgnoreCase("")) {
            userId = "";
        } else {
            userId = Common.getSavedUserData(MainActivity.this, "userId");
            Log.d("userId", userId);
        }

    }

    private void getCurrency() {
        try {
            if (SplashScreen.allProductsData.get(0).getCurrency().equalsIgnoreCase("USD"))
                currency = "$";
            else
                currency = "₹";
        } catch (Exception e) {
        }
    }

    private void initViews() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        title = (TextView) findViewById(R.id.title);
        cartCount = (TextView) findViewById(R.id.cartCount);
        menu = (ImageView) findViewById(R.id.menu);
        cart = (ImageView) findViewById(R.id.cart);
        back = (ImageView) findViewById(R.id.back);
        search = (ImageView) findViewById(R.id.search);
    }

    @Override
    public void onBackPressed() {
        // double press to exit
        if (menu.getVisibility() == View.VISIBLE) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
        } else {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back once more to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    public void showToolbar() {
        toolbarContainer.clearAnimation();
        toolbarContainer
                .animate()
                .translationY(0)
                .start();

    }

    public void lockUnlockDrawer(int lockMode) {
        drawerLayout.setDrawerLockMode(lockMode);
        if (lockMode == DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
            menu.setVisibility(View.GONE);
            back.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.GONE);
            showToolbar();

        } else {
            menu.setVisibility(View.VISIBLE);
            back.setVisibility(View.GONE);
            searchLayout.setVisibility(View.VISIBLE);

        }

    }

    @OnClick({R.id.menu, R.id.back, R.id.cart, R.id.cartCount, R.id.searchTextView, R.id.search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu:
                if (!Home.swipeRefreshLayout.isRefreshing())
                    if (!MainActivity.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    MainActivity.drawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.back:
                if (!Home.swipeRefreshLayout.isRefreshing())
                    removeCurrentFragmentAndMoveBack();
                break;
            case R.id.searchTextView:
            case R.id.search:
                loadFragment(new SearchProducts(), true);
                break;
            case R.id.cart:
            case R.id.cartCount:
                if (!Home.swipeRefreshLayout.isRefreshing())
                    loadFragment(new MyCartList(), true);

                break;

        }
    }

    public void removeCurrentFragmentAndMoveBack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        /*FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.remove(fragment);
        trans.commit();*/
        fragmentManager.popBackStack();
    }

    public void loadFragment(Fragment fragment, Boolean bool) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        if (bool) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }


}


package co.sandyedemo.ecomdemo.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Common;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.SignUpResponse;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Login extends AppCompatActivity {


    @BindViews({R.id.email, R.id.password})
    List<EditText> editTexts;
    @BindView(R.id.loginLinearLayout)
    LinearLayout loginLinearLayout;
    @BindView(R.id.appIcon)
    ImageView appIcon;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loginLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);

            }
        });

    }
    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @OnClick({R.id.txtSignUp, R.id.txtForgotPassword, R.id.skipLoginLayout, R.id.signIn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtSignUp:
                Config.moveTo(Login.this, SignUp.class);
                break;
            case R.id.txtForgotPassword:
                Config.moveTo(Login.this, ForgotPassword.class);
                break;
            case R.id.skipLoginLayout:
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.putExtra("from", "skip");
                startActivity(intent);
                finishAffinity();
                break;

            case R.id.signIn:
                if (Config.validateEmail(editTexts.get(0),Login.this) && validatePassword(editTexts.get(1))) {
                    login();
                }
                break;
        }
    }

    private boolean validatePassword(EditText editText) {
        if (editText.getText().toString().trim().length() > 5) {
            return true;
        } else if (editText.getText().toString().trim().length() > 0) {
            editText.setError("Password must be of 6 characters");
            editText.requestFocus();
            return false;
        }
        editText.setError("Please Fill This");
        editText.requestFocus();
        return false;
    }

    private void login() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(Login.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        // sending gcm token to server
        Api.getClient().login(editTexts.get(0).getText().toString().trim(),
                editTexts.get(1).getText().toString().trim(),
                "email",
                new Callback<SignUpResponse>() {
                    @Override
                    public void success(SignUpResponse signUpResponse, Response response) {
                        pDialog.dismiss();
                        Log.d("signUpResponse", signUpResponse.getUserid() + "");
                        Toast.makeText(Login.this, signUpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if (signUpResponse.getSuccess().equalsIgnoreCase("true")) {
                            Common.saveUserData(Login.this, "email", editTexts.get(1).getText().toString());
                            Common.saveUserData(Login.this, "userId", signUpResponse.getUserid() + "");
                            Config.moveTo(Login.this, MainActivity.class);
                            finishAffinity();
                        } else if (signUpResponse.getSuccess().equalsIgnoreCase("notactive")) {
                            Config.moveTo(Login.this, AccountVerification.class);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }
}


package co.sandyedemo.ecomdemo.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.SignUpResponse;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AccountVerification extends AppCompatActivity {

    @BindView(R.id.resendEmail)
    Button resendEmail;
    @BindView(R.id.email)
    EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_verification);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.resendEmail, R.id.signUp,R.id.login, R.id.back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.resendEmail:
                if (Config.validateEmail(email,AccountVerification.this))
                resendEmail();
                break;
            case R.id.signUp:
                Config.moveTo(AccountVerification.this, SignUp.class);
                break;
            case R.id.login:
                Config.moveTo(AccountVerification.this, Login.class);
                break;
            case R.id.back:
                finish();
                break;
        }
    }

    private void resendEmail() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(AccountVerification.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        // sending gcm token to server
        Api.getClient().resentEmail(email.getText().toString().trim(),
                new Callback<SignUpResponse>() {
                    @Override
                    public void success(SignUpResponse signUpResponse, Response response) {
                        pDialog.dismiss();
                        Log.d("resendEmailResponse", signUpResponse.getSuccess() + "");
                        Toast.makeText(AccountVerification.this, signUpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if (signUpResponse.getSuccess().equalsIgnoreCase("true")) {
                            finish();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }
}


package co.sandyedemo.ecomdemo.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import co.sandyedemo.ecomdemo.Common;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.MVP.SignUpResponse;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import co.sandyedemo.ecomdemo.R;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SignUp extends AppCompatActivity {

    @BindViews({R.id.username, R.id.email, R.id.password, R.id.confirmPassword})
    List<EditText> editTexts;

    @BindView(R.id.loginLinearLayout)
    LinearLayout loginLinearLayout;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        loginLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(view);

            }
        });
    }
    protected void hideKeyboard(View view)
    {
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @OnClick({R.id.txtSignIn, R.id.signUp, R.id.back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtSignIn:
                Config.moveTo(SignUp.this, Login.class);
                finishAffinity();
                break;
            case R.id.back:
                Config.moveTo(SignUp.this, Login.class);
                finishAffinity();
                break;
            case R.id.signUp:
                if (validate(editTexts.get(0)) && Config.validateEmail(editTexts.get(1),SignUp.this) && validatePassword(editTexts.get(2)) &&
                        validatePassword(editTexts.get(3))) {
                    if (editTexts.get(2).getText().toString().trim().equals(editTexts.get(3).getText().toString().trim())) {
                        signUp();
                    } else {
                        Toast.makeText(SignUp.this, "Password and Confirm Password should be same", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
    }

    private boolean validate(EditText editText) {
        if (editText.getText().toString().trim().length() > 0) {
            return true;
        }
        editText.setError("Please Fill This");
        editText.requestFocus();
        return false;
    }

    private boolean validatePassword(EditText editText) {
        if (editText.getText().toString().trim().length() > 5) {
            return true;
        } else if (editText.getText().toString().trim().length() > 0) {
            editText.setError("Password must be of 6 characters");
            editText.requestFocus();
            return false;
        }
        editText.setError("Please Fill This");
        editText.requestFocus();
        return false;
    }


    private void signUp() {
        final SweetAlertDialog pDialog = new SweetAlertDialog(SignUp.this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        pDialog.show();
        // sending gcm token to server
        Api.getClient().registration(editTexts.get(0).getText().toString().trim(),
                editTexts.get(1).getText().toString().trim(),
                editTexts.get(2).getText().toString().trim(),
                "email",
                new Callback<SignUpResponse>() {
                    @Override
                    public void success(SignUpResponse signUpResponse, Response response) {
                        pDialog.dismiss();
//                        Log.d("signUpResponse", signUpResponse.getMessage());
                        Toast.makeText(SignUp.this, signUpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        if (signUpResponse.getSuccess().equalsIgnoreCase("true")) {

                            Common.saveUserData(SignUp.this, "email", editTexts.get(1).getText().toString());
                            Common.saveUserData(SignUp.this, "userId", signUpResponse.getUserid() + "");
                            Intent intent = new Intent(SignUp.this, MainActivity.class);
                            intent.putExtra("from", "signUp");
                            startActivity(intent);
//                            Config.moveTo(SignUp.this, MainActivity.class);
                            finishAffinity();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        pDialog.dismiss();

                        Log.e("error", error.toString());
                    }
                });
    }


}


package co.sandyedemo.ecomdemo.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.sandyedemo.ecomdemo.Common;
import co.sandyedemo.ecomdemo.Config;
import co.sandyedemo.ecomdemo.DetectConnection;
import co.sandyedemo.ecomdemo.MVP.CategoryListResponse;
import co.sandyedemo.ecomdemo.MVP.Product;
import co.sandyedemo.ecomdemo.MVP.SliderListResponse;
import co.sandyedemo.ecomdemo.R;
import co.sandyedemo.ecomdemo.Retrofit.Api;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SplashScreen extends Activity {

    public static List<CategoryListResponse> categoryListResponseData;
    public static List<SliderListResponse> sliderListResponsesData;
    public static List<Product> allProductsData;
    public static List<Product> imagesList1;
    String id = "";
    @BindView(R.id.errorText)
    TextView errorText;
    @BindView(R.id.internetNotAvailable)
    LinearLayout internetNotAvailable;
    @BindView(R.id.splashImage)
    ImageView splashImage;
    SharedPreferences sharedPreference, sharedPreferencesCache;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreferencesCache = getSharedPreferences("cacheExist", 0);

        // check data from FCM
        try {
            Intent intent = getIntent();
            id = intent.getStringExtra("id");
            Log.d("notification Data", id);
        } catch (Exception e) {
            Log.d("error notification data", e.toString());
        }
        sharedPreference = getSharedPreferences("localData", 0);
        editor = sharedPreference.edit();
        // Check the internet and get response from API's
        if (DetectConnection.checkInternetConnection(getApplicationContext())) {
            getCategoryList();
        } else {
            errorText.setText("Internet Connection Not Available");
            internetNotAvailable.setVisibility(View.VISIBLE);
            splashImage.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.tryAgain)
    public void onClick() {
        if (DetectConnection.checkInternetConnection(getApplicationContext())) {
            internetNotAvailable.setVisibility(View.GONE);
            splashImage.setVisibility(View.VISIBLE);
            getCategoryList();
        } else {
            errorText.setText("Internet Connection Not Available");
            internetNotAvailable.setVisibility(View.VISIBLE);
            splashImage.setVisibility(View.GONE);
        }
    }

    public void getCategoryList() {
        // getting category list news data
        Api.getClient().getCategoryList(new Callback<List<CategoryListResponse>>() {
            @Override
            public void success(List<CategoryListResponse> categoryListResponses, Response response) {
                try {
                    categoryListResponseData = categoryListResponses;
                    Log.d("categoryData", categoryListResponses.get(0).getCategory_name());
                    Gson gson = new Gson();
                    String json = gson.toJson(categoryListResponseData);
                    editor.putString("categoryList", json);
                    editor.commit();
                    getSliderList();
                } catch (Exception e) {
                    errorText.setText("No Category Added In This Store!");
                    internetNotAvailable.setVisibility(View.VISIBLE);
                    splashImage.setVisibility(View.GONE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
                errorText.setText("Internet Connection Not Available");
                internetNotAvailable.setVisibility(View.VISIBLE);
                splashImage.setVisibility(View.GONE);
            }
        });
    }

    public void getSliderList() {
        // getting slider list data
        Api.getClient().getSliderList(new Callback<List<SliderListResponse>>() {
            @Override
            public void success(List<SliderListResponse> sliderListResponses, Response response) {
                sliderListResponsesData = sliderListResponses;
                getAllProducts();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
                errorText.setText("Internet Connection Not Available");
                internetNotAvailable.setVisibility(View.VISIBLE);
                splashImage.setVisibility(View.GONE);
            }
        });
    }

    public void getAllProducts() {
        // getting news list data
        Api.getClient().getAllProducts(new Callback<List<Product>>() {
            @Override
            public void success(List<Product> allProducts, Response response) {
                try {
                    allProductsData = allProducts;
                    Log.d("allProductsData", allProducts.get(0).getProductName());
                    Gson gson = new Gson();
                    String json = gson.toJson(allProductsData);
                    editor.putString("newslist", json);
                    editor.commit();
                    moveNext();
                } catch (Exception e) {
                    errorText.setText("No Product Added In This Store!");
                    internetNotAvailable.setVisibility(View.VISIBLE);
                    splashImage.setVisibility(View.GONE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("error", error.toString());
                errorText.setText("Internet Connection Not Available");
                internetNotAvailable.setVisibility(View.VISIBLE);
                splashImage.setVisibility(View.GONE);
            }
        });
    }

    private void moveNext() {
// redirect to next page after getting data from server

        boolean isFromNotification;
        try {
            imagesList1 = new ArrayList<>();
            if (id.length() > 0) {
                for (int j = 0; j < allProductsData.size(); j++) {
                    if (allProductsData.get(j).getProductId().trim().equalsIgnoreCase(id)) {
                        imagesList1.add(allProductsData.get(j));
                    }
                }

                isFromNotification = true;
            } else {
                isFromNotification = false;
            }
        } catch (Exception e) {
            Log.d("error notification data", e.toString());
            isFromNotification = false;
        }
        if (Common.getSavedUserData(SplashScreen.this, "email").equalsIgnoreCase("")&&!isFromNotification) {
            Config.moveTo(SplashScreen.this, Login.class);
            finishAffinity();
        } else {
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            intent.putExtra("isFromNotification", isFromNotification);
            startActivity(intent);
            finishAffinity();
        }

    }

}


