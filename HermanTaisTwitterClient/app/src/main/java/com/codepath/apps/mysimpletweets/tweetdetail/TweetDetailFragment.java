package com.codepath.apps.mysimpletweets.tweetdetail;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.models.ExtendedEntities;
import com.codepath.apps.mysimpletweets.models.Media;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.VideoInfo;
import com.codepath.apps.mysimpletweets.models.VideoInfoVariant;
import com.codepath.apps.mysimpletweets.reply.ReplyFragment;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TweetDetailFragment extends Fragment {
    private static final String ARG_TWEET = "tweet";

    @Bind(R.id.ivTweetDetailProfileImage) ImageView mIvTweetDetailProfileImage;
    @Bind(R.id.tvTweetDetailUserName) TextView mTvTweetDetailUserName;
    @Bind(R.id.tvTweetDetailUserScreenName) TextView mTvTweetDetailUserScreenName;
    @Bind(R.id.tvTweetDetailBody) TextView mTvTweetDetailBody;
    @Bind(R.id.llTweetDetailPictures) LinearLayout mLlTweetDetailPictures;
    @Bind(R.id.vvTweetDetailVideo) VideoView mVvTweetDetailVideo;
    @Bind(R.id.tvTweetDetailCreatedAt) TextView mTvTweetDetailCreatedAt;
    @Bind(R.id.llTweetDetailRetweetLikeCounts) LinearLayout mLlTweetDetailRetweetLikeCounts;
    @Bind(R.id.tvTweetDetailRetweetLikeCounts) TextView mTvTweetDetailRetweetLikeCounts;
    @Bind(R.id.ivTweetDetailReply) ImageView mIvTweetDetailReply;
    @Bind(R.id.ivTweetDetailFavorited) ImageView mIvTweetDetailFavorited;

    private TwitterClient mClient;
    private Tweet mTweet;

    public static TweetDetailFragment newInstance(Tweet tweet) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_TWEET, tweet);

        TweetDetailFragment frag = new TweetDetailFragment();
        frag.setArguments(bundle);

        return frag;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = SimpleTweetsApplication.getRestClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweet_detail, container, false);
        ButterKnife.bind(this, v);

        mTweet = getArguments().getParcelable(ARG_TWEET);
        LogUtil.d(Common.INFO_TAG, "Detail for tweet: " + mTweet);
        final FragmentActivity activity = getActivity();

        Glide.with(activity)
                .load(mTweet.getUser().getProfileImageUrl())
                .into(mIvTweetDetailProfileImage);
        mTvTweetDetailUserName.setText(mTweet.getUser().getName());
        mTvTweetDetailUserScreenName.setText("@" + mTweet.getUser().getScreenName());
        mTvTweetDetailBody.setText(mTweet.getText());

        List<String> urlsToBeRemoved = new ArrayList<>();

        // add all photos and videos as needed
        ExtendedEntities extendedEntities = mTweet.getExtendedEntities();
        if (extendedEntities != null) {
            for (Media media : mTweet.getExtendedEntities().getMedia()) {
                if (media.getType().equals("photo")) {
                    ImageView imageView = new ImageView(activity);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 10, 0, 0);
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    imageView.setLayoutParams(params);
                    mLlTweetDetailPictures.addView(imageView);

                    Glide.with(activity)
                            .load(media.getMediaUrl())
                            .into(imageView);

                    urlsToBeRemoved.add(media.getUrl());
                } else if (media.getType().equals("video")) {
                    // We assume there is only at most one video
                    VideoInfo videoInfo = media.getVideoInfo();
                    boolean done = false;
                    // First, find if there is application/x-mpegURL, use that one,
                    // if not, use the first one.
                    for (VideoInfoVariant variant : videoInfo.getVariants()) {
                        if (variant.getContentType().equals("application/x-mpegURL")) {
                            mVvTweetDetailVideo.setVideoURI(Uri.parse(variant.getUrl()));
                            //mVvTweetDetailVideo.setMediaController(new MediaController(activity));
                            mVvTweetDetailVideo.requestFocus();
                            mVvTweetDetailVideo.start();
                            mVvTweetDetailVideo.setVisibility(View.VISIBLE);
                            done = true;
                            break;
                        }
                    }
                    if (!done) {
                        VideoInfoVariant variant = videoInfo.getVariants().get(1);
                        mVvTweetDetailVideo.setVideoURI(Uri.parse(variant.getUrl()));
                        mVvTweetDetailVideo.setMediaController(new MediaController(activity));
                        mVvTweetDetailVideo.requestFocus();
                        mVvTweetDetailVideo.start();
                        mVvTweetDetailVideo.setVisibility(View.VISIBLE);
                    }
                    urlsToBeRemoved.add(media.getUrl());
                }
            }
        }

        if (!urlsToBeRemoved.isEmpty()) {
            String s = mTweet.getText();
            for (String toBeRemoved: urlsToBeRemoved) {
                s = s.replace(toBeRemoved, "");
            }
            mTvTweetDetailBody.setText(s);
        }

        mTvTweetDetailCreatedAt.setText(mTweet.getCreatedAt().toString());

        setUpRetweetLikeCounts();

        mIvTweetDetailReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReplyFragment frag = ReplyFragment.newInstance(
                        SimpleTweetsPrefs.getUser(activity), mTweet);
                FragmentManager fm = activity.getSupportFragmentManager();
                frag.show(fm, "Reply");
            }
        });

        if (mTweet.isFavorited()) {
            setLiked(mIvTweetDetailFavorited);
        } else {
            setNotLiked(mIvTweetDetailFavorited);

        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void setUpRetweetLikeCounts() {
        StringBuilder retweetLikeCountsSb = new StringBuilder();
        if (mTweet.getRetweetCount() > 0) {
            retweetLikeCountsSb.append("<font color=\"black\">");
            retweetLikeCountsSb.append(mTweet.getRetweetCount());
            retweetLikeCountsSb.append("</font>");
            retweetLikeCountsSb.append(" RETWEETS");
        }
        if (mTweet.getFavoriteCount() > 0) {
            if (retweetLikeCountsSb.length() > 0) {
                retweetLikeCountsSb.append(" ");
            }
            retweetLikeCountsSb.append("<font color=\"black\">");
            retweetLikeCountsSb.append(mTweet.getFavoriteCount());
            retweetLikeCountsSb.append("</font>");
            retweetLikeCountsSb.append(" LIKES");
        }

        if (retweetLikeCountsSb.length() > 0) {
            mLlTweetDetailRetweetLikeCounts.setVisibility(View.VISIBLE);
            mTvTweetDetailRetweetLikeCounts.setText(Html.fromHtml(retweetLikeCountsSb.toString()));
        } else {
            mLlTweetDetailRetweetLikeCounts.setVisibility(View.GONE);
        }
    }

    private void setNotLiked(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_like);
        mIvTweetDetailFavorited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure next time we refresh this tweet, in case we cannot make it after the
                // like.
                Context context = TweetDetailFragment.this.getActivity();
                if (SimpleTweetsPrefs.getNewestFetchedId(context) > mTweet.getUid()) {
                    SimpleTweetsPrefs.setNewestFetchedId(context, mTweet.getUid());
                }

                mClient.like(mTweet.getUid(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(
                            int statusCode, Header[] headers, JSONObject response) {
                        Log.d(Common.INFO_TAG, "Liked tweet: " + mTweet);
                        setLiked(mIvTweetDetailFavorited);
                        refreshTweet();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error liking the tweet",
                                throwable);
                    }
                });
            }
        });
    }

    public void setLiked(ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_liked);
        mIvTweetDetailFavorited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure next time we refresh this tweet, in case we cannot make it after the
                // unlike.
                Context context = TweetDetailFragment.this.getActivity();
                if (SimpleTweetsPrefs.getNewestFetchedId(context) > mTweet.getUid()) {
                    SimpleTweetsPrefs.setNewestFetchedId(context, mTweet.getUid());
                }

                mClient.unlike(mTweet.getUid(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(
                            int statusCode, Header[] headers, JSONObject response) {
                        Log.d(Common.INFO_TAG, "Unliked tweet: " + mTweet);
                        setNotLiked(mIvTweetDetailFavorited);
                        refreshTweet();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error unliking the tweet",
                                throwable);
                    }
                });
            }
        });
    }

    private void refreshTweet() {
        mClient.lookup(mTweet.getUid(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(Common.INFO_TAG, "New tweet: " + response.toString());
                List<Tweet> tweets = Tweet.fromJsonArray(response);
                if (!tweets.isEmpty()) {
                    mTweet = tweets.get(0);
                    mTweet.save();
                    setUpRetweetLikeCounts();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Context context = getActivity();
                ErrorHandling.handleError(
                        context,
                        Common.INFO_TAG,
                        "Error refreshing the tweet: " + throwable.getLocalizedMessage(),
                        throwable);
            }
        });
    }
}
