package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class ExtendedEntities implements Parcelable {
    List<Media> mMedia;

    public List<Media> getMedia() {
        return mMedia;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mMedia);
    }

    public ExtendedEntities() {
    }

    protected ExtendedEntities(Parcel in) {
        this.mMedia = in.createTypedArrayList(Media.CREATOR);
    }

    public static final Parcelable.Creator<ExtendedEntities> CREATOR = new Parcelable
            .Creator<ExtendedEntities>() {
        public ExtendedEntities createFromParcel(Parcel source) {
            return new ExtendedEntities(source);
        }

        public ExtendedEntities[] newArray(int size) {
            return new ExtendedEntities[size];
        }
    };
}
