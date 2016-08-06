package com.woodblockwithoutco.beretained.builder;

import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.android.AndroidClasses;

import java.util.Collection;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Created by aleksandr on 8/6/16.
 */
public class NonSupportBeRetainedFragmentClassBuilder extends BeRetainedFragmentClassBuilder {

    public NonSupportBeRetainedFragmentClassBuilder(TypeMirror enclosingClassType,
                                                    Messager m,
                                                    Collection<TypeMirror> superRetainEnabledTypes,
                                                    Types typeUtils) {
        super(enclosingClassType, m, superRetainEnabledTypes, typeUtils);
    }

    @Override
    protected TypeName getFragmentTypeName() {
        return AndroidClasses.ANDROID_APP_FRAGMENT;
    }
}
