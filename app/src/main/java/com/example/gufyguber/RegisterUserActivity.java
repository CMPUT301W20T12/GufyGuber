package com.example.gufyguber;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterUserActivity extends AppCompatActivity {
    private static final String TAG = "EmailPassword";
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

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CollectionReference users = db.collection("users");

        Intent intent = getIntent();
        final String userType = intent.getStringExtra("userType");
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
                createAccount(email.getText().toString(), password.getText().toString());

                HashMap<String, String> userData = new HashMap<>();

                userData.put("username", username.getText().toString());
                userData.put("email", email.getText().toString());
                userData.put("first_name", firstName.getText().toString());
                userData.put("last_name", lastName.getText().toString());
                userData.put("phone", phone.getText().toString());
                userData.put("userType", userType);

                users
                        .document(username.getText().toString())
                        .set(userData)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "User addition successful");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "User addition failed" + e.toString());
                            }
                        });
                finish();
            }
        });
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterUserActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        ArrayList<String> fieldArray = new ArrayList<String>();

        String usernameStr = username.getText().toString();
        fieldArray.add(usernameStr);
        String emailStr = email.getText().toString();
        fieldArray.add(emailStr);
        String firstNameStr = firstName.getText().toString();
        fieldArray.add(firstNameStr);
        String lastNameStr = lastName.getText().toString();
        fieldArray.add(lastNameStr);
        String phoneStr = phone.getText().toString();
        fieldArray.add(phoneStr);
        String passwordStr = password.getText().toString();
        fieldArray.add(passwordStr);
        String confirmPasswordStr = confirmPassword.getText().toString();
        fieldArray.add(confirmPasswordStr);

//        for (String field : fieldArray) {
//            if (TextUtils.isEmpty(field)) {
//
//            }
//        }

        if (TextUtils.isEmpty(emailStr)) {
            email.setError("Required.");
            valid = false;
        } else {
            email.setError(null);
        }

        if (TextUtils.isEmpty(passwordStr)) {
            password.setError("Required.");
            valid = false;
        } else {
            password.setError(null);
        }

        return valid;
    }

}
