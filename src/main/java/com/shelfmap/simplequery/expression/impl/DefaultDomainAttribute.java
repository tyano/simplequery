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

import com.shelfmap.simplequery.expression.AttributeConverter;
import static com.shelfmap.simplequery.util.Assertion.isNotNull;
import com.shelfmap.simplequery.expression.DomainAttribute;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainAttribute<T> implements DomainAttribute<T> {
    private String attributeName;
    private Class<T> type;
    private AttributeConverter<T> attributeInfo;

    public DefaultDomainAttribute(String attributeName, Class<T> type, AttributeConverter<T> attributeInfo) {
        isNotNull("attributeName", attributeName);
        isNotNull("type", type);
        isNotNull("attributeInfo", attributeInfo);
        this.attributeName = attributeName;
        this.type = type;
        this.attributeInfo = attributeInfo;
    }
    
    public DefaultDomainAttribute(String attributeName, Class<T> type) {
        this(attributeName, type, new DefaultAttributeInfo<T>(type));
    }
    
    @Override
    public String getName() {
        return attributeName;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public AttributeConverter<T> getAttributeInfo() {
        return attributeInfo;
    }
}
