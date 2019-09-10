package com.openclassrooms.go4lunch.controller.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.controller.activity.PrincipalActivity;
import com.openclassrooms.go4lunch.controller.activity.RestaurantActivity;
import com.openclassrooms.go4lunch.model.Employee;
import com.openclassrooms.go4lunch.utils.ItemClickSupport;
import com.openclassrooms.go4lunch.view.WorkmatesAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesFragment extends Fragment {
    @BindView(R.id.fragment_workmates_recycler_view) RecyclerView mRecyclerView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference mWorkmatesRef = db.collection("employees");
    private WorkmatesAdapter mAdapter;
    private TextView mEmployeeDescription;
    private PrincipalActivity mPrincipalActivity;

    public WorkmatesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPrincipalActivity = (PrincipalActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this, view);
        configureMenu(false);
        configureRecyclerView();
        configureOnClickRecyclerView();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        configureMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        configureMenu(false);
    }

    /**
     * This method allows to hide the Search icon which is not required on this fragment
     * @param makeVisible
     */
    private void configureMenu(boolean makeVisible) {
        mPrincipalActivity.mSearchIcon.setVisible(makeVisible);
    }

    /**
     * This methods configures the RecyclerView
     */
    private void configureRecyclerView() {
        Query query = mWorkmatesRef.orderBy("name", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Employee> options = new FirestoreRecyclerOptions.Builder<Employee>()
                .setQuery(query, Employee.class)
                .build();

        mAdapter = new WorkmatesAdapter(options);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * This method defines the action to do when clicking on an element from the list of employees
     */
    void configureOnClickRecyclerView(){
        ItemClickSupport.addTo(mRecyclerView, R.layout.recycler_view_employee_item)
            .setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                    final String PLACE_ID = "placeId";
                    mEmployeeDescription = v.findViewById(R.id.item_employee_description);
                    String lunchPlaceId = (String) mEmployeeDescription.getTag();

                    if (lunchPlaceId != null && lunchPlaceId != "null") {
                        Bundle bundle = new Bundle();
                        bundle.putString(PLACE_ID, lunchPlaceId);

                        Intent intent = new Intent(v.getContext(), RestaurantActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    } else {
                        Toast.makeText(v.getContext(),R.string.no_lunch_defined,Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    /**
     * This method indicates when the adapter needs to start listening
     */
    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    /**
     * This method indicates when the adapter needs to stop listening
     */
    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}
