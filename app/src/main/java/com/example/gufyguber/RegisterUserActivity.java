package com.example.gufyguber;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterUserActivity extends AppCompatActivity {
    EditText username;
    EditText email;
    EditText firstName;
    EditText lastName;
    EditText phone;
    EditText password;
    EditText confirmPassword;
    EditText make;
    EditText model;
    EditText plateNumber;
    EditText seatNumber;
    Button register;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        String userType = intent.getStringExtra("userType");
        if (userType.equals("Rider")){
            setContentView(R.layout.register_rider);
        }
        else{
            setContentView(R.layout.register_driver);
            make = findViewById(R.id.make);
            model = findViewById(R.id.model);
            plateNumber = findViewById(R.id.plate_number);
            seatNumber = findViewById(R.id.seat_number);

        }


        username = findViewById(R.id.user_name);
        email = findViewById(R.id.email);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        register = findViewById(R.id.register);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

    }
}
