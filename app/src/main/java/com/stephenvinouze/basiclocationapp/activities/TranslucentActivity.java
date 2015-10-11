package com.stephenvinouze.basiclocationapp.activities;

import android.view.MenuItem;
import android.view.MotionEvent;

import org.androidannotations.annotations.EActivity;

/*
 * Created by stephenvinouze on 17/09/2015.
 */
@EActivity
public abstract class TranslucentActivity extends KBActivity {

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            finish();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

}
