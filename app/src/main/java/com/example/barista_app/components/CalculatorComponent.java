package com.example.barista_app.components;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.barista_app.R;

import java.util.Locale;

public class CalculatorComponent extends FrameLayout {

    private static final String TAG = "CALC";
    private static final double FACTOR_CONVERSOR = 16.0;
    private EditText etWater;
    private EditText etCoffee;
    private boolean updating = false;

    public CalculatorComponent(Context context) {
        super(context);
        init(context);
    }

    public CalculatorComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalculatorComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.component_calculator, this, true);

        etWater = findViewById(R.id.et_water);
        etCoffee = findViewById(R.id.et_coffee);
        Log.d(TAG, "init: water=" + etWater + " coffee=" + etCoffee);

        if (etWater == null || etCoffee == null) {
            throw new IllegalStateException(
                    "component_calculator.xml debe contener et_water y et_coffee");
        }

        syncInitialData();
        bidireccionalCoffeWaterCalc();
    }

    private void syncInitialData() {
        updating = true;
        try {
            String cleanWater = etWater.getText().toString().replace(',', '.').trim();
            if (!cleanWater.isEmpty()) {
                double water = Double.parseDouble(cleanWater);
                etCoffee.setText(String.format(Locale.US, "%.1f", water / FACTOR_CONVERSOR));
            }
        } catch (NumberFormatException ignored) {
        } finally {
            updating = false;
        }
    }

    private void bidireccionalCoffeWaterCalc() {
        etWater.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "agua cambió: " + s + " updating=" + updating);
                if (updating) return;
                updating = true;
                try {
                    String input = s.toString().replace(',', '.').trim();
                    if (input.isEmpty()) {
                        etCoffee.setText("");
                    } else {
                        try {
                            double water = Double.parseDouble(input);
                            etCoffee.setText(String.format(Locale.US, "%.1f", water / FACTOR_CONVERSOR));
                        } catch (NumberFormatException e) {
                            etCoffee.setText("");
                        }
                    }
                } finally {
                    updating = false;
                }
            }
        });

        etCoffee.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "café cambió: " + s + " updating=" + updating);
                if (updating) return;
                updating = true;
                try {
                    String input = s.toString().replace(',', '.').trim();
                    if (input.isEmpty()) {
                        etWater.setText("");
                    } else {
                        try {
                            double coffee = Double.parseDouble(input);
                            etWater.setText(String.format(Locale.US, "%.0f", coffee * FACTOR_CONVERSOR));
                        } catch (NumberFormatException e) {
                            etWater.setText("");
                        }
                    }
                } finally {
                    updating = false;
                }
            }
        });
    }

    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}