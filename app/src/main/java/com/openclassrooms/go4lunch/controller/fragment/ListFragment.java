package com.openclassrooms.go4lunch.controller.fragment;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class ListFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "List Fragment";
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private Place mPlace;
    private PlacesClient mPlacesClient;
    private final LatLng mDefaultLocation = new LatLng(48.864716, 2.349014);
    private List<Restaurant> mRestaurantList = new ArrayList<Restaurant>();

    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private CollectionReference mRestaurantsRef = mDb.collection("restaurants");

    public ListFragment() {}

    public static ListFragment newInstance() {
        return (new ListFragment());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(getContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(getContext());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);

        //updateLocationUI();
        getLocationPermission();
        getDeviceLocation();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        //updateLocationUI();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

//    private void updateLocationUI() {
//        try {
//            if (mLocationPermissionGranted) {
//                mMap.setMyLocationEnabled(true);
//                //mMap.getUiSettings().setMyLocationButtonEnabled(true);
//            } else {
//                mMap.setMyLocationEnabled(false);
//                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
//                mLastKnownLocation = null;
//                getLocationPermission();
//            }
//        } catch (SecurityException e)  {
//            Log.e("Exception: %s", e.getMessage());
//        }
//    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Location successful.");
                            mLastKnownLocation = task.getResult();

                            getRestaurants();
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }

                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

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
                                        getPlaceInfo(currPlace.getId());
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

    private void getPlaceInfo(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.OPENING_HOURS);

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        mPlacesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            mPlace = response.getPlace();
            String placeName = mPlace.getName();
            String placeAddress = mPlace.getAddress();
            Double placeRating = mPlace.getRating();
            OpeningHours placeOpeningHours = mPlace.getOpeningHours();
            String placeDistance = getDistanceFromLastKnownLocation(mPlace.getLatLng().latitude, mPlace.getLatLng().longitude);
            PhotoMetadata placePhotoMetadata = mPlace.getPhotoMetadatas().get(0);


            DocumentReference docRef = mRestaurantsRef.document(mPlace.getId());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            Restaurant thisRestaurant = document.toObject(Restaurant.class);

                            HashMap<String, HashMap<String, String>> placeLunchAttendees = thisRestaurant.getLunchAttendees();
                            mRestaurantList.add(new Restaurant(placeId, placeName, null, placeLunchAttendees, placeAddress, placeOpeningHours, placeDistance, placeRating, placePhotoMetadata));
                            System.out.println("LISTE RESTO"+mRestaurantList);
                        } else {
                            Log.d("LOGGER", "No such document");
                        }
                    } else {
                        Log.d("LOGGER", "get failed with ", task.getException());
                    }
                }
            });

            System.out.println("LISTE RESTO"+mRestaurantList);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                Log.e(TAG, "Restaurant not found: " + exception.getMessage());
            }
        });
    }

    private String getDistanceFromLastKnownLocation(Double lat, Double lng) {
        Location targetLocation = new Location("");
        targetLocation.setLatitude(lat);
        targetLocation.setLongitude(lng);

        float distance =  targetLocation.distanceTo(mLastKnownLocation);
        return (int)distance + "m";
    }
}
