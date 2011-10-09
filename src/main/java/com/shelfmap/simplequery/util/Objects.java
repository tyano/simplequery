/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.shelfmap.simplequery.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Tsutomu YANO
 */
public final class Objects {

    private Objects() {
    }

    public static Collection<Class<?>> linearize(Class<?> parentClass) {
        List<Class<?>> resultList = new ArrayList<Class<?>>();
        appendAllInterfaces(parentClass, resultList);

        Class<?> superClass = parentClass.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            appendAllInterfaces(superClass, resultList);
            superClass = superClass.getSuperclass();
        }
        return resultList;
    }

    private static void appendAllInterfaces(Class<?> clazz, List<Class<?>> list) {
        list.add(clazz);
        list.addAll(asList(clazz.getInterfaces()));
    }

    public static <A extends Object & Annotation> A findAnnotation(Class<?> targetClass, Class<A> findingAnnotation) {
        for (Class<?> clazz : linearize(targetClass)) {
            if (clazz.isAnnotationPresent(findingAnnotation)) {
                return clazz.getAnnotation(findingAnnotation);
            }
        }
        return null;
    }

    public static <A extends Object & Annotation> A findAnnotationOnProperty(Class<?> targetClass, String propertyName, Class<A> findingAnnotation) throws IntrospectionException {
        CLASS_LOOP:
        for (Class<?> clazz : linearize(targetClass)) {
            PropertyDescriptor[] descriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                if (descriptor.getName().equals(propertyName)) {
                    Method readMethod = descriptor.getReadMethod();
                    A annotation = readMethod.getAnnotation(findingAnnotation);
                    if (annotation != null) {
                        return annotation;
                    } else {
                        continue CLASS_LOOP;
                    }
                }
            }
        }
        return null;
    }
    
    public static <A extends Object & Annotation> boolean isAnnotationPresentOnProperty(Class<?> targetClass, String propertyName, Class<A> findingAnnotation) throws IntrospectionException {
        return findAnnotationOnProperty(targetClass, propertyName, findingAnnotation) != null;
    }
}
