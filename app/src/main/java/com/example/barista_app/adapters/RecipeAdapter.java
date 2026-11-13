package com.example.barista_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barista_app.R;
import com.example.barista_app.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeActionListener {
        void onRecipeClick(Recipe recipe);

        void onRecipeLongClick(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final OnRecipeActionListener listener;

    public RecipeAdapter(OnRecipeActionListener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        holder.bind(recipes.get(position));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView;
        private final TextView summaryTextView;
        private final TextView noteTextView;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.recipeItemName);
            summaryTextView = itemView.findViewById(R.id.recipeItemSummary);
            noteTextView = itemView.findViewById(R.id.recipeItemNote);
        }

        void bind(Recipe recipe) {
            Context context = itemView.getContext();
            nameTextView.setText(recipe.getMethodName());
            summaryTextView.setText(context.getString(R.string.recipe_summary,
                    recipe.getWaterMl(), recipe.getCoffeeGrams(), recipe.getIntensity()));

            String note = recipe.getNote();
            if (note == null || note.isEmpty()) {
                noteTextView.setVisibility(View.GONE);
            } else {
                noteTextView.setText(context.getString(R.string.recipe_note_display, note));
                noteTextView.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
            itemView.setOnLongClickListener(v -> {
                listener.onRecipeLongClick(recipe);
                return true;
            });
        }
    }
}
