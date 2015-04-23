package com.google.android.visualimprints;

import android.app.Application;

import com.google.android.visualimprints.storage.DatabaseAdapter;

/**
 * Controlling class for the entire application.
 *
 * @author Christina Lidwin (clidwin)
 * @version April 23, 2015
 */
public class VisualImprintsApplication extends Application {
    private DatabaseAdapter dbAdapter;


    @Override
    public void onCreate() {
        dbAdapter = new DatabaseAdapter(getApplicationContext());
        dbAdapter.open();
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
