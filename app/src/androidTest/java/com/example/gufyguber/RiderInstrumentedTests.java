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
 */

package com.example.gufyguber;

import android.content.Intent;
import android.view.Gravity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static com.example.gufyguber.InstrumentedTestHelpers.*;
import static org.hamcrest.core.StringContains.containsString;

import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Handle some simple instrumented tests that we can do for Riders without Google authentication
 * @author Robert MacGillivray | Mar.24.2020
 */
public class RiderInstrumentedTests {
    private Driver testDriver;
    private Vehicle testVehicle;
    private Rating testRating;
    private Rider testRider;
    private float testFare;
    private RideRequest.Status testStatus;
    private LocationInfo testLocation;
    private TimeInfo testTime;
    private RideRequest testRideRequest;
    private SimpleDateFormat formatter;

    @Rule
    public ActivityTestRule<NavigationActivity> navigationActivityRule = new ActivityTestRule<>(NavigationActivity.class, false, false);

    /**
     * Populate some test data, cache it in the OfflineCache, and bypass the login activity
     */
    @Before
    public void init() {
        testVehicle =  new Vehicle("TestModel", "TestMake", "TestPlate", 1);
        testRating = new Rating(8, 2);
        testDriver = new Driver("1234", "user@test.com", "TestFN", "TestLN", "(123)456-7890", testVehicle, testRating);
        testRider = new Rider("4321", "test@user.com", "FNTest", "LNTest", "(098)765-4321");
        testFare = 13.0f;
        testStatus = RideRequest.Status.CONFIRMED;
        testLocation = new LocationInfo();
        testTime = new TimeInfo();
        testTime.setRequestAcceptedTime();
        testRideRequest = new RideRequest(testRider.getUID(), testDriver.getUID(), testStatus,
                testFare, testLocation, testTime);
        formatter = new SimpleDateFormat("h:mm a, MMMM dd yyyy", Locale.CANADA);

        FirebaseManager.getReference().setTestMode(true);
        OfflineCache.getReference().cacheCurrentUser(testRider);
        OfflineCache.getReference().cacheCurrentRideRequest(testRideRequest);

        Intent intent = new Intent(Intent.ACTION_PICK);
        navigationActivityRule.launchActivity(intent);

        checkMapLoaded();
    }

    /**
     * Clear the offline cache, turn off test mode, and shut down the activity
     */
    @After
    public void cleanup() {
        OfflineCache.getReference().clearCache();
        FirebaseManager.getReference().setTestMode(false);
        navigationActivityRule.finishActivity();
    }

    /**
     * Test that makes sure the drawer menu displays the correct name and email
     */
    @Test
    public void testDrawerInfoDisplay() {
        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // Check the displayed name for accuracy
        onView(withId(R.id.display_name))
                .check(matches(withText(String.format("%s %s", testRider.getFirstName(), testRider.getLastName()))));

        // Check the displayed email for accuracy
        onView(withId(R.id.display_email))
                .check(matches(withText(testRider.getEmail())));

        // Close the drawer and make sure it's closed
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.close())
                .check(matches(isClosed(Gravity.LEFT)));

        // Just kidding. It probably needs more time to close because emulators are slow
        onView(isRoot()).perform(waitFor(1000));
    }

    /**
     * Test that makes sure the profile page displays the correct info
     */
    @Test
    public void testProfileScreen() {
        goToProfileScreenFromAnyScreen();

        // Make sure first name is auto-populated correctly
        onView(withId(R.id.user_first_name))
                .check(matches(withText(testRider.getFirstName())));

        // Make sure last name is auto-populated correctly
        onView(withId(R.id.user_last_name))
                .check(matches(withText(testRider.getLastName())));

        // Make sure phone number is auto-populated correctly
        onView(withId(R.id.rider_phone))
                .check(matches(withText(testRider.getPhoneNumber())));

        // Make sure email is auto-populated correctly
        onView(withId(R.id.rider_email))
                .check(matches(withText(testRider.getEmail())));

        goToMapScreenFromAnyScreen();
    }

    /**
     * Tests trying to navigate to the map from the map
     */
    @Test
    public void testMapToMap() {
        // Extra time for weird test that involves rapid menu switching
        onView(isRoot()).perform(waitFor(1000));
        goToMapScreenFromAnyScreen();
    }

    /**
     * Tests trying to navigate to the profile screen from the profile screen
     */
    @Test
    public void testProfileToProfile() {
        goToProfileScreenFromAnyScreen();
        // Extra time for weird test that involves rapid menu switching
        onView(isRoot()).perform(waitFor(1000));
        goToProfileScreenFromAnyScreen();
    }

    /**
     * Tests trying to navigate to the request screen from the request screen
     */
    @Test
    public void testRequestToRequest() {
        goToRideRequestScreenFromAnyScreen();
        // Extra time for weird test that involves rapid menu switching
        onView(isRoot()).perform(waitFor(1000));
        goToRideRequestScreenFromAnyScreen();
    }

    /**
     * Tests editing the user profile
     */
    @Test
    public void testEditProfileScreen() {
        goToProfileScreenFromAnyScreen();

        // Enable editing the fields
        onView(withId(R.id.edit_profile_button))
                .perform(click());

        String newFirstName = "2" + testRider.getFirstName() + "2";
        String newLastName = "2" + testRider.getLastName() + "2";
        String newPhone = "7 131-313-1313";

        // Give some animations time to finish
        onView(isRoot()).perform(waitFor(1000));

        // Edit the rider's first name
        onView(withId(R.id.user_first_name))
                .perform(clearText())
                .perform(typeText(newFirstName))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newFirstName)));
        // Edit the rider's last name
        onView(withId(R.id.user_last_name))
                .perform(clearText())
                .perform(typeText(newLastName))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newLastName)));
        // Edit the rider's phone number
        onView(withId(R.id.rider_phone))
                .perform(clearText())
                .perform(typeText(newPhone))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newPhone)));

        // Save the new profile information
        onView(withId(R.id.save_profile_button))
                .perform(click());

        // Clicking the saved button sends us back to the map
        checkMapLoaded();

        // See if new values were saved to the cache properly
        Rider cachedRider = (Rider)OfflineCache.getReference().retrieveCurrentUser();
        assert(cachedRider.getFirstName().equalsIgnoreCase(newFirstName));
        assert(cachedRider.getLastName().equalsIgnoreCase(newLastName));
        assert(cachedRider.getPhoneNumber().equalsIgnoreCase(newPhone));

        // Redundancy test to make sure that the following tests still pass after editing
        testRider = cachedRider;
        testDrawerInfoDisplay();
        testProfileScreen();
    }

    /**
     * Test that makes sure the current ride request screen opens and displays the correct info
     */
    @Test
    public void testRideRequestScreen() {
        goToRideRequestScreenFromAnyScreen();

        // Make sure the status message contains the right status
        onView(withId(R.id.ride_status))
                .check(matches(withText(containsString(testRideRequest.getStatus().toString()))));
        // Make sure the request open time is correct
        onView(withId(R.id.user_pickup_time))
                .check(matches(withText(formatter.format(testRideRequest.getTimeInfo().getRequestOpenTime()))));
        // Make sure the request accepted time is correct if we have one
        if (testRideRequest.getTimeInfo().getRequestAcceptedTime() != null) {
            onView(withId(R.id.user_arrival_time))
                    .check(matches(withText(formatter.format(testRideRequest.getTimeInfo().getRequestAcceptedTime()))));
        }
        // Make sure the pickup coordinates are correct
        onView(withId(R.id.user_pickup_location))
                .check(matches(withText(testRideRequest.getLocationInfo().getPickupName())));
        // Make sure the dropoff coordinates are correct
        onView(withId(R.id.user_dropoff_location))
                .check(matches(withText(testRideRequest.getLocationInfo().getDropoffName())));
        // Make sure the fare is correct
        onView(withId(R.id.user_fare))
                .check(matches(withText(String.format("$%.2f", testRideRequest.getOfferedFare()))));
    }

    /**
     * Stress-tests switching between different menu screens
     * Note that each test needs some extra time for animations to complete
     */
    @Test
    public void testMenuSwitching() {
        goToProfileScreenFromAnyScreen();
        onView(isRoot()).perform(waitFor(1000));
        goToRideRequestScreenFromAnyScreen();
        onView(isRoot()).perform(waitFor(1000));
        goToMapScreenFromAnyScreen();
        onView(isRoot()).perform(waitFor(1000));
        goToRideRequestScreenFromAnyScreen();
        onView(isRoot()).perform(waitFor(1000));
        goToProfileScreenFromAnyScreen();
    }

    /**
     * Tests the create ride request fragment and associated map pins
     * There are a ton of manual waits because that's the only way to get it to work
     */
    @Test
    public void testCreateRideRequest() {
        onView(withId(R.id.cancel_fab))
                .perform(click());

        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.cancel_ride_yes_btn))
                .perform(click());

        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.cancelled_rider_button))
                .perform(click());

        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.request_fab))
                .perform(click());

        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.start_location_EditText))
                .perform(click());

        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.user_map))
                .perform(clickPercent(0.25f, 0.25f));

        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.end_location_EditText))
                .perform(click());

        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.user_map))
                .perform(clickPercent(0.75f, 0.75f));

        onView(isRoot()).perform(waitFor(1000));

        // Type a value into the fare field and make sure it's formatted properly
        onView(withId(R.id.fare_EditText))
                .perform(typeText("1347"))
                .check(matches(withText("$13.47")));

        onView(isRoot()).perform(waitFor(1000));

        onView(withId(R.id.create_ride_request_button))
                .perform(click());

        onView(isRoot()).perform(waitFor(1000));

        // Keep a local reference to the new ride request and perform the request test
        testRideRequest = OfflineCache.getReference().retrieveCurrentRideRequest();
        testRideRequestScreen();

        onView(isRoot()).perform(waitFor(1000));
    }
}
