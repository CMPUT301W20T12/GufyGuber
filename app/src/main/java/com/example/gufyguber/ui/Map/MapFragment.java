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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.gufyguber.CreateRideRequestFragment;
import com.example.gufyguber.Driver;
import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.example.gufyguber.Rider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class creates a MapFragment on the NavigationActivity "Map" tab.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, CreateRideRequestFragment.CreateRideRequestListener,
        CreateRideRequestFragment.CancelCreateRideRequestListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Marker pickupMarker;
    private Marker dropoffMarker;
    private CreateRideRequestFragment requestDialog;
    private FloatingActionButton fab;
    private TextView offlineText;
    private Timer offlineTestTimer;
    private boolean isDriver;

    /**
     *  This class is an intermediate step to differentiate the driver from the rider
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * returns what the user type is (rider/driver)
     */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isDriver = (OfflineCache.getReference().retrieveCurrentUser() instanceof Driver);

        // Kick off a cache to sync with Firebase in case the app was closed and opened
        if (isDriver) {
            // For the driver, having the Rider UID will be enough to keep it synced later on with a cheaper query
            FirebaseManager.getReference().fetchRideRequestsWithStatus(RideRequest.Status.ACCEPTED, new FirebaseManager.ReturnValueListener<ArrayList<RideRequest>>() {
                @Override
                public void returnValue(ArrayList<RideRequest> value) {
                    if (value == null) {
                        return;
                    }

                    for (RideRequest request : value) {
                        if (request.getDriverUID().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            OfflineCache.getReference().cacheCurrentRideRequest(request);
                        }
                    }
                }
            });
        } else {
            FirebaseManager.getReference().fetchRideRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), new FirebaseManager.ReturnValueListener<RideRequest>() {
                @Override
                public void returnValue(RideRequest value) {
                    OfflineCache.getReference().cacheCurrentRideRequest(value);
                }
            });
        }

        if (isDriver) {
            return driverOnCreateView(inflater, container, savedInstanceState);
        } else {
            return riderOnCreateView(inflater, container, savedInstanceState);
        }
    }

    /**
     * This function handles rider users. Creates a button for requests.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * returns the view
     */
    private View riderOnCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // makes a button for us to create ride requests (RIDER) from navigation drawer activity default

        offlineText = v.findViewById(R.id.offline_text);
        fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestDialog == null) {
                    requestDialog = new CreateRideRequestFragment();
                }
                requestDialog.show(getChildFragmentManager(), "create_ride_request");
            }
        });

        // Sets a background task to periodically check for an internet connection
        offlineTestTimer = new Timer();
        offlineTestTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    offlineTestTimer.cancel();
                    offlineTestTimer.purge();
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isOnline = FirebaseManager.getReference().isOnline(getContext());
                            fab.setVisibility(isOnline ? View.VISIBLE : View.GONE);
                            offlineText.setVisibility(isOnline ? View.GONE : View.VISIBLE);

                            if (mMap != null) {
                                RideRequest request = OfflineCache.getReference().retrieveCurrentRideRequest();
                                if (request == null && pickupMarker != null && dropoffMarker != null) {
                                    pickupMarker.remove();
                                    pickupMarker = null;
                                    dropoffMarker.remove();
                                    dropoffMarker = null;
                                } else if (request != null && pickupMarker == null && dropoffMarker == null) {
                                    MarkerInfo newMarker = new MarkerInfo();
                                    pickupMarker = newMarker.makeMarker("Pickup", true, request.getLocationInfo().getPickup(), mMap);
                                    newMarker = new MarkerInfo();
                                    dropoffMarker = newMarker.makeMarker("Drop Off", false, request.getLocationInfo().getDropoff(), mMap);
                                }
                            }
                        }
                    });
                }
            }
        }, 0, 3000);

        return v;
    }

    /**
     * This function handles the driver users. Drivers are able to see the open requests.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     * returns the view
     */
    public View driverOnCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_driver_map, container, false);

        offlineText = v.findViewById(R.id.offline_text);

        // Sets a background task to periodically check for an internet connection
        offlineTestTimer = new Timer();
        offlineTestTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null) {
                    offlineTestTimer.cancel();
                    offlineTestTimer.purge();
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean isOnline = FirebaseManager.getReference().isOnline(getContext());
                            offlineText.setVisibility(isOnline ? View.GONE : View.VISIBLE);
                            if (mMap != null) {
                                refreshRequests(isOnline);
                            }
                        }
                    });
                }
            }
        }, 0, 3000);

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (isDriver) {
            driverOnViewCreated(view, savedInstanceState);
        } else {
            riderOnViewCreated(view, savedInstanceState);
        }
    }

    private void riderOnViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.rider_map);
        mapFragment.getMapAsync(this);
    }

    private void driverOnViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.driver_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (isDriver) {
            driverOnMapReady(googleMap);
        } else {
            riderOnMapReady(googleMap);
        }
    }

    /**
     * This function handles the request input for pickup and destination
     * @param googleMap
     */

    private void riderOnMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // zoom to Edmonton and move the camera UNTIL CURRENT LOCATION WORKS
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edmonton));

        float zoomLevel = 16.0f; //max is 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, zoomLevel));

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
                        dropoffMarker = newMarker.makeMarker("Drop Off", false, latLng, mMap);
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

    private void driverOnMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        // zoom to Edmonton and move the camera UNTIL CURRENT LOCATION WORKS
        LatLng edmonton = new LatLng(53.5461, -113.4938);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(edmonton));

        float zoomLevel = 16.0f; //max is 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, zoomLevel));
    }

    @Override
    public void onDestroy() {
        offlineTestTimer.cancel();
        offlineTestTimer.purge();

        super.onDestroy();
    }

    /**
     * Automatically called when the CreateRideRequestFragment builds a new RideRequest
     * @param newRequest The request created by the dialog fragment
     */
    public void onRideRequestCreated(RideRequest newRequest) {
        OfflineCache.getReference().cacheCurrentRideRequest(newRequest);
        FirebaseManager.getReference().storeRideRequest(newRequest);
        requestDialog = null;
    }

    /**
     * This function handles the markers created when the rider is making a new request and cancels
     */

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

    private void refreshRequests(final boolean isOnline) {
        if (isOnline) {
            if (OfflineCache.getReference().retrieveCurrentRideRequest() == null) {
                FirebaseManager.getReference().fetchRideRequestsWithStatus(RideRequest.Status.PENDING, new FirebaseManager.ReturnValueListener<ArrayList<RideRequest>>() {
                    @Override
                    public void returnValue(ArrayList<RideRequest> value) {
                        mMap.clear();
                        pickupMarker = null;
                        dropoffMarker = null;

                        if (value == null) {
                            return;
                        }

                        for (final RideRequest request : value) {
                            DriverRequestMarker marker = new DriverRequestMarker(request);
                            marker.makeMarker(mMap);
                        }
                    }
                });
            } else {
                FirebaseManager.getReference().fetchRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), new FirebaseManager.ReturnValueListener<RideRequest>() {
                    @Override
                    public void returnValue(RideRequest value) {
                        OfflineCache.getReference().cacheCurrentRideRequest(value);
                        if (value != null && pickupMarker == null && dropoffMarker == null) {
                            mMap.clear();
                            MarkerInfo newMarker = new MarkerInfo();
                            pickupMarker = newMarker.makeMarker("Pickup", true, value.getLocationInfo().getPickup(), mMap);
                            newMarker = new MarkerInfo();
                            dropoffMarker = newMarker.makeMarker("Drop Off", false, value.getLocationInfo().getDropoff(), mMap);
                        }
                    }
                });
            }
        } else {
            if (OfflineCache.getReference().retrieveCurrentRideRequest() == null) {
                mMap.clear();
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (isDriver && OfflineCache.getReference().retrieveCurrentRideRequest() == null) {
            DriverRequestMarker markerInfo = (DriverRequestMarker) marker.getTag();
            new DriverMarkerInfoDialog(markerInfo).show(getChildFragmentManager(), "driver_marker_info_dialog");
            return true;
        } else {
            return false;
        }
    }
}
