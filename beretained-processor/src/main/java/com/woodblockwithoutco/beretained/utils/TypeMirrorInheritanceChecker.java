package com.woodblockwithoutco.beretained.utils;

import com.woodblockwithoutco.beretained.android.AndroidClasses;

import java.util.List;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Helper class to check if TypeMirror inherits from given class
 */
public final class TypeMirrorInheritanceChecker {

    private TypeMirrorInheritanceChecker() {}

    //check if class containing @Retain fields is FragmentActivity or it's subclasses
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
