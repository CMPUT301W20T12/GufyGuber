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

package com.example.gufyguber.Map;

import com.example.gufyguber.Models.LocationInfo;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * This class creates an instance of a marker.
 */

public class MarkerInfo {

    private Marker marker;

    /**
     *
     * @param title name of the marker
     * @param startPoint the start point or the end point
     * @param point LatLng of where the marker will be
     * @param mMap the GoogleMap view
     * @return
     * returns a marker
     */

    public Marker makeMarker(String title, boolean startPoint, LatLng point, GoogleMap mMap) {

                // creates a maker and zooms to it. Get coordinates.
                marker = mMap.addMarker(new MarkerOptions()
                        .position(point)
                        .title(title)
                        .snippet(LocationInfo.latlngToString(point))
                        .icon(BitmapDescriptorFactory.defaultMarker(startPoint ? BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_ROSE))
                );

                //zoom
                marker.setPosition(point);
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

                //float zoomLevel = 16.0f; //max is 21
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoomLevel));

                return marker;
            }


}
