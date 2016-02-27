package com.codepath.apps.mysimpletweets.timeline;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Media;
import com.codepath.apps.mysimpletweets.models.TweetInterface;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MediaViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.llItemMediaTweetImages) LinearLayout mLlItemMediaTweetImages;

    public MediaViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bindTweet(Context context, TweetInterface tweet) {
        for (Media media : tweet.getExtendedEntities().getMedia()) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 10, 0, 0);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(params);
            mLlItemMediaTweetImages.addView(imageView);

            Glide.with(context)
                    .load(media.getMediaUrl())
                    .into(imageView);
        }
    }
}
