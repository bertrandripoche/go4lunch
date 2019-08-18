package com.openclassrooms.go4lunch.view;

import android.annotation.SuppressLint;
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

public class WorkmatesAdapter extends FirestoreRecyclerAdapter<Employee, WorkmatesViewHolder> {

    public WorkmatesAdapter(@NonNull FirestoreRecyclerOptions<Employee> options) {
        super(options);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onBindViewHolder(@NonNull WorkmatesViewHolder workmatesViewHolder, int i, @NonNull Employee employee) {
        Resources resources = workmatesViewHolder.itemView.getContext().getResources();
        Uri employeePicUri = Uri.parse(employee.getUrlPicture());
        Glide.with(workmatesViewHolder.itemView.getContext())
                .load(employeePicUri)
                .fitCenter()
                .circleCrop()
                .into(workmatesViewHolder.mEmployeePic);

        String firstName = employee.getName().split(" ")[0] + " ";
        String employeeDescription;
        if (employee != null) {
            if (employee.getLunchPlace() != null && !employee.getLunchPlace().equals("")) {
                employeeDescription = firstName + resources.getString(R.string.is_eating_at) + " \"" + employee.getLunchPlace() +"\"";
                workmatesViewHolder.mEmployeeDescription.setText(employeeDescription);
            } else {
                employeeDescription = firstName + resources.getString(R.string.not_decided_yet);
                workmatesViewHolder.mEmployeeDescription.setText(employeeDescription);
                workmatesViewHolder.mEmployeeDescription.setTextColor(resources.getColor(R.color.quantum_grey500));
                workmatesViewHolder.mEmployeeDescription.setTypeface(null,Typeface.ITALIC);
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
