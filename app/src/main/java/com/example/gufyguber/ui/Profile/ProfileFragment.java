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
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.gufyguber.Rating;
import com.example.gufyguber.Rider;
import com.example.gufyguber.SignInActivity;
import com.example.gufyguber.User;
import com.example.gufyguber.Vehicle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static android.telephony.PhoneNumberUtils.formatNumber;

public class ProfileFragment extends Fragment {

    // TAG for debug
    private static final String TAG = "ProfileFragment";

    // Declare variables for later; some are for the Vehicle object and will not
    // be used if the user is a rider
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
    private TextView positive;
    private TextView negative;
    private int numPositive;
    private int numNegative;

    // simple boolean to check if the user is a driver or rider so we know
    private boolean driver;

    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        // get he driver boolean from the offline cache by checking if instance driver object
        driver = OfflineCache.getReference().retrieveCurrentUser() instanceof Driver;

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
        positive = view.findViewById(R.id.positive);
        negative = view.findViewById(R.id.negative);

        // Use cached info or firebase manager to get the user profile info to auto pop the fields
        User user = OfflineCache.getReference().retrieveCurrentUser();
        if (user != null) {
            populateForm(user);
        } else {
            if (driver) {
                FirebaseManager.getReference().fetchDriverInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<Driver>() {
                    @Override
                    public void returnValue(Driver value) {
                        populateForm(value);
                    }
                });
            } else {
                FirebaseManager.getReference().fetchRiderInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<Rider>() {
                    @Override
                    public void returnValue(Rider value) {
                        populateForm(value);
                    }
                });
            }
        }

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                // when user clicks the edit button, change the fields to be editable
                firstNameText.setEnabled(true);
                lastNameText.setEnabled(true);
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
                phoneText.setText(PhoneNumberUtils.formatNumber(phoneText.getText().toString(), "CA"));

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
                        Driver updatedDriver = new Driver(OfflineCache.getReference().retrieveCurrentUser().getUID(),
                                userEmail, userFirstName, userLastName, userPhone,
                                new Vehicle(modelText.getText().toString(), makeText.getText().toString(),
                                        plateText.getText().toString(), Integer.parseInt(seatText.getText().toString())),
                                new Rating(numPositive, numNegative));

                        OfflineCache.getReference().cacheCurrentUser(updatedDriver);
                        FirebaseManager.getReference().storeDriverInfo(updatedDriver);
                        FirebaseManager.getReference().storeVehicleInfo(updatedDriver.getUID(), updatedDriver.getVehicle());
                        FirebaseManager.getReference().storeRatingInfo(updatedDriver.getUID(), updatedDriver.getRating());

                        // update the authentication email for the firebase project
                        if (FirebaseAuth.getInstance() != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
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
                        }
                    } else {
                        // Same as above, but this time for a rider...
                        Rider updatedRider = new Rider(OfflineCache.getReference().retrieveCurrentUser().getUID(),
                                userEmail, userFirstName, userLastName, userPhone);

                        OfflineCache.getReference().cacheCurrentUser(updatedRider);
                        FirebaseManager.getReference().storeRiderInfo(updatedRider);
                        // update the authentication email for the firebase project
                        if (FirebaseAuth.getInstance() != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
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
                        }
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
        String phoneRegex = "^(\\+?\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$"; //https://stackoverflow.com/questions/16699007/regular-expression-to-match-standard-10-digit-phone-number

        Boolean isValid = true;

        HashMap<EditText, String> fields = new HashMap<>();

        fields.put((EditText) firstNameText, firstNameText.getText().toString());
        fields.put((EditText) lastNameText, lastNameText.getText().toString());
        fields.put((EditText) phoneText, phoneText.getText().toString());
        //fields.put((EditText) phoneText, phoneText.getText().toString());
        if(driver){
            fields.put((EditText) makeText, makeText.getText().toString());
            fields.put((EditText) modelText, modelText.getText().toString());
            fields.put((EditText) plateText, plateText.getText().toString());
            fields.put((EditText) seatText, seatText.getText().toString());
        }

        for (Map.Entry field : fields.entrySet()) {
            /* iterate over Map and check all fields are filled in*/
            if (TextUtils.isEmpty((String) field.getValue())) {
                ((EditText) field.getKey()).setError("Required");   /* if not filled in, flag */
                isValid = false;
            } else {
                ((EditText) field.getKey()).setError(null);                 /* else, update counter */
            }
        }

        if(isValid){
            if(!phoneText.getText().toString().matches(phoneRegex)) {
                phoneText.setError("Invalid Phone Number");
                isValid = false;
            }
        }

        return isValid;  
    }
    
    /**
     * Populates the Profile form based on provided user information
     * @param user The user information to use to populate the form
     */
    private void populateForm(User user) {
        // Retrieve google sign in profile photo and use Picasso to set the image.
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (acct != null) {
            String userPhoto = acct.getPhotoUrl().toString();
            Picasso.with(getContext()).load(userPhoto).into(profilePicture);
        } else {
            Log.e(TAG, "Invalid Google account details for profile picture.");
        }

        if (user != null) {
            firstNameText.setText(user.getFirstName());
            lastNameText.setText(user.getLastName());
            emailText.setText(user.getEmail());
            phoneText.setText(user.getPhoneNumber());
            if (driver) {
                Vehicle vehicle = ((Driver)user).getVehicle();
                makeText.setText(vehicle.getMake());
                modelText.setText(vehicle.getModel());
                plateText.setText(vehicle.getPlateNumber());
                seatText.setText(String.valueOf(vehicle.getSeatNumber()));

                Rating rating = ((Driver)user).getRating();
                positive.setText(rating.getPosPercent(rating.getPositive(), rating.getNegative()));
                negative.setText(rating.getNegPercent(rating.getPositive(), rating.getNegative()));

                numPositive = rating.getPositive();
                numNegative = rating.getNegative();
            }
        } else {
            Log.e(TAG, "Null rider passed to Profile Fragment.");
        }
    }
}
