/*
 *    Copyright (c) 2020. Gufy Guber
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    Adapted from tutorial:
 *    Prudhvi Raj Kumar. "QRCode Scanner - Google Vision API." Online video clip. Youtube.
 *    Youtube, 2 Jan. 2017. Web. 18 Feb. 2020.
 */

package com.example.gufyguber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.protobuf.StringValue;

import java.io.IOException;

/**
 * Builds camera and stores the data of the scanned QR code.
 * @author kenzbauer
 *
 * External resource used is:
 *
 *      Prudhvi Raj Kumar. "QRCode Scanner - Google Vision API." Online video clip. Youtube. Youtube, 2 Jan. 2017. Web. 18 Feb. 2020.
 *
 */
public class Scan extends AppCompatActivity {
    SurfaceView cameraView;
    BarcodeDetector barcode;
    CameraSource cameraSource;
    SurfaceHolder holder;
    String userID;
    String transaction;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public static final String TRANSACTION_COLLECTION = "transactions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_scan);
        cameraView = findViewById(R.id.cameraView);
        cameraView.setZOrderMediaOverlay(true);
        holder = cameraView.getHolder();

        barcode = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if(!barcode.isOperational()) {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            this.finish();
        }
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        RideRequest currentRequest = OfflineCache.getReference().retrieveCurrentRideRequest();
        userID = mAuth.getCurrentUser().getUid();


        cameraSource = new CameraSource.Builder(this, barcode)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1920, 1024)
                .build();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if(ContextCompat.checkSelfPermission(Scan.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(cameraView.getHolder());
                    }
                }
                catch (IOException issue) {
                    issue.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        barcode.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            /**
             * Takes in detections and put the value into the original intent to be displayed.
             * @param detections
             */
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    db.collection(TRANSACTION_COLLECTION).document(userID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // not sure how to add here
                                //db.collection(TRANSACTION_COLLECTION).document(userID).update(barcode)
                            }
                        }
                    });
                    Intent intent = new Intent();
                    intent.putExtra("barcode", barcodes.valueAt(0));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Override action bar to make the home button call onBackPressed()
    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //https://stackoverflow.com/questions/36457564/display-back-button-of-action-bar-is-not-going-back-in-android/36457747
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
