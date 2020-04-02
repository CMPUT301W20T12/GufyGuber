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
 * CustomList.java
 *
 * Last edit: dalton, 30/03/20 4:09 PM
 *
 * Version
 */

package com.example.gufyguber;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

/**
 * A custom list the extends an ArrayAdapter to display a Wallet object to
 * display the users transaction history. Based on CustomList class implemented
 * in labs.
 * @author dalton
 * @see Wallet
 */
public class CustomList extends ArrayAdapter<String> {
    private ArrayList<String> transactions;
    private Context context;

    public CustomList(Context context, ArrayList<String> transactions) {
        super(context, 0, transactions);
        this.transactions = transactions;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.wallet_content, parent, false);
        }
        String transaction = transactions.get(position);

        TextView transactionText = view.findViewById(R.id.transaction_text);

        transactionText.setText(transaction);

        return view;
    }
}