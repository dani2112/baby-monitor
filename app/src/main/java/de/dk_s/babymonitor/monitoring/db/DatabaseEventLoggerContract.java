package de.dk_s.babymonitor.monitoring.db;

import android.provider.BaseColumns;

public final class DatabaseEventLoggerContract {

    /* Inner class that defines the table contents */
    public static abstract class LogEvent implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_EVENT_TYPE = "event_type";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_ASSOCIATED_EVENT = "associated_event";
    }

}