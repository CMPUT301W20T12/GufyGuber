/*
 * ProfileFragment.java
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

package com.example.gufyguber.ui.Profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.R;
import com.example.gufyguber.Rider;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private ProfileViewModel profileViewModel;
    private TextView nameText;
    private TextView phoneText;
    private TextView emailText;
    private Button editProfile;
    private Button saveProfile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        //final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);

            }
        });
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameText = view.findViewById(R.id.rider_name);
        emailText = view.findViewById(R.id.rider_email);
        phoneText = view.findViewById(R.id.rider_phone);
        editProfile = view.findViewById(R.id.edit_profile_button);
        saveProfile = view.findViewById(R.id.save_profile_button);

        FirebaseManager.getReference().fetchRiderInfo(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<Rider>() {
            @Override
            public void returnValue(Rider value) {
                if (value != null) {
                    nameText.setText(String.format("%s %s", value.getFirstName(), value.getLastName()));
                    emailText.setText(value.getEmail());
                    phoneText.setText(value.getPhoneNumber());
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
            }
        });

        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()) {
                    FirebaseManager.getReference().storeRiderInfo(new
                            Rider(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            emailText.getText().toString(),
                            nameText.getText().toString().split(" ")[0],
                            nameText.getText().toString().split(" ")[1],
                            phoneText.getText().toString()));
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