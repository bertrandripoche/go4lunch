package com.openclassrooms.go4lunch.model;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class Restaurant {
    private String name;
    @Nullable
    private HashMap<String, HashMap<String,String>> likes;
    @Nullable
    private HashMap<String, HashMap<String,String>> lunchAttendees;

    public Restaurant() { }

    public Restaurant(String name) {
        this.name = name;
    }

    // --- GETTERS ---
    public String getName() {return name;}
    public HashMap<String, HashMap<String,String>> getLikes() {return likes;}
    public HashMap<String, HashMap<String,String>> getLunchAttendees() {return lunchAttendees;}

    // --- SETTERS ---
    public void setName(String name) {this.name = name;}
    public void setLikes(HashMap<String, HashMap<String,String>> likes) {this.likes = likes;}
    public void setLunchPlaces(HashMap<String, HashMap<String,String>> lunchAttendees) {this.lunchAttendees = lunchAttendees;}
}
