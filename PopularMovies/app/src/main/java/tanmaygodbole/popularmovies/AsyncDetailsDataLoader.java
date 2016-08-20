package tanmaygodbole.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Tanmay.godbole on 18-08-2016
 */
public class AsyncDetailsDataLoader extends AsyncTaskLoader<HashMap<String, String>[]> {
    private static final String LOG_TAG = AsyncDetailsDataLoader.class.getSimpleName();
    private static final String[] resources = {"trailers","reviews"};
    private static final String youtubePrefix = "https://www.youtube.com/watch?v=";
    static final String detailsLoaderBundleMovieIdKey = "detailsLoaderBundleMovieIdKey";
    private final String movieId;
    private boolean hasResult = false;
    private HashMap<String, String>[] mResultData;

    public AsyncDetailsDataLoader(Context context, Bundle args) {
        super(context);
        movieId = args.getString(detailsLoaderBundleMovieIdKey);
    }

    @Override
    protected void onStartLoading() {
        if (hasResult) {
            deliverResult(mResultData);
        }
        else {
            //this instance has already finished loading
            forceLoad();
        }

    }

    @Override
    public void deliverResult(HashMap<String, String>[] data) {
        mResultData = data;
        hasResult = true;
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if (hasResult) {
            mResultData = null;
            hasResult = false;
        }
    }

    @Override
    public HashMap<String, String>[] loadInBackground() {
        HashMap<String,String> trailerResult;
        HashMap<String,String> reviewResult;

        //get the response from net
        String trailersJsonStr = getJSONStrByResourceType(resources[0]);
        //get response from net
        String reviewsJsonStr = getJSONStrByResourceType(resources[1]);
        if (trailersJsonStr == null || reviewsJsonStr == null)
            return null;

        //now parse to get the name & url-end
        trailerResult = parseTrailersResult(trailersJsonStr);
        //now parse this to get the reviewer name and content
        reviewResult = parseReviewsResult(reviewsJsonStr);

        return new HashMap[]{trailerResult, reviewResult};
    }

    private HashMap<String, String> parseReviewsResult(String reviewsJsonStr){
        final String TMDB_RESULTS = "results";
        final String TMDB_AUTHOR = "author";
        final String TMDB_CONTENT = "content";
        HashMap<String,String> trailerResult = new HashMap<>();

        JSONObject movieJson;
        try {
            movieJson = new JSONObject(reviewsJsonStr);
            JSONArray trailersJsonArray = movieJson.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < trailersJsonArray.length(); i++){
                JSONObject trailerEntry = trailersJsonArray.getJSONObject(i);
                trailerResult.put(trailerEntry.getString(TMDB_AUTHOR),
                        trailerEntry.getString(TMDB_CONTENT));
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return trailerResult;
    }

    private String generateYoutubeUrl(String trailerYoutubeId){
        return youtubePrefix + trailerYoutubeId;
    }

    private HashMap<String, String> parseTrailersResult(String trailersJsonStr){
        final String TMDB_YOUTUBE = "youtube";
        final String TMDB_NAME = "name";
        final String TMDB_SOURCE = "source";
        HashMap<String,String> trailerResult = new HashMap<>();

        JSONObject movieJson;
        try {
            movieJson = new JSONObject(trailersJsonStr);
            JSONArray trailersJsonArray = movieJson.getJSONArray(TMDB_YOUTUBE);

            for (int i = 0; i < trailersJsonArray.length(); i++){
                JSONObject trailerEntry = trailersJsonArray.getJSONObject(i);
                trailerResult.put(trailerEntry.getString(TMDB_NAME),
                        generateYoutubeUrl(trailerEntry.getString(TMDB_SOURCE)));
            }


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return trailerResult;
    }

    private String getJSONStrByResourceType(String resourceToFetch){
        String jsonStr;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            Uri.Builder tmdbBuilder = new Uri.Builder();
            tmdbBuilder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(movieId)
                    .appendPath(resourceToFetch)
                    .appendQueryParameter("api_key",BuildConfig.THE_MOVIE_DATABASE_API_KEY);
            URL fetchURL = new URL(tmdbBuilder.build().toString());
            //Log.i(LOG_TAG, "doInBackground URL: " + fetchURL.toString());
            urlConnection = (HttpURLConnection) fetchURL.openConnection();
            urlConnection.setRequestMethod("GET");
            Log.e(LOG_TAG, "Accessing the net for: " + resourceToFetch);
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
            jsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error " + e.toString());
            return null;
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
        return jsonStr;
    }

}
