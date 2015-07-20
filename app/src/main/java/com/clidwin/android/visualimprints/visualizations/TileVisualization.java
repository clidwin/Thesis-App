package com.clidwin.android.visualimprints.visualizations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
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
import com.clidwin.android.visualimprints.ui.Tile;

import java.lang.reflect.Type;
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

    private PopupWindow popUp;

    private ArrayList<Tile> drawnRects;

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
        Tile tile = new Tile(pin);
        drawnRects.add(tile);
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
        mFillPaint.setColor(Color.LTGRAY);
        mFillPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        float hourCellWidth = canvas.getWidth() / 4;
        float hourCellHeight = canvas.getHeight() / 6;

        float secondIncrementWidth = hourCellWidth /3600000;

        boolean colorReverse = false;

        for (Tile tile: drawnRects) {
            // Calculate placement
            if (!tile.isRectangleSet()) {
                GregorianCalendar arrivalTime = tile.getArrivalTime();
                int hour = arrivalTime.get(GregorianCalendar.HOUR_OF_DAY);
                long duration = tile.getDuration();


                int displayRow = (hour)/4;
                float leftSide = ((hour % 4) * 3600000 +
                        arrivalTime.get(GregorianCalendar.MINUTE) * 60000) * secondIncrementWidth;
                float rightSide = leftSide + duration * secondIncrementWidth;

                if (displayRow % 2 != 0) { // If the row is odd, reverse trend  (0 is even in this case)
                    float endOfCanvas = hourCellWidth * 4;
                    rightSide = endOfCanvas - leftSide;
                    leftSide = rightSide - duration * secondIncrementWidth;
                }

                tile.setRectangle(new RectF(
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
            canvas.drawRect(tile.getRectangle(), mFillPaint);
            colorReverse = !colorReverse;
        }

        // Draw grid overlay.
        drawGrid(canvas, hourCellHeight, hourCellWidth);
    }

    /**
     * Draws a grid to deliniate hours being shown.
     *
     * @param canvas The drawing area.
     * @param hourCellHeight The number of divisions along the Y coordinate axis
     * @param hourCellWidth The number of divisions along the X coordinate axis
     */
    private void drawGrid(Canvas canvas, float hourCellHeight, float hourCellWidth) {

        mFillPaint.setColor(getResources().getColor(R.color.white));
        mFillPaint.setStrokeWidth(2);
        // Draw horizontal lines.
        for (int i=1; i<4; i++) {
            canvas.drawLine(i*hourCellWidth, 0, i*hourCellWidth, canvas.getHeight(), mFillPaint);
        }

        // Draw vertical lines.
        for (int i=1; i<6; i++) {
            canvas.drawLine(0, i*hourCellHeight, canvas.getWidth(), i*hourCellHeight, mFillPaint);
        }

        // Draw border (for tile transitions).
        mFillPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);
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
                //TODO(clidwin): Handle touching areas with no data and revamp popup
                Log.e(TAG, "(" + touchX + ", " + touchY + ")");
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
        for(Tile tile : drawnRects){
            if(tile.getRectangle().contains(touchX, touchY)) {
                //TODO(clidwin): Create popup
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
                        timeFormat.format(tile.getPin().getArrivalTime().getTime()) + " record");
                popupLayout.addView(arrivalTimeText);

                // Date description.
                SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DISPLAY_DATE_FORMAT);
                TextView arrivalDateText = new TextView(getContext());
                arrivalDateText.setText(
                        "on " + dateFormat.format(tile.getPin().getArrivalTime().getTime()));
                popupLayout.addView(arrivalDateText);

                // Show duration.
                TextView durationText = new TextView(getContext());
                long millis = tile.getPin().getDuration();
                durationText.setText(
                        String.format("for %d min, %d sec",
                                TimeUnit.MILLISECONDS.toMinutes(millis),
                                TimeUnit.MILLISECONDS.toSeconds(millis) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                        ));
                popupLayout.addView(durationText);

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
}
