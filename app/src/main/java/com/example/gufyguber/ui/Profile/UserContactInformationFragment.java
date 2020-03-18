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

public class UserContactInformationFragment extends DialogFragment {
    private TextView contactEmail;
    private TextView contactPhone;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_contact_info, null);

        contactEmail = view.findViewById(R.id.contact_email);
        contactPhone = view.findViewById(R.id.contact_phone);

        contactEmail.setText("hmp@ualberta.ca");
        contactPhone.setText("7809654819");

        Paint paint = new Paint();
        paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
        contactPhone.setPaintFlags(paint.getFlags()); //https://stackoverflow.com/questions/8033316/to-draw-an-underline-below-the-textview-in-android/43757835

        contactPhone.setTextColor(Color.BLUE);
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


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .setTitle("Contact")
                .create();
    }
}
