package com.google.android.visualimprints.storage;

/**
 * Database data model for geospatial pin objects.
 *
 * @author clidwin
 * @created April 19, 2015
 * @modified April 19, 2015
 */
public class GeospatialDataModel {
    private long duration;
    private String location;
    private String address;
    private String arrivalTime;

    public GeospatialDataModel() {
    }

    public void setAddress(String address) { this.address = address; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }
    public void setDuration(long duration) { this.duration = duration; }
    public void setLocation(String location) { this.location = location; }
}
