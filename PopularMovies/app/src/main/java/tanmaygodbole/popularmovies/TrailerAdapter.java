package tanmaygodbole.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tanmay.godbole on 20-08-2016
 */
public class TrailerAdapter extends DetailsContentAdapter {

    public TrailerAdapter(Context context, HashMap<String, String> map) {
        super(context, map);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //This is the LinearLayout from trailers_item_layout
        View resultView;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null){
            resultView = inflater.inflate(R.layout.trailers_item_layout, parent);
        } else {
            resultView = convertView;
        }

        Map.Entry<String, String> item = getItem(position);

        TextView trailerName = ((TextView) resultView.findViewById(R.id.trailer_name));
        trailerName.setText(item.getKey());
        trailerName.setId(View.generateViewId());

        resultView.setTag(item.getValue());

        resultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String youtubeUrlStr = (String) v.getTag();
                Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrlStr));
                youtubeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(youtubeIntent);
            }
        });


        resultView.setId(View.generateViewId());

        return resultView;
    }
}
