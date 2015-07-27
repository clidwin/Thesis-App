package com.clidwin.android.visualimprints.visualizations;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;

import java.util.ArrayList;

/**
 * Blueprint class for any visualization.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 15, 2015
 */
public abstract class ParentVisualization extends View {
    private static final String TAG = "vi-parent-vis";

    protected ArrayList<GeospatialPin> visualizationLocations;

    public ParentVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        /*TypedArray array = context.getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.Visualization,
                0, 0);

        try {
            //mShowText = array.getBoolean(R.styleable.TestVisualization_showText2, false);
        } finally {
            array.recycle();
        }*/
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshLocations();
    }

    protected abstract void processPin(GeospatialPin pin);

    /**
     * Overrides the fragment's onMeasure to fill the screen.
     *      Code based on: http://stackoverflow.com/questions/8577117/
     *
     * @param widthMeasureSpec the specified width to be used in measurements
     * @param heightMeasureSpec the specified height to be used in measurements
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "Width spec: " + MeasureSpec.toString(widthMeasureSpec));
        Log.d(TAG, "Height spec: " + MeasureSpec.toString(heightMeasureSpec));

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseDimension(widthMode, widthSize);
        int chosenHeight = chooseDimension(heightMode, heightSize);

        //int chosenDimension = Math.min(chosenWidth, chosenHeight);
        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    /**
     * Selects whether to use a default or app-generated size.
     *
     * @param mode the mode that determines what size to use.
     * @param size a possible indicator of what size should be used (if not unspecified).
     *
     * @return the size variable if theh MeasureSpec is specified, else it gets the preferred size.
     */
    private int chooseDimension(int mode, int size) {
        int preferredViewSize = 400;
        return mode == MeasureSpec.UNSPECIFIED ? preferredViewSize : size;
    }

    /**
     * Retrieves locations from the database matching the parameter timestamps from a
     *      VisualizationActivity.
     */
    public void refreshLocations() {
        VisualizationsActivity activity = (VisualizationsActivity) getContext();
        if (activity != null) {
            DatabaseAdapter dbAdapter = activity.getDatabaseAdapter();
            visualizationLocations = dbAdapter.getEntriesInDateRange(
                    activity.getOldestTimestamp(), activity.getNewestTimestamp());
            //TODO(clidwin): let children know what time range & timestamps are being covered here.
            Log.e(TAG, "Number of locations: " + visualizationLocations.size());
            for(GeospatialPin pin: visualizationLocations) {
                processPin(pin);
                Log.d(TAG, pin.getArrivalTime().toString());
            }
            //TODO(clidwin): Error checking on subclasses for no data
        } else {
            Log.e(TAG, "Database disconnected");
        }
    }
}
