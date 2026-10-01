package com.example.barista_app.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.barista_app.R;

public class ButtonCalcTimer extends FrameLayout {

    public interface OnButtonClickListener {
        void onClick();
    }

    private OnButtonClickListener listener;

    public ButtonCalcTimer(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ButtonCalcTimer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ButtonCalcTimer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.component_button_to_calc_timer, this, true);

        setClickable(true);
        setFocusable(true);

        setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick();
            }
            Log.d("BTN", "XD");
        });
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        this.listener = listener;
    }
}