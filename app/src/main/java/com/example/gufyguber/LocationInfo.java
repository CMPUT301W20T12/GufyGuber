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
// File: LocationInfo.java
// Date: Feb.29.2020
// Purpose: To collect location information for a Ride Request

// Last Updated: Mar.01.2020 by Robert MacGillivray

package com.example.gufyguber;

import com.google.type.LatLng;

public class LocationInfo {
    private LatLng pickup;
    private void setPickup(LatLng pickup) { this.pickup = pickup; }
    public LatLng getPickup() { return pickup; }

    private LatLng dropoff;
    private void setDropoff(LatLng dropoff) { this.dropoff = dropoff; }
    public LatLng getDropoff() { return this.dropoff; }

    public LatLng current;
    private void setCurrent(LatLng current) { this.current = current; }
    public LatLng getCurrent() { return current; }

    public LocationInfo(LatLng pickup, LatLng dropoff) {
        setPickup(pickup);
        setDropoff(dropoff);
        setCurrent(pickup);
    }

    /**
     * Calculates the distance between current and dropoff
     * @return The distance between current and dropoff in meters
     */
    public float getRemainingDist() {
        //TODO: Calculate distance between current and dropoff
        return -1.0f;
    }
}
