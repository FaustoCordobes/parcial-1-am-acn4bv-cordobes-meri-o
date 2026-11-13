package com.example.barista_app.strategy;

import androidx.fragment.app.FragmentManager;

import com.example.barista_app.fragments.ProfileFragment;

public class MeNavigationStrategy implements NavigationStrategy {

    @Override
    public void navigate(FragmentManager fragmentManager, int containerId) {
        fragmentManager.beginTransaction()
                .replace(containerId, new ProfileFragment())
                .commit();
    }

}