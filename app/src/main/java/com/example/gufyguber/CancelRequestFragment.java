package com.example.gufyguber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CancelRequestFragment extends DialogFragment {
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.cancel_ride_rider, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);

        return builder
                .setView(view)
                .setTitle("Cancel Request")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CancelFragment().show(getFragmentManager(), "fragment");
                    }
                })
                .create();

    }

}
