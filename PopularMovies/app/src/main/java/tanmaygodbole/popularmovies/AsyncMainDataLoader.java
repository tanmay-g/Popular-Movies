package tanmaygodbole.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import tanmaygodbole.popularmovies.data.MoviesDataContract;


/**
 * Created by Tanmay.godbole on 06-08-2016
 */
public class AsyncMainDataLoader extends AsyncTaskLoader<Cursor> {
    private static String LOG_TAG = AsyncMainDataLoader.class.getSimpleName();

    private static final String imgSize = "w500";
    private static final String[] sortValues = {"popular", "top_rated"};

    public AsyncMainDataLoader(Context context, Bundle args) {
        super(context);
    }

    @Override
    public Cursor loadInBackground() {
        //assuming this is only called when we want to reload the database entries

        //now, fetch the data for both popular and top-rated paths, and store it in the db

        String popularJSON = null;
        String topratedJSON = null;

        for (String loadType:sortValues){
            String movieListJsonStr;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                Uri.Builder tmdbBuilder = new Uri.Builder();
                tmdbBuilder.scheme("http")
                        .authority("api.themoviedb.org")
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(loadType)
                        .appendQueryParameter("api_key",BuildConfig.THE_MOVIE_DATABASE_API_KEY);
                URL fetchURL = new URL(tmdbBuilder.build().toString());
                //Log.i(LOG_TAG, "doInBackground URL: " + fetchURL.toString());
                urlConnection = (HttpURLConnection) fetchURL.openConnection();
                urlConnection.setRequestMethod("GET");
                Log.e(LOG_TAG, "Accessing the net for: " + loadType);
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
                if (loadType.equals(sortValues[0]))
                    popularJSON = movieListJsonStr;
                else
                    topratedJSON = movieListJsonStr;


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
            //Log.i(LOG_TAG, "movieListJsonStr: " + movieListJsonStr);

        }
        getMovieDataFromJson(popularJSON, topratedJSON);
        return null;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMovieDataFromJson(String popularJSON, String topratedJSON) {
        //Log.i(LOG_TAG, "getMovieDataFromJson");

        Vector<ContentValues> popularDataVector = jsonToVector(popularJSON);
        Vector<ContentValues> topratedDataVector = jsonToVector(topratedJSON);
        //update DB now
        if (popularDataVector.size() > 0 || topratedDataVector.size() > 0) {
            //first delete old popular and top_rated entries not in favourites

            //delete the poster images for the above movies

            //first get the movie ids to delete for
            Cursor popularOnesToDelete = getContext().getContentResolver().query(
                    MoviesDataContract.PopularEntry.CONTENT_URI,
                    new String[]{MoviesDataContract.PopularEntry.TABLE_NAME + "." +
                            MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID + " as movie_id"},
                    MoviesDataContract.PopularEntry.TABLE_NAME + "." +
                            MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID + " NOT IN " +
                            "(SELECT " + MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID +
                            " FROM " + MoviesDataContract.FavouritesEntry.TABLE_NAME +
                            ")",
                    null,
                    null
                    );
            Cursor topratedOnesToDelete = getContext().getContentResolver().query(
                    MoviesDataContract.TopratedEntry.CONTENT_URI,
                    new String[]{MoviesDataContract.TopratedEntry.TABLE_NAME + "." +
                            MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID + " as movie_id"},
                    MoviesDataContract.TopratedEntry.TABLE_NAME + "." +
                            MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID + " NOT IN " +
                            "(SELECT " + MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID +
                            " FROM " + MoviesDataContract.FavouritesEntry.TABLE_NAME +
                            ")",
                    null,
                    null
            );
            //delete the poster files
            try {
                deleteFilesByCursor(popularOnesToDelete);
                deleteFilesByCursor(topratedOnesToDelete);
            } finally {
                popularOnesToDelete.close();
                topratedOnesToDelete.close();
            }

            //now delete from Movies
            getContext().getContentResolver().delete(
                    MoviesDataContract.MoviesEntry.CONTENT_URI,
                    MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " IN " +
                            "(SELECT " + MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID +
                            " FROM " + MoviesDataContract.PopularEntry.TABLE_NAME +
                            " WHERE " + MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID +
                            " NOT IN " +
                                "(SELECT " + MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID +
                                " FROM " + MoviesDataContract.FavouritesEntry.TABLE_NAME +
                                ")" +
                            ")",
                    null);

            getContext().getContentResolver().delete(
                    MoviesDataContract.MoviesEntry.CONTENT_URI,
                    MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " IN " +
                            "(SELECT " + MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID +
                            " FROM " + MoviesDataContract.TopratedEntry.TABLE_NAME +
                            " WHERE " + MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID +
                            " NOT IN " +
                                "(SELECT " + MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID +
                                " FROM " + MoviesDataContract.FavouritesEntry.TABLE_NAME +
                                ")" +
                            ")",
                    null);

            //now delete all entries in the popular and top rated tables
            getContext().getContentResolver().delete(
                    MoviesDataContract.PopularEntry.CONTENT_URI,
                    null,
                    null
            );
            getContext().getContentResolver().delete(
                    MoviesDataContract.TopratedEntry.CONTENT_URI,
                    null,
                    null
            );

            //now insert the new data into the tables
            ContentValues[] popCvArray = new ContentValues[popularDataVector.size()];
            ContentValues[] topCvArray = new ContentValues[topratedDataVector.size()];
            popularDataVector.toArray(popCvArray);
            topratedDataVector.toArray(topCvArray);
            try {


                getContext().getContentResolver().bulkInsert(
                        MoviesDataContract.MoviesEntry.CONTENT_URI,
                        popCvArray
                );

                getContext().getContentResolver().bulkInsert(
                        MoviesDataContract.MoviesEntry.CONTENT_URI,
                        topCvArray
                );
            }
            catch (SQLiteConstraintException sq){
                Log.e(LOG_TAG, sq.getMessage());
            }

            Vector<ContentValues> popularDataIdsVector = jsonToVector_onlyIds(popularJSON);
            Vector<ContentValues> topratedDataIdsVector = jsonToVector_onlyIds(topratedJSON);
            popularDataIdsVector.toArray(popCvArray);
            topratedDataIdsVector.toArray(topCvArray);
            getContext().getContentResolver().bulkInsert(
                    MoviesDataContract.PopularEntry.CONTENT_URI,
                    popCvArray
            );

            getContext().getContentResolver().bulkInsert(
                    MoviesDataContract.TopratedEntry.CONTENT_URI,
                    topCvArray
            );


            //now we have the movie data, but not the posters, so save the posters for these movies
            saveImageByVector(popularDataVector);
            saveImageByVector(topratedDataVector);

        }
    }

    private void deleteFilesByCursor(Cursor filesToDelete) {
        while (filesToDelete.moveToNext()) {
            String movieId = String.valueOf(filesToDelete.getLong(0));
            deleteFile(movieId);
        }
    }
    private void deleteFile(String movieId){
        File toDelete = MainFragmentCursorAdapter.getPosterFile(getContext(), movieId);
        Log.i(LOG_TAG, "Deleting file: " + toDelete.getAbsolutePath() + " " + String.valueOf(toDelete.delete()));

    }

    private void saveImageByVector(Vector<ContentValues> movieVector){
        for (ContentValues movieValues:movieVector) {
            String posterPath = movieValues.getAsString(MoviesDataContract.MoviesEntry.COLUMN_POSTER_PATH);
            String movieId = String.valueOf(movieValues.getAsLong(MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID));
            saveImage(posterPath, movieId);
        }
    }

    private String buildPosterURL(String posterPath){
        Uri.Builder posterPathBuilder = new Uri.Builder();
        posterPathBuilder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath(imgSize)
                .appendEncodedPath(posterPath);

        return posterPathBuilder.build().toString();
    }

    private void saveImage(String posterPath, final String movieId){
        try {
            Bitmap imageToSave = Picasso.with(getContext())
                    .load(buildPosterURL(posterPath))
                    .get();
            File myDir = getContext().getFilesDir();

            String name = movieId + ".jpg";
            myDir = new File(myDir, name);
            FileOutputStream out = new FileOutputStream(myDir);
            boolean compression = imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
//            Log.i(LOG_TAG, "Compression status: " + compression);
            out.flush();
            out.close();
        } catch(Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private Vector<ContentValues> jsonToVector(String movieJsonStr){
        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_ID = "id";
        final String TMDB_RESULTS = "results";
        final String TMDB_TITLE = "title";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_POSTER = "poster_path";
        final String TMDB_AVG = "vote_average";
        final String TMDB_DATE = "release_date";
        Vector<ContentValues> cVVector = null;
        try {


            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesJsonArray = movieJson.getJSONArray(TMDB_RESULTS);

            cVVector = new Vector<>(moviesJsonArray.length());

            //ArrayList<MovieData> movieDataArrayList = new ArrayList<>(moviesJsonArray.length());

            for (int i = 0; i < moviesJsonArray.length(); i++) {
                JSONObject movieEntry = moviesJsonArray.getJSONObject(i);
                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID, movieEntry.getLong(TMDB_ID));
                movieValues.put(MoviesDataContract.MoviesEntry.COLUMN_TITLE, movieEntry.getString(TMDB_TITLE));
                movieValues.put(MoviesDataContract.MoviesEntry.COLUMN_POSTER_PATH, movieEntry.getString(TMDB_POSTER));
                movieValues.put(MoviesDataContract.MoviesEntry.COLUMN_OVERVIEW, movieEntry.getString(TMDB_OVERVIEW));
                movieValues.put(MoviesDataContract.MoviesEntry.COLUMN_RELEASE_DATE, movieEntry.getString(TMDB_DATE));
                movieValues.put(MoviesDataContract.MoviesEntry.COLUMN_RATING, movieEntry.getDouble(TMDB_AVG));

                cVVector.add(movieValues);
                //Log.i(LOG_TAG, "Parsed poster path: " + movieEntry.getString(TMDB_POSTER));
            }
        }
        catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return cVVector;
    }
    private Vector<ContentValues> jsonToVector_onlyIds(String movieJsonStr){
        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_ID = "id";
        final String TMDB_RESULTS = "results";
        Vector<ContentValues> cVVector = null;
        try {


            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray moviesJsonArray = movieJson.getJSONArray(TMDB_RESULTS);

            cVVector = new Vector<>(moviesJsonArray.length());

            for (int i = 0; i < moviesJsonArray.length(); i++) {
                JSONObject movieEntry = moviesJsonArray.getJSONObject(i);
                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID, movieEntry.getLong(TMDB_ID));
                cVVector.add(movieValues);
                //Log.i(LOG_TAG, "Parsed poster path: " + movieEntry.getString(TMDB_POSTER));
            }
        }
        catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return cVVector;
    }
}
