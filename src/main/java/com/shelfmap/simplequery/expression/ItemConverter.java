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

package com.shelfmap.simplequery.expression;

import com.amazonaws.services.simpledb.model.Item;
import com.shelfmap.simplequery.InstanceFactory;
import com.shelfmap.simplequery.expression.impl.BeanDomainAttribute;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 *
 * @author Tsutomu YANO
 */
public class ItemConverter<T> {
    private final Class<T> domainClass;
    private final DomainAttribute domainAttributes;
    private final InstanceFactory<T> instanceFactory;

    public ItemConverter(Class<T> domainClass, InstanceFactory<T> factory) {
        this.domainClass = domainClass;
        this.domainAttributes = new BeanDomainAttribute(domainClass);
        this.instanceFactory = factory;
    }
    
    public T convert(Item item) {
        T instance = instanceFactory.createInstance(domainClass);
        for (com.amazonaws.services.simpledb.model.Attribute attr : item.getAttributes()) {
            String attributeName = attr.getName();
            String attributeValue = attr.getValue();
            Attribute domainAttribute = domainAttributes.getAttribute(attributeName);
            
            if(domainAttribute != null) {
                Object convertedValue = domainAttribute.getAttributeInfo().restoreValue(attributeValue);

                domainAttributes.writeAttribute(instance, attributeName, convertedValue);
            }
        }
        return instance;
    }
}
