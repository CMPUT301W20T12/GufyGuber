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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirebaseManager {

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
     * @return A RideRequest, or null if no RideRequest was found
     */
    public RideRequest fetchRideRequest(String riderUID) {
        //TODO: Retrieve a RideRequest from Firebase based on the requester's UID
        return null;
    }

    /**
     * Gets all pending ride requests (requests without an assigned driver) from our cloud Firestore
     * @return A collection of all pending ride requests from our cloud Firestore
     */
    public List<RideRequest> fetchPendingRideRequests() {
        //TODO: Retrieve all pending ride requests from Firebase
        return null;
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
     * @return A Rider, or null if no rider with that UID was found.
     */
    public Rider fetchRiderInfo(String riderUID) {
        //TODO: Fetch information about a rider using their UID
        return null;
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
     * @return A Driver, or null if no driver with that UID was found
     */
    public Driver fetchDriverInfo(String driverUID) {
        //TODO: Fetch information about a driver using their UID
        return null;
    }
}
