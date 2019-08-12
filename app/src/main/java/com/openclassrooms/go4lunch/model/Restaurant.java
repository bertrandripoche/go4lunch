package com.openclassrooms.go4lunch.model;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class Restaurant {
    private String id;
    private String name;
    @Nullable
    private HashMap<String, String> likes;
    @Nullable
    private HashMap<String, String> lunchAttendees;

    public Restaurant() { }

    public Restaurant(String id, String name, @Nullable  HashMap<String, String> likes, @Nullable HashMap<String, String> lunchAttendees) {
        this.id = id;
        this.name = name;
        this.likes = likes;
        this.lunchAttendees = lunchAttendees;
    }

    // --- GETTERS ---
    public String getId() {return id;}
    public String getName() {return name;}
    public HashMap<String, String> getLikes() {return likes;}
    public HashMap<String, String> getLunchAttendees() {return lunchAttendees;}

    // --- SETTERS ---
    public void setId(String id) {this.id = id;}
    public void setName(String name) {this.name = name;}
    public void setLikes(HashMap<String, String> likes) {this.likes = likes;}
    public void setLunchPlaces(HashMap<String, String> lunchAttendees) {this.lunchAttendees = lunchAttendees;}
}
