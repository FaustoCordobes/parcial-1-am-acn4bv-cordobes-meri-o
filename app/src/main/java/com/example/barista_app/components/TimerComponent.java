package com.example.barista_app.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.barista_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class TimerComponent extends MaterialCardView {

    private static final String TAG = "TIMER";
    private static final long TICK_MS = 100;
    private static final long DEFAULT_COUNTDOWN_MS = 3 * 60 * 1000L;

    private MaterialButton btnStopwatch, btnTimer, btnStart, btnReset;
    private TextView tvDisplay;
    private TextView tvHint;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean countdownMode = false;
    private boolean running = false;
    private long countdownTotalMs = DEFAULT_COUNTDOWN_MS;
    private long accumulatedMs = 0;
    private long startedAt = 0;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            updateDisplay();
            if (countdownMode && currentElapsed() >= countdownTotalMs) {
                finishCountdown();
                return;
            }
            handler.postDelayed(this, TICK_MS);
        }
    };

    public TimerComponent(Context context) {
        super(context);
    }

    public TimerComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimerComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        btnStopwatch = findViewById(R.id.btn_mode_stopwatch);
        btnTimer = findViewById(R.id.btn_mode_timer);
        btnStart = findViewById(R.id.btn_start_timer);
        btnReset = findViewById(R.id.btn_reset_timer);
        tvDisplay = findViewById(R.id.tv_timer_display);
        tvHint = findViewById(R.id.tv_timer_hint);

        Log.d(TAG, "onFinishInflate: start=" + btnStart + " display=" + tvDisplay);

        if (btnStopwatch == null || btnTimer == null || btnStart == null
                || btnReset == null || tvDisplay == null) {
            throw new IllegalStateException("component_timer.xml no tiene todos los ids esperados");
        }

        btnStopwatch.setOnClickListener(v -> setMode(false));
        btnTimer.setOnClickListener(v -> setMode(true));
        btnStart.setOnClickListener(v -> onStartPause());
        btnReset.setOnClickListener(v -> resetTimer());
        tvDisplay.setOnClickListener(v -> {
            if (countdownMode) showSetTimeDialog();
        });

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        tvDisplay.setBackgroundResource(outValue.resourceId);

        applyModeStyles();
        updateDisplay();
        updateEditHint();
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(ticker);
        super.onDetachedFromWindow();
    }

    private long currentElapsed() {
        return running
                ? accumulatedMs + (SystemClock.elapsedRealtime() - startedAt)
                : accumulatedMs;
    }

    private void onStartPause() {
        if (running) {
            // Pausar
            accumulatedMs = currentElapsed();
            running = false;
            handler.removeCallbacks(ticker);
            btnStart.setText("Reanudar");
            updateDisplay();
            updateEditHint();
        } else {
            if (countdownMode && accumulatedMs >= countdownTotalMs) {
                accumulatedMs = 0;
            }
            startedAt = SystemClock.elapsedRealtime();
            running = true;
            btnStart.setText("Pausar");
            updateEditHint();
            handler.removeCallbacks(ticker);
            handler.post(ticker);
        }
    }

    private void resetTimer() {
        handler.removeCallbacks(ticker);
        running = false;
        accumulatedMs = 0;
        btnStart.setText(R.string.iniciar);
        updateDisplay();
        updateEditHint();
    }

    private void setMode(boolean countdown) {
        if (countdown == countdownMode) return;
        countdownMode = countdown;
        resetTimer();
        applyModeStyles();
    }

    private void finishCountdown() {
        running = false;
        accumulatedMs = countdownTotalMs;
        handler.removeCallbacks(ticker);
        btnStart.setText(R.string.iniciar);
        updateDisplay();
        updateEditHint();
        beep();
    }

    private void beep() {
        try {
            final ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 800);
            handler.postDelayed(tone::release, 1000);
        } catch (RuntimeException e) {
            Log.w(TAG, "No se pudo reproducir el sonido", e);
        }
    }

    private void updateDisplay() {
        long seconds;
        if (countdownMode) {
            long remaining = Math.max(0, countdownTotalMs - currentElapsed());
            seconds = (remaining + 999) / 1000;
        } else {
            seconds = currentElapsed() / 1000;
        }
        tvDisplay.setText(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60));
    }

    private void updateEditHint() {
        boolean editable = countdownMode && !running;
        tvDisplay.setClickable(editable);

        if (!editable) {
            tvDisplay.setCompoundDrawablesRelative(null, null, null, null);
            tvDisplay.setContentDescription(null);
            return;
        }

        Context c = getContext();
        int size = (int) (28 * c.getResources().getDisplayMetrics().density);
        int primary = ContextCompat.getColor(c, R.color.primary_color);

        Drawable visible = makeEditIcon(c, size, primary, 160);
        Drawable spacer = makeEditIcon(c, size, primary, 0);
        if (visible == null || spacer == null) return;

        tvDisplay.setCompoundDrawablePadding((int) (8 * c.getResources().getDisplayMetrics().density));
        tvDisplay.setCompoundDrawablesRelative(spacer, null, visible, null);
        tvDisplay.setContentDescription("Tocar para cambiar el tiempo");
    }

    private Drawable makeEditIcon(Context c, int size, int color, int alpha) {
        Drawable d = ContextCompat.getDrawable(c, R.drawable.ic_edit);
        if (d == null) return null;
        d = DrawableCompat.wrap(d.mutate());
        DrawableCompat.setTint(d, color);
        d.setAlpha(alpha);
        d.setBounds(0, 0, size, size);
        return d;
    }

    private void applyModeStyles() {
        styleModeButton(btnStopwatch, !countdownMode);
        styleModeButton(btnTimer, countdownMode);
        if (tvHint != null) tvHint.setVisibility(countdownMode ? VISIBLE : GONE);
    }

    private void styleModeButton(MaterialButton button, boolean selected) {
        Context c = getContext();
        int primary = ContextCompat.getColor(c, R.color.primary_color);
        int secondary = ContextCompat.getColor(c, R.color.secondary_color);
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? primary : 0x00000000));
        button.setTextColor(selected ? secondary : primary);
        button.setStrokeColor(ColorStateList.valueOf(primary));
        button.setStrokeWidth((int) (1 * c.getResources().getDisplayMetrics().density));
    }

    private void showSetTimeDialog() {
        Context c = getContext();
        float d = c.getResources().getDisplayMetrics().density;
        int pad = (int) (24 * d);

        LinearLayout root = new LinearLayout(c);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad / 2, pad, 0);

        LinearLayout row = new LinearLayout(c);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        EditText etMin = new EditText(c);
        etMin.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMin.setHint("min");
        etMin.setGravity(Gravity.CENTER);
        etMin.setText(String.valueOf(countdownTotalMs / 60000));
        etMin.setSelectAllOnFocus(true);

        TextView sep = new TextView(c);
        sep.setText("  :  ");

        EditText etSec = new EditText(c);
        etSec.setInputType(InputType.TYPE_CLASS_NUMBER);
        etSec.setHint("seg");
        etSec.setGravity(Gravity.CENTER);
        etSec.setText(String.valueOf((countdownTotalMs / 1000) % 60));
        etSec.setSelectAllOnFocus(true);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(etMin, lp);
        row.addView(sep);
        row.addView(etSec, new LinearLayout.LayoutParams(lp));
        root.addView(row);

        LinearLayout presets = new LinearLayout(c);
        presets.setOrientation(LinearLayout.HORIZONTAL);
        presets.setGravity(Gravity.CENTER);
        presets.setPadding(0, (int) (12 * d), 0, 0);

        int[] presetMinutes = {1, 2, 3, 4, 5};
        for (int m : presetMinutes) {
            MaterialButton b = new MaterialButton(c, null,
                    android.R.attr.borderlessButtonStyle);
            b.setText(m + ":00");
            b.setAllCaps(false);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            b.setInsetTop(0);
            b.setInsetBottom(0);
            b.setPadding((int) (8 * d), 0, (int) (8 * d), 0);
            b.setOnClickListener(v -> {
                etMin.setText(String.valueOf(m));
                etSec.setText("0");
            });
            presets.addView(b, new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        }
        root.addView(presets);

        new MaterialAlertDialogBuilder(c)
                .setTitle("Tiempo de cuenta regresiva")
                .setView(root)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    long min = parseLong(etMin.getText().toString());
                    long sec = parseLong(etSec.getText().toString());
                    long total = (min * 60 + sec) * 1000L;
                    if (total > 0) {
                        countdownTotalMs = total;
                        resetTimer();
                    }
                })
                .show();
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}