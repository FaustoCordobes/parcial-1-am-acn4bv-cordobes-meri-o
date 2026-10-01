package com.example.barista_app.strategy;

import androidx.fragment.app.FragmentManager;

import com.example.barista_app.fragments.CoffeeOrderFragment;

public class FavoriteNavigationStrategy implements NavigationStrategy {

    @Override
    public void navigate(FragmentManager fragmentManager, int containerId) {
        fragmentManager.beginTransaction()
                .replace(containerId, new CoffeeOrderFragment())
                .commit();
    }

}