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


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
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
import com.example.gufyguber.GenerateQR;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.example.gufyguber.Rider;
import com.example.gufyguber.startScanQR;
import com.example.gufyguber.User;
import com.example.gufyguber.ui.Profile.UserContactInformationFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Displays ride request information for a rider's current open request
 * @author Nahome
 */
public class CurrentRequestFragment extends Fragment implements FirebaseManager.RideRequestListener {

    private CurrentRequestViewModel currentRequestViewModel;
    private Button cancelBtn;
    private Button confirmPickup;
    private Button confirmArrival;
    private Button makePayment;
    private Button takePayment;

    private TextView driverText;
    private TextView riderText;
    private TextView pickupTimeText;
    private TextView arrivalTimeText;
    private TextView pickupLocationText;
    private TextView dropoffLocationText;
    private TextView suggestedFareText;
    private TextView rideStatus;
    private ListenerRegistration rideRequestListener;

    private boolean isDriver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        currentRequestViewModel =
                ViewModelProviders.of(this).get(CurrentRequestViewModel.class);

        isDriver = (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver);

        if (OfflineCache.getReference().retrieveCurrentRideRequest() != null && !isDriver) {
            rideRequestListener = FirebaseManager.getReference().listenToRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), this);
            View root = inflater.inflate(R.layout.fragment_current_requests_rider, container, false);
            //final TextView textView = root.findViewById(R.id.text_current_requests);
            currentRequestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    //textView.setText(s);
                }
            });
            return root;
        } else if (OfflineCache.getReference().retrieveCurrentRideRequest() != null && isDriver) {
            rideRequestListener = FirebaseManager.getReference().listenToRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), this);
            View root = inflater.inflate(R.layout.fragment_current_requests_driver, container, false);
            //final TextView textView = root.findViewById(R.id.text_current_requests);
            currentRequestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    //textView.setText(s);
                }
            });
            return root;
        } else {
            View root = inflater.inflate(R.layout.no_current_request, container, false);
            //final TextView textView = root.findViewById(R.id.text_current_requests);
            currentRequestViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    //textView.setText(s);
                }
            });
            return root;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cancelBtn = view.findViewById(R.id.cancel_btn);
        driverText = view.findViewById(R.id.driver_name);
        riderText = view.findViewById(R.id.rider_name);
        pickupTimeText = view.findViewById(R.id.user_pickup_time);
        arrivalTimeText = view.findViewById(R.id.user_arrival_time);
        pickupLocationText = view.findViewById(R.id.user_pickup_location);
        dropoffLocationText = view.findViewById(R.id.user_dropoff_location);
        suggestedFareText = view.findViewById(R.id.user_fare);
        rideStatus = view.findViewById(R.id.ride_status);
        confirmPickup =  view.findViewById(R.id.confirm_pickup);
        confirmArrival = view.findViewById(R.id.confirm_arrival);
        makePayment = view.findViewById(R.id.make_payment);
        takePayment = view.findViewById(R.id.take_payment);

        if (OfflineCache.getReference().retrieveCurrentRideRequest() != null && !isDriver) {
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new CancelRequestFragment().show(getFragmentManager(), "cancel_request_fragment");

                }
            });
            if (FirebaseManager.getReference().isOnline(getContext())) {
                FirebaseManager.getReference().fetchRideRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<RideRequest>() {
                    @Override
                    public void returnValue(RideRequest value) {
                        // Cache latest version of request (might be null, but this corresponds to a delete)
                        OfflineCache.getReference().cacheCurrentRideRequest(value);
                        updateUIRider(value);
                    }
                });
            } else {
                updateUIRider(OfflineCache.getReference().retrieveCurrentRideRequest());
            }
        } else {
            updateUIDriver(OfflineCache.getReference().retrieveCurrentRideRequest());
        }
    }
    private void updateUIDriver(final RideRequest request) {
        if (request != null) {
            FirebaseManager.getReference().fetchRiderInfo(request.getRiderUID(), new FirebaseManager.ReturnValueListener<Rider>() {
                @Override
                public void returnValue(Rider value) {
                    if (value == null) {
                        riderText.setText("Rider Unavailable");
                    } else {
                        riderText.setText(String.format("%s %s", value.getFirstName(), value.getLastName()));
                        makeNameClickable(value, riderText);
                    }
                }
            });
            pickupLocationText.setText(LocationInfo.latlngToString(request.getLocationInfo().getPickup()));
            dropoffLocationText.setText(LocationInfo.latlngToString(request.getLocationInfo().getDropoff()));
            if (request.getTimeInfo().getRequestOpenTime() != null) {
                pickupTimeText.setText(request.getTimeInfo().getRequestOpenTime().toString());
            } else {
                pickupTimeText.setText("Time Unavailable");
            }
            if (request.getTimeInfo().getRequestAcceptedTime() != null) {
                arrivalTimeText.setText(request.getTimeInfo().getRequestAcceptedTime().toString());
            } else {
                arrivalTimeText.setText("Time Unavailable");
            }
            rideStatus.setText(getResources().getString(R.string.request_status, request.getStatus().toString()));
            if (request.getStatus().toString().equals("Confirmed")) {
                confirmPickup.setVisibility(View.VISIBLE);
                confirmPickup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseManager.getReference().confirmPickup(request);
                    }
                });
            }
            if (request.getStatus().toString().equals("En Route")) {
                confirmPickup.setVisibility(View.GONE);
            }
            if (request.getStatus().toString().equals("Arrived")) {
                takePayment.setVisibility(View.VISIBLE);
                takePayment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent qrIntent = new Intent(getActivity(), startScanQR.class);
                        startActivity(qrIntent);
                    }
                });
            }
            suggestedFareText.setText(String.format("$%.2f", request.getOfferedFare()));
        } else {
            rideStatus.setText(getResources().getString(R.string.request_status, ' '));
        }
    }

    private void updateUIRider(final RideRequest request) {
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
                            makeNameClickable(value, driverText);
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
            if (request.getTimeInfo().getRequestAcceptedTime() != null) {
                arrivalTimeText.setText(request.getTimeInfo().getRequestAcceptedTime().toString());
            } else {
                arrivalTimeText.setText("Time Unavailable");
            }
            rideStatus.setText(getResources().getString(R.string.request_status, request.getStatus().toString()));
            suggestedFareText.setText(String.format("$%.2f", request.getOfferedFare()));
            if (request.getStatus().toString().equals("En Route")) {
                confirmArrival.setVisibility(View.VISIBLE);
                confirmArrival.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseManager.getReference().confirmArrival(request, new FirebaseManager.ReturnValueListener<Boolean>() {
                            @Override
                            public void returnValue(Boolean value) {
                                if (value) {
                                    OfflineCache.getReference().cacheCurrentRideRequest(request);
                                }
                            }
                        });
                    }
                });
            }
            if (request.getStatus().toString().equals("Arrived")) {
                cancelBtn.setVisibility(View.GONE);
                confirmArrival.setVisibility(View.GONE);
                makePayment.setVisibility(View.VISIBLE);
                makePayment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent qrIntent = new Intent(getActivity(), GenerateQR.class);
                        startActivity(qrIntent);
                    }
                });
            }
        } else {
            rideStatus.setText(getResources().getString(R.string.request_status, ' '));
        }

    }

    private void makeNameClickable(final User user, TextView driverText){
        driverText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("email", user.getEmail());
                bundle.putString("phone", user.getPhoneNumber());
                UserContactInformationFragment infoFragment = new UserContactInformationFragment();
                infoFragment.setArguments(bundle);
                infoFragment.show(getFragmentManager(), "user_contact_information");
            }
        });
        driverText.setTextColor(Color.BLUE);
        Paint paint = new Paint();
        paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
        driverText.setPaintFlags(paint.getFlags());

    }

    @Override
    public void onRideRequestUpdated(RideRequest updatedRequest) {
        if (updatedRequest != null) {
            if (isDriver) {
                updateUIDriver(updatedRequest);
            } else {
                updateUIRider(updatedRequest);
            }
        } else {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (rideRequestListener != null) {
            rideRequestListener.remove();
            rideRequestListener = null;
        }
        super.onDestroy();
    }
}
