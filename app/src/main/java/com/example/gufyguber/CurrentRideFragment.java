package com.example.gufyguber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.type.LatLng;

import java.util.Date;

/**
 * This creates the dialog fragment that pop's up when a user would like to view their current ride status.
 * TextViews are populated from rideRequest and timeInfo classes.
 */

public class CurrentRideFragment extends DialogFragment {

    private TextView userDestination;
    private TextView userPickupTime;
    private TextView userArrivalTime;
    private TextView userPickupLocation;
    private TextView userDropoffLocation;

    private Button backBtn;
    private Button cancelBtn;

    private String riderUID;
    private float offeredFare;
    private LatLng pickupLocation;
    private LatLng dropoffLocation;
    private Date pickupTime;
    private Date dropoffTime;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.current_ride_rider, null);

        userDestination = view.findViewById(R.id.user_destination);
        userPickupTime = view.findViewById(R.id.user_pickup_time);
        userArrivalTime = view.findViewById(R.id.user_arrival_time);
        userPickupLocation = view.findViewById(R.id.user_pickup_location);
        userDropoffLocation = view.findViewById(R.id.user_dropoff_location);
        backBtn = view.findViewById(R.id.back_btn);
        cancelBtn = view.findViewById(R.id.cancel_btn);


        pickupLocation = LatLng.newBuilder().setLatitude(13).setLongitude(13).build();
        dropoffLocation = LatLng.newBuilder().setLatitude(15).setLongitude(20).build();

        RideRequest rideRequest = new RideRequest(riderUID, offeredFare, pickupLocation, dropoffLocation);
        TimeInfo timeInfo = rideRequest.getTimeInfo();

        timeInfo.setRequestAcceptedTime();
        timeInfo.setRequestCompletedTime();


        userPickupLocation.setText(pickupLocation.toString());
        userDropoffLocation.setText(dropoffLocation.toString());
        userDestination.setText(dropoffLocation.toString());

        userPickupTime.setText(timeInfo.getRequestAcceptedTime().toString());
        userArrivalTime.setText(timeInfo.getRequestCompletedTime().toString());




        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DriverInfoFragment().show(getFragmentManager(), "driver_info_fragment");
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new CancelRequestFragment().show(getFragmentManager(), "cancel_request_fragment");
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .create();

    }
}

