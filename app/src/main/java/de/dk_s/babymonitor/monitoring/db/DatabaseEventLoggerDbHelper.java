package de.dk_s.babymonitor.monitoring.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.dk_s.babymonitor.monitoring.db.DatabaseEventLoggerContract;

public class DatabaseEventLoggerDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BabyMonitor.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DatabaseEventLoggerContract.LogEvent.TABLE_NAME + " (" +
                    DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                    DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_EVENT_TYPE + INTEGER_TYPE + COMMA_SEP +
                    DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_TIMESTAMP + INTEGER_TYPE + COMMA_SEP +
                    DatabaseEventLoggerContract.LogEvent.COLUMN_NAME_ASSOCIATED_EVENT + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DatabaseEventLoggerContract.LogEvent.TABLE_NAME;


    public DatabaseEventLoggerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
