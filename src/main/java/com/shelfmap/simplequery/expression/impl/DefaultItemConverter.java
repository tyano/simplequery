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

import com.amazonaws.services.simpledb.model.*;
import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.DomainInstanceFactory;
import com.shelfmap.simplequery.ItemState;
import com.shelfmap.simplequery.SimpleItemState;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.expression.CanNotConvertItemException;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;
import com.shelfmap.simplequery.expression.ItemConverter;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;

import com.shelfmap.simplequery.util.Objects;
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

        T instance = instanceFactory.create();
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

    @SuppressWarnings("unchecked")
    private <VT,CT> void writeValueToDomain(DomainAttribute<VT,CT> domainAttribute, T instance, String attributeValue) throws CanNotRestoreAttributeException, CanNotConvertItemException {
        if(domainAttribute != null) {
            Class<VT> valueType = domainAttribute.getValueType();
            Class<CT> containerType = domainAttribute.getContainerType();

            AttributeAccessor<CT> accessor = domainAttribute.getAttributeAccessor();
            LOGGER.trace("valueType: " + valueType);
            LOGGER.trace("containerType: " + containerType);

            Class<?> objValueType = Objects.primitiveToObject(valueType);
            Class<?> objContainerType = Objects.primitiveToObject(containerType);

            if(objValueType.equals(objContainerType)) {
                VT convertedValue = domainAttribute.getAttributeConverter().restoreValue(attributeValue);
                accessor.write(instance, (CT)convertedValue);
            } else if(containerType.isArray()) {
                CT prev = accessor.read(instance);
                if(attributeValue == null) {
                    if(prev == null) {
                        Object newArray = Array.newInstance(valueType, 0);
                        LOGGER.trace("type of newArray: {}", newArray.getClass());
                        accessor.write(instance, (CT)newArray);
                    }
                } else {
                    int prevLength = prev == null ? 0 : Array.getLength(prev);
                    Object newArray = Array.newInstance(valueType, prevLength+1);
                    if(prev != null) {
                        for(int i = 0; i < prevLength; i++) {
                            Object o = Array.get(prev, i);
                            Array.set(newArray, i, o);
                        }
                    }
                    VT convertedValue = domainAttribute.getAttributeConverter().restoreValue(attributeValue);
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
                        VT convertedValue = domainAttribute.getAttributeConverter().restoreValue(attributeValue);
                        newCol.add(convertedValue);
                    }
                    accessor.write(instance, (CT)newCol);
                } catch (InstantiationException ex) {
                    throw new IllegalStateException("Could not instantiate a collection: " + containerType.getCanonicalName(), ex);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Could not access to the default constructor of the class: " + containerType.getCanonicalName(), ex);
                }
            } else {
                throw new IllegalStateException("The property's type with multiple values must be a subclass of Collection or an Array.");
            }
        }
    }

    @Override
    public ItemState makeCurrentStateOf(Object domainObject) {
        if(descriptor == null) {
            descriptor = getContext().getDomainDescriptorFactory().create(getDomain());
        }

        String itemName = descriptor.getItemNameAttribute().getAttributeAccessor().read(domainObject);
        ItemState lastState = new SimpleItemState(itemName);

        for (DomainAttribute<?,?> domainAttribute : descriptor) {
            ItemState state = updateState(itemName, domainAttribute, domainObject);
            Collection<ReplaceableAttribute> changed = state.getChangedItems();
            Collection<Attribute> deleted = state.getDeletedItems();

            if(!changed.isEmpty()) {
                lastState.addChanged(changed.toArray(new ReplaceableAttribute[0]));
            }

            if(!deleted.isEmpty()) {
                lastState.addDeleted(deleted.toArray(new Attribute[0]));
            }
        }

        return lastState;
    }

    private <VT,CT> ItemState updateState(String itemName, DomainAttribute<VT,CT> domainAttribute, Object domainObject) {
        Class<CT> containerType = domainAttribute.getContainerType();
        Class<VT> valueType = domainAttribute.getValueType();

        AttributeAccessor<CT> accessor = domainAttribute.getAttributeAccessor();
        AttributeConverter<VT> converter = domainAttribute.getAttributeConverter();
        String attributeName = domainAttribute.getAttributeName();

        List<ReplaceableAttribute> sdbChangedAttributes = new ArrayList<ReplaceableAttribute>();
        List<Attribute> sdbDeletedAttributes = new ArrayList<Attribute>();

        Class<?> objValueType = Objects.primitiveToObject(valueType);
        Class<?> objContainerType = Objects.primitiveToObject(containerType);

        if(objValueType.equals(objContainerType)) {
            @SuppressWarnings("unchecked")
            VT attributeValue = (VT)accessor.read(domainObject);
            if(attributeValue != null) {
                String convertedAttributeValue = converter.convertValue(attributeValue);
                registerAttributeAsPut(convertedAttributeValue, attributeName, true, sdbChangedAttributes);
            } else {
                registerAttributeAsDeleted(attributeName, sdbDeletedAttributes);
            }
        } else if(containerType.isArray()) {
            CT array = accessor.read(domainObject);
            if(array == null || Array.getLength(array) == 0) {
                registerAttributeAsDeleted(attributeName, sdbDeletedAttributes);
            } else {
                int length = Array.getLength(array);
                boolean isFirst = true;
                for(int i = 0; i < length; i++) {
                    @SuppressWarnings("unchecked")
                    VT attributeValue = (VT) Array.get(array, i);

                    if(attributeValue != null) {
                        String convertedAttributeValue = converter.convertValue(attributeValue);
                        registerAttributeAsPut(convertedAttributeValue, attributeName, isFirst, sdbChangedAttributes);
                    }
                    if(isFirst) isFirst = false;
                }
            }
        } else if(Collection.class.isAssignableFrom(containerType)) {
            @SuppressWarnings("unchecked")
            Collection<? extends VT> collection = (Collection<? extends VT>) accessor.read(domainObject);
            if(collection == null || collection.isEmpty()) {
                registerAttributeAsDeleted(attributeName, sdbDeletedAttributes);
            } else {
                boolean isFirst = true;
                for (VT attributeValue : collection) {
                    if(attributeValue != null) {
                        String convertedAttributeValue = converter.convertValue(attributeValue);
                        registerAttributeAsPut(convertedAttributeValue, attributeName, isFirst, sdbChangedAttributes);
                    }
                    if(isFirst) isFirst = false;
                }
            }
        }

        ItemState state = new SimpleItemState(itemName);
        if(!sdbChangedAttributes.isEmpty()) {
            state.addChanged(sdbChangedAttributes.toArray(new ReplaceableAttribute[0]));
        }

        if(!sdbDeletedAttributes.isEmpty()) {
            state.addDeleted(sdbDeletedAttributes.toArray(new Attribute[0]));
        }

        return state;
    }

    private <VT> void registerAttributeAsPut(String attributeValue, String attributeName, boolean replace, List<ReplaceableAttribute> sdbChangedAttributes) {
        ReplaceableAttribute sdbAttribute = new ReplaceableAttribute(attributeName, attributeValue, replace);
        sdbChangedAttributes.add(sdbAttribute);
    }

    private void registerAttributeAsDeleted(String attributeName, List<Attribute> sdbDeletedAttributes) {
        Attribute deleted = new Attribute().withName(attributeName);
        sdbDeletedAttributes.add(deleted);
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
