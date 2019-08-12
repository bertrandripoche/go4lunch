package com.openclassrooms.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.model.Team;

import java.util.HashMap;

public class TeamHelper {

    private static final String COLLECTION_NAME = "team";
    private static final String DOCUMENT_NAME = "team";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getTeamCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createTeam(HashMap<String, String> lunchPlaceByEmployee) {
        Team teamToCreate = new Team(lunchPlaceByEmployee);
        return TeamHelper.getTeamCollection().document(DOCUMENT_NAME).set(teamToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getTeam(){
        return TeamHelper.getTeamCollection().document(DOCUMENT_NAME).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateLunchPlaceByEmployee(HashMap<String, String> lunchPlaceByEmployee) {
        return TeamHelper.getTeamCollection().document(DOCUMENT_NAME).update("lunchPlaceByEmployee", lunchPlaceByEmployee);
    }

    // --- DELETE ---

    public static Task<Void> deleteLunchPlaceByEmployee() {
        return TeamHelper.getTeamCollection().document(DOCUMENT_NAME).update("lunchPlaceByEmployee", FieldValue.delete());
    }

}
