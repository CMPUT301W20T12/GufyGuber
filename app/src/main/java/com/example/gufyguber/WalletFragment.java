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
 * Wallet.java
 *
 * Last edit: homie, 28/03/20 7:21 PM
 *
 * Version
 */

package com.example.gufyguber;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WalletFragment newInstance} factory method to
 * create an instance of this fragment.
 */
public class WalletFragment extends Fragment {
    private TextView transaction;
    private String transaction_info;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TRANSACTION_COLLECTION = "transactions";

//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    public WalletFragment() {
        // Required empty public constructor
    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment Wallet.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static WalletFragment newInstance(String param1, String param2) {
//        WalletFragment fragment = new WalletFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        if (getArguments() != null) {
////            mParam1 = getArguments().getString(ARG_PARAM1);
////            mParam2 = getArguments().getString(ARG_PARAM2);
////        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseManager.getReference().fetchWalletInfo(OfflineCache.getReference().retrieveCurrentUser().getUID(), new FirebaseManager.ReturnValueListener<Wallet>() {
            @Override
            public void returnValue(Wallet value) {
                if (value == null) {
                    transaction.setText("Wallet Info Unavailable");
                } else {
                    transaction.setText(value.getTransaction());
                }
            }
        });

    }
}
