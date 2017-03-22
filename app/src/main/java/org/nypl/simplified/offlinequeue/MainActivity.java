package org.nypl.simplified.offlinequeue;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;

import org.nypl.simplified.R;

public class MainActivity extends AppCompatActivity {

    private String TAG = this.getClass().getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Make sure new rows are added
        networkRequest(0, Request.Method.GET, false);
        networkRequest(0, Request.Method.POST, false);

//        //Add requests that should update a row instead of inserting a new one
        networkRequest(0, Request.Method.GET, true);     //Should Insert
        networkRequest(0, Request.Method.GET, true);     //Should Update
        networkRequest(1, Request.Method.GET, true);     //Should Insert
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    //Testing NYPLRequestQueue
    private void networkRequest(final int library, final int method, boolean update) {

        //Mocked Data and Response API
        final String url = "http://www.mocky.io/v2/58c49e31100000c123eef42b";
        final String updateID;
        if (update) {
            updateID = "sample";
        } else {
            updateID = null;
        }

        Simplified.getInstance().getRequestQueue().add(library, updateID, url, method, null, null);

    }
}
