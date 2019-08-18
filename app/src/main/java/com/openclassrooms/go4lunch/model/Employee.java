package com.openclassrooms.go4lunch.model;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class Employee {

    private String name;
    private String mail;
    @Nullable
    private String urlPicture;
    @Nullable
    private String lunchPlace;
    @Nullable
    private String lunchPlaceId;
    @Nullable
    private HashMap<String, String> likedPlaces;

    public Employee() { }

    public Employee(String name, String mail, String urlPicture) {
        this.name = name;
        this.urlPicture = urlPicture;
        this.mail = mail;
        this.lunchPlace = null;
        this.lunchPlaceId = null;
        this.likedPlaces = null;
    }

    public Employee(String name, String mail, @Nullable String urlPicture, @Nullable String lunchPlace, @Nullable String lunchPlaceId, @Nullable HashMap<String, String> likedPlaces) {
        this.name = name;
        this.mail = mail;
        this.urlPicture = urlPicture;
        this.lunchPlace = lunchPlace;
        this.lunchPlaceId = lunchPlaceId;
        this.likedPlaces = likedPlaces;
    }

    // --- GETTERS ---
    public String getName() {return name;}
    public String getMail() {return mail;}
    @Nullable
    public String getUrlPicture() {return urlPicture;}
    public String getLunchPlace() {return lunchPlace;}
    public String getLunchPlaceId() {return lunchPlaceId;}
    public  HashMap<String, String>  getLikedPlaces() {return likedPlaces;}

    // --- SETTERS ---
    public void setLunchPlace(String lunchPlace) {this.lunchPlace = lunchPlace;}
    public void setLunchPlaceId(String lunchPlaceId) {this.lunchPlaceId = lunchPlaceId;}
    public void setLikedPlaces(HashMap<String, String> likedPlaces) {this.likedPlaces = likedPlaces;}

}
