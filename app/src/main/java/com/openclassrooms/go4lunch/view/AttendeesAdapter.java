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
import com.openclassrooms.go4lunch.model.Attendee;
import com.openclassrooms.go4lunch.utils.FirebaseUserManagement;

public class AttendeesAdapter extends FirestoreRecyclerAdapter<Attendee, AttendeesViewHolder> {

    public AttendeesAdapter(@NonNull FirestoreRecyclerOptions<Attendee> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AttendeesViewHolder attendeesViewHolder, int i, @NonNull Attendee attendee) {
        Resources resources = attendeesViewHolder.itemView.getContext().getResources();
        String currentUserUid = FirebaseUserManagement.getCurrentUser().getUid();

        Uri employeePicUri = (attendee.getUrlPicture() == null) ?
                Uri.parse("android.resource://"+attendeesViewHolder.itemView.getContext().getPackageName()+"/drawable/ic_thats_me"):
                Uri.parse(attendee.getUrlPicture());
        Glide.with(attendeesViewHolder.itemView.getContext())
                .load(employeePicUri)
                .fitCenter()
                .circleCrop()
                .into(attendeesViewHolder.mEmployeePic);

        String firstName = attendee.getName().split(" ")[0] + " ";
        if (attendee.getUid().equals(currentUserUid)) {
            attendeesViewHolder.mEmployeeDescription.setText(resources.getString(R.string.you_eat_here));
            attendeesViewHolder.mEmployeeDescription.setTextColor(resources.getColor(R.color.orange));
            attendeesViewHolder.mEmployeeDescription.setTypeface(null, Typeface.BOLD);
        } else {
            attendeesViewHolder.mEmployeeDescription.setText(firstName + resources.getString(R.string.is_eating_here));
            attendeesViewHolder.mEmployeeDescription.setTextColor(resources.getColor(R.color.black));
            attendeesViewHolder.mEmployeeDescription.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public AttendeesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_employee_item,parent,false);
        return new AttendeesViewHolder(v);
    }
}
