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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.controller.activity.PrincipalActivity;

public class BaseFragment extends Fragment {

    private static final String TAG = "BaseFragment - ";

    protected static RectangularBounds mLastKnownLocationBounds;
    protected static PlacesClient mPlacesClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        PrincipalActivity principalActivity = (PrincipalActivity) getActivity();
        principalActivity.mMySearch.addTextChangedListener(textWatcher);

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
                    .setLocationBias(bounds)
                    .setCountry("fr")
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(query.toString())
                    .build();

            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                    if (prediction.getPlaceTypes().contains(Place.Type.RESTAURANT)) {
                        System.out.println(prediction.getPlaceId()+"-"+prediction.getPrimaryText(null)+" est un restaurant.");
                    } else {
                        System.out.println(prediction.getPlaceId()+"-"+prediction.getPrimaryText(null)+" N'est PAS un restaurant.");
                    }

                }
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
        }
    }
}
