package com.codepath.apps.mysimpletweets.timeline;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

public class SimpleSmartFragmentStatePagerAdapter extends SmartFragmentStatePagerAdapter {
    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    public SimpleSmartFragmentStatePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    public SimpleSmartFragmentStatePagerAdapter addFragment(Fragment fragment, String title) {
        mFragments.add(fragment);
        mTitles.add(title);

        return this;
    }
}
