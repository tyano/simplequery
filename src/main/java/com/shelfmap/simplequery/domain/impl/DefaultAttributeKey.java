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
import java.io.Serializable;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultAttributeKey implements AttributeKey, Serializable {
    private static final long serialVersionUID = 1L;
    private final String attributeName;
    private final ClassReference valueTypeRef;
    private final ClassReference containerTypeRef;

    public DefaultAttributeKey(String attributeName, Class<?> valueType, Class<?> containerType) {
        this.attributeName = attributeName;
        this.valueTypeRef = new SimpleClassReference(valueType);
        this.containerTypeRef = new SimpleClassReference(containerType);
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public Class<?> getValueType() {
        return valueTypeRef.get();
    }
    
    @Override
    public Class<?> getContainerType() {
        return containerTypeRef.get();
    }

    public DefaultAttributeKey(String attributeName, ClassReference valueTypeRef, ClassReference containerTypeRef) {
        this.attributeName = attributeName;
        this.valueTypeRef = valueTypeRef;
        this.containerTypeRef = containerTypeRef;
    }

    @Override
    public String toString() {
        return "DefaultAttributeKey{" + "attributeName=" + attributeName + ", valueTypeRef=" + valueTypeRef + ", containerTypeRef=" + containerTypeRef + '}';
    }
}
