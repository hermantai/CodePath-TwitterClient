package com.codepath.apps.mysimpletweets.reply;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
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

public class ReplyFragment extends DialogFragment {
    private static final String ARG_USER = "user";
    private static final String ARG_TWEET = "tweet";

    @Bind(R.id.tvReplyDescription) TextView mTvReplyDescription;
    @Bind(R.id.etReplyTweet) EditText mEtReplyTweet;
    @Bind(R.id.btnReplySend) Button mBtnReplySend;

    private TwitterClient mClient;
    private Tweet mTweet;

    public static ReplyFragment newInstance(User user, Tweet tweet) {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        args.putParcelable(ARG_TWEET, tweet);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = SimpleTweetsApplication.getRestClient();
        mTweet = getArguments().getParcelable(ARG_TWEET);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View v = inflater.inflate(R.layout.fragment_reply, container, false);
        ButterKnife.bind(this, v);

        mTvReplyDescription.setText("In reply to " + mTweet.getUser().getName());
        String prefilled = "@" + mTweet.getUser().getScreenName() + " ";
        mEtReplyTweet.setText(prefilled);
        mEtReplyTweet.setSelection(prefilled.length());

        mBtnReplySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnReplySend.setEnabled(false);
                CharSequence tweet = mEtReplyTweet.getText();
                mClient.replyStatus(tweet, mTweet.getUid(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        mBtnReplySend.setEnabled(true);

                        Tweet newTweet = Tweet.fromJson(response);
                        Log.d(Common.INFO_TAG, "New reply tweet: " + response.toString());
                        newTweet.save();

                        getTargetFragment().onActivityResult(
                                getTargetRequestCode(),
                                Activity.RESULT_OK,
                                null);
                        ReplyFragment.this.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        mBtnReplySend.setEnabled(true);

                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error replying the tweet: " + throwable.getLocalizedMessage(),
                                throwable);
                    }
                });
            }
        });

        return v;
    }
}
