package com.woodblockwithoutco.beretained;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;

import com.woodblockwithouco.beretained.Retain;
import com.woodblockwithoutco.beretained.widget.RecyclerViewFragment;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Retain
    int[] mIntArray;

    @Retain
    Object mObject;

    @Retain
    Map<String, String> mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean wasRestored = MainActivityFieldsRetainer.restore(this);
        if(wasRestored) {
            setTitle(R.string.retained);
        } else {
            setTitle(R.string.not_retained);

            fillInitialValues();
        }

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
        MainActivityFieldsRetainer.save(this);
    }

    protected CharSequence[] getItems() {
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

    private void fillInitialValues() {
        mIntArray = new int[] {0, 1, 2, 3};
        mObject = new Object();
        mMap = new HashMap<>();
        mMap.put("testkey1", "testvalue1");
        mMap.put("testkey2", "testvalue2");
        mMap.put("testkey3", "testvalue3");
    }

}
