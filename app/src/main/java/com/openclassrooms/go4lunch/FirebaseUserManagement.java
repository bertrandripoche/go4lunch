package com.openclassrooms.go4lunch;

import android.app.Activity;
import android.content.Context;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;

public class FirebaseUserManagement {
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;

    public void signOutUserFromFirebase(Activity activity){
        AuthUI.getInstance()
                .signOut(activity)
                .addOnSuccessListener(activity, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK, activity));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin, Activity activity){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        System.out.println("DECONNEXION");
                        activity.finish();
                        break;
                    case DELETE_USER_TASK:

                        activity.finish();
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
