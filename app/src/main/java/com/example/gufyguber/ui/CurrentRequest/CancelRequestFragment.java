package com.example.gufyguber.ui.CurrentRequest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.LoginActivity;
import com.example.gufyguber.NavigationActivity;
import com.example.gufyguber.R;
import com.example.gufyguber.RideRequest;

/**
 * Builds a DialogFragment with two buttons for  user to confirm that they would like to cancel
 * the current ride request. Selecting yes will bring up another Fragment displaying a final
 * confirmation of cancellation.
 */

public class CancelRequestFragment extends DialogFragment {

    private Button noBtn;
    private Button yesBtn;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.cancel_ride_rider, null);

        noBtn = view.findViewById(R.id.cancel_rider_no_btn);
        yesBtn = view.findViewById(R.id.cancel_ride_yes_btn);

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().remove(CancelRequestFragment.this).commit();

            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new com.example.gufyguber.ui.CurrentRequest.CancelFragment().show(getFragmentManager(), "cancel_fragment");
                getFragmentManager().beginTransaction().remove(CancelRequestFragment.this).commit();
                RideRequest currentRequest = RideRequest.getCurrentRideRequest();
                if (currentRequest != null) {
                    currentRequest.cancelRideRequest();
                }
            }
        });
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .create();

    }

}