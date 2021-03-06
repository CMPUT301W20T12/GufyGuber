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
 * RateDriver.java
 *
 * Last edit: scott, 02/04/20 5:57 PM
 *
 * Version
 */

package com.example.gufyguber.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gufyguber.Models.Rating;
import com.example.gufyguber.Models.RideRequest;
import com.example.gufyguber.NavigationActivity;
import com.example.gufyguber.R;
import com.example.gufyguber.Singletons.FirebaseManager;
import com.example.gufyguber.Singletons.OfflineCache;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This activity allows the Rider to rate the Driver
 * @author kenzbauer
 */

public class RateDriver extends AppCompatActivity {
    ImageButton thumbsUp;
    ImageButton thumbsDown;
    String userID;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public static final String RATING_COLLECTION = "ratings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_driver);

        mAuth = FirebaseAuth.getInstance();

        //for collection
        db = FirebaseFirestore.getInstance();

        //get id of driver
        RideRequest currentRequest = OfflineCache.getReference().retrieveCurrentRideRequest();
        if (currentRequest == null) {
            //sets userID to the Uid of the current user
            userID = mAuth.getCurrentUser().getUid();
        }
        else {
            userID = currentRequest.getDriverUID();
        }


        thumbsUp = findViewById(R.id.upp);
        thumbsUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection(RATING_COLLECTION).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            db.collection(RATING_COLLECTION).document(userID).update("positive", FieldValue.increment(1));
                        }
                        else {
                            Rating newRate = new Rating(1, 0);
                            FirebaseManager.getReference().storeRatingInfo(userID, newRate);
                        }
                    }
                });
                Intent intent = new Intent(RateDriver.this, NavigationActivity.class);
                startActivity(intent);
            }
        });

        thumbsDown = findViewById(R.id.down);
        thumbsDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection(RATING_COLLECTION).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            db.collection(RATING_COLLECTION).document(userID).update("negative", FieldValue.increment(1));
                        }
                        else {
                            Rating newRate = new Rating(0, 1);
                            FirebaseManager.getReference().storeRatingInfo(userID, newRate);
                        }
                    }
                });
                Intent intent = new Intent(RateDriver.this, NavigationActivity.class);
                startActivity(intent);
            }
        });

    }

}
