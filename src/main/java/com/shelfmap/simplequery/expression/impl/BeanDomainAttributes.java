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
import com.shelfmap.simplequery.FlatAttribute;
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

    private final Map<String, DomainAttribute<?>> attributeMap = new LinkedHashMap<String, DomainAttribute<?>>();
    private final Class<?> domainClass;
    private final String domainName;
    private final Map<String, Method> writeMethodMap = new HashMap<String, Method>();

    public BeanDomainAttributes(Class<?> domainClass, String domainName) {
        isNotNull("domainClass", domainClass);
        isNotNull("domainName", domainName);
        
        this.domainClass = domainClass;
        this.domainName = domainName;
        try {
            BeanInfo info = Introspector.getBeanInfo(domainClass);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                Class<?> type = descriptor.getPropertyType();
                String propertyName = descriptor.getName();
                Method getter = descriptor.getReadMethod();
                Method setter = descriptor.getWriteMethod();
                
                if(getter.isAnnotationPresent(FlatAttribute.class)) {
                    buildFlatAttribute(type);
                } else {
                    DomainAttribute<?> attribute = createAttribute(propertyName, type, getter);
                    attributeMap.put(attribute.getAttributeName(), attribute);
                    writeMethodMap.put(attribute.getAttributeName(), setter);
                }
            }
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Can not introspect a class object.", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <C> DomainAttribute<?> createAttribute(String propertyName, Class<C> type, Method getter) {
        DomainAttribute<?> result = null;
        if (getter.isAnnotationPresent(FloatAttribute.class)) {
            FloatAttribute annotation = getter.getAnnotation(FloatAttribute.class);
            result = processFloatAttribute(annotation, propertyName, getter);

        } else if (getter.isAnnotationPresent(IntAttribute.class)) {
            IntAttribute annotation = getter.getAnnotation(IntAttribute.class);
            result = processIntAttribute(annotation, propertyName, getter);

        } else if (getter.isAnnotationPresent(LongAttribute.class)) {
            LongAttribute annotation = getter.getAnnotation(LongAttribute.class);
            result = processLongAttribute(annotation, propertyName, getter);

        } else if (getter.isAnnotationPresent(SimpleDBAttribute.class)) {
            SimpleDBAttribute annotation = getter.getAnnotation(SimpleDBAttribute.class);
            result = processSimpleDBAttribute(annotation, propertyName, type, getter);
        } else {
            result = new DefaultDomainAttribute<C>(getDomainName(), propertyName, type);
        }

        return result;
    }

    private DomainAttribute<Float> processFloatAttribute(FloatAttribute annotation, String propertyName, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();
        return new FloatDomainAttribute(getDomainName(), attributeName, annotation.maxDigitLeft(), annotation.maxDigitRight(), annotation.offset());
    }

    private DomainAttribute<Integer> processIntAttribute(IntAttribute annotation, String propertyName, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();
        return new IntDomainAttribute(getDomainName(), attributeName, annotation.padding(), annotation.offset());
    }

    private DomainAttribute<Long> processLongAttribute(LongAttribute annotation, String propertyName, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();
        return new LongDomainAttribute(getDomainName(), attributeName, annotation.padding(), annotation.offset());
    }

    @SuppressWarnings("unchecked")
    private <C> DomainAttribute<C> processSimpleDBAttribute(SimpleDBAttribute annotation, String propertyName, Class<C> type, Method getter) {
        try {
            String attributeName = annotation.attributeName().isEmpty()
                    ? propertyName
                    : annotation.attributeName();
            Class<? extends AttributeConverter<?>> converterClass = annotation.attributeConverter();

            AttributeConverter<?> converter =
                    (converterClass.equals(DefaultAttributeConverter.class))
                    ? new DefaultAttributeConverter<C>(type)
                    : converterClass.newInstance();
            return new DefaultDomainAttribute<C>(getDomainName(), attributeName, type, (AttributeConverter<C>) converter);
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException("Can not instanciate a converter. possible cause is that the converter class specified in @SimpleDBAttribute do not have a default constructor.", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Can not instanciate a converter, because we could not be able to access the default constructor of the converter class specified in a @SimpleDBAttribute annotation.", ex);
        }
    }

    /**
     * If an @FlatAttribute annotation is applied on a method, we must handle the class of 
     * the return type of the method on which the annotation is applied.
     * 
     * @param type the return type of the method on which FlatAttribute annotation is applied.
     */
    private void buildFlatAttribute(Class<?> type) {
        BeanDomainAttributes attributes = new BeanDomainAttributes(type, getDomainName());
        copy(attributes, this);
    }
    
    private void copy(BeanDomainAttributes source, BeanDomainAttributes dest) {
        for (String key : source.attributeMap.keySet()) {
            if(dest.attributeMap.containsKey(key)) {
                throw new IllegalArgumentException("Can not retrieve attributes from classes: the name of the attribute '" + key + "' of " + source.getDomainClass().getCanonicalName() + " is duplicated with the parent domainClass '" + dest.getDomainClass().getCanonicalName() + "'.");
            }
        }
        dest.attributeMap.putAll(source.attributeMap);
        dest.writeMethodMap.putAll(source.writeMethodMap);
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
    public String getDomainName() {
        return domainName;
    }
    
    @Override
    public Iterator<DomainAttribute<?>> iterator() {
        return attributeMap.values().iterator();
    }

    @Override
    public void writeAttribute(Object instance, String attributeName, Object value) throws CanNotWriteAttributeException {
        //TODO must process @FlatAttribute in this method.
        
        DomainAttribute<?> attribute = attributeMap.get(attributeName);
        Method writeMethod = writeMethodMap.get(attributeName);
        if (writeMethod == null) {
            throw new IllegalStateException("the attribute '" + attributeName + "' is not writable.");
        }

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
