package de.dk_s.babymonitor.monitoring.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import de.dk_s.babymonitor.gui.eventlist.EventHistoryDataProvider;
import de.dk_s.babymonitor.monitoring.BabyVoiceMonitor;

public class DatabaseEventLogger implements EventHistoryDataProvider {

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

    public long logMonitoringEnabled(long timestamp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE, 4);
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP, timestamp);
        values.putNull(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ASSOCIATED_EVENT);
        long entryId = db.insert(DatabaseEventLoggerContract.LogEvent.TABLE_NAME, null, values);
        return entryId;
    }

    public long logMonitoringDisabled(long timestamp, long associatedEvent) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE, 5);
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

    public Cursor getAllEntriesSince(long timestamp) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DatabaseEventLoggerContract.LogEvent.TABLE_NAME + " where " +
                DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP + " > " + timestamp, null);
        return cursor;
    }

    public Cursor getLastEntry() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DatabaseEventLoggerContract.LogEvent.TABLE_NAME  +
                " order by " + DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ID + " desc limit 1", null);
        return cursor;
    }

    @Override
    public List<BabyVoiceMonitor.AudioEvent> get24HoursAudioEvents() {
        List<BabyVoiceMonitor.AudioEvent> eventList = new LinkedList<>();
        long sinceTimestamp = System.currentTimeMillis() - 86400000; // 24* 60 * 60 * 1000;
        Cursor cursor = getAllEntriesSince(sinceTimestamp);
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                int eventType = cursor.getInt(cursor
                        .getColumnIndex(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE));
                long timestamp = cursor.getLong(cursor
                        .getColumnIndex(DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP));
                eventList.add(0, new BabyVoiceMonitor.AudioEvent(eventType, timestamp));
                cursor.moveToNext();
            }
        }
        return eventList;
    }

    @Override
    public BabyVoiceMonitor.AudioEvent getLastAudioEvent() {
        return null;
    }
}
