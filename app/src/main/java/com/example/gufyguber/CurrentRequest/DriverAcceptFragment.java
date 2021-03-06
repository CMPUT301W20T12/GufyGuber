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
 * Last edit: scott, 02/04/20 5:58 PM
 *
 * Version
 */

package com.example.gufyguber.CurrentRequest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.R;
import com.example.gufyguber.Singletons.FirebaseManager;
import com.example.gufyguber.Utility.GlobalDoubleClickHandler;
import com.example.gufyguber.Singletons.OfflineCache;

public class DriverAcceptFragment extends DialogFragment {

    private TextView driverName;
    private TextView positiveRating;
    private TextView negativeRating;
    private Button accept;
    private Button decline;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.driver_offer, null);
        Bundle bundle = this.getArguments();

        String fName = bundle.getString("first_name");
        String lName = bundle.getString("last_name");
        String positive = bundle.getString("positive");
        String negative = bundle.getString("negative");

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        driverName = view.findViewById(R.id.driver_offer);
        driverName.setText(String.format("%s %s", fName, lName));
        positiveRating = view.findViewById(R.id.driver_rating_pos);
        positiveRating.setText(positive);
        negativeRating = view.findViewById(R.id.driver_rating_neg);
        negativeRating.setText(negative);

        accept = view.findViewById(R.id.accept_driver);
        decline = view.findViewById(R.id.decline_driver);

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                // Attempt to accept the request. Show an error popup if it fails.
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
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

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
