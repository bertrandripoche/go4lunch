package com.openclassrooms.go4lunch.model;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;

import java.util.HashMap;

public class Restaurant {
    private String id;
    private String name;
    @Nullable private HashMap<String, HashMap<String,String>> likes;
    @Nullable private Integer lunchAttendees;
    @Nullable private String address;
    @Nullable private OpeningHours openingHours;
    @Nullable private LatLng latLng;
    @Nullable private String distance;
    @Nullable private Double rating;
    @Nullable private PhotoMetadata photo;

    public Restaurant() { }

    public Restaurant(String id, String name, @Nullable HashMap<String, HashMap<String, String>> likes, @Nullable Integer lunchAttendees, @Nullable String address, @Nullable OpeningHours openingHours, @Nullable LatLng latLng, @Nullable String distance, @Nullable Double rating, @Nullable PhotoMetadata photo) {
        this.id = id;
        this.name = name;
        this.likes = likes;
        this.lunchAttendees = lunchAttendees;
        this.address = address;
        this.openingHours = openingHours;
        this.latLng = latLng;
        this.distance = distance;
        this.rating = rating;
        this.photo = photo;
    }

    public Restaurant(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // --- GETTERS ---
    public String getId() {return id;}
    public String getName() {return name;}
    @Nullable public String getAddress() {return address;}
    @Nullable public OpeningHours getOpeningHours() {return openingHours;}
    @Nullable public LatLng getLatLng() {return latLng;}
    @Nullable public String getDistance() {return distance;}
    @Nullable public Double getRating() {return rating;}
    @Nullable public HashMap<String, HashMap<String,String>> getLikes() {return likes;}
    @Nullable public Integer getLunchAttendees() {return lunchAttendees;}
    @Nullable public PhotoMetadata getPhoto() {return photo;}

    // --- SETTERS ---
    public void setLikes(HashMap<String, HashMap<String,String>> likes) {this.likes = likes;}
    public void setLunchAttendees(int lunchAttendees) {this.lunchAttendees = lunchAttendees;}
}
