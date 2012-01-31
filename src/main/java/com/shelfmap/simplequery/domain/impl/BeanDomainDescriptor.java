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

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.annotation.*;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.factory.DomainAttributeFactory;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.util.Objects;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Tsutomu YANO
 */
public class BeanDomainDescriptor implements DomainDescriptor {
    private final AttributeStore attributeStore = new DefaultAttributeStore();
    private final Domain<?> domain;
    private final String parentPropertyPath;
    private final Context context;
    private String itemNameProperty;

    public BeanDomainDescriptor(Context context, Domain<?> domain) {
        this(context, domain, null);
    }

    public BeanDomainDescriptor(Context context, Domain<?> domain, String parentPropertyPath) {
        isNotNull("domain", domain);
        isNotNull("context", context);

        this.domain = domain;
        this.context = context;
        this.parentPropertyPath = parentPropertyPath == null ? "" : parentPropertyPath;
        try {
            Class<?> domainClass = domain.getDomainClass();
            BeanInfo info = Introspector.getBeanInfo(domainClass);
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                //do not handle the properties of Object class.
                //(Object class have only one property 'getClass()')
                if(!descriptor.getName().equals("class")) {
                    Class<?> originalPropertyType = descriptor.getPropertyType();
                    Class<?> containerType = originalPropertyType;
                    Class<?> valueType = originalPropertyType;
                    String propertyName = descriptor.getName();

                    //skip an attribute when ignore == true
                    Attribute attributeAnnotation = Objects.findAnnotationOnProperty(domainClass, descriptor.getName(), Attribute.class);
                    if(attributeAnnotation != null && attributeAnnotation.ignore()) continue;

                    if(ReverseReference.class.isAssignableFrom(originalPropertyType)) {
                        //ReverseReference do not have their own value in a domain.
                        //because it programmatically retrieve the value from another domain.
                        continue;
                    } else if(Objects.isAnnotationPresentOnProperty(domainClass, propertyName, ItemName.class)) {
                        if(DomainReference.class.isAssignableFrom(originalPropertyType) ||
                           Collection.class.isAssignableFrom(originalPropertyType) ||
                           originalPropertyType.isArray()) {
                            throw new IllegalStateException("the 'itemName' property should not be an array, collection or DomainReference. it should be an primitive or String which is persistable as an simple attribute on SimpleDB.");
                        }
                        handleItemName(domainClass, originalPropertyType, valueType, containerType, propertyName);
                    } else {

                        if(ForwardReference.class.isAssignableFrom(originalPropertyType)) {
                            containerType = String.class;
                            valueType = String.class;
                        } else if(Collection.class.isAssignableFrom(originalPropertyType)) {
                            Container container = Objects.findAnnotationOnProperty(domainClass, propertyName, Container.class);
                            if(container == null) throw new IllegalStateException("Collection property must have a @Container annotation.");
                            containerType = container.containerType();
                            valueType = container.valueType();
                        } else if(originalPropertyType.isArray()) {
                            valueType = originalPropertyType.getComponentType();
                        }
                        handleAttributeWithType(domainClass, originalPropertyType, valueType, containerType, propertyName);
                    }
                }
            }
        } catch (IntrospectionException ex) {
            throw new IllegalStateException("Can not introspect a class object.", ex);
        }
    }

    private <VT,CT> void handleItemName(Class<?> domainClass, Class<?> originalPropertyType, Class<VT> valueType, Class<CT> containerType, String propertyName) throws IntrospectionException {
        DomainAttribute<VT,CT> itemNameAttribute = createAttribute(domainClass, propertyName, originalPropertyType, valueType, containerType);
        attributeStore.putAttribute(itemNameAttribute.getAttributeName(), valueType, containerType, itemNameAttribute);
        this.itemNameProperty = propertyName;
    }

    private <VT,CT> void handleAttributeWithType(Class<?> domainClass, Class<?> originalPropertyType, Class<VT> valueType, Class<CT> containerType, String propertyName) throws IntrospectionException {
        if(Objects.isAnnotationPresentOnProperty(domainClass, propertyName, FlatAttribute.class)) {
            buildFlatAttribute(originalPropertyType, propertyName);
        } else {
            DomainAttribute<VT,CT> attribute = createAttribute(domainClass, propertyName, originalPropertyType, valueType, containerType);
            attributeStore.putAttribute(attribute.getAttributeName(), valueType, containerType, attribute);
        }
    }

    @SuppressWarnings("unchecked")
    private <VT,CT> DomainAttribute<VT,CT> createAttribute(Class<?> domainClass, String propertyName, Class<?> originalPropertyType, Class<VT> valueType, Class<CT> containerType) throws IntrospectionException {
        DomainAttribute<VT,CT> result = null;

        DomainAttributeFactory factory = getContext().getDomainAttributeFactory();
        AttributeAccessor<CT> accessor = factory.createAttributeAccessor(originalPropertyType, containerType, fullPropertyPath(propertyName));

        if (Objects.isAnnotationPresentOnProperty(domainClass, propertyName, FloatAttribute.class) && (valueType.equals(Float.class) || valueType.equals(float.class))) {
            FloatAttribute annotation = Objects.findAnnotationOnProperty(domainClass, propertyName, FloatAttribute.class);
            AttributeConverter<VT> converter = (AttributeConverter<VT>) factory.createFloatAttributeConverter(annotation.maxDigitLeft(), annotation.maxDigitRight(), annotation.offset());

            String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();

            result = factory.createAttribute(getDomain(), attributeName, valueType, containerType, converter, accessor);

        } else if (Objects.isAnnotationPresentOnProperty(domainClass, propertyName, IntAttribute.class) && (valueType.equals(Integer.class) || valueType.equals(int.class))) {
            IntAttribute annotation = Objects.findAnnotationOnProperty(domainClass, propertyName, IntAttribute.class);
            AttributeConverter<VT> converter = (AttributeConverter<VT>) factory.createIntAttributeConverter(annotation.padding(), annotation.offset());

            String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();

            result = factory.createAttribute(getDomain(), attributeName, valueType, containerType, converter, accessor);

        } else if (Objects.isAnnotationPresentOnProperty(domainClass, propertyName, LongAttribute.class) && (valueType.equals(Long.class) || valueType.equals(long.class))) {
            LongAttribute annotation = Objects.findAnnotationOnProperty(domainClass, propertyName, LongAttribute.class);
            AttributeConverter<VT> converter = (AttributeConverter<VT>) factory.createLongAttributeConverter(annotation.padding(), annotation.offset());

            String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();

            result = factory.createAttribute(getDomain(), attributeName, valueType, containerType, converter, accessor);

        } else if (Objects.isAnnotationPresentOnProperty(domainClass, propertyName, Attribute.class)) {
            Attribute annotation = Objects.findAnnotationOnProperty(domainClass, propertyName, Attribute.class);
            result = processAttribute(annotation, propertyName, valueType, containerType, factory, accessor);

        } else {
            //No Annotation. the attribute name of this property become same with the property name.
            //AttributeConverter is created by the type of this attribute.
            AttributeConverter<VT> converter = factory.createAttributeConverter(valueType);
            result = new DefaultDomainAttribute<VT,CT>(getDomain(), propertyName, valueType, containerType, converter, accessor);
        }

        return result;
    }

    protected <C> AttributeAccessor<C> newAttributeAccessor(Class<C> type, String propertyPath) {
        return new PropertyAttributeAccessor<C>(context, propertyPath);
    }

    @SuppressWarnings("unchecked")
    private <VT,CT> DomainAttribute<VT,CT> processAttribute(Attribute annotation, String propertyName, Class<VT> valueType, Class<CT> containerType, DomainAttributeFactory factory, AttributeAccessor<CT> accessor) {
        try {
            String attributeName = annotation.attributeName().isEmpty()
                    ? propertyName
                    : annotation.attributeName();
            Class<? extends AttributeConverter<?>> converterClass = annotation.attributeConverter();

            AttributeConverter<?> converter =
                    (converterClass.equals(NullAttributeConverter.class))
                    ? factory.createAttributeConverter(valueType)
                    : converterClass.newInstance();

            return factory.createAttribute(getDomain(), attributeName, valueType, containerType, (AttributeConverter<VT>) converter, accessor);
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
     * the return type of the method on which the annotation is applied as another domain.
     *
     * @param type the return type of the method on which FlatAttribute annotation is applied.
     */
    private void buildFlatAttribute(Class<?> propertyType, String propertyName) {
        DomainFactory domainFactory = getContext().getDomainFactory();
        Domain<?> childDomain = domainFactory.createDomain(propertyType);
        BeanDomainDescriptor attributes = new BeanDomainDescriptor(getContext(), childDomain, fullPropertyPath(propertyName));
        copy(this, attributes);
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private void copy(BeanDomainDescriptor dest, BeanDomainDescriptor source) {
        for (AttributeKey key : source.attributeStore.keySet()) {
            if(dest.attributeStore.isAttributeDefined(key.getAttributeName())) {
                throw new IllegalArgumentException("The name of the attribute '" + key.getAttributeName() + "' of " + source.getDomain().getDomainClass().getName() + " is duplicated with the parent domainClass '" + dest.getDomain().getDomainClass().getName() + "'.");
            }
            copyAttribute(dest, source, key.getValueType(), key.getContainerType(), key.getAttributeName());
        }
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    private <VT,CT> void copyAttribute(BeanDomainDescriptor dest, BeanDomainDescriptor source, Class<VT> valueType, Class<CT> containerType, String attributeName) {
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
    public Domain<?> getDomain() {
        return domain;
    }

    @Override
    public Iterator<DomainAttribute<?,?>> iterator() {
        return attributeStore.values().iterator();
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
    public Context getContext() {
        return context;
    }

    @Override
    public DomainAttribute<?, ?> getItemNameAttribute() {
        return attributeStore.getAttribute(this.itemNameProperty);
    }

    public String getItemNameFrom(Object object) {
        return asItemName(getItemNameAttribute(), object);
    }

    @SuppressWarnings("unchecked")
    private <VT,CT> String asItemName(DomainAttribute<VT,CT> attribute, Object object) {
        CT attributeValue = attribute.getAttributeAccessor().read(object);
        AttributeConverter<VT> converter = attribute.getAttributeConverter();

        //ValueType(VT) and ContainerType(CT) must be a same type;
        return converter.convertValue((VT)attributeValue);
    }
}
