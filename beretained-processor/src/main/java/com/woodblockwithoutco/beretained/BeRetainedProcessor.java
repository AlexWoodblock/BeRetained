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
package com.woodblockwithoutco.beretained;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.woodblockwithouco.beretained.Retain;
import com.woodblockwithoutco.beretained.android.AndroidClasses;
import com.woodblockwithoutco.beretained.builder.AndroidBridgeClassBuilder;
import com.woodblockwithoutco.beretained.builder.SupportBeRetainedFragmentClassBuilder;
import com.woodblockwithoutco.beretained.info.RetainedFieldDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * The annotation processor that handles @Retain annotations.
 */
@AutoService(Processor.class)
public class BeRetainedProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private Types types;
    private Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        types = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        elements = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Retain.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> allElements = roundEnv.getElementsAnnotatedWith(Retain.class);
        Set<VariableElement> retainedFields = ElementFilter.fieldsIn(allElements);

        final Set<TypeMirror> retainEnabledClasses =
                new HashSet<>(); //complete set of all retain-enabled classes
        final Map<TypeMirror, List<RetainedFieldDescription>> classFieldMap =
                new HashMap<>(); //retain-enabled-class -> list of retained fields

        for (VariableElement field : retainedFields) {
            TypeMirror fieldType = field.asType();
            String fieldName = field.getSimpleName().toString();

            if (!validate(field)) {
                //stop the processing if field is invalid
                return true;
            }

            TypeMirror enclosingClassType = field.getEnclosingElement().asType(); //type that contains retained fields
            retainEnabledClasses.add(enclosingClassType);

            List<RetainedFieldDescription> fields = classFieldMap.get(enclosingClassType);
            if (fields == null) {
                fields = new ArrayList<>();
                classFieldMap.put(enclosingClassType, fields);
            }

            //checking for @NonNull annotations
            List<? extends AnnotationMirror> annotationMirrors = field.getAnnotationMirrors();
            boolean nullAllowed = true;
            for(AnnotationMirror annotationMirror : annotationMirrors) {
                if(annotationMirror.toString().equals(AndroidClasses.ANDROID_SUPPORT_ANNOTATION_NON_NULL)) {
                    nullAllowed = false;
                }

                if(!nullAllowed) {
                    break;
                }
            }

            fields.add(new RetainedFieldDescription(fieldType, fieldName, nullAllowed));
        }

        //now, after all the classes have been found, start actual code generation
        for (TypeMirror enclosingType : classFieldMap.keySet()) {
            //build the set that doesn't contain current type - it's used to search for inheritance
            Set<TypeMirror> retainEnabledClassesWithoutEnclosingType = new HashSet<>(retainEnabledClasses);
            retainEnabledClassesWithoutEnclosingType.remove(enclosingType);
            writeBeRetainedClasses(classFieldMap.get(enclosingType),
                    enclosingType,
                    retainEnabledClassesWithoutEnclosingType);
        }

        return true;
    }

    //validate field - it should not be final or private
    private boolean validate(VariableElement element) {
        //must be package-accessible, protected or public
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            messager.printMessage(ERROR, element.getSimpleName() +
                            " in class " +
                            element.getEnclosingElement().asType().toString() +
                            " must have package, protected or public access modifier",
                    element);

            return false;
        }

        //must not be final
        if (element.getModifiers().contains(Modifier.FINAL)) {
            messager.printMessage(ERROR, element.getSimpleName() +
                            " in class " +
                            element.getEnclosingElement().asType().toString() +
                            " must not be final",
                    element);
            return false;
        }

        //this is something I can't imagine happening - fields that are not in the class - but let's check it,
        //just to be sure
        Element enclosingClass = element.getEnclosingElement();
        TypeElement typeElement = elements.getTypeElement(enclosingClass.asType().toString());
        if(typeElement == null) {
            messager.printMessage(ERROR, "Enclosing type must be a class, but it's a " + enclosingClass.asType().toString(), element);
            return false;
        }

        //check if fields marked with @Retain are in FragmentActivity
        boolean enclosedInFragmentActivity = validateIsEnclosedInFragmentActivity(enclosingClass.asType());

        if(!enclosedInFragmentActivity) {
            messager.printMessage(ERROR,
                    "Fields marked with @Retain annotation must be placed in " +
                            AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_ACTIVITY_CLASS_NAME +
                            " or it's subclass!",
                    element);
            return false;
        }

        return true;
    }

    //check if class containing @Retain fields is FragmentActivity or it's subclasses
    private boolean validateIsEnclosedInFragmentActivity(TypeMirror enclosingType) {
        if(AndroidClasses.ANDROID_SUPPORT_V4_APP_FRAGMENT_ACTIVITY_CLASS_NAME.equals(enclosingType.toString())) {
            return true;
        }

        //as classes should appear first in the list, only check them
        List<? extends TypeMirror> supertypes = types.directSupertypes(enclosingType);
        if(supertypes.size() > 0) {
            if(validateIsEnclosedInFragmentActivity(supertypes.get(0))) {
                return true;
            }
        }

        return false;
    }

    private void writeBeRetainedClasses(List<RetainedFieldDescription> fields,
                                        TypeMirror enclosingClass,
                                        Collection<TypeMirror> retainEnabledClasses) {

        SupportBeRetainedFragmentClassBuilder beRetainedFragmentBuilder = new SupportBeRetainedFragmentClassBuilder(
                enclosingClass,
                messager,
                retainEnabledClasses,
                types
        );

        //these calls must be in this exact order, otherwise addBody() won't have any fields to add
        beRetainedFragmentBuilder.setFields(fields);
        beRetainedFragmentBuilder.addBody();

        JavaFile fragment = beRetainedFragmentBuilder.build();
        writeJavaFile(fragment);

        AndroidBridgeClassBuilder bridgeClassBuilder = new AndroidBridgeClassBuilder(enclosingClass, messager);
        bridgeClassBuilder.addBody();
        writeJavaFile(bridgeClassBuilder.build());
    }

    private void writeJavaFile(JavaFile javaFile) {
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(ERROR, "Error during writing generated class: " + e.getMessage());
        }
    }
}
