/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery.processor;

import com.shelfmap.interfaceprocessor.FieldModifier;
import com.shelfmap.interfaceprocessor.InterfaceDefinition;
import com.shelfmap.interfaceprocessor.InterfaceProcessor;
import com.shelfmap.interfaceprocessor.Property;
import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.annotation.*;
import com.shelfmap.simplequery.attribute.Attributes;
import com.shelfmap.simplequery.attribute.ConditionAttribute;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.domain.impl.DefaultReverseToManyDomainReference;
import com.shelfmap.simplequery.domain.impl.DefaultReverseToOneDomainReference;
import com.shelfmap.simplequery.domain.impl.DefaultToOneDomainReference;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 * @author Tsutomu YANO
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"com.shelfmap.interfaceprocessor.annotation.GenerateClass"})
public class SimpleQueryProcessor extends InterfaceProcessor {

    @Override
    protected boolean precheck(InterfaceDefinition definition, AnnotationMirror annotation, String className, Element element) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();
        Messager messager = processingEnv.getMessager();

        boolean result = true;
        for (Property property : definition.getProperties()) {
            TypeMirror type = property.getType();
            if(isDomainReference(type)) {
                if(property.isWritable()) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "A property the type is a subtype of DomainReference must not have a setter method. It must be read-only.", property.getWriter());
                    result = false;
                }

                //the interface must have a 'context' property if the interface
                //have a DomainReference property.
                TypeMirror contextType = elementUtils.getTypeElement(Context.class.getName()).asType();
                if(definition.findProperty("context", contextType, typeUtils) == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "The interface must have a property named as 'context' and typed as com.shelfmap.Context if the interface have a property typed as DomainReference.", element, annotation);
                    result = false;
                }
            } else if(isInt(type)) {
                if(property.getReader().getAnnotation(IntAttribute.class) == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "A property the type is int or java.lang.Integer must have a @com.shelfmap.simplequery.annotation.IntAttribute annotation on the getter method.", property.getReader());
                }
            } else if(isLong(type)) {
                if(property.getReader().getAnnotation(LongAttribute.class) == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "A property the type is long or java.lang.Long must have a @com.shelfmap.simplequery.annotation.LongAttribute annotation on the getter method.", property.getReader());
                }
            } else if(isFloat(type)) {
                if(property.getReader().getAnnotation(FloatAttribute.class) == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "A property the type is float or java.lang.Float must have a @com.shelfmap.simplequery.annotation.FloatAttribute annotation on the getter method.", property.getReader());
                }
            } else if(isCollection(type)) {
                if(property.getReader().getAnnotation(Container.class) == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "A property the type is Array or a subtype of Collection must have a @com.shelfmap.simplequery.annotation.Container annotation on the getter method.", property.getReader());
                }
            }
        }

        if(result) {
            return super.precheck(definition, annotation, className, element);
        } else {
            return result;
        }
    }

    @Override
    protected int generatePropertyFieldInitializer(Writer writer, int shift, TypeElement element, Property property) throws IOException {
        TypeMirror propertyType = property.getType();

        if(isForwardToOneReference(propertyType)) {
            shift = generateForwardToOneReferenceInitialization(writer, shift, element, property);
        } else if(isReverseToOneDomainReference(propertyType)) {
            shift = generateReverseToOneReferenceInitialization(writer, shift, element, property);
        } else if(isReverseToManyDomainReference(propertyType)) {
            shift = generateReverseToManyReferenceInitialization(writer, shift, element, property);
        } else {
            shift = super.generatePropertyFieldInitializer(writer, shift, element, property);
        }
        return shift;
    }

    protected int generateForwardToOneReferenceInitialization(Writer writer, int shift, TypeElement element, Property property) throws IOException {
        Types typeUtils = processingEnv.getTypeUtils();

        DeclaredType propertyType = (DeclaredType)property.getType();
        TypeMirror targetDomainType = propertyType.getTypeArguments().get(0);
        TypeMirror domainType = typeUtils.erasure(getTypeMirrorOf(Domain.class));

        String domainVarName = property.getName() + "TargetDomain";
        writer.append(indent(shift)).append(domainType.toString()).append("<").append(targetDomainType.toString()).append("> ")
              .append(domainVarName).append(" = context.getDomainFactory().createDomain(").append(targetDomainType.toString()).append(".class);\n");

        writer.append(indent(shift))
              .append("this.").append(toSafeName(property.getName()))
              .append(" = new ").append(getForwardToOneDomainReferenceType().toString())
                                .append("<").append(targetDomainType.toString()).append(">")
                                .append("(context, ")
                                .append(domainVarName)
                                .append(");\n");

        return shift;
    }

    protected int generateReverseReferenceInitalization(Writer writer, int shift, TypeElement element, Property property, boolean toMany) throws IOException {
        Types typeUtils = processingEnv.getTypeUtils();

        DeclaredType propertyType = (DeclaredType)property.getType();
        List<? extends TypeMirror> typeArguments = propertyType.getTypeArguments();
        TypeMirror targetDomainType = typeArguments.get(0);
        TypeMirror masterDomainType = element.asType();
        TypeMirror domainType = typeUtils.erasure(getTypeMirrorOf(Domain.class));
        TypeMirror conditionAttributeType = getTypeMirrorOf(ConditionAttribute.class);
        TypeMirror attributesType = getTypeMirrorOf(Attributes.class);

        String domainVarName = property.getName() + "TargetDomain";
        writer.append(indent(shift)).append(domainType.toString()).append("<").append(targetDomainType.toString()).append("> ")
              .append(domainVarName).append(" = context.getDomainFactory().createDomain(").append(targetDomainType.toString()).append(".class);\n");

        //retrieve @Reverse annotation from a property.
        //all properties typed as one of ReverseDomainReference must have a @Reverse annotation on the getter method definition.
        ExecutableElement getter = property.getReader();
        Reverse reverseAnnotation = getter.getAnnotation(Reverse.class);
        if(reverseAnnotation == null) {
            Messager messager = processingEnv.getMessager();
            messager.printMessage(Diagnostic.Kind.ERROR, "Property typed as ReverseDomainReference must have a @Reverse annotation on the getter method.", getter);
        }

        String targetAttribute = reverseAnnotation.targetAttributeName();
        String targetAttributeVarName = property.getName() + "TargetAttribute";
        writer.append(indent(shift)).append(conditionAttributeType.toString()).append(" ")
              .append(targetAttributeVarName).append(" = ")
              .append(attributesType.toString()).append(".attr(\"").append(targetAttribute).append("\");\n");

        writer.append(indent(shift))
              .append("this.").append(toSafeName(property.getName()))
              .append(" = new ").append(toMany ? getReverseToManyDomainReferenceType().toString() : getReverseToOneDomainReferenceType().toString())
                                .append("<")
                                .append(masterDomainType.toString()).append(",")
                                .append(targetDomainType.toString())
                                .append(">")
                                .append("(context, this, ")
                                .append(domainVarName).append(", ")
                                .append(targetAttributeVarName)
                                .append(");\n");

        return shift;
    }

    protected int generateReverseToOneReferenceInitialization(Writer writer, int shift, TypeElement element, Property property) throws IOException {
        return generateReverseReferenceInitalization(writer, shift, element, property, false);
    }

    protected int generateReverseToManyReferenceInitialization(Writer writer, int shift, TypeElement element, Property property) throws IOException {
        return generateReverseReferenceInitalization(writer, shift, element, property, true);
    }

    protected final DeclaredType getTypeMirrorOf(Class<?> clazz) {
        Elements elementUtils = processingEnv.getElementUtils();
        return (DeclaredType) elementUtils.getTypeElement(clazz.getName()).asType();
    }

    protected int generateReverseToOneReferenceField(Property property, Writer writer, int shift, FieldModifier modifier) throws IOException {
        TypeMirror propertyType = property.getType();
        String modifierStr = modifier.getModifier() + (modifier == FieldModifier.DEFAULT ? "" : " ");
        String typeName = propertyType.toString();

        writer.append(indent(shift)).append(modifierStr).append(typeName).append(" ").append(toSafeName(property.getName())).append(";\n");
        return shift;
    }

    protected TypeMirror getReverseToManyDomainReferenceType() {
        Types typeUtils = processingEnv.getTypeUtils();
        return typeUtils.erasure(getTypeMirrorOf(DefaultReverseToManyDomainReference.class));
    }

    protected TypeMirror getReverseToOneDomainReferenceType() {
        Types typeUtils = processingEnv.getTypeUtils();
        return typeUtils.erasure(getTypeMirrorOf(DefaultReverseToOneDomainReference.class));
    }

    protected TypeMirror getForwardToOneDomainReferenceType() {
        Types typeUtils = processingEnv.getTypeUtils();
        return typeUtils.erasure(getTypeMirrorOf(DefaultToOneDomainReference.class));
    }

    private boolean isSubtypeIfErased(TypeMirror type, Class<?> targetType) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();

        TypeMirror erasedTargetType = typeUtils.erasure(elementUtils.getTypeElement(targetType.getName()).asType());
        TypeMirror erasedType = typeUtils.erasure(type);

        return typeUtils.isSubtype(erasedType, erasedTargetType);
    }

    private boolean isDomainReference(TypeMirror type) {
        return isSubtypeIfErased(type, DomainReference.class);
    }

    private boolean isForwardToOneReference(TypeMirror type) {
        return isSubtypeIfErased(type, ToOneDomainReference.class);
    }

    private boolean isReverseToOneDomainReference(TypeMirror type) {
        return isSubtypeIfErased(type, ReverseToOneDomainReference.class);
    }

    private boolean isReverseToManyDomainReference(TypeMirror type) {
        return isSubtypeIfErased(type, ReverseToManyDomainReference.class);
    }

    private boolean isReverseReference(TypeMirror type) {
        return isSubtypeIfErased(type, ReverseReference.class);
    }

    private boolean isInt(TypeMirror type) {
        Types typeUtils = processingEnv.getTypeUtils();
        PrimitiveType intType = typeUtils.getPrimitiveType(TypeKind.INT);
        return isSubtypeIfErased(type, Integer.class) || typeUtils.isSameType(type, intType);
    }

    private boolean isLong(TypeMirror type) {
        Types typeUtils = processingEnv.getTypeUtils();
        PrimitiveType longType = typeUtils.getPrimitiveType(TypeKind.LONG);
        return isSubtypeIfErased(type, Long.class) || typeUtils.isSameType(type, longType);
    }

    private boolean isFloat(TypeMirror type) {
        Types typeUtils = processingEnv.getTypeUtils();
        PrimitiveType floatType = typeUtils.getPrimitiveType(TypeKind.FLOAT);
        return isSubtypeIfErased(type, Float.class) || typeUtils.isSameType(type, floatType);
    }

    private boolean isCollection(TypeMirror type) {
        return isSubtypeIfErased(type, Collection.class) || (type instanceof ArrayType);
    }

    private boolean hasDomainReferenceProperty(InterfaceDefinition definition) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();
        final TypeMirror domainReferenceType = typeUtils.erasure(elementUtils.getTypeElement(DomainReference.class.getName()).asType());
        for (Property property : definition.getProperties()) {
            TypeMirror propertyType = typeUtils.erasure(property.getType());
            if(typeUtils.isSubtype(propertyType, domainReferenceType)) {
                return true;
            }
        }
        return false;
    }
}
