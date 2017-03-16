package com.example.gregoneill.sqlitesimplye;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.logging.Logger;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class MainActivity extends AppCompatActivity {

    private NYPLRequestQueue nyplRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testNetworkQueue();
    }

    @Override
    protected void onDestroy() {
        nyplRequestQueue.close();
        super.onDestroy();
    }

    private void testNetworkQueue() {

        nyplRequestQueue = new NYPLRequestQueue(this);

        //Test Criteria
        //Fake network requests for POST and GET

        final int methodTypeGet = StringRequest.Method.GET;
        final int methodTypePost = StringRequest.Method.POST;

        //Make sure new rows are added

        networkRequest(0, methodTypeGet, false);
        networkRequest(0, methodTypeGet, false);
        networkRequest(0, methodTypeGet, false);
        networkRequest(0, methodTypePost, false);

        //Add requests that should update a row instead of inserting a new one

        networkRequest(0, methodTypeGet, true);     //Should Insert
        networkRequest(0, methodTypeGet, true);     //Should Update
        networkRequest(1, methodTypeGet, true);     //Should Insert

        //Attempt to retry queue (when online)

        //TODO find better home for retryQueue() when integrated into app
        nyplRequestQueue.retryQueue();

        //If a retry is successful, make sure it is removed from the queue
        //If a retry is not successful, make sure it remains in the queue, with the retry count incremented
        //Once a retry has reached its count limit, make sure it's removed from the queue
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

        nyplRequestQueue.queueRequest(library, updateID, url, method, null, null);
    }
}
