package code.diegohdez.githubapijava;

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

        assertEquals("code.diegohdez.githubapijava", appContext.getPackageName());
    }
}


package code.diegohdez.githubapijava;

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

package code.diegohdez.navbottom.githubapijava.Activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import code.diegohdez.githubapijava.AsyncTask.DetailsRepo;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Constants.Intents;
import code.diegohdez.navbottom.githubapijava.Adapter.BranchesFragment;
import code.diegohdez.navbottom.githubapijava.Adapter.IssuesFragment;
import code.diegohdez.navbottom.githubapijava.Adapter.PullsFragment;
import io.realm.Realm;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.BRANCHES;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_ALL;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_ISSUES;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_PULLS;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;

public class ReposDetailsActivity extends AppCompatActivity {

    public static final String ARG_ID = "ID";
    private long repoId;
    public static final String ARG_REPO_NAME = "REPO_NAME";
    private String repoName;

    private AppManager appManager;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repos_details);

        Toolbar toolbar = findViewById(R.id.repo_details_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appManager = AppManager.getOurInstance();
        Bundle bundle = getIntent().getExtras();

        repoName = bundle.getString(Intents.REPO_NAME);
        repoId = bundle.getLong(Intents.REPO_ID);

        DetailsRepo details = new DetailsRepo(this, repoId);
        details.execute(BASE_URL + USER_REPOS + appManager.getAccount() + "/" + repoName + USER_PULLS + STATE_ALL,
                BASE_URL + USER_REPOS + appManager.getAccount() + "/" +repoName + USER_ISSUES + STATE_ALL,
                BASE_URL + USER_REPOS + appManager.getAccount() + "/" + repoName + BRANCHES);

        bottomNavigationView = findViewById(R.id.repo_details_nav_bottom);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Bundle args = new Bundle();
                args.putLong(ARG_ID, repoId);
                args.putString(ARG_REPO_NAME, repoName);
                Fragment fragment = null;
                switch (item.getItemId()) {
                    case R.id.issues_menu:
                        fragment = code.diegohdez.navbottom.githubapijava.Adapter.IssuesFragment.newInstance();
                        break;
                    case R.id.pulls_menu:
                        fragment = PullsFragment.newInstance();
                        break;
                    case R.id.branches_menu:
                        fragment = BranchesFragment.newInstance();
                        break;
                }
                if (fragment != null) {
                    fragment.setArguments(args);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frame_repo_details, fragment);
                    fragmentTransaction.commit();
                }
                return true;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void createFragment() {
        Bundle args = new Bundle();
        args.putLong(ARG_ID, repoId);
        args.putString(ARG_REPO_NAME, repoName);

        IssuesFragment initFragment = IssuesFragment.newInstance();
        initFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_repo_details, initFragment);
        fragmentTransaction.commit();
        ProgressBar initLoader = findViewById(R.id.issues_init_loader);
        initLoader.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Realm realm = Realm.getDefaultInstance();
        final Repo repo = realm.where(Repo.class).equalTo(Fields.ID, repoId).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                repo.getIssues().deleteAllFromRealm();
                repo.getPulls().deleteAllFromRealm();
                repo.getBranches().deleteAllFromRealm();
            }
        });
        realm.close();
        AppManager.getOurInstance().resetRepoDetailsPage();
    }
}


package code.diegohdez.navbottom.githubapijava.Adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Adapter.PullsAdapter;
import code.diegohdez.githubapijava.AsyncTask.PullsRepo;
import code.diegohdez.githubapijava.Data.DataOfPulls;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Pull;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.PullsPaginationScrollListener;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.AND_PAGE;
import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_ALL;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_CLOSED;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_OPEN;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_PULLS;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class PullsFragment extends Fragment {

    public static final String ARG_ID = "ID";
    public static final String ARG_REPO_NAME = "REPO_NAME";

    PullsFragment fragment;
    PullsAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<DataOfPulls> list = new ArrayList<>();
    Bundle args;
    int page = PAGE_ONE;
    boolean isLoading = false;
    boolean isLastPage = false;
    String state;
    CheckBox open;
    CheckBox closed;
    ProgressBar progressBar;

    public static PullsFragment newInstance() {
        return new PullsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.pulls_fragment, container, false);
        fragment = this;
        args = getArguments();
        state = STATE_ALL;
        open = root.findViewById(R.id.open);
        closed = root.findViewById(R.id.closed);
        progressBar = root.findViewById(R.id.loading_selected_checkbox_pulls);
        adapter = new PullsAdapter(this.list);
        recyclerView = root.findViewById(R.id.pulls_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new PullsPaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadPulls() {
                isLoading = true;
                page++;
                AppManager.getOurInstance().setPullsPage(page);
                Toast.makeText(
                        getActivity(),
                        "Display " + page + " page",
                        Toast.LENGTH_SHORT).show();
                PullsRepo issuesRepo = new PullsRepo(fragment, args.getLong(ARG_ID));
                issuesRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" +args.getString(ARG_REPO_NAME) + USER_PULLS + state + AND_PAGE + page);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.clear();
                AppManager.getOurInstance().setPullsPage(PAGE_ONE);
                page = 1;
                Realm realm = Realm.getDefaultInstance();
                final Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        repo.getPulls().deleteAllFromRealm();
                    }
                });
                if (isChecked) {
                    state = STATE_OPEN;
                    if (closed.isChecked()) state = STATE_ALL;
                } else {
                    if (closed.isChecked()) state = STATE_CLOSED;
                    else state = STATE_ALL;
                }
                PullsRepo pullsRepo = new PullsRepo(fragment, args.getLong(ARG_ID));
                pullsRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" +args.getString(ARG_REPO_NAME) + USER_PULLS + state + AND_PAGE + page);
                progressBar.setVisibility(View.VISIBLE);
                realm.close();
            }
        });
        closed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.clear();
                AppManager.getOurInstance().setPullsPage(PAGE_ONE);
                page = 1;
                Realm realm = Realm.getDefaultInstance();
                final Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        repo.getPulls().deleteAllFromRealm();
                    }
                });
                if (isChecked) {
                    state = STATE_CLOSED;
                    if (open.isChecked()) state = STATE_ALL;
                } else {
                    if (open.isChecked()) state = STATE_OPEN;
                    else state = STATE_ALL;
                }
                PullsRepo pullsRepo = new PullsRepo(fragment, args.getLong(ARG_ID));
                pullsRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" +args.getString(ARG_REPO_NAME) + USER_PULLS + state + AND_PAGE + page);
                progressBar.setVisibility(View.VISIBLE);
                realm.close();
            }
        });
        setPullsList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        page = AppManager.getOurInstance().getCurrentPullsPage();
    }

    public void setPullsList() {
        Realm realm = Realm.getDefaultInstance();
        Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
        RealmList<Pull> pulls = repo != null ? repo.getPulls() : new RealmList<Pull>();
        ArrayList<DataOfPulls> list = DataOfPulls.createList(pulls);
        this.adapter.addPulls(list);
        if (list.size() == PAGE_SIZE) adapter.addLoading();
        realm.close();
    }

    public void addPulls(RealmList<Pull> pulls) {
        progressBar.setVisibility(View.GONE);
        if (page > PAGE_ONE) {
            adapter.deleteLoading();
            isLoading = false;
        }
        ArrayList<DataOfPulls> list = DataOfPulls.createList(pulls);
        this.adapter.addPulls(list);
        if (list.size() < PAGE_SIZE) isLastPage = true;
        else adapter.addLoading();
    }
}


package code.diegohdez.navbottom.githubapijava.Adapter;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Adapter.BranchesAdapter;
import code.diegohdez.githubapijava.AsyncTask.BranchesRepo;
import code.diegohdez.githubapijava.Data.DataOfBranches;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Branch;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.BranchPaginationScrollListener;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.BRANCHES;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class BranchesFragment extends Fragment {

    public static final String ARG_ID = "ID";
    public static final String ARG_REPO_NAME = "REPO_NAME";

    BranchesFragment fragment;
    BranchesAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<DataOfBranches> list = new ArrayList<>();
    Bundle args;
    int page = PAGE_ONE;
    boolean isLoading = false;
    boolean isLastPage = false;

    public static BranchesFragment newInstance() {
        return new BranchesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.branches_fragment, container, false);
        fragment = this;
        args = getArguments();
        adapter = new BranchesAdapter(args.getLong(ARG_ID), args.getString(ARG_REPO_NAME), this);
        recyclerView = root.findViewById(R.id.branches_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new BranchPaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadBranches() {
                isLoading = true;
                page++;
                AppManager.getOurInstance().setBranchesPage(page);
                BranchesRepo branchesRepo = new BranchesRepo(fragment, args.getLong(ARG_ID));
                branchesRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" + args.getString(ARG_REPO_NAME) + BRANCHES + "&page=" + page);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        setBranchesList();
        return root;
    }

    public void setBranchesList(){
        Realm realm = Realm.getDefaultInstance();
        Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
        RealmList<Branch> branches = repo != null ? repo.getBranches() : new RealmList<Branch>();
        ArrayList<DataOfBranches> list = DataOfBranches.createList(branches);
        this.adapter.addBranches(list);
        realm.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        page = AppManager.getOurInstance().getCurrentBranchPage();
    }

    public void addBranches(RealmList<Branch> branches) {
        adapter.deleteLoading();
        isLoading = false;
        ArrayList<DataOfBranches> list = DataOfBranches.createList(branches);
        this.adapter.addBranches(list);
        if (list.size() < PAGE_SIZE) isLastPage = true;
        else adapter.addLoading();
    }
}


package code.diegohdez.navbottom.githubapijava.Adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Adapter.IssuesAdapter;
import code.diegohdez.githubapijava.AsyncTask.IssuesRepo;
import code.diegohdez.githubapijava.Data.DataOfIssues;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Issue;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.IssuesPaginationScrollListener;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.AND_PAGE;
import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_ALL;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_CLOSED;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_OPEN;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_ISSUES;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class IssuesFragment extends Fragment {

    public static final String TAG = IssuesFragment.class.getSimpleName();

    public static final String ARG_ID = "ID";
    public static final String ARG_REPO_NAME = "REPO_NAME";

    IssuesFragment fragment;
    IssuesAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<DataOfIssues> list = new ArrayList<>();
    Bundle args;
    int page = PAGE_ONE;
    boolean isLoading = false;
    boolean isLastPage = false;
    String state;
    CheckBox open;
    CheckBox closed;
    ProgressBar progressBar;

    public static IssuesFragment newInstance() {
        return new IssuesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.issues_fragment, container, false);
        fragment = this;
        args = getArguments();
        state = STATE_ALL;
        adapter = new IssuesAdapter(this.list);
        recyclerView = root.findViewById(R.id.issues_list);
        open = root.findViewById(R.id.open);
        closed = root.findViewById(R.id.closed);
        progressBar = root.findViewById(R.id.loading_selected_checkbox_issues);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new IssuesPaginationScrollListener(linearLayoutManager) {

            @Override
            protected void loadIssues() {
                isLoading = true;
                page++;
                IssuesRepo issuesRepo = new IssuesRepo(fragment, args.getLong(ARG_ID));
                Toast.makeText(
                        getActivity(),
                        "Display " + page + " page",
                        Toast.LENGTH_SHORT).show();
                issuesRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" + args.getString(ARG_REPO_NAME) + USER_ISSUES + state + AND_PAGE + page);
                AppManager.getOurInstance().setIssuesPage(page);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.clear();
                AppManager.getOurInstance().setIssuesPage(PAGE_ONE);
                page = 1;
                Realm realm = Realm.getDefaultInstance();
                final Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        repo.getIssues().deleteAllFromRealm();
                    }
                });
                if (isChecked) {
                    state = STATE_OPEN;
                    if (closed.isChecked()) state = STATE_ALL;
                } else {
                    if (closed.isChecked()) state = STATE_CLOSED;
                    else state = STATE_ALL;
                }
                IssuesRepo issuesRepo = new IssuesRepo(fragment, args.getLong(ARG_ID));
                issuesRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" + args.getString(ARG_REPO_NAME) + USER_ISSUES + state + AND_PAGE + page);
                progressBar.setVisibility(View.VISIBLE);
                realm.close();
            }
        });
        closed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adapter.clear();
                AppManager.getOurInstance().setIssuesPage(PAGE_ONE);
                page = 1;
                Realm realm = Realm.getDefaultInstance();
                final Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        repo.getIssues().deleteAllFromRealm();
                    }
                });
                if (isChecked) {
                    state = STATE_CLOSED;
                    if (open.isChecked()) state = STATE_ALL;
                } else {
                    if (open.isChecked()) state = STATE_OPEN;
                    else state = STATE_ALL;
                }
                IssuesRepo issuesRepo = new IssuesRepo(fragment, args.getLong(ARG_ID));
                issuesRepo.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" + args.getString(ARG_REPO_NAME) + USER_ISSUES + state + AND_PAGE + page);
                progressBar.setVisibility(View.VISIBLE);
                realm.close();
            }
        });
        setIssuesList();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        page = AppManager.getOurInstance().getCurrentIssuesPage();
    }

    public void setIssuesList() {
        Realm realm = Realm.getDefaultInstance();
        Repo repo = realm.where(Repo.class).equalTo(Fields.ID, args.getLong(ARG_ID)).findFirst();
        RealmList<Issue> issues = repo != null ? repo.getIssues() : new RealmList<Issue>();
        ArrayList<DataOfIssues> list = DataOfIssues.createList(issues);
        this.adapter.addIssues(list);
        if (list.size() == PAGE_SIZE) adapter.addLoading();
        realm.close();
    }

    public void addIssues(RealmList<Issue> issues) {
        progressBar.setVisibility(View.GONE);
        if (page > PAGE_ONE) {
            adapter.deleteLoading();
            isLoading = false;
        }
        ArrayList<DataOfIssues> list = DataOfIssues.createList(issues);
        this.adapter.addIssues(list);
        if (list.size() < PAGE_SIZE) isLastPage = true;
        else adapter.addLoading();
    }
}


package code.diegohdez.githubapijava;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Migration.Migration;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;

public class GithubApi extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        OkHttpClient okHttpClient = new OkHttpClient() .newBuilder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        AndroidNetworking.initialize(getApplicationContext(), okHttpClient);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        AndroidNetworking.setParserFactory(new GsonParserFactory(gson));
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration
                .Builder()
                .name("github-api")
                .schemaVersion(11)
                .migration(new Migration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        AppManager.init(getApplicationContext());
    }
}


package code.diegohdez.githubapijava.Activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Adapter.PageRepoAdapter;
import code.diegohdez.githubapijava.AsyncTask.DetailsRepo;
import code.diegohdez.githubapijava.Data.DataOfBranches;
import code.diegohdez.githubapijava.Data.DataOfIssues;
import code.diegohdez.githubapijava.Data.DataOfPulls;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Issue;
import code.diegohdez.githubapijava.Model.Pull;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Constants.Intents;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.BRANCHES;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_ALL;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_ISSUES;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_PULLS;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;

public class ReposDetailActivity extends AppCompatActivity {

    private static final String TAG = ReposDetailActivity.class.getSimpleName();

    private Realm realm;
    private AppManager appManager;

    private long repoId;

    PageRepoAdapter pageRepoAdapter;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        setContentView(R.layout.activity_repos_detail);
        realm = Realm.getDefaultInstance();
        appManager = AppManager.getOurInstance();

        Bundle bundle = getIntent().getExtras();
        String repoName = bundle.getString(Intents.REPO_NAME);
        repoId = bundle.getLong(Intents.REPO_ID);
        DetailsRepo details = new DetailsRepo(this, repoId);
        details.execute(BASE_URL + USER_REPOS + appManager.getAccount() + "/" + repoName + USER_PULLS + STATE_ALL,
                BASE_URL + USER_REPOS + appManager.getAccount() + "/" +repoName + USER_ISSUES + STATE_ALL,
                BASE_URL + USER_REPOS + appManager.getAccount() + "/" + repoName + BRANCHES);
        pageRepoAdapter = new PageRepoAdapter(getSupportFragmentManager());
        pageRepoAdapter.setId(repoId);
        pageRepoAdapter.setRepoName(repoName);
        viewPager = findViewById(R.id.repo_pager_details);
        viewPager.setAdapter(pageRepoAdapter);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        android.support.v7.app.ActionBar.TabListener tabListener = new android.support.v7.app.ActionBar.TabListener() {
            @Override
            public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

            }

            @Override
            public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

            }
        };

        actionBar.addTab(actionBar.newTab()
        .setText("Issues")
        .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab()
        .setText("Pull Request")
        .setTabListener(tabListener));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final Repo repo = realm.where(Repo.class).equalTo(Fields.ID, repoId).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                repo.getIssues().deleteAllFromRealm();
                repo.getPulls().deleteAllFromRealm();
                repo.getBranches().deleteAllFromRealm();
            }
        });
        realm.close();
    }

    public void createAdapter(long id) {
        Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
        ArrayList<DataOfIssues> issues = DataOfIssues.createList(repo.getIssues());
        ArrayList<DataOfPulls> pulls = DataOfPulls.createList(repo.getPulls());
        ArrayList<DataOfBranches> branches = DataOfBranches.createList(repo.getBranches());
        pageRepoAdapter.setData(issues, pulls, branches);
    }

    public void addIssues(RealmList<Issue> issues) {
        ArrayList<DataOfIssues> list = DataOfIssues.createList(issues);
        pageRepoAdapter.setIssues(list);
    }

    public void addPulls(RealmList<Pull> pulls) {
        ArrayList<DataOfPulls> list= DataOfPulls.createList(pulls);
        pageRepoAdapter.setPulls(list);
    }
}


package code.diegohdez.githubapijava.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import code.diegohdez.githubapijava.AsyncTask.Repos;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.R;
import io.realm.Realm;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.USERS;
import static code.diegohdez.githubapijava.Utils.Constants.API.getRepos;
import static code.diegohdez.githubapijava.Utils.Constants.Result.RESULT_MAIN_GET_TOKEN;
import static code.diegohdez.githubapijava.Utils.Constants.Result.RESULT_OK_GET_TOKEN;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText account;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        account = findViewById(R.id.account);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((AppManager.getOurInstance().getToken().length() > 0)) {
            Toast.makeText(
                    getApplicationContext(),
                    "Session Initialized",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void getAccount(View view) {
        final String username = account.getText().toString().trim();
        if (username.length() > 0) {
            AppManager appManager = AppManager.getOurInstance();
            appManager.setAccount(username);
            Repos asyncRepos = new Repos(MainActivity.this);
            asyncRepos.execute(getRepos(username), BASE_URL + USERS + username);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Provide a github account",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (AppManager.getOurInstance().getToken().length() > 0) menu.findItem(R.id.login_main).setVisible(false);
        else menu.findItem(R.id.logout_main).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login_main:
                Intent intent = new Intent(getApplicationContext(), GetTokenActivity.class);
                startActivityForResult(intent, RESULT_MAIN_GET_TOKEN);
                return true;
            case R.id.logout_main:
                menu.clear();
                AppManager.getOurInstance().logout();
                Realm.deleteRealm(Realm.getDefaultConfiguration());
                Intent intentToLogout = new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_NO_ANIMATION |
                                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intentToLogout);
                finish();
                return true;
                default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK_GET_TOKEN:
                menu.clear();
                onCreateOptionsMenu(this.menu);
                Toast.makeText(getApplicationContext(),
                        "Get a token",
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static void successRepos(Context context, String message, int status) {
        Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT)
                .show();
        if (status == 200) {
            Intent intent = new Intent(context, ReposActivity.class);
            context.startActivity(intent);
        }
    }
}


package code.diegohdez.githubapijava.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Adapter.CommitsAdapter;
import code.diegohdez.githubapijava.AsyncTask.CommitsBranch;
import code.diegohdez.githubapijava.Data.DataOfCommits;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Branch;
import code.diegohdez.githubapijava.Model.Commit;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.BranchPaginationScrollListener;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Constants.Intents;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.COMMITS;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class CommitsActivity extends AppCompatActivity {

    private Long id;
    private String repoName;
    private String branchName;
    private CommitsAdapter adapter;
    private int page = PAGE_ONE;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private CommitsActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commits);
        context = this;
        Bundle bundle = getIntent().getExtras();
        id = bundle.getLong(Intents.REPO_ID);
        repoName = bundle.getString(Intents.REPO_NAME);
        branchName = bundle.getString(Intents.BRANCH_NAME);
        CommitsBranch commitsBranch = new CommitsBranch(this, id, branchName);
        commitsBranch.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" + repoName + COMMITS + "?sha=" + branchName);
        adapter = new CommitsAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        RecyclerView recyclerView = findViewById(R.id.commits_list);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new BranchPaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadBranches() {
                isLoading = true;
                page++;
                CommitsBranch commitsBranch = new CommitsBranch(context, id, branchName, page);
                commitsBranch.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() + "/" + repoName + COMMITS + "?sha=" + branchName);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }


    public void setCommits(RealmList<Commit> list) {
        ArrayList<DataOfCommits> commits = DataOfCommits.createList(list);
        adapter.addCommits(commits);
        adapter.addLoading();
    }

    public void addCommits(RealmList<Commit> list) {
        adapter.deleteLoading();
        isLoading = false;
        ArrayList<DataOfCommits> commits = DataOfCommits.createList(list);
        this.adapter.addCommits(commits);
        if (list.size() < PAGE_SIZE) isLastPage = true;
        else adapter.addLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Realm realm = Realm.getDefaultInstance();
        final Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Branch branch = repo.getBranches().where().equalTo(Fields.BRANCH_NAME, branchName).findFirst();
                branch.getCommits().deleteAllFromRealm();
            }
        });
    }
}


package code.diegohdez.githubapijava.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import code.diegohdez.githubapijava.Adapter.ReposAdapter;
import code.diegohdez.githubapijava.AsyncTask.ForkRepo;
import code.diegohdez.githubapijava.AsyncTask.RepoInfo;
import code.diegohdez.githubapijava.AsyncTask.Repos;
import code.diegohdez.githubapijava.AsyncTask.SearchRepo;
import code.diegohdez.githubapijava.AsyncTask.StarRepo;
import code.diegohdez.githubapijava.AsyncTask.WatchRepo;
import code.diegohdez.githubapijava.Data.DataOfRepos;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Owner;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.ReposPaginationScrollListener;
import code.diegohdez.githubapijava.Utils.Constants.API;
import code.diegohdez.githubapijava.Utils.Constants.Dialog;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.COLON;
import static code.diegohdez.githubapijava.Utils.Constants.API.FORK_REPO;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.PLUS;
import static code.diegohdez.githubapijava.Utils.Constants.API.QUERY;
import static code.diegohdez.githubapijava.Utils.Constants.API.REPOSITORIES;
import static code.diegohdez.githubapijava.Utils.Constants.API.SEARCH;
import static code.diegohdez.githubapijava.Utils.Constants.API.STAR;
import static code.diegohdez.githubapijava.Utils.Constants.API.STAR_REPO;
import static code.diegohdez.githubapijava.Utils.Constants.API.UN_STAR;
import static code.diegohdez.githubapijava.Utils.Constants.API.UN_WATCH;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.API.WATCH;
import static code.diegohdez.githubapijava.Utils.Constants.API.WATCH_REPO;
import static code.diegohdez.githubapijava.Utils.Constants.Fields.LOGIN;
import static code.diegohdez.githubapijava.Utils.Constants.Fields.OWNER_LOGIN;
import static code.diegohdez.githubapijava.Utils.Constants.Fields.REPO_NAME;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;
import static code.diegohdez.githubapijava.Utils.Constants.Result.RESULT_MAIN_GET_TOKEN;
import static code.diegohdez.githubapijava.Utils.Constants.Result.RESULT_OK_GET_TOKEN;

public class ReposActivity extends AppCompatActivity {

    private static final String TAG = ReposActivity.class.getSimpleName();
    private Realm realm;
    private String account;
    ReposAdapter adapter;
    private Menu menu;

    AlertDialog dialog;
    AlertDialog.Builder builder;
    LayoutInflater inflaterRepoDetails;
    View viewRepoDetailsModal;

    private boolean isWatched = false;
    private boolean isStarred = false;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int page = PAGE_ONE;
    private int TOTAL_PAGES = PAGE_ONE;

    private long total_repos = 0;
    private ProgressBar loadReposBar;
    private boolean searchRepos = false;
    private boolean searchInAccount = false;
    private String queryTextEdit = "";

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repos);
        Toolbar toolbar = findViewById(R.id.repos_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        builder = new AlertDialog.Builder(this);
        inflaterRepoDetails = this.getLayoutInflater();
        viewRepoDetailsModal = inflaterRepoDetails.inflate(R.layout.repo_details_modal, null);
        builder.setView(viewRepoDetailsModal)
            .setPositiveButton(Dialog.CLOSE_REPO_DETAIL_DIALOG, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    isWatched = false;
                    isStarred = false;
                }
            });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isWatched = false;
                isStarred = false;
            }
        });
        dialog = builder.create();
        realm = Realm.getDefaultInstance();
        AppManager appManager = AppManager.getOurInstance();
        account = appManager.getAccount();
        Owner owner = realm.where(Owner.class).equalTo(LOGIN, account).findFirst();
        if (owner != null) {
            TOTAL_PAGES = (owner.getRepos() > 0) ? (int) Math.ceil((double) owner.getRepos() / PAGE_SIZE) : 0;
            total_repos = owner.getRepos();
        }
        loadReposBar = findViewById(R.id.loading_repos);
        RecyclerView recyclerView = findViewById(R.id.reposList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RealmResults<Repo> repos = realm.where(Repo.class).equalTo(OWNER_LOGIN, account).findAll();
        ArrayList<DataOfRepos> list = DataOfRepos.createRepoList(repos);
        adapter = new ReposAdapter(list, account, this);
        if (list.size() == PAGE_SIZE) adapter.addLoading();
        recyclerView.setAdapter(adapter);
        loadReposBar.setVisibility(View.GONE);
        recyclerView.addOnScrollListener(new ReposPaginationScrollListener(layoutManager) {
            @Override
            protected void loadRepos() {
                isLoading = true;
                page++;
                AppManager.getOurInstance().setCurrentPage(page);
                Toast.makeText(
                        getApplicationContext(),
                        "Get repos for page: " + page,
                        Toast.LENGTH_SHORT).show();
                if (searchRepos) {
                    SearchRepo searchRepo = new SearchRepo(ReposActivity.this, page);
                    searchRepo.execute(BASE_URL + SEARCH + REPOSITORIES + QUERY + queryTextEdit + PLUS + USER + COLON + account);
                } else {
                    Repos repos = new Repos(ReposActivity.this, page);
                    repos.execute(API.getRepos(account));
                }
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getOurInstance().initPager();
        if (realm.isClosed()) realm = Realm.getDefaultInstance();
        final RealmResults<Repo> result = realm.where(Repo.class).equalTo(OWNER_LOGIN, account).findAll();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                result.deleteAllFromRealm();
            }
        });
        AppManager.getOurInstance().resetAccount();
        realm.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        realm.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (realm.isClosed()) realm = Realm.getDefaultInstance();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.repos_menu, menu);

        MenuItem menuItemSearch = menu.findItem(R.id.search_repos);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItemSearch);
        searchView.setQueryHint("Search in " + account);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!searchRepos) {
                    loadReposBar.setVisibility(View.VISIBLE);
                    searchInAccount = true;
                    searchRepos = true;
                    queryTextEdit = query;
                    if (realm.isClosed()) realm = Realm.getDefaultInstance();
                    final RealmResults<Repo> result = realm.where(Repo.class).equalTo(OWNER_LOGIN, account).findAll();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            result.deleteAllFromRealm();
                        }
                    });
                    isLoading = false;
                    isLastPage = false;
                    page = 1;
                    AppManager.getOurInstance().setCurrentPage(page);
                    adapter.clear();
                    SearchRepo searchRepo = new SearchRepo(ReposActivity.this);
                    searchRepo.execute(BASE_URL + SEARCH + REPOSITORIES + QUERY + queryTextEdit + PLUS + USER + COLON + account);
                    realm.close();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (searchRepos && !text.equals(queryTextEdit)) {
                    searchRepos = false;
                }
                if (searchInAccount && text.length() == 0) {
                    loadReposBar.setVisibility(View.VISIBLE);
                    searchRepos = false;
                    if (realm.isClosed()) realm = Realm.getDefaultInstance();
                    final RealmResults<Repo> result = realm.where(Repo.class).equalTo(OWNER_LOGIN, account).findAll();
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            result.deleteAllFromRealm();
                        }
                    });
                    isLoading = false;
                    isLastPage = false;
                    page = 1;
                    AppManager.getOurInstance().setCurrentPage(page);
                    adapter.clear();
                    Repos repos = new Repos(ReposActivity.this, page);
                    repos.execute(API.getRepos(account));
                    return false;
                }
                return false;
            }
        });
        if (AppManager.getOurInstance().getToken().length() > 0) menu.findItem(R.id.login_from_repos).setVisible(false);
        else menu.findItem(R.id.logout_from_repos).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_from_repos:
                AppManager.getOurInstance().logout();
                realm.close();
                Realm.deleteRealm(Realm.getDefaultConfiguration());
                Intent intentToLogout = new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_NO_ANIMATION |
                                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intentToLogout);
                finish();
                return true;
            case R.id.login_from_repos:
                Intent intentToLogin = new Intent(getApplicationContext(), GetTokenActivity.class);
                startActivityForResult(intentToLogin, RESULT_MAIN_GET_TOKEN);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK_GET_TOKEN:
                Toast.makeText(getApplicationContext(),
                        "Get a token",
                        Toast.LENGTH_SHORT).show();
                menu.clear();
                onCreateOptionsMenu(menu);
                break;
        }
    }

    public void successLoader(List<Repo> list) {
        adapter.deleteLoading();
        isLoading = false;
        List<DataOfRepos> repos = DataOfRepos.createRepoList(list);
        adapter.addAll(repos);
        if (page <= TOTAL_PAGES) adapter.addLoading();
        else isLastPage = true;
    }

    public void setRepoStatus(String name, long watchers, long stars, long forks) {
        final String repoName = name;
        TextView watchTextView = viewRepoDetailsModal.findViewById(R.id.watches);
        watchTextView.setText(Long.toString(watchers));
        ImageView watchRepo = viewRepoDetailsModal.findViewById(R.id.watch);
        watchRepo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                WatchRepo watchRepo = new WatchRepo(isWatched, ReposActivity.this, repoName);
                watchRepo.execute(BASE_URL + USER_REPOS + account + "/" + repoName + WATCH_REPO);
            }
        });
        ImageView starRepo = viewRepoDetailsModal.findViewById(R.id.star);
        starRepo.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                StarRepo starRepo = new StarRepo(isStarred, ReposActivity.this, repoName);
                starRepo.execute(BASE_URL + USER + STAR_REPO + "/" + account + "/" + repoName);
            }
        });
        ImageView forkRepo = viewRepoDetailsModal.findViewById(R.id.fork);
        forkRepo.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForkRepo forkRepo = new ForkRepo(ReposActivity.this, repoName);
                forkRepo.execute(BASE_URL + USER_REPOS + account + "/" + repoName + FORK_REPO);
            }
        });
        TextView starsTextView = viewRepoDetailsModal.findViewById(R.id.stars);
        starsTextView.setText(Long.toString(stars));
        TextView forksTextView = viewRepoDetailsModal.findViewById(R.id.forks);
        forksTextView.setText(Long.toString(forks));
        dialog.show();

    }

    public void isSubscribed(boolean subscribed) {
        ImageView watchRepo = viewRepoDetailsModal.findViewById(R.id.watch);
        TextView watchTextView = viewRepoDetailsModal.findViewById(R.id.watched_text);
        if (subscribed) {
            watchRepo.setImageResource(R.mipmap.baseline_visibility_off_black_48);
            watchTextView.setText(UN_WATCH);
            isWatched = true;
        } else {
            watchRepo.setImageResource(R.mipmap.baseline_visibility_black_48);
            watchTextView.setText(WATCH);
            isWatched = false;
        }
    }

    public void updateRepoAfterWatched(boolean isWatched, String name, String message) {
        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT).show();
        RepoInfo updateInfo = new RepoInfo(ReposActivity.this);
        updateInfo.execute(BASE_URL + USER_REPOS + account + "/" + name);
        isSubscribed(isWatched);
    }

    public void updateRepoAfterStarred(boolean isStarred, String name, String message) {
        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT).show();
        RepoInfo updateInfo = new RepoInfo(ReposActivity.this);
        updateInfo.execute(BASE_URL + USER_REPOS + account + "/" +  name);
        isStarred(isStarred);
    }

    public void isStarred(boolean isStarred) {
        ImageView starRepo = viewRepoDetailsModal.findViewById(R.id.star);
        TextView starred = viewRepoDetailsModal.findViewById(R.id.star_text);
        if (isStarred) {
            starRepo.setImageResource(R.mipmap.baseline_star_black_48);
            starred.setText(UN_STAR);
            this.isStarred = true;
        } else {
            starRepo.setImageResource(R.mipmap.baseline_star_border_black_48);
            starred.setText(STAR);
            this.isStarred = false;
        }
    }

    public void displayMessage(String message, String name) {
        Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_SHORT).show();
        RepoInfo updateInfo = new RepoInfo(ReposActivity.this);
        updateInfo.execute(BASE_URL + USER_REPOS + account + "/" +  name);
    }

    public void updateCounter(String name) {
        Repo repo = realm.where(Repo.class).equalTo(REPO_NAME, name).findFirst();
        TextView watchTextView = viewRepoDetailsModal.findViewById(R.id.watches);
        watchTextView.setText(Long.toString(repo.getWatchers()));
        TextView starTextView = viewRepoDetailsModal.findViewById(R.id.stars);
        starTextView.setText(Long.toString(repo.getStars()));
        TextView forkTextView = viewRepoDetailsModal.findViewById(R.id.forks);
        forkTextView.setText(Long.toString(repo.getForks()));
    }

    public void initSearch(RealmList<Repo> repos, long total_repos) {
        Toast.makeText(getApplicationContext(),
                "We found " + total_repos + " repos",
                Toast.LENGTH_SHORT).show();
        TOTAL_PAGES = (total_repos > 0) ? (int) Math.ceil((double) total_repos / PAGE_SIZE) : 0;
        ArrayList<DataOfRepos> list = DataOfRepos.createRepoList(repos);
        adapter.addAll(list);
        loadReposBar.setVisibility(View.GONE);
        if (page <= TOTAL_PAGES) adapter.addLoading();
        else isLastPage = true;
    }

    public void addSearch(RealmList<Repo> repos) {
        adapter.deleteLoading();
        isLoading = false;
        List<DataOfRepos> list = DataOfRepos.createRepoList(repos);
        adapter.addAll(list);
        if (page <= TOTAL_PAGES) adapter.addLoading();
        else isLastPage = true;
    }

    public void initList(List<Repo> repos) {
        TOTAL_PAGES =  (int) Math.ceil((double) total_repos / PAGE_SIZE);
        ArrayList<DataOfRepos> list = DataOfRepos.createRepoList(repos);
        adapter.addAll(list);
        loadReposBar.setVisibility(View.GONE);
        if (page <= TOTAL_PAGES) adapter.addLoading();
        else isLastPage = true;
    }
}


package code.diegohdez.githubapijava.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import code.diegohdez.githubapijava.AsyncTask.GetToken;
import code.diegohdez.githubapijava.R;
import okhttp3.Credentials;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER;
import static code.diegohdez.githubapijava.Utils.Constants.Result.RESULT_OK_GET_TOKEN;

public class GetTokenActivity extends AppCompatActivity {

    private static final String TAG = GetTokenActivity.class.getSimpleName();

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_token);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
    }


    public void getToken(View view) {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        if (username.length() > 0 && password.length() > 0) {
            String credentials = Credentials.basic(username,password);
            GetToken asyncGetToken = new GetToken(GetTokenActivity.this, credentials);
            asyncGetToken.execute(BASE_URL + USER);
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Please provide username a password",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public static void responseMessage(GetTokenActivity context, String message, int code) {
        Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT)
                .show();
        if (code == 200) {
            Intent intent = new Intent();
            context.setResult(RESULT_OK_GET_TOKEN, intent);
            context.finish();
        }
    }
}


package code.diegohdez.githubapijava.Utils.Constants;

public class Intents {
    public static final String REPO_ID = "REPO_ID";
    public static final String REPO_NAME = "REPO_NAME";
    public static final String BRANCH_NAME = "BRANCH_NAME";
}


package code.diegohdez.githubapijava.Utils.Constants;

public class Schema {
    public static final String REPO_SCHEMA = "Repo";
    public static final String OWNER_SCHEMA = "Owner";
    public static final String ISSUE_SCHEMA = "Issue";
    public static final String PULL_SCHEMA = "Pull";
    public static final String PULL_INFO_SCHEMA = "PullInfo";
    public static final String BRANCH_SCHEMA = "Branch";
    public static final String COMMIT_SCHEMA = "Commit";
    public static final String COMMIT_INFO_SCHEMA = "CommitInfo";
    public static final String AUTHOR_SCHEMA = "Author";
    public static final String DATE_COMMIT_SCHEMA = "DateCommit";
}


package code.diegohdez.githubapijava.Utils.Constants;

public class Dialog {
    public static final String CLOSE_REPO_DETAIL_DIALOG = "Close";
}


package code.diegohdez.githubapijava.Utils.Constants;

public class Numbers {
    public static final int PAGE_ONE = 1;
}


package code.diegohdez.githubapijava.Utils.Constants;

public class Fields {
    public static final String OWNER_LOGIN = "owner.login";
    public static final String ID ="id";
    public static final String LOGIN = "login";
    public static final String REPO_NAME = "name";
    public static final String OWNER = "owner";
    public static final String STARS = "stars";
    public static final String WATCHERS = "watchers";
    public static final String FORKS = "forks";
    public static final String SUBSCRIBERS = "subscribers";
    public static final String OLD_FULL_NAME = "full_name";
    public static final String OLD_CREATED_AT = "created_at";
    public static final String OLD_PUSHED_AT = "pushed_at";
    public static final String OLD_UPDATED_AT = "updated_at";
    public static final String FULL_NAME = "fullName";
    public static final String CREATED_AT = "createdAt";
    public static final String PUSHED_AT = "pushedAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String MERGED_AT = "mergedAt";
    public static final String REPOS = "repos";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String STATE = "state";
    public static final String NUMBER = "number";
    public static final String USER = "user";
    public static final String ASSIGNEE = "assignee";
    public static final String CLOSED_AT = "closedAt";
    public static final String ISSUES = "issues";
    public static final String PULLS = "pulls";
    public static final String PULL_INFO = "pullInfo";
    public static final String PULL_STATE = "state";
    public static final String BRANCH_NAME = "name";
    public static final String BRANCHES = "branches";
    public static final String AUTHOR = "author";
    public static final String SHA = "sha";
    public static final String COMMITS = "commits";
    public static final String MESSAGE = "message";
    public static final String DATE = "date";
    public static final String COMMIT_INFO = "commitInfo";
    public static final String AUTHOR_NAME = "name";
}


package code.diegohdez.githubapijava.Utils.Constants;

public class Result {
    public static final int RESULT_MAIN_GET_TOKEN = 1001;
    public static final int RESULT_OK_GET_TOKEN = 1002;
}


package code.diegohdez.githubapijava.Utils.Constants;

public class API {
    public static final String BASE_URL = "https://api.github.com/";
    public static final String USERS = "users/";
    public static final String REPOS = "/repos";
    public static final String USER_REPOS = "repos/";
    public static final String USER_ISSUES = "/issues";
    public static final String USER_PULLS = "/pulls";
    public static final String USER = "user";
    public static final String WATCH_REPO = "/subscription";
    public static final String STAR_REPO = "/starred";
    public static final String FORK_REPO = "/forks";
    public static final String BRANCHES = "/branches";
    public static final String COMMITS = "/commits";
    public static final String SEARCH = "search/";
    public static final String REPOSITORIES = "repositories";
    public static final String QUERY = "?q=";
    public static final String EQUAL = "=";
    public static final String PLUS = "+";
    public static final String COLON = ":";

    public static final String STATE_ALL = "?state=all";
    public static final String STATE_OPEN ="?state=open";
    public static final String STATE_CLOSED = "?state=closed";
    public static final String AND_PAGE = "&page=";

    public static final String AUTHORIZATION = "Authorization";
    public static final String ACCEPT = "Accept";

    public static final String APPLICATION_VND_GITHUB = "application/vnd.github.symmetra-preview+json";
    public static final int PAGE_SIZE = 30;

    public static final String UN_WATCH = "Unwatch";
    public static final String UN_STAR = "Unstar";

    public static final String WATCH = "Watch";
    public static final String STAR = "Star";

    public static final int AUTH_SUCCESS = 200;
    public static final int WATCH_REPO_SUCCESS = 200;
    public static final int UNWATCH_REPO_SUCCESS = 204;
    public static final int WATCHED_REPO_ERROR = 404;
    public static final int STARRED_REPO_SUCCESS = 204;
    public static final int STARRED_REPO_ERROR = 404;
    public static final int FORK_REPO_SUCCESS = 202;

    public static final String OPEN = "open";
    public static final String CLOSED = "closed";
    public static final String MERGED = "merged";

    public static final String GET_TOKEN = BASE_URL + USER;

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_REPO_FORMAT = "MMMM dd',' yyyy";

    public static String getRepos(String username) {
        return BASE_URL + USERS + username + REPOS;
    }
}


package code.diegohdez.githubapijava.Utils.Request;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;

import code.diegohdez.githubapijava.Manager.AppManager;

import static code.diegohdez.githubapijava.Utils.Constants.API.ACCEPT;
import static code.diegohdez.githubapijava.Utils.Constants.API.APPLICATION_VND_GITHUB;
import static code.diegohdez.githubapijava.Utils.Constants.API.AUTHORIZATION;

public class API {

    public static ANRequest getToken (String url, String token) {
        return AndroidNetworking
                .get(url)
                .addHeaders("Authorization", token)
                .build();
    }

    public static ANRequest getAccount(String url) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest getRepos(String url, int page) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url + "?page=" + page);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest searchInRepo(String url) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest onRepoEvenListener (String url) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }
    public static ANRequest watchRepo (String url) {
        ANRequest.PutRequestBuilder builder = AndroidNetworking
                .put(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders("Authorization", token);
        return builder.build();
    }

    public static ANRequest unWatchRepo (String url) {
        ANRequest.DeleteRequestBuilder builder = AndroidNetworking
                .delete(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest starRepo (String url) {
        ANRequest.PutRequestBuilder builder = AndroidNetworking
                .put(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest unStarRepo (String url) {
        ANRequest.DeleteRequestBuilder builder = AndroidNetworking
                .delete(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest forkRepo (String url) {
        ANRequest.PostRequestBuilder builder = AndroidNetworking
                .post(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest getIssues (String url) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url)
                .addHeaders(ACCEPT, APPLICATION_VND_GITHUB);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest getPulls (String url) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url)
                .addHeaders(ACCEPT, APPLICATION_VND_GITHUB);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest getBranches (String url) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return builder.build();
    }

    public static ANRequest getCommits (String url ) {
        ANRequest.GetRequestBuilder builder = AndroidNetworking
                .get(url);
        String token = AppManager.getOurInstance().getToken();
        if (token.length() > 0) builder.addHeaders(AUTHORIZATION, token);
        return  builder.build();
    }
}


package code.diegohdez.githubapijava.Adapter;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Data.DataOfIssues;

interface UpdateableIssuesFragment {
    public void update(ArrayList<DataOfIssues> issues);
}


package code.diegohdez.githubapijava.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import code.diegohdez.githubapijava.Activity.ReposActivity;
import code.diegohdez.githubapijava.Activity.ReposDetailActivity;
import code.diegohdez.githubapijava.BuildConfig;
import code.diegohdez.githubapijava.Data.DataOfRepos;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.Utils.Constants.Intents;
import code.diegohdez.navbottom.githubapijava.Activity.ReposDetailsActivity;
import okhttp3.Response;

import static code.diegohdez.githubapijava.Utils.Constants.API.ACCEPT;
import static code.diegohdez.githubapijava.Utils.Constants.API.APPLICATION_VND_GITHUB;
import static code.diegohdez.githubapijava.Utils.Constants.API.AUTHORIZATION;
import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.STARRED_REPO_ERROR;
import static code.diegohdez.githubapijava.Utils.Constants.API.STARRED_REPO_SUCCESS;
import static code.diegohdez.githubapijava.Utils.Constants.API.STAR_REPO;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.API.WATCHED_REPO_ERROR;
import static code.diegohdez.githubapijava.Utils.Constants.API.WATCH_REPO;

public class ReposAdapter extends RecyclerView.Adapter {
    private static final String TAG = ReposAdapter.class.getSimpleName();
    private static int ITEM = 1;
    private static int LOADER = 2;

    private List<DataOfRepos> repos;
    private String account;
    private ReposActivity context;
    private boolean isLoading;

    public ReposAdapter (List<DataOfRepos> repos, String account, ReposActivity context) {
        this.repos = repos;
        this.account = account;
        this.context = context;
        this.isLoading = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ITEM) {
            View repo = inflater.inflate(R.layout.item_repos, parent, false);
            return new ViewHolderItemRepo(repo);
        } else if (viewType == LOADER) {
            View loader = inflater.inflate(R.layout.repo_loader, parent, false);
            return new ViewHolderLoaderRepo(loader);
        } else
            throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderItemRepo) {
            final DataOfRepos repo = repos.get(position);
            ((ViewHolderItemRepo) holder).root.setTag(holder);
            ((ViewHolderItemRepo) holder).root.setOnClickListener(new View.OnClickListener(){
                Intent intent = null;
                @Override
                public void onClick(View v) {
                    if (BuildConfig.FLAVOR == "navBottom") {
                        intent = new Intent(context, ReposDetailsActivity.class);
                    } else {
                        intent = new Intent(context, ReposDetailActivity.class);
                    }
                    intent.putExtra(Intents.REPO_ID, repo.getId())
                            .putExtra(Intents.REPO_NAME, repo.getName());
                    context.startActivity(intent);
                }
            });
            ((ViewHolderItemRepo) holder).name.setText(repo.getName());
            ((ViewHolderItemRepo) holder).description.setText(repo.getDescription());
            ((ViewHolderItemRepo) holder).repoModal.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    context.setRepoStatus(
                            repo.getName(),
                            repo.getWatchers(),
                            repo.getStars(),
                            repo.getForks());
                    ANRequest.GetRequestBuilder getWatcher = AndroidNetworking
                            .get(BASE_URL + USER_REPOS + account + "/" + repo.getName() + WATCH_REPO);
                    ANRequest.GetRequestBuilder getStar = AndroidNetworking
                            .get(BASE_URL + USER + STAR_REPO + "/" + account + "/" + repo.getName());
                    String token = AppManager.getOurInstance().getToken();
                    if (token.length() > 0) {
                        getWatcher.addHeaders(AUTHORIZATION, token);
                        getStar.addHeaders(AUTHORIZATION, token);

                        getWatcher.build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        boolean subscribed = false;
                                        try {
                                            subscribed = response.getBoolean("subscribed");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        context.isSubscribed(subscribed);
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        if (anError.getErrorCode() == WATCHED_REPO_ERROR) context.isSubscribed(false);
                                        String message = "Error: " + anError.getErrorDetail() + "\n" +
                                                "Body: " + anError.getErrorBody() + "\n" +
                                                "Message: " + anError.getMessage() + "\n" +
                                                "Code: " + anError.getErrorCode();
                                        Log.e(TAG, message);
                                    }
                                });

                        getStar.addHeaders(ACCEPT, APPLICATION_VND_GITHUB)
                                .build()
                                .getAsOkHttpResponse(new OkHttpResponseListener() {
                                    @Override
                                    public void onResponse(Response response) {
                                        if (response.code() == STARRED_REPO_SUCCESS) context.isStarred(true);
                                        else if (response.code() == STARRED_REPO_ERROR) context.isStarred(false);
                                    }

                                    @Override
                                    public void onError(ANError anError) {
                                        if (anError.getErrorCode() == STARRED_REPO_ERROR) context.isStarred(false);
                                        String message = "Error: " + anError.getErrorDetail() + "\n" +
                                                "Body: " + anError.getErrorBody() + "\n" +
                                                "Message: " + anError.getMessage() + "\n" +
                                                "Code: " + anError.getErrorCode();
                                        Log.e(TAG, message);
                                    }
                                });
                    }
                }
            });
        } else if (holder instanceof ViewHolderLoaderRepo) {
            /*
            Nothing
             */
        } else {
            Log.d(TAG, "no instance of view holder found");
        }
    }

    @Override
    public int getItemCount() {
        return repos.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public void add(DataOfRepos repo) {
        repos.add(repo);
        notifyItemInserted(repos.size() - 1);
    }

    public void addAll(List<DataOfRepos> repos) {
        for (DataOfRepos repo : repos) add(repo);
    }

    public void clear () {
        isLoading = false;
        repos = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addLoading () {
        isLoading = true;
        add(new DataOfRepos());
    }

    public void deleteLoading () {
        isLoading = false;
        int position = repos.size() - 1;
        repos.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == repos.size() - 1 && isLoading) return LOADER;
        else return ITEM;
    }

    private class ViewHolderItemRepo extends RecyclerView.ViewHolder{

        TextView name;
        TextView description;
        ImageView repoModal;
        View root;
        ViewHolderItemRepo(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.repo_name);
            description = itemView.findViewById(R.id.repo_description);
            repoModal = itemView.findViewById(R.id.repo_details_modal);
            root = itemView;
        }
    }

    private class ViewHolderLoaderRepo extends RecyclerView.ViewHolder {

        ViewHolderLoaderRepo(View itemView) {
            super(itemView);
        }
    }

}


package code.diegohdez.githubapijava.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import code.diegohdez.githubapijava.Data.DataOfIssues;
import code.diegohdez.githubapijava.R;

import static code.diegohdez.githubapijava.Utils.Constants.API.CLOSED;
import static code.diegohdez.githubapijava.Utils.Constants.API.DATE_REPO_FORMAT;
import static code.diegohdez.githubapijava.Utils.Constants.API.MERGED;
import static code.diegohdez.githubapijava.Utils.Constants.API.OPEN;

public class IssuesAdapter extends RecyclerView.Adapter {

    private static final String TAG = IssuesAdapter.class.getSimpleName();

    private static int ITEM = 1;
    private static int LOADER = 2;

    SimpleDateFormat dateFormat;
    private ArrayList<DataOfIssues> issues;
    private boolean isLoading;

    public IssuesAdapter() {
        this.issues = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat(DATE_REPO_FORMAT);
        this.isLoading = false;
    }


    public IssuesAdapter (ArrayList<DataOfIssues> issues) {
        this.issues = issues;
        this.dateFormat = new SimpleDateFormat(DATE_REPO_FORMAT);
        this.isLoading = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ITEM) {
            View item = inflater.inflate(R.layout.item_issue, parent, false);
            return new ViewHolderItemIssue(item);
        } else if (viewType == LOADER){
            View loader = inflater.inflate(R.layout.issue_loader, parent, false);
            return new ViewHolderLoaderIssue(loader);
        } else
            throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderItemIssue) {
            DataOfIssues issue = issues.get(position);
            ((ViewHolderItemIssue) holder).title.setText(issue.getTitle());
            ((ViewHolderItemIssue) holder).header.setText(getHeader(
                    issue.isPull(),
                    issue.getPullState(),
                    issue.getNumber(),
                    issue.getState(),
                    issue.getUser(),
                    issue.getCreatedAt(),
                    issue.getClosedAt()
            ));
            ((ViewHolderItemIssue) holder).icon.setImageResource(getDrawable(
                    issue.isPull(),
                    issue.getPullState(),
                    issue.getState()));
        } else if (holder instanceof ViewHolderLoaderIssue) {
            /*
            Nothing
             */
        } else {
            Log.d(TAG, "no instance of view holder found");
        }
    }

    @Override
    public int getItemCount() {
        return issues.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == this.issues.size() - 1 && isLoading) return LOADER;
        else return ITEM;
    }

    private String getHeader(boolean isPull,
                             String pullState,
                             long number,
                             String state,
                             String user,
                             Date createdAt,
                             Date closedAt) {
        if (isPull) {
         switch (pullState) {
             case OPEN:
                 return "#" + number + " opened by " + user + " in " + dateFormat.format(createdAt);
             case CLOSED:
                 return "#" + number + " by " + user + " closed in " + dateFormat.format(closedAt);
             case MERGED:
                 return "#" + number + " by " + user + " merged in " + dateFormat.format(closedAt);
         }
        }
        switch (state) {
            case OPEN:
                return "#" + number + " opened by " + user + " in " + dateFormat.format(createdAt);
            case CLOSED:
                return "#" + number + " by " + user + " closed in " + dateFormat.format(closedAt);
            default:
                return "";
        }
    }

    private int getDrawable(boolean isPull, String pullState, String state) {
        if (isPull) {
            switch (pullState) {
                case OPEN:
                    return R.drawable.pull_open;
                case CLOSED:
                    return R.drawable.pull_closed;
                case MERGED:
                    return R.drawable.pull_merged;
            }
        }
        switch (state) {
            case OPEN:
                return R.drawable.issue_open;
            case CLOSED:
                return R.drawable.issue_closed;
        }
        return -1;
    }

    public void clear () {
        this.issues = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addIssues(ArrayList<DataOfIssues> issues) {
        this.issues.addAll(issues);
        notifyDataSetChanged();
    }

    public void add(DataOfIssues issue) {
        this.issues.add(issue);
        notifyItemInserted(issues.size() - 1);
    }

    public void deleteLoading() {
        isLoading = false;
        int position = this.issues.size() - 1;
        this.issues.remove(position);
        notifyItemRemoved(position);
    }

    public void addLoading() {
        isLoading = true;
        add(new DataOfIssues());
    }

    public boolean isLoading() {
        return isLoading;
    }

    private class ViewHolderItemIssue extends RecyclerView.ViewHolder {

        TextView title;
        TextView header;
        ImageView icon;
        ViewHolderItemIssue(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.issue_title);
            header = itemView.findViewById(R.id.issue_header);
            icon = itemView.findViewById(R.id.image_issue);
        }
    }

    private class ViewHolderLoaderIssue extends RecyclerView.ViewHolder {

        ViewHolderLoaderIssue(View itemView) {
            super(itemView);
        }
    }
}


package code.diegohdez.githubapijava.Adapter;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Data.DataOfBranches;

public interface UpdateableBranchesFragment {
    void update(ArrayList<DataOfBranches> branches);
}


package code.diegohdez.githubapijava.Adapter;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Data.DataOfPulls;

public interface UpdateablePullsFragment {
    public void update(ArrayList<DataOfPulls> pulls);
}


package code.diegohdez.githubapijava.Adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import code.diegohdez.githubapijava.AsyncTask.PullsRepo;
import code.diegohdez.githubapijava.Data.DataOfPulls;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.PullsPaginationScrollListener;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_ALL;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_PULLS;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class PullsFragment extends Fragment implements UpdateablePullsFragment{

    public static final String ARG_ID = "ID";
    public static final String ARG_REPO_NAME = "REPO_NAME";

    PullsAdapter adapter;

    int page = PAGE_ONE;
    boolean isLoading = false;
    boolean isLastPage = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.pulls_fragment, container, false);
        final Bundle args = getArguments();
        RecyclerView recyclerView = root.findViewById(R.id.pulls_list);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        adapter = new PullsAdapter();
        recyclerView.setScrollbarFadingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayout);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new PullsPaginationScrollListener(linearLayout) {
            @Override
            protected void loadPulls() {
                isLoading = true;
                page++;
                Toast.makeText(
                        getContext(),
                        "Display page " + page,
                        Toast.LENGTH_SHORT).show();
                PullsRepo pulls = new PullsRepo(getActivity(), args.getLong(ARG_ID));
                pulls.execute(BASE_URL + USER_REPOS + AppManager.getOurInstance().getAccount() +
                        "/" + args.getString(ARG_REPO_NAME) + USER_PULLS + STATE_ALL + "&page=" + page);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        return  root;
    }

    @Override
    public void update(ArrayList<DataOfPulls> pulls) {
        if (page > 1) {
            adapter.deleteLoading();
            isLoading = false;
        }
        if (!adapter.isLoading()) {
            adapter.addPulls(pulls);
            if (pulls.size() < PAGE_SIZE) isLastPage = true;
            else adapter.addLoading();
        }
    }
}


package code.diegohdez.githubapijava.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Activity.CommitsActivity;
import code.diegohdez.githubapijava.BuildConfig;
import code.diegohdez.githubapijava.Data.DataOfBranches;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.Utils.Constants.Intents;
import code.diegohdez.navbottom.githubapijava.Adapter.BranchesFragment;

public class BranchesAdapter extends RecyclerView.Adapter {

    private static final String TAG = BranchesAdapter.class.getSimpleName();

    private static final int ITEM = 0;
    private static final int LOADER = 1;

    private boolean isLoading;
    private ArrayList<DataOfBranches> branches;
    private long id;
    private String repoName;
    private Context context;
    private BranchesFragment fragment;

    public BranchesAdapter() {
        this.branches = new ArrayList<>();
    }

    public BranchesAdapter(long id, String repoName, Context context) {
        this.branches = new ArrayList<>();
        this.id = id;
        this.repoName = repoName;
        this.context = context;
    }

    public BranchesAdapter(long id, String repoName, BranchesFragment fragment) {
        this.branches = new ArrayList<>();
        this.id = id;
        this.repoName = repoName;
        this.fragment = fragment;
        this.isLoading = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ITEM) {
            View item = inflater.inflate(R.layout.item_branch, parent, false);
            return new ViewHolderBranchItem(item);
        } else if (viewType == LOADER) {
            View loader = inflater.inflate(R.layout.branch_loader, parent, false);
            return new ViewHolderBranchLoader(loader);
        } else throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderBranchItem) {
            final DataOfBranches item = this.branches.get(position);
            ((ViewHolderBranchItem) holder).item.setText(item.getName());
            ((ViewHolderBranchItem) holder).root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    if (BuildConfig.FLAVOR.equals("navBottom"))
                        intent = new Intent(fragment.getContext(), CommitsActivity.class);
                    else intent = new Intent(context, CommitsActivity.class);
                    intent.putExtra(Intents.REPO_ID, id)
                            .putExtra(Intents.REPO_NAME, repoName)
                            .putExtra(Intents.BRANCH_NAME, item.getName());
                    if (BuildConfig.FLAVOR.equals("navBottom")) {
                        fragment.startActivity(intent);
                    } else {
                        context.startActivity(intent);
                    }
                }
            });
        } else if (holder instanceof ViewHolderBranchLoader) {
            // Nothing to do
        } else {
            Log.d(TAG, "no instance of view holder found");
        }
    }

    @Override
    public int getItemCount() {
        return branches.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == this.branches.size() - 1 && isLoading) return LOADER;
        else return ITEM;
    }

    public void addBranches(ArrayList<DataOfBranches> branches) {
        this.branches.addAll(branches);
        notifyDataSetChanged();
    }

    public void add(DataOfBranches branch) {
        this.branches.add(branch);
        notifyItemInserted(branches.size() - 1);
    }

    public void deleteLoading() {
        isLoading = false;
        int position = this.branches.size() - 1;
        this.branches.remove(position);
        notifyItemRemoved(position);
    }

    public void addLoading() {
        isLoading = true;
        add(new DataOfBranches());
    }

    private class ViewHolderBranchItem extends RecyclerView.ViewHolder {

        TextView item;
        View root;

        public ViewHolderBranchItem(View itemView) {
            super(itemView);
            root = itemView;
            item = itemView.findViewById(R.id.branch_name);
        }
    }

    private class ViewHolderBranchLoader extends RecyclerView.ViewHolder {
        public ViewHolderBranchLoader(View itemView) {
            super(itemView);
        }
    }
}


package code.diegohdez.githubapijava.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import code.diegohdez.githubapijava.Data.DataOfCommits;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.Utils.Constants.API;

public class CommitsAdapter extends RecyclerView.Adapter {

    private static final String TAG = CommitsAdapter.class.getSimpleName();

    private static int ITEM = 0;
    private static int LOADER = 1;

    private ArrayList<DataOfCommits> commits;
    private boolean isLoading;
    SimpleDateFormat dateFormat;

    public CommitsAdapter() {
        this.commits = new ArrayList<>();
        this.isLoading = false;
        dateFormat = new SimpleDateFormat(API.DATE_REPO_FORMAT);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ITEM) {
            View item = inflater.inflate(R.layout.item_commit, parent, false);
            return new ViewHolderCommitItem(item);
        } else if (viewType == LOADER) {
            View loader = inflater.inflate(R.layout.commit_loader, parent, false);
            return new ViewHolderCommitLoader(loader);
        } throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderCommitItem) {
            DataOfCommits commit = commits.get(position);
            ((ViewHolderCommitItem) holder).title.setText(parseTitle(commit.getMessage()));
            ((ViewHolderCommitItem) holder).description.setText(setDescription(commit.getAuthor(), commit.getDate()));
        } else if (holder instanceof ViewHolderCommitLoader){
            // Nothing
        } else
            Log.d(TAG, "no instance of view holder found");

    }

    private String parseTitle(String title) {
        return (title.length() > 50)
                ? ((title.indexOf("\n") > -1) ? title.split("\n")[0] : title.substring(0, 50) + "...")
                : ((title.indexOf("\n") > -1) ? title.split("\n")[0] : title);
    }

    private String setDescription(String author, Date date) {
        return author + " commited at " + dateFormat.format(date);
    }

    @Override
    public int getItemCount() {
        return commits.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == this.commits.size() - 1 && isLoading ) return LOADER;
        else return ITEM;
    }

    public void addCommits(ArrayList<DataOfCommits> commits) {
        this.commits.addAll(commits);
        notifyDataSetChanged();
    }

    public void add(DataOfCommits commit) {
        this.commits.add(commit);
        notifyItemInserted(commits.size() - 1);
    }

    public void deleteLoading() {
        isLoading = false;
        int position = this.commits.size() - 1;
        this.commits.remove(position);
        notifyItemRemoved(position);
    }

    public void addLoading() {
        isLoading = true;
        add(new DataOfCommits());
    }

    public boolean isLoading() {
        return isLoading;
    }

    private class ViewHolderCommitItem extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;

        public ViewHolderCommitItem(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.commit_title);
            description = itemView.findViewById(R.id.commit_header);
        }
    }

    private class ViewHolderCommitLoader extends RecyclerView.ViewHolder {

        public ViewHolderCommitLoader(View itemView) {
            super(itemView);
        }
    }
}


package code.diegohdez.githubapijava.Adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Data.DataOfBranches;
import code.diegohdez.githubapijava.Data.DataOfIssues;
import code.diegohdez.githubapijava.Data.DataOfPulls;

public class PageRepoAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = PageRepoAdapter.class.getSimpleName();
    private static final String ID = "ID";
    private long id;
    private static final String REPO_NAME = "REPO_NAME";
    private String repoName;

    private ArrayList<DataOfIssues> issues;
    private ArrayList<DataOfPulls> pulls;
    private ArrayList<DataOfBranches> branches;

    @SuppressLint("SimpleDateFormat")
    public PageRepoAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setIssues (ArrayList<DataOfIssues> issues) {
        this.issues = issues;
        notifyDataSetChanged();
    }

    public void setPulls(ArrayList<DataOfPulls> pulls) {
        this.pulls = pulls;
        notifyDataSetChanged();
    }

    public void setData(ArrayList<DataOfIssues> issues,
                        ArrayList<DataOfPulls> pulls,
                        ArrayList<DataOfBranches> branches) {
        this.issues = issues;
        this.pulls = pulls;
        this.branches = branches;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putLong(ID, this.id);
        args.putString(REPO_NAME, this.repoName);
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new IssuesFragment();
                fragment.setArguments(args);
                break;
            case 1:
                fragment = new PullsFragment();
                fragment.setArguments(args);
                break;
        }
        return fragment;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (object instanceof IssuesFragment) {
            ((IssuesFragment) object).update(this.issues);
        } else if (object instanceof PullsFragment) {
            ((PullsFragment) object).update(this.pulls);
        }
        return super.getItemPosition(object);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Issues";
            case 1:
                return "Pull Request";
        }
        return null;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRepoName (String repoName) {
        this.repoName = repoName;
    }
}


package code.diegohdez.githubapijava.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import code.diegohdez.githubapijava.Data.DataOfPulls;
import code.diegohdez.githubapijava.R;

import static code.diegohdez.githubapijava.Utils.Constants.API.CLOSED;
import static code.diegohdez.githubapijava.Utils.Constants.API.DATE_REPO_FORMAT;
import static code.diegohdez.githubapijava.Utils.Constants.API.MERGED;
import static code.diegohdez.githubapijava.Utils.Constants.API.OPEN;

public class PullsAdapter extends RecyclerView.Adapter {

    private static final String TAG = PullsAdapter.class.getSimpleName();

    private static final int ITEM = 0;
    private static final int LOADER = 1;

    ArrayList<DataOfPulls> pulls;
    SimpleDateFormat dateFormat;
    private boolean isLoading;

    public PullsAdapter() {
        this.pulls = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat(DATE_REPO_FORMAT);
        this.isLoading = false;
    }


    public PullsAdapter(ArrayList<DataOfPulls> pulls) {
        this.pulls = pulls;
        this.dateFormat = new SimpleDateFormat(DATE_REPO_FORMAT);
        this.isLoading = false;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == ITEM) {
            View item = inflater.inflate(R.layout.item_pull, parent, false);
            return new ViewHolderPullItem(item);
        } else if(viewType == LOADER) {
            View loader = inflater.inflate(R.layout.pull_loader, parent, false);
            return new VIewHolderPullLoader(loader);
        } else throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolderPullItem) {
            DataOfPulls pull = pulls.get(position);
            ((ViewHolderPullItem) holder).title.setText(pull.getTitle());
            ((ViewHolderPullItem) holder).header.setText(getHeader(
                    pull.getState(),
                    pull.getNumber(),
                    pull.getUser(),
                    pull.getCreatedAt(),
                    pull.getClosedAt(),
                    pull.getMergedAt()));
            ((ViewHolderPullItem) holder).icon.setImageResource(getDrawable(pull.getState()));
        } else if (holder instanceof VIewHolderPullLoader) {
            /*
            Nothing
             */
        } else {
            Log.d(TAG, "no instance of view holder found");
        }
    }

    @Override
    public int getItemCount() {
        return pulls.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == this.pulls.size() - 1 && isLoading) return LOADER;
        return ITEM;
    }

    private String getHeader(String state,
                             long number,
                             String user,
                             Date createdAt,
                             Date closedAt,
                             Date mergedAt) {
        switch (state) {
            case OPEN:
                return "#" + number + " opened by " + user + " in " + dateFormat.format(createdAt);
            case CLOSED:
                return "#" + number + " by " + user + " closed in " + dateFormat.format(closedAt);
            case MERGED:
                return "#" + number + " by " + user + " merged in " + dateFormat.format(mergedAt);
                default:
                    return "";
        }
    }

    private int getDrawable(String state) {
        switch (state) {
            case OPEN:
                return R.drawable.pull_open;
            case CLOSED:
                return R.drawable.pull_closed;
            case MERGED:
                return R.drawable.pull_merged;
                default:
                    return 1;
        }

    }

    public void clear() {
        this.pulls = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPulls(ArrayList<DataOfPulls> list) {
        pulls.addAll(list);
        notifyDataSetChanged();
    }

    public void add(DataOfPulls pull) {
        this.pulls.add(pull);
        notifyItemInserted(pulls.size() - 1);
    }

    public void deleteLoading() {
        isLoading = false;
        int position = this.pulls.size() - 1;
        this.pulls.remove(position);
        notifyItemRemoved(position);
    }

    public void addLoading() {
        isLoading = true;
        add(new DataOfPulls());
    }

    public boolean isLoading() {
        return isLoading;
    }

    private class ViewHolderPullItem extends RecyclerView.ViewHolder {

        TextView title;
        TextView header;
        ImageView icon;

        ViewHolderPullItem(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.pull_title);
            header = itemView.findViewById(R.id.pull_header);
            icon = itemView.findViewById(R.id.image_pull);
        }
    }

    private class VIewHolderPullLoader extends RecyclerView.ViewHolder {

        public VIewHolderPullLoader(View itemView) {
            super(itemView);
        }
    }
}


package code.diegohdez.githubapijava.Adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import code.diegohdez.githubapijava.AsyncTask.IssuesRepo;
import code.diegohdez.githubapijava.Data.DataOfIssues;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.R;
import code.diegohdez.githubapijava.ScrollListener.IssuesPaginationScrollListener;

import static code.diegohdez.githubapijava.Utils.Constants.API.BASE_URL;
import static code.diegohdez.githubapijava.Utils.Constants.API.PAGE_SIZE;
import static code.diegohdez.githubapijava.Utils.Constants.API.STATE_ALL;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_ISSUES;
import static code.diegohdez.githubapijava.Utils.Constants.API.USER_REPOS;
import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

@SuppressLint("ValidFragment")
public class IssuesFragment extends Fragment implements UpdateableIssuesFragment {

    public static final String ARG_ID = "ID";
    public static final String ARG_REPO_NAME = "REPO_NAME";

    RecyclerView recyclerView;
    IssuesAdapter adapter;
    LinearLayout layout_checkboxes;

    int page = PAGE_ONE;
    boolean isLoading = false;
    boolean isLastPage = false;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.issues_fragment, container, false);
        layout_checkboxes.setVisibility(View.GONE);
        final Bundle args = getArguments();
        recyclerView = root.findViewById(R.id.issues_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        adapter = new IssuesAdapter();
        recyclerView.setScrollbarFadingEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener( new IssuesPaginationScrollListener(layoutManager) {

            @Override
            protected void loadIssues() {
                isLoading = true;
                page++;
                Toast.makeText(
                        getContext(),
                        "Display page " + page,
                        Toast.LENGTH_SHORT).show();
                IssuesRepo loadIssues = new IssuesRepo(getActivity(), args.getLong(ARG_ID));
                loadIssues.execute(BASE_URL +
                        USER_REPOS +
                        AppManager.getOurInstance().getAccount() +
                        "/" +args.getString(ARG_REPO_NAME) +
                        USER_ISSUES + STATE_ALL + "&page=" + page);
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
        return  root;
    }

    @Override
    public void update(ArrayList<DataOfIssues> issues) {
        if (page > PAGE_ONE) {
            adapter.deleteLoading();
            isLoading = false;
        }
        if (!adapter.isLoading()) {
            adapter.addIssues(issues);
            if (issues.size() < PAGE_SIZE) isLastPage = true;
            else adapter.addLoading();
        }
    }
}


package code.diegohdez.githubapijava.Manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class AppManager {
    private static AppManager ourInstance;

    public static AppManager getOurInstance() {
        return ourInstance;
    }

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private final String ACCOUNT = "ACCOUNT";
    private String account;
    private final String TOKEN = "TOKEN";
    private String token;
    private final String CURRENT_PAGE = "CURRENT_PAGE";
    private int currentPage;
    private final String ISSUES_PAGE = "ISSUES_PAGE";
    private int issuesPage;
    private final String PULLS_PAGE = "PULLS_PAGE";
    private int pullsPage;
    private final String BRANCHES_PAGE = "BRANCHES_PAGE";
    private int branchesPage;

    public void setAccount (String account) {
        this.account = account;
        editor.putString(ACCOUNT, account);
        editor.commit();
    }

    public String getAccount() {
        return account;
    }

    public void setToken (String token) {
        this.token = token;
        editor.putString(TOKEN, token);
        editor.commit();
    }

    public void setIssuesPage(int issuesPage) {
        this.issuesPage = issuesPage;
        editor.putInt(ISSUES_PAGE, issuesPage);
        editor.commit();
    }

    public void setPullsPage(int pullsPage) {
        this.pullsPage = pullsPage;
        editor.putInt(PULLS_PAGE, pullsPage);
        editor.commit();
    }

    public void setBranchesPage(int branchesPage) {
        this.branchesPage = branchesPage;
        editor.putInt(BRANCHES_PAGE, branchesPage);
        editor.commit();
    }

    public String getToken() {
        return token;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCurrentIssuesPage() { return issuesPage; }

    public int getCurrentPullsPage() { return pullsPage; }

    public int getCurrentBranchPage() { return  branchesPage; }

    public static void init (Context context) {
        ourInstance = new AppManager(context.getApplicationContext());
    }

    private AppManager(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        editor = prefs.edit();
        account = prefs.getString(ACCOUNT, "");
        token = prefs.getString(TOKEN, "");
        currentPage = prefs.getInt(CURRENT_PAGE, PAGE_ONE);

        /*
          For NavBottom
         */

        issuesPage = prefs.getInt(ISSUES_PAGE, PAGE_ONE);
        pullsPage = prefs.getInt(PULLS_PAGE, PAGE_ONE);
        branchesPage = prefs.getInt(BRANCHES_PAGE, PAGE_ONE);
    }

    public void resetAccount () {
        setAccount("");
    }

    public void resetRepoDetailsPage() {
        setIssuesPage(1);
        setPullsPage(1);
        setBranchesPage(1);
    }

    public void initPager () { setCurrentPage(1); }

    public void logout () {
        setAccount("");
        setToken("");
        setCurrentPage(1);
    }
}


package code.diegohdez.githubapijava.ScrollListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class ReposPaginationScrollListener extends RecyclerView.OnScrollListener {
    LinearLayoutManager layoutManager;

    public ReposPaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                    && totalItemCount >= getTotalPageCount()) {
                loadRepos();
            }
        }
    }

    protected abstract void loadRepos ();

    public abstract int getTotalPageCount ();
    public abstract boolean isLastPage ();
    public abstract boolean isLoading ();
}


package code.diegohdez.githubapijava.ScrollListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class CommitPaginationScrollListener extends RecyclerView.OnScrollListener {
    LinearLayoutManager linearLayoutManager;

    public CommitPaginationScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        int visibleItemCount = linearLayoutManager.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadCommits();
            }
        }
    }

    protected abstract void loadCommits ();
    public abstract boolean isLastPage ();
    public abstract boolean isLoading ();

}


package code.diegohdez.githubapijava.ScrollListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class IssuesPaginationScrollListener extends RecyclerView.OnScrollListener {

    LinearLayoutManager linearLayoutManager;

    public IssuesPaginationScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        int visibleItemCount = linearLayoutManager.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadIssues();
            }
        }
    }

    protected abstract void loadIssues ();
    public abstract boolean isLastPage ();
    public abstract boolean isLoading ();

}


package code.diegohdez.githubapijava.ScrollListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class PullsPaginationScrollListener extends RecyclerView.OnScrollListener{

    LinearLayoutManager linearLayoutManager;

    public PullsPaginationScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        int visibleItemCount = linearLayoutManager.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadPulls();
            }
        }
    }

    protected abstract void loadPulls ();
    public abstract boolean isLastPage ();
    public abstract boolean isLoading ();

}


package code.diegohdez.githubapijava.ScrollListener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class BranchPaginationScrollListener extends RecyclerView.OnScrollListener{

    LinearLayoutManager linearLayoutManager;

    public BranchPaginationScrollListener(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        int visibleItemCount = linearLayoutManager.getChildCount();
        int totalItemCount = linearLayoutManager.getItemCount();
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadBranches();
            }
        }
    }

    protected abstract void loadBranches ();
    public abstract boolean isLastPage ();
    public abstract boolean isLoading ();
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import java.util.ArrayList;
import java.util.List;

import code.diegohdez.githubapijava.Activity.MainActivity;
import code.diegohdez.githubapijava.Activity.ReposActivity;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Model.Owner;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Request.API;
import io.realm.Realm;

import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class Repos extends AsyncTask<String, Void, ANResponse[]> {

    public static final String TAG = Repos.class.getSimpleName();
    private Realm realm;
    private Context context;
    private int page = 1;

    public Repos(MainActivity context) {
        realm = Realm.getDefaultInstance();
        this.context = context;
    }

    public Repos(ReposActivity context, int page) {
        realm = Realm.getDefaultInstance();
        this.context = context;
        this.page = page;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ANResponse[] doInBackground(String... urls) {
        ANRequest[] request;
        if (urls.length > 1 ) request = new ANRequest[2];
        else request = new ANRequest[1];
        request[0] = API.getRepos(urls[0], page);
        ANResponse user = null;
        ANResponse repos = (ANResponse<List<Repo>>) request[0].executeForObjectList(Repo.class);
        if (urls.length > 1) {
            request[1] = API.getAccount(urls[1]);
            user = (ANResponse<Owner>) request[1].executeForObject(Owner.class);
        }
        ANResponse[] results;
        if (urls.length > 1 ) results = new ANResponse[2];
        else  results = new ANResponse[1];
        results[0] = repos;
        if (urls.length > 1) results[1] = user;
        return results;
    }

    @Override
    protected void onPostExecute(ANResponse[] response) {

        super.onPostExecute(response);
        List<Repo> list = new ArrayList<>();
        String message = "";
        int status = 0;
        ANResponse repos = response[0];
        if (repos.isSuccess()) {
            list = (List<Repo>) repos.getResult();
            if (realm.isClosed()) {
                realm = Realm.getDefaultInstance();
            }
            final List<Repo> listOfRepos = list;
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(listOfRepos);
                }
            });
            message = AppManager.getOurInstance().getAccount() + " repositories successfully";
            status = repos.getOkHttpResponse().code();
        } else {
            AppManager.getOurInstance().resetAccount();
            ANError anError = repos.getError();
            message = "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            status = anError.getErrorCode();
        }
        if (response.length > 1) {
            ANResponse user = response[1];
            if (user.isSuccess()) {
                final Owner owner = (Owner) user.getResult();
                if (realm.isClosed()) realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.insertOrUpdate(owner);
                    }
                });
            } else {
                AppManager.getOurInstance().resetAccount();
                ANError anError = user.getError();
                message = "Error: " + anError.getErrorDetail() + "\n" +
                        "Body: " + anError.getErrorBody() + "\n" +
                        "Message: " + anError.getMessage() + "\n" +
                        "Code: " + anError.getErrorCode();
                Log.e(TAG, message);
            }
        }
        realm.close();
        switch (context.getClass().getSimpleName()) {
            case "MainActivity":
                MainActivity.successRepos(context, message, status);
                break;
            case "ReposActivity":
                ReposActivity activity = (ReposActivity) context;
                if (page > PAGE_ONE)activity.successLoader(list);
                else activity.initList(list);
                break;
        }
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Activity.ReposDetailActivity;
import code.diegohdez.githubapijava.BuildConfig;
import code.diegohdez.githubapijava.Model.Pull;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Request.API;
import code.diegohdez.navbottom.githubapijava.Adapter.PullsFragment;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.CLOSED;

public class PullsRepo extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = PullsRepo.class.getSimpleName();

    private Realm realm;
    private Context context;
    private PullsFragment fragment;
    private long id;

    public PullsRepo (FragmentActivity context, long id) {
        this.context = context;
        this.id = id;
        this.realm = Realm.getDefaultInstance();
    }

    public PullsRepo (PullsFragment context, long id) {
        this.fragment = context;
        this.id = id;
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        ANRequest request = API.getPulls(urls[0]);
        return request.executeForObjectList(Pull.class);
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);

        if (response.isSuccess()) {
            final RealmList<Pull> pulls = toPullsList((ArrayList<Pull>) response.getResult());
            if (realm.isClosed()) realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
                    for (Pull pull : pulls) {
                        if (pull.getMergedAt() != null) pull.setState("merged");
                    }
                    repo.getPulls().addAll(pulls);
                    realm.insertOrUpdate(repo);
                }
            });
            if (BuildConfig.FLAVOR.equals("navBottom")) fragment.addPulls(pulls);
            else ((ReposDetailActivity) context).addPulls(pulls);
        }
    }

    public RealmList<Pull> toPullsList(ArrayList<Pull> list) {
        RealmList<Pull> pulls = new RealmList();
        for (Pull item : list) {
            Pull pull = new Pull();
            pull.setId(item.getId());
            pull.setTitle(item.getTitle());
            pull.setDescription(item.getDescription());
            pull.setNumber(item.getNumber());
            pull.setState(item.getState());
            pull.setUser(item.getUser());
            if (item.getAssignee() != null) pull.setAssignee(item.getAssignee());
            pull.setCreatedAt(item.getCreatedAt());
            pull.setUpdatedAt(item.getUpdatedAt());
            if (item.getState().equals(CLOSED)) pull.setClosedAt(item.getClosedAt());
            if (item.getMergedAt() != null) pull.setMergedAt(item.getMergedAt());
            pulls.add(pull);
        }
        return pulls;
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import code.diegohdez.githubapijava.Activity.ReposActivity;
import code.diegohdez.githubapijava.Utils.Request.API;

import static code.diegohdez.githubapijava.Utils.Constants.API.UNWATCH_REPO_SUCCESS;
import static code.diegohdez.githubapijava.Utils.Constants.API.WATCH_REPO_SUCCESS;

public class WatchRepo extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = WatchRepo.class.getSimpleName();
    private Context context;
    private boolean isWatched;
    private String name;

    public WatchRepo (boolean isWatched, ReposActivity context, String name){
        this.isWatched = isWatched;
        this.context = context;
        this.name = name;
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        if (isWatched) {
            ANRequest request = API.unWatchRepo(urls[0]);
            return request.executeForOkHttpResponse();
        }
        ANRequest request = API.watchRepo(urls[0]);
        return request.executeForOkHttpResponse();
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            if(response.getOkHttpResponse().code() == WATCH_REPO_SUCCESS) {
                ((ReposActivity) context).updateRepoAfterWatched(true, name,  "Watch repo successfully");
            } else if (response.getOkHttpResponse().code() == UNWATCH_REPO_SUCCESS)
                ((ReposActivity) context).updateRepoAfterWatched(false, name, "Unwatch repo successfully");
        } else {
            ANError anError = response.getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Activity.ReposDetailActivity;
import code.diegohdez.githubapijava.BuildConfig;
import code.diegohdez.githubapijava.Model.Branch;
import code.diegohdez.githubapijava.Model.Issue;
import code.diegohdez.githubapijava.Model.Pull;
import code.diegohdez.githubapijava.Model.PullInfo;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Request.API;
import code.diegohdez.navbottom.githubapijava.Activity.ReposDetailsActivity;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.CLOSED;

public class DetailsRepo extends AsyncTask<String, Void, ANResponse[]> {

    private static final String TAG = DetailsRepo.class.getSimpleName();

    private Realm realm;
    private Context context;
    private long id;

    public DetailsRepo(ReposDetailActivity context, long id) {
        this.context = context;
        this.id = id;
        realm = Realm.getDefaultInstance();
    }

    public DetailsRepo(ReposDetailsActivity context, long id) {
        this.context = context;
        this.id = id;
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected ANResponse[] doInBackground(String... urls) {
        ANRequest[] requests = new ANRequest[3];
        ANResponse[] responses = new ANResponse[3];
        requests[0] = API.getPulls(urls[0]);
        requests[1] = API.getIssues(urls[1]);
        requests[2] = API.getBranches(urls[2]);
        responses[0] = requests[0].executeForObjectList(Pull.class);
        responses[1] = requests[1].executeForObjectList(Issue.class);
        responses[2] = requests[2].executeForObjectList(Branch.class);
        return responses;
    }

    @Override
    protected void onPostExecute(ANResponse[] responses) {
        super.onPostExecute(responses);
        if (responses[0].isSuccess()) {
            final RealmList<Pull> pulls = toPullsList((ArrayList<Pull>) responses[0].getResult());
            if (realm.isClosed()) realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
                    for (Pull pull : pulls) {
                        if (pull.getMergedAt() != null) pull.setState("merged");
                    }
                    repo.getPulls().addAll(pulls);
                    realm.insertOrUpdate(repo);
                }
            });
        } else {
            ANError anError = responses[0].getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
        if (responses[1].isSuccess()) {
            if (realm.isClosed()) realm = Realm.getDefaultInstance();
            final RealmList<Issue> list = toIssuesList((ArrayList<Issue>) responses[1].getResult());
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
                    RealmList<Pull> pulls = repo.getPulls();
                    for (Issue item : list) {
                        Pull pull = pulls.where().equalTo(Fields.NUMBER, item.getNumber()).findFirst();
                        if (pull != null) {
                            PullInfo pullInfo = new PullInfo();
                            pullInfo.setState(pull.getState());
                            item.setPullInfo(pullInfo);
                        }
                    }
                    repo.getIssues().addAll(list);
                    realm.insertOrUpdate(repo);
                }
            });
        } else {
            ANError anError = responses[1].getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
        if (responses[2].isSuccess()) {
            final RealmList<Branch> list = toBrancheshList((ArrayList<Branch>) responses[2].getResult());
            if (realm.isClosed()) realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
                    repo.getBranches().addAll(list);
                    realm.insertOrUpdate(repo);
                }
            });
        } else {
            ANError anError = responses[2].getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
        realm.close();
        if (BuildConfig.FLAVOR.equals("navBottom")) ((ReposDetailsActivity) context).createFragment();
        else ((ReposDetailActivity) context).createAdapter(id);
    }

    public RealmList<Issue> toIssuesList(ArrayList<Issue> list) {
        RealmList<Issue> issues = new RealmList();
        for (Issue item: list) {
            Issue issue = new Issue();
            issue.setId(item.getId());
            issue.setTitle(item.getTitle());
            issue.setDescription(item.getDescription());
            issue.setNumber(item.getNumber());
            issue.setState(item.getState());
            issue.setUser(item.getUser());
            if (item.getAssignee() != null) issue.setAssignee(item.getAssignee());
            issue.setCreatedAt(item.getCreatedAt());
            issue.setUpdatedAt(item.getUpdatedAt());
            if (issue.getState().equals(CLOSED)) issue.setClosedAt(item.getClosedAt());
            issues.add(issue);
        }
        return issues;
    }

    public RealmList<Pull> toPullsList(ArrayList<Pull> list) {
        RealmList<Pull> pulls = new RealmList();
        for (Pull item : list) {
            Pull pull = new Pull();
            pull.setId(item.getId());
            pull.setTitle(item.getTitle());
            pull.setDescription(item.getDescription());
            pull.setNumber(item.getNumber());
            pull.setState(item.getState());
            pull.setUser(item.getUser());
            if (item.getAssignee() != null) pull.setAssignee(item.getAssignee());
            pull.setCreatedAt(item.getCreatedAt());
            pull.setUpdatedAt(item.getUpdatedAt());
            if (item.getState().equals(CLOSED)) pull.setClosedAt(item.getClosedAt());
            if (item.getMergedAt() != null) pull.setMergedAt(item.getMergedAt());
            pulls.add(pull);
        }
        return pulls;
    }

    public RealmList<Branch> toBrancheshList(ArrayList<Branch> list) {
        RealmList<Branch> branches = new RealmList<>();
        for (Branch item : list) {
            Branch branch = new Branch();
            branch.setName(item.getName());
            branches.add(branch);
        }
        return branches;
    }

}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import java.util.ArrayList;

import code.diegohdez.githubapijava.BuildConfig;
import code.diegohdez.githubapijava.Model.Branch;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Request.API;
import code.diegohdez.navbottom.githubapijava.Adapter.BranchesFragment;
import io.realm.Realm;
import io.realm.RealmList;

public class BranchesRepo extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = BranchesRepo.class.getSimpleName();
    private Realm realm;
    private Context context;
    private BranchesFragment fragment;
    private long id;

    public BranchesRepo(FragmentActivity context, long id) {
        this.context = context;
        this.id = id;
        realm = Realm.getDefaultInstance();
    }

    public BranchesRepo(BranchesFragment fragment, long id) {
        this.fragment = fragment;
        this.id = id;
        realm = Realm.getDefaultInstance();
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        ANRequest request = API.getBranches(urls[0]);
        return request.executeForObjectList(Branch.class);
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            final RealmList<Branch> list = toBrancheshList((ArrayList<Branch>) response.getResult());
            if (realm.isClosed()) realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
                    repo.getBranches().addAll(list);
                    realm.insertOrUpdate(repo);
                }
            });
            if (BuildConfig.FLAVOR.equals("navBottom")) fragment.addBranches(list);
        } else {
            ANError anError = response.getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
    }

    private RealmList<Branch> toBrancheshList(ArrayList<Branch> list) {
        RealmList<Branch> branches = new RealmList<>();
        for (Branch item : list) {
            Branch branch = new Branch();
            branch.setName(item.getName());
            branches.add(branch);
        }
        return branches;
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Activity.ReposDetailActivity;
import code.diegohdez.githubapijava.BuildConfig;
import code.diegohdez.githubapijava.Model.Issue;
import code.diegohdez.githubapijava.Model.Pull;
import code.diegohdez.githubapijava.Model.PullInfo;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Request.API;
import code.diegohdez.navbottom.githubapijava.Adapter.IssuesFragment;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.API.CLOSED;

public class IssuesRepo extends AsyncTask<String, Void, ANResponse> {

    private static String TAG = IssuesRepo.class.getSimpleName();

    private Realm realm;
    private Context context;
    private IssuesFragment fragment;
    private long id;
    private int page;

    public IssuesRepo(FragmentActivity context, long id) {
        this.context = context;
        this.id = id;
        realm = Realm.getDefaultInstance();
    }

    public IssuesRepo(IssuesFragment context, long id) {
        this.fragment = context;
        this.id = id;
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        ANRequest request = API.getIssues(urls[0]);
        return request.executeForObjectList(Issue.class);
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            if (realm.isClosed()) realm = Realm.getDefaultInstance();
            final RealmList<Issue> list = toIssuesList((ArrayList<Issue>) response.getResult());
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Repo repo = realm.where(Repo.class).equalTo(Fields.ID, id).findFirst();
                    RealmList<Pull> pulls = repo.getPulls();
                    for (Issue item : list) {
                        Pull pull = pulls.where().equalTo(Fields.NUMBER, item.getNumber()).findFirst();
                        if (pull != null) {
                            PullInfo pullInfo = new PullInfo();
                            pullInfo.setState(pull.getState());
                            item.setPullInfo(pullInfo);
                        }
                    }
                    repo.getIssues().addAll(list);
                    realm.insertOrUpdate(repo);
                }
            });
            realm.close();
            if (BuildConfig.FLAVOR == "navBottom") ((IssuesFragment) fragment).addIssues(list);
            else ((ReposDetailActivity) context).addIssues(list);
        } else {
            ANError anError = response.getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
    }

    private RealmList<Issue> toIssuesList(ArrayList<Issue> list) {
        RealmList<Issue> issues = new RealmList();
        for (Issue item: list) {
            Issue issue = new Issue();
            issue.setId(item.getId());
            issue.setTitle(item.getTitle());
            issue.setDescription(item.getDescription());
            issue.setNumber(item.getNumber());
            issue.setState(item.getState());
            issue.setUser(item.getUser());
            if (item.getAssignee() != null) issue.setAssignee(item.getAssignee());
            issue.setCreatedAt(item.getCreatedAt());
            issue.setUpdatedAt(item.getUpdatedAt());
            if (issue.getState().equals(CLOSED)) issue.setClosedAt(item.getClosedAt());
            issues.add(issue);
        }
        return issues;
    }

}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import code.diegohdez.githubapijava.Activity.ReposActivity;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Request.API;
import io.realm.Realm;
import io.realm.RealmList;

import static code.diegohdez.githubapijava.Utils.Constants.Numbers.PAGE_ONE;

public class SearchRepo extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = SearchRepo.class.getSimpleName();

    private Context context;
    private Realm realm;
    private int page;

    public SearchRepo(ReposActivity context) {
        this.context = context;
        this.page = PAGE_ONE;
        this.realm = Realm.getDefaultInstance();
    }

    public SearchRepo(ReposActivity context, int page) {
        this.context = context;
        this.page = page;
        this.realm = Realm.getDefaultInstance();
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        ANRequest request = API.searchInRepo(urls[0] + "&page=" + page);
        return request.executeForJSONObject();
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            Log.i(TAG, response
                    .getOkHttpResponse()
                    .body().toString());
            JsonParser parser = new JsonParser();
            JsonObject result = parser
                    .parse(response
                            .getResult().toString())
                    .getAsJsonObject();
            RealmList<Repo> repos = toReposList(result.getAsJsonArray("items"));
            long total_repos = result.get("total_count").getAsLong();
            if (realm.isClosed()) realm = Realm.getDefaultInstance();
            final RealmList<Repo> finalRepos = repos;
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.insertOrUpdate(finalRepos);
                }
            });
            realm.close();
            if (page > PAGE_ONE) ((ReposActivity) context).addSearch(repos);
            else ((ReposActivity) context).initSearch(repos, total_repos);
        } else {
            ANError anError = response.getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
    }

    private RealmList<Repo> toReposList(JsonArray items) {
        RealmList<Repo> repos = new RealmList<>();
        Gson gson = new Gson();
        for (int i = 0; i < items.size(); i++){
            Repo repo = gson.fromJson(items.get(i), Repo.class);
            repos.add(repo);
        }
        return repos;
    }
}

package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;

import java.util.ArrayList;

import code.diegohdez.githubapijava.Activity.CommitsActivity;
import code.diegohdez.githubapijava.Model.Commit;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Constants.Fields;
import code.diegohdez.githubapijava.Utils.Request.API;
import io.realm.Realm;
import io.realm.RealmList;

public class CommitsBranch extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = CommitsBranch.class.getSimpleName();

    private long id;
    private String branch;
    private Realm realm;
    private Context context;
    private int page;

    public CommitsBranch(CommitsActivity context, long id, String branch) {
        this.context = context;
        this.id = id;
        this.branch = branch;
        realm = Realm.getDefaultInstance();
        this.page = 1;
    }

    public CommitsBranch(CommitsActivity context, long id, String branch, int page) {
        this.context = context;
        this.id = id;
        this.branch = branch;
        realm = Realm.getDefaultInstance();
        this.page = page;
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        ANRequest request =  (page > 1) ? API.getCommits(urls[0] + "&page=" + page) :  API.getCommits(urls[0]);
        return request.executeForObjectList(Commit.class);
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            final RealmList<Commit> list = toCommitList((ArrayList<Commit>) response.getResult());
            if (realm.isClosed()) realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Repo repo = realm.where(Repo.class)
                            .equalTo(Fields.ID, id)
                            .findFirst();
                    repo.getBranches()
                            .where()
                            .equalTo(Fields.BRANCH_NAME, branch)
                            .findFirst()
                            .getCommits()
                            .addAll(list);
                    realm.insertOrUpdate(repo);
                }
            });
            if (page > 1) ((CommitsActivity) context).addCommits(list);
            else ((CommitsActivity) context).setCommits(list);
        }
    }

    private RealmList<Commit> toCommitList (ArrayList<Commit> commits) {
        RealmList<Commit> list = new RealmList<>();
        for (Commit commit : commits) {
            Commit item = new Commit();
            item.setSha(commit.getSha());
            item.setAuthor(commit.getAuthor());
            item.setCommitInfo(commit.getCommitInfo());
            list.add(item);
        }
        return list;
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import code.diegohdez.githubapijava.Activity.ReposActivity;
import code.diegohdez.githubapijava.Utils.Request.API;

import static code.diegohdez.githubapijava.Utils.Constants.API.FORK_REPO_SUCCESS;

public class ForkRepo extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = ForkRepo.class.getSimpleName();

    private Context context;
    private String name;

    public ForkRepo(ReposActivity context, String name) {
        this.context = context;
        this.name = name;
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        return API.forkRepo(urls[0]).executeForOkHttpResponse();
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            if (response.getOkHttpResponse().code() == FORK_REPO_SUCCESS) ((ReposActivity) context).displayMessage("Fork success", name);
        } else {
            ANError anError = response.getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;

import code.diegohdez.githubapijava.Activity.ReposActivity;
import code.diegohdez.githubapijava.Model.Repo;
import code.diegohdez.githubapijava.Utils.Request.API;
import io.realm.Realm;

public class RepoInfo extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = Repo.class.getSimpleName();

    private Realm realm;
    private Context context;

    public RepoInfo(ReposActivity context) {
        realm = Realm.getDefaultInstance();
        this.context = context;
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        ANRequest request = API.onRepoEvenListener(urls[0]);
        return (ANResponse<Repo>) request.executeForObject(Repo.class);
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            final Repo repo = (Repo) response.getResult();
            if (realm.isClosed())
                realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insertOrUpdate(repo);
                }
            });
            realm.close();
            ((ReposActivity) context).updateCounter(repo.getName());

        }
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import code.diegohdez.githubapijava.Activity.GetTokenActivity;
import code.diegohdez.githubapijava.Manager.AppManager;
import code.diegohdez.githubapijava.Utils.Request.API;
import okhttp3.Response;

import static code.diegohdez.githubapijava.Utils.Constants.API.AUTH_SUCCESS;

public class GetToken extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = GetToken.class.getSimpleName();
    private GetTokenActivity context;
    private String token;

    public GetToken(GetTokenActivity context, String token) {
        this.context = context;
        this.token = token;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected ANResponse doInBackground(String... strings) {
        ANRequest request = API.getToken(strings[0], token);
        return request.executeForJSONObject();
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        String message = "";
        int code;
        if (response.isSuccess()) {
            Response anResponse = response.getOkHttpResponse();
            code = anResponse.code();
            if (code == AUTH_SUCCESS) {
                message = "Auth successfully";
            }
            AppManager.getOurInstance().setToken(token);
        } else {
            ANError error = response.getError();
            message = "Error: " + error.getErrorDetail() + "\n" +
                    "Body: " + error.getErrorBody() + "\n" +
                    "Message: " + error.getMessage() + "\n" +
                    "Code: " + error.getErrorCode();
            Log.e(TAG, message);
            code = error.getErrorCode();
        }
        GetTokenActivity.responseMessage(context, message, code);
    }
}


package code.diegohdez.githubapijava.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;

import code.diegohdez.githubapijava.Activity.ReposActivity;
import code.diegohdez.githubapijava.Utils.Request.API;

public class StarRepo extends AsyncTask<String, Void, ANResponse> {

    private static final String TAG = StarRepo.class.getSimpleName();
    private Context context;
    private boolean isStarred;
    private String name;

    public StarRepo (boolean isStarred, ReposActivity context, String name) {
        this.isStarred = isStarred;
        this.context = context;
        this.name = name;
    }

    @Override
    protected ANResponse doInBackground(String... urls) {
        if (isStarred) return API.unStarRepo(urls[0]).executeForOkHttpResponse();
        else return API.starRepo(urls[0]).executeForOkHttpResponse();
    }

    @Override
    protected void onPostExecute(ANResponse response) {
        super.onPostExecute(response);
        if (response.isSuccess()) {
            String message = "";
            if(isStarred) {
                message = "Unstar repo successfully";
            } else message = "Star repo successfully";
            ((ReposActivity) context).updateRepoAfterStarred(!isStarred, name, message);
        } else {
            ANError anError = response.getError();
            String message = "Delete: " + "\n" +
                    "Error: " + anError.getErrorDetail() + "\n" +
                    "Body: " + anError.getErrorBody() + "\n" +
                    "Message: " + anError.getMessage() + "\n" +
                    "Code: " + anError.getErrorCode();
            Log.e(TAG, message);
        }
    }
}


package code.diegohdez.githubapijava.Model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Pull extends RealmObject {

    @PrimaryKey
    private long id;
    private String title;
    @SerializedName("body")
    private String description;
    private long number;
    private String state;
    private Owner user;
    private Owner assignee;
    @SerializedName("closed_at")
    private Date closedAt;
    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("updated_at")
    private Date updatedAt;
    @SerializedName("merged_at")
    private Date mergedAt;

    public Pull () { }

    public Pull(long id,
                String title,
                String description,
                long number,
                String state,
                Owner user,
                Owner assignee,
                Date closedAt,
                Date createdAt,
                Date updatedAt,
                Date mergedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.number = number;
        this.state = state;
        this.user = user;
        this.assignee = assignee;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.mergedAt = mergedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Owner getUser() {
        return user;
    }

    public void setUser(Owner user) {
        this.user = user;
    }

    public Owner getAssignee() {
        return assignee;
    }

    public void setAssignee(Owner assignee) {
        this.assignee = assignee;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(Date mergedAt) {
        this.mergedAt = mergedAt;
    }

}


package code.diegohdez.githubapijava.Model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Author extends RealmObject {

    @PrimaryKey
    private long id;
    private String login;

    public Author() {}

    public Author(long id, String login) {
        this.id = id;
        this.login = login;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}


package code.diegohdez.githubapijava.Model;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Commit extends RealmObject {

    @PrimaryKey
    private String id = UUID.randomUUID().toString();
    private String sha;
    @SerializedName("commit")
    private CommitInfo commitInfo;
    private Author author;

    public Commit () {}

    public Commit (String sha, CommitInfo commitInfo, Author author) {
        this.sha = sha;
        this.commitInfo = commitInfo;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getSha() {
        return sha;
    }

    public void setCommitInfo(CommitInfo commitInfo) {
        this.commitInfo = commitInfo;
    }

    public CommitInfo getCommitInfo() {
        return commitInfo;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Author getAuthor() {
        return author;
    }
}


package code.diegohdez.githubapijava.Model;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Branch extends RealmObject {

    @PrimaryKey
    private String id = UUID.randomUUID().toString();
    private String name;
    private RealmList<Commit> commits;

    public Branch () { }

    public Branch (String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setCommits(RealmList<Commit> commits) {
        this.commits = commits;
    }

    public RealmList<Commit> getCommits() {
        return commits;
    }
}

package code.diegohdez.githubapijava.Model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Repo extends RealmObject{
    @PrimaryKey
    private long id;
    private String name;
    @SerializedName("full_name")
    private String fullName;
    private String description;
    @SerializedName("forks_count")
    private long forks;
    @SerializedName("stargazers_count")
    private long stars;
    @SerializedName("watchers_count")
    private long watchers;
    @SerializedName("pushed_at")
    private Date pushedAt;
    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("updated_at")
    private Date updatedAt;
    @SerializedName("subscribers_count")
    private long subscribers;
    private Owner owner;
    private RealmList<Issue> issues;
    private RealmList<Pull> pulls;
    private RealmList<Branch> branches;

    public Repo() { }

    public Repo(long id,
                String name,
                String fullName,
                String description,
                long forks,
                long stars,
                long watchers,
                Date pushedAt,
                Date createdAt,
                Date updatedAt,
                long subscribers,
                Owner owner,
                RealmList<Issue> issues,
                RealmList<Pull> pulls,
                RealmList<Branch> branches) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.forks = forks;
        this.stars = stars;
        this.watchers = watchers;
        this.pushedAt = pushedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.subscribers = subscribers;
        this.owner = owner;
        this.issues = issues;
        this.pulls = pulls;
        this.branches = branches;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getForks() {
        return forks;
    }

    public void setForks(long forks) {
        this.forks = forks;
    }

    public long getStars() {
        return stars;
    }

    public void setStars(long stars) {
        this.stars = stars;
    }

    public long getWatchers() {
        return watchers;
    }

    public void setWatchers(long watchers) {
        this.watchers = watchers;
    }

    public Date getPushedAt() {
        return pushedAt;
    }

    public void setPushedAt(Date pushedAt) {
        this.pushedAt = pushedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(long subscribers) {
        this.subscribers = subscribers;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public RealmList<Issue> getIssues() { return issues; }

    public void setIssues(RealmList<Issue> issues) { this.issues = issues; }

    public void setPulls (RealmList<Pull> pulls) { this.pulls = pulls; }

    public RealmList<Pull> getPulls() { return pulls; }

    public void setBranches(RealmList<Branch> branches) { this.branches = branches; }

    public RealmList<Branch> getBranches() {  return branches;
    }
}


package code.diegohdez.githubapijava.Model;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CommitInfo extends RealmObject {

    @PrimaryKey
    private String id = UUID.randomUUID().toString();
    private String message;
    private DateCommit author;

    public CommitInfo () {}

    public CommitInfo (String message, DateCommit date) {
        this.message = message;
        this.author = date;
    }

    public String getId() {
        return id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setAuthor(DateCommit author) {
        this.author = author;
    }

    public DateCommit getAuthor() {
        return author;
    }
}


package code.diegohdez.githubapijava.Model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Issue extends RealmObject {

    @PrimaryKey
    private long id;
    private String title;
    @SerializedName("body")
    private String description;
    private String state;
    private long number;
    private Owner user;
    private Owner assignee;
    @SerializedName("closed_at")
    private Date closedAt;
    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("updated_at")
    private Date updatedAt;
    private PullInfo pullInfo;

    public Issue() { }

    public Issue(long id,
                 String title,
                 String description,
                 String state,
                 long number,
                 Owner user,
                 Owner assignee,
                 Date closedAt,
                 Date createdAt,
                 Date updatedAt,
                 PullInfo pullInfo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.state = state;
        this.number = number;
        this.user = user;
        this.assignee = assignee;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pullInfo = pullInfo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public Owner getUser() {
        return user;
    }

    public void setUser(Owner user) {
        this.user = user;
    }

    public Owner getAssignee() {
        return assignee;
    }

    public void setAssignee(Owner assignee) {
        this.assignee = assignee;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPullInfo(PullInfo pullInfo) { this.pullInfo = pullInfo; }

    public PullInfo getPullInfo() { return pullInfo; }
}


package code.diegohdez.githubapijava.Model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Owner extends RealmObject{

    @PrimaryKey
    private long id;
    private String login;
    @SerializedName("public_repos")
    private int repos;

    public Owner() { }

    public Owner(long id, String login, int repos) {
        this.id = id;
        this.login = login;
        this.repos = repos;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setRepos(int repos) {
        this.repos = repos;
    }

    public int getRepos() {
        return repos;
    }
}


package code.diegohdez.githubapijava.Model;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class DateCommit extends RealmObject {

    @PrimaryKey
    private String id = UUID.randomUUID().toString();
    private String name;
    private Date date;

    public DateCommit() { }

    public DateCommit(String id, String name, Date date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}


package code.diegohdez.githubapijava.Model;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PullInfo extends RealmObject {

    @PrimaryKey
    private String id = UUID.randomUUID().toString();
    private String state;

    public PullInfo ( ) {}

    public PullInfo(String state) {
        this.state = state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}


package code.diegohdez.githubapijava.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import code.diegohdez.githubapijava.Builder.CommitsBuilder;
import code.diegohdez.githubapijava.Model.Commit;

public class DataOfCommits {

    private String sha;
    private String author;
    private String message;
    private Date date;

    public DataOfCommits() {}

    public DataOfCommits(CommitsBuilder builder) {
        this.sha = builder.getSha();
        this.author = builder.getAuthor();
        this.message = builder.getMessage();
        this.date = builder.getDate();
    }

    public String getSha() {
        return sha;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public static ArrayList<DataOfCommits> createList(List<Commit> commits) {
        ArrayList<DataOfCommits> list = new ArrayList<>();
        for (Commit commit : commits) {
            DataOfCommits item = new CommitsBuilder(commit.getSha())
                    .setAuthor(commit.getAuthor(), commit.getCommitInfo())
                    .setMessage(commit.getCommitInfo())
                    .build();
            list.add(item);
        }
        return list;
    }
}


package code.diegohdez.githubapijava.Data;

import java.util.ArrayList;
import java.util.List;

import code.diegohdez.githubapijava.Model.Branch;

public class DataOfBranches {

    private String name;

    public DataOfBranches() { }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ArrayList<DataOfBranches> createList(List<Branch> branches) {
        ArrayList<DataOfBranches> list = new ArrayList<>();
        for (Branch branch : branches) {
            DataOfBranches item = new DataOfBranches();
            item.setName(branch.getName());
            list.add(item);
        }
        return list;
    }
}


package code.diegohdez.githubapijava.Data;

import java.util.ArrayList;
import java.util.List;

import code.diegohdez.githubapijava.Builder.ReposBuilder;
import code.diegohdez.githubapijava.Model.Repo;

public class DataOfRepos {

    private long id;
    private String name;
    private String description;
    private long stars;
    private long watchers;
    private long forks;
    private long subscribers;

    public DataOfRepos (ReposBuilder builder) {
        this.id = builder.getId();
        this.name = builder.getName();
        this.description = builder.getDescription();
        this.stars = builder.getStars();
        this.watchers = builder.getWatchers();
        this.forks = builder.getForks();
        this.subscribers = builder.getSubscribers();
    }

    public DataOfRepos() { }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStars() {
        return stars;
    }

    public void setStars(long stars) {
        this.stars = stars;
    }

    public long getWatchers() {
        return watchers;
    }

    public void setWatchers(long watchers) {
        this.watchers = watchers;
    }

    public long getForks() {
        return forks;
    }

    public void setFork(int forks) {
        this.forks = forks;
    }

    public void setSubscribers(long subscribers) {
        this.subscribers = subscribers;
    }

    public long getSubscribers() {
        return subscribers;
    }

    public static ArrayList<DataOfRepos> createRepoList(List<Repo> repos) {
        ArrayList<DataOfRepos> list = new ArrayList<>();
        for (Repo repo: repos) {
            DataOfRepos item = new ReposBuilder(repo.getId())
                    .setName(repo.getName())
                    .setDescription(repo.getDescription())
                    .setWatchers(repo.getWatchers())
                    .setStars(repo.getStars())
                    .setForks(repo.getForks())
                    .setSubscribers(repo.getSubscribers())
                    .build();
            list.add(item);
        }
        return list;
    }
}


package code.diegohdez.githubapijava.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import code.diegohdez.githubapijava.Builder.PullsBuilder;
import code.diegohdez.githubapijava.Model.Pull;

public class DataOfPulls {

    private long id;
    private String title;
    private String description;
    private long number;
    private String state;
    private String user;
    private String assignee;
    private Date closedAt;
    private Date createdAt;
    private Date updatedAt;
    private Date mergedAt;

    public DataOfPulls() {}

    public DataOfPulls(PullsBuilder builder) {
        this.id = builder.getId();
        this.title = builder.getTitle();
        this.description = builder.getDescription();
        this.number = builder.getNumber();
        this.state = builder.getState();
        this.user = builder.getUser();
        this.assignee = builder.getAssignee();
        this.createdAt = builder.getCreatedAt();
        this.updatedAt = builder.getUpdatedAt();
        this.closedAt = builder.getClosedAt();
        this.mergedAt = builder.getMergedAt();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getNumber() {
        return number;
    }

    public String getState() {
        return state;
    }

    public String getUser() {
        return user;
    }

    public String getAssignee() {
        return assignee;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getMergedAt() {
        return mergedAt;
    }

    public static ArrayList<DataOfPulls> createList(List<Pull> pulls) {
        ArrayList<DataOfPulls> list = new ArrayList<>();
        for (Pull pull : pulls) {
            DataOfPulls item = new PullsBuilder(pull.getId())
                    .setTitle(pull.getTitle())
                    .setDescription(pull.getDescription())
                    .setNumber(pull.getNumber())
                    .setState(pull.getState())
                    .setUser(pull.getUser())
                    .setAssignee(pull.getAssignee())
                    .setCreatedAt(pull.getCreatedAt())
                    .setUpdatedAt(pull.getUpdatedAt())
                    .setClosedAt(pull.getClosedAt())
                    .setMergedAt(pull.getMergedAt())
                    .build();
            list.add(item);
        }
        return list;
    }
}


package code.diegohdez.githubapijava.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import code.diegohdez.githubapijava.Builder.IssuesBuilder;
import code.diegohdez.githubapijava.Model.Issue;

public class DataOfIssues {

    private long id;
    private String title;
    private String description;
    private String state;
    private long number;
    private String user;
    private String assignee;
    private Date closedAt;
    private Date createdAt;
    private Date updatedAt;
    private boolean isPull;
    private String pullState;

    public DataOfIssues() { }

    public DataOfIssues(IssuesBuilder builder) {
        this.id = builder.getId();
        this.title = builder.getTitle();
        this.description = builder.getDescription();
        this.state = builder.getState();
        this.number = builder.getNumber();
        this.user = builder.getUser();
        this.assignee = builder.getAssignee();
        this.closedAt = builder.getClosedAt();
        this.createdAt = builder.getCreatedAt();
        this.updatedAt = builder.getUpdatedAt();
        this.isPull = builder.isPull();
        this.pullState = builder.getPullState();
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getState() {
        return state;
    }

    public long getNumber() {
        return number;
    }

    public String getUser() {
        return user;
    }

    public String getAssignee() {
        return assignee;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public boolean isPull() { return isPull; }

    public String getPullState() { return pullState; }

    public static ArrayList<DataOfIssues> createList (List<Issue> issues) {
        ArrayList<DataOfIssues> list = new ArrayList<>();
        for (Issue issue :  issues) {
            DataOfIssues item = new IssuesBuilder(issue.getId())
                    .setTitle(issue.getTitle())
                    .setDescription(issue.getDescription())
                    .setNumber(issue.getNumber())
                    .setState(issue.getState())
                    .setUser(issue.getUser().getLogin())
                    .setAssignee(issue.getAssignee())
                    .setCreatedAt(issue.getCreatedAt())
                    .setUpdatedAt(issue.getUpdatedAt())
                    .setClosedAt(issue.getClosedAt())
                    .setIsPull(issue.getPullInfo())
                    .build();
            list.add(item);
        }
        return list;
    }
}


package code.diegohdez.githubapijava.Builder;

import java.util.Date;

import code.diegohdez.githubapijava.Data.DataOfPulls;
import code.diegohdez.githubapijava.Model.Owner;

public class PullsBuilder {

    private long id;
    private String title;
    private String description;
    private long number;
    private String state;
    private String user;
    private String assignee;
    private Date closedAt;
    private Date createdAt;
    private Date updatedAt;
    private Date mergedAt;

    public PullsBuilder (long id) {
        this.id = id;
    }

    public PullsBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public PullsBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public PullsBuilder setNumber(long number) {
        this.number = number;
        return this;
    }

    public PullsBuilder setState(String state) {
        this.state = state;
        return this;
    }

    public PullsBuilder setUser(Owner user) {
        this.user = user.getLogin();
        return this;
    }

    public PullsBuilder setAssignee(Owner assignee) {
        if (assignee != null) this.assignee = assignee.getLogin();
        return this;
    }

    public PullsBuilder setCreatedAt(Date createdAt) {
        this.createdAt =createdAt;
        return this;
    }

    public PullsBuilder setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public PullsBuilder setClosedAt(Date closedAt) {
        if (closedAt != null) this.closedAt = closedAt;
        return this;
    }

    public PullsBuilder setMergedAt(Date mergedAt) {
        if (mergedAt != null) this.mergedAt = mergedAt;
            return this;
    }

    public DataOfPulls build(){
        return new DataOfPulls(this);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getNumber() {
        return number;
    }

    public String getState() {
        return state;
    }

    public String getUser() {
        return user;
    }

    public String getAssignee() {
        return assignee;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getMergedAt() {
        return mergedAt;
    }
}


package code.diegohdez.githubapijava.Builder;

import android.util.Log;

import java.util.Date;

import code.diegohdez.githubapijava.Data.DataOfCommits;
import code.diegohdez.githubapijava.Model.Author;
import code.diegohdez.githubapijava.Model.CommitInfo;

public class CommitsBuilder {

    private String sha;
    private String author;
    private String message;
    private Date date;

    public CommitsBuilder(String sha) {
        this.sha = sha;
    }

    public CommitsBuilder setAuthor(Author author, CommitInfo commitInfo) {
        this.author = (author != null) ? author.getLogin() : commitInfo.getAuthor().getName();
        return this;
    }

    public CommitsBuilder setMessage(CommitInfo commitInfo) {
        this.message = commitInfo.getMessage();
        this.date = commitInfo.getAuthor().getDate();
        return this;
    }

    public DataOfCommits build(){
        return new DataOfCommits(this);
    }

    public String getSha() {
        return sha;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }
}


package code.diegohdez.githubapijava.Builder;

import java.util.Date;

import code.diegohdez.githubapijava.Data.DataOfIssues;
import code.diegohdez.githubapijava.Model.Owner;
import code.diegohdez.githubapijava.Model.PullInfo;

public class IssuesBuilder {
    private long id;
    private String title;
    private String description;
    private String state;
    private long number;
    private String user;
    private String assignee;
    private Date closedAt;
    private Date createdAt;
    private Date updatedAt;
    private boolean isPull;
    private String pullState;

    public IssuesBuilder (long id) {
        this.id = id;
    }

    public IssuesBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public IssuesBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public IssuesBuilder setNumber(long number) {
        this.number = number;
        return this;
    }

    public IssuesBuilder setState(String state) {
        this.state = state;
        return this;
    }

    public IssuesBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public IssuesBuilder setAssignee(Owner assignee) {
        if (assignee != null) this.assignee = assignee.getLogin();
        return this;
    }

    public IssuesBuilder setClosedAt(Date closedAt) {
        if (closedAt != null) this.closedAt = closedAt;
        return this;
    }

    public IssuesBuilder setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public IssuesBuilder setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public IssuesBuilder setIsPull(PullInfo pullInfo) {
        if (pullInfo != null) {
            this.isPull = true;
            this.pullState = pullInfo.getState();
        }
        else {
            this.isPull = false;
            this.pullState = "none";
        }
        return this;
    }

    public DataOfIssues build() {
        return new DataOfIssues(this);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getState() {
        return state;
    }

    public long getNumber() {
        return number;
    }

    public String getUser() {
        return user;
    }

    public String getAssignee() {
        return assignee;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public boolean isPull() { return isPull; }

    public String getPullState() { return pullState; }
}


package code.diegohdez.githubapijava.Builder;

import code.diegohdez.githubapijava.Data.DataOfRepos;

public class ReposBuilder {

    private long id;
    private String name;
    private String description;
    private long watchers;
    private long stars;
    private long forks;
    private long subscribers;

    public ReposBuilder (long id) {
        this.id = id;
    }

    public ReposBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ReposBuilder setDescription (String description) {
        this.description = description;
        return this;
    }

    public ReposBuilder setWatchers (long watchers) {
        this.watchers = watchers;
        return this;
    }

    public ReposBuilder setStars (long stars) {
        this.stars = stars;
        return this;
    }

    public ReposBuilder setForks (long forks) {
        this.forks = forks;
        return this;
    }

    public ReposBuilder setSubscribers (long subscribers) {
        this.subscribers = subscribers;
        return this;
    }

    public DataOfRepos build(){
        return new DataOfRepos(this);
    }

    public void setId(long id) { this.id = id; }

    public long getId() { return id; }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getWatchers() {
        return watchers;
    }

    public long getStars() {
        return stars;
    }

    public long getForks() {
        return forks;
    }

    public long getSubscribers() { return subscribers; }
}


package code.diegohdez.githubapijava.Migration;

import java.util.Date;

import code.diegohdez.githubapijava.Utils.Constants.Fields;
import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

import static code.diegohdez.githubapijava.Utils.Constants.Schema.AUTHOR_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.BRANCH_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.COMMIT_INFO_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.COMMIT_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.DATE_COMMIT_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.ISSUE_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.OWNER_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.PULL_INFO_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.PULL_SCHEMA;
import static code.diegohdez.githubapijava.Utils.Constants.Schema.REPO_SCHEMA;

public class Migration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 1) {
            realm.getSchema().get(REPO_SCHEMA)
                    .addPrimaryKey(Fields.ID);
            oldVersion++;
        }

        if (oldVersion == 2) {
            schema.create(OWNER_SCHEMA)
                    .addField(Fields.LOGIN, String.class)
                    .addField(Fields.ID, long.class, FieldAttribute.PRIMARY_KEY);

            realm.getSchema()
                    .get(REPO_SCHEMA)
                    .addField("_new_id", long.class)
                    .removeField(Fields.ID)
                    .renameField("_new_id", Fields.ID)
                    .addPrimaryKey(Fields.ID)
                    .addRealmObjectField(Fields.OWNER, schema.get(OWNER_SCHEMA));
            oldVersion++;
        }

        if (oldVersion == 3) {
            schema.get(REPO_SCHEMA)
                    .renameField(Fields.OLD_FULL_NAME, Fields.FULL_NAME)
                    .renameField(Fields.OLD_PUSHED_AT, Fields.PUSHED_AT)
                    .renameField(Fields.OLD_CREATED_AT, Fields.CREATED_AT)
                    .renameField(Fields.OLD_UPDATED_AT, Fields.UPDATED_AT)
                    .removeField("forks_count")
                    .removeField("stargazers_count")
                    .removeField("watchers_count")
                    .removeField("subscribers_count")
                    .addField(Fields.FORKS, long.class)
                    .addField(Fields.STARS, long.class)
                    .addField(Fields.WATCHERS, long.class)
                    .addField(Fields.SUBSCRIBERS, long.class);
            oldVersion++;
        }

        if (oldVersion == 4) {
            schema.get(OWNER_SCHEMA)
                    .addField(Fields.REPOS, long.class);
            oldVersion++;
        }

        if (oldVersion == 5) {
            schema.create(ISSUE_SCHEMA)
                    .addField(Fields.ID, long.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.TITLE, String.class)
                    .addField(Fields.DESCRIPTION, String.class)
                    .addField(Fields.NUMBER, long.class)
                    .addField(Fields.STATE, String.class)
                    .addRealmObjectField(Fields.USER, schema.get(OWNER_SCHEMA))
                    .addRealmObjectField(Fields.ASSIGNEE, schema.get(OWNER_SCHEMA))
                    .addField(Fields.CLOSED_AT, Date.class)
                    .addField(Fields.CREATED_AT, Date.class)
                    .addField(Fields.UPDATED_AT, Date.class);

            schema.get(REPO_SCHEMA)
                    .addRealmListField(Fields.ISSUES, schema.get(ISSUE_SCHEMA));

            oldVersion++;
        }

        if (oldVersion == 6) {
            schema.create(PULL_SCHEMA)
                    .addField(Fields.ID, long.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.TITLE, String.class)
                    .addField(Fields.DESCRIPTION, String.class)
                    .addField(Fields.NUMBER, long.class)
                    .addField(Fields.STATE, String.class)
                    .addRealmObjectField(Fields.USER, schema.get(OWNER_SCHEMA))
                    .addRealmObjectField(Fields.ASSIGNEE, schema.get(OWNER_SCHEMA))
                    .addField(Fields.CLOSED_AT, Date.class)
                    .addField(Fields.CREATED_AT, Date.class)
                    .addField(Fields.UPDATED_AT, Date.class)
                    .addField(Fields.MERGED_AT, Date.class);

            schema.create(PULL_INFO_SCHEMA)
                    .addField(Fields.ID, String.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.PULL_STATE, String.class);

            schema.get(REPO_SCHEMA)
                    .addRealmListField(Fields.PULLS, schema.get(PULL_SCHEMA));

            schema.get(ISSUE_SCHEMA)
                    .addRealmObjectField(Fields.PULL_INFO, schema.get(PULL_INFO_SCHEMA));

            oldVersion++;
        }

        if (oldVersion == 7) {
            schema.create(BRANCH_SCHEMA)
                    .addField(Fields.ID, String.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.BRANCH_NAME, String.class);

            schema.get(REPO_SCHEMA)
                    .addRealmListField(Fields.BRANCHES, schema.get(BRANCH_SCHEMA));

            oldVersion++;
        }

        if (oldVersion == 8) {
            schema.create(COMMIT_INFO_SCHEMA)
                    .addField(Fields.ID, String.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.MESSAGE, String.class)
                    .addField(Fields.DATE, Date.class);

            schema.create(COMMIT_SCHEMA)
                    .addField(Fields.ID, String.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.SHA, String.class)
                    .addRealmObjectField(Fields.AUTHOR, schema.get(OWNER_SCHEMA))
                    .addRealmObjectField(Fields.COMMIT_INFO, schema.get(COMMIT_INFO_SCHEMA));

            schema.get(BRANCH_SCHEMA)
                    .addRealmListField(Fields.COMMITS, schema.get(COMMIT_SCHEMA));

            oldVersion++;
        }

        if (oldVersion == 9) {
            schema.get(COMMIT_SCHEMA)
                    .addField("_new_author_commit", String.class)
                    .removeField(Fields.AUTHOR)
                    .renameField("_new_author_commit", "author");
            oldVersion++;
        }

        if (oldVersion == 10) {
            schema.create(AUTHOR_SCHEMA)
                    .addField(Fields.ID, long.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.LOGIN, String.class);

            schema.create(DATE_COMMIT_SCHEMA)
                    .addField(Fields.ID, String.class, FieldAttribute.PRIMARY_KEY)
                    .addField(Fields.AUTHOR_NAME, String.class)
                    .addField(Fields.DATE, Date.class);

            schema.get(COMMIT_SCHEMA)
                    .addRealmObjectField("_new_author_commit", schema.get(AUTHOR_SCHEMA))
                    .removeField(Fields.AUTHOR)
                    .renameField("_new_author_commit", Fields.AUTHOR);

            schema.get(COMMIT_INFO_SCHEMA)
                    .addRealmObjectField(Fields.AUTHOR, schema.get(DATE_COMMIT_SCHEMA))
                    .removeField(Fields.DATE);

            oldVersion++;
        }
    }
}


