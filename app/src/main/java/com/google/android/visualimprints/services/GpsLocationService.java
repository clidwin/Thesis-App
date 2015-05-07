package com.google.android.visualimprints.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.visualimprints.Constants;
import com.google.android.visualimprints.GeospatialPin;
import com.google.android.visualimprints.storage.DatabaseAdapter;

import java.util.Date;

/**
 * Retrieves location information
 *
 * @author Christina Lidwin (clidwin)
 * @version May 06, 2015
 */
public class GpsLocationService extends Service
        implements SensorEventListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "gps-location-service";
    private static final double MAX_REST_SPEED = 5;
    DatabaseAdapter dbAdapter;
    GeospatialPin mostRecentPin;
    LocationManager locationManager;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ViLocationListener mLocationListener;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private boolean deviceIsMoving = false;

    public GpsLocationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
        mLocationListener = new ViLocationListener();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //subscribeToLocationUpdates();
        Log.d(TAG, "GpsLocationService started.");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "GpsLocationService destroyed.");
        super.onDestroy();
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
        int locationAccuracy = 8; // Anything beyond this is noise in location data
        double lat1 = roundValue(fromLocation.getLatitude(), locationAccuracy);
        double lat2 = roundValue(toLocation.getLatitude(), locationAccuracy);
        double long1 = roundValue(fromLocation.getLongitude(), locationAccuracy);
        double long2 = roundValue(toLocation.getLongitude(), locationAccuracy);


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
     * Source:
     *      //stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
     * @param value The number to round.
     * @return The value rounded to eight decimal places of precision.
     */
    private double roundValue(double value, int accuracy) {
        int precision = 10 ^ accuracy; // Eight decimal places
        return (double) Math.round(value * precision) / precision;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //TODO(clidwin): Remove or update this code.
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long curTime = System.currentTimeMillis();

        if (locationManager == null) {
            this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > MAX_REST_SPEED && !deviceIsMoving) {
                Log.d(TAG, "device is in motion");
                //TODO(clidwin): Use whatever provider is available.
                //locationManager.requestSingleUpdate(
                //        LocationManager.GPS_PROVIDER, this, Looper.myLooper());
                deviceIsMoving = true;
            } else if (speed < MAX_REST_SPEED && deviceIsMoving) {
                Log.d(TAG, "device is at rest");
                //locationManager.requestSingleUpdate(
                //        LocationManager.GPS_PROVIDER, this, Looper.myLooper());
                deviceIsMoving = false;
            }

            last_x = x;
            last_y = y;
            last_z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        //TODO(clidwin): Create a setting allowing people to do this
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);

        // Create database connection if it does not already exist.
        if (dbAdapter == null) {
            dbAdapter = new DatabaseAdapter(this);
            dbAdapter.open();
        }

        Log.d(TAG, getClass().getSimpleName() + " started.");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class ViLocationListener implements LocationListener {
        //TODO(clidwin): Break this method into smaller pieces and use accelerometer data to help categorize events as motion or rest
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, location.toString());

            // Add location item to the database.
            GeospatialPin newPin = new GeospatialPin(location);
            if (mostRecentPin == null) {
                mostRecentPin = dbAdapter.getMostRecentEntry();
            }
            if (mostRecentPin != null) {
                Location newLocation = newPin.getLocation();
                Location recentLocation = mostRecentPin.getLocation();

                // Compare latitudes and longitudes to see if they're approximately the same location
                int accuracy = 5;
                if (distanceBetweenLocations(newLocation, recentLocation) > newLocation.getAccuracy() ||
                        (roundValue(newLocation.getLatitude(), accuracy) ==
                                roundValue(recentLocation.getLatitude(), accuracy)
                                && (roundValue(newLocation.getLongitude(), accuracy) ==
                                roundValue(recentLocation.getLongitude(), accuracy)))) {
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
    }
}
