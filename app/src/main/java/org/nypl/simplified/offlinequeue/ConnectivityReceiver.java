package org.nypl.simplified.offlinequeue;

/**
 * Created by aferditamuriqi on 3/22/17.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityReceiver
        extends BroadcastReceiver {

    public static ConnectivityReceiverListener connectivity_receiver_listener;

    public ConnectivityReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active_network = cm.getActiveNetworkInfo();
        boolean isConnected = active_network != null
                && active_network.isConnectedOrConnecting();

        if (connectivity_receiver_listener != null) {
            connectivity_receiver_listener.onNetworkConnectionChanged(isConnected);
        }
    }

    public static boolean isConnected() {
        ConnectivityManager
                connectivity_manager = (ConnectivityManager) Simplified.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active_network = connectivity_manager.getActiveNetworkInfo();
        return active_network != null
                && active_network.isConnectedOrConnecting();
    }


    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }

}