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
 * Rating.java
 *
 * Last edit: kenzbauer, 21/03/20 11:04 PM
 *
 * Version
 */

package com.example.gufyguber;

/**
 * This is a class to model a Driver's ratings
 * @author kenzbauer
 * @see Driver
 */
public class Rating {
    private int positive;
    private int negative;

    /**
     * This is the constructor method that sets the attributes of the rating of the Driver
     * @param positive
     * This is the number of positive ratings from Riders
     * @param negative
     * This is the number of negative ratings from Riders
     */
    public Rating(int positive, int negative) {
        this.setPositive(positive);
        this.setNegative(negative);
    }

    /**
     * This sets the objects positive attribute
     * @param positive
     * The positive number of ratings
     */
    public void setPositive(int positive) { this.positive = positive; }

    /**
     * This sets the objects negative attribute
     * @param negative
     * The negative number of ratings
     */
    public void setNegative(int negative) { this.negative = negative; }

    /**
     * This returns the positive ratings
     * @return
     * Return the number of positive ratings
     */
    public Integer getPositive() { return positive; }

    /**
     * This returns the negative ratings
     * @return
     * Return the number of negative ratings
     */
    public Integer getNegative() { return  negative; }

    /**
     * This returns the percentage of ratings that are positive
     * @param positive
     * The positive number of ratings
     * @param negative
     * The negative number of ratings
     * @return
     * Return the positive ratings percentage
     */
    public String getPosPercent(int positive, int negative) {
        int total = positive + negative;

        double posPercent;
        if(total > 0) {
            posPercent = (double)(positive * 100) / total;
        } else {
            posPercent = 0;
        }
        String finalPercent = String.format("%.1f",posPercent) + "%";

        return finalPercent;
    }

    /**
     * This returns the percentage of ratings that are negative
     * @param positive
     * The positive number of ratings
     * @param negative
     * The negative number of ratings
     * @return
     * Return the negative ratings percentage
     */
    public String getNegPercent(int positive, int negative) {
        int total = positive + negative;

        double negPercent;
        if(total > 0) {
            negPercent = (double)(negative * 100)/ total;
        } else {
            negPercent = 0;
        }
        String finalPercent = String.format("%.1f", negPercent) + "%";

        return finalPercent;
    }
}
