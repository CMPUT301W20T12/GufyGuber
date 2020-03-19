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

import android.content.Intent;
import android.os.Bundle;

import com.example.gufyguber.ui.Map.MapFragment;

import android.util.Log;
import android.view.Gravity;
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.gufyguber.R.id.nav_host_fragment;


/**
 * This class creates an instance of the NavigationActivity and populates the toolbar with tabs
 */

public class NavigationActivity extends AppCompatActivity implements RideRequest.StatusChangedListener {

    private static final String TAG = "NavigationActivity";

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

        User user = OfflineCache.getReference().retrieveCurrentUser();
        setMenuDisplays(user.getFirstName(), user.getLastName(), user.getEmail());

        OfflineCache.getReference().addRideRequestStatusChangedListener(this);
    }

    @Override
    public void onDestroy() {
        OfflineCache.getReference().removeRideRequestStatusChangedListener(this);
        super.onDestroy();
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

    /**
     * This function directs the user to the correct fragment when the item is pressed.
     * @param item
     * @return
     */
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

    /**
     * Sets the display name and emails for the current user in the sidebar menu using the
     * offline cache.
     */
    public void setMenuDisplays(String firstName, String lastName, String email){
        NavigationView navigationView = findViewById(R.id.nav_view);

        TextView displayName = navigationView.getHeaderView(0)
                .findViewById(R.id.display_name);

        TextView displayEmail = navigationView.getHeaderView(0)
                .findViewById(R.id.display_email);

        displayName.setText(firstName + " " + lastName);
        displayEmail.setText(email);

        return;
    }

    public void onStatusChanged(RideRequest.Status status) {
        final Toast toast = Toast.makeText(this, "Error In Navigation Activity Status Callback", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        View toastView = toast.getView();
        toastView.setBackgroundColor(0x99000000);
        TextView toastText = (TextView)toastView.findViewById(android.R.id.message);
        toastText.setTextColor(0xFFFFFFFF);
        toastText.setTextSize(20);
        toastText.setGravity(Gravity.CENTER);
        ((TextView)toastView.findViewById(android.R.id.message)).setTextColor(0xFFFFFFFF);
        switch (status) {
            case PENDING:
                Log.w(TAG, "Status changed to PENDING... which shouldn't be possible."); // It is now
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver) {
                    FirebaseManager.getReference().fetchDriverInfo(OfflineCache.getReference().retrieveCurrentRideRequest().getDriverUID(), new FirebaseManager.ReturnValueListener<Driver>() {
                        @Override
                        public void returnValue(Driver value) {
                            if (value != null) {
                                toast.setText(String.format("Your offer was declined."));
                                toast.show();
                            }
                        }
                    });
                }
                break;
            case ACCEPTED:
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Rider) {
                    FirebaseManager.getReference().fetchDriverInfo(OfflineCache.getReference().retrieveCurrentRideRequest().getDriverUID(), new FirebaseManager.ReturnValueListener<Driver>() {
                        @Override
                        public void returnValue(Driver value) {
                            if (value != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString("first_name", value.getFirstName());
                                bundle.putString("last_name", value.getLastName());
                                bundle.putString("rating", "99"); //TODO: get driver rating
                                DriverAcceptFragment acceptFragment = new DriverAcceptFragment();
                                acceptFragment.setArguments(bundle);
                                acceptFragment.show(getSupportFragmentManager(), "DRIVER_OFFER");
                            }
                        }
                    });
                }
                break;
            case CONFIRMED:
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver) {
                    FirebaseManager.getReference().fetchRiderInfo(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), new FirebaseManager.ReturnValueListener<Rider>() {
                        @Override
                        public void returnValue(Rider value) {
                            if (value != null) {
                                toast.setText(String.format("Offer accepted by %s %s.", value.getFirstName(), value.getLastName()));
                                toast.show();
                            }
                        }
                    });
                }
                break;
            case EN_ROUTE:
                break;
            case ARRIVED:
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Rider) {
                    toast.setText("You have arrived. Please offer payment.");
                    toast.show();
                } else {
                    toast.setText("Ride complete. Please collect payment.");
                    toast.show();
                }
                break;
            case COMPLETED:
                toast.setText("Payment complete.");
                toast.show();
                break;
            case CANCELLED:
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver) {
                    toast.setText("Your ride offer was rejected.");
                    toast.show();
                }
                break;
        }
    }

    /**
     * This method starts a new sign in activity so that the user can sign in again
     */
    public void logout(){
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }

}
