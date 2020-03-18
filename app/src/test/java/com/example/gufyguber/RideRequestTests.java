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
// File: RideRequestTests.java
// Date: Feb.25.2020
// Purpose: To unit-test the RideRequest class

// Last Updated: Feb.25.2020 by Robert MacGillivray

package com.example.gufyguber;

import com.google.android.gms.maps.model.LatLng;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RideRequestTests {

    private RideRequest testRideRequest;

    @Before
    public void testInit() {
        testRideRequest = generateTestRequest();
    }

    @Test
    public void testStatusEnum() {
        // Test PENDING enum for incorrect overrides
        assertEquals(0, RideRequest.Status.PENDING.ordinal());
        assertEquals("Pending", RideRequest.Status.PENDING.toString());
        assertEquals("PENDING", RideRequest.Status.PENDING.name());
        assertEquals(RideRequest.Status.PENDING, RideRequest.Status.valueOf("PENDING"));

        // Test ACCEPTED enum for incorrect overrides
        assertEquals(1, RideRequest.Status.ACCEPTED.ordinal());
        assertEquals("Accepted", RideRequest.Status.ACCEPTED.toString());
        assertEquals("ACCEPTED", RideRequest.Status.ACCEPTED.name());

        // Test CONFIRMED enum for incorrect overrides
        assertEquals(2, RideRequest.Status.CONFIRMED.ordinal());
        assertEquals("Confirmed", RideRequest.Status.CONFIRMED.toString());
        assertEquals("CONFIRMED", RideRequest.Status.CONFIRMED.name());

        // Test EN_ROUTE enum for incorrect overrides
        assertEquals(3, RideRequest.Status.EN_ROUTE.ordinal());
        assertEquals("En Route", RideRequest.Status.EN_ROUTE.toString());
        assertEquals("EN_ROUTE", RideRequest.Status.EN_ROUTE.name());

        // Test ARRIVED enum for incorrect overrides
        assertEquals(4, RideRequest.Status.ARRIVED.ordinal());
        assertEquals("Arrived", RideRequest.Status.ARRIVED.toString());
        assertEquals("ARRIVED", RideRequest.Status.ARRIVED.name());

        // Test COMPLETED enum for incorrect overrides
        assertEquals(5, RideRequest.Status.COMPLETED.ordinal());
        assertEquals("Completed", RideRequest.Status.COMPLETED.toString());
        assertEquals("COMPLETED", RideRequest.Status.COMPLETED.name());
        assertEquals(RideRequest.Status.COMPLETED, RideRequest.Status.valueOf("COMPLETED"));

        // Test CANCELLED enum for incorrect overrides
        assertEquals(6, RideRequest.Status.CANCELLED.ordinal());
        assertEquals("Cancelled", RideRequest.Status.CANCELLED.toString());
        assertEquals("CANCELLED", RideRequest.Status.CANCELLED.name());
        assertEquals(RideRequest.Status.CANCELLED, RideRequest.Status.valueOf("CANCELLED"));

        // Test some interactions with a RideRequest instance
        assertEquals(RideRequest.Status.PENDING, testRideRequest.getStatus());
        testRideRequest.setStatus(RideRequest.Status.ACCEPTED);
        assertEquals(RideRequest.Status.ACCEPTED, testRideRequest.getStatus());
        testRideRequest.setStatus(RideRequest.Status.values()[3]);
        assertEquals(RideRequest.Status.EN_ROUTE, testRideRequest.getStatus());
    }

    @Test
    public void testFairFare() {
        assertEquals(testRideRequest.fairFareEstimate(-Float.MAX_VALUE), 0f);
        assertEquals(testRideRequest.fairFareEstimate(0f), 0f);
        if (RideRequest.FAIR_FARE_PER_METRE > 1) {
            assertEquals(testRideRequest.fairFareEstimate(Float.MAX_VALUE), Float.MAX_VALUE);
        } else {
            assertEquals(testRideRequest.fairFareEstimate(Float.MAX_VALUE), Float.MAX_VALUE * RideRequest.FAIR_FARE_PER_METRE);
        }
    }

    @Test
    public void testDriverAccept() {
        assertEquals(testRideRequest.driverAcceptRideRequest("Test Driver"), true);
        assertEquals(testRideRequest.getDriverUID(), "Test Driver");
        assertEquals(testRideRequest.driverAcceptRideRequest("Test Driver 2"), false);
        assertEquals(testRideRequest.getDriverUID(), "Test Driver");
    }

  public static RideRequest generateTestRequest() {
      LocationInfo testLocation = new LocationInfo(new LatLng(13, 13), new LatLng(13,13));
      return new RideRequest("123456789", 13.13f, testLocation);
  }
}
