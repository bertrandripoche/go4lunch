package com.openclassrooms.go4lunch.controller.activity;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {
    @BindView(R.id.main_activity_coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.main_activity_button_login_facebook) Button buttonLoginFacebook;
    @BindView(R.id.main_activity_button_login_google) Button buttonLoginGoogle;
    @BindView(R.id.main_activity_button_login_twitter) Button buttonLoginTwitter;
    @BindView(R.id.main_activity_button_login_mail) Button buttonLoginMail;

    private static final String TAG = "Log Main Activity";
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isCurrentUserLogged()) {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
            startPrincipalActivity();
        } else {
            setContentView(R.layout.activity_main);
            ButterKnife.bind(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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

    @OnClick({R.id.main_activity_button_login_twitter})
    public void onClickTwitterLoginButton() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startPrincipalActivity();
        } else {
            this.startSignInActivity("twitter");
        }
    }

    @OnClick({R.id.main_activity_button_login_mail})
    public void onClickMailLoginButton() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startPrincipalActivity();
        } else {
            this.startSignInActivity("mail");
        }
    }

    private void startPrincipalActivity() {
        Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
        startActivity(intent);
    }

    private void startSignInActivity(String login){
        AuthUI.IdpConfig provider;
        if (login.equals(getResources().getString(R.string.google))) {
            provider = new AuthUI.IdpConfig.GoogleBuilder().build();
        } else if (login.equals(getResources().getString(R.string.facebook))) {
            provider = new AuthUI.IdpConfig.FacebookBuilder().build();
        } else if (login.equals(getResources().getString(R.string.twitter))) {
            provider = new AuthUI.IdpConfig.TwitterBuilder().build();
        } else {
            provider = new AuthUI.IdpConfig.EmailBuilder().build();
        }

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(provider))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.logo_g4l)
                        .build(),
                RC_SIGN_IN);
    }

    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(this.coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                this.createUserInFirestore();
                startPrincipalActivity();
            } else {
                System.out.println("Connexion foirée");
                if (response == null) {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_authentication_canceled));
                    System.out.println("Connexion pour une annulation");
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    System.out.println("Connexion pour une réseau");

                    showSnackBar(this.coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    System.out.println("Connexion pour une raison inconnue");
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_unknown_error));
                } else {
                    showSnackBar(this.coordinatorLayout, getString(R.string.error_undefined_error));
                }
            }
        }
    }

    private void createUserInFirestore(){
        if (this.getCurrentUser() != null){
            String uid = this.getCurrentUser().getUid();
            String name = this.getCurrentUser().getDisplayName();
            String mail = this.getCurrentUser().getEmail();
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            EmployeeHelper.createEmployee(uid, name, mail, urlPicture, null, null,null).addOnFailureListener(this.onFailureListener());
        }
    }
}
