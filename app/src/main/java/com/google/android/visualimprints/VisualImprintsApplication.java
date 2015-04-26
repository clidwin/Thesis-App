package com.google.android.visualimprints;

import android.app.Application;
import android.content.Intent;

import com.google.android.visualimprints.services.GpsLocationService;
import com.google.android.visualimprints.storage.DatabaseAdapter;

/**
 * Controlling class for the entire application.
 *
 * @author Christina Lidwin (clidwin)
 * @version April 26, 2015
 */
public class VisualImprintsApplication extends Application {
    private DatabaseAdapter dbAdapter;


    @Override
    public void onCreate() {
        dbAdapter = new DatabaseAdapter(getApplicationContext());
        dbAdapter.open();

        // Start Service
        //TODO(clidwin): Start the service on device startup rather than application startup
        Intent gpsIntent = new Intent(getApplicationContext(), GpsLocationService.class);
        startService(gpsIntent);
    }

    /**
     * Retrieves the opened database so that only one database connection is open
     * in the application.
     *
     * @return {@link com.google.android.visualimprints.storage.DatabaseAdapter}
     */
    public DatabaseAdapter getDatabaseAdapter() {
        return dbAdapter;
    }
}
