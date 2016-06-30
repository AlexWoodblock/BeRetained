package com.woodblockwithoutco.beretained.builder;


import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.utils.SuperBeRetainedFragmentTypeFinder;

import java.util.Collection;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Created by aleksandr on 6/30/16.
 */
abstract class SuperClassAwareSaveRestoreClassBuilder extends SaveRestoreClassBuilder {

    protected TypeName closestSuperBeRetainedFragment;

    public SuperClassAwareSaveRestoreClassBuilder(TypeMirror enclosingClassType, Messager m, Collection<TypeMirror> superRetainEnabledTypes, Types typeUtils) {
        super(enclosingClassType, m);

        closestSuperBeRetainedFragment = SuperBeRetainedFragmentTypeFinder.getSuperBeRetainedFragment(enclosingClassType,
                superRetainEnabledTypes,
                typeUtils,
                m);

        TypeName superclass = getSuperTypeName();
        if(superclass != null) {
            saveRestoreClassBuilder.superclass(superclass);
        }
    }

    protected abstract TypeName getSuperTypeName();
}
