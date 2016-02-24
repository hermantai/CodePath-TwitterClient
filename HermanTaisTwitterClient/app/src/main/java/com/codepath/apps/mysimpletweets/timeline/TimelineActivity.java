package com.codepath.apps.mysimpletweets.timeline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.profile.ProfileActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity {
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.viewPager) ViewPager mViewPager;
    @Bind(R.id.tabStrip) PagerSlidingTabStrip mTabStrip;

    private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Common.INFO_TAG, "Network changed: " + intent);
            if (mNetworkChangeListener != null) {
                mNetworkChangeListener.onNetworkChange(context, intent);
            }
        }
    };

    private NetworkChangeListener mNetworkChangeListener;
    private ToolbarClickListener mToolbarClickListener;
    private FloatingActionButtonClickListener mFloatingActionButtonClickListener;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkChangeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetworkChangeReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mToolbarClickListener != null) {
                    mToolbarClickListener.onToolbarClicked(v);
                }
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFloatingActionButtonClickListener != null) {
                    mFloatingActionButtonClickListener.onFloatingActionButtonClicked(view);
                }
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new TweetsPagerAdapter(fm));
        mTabStrip.setViewPager(mViewPager);

        // TODO: need to do something similar to below to set the listeners
        // TimelineFragment frag = new MentionsTimelineFragment(); //new HomeTimelineFragment();
        // mNetworkChangeListener = frag;
        // mToolbarClickListener = frag;
        // mFloatingActionButtonClickListener = frag;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch(itemId) {
            case R.id.miProfile:
                Intent i = ProfileActivity.newIntent(this);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = {"Home", "Mentions"};

        public TweetsPagerAdapter(FragmentManager fm ) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new HomeTimelineFragment();
            } else if (position == 1){
                return new MentionsTimelineFragment();
            } else {
                throw new RuntimeException("Impossible position: " + position);
            }
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
