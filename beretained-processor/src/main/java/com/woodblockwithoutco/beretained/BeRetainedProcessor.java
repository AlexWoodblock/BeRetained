package com.woodblockwithoutco.beretained;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.woodblockwithouco.beretained.Retain;
import com.woodblockwithoutco.beretained.android.AndroidClasses;
import com.woodblockwithoutco.beretained.builder.AndroidBridgeClassBuilder;
import com.woodblockwithoutco.beretained.builder.SupportBeRetainedFragmentClassBuilder;
import com.woodblockwithoutco.beretained.info.FieldDescription;

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
 * Created by aleksandr on 6/11/16.
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

        final Set<TypeMirror> retainEnabledClasses = new HashSet<>();
        final Map<TypeName, TypeMirror> retainEnabledClassesMap = new HashMap<>();
        final Map<TypeName, List<FieldDescription>> classFieldMap = new HashMap<>();
        for (VariableElement field : retainedFields) {
            TypeMirror fieldType = field.asType();
            TypeName fieldClass = TypeName.get(fieldType);
            String fieldName = field.getSimpleName().toString();

            if (!validate(field)) {
                return true;
            }

            TypeMirror enclosingClassType = field.getEnclosingElement().asType();
            TypeName enclosingClass = TypeName.get(enclosingClassType);
            retainEnabledClassesMap.put(enclosingClass, enclosingClassType);
            retainEnabledClasses.add(enclosingClassType);

            List<FieldDescription> fields = classFieldMap.get(enclosingClass);
            if (fields == null) {
                fields = new ArrayList<>();
                classFieldMap.put(enclosingClass, fields);
            }
            fields.add(new FieldDescription(fieldClass, fieldName));
        }

        for (TypeName clazz : classFieldMap.keySet()) {
            TypeMirror enclosingType = retainEnabledClassesMap.get(clazz);
            Set<TypeMirror> retainEnabledClassesWithoutEnclosingType = new HashSet<>(retainEnabledClasses);
            retainEnabledClassesWithoutEnclosingType.remove(enclosingType);
            writeBeRetainedClasses(classFieldMap.get(clazz), enclosingType, retainEnabledClassesWithoutEnclosingType);
        }

        return true;
    }

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

        Element enclosingClass = element.getEnclosingElement();
        TypeElement typeElement = elements.getTypeElement(enclosingClass.asType().toString());
        if(typeElement == null) {
            messager.printMessage(ERROR, "Enclosing type must be a class!", element);
            return false;
        }


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

    private void writeBeRetainedClasses(List<FieldDescription> fields,
                                        TypeMirror enclosingClass,
                                        Collection<TypeMirror> retainEnabledClasses) {

        SupportBeRetainedFragmentClassBuilder beRetainedFragmentBuilder = new SupportBeRetainedFragmentClassBuilder(
                enclosingClass,
                messager,
                retainEnabledClasses,
                types
        );

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
