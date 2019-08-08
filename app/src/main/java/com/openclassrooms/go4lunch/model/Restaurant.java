package com.openclassrooms.go4lunch.model;

import java.util.HashMap;

public class Restaurant {
    private String id;
    private String name;
    private HashMap<String, String> likes;
    private HashMap<String, String> lunchPlaces;

    public Restaurant(String id, String name, HashMap<String, String> likes, HashMap<String, String> lunchPlaces) {
        this.id = id;
        this.name = name;
        this.likes = likes;
        this.lunchPlaces = lunchPlaces;
    }

    // --- GETTERS ---
    public String getId() {return id;}
    public String getName() {return name;}
    public HashMap<String, String> getLikes() {return likes;}
    public HashMap<String, String> getLunchPlaces() {return lunchPlaces;}

    // --- SETTERS ---
    public void setId(String id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setLikes(HashMap<String, String> likes) {this.likes = likes;}
    public void setLunchPlaces(HashMap<String, String> lunchPlaces) {this.lunchPlaces = lunchPlaces;}
}
