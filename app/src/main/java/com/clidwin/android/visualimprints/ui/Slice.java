package com.clidwin.android.visualimprints.ui;

import android.graphics.RectF;

import com.clidwin.android.visualimprints.location.GeospatialPin;

import java.util.GregorianCalendar;

/**
 * Wrapper for the data in a single slice/location element for the TileVisualization.
 *
 * @author clidwin
 * @version July 21, 2015
 */
public class Slice {
    private RectF rect;
    private boolean rectangleIsSet;
    private GeospatialPin pin;

    public Slice(GeospatialPin pin) {
        this(pin, new RectF(), false);
    }

    public Slice(GeospatialPin pin, RectF rect, boolean rectangleIsSet) {
        this.pin = pin;
        this.rect = rect;
        this.rectangleIsSet = rectangleIsSet;
    }

    /**
     * @return the {@link RectF} associated with this object
     */
    public RectF getRectangle() {
        return rect;
    }

    /**
     * @return the {@link GeospatialPin} associated with this object
     */
    public GeospatialPin getPin() {
        return pin;
    }

    /**
     * @return true if the rectangle's position has been set, else false
     */
    public boolean isRectangleSet() {
        return rectangleIsSet;
    }

    /**
     * @return the time this pin was created in
     */
    public GregorianCalendar getArrivalTime() {
        GregorianCalendar arrivalTime = new GregorianCalendar();
        arrivalTime.setTime(pin.getArrivalTime());
        return arrivalTime;
    }

    public long getDuration() {
        return pin.getDuration();
    }

    public void setRectangle(RectF rectangle) {
        this.rect = rectangle;
    }
}
