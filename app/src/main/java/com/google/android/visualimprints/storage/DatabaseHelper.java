package com.google.android.visualimprints.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Class handling the creation, deletion, and upgrade of the database.
 *
 * @author clidwin
 * @created April 19, 2015
 * @modified April 19, 2015
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "GeospatialPins.db";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    // Database creation statement
    private static final String GEOSPATIAL_PINS_TABLE_CREATE =
            "CREATE TABLE " + GeospatialPinContract.Entry.TABLE_NAME + " (" +
                    GeospatialPinContract.Entry._ID + " INTEGER PRIMARY KEY," +
                    GeospatialPinContract.Entry.COLUMN_NAME_ARRIVAL_TIME + TEXT_TYPE + COMMA_SEP +
                    GeospatialPinContract.Entry.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    GeospatialPinContract.Entry.COLUMN_NAME_LOCATION + TEXT_TYPE + COMMA_SEP +
                    GeospatialPinContract.Entry.COLUMN_NAME_DURATION + TEXT_TYPE +
                    " )";

    // Database deletion statement
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + GeospatialPinContract.Entry.TABLE_NAME;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GEOSPATIAL_PINS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
