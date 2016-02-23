package com.codepath.apps.mysimpletweets.tweetdetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.models.VideoInfo;
import com.codepath.apps.mysimpletweets.models.VideoInfoVariant;
import com.codepath.apps.mysimpletweets.reply.ReplyFragment;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.codepath.apps.mysimpletweets.showvideo.VideoActivity;
import com.codepath.apps.mysimpletweets.timeline.TweetViewHolder;
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
    private static final String ARG_TWEET_POS = "tweet_position";

    private static final String EXTRA_TWEET = "com.codepath.apps.mysimpletweets.tweet";
    private static final String EXTRA_TWEET_POS = "com.codepath.apps.mysimpletweets.tweet_position";

    private static final int REQUEST_REPLY = 0;
    private static final int REQUEST_DETAIL = 1;

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
    @Bind(R.id.ivTweetDetailRetweeted) ImageView mIvTweetDetailRetweeted;
    @Bind(R.id.llTweetDetailReplies) LinearLayout mLlTweetDetailReplies;
    @Bind(R.id.vFullScreenVideoTrigger) View mVFullScreenVideoTrigger;

    private TwitterClient mClient;
    private Tweet mTweet;
    private int mTweetPos;

    public static TweetDetailFragment newInstance(int tweetPosition, Tweet tweet) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_TWEET, tweet);
        bundle.putInt(ARG_TWEET_POS, tweetPosition);

        TweetDetailFragment frag = new TweetDetailFragment();
        frag.setArguments(bundle);

        return frag;
    }

    public static Tweet getUpdatedTweet(Intent data) {
        return data.getParcelableExtra(EXTRA_TWEET);
    }

    public static int getUpdatedTweetPosition(Intent data) {
        return data.getIntExtra(EXTRA_TWEET_POS, -1);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = SimpleTweetsApplication.getRestClient();
        mTweet = getArguments().getParcelable(ARG_TWEET);
        mTweetPos = getArguments().getInt(ARG_TWEET_POS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweet_detail, container, false);
        ButterKnife.bind(this, v);

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
                    String videoUrl = null;
                    // First, find if there is application/x-mpegURL, use that one,
                    // if not, use the first one.
                    for (VideoInfoVariant variant : videoInfo.getVariants()) {
                        if (variant.getContentType().equals("application/x-mpegURL")) {
                            videoUrl = variant.getUrl();
                            break;
                        }
                    }
                    if (videoUrl == null) {
                        VideoInfoVariant variant = videoInfo.getVariants().get(1);
                        videoUrl = variant.getUrl();
                    }

                    mVvTweetDetailVideo.setVideoURI(Uri.parse(videoUrl));
                    mVvTweetDetailVideo.requestFocus();
                    mVvTweetDetailVideo.start();
                    mVvTweetDetailVideo.setVisibility(View.VISIBLE);
                    mVFullScreenVideoTrigger.setVisibility(View.VISIBLE);

                    final String _videoUrl = videoUrl;
                    mVFullScreenVideoTrigger.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = VideoActivity.newIntent(activity, _videoUrl);
                            startActivity(i);
                        }
                    });
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
                frag.setTargetFragment(TweetDetailFragment.this, REQUEST_REPLY);
                FragmentManager fm = activity.getSupportFragmentManager();
                frag.show(fm, "Reply");
            }
        });

        if (mTweet.isFavorited()) {
            setLiked(mIvTweetDetailFavorited);
        } else {
            setNotLiked(mIvTweetDetailFavorited);
        }

        User user = SimpleTweetsPrefs.getUser(activity);
        if (user != null && user.getUid() == mTweet.getUser().getUid()) {
            mIvTweetDetailRetweeted.setImageResource(R.drawable.ic_unretweetable);
        } else {
            if (mTweet.isRetweeted()) {
                setRetweeted(mIvTweetDetailRetweeted);
            } else {
                setNotRetweeted(mIvTweetDetailRetweeted);
            }
        }

        setUpReplies();

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REPLY && resultCode == Activity.RESULT_OK) {
            setUpReplies();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    private void setUpReplies() {
        mLlTweetDetailReplies.removeAllViews();

        List<Tweet> replies = Tweet.fetchRepliesTweets(mTweet.getUid());

        Activity activity = getActivity();
        if (!replies.isEmpty()) {
            for (int i = 0; i < replies.size(); i++) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View v = inflater.inflate(R.layout.item_tweet, mLlTweetDetailReplies, false);

                new TweetViewHolder(
                        v,
                        null,
                        null,
                        new TweetViewHolder.TweetOnClickListener() {
                            @Override
                            public void onClick(int position, Tweet tweet) {
                                Activity activity = getActivity();
                                Intent i = TweetDetailActivity.newIntent(
                                        getActivity(),
                                        position,
                                        tweet);
                                activity.startActivityForResult(i, REQUEST_DETAIL);
                            }
                        })
                        .bindTweet(activity, i, replies.get(i));

                mLlTweetDetailReplies.addView(v);
            }
        }
    }

    private void setLiked(final ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_liked);
        imageView.setOnClickListener(new View.OnClickListener() {
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
                        setNotLiked(imageView);
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

    private void setNotLiked(final ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_like);
        imageView.setOnClickListener(new View.OnClickListener() {
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
                        setLiked(imageView);
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

    private void setRetweeted(final ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_retweeted);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure next time we refresh this tweet, in case we cannot make it after the
                // unretweet.
                Context context = TweetDetailFragment.this.getActivity();
                if (SimpleTweetsPrefs.getNewestFetchedId(context) > mTweet.getUid()) {
                    SimpleTweetsPrefs.setNewestFetchedId(context, mTweet.getUid());
                }

                mClient.unretweet(mTweet.getUid(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(
                            int statusCode, Header[] headers, JSONObject response) {
                        Log.d(Common.INFO_TAG, "Unretweeted tweet: " + mTweet);
                        setNotRetweeted(imageView);
                        refreshTweet();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error unretweeting the tweet",
                                throwable);
                    }
                });
            }
        });
    }

    private void setNotRetweeted(final ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_retweet);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure next time we refresh this tweet, in case we cannot make it after the
                // retweet.
                Context context = TweetDetailFragment.this.getActivity();
                if (SimpleTweetsPrefs.getNewestFetchedId(context) > mTweet.getUid()) {
                    SimpleTweetsPrefs.setNewestFetchedId(context, mTweet.getUid());
                }

                mClient.retweet(mTweet.getUid(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(
                            int statusCode, Header[] headers, JSONObject response) {
                        Log.d(Common.INFO_TAG, "Retweeted tweet: " + mTweet);
                        setRetweeted(imageView);
                        refreshTweet();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error retweeting the tweet",
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
                Intent i = new Intent();
                i.putExtra(EXTRA_TWEET, mTweet);
                i.putExtra(EXTRA_TWEET_POS, mTweetPos);
                TweetDetailFragment.this.getActivity().setResult(
                        Activity.RESULT_OK,
                        i);
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
