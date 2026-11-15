package com.example.barista_app;

import android.content.Context;

public final class UiUtils {

    private UiUtils() {
    }

    public static int statusBarHeight(Context context) {
        int id = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? context.getResources().getDimensionPixelSize(id) : 0;
    }
}
