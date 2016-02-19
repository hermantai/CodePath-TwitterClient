package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class VideoInfo implements Parcelable {
    private List<VideoInfoVariant> mVariants;

    public List<VideoInfoVariant> getVariants() {
        return mVariants;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mVariants);
    }

    public VideoInfo() {
    }

    protected VideoInfo(Parcel in) {
        this.mVariants = in.createTypedArrayList(VideoInfoVariant.CREATOR);
    }

    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>
            () {
        public VideoInfo createFromParcel(Parcel source) {
            return new VideoInfo(source);
        }

        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
}
