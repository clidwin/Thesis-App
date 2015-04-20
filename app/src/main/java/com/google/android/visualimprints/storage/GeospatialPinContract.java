package com.google.android.visualimprints.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Container class for defining a database structure for geospatial information.
 *
 * More information on contract classes:
 *      https://developer.android.com/training/basics/data-storage/databases.html
 *
 * @author clidwin
 * @created April 19, 2015
 * @modified April 19, 2015
 */
public class GeospatialPinContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public GeospatialPinContract() {}

    /* Inner class that defines the table contents */
    public static abstract class Entry implements BaseColumns {
        public static final String TABLE_NAME = "pins";

        // Column names
        public static final String COLUMN_NAME_NULLABLE = null;
        public static final String COLUMN_NAME_ARRIVAL_TIME = "arrivalTime";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_LOCATION = "location";
    }
}
