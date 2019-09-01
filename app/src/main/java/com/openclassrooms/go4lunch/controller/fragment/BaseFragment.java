package com.openclassrooms.go4lunch.controller.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controller.activity.PrincipalActivity;
import com.openclassrooms.go4lunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseFragment extends Fragment {

    abstract void displayPlacesIdList();
    abstract void getStandardDisplay();
    abstract void displayPlace(Restaurant restaurant);

    private static final String TAG = "BaseFragment - ";

    protected static RectangularBounds mLastKnownLocationBounds;
    protected static PlacesClient mPlacesClient;
    protected static List<String> mPlacesIdList = new ArrayList<>();
    protected final LatLng mDefaultLocation = new LatLng(48.864716, 2.349014);
    protected FusedLocationProviderClient mFusedLocationProviderClient;
    protected FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    protected PrincipalActivity mPrincipalActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(getContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(getContext());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPrincipalActivity = (PrincipalActivity) getActivity();
        mPrincipalActivity.mMySearch.addTextChangedListener(textWatcher);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().equals("")) getStandardDisplay();
            mPlacesIdList.clear();
            makeSearch(s.toString(), mLastKnownLocationBounds, mPlacesClient);
        }
    };

    protected RectangularBounds getBound(LatLng coordinates) {
        double latStart = coordinates.latitude - 0.005;
        double latStop = coordinates.latitude + 0.005;
        double lngStart = coordinates.longitude - 0.005;
        double lngStop = coordinates.longitude + 0.005;

        return RectangularBounds.newInstance(
                new LatLng(latStart, lngStart),
                new LatLng(latStop, lngStop));
    }

    protected void makeSearch(String query, RectangularBounds bounds, PlacesClient placesClient) {
        if (bounds != null) {
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setLocationRestriction(bounds)
                    .setCountry("fr")
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(query.toString())
                    .build();

            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                    if (prediction.getPlaceTypes().contains(Place.Type.RESTAURANT)) {
                        mPlacesIdList.add(prediction.getPlaceId());
                    }
                }
                displayPlacesIdList();
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
        }
    }

    protected void makeRestaurantFromPlaceId(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            String name = place.getName();
            String address = place.getAddress();
            LatLng latLng = place.getLatLng();
            OpeningHours openingHours = place.getOpeningHours();
            PhotoMetadata photoMetadata = (place.getPhotoMetadatas() == null) ? null : place.getPhotoMetadatas().get(0);

            Restaurant restaurant = new Restaurant(placeId, name, null, null, address, openingHours, latLng, null, null, photoMetadata);
            createRestaurantInFirestore(restaurant);
            displayPlace(restaurant);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                Log.e(TAG, "Restaurant not found: " + exception.getMessage());
            }
        });
    }

    protected void createRestaurantInFirestore(Restaurant restaurant) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("name", restaurant.getName());
        docData.put("id", restaurant.getId());
        docData.put("address", restaurant.getAddress());
        docData.put("openingHours", restaurant.getOpeningHours());
        docData.put("photoMetadata", restaurant.getPhoto());

        mDb.collection("restaurants").document(restaurant.getId()).set(docData, SetOptions.merge());
    }

}
