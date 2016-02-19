package com.codepath.apps.mysimpletweets.timeline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.compose.ComposeFragment;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.helpers.NetworkUtil;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.tweetdetail.TweetDetailActivity;
import com.codepath.apps.mysimpletweets.twitter.TwitterApplication;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity
        implements ComposeFragment.OnNewTweetHandler{
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat(
            "E MMM dd HH:mm:ss Z yyyy");

    @Bind(R.id.rvTweets) RecyclerView rvTweets;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.swipeContainer) SwipeRefreshLayout mSwipeContainer;

    private TwitterClient mClient;
    private TweetsAdapter mTweetsAdapter;
    private EndlessRecyclerViewScrollListener mEndlessRecyclerViewScrollListener;
    private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Common.INFO_TAG, "Network changed: " + intent);
            if (mEndlessRecyclerViewScrollListener != null) {
                mEndlessRecyclerViewScrollListener.enableLoadMore(
                        NetworkUtil.isNetworkAvailable(TimelineActivity.this));
            }
        }
    };

    private boolean mStartedLoadingMore = false;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkChangeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mNetworkChangeReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        ButterKnife.bind(this);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //layoutManager.scrollToPositionWithOffset(0, 0);
                rvTweets.smoothScrollToPosition(0);
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComposeFragment frag = ComposeFragment.newInstance();
                frag.setOnNewTweetHandler(TimelineActivity.this);

                FragmentManager fm = getSupportFragmentManager();
                frag.show(fm, "Compose");
            }
        });

        mTweetsAdapter = new TweetsAdapter();
        rvTweets.setAdapter(mTweetsAdapter);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTweets.setLayoutManager(layoutManager);

        mEndlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            protected void onLoadMore(int page, int totalItemsCount) {
                int last = mTweetsAdapter.getItemCount() - 1;
                if (last >= 0) {
                    long max_id = mTweetsAdapter.getItem(last).getId();
                    Log.d(Common.INFO_TAG, "Fetch older tweets with max_id: " + max_id);
                    mStartedLoadingMore = true;
                    fetchOlderTweets(max_id);
                }
            }
        };
        rvTweets.addOnScrollListener(mEndlessRecyclerViewScrollListener);

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNewerTweets(0);
            }
        });
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mClient = TwitterApplication.getRestClient();
        fetchNewerTweets(0);
    }

    /**
     * Send an API request to get new tweets from the timeline json
     * Fill the list by creating the tweet objects from the json
     * @param since_id The ID of the oldest tweets to retrieve, exclusive. Use 0 if getting the
     *                 newest tweets.
     */
    private void fetchNewerTweets(final long since_id) {
        mClient.getHomeTimeline(
                25,
                since_id,
                0,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        LogUtil.d(Common.INFO_TAG, response.toString());
                        mSwipeContainer.setRefreshing(false);

                        // Deserialize JSON
                        // Create models
                        // Load the model data into ListView
                        List<Tweet> newTweets = Tweet.fromJsonArray(response);
                        // TODO: handle gaps on the timeline created from this
                        if (!newTweets.isEmpty()
                                && mTweetsAdapter.getItemCount() != 0
                                // The oldest new tweet is earlier than or equal to newest tweet
                                // we have so far, in terms of uid of tweets
                                && newTweets.get(newTweets.size() - 1).getId() >=
                                        mTweetsAdapter.getItem(0).getId()) {
                            // Overlapped with what we have
                            long newestExistingId = mTweetsAdapter.getItem(0).getId();

                            int oldestNewItemToBeInserted = newTweets.size() - 1;
                            for (oldestNewItemToBeInserted -= 1;
                                 oldestNewItemToBeInserted >= 0;
                                 oldestNewItemToBeInserted--) {
                                if (
                                        newTweets.get(oldestNewItemToBeInserted)
                                                .getId() > newestExistingId) {
                                    break;
                                }
                            }
                            if (oldestNewItemToBeInserted >= 0) {
                                mTweetsAdapter.addAllToFront(
                                        newTweets.subList(0, oldestNewItemToBeInserted + 1));
                            }
                        } else {
                            mTweetsAdapter.addAllToFront(newTweets);
                        }
                    }

                    @Override
                    public void onFailure(
                            int statusCode,
                            Header[] headers,
                            Throwable throwable,
                            JSONObject errorResponse) {
                        mSwipeContainer.setRefreshing(false);

                        ErrorHandling.handleError(
                                TimelineActivity.this,
                                Common.INFO_TAG,
                                "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                        if (!NetworkUtil.isNetworkAvailable(TimelineActivity.this)
                                || throwable instanceof UnknownHostException) {
                            Snackbar.make(
                                    rvTweets,
                                    "Network error!",
                                    Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Reload", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchNewerTweets(since_id);
                                        }
                                    })
                                    .setActionTextColor(Color.WHITE)
                                    .show();
                        }
                    }
                });
    }

    /**
     * Send an API request to get old tweets from the timeline json
     * Fill the list by creating the tweet objects from the json
     *
     * @param max_id The ID of the newest tweets to retrieve, exclusive.
     */
    private void fetchOlderTweets(final long max_id) {
        mClient.getHomeTimeline(
                25,
                0,
                max_id,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                        LogUtil.d(Common.INFO_TAG, response.toString());

                        // Deserialize JSON
                        // Create models
                        // Load the model data into ListView
                        List<Tweet> newTweets = Tweet.fromJsonArray(response);
                        mTweetsAdapter.addAll(newTweets);

                        if (mStartedLoadingMore) {
                            mStartedLoadingMore = false;
                            if (newTweets.isEmpty()
                                    && mEndlessRecyclerViewScrollListener != null) {
                                mEndlessRecyclerViewScrollListener.notifyNoMoreItems();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            int statusCode,
                            Header[] headers,
                            Throwable throwable,
                            JSONObject errorResponse) {
                        if (mStartedLoadingMore && mEndlessRecyclerViewScrollListener != null) {
                            mStartedLoadingMore = false;
                            mEndlessRecyclerViewScrollListener.notifyLoadMoreFailed();
                        }
                        ErrorHandling.handleError(
                                TimelineActivity.this,
                                Common.INFO_TAG,
                                "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                        if (!NetworkUtil.isNetworkAvailable(TimelineActivity.this)
                                || throwable instanceof UnknownHostException) {
                            Snackbar.make(
                                    rvTweets,
                                    "Network error!",
                                    Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Reload", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            fetchOlderTweets(max_id);
                                        }
                                    })
                                    .setActionTextColor(Color.WHITE)
                                    .show();
                        }
                    }
                });
    }

    @Override
    public void onNewTweet(Tweet newTweet) {
        mTweetsAdapter.addToFront(newTweet);
        rvTweets.smoothScrollToPosition(0);
    }

    class TweetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @Bind(R.id.ivItemTweetProfileImage) ImageView mIvItemTweetProfileImage;
        @Bind(R.id.tvItemTweetUserName) TextView mTvItemTweetUserName;
        @Bind(R.id.tvItemTweetBody) TextView mTvItemTweetBody;
        @Bind(R.id.tvItemTweetCreatedAt) TextView mTvItemTweetCreatedAt;

        View.OnClickListener mTvItemTweetCreatedAtOnClickListener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvItemTweetCreatedAt.setText(sDateFormat.format(mTweet.getCreatedAt()));
                mTvItemTweetCreatedAt.setBackgroundResource(
                        R.drawable.rectangle_background_timestamp);
                mTvItemTweetCreatedAt.setOnClickListener(mTvItemTweetCreatedAtOnClickListener2);
                // To yield space for the time stamp
                mTvItemTweetUserName.setText("");
            }
        };

        View.OnClickListener mTvItemTweetCreatedAtOnClickListener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvItemTweetCreatedAt.setText(
                        DateUtils.getRelativeTimeSpanString(mTweet.getCreatedAt().getTime()));
                mTvItemTweetCreatedAt.setBackgroundResource(0);
                mTvItemTweetCreatedAt.setOnClickListener(mTvItemTweetCreatedAtOnClickListener1);
                mTvItemTweetUserName.setText(mTweet.getUser().getScreenName());
            }
        };

        private Tweet mTweet;

        private TweetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        private void bindTweet(Context context, Tweet tweet) {
            mTweet = tweet;

            mTvItemTweetUserName.setText(tweet.getUser().getScreenName());
            mTvItemTweetBody.setText(tweet.getText());
            mTvItemTweetCreatedAt.setText(
                    DateUtils.getRelativeTimeSpanString(tweet.getCreatedAt().getTime()));

            mIvItemTweetProfileImage.setImageResource(android.R.color.transparent);
            Picasso.with(context)
                    .load(tweet.getUser().getProfileImageUrl())
                    .fit()
                    .into(mIvItemTweetProfileImage);

            mTvItemTweetCreatedAt.setOnClickListener(mTvItemTweetCreatedAtOnClickListener1);
        }

        @Override
        public void onClick(View v) {
            Intent i = TweetDetailActivity.newIntent(TimelineActivity.this, mTweet);
            startActivity(i);
        }
    }

    class TweetsAdapter extends RecyclerView.Adapter<TweetViewHolder> {
        // Sorted by ID's in descending order
        private List<Tweet> mTweets = new ArrayList<>();

        @Override
        public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_tweet, parent, false);
            TweetViewHolder vh = new TweetViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(TweetViewHolder holder, int position) {
            Tweet tweet = mTweets.get(position);
            holder.bindTweet(TimelineActivity.this, tweet);
        }

        @Override
        public int getItemCount() {
            return mTweets.size();
        }

        public void addAll(List<Tweet> tweets) {
            int oldLen = mTweets.size();
            mTweets.addAll(tweets);
            notifyItemRangeInserted(oldLen, tweets.size());
        }

        public void addToFront(Tweet tweet) {
            mTweets.add(0, tweet);
            notifyItemInserted(0);
        }

        public void addAllToFront(List<Tweet> tweets) {
            List<Tweet> newTweets = new ArrayList<>(tweets);
            newTweets.addAll(mTweets);
            mTweets = newTweets;
            notifyDataSetChanged();
        }

        public Tweet getItem(int position) {
            return mTweets.get(position);
        }
    }
}
