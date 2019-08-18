package com.openclassrooms.go4lunch.view;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controller.activity.RestaurantActivity;
import com.openclassrooms.go4lunch.model.Employee;

public class RestaurantAttendeesAdapter extends FirestoreRecyclerAdapter<Employee, RestaurantAttendeesViewHolder> {

    public RestaurantAttendeesAdapter(@NonNull FirestoreRecyclerOptions<Employee> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RestaurantAttendeesViewHolder restaurantAttendeesViewHolder, int i, @NonNull Employee employee) {
        Uri employeePicUri = Uri.parse(employee.getUrlPicture());
//        Glide.with()
//                .load(employeePicUri)
//                .fitCenter()
//                .circleCrop()
//                .into(restaurantAttendeesViewHolder.mEmployeePic);

        String firstName = employee.getName().split(" ")[0];
        String employeeDescription = firstName + R.string.is_eating_here;
        restaurantAttendeesViewHolder.mEmployeeDescription.setText(employeeDescription);
    }

    @NonNull
    @Override
    public RestaurantAttendeesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_employee_item,parent,false);
        return new RestaurantAttendeesViewHolder(v);
    }
}
