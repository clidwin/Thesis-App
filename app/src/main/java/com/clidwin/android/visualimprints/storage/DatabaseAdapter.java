package com.clidwin.android.visualimprints.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.clidwin.android.visualimprints.Constants;
import com.clidwin.android.visualimprints.location.GeospatialPin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Facilitates communication between the application and its database.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 09, 2015
 */
public class DatabaseAdapter {
    private static final String TAG = "vi-database-adapter";

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    private final SimpleDateFormat DateFormatter =
            new SimpleDateFormat(Constants.DATABASE_DATE_FORMAT, Locale.getDefault());

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
     * @param pin The location information to be encoded.
     * @return A database-ready version of location information.
     */
    private ContentValues generateContentValues(GeospatialPin pin) {
        SimpleDateFormat timeFormat =
                new SimpleDateFormat(Constants.DATABASE_TIME_FORMAT, Locale.getDefault());

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.Keys._ID, pin.getArrivalTime().hashCode());
        values.put(DatabaseHelper.Keys.COLUMN_NAME_ADDRESS, "");
        values.put(
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE,
                DateFormatter.format(pin.getArrivalTime()));
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
     *      {@link com.clidwin.android.visualimprints.location.GeospatialPin} object
     */
    public GeospatialPin getMostRecentEntry() {
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
     *      {@link com.clidwin.android.visualimprints.location.GeospatialPin} objects
     */
    public ArrayList<String> getAllEntryDates() {
        String sortOrder = DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE + " DESC";

        String [] returnColumns = {DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE};

        Cursor c = database.query(
                true,                                   // Whether a value needs to be distinct
                DatabaseHelper.Keys.TABLE_NAME,         // The table to query
                returnColumns,                          // The columns to return
                null,                                   // The columns for the WHERE clause
                null,                                   // The values for the WHERE clause
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE,   // The column to be distinct
                null,                                   // Row groupings
                sortOrder,                                   // Sort order
                null                                    // Limit
        );

        ArrayList<String> recordedDates = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                String arrivalDate = c.getString(
                        c.getColumnIndexOrThrow(DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE));
                recordedDates.add(arrivalDate);
            } while (c.moveToNext());
        }
        c.close();
        return recordedDates;
    }

    /**
     * @return all entries in the database with the given date as a list of
     *      {@link com.clidwin.android.visualimprints.location.GeospatialPin} objects
     */
    public ArrayList<GeospatialPin> getAllEntriesFromDates(String [] dates) {
        String sortOrder = DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE + " DESC, " +
                DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_TIME + " DESC";

        return getAllEntriesFromDates(sortOrder, dates);
    }

    /**
     * @return all entries in the database with the given date as a list of
     *      {@link com.clidwin.android.visualimprints.location.GeospatialPin} objects
     */
    public ArrayList<GeospatialPin> getAllEntriesFromDates(String sortOrder, String [] dates) {

        // Construct the query template.
        String whereClause = DatabaseHelper.Keys.COLUMN_NAME_ARRIVAL_DATE + " IN (?";
        for (int i=1; i <dates.length; i++) {
            whereClause+=",?";
        }
        whereClause += ")";

        Cursor c = database.query(
                DatabaseHelper.Keys.TABLE_NAME,         // The table to query
                DatabaseHelper.Keys.getAllColumns(),    // The columns to return
                whereClause,                            // The WHERE clause
                dates,                                  // The arguments for the WHERE clause
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
     * Retrieve all entries from the last 24 hours.
     *
     * @return all {@link com.clidwin.android.visualimprints.location.GeospatialPin}
     *      within the date and time range.
     */
    public ArrayList<GeospatialPin> getEntriesInDateRange(Calendar olderDay, Calendar newerDay) {
        //TODO(clidwin): Incorporate timestamp into query
        ArrayList<String> dates = new ArrayList<>();
        dates.add(DateFormatter.format(newerDay.getTime()));

        long numDays = TimeUnit.DAYS.convert(
                newerDay.getTimeInMillis() - olderDay.getTimeInMillis(), // Difference in time ms
                TimeUnit.MILLISECONDS
        );
        if (numDays > 0) {
            dates.add(DateFormatter.format(olderDay.getTime()));
            numDays--;

            // Add all the days in between the oldest and newest times
            Calendar tempCal = Calendar.getInstance();
            tempCal.setTimeInMillis(olderDay.getTimeInMillis());
            for (int i=0; i<numDays; i++) {
                tempCal.add(Calendar.DAY_OF_YEAR, 1);
                dates.add(DateFormatter.format(tempCal.getTime()));
            }

            Log.e(TAG, "Dates recorded: " + dates.size());
        }

        String [] allDates = new String[dates.size()];
        allDates = dates.toArray(allDates);

        // Retrieve all dates recorded in the range
        ArrayList<GeospatialPin> pinsInDateRange = new ArrayList<>();
        for (GeospatialPin pin: getAllEntriesFromDates(allDates)) {
            if (pin.getArrivalTime().after(olderDay.getTime()) &&
                    pin.getArrivalTime().before(newerDay.getTime())) {
                pinsInDateRange.add(pin);
            }
        }
        Log.e(TAG, "Database entries found for range " + olderDay.getTime().toString() + " to " + newerDay.getTime().toString() + ": "  + pinsInDateRange.size());

        return pinsInDateRange;
    }


    /**
     * @return all entries in the database as a list of
     *      {@link com.clidwin.android.visualimprints.location.GeospatialPin} objects
     */
    @Deprecated
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
     * Creates a {@link com.clidwin.android.visualimprints.location.GeospatialPin} object from database row
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
            SimpleDateFormat dateTimeFormatter =
                    new SimpleDateFormat(Constants.DATABASE_DATE_TIME_FORMAT, Locale.getDefault());
            Date arrivalDateTime = dateTimeFormatter.parse(arrivalDate + " " + arrivalTime);

            // Reconstruct location information
            Location location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            return new GeospatialPin(location, arrivalDateTime, duration);
            //TODO(clidwin): Create the address object and then uncomment the line below
            //GeospatialPin pin = new GeospatialPin(location, arrivalDateTime, duration);
            //pin.setAddress(pinAddress);
            //c.close();
            //return pin;
        } catch (ParseException e) {
            e.printStackTrace();
            //c.close();
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
     * @return a row of the database as a {@link com.clidwin.android.visualimprints.location.GeospatialPin}
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
     * @param pin The {@link com.clidwin.android.visualimprints.location.GeospatialPin} to remove.
     */
    public void deleteEntry(GeospatialPin pin) {
        int id = pin.getArrivalTime().hashCode();

        database.delete(DatabaseHelper.Keys.TABLE_NAME, "_id=" + id, null);
    }
}
