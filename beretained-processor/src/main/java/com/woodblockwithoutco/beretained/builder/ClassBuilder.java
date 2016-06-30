package com.woodblockwithoutco.beretained.builder;

import com.squareup.javapoet.JavaFile;

/**
 * Created by aleksandr on 6/30/16.
 */
public interface ClassBuilder {
    JavaFile build();
    void addBody();
}
