package com.openclassrooms.go4lunch.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.model.Employee;

import java.util.List;

public class RestaurantAttendeesAdapter extends RecyclerView.Adapter<RestaurantAttendeesViewHolder> {

    private List<Employee> mEmployeeList;

    public RestaurantAttendeesAdapter(List<Employee> mEmployeeList) {
        this.mEmployeeList = mEmployeeList;
    }

    @NonNull
    @Override
    public RestaurantAttendeesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_restaurant_item, parent, false);

        return new RestaurantAttendeesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantAttendeesViewHolder holder, int position) {
        holder.updateWithEmployeeInfo(this.mEmployeeList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.mEmployeeList.size();
    }
}
