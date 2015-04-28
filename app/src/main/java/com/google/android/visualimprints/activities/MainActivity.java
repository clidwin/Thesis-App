/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.visualimprints.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.visualimprints.Constants;
import com.google.android.visualimprints.GeospatialPin;
import com.google.android.visualimprints.VisualImprintsApplication;
import com.google.android.visualimprints.storage.DatabaseAdapter;

import java.text.DateFormat;

/**
 * Getting Location Updates.
 * <p/>
 * Based on the samples from: https://github.com/googlesamples/android-play-location/
 *
 * @author Christina Lidwin (clidwin)
 * @version April 27, 2015
 */
public class MainActivity extends Activity {
    protected static final String TAG = "visual-imprints-main";
    // Keys for storing activity state in the Bundle.
    protected final static String LOCATION_KEY = "location-key";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    //TODO(clidwin): Add an ActionBar for settings and refresh buttons

    /**
     * Represents a current location-based point of interest.
     */
    protected GeospatialPin mCurrentPin;

    // UI Widgets.
    protected TableLayout mAllLocationsTable;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mLocationAddressTextView;
    private DatabaseAdapter dbAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Locate the UI widgets.
        mAllLocationsTable = (TableLayout) findViewById(R.id.all_locations_table_layout);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mLocationAddressTextView = (TextView) findViewById(R.id.location_address_text);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
        connectToDatabase();
        openReceiver();
    }

    private void openReceiver() {
        IntentFilter mGpsLocationFilter = new IntentFilter(Constants.BROADCAST_ACTION);

        GpsLocationReceiver mGpsLocationReceiver = new GpsLocationReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mGpsLocationReceiver,
                mGpsLocationFilter);
    }

    /**
     * Piggy-backs off the application's established connection with the database and loads
     * its contents into the activity UI.
     */
    private void connectToDatabase() {
        VisualImprintsApplication vI = (VisualImprintsApplication) this.getApplication();
        dbAdapter = vI.getDatabaseAdapter();

        loadDatabaseContents();
    }

    /**
     * Adds the contents of the database to the Location History Table.
     */
    private void loadDatabaseContents() {
        int i = 1;
        for (GeospatialPin pin : dbAdapter.getAllEntries()) {
            addLocationToHistoryTable(pin, i);
            i++;
        }
    }

    /**
     * Adds the list of {@link com.google.android.visualimprints.GeospatialPin} to the
     * bottom of the history table.
     *
     * @param pin   The Geospatial Pin to be added to the table.
     * @param index the location to add the location to in the table
     */
    private void addLocationToHistoryTable(GeospatialPin pin, int index) {
        TableRow row = new TableRow(this);

        TextView timeText = new TextView(this);
        timeText.setText(DateFormat.getInstance().format(pin.getArrivalTime()));
        row.addView(timeText);

        TextView latText = new TextView(this);
        latText.setText(
                "(" + pin.getLocation().getLatitude() +
                        ", " + pin.getLocation().getLongitude() + ")"
        );
        row.addView(latText);

        TextView durationText = new TextView(this);
        durationText.setText("" + pin.getDuration() + "ms");
        row.addView(durationText);

        /*TextView longText = new TextView(this);
        longText.setText("" + pin.getLocation().getLongitude());
        row.addView(longText);*/

        if (pin.getAddress() != null) {

        }

        mAllLocationsTable.addView(row, index);
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                Location currentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                mCurrentPin = new GeospatialPin(currentLocation);
            }

            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                String address = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
                //TODO(clidwin): Handle showing this in the UI
            }

            updateUI();
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        if (mCurrentPin == null) {
            mCurrentPin = dbAdapter.getMostRecentEntry();
        }
        if (mCurrentPin != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentPin.getLocation().getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentPin.getLocation().getLongitude()));
            mLastUpdateTimeTextView.setText(
                    DateFormat.getTimeInstance().format(mCurrentPin.getArrivalTime()));
            mLocationAddressTextView.setText("" + mCurrentPin.getDuration() + "ms");
        }
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private class GpsLocationReceiver extends BroadcastReceiver {

        private GpsLocationReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
           showToast("Location received");

           String b = intent.getStringExtra(Constants.BROADCAST_UPDATE);
           if (b != null) {
               switch (b) {
                   case Constants.BROADCAST_NEW_LOCATION:
                       if (mCurrentPin != null) {
                           addLocationToHistoryTable(mCurrentPin, 1);
                       }
                       mCurrentPin = dbAdapter.getMostRecentEntry();
                       updateUI();
                       break;
                   case Constants.BROADCAST_UPDATED_LOCATION:
                       mCurrentPin = dbAdapter.getMostRecentEntry();
                       updateUI();
                       break;
                   default:
                       break;
               }
           }
        }
    }
}
