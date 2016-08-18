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
import android.widget.TextView;

import java.io.File;

import tanmaygodbole.popularmovies.data.MoviesDataContract;

public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = MovieDetailFragment.class.getSimpleName();
    private Uri mSelectedMovieUri;
    private String movieId;
    private static final int CURS_LOADER = 0;

    public MovieDetailFragment() {
    }

    ImageView posterView;

    TextView titleView;
    TextView synopsisView;
    TextView ratingView;
    TextView dateView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        return rootView;
    }

//    private void populateScreen(View rootView){

    //    }
//        ((TextView)rootView.findViewById(R.id.date)).setText(date);
//        ((TextView)rootView.findViewById(R.id.rating)).setText(Double.toString(mSelectedMovieUri.getRating()) + "/10");
//        ((TextView)rootView.findViewById(R.id.synopsis)).setText(mSelectedMovieUri.getOverview());
//        ((TextView)rootView.findViewById(R.id.title)).setText(mSelectedMovieUri.getTitle());
//        //Log.i(LOG_TAG, date);
//        String date = mSelectedMovieUri.getReleaseDate().split("-")[0];
//                );
//                mSelectedMovieUri.getPosterPath(),
//        CustomMovieToImageAdapter.posterAdder(getActivity(),
//        getActivity().setTitle(mSelectedMovieUri.getTitle());

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(CURS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        Log.i(LOG_TAG, "Getting to problem");
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
}
