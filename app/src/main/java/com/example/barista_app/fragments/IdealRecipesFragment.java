package com.example.barista_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barista_app.R;
import com.example.barista_app.RecipeDetailActivity;
import com.example.barista_app.adapters.RecipeAdapter;
import com.example.barista_app.data.RecipeRepository;
import com.example.barista_app.models.Recipe;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IdealRecipesFragment extends Fragment implements RecipeAdapter.OnRecipeActionListener {

    private final RecipeRepository recipeRepository = new RecipeRepository();
    private RecipeAdapter recipeAdapter;
    private TextView emptyTextView;

    public IdealRecipesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ideal_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyTextView = view.findViewById(R.id.ideal_empty);
        RecyclerView recipesList = view.findViewById(R.id.ideal_recipes_list);
        recipesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        recipeAdapter = new RecipeAdapter(this);
        recipesList.setAdapter(recipeAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void loadRecipes() {
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
                if (ids.isEmpty()) {
                    showRecipes(new ArrayList<>());
                    return;
                }
                loadFavoriteRecipes(user.getUid(), ids);
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.recipes_load_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadFavoriteRecipes(String uid, List<String> ids) {
        List<Recipe> results = new ArrayList<>();
        int[] pending = {ids.size()};
        for (String id : ids) {
            recipeRepository.loadRecipe(id, new RecipeRepository.RecipeCallback() {
                @Override
                public void onSuccess(Recipe recipe) {
                    if (recipe != null && (recipe.isShared() || uid.equals(recipe.getOwnerId()))) {
                        results.add(recipe);
                    }
                    finishOne();
                }

                @Override
                public void onError(Exception exception) {
                    finishOne();
                }

                private void finishOne() {
                    pending[0]--;
                    if (pending[0] == 0 && isAdded()) {
                        showRecipes(results);
                    }
                }
            });
        }
    }

    private void showRecipes(List<Recipe> recipes) {
        recipeAdapter.setRecipes(recipes);
        emptyTextView.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
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
    public void onRecipeLongClick(Recipe recipe) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        Snackbar.make(requireView(), recipe.getMethodName(), Snackbar.LENGTH_LONG)
                .setAction(R.string.recipe_remove_favorite, v -> removeFromFavorites(user.getUid(), recipe))
                .show();
    }

    private void removeFromFavorites(String uid, Recipe recipe) {
        recipeRepository.removeFavorite(uid, recipe.getId(), new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.recipe_favorite_removed, Toast.LENGTH_SHORT).show();
                loadRecipes();
            }

            @Override
            public void onError(Exception exception) {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.recipe_favorite_error, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void deleteRecipe(Recipe recipe) {
        recipeRepository.deleteRecipe(recipe.getId(), new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }
                Toast.makeText(requireContext(), R.string.recipe_deleted, Toast.LENGTH_SHORT).show();
                loadRecipes();
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
