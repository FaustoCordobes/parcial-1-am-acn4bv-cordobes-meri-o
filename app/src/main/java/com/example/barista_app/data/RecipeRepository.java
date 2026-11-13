package com.example.barista_app.data;

import com.example.barista_app.models.Recipe;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {

    public interface RecipesCallback {
        void onSuccess(List<Recipe> recipes);

        void onError(Exception exception);
    }

    public interface OperationCallback {
        void onSuccess();

        void onError(Exception exception);
    }

    private static final String COLLECTION_RECIPES = "recipes";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_NOTE = "note";

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    public void saveRecipe(Recipe recipe, OperationCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .add(recipe)
                .addOnSuccessListener(reference -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void loadRecipes(RecipesCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Recipe> recipes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshot) {
                        Recipe recipe = document.toObject(Recipe.class);
                        recipe.setId(document.getId());
                        recipes.add(recipe);
                    }
                    callback.onSuccess(recipes);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateNote(String recipeId, String note, OperationCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .document(recipeId)
                .update(FIELD_NOTE, note)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteRecipe(String recipeId, OperationCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .document(recipeId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}
