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

package com.clidwin.android.visualimprints.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.clidwin.android.visualimprints.Constants;
import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.MainActivity;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Fragment showing the raw data.
 *
 * @author Christina Lidwin (clidwin)
 * @version May 14, 2015
 */
public class RawDataFragment extends Fragment implements OnClickListener {
    protected static final String TAG = "visual-imprints-main";
    // Keys for storing activity state in the Bundle.
    protected final static String LOCATION_KEY = "location-key";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    /**
     * Represents a current location-based point of interest.
     */
    protected GeospatialPin mCurrentPin;

    // UI Widgets.
    protected LinearLayout mAllLocationsListLayout;
    protected TextView mLastArrivalTimeTextView;
    protected TextView mLastUpdatedTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mLastLocationTextView;
    protected Chronometer mDurationCounter;
    private DatabaseAdapter dbAdapter;

    private ArrayList<TableRow> selectedRows;
    private boolean rowsClickable = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "Loading database contents");
        View view = inflater.inflate(R.layout.fragment_raw_data, container, false);

        // Locate the UI widgets.
        linkUiElements(view);
        selectedRows = new ArrayList<>();

        // Update values using data stored in the Bundle.
        //updateValuesFromBundle(savedInstanceState);

        MainActivity activity = (MainActivity)getActivity();
        if (activity != null && isAdded()) {
            dbAdapter = activity
                    .getDatabaseAdapter();
            openReceiver();
            loadDatabaseContents();
        }

        return view;
    }

    private void linkUiElements(View view) {
        mAllLocationsListLayout =
                (LinearLayout) view.findViewById(R.id.all_locations_linear_layout);
        mLastLocationTextView = (TextView) view.findViewById(R.id.last_location_label);
        mLastUpdatedTextView = (TextView) view.findViewById(R.id.last_updated_label);
        mLatitudeTextView = (TextView) view.findViewById(R.id.coordinates_latitude_text);
        mLongitudeTextView = (TextView) view.findViewById(R.id.coordinates_longitude_text);
        mLastArrivalTimeTextView = (TextView) view.findViewById(R.id.last_arrival_time_text);
        mDurationCounter = (Chronometer) view.findViewById(R.id.duration_counter);
    }

    private void openReceiver() {
        IntentFilter mGpsLocationFilter = new IntentFilter(Constants.BROADCAST_ACTION);

        GpsLocationReceiver mGpsLocationReceiver = new GpsLocationReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mGpsLocationReceiver,
                mGpsLocationFilter);
    }

    /**
     * Adds the contents of the database to the Location History Table.
     */
    private void loadDatabaseContents() {
        // Date formats used in the application.
        final DateFormat dbDateFormat = new SimpleDateFormat(Constants.DATABASE_DATE_FORMAT);
        final DateFormat displayDateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");

        // Profess all entry dates.
        ArrayList<String> allEntryDates = dbAdapter.getAllEntryDates();
        Log.d(TAG, "Database size: " + allEntryDates.size());
        for (int index = 0; index < allEntryDates.size(); index++) {

            //TODO(clidwin): if date matches today, get all entries for date and if one entry, don't show since it's the current location
            // Construct row object.
            String dateText = allEntryDates.get(index);
            final LinearLayout row = new LinearLayout(getActivity());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(
                    0, (int) getResources().getDimension(R.dimen.layout_padding),
                    0, (int) getResources().getDimension(R.dimen.layout_padding)
            );
            row.setId(("dateRow-" + dateText).hashCode());

            // Parse date from database.
            Date date;
            try {
                date = dbDateFormat.parse(dateText);
            } catch (ParseException e) {
                date = new Date();
                e.printStackTrace();
            }

            // Format date for display.
            final TextView dateTextView = new TextView(getActivity());
            dateTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.default_text_size)
            );
            dateTextView.setText(displayDateFormat.format(date));
            row.addView(dateTextView);

            // Establish interactivity.
            row.setClickable(true);
            row.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retrieve the layout object that has been clicked.
                    int childrenCount = ((LinearLayout)v).getChildCount();
                    if (childrenCount >= 2) {
                        View locationsTable = ((LinearLayout) v).getChildAt(1);
                        ((LinearLayout) v).removeView(locationsTable);
                        return;
                    }

                    showToast("Loading data...");
                    String dateText = ((TextView) row.getChildAt(0)).getText().toString();
                    Date date;
                    try {
                        date = displayDateFormat.parse(dateText);

                        TableLayout locationsTable = new TableLayout(v.getContext());
                        TableLayout.LayoutParams lp = new TableLayout.LayoutParams();
                        lp.width = TableLayout.LayoutParams.MATCH_PARENT;
                        locationsTable.setStretchAllColumns(true);
                        locationsTable.setLayoutParams(lp);
                        locationsTable.setPadding(
                                0, (int) getResources().getDimension(R.dimen.layout_padding), 0, 0
                        );
                        locationsTable.addView(makeLocationHeader(), 0);
                        ArrayList<GeospatialPin> datePins = dbAdapter.getAllEntriesFromDates(
                                new String [] {dbDateFormat.format(date)});
                        for (int i = datePins.size() - 1; i > 0; i--) {
                            GeospatialPin pin = datePins.get(i);
                            addLocationToHistoryTable(locationsTable, pin, 1);
                        }
                        ((LinearLayout) v).addView(locationsTable);


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            });

            mAllLocationsListLayout.addView(row);

            // Add horizontal rule to separate list entries.
            if (index+1 != allEntryDates.size()) {
                View ruler = new View(getActivity());
                ruler.setBackgroundColor(getResources().getColor(R.color.Divider));
                ruler.setPadding(0, 0, 0, (int)getResources().getDimension(R.dimen.layout_padding));
                mAllLocationsListLayout.addView(ruler,
                        new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 2));
            }
        }

        updateMostRecentLocation();
    }

    /**
     * Constructs a header for a table with location information.
     *
     * @return the constructed {@link android.widget.TableRow} object.
     */
    private TableRow makeLocationHeader() {
        TableRow tableRow = new TableRow(getActivity());
        //TODO(clidwin): Set minimum width and height

        TableRow.LayoutParams lp = new TableRow.LayoutParams();
        lp.height = TableLayout.LayoutParams.WRAP_CONTENT;
        lp.width = 0;
        lp.weight = 1;

        // Arrival Date and Time
        TextView arrivalTimeView = new TextView(getActivity());
        arrivalTimeView.setLayoutParams(lp);
        arrivalTimeView.setText(getResources().getText(R.string.arrival_time_label));
        arrivalTimeView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.default_text_size));
        arrivalTimeView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        arrivalTimeView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(arrivalTimeView);

        // Location
        TextView locationLabelView = new TextView(getActivity());
        locationLabelView.setLayoutParams(lp);
        locationLabelView.setText(getResources().getText(R.string.location_label));
        locationLabelView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.default_text_size));
        locationLabelView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        locationLabelView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(locationLabelView);

        // Duration
        TextView durationLabelView = new TextView(getActivity());
        durationLabelView.setLayoutParams(lp);
        durationLabelView.setText(getResources().getText(R.string.duration_label));
        durationLabelView.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.default_text_size));
        durationLabelView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        durationLabelView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(durationLabelView);

        return tableRow;
    }

    /**
     * Adds the list of {@link com.clidwin.android.visualimprints.location.GeospatialPin} to the
     * bottom of the history table.
     *
     * @param pin   The Geospatial Pin to be added to the table.
     * @param index the location to add the location to in the table
     */
    private void addLocationToHistoryTable(TableLayout tableLayout, GeospatialPin pin, int index) {
        TableRow.LayoutParams cellLayoutParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f);

        TableRow row = new TableRow(getActivity());
        row.setId(pin.getArrivalTime().hashCode());
        row.setClickable(rowsClickable);
        row.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1.0f));

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        TextView timeText = new TextView(getActivity());
        timeText.setText(timeFormat.format(pin.getArrivalTime()));
        row.addView(timeText, cellLayoutParams);

        LinearLayout gpsLayout = new LinearLayout(getActivity());
        gpsLayout.setOrientation(LinearLayout.VERTICAL);
        TextView latText = new TextView(getActivity());
        latText.setText("" + pin.getLocation().getLatitude());
        gpsLayout.addView(latText);
        TextView longText = new TextView(getActivity());
        longText.setText("" + pin.getLocation().getLongitude());
        gpsLayout.addView(longText);
        row.addView(gpsLayout, cellLayoutParams);

        TextView durationText = new TextView(getActivity());
        durationText.setText(formatDuration(pin.getDuration()));
        row.addView(durationText, cellLayoutParams);

        tableLayout.addView(row, index);
    }

    /**
     * Formats duration value in hours, minutes, and seconds.
     *
     * Code reference:
     *      //stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
     * @param duration the amount of time to be formatted
     * @return the formatted time
     */
    private String formatDuration(long duration) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        //TODO(clidwin): Utilize this method to tell when the location was last updated.
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

            updateMostRecentLocation();
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateMostRecentLocation() {
        if (mCurrentPin == null) {
            mCurrentPin = dbAdapter.getMostRecentEntry();
        }
        if (mCurrentPin != null) {
            // Show location information
            mLatitudeTextView.setText(String.valueOf(mCurrentPin.getLocation().getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentPin.getLocation().getLongitude()));

            // Format date and time information
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss zz");
            mLastArrivalTimeTextView.setText(
                    dateFormat.format(mCurrentPin.getArrivalTime()) + " at " +
                            timeFormat.format(mCurrentPin.getArrivalTime()));

            // Show a duration counter (based on: http://stackoverflow.com/questions/10862845/)
            long elapsedRealtimeOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime();
            mDurationCounter.setBase(mCurrentPin.getArrivalTime().getTime() - elapsedRealtimeOffset);
            mDurationCounter.start();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.edit_rows:
                rowsClickable = !rowsClickable;
                toggleClickableRows();
                return true;
            case R.id.action_settings:
                showToast("Merging selected rows.");
                mergeLocations();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void mergeLocations() {
        // Confirm only adjacent rows are selected
        // (Can't merge locations that are not temporally sequential)
        String [] query = new String [selectedRows.size()];
        int i = 0;
        for (View view: selectedRows) {
            /*int tableIndex = mAllLocationsTable.indexOfChild(view);
            View vM1 = mAllLocationsTable.getChildAt(tableIndex - 1);

            if (!selectedRows.contains(vM1)) {
                if (tableIndex + 1 < mAllLocationsTable.getChildCount()) {
                    View vM2 = mAllLocationsTable.getChildAt(tableIndex + 1);
                    if (!selectedRows.contains(vM2)) {
                        //TODO(clidwin): Debug this, since some valid merges get here.
                        Log.d(TAG, "Adjacent row not found");
                        showToast("Merge not successful. Select adjacent rows.");
                        return;
                    }
                }
            }*/
            query[i] = String.valueOf(view.getId());
            i++;
        }

        // Get the database objects for the selected rows
        ArrayList<GeospatialPin> pinsToMerge = new ArrayList<>();
        int indexOfEarliestPin = -1;
        i=0;
        for (View view: selectedRows) {
            //TODO(clidwin): Make one call to the database with all IDs using the query array (above)
            GeospatialPin pin = dbAdapter.getEntryById(view.getId());
            if (pin != null) {
                pinsToMerge.add(pin);
                if (indexOfEarliestPin == -1) {
                    indexOfEarliestPin = i;
                } else {
                    Date earliestPinTime = pinsToMerge.get(indexOfEarliestPin).getArrivalTime();
                    Date newPinTime = pin.getArrivalTime();
                    if (newPinTime.before(earliestPinTime)) {
                        indexOfEarliestPin = i;
                    }
                    //TODO(clidwin): Maybe delete entities here as they're "not earliest"
                }
                i++;
            } else {
                showToast("Merge not successful. Row not found.");
                return;
            }
        }

        GeospatialPin earliestPin = pinsToMerge.remove(indexOfEarliestPin);
        for (GeospatialPin pin: pinsToMerge) {
            earliestPin.setDuration(earliestPin.getDuration() + pin.getDuration());
            //TODO(clidwin): delete all entries with one database call
            dbAdapter.deleteEntry(pin);
            dbAdapter.updateEntry(earliestPin);
            for (View row: selectedRows) {
                if (row.getId() == pin.getArrivalTime().hashCode()) {
                    //mAllLocationsTable.removeView(row);
                    break;
                }
            }
        }
        TableRow selectedRow = selectedRows.get(0);
        selectedRow.removeViewAt(2);
        TextView durationText = new TextView(getActivity());
        durationText.setText(formatDuration(earliestPin.getDuration()));
        selectedRow.addView(durationText);
        showToast("Pins merged.");

    }

    private void toggleClickableRows() {
        //TODO(clidwin): Fix bug causing rows to stay clickable despite toggle
        /*for(int i = 1, j = mAllLocationsTable.getChildCount(); i < j; i++) {
            View view = mAllLocationsTable.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                row.setClickable(rowsClickable);
                row.setOnClickListener(getActivity());
            }
        }

        if (rowsClickable) {
            showToast("Table Rows are now editable.");
        } else {
            showToast("Table Rows are no longer editable.");
        }*/
    }

    @Override
    public void onClick(View v) {
        if (selectedRows.contains(v)) {
            selectedRows.remove(v);
            v.setBackgroundColor(Color.TRANSPARENT);
        } else {
            selectedRows.add((TableRow)v);
            v.setBackgroundColor(Color.CYAN);
        }
    }

    /**
     * Shows a toast with the given text.
     */
    protected void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles location broadcasts for the main activity.
     */
    private class GpsLocationReceiver extends BroadcastReceiver {

        private GpsLocationReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
           String broadcastUpdate = intent.getStringExtra(Constants.BROADCAST_UPDATE);
           if (broadcastUpdate != null) {
               switch (broadcastUpdate) {
                   case Constants.BROADCAST_SERVICE_START:
                       processLocationInfo();
                       break;
                   case Constants.BROADCAST_NEW_LOCATION:
                       if (mCurrentPin != null) {
                           //addLocationToHistoryTable(mCurrentPin, 1);
                       }
                       mCurrentPin = dbAdapter.getMostRecentEntry();
                       processLocationInfo();
                       break;
                   case Constants.BROADCAST_UPDATED_LOCATION:
                       mCurrentPin.setDuration(mDurationCounter.getBase());
                       processLocationInfo();
                       break;
                   default:
                       break;
               }
           }
        }

        /**
         * Updates UI based on new location information.
         */
        private void processLocationInfo() {
            // Update the last time the app looked for a location
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss ZZZZ");
            mLastUpdatedTextView.setText(getResources().getString(R.string.last_updated_label) +
                    " " + timeFormat.format(System.currentTimeMillis()) + " on " +
                    dateFormat.format(System.currentTimeMillis()));

            updateMostRecentLocation();
        }
    }
}
