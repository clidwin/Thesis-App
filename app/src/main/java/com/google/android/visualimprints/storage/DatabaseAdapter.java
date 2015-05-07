package com.google.android.visualimprints.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.android.visualimprints.Constants;
import com.google.android.visualimprints.GeospatialPin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Facilitates communication between the application and its database.
 *
 * @author Christina Lidwin (clidwin)
 * @version May 06, 2015
 */
public class DatabaseAdapter {
    private static final String TAG = "vi-database-adapter";

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DatabaseAdapter(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Opens the database for writing.
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Closes the database for writing.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Creates a new row in the database with information about a new pin.
     *
     * @param pin {@link GeospatialPin} location-based data to be included in the new table row.
     */
    public void addNewEntry(GeospatialPin pin) {
        ContentValues values = generateContentValues(pin);

        // Write the entry into the database
        database.insert(
                DatabaseHelper.Keys.TABLE_NAME,
                DatabaseHelper.Keys.COLUMN_NAME_NULLABLE,
                values);
        Log.d(TAG, "New entry added.");
    }

    /**
     * Constructs database-ready versions of the information to be added.
     *
     * @param pin
     * @return
     */
    private ContentValues generateContentValues(GeospatialPin pin) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATABASE_DATE_FORMAT);
        SimpleDateFormat timeFormat = new SimpleDateFormat(Constants.DATABASE_TIME_FORMAT);

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.Keys._ID, pin.getArrivalTime().hashCode());
        values.put(DatabaseHelper.Keys.COLUMN_NAME_ADDRESS, "");
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE,
                dateFormat.format(pin.getArrivalTime()));
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME,
                timeFormat.format(pin.getArrivalTime()));
        values.put(DatabaseHelper.Keys.COLUMN_NAME_DURATION, pin.getDuration());
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LAT,
                String.valueOf(roundValue(pin.getLocation().getLatitude())));
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_LOCATION_LONG,
                String.valueOf(roundValue(pin.getLocation().getLongitude())));

        return values;
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
            GeospatialPin pin = constructGeospatialPin(c);
            c.close();
            return pin;
        }
        c.close();
        return null;
    }

    /**
     * @return all entries in the database as a list of
     * {@link com.google.android.visualimprints.GeospatialPin} objects
     */
    public ArrayList<GeospatialPin> getAllEntries() {
        String sortOrder = DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE + " DESC, " +
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME + " DESC";

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
                geospatialPinList.add(constructGeospatialPin(c));
            } while (c.moveToNext());
        }
        c.close();
        return geospatialPinList;
    }

    /**
     * Creates a {@link com.google.android.visualimprints.GeospatialPin} object from database row
     * components.
     *
     * @param c {@link android.database.Cursor} A database pointer pointing to a data row
     * @return a constructed GeospatialPin
     */
    private GeospatialPin constructGeospatialPin(Cursor c) {
        String arrivalDate = c.getString(
                c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE));
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

        try {
            // Extract Date Information
            SimpleDateFormat dateFormatter =
                    new SimpleDateFormat(Constants.DATABASE_DATE_TIME_FORMAT);
            Date arrivalDateTime = dateFormatter.parse(arrivalDate + " " + arrivalTime);

            // Reconstruct location information
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            GeospatialPin pin = new GeospatialPin(location, arrivalDateTime, duration);
            //TODO(clidwin): Create the address object and then uncomment the line below
            //pin.setAddress(pinAddress);
            return pin;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Rounds a value to eight decimal places of precision.
     *
     * @param value The number to round.
     * @return The rounded value.
     */
    private double roundValue(double value) {
        int precision = 100000000; // Eight decimal places
        return (double) Math.round(value * precision) / precision;
    }

    /**
     * Retrieve an entity from the database by its id.
     *
     * @param id The identification value of the entry.
     * @return a row of the database as a {@link com.google.android.visualimprints.GeospatialPin}
     */
    public GeospatialPin getEntryById(int id) {
        String sortOrder = DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME + " DESC";
        String selection = DatabaseHelper.Keys._ID + " LIKE ?";
        String[] query = {String.valueOf(id)};

        Cursor c = database.query(
                DatabaseHelper.Keys.TABLE_NAME,         // The table to query
                DatabaseHelper.Keys.getAllColumns(),    // The columns to return
                selection,                // The columns for the WHERE clause
                query,                                   // The values for the WHERE clause
                null,                                   // Row groupings
                null,                                   // Row group filters
                sortOrder                               // Sort order
        );

        if (c.moveToFirst()) {
            GeospatialPin pin = constructGeospatialPin(c);
            c.close();
            return pin;
        }
        c.close();
        return null;
    }

    /**
     * Modify an entry in the database.
     *
     * @param pin The entity with values to update in the database.
     */
    public void updateEntry(GeospatialPin pin) {
        ContentValues values = generateContentValues(pin);
        int id = pin.getArrivalTime().hashCode();

        database.update(DatabaseHelper.Keys.TABLE_NAME, values, "_id=" + id, null);
    }

    /**
     * Remove a row in the database.
     *
     * @param pin The {@link com.google.android.visualimprints.GeospatialPin} to remove.
     */
    public void deleteEntry(GeospatialPin pin) {
        int id = pin.getArrivalTime().hashCode();

        database.delete(DatabaseHelper.Keys.TABLE_NAME, "_id=" + id, null);
    }
}
