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
 * FirebaseManagerTests.java
 *
 * Last edit:  09/03/20 9:11 PM
 *
 * Version
 */

// Name: Robert MacGillivray
// File: FirebaseManagerTests.java
// Date: Mar.09.2020
// Purpose: To run unit tests on the FirebaseManager class

package com.example.gufyguber;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

public class FirebaseManagerTests {
    private Rider testRider;
    private Driver testDriver;
    private Vehicle testVehicle;
    private RideRequest testRideRequest;

    @Ignore("Unable to test Firestore right now.")
    @BeforeAll
    public void setup() {
        //TODO: Should connect to Firestore
    }

    @Ignore("Unable to test Firestore right now.")
    @Test
    public void testRider() {
        final Rider testRider = new Rider("testRiderUID", "testEmail@test.com", "TestFirstName",
                "TestLastName", "(123)456-7890");

        FirebaseManager.getReference().fetchRiderInfo(testRider.getUID(), new FirebaseManager.ReturnValueListener<Rider>() {
            @Override
            public void returnValue(Rider value) {
                assertNull(value);
            }
        });

        FirebaseManager.getReference().storeRiderInfo(testRider);
        FirebaseManager.getReference().fetchRiderInfo(testRider.getUID(), new FirebaseManager.ReturnValueListener<Rider>() {
            @Override
            public void returnValue(Rider value) {
                assertEquals(value.getUID(), testRider.getUID());
                assertEquals(value.getEmail(), testRider.getEmail());
                assertEquals(value.getFirstName(), testRider.getFirstName());
                assertEquals(value.getLastName(), testRider.getLastName());
                assertEquals(value.getPhoneNumber(), testRider.getPhoneNumber());
            }
        });

        FirebaseManager.getReference().deleteRiderInfo(testRider.getUID());
        FirebaseManager.getReference().fetchRiderInfo(testRider.getUID(), new FirebaseManager.ReturnValueListener<Rider>() {
            @Override
            public void returnValue(Rider value) {
                assertNull(value);
            }
        });
    }

    @Ignore("Unable to test Firestore right now.")
    @Test
    public void testVehicle() {
        String testDriverUID = "testDriver";
        final Vehicle testVehicle = new Vehicle("testModel", "testMake", "testPlate", 3);

        FirebaseManager.getReference().fetchVehicleInfo(testDriverUID, new FirebaseManager.ReturnValueListener<Vehicle>() {
            @Override
            public void returnValue(Vehicle value) {
                assertNull(value);
            }
        });

        FirebaseManager.getReference().storeVehicleInfo(testDriverUID, testVehicle);
        FirebaseManager.getReference().fetchVehicleInfo(testDriverUID, new FirebaseManager.ReturnValueListener<Vehicle>() {
            @Override
            public void returnValue(Vehicle value) {
                assertEquals(value.getMake(), testVehicle.getMake());
                assertEquals(value.getModel(), testVehicle.getModel());
                assertEquals(value.getPlateNumber(), testVehicle.getPlateNumber());
                assertEquals(value.getSeatNumber(), testVehicle.getSeatNumber());
            }
        });

        FirebaseManager.getReference().deleteVehicleInfo(testDriverUID);
        FirebaseManager.getReference().fetchVehicleInfo(testDriverUID, new FirebaseManager.ReturnValueListener<Vehicle>() {
            @Override
            public void returnValue(Vehicle value) {
                assertNull(value);
            }
        });
    }

    @Ignore("Unable to test Firestone right now.")
    @Test
    public void testRating() {
        String testDriverUID = "testDriver";
        final Rating testRating = new Rating(8, 8);

        FirebaseManager.getReference().fetchRatingInfo(testDriverUID, new FirebaseManager.ReturnValueListener<Rating>() {
            @Override
            public void returnValue(Rating value) {
                assertNull(value);
            }
        });

        FirebaseManager.getReference().storeRatingInfo(testDriverUID, testRating);
        FirebaseManager.getReference().fetchRatingInfo(testDriverUID, new FirebaseManager.ReturnValueListener<Rating>() {
            @Override
            public void returnValue(Rating value) {
                assertEquals(value.getPositive(), testRating.getPositive());
                assertEquals(value.getNegative(), testRating.getNegative());
            }
        });

        FirebaseManager.getReference().deleteRatingInfo(testDriverUID);
        FirebaseManager.getReference().fetchRatingInfo(testDriverUID, new FirebaseManager.ReturnValueListener<Rating>() {
            @Override
            public void returnValue(Rating value) {
                assertNull(value);
            }
        });
    }

    @Ignore("Unable to test Firestore right now.")
    @Test
    public void testDriver() {
        final Vehicle testVehicle = new Vehicle("testModel", "testMake", "testPlate", 3);
        final Rating testRating = new Rating(8, 8);
        final Driver testDriver = new Driver("testDriverUID", "testEmail@test.com",
                "testFirstName", "testLastName", "(098)765-4321", testVehicle, testRating);

        FirebaseManager.getReference().fetchDriverInfo(testDriver.getUID(), new FirebaseManager.ReturnValueListener<Driver>() {
            @Override
            public void returnValue(Driver value) {
                assertNull(value);
            }
        });

        FirebaseManager.getReference().storeDriverInfo(testDriver);
        FirebaseManager.getReference().fetchDriverInfo(testDriver.getUID(), new FirebaseManager.ReturnValueListener<Driver>() {
            @Override
            public void returnValue(Driver value) {
                assertEquals(value.getUID(), testDriver.getUID());
                assertEquals(value.getEmail(), testDriver.getEmail());
                assertEquals(value.getFirstName(), testDriver.getFirstName());
                assertEquals(value.getLastName(), testDriver.getLastName());
                assertEquals(value.getPhoneNumber(), testDriver.getPhoneNumber());
                assertEquals(value.getVehicle().getMake(), testDriver.getVehicle().getMake());
                assertEquals(value.getVehicle().getModel(), testDriver.getVehicle().getModel());
                assertEquals(value.getVehicle().getPlateNumber(), testDriver.getVehicle().getPlateNumber());
                assertEquals(value.getVehicle().getSeatNumber(), testDriver.getVehicle().getSeatNumber());
            }
        });
    }
}