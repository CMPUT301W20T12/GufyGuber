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
// File: TimeInfo.java
// Date: Feb.25.2020
// Purpose: To model data related to the time of Ride Requests and provide some associated static helper functions

// Last Updated: Feb.25.2020 by Robert MacGillivray

package com.example.gufyguber;

import java.util.Date;

/**
 * Models data related to the time of Ride Requests and provides associated static helper functions
 */
public class TimeInfo {
    private Date requestOpenTime;
    public void setRequestOpenTime() { requestAcceptedTime = new Date(); }
    public void setRequestOpenTime(Date requestOpenTime) { this.requestOpenTime = requestOpenTime; }
    public Date getRequestOpenTime() { return requestOpenTime; }

    private Date requestAcceptedTime;
    public void setRequestAcceptedTime() { requestAcceptedTime = new Date(); }
    public void setRequestAcceptedTime(Date requestAcceptedTime) { this.requestAcceptedTime = requestAcceptedTime; }
    public Date getRequestAcceptedTime() { return requestAcceptedTime; }

    private Date requestCompletedTime;
    public void setRequestCompletedTime() { requestCompletedTime = new Date(); }
    public void setRequestCompletedTime(Date requestCompletedTime) { this.requestCompletedTime = requestCompletedTime; }
    public Date getRequestCompletedTime() { return requestCompletedTime; }

    /**
     * Creates new instance of TimeInfo with exact time of creation as the requestOpenTime
     */
    public TimeInfo() {
        setRequestOpenTime();
    }

    //TODO: Should estimate time to travel between two coordinates
}
