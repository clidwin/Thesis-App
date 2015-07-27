package com.clidwin.android.visualimprints;

/**
 * Constant values reused in this sample.
 *
 * @author Christina Lidwin (clidwin)
 * @version May 09, 2015
 */
public final class Constants {
    public static final int SUCCESS_RESULT = 0;

    public static final int FAILURE_RESULT = 1;

    public static final String PACKAGE_NAME = "com.clidwin.android.visualimprints";

    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";

    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";

    /* Broadcast information */
    public static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST";
    public static final String BROADCAST_UPDATE = PACKAGE_NAME + ".BROADCAST_UPDATE";
    public static final String BROADCAST_NEW_LOCATION = PACKAGE_NAME + ".NEW_LOCATION_ADDED";
    public static final String BROADCAST_UPDATED_LOCATION = PACKAGE_NAME + ".UPDATED_LOCATION";

    /* Keys */
    public static final String RESULT_ADDRESS_KEY = PACKAGE_NAME + ".RESULT_ADDRESS_KEY";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";

    /**
     * Desired interval for location updates in milliseconds (updates may be more or less frequent)
     */
    public static final long UPDATE_INTERVAL = 45000; //45 seconds

    /**
     * Fastest interval for location updates in milliseconds (updates may be more or less frequent)
     */
    public static final long FASTEST_UPDATE_INTERVAL = 30000; //30 seconds

    /**
     * Desired distance to receive location updates in meters (updates may be more or less frequent)
     */
    public static final long UPDATE_DISTANCE = 2;

    /**
     * String code based on:
     * https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */
    public static final String DATABASE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ EEE";

    /**
     * String code based on:
     * https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */
    public static final String DATABASE_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * String code based on:
     * https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */
    public static final String DATABASE_TIME_FORMAT = "HH:mm:ss.SSSZ EEE";
    public static final String BROADCAST_SERVICE_START = "GPS service started";

    public static final String DISPLAY_DATE_FORMAT = "EEE, MMMM dd, yyyy";
    public static final String DISPLAY_TIME_FORMAT = "hh:mm a";
}

