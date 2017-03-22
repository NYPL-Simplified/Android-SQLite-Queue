package org.nypl.simplified.offlinequeue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NYPLSQLiteHelper extends SQLiteOpenHelper {

    private String TAG = this.getClass().getName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "simplified.db";

    private static final String SQL_TABLE_CREATE_OFFLINE_QUEUE =
            "CREATE TABLE " + OfflineQueueModel.TABLE_OFFLINE_QUEUE + " (" +
                    OfflineQueueModel.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    OfflineQueueModel.COLUMN_LIBRARY_ID + " INTEGER," +
                    OfflineQueueModel.COLUMN_UPDATE_ID + " TEXT," +
                    OfflineQueueModel.COLUMN_URL + " TEXT," +
                    OfflineQueueModel.COLUMN_METHOD + " TEXT," +
                    OfflineQueueModel.COLUMN_PARAMETERS + " TEXT," +
                    OfflineQueueModel.COLUMN_HEADER + " TEXT," +
                    OfflineQueueModel.COLUMN_RETRIES + " INTEGER)";

    private static final String SQL_UPDATE_OFFLINE_QUEUE_ENTRY = OfflineQueueModel.COLUMN_LIBRARY_ID + " = ? AND " +
            OfflineQueueModel.COLUMN_UPDATE_ID + " = ? AND " + OfflineQueueModel.COLUMN_UPDATE_ID + " IS NOT NULL";

    private static final String SQL_UPDATE_OFFLINE_QUEUE_BY_ID = OfflineQueueModel.COLUMN_ID + " = ?";


    private SQLiteDatabase db = getWritableDatabase();
    private SQLiteDatabase db_read = getReadableDatabase();

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_TABLE_CREATE_OFFLINE_QUEUE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public NYPLSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addRequest(int library_id,
                           String update_id,
                           String request_url,
                           int method,
                           String json,
                           String headers) {

        if (request_url == null) {
            Log.d(TAG, "Required parameter to save is missing.");
            return;
        }

        String[] args = {String.valueOf(library_id), update_id};


        ContentValues values = new ContentValues();
        values.put(OfflineQueueModel.COLUMN_PARAMETERS, json);
        values.put(OfflineQueueModel.COLUMN_HEADER, headers);


        //Update Row
        int count = this.db.update(OfflineQueueModel.TABLE_OFFLINE_QUEUE, values, SQL_UPDATE_OFFLINE_QUEUE_ENTRY, args);
        if (count > 0) {
            Log.d(TAG, "SQLite Row Updated - Success");
        } else {
            //Insert New Row
            int retries = 0;
            values.put(OfflineQueueModel.COLUMN_LIBRARY_ID, library_id);
            values.put(OfflineQueueModel.COLUMN_UPDATE_ID, update_id);
            values.put(OfflineQueueModel.COLUMN_URL, request_url);
            values.put(OfflineQueueModel.COLUMN_METHOD, method);
            values.put(OfflineQueueModel.COLUMN_PARAMETERS, json);
            values.put(OfflineQueueModel.COLUMN_HEADER, headers);
            values.put(OfflineQueueModel.COLUMN_RETRIES, retries);

            try {
                long newRowId = this.db.insertOrThrow(OfflineQueueModel.TABLE_OFFLINE_QUEUE, null, values);
                Log.d(TAG, "SQLite Row Added - Success");
            } catch (Exception ex) {
                Log.d(TAG, "Error adding row to SQLite DB");
            }
        }
    }

    public Cursor retryQueue() {
        return this.db_read.query(OfflineQueueModel.TABLE_OFFLINE_QUEUE, null, null, null, null, null, null, null);
    }

    public void deleteRow(int row) {
        String selection = OfflineQueueModel.COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(row)};
        this.db.delete(OfflineQueueModel.TABLE_OFFLINE_QUEUE, selection, selectionArgs);
        Log.d(TAG, "SQLite deleted row from queue");
    }

    public void incrementRetryCount(Cursor cursor) {

        int retries = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_RETRIES));
        int rowID = cursor.getInt(cursor.getColumnIndexOrThrow(OfflineQueueModel.COLUMN_ID));

        //Increment Retry Count
        retries++;
        String[] args = {String.valueOf(rowID)};
        ContentValues values = new ContentValues();
        values.put(OfflineQueueModel.COLUMN_RETRIES, retries);

        int result = this.db.update(OfflineQueueModel.TABLE_OFFLINE_QUEUE, values, SQL_UPDATE_OFFLINE_QUEUE_BY_ID, args);
        Log.d(TAG, String.format("%d Row Updated to increment Retries", result));
    }


}
