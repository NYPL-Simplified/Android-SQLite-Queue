package com.example.gregoneill.sqlitesimplye;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class NYPLRequestQueue {

    private Context context;
    private NYPLSQLiteHelper databaseHelper;

    private static final int MAX_RETRIES_IN_QUEUE = 5;

    public NYPLRequestQueue(Context context) {
        this.context = context;
        this.databaseHelper = new NYPLSQLiteHelper(context);
    }

    /// For Activities and Network Requests that should support offline saving,
    /// this method should be used in place of a custom or NYPLStringRequest.
    public void queueRequest(int libraryID,
                             String updateID,
                             String requestURL,
                             int method,
                             String json,
                             String headers) {

            networkRequest(false, 0, libraryID, updateID, requestURL, method, json, headers);
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
        int row = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_ID));

        if (retries > MAX_RETRIES_IN_QUEUE) {
            databaseHelper.deleteRow(row);
            Log.i("NYPL", "Removing after too many retries");
            return;
        }

        databaseHelper.incrementRetryCount(cursor);
        retryNetworkRequest(cursor);
    }

    private void retryNetworkRequest(final Cursor cursor) {

        //Decode any necessary columns to retry request
        //TODO handle JSON and Auth Headers
        int rowID = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_ID));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_URL));
        int method = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_METHOD));
        int libraryID = cursor.getInt(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_LIBRARY));
        String updateID = cursor.getString(cursor.getColumnIndexOrThrow(NYPLSQLiteHelper.COLUMN_UPDATE));

        networkRequest(true, rowID, libraryID, updateID, url, method, null, null); //temp
    }

    // Used to perform network request for first time, or retry a request already in the queue.
    // Performs work accordingly in response listener.
    private void networkRequest(final boolean isRetry,
                                final int rowID,
                                final int libraryID,
                                final String updateID,
                                final String requestURL,
                                final int method,
                                final String json,
                                final String headers) {

        RequestQueue queue = Volley.newRequestQueue(this.context);
        StringRequest stringRequest = new StringRequest(method, requestURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response) {
                        Log.i("NYPL", "Success with Network Request");
                        if (isRetry) {
                            databaseHelper.deleteRow(rowID);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isRetry) {
                            Log.i("NYPL", "Error retrying request. Staying in queue.");
                        } else {
                            Log.i("NYPL", "Error with request. Adding to queue.");
                            databaseHelper.saveRequest(libraryID, updateID, requestURL, method, json, headers);
                        }
                    }
        });
        queue.add(stringRequest);
    }

    public void close() {
        databaseHelper.close();
    }
}
