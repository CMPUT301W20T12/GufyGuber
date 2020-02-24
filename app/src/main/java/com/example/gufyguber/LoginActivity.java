/*
 * LoginActivity.java
 *
 * Version 1
 *
 * Last edit: dalton, 24/02/20 3:12 PM
 *
 * Copyright (c) CMPUT301W20T12, 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 */

package com.example.gufyguber;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * App activity displayed when the app first launches. Allows user to enter email and password to
 * sign in, register a new account. If the user requests to make a new account, they will be given
 * an option to register a RIDER ar DRIVER account via a dialog fragment.
 *
 * @see UserTypeFragment
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button loginButton = findViewById(R.id.login);
        final Button registerButton = findViewById(R.id.register);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UserTypeFragment().show(getSupportFragmentManager(), "USER_TYPE");
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

    }
}
