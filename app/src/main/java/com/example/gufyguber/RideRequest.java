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

// Last Updated: Feb.25.2020 by Robert MacGillivray

package com.example.gufyguber;

/**
 * Model class for Gufy Guber ride requests
 */
public class RideRequest {
    /**
     * Enumerated representation of RideRequest status.
     * toString() methods have been overridden to provide a pretty-print string representation
     */
    enum Status {
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
        }
    }

    //TODO: Reference to a Rider
    //TODO: Reference to a Driver

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

    //TODO: A locally owned LocationInfo instance
    //TODO: A locally owned TimeInfo instance

    //TODO: Ride request constructor that has rider, location, and fare info parameters
    public RideRequest (float offeredFare) {
        setStatus(Status.PENDING);
        setOfferedFare(offeredFare);
    }

    /**
     * Cancels this ride request and initiates related cleanup
     */
    public void cancelRideRequest() {
        //TODO: Handle canceling and cleaning up after a ride request
    }
}
