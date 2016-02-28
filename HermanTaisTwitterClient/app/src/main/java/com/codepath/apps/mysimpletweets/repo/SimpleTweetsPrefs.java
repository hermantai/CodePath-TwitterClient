package com.codepath.apps.mysimpletweets.repo;

import android.content.Context;
import android.preference.PreferenceManager;

import com.codepath.apps.mysimpletweets.Common;
import com.codepath.apps.mysimpletweets.models.User;

public class SimpleTweetsPrefs {
    private static final String PREF_USER = "user";
    public static final String PREF_NEWEST_HOME_FETCHED_ID = "newest_home_fetched_id";
    public static final String PREF_NEWEST_MENTIONS_FETCHED_ID = "newest_mentions_fetched_id";
    public static final String PREF_NEWEST_USER_TIMELINE_FETCHED_ID =
            "newest_user_timeline_fetched_id";
    public static final String PREF_NEWEST_LIKED_FETCHED_ID = "newest_liked_fetched_id";

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

    public static long getNewestHomeFetchedId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_NEWEST_HOME_FETCHED_ID, 0);
    }

    public static void setNewestHomeFetchedId(Context context, long id) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_NEWEST_HOME_FETCHED_ID, id)
                .apply();
    }

    public static long getNewestMentionsFetchedId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_NEWEST_MENTIONS_FETCHED_ID, 0);
    }

    public static void setNewestMentionsFetchedId(Context context, long id) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_NEWEST_MENTIONS_FETCHED_ID, id)
                .apply();
    }

    public static long getNewestUserTimelineFetchedId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_NEWEST_USER_TIMELINE_FETCHED_ID, 0);
    }

    public static void setNewestUserTimelineFetchedId(Context context, long id) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_NEWEST_USER_TIMELINE_FETCHED_ID, id)
                .apply();
    }

    public static long getNewestLikedFetchedId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(PREF_NEWEST_LIKED_FETCHED_ID, 0);
    }

    public static void setNewestLikedFetchedId(Context context, long id) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(PREF_NEWEST_LIKED_FETCHED_ID, id)
                .apply();
    }
}
