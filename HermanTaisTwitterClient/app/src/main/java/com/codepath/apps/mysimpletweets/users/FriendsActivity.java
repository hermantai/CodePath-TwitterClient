package com.codepath.apps.mysimpletweets.users;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.tweetdetail.SingleFragmentActivity;

public class FriendsActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, FriendsActivity.class);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return FriendsFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_friends));
    }
}
