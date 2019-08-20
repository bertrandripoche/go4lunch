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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Restaurant;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "RestaurantViewHolder";
    private PlacesClient mPlacesClient;
    private Place mPlace;

    TextView mRestaurantName;
    TextView mRestaurantAddress;
    TextView mRestaurantOpeningHours;
    TextView mRestaurantDistance;
    TextView mRestaurantLunchAttendees;
    AppCompatImageView mRestaurantRating;
    AppCompatImageView mRestaurantPhoto;

    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);

        Places.initialize(itemView.getContext(), String.valueOf(R.string.google_maps_key));
        mPlacesClient = Places.createClient(itemView.getContext());

        mRestaurantName = itemView.findViewById(R.id.item_restaurant_name);
        mRestaurantAddress = itemView.findViewById(R.id.item_restaurant_address);
        mRestaurantOpeningHours = itemView.findViewById(R.id.item_restaurant_opening_hours);
        mRestaurantDistance = itemView.findViewById(R.id.item_restaurant_distance);
        mRestaurantLunchAttendees = itemView.findViewById(R.id.item_restaurant_lunch_attendees);
        mRestaurantRating = itemView.findViewById(R.id.item_restaurant_rating);
        mRestaurantPhoto = itemView.findViewById(R.id.item_restaurant_photo);
    }

    void populateRestaurantItem(Restaurant restaurant) {
        mRestaurantName.setText(restaurant.getName());
        mRestaurantAddress.setText(restaurant.getAddress());
        mRestaurantOpeningHours.setText(restaurant.getOpeningHours().toString());
        mRestaurantDistance.setText(restaurant.getDistance());
        if (!restaurant.getLunchAttendees().isEmpty() && restaurant.getLunchAttendees() != null) {
            String lunchAttendeesNumber = "(" + restaurant.getLunchAttendees().size() + ")";
            mRestaurantLunchAttendees.setText(lunchAttendeesNumber);
        }
        if (2 <= restaurant.getRating() && restaurant.getRating() < 3) mRestaurantRating.setImageResource(R.drawable.ic_star_1);
        if (3 <= restaurant.getRating() && restaurant.getRating() < 3) mRestaurantRating.setImageResource(R.drawable.ic_star_2);
        if (4 <= restaurant.getRating()) mRestaurantRating.setImageResource(R.drawable.ic_star_3);

        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(restaurant.getPhoto()).build();
        mPlacesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            mRestaurantPhoto.setImageBitmap(bitmap);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }
}
