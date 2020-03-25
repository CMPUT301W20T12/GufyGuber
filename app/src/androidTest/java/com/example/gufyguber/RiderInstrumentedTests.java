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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import androidx.test.rule.ActivityTestRule;

/**
 * Handle some simple instrumented tests that we can do for Riders without Google authentication
 * @author Robert MacGillivray | Mar.24.2020
 */
public class RiderInstrumentedTests {
    @Rule
    public ActivityTestRule<NavigationActivity> navigationActivityRule = new ActivityTestRule<>(NavigationActivity.class, false, false);

    @Before
    public void init() {
        OfflineCache.getReference().cacheCurrentUser(new Rider("1234", "user@test.com",
                "TestFN", "TestLN", "(123)456-7890"));

        Intent intent = new Intent(Intent.ACTION_PICK);
        navigationActivityRule.launchActivity(intent);
    }

    @After
    public void cleanup() {
        navigationActivityRule.finishActivity();
    }

    @Test
    public void testProfileScreen() {
        // TODO: Copy test from Driver (minus vehicle checks)
    }

    @Test
    public void testEditProfileScreen() {
        // TODO: Copy test from Driver (minus vehicle checks)
    }

    @Test
    public void testCreateRideRequest() {
        onView(withId(R.id.fab))
                .perform(click());

        // TODO: Verify that form is created

        // TODO: Verify that form is properly fillable (perhaps with some clicking on the map)

        // TODO: Verify that error is thrown when creating request due to lack of Firestore connection? Or maybe not.
    }
}
