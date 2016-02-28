package com.codepath.apps.mysimpletweets.users;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.codepath.apps.mysimpletweets.tweetdetail.SingleFragmentActivity;

public class FollowersActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, FollowersActivity.class);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return FollowersFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Followers");
    }
}
