package com.android.popmoviestwo;

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

        assertEquals("com.android.popmoviestwo", appContext.getPackageName());
    }
}


package com.android.popmoviestwo;

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

package com.android.popmoviestwo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mmalla on 16/01/18.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.MyReviewViewHolder> {

    private Context mContext;
    private List<String> reviewsList;

    public ReviewsAdapter(Context context, List<String> listOfReviews) {
        this.reviewsList = listOfReviews;
        mContext = context;
    }

    public class MyReviewViewHolder extends RecyclerView.ViewHolder{

        public TextView reviewText;

        public MyReviewViewHolder(View itemView) {
            super(itemView);
            reviewText = (TextView) itemView.findViewById(R.id.review_item);
        }
    }

    @Override
    public MyReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.review_list_item, parent, false);
        return new MyReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyReviewViewHolder holder, int position) {
        String reviewStr = reviewsList.get(position);
        TextView reviewTextView = (TextView) holder.reviewText.findViewById(R.id.review_item);
        reviewTextView.setText("Review " + (position+1) + "\n" + reviewStr);
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }
}


package com.android.popmoviestwo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.popmoviestwo.FavMoviesAdapter.FavMoviesAdapterOnClickListener;
import com.android.popmoviestwo.MoviesAdapter.MoviesAdapterOnClickListener;
import com.android.popmoviestwo.data.MovieContract;
import com.android.popmoviestwo.utils.MoviesListJsonUtils;
import com.android.popmoviestwo.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, MoviesAdapterOnClickListener, FavMoviesAdapterOnClickListener {

    private RecyclerView recyclerView;
    private MoviesAdapter moviesAdapter;
    private FavMoviesAdapter favMoviesAdapter;
    private TextView mErrorMessage;
    private ProgressBar mLoadingIcon;
    private GridLayoutManager mLayoutManager;
    private EndlessRecyclerViewScrollListener scrollListener;
    private boolean fav_flag = false;
    private final static String FAV_MOVIES_FLAG = "fav_flag";
    private final static String MOVIE_LIST_SAVE_INSTANCE = "movie_list";
    private final static String PATH_POPULAR_PARAM = "popular";
    private final static String PATH_TOP_RATED_PARAM = "top_rated";
    private final static String PAGE_NUMBER = "1";

    private ArrayList<Movie> moviesList = new ArrayList<>();

    /*
    * The columns of data that we are interested in displaying within our MainActivity's list of
    * movie data.
    */
    private static final String[] MAIN_MOVIE_PROJECTION = {
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE_PATH,
            MovieContract.MovieEntry.COLUMN_FAVORITE
    };

    public static final int INDEX_MOVIE_ID = 0;
    public static final int INDEX_MOVIE_IMG = 2;

    private static final int ID_MOVIE_LOADER = 77;


    private void showLoading() {
        recyclerView.setVisibility(View.INVISIBLE);
        mLoadingIcon.setVisibility(View.VISIBLE);
    }

    private void showMovieThumbnails() {
        mLoadingIcon.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mErrorMessage = (TextView) findViewById(R.id.error_message);
        mLoadingIcon = (ProgressBar) findViewById(R.id.loading_icon);

        if (savedInstanceState != null) {
            this.fav_flag = savedInstanceState.getBoolean(FAV_MOVIES_FLAG);
        }
        if (fav_flag) {
            getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
        } else {
            if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_LIST_SAVE_INSTANCE)) {
                new FetchMoviesList().execute(PATH_POPULAR_PARAM, PAGE_NUMBER);
                showGeneralMovieLists(PATH_POPULAR_PARAM);
            } else {
                moviesList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_SAVE_INSTANCE);
                showGeneralMovieLists(PATH_POPULAR_PARAM);
            }
        }
    }

    private void showGeneralMovieLists(final String path_param) {
        /**
         * This is to automatically decide based on the width of the device how many noOfColumns are
         * possible in one row for the display of movie thumbnails
         */
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        mLayoutManager = new GridLayoutManager(getApplicationContext(), noOfColumns);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        favMoviesAdapter = new FavMoviesAdapter(this, this);
        moviesAdapter = new MoviesAdapter(this, moviesList, this);
        recyclerView.setAdapter(moviesAdapter);
        scrollListener = new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                new FetchMoviesList().execute(path_param, Integer.toString(page));
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
        showMovieThumbnails();
    }

    @Override
    public void onClick(Movie movie) {
        Intent movieDetailsIntent = new Intent(this, MovieDetailsActivity.class);
        movieDetailsIntent.putExtra(Intent.EXTRA_TEXT, movie.getMovieId());
        startActivity(movieDetailsIntent);
    }

    @Override
    public void onClick(Cursor cursor, int position) {
        Intent movieDetailsIntent = new Intent(this, MovieDetailsActivity.class);
        cursor.moveToPosition(position);
        movieDetailsIntent.putExtra(Intent.EXTRA_TEXT, cursor.getString(INDEX_MOVIE_ID));
        startActivity(movieDetailsIntent);
    }

    private class FetchMoviesList extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            recyclerView.setVisibility(View.INVISIBLE);
            mLoadingIcon.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String path = params[0];
            String page = params[1];

            /**
             * Get the URL to fetch the Popular movies
             */
            URL moviesListUrl = NetworkUtils.buildUrl(path, page);

            try {
                String jsonPopularMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesListUrl);
                return MoviesListJsonUtils.getSimpleMoviesInformationFromJson(jsonPopularMoviesResponse);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> mList) {
            if (mList != null) {
                mErrorMessage.setVisibility(View.INVISIBLE);
                showMovieThumbnails();
                moviesList.addAll(mList);
                moviesAdapter.notifyDataSetChanged();
            } else {
                showLoading();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_by_top_rated) {
            /**
             * Clear the list so it can retrieve and display a new set of movies
             */
            moviesList.clear();
            fav_flag = false;
            getSupportLoaderManager().destroyLoader(ID_MOVIE_LOADER);
            new FetchMoviesList().execute(PATH_TOP_RATED_PARAM, PAGE_NUMBER);
            showGeneralMovieLists(PATH_TOP_RATED_PARAM);
            return true;
        }
        if (id == R.id.action_sort_by_popular) {
            /**
             * Clear the list so it can retrieve and display a new set of movies
             */
            moviesList.clear();
            fav_flag = false;
            getSupportLoaderManager().destroyLoader(ID_MOVIE_LOADER);
            new FetchMoviesList().execute(PATH_POPULAR_PARAM, PAGE_NUMBER);
            showGeneralMovieLists(PATH_POPULAR_PARAM);
            return true;
        }
        if (id == R.id.favorites_list) {
            fav_flag = true;
            displayFavoriteThumbnails();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_MOVIE_LOADER:
                Uri favMoviesList = MovieContract.MovieEntry.CONTENT_URI;

                String selectionArgs = MovieContract.MovieEntry.COLUMN_FAVORITE + " = 1";

                return new CursorLoader(this,
                        favMoviesList,
                        MAIN_MOVIE_PROJECTION,
                        selectionArgs,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        moviesList.clear();
        int position = 0;
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), noOfColumns);
        recyclerView.setLayoutManager(mLayoutManager);
        favMoviesAdapter = new FavMoviesAdapter(this, this);
        recyclerView.setAdapter(favMoviesAdapter);
        favMoviesAdapter.swapCursor(data);
        if (data.getPosition() == RecyclerView.NO_POSITION) position = 0;
        recyclerView.smoothScrollToPosition(position);
        if (data.getCount() != 0) showMovieThumbnails();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        favMoviesAdapter.swapCursor(null);
    }

    private void displayFavoriteThumbnails() {
        getSupportLoaderManager().initLoader(ID_MOVIE_LOADER, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST_SAVE_INSTANCE, moviesList);
        outState.putBoolean(FAV_MOVIES_FLAG, fav_flag);
        super.onSaveInstanceState(outState);
    }
}


package com.android.popmoviestwo;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mmalla on 05/01/18.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    private Context mContext;

    private final MoviesAdapter.MoviesAdapterOnClickListener mListener;

    private List<Movie> moviesList;

    public interface MoviesAdapterOnClickListener{
        void onClick(Movie movie);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView movie_thumbnail;

        public MyViewHolder(View view) {
            super(view);
            movie_thumbnail = (ImageView) view.findViewById(R.id.movie_image);
        }
    }

    public MoviesAdapter(Context context, List<Movie> moviesList, MoviesAdapter.MoviesAdapterOnClickListener listener) {
        this.moviesList = moviesList;
        mContext = context;
        mListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Movie movie = moviesList.get(holder.getAdapterPosition());

        ImageView movie_thumbnail = (ImageView) holder.movie_thumbnail.findViewById(R.id.movie_image);

        movie_thumbnail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Movie movie = moviesList.get(position);
                mListener.onClick(movie);
            }
        });

        try{
            String IMAGE_MOVIE_URL = "http://image.tmdb.org/t/p/w185//";
            Picasso.with(mContext).load(IMAGE_MOVIE_URL + movie.getMovieImgPath()).error(R.drawable.user_placeholder_error).into(movie_thumbnail);
        } catch(IllegalArgumentException e){
            movie_thumbnail.setImageResource(R.drawable.user_placeholder_error);
        }
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}


package com.android.popmoviestwo;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by mmalla on 17/01/18.
 */

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.MyTrailerViewHolder> {

    private Context mContext;
    private List<String> trailersList;
    private final TrailersAdapter.TrailerAdapterOnClickListener mListener;

    public interface TrailerAdapterOnClickListener {
        void onClick(String trailerStr);
    }

    public TrailersAdapter(Context context, List<String> trailerList, TrailerAdapterOnClickListener mListener) {
        mContext = context;
        this.trailersList = trailerList;
        this.mListener = mListener;
    }

    @Override
    public MyTrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.trailer_list_item, parent, false);
        return new MyTrailerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTrailerViewHolder holder, final int position) {

        ImageView trailerImg = (ImageView) holder.trailerImg.findViewById(R.id.trailerImg);

        String id = trailersList.get(holder.getAdapterPosition());

        try {
            String IMG_STR_CONSTRUCT = "https://img.youtube.com/vi/";
            Picasso.with(mContext).load(IMG_STR_CONSTRUCT + id + "/0.jpg").error(R.drawable.user_placeholder_error).into(trailerImg);
        } catch (IllegalArgumentException e) {
            trailerImg.setImageResource(R.drawable.user_placeholder_error);
        }

        trailerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trailerStr = trailersList.get(holder.getAdapterPosition());
                mListener.onClick(trailerStr);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trailersList.size();
    }

    public class MyTrailerViewHolder extends RecyclerView.ViewHolder {

        public ImageView trailerImg;

        public MyTrailerViewHolder(View itemView) {
            super(itemView);
            trailerImg = (ImageView) itemView.findViewById(R.id.trailerImg);
        }
    }

}


package com.android.popmoviestwo;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by mmalla on 14/01/18.
 */

public class FavMoviesAdapter extends RecyclerView.Adapter<FavMoviesAdapter.FavMoviewsViewHolder> {

    private final Context mContext;

    private final FavMoviesAdapter.FavMoviesAdapterOnClickListener mListener;

    public interface FavMoviesAdapterOnClickListener{
        void onClick(Cursor cursor, int position);
    }

    private Cursor mCursor;

    public FavMoviesAdapter(@NonNull Context mContext, FavMoviesAdapterOnClickListener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @Override
    public FavMoviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutId;
        layoutId = R.layout.movie_list_row;
        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        view.setFocusable(true);

        return new FavMoviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FavMoviewsViewHolder viewHolder, final int position) {

        mCursor.moveToPosition(position);
        String img = mCursor.getString(MainActivity.INDEX_MOVIE_IMG);

        try {
            String IMAGE_MOVIE_URL = "http://image.tmdb.org/t/p/w185//";
            Picasso.with(mContext).load(IMAGE_MOVIE_URL + img).error(R.drawable.user_placeholder_error).into(viewHolder.movieThumbnail);
        } catch (IllegalArgumentException e) {
            viewHolder.movieThumbnail.setImageResource(R.drawable.user_placeholder_error);
        }

        viewHolder.movieThumbnail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCursor.getPosition();
                mListener.onClick(mCursor, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * Swaps the cursor used by the ForecastAdapter for its weather data. This method is called by
     * MainActivity after a load has finished, as well as when the Loader responsible for loading
     * the weather data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ForecastAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public class FavMoviewsViewHolder extends RecyclerView.ViewHolder {

        final ImageView movieThumbnail;

        public FavMoviewsViewHolder(View itemView) {
            super(itemView);
            movieThumbnail = (ImageView) itemView.findViewById(R.id.movie_image);
        }
    }
}


package com.android.popmoviestwo;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by mmalla on 13/03/18.
 */

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleThreshold = 5;
    // The current offset index of data you have loaded
    private int currentPage = 1;
    // The total number of items in the dataset after the last load
    private int previousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;
    // Sets the starting page index
    private int startingPageIndex = 0;

    RecyclerView.LayoutManager mLayoutManager;

    public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            }
            else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(null);
            // get maximum element within the list
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
        } else if (mLayoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
            currentPage++;
            onLoadMore(currentPage, totalItemCount, view);
            loading = true;
        }
    }

    // Call this method whenever performing new searches
    public void resetState() {
        this.currentPage = this.startingPageIndex;
        this.previousTotalItemCount = 0;
        this.loading = true;
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);

}

package com.android.popmoviestwo;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

import com.android.popmoviestwo.data.MovieContract;
import com.android.popmoviestwo.utils.MovieDetailsJsonUtils;
import com.android.popmoviestwo.utils.MovieReviewsJsonUtils;
import com.android.popmoviestwo.utils.MovieTrailersJsonUtils;
import com.android.popmoviestwo.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MovieDetailsActivity extends AppCompatActivity implements TrailersAdapter.TrailerAdapterOnClickListener {

    private Movie movie;
    private TextView movieTitle;
    private ImageView moviePoster;
    private TextView movieReleaseDate;
    private TextView movieOverview;
    private TextView movieUserRating;
    private TextView mFavButton;

    private RecyclerView recyclerViewReviews;
    private RecyclerView recyclerViewTrailers;
    private TrailersAdapter trailersAdapter;
    private ReviewsAdapter reviewsAdapter;

    private String mTrailerSummary;
    private String YOUTUBE_CONSTRUCT = "vnd.youtube:";
    private String BROWSER_CONSTRUCT = "http://www.youtube.com/watch?v=";
    private String RATING_OUT_OF_TEN = "/10";
    private final String POPMOVIES_SHARE_HASHTAG = "#PopMovies";
    private final String IMAGE_MOVIE_URL = "http://image.tmdb.org/t/p/w500//";
    private String SAVE_INSTANCE_KEY = "movie_detail";

    private String TAG = MovieDetailsActivity.class.getSimpleName();

    private void showReviews() {
        recyclerViewReviews.setVisibility(View.VISIBLE);
    }

    private void showTrailers() {
        recyclerViewTrailers.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().setDisplayShowHomeEnabled(true);
        setContentView(R.layout.movie_details_activity);

        movieTitle = (TextView) findViewById(R.id.movie_orig_title);
        movieOverview = (TextView) findViewById(R.id.movie_overview);
        moviePoster = (ImageView) findViewById(R.id.movie_image);
        movieReleaseDate = (TextView) findViewById(R.id.movie_release_date);
        movieUserRating = (TextView) findViewById(R.id.movie_user_rating);
        mFavButton = (TextView) findViewById(R.id.mark_as_favorite);
        recyclerViewReviews = (RecyclerView) findViewById(R.id.recyclerview_reviews);
        recyclerViewTrailers = (RecyclerView) findViewById(R.id.recyclerview_trailers);

        /**
         * Creating an onClickListener so it adds this particular movie to the user's list of favorite movies
         */
        mFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateFavMoviesList() > 0) {
                    Log.v(TAG, "This movie has been added to your favorites");
                            /*
                            Lets the user know that the movie has been added to his/her favourites.
                             */
                    Toast.makeText(MovieDetailsActivity.this, "This movie has been added to your favorites", Toast.LENGTH_SHORT).show();
                }
            }

            /**
             * Description: Adds the movie to the list of favorites
             * @return the number of movies added to the favorites
             */
            private int updateFavMoviesList() {
                /**
                 * Creating a single ContentValues object and adding to the list of ContentValues
                 */
                ContentValues[] contentValuesList = new ContentValues[1];
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, true);
                contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
                contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE_PATH, movie.getMovieImgPath());
                contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getMovieTitle());
                contentValuesList[0] = contentValues;
                return getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, contentValuesList);
            }
        });

        Intent previousIntent = getIntent();
        String movieId = previousIntent.getStringExtra(Intent.EXTRA_TEXT);

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_INSTANCE_KEY)) {
            movie = savedInstanceState.getParcelable(SAVE_INSTANCE_KEY);
            displayMovieDetails(movie);
        } else {
            new FetchMovieDetails().execute(movieId);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = createShareTrailerIntent();
                startActivity(shareIntent);
            }
        });
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mTrailerSummary + " " + POPMOVIES_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    @Override
    public void onClick(String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_CONSTRUCT + id));
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(BROWSER_CONSTRUCT + id));
        webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (appIntent.resolveActivity(getPackageManager()) != null) {
            // Open Youtube client
            startActivity(appIntent);
        } else {
            // Default to Web browser
            startActivity(webIntent);
        }
    }

    private class FetchMovieDetails extends AsyncTask<String, Void, Movie> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Movie doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String id = params[0];

            /**
             * Get the URL to fetch a movie's details via it's ID
             */
            URL movieDetailUrl = NetworkUtils.buildGetMovieDetailsUrl(id);
            /**
             * Get the URL to fetch the reviews of a movie via ID
             */
            URL getReviewsUrl = NetworkUtils.buildGetMovieReviewUrl(id);
            /**
             * Get the URL to fetch the videos attached to a movie via ID
             */
            URL getVideosUrl = NetworkUtils.buildGetVideosUrl(id);
            try {
                /**
                 * Gather required information you need about one movie
                 */
                String jsonMovieDetailsResponse = NetworkUtils.getResponseFromHttpUrl(movieDetailUrl);
                String jsonMovieReviewsResponse = NetworkUtils.getResponseFromHttpUrl(getReviewsUrl);
                String jsonMovieVideosResponse = NetworkUtils.getResponseFromHttpUrl(getVideosUrl);

                movie = MovieDetailsJsonUtils.getMovieInformationFromJson(jsonMovieDetailsResponse);

                /**
                 * Add the movie reviews to it's respective movie object
                 */
                movie.setMoviereviewsList(MovieReviewsJsonUtils.getMovieReviewsFromJson(jsonMovieReviewsResponse));

                /**
                 * Add the movie trailers to it's respective movie object
                 */
                movie.setMovieTrailerList(MovieTrailersJsonUtils.getMovieTrailersListFromJson(jsonMovieVideosResponse));
                return movie;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Movie movie) {
            if (movie != null) {
                displayMovieDetails(movie);
            }
        }
    }

    private void displayMovieDetails(Movie movie) {

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewReviews.setLayoutManager(mLayoutManager);
        recyclerViewReviews.setItemAnimator(new DefaultItemAnimator());
        reviewsAdapter = new ReviewsAdapter(this, movie.getMoviereviewsList());
        recyclerViewReviews.setAdapter(reviewsAdapter);
        /**
         * Takes care of showing the reviews
         */
        showReviews();

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(MovieDetailsActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTrailers.setLayoutManager(horizontalLayoutManagaer);
        recyclerViewTrailers.setItemAnimator(new DefaultItemAnimator());
        trailersAdapter = new TrailersAdapter(this, movie.getMovieTrailerList(), this);
        recyclerViewTrailers.setAdapter(trailersAdapter);
        /**
         * Shows the trailers
         */
        showTrailers();

        movieTitle.setText(movie.getMovieTitle());
        try {
            Picasso.with(getApplicationContext()).load(IMAGE_MOVIE_URL + movie.getMovieImgPath()).error(R.drawable.user_placeholder_error).into(moviePoster);
        } catch (IllegalArgumentException e) {
            moviePoster.setImageResource(R.drawable.user_placeholder_error);
        }

        movieOverview.setText(movie.getOverview());
        /**
         * Display the Release date
         */
        movieReleaseDate.setText(movie.getReleaseDate());
        /**
         * Display the User rating
         */
        movieUserRating.setText(String.format("%s%s", movie.getUserRating(), RATING_OUT_OF_TEN));
        /**
         * Prepare the mTrailerSummary in case the user wants to share it!
         */
        mTrailerSummary = "Trailer of " + movie.getMovieTitle() + " " + BROWSER_CONSTRUCT + movie.getMovieTrailerList().get(0);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVE_INSTANCE_KEY, movie);
        super.onSaveInstanceState(outState);
    }
}


package com.android.popmoviestwo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by mmalla on 25/10/17.
 */

public class Movie implements Parcelable{

    private String movieTitle;
    private String movieImgPath;
    private String movieId;
    private String releaseDate;
    private String overview;
    private String userRating;

    /**
     * There can be more than one review
     */
    private List<String> moviereviewsList;

    /**
     * There can be more than one video link
     */
    private List<String> movieTrailerList;

    /**
     * Constructor here
     *
     * @param name
     * @param movieImgPath
     * @param movieId
     */
    public Movie(String name, String movieImgPath, String movieId) {
        this.movieTitle = name;
        this.movieImgPath = movieImgPath;
        this.movieId = movieId;
    }

    public Movie(Parcel source) {
        movieTitle = source.readString();
        movieId = source.readString();
        movieImgPath = source.readString();
        releaseDate = source.readString();
        overview = source.readString();
        userRating = source.readString();
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieImgPath() {
        return movieImgPath;
    }

    public void setMovieImgPath(String movieImgPath) {
        this.movieImgPath = movieImgPath;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getUserRating() {
        return userRating;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    public List<String> getMoviereviewsList() {
        return moviereviewsList;
    }

    public void setMoviereviewsList(List<String> moviereviewsList) {
        this.moviereviewsList = moviereviewsList;
    }

    public List<String> getMovieTrailerList() {
        return movieTrailerList;
    }

    public void setMovieTrailerList(List<String> movieTrailerList) {
        this.movieTrailerList = movieTrailerList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieTitle);
        dest.writeString(movieId);
        dest.writeString(movieImgPath);
        dest.writeString(releaseDate);
        dest.writeString(overview);
        dest.writeString(userRating);
//        dest.writeList(moviereviewsList);
//        dest.writeList(movieTrailerList);
    }

    public final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }

    };
}


package com.android.popmoviestwo.utils;

/**
 * Created by mmalla on 01/01/18.
 */

import android.content.ContentValues;

import com.android.popmoviestwo.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: This class is created to assist in parsing and understanding the JSON response given by the 'GET reviews' API of TMDB
 * This class is used to gather the reviews given to a movie
 *
 */
public class MovieReviewsJsonUtils {

    private final static String MR_REVIEW_CONTENT = "content";

    private final static String MR_RESULTS = "results";

    public static List<String> getMovieReviewsFromJson(String reviewsJsonStr) throws JSONException{

        List<String> reviewList = new ArrayList<String>();

        JSONObject moviewReviewsObj = new JSONObject(reviewsJsonStr);

        JSONArray reviewResultsArr = moviewReviewsObj.getJSONArray(MR_RESULTS);

        /**
         * Verifying if the movie doesn't have any reviews, it adds a simple note saying there're no reviews for that movie
         */
        if(reviewResultsArr.length() == 0){
            reviewList.add(0, "No reviews for this movie. Why don't you give one?");
            return reviewList;
        }

        /**
         * Run through the results list and get all the information needed
         */

        for(int i = 0; i < reviewResultsArr.length(); i++){
            JSONObject reviewObj = reviewResultsArr.getJSONObject(i);
            String review_content = reviewObj.getString(MR_REVIEW_CONTENT);
            reviewList.add(i, review_content);
        }
        return reviewList;
    }
}


package com.android.popmoviestwo.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmalla on 01/01/18.
 * Description: This class is created to assist in parsing and understanding the JSON response given by the 'GET videos' API of TMDB
 * We retrieve a list of key strings of both Trailers/Featurettes listed
 */

public class MovieTrailersJsonUtils {

    public static List<String> getMovieTrailersListFromJson(String trailersJsonResponseStr) throws JSONException{

        final String TL_RESULTS = "results";

        final String TL_TYPE = "type";

        final String TL_KEY = "key";

        final String TRAILER_STRING = "Trailer";

        final String FEATURETTE_STRING = "Featurette";

        List<String> movieTrailersList = new ArrayList<String>();

        JSONObject movieTrailersJSONObj = new JSONObject(trailersJsonResponseStr);

        JSONArray movieTrailersArr = movieTrailersJSONObj.getJSONArray(TL_RESULTS);

        /**
         * If there are no results in the response, attach a note and return the list
         */
        if(movieTrailersArr.length() == 0){
            movieTrailersList.add(0, "There are no trailers attached to the movie!");
            return movieTrailersList;
        }

        /**
         * Use the int j to keep a count of the list of trailer keys
         */
        int j = 0;
        /**
         * Use i to parse through all the results of the JSON
         */
        for(int i = 0; i < movieTrailersArr.length(); i++){
            JSONObject tentativeTrailerObj = movieTrailersArr.getJSONObject(i);
            String key = tentativeTrailerObj.getString(TL_KEY).trim().toString();
            String videoType = tentativeTrailerObj.getString(TL_TYPE).trim().toString();

            // TODO Remove Featurette string if it's not needed for the app
            if(videoType.equals(TRAILER_STRING) || videoType.equals(FEATURETTE_STRING)){
                movieTrailersList.add(j, key);
                j++;
            }
        }

        if(j == 0){
            movieTrailersList.add(0, "There are no trailers attached to the movie!");
            return movieTrailersList;
        }

        return movieTrailersList;
    }
}


package com.android.popmoviestwo.utils;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by mmalla on 23/10/17.
 */

/**
 * This class is used to handle the network connections and get the response
 */

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String BASE_URL = "https://api.themoviedb.org/3/movie";

    private final static String QUERY_PARAM = "api_key";

    private final static String LANG_PARAM = "language";

    private final static String PAGE_PARAM = "page";

    private final static String PATH_VIDEOS = "videos";

    private final static String PATH_REVIEWS = "reviews";

    private final static String LANG_VALUE = "en_US";

    private final static String PAGE_VALUE = "1";

   /* */
    /**
     * TODO Add your API key here
     */
    private final static String API_Key = "<<Add your API key here>>";

    public static URL buildUrl(String path, String page) {

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(path)
                .appendQueryParameter(QUERY_PARAM, API_Key)
                .appendQueryParameter(LANG_PARAM, LANG_VALUE)
                .appendQueryParameter(PAGE_PARAM, page)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URL " + url);
        return url;
    }

    /**
     * Description: https://api.themoviedb.org/3/movie/19404?api_key=<<api-key>></>&language=en-US
     *
     * @param id
     * @return
     */
    public static URL buildGetMovieDetailsUrl(String id) {

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(id)
                .appendQueryParameter(QUERY_PARAM, API_Key)
                .appendQueryParameter(LANG_PARAM, LANG_VALUE)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URL " + url);
        return url;
    }

    /**
     * https://api.themoviedb.org/3/movie/19404/videos?api_key=<<api-key>></>&language=en-US
     *
     * @param id
     * @return
     */
    public static URL buildGetVideosUrl(String id) {

        Uri builtUrl = Uri.parse(BASE_URL).buildUpon()
                .appendPath(id)
                .appendPath(PATH_VIDEOS)
                .appendQueryParameter(QUERY_PARAM, API_Key)
                .appendQueryParameter(LANG_PARAM, LANG_VALUE)
                .build();

        URL url = null;

        try {
            url = new URL(builtUrl.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URL " + url);
        return url;
    }

    /**
     * https://api.themoviedb.org/3/movie/19404/reviews?api_key=<<api-key>></>&language=en-US&page=1
     *
     * @param id
     * @return
     */

    public static URL buildGetMovieReviewUrl(String id) {

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(id)
                .appendPath(PATH_REVIEWS)
                .appendQueryParameter(QUERY_PARAM, API_Key)
                .appendQueryParameter(LANG_PARAM, LANG_VALUE)
                .build();

        URL url = null;

        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URL " + url);

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}


package com.android.popmoviestwo.utils;

/**
 * Created by mmalla on 23/10/17.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;

import com.android.popmoviestwo.Movie;
import com.android.popmoviestwo.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: This class is created to assist in parsing and understanding the JSON response given by APIs from TMDB
 * This parses data from the The Movie DataBase APIs explained in:  https://developers.themoviedb.org/3/movies/get-top-rated-movies &
 * https://developers.themoviedb.org/3/movies/get-popular-movies having by paths /movie/popular & /movie/top_rated.
 */
public class MoviesListJsonUtils {

    private static final String PM_RESULTS = "results";

    private static final String PM_MOVIE_ID = "id";

    private static final String PM_MOVIE_TITLE = "title";

    private static final String PM_IMG_PATH = "poster_path";

    public static List<Movie> getSimpleMoviesInformationFromJson(String moviesJsonStr) throws JSONException {

        List<Movie> parsedMovieResults;

        JSONObject movieJson = new JSONObject(moviesJsonStr);

        JSONArray movielist = movieJson.getJSONArray(PM_RESULTS);

        parsedMovieResults = new ArrayList<>();

        /**
         * Run through the results list and get all the information needed
         */
        for (int i = 0; i < movielist.length(); i++) {
            JSONObject movieData = movielist.getJSONObject(i);

            Movie movie_object = new Movie(movieData.getString(PM_MOVIE_TITLE), movieData.getString(PM_IMG_PATH), movieData.getString(PM_MOVIE_ID));
            parsedMovieResults.add(movie_object);
        }
        return parsedMovieResults;
    }

    public static ContentValues[] getMovieContentValuesFromJson(String moviesJsonStr) throws JSONException{

        JSONObject movieJson = new JSONObject(moviesJsonStr);

        JSONArray movielistArray = movieJson.getJSONArray(PM_RESULTS);

        ContentValues[] movielistValues = new ContentValues[movielistArray.length()];

        for(int i = 0; i < movielistArray.length(); i++){

            JSONObject movieData = movielistArray.getJSONObject(i);

            ContentValues movieContentValue = new ContentValues();

            movieContentValue.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieData.getString(PM_MOVIE_ID));
            movieContentValue.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movieData.getString(PM_MOVIE_TITLE));
            movieContentValue.put(MovieContract.MovieEntry.COLUMN_MOVIE_IMAGE_PATH, movieData.getString(PM_IMG_PATH));
            movieContentValue.put(MovieContract.MovieEntry.COLUMN_FAVORITE, false);
            movielistValues[i] = movieContentValue;
        }
        return movielistValues;
    }
}


package com.android.popmoviestwo.utils;

/**
 * Created by mmalla on 27/10/17.
 */

import com.android.popmoviestwo.Movie;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Description: This class is created to assist in parsing and understanding the JSON response given by the GET movie details of TMDB
 * This class is used to parse through the API which gives the movie details
 */
public class MovieDetailsJsonUtils {

    public static Movie getMovieInformationFromJson(String moviesJsonStr) throws JSONException {

        /**
         * Movie release date
         */
        final String PM_MOVIE_RELEASE_DATE = "release_date";

        /**
         * Movie id
         */
        final String PM_MOVIE_ID = "id";

        /**
         * Movie title
         */
        final String PM_MOVIE_TITLE = "original_title";

        /**
         * Image path
         */
        final String PM_IMG_PATH = "poster_path";

        /**
         * Overview of the movie
         */
        final String PM_OVERVIEW = "overview";

        /**
         * user rating for the movie
         */
        final String PM_VOTE_AVG = "vote_average";

        Movie parsedMovieDetails = new Movie("", "", "");

        JSONObject movieJson = new JSONObject(moviesJsonStr);

        String releaseDate = movieJson.getString(PM_MOVIE_RELEASE_DATE);
        String overview = movieJson.getString(PM_OVERVIEW);
        String movieTitle = movieJson.getString(PM_MOVIE_TITLE);
        String movieImgPath = movieJson.getString(PM_IMG_PATH);
        String userRating = movieJson.getString(PM_VOTE_AVG);
        String movieId = movieJson.getString(PM_MOVIE_ID);

        parsedMovieDetails.setMovieTitle(movieTitle);
        parsedMovieDetails.setMovieId(movieId);
        parsedMovieDetails.setReleaseDate(releaseDate);
        parsedMovieDetails.setMovieImgPath(movieImgPath);
        parsedMovieDetails.setOverview(overview);
        parsedMovieDetails.setUserRating(userRating);

        return parsedMovieDetails;
    }
}


package com.android.popmoviestwo.data;

/**
 * Created by mmalla on 07/01/18.
 */

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines the table columns for the Movie Database.
 */
public class MovieContract {

    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store.
     */

    public static final String CONTENT_AUTHORITY = "com.android.popmoviestwo";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider for PopMovies2.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";

    /**
     * Inner class that defines the table contents of the MovieTable
     */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_MOVIE_TITLE = "title";

        public static final String COLUMN_MOVIE_IMAGE_PATH = "image_path";

        public static final String COLUMN_FAVORITE = "favorite";

    }
}


package com.android.popmoviestwo.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.popmoviestwo.data.MovieContract.MovieEntry;

/**
 * Created by mmalla on 07/01/18.
 */

public class MovieProvider extends ContentProvider {

    private static final int CODE_MOVIE = 100;
    private static final int CODE_MOVIE_FAVORITES_LIST = 101;
    private static final int CODE_MOVIE_FAV_MOVIE = 103;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper movieDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MovieContract.PATH_MOVIE, CODE_MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/fav", CODE_MOVIE_FAVORITES_LIST);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/fav/#", CODE_MOVIE_FAV_MOVIE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE:
                cursor = movieDbHelper.getReadableDatabase().query(MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (_id < 0) {
                            throw new SQLException("Failed to insert row into " + uri);
                        }
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();

                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new RuntimeException("We are not implementing single insert here");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new RuntimeException("We are not implementing update here");
    }

    @Override
    public void shutdown() {
        movieDbHelper.close();
        super.shutdown();
    }
}

package com.android.popmoviestwo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.popmoviestwo.data.MovieContract.MovieEntry;


/**
 * Created by mmalla on 07/01/18.
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movie.db";

    private static final int DATABASE_VERSION = 7;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + "("
                + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MovieEntry.COLUMN_MOVIE_ID + " STRING NULL, "
                + MovieEntry.COLUMN_MOVIE_TITLE + " STRING NULL, "
                + MovieEntry.COLUMN_MOVIE_IMAGE_PATH + " STRING NULL, "
                + MovieEntry.COLUMN_FAVORITE + " BOOLEAN, "
                + "UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}


