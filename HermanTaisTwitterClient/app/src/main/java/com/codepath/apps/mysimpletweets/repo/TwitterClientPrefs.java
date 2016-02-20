package com.codepath.apps.mysimpletweets.repo;

import android.content.Context;
import android.preference.PreferenceManager;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.models.User;

public class TwitterClientPrefs {
    private static final String PREF_USER = "user";

    public static User getUser(Context context) {
        String userJson = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_USER, null);

        if (userJson == null) {
            return null;
        } else {
            return Common.getGson().fromJson(userJson, User.class);
        }
    }

    public static void setUser(Context context, User user) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_USER, Common.getGson().toJson(user))
                .apply();
    }
}
