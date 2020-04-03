/*
 * Copyright (c) 2020  GufyGuber. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * CreateRideRequestFragment.java
 *
 * Last edit: scott, 02/04/20 5:57 PM
 *
 * Version
 */

package com.example.gufyguber.CurrentRequest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.Utility.CurrencyTextWatcher;
import com.example.gufyguber.Models.LocationInfo;
import com.example.gufyguber.Models.RideRequest;
import com.example.gufyguber.R;
import com.example.gufyguber.Utility.GlobalDoubleClickHandler;
import com.example.gufyguber.Singletons.OfflineCache;
import com.google.android.gms.maps.model.LatLng;

/**
 * Manages the dialog fragment that pops up when a Rider is attempting to create a new ride request
 * @author Robert MacGillivray | Mar.02.2020
 */
public class CreateRideRequestFragment extends DialogFragment {

    public interface CreateRideRequestListener {
        void onRideRequestCreated (RideRequest newRideRequest);
    }

    public interface CancelCreateRideRequestListener {
        void onRideRequestCreationCancelled();
    }

    private CreateRideRequestListener onCreatedListener;
    private CancelCreateRideRequestListener onCreationCancelledListener;

    private EditText fareEditText;
    private EditText startLocationEditText;
    private EditText endLocationEditText;
    private Button positiveButton;
    private Button negativeButton;

    private TextView fairFareText;

    public boolean settingStart = false;
    public boolean settingEnd = false;

    private float tempFare = -1f;
    private LocationInfo tempLocationInfo;

    private void initUIElements() {
        if (tempFare >= 0) {
            fareEditText.setText(String.format("$%.2f", tempFare));
        } else {
            fareEditText.setText("");
        }

        if (tempLocationInfo != null) {
            if (tempLocationInfo.getPickup() != null) {
                startLocationEditText.setText(tempLocationInfo.getPickupName());
            } else {
                startLocationEditText.setText("");
            }
            if (tempLocationInfo.getDropoff() != null) {
                endLocationEditText.setText(tempLocationInfo.getDropoffName());
            } else {
                endLocationEditText.setText("");
            }

            float fairFare = RideRequest.fairFareEstimate(tempLocationInfo);
            if (fairFare < 0) {
                clearFairFare();
            } else {
                updateFairFare(fairFare);
            }
        } else {
            clearFairFare();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getParentFragment() instanceof CreateRideRequestListener) {
            onCreatedListener = (CreateRideRequestListener)getParentFragment();
        } else {
            onCreatedListener = null;
        }

        if (getParentFragment() instanceof CancelCreateRideRequestListener) {
            onCreationCancelledListener = (CancelCreateRideRequestListener)getParentFragment();
        } else {
            onCreationCancelledListener = null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.create_ride_request_layout, null);
        fareEditText = view.findViewById(R.id.fare_EditText);
        fareEditText.addTextChangedListener(new CurrencyTextWatcher(fareEditText));
        startLocationEditText = view.findViewById(R.id.start_location_EditText);
        endLocationEditText = view.findViewById(R.id.end_location_EditText);
        fairFareText = view.findViewById(R.id.fair_fare_textview);
        positiveButton = view.findViewById(R.id.create_ride_request_button);
        negativeButton = view.findViewById(R.id.cancel_ride_request_button);
        initUIElements();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                if (validateEntries()) {
                    RideRequest newRequest = new RideRequest(OfflineCache.getReference().retrieveCurrentUser().getUID(),
                            Float.parseFloat(fareEditText.getText().toString().replaceAll("[$]","")),
                            tempLocationInfo);
                    if (onCreatedListener != null) {
                        onCreatedListener.onRideRequestCreated(newRequest);
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Missing Required Info", Toast.LENGTH_SHORT).show();
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                if (onCreationCancelledListener != null) {
                    onCreationCancelledListener.onRideRequestCreationCancelled();
                }
                dialog.dismiss();
            }
        });

        startLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                settingStart = true;
                if (fareEditText.getText().length() > 0) {
                    tempFare = Float.parseFloat(fareEditText.getText().toString().replaceAll("[$]", ""));
                }
                dialog.dismiss();
            }
        });

        endLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                settingEnd = true;
                if (fareEditText.getText().length() > 0) {
                    tempFare = Float.parseFloat(fareEditText.getText().toString().replaceAll("[$]", ""));
                }
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public void setNewPickup(LatLng pickup, String pickupName) {
        if (tempLocationInfo == null) {
            tempLocationInfo = new LocationInfo();
        }

        tempLocationInfo.setPickup(pickup);
        tempLocationInfo.setPickupName(pickupName);
        settingStart = false;
    }

    public void setNewDropoff(LatLng dropoff, String dropoffName) {
        if (tempLocationInfo == null) {
            tempLocationInfo = new LocationInfo();
        }

        tempLocationInfo.setDropoff(dropoff);
        tempLocationInfo.setDropoffName(dropoffName);
        settingEnd = false;
    }

    private void updateFairFare(float fairFare) {
        if (fairFareText != null) {
            fairFareText.setText(String.format("Fair Fare(TM): $%.2f", fairFare));
        }
    }

    private void clearFairFare() {
        if (fairFareText != null) {
            fairFareText.setText("Fair Fare(TM): --");
        }
    }

    /**
     * A helper to check that all required fields have entries
     * @return True if all required information has been entered in the fragment, false otherwise
     */
    private boolean validateEntries() {
        return (!TextUtils.isEmpty(fareEditText.getText().toString()) &&
                !TextUtils.isEmpty(startLocationEditText.getText().toString()) &&
                !TextUtils.isEmpty(endLocationEditText.getText().toString()));
    }

    /**
     * @return True if this dialog already had a pickup location
     */
    public boolean hasPickupData() {
        return (tempLocationInfo != null && tempLocationInfo.getPickup() != null);
    }

    /**
     * @return True if this dialog already has a dropoff location
     */
    public boolean hasDropoffData() {
        return (tempLocationInfo != null && tempLocationInfo.getDropoff() != null);
    }
}
