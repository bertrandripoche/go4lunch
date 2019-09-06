package com.openclassrooms.go4lunch.controller.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
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

    private Toolbar mToolbar;
    public RelativeLayout mSearchBar;
    public EditText mMySearch;
    public MenuItem mSearchIcon;
    private DrawerLayout mDrawerLayout;
    private FirebaseUserManagement mFirebaseUserManagement;
    public static FirebaseUser mCurrentUser;
    private NavigationView navigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        mFirebaseUserManagement = new FirebaseUserManagement();
        mSearchBar = findViewById(R.id.search_bar);
        mMySearch = findViewById(R.id.my_search);

        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        loadFragment(new MapFragment());
        configureBottomNavigationMenu();
    }

    /**
     * This method configure the bottom navigation bar
     */
    void configureBottomNavigationMenu() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navbar);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.nav_map:
                        mToolbar.setTitle(R.string.toolbar_title_map);
                        closeSearch();
                        fragment = new MapFragment();
                        loadFragment(fragment);
                        break;
                    case R.id.nav_list:
                        mToolbar.setTitle(R.string.toolbar_title_list);
                        closeSearch();
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

    /**
     * This method manages the action when we click on the "Search" magnifying glass icon
     * @param item we click on
     * @return a boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.menu_search) {
            openSearch();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method manages the action when an item from the navigation view menu is clicked
     * @param item is the item clicked
     * @return true
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            // In case we click "Your lunch"
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

            // In case we click "Settings"
            case R.id.activity_main_drawer_settings:
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;

            // In case we click "Logout"
            case R.id.activity_main_drawer_logout:
                mFirebaseUserManagement.signOutUserFromFirebase(this);
                return true;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * This method override the onBackPressed method to change the behavior of the Back button
     */
    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (this.mSearchBar.getVisibility() == View.VISIBLE) {
            closeSearch();
        }
    }

    /**
     * This method create the single icon Search menu on the toolbar
     * @param menu is the menu to create
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        mSearchIcon = menu.getItem(0);
        return true;
    }

    /**
     * This methods loads the specified fragment
     * @param fragment is the fragment to load
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * This method configures the toolbar
     */
    private void configureToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    /**
     * This method configures the drawer layout needed for the drawer menu
     */
    private void configureDrawerLayout(){
        mDrawerLayout = findViewById(R.id.activity_main_drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * This method configure the lateral navigation menu
     */
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

    /**
     * This method makes the Search field visible
     */
    private void openSearch() {
        mSearchBar.setVisibility(View.VISIBLE);
        mToolbar.setVisibility(View.INVISIBLE);
    }

    /**
     * This method close the Search field
     */
    private void closeSearch() {
        mMySearch.setText(null);
        mSearchBar.setVisibility(View.INVISIBLE);
        mToolbar.setVisibility(View.VISIBLE);
    }

}
