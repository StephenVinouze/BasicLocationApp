package com.stephenvinouze.basiclocationapp.activities;

import android.content.Intent;
import android.location.Location;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.stephenvinouze.basiclocationapp.R;
import com.stephenvinouze.basiclocationapp.location.KBLocationCallback;
import com.stephenvinouze.basiclocationapp.location.KBLocationProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.map_activity)
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    @Bean
    KBLocationProvider mLocationProvider;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;

    @ViewById(R.id.navigation_view)
    NavigationView mNavigationView;

    private boolean mFollowUserLocation;
    private SupportMapFragment mMapFragment;

    @AfterViews
    void initViews() {
        setSupportActionBar(mToolbar);
        setTitle(null);

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mNavigationDrawer.closeDrawers();
                menuItem.setChecked(true);

                switch (menuItem.getItemId()) {
                    case R.id.menu_map_item:
                        mMapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;

                    case R.id.menu_location_item:
                        menuItem.setChecked(false);
                        startActivity(new Intent(MapActivity.this, LocationActivity_.class));
                        break;

                    case R.id.menu_satellite_item:
                        mMapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;

                    case R.id.menu_terrain_item:
                        mMapFragment.getMap().setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                }

                return true;
            }
        });

        mMapFragment = SupportMapFragment.newInstance();
        mMapFragment.getMapAsync(this);

        getSupportFragmentManager().beginTransaction().add(R.id.map_container, mMapFragment).commit();
    }

    private void configureMap(Location location) {
        if (location != null && mFollowUserLocation) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
            mMapFragment.getMap().moveCamera(cameraUpdate);
        }
    }

    @Click(R.id.map_locate_me_button)
    void onLocateMeClicked() {
        mFollowUserLocation = true;

        Location currentLocation = KBLocationProvider.getLocation();
        if (currentLocation != null) {
            CameraPosition cp = new CameraPosition.Builder()
                    .target(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .bearing(currentLocation.getBearing())
                    .tilt(0)
                    .zoom(16)
                    .build();
            mMapFragment.getMap().animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mNavigationDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mFollowUserLocation = true;

        map.setMyLocationEnabled(true);
        map.setPadding(0, (int) getResources().getDimension(R.dimen.compass_margin_top), 0, 0);
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (KBLocationProvider.getLocation() != null) {
                    mFollowUserLocation = false;
                }
            }
        });

        UiSettings mapSettings = map.getUiSettings();
        mapSettings.setMyLocationButtonEnabled(false);
        mapSettings.setCompassEnabled(true);

        Location currentLocation = KBLocationProvider.getLocation();
        if (currentLocation != null) {
            configureMap(currentLocation);
        }
        else {
            mLocationProvider.setIsListeningLocationUpdates(true);
            mLocationProvider.fetchLocation(this, new KBLocationCallback() {
                @Override
                public void onLocationReceived(Location location) {
                    if (mMapFragment.isAdded()) {
                        configureMap(location);
                    }
                }

                @Override
                public void onLocationFailed() {
                    Toast.makeText(MapActivity.this, getString(R.string.location_update_error), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLocationRefused() {
                    Toast.makeText(MapActivity.this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onGpsStatusChanged(KBLocationProvider.KBGpsStatus status) {
                    switch (status) {
                        case OK:
                            Toast.makeText(MapActivity.this, getString(R.string.gps_status_ok), Toast.LENGTH_SHORT).show();
                            break;

                        case KO:
                            Toast.makeText(MapActivity.this, getString(R.string.gps_status_ko), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mLocationProvider.checkPermissions(this, requestCode, grantResults);
    }
}
