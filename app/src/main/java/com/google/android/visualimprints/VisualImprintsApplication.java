package com.google.android.visualimprints;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.google.android.visualimprints.services.GpsLocationService;
import com.google.android.visualimprints.storage.DatabaseAdapter;

/**
 * Controlling class for the entire application.
 *
 * @author Christina Lidwin (clidwin)
 * @version April 27, 2015
 */
public class VisualImprintsApplication extends Application {
    private DatabaseAdapter dbAdapter;


    @Override
    public void onCreate() {
        dbAdapter = new DatabaseAdapter(getApplicationContext());
        dbAdapter.open();

        if (!gpsServiceIsRunning()) {
            Intent gpsIntent = new Intent(getApplicationContext(), GpsLocationService.class);
            startService(gpsIntent);
        }
    }

    /**
     * Retrieves the opened database so that only one database connection is open
     * in the application.
     * <p/>
     * Code inspired by:
     * //stackoverflow.com/questions/6356170/how-should-i-open-and-close-my-database-properly
     *
     * @return {@link com.google.android.visualimprints.storage.DatabaseAdapter}
     */
    public DatabaseAdapter getDatabaseAdapter() {
        return dbAdapter;
    }

    /**
     * Checks whether the {@link com.google.android.visualimprints.services.GpsLocationService}
     * is running.
     * <p/>
     * Code reference:
     * //stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
     *
     * @return true if the service is running, else false.
     */
    private boolean gpsServiceIsRunning() {
        String gpsServiceName = GpsLocationService.class.getName();

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (gpsServiceName.equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
