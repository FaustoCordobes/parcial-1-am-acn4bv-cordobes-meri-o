package com.example.barista_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MethodDetailActivity extends AppCompatActivity {

    public static final String KEY_NAME = "method_name";
    public static final String KEY_DESCRIPTION = "method_description";
    public static final String KEY_RATIO = "method_ratio";
    public static final String KEY_GRIND = "method_grind";
    public static final String KEY_BREW_TIME = "method_brew_time";
    public static final String KEY_IMAGE = "method_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_method_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle methodData = getIntent().getExtras();
        if (methodData == null) {
            finish();
            return;
        }

        ImageView imageView = findViewById(R.id.methodDetailImage);
        TextView nameTextView = findViewById(R.id.methodDetailName);
        TextView descriptionTextView = findViewById(R.id.methodDetailDescription);
        TextView ratioTextView = findViewById(R.id.methodDetailRatio);
        TextView grindTextView = findViewById(R.id.methodDetailGrind);
        TextView timeTextView = findViewById(R.id.methodDetailTime);
        Button backButton = findViewById(R.id.methodDetailBackButton);

        imageView.setImageResource(methodData.getInt(KEY_IMAGE, R.drawable.ic_coffee_generic));
        nameTextView.setText(methodData.getString(KEY_NAME));
        descriptionTextView.setText(methodData.getString(KEY_DESCRIPTION));
        ratioTextView.setText(getString(R.string.method_detail_ratio, methodData.getInt(KEY_RATIO)));
        grindTextView.setText(getString(R.string.method_detail_grind, methodData.getString(KEY_GRIND)));
        timeTextView.setText(getString(R.string.method_detail_time, methodData.getString(KEY_BREW_TIME)));

        backButton.setOnClickListener(v -> finish());
    }
}
