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

import com.shelfmap.simplequery.expression.AttributeAccessor;
import com.shelfmap.simplequery.expression.AttributeConverter;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.DomainAttribute;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainAttribute<T> implements DomainAttribute<T> {
    private String domainName;
    private String attributeName;
    private Class<T> type;
    private AttributeConverter<T> attributeConverte;

    public DefaultDomainAttribute(String domainName, String attributeName, Class<T> type, AttributeConverter<T> attributeConverter) {
        isNotNull("domainName", domainName);
        isNotNull("attributeName", attributeName);
        isNotNull("type", type);
        isNotNull("attributeInfo", attributeConverter);
        this.attributeName = attributeName;
        this.type = type;
        this.attributeConverte = attributeConverter;
    }
    
    public DefaultDomainAttribute(String domainName, String attributeName, Class<T> type) {
        this(domainName, attributeName, type, new DefaultAttributeConverter<T>(type));
    }
    
    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public AttributeConverter<T> getAttributeConverter() {
        return attributeConverte;
    }

    @Override
    public AttributeAccessor<T> getAttributeAccessor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDomainName() {
        return domainName;
    }
}
