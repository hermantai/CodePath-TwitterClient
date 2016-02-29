package com.codepath.apps.mysimpletweets.users;

import android.app.Activity;
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
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.helpers.NetworkUtil;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UsersFragment extends Fragment {
    @Bind(R.id.rvUsers) RecyclerView mRvUsers;

    private UsersAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new UsersAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        ButterKnife.bind(this, v);

        LinearLayoutManager llManager = new LinearLayoutManager(getActivity());
        llManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRvUsers.setLayoutManager(llManager);
        mRvUsers.setAdapter(mAdapter);

        return v;
    }

    protected void addUsers(List<User> users) {
        mAdapter.addAll(users);
    }

    protected void showSnackBarForNetworkError(View.OnClickListener listener) {
        if(!NetworkUtil.isNetworkAvailable(getActivity())) {
            Snackbar.make(
                    mRvUsers,
                    "Network error!",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Reload", listener)
                    .setActionTextColor(Color.YELLOW)
                    .show();
        }
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.ivItemUserProfileImage) ImageView mIvItemUserProfileImage;
        @Bind(R.id.tvItemUserUserName) TextView mTvItemUserUserName;
        @Bind(R.id.tvItemUserUserScreenName) TextView mTvItemUserUserScreenName;

        private Activity mActivity;

        public UserViewHolder(View itemView, Activity activity) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mActivity = activity;
        }

        private void bindUser(final User user) {
            Glide.with(mActivity)
                    .load(user.getProfileImageUrl())
                    .into(mIvItemUserProfileImage);
            mIvItemUserProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = ProfileActivity.newIntent(mActivity, user);
                    mActivity.startActivity(i);
                }
            });
            mTvItemUserUserName.setText(user.getName());
            mTvItemUserUserScreenName.setText(user.getScreenName());
        }
    }

    class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {
        private List<User> mUsers = new ArrayList<>();

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_user, parent, false);

            return new UserViewHolder(v, getActivity());
        }

        @Override
        public void onBindViewHolder(UserViewHolder holder, int position) {
            holder.bindUser(getItem(position));
        }

        @Override
        public int getItemCount() {
            return mUsers.size();
        }

        private void addAll(List<User> users){
            int oldLen = mUsers.size();
            mUsers.addAll(users);
            notifyItemRangeInserted(oldLen, users.size());
        }

        private User getItem(int position) {
            return mUsers.get(position);
        }
    }
}
