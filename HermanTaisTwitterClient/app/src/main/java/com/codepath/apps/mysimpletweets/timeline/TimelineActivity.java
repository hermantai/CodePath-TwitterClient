package com.codepath.apps.mysimpletweets.timeline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity {
    @Bind(R.id.fab) FloatingActionButton mFab;

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

        TimelineFragment frag = new TweetsTimelineFragment();
        mNetworkChangeListener = frag;
        mToolbarClickListener = frag;
        mFloatingActionButtonClickListener = frag;

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, frag).commit();
    }
}
