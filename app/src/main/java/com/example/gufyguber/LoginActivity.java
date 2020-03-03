///*
// * Copyright (c) 2020  GufyGuber. All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// *
// * LoginActivity.java
// *
// * Last edit: dalton, 02/03/20 4:01 PM
// *
// * Version
// */
//
//package com.example.gufyguber;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.SignInButton;
//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//
///**
// * App activity displayed when the app first launches. Allows user to enter email and password to
// * sign in, register a new account. If the user requests to make a new account, they will be given
// * an option to register a RIDER ar DRIVER account via a dialog fragment.
// *
// * @see UserTypeFragment
// */
//public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
//    private static final String TAG = "SignInActivity";
//    private static final int RC_SIGN_IN = 9001;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        final Button loginButton = findViewById(R.id.login);
//        final Button registerButton = findViewById(R.id.register);
//        SignInButton signInButton = findViewById(R.id.sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_STANDARD);
//
//        GoogleSignInClient mGoogleSignInClient;
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        findViewById(R.id.sign_in_button).setOnClickListener(this);
//
////        registerButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                new UserTypeFragment().show(getSupportFragmentManager(), "USER_TYPE");
////            }
////        });
////
////        loginButton.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                //
////            }
////        });
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
////        updateUI(account);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            handleSignInResult(task);
//        }
//    }
////
//    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
////        try {
////            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
////
////            // Signed in successfully, show authenticated UI.
////            updateUI(account);
////        } catch (ApiException e) {
////            // The ApiException status code indicates the detailed failure reason.
////            // Please refer to the GoogleSignInStatusCodes class reference for more information.
////            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
////            updateUI(null);
////        }
////    }
//
//    // [START signIn]
////    private void signIn() {
////        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
////        startActivityForResult(signInIntent, RC_SIGN_IN);
////    }
//    // [END signIn]
//
//    // [START signOut]
////    private void signOut() {
////        mGoogleSignInClient.signOut()
////                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
////                    @Override
////                    public void onComplete(@NonNull Task<Void> task) {
////                        // [START_EXCLUDE]
////                        updateUI(null);
////                        // [END_EXCLUDE]
////                    }
////                });
////    }
//    // [END signOut]
//
//    // [START revokeAccess]
////    private void revokeAccess() {
////        mGoogleSignInClient.revokeAccess()
////                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
////                    @Override
////                    public void onComplete(@NonNull Task<Void> task) {
////                        // [START_EXCLUDE]
////                        updateUI(null);
////                        // [END_EXCLUDE]
////                    }
////                });
////    }
//    // [END revokeAccess]
//
////    private void updateUI(@Nullable GoogleSignInAccount account) {
////        if (account != null) {
////            mStatusTextView.setText(getString(R.string.signed_in_fmt, account.getDisplayName()));
////
////            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
////            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
////        } else {
////            mStatusTextView.setText(R.string.signed_out);
////
////            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
////            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
////        }
////    }
////
////    @Override
////    public void onClick(View v) {
////        switch (v.getId()) {
////            case R.id.sign_in_button:
////                signIn();
////                break;
////            case R.id.sign_out_button:
////                signOut();
////                break;
////            case R.id.disconnect_button:
////                revokeAccess();
////                break;
//        }
//    }
//}
//
//}
