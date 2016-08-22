package tanmaygodbole.popularmovies;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tanmay.godbole on 20-08-2016
 */
public abstract class DetailsContentAdapter extends BaseAdapter {
    ArrayList<Map.Entry<String, String>> mData;
    Context context;

    public DetailsContentAdapter(Context context, HashMap<String, String> map) {
        mData = new ArrayList<>();
        this.context = context;
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        //TODO see if adding unique id auto saves state
        return 0;
    }

}
