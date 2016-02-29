package com.codepath.apps.mysimpletweets.messages;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.SimpleTweetsApplication;
import com.codepath.apps.mysimpletweets.helpers.ErrorHandling;
import com.codepath.apps.mysimpletweets.models.Message;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.twitter.TwitterClient;
import com.codepath.apps.mysimpletweets.widgets.SimpleProgressDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessagesFragment extends Fragment {
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat(
            "yyyy/MMM/dd h:mm a");

    @Bind(R.id.rvMessages) RecyclerView mRvMessages;
    @Bind(R.id.etMessagesMessage) EditText mEtMessagesMessage;
    @Bind(R.id.btnMessagesSent) Button mBtnMessagesSent;

    private MessagesAdapter mAdapter;
    private TwitterClient mClient;

    private static final String ARG_MESSAGE_RECIPIENT = "message_recipient";
    private static final String ARG_USER = "user";

    private User mUser;

    public static MessagesFragment newInstance(User user, ArrayList<Message> messages) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);
        args.putParcelableArrayList(ARG_MESSAGE_RECIPIENT, messages);

        MessagesFragment fragment = new MessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = SimpleTweetsApplication.getRestClient();

        mUser = getArguments().getParcelable(ARG_USER);
        List<Message> messages = getArguments().getParcelableArrayList(ARG_MESSAGE_RECIPIENT);
        Collections.reverse(messages);
        mAdapter = new MessagesAdapter();
        mAdapter.addAll(messages);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRvMessages.setLayoutManager(llManager);
        mRvMessages.setAdapter(mAdapter);

        mBtnMessagesSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnMessagesSent.setEnabled(false);
                final ProgressDialog progressDialog = SimpleProgressDialog.createProgressDialog(
                        getActivity());

                String msg = mEtMessagesMessage.getText().toString().trim();
                if (!msg.isEmpty()) {
                    mClient.sendMessage(
                            mUser.getScreenName(),
                            msg,
                            new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers,
                                                      JSONObject response) {
                                    Message sentMessage = Message.fromJson(response);
                                    mAdapter.add(sentMessage);
                                    mBtnMessagesSent.setEnabled(true);
                                    mEtMessagesMessage.setText("");
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, String
                                        responseString, Throwable throwable) {
                                    ErrorHandling.handleError(
                                            getActivity(),
                                            Common.INFO_TAG,
                                            "Error sending message: "
                                                    + throwable.getLocalizedMessage(),
                                            throwable);
                                    mBtnMessagesSent.setEnabled(true);
                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable
                                        throwable, JSONObject errorResponse) {
                                    ErrorHandling.handleError(
                                            getActivity(),
                                            Common.INFO_TAG,
                                            "Error sending message: "
                                                    + throwable.getLocalizedMessage(),
                                            throwable);
                                    mBtnMessagesSent.setEnabled(true);
                                    progressDialog.dismiss();
                                }
                            }
                    );
                }
            }
        });

        return v;
    }

    abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        protected Activity mActivity;

        public MessageViewHolder(View itemView, Activity activity) {
            super(itemView);
            mActivity = activity;
        }

        protected abstract void bindMessage(Message message);
    }

    class ReceivedMessageViewHolder extends MessageViewHolder {
        @Bind(R.id.ivReceivedMessageSenderUserProfileImage)
        ImageView mIvReceivedMessageSenderUserProfileImage;

        @Bind(R.id.tvReceivedMessageReceivedMessage) TextView mTvReceivedMessageReceivedMessage;
        @Bind(R.id.tvReceivedMessageCreatedAt) TextView mTvReceivedMessageCreatedAt;

        public ReceivedMessageViewHolder(View itemView, Activity activity) {
            super(itemView, activity);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void bindMessage(Message message) {
            Glide.with(mActivity)
                    .load(message.getSender().getProfileImageUrl())
                    .into(mIvReceivedMessageSenderUserProfileImage);
            mTvReceivedMessageReceivedMessage.setText(message.getText());
            mTvReceivedMessageCreatedAt.setText(sDateFormat.format(message.getCreatedAt()));
        }
    }

    class SentMessageViewHolder extends MessageViewHolder {
        @Bind(R.id.tvSentMessageMessage) TextView mTvSentMessageMessage;
        @Bind(R.id.tvSentMessageCreatedAt) TextView mTvSentMessageCreatedAt;

        public SentMessageViewHolder(View itemView, Activity activity) {
            super(itemView, activity);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void bindMessage(Message message) {
            mTvSentMessageMessage.setText(message.getText());
            mTvSentMessageCreatedAt.setText(sDateFormat.format(message.getCreatedAt()));
        }
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private List<Message> mMessages = new ArrayList<>();

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MessageViewHolder vh = null;
            switch (viewType) {
                case 0:
                    View v = LayoutInflater.from(getActivity()).inflate(
                            R.layout.item_received_message, parent, false);
                    vh = new ReceivedMessageViewHolder(v, getActivity());
                    break;
                case 1:
                    View v2 = LayoutInflater.from(getActivity()).inflate(
                            R.layout.item_sent_message, parent, false);
                    vh = new SentMessageViewHolder(v2, getActivity());
                    break;
                default:
                    throw new RuntimeException("Impossible view type: " + viewType);
            }

            return vh;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = getItem(position);
            Log.d(Common.INFO_TAG, "mUser is " + mUser);
            Log.d(Common.INFO_TAG, "message is " + message);
            if (message.getSender().getUid() == mUser.getUid()) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            holder.bindMessage(getItem(position));
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }

        private void addAll(List<Message> messages){
            int oldLen = mMessages.size();
            mMessages.addAll(messages);
            notifyItemRangeInserted(oldLen, messages.size());
        }

        private void add(Message message) {
            mMessages.add(message);
            notifyItemInserted(mMessages.size());
        }

        private Message getItem(int position) {
            return mMessages.get(position);
        }
    }
}
