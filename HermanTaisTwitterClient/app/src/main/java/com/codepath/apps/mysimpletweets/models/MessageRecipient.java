package com.codepath.apps.mysimpletweets.models;

import java.util.List;

public class MessageRecipient {
    private User mUser;
    private Message mMostRecentMessage;
    private List<Message> mAllMessages;

    public MessageRecipient(User user, Message mostRecentMessage, List<Message> allMessages) {
        mUser = user;
        mMostRecentMessage = mostRecentMessage;
        mAllMessages = allMessages;
    }

    public User getUser() {
        return mUser;
    }

    public Message getMostRecentMessage() {
        return mMostRecentMessage;
    }

    public List<Message> getAllMessages() {
        return mAllMessages;
    }
}
