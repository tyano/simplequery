/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery.processing;

import com.shelfmap.simplequery.processing.impl.DefaultProperty;
import static com.shelfmap.simplequery.util.Strings.*;
import javax.lang.model.element.*;


import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Types;

/**
 *
 * @author Tsutomu YANO
 */
public class DomainInterfaceVisitor extends ElementScanner6<Void, Environment> {

    /**
     * {@inheritDoc }
     *
     * we here output the line of class-definition of this interface.
     */
    @Override
    public Void visitType(TypeElement element, Environment env) {
        if(element.getKind() != ElementKind.INTERFACE) return super.visitType(element, env);

        //if the element have some super-interfaces, visit them at first.
        for (TypeMirror superType : element.getInterfaces()) {
            Element superInterface = env.getProcessingEnvironment().getTypeUtils().asElement(superType);
            env.setLevel(env.getLevel() + 1);
            this.visit(superInterface, env);
            env.setLevel(env.getLevel() - 1);
        }

        InterfaceDefinition definition = env.getInterfaceDefinition();

        if(env.getLevel() == 0) {
            String[] splited = splitPackageName(element.getQualifiedName().toString());
            if(splited == null) {
                throw new IllegalStateException("the qualified name of the element " + element.toString() + " was a null or an empty string.");
            }
            definition.setPackage(splited[0]);
            definition.setInterfaceName(splited[1]);
            definition.addTypeParameters(element.getTypeParameters().toArray(new TypeParameterElement[0]));
        }
        return super.visitType(element, env);
    }

    private String[] splitPackageName(String value) {
        if(value == null) return null;
        if(value.isEmpty()) return null;
        int lastIndexOfDot = value.lastIndexOf('.');
        if(lastIndexOfDot < 0) return new String[]{"", value};
        return new String[]{ value.substring(0, lastIndexOfDot), value.substring(lastIndexOfDot + 1, value.length()) };
    }

    @Override
    public Void visitExecutable(ExecutableElement ee, Environment env) {
        //handle only methods.
        if(ee.getKind() != ElementKind.METHOD) return super.visitExecutable(ee, env);

        //this visitor handle methods only in interface.
        Element enclosing = ee.getEnclosingElement();
        if(enclosing == null || enclosing.getKind() != ElementKind.INTERFACE) return super.visitExecutable(ee, env);

        InterfaceDefinition definition = env.getInterfaceDefinition();

        Types typeUtils = env.getProcessingEnvironment().getTypeUtils();
        Property property = buildPropertyFromExecutableElement(ee, typeUtils);

        //if the building of property object is succeed, the ee is a variation of a property (readable or writable)
        if(property != null) {
            Property prev = definition.findProperty(property.getName(), property.getType(), typeUtils);

            //if a property having same name and same type is already added to InterfaceDifinition,
            //we merge their readable and writable attribute into the previously added object.
            if(prev != null) {
                mergeProperty(prev, property);
            } else {
                //new property. simplly add it into InterfaceDefinition.
                definition.addProperties(property);
            }
        } else {
            //found a method which is not a part of a property.
            definition.addMethods(ee);
        }
        return super.visitExecutable(ee, env);
    }

    private void mergeProperty(Property p1, Property p2) {
        if(p2.isReadable()) {
            p1.setReadable(true);
        }

        if(p2.isWritable()) {
            p1.setWritable(true);
        }
    }

    private Property buildPropertyFromExecutableElement(ExecutableElement ee, Types types) {
        String name = ee.getSimpleName().toString();

        if(name.startsWith("get")) {
            return new DefaultProperty(uncapitalize(name.substring(3)), ee.getReturnType(), true, false);
        } else if(name.startsWith("set")) {
            if(ee.getParameters().size() == 1) {
                return new DefaultProperty(uncapitalize(name.substring(3)), ee.getParameters().get(0).asType(), false, true);
            }
        } else if(name.startsWith("is")) {
            PrimitiveType bool = types.getPrimitiveType(TypeKind.BOOLEAN);
            if(types.isSameType(ee.getReturnType(), bool) ||
               types.isSameType(ee.getReturnType(), types.boxedClass(bool).asType())) {

                return new DefaultProperty(uncapitalize(name.substring(2)), ee.getReturnType(), true, false);
            }
        }
        return null;
    }


}
