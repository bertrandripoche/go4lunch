package com.openclassrooms.go4lunch.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.openclassrooms.go4lunch.R;

public class RestaurantAttendeesViewHolder extends RecyclerView.ViewHolder {

    AppCompatImageView mEmployeePic;
    TextView mEmployeeDescription;

    public RestaurantAttendeesViewHolder(@NonNull View itemView) {
        super(itemView);
        mEmployeePic = itemView.findViewById(R.id.employee_pic);
        mEmployeeDescription = itemView.findViewById(R.id.employee_description);


    }
}
