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
    public interface FavoriteIdsCallback {
        void onSuccess(List<String> ids);

        void onError(Exception exception);
    }

    public interface BooleanCallback {
        void onResult(boolean value);

        void onError(Exception exception);
    }
    public interface RecipeCallback {
        void onSuccess(Recipe recipe);

        void onError(Exception exception);
    }
    private static final String COLLECTION_RECIPES = "recipes";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_NOTE = "note";
    private static final String FIELD_SHARED = "shared";
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private static final String COLLECTION_USERS = "users";
    private static final String SUBCOLLECTION_FAVORITES = "favorites";
    private static final String FIELD_RECIPE_ID = "recipeId";
    private static final String FIELD_METHOD_NAME = "methodName";
    private static final String FIELD_ADDED_AT = "addedAt";
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
    public void addFavorite(String uid, Recipe recipe, OperationCallback callback) {
        Map<String, Object> favorite = new HashMap<>();
        favorite.put(FIELD_RECIPE_ID, recipe.getId());
        favorite.put(FIELD_METHOD_NAME, recipe.getMethodName());
        favorite.put(FIELD_ADDED_AT, System.currentTimeMillis());
        database.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_FAVORITES)
                .document(recipe.getId())
                .set(favorite)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
    public void removeFavorite(String uid, String recipeId, OperationCallback callback) {
        database.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_FAVORITES)
                .document(recipeId)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
    public void isFavorite(String uid, String recipeId, BooleanCallback callback) {
        database.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_FAVORITES)
                .document(recipeId)
                .get()
                .addOnSuccessListener(snapshot -> callback.onResult(snapshot.exists()))
                .addOnFailureListener(callback::onError);
    }

    public void loadFavoriteIds(String uid, FavoriteIdsCallback callback) {
        database.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_FAVORITES)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> ids = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshot) {
                        ids.add(document.getId());
                    }
                    callback.onSuccess(ids);
                })
                .addOnFailureListener(callback::onError);
    }
    public void loadRecipe(String recipeId, RecipeCallback callback) {
        database.collection(COLLECTION_RECIPES)
                .document(recipeId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onSuccess(null);
                        return;
                    }
                    Recipe recipe = snapshot.toObject(Recipe.class);
                    if (recipe != null) {
                        recipe.setId(snapshot.getId());
                    }
                    callback.onSuccess(recipe);
                })
                .addOnFailureListener(callback::onError);
    }
    public void setFavoriteOrRemove(String uid, Recipe recipe, boolean currentlyFavorite, OperationCallback callback) {
        if (currentlyFavorite) {
            removeFavorite(uid, recipe.getId(), callback);
        } else {
            addFavorite(uid, recipe, callback);
        }
    }

}
