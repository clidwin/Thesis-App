package com.google.android.visualimprints.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.google.android.visualimprints.GeospatialPin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Facilitates communication between the application and its database.
 *
 * @author Christina Lidwin (clidwin)
 * @version April 20, 2015
 */
public class DatabaseAdapter {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context context){
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Opens the database for writing.
     * @throws SQLException
     */
    public void open() throws SQLException { database = dbHelper.getWritableDatabase(); }

    /**
     * Closes the database for writing.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Creates a new row in the database with information about a new pin.
     * @param pin {@link GeospatialPin} location-based data to be included in the new row of the table.
     */
    public void addNewEntry(GeospatialPin pin) {
        open();

        // Create a map for the new pin,
        // where the keys are the column names and values are the data pieces.
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.Keys._ID, pin.getArrivalTime().hashCode());
        values.put(DatabaseHelper.Keys.COLUMN_NAME_ADDRESS, "");
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME,
                pin.getArrivalTime().toString());
        values.put(DatabaseHelper.Keys.COLUMN_NAME_LOCATION, pin.getLocation().toString());
        values.put(DatabaseHelper.Keys.COLUMN_NAME_DURATION, pin.getDuration());

        // Write the entry into the database
        database.insert (
                DatabaseHelper.Keys.TABLE_NAME,
                DatabaseHelper.Keys.COLUMN_NAME_NULLABLE,
                values);
    }

    public ArrayList<Date> getAllEntries() {
        String sortOrder = DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME + " DESC";

        Cursor c = database.query(
                DatabaseHelper.Keys.TABLE_NAME,         // The table to query
                DatabaseHelper.Keys.getAllColumns(),    // The columns to return
                null,                                   // The columns for the WHERE clause
                null,                                   // The values for the WHERE clause
                null,                                   // Row groupings
                null,                                   // Row group filters
                sortOrder                               // Sort order
        );

        ArrayList<Date> geospatialPinList = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                String arrivalTime = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME));
                geospatialPinList.add(constructGeospatialPin(arrivalTime));
            } while (c.moveToNext());
        }
        return geospatialPinList;
    }

    private Date constructGeospatialPin(String arrivalTime) {
        try {
            // Extract Date Information
            SimpleDateFormat dateFormatter =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
            Date arrivalDate = dateFormatter.parse(arrivalTime);

            // Reconstruct location information
            Location location = new Location("");
            location.setLatitude(0);
            location.setLongitude(0);

            return arrivalDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
