package com.example.gufyguber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
                new CancelFragment().show(getFragmentManager(), "cancel_fragment");
            }
        });
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .create();

    }

}
