package com.example.barista_app.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.barista_app.LoginActivity;
import com.example.barista_app.MethodDetailActivity;
import com.example.barista_app.R;
import com.example.barista_app.data.MethodRepository;
import com.example.barista_app.data.RecipeRepository;
import com.example.barista_app.models.CoffeeMethod;
import com.example.barista_app.models.Recipe;

import java.util.List;
import java.util.Locale;

public class CoffeeOrderFragment extends Fragment {

    private View optionsContainer;
    private LinearLayout methodCardsContainer;
    private TextView methodsLoadingTextView;
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

    private final MethodRepository methodRepository = new MethodRepository();
    private final RecipeRepository recipeRepository = new RecipeRepository();
    private CoffeeMethod selectedMethod;
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

    public CoffeeOrderFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_coffee_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        optionsContainer = view.findViewById(R.id.optionsContainer);
        methodCardsContainer = view.findViewById(R.id.methodCardsContainer);
        methodsLoadingTextView = view.findViewById(R.id.methodsLoadingTextView);
        step2Container = view.findViewById(R.id.step2Container);
        step3Container = view.findViewById(R.id.step3Container);
        step4Container = view.findViewById(R.id.step4Container);
        idealRecipesContainer = view.findViewById(R.id.idealRecipesContainer);
        savedRecipesListContainer = view.findViewById(R.id.savedRecipesListContainer);
        noRecipesTextView = view.findViewById(R.id.noRecipesTextView);
        step2TitleTextView = view.findViewById(R.id.step2TitleTextView);
        coffeeAmountInput = view.findViewById(R.id.coffeeAmountInput);
        intensityRadioGroup = view.findViewById(R.id.intensityRadioGroup);
        grindStatusRadioGroup = view.findViewById(R.id.grindStatusRadioGroup);
        wholeBeanRadioButton = view.findViewById(R.id.wholeBeanRadioButton);
        grindTypeResultTextView = view.findViewById(R.id.grindTypeResultTextView);
        waterAmountTextView = view.findViewById(R.id.waterAmountTextView);
        coffeeAmountResultTextView = view.findViewById(R.id.coffeeAmountResultTextView);
        estimatedTimeTextView = view.findViewById(R.id.estimatedTimeTextView);
        tipTextView = view.findViewById(R.id.tipTextView);
        stopwatchDisplayTextView = view.findViewById(R.id.stopwatchDisplayTextView);
        stopwatchStartButton = view.findViewById(R.id.stopwatchStartButton);

        Button step2BackButton = view.findViewById(R.id.step2BackButton);
        Button step2ContinueButton = view.findViewById(R.id.step2ContinueButton);
        Button step3BackButton = view.findViewById(R.id.step3BackButton);
        Button step3ContinueButton = view.findViewById(R.id.step3ContinueButton);
        Button stopwatchResetButton = view.findViewById(R.id.stopwatchResetButton);
        Button addToIdealButton = view.findViewById(R.id.addToIdealButton);
        Button step4BackButton = view.findViewById(R.id.step4BackButton);
        Button idealBackButton = view.findViewById(R.id.idealBackButton);

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

        loadMethods();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
    }

    private void loadMethods() {
        methodRepository.loadMethods(new MethodRepository.MethodsCallback() {
            @Override
            public void onSuccess(List<CoffeeMethod> methods) {
                if (!isAdded()) {
                    return;
                }
                methodsLoadingTextView.setVisibility(View.GONE);
                for (CoffeeMethod method : methods) {
                    methodCardsContainer.addView(createMethodCard(method));
                }
                addIdealCoffeeCard();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }
                methodsLoadingTextView.setText(R.string.methods_error);
                Toast.makeText(requireContext(), R.string.methods_error, Toast.LENGTH_LONG).show();
                addIdealCoffeeCard();
            }
        });
    }

    private LinearLayout createMethodCard(CoffeeMethod method) {
        Resources resources = getResources();
        int cardPadding = resources.getDimensionPixelSize(R.dimen.spacing_card);
        int imageSize = resources.getDimensionPixelSize(R.dimen.card_image_size);
        float labelTextSize = resources.getDimension(R.dimen.text_size_label);

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
        card.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background));
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, cardPadding);
        card.setLayoutParams(cardParams);

        ImageView image = new ImageView(requireContext());
        image.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));
        image.setImageResource(getImageForMethod(method.getId()));

        LinearLayout textColumn = new LinearLayout(requireContext());
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams columnParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        columnParams.setMarginStart(cardPadding);
        textColumn.setLayoutParams(columnParams);

        TextView label = new TextView(requireContext());
        label.setText(method.getName());
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.coffee_brown));
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);

        TextView detailLink = new TextView(requireContext());
        detailLink.setText(R.string.btn_view_detail);
        detailLink.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_caramel_dark));
        detailLink.setPaintFlags(detailLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        detailLink.setPadding(0, cardPadding / 2, 0, 0);
        detailLink.setOnClickListener(v -> openMethodDetail(method));

        textColumn.addView(label);
        textColumn.addView(detailLink);

        Button selectButton = new Button(requireContext());
        selectButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        selectButton.setText(R.string.btn_select);
        selectButton.setOnClickListener(v -> selectCoffeeMachine(method));

        image.setOnClickListener(v -> openMethodDetail(method));

        card.addView(image);
        card.addView(textColumn);
        card.addView(selectButton);

        return card;
    }

    private void openMethodDetail(CoffeeMethod method) {
        Bundle methodData = new Bundle();
        methodData.putString(MethodDetailActivity.KEY_NAME, method.getName());
        methodData.putString(MethodDetailActivity.KEY_DESCRIPTION, method.getDescription());
        methodData.putInt(MethodDetailActivity.KEY_RATIO, method.getRatio());
        methodData.putString(MethodDetailActivity.KEY_GRIND, method.getGrind());
        methodData.putString(MethodDetailActivity.KEY_BREW_TIME, method.getBrewTime());
        methodData.putInt(MethodDetailActivity.KEY_IMAGE, getImageForMethod(method.getId()));

        Intent intent = new Intent(requireContext(), MethodDetailActivity.class);
        intent.putExtras(methodData);
        startActivity(intent);
    }

    private int getImageForMethod(String methodId) {
        switch (methodId) {
            case "prensa":
                return R.drawable.prensa_francesa;
            case "moka":
                return R.drawable.moka_italiana;
            case "filtro":
                return R.drawable.cafetera_de_goteo;
            default:
                return R.drawable.ic_coffee_generic;
        }
    }

    private void selectCoffeeMachine(CoffeeMethod method) {
        selectedMethod = method;
        step2TitleTextView.setText(getString(R.string.step2_title, method.getName()));
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
            Toast.makeText(requireContext(), getString(R.string.error_amount_required), Toast.LENGTH_SHORT).show();
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
        estimatedTimeTextView.setText(getString(R.string.label_estimated_time, selectedMethod.getBrewTime()));
        tipTextView.setText(getTipForMethod());
    }

    private int getRatioForIntensity(String intensity) {
        int baseRatio = selectedMethod.getRatio();
        if (intensity.equals(getString(R.string.intensity_soft))) {
            return baseRatio + 2;
        } else if (intensity.equals(getString(R.string.intensity_strong))) {
            return baseRatio - 3;
        }
        return baseRatio;
    }

    private String getTipForMethod() {
        switch (selectedMethod.getId()) {
            case "prensa":
                return getString(R.string.tip_french_press);
            case "moka":
                return getString(R.string.tip_moka);
            case "filtro":
                return getString(R.string.tip_drip);
            default:
                return getString(R.string.tip_default);
        }
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
        String summary = getString(R.string.saved_recipe_summary, selectedMethod.getName(), selectedAmount, coffeeGrams, selectedIntensity);

        Resources resources = getResources();
        int cardPadding = resources.getDimensionPixelSize(R.dimen.spacing_card);

        TextView recipeView = new TextView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, cardPadding);
        recipeView.setLayoutParams(params);
        recipeView.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
        recipeView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background));
        recipeView.setTextColor(ContextCompat.getColor(requireContext(), R.color.coffee_brown));
        recipeView.setText(summary);
        recipeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_label));

        savedRecipesListContainer.addView(recipeView);
        noRecipesTextView.setVisibility(View.GONE);

        saveRecipeToFirestore(coffeeGrams);

        resetStopwatch();
        step4Container.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.VISIBLE);
    }

    private void saveRecipeToFirestore(int coffeeGrams) {
        Recipe recipe = new Recipe();
        recipe.setUserName(requireActivity().getIntent().getStringExtra(LoginActivity.EXTRA_USER_NAME));
        recipe.setMethodId(selectedMethod.getId());
        recipe.setMethodName(selectedMethod.getName());
        recipe.setWaterMl(selectedAmount);
        recipe.setCoffeeGrams(coffeeGrams);
        recipe.setIntensity(selectedIntensity);
        recipe.setGrind(selectedMethod.getGrind());
        recipe.setNote("");
        recipe.setCreatedAt(System.currentTimeMillis());

        recipeRepository.saveRecipe(recipe, new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.msg_added_to_ideal, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.recipe_save_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateGrindRecommendation(boolean isWholeBean) {
        if (!isWholeBean) {
            grindTypeResultTextView.setVisibility(View.GONE);
            return;
        }
        String grind = selectedMethod.getGrind().toLowerCase(Locale.getDefault());
        grindTypeResultTextView.setText(getString(R.string.grind_recommendation, selectedMethod.getName(), grind));
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

        LinearLayout idealCard = new LinearLayout(requireContext());
        idealCard.setOrientation(LinearLayout.HORIZONTAL);
        idealCard.setGravity(Gravity.CENTER_VERTICAL);
        idealCard.setPadding(cardPadding, cardPadding, cardPadding, cardPadding);
        idealCard.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.card_background));
        idealCard.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ImageView idealImage = new ImageView(requireContext());
        idealImage.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));
        idealImage.setImageResource(R.drawable.cafe_ideal);

        TextView idealLabel = new TextView(requireContext());
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f);
        labelParams.setMarginStart(cardPadding);
        idealLabel.setLayoutParams(labelParams);
        idealLabel.setText(R.string.coffee_ideal);
        idealLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.coffee_brown));
        idealLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);

        Button idealButton = new Button(requireContext());
        idealButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        idealButton.setText(R.string.btn_view_recipes);
        idealButton.setOnClickListener(v -> showIdealRecipes());

        idealCard.addView(idealImage);
        idealCard.addView(idealLabel);
        idealCard.addView(idealButton);

        methodCardsContainer.addView(idealCard);
    }
}
