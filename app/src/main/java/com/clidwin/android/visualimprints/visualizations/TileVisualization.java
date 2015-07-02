package com.clidwin.android.visualimprints.visualizations;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.activities.VisualizationsActivity;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.storage.DatabaseAdapter;
import com.clidwin.android.visualimprints.ui.PinRect;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Controlling class for the entire application.
 *
 * @author Christina Lidwin (clidwin)
 * @version June 01, 2015
 */
public class TileVisualization extends View {
    private static final String TAG = "visualimprints-test-vis";

    //private final boolean mShowText;
    private Paint mFillPaint;
    private Paint mBorderPaint;
    private int preferredViewSize = 400;

    private PopupWindow popUp;

    private float hourCellHeight;
    private float hourCellWidth;

    private ArrayList<GeospatialPin> lastLocations;
    private ArrayList<PinRect> drawnRects;

    GregorianCalendar arrivalTime = new GregorianCalendar();

    public TileVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.TileVisualization,
                0, 0);

        try {
            //mShowText = a.getBoolean(R.styleable.TestVisualization_showText2, false);
        } finally {
            a.recycle();
        }

        initializePaints();
        drawnRects = new ArrayList<>();

        popUp = new PopupWindow();

        VisualizationsActivity activity = (VisualizationsActivity) getContext();
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

        hourCellWidth = canvas.getWidth() / 4;
        hourCellHeight = canvas.getHeight() / 6;

        float secondIncrementWidth = hourCellWidth/3600000;

        Log.e(TAG, "interval width: " + secondIncrementWidth * 30);
        boolean colorReverse = false;

        for (GeospatialPin pin: lastLocations) {
            // Information provided by the pin;
            arrivalTime.setTime(pin.getArrivalTime());
            int hour = arrivalTime.get(GregorianCalendar.HOUR_OF_DAY);
            long duration = pin.getDuration();

            // Calculate placement
            int displayRow = (hour)/4;
            float leftSide = ((hour % 4) * 3600000 +
                        arrivalTime.get(GregorianCalendar.MINUTE) * 60000) * secondIncrementWidth;
            float rightSide = leftSide + duration * secondIncrementWidth;

            if (displayRow % 2 != 0) { // If the row is odd, reverse trend  (0 is even in this case)
                float endOfCanvas = hourCellWidth * 4;
                rightSide = endOfCanvas - leftSide;
                leftSide = rightSide - duration * secondIncrementWidth;
            }

            // Select color.
            mFillPaint.setColor(Color.GREEN);
            if (colorReverse) {
                mFillPaint.setColor(Color.DKGRAY);
            }

            // Draw cell.
            //TODO(clidwin): Fix memory issue.
            PinRect pinRect = new PinRect(pin, new RectF(
                    leftSide,
                    displayRow * hourCellHeight,
                    rightSide,
                    (displayRow + 1) * hourCellHeight));
            canvas.drawRect(pinRect.getRectangle(), mFillPaint);
            drawnRects.add(pinRect);
            colorReverse = !colorReverse;
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "(" + touchX + ", " + touchY + ")");
                getLocationTime(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    /**
     * @param touchX
     * @param touchY
     * @return
     */
    private void getLocationTime(float touchX, float touchY) {
        for(PinRect pinRect: drawnRects){
            if(pinRect.getRectangle().contains(touchX, touchY)) {
                //TODO(clidwin): Create popup
                LinearLayout popupLayout = new LinearLayout(getContext());
                popupLayout.setBackgroundColor(Color.WHITE);
                TextView arrivalTimeText = new TextView(getContext());

                arrivalTimeText.setText(pinRect.getPin().getArrivalTime().toString());
                popupLayout.addView(arrivalTimeText);
                popupLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popUp.isShowing()) {
                            popUp.dismiss();
                        }
                    }
                });
                popUp.setContentView(popupLayout);

                popUp.showAtLocation(this, Gravity.CENTER, (int)touchX, (int)touchY);
                popUp.update((int)touchX, (int)touchY, 300, 150);
            }
        }
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
