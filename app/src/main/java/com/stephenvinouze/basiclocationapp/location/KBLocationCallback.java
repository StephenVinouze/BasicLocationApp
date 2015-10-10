package com.stephenvinouze.basiclocationapp.location;

import android.location.Location;

/*
 * Created by stephenvinouze on 13/03/15.
 */
public interface KBLocationCallback {
    void onLocationReceived(Location location);
    void onLocationFailed();
    void onLocationRefused();
}