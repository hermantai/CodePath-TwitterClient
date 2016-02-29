package com.codepath.apps.mysimpletweets.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.tweetdetail.SingleFragmentActivity;

public class MessageRecipientsActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, MessageRecipientsActivity.class);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return MessageRecipientsFragment.newInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_activity_message_recipients));
    }
}
