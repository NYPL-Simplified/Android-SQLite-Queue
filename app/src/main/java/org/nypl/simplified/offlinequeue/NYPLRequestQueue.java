package org.nypl.simplified.offlinequeue;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NYPLRequestQueue {

    private static final int MAX_RETRIES_IN_QUEUE = 5;
    private String TAG = this.getClass().getName();
    private Context context;
    private NYPLSQLiteHelper databaseHelper;

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
        while (!cursor.isAfterLast()) {
            retryQueuedRequest(cursor);
            cursor.moveToNext();
            count++;
        }
        cursor.close();
    }

    private void retryQueuedRequest(Cursor cursor) {

        int retries = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_RETRIES));
        int row = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_ID));

        if (retries > MAX_RETRIES_IN_QUEUE) {
            databaseHelper.deleteRow(row);
            Log.d(TAG, "Removing after too many retries");
            return;
        }

        databaseHelper.incrementRetryCount(cursor);
        retryNetworkRequest(cursor);
    }

    private void retryNetworkRequest(final Cursor cursor) {

        //Decode any necessary columns to retry request
        //TODO handle JSON and Auth Headers
        int rowID = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_ID));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_URL));
        int method = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_METHOD));
        int libraryID = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_LIBRARY_ID));
        String updateID = cursor.getString(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_UPDATE_ID));

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
                    public void onResponse(String response) {
                        Log.d(TAG, "Success with Network Request");
                        if (isRetry) {
                            databaseHelper.deleteRow(rowID);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isRetry) {
                    Log.d(TAG, "Error retrying request. Staying in queue.");
                } else {
                    Log.d(TAG, "Error with request. Adding to queue.");
                    databaseHelper.addRequest(libraryID, updateID, requestURL, method, json, headers);
                }
            }
        });
        queue.add(stringRequest);
    }

    public void close() {
        databaseHelper.close();
    }
}
