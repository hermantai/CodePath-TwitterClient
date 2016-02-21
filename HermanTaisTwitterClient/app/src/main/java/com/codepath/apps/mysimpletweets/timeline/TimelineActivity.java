package com.codepath.apps.mysimpletweets.timeline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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
import android.widget.Toast;

import com.activeandroid.Cache;
import com.activeandroid.query.Select;
import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.BuildConfig;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.compose.ComposeFragment;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.helpers.NetworkUtil;
import com.codepath.apps.mysimpletweets.helpers.StringUtil;
import com.codepath.apps.mysimpletweets.models.Media;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.codepath.apps.mysimpletweets.tweetdetail.TweetDetailActivity;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

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
                User user = SimpleTweetsPrefs.getUser(TimelineActivity.this);
                if (user == null) {
                    Toast.makeText(
                            TimelineActivity.this,
                            "User info does not exist, please pull down to reload",
                            Toast.LENGTH_LONG)
                            .show();
                }
                ComposeFragment frag = ComposeFragment.newInstance(user);
                frag.setOnNewTweetHandler(TimelineActivity.this);

                FragmentManager fm = getSupportFragmentManager();
                frag.show(fm, "Compose");
            }
        });

        mTweetsAdapter = new TweetsAdapter();
        int itemCount = mTweetsAdapter.getItemCount();
        if (itemCount != 0) {
            SimpleTweetsPrefs.setNewestFetchedId(
                    this, mTweetsAdapter.getItem(itemCount - 1).getUid());
        }
        rvTweets.setAdapter(mTweetsAdapter);
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

        mClient = SimpleTweetsApplication.getRestClient();

        if (NetworkUtil.isNetworkAvailable(this)) {
            fetchNewerTweets();
            refreshUser();
        }
    }

    /**
     * Send an API request to get new tweets from the timeline json
     * Fill the list by creating the tweet objects from the json
     */
    private void fetchNewerTweets() {
        long since_id = SimpleTweetsPrefs.getNewestFetchedId(this);
        if (since_id != 0) {
            // We want to fetch some overlapped items to check if there is a gap between the newest
            // existing item and the new items we are fetching.
            since_id -= 1;
        }

        mClient.getHomeTimeline(
                10,
                since_id,
                0,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(Common.INFO_TAG, "fetchOlderTweets: " + response.toString());
                        }
                        mSwipeContainer.setRefreshing(false);

                        // Deserialize JSON
                        // Create models
                        // Note that response sorts tweets in descending IDs
                        List<Tweet> newTweets = Tweet.fromJsonArray(response);

                        // We always fetch new items using the last fetched id - 1 as the
                        // since_id, so we support to get our previous fetched tweet back if
                        // there are not that many new items, and thus no gap between the last
                        // fetch and this fetch.. However, if the oldest tweet of the newTweets has
                        // ID newer than last fetched ID, we may have a gap, so we need to take
                        // care of this.
                        if (mTweetsAdapter.getItemCount() != 0
                                && !newTweets.isEmpty()
                                && newTweets.get(newTweets.size() - 1).getUid()
                                        > SimpleTweetsPrefs.getNewestFetchedId(
                                TimelineActivity.this)) {
                            newTweets.get(newTweets.size() - 1).setHasMoreBefore(true);
                        }
                        // The adapter takes care of de-dedup
                        mTweetsAdapter.addAllToFront(newTweets);
                        if (!newTweets.isEmpty()) {
                            SimpleTweetsPrefs.setNewestFetchedId(TimelineActivity.this, newTweets
                                    .get(0).getUid());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        mSwipeContainer.setRefreshing(false);

                        ErrorHandling.handleError(
                                TimelineActivity.this,
                                Common.INFO_TAG,
                                "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(Common.INFO_TAG, responseString);
                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fetchNewerTweets();
                            }
                        });
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
                        LogUtil.d(
                                Common.INFO_TAG,
                                errorResponse == null ? "" : errorResponse.toString());

                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fetchNewerTweets();
                            }
                        });
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
                10,
                0,
                max_id,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(Common.INFO_TAG, "fetchOlderTweets: " + response.toString());
                        }

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
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        if (mStartedLoadingMore && mEndlessRecyclerViewScrollListener != null) {
                            mStartedLoadingMore = false;
                            mEndlessRecyclerViewScrollListener.notifyLoadMoreFailed();
                        }

                        ErrorHandling.handleError(
                                TimelineActivity.this,
                                Common.INFO_TAG,
                                "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(Common.INFO_TAG, responseString);
                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fetchOlderTweets(max_id);
                            }
                        });
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
                        LogUtil.d(
                                Common.INFO_TAG,
                                errorResponse == null ? "" : errorResponse.toString());
                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fetchOlderTweets(max_id);
                            }
                        });
                    }
                });
    }

    private void refreshUser() {
        mClient.getCurrentUser(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                SimpleTweetsPrefs.setUser(TimelineActivity.this, User.fromJson(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString,
                                  Throwable throwable) {
                ErrorHandling.handleError(
                        TimelineActivity.this,
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
                        TimelineActivity.this,
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

    @Override
    public void onNewTweet(Tweet newTweet) {
        mTweetsAdapter.addToFront(newTweet);
        rvTweets.smoothScrollToPosition(0);
    }

    private void showSnackBarForNetworkError(View.OnClickListener listener) {
        Snackbar.make(
                rvTweets,
                "Network error!",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Reload", listener)
                .setActionTextColor(Color.YELLOW)
                .show();
    }

    class TweetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @Bind(R.id.ivItemTweetProfileImage) ImageView mIvItemTweetProfileImage;
        @Bind(R.id.tvItemTweetUserName) TextView mTvItemTweetUserName;
        @Bind(R.id.tvItemTweetUserScreenName) TextView mTvItemTweetUserScreenName;
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

        private Tweet mTweet;

        private TweetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
            mTvItemTweetBody.setOnClickListener(this);
        }

        private void bindTweet(Context context, Tweet tweet) {
            mTweet = tweet;

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
            Glide.with(context)
                    .load(tweet.getUser().getProfileImageUrl())
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
        private Cursor mCursor;

        public TweetsAdapter() {
            mCursor = fetchTweetsCursor();
        }

        @Override
        public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_tweet, parent, false);
            TweetViewHolder vh = new TweetViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(TweetViewHolder holder, int position) {
            Tweet tweet = getItem(position);;
            holder.bindTweet(TimelineActivity.this, tweet);
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }

        public void addAll(List<Tweet> tweets) {
            int oldLen = getItemCount();
            Tweet.saveAll(tweets);
            mCursor = fetchTweetsCursor();
            notifyItemRangeInserted(oldLen, tweets.size());
        }

        public void addToFront(Tweet tweet) {
            tweet.save();
            mCursor = fetchTweetsCursor();
            notifyItemInserted(0);
        }

        public void addAllToFront(List<Tweet> tweets) {
            Tweet.saveAll(tweets);
            mCursor = fetchTweetsCursor();
            notifyDataSetChanged();
        }

        public Tweet getItem(int position) {
            mCursor.moveToPosition(position);
            Tweet tweet = new Tweet();
            tweet.loadFromCursor(mCursor);
            return tweet;
        }

        private Cursor fetchTweetsCursor() {
            String resultRecords = new Select().from(Tweet.class).orderBy("m_uid desc").toSql();
            return Cache.openDatabase().rawQuery(resultRecords, new String[]{});
        }
    }
}
