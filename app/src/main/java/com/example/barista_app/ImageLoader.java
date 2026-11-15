package com.example.barista_app;

import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

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
                .placeholder(new ColorDrawable(ContextCompat.getColor(target.getContext(), R.color.ds_header_light)))
                .error(fallback)
                .into(target);
    }

}
