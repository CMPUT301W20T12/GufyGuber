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
 * User.java
 *
 * Last edit: dalton, 26/02/20 12:40 PM
 *
 * Version
 */

package com.example.gufyguber;

import java.net.URI;

/**
 * This is an abstract class that stores the app user's profile details. It is the superclass to
 * user types Rider and Driver.
 * @author dalton
 * @version 1.0
 * @see Rider
 * @see Driver
 */
public abstract class User {
    private String UID;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private URI profilePicture;
    private Wallet wallet;

    /**
     * This is the constructor method that sets the users attributes
     * @param email
     *  The email address associated with the account
     * @param firstName
     *  The user's first name
     * @param lastName
     * The users' last name
     * @param phoneNumber
     *  The users phone number
     */
    public User(String UID, String email, String firstName, String lastName,
                String phoneNumber) {
        this.setUID(UID);
        this.setEmail(email);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setPhoneNumber(phoneNumber);
    }

    /**
     * This gets the account's unique firebase UID
     */
    public String getUID() {
        return UID;
    }

    /**
     * This returns the account's email address
     * @return
     *  Return the email address string
     */
    public String getEmail() {
        return email;
    }

    /**
     * This returns the user's first name
     * @return
     *  Return the first name string
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * This returns the user's last name
     * @return
     *  Return the last name string
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * This returns the account's phone number
     * @return
     *  Return the phone number string
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }


    /**
     * This sets the user's UID to the unique firebase UID associated with the email
     * @param UID
     *  This is the candidate email address to set for the user
     */
    public void setUID(String UID) {
        this.UID = UID;
    }

    /**
     * This sets the account's email address
     * @param email
     *  This is the candidate email address to set for the user
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * This sets the first name associated with the user's account
     * @param firstName
     *  This is the candidate first name to set for the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * This sets the last name associated with the user's account
     * @param lastName
     *  This is the candidate last name to set for the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * This sets the phone number associated with the users account
     * @param phoneNumber
     *  This is the candidate phone number to set for the user
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


}
