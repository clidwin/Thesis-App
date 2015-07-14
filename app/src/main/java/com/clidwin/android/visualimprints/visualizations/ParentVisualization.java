package com.clidwin.android.visualimprints.visualizations;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;

import java.util.ArrayList;

/**
 * Blueprint class for any visualization.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 09, 2015
 */
public abstract class ParentVisualization extends View {
    private static final String TAG = "vi-parent-vis";

    protected ArrayList<GeospatialPin> lastLocations;
    private VisualizationsActivity activity;

    public ParentVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        TypedArray array = context.getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.Visualization,
                0, 0);

        try {
            //mShowText = a.getBoolean(R.styleable.TestVisualization_showText2, false);
        } finally {
            array.recycle();
        }

        activity = (VisualizationsActivity) getContext();
        if (activity != null) {
            DatabaseAdapter dbAdapter = activity.getDatabaseAdapter();
            lastLocations = dbAdapter.getEntriesInDateRange(
                    activity.getOldestTimestamp(), activity.getNewestTimestamp());
            Log.e(TAG, "Number of locations: " + lastLocations.size());
            for(GeospatialPin pin: lastLocations) {
                //TODO(clidwin): Switch this to a debug log
                Log.e(TAG, pin.getArrivalTime().toString());
            }
        } else {
            Log.e(TAG, "Database disconnected");
        }
    }
}
