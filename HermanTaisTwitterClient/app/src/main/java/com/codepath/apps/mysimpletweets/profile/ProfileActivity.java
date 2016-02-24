package com.codepath.apps.mysimpletweets.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.codepath.apps.mysimpletweets.R;

public class ProfileActivity extends AppCompatActivity {
    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, ProfileActivity.class);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }
}
