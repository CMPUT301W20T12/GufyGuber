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
 * RegisterUserActivity.java
 *
 * Last edit: dalton, 03/03/20 8:56 PM
 *
 * Version 2
 */

package com.example.gufyguber;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

/**
 * App activity to allow the user to register a new account. Depending on what account type the user
 * selected via the user type dialog fragment, the activity will be set up with a different layout.
 */
public class RegisterUserActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
    private TextView email;
    private EditText firstName;
    private EditText lastName;
    private EditText phoneNumber;
    private EditText make;
    private EditText model;
    private EditText plateNumber;
    private EditText seatNumber;
    private Button register;
    private User newUser;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        final String userType = intent.getStringExtra("userType");
        if (userType.equals("Rider")){
            setContentView(R.layout.register_rider);
        }
        else{
            setContentView(R.layout.register_driver);
            make = findViewById(R.id.make);
            model = findViewById(R.id.model);
            plateNumber = findViewById(R.id.plate_number);
            seatNumber = findViewById(R.id.seat_number);
        }
        email = findViewById(R.id.email);
        email.setText(intent.getStringExtra("email"));
        firstName = findViewById(R.id.first_name);
        firstName.setText(intent.getStringExtra("firstName"));
        lastName = findViewById(R.id.last_name);
        lastName.setText(intent.getStringExtra("lastName"));
        phoneNumber = findViewById(R.id.phone_number);
        register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()) {
                    if(userType.equals("Rider")) {
                        newUser = new Rider(email.getText().toString().toLowerCase(),
                                firstName.getText().toString().toLowerCase(),
                                lastName.getText().toString().toLowerCase(),
                                phoneNumber.getText().toString());
                    }else{
                        if(validateVehicleInfo()) {
                            newUser = new Driver(email.getText().toString().toLowerCase(),
                                    firstName.getText().toString().toLowerCase(),
                                    lastName.getText().toString().toLowerCase(),
                                    phoneNumber.getText().toString());

                        }
                    }
                    createAccount(newUser, userType, db);
                }
            }
        });
    }

    private void createAccount(User newUser, String userType, FirebaseFirestore db) {
        final CollectionReference users = db.collection("users");

        Log.d(TAG, "createAccount:" + newUser.getEmail());

        HashMap<String, String> data = new HashMap<>();
        data.put("email", newUser.getEmail());

        HashMap<String, String> userData = new HashMap<>();

        userData.put("first_name", newUser.getFirstName());
        userData.put("last_name", newUser.getLastName());
        userData.put("phone", newUser.getPhoneNumber());
        userData.put("userType", userType);

        users.document(newUser.getEmail())
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User addition successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "User addition failed" + e.toString());
                    }
                });
        finish();
    }

    private boolean validateForm() {
        boolean valid = false;          // return boolean
        int validCounter = 0;           // counter to check all needs of form are satisfied
        HashMap<EditText, String> fields = new HashMap<>();

        /* populate HashMap of fields with EditText and user inputted values */
        fields.put(firstName, firstName.getText().toString());
        fields.put(lastName, lastName.getText().toString());
        fields.put(phoneNumber, phoneNumber.getText().toString());

        for (Map.Entry field : fields.entrySet()) {
            /* iterate over Map and check all fields are filled in*/
            if (TextUtils.isEmpty((String) field.getValue())) {
                ((EditText) field.getKey()).setError("Required");   /* if not filled in, flag */
            } else {
                ((EditText) field.getKey()).setError(null);
                validCounter++;                                     /* else, update counter */
            }
        }
        if (validCounter == 3) {
            /* if all 8 checks pass, the register form is valid */
            valid = true;
        }

        return valid;
    }

    private boolean validateVehicleInfo() {
        boolean valid = false;
        int validCounter = 0;

        HashMap<EditText, String> fields = new HashMap<>();

        /* populate HashMap of fields with EditText and user inputted values */
        fields.put(make, make.getText().toString());
        fields.put(model, model.getText().toString());
        fields.put(plateNumber, plateNumber.getText().toString());
        fields.put(seatNumber, seatNumber.getText().toString());

        for (Map.Entry field : fields.entrySet()) {
            if (TextUtils.isEmpty((String) field.getValue())) {
                ((EditText) field.getKey()).setError("Required");
            } else {
                ((EditText) field.getKey()).setError(null);
                validCounter++;
            }
        }
        if (validCounter == 4) {
            valid = true;
        }
        return valid;

    }
}
