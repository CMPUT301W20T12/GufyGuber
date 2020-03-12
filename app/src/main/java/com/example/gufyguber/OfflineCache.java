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
 * OfflineCache.java
 *
 * Last edit: scott, 11/03/20 7:09 PM
 *
 * Version
 */

package com.example.gufyguber;

public class OfflineCache {
    private static OfflineCache reference;
    public static OfflineCache getReference() {
        if (reference == null) {
            reference = new OfflineCache();
        }
        return reference;
    }

    private RideRequest currentRideRequest;
    public void cacheCurrentRideRequest(RideRequest request) {
        currentRideRequest = request;
    }
    public RideRequest retrieveCurrentRideRequest() {
        return currentRideRequest;
    }

    private User currentUser;
    public void cacheCurrentUser(User user) { currentUser = user; }
    public User retrieveCurrentUser() { return currentUser; }
}
