package com.google.android.visualimprints.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.visualimprints.GeospatialPin;

/**
 * Created by clidwin on 4/19/15.
 */
public class DatabaseAdapter {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    private String[] allColumns = {
            GeospatialPinContract.Entry.COLUMN_NAME_ADDRESS,
            GeospatialPinContract.Entry.COLUMN_NAME_ARRIVAL_TIME,
            GeospatialPinContract.Entry.COLUMN_NAME_DURATION,
            GeospatialPinContract.Entry.COLUMN_NAME_LOCATION
    };

    public DatabaseAdapter(Context context){
        this.dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addNewEntry(GeospatialPin pin) {
        open();

        // Create a map for the new pin, where the keys are the columns and values are the data pieces.
        ContentValues values = new ContentValues();
        values.put(
                GeospatialPinContract.Entry.COLUMN_NAME_ADDRESS, pin.getReadableAddress());
        values.put(
                GeospatialPinContract.Entry.COLUMN_NAME_ARRIVAL_TIME,
                pin.getArrivalTime().toString());
        values.put(
                GeospatialPinContract.Entry.COLUMN_NAME_DURATION, pin.getDuration());
        values.put(
                GeospatialPinContract.Entry.COLUMN_NAME_LOCATION, pin.getLocation().toString());

        // Write the entry into the database
        database.insert (
                GeospatialPinContract.Entry.TABLE_NAME,
                GeospatialPinContract.Entry.COLUMN_NAME_NULLABLE,
                values);
    }

}
