package com.openclassrooms.go4lunch.view;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Restaurant;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "RestaurantViewHolder";
    private PlacesClient mPlacesClient;

    TextView mRestaurantName;
    TextView mRestaurantAddress;
    TextView mRestaurantOpeningHours;
    TextView mRestaurantDistance;
    TextView mRestaurantLunchAttendees;
    AppCompatImageView mRestaurantRating;
    AppCompatImageView mRestaurantPhoto;

    /**
     * This method describes a line of our RecyclerView
     * @param itemView represents one line of the list
     */
    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);

        mRestaurantName = itemView.findViewById(R.id.item_restaurant_name);
        mRestaurantAddress = itemView.findViewById(R.id.item_restaurant_address);
        mRestaurantOpeningHours = itemView.findViewById(R.id.item_restaurant_opening_hours);
        mRestaurantDistance = itemView.findViewById(R.id.item_restaurant_distance);
        mRestaurantLunchAttendees = itemView.findViewById(R.id.item_restaurant_lunch_attendees);
        mRestaurantRating = itemView.findViewById(R.id.item_restaurant_rating);
        mRestaurantPhoto = itemView.findViewById(R.id.item_restaurant_photo);
    }

}
