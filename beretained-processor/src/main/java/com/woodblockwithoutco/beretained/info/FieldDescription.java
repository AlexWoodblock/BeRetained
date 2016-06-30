package com.woodblockwithoutco.beretained.info;

import com.squareup.javapoet.TypeName;

/**
 * Created by aleksandr on 6/11/16.
 */
public class FieldDescription {
    public final TypeName typeName;
    public final String name;

    public FieldDescription(TypeName typeName, String name) {
        this.typeName = typeName;
        this.name = name;
    }
}
