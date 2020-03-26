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

public class Rating {
    private int positive;
    private int negative;

    public Rating(int positive, int negative) {
        this.setPositive(positive);
        this.setNegative(negative);
    }

    public void setPositive(int positive) { this.positive = positive; }

    public void setNegative(int negative) { this.negative = negative; }

    public Integer getPositive() { return positive; }

    public Integer getNegative() { return  negative; }

    public String getPosPercent(int positive, int negative) {
        int total = positive + negative;

        int posPercent;
        if(total > 0) {
            posPercent = (positive * 100) / total;
        } else {
            posPercent = 0;
        }
        String finalPercent = posPercent + "%";

        return finalPercent;
    }

    public String getNegPercent(int positive, int negative) {
        int total = positive + negative;

        int negPercent;
        if(total > 0) {
            negPercent = (negative * 100)/ total;
        } else {
            negPercent = 0;
        }
        String finalPercent = negPercent + "%";

        return finalPercent;
    }
}
