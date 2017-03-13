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

import static com.example.gregoneill.sqlitesimplye.NetworkQueueContract.QueueTable;

public class NYPLRequestQueue {

    private Context context;
    private NYPLSQLiteHelper databaseHelper;

    public static final int MAX_RETRIES_IN_QUEUE = 5;


    public NYPLRequestQueue(Context context) {
        this.context = context;
        this.databaseHelper = new NYPLSQLiteHelper();
    }

    public void addRequest(int libraryID,
                           String updateID,
                           String requestURL,
                           int method,
                           String json,
                           String headers) {

//        if (request should be queued) {
            databaseHelper.addRequest(libraryID, updateID, requestURL, method, json, headers);
//        }
    }

    public void retryQueue() {

        Cursor cursor = databaseHelper.retryQueue();

        int count = 1;
        cursor.moveToFirst();
        while(cursor.isAfterLast() == false) {
            //Retry request
            retryRequest(cursor);
            Log.i(null, String.format("Retrying Request - %d", count));
            cursor.moveToNext();
            count++;
        }
        cursor.close();
    }

    private void retryRequest(Cursor cursor) {
        int retries = cursor.getInt(cursor.getColumnIndexOrThrow(NetworkQueueContract.QueueTable.COLUMN_RETRIES));
        int rowID = cursor.getInt(cursor.getColumnIndexOrThrow(QueueTable._ID));

        if (retries > MAX_RETRIES_IN_QUEUE) {
            databaseHelper.deleteRow(rowID);
            Log.i(null, "Removing after too many retries");
            return;
        }

        databaseHelper.incrementRetryCount(cursor);

        performNetworkRequest(cursor);
    }

    private void performNetworkRequest(final Cursor cursor) {

        //This can be refactored into its own class if there is one class for all networking
        //TODO: Listeners currently not working

        int method = cursor.getInt(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_METHOD));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_URL));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_PARAMETERS));
//        String headers = cursor.getString(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_HEADER));

        String username = "gregnypl9";
        String password = "1234";

        //Simple StringRequest
        RequestQueue queue = Volley.newRequestQueue(this.context);
        StringRequest stringRequest = new StringRequest(method, url,
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
        queue.add(stringRequest);
    }
}

//Since getWritableDatabase() and getReadableDatabase() are expensive to call when the database is closed,
//you should leave your database connection open for as long as you possibly need to access it.
//Typically, it is optimal to close the database in the onDestroy() of the calling Activity.
//
//@Override
//protected void onDestroy() {
//   mDbHelper.close();
//   super.onDestroy();
//}

