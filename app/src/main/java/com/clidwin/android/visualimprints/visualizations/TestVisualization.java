package com.clidwin.android.visualimprints.visualizations;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.MainActivity;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;

import java.util.ArrayList;

/**
 * Controlling class for the entire application.
 *
 * @author Christina Lidwin (clidwin)
 * @version June 01, 2015
 */
public class TestVisualization extends View {
    private static final String TAG = "visualimprints-test-vis";

    //private final boolean mShowText;
    private Paint mFillPaint;
    private Paint mBorderPaint;
    private int preferredViewSize = 400;

    private ArrayList<GeospatialPin> lastLocations;

    public TestVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.TestVisualization,
                0, 0);

        try {
            //mShowText = a.getBoolean(R.styleable.TestVisualization_showText2, false);
        } finally {
            a.recycle();
        }

        initializePaints();

        MainActivity activity = (MainActivity) getContext();
        if (activity != null) {
            DatabaseAdapter dbAdapter = activity.getDatabaseAdapter();
            lastLocations = dbAdapter.getLast24HoursOfEntries();
            /*Log.e(TAG, "Number of locations: " + lastLocations.size());
            for(GeospatialPin pin: lastLocations) {
                Log.e(TAG, pin.getArrivalTime().toString());
            }*/
        } else {
            //TODO(clidwin): Display a message saying the database wasn't found.
            Log.e(TAG, "Database disconnected");
        }
    }

    /**
     * Create components used for drawing the visualization.
     */
    private void initializePaints() {
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(Color.LTGRAY);

        mBorderPaint = new Paint(Paint.LINEAR_TEXT_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG,
                "drawing on canvas (" + canvas.getWidth()
                        + ", " + canvas.getHeight() + ")");
        super.onDraw(canvas);

        // Paint the background.
        mFillPaint.setColor(Color.LTGRAY);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        int hourCellWidth = canvas.getWidth() / 4;
        int hourCellHeight = canvas.getHeight() / 6;

        boolean reverse = false;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 4; col++) {
                float hue;
                if (reverse) {
                    int rowAdjust = 4 - row;
                    hue = (float)(15 * ((row * 4) + (3 - col) + 1));
                } else {
                    hue = (float)(15 * ((row * 4) + col + 1));
                }
                float[] hsbColor = {hue, (float) 0.5, (float) 0.5};
                mFillPaint.setColor(Color.HSVToColor(hsbColor));
                canvas.drawRect(
                    col * hourCellWidth,
                    row * hourCellHeight,
                    (col + 1) * hourCellWidth,
                    (row + 1) * hourCellHeight,
                    mFillPaint
                );
            }
            reverse = !reverse;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * Overrides the fragment's onMeasure to fill the screen.
     *      Code based on: http://stackoverflow.com/questions/8577117/
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
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
        return mode == MeasureSpec.UNSPECIFIED ? preferredViewSize : size;
    }
}
