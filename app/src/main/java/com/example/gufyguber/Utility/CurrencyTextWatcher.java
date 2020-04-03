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
 * CurrencyTextWatcher.java
 *
 * Last edit: scott, 02/04/20 5:58 PM
 *
 * Version
 */

package com.example.gufyguber.Utility;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Formats an EditText to display currency values as they are entered.
 * Note, this will only work with North American currency in $X.XX format.
 * @author Robert MacGillivray | Mar.12.2020
 */
public class CurrencyTextWatcher implements TextWatcher {
    private EditText watchedEditText;
    private String cachedString;

    public CurrencyTextWatcher(EditText watchedEditText) {
        this.watchedEditText = watchedEditText;
        if (this.watchedEditText.getText() != null) {
            this.cachedString = watchedEditText.getText().toString();
        } else {
            this.cachedString = null;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().equalsIgnoreCase(cachedString)){
            return;
        }

        String newString = s.toString();
        if (newString.isEmpty()) {
            return;
        }

        newString = newString.replaceAll("[$.]", "");
        float currencyVal = Float.parseFloat(newString) / 100f;
        newString = String.format("$%.2f", currencyVal);

        // Have to remove the listener or this will be called infinitely
        watchedEditText.removeTextChangedListener(this);
        watchedEditText.setText(newString);
        cachedString = newString;
        // Move cursor to the end of the text so that it's less frustrating to type
        watchedEditText.setSelection(newString.length());
        watchedEditText.addTextChangedListener(this);
    }
}
