package tanmaygodbole.popularmovies.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Tanmay.godbole on 02-08-2016
 */
public class DetailFetchService extends IntentService {

    private static final String LOG_TAG = DetailFetchService.class.getSimpleName();
    public DetailFetchService() {
        super(LOG_TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Dunno if needed

    }
}
