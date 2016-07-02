package com.woodblockwithoutco.beretained;

import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.view.Menu;

import com.woodblockwithouco.beretained.Retain;

import java.util.LinkedList;

/**
 * Created by aleksandr on 6/11/16.
 */
public class SubMainActivity extends MainActivity {

    @Retain
    @NonNull
    LinkedList<String> mStringLinkedList;

    @Override
    protected boolean restoreState() {
        return SubMainActivityFieldsRetainer.restore(this);
    }

    @Override
    protected void saveState() {
        SubMainActivityFieldsRetainer.save(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
