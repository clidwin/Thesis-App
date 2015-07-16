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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.location.GeospatialPin;
import com.clidwin.android.visualimprints.ui.Tile;

import java.util.ArrayList;
import java.util.GregorianCalendar;

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
        Log.d(TAG,
                "drawing on canvas (" + canvas.getWidth()
                        + ", " + canvas.getHeight() + ")");
        super.onDraw(canvas);

        // Paint the background.
        mFillPaint.setColor(Color.LTGRAY);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        float hourCellWidth = canvas.getWidth() / 4;
        float hourCellHeight = canvas.getHeight() / 6;

        float secondIncrementWidth = hourCellWidth /3600000;

        Log.e(TAG, "interval width: " + secondIncrementWidth * 30);
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
                popupLayout.setBackgroundColor(Color.WHITE);
                TextView arrivalTimeText = new TextView(getContext());

                arrivalTimeText.setText(tile.getPin().getArrivalTime().toString());
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
}
