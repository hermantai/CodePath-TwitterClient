package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

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
    private long mId;
    private String mScreenName;
    private String mProfileImageUrl;

    public static User fromJson(JSONObject jsonObject) {
        Gson gson = new GsonBuilder()
                // "Tue Aug 28 21:16:23 +0000 2012"
                .setDateFormat("E MMM dd HH:mm:ss Z yyyy")
                .setFieldNamingStrategy(new AndroidFieldNamingStrategy())
                .create();
        User user = gson.fromJson(jsonObject.toString(), User.class);
        return user;
    }



    public String getName() {
        return mName;
    }

    public long getId() {
        return mId;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public String getProfileImageUrl() {
        return mProfileImageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeLong(this.mId);
        dest.writeString(this.mScreenName);
        dest.writeString(this.mProfileImageUrl);
    }

    public User() {
    }

    protected User(Parcel in) {
        this.mName = in.readString();
        this.mId = in.readLong();
        this.mScreenName = in.readString();
        this.mProfileImageUrl = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
