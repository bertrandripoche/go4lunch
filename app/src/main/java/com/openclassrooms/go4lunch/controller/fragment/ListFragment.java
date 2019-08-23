package com.openclassrooms.go4lunch.controller.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controller.activity.PrincipalActivity;
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

public class ListFragment extends Fragment {
    @BindView(R.id.fragment_list_recycler_view) RecyclerView mRecyclerView;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "List Fragment";
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private Place mPlace;
    private PlacesClient mPlacesClient;
    private final LatLng mDefaultLocation = new LatLng(48.864716, 2.349014);
    private List<Restaurant> mRestaurantList = new ArrayList<>();

    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private CollectionReference mRestaurantsRef = mDb.collection("restaurants");

    private RestaurantAdapter mAdapter;
    private TextView mRestaurantName;

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

        getLocationPermission();
        getDeviceLocation();

        configureRecyclerView();
        configureOnClickRecyclerView();

        return view;
    }

    private void configureRecyclerView() {
        mRestaurantList = new ArrayList<>();

        mAdapter = new RestaurantAdapter(mRestaurantList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

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
                                List<Restaurant> pendingRestaurantList = new ArrayList<>();

                                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                    Place currPlace = placeLikelihood.getPlace();

                                    if (currPlace.getTypes().contains(Place.Type.RESTAURANT)) {
                                        getPlaceInfo(currPlace.getId(), pendingRestaurantList);
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

    private void getPlaceInfo(String placeId, List<Restaurant> pendingRestaurantList) {
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
                    int placeLunchAttendees;
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document != null) {
                            Restaurant restaurant = document.toObject(Restaurant.class);
                            placeLunchAttendees = (restaurant != null) ? restaurant.getLunchAttendees() : 0;
                        } else {
                            placeLunchAttendees = 0;
                            Log.d("LOGGER", "No such document");
                        }
                    } else {
                        placeLunchAttendees = 0;
                        Log.d("LOGGER", "get failed with ", task.getException());
                    }

                    pendingRestaurantList.add(new Restaurant(placeId, placeName, null, placeLunchAttendees, placeAddress, placeOpeningHours, placeDistance, placeRating, placePhotoMetadata));
                    updateUI(pendingRestaurantList);
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

    void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recycler_view_restaurant_item)
                .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        final String PLACE_ID = "placeId";
                        mRestaurantName = v.findViewById(R.id.item_restaurant_name);
                        String lunchPlaceId = (String) mRestaurantName.getTag();

                        if (lunchPlaceId != null && lunchPlaceId != "null") {
                            Bundle bundle = new Bundle();
                            bundle.putString(PLACE_ID, lunchPlaceId);

                            Intent intent = new Intent(v.getContext(), RestaurantActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(),R.string.no_lunch_defined,Toast.LENGTH_LONG).show();
                        }
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

    private void updateUI(List<Restaurant> restaurantList){
        mRestaurantList.clear();
        mRestaurantList.addAll(restaurantList);
        mAdapter.notifyDataSetChanged();
    }

}
