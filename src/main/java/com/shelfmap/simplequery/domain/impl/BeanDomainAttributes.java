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
import com.shelfmap.simplequery.annotation.FlatAttribute;
import com.shelfmap.simplequery.annotation.FloatAttribute;
import com.shelfmap.simplequery.annotation.IntAttribute;
import com.shelfmap.simplequery.annotation.LongAttribute;
import com.shelfmap.simplequery.annotation.Attribute;
import com.shelfmap.simplequery.annotation.Container;
import com.shelfmap.simplequery.annotation.ItemName;
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
import java.util.Collection;
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
    private String itemNameProperty;

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
                //do not handle the properties of Object class.
                //(Object class have only one property 'getClass()')
                if(!descriptor.getName().equals("class")) {
                    Class<?> propertyType = primitiveToObject(descriptor.getPropertyType());
                    String propertyName = descriptor.getName();
                    Method getter = descriptor.getReadMethod();

                    if(getter.isAnnotationPresent(ItemName.class)) {
                        handleItemName(propertyType, propertyName, getter);
                    } else {
                        Class<?> valueType = propertyType;
                        if(Collection.class.isAssignableFrom(propertyType) || propertyType.isArray()) {
                            Container container = getter.getAnnotation(Container.class);
                            if(container == null) throw new IllegalStateException("Collection property must have a @Container annotation.");
                            propertyType = container.containerType();
                            valueType = container.valueType();
                        }
                        handleAttributeWithType(valueType, propertyType, propertyName, getter);
                    }
                }
            }
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Can not introspect a class object.", ex);
        }
    }

    private Class<?> primitiveToObject(Class<?> type) {
        if(int.class.isAssignableFrom(type))
            return Integer.class;
        else if(long.class.isAssignableFrom(type))
            return Long.class;
        else if(float.class.isAssignableFrom(type))
            return Float.class;
        else
            return type;
    }

    private void handleItemName(Class<?> type, String propertyName, Method getter) {
        if(!String.class.isAssignableFrom(type)) {
            throw new IllegalStateException("Can not handle a domain class: " + domainClass.getCanonicalName() + " - The type of @ItemName property must be String.class.");
        }
        DomainAttribute<String,String> itemNameAttribute = createAttribute(propertyName, String.class, String.class, getter);
        attributeStore.putAttribute(propertyName, String.class, String.class, itemNameAttribute);
        this.itemNameProperty = propertyName;
    }

    private <VT,CT> void handleAttributeWithType(Class<VT> valueType, Class<CT> containerType, String propertyName, Method getter) {
        if(getter.isAnnotationPresent(FlatAttribute.class)) {
            buildFlatAttribute(valueType, propertyName);
        } else {
            DomainAttribute<VT,CT> attribute = createAttribute(propertyName, valueType, containerType, getter);
            attributeStore.putAttribute(attribute.getAttributeName(), valueType, containerType, attribute);
        }
    }

    @SuppressWarnings("unchecked")
    private <VT,CT> DomainAttribute<VT,CT> createAttribute(String propertyName, Class<VT> valueType, Class<CT> containerType, Method getter) {
        DomainAttribute<VT,CT> result = null;

        if (getter.isAnnotationPresent(FloatAttribute.class) && (valueType.equals(Float.class) || valueType.equals(float.class))) {
            FloatAttribute annotation = getter.getAnnotation(FloatAttribute.class);
            result = (DomainAttribute<VT,CT>) processFloatAttribute(annotation, propertyName, containerType, getter);

        } else if (getter.isAnnotationPresent(IntAttribute.class) && (valueType.equals(Integer.class) || valueType.equals(int.class))) {
            IntAttribute annotation = getter.getAnnotation(IntAttribute.class);
            result = (DomainAttribute<VT,CT>) processIntAttribute(annotation, propertyName, containerType, getter);

        } else if (getter.isAnnotationPresent(LongAttribute.class) && (valueType.equals(Long.class) || valueType.equals(long.class))) {
            LongAttribute annotation = getter.getAnnotation(LongAttribute.class);
            result = (DomainAttribute<VT,CT>) processLongAttribute(annotation, propertyName, containerType, getter);

        } else if (getter.isAnnotationPresent(Attribute.class)) {
            Attribute annotation = getter.getAnnotation(Attribute.class);
            result = processAttribute(annotation, propertyName, valueType, containerType, getter);
        } else {
            //No Annotation. the attribute name of this property become same with the property name.
            result = new DefaultDomainAttribute<VT,CT>(getDomainName(), propertyName, valueType, containerType, newAttributeConverter(valueType), newAttributeAccessor(containerType, fullPropertyPath(propertyName)));
        }

        return result;
    }

    protected <C> AttributeAccessor<C> newAttributeAccessor(Class<C> type, String propertyPath) {
        return new PropertyAttributeAccessor<C>(propertyPath, configuration);
    }

    protected <C> AttributeConverter<C> newAttributeConverter(Class<C> type) {
        return new DefaultAttributeConverter<C>(type);
    }

    private <CT> DomainAttribute<Float,CT> processFloatAttribute(FloatAttribute annotation, String propertyName, Class<CT> containerType, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();

        AttributeConverter<Float> converter = createFloatConverter(annotation);
        AttributeAccessor<CT> accessor = newAttributeAccessor(containerType, fullPropertyPath(propertyName));
        return new DefaultDomainAttribute<Float, CT>(getDomainName(), attributeName, Float.class, containerType, converter, accessor);
    }

    private AttributeConverter<Float> createFloatConverter(FloatAttribute annotation) {
        return new FloatAttributeConverter(annotation.maxDigitLeft(), annotation.maxDigitRight(), annotation.offset());
    }

    private <CT> DomainAttribute<Integer,CT> processIntAttribute(IntAttribute annotation, String propertyName, Class<CT> containerType, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();

        AttributeConverter<Integer> converter = createIntConverter(annotation);
        AttributeAccessor<CT> accessor = newAttributeAccessor(containerType, fullPropertyPath(propertyName));
        return new DefaultDomainAttribute<Integer, CT>(getDomainName(), attributeName, Integer.class, containerType, converter, accessor);
    }

    private AttributeConverter<Integer> createIntConverter(IntAttribute annotation) {
        return new IntAttributeConverter(annotation.padding(), annotation.offset());
    }

    private <CT> DomainAttribute<Long,CT> processLongAttribute(LongAttribute annotation, String propertyName, Class<CT> containerType, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();

        AttributeConverter<Long> converter = createLongConverter(annotation);
        AttributeAccessor<CT> accessor = newAttributeAccessor(containerType, fullPropertyPath(propertyName));
        return new DefaultDomainAttribute<Long, CT>(getDomainName(), attributeName, Long.class, containerType, converter, accessor);
    }

    private AttributeConverter<Long> createLongConverter(LongAttribute annotation) {
        return new LongAttributeConverter(annotation.padding(), annotation.offset());
    }

    @SuppressWarnings("unchecked")
    private <VT,CT> DomainAttribute<VT,CT> processAttribute(Attribute annotation, String propertyName, Class<VT> valueType, Class<CT> containerType, Method getter) {
        try {
            String attributeName = annotation.attributeName().isEmpty()
                    ? propertyName
                    : annotation.attributeName();
            Class<? extends AttributeConverter<?>> converterClass = annotation.attributeConverter();

            AttributeConverter<?> converter =
                    (converterClass.equals(NullAttributeConverter.class))
                    ? new DefaultAttributeConverter<VT>(valueType)
                    : converterClass.newInstance();
            return new DefaultDomainAttribute<VT,CT>(getDomainName(), attributeName, valueType, containerType, (AttributeConverter<VT>) converter, newAttributeAccessor(containerType, fullPropertyPath(propertyName)));
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException("Can not instanciate a converter. possible cause is that the converter class specified in @Attribute do not have a default constructor.", ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Can not instanciate a converter, because we could not be able to access the default constructor of the converter class specified in a @Attribute annotation.", ex);
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
        copy(this, attributes);
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private void copy(BeanDomainAttributes dest, BeanDomainAttributes source) {
        for (AttributeKey key : source.attributeStore.keySet()) {
            if(dest.attributeStore.isAttributeDefined(key.getAttributeName())) {
                throw new IllegalArgumentException("The name of the attribute '" + key.getAttributeName() + "' of " + source.getDomainClass().getCanonicalName() + " is duplicated with the parent domainClass '" + dest.getDomainClass().getCanonicalName() + "'.");
            }
            copyAttribute(dest, source, key.getValueType(), key.getContainerType(), key.getAttributeName());
        }
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private <VT,CT> void copyAttribute(BeanDomainAttributes dest, BeanDomainAttributes source, Class<VT> valueType, Class<CT> containerType, String attributeName) {
        dest.attributeStore.putAttribute(attributeName, valueType, containerType, source.getAttribute(attributeName, valueType, containerType));
    }

    @Override
    public boolean isAttributeDefined(String attributeName) {
        return attributeStore.isAttributeDefined(attributeName);
    }

    @Override
    public <VT,CT> DomainAttribute<VT,CT> getAttribute(String attributeName, Class<VT> valueType, Class<CT> containerType) {
        return attributeStore.getAttribute(attributeName, valueType, containerType);
    }

    @Override
    public DomainAttribute<?,?> getAttribute(String attributeName) {
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
    public Iterator<DomainAttribute<?,?>> iterator() {
        return attributeStore.values().iterator();
    }

    @Override
    public <VT,CT> void writeAttribute(Object instance, String attributeName, Class<VT> valueType, Class<CT> containerType, CT value) throws CanNotWriteAttributeException {
        DomainAttribute<VT,CT> attribute = attributeStore.getAttribute(attributeName, valueType, containerType);
        attribute.getAttributeAccessor().write(instance, value);
    }

    @Override
    public Class<?> getValueType(String attributeName) {
        return attributeStore.getValueType(attributeName);
    }

    @Override
    public Class<?> getContainerType(String attributeName) {
        return attributeStore.getContainerType(attributeName);
    }

    @Override
    public DomainAttribute<String, String> getItemNameAttribute() {
        return attributeStore.getAttribute(this.itemNameProperty, String.class, String.class);
    }
}
