package com.example.barista_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_USER_NAME = "extra_user_name";

    private EditText nameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameInput = findViewById(R.id.login_name_input);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String userName = nameInput.getText().toString().trim();

        if (userName.isEmpty()) {
            Toast.makeText(this, R.string.login_error_empty_name, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_USER_NAME, userName);
        startActivity(intent);
        finish();
    }
}