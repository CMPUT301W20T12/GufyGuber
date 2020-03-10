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

package com.example.gufyguber;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class LocationInfo {
    private static final String TAG = "LocationInfo";

    private LatLng pickup;
    public void setPickup(LatLng pickup) { this.pickup = pickup; }
    public LatLng getPickup() { return pickup; }

    private LatLng dropoff;
    public void setDropoff(LatLng dropoff) { this.dropoff = dropoff; }
    public LatLng getDropoff() { return this.dropoff; }

    public LatLng current;
    public void setCurrent(LatLng current) { this.current = current; }
    public LatLng getCurrent() { return current; }

    public LocationInfo () {

    }

    public LocationInfo(LatLng pickup, LatLng dropoff) {
        setPickup(pickup);
        setDropoff(dropoff);
        setCurrent(pickup);
    }

    public LocationInfo(GeoPoint pickup, GeoPoint dropoff) {
        if (pickup != null) {
            setPickup(new LatLng(pickup.getLatitude(), pickup.getLongitude()));
        } else {
            setPickup(null);
        }

        if (dropoff != null) {
            setDropoff(new LatLng(dropoff.getLatitude(), dropoff.getLongitude()));
        } else {
            setDropoff(null);
        }

        setCurrent(getPickup());
    }

    public GeoPoint pickupToGeoPoint() {
        return new GeoPoint(getPickup().latitude, getPickup().longitude);
    }

    public GeoPoint dropoffToGeoPoint() {
        return new GeoPoint(getPickup().latitude, getPickup().longitude);
    }

    public static String latlngToString(LatLng latlng) {
        return String.format("(%f, %f)", latlng.latitude, latlng.longitude);
    }

    /**
     * Calculates the distance between current and dropoff
     * @return The distance between current and dropoff in meters
     */
    public float getRemainingDist() {
        if (getCurrent() == null || getDropoff() == null) {
            Log.e(TAG, "LocationInfo.getRemainingDist called with a null current or dropoff.");
            return -1.0f;
        }
        float[] results = new float[1];
        Location.distanceBetween(getCurrent().latitude, getCurrent().longitude,
                getDropoff().latitude, getDropoff().longitude, results);
        return results[0];
    }

    /**
     * Calculates the distance between pickup and dropoff
     * @return The distance between the pickup and dropoff in meters
     */
    public float getTotalDist() {
        if (getPickup() == null || getDropoff() == null) {
            Log.e(TAG, "LocationInfo.getTotalDist() called with a null pickup or dropoff.");
            return -1.0f;
        }
        float[] results = new float[1];
        Location.distanceBetween(getPickup().latitude, getPickup().longitude,
                getDropoff().latitude, getDropoff().longitude, results);
        return results[0];
    }
}
