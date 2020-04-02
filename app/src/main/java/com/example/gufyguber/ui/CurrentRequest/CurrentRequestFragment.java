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


import android.content.Intent;
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
import com.example.gufyguber.GlobalDoubleClickHandler;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.example.gufyguber.Rider;
import com.example.gufyguber.startScanQR;
import com.example.gufyguber.User;
import com.example.gufyguber.ui.Profile.DriverContactInformationFragment;
import com.example.gufyguber.ui.Profile.RiderContactInformationFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Displays ride request information for a rider's current open request
 * @author Nahome
 */
public class CurrentRequestFragment extends Fragment implements FirebaseManager.RideRequestListener {
    private static final String TAG = "CurrentRequestFragment";

    private Button cancelBtn;
    private Button confirmPickup;
    private Button confirmArrival;
    private Button makePayment;
    private Button takePayment;
    private Button driverContactBtn;
    private Button riderContactBtn;
    private TextView driverText;
    private TextView riderText;
    private TextView pickupTimeText;
    private TextView arrivalTimeText;
    private TextView pickupLocationText;
    private TextView dropoffLocationText;
    private TextView suggestedFareText;
    private TextView rideStatus;
    private ListenerRegistration rideRequestListener;
    private SimpleDateFormat formatter;
    private boolean isDriver;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        isDriver = (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver);
        if (OfflineCache.getReference().retrieveCurrentRideRequest() != null) {
            rideRequestListener = FirebaseManager.getReference().listenToRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), this);
            if (isDriver) {
                return inflater.inflate(R.layout.fragment_current_requests_driver, container, false);
            } else {
                return inflater.inflate(R.layout.fragment_current_requests_rider, container, false);
            }
        } else {
            return inflater.inflate(R.layout.no_current_request, container, false);
        }
    }

    /**
     * Creates the view of UI elements, depending on if user is a Rider or Driver
     * @param view
     * @param savedInstanceState
     */
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
        driverContactBtn = view.findViewById(R.id.driver_contact_button);
        riderContactBtn = view.findViewById(R.id.rider_contact_button);

        if (OfflineCache.getReference().retrieveCurrentRideRequest() != null) {
            if (isDriver) {
                updateUIDriver(OfflineCache.getReference().retrieveCurrentRideRequest());
            } else {
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalDoubleClickHandler.isDoubleClick()) {
                            return;
                        }

                        new CancelRequestFragment().show(getChildFragmentManager(), "cancel_request_fragment");
                    }
                });
                updateUIRider(OfflineCache.getReference().retrieveCurrentRideRequest());
            }
        } else {
            updateUIDriver(null);
        }
    }

    /**
     * Updates CUI elements according to the Driver's layout
     * @param request
     */
    private void updateUIDriver(final RideRequest request) {
        if (request != null) {
            FirebaseManager.getReference().fetchRiderInfo(request.getRiderUID(), new FirebaseManager.ReturnValueListener<Rider>() {
                @Override
                public void returnValue(Rider value) {
                    if (value == null) {
                        riderText.setText("Rider Info Unavailable");
                    } else {
                        riderText.setText(String.format("%s %s", value.getFirstName(), value.getLastName()));
                        activateRiderContactButton(value, riderContactBtn);
                        //activateContactButton(value, riderContactBtn);
                    }
                }
            });
            pickupLocationText.setText(request.getLocationInfo().getPickupName());
            dropoffLocationText.setText(request.getLocationInfo().getDropoffName());

            formatter = new SimpleDateFormat("h:mm a, MMMM dd yyyy", Locale.CANADA);
            if (request.getTimeInfo().getRequestOpenTime() != null) {
                pickupTimeText.setText(formatter.format(request.getTimeInfo().getRequestOpenTime()));
            } else {
                pickupTimeText.setText("Time Unavailable");
            }
            if (request.getTimeInfo().getRequestAcceptedTime() != null) {
                arrivalTimeText.setText(formatter.format(request.getTimeInfo().getRequestAcceptedTime()));
            } else {
                arrivalTimeText.setText("Time Unavailable");
            }
            rideStatus.setText(getResources().getString(R.string.request_status, request.getStatus().toString()));
            if (request.getStatus().toString().equals("Confirmed")) {
                confirmPickup.setVisibility(View.VISIBLE);
                confirmPickup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalDoubleClickHandler.isDoubleClick()) {
                            return;
                        }

                        FirebaseManager.getReference().confirmPickup(request, new FirebaseManager.ReturnValueListener<Boolean>() {
                            @Override
                            public void returnValue(Boolean value) {
                                if (!value) {
                                    Log.e(TAG, "Confirm pickup failed.");
                                }
                            }
                        });
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
                        if (GlobalDoubleClickHandler.isDoubleClick()) {
                            return;
                        }

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

    /**
     * Updates CUI elements according to the Rider's layout
     * @param request
     */
    private void updateUIRider(final RideRequest request) {
        if (request != null) {
            if (request.getDriverUID() == null) {
                driverText.setText("No Driver");
            } else {
                FirebaseManager.getReference().fetchDriverInfo(request.getDriverUID(), new FirebaseManager.ReturnValueListener<Driver>() {
                    @Override
                    public void returnValue(Driver value) {
                        if (value == null) {
                            driverText.setText("Driver Info Unavailable");
                        } else {
                            driverText.setText(String.format("%s %s", value.getFirstName(), value.getLastName()));
                            activateDriverContactButton(value, driverContactBtn);
                            //activateContactButton(value, driverContactBtn);
                        }
                    }

                });
            }
            pickupLocationText.setText(request.getLocationInfo().getPickupName());
            dropoffLocationText.setText(request.getLocationInfo().getDropoffName());
            formatter = new SimpleDateFormat("h:mm a, MMMM dd yyyy", Locale.CANADA);
            if (request.getTimeInfo().getRequestOpenTime() != null) {
                pickupTimeText.setText(formatter.format(request.getTimeInfo().getRequestOpenTime()));
            } else {
                pickupTimeText.setText("Time Unavailable");
            }
            if (request.getTimeInfo().getRequestAcceptedTime() != null) {
                arrivalTimeText.setText(formatter.format(request.getTimeInfo().getRequestAcceptedTime()));
            } else {
                arrivalTimeText.setText("Time Unavailable");
            }
            rideStatus.setText(getResources().getString(R.string.request_status, request.getStatus()));
            suggestedFareText.setText(String.format("$%.2f", request.getOfferedFare()));

            if (request.getStatus().toString().equals("En Route")) {
                // When status is En Route, make rider's button set to confirm
                // arrival at drop off destination
                confirmArrival.setVisibility(View.VISIBLE);
                confirmArrival.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalDoubleClickHandler.isDoubleClick()) {
                            return;
                        }

                        FirebaseManager.getReference().confirmArrival(request, new FirebaseManager.ReturnValueListener<Boolean>() {
                            @Override
                            public void returnValue(Boolean value) {
                                if (!value) {
                                    Log.e(TAG, "Arrival confirmation failed.");
                                }
                            }
                        });
                    }
                });
            }

            if (request.getStatus().toString().equals("Arrived")) {
                // When status is Arrived, give rider a button that
                // generates QR code for their driver
                cancelBtn.setVisibility(View.GONE);
                confirmArrival.setVisibility(View.GONE);
                makePayment.setVisibility(View.VISIBLE);
                makePayment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GlobalDoubleClickHandler.isDoubleClick()) {
                            return;
                        }
                        Intent qrIntent = new Intent(getActivity(), GenerateQR.class);
                        startActivity(qrIntent);
                        getActivity().finish();
                    }
                });
            }
        } else {
            rideStatus.setText(getResources().getString(R.string.request_status, ' '));
        }
    }

    private void activateDriverContactButton(final Driver driver, Button contactBtn){
        contactBtn.setVisibility(View.VISIBLE);
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("email", driver.getEmail());
                bundle.putString("phone", driver.getPhoneNumber());
                bundle.putString("make", driver.getVehicle().getModel());
                bundle.putString("model", driver.getVehicle().getMake());
                bundle.putString("plate", driver.getVehicle().getPlateNumber());
                bundle.putString("positive", driver.getRating().getPosPercent(driver.getRating().getPositive(), driver.getRating().getNegative()));
                bundle.putString("negative", driver.getRating().getNegPercent(driver.getRating().getPositive(), driver.getRating().getNegative()));
                DriverContactInformationFragment infoFragment = new DriverContactInformationFragment();
                infoFragment.setArguments(bundle);
                infoFragment.show(getFragmentManager(), "user_contact_information");
            }
        });

    }
    private void activateRiderContactButton(final User user, Button contactBtn){
        contactBtn.setVisibility(View.VISIBLE);
        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString("email", user.getEmail());
                bundle.putString("phone", user.getPhoneNumber());
                RiderContactInformationFragment infoFragment = new RiderContactInformationFragment();
                infoFragment.setArguments(bundle);
                infoFragment.show(getChildFragmentManager(), "user_contact_information");
            }
        });

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
