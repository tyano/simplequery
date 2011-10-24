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

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.DomainInstanceFactory;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.expression.CanNotConvertItemException;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;
import com.shelfmap.simplequery.expression.ItemConverter;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>this class is NOT THREAD SAFE</b>
 * @param <T>
 * @author Tsutomu YANO
 */
public class DefaultItemConverter<T> implements ItemConverter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultItemConverter.class);

    private final Domain<T> domain;
    private final Context context;
    private final DomainInstanceFactory<T> instanceFactory;
    private DomainDescriptor descriptor;

    public DefaultItemConverter(Context context, Domain<T> domain) {
        isNotNull("domain", domain);
        isNotNull("context", context);
        this.domain = domain;
        this.context = context;
        this.instanceFactory = context.getDomainInstanceFactory(domain);
    }

    @Override
    public T convertToInstance(Item item) throws CanNotConvertItemException {
        if(descriptor == null) {
            descriptor = getContext().getDomainDescriptorFactory().create(getDomain());
        }

        T instance = instanceFactory.create(getDomain());
        for (Attribute attr : item.getAttributes()) {
            DomainAttribute<?,?> domainAttribute = null;
            try {
                String attributeName = attr.getName();
                String attributeValue = attr.getValue();
                domainAttribute = descriptor.getAttribute(attributeName);
                writeValueToDomain(domainAttribute, instance, attributeValue);
            } catch (CanNotRestoreAttributeException ex) {
                throw new CanNotConvertItemException("could not write a attribute: " + domainAttribute.getAttributeName() + " for the item: " + item.getName(), ex, item);
            }
        }

        //Fill all collection properties with empty collection
        for (DomainAttribute<?,?> domainAttribute : descriptor) {
            if(domainAttribute.getContainerType().isArray() || Collection.class.isAssignableFrom(domainAttribute.getContainerType())) {
                Object value = domainAttribute.getAttributeAccessor().read(instance);
                if(value == null) {
                    try {
                        writeValueToDomain(domainAttribute, instance, null);
                    } catch (CanNotRestoreAttributeException ex) {
                        throw new CanNotConvertItemException("could not write a attribute: " + domainAttribute.getAttributeName() + " for the item: " + item.getName(), ex, item);
                    }
                }
            }
        }

        DomainAttribute<String,String> itemNameAttribute = descriptor.getItemNameAttribute();
        String itemNameValue = item.getName();
        if(itemNameAttribute != null) {
           itemNameAttribute.getAttributeAccessor().write(instance, itemNameValue);
        }

        return instance;
    }

    private <VT,CT> void writeValueToDomain(DomainAttribute<VT,CT> domainAttribute, T instance, String attributeValue) throws CanNotRestoreAttributeException, CanNotConvertItemException {
        if(domainAttribute != null) {
            Class<VT> valueType = domainAttribute.getValueType();
            Class<CT> containerType = domainAttribute.getContainerType();

            VT convertedValue = domainAttribute.getAttributeConverter().restoreValue(attributeValue);
            AttributeAccessor<CT> accessor = domainAttribute.getAttributeAccessor();
            LOGGER.trace("valueType: " + valueType.getCanonicalName());
            LOGGER.trace("containerType: " + containerType.getCanonicalName());
            if(valueType.equals(containerType)) {
                accessor.write(instance, containerType.cast(convertedValue));
            } else if(containerType.isArray()) {
                CT prev = accessor.read(instance);
                if(attributeValue == null) {
                    if(prev == null) {
                        Object newArray = Array.newInstance(valueType, 0);
                        accessor.write(instance, containerType.cast(newArray));
                    }
                } else {
                    int prevLength = prev == null ? 0 : Array.getLength(prev);
                    Object newArray = Array.newInstance(valueType, prevLength+1);
                    for(int i = 0; i < prevLength; i++) {
                        Object o = Array.get(prev, i);
                        Array.set(newArray, i, o);
                    }
                    Array.set(newArray, prevLength, convertedValue);
                    accessor.write(instance, containerType.cast(newArray));
                }
            } else if(Collection.class.isAssignableFrom(containerType)) {
                try {
                    @SuppressWarnings("unchecked")
                    Collection<VT> prev = (Collection<VT>) accessor.read(instance);

                    @SuppressWarnings("unchecked")
                    Collection<VT> newCol = (Collection<VT>) containerType.newInstance();
                    if(prev != null) {
                        newCol.addAll(prev);
                    }
                    if(attributeValue != null) {
                        newCol.add(convertedValue);
                    }
                    accessor.write(instance, containerType.cast(newCol));
                } catch (InstantiationException ex) {
                    throw new IllegalStateException("Could not instantiate a collection: " + containerType.getCanonicalName(), ex);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Could not access to the default constructor of the class: " + containerType.getCanonicalName(), ex);
                }
            } else {
                throw new IllegalStateException("The property's type with multiple value must be a subclass of Collection or an Array.");
            }
        }
    }

    @Override
    public ReplaceableItem convertToItem(Object domainObject) {
        if(descriptor == null) {
            descriptor = getContext().getDomainDescriptorFactory().create(getDomain());
        }

        String itemName = descriptor.getItemNameAttribute().getAttributeAccessor().read(domainObject);
        ReplaceableItem item = new ReplaceableItem(itemName);

        List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>();
        for (DomainAttribute<?,?> domainAttribute : descriptor) {
            attributes.addAll(convertToReplaceableAttribute(domainAttribute, domainObject));
        }

        item.setAttributes(attributes);
        return item;
    }

    private <VT,CT> Collection<ReplaceableAttribute> convertToReplaceableAttribute(DomainAttribute<VT,CT> domainAttribute, Object domainObject) {
        Class<CT> containerType = domainAttribute.getContainerType();
        Class<VT> valueType = domainAttribute.getValueType();

        AttributeAccessor<CT> accessor = domainAttribute.getAttributeAccessor();
        AttributeConverter<VT> converter = domainAttribute.getAttributeConverter();
        String attributeName = domainAttribute.getAttributeName();

        List<ReplaceableAttribute> sdbAttributes = new ArrayList<ReplaceableAttribute>();
        if(valueType.equals(containerType)) {
            VT attributeValue = valueType.cast(accessor.read(domainObject));
            String convertedAttributeValue = converter.convertValue(attributeValue);
            ReplaceableAttribute sdbAttribute = new ReplaceableAttribute(attributeName, convertedAttributeValue, true);
            sdbAttributes.add(sdbAttribute);
        } else if(containerType.isArray()) {
            CT array = accessor.read(domainObject);
            int length = Array.getLength(array);
            boolean isFirst = true;
            for(int i = 0; i < length; i++) {
                @SuppressWarnings("unchecked")
                VT attributeValue = (VT) Array.get(domainObject, i);
                String convertedAttributeValue = converter.convertValue(attributeValue);
                ReplaceableAttribute sdbAttribute = new ReplaceableAttribute(attributeName, convertedAttributeValue, isFirst);
                sdbAttributes.add(sdbAttribute);
                if(isFirst) isFirst = false;
            }
        } else if(Collection.class.isAssignableFrom(containerType)) {
            @SuppressWarnings("unchecked")
            Collection<? extends VT> collection = (Collection<? extends VT>) accessor.read(domainObject);
            boolean isFirst = true;
            for (VT attributeValue : collection) {
                String convertedAttributeValue = converter.convertValue(attributeValue);
                ReplaceableAttribute sdbAttribute = new ReplaceableAttribute(attributeName, convertedAttributeValue, isFirst);
                sdbAttributes.add(sdbAttribute);
                if(isFirst) isFirst = false;
            }
        }
        return sdbAttributes;
    }

    @Override
    public Domain<T> getDomain() {
        return domain;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
