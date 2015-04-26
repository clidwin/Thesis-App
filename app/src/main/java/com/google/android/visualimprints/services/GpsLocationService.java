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

/**
 * Retrieves location information
 *
 * @author Christina Lidwin (clidwin)
 * @version April 26, 2015
 */
public class GpsLocationService extends Service implements LocationListener {
    private static final String TAG = "gps-location-service";

    DatabaseAdapter dbAdapter;
    LocationManager locationManager;

    public GpsLocationService() {
        super();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "GpsLocationService started.");
        super.onCreate();
        subscribeToLocationUpdates();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "GpsLocationService destroyed.");
        super.onDestroy();
    }

    /**
     * Helper method.
     * Referenced from:
     *      http://androidgps.blogspot.com/2008/09/simple-android-tracklogging-service.html
     */
    private void subscribeToLocationUpdates() {
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
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
            dbAdapter = new DatabaseAdapter(getApplicationContext());
            dbAdapter.open();
        }

        // Add location item to the database.
        GeospatialPin pin = new GeospatialPin(location);
        //TODO(clidwin): Only add a new entry if the most recent entry is a different location.
        //TODO(clidwin): Update duration at location through the service.
        dbAdapter.addNewEntry(pin);

        //Broadcast a change was made
        Log.d(TAG, "pin added successfully");
        sendBroadcast(Constants.BROADCAST_NEW_LOCATION);
    }

    private void sendBroadcast(String message) {
        Intent gpsIntent = new Intent(Constants.BROADCAST_ACTION)
                .putExtra(Constants.BROADCAST_UPDATE, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(gpsIntent);
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
