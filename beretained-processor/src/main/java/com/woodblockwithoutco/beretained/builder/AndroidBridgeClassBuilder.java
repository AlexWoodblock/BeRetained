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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.android.AndroidClasses;
import com.woodblockwithoutco.beretained.android.Suffixes;


import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class AndroidBridgeClassBuilder extends SaveRestoreClassBuilder {

    private String fragmentManagerTag;
    private ClassName beRetainedFragmentClass;

    public AndroidBridgeClassBuilder(TypeMirror retainEnabledType, Messager messager) {
        super(retainEnabledType, messager);

        if(enclosingClass != null) {
            fragmentManagerTag = enclosingClass.simpleName() + "_BeRetainedFragment";

            beRetainedFragmentClass = ClassName.get(enclosingClass.packageName(), enclosingClass.simpleName() + Suffixes.BERETAINED_FRAGMENT_SUFFIX);
        }
    }

    @Override
    protected String getSuffix() {
        return Suffixes.FIELDS_RETAINER_SUFFIX;
    }

    @Override
    protected CodeBlock getSaveMethodCode(String sourceArgName) {
        CodeBlock.Builder saveCode = CodeBlock.builder().
                addStatement("$T fm = source.getSupportFragmentManager()", AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS).
                addStatement("$T container = ($T) fm.findFragmentByTag($S)", beRetainedFragmentClass, beRetainedFragmentClass, fragmentManagerTag).
                beginControlFlow("if (container == null)").
                addStatement("throw new IllegalStateException($S)", "Did you forget to call FieldsRetainer.restore() inside onCreate(Bundle) method?").
                endControlFlow().
                addStatement("container.save($L)", sourceArgName);

        return saveCode.build();
    }

    @Override
    protected CodeBlock getRestoreMethodCode(String targetArgName) {
        CodeBlock.Builder restoreCode = CodeBlock.builder().
                addStatement("$T fm = target.getSupportFragmentManager()", AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS).
                addStatement("$T container = ($T) fm.findFragmentByTag($S)", beRetainedFragmentClass, beRetainedFragmentClass, fragmentManagerTag).
                beginControlFlow("if(container == null)").
                addStatement("container = new $T()", beRetainedFragmentClass).
                addStatement("fm.beginTransaction().add(container, $S).commit()", fragmentManagerTag).
                endControlFlow().
                addStatement("return container.restore($L)", targetArgName);

        return restoreCode.build();
    }

    @Override
    protected MethodSpec.Builder getSaveMethodSkeleton() {
        MethodSpec.Builder saveMethodSkeleton = super.getSaveMethodSkeleton();
        saveMethodSkeleton.addModifiers(Modifier.STATIC);
        return saveMethodSkeleton;
    }

    @Override
    protected MethodSpec.Builder getRestoreMethodSkeleton() {
        MethodSpec.Builder restoreMethodSkeleton = super.getRestoreMethodSkeleton();
        restoreMethodSkeleton.addModifiers(Modifier.STATIC);
        return restoreMethodSkeleton;
    }
}
