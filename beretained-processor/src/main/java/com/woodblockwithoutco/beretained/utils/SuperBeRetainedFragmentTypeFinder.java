package com.woodblockwithoutco.beretained.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.android.Suffixes;

import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Created by aleksandr on 6/19/16.
 */
public final class SuperBeRetainedFragmentTypeFinder {

    private SuperBeRetainedFragmentTypeFinder() {}

    public static TypeName getSuperBeRetainedFragment(TypeMirror type,
                                                      Collection<TypeMirror> superRetainEnabledTypes,
                                                      Types typeUtils,
                                                      Messager messager) {
        if(superRetainEnabledTypes.contains(type)) {
            TypeName retainEnabledType = ClassName.get(type);
            if(retainEnabledType instanceof ClassName) {
                ClassName retainEnabledClass = (ClassName) retainEnabledType;
                ClassName fragmentClass = ClassName.get(retainEnabledClass.packageName(), retainEnabledClass.simpleName() + Suffixes.BERETAINED_FRAGMENT_SUFFIX);
                return fragmentClass;
            } else {
                messager.printMessage(ERROR, "One of types that contains retained fields is not a class!");
                return null;
            }
        }

        List<? extends TypeMirror> supertypes = typeUtils.directSupertypes(type);
        if(supertypes.size() > 0) {
            TypeName superBeRetainedFragmentClass;
            //supertypes.get(0) is used as optimization to avoid going up interfaces hierarchy
            if((superBeRetainedFragmentClass = getSuperBeRetainedFragment(supertypes.get(0),
                    superRetainEnabledTypes,
                    typeUtils,
                    messager)) != null) {
                return superBeRetainedFragmentClass;
            }
        }

        return null;
    }
}
