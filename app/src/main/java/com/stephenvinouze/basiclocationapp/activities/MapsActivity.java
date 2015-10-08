package com.stephenvinouze.basiclocationapp.activities;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.stephenvinouze.basiclocationapp.R;
import com.stephenvinouze.basiclocationapp.location.KBLocationCallback;
import com.stephenvinouze.basiclocationapp.location.KBLocationProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_maps)
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    @Bean
    KBLocationProvider mLocationProvider;

    private SupportMapFragment mMapFragment;

    @AfterViews
    void initViews() {
        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);

        getSupportFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
    }

    private void configureMap(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
            mMapFragment.getMap().moveCamera(cameraUpdate);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.setPadding(16, 56, 16, 16);

        UiSettings mapSettings = map.getUiSettings();
        mapSettings.setMyLocationButtonEnabled(true);
        mapSettings.setCompassEnabled(true);
        mapSettings.setMyLocationButtonEnabled(false);

        Location currentLocation = KBLocationProvider.getLocation();
        if (currentLocation != null) {
            configureMap(currentLocation);
        }
        else {
            mLocationProvider.setIsListeningLocationUpdates(true);
            mLocationProvider.fetchLocation(new KBLocationCallback() {
                @Override
                public void onLocationReceived(Location location) {
                    if (mMapFragment.isAdded()) {
                        configureMap(location);
                    }
                }

                @Override
                public void onLocationFailed() {
                    Toast.makeText(MapsActivity.this, "Failed to fetch current location", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
