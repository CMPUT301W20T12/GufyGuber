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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
    private Polyline routeLine;
    private CreateRideRequestFragment requestDialog;
    private FloatingActionButton fab;
    private TextView offlineText;
    private Timer offlineTestTimer;
    private boolean isDriver;

    private static final String TAG = "MapFragment";

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
                                FirebaseManager.getReference().fetchRideRequest(OfflineCache.getReference().retrieveCurrentUser().getUID(), new FirebaseManager.ReturnValueListener<RideRequest>() {
                                    @Override
                                    public void returnValue(RideRequest value) {
                                        OfflineCache.getReference().cacheCurrentRideRequest(value);
                                        RideRequest request = OfflineCache.getReference().retrieveCurrentRideRequest();
                                        if (request == null) {
                                            if (requestDialog == null) {
                                                removePickupFromMap();
                                                removeDropoffFromMap();
                                            }
                                        } else {
                                            if (request.getLocationInfo().getPickup() != null) {
                                                if (pickupMarker == null ||
                                                        pickupMarker.getPosition().latitude != request.getLocationInfo().getPickup().latitude ||
                                                        pickupMarker.getPosition().longitude != request.getLocationInfo().getPickup().longitude) {
                                                    addPickupToMap(request.getLocationInfo().getPickup());
                                                }
                                            }
                                            if (request.getLocationInfo().getDropoff() != null) {
                                                if (dropoffMarker == null ||
                                                        dropoffMarker.getPosition().latitude != request.getLocationInfo().getDropoff().latitude ||
                                                        dropoffMarker.getPosition().longitude != request.getLocationInfo().getDropoff().longitude) {
                                                    addDropoffToMap(request.getLocationInfo().getDropoff());
                                                }
                                            }
                                        }
                                    }
                                });
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
                        addPickupToMap(latLng);
                        dirty = true;
                    }

                    if (requestDialog.settingEnd) {
                        requestDialog.setNewDropoff(latLng);
                        addDropoffToMap(latLng);
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
        removePickupFromMap();
        removeDropoffFromMap();
        requestDialog = null;
    }

    /**
     * Refreshes the pins in the map based on current data (drivers only)
     * @param isOnline Used to determine whether to lean into cached data or not
     */
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

                        // Add a marker to the map for each pending ride request (at the request start location)
                        for (final RideRequest request : value) {
                            new DriverRequestMarker(request).makeMarker(mMap);
                        }
                    }
                });
            } else {
                FirebaseManager.getReference().fetchRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), new FirebaseManager.ReturnValueListener<RideRequest>() {
                    @Override
                    public void returnValue(RideRequest value) {
                        if (pickupMarker == null && dropoffMarker == null) {
                            mMap.clear();
                        }

                        OfflineCache.getReference().cacheCurrentRideRequest(value);
                        if (value != null) {
                            if (pickupMarker == null ||
                                    pickupMarker.getPosition().latitude != value.getLocationInfo().getPickup().latitude ||
                                    pickupMarker.getPosition().longitude != value.getLocationInfo().getPickup().longitude) {
                                addPickupToMap(value.getLocationInfo().getPickup());
                            }
                            if (dropoffMarker == null ||
                                    dropoffMarker.getPosition().latitude != value.getLocationInfo().getDropoff().latitude ||
                                    dropoffMarker.getPosition().longitude != value.getLocationInfo().getDropoff().longitude) {
                                addDropoffToMap(value.getLocationInfo().getDropoff());
                            }
                        } else {
                            removePickupFromMap();
                            removeDropoffFromMap();
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

    private void addPickupToMap(LatLng location) {
        removePickupFromMap();
        pickupMarker = new MarkerInfo().makeMarker("Pickup", true, location, mMap);
        updateNavLine();
    }

    private void removePickupFromMap() {
        if (pickupMarker != null) {
            pickupMarker.remove();
            pickupMarker = null;
            updateNavLine();
        }
    }

    private void addDropoffToMap(LatLng location) {
        removeDropoffFromMap();
        dropoffMarker = new MarkerInfo().makeMarker("Drop Off", false, location, mMap);
        updateNavLine();
    }

    private void removeDropoffFromMap() {
        if (dropoffMarker != null) {
            dropoffMarker.remove();
            dropoffMarker = null;
            updateNavLine();
        }
    }

    /**
     * Removes the current nav line and adds a new one as appropriate
     */
    private void updateNavLine() {
        if (routeLine != null) {
            routeLine.remove();
            routeLine = null;
        }
        if (pickupMarker != null && dropoffMarker != null) {
            routeLine = mMap.addPolyline(new PolylineOptions()
                    .add(pickupMarker.getPosition())
                    .add(dropoffMarker.getPosition())
                    .color(0xFFFF0000));
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
