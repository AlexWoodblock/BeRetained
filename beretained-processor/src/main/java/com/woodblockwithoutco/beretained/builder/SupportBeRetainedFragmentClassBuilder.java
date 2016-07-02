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
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.android.AndroidClasses;
import com.woodblockwithoutco.beretained.android.Suffixes;
import com.woodblockwithoutco.beretained.info.RetainedFieldDescription;

import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Class builder for retained Fragments that will hold objects.
 */
public class SupportBeRetainedFragmentClassBuilder extends SuperBeRetainedFragmentAwareSaveRestoreClassBuilder {

    //name for flag that will have true set to it if save() was called on Fragment instance
    //it's needed to let the user know about whether the restore was successful(we had the instances) or not(we didn't have the instances)
    private final static String WAS_SAVED_FIELD_NAME = "wasSaved";

    private List<RetainedFieldDescription> fields;

    public SupportBeRetainedFragmentClassBuilder(TypeMirror enclosingClassType, Messager m, Collection<TypeMirror> superRetainEnabledTypes, Types typeUtils) {
        super(enclosingClassType, m, superRetainEnabledTypes, typeUtils);
    }

    @Override
    protected String getSuffix() {
        return Suffixes.BERETAINED_FRAGMENT_SUFFIX;
    }

    @Override
    protected TypeName getSuperTypeName() {
        //if we found super Fragment(when one Activity inherits from another), return it,
        //otherwise inherit from support Fragment
        if(closestSuperBeRetainedFragment != null) {
            return closestSuperBeRetainedFragment;
        } else {
            return AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_CLASS;
        }
    }

    @Override
    protected CodeBlock getSaveMethodCode(String sourceArgName) {
        if(fields == null) {
            messager.printMessage(ERROR, "No @Retain fields were added - looks like an internal bug, please contact the developer");
            return null;
        }

        CodeBlock.Builder builder = CodeBlock.builder();
        //if there's superclass - save flag will be set by it, otherwise set it ourselves
        if(closestSuperBeRetainedFragment != null) {
            builder.addStatement("super.save($L)", sourceArgName);
        } else {
            builder.addStatement("$L = true", WAS_SAVED_FIELD_NAME);
        }

        for(RetainedFieldDescription field : fields) {
            if(!field.nullAllowed) {
                //adding null check that will throw NPE in case we're trying to save null value if null is not allowed
                builder.beginControlFlow("if($L.$L == null)", sourceArgName, field.name);
                builder.addStatement("throw new $T($S)",
                        ClassName.get(NullPointerException.class),
                        "Trying to save null value from @NonNull field " + field.name);
                builder.endControlFlow();
            }

            //saving instance
            builder.addStatement("$L = $L.$L", field.name, sourceArgName, field.name);
        }
        return builder.build();
    }

    @Override
    protected CodeBlock getRestoreMethodCode(String targetArgName) {
        if(fields == null) {
            messager.printMessage(ERROR, "No @Retain fields were added - looks like an internal bug, please contact the developer");
            return null;
        }

        CodeBlock.Builder builder = CodeBlock.builder();

        if(closestSuperBeRetainedFragment != null) {
            //if there's super Fragment - take restoration status from it
            builder.addStatement("$T restored = super.restore($L)", TypeName.BOOLEAN, targetArgName);
        }

        //if there's super Fragment, then we have boolean variable called "restored",
        //otherwise we check mWasSaved field
        String restoreStateFieldName = closestSuperBeRetainedFragment != null ? "restored" : WAS_SAVED_FIELD_NAME;
        builder.beginControlFlow("if($L)", restoreStateFieldName);
        for(RetainedFieldDescription field : fields) {
            if(!field.nullAllowed) {
                //adding null check that will throw NPE in case we're trying to restore null value if null is not allowed
                builder.beginControlFlow("if($L == null)", field.name);
                builder.addStatement("throw new $T($S)",
                        ClassName.get(NullPointerException.class),
                        "Trying to restore restore null value to @NonNull field " + field.name);
                builder.endControlFlow();
            }

            //restoring instance
            builder.addStatement("$L.$L = $L", targetArgName, field.name, field.name);
        }

        builder.endControlFlow();

        //returning restoration status
        builder.addStatement("return $L", restoreStateFieldName);

        return builder.build();
    }

    @Override
    public void addBody() {
        if(fields == null) {
            messager.printMessage(ERROR, "No @Retain fields were added - looks like an internal bug, please contact the developer");
            return;
        }

        super.addBody();

        //adding fields that will keep the instances
        for(RetainedFieldDescription field : fields) {
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(field.type), field.name, Modifier.PRIVATE);
            saveRestoreClassBuilder.addField(fieldSpecBuilder.build());
        }

        //if there's no super Fragment - create mWasSaved field and onCreate method
        if(closestSuperBeRetainedFragment == null) {
            FieldSpec.Builder wasSavedFieldBuilder = FieldSpec.builder(TypeName.BOOLEAN, WAS_SAVED_FIELD_NAME, Modifier.PRIVATE);
            saveRestoreClassBuilder.addField(wasSavedFieldBuilder.build());

            final String icicleName = "savedInstanceState";
            MethodSpec.Builder onCreateMethod = MethodSpec.methodBuilder("onCreate");
            ParameterSpec.Builder savedInstanceStateParameter = ParameterSpec.builder(
                    AndroidClasses.ANDROID_OS_BUNDLE_CLASS,
                    icicleName);
            onCreateMethod.addParameter(savedInstanceStateParameter.build());
            onCreateMethod.addAnnotation(Override.class);
            onCreateMethod.addModifiers(Modifier.PUBLIC);

            //adding setRetainInstance(true) to onCreate body() - this is crucial
            //because otherwise fragment will be destroyed
            CodeBlock.Builder onCreateCode = CodeBlock.builder();
            onCreateCode.addStatement("super.onCreate($L)", icicleName);
            onCreateCode.addStatement("setRetainInstance(true)");
            onCreateMethod.addCode(onCreateCode.build());

            saveRestoreClassBuilder.addMethod(onCreateMethod.build());
        }
    }

    public void setFields(List<RetainedFieldDescription> fieldList) {
        fields = fieldList;
    }
}
