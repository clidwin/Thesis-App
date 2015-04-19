package com.google.android.visualimprints;

import android.location.Address;
import android.location.Location;
import android.text.format.Time;

/**
 * Custom data structure representing all of the information relating to a particular location.
 *
 * @author clidwin
 * @created April 19, 2015
 * @modified April 19, 2015
 */
public class GeospatialPin {
    private Address address;
    private Location location;
    private Time arrivalTime;


    public GeospatialPin(Location location) {
        this.location = location;
        this.arrivalTime = new Time();
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
     * @return the {Time} the pin was created
     */
    public Time getArrivalTime() {
        return arrivalTime;
    }
}
