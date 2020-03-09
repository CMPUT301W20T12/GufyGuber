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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.sql.Time;
import java.util.Date;
import java.util.HashMap;
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

    // User Keys
    public static final String EMAIL_KEY = "email";
    public static final String FIRST_NAME_KEY = "first_name";
    public static final String LAST_NAME_KEY = "last_name";
    public static final String PHONE_NUMBER_KEY = "phone";

    // Ride Request Keys
    public static final String RIDE_REQUEST_COLLECTION = "ride_requests";
    public static final String STATUS_KEY = "status";
    public static final String DRIVER_KEY = "driver_uid";
    public static final String FARE_KEY = "fare";
    public static final String PICKUP_KEY = "pickup";
    public static final String DROPOFF_KEY = "dropoff";
    public static final String REQUEST_OPEN_TIME_KEY = "request_start";
    public static final String REQUEST_ACCEPTED_TIME_KEY = "request_accepted";
    public static final String REQUEST_CLOSED_TIME_KEY = "request_closed";

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
     * @param rideRequest The valid RideRequest object to store using Firebase
     */
    public void storeRideRequest(RideRequest rideRequest) {
        CollectionReference requestDoc = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION);

        Log.d(TAG, String.format("Storing Ride Request for user [%s]", rideRequest.getRiderUID()));

        HashMap<String, Object> data = new HashMap<>();
        data.put(DRIVER_KEY, rideRequest.getDriverUID());
        data.put(STATUS_KEY, rideRequest.getStatus().name());
        data.put(FARE_KEY, rideRequest.getOfferedFare());
        data.put(PICKUP_KEY, rideRequest.getLocationInfo().pickupToGeoPoint());
        data.put(DROPOFF_KEY, rideRequest.getLocationInfo().dropoffToGeoPoint());
        data.put(REQUEST_OPEN_TIME_KEY, rideRequest.getTimeInfo().getRequestOpenTime());
        data.put(REQUEST_ACCEPTED_TIME_KEY, rideRequest.getTimeInfo().getRequestAcceptedTime());
        data.put(REQUEST_CLOSED_TIME_KEY, rideRequest.getTimeInfo().getRequestClosedTime());

        requestDoc.document(rideRequest.getRiderUID())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Ride request addition successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Ride request addition failed" + e.toString());
                    }
                });
    }

    /**
     * Retrieves a RideRequest from our cloud Firestore
     * @param riderUID The user ID of the rider who created the RideRequest
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchRideRequest(final String riderUID, final ReturnValueListener<RideRequest> returnFunction) {
        DocumentReference requestDoc = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION).document(riderUID);
        requestDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {

                        String driverUID = snapshot.getString(DRIVER_KEY);
                        RideRequest.Status rideStatus = RideRequest.Status.valueOf(snapshot.getString(STATUS_KEY));
                        float fare = snapshot.getDouble(FARE_KEY).floatValue();
                        GeoPoint pickup = snapshot.getGeoPoint(PICKUP_KEY);
                        GeoPoint dropoff = snapshot.getGeoPoint(DROPOFF_KEY);
                        LocationInfo locationInfo = new LocationInfo(pickup, dropoff);
                        Timestamp requestOpenTime = snapshot.getTimestamp(REQUEST_OPEN_TIME_KEY);
                        Date requestOpenDate = requestOpenTime == null ? null : requestOpenTime.toDate();
                        Timestamp requestAcceptedTime = snapshot.getTimestamp(REQUEST_ACCEPTED_TIME_KEY);
                        Date requestAcceptedDate = requestAcceptedTime == null ? null : requestAcceptedTime.toDate();
                        Timestamp requestClosedTime = snapshot.getTimestamp(REQUEST_CLOSED_TIME_KEY);
                        Date requestClosedDate = requestClosedTime == null ? null : requestClosedTime.toDate();
                        TimeInfo timeInfo = new TimeInfo(requestOpenDate, requestAcceptedDate, requestClosedDate);

                        returnFunction.returnValue(new RideRequest(riderUID, driverUID, rideStatus, fare, locationInfo, timeInfo));
                    } else {
                        Log.e(TAG, String.format("Ride request info for [%s] not found on Firestore.", riderUID));
                        returnFunction.returnValue(null);
                    }
                } else {
                    Log.e(TAG, "Fetching ride request info failed. Issue communicating with Firestore.");
                    returnFunction.returnValue(null);
                }
            }
        });
        returnFunction.returnValue(null);
    }

    public void deleteRideRequest(final String riderUID) {
        DocumentReference requestDoc = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION).document(riderUID);
        requestDoc.delete();
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
        CollectionReference usersDoc = FirebaseFirestore.getInstance().collection("users");

        Log.d(TAG, "createAccount:" + rider.getUID() + " - " + rider.getEmail());
        HashMap<String, String> data = new HashMap<>();
        data.put("UID", rider.getUID());
        HashMap<String, String> userData = new HashMap<>();
        userData.put("email", rider.getEmail());
        userData.put("first_name", rider.getFirstName());
        userData.put("last_name", rider.getLastName());
        userData.put("phone", rider.getPhoneNumber());
        userData.put("userType", "rider");

        usersDoc.document(rider.getUID())
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

    /**
     * Retrieves Rider information from our cloud Firestore
     * @param riderUID The user ID of the rider to retrieve a profile for
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchRiderInfo(final String riderUID, final ReturnValueListener<Rider> returnFunction) {
        DocumentReference riderDoc = FirebaseFirestore.getInstance().collection("users").document(riderUID);
        riderDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Map<String, Object> docData = snapshot.getData();
                        String email = String.valueOf(docData.get(EMAIL_KEY));
                        String firstName= String.valueOf(docData.get(FIRST_NAME_KEY));
                        String lastName = String.valueOf(docData.get(LAST_NAME_KEY));
                        String phoneNumber = String.valueOf(docData.get(PHONE_NUMBER_KEY));
                        returnFunction.returnValue(new Rider(riderUID, email, firstName, lastName, phoneNumber));
                    } else {
                        Log.w(TAG, String.format("Rider info for %s not found on Firestore.", riderUID));
                        returnFunction.returnValue(null);
                    }
                } else {
                    Log.e(TAG, "Fetching rider info failed. Issue communicating with Firestore.");
                    returnFunction.returnValue(null);
                }
            }
        });
    }

    /**
     * Adds or updates a Driver record in our cloud Firestore
     * @param driver The Driver to store in our cloud Firestore.
     */
    public void storeDriverInfo(Driver driver) {
        CollectionReference usersDoc = FirebaseFirestore.getInstance().collection("users");

        Log.d(TAG, "createAccount:" + driver.getUID() + " - " + driver.getEmail());
        HashMap<String, String> data = new HashMap<>();
        data.put("UID", driver.getUID());
        HashMap<String, String> userData = new HashMap<>();
        userData.put("email", driver.getEmail());
        userData.put("first_name", driver.getFirstName());
        userData.put("last_name", driver.getLastName());
        userData.put("phone", driver.getPhoneNumber());
        userData.put("userType", "driver");

        usersDoc.document(driver.getUID())
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

    /**
     * Retrieves Driver information from our cloud Firestore
     * @param driverUID The user ID of the driver to retrieve a profile for
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchDriverInfo(final String driverUID, ReturnValueListener<Driver> returnFunction) {
        //TODO: Fetch information about a driver using their UID
        returnFunction.returnValue(null);
    }

    /**
     * Adds or updates a Driver's vehicle in our cloud Firestore
     * @param driver
     *  This is the driver who is registering the vehicle
     */
    public void storeVehicleInfo(Driver driver) {
        CollectionReference vehicles = FirebaseFirestore.getInstance().collection("vehicles");
        HashMap<String, String> vehicleData = new HashMap<>();

        vehicleData.put("make", driver.getVehicle().getMake());
        vehicleData.put("model", driver.getVehicle().getModel());
        vehicleData.put("seat_number", Integer.toString(driver.getVehicle().getSeatNumber()));
        vehicleData.put("plate_number", driver.getVehicle().getPlateNumber());
        vehicles.document(driver.getUID())
                .set(vehicleData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Vehicle addition successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Vehicle addition failed" + e.toString());
                    }
                });
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

    public void checkUser(String UID, final ReturnValueListener<Boolean> returnFunction) {

        final DocumentReference userDoc = FirebaseFirestore.getInstance().collection("users").document(UID);
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        // if email doc exists, user is registered as driver/rider and can be directed to next activity
                        Log.d(TAG, "Document exists");
                        returnFunction.returnValue(Boolean.TRUE);
                    } else {
                        // if email doc not found, user may be authorized, but is still ont registered to use the app
                        // fragment will be displayed allowing them to select user type, and finish signing up
                        Log.d(TAG, "Document does not exist");
                        returnFunction.returnValue(Boolean.FALSE);
                    }
                }
            }
        });
    }
}
