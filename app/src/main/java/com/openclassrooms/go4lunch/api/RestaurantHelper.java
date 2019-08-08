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

    public static Task<Void> createRestaurant(String id, String name, HashMap<String, String> likes, HashMap<String, String> lunchPlaces) {
        Restaurant restaurantToCreate = new Restaurant(id, name, likes, lunchPlaces);
        return RestaurantHelper.getRestaurantsCollection().document(id).set(restaurantToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurant(String uid){
        return RestaurantHelper.getRestaurantsCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateName(String name, String uid) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).update("name", name);
    }

    public static Task<Void> updateMail(String uid, String mail) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).update("mail", mail);
    }

    public static Task<Void> updateLikes(String uid, List<String> likedPlaces) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).update("likes", likedPlaces);
    }

    public static Task<Void> updateLunchPlace(String uid, String lunchPlace) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).update("lunchPlace", lunchPlace);
    }

    // --- DELETE ---

    public static Task<Void> deleteRestaurant(String uid) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).delete();
    }

    public static Task<Void> deleteLunchPlaces(String uid) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).update("lunchPlace", null);
    }

    public static Task<Void> deleteLikes(String uid) {
        return RestaurantHelper.getRestaurantsCollection().document(uid).update("likes", null);
    }
}
