package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.codepath.apps.mysimpletweets.Common;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * "user": {
     "id": 119476949,
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
    }
 */
public class User implements Parcelable {
    private String mName;
    private long mUid;
    private String mScreenName;
    private String mProfileImageUrl;
    private String mDescription;
    private int mFollowersCount;
    @SerializedName("friends_count")
    private int mFollowingCount;

    public String getName() {
        return mName;
    }

    public long getUid() {
        return mUid;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public String getProfileImageUrl() {
        return mProfileImageUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getFollowersCount() {
        return mFollowersCount;
    }

    public int getFollowingCount() {
        return mFollowingCount;
    }

    public static User fromJson(JSONObject jsonObject) {
        Gson gson = Common.getGson();
        User user = gson.fromJson(jsonObject.toString(), User.class);
        return user;
    }

    public static ArrayList<User> fromJsonArray(JSONArray jsonArray) {
        ArrayList<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++ ) {
            try {
                users.add(User.fromJson(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return users;
    }

    public User() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeLong(this.mUid);
        dest.writeString(this.mScreenName);
        dest.writeString(this.mProfileImageUrl);
        dest.writeString(this.mDescription);
        dest.writeInt(this.mFollowersCount);
        dest.writeInt(this.mFollowingCount);
    }

    protected User(Parcel in) {
        this.mName = in.readString();
        this.mUid = in.readLong();
        this.mScreenName = in.readString();
        this.mProfileImageUrl = in.readString();
        this.mDescription = in.readString();
        this.mFollowersCount = in.readInt();
        this.mFollowingCount = in.readInt();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
