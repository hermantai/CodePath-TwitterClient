package com.codepath.apps.mysimpletweets.tweetdetail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Tweet;

public class TweetDetailFragment extends Fragment {
    private static final String ARG_TWEET = "tweet";

    public static TweetDetailFragment newInstance(Tweet tweet) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_TWEET, tweet);

        TweetDetailFragment frag = new TweetDetailFragment();
        frag.setArguments(bundle);

        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweet_detail, container, false);
        return v;
    }
}
