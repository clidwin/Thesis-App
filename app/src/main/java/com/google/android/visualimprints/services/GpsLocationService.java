package com.google.android.visualimprints.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.visualimprints.VisualImprintsApplication;
import com.google.android.visualimprints.storage.DatabaseAdapter;

/**
 * Retrieves location information
 *
 * @author Christina Lidwin (clidwin)
 * @version April 23, 2015
 */
public class GpsLocationService extends Service implements LocationListener {
    protected static final String TAG = "gps-location-service";
    private DatabaseAdapter dbAdapter;

    LocationManager locationManager;

    public GpsLocationService() {
        super();

        // Establish database connection
        VisualImprintsApplication vI = (VisualImprintsApplication) this.getApplication();
        dbAdapter = vI.getDatabaseAdapter();
    }

    @Override
    public void onLocationChanged(Location location) {

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
