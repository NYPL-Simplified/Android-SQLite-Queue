package com.example.gregoneill.sqlitesimplye;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NYPLSQLiteHelper extends SQLiteOpenHelper {

    private Context context;

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "NetworkQueue.db";

    public static final String TABLE_NAME = "queueTable";
    public static final String COLUMN_ID = "columnID";
    public static final String COLUMN_LIBRARY = "libraryIdentifier";
    public static final String COLUMN_UPDATE = "updateIdentifier";
    public static final String COLUMN_URL = "requestURL";
    public static final String COLUMN_METHOD = "requestMethod";
    public static final String COLUMN_PARAMETERS = "requestParameters";
    public static final String COLUMN_HEADER = "requestHeader";
    public static final String COLUMN_RETRIES = "retryCount";
    public static final String COLUMN_DATE_CREATED = "dateCreated";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_LIBRARY + " INTEGER," +
                    COLUMN_UPDATE + " TEXT," +
                    COLUMN_URL + " TEXT," +
                    COLUMN_METHOD + " INTEGER," +
                    COLUMN_PARAMETERS + " TEXT," +
                    COLUMN_HEADER + " TEXT," +
                    COLUMN_RETRIES + " INTEGER," +
                    COLUMN_DATE_CREATED + " INTEGER)";

    private static final String SQL_DELETE_ALL_ENTRIES =
            "DROP TABLE IF EXISTS " + NetworkQueueContract.QueueTable.TABLE_NAME;

    private static final String SQL_UPDATE_QUERY = COLUMN_LIBRARY + " = ? AND " +
            COLUMN_UPDATE + " = ? AND " + COLUMN_UPDATE + " IS NOT NULL";

    private static final String SQL_UPDATE_BY_ID = NetworkQueueContract.QueueTable._ID + " = ?";



    public NYPLSQLiteHelper(Context context) {
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
        values.put(NetworkQueueContract.QueueTable.COLUMN_PARAMETERS, json);
        values.put(NetworkQueueContract.QueueTable.COLUMN_HEADER, headers);

        //Update Row
        int count = db.update(NetworkQueueContract.QueueTable.TABLE_NAME, values, SQL_UPDATE_QUERY, args);
        if (count > 0) {
            Log.i(null, "SQLite Row Updated - Success");
        } else {
            //Insert New Row
            long currentTime = System.currentTimeMillis();
            int retries = 0;
            ContentValues newRowValues = new ContentValues();
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_LIBRARY_ID, libraryID);
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_UPDATE_ID, updateID);
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_URL, requestURL);
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_METHOD, method);
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_PARAMETERS, json);
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_HEADER, headers);
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_RETRIES, retries);
            newRowValues.put(NetworkQueueContract.QueueTable.COLUMN_DATE_CREATED, currentTime);

            try {
                long newRowId = db.insertOrThrow(NetworkQueueContract.QueueTable.TABLE_NAME, null, newRowValues);
                Log.i(null, "SQLite Row Added - Success");
            } catch(Exception ex) {
                Log.i(null, "Error adding row to SQLite DB");
            }
        }
    }

    public Cursor retryQueue() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(NetworkQueueContract.QueueTable.TABLE_NAME, null, null, null, null, null, null, null);
    }

    public void deleteRow(int row) {
        SQLiteDatabase writable_db = getWritableDatabase();
        String selection = NetworkQueueContract.QueueTable._ID + " = ?";
        String[] selectionArgs = { String.valueOf(row) };
        writable_db.delete(NetworkQueueContract.QueueTable.TABLE_NAME, selection, selectionArgs);
        Log.d(null, "SQLite deleted row from queue");
    }

    public void incrementRetryCount(Cursor cursor) {

        int retries = cursor.getInt(cursor.getColumnIndexOrThrow(NetworkQueueContract.QueueTable.COLUMN_RETRIES));
        int rowID = cursor.getInt(cursor.getColumnIndexOrThrow(NetworkQueueContract.QueueTable._ID));

        SQLiteDatabase writable_db = getWritableDatabase();

        //Increment Retry Count
        retries++;
        String[] args = { String.valueOf(rowID) };
        ContentValues values = new ContentValues();
        values.put(NetworkQueueContract.QueueTable.COLUMN_RETRIES, retries);

        int result = writable_db.update(NetworkQueueContract.QueueTable.TABLE_NAME, values, SQL_UPDATE_BY_ID, args);
        Log.i(null, String.format("%d Row Updated for Retry Count", result));
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
