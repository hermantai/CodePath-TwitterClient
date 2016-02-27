package com.codepath.apps.mysimpletweets.models;

/*
  {
    "coordinates": null,
    "truncated": false,
    "created_at": "Tue Aug 28 21:16:23 +0000 2012",
    "favorited": false,
    "id_str": "240558470661799936",
    "in_reply_to_user_id_str": null,
    "entities": {
      "urls": [

      ],
      "hashtags": [

      ],
      "user_mentions": [

      ]
    },
    "text": "just another test",
    "contributors": null,
    "id": 240558470661799936,
    "retweet_count": 0,
    "in_reply_to_status_id_str": null,
    "geo": null,
    "retweeted": false,
    "in_reply_to_user_id": null,
    "place": null,
    "source": "<a href="//realitytechnicians.com%5C%22" rel="\"nofollow\"">OAuth Dancer Reborn</a>",
    "user": {
      "name": "OAuth Dancer",
      "profile_sidebar_fill_color": "DDEEF6",
      "profile_background_tile": true,
      "profile_sidebar_border_color": "C0DEED",
      "profile_image_url": "http://a0.twimg.com/profile_images/730275945/oauth-dancer_normal.jpg",
      "created_at": "Wed Mar 03 19:37:35 +0000 2010",
      "location": "San Francisco, CA",
      "follow_request_sent": false,
      "id_str": "119476949",
      "is_translator": false,
      "profile_link_color": "0084B4",
      "entities": {
        "url": {
          "urls": [
            {
              "expanded_url": null,
              "url": "http://bit.ly/oauth-dancer",
              "indices": [
                0,
                26
              ],
              "display_url": null
            }
          ]
        },
        "description": null
      },
      "default_profile": false,
      "url": "http://bit.ly/oauth-dancer",
      "contributors_enabled": false,
      "favourites_count": 7,
      "utc_offset": null,
      "profile_image_url_https": "https://si0.twimg.com/profile_images/730275945/oauth-dancer_normal.jpg",
      "id": 119476949,
      "listed_count": 1,
      "profile_use_background_image": true,
      "profile_text_color": "333333",
      "followers_count": 28,
      "lang": "en",
      "protected": false,
      "geo_enabled": true,
      "notifications": false,
      "description": "",
      "profile_background_color": "C0DEED",
      "verified": false,
      "time_zone": null,
      "profile_background_image_url_https": "https://si0.twimg.com/profile_background_images/80151733/oauth-dance.png",
      "statuses_count": 166,
      "profile_background_image_url": "http://a0.twimg.com/profile_background_images/80151733/oauth-dance.png",
      "default_profile_image": false,
      "friends_count": 14,
      "following": false,
      "show_all_inline_media": false,
      "screen_name": "oauth_dancer"
    },
    "in_reply_to_screen_name": null,
    "in_reply_to_status_id": null
  }
*/

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.codepath.apps.mysimpletweets.Common;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parse the json + store the data, encapsulate state logic or display logic
 */
@Table(name = "user_tweet")
public class UserTweet extends Model implements Parcelable, TweetInterface {
    // list out the attributes

    @Column(name = "text")
    private String mText;
    @Column(name = "uid",
            index = true,
            unique = true,
            onUniqueConflict = Column.ConflictAction.REPLACE)
    private long mUid;  // unique id for the tweet
    @Column(name = "user")
    private User mUser;
    @Column(name = "created_at")
    private Date mCreatedAt;
    @Column(name = "favorite_count")
    private long mFavoriteCount;
    @Column(name = "retweet_count")
    private long mRetweetCount;
    @Column(name = "favorited")
    private boolean mFavorited;
    @Column(name = "retweeted")
    private boolean mRetweeted;

    // Use ExtendedEntitiesTypeSerializer to serialize this
    @Column(name = "extended_entities")
    private ExtendedEntities mExtendedEntities;
    // Indicates there are potentially more tweets before this tweet (in terms of id) that they may
    // not be in the cache. This flag should not be in the model but I am lazy to create another
    // proxy model for persistence.
    @Column(name = "has_more_before")
    private boolean mHasMoreBefore = false;

    @Column(name = "in_reply_to_status_id", index = true)
    private long mInReplyToStatusId;

    public static UserTweet fromJson(JSONObject jsonObject) {
        Gson gson = Common.getGson();
        UserTweet userTweet = gson.fromJson(jsonObject.toString(), UserTweet.class);
        return userTweet;
    }

    public String getText() {
        return mText;
    }

    public long getUid() {
        return mUid;
    }

    @Override
    public void saveOne() {
        save();
    }

    @Override
    public void saveAll(List<TweetInterface> tweets) {
        ActiveAndroid.beginTransaction();
        try {
            for (TweetInterface tweet : tweets) {
                tweet.saveOne();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    @Override
    public List<TweetInterface> fetchRepliesTweets() {
        List<UserTweet> userTweets = new Select()
                .from(UserTweet.class)
                .where("in_reply_to_status_id = ?", getUid())
                .orderBy("uid desc")
                .execute();
        return new ArrayList<TweetInterface>(userTweets);
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public User getUser() {
        return mUser;
    }

    public long getFavoriteCount() {
        return mFavoriteCount;
    }

    public long getRetweetCount() {
        return mRetweetCount;
    }

    public boolean isFavorited() {
        return mFavorited;
    }

    public boolean isRetweeted() {
        return mRetweeted;
    }

    public ExtendedEntities getExtendedEntities() {
        return mExtendedEntities;
    }

    public boolean isHasMoreBefore() {
        return mHasMoreBefore;
    }

    public void setHasMoreBefore(boolean hasMoreBefore) {
        this.mHasMoreBefore = hasMoreBefore;
    }

    public long getInReplyToStatusId() {
        return mInReplyToStatusId;
    }

    public static ArrayList<UserTweet> fromJsonArray(JSONArray jsonArray) {
        ArrayList<UserTweet> userTweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++ ) {
            try {
                userTweets.add(UserTweet.fromJson(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return userTweets;
    }

    @Override
    public String toString() {
        return Common.getGson().toJson(this);
    }

    public UserTweet() {
    }

    public static Cursor fetchTweetsCursorForTimeline(long userUid) {
        String resultRecords = new Select()
                .from(UserTweet.class)
                        // Because we are using rawQuery, the argument should be put in rawQuery, not here
                .where("user like ?")
                .orderBy("uid desc")
                .toSql();
        Log.d(Common.INFO_TAG, resultRecords);
        return Cache.openDatabase().rawQuery(
                resultRecords, new String[]{ getLikeValueForMatchingUserId(userUid) });
    }

    public static Cursor fetchTweetsWithMediaCursorForTimeline(long userUid) {
        String resultRecords = new Select()
                .from(UserTweet.class)
                        // Because we are using rawQuery, the argument should be put in rawQuery, not here
                .where("user like ? and extended_entities is not null")
                .orderBy("uid desc")
                .toSql();
        Log.d(Common.INFO_TAG, resultRecords);
        return Cache.openDatabase().rawQuery(
                resultRecords, new String[]{ getLikeValueForMatchingUserId(userUid) });
    }

    public TweetInterface fetchTweetBefore() {
        long userUid = mUser.getUid();

        return new Select()
                .from(UserTweet.class)
                .where("uid < ? and user like ?", getUid(), getLikeValueForMatchingUserId(userUid))
                .orderBy("uid desc")
                .limit(1)
                .executeSingle();
    }

    public TweetInterface fetchTweetWithMediaBefore() {
        long userUid = mUser.getUid();

        return new Select()
                .from(UserTweet.class)
                .where(
                        "uid < ? and user like ? and extended_entities is not null",
                        getUid(),
                        getLikeValueForMatchingUserId(userUid))
                .orderBy("uid desc")
                .limit(1)
                .executeSingle();
    }

    private static String getLikeValueForMatchingUserId(long userUid) {
        return "%\"id\":" + userUid + "%";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mText);
        dest.writeLong(this.mUid);
        dest.writeParcelable(this.mUser, 0);
        dest.writeLong(mCreatedAt != null ? mCreatedAt.getTime() : -1);
        dest.writeLong(this.mFavoriteCount);
        dest.writeLong(this.mRetweetCount);
        dest.writeByte(mFavorited ? (byte) 1 : (byte) 0);
        dest.writeByte(mRetweeted ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.mExtendedEntities, 0);
        dest.writeByte(mHasMoreBefore ? (byte) 1 : (byte) 0);
        dest.writeLong(this.mInReplyToStatusId);
    }

    protected UserTweet(Parcel in) {
        this.mText = in.readString();
        this.mUid = in.readLong();
        this.mUser = in.readParcelable(User.class.getClassLoader());
        long tmpMCreatedAt = in.readLong();
        this.mCreatedAt = tmpMCreatedAt == -1 ? null : new Date(tmpMCreatedAt);
        this.mFavoriteCount = in.readLong();
        this.mRetweetCount = in.readLong();
        this.mFavorited = in.readByte() != 0;
        this.mRetweeted = in.readByte() != 0;
        this.mExtendedEntities = in.readParcelable(ExtendedEntities.class.getClassLoader());
        this.mHasMoreBefore = in.readByte() != 0;
        this.mInReplyToStatusId = in.readLong();
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
