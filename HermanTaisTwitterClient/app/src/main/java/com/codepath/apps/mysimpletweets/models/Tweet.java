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
@Table(name = "tweet")
public class Tweet extends Model implements Parcelable, TweetInterface {
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

    public static Tweet fromJson(JSONObject jsonObject) {
        Gson gson = Common.getGson();
        Tweet tweet = gson.fromJson(jsonObject.toString(), Tweet.class);
        return tweet;
    }

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

    public void saveOne() {
        save();
    }

    public String getText() {
        return mText;
    }

    public long getUid() {
        return mUid;
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

    public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++ ) {
            try {
                tweets.add(Tweet.fromJson(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return tweets;
    }

    @Override
    public String toString() {
        return Common.getGson().toJson(this);
    }

    public Tweet() {
    }

    public static Cursor fetchNonRepliesTweetsCursor() {
        String resultRecords = new Select()
                .from(Tweet.class)
                .where("in_reply_to_status_id == 0")
                .orderBy("uid desc")
                .toSql();
        return Cache.openDatabase().rawQuery(resultRecords, new String[]{});
    }

    public List<TweetInterface> fetchRepliesTweets() {
        List<Tweet> tweets = new Select()
            .from(Tweet.class)
            .where("in_reply_to_status_id = ?", getUid())
            .orderBy("uid desc")
            .execute();
        return new ArrayList<TweetInterface>(tweets);
    }

    public TweetInterface fetchTweetBefore() {
        return new Select()
                .from(Tweet.class)
                .where("uid < ?", getUid())
                .orderBy("uid desc")
                .limit(1)
                .executeSingle();
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

    protected Tweet(Parcel in) {
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
