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
import android.util.Log;
import android.view.Gravity;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.DrawerActions;
import static androidx.test.espresso.contrib.DrawerMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static com.example.gufyguber.InstrumentedTestHelpers.*;
import static org.hamcrest.core.StringContains.containsString;

import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Handle some simple instrumented tests that we can do for Drivers without Google authentication
 * @author Robert MacGillivray | Mar.24.2020
 */
public class DriverInstrumentedTests {
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

    @BeforeClass
    public static void preInit() {
        FirebaseManager.getReference().setTestMode(true);
    }

    /**
     * Populate some test data, cache it in the OfflineCache, and bypass the login activity
     */
    @Before
    public void init() {
        // Make sure the cache is clean. Tried to do it @After, but there are some known timing bugs
        OfflineCache.getReference().clearCache();

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

        OfflineCache.getReference().cacheCurrentUser(testDriver);
        OfflineCache.getReference().cacheCurrentRideRequest(testRideRequest);

        Intent intent = new Intent(Intent.ACTION_PICK);
        navigationActivityRule.launchActivity(intent);
    }

    /**
     * Shut down the activity
     */
    @After
    public void cleanup() {
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
                .check(matches(withText(String.format("%s %s", testDriver.getFirstName(), testDriver.getLastName()))));

        // Check the displayed email for accuracy
        onView(withId(R.id.display_email))
                .check(matches(withText(testDriver.getEmail())));

        // Close the drawer and make sure it's closed
        onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.close())
                .check(matches(isClosed(Gravity.LEFT)));
    }

    /**
     * Test that makes sure the profile page displays the correct info
     */
    @Test
    public void testProfileScreen() {
        goToProfileScreenFromAnyScreen();

        // Make sure first name is auto-populated correctly
        onView(withId(R.id.user_first_name))
                .check(matches(withText(testDriver.getFirstName())));

        // Make sure last name is auto-populated correctly
        onView(withId(R.id.user_last_name))
                .check(matches(withText(testDriver.getLastName())));

        // Make sure phone number is auto-populated correctly
        onView(withId(R.id.rider_phone))
                .check(matches(withText(testDriver.getPhoneNumber())));

        // Make sure email is auto-populated correctly
        onView(withId(R.id.rider_email))
                .check(matches(withText(testDriver.getEmail())));

        // Make sure vehicle make is auto-populated correctly
        onView(withId(R.id.make))
                .check(matches(withText(testVehicle.getMake())));

        // Make sure vehicle model is auto-populated correctly
        onView(withId(R.id.model))
                .check(matches(withText(testVehicle.getModel())));

        // Make sure vehicle plate number is auto-populated correctly
        onView(withId(R.id.plate))
                .check(matches(withText(testVehicle.getPlateNumber())));

        // Make sure vehicle seat number is auto-populated correctly
        onView(withId(R.id.seats))
                .check(matches(withText(testVehicle.getSeatNumber().toString())));

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

        String newFirstName = "2" + testDriver.getFirstName() + "2";
        String newLastName = "2" + testDriver.getLastName() + "2";
        String newPhone = "7 131-313-1313";
        String newMake = "2" + testVehicle.getMake() + "2";
        String newModel = "2" + testVehicle.getModel() + "2";
        String newPlate = "2" + testVehicle.getPlateNumber() + "2";
        int newSeats = 2 + testVehicle.getSeatNumber() + 2;

        // Give some animations time to finish
        onView(isRoot()).perform(waitFor(1000));

        // Edit the driver's first name
        onView(withId(R.id.user_first_name))
                .perform(clearText())
                .perform(typeText(newFirstName))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newFirstName)));
        // Edit the driver's last name
        onView(withId(R.id.user_last_name))
                .perform(clearText())
                .perform(typeText(newLastName))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newLastName)));
        // Edit the driver's phone number
        onView(withId(R.id.rider_phone))
                .perform(clearText())
                .perform(typeText(newPhone))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newPhone)));
        // Edit the driver's vehicle's make
        onView(withId(R.id.make))
                .perform(clearText())
                .perform(typeText(newMake))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newMake)));
        // Edit the driver's vehicle's model
        onView(withId(R.id.model))
                .perform(clearText())
                .perform(typeText(newModel))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newModel)));
        // Edit the driver's vehicle's plate number
        onView(withId(R.id.plate))
                .perform(clearText())
                .perform(typeText(newPlate))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(newPlate)));
        // Edit the driver's vehicle's seat count
        onView(withId(R.id.seats))
                .perform(clearText())
                .perform(typeText(String.format("%d", newSeats)))
                .perform(ViewActions.closeSoftKeyboard())
                .check(matches(withText(String.format("%d", newSeats))));

        // Save the new profile information
        onView(withId(R.id.save_profile_button))
                .perform(click());

        // Clicking the saved button sends us back to the map
        checkMapLoaded();

        // See if new values were saved to the cache properly
        Driver cachedDriver = (Driver)OfflineCache.getReference().retrieveCurrentUser();
        assert(cachedDriver.getFirstName().equalsIgnoreCase(newFirstName));
        assert(cachedDriver.getLastName().equalsIgnoreCase(newLastName));
        assert(cachedDriver.getPhoneNumber().equalsIgnoreCase(newPhone));
        assert(cachedDriver.getVehicle().getMake().equalsIgnoreCase(newMake));
        assert(cachedDriver.getVehicle().getModel().equalsIgnoreCase(newModel));
        assert(cachedDriver.getVehicle().getPlateNumber().equalsIgnoreCase(newPlate));
        assert(cachedDriver.getVehicle().getSeatNumber() == newSeats);

        // Redundancy test to make sure that the following tests still pass after editing
        testDriver = cachedDriver;
        testVehicle = testDriver.getVehicle();
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
        // Make sure the request accepted time is correct
        onView(withId(R.id.user_arrival_time))
                .check(matches(withText(formatter.format(testRideRequest.getTimeInfo().getRequestAcceptedTime()))));
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
}
