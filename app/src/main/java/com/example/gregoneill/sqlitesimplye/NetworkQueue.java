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

public class NetworkQueue extends SQLiteOpenHelper {

    private Context context;

    public static final int MAX_RETRIES_IN_QUEUE = 5;
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NetworkQueue.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + QueueTable.TABLE_NAME + " (" +
                    QueueTable._ID + " INTEGER PRIMARY KEY," +
                    QueueTable.COLUMN_LIBRARY_ID + " INTEGER," +
                    QueueTable.COLUMN_UPDATE_ID + " TEXT," +
                    QueueTable.COLUMN_URL + " TEXT," +
                    QueueTable.COLUMN_METHOD + " INTEGER," +
                    QueueTable.COLUMN_PARAMETERS + " TEXT," +
                    QueueTable.COLUMN_HEADER + " TEXT," +
                    QueueTable.COLUMN_RETRIES + " INTEGER," +
                    QueueTable.COLUMN_DATE_CREATED + " INTEGER)";

    private static final String SQL_DELETE_ALL_ENTRIES =
            "DROP TABLE IF EXISTS " + QueueTable.TABLE_NAME;

    private static final String SQL_UPDATE_QUERY = QueueTable.COLUMN_LIBRARY_ID + " = ? AND " +
            QueueTable.COLUMN_UPDATE_ID + " = ? AND " + QueueTable.COLUMN_UPDATE_ID + " IS NOT NULL";

    private static final String SQL_UPDATE_BY_ID = QueueTable._ID + " = ?";


    public NetworkQueue(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void addRequest(int libraryID,
                           String updateID,
                           String requestURL,
                           int method,
                           String json,
                           String headers) {

        if (requestURL == null) {
            Log.i(null, "Required parameter is missing.");
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        String[] args = { String.valueOf(libraryID), updateID };

        ContentValues values = new ContentValues();
        values.put(QueueTable.COLUMN_PARAMETERS, json);
        values.put(QueueTable.COLUMN_HEADER, headers);

        //Update Row
        int count = db.update(QueueTable.TABLE_NAME, values, SQL_UPDATE_QUERY, args);
        if (count > 0) {
            Log.i(null, "SQLite Row Updated - Success");
        } else {
            //Insert New Row
            long currentTime = System.currentTimeMillis();
            int retries = 0;
            ContentValues newRowValues = new ContentValues();
            newRowValues.put(QueueTable.COLUMN_LIBRARY_ID, libraryID);
            newRowValues.put(QueueTable.COLUMN_UPDATE_ID, updateID);
            newRowValues.put(QueueTable.COLUMN_URL, requestURL);
            newRowValues.put(QueueTable.COLUMN_METHOD, method);
            newRowValues.put(QueueTable.COLUMN_PARAMETERS, json);
            newRowValues.put(QueueTable.COLUMN_HEADER, headers);
            newRowValues.put(QueueTable.COLUMN_RETRIES, retries);
            newRowValues.put(QueueTable.COLUMN_DATE_CREATED, currentTime);

            try {
                long newRowId = db.insertOrThrow(QueueTable.TABLE_NAME, null, values);
                Log.i(null, "SQLite Row Added - Success");
            } catch(Exception ex) {
                Log.i(null, "Error adding row to SQLite DB");
            }
        }
    }

    public void retryQueue() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(QueueTable.TABLE_NAME, null, null, null, null, null, null, null);

        int count = 1;
        while(cursor.moveToNext()) {
            //Retry request
            retryRequest(cursor);
            Log.i(null, String.format("Retrying Request - %d", count));
            count++;
        }
        cursor.close();
    }

    private void retryRequest(Cursor cursor) {

        SQLiteDatabase writable_db = getWritableDatabase();

        int retries = cursor.getInt(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_RETRIES));
        int rowID = cursor.getInt(cursor.getColumnIndexOrThrow(QueueTable._ID));
        String testString = cursor.getString(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_URL));

        if (retries > MAX_RETRIES_IN_QUEUE) {
            deleteRow(writable_db, rowID);
            Log.i(null, "Removing after too many retries");
            return;
        }

        //Increment Retry Count
        retries++;
        String[] args = { String.valueOf(rowID) };
        ContentValues values = new ContentValues();
        values.put(QueueTable.COLUMN_RETRIES, retries);

        int result = writable_db.update(QueueTable.TABLE_NAME, values, SQL_UPDATE_BY_ID, args);
        Log.i(null, String.format("%d Row Updated for Retry Count", result));

        performNetworkRequest(cursor);
    }

    private void deleteRow(SQLiteDatabase db, int rowID) {
        String selection = QueueTable._ID + " = ?";
        String[] selectionArgs = { String.valueOf(rowID) };
        db.delete(QueueTable.TABLE_NAME, selection, selectionArgs);
        Log.d(null, "SQLite deleted row from queue");
    }

    private void performNetworkRequest(final Cursor cursor) {

        int method = cursor.getInt(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_METHOD));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_URL));
        String body = cursor.getString(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_PARAMETERS));
        String headers = cursor.getString(cursor.getColumnIndexOrThrow(QueueTable.COLUMN_HEADER));

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



//        //Volley Network Request
//        final RequestQueue queue = Volley.newRequestQueue(this.context);
//        final NYPLStringRequest request = new NYPLStringRequest(method, url, username, password, null, body, new Response.Listener<String>() {
//            @Override
//            public void onResponse(final String string) {
//                //Success. Delete Row from Table.
//                SQLiteDatabase writable_db = getWritableDatabase();
//                deleteRow(writable_db, cursor.getColumnIndex(QueueTable._ID));
//                Log.d(null, string);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(final VolleyError error) {
//                Log.d(null, error.getLocalizedMessage());
//            }
//        });
//        queue.add(request);
    }


//SQLiteOpenHelper Methods

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Handle upgrade to db, then call onCreate()
        db.execSQL(SQL_DELETE_ALL_ENTRIES);
        onCreate(db);
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

