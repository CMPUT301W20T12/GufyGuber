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
 * Last edit: scott, 02/04/20 5:50 PM
 *
 * Version
 */

package com.example.gufyguber.Models;

import java.util.ArrayList;

/**
 * This class models a User's wallet
 * @author woldegio, harrison, dalton
 */
public class Wallet {
    private ArrayList<String> transactions;

    /**
     * This is the constructor method that sets the attributes of the wallet of the user
     * @param transaction
     * this is the string containing the value of the transaction
     */
    public Wallet(String transaction) {
        this.transactions = new ArrayList<String>();
        this.setTransaction(transaction);
    }
    public Wallet() {
        this.transactions = new ArrayList<String>();
    }
    /**
     * Sets the objects transaction attribute
     * @param transaction
     * value of transaction
     */
    public void setTransaction(String transaction) { transactions.add(transaction); }

    /**
     * This returns the string containing the transaction
     * @return
     */
    public ArrayList<String> getTransactions() { return transactions; }
}
