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

import com.amazonaws.auth.AWSCredentials;
import com.shelfmap.simplequery.domain.*;
import com.shelfmap.simplequery.domain.impl.BeanDomainSnapshot;
import com.shelfmap.simplequery.domain.impl.DefaultDomainFactory;
import com.shelfmap.simplequery.expression.ItemConverter;
import com.shelfmap.simplequery.expression.impl.DefaultItemConverter;

/**
 *
 * @author Tsutomu YANO
 */
public class DefaultContext implements Context {
    private static final long serialVersionUID = 1L;

    private AWSCredentials credentials;

    public DefaultContext(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Client createNewClient() {
        return new SimpleQueryClient(this, credentials);
    }

    @Override
    public <T> ItemConverter<T> getItemConverter(Domain<T> domain) {
        return new DefaultItemConverter<T>(this, domain);
    }

    @Override
    public <T> InstanceFactory<T> getInstanceFactory(Domain<T> domain) {
        return new DefaultInstanceFactory<T>();
    }

    @Override
    public DomainSnapshot createDomainSnapshot(Domain<?> domain) {
        return new BeanDomainSnapshot(this, domain);
    }

    @Override
    public AttributeConverterFactory getAttributeConverterFactory() {
        return new DefaultAttributeConverterFactory(this);
    }

    @Override
    public DomainFactory getDomainFactory() {
        return new DefaultDomainFactory();
    }

    @Override
    public AWSCredentials getCredentials() {
        return this.credentials;
    }
}
