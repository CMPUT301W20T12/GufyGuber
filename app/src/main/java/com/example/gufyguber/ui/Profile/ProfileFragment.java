/*
 * ProfileFragment.java
 *
 * Version
 *
 * Last edit: dalton, 10/03/20 12:12 AM
 *
 * Copyright (c) CMPUT301W20T12 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 */

package com.example.gufyguber.ui.Profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.gufyguber.Driver;
import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.Rider;
import com.example.gufyguber.Vehicle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ProfileViewModel profileViewModel;
    private TextView nameText;
    private TextView phoneText;
    private TextView emailText;
    private TextView makeText;
    private TextView modelText;
    private TextView plateText;
    private TextView seatText;
    private Button editProfile;
    private Button saveProfile;
    private boolean driver;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);

        driver = OfflineCache.getReference().retrieveCurrentUser() instanceof Driver;

        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });

        if (!driver) {
            View root = inflater.inflate(R.layout.fragment_profile_rider, container, false);
            return root;
        } else {
            View root = inflater.inflate(R.layout.fragment_profile_driver, container, false);
            return root;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameText = view.findViewById(R.id.rider_name);
        emailText = view.findViewById(R.id.rider_email);
        phoneText = view.findViewById(R.id.rider_phone);
        makeText = view.findViewById(R.id.make);
        modelText = view.findViewById(R.id.model);
        plateText = view.findViewById(R.id.plate);
        seatText = view.findViewById(R.id.seats);

        editProfile = view.findViewById(R.id.edit_profile_button);
        saveProfile = view.findViewById(R.id.save_profile_button);

        FirebaseManager.getReference().fetchRiderInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<Rider>() {
            @Override
            public void returnValue(Rider value) {
                if (value != null) {
                    nameText.setText(String.format("%s %s", value.getFirstName(), value.getLastName()));
                    emailText.setText(value.getEmail());
                    phoneText.setText(value.getPhoneNumber());
                    if (driver) {
                        FirebaseManager.getReference().fetchVehicleInfo(FirebaseAuth.getInstance().getUid(), new FirebaseManager.ReturnValueListener<Vehicle>() {
                            @Override
                            public void returnValue(Vehicle value) {
                                makeText.setText(value.getMake());
                                modelText.setText(value.getModel());
                                plateText.setText(value.getPlateNumber());
                                seatText.setText(String.valueOf(value.getSeatNumber()));
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Null rider passed to Profile Fragment.");
                }
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameText.setEnabled(true);
                emailText.setEnabled(true);
                phoneText.setEnabled(true);
                editProfile.setVisibility(View.GONE);
                saveProfile.setVisibility(View.VISIBLE);
                if (driver) {
                    makeText.setEnabled(true);
                    modelText.setEnabled(true);
                    seatText.setEnabled(true);
                    plateText.setEnabled(true);
                }
            }
        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()) {
                    if (driver) {
                        FirebaseManager.getReference().storeDriverInfo(new
                                Driver(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                emailText.getText().toString().toLowerCase(),
                                nameText.getText().toString().split(" ")[0].toLowerCase(),
                                nameText.getText().toString().split(" ")[1].toLowerCase(),
                                phoneText.getText().toString(),
                                new Vehicle(modelText.getText().toString(),
                                        makeText.getText().toString(),
                                        plateText.getText().toString(),
                                        Integer.parseInt(seatText.getText().toString()))));
                        FirebaseManager.getReference().storeVehicleInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), new Vehicle(modelText.getText().toString(),
                                makeText.getText().toString(),
                                plateText.getText().toString(),
                                Integer.parseInt(seatText.getText().toString())));
                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(emailText.getText().toString().toLowerCase())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User email address updated.");
                                        } else {
                                            Log.d(TAG, "Failed to update user email address.");
                                        }
                                    }
                                });
                    } else {
                        FirebaseManager.getReference().storeRiderInfo(new
                                Rider(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                emailText.getText().toString().toLowerCase(),
                                nameText.getText().toString().split(" ")[0].toLowerCase(),
                                nameText.getText().toString().split(" ")[1].toLowerCase(),
                                phoneText.getText().toString()));
                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(emailText.getText().toString().toLowerCase())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "User email address updated.");
                                        } else {
                                            Log.d(TAG, "Failed to update user email address.");
                                        }
                                    }
                                });
                    }
                    Toast.makeText(getContext(), "Profile successfully updated", Toast.LENGTH_LONG).show();
                    getActivity().onBackPressed();
                } else {
                    Toast.makeText(getContext(), "Missing Required Info", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateForm() {
        return (!TextUtils.isEmpty(emailText.getText().toString()) &&
                !TextUtils.isEmpty(nameText.getText().toString()) &&
                !TextUtils.isEmpty(phoneText.getText().toString()));
    }
}