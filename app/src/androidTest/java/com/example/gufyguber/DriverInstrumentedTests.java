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
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import androidx.test.espresso.contrib.DrawerActions;
import static androidx.test.espresso.contrib.DrawerMatchers.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Handle some simple instrumented tests that we can do for Drivers without Google authentication
 * @author Robert MacGillivray | Mar.24.2020
 */
public class DriverInstrumentedTests {
    private Driver testDriver;
    private Rider testRider;
    private float testFare;
    private RideRequest.Status testStatus;
    private LocationInfo testLocation;
    private TimeInfo testTime;
    private RideRequest testRideRequest;

    @Rule
    public ActivityTestRule<NavigationActivity> navigationActivityRule = new ActivityTestRule<>(NavigationActivity.class, false, false);

    @Before
    public void init() {
        testDriver = new Driver("1234", "user@test.com", "TestFN", "TestLN", "(123)456-7890",
                new Vehicle("TestModel", "TestMake", "TestPlate", 1));
        testRider = new Rider("4321", "test@user.com", "FNTest", "LNTest", "(098)765-4321");
        testFare = 13.0f;
        testStatus = RideRequest.Status.CONFIRMED;
        testLocation = new LocationInfo();
        testTime = new TimeInfo();
        testTime.setRequestAcceptedTime();
        testRideRequest = new RideRequest(testRider.getUID(), testDriver.getUID(), testStatus,
                testFare, testLocation, testTime);

        OfflineCache.getReference().setIgnoreFirestore(true);
        OfflineCache.getReference().cacheCurrentUser(testDriver);
        OfflineCache.getReference().cacheCurrentRideRequest(testRideRequest);

        Intent intent = new Intent(Intent.ACTION_PICK);
        navigationActivityRule.launchActivity(intent);
    }

    @After
    public void cleanup() {
        OfflineCache.getReference().clearCache();
        navigationActivityRule.finishActivity();
    }

    @Test
    public void testProfileScreen() {
        // Waits until the map fragment opens and tests that it actually opened
        onView(withId(R.id.driver_map))
                .check(matches(withId(R.id.driver_map)));

        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // TODO: Verify that our info is displayed properly in the drawer

        // Use the drawer menu to go to the user's profile screen
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_profile));

        // Waits until we get to the driver profile screen and tests that we actually get there
        onView(withId(R.id.driver_profile))
                .check(matches(withId(R.id.driver_profile)));


        // TODO: Verify that our info is displayed properly in the profile

        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // Use the drawer menu to go to the map screen
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_map));

        // Waits until we get back to the map and tests that we actually get there before finishing the test
        onView(withId(R.id.driver_map))
                .check(matches(withId(R.id.driver_map)));
    }

    @Test
    public void testEditProfileScreen() {
        //TODO: Test editing info on the profile screen
    }
}
