package com.woodblockwithoutco.beretained.builder;

import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.android.AndroidClasses;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;

/**
 * FieldsRetainer class builder for regular Activities.
 */
public class NonSupportFieldsRetainerClassBuilder extends FieldsRetainerClassBuilder {

    public NonSupportFieldsRetainerClassBuilder(TypeMirror retainEnabledType, Messager messager) {
        super(retainEnabledType, messager);
    }

    @Override
    protected String getGetFragmentManagerMethodName() {
        return "getFragmentManager";
    }

    @Override
    protected TypeName getFragmentManagerTypeName() {
        return AndroidClasses.ANDROID_APP_FRAGMENT_MANAGER;
    }
}
