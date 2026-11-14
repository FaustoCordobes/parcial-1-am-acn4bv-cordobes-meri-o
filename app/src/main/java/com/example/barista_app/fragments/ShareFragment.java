package com.example.barista_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barista_app.R;
import com.example.barista_app.adapters.RecipeAdapter;
import com.example.barista_app.data.RecipeRepository;
import com.example.barista_app.models.Recipe;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ShareFragment extends Fragment {
    private final RecipeRepository recipeRepository = new RecipeRepository();
    private RecipeAdapter adapter;
    private TextView emptyView;


    public ShareFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView list = view.findViewById(R.id.share_recycler);
        emptyView = view.findViewById(R.id.share_empty);
        adapter = new RecipeAdapter(new RecipeAdapter.OnRecipeActionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                showRatingDialog(recipe);
            }


            @Override
            public void onRecipeLongClick(Recipe recipe) {
            }
        });
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setAdapter(adapter);
        loadFeed();

    }
    private void loadFeed() {
        recipeRepository.loadPublished(new RecipeRepository.RecipesCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                if (getView() != null) {
                    adapter.setRecipes(recipes);

                    emptyView.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);


                }
            }
            @Override
            public void onError(Exception exception) {
                if (isAdded()) {

                    Toast.makeText(requireContext(), R.string.recipes_load_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showRatingDialog(Recipe recipe) {
        recipeRepository.loadAverage(recipe.getId(), new RecipeRepository.RatingCallback() {
            @Override
            public void onResult(double average, int count) {
                if (!isAdded()) {
                    return;
                }
                String summary = count == 0
                        ? getString(R.string.share_no_ratings)
                        : getString(R.string.share_average_format, average, count);

                View box = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rate, null);
                ((TextView) box.findViewById(R.id.rate_summary)).setText(summary);
                RatingBar bar = box.findViewById(R.id.rate_bar);

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(recipe.getMethodName())
                        .setView(box)
                        .setPositiveButton(R.string.share_rate_save,
                                (dialog, which) -> saveRating(recipe, (int) bar.getRating()))
                        .setNegativeButton(R.string.share_cancel, null)
                        .show();
            }

            @Override
            public void onError(Exception exception) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.recipes_load_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveRating(Recipe recipe, int stars) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (stars == 0 || user == null) {
            return;
        }
        recipeRepository.setRating(recipe.getId(), user.getUid(), stars, new RecipeRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.share_rated, Toast.LENGTH_SHORT).show();
                    loadFeed();
                }
            }

            @Override
            public void onError(Exception exception) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.recipe_update_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}