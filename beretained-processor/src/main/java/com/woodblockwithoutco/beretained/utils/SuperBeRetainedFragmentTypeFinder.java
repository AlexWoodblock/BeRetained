/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
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
 * Helper class to find fitting Fragment class for closest superclass of Activity that also uses retained fields.
 */
public final class SuperBeRetainedFragmentTypeFinder {

    private SuperBeRetainedFragmentTypeFinder() {}

    /**
     * Find fitting Fragment superclass for BeRetained fragment for Activity that extends from Activity that also uses retained fields.
     * @param type TypeMirror for current type we are processing
     * @param superRetainEnabledTypes Collection of types that have retained fields(excluding the class we are currently processing).
     * @param typeUtils Type utils from processing environment.
     * @param messager Messager for error logging.
     * @return TypeName of the Fragment that needs to be a superclass for generated Fragment or null if not found.
     */
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
