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

/**
 * This creates the dialog fragment that pop's up when a user would like to view their current ride status.
 */

public class CurrentRideFragment extends DialogFragment {

    private TextView userDestination;
    private TextView userPickupTime;
    private TextView userArrivalTime;
    private TextView userPickupLocation;
    private TextView userDropoffLocation;

    private Button backBtn;
    private Button cancelBtn;


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

