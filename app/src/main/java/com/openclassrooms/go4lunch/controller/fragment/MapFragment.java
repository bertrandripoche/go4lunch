package com.openclassrooms.go4lunch.controller.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controller.activity.RestaurantActivity;
import com.openclassrooms.go4lunch.model.Restaurant;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMarkerClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "Map Fragment";
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private LatLng mLatLngLastKnownLocation;

    private static final int DEFAULT_ZOOM = 19;

    public MapFragment() {
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_map, container, false);

        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mMapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            mMapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.map, mMapFragment).commit();
        }

        mMapFragment.getMapAsync(this);

        return v;
    }

    /**
     * This method displays the list of PlaceId retrieved during a user Search operation
     */
    @Override
    void displayPlacesIdList() {
        if (!mPlacesIdList.isEmpty()) {
            mMap.clear();
            for (String placeId : mPlacesIdList) {
                makeRestaurantFromPlaceId(placeId);
            }
        }
    }

    /**
     * This method allows to display the restaurants around the user location
     */
    @Override
    void getStandardDisplay() {
        getRestaurants();
    }

    /**
     * This method populates the list of restaurant to allow to display it via the RecyclerView
     * @param restaurant is a Restaurant object which provides all the needed restaurant information
     */
    @Override
    void displayPlace(Restaurant restaurant) {

        DocumentReference documentRef = mDb.collection("restaurants").document(restaurant.getId());
        documentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                String iconName = "ic_markers_restaurant_red";
                if (snapshot != null && snapshot.exists()) {
//                                                Log.d(TAG, "Current data: " + snapshot.getData());
                    Restaurant restaurant = snapshot.toObject(Restaurant.class);
                    assert restaurant != null;
                    if (restaurant.getLunchAttendees() != null && restaurant.getLunchAttendees() > 0) iconName = "ic_markers_restaurant_green";
                } else {
//                                                Log.d(TAG, "Current data: null");
                }
                mMap.addMarker(
                        new MarkerOptions()
                                .position(new LatLng(restaurant.getLatLng().latitude, restaurant.getLatLng().longitude))
                                .title(restaurant.getName())
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName,150,150)))
                ).setTag(restaurant.getId());
            }
        });
    }

    /**
     * Display the standard map when resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) mMap.clear();
        getRestaurants();
    }

    /**
     * This method display the Google Map on the screen when ready
     * @param googleMap is the GoogleMap object to display
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }

        updateLocationUI();
        getDeviceLocation();

        enableMyLocation();
        mMap.setOnMyLocationButtonClickListener(this);

        View mapView = mMapFragment.getView();
        moveCompassButton(mapView);

        mMap.setOnMarkerClickListener(this);
    }

    /**
     * This method defines the action to take when the user clicks on the location button
     * @return a boolean
     */
    @Override
    public boolean onMyLocationButtonClick() {
        if (mPrincipalActivity.mSearchBar.getVisibility() == View.INVISIBLE) getRestaurants();
        return false;
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
        updateLocationUI();
    }

    /**
     * This method allows us to get location permissions
     */
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

    /**
     * This method moves the location button into the right bottom corner
     * @param mapView is the View on which we work
     */
    private void moveCompassButton(View mapView) {
        try {
            assert mapView != null; // skip this if the mapView has not been set yet
            View view = mapView.findViewWithTag("GoogleMapMyLocationButton");

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.setMargins(0, 0, 20, 20);

            view.setLayoutParams(layoutParams);
            view.setBackgroundColor(getResources().getColor(R.color.white));
        } catch (Exception ex) {
            Log.e(TAG, "MoveCompassButton() - failed: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    /**
     * This method enables the compass / myLocation button
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }
    }

    /**
     * This method requests permission if not granted
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * This method gets the current user location and move camera adequately
     */
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
                            mLatLngLastKnownLocation =  new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngLastKnownLocation, DEFAULT_ZOOM));

                            getRestaurants();
                            mLastKnownLocationBounds = getBound(mLatLngLastKnownLocation);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
    public void getRestaurants() {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.TYPES, Place.Field.ID);

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
                                    String mLikelyPlaceNames = currPlace.getName();
                                    LatLng mLikelyPlaceLatLngs = currPlace.getLatLng();

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                                    DocumentReference documentRef = db.collection("restaurants").document(currPlace.getId());
                                    documentRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                                            @Nullable FirebaseFirestoreException e) {
                                            if (e != null) {
                                                Log.w(TAG, "Listen failed.", e);
                                                return;
                                            }
                                            String iconName = "ic_markers_restaurant_red";
                                            if (snapshot != null && snapshot.exists()) {
//                                                Log.d(TAG, "Current data: " + snapshot.getData());
                                                Restaurant restaurant = snapshot.toObject(Restaurant.class);
                                                assert restaurant != null;
                                                if (restaurant.getLunchAttendees() != null && restaurant.getLunchAttendees() > 0) iconName = "ic_markers_restaurant_green";
                                            } else {
//                                                Log.d(TAG, "Current data: null");
                                            }
                                            if (mLikelyPlaceLatLngs != null) {
                                                mMap.addMarker(
                                                        new MarkerOptions()
                                                                .position(new LatLng(mLikelyPlaceLatLngs.latitude, mLikelyPlaceLatLngs.longitude))
                                                                .title(mLikelyPlaceNames)
                                                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName, 150, 150)))
                                                ).setTag(currPlace.getId());
                                            }
                                        }
                                    });
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
     * This method opens a new activity with the restaurant information when its marker is clicked
     * @param marker is the marker clicked
     * @return a boolean
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        final String PLACE_ID = "placeId";

        Bundle bundle = new Bundle();
        String placeId = marker.getTag().toString();
        bundle.putString(PLACE_ID,placeId);

        Intent intent = new Intent(getActivity(), RestaurantActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

        return false;
    }

    /**
     * This method get a Bitmap object to display then as marker
     * @param iconName is the name of the icon
     * @param width is the requested width
     * @param height is the requested height
     * @return a Bitmap object of the icon
     */
    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getContext().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

}