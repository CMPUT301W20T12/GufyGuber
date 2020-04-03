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
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;

import com.example.gufyguber.Models.User;
import com.example.gufyguber.Singletons.OfflineCache;

import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.ViewMatchers.*;

public class InstrumentedTestHelpers {
    private static final String TAG = "InstrumentedTestHelpers";

    /**
     * Waits until the map fragment is opened (typically used to make sure cleanup happens properly)
     */
    public static void checkMapLoaded() {
        // Waits until the map fragment opens and tests that it actually opened
        onView(withId(R.id.user_map))
                .check(matches(withId(R.id.user_map)));

        // Buffer to let transition animations finish
        onView(isRoot()).perform(waitFor(1000));
    }

    /**
     * Uses the navigation drawer menu to go to the profile screen
     */
    public static void goToProfileScreenFromAnyScreen() {
        // The profile screen will try to pull something from Firebase, we have to fix the cache manually
        User currentUser = OfflineCache.getReference().retrieveCurrentUser();

        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // Use the drawer menu to go to the user's profile screen
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_profile));

        // Waits until we get to the driver profile screen and tests that we actually get there
        onView(withId(R.id.profile))
                .check(matches(withId(R.id.profile)));

        // Buffer to let transition animations finish
        onView(isRoot()).perform(waitFor(1000));

        // Fixes the cache that gets wiped by the FirebaseManager refusal.
        OfflineCache.getReference().cacheCurrentUser(currentUser);
    }

    /**
     * Uses the navigation drawer menu to go to the map screen
     */
    public static void goToMapScreenFromAnyScreen() {
        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // Use the drawer menu to go to the map screen
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_map));

        // Waits until we get to the map and tests that we actually got there
        onView(withId(R.id.user_map))
                .check(matches(withId(R.id.user_map)));

        // Buffer to let transition animations finish
        onView(isRoot()).perform(waitFor(1000));
    }

    /**
     * Uses the navigation drawer menu to go to the current request screen
     */
    public static void goToRideRequestScreenFromAnyScreen() {
        // Makes sure the drawer menu is closed, then opens it
        onView(withId(R.id.drawer_layout))
                .check(matches(isClosed(Gravity.LEFT)))
                .perform(DrawerActions.open());

        // Use the drawer menu to go to the Current Ride Request Screen
        onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_current_requests));

        // Waits until we get to the request screen and tests that we actually got there
        onView(withId(R.id.current_request))
                .check(matches(withId(R.id.current_request)));

        // Buffer to let transition animations finish
        onView(isRoot()).perform(waitFor(1000));
    }

    /**
     * Puts the thread to sleep in order to pause testing. Typically used temporarily
     * to make the tests viewable by human eyes
     * @param seconds The number of seconds to pause testing for
     */
    public static void waitForSeconds(UiController controller, float seconds) {
        try{
            Thread.sleep((long)(seconds * 1000));
        } catch (Exception e) {
            Log.e(TAG, "Error waiting: ", e);
        }
    }

    /**
     * Perform action of waiting for a specific time.
     * @author Hesam | https://stackoverflow.com/a/35924943
     */
    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }

    public static ViewAction clickPercent(final float pctX, final float pctY){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);
                        int w = view.getWidth();
                        int h = view.getHeight();

                        float x = w * pctX;
                        float y = h * pctY;

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER, InputDevice.SOURCE_ANY, MotionEvent.BUTTON_PRIMARY);
    }

}
