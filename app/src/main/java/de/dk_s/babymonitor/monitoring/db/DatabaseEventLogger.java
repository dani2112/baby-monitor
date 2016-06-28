package de.dk_s.babymonitor.monitoring.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

public class DatabaseEventLogger {

    private static final String TAG = "DatabaseEventLogger";

    private Context context;

    private DatabaseEventLoggerDbHelper dbHelper;

    public  DatabaseEventLogger(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseEventLoggerDbHelper(context);
    }

    public long logAlarmEnabled(long timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE, 1);
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP, timestamp);
        values.putNull(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ASSOCIATED_EVENT);
        long entryId = db.insert(DatabaseEventLoggerContract.LogEvent.TABLE_NAME, null, values);
        return entryId;
    }

    public long logAlarmDisabled(long timestamp, long associatedEvent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE, 3);
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP, timestamp);
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ASSOCIATED_EVENT, associatedEvent);
        long entryId = db.insert(DatabaseEventLoggerContract.LogEvent.TABLE_NAME, null, values);
        return entryId;
    }

    public Cursor getAllEntries() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DatabaseEventLoggerContract.LogEvent.TABLE_NAME, null);
        return cursor;
    }

}
