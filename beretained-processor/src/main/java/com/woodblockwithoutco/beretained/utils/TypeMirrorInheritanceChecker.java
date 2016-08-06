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

import java.util.List;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Helper class to check if TypeMirror inherits from given class
 */
public final class TypeMirrorInheritanceChecker {

    private TypeMirrorInheritanceChecker() {}

    /**
     * Checks if given TypeMirror inherits from given class.
     * @param enclosingType TypeMirror to check.
     * @param parentClass String description of parent class.
     * @param types TypeUtils from annotation processor.
     * @return True if inherits, false otherwise.
     */
    public static boolean checkTypeMirrorInheritance(TypeMirror enclosingType, String parentClass, Types types) {
        if(parentClass.equals(enclosingType.toString())) {
            return true;
        }

        //as classes should appear first in the list, only check them
        List<? extends TypeMirror> supertypes = types.directSupertypes(enclosingType);
        if(supertypes.size() > 0) {
            if(checkTypeMirrorInheritance(supertypes.get(0), parentClass, types)) {
                return true;
            }
        }

        return false;
    }
}
