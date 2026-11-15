package com.example.barista_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barista_app.ImageLoader;
import com.example.barista_app.R;
import com.example.barista_app.data.MethodImages;
import com.example.barista_app.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class ProfileRecipeAdapter extends RecyclerView.Adapter<ProfileRecipeAdapter.ProfileRecipeViewHolder> {

    public interface Listener {
        void onRecipeClick(Recipe recipe);

        void onRecipeDelete(Recipe recipe);
    }

    private final List<Recipe> recipes = new ArrayList<>();
    private final Listener listener;

    public ProfileRecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        recipes.clear();
        recipes.addAll(newRecipes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_recipe, parent, false);
        return new ProfileRecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileRecipeViewHolder holder, int position) {
        holder.bind(recipes.get(position));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    class ProfileRecipeViewHolder extends RecyclerView.ViewHolder {

        private final ImageView image;
        private final TextView nameTextView;
        private final TextView summaryTextView;
        private final ImageButton deleteButton;

        ProfileRecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.profile_row_image);
            nameTextView = itemView.findViewById(R.id.profile_row_name);
            summaryTextView = itemView.findViewById(R.id.profile_row_summary);
            deleteButton = itemView.findViewById(R.id.profile_row_delete);
        }

        void bind(Recipe recipe) {
            Context context = itemView.getContext();
            nameTextView.setText(recipe.getMethodName());
            int summaryRes = recipe.isShared() ? R.string.profile_row_summary_shared : R.string.profile_row_summary;
            summaryTextView.setText(context.getString(summaryRes,
                    recipe.getWaterMl(), recipe.getCoffeeGrams(), recipe.getIntensity()));

            String imageUrl = recipe.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = MethodImages.urlFor(recipe.getMethodId());
            }
            ImageLoader.load(image, imageUrl, R.drawable.ic_coffee_generic);

            itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
            deleteButton.setOnClickListener(v -> listener.onRecipeDelete(recipe));
        }
    }
}
