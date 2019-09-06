package com.openclassrooms.go4lunch.controller.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;
import com.openclassrooms.go4lunch.model.Employee;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    SwitchCompat notifSwitch;
    String mEmployeeUid;
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getCurrentUser() != null) mEmployeeUid = this.getCurrentUser().getUid();

        notifSwitch = findViewById(R.id.activity_notif_switch);

        checkCurrentNotifSetting();
        notifSwitch.setOnClickListener(this);
    }

    /**
     * This method checks the current setting of the current user for notifications
     */
    private void checkCurrentNotifSetting() {
        if (mEmployeeUid != null){
            EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Employee mCurrentEmployee = documentSnapshot.toObject(Employee.class);
                    assert mCurrentEmployee != null;
                    if (mCurrentEmployee.getNotif()) {
                        setEnabledSwitchStatus();
                    } else {
                        setDisabledSwitchStatus();
                    }
                }
            });
        }
    }

    /**
     * This method allows to manage the notification switch on the screen
     * @param v is the view on which it applies
     */
    @Override
    public void onClick(View v) {
        if (notifSwitch.isChecked()) {
            notifSwitch.setText(R.string.disable_notifications);
            mDb.collection("employees").document(mEmployeeUid).update("notif", true);
        } else {
            notifSwitch.setText(R.string.enable_notifications);
            mDb.collection("employees").document(mEmployeeUid).update("notif", false);
        }
    }

    /**
     * This method set the switch to enabled
     */
    private void setEnabledSwitchStatus() {
        notifSwitch.setText(R.string.disable_notifications);
        notifSwitch.setChecked(true);
    }

    /**
     * This method set the switch to disabled
     */
    private void setDisabledSwitchStatus() {
        notifSwitch.setText(R.string.enable_notifications);
        notifSwitch.setChecked(false);
    }

}
