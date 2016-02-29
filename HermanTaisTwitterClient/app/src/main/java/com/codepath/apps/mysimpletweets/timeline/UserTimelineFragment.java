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
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.models.UserTweet;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserTimelineFragment extends TimelineFragment {
    private static final String ARG_USER = "user";

    private User mUser;

    public static UserTimelineFragment newInstance(User user) {
        UserTimelineFragment frag = new UserTimelineFragment();
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
        long since_id = SimpleTweetsPrefs.getNewestUserTimelineFetchedId(context);
        if (since_id != 0) {
            // We want to fetch some overlapped items to check if there is a gap between the newest
            // existing item and the new items we are fetching.
            since_id -= 1;
        }
        // TODO: cannot differentiate between other users, so always fetch new tweets
        since_id = 0;

        mClient.getUserTimeline(
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
                                    "fetch newer user timeline: " + response.toString());
                        }
                        mSwipeContainer.setRefreshing(false);

                        // Deserialize JSON
                        // Create models
                        // Note that response sorts tweets in descending IDs
                        List<UserTweet> newTweets = UserTweet.fromJsonArray(response);

                        // We always fetch new items using the last fetched id - 1 as the
                        // since_id, so we support to get our previous fetched tweet back if
                        // there are not that many new items, and thus no gap between the last
                        // fetch and this fetch.. However, if the oldest tweet of the newTweets has
                        // ID newer than last fetched ID, we may have a gap, so we need to take
                        // care of this.
                        long prevNewestFetchedId = SimpleTweetsPrefs
                                .getNewestUserTimelineFetchedId(context);
                        if (mTweetsAdapter.getItemCount() != 0
                                && !newTweets.isEmpty()
                                && newTweets.get(newTweets.size() - 1).getUid()
                                        > prevNewestFetchedId
                                && prevNewestFetchedId != 0) {
                            newTweets.get(newTweets.size() - 1).setHasMoreBefore(true);
                        }
                        // The adapter takes care of de-dedup
                        mTweetsAdapter.addAllToFront(new ArrayList<TweetInterface>(newTweets));
                        if (!newTweets.isEmpty()) {
                            SimpleTweetsPrefs.setNewestUserTimelineFetchedId(context, newTweets
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
                                "Error retrieving user timeline: "
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
                                "Error retrieving user timeline: "
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
        mClient.getUserTimeline(
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
                                    "fetchOlderUserTweets: " + response.toString());
                        }

                        // Deserialize JSON
                        // Create models
                        List<TweetInterface> newTweets = new ArrayList<TweetInterface>(
                                UserTweet.fromJsonArray(response));
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
                                "Error retrieving user tweets: " + throwable.getLocalizedMessage(),
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
                                "Error retrieving user tweets: " + throwable.getLocalizedMessage(),
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
        mClient.getUserTimeline(
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
                        List<TweetInterface> newTweets = new ArrayList<TweetInterface>(
                                UserTweet.fromJsonArray(response));
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
                                "Error retrieving user tweets: " + throwable.getLocalizedMessage(),
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
                                "Error retrieving user tweets: " + throwable.getLocalizedMessage(),
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
        return UserTweet.fetchTweetsCursorForTimeline(mUser.getUid());
    }

    @Override
    protected String getNewestFetchedIdFieldInPrefs() {
        return SimpleTweetsPrefs.PREF_NEWEST_USER_TIMELINE_FETCHED_ID;
    }

    @Override
    protected void setNewestFetchedId(Context context, long id) {
        SimpleTweetsPrefs.setNewestUserTimelineFetchedId(context, id);
    }
}
