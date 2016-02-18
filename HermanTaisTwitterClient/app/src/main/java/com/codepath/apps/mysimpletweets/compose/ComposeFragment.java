package com.codepath.apps.mysimpletweets.compose;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codepath.apps.mysimpletweets.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ComposeFragment extends DialogFragment {
    @Bind(R.id.etComposeTweet) TextView mTvComposeTweet;
    @Bind(R.id.btnComposeSend) Button mBtnComposeSend;

    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        getDialog().setTitle("Compose");

        View v = inflater.inflate(R.layout.fragment_compose, container, false);
        ButterKnife.bind(this, v);

        return v;
    }
}
