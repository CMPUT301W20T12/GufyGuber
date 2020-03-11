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

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.google.firebase.auth.FirebaseAuth;

public class CurrentRequestFragment extends Fragment {

    private CurrentRequestViewModel currentRequestViewModel;
    private Button cancelBtn;

    private TextView destinationText;
    private TextView pickupTimeText;
    private TextView arrivalTimeText;
    private TextView pickupLocationText;
    private TextView dropoffLocationText;
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
        destinationText = view.findViewById(R.id.user_destination);
        pickupTimeText = view.findViewById(R.id.user_pickup_time);
        arrivalTimeText = view.findViewById(R.id.user_arrival_time);
        pickupLocationText = view.findViewById(R.id.user_pickup_location);
        dropoffLocationText = view.findViewById(R.id.user_dropoff_location);
        rideStatus = view.findViewById(R.id.ride_status);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CancelRequestFragment().show(getFragmentManager(), "cancel_request_fragment");

                }
            });

        FirebaseManager.getReference().fetchRideRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<RideRequest>() {
            @Override
            public void returnValue(RideRequest value) {
                if (value != null) {
                    pickupLocationText.setText(LocationInfo.latlngToString(value.getLocationInfo().getPickup()));
                    dropoffLocationText.setText(LocationInfo.latlngToString(value.getLocationInfo().getDropoff()));
                    rideStatus.setText(getResources().getString(R.string.request_status, value.getStatus().toString()));
                } else {
                    rideStatus.setText(getResources().getString(R.string.request_status, ' '));
                }
            }
        });

    }
}
