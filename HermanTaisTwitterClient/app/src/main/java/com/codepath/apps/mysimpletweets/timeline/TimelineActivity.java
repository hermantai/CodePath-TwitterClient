package com.codepath.apps.mysimpletweets.timeline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.NetworkUtil;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.profile.ProfileActivity;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity {
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.viewPager) ViewPager mViewPager;
    @Bind(R.id.tabStrip) PagerSlidingTabStrip mTabStrip;

    protected TwitterClient mClient;

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
    private ViewPager.OnPageChangeListener mOnPageChangeListener;

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

        mClient = SimpleTweetsApplication.getRestClient();
        refreshUser();

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
        final TweetsPagerAdapter adapter = new TweetsPagerAdapter(fm);
        mViewPager.setAdapter(adapter);

        mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                    int position, float positionOffset, int positionOffsetPixels) {
                // do nothing
            }

            @Override
            public void onPageSelected(int position) {
                mFab.setVisibility(View.GONE);

                Fragment frag = adapter.getRegisteredFragment(position);
                if (frag instanceof HomeTimelineFragment) {
                    HomeTimelineFragment homeTimelineFragment = (HomeTimelineFragment) frag;
                    mFab.setVisibility(View.VISIBLE);

                    mNetworkChangeListener = homeTimelineFragment;
                    mToolbarClickListener = homeTimelineFragment;
                    mFloatingActionButtonClickListener = homeTimelineFragment;
                } else if (frag instanceof MentionsTimelineFragment) {
                    MentionsTimelineFragment mentionsTimelineFragment =
                            (MentionsTimelineFragment) frag;

                    mNetworkChangeListener = mentionsTimelineFragment;
                    mToolbarClickListener = mentionsTimelineFragment;
                    mFloatingActionButtonClickListener = mentionsTimelineFragment;
                } else {
                    throw new RuntimeException(
                            "Impossible fragment: " + frag + " at position " + position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        };

        mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        mTabStrip.setViewPager(mViewPager);

        // Trigger the ViewPager's OnPageChangeListener once at the beginning to set up the
        // UI right after the ViewPager is rendered.
        ViewTreeObserver observer = mViewPager.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mViewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                mOnPageChangeListener.onPageSelected(0);
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Have to let the ViewPager instantiates all the items after the creation of the view
        // and stuff before calling onPageSelected
        // mOnPageChangeListener.onPageSelected(0);
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
                Intent i = ProfileActivity.newIntent(this, SimpleTweetsPrefs.getUser(this));
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshUser() {
        mClient.getCurrentUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                SimpleTweetsPrefs.setUser(TimelineActivity.this, User.fromJson(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                ErrorHandling.handleError(
                        TimelineActivity.this,
                        Common.INFO_TAG,
                        "Error loading newest user info: " + throwable.getLocalizedMessage(),
                        throwable);
                showSnackBarForNetworkError(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshUser();
                    }
                });
            }

            @Override
            public void onFailure(
                    int statusCode, Header[] headers, Throwable throwable, JSONObject
                    errorResponse) {
                ErrorHandling.handleError(
                        TimelineActivity.this,
                        Common.INFO_TAG,
                        "Error retrieving newest user info: " + throwable.getLocalizedMessage(),
                        throwable);
                showSnackBarForNetworkError(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshUser();
                    }
                });
            }
        });
    }

    private void showSnackBarForNetworkError(View.OnClickListener listener) {
        if(!NetworkUtil.isNetworkAvailable(this)) {
            Snackbar.make(
                    mTabStrip,
                    "Network error when loading user!",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload", listener)
                    .setActionTextColor(Color.YELLOW)
                    .show();
        }
    }

    private class TweetsPagerAdapter extends SmartFragmentStatePagerAdapter {
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
