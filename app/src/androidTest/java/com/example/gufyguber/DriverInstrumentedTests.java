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

/**
 * Handle some simple instrumented tests that we can do for Drivers without Google authentication
 * @author Robert MacGillivray | Mar.24.2020
 */
public class DriverInstrumentedTests {
    @Rule
    public ActivityTestRule<NavigationActivity> navigationActivityRule = new ActivityTestRule<>(NavigationActivity.class, false, false);

    @Before
    public void init() {
        OfflineCache.getReference().cacheCurrentUser(new Driver("1234", "user@test.com",
                "TestFN", "TestLN", "(123)456-7890",
                new Vehicle("TestModel", "TestMake", "TestPlate", 1)));

        Intent intent = new Intent(Intent.ACTION_PICK);
        navigationActivityRule.launchActivity(intent);
    }

    @After
    public void cleanup() {
        navigationActivityRule.finishActivity();
    }

    @Test
    public void testProfileScreen() {
        //TODO: Verify that we're on the map

        onView(withId(R.id.drawer_layout))
                // Is the drawer closed?
                .check(matches(isClosed(Gravity.LEFT)))
                // Open the drawer
                .perform(DrawerActions.open());

        // TODO: Verify that our info is displayed properly in the drawer

        onView(withId(R.id.nav_view))
                // Click on profile menu item
                .perform(NavigationViewActions.navigateTo(R.id.nav_profile));

        // TODO: Verify that our info is displayed properly in the profile

        onView(withId(R.id.drawer_layout))
                // Is the drawer closed?
                .check(matches(isClosed(Gravity.LEFT)))
                // Open the drawer
                .perform(DrawerActions.open());

        onView(withId(R.id.nav_view))
                // Click on map menu item
                .perform(NavigationViewActions.navigateTo(R.id.nav_map));

        // TODO: Verify that we're on the map
    }

    @Test
    public void testEditProfileScreen() {
        // TODO: Test editing info on the profile screen
    }
}
