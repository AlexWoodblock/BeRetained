/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package com.woodblockwithoutco.beretainedexample;

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.Menu;

import com.woodblockwithouco.beretained.Retain;

import java.util.LinkedList;

/**
 * Subclass of MainActivity demoing that inheritance automatically works provided
 * you call generated save/restore methods for subclass.
 */
public class SubMainActivity extends MainActivity {

    @Retain
    @NonNull
    LinkedList<String> mStringLinkedList;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //no menu needed
        return false;
    }

    @Override
    protected CharSequence[] getItems() {
        CharSequence[] superItems = super.getItems();

        String[] fieldNames = new String[] {
                "mStringLinkedList",
        };

        String[] fieldHashcodes = new String[] {
                "0x" + Integer.toHexString(System.identityHashCode(mStringLinkedList))
        };

        if(fieldHashcodes.length != fieldNames.length) {
            throw new IllegalStateException("Did you forget to add something?");
        }

        int length = fieldHashcodes.length;
        CharSequence[] items = new CharSequence[superItems.length + length];
        for(int i = 0; i < superItems.length; i++) {
            items[i] = superItems[i];
        }

        for(int i = 0; i < length; i++) {
            SpannableStringBuilder description = new SpannableStringBuilder();
            description.append(fieldNames[i]);
            description.setSpan(new TypefaceSpan("bold"), 0, description.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            description.append(" ").append(fieldHashcodes[i]);
            items[i + superItems.length] = description;
        }


        return items;
    }

    @Override
    protected void fillInitialValues() {
        super.fillInitialValues();

        mStringLinkedList = new LinkedList<>();
    }

}
