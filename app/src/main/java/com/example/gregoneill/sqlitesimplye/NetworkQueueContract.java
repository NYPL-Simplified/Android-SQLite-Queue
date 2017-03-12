package com.example.gregoneill.sqlitesimplye;

import android.provider.BaseColumns;

public final class NetworkQueueContract {

    private NetworkQueueContract() {}

    public static class QueueTable implements BaseColumns {
        public static final String TABLE_NAME = "queueTable";
        public static final String COLUMN_LIBRARY = "libraryIdentifier";
        public static final String COLUMN_UPDATE = "updateIdentifier";
        public static final String COLUMN_URL = "requestURL";
        public static final String COLUMN_METHOD = "requestMethod";
        public static final String COLUMN_PARAMETERS = "requestParameters";
        public static final String COLUMN_HEADER = "requestHeader";
        public static final String COLUMN_RETRIES = "retryCount";
        public static final String COLUMN_DATE_CREATED = "dateCreated";
    }
}
