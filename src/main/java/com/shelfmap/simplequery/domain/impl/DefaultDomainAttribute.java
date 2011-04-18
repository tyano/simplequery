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

import com.shelfmap.simplequery.domain.AttributeAccessor;
import com.shelfmap.simplequery.domain.AttributeConverter;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.domain.DomainAttribute;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainAttribute<T> implements DomainAttribute<T> {
    private final String domainName;
    private final String attributeName;
    private final Class<T> type;
    private final AttributeConverter<T> attributeConverte;
    private final AttributeAccessor<T> attributeAccessor;

    public DefaultDomainAttribute(String domainName, String attributeName, Class<T> type, AttributeConverter<T> attributeConverter, AttributeAccessor<T> attributeAccessor) {
        isNotNull("domainName", domainName);
        isNotNull("attributeName", attributeName);
        isNotNull("type", type);
        isNotNull("attributeInfo", attributeConverter);
        this.domainName = domainName;
        this.attributeName = attributeName;
        this.type = type;
        this.attributeConverte = attributeConverter;
        this.attributeAccessor = attributeAccessor;
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
        return attributeAccessor;
    }
    
    @Override
    public String getDomainName() {
        return domainName;
    }
}
