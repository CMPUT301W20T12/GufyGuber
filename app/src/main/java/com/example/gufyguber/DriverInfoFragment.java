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
 * This creates the dialog fragment that pop's up when a user would like to view the information of the driver.
 */

public class DriverInfoFragment extends DialogFragment {

    private Button doneBtn;
    private TextView driverName;
    private TextView driverCar;
    private TextView driverPhone;
    private TextView driverEmail;
    private TextView driverRating;

    private String username = "woldegio";
    private String email = "woldegio@ualberta.ca";
    private String firstName = "Nahome";
    private String lastName = "Wolde-Giorgis";
    private String phoneNumber = "7809938581";

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.driver_info, null);

        doneBtn = view.findViewById(R.id.driver_info_button);
        driverName = view.findViewById(R.id.driver_name);
        driverCar = view.findViewById(R.id.driver_car);
        driverPhone = view.findViewById(R.id.driver_phone);
        driverEmail = view.findViewById(R.id.driver_email);
        driverRating = view.findViewById(R.id.driver_rating);


        Driver driver = new Driver(username, email, firstName, lastName, phoneNumber);

        /*
        username = driver.getUsername();
        email = driver.getEmail();
        firstName = driver.getFirstName();
        lastName = driver.getLastName();
        phoneNumber = driver.getPhoneNumber();

         */

        driverName.setText(firstName + ' ' + lastName);
        driverPhone.setText(phoneNumber);
        driverEmail.setText(email);


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().remove(DriverInfoFragment.this).commit();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .create();

    }
}
