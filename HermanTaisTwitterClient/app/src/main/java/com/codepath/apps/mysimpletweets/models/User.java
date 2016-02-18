package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

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
