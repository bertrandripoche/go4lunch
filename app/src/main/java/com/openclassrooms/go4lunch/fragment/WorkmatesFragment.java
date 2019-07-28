package com.openclassrooms.go4lunch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.openclassrooms.go4lunch.R;

import butterknife.BindView;
import butterknife.OnClick;

public class WorkmatesFragment extends Fragment {
    @BindView(R.id.logout_button)
    Button logoutButton;

    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;

    public WorkmatesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        return view;
    }

    @OnClick(R.id.logout_button)
    public void onClickSignOutButton() {
        System.out.println("CLIC");
        this.signOutUserFromFirebase(); }

    private void signOutUserFromFirebase(){
        System.out.println("SIGN OUT");
        AuthUI.getInstance()
                .signOut(getActivity())
                .addOnSuccessListener(getActivity(), this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                switch (origin){
                    case SIGN_OUT_TASK:
                        System.out.println("DECONNEXION");
                        getActivity().finish();
                        break;
                    case DELETE_USER_TASK:
                        getActivity().finish();
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
