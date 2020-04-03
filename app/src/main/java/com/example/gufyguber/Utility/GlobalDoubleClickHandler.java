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
 * GlobalDoubleClickHandler.java
 *
 * Last edit: scott, 02/04/20 6:01 PM
 *
 * Version
 */

package com.example.gufyguber.Utility;

import android.os.SystemClock;

/**
 * Simple one-method class to globally refuse rapid double-clicks on buttons
 * @author Robert MacGillivray | Mar.24.2020
 *
 * Inspired by user qezt, though the final result is very different https://stackoverflow.com/a/16514644
 */
public class GlobalDoubleClickHandler {
    // Amount of time that must pass before a new click isn't considered a double-click
    private static final long DOUBLE_CLICK_THRESHOLD = 500;
    private static long lastClicked;
    public static boolean isDoubleClick() {
        long thisClick = SystemClock.elapsedRealtime();
        boolean result = (lastClicked + DOUBLE_CLICK_THRESHOLD) >= thisClick;
        lastClicked = thisClick;
        return result;
    }
}
