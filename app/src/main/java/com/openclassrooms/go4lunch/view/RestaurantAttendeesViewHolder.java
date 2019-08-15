package com.openclassrooms.go4lunch.view;

import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Employee;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantAttendeesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.activity_restaurant_profile_name) TextView textView;
    @BindView(R.id.activity_restaurant_profile_pic) AppCompatImageView picView;

    public RestaurantAttendeesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithEmployeeInfo(Employee employee){
        this.textView.setText(employee.getName());

//        Uri employeePicUri = Uri.parse(employee.getUrlPicture());
//
//        Glide.with(this)
//                .load(employeePicUri)
//                .fitCenter()
//                .circleCrop()
//                .into(picView);
    }
}
