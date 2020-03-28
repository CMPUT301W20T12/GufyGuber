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
 * GenerateQR.java
 *
 * Last edit: kenzbauer, 09/03/20 7:03 PM
 *
 * Version
 */

package com.example.gufyguber;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.ListenerRegistration;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * This activity encodes a message and stores it as a Bitmap to be shown to the user.
 *
 * Right now the message is set to an arbitrary value, but it will be set to the rider's username
 *      and the cost of the ride.
 */

public class GenerateQR extends AppCompatActivity implements FirebaseManager.RideRequestListener {
    private static final String TAG = "GenerateQR";

    BitMatrix matrix;
    Bitmap map;
    String codeMessage;
    ImageView qrCode;
    TextView qrMessage;
    ListenerRegistration rideRequestListener;
    Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_qr);
        rideRequestListener = FirebaseManager.getReference().listenToRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        qrCode = findViewById(R.id.qrCode);
        qrMessage = findViewById(R.id.showMessage);

        //code message will equal the user's email and the amount owed to the driver
        RideRequest currentRequest = OfflineCache.getReference().retrieveCurrentRideRequest();
        if (currentRequest == null) {
            codeMessage = "Trial!!!";
        } else {
            User user = OfflineCache.getReference().retrieveCurrentUser();
            if (user != null && user instanceof Rider) {
                codeMessage = String.format("%s %s owes $%.2f", user.getFirstName(), user.getLastName(), currentRequest.getOfferedFare());
            } else {
                codeMessage = String.format("You are owed $%.2f", currentRequest.getOfferedFare());
            }
        }

        qrMessage.setText(codeMessage);

        MultiFormatWriter multi = new MultiFormatWriter();
        try {
            matrix = multi.encode(codeMessage, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            map = barcodeEncoder.createBitmap(matrix);
            qrCode.setImageBitmap(map);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        //button to begin rating driver
        next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rateDriver = new Intent(getApplicationContext(), RateDriver.class);
                startActivity(rateDriver);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(rideRequestListener != null) {
            rideRequestListener.remove();
            rideRequestListener = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRideRequestUpdated(RideRequest updatedRequest) {
        if(updatedRequest != null && updatedRequest.getStatus() == RideRequest.Status.COMPLETED){
            onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //https://stackoverflow.com/questions/36457564/display-back-button-of-action-bar-is-not-going-back-in-android/36457747
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
