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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.Menu;
import android.view.MenuItem;

import com.woodblockwithouco.beretained.Retain;
import com.woodblockwithoutco.beretained.BeRetained;
import com.woodblockwithoutco.beretainedexample.widget.RecyclerViewFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Retain
    int[] mIntArray;

    @Retain
    @NonNull
    Object mObject;

    @Retain
    Map<String, String> mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //we must always call BeRetained.onCreate()
        BeRetained.onCreate(this);

        boolean wasRestored = BeRetained.restore(this);
        if(wasRestored) {
            setTitle(R.string.retained);
        } else {
            setTitle(R.string.not_retained);
        }

        fillInitialValues();

        FragmentManager fragmentManager = getSupportFragmentManager();
        RecyclerViewFragment recyclerViewFragment = (RecyclerViewFragment) fragmentManager.findFragmentById(R.id.container);
        if(recyclerViewFragment == null) {
            recyclerViewFragment = new RecyclerViewFragment();
            fragmentManager.beginTransaction().add(R.id.container, recyclerViewFragment).commit();
        }

        recyclerViewFragment.setItems(getItems());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        BeRetained.save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        if(id == R.id.open_sub_main_activity) {
            startActivity(new Intent(this, SubMainActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    protected CharSequence[] getItems() {
        //filling list with entries like "fieldName 0xfieldHash"
        String[] fieldNames = new String[] {
                "mIntArray",
                "mObject",
                "mMap",
        };

        String[] fieldHashcodes = new String[] {
                "0x" + Integer.toHexString(System.identityHashCode(mIntArray)),
                "0x" + Integer.toHexString(System.identityHashCode(mObject)),
                "0x" + Integer.toHexString(System.identityHashCode(mMap))
        };

        if(fieldHashcodes.length != fieldNames.length) {
            throw new IllegalStateException("Did you forget to add something?");
        }

        int length = fieldHashcodes.length;
        CharSequence[] items = new CharSequence[length];
        for(int i = 0; i < length; i++) {
            SpannableStringBuilder description = new SpannableStringBuilder();
            description.append(fieldNames[i]);
            description.setSpan(new TypefaceSpan("bold"), 0, description.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            description.append(" ").append(fieldHashcodes[i]);
            items[i] = description;
        }

        return items;
    }

    protected void fillInitialValues() {
        if(mIntArray == null) {
            mIntArray = new int[]{0, 1, 2, 3};
        }

        if(mObject == null) {
            mObject = new Object();
        }

        if(mMap == null) {
            mMap = new HashMap<>();
            mMap.put("testkey1", "testvalue1");
            mMap.put("testkey2", "testvalue2");
            mMap.put("testkey3", "testvalue3");
        }
    }

}
