package com.openclassrooms.go4lunch.controller.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;
import com.openclassrooms.go4lunch.model.Employee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestaurantActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "Restaurant Log - ";
    private Bundle mExtras;
    private String mPlaceId;
    private String mEmployeeUid;
    private PlacesClient mPlacesClient;
    private Place mPlace;
    private Button mBtnCall;
    private Button mBtnLike;
    private Button mBtnWeb;
    private FloatingActionButton mBtnLunch;
    private static boolean mLikeOn;
    private static boolean mLunchOn;
    private Employee mCurrentEmployee;

    final String PLACE_ID = "placeId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        mBtnCall=(Button)findViewById(R.id.btnCall);
        mBtnLike=(Button)findViewById(R.id.btnLike);
        mBtnWeb=(Button)findViewById(R.id.btnWeb);
        mBtnLunch=(FloatingActionButton)findViewById(R.id.mBtnLunch);

        mBtnCall.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mBtnWeb.setOnClickListener(this);
        mBtnLunch.setOnClickListener(this);

        mExtras = getBundle();
        mPlaceId = getPlaceIdFromBundle();

        getUid();
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);

        getPlaceInfo(mPlaceId);
    }

    @Override
    public void onClick(View v) {

        if (v.equals(mBtnCall)) {
            if (mPlace.getPhoneNumber() != null) {
                String phoneNumber = mPlace.getPhoneNumber();

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+phoneNumber));
                startActivity(callIntent);

            } else {
                System.out.println("Pas de téléphone");
                Toast.makeText(this, R.string.no_phone_available, Toast.LENGTH_LONG).show();
            }

        }
        if (v.equals(mBtnLike)) {
            if (mLikeOn) {
                mBtnLike.setTextColor(getResources().getColor(R.color.orange));
                mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star, 0, 0);
                mLikeOn = false;
            } else {
                this.addLikedRestaurantForEmployeeInFirestore();
                mBtnLike.setTextColor(getResources().getColor(R.color.yellow));
                mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_1, 0, 0);
                mLikeOn = true;
            }
        }
        if (v.equals(mBtnWeb)) {
            final String RESTAURANT_URL = "restaurantUrl";

            Bundle bundle = new Bundle();
            String restaurantUrl = mPlace.getWebsiteUri().toString();
            bundle.putString(RESTAURANT_URL,restaurantUrl);
            System.out.println("Url "+restaurantUrl);

            Intent intent = new Intent(getApplicationContext(), RestaurantWebPageActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        if (v.equals(mBtnLunch)) {
            if (mLunchOn) {
                this.removeLunchRestaurantForEmployeeInFirestore();
                mBtnLunch.setImageResource(R.drawable.ic_my_choice_off);
                mLunchOn = false;
            } else {
                this.addLunchRestaurantForEmployeeInFirestore();
                mBtnLunch.setImageResource(R.drawable.ic_my_choice_on);
                mLunchOn = true;
            }
        }
    }

    private void getUid() {
        if (this.getCurrentUser() != null){
            mEmployeeUid = this.getCurrentUser().getUid();
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

            checkLunchAndLike();
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
        String name = (mPlace == null) ? "":mPlace.getName();
        restaurantName.setText(name);

        TextView restaurantAddress = (TextView)findViewById(R.id.restaurant_activity_address);
        String address = (mPlace == null) ? "":mPlace.getAddress();
        restaurantAddress.setText(address);

        AppCompatImageView stars = (AppCompatImageView)findViewById(R.id.restaurant_activity_stars);
        if (2 <= mPlace.getRating() && mPlace.getRating() < 3) stars.setImageResource(R.drawable.ic_star_1);
        if (3 <= mPlace.getRating() && mPlace.getRating() < 3) stars.setImageResource(R.drawable.ic_star_2);
        if (4 <= mPlace.getRating()) stars.setImageResource(R.drawable.ic_star_3);

        if (mPlace.getPhoneNumber() == null) {
            mBtnCall.setEnabled(false);
            mBtnCall.setTextColor(getResources().getColor(R.color.light_grey));
            mBtnCall.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_call_off, 0, 0);
        }
        if (mPlace.getWebsiteUri() == null) {
            mBtnWeb.setEnabled(false);
            mBtnWeb.setTextColor(getResources().getColor(R.color.light_grey));
            mBtnWeb.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_web_off, 0, 0);
        }
    }

    private void addLikedRestaurantForEmployeeInFirestore(){
        if (this.getCurrentUser() != null){
            String likedRestaurant = mPlace.getName();
            List<String> likedPlaces = new ArrayList<String>();
            likedPlaces.add(likedRestaurant);
            EmployeeHelper.updateLikedPlaces(mEmployeeUid, likedPlaces).addOnFailureListener(this.onFailureListener());
        }
    }

    private void removeLikedRestaurantForEmployeeInFirestore() {
        if (this.getCurrentUser() != null){
            EmployeeHelper.updateLunchPlace(mEmployeeUid, null).addOnFailureListener(this.onFailureListener());
        }
    }

    private void addLunchRestaurantForEmployeeInFirestore(){
        if (this.getCurrentUser() != null){
            String lunchRestaurant = mPlace.getName();
            String lunchRestaurantId = mPlace.getId();
            EmployeeHelper.updateLunchPlace(mEmployeeUid, lunchRestaurant).addOnFailureListener(this.onFailureListener());
            EmployeeHelper.updateLunchPlaceId(mEmployeeUid, lunchRestaurantId).addOnFailureListener(this.onFailureListener());
        }
    }

    private void removeLunchRestaurantForEmployeeInFirestore() {
        if (this.getCurrentUser() != null){
            EmployeeHelper.updateLunchPlace(mEmployeeUid, null).addOnFailureListener(this.onFailureListener());
            EmployeeHelper.updateLunchPlaceId(mEmployeeUid, null).addOnFailureListener(this.onFailureListener());
        }
    }

    private void checkLunchAndLike() {
        if (this.getCurrentUser() != null){
            EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    mCurrentEmployee = documentSnapshot.toObject(Employee.class);

                    String thisRestaurant = mPlace.getName();
                    String lunchPlace = mCurrentEmployee.getLunchPlace();
                    List<String> likedPlaces = mCurrentEmployee.getLikedPlaces();
                    System.out.println("Lunch - "+lunchPlace+"-Resto actuel"+mPlace.getName()+"-Resto likés"+likedPlaces);
                    if (lunchPlace != null && lunchPlace.equals(thisRestaurant)) {
                        mBtnLunch.setImageResource(R.drawable.ic_my_choice_on);
                        mLunchOn = true;
                    }
                    if (likedPlaces != null && likedPlaces.contains(thisRestaurant)) {
                        mBtnLike.setTextColor(getResources().getColor(R.color.yellow));
                        mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_1, 0, 0);
                        mLikeOn = true;
                    }
                }
            });
        }
    }

}

