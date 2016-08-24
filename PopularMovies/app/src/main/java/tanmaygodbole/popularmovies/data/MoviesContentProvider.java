package tanmaygodbole.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by Tanmay.godbole on 02-08-2016
 */
public class MoviesContentProvider extends ContentProvider {

    private static final String LOG_TAG = MoviesContentProvider.class.getSimpleName();

    static final int POPULAR = 100;
    static final int POPULAR_WITH_ID = 101;
    static final int TOPRATED = 200;
    static final int TOPRATED_WITH_ID = 201;
    static final int FAVOURITES = 300;
    static final int FAVOURITES_WITH_ID = 301;
    static final int MOVIES = 400;
    static final int MOVIES_WITH_ID = 401;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    private static final SQLiteQueryBuilder sMoviesQueryBuilder;
    private static final SQLiteQueryBuilder sFavouritesQueryBuilder;
    private static final SQLiteQueryBuilder sPopularQueryBuilder;
    private static final SQLiteQueryBuilder sTopratedQueryBuilder;

    static {
        sMoviesQueryBuilder = new SQLiteQueryBuilder();
        sMoviesQueryBuilder.setTables(MoviesDataContract.MoviesEntry.TABLE_NAME);

        sFavouritesQueryBuilder = new SQLiteQueryBuilder();
        sFavouritesQueryBuilder.setTables(MoviesDataContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                MoviesDataContract.FavouritesEntry.TABLE_NAME + " ON " +
                MoviesDataContract.MoviesEntry.TABLE_NAME + "." +
                MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " = " +
                MoviesDataContract.FavouritesEntry.TABLE_NAME + "." +
                MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID);

        sPopularQueryBuilder = new SQLiteQueryBuilder();
        sPopularQueryBuilder.setTables(MoviesDataContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                MoviesDataContract.PopularEntry.TABLE_NAME + " ON " +
                MoviesDataContract.MoviesEntry.TABLE_NAME + "." +
                MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " = " +
                MoviesDataContract.PopularEntry.TABLE_NAME + "." +
                MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID);

        sTopratedQueryBuilder = new SQLiteQueryBuilder();
        sTopratedQueryBuilder.setTables(MoviesDataContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                MoviesDataContract.TopratedEntry.TABLE_NAME + " ON " +
                MoviesDataContract.MoviesEntry.TABLE_NAME + "." +
                MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " = " +
                MoviesDataContract.TopratedEntry.TABLE_NAME + "." +
                MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = MoviesDataContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_MOVIES,
                MOVIES);
        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_MOVIES + "/*",
                MOVIES_WITH_ID);
        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_FAVOURITES,
                FAVOURITES);
        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_FAVOURITES + "/*",
                FAVOURITES_WITH_ID);
        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_POPULAR,
                POPULAR);
        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_POPULAR + "/*",
                POPULAR_WITH_ID);
        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_TOPRATED,
                TOPRATED);
        uriMatcher.addURI(authority,
                MoviesDataContract.PATH_TOPRATED + "/*",
                TOPRATED_WITH_ID);

        return uriMatcher;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case POPULAR:
                return MoviesDataContract.PopularEntry.CONTENT_TYPE;
            case POPULAR_WITH_ID:
                return MoviesDataContract.PopularEntry.CONTENT_ITEM_TYPE;
            case TOPRATED:
                return MoviesDataContract.TopratedEntry.CONTENT_TYPE;
            case TOPRATED_WITH_ID:
                return MoviesDataContract.TopratedEntry.CONTENT_ITEM_TYPE;
            case FAVOURITES:
                return MoviesDataContract.FavouritesEntry.CONTENT_TYPE;
            case FAVOURITES_WITH_ID:
                return MoviesDataContract.FavouritesEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MoviesDataContract.MoviesEntry.CONTENT_TYPE;
            case MOVIES_WITH_ID:
                return MoviesDataContract.MoviesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnUri;
        switch (sUriMatcher.match(uri)){
            case POPULAR:{
//                Log.i(LOG_TAG, "Query for popular");
//                Log.d("PROVIDER", Arrays.toString(projection));
                returnUri = sPopularQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case POPULAR_WITH_ID:{
//                Log.i(LOG_TAG, "Query for popular with id");
                String select = MoviesDataContract.PopularEntry.TABLE_NAME + "." +
                        MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID + " =? ";
                String id = MoviesDataContract.PopularEntry.getMovieIdFromUri(uri);
                returnUri = sPopularQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        select,
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TOPRATED:{
//                Log.i(LOG_TAG, "Query for toprated");
                returnUri = sTopratedQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case TOPRATED_WITH_ID:{
//                Log.i(LOG_TAG, "Query for toprated with id");
                String select = MoviesDataContract.TopratedEntry.TABLE_NAME + "." +
                        MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID + " =? ";
                String id = MoviesDataContract.TopratedEntry.getMovieIdFromUri(uri);
                returnUri = sTopratedQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        select,
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FAVOURITES:{
//                Log.i(LOG_TAG, "Query for fav");
                returnUri = sFavouritesQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case FAVOURITES_WITH_ID:{
                //Log.i(LOG_TAG, "Query for fav with id");
                String select = MoviesDataContract.FavouritesEntry.TABLE_NAME + "." +
                        MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID + " =? ";
                String id = MoviesDataContract.FavouritesEntry.getMovieIdFromUri(uri);
                returnUri = sFavouritesQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        select,
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIES:{
                //Log.i(LOG_TAG, "Query for movies");
                returnUri = sMoviesQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case MOVIES_WITH_ID:{
                //Log.i(LOG_TAG, "Query for movie with id");
                String select = MoviesDataContract.MoviesEntry.TABLE_NAME + "." +
                        MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " =? ";
                String id = MoviesDataContract.MoviesEntry.getMovieIdFromUri(uri);
                //Log.d("Provider", "Query for URI: " + uri + ", and id: " + id);
                returnUri = sMoviesQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        select,
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        returnUri.setNotificationUri(getContext().getContentResolver(), uri);
        return returnUri;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)){
            case POPULAR:{
                long _id = db.insert(MoviesDataContract.PopularEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 ) {
                    String movieId = contentValues.getAsString(MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID);
                    returnUri = MoviesDataContract.PopularEntry.buildPopularUri(
                            Long.parseLong(movieId));
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TOPRATED:{
                long _id = db.insert(MoviesDataContract.TopratedEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 ) {
                    String movieId = contentValues.getAsString(MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID);
                    returnUri = MoviesDataContract.TopratedEntry.buildTopRatedUri(
                            Long.parseLong(movieId));
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case FAVOURITES:{
                long _id = db.insert(MoviesDataContract.FavouritesEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 ) {
                    String movieId = contentValues.getAsString(MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID);
                    returnUri = MoviesDataContract.FavouritesEntry.buildFavouritesUri(
                            Long.parseLong(movieId));
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIES:{
                long _id = db.insert(MoviesDataContract.MoviesEntry.TABLE_NAME, null, contentValues);
                if ( _id > 0 ) {
                    String movieId = contentValues.getAsString(MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID);
                    returnUri = MoviesDataContract.MoviesEntry.buildMovieUri(
                            Long.parseLong(movieId));
                }
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int deleted;
        if (selection == null)
            selection = "1";

        switch (sUriMatcher.match(uri)){
            case POPULAR:{
                deleted = db.delete(MoviesDataContract.PopularEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case TOPRATED:{
                deleted = db.delete(MoviesDataContract.TopratedEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case FAVOURITES:{
                deleted = db.delete(MoviesDataContract.FavouritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MOVIES:{
                deleted = db.delete(MoviesDataContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (deleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int updated;
        switch (sUriMatcher.match(uri)){
            case POPULAR:{
                updated = db.update(MoviesDataContract.PopularEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case TOPRATED:{
                updated = db.update(MoviesDataContract.TopratedEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case FAVOURITES:{
                updated = db.update(MoviesDataContract.FavouritesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case MOVIES:{
                updated = db.update(MoviesDataContract.MoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (updated != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return updated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int returnCount = 0;
        switch (sUriMatcher.match(uri)){
            case POPULAR:{
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesDataContract.PopularEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case TOPRATED:{
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesDataContract.TopratedEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case FAVOURITES:{
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesDataContract.FavouritesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case MOVIES:{
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(MoviesDataContract.MoviesEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_IGNORE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
