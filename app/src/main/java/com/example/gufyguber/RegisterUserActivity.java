/*
 * RegisterUserActivity.java
 *
 * Version
 *
 * Last edit: dalton, 24/02/20 3:13 PM
 *
 * Copyright (c) CMPUT301W20T12 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
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
    EditText username;
    EditText email;
    EditText firstName;
    EditText lastName;
    EditText phone;
    EditText password;
    EditText confirmPassword;
    EditText make;
    EditText model;
    EditText plateNumber;
    EditText seatNumber;
    Button register;

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

        username = findViewById(R.id.user_name);
        email = findViewById(R.id.email);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        register = findViewById(R.id.register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(email.getText().toString(), password.getText().toString(), userType, db);
//                signIn(email.getText().toString(), password.getText().toString());
//                finish();
            }
        });
    }

    private void createAccount(String emailStr, String passwordStr, String userType, FirebaseFirestore db) {
        final CollectionReference users = db.collection("users");
        final CollectionReference usernames = db.collection("usernames");

        Log.d(TAG, "createAccount:" + emailStr);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
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
        data.put("email", email.getText().toString());

        usernames.document(username.getText().toString())
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
                });

        HashMap<String, String> userData = new HashMap<>();

        userData.put("username", username.getText().toString());
        userData.put("first_name", firstName.getText().toString());
        userData.put("last_name", lastName.getText().toString());
        userData.put("phone", phone.getText().toString());
        userData.put("userType", userType);

        users
                .document(email.getText().toString())
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
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUserActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm() {
        boolean valid = false;
        int validCounter = 0;
        HashMap<EditText, String> fields = new HashMap<>();

        fields.put(username, username.getText().toString());
        fields.put(email, email.getText().toString());
        fields.put(firstName, firstName.getText().toString());
        fields.put(lastName, lastName.getText().toString());
        fields.put(phone, phone.getText().toString());
        fields.put(password, password.getText().toString());
        fields.put(confirmPassword, confirmPassword.getText().toString());

        for (Map.Entry field : fields.entrySet()) {
            if (TextUtils.isEmpty((String) field.getValue())) {
                ((EditText) field.getKey()).setError("Required");
            } else {
                ((EditText) field.getKey()).setError(null);
                validCounter++;
            }
        }

        if (validCounter == 7) {
            valid = true;
        }

        return valid;
    }
}
