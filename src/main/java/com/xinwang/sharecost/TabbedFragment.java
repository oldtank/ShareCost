package com.xinwang.sharecost;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

/**
 * Created by xinwang on 12/16/17.
 */

public class TabbedFragment extends Fragment {
    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_EVENT_TITLE = "event_title";

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private Toolbar titleToolbar;

    private UUID eventId;
    private String eventTitle;

    public static TabbedFragment newInstance(UUID eventId, String eventTitle) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_TITLE, eventTitle);

        TabbedFragment fragment = new TabbedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.eventId = (UUID) getArguments().getSerializable(ARG_EVENT_ID);
        this.eventTitle = getArguments().getString(ARG_EVENT_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tabbed, container, false);

        sectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        viewPager = v.findViewById(R.id.view_container);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ((SectionsPagerAdapter) viewPager.getAdapter()).onPageSelected(position);
//                SectionsPagerAdapter adapter =  viewPager.getAdapter();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        titleToolbar = v.findViewById(R.id.title_toolbar);
        titleToolbar.setTitle(eventTitle);

        TabLayout tabLayout = v.findViewById(R.id.tabs);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        return v;
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private PeopleTabFragment peopleTabFragment;
        private BillsTabFragment billsTabFragment;
        private ResultsTabFragment resultsTabFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            peopleTabFragment = PeopleTabFragment.newInstance(eventId);
            billsTabFragment = BillsTabFragment.newInstance(eventId);
            resultsTabFragment = ResultsTabFragment.newInstance(eventId, eventTitle);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return peopleTabFragment;
            } else if (position == 1) {
                return billsTabFragment;
            } else {
                return resultsTabFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        public void onPageSelected(int position) {
            if (position == 1) {
                billsTabFragment.updateUI();
            } else if (position == 0) {
                peopleTabFragment.updateUI();
            } else if (position == 2) {
                resultsTabFragment.updateUI();
            }
        }

    }
}
