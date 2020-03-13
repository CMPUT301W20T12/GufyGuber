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
 * Rider.java
 *
 * Last edit: dalton, 26/02/20 12:40 PM
 *
 * Version
 */

package com.example.gufyguber;

/**
 * This is an instance of a User class for a 'Rider' on the app
 * @author dalton
 * @version 1.0
 * @see User
 */
public class Rider extends User {

    //TODO: reference to a RideRequest

    /**
     * This is the constructor method that sets the riders attributes by calling the superclass
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
    public Rider(String UID, String email, String firstName, String lastName,
                 String phoneNumber) {
        super(UID, email, firstName, lastName, phoneNumber);
    }
}
