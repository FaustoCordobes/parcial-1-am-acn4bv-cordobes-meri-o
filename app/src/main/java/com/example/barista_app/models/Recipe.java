package com.example.barista_app.models;

import com.google.firebase.firestore.Exclude;

public class Recipe {

    private String id;
    private String userName;
    private String methodId;
    private String methodName;
    private int waterMl;
    private int coffeeGrams;
    private String intensity;
    private String grind;
    private String note;
    private long createdAt;

    public Recipe() {
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getWaterMl() {
        return waterMl;
    }

    public void setWaterMl(int waterMl) {
        this.waterMl = waterMl;
    }

    public int getCoffeeGrams() {
        return coffeeGrams;
    }

    public void setCoffeeGrams(int coffeeGrams) {
        this.coffeeGrams = coffeeGrams;
    }

    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }

    public String getGrind() {
        return grind;
    }

    public void setGrind(String grind) {
        this.grind = grind;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
