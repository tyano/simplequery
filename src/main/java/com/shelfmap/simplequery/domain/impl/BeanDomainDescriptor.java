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
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
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
            BeanInfo info = Introspector.getBeanInfo(domain.getDomainClass());
            PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
            for (PropertyDescriptor descriptor : descriptors) {
                //do not handle the properties of Object class.
                //(Object class have only one property 'getClass()')
                if(!descriptor.getName().equals("class")) {
                    Class<?> propertyType = primitiveToObject(descriptor.getPropertyType());
                    String propertyName = descriptor.getName();
                    Method getter = descriptor.getReadMethod();

                    if(ReverseReference.class.isAssignableFrom(propertyType)) {
                        //ReverseReference do not have their own value in a domain.
                        //because it programmatically retrieve the value from another domain.
                        continue;
                    } else if(getter.isAnnotationPresent(ItemName.class)) {
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
            throw new IllegalStateException("Can not handle a domain class: " + getDomain().getDomainClass().getName() + " - The type of @ItemName property must be String.class.");
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
        } else if (getter.isAnnotationPresent(ForwardDomainReference.class)) { 
            ForwardDomainReference annotation = getter.getAnnotation(ForwardDomainReference.class);
            result = processForwardDomainReference(annotation, propertyName, valueType, containerType, getter);
        } else if (getter.isAnnotationPresent(Attribute.class)) {
            Attribute annotation = getter.getAnnotation(Attribute.class);
            result = processAttribute(annotation, propertyName, valueType, containerType, getter);
        } else {
            //No Annotation. the attribute name of this property become same with the property name.
            //AttributeConverter is created by the type of this attribute.
            AttributeConverter<VT> converter = createConverter(valueType);
            result = new DefaultDomainAttribute<VT,CT>(getDomain(), propertyName, valueType, containerType, converter, newAttributeAccessor(containerType, fullPropertyPath(propertyName)));
        }

        return result;
    }

    protected <C> AttributeAccessor<C> newAttributeAccessor(Class<C> type, String propertyPath) {
        return new PropertyAttributeAccessor<C>(context, propertyPath);
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
        return new DefaultDomainAttribute<Float, CT>(getDomain(), attributeName, Float.class, containerType, converter, accessor);
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
        return new DefaultDomainAttribute<Integer, CT>(getDomain(), attributeName, Integer.class, containerType, converter, accessor);
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
        return new DefaultDomainAttribute<Long, CT>(getDomain(), attributeName, Long.class, containerType, converter, accessor);
    }

    private AttributeConverter<Long> createLongConverter(LongAttribute annotation) {
        return new LongAttributeConverter(annotation.padding(), annotation.offset());
    }

    private <VT> AttributeConverter<VT> createConverter(Class<VT> attributeType) {
        AttributeConverterFactory factory = context.getAttributeConverterFactory();
        return factory.getAttributeConverter(attributeType);
    }
    
    @SuppressWarnings("unchecked")
    private <VT,CT> DomainAttribute<VT,CT> processForwardDomainReference(ForwardDomainReference annotation, String propertyName, Class<VT> valueType, Class<CT> containerType, Method getter) {
        String attributeName = annotation.attributeName().isEmpty()
                ? propertyName
                : annotation.attributeName();
        Class<?> targetDomainClass = annotation.targetDomainClass();
        AttributeConverter<?> converter = new DefaultToOneDomainReferenceAttributeConverter(context, targetDomainClass);
        return new DefaultDomainAttribute<VT,CT>(getDomain(), attributeName, valueType, containerType, (AttributeConverter<VT>) converter, newAttributeAccessor(containerType, fullPropertyPath(propertyName)));
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
                    ? createConverter(valueType)
                    : converterClass.newInstance();
            return new DefaultDomainAttribute<VT,CT>(getDomain(), attributeName, valueType, containerType, (AttributeConverter<VT>) converter, newAttributeAccessor(containerType, fullPropertyPath(propertyName)));
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
    public DomainAttribute<String, String> getItemNameAttribute() {
        return attributeStore.getAttribute(this.itemNameProperty, String.class, String.class);
    }
}
