package com.example.barista_app.strategy;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.barista_app.fragments.HomeFragment;

public class HomeNavigationStrategy implements NavigationStrategy{


    @Override
    public void navigate(FragmentManager fragmentManager, int containerId) {
        fragmentManager.beginTransaction()
                .replace(containerId, new HomeFragment())
                .commit();
    }

}
