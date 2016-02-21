package com.codepath.apps.mysimpletweets.tweetdetail;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
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
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.models.ExtendedEntities;
import com.codepath.apps.mysimpletweets.models.Media;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.VideoInfo;
import com.codepath.apps.mysimpletweets.models.VideoInfoVariant;

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

    public static TweetDetailFragment newInstance(Tweet tweet) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_TWEET, tweet);

        TweetDetailFragment frag = new TweetDetailFragment();
        frag.setArguments(bundle);

        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweet_detail, container, false);
        ButterKnife.bind(this, v);

        Tweet tweet = getArguments().getParcelable(ARG_TWEET);
        LogUtil.d(Common.INFO_TAG, "Detail for tweet: " + tweet);
        Activity activity = getActivity();

        Glide.with(activity)
                .load(tweet.getUser().getProfileImageUrl())
                .into(mIvTweetDetailProfileImage);
        mTvTweetDetailUserName.setText(tweet.getUser().getName());
        mTvTweetDetailUserScreenName.setText("@" + tweet.getUser().getScreenName());
        mTvTweetDetailBody.setText(tweet.getText());

        List<String> urlsToBeRemoved = new ArrayList<>();

        // add all photos and videos as needed
        ExtendedEntities extendedEntities = tweet.getExtendedEntities();
        if (extendedEntities != null) {
            for (Media media : tweet.getExtendedEntities().getMedia()) {
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
            String s = tweet.getText();
            for (String toBeRemoved: urlsToBeRemoved) {
                s = s.replace(toBeRemoved, "");
            }
            mTvTweetDetailBody.setText(s);
        }

        mTvTweetDetailCreatedAt.setText(tweet.getCreatedAt().toString());

        StringBuilder retweetLikeCountsSb = new StringBuilder();
        if (tweet.getRetweetCount() > 0) {
            retweetLikeCountsSb.append("<font color=\"black\">");
            retweetLikeCountsSb.append(tweet.getRetweetCount());
            retweetLikeCountsSb.append("</font>");
            retweetLikeCountsSb.append(" RETWEETS");
        }
        if (tweet.getFavoriteCount() > 0) {
            if (retweetLikeCountsSb.length() > 0) {
                retweetLikeCountsSb.append(" ");
            }
            retweetLikeCountsSb.append("<font color=\"black\">");
            retweetLikeCountsSb.append(tweet.getFavoriteCount());
            retweetLikeCountsSb.append("</font>");
            retweetLikeCountsSb.append(" LIKES");
        }

        if (retweetLikeCountsSb.length() > 0) {
            mLlTweetDetailRetweetLikeCounts.setVisibility(View.VISIBLE);
            mTvTweetDetailRetweetLikeCounts.setText(Html.fromHtml(retweetLikeCountsSb.toString()));
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
