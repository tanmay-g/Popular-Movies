package tanmaygodbole.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tanmay.godbole on 02-08-2016
 */
public class MoviesDbHelper  extends SQLiteOpenHelper{
    private final static String LOG_TAG = MoviesDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE =
                "CREATE TABLE " + MoviesDataContract.MoviesEntry.TABLE_NAME + " ( " +
                        MoviesDataContract.MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, " +
                        MoviesDataContract.MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MoviesDataContract.MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                        MoviesDataContract.MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                        MoviesDataContract.MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                        MoviesDataContract.MoviesEntry.COLUMN_RATING + " TEXT NOT NULL " +
                        ");";

        final String SQL_CREATE_FAVOURITES_TABLE =
                "CREATE TABLE " + MoviesDataContract.FavouritesEntry.TABLE_NAME + " ( " +
                        MoviesDataContract.FavouritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, " +
                        " FOREIGN KEY ( " + MoviesDataContract.FavouritesEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MoviesDataContract.MoviesEntry.TABLE_NAME + " ( " + MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + ")" +
                        ");";

        final String SQL_CREATE_POPULAR_TABLE =
                "CREATE TABLE " + MoviesDataContract.PopularEntry.TABLE_NAME + " ( " +
                        MoviesDataContract.PopularEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, " +
                        " FOREIGN KEY ( " + MoviesDataContract.PopularEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MoviesDataContract.MoviesEntry.TABLE_NAME + " ( " + MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + ")" +
                        ");";

        final String SQL_CREATE_TOPRATED_TABLE =
                "CREATE TABLE " + MoviesDataContract.TopratedEntry.TABLE_NAME + " ( " +
                        MoviesDataContract.TopratedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL UNIQUE, " +
                        " FOREIGN KEY ( " + MoviesDataContract.TopratedEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                        MoviesDataContract.MoviesEntry.TABLE_NAME + " ( " + MoviesDataContract.MoviesEntry.COLUMN_MOVIE_ID + ")" +
                        ");";

        //Log.i(LOG_TAG, "Creating the tables");
//        Log.i(LOG_TAG, "MOVIES: " + SQL_CREATE_MOVIES_TABLE);
//        Log.i(LOG_TAG, "FAV: " + SQL_CREATE_FAVOURITES_TABLE);
//        Log.i(LOG_TAG, "POP: " + SQL_CREATE_POPULAR_TABLE);
//        Log.i(LOG_TAG, "TOP: " + SQL_CREATE_TOPRATED_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_POPULAR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TOPRATED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //not handling preserving favourites data on db version upgrade, for now
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDataContract.MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDataContract.FavouritesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDataContract.PopularEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesDataContract.TopratedEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
