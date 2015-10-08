package com.stephenvinouze.basiclocationapp.activities;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
        mMapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 15));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.setMyLocationEnabled(true);

        Location currentLocation = KBLocationProvider.getLocation();
        if (currentLocation != null) {
            configureMap(currentLocation);
        }
        else {
            mLocationProvider.fetchLocation(0, new KBLocationCallback() {
                @Override
                public void onLocationReceived(Location location) {
                    configureMap(location);
                }

                @Override
                public void onLocationFailed() {
                    Log.e("Map", "Failed to fetch current location");
                }
            });
        }
    }
}
