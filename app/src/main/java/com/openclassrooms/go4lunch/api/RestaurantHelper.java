package com.openclassrooms.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.model.Restaurant;

import java.util.HashMap;
import java.util.List;

public class RestaurantHelper {
    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createRestaurant(String id, String name, HashMap<String, String> likes, HashMap<String, String> lunchAttendees) {
        Restaurant restaurantToCreate = new Restaurant(id, name, likes, lunchAttendees);
        return RestaurantHelper.getRestaurantsCollection().document(id).set(restaurantToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurant(String id){
        return RestaurantHelper.getRestaurantsCollection().document(id).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateName(String id, String name) {
        return RestaurantHelper.getRestaurantsCollection().document(id).update("name", name);
    }

    public static Task<Void> updateLikes(String id, HashMap<String, String> likes) {
        return RestaurantHelper.getRestaurantsCollection().document(id).update("likes", likes);
    }

    public static Task<Void> updateLunchAttendees(String id, HashMap<String, String> lunchAttendees) {
        return RestaurantHelper.getRestaurantsCollection().document(id).update("lunchAttendees", lunchAttendees);
    }

    // --- DELETE ---

    public static Task<Void> deleteRestaurant(String id) {
        return RestaurantHelper.getRestaurantsCollection().document(id).delete();
    }

    public static Task<Void> deleteLunchPlaces(String id) {
        return RestaurantHelper.getRestaurantsCollection().document(id).update("lunchAttendees", null);
    }

    public static Task<Void> deleteLikes(String uid) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).update("likes", null);
    }
}
