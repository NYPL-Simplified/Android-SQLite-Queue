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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testNetworkQueue();
    }

    private void testNetworkQueue() {

        NYPLRequestQueue nyplQueue = new NYPLRequestQueue(this);

        //Fake network requests for POST and GET
        //These requests can eventually go in their own class

        String url = "http://www.mocky.io/v2/58c49e31100000c123eef42b";

        //Simple StringRequest
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response) {
                        Log.i(null, "Success with Network Request");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(null, "Error with network request");
                    }
        });
        requestQueue.add(stringRequest);

        //In response listener, put through NYPLRequestQueue to see if it should be saved

        //Make sure new rows are added

        //Add requests that should update a row instead of inserting a new one

        //Attempt to retry queue
        //If a retry is successful, make sure it is removed from the queue
        //If a retry is not successful, make sure it remains in the queue, with the retry count incremented

        //Once a retry has reached its count limit, make sure it's removed from the queue
    }
}
