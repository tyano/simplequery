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
import com.shelfmap.simplequery.domain.AttributeConverterFactory;
import com.shelfmap.simplequery.domain.DefaultAttributeConverterFactory;
import com.shelfmap.simplequery.domain.Domain;
import com.shelfmap.simplequery.domain.DomainFactory;
import com.shelfmap.simplequery.domain.impl.DefaultDomainFactory;
import com.shelfmap.simplequery.factory.DomainDescriptorFactory;
import com.shelfmap.simplequery.factory.ItemConverterFactory;
import com.shelfmap.simplequery.factory.impl.DefaultDomainDescriptorFactory;
import com.shelfmap.simplequery.factory.impl.DefaultItemConverterFactory;

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
        return new SimpleQueryClient(this);
    }

    @Override
    public ItemConverterFactory getItemConverterFactory() {
        return new DefaultItemConverterFactory(this);
    }

    @Override
    public <T> DomainInstanceFactory<T> getDomainInstanceFactory(Domain<T> domain) {
        return new DefaultInstanceFactory<T>();
    }

    @Override
    public DomainDescriptorFactory getDomainDescriptorFactory() {
        return new DefaultDomainDescriptorFactory(this);
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
