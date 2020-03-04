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
 * Last edit: dalton, 26/02/20 12:40 PM
 *
 * Version
 */

package com.example.gufyguber;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
//        final CollectionReference users = db.collection("users");
//        final CollectionReference usernames = db.collection("usernames");

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
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        phoneNumber = findViewById(R.id.phone);

        register = findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()) {
                    if(userType.equals("Rider")) {
//                        newUser = new Rider(username.getText().toString().toLowerCase(),
//                                email.getText().toString().toLowerCase(),
//                                firstName.getText().toString().toLowerCase(),
//                                lastName.getText().toString().toLowerCase(),
//                                phoneNumber.getText().toString());
//                    }else{
//                        newUser = new Driver(username.getText().toString().toLowerCase(),
//                                email.getText().toString().toLowerCase(),
//                                firstName.getText().toString().toLowerCase(),
//                                lastName.getText().toString().toLowerCase(),
//                                phoneNumber.getText().toString());
//                    }
//                    createAccount(newUser, password.getText().toString(), userType, db);

                }

            }
        });
    }

    private void createAccount(User newUser, String passwordString, String userType, FirebaseFirestore db) {
        final CollectionReference users = db.collection("users");
        final CollectionReference usernames = db.collection("usernames");

        Log.d(TAG, "createAccount:" + newUser.getEmail());

        mAuth.createUserWithEmailAndPassword(newUser.getEmail(), passwordString)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUserActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        HashMap<String, String> data = new HashMap<>();
        data.put("email", newUser.getEmail());

       /* usernames.document(newUser.getUsername())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Username addition successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Username addition failed" + e.toString());
                    }
                }); */

        HashMap<String, String> userData = new HashMap<>();

        userData.put("username", newUser.getUsername());
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
        fields.put(username, username.getText().toString());
        fields.put(email, email.getText().toString());
        fields.put(firstName, firstName.getText().toString());
        fields.put(lastName, lastName.getText().toString());
        fields.put(phoneNumber, phoneNumber.getText().toString());
        fields.put(password, password.getText().toString());
        fields.put(confirmPassword, confirmPassword.getText().toString());

        for (Map.Entry field : fields.entrySet()) {
            /* iterate over Map and check all fields are filled in*/
            if (TextUtils.isEmpty((String) field.getValue())) {
                ((EditText) field.getKey()).setError("Required");   /* if not filled in, flag */
            } else {
                ((EditText) field.getKey()).setError(null);
                validCounter++;                                     /* else, update counter */
            }
        }

        if (validCounter == 7) {
            /* if counter is 7, all fields filled in; check if password confirmed */
            if (fields.get(password).equals(fields.get(confirmPassword))) {
                validCounter++;         /* if fields are the same, password is OK */
            } else {                    /* otherwise, notify user */
                password.setError("Passwords do not match.");
                confirmPassword.setError("Passwords do not match.");
                password.setText("");
                confirmPassword.setText("");
            }
        }


        if (validCounter == 8) {
            /* if all 8 checks pass, the register form is valid */
            valid = true;
        }

        return valid;
    }
}
