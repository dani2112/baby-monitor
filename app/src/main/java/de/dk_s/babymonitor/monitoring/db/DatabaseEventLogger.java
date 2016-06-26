package de.dk_s.babymonitor.monitoring.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class DatabaseEventLogger {

    private Context context;

    private DatabaseEventLoggerDbHelper dbHelper;

    public  DatabaseEventLogger(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseEventLoggerDbHelper(context);
    }

    public void performDatabaseIntegrityCheck() {

    }

    public void logAlarmEnabled(long timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE, 1);
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP, timestamp);
        values.putNull(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ASSOCIATED_EVENT);
        db.insert(DatabaseEventLoggerContract.LogEvent.TABLE_NAME, null, values);
    }

    public void getLastEntry() {
        String[] projection = {
                DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ID,
                DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE,
        };

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(
                DatabaseEventLoggerContract.LogEvent.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
    }

    public void logAlarmDisabled(long timestamp, long associatedEvent) {

    }

}
