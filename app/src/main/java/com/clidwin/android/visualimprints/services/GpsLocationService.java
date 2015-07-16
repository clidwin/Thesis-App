package com.clidwin.android.visualimprints.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.clidwin.android.visualimprints.Constants;
import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

/**
 * Retrieves location information
 *
 * @author Christina Lidwin (clidwin)
 * @version July 15, 2015
 */
public class GpsLocationService extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "gps-location-service";

    private static final String SAVED_STATE_LAST_UPDATED_TIME = "lastUpdatedTime";

    DatabaseAdapter dbAdapter;
    GeospatialPin mostRecentPin;
    SensorManager mSensorManager;
    Sensor mAccelerometer;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    ViLocationListener mLocationListener;
    private long lastUpdatedTime;

    public GpsLocationService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Connect to Google Play Services.
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }

            if (!mGoogleApiClient.isConnected()) {
                createNotification("Location service is offline.");
                //ConnectionResult result = mGoogleApiClient.ge
                Log.e(TAG, "GPS Location is not connected.");
            }
        } else {
            Log.e(TAG, "Unable to connect to Google Play Services.");
        }
        mLocationListener = new ViLocationListener();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //subscribeToLocationUpdates();
        createNotification("Location service is running.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int toReturn = super.onStartCommand(intent, flags, startId);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lastUpdatedTime = preferences.getLong(SAVED_STATE_LAST_UPDATED_TIME, -1);

        // Create database connection if it does not already exist.
        if (dbAdapter == null) {
            dbAdapter = new DatabaseAdapter(this);
            dbAdapter.open();
        }
        mostRecentPin = dbAdapter.getMostRecentEntry();

        return toReturn;
    }

    /**
     * Creates a notification, which makes the service a foreground process
     * and less likely to be killed by another background task.
     */
    private void createNotification(String contentText) {
        Intent notificationIntent = new Intent(getApplicationContext(), VisualizationsActivity.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        startForeground(81015, notification);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "GpsLocationService destroyed.");
        super.onDestroy();
    }

    /**
     * Calculates the distance in meters between two geo coordinates.
     * http://www.movable-type.co.uk/scripts/latlong.html
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

        Log.d(TAG, "Finding distance between (" + lat1 + ", " + long1 +
                ") and (" + lat2 + ", " + long2 + ")");

        int earthRadius = 6371 * 1000; // m
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
     * //stackoverflow.com/questions/153724/how-to-round-a-number-to-n-decimal-places-in-java
     *
     * @param value The number to round.
     * @return The value rounded to eight decimal places of precision.
     */
    private double roundValue(double value, int accuracy) {
        double precision = Math.pow(10, accuracy); // Eight decimal places
        return Math.round(value * precision) / precision;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        //TODO(clidwin): Create a setting allowing people to change these
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(Constants.UPDATE_INTERVAL)
                .setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL)
                .setSmallestDisplacement(Constants.UPDATE_DISTANCE);

        // Request last location to get an immediate update, then request continuous updates.
        //TODO(clidwin): Update the duration of the last location using the last recorded time.
        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mLocationListener);

        Log.d(TAG, getClass().getSimpleName() + " started.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO(clidwin): Indicate when connection is suspended.

        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putLong(SAVED_STATE_LAST_UPDATED_TIME, lastUpdatedTime);
        editor.commit();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO(clidwin): Indicate in the UI when this happens.
    }

    /**
     * Determines if two locations are the "same".
     *
     * @param one      the first location to test.
     * @param two      the second location to test
     * @param accuracy the level of accuracy (decimal places) to test.
     * @return true if the locations are the same, false otherwise.
     */
    public boolean areSameLocation(Location one, Location two, int accuracy) {
        double lat1 = one.getLatitude();
        double lat2 = two.getLatitude();
        double long1 = one.getLongitude();
        double long2 = two.getLongitude();

        Log.d(TAG, "Checking sameness between (" + roundValue(lat1, accuracy) + ", " +
                roundValue(long1, accuracy) + ") and (" + roundValue(lat2, accuracy) + ", " +
                roundValue(long2, accuracy) + ")");

        return roundValue(lat1, accuracy) == roundValue(lat2, accuracy) &&
                roundValue(long1, accuracy) == roundValue(long2, accuracy);
    }

    /**
     * Handles documenting a new location when the service finds one.
     */
    public class ViLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, location.toString());

            // Create pin from location and retrieve the most recent previously recorded pin.
            GeospatialPin newPin = new GeospatialPin(location);

            if (mostRecentPin != null) {
                location.setSpeed(calculateSpeed(mostRecentPin.getLocation(), location));
                /*if (location.getAccuracy() < 100 && location.getSpeed() < 6.45) {
                    //TODO(clidwin): Update the GPS last checked time.
                    Log.d(TAG, "High probability location hasn't changed.");

                    long duration = (new Date()).getTime() - mostRecentPin.getArrivalTime().getTime();
                    mostRecentPin.setDuration(duration);
                    dbAdapter.updateEntry(mostRecentPin);

                    return;
                } else if (location.getAccuracy() > 100) {
                    Log.d(TAG, "Location is inaccurate");
                } else if (location.getSpeed() > 6.45) {
                    //TODO(clidwin): Record this as a motion value.
                    Log.d(TAG, "Moving too fast to get accurate location");
                }*/

                Location newLocation = newPin.getLocation();
                Location recentLocation = mostRecentPin.getLocation();

                // Compare latitudes and longitudes to see if they're approximately the same location
                int accuracy = 5;
                if (areSameLocation(newLocation, recentLocation, accuracy) ||
                        distanceBetweenLocations(newLocation, recentLocation)
                                < Constants.UPDATE_DISTANCE) {
                    Log.d(TAG, "Locations are geographically similar");

                    long duration = (new Date()).getTime()
                            - mostRecentPin.getArrivalTime().getTime();
                    mostRecentPin.setDuration(duration);

                    sendBroadcast(Constants.BROADCAST_UPDATED_LOCATION);
                    return;
                }

                //TODO(clidwin): decrease database calls made by these actions.
                // Even if the location doesn't match, we want to update the duration
                // of the last known location.
                long duration = (new Date()).getTime() - mostRecentPin.getArrivalTime().getTime();
                mostRecentPin.setDuration(duration);
                dbAdapter.updateEntry(mostRecentPin);
            }

            dbAdapter.addNewEntry(newPin);
            mostRecentPin = newPin;
            lastUpdatedTime = new Date().getTime();

            //Broadcast a change was made
            Log.d(TAG, "New location recorded");
            sendBroadcast(Constants.BROADCAST_NEW_LOCATION);
        }
    }

    /**
     * Calculates the speed between two recorded locations
     *
     * @param lastRecordedLocation The location recorded before newLocation was found
     * @param newLocation The newest location information
     *
     * @return the speed in meters per second
     */
    private float calculateSpeed(Location lastRecordedLocation, Location newLocation) {
        double distance = distanceBetweenLocations(lastRecordedLocation, newLocation);
        Log.d(TAG, "Calculated distance is: " + distance);
        long timeInMilliseconds = newLocation.getTime() - lastRecordedLocation.getTime();
        Log.d(TAG, "Calculated speed is: " + (float)(distance/(timeInMilliseconds*1000)));
        return (float)(distance/(timeInMilliseconds*1000));
    }
}