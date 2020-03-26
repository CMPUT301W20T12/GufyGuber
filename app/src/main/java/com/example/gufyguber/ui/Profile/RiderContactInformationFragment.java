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
 * RiderContactInformationFragment.java
 *
 * Last edit: homie, 25/03/20 6:19 PM
 *
 * Version
 */

package com.example.gufyguber.ui.Profile;

//https://developer.android.com/guide/components/intents-common#Phone
//https://developer.android.com/guide/components/intents-common#ComposeEmail

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.GlobalDoubleClickHandler;
import com.example.gufyguber.R;

/**
 * Displays user contact information for another user to view.
 * Handles directing to and pre-dialing the users default phone app
 *
 * @author Harrison Peters
 */

public class RiderContactInformationFragment extends DialogFragment {
    private ImageButton contactEmail;
    private ImageButton contactPhone;
    private String email;
    private String phoneNumber;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_rider_contact_info, null);
        Bundle bundle = this.getArguments();

        email = bundle.getString("email");
        phoneNumber = bundle.getString("phone");

        contactEmail = view.findViewById(R.id.contact_email);
        contactPhone = view.findViewById(R.id.contact_phone);

        contactPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        contactEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                String[] recipient = {email};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, recipient);
                intent.putExtra(Intent.EXTRA_SUBJECT, "GufyGuber Ride Request Inquiry");
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .create();
    }
}
