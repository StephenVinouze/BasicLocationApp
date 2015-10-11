package com.stephenvinouze.basiclocationapp.activities;

import android.content.Intent;
import android.location.Location;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.stephenvinouze.basiclocationapp.R;
import com.stephenvinouze.basiclocationapp.fragments.GoogleMapFragment;
import com.stephenvinouze.basiclocationapp.fragments.GoogleMapFragment_;
import com.stephenvinouze.basiclocationapp.fragments.LocationFragment_;
import com.stephenvinouze.basiclocationapp.fragments.OpenStreetMapFragment;
import com.stephenvinouze.basiclocationapp.fragments.OpenStreetMapFragment_;
import com.stephenvinouze.basiclocationapp.interfaces.IMapListener;
import com.stephenvinouze.basiclocationapp.location.KBLocationCallback;
import com.stephenvinouze.basiclocationapp.location.KBLocationProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.map_activity)
public class MapActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Bean
    KBLocationProvider mLocationProvider;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;

    @ViewById(R.id.navigation_view)
    NavigationView mNavigationView;

    @ViewById(R.id.left_panel_container)
    FrameLayout mLeftPanelContainer;

    private boolean mSatelliteChecked;
    private boolean mTerrainChecked;
    private IMapListener mMapListener;
    private GoogleMapFragment mGoogleMapFragment;
    private OpenStreetMapFragment mOpenStreetMapFragment;

    @AfterViews
    void initViews() {
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mNavigationDrawer, mToolbar, R.string.app_name, R.string.app_name);
        drawerToggle.syncState();

        mNavigationDrawer.setDrawerListener(drawerToggle);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mNavigationDrawer.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.menu_satellite_item:
                        mTerrainChecked = false;
                        mSatelliteChecked = !mSatelliteChecked;
                        menuItem.setChecked(mSatelliteChecked);
                        mMapListener.onMapTypeChanged(mSatelliteChecked ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
                        break;

                    case R.id.menu_terrain_item:
                        mSatelliteChecked = false;
                        mTerrainChecked = !mTerrainChecked;
                        menuItem.setChecked(mTerrainChecked);
                        mMapListener.onMapTypeChanged(mTerrainChecked ? GoogleMap.MAP_TYPE_TERRAIN : GoogleMap.MAP_TYPE_NORMAL);
                        break;

                    case R.id.menu_location_item:
                        if (mLeftPanelContainer == null) {
                            startActivity(new Intent(MapActivity.this, LocationActivity_.class));
                        }
                        else {
                            displayLeftFragment(LocationFragment_.builder().build());
                        }

                        break;
                }

                return true;
            }
        });

        mLocationProvider.setIsListeningLocationUpdates(true);
        mLocationProvider.fetchLocation(this, new KBLocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (mGoogleMapFragment.isAdded() && mGoogleMapFragment.isMapReady()) {
                    mGoogleMapFragment.updateMap(location);
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

        displayGoogleMap();
    }

    private void displayGoogleMap() {
        mMapListener = mGoogleMapFragment = GoogleMapFragment_.builder().build();
        getSupportFragmentManager().beginTransaction().replace(R.id.map_container, mGoogleMapFragment).commit();
    }

    private void displayOpenStreetMap() {
        mMapListener = mOpenStreetMapFragment = OpenStreetMapFragment_.builder().build();
        getSupportFragmentManager().beginTransaction().replace(R.id.map_container, mOpenStreetMapFragment).commit();
    }

    private void displayLeftFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.left_panel_container, fragment).commit();
    }

    @Click(R.id.map_locate_me_button)
    void onLocateMeClicked() {
        mMapListener.onCenterMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.google_map_action:
                displayGoogleMap();
                break;

            case R.id.osm_map_action:
                displayOpenStreetMap();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationProvider.checkPermissions(this, requestCode, grantResults);
    }

}
