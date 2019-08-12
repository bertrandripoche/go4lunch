package com.openclassrooms.go4lunch.api;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.model.Employee;

import java.util.HashMap;
import java.util.List;

public class EmployeeHelper {
    private static final String COLLECTION_NAME = "employees";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getEmployeesCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createEmployee(String uid, String name, String mail, String urlPicture, String lunchPlace, String lunchPlaceId, HashMap<String, String> likedPlaces) {
        Employee employeeToCreate = new Employee(uid, name, mail, urlPicture, lunchPlace, lunchPlaceId, likedPlaces);
        return EmployeeHelper.getEmployeesCollection().document(uid).set(employeeToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getEmployee(String uid){
        return EmployeeHelper.getEmployeesCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateName(String name, String uid) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("name", name);
    }

    public static Task<Void> updateMail(String uid, String mail) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("mail", mail);
    }

    public static Task<Void> updateLunchPlace(String uid, String lunchPlace) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("lunchPlace", lunchPlace);
    }

    public static Task<Void> updateLunchPlaceId(String uid, String lunchPlaceId) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("lunchPlaceId", lunchPlaceId);
    }

    public static Task<Void> updateLikedPlaces(String uid, HashMap<String, String> likedPlaces) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("likedPlaces", likedPlaces);
    }

    // --- DELETE ---

    public static Task<Void> deleteEmployee(String uid) {
        return EmployeeHelper.getEmployeesCollection().document(uid).delete();
    }

    public static Task<Void> deleteLunchPlaces(String uid) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("lunchPlace", FieldValue.delete());
    }

    public static Task<Void> deleteLikedPlaces(String uid) {
        return EmployeeHelper.getEmployeesCollection().document(uid).update("likedPlaces", FieldValue.delete());
    }
}
