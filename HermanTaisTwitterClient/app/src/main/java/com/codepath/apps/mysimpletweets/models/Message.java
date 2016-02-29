package com.codepath.apps.mysimpletweets.models;

import com.codepath.apps.mysimpletweets.Common;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Message {
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
}
