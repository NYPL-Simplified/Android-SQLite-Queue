package org.nypl.simplified.offlinequeue;

import android.app.Application;

/**
 * Created by aferditamuriqi on 3/22/17.
 */

public class Simplified extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NYPLVolley.init(getApplicationContext());
    }
}
