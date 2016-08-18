//package tanmaygodbole.popularmovies;
//
//import android.content.Context;
//import android.net.Uri;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//
//import com.squareup.picasso.Picasso;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Adapter to populate the gridview from the Arraylist of MovieData objects
// * Created by Tanmay.godbole on 11-06-2016.
// */
//public class CustomMovieToImageAdapter extends ArrayAdapter<MovieData> {
//
//    private Context context;
//    private static final String imgSize = "w500";
//    private ArrayList<MovieData> movieDataArrayList = null;
//    private final String LOG_TAG = CustomMovieToImageAdapter.class.getSimpleName();
//    private int imageLayoutResource;
//
//    public CustomMovieToImageAdapter(Context context, int resource, int textViewResourceId, List<MovieData> objects) {
//        super(context, resource, textViewResourceId, objects);
//        this.imageLayoutResource = resource;
//        this.context=context;
//        this.movieDataArrayList = (ArrayList<MovieData>) objects;
//    }
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        //Log.i(LOG_TAG, "Will load: " + getItem(position));
//        ImageView posterView = (ImageView) convertView;
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        if (convertView == null) {
//            posterView = (ImageView) inflater.inflate(imageLayoutResource, null);
//        }
//        String posterPath = getItem(position).getPosterPath();
//        //Log.i(LOG_TAG, "Building poster url on: " + posterPath);
//        posterAdder(context, posterPath, posterView);
//
//        return posterView;
//    }
//
//    static void posterAdder(Context context, String posterPath, ImageView posterView){
//        Uri.Builder posterPathBuilder = new Uri.Builder();
//        posterPathBuilder.scheme("http")
//                .authority("image.tmdb.org")
//                .appendPath("t")
//                .appendPath("p")
//                .appendPath(imgSize)
//                .appendEncodedPath(posterPath);
//
//        Picasso
//                .with(context)
//                .load(posterPathBuilder.build().toString())
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.error)
//                .into(posterView);
//    }
//}
///*
//*/