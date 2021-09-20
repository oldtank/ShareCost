package com.xinwang.sharecost;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ShareCostActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return EventsFragment.newInstance();
    }
}
