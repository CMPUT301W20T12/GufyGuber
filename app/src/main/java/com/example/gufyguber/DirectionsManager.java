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

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.example.gufyguber.FirebaseManager.ReturnValueListener;

/**
 * Handles interactions with Google's Directions API
 * @author Robert MacGillivray | Mar.22.2020
 */
public class DirectionsManager {
    public static final String TAG = "DirectionsManager";

    /**
     * Entry point for any code that wants to draw a polyline on our map. Static because it has no state.
     * @param locationInfo The LocationInfo indicating start and end points for the route.
     * @param map The map to draw the polyline on
     * @param onComplete Will return the polyline so that it can be removed by the owning map fragment later
     */
    public static void drawDirectionsPolyline(LocationInfo locationInfo, final GoogleMap map, final ReturnValueListener<Polyline> onComplete) {
        FetchDirectionsTask fetchTask = new FetchDirectionsTask(new ReturnValueListener<ArrayList<LatLng>>() {
            @Override
            public void returnValue(ArrayList<LatLng> value) {
                if (value != null && value.size() > 0) {
                    onComplete.returnValue(map.addPolyline(new PolylineOptions()
                            .addAll(value)
                            .color(0xFFFF0000)));
                } else {
                    onComplete.returnValue(null);
                }
            }
        });
        fetchTask.execute(buildDirectionsURL(locationInfo));
    }

    /**
     * Builds a valid Directions API request URL that will get a JSON object indicating how to
     * navigate between two provided points.
     * @param locationInfo Used to specify start and end route points in the request URL
     * @return A valid Directions API web request URL
     */
    private static String buildDirectionsURL(LocationInfo locationInfo) {
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

    /**
     * Sends an HTTP request using the provided URL and returns the result
     * @param requestURL A valid Directions API web request URL
     * @return A string representation of a JSON object (or XML, depending on URL format) that encodes
     * information about how to navigate between two or more points.
     */
    private static String getDirectionsResponse(String requestURL) {
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

    /**
     * Handles a Directions API web request asynchronously so we don't freeze the UI while we wait
     */
    private static class FetchDirectionsTask extends AsyncTask<String, Void, String> {
        FirebaseManager.ReturnValueListener<ArrayList<LatLng>> onComplete;

        /**
         * Task constructor. Not default to let callback handle passing the result instead of doing it
         * with messy global references or needless statefullness.
         * @param onComplete Will return the parsed result of of the async HTTP request
         */
        public FetchDirectionsTask(FirebaseManager.ReturnValueListener<ArrayList<LatLng>> onComplete) { this.onComplete = onComplete; }

        /**
         * @param url The URL to use in the request (can pass more than one, but only the first one is considered)
         * @return The result of the HTTP request (is sent to onPostExecute automatically)
         */
        @Override
        protected String doInBackground(String... url) {
            String jsonData = "";
            try{
                jsonData = getDirectionsResponse(url[0]);
                Log.d(TAG,"Directions API Request Result: " + jsonData);
            }catch(Exception e){
                Log.e(TAG,"Error fetching directions: ", e);
            }
            return jsonData;
        }

        /**
         * Parses the result of the HTTP request and returns an ArrayList of LatLng points via callback
         * @param result The result of the HTTP request
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            JSONObject jsonResult = null;
            JSONArray routes = null;
            JSONArray legs = null;
            JSONArray steps = null;
            ArrayList<LatLng> points = new ArrayList<LatLng>();
            try {
                // Parse JSON object as per Google Directions API Specification
                // Note that we can make some simplifying assumptions based on the supposed nature of our request
                // would have to iteratively loop through routes and legs if we weren't guaranteed to have one of each
                jsonResult = new JSONObject(result);
                routes = jsonResult.getJSONArray("routes");
                legs = routes.getJSONObject(0).getJSONArray("legs");
                steps = legs.getJSONObject(0).getJSONArray("steps");
                // for each nav step (i.e. go straight, turn here, etc.) get smoothed polyline points
                for (int i = 0; i < steps.length(); ++i) {
                    JSONObject step = steps.getJSONObject(i);
                    JSONObject jsonPolyline = step.getJSONObject("polyline");
                    String encodedPolyLine = jsonPolyline.getString("points");
                    // Helper utility can decode an encoded polyline into an array of LatLngs
                    // I've checked, it is Apache 2.0
                    points.addAll(PolyUtil.decode(encodedPolyLine));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing JSON response: ", e);
            }
            onComplete.returnValue(points);
        }
    }
}
