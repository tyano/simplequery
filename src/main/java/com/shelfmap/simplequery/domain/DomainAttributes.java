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

/**
 *
 * @author Tsutomu YANO
 */
public interface DomainAttributes extends Iterable<DomainAttribute<?>> {
    boolean isAttributeDefined(String attributeName);
    <T> DomainAttribute<T> getAttribute(String attributeName, Class<T> type);
    DomainAttribute<?> getAttribute(String attributeName);
    Class<?> getDomainClass();
    String getDomainName();
    <T> void writeAttribute(Object instance, String attributeName, Class<T> type, T value) throws CanNotWriteAttributeException;
}