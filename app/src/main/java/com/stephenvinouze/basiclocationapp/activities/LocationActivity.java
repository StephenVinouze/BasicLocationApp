package com.stephenvinouze.basiclocationapp.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.stephenvinouze.basiclocationapp.R;
import com.stephenvinouze.basiclocationapp.fragments.LocationFragment_;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Stephen Vinouze on 09/10/2015.
 */
@EActivity(R.layout.location_activity)
public class LocationActivity extends AppCompatActivity {

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @AfterViews
    void initViews() {
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager().beginTransaction().add(R.id.location_container, LocationFragment_.builder().build()).commit();
    }
}
