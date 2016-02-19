package com.codepath.apps.mysimpletweets.compose;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.models.Tweet;
import com.codepath.apps.mysimpletweets.twitter.TwitterApplication;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeFragment extends DialogFragment {
    @Bind(R.id.etComposeTweet) TextView mEtComposeTweet;
    @Bind(R.id.btnComposeSend) Button mBtnComposeSend;

    private TwitterClient mClient;

    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = TwitterApplication.getRestClient();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        getDialog().setTitle("Compose");

        View v = inflater.inflate(R.layout.fragment_compose, container, false);
        ButterKnife.bind(this, v);

        mBtnComposeSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnComposeSend.setEnabled(false);
                CharSequence tweet = mEtComposeTweet.getText();
                mClient.updateStatus(tweet, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        mBtnComposeSend.setEnabled(true);

                        Tweet newTweet = Tweet.fromJson(response);
                        Log.d(Common.INFO_TAG, response.toString());
                        ComposeFragment.this.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {
                        mBtnComposeSend.setEnabled(true);

                        Context context = getActivity();
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error retrieving tweets: " + throwable.getLocalizedMessage(),
                                throwable);
                    }
                });
            }
        });

        return v;
    }
}
