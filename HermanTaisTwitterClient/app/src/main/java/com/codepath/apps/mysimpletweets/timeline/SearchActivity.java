package com.codepath.apps.mysimpletweets.timeline;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.codepath.apps.mysimpletweets.tweetdetail.SingleFragmentActivity;

public class SearchActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search");
    }

    @Override
    protected Fragment createFragment() {
        return SearchFragment.newInstance();
    }
}
