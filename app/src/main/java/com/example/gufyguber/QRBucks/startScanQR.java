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
 * startScanQR.java
 *
 * Last edit: scott, 02/04/20 5:57 PM
 *
 * Version
 */

package com.example.gufyguber.QRBucks;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gufyguber.Models.Wallet;
import com.example.gufyguber.R;
import com.example.gufyguber.Singletons.FirebaseManager;
import com.example.gufyguber.Singletons.OfflineCache;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * This activity includes accessing the camera to scan a QR code.
 *  It also shows the result of the QR code scan.
 *
 * @author kenzbauer
 */

public class startScanQR extends AppCompatActivity {
    TextView result;

    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;

    private static final String TAG = "startScanQR";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        result = findViewById(R.id.result);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously*
                new CameraRationaleFragment().show(getSupportFragmentManager(), "camera_rationale");
            }else {
                // request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
            }
        } else{
            // permission already granted
            // proceed to receive payment
            Intent intent = new Intent(startScanQR.this, Scan.class);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    // proceed to receive payment
                    Intent intent = new Intent(startScanQR.this, Scan.class);
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    // permission is denied
                    // return back to navigation activity
                    finish();
                }
            }
        }
    }

    /**
     * Once qr is scanned, this allows the result to be shown to the user.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                final Barcode barcode = data.getParcelableExtra("barcode");
                result.post(new Runnable() {
                    @Override
                    public void run() {
                        result.setText(barcode.displayValue);
                        // when the QR code is scanned, ride status will be updated to COMPLETE
                        FirebaseManager.getReference().completeRide(OfflineCache.getReference().retrieveCurrentRideRequest(), new FirebaseManager.ReturnValueListener<Boolean>() {
                            @Override
                            public void returnValue(Boolean value) {
                                if (value == null) {
                                    Log.e(TAG, "Setting ride request to complete failed.");
                                } else {    // If status successfully updates, store the transaction in the Drive's wallet
                                    FirebaseManager.getReference().storeWalletInfo(OfflineCache.getReference().retrieveCurrentUser().getUID(), new Wallet(barcode.displayValue), new FirebaseManager.ReturnValueListener<Boolean>() {
                                        @Override
                                        public void returnValue(Boolean value) {
                                            if (value) {
                                                finish();
                                            } else {
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                });
            }
        }
    }
}
