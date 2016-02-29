package com.codepath.apps.mysimpletweets.messages;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;

import com.codepath.apps.mysimpletweets.models.Message;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.tweetdetail.SingleFragmentActivity;

import java.util.ArrayList;

public class MessagesActivity extends SingleFragmentActivity {
    private static final String EXTRA_USER = "com.codepath.apps.mysimpletweets.messages" +
            ".MessagesActivity.user";
    private static final String EXTRA_MESSAGES = "com.codepath.apps.mysimpletweets.messages" +
            ".MessagesActivity.messages";

    public static Intent newIntent(Context context, User user, ArrayList<Message> messages) {
        Intent i = new Intent(context, MessagesActivity.class);
        i.putExtra(EXTRA_USER, user);
        i.putParcelableArrayListExtra(EXTRA_MESSAGES, messages);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user = getIntent().getParcelableExtra(EXTRA_USER);
        setTitle(Html.fromHtml(
                String.format("<b>%s</b>", user.getName())
        ));
    }

    @Override
    protected Fragment createFragment() {
        User user = getIntent().getParcelableExtra(EXTRA_USER);
        ArrayList<Message> messages = getIntent().getParcelableArrayListExtra(EXTRA_MESSAGES);
        return MessagesFragment.newInstance(
                user,
                messages
        );
    }
}
