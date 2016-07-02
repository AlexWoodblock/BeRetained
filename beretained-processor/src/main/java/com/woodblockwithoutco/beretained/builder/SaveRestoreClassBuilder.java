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
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Base class for building classes that will contain save()/restore() method pair.
 */
abstract class SaveRestoreClassBuilder implements ClassBuilder {

    private final static String TARGET_ARG_NAME = "target"; //parameter name for restore() method
    private final static String SOURCE_ARG_NAME = "source"; //parameter name for save() method

    protected Messager messager;

    protected ClassName enclosingClass; //class that contains retained fields

    protected TypeSpec.Builder saveRestoreClassBuilder;

    protected MethodSpec.Builder saveMethodBuilder;
    protected MethodSpec.Builder restoreMethodBuilder;

    public SaveRestoreClassBuilder(TypeMirror enclosingClassType,
                                   Messager m) {
        messager = m;

        TypeName enclosingType = ClassName.get(enclosingClassType);
        if(!(enclosingType instanceof ClassName)) {
            //this should not happen, but let's handle it just in case
            messager.printMessage(ERROR, "Enclosing type is not a class but a " + (enclosingClassType != null ? enclosingClassType.toString() : "null"));
            return;
        }

        this.enclosingClass = (ClassName) enclosingType;

        saveRestoreClassBuilder = TypeSpec.
                classBuilder(enclosingClass.simpleName() + getSuffix())
                .addModifiers(Modifier.PUBLIC);
    }

    @Override
    public void addBody() {
        saveMethodBuilder = getSaveMethodSkeleton();
        restoreMethodBuilder = getRestoreMethodSkeleton();

        CodeBlock saveMethodCodeBlock = getSaveMethodCode(SOURCE_ARG_NAME);
        if(saveMethodCodeBlock != null) {
            saveMethodBuilder.addCode(saveMethodCodeBlock);
        }
        saveRestoreClassBuilder.addMethod(saveMethodBuilder.build());

        CodeBlock restoreMethodCodeBlock = getRestoreMethodCode(TARGET_ARG_NAME);
        if(restoreMethodCodeBlock != null) {
            restoreMethodBuilder.addCode(restoreMethodCodeBlock);
        }
        saveRestoreClassBuilder.addMethod(restoreMethodBuilder.build());
    }

    /**
     * Returns suffix for class name that will be generated. Must reflect purpose of generated class.
     * @return Suffix for generated class name.
     */
    protected abstract String getSuffix();

    /**
     * Returns code that will be run on save() method invocation.
     * @param sourceArgName Name of parameter that will be passed to save() method.
     * @return Code for save() method.
     */
    protected abstract CodeBlock getSaveMethodCode(String sourceArgName);

    /**
     * Returns code that will be run on save() method invocation.
     * @param targetArgName Name of parameter that will be passed to restore() method.
     * @return Code for restore() method.
     */
    protected abstract CodeBlock getRestoreMethodCode(String targetArgName);

    /**
     * Returns target package for generated class.
     * @return Target package for generated class.
     */
    protected String getTargetPackage() {
        return enclosingClass.packageName();
    }

    protected MethodSpec.Builder getRestoreMethodSkeleton() {
        ParameterSpec parameterSpecTarget = ParameterSpec.builder(enclosingClass, TARGET_ARG_NAME).build();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("restore").
                addParameter(parameterSpecTarget).
                addModifiers(Modifier.PUBLIC).
                returns(TypeName.BOOLEAN);

        return methodBuilder;
    }

    protected MethodSpec.Builder getSaveMethodSkeleton() {
        ParameterSpec parameterSpecSource = ParameterSpec.builder(enclosingClass, SOURCE_ARG_NAME).build();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("save").
                addParameter(parameterSpecSource).
                addModifiers(Modifier.PUBLIC);
        return methodBuilder;
    }

    @Override
    public JavaFile build() {
        JavaFile.Builder builder = JavaFile.builder(getTargetPackage(), saveRestoreClassBuilder.build());
        return builder.build();
    }
}
