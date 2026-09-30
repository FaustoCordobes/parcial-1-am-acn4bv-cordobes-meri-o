package com.example.barista_app;

import android.content.res.Resources;
import android.os.Bundle;
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
    private TextView step2TitleTextView;
    private EditText coffeeAmountInput;
    private RadioGroup intensityRadioGroup;
    private RadioGroup grindStatusRadioGroup;
    private RadioButton wholeBeanRadioButton;
    private TextView grindTypeResultTextView;
    private String selectedCoffeeType;
    private int selectedAmount;
    private String selectedIntensity;

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
        step2TitleTextView = findViewById(R.id.step2TitleTextView);
        coffeeAmountInput = findViewById(R.id.coffeeAmountInput);
        intensityRadioGroup = findViewById(R.id.intensityRadioGroup);
        grindStatusRadioGroup = findViewById(R.id.grindStatusRadioGroup);
        wholeBeanRadioButton = findViewById(R.id.wholeBeanRadioButton);
        grindTypeResultTextView = findViewById(R.id.grindTypeResultTextView);

        Button frenchPressButton = findViewById(R.id.frenchPressButton);
        Button mokaButton = findViewById(R.id.mokaButton);
        Button dripButton = findViewById(R.id.dripButton);
        Button step2BackButton = findViewById(R.id.step2BackButton);
        Button step2ContinueButton = findViewById(R.id.step2ContinueButton);
        Button step3BackButton = findViewById(R.id.step3BackButton);
        Button step3ContinueButton = findViewById(R.id.step3ContinueButton);

        frenchPressButton.setOnClickListener(v -> selectCoffeeMachine(getString(R.string.coffee_french_press)));
        mokaButton.setOnClickListener(v -> selectCoffeeMachine(getString(R.string.coffee_moka)));
        dripButton.setOnClickListener(v -> selectCoffeeMachine(getString(R.string.coffee_drip)));

        step2BackButton.setOnClickListener(v -> goBackToStep1());
        step2ContinueButton.setOnClickListener(v -> handleStep2Continue());

        step3BackButton.setOnClickListener(v -> goBackToStep2());
        step3ContinueButton.setOnClickListener(v -> handleStep3Continue());

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

    private void goBackToStep1() {
        step2Container.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.VISIBLE);
    }

    private void goBackToStep2() {
        step3Container.setVisibility(View.GONE);
        step2Container.setVisibility(View.VISIBLE);
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
        String grindStatusText = wholeBeanRadioButton.isChecked()
                ? getString(R.string.grind_status_whole_bean)
                : getString(R.string.grind_status_ground);
        String message = getString(R.string.msg_step3_summary, selectedCoffeeType, selectedAmount, selectedIntensity, grindStatusText);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
        idealButton.setText(R.string.btn_select);
        idealButton.setOnClickListener(v -> selectCoffeeMachine(getString(R.string.coffee_ideal)));

        idealCard.addView(idealImage);
        idealCard.addView(idealLabel);
        idealCard.addView(idealButton);

        optionsContainer.addView(idealCard);
    }
}