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

    private String TAG = this.getClass().getName();

    private static final int MAX_RETRIES_IN_QUEUE = 0;
    private NYPLSQLiteHelper database_helper;

    public NYPLRequestQueue(NYPLSQLiteHelper in_database_helper) {
        this.database_helper = in_database_helper;
    }

    /// For Activities and Network Requests that should support offline saving,
    /// this method should be used in place of a custom or NYPLStringRequest.
    public void add(int library_id,
                             String update_id,
                             String request_url,
                             int method,
                             String json,
                             String headers) {

        networkRequest(false, 0, library_id, update_id, request_url, method, json, headers);
    }

    public void retryQueue() {

        Log.d(TAG, "retryQueue");

        Cursor cursor = this.database_helper.retryQueue();


        if (cursor.moveToFirst()) {
            do {

                Log.d(TAG, "while");

                retryNetworkRequest(cursor);
                this.database_helper.incrementRetryCount(cursor);

                int retries = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_RETRIES));
                int row = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_ID));
                Log.d(TAG, String.format("retries %s",retries));

                if (retries > MAX_RETRIES_IN_QUEUE) {
                    this.database_helper.deleteRow(row);
                    Log.d(TAG, "Removing after too many retries");
                }

            } while (cursor.moveToNext());
        }

    }


    private void retryNetworkRequest(final Cursor cursor) {

        int row_id = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_ID));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_URL));
        int method = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_METHOD));
        int library_id = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_LIBRARY_ID));
        String update_id = cursor.getString(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_UPDATE_ID));

        networkRequest(true, row_id, library_id, update_id, url, method, null, null); //temp
    }

    // Used to perform network request for first time, or retry a request already in the queue.
    // Performs work accordingly in response listener.
    private void networkRequest(final boolean is_retry,
                                final int row_id,
                                final int library_id,
                                final String update_id,
                                final String request_url,
                                final int method,
                                final String json,
                                final String headers) {


        StringRequest request = new StringRequest(method, request_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Success with Network Request");
                        if (is_retry) {
                            NYPLRequestQueue.this.database_helper.deleteRow(row_id);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (is_retry) {
                    Log.d(TAG, "Error retrying request. Staying in queue.");
                } else {
                    Log.d(TAG, "Error with request. Adding to queue.");
                    NYPLRequestQueue.this.database_helper.addRequest(library_id, update_id, request_url, method, json, headers);
                }
            }
        });
        NYPLVolley.add(request);
    }


}
