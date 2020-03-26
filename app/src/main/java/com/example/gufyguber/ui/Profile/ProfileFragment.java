/*
 * ProfileFragment.java
 *
 * Version
 *
 * Last edit: dalton, 12/03/20 9:01 AM
 *
 * Copyright (c) CMPUT301W20T12 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 */

package com.example.gufyguber.ui.Profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.gufyguber.Driver;
import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.GlobalDoubleClickHandler;
import com.example.gufyguber.NavigationActivity;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.Rider;
import com.example.gufyguber.SignInActivity;
import com.example.gufyguber.Vehicle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.net.URI;

import static android.telephony.PhoneNumberUtils.formatNumber;

public class ProfileFragment extends Fragment {

    // TAG for debug
    private static final String TAG = "ProfileFragment";

    // Declare variables for later; some are for the Vehicle object and will not
    // be used if the user is a rider
    private ProfileViewModel profileViewModel;
    private TextView firstNameText;
    private TextView lastNameText;
    private TextView phoneText;
    private TextView emailText;
    private TextView makeText;
    private TextView modelText;
    private TextView plateText;
    private TextView seatText;
    private Button editProfile;
    private Button saveProfile;
    private ImageView profilePicture;
    // simple boolean to check if the user is a driver or rider so we know
    private boolean driver;

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             final ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);

        // get he driver boolean from the offline cache by checking if instance driver object
        driver = OfflineCache.getReference().retrieveCurrentUser() instanceof Driver;

        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }
        });

        // which layout file to inflate with
        if (driver) {
            return inflater.inflate(R.layout.fragment_profile_driver, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_profile_rider, container, false);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get all the views by id
        firstNameText = view.findViewById(R.id.user_first_name);
        lastNameText = view.findViewById(R.id.user_last_name);
        emailText = view.findViewById(R.id.rider_email);
        phoneText = view.findViewById(R.id.rider_phone);
        makeText = view.findViewById(R.id.make);
        modelText = view.findViewById(R.id.model);
        plateText = view.findViewById(R.id.plate);
        seatText = view.findViewById(R.id.seats);
        editProfile = view.findViewById(R.id.edit_profile_button);
        saveProfile = view.findViewById(R.id.save_profile_button);
        profilePicture = view.findViewById(R.id.user_image);


        // Use firebase manager to get the user profile info to auto pop the fields
        // use googleSignIn feature to retrieve profile picture


        FirebaseManager.getReference().fetchRiderInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new FirebaseManager.ReturnValueListener<Rider>() {
            @Override
            public void returnValue(Rider value) {
                // Retrieve google sign in profile photo and use Picasso to set the image.

                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
                if (acct != null) {
                    String userPhoto = acct.getPhotoUrl().toString();
                    Picasso.with(getContext()).load(userPhoto).into(profilePicture);
                }
                if (value != null) {
                    firstNameText.setText(value.getFirstName());
                    lastNameText.setText(value.getLastName());
                    emailText.setText(value.getEmail());
                    phoneText.setText(formatNumber(value.getPhoneNumber(), "CA"));
                    if (driver) {
                        // if they are a driver we also need the vehicle info from firebase manager
                        FirebaseManager.getReference().fetchVehicleInfo(FirebaseAuth.getInstance().getUid(),
                                new FirebaseManager.ReturnValueListener<Vehicle>() {
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
                    // log the error if firebase manager fails to get user
                    Log.e(TAG, "Null rider passed to Profile Fragment.");
                }
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                // when user clicks the edit button, change the fields to be editable
                firstNameText.setEnabled(true);
                lastNameText.setEnabled(true);
                emailText.setEnabled(true);
                phoneText.setEnabled(true);
                phoneText.addTextChangedListener(new PhoneNumberFormattingTextWatcher("CA"));
                editProfile.setVisibility(View.GONE);
                saveProfile.setVisibility(View.VISIBLE);
                if (driver) {
                    // do the same for the vehicle if they are driver
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
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                if(validateForm()) {    // check that all fields are filled in
                    String userEmail = emailText.getText().toString().toLowerCase();
                    String userFirstName = firstNameText.getText().toString();
                    String userLastName = lastNameText.getText().toString();
                    String userPhone = phoneText.getText().toString();

                    ((NavigationActivity)getActivity()).setMenuDisplays(
                            userFirstName,
                            userLastName,
                            userEmail);
                    if (driver) {       // if they are a driver, store the new Driver object in FB via the manager
                        FirebaseManager.getReference().storeDriverInfo(new
                                Driver(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                userEmail,
                                userFirstName,
                                userLastName,
                                userPhone,
                                new Vehicle(modelText.getText().toString(),     // Driver needs a Vehicle in the input as well
                                        makeText.getText().toString(),
                                        plateText.getText().toString(),
                                        Integer.parseInt(seatText.getText().toString()))));
                        // update the vehicle as well via the manager
                        FirebaseManager.getReference().storeVehicleInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                new Vehicle(modelText.getText().toString(),
                                makeText.getText().toString(),
                                plateText.getText().toString(),
                                Integer.parseInt(seatText.getText().toString())));
                        // update the authentication email for the firebase projec
                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(userEmail)
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
                        // get the newly updated Driver object to store in the offline cache
                        FirebaseManager.getReference().fetchDriverInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<Driver>() {
                            @Override
                            public void returnValue(Driver value) {
                                OfflineCache.getReference().cacheCurrentUser(value);
                            }
                        });
                    } else {
                        // Same as above, but this time for a rider...
                        FirebaseManager.getReference().storeRiderInfo(new
                                Rider(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                userEmail,
                                userFirstName,
                                userLastName,
                                userPhone));
                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(userEmail)
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
                        FirebaseManager.getReference().fetchRiderInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<Rider>() {
                            @Override
                            public void returnValue(Rider value) {
                                OfflineCache.getReference().cacheCurrentUser(value); 
                            }
                        });
                    }
                    Toast.makeText(getContext(), "Profile successfully updated", Toast.LENGTH_LONG).show();
                    closeFragment();// head back to map after updated
                } else {
                    Toast.makeText(getContext(), "Missing Required Info", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // https://stackoverflow.com/a/43061269
    private void closeFragment(){
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    /**
     * Method to check that all fields in the edit profile screen are filled in
     * @return
     *  Return true if all fields are filled, false if any are empty
     */
    private boolean validateForm() {
        if (driver) {
            return (!TextUtils.isEmpty(emailText.getText().toString()) &&
                    !TextUtils.isEmpty(firstNameText.getText().toString()) &&
                    !TextUtils.isEmpty(lastNameText.getText().toString()) &&
                    !TextUtils.isEmpty(phoneText.getText().toString()) &&
                    !TextUtils.isEmpty(makeText.getText().toString()) &&
                    !TextUtils.isEmpty(modelText.getText().toString()) &&
                    !TextUtils.isEmpty(plateText.getText().toString()) &&
                    !TextUtils.isEmpty(seatText.getText().toString()));
        } else {
            return (!TextUtils.isEmpty(emailText.getText().toString()) &&
                    !TextUtils.isEmpty(firstNameText.getText().toString()) &&
                    !TextUtils.isEmpty(lastNameText.getText().toString()) &&
                    !TextUtils.isEmpty(phoneText.getText().toString()));
        }
    }
}