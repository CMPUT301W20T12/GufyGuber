/*
 * MarkerInfo.java
 *
 * Version
 *
 * Last edit: mai-thyle, 08/03/20 6:36 PM
 *
 * Copyright (c) CMPUT301W20T12 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 */

package com.example.gufyguber.ui.Map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerInfo extends MapFragment implements OnMapReadyCallback {

    private Marker marker;

    public void makeMarker(LatLng point, GoogleMap mMap) {

                // creates a maker and zooms to it. Get coordinates.
                marker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title("Start Point??")
                        .snippet(point.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                );

                //zoom
                marker.setPosition(point);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

                float zoomLevel = 18.0f; //max is 21
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoomLevel));


            }



    /**
     * takes in a marker (on Click)
     * @param marker
     * @return
     * returns the LatLng
     */

    public LatLng getLatLng(Marker marker) {
        Marker newMarker = marker;

        return newMarker.getPosition();
    }
}
