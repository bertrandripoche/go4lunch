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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controller.activity.PrincipalActivity;
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
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private LatLng mLatLngLastKnownLocation;

//    private RectangularBounds mLastKnownLocationBounds;
//    private PlacesClient mPlacesClient;

    private final LatLng mDefaultLocation = new LatLng(48.864716, 2.349014);
    private static final int DEFAULT_ZOOM = 19;

    public MapFragment() {
    }

    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Places.initialize(getContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(getContext());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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


    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null) mMap.clear();
        getRestaurants();
    }

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

    @Override
    public boolean onMyLocationButtonClick() {
        getRestaurants();
        return false;
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
        updateLocationUI();
    }

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

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
        }
    }

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

    private void getRestaurants() {
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
                                                if (restaurant.getLunchAttendees() > 0) iconName = "ic_markers_restaurant_green";
                                            } else {
//                                                Log.d(TAG, "Current data: null");
                                            }
                                            mMap.addMarker(
                                                new MarkerOptions()
                                                        .position(new LatLng(mLikelyPlaceLatLngs.latitude, mLikelyPlaceLatLngs.longitude))
                                                        .title(mLikelyPlaceNames)
                                                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(iconName,150,150)))
                                                ).setTag(currPlace.getId());
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

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getContext().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

//    public TextWatcher textWatcher = new TextWatcher() {
//        @Override
//        public void onTextChanged(CharSequence s, int start, int before, int count) {
//        }
//
//        @Override
//        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        }
//
//        @Override
//        public void afterTextChanged(Editable s) {
//            makeSearch(s.toString());
//        }
//    };
//
//    private void getBound(LatLng coordinates) {
//        double latStart = coordinates.latitude - 0.005;
//        double latStop = coordinates.latitude + 0.005;
//        double lngStart = coordinates.longitude - 0.005;
//        double lngStop = coordinates.longitude + 0.005;
//
//        mLastKnownLocationBounds = RectangularBounds.newInstance(
//                new LatLng(latStart, lngStart),
//                new LatLng(latStop, lngStop));
//    }
//
//    public void makeSearch(String query) {
//        if (mLastKnownLocationBounds != null) {
//            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
//            System.out.println("mLastKnownLocationBounds is : "+mLastKnownLocationBounds);
//            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
//                    .setLocationBias(mLastKnownLocationBounds)
//                    .setCountry("fr")
//                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
//                    .setSessionToken(token)
//                    .setQuery(query.toString())
//                    .build();
//
//            mPlacesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
//                for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
//                    if (prediction.getPlaceTypes().contains(Place.Type.RESTAURANT)) {
//                        System.out.println(prediction.getPlaceId()+"-"+prediction.getPrimaryText(null)+" est un restaurant.");
//                    } else {
//                        System.out.println(prediction.getPlaceId()+"-"+prediction.getPrimaryText(null)+" N'est PAS un restaurant.");
//                    }
//
//                }
//            }).addOnFailureListener((exception) -> {
//                if (exception instanceof ApiException) {
//                    ApiException apiException = (ApiException) exception;
//                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
//                }
//            });
//        }
//    }
}