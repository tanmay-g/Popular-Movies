package tanmaygodbole.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainFragment extends Fragment {

    private static final String LOG_TAG = MainFragment.class.getSimpleName();
    private static final String sortPreferenceKey = "sortPreference";
    private static String[] sortValues = null;// = {"popular", "top_rated"};
    static final String movieDataKey = "movieData";
    private ArrayList<MovieData> movieData = new ArrayList<>();
    //private final static String movieDataSaveStateKey = "movieData";
    private CustomMovieToImageAdapter imageAdapter;
    //private int sortPreferenceValue = 0;
    private Spinner sortTypeSpinner;
    private static final String spinnerPrefKey = "spinnerPref";
    private int spinnerPrefValue = -1;
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //Log.d(LOG_TAG, "saving instance");
        outState.putInt(spinnerPrefKey, sortTypeSpinner.getSelectedItemPosition());
        outState.putParcelableArrayList(movieDataKey, movieData);
        //Log.d(LOG_TAG, Integer.toString(movieData.size()) + " " + Integer.toString(sortTypeSpinner.getSelectedItemPosition()));
        super.onSaveInstanceState(outState);
    }

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.i(LOG_TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        sortValues = getResources().getStringArray(R.array.sort_spinner_choice_values);
        setHasOptionsMenu(true);
        if (savedInstanceState != null){
            //Log.i(LOG_TAG, "Restoring from saved state: ");
            movieData = savedInstanceState.getParcelableArrayList(movieDataKey);
            spinnerPrefValue = savedInstanceState.getInt(spinnerPrefKey);
            //Log.d(LOG_TAG, Integer.toString(movieData.size()) + " " /*+ Integer.toString(sortPreferenceValue) + " "*/ + Integer.toString(savedInstanceState.getInt(spinnerPrefKey)));

        }
        /*else {
            spinnerPrefValue = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(sortPreferenceKey,0);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.i(LOG_TAG, "OnCreateView");

        View rootView = inflater.inflate(R.layout.fragment_main,container,false);

        //new FetchMoviesTask().execute(sortValues[sortPreferenceValue]);

        imageAdapter = new CustomMovieToImageAdapter(getActivity(),
                R.layout.grid_item_image,
                R.id.grid_item_image_imageview,
                movieData
                );
        GridView mainGrid = (GridView)rootView.findViewById(R.id.main_grid);
        mainGrid.setAdapter(imageAdapter);

        mainGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MovieData selectedMovie = (MovieData)adapterView.getItemAtPosition(position);
                //Toast.makeText(getActivity(), "Clicked on Movie: " + selectedMovie.getTitle(), Toast.LENGTH_SHORT).show();
                Intent detailCallingIntent = new Intent(getActivity(),MovieDetailActivity.class);
                startActivity(detailCallingIntent.putExtra(movieDataKey, selectedMovie));
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
                        new FetchMoviesTask().execute(sortValues[position]);
                        spinnerPrefValue = position;
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

    class FetchMoviesTask extends AsyncTask<String, Void, String> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<MovieData> getMovieDataFromJson(String movieJsonStr) throws JSONException {
            //Log.i(LOG_TAG, "getMovieDataFromJson");

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_TITLE = "title";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_POSTER = "poster_path";
            final String TMDB_AVG = "vote_average";
            final String TMDB_DATE = "release_date";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray resultsArray = movieJson.getJSONArray(TMDB_RESULTS);

            ArrayList<MovieData> movieDataArrayList = new ArrayList<>(resultsArray.length());

            for (int i=0; i < resultsArray.length(); i++){
                JSONObject movieEntry = resultsArray.getJSONObject(i);
                movieDataArrayList.add(new MovieData(movieEntry.getString(TMDB_POSTER),
                        movieEntry.getString(TMDB_TITLE),
                        movieEntry.getString(TMDB_OVERVIEW),
                        movieEntry.getDouble(TMDB_AVG),
                        movieEntry.getString(TMDB_DATE)));
                //Log.i(LOG_TAG, "Parsed poster path: " + movieEntry.getString(TMDB_POSTER));
            }
            return movieDataArrayList;
        }

        @Override
        protected String doInBackground(String... params) {
            //Log.i(LOG_TAG, "doInBackground");
            String movieListJsonStr = null;
            if (params.length==0){
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                if (params[0] == null){
                    params[0]="popular";
                    Log.e(LOG_TAG, "had to use default sort pref");
                }
                Uri.Builder tmdbBuilder = new Uri.Builder();
                tmdbBuilder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(params[0])
                        .appendQueryParameter("api_key",BuildConfig.THE_MOVIE_DATABASE_API_KEY);
                URL fetchURL = new URL(tmdbBuilder.build().toString());
                //Log.i(LOG_TAG, "doInBackground URL: " + fetchURL.toString());
                urlConnection = (HttpURLConnection) fetchURL.openConnection();
                urlConnection.setRequestMethod("GET");
                Log.i(LOG_TAG, "Accessing the net");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieListJsonStr = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            //Log.i(LOG_TAG, "movieListJsonStr: " + movieListJsonStr);
            return movieListJsonStr;
        }

        @Override
        protected void onPostExecute(String movieJson) {
            //Log.i(LOG_TAG, "onPostExecute");
            if (movieJson != null){
                try {
                    movieData = getMovieDataFromJson(movieJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),"Failed parsing JSON", Toast.LENGTH_SHORT).show();
                    return;
                }

                imageAdapter.clear();
                imageAdapter.addAll(movieData);

                //Log.i(LOG_TAG, "Sent list to adapter");

            }
        }
    }
}

