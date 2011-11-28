/*
 * Copyright 2011 Tsutomu YANO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shelfmap.simplequery.factory;

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.domain.AttributeAccessor;
import com.shelfmap.simplequery.domain.AttributeConverter;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainAttribute;

/**
 *
 * @author Tsutomu YANO
 */
public interface DomainAttributeFactory {
    Context getContext();

    <VT,CT> DomainAttribute<VT, CT> createAttribute(Domain<?> domain, String attributeName, Class<VT> valueType, Class<CT> containerType, AttributeConverter<VT> converter, AttributeAccessor<CT> accessor);

    <CT> AttributeAccessor<CT> createAttributeAccessor(Class<?> propertyType, Class<CT> containerType, String propertyPath);

    <VT> AttributeConverter<VT> createAttributeConverter(Class<VT> converterType);
    AttributeConverter<Float> createFloatAttributeConverter(int maxDigitLeft, int maxDigitRight, int offset);
    AttributeConverter<Integer> createIntAttributeConverter(int padding, int offset);
    AttributeConverter<Long> createLongAttributeConverter(int padding, long offset);

}
