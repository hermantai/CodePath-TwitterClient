package com.codepath.apps.mysimpletweets.timeline;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Media;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.repo.SimpleTweetsPrefs;
import com.codepath.apps.mysimpletweets.tweetdetail.TweetDetailActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MediaViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.llItemMediaTweetImages) LinearLayout mLlItemMediaTweetImages;

    public MediaViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindTweet(final Activity activity, final TweetInterface tweet) {
        for (Media media : tweet.getExtendedEntities().getMedia()) {
            ImageView imageView = new ImageView(activity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 1, 0, 0);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(params);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = TweetDetailActivity.newIntent(
                            activity,
                            getAdapterPosition(),
                            tweet,
                            SimpleTweetsPrefs.PREF_NEWEST_USER_TIMELINE_FETCHED_ID);
                    activity.startActivity(i);
                }
            });

            mLlItemMediaTweetImages.addView(imageView);

            Glide.with(activity)
                    .load(media.getMediaUrl())
                    .into(imageView);
        }
    }
}
