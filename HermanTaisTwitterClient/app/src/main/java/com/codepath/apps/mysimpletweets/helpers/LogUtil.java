package com.codepath.apps.mysimpletweets.helpers;

import android.util.Log;

import com.codepath.apps.mysimpletweets.BuildConfig;

public class LogUtil {
    // Each line of logcat message can only be around 4000 bytes, so the worst case is 1000
    // characters. See
    // <a href="http://stackoverflow.com/questions/8888654/android-set-max-length-of-logcat-messages">
    // this</a>.
    public static final int MAX_LINE_LENGTH = 1000;

    /**
     * Logs with Log.d with the msg chopped in chunks so each chunk has at most
     * {@code MAX_LINE_LENGTH} characters.
     */
    public static void d(String tag, String msg) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        int start = 0;
        int end = msg.length();

        while (start < end) {
            int n = Math.min(start + MAX_LINE_LENGTH, end);
            Log.d(tag, msg.substring(start, n));
            start += MAX_LINE_LENGTH;
        }
    }

    /**
     * Logs with Log.i with the msg chopped in chunks so each chunk has at most
     * {@code MAX_LINE_LENGTH} characters.
     */
    public static void i(String tag, String msg) {
        int start = 0;
        int end = msg.length();

        while (start < end) {
            int n = Math.min(start + MAX_LINE_LENGTH, end);
            Log.i(tag, msg.substring(start, n));
            start += MAX_LINE_LENGTH;
        }
    }

    public static void logForDemo(String msg){
        d("ForDemo", msg);
    }
}
