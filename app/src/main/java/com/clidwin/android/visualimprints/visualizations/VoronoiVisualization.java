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
public class VoronoiVisualization extends ParentVisualization {
    private static final String TAG = "visualimprints-tile-vis";

    private Paint mFillPaint;

    private PopupWindow popUp;

    //TODO(clidwin): Handle week and month view setups.
    public VoronoiVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        initializePaints();
        popUp = new PopupWindow();
    }

    @Override
    protected void processPin(GeospatialPin pin) {
        //TODO(clidwin): Implement
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
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);

        mFillPaint.setColor(getResources().getColor(R.color.black));
        mFillPaint.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        canvas.drawText("Visualization coming soon", 50, 150, mFillPaint);
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
}
