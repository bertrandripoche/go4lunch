package com.openclassrooms.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.model.Attendee;
import com.openclassrooms.go4lunch.model.Employee;

import java.util.HashMap;

public class AttendeeHelper {
    private static final String COLLECTION_NAME = "restaurants";
    private static final String SUB_COLLECTION_NAME = "attendees";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getAttendeeCollection(String placeId){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME).document(placeId).collection(SUB_COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createAttendee(String uid, String name, String urlPicture, String placeId) {
        Attendee attendeeToCreate = new Attendee(name, urlPicture);
        return AttendeeHelper.getAttendeeCollection(placeId).document(uid).set(attendeeToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getEmployee(String uid){
        return EmployeeHelper.getEmployeesCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateLunchPlace(String uid, String lunchPlace) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("lunchPlace", lunchPlace);
    }

    public static Task<Void> updateLunchPlaceId(String uid, String lunchPlaceId) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("lunchPlaceId", lunchPlaceId);
    }

    public static Task<Void> updateLikedPlaces(String uid, String restaurantId, String restaurantName) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("likedPlaces."+restaurantId, restaurantName);
    }

    // --- DELETE ---

    public static Task<Void> deleteLikedPlaces(String uid, String restaurantId) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("likedPlaces."+restaurantId, FieldValue.delete());
    }

}
