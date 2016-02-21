package com.codepath.apps.mysimpletweets.showvideo;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.codepath.apps.mysimpletweets.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoActivity extends AppCompatActivity {
    private final static String INTENT_EXTRA_VIDEO_URL =
            "com.codepath.apps.mysimpletweets.video_url";

    public final static Intent newIntent(Context context, String videoUrl) {
        Intent i = new Intent(context, VideoActivity.class);
        i.putExtra(INTENT_EXTRA_VIDEO_URL, videoUrl);
        return i;
    }

    @Bind(R.id.vvTweetVideo)
    VideoView mVvTweetVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        ButterKnife.bind(this);

        mVvTweetVideo.setVideoPath(
                getIntent().getStringExtra(INTENT_EXTRA_VIDEO_URL));

        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(mVvTweetVideo);
        mVvTweetVideo.setMediaController(mediaController);

        mVvTweetVideo.requestFocus();
        mVvTweetVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVvTweetVideo.start();
            }
        });
    }
}
