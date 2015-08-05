package com.clidwin.android.visualimprints.ui;

/**
 * UI element denoting the location and dimensions of a rectangular region.
 *
 * @author clidwin
 * @version August 5, 2015
 */
public class GridCell {

    public float height;
    public float topLeftCornerX;
    public float topLeftCornerY;
    public float width;
    private int value;

    public GridCell(float x, float y, float width, float height) {
        topLeftCornerX = x;
        topLeftCornerY = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Sets the value associated with this cell.
     * @param value A multipurpose value associated with this cell.
     */
    public void setValue(int value) { this.value = value; }

    /**
     * @return the value associated with this cell.
     */
    public int getValue() { return value; }

    /**
     * Determines whether a point is contained by this gridcell.
     *
     * @param x The x point to be verified.
     * @param y The y point to be verified.
     * @return true if the parameter coordinate is inside this gridcell, else false.
     */
    public boolean containsPoint(float x, float y) {
        if (x >= topLeftCornerX && x <= topLeftCornerX + width) {
            if (y >= topLeftCornerY && y <= topLeftCornerY + height) {
                return true;
            }
        }
        return false;
    }
}
