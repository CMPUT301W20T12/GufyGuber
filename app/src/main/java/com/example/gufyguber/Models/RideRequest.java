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
 * RideRequest.java
 *
 * Last edit: scott, 02/04/20 5:50 PM
 *
 * Version
 */

package com.example.gufyguber.Models;

import android.util.Log;

/**
 * Model class for Gufy Guber ride requests
 * @author Robert MacGillivray | Feb.24.2020
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
            public String userDisplay() { return "Request Pending"; }
        },
        ACCEPTED {
            @Override
            public String toString() {
                return "Accepted";
            }
            public String userDisplay() { return "Driver Accepted"; }
        },
        CONFIRMED {
          @Override
          public String toString() { return "Confirmed"; }
          public String userDisplay() { return "Awaiting Pickup"; }
        },
        EN_ROUTE {
            @Override
            public String toString() { return "En Route"; }
            public String userDisplay() { return "En Route"; }
        },
        ARRIVED {
            @Override
            public String toString() { return "Arrived"; }
            public String userDisplay() { return "Ride Complete"; }
        },
        COMPLETED {
            @Override
            public String toString() { return "Completed"; }
            public String userDisplay() { return "Payment Complete"; }
        },
        CANCELLED {
            @Override
            public String toString() { return "Cancelled"; }
            public String userDisplay() { return "Request Cancelled"; }
        }
    }

    /**
     * Interface that allows us to message listeners when the cached ride request status changes
     */
    public interface StatusChangedListener{
        public void onStatusChanged(Status newStatus);
    }

    private static final String TAG = "RideRequest";

    public static final float FAIR_FARE_PER_METRE = 0.01f;

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
    public void setDriverUID(String driverUID) { this.driverUID = driverUID; }

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

    /**
     * Greedy constructor for the RideRequest class
     * @param riderUID The UID of the rider user that created the request
     * @param driverUID The UID of the driver user that accepted the request
     * @param status The current status of the request
     * @param offeredFare The fare that the rider offered for this request
     * @param locationInfo The location info for the pickup and dropoff of the request
     * @param timeInfo The timestamps for important events related to the request
     */
    public RideRequest(String riderUID, String driverUID, Status status, float offeredFare, LocationInfo locationInfo, TimeInfo timeInfo) {
        setRiderUID(riderUID);
        setDriverUID(driverUID);
        setStatus(status);
        setOfferedFare(offeredFare);
        this.locationInfo = locationInfo;
        this.timeInfo = timeInfo;
    }

    @Override
    public String toString() {
        return String.format("Rider UID: %s\nDriver UID: %s\nStatus: %s\nFare: %.2f\nPickup: %s\nDrop Off: %s\nOpen Time: %tc\nAccepted Time: %tc\nClosed Time: %tc\n",
                getRiderUID(), getDriverUID(), getStatus().name(), getOfferedFare(), LocationInfo.latlngToString(getLocationInfo().getPickup()), LocationInfo.latlngToString(getLocationInfo().getDropoff()), getTimeInfo().getRequestOpenTime(), getTimeInfo().getRequestAcceptedTime(), getTimeInfo().getRequestClosedTime());
    }

    /**
     * Calculates a fair fare based on provided LocationInfo
     * @param locationInfo The LocationInfo used to calculate a distance with which to calculate the fair fare
     * @return A float representing a fair fare in dollars
     */
    public static float fairFareEstimate(LocationInfo locationInfo) {
        return fairFareEstimate(locationInfo.getTotalDist());
    }

    /**
     * Calculates a Fair fare based on provided distance
     * @param estimatedDistance The driving distance used to calculate a fair fare
     * @return A float representing a fair fare in dollars
     */
    public static float fairFareEstimate(float estimatedDistance) {
        if (estimatedDistance < 0) {
            Log.e(TAG, "Fairs cannot be estimated with negative distance. Returning 0.");
            return 0f;
        } else {
            float fairFair = estimatedDistance * FAIR_FARE_PER_METRE;
            return fairFair == Float.POSITIVE_INFINITY ? Float.MAX_VALUE : fairFair;
        }
    }
}
