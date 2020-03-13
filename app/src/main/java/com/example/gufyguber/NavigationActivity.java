/*
 * NavigationActivity.java
 *
 * Version
 *
 * Last edit: mai-thyle, 04/03/20 11:21 PM
 *
 * Copyright (c) CMPUT301W20T12 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 */

package com.example.gufyguber;

import android.os.Bundle;

import com.example.gufyguber.ui.Map.MapFragment;

import android.view.MenuItem;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;

import static com.example.gufyguber.R.id.nav_host_fragment;

public class NavigationActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map, R.id.nav_profile, R.id.nav_current_requests, R.id.nav_generateQR, R.id.nav_scan, R.id.nav_sign_out)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        TextView displayName = navigationView.getHeaderView(0).findViewById(R.id.display_name);
        TextView displayEmail = navigationView.getHeaderView(0).findViewById(R.id.display_email);
        User user = OfflineCache.getReference().retrieveCurrentUser();
        displayName.setText(user.getFirstName() + " " + user.getLastName());
        displayEmail.setText(user.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //@Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_map){
            MapFragment mapFragment = new MapFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(id, mapFragment).commit();
        }
        if(id == R.id.nav_generateQR){
            GenerateQrFragment qrFragment = new GenerateQrFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(id, qrFragment).commit();
        }
        if(id == R.id.nav_scan){
            ScanQrFragment scanFragment = new ScanQrFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(id, scanFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
