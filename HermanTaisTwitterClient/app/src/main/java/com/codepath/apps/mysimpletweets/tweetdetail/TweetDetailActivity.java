package com.codepath.apps.mysimpletweets.tweetdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Tweet;

public class TweetDetailActivity extends SingleFragmentActivity {
    private static final String EXTRA_TWEET = "com.codepath.apps.mysimpletweets.tweet";

    public static Intent newIntent(Context context, Tweet tweet) {
        Intent i = new Intent(context, TweetDetailActivity.class);
        i.putExtra(EXTRA_TWEET, tweet);

        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tbActivityFragment);
        setSupportActionBar(toolbar);
    }

    @Override
    protected Fragment createFragment() {
        return TweetDetailFragment.newInstance((Tweet) getIntent().getParcelableExtra(EXTRA_TWEET));
    }
}
