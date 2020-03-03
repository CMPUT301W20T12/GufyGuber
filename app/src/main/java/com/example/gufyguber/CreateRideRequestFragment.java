/*
 *    Copyright (c) 2020. Gufy Guber
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

// Name: Robert MacGillivray
// File: CreateRideRequestFragment.java
// Date: Mar.02.2020
// Purpose: To control a fragment that Riders use to fill in their ride requests

// Last Updated: Mar.02.2020 by Robert MacGillivray

package com.example.gufyguber;

import android.widget.EditText;

public class CreateRideRequestFragment {

    //TODO: I'm thinking about this and realizing that by the time the user gets here, they really
    //      only need to enter their fair fare since we pull the rest from behind the scenes, and
    //      should have selected the start and end points first. Perhaps we should go to the map from
    //      this screen instead of going to this screen from the map?

    private EditText fareEditText;
}
