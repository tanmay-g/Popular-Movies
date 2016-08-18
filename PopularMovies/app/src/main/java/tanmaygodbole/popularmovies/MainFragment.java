package tanmaygodbole.popularmovies;

import android.content.Intent;
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

;


public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String sortPreferenceKey = "sortPreference";
    private static final String lastSyncKey = "lastSync";
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private static String[] sortValues = null;// = {"popular", "top_rated"};
//    static final String movieDataKey = "movieData";
//    private ArrayList<MovieData> movieData = new ArrayList<>();
    //private final static String movieDataSaveStateKey = "movieData";
//    private CustomMovieToImageAdapter imageAdapter;
    private MainFragmentCursorAdapter mCursAdapter;

//    private int mSelectedItemPosition = GridView.INVALID_POSITION;
//    private final static String mSelectedItemPosKey = "selectedItemPosKey";

    //private int sortPreferenceValue = 0;
    private Spinner sortTypeSpinner;
    private static final String spinnerPrefKey = "spinnerPref";
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long lastSync = settings.getLong(lastSyncKey, 0);
        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS)
            getLoaderManager().initLoader(ASYNC_MAIN_DATA_LOADER, null, this).forceLoad();
        else
            getLoaderManager().initLoader(CURS_LOADER, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Log.d(LOG_TAG, "saving instance");
        outState.putInt(spinnerPrefKey, sortTypeSpinner.getSelectedItemPosition());
//        outState.putInt(mSelectedItemPosKey, mSelectedItemPosition);
        //not needed, as DB does this
//        outState.putParcelableArrayList(movieDataKey, movieData);
        //Log.d(LOG_TAG, Integer.toString(movieData.size()) + " " + Integer.toString(sortTypeSpinner.getSelectedItemPosition()));
        super.onSaveInstanceState(outState);
    }

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        sortValues = getResources().getStringArray(R.array.sort_spinner_choice_values);
        setHasOptionsMenu(true);
        if (savedInstanceState != null){
            Log.i(LOG_TAG, "Restoring from saved state");
            //TODO either save gridview state, or save and scroll to the selected item
//            mSelectedItemPosition = savedInstanceState.getInt(mSelectedItemPosKey);
            spinnerPrefValue = savedInstanceState.getInt(spinnerPrefKey);
            refreshUI();
            //Log.d(LOG_TAG, Integer.toString(movieData.size()) + " " /*+ Integer.toString(sortPreferenceValue) + " "*/ + Integer.toString(savedInstanceState.getInt(spinnerPrefKey)));

        }
        else {
            //why was this commented?
            spinnerPrefValue = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(sortPreferenceKey,0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(LOG_TAG, "OnCreateView");

        View rootView = inflater.inflate(R.layout.fragment_main,container,false);
        mCursAdapter = new MainFragmentCursorAdapter(getActivity(), null, 0);

        GridView mainGrid = (GridView)rootView.findViewById(R.id.main_grid);

        mainGrid.setAdapter(mCursAdapter);
        mainGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                mSelectedItemPosition = position;
                Intent detailCallingIntent = new Intent(getActivity(),MovieDetailActivity.class);
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    //set flag for clear top?
                    Uri detailUri = MoviesDataContract.MoviesEntry.buildMovieUri(cursor.getLong(COL_MOVIE_ID));
                    Log.d(LOG_TAG, "Calling Detail with uri: " + detailUri);
                    detailCallingIntent.setData(detailUri);
                    startActivity(detailCallingIntent);
                }
            }
        });

        return rootView;
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
                        //new FetchMoviesTask().execute(sortValues[settings.getInt(sortPreferenceKey, 0)]);
                        //new FetchMoviesTask().execute(sortValues[position]);

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
        Log.i(LOG_TAG, "refreshUI was called");
        getLoaderManager().restartLoader(CURS_LOADER, null, this);
    }

//    class FetchMoviesTask extends AsyncTask<String, Void, String> {
//
//        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
//
//        /**
//         * Take the String representing the complete forecast in JSON Format and
//         * pull out the data we need to construct the Strings needed for the wireframes.
//         *
//         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
//         * into an Object hierarchy for us.
//         */
//        private ArrayList<MovieData> getMovieDataFromJson(String movieJsonStr) throws JSONException {
//            //Log.i(LOG_TAG, "getMovieDataFromJson");
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String TMDB_RESULTS = "results";
//            final String TMDB_TITLE = "title";
//            final String TMDB_OVERVIEW = "overview";
//            final String TMDB_POSTER = "poster_path";
//            final String TMDB_AVG = "vote_average";
//            final String TMDB_DATE = "release_date";
//
//            JSONObject movieJson = new JSONObject(movieJsonStr);
//            JSONArray resultsArray = movieJson.getJSONArray(TMDB_RESULTS);
//
//            ArrayList<MovieData> movieDataArrayList = new ArrayList<>(resultsArray.length());
//
//            for (int i=0; i < resultsArray.length(); i++){
//                JSONObject movieEntry = resultsArray.getJSONObject(i);
//                movieDataArrayList.add(new MovieData(movieEntry.getString(TMDB_POSTER),
//                        movieEntry.getString(TMDB_TITLE),
//                        movieEntry.getString(TMDB_OVERVIEW),
//                        movieEntry.getDouble(TMDB_AVG),
//                        movieEntry.getString(TMDB_DATE)));
//                //Log.i(LOG_TAG, "Parsed poster path: " + movieEntry.getString(TMDB_POSTER));
//            }
//            return movieDataArrayList;
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            //Log.i(LOG_TAG, "doInBackground");
//            String movieListJsonStr = null;
//            if (params.length==0){
//                return null;
//            }
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//            try {
//                if (params[0] == null){
//                    params[0]="popular";
//                    Log.e(LOG_TAG, "had to use default sort pref");
//                }
//                Uri.Builder tmdbBuilder = new Uri.Builder();
//                tmdbBuilder.scheme("http")
//                        .authority("api.themoviedb.org")
//                        .appendPath("3")
//                        .appendPath("movie")
//                        .appendPath(params[0])
//                        .appendQueryParameter("api_key",BuildConfig.THE_MOVIE_DATABASE_API_KEY);
//                URL fetchURL = new URL(tmdbBuilder.build().toString());
//                //Log.i(LOG_TAG, "doInBackground URL: " + fetchURL.toString());
//                urlConnection = (HttpURLConnection) fetchURL.openConnection();
//                urlConnection.setRequestMethod("GET");
//                Log.e(LOG_TAG, "Accessing the net");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuilder buffer = new StringBuilder();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                    // But it does make debugging a *lot* easier if you print out the completed
//                    // buffer for debugging.
//                    buffer.append(line).append("\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                movieListJsonStr = buffer.toString();
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally{
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//            //Log.i(LOG_TAG, "movieListJsonStr: " + movieListJsonStr);
//            return movieListJsonStr;
//        }
//
//        @Override
//        protected void onPostExecute(String movieJson) {
//            //Log.i(LOG_TAG, "onPostExecute");
//            if (movieJson != null){
//                try {
//                    movieData = getMovieDataFromJson(movieJson);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Toast.makeText(getActivity(),"Failed parsing JSON", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                imageAdapter.clear();
//                imageAdapter.addAll(movieData);
//
//                //Log.i(LOG_TAG, "Sent list to adapter");
//
//            }
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case ASYNC_MAIN_DATA_LOADER: {
                return new AsyncMainDataLoader(getActivity(), args);
            }
            case CURS_LOADER: {
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
                Log.i(LOG_TAG, "Finished asyncLoad. Will now start cursor loader");
                //Cursor loader isn't started at activity creation if a sync is needed
                getLoaderManager().initLoader(CURS_LOADER, null, this);
                break;

            }
            case CURS_LOADER:{
                mCursAdapter.swapCursor(data);
//                mCursAdapter.notifyDataSetChanged();
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown loader");
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //need to clean up resources
        switch (loader.getId()){
            case ASYNC_MAIN_DATA_LOADER:{
                break;
                //nothing to do?
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

