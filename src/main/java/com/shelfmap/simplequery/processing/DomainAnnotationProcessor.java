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

import com.shelfmap.simplequery.annotation.GenerateClass;
import com.shelfmap.simplequery.processing.impl.BuildingEnvironment;
import com.shelfmap.simplequery.processing.impl.DefaultInterfaceDefinition;
import com.shelfmap.simplequery.util.IO;
import com.shelfmap.simplequery.util.Objects;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import static com.shelfmap.simplequery.util.Strings.capitalize;

import java.util.Date;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 *
 * @author Tsutomu YANO
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"com.shelfmap.simplequery.annotation.GenerateClass"})
public class DomainAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment re) {
        if(annotations.isEmpty()) return false;

        boolean processed = false;
        Set<? extends Element> elements = re.getElementsAnnotatedWith(GenerateClass.class);
        for (Element element : elements) {
            if(element.getKind() == ElementKind.INTERFACE) {
                GenerateClass generateAnnotation = element.getAnnotation(GenerateClass.class);
                assert generateAnnotation != null;

                if(!generateAnnotation.autoGenerate()) continue;

                InterfaceDefinition definition = new DefaultInterfaceDefinition();
                Environment visitorEnvironment = new BuildingEnvironment(processingEnv, definition);
                DomainInterfaceVisitor visitor = new DomainInterfaceVisitor();
                visitor.visit(element, visitorEnvironment);

                String packageName = resolvePackageName(generateAnnotation, definition.getPackage());
                String className = resolveImplementationClassName(generateAnnotation, definition.getInterfaceName());
                String fullClassName = packageName + "." + className;

                int readablePropertyCount = 0;
                int writablePropertyCount = 0;
                for (Property property : definition.getProperties()) {
                    if(property.isReadable()) readablePropertyCount++;
                    if(property.isWritable()) writablePropertyCount++;
                }

                Writer writer = null;
                try {
                    JavaFileObject javaFile = processingEnv.getFiler().createSourceFile(fullClassName);
                    writer = javaFile.openWriter();

                    String generationTime = String.format("%1$tY%1$tm%1$td-%1$tH%1$tk%1$tS-%1$tN%1$tz", new Date());
                    Types typeUtils = processingEnv.getTypeUtils();

                    writer.append("package ").append(packageName).append(";\n\n");
                    writer.append("@javax.annotation.Generated(\"" + generationTime + "\")\n");
                    writer.append("public ").append(definition.getMethods().isEmpty() ? "" : "abstract ").append("class ").append(className).append(" implements ").append(definition.getPackage() + "." + definition.getInterfaceName()).append(" {\n");

                    int shift = 1;
                    for (Property property : definition.getProperties()) {
                        String typeName = property.getType().toString();
                        writer.append(indent(shift)).append("private ").append(typeName).append(" ").append(toSafeName(property.getName())).append(";\n");
                    }

                    writer.append("\n");

                    {
                        writer.append(indent(shift)).append("public ").append(className).append("(");
                        boolean isFirst = true;
                        for (Property property : definition.getProperties()) {
                            if(!property.isWritable() && property.isReadable()) {
                                String type = property.getType().toString();
                                if(!isFirst) {
                                    writer.append(", ");
                                } else {
                                    isFirst = false;
                                }
                                writer.append(type).append(" ").append(toSafeName(property.getName()));
                            }
                        }
                        writer.append(") {\n");
                        shift++;
                        writer.append(indent(shift)).append("super();\n");
                        for (Property property : definition.getProperties()) {
                            if(!property.isWritable() && property.isReadable()) {
                                writer.append(indent(shift)).append("this.").append(toSafeName(property.getName())).append(" = ").append(retain(property)).append(";\n");
                            }
                        }
                        shift--;
                        writer.append(indent(shift)).append("}\n\n");
                    }

                    if(readablePropertyCount > writablePropertyCount) {
                        writer.append(indent(shift)).append("public ").append(className).append("(");
                        boolean isFirst = true;
                        for (Property property : definition.getProperties()) {
                            String type = property.getType().toString();
                            if(!isFirst) {
                                writer.append(", ");
                            } else {
                                isFirst = false;
                            }
                            writer.append(type).append(" ").append(toSafeName(property.getName()));
                        }
                        writer.append(") {\n");
                        shift++;
                        writer.append(indent(shift)).append("super();\n");
                        for (Property property : definition.getProperties()) {
                            writer.append(indent(shift)).append("this.").append(toSafeName(property.getName())).append(" = ").append(retain(property)).append(";\n");
                        }
                        shift--;
                        writer.append(indent(shift)).append("}\n\n");
                    }

                    for (Property property : definition.getProperties()) {
                        String propertyType = property.getType().toString();
                        if(property.isReadable()) {
                            writer.append(indent(shift)).append("@Override\n");
                            writer.append(indent(shift)).append("public ").append(propertyType).append(isBoolean(property.getType(), typeUtils) ? " is" : " get").append(capitalize(property.getName())).append("() {\n");
                            writer.append(indent(++shift)).append("return ").append(retain(property, "this.")).append(";\n");
                            writer.append(indent(--shift)).append("}\n\n");
                        }

                        if(property.isWritable()) {
                            writer.append(indent(shift)).append("@Override\n");
                            writer.append(indent(shift)).append("public void set").append(capitalize(property.getName())).append("(").append(propertyType).append(" ").append(toSafeName(property.getName())).append(") {\n");
                            writer.append(indent(++shift)).append("this.").append(toSafeName(property.getName())).append(" = ").append(retain(property)).append(";\n");
                            writer.append(indent(--shift)).append("}\n\n");
                        }
                    }
                    writer.append("}");
                    writer.flush();
                } catch (IOException ex) {
                    Logger.getLogger(DomainInterfaceVisitor.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    IO.close(writer, this);
                }
                processed = true;
            }
        }
        return processed;
    }


    private String retain(Property property) {
        return retain(property, "");
    }

    private String retain(Property property, String prefix) {
        assert property != null;

        String safeName = prefix + toSafeName(property.getName());
        RetainType type = RetainType.valueOf(property.getRetainType());
        return type.codeFor(safeName, property);
    }

    private String toSafeName(String word) {
        return Objects.isPreserved(word) ? "_" + word : word;
    }

    private boolean isBoolean(TypeMirror type, Types typeUtils) {
        switch(type.getKind()) {
            case BOOLEAN:
                return true;
            default:
                return typeUtils.isSameType(type, typeUtils.boxedClass(typeUtils.getPrimitiveType(TypeKind.BOOLEAN)).asType());
        }
    }

    private boolean isPrimitive(TypeMirror type) {
        switch(type.getKind()) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
            case VOID:
                return true;
            default:
                return false;
        }
    }

    private String indent(int indent) {
        if(indent <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < indent; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }

    private String resolvePackageName(GenerateClass domain, String interfacePackageName) {
        String packageName = domain.packageName();
        boolean isRelative = domain.isPackageNameRelative();

        if(packageName.isEmpty()) {
            return interfacePackageName + ".impl";
        } else {
            if(isRelative) {
                return interfacePackageName + "." + packageName;
            } else {
                return packageName;
            }
        }
    }

    private String resolveImplementationClassName(GenerateClass domain, String interfaceName) {
        String className = domain.className();
        if(className.isEmpty()) {
            return "Default" + capitalize(interfaceName);
        } else {
            return className;
        }
    }
}
