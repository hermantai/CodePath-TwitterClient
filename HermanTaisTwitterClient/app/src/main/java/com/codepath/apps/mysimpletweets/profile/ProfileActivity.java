package com.codepath.apps.mysimpletweets.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.mysimpletweets.R;
import com.codepath.apps.mysimpletweets.models.User;
import com.codepath.apps.mysimpletweets.timeline.SimpleSmartFragmentStatePagerAdapter;
import com.codepath.apps.mysimpletweets.timeline.UserMediaTimelineFragment;
import com.codepath.apps.mysimpletweets.timeline.UserTimelineFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {
    private static final String EXTRA_USER = "com.codepath.apps.mysimpletweets.user";

    @Bind(R.id.ivUserProfileImage) ImageView mIvUserProfileImage;
    @Bind(R.id.tvUserProfileName) TextView mTvUserProfileName;
    @Bind(R.id.tvUserProfileTagline) TextView mUserProfileTagline;
    @Bind(R.id.tvUserProfileFollowersCount) TextView mTvUserProfileFollowersCount;
    @Bind(R.id.tvUserProfileFollowingCount) TextView mTvUserProfileFollowingCount;
    @Bind(R.id.tlUserProfile) TabLayout mTlUserProfile;
    @Bind(R.id.vpUserProfile) ViewPager mVpUserProfile;

    private User mUser;

    public static Intent newIntent(Context context, User user) {
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra(EXTRA_USER, user);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUser = getIntent().getParcelableExtra(EXTRA_USER);
        ButterKnife.bind(this, this);

        Glide.with(this)
                .load(mUser.getProfileImageUrl())
                .into(mIvUserProfileImage);

        mTvUserProfileName.setText(mUser.getName());
        mUserProfileTagline.setText(mUser.getDescription());
        mTvUserProfileFollowersCount.setText(mUser.getFollowersCount() + " Followers");
        mTvUserProfileFollowingCount.setText(mUser.getFollowingCount() + " Following");

        getSupportActionBar().setTitle("@" + mUser.getScreenName());

        FragmentManager fm = getSupportFragmentManager();
        SimpleSmartFragmentStatePagerAdapter pagerAdapter =
                new SimpleSmartFragmentStatePagerAdapter(fm)
                        .addFragment(UserTimelineFragment.newInstance(mUser), "Tweets")
                        .addFragment(UserMediaTimelineFragment.newInstance(mUser), "Media")
                        .addFragment(UserTimelineFragment.newInstance(mUser), "Likes");

        mVpUserProfile.setAdapter(pagerAdapter);
        mTlUserProfile.setupWithViewPager(mVpUserProfile);
    }


}
