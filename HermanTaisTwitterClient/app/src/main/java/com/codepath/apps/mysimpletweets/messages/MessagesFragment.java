package com.codepath.apps.mysimpletweets.messages;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.Message;
import com.codepath.apps.mysimpletweets.models.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MessagesFragment extends Fragment {
    @Bind(R.id.rvMessages) RecyclerView mRvMessages;
    private MessagesAdapter mAdapter;

    private static final String ARG_MESSAGE_RECIPIENT = "message_recipient";
    private static final String ARG_USER = "user";

    private User mUser;

    public static MessagesFragment newInstance(User user, List<Message> messages) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, user);

        MessagesFragment fragment = new MessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = getArguments().getParcelable(ARG_USER);
        List<Message> messages = new ArrayList<>();
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

        return v;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        private Activity mActivity;

        public MessageViewHolder(View itemView, Activity activity) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mActivity = activity;
        }

        private void bindMessage(Message message) {
            // TODO
//            Glide.with(mActivity)
//                    .load(mUser.getProfileImageUrl())
//                    .into(mIvMessageRecipientUserProfileImage);
//            mTvMessageRecipientUserName.setText(mUser.getName());
//            mTvMessageRecipientMessage.setText(message.getText());
//            mTvMessageRecipientCreatedAt.setText(
//                    StringUtil.getRelativeTimeSpanString(
//                            message.getCreatedAt()));
        }
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> {
        private List<Message> mMessages = new ArrayList<>();

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(
                    R.layout.item_message_recipient, parent, false);

            return new MessageViewHolder(v, getActivity());
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

        private Message getItem(int position) {
            return mMessages.get(position);
        }
    }
}
