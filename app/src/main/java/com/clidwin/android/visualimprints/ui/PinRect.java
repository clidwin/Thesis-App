package com.clidwin.android.visualimprints.ui;

import android.graphics.RectF;

import com.clidwin.android.visualimprints.location.GeospatialPin;

/**
 * Created by clidwin on 6/2/15.
 */
public class PinRect {
    private RectF rect;
    private GeospatialPin pin;

    public PinRect(GeospatialPin pin, RectF rect) {
        this.pin = pin;
        this.rect = rect;
    }

    /**
     * @return {@link RectF}
     */
    public RectF getRectangle() {
        return rect;
    }

    /**
     * @return the {@link GeospatialPin}
     */
    public GeospatialPin getPin() {
        return pin;
    }
}
