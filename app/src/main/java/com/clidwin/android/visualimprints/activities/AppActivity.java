package com.clidwin.android.visualimprints.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.clidwin.android.visualimprints.VisualImprintsApplication;
import com.clidwin.android.visualimprints.services.GpsLocationService;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;

/**
 * Parent activity class for all activities in the Visual Imprints application.
 *
 * @author Christina Lidwin
 * @version June 30, 2015
 */
public abstract class AppActivity extends AppCompatActivity {

    protected DatabaseAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectToDatabase();
    }

    /**
     * Piggy-backs off the application's established connection with the database and loads
     * its contents into the activity UI.
     */
    private void connectToDatabase() {
        VisualImprintsApplication vI = (VisualImprintsApplication) this.getApplication();
        dbAdapter = vI.getDatabaseAdapter();
    }

    /**
     * @return the {@link DatabaseAdapter} being used by this activity
     */
    public DatabaseAdapter getDatabaseAdapter() { return dbAdapter; }

    private boolean isConnected() {
        String gpsServiceName = GpsLocationService.class.getName();

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (gpsServiceName.equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}