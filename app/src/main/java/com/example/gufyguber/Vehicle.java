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
 * Last edit: dalton, 26/02/20 1:54 PM
 *
 * Version 1.0
 */

package com.example.gufyguber;

/**
 * This is a class to model a Driver's vehicle
 * @author dalton, harrison
 * @version 1.0
 * @see Driver
 */
public class Vehicle {
    private String model;
    private String make;
    private String year;
    private String plateNumber;
    private int seatNumber;
    private String colour;

    /**
     * This is the constructor method that sets the attributes of the vehicle being registered by
     * the Driver.
     * @param model
     *  This is the model of the vehicle being registered
     * @param make
     *  This is the make of the vehicle being registered
     * @param plateNumber
     *  This is the license plate number of the vehicle being registered
     * @param seatNumber
     *  This is the number of seats available for Riders to sit
     */
    public Vehicle(String model, String make, String plateNumber, int seatNumber) {
        this.setModel(model);
        this.setMake(make);
        this.setPlateNumber(plateNumber);
        this.setSeatNumber(seatNumber);
    }

    /**
     * This sets the objects model attribute
     * @param model
     *  The model of the vehicle being registered
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * This sets the objects make attribute
     * @param make
     *  The make of the vehicle being registered
     */
    public void setMake(String make) {
        this.make = make;
    }

    /**
     * This sets the objects plateNumber attribute
     * @param plateNumber
     *  The license plate number of the vehicle being registered
     */
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    /**
     * This sets the objects seatNumber attribute
     * @param seatNumber
     *  The number of sets available for Riders of the vehicle being registered
     */
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    /**
     * This returns the model of the Driver's vehicle
     * @return
     *  Return the vehicle model
     */
    public String getModel() {
        return model;
    }

    /**
     * This returns the make of the Driver's vehicle
     * @return
     *  Return the vehicle make
     */
    public String getMake() {
        return make;
    }

    /**
     * This returns the license plate number of the Driver's vehicle
     * @return
     *  Return the vehicle's license plate number
     */
    public String getPlateNumber() {
        return plateNumber;
    }

    /**
     * This returns the number of available seats in the Driver's vehicle
     * @return
     *  Return the number of available seats in the vehicle
     */
    public Integer getSeatNumber() {
        return seatNumber;
    }
}
