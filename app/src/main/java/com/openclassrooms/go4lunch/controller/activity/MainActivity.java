package com.openclassrooms.go4lunch.controller.activity;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.openclassrooms.go4lunch.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkLoginAndDisplayAppropriateScreen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    @OnClick({R.id.main_activity_button_login_google})
    public void onClickGoogleLoginButton() {
        this.startSignInActivity("google");
    }

    @OnClick({R.id.main_activity_button_login_facebook})
    public void onClickFacebookLoginButton() {
        this.startSignInActivity("facebook");
    }

    @OnClick({R.id.main_activity_button_login_twitter})
    public void onClickTwitterLoginButton() {
        this.startSignInActivity("twitter");
    }

    @OnClick({R.id.main_activity_button_login_mail})
    public void onClickMailLoginButton() {
        this.startSignInActivity("mail");
    }

    /**
     * This method check if there is a user logged and in that case, start the PrincipalActivity
     */
    private void checkLoginAndDisplayAppropriateScreen() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (isCurrentUserLogged()) startPrincipalActivity();
    }

    /**
     * This method launches the PrincipalActivity
     */
    private void startPrincipalActivity() {
        Intent intent = new Intent(MainActivity.this, PrincipalActivity.class);
        startActivity(intent);
    }

    /**
     * This method trigger the authentication process
     * @param login is the provider the user wants to use
     */
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

    /**
     * Method allowing to display a snackbar message
     * @param coordinatorLayout is the element where the snackbar should be displayed
     * @param message is the message we want to display
     */
    private void showSnackBar(CoordinatorLayout coordinatorLayout, String message){
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * This method handle the response from Firebase to authentication attempt
     * @param requestCode is the code of the request launched
     * @param resultCode is the result code of the request
     * @param data is the intent representing the authentication attempt
     */
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {

            if (resultCode == RESULT_OK) {
                this.createUserInFirestore();
                startPrincipalActivity();
            } else {
                if (response == null) {
                    showSnackBar(coordinatorLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(coordinatorLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(coordinatorLayout, getString(R.string.error_unknown_error));
                } else {
                    showSnackBar(coordinatorLayout, getString(R.string.error_undefined_error));
                }
            }
        }
    }

    /**
     * This method create a user entry in the Firebase database "employees" collection, only if needed (thanks to merge option)
     */
    private void createUserInFirestore(){

        if (this.getCurrentUser() != null){
            String uid = this.getCurrentUser().getUid();
            String name = this.getCurrentUser().getDisplayName();
            String mail = this.getCurrentUser().getEmail();
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;

            Map<String, Object> docData = new HashMap<>();
            docData.put("name", name);
            docData.put("uid", uid);
            docData.put("urlPicture", urlPicture);
            docData.put("mail", mail);
            docData.put("notif", true);

            mDb.collection("employees").document(uid).set(docData, SetOptions.merge());
        }
    }
}
