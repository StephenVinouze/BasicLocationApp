package com.stephenvinouze.basiclocationapp;

import android.Manifest;
import android.os.Build;

import com.stephenvinouze.basiclocationapp.activities.MapActivity_;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class MapActivityTest {

    private MapActivity_ mMapActivity;

    @Before
    public void setup() {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        mMapActivity = Robolectric.buildActivity(MapActivity_.class).create().get();
    }

    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(mMapActivity);
    }

//    @Test
//    public void clickingLocationMenu_shouldStartLocationActivity() throws Exception {
//        ShadowActivity shadowActivity = Shadows.shadowOf(mMapActivity);
//        shadowActivity.clickMenuItem(R.id.menu_location_item);
//
//        Intent startedIntent = shadowActivity.getNextStartedActivity();
//        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
//
//        assertThat(shadowIntent.getComponent().getClassName(), equalTo(LocationActivity_.class.getName()));
//    }

}