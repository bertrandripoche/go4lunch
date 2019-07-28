package com.openclassrooms.go4lunch;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.openclassrooms.go4lunch.fragment.ListFragment;
import com.openclassrooms.go4lunch.fragment.MapFragment;
import com.openclassrooms.go4lunch.fragment.WorkmatesFragment;

import butterknife.ButterKnife;

public class PrincipalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private FirebaseUserManagement mFirebaseUserManagement;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        ButterKnife.bind(this);
        this.configureToolbar();
        this.configureDrawerLayout();
        this.configureNavigationView();

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
                System.out.println("MENU LUNCH");
                return true;
            case R.id.activity_main_drawer_settings:
                System.out.println("MENU SETTINGS");
                return true;
            case R.id.activity_main_drawer_logout:
                System.out.println("MENU LOGOUT");
                mFirebaseUserManagement = new FirebaseUserManagement();
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
        this.mDrawerLayout = findViewById(R.id.activity_main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView(){
        NavigationView navigationView = findViewById(R.id.activity_main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
}
