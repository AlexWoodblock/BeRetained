package com.woodblockwithoutco.beretained;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.woodblockwithouco.beretained.Retain;

/**
 * Created by aleksandr on 6/11/16.
 */
public class TestActivity extends AppCompatActivity {

    @Retain
    @Nullable
    boolean testBooleanField;

}
