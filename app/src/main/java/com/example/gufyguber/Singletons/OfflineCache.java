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
 * OfflineCache.java
 *
 * Last edit: scott, 02/04/20 5:49 PM
 *
 * Version
 */

package com.example.gufyguber.Singletons;

import android.util.Log;

import com.example.gufyguber.Models.RideRequest;
import com.example.gufyguber.Models.User;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

/**
 * Caches references to important objects that need to persist in the event of connectivity loss.
 * Utilizes a singleton pattern.
 * @author Robert MacGillivray | Mar.11.2020
 */
public class OfflineCache implements FirebaseManager.RideRequestListener{
    private static OfflineCache reference;
    public static OfflineCache getReference() {
        if (reference == null) {
            reference = new OfflineCache();
            Log.d(TAG, "Offline cache lazy instantiation");
        }
        return reference;
    }

    private static final String TAG = "OfflineCache";

    private ArrayList<RideRequest.StatusChangedListener> statusChangedListeners;

    private RideRequest currentRideRequest;
    private ListenerRegistration rideRequestListener;

    /**
     * Caches a RideRequest instance and initiates any necessary callbacks. Prefer using this
     * instance over fetching a new one from Firestore when possible.
     * @param request The RideRequest instance to cache
     */
    public void cacheCurrentRideRequest(RideRequest request) {
        RideRequest temp = currentRideRequest;
        currentRideRequest = request;

        String rrString = currentRideRequest == null ? "Null" : currentRideRequest.toString();
        Log.d(TAG, "Ride request cached:\n" + rrString);

        if (temp != null && currentRideRequest != null) {
            if (temp.getStatus() != currentRideRequest.getStatus()) {
                notifyRideRequestStatusChangedListeners(currentRideRequest.getStatus());
            }
        } else if (temp == null && currentRideRequest != null) {
            notifyRideRequestStatusChangedListeners(currentRideRequest.getStatus());
        } else if (temp != null && currentRideRequest == null) {
            // Special case for rides cancelled after they're confirmed
            if (temp.getStatus() != RideRequest.Status.PENDING && temp.getStatus() != RideRequest.Status.COMPLETED) {
                notifyRideRequestStatusChangedListeners(RideRequest.Status.CANCELLED);
            }
        }

        // If the current ride request isn't null, we need to make sure we're listening to Firestore for updates
        if (currentRideRequest != null) {
            // Modify listener if request has changed
            if (rideRequestListener == null || temp == null ||
                    !temp.getRiderUID().equalsIgnoreCase(currentRideRequest.getRiderUID())) {
                if (rideRequestListener != null) {
                    rideRequestListener.remove();
                    rideRequestListener = null;
                }
                rideRequestListener = FirebaseManager.getReference().listenToRideRequest(currentRideRequest.getRiderUID(), this);
            }
        } else {
            // Null ride request, no need to listen anymore
            if (rideRequestListener != null) {
                rideRequestListener.remove();
                rideRequestListener = null;
            }
        }
    }

    public void clearCurrentRideRequest(){
        cacheCurrentRideRequest(null);
    }

    public RideRequest retrieveCurrentRideRequest() {
        return currentRideRequest;
    }

    private User currentUser;

    /**
     * Caches a User instance and initiates any necessary callbacks. Prefer using this
     * instance over fetching a new one from Firestore when possible.
     * @param user The User instance to cache (will either be a Rider or a Driver)
     */
    public void cacheCurrentUser(User user) { currentUser = user; }
    public User retrieveCurrentUser() { return currentUser; }

    /**
     * Subscribes a listener to this callback
     * @param newListener An observer that should be notified when the status of a newly cached
     *                    RideRequest is different than the previously cached RideRequest
     */
    public void addRideRequestStatusChangedListener(RideRequest.StatusChangedListener newListener) {
        if (statusChangedListeners == null) {
            statusChangedListeners = new ArrayList<RideRequest.StatusChangedListener>();
        }

        statusChangedListeners.add(newListener);
    }

    /**
     * Unsubscribes a listener from this callback
     * @param listener An observer that should no longer be notified when the status of a newly cached
     *                 RideRequest is different than the previously cached RideRequest
     */
    public void removeRideRequestStatusChangedListener(RideRequest.StatusChangedListener listener) {
        if (statusChangedListeners == null) {
            statusChangedListeners = new ArrayList<RideRequest.StatusChangedListener>();
        }

        statusChangedListeners.remove(listener);
    }

    /**
     * @param newStatus The new status to poss to the listeners
     */
    private void notifyRideRequestStatusChangedListeners(RideRequest.Status newStatus) {
        if (statusChangedListeners == null) {
            statusChangedListeners = new ArrayList<RideRequest.StatusChangedListener>();
        }

        for(RideRequest.StatusChangedListener listener : statusChangedListeners) {
            listener.onStatusChanged(newStatus);
        }
    }

    public void onRideRequestUpdated(RideRequest updatedRideRequest) {
        String rrString = updatedRideRequest == null ? "Null" : updatedRideRequest.toString();
        Log.d(TAG, "Ride request updated from Firestore to:\n" + rrString);
        cacheCurrentRideRequest(updatedRideRequest);
    }

    /**
     * Dismantles the cache. Should really only be used when the user signs out.
     */
    public void clearCache() {
        reference = null;
        if (rideRequestListener != null) {
            rideRequestListener.remove();
            rideRequestListener = null;
        }
        Log.d(TAG, "Offline cache cleared" );
    }
}
