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
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import androidx.fragment.app.FragmentManager;

import com.example.gufyguber.CreateRideRequestFragment;
import com.example.gufyguber.DirectionsManager;
import com.example.gufyguber.Driver;
import com.example.gufyguber.FirebaseManager;
import com.example.gufyguber.GlobalDoubleClickHandler;
import com.example.gufyguber.LocationInfo;
import com.example.gufyguber.OfflineCache;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;
import com.example.gufyguber.Rider;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private Button fab;
    private TextView offlineText;
    private Timer offlineTestTimer;
    private boolean isDriver;

    private ListenerRegistration rideRequestListener;
    private ListenerRegistration allRideRequestListener;

    private static final String TAG = "MapFragment";

    private Address address;

    //for permissions
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    //widgets
    private Place mAutocomplete;
    private ImageView mGps;

    private Boolean mLocationPermissionsGranted = false;
    private Boolean mGPSPermissionsGranted = false;

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

        // need to initialize places
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), getString(R.string.api_key), Locale.CANADA);
            PlacesClient placesClient = Places.createClient(getActivity());
        }

        AutocompleteSupportFragment autocompleteFragment;
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Bias in Edmonton (SE,NW)
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(53.415299,-113.674242),
                new LatLng(53.654777, -113.328740)));

        // gives suggestions in Canada in general
        autocompleteFragment.setCountries("CA");

        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.ID,Place.Field.ADDRESS_COMPONENTS));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: + place.getName()" + ", " + place.getId());
                mAutocomplete = place;

                Address searchAddress = geoLocate();
                LatLng latLng = new LatLng(searchAddress.getLatitude(),searchAddress.getLongitude());

                if (requestDialog != null) {
                    boolean dirty = false;
                    if (requestDialog.settingStart) {
                        requestDialog.setNewPickup(latLng);
                        if (pickupMarker != null) {
                            pickupMarker.remove(); // replaces old pickup marker
                        }
                        MarkerInfo newMarker = new MarkerInfo();
                        pickupMarker = newMarker.makeMarker("Pickup", true, latLng, mMap);
                        dirty = true;
                    }

                    if (requestDialog.settingEnd) {
                        requestDialog.setNewDropoff(latLng);
                        if (dropoffMarker != null) {
                            dropoffMarker.remove(); //replaces old dropoff marker
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


                if((ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) && mGPSPermissionsGranted) {
                    Log.d(TAG,"onClick: checking permissions");
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    getDeviceLocation();
                }
                else {
                    Log.d(TAG,"onClick: do we have permissions? " +mLocationPermissionsGranted.toString());
                }
            }
        });

        // makes a button for us to create ride requests (RIDER) from navigation drawer activity default

        offlineText = v.findViewById(R.id.offline_text);
        fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
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
                            if (OfflineCache.getReference().retrieveCurrentRideRequest() == null && requestDialog == null) {
                                onRideRequestUpdated(null);
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
        //______________________________________ AutoComplete Widget ______________________________________

        // need to initialize places
        if (!Places.isInitialized()) {
            Places.initialize(getActivity(), getString(R.string.api_key), Locale.CANADA);
            PlacesClient placesClient = Places.createClient(getActivity());
        }

        AutocompleteSupportFragment autocompleteFragment;
        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //Bias in Edmonton (SE,NW)
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(53.415299,-113.674242),
                new LatLng(53.654777, -113.328740)));

        // gives suggestions in Canada in general
        autocompleteFragment.setCountries("CA");

        //specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,Place.Field.ID,Place.Field.ADDRESS_COMPONENTS));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place: + place.getName()" + ", " + place.getId());
                mAutocomplete = place;
                geoLocate();
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


                if(mLocationPermissionsGranted && mGPSPermissionsGranted) {
                    Log.d(TAG,"onClick: checking permissions");
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    getDeviceLocation();
                }
                else {
                    Log.d(TAG,"onClick: do we have permissions? " +mLocationPermissionsGranted.toString());
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
                            offlineText.setVisibility(isOnline ? View.GONE : View.VISIBLE);
                            validateCallbacks();
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
        mMap = googleMap;

        // If our Firestore async request finished before the map loaded, this will force a UI update
        onRideRequestUpdated(OfflineCache.getReference().retrieveCurrentRideRequest());

        // Checks the user's permissions beforehand, so on start the map would automatically start at your current location
        if(mLocationPermissionsGranted && mGPSPermissionsGranted){
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            getDeviceLocation();

        }
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
                            requestDialog.setNewPickup(latLng);
                            addPickupToMap(latLng);
                            dirty = true;
                        }

                        if (requestDialog.settingEnd) {
                            // If we have an active request, it's confusing to show its pins while we do this
                            if (!requestDialog.hasPickupData()) {
                                removePickupFromMap();
                            }
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

        if (updatedValue == null) {
            removePickupFromMap();
            removeDropoffFromMap();

            if (rideRequestListener != null) {
                rideRequestListener.remove();
                rideRequestListener = null;
            }

            if (mMap != null) {
                mMap.clear();
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


    //___________________________ GEOLOCATION / CURRENT LOCATION FEATURES _______________________________

    /**
     *  This function allows the user to input a name and retrieve the address in the search bar
     *  Also moves the camera to the searched location
     * @return
     * returns the Address value
     */

    private Address geoLocate(){
        Log.d(TAG,"geoLocate: geolocating");

        String autoSearch = mAutocomplete.getName();

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            // only looking for one result
            list = geocoder.getFromLocationName(autoSearch, 1);

        }
        catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());

        }
        // that means we have some requests
        if(list.size() > 0){
            // made final for request fragment
            address = list.get(0);
            Log.d(TAG,"goeLocate: found a location: " + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM);
        }
        return address;
    }

    /**
     *  This function allows the camera to move to the user's current location when the location button is pressed
     */

    private void getDeviceLocation() {
        Log.d(TAG,"getDeviceLocation: getting the device's current location");
        //vars
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            if(mLocationPermissionsGranted && mGPSPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful())   {
                            Log.d(TAG,"onComplete: found location");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM);

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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG,"onRequestPermissionsResult: do we have permissions? " +mLocationPermissionsGranted.toString());
                            Toast.makeText(getActivity(), "Please allow us to use your location",Toast.LENGTH_SHORT).show();
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
        Log.d(TAG,"askLocationPermissions: This gets the permission for GPS");
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new
                LocationSettingsRequest.Builder();
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
