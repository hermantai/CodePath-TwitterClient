package com.codepath.apps.mysimpletweets.timeline;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.helpers.StringUtil;
import com.codepath.apps.mysimpletweets.models.Media;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.profile.ProfileActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TweetViewHolder extends RecyclerView.ViewHolder implements View
        .OnClickListener {

    public interface ShowMoreOnClickListener {
        void onClick(TweetInterface tweet, long prevTweetId);
    }

    public interface TweetUpdater {
        void updateTweet(TweetInterface tweet);
    }

    public interface TweetOnClickListener {
        void onClick(int position, TweetInterface tweet);
    }

    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat(
            "E MMM dd HH:mm:ss Z yyyy");

    @Bind(R.id.ivItemTweetProfileImage) ImageView mIvItemTweetProfileImage;
    @Bind(R.id.tvItemTweetUserName) TextView mTvItemTweetUserName;
    @Bind(R.id.tvItemTweetUserScreenName) TextView mTvItemTweetUserScreenName;
    @Bind(R.id.tvItemTweetBody) TextView mTvItemTweetBody;
    @Bind(R.id.tvItemTweetCreatedAt) TextView mTvItemTweetCreatedAt;
    @Bind(R.id.btnItemTweetShowMore) Button mBtnItemTweetShowMore;

    private ShowMoreOnClickListener mShowMoreOnClickListener;
    private TweetOnClickListener mTweetOnClickListener;

    View.OnClickListener mTvItemTweetCreatedAtOnClickListener1 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTvItemTweetCreatedAt.setText(sDateFormat.format(mTweet.getCreatedAt()));
            mTvItemTweetCreatedAt.setBackgroundResource(
                    R.drawable.rectangle_background_timestamp);
            mTvItemTweetCreatedAt.setOnClickListener(mTvItemTweetCreatedAtOnClickListener2);
            // To yield space for the time stamp
            mTvItemTweetUserScreenName.setText("");
        }
    };

    View.OnClickListener mTvItemTweetCreatedAtOnClickListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTvItemTweetCreatedAt.setText(
                    DateUtils.getRelativeTimeSpanString(mTweet.getCreatedAt().getTime()));
            mTvItemTweetCreatedAt.setBackgroundResource(0);
            mTvItemTweetCreatedAt.setOnClickListener(mTvItemTweetCreatedAtOnClickListener1);
            mTvItemTweetUserScreenName.setText("@" + mTweet.getUser().getScreenName());
        }
    };

    private TweetInterface mTweet;
    private int mTweetPos;
    private TweetUpdater mTweetUpdater;

    public TweetViewHolder(
            View itemView,
            ShowMoreOnClickListener showMoreOnClickListener,
            TweetUpdater tweetUpdater,
            TweetOnClickListener tweetOnClickListener) {
        super(itemView);
        mShowMoreOnClickListener = showMoreOnClickListener;
        mTweetUpdater = tweetUpdater;
        mTweetOnClickListener = tweetOnClickListener;

        ButterKnife.bind(this, itemView);

        itemView.setOnClickListener(this);
        mTvItemTweetBody.setOnClickListener(this);
    }

    public void bindTweet(final Activity activity, int tweetPosition, TweetInterface tweet) {
        mTweet = tweet;
        mTweetPos = tweetPosition;

        mTvItemTweetUserName.setText(tweet.getUser().getName());
        mTvItemTweetUserScreenName.setText("@" + tweet.getUser().getScreenName());

        // somehow twitter adds the url to the text...
        if (mTweet.getExtendedEntities() != null) {
            List<String> urlsToBeRemoved = new ArrayList<>();
            for (Media media : mTweet.getExtendedEntities().getMedia()) {
                urlsToBeRemoved.add(media.getUrl());
            }

            if (!urlsToBeRemoved.isEmpty()) {
                String s = tweet.getText();
                for (String toBeRemoved : urlsToBeRemoved) {
                    s = s.replace(toBeRemoved, "");
                }
                mTvItemTweetBody.setText(s);
            }
        } else {
            mTvItemTweetBody.setText(tweet.getText());
        }

        mTvItemTweetCreatedAt.setText(
                StringUtil.getRelativeTimeSpanString(tweet.getCreatedAt()));

        mIvItemTweetProfileImage.setImageResource(android.R.color.transparent);
        Glide.with(activity)
                .load(tweet.getUser().getProfileImageUrl())
                .into(mIvItemTweetProfileImage);
        mIvItemTweetProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ProfileActivity.newIntent(activity, mTweet.getUser());
                activity.startActivity(i);
            }
        });

        mTvItemTweetCreatedAt.setOnClickListener(mTvItemTweetCreatedAtOnClickListener1);

        if (mShowMoreOnClickListener != null) {
            if (mTweet.isHasMoreBefore()) {
                Log.d(Common.INFO_TAG, "mTweet that has before: " + mTweet);
                final TweetInterface prevTweet = mTweet.fetchTweetBefore();
                if (prevTweet == null) {
                    // Very unlikely to happen
                    mBtnItemTweetShowMore.setVisibility(View.GONE);
                    mTweet.setHasMoreBefore(false);
                    mTweetUpdater.updateTweet(mTweet);
                } else {
                    mBtnItemTweetShowMore.setVisibility(View.VISIBLE);
                    mBtnItemTweetShowMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mTweetUpdater != null) {
                                mShowMoreOnClickListener.onClick(mTweet, prevTweet.getUid());
                            }
                        }
                    });
                }
            } else {
                mBtnItemTweetShowMore.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mTweetOnClickListener != null) {
            mTweetOnClickListener.onClick(mTweetPos, mTweet);
        }
    }
}