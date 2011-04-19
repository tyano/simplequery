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

import com.shelfmap.simplequery.domain.DomainAttribute;
import com.shelfmap.simplequery.domain.AttributeAccessor;
import com.shelfmap.simplequery.domain.AttributeConverter;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainAttribute<VT,CT> implements DomainAttribute<VT,CT> {
    private final String domainName;
    private final String attributeName;
    private final Class<VT> valueType;
    private final Class<CT> containerType;
    private final AttributeConverter<VT> attributeConverter;
    private final AttributeAccessor<CT> attributeAccessor;

    public DefaultDomainAttribute(String domainName, String attributeName, Class<VT> valueType, Class<CT> containerType, AttributeConverter<VT> attributeConverter, AttributeAccessor<CT> attributeAccessor) {
        isNotNull("domainName", domainName);
        isNotNull("attributeName", attributeName);
        isNotNull("valueType", valueType);
        isNotNull("containerType", containerType);
        isNotNull("attributeConverter", attributeConverter);
        isNotNull("attributeAccessor", attributeAccessor);
        this.domainName = domainName;
        this.attributeName = attributeName;
        this.valueType = valueType;
        this.containerType = containerType;
        this.attributeConverter = attributeConverter;
        this.attributeAccessor = attributeAccessor;
    }
    
    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public Class<VT> getValueType() {
        return valueType;
    }

    @Override
    public AttributeConverter<VT> getAttributeConverter() {
        return attributeConverter;
    }

    @Override
    public AttributeAccessor<CT> getAttributeAccessor() {
        return attributeAccessor;
    }
    
    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public Class<CT> getContainerType() {
        return containerType;
    }
}
