/*
 * MapFragment.java
 *
 * Version
 *
 * Last edit: mai-thyle, 04/03/20 11:21 PM
 *
 * Copyright (c) CMPUT301W20T12 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 */

package com.example.gufyguber.ui.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gufyguber.CreateRideRequestFragment;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapFragment extends Fragment implements OnMapReadyCallback, CreateRideRequestFragment.CreateRideRequestListener, CreateRideRequestFragment.CancelCreateRideRequestListener {

    private GoogleMap mMap;
    private Marker pickupMarker;
    private Marker dropoffMarker;
    private CreateRideRequestFragment requestDialog;

    public MapFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // makes a button for us to create ride requests (RIDER) from navigation drawer activity default

        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestDialog == null) {
                    requestDialog = new CreateRideRequestFragment();
                }
                requestDialog.show(getChildFragmentManager(), "create_ride_request");
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapTest);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // zoom to Edmonton and move the camera UNTIL CURRENT LOCATION WORKS
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edmonton));

        float zoomLevel = 16.0f; //max is 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, zoomLevel));


        //______________________________________________________________________________

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if (requestDialog != null) {
                    boolean dirty = false;
                    if (requestDialog.settingStart) {
                        requestDialog.setNewPickup(latLng);
                        if (pickupMarker != null) {
                            pickupMarker.remove();
                        }
                        MarkerInfo newMarker = new MarkerInfo();
                        pickupMarker = newMarker.makeMarker("Pickup", true, latLng, mMap);
                        dirty = true;
                    }

                    if (requestDialog.settingEnd) {
                        requestDialog.setNewDropoff(latLng);
                        if (dropoffMarker != null) {
                            dropoffMarker.remove();
                        }
                        MarkerInfo newMarker = new MarkerInfo();
                        dropoffMarker = newMarker.makeMarker("Dropoff", false, latLng, mMap);
                        dirty = true;
                    }

                    if (dirty) {
                        requestDialog.show(getChildFragmentManager(), "create_ride_request");
                        dirty = false;
                    }
                }
            }
        });
    }

    /**
     * Automatically called when the CreateRideRequestFragment builds a new RideRequest
     * @param newRequest The request created by the dialog fragment
     */
    public void onRideRequestCreated(RideRequest newRequest) {
        RideRequest.setCurrentRideRequest(newRequest);
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (dropoffMarker != null) {
            dropoffMarker.remove();
        }
        pickupMarker = null;
        dropoffMarker = null;
        requestDialog = null;
    }

    public void onRideRequestCreationCancelled() {
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (dropoffMarker != null) {
            dropoffMarker.remove();
        }
        pickupMarker = null;
        dropoffMarker = null;
        requestDialog = null;
    }
}
