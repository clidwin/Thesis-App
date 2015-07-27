package com.clidwin.android.visualimprints.ui;

import java.util.ArrayList;

/**
 * Wrapper for a group of visual location data items (slices).
 *
 * @author clidwin
 * @version July 27, 2015
 */
public class Cluster {
    private ArrayList<Slice> slices;

    private int startX;
    private int startY;
    private int width;
    private int height;

    public Cluster() {
        slices = new ArrayList<>();
    }

    public Cluster(int startX, int startY, int width, int height, ArrayList<Slice> slices) {
        this.startX = startX;
        this.startY = startY;
        this.width = width;
        this.height = height;

        this.slices = slices;
    }

    /**
     * Add a slice to the tile.
     * @param slice The slice to be added.
     */
    public void addSlice(Slice slice) {
        slices.add(slice);
    }

    /**
     * @return all of the {@link Slice} items contained by this tile.
     */
    public ArrayList<Slice> getSlices() { return slices; }

    /**
     * @return the number of slices within this tile.
     */
    public int getSlicesCount() { return slices.size(); }

    /**
     * @return the first {@link Slice} in the list.
     */
    public Slice getFirstSlice() { return slices.get(0); }
}
