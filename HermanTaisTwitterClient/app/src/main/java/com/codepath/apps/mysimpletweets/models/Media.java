package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable {
    private long mId;
    private String mUrl;
    private String mType;
    private VideoInfo mVideoInfo;
    private String mMediaUrl;

    public long getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getType() {
        return mType;
    }

    public VideoInfo getVideoInfo() {
        return mVideoInfo;
    }

    public String getMediaUrl() {
        return mMediaUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeString(this.mUrl);
        dest.writeString(this.mType);
        dest.writeParcelable(this.mVideoInfo, 0);
        dest.writeString(this.mMediaUrl);
    }

    public Media() {
    }

    protected Media(Parcel in) {
        this.mId = in.readLong();
        this.mUrl = in.readString();
        this.mType = in.readString();
        this.mVideoInfo = in.readParcelable(VideoInfo.class.getClassLoader());
        this.mMediaUrl = in.readString();
    }

    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}
