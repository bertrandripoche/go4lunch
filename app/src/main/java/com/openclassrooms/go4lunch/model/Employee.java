package com.openclassrooms.go4lunch.model;

import androidx.annotation.Nullable;

import java.util.List;

public class Employee {

    private String uid;
    private String name;
    private String mail;
    @Nullable
    private String urlPicture;

    private String lunchPlace;
    private List<String> likedPlaces;

    public Employee() { }

    public Employee(String uid, String name, String mail, @Nullable String urlPicture, @Nullable String lunchPlace, @Nullable List<String> likedPlaces) {
        this.uid = uid;
        this.name = name;
        this.mail = mail;
        this.urlPicture = urlPicture;
        this.lunchPlace = lunchPlace;
        this.likedPlaces = likedPlaces;
    }

    // --- GETTERS ---
    public String getUid() {return uid;}
    public String getName() {return name;}
    public String getMail() {return mail;}
    @Nullable
    public String getUrlPicture() {return urlPicture;}
    public String getLunchPlace() {return lunchPlace;}
    public List<String> getLikedPlaces() {return likedPlaces;}

    // --- SETTERS ---
    public void setName(String name) {this.name = name;}
    public void setMail(String mail) {this.mail = mail;}
    public void setLunchPlace(String lunchPlace) {this.lunchPlace = lunchPlace;}
    public void setLikedPlaces(List<String> likedPlaces) {this.likedPlaces = likedPlaces;}
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }

}
