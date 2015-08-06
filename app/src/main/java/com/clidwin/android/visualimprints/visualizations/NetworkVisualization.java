package com.clidwin.android.visualimprints.visualizations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.ui.Cluster;
import com.clidwin.android.visualimprints.ui.Slice;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/**
 * Visualization for locational data based on a tile/grid structure.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 15, 2015
 */
public class NetworkVisualization extends ParentVisualization {
    private static final String TAG = "vi-network-vis";

    private Paint mFillPaint;

    private PopupWindow popUp;

    private ArrayList<Cluster> allPoints;

    private RandomWithSeed randomNumberGenerator;

    //TODO(clidwin): Handle week and month view setups.
    public NetworkVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        initializePaints();
        popUp = new PopupWindow();

        allPoints = new ArrayList<>();

        Calendar time = ((VisualizationsActivity) context).getOldestTimestamp();
        randomNumberGenerator = new RandomWithSeed(time.getTimeInMillis());

    }

    @Override
    protected void processPin(GeospatialPin pin) {
        Slice newSlice = new Slice(pin);

        //TODO(clidwin): Improve clustering methodology to include more points.
        for (Cluster cluster: allPoints) {
            Slice slice = cluster.getFirstSlice();
            Location recordedLocation = slice.getPin().getLocation();
            Location newLocation = pin.getLocation();
            int threshhold = 30;
            if (distanceBetweenLocations(recordedLocation, newLocation) < threshhold) {
                cluster.addSlice(newSlice);
                return;
            }
        }

        Cluster newCluster = new Cluster();
        newCluster.addSlice(newSlice);
        allPoints.add(newCluster);
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

    /**
     * Create components used for drawing the visualization.
     */
    private void initializePaints() {
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(Color.LTGRAY);
    }

    //TODO(clidwin): Remove the @SuppressLint and fix warning related to allocation in setRect
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Paint the background.
        mFillPaint.setColor(getResources().getColor(R.color.grey_200));
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        float width = canvas.getWidth();
        float height = canvas.getHeight();

        //TODO(clidwin): Even out distribution
        for (Cluster c: allPoints) {
            mFillPaint.setColor(getRandomColor());
            float startX = (float)randomNumberGenerator.nextValue() * (width - 50) + 75;
            if (startX > canvas.getWidth()) {
                startX = canvas.getWidth() - startX;
            }
            float startY = (float)randomNumberGenerator.nextValue() * (height - 50) + 75;
            if (startY > canvas.getHeight()) {
                startY = canvas.getHeight() - startY;
            }

            for (Slice s: c.getSlices()) {

                // (x1+(x2-x1)*r,y1+(y2-y1)*r)
                float x1 = startX + 150*(float)randomNumberGenerator.nextValue() - 75;
                float y1 = startY + 150*(float)randomNumberGenerator.nextValue() - 75;

                canvas.drawLine(startX, startY, x1, y1, mFillPaint);

                canvas.drawCircle(x1, y1, 3, mFillPaint);
            }
        }

        randomNumberGenerator.reset();
    }

    /**
     * Class to handle a random number generator based on a particular seed.
     */
    public class RandomWithSeed {
        private Random value;
        private long seed;

        public RandomWithSeed(long seed) {
            value = new Random();
            this.seed = seed;
            value.setSeed(seed);
        }

        /**
         * @return the next random value.
         */
        public double nextValue() {
            return value.nextDouble();
        }

        /**
         * Return to the starting seed position of the Random value generator.
         */
        public void reset() {
            value = new Random(seed);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();
        invalidate();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //TODO(clidwin) Show hovered/pressed down area.
                popUp.dismiss();
                break;
            case MotionEvent.ACTION_UP:
                //TODO(clidwin): Handle click
                Log.e(TAG, "(" + touchX + ", " + touchY + ")");
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    /**
     * @return a random color value.
     */
    private int getRandomColor() {
        double randomNumber = (float)randomNumberGenerator.nextValue();

        if (randomNumber < 0.33) {
            return getResources().getColor(R.color.light_blue_500);
        } else if (randomNumber < 0.66) {
            return getResources().getColor(R.color.light_green_500);
        } else {
            return getResources().getColor(R.color.teal_500);
        }
    }
}