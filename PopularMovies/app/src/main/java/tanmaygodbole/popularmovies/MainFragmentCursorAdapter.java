package tanmaygodbole.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Tanmay.godbole on 08-08-2016
 */
public class MainFragmentCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = MainFragmentCursorAdapter.class.getSimpleName();

    public MainFragmentCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.grid_item_image, parent, false);
    }

    static File getPosterFile(Context context, String movieId){
//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root + "/" + context.getString(R.string.image_dir));
        File myDir = context.getFilesDir();
        String name = movieId + ".jpg";
        File image = new File(myDir, name);
        if (!(image.exists() && image.canRead())) {
            Log.d(LOG_TAG, "Apparently the image can't be read: "+image.getAbsolutePath());
        }

        return image;
    }

    static void addPoster(Context context, ImageView posterView, File posterImage){
        Picasso
                .with(context)
                .load(posterImage)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(posterView);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String movieId = String.valueOf(cursor.getLong(MainFragment.COL_MOVIE_ID));
        File posterImage = getPosterFile(context, movieId);
        ImageView image = (ImageView) view.findViewById(R.id.grid_item_image_imageview);
        if (R.id.grid_item_image_imageview != image.getId())
            Log.e(LOG_TAG, "Different image view??");

//        image.setAdjustViewBounds(true);
        addPoster(context, image, posterImage);
    }
}
