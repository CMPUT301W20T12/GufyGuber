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
 * FirebaseManager.java
 *
 * Last edit: , 07/03/20 11:29 PM
 *
 * Version
 */

// Name: Robert MacGillivray
// File: FirebaseManager.java
// Date: Mar.07.2020
// Purpose: Simple manager for dealing with common Firebase interactions in a consistent way

package com.example.gufyguber;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class FirebaseManager {
    /**
     * Interface that allows us to return values asynchronously when loaded from our cloud Firestore
     * @param <T> The return type of the value you're trying to fetch
     */
    public interface ReturnValueListener<T>{
        public void returnValue(T value);
    }

    // Singleton Pattern for this manager
    private static FirebaseManager reference;
    public static FirebaseManager getReference() {
        // Auto-construct single instance if it hasn't been constructed yet
        if (reference == null) {
            reference = new FirebaseManager();
        }
        return reference;
    }

    // Tag for logging purposes
    private static final String TAG = "FirebaseManager";

    // Firestore keys
    public static final String FIRST_NAME_KEY = "first_name";
    public static final String LAST_NAME_KEY = "last_name";
    public static final String PHONE_NUMBER_KEY = "phone";

    /**
     * FireBaseManager's construction should be private since it's supposed to be a singleton
     */
    private FirebaseManager() {
        if (reference != null) {
            Log.e(TAG, "There can only be one FirebaseManager.");
        }
    }

    /**
     * @param context Context we'll use to access the system service that'll tell us about our current network
     * @return true if connected to the internet, false otherwise
     */
    public boolean isOnline(@NonNull Context context) {
        NetworkInfo netInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Stores a RideRequest in our cloud Firestore
     * @param rideRequest The RideRequest object to store using Firebase
     */
    public void storeRideRequest(RideRequest rideRequest) {
        //TODO: Store a rideRequest on Firebase, or update the details of an existing request
    }

    /**
     * Retrieves a RideRequest from our cloud Firestore
     * @param riderUID The user ID of the rider who created the RideRequest
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchRideRequest(final String riderUID, final ReturnValueListener<RideRequest> returnFunction) {
        //TODO: Retrieve a RideRequest from Firebase based on the requester's UID
        returnFunction.returnValue(null);
    }

    /**
     * Gets all pending ride requests (requests without an assigned driver) from our cloud Firestore
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchPendingRideRequests(final ReturnValueListener<List<RideRequest>> returnFunction) {
        //TODO: Retrieve all pending ride requests from Firebase
        returnFunction.returnValue(null);
    }

    /**
     * Adds or updates a Rider record in our cloud Firestore
     * @param rider The Rider to store in our cloud Firestore
     */
    public void storeRiderInfo(Rider rider) {
        //TODO: Store a new Rider in Firebase, or update an existing Rider
    }

    /**
     * Retrieves Rider information from our cloud Firestore
     * @param riderUID The user ID of the rider to retrieve a profile for
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchRiderInfo(final String riderUID, final ReturnValueListener<Rider> returnFunction) {
        //TODO: Fetch information about a rider using their UID instead of their email
        DocumentReference riderDoc = FirebaseFirestore.getInstance().collection("users").document(riderUID);
        riderDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Map<String, Object> docData = snapshot.getData();
                        String email = riderUID;
                        String firstName= String.valueOf(docData.get(FIRST_NAME_KEY));
                        String lastName = String.valueOf(docData.get(LAST_NAME_KEY));
                        String phoneNumber = String.valueOf(docData.get(PHONE_NUMBER_KEY));
                        returnFunction.returnValue(new Rider(email, firstName, lastName, phoneNumber));
                    }
                }
                Log.e("TAG", "Fetching rider info failed.");
                returnFunction.returnValue(null);
            }
        });
    }

    /**
     * Adds or updates a Driver record in our cloud Firestore
     * @param driver The Driver to store in our cloud Firestore.
     */
    public void storeDriverInfo(Driver driver) {
        //TODO: Store a new Driver in Firebase, or update an existing Driver
    }

    /**
     * Retrieves Driver information from our cloud Firestore
     * @param driverUID The user ID of the driver to retrieve a profile for
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchDriverInfo(final String driverUID, ReturnValueListener<Driver> returnFunction) {
        //TODO: Fetch information about a driver using their UID
        returnFunction.returnValue(null);
    }

    public void storeVehicleInfo(Vehicle vehicle) {
        //TODO: Store a new Vehicle in Firebase, or update an existing vehicle
    }

    /**
     * Retrieves Vehicle information from our cloud Firestore
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchVehicleInfo(ReturnValueListener<Vehicle> returnFunction) {
        //TODO: Decide what constitutes a key for getting vehicle info (especially if drivers have > 1 vehicle
        //TODO: Fetch information about a vehicle
        returnFunction.returnValue(null);
    }
}
