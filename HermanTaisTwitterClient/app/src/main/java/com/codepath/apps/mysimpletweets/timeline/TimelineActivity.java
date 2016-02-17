package com.codepath.apps.mysimpletweets.timeline;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.twitter.TwitterApplication;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TimelineActivity extends AppCompatActivity {
    @Bind(R.id.rvTweets) RecyclerView rvTweets;
    @Bind(R.id.fab) FloatingActionButton mFab;

    private TwitterClient mClient;
    private TweetsAdapter mTweetsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mTweetsAdapter = new TweetsAdapter();
        rvTweets.setAdapter(mTweetsAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTweets.setLayoutManager(layoutManager);

        mClient = TwitterApplication.getRestClient();
        populateTimeline();
    }

    /**
     * Send an API request to get the timeline json
     * Fill the listview by creating the tweet objects from the json
     */
    private void populateTimeline() {
        mClient.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                LogUtil.d(Common.INFO_TAG, response.toString());

                // Deserialize JSON
                // Create models
                // Load the model data into ListView
                mTweetsAdapter.addAll(Tweet.fromJsonArray(response));
            }

            @Override
            public void onFailure(
                    int statusCode,
                    Header[] headers,
                    Throwable throwable,
                    JSONObject errorResponse) {
                ErrorHandling.handleError(
                        TimelineActivity.this,
                        Common.INFO_TAG,
                        "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                        throwable);
            }
        });
    }

    class TweetViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivProfileImage) ImageView mIvProfileImage;
        @Bind(R.id.tvUserName) TextView mTvUserName;
        @Bind(R.id.tvBody) TextView mTvBody;

        private TweetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bindTweet(Context context, Tweet tweet) {
            mTvUserName.setText(tweet.getUser().getScreenName());
            mTvBody.setText(tweet.getBody());

            mIvProfileImage.setImageResource(android.R.color.transparent);
            Picasso.with(context)
                    .load(tweet.getUser().getProfileImageUrl())
                    .fit()
                    .into(mIvProfileImage);
        }
    }

    class TweetsAdapter extends RecyclerView.Adapter<TweetViewHolder> {
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

        private void addAll(List<Tweet> tweets) {
            int oldLen = mTweets.size();
            mTweets.addAll(tweets);
            notifyItemRangeInserted(oldLen, tweets.size());
        }
    }
}
