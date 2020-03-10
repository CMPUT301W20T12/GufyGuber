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

import com.google.type.LatLng;

import org.junit.Before;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RideRequestTests {

    private RideRequest testRideRequest;

    @Before
    public void testInit() {
        testRideRequest = GenerateTestRequest();
    }

    @Test
    public void TestStatusEnum() {
        // Test PENDING enum for incorrect overrides
        assertEquals(0, RideRequest.Status.PENDING.ordinal());
        assertEquals("Pending", RideRequest.Status.PENDING.toString());
        assertEquals("PENDING", RideRequest.Status.PENDING.name());

        // Test ACCEPTED enum for incorrect overrides
        assertEquals(1, RideRequest.Status.ACCEPTED.ordinal());
        assertEquals("Accepted", RideRequest.Status.ACCEPTED.toString());
        assertEquals("ACCEPTED", RideRequest.Status.ACCEPTED.name());

        // Test COMPLETED enum for incorrect overrides
        assertEquals(2, RideRequest.Status.COMPLETED.ordinal());
        assertEquals("Completed", RideRequest.Status.COMPLETED.toString());
        assertEquals("COMPLETED", RideRequest.Status.COMPLETED.name());

        // Test some interactions with a RideRequest instance
        assertEquals(RideRequest.Status.PENDING, testRideRequest.getStatus());
        testRideRequest.setStatus(RideRequest.Status.ACCEPTED);
        assertEquals(RideRequest.Status.ACCEPTED, testRideRequest.getStatus());
        testRideRequest.setStatus(RideRequest.Status.values()[2]);
        assertEquals(RideRequest.Status.COMPLETED, testRideRequest.getStatus());
    }

  public static RideRequest GenerateTestRequest() {
      LatLng testPickup = LatLng.newBuilder().setLatitude(13).setLongitude(13).build();
      LatLng testDropoff = LatLng.newBuilder().setLatitude(31).setLongitude(31).build();
      LocationInfo testLocation = new LocationInfo(testPickup, testDropoff);
      return new RideRequest("123456789", 13.13f, testLocation);
  }
}
