package com.example.barista_app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barista_app.LoginActivity;
import com.example.barista_app.R;
import com.example.barista_app.RecipeDetailActivity;
import com.example.barista_app.UiUtils;
import com.example.barista_app.adapters.ProfileRecipeAdapter;
import com.example.barista_app.data.RecipeRepository;
import com.example.barista_app.models.Recipe;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment implements ProfileRecipeAdapter.Listener {
    private final RecipeRepository recipeRepository = new RecipeRepository();
    private ProfileRecipeAdapter recipeAdapter;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView avatarTextView;
    private TextView recipesValueTextView;
    private TextView favoritesValueTextView;
    private TextView averageValueTextView;
    private TextView emptyTextView;

    public ProfileFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View header = view.findViewById(R.id.profile_header);
        header.setPadding(header.getPaddingLeft(),
                UiUtils.statusBarHeight(requireContext()) + getResources().getDimensionPixelSize(R.dimen.spacing_screen),
                header.getPaddingRight(), header.getPaddingBottom());

        nameTextView = view.findViewById(R.id.profile_name);
        emailTextView = view.findViewById(R.id.profile_email);
        avatarTextView = view.findViewById(R.id.profile_avatar);
        recipesValueTextView = view.findViewById(R.id.profile_stat_recipes);
        favoritesValueTextView = view.findViewById(R.id.profile_stat_favorites);
        averageValueTextView = view.findViewById(R.id.profile_stat_average);
        emptyTextView = view.findViewById(R.id.profile_empty);

        RecyclerView recipesList = view.findViewById(R.id.profile_recipes_list);

        MaterialSwitch darkModeSwitch = view.findViewById(R.id.profile_dark_mode_switch);
        SharedPreferences settings = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isNight = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        darkModeSwitch.setChecked(isNight);
        darkModeSwitch.setOnCheckedChangeListener((button, checked) -> {
            settings.edit().putBoolean("dark_mode", checked).apply();
            AppCompatDelegate.setDefaultNightMode(checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        recipesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        recipeAdapter = new ProfileRecipeAdapter(this);
        recipesList.setAdapter(recipeAdapter);

        Button logoutButton = view.findViewById(R.id.logoutBtn);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        showUserInfo();
        loadMyRecipes();
        loadFavoritesCount();
    }

    private void showUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String email = user.getEmail();
        emailTextView.setText(email != null ? email : getString(R.string.profile_no_email));
        if (email != null && !email.isEmpty()) {
            avatarTextView.setText(email.substring(0, 1).toUpperCase(Locale.getDefault()));
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(userDocument -> {
                    if (!isAdded()) {
                        return;
                    }
                    String username = userDocument.getString("username");
                    nameTextView.setText(username != null ? username : getString(R.string.profile_no_name));
                    if (username != null && !username.isEmpty()) {
                        avatarTextView.setText(username.substring(0, 1).toUpperCase(Locale.getDefault()));
                    }
                });
    }

    private void loadFavoritesCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        recipeRepository.loadFavoriteIds(user.getUid(), new RecipeRepository.FavoriteIdsCallback() {
            @Override
            public void onSuccess(List<String> ids) {
                if (!isAdded()) {
                    return;
                }
                favoritesValueTextView.setText(String.valueOf(ids.size()));
            }

            @Override
            public void onError(Exception exception) {
            }
        });
    }

    private void loadMyRecipes() {
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
                        recipe.setUserName(null);
                        ratingSum += recipe.getRatingSum();
                        ratingCount += recipe.getRatingCount();
                        mine.add(recipe);
                    }
                    Collections.sort(mine, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    recipeAdapter.setRecipes(mine);
                    recipesValueTextView.setText(String.valueOf(mine.size()));
                    if (ratingCount == 0) {
                        averageValueTextView.setText(R.string.profile_no_average);
                    } else {
                        averageValueTextView.setText(getString(R.string.profile_average_format,
                                (double) ratingSum / ratingCount));
                    }
                    emptyTextView.setVisibility(mine.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(requireContext(), R.string.recipes_load_error, Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
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

    @Override
    public void onRecipeDelete(Recipe recipe) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.recipe_delete_dialog_title)
                .setMessage(R.string.recipe_delete_dialog_message)
                .setPositiveButton(R.string.recipe_delete_dialog_confirm, (dialog, which) -> deleteRecipe(recipe))
                .setNegativeButton(R.string.recipe_delete_dialog_cancel, null)
                .show();
    }

    private void deleteRecipe(Recipe recipe) {
        recipeRepository.deleteRecipe(recipe.getId(), new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.recipe_deleted, Toast.LENGTH_SHORT).show();
                loadMyRecipes();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.recipe_update_error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
