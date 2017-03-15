package com.example.gregoneill.sqlitesimplye;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;

public class NYPLRequestQueue {

    private Context context;
    private NYPLSQLiteHelper databaseHelper;

    public static final int MAX_RETRIES_IN_QUEUE = 5;


    public NYPLRequestQueue(Context context) {
        this.context = context;
        this.databaseHelper = new NYPLSQLiteHelper(context);
    }

    public void queueRequest(int libraryID,
                             String updateID,
                             String requestURL,
                             int method,
                             String json,
                             String headers) {

//        if (request should be queued) {
            networkRequest(null, libraryID, updateID, requestURL, method, json, headers);
//        }
    }

    public void retryQueue() {

        Cursor cursor = databaseHelper.retryQueue();

        int count = 1;
        cursor.moveToFirst();
        while(cursor.isAfterLast() == false) {
            retryQueuedRequest(cursor);
            cursor.moveToNext();
            count++;
        }
        cursor.close();
    }

    private void retryQueuedRequest(Cursor cursor) {
        int retries = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_RETRIES));

        if (retries > MAX_RETRIES_IN_QUEUE) {
            databaseHelper.deleteRow(cursor);
            Log.i(null, "Removing after too many retries");
            return;
        }

        databaseHelper.incrementRetryCount(cursor);
        retryNetworkRequest(cursor);
    }

    private void retryNetworkRequest(final Cursor cursor) {
        final int rowID = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_ID));
        int method = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_METHOD));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_URL));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_PARAMETERS));
        int libraryID = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_LIBRARY));
        String updateID = cursor.getString(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_UPDATE));
//        String headers = cursor.getString(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_HEADER));

        //temp
        String username = "gregnypl9";
        String password = "1234";

        networkRequest(cursor, libraryID, updateID, url, method, null, null); //temp
    }

    //This can be refactored into its own class if there is one class for all networking
    private void networkRequest(final Cursor cursor,
                                final int libraryID,
                                final String updateID,
                                final String requestURL,
                                final int method,
                                final String json,
                                final String headers) {

        //Simple StringRequest
        RequestQueue queue = Volley.newRequestQueue(this.context);
        StringRequest stringRequest = new StringRequest(method, requestURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response) {
                        Log.i(null, "Success with Network Request");
                        if (cursor != null) {
                            databaseHelper.deleteRow(cursor);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (cursor != null) {
                            Log.i(null, "Error retrying request. Staying in queue.");
                        } else {
                            Log.i(null, "Error with request. Adding to queue.");
                            databaseHelper.saveRequest(libraryID, updateID, requestURL, method, json, headers);
                        }
                    }
        });
        queue.add(stringRequest);
    }
}
