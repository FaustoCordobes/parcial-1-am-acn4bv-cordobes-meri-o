package com.example.barista_app;

import androidx.fragment.app.FragmentManager;

import com.example.barista_app.strategy.HomeNavigationStrategy;
import com.example.barista_app.strategy.NavigationStrategy;

import java.util.HashMap;
import java.util.Map;

public class NavigationRouter {

    private final Map<Integer, NavigationStrategy> strategies = new HashMap<>();

    public NavigationRouter() {
        strategies.put(R.id.nav_home, new HomeNavigationStrategy());
        //strategies.put(R.id.nav_share, new ShareNavigationStrategy());
        //strategies.put(R.id.nav_favorite, new FavoriteNavigationStrategy());
        //strategies.put(R.id.nav_me, new MeNavigationStrategy());
    }

    public boolean navigateTo(int menuItemId, FragmentManager fragmentManager, int containerId) {
        NavigationStrategy strategy = strategies.get(menuItemId);

        if(strategy != null) {
            strategy.navigate(fragmentManager, containerId);
            return true;
        }
        return false;
    }
    public void navigateToDefault(FragmentManager fragmentManager, int containerId) {
        navigateTo(R.id.nav_home, fragmentManager, containerId);
    }
}
