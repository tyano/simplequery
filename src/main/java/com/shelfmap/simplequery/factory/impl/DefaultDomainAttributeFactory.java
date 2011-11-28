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
package com.shelfmap.simplequery.factory.impl;

import com.shelfmap.simplequery.Context;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.domain.impl.*;
import com.shelfmap.simplequery.factory.DomainAttributeFactory;
import java.util.Date;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultDomainAttributeFactory implements DomainAttributeFactory {
    private Context context;

    public DefaultDomainAttributeFactory(Context context) {
        super();
        this.context = context;
    }

    @Override
    public <VT, CT> DomainAttribute<VT, CT> createAttribute(Domain<?> domain, String attributeName, Class<VT> valueType, Class<CT> containerType, AttributeConverter<VT> converter, AttributeAccessor<CT> accessor) {
        return new DefaultDomainAttribute<VT,CT>(domain, attributeName, valueType, containerType, converter, accessor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <CT> AttributeAccessor<CT> createAttributeAccessor(Class<?> propertyType, Class<CT> attributeType, String propertyPath) {
        if(ForwardReference.class.isAssignableFrom(propertyType)
           && String.class.isAssignableFrom(attributeType)) {

            return (AttributeAccessor<CT>) new ForwardReferenceAttributeAccessor(context, propertyPath);
        }

        return new PropertyAttributeAccessor<CT>(context, propertyPath);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <VT> AttributeConverter<VT> createAttributeConverter(Class<VT> converterType) {
        if(converterType == null) throw new IllegalArgumentException("the parameter 'attributeType' must not be null.");

        if(Date.class.isAssignableFrom(converterType)) {
            return (AttributeConverter<VT>) new DateAttributeConverter();
        }

        if(Enum.class.isAssignableFrom(converterType)) {
            Class<? extends Enum> enumClass = converterType.asSubclass(Enum.class);
            return (AttributeConverter<VT>) createEnumConverter(enumClass);
        }

        return new DefaultAttributeConverter<VT>(converterType);
    }

    @Override
    public AttributeConverter<Float> createFloatAttributeConverter(int maxDigitLeft, int maxDigitRight, int offset) {
        return new FloatAttributeConverter(maxDigitLeft, maxDigitRight, offset);
    }

    @Override
    public AttributeConverter<Integer> createIntAttributeConverter(int padding, int offset) {
        return new IntAttributeConverter(padding, offset);
    }

    @Override
    public AttributeConverter<Long> createLongAttributeConverter(int padding, long offset) {
        return new LongAttributeConverter(padding, offset);
    }

    protected <E extends Enum<E>> EnumAttributeConverter<E> createEnumConverter(Class<E> type) {
        return new EnumAttributeConverter<E>(type);
    }

    @Override
    public Context getContext() {
        return this.context;
    }
}
