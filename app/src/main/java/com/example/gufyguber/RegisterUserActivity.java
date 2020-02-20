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
    Button register;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String userType = intent.getStringExtra("userType");
        if (userType.equals("Rider")){
            setContentView(R.layout.register_rider);
        }
        else{
            setContentView(R.layout.register_driver);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        username = findViewById(R.id.user_name);
        username.setText(userType); //debug check
        email = findViewById(R.id.email);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        register = findViewById(R.id.register);

        if(userType.equals("Driver")){
            //
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

    }
}
