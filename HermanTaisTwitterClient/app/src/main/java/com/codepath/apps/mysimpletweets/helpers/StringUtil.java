package com.codepath.apps.mysimpletweets.helpers;

import android.text.format.DateUtils;

import java.util.Date;

public class StringUtil {
    public static String getRelativeTimeSpanString(Date startDate) {
        String s = DateUtils.getRelativeTimeSpanString(startDate.getTime()).toString();
        return s.replace("in ", "")
                .replace(" ago", "")
                .replace(" minutes", "m")
                .replace(" minute", "m")
                .replace(" hours", "h")
                .replace(" hour", "h")
                .replace("yesterday", "1d");
    }
}
