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
 * DirectionsManager.java
 *
 * Last edit: scott, 23/03/20 12:16 AM
 *
 * Version
 */

package com.example.gufyguber;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DirectionsManager {
    public static final String TAG = "DirectionsManager";

    public static String buildDirectionsURL(LocationInfo locationInfo) {
        String apiKey = "AIzaSyAtsoVmhyaUqxW6g2v5IKb6ftaVTr4vWnA";
        String baseURL = "https://maps.googleapis.com/maps/api/directions/";
        String outputFormat = "json";
        String partSeparator = "?";
        String parameterSeparator = "&";
        String originParam = String.format("origin=%f,%f", locationInfo.getPickup().latitude, locationInfo.getPickup().longitude);
        String destinationParam = String.format("destination=%f,%f", locationInfo.getDropoff().latitude, locationInfo.getDropoff().longitude);
        String keyParam = String.format("key=%s", apiKey);
        return baseURL + outputFormat + partSeparator + originParam +
                parameterSeparator + destinationParam + parameterSeparator + keyParam;
    }

    public static String getDirectionsResponse(String requestURL) {
        String response = null;
        try {
            URL url = new URL(requestURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer stringBuffer = new StringBuffer();
                String nextLine = reader.readLine();
                while (nextLine != null) {
                    stringBuffer.append(nextLine);
                    nextLine = reader.readLine();
                }
                response = stringBuffer.toString();
                reader.close();
            } catch(Exception e) {
                Log.e(TAG, "Error getting directions: ", e);
            } finally {
                urlConnection.disconnect();
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting directions: ", e);
        }
        return response;
    }

    public static void drawDirectionsPolyline(LocationInfo locationInfo, GoogleMap map) {

    }

    public static class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = getDirectionsResponse(url[0]);
                Log.d(TAG,"DownloadTask : " + data);
            }catch(Exception e){
                Log.d(TAG,e.toString());
            }
            return data;
        }
    }
}
