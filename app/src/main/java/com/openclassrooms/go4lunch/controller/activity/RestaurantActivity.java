package com.openclassrooms.go4lunch.controller.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Restaurant;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.CALL_PHONE;

public class RestaurantActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Restaurant Log - ";
    private Bundle mExtras;
    private String mPlaceId;
    private PlacesClient mPlacesClient;
    private Place mPlace;
    private Restaurant mRestaurant;
    private Button mBtnCall;
    private Button mBtnLike;
    private Button mBtnWeb;

    final String PLACE_ID = "placeId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        mBtnCall=(Button)findViewById(R.id.btnCall);
        mBtnLike=(Button)findViewById(R.id.btnLike);
        mBtnWeb=(Button)findViewById(R.id.btnWeb);

        mBtnCall.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mBtnWeb.setOnClickListener(this);

        mExtras = getBundle();
        mPlaceId = getPlaceIdFromBundle();

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);

        getPlaceInfo(mPlaceId);
    }


    @Override
    public void onClick(View v) {

        if (v.equals(mBtnCall)) {
            if (mRestaurant.getPhone() != null) {
                String phoneNumber = mRestaurant.getPhone();

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+phoneNumber));

                if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{CALL_PHONE}, 1);
                }

            } else {
                System.out.println("Pas de téléphone");
                Toast.makeText(this, R.string.no_phone_available, Toast.LENGTH_LONG).show();
            }

        }
        else if (v.equals(mBtnLike)) {
            // DO SOMETHING NICE

        }
        if (v.equals(mBtnWeb)) {
            final String RESTAURANT_URL = "restaurantUrl";

            Bundle bundle = new Bundle();
            String restaurantUrl = mRestaurant.getUrl().toString();
            bundle.putString(RESTAURANT_URL,restaurantUrl);
            System.out.println("Url "+restaurantUrl);

            Intent intent = new Intent(getApplicationContext(), RestaurantWebPageActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

    }

    private Bundle getBundle() {
        return this.getIntent().getExtras();
    }

    private String getPlaceIdFromBundle() {
        if (mExtras.get(PLACE_ID) != null) return mExtras.get(PLACE_ID).toString();
        else return null;
    }

    private void getPlaceInfo(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            mPlace = response.getPlace();
            mRestaurant = new Restaurant(mPlace.getId(),mPlace.getName(),mPlace.getAddress(), mPlace.getPhoneNumber(), mPlace.getWebsiteUri(), mPlace.getRating());
System.out.println("Info resto : "+mPlace.getWebsiteUri());
            displayRestaurant();

            PhotoMetadata photoMetadata = mPlace.getPhotoMetadatas().get(0);

            AppCompatImageView imageView = (AppCompatImageView)findViewById(R.id.restaurant_activity_pic);

            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();
            mPlacesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                imageView.setImageBitmap(bitmap);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    Log.e(TAG, "Place not found: " + exception.getMessage());
                }
            });
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                Log.e(TAG, "Restaurant not found: " + exception.getMessage());
            }
        });

    }

    private void displayRestaurant() {
        TextView restaurantName = (TextView)findViewById(R.id.restaurant_activity_name);
        String name = (mRestaurant == null) ? "":mRestaurant.getName();
        restaurantName.setText(name);

        TextView restaurantAddress = (TextView)findViewById(R.id.restaurant_activity_address);
        String address = (mRestaurant == null) ? "":mRestaurant.getAddress();
        restaurantAddress.setText(address);

        AppCompatImageView stars = (AppCompatImageView)findViewById(R.id.restaurant_activity_stars);
        if (2 <= mRestaurant.getRating() && mRestaurant.getRating() < 3) stars.setImageResource(R.drawable.ic_star_1);
        if (3 <= mRestaurant.getRating() && mRestaurant.getRating() < 3) stars.setImageResource(R.drawable.ic_star_2);
        if (4 <= mRestaurant.getRating()) stars.setImageResource(R.drawable.ic_star_3);

        if (mRestaurant.getPhone() == null) {
            mBtnCall.setEnabled(false);
            mBtnCall.setTextColor(getResources().getColor(R.color.light_grey));
        }
        if (mRestaurant.getUrl() == null) {
            mBtnWeb.setEnabled(false);
            mBtnWeb.setTextColor(getResources().getColor(R.color.light_grey));
        }
    }
}

