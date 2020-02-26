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
 * UserTypeFragment.java
 *
 * Last edit: dalton, 26/02/20 12:39 PM
 *
 * Version 1.0
 */

package com.example.gufyguber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Builds a DialogFragment with two buttons for user to choose what type of account to create:
 * RIDER or DRIVER. Fragment is displayed on top of the LoginActivity.java screen.
 *
 * @see LoginActivity
 */
public class UserTypeFragment extends DialogFragment {

    private Button riderButton;
    private Button driverButton;
    private String userType;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.user_type_fragment,
                null);
        riderButton = view.findViewById(R.id.rider_button);
        driverButton = view.findViewById(R.id.driver_button);

        riderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                userType = "Rider";
                Intent intent  = new Intent(getActivity(), RegisterUserActivity.class);
                intent.putExtra("userType", userType);
                startActivity(intent);
                dismiss();
            }
        });

        driverButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                userType = "Driver";
                Intent intent  = new Intent(getActivity(), RegisterUserActivity.class);
                intent.putExtra("userType", userType);
                startActivity(intent);
                dismiss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .create();
    }
}
