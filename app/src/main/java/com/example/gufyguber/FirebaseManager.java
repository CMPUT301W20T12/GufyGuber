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
 */

package com.example.gufyguber;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.io.BufferedOutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all Firebase Firestore connections. Utilizes a singleton pattern.
 * @author Robert MacGillivray | Mar.07.2020
 * @author Dalton Ronan
 */
public class FirebaseManager {
    /**
     * Interface that allows us to return values asynchronously when loaded from our cloud Firestore
     * @param <T> The return type of the value you're trying to fetch
     */
    public interface ReturnValueListener<T>{
        public void returnValue(T value);
    }

    public interface RideRequestListener{
        public void onRideRequestUpdated(RideRequest updatedRequest);
    }

    public interface DriverRideRequestCollectionListener{
        public void onRideRequestsUpdated(ArrayList<RideRequest> rideRequests);
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

    // True when instrument testing
    private boolean testMode;
    public void setTestMode(boolean testMode) { this.testMode = testMode; }

    // Firestore keys

    // User Keys
    public static final String USER_COLLECTION = "users";
    public static final String EMAIL_KEY = "email";
    public static final String FIRST_NAME_KEY = "first_name";
    public static final String LAST_NAME_KEY = "last_name";
    public static final String PHONE_NUMBER_KEY = "phone";
    public static final String USER_TYPE_KEY = "userType";
    public static final String USER_PROFILEPIC_KEY = "profile_pic";

    // Vehicle Keys
    public static final String VEHICLE_COLLECTION = "vehicles";
    public static final String MAKE_KEY = "make";
    public static final String MODEL_KEY = "model";
    public static final String SEAT_NUMBER_KEY = "seat_number";
    public static final String PLATE_NUMBER_KEY = "plate_number";

    //Rating Keys
    public static final String RATING_COLLECTION = "ratings";
    public static final String POSITIVE_KEY = "positive";
    public static final String NEGATIVE_KEY = "negative";

    // Ride Request Keys
    public static final String RIDE_REQUEST_COLLECTION = "ride_requests";
    public static final String DRIVER_ACCEPTED_REQUESTS = "driver_accepted_requests";
    public static final String STATUS_KEY = "status";
    public static final String DRIVER_KEY = "driver_uid";
    public static final String FARE_KEY = "fare";
    public static final String PICKUP_KEY = "pickup";
    public static final String DROPOFF_KEY = "dropoff";
    public static final String REQUEST_OPEN_TIME_KEY = "request_start";
    public static final String REQUEST_ACCEPTED_TIME_KEY = "request_accepted";
    public static final String REQUEST_CLOSED_TIME_KEY = "request_closed";

    //Wallet Keys
    public static final String TRANSACTION_COLLECTION = "transactions";
    public static final String TRANSACTION = "transaction_details";

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
        if (context != null && context.getSystemService(Context.CONNECTIVITY_SERVICE) != null) {
            NetworkInfo netInfo = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } else {
            return true;
        }
    }

    /**
     * Stores a RideRequest in our cloud Firestore
     * @param rideRequest The valid RideRequest object to store using Firebase
     */
    public void storeRideRequest(RideRequest rideRequest) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot store ride requests on Firestore in Test Mode");
            return;
        }

        CollectionReference requestDoc = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION);

        Log.d(TAG, String.format("Storing Ride Request:\n%s", rideRequest.toString()));

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
     * @param returnFunction The callback to use once we've finished retrieving a RideRequest
     */
    public void fetchRideRequest(final String riderUID, final ReturnValueListener<RideRequest> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot fetch ride requests from Firestore in Test Mode.");
            returnFunction.returnValue(null);
            return;
        }

        DocumentReference requestDoc = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION).document(riderUID);
        requestDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        returnFunction.returnValue(buildRideRequest(snapshot));
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
    }

    /**
     * Constructs a RideRequest object from a Firestore document.
     * @param document The Firestore document that represents this RideRequest
     * @return The document reconstructed as a RideRequest
     */
    private RideRequest buildRideRequest(DocumentSnapshot document) {
        String driverUID = document.getString(DRIVER_KEY);
        RideRequest.Status rideStatus = RideRequest.Status.valueOf(document.getString(STATUS_KEY));
        float fare = document.getDouble(FARE_KEY).floatValue();
        GeoPoint pickup = document.getGeoPoint(PICKUP_KEY);
        GeoPoint dropoff = document.getGeoPoint(DROPOFF_KEY);
        LocationInfo locationInfo = new LocationInfo(pickup, dropoff);
        Date requestOpenDate = document.getDate(REQUEST_OPEN_TIME_KEY);
        Date requestAcceptedDate = document.getDate(REQUEST_ACCEPTED_TIME_KEY);
        Date requestClosedDate = document.getDate(REQUEST_CLOSED_TIME_KEY);
        TimeInfo timeInfo = new TimeInfo(requestOpenDate, requestAcceptedDate, requestClosedDate);
        return new RideRequest(document.getId(), driverUID, rideStatus, fare, locationInfo, timeInfo);
    }

    /**
     * @param riderUID The key used to identify the ride request to listen to
     * @param onChangedListener Listener to notify when the ride request is updated on Firestore
     * @return A ListenerRegistration object that should be used to remove the listener when no longer needed
     */
    public ListenerRegistration listenToRideRequest(final String riderUID, final RideRequestListener onChangedListener) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot listen to Firestore in Test Mode.");
            return null;
        }

        DocumentReference requestDoc = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION).document(riderUID);
        return requestDoc.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, String.format("Listen to ride request [%s] failed", riderUID), e);
                    return;
                }

                // Return our value. Null if deleted.
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    onChangedListener.onRideRequestUpdated(buildRideRequest(documentSnapshot));
                } else {
                    onChangedListener.onRideRequestUpdated(null);
                }
            }
        });
    }

    /**
     * Allows a driver to accept a ride request if it hasn't already been accepted
     * @param driverUID The UID of the driver user that is attempting to accept this ride request
     * @return True if the request acceptance succeeded, false otherwise
     */
    public void driverAcceptRideRequest(final String driverUID, final RideRequest request, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if(testMode) {
            Log.e(TAG, "Cannot accept ride requests on Firestore in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

        fetchRideRequest(request.getRiderUID(), new ReturnValueListener<RideRequest>() {
            @Override
            public void returnValue(RideRequest value) {
                // Request was deleted before it could be accepted
                if (value == null) {
                    returnFunction.returnValue(false);
                    return;
                }

                // Request was accepted by another driver first
                if (value.getDriverUID() != null || value.getStatus() != RideRequest.Status.PENDING) {
                    Log.w(TAG, "Ride Request failed to be accepted.");
                    returnFunction.returnValue(false);
                    return;
                }

                // Can set the driverUID to null to cancel an accepted request, otherwise mark request accepted
                value.setDriverUID(driverUID);
                value.setStatus(RideRequest.Status.ACCEPTED);
                value.getTimeInfo().setRequestAcceptedTime();

                // Push the modifed request to Firebase since we've successfully claimed it
                storeRideRequest(value);
                OfflineCache.getReference().cacheCurrentRideRequest(value);
                returnFunction.returnValue(true);
            }
        });
    }

    /**
     * Cancels a ride request and deletes it from Firestore
     * @param request The ride request to cancel and delete from Firestore
     * @param returnFunction Returns true if successful, false otherwise
     */
    public void riderCancelRequest(RideRequest request, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.w(TAG, "Cannot properly delete ride request from Firestore in Test Mode.");
            OfflineCache.getReference().clearCurrentRideRequest();
            returnFunction.returnValue(true);
            return;
        }

        Log.d(TAG, String.format("Trying to cancel and delete Ride Request:\n", request.toString()));
        OfflineCache.getReference().clearCurrentRideRequest();
        DocumentReference requestDoc = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION).document(request.getRiderUID());
        requestDoc.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Ride request deleted successfully.");
                        returnFunction.returnValue(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error deleting ride request: ", e);
                        returnFunction.returnValue(false);
                    }
                });
    }

    /**
     *
     * @param riderUID
     * @param request
     * @param returnFunction
     */
    public void riderAcceptDriverOffer(String riderUID, RideRequest request, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot accept driver offers on Firestore while in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

        fetchRideRequest(riderUID, new ReturnValueListener<RideRequest>() {
            @Override
            public void returnValue(RideRequest value) {
                value.setStatus(RideRequest.Status.CONFIRMED);
                storeRideRequest(value);
                OfflineCache.getReference().cacheCurrentRideRequest(value);
                returnFunction.returnValue(true);
            }
        });
    }

    /**
     *
     * @param riderUID
     * @param request
     * @param returnFunction
     */
    public void riderDeclineDriverOffer(String riderUID, RideRequest request, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot decline ride offers on Firestore in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

        request.setDriverUID(null);
        request.setStatus(RideRequest.Status.PENDING);
        storeRideRequest(request);
        OfflineCache.getReference().cacheCurrentRideRequest(request);

        fetchRideRequest(riderUID, new ReturnValueListener<RideRequest>() {
            @Override
            public void returnValue(RideRequest value) {
                returnFunction.returnValue(value != null && value.getStatus() == RideRequest.Status.PENDING);
            }
        });
    }

    /**
     * Gets all pending ride requests (requests without an assigned driver) from our cloud Firestore
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchRideRequestsWithStatus(final RideRequest.Status status, final ReturnValueListener<ArrayList<RideRequest>> returnFunction) {
        // Alternative handler when in test mode
        if(testMode) {
            Log.e(TAG, "Cannot fetch collections of ride requests from Firestore in Test Mode.");
            returnFunction.returnValue(null);
            return;
        }

        FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION).whereEqualTo(STATUS_KEY, status.name()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (!snapshot.isEmpty()) {
                        ArrayList<RideRequest> rideRequests = new ArrayList<RideRequest>();
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            rideRequests.add(buildRideRequest(document));
                        }
                        returnFunction.returnValue(rideRequests);
                    } else {
                        Log.e(TAG, "No pending request info found on Firestore.");
                        returnFunction.returnValue(null);
                    }
                } else {
                    Log.e(TAG, "Fetching pending ride requests failed. Issue communicating with Firestore.");
                    returnFunction.returnValue(null);
                }
            }
        });
    }

    public ListenerRegistration listenToAllRideRequests(final DriverRideRequestCollectionListener onChangedListener) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot listen to Firestore in Test Mode.");
            return null;
        }

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(RIDE_REQUEST_COLLECTION);
        return collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen to ride request collection failed.", e);
                    return;
                }

                // Return the results of a new "Pending" query
                fetchRideRequestsWithStatus(RideRequest.Status.PENDING, new ReturnValueListener<ArrayList<RideRequest>>() {
                    @Override
                    public void returnValue(ArrayList<RideRequest> value) {
                        onChangedListener.onRideRequestsUpdated(value);
                    }
                });
            }
        });
    }

    /**
     * Adds or updates a Rider record in our cloud Firestore
     * @param rider The Rider to store in our cloud Firestore
     */
    public void storeRiderInfo(Rider rider) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot store rider info on Firestore in Test Mode.");
            return;
        }

        CollectionReference usersDoc = FirebaseFirestore.getInstance().collection(USER_COLLECTION);

        Log.d(TAG, "createAccount:" + rider.getUID() + " - " + rider.getEmail());

        HashMap<String, String> userData = new HashMap<>();
        userData.put(EMAIL_KEY, rider.getEmail());
        userData.put(FIRST_NAME_KEY, rider.getFirstName());
        userData.put(LAST_NAME_KEY, rider.getLastName());
        userData.put(PHONE_NUMBER_KEY, rider.getPhoneNumber());
        userData.put(USER_TYPE_KEY, "rider");

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
     * @param returnFunction The callback to use once we've finished retrieving a Rider
     */
    public void fetchRiderInfo(final String riderUID, final ReturnValueListener<Rider> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot fetch rider info from Firestore in Test Mode.");
            returnFunction.returnValue(null);
            return;
        }
        DocumentReference riderDoc = FirebaseFirestore.getInstance().collection(USER_COLLECTION).document(riderUID);
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
     * Deletes a user record from our cloud Firestore
     * @param riderUID The UID associated with the rider record to be deleted
     */
    public void deleteRiderInfo(final String riderUID) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot delete rider records from Firestore in Test Mode.");
            return;
        }

        // TODO: If we delete a rider, what should we delete that's associated with them? Just their personal info?
        DocumentReference riderDoc = FirebaseFirestore.getInstance().collection(USER_COLLECTION).document(riderUID);
        riderDoc.delete();
    }

    /**
     * Adds or updates a Driver record in our cloud Firestore
     * @param driver The Driver to store in our cloud Firestore.
     */
    public void storeDriverInfo(Driver driver) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot store driver info on Firestore in Test Mode.");
            return;
        }

        CollectionReference usersDoc = FirebaseFirestore.getInstance().collection(USER_COLLECTION);

        Log.d(TAG, "createAccount:" + driver.getUID() + " - " + driver.getEmail());

        HashMap<String, String> userData = new HashMap<>();
        userData.put(EMAIL_KEY, driver.getEmail());
        userData.put(FIRST_NAME_KEY, driver.getFirstName());
        userData.put(LAST_NAME_KEY, driver.getLastName());
        userData.put(PHONE_NUMBER_KEY, driver.getPhoneNumber());
        userData.put(USER_TYPE_KEY, "driver");

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
     * @param returnFunction The callback to use once we've finished retrieving a driver (and their vehicle)
     */
    public void fetchDriverInfo(final String driverUID, final ReturnValueListener<Driver> returnFunction) {
        // Alternative handler when in test mode
        if(testMode) {
            Log.e(TAG, "Cannot fetch driver info from Firestore in Test Mode.");
            returnFunction.returnValue(null);
            return;
        }

        DocumentReference driverDoc = FirebaseFirestore.getInstance().collection(USER_COLLECTION).document(driverUID);
        driverDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        Map<String, Object> docData = snapshot.getData();
                        final String email = String.valueOf(docData.get(EMAIL_KEY));
                        final String firstName= String.valueOf(docData.get(FIRST_NAME_KEY));
                        final String lastName = String.valueOf(docData.get(LAST_NAME_KEY));
                        final String phoneNumber = String.valueOf(docData.get(PHONE_NUMBER_KEY));
                        fetchVehicleInfo(driverUID, new ReturnValueListener<Vehicle>() {
                            @Override
                            public void returnValue(final Vehicle value) {
                                if (value != null) {
                                    fetchRatingInfo(driverUID, new ReturnValueListener<Rating>() {
                                        @Override
                                        public void returnValue(Rating value2) {
                                            if (value2 != null) {
                                                returnFunction.returnValue(new Driver(driverUID, email, firstName, lastName, phoneNumber, value, value2));
                                            } else {
                                                Log.w(TAG, String.format("Failed to fetch rating for driver [%s]", driverUID));
                                                returnFunction.returnValue(null);
                                            }
                                        }
                                    });
                                } else {
                                    Log.w(TAG, String.format("Failed to fetch vehicle for driver [%s]", driverUID));
                                    returnFunction.returnValue(null);
                                }
                            }
                        });
                    } else {
                        Log.w(TAG, String.format("Driver info for [%s] not found on Firestore.", driverUID));
                        returnFunction.returnValue(null);
                    }
                } else {
                    Log.e(TAG, "Fetching driver info failed. Issue communicating with Firestore.");
                    returnFunction.returnValue(null);
                }
            }
        });
    }

    /**
     * Deletes a user record from our cloud Firestore
     * @param driverUID The UID associated with the driver record to be deleted
     */
    public void deleteDriverInfo(final String driverUID) {
        // Alternative handler when in test mode
        if(testMode) {
            Log.e(TAG, "Cannot delete driver record from Firestore in Test Mode.");
            return;
        }

        // TODO: If we delete a driver, what should we delete that's associated with them? Just their personal info?
        DocumentReference driverDoc = FirebaseFirestore.getInstance().collection(USER_COLLECTION).document(driverUID);
        driverDoc.delete();
    }

    /**
     * Adds or updates a Driver's vehicle in our cloud Firestore
     * @param driverUID This is the driver who is registering the vehicle
     * @param vehicle The vehicle that is being registered
     */
    public void storeVehicleInfo(String driverUID, Vehicle vehicle) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot store vehicle info on Firestore in Test Mode.");
            return;
        }

        CollectionReference vehicles = FirebaseFirestore.getInstance().collection(VEHICLE_COLLECTION);
        HashMap<String, Object> vehicleData = new HashMap<>();

        vehicleData.put(MAKE_KEY, vehicle.getMake());
        vehicleData.put(MODEL_KEY, vehicle.getModel());
        vehicleData.put(SEAT_NUMBER_KEY, vehicle.getSeatNumber());
        vehicleData.put(PLATE_NUMBER_KEY, vehicle.getPlateNumber());
        vehicles.document(driverUID)
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
     * @param driverUID The UID of the driver who owns the vehicle
     * @param returnFunction The callback to use once we've finished retrieving a Vehicle
     */
    public void fetchVehicleInfo(final String driverUID, final ReturnValueListener<Vehicle> returnFunction) {
        //TODO: Decide what constitutes a key for getting vehicle info if drivers have > 1 vehicle

        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot fetch vehicle record from Firestore while in Test Mode.");
            returnFunction.returnValue(null);
            return;
        }

        DocumentReference vehicleDoc = FirebaseFirestore.getInstance().collection(VEHICLE_COLLECTION).document(driverUID);
        vehicleDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        String model = snapshot.getString(MODEL_KEY);
                        String make = snapshot.getString(MAKE_KEY);
                        String plateNumber = snapshot.getString(PLATE_NUMBER_KEY);
                        int seatNumber = snapshot.getLong(SEAT_NUMBER_KEY).intValue();
                        returnFunction.returnValue(new Vehicle(model, make, plateNumber, seatNumber));
                    } else {
                        Log.w(TAG, String.format("Vehicle info for driver [%s] not found on Firestore.", driverUID));
                        returnFunction.returnValue(null);
                    }
                } else {
                    Log.e(TAG, "Fetching vehicle info failed. Issue communicating with Firestore.");
                    returnFunction.returnValue(null);
                }
            }
        });
    }

    /**
     * Adds or updates a Driver's rating in our cloud Firestore
     * @param driverUID This is the driver the rider is rating
     * @param rating The rating that the rider is assigning
     */
    public void storeRatingInfo(String driverUID, Rating rating) {
        CollectionReference ratings = FirebaseFirestore.getInstance().collection(RATING_COLLECTION);
        HashMap<String, Object> ratingData = new HashMap<>();

        ratingData.put(POSITIVE_KEY, rating.getPositive());
        ratingData.put(NEGATIVE_KEY, rating.getNegative());
        ratings.document(driverUID)
                .set(ratingData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating addition successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Rating addition failed" + e.toString());
                    }
                });
    }

    /**
     * Retrieves Rating information from our cloud Firestore
     * @param driverUID The UID of the Driver that belongs to the rating
     * @param returnValueListener The callback to use once we've finished retrieving a Rating
     */
    public void fetchRatingInfo(final String driverUID, final ReturnValueListener<Rating> returnValueListener) {
        DocumentReference ratingDoc = FirebaseFirestore.getInstance().collection(RATING_COLLECTION).document(driverUID);
        ratingDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()) {
                        int positive = snapshot.getLong(POSITIVE_KEY).intValue();
                        int negative = snapshot.getLong(NEGATIVE_KEY).intValue();
                        returnValueListener.returnValue(new Rating(positive, negative));
                    }
                    else {
                        Log.w(TAG, String.format("Rating for driver [%s] not found on Firestore.", driverUID));
                        returnValueListener.returnValue(null);
                    }
                }
                else {
                    Log.e(TAG, "Fetching rating failed. Issue communicating with Firestore.");
                    returnValueListener.returnValue(null);
                }
            }
        });
    }

    /**
     * Adds a users transaction record to our Firestore cloud
     * @param userUID
     * the ID of the user who's wallet is being modified
     * @param wallet
     * the wallet in which a transaction is being added to
     */
    public void storeWalletInfo(String userUID, Wallet wallet, final ReturnValueListener<Boolean> returnValueListener) {
        CollectionReference transactions = FirebaseFirestore.getInstance().collection("users");
        HashMap<String, Object> walletData = new HashMap<>();

        walletData.put(TRANSACTION, wallet.getTransactions().get(0));
        transactions.document(userUID).collection("transactions").document(new Date().toString())
                .set(walletData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        returnValueListener.returnValue(Boolean.TRUE);
                        Log.d(TAG, "Transaction addition successful");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        returnValueListener.returnValue(Boolean.FALSE);
                        Log.d(TAG, "Transaction addition failed" + e.toString());
                    }
                });
    }

    /**
     * Retrieves wallet information from Firestore
     * @param userUID
     * the UID of the User who's wallet is being modified
     * @param returnValueListener
     * The callback to use once we've finished retrieving a Wallet
     */
    public void fetchWalletInfo(final String userUID, final ReturnValueListener<Wallet> returnValueListener) {
        CollectionReference transactionCollection = FirebaseFirestore.getInstance().collection("users").document(userUID).collection("transactions");
        transactionCollection.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Wallet wallet = new Wallet();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                wallet.setTransaction(doc.getData().toString().replace("{transaction_details=","").replace("}", ""));
                            }
                            returnValueListener.returnValue(wallet);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

//        walletDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot snapshot = task.getResult();
//                    if (snapshot.exists()) {
//                        String transaction = snapshot.getString(TRANSACTION);
//                        returnValueListener.returnValue(new Wallet(transaction));
//                    }
//                    else {
//                        Log.w(TAG, String.format("Wallet for user [%s] not found on Firestore.", userUID));
//                        returnValueListener.returnValue(null);
//                    }
//                }
//                else {
//                    Log.e(TAG, "Fetching wallet failed. Issue communicating with Firestore.");
//                    returnValueListener.returnValue(null);
//                }
//            }
//        });
//    }

    /**
     * Temporary function. We'll need to figure out a better solution than this.
     * @param userUID The user ID to look up.
     * @param returnFunction Callback. Will pass true if the user is a driver, false otherwise
     */
    public void isUserDriver(final String userUID, final ReturnValueListener<Boolean> returnFunction) {
        //TODO: Replace with something better

        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot use Firestore to determine whether or not a user is a driver in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

        DocumentReference userDoc = FirebaseFirestore.getInstance().collection(USER_COLLECTION).document(userUID);
        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        returnFunction.returnValue(snapshot.getString(USER_TYPE_KEY).equalsIgnoreCase("driver"));
                    } else {
                        Log.w(TAG, String.format("User info for [%s] not found on Firestore.", userUID));
                        returnFunction.returnValue(null);
                    }
                } else {
                    Log.e(TAG, "Fetching user info failed. Issue communicating with Firestore.");
                    returnFunction.returnValue(null);
                }
            }
        });
    }

    /**
     * Deletes a vehicle record from our cloud Firestore
     * @param driverUID The UID associated with the driver who owns the vehicle
     */
    public void deleteVehicleInfo(final String driverUID) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot delete a vehicle record from Firestore in Test Mode.");
            return;
        }

        DocumentReference vehicleDoc = FirebaseFirestore.getInstance().collection(VEHICLE_COLLECTION).document(driverUID);
        vehicleDoc.delete();
    }

    /**
     * Deletes a rating record from our cloud Firestore
     * @param driverUID The UID associated with the driver that belongs to the rating
     */
    public void deleteRatingInfo(final String driverUID) {
        DocumentReference ratingDoc = FirebaseFirestore.getInstance().collection(RATING_COLLECTION).document(driverUID);
        ratingDoc.delete();
    }

    /**
     * Used to determine whether or not a record exists for a user in our cloud Firestore
     * @param UID The UID of the user we're looking for
     * @param returnFunction Asynchronously returns true if the user exists in our cloud Firestore, false otherwise
     */
    public void checkUser(String UID, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot check Firestore for the presence of a user record in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

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

    /**
     *
     * @param request
     */
    public void confirmPickup(RideRequest request, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot confirm pickup on Firestore in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

        request.setStatus(RideRequest.Status.EN_ROUTE);
        storeRideRequest(request);
        OfflineCache.getReference().cacheCurrentRideRequest(request);
        fetchRideRequest(request.getRiderUID(), new ReturnValueListener<RideRequest>() {
            @Override
            public void returnValue(RideRequest value) {
                returnFunction.returnValue(value != null && value.getStatus() == RideRequest.Status.EN_ROUTE);
            }
        });
    }

    /**
     *
     * @param request
     */
    public void confirmArrival(RideRequest request, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot confirm arrival on Firestore in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

        request.setStatus(RideRequest.Status.ARRIVED);
        storeRideRequest(request);
        OfflineCache.getReference().cacheCurrentRideRequest(request);
        fetchRideRequest(request.getRiderUID(), new ReturnValueListener<RideRequest>() {
            @Override
            public void returnValue(RideRequest value) {
                returnFunction.returnValue(value != null && value.getStatus() == RideRequest.Status.ARRIVED);
            }
        });
    }

    public void completeRide(RideRequest request, final ReturnValueListener<Boolean> returnFunction) {
        // Alternative handler when in test mode
        if (testMode) {
            Log.e(TAG, "Cannot complete rides on Firestore in Test Mode.");
            returnFunction.returnValue(Boolean.FALSE);
            return;
        }

        request.setStatus(RideRequest.Status.COMPLETED);
        storeRideRequest(request);
        fetchRideRequest(request.getRiderUID(), new ReturnValueListener<RideRequest>() {
            @Override
            public void returnValue(RideRequest value) {
                // If this is null or complete, we've done our part
                returnFunction.returnValue(value == null || value.getStatus() == RideRequest.Status.COMPLETED);
            }
        });
    }
}
