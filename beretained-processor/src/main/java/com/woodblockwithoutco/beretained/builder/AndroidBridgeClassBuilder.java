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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.woodblockwithoutco.beretained.android.AndroidClasses;
import com.woodblockwithoutco.beretained.android.Suffixes;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

/**
 * Class builder for &lt;ActivityName&gt;FieldsRetainer -
 * classes that Activities will have to call in order to save/restore fields.
 */
public class AndroidBridgeClassBuilder extends SaveRestoreClassBuilder {

    private static final String TARGET_PKG = "com.woodblockwithoutco.beretained";

    private String fragmentManagerTag; //tag for retained fragment
    private ClassName beRetainedFragmentClass; //storing retained fragment class for JavaPoet usage

    public AndroidBridgeClassBuilder(TypeMirror retainEnabledType, Messager messager) {
        super(retainEnabledType, messager);

        if(enclosingClass != null) {
            fragmentManagerTag = enclosingClass.simpleName() + "_BeRetainedFragment";

            beRetainedFragmentClass = ClassName.get(enclosingClass.packageName(), enclosingClass.simpleName() + Suffixes.BERETAINED_FRAGMENT_SUFFIX);
            saveRestoreClassBuilder.addSuperinterface(ParameterizedTypeName.get(AndroidClasses.COM_WOODBLOCKWITHOUTCO_BERETAINED_FIELDS_RETAINER, enclosingClass));
            saveRestoreClassBuilder.addModifiers(Modifier.PUBLIC);
        }
    }

    @Override
    protected String getSuffix() {
        return Suffixes.FIELDS_RETAINER_SUFFIX;
    }

    @Override
    protected CodeBlock getSaveMethodCode(String sourceArgName) {
        //finding fragment, if it's null - throwing exception, if it's there - put instances into it
        CodeBlock.Builder saveCode = CodeBlock.builder().
                addStatement("$T fm = $L.getSupportFragmentManager()", AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS, sourceArgName).
                addStatement("$T fragment = ($T) fm.findFragmentByTag($S)", beRetainedFragmentClass, beRetainedFragmentClass, fragmentManagerTag).
                beginControlFlow("if (fragment == null)").
                addStatement("throw new NullPointerException($S)", "Did you forget to call " + enclosingClass.simpleName() + Suffixes.FIELDS_RETAINER_SUFFIX + ".onCreate() inside onCreate(Bundle) method?").
                endControlFlow().
                addStatement("fragment.save($L)", sourceArgName);

        return saveCode.build();
    }

    @Override
    protected CodeBlock getRestoreMethodCode(String targetArgName) {
        //finding fragment, if it's not there - adding it to FragmentManager,
        //and after all try to restore instances from it.
        CodeBlock.Builder restoreCode = CodeBlock.builder().
                addStatement("$T fm = $L.getSupportFragmentManager()", AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS, targetArgName).
                addStatement("$T fragment = ($T) fm.findFragmentByTag($S)", beRetainedFragmentClass, beRetainedFragmentClass, fragmentManagerTag).
                beginControlFlow("if(fragment == null)").
                addStatement("throw new NullPointerException($S)", "Did you forget to call " + enclosingClass.simpleName() + Suffixes.FIELDS_RETAINER_SUFFIX + ".onCreate() inside onCreate(Bundle) method?").
                endControlFlow().
                addStatement("return fragment.restore($L)", targetArgName);

        return restoreCode.build();
    }

    @Override
    public void addBody() {
        super.addBody();

        //adding package-level constructor
        MethodSpec privateConstructor = MethodSpec.
                constructorBuilder().
                build();

        saveRestoreClassBuilder.addMethod(privateConstructor);

        //adding onCreate method to add fragment
        final String activityArgName = "activity";
        CodeBlock.Builder onCreateCode = CodeBlock.builder().
                addStatement("$T fm = $L.getSupportFragmentManager()", AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_MANAGER_CLASS, activityArgName).
                addStatement("$T fragment = ($T) fm.findFragmentByTag($S)", beRetainedFragmentClass, beRetainedFragmentClass, fragmentManagerTag).
                beginControlFlow("if(fragment == null)").
                addStatement("fragment = new $T()", beRetainedFragmentClass).
                addStatement("fm.beginTransaction().add(fragment, $S).commit()", fragmentManagerTag).
                addStatement("fm.executePendingTransactions()").
                endControlFlow();

        ParameterSpec.Builder activityParameter = ParameterSpec.builder(enclosingClass, activityArgName);
        MethodSpec onCreateMethod = MethodSpec.
                methodBuilder("onCreate").
                addParameter(activityParameter.build()).
                addCode(onCreateCode.build()).
                addModifiers(Modifier.PUBLIC).
                addAnnotation(Override.class).
                build();

        saveRestoreClassBuilder.addMethod(onCreateMethod);
    }

    @Override
    protected MethodSpec.Builder getSaveMethodSkeleton() {
        //adding static modifier for easier usage
        MethodSpec.Builder saveMethodSkeleton = super.getSaveMethodSkeleton();
        saveMethodSkeleton.addAnnotation(Override.class);
        return saveMethodSkeleton;
    }

    @Override
    protected MethodSpec.Builder getRestoreMethodSkeleton() {
        //adding static modifier for easier usage
        MethodSpec.Builder restoreMethodSkeleton = super.getRestoreMethodSkeleton();
        restoreMethodSkeleton.addAnnotation(Override.class);
        return restoreMethodSkeleton;
    }

    @Override
    protected String getTargetPackage() {
        return TARGET_PKG;
    }
}
