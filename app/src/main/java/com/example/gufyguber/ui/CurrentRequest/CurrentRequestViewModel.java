/*
 * CurrentRequestViewModel.java
 *
 * Version
 *
 * Last edit: mai-thyle, 04/03/20 11:21 PM
 *
 * Copyright (c) CMPUT301W20T12 2020. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 */

package com.example.gufyguber.ui.CurrentRequest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CurrentRequestViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CurrentRequestViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Current Requests fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}