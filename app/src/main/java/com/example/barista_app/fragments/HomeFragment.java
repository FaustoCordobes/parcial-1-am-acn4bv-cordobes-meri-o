package com.example.barista_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.barista_app.R;
import com.example.barista_app.components.ButtonCalcTimer;

public class HomeFragment extends Fragment {

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.coffee_order_container, new CoffeeOrderFragment())
                    .commit();
        }

        ButtonCalcTimer btnTimerCalc = view.findViewById(R.id.btn_open_timer_calc);

        if (btnTimerCalc != null) {
            btnTimerCalc.setOnButtonClickListener(() -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TimerCalculatorFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }
}