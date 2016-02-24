package com.codepath.apps.mysimpletweets.timeline;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.codepath.apps.mysimpletweets.BuildConfig;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.models.Mention;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MentionsTimelineFragment extends TimelineFragment {
    /**
     * Send an API request to get new tweets from the timeline json
     */
    @Override
    protected void fetchNewerTweets() {
        final Context context = getActivity();
        long since_id = SimpleTweetsPrefs.getNewestFetchedId(context);
        if (since_id != 0) {
            // We want to fetch some overlapped items to check if there is a gap between the newest
            // existing item and the new items we are fetching.
            since_id -= 1;
        }
        Log.d(Common.INFO_TAG, "fetch newer mentions");

        mClient.getMentions(
                10,
                since_id,
                0,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(
                                    Common.INFO_TAG, "fetch newer mentions: "
                                            + response.toString());
                        }
                        mSwipeContainer.setRefreshing(false);

                        // Deserialize JSON
                        // Create models
                        // Note that response sorts tweets in descending IDs
                        List<TweetInterface> newTweets = new ArrayList<TweetInterface>(
                                Mention.fromJsonArray(response));
                        mTweetsAdapter.appendAll(newTweets);

                        // We always fetch new items using the last fetched id - 1 as the
                        // since_id, so we support to get our previous fetched tweet back if
                        // there are not that many new items, and thus no gap between the last
                        // fetch and this fetch.. However, if the oldest tweet of the newTweets has
                        // ID newer than last fetched ID, we may have a gap, so we need to take
                        // care of this.
                        if (mTweetsAdapter.getItemCount() != 0
                                && !newTweets.isEmpty()
                                && newTweets.get(newTweets.size() - 1).getUid()
                                > SimpleTweetsPrefs.getNewestFetchedId(context)) {
                            if (SimpleTweetsPrefs.getNewestFetchedId(context) != 0) {
                                newTweets.get(newTweets.size() - 1).setHasMoreBefore(true);
                            }
                        }
                        // The adapter takes care of de-dedup
                        mTweetsAdapter.addAllToFront(newTweets);
                        if (!newTweets.isEmpty()) {
                            SimpleTweetsPrefs.setNewestFetchedId(context, newTweets
                                    .get(0).getUid());
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        mSwipeContainer.setRefreshing(false);

                        ErrorHandling.handleError(
                                context,
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
                                context,
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
     *
     * @param max_id The ID of the newest tweets to retrieve, exclusive.
     */
    @Override
    protected void fetchOlderTweets(final long max_id) {
        final Context context = getActivity();
        mPbLoading.setVisibility(View.VISIBLE);
        mClient.getMentions(
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
                        List<TweetInterface> newTweets = new ArrayList<TweetInterface>(
                                Mention.fromJsonArray(response));
                        mTweetsAdapter.appendAll(newTweets);

                        if (mStartedLoadingMore) {
                            mStartedLoadingMore = false;
                            if (newTweets.isEmpty()
                                    && mEndlessRecyclerViewScrollListener != null) {
                                mEndlessRecyclerViewScrollListener.notifyNoMoreItems();
                            }
                        }
                        mPbLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        if (mStartedLoadingMore && mEndlessRecyclerViewScrollListener != null) {
                            mStartedLoadingMore = false;
                            mEndlessRecyclerViewScrollListener.notifyLoadMoreFailed();
                        }

                        ErrorHandling.handleError(
                                context,
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
                        mPbLoading.setVisibility(View.GONE);
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
                                context,
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
                        mPbLoading.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Send an API request to get old tweets from the timeline json to fill the gap
     *
     * @param tweetWithGapEarlier the tweet that has a potential gap between this tweet and the
     *                            tweet before this tweet (by tweet id, Uid)
     * @param since_id The ID of the tweet that is right before tweetWithGapEarlier
     */
    @Override
    protected void fetchOlderTweetsForTimelineGap(
            final TweetInterface tweetWithGapEarlier,
            final long since_id) {
        mPbLoading.setVisibility(View.VISIBLE);
        mClient.getMentions(
                10,
                since_id - 1,  // This means we should get the prev tweet back if there is no gap
                tweetWithGapEarlier.getUid(),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(Common.INFO_TAG, "fetchOlderTweets: " + response.toString());
                        }

                        // Deserialize JSON
                        // Create models
                        List<TweetInterface> newTweets = new ArrayList<TweetInterface>(
                                Mention.fromJsonArray(response));
                        mTweetsAdapter.appendAll(newTweets);

                        if (!newTweets.isEmpty()
                                && newTweets.get(newTweets.size() - 1).getUid() > since_id) {
                            newTweets.get(newTweets.size() - 1).setHasMoreBefore(true);
                        }
                        tweetWithGapEarlier.setHasMoreBefore(false);
                        newTweets.add(tweetWithGapEarlier);
                        mTweetsAdapter.addAll(newTweets);
                        mPbLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        ErrorHandling.handleError(
                                getActivity(),
                                Common.INFO_TAG,
                                "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(Common.INFO_TAG, responseString);
                        mPbLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(
                            int statusCode,
                            Header[] headers,
                            Throwable throwable,
                            JSONObject errorResponse) {
                        ErrorHandling.handleError(
                                getActivity(),
                                Common.INFO_TAG,
                                "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(
                                Common.INFO_TAG,
                                errorResponse == null ? "" : errorResponse.toString());
                        mPbLoading.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected android.database.Cursor fetchTweetsCursor() {
        return Mention.fetchTweetsCursorForTimeline();
    }
}