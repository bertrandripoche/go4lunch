package com.openclassrooms.go4lunch.controller.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controller.activity.RestaurantActivity;
import com.openclassrooms.go4lunch.model.Restaurant;
import com.openclassrooms.go4lunch.utils.ItemClickSupport;
import com.openclassrooms.go4lunch.view.RestaurantAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ListFragment extends BaseFragment {
    @BindView(R.id.fragment_list_recycler_view) RecyclerView mRecyclerView;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "List Fragment";

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private LatLng mLatLngLastKnownLocation;

    private Place mPlace;
    private List<Restaurant> mRestaurantList;

    private CollectionReference mRestaurantsRef = mDb.collection("restaurants");

    private RestaurantAdapter mAdapter;
    private TextView mRestaurantName;

    public ListFragment() {}

    public static ListFragment newInstance() {
        return (new ListFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);

        getLocationPermission();
        getDeviceLocation();

        configureRecyclerView();
        configureOnClickRecyclerView();

        return view;
    }

    /**
     * This method displays the list of PlaceId retrieved during a user Search operation
     */
    @Override
    void displayPlacesIdList() {
        mRestaurantList.clear();
        if (!mPlacesIdList.isEmpty()) {
            for (String placeId : mPlacesIdList) {
                makeRestaurantFromPlaceId(placeId);
            }
        } else {
            updateUI(mRestaurantList);
        }
    }

    /**
     * This method populates the list of restaurant to allow to display it via the RecyclerView
     * @param restaurant is a Restaurant object which provides all the needed restaurant information
     */
    @Override
    void displayPlace(Restaurant restaurant) {
        getPlaceInfo(restaurant.getId(), mRestaurantList);
    }

    /**
     * This method allows to display the restaurants around the user location
     */
    @Override
    void getStandardDisplay() {
        mRestaurantList.clear();
        getDeviceLocation();
    }

    /**
     * This method configures the RecyclerView to have the restaurants displayed
     */
    private void configureRecyclerView() {
        mRestaurantList = new ArrayList<>();

        mAdapter = new RestaurantAdapter(mRestaurantList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * This method is needed to check if location permissions are granted
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    /**
     * This method allows us to get location permissions
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * This method allows us to get the current user location
     */
    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            mLatLngLastKnownLocation =  new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                            getRestaurants();
                            mLastKnownLocationBounds = getBound(mLatLngLastKnownLocation);
                        } else {
                            mLatLngLastKnownLocation = mDefaultLocation;
                            Log.e(TAG, "Exception: %s", task.getException());
                        }

                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * This method gets the restaurants around the last known user location
     */
    private void getRestaurants() {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.TYPES, Place.Field.ID);

        final FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields).build();

        if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Task<FindCurrentPlaceResponse> placeResponse = mPlacesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(getActivity(),
                    new OnCompleteListener<FindCurrentPlaceResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                            if (task.isSuccessful()) {
                                FindCurrentPlaceResponse response = task.getResult();

                                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                    Place currPlace = placeLikelihood.getPlace();

                                    if (currPlace.getTypes().contains(Place.Type.RESTAURANT)) {
                                        getPlaceInfo(currPlace.getId(), mRestaurantList);
                                    }
                                }

                            } else {
                                Exception exception = task.getException();
                                if (exception instanceof ApiException) {
                                    ApiException apiException = (ApiException) exception;
                                    Log.e(TAG, "Restaurant not found: " + apiException.getStatusCode() + apiException.getLocalizedMessage());
                                }
                            }
                        }

                    });
        } else {
            getLocationPermission();
        }
    }

    /**
     * This method adds the restaurant specified by its id to the list of Restaurant object needed for the RecyclerView
     * @param placeId is the unique id of the restaurant to add to the list
     * @param restaurantList is the list of Restaurant to populate
     */
    private void getPlaceInfo(String placeId, List<Restaurant> restaurantList) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            mPlace = response.getPlace();
            String placeName = mPlace.getName();
            String placeAddress = mPlace.getAddress();
            Double placeRating = mPlace.getRating();
            LatLng placeLatLng = mPlace.getLatLng();
            OpeningHours placeOpeningHours = mPlace.getOpeningHours();
            String placeDistance = getDistanceFromLastKnownLocation(mPlace.getLatLng().latitude, mPlace.getLatLng().longitude);
            PhotoMetadata placePhotoMetadata = (mPlace.getPhotoMetadatas() == null) ? null: mPlace.getPhotoMetadatas().get(0);

            DocumentReference docRef = mRestaurantsRef.document(mPlace.getId());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    int placeLunchAttendees = 0;
                    if (snapshot != null && snapshot.exists()) {
                        Log.d(TAG, "Current data: " + snapshot.getData());
                        Restaurant restaurant = snapshot.toObject(Restaurant.class);
                        assert restaurant != null;
                        placeLunchAttendees = (restaurant != null && restaurant.getLunchAttendees() != null) ? restaurant.getLunchAttendees() : 0;
                    } else {
                        Log.d(TAG, "Current data: null");
                    }

                    Restaurant restaurant = findUsingEnhancedForLoop(placeName, restaurantList);
                    if (restaurant != null) {
                        restaurant.setLunchAttendees(placeLunchAttendees);
                    }
                    else {
                        restaurantList.add(new Restaurant(placeId, placeName, null, placeLunchAttendees, placeAddress, placeOpeningHours, placeLatLng, placeDistance, placeRating, placePhotoMetadata));
                    }
                    updateUI(restaurantList);
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

    /**
     * This method defines the action to do when clicking on an element from the list of restaurant
     */
    void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recycler_view_restaurant_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        final String PLACE_ID = "placeId";
                        mRestaurantName = v.findViewById(R.id.item_restaurant_name);
                        String restaurantId = (String) mRestaurantName.getTag();

                        if (restaurantId != null && restaurantId != "null") {
                            Bundle bundle = new Bundle();
                            bundle.putString(PLACE_ID, restaurantId);

                            Intent intent = new Intent(v.getContext(), RestaurantActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(),R.string.no_restaurant_found,Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * This method calculate the distance of the restaurant to the current user location
     * @param lat is the latitude of the location
     * @param lng is the longitude of the location
     * @return the distance in meters
     */
    public String getDistanceFromLastKnownLocation(Double lat, Double lng) {
        Location targetLocation = new Location("");
        targetLocation.setLatitude(lat);
        targetLocation.setLongitude(lng);

        float distance =  targetLocation.distanceTo(mLastKnownLocation);
        return (int)distance + "m";
    }

    /**
     * This methods refreshes the RecyclerView
     * @param restaurantList is the current list of Restaurants to display
     */
    private void updateUI(List<Restaurant> restaurantList){
        mAdapter.notifyDataSetChanged();
    }

    /**
     * This method searches if the restaurant name already exists in the restaurant list
     * @param name is the name of the restaurant to search
     * @param restaurantList is the restaurant list
     * @return the Restaurant object if found
     */
    public Restaurant findUsingEnhancedForLoop(String name, List<Restaurant> restaurantList) {

        for (Restaurant restaurant : restaurantList) {
            if (restaurant.getName().equals(name)) {
                return restaurant;
            }
        }
        return null;
    }
}
