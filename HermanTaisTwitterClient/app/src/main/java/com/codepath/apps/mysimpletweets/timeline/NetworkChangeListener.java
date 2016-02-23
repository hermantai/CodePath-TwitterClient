package com.codepath.apps.mysimpletweets.timeline;

import android.content.Context;
import android.content.Intent;

public interface NetworkChangeListener {
    void onNetworkChange(Context context, Intent intent);
}
