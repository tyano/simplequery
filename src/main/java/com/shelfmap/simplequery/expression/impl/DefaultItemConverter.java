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

import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.amazonaws.services.simpledb.model.Item;
import com.shelfmap.simplequery.Domain;
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

    public DefaultItemConverter(Class<T> domainClass) {
        isNotNull("domainClass", domainClass);
        this.domainClass = domainClass;
    }
    
    @Override
    public T convert(Item item) throws CanNotConvertItemException {
        Domain domain = domainClass.getAnnotation(Domain.class);
        if(domain == null) throw new IllegalArgumentException("domainClass do not have @Domain annotation. You can convert an Item object only to a instanceo of class which have a @Domain annotation.");

        DomainAttributes attributes = newDomainAttributes();
        for(com.amazonaws.services.simpledb.model.Attribute attr : item.getAttributes()) {
            String attributeName = attr.getName();
            String attributeValue = attr.getValue();
            
            DomainAttribute<?> domainAttribute = attributes.getAttribute(attributeName);
        
        }
        return null;
    }

    @Override
    public Class<T> getDomainClass() {
        return domainClass;
    }
    
    //Factory method
    protected DomainAttributes newDomainAttributes() {
        return new BeanDomainAttributes(getDomainClass());
    }
}
