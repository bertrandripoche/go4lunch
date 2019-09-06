package com.openclassrooms.go4lunch.utils;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseUserManagement {
    private static final int SIGN_OUT_TASK = 10;

    private static FirebaseAuth mAuth;

    public FirebaseUserManagement() {
         mAuth = FirebaseAuth.getInstance();
    }

    /**
     * This method logout the user from Firebase
     * @param activity is the Activity
     */
    public void signOutUserFromFirebase(Activity activity){
        AuthUI.getInstance()
                .signOut(activity)
                .addOnSuccessListener(activity, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK, activity));
    }

    /**
     * This method returns the current FirebaseUser
     * @return a FirebaseUser
     */
    @Nullable
    public static FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser(); }

    /**
     * This method allow us to take action once we signed out from Firestore
     * @param origin is the code for which we want to take action
     * @param activity is the current Activity
     * @return an OnSuccessListener object
     */
    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin, Activity activity){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        activity.finish();
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
