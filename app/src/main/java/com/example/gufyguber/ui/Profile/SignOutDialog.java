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
 * SignOutDialog.java
 *
 * Last edit: Robert, 11/03/20 12:48 PM
 *
 * Version
 */

package com.example.gufyguber.ui.Profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.R;
import com.example.gufyguber.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class SignOutDialog extends DialogFragment {

    private Button noBtn;
    private Button yesBtn;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.sign_out_dialog, null);

        noBtn = view.findViewById(R.id.cancel_rider_no_btn);
        yesBtn = view.findViewById(R.id.cancel_ride_yes_btn);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Configure Google sign-in to request the user's ID, email address,
                GoogleSignInOptions gso = new
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        // Pass server's client ID to requestIdToken method
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                final Activity tempActivity = getActivity();
                // Build a GoogleSignInClient with the options specified by gso.
                GoogleSignInClient signInClient = GoogleSignIn.getClient(tempActivity, gso);
                // Sign the user out of both firebase and the Google Client
                FirebaseAuth.getInstance().signOut();
                signInClient.signOut()
                        .addOnCompleteListener(tempActivity, new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        // update the UI to not signed in
                                        tempActivity.finish();
                                    }
                                });
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view);

        return builder.create();
    }

}