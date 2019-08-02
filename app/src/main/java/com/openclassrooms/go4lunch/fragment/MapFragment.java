package com.openclassrooms.go4lunch.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.R;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = "Map Fragment";
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    private LatLng mLatLngLastKnownLocation;

    private PlacesClient mPlacesClient;

    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private LatLng[] mLikelyPlaceLatLngs;

    private final LatLng mDefaultLocation = new LatLng(48.864716, 2.349014);
    private static final int DEFAULT_ZOOM = 18;

    public MapFragment() {
        // Required empty public constructor
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

    @Override
    public void onCameraIdle() {
        Toast.makeText(getActivity(), "The camera is moving.", Toast.LENGTH_SHORT).show();

        double mylat = mMap.getCameraPosition().target.latitude;
        double mylon = mMap.getCameraPosition().target.longitude;
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
                //mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
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
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mLatLngLastKnownLocation =  new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngLastKnownLocation, DEFAULT_ZOOM));

                            getRestaurants();
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

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getRestaurants() {
        // Use fields to define the data types to return.
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
                            // Set the count, handling cases where less than 5 entries are returned.
                            int count;
                            System.out.println("TAILLE : "+response.getPlaceLikelihoods().size());
                            if (response.getPlaceLikelihoods().size() < 100) {
                                count = response.getPlaceLikelihoods().size();
                            } else {
                                count = 100;
                            }

                            int i = 0;
                            mLikelyPlaceNames = new String[count];
                            mLikelyPlaceAddresses = new String[count];
                            mLikelyPlaceLatLngs = new LatLng[count];

                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                                Place currPlace = placeLikelihood.getPlace();

                                if (currPlace.getTypes().contains(Place.Type.RESTAURANT)) {
                                    mLikelyPlaceNames[i] = currPlace.getName();
                                    mLikelyPlaceAddresses[i] = currPlace.getAddress();
                                    mLikelyPlaceLatLngs[i] = currPlace.getLatLng();
                                    System.out.println("Id du resto : "+currPlace.getId());

                                    String currLatLng = (mLikelyPlaceLatLngs[i] == null) ?
                                            "" : mLikelyPlaceLatLngs[i].toString();

                                    Log.i(TAG, String.format("Place " + currPlace.getName()
                                            + " has likelihood: " + placeLikelihood.getLikelihood()
                                            + " at " + currLatLng));

                                    mMap.addMarker(new MarkerOptions().position(new LatLng(mLikelyPlaceLatLngs[i].latitude, mLikelyPlaceLatLngs[i].longitude)).title(mLikelyPlaceNames[i]).icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_markers_restaurant_red)));
                                }

                                i++;
                                if (i > (count - 1)) {
                                    break;
                                }
                            }

                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                            }
                        }
                    }
                });
        } else {
            getLocationPermission();
        }
    }
}