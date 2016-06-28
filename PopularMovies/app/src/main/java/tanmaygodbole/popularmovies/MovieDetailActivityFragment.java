package tanmaygodbole.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieDetailActivityFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();
    private MovieData selectedMovie = null;
    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent callingIntent = getActivity().getIntent();
        if (callingIntent != null && callingIntent.hasExtra(MainFragment.movieDataKey)){
            //get MovieData
            selectedMovie =  callingIntent.getExtras().getParcelable(MainFragment.movieDataKey);
        }
        if (selectedMovie==null)
            return rootView;
        populateScreen(rootView);

        return rootView;
    }

    private void populateScreen(View rootView){
        getActivity().setTitle(selectedMovie.getTitle());
        CustomMovieToImageAdapter.posterAdder(getActivity(),
                selectedMovie.getPosterPath(),
                (ImageView) rootView.findViewById(R.id.poster));
        String date = selectedMovie.getReleaseDate().split("-")[0];
        //Log.i(LOG_TAG, date);
        ((TextView)rootView.findViewById(R.id.title)).setText(selectedMovie.getTitle());
        ((TextView)rootView.findViewById(R.id.synopsis)).setText(selectedMovie.getOverview());
        ((TextView)rootView.findViewById(R.id.rating)).setText(Double.toString(selectedMovie.getRating()) + "/10");
        ((TextView)rootView.findViewById(R.id.date)).setText(date);
    }
}
