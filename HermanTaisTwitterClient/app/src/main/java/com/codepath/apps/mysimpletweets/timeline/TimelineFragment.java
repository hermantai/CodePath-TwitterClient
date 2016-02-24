package com.codepath.apps.mysimpletweets.timeline;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.compose.ComposeFragment;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.helpers.NetworkUtil;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.codepath.apps.mysimpletweets.tweetdetail.TweetDetailActivity;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class TimelineFragment extends Fragment implements NetworkChangeListener,
        ToolbarClickListener, FloatingActionButtonClickListener, ComposeFragment.OnNewTweetHandler {
    private static final int REQUEST_DETAIL = 0;

    @Bind(R.id.rvTweets) RecyclerView rvTweets;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout mSwipeContainer;
    @Bind(R.id.pbLoading) ProgressBar mPbLoading;

    protected TwitterClient mClient;
    protected TweetsAdapter mTweetsAdapter;
    protected EndlessRecyclerViewScrollListener mEndlessRecyclerViewScrollListener;

    protected boolean mStartedLoadingMore = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = SimpleTweetsApplication.getRestClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweets_list, container, false);
        ButterKnife.bind(this, v);

        Activity activity = getActivity();

        mTweetsAdapter = new TweetsAdapter(activity);
        int itemCount = mTweetsAdapter.getItemCount();
        if (itemCount != 0) {
            SimpleTweetsPrefs.setNewestHomeFetchedId(
                    activity, mTweetsAdapter.getItem(itemCount - 1).getUid());
        }
        rvTweets.setAdapter(mTweetsAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTweets.setLayoutManager(layoutManager);

        mEndlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            protected void onLoadMore(int page, int totalItemsCount) {
                int last = mTweetsAdapter.getItemCount() - 1;
                if (last >= 0) {
                    // only load more if we have some existing items
                    long max_id = mTweetsAdapter.getItem(last).getUid();
                    Log.d(Common.INFO_TAG, "Fetch older tweets with max_id: " + max_id);
                    mStartedLoadingMore = true;
                    fetchOlderTweets(max_id);
                } else {
                    mEndlessRecyclerViewScrollListener.notifyLoadMoreFailed();
                }
            }
        };
        rvTweets.addOnScrollListener(mEndlessRecyclerViewScrollListener);

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewerTweets();
                refreshUser();
            }
        });
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (NetworkUtil.isNetworkAvailable(activity)) {
            fetchNewerTweets();
            refreshUser();
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_DETAIL && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Tweet updatedTweet = TweetDetailActivity.getUpdatedTweet(data);
                int position = TweetDetailActivity.getUpdatedTweetPosition(data);
                Log.d(
                        Common.INFO_TAG,
                        String.format(
                                "Updated tweet: %s, updated pos: %d",
                                updatedTweet,
                                position));
                mTweetsAdapter.updateTweetAtPos(position, updatedTweet);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNetworkChange(Context context, Intent intent) {
        if (mEndlessRecyclerViewScrollListener != null) {
            mEndlessRecyclerViewScrollListener.enableLoadMore(
                    NetworkUtil.isNetworkAvailable(context));
        }
    }

    @Override
    public void onToolbarClicked(View v) {
        rvTweets.smoothScrollToPosition(0);
    }

    @Override
    public void onFloatingActionButtonClicked(View v) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        User user = SimpleTweetsPrefs.getUser(activity);
        if (user == null) {
            Toast.makeText(
                    getActivity(),
                    "User info does not exist, please pull down to reload",
                    Toast.LENGTH_LONG)
                    .show();
        }
        ComposeFragment frag = ComposeFragment.newInstance(user);
        frag.setOnNewTweetHandler(this);

        FragmentManager fm = activity.getSupportFragmentManager();
        frag.show(fm, "Compose");
    }

    @Override
    public void onNewTweet(Tweet newTweet) {
        mTweetsAdapter.addToFront(newTweet);
        rvTweets.smoothScrollToPosition(0);
    }

    /**
     * Send an API request to get new tweets from the timeline json
     */
    protected abstract void fetchNewerTweets();

    /**
     * Send an API request to get old tweets from the timeline json
     *
     * @param max_id The ID of the newest tweets to retrieve, exclusive.
     */
    protected abstract void fetchOlderTweets(final long max_id);

    /**
     * Send an API request to get old tweets from the timeline json to fill the gap
     *
     * @param tweetWithGapEarlier the tweet that has a potential gap between this tweet and the
     *                            tweet before this tweet (by tweet id, Uid)
     * @param since_id The ID of the tweet that is right before tweetWithGapEarlier
     */
    protected abstract void fetchOlderTweetsForTimelineGap(
            final TweetInterface tweetWithGapEarlier,
            final long since_id);

    private void refreshUser() {
        mClient.getCurrentUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                SimpleTweetsPrefs.setUser(getActivity(), User.fromJson(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                ErrorHandling.handleError(
                        getActivity(),
                        Common.INFO_TAG,
                        "Error loading newest user info: " + throwable.getLocalizedMessage(),
                        throwable);
                LogUtil.d(Common.INFO_TAG, responseString);
            }

            @Override
            public void onFailure(
                    int statusCode, Header[] headers, Throwable throwable, JSONObject
                    errorResponse) {
                ErrorHandling.handleError(
                        getActivity(),
                        Common.INFO_TAG,
                        "Error retrieving newest user info: " + throwable.getLocalizedMessage(),
                        throwable);
                showSnackBarForNetworkError(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshUser();
                    }
                });
            }
        });
    }

    protected void showSnackBarForNetworkError(View.OnClickListener listener) {
        if(!NetworkUtil.isNetworkAvailable(getActivity())) {
            Snackbar.make(
                    rvTweets,
                    "Network error!",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload", listener)
                    .setActionTextColor(Color.YELLOW)
                    .show();
        }
    }

    protected abstract Cursor fetchTweetsCursor();

    protected abstract String getNewestFetchedIdFieldInPrefs();

    class TweetsAdapter extends RecyclerView.Adapter<TweetViewHolder> {
        private Cursor mCursor;
        private Context mContext;

        public TweetsAdapter(Context context) {
            mCursor = TimelineFragment.this.fetchTweetsCursor();
            mContext = context;
        }

        @Override
        public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_tweet, parent, false);

            TweetViewHolder vh = new TweetViewHolder(
                    v,
                    new TweetViewHolder.ShowMoreOnClickListener() {
                        @Override
                        public void onClick(TweetInterface tweet, long prevTweetId) {
                            TimelineFragment.this.fetchOlderTweetsForTimelineGap(tweet,
                                    prevTweetId);
                        }
                    },
                    new TweetViewHolder.TweetUpdater() {
                        @Override
                        public void updateTweet(TweetInterface tweet) {
                            TweetsAdapter.this.updateTweet(tweet);
                        }
                    },
                    new TweetViewHolder.TweetOnClickListener(){
                        @Override
                        public void onClick(int position, TweetInterface tweet) {
                            Intent i = TweetDetailActivity.newIntent(
                                    mContext, position, tweet, getNewestFetchedIdFieldInPrefs());
                            startActivityForResult(i, REQUEST_DETAIL);
                        }
                    });
            return vh;
        }

        @Override
        public void onBindViewHolder(TweetViewHolder holder, int position) {
            TweetInterface tweet = getItem(position);;
            holder.bindTweet(mContext, position, tweet);
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }

        public void appendAll(List<TweetInterface> tweets) {
            int oldLen = getItemCount();
            if (!tweets.isEmpty()) {
                tweets.get(0).saveAll(tweets);
            }

            mCursor = TimelineFragment.this.fetchTweetsCursor();
            notifyItemRangeInserted(oldLen, tweets.size());
        }

        public void addAll(List<TweetInterface> tweets) {
            if (!tweets.isEmpty()) {
                tweets.get(0).saveAll(tweets);
            }
            mCursor = TimelineFragment.this.fetchTweetsCursor();
            notifyDataSetChanged();
        }

        public void addToFront(TweetInterface tweet) {
            tweet.saveOne();
            mCursor = TimelineFragment.this.fetchTweetsCursor();
            notifyItemInserted(0);
        }

        public void addAllToFront(List<TweetInterface> tweets) {
            if (!tweets.isEmpty()) {
                tweets.get(0).saveAll(tweets);
            }

            mCursor = TimelineFragment.this.fetchTweetsCursor();
            notifyDataSetChanged();
        }

        public TweetInterface getItem(int position) {
            mCursor.moveToPosition(position);
            TweetInterface tweet = new Tweet();
            tweet.loadFromCursor(mCursor);
            return tweet;
        }

        public void updateTweetAtPos(int position, Tweet tweet) {
            tweet.save();
            mCursor = TimelineFragment.this.fetchTweetsCursor();
            notifyItemChanged(position);
        }

        public void updateTweet(TweetInterface tweet) {
            tweet.saveOne();
            int p = mCursor.getPosition();
            if (getItem(p).getUid() == tweet.getUid()) {
                // This is the most common case: the tweet to be updated is the one we just returned
                mCursor = TimelineFragment.this.fetchTweetsCursor();
                notifyItemChanged(p);
            } else {
                // We don't know where this tweet is, so just update everything
                mCursor = TimelineFragment.this.fetchTweetsCursor();
                notifyDataSetChanged();
            }
        }
    }
}
