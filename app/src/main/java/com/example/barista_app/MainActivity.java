package com.example.barista_app;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout optionsContainer;
    private LinearLayout step2Container;
    private LinearLayout step3Container;
    private LinearLayout step4Container;
    private LinearLayout idealRecipesContainer;
    private LinearLayout savedRecipesListContainer;
    private TextView noRecipesTextView;
    private TextView step2TitleTextView;
    private EditText coffeeAmountInput;
    private RadioGroup intensityRadioGroup;
    private RadioGroup grindStatusRadioGroup;
    private RadioButton wholeBeanRadioButton;
    private TextView grindTypeResultTextView;
    private TextView waterAmountTextView;
    private TextView coffeeAmountResultTextView;
    private TextView estimatedTimeTextView;
    private TextView tipTextView;
    private TextView stopwatchDisplayTextView;
    private Button stopwatchStartButton;
    private String selectedCoffeeType;
    private int selectedAmount;
    private String selectedIntensity;

    private final Handler stopwatchHandler = new Handler(Looper.getMainLooper());
    private int elapsedSeconds = 0;
    private boolean isStopwatchRunning = false;
    private final Runnable stopwatchRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedSeconds++;
            updateStopwatchDisplay();
            stopwatchHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        optionsContainer = findViewById(R.id.optionsContainer);
        step2Container = findViewById(R.id.step2Container);
        step3Container = findViewById(R.id.step3Container);
        step4Container = findViewById(R.id.step4Container);
        idealRecipesContainer = findViewById(R.id.idealRecipesContainer);
        savedRecipesListContainer = findViewById(R.id.savedRecipesListContainer);
        noRecipesTextView = findViewById(R.id.noRecipesTextView);
        step2TitleTextView = findViewById(R.id.step2TitleTextView);
        coffeeAmountInput = findViewById(R.id.coffeeAmountInput);
        intensityRadioGroup = findViewById(R.id.intensityRadioGroup);
        grindStatusRadioGroup = findViewById(R.id.grindStatusRadioGroup);
        wholeBeanRadioButton = findViewById(R.id.wholeBeanRadioButton);
        grindTypeResultTextView = findViewById(R.id.grindTypeResultTextView);
        waterAmountTextView = findViewById(R.id.waterAmountTextView);
        coffeeAmountResultTextView = findViewById(R.id.coffeeAmountResultTextView);
        estimatedTimeTextView = findViewById(R.id.estimatedTimeTextView);
        tipTextView = findViewById(R.id.tipTextView);
        stopwatchDisplayTextView = findViewById(R.id.stopwatchDisplayTextView);
        stopwatchStartButton = findViewById(R.id.stopwatchStartButton);

        Button frenchPressButton = findViewById(R.id.frenchPressButton);
        Button mokaButton = findViewById(R.id.mokaButton);
        Button dripButton = findViewById(R.id.dripButton);
        Button step2BackButton = findViewById(R.id.step2BackButton);
        Button step2ContinueButton = findViewById(R.id.step2ContinueButton);
        Button step3BackButton = findViewById(R.id.step3BackButton);
        Button step3ContinueButton = findViewById(R.id.step3ContinueButton);
        Button stopwatchResetButton = findViewById(R.id.stopwatchResetButton);
        Button addToIdealButton = findViewById(R.id.addToIdealButton);
        Button step4BackButton = findViewById(R.id.step4BackButton);
        Button idealBackButton = findViewById(R.id.idealBackButton);

        frenchPressButton.setOnClickListener(v -> selectCoffeeMachine(getString(R.string.coffee_french_press)));
        mokaButton.setOnClickListener(v -> selectCoffeeMachine(getString(R.string.coffee_moka)));
        dripButton.setOnClickListener(v -> selectCoffeeMachine(getString(R.string.coffee_drip)));

        step2BackButton.setOnClickListener(v -> goBackToStep1());
        step2ContinueButton.setOnClickListener(v -> handleStep2Continue());

        step3BackButton.setOnClickListener(v -> goBackToStep2());
        step3ContinueButton.setOnClickListener(v -> handleStep3Continue());

        stopwatchStartButton.setOnClickListener(v -> toggleStopwatch());
        stopwatchResetButton.setOnClickListener(v -> resetStopwatch());
        addToIdealButton.setOnClickListener(v -> saveCurrentRecipeToIdeal());
        step4BackButton.setOnClickListener(v -> goBackToStep3());

        idealBackButton.setOnClickListener(v -> goBackToOptionsFromIdeal());

        grindStatusRadioGroup.setOnCheckedChangeListener((group, checkedId) ->
                updateGrindRecommendation(checkedId == R.id.wholeBeanRadioButton));

        addIdealCoffeeCard();
    }

    private void selectCoffeeMachine(String coffeeName) {
        selectedCoffeeType = coffeeName;
        step2TitleTextView.setText(getString(R.string.step2_title, coffeeName));
        optionsContainer.setVisibility(View.GONE);
        step2Container.setVisibility(View.VISIBLE);
    }

    private void showIdealRecipes() {
        optionsContainer.setVisibility(View.GONE);
        idealRecipesContainer.setVisibility(View.VISIBLE);
    }

    private void goBackToOptionsFromIdeal() {
        idealRecipesContainer.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.VISIBLE);
    }

    private void goBackToStep1() {
        step2Container.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.VISIBLE);
    }

    private void goBackToStep2() {
        step3Container.setVisibility(View.GONE);
        step2Container.setVisibility(View.VISIBLE);
    }

    private void goBackToStep3() {
        resetStopwatch();
        step4Container.setVisibility(View.GONE);
        step3Container.setVisibility(View.VISIBLE);
    }

    private void handleStep2Continue() {
        String amountText = coffeeAmountInput.getText().toString().trim();
        try {
            selectedAmount = Integer.parseInt(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.error_amount_required), Toast.LENGTH_SHORT).show();
            return;
        }
        selectedIntensity = getSelectedIntensity();
        step2Container.setVisibility(View.GONE);
        step3Container.setVisibility(View.VISIBLE);
        updateGrindRecommendation(wholeBeanRadioButton.isChecked());
    }

    private void handleStep3Continue() {
        step3Container.setVisibility(View.GONE);
        step4Container.setVisibility(View.VISIBLE);
        calculateRecipe();
    }

    private void calculateRecipe() {
        int ratio = getRatioForIntensity(selectedIntensity);
        int coffeeGrams = Math.round((float) selectedAmount / ratio);
        waterAmountTextView.setText(getString(R.string.label_water_amount, selectedAmount));
        coffeeAmountResultTextView.setText(getString(R.string.label_coffee_amount, coffeeGrams));
        estimatedTimeTextView.setText(getString(R.string.label_estimated_time, getEstimatedTime()));
        tipTextView.setText(getTipForCoffeeType());
    }

    private int getRatioForIntensity(String intensity) {
        if (intensity.equals(getString(R.string.intensity_soft))) {
            return 17;
        } else if (intensity.equals(getString(R.string.intensity_strong))) {
            return 13;
        }
        return 15;
    }

    private String getEstimatedTime() {
        if (selectedCoffeeType.equals(getString(R.string.coffee_french_press))) {
            return "4 min";
        } else if (selectedCoffeeType.equals(getString(R.string.coffee_moka))) {
            return "5 min";
        } else if (selectedCoffeeType.equals(getString(R.string.coffee_drip))) {
            return "3 min";
        }
        return getString(R.string.estimated_time_default);
    }

    private String getTipForCoffeeType() {
        if (selectedCoffeeType.equals(getString(R.string.coffee_french_press))) {
            return getString(R.string.tip_french_press);
        } else if (selectedCoffeeType.equals(getString(R.string.coffee_moka))) {
            return getString(R.string.tip_moka);
        } else if (selectedCoffeeType.equals(getString(R.string.coffee_drip))) {
            return getString(R.string.tip_drip);
        }
        return getString(R.string.tip_ideal);
    }

    private void toggleStopwatch() {
        if (isStopwatchRunning) {
            stopwatchHandler.removeCallbacks(stopwatchRunnable);
            stopwatchStartButton.setText(R.string.btn_start);
        } else {
            stopwatchHandler.postDelayed(stopwatchRunnable, 1000);
            stopwatchStartButton.setText(R.string.btn_pause);
        }
        isStopwatchRunning = !isStopwatchRunning;
    }

    private void resetStopwatch() {
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
        isStopwatchRunning = false;
        elapsedSeconds = 0;
        updateStopwatchDisplay();
        stopwatchStartButton.setText(R.string.btn_start);
    }

    private void updateStopwatchDisplay() {
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;
        String display = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        stopwatchDisplayTextView.setText(display);
    }

    private void saveCurrentRecipeToIdeal() {
        int ratio = getRatioForIntensity(selectedIntensity);
        int coffeeGrams = Math.round((float) selectedAmount / ratio);
        String summary = getString(R.string.saved_recipe_summary, selectedCoffeeType, selectedAmount, coffeeGrams, selectedIntensity);

        Resources resources = getResources();
        int cardPadding = resources.getDimensionPixelSize(R.dimen.spacing_card);

        TextView recipeView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, cardPadding);
        recipeView.setLayoutParams(params);
        recipeView.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
        recipeView.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background));
        recipeView.setText(summary);
        recipeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_label));

        savedRecipesListContainer.addView(recipeView);
        noRecipesTextView.setVisibility(View.GONE);

        Toast.makeText(this, getString(R.string.msg_added_to_ideal), Toast.LENGTH_SHORT).show();

        resetStopwatch();
        step4Container.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.VISIBLE);
    }

    private void updateGrindRecommendation(boolean isWholeBean) {
        if (!isWholeBean) {
            grindTypeResultTextView.setVisibility(View.GONE);
            return;
        }
        String grindMessage;
        if (selectedCoffeeType.equals(getString(R.string.coffee_french_press))) {
            grindMessage = getString(R.string.grind_type_french_press);
        } else if (selectedCoffeeType.equals(getString(R.string.coffee_moka))) {
            grindMessage = getString(R.string.grind_type_moka);
        } else if (selectedCoffeeType.equals(getString(R.string.coffee_drip))) {
            grindMessage = getString(R.string.grind_type_drip);
        } else {
            grindMessage = getString(R.string.grind_type_default);
        }

        grindTypeResultTextView.setText(grindMessage);
        grindTypeResultTextView.setVisibility(View.VISIBLE);
    }

    private String getSelectedIntensity() {
        int checkedId = intensityRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.intensitySoftRadioButton) {
            return getString(R.string.intensity_soft);
        } else if (checkedId == R.id.intensityStrongRadioButton) {
            return getString(R.string.intensity_strong);
        }
        return getString(R.string.intensity_medium);
    }

    private void addIdealCoffeeCard() {
        Resources resources = getResources();
        int cardPadding = resources.getDimensionPixelSize(R.dimen.spacing_card);
        int imageSize = resources.getDimensionPixelSize(R.dimen.card_image_size);
        float labelTextSize = resources.getDimension(R.dimen.text_size_label);

        LinearLayout idealCard = new LinearLayout(this);
        idealCard.setOrientation(LinearLayout.HORIZONTAL);
        idealCard.setGravity(Gravity.CENTER_VERTICAL);
        idealCard.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
        idealCard.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background));
        idealCard.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView idealImage = new ImageView(this);
        idealImage.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));
        idealImage.setImageResource(R.drawable.cafe_ideal);

        TextView idealLabel = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        labelParams.setMarginStart(cardPadding);
        idealLabel.setLayoutParams(labelParams);
        idealLabel.setText(R.string.coffee_ideal);
        idealLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);

        Button idealButton = new Button(this);
        idealButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        idealButton.setText(R.string.btn_view_recipes);
        idealButton.setOnClickListener(v -> showIdealRecipes());

        idealCard.addView(idealImage);
        idealCard.addView(idealLabel);
        idealCard.addView(idealButton);

        optionsContainer.addView(idealCard);


    }

}