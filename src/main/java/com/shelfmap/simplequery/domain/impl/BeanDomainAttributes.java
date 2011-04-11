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
package com.shelfmap.simplequery.domain.impl;

import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.domain.AttributeStore;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.FlatAttribute;
import com.shelfmap.simplequery.FloatAttribute;
import com.shelfmap.simplequery.IntAttribute;
import com.shelfmap.simplequery.LongAttribute;
import com.shelfmap.simplequery.SimpleDBAttribute;
import com.shelfmap.simplequery.domain.AttributeAccessor;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.domain.AttributeKey;
import com.shelfmap.simplequery.domain.CanNotWriteAttributeException;
import com.shelfmap.simplequery.domain.DomainAttributes;
import com.shelfmap.simplequery.domain.DomainAttribute;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 *
 * @author Tsutomu YANO
 */
public class BeanDomainAttributes implements DomainAttributes {
    private final AttributeStore attributeStore = new DefaultAttributeStore();
    private final Class<?> domainClass;
    private final String domainName;
    private final String parentPropertyPath;
    private final Configuration configuration;

    public BeanDomainAttributes(Class<?> domainClass, String domainName, Configuration configuration) {
        this(domainClass, domainName, configuration, null);
    }
    
    public BeanDomainAttributes(Class<?> domainClass, String domainName, Configuration configuration, String parentPropertyPath) {
        isNotNull("domainClass", domainClass);
        isNotNull("domainName", domainName);
        isNotNull("configuration", configuration);
        
        this.domainClass = domainClass;
        this.domainName = domainName;
        this.configuration = configuration;
        this.parentPropertyPath = parentPropertyPath == null ? "" : parentPropertyPath;
        try {
            BeanInfo info = Introspector.getBeanInfo(domainClass);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                Class<?> type = descriptor.getPropertyType();
                String propertyName = descriptor.getName();
                Method getter = descriptor.getReadMethod();
                handleAttributeWithType(type, propertyName, getter);
            }
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Can not introspect a class object.", ex);
        }
    }
    
    private <T> void handleAttributeWithType(Class<T> type, String propertyName, Method getter) {
        if(getter.isAnnotationPresent(FlatAttribute.class)) {
            buildFlatAttribute(type, propertyName);
        } else {
            DomainAttribute<T> attribute = createAttribute(propertyName, type, getter);
            attributeStore.putAttribute(attribute.getAttributeName(), type, attribute);
        }        
    }

    @SuppressWarnings("unchecked")
    private <C> DomainAttribute<C> createAttribute(String propertyName, Class<C> type, Method getter) {
        DomainAttribute<C> result = null;
        if (getter.isAnnotationPresent(FloatAttribute.class) && (type.equals(Float.class) || type.equals(float.class))) {
            FloatAttribute annotation = getter.getAnnotation(FloatAttribute.class);
            result = (DomainAttribute<C>) processFloatAttribute(annotation, propertyName, getter);

        } else if (getter.isAnnotationPresent(IntAttribute.class) && (type.equals(Integer.class) || type.equals(int.class))) {
            IntAttribute annotation = getter.getAnnotation(IntAttribute.class);
            result = (DomainAttribute<C>) processIntAttribute(annotation, propertyName, getter);

        } else if (getter.isAnnotationPresent(LongAttribute.class) && (type.equals(Long.class) || type.equals(long.class))) {
            LongAttribute annotation = getter.getAnnotation(LongAttribute.class);
            result = (DomainAttribute<C>) processLongAttribute(annotation, propertyName, getter);

        } else if (getter.isAnnotationPresent(SimpleDBAttribute.class)) {
            SimpleDBAttribute annotation = getter.getAnnotation(SimpleDBAttribute.class);
            result = processSimpleDBAttribute(annotation, propertyName, type, getter);
        } else {
            //No Annotation. the attribute name of this property become same with the property name.
            result = new DefaultDomainAttribute<C>(getDomainName(), propertyName, type, newAttributeConverter(type), newAttributeAccessor(type, fullPropertyPath(propertyName)));
        }

        return result;
    }
    
    protected <C> AttributeAccessor<C> newAttributeAccessor(Class<C> type, String propertyPath) {
        return new PropertyAttributeAccessor<C>(propertyPath, configuration);
    }
    
    protected <C> AttributeConverter<C> newAttributeConverter(Class<C> type) {
        return new DefaultAttributeConverter<C>(type);
    }

    private DomainAttribute<Float> processFloatAttribute(FloatAttribute annotation, String propertyName, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();
        return new FloatDomainAttribute(getDomainName(), attributeName, annotation.maxDigitLeft(), annotation.maxDigitRight(), annotation.offset(), newAttributeAccessor(Float.class, fullPropertyPath(propertyName)));
    }

    private DomainAttribute<Integer> processIntAttribute(IntAttribute annotation, String propertyName, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();
        return new IntDomainAttribute(getDomainName(), attributeName, annotation.padding(), annotation.offset(), newAttributeAccessor(Integer.class, fullPropertyPath(propertyName)));
    }

    private DomainAttribute<Long> processLongAttribute(LongAttribute annotation, String propertyName, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();
        return new LongDomainAttribute(getDomainName(), attributeName, annotation.padding(), annotation.offset(), newAttributeAccessor(Long.class, fullPropertyPath(propertyName)));
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
            return new DefaultDomainAttribute<C>(getDomainName(), attributeName, type, (AttributeConverter<C>) converter, newAttributeAccessor(type, fullPropertyPath(propertyName)));
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException("Can not instanciate a converter. possible cause is that the converter class specified in @SimpleDBAttribute do not have a default constructor.", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Can not instanciate a converter, because we could not be able to access the default constructor of the converter class specified in a @SimpleDBAttribute annotation.", ex);
        }
    }
    
    private String fullPropertyPath(String propertyName) {
        return this.parentPropertyPath.isEmpty() ? propertyName : this.parentPropertyPath + "." + propertyName;
    }

    /**
     * If an @FlatAttribute annotation is applied on a method, we must handle the class of 
     * the return type of the method on which the annotation is applied.
     * 
     * @param type the return type of the method on which FlatAttribute annotation is applied.
     */
    private void buildFlatAttribute(Class<?> type, String propertyName) {
        BeanDomainAttributes attributes = new BeanDomainAttributes(type, getDomainName(), this.configuration, fullPropertyPath(propertyName));
        copy(attributes, this);
    }
    
    private void copy(BeanDomainAttributes source, BeanDomainAttributes dest) {
        for (AttributeKey key : source.attributeStore.keySet()) {
            if(dest.attributeStore.isAttributeDefined(key.getAttributeName())) {
                throw new IllegalArgumentException("Can not retrieve attributes from classes: the name of the attribute '" + key + "' of " + source.getDomainClass().getCanonicalName() + " is duplicated with the parent domainClass '" + dest.getDomainClass().getCanonicalName() + "'.");
            }
            copyAttribute(dest, source, key.getType(), key.getAttributeName());
        }
    }

    private <T> void copyAttribute(BeanDomainAttributes dest, BeanDomainAttributes source, Class<T> type, String attributeName) {
        dest.attributeStore.putAttribute(attributeName, type, source.getAttribute(attributeName, type));
    }

    @Override
    public boolean isAttributeDefined(String attributeName) {
        return attributeStore.isAttributeDefined(attributeName);
    }

    @Override
    public <T> DomainAttribute<T> getAttribute(String attributeName, Class<T> type) {
        return attributeStore.getAttribute(attributeName, type);
    }
    
    @Override
    public DomainAttribute<?> getAttribute(String attributeName) {
        return attributeStore.getAttribute(attributeName);
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
        return attributeStore.values().iterator();
    }

    @Override
    public <T> void writeAttribute(Object instance, String attributeName, Class<T> type, T value) throws CanNotWriteAttributeException {
        DomainAttribute<T> attribute = attributeStore.getAttribute(attributeName, type);
        attribute.getAttributeAccessor().write(instance, value);
    }
}
