package com.example.barista_app.data;

import com.example.barista_app.models.CoffeeMethod;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MethodRepository {

    public interface MethodsCallback {
        void onSuccess(List<CoffeeMethod> methods);

        void onError(Exception exception);
    }

    private static final String COLLECTION_METHODS = "coffee_methods";
    private static final String FIELD_ORDER = "order";

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    public void loadMethods(MethodsCallback callback) {
        database.collection(COLLECTION_METHODS)
                .orderBy(FIELD_ORDER)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<CoffeeMethod> methods = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshot) {
                        CoffeeMethod method = document.toObject(CoffeeMethod.class);
                        method.setId(document.getId());
                        methods.add(method);
                    }
                    callback.onSuccess(methods);
                })
                .addOnFailureListener(callback::onError);
    }
}
