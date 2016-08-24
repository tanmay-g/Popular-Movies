package tanmaygodbole.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovieDetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_URI_KEY, getIntent().getData());

            MovieDetailFragment movieDetailFragment = new MovieDetailFragment();
            movieDetailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, movieDetailFragment)
                    .commit();
        }

    }
}
