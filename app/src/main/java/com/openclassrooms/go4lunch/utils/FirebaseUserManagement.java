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
    private static final int DELETE_USER_TASK = 20;

    private static FirebaseAuth mAuth;

    public FirebaseUserManagement() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
    }

    public void signOutUserFromFirebase(Activity activity){
        AuthUI.getInstance()
                .signOut(activity)
                .addOnSuccessListener(activity, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK, activity));
    }

    @Nullable
    public static FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser(); }

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
