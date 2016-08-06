package com.woodblockwithoutco.beretained.builder;

import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.android.AndroidClasses;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;

/**
 * FieldsRetainer class builder for Android Support library Activities.
 */
public class SupportFieldsRetainerClassBuilder extends FieldsRetainerClassBuilder {

    public SupportFieldsRetainerClassBuilder(TypeMirror retainEnabledType, Messager messager) {
        super(retainEnabledType, messager);
    }

    @Override
    protected String getGetFragmentManagerMethodName() {
        return "getSupportFragmentManager";
    }

    @Override
    protected TypeName getFragmentManagerTypeName() {
        return AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS;
    }
}