package com.stephenvinouze.basiclocationapp.fragments;

import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.stephenvinouze.basiclocationapp.R;
import com.stephenvinouze.basiclocationapp.location.KBLocationProvider;
import com.stephenvinouze.basiclocationapp.spans.KBTypefaceSpan;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Stephen Vinouze on 09/10/2015.
 */
@EFragment(R.layout.location_layout)
public class LocationFragment extends Fragment {

    @Bean
    KBLocationProvider mLocationProvider;

    @ViewById(R.id.location_latitude_view)
    TextView mLatitudeTextView;

    @ViewById(R.id.location_longitude_view)
    TextView mLongitudeTextView;

    @ViewById(R.id.location_accuracy_view)
    TextView mAccuracyTextView;

    @ViewById(R.id.location_city_view)
    TextView mCityTextView;

    private final static String UNKNOWN_LOCATION = "--";

    @AfterViews
    void initViews() {
        Location currentLocation = KBLocationProvider.getLocation();

        boolean isLocationAvailable = (currentLocation != null);

        String latitudeResult = getString(R.string.location_latitude, isLocationAvailable ? String.format("%f", currentLocation.getLatitude()) : UNKNOWN_LOCATION);
        String longitudeResult = getString(R.string.location_latitude, isLocationAvailable ? String.format("%f", currentLocation.getLongitude()) : UNKNOWN_LOCATION);
        String accuracyResult = getString(R.string.location_latitude, isLocationAvailable ? String.format("%f", currentLocation.getAccuracy()) : UNKNOWN_LOCATION);
        String cityResult = getString(R.string.location_latitude, isLocationAvailable ? KBLocationProvider.getCity(getActivity()) : UNKNOWN_LOCATION);

        updateLocationTextView(mLatitudeTextView, getString(R.string.icon_location), latitudeResult);
        updateLocationTextView(mLongitudeTextView, getString(R.string.icon_location), longitudeResult);
        updateLocationTextView(mAccuracyTextView, getString(R.string.icon_target), accuracyResult);
        updateLocationTextView(mCityTextView, getString(R.string.icon_map), cityResult);
    }

    private TextView updateLocationTextView(TextView textView, String icon, String text) {
        SpannableString legend = new SpannableString(icon + "   " + text);
        legend.setSpan(new KBTypefaceSpan(getActivity(), "basiclocationfont"),
                0, icon.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        legend.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(), R.color.colorPrimary)),
                0, icon.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(legend);

        return textView;
    }
}
