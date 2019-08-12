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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.openclassrooms.go4lunch.api.RestaurantHelper;
import com.openclassrooms.go4lunch.api.TeamHelper;
import com.openclassrooms.go4lunch.model.Employee;
import com.openclassrooms.go4lunch.model.Restaurant;
import com.openclassrooms.go4lunch.model.Team;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

public class RestaurantActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "Restaurant Log - ";
    private Bundle mExtras;
    private String mPlaceId;
    private String mEmployeeUid;
    private String mEmployeeName;
    private String mEmployeePic;
    private String mEmployeeParameterForTeam;
    private String mLunchPlaceToRemove;
    private PlacesClient mPlacesClient;
    private Place mPlace;
    private Button mBtnCall;
    private Button mBtnLike;
    private Button mBtnWeb;
    private FloatingActionButton mBtnLunch;
    private static boolean mLikeOn = false;
    private static boolean mLunchOn = false;
    private Employee mCurrentEmployee;

    final String PLACE_ID = "placeId";
    final String TEAM = "team";


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
                Toast.makeText(this, R.string.no_phone_available, Toast.LENGTH_LONG).show();
            }

        }
        if (v.equals(mBtnLike)) {
            if (mLikeOn) {
                this.removeLikedRestaurantForEmployeeInFirestore();
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
            mEmployeeName = this.getCurrentUser().getDisplayName();
            mEmployeePic = (Objects.requireNonNull(this.getCurrentUser().getPhotoUrl()).toString().equals("")) ? "noPicture": this.getCurrentUser().getPhotoUrl().toString();
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
            EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    mCurrentEmployee = documentSnapshot.toObject(Employee.class);
                    String thisRestaurant = mPlace.getName();
                    String thisRestaurantId = mPlace.getId();
                    HashMap<String, String> likedPlaces = (mCurrentEmployee.getLikedPlaces() != null) ? mCurrentEmployee.getLikedPlaces(): new HashMap<String, String>();;

                    if (likedPlaces.isEmpty() || !likedPlaces.containsKey(thisRestaurantId)) {
                        likedPlaces.put(thisRestaurantId, thisRestaurant);
                    }
                    EmployeeHelper.updateLikedPlaces(mEmployeeUid, likedPlaces).addOnFailureListener(onFailureListener());
                }
            });

            RestaurantHelper.getRestaurant(mPlaceId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                    if (restaurant == null) {
                        HashMap<String, String> likes = new HashMap<String, String>();
                        likes.put(mEmployeeName,mEmployeePic);
                        RestaurantHelper.createRestaurant(mPlaceId,mPlace.getName(),likes,null).addOnFailureListener(onFailureListener());
                    } else {
                        HashMap<String, String > likes = (restaurant.getLikes() == null) ? new HashMap<String, String>() : restaurant.getLikes();
                        if (likes.isEmpty() || !likes.containsKey(mEmployeeName)) {
                            likes.put(mEmployeeName, mEmployeePic);
                        }
                        likes.put(mEmployeeName,mEmployeePic);
                        RestaurantHelper.updateLikes(mPlaceId, likes).addOnFailureListener(onFailureListener());
                    }
                }
            });


        }
    }

    private void removeLikedRestaurantForEmployeeInFirestore() {
        if (this.getCurrentUser() != null){
            EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    mCurrentEmployee = documentSnapshot.toObject(Employee.class);
                    String thisRestaurant = mPlace.getName();
                    String thisRestaurantId = mPlace.getId();
                    HashMap<String, String > likedPlaces = mCurrentEmployee.getLikedPlaces();

                    if (likedPlaces != null && likedPlaces.containsKey(thisRestaurantId)) {
                        likedPlaces.remove(thisRestaurantId);
                    }
                    EmployeeHelper.updateLikedPlaces(mEmployeeUid, likedPlaces).addOnFailureListener(onFailureListener());
                }
            });

            RestaurantHelper.getRestaurant(mPlaceId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                    HashMap<String, String > likes = restaurant.getLikes();
                    if (likes != null && likes.containsKey(mEmployeeName)) {
                        likes.remove(mEmployeeName);
                    }
                    RestaurantHelper.updateLikes(mPlaceId, likes).addOnFailureListener(onFailureListener());
                }
            });
        }
    }

    private void addLunchRestaurantForEmployeeInFirestore(){
        if (this.getCurrentUser() != null){
            String lunchRestaurant = mPlace.getName();
            String lunchRestaurantId = mPlace.getId();

            EmployeeHelper.updateLunchPlace(mEmployeeUid, lunchRestaurant).addOnFailureListener(this.onFailureListener());
            EmployeeHelper.updateLunchPlaceId(mEmployeeUid, lunchRestaurantId).addOnFailureListener(this.onFailureListener());

            RestaurantHelper.getRestaurant(mPlaceId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                    if (restaurant == null) {
                        HashMap<String, String> lunchAttendees = new HashMap<String, String>();
                        lunchAttendees.put(mEmployeeName,mEmployeePic);
                        RestaurantHelper.createRestaurant(mPlaceId,mPlace.getName(),null,lunchAttendees).addOnFailureListener(onFailureListener());
                    } else {
                        HashMap<String, String > lunchAttendees = (restaurant.getLunchAttendees() == null) ? new HashMap<String, String>() : restaurant.getLunchAttendees();
                        if (lunchAttendees.isEmpty() || !lunchAttendees.containsKey(mEmployeeName)) {
                            lunchAttendees.put(mEmployeeName, mEmployeePic);
                        }
                        lunchAttendees.put(mEmployeeName,mEmployeePic);
                        RestaurantHelper.updateLunchAttendees(mPlaceId, lunchAttendees).addOnFailureListener(onFailureListener());
                    }

                }
            });

            TeamHelper.getTeam().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Team team = documentSnapshot.toObject(Team.class);
                    HashMap<String, String > lunchPlaceByEmployee = (team.getLunchPlaceByEmployee() == null) ? new HashMap<String, String>() : team.getLunchPlaceByEmployee();

                    String valueForTeam = (mEmployeePic == null) ? "Null___"+mPlace.getName() : mEmployeePic+"___"+mPlace.getName();
                    lunchPlaceByEmployee.put(mEmployeeName, valueForTeam);
                    TeamHelper.updateLunchPlaceByEmployee(lunchPlaceByEmployee);
                }
            });

            System.out.println("mLunchPlaceToRemove : "+mLunchPlaceToRemove+" - mPlaceId : "+mPlaceId);
            if (mLunchPlaceToRemove != null && mLunchPlaceToRemove != mPlaceId)  {
                System.out.println("Lieu à effacer : "+mLunchPlaceToRemove);
                RestaurantHelper.getRestaurant(mLunchPlaceToRemove).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                        HashMap<String, String > lunchAttendees = restaurant.getLunchAttendees();
                        if (lunchAttendees != null && lunchAttendees.containsKey(mEmployeeName)) {
                            lunchAttendees.remove(mEmployeeName);
                        }
                        RestaurantHelper.updateLunchAttendees(mLunchPlaceToRemove, lunchAttendees).addOnFailureListener(onFailureListener());
                    }
                });
            }
        }
    }

    private void removeLunchRestaurantForEmployeeInFirestore() {
        if (this.getCurrentUser() != null){
            EmployeeHelper.updateLunchPlace(mEmployeeUid, null).addOnFailureListener(this.onFailureListener());
            EmployeeHelper.updateLunchPlaceId(mEmployeeUid, null).addOnFailureListener(this.onFailureListener());

            RestaurantHelper.getRestaurant(mPlaceId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                    HashMap<String, String > lunchAttendees = restaurant.getLunchAttendees();
                    if (lunchAttendees != null && lunchAttendees.containsKey(mEmployeeName)) {
                        lunchAttendees.remove(mEmployeeName);
                    }
                    RestaurantHelper.updateLunchAttendees(mPlaceId, lunchAttendees).addOnFailureListener(onFailureListener());
                }
            });

            TeamHelper.getTeam().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Team team = documentSnapshot.toObject(Team.class);
                    HashMap<String, String > lunchPlaceByEmployee = (team.getLunchPlaceByEmployee() == null) ? new HashMap<String, String>() : team.getLunchPlaceByEmployee();
                    String valueForTeam = (mEmployeePic == null) ? "Null___" : mEmployeePic+"___";
                    lunchPlaceByEmployee.put(mEmployeeName, valueForTeam);
                    TeamHelper.updateLunchPlaceByEmployee(lunchPlaceByEmployee);
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
//                    System.out.println("Lunch - "+lunchPlace+"-Resto actuel"+mPlace.getName()+"-Resto likés"+likedPlaces);
                    if (lunchPlace != null && lunchPlace.equals(thisRestaurant)) {
                        mBtnLunch.setImageResource(R.drawable.ic_my_choice_on);
                        mLunchOn = true;
                    } else {mLunchOn = false;}
                    if (likedPlaces != null && likedPlaces.containsKey(thisRestaurantId)) {
                        mBtnLike.setTextColor(getResources().getColor(R.color.yellow));
                        mBtnLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_star_1, 0, 0);
                        mLikeOn = true;
                    } else {mLikeOn = false;}
                }
            });
        }
    }

//    private String checkPreviousLunch() {
//        if (this.getCurrentUser() != null){
//
//            EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                    mCurrentEmployee = documentSnapshot.toObject(Employee.class);
//System.out.println("mCurrentEmployee.getLunchPlaceId : "+mCurrentEmployee.getLunchPlaceId()+" - mPlaceId : "+mPlaceId);
//                    if (mCurrentEmployee.getLunchPlace() != null && mCurrentEmployee.getLunchPlace() != "" && mCurrentEmployee.getLunchPlaceId() != mPlaceId)  {
//                        mLunchPlaceToRemove = mCurrentEmployee.getLunchPlaceId();
//                        System.out.println("CheckPreviousLunch : ancien lieu à effacer : "+mLunchPlaceToRemove);
//                        RestaurantHelper.getRestaurant(mLunchPlaceToRemove).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                            @Override
//                            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
//                                HashMap<String, String > lunchAttendees = restaurant.getLunchAttendees();
//                                if (lunchAttendees != null && lunchAttendees.containsKey(mEmployeeName)) {
//                                    lunchAttendees.remove(mEmployeeName);
//                                }
//                                RestaurantHelper.updateLunchAttendees(mPlaceId, lunchAttendees).addOnFailureListener(onFailureListener());
//                            }
//                        });
//                    } else {
//                        mLunchPlaceToRemove = null;
//                        System.out.println("CheckPreviousLunch : même lieu ou rien à effacer");
//                    }
//
//                }
//            });
//        }
//        return mLunchPlaceToRemove;
//    }

}

