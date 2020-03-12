/*
 * Copyright (c) 2020  GufyGuber. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * SignInActivity.java
 *
 * Last edit: dalton, 02/03/20 11:12 PM
 *
 * Version
 */

package com.example.gufyguber;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Activity to allow user to login to the App via Google. If the user has
 * previously signed in with a registered account, this activity will redirect
 * them to the map screen. If the user has not registered, a fragment will
 * display allowing them to select their user type (driver or rider) and redirect
 * them to the registration screen
 *
 * @author dalton, harrison
 * @see UserTypeFragment
 * @see RegisterUserActivity
 * @version 1.1
 */
public class SignInActivity extends AppCompatActivity {

    // Activity tag and request code
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;    // Just pick a random number?

    // Declare firebase authorization
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private SignInButton signInButton;
    private Button signOutButton;
    private FirebaseManager firebaseManager = FirebaseManager.getReference();

    private static SignInActivity reference;
    public static SignInActivity getReference() {
        return reference;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        reference = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_sign_in);

        // View for signed in status - can maybe delete this depending on how we implement map view?
        mStatusTextView = findViewById(R.id.status);

        // Button listeners and onClick methods
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });


        // Configure Google sign-in to request the user's ID, email address,
        GoogleSignInOptions gso = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Pass server's client ID to requestIdToken method
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Use the WIDE button and LIGHT theme for the Google button
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_LIGHT);

        // Initialize firebase authorization and firestore instance
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
    }

    /**
     * Method to check if a Google Sign In account is already signed in, and
     * updates the UI accordingly.
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already
        // signed in the GoogleSignInAccount will be non-null.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        updateUI(currentUser);

        if (currentUser != null) {
            FirebaseManager.getReference().checkUser(currentUser.getUid(), new FirebaseManager.ReturnValueListener<Boolean>() {
                @Override
                public void returnValue(Boolean value) {
                    if (value) {
                        goToMapView();
                    }
                }
            });
        }
    }

    /**
     * Sign the user in.
     * Called by clicking the Google Sign In button.
     */
    private void signIn() {
        // Get sign in intent from the Google Client and start activity for
        // the result code
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Sign the user out of the firebase project and the Google Sign In Client.
     */
    public void signOut() {
        // Sign the user out of both firebase and the Google Client
        mFirebaseAuth.signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // update the UI to not signed in
                                updateUI(null);
                            }
                        });
    }

    /**
     * Receive result from signIn() method.
     * @param requestCode
     *  Integer request code from StartActivityForResult(...) in signIn()
     * @param resultCode
     *  Result code returned by child activity
     * @param data
     *  Intent from the signIn() method
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        // GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to
            // attach a listener.
            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                // Sign into firebase with google info
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Sign in failed", e);
                updateUI(null);
            }
        }
    }

    /**
     * Authorize the Google Sign In account with the app firebase project.
     * If account is authorized, the method will qeury the users firestore
     * database to look for the account email to shekc if the user has completed
     * registration.
     * If the document is not found, they will be directed to
     * finish the sign up process.
     * If the document exsits, the user will be directed to their user type map
     * view.
     * @param account
     *  GoogleSignInAccount from the onActivityResult() method
     */
    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());

        // Get credentials from Google account
        AuthCredential credential = GoogleAuthProvider.getCredential(
                account.getIdToken(), null);

        // Try to authorize user in firebase
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If successful, get user details (email, name)
                            Log.d(TAG, "signInWithCredential: success");
                            final FirebaseUser user = mFirebaseAuth.getCurrentUser();

                            // use FireBaseManager to check if user is registered
                            firebaseManager.checkUser(user.getUid(), new FirebaseManager.ReturnValueListener<Boolean>() {
                                @Override
                                public void returnValue(Boolean value) {
                                    boolean userExists = value.booleanValue();
                                    if (userExists){
                                        Log.d("ACCNT", "Sending user to map screen");
                                        updateUI(user);
                                        goToMapView();
                                    } else {
                                        Log.d("ACCNT", "Sending user to sign up");
                                        Bundle bundle = new Bundle();
                                        // pass account info into fragment to auto-pop some details in registration form
                                        bundle.putString("UID", user.getUid());
                                        bundle.putString("email", account.getEmail());
                                        bundle.putString("firstName", account.getGivenName());
                                        bundle.putString("lastName", account.getFamilyName());
                                        UserTypeFragment mUserTypeFragment = new UserTypeFragment();
                                        mUserTypeFragment.setArguments(bundle);
                                        mUserTypeFragment.show(getSupportFragmentManager(), "USER_TYPE");
                                    }
                                }
                            });
                        } else {
                            Log.w(TAG, "signInWithCredential: failure");
                            Snackbar.make(findViewById(R.id.main_layout),
                                    "Authentication Failed.",
                                    Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    /**
     * Update the UI depending on whether user is Null or not.
     * @param user
     *  The authorized Firebase user that is logged into the app, or null
     */
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            mStatusTextView.setText(getString(R.string.signed_in_fmt,
                    user.getDisplayName()));

            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(
                    View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

    /**
     * Goes to the app's main view
     */
    private void goToMapView() {
        Intent openNavigation = new Intent(this, NavigationActivity.class);
        startActivity(openNavigation);
    }
}
