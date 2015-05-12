package com.clidwin.android.visualimprints;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.clidwin.android.visualimprints.services.GpsLocationService;

/**
 *  Called when the device is turned on, this class initializes the GpsLocationService.
 *
 *  Code reference:
 *        //stackoverflow.com/questions/20595337/how-to-start-service-at-device-boot-in-android
 *
 * @author Christina Lidwin (clidwin)
 * @version April 27, 2015
 */
public class StartupBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent gpsIntent = new Intent(context, GpsLocationService.class);
        context.startService(gpsIntent);
    }
}
