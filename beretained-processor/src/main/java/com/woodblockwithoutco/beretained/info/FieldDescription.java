package com.woodblockwithoutco.beretained.info;

import com.squareup.javapoet.TypeName;

/**
 * Created by aleksandr on 6/11/16.
 */
public class FieldDescription {
    public final TypeName typeName;
    public final String name;
    public final boolean nullAllowed;

    public FieldDescription(TypeName typeName, String name, boolean nullAllowed) {
        this.typeName = typeName;
        this.name = name;
        this.nullAllowed = nullAllowed;
    }
}
