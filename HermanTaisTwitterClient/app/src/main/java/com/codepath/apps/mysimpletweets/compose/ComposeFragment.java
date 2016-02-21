package com.codepath.apps.mysimpletweets.compose;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeFragment extends DialogFragment {
    private static final String ARG_USER = "user";

    @Bind(R.id.etComposeTweet) TextView mEtComposeTweet;
    @Bind(R.id.tvComposeSend) TextView mTvComposeSend;

    private TwitterClient mClient;
    private OnNewTweetHandler mOnNewTweetHandler;

    public interface OnNewTweetHandler {
        void onNewTweet(Tweet newTweet);
    }

    public static ComposeFragment newInstance(User user) {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        fragment.setArguments(args);

        return fragment;
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
        getDialog().setTitle("Compose");

        View v = inflater.inflate(R.layout.fragment_compose, container, false);
        ButterKnife.bind(this, v);

        mTvComposeSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvComposeSend.setEnabled(false);
                CharSequence tweet = mEtComposeTweet.getText();
                mClient.updateStatus(tweet, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        mTvComposeSend.setEnabled(true);

                        Tweet newTweet = Tweet.fromJson(response);
                        Log.d(Common.INFO_TAG, "New tweet: " + response.toString());

                        if (mOnNewTweetHandler != null) {
                            Log.d(Common.INFO_TAG, "Call OnNewTweetHandler");
                            mOnNewTweetHandler.onNewTweet(newTweet);
                        }

                        ComposeFragment.this.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        mTvComposeSend.setEnabled(true);

                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error sending the tweet: " + throwable.getLocalizedMessage(),
                                throwable);
                    }
                });
            }
        });

        return v;
    }

    public void setOnNewTweetHandler(OnNewTweetHandler handler) {
        mOnNewTweetHandler = handler;
    }
}
