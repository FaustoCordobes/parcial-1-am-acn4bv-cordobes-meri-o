package com.example.barista_app.data;

import com.example.barista_app.models.Recipe;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final String FIELD_SHARED = "shared";
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

    public void setShared(String recipeId, boolean shared, OperationCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .document(recipeId)
                .update(FIELD_SHARED, shared)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
    public void loadPublished(RecipesCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .whereEqualTo(FIELD_SHARED, true)
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
    public interface RatingCallback {
        void onResult(double average, int count);

        void onError(Exception exception);
    }
    public void loadAverage(String recipeId, RatingCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .document(recipeId)
                .collection("ratings")
                .get()
                .addOnSuccessListener(snapshot -> {

                    int total = 0;
                    for (QueryDocumentSnapshot document : snapshot) {

                        Long stars = document.getLong("stars");
                        if (stars != null) {

                            total += stars;

                        }
                    }
                    int count = snapshot.size();
                    callback.onResult(count == 0 ? 0 : (double) total / count, count);

                })
                .addOnFailureListener(callback::onError);
    }
    public void setRating(String recipeId, String uid, int stars, OperationCallback callback) {
        DocumentReference recipeRef = database.collection(COLLECTION_RECIPES).document(recipeId);
        DocumentReference ratingRef = recipeRef.collection("ratings").document(uid);

        database.runTransaction(transaction -> {
                    DocumentSnapshot recipeSnapshot = transaction.get(recipeRef);
                    DocumentSnapshot ratingSnapshot = transaction.get(ratingRef);

                    long sum = getLongOrZero(recipeSnapshot, "ratingSum");
                    long count = getLongOrZero(recipeSnapshot, "ratingCount");

                    if (ratingSnapshot.exists()) {
                        sum -= getLongOrZero(ratingSnapshot, "stars"); // quita el voto anterior
                    } else {
                        count += 1; // primer voto de este usuario
                    }
                    sum += stars;

                    Map<String, Object> rating = new HashMap<>();
                    rating.put("stars", stars);
                    transaction.set(ratingRef, rating);
                    transaction.update(recipeRef, "ratingSum", sum, "ratingCount", count);
                    return null;
                })
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    private long getLongOrZero(DocumentSnapshot snapshot, String field) {
        Long value = snapshot.getLong(field);
        return value == null ? 0 : value;
    }

}
