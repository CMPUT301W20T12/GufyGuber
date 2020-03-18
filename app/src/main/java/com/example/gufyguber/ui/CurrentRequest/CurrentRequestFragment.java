/*
 * CurrentRequestFragment.java
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

package com.example.gufyguber.ui.CurrentRequest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.gufyguber.Driver;
import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.example.gufyguber.ui.Profile.UserContactInformationFragment;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Displays ride request information for a rider's current open request
 * @author Nahome
 */
public class CurrentRequestFragment extends Fragment {

    private CurrentRequestViewModel currentRequestViewModel;
    private Button cancelBtn;

    private TextView driverText;
    private TextView pickupTimeText;
    private TextView arrivalTimeText;
    private TextView pickupLocationText;
    private TextView dropoffLocationText;
    private TextView suggestedFareText;
    private TextView rideStatus;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        currentRequestViewModel =
                ViewModelProviders.of(this).get(CurrentRequestViewModel.class);
        View root = inflater.inflate(R.layout.fragment_current_requests, container, false);
        //final TextView textView = root.findViewById(R.id.text_current_requests);
        currentRequestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cancelBtn = view.findViewById(R.id.cancel_btn);
        driverText = view.findViewById(R.id.driver_name);
        pickupTimeText = view.findViewById(R.id.user_pickup_time);
        arrivalTimeText = view.findViewById(R.id.user_arrival_time);
        pickupLocationText = view.findViewById(R.id.user_pickup_location);
        dropoffLocationText = view.findViewById(R.id.user_dropoff_location);
        suggestedFareText = view.findViewById(R.id.user_fare);
        rideStatus = view.findViewById(R.id.ride_status);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CancelRequestFragment().show(getFragmentManager(), "cancel_request_fragment");

                }
            });

        driverText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UserContactInformationFragment().show(getFragmentManager(), "user_contact_information");
            }
        });

        if (FirebaseManager.getReference().isOnline(getContext())) {
            FirebaseManager.getReference().fetchRideRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<RideRequest>() {
                @Override
                public void returnValue(RideRequest value) {
                    // Cache latest version of request (might be null, but this corresponds to a delete)
                    OfflineCache.getReference().cacheCurrentRideRequest(value);
                    updateUI(value);
                }
            });
        } else {
            updateUI(OfflineCache.getReference().retrieveCurrentRideRequest());
        }
    }

    private void updateUI(RideRequest request) {
        if (request != null) {
            if (request.getDriverUID() == null) {
                driverText.setText("Driver Unavailable");
            } else {
                FirebaseManager.getReference().fetchDriverInfo(request.getDriverUID(), new FirebaseManager.ReturnValueListener<Driver>() {
                    @Override
                    public void returnValue(Driver value) {
                        if (value == null) {
                            driverText.setText("Driver Unavailable");
                        } else {
                            driverText.setText(String.format("%s %s", value.getFirstName(), value.getLastName()));
                        }
                    }
                });
            }
            pickupLocationText.setText(LocationInfo.latlngToString(request.getLocationInfo().getPickup()));
            dropoffLocationText.setText(LocationInfo.latlngToString(request.getLocationInfo().getDropoff()));
            if (request.getTimeInfo().getRequestOpenTime() != null) {
                pickupTimeText.setText(request.getTimeInfo().getRequestOpenTime().toString());
            } else {
                pickupTimeText.setText("Time Unavailable");
            }
            //pickupTimeText.setText(String.format("%t", request.getTimeInfo().getRequestOpenTime()));
            if (request.getTimeInfo().getRequestAcceptedTime() != null) {
                arrivalTimeText.setText(request.getTimeInfo().getRequestAcceptedTime().toString());
            } else {
                arrivalTimeText.setText("Time Unavailable");
            }
            rideStatus.setText(getResources().getString(R.string.request_status, request.getStatus().toString()));
            suggestedFareText.setText(String.format("$%.2f", request.getOfferedFare()));
        } else {
            rideStatus.setText(getResources().getString(R.string.request_status, ' '));
        }
    }
}
