package com.stephenvinouze.basiclocationapp.fragments;

import android.support.v4.app.Fragment;

import com.stephenvinouze.basiclocationapp.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * Created by Stephen Vinouze on 10/10/2015.
 */
@EFragment(R.layout.osm_layout)
public class OpenStreetMapFragment extends Fragment {

    @ViewById(R.id.mapview)
    MapView mMapView;

    private MyLocationNewOverlay mOverlay;

    @AfterViews
    void initViews() {
        mMapView.getController().setZoom(20);
        mOverlay = new MyLocationNewOverlay(getActivity(), mMapView);
        mMapView.getOverlays().add(mOverlay);
    }

    @Override
    public void onResume() {
        super.onResume();

        mOverlay.enableMyLocation();
        mOverlay.enableFollowLocation();
    }

    @Override
    public void onPause() {
        super.onPause();

        mOverlay.disableMyLocation();
        mOverlay.disableFollowLocation();
    }
}
