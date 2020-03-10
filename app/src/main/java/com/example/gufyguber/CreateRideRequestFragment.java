/*
 *    Copyright (c) 2020. Gufy Guber
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

// Name: Robert MacGillivray
// File: CreateRideRequestFragment.java
// Date: Mar.02.2020
// Purpose: To control a fragment that Riders use to fill in their ride requests

// Last Updated: Mar.02.2020 by Robert MacGillivray

package com.example.gufyguber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;

public class CreateRideRequestFragment extends DialogFragment {

    public interface CreateRideRequestListener {
        void onRideRequestCreated (RideRequest newRideRequest);
    }

    public interface CancelCreateRideRequestListener {
        void onRideRequestCreationCancelled();
    }

    //TODO: Looks a little clunky. Maybe create a custom fragment that covers the bottom 1/3 of the screen
    //      with the three fields, and a positive and negative button. Can hide when start or end fields are touched
    //      just like the alert dialog now.

    private CreateRideRequestListener onCreatedListener;
    private CancelCreateRideRequestListener onCreationCancelledListener;

    private EditText fareEditText;
    private EditText startLocationEditText;
    private EditText endLocationEditText;

    private TextView fairFareText;

    public boolean settingStart = false;
    public boolean settingEnd = false;

    private float tempFare = -1f;
    private LocationInfo tempLocationInfo;

    public CreateRideRequestFragment() {

    }

    public CreateRideRequestFragment(float initialFare, LocationInfo initialLocationInfo) {
        tempFare = initialFare;
        tempLocationInfo = initialLocationInfo;
    }

    private void initUIElements() {
        if (tempFare >= 0) {
            fareEditText.setText(String.format("%.2f", tempFare));
        } else {
            fareEditText.setText("");
        }

        if (tempLocationInfo != null) {
            if (tempLocationInfo.getPickup() != null) {
                startLocationEditText.setText(LocationInfo.latlngToString(tempLocationInfo.getPickup()));
            } else {
                startLocationEditText.setText("");
            }
            if (tempLocationInfo.getDropoff() != null) {
                endLocationEditText.setText(LocationInfo.latlngToString(tempLocationInfo.getDropoff()));
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
        startLocationEditText = view.findViewById(R.id.start_location_EditText);
        endLocationEditText = view.findViewById(R.id.end_location_EditText);
        fairFareText = view.findViewById(R.id.fair_fare_textview);
        initUIElements();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Create Ride Request")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send Request", null);

        // Need to set the onClick here instead of in the builder because normal AlertDialog onClicks
        // automatically close the dialog
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateEntries()) {
                    RideRequest newRequest = new RideRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), Float.parseFloat(fareEditText.getText().toString()), tempLocationInfo);
                    if (onCreatedListener != null) {
                        onCreatedListener.onRideRequestCreated(newRequest);
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Missing Required Info", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onCreationCancelledListener != null) {
                    onCreationCancelledListener.onRideRequestCreationCancelled();
                }
                dialog.dismiss();
            }
        });

        startLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingStart = true;
                dialog.dismiss();
            }
        });

        endLocationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingEnd = true;
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public void setNewPickup(LatLng pickup) {
        if (tempLocationInfo == null) {
            tempLocationInfo = new LocationInfo();
        }

        tempLocationInfo.setPickup(pickup);
        settingStart = false;
    }

    public void setNewDropoff(LatLng dropoff) {
        if (tempLocationInfo == null) {
            tempLocationInfo = new LocationInfo();
        }

        tempLocationInfo.setDropoff(dropoff);
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
}
