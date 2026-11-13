package com.example.barista_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput;
    private EditText passwordInput;
    private EditText passwordConfirmInput;

    private Button registerButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        String urlImage = "https://images.unsplash.com/photo-1760175445000-0e01e193d1cd";
        ImageView loginLogo = findViewById(R.id.login_logo);
        ImageLoader.load(loginLogo, urlImage, R.drawable.ic_coffee_generic);

        emailInput = findViewById(R.id.login_email_input);
        passwordInput = findViewById(R.id.login_password_input);
        findViewById(R.id.login_button).setOnClickListener(v -> signIn());
        registerButton = findViewById(R.id.login_register_button);
        registerButton.setOnClickListener(v -> register());
        findViewById(R.id.login_register_button).setOnClickListener(v -> onRegisterShow());
        passwordConfirmInput = findViewById(R.id.login_password_confirm_input);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if(user != null) {
            user.reload()
                    .addOnSuccessListener(v -> goToMain())
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            auth.signOut();
                        } else {
                            goToMain();
                        }
                    });
        }
    }

    private void onRegisterShow() {
        if (passwordConfirmInput.getVisibility() != View.VISIBLE) {
            passwordConfirmInput.setVisibility(View.VISIBLE);
            return;
        }
        register();
    }

    private void signIn() {
        passwordConfirmInput.setVisibility(View.GONE);
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) { Toast.makeText(this, R.string.login_error_empty_email, Toast.LENGTH_SHORT).show(); return; }
        if (password.isEmpty()) { Toast.makeText(this, R.string.login_error_empty_password, Toast.LENGTH_SHORT).show(); return; }
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> goToMain())
                .addOnFailureListener(e -> Toast.makeText(this, R.string.login_error_invalid_credentials, Toast.LENGTH_SHORT).show());
    }

    private void register() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = passwordConfirmInput.getText().toString().trim();
        if (email.isEmpty()) { Toast.makeText(this, R.string.login_error_empty_email, Toast.LENGTH_SHORT).show(); return; }
        if (password.isEmpty()) { Toast.makeText(this, R.string.login_error_empty_password, Toast.LENGTH_SHORT).show(); return; }
        if (password.length() < 6) { Toast.makeText(this, R.string.login_error_short_password, Toast.LENGTH_SHORT).show(); return; }
        if(!password.equals(confirmPassword)) {Toast.makeText(this, R.string.login_error_password_mismatch, Toast.LENGTH_SHORT).show(); return;}
        registerButton.setEnabled(false);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> goToMain())
                .addOnFailureListener(e -> {
                    registerButton.setEnabled(true);
                    Toast.makeText(this, R.string.login_error_email_taken, Toast.LENGTH_SHORT).show();
                });
    }
    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}