package com.clidwin.android.visualimprints.visualizations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.clidwin.android.visualimprints.Constants;
import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.ui.GridCell;
import com.clidwin.android.visualimprints.ui.Slice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

/**
 * Visualization for locational data based on a tile/grid structure.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 15, 2015
 */
public class TileVisualization extends ParentVisualization {
    private static final String TAG = "visualimprints-tile-vis";

    private Paint mFillPaint;
    private Paint mOverlayPaint;
    private Paint mHoverTextPaint;

    private PopupWindow popUp;

    private ArrayList<Slice> drawnRects;
    private ArrayList<GridCell> gridCells;
    private GridCell selectedCell;

    //TODO(clidwin): Handle week and month view setups.
    public TileVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        initializePaints();
        drawnRects = new ArrayList<>();

        popUp = new PopupWindow();
    }

    @Override
    protected void processPin(GeospatialPin pin) {
        //TODO(clidwin): Move some onDraw things in here
        Slice slice = new Slice(pin);
        drawnRects.add(slice);
    }

    /**
     * Create components used for drawing the visualization.
     */
    private void initializePaints() {
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(Color.LTGRAY);

        mOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOverlayPaint.setColor(Color.BLACK);
        mOverlayPaint.setAlpha(75);

        mHoverTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHoverTextPaint.setColor(Color.WHITE);
        mHoverTextPaint.setTextAlign(Paint.Align.CENTER);
        mHoverTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.title_text_size));
    }

    //TODO(clidwin): Remove the @SuppressLint and fix warning related to allocation in setRect
    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Paint the background.
        mFillPaint.setColor(Color.LTGRAY);
        mFillPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        float hourCellWidth = canvas.getWidth() / 4;
        float hourCellHeight = canvas.getHeight() / 6;

        float secondIncrementWidth = hourCellWidth /3600000;

        boolean colorReverse = false;

        for (Slice slice : drawnRects) {
            // Calculate placement
            if (!slice.isRectangleSet()) {
                GregorianCalendar arrivalTime = slice.getArrivalTime();
                int hour = arrivalTime.get(GregorianCalendar.HOUR_OF_DAY);
                long duration = slice.getDuration();


                int displayRow = (hour)/4;
                float leftSide = ((hour % 4) * 3600000 +
                        arrivalTime.get(GregorianCalendar.MINUTE) * 60000) * secondIncrementWidth;
                float rightSide = leftSide + duration * secondIncrementWidth;

                if (displayRow % 2 != 0) { // If the row is odd, reverse trend  (0 is even in this case)
                    float endOfCanvas = hourCellWidth * 4;
                    rightSide = endOfCanvas - leftSide;
                    leftSide = rightSide - duration * secondIncrementWidth;
                }

                slice.setRectangle(new RectF(
                        leftSide,
                        displayRow * hourCellHeight,
                        rightSide,
                        (displayRow + 1) * hourCellHeight));
            }

            // Select color.
            mFillPaint.setColor(getResources().getColor(R.color.light_blue_500));
            if (colorReverse) {
                mFillPaint.setColor(getResources().getColor(R.color.light_blue_200));
            }

            // Draw cell.
            canvas.drawRect(slice.getRectangle(), mFillPaint);
            colorReverse = !colorReverse;
        }

        // Draw grid overlay.
        drawGrid(canvas, hourCellHeight, hourCellWidth);
    }

    /**
     * Draws a grid to delineate hours being shown.
     *
     * @param canvas The drawing area.
     * @param hourCellHeight The number of divisions along the Y coordinate axis
     * @param hourCellWidth The number of divisions along the X coordinate axis
     */
    private void drawGrid(Canvas canvas, float hourCellHeight, float hourCellWidth) {
        mFillPaint.setColor(getResources().getColor(R.color.white));
        mFillPaint.setStrokeWidth(2);
        // Draw horizontal lines.
        /*for (int i=1; i<4; i++) {
            canvas.drawLine(i*hourCellWidth, 0, i*hourCellWidth, canvas.getHeight(), mFillPaint);
        }

        // Draw vertical lines.
        for (int i=1; i<6; i++) {
            canvas.drawLine(0, i*hourCellHeight, canvas.getWidth(), i*hourCellHeight, mFillPaint);
        }*/

        // Set up rects
        //TODO(clidwin): Only add grid cells if this hasn't executed before
        gridCells = new ArrayList<>();
        for (int i=0; i<4; i++) {
            for (int j=0; j<6; j++) {
                GridCell newCell = new GridCell(
                        i*hourCellWidth, j*hourCellHeight, hourCellWidth, hourCellHeight);
                newCell.setValue(calculateHour(j, i));
                gridCells.add(newCell);
                canvas.drawLine(0, j*hourCellHeight, canvas.getWidth(), j*hourCellHeight, mFillPaint);
            }
            canvas.drawLine(i*hourCellWidth, 0, i*hourCellWidth, canvas.getHeight(), mFillPaint);
        }

        if (selectedCell != null) {
            canvas.drawRect(
                    selectedCell.topLeftCornerX,
                    selectedCell.topLeftCornerY,
                    selectedCell.topLeftCornerX + selectedCell.width,
                    selectedCell.topLeftCornerY + selectedCell.height,
                    mOverlayPaint);

            canvas.drawText(
                    "" + selectedCell.getValue(),
                    selectedCell.topLeftCornerX + selectedCell.width/2,
                    selectedCell.topLeftCornerY + selectedCell.height/2
                            + mHoverTextPaint.getTextSize()/2,
                    mHoverTextPaint);
        }

        // Draw border (for tile transitions).
        mFillPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);
    }

    /**
     * Calculates the hour a grid cell represents.
     * @param row The row of the grid cell.
     * @param column The column of the grid cell.
     * @return the hour the grid cell represents.
     */
    private int calculateHour(int row, int column) {
        int hour = row*4;
        if ((row+1)%2==0) {
            return hour + 4 - (column + 1);
        }
        else {
            return hour + column;
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
                if (gridCells != null) {
                    for (GridCell cell: gridCells) {
                        if (cell.containsPoint(touchX, touchY)) {
                            selectedCell = cell;
                        }
                    }
                }
                popUp.dismiss();
                break;
            case MotionEvent.ACTION_UP:
                //TODO(clidwin): Handle touching areas with no data and revamp popup
                Log.e(TAG, "(" + touchX + ", " + touchY + ")");
                selectedCell = null;
                showLocationInfo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    /**
     * @param touchX the touch location's x coordinate
     * @param touchY the touch location's y coordinate
     */
    private void showLocationInfo(float touchX, float touchY) {
        for(Slice slice : drawnRects){
            if(slice.getRectangle().contains(touchX, touchY)) {
                //TODO(clidwin): Create dynamic popup
                LinearLayout popupLayout = new LinearLayout(getContext());
                popupLayout.setOrientation(LinearLayout.VERTICAL);
                popupLayout.setBackgroundColor(Color.WHITE);

                int padding = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
                popupLayout.setPadding(padding, padding, padding, padding);

                // Show display time as heading
                SimpleDateFormat timeFormat = new SimpleDateFormat(Constants.DISPLAY_TIME_FORMAT);
                TextView arrivalTimeText = new TextView(getContext());
                arrivalTimeText.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        16);
                arrivalTimeText.setText(
                        timeFormat.format(slice.getPin().getArrivalTime().getTime()) + " record");
                popupLayout.addView(arrivalTimeText);

                // Date description.
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DISPLAY_DATE_FORMAT);
                TextView arrivalDateText = new TextView(getContext());
                arrivalDateText.setText(
                        "Arrival Time: " + dateFormat.format(slice.getPin().getArrivalTime().getTime()));
                popupLayout.addView(arrivalDateText);

                // Show duration.
                TextView durationText = new TextView(getContext());
                durationText.setText("Duration: "
                        + getDurationTimeString(slice.getPin().getDuration()));
                popupLayout.addView(durationText);

                Location location = slice.getPin().getLocation();
                TextView locationText = new TextView(getContext());
                locationText.setText("Location: ("
                        + location.getLatitude() + ", " + location.getLongitude() + ")");
                popupLayout.addView(locationText);

                popupLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (popUp.isShowing()) {
                            popUp.dismiss();
                        }
                    }
                });
                popUp.setContentView(popupLayout);

                popUp.showAtLocation(
                        this, Gravity.NO_GRAVITY, getWidth()/2, getHeight()/2);
                popUp.update((int)touchX, (int)touchY, 400, 200);
            }
        }
    }

    /**
     * Calculates and returns a human-readable version of the location duration.
     *
     * @param millis The duration formatted in milliseconds.
     * @return a String-formatted version of the duration.
     */
    private String getDurationTimeString(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
