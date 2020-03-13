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
 * LocationInfoTests.java
 *
 * Last edit: Robert, 12/03/20 1:41 PM
 *
 * Version
 */

package com.example.gufyguber;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationInfoTests {
    @Test
    public void testDistanceErrors() {
        // Empty location info should fail these tests
        LocationInfo locInf = new LocationInfo();
        assert(locInf.getRemainingDist() < 0);
        assert(locInf.getTotalDist() < 0);
    }
}