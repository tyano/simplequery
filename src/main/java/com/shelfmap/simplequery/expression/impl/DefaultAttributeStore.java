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

import com.shelfmap.simplequery.expression.AttributeKey;
import com.shelfmap.simplequery.expression.AttributeStore;
import com.shelfmap.simplequery.expression.DomainAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tsutomu YANO
 */
@SuppressWarnings("unchecked")
public class DefaultAttributeStore implements AttributeStore {
    private final Map<AttributeKey, DomainAttribute> attributeMap = new HashMap<AttributeKey, DomainAttribute>();

    @Override
    public <T> DomainAttribute<T> putAttribute(String attributeName, Class<T> type, DomainAttribute<T> value) {
        return attributeMap.put(new DefaultAttributeKey(attributeName, type), value);
    }

    @Override
    public <T> DomainAttribute<T> getAttribute(String attributeName, Class<T> type) {
        return attributeMap.get(new DefaultAttributeKey(attributeName, type));
    }
}
