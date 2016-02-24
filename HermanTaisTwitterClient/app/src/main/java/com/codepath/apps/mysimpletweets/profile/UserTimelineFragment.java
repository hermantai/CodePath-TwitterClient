package com.codepath.apps.mysimpletweets.profile;

import android.database.Cursor;

import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.timeline.TimelineFragment;

public class UserTimelineFragment extends TimelineFragment {
    @Override
    protected void fetchNewerTweets() {

    }

    @Override
    protected void fetchOlderTweets(long max_id) {

    }

    @Override
    protected void fetchOlderTweetsForTimelineGap(
            TweetInterface tweetWithGapEarlier, long since_id) {

    }

    @Override
    protected Cursor fetchTweetsCursor() {
        return null;
    }
}
