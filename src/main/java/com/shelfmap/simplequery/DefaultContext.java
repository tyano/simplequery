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
package com.shelfmap.simplequery;

import com.shelfmap.simplequery.domain.AttributeConverterFactory;
import com.shelfmap.simplequery.domain.DefaultAttributeConverterFactory;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainAttributes;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.domain.impl.BeanDomainAttributes;
import com.shelfmap.simplequery.domain.impl.DefaultDomainFactory;
import com.shelfmap.simplequery.expression.ItemConverter;
import com.shelfmap.simplequery.expression.impl.DefaultItemConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultContext implements Context {
    private static final long serialVersionUID = 1L;

    @Override
    public Client createNewClient() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> ItemConverter<T> getItemConverter(Domain<T> domain) {
        return new DefaultItemConverter<T>(domain, this);
    }

    @Override
    public <T> InstanceFactory<T> getInstanceFactory(Domain<T> domain) {
        return new DefaultInstanceFactory<T>();
    }

    @Override
    public DomainAttributes getDomainAttributes(Domain<?> domain) {
        return new BeanDomainAttributes(domain, this);
    }

    @Override
    public AttributeConverterFactory getAttributeConverterFactory() {
        return new DefaultAttributeConverterFactory();
    }

    @Override
    public DomainFactory getDomainFactory() {
        return new DefaultDomainFactory();
    }

}
