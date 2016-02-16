package com.codepath.apps.mysimpletweets.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * "user": {
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
public class User {
    private String name;
    private long uid;
    private String screenName;
    private String profileImageUrl;

    public static User fromJson(JSONObject json) {
        User u = new User();
        try {
            u.name = json.getString("name");
            u.uid = json.getLong("id");
            u.screenName = json.getString("screen_name");
            u.profileImageUrl = json.getString("profile_image_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return u;
    }

    public String getName() {
        return name;
    }

    public long getUid() {
        return uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
