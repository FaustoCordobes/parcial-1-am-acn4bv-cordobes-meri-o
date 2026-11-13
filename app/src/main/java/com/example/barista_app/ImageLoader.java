package com.example.barista_app;

import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
public final class ImageLoader {

    private ImageLoader() {

    }

    public static void load (ImageView target, String url, @DrawableRes int fallback) {
        if(target == null) return;

        if(url == null || url.trim().isEmpty()) {
            target.setImageResource(fallback);
            return;
        }


        Glide.with(target)
                .load(url)
                .placeholder(fallback)
                .error(fallback)
                .into(target);
    }

}
