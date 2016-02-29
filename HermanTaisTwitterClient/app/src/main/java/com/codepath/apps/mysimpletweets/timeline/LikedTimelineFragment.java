package com.codepath.apps.mysimpletweets.timeline;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.codepath.apps.mysimpletweets.BuildConfig;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.models.LikedTweet;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LikedTimelineFragment extends TimelineFragment {
    private static final String ARG_USER = "user";

    private User mUser;

    public static LikedTimelineFragment newInstance(User user) {
        LikedTimelineFragment frag = new LikedTimelineFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = getArguments().getParcelable(ARG_USER);
    }

    @Override
    protected void fetchNewerTweets() {
        final Context context = getActivity();
        long since_id = SimpleTweetsPrefs.getNewestLikedFetchedId(context);
        if (since_id != 0) {
            // We want to fetch some overlapped items to check if there is a gap between the newest
            // existing item and the new items we are fetching.
            since_id -= 1;
        }
        // TODO: cannot differentiate between other users, so always fetch new tweets
        since_id = 0;

        mClient.getLikedTweets(
                mUser.getScreenName(),
                10,
                since_id,
                0,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(
                                    Common.INFO_TAG,
                                    "fetch newer liked tweets: " + response.toString());
                        }
                        mSwipeContainer.setRefreshing(false);

                        // Deserialize JSON
                        // Create models
                        // Note that response sorts tweets in descending IDs
                        List<LikedTweet> likedTweets = LikedTweet.fromJsonArray(
                                response,
                                mUser.getScreenName());

                        // We always fetch new items using the last fetched id - 1 as the
                        // since_id, so we support to get our previous fetched tweet back if
                        // there are not that many new items, and thus no gap between the last
                        // fetch and this fetch.. However, if the oldest tweet of the newTweets has
                        // ID newer than last fetched ID, we may have a gap, so we need to take
                        // care of this.
                        if (mTweetsAdapter.getItemCount() != 0
                                && !likedTweets.isEmpty()
                                && likedTweets.get(likedTweets.size() - 1).getUid()
                                > SimpleTweetsPrefs.getNewestLikedFetchedId(context)) {
                            likedTweets.get(likedTweets.size() - 1).setHasMoreBefore(true);
                        }
                        // The adapter takes care of de-dedup
                        mTweetsAdapter.addAllToFront(new ArrayList<TweetInterface>(likedTweets));
                        if (!likedTweets.isEmpty()) {
                            SimpleTweetsPrefs.setNewestLikedFetchedId(context, likedTweets
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
                                "Error retrieving liked tweets: "
                                        + throwable.getLocalizedMessage(),
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
                                "Error retrieving liked tweets: "
                                        + throwable.getLocalizedMessage(),
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

    @Override
    protected void fetchOlderTweets(final long max_id) {
        final Context context = getActivity();
        mPbLoading.setVisibility(View.VISIBLE);
        mClient.getLikedTweets(
                mUser.getScreenName(),
                10,
                0,
                max_id,
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(
                                    Common.INFO_TAG,
                                    "fetchOlderLikedTweets: " + response.toString());
                        }

                        // Deserialize JSON
                        // Create models
                        List<TweetInterface> newLikedTweets = new ArrayList<TweetInterface>(
                                LikedTweet.fromJsonArray(response, mUser.getScreenName()));
                        mTweetsAdapter.appendAll(newLikedTweets);

                        if (mStartedLoadingMore) {
                            mStartedLoadingMore = false;
                            if (newLikedTweets.isEmpty()
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
                                "Error retrieving liked tweets: " + throwable.getLocalizedMessage(),
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
                                "Error retrieving liked tweets: " + throwable.getLocalizedMessage(),
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

    @Override
    protected void fetchOlderTweetsForTimelineGap(
            final TweetInterface tweetWithGapEarlier, final long since_id) {
        mPbLoading.setVisibility(View.VISIBLE);
        mClient.getLikedTweets(
                mUser.getScreenName(),
                10,
                since_id - 1,  // This means we should get the prev tweet back if there is no gap
                tweetWithGapEarlier.getUid(),
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(
                                    Common.INFO_TAG,
                                    "fetchOlderUserTweets: " + response.toString());
                        }

                        // Deserialize JSON
                        // Create models
                        List<TweetInterface> newLikedTweets = new ArrayList<TweetInterface>(
                                LikedTweet.fromJsonArray(response, mUser.getScreenName()));
                        mTweetsAdapter.appendAll(newLikedTweets);

                        if (!newLikedTweets.isEmpty()
                                && newLikedTweets.get(newLikedTweets.size() - 1).getUid() > since_id) {
                            newLikedTweets.get(newLikedTweets.size() - 1).setHasMoreBefore(true);
                        }
                        tweetWithGapEarlier.setHasMoreBefore(false);
                        newLikedTweets.add(tweetWithGapEarlier);
                        mTweetsAdapter.addAll(newLikedTweets);
                        mPbLoading.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        ErrorHandling.handleError(
                                getActivity(),
                                Common.INFO_TAG,
                                "Error retrieving liked tweets: " + throwable.getLocalizedMessage(),
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
                                "Error retrieving liked tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(
                                Common.INFO_TAG,
                                errorResponse == null ? "" : errorResponse.toString());
                        mPbLoading.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected Cursor fetchTweetsCursor() {
        return LikedTweet.fetchTweetsCursorForTimeline(mUser.getScreenName());
    }

    @Override
    protected String getNewestFetchedIdFieldInPrefs() {
        return SimpleTweetsPrefs.PREF_NEWEST_LIKED_FETCHED_ID;
    }

    @Override
    protected void setNewestFetchedId(Context context, long id) {
        SimpleTweetsPrefs.setNewestLikedFetchedId(context, id);
    }
}
