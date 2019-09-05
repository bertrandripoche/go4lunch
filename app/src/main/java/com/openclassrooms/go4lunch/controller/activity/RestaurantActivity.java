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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;
import com.openclassrooms.go4lunch.model.Attendee;
import com.openclassrooms.go4lunch.model.Employee;
import com.openclassrooms.go4lunch.view.AttendeesAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.activity_restaurant_recycler_view) RecyclerView mRecyclerView;

    private static final String TAG = "Restaurant Log - ";
    private Bundle mExtras;
    private String mPlaceId;
    private String mEmployeeUid;
    private String mEmployeeName;
    private String mEmployeePic;
    private String mLunchPlaceToRemove;
    private PlacesClient mPlacesClient;
    private Place mPlace;
    private Button mBtnCall;
    private Button mBtnLike;
    private Button mBtnWeb;
    private FloatingActionButton mBtnLunch;
    private static boolean sLikeOn = false;
    private static boolean sLunchOn = false;
    private Employee mCurrentEmployee;
    private List<Employee> mEmployeeList;

    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();
    private CollectionReference mRestaurantAttendeesRef;
    private AttendeesAdapter mAdapter;

    final String PLACE_ID = "placeId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        ButterKnife.bind(this);

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

        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);

        getEmployeeInfo();
        getPlaceInfo(mPlaceId);

        configureRecyclerView();
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
                Toast.makeText(this, R.string.no_phone_available, Toast.LENGTH_LONG).show();
            }

        }
        if (v.equals(mBtnLike)) {
            if (sLikeOn) {
                this.removeLikeInFirestore();
//                mBtnLike.setTextColor(getResources().getColor(R.color.orange));
//                mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb_off, 0, 0);
//                sLikeOn = false;
            } else {
                this.addLikeInFirestore();
//                mBtnLike.setTextColor(getResources().getColor(R.color.blue));
//                mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb, 0, 0);
//                sLikeOn = true;
            }
        }
        if (v.equals(mBtnWeb)) {
            final String RESTAURANT_URL = "restaurantUrl";

            Bundle bundle = new Bundle();
            String restaurantUrl = mPlace.getWebsiteUri().toString();
            bundle.putString(RESTAURANT_URL,restaurantUrl);

            Intent intent = new Intent(getApplicationContext(), RestaurantWebPageActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        if (v.equals(mBtnLunch)) {
            if (sLunchOn) {
                this.removeLunchInFirestore();
//                mBtnLunch.setImageResource(R.drawable.ic_my_choice_off);
//                sLunchOn = false;
            } else {
                this.addLunchInFirestore();
//                mBtnLunch.setImageResource(R.drawable.ic_my_choice_on);
//                sLunchOn = true;
            }
        }
    }

    private void configureRecyclerView() {
        mRestaurantAttendeesRef = mDb.collection("restaurants").document(mPlaceId).collection("lunchAttendees");
        Query query = mRestaurantAttendeesRef.orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Attendee> options = new FirestoreRecyclerOptions.Builder<Attendee>()
                .setQuery(query, Attendee.class)
                .build();

        mAdapter = new AttendeesAdapter(options);

        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    private void getEmployeeInfo() {
        if (this.getCurrentUser() != null){
            mEmployeeUid = this.getCurrentUser().getUid();
            mEmployeeName = this.getCurrentUser().getDisplayName();
            mEmployeePic = (this.getCurrentUser().getPhotoUrl() == null) ? null : this.getCurrentUser().getPhotoUrl().toString();
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
            createRestaurantInFirestore();

            PhotoMetadata photoMetadata = Objects.requireNonNull(mPlace.getPhotoMetadatas()).get(0);

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
        if (3 <= mPlace.getRating() && mPlace.getRating() < 4) stars.setImageResource(R.drawable.ic_star_1);
        if (4 <= mPlace.getRating() && mPlace.getRating() < 4.5) stars.setImageResource(R.drawable.ic_star_2);
        if (4.5 <= mPlace.getRating()) stars.setImageResource(R.drawable.ic_star_3);

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

    private void createRestaurantInFirestore() {
        Map<String, Object> docData = new HashMap<>();
        docData.put("name", mPlace.getName());
        docData.put("id", mPlaceId);

        mDb.collection("restaurants").document(mPlaceId).set(docData, SetOptions.merge());
    }

    private void addLikeInFirestore(){
        if (this.getCurrentUser() != null){

            HashMap<String, String> employeeInfo = new HashMap<String, String>();
            employeeInfo.put("name", mEmployeeName);
            employeeInfo.put("urlPic", mEmployeePic);

            WriteBatch batch = mDb.batch();
            DocumentReference employeeRef = mDb.collection("employees").document(mEmployeeUid);
            DocumentReference restaurantRef = mDb.collection("restaurants").document(mPlaceId);

            batch.update(employeeRef, "likedPlaces."+mPlaceId, mPlace.getName());
            batch.update(restaurantRef, "likes."+mEmployeeUid, employeeInfo);

            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(TAG, "Batched addLike done");
                    mBtnLike.setTextColor(getResources().getColor(R.color.blue));
                    mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb, 0, 0);
                    sLikeOn = true;
                }
            });
        }
    }

    private void removeLikeInFirestore() {
        if (this.getCurrentUser() != null){
            WriteBatch batch = mDb.batch();
            DocumentReference employeeRef = mDb.collection("employees").document(mEmployeeUid);
            DocumentReference restaurantRef = mDb.collection("restaurants").document(mPlaceId);

            batch.update(employeeRef, "likedPlaces."+mPlaceId, FieldValue.delete());
            batch.update(restaurantRef, "likes."+mEmployeeUid, FieldValue.delete());
            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(TAG, "Batched removeLike done");
                    mBtnLike.setTextColor(getResources().getColor(R.color.orange));
                    mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb_off, 0, 0);
                    sLikeOn = false;
                }
            });
        }
    }

    private void addLunchInFirestore(){
        if (this.getCurrentUser() != null){
            String lunchRestaurant = mPlace.getName();
            String lunchRestaurantId = mPlace.getId();

            WriteBatch batch = mDb.batch();
            DocumentReference employeeRef = mDb.collection("employees").document(mEmployeeUid);
            DocumentReference restaurantRef = mDb.collection("restaurants").document(mPlaceId);
            DocumentReference attendeeRef = mDb.collection("restaurants").document(mPlaceId).collection("lunchAttendees").document(mEmployeeUid);

            batch.set(attendeeRef, new Attendee(mEmployeeUid, mEmployeeName, mEmployeePic));
            batch.update(restaurantRef, "lunchAttendees", FieldValue.increment(1));
            batch.update(employeeRef, "lunchPlace", lunchRestaurant);
            batch.update(employeeRef, "lunchPlaceId", lunchRestaurantId);
            if (mLunchPlaceToRemove != null && !mLunchPlaceToRemove.equals(mPlaceId)) {
                DocumentReference attendeeToRemoveRef = mDb.collection("restaurants").document(mLunchPlaceToRemove).collection("lunchAttendees").document(mEmployeeUid);
                batch.delete(attendeeToRemoveRef);
                DocumentReference attendeeToRemoveForCounterRef = mDb.collection("restaurants").document(mLunchPlaceToRemove);
                batch.update(attendeeToRemoveForCounterRef, "lunchAttendees", FieldValue.increment(-1));
            }
            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(TAG, "Batched addLunch done");
                    mBtnLunch.setImageResource(R.drawable.ic_my_choice_on);
                    sLunchOn = true;
                }
            });
        }
    }

    private void removeLunchInFirestore() {
        if (this.getCurrentUser() != null){
            WriteBatch batch = mDb.batch();
            DocumentReference employeeRef = mDb.collection("employees").document(mEmployeeUid);
            DocumentReference restaurantRef = mDb.collection("restaurants").document(mPlaceId);
            DocumentReference attendeeToRemoveRef = mDb.collection("restaurants").document(mPlaceId).collection("lunchAttendees").document(mEmployeeUid);

            batch.update(employeeRef, "lunchPlace", null);
            batch.update(employeeRef, "lunchPlaceId", null);

            batch.update(restaurantRef, "lunchAttendees", FieldValue.increment(-1));
            batch.delete(attendeeToRemoveRef);
            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.i(TAG, "Batched removeLunch done");
                    mBtnLunch.setImageResource(R.drawable.ic_my_choice_off);
                    sLunchOn = false;
                }
            });
        }
    }

    private void checkLunchAndLike() {
        if (this.getCurrentUser() != null){
            EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    mCurrentEmployee = documentSnapshot.toObject(Employee.class);

                    String thisRestaurant = mPlace.getName();
                    String thisRestaurantId = mPlace.getId();
                    String lunchPlace = mCurrentEmployee.getLunchPlace();
                    mLunchPlaceToRemove = mCurrentEmployee.getLunchPlaceId();
                    HashMap<String, String> likedPlaces = mCurrentEmployee.getLikedPlaces();

                    if (lunchPlace != null && lunchPlace.equals(thisRestaurant)) {
                        mBtnLunch.setImageResource(R.drawable.ic_my_choice_on);
                        sLunchOn = true;
                    } else {sLunchOn = false;}
                    if (likedPlaces != null && likedPlaces.containsKey(thisRestaurantId)) {
                        mBtnLike.setTextColor(getResources().getColor(R.color.blue));
                        mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_thumb, 0, 0);
                        sLikeOn = true;
                    } else {sLikeOn = false;}
                }
            });
        }
    }

}

