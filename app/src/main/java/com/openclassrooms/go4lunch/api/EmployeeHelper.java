package com.openclassrooms.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.model.Employee;

import java.util.HashMap;

public class EmployeeHelper {
    private static final String COLLECTION_NAME = "employees";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getEmployeesCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createEmployee(String uid, String name, String mail, String urlPicture) {
        Employee employeeToCreate = new Employee(uid, name, mail, urlPicture);
        return EmployeeHelper.getEmployeesCollection().document(uid).set(employeeToCreate);
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

    public static Task<Void> updateNotif(String uid, boolean notif) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("notif", notif);
    }

    // --- DELETE ---

    public static Task<Void> deleteLikedPlaces(String uid, String restaurantId) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("likedPlaces."+restaurantId, FieldValue.delete());
    }
}
