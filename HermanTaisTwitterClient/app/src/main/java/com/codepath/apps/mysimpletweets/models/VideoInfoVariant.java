package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoInfoVariant implements Parcelable {
    private String mContentType;
    private String mUrl;

    public String getContentType() {
        return mContentType;
    }

    public String getUrl() {
        return mUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mContentType);
        dest.writeString(this.mUrl);
    }

    public VideoInfoVariant() {
    }

    protected VideoInfoVariant(Parcel in) {
        this.mContentType = in.readString();
        this.mUrl = in.readString();
    }

    public static final Parcelable.Creator<VideoInfoVariant> CREATOR = new Parcelable
            .Creator<VideoInfoVariant>() {
        public VideoInfoVariant createFromParcel(Parcel source) {
            return new VideoInfoVariant(source);
        }

        public VideoInfoVariant[] newArray(int size) {
            return new VideoInfoVariant[size];
        }
    };
}
