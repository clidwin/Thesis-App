package com.clidwin.android.visualimprints.visualizations;

import android.content.Context;
import android.graphics.Canvas;
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
        mFillPaint.setColor(getResources().getColor(R.color.grey_300));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getResources().getColor(R.color.TextPrimary));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.small_text_size));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //TODO(clidwin): figure out why this draws slowest.
        super.onDraw(canvas);

        if (maxBarHeight == 0) {
            return;
        }

        // Paint the background and the grid.
        mFillPaint.setColor(getResources().getColor(R.color.grey_300));
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        float cellWidth = (canvas.getWidth() - 64) / 24;
        //TODO(clidwin): Make this dynamic (more than 50 shouldn't have as many lines as points
        float cellHeight = (canvas.getHeight() - 64) / (maxBarHeight+2);
        drawGrid(canvas, cellWidth, cellHeight);

        // Draw bars
        boolean colorReverse = false;
        for (int i=0; i< barInfo.length; i++, colorReverse = !colorReverse) {
            // Select color.
            int color = colorReverse ?
                    getResources().getColor(R.color.green_200) :
                    getResources().getColor(R.color.green_500);
            mFillPaint.setColor(color);

            // Draw bar
            drawingRect.set(
                    cellWidth * i + 64,
                    (canvas.getHeight() - 72) - (cellHeight * barInfo[i]),
                    cellWidth * (i + 1) + 64,
                    canvas.getHeight() - 72);
            canvas.drawRect(drawingRect, mFillPaint);

            // Draw bar label
            if (barInfo[i] != 0) {
                canvas.drawText(
                        "" + barInfo[i],
                        (float) (cellWidth * (i + 0.5)) + 64,
                        (canvas.getHeight() - 72) - (cellHeight * barInfo[i]) - 4,
                        mTextPaint
                );
            }
        }
    }

    /**
     * Draws the grid lines and labels.
     * @param canvas The canvas to draw the grid on.
     * @param cellWidth The height of each cell.
     * @param cellHeight The width of each cell.
     */
    private void drawGrid(Canvas canvas, float cellWidth, float cellHeight) {

        mFillPaint.setColor(getResources().getColor(R.color.grey_400));
        // Draw the vertical quantity divisions.
        for (int i=0; i<maxBarHeight + 2; i++) {
            float verticalDraw = ((maxBarHeight + 2) - i)*cellHeight;
            Log.e(TAG, "VerticalDraw: " + i);

            if (i%5 == 0) {
                mFillPaint.setColor(getResources().getColor(R.color.grey_500));
                canvas.drawLine(56, verticalDraw, canvas.getWidth(), verticalDraw, mFillPaint);
                mFillPaint.setColor(getResources().getColor(R.color.grey_400));

                canvas.drawText("" + i, 40, verticalDraw, mTextPaint);
            } else {
                canvas.drawLine(64, verticalDraw, canvas.getWidth(), verticalDraw, mFillPaint);
            }
        }

        // Draw the timestamp dividing lines
        //TODO(clidwin): Make this dynamic for custom timeframes
        for (int j=0; j<=24; j++) {
            if (j%5 == 0) {
                mFillPaint.setColor(getResources().getColor(R.color.grey_500));
                canvas.drawLine(
                        j * cellWidth + 64, 0,
                        j * cellWidth + 64, canvas.getHeight() - 56,
                        mFillPaint);
                mFillPaint.setColor(getResources().getColor(R.color.grey_400));

                canvas.drawText("" + j, j * cellWidth + 64, canvas.getHeight() - 40, mTextPaint);
            } else {
                canvas.drawLine(
                        j*cellWidth + 64, 0, j*cellWidth + 64, canvas.getHeight() - 64, mFillPaint);
            }
        }

        // Draw axis labels.
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.default_text_size));
        canvas.drawText(
                "Recorded Hour",
                canvas.getWidth() / 2,
                canvas.getHeight() - 16,
                mTextPaint);

        canvas.save();
        canvas.rotate(270, canvas.getWidth()/2, canvas.getHeight()/2);
        canvas.drawText(
                "Number of Data Points",
                canvas.getWidth()/2,
                canvas.getHeight()/4 - 32,
                mTextPaint);
        canvas.restore();
        mTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.small_text_size));
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