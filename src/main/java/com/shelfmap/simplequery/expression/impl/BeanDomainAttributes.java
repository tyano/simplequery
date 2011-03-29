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
import com.shelfmap.simplequery.FloatAttribute;
import com.shelfmap.simplequery.IntAttribute;
import com.shelfmap.simplequery.LongAttribute;
import com.shelfmap.simplequery.SimpleDBAttribute;
import com.shelfmap.simplequery.expression.AttributeConverter;
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
                String propertyName = descriptor.getName();
                Method getter = descriptor.getReadMethod();
                Method setter = descriptor.getWriteMethod();
                DomainAttribute<?> attribute = createAttribute(propertyName, type, getter);
                attributeMap.put(attribute.getName(), attribute);
                writeMethodMap.put(attribute.getName(), setter);
            }   
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Can not introspect a class object.", ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <C> DomainAttribute<?> createAttribute(String propertyName, Class<C> type, Method getter) {
        DomainAttribute<?> result = null;
        if(getter.isAnnotationPresent(FloatAttribute.class)) {
            FloatAttribute annotation = getter.getAnnotation(FloatAttribute.class);
            String attributeName = annotation.attributeName().isEmpty() ? propertyName : annotation.attributeName();
            result = new FloatDomainAttribute(attributeName, annotation.maxDigitLeft(), annotation.maxDigitRight(), annotation.offset());
        } else if(getter.isAnnotationPresent(IntAttribute.class)) {
            IntAttribute annotation = getter.getAnnotation(IntAttribute.class);
            String attributeName = annotation.attributeName().isEmpty() ? propertyName : annotation.attributeName();
            result = new IntDomainAttribute(attributeName, annotation.padding(), annotation.offset());
        } else if(getter.isAnnotationPresent(LongAttribute.class)) {
            LongAttribute annotation = getter.getAnnotation(LongAttribute.class);
            String attributeName = annotation.attributeName().isEmpty() ? propertyName : annotation.attributeName();
            result = new LongDomainAttribute(attributeName, annotation.padding(), annotation.offset());
        } else if(getter.isAnnotationPresent(SimpleDBAttribute.class)) {
            try {
                SimpleDBAttribute annotation = getter.getAnnotation(SimpleDBAttribute.class);
                String attributeName = annotation.attributeName().isEmpty() ? propertyName : annotation.attributeName();
                Class<? extends AttributeConverter<?>> converterClass = annotation.attributeConverter();
                AttributeConverter<?> converter = 
                        (converterClass.equals(NullAttributeConverter.class)) 
                            ? new DefaultAttributeConverter<C>(type)
                            : converterClass.newInstance();
                result = new DefaultDomainAttribute<C>(attributeName, type, (AttributeConverter<C>)converter);
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException("Can not instanciate a converter. possible cause is that the converter class specified in @SimpleDBAttribute do not have a default constructor.", ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Can not instanciate a converter, because we could not be able to access the default constructor of the converter class specified in a @SimpleDBAttribute annotation.", ex);
            }
        } else {
            result = new DefaultDomainAttribute<C>(propertyName, type);
        }
        
        return result;
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
