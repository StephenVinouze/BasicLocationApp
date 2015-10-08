package com.stephenvinouze.basiclocationapp.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Created by stephenvinouze on 17/09/2014.
 */
@EBean(scope = EBean.Scope.Singleton)
public class KBLocationProvider implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    @RootContext
    Context mContext;

    private boolean mIsUpdatingLocation;
    private boolean mIsListeningLocationUpdates;
    private float mDesiredAccuracy = MINIMUM_ACCURACY;
    private int mTimeoutDuration;
    private Timer mTimer;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocationRequest;
    private KBLocationCallback mCallback;

    private static Location kLocation;

    private static final String TAG = "LocationProvider";
    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = INTERVAL / 2;
    private static final float MINIMUM_ACCURACY = 50.0f;

    @AfterInject
    void init() {
        mApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    public int getTimeoutDuration() {
        return mTimeoutDuration;
    }

    public void setTimeoutDuration(int mTimeoutDuration) {
        this.mTimeoutDuration = mTimeoutDuration;
    }

    public boolean isIsListeningLocationUpdates() {
        return mIsListeningLocationUpdates;
    }

    public void setIsListeningLocationUpdates(boolean mIsListeningLocationUpdates) {
        this.mIsListeningLocationUpdates = mIsListeningLocationUpdates;
    }

    public float getDesiredAccuracy() {
        return mDesiredAccuracy;
    }

    public void setDesiredAccuracy(float mDesiredAccuracy) {
        this.mDesiredAccuracy = mDesiredAccuracy;
    }

    public static Location getLocation() {
        return kLocation;
    }

    public static String getCity(Context context) {
        String city = null;
        if (kLocation != null) {

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocation(kLocation.getLatitude(), kLocation.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    city = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return city;
    }

    private void startLocationUpdates() {
        Log.i(TAG, "Location started");

        mIsUpdatingLocation = true;
        LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient, mLocationRequest, this);
        startTimer(mTimeoutDuration);
    }

    private void stopLocationUpdates() {
        Log.i(TAG, "Location stopped");

        mIsUpdatingLocation = false;
        stopTimer();

        if (mApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mApiClient, this);
            mApiClient.disconnect();
        }
    }

    private void startTimer(int timeoutDuration) {
        if (mTimer == null && timeoutDuration > 0) {

            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mApiClient.isConnected()) {
                        stopLocationUpdates();

                        if (mCallback != null) {
                            mCallback.onLocationFailed();
                        }
                    }
                }
            }, timeoutDuration);
        }
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Background
    public void fetchLocation() {
        fetchLocation(null);
    }

    @Background
    public void fetchLocation(KBLocationCallback callback) {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS) {

            mCallback = callback;

            if (!mApiClient.isConnected()) {
                mApiClient.connect();
            }
            else if (!mIsUpdatingLocation) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.i(TAG, "Connection successful");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed");
        mApiClient.disconnect();

        if (mCallback != null) {
            mCallback.onLocationFailed();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location changed (Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude() + ")");

        kLocation = location;

        Log.i(TAG, "city found " + getCity(mContext));

        if (!mIsListeningLocationUpdates && location.getAccuracy() < mDesiredAccuracy) {
            stopLocationUpdates();
        }

        if (mCallback != null) {
            mCallback.onLocationReceived(location);
        }
    }

}
