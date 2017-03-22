package org.nypl.simplified.offlinequeue;

import android.app.Application;
import android.widget.Toast;

/**
 * Created by aferditamuriqi on 3/22/17.
 */

public class Simplified extends Application implements ConnectivityReceiver.ConnectivityReceiverListener {

    private final String TAG = this.getClass().getName();

    private NYPLRequestQueue request_queue;

    private static Simplified simplified_instance;

    public static synchronized Simplified getInstance() {
        return simplified_instance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivity_receiver_listener = listener;
    }

    public NYPLRequestQueue getRequestQueue() {
        return this.request_queue;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NYPLVolley.init(getApplicationContext());

        NYPLSQLiteHelper database_helper = new NYPLSQLiteHelper(getApplicationContext());

        this.request_queue = new NYPLRequestQueue(database_helper);

        simplified_instance = this;

        setConnectivityListener(this);

        boolean isConnected = ConnectivityReceiver.isConnected();
        showToast(isConnected);

    }

    @Override
    public void onNetworkConnectionChanged(boolean connected) {
        showToast(connected);

        if (connected) {
            this.request_queue.retryQueue();
        }


    }

    private void showToast(boolean isConnected) {
        String message;
        if (isConnected) {
            message = "Good! Connected to Internet";
        } else {
            message = "Sorry! Not connected to internet";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
