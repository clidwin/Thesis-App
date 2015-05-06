package com.google.android.visualimprints.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.visualimprints.Constants;
import com.google.android.visualimprints.GeospatialPin;
import com.google.android.visualimprints.storage.DatabaseAdapter;

import java.util.Date;

/**
 * Retrieves location information
 *
 * @author Christina Lidwin (clidwin)
 * @version April 27, 2015
 */
public class GpsLocationService extends Service implements LocationListener {
    private static final String TAG = "gps-location-service";

    DatabaseAdapter dbAdapter;
    GeospatialPin mostRecentPin;
    LocationManager locationManager;

    public GpsLocationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        subscribeToLocationUpdates();
        Log.d(TAG, "GpsLocationService started.");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "GpsLocationService destroyed.");
        super.onDestroy();
    }

    /**
     * Helper method to begin pinging for location updates.
     * Referenced from:
     * http://androidgps.blogspot.com/2008/09/simple-android-tracklogging-service.html
     */
    private void subscribeToLocationUpdates() {
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Constants.UPDATE_INTERVAL,
                Constants.UPDATE_DISTANCE,
                this // LocationListener
        );
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, location.toString());

        // Create database connection if it does not already exist.
        if (dbAdapter == null) {
            dbAdapter = new DatabaseAdapter(this);
            dbAdapter.open();
        }

        // Add location item to the database.
        GeospatialPin newPin = new GeospatialPin(location);
        if (mostRecentPin == null) {
            mostRecentPin = dbAdapter.getMostRecentEntry();
        }
        if (mostRecentPin != null) {
            Location newLocation = newPin.getLocation();
            Location recentLocation = mostRecentPin.getLocation();

            // Compare latitudes and longitudes to see if they're approximately the same location
            // From //stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
            if (distanceBetweenLocations(newLocation, recentLocation) > newLocation.getAccuracy()) {
                // The locations are geographically similar
                Log.d(TAG, "Locations are similar");

                long duration = (new Date()).getTime() - mostRecentPin.getArrivalTime().getTime();
                mostRecentPin.setDuration(duration);
                dbAdapter.updateEntry(mostRecentPin);

                sendBroadcast(Constants.BROADCAST_UPDATED_LOCATION);
                return;
            }

            // Even if the location doesn't match, we want to update the duration
            // of the last known location.
            long duration = (new Date()).getTime() - mostRecentPin.getArrivalTime().getTime();
            mostRecentPin.setDuration(duration);
            dbAdapter.updateEntry(mostRecentPin);
        } else {
            Log.d(TAG, "No recent pin found");
        }

        dbAdapter.addNewEntry(newPin);
        mostRecentPin = newPin;

        //Broadcast a change was made
        Log.d(TAG, "Location recorded");
        sendBroadcast(Constants.BROADCAST_NEW_LOCATION);
    }

    /**
     * Calculates the distance in meters between two geo coordinates.
     * //stackoverflow.com/questions/4102520/how-to-transform-a-distance-from-degrees-to-metres
     *
     * @param fromLocation The first location of reference
     * @param toLocation   The second location of reference
     * @return the distance between the locations (in meters)
     */
    private double distanceBetweenLocations(Location fromLocation, Location toLocation) {
        double lat1 = roundValue(fromLocation.getLatitude());
        double lat2 = roundValue(toLocation.getLatitude());
        double long1 = roundValue(fromLocation.getLongitude());
        double long2 = roundValue(toLocation.getLongitude());


        int earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(long2 - long1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * Streams a message to anyone listening to this service.
     *
     * @param message The extra information to go in the broadcast.
     */
    private void sendBroadcast(String message) {
        Intent gpsIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.BROADCAST_UPDATE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(gpsIntent);
    }

    /**
     * @param value The number to round.
     * @return The value rounded to eight decimal places of precision.
     */
    private double roundValue(double value) {
        int precision = 100000000; // Eight decimal places
        return (double) Math.round(value * precision) / precision;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
