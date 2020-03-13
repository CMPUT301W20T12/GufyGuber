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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

public class GenerateQR extends AppCompatActivity {
    BitMatrix matrix;
    Bitmap map;
    String codeMessage;
    ImageView qrCode;
    TextView qrMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_qr);

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
    }

}
