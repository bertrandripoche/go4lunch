package com.openclassrooms.go4lunch.api;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.openclassrooms.go4lunch.model.Restaurant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class RestaurantHelper {
    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createRestaurant(String id, String name) {
        Restaurant restaurantToCreate = new Restaurant(id, name);
//        Map<String, Object> restaurantToCreate = new HashMap<>();
//        restaurantToCreate.put("id", id);
//        restaurantToCreate.put("name", name);
        return RestaurantHelper.getRestaurantsCollection().document(id).set(restaurantToCreate, SetOptions.merge());
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurant(String id){
        return RestaurantHelper.getRestaurantsCollection().document(id).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateLikes(String id, String employeeId, HashMap<String, String> employeeInfo) {
        return RestaurantHelper.getRestaurantsCollection().document(id).update("likes."+employeeId, employeeInfo);
    }

//    public static Task<Void> updateLunchAttendees(String id, String employeeId, HashMap<String, String> employeeInfo) {
//        return RestaurantHelper.getRestaurantsCollection().document(id).update("lunchAttendees."+employeeId, employeeInfo);
//    }

    // --- DELETE ---

//    public static Task<Void> deleteLunchPlaces(String id, String employeeId) {
//        return RestaurantHelper.getRestaurantsCollection().document(id).update("lunchAttendees."+employeeId, FieldValue.delete());
//    }

    public static Task<Void> deleteLikes(String id, String employeeId) {
        return RestaurantHelper.getRestaurantsCollection().document(id).update("likes."+employeeId, FieldValue.delete());
    }

//    public static boolean isCurrentlyALunchPlace(String id) {
//        DocumentReference restaurantDocRef = getRestaurantsCollection().document(id);
//        mIsCurrentlyALunchPlace = false;
//
//        restaurantDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//                        Restaurant restaurant = document.toObject(Restaurant.class);
//                        HashMap<String, HashMap<String, String>> lunchAttendees = restaurant.getLunchAttendees();
//                        System.out.println("Lunch Attendees : " + lunchAttendees);
//                        if (lunchAttendees != null && !lunchAttendees.isEmpty()) {
//                            mIsCurrentlyALunchPlace = true;
//                            System.out.println("Lunch Attendees now : " + lunchAttendees);
//                        }
//                    }
//                }
//            }
//        });
//        return mIsCurrentlyALunchPlace;
//    }

//    public static void checkLunchPlaces() {
//        getRestaurantsCollection().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                List<String> mLunchPlaces = new ArrayList<>();
//                if (task.isSuccessful()) {
//                    List<DocumentSnapshot> myListOfDocuments = new ArrayList<DocumentSnapshot>();
//                    myListOfDocuments = task.getResult().getDocuments();
//
//                    if (!myListOfDocuments.isEmpty()) {
//                        ListIterator<DocumentSnapshot> it = myListOfDocuments.listIterator();
//                        while(it.hasNext()){
//                            DocumentSnapshot restaurantDoc = it.next();
//                            Restaurant restaurant = restaurantDoc.toObject(Restaurant.class);
//                            HashMap<String, HashMap<String, String>> lunchAttendees = restaurant.getLunchAttendees();
//                            if (lunchAttendees != null && !lunchAttendees.isEmpty()) {
//                                mLunchPlaces.add(restaurant.getId());
//                                }
//                            }
//                        }
//                    }
//                }
//            });
//    }
}
