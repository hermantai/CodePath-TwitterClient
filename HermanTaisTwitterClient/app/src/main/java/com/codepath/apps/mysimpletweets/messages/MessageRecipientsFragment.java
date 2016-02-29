package com.codepath.apps.mysimpletweets.messages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.BuildConfig;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.helpers.LogUtil;
import com.codepath.apps.mysimpletweets.helpers.NetworkUtil;
import com.codepath.apps.mysimpletweets.helpers.StringUtil;
import com.codepath.apps.mysimpletweets.models.Message;
import com.codepath.apps.mysimpletweets.models.MessageRecipient;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.codepath.apps.mysimpletweets.widgets.SimpleProgressDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessageRecipientsFragment extends Fragment {
    @Bind(R.id.rvMessageRecipients) RecyclerView mRvMessageRecipients;
    private TwitterClient mClient;
    private List<Message> mReceivedMessages;
    private List<Message> mSentMessages;
    private ProgressDialog mProgressDialog;
    private MessageRecipientsAdapter mAdapter;

    public static MessageRecipientsFragment newInstance() {
        return new MessageRecipientsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = SimpleTweetsApplication.getRestClient();
        mAdapter = new MessageRecipientsAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message_recipients, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRvMessageRecipients.setLayoutManager(llManager);
        mRvMessageRecipients.setAdapter(mAdapter);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populate();
    }

    private void populate() {
        mProgressDialog = SimpleProgressDialog.createProgressDialog(getActivity());
        final Context context = getActivity();

        mClient.getReceivedMessages(
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(
                                    Common.INFO_TAG,
                                    "fetch received messages : " + response.toString());
                        }

                        // Deserialize JSON
                        // Create models
                        // Note that response sorts tweets in descending IDs
                        mReceivedMessages = Message.fromJsonArray(response);
                        populateRecipients();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error retrieving received messages: "
                                        + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(Common.INFO_TAG, responseString);

                        mProgressDialog.dismiss();
                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                populate();
                            }
                        });
                    }

                    @Override
                    public void onFailure(
                            int statusCode,
                            Header[] headers,
                            Throwable throwable,
                            JSONObject errorResponse) {
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error retrieving received messages: "
                                        + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(
                                Common.INFO_TAG,
                                errorResponse == null ? "" : errorResponse.toString());

                        mProgressDialog.dismiss();
                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                populate();
                            }
                        });
                    }
                });

        mClient.getSentMessages(
                new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        if (BuildConfig.DEBUG) {
                            LogUtil.d(
                                    Common.INFO_TAG,
                                    "fetch sent messages : " + response.toString());
                        }

                        // Deserialize JSON
                        // Create models
                        // Note that response sorts tweets in descending IDs
                        mSentMessages = Message.fromJsonArray(response);
                        populateRecipients();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error retrieving sent messages: "
                                        + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(Common.INFO_TAG, responseString);

                        mProgressDialog.dismiss();
                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                populate();
                            }
                        });
                    }

                    @Override
                    public void onFailure(
                            int statusCode,
                            Header[] headers,
                            Throwable throwable,
                            JSONObject errorResponse) {
                        ErrorHandling.handleError(
                                context,
                                Common.INFO_TAG,
                                "Error retrieving sent messages: "
                                        + throwable.getLocalizedMessage(),
                                throwable);
                        LogUtil.d(
                                Common.INFO_TAG,
                                errorResponse == null ? "" : errorResponse.toString());

                        mProgressDialog.dismiss();
                        showSnackBarForNetworkError(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                populate();
                            }
                        });
                    }
                });
    }

    private void populateRecipients() {
        if (mSentMessages != null && mReceivedMessages != null) {
            Map<Long, User> users = new HashMap<>();
            Map<Long, List<Message>> messagesForUsers = new HashMap<>();

            for (Message m : mSentMessages) {
                User recipient = m.getRecipient();
                if (!users.containsKey(recipient.getUid())) {
                    users.put(recipient.getUid(), recipient);
                    List<Message> messages = new ArrayList<>();
                    messages.add(m);
                    messagesForUsers.put(recipient.getUid(), messages);
                } else {
                    List<Message> messages = messagesForUsers.get(recipient.getUid());
                    messages.add(m);
                }
            }

            for (Message m : mReceivedMessages) {
                User sender = m.getSender();
                if (!users.containsKey(sender.getUid())) {
                    users.put(sender.getUid(), sender);
                    List<Message> messages = new ArrayList<>();
                    messages.add(m);
                    messagesForUsers.put(sender.getUid(), messages);
                } else {
                    List<Message> messages = messagesForUsers.get(sender.getUid());
                    messages.add(m);
                }
            }

            // sort all messages with created at in descending order
            for (List<Message> messages : messagesForUsers.values()) {
                Collections.sort(messages, new Comparator<Message>() {
                    @Override
                    public int compare(Message lhs, Message rhs) {
                        if (lhs.getCreatedAt()
                                .before(rhs.getCreatedAt())) {
                            return 1;
                        } else if (lhs.getCreatedAt()
                                .after(rhs.getCreatedAt())) {
                            return -1;
                        }
                        return 0;
                    }
                });
            }

            List<MessageRecipient> messageRecipients = new ArrayList<>();

            for (Map.Entry<Long, User> entry : users.entrySet()) {
                User user = entry.getValue();
                // The first message of the messages is the most recent message
                List<Message> messages = messagesForUsers.get(entry.getKey());
                Message m = messages.get(0);
                messageRecipients.add(new MessageRecipient(user, m, messages));
            }

            // Sort MessageRecipients with the message's createdAt in descending order
            Collections.sort(messageRecipients, new Comparator<MessageRecipient>() {
                @Override
                public int compare(MessageRecipient lhs, MessageRecipient rhs) {
                    if (lhs.getMostRecentMessage().getCreatedAt()
                            .before(rhs.getMostRecentMessage().getCreatedAt())) {
                        return 1;
                    } else if (lhs.getMostRecentMessage().getCreatedAt()
                            .after(rhs.getMostRecentMessage().getCreatedAt())) {
                        return -1;
                    }
                    return 0;
                }
            });

            mAdapter.clear();
            mAdapter.addAll(messageRecipients);

            mProgressDialog.dismiss();
        }
    }

    protected void showSnackBarForNetworkError(View.OnClickListener listener) {
        if(!NetworkUtil.isNetworkAvailable(getActivity())) {
            Snackbar.make(
                    mRvMessageRecipients,
                    "Network error!",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload", listener)
                    .setActionTextColor(Color.YELLOW)
                    .show();
        }
    }

    class MessageRecipientViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivMessageRecipientUserProfileImage)
        ImageView mIvMessageRecipientUserProfileImage;

        @Bind(R.id.tvMessageRecipientCreatedAt)
        TextView mTvMessageRecipientCreatedAt;

        @Bind(R.id.tvMessageRecipientMessage)
        TextView mTvMessageRecipientMessage;

        @Bind(R.id.tvMessageRecipientUserName)
        TextView mTvMessageRecipientUserName;

        private Activity mActivity;

        public MessageRecipientViewHolder(View itemView, Activity activity) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mActivity = activity;
        }

        private void bindMessageRecipient(final MessageRecipient messageRecipient) {
            Glide.with(mActivity)
                    .load(messageRecipient.getUser().getProfileImageUrl())
                    .into(mIvMessageRecipientUserProfileImage);
            mTvMessageRecipientUserName.setText(messageRecipient.getUser().getName());
            mTvMessageRecipientMessage.setText(messageRecipient.getMostRecentMessage().getText());
            mTvMessageRecipientCreatedAt.setText(
                    StringUtil.getRelativeTimeSpanString(
                            messageRecipient.getMostRecentMessage().getCreatedAt()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = MessagesActivity.newIntent(
                            mActivity,
                            messageRecipient.getUser(),
                            new ArrayList<Message>(messageRecipient.getAllMessages()));
                    startActivity(i);
                }
            });
        }
    }

    class MessageRecipientsAdapter extends RecyclerView.Adapter<MessageRecipientViewHolder> {
        private List<MessageRecipient> mMessageRecipients = new ArrayList<>();

        @Override
        public MessageRecipientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(
                    R.layout.item_message_recipient, parent, false);

            return new MessageRecipientViewHolder(v, getActivity());
        }

        @Override
        public void onBindViewHolder(MessageRecipientViewHolder holder, int position) {
            holder.bindMessageRecipient(getItem(position));
        }

        @Override
        public int getItemCount() {
            return mMessageRecipients.size();
        }

        private void addAll(List<MessageRecipient> messageRecipients){
            int oldLen = mMessageRecipients.size();
            mMessageRecipients.addAll(messageRecipients);
            notifyItemRangeInserted(oldLen, messageRecipients.size());
        }

        private MessageRecipient getItem(int position) {
            return mMessageRecipients.get(position);
        }

        public void clear() {
            mMessageRecipients.clear();
            notifyDataSetChanged();
        }
    }
}