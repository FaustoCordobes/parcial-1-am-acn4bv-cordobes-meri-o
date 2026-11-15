package com.example.barista_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.barista_app.data.MethodRepository;
import com.example.barista_app.models.CoffeeMethod;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final String KEY_TAB = "selected_tab";
    private static final float ACTIVE_TAB_WEIGHT = 2.4f;

    private static final int[] TAB_IDS = {R.id.nav_home, R.id.nav_share, R.id.nav_favorite, R.id.nav_me};
    private static final int[] TAB_ICONS = {R.drawable.ic_nav_home, R.drawable.ic_nav_share,
            R.drawable.ic_nav_favorite, R.drawable.ic_nav_me};
    private static final int[] TAB_LABELS = {R.string.nav_home, R.string.nav_share,
            R.string.nav_favorite, R.string.nav_me};

    private NavigationRouter router;
    private MethodRepository methodRepository;
    private View loadingView;
    private View bottomNavCard;
    private LinearLayout navBar;
    private final View[] tabViews = new View[TAB_IDS.length];
    private int selectedTabId = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        if (settings.contains("dark_mode")) {
            AppCompatDelegate.setDefaultNightMode(settings.getBoolean("dark_mode", false)
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View header = findViewById(R.id.main_header);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            header.setPadding(header.getPaddingLeft(),
                    systemBars.top + getResources().getDimensionPixelSize(R.dimen.spacing_card),
                    header.getPaddingRight(), header.getPaddingBottom());
            return insets;
        });

        router = new NavigationRouter();
        methodRepository = new MethodRepository();
        loadingView = findViewById(R.id.main_loading);
        bottomNavCard = findViewById(R.id.bottom_nav_card);
        navBar = findViewById(R.id.nav_bar);

        if (savedInstanceState != null) {
            selectedTabId = savedInstanceState.getInt(KEY_TAB, R.id.nav_home);
        }
        buildNavBar();
        updateNavBar(false);
        applyScreenChrome(selectedTabId);

        TextView avatar = findViewById(R.id.main_header_avatar);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
            avatar.setText(currentUser.getEmail().substring(0, 1).toUpperCase(Locale.getDefault()));
        }
        avatar.setOnClickListener(v -> selectTab(R.id.nav_me));

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            showContent(false);
            loadInitialData();
        } else {
            showContent(true);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TAB, selectedTabId);
    }

    public void selectTab(int tabId) {
        if (tabId == selectedTabId) {
            return;
        }
        selectedTabId = tabId;
        updateNavBar(true);
        applyScreenChrome(tabId);
        router.navigateTo(tabId, getSupportFragmentManager(), R.id.fragment_container);
    }

    private void buildNavBar() {
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < TAB_IDS.length; i++) {
            int tabId = TAB_IDS[i];
            View tab = inflater.inflate(R.layout.item_nav_tab, navBar, false);
            ((ImageView) tab.findViewById(R.id.nav_tab_icon)).setImageResource(TAB_ICONS[i]);
            ((TextView) tab.findViewById(R.id.nav_tab_label)).setText(TAB_LABELS[i]);
            tab.setContentDescription(getString(TAB_LABELS[i]));
            tab.setOnClickListener(v -> selectTab(tabId));
            tabViews[i] = tab;
            navBar.addView(tab);
        }
    }

    private void updateNavBar(boolean animate) {
        if (animate) {
            TransitionManager.beginDelayedTransition(navBar);
        }
        for (int i = 0; i < TAB_IDS.length; i++) {
            View tab = tabViews[i];
            boolean active = TAB_IDS[i] == selectedTabId;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tab.getLayoutParams();
            params.weight = active ? ACTIVE_TAB_WEIGHT : 1f;
            tab.setLayoutParams(params);
            tab.setBackgroundResource(active ? R.drawable.bg_nav_active : 0);

            int color = MaterialColors.getColor(tab, active
                    ? com.google.android.material.R.attr.colorOnPrimary
                    : com.google.android.material.R.attr.colorOnSurfaceVariant);
            ImageView icon = tab.findViewById(R.id.nav_tab_icon);
            TextView label = tab.findViewById(R.id.nav_tab_label);
            icon.setColorFilter(color);
            label.setTextColor(color);
            label.setVisibility(active ? View.VISIBLE : View.GONE);
        }
    }

    private void applyScreenChrome(int tabId) {
        boolean isMe = tabId == R.id.nav_me;
        findViewById(R.id.main_header).setVisibility(isMe ? View.GONE : View.VISIBLE);
        View container = findViewById(R.id.fragment_container);
        if (isMe) {
            container.setBackground(null);
            container.setPadding(0, 0, 0, 0);
        } else {
            container.setBackgroundResource(R.drawable.bg_sheet_top);
            container.setPadding(0, getResources().getDimensionPixelSize(R.dimen.spacing_card), 0, 0);
        }
    }

    private void loadInitialData() {
        methodRepository.loadMethods(new MethodRepository.MethodsCallback() {
            @Override
            public void onSuccess(List<CoffeeMethod> methods) {
                startApp();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(MainActivity.this, R.string.methods_error, Toast.LENGTH_LONG).show();
                startApp();
            }
        });
    }

    private void startApp() {
        if (isDestroyed()) {
            return;
        }
        router.navigateToDefault(getSupportFragmentManager(), R.id.fragment_container);
        showGreeting();
        showContent(true);
    }

    private void showContent(boolean ready) {
        loadingView.setVisibility(ready ? View.GONE : View.VISIBLE);
        findViewById(R.id.fragment_container).setVisibility(ready ? View.VISIBLE : View.INVISIBLE);
        bottomNavCard.setVisibility(ready ? View.VISIBLE : View.GONE);
    }

    private void showGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String greeting = getString(R.string.main_greeting, user.getEmail());
            Toast.makeText(this, greeting, Toast.LENGTH_LONG).show();
        }
    }
}
