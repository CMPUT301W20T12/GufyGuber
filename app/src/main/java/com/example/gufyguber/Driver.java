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
 * Driver.java
 *
 * Last edit: dalton, 04/03/20 1:40 PM
 *
 * Version
 */

package com.example.gufyguber;

/**
 * This is an instance of a User class for a 'Driver' on the app
 * @author dalton
 * @version 1.0
 * @see User
 */
public class Driver extends User{
    //TODO: reference to a RideRequest
    //TODO: reference to a Rating

    private Vehicle vehicle;

    /**
     * This is the constructor method that sets the drivers attributes by calling the superclass
     * constructor method
     * @param email
     *  The email address associated with the rider's account
     * @param firstName
     *  The rider's first name
     * @param lastName
     * The riders' last name
     * @param phoneNumber
     *  The rider's phone number
     */
    public Driver( String email, String firstName, String lastName,
                  String phoneNumber, Vehicle vehicle) {
        super(email, firstName, lastName, phoneNumber);
        this.setVehicle(vehicle);
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}
