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
 * @version April 26, 2015
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
     * @param pin {@link GeospatialPin} location-based data to be included in the new table row.
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
        values.put(DatabaseHelper.Keys.COLUMN_NAME_DURATION, pin.getDuration());
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LAT,
                String.valueOf(pin.getLocation().getLatitude()));
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LONG,
                String.valueOf(pin.getLocation().getLongitude()));

        // Write the entry into the database
        database.insert (
                DatabaseHelper.Keys.TABLE_NAME,
                DatabaseHelper.Keys.COLUMN_NAME_NULLABLE,
                values);
    }

    /**
     * @return the most recent entry in the database as a
     * {@link com.google.android.visualimprints.GeospatialPin} object
     */
    public GeospatialPin getMostRecentEntry() {
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

        if (c.moveToFirst()) {
            String arrivalTime = c.getString(
                    c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME));
            double latitude = c.getDouble(
                    c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LAT));
            double longitude = c.getDouble(
                    c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LONG));
            int duration = c.getInt(
                    c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_DURATION));
            String address = c.getString(
                    c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_ADDRESS));
            c.close();
            return constructGeospatialPin(arrivalTime, latitude, longitude, duration, address);
        }
        c.close();
        return null;
    }

    /**
     * @return all entries in the database as a list of
     * {@link com.google.android.visualimprints.GeospatialPin} objects
     */
    public ArrayList<GeospatialPin> getAllEntries() {
        String sortOrder = DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME + " ASC";

        Cursor c = database.query(
                DatabaseHelper.Keys.TABLE_NAME,         // The table to query
                DatabaseHelper.Keys.getAllColumns(),    // The columns to return
                null,                                   // The columns for the WHERE clause
                null,                                   // The values for the WHERE clause
                null,                                   // Row groupings
                null,                                   // Row group filters
                sortOrder                               // Sort order
        );

        ArrayList<GeospatialPin> geospatialPinList = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                String arrivalTime = c.getString(
                        c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME));
                double latitude = c.getDouble(
                        c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LAT));
                double longitude = c.getDouble(
                        c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LONG));
                int duration = c.getInt(
                        c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_DURATION));
                String address = c.getString(
                        c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_ADDRESS));
                GeospatialPin pin =
                        constructGeospatialPin(arrivalTime, latitude, longitude, duration, address);
                geospatialPinList.add(pin);
            } while (c.moveToNext());
        }
        c.close();
        return geospatialPinList;
    }

    /**
     * Creates a {@link com.google.android.visualimprints.GeospatialPin} object from database row
     * components.
     *
     * @param arrivalTime {@link String} the time of arrival at the location
     * @param latitude {long} the latitude portion of the location
     * @param longitude {long} the longitude portion of the location
     * @param duration {int} the number of seconds spent at the location
     * @param address {String} representation of the address associated with the location
     * @return a constructed
     */
    private GeospatialPin constructGeospatialPin(
            String arrivalTime, double latitude, double longitude, int duration, String address) {
        try {
            // Extract Date Information
            SimpleDateFormat dateFormatter =
                    new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
            Date arrivalDate = dateFormatter.parse(arrivalTime);

            // Reconstruct location information
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            GeospatialPin pin = new GeospatialPin(location, arrivalDate, duration);
            //TODO(clidwin): Create the address object and then uncomment the line below
            //pin.setAddress(pinAddress);
            return pin;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
