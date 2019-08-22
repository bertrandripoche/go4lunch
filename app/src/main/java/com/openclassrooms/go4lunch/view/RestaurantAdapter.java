package com.openclassrooms.go4lunch.view;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Restaurant;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {
    private static final String TAG = "RestaurantAdapter";
    private List<Restaurant> mRestaurantsList;
    private PlacesClient mPlacesClient;

    public RestaurantAdapter(List<Restaurant> mRestaurantsList) {
        this.mRestaurantsList = mRestaurantsList;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_restaurant_item,parent,false);

        String key = parent.getResources().getString(R.string.google_maps_key);
        Places.initialize(parent.getContext(), key);
        mPlacesClient = Places.createClient(parent.getContext());

        return new RestaurantViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        Restaurant restaurant = mRestaurantsList.get(position);
        
        holder.mRestaurantName.setText(restaurant.getName());
        holder.mRestaurantName.setTag(restaurant.getId());

        holder.mRestaurantAddress.setText(restaurant.getAddress());
        if (restaurant.getOpeningHours() != null) {
            String placeOpeningHours = restaurant.getOpeningHours().getWeekdayText().get(getDayOfWeek());
            holder.mRestaurantOpeningHours.setText(placeOpeningHours);
        }
        if (restaurant.getDistance() != null) holder.mRestaurantDistance.setText(restaurant.getDistance());

        System.out.println("RATING : "+restaurant.getRating());
        if (2 <= restaurant.getRating() && restaurant.getRating() < 3) holder.mRestaurantRating.setImageResource(R.drawable.ic_star_1);
        if (3 <= restaurant.getRating() && restaurant.getRating() < 4) holder.mRestaurantRating.setImageResource(R.drawable.ic_star_2);
        if (4 <= restaurant.getRating()) holder.mRestaurantRating.setImageResource(R.drawable.ic_star_3);

        if (restaurant.getPhoto() != null) {
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(restaurant.getPhoto()).build();
            mPlacesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                holder.mRestaurantPhoto.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    Log.e(TAG, "Photo and place not found: " + exception.getMessage());
                    holder.mRestaurantPhoto.setImageResource(R.drawable.bg_resto);
                }
            });
        } else{
            holder.mRestaurantPhoto.setImageResource(R.drawable.bg_resto);
        }

        if (restaurant.getLunchAttendees() != 0) {
            holder.mRestaurantLunchAttendees.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_participant, 0, 0, 0);
            String lunchAttendeesText = "(" +restaurant.getLunchAttendees()+ ")";
            holder.mRestaurantLunchAttendees.setText(lunchAttendeesText);
        }
    }

    @Override
    public int getItemCount() {
        return mRestaurantsList.size();
    }

    private int getDayOfWeek() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek;
    }
}
