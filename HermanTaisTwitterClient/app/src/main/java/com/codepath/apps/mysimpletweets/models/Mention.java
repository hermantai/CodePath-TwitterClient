package com.codepath.apps.mysimpletweets.models;

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
@Table(name = "mention")
public class Mention extends Model implements Parcelable, TweetInterface {
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

    public static Mention fromJson(JSONObject jsonObject) {
        Gson gson = Common.getGson();
        Mention mention = gson.fromJson(jsonObject.toString(), Mention.class);
        return mention;
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
        List<Mention> mentions = new Select()
            .from(Mention.class)
            .where("in_reply_to_status_id = ?", getUid())
            .orderBy("uid desc")
            .execute();
        return new ArrayList<TweetInterface>(mentions);
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

    public static ArrayList<Mention> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Mention> mentions = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++ ) {
            try {
                mentions.add(Mention.fromJson(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return mentions;
    }

    @Override
    public String toString() {
        return Common.getGson().toJson(this);
    }

    public Mention() {
    }

    public static Cursor fetchTweetsCursorForTimeline() {
        String resultRecords = new Select()
                .from(Mention.class)
                .orderBy("uid desc")
                .toSql();
        return Cache.openDatabase().rawQuery(resultRecords, new String[]{});
    }

    public TweetInterface fetchTweetBefore() {
        return new Select()
                .from(Mention.class)
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

    protected Mention(Parcel in) {
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

    public static final Creator<Mention> CREATOR = new Creator<Mention>() {
        public Mention createFromParcel(Parcel source) {
            return new Mention(source);
        }

        public Mention[] newArray(int size) {
            return new Mention[size];
        }
    };
}
