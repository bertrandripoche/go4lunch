package com.openclassrooms.go4lunch.view;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Employee;
import com.openclassrooms.go4lunch.utils.FirebaseUserManagement;

public class WorkmatesAdapter extends FirestoreRecyclerAdapter<Employee, WorkmatesViewHolder> {

    public WorkmatesAdapter(@NonNull FirestoreRecyclerOptions<Employee> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmatesViewHolder workmatesViewHolder, int i, @NonNull Employee employee) {
        Resources resources = workmatesViewHolder.itemView.getContext().getResources();

        Uri employeePicUri = (employee.getUrlPicture() == null) ?
                Uri.parse("android.resource://"+workmatesViewHolder.itemView.getContext().getPackageName()+"/drawable/ic_thats_me"):
                Uri.parse(employee.getUrlPicture());
        Glide.with(workmatesViewHolder.itemView.getContext())
                .load(employeePicUri)
                .fitCenter()
                .circleCrop()
                .into(workmatesViewHolder.mEmployeePic);

        String firstName = employee.getName().split(" ")[0] + " ";

        String currentUser = FirebaseUserManagement.getCurrentUser().getUid();
        boolean isCurrentUser = currentUser.equals(employee.getUid());

        String employeeDescription;

        if (employee.getLunchPlace() != null && !employee.getLunchPlace().equals("")) {
            employeeDescription = (isCurrentUser) ? resources.getString(R.string.you_eat_at) + " \"" + employee.getLunchPlace() + "\"" : firstName + resources.getString(R.string.eats_at) + " \"" + employee.getLunchPlace() + "\"";
            workmatesViewHolder.mEmployeeDescription.setText(employeeDescription);
            workmatesViewHolder.mEmployeeDescription.setTag(employee.getLunchPlaceId());
            if ((isCurrentUser)) {
                workmatesViewHolder.mEmployeeDescription.setTextColor(resources.getColor(R.color.colorPrimaryDark));
                workmatesViewHolder.mEmployeeDescription.setTypeface(null, Typeface.BOLD);
            }
        } else {
            employeeDescription = (isCurrentUser) ? resources.getString(R.string.you_did_not_decide_yet) : firstName + resources.getString(R.string.not_decided_yet);
            workmatesViewHolder.mEmployeeDescription.setText(employeeDescription);

            workmatesViewHolder.mEmployeeDescription.setTag("null");
            if ((isCurrentUser)) {
                workmatesViewHolder.mEmployeeDescription.setTextColor(resources.getColor(R.color.colorPrimaryDark));
                workmatesViewHolder.mEmployeeDescription.setTypeface(null, Typeface.BOLD);
            } else {
                workmatesViewHolder.mEmployeeDescription.setTextColor(resources.getColor(R.color.quantum_grey500));
                workmatesViewHolder.mEmployeeDescription.setTypeface(null, Typeface.ITALIC);
            }
        }
    }

    @NonNull
    @Override
    public WorkmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_employee_item, parent, false);
        return new WorkmatesViewHolder(v);
    }

}
