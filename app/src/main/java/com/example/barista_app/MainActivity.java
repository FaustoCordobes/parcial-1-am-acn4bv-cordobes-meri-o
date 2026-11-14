package com.example.barista_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.barista_app.data.MethodRepository;
import com.example.barista_app.models.CoffeeMethod;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private NavigationRouter router;
    private MethodRepository methodRepository;
    private View loadingView;
    private View bottomNavCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        router = new NavigationRouter();
        methodRepository = new MethodRepository();
        loadingView = findViewById(R.id.main_loading);
        bottomNavCard = findViewById(R.id.bottom_nav_card);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        bottomNav.setOnItemSelectedListener(i ->
                router.navigateTo(i.getItemId(), getSupportFragmentManager(), R.id.fragment_container)
        );

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            showContent(false);
            loadInitialData();
        } else {
            showContent(true);
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