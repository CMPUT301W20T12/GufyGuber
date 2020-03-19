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
 * UserContactInformation.java
 *
 * Last edit: hmp, 18/03/20 4:39 PM
 *
 * Version
 */

package com.example.gufyguber.ui.Profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.R;


//https://developer.android.com/guide/components/intents-common#Phone
//https://developer.android.com/guide/components/intents-common#ComposeEmail

/**
 * Displays user contact information for another user to view.
 * Handles directing to and pre-dialing the users default phone app
 *
 * @author Harrison Peters
 */

public class UserContactInformationFragment extends DialogFragment {
    private TextView contactEmail;
    private TextView contactPhone;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_contact_info, null);

        contactEmail = view.findViewById(R.id.contact_email);
        contactPhone = view.findViewById(R.id.contact_phone);

        contactEmail.setText("example@gufyguber.ca");
        contactPhone.setText("12345678901");

        Paint paint = new Paint();
        paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
        contactPhone.setPaintFlags(paint.getFlags()); //https://stackoverflow.com/questions/8033316/to-draw-an-underline-below-the-textview-in-android/43757835
        contactEmail.setPaintFlags(paint.getFlags());

        contactPhone.setTextColor(Color.BLUE);
        contactEmail.setTextColor(Color.BLUE);

        contactPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + contactPhone.getText().toString()));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    }
            }
        });

        contactEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String [] recipient = {"example@gufyguber.ca"};
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
