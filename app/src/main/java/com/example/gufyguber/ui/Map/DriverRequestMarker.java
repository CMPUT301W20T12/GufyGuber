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
 * DriverRequestMarker.java
 *
 * Last edit: scott, 11/03/20 9:23 PM
 *
 * Version
 */

package com.example.gufyguber.ui.Map;

import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.RideRequest;
import com.example.gufyguber.Rider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class DriverRequestMarker {
    private Marker marker;
    public Marker getMarker() { return marker; }
    private RideRequest rideRequest;
    public RideRequest getRideRequest() { return rideRequest; }
    private Rider requestRider;
    private String markerTitle = "";

    public DriverRequestMarker(RideRequest request) {
        this.rideRequest = request;
        FirebaseManager.getReference().fetchRiderInfo(request.getRiderUID(), new FirebaseManager.ReturnValueListener<Rider>() {
            @Override
            public void returnValue(Rider value) {
                if (value == null) {
                    return;
                }
                requestRider = value;
                markerTitle = String.format("%s %s is requesting a ride.", requestRider.getFirstName(), requestRider.getLastName());
                if (getMarker() != null) {
                    getMarker().setTitle(markerTitle);
                }
            }
        });
    }

    public Marker makeMarker(GoogleMap mMap) {
        // creates a maker and zooms to it. Get coordinates.
        marker = mMap.addMarker(new MarkerOptions()
                .position(rideRequest.getLocationInfo().getPickup())
                .title(markerTitle)
                .snippet(String.format("Opened: %s\nPickup: %s\nDrop Off: %s\nTotal Distance: %.0f Metres\n Fare: $%.2f",
                        rideRequest.getTimeInfo().getRequestOpenTime().toString(),
                        LocationInfo.latlngToString(rideRequest.getLocationInfo().getPickup()),
                        LocationInfo.latlngToString(rideRequest.getLocationInfo().getPickup()),
                        rideRequest.getLocationInfo().getTotalDist(), rideRequest.getOfferedFare()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );

        marker.setPosition(rideRequest.getLocationInfo().getPickup());
        marker.setTag(this);

        return marker;
    }
}
