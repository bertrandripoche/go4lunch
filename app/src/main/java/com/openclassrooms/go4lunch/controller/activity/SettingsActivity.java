package com.openclassrooms.go4lunch.controller.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;
import com.openclassrooms.go4lunch.model.Employee;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    SwitchCompat notifSwitch;
    String mEmployeeUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getCurrentUser() != null) mEmployeeUid = this.getCurrentUser().getUid();

        notifSwitch = findViewById(R.id.activity_notif_switch);

        checkCurrentNotifSetting();
        notifSwitch.setOnClickListener(this);

    }

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

    @Override
    public void onClick(View v) {
        if (notifSwitch.isChecked()) {
            notifSwitch.setText(R.string.disable_notifications);
            EmployeeHelper.updateNotif(mEmployeeUid, true);
        } else {
            notifSwitch.setText(R.string.enable_notifications);
            EmployeeHelper.updateNotif(mEmployeeUid, false);
        }
    }

    private void setEnabledSwitchStatus() {
        notifSwitch.setText(R.string.disable_notifications);
        notifSwitch.setChecked(true);
    }

    private void setDisabledSwitchStatus() {
        notifSwitch.setText(R.string.enable_notifications);
        notifSwitch.setChecked(false);
    }

}
