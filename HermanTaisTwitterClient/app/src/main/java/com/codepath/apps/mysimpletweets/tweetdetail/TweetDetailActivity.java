package com.codepath.apps.mysimpletweets.tweetdetail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tbActivityFragment);
        setSupportActionBar(toolbar);
    }

    @Override
    protected Fragment createFragment() {
        TweetDetailFragment.NewestFetchedIdProvider provider;

        String newestIdFieldInPrefs = getIntent().getStringExtra(EXTRA_NEWEST_ID_FIELD_IN_PREFS);

        if (newestIdFieldInPrefs.equals(SimpleTweetsPrefs.PREF_NEWEST_HOME_FETCHED_ID)) {
            provider = new TweetDetailFragment.NewestFetchedIdProvider() {
                @Override
                public long getNewestFetchedId(Context context) {
                    return SimpleTweetsPrefs.getNewestHomeFetchedId(context);
                }

                @Override
                public void setNewestFetchedId(Context context, long id) {
                    SimpleTweetsPrefs.setNewestHomeFetchedId(context, id);
                }
            };
        } else if(newestIdFieldInPrefs.equals(SimpleTweetsPrefs.PREF_NEWEST_MENTIONS_FETCHED_ID)) {
            provider = new TweetDetailFragment.NewestFetchedIdProvider() {
                @Override
                public long getNewestFetchedId(Context context) {
                    return SimpleTweetsPrefs.getNewestMentionsFetchedId(context);
                }

                @Override
                public void setNewestFetchedId(Context context, long id) {
                    SimpleTweetsPrefs.setNewestMentionsFetchedId(context, id);
                }
            };
        } else if(newestIdFieldInPrefs.equals(SimpleTweetsPrefs
                .PREF_NEWEST_USER_TIMELINE_FETCHED_ID)) {
            provider = new TweetDetailFragment.NewestFetchedIdProvider() {
                @Override
                public long getNewestFetchedId(Context context) {
                    return SimpleTweetsPrefs.getNewestUserTimelineFetchedId(context);
                }

                @Override
                public void setNewestFetchedId(Context context, long id) {
                    SimpleTweetsPrefs.setNewestUserTimelineFetchedId(context, id);
                }
            };
        } else {
            throw new RuntimeException("Impossible pref field: " + newestIdFieldInPrefs);
        }

        return TweetDetailFragment.newInstance(
                getIntent().getIntExtra(EXTRA_TWEET_POS, 0),
                (Tweet) getIntent().getParcelableExtra(EXTRA_TWEET),
                provider);
    }

    public static Tweet getUpdatedTweet(Intent data) {
        return TweetDetailFragment.getUpdatedTweet(data);
    }

    public static int getUpdatedTweetPosition(Intent data) {
        return TweetDetailFragment.getUpdatedTweetPosition(data);
    }
}
