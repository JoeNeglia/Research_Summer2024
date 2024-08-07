package com.project.laundryappui;

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
        assertEquals("com.project.laundryappui", appContext.getPackageName());
    }
}

package com.project.laundryappui;

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

package com.project.laundryappui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.project.laundryappui.menu.home.HomeFragment;
import com.project.laundryappui.menu.message.MessageFragment;
import com.project.laundryappui.menu.notification.NotificationFragment;
import com.project.laundryappui.menu.search.SearchFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();
        initViews();
        initComponentsNavHeader();
        loadFragment(new HomeFragment());
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(0);
    }

    private void initViews() {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        /**
         * Menu Navigation Drawer
         **/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setToolbarNavigationClickListener(view -> drawer.openDrawer(GravityCompat.START));
        toggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
        toggle.syncState();
    }

    /**
     * Fragment
     **/
    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    /**
     * Menu Bottom Navigation Drawer
     * */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.nav_menu_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_menu_search:
                fragment = new SearchFragment();
                break;
            case R.id.nav_menu_notification:
                fragment = new NotificationFragment();
                break;
            case R.id.nav_menu_message:
                fragment = new MessageFragment();
                break;
        }
        return loadFragment(fragment);
    }

    private void initComponentsNavHeader(){
        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setItemIconTintList(null); //disable tint on each icon to use color icon svg
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_my_account:
                        Pesan("My Account");
                        break;
                    case R.id.nav_support:
                        Pesan("Support");
                        break;
                    case R.id.nav_setting:
                        Pesan("Setting");
                        break;
                    case R.id.nav_help:
                        Pesan("Help");
                        break;
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            private void Pesan(String pesan) {
                Toast.makeText(MainActivity.this, pesan, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_profile) {
            Uri uri = Uri.parse("https://github.com/achmadqomarudin");
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, uri), "Choose Browser"));
        }
        return true;
    }
}

package com.project.laundryappui.menu.home;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.laundryappui.R;
import com.project.laundryappui.menu.home.adapter.HomeAdapter;
import com.project.laundryappui.menu.home.model.HomeModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private Context mContext;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private HomeAdapter homeAdapter;
    private List<HomeModel> homeModelList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAdapterType(view);
        setAdapter();
    }

    private void initData() {
        homeModelList = new ArrayList<>();

        homeModelList.add(new HomeModel(R.drawable.bg_post1, "Amanda Laundry", "$10-$20", "Distance 1.2 km" ));
        homeModelList.add(new HomeModel(R.drawable.bg_post2, "Papa Laundry", "$30-$40", "Distance 1.3 km" ));
        homeModelList.add(new HomeModel(R.drawable.bg_post3, "Mama Laundry", "$50-$60", "Distance 1.4 km" ));
    }

    private void setAdapterType(View view) {
        recyclerView    = view.findViewById(R.id.recyclerview_recommended);
        layoutManager   = new LinearLayoutManager(mContext);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(true);
    }

    private void setAdapter() {
        initData();

        homeAdapter = new HomeAdapter(homeModelList);
        recyclerView.setAdapter(homeAdapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}

package com.project.laundryappui.menu.home.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.project.laundryappui.R;
import com.project.laundryappui.menu.home.home_detail.HomeDetailActivity;
import com.project.laundryappui.menu.home.model.HomeModel;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    List<HomeModel> listHome;

    public HomeAdapter(List<HomeModel> listHome) {
        this.listHome = listHome;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeModel list = listHome.get(position);

        holder.imageRecommended.setBackgroundResource(list.getImage());
        holder.textName.setText(list.getName());
        holder.textPrice.setText(list.getPrice());
        holder.textLocation.setText(list.getLocation());
        holder.containerRecommended.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(new Intent(view.getContext(), HomeDetailActivity.class));
//                Toast.makeText(view.getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listHome.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView containerRecommended;
        ImageView imageRecommended;
        TextView textPrice, textName, textLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            containerRecommended = itemView.findViewById(R.id.container_recommended);
            imageRecommended     = itemView.findViewById(R.id.item_recommended_image);
            textName             = itemView.findViewById(R.id.item_recommended_name);
            textPrice            = itemView.findViewById(R.id.item_recommended_price);
            textLocation         = itemView.findViewById(R.id.item_recommended_location);
        }
    }
}


package com.project.laundryappui.menu.home.model;

public class HomeModel {
    private int image;
    private String name;
    private String price;
    private String location;

    public HomeModel() {}

    public HomeModel(int image, String name, String price, String location) {
        this.image = image;
        this.name = name;
        this.price = price;
        this.location = location;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}


package com.project.laundryappui.menu.home.home_detail;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.project.laundryappui.R;

public class HomeDetailActivity extends AppCompatActivity {
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar();
        setContentView(R.layout.activity_home_detail);

        buttonBack = findViewById(R.id.button_back);
        buttonBack.setOnClickListener(view -> onBackPressed());
    }

    @SuppressLint("ObsoleteSdkInt")
    private void hideStatusBar() {
        try {
            if (Build.VERSION.SDK_INT >= 19) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(3328);
            } else {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.project.laundryappui.menu.notification;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.laundryappui.R;
import com.project.laundryappui.menu.home.adapter.HomeAdapter;
import com.project.laundryappui.menu.home.model.HomeModel;
import com.project.laundryappui.menu.notification.adapter.NotificationAdapter;
import com.project.laundryappui.menu.notification.model.NotificationModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {
    private Context mContext;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationModelList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAdapterType(view);
        setAdapter();
    }

    private void initData() {
        notificationModelList = new ArrayList<>();

        notificationModelList.add(new NotificationModel(R.drawable.ic_box, "Order No : #73636265", "Order Confirmed" ));
        notificationModelList.add(new NotificationModel(R.drawable.ic_box, "Order No : #63231323", "Order Confirmed" ));
    }

    private void setAdapterType(View view) {
        recyclerView    = view.findViewById(R.id.recyclerview_order);
        layoutManager   = new LinearLayoutManager(mContext);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(true);
    }

    private void setAdapter() {
        initData();

        notificationAdapter = new NotificationAdapter(notificationModelList);
        recyclerView.setAdapter(notificationAdapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}

package com.project.laundryappui.menu.notification.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.project.laundryappui.R;
import com.project.laundryappui.menu.notification.model.NotificationModel;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    List<NotificationModel> listNotification;

    public NotificationAdapter(List<NotificationModel> listNotification) {
        this.listNotification = listNotification;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel list = listNotification.get(position);

        holder.imageOrder.setImageResource(list.getImage());
        holder.textName.setText(list.getName());
        holder.textStatusOrder.setText(list.getStatus_order());
        holder.containerRecommended.setOnClickListener(view -> {
//                view.getContext().startActivity(new Intent(view.getContext(), HomeDetailActivity.class));
            Toast.makeText(view.getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return listNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView containerRecommended;
        ImageView imageOrder;
        TextView textName, textStatusOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            containerRecommended = itemView.findViewById(R.id.container_order);
            imageOrder           = itemView.findViewById(R.id.item_order_image);
            textName             = itemView.findViewById(R.id.item_order_name);
            textStatusOrder      = itemView.findViewById(R.id.item_order_status_order);
        }
    }
}


package com.project.laundryappui.menu.notification.model;

public class NotificationModel {
    private int image;
    private String name;
    private String status_order;

    public NotificationModel(int image, String name, String status_order) {
        this.image = image;
        this.name = name;
        this.status_order = status_order;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus_order() {
        return status_order;
    }

    public void setStatus_order(String status_order) {
        this.status_order = status_order;
    }
}


package com.project.laundryappui.menu.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.project.laundryappui.R;

public class MessageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}

package com.project.laundryappui.menu.search;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.laundryappui.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private Context mContext;
    private RecyclerView recyclerView;
    private SearchAdapter homeAdapter;
    private List<SearchModel> searchModelList;
    public LinearLayoutManager layoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAdapterType(view);
        setAdapter();
    }

    private void initData() {
        searchModelList = new ArrayList<>();

        searchModelList.add(new SearchModel(R.drawable.bg_post1, "Adek Laundry \nCompany and Co.", "7.5", "Distance 1.5 km"));
        searchModelList.add(new SearchModel(R.drawable.bg_post2, "Saudara Laundry \nCompany and Co.", "8.5", "Distance 2.5 km"));
        searchModelList.add(new SearchModel(R.drawable.bg_post3, "Ponakan Laundry \nCompany and Co.", "9.5", "Distance 3.5 km"));
    }

    private void setAdapterType(View view) {
        recyclerView = view.findViewById(R.id.recyclerview_maps);
        layoutManager = new LinearLayoutManager(mContext);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(true);
    }

    private void setAdapter() {
        initData();

        homeAdapter = new SearchAdapter(searchModelList);
        recyclerView.setAdapter(homeAdapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}

package com.project.laundryappui.menu.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.project.laundryappui.R;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    List<SearchModel> listSearch;

    public SearchAdapter(List<SearchModel> listSearch) {
        this.listSearch = listSearch;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maps, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchModel list = listSearch.get(position);

        holder.textName.setText(list.getName());
        holder.textScore.setText(list.getScore());
        holder.textLocation.setText(list.getLocation());
        holder.containerMaps.setOnClickListener(view -> Toast.makeText(view.getContext(), "Clicked!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return listSearch.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView containerMaps;
        TextView textScore, textName, textLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            containerMaps   = itemView.findViewById(R.id.container_recommended);
            textName        = itemView.findViewById(R.id.item_recommended_name);
            textScore       = itemView.findViewById(R.id.item_recommended_price);
            textLocation    = itemView.findViewById(R.id.item_recommended_location);
        }
    }
}


package com.project.laundryappui.menu.search;

public class SearchModel {
    private int image;
    private String name;
    private String score;
    private String location;

    public SearchModel(int image, String name, String score, String location) {
        this.image = image;
        this.name = name;
        this.score = score;
        this.location = location;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}


