package com.stephenvinouze.basiclocationapp.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

    private boolean isGPSFix;
    private boolean mIsUpdatingLocation;
    private boolean mIsListeningLocationUpdates;
    private float mDesiredAccuracy = MINIMUM_ACCURACY;
    private int mTimeoutDuration;
    private long mLastLocationElapsedTime;
    private Timer mTimer;
    private GoogleApiClient mApiClient;
    private LocationRequest mLocationRequest;
    private KBLocationCallback mCallback;

    private static Location kLocation;

    private static final String TAG = "LocationProvider";
    private static final long GPS_UPDATE_INTERVAL = 5000;
    private static final long UPDATE_POSITION_INTERVAL = 10000;
    private static final long FASTEST_UPDATE_POSITION_INTERVAL = UPDATE_POSITION_INTERVAL / 2;
    private static final float MINIMUM_ACCURACY = 50.0f;
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final int REQUEST_CODE_LOCATION = 2;

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

    public boolean checkPermissions(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return initializeLocation(activity);
            } else {
                if (mCallback != null) {
                    mCallback.onLocationRefused();
                }
            }
        }
        return false;
    }

    private boolean initializeLocation(Activity activity) {
        boolean isPermissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!isPermissionGranted) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            mApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(UPDATE_POSITION_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_POSITION_INTERVAL);

//            LocationManager locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
//            locationManager.addGpsStatusListener(new GpsListener());
        }
        return isPermissionGranted;
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
    public void fetchLocation(Activity activity) {
        fetchLocation(activity, null);
    }

    @Background
    public void fetchLocation(Activity activity, KBLocationCallback callback) {
        if (initializeLocation(activity) && (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS)) {
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

    private boolean mResolvingError;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed");

        Activity activity = (Activity)mContext;
        if (!mResolvingError) {
            if (connectionResult.hasResolution()) {
                try {
                    mResolvingError = true;
                    connectionResult.startResolutionForResult(activity, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mApiClient.connect();
                }
            }
            else {
                // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, connectionResult.getErrorCode());
                mResolvingError = true;

                if (mCallback != null) {
                    mCallback.onLocationFailed();
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location changed");

        kLocation = location;

        mLastLocationElapsedTime = SystemClock.elapsedRealtime();

        if (!mIsListeningLocationUpdates && location.getAccuracy() < mDesiredAccuracy) {
            stopLocationUpdates();
        }

        if (mCallback != null) {
            mCallback.onLocationReceived(location);
        }
    }

    @UiThread
    void updateGpsStatus(String status) {
        Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
    }

    private class GpsListener implements GpsStatus.Listener {

        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    if (kLocation != null) {
                        isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationElapsedTime) < GPS_UPDATE_INTERVAL;
                    }

                    updateGpsStatus(isGPSFix ? "GPS fixed" : "GPS lost");

                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    isGPSFix = true;
                    updateGpsStatus("GPS first acquired");

                    break;
            }
        }

    }

}
