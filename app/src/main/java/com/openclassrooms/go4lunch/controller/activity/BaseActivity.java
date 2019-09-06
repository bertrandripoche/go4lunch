package com.openclassrooms.go4lunch.controller.activity;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.openclassrooms.go4lunch.R;


public class BaseActivity extends AppCompatActivity {

    /**
     * This method returns the current logged user
     * @return a FirebaseUser object representing the logged user
     */
    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    /**
     * This method allows to know if a user is logged
     * @return true if there is a logged user
     */
    protected Boolean isCurrentUserLogged(){ return (this.getCurrentUser() != null); }

}
