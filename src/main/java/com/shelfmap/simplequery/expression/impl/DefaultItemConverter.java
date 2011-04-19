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
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.amazonaws.services.simpledb.model.Item;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.InstanceFactory;
import com.shelfmap.simplequery.domain.AttributeAccessor;
import com.shelfmap.simplequery.domain.DomainAttribute;
import com.shelfmap.simplequery.domain.DomainAttributes;
import com.shelfmap.simplequery.expression.CanNotConvertItemException;
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;
import com.shelfmap.simplequery.expression.ItemConverter;
import java.lang.reflect.Array;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <b>this class is NOT THREAD SAFE</b>
 * @author Tsutomu YANO
 */
public class DefaultItemConverter<T> implements ItemConverter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultItemConverter.class);
    
    private final Class<T> domainClass;
    private final String domainName;
    private final Configuration configuration;
    private final InstanceFactory<T> instanceFactory;
    private DomainAttributes domainAttributes;

    public DefaultItemConverter(Class<T> domainClass, String domainName, Configuration configuration) {
        isNotNull("domainClass", domainClass);
        isNotNull("domainName", domainName);
        isNotNull("configuration", configuration);
        this.domainClass = domainClass;
        this.domainName = domainName;
        this.configuration = configuration;
        this.instanceFactory = configuration.getInstanceFactory(domainClass, domainName);
    }
    
    @Override
    public T convert(Item item) throws CanNotConvertItemException {
        if(domainAttributes == null) {
            domainAttributes = getConfiguration().getDomainAttributes(domainClass, domainName);
        }
        
        T instance = instanceFactory.createInstance(getDomainClass());
        for (Attribute attr : item.getAttributes()) {
            DomainAttribute<?,?> domainAttribute = null;
            try {
                String attributeName = attr.getName();
                String attributeValue = attr.getValue();
                domainAttribute = domainAttributes.getAttribute(attributeName);
                writeValueToDomain(domainAttribute, instance, attributeValue);
            } catch (CanNotRestoreAttributeException ex) {
                throw new CanNotConvertItemException("could not write a attribute: " + domainAttribute.getAttributeName() + " for the item: " + item.getName(), ex, item);
            }
        }
        return instance;
    }
    
    private <VT,CT> void writeValueToDomain(DomainAttribute<VT,CT> domainAttribute, T instance, String attributeValue) throws CanNotRestoreAttributeException, CanNotConvertItemException {
        if(domainAttribute != null) {
            Class<VT> valueType = domainAttribute.getValueType();
            Class<CT> containerType = domainAttribute.getContainerType();
            
            VT convertedValue = domainAttribute.getAttributeConverter().restoreValue(attributeValue);
            AttributeAccessor<CT> accessor = domainAttribute.getAttributeAccessor();
            if(valueType.equals(containerType)) {
                accessor.write(instance, containerType.cast(convertedValue));
            } else if(containerType.isArray()) {
                CT prev = accessor.read(instance);
                int prevLength = Array.getLength(prev);
                Object newArray = Array.newInstance(containerType, prevLength+1);
                for(int i = 0; i < prevLength; i++) {
                    Object o = Array.get(prev, i);
                    Array.set(newArray, i, o);
                }
                Array.set(newArray, prevLength+1, convertedValue);
                accessor.write(instance, containerType.cast(newArray));
            } else if(Collection.class.isAssignableFrom(containerType)) {
                try {
                    Collection<VT> prev = (Collection<VT>) accessor.read(instance);
                    Collection<VT> newCol = (Collection<VT>) containerType.newInstance();
                    newCol.addAll(prev);
                    newCol.add(convertedValue);
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
    public Class<T> getDomainClass() {
        return domainClass;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public String getDomainName() {
        return domainName;
    }
}
