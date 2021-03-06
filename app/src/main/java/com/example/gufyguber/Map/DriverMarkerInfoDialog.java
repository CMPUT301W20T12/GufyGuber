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
 * DriverMarkerInfoDialog.java
 *
 * Last edit: scott, 11/03/20 11:32 PM
 *
 * Version
 */

package com.example.gufyguber.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.Singletons.FirebaseManager;
import com.example.gufyguber.Utility.GlobalDoubleClickHandler;
import com.example.gufyguber.Singletons.OfflineCache;
import com.example.gufyguber.R;

public class DriverMarkerInfoDialog extends DialogFragment {
    private Button noBtn;
    private Button yesBtn;
    private TextView titleText;
    private TextView infoText;
    private DriverRequestMarker clickedMarker;

    private Context parentContext;

    public DriverMarkerInfoDialog(DriverRequestMarker clickedMarker) {
        this.clickedMarker = clickedMarker;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        parentContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.driver_marker_dialog, null);

        noBtn = view.findViewById(R.id.decline_ride_request_button);
        yesBtn = view.findViewById(R.id.accept_ride_request_button);
        titleText = view.findViewById(R.id.ride_request_title);
        infoText = view.findViewById(R.id.ride_request_info);

        titleText.setText(clickedMarker.getMarker().getTitle());
        infoText.setText(clickedMarker.getMarker().getSnippet());

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                dismiss();
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                // Try to claim the request. If it succeeds, cache the request and update Firestore
                FirebaseManager.getReference().driverAcceptRideRequest(OfflineCache.getReference().retrieveCurrentUser().getUID(), clickedMarker.getRideRequest(),
                        new FirebaseManager.ReturnValueListener<Boolean>() {
                    @Override
                    public void returnValue(Boolean value) {
                        if (!value) {
                            Toast toast = Toast.makeText(parentContext, "Ride request unavailable.", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view);

        return builder.create();
    }
}
