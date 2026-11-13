package com.example.barista_app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private NavigationRouter router;

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

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);

        if (savedInstanceState == null) {
            router.navigateToDefault(getSupportFragmentManager(), R.id.fragment_container);
            showGreeting();
        }

        bottomNav.setOnItemSelectedListener(i ->
                router.navigateTo(i.getItemId(), getSupportFragmentManager(), R.id.fragment_container)
        );
    }

    private void showGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String greeting = getString(R.string.main_greeting, user.getEmail());
            Toast.makeText(this, greeting, Toast.LENGTH_LONG).show();
        }

    }

}