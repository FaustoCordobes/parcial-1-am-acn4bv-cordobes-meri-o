package com.example.barista_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barista_app.LoginActivity;
import com.example.barista_app.MainActivity;
import com.example.barista_app.R;
import com.example.barista_app.RecipeDetailActivity;
import com.example.barista_app.adapters.RecipeAdapter;
import com.example.barista_app.data.RecipeRepository;
import com.example.barista_app.models.Recipe;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment implements RecipeAdapter.OnRecipeActionListener{
    private final RecipeRepository recipeRepository = new RecipeRepository();
    private RecipeAdapter recipeAdapter;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView countTextView;
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

        nameTextView = view.findViewById(R.id.profile_name);
        emailTextView = view.findViewById(R.id.profile_email);
        countTextView = view.findViewById(R.id.profile_recipes_count);
        emptyTextView = view.findViewById(R.id.profile_empty);

        RecyclerView recipesList = view.findViewById(R.id.profile_recipes_list);
        recipesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        recipeAdapter = new RecipeAdapter(this);
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
    }
    @Override
    public void onRecipeLongClick(Recipe recipe) {
    }


    private void showUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        String email = user.getEmail();
        emailTextView.setText(email != null ? email : getString(R.string.profile_no_email));

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
                    for (QueryDocumentSnapshot document : snapshot) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setId(document.getId());
                        recipe.setUserName(null);
                        mine.add(recipe);
                    }
                    Collections.sort(mine, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                    recipeAdapter.setRecipes(mine);
                    countTextView.setText(getString(R.string.profile_recipes_count, mine.size()));
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