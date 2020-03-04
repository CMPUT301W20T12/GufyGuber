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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.type.LatLng;

public class CreateRideRequestFragment extends DialogFragment {

    public interface CreateRideRequestListener {
        void onRideRequestCreated (RideRequest newRideRequest);
    }

    //TODO: I'm thinking about this and realizing that by the time the user gets here, they really
    //      only need to enter their fair fare since we pull the rest from behind the scenes, and
    //      should have selected the start and end points first. Perhaps we should go to the map from
    //      this screen instead of going to this screen from the map?

    private CreateRideRequestListener onCreatedListener;

    private EditText fareEditText;

    @Override
    public void onAttach(Context context)  {
        super.onAttach(context);
        if (context instanceof CreateRideRequestListener) {
            onCreatedListener = (CreateRideRequestListener)context;
        } else {
            onCreatedListener = null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle ssavedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.create_ride_request_layout, null);
        fareEditText = view.findViewById(R.id.fare_EditText);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("Create Ride Request")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send Request", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (validateEntries()) {
                            LatLng testPickup = LatLng.newBuilder().setLatitude(13).setLongitude(13).build();
                            LatLng testDropoff = LatLng.newBuilder().setLatitude(31).setLongitude(31).build();
                            RideRequest newRequest = new RideRequest("TestRiderUID", Float.parseFloat(fareEditText.getText().toString()), testPickup, testDropoff);
                            if (onCreatedListener != null) {
                                onCreatedListener.onRideRequestCreated(newRequest);
                            }
                        } else {
                            Toast.makeText(getContext(), "Missing Required Info", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return builder.create();
    }

    /**
     * A helper to check that all required fields have entries
     * @return True if all required information has been entered in the fragment, false otherwise
     */
    private boolean validateEntries() {
        //TODO: Probably have more to validate
        return (!TextUtils.isEmpty(fareEditText.getText().toString()));
    }
}
