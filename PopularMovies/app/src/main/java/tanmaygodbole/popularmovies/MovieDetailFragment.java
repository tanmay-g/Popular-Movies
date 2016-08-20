package tanmaygodbole.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;

import tanmaygodbole.popularmovies.data.MoviesDataContract;

public class MovieDetailFragment extends Fragment{

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private Uri mSelectedMovieUri;
    private String movieId;
    private static final int CURS_LOADER = 0;
    private static final int ASYNC_DETAILS_LOADER = 1;

    private LoaderManager.LoaderCallbacks<HashMap<String, String>[]> asyncLoaderListener = new LoaderManager.LoaderCallbacks<HashMap<String, String>[]>() {
        @Override
        public Loader<HashMap<String, String>[]> onCreateLoader(int id, Bundle args) {
            Log.i(LOG_TAG, "Starting the details async Loader");
            return new AsyncDetailsDataLoader(getActivity(), args);
        }

        @Override
        public void onLoadFinished(Loader<HashMap<String, String>[]> loader, HashMap<String, String>[] data) {
            Log.i(LOG_TAG, "Finished the details async Loader");
            //Since very few items are expected, use a Linear layout, and then just populate
            //first for trailers
            HashMap<String, String> trailersMap = null;
            HashMap<String, String> reviewsMap = null;
            if (data != null){
            trailersMap = data[0];
            reviewsMap = data[1];
            }
            if (trailersMap != null && trailersMap.size() > 0){
                TrailerAdapter trailerAdapter = new TrailerAdapter(getActivity(), trailersMap);
                for (int i = 0; i < trailerAdapter.getCount(); i++){
                    trailersList.addView(trailerAdapter.getView(i, null, null));
                }
            }
            else {
                //No need to show
                trailersLabelView.setVisibility(View.GONE);
                trailersList.setVisibility(View.GONE);
            }
            //now reviews
            if (reviewsMap != null && reviewsMap.size() > 0){
                ReviewAdapter reviewAdapter = new ReviewAdapter(getActivity(), reviewsMap);
                for (int i =0; i < reviewAdapter.getCount(); i++){
                    reviewsList.addView(reviewAdapter.getView(i, null, null));
                }
            }
            else {
                //No need to show
                reviewsLabelView.setVisibility(View.GONE);
                reviewsList.setVisibility(View.GONE);
            }

        }

        @Override
        public void onLoaderReset(Loader<HashMap<String, String>[]> loader) {
            loader.reset();
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderListener = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.i(LOG_TAG, "Starting the details cursor loader");
            return new CursorLoader(
                    getActivity(),
                    mSelectedMovieUri,
                    MainFragment.MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.i(LOG_TAG, "Finished the details cursor loader");
            if (!data.moveToFirst())
                return;
            String movieId = String.valueOf(data.getLong(MainFragment.COL_MOVIE_ID));
            String title = data.getString(MainFragment.COL_MOVIE_TITLE);
            File poster = MainFragmentCursorAdapter.getPosterFile(getActivity(), movieId);
            String synopsis = data.getString(MainFragment.COL_MOVIE_OVERVIEW);
            String date = data.getString(MainFragment.COL_MOVIE_DATE).split("-")[0];
            String rating = data.getString(MainFragment.COL_MOVIE_RATING) + "/10";

            getActivity().setTitle(title);
            titleView.setText(title);

            MainFragmentCursorAdapter.addPoster(getActivity(), posterView, poster);

            synopsisView.setText(synopsis);

            dateView.setText(date);

            ratingView.setText(rating);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            loader.reset();
        }
    };

    private ImageView posterView;
    private TextView titleView;
    private TextView synopsisView;
    private TextView ratingView;
    private TextView dateView;
    private TextView trailersLabelView;
    private LinearLayout trailersList;
    private TextView reviewsLabelView;
    private LinearLayout reviewsList;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //TODO save instance state?
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.details_fragment_menu, menu);
        CheckBox favStar = (CheckBox) menu.findItem(R.id.favorite_button_menu_item).getActionView();
        if (favStar != null){
            //set state as per db
            Cursor favTableQueryResult = getActivity().getContentResolver().query(
                    MoviesDataContract.FavouritesEntry.buildFavouritesUri(Long.parseLong(movieId)),
                    null,
                    null,
                    null,
                    null
            );
            favStar.setChecked(favTableQueryResult.moveToFirst());

            favStar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox favStar = (CheckBox) v;
                    if (favStar.isChecked()){
                        //insert into favourites table
                        ContentValues newFavEntry = new ContentValues(1);
                        newFavEntry.put(MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID, movieId);
                        getActivity().getContentResolver().insert(
                                MoviesDataContract.FavouritesEntry.CONTENT_URI,
                                newFavEntry);
                    }
                    else {
                        //remove from fav table
                        getActivity().getContentResolver().delete(
                                MoviesDataContract.FavouritesEntry.CONTENT_URI,
                                MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID + "=?",
                                new String[]{movieId}
                        );
                    }
                }
            });
        }
        else {
            Log.e(LOG_TAG, "Somehow the star was not made");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent callingIntent = getActivity().getIntent();
        if (callingIntent != null){
            //get MovieData
            mSelectedMovieUri = callingIntent.getData();
            movieId = MoviesDataContract.MoviesEntry.getMovieIdFromUri(mSelectedMovieUri);
        }
        //populateScreen(rootView);
        posterView = (ImageView) rootView.findViewById(R.id.poster);
        titleView = ((TextView)rootView.findViewById(R.id.title));
        synopsisView = ((TextView)rootView.findViewById(R.id.synopsis));
        ratingView = ((TextView)rootView.findViewById(R.id.rating));
        dateView = ((TextView)rootView.findViewById(R.id.date));
        trailersLabelView = (TextView) rootView.findViewById(R.id.trailers_label);
        trailersList = (LinearLayout) rootView.findViewById(R.id.trailers_list);
        reviewsLabelView = (TextView) rootView.findViewById(R.id.reviews_label);
        reviewsList = (LinearLayout) rootView.findViewById(R.id.reviews_list);

        return rootView;
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, "onResume");
        getLoaderManager().initLoader(CURS_LOADER, null, cursorLoaderListener);
        Bundle args = new Bundle();
        args.putString(AsyncDetailsDataLoader.detailsLoaderBundleMovieIdKey, movieId);
        getLoaderManager().initLoader(ASYNC_DETAILS_LOADER, args, asyncLoaderListener);
        super.onResume();
    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        Log.i(LOG_TAG, "onActivityCreated");
//        /*
//            OnLoadFinished called twice, so moving to onResume.
//            Possible reason:
//            The problem is that it called twice:
//            1. from Fragment.onStart
//            2. from FragmentActivity.onStart
//
//            The only difference is that in Fragment.onStart it checks if mLoaderManager != null.
//            What this means is if you call getLoadManager before onStart, like in onActivityCreated,
//            it will get/create load manager and it will be called. To avoid this you need to call it later, like in onResume.
//
//        */
//        super.onActivityCreated(savedInstanceState);
//    }


}
