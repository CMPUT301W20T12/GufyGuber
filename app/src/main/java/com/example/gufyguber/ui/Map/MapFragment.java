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

import android.content.Intent;
import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.gufyguber.CreateRideRequestFragment;
import com.example.gufyguber.DirectionsManager;
import com.example.gufyguber.Driver;
import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.GenerateQR;
import com.example.gufyguber.GlobalDoubleClickHandler;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.example.gufyguber.ui.CurrentRequest.CancelRequestFragment;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class creates a MapFragment on the NavigationActivity "Map" tab.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback, CreateRideRequestFragment.CreateRideRequestListener,
        CreateRideRequestFragment.CancelCreateRideRequestListener, GoogleMap.OnMarkerClickListener, FirebaseManager.RideRequestListener,
        FirebaseManager.DriverRideRequestCollectionListener, RideRequest.StatusChangedListener{

    private GoogleMap mMap;
    private Marker pickupMarker;
    private Marker dropoffMarker;
    private Polyline routeLine;
    private CreateRideRequestFragment requestDialog;
    private Button request_fab;
    private Button cancel_fab;
    private Button pay_fab;
    private Button arrived_fab;
    private TextView offlineText;
    private Timer offlineTestTimer;
    // Quick reference for the user type we're dealing with
    private boolean isDriver;
    // Tracks the connectivity status to handle returning from being offline
    private boolean wasOffline;

    private ListenerRegistration rideRequestListener;
    private ListenerRegistration allRideRequestListener;

    private static final String TAG = "MapFragment";

    private Address address;

    //for location permissions
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private Boolean mLocationPermissionsGranted = false;
    private Boolean mGPSPermissionsGranted = false;

    //widgets
    private Place mAutocomplete;
    private ImageView mGps;


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

        // Have cached request, no need to refresh
        if (OfflineCache.getReference().retrieveCurrentRideRequest() != null) {
            onRideRequestUpdated(OfflineCache.getReference().retrieveCurrentRideRequest());
        } else {
            // Start by syncing any current requests with Firebase in case the app was closed and opened
            if (isDriver) {
                // For the driver, having the Rider UID will be enough to keep it synced later on with a cheaper query
                FirebaseManager.getReference().fetchRideRequestsWithStatus(RideRequest.Status.ACCEPTED, new FirebaseManager.ReturnValueListener<ArrayList<RideRequest>>() {
                    @Override
                    public void returnValue(ArrayList<RideRequest> value) {
                        if (value == null) {
                            return;
                        }

                        for (RideRequest request : value) {
                            String dUID = request.getDriverUID();
                            if (dUID != null && dUID.equalsIgnoreCase(OfflineCache.getReference().retrieveCurrentUser().getUID())) {
                                OfflineCache.getReference().cacheCurrentRideRequest(request);
                                onRideRequestUpdated(request);
                            }
                        }
                    }
                });
                FirebaseManager.getReference().fetchRideRequestsWithStatus(RideRequest.Status.CONFIRMED, new FirebaseManager.ReturnValueListener<ArrayList<RideRequest>>() {
                    @Override
                    public void returnValue(ArrayList<RideRequest> value) {
                        if (value == null) {
                            return;
                        }

                        for (RideRequest request : value) {
                            String dUID = request.getDriverUID();
                            if (dUID != null && dUID.equalsIgnoreCase(OfflineCache.getReference().retrieveCurrentUser().getUID())) {
                                OfflineCache.getReference().cacheCurrentRideRequest(request);
                                onRideRequestUpdated(request);
                            }
                        }
                    }
                });
                FirebaseManager.getReference().fetchRideRequestsWithStatus(RideRequest.Status.EN_ROUTE, new FirebaseManager.ReturnValueListener<ArrayList<RideRequest>>() {
                    @Override
                    public void returnValue(ArrayList<RideRequest> value) {
                        if (value == null) {
                            return;
                        }

                        for (RideRequest request : value) {
                            String dUID = request.getDriverUID();
                            if (dUID != null && dUID.equalsIgnoreCase(OfflineCache.getReference().retrieveCurrentUser().getUID())) {
                                OfflineCache.getReference().cacheCurrentRideRequest(request);
                                onRideRequestUpdated(request);
                            }
                        }
                    }
                });
                FirebaseManager.getReference().fetchRideRequestsWithStatus(RideRequest.Status.ARRIVED, new FirebaseManager.ReturnValueListener<ArrayList<RideRequest>>() {
                    @Override
                    public void returnValue(ArrayList<RideRequest> value) {
                        if (value == null) {
                            return;
                        }

                        for (RideRequest request : value) {
                            String dUID = request.getDriverUID();
                            if (dUID != null && dUID.equalsIgnoreCase(OfflineCache.getReference().retrieveCurrentUser().getUID())) {
                                OfflineCache.getReference().cacheCurrentRideRequest(request);
                                onRideRequestUpdated(request);
                            }
                        }
                    }
                });

            } else {
                FirebaseManager.getReference().fetchRideRequest(OfflineCache.getReference().retrieveCurrentUser().getUID(), new FirebaseManager.ReturnValueListener<RideRequest>() {
                    @Override
                    public void returnValue(RideRequest value) {
                        OfflineCache.getReference().cacheCurrentRideRequest(value);
                        onRideRequestUpdated(value);
                    }
                });
            }
        }

        OfflineCache.getReference().addRideRequestStatusChangedListener(this);

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

        //______________________________________ AutoComplete Widget ______________________________________

        /* Google Guides : https://developers.google.com/places/android-sdk/autocomplete
        */

        // need to initialize places
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), getString(R.string.api_key), Locale.CANADA);
            PlacesClient placesClient = Places.createClient(getActivity());
        }
        final AutocompleteSupportFragment autocompleteFragment;
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        //Bias in Edmonton (SE,NW)
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(53.415299,-113.674242),
                new LatLng(53.654777, -113.328740)));

        // gives suggestions in Canada in general
        autocompleteFragment.setCountries("CA");

        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.ID,Place.Field.ADDRESS_COMPONENTS,Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
                mAutocomplete = place;

                Log.d(TAG, "Moving camera to selected location");
                moveCamera(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude), DEFAULT_ZOOM);

                if (requestDialog != null) {
                    boolean dirty = false;

                    // Build a string representation to show to users
                    String placeString = String.format("%s - %s", place.getName(), place.getAddress());
                    if (requestDialog.settingStart) {
                        // If we have an active request, it's confusing to show its pins while we do this
                        if (!requestDialog.hasDropoffData()) {
                            removeDropoffFromMap();
                        }
                        requestDialog.setNewPickup(place.getLatLng(), placeString);
                        addPickupToMap(place.getLatLng());
                        dirty = true;
                    }

                    if (requestDialog.settingEnd) {
                        // If we have an active request, it's confusing to show its pins while we do this
                        if (!requestDialog.hasPickupData()) {
                            removePickupFromMap();
                        }
                        requestDialog.setNewDropoff(place.getLatLng(), placeString);
                        addDropoffToMap(place.getLatLng());
                        dirty = true;
                    }

                    if (dirty) {
                        requestDialog.show(getChildFragmentManager(), "create_ride_request");
                        dirty = false;
                    }
                }

                autocompleteFragment.setText("");
            }
            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "And error occurred: " + status);

            }
        });

        // for current location
        mGps = v.findViewById(R.id.ic_gps);
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: clicked gps icon");

                //asking for permissions.
                getLocationPermissions();
                Log.d(TAG,"getLocationPermissions: do we have permissions? " +mLocationPermissionsGranted.toString());
                getGPS();
                Log.d(TAG,"getGPS: do we have permissions? " +mLocationPermissionsGranted.toString());


                if((ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && mGPSPermissionsGranted && mLocationPermissionsGranted) {
                    Log.d(TAG,"onClick: checking permissions");
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    getDeviceLocation();

                }
                else {
                    Log.d(TAG,"onClick: do we have permissions? " +mLocationPermissionsGranted.toString());
                    Toast.makeText(getActivity(), "Please enable location to use this feature", Toast.LENGTH_SHORT).show();

                }
            }
        });

        // makes a button for us to create ride requests (RIDER) from navigation drawer activity default
        request_fab = v.findViewById(R.id.request_fab);
        request_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                if (requestDialog == null) {
                    requestDialog = new CreateRideRequestFragment();
                }
                requestDialog.show(getChildFragmentManager(), "create_ride_request");
            }
        });

        // these buttons will not be visible depending on the ride status, but we set up the click listener's here
        cancel_fab = v.findViewById(R.id.cancel_fab);
        cancel_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }
                new CancelRequestFragment().show(getChildFragmentManager(), "cancel_request_fragment");
            }
        });

        pay_fab = v.findViewById(R.id.pay_fab);
        pay_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }
                Intent qrIntent = new Intent(getActivity(), GenerateQR.class);
                startActivity(qrIntent);
                getActivity().finish();

            }
        });

        arrived_fab = v.findViewById(R.id.arrived_fab);
        arrived_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }
                FirebaseManager.getReference().confirmArrival(OfflineCache.getReference().retrieveCurrentRideRequest(), new FirebaseManager.ReturnValueListener<Boolean>() {
                    @Override
                    public void returnValue(Boolean value) {
                        if (!value) {
                            Log.e(TAG, "Arrival confirmation failed.");
                        }
                    }
                });
            }
        });

        offlineText = v.findViewById(R.id.offline_text);

        if (OfflineCache.getReference().retrieveCurrentRideRequest() == null) {
            request_fab.setVisibility(View.VISIBLE);
            cancel_fab.setVisibility(View.GONE);
            arrived_fab.setVisibility(View.GONE);
            pay_fab.setVisibility(View.GONE);
        }

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

                            // Handle things that need to be dealt with now that we're online again
                            if (wasOffline && isOnline) {
                                updateNavLine();
                            }

                            offlineText.setVisibility(isOnline ? View.GONE : View.VISIBLE);
                            if (OfflineCache.getReference().retrieveCurrentRideRequest() == null && requestDialog == null) {
                                onRideRequestUpdated(null);
                            }

                            wasOffline = !isOnline;
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
        //______________________________________ AutoComplete Widget ______________________________________
        /* Google Guides : https://developers.google.com/places/android-sdk/autocomplete
         */
        // need to initialize places
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), getString(R.string.api_key), Locale.CANADA);
            PlacesClient placesClient = Places.createClient(getActivity());
        }

        final AutocompleteSupportFragment autocompleteFragment;
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Bias in Edmonton (SE,NW)
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(53.415299,-113.674242),
                new LatLng(53.654777, -113.328740)));

        // gives suggestions in Canada in general
        autocompleteFragment.setCountries("CA");

        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.ID,Place.Field.ADDRESS_COMPONENTS,Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getLatLng());
                mAutocomplete = place;

                Log.d(TAG, "Moving camera to selected location");
                moveCamera(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude), DEFAULT_ZOOM);

            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "And error occurred: " + status);

            }
        });

        // for current location feature
        mGps = v.findViewById(R.id.ic_gps);
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: clicked gps icon");

                //asking for permissions.
                getLocationPermissions();
                Log.d(TAG,"getLocationPermissions: do we have permissions? " +mLocationPermissionsGranted.toString());
                getGPS();
                Log.d(TAG,"getGPS: do we have permissions? " +mLocationPermissionsGranted.toString());


                if((ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && mGPSPermissionsGranted && mLocationPermissionsGranted) {
                    Log.d(TAG,"onClick: checking permissions");
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    getDeviceLocation();
                }
                else {
                    Log.d(TAG,"onClick: do we have permissions? " +mLocationPermissionsGranted.toString());
                    Toast.makeText(getActivity(), "Please enable location to use this feature", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

                            // Handle things that need to be dealt with now that we're online again
                            if (wasOffline && isOnline) {
                                updateNavLine();
                            }

                            offlineText.setVisibility(isOnline ? View.GONE : View.VISIBLE);
                            validateCallbacks();

                            wasOffline = !isOnline;
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
        // Need to subscribe a RideRequest listener for Rider view if a request is active and there is no current user
        // This can happen if the rider closes the app/starts a new map fragment with a request open
        if (rideRequestListener == null && OfflineCache.getReference().retrieveCurrentRideRequest() != null) {
            rideRequestListener = FirebaseManager.getReference().listenToRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), this);
        }
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
        mMap = googleMap;

        // If our Firestore async request finished before the map loaded, this will force a UI update
        onRideRequestUpdated(OfflineCache.getReference().retrieveCurrentRideRequest());

        // Use open request markers for initial map zoom
        if (pickupMarker != null && dropoffMarker != null && mMap != null){
            zoomFit();
        }
        // If no request is in progress, and permissions have been granted, use location for initial map zoom
        else if ((ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && mLocationPermissionsGranted && mGPSPermissionsGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            getDeviceLocation();
        }
        // Default Edmonton fallback for initial map zoom
        else {
            // zoom to Edmonton and move the camera on start unless Permissions granted
            LatLng edmonton = new LatLng(53.5461, -113.4938);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton, DEFAULT_ZOOM));
        }


        if (isDriver) {
            mMap.setOnMarkerClickListener(this);
        } else {
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (requestDialog != null) {
                        boolean dirty = false;

                        if (requestDialog.settingStart) {
                            // If we have an active request, it's confusing to show its pins while we do this
                            if (!requestDialog.hasDropoffData()) {
                                removeDropoffFromMap();
                            }
                            requestDialog.setNewPickup(latLng, null);
                            addPickupToMap(latLng);
                            dirty = true;
                        }

                        if (requestDialog.settingEnd) {
                            // If we have an active request, it's confusing to show its pins while we do this
                            if (!requestDialog.hasPickupData()) {
                                removePickupFromMap();
                            }
                            requestDialog.setNewDropoff(latLng, null);
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
    }

    @Override
    public void onDestroy() {
        offlineTestTimer.cancel();
        offlineTestTimer.purge();
        OfflineCache.getReference().removeRideRequestStatusChangedListener(this);

        if (rideRequestListener != null) {
            rideRequestListener.remove();
            rideRequestListener = null;
        }
        if (allRideRequestListener != null) {
            allRideRequestListener.remove();
            allRideRequestListener = null;
        }

        super.onDestroy();
    }

    /**
     * Automatically called when the CreateRideRequestFragment builds a new RideRequest
     * @param newRequest The request created by the dialog fragment
     */
    public void onRideRequestCreated(RideRequest newRequest) {
        FirebaseManager.getReference().storeRideRequest(newRequest);
        OfflineCache.getReference().cacheCurrentRideRequest(newRequest);
        rideRequestListener = FirebaseManager.getReference().listenToRideRequest(newRequest.getRiderUID(), this);
        requestDialog = null;
    }

    /**
     * This function handles the markers created when the rider is making a new request and cancels
     */
    public void onRideRequestCreationCancelled() {
        removePickupFromMap();
        removeDropoffFromMap();
        requestDialog = null;

        RideRequest cachedReference = OfflineCache.getReference().retrieveCurrentRideRequest();
        if (cachedReference != null) {
            addPickupToMap(cachedReference.getLocationInfo().getPickup());
            addDropoffToMap(cachedReference.getLocationInfo().getDropoff());
        }

    }

    private void addPickupToMap(LatLng location) {
        removePickupFromMap();
        pickupMarker = new MarkerInfo().makeMarker("Pickup", true, location, mMap);
        updateNavLine();
        zoomFit();
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
        zoomFit();
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
        if (pickupMarker != null && dropoffMarker != null && mMap != null) {
            DirectionsManager.drawDirectionsPolyline(new LocationInfo(pickupMarker.getPosition(), dropoffMarker.getPosition()), mMap,
                    new FirebaseManager.ReturnValueListener<Polyline>() {
                        @Override
                        public void returnValue(Polyline value) {
                            if (value != null) {
                                // Handles the edge case where another navline is calculated before the first one finishes
                                if (routeLine != null) {
                                    routeLine.remove();
                                }
                                routeLine = value;
                            } else {
                                // Fallback in case of null return
                                routeLine = mMap.addPolyline(new PolylineOptions()
                                        .add(pickupMarker.getPosition())
                                        .add(dropoffMarker.getPosition())
                                        .color(0xFFFF0000));
                            }
                        }
                    });
        }
    }

    /**
     * This function allows the map to zoom to fit both pickup and dropoff markers when the information is filled
     */

    public void zoomFit() {
        /* Stackoverflow post by sharmilee https://stackoverflow.com/users/1759525/sharmilee
           Answer for bounds: https://stackoverflow.com/a/16416817, girish-nair https://stackoverflow.com/users/1231359/girish-nair
           Answer for camera update: https://stackoverflow.com/questions/16416041/zoom-to-fit-all-markers-on-map-google-maps-v2#comment33527887_16416817, rahul sainani https://stackoverflow.com/users/1262089/rahul-sainani
           This makes the camera zoom appropriately to fit markers
        */
        Log.d(TAG, "zoomFit: initializing ");

        if (pickupMarker == null || dropoffMarker == null || mMap == null) {
            Log.d(TAG, "zoomFit aborted due to missing marker or map. Expected behaviour.");
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(pickupMarker.getPosition());
        builder.include(dropoffMarker.getPosition());

        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;

        // i2 = padding hard coded, because anthony henday is a thing
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, width,width, 200);

        mMap.animateCamera(update);
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

    /**
     * Refreshes the pending ride request pins in the map based on current data (drivers only)
     */
    private void validateCallbacks() {
        if (OfflineCache.getReference().retrieveCurrentRideRequest() != null && rideRequestListener == null) {
            rideRequestListener = FirebaseManager.getReference().listenToRideRequest(OfflineCache.getReference().retrieveCurrentRideRequest().getRiderUID(), this);
        }

        if (allRideRequestListener == null) {
            allRideRequestListener = FirebaseManager.getReference().listenToAllRideRequests(this);
        }
    }

    @Override
    public void onRideRequestUpdated(RideRequest updatedValue) {
        if (mMap == null) {
            Log.e(TAG, "Map shouldn't be null.");
            return;
        }

        if (isDriver && updatedValue == null) {
            mMap.clear();
            if (rideRequestListener != null) {
                rideRequestListener.remove();
                rideRequestListener = null;
            }
            return;
        }

        if (!isDriver && updatedValue == null) {
            request_fab.setVisibility(View.VISIBLE);
            if (cancel_fab.getVisibility() == View.VISIBLE) {
                cancel_fab.setVisibility(View.GONE);
            }
            if (pay_fab.getVisibility() == View.VISIBLE) {
                pay_fab.setVisibility(View.GONE);
            }
            if (arrived_fab.getVisibility() == View.VISIBLE) {
                arrived_fab.setVisibility(View.GONE);
            }
            removePickupFromMap();
            removeDropoffFromMap();

            if (rideRequestListener != null) {
                rideRequestListener.remove();
                rideRequestListener = null;
            }
            return;
        }

        // Driver has just accepted a request, need to clear the open request markers
        if (isDriver && pickupMarker == null && dropoffMarker == null) {
            mMap.clear();
        }

        // If we have a pickup point and there isn't a marker for it, or that point has moved, update it
        if (updatedValue.getLocationInfo().getPickup() != null) {
            if (pickupMarker == null ||
                    pickupMarker.getPosition().latitude != updatedValue.getLocationInfo().getPickup().latitude ||
                    pickupMarker.getPosition().longitude != updatedValue.getLocationInfo().getPickup().longitude) {
                addPickupToMap(updatedValue.getLocationInfo().getPickup());
            }
        }

        // If we have a dropoff point and there isn' a marker for it, or that point has moved, update it
        if (updatedValue.getLocationInfo().getDropoff() != null) {
            if (dropoffMarker == null ||
                    dropoffMarker.getPosition().latitude != updatedValue.getLocationInfo().getDropoff().latitude ||
                    dropoffMarker.getPosition().longitude != updatedValue.getLocationInfo().getDropoff().longitude) {
                addDropoffToMap(updatedValue.getLocationInfo().getDropoff());
            }
        }

        // If user is a rider, we update what FAB is available for them depending
        // on the current ride status
        if(!isDriver) {
            switch (updatedValue.getStatus()) {
                // freeeee fallin'
                case PENDING:
                case ACCEPTED:
                case CONFIRMED:
                    // any ride status between pending and en route should have a cancel button
                    request_fab.setVisibility(View.GONE);
                    cancel_fab.setVisibility(View.VISIBLE);
                    break;
                case EN_ROUTE:
                    // when en route, rider will have button to confirm arrival
                    request_fab.setVisibility(View.GONE);
                    cancel_fab.setVisibility(View.GONE);
                    arrived_fab.setVisibility(View.VISIBLE);
                    break;
                case ARRIVED:
                    // pay when arrived
                    request_fab.setVisibility(View.GONE);
                    arrived_fab.setVisibility(View.GONE);
                    pay_fab.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public void onRideRequestsUpdated(ArrayList<RideRequest> rideRequests) {
        if (isDriver && OfflineCache.getReference().retrieveCurrentRideRequest() == null) {
            if (mMap == null) {
                return;
            }

            mMap.clear();
            pickupMarker = null;
            dropoffMarker = null;

            if (rideRequests == null) {
                return;
            }

            // Add a marker to the map for each pending ride request (at the request start location)
            for (final RideRequest request : rideRequests) {
                new DriverRequestMarker(request).makeMarker(mMap);
            }
        }
    }

    /**
     * Used to catch cases where the map is visible and needs to react to a status change
     * @param newStatus The new status of the current ride request
     */
    @Override
    public void onStatusChanged(RideRequest.Status newStatus) {
        if (isDriver) {
            if (newStatus == RideRequest.Status.CANCELLED || newStatus == RideRequest.Status.PENDING || newStatus == RideRequest.Status.COMPLETED) {
                FirebaseManager.getReference().fetchRideRequestsWithStatus(RideRequest.Status.PENDING, new FirebaseManager.ReturnValueListener<ArrayList<RideRequest>>() {
                    @Override
                    public void returnValue(ArrayList<RideRequest> value) {
                        OfflineCache.getReference().clearCurrentRideRequest();
                        onRideRequestsUpdated(value);
                    }
                });
            }
        }
    }


    //___________________________  CURRENT LOCATION FEATURES _______________________________

    /**
     *  This function allows the camera to move to the user's current location when the location button is pressed
     */

    private void getDeviceLocation() {
        /*
            GitHub: Mitchtabian Repository: https://github.com/mitchtabian/Google-Maps-Google-Places
         */
        Log.d(TAG,"getDeviceLocation: getting the device's current location");
        //vars
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            if((ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&mLocationPermissionsGranted && mGPSPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())   {
                            Log.d(TAG,"onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            if (currentLocation == null){
                                Log.d(TAG,"onComplete: location services is turned off");
                                Toast.makeText(getActivity(), "Please enable location to use this feature", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            }
                        }
                        else {
                            Log.d(TAG,"onComplete: current location is null");
                            Toast.makeText(getActivity(), "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    /**
     *  This function handles all the camera movement when called
     * @param latLng where you want the camera to update to
     * @param zoom level of zoom (DEFAULT_ZOOM is 16)
     */

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG,"moveCamera: moving camera to: lat:" + latLng.latitude + ", lng: " + latLng.longitude);

        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng,DEFAULT_ZOOM);
        mMap.animateCamera(location);

    }

    //____________________________________ PERMISSIONS ______________________________________

    /**
     * This function asks the user to allow OUR APP to get location permissions
     */

    private void getLocationPermissions() {
        /* Google Guide: https://developers.google.com/android/guides/permissions
           This asks the user to allow the app to use location services
        */


        Log.d(TAG,"getLocationPermissions: getting the APP's location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getActivity(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                Log.d(TAG,"getLocationPermissions: Location permissions granted");

            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        else {
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    /**
     * This function checks if we have permissions and will initialize the map
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /* Stackoverflow post by mufri-a https://stackoverflow.com/users/4016369/mufri-a
           Answer: https://stackoverflow.com/a/50796199, arul pandian https://stackoverflow.com/users/1688068/arul-pandian
           This checks all of our permissions if they are granted
        */

        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG,"onRequestPermissionsResult: do we have permissions? " +mLocationPermissionsGranted.toString());
                            return;
                        }
                        mLocationPermissionsGranted = true;
                        onMapReady(mMap);
                    }
                }
            }
        }
    }

    /**
     * This function asks the user for location permission before map usage. (for current location)
     */

    public void getGPS() {
        /* Stackoverflow post by mufri-a https://stackoverflow.com/users/4016369/mufri-a
           Answer: https://stackoverflow.com/a/50796199, arul pandian https://stackoverflow.com/users/1688068/arul-pandian
           This asks the device for location permissions
        */
        Log.d(TAG,"askLocationPermissions: This gets the permission for GPS");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);

        Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build());

        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    mGPSPermissionsGranted = true;
                    Log.d(TAG,"getGPSPermissions: GPS permissions granted");

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            mGPSPermissionsGranted = false;
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        getActivity(),
                                        101);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            mGPSPermissionsGranted = false;
                            break;
                    }
                }
            }
        });
    }
}
