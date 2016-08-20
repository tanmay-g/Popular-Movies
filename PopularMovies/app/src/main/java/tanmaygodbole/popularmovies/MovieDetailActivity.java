package tanmaygodbole.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovieDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();
//    private static final String fragmentSaveKey = "fragmentSaveKey";
//    private Fragment savedFragment;
//    private String movieDetailFragmentTag = "movieDetailFragmentTag";
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        if (savedFragment == null)
//            savedFragment = getSupportFragmentManager().findFragmentByTag(movieDetailFragmentTag);
//        getSupportFragmentManager().putFragment(outState, fragmentSaveKey, savedFragment);
//        super.onSaveInstanceState(outState);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new MovieDetailFragment())
                    .commit();
//            savedFragment = getSupportFragmentManager().findFragmentByTag(movieDetailFragmentTag);
        }
//        else {
//            savedFragment = getSupportFragmentManager().getFragment(savedInstanceState, fragmentSaveKey);
//            getSupportFragmentManager().beginTransaction().replace(R.id.container, savedFragment).commit();
//        }
    }
}
