package com.openclassrooms.go4lunch.model;

public class Attendee {
    private String uid;
    private String name;
    private String urlPicture;

    public Attendee() {}

    public Attendee(String uid, String name, String urlPicture) {
        this.uid = uid;
        this.name = name;
        this.urlPicture = urlPicture;
    }

    // --- GETTERS ---
    public String getUid() {return uid;}
    public String getName() {return name;}
    public String getUrlPicture() {return urlPicture;}

    // --- SETTERS ---
    public void setUid(String uid) {this.uid = uid;}
    public void setName(String name) {this.name = name;}
    public void setUrlPicture(String urlPicture) {this.urlPicture = urlPicture;}
}
