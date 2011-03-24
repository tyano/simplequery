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

package com.shelfmap.simplequery.expression.impl;

import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.Domain;
import com.shelfmap.simplequery.SimpleDBAttribute;
import com.shelfmap.simplequery.expression.CanNotWriteAttributeException;
import com.shelfmap.simplequery.expression.DomainAttributes;
import com.shelfmap.simplequery.expression.DomainAttribute;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Tsutomu YANO
 */
public class BeanDomainAttributes implements DomainAttributes {
    private final Map<String,DomainAttribute<?>> attributeMap = new LinkedHashMap<String, DomainAttribute<?>>();
    private final Class<?> domainClass;
    private final Map<String,Method> writeMethodMap = new HashMap<String,Method>();
    
    public BeanDomainAttributes(Class<?> domainClass) {
        isNotNull("domainClass", domainClass);
        if( !domainClass.isAnnotationPresent(Domain.class)) {
            throw new IllegalArgumentException("domainClass must have a @Domain annotation.");
        }
        
        this.domainClass = domainClass;
        try {
            BeanInfo info = Introspector.getBeanInfo(domainClass);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for(PropertyDescriptor descriptor : descriptors) {
                Class<?> type = descriptor.getPropertyType();
                String name = descriptor.getName();
                Method getter = descriptor.getReadMethod();
                Method setter = descriptor.getWriteMethod();
                int maxDigitLeft = 0;
                int maxDigitRight = 0;
                long offset = 0L;
                SimpleDBAttribute annotation = getter.getAnnotation(SimpleDBAttribute.class);
                if(annotation != null) {
                    String attributeName = annotation.attributeName();
                    if( !attributeName.isEmpty()) name = attributeName;
                    maxDigitLeft = annotation.maxDigitLeft();
                    maxDigitRight = annotation.maxDigitRight();
                    offset = annotation.offset();
                }
                DomainAttribute<?> attribute = createAttribute(name, type, maxDigitLeft, maxDigitRight, offset);
                attributeMap.put(name, attribute);
                writeMethodMap.put(name, setter);
            }   
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Can not introspect a class object.", ex);
        }
    }
    
    private <C> DomainAttribute<?> createAttribute(String attributeName, Class<C> type, int maxDigitLeft, int maxDigitRight, long offset) {
        if(type == float.class || type == Float.class) {
            return new FloatDomainAttribute(attributeName, maxDigitLeft, maxDigitRight, (int)offset);
        } else if(type == int.class || type == Integer.class) {
            return new IntDomainAttribute(attributeName, maxDigitLeft, (int)offset);
        } else if(type == long.class || type == Long.class) {
            return new LongDomainAttribute(attributeName, maxDigitLeft, offset);
        } else {
            return new DefaultDomainAttribute<C>(attributeName, type);
        }
    }
    
    @Override
    public boolean isAttributeDefined(String attributeName) {
        return attributeMap.get(attributeName) != null;
    }

    @Override
    public DomainAttribute<?> getAttribute(String attributeName) {
        return attributeMap.get(attributeName);
    }

    @Override
    public Class<?> getDomainClass() {
        return domainClass;
    }

    @Override
    public Iterator<DomainAttribute<?>> iterator() {
        return attributeMap.values().iterator();
    }

    @Override
    public void writeAttribute(Object instance, String attributeName, Object value) throws CanNotWriteAttributeException {
        DomainAttribute<?> attribute = attributeMap.get(attributeName);
        Method writeMethod = writeMethodMap.get(attributeName);
        if(writeMethod == null) throw new IllegalStateException("the attribute '" + attributeName + "' is not writable.");
        
        writeMethod.setAccessible(true);
        try {
            writeMethod.invoke(instance, value);
        } catch (IllegalAccessException ex) {
            throw new CanNotWriteAttributeException(ex, attribute);
        } catch (IllegalArgumentException ex) {
            throw new CanNotWriteAttributeException(ex, attribute);
        } catch (InvocationTargetException ex) {
            throw new CanNotWriteAttributeException(ex, attribute);
        }
    }
}
