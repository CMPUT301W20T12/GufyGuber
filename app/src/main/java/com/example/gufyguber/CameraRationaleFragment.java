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
 * cameraRationaleFragment.java
 *
 * Last edit: hmp, 26/03/20 10:15 AM
 *
 * Version
 */

package com.example.gufyguber;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import static com.example.gufyguber.startScanQR.PERMISSION_REQUEST;


/***
 * Explains the rationale behind using the camera then
 * requests permission to use camera
 * @author Harrison Peters | Mar.26.2020
 */
public class CameraRationaleFragment extends DialogFragment {
    Button confirm;

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.camera_rationale, null);
        confirm = view.findViewById(R.id.confirm_rationale);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalDoubleClickHandler.isDoubleClick()) {
                    return;
                }
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                dismiss();
            }
        });

        setCancelable(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        return builder
                .setView(view)
                .create();
    }

}
