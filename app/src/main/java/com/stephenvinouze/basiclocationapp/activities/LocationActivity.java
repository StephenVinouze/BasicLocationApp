package com.stephenvinouze.basiclocationapp.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.stephenvinouze.basiclocationapp.R;
import com.stephenvinouze.basiclocationapp.location.KBLocationProvider;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Stephen Vinouze on 09/10/2015.
 */
@EActivity(R.layout.location_activity)
public class LocationActivity extends AppCompatActivity {

    @Bean
    KBLocationProvider mLocationProvider;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @AfterViews
    void initViews() {
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
