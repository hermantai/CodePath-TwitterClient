package com.codepath.apps.mysimpletweets.models;

import android.database.Cursor;
import android.os.Parcelable;

import java.util.Date;
import java.util.List;

/**
 * Since ActiveAndroid does not support inheritance very well, Mention and Tweet share an
 * interface TweetInterface, so most of the application code that can be agnostic
 * with the actual Mention or Tweet can deal with the Interface, which makes them more reusable.
 */
public interface TweetInterface extends Parcelable {
    Date getCreatedAt();

    User getUser();

    ExtendedEntities getExtendedEntities();

    String getText();

    boolean isHasMoreBefore();

    void setHasMoreBefore(boolean b);
    
    TweetInterface fetchTweetBefore();

    long getUid();

    void saveOne();

    void saveAll(List<TweetInterface> tweets);

    void loadFromCursor(Cursor cursor);

    List<TweetInterface> fetchRepliesTweets();

    boolean isRetweeted();

    boolean isFavorited();

    long getRetweetCount();

    long getFavoriteCount();
}
