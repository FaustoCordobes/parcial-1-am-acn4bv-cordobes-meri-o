package com.example.barista_app.models;

public class CoffeeMethod {

    private String id;
    private String name;
    private String description;
    private int ratio;
    private String grind;
    private String brewTime;
    private int brewTimeSeconds;
    private int order;

    public CoffeeMethod() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public String getGrind() {
        return grind;
    }

    public void setGrind(String grind) {
        this.grind = grind;
    }

    public String getBrewTime() {
        return brewTime;
    }

    public void setBrewTime(String brewTime) {
        this.brewTime = brewTime;
    }

    public int getBrewTimeSeconds() {
        return brewTimeSeconds;
    }

    public void setBrewTimeSeconds(int brewTimeSeconds) {
        this.brewTimeSeconds = brewTimeSeconds;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
