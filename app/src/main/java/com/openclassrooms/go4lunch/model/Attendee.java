package com.openclassrooms.go4lunch.model;

public class Attendee {
    private String name;
    private String urlPicture;

    public Attendee() {}

    public Attendee(String name, String urlPicture) {
        this.name = name;
        this.urlPicture = urlPicture;
    }

    public String getName() {return name;}
    public String getUrlPicture() {return urlPicture;}

    public void setName(String name) {this.name = name;}
    public void setUrlPicture(String urlPicture) {this.urlPicture = urlPicture;}
}
