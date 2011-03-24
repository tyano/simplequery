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
import com.shelfmap.simplequery.InstanceFactory;
import com.shelfmap.simplequery.expression.CanNotConvertItemException;
import com.shelfmap.simplequery.expression.DomainAttribute;
import com.shelfmap.simplequery.expression.DomainAttributes;
import com.shelfmap.simplequery.expression.ItemConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultItemConverter<T> implements ItemConverter<T> {
    private final Class<T> domainClass;
    private final InstanceFactory<T> instanceFactory;
    private DomainAttributes domainAttributes;

    public DefaultItemConverter(Class<T> domainClass, InstanceFactory<T> factory) {
        isNotNull("domainClass", domainClass);
        isNotNull("factory", factory);
        this.domainClass = domainClass;
        this.instanceFactory = factory;
    }
    
    @Override
    public T convert(Item item) throws CanNotConvertItemException {
        if(domainAttributes == null) {
            domainAttributes = newDomainAttributes();
        }
        
        T instance = getInstanceFactory().createInstance(getDomainClass());
        for (Attribute attr : item.getAttributes()) {
            String attributeName = attr.getName();
            String attributeValue = attr.getValue();
            DomainAttribute<?> domainAttribute = domainAttributes.getAttribute(attributeName);
            
            if(domainAttribute != null) {
                Object convertedValue = domainAttribute.getAttributeInfo().restoreValue(attributeValue);
                domainAttributes.writeAttribute(instance, attributeName, convertedValue);
            }
        }
        return instance;
    }

    @Override
    public Class<T> getDomainClass() {
        return domainClass;
    }

    @Override
    public InstanceFactory<T> getInstanceFactory() {
        return this.instanceFactory;
    }    
    
    //Factory method
    protected DomainAttributes newDomainAttributes() {
        return new BeanDomainAttributes(getDomainClass());
    }
}
