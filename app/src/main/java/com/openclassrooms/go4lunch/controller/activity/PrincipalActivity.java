package com.openclassrooms.go4lunch.controller.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.api.EmployeeHelper;
import com.openclassrooms.go4lunch.controller.fragment.ListFragment;
import com.openclassrooms.go4lunch.controller.fragment.MapFragment;
import com.openclassrooms.go4lunch.controller.fragment.WorkmatesFragment;
import com.openclassrooms.go4lunch.model.Employee;
import com.openclassrooms.go4lunch.utils.FirebaseUserManagement;

public class PrincipalActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppCompatImageView mProfilePic;
    private TextView mProfileName;
    private TextView mProfileMail;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private FirebaseUserManagement mFirebaseUserManagement;
    private FirebaseUser mCurrentUser;
    private NavigationView navigationView;

    private boolean mIsLunchSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        this.configureToolbar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        mFirebaseUserManagement = new FirebaseUserManagement();

        loadFragment(new MapFragment());

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navbar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.nav_map:
                        mToolbar.setTitle(R.string.toolbar_title_map);
                        fragment = new MapFragment();
                        loadFragment(fragment);
                        break;
                    case R.id.nav_list:
                        mToolbar.setTitle(R.string.toolbar_title_list);
                        fragment = new ListFragment();
                        loadFragment(fragment);
                        break;
                    case R.id.nav_workmates:
                        mToolbar.setTitle(R.string.toolbar_title_workmates);
                        fragment = new WorkmatesFragment();
                        loadFragment(fragment);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.activity_main_drawer_lunch:
                final String PLACE_ID = "placeId";

                if (this.getCurrentUser() != null){
                    String mEmployeeUid = this.getCurrentUser().getUid();

                    EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Employee mCurrentEmployee = documentSnapshot.toObject(Employee.class);
                            String lunchPlaceId = mCurrentEmployee.getLunchPlaceId();

                            if (lunchPlaceId != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString(PLACE_ID, lunchPlaceId);

                                Intent intent = new Intent(getApplicationContext(), RestaurantActivity.class);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(),R.string.no_lunch_defined,Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }

                return true;
            case R.id.activity_main_drawer_settings:
                System.out.println("MENU SETTINGS");
                return true;
            case R.id.activity_main_drawer_logout:
                mFirebaseUserManagement.signOutUserFromFirebase(this);
                return true;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void configureToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    private void configureDrawerLayout(){
        mDrawerLayout = findViewById(R.id.activity_main_drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView(){
        navigationView = findViewById(R.id.activity_main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCurrentUser = FirebaseUserManagement.getCurrentUser();
        View header = navigationView.getHeaderView(0);

        TextView userName = (TextView) header.findViewById(R.id.activity_main_drawer_profile_name);
        userName.setText(mCurrentUser.getDisplayName());
        TextView userMail = (TextView) header.findViewById(R.id.activity_main_drawer_profile_mail);
        userMail.setText(mCurrentUser.getEmail());
        AppCompatImageView userPic = (AppCompatImageView) header.findViewById(R.id.activity_main_drawer_profile_pic);
        Uri userPhotoUri = mCurrentUser.getPhotoUrl();

        Glide.with(this)
                .load(userPhotoUri)
                .fitCenter()
                .circleCrop()
                .into(userPic);
    }

    public boolean isLunchPlaceSet() {
        if (this.getCurrentUser() != null){
            String mEmployeeUid = this.getCurrentUser().getUid();
            EmployeeHelper.getEmployee(mEmployeeUid).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String lunchPlaceId = documentSnapshot.toObject(Employee.class).getLunchPlaceId();
                    mIsLunchSet = lunchPlaceId != null;
//                    System.out.println("LunchPlace : "+lunchPlaceId+"IsLunchSet"+mIsLunchSet);
                }
            });
        }
        return mIsLunchSet;
    }

}
