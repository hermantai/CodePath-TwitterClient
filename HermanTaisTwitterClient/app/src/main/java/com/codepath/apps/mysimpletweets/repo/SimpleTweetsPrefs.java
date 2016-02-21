package com.codepath.apps.mysimpletweets.repo;

import android.content.Context;
import android.preference.PreferenceManager;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.models.User;

public class SimpleTweetsPrefs {
    private static final String PREF_USER = "user";
    private static final String PREF_NEWEST_FETCHED_ID = "newest_fetched_id";

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

    public static long getNewestFetchedId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_NEWEST_FETCHED_ID, 0);
    }

    public static void setNewestFetchedId(Context context, long id) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_NEWEST_FETCHED_ID, id)
                .apply();
    }
}
