package com.openclassrooms.go4lunch.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.openclassrooms.go4lunch.R;

public class AttendeesViewHolder extends RecyclerView.ViewHolder {
    AppCompatImageView mEmployeePic;
    TextView mEmployeeDescription;

    /**
     * This method describes a line of our RecyclerView
     * @param itemView represents one line of the list
     */
    public AttendeesViewHolder(@NonNull View itemView) {
        super(itemView);

        mEmployeePic = itemView.findViewById(R.id.item_employee_pic);
        mEmployeeDescription = itemView.findViewById(R.id.item_employee_description);
    }
}
