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

import com.shelfmap.simplequery.domain.AttributeKey;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultAttributeKey implements AttributeKey {
    private final String attributeName;
    private final Class<?> valueType;
    private final Class<?> containerType;

    public DefaultAttributeKey(String attributeName, Class<?> valueType, Class<?> containerType) {
        this.attributeName = attributeName;
        this.valueType = valueType;
        this.containerType = containerType;
    }

    @Override
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public Class<?> getValueType() {
        return valueType;
    }
    
    @Override
    public Class<?> getContainerType() {
        return containerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultAttributeKey other = (DefaultAttributeKey) obj;
        if ((this.attributeName == null) ? (other.attributeName != null) : !this.attributeName.equals(other.attributeName)) {
            return false;
        }
        if (this.valueType != other.valueType && (this.valueType == null || !this.valueType.equals(other.valueType))) {
            return false;
        }
        if (this.containerType != other.containerType && (this.containerType == null || !this.containerType.equals(other.containerType))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.attributeName != null ? this.attributeName.hashCode() : 0);
        hash = 53 * hash + (this.valueType != null ? this.valueType.hashCode() : 0);
        hash = 53 * hash + (this.containerType != null ? this.containerType.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "DefaultAttributeKey{" + "attributeName=" + attributeName + ", valueType=" + valueType.getCanonicalName() + ", containerType=" + containerType.getCanonicalName() + "}";
    }
}
