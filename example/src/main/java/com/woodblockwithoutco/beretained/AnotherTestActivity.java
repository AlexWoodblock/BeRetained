package com.woodblockwithoutco.beretained;

import com.woodblockwithouco.beretained.Retain;

import java.util.Map;

/**
 * Created by aleksandr on 6/18/16.
 */
public class AnotherTestActivity extends TestActivity {

    @Retain
    Map<String, Map<String, Integer>> complexMap;



}
