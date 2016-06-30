package com.woodblockwithoutco.beretained.builder;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.woodblockwithoutco.beretained.android.AndroidClasses;
import com.woodblockwithoutco.beretained.android.Suffixes;
import com.woodblockwithoutco.beretained.info.FieldDescription;

import java.util.Collection;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Created by aleksandr on 6/25/16.
 */
public class SupportBeRetainedFragmentClassBuilder extends SuperClassAwareSaveRestoreClassBuilder {

    private final static String WAS_SAVED_FIELD_NAME = "wasSaved";

    private List<FieldDescription> fields;

    public SupportBeRetainedFragmentClassBuilder(TypeMirror enclosingClassType, Messager m, Collection<TypeMirror> superRetainEnabledTypes, Types typeUtils) {
        super(enclosingClassType, m, superRetainEnabledTypes, typeUtils);
    }

    @Override
    protected String getSuffix() {
        return Suffixes.BERETAINED_FRAGMENT_SUFFIX;
    }

    @Override
    protected TypeName getSuperTypeName() {
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
        if(closestSuperBeRetainedFragment != null) {
            builder.addStatement("super.save($L)", sourceArgName);
        } else {
            builder.addStatement("$L = true", WAS_SAVED_FIELD_NAME);
        }

        for(FieldDescription field : fields) {
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
            builder.addStatement("$T restored = super.restore($L)", TypeName.BOOLEAN, targetArgName);
        }

        String restoreStateFieldName = closestSuperBeRetainedFragment != null ? "restored" : WAS_SAVED_FIELD_NAME;
        builder.beginControlFlow("if($L)", restoreStateFieldName);
        for(FieldDescription field : fields) {
            builder.addStatement("$L.$L = $L", targetArgName, field.name, field.name);
        }
        builder.endControlFlow();

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
        for(FieldDescription field : fields) {
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(field.typeName, field.name, Modifier.PROTECTED);
            saveRestoreClassBuilder.addField(fieldSpecBuilder.build());
        }

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

            CodeBlock.Builder onCreateCode = CodeBlock.builder();
            onCreateCode.addStatement("super.onCreate($L)", icicleName);
            onCreateCode.addStatement("setRetainInstance(true)");
            onCreateMethod.addCode(onCreateCode.build());

            saveRestoreClassBuilder.addMethod(onCreateMethod.build());
        }
    }

    public void setFields(List<FieldDescription> fieldList) {
        fields = fieldList;
    }
}
