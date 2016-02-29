package com.codepath.apps.mysimpletweets.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.codepath.apps.mysimpletweets.Common;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Message implements Parcelable {
    private long mUid;
    private User mSender;
    private User mRecipient;
    private Date mCreatedAt;
    private String mText;

    public long getUid() {
        return mUid;
    }

    public User getSender() {
        return mSender;
    }

    public User getRecipient() {
        return mRecipient;
    }

    public Date getCreatedAt() {
        return mCreatedAt;
    }

    public String getText() {
        return mText;
    }

    public static ArrayList<Message> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++ ) {
            try {
                messages.add(Message.fromJson(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        return messages;
    }

    public static Message fromJson(JSONObject jsonObject) {
        Gson gson = Common.getGson();
        Message message = gson.fromJson(jsonObject.toString(), Message.class);
        return message;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUid);
        dest.writeParcelable(this.mSender, 0);
        dest.writeParcelable(this.mRecipient, 0);
        dest.writeLong(mCreatedAt != null ? mCreatedAt.getTime() : -1);
        dest.writeString(this.mText);
    }

    @Override
    public String toString() {
        return "Message{" +
                "mUid=" + mUid +
                ", mSender=" + mSender +
                ", mRecipient=" + mRecipient +
                ", mCreatedAt=" + mCreatedAt +
                ", mText='" + mText + '\'' +
                '}';
    }

    public Message() {
    }

    protected Message(Parcel in) {
        this.mUid = in.readLong();
        this.mSender = in.readParcelable(User.class.getClassLoader());
        this.mRecipient = in.readParcelable(User.class.getClassLoader());
        long tmpMCreatedAt = in.readLong();
        this.mCreatedAt = tmpMCreatedAt == -1 ? null : new Date(tmpMCreatedAt);
        this.mText = in.readString();
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
