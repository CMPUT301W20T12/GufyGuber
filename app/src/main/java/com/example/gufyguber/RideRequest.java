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
 */

// Name: Robert MacGillivray
// File: RideRequest.java
// Date: Feb.24.2020
// Purpose: To model user ride requests for the Gufy Guber app

package com.example.gufyguber;

import android.location.Location;

import java.sql.Time;

/**
 * Model class for Gufy Guber ride requests
 */
public class RideRequest {
    /**
     * Enumerated representation of RideRequest status.
     * toString() methods have been overridden to provide a pretty-print string representation
     */
    public enum Status {
        PENDING {
            @Override
            public String toString() {
                return "Pending";
            }
        },
        ACCEPTED {
            @Override
            public String toString() {
                return "Accepted";
            }
        },
        COMPLETED {
            @Override
            public String toString() {
                return "Completed";
            }
        },
        CANCELLED {
            @Override
            public String toString() { return "Cancelled"; }
        }
    }

    private static final float FAIR_FARE_PER_METRE = 0.01f;

    /**
     * Firebase UID of rider that initiated this ride request
     */
    private String riderUID;
    private void setRiderUID(String riderUID) { this.riderUID = riderUID; }
    public String getRiderUID() { return riderUID; }

    /**
     * Firebase UID of driver user that accepted this ride request
     */
    private String driverUID;
    public String getDriverUID() { return driverUID; }
    private void setDriverUID(String driverUID) { this.driverUID = driverUID; }

    /**
     * Current status of this ride request
     */
    private Status status;
    public Status getStatus(){ return status; }
    public void setStatus(Status status) { this.status = status; }

    /**
     * Total fare offered by rider for this ride request
     */
    private float offeredFare;
    public float getOfferedFare() { return offeredFare; }
    public void setOfferedFare(float offeredFare) { this.offeredFare = offeredFare; }

    /**
     * Used to keep track of pickup, dropoff, and current locations
     */
    private LocationInfo locationInfo;
    public LocationInfo getLocationInfo() { return locationInfo; }

    /**
     * Used to keep track of request, pickup, and dropoff times
     */
    private TimeInfo timeInfo;
    public TimeInfo getTimeInfo() { return timeInfo; }

    /**
     * Constructor for the RideRequest class
     * @param riderUID The UID of the rider user that's creating the request
     * @param offeredFare The price offered for the ride, in QR-Bucks
     * @param locationInfo Contains the coordinates of the pickup and dropoff locations
     */
    public RideRequest (String riderUID, float offeredFare, LocationInfo locationInfo) {
        setRiderUID(riderUID);
        setDriverUID(null);
        setStatus(Status.PENDING);
        setOfferedFare(offeredFare);
        this.locationInfo = locationInfo;
        timeInfo = new TimeInfo();
    }

    public RideRequest(String riderUID, String driverUID, Status status, float offeredFare, LocationInfo locationInfo, TimeInfo timeInfo) {
        setRiderUID(riderUID);
        setDriverUID(driverUID);
        setStatus(status);
        setOfferedFare(offeredFare);
        this.locationInfo = locationInfo;
        this.timeInfo = timeInfo;
    }

    /**
     * Allows a driver to accept a ride request if it hasn't already been accepted
     * @param driverUID The UID of the driver user that is attempting to accept this ride request
     * @return True if the request acceptance succeeded, false otherwise
     */
    public boolean driverAcceptRideRequest(String driverUID) {
        if (getDriverUID() == null || getStatus() != Status.PENDING) {
            return false;
        }

        // Can set the driverUID to null to cancel an accepted request, otherwise mark request accepted
        setDriverUID(driverUID);
        setStatus(getDriverUID() == null ? Status.PENDING : Status.ACCEPTED);
        return true;
    }

    /**
     * Cancels this ride request and initiates related cleanup
     */
    public void cancelRideRequest() {
        setStatus(Status.CANCELLED);
        FirebaseManager.getReference().deleteRideRequest(getRiderUID());

        //TODO: Instead of deleting, modify status to notify drivers of cancelled requests
    }

    public static float fairFareEstimate(LocationInfo locationInfo) {
        return locationInfo.getTotalDist() * FAIR_FARE_PER_METRE;
    }
}
