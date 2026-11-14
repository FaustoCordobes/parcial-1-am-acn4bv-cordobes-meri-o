package com.example.barista_app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.barista_app.data.RecipeRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_METHOD_NAME = "recipe_method_name";
    public static final String EXTRA_WATER_ML = "recipe_water_ml";
    public static final String EXTRA_COFFEE_GRAMS = "recipe_coffee_grams";
    public static final String EXTRA_INTENSITY = "recipe_intensity";
    public static final String EXTRA_GRIND = "recipe_grind";
    public static final String EXTRA_NOTE = "recipe_note";
    public static final String EXTRA_OWNER_ID = "recipe_owner_id";
    public static final String EXTRA_SHARED = "recipe_shared";
    private final RecipeRepository recipeRepository = new RecipeRepository();
    private String recipeId;
    private EditText noteInput;
    private String ownerId;
    private boolean shared;
    private Button publishButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (recipeId == null) {
            finish();
            return;
        }
        ownerId = getIntent().getStringExtra(EXTRA_OWNER_ID);
        shared = getIntent().getBooleanExtra(EXTRA_SHARED, false);
        publishButton = findViewById(R.id.recipeDetailPublishButton);
        Button whatsappButton = findViewById(R.id.recipeDetailWhatsappButton);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isOwner = currentUser != null && currentUser.getUid().equals(ownerId);
        publishButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        updatePublishButton();
        publishButton.setOnClickListener(v -> togglePublish());
        whatsappButton.setOnClickListener(v -> shareToWhatsApp());

        TextView nameTextView = findViewById(R.id.recipeDetailName);
        TextView waterTextView = findViewById(R.id.recipeDetailWater);
        TextView coffeeTextView = findViewById(R.id.recipeDetailCoffee);
        TextView intensityTextView = findViewById(R.id.recipeDetailIntensity);
        TextView grindTextView = findViewById(R.id.recipeDetailGrind);
        noteInput = findViewById(R.id.recipeDetailNoteInput);
        Button saveNoteButton = findViewById(R.id.recipeDetailSaveButton);
        Button deleteButton = findViewById(R.id.recipeDetailDeleteButton);
        Button backButton = findViewById(R.id.recipeDetailBackButton);

        nameTextView.setText(getIntent().getStringExtra(EXTRA_METHOD_NAME));
        waterTextView.setText(getString(R.string.label_water_amount, getIntent().getIntExtra(EXTRA_WATER_ML, 0)));
        coffeeTextView.setText(getString(R.string.label_coffee_amount, getIntent().getIntExtra(EXTRA_COFFEE_GRAMS, 0)));
        intensityTextView.setText(getString(R.string.recipe_detail_intensity, getIntent().getStringExtra(EXTRA_INTENSITY)));
        grindTextView.setText(getString(R.string.method_detail_grind, getIntent().getStringExtra(EXTRA_GRIND)));
        noteInput.setText(getIntent().getStringExtra(EXTRA_NOTE));

        saveNoteButton.setOnClickListener(v -> saveNote());
        deleteButton.setOnClickListener(v -> deleteRecipe());
        backButton.setOnClickListener(v -> finish());
    }

    private void saveNote() {
        String note = noteInput.getText().toString().trim();
        recipeRepository.updateNote(recipeId, note, new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(RecipeDetailActivity.this, R.string.recipe_note_saved, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(RecipeDetailActivity.this, R.string.recipe_update_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteRecipe() {
        recipeRepository.deleteRecipe(recipeId, new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(RecipeDetailActivity.this, R.string.recipe_deleted, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(RecipeDetailActivity.this, R.string.recipe_update_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updatePublishButton() {
        publishButton.setText(shared ? R.string.recipe_unpublish : R.string.recipe_publish);
    }

    private void togglePublish() {
        boolean newValue = !shared;
        publishButton.setEnabled(false);
        recipeRepository.setShared(recipeId, newValue, new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                shared = newValue;
                updatePublishButton();
                publishButton.setEnabled(true);
                Toast.makeText(RecipeDetailActivity.this,
                        newValue ? R.string.recipe_published : R.string.recipe_unpublished,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception exception) {
                publishButton.setEnabled(true);
                Toast.makeText(RecipeDetailActivity.this, R.string.recipe_update_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void shareToWhatsApp() {
        Intent intent = getIntent();
        String text = getString(R.string.recipe_share_text,
                intent.getStringExtra(EXTRA_METHOD_NAME),
                intent.getIntExtra(EXTRA_WATER_ML, 0),
                intent.getIntExtra(EXTRA_COFFEE_GRAMS, 0),
                intent.getStringExtra(EXTRA_INTENSITY),
                intent.getStringExtra(EXTRA_GRIND));
        String note = noteInput.getText().toString().trim();
        if (!note.isEmpty()) {
            text += "\n" + getString(R.string.recipe_share_note, note);
        }

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        send.setPackage("com.whatsapp");
        try {
            startActivity(send);
        } catch (ActivityNotFoundException exception) {
            send.setPackage(null);
            startActivity(Intent.createChooser(send, getString(R.string.share_chooser_title)));
        }
    }

}
