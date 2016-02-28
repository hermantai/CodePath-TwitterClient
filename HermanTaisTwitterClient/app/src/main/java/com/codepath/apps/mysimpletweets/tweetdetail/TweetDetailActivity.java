package com.codepath.apps.mysimpletweets.tweetdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.TweetInterface;

public class TweetDetailActivity extends SingleFragmentActivity {
    private static final String EXTRA_TWEET = "com.codepath.apps.mysimpletweets.tweet";
    private static final String EXTRA_TWEET_POS = "com.codepath.apps.mysimpletweets.tweet_position";
    private static final String EXTRA_NEWEST_ID_FIELD_IN_PREFS =
            "com.codepath.apps.mysimpletweets.newest_id_field_in_prefs";

    public static Intent newIntent(
            Context context,
            int tweetPosition,
            TweetInterface tweet,
            String newestFetchedIdFieldInPrefs) {
        Intent i = new Intent(context, TweetDetailActivity.class);
        i.putExtra(EXTRA_TWEET, tweet);
        i.putExtra(EXTRA_TWEET_POS, tweetPosition);
        i.putExtra(EXTRA_NEWEST_ID_FIELD_IN_PREFS, newestFetchedIdFieldInPrefs);

        return i;
    }

    @Override
    protected Fragment createFragment() {
        return TweetDetailFragment.newInstance(
                getIntent().getIntExtra(EXTRA_TWEET_POS, 0),
                (Tweet) getIntent().getParcelableExtra(EXTRA_TWEET),
                getIntent().getStringExtra(EXTRA_NEWEST_ID_FIELD_IN_PREFS));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_tweet_detail));
    }

    public static Tweet getUpdatedTweet(Intent data) {
        return TweetDetailFragment.getUpdatedTweet(data);
    }

    public static int getUpdatedTweetPosition(Intent data) {
        return TweetDetailFragment.getUpdatedTweetPosition(data);
    }
}
