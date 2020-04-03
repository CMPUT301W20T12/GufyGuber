package com.example.gufyguber.CurrentRequest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.gufyguber.Singletons.FirebaseManager;
import com.example.gufyguber.Utility.GlobalDoubleClickHandler;
import com.example.gufyguber.Singletons.OfflineCache;
import com.example.gufyguber.R;

/**
 * Builds a DialogFragment with two buttons for  user to confirm that they would like to cancel
 * the current ride request. Selecting yes will bring up another Fragment displaying a final
 * confirmation of cancellation.
 */

public class CancelRequestFragment extends DialogFragment {

    private static final String TAG = "CancelRequestFragment";

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
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                getFragmentManager().beginTransaction().remove(CancelRequestFragment.this).commit();
            }
        });

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }

                new com.example.gufyguber.CurrentRequest.CancelFragment().show(getFragmentManager(), "cancel_fragment");
                getFragmentManager().beginTransaction().remove(CancelRequestFragment.this).commit();
                if (OfflineCache.getReference().retrieveCurrentRideRequest() != null) {
                    FirebaseManager.getReference().riderCancelRequest(OfflineCache.getReference().retrieveCurrentRideRequest(), new FirebaseManager.ReturnValueListener<Boolean>() {
                        @Override
                        public void returnValue(Boolean value) {
                            if (!value) {
                                Log.e(TAG, "Error deleting ride.");
                            }
                        }
                    });
                }
            }
        });
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .create();

    }

}