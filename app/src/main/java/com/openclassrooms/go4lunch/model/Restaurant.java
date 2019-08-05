package com.openclassrooms.go4lunch.model;

import android.net.Uri;

public class Restaurant {
    private String mId;
    private String mName;
    private String mAddress;
    private String mPhone;
    private Uri mUrl;
    private Double mRating;

    public Restaurant(String id, String name, String address, String phone, Uri url, Double rating) {
        mId = id;
        mName = name;
        mAddress = address;
        mPhone = phone;
        mUrl = url;
        mRating = rating;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getPhone() {
        return mPhone;
    }

    public Uri getUrl() {
        return mUrl;
    }

    public Double getRating() {
        return mRating;
    }
}
