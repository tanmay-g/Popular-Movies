package tanmaygodbole.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements MainFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String MOVIEDETAILFRAGMENT_TAG = "MDFTAG";
    boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.i(LOG_TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), MOVIEDETAILFRAGMENT_TAG)
                        .commit();
            }
        }
        else {
            mTwoPane = false;
            //static main fragment is already added in this case
        }
    }

    @Override
    public void onItemSelected(Uri detailUri) {
        if (mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI_KEY, detailUri);

            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, movieDetailFragment, MOVIEDETAILFRAGMENT_TAG)
                    .commit();

        }
        else {
            //launch intent for MoviesDetailActivity
            Intent detailCallingIntent = new Intent(this, MovieDetailActivity.class);
            detailCallingIntent.setData(detailUri);
            startActivity(detailCallingIntent);
        }
    }
}
