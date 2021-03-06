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

import com.example.gufyguber.CurrentRequest.DriverAcceptFragment;
import com.example.gufyguber.Models.Driver;
import com.example.gufyguber.Models.RideRequest;
import com.example.gufyguber.Models.Rider;
import com.example.gufyguber.Models.User;
import com.example.gufyguber.Singletons.FirebaseManager;

import android.util.Log;
import android.view.Gravity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.gufyguber.Singletons.OfflineCache;
import com.example.gufyguber.Profile.SignInActivity;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
                R.id.nav_map, R.id.nav_profile, R.id.nav_current_requests, R.id.nav_wallet, R.id.nav_sign_out)
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
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
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
    }

    @Override
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
                // When a driver sees a pending request, it's because the rider has rejected them
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver) {
                    FirebaseManager.getReference().fetchRiderInfo(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), new FirebaseManager.ReturnValueListener<Rider>() {
                        @Override
                        public void returnValue(Rider value) {
                            if (value != null) {
                                toast.setText(String.format("Your offer was declined by %s %s.",
                                        value.getFirstName(), value.getLastName()));
                                toast.show();
                                OfflineCache.getReference().clearCurrentRideRequest();
                            }
                        }
                    });
                }
                break;
            case ACCEPTED:
                // When a rider sees an accepted request, it is because a driver is offering a ride
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Rider) {
                    FirebaseManager.getReference().fetchDriverInfo(OfflineCache.getReference().retrieveCurrentRideRequest().getDriverUID(), new FirebaseManager.ReturnValueListener<Driver>() {
                        @Override
                        public void returnValue(Driver value) {
                            if (value != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString("first_name", value.getFirstName());
                                bundle.putString("last_name", value.getLastName());
                                bundle.putString("positive", value.getRating().getPosPercent(value.getRating().getPositive(), value.getRating().getNegative()));
                                bundle.putString("negative", value.getRating().getNegPercent(value.getRating().getPositive(), value.getRating().getNegative()));
                                DriverAcceptFragment acceptFragment = new DriverAcceptFragment();
                                acceptFragment.setArguments(bundle);
                                acceptFragment.show(getSupportFragmentManager(), "DRIVER_OFFER");
                            }
                        }
                    });
                }
                break;
            case CONFIRMED:
                // When a driver sees a confirmed request, a rider has accepted their offer
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
                // When a rider sees an En Route request, their driver has arrived at the pickup
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Rider) {
                    toast.setText("Your driver is waiting for you!");
                    toast.show();
                }
                break;
            case ARRIVED:
                // When either user sees an arrived request, they are at the destination
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Rider) {
                    toast.setText("You have arrived. Please offer payment.");
                    toast.show();
                } else {
                    toast.setText("Ride complete. Please collect payment.");
                    toast.show();
                }
                break;
            case COMPLETED:
                // When either user sees a completed request, payment has been verified
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Rider) {
                    toast.setText("Payment complete.");
                    toast.show();
                    FirebaseManager.getReference().riderCancelRequest(OfflineCache.getReference().retrieveCurrentRideRequest(), new FirebaseManager.ReturnValueListener<Boolean>() {
                        @Override
                        public void returnValue(Boolean value) {
                            if(value) {
                                Log.w(TAG, "Error deleting completed ride request.");
                            }
                        }
                    });
                } else {
                    toast.setText("Payment received.");
                    toast.show();
                    OfflineCache.getReference().clearCurrentRideRequest();
                }
                break;
            case CANCELLED:
                // Whenever a request is deleted by the rider and it's not rejected or complete
                if (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver) {
                    toast.setText("The ride request was canceled.");
                    toast.show();
                }
                break;
        }
    }

    /**
     * This method starts a new sign in activity so that the user can sign in again
     */
    public void logout(){
        finish(); // Can't forget to finish this activity to clear the stack
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //https://stackoverflow.com/questions/7599955/android-finish-activity-and-start-another-one
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        RideRequest currentRideRequest = OfflineCache.getReference().retrieveCurrentRideRequest();
        if (currentRideRequest != null) {
            if (OfflineCache.getReference().retrieveCurrentRideRequest().getStatus() == RideRequest.Status.COMPLETED) {
                onStatusChanged(RideRequest.Status.COMPLETED);
            }
        }
    }
}
