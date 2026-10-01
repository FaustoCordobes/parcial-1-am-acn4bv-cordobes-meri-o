package com.example.barista_app.strategy;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public interface NavigationStrategy {

    void navigate(FragmentManager fragmentManager, int containerId);
}
