package com.google.android.visualimprints.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Class handling the creation, deletion, and upgrade of the database.
 *
 * @author Christina Lidwin (clidwin)
 * @version April 20, 2015
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 4;
    private static final String DATABASE_NAME = "GeospatialPins.db";

    private static final String REAL_TYPE = " REAL";
    private static final String NUMERIC_TYPE = " NUMERIC";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    // Database creation statement
    private static final String GEOSPATIAL_PINS_TABLE_CREATE =
            "CREATE TABLE " + Keys.TABLE_NAME + " (" +
                    Keys._ID + " INTEGER PRIMARY KEY," +
                    Keys.COLUMN_NAME_ARRIVAL_TIME + TEXT_TYPE + COMMA_SEP +
                    Keys.COLUMN_NAME_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    Keys.COLUMN_NAME_DURATION + INTEGER_TYPE + COMMA_SEP +
                    Keys.COLUMN_NAME_LOCATION_LAT + REAL_TYPE + COMMA_SEP +
                    Keys.COLUMN_NAME_LOCATION_LONG + REAL_TYPE +
                    " )";

    // Database deletion statement
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Keys.TABLE_NAME;

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
        //TODO(clidwin): Transfer (don't delete) data on upgrade.
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * Contains table information (including table and column names) and related helper methods.
     */
    public static class Keys implements BaseColumns {
        public static final String TABLE_NAME = "pins";

        private static String[] allColumns = {
                DatabaseHelper.Keys._ID,
                DatabaseHelper.Keys.COLUMN_NAME_ADDRESS,
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME,
                DatabaseHelper.Keys.COLUMN_NAME_DURATION,
                DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LAT,
                Keys.COLUMN_NAME_LOCATION_LONG
        };

        // Column names
        public static final String COLUMN_NAME_NULLABLE = null;
        public static final String COLUMN_NAME_ARRIVAL_TIME = "arrivalTime";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_LOCATION_LAT = "locationLat";
        public static final String COLUMN_NAME_LOCATION_LONG = "locationLong";

        /**
         * @return all of the possible columns in this table.
         */
        public static String [] getAllColumns() {
            return allColumns;
        }
    }
}
