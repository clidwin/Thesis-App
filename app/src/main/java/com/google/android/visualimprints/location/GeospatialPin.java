package com.google.android.visualimprints.location;

import android.location.Address;
import android.location.Location;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Custom data structure representing all of the information relating to a particular location.
 *
 * @author Christina Lidwin (clidwin)
 * @version April 27, 2015
 */
public class GeospatialPin {
    private Address address;
    private Location location;
    private Date arrivalTime;
    private long duration;


    public GeospatialPin(Location location) {
        this(location, new Date(), 0);
    }

    public GeospatialPin(Location location, Date arrivalTime, long duration) {
        this.location = location;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
    }

    /**
     * @return the {Location} of the pin
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return the {Address} of the pin
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the address for the pin.
     *
     * @param address the {Address} object for the geospatial pin.
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * @return the {Date} the pin was created
     */
    public Date getArrivalTime() {
        return arrivalTime;
    }

    /**
     * @return the the amount of time in milliseconds spent at the pin
     */
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * @return the full {@link android.location.Address} in String format.
     */
    public String getReadableAddress() {
        if (address == null) {
            return "";
        }

        ArrayList<String> addressFragments = new ArrayList<String>();
        for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
            addressFragments.add(address.getAddressLine(i));
        }
        return TextUtils.join(System.getProperty("line.separator"), addressFragments);
    }
}
