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
package com.shelfmap.simplequery.domain;

import java.util.Set;

/**
 *
 * @author Tsutomu YANO
 */
public interface AttributeStore {
    <VT,CT> DomainAttribute<VT, CT> putAttribute(String attributeName, Class<VT> valueType, Class<CT> containerType, DomainAttribute<VT,CT> value);
    <VT,CT> DomainAttribute<VT, CT> getAttribute(String attributeName, Class<VT> valueType, Class<CT> containerType);
    Class<?> getValueType(String attributeName);
    Class<?> getContainerType(String attributeName);
    DomainAttribute<?,?> getAttribute(String attributeName);
    boolean isAttributeDefined(String attributeName);
    Set<AttributeKey> keySet();
    Set<DomainAttribute<?,?>> values();
}
