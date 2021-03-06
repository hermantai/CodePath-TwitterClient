package com.codepath.apps.mysimpletweets.reply;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.models.TweetInterface;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.codepath.apps.mysimpletweets.widgets.SimpleProgressDialog;
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
    @Bind(R.id.tvReplySend) TextView mTvReplySend;

    private TwitterClient mClient;
    private TweetInterface mTweet;

    public static ReplyFragment newInstance(User user, TweetInterface tweet) {
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

        mTvReplySend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvReplySend.setEnabled(false);
                final ProgressDialog progressDialog = SimpleProgressDialog.createProgressDialog(
                        getActivity());
                CharSequence tweet = mEtReplyTweet.getText();
                mClient.replyStatus(tweet, mTweet.getUid(), new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        mTvReplySend.setEnabled(true);

                        Tweet newTweet = Tweet.fromJson(response);
                        Log.d(Common.INFO_TAG, "New reply tweet: " + response.toString());
                        newTweet.save();

                        getTargetFragment().onActivityResult(
                                getTargetRequestCode(),
                                Activity.RESULT_OK,
                                null);
                        progressDialog.dismiss();
                        ReplyFragment.this.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        mTvReplySend.setEnabled(true);

                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error replying the tweet: " + throwable.getLocalizedMessage(),
                                throwable);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        mTvReplySend.setEnabled(true);

                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error replying the tweet: " + throwable.getLocalizedMessage(),
                                throwable);
                        progressDialog.dismiss();
                    }
                });
            }
        });

        return v;
    }
}
