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

import com.shelfmap.simplequery.ClassReference;
import com.shelfmap.simplequery.SimpleClassReference;
import com.shelfmap.simplequery.domain.AttributeKey;
import com.shelfmap.simplequery.domain.AttributeStore;
import com.shelfmap.simplequery.domain.DomainAttribute;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Tsutomu YANO
 */
@SuppressWarnings({"unchecked"})
public class DefaultAttributeStore implements AttributeStore, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Map<AttributeKey, DomainAttribute<?,?>> attributeMap = new HashMap<AttributeKey, DomainAttribute<?,?>>();
    private final Map<String, ClassReference> valueTypeMap = new HashMap<String, ClassReference>();
    private final Map<String, ClassReference> containerTypeMap = new HashMap<String, ClassReference>();
    
    @Override
    public <VT,CT> DomainAttribute<VT, CT> putAttribute(String attributeName, Class<VT> valueType, Class<CT> containerType, DomainAttribute<VT,CT> value) {
        DomainAttribute<VT, CT> result = (DomainAttribute<VT, CT>) attributeMap.put(new DefaultAttributeKey(attributeName, valueType, containerType), value);
        valueTypeMap.put(attributeName, new SimpleClassReference(valueType));
        containerTypeMap.put(attributeName, new SimpleClassReference(containerType));
        return result;
    }

    @Override
    public <VT,CT> DomainAttribute<VT,CT> getAttribute(String attributeName, Class<VT> valueType, Class<CT> containerType) {
        return (DomainAttribute<VT,CT>) attributeMap.get(new DefaultAttributeKey(attributeName, valueType, containerType));
    }

    @Override
    public Set<AttributeKey> keySet() {
        return new HashSet<AttributeKey>(attributeMap.keySet());
    }

    @Override
    public Set<DomainAttribute<?,?>> values() {
        return new HashSet<DomainAttribute<?,?>>(attributeMap.values());
    }

    @Override
    public DomainAttribute<?,?> getAttribute(String attributeName) {
        Class<?> valueType = getValueType(attributeName);
        Class<?> containerType = getContainerType(attributeName);
        return getAttribute(attributeName, valueType, containerType);
    }

    @Override
    public boolean isAttributeDefined(String attributeName) {
        return getAttribute(attributeName) != null;
    }

    @Override
    public Class<?> getValueType(String attributeName) {
        ClassReference classRef = valueTypeMap.get(attributeName);
        return classRef == null ? null : classRef.get();
    }

    @Override
    public Class<?> getContainerType(String attributeName) {
        ClassReference classRef = containerTypeMap.get(attributeName);
        return classRef == null ? null : classRef.get();
    }
}
