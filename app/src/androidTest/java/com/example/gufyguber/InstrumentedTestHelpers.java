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
 * InstrumentedTestHelpers.java
 *
 * Last edit: scott, 25/03/20 5:04 PM
 *
 * Version
 */

package com.example.gufyguber;

import android.util.Log;
import android.view.Gravity;

import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class InstrumentedTestHelpers {
    private static final String TAG = "InstrumentedTestHelpers";

    public static void checkMapLoaded() {
        // Waits until the map fragment opens and tests that it actually opened
        onView(withId(R.id.driver_map))
                .check(matches(withId(R.id.driver_map)));
    }

    public static void goToProfileScreenFromAnyScreen() {
        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // Use the drawer menu to go to the user's profile screen
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_profile));

        // Waits until we get to the driver profile screen and tests that we actually get there
        onView(withId(R.id.driver_profile))
                .check(matches(withId(R.id.driver_profile)));
    }

    public static void goToMapScreenFromAnyScreen() {
        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // Use the drawer menu to go to the map screen
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_map));

        // Waits until we get back to the map and tests that we actually got there
        onView(withId(R.id.driver_map))
                .check(matches(withId(R.id.driver_map)));
    }

    public static void waitForSeconds(float seconds) {
        try{
            Thread.sleep((long)(seconds * 1000));
        } catch (Exception e) {
            Log.e(TAG, "Error waiting: ", e);
        }
    }
}
