package com.codepath.apps.mysimpletweets;

import com.codepath.apps.mysimpletweets.models.AndroidFieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Common {
    public static final String INFO_TAG = "MySimpleTweets.INFO";

    private static Gson sGson = new GsonBuilder()
            // "Tue Aug 28 21:16:23 +0000 2012"
            .setDateFormat("E MMM dd HH:mm:ss Z yyyy")
            .setFieldNamingStrategy(new AndroidFieldNamingStrategy())
            .create();

    public static Gson getGson() {
        return sGson;
    }
}
