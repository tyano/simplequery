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
import com.shelfmap.simplequery.expression.CanNotRestoreAttributeException;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.amazonaws.services.simpledb.model.Item;
import com.shelfmap.simplequery.Configuration;
import com.shelfmap.simplequery.InstanceFactory;
import com.shelfmap.simplequery.expression.CanNotConvertItemException;
import com.shelfmap.simplequery.expression.DomainAttribute;
import com.shelfmap.simplequery.expression.DomainAttributes;
import com.shelfmap.simplequery.expression.ItemConverter;

/**
 * <b>this class is NOT THREAD SAFE</b>
 * @author Tsutomu YANO
 */
public class DefaultItemConverter<T> implements ItemConverter<T> {
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
        this.instanceFactory = configuration.getInstanceFactory(domainClass);
    }
    
    @Override
    public T convert(Item item) throws CanNotConvertItemException {
        if(domainAttributes == null) {
            domainAttributes = getConfiguration().getDomainAttributes(domainClass, domainName);
        }
        
        T instance = instanceFactory.createInstance(getDomainClass());
        for (Attribute attr : item.getAttributes()) {
            String attributeName = attr.getName();
            String attributeValue = attr.getValue();
            @SuppressWarnings("unchecked")
            DomainAttribute<T> domainAttribute = (DomainAttribute<T>) domainAttributes.getAttribute(attributeName);
            if(domainAttribute != null) {
                try {
                    T convertedValue = domainAttribute.getAttributeConverter().restoreValue(attributeValue);
                    domainAttribute.getAttributeAccessor().write(instance, convertedValue);
                } catch (CanNotRestoreAttributeException ex) {
                    throw new CanNotConvertItemException("could not write a attribute: " + domainAttribute.getAttributeName() + " for the item: " + item.getName(), ex, item);
                }
            }
        }
        return instance;
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
