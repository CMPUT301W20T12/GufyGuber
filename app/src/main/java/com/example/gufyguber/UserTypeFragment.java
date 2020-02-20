package com.example.gufyguber;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class UserTypeFragment extends DialogFragment {

    private Button riderButton;
    private Button driverButton;
    private String userType;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.user_type_fragment, null);
        riderButton = view.findViewById(R.id.rider_button);
        driverButton = view.findViewById(R.id.driver_button);

        riderButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                userType = "Rider";
                Intent intent  = new Intent(getActivity(), RegisterUserActivity.class);
                intent.putExtra("userType", userType);
                startActivity(intent);
            }
        });

        driverButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                userType = "Driver";
                Intent intent  = new Intent(getActivity(), RegisterUserActivity.class);
                intent.putExtra("userType", userType);
                startActivity(intent);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        return builder
                .setView(view)
                .create();
    }
}
