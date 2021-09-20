package com.xinwang.sharecost;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

/**
 * Created by xinwang on 12/16/17.
 */

public class TabbedActivity extends SingleFragmentActivity {
    private static final String EXTRA_EVENT_ID = "com.xinwang.sharecost.event_id";
    private static final String EXTRA_EVENT_TITLE = "com.xinwang.sharecost.event_title";

    @Override
    protected Fragment createFragment() {
        return TabbedFragment.newInstance(
                (UUID) getIntent().getSerializableExtra(EXTRA_EVENT_ID),
                getIntent().getStringExtra(EXTRA_EVENT_TITLE));
    }

    public static Intent createIntent(Context context, UUID eventId, String title) {
        Intent intent = new Intent(context, TabbedActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        intent.putExtra(EXTRA_EVENT_TITLE, title);
        return intent;
    }
}
