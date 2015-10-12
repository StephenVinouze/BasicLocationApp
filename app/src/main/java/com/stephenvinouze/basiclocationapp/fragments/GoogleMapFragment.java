package com.stephenvinouze.basiclocationapp.fragments;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.stephenvinouze.basiclocationapp.R;
import com.stephenvinouze.basiclocationapp.interfaces.MapInterface;
import com.stephenvinouze.basiclocationapp.location.KBLocationProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;

/**
 * Created by Stephen Vinouze on 11/10/2015.
 */
@EFragment
public class GoogleMapFragment extends SupportMapFragment implements MapInterface, OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    private boolean mIsReady;
    private boolean mIsFirstUpdate;
    private boolean mFollowUserLocation;

    @AfterViews
    void initViews() {
        getMapAsync(this);
    }

    public boolean isMapReady() {
        return mIsReady;
    }

    public void updateMap(Location location) {
        if (location != null && (mFollowUserLocation || mIsFirstUpdate)) {
            mIsFirstUpdate = false;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
            getMap().moveCamera(cameraUpdate);
        }
    }

    private void followLocation() {
        mFollowUserLocation = true;

        Location currentLocation = KBLocationProvider.getLocation();
        if (currentLocation != null) {
            CameraPosition cp = new CameraPosition.Builder()
                    .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .bearing(currentLocation.getBearing())
                    .tilt(0)
                    .zoom(16)
                    .build();
            getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mIsReady = true;
        mIsFirstUpdate = true;
        mFollowUserLocation = true;

        map.setMyLocationEnabled(true);
        map.setPadding(0, (int) getResources().getDimension(R.dimen.compass_margin_top), 0, 0);
        map.setOnCameraChangeListener(this);

        UiSettings mapSettings = map.getUiSettings();
        mapSettings.setMyLocationButtonEnabled(false);
        mapSettings.setCompassEnabled(true);

        updateMap(KBLocationProvider.getLocation());
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        mFollowUserLocation = false;
    }

    @Override
    public void onCenterMap() {
        followLocation();
    }

    @Override
    public void onMapTypeChanged(int mapType) {
        getMap().setMapType(mapType);
    }
}
