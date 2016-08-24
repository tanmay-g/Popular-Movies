package tanmaygodbole.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import tanmaygodbole.popularmovies.data.MoviesDataContract;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String sortPreferenceKey = "sortPreference";
    private static final String lastSyncKey = "lastSync";
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;


    private MainFragmentCursorAdapter mCursAdapter;

    private int mSelectedItemPosition = GridView.INVALID_POSITION;
    private final static String mSelectedItemPosKey = "selectedItemPosKey";

    private Spinner sortTypeSpinner;
    private static final String spinnerPrefKey = "spinnerPref";
    private static final String mainGridStateKey = "mainGridStateKey";

    private int spinnerPrefValue = -1;
    private final int POPULAR = 0;
    private final int TOPRATED = 1;
    private final int FAVOURITES = 2;

    private static final int CURS_LOADER = 0;
    private static final int ASYNC_MAIN_DATA_LOADER = 1;

    static final int COL_MOVIE_ID = 1;
    static final int COL_MOVIE_TITLE = 2;
    static final int COL_MOVIE_POSTER_PATH = 3;
    static final int COL_MOVIE_RATING = 4;
    static final int COL_MOVIE_DATE = 5;
    static final int COL_MOVIE_OVERVIEW = 6;
    static final String[] MOVIE_COLUMNS = {
            MoviesDataContract.MoviesEntry.TABLE_NAME + "." + MoviesDataContract.MoviesEntry._ID + " as " + MoviesDataContract.MoviesEntry._ID,
            MoviesDataContract.MoviesEntry.TABLE_NAME + "." + MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " as " + MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesDataContract.MoviesEntry.COLUMN_TITLE,
            MoviesDataContract.MoviesEntry.COLUMN_POSTER_PATH,
            MoviesDataContract.MoviesEntry.COLUMN_RATING,
            MoviesDataContract.MoviesEntry.COLUMN_RELEASE_DATE,
            MoviesDataContract.MoviesEntry.COLUMN_OVERVIEW
    };

    private GridView mainGrid;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri detailUri);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Log.d(LOG_TAG, "saving instance");
        outState.putInt(spinnerPrefKey, sortTypeSpinner.getSelectedItemPosition());
        outState.putInt(mSelectedItemPosKey, mSelectedItemPosition);
        outState.putParcelable(mainGridStateKey, mainGrid.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.i(LOG_TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null){
            mSelectedItemPosition = savedInstanceState.getInt(mSelectedItemPosKey);
            spinnerPrefValue = savedInstanceState.getInt(spinnerPrefKey);

        }
        else {

            spinnerPrefValue = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(sortPreferenceKey,0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.i(LOG_TAG, "OnCreateView");

        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        mCursAdapter = new MainFragmentCursorAdapter(getActivity(), null, 0);

        mainGrid = (GridView)rootView.findViewById(R.id.main_grid);

        mainGrid.setAdapter(mCursAdapter);
        mainGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mSelectedItemPosition = position;
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Uri detailUri = MoviesDataContract.MoviesEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID));
//                    Log.d(LOG_TAG, "Calling Detail with uri: " + detailUri);
                    ((Callback)getActivity()).onItemSelected(detailUri);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long lastSync = settings.getLong(lastSyncKey, 0);
        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS)
            getLoaderManager().initLoader(ASYNC_MAIN_DATA_LOADER, null, this).forceLoad();
        else
            getLoaderManager().initLoader(CURS_LOADER, null, this);
        if (savedInstanceState != null) {
            mainGrid.onRestoreInstanceState(savedInstanceState.getParcelable(mainGridStateKey));
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        sortTypeSpinner = (Spinner) (menu.findItem(R.id.sort_spinner_menu_item).getActionView());
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.sort_spinner_choices,
                R.layout.custom_spinner_item);
        spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        if (sortTypeSpinner != null) {
            sortTypeSpinner.setAdapter(spinnerAdapter);
            sortTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    //Log.d(LOG_TAG, "onItemSelected being called");
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor settingsEditor = settings.edit();
                    settingsEditor.putInt(sortPreferenceKey, position);
                    settingsEditor.apply();
                    if (spinnerPrefValue != position) {
                        spinnerPrefValue = position;
                        refreshUI();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
        else
            Log.e(LOG_TAG, "Didn't get the spinner");
        if (spinnerPrefValue != -1){
            sortTypeSpinner.setSelection(spinnerPrefValue);
        }

    }

    private void refreshUI(){
//        Log.i(LOG_TAG, "refreshUI was called");
        getLoaderManager().restartLoader(CURS_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case ASYNC_MAIN_DATA_LOADER: {
//                Log.i(LOG_TAG, "Starting AsyncLoader");
                return new AsyncMainDataLoader(getActivity(), args);
            }
            case CURS_LOADER: {
//                Log.i(LOG_TAG, "Starting CursorLoader");
                Uri fetchUri = null;
                switch (spinnerPrefValue){
                    case POPULAR:{
                        fetchUri = MoviesDataContract.PopularEntry.CONTENT_URI;
                        break;
                    }
                    case TOPRATED:{
                        fetchUri = MoviesDataContract.TopratedEntry.CONTENT_URI;
                        break;
                    }
                    case FAVOURITES:{
                        fetchUri = MoviesDataContract.FavouritesEntry.CONTENT_URI;
                        break;
                    }
                    default:{
                        Log.e(LOG_TAG, "spinnerPrefValue not set at loader create");
                    }
                }
                return new CursorLoader(
                        getActivity(),
                        fetchUri,
                        MOVIE_COLUMNS,
                        null,
                        null,
                        null
                );
            }
            default:
                throw new UnsupportedOperationException("Unknown loader");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case ASYNC_MAIN_DATA_LOADER:{
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor settingsEditor = settings.edit();
                settingsEditor.putLong(lastSyncKey, System.currentTimeMillis());
                settingsEditor.apply();
//                Log.i(LOG_TAG, "Finished asyncLoad. Will now start cursor loader");
                //Cursor loader isn't started at activity creation if a sync is needed
                getLoaderManager().initLoader(CURS_LOADER, null, this);
                break;

            }
            case CURS_LOADER:{
//                Log.i(LOG_TAG, "Finished CursorLoader");
                mCursAdapter.swapCursor(data);
                if (mSelectedItemPosition != GridView.INVALID_POSITION) {
                    //possibly not best practice
                    mainGrid.requestFocusFromTouch();

                    mainGrid.setSelection(mSelectedItemPosition);
                    mainGrid.clearFocus();
                }
//                mCursAdapter.notifyDataSetChanged();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown loader");
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //clean up resources
        switch (loader.getId()){
            case ASYNC_MAIN_DATA_LOADER:{
                break;
            }
            case CURS_LOADER:{
                mCursAdapter.swapCursor(null).close();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown loader");
        }
    }

}

