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
package com.woodblockwithoutco.beretained.builder;

import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.utils.SuperBeRetainedFragmentTypeFinder;

import java.util.Collection;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

abstract class SuperClassAwareSaveRestoreClassBuilder extends SaveRestoreClassBuilder {

    protected TypeName closestSuperBeRetainedFragment;

    public SuperClassAwareSaveRestoreClassBuilder(TypeMirror enclosingClassType,
                                                  Messager m,
                                                  Collection<TypeMirror> superRetainEnabledTypes,
                                                  Types typeUtils) {
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
