package com.example.barista_app.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.barista_app.ImageLoader;
import com.example.barista_app.LoginActivity;
import com.example.barista_app.MainActivity;
import com.example.barista_app.MethodDetailActivity;
import com.example.barista_app.R;
import com.example.barista_app.RecipeDetailActivity;
import com.example.barista_app.components.ButtonCalcTimer;
import com.example.barista_app.data.ImageUploader;
import com.example.barista_app.data.MethodImages;
import com.example.barista_app.data.MethodRepository;
import com.example.barista_app.data.RecipeRepository;
import com.example.barista_app.models.CoffeeMethod;
import com.example.barista_app.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
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
    private ImageView recipePhotoPreview;
    private Uri selectedPhotoUri;
    private TextView statRecipesTextView;
    private TextView statFavoritesTextView;
    private TextView statAverageTextView;
    private TextView recentEmptyTextView;
    private LinearLayout recentContainer;

    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) {
                    return;
                }
                selectedPhotoUri = uri;
                recipePhotoPreview.setImageURI(uri);
                recipePhotoPreview.setVisibility(View.VISIBLE);
            });

    private CoffeeMethod selectedMethod;
    private int selectedAmount;
    private String selectedIntensity;

    private final Handler stopwatchHandler = new Handler(Looper.getMainLooper());
    private int elapsedSeconds = 0;
    private boolean isStopwatchRunning = false;
    private Button step3ContinueButton;
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

        recipePhotoPreview = view.findViewById(R.id.recipePhotoPreview);
        Button recipePickPhotoButton = view.findViewById(R.id.recipePickPhotoButton);
        recipePickPhotoButton.setOnClickListener(v -> photoPicker.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));


        Button step2BackButton = view.findViewById(R.id.step2BackButton);
        Button step2ContinueButton = view.findViewById(R.id.step2ContinueButton);
        Button step3BackButton = view.findViewById(R.id.step3BackButton);
        step3ContinueButton = view.findViewById(R.id.step3ContinueButton);
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
        statRecipesTextView = view.findViewById(R.id.stat_recipes);
        statFavoritesTextView = view.findViewById(R.id.stat_favorites);
        statAverageTextView = view.findViewById(R.id.stat_average);
        recentEmptyTextView = view.findViewById(R.id.home_recent_empty);
        recentContainer = view.findViewById(R.id.home_recent_container);
        view.findViewById(R.id.home_see_all).setOnClickListener(v -> ((MainActivity) requireActivity()).selectTab(R.id.nav_me));
        ButtonCalcTimer calcButton = view.findViewById(R.id.btn_open_timer_calc);
        calcButton.setOnButtonClickListener(() ->requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TimerCalculatorFragment())
                        .addToBackStack(null)
                        .commit());

        view.findViewById(R.id.step2DetailLink).setOnClickListener(v -> {
            if (selectedMethod != null) {
                openMethodDetail(selectedMethod);
            }
        });

        loadMethods();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopwatchHandler.removeCallbacks(stopwatchRunnable);
    }
    @Override
    public void onResume() {
        super.onResume();
        loadActivity();
    }

    private void loadActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("recipes")
                .whereEqualTo("ownerId", user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }
                    List<Recipe> mine = new ArrayList<>();
                    long ratingSum = 0;
                    long ratingCount = 0;
                    for (QueryDocumentSnapshot document : snapshot) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setId(document.getId());
                        ratingSum += recipe.getRatingSum();
                        ratingCount += recipe.getRatingCount();
                        mine.add(recipe);
                    }
                    Collections.sort(mine, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    statRecipesTextView.setText(String.valueOf(mine.size()));
                    statAverageTextView.setText(ratingCount == 0
                            ? getString(R.string.profile_no_average)
                            : getString(R.string.profile_average_format, (double) ratingSum / ratingCount));
                    showRecentRecipes(mine);
                });

        recipeRepository.loadFavoriteIds(user.getUid(), new RecipeRepository.FavoriteIdsCallback() {
            @Override
            public void onSuccess(List<String> ids) {
                if (!isAdded()) {
                    return;
                }
                statFavoritesTextView.setText(String.valueOf(ids.size()));
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void showRecentRecipes(List<Recipe> mine) {
        recentContainer.removeAllViews();
        recentEmptyTextView.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int count = Math.min(3, mine.size());
        for (int i = 0; i < count; i++) {
            Recipe recipe = mine.get(i);
            View row = inflater.inflate(R.layout.item_profile_recipe, recentContainer, false);
            ImageView image = row.findViewById(R.id.profile_row_image);
            TextView name = row.findViewById(R.id.profile_row_name);
            TextView summary = row.findViewById(R.id.profile_row_summary);
            TextView rating = row.findViewById(R.id.profile_row_rating);
            row.findViewById(R.id.profile_row_delete).setVisibility(View.GONE);

            name.setText(recipe.getMethodName());
            summary.setText(getString(R.string.profile_row_summary,
                    recipe.getWaterMl(), recipe.getCoffeeGrams(), recipe.getIntensity()));

            String imageUrl = recipe.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = MethodImages.urlFor(recipe.getMethodId());
            }
            ImageLoader.load(image, imageUrl, R.drawable.ic_coffee_generic);

            long votes = recipe.getRatingCount();
            if (votes > 0) {
                rating.setText(getString(R.string.home_recent_rating, (double) recipe.getRatingSum() / votes));
                rating.setVisibility(View.VISIBLE);
            } else {
                rating.setVisibility(View.GONE);
            }

            row.setOnClickListener(v -> openRecipeDetail(recipe));
            recentContainer.addView(row);
        }
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        intent.putExtra(RecipeDetailActivity.EXTRA_METHOD_NAME, recipe.getMethodName());
        intent.putExtra(RecipeDetailActivity.EXTRA_WATER_ML, recipe.getWaterMl());
        intent.putExtra(RecipeDetailActivity.EXTRA_COFFEE_GRAMS, recipe.getCoffeeGrams());
        intent.putExtra(RecipeDetailActivity.EXTRA_INTENSITY, recipe.getIntensity());
        intent.putExtra(RecipeDetailActivity.EXTRA_GRIND, recipe.getGrind());
        intent.putExtra(RecipeDetailActivity.EXTRA_NOTE, recipe.getNote());
        intent.putExtra(RecipeDetailActivity.EXTRA_OWNER_ID, recipe.getOwnerId());
        intent.putExtra(RecipeDetailActivity.EXTRA_SHARED, recipe.isShared());
        startActivity(intent);
    }

    private void loadMethods() {
        methodRepository.loadMethods(new MethodRepository.MethodsCallback() {
            @Override
            public void onSuccess(List<CoffeeMethod> methods) {
                if (!isAdded()) {
                    return;
                }
                methodsLoadingTextView.setVisibility(View.GONE);
                showMethodGrid(methods);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }
                methodsLoadingTextView.setText(R.string.methods_error);
                Toast.makeText(requireContext(), R.string.methods_error, Toast.LENGTH_LONG).show();
                showMethodGrid(new ArrayList<>());
            }
        });
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
    private void updateGrindRecommendation(boolean isWholeBean) {
        if (!isWholeBean) {
            grindTypeResultTextView.setVisibility(View.GONE);
            setStep3ContinueEnabled(true);
            return;
        }
        String grind = selectedMethod.getGrind().toLowerCase(Locale.getDefault());
        grindTypeResultTextView.setText(getString(R.string.grind_recommendation, selectedMethod.getName(), grind)
                + "\n" + getString(R.string.grind_blocked_hint));
        grindTypeResultTextView.setVisibility(View.VISIBLE);
        setStep3ContinueEnabled(false);
    }

    private void setStep3ContinueEnabled(boolean enabled) {
        step3ContinueButton.setEnabled(enabled);
        step3ContinueButton.setAlpha(enabled ? 1f : 0.5f);
    }

    private void handleStep3Continue() {
        if (wholeBeanRadioButton.isChecked()) {
            return;
        }
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            Toast.makeText(requireContext(), R.string.recipe_save_error, Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(userDocument -> {
                    if(!isAdded()) {
                        return;
                    }
                    String userName = userDocument.getString("username");
                    if (selectedPhotoUri == null) {
                        writeRecipe(user, userName, coffeeGrams, null);
                        return;
                    }
                    ImageUploader.upload(requireContext(), selectedPhotoUri, new ImageUploader.UploadCallback() {
                        @Override
                        public void onSuccess(String url) {
                            android.util.Log.d("Recipe", "upload ok, isAdded=" + isAdded());
                            writeRecipe(user, userName, coffeeGrams, url);
                        }

                        @Override
                        public void onError(Exception exception) {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), R.string.recipe_upload_error, Toast.LENGTH_LONG).show();
                            }
                            writeRecipe(user, userName, coffeeGrams, null);
                        }
                    });

                });
    }

    private void writeRecipe(FirebaseUser user, String userName, int coffeeGrams, String imageUrl) {
        Recipe recipe = new Recipe();
        recipe.setOwnerId(user.getUid());
        recipe.setUserName(userName);
        recipe.setShared(false);
        recipe.setMethodId(selectedMethod.getId());
        recipe.setMethodName(selectedMethod.getName());
        recipe.setWaterMl(selectedAmount);
        recipe.setCoffeeGrams(coffeeGrams);
        recipe.setIntensity(selectedIntensity);
        recipe.setGrind(selectedMethod.getGrind());
        recipe.setNote("");
        recipe.setCreatedAt(System.currentTimeMillis());
        recipe.setImageUrl(imageUrl);
        selectedPhotoUri = null;
        recipePhotoPreview.setVisibility(View.GONE);

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


    private String getSelectedIntensity() {
        int checkedId = intensityRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.intensitySoftRadioButton) {
            return getString(R.string.intensity_soft);
        } else if (checkedId == R.id.intensityStrongRadioButton) {
            return getString(R.string.intensity_strong);
        }
        return getString(R.string.intensity_medium);
    }

    private void showMethodGrid(List<CoffeeMethod> methods) {
        List<View> cards = new ArrayList<>();
        int limit = Math.min(methods.size(), 6);
        for (int i = 0; i < limit; i++) {
            cards.add(createMethodCard(methods.get(i)));
        }

        int columns = 3;
        int gap = getResources().getDimensionPixelSize(R.dimen.method_grid_gap);
        for (int start = 0; start < cards.size(); start += columns) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, gap);
            row.setLayoutParams(rowParams);

            for (int i = 0; i < columns; i++) {
                int index = start + i;
                View cell = index < cards.size() ? cards.get(index) : new View(requireContext());
                LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                cellParams.setMarginEnd(i < columns - 1 ? gap : 0);
                cell.setLayoutParams(cellParams);
                row.addView(cell);
            }
            methodCardsContainer.addView(row);
        }
    }

    private View createMethodCard(CoffeeMethod method) {
        View card = LayoutInflater.from(requireContext()).inflate(R.layout.item_method_card, methodCardsContainer, false);
        ImageView image = card.findViewById(R.id.method_card_image);
        TextView name = card.findViewById(R.id.method_card_name);
        TextView time = card.findViewById(R.id.method_card_time);

        name.setText(method.getName());
        time.setText(method.getBrewTime());
        ImageLoader.load(image, MethodImages.urlFor(method.getId()), getImageForMethod(method.getId()));

        card.setOnClickListener(v -> selectCoffeeMachine(method));
        return card;
    }


}
