package com.codepath.apps.mysimpletweets.widgets;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import com.codepath.apps.mysimpletweets.R;

public class SimpleProgressDialog {
    /**
     * Shows a {@link ProgressDialog} with only an indeterminate loading image. You should
     * call {@link ProgressDialog#dismiss()} to dismiss it.
     */
    public static ProgressDialog createProgressDialog(Context mContext) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.show();

        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_progress);
        return dialog;
    }
}
