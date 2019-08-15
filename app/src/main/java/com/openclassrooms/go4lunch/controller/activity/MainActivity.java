package com.openclassrooms.go4lunch.controller.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.health.SystemHealthManager;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;
import com.openclassrooms.go4lunch.api.TeamHelper;
import com.openclassrooms.go4lunch.model.Team;

import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.main_activity_coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.main_activity_button_login_facebook) Button buttonLoginFacebook;
    @BindView(R.id.main_activity_button_login_google) Button buttonLoginGoogle;

    private static final String TAG = "Log Main Activity";
    private static final int RC_SIGN_IN = 123;
    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isCurrentUserLogged()) {
            startPrincipalActivity();
        } else {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    @OnClick({R.id.main_activity_button_login_google})
    public void onClickGoogleLoginButton() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startPrincipalActivity();
        } else {
            this.startSignInActivity("google");
        }
    }

    @OnClick({R.id.main_activity_button_login_facebook})
    public void onClickFacebookLoginButton() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startPrincipalActivity();
        } else {
            this.startSignInActivity("facebook");
        }
    }

    private void startPrincipalActivity() {
        Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
        startActivity(intent);
    }

    private void startSignInActivity(String login){
        AuthUI.IdpConfig provider;
        if (login.equals("google")) {
            provider = new AuthUI.IdpConfig.GoogleBuilder().build();
        } else {
            provider = new AuthUI.IdpConfig.FacebookBuilder().build();
        }

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                                Arrays.asList(provider))
                        .setIsSmartLockEnabled(false, true)
                        .build(),
                RC_SIGN_IN);
    }

    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                this.createUserInFirestore();
                startPrincipalActivity();
            } else {
                if (response == null) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_unknown_error));
                } else {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_undefined_error));
                }
            }
        }
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        finish();
                        break;
                    case DELETE_USER_TASK:
                        finish();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){

            String uid = this.getCurrentUser().getUid();
            String name = this.getCurrentUser().getDisplayName();
            String mail = this.getCurrentUser().getEmail();
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            EmployeeHelper.createEmployee(uid, name, mail, urlPicture, null, null,null).addOnFailureListener(this.onFailureListener());

//            TeamHelper.getTeam().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot) {
//                    Team team = documentSnapshot.toObject(Team.class);
//                    String valueForTeam = (urlPicture == null) ? name + "___Null___" : name + "___" + urlPicture + "___";
//
//                    if (team == null) {
//                        HashMap<String, String > lunchPlaceByEmployee = new HashMap<String, String>();
//                        System.out.println("Trouvé mais vide");
//                        lunchPlaceByEmployee.put(uid, valueForTeam);
//                        TeamHelper.createTeam(lunchPlaceByEmployee).addOnFailureListener(onFailureListener());
//                    } else {
//                        HashMap<String, String > lunchPlaceByEmployee = team.getLunchPlaceByEmployee();
//                        System.out.println("Trouvé mais pleine");
//                        lunchPlaceByEmployee.put(uid, valueForTeam);
//                        TeamHelper.updateLunchPlaceByEmployee(lunchPlaceByEmployee);
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    System.out.println("Rien trouvé");
//                }
//            });
        }
    }
}
