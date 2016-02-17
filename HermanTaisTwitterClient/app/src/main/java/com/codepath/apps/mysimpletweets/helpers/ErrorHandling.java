package com.codepath.apps.mysimpletweets.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ErrorHandling {
    public static void handleError(Context context, String tag, String msg, Throwable throwable) {
        Log.e(tag, msg, throwable);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
