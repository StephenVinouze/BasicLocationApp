package com.stephenvinouze.basiclocationapp.activities;

import android.content.Intent;
import android.location.Location;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

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
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.map_activity)
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Bean
    KBLocationProvider mLocationProvider;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;

    @ViewById(R.id.navigation_view)
    NavigationView mNavigationView;

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
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                mNavigationDrawer.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id. menu_map_item:
                        menuItem.setChecked(true);
                        break;

                    case R.id. menu_location_item:
                        startActivity(new Intent(MapActivity.this, LocationActivity_.class));

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
        if (location != null) {
            CameraPosition cp = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .bearing(location.getBearing())
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
        map.setMyLocationEnabled(true);
        map.setPadding(0, (int) getResources().getDimension(R.dimen.compass_margin_top), 0, 0);

        UiSettings mapSettings = map.getUiSettings();
        mapSettings.setMyLocationButtonEnabled(false);
        mapSettings.setCompassEnabled(true);

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
                    Toast.makeText(MapActivity.this, getString(R.string.location_error), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
