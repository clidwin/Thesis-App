package com.clidwin.android.visualimprints.visualizations;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.PopupWindow;

import com.clidwin.android.visualimprints.R;
import com.clidwin.android.visualimprints.location.GeospatialPin;

import java.util.ArrayList;

/**
 * Visualization for locational data based on a tile/grid structure.
 *
 * @author Christina Lidwin (clidwin)
 * @version July 15, 2015
 */
public class VoronoiVisualization extends ParentVisualization {
    private static final String TAG = "vi-voronoi-vis";

    private Paint mFillPaint;

    private PopupWindow popUp;

    private ArrayList<PointF> allPoints;

    private float minX;
    private float maxX;
    private float minY;
    private float maxY;

    //TODO(clidwin): Handle week and month view setups.
    public VoronoiVisualization(Context context, AttributeSet attributes) {
        super(context, attributes);

        initializePaints();
        popUp = new PopupWindow();

        allPoints = new ArrayList<>();

        minX = 180;
        maxX = 0;
        minY = 360;
        maxY = 0;
    }

    @Override
    protected void processPin(GeospatialPin pin) {
        //TODO(clidwin): Base off time rather than location
        Location location = pin.getLocation();
        PointF newPoint = new PointF(
                90 + (float)location.getLatitude(),
                180 + (float)location.getLongitude());

        allPoints.add(newPoint);

        if (newPoint.x < minX) { minX = newPoint.x; }
        if (newPoint.x > maxX) { maxX = newPoint.x; }
        if (newPoint.y < minY) { minY = newPoint.y; }
        if (newPoint.y > maxY) { maxY = newPoint.y; }
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
        //canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mFillPaint);
        Drawable bg = getResources().getDrawable(R.drawable.voronoi);
        bg.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        bg.draw(canvas);

        float width = canvas.getWidth();
        float height = canvas.getHeight();

        float xDivider = (Math.abs(maxX - minX));
        float yDivider = (Math.abs(maxY - minY));

        // Draw all data points
        mFillPaint.setColor(getResources().getColor(R.color.black));

        ArrayList<PointF> voronoiPoints = new ArrayList<>();
        for (int i = 0; i < allPoints.size(); i++) {
            PointF point = allPoints.get(i);
            canvas.drawCircle(
                    ((point.x - minX)/xDivider)*width,
                    ((point.y - minY)/yDivider)*height,
                    3.0f,
                    mFillPaint);

            voronoiPoints.add(new PointF(
                    ((point.x - minX)/xDivider)*width,
                    ((point.y - minY)/yDivider)*height
            ));
        }

        //TODO(clidwin): Fortune's algorithm to draw edges

        //Log.e(TAG, "Number of edges: " + voronoi.getEdgeList().size());

        /*mFillPaint.setTextSize((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()));
        canvas.drawText("Visualization coming soon.", 50, 150, mFillPaint);*/
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
