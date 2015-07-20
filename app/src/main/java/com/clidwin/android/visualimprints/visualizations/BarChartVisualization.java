package com.clidwin.android.visualimprints.visualizations;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.location.GeospatialPin;

import java.util.Calendar;

/**
 * Visualization for locational data based on a bar chart format.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 15, 2015
 */
public class BarChartVisualization extends ParentVisualization {
    private static final String TAG = "visualimprints-bar-vis";

    private Paint mFillPaint;
    private Paint mTextPaint;
    private RectF drawingRect;

    private int [] barInfo;
    private int maxBarHeight;

    public BarChartVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        // Set up drawing tools
        initializePaints();
        drawingRect = new RectF();

        // Set up information holders
        barInfo = new int[24];
        maxBarHeight = 0;
    }

    @Override
    public void processPin(GeospatialPin pin) {
        Calendar pinTime = Calendar.getInstance();
        pinTime.setTimeInMillis(pin.getArrivalTime().getTime());

        int hourOfTheDay = pinTime.get(Calendar.HOUR_OF_DAY);
        barInfo[hourOfTheDay] = barInfo[hourOfTheDay]+ 1;
        if (barInfo[hourOfTheDay] > maxBarHeight) {
            maxBarHeight = barInfo[hourOfTheDay];
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /**
     * Create components used for drawing the visualization.
     */
    private void initializePaints() {
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(Color.LTGRAY);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //TODO(clidwin): figure out why this draws slowest.
        super.onDraw(canvas);

        if (maxBarHeight == 0) {
            return;
        }

        // Paint the background.
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        float cellWidth = canvas.getWidth() / 24;
        float cellHeight = canvas.getHeight() / maxBarHeight;

        boolean colorReverse = false;
        for (int i=0; i< barInfo.length; i++, colorReverse = !colorReverse) {
            // Select color.
            int color = colorReverse ?
                    getResources().getColor(R.color.green_200) :
                    getResources().getColor(R.color.green_500);
            mFillPaint.setColor(color);

            // Draw bar
            drawingRect.set(
                    cellWidth * i,
                    canvas.getHeight() - (cellHeight * barInfo[i]),
                    cellWidth * (i + 1),
                    canvas.getHeight());
            canvas.drawRect(drawingRect, mFillPaint);

            // Draw bar label
            canvas.drawText(
                    "" + barInfo[i],
                    (float) (cellWidth * (i + 0.5)),
                    canvas.getHeight() - (cellHeight * barInfo[i]),
                    mTextPaint
            );
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);

        float touchX = event.getX();
        float touchY = event.getY();

        //TODO(clidwin): Implement interaction
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "Touched at: (" + touchX + ", " + touchY + ")");
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }
}