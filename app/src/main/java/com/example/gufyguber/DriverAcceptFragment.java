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
 * DriverAcceptFragment.java
 *
 * Last edit: dalton, 19/03/20 12:19 PM
 *
 * Version
 */

package com.example.gufyguber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DriverAcceptFragment extends DialogFragment {

    private TextView driverName;
    private TextView driverRating;
    private Button accept;
    private Button decline;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.driver_offer, null);
        Bundle bundle = this.getArguments();

        String fName = bundle.getString("first_name");
        String lName = bundle.getString("last_name");
        String rating = bundle.getString("rating");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        driverName = view.findViewById(R.id.driver_offer);
        driverName.setText(String.format("%s %s", fName, lName));
        driverRating = view.findViewById(R.id.driver_rating);
        driverRating.setText(rating);

        accept = view.findViewById(R.id.accept_driver);
        decline = view.findViewById(R.id.decline_driver);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (OfflineCache.getReference().retrieveCurrentRideRequest() != null) {
                    OfflineCache.getReference().retrieveCurrentRideRequest().setStatus(RideRequest.Status.ACCEPTED);
                    FirebaseManager.getReference().storeRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest());
                }
                FirebaseManager.getReference().riderAcceptDriverOffer(OfflineCache.getReference().retrieveCurrentUser().getUID(), OfflineCache.getReference().retrieveCurrentRideRequest(), new FirebaseManager.ReturnValueListener<Boolean>() {
                    @Override
                    public void returnValue(Boolean value) {
                        if (!value) {
                            Toast toast = Toast.makeText(getContext(), "Ride request unavailable.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dismiss();
            }
        });

        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseManager.getReference().riderDeclineDriverOffer(OfflineCache.getReference().retrieveCurrentUser().getUID(), OfflineCache.getReference().retrieveCurrentRideRequest(), new FirebaseManager.ReturnValueListener<Boolean>() {
                    @Override
                    public void returnValue(Boolean value) {
                        if (!value) {
                            Toast toast = Toast.makeText(getContext(), "Ride request unavailable.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dismiss();
            }
        });
        
        setCancelable(false);

        return builder
                .setView(view)
                .create();
    }
}
